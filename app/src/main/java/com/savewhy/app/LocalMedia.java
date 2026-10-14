package com.savewhy.app;
import android.content.*;
import android.database.Cursor;
import android.graphics.*;
import android.media.MediaMetadataRetriever;
import android.net.Uri;
import android.os.*;
import android.provider.OpenableColumns;
import android.widget.ImageView;
import androidx.exifinterface.media.ExifInterface;
import java.io.*;
import java.util.*;
import java.util.concurrent.Executor;

/** Read existing documents through persistent, read-only grants. Originals are never copied. */
final class LocalMedia {
    private final Context context;
    LocalMedia(Context context){this.context=context.getApplicationContext();}
    boolean persist(Uri uri){if(uri==null||!"content".equals(uri.getScheme()))return false;for(UriPermission grant:context.getContentResolver().getPersistedUriPermissions())if(grant.isReadPermission()&&grant.getUri().equals(uri))return true;try{context.getContentResolver().takePersistableUriPermission(uri,Intent.FLAG_GRANT_READ_URI_PERMISSION);return true;}catch(SecurityException e){return false;}}
    String name(Uri uri){try(Cursor c=context.getContentResolver().query(uri,new String[]{OpenableColumns.DISPLAY_NAME},null,null,null)){if(c!=null&&c.moveToFirst()){String name=c.getString(0);if(name!=null)return name.substring(0,Math.min(160,name.length()));}}catch(Exception ignored){}return "";}
    static String identity(Uri uri)throws Exception{return ImageStore.digest(uri.toString().getBytes(java.nio.charset.StandardCharsets.UTF_8));}
    boolean accessible(Uri uri){try(InputStream in=context.getContentResolver().openInputStream(uri)){return in!=null;}catch(Exception e){return false;}}
    Bitmap bitmap(Uri uri,boolean video,int max)throws Exception{
        if(video){MediaMetadataRetriever retriever=new MediaMetadataRetriever();try{retriever.setDataSource(context,uri);if(Build.VERSION.SDK_INT>=27)return retriever.getScaledFrameAtTime(-1,MediaMetadataRetriever.OPTION_CLOSEST_SYNC,max,max);int w=Integer.parseInt(retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_VIDEO_WIDTH)),h=Integer.parseInt(retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_VIDEO_HEIGHT));return w<=4096&&h<=4096?retriever.getFrameAtTime():null;}finally{retriever.release();}}
        BitmapFactory.Options options=new BitmapFactory.Options();options.inJustDecodeBounds=true;try(InputStream in=context.getContentResolver().openInputStream(uri)){if(in==null)throw new IOException("Missing file");BitmapFactory.decodeStream(in,null,options);}if(options.outWidth<1||options.outHeight<1||options.outWidth>16384||options.outHeight>16384||(long)options.outWidth*options.outHeight>100000000)throw new IOException("Image bounds");options.inJustDecodeBounds=false;options.inSampleSize=1;while(Math.max(options.outWidth,options.outHeight)/options.inSampleSize>max)options.inSampleSize*=2;
        Bitmap bitmap;try(InputStream in=context.getContentResolver().openInputStream(uri)){if(in==null)throw new IOException("Missing file");bitmap=BitmapFactory.decodeStream(in,null,options);}if(bitmap==null)throw new IOException("Invalid image");
        try(InputStream in=context.getContentResolver().openInputStream(uri)){if(in!=null){ExifInterface exif=new ExifInterface(in);Matrix matrix=new Matrix();int degrees=exif.getRotationDegrees();if(degrees!=0)matrix.postRotate(degrees);if(exif.isFlipped())matrix.postScale(-1,1);if(degrees!=0||exif.isFlipped()){Bitmap oriented=Bitmap.createBitmap(bitmap,0,0,bitmap.getWidth(),bitmap.getHeight(),matrix,true);if(oriented!=bitmap){bitmap.recycle();bitmap=oriented;}}}}catch(IOException ignored){}return bitmap;
    }
    void bind(ImageView view,String uri,boolean video,Executor worker,Handler main){String key="reference:"+uri;view.setTag(key);view.setImageTintList(null);view.setScaleType(ImageView.ScaleType.CENTER_INSIDE);view.setImageResource(video?R.drawable.ic_watch:R.drawable.ic_image);java.lang.ref.WeakReference<ImageView> target=new java.lang.ref.WeakReference<>(view);worker.execute(()->{try{Bitmap bitmap=bitmap(Uri.parse(uri),video,320);if(bitmap==null)return;main.post(()->{ImageView current=target.get();if(current!=null&&key.equals(current.getTag())){current.setScaleType(ImageView.ScaleType.CENTER_CROP);current.setImageBitmap(bitmap);}else bitmap.recycle();});}catch(Exception ignored){}});}
}
