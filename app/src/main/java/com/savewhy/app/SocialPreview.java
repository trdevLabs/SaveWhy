package com.savewhy.app;

import java.net.*;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.*;
import java.util.regex.*;
import android.text.Html;

/** Only public HTTPS social pages and their image CDNs can be requested. */
final class SocialPreview {
    private static final String[] PAGES={"youtube.com","youtu.be","instagram.com","facebook.com","fb.com","fb.watch","tiktok.com","threads.net","threads.com","x.com","twitter.com","pinterest.com","pin.it"};
    private static final String[] IMAGES={"ytimg.com","fbcdn.net","cdninstagram.com","tiktokcdn.com","tiktokcdn-us.com","tiktokcdn-eu.com","tiktokcdn-eu.net","byteimg.com","ibyteimg.com","ibytedtos.com","muscdn.com","twimg.com","pinimg.com"};
    static boolean domain(String host,String suffix){return host!=null&&(host.equals(suffix)||host.endsWith("."+suffix));}
    static boolean allowed(String url,boolean image){
        try{URI u=URI.create(url);String host=u.getHost();if(host==null||!"https".equalsIgnoreCase(u.getScheme())||u.getRawUserInfo()!=null||(u.getPort()!=-1&&u.getPort()!=443))return false;host=host.toLowerCase(Locale.ROOT);for(String suffix:PAGES)if(domain(host,suffix))return true;if(image)for(String suffix:IMAGES)if(domain(host,suffix))return true;}catch(Exception ignored){}return false;
    }
    static String key(String url){String id=VideoLinks.youtubeId(url);if(!id.isEmpty())return id;try{byte[] hash=MessageDigest.getInstance("SHA-256").digest(url.getBytes(StandardCharsets.UTF_8));StringBuilder key=new StringBuilder();for(byte b:hash)key.append(String.format(Locale.ROOT,"%02x",b&255));return key.toString();}catch(Exception e){throw new IllegalStateException(e);}}
    static String image(String html,String base){
        Matcher tags=Pattern.compile("<meta\\b([^>]{0,8192})>",Pattern.CASE_INSENSITIVE).matcher(html);
        String fallback="";while(tags.find()){Map<String,String> attrs=new HashMap<>();Matcher a=Pattern.compile("([\\w:-]+)\\s*=\\s*(?:\"([^\"]*)\"|'([^']*)'|([^\\s>]+))").matcher(tags.group(1));while(a.find())attrs.put(a.group(1).toLowerCase(Locale.ROOT),a.group(2)!=null?a.group(2):a.group(3)!=null?a.group(3):a.group(4));String property=attrs.getOrDefault("property",attrs.getOrDefault("name","")).toLowerCase(Locale.ROOT);if(!property.equals("og:image")&&!property.equals("og:image:secure_url")&&!property.equals("twitter:image")&&!property.equals("twitter:image:src"))continue;
            try{String value=Html.fromHtml(attrs.getOrDefault("content",""),Html.FROM_HTML_MODE_LEGACY).toString();String url=URI.create(base).resolve(value).toString();if(!allowed(url,true))continue;if(property.startsWith("og:"))return url;fallback=url;}catch(Exception ignored){}
        }return fallback;
    }
    static boolean publicAddress(InetAddress address){byte[] b=address.getAddress();if(address.isAnyLocalAddress()||address.isLoopbackAddress()||address.isLinkLocalAddress()||address.isSiteLocalAddress()||address.isMulticastAddress())return false;if(b.length==4){int a=b[0]&255,c=b[1]&255;return a!=0&&a!=127&&a<224&&!(a==100&&c>=64&&c<=127)&&!(a==198&&(c==18||c==19));}return b.length==16&&(b[0]&0xe0)==0x20;}
    private SocialPreview(){}
}
