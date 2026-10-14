package com.savewhy.app;
import android.content.Context;
import org.json.*;
import java.util.*;

/** Local reminder history, capped at 200 events. Clearing only hides history, not cards. */
public final class NotificationInbox {
    public static final class Entry {
        public final String key,cardId,title; public final long time;public final boolean read,hidden;
        Entry(JSONObject o)throws JSONException{key=o.getString("key");cardId=o.getString("cardId");title=o.getString("title");time=o.getLong("time");read=o.getBoolean("read");hidden=o.optBoolean("hidden",false);}
    }
    private static JSONArray load(Context c)throws JSONException{return new JSONArray(c.getSharedPreferences("inbox",0).getString("events","[]"));}
    private static void save(Context c,JSONArray events){if(!c.getSharedPreferences("inbox",0).edit().putString("events",events.toString()).commit())throw new IllegalStateException("Bildirimler kaydedilemedi.");}
    public static void record(Context c,Card card,long time)throws JSONException {
        JSONArray events=load(c);String key=card.id+":"+time;for(int i=0;i<events.length();i++)if(key.equals(events.getJSONObject(i).getString("key")))return;
        JSONArray next=new JSONArray();next.put(new JSONObject().put("key",key).put("cardId",card.id).put("title",card.title).put("time",time).put("read",false).put("hidden",false));
        for(int i=0;i<Math.min(199,events.length());i++)next.put(events.getJSONObject(i));save(c,next);
    }
    public static List<Entry> entries(Context c)throws JSONException {JSONArray events=load(c);List<Entry> result=new ArrayList<>();for(int i=0;i<events.length();i++){Entry e=new Entry(events.getJSONObject(i));if(!e.hidden)result.add(e);}return result;}
    public static int unread(Context c)throws JSONException {int n=0;for(Entry e:entries(c))if(!e.read)n++;return n;}
    public static void markRead(Context c,boolean clear)throws JSONException {JSONArray events=load(c);for(int i=0;i<events.length();i++){JSONObject o=events.getJSONObject(i);o.put("read",true);if(clear)o.put("hidden",true);}save(c,events);}
}
