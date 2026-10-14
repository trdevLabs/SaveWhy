package com.savewhy.app;
import java.net.URI;
import java.util.*;
/** Only canonical YouTube video identifiers may reach the thumbnail endpoints. */
public final class VideoLinks {
    private VideoLinks(){}
    public static String youtubeId(String url){
        try {
            URI uri=URI.create(url);String host=uri.getHost();if(host==null)return "";host=host.toLowerCase(Locale.ROOT);
            if(!"https".equalsIgnoreCase(uri.getScheme())&&!"http".equalsIgnoreCase(uri.getScheme()))return "";
            if(uri.getRawUserInfo()!=null)return "";
            String id="",path=uri.getPath()==null?"":uri.getPath();
            if(host.equals("youtu.be")){String[] parts=path.split("/");if(parts.length==2)id=parts[1];}
            else if(host.equals("youtube.com")||host.endsWith(".youtube.com")){
                if(path.equals("/watch")&&uri.getRawQuery()!=null)for(String part:uri.getRawQuery().split("&")){if(part.startsWith("v=")){id=part.substring(2);break;}}
                else {String[] parts=path.split("/");if(parts.length==3&&(parts[1].equals("shorts")||parts[1].equals("embed")||parts[1].equals("live")))id=parts[2];}
            }
            return id.matches("[A-Za-z0-9_-]{11}")?id:"";
        }catch(Exception e){return "";}
    }
    public static String thumbnail(String id){if(!id.matches("[A-Za-z0-9_-]{11}"))throw new IllegalArgumentException("Invalid video id");return "https://i.ytimg.com/vi/"+id+"/hqdefault.jpg";}
}
