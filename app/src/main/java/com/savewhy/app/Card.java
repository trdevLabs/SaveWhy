package com.savewhy.app;

import java.text.Normalizer;
import java.util.*;

public final class Card {
    public static final String[] INTENTS = IntentCatalog.labels();
    public static final int MAX_STEPS = 30;
    public final String id, url, title, why, note, searchQuery, imageId;
    public final List<String> tags;
    public String recognizedText="", ocrState="none";private String videoId="";
    public final long createdAt;
    public long reminderAt, lastActivityAt;
    public boolean done;
    public final List<Step> steps;

    public static final class Step {
        public final String text;
        public boolean done;
        public Step(String text, boolean done) {
            this.text = required(text, 300, "Adım"); this.done = done;
        }
    }
    public Card(String id, String url, String title, String why, String note, long createdAt, boolean done, List<Step> steps) {
        this(id,url,title,why,note,createdAt,done,steps,"");
    }
    public Card(String id, String url, String title, String why, String note, long createdAt, boolean done, List<Step> steps, String searchQuery) {
        this(id,url,title,why,note,createdAt,done,steps,searchQuery,"",Collections.emptyList(),"","none");
    }
    public Card(String id,String url,String title,String why,String note,long createdAt,boolean done,List<Step> steps,String searchQuery,String imageId,List<String> tags,String recognizedText,String ocrState) {
        this.id = required(id, 100, "Kimlik");
        if(imageId==null||(!imageId.isEmpty()&&!imageId.matches("[a-f0-9]{64}")))throw new IllegalArgumentException("Geçersiz görsel kimliği.");
        this.imageId=imageId;this.url=imageId.isEmpty()?UrlTools.normalize(url):"";
        if(!imageId.isEmpty()&&url!=null&&!url.isEmpty())throw new IllegalArgumentException("Görsel kaydı bağlantı içeremez.");
        this.tags=Collections.unmodifiableList(parseTags(tags));
        if(recognizedText==null||recognizedText.length()>4000)throw new IllegalArgumentException("Geçersiz görsel metni.");
        if(!Arrays.asList("none","pending","ready","failed").contains(ocrState))throw new IllegalArgumentException("Geçersiz metin tanıma durumu.");
        this.recognizedText=recognizedText;this.ocrState=ocrState;
        this.title = required(title, 160, "Başlık"); this.note = required(note, 2000, "Kaydetme nedeni");
        IntentCatalog.get(why);
        this.why = why;
        if(searchQuery==null || searchQuery.trim().length()>300) throw new IllegalArgumentException("Arama sorgusu en fazla 300 karakter olabilir.");
        this.searchQuery=searchQuery.trim();
        if (createdAt < 0) throw new IllegalArgumentException("Geçersiz kayıt tarihi.");
        this.createdAt = createdAt; this.lastActivityAt=createdAt; this.done = done;
        if (steps == null || steps.size() > MAX_STEPS) throw new IllegalArgumentException("En fazla 30 adım eklenebilir.");
        this.steps = new ArrayList<>();
        for (Step s : steps) this.steps.add(new Step(s.text, s.done));
    }
    private static String required(String text, int max, String name) {
        if (text == null || text.trim().isEmpty()) throw new IllegalArgumentException(name + " boş bırakılamaz.");
        if (text.trim().length() > max) throw new IllegalArgumentException(name + " en fazla " + max + " karakter olabilir.");
        return text.trim();
    }
    public Card copy() { Card c=new Card(id, url, title, why, note, createdAt, done, steps, searchQuery,imageId,tags,recognizedText,ocrState); c.reminderAt=reminderAt; c.lastActivityAt=lastActivityAt;c.setVideo(videoId); return c; }
    public boolean isImage(){return !imageId.isEmpty()&&videoId.isEmpty();}
    public boolean isVideo(){return !videoId.isEmpty();}
    public boolean hasVisual(){return !imageId.isEmpty();}
    public String videoId(){return videoId;}
    public void setVideo(String value){if(value==null||(!value.isEmpty()&&(!value.matches("[a-f0-9]{64}")||imageId.isEmpty())))throw new IllegalArgumentException("Geçersiz video kimliği.");videoId=value;}
    public static List<String> parseTags(List<String> values){if(values==null)throw new IllegalArgumentException("Geçersiz etiketler.");List<String> tags=new ArrayList<>();Set<String> keys=new HashSet<>();for(String value:values){if(value==null)throw new IllegalArgumentException("Geçersiz etiketler.");value=value.trim();if(value.isEmpty())continue;if(value.length()>40||value.contains("\n")||value.contains(","))throw new IllegalArgumentException("Etiket en fazla 40 karakter olabilir.");if(keys.add(fold(value)))tags.add(value);}if(tags.size()>12)throw new IllegalArgumentException("En fazla 12 etiket eklenebilir.");return tags;}
    public static List<String> parseTags(String text){return parseTags(Arrays.asList(text.split(",",-1)));}
    public int completedSteps() { int n = 0; for (Step s : steps) if (s.done) n++; return n; }
    public String duplicateKey() { return (isVideo()?"video:"+videoId:isImage()?"image:"+imageId:url) + "\n" + why; }
    public boolean matches(String query) {
        StringBuilder data = new StringBuilder(title).append(' ').append(note).append(' ').append(url).append(' ').append(why).append(' ').append(searchQuery).append(' ').append(recognizedText);
        for(String tag:tags)data.append(' ').append(tag);
        for (Step s : steps) data.append(' ').append(s.text);
        return fold(data.toString()).contains(fold(query));
    }
    public boolean visible(String filter,String query) {
        if(!matches(query)) return false;
        if(filter.equals("Görseller")||filter.equals("Ekran görüntüleri"))return isImage();
        if(filter.equals("Videolar"))return isVideo();
        if(filter.equals("Sosyal"))return !hasVisual();
        if(filter.equals("Tümü")) return true;
        if(filter.equals("Bekleyen")) return !done;
        if(filter.equals("Tamamlanan")) return done;
        return why.equals(filter);
    }
    public boolean forgotten(long now,int days) { return !done && now>lastActivityAt && now-lastActivityAt>days*86400000L; }
    public boolean visible(String filter,String query,int days,long now) {
        if(filter.equals("Unutulanlar"))return matches(query)&&forgotten(now,days);
        if(filter.equals("Bekleyen"))return matches(query)&&!done&&!forgotten(now,days);
        return visible(filter,query);
    }
    public String effectiveSearchQuery() { return searchQuery.isEmpty()?title:searchQuery; }
    public String webSearchUrl() {
        if(!why.equals("Bul")) throw new IllegalArgumentException("Web araması Bul niyeti içindir.");
        try { return "https://www.google.com/search?q="+java.net.URLEncoder.encode(effectiveSearchQuery(),"UTF-8"); }
        catch(java.io.UnsupportedEncodingException e) { throw new IllegalStateException(e); }
    }
    static String fold(String text) {
        return Normalizer.normalize(text.toLowerCase(Locale.forLanguageTag("tr")).replace('ı','i'), Normalizer.Form.NFD)
                .replaceAll("\\p{M}", "").trim();
    }
    public static String template(String why) {
        return IntentCatalog.get(why).template;
    }
    public static List<Step> parseSteps(String lines, List<Step> oldSteps) {
        List<Step> result = new ArrayList<>();
        Map<String, Queue<Boolean>> previous = new HashMap<>();
        for (Step old : oldSteps) previous.computeIfAbsent(old.text, k -> new ArrayDeque<>()).add(old.done);
        for (String line : lines.split("\\r?\\n")) if (!line.trim().isEmpty()) {
            String value = line.trim(); Queue<Boolean> statuses = previous.get(value);
            result.add(new Step(value, statuses != null && !statuses.isEmpty() && statuses.remove()));
        }
        if (result.size() > MAX_STEPS) throw new IllegalArgumentException("En fazla 30 adım eklenebilir.");
        return result;
    }
}
