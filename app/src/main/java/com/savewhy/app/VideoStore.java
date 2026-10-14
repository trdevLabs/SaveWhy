package com.savewhy.app;
import android.content.Context;
import android.graphics.*;
import android.media.MediaMetadataRetriever;
import android.os.Build;
import java.io.*;
import java.security.MessageDigest;
import java.util.*;
/** Bounded private video copies plus a frame poster; no cloud/video analysis. */
final class VideoStore {
    static final long MAX_BYTES=64L*1024*1024;private final File directory;private final ImageStore images;
    static final class Clip{final String id,poster;Clip(String id,String poster){this.id=id;this.poster=poster;}}
    VideoStore(Context context,ImageStore images){this(new File(context.getFilesDir(),"videos"),images);}
    VideoStore(File directory,ImageStore images){this.directory=directory;this.images=images;directory.mkdirs();}
    File file(String id){if(!id.matches("[a-f0-9]{64}"))throw new IllegalArgumentException("Invalid video id");return new File(directory,id+".mp4");}
    Clip ingest(InputStream input)throws Exception{
        File temporary=new File(directory,UUID.randomUUID()+".tmp");try{String id;try(OutputStream out=new FileOutputStream(temporary)){id=copy(input,out,MAX_BYTES);}Bitmap poster=null;MediaMetadataRetriever retriever=new MediaMetadataRetriever();try{retriever.setDataSource(temporary.getAbsolutePath());if(!"yes".equals(retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_HAS_VIDEO)))throw new IOException("Not a playable video");try{if(Build.VERSION.SDK_INT>=27)poster=retriever.getScaledFrameAtTime(-1,MediaMetadataRetriever.OPTION_CLOSEST_SYNC,640,640);else{int w=Integer.parseInt(retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_VIDEO_WIDTH)),h=Integer.parseInt(retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_VIDEO_HEIGHT));if(w<=4096&&h<=4096)poster=retriever.getFrameAtTime();}}catch(Exception ignored){}}finally{retriever.release();}
            if(poster==null){poster=Bitmap.createBitmap(320,180,Bitmap.Config.ARGB_8888);Canvas canvas=new Canvas(poster);canvas.drawColor(Color.BLACK);Paint paint=new Paint(Paint.ANTI_ALIAS_FLAG);paint.setColor(Color.rgb(148,230,195));Path triangle=new Path();triangle.moveTo(140,65);triangle.lineTo(185,90);triangle.lineTo(140,115);triangle.close();canvas.drawPath(triangle,paint);}
            String image;try{ByteArrayOutputStream out=new ByteArrayOutputStream();poster.compress(Bitmap.CompressFormat.JPEG,85,out);image=images.ingest(new ByteArrayInputStream(out.toByteArray()));}finally{poster.recycle();}
            File destination=file(id);if(destination.exists())temporary.delete();else if(!temporary.renameTo(destination))throw new IOException("Cannot save video");return new Clip(id,image);
        }finally{temporary.delete();}
    }
    static String copy(InputStream input,OutputStream output,long max)throws Exception{return copy(input,output,max,true);}
    static String copy(InputStream input,OutputStream output,long max,boolean closeInput)throws Exception{if(input==null)throw new IOException("Missing video");MessageDigest hash=MessageDigest.getInstance("SHA-256");try{byte[] buffer=new byte[8192];long total=0;int count;while((count=input.read(buffer))!=-1){if(Thread.currentThread().isInterrupted()||(total+=count)>max)throw new IOException("Video limit");output.write(buffer,0,count);hash.update(buffer,0,count);}if(total==0)throw new IOException("Empty video");}finally{if(closeInput)input.close();}StringBuilder id=new StringBuilder();for(byte b:hash.digest())id.append(String.format(Locale.ROOT,"%02x",b&255));return id.toString();}
    static String digest(File file)throws Exception{return copy(new FileInputStream(file),new OutputStream(){public void write(int b){}public void write(byte[] b,int off,int len){}},MAX_BYTES);}
    void cleanup(Set<String> keep,long now){File[] files=directory.listFiles();if(files==null)return;for(File file:files){String name=file.getName(),id=name.endsWith(".mp4")?name.substring(0,name.length()-4):"";if(!keep.contains(id)&&now-file.lastModified()>7L*86400000&&(name.matches("[a-f0-9]{64}\\.mp4")||name.endsWith(".tmp")))file.delete();}}
    void retainForUndo(String id){File file=file(id);if(file.exists())file.setLastModified(System.currentTimeMillis());}
}
