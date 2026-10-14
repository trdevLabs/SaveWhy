package com.savewhy.app;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.*;
import java.util.zip.*;
/** Portable ZIP with bounded entries, complete validation and staged asset restoration. */
final class MediaBackup {
    static final long MAX_TOTAL=2L*1024*1024*1024;
    static void write(OutputStream output,String json,List<Card> cards,ImageStore images,File previews)throws Exception{write(output,json,cards,images,previews,null);}
    static void write(OutputStream output,String json,List<Card> cards,ImageStore images,File previews,VideoStore videos)throws Exception{
        byte[] metadata=json.getBytes(StandardCharsets.UTF_8);if(metadata.length>CardCodec.MAX_BYTES)throw new IOException("Metadata limit");
        try(ZipOutputStream zip=new ZipOutputStream(output)){long total=metadata.length;entry(zip,"records.json",metadata);Set<String> paths=new HashSet<>();for(Card c:cards){
            if(c.isReference())continue;
            if(c.hasVisual()){String name="images/"+c.imageId+".jpg";if(paths.add(name)){byte[] data=ImageStore.read(new FileInputStream(images.file(c.imageId)),ImageStore.MAX_IMAGE_BYTES);total+=data.length;if(total>MAX_TOTAL)throw new IOException("Backup limit");entry(zip,name,data);}}
            else{String key=SocialPreview.key(c.url);File file=new File(previews,key+".jpg");String name="previews/"+key+".jpg";if(file.isFile()&&paths.add(name)){byte[] data=ImageStore.read(new FileInputStream(file),1024*1024);total+=data.length;if(total>MAX_TOTAL)throw new IOException("Backup limit");entry(zip,name,data);}}
            if(c.isVideo()){String name="videos/"+c.videoId()+".mp4";if(paths.add(name)){if(videos==null)throw new IOException("Missing videos");File file=videos.file(c.videoId());total+=file.length();if(total>MAX_TOTAL)throw new IOException("Backup limit");zip.putNextEntry(new ZipEntry(name));if(!c.videoId().equals(VideoStore.copy(new FileInputStream(file),zip,VideoStore.MAX_BYTES)))throw new IOException("Video checksum mismatch");zip.closeEntry();}}
        }}
    }
    private static void entry(ZipOutputStream zip,String name,byte[] bytes)throws IOException{zip.putNextEntry(new ZipEntry(name));zip.write(bytes);zip.closeEntry();}
    static final class Pending implements AutoCloseable {
        final String json;final List<Card> cards;final File staged;final Map<String,File> assets;
        Pending(String json,List<Card> cards,File staged,Map<String,File> assets){this.json=json;this.cards=cards;this.staged=staged;this.assets=assets;}
        Install install(ImageStore images,File previews,List<Card> merged)throws Exception{return install(images,previews,merged,null);}
        Install install(ImageStore images,File previews,List<Card> merged,VideoStore videos)throws Exception{
            Install result=new Install();try{Set<String> needed=new HashSet<>();for(Card card:merged){if(card.isReference())continue;needed.add(card.hasVisual()?"images/"+card.imageId+".jpg":"previews/"+SocialPreview.key(card.url)+".jpg");if(card.isVideo())needed.add("videos/"+card.videoId()+".mp4");}previews.mkdirs();for(Map.Entry<String,File> asset:assets.entrySet()){String name=asset.getKey();if(!needed.contains(name))continue;File destination=name.startsWith("images/")?images.file(name.substring(7,name.length()-4)):name.startsWith("videos/")?videoDestination(videos,name):new File(previews,name.substring(9));if(destination.exists())continue;File temporary=new File(destination.getParentFile(),UUID.randomUUID()+".tmp");try{Files.copy(asset.getValue().toPath(),temporary.toPath());if(!temporary.renameTo(destination))throw new IOException("Cannot restore image");result.created.add(destination);}finally{temporary.delete();}}return result;}catch(Exception e){result.close();throw e;}
        }
        private File videoDestination(VideoStore videos,String name)throws IOException{if(videos==null)throw new IOException("Missing video storage");return videos.file(name.substring(7,name.length()-4));}
        public void close(){if(staged!=null){File[] files=staged.listFiles();if(files!=null)for(File file:files)file.delete();staged.delete();}}
    }
    static final class Install implements AutoCloseable {private final List<File> created=new ArrayList<>();private boolean committed;void commit(){committed=true;}public void close(){if(!committed)for(File file:created)file.delete();}}
    static Pending read(InputStream input,File cache,ImageStore images)throws Exception{
        PushbackInputStream peek=new PushbackInputStream(input,4);byte[] head=new byte[4];int count=0,n;while(count<4&&(n=peek.read(head,count,4-count))>0)count+=n;peek.unread(head,0,count);
        if(count<4||head[0]!='P'||head[1]!='K'){String json=new String(ImageStore.read(peek,CardCodec.MAX_BYTES),StandardCharsets.UTF_8);List<Card> cards=CardCodec.decode(json);for(Card c:cards)if(c.hasVisual()&&!c.isReference())throw new IOException("Media requires a ZIP backup");return new Pending(json,cards,null,Collections.emptyMap());}
        File staged=new File(cache,"restore-"+UUID.randomUUID());if(!staged.mkdirs())throw new IOException("Cannot stage backup");Map<String,File> assets=new LinkedHashMap<>();Set<String> names=new HashSet<>();String json=null;long total=0;
        try(ZipInputStream zip=new ZipInputStream(peek)){ZipEntry entry;while((entry=zip.getNextEntry())!=null){String name=entry.getName();if(!names.add(name)||names.size()>10001||entry.isDirectory())throw new IOException("Invalid duplicate entry");boolean metadata=name.equals("records.json");boolean image=name.matches("images/[a-f0-9]{64}\\.jpg"),preview=name.matches("previews/(?:[a-f0-9]{64}|[A-Za-z0-9_-]{11})\\.jpg"),video=name.matches("videos/[a-f0-9]{64}\\.mp4");if(!metadata&&!image&&!preview&&!video)throw new IOException("Invalid backup path");
            if(video){File file=new File(staged,UUID.randomUUID()+".mp4");try(OutputStream out=new FileOutputStream(file)){String id=VideoStore.copy(zip,out,VideoStore.MAX_BYTES,false);if(!id.equals(name.substring(7,name.length()-4)))throw new IOException("Video checksum mismatch");}total+=file.length();assets.put(name,file);}
            else{byte[] data=readEntry(zip,metadata?CardCodec.MAX_BYTES:image?ImageStore.MAX_IMAGE_BYTES:1024*1024);total+=data.length;if(metadata)json=new String(data,StandardCharsets.UTF_8);else{if(image)images.validate(name.substring(7,name.length()-4),data);else{android.graphics.Bitmap bitmap=ImageStore.decode(data,64);if(bitmap==null)throw new IOException("Invalid preview");bitmap.recycle();}File file=new File(staged,UUID.randomUUID()+".jpg");try(FileOutputStream out=new FileOutputStream(file)){out.write(data);}assets.put(name,file);}}
            if(total>MAX_TOTAL)throw new IOException("Backup limit");zip.closeEntry();}
            if(json==null)throw new IOException("Missing backup metadata");List<Card> cards=CardCodec.decode(json);Set<String> referenced=new HashSet<>();for(Card card:cards){if(card.isReference())continue;String name=card.hasVisual()?"images/"+card.imageId+".jpg":"previews/"+SocialPreview.key(card.url)+".jpg";referenced.add(name);if(card.hasVisual()&&!assets.containsKey(name))throw new IOException("Missing backup image");if(card.isVideo()){String video="videos/"+card.videoId()+".mp4";referenced.add(video);if(!assets.containsKey(video))throw new IOException("Missing backup video");}}if(!referenced.containsAll(assets.keySet()))throw new IOException("Unreferenced backup media");return new Pending(json,cards,staged,assets);

        }catch(Exception e){new Pending("",Collections.emptyList(),staged,assets).close();throw e;}
    }
    private static byte[] readEntry(InputStream input,int max)throws IOException{ByteArrayOutputStream out=new ByteArrayOutputStream();byte[] buffer=new byte[8192];int n;while((n=input.read(buffer))!=-1){if(out.size()+n>max)throw new IOException("Backup entry limit");out.write(buffer,0,n);}return out.toByteArray();}
}
