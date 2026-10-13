package com.savewhy.app;

import java.net.URI;
import java.util.*;

/** Save without typing: shared URL + intent are sufficient. Optional edits stay optional. */
public final class QuickCapture {
    private QuickCapture() {}
    public static String sharedTitle(String text,String explicitTitle) {
        String value=explicitTitle==null||explicitTitle.trim().isEmpty()?text:explicitTitle;
        if(value==null) return "";
        value=value.replaceAll("(?i)https?://[^\\s<>]+","").replaceAll("\\s+"," ").trim();
        return value.substring(0,Math.min(160,value.length()));
    }
    public static List<String> reasons(String why) {
        switch(why) {
            case "Yap": return Arrays.asList("Sonra deneyeceğim","Hafta sonu yapacağım","İşime yarar","Birlikte yapacağız");
            case "Git": return Arrays.asList("Gitmek istiyorum","Hafta sonu için","Arkadaşımla gideceğim","Gezi planıma ekleyeceğim");
            case "Al": return Arrays.asList("Almayı düşünüyorum","Fiyat karşılaştıracağım","Hediye fikri","İhtiyacım var");
            case "Öğren": return Arrays.asList("Öğrenmek istiyorum","İşimde kullanacağım","Pratik yapacağım","Referans olarak saklıyorum");
            case "Üret": return Arrays.asList("İlham aldım","Formatı uyarlayacağım","Yeni içerik fikri","Anlatımı hoşuma gitti");
            case "İzle": return Arrays.asList("Sonra izleyeceğim","Akşam için","Boş zamanımda","Birlikte izleyeceğiz");
            case "Bul": return Arrays.asList("Ne olduğunu bulacağım","Benzerini arayacağım","Kaynağını bulacağım","Nerede olduğunu bulacağım");
            default: throw new IllegalArgumentException("Geçersiz niyet.");
        }
    }
    public static String title(String url,String sharedTitle) {
        String link=UrlTools.normalize(url);
        String given=sharedTitle==null?"":sharedTitle.trim().replaceAll("[\\r\\n]+"," ");
        if(!given.isEmpty() && UrlTools.extract(given).isEmpty()) return given.substring(0,Math.min(160,given.length()));
        URI uri=URI.create(link); String token="";
        if(uri.getRawQuery()!=null) for(String param:uri.getRawQuery().split("&")) {
            if(param.startsWith("v=")) { token=param.substring(2); break; }
        }
        if(token.isEmpty() && uri.getPath()!=null) {
            String[] parts=uri.getPath().split("/");
            if(parts.length>0) token=parts[parts.length-1];
        }
        token=token.replace('-', ' ').replace('_',' ');
        String result=UrlTools.source(link)+(token.isEmpty()?"":" · "+token.substring(0,Math.min(36,token.length())));
        return result.substring(0,Math.min(160,result.length()));
    }
    public static Card create(String rawUrl,String sharedTitle,String why,String quickReason,String customTitle,
                              String customNote,String lines,String searchQuery,Card previous) {
        String url=UrlTools.extract(rawUrl);
        if(url.isEmpty()) throw new IllegalArgumentException("Bir bağlantı paylaş veya yapıştır.");
        IntentCatalog.get(why);
        String title=customTitle.trim().isEmpty()?title(url,sharedTitle):customTitle.trim();
        String note=!customNote.trim().isEmpty()?customNote.trim():quickReason.trim();
        if(note.isEmpty()) note=reasons(why).get(0);
        return new Card(previous==null?UUID.randomUUID().toString():previous.id,url,title,why,note,
                previous==null?System.currentTimeMillis():previous.createdAt,previous!=null&&previous.done,
                Card.parseSteps(lines,previous==null?Collections.emptyList():previous.steps),searchQuery);
    }
}
