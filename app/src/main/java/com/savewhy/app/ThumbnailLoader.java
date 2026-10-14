package com.savewhy.app;
import android.content.Context;
import android.graphics.*;
import android.os.*;
import android.util.LruCache;
import android.widget.ImageView;
import java.io.*;
import java.lang.ref.WeakReference;
import java.net.*;
import java.util.*;
import java.util.concurrent.*;

/** Bounded, key-based image cache; private/source URLs are never fetched here. */
public final class ThumbnailLoader implements AutoCloseable {
    private final File directory;private final Handler main=new Handler(Looper.getMainLooper());private volatile boolean closed;
    private final LruCache<String,Bitmap> memory=new LruCache<String,Bitmap>(8*1024*1024){protected int sizeOf(String k,Bitmap b){return b.getByteCount();}};
    private final ThreadPoolExecutor worker=new ThreadPoolExecutor(2,2,0,TimeUnit.SECONDS,new ArrayBlockingQueue<>(32),new ThreadPoolExecutor.DiscardOldestPolicy());
    public ThumbnailLoader(Context context){directory=new File(context.getCacheDir(),"thumbnails");directory.mkdirs();}
    public void bind(ImageView view,String url,int fallback){
        String id=VideoLinks.youtubeId(url);view.setTag(id);view.setScaleType(ImageView.ScaleType.CENTER_INSIDE);view.setImageTintList(android.content.res.ColorStateList.valueOf(androidx.core.content.ContextCompat.getColor(view.getContext(),R.color.app_muted)));view.setImageResource(fallback);
        if(id.isEmpty()||closed)return;Bitmap hit=memory.get(id);if(hit!=null){view.setScaleType(ImageView.ScaleType.CENTER_CROP);view.setImageTintList(null);view.setImageBitmap(hit);return;}
        WeakReference<ImageView> target=new WeakReference<>(view);worker.execute(()->{
            Bitmap bitmap=load(id);if(bitmap==null||closed)return;memory.put(id,bitmap);main.post(()->{ImageView current=target.get();if(!closed&&current!=null&&id.equals(current.getTag())){current.setScaleType(ImageView.ScaleType.CENTER_CROP);current.setImageTintList(null);current.setImageBitmap(bitmap);}});
        });
    }
    private Bitmap load(String id){
        File file=new File(directory,id+".jpg");
        try {
            byte[] bytes;if(file.isFile())bytes=read(new FileInputStream(file));else {
                HttpURLConnection connection=(HttpURLConnection)new URL(VideoLinks.thumbnail(id)).openConnection();connection.setInstanceFollowRedirects(false);connection.setConnectTimeout(3000);connection.setReadTimeout(5000);
                try {if(connection.getResponseCode()!=200)return null;String type=connection.getContentType();if(type==null||!type.startsWith("image/"))return null;bytes=read(connection.getInputStream());}finally{connection.disconnect();}
            }
            BitmapFactory.Options options=new BitmapFactory.Options();options.inJustDecodeBounds=true;BitmapFactory.decodeByteArray(bytes,0,bytes.length,options);if(options.outWidth<1||options.outHeight<1||options.outWidth>4096||options.outHeight>4096)return null;
            options.inJustDecodeBounds=false;options.inSampleSize=1;while(options.outWidth/options.inSampleSize>640)options.inSampleSize*=2;Bitmap bitmap=BitmapFactory.decodeByteArray(bytes,0,bytes.length,options);
            if(bitmap==null&&file.exists())file.delete();
            if(bitmap!=null&&!file.exists()) {File temp=new File(directory,id+"-"+UUID.randomUUID()+".tmp");try(FileOutputStream out=new FileOutputStream(temp)){out.write(bytes);}if(!temp.renameTo(file))temp.delete();trim();}
            return bitmap;
        }catch(Exception e){return null;}
    }
    private byte[] read(InputStream stream)throws IOException {try(InputStream in=stream;ByteArrayOutputStream out=new ByteArrayOutputStream()){byte[] buffer=new byte[8192];int n;while((n=in.read(buffer))!=-1){if(closed||Thread.currentThread().isInterrupted()||out.size()+n>1024*1024)throw new IOException("Image limit");out.write(buffer,0,n);}return out.toByteArray();}}
    private void trim(){File[] files=directory.listFiles((d,name)->name.endsWith(".jpg"));if(files==null||files.length<=40)return;Arrays.sort(files,Comparator.comparingLong(File::lastModified));for(int i=0;i<files.length-40;i++)files[i].delete();}
    @Override public void close(){closed=true;worker.shutdownNow();main.removeCallbacksAndMessages(null);memory.evictAll();}
}
