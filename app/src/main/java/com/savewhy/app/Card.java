package com.savewhy.app;

import java.text.Normalizer;
import java.util.*;

public final class Card {
    public static final String[] INTENTS = {"Yap", "Git", "Al", "Öğren", "Üret"};
    public static final int MAX_STEPS = 30;
    public final String id, url, title, why, note;
    public final long createdAt;
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
        this.id = required(id, 100, "Kimlik"); this.url = UrlTools.normalize(url);
        this.title = required(title, 160, "Başlık"); this.note = required(note, 2000, "Kaydetme nedeni");
        if (!Arrays.asList(INTENTS).contains(why)) throw new IllegalArgumentException("Geçersiz niyet.");
        this.why = why;
        if (createdAt < 0) throw new IllegalArgumentException("Geçersiz kayıt tarihi.");
        this.createdAt = createdAt; this.done = done;
        if (steps == null || steps.size() > MAX_STEPS) throw new IllegalArgumentException("En fazla 30 adım eklenebilir.");
        this.steps = new ArrayList<>();
        for (Step s : steps) this.steps.add(new Step(s.text, s.done));
    }
    private static String required(String text, int max, String name) {
        if (text == null || text.trim().isEmpty()) throw new IllegalArgumentException(name + " boş bırakılamaz.");
        if (text.trim().length() > max) throw new IllegalArgumentException(name + " en fazla " + max + " karakter olabilir.");
        return text.trim();
    }
    public Card copy() { return new Card(id, url, title, why, note, createdAt, done, steps); }
    public int completedSteps() { int n = 0; for (Step s : steps) if (s.done) n++; return n; }
    public String duplicateKey() { return url + "\n" + why; }
    public boolean matches(String query) {
        StringBuilder data = new StringBuilder(title).append(' ').append(note).append(' ').append(url).append(' ').append(why);
        for (Step s : steps) data.append(' ').append(s.text);
        return fold(data.toString()).contains(fold(query));
    }
    static String fold(String text) {
        return Normalizer.normalize(text.toLowerCase(Locale.forLanguageTag("tr")).replace('ı','i'), Normalizer.Form.NFD)
                .replaceAll("\\p{M}", "").trim();
    }
    public static String template(String why) {
        switch (why) {
            case "Yap": return "Gerekenleri listele\nEksikleri hazırla\nDene ve sonucu not et";
            case "Git": return "Adres ve çalışma saatlerini kontrol et\nGitmek için bir gün seç\nZiyaret sonrası not ekle";
            case "Al": return "İhtiyacını ve bütçeni yaz\nFiyat ve alternatifleri karşılaştır\nSatın alma kararını ver";
            case "Öğren": return "Öğrenmek istediğin noktayı yaz\nİçerikten not çıkar\nBir örnekle uygula";
            case "Üret": return "Açılış cümlesini ve formatı not et\nKendi açını ve hedef kitleni belirle\nAkış ve eylem çağrısını yaz\nKendi versiyonunu üret";
            default: throw new IllegalArgumentException("Geçersiz niyet.");
        }
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
