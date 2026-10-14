package com.savewhy.app;
import org.junit.Test;
import static org.junit.Assert.*;
public class VideoLinksTest {
    private static final String ID="abcDEF123_-";
    @Test public void watchShortsShareEmbedAndLiveResolveSameId(){
        assertEquals(ID,VideoLinks.youtubeId("https://youtube.com/watch?v="+ID+"&t=22"));assertEquals(ID,VideoLinks.youtubeId("https://youtu.be/"+ID+"?si=ignored"));
        assertEquals(ID,VideoLinks.youtubeId("https://www.youtube.com/shorts/"+ID));assertEquals(ID,VideoLinks.youtubeId("https://m.youtube.com/live/"+ID));assertEquals(ID,VideoLinks.youtubeId("https://youtube.com/embed/"+ID));
    }
    @Test public void malformedAndLookalikeUrlsNeverReachPlayer(){
        for(String url:new String[]{"https://youtube.com.evil.test/watch?v="+ID,"https://evil.test/?v="+ID,"https://youtube.com/watch?v=<script>","https://youtu.be/short","https://user@youtube.com/watch?v="+ID,"javascript:alert(1)"})assertEquals("",VideoLinks.youtubeId(url));
    }
    @Test public void generatedUrlsAreBoundToKnownHttpsHosts(){assertEquals("https://i.ytimg.com/vi/"+ID+"/hqdefault.jpg",VideoLinks.thumbnail(ID));assertTrue(VideoLinks.embed(ID).startsWith("https://www.youtube.com/embed/"+ID));assertThrows(IllegalArgumentException.class,()->VideoLinks.embed("bad/../id"));}
}
