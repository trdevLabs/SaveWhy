package com.savewhy.app;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;
import java.net.InetAddress;
import static org.junit.Assert.*;
@RunWith(RobolectricTestRunner.class) @Config(sdk=28)
public class SocialPreviewTest {
    @Test public void extractsRealSocialImagesRegardlessOfAttributeOrder(){
        assertEquals("https://scontent.cdninstagram.com/photo.jpg?a=1&b=2",SocialPreview.image("<META content='https://scontent.cdninstagram.com/photo.jpg?a=1&amp;b=2' property='og:image'>","https://www.instagram.com/reel/123/"));
        assertEquals("https://scontent.fbcdn.net/photo.jpg",SocialPreview.image("<meta property=og:image content=https://scontent.fbcdn.net/photo.jpg>","https://www.facebook.com/watch/?v=123"));
        assertEquals("https://p16.muscdn.com/photo.jpg",SocialPreview.image("<meta name='twitter:image' content='https://p16.muscdn.com/photo.jpg'>","https://www.tiktok.com/@demo/video/123"));
    }
    @Test public void prefersOpenGraphAndResolvesRelativeImage(){assertEquals("https://www.instagram.com/thumb.jpg",SocialPreview.image("<meta name='twitter:image' content='https://scontent.fbcdn.net/twitter.jpg'><meta property='og:image' content='/thumb.jpg'>","https://www.instagram.com/reel/123/"));}
    @Test public void blocksPrivateAndLookalikeUrls()throws Exception{
        for(String url:new String[]{"http://www.instagram.com/photo.jpg","https://instagram.com.evil.test/image","https://evilinstagram.com/image","https://instagram.com@127.0.0.1/image","https://www.instagram.com:8080/image","file:///etc/passwd","https://127.0.0.1/photo"})assertFalse(url,SocialPreview.allowed(url,true));
        for(String ip:new String[]{"127.0.0.1","10.0.0.1","192.168.1.1","169.254.169.254","100.64.1.1","198.18.0.1","::1","fc00::1"})assertFalse(ip,SocialPreview.publicAddress(InetAddress.getByName(ip)));
        assertTrue(SocialPreview.publicAddress(InetAddress.getByName("8.8.8.8")));assertEquals("",SocialPreview.image("<meta property='og:image' content='https://evil.test/pixel'>","https://instagram.com/reel/123"));
    }
    @Test public void keysDoNotContainUrlPathOrPrivateQuery(){assertEquals("abcDEF123_-",SocialPreview.key("https://youtu.be/abcDEF123_-"));String key=SocialPreview.key("https://instagram.com/reel/123?secret=token");assertTrue(key.matches("[a-f0-9]{64}"));assertNotEquals(key,SocialPreview.key("https://instagram.com/reel/456"));}
}
