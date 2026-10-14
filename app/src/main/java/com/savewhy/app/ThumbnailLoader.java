package com.savewhy.app;
import android.content.Context;
import android.graphics.*;
import android.os.*;
import android.util.LruCache;
import android.widget.ImageView;
import java.io.*;
import java.lang.ref.WeakReference;
import java.net.*;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.concurrent.*;
import java.util.function.Consumer;

/** Bounded public previews, with durable user-selected images taking precedence. */
public final class ThumbnailLoader implements AutoCloseable {
    private final File directory,selected;private final Handler main=new Handler(Looper.getMainLooper());private volatile boolean closed;
    private final LruCache<String,Bitmap> memory=new LruCache<String,Bitmap>(8*1024*1024){protected int sizeOf(String k,Bitmap b){return b.getByteCount();}};
    private final Map<String,Long> failed=new ConcurrentHashMap<>();
    private final ThreadPoolExecutor worker=new ThreadPoolExecutor(2,2,0,TimeUnit.SECONDS,new ArrayBlockingQueue<>(32),new ThreadPoolExecutor.AbortPolicy());
    public ThumbnailLoader(Context context){directory=new File(context.getCacheDir(),"thumbnails");selected=new File(context.getFilesDir(),"previews");directory.mkdirs();selected.mkdirs();}
    public void bind(ImageView view,String url,int fallback){
        String id=SocialPreview.key(url);view.setTag(id);view.setScaleType(ImageView.ScaleType.CENTER_INSIDE);view.setImageTintList(android.content.res.ColorStateList.valueOf(Palettes.color(view.getContext(),R.color.app_muted)));view.setImageResource(fallback);
        if(closed)return;Bitmap hit=memory.get(id);if(hit!=null){apply(view,hit);return;}
        WeakReference<ImageView> target=new WeakReference<>(view);try{worker.execute(()->{
            Bitmap loaded=load(url,id);if(loaded==null||closed)return;Bitmap preferred=loaded;synchronized(memory){File manual=new File(selected,id+".jpg");if(manual.isFile())try{Bitmap choice=decode(read(new FileInputStream(manual),1024*1024));if(choice!=null)preferred=choice;}catch(IOException ignored){}memory.put(id,preferred);}final Bitmap bitmap=preferred;main.post(()->{ImageView current=target.get();if(!closed&&current!=null&&id.equals(current.getTag()))apply(current,bitmap);});
        });}catch(RejectedExecutionException ignored){}
    }
    private void apply(ImageView view,Bitmap bitmap){view.setScaleType(ImageView.ScaleType.CENTER_CROP);view.setImageTintList(null);view.setImageBitmap(bitmap);}
    private Bitmap load(String url,String id){
        File manual=new File(selected,id+".jpg"),file=new File(directory,id+".jpg");
        try {
            if(manual.isFile())return decode(read(new FileInputStream(manual),1024*1024));
            if(file.isFile()){Bitmap bitmap=decode(read(new FileInputStream(file),1024*1024));if(bitmap!=null)return bitmap;file.delete();}
            if(System.currentTimeMillis()-failed.getOrDefault(id,0L)<300000)return null;
            {
                String image=VideoLinks.youtubeId(url).isEmpty()?resolve(url):VideoLinks.thumbnail(VideoLinks.youtubeId(url));
                if(image.isEmpty()){failed.put(id,System.currentTimeMillis());return null;}
                byte[] bytes=request(image,true).bytes;Bitmap bitmap=decode(bytes);if(bitmap==null){failed.put(id,System.currentTimeMillis());return null;}write(file,bytes);trim();return bitmap;
            }
        }catch(Exception e){failed.put(id,System.currentTimeMillis());return null;}
    }
    private String resolve(String url)throws Exception {
        if(!SocialPreview.allowed(url,false))return "";
        if(SocialPreview.domain(URI.create(url).getHost(),"tiktok.com")&&URI.create(url).getPath().contains("/video/")){String image=tiktokImage(url);if(!image.isEmpty())return image;}
        Reply page=request(url,false);
        if(SocialPreview.domain(URI.create(page.url).getHost(),"tiktok.com")){String image=tiktokImage(page.url);if(!image.isEmpty())return image;}
        return SocialPreview.image(new String(page.bytes,StandardCharsets.UTF_8),page.url);
    }
    private String tiktokImage(String url){try{String endpoint="https://www.tiktok.com/oembed?url="+URLEncoder.encode(url,"UTF-8");String image=new org.json.JSONObject(new String(request(endpoint,false).bytes,StandardCharsets.UTF_8)).optString("thumbnail_url");return SocialPreview.allowed(image,true)?image:"";}catch(Exception ignored){return "";}}
    private static final class Reply {final String url;final byte[] bytes;Reply(String u,byte[] b){url=u;bytes=b;}}
    private Reply request(String original,boolean image)throws Exception {
        String url=original;
        for(int redirects=0;redirects<=4;redirects++){
            if(!SocialPreview.allowed(url,image))throw new IOException("Untrusted preview URL");URL target=new URL(url);InetAddress[] addresses=InetAddress.getAllByName(target.getHost());if(addresses.length==0)throw new IOException("No address");for(InetAddress address:addresses)if(!SocialPreview.publicAddress(address))throw new IOException("Non-public address");
            HttpURLConnection connection=(HttpURLConnection)target.openConnection();connection.setInstanceFollowRedirects(false);connection.setConnectTimeout(3000);connection.setReadTimeout(5000);connection.setRequestProperty("User-Agent","Mozilla/5.0 (Linux; Android 13) AppleWebKit/537.36 Chrome/120.0 Mobile Safari/537.36 tosave/0.9");connection.setRequestProperty("Accept",image?"image/*":"text/html,application/json");
            try{int status=connection.getResponseCode();if(status==301||status==302||status==303||status==307||status==308){String location=connection.getHeaderField("Location");if(location==null)throw new IOException("Missing redirect");url=target.toURI().resolve(location).toString();continue;}if(status!=200)throw new IOException("Preview unavailable");String type=connection.getContentType();if(type==null||!(image?type.toLowerCase(Locale.ROOT).startsWith("image/"):type.toLowerCase(Locale.ROOT).startsWith("text/html")||type.toLowerCase(Locale.ROOT).startsWith("application/json")))throw new IOException("Unexpected preview type");return new Reply(url,read(connection.getInputStream(),1024*1024));}finally{connection.disconnect();}
        }throw new IOException("Too many redirects");
    }
    interface Input {InputStream open()throws IOException;}
    void select(String url,Input input,Consumer<Boolean> callback){
        if(closed)return;try{worker.execute(()->{boolean success=false;try{Bitmap bitmap=decode(read(input.open(),8*1024*1024));if(bitmap!=null){ByteArrayOutputStream out=new ByteArrayOutputStream();bitmap.compress(Bitmap.CompressFormat.JPEG,85,out);if(out.size()>1024*1024)throw new IOException("Image limit");String key=SocialPreview.key(url);synchronized(memory){write(new File(selected,key+".jpg"),out.toByteArray());memory.put(key,bitmap);}failed.remove(key);success=true;}}catch(Exception ignored){}final boolean result=success;main.post(()->{if(!closed)callback.accept(result);});});}catch(RejectedExecutionException e){callback.accept(false);}
    }
    private Bitmap decode(byte[] bytes){BitmapFactory.Options o=new BitmapFactory.Options();o.inJustDecodeBounds=true;BitmapFactory.decodeByteArray(bytes,0,bytes.length,o);if(o.outWidth<1||o.outHeight<1||o.outWidth>8192||o.outHeight>8192)return null;o.inJustDecodeBounds=false;o.inSampleSize=1;while(Math.max(o.outWidth,o.outHeight)/o.inSampleSize>640)o.inSampleSize*=2;return BitmapFactory.decodeByteArray(bytes,0,bytes.length,o);}
    private byte[] read(InputStream stream,int max)throws IOException {if(stream==null)throw new IOException("No image");try(InputStream in=stream;ByteArrayOutputStream out=new ByteArrayOutputStream()){byte[] buffer=new byte[8192];int n;while((n=in.read(buffer))!=-1){if(closed||Thread.currentThread().isInterrupted()||out.size()+n>max)throw new IOException("Preview limit");out.write(buffer,0,n);}return out.toByteArray();}}
    private void write(File file,byte[] bytes)throws IOException {File temp=new File(file.getParentFile(),UUID.randomUUID()+".tmp");try{try(FileOutputStream out=new FileOutputStream(temp)){out.write(bytes);}if(!temp.renameTo(file))throw new IOException("Cannot save image");}finally{temp.delete();}}
    private void trim(){File[] files=directory.listFiles((d,name)->name.endsWith(".jpg"));if(files==null||files.length<=40)return;Arrays.sort(files,Comparator.comparingLong(File::lastModified));for(int i=0;i<files.length-40;i++)files[i].delete();}
    @Override public void close(){closed=true;worker.shutdownNow();main.removeCallbacksAndMessages(null);memory.evictAll();}
}
