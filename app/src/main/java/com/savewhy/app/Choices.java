package com.savewhy.app;

import android.content.Context;
import android.content.SharedPreferences;
import org.json.*;
import java.util.*;

/** Stable keys keep hidden and renamed options attached to existing cards. */
public final class Choices {
    public static final class Option {
        public final String key, label; public final boolean visible;
        Option(String key,String label,boolean visible) { this.key=key;this.label=label;this.visible=visible; }
    }
    private final SharedPreferences prefs;
    private JSONObject state;
    public Choices(Context context) {
        prefs=context.getSharedPreferences("choices",0);
        try { state=new JSONObject(prefs.getString("data","{}")); }
        catch(JSONException e) { throw new IllegalStateException("Seçenekler okunamadı. Mevcut ayarlar korunuyor.",e); }
    }
    public static String customKey(String label) { return "custom:"+UUID.randomUUID()+":"+validLabel(label); }
    public static boolean custom(String key) { return key!=null && key.matches("custom:[0-9a-fA-F]{8}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{12}:.{1,60}") && key.indexOf('\n')<0; }
    public static String fallback(String key) { return custom(key)?key.substring(44):key; }
    private static String validLabel(String label) {
        if(label==null || label.trim().isEmpty() || label.trim().length()>60 || label.matches("(?s).*\\p{Cntrl}.*"))throw new IllegalArgumentException("Seçenek adı 1–60 karakter olmalı.");
        return label.trim();
    }
    public JSONObject snapshot() {
        try {return new JSONObject(state.toString());}catch(JSONException e){throw new IllegalStateException(e);}
    }
    public static JSONObject validateSnapshot(JSONObject data) throws JSONException {
        if(data.length()>500)throw new IllegalArgumentException("En fazla 500 kişisel seçenek eklenebilir.");
        for(Iterator<String> it=data.keys();it.hasNext();) {
            String key=it.next();JSONObject o=data.getJSONObject(key);validLabel(o.getString("label"));o.getBoolean("visible");String kind=o.getString("kind");
            if("intent".equals(kind))IntentCatalog.get(key);
            else if("reason".equals(kind)) {
                String why=o.getString("why");IntentCatalog.get(why);
                if(!custom(key) && (!key.startsWith(reasonKey(why,"")) || !QuickCapture.reasons(why).contains(key.substring(reasonKey(why,"").length()))))throw new IllegalArgumentException("Geçersiz kişisel seçenek.");
            } else throw new IllegalArgumentException("Geçersiz kişisel seçenek.");
        }
        return new JSONObject(data.toString());
    }
    public JSONObject previewMerge(JSONObject imported) throws JSONException {
        JSONObject next=snapshot();JSONObject valid=validateSnapshot(imported);
        for(Iterator<String> it=valid.keys();it.hasNext();) {String key=it.next();if(!next.has(key))next.put(key,valid.getJSONObject(key));}
        return validateSnapshot(next);
    }
    public void mergeSnapshot(JSONObject imported) throws JSONException {
        JSONObject next=previewMerge(imported);if(!prefs.edit().putString("data",next.toString()).commit())throw new IllegalStateException("Seçenekler kaydedilemedi.");state=next;
    }
    private Option option(String key) {
        JSONObject data=state.optJSONObject(key);return new Option(key,data==null?fallback(key):data.optString("label",fallback(key)),data==null||data.optBoolean("visible",true));
    }
    public String label(String key) { return option(key).label; }
    public List<Option> intents(boolean visibleOnly) {
        List<Option> result=new ArrayList<>();for(String key:IntentCatalog.labels())result.add(option(key));
        for(Iterator<String> it=state.keys();it.hasNext();) { String key=it.next();JSONObject o=state.optJSONObject(key);if(custom(key)&&o!=null&&"intent".equals(o.optString("kind")))result.add(option(key)); }
        if(visibleOnly)result.removeIf(o->!o.visible);return result;
    }
    public List<Option> reasons(String why,boolean visibleOnly) {
        List<Option> result=new ArrayList<>();for(String key:QuickCapture.reasons(why))result.add(option(reasonKey(why,key)));
        // Built-in reasons use their plain display text as logical keys, scoped by intent.
        List<Option> mapped=new ArrayList<>();for(Option o:result) { String key=o.key.substring(("reason:"+why+"|").length());JSONObject data=state.optJSONObject(o.key);mapped.add(new Option(key,data==null?key:data.optString("label",key),o.visible)); }
        for(Iterator<String> it=state.keys();it.hasNext();) { String key=it.next();JSONObject o=state.optJSONObject(key);if(custom(key)&&o!=null&&"reason".equals(o.optString("kind"))&&why.equals(o.optString("why")))mapped.add(option(key)); }
        if(visibleOnly)mapped.removeIf(o->!o.visible);return mapped;
    }
    private static String reasonKey(String why,String key) { return "reason:"+why+"|"+key; }
    public void setIntent(String key,String label,boolean visible) { set(key,label,visible,"intent",""); }
    public void setReason(String why,String key,String label,boolean visible) { set(custom(key)?key:reasonKey(why,key),label,visible,"reason",why); }
    private void set(String key,String label,boolean visible,String kind,String why) {
        String valid=validLabel(label);
        try {
            JSONObject next=new JSONObject(state.toString());next.put(key,new JSONObject().put("label",valid).put("visible",visible).put("kind",kind).put("why",why));
            if(next.length()>500)throw new IllegalArgumentException("En fazla 500 kişisel seçenek eklenebilir.");
            if(!prefs.edit().putString("data",next.toString()).commit())throw new IllegalStateException("Seçenekler kaydedilemedi.");state=next;
        } catch(JSONException e) { throw new IllegalStateException(e); }
    }
}
