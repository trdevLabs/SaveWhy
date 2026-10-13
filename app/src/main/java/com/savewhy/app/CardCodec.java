package com.savewhy.app;

import org.json.*;
import java.util.*;

/** Versioned portable backup. Decode fully before touching live storage. */
public final class CardCodec {
    public static final int MAX_CARDS = 5000;
    public static final int MAX_BYTES = 4 * 1024 * 1024;
    private CardCodec() {}

    public static String encode(List<Card> cards) throws JSONException {
        JSONArray array = new JSONArray();
        for (Card c : cards) {
            JSONArray steps = new JSONArray();
            for (Card.Step s : c.steps) steps.put(new JSONObject().put("text", s.text).put("done", s.done));
            array.put(new JSONObject().put("id", c.id).put("url", c.url).put("title", c.title)
                    .put("why", c.why).put("note", c.note).put("createdAt", c.createdAt).put("done", c.done).put("steps", steps).put("searchQuery",c.searchQuery).put("reminderAt",c.reminderAt));
        }
        return new JSONObject().put("app", "SaveWhy").put("version", 3).put("cards", array).toString(2);
    }

    public static List<Card> decode(String json) throws JSONException {
        if (json.getBytes(java.nio.charset.StandardCharsets.UTF_8).length > MAX_BYTES)
            throw new IllegalArgumentException("Dosya en fazla 4 MB olabilir.");
        JSONTokener tokener = new JSONTokener(json);
        Object root = tokener.nextValue();
        if (tokener.nextClean() != 0) throw new IllegalArgumentException("Dosyada beklenmeyen ek içerik var.");
        // v0.1 stored a bare array. Existing installations migrate without losing cards.
        boolean legacy = root instanceof JSONArray;
        JSONArray array; int version=0;
        if (legacy) array = (JSONArray) root;
        else if (root instanceof JSONObject) {
            JSONObject object = (JSONObject) root;
            version=object.getInt("version");
            if (!"SaveWhy".equals(object.getString("app")) || (version != 1 && version != 2 && version != 3))
                throw new IllegalArgumentException("Desteklenmeyen yedek biçimi.");
            array = object.getJSONArray("cards");
        } else throw new IllegalArgumentException("Geçersiz yedek dosyası.");
        if (array.length() > MAX_CARDS) throw new IllegalArgumentException("En fazla 5000 kayıt içe aktarılabilir.");
        List<Card> result = new ArrayList<>(); Set<String> ids = new HashSet<>(), keys = new HashSet<>();
        for (int i = 0; i < array.length(); i++) {
            JSONObject c = array.getJSONObject(i); List<Card.Step> steps = new ArrayList<>();
            JSONArray a = legacy ? c.optJSONArray("steps") : c.getJSONArray("steps");
            if (a != null) for (int j = 0; j < a.length(); j++) {
                JSONObject step = a.getJSONObject(j); steps.add(new Card.Step(step.getString("text"), step.getBoolean("done")));
            }
            Card card = new Card(c.getString("id"), c.getString("url"), c.getString("title"), c.getString("why"),
                    c.getString("note"), legacy ? c.optLong("createdAt", 0) : c.getLong("createdAt"), c.getBoolean("done"), steps,
                    version>=2?c.getString("searchQuery"):"");
            card.reminderAt=version>=3?c.getLong("reminderAt"):0;
            if(card.reminderAt<0) throw new IllegalArgumentException("Geçersiz hatırlatma tarihi.");
            if (!ids.add(card.id)) throw new IllegalArgumentException("Dosyada tekrarlanan kayıt kimliği var.");
            // Tracking cleanup can collapse two old URLs; retain the first existing card.
            if (keys.add(card.duplicateKey())) result.add(card);
        }
        return result;
    }

    public static List<Card> merge(List<Card> current, List<Card> imported) {
        List<Card> result = new ArrayList<>(current); Set<String> keys = new HashSet<>(), ids = new HashSet<>();
        for (Card c : current) { keys.add(c.duplicateKey()); ids.add(c.id); }
        for (Card c : imported) if (keys.add(c.duplicateKey())) {
            String id = c.id;
            if (ids.contains(id)) id = UUID.randomUUID().toString();
            ids.add(id); Card added=new Card(id,c.url,c.title,c.why,c.note,c.createdAt,c.done,c.steps,c.searchQuery); added.reminderAt=c.reminderAt; result.add(added);
        }
        if (result.size() > MAX_CARDS) throw new IllegalArgumentException("Toplam kayıt sayısı 5000'i aşamaz.");
        return result;
    }
}
