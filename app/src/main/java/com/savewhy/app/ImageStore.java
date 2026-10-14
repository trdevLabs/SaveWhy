package com.savewhy.app;

import android.content.Context;
import android.graphics.*;
import android.widget.ImageView;
import androidx.exifinterface.media.ExifInterface;
import java.io.*;
import java.security.MessageDigest;
import java.util.*;

/** Content-addressed private JPEG copies, never dependent on a gallery URI grant. */
final class ImageStore {
    static final int MAX_IMAGE_BYTES=2*1024*1024, MAX_INPUT_BYTES=12*1024*1024;
    private final File directory;
    ImageStore(Context context){this(new File(context.getFilesDir(),"images"));}
    ImageStore(File directory){this.directory=directory;directory.mkdirs();}
    File file(String id){if(!id.matches("[a-f0-9]{64}"))throw new IllegalArgumentException("Invalid image id");return new File(directory,id+".jpg");}
    String ingest(InputStream input)throws Exception{
        byte[] bytes=read(input,MAX_INPUT_BYTES);Bitmap bitmap=decode(bytes,2560);if(bitmap==null)throw new IOException("Invalid image");
        ExifInterface exif=new ExifInterface(new ByteArrayInputStream(bytes));Matrix matrix=new Matrix();int rotation=exif.getRotationDegrees();if(rotation!=0)matrix.postRotate(rotation);if(exif.isFlipped())matrix.postScale(-1,1);if(rotation!=0||exif.isFlipped()){Bitmap oriented=Bitmap.createBitmap(bitmap,0,0,bitmap.getWidth(),bitmap.getHeight(),matrix,true);if(oriented!=bitmap){bitmap.recycle();bitmap=oriented;}}
        try{ByteArrayOutputStream out=new ByteArrayOutputStream();for(int quality=92;quality>=52;quality-=10){out.reset();if(!bitmap.compress(Bitmap.CompressFormat.JPEG,quality,out))throw new IOException("Image compression failed");if(out.size()<=MAX_IMAGE_BYTES)break;}if(out.size()>MAX_IMAGE_BYTES)throw new IOException("Image limit");return install(out.toByteArray());}finally{bitmap.recycle();}
    }
    static String digest(byte[] bytes)throws Exception{StringBuilder result=new StringBuilder();for(byte b:MessageDigest.getInstance("SHA-256").digest(bytes))result.append(String.format(Locale.ROOT,"%02x",b&255));return result.toString();}
    String install(byte[] bytes)throws Exception{if(bytes.length>MAX_IMAGE_BYTES)throw new IOException("Image limit");String id=digest(bytes);File file=file(id);if(file.isFile())return id;File temp=new File(directory,UUID.randomUUID()+".tmp");try{try(FileOutputStream out=new FileOutputStream(temp)){out.write(bytes);}if(!temp.renameTo(file))throw new IOException("Cannot save image");}finally{temp.delete();}return id;}
    void validate(String id,byte[] bytes)throws Exception{if(!id.equals(digest(bytes))||bytes.length>MAX_IMAGE_BYTES)throw new IOException("Image checksum mismatch");Bitmap b=decode(bytes,64);if(b==null)throw new IOException("Invalid backup image");b.recycle();}
    Bitmap bitmap(String id,int max)throws IOException{return decode(read(new FileInputStream(file(id)),MAX_IMAGE_BYTES),max);}
    boolean exists(String id){return file(id).isFile();}
    void bind(ImageView view,String id,java.util.concurrent.Executor executor,android.os.Handler main){view.setTag("image:"+id);view.setScaleType(ImageView.ScaleType.CENTER_INSIDE);view.setImageTintList(null);view.setImageResource(R.drawable.ic_image);java.lang.ref.WeakReference<ImageView> target=new java.lang.ref.WeakReference<>(view);executor.execute(()->{try{Bitmap bitmap=bitmap(id,320);main.post(()->{ImageView v=target.get();if(v!=null&&("image:"+id).equals(v.getTag())){v.setImageTintList(null);v.setScaleType(ImageView.ScaleType.CENTER_CROP);v.setImageBitmap(bitmap);}});}catch(Exception ignored){}});}
    void retainForUndo(String id){File file=file(id);if(file.exists())file.setLastModified(System.currentTimeMillis());}
    void cleanup(Set<String> retained,long now){File[] files=directory.listFiles();if(files==null)return;for(File file:files){String name=file.getName();String id=name.endsWith(".jpg")?name.substring(0,name.length()-4):"";if(!retained.contains(id)&&now-file.lastModified()>7L*86400000&&(name.matches("[a-f0-9]{64}\\.jpg")||name.endsWith(".tmp")))file.delete();}}
    static Bitmap decode(byte[] bytes,int max){BitmapFactory.Options o=new BitmapFactory.Options();o.inJustDecodeBounds=true;BitmapFactory.decodeByteArray(bytes,0,bytes.length,o);if(o.outWidth<1||o.outHeight<1||o.outWidth>16384||o.outHeight>16384||(long)o.outWidth*o.outHeight>100000000)return null;o.inJustDecodeBounds=false;o.inSampleSize=1;while(Math.max(o.outWidth,o.outHeight)/o.inSampleSize>max)o.inSampleSize*=2;return BitmapFactory.decodeByteArray(bytes,0,bytes.length,o);}
    static byte[] read(InputStream input,int max)throws IOException{if(input==null)throw new IOException("Missing image");try(InputStream in=input;ByteArrayOutputStream out=new ByteArrayOutputStream()){byte[] b=new byte[8192];int n;while((n=in.read(b))!=-1){if(out.size()+n>max)throw new IOException("File limit");out.write(b,0,n);}return out.toByteArray();}}
}
