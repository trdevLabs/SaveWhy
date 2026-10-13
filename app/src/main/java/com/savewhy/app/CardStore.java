package com.savewhy.app;

import android.util.AtomicFile;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.*;

public final class CardStore {
    private final AtomicFile file;
    private List<Card> cards = new ArrayList<>();
    private boolean healthy = true;

    public CardStore(File directory) { file = new AtomicFile(new File(directory, "cards.json")); }
    public void load() throws Exception {
        try (InputStream in = file.openRead()) { cards = CardCodec.decode(read(in)); }
        catch (FileNotFoundException e) {
            if (file.getBaseFile().exists()) { healthy = false; throw e; }
            cards = new ArrayList<>();
        } catch (Exception e) { healthy = false; throw e; }
    }
    public boolean isHealthy() { return healthy; }
    public List<Card> all() { return new ArrayList<>(cards); }
    public Card find(String id) { for (Card c : cards) if (c.id.equals(id)) return c; return null; }

    public void save(Card card) throws Exception { save(card,true); }
    public void save(Card card,boolean touch) throws Exception {
        Card saved=card.copy(); if(touch)saved.lastActivityAt=System.currentTimeMillis();
        card=saved;
        List<Card> next = all(); boolean replaced = false;
        for (int i=0; i<next.size(); i++) {
            Card old = next.get(i);
            if (!old.id.equals(card.id) && old.duplicateKey().equals(card.duplicateKey()))
                throw new IllegalArgumentException("Bu bağlantı aynı niyetle zaten kayıtlı.");
            if (old.id.equals(card.id)) { next.set(i,card); replaced=true; }
        }
        if (!replaced) next.add(0,card);
        commit(next);
    }
    public void delete(String id) throws Exception {
        List<Card> next = all(); next.removeIf(c -> c.id.equals(id)); commit(next);
    }
    public int merge(List<Card> imported) throws Exception {
        List<Card> next = CardCodec.merge(cards,imported); int added = next.size()-cards.size(); commit(next); return added;
    }
    public String backup() throws Exception {
        if (!healthy) throw new IOException("Kayıt dosyası okunamadı. Yedek oluşturulamıyor.");
        return CardCodec.encode(cards);
    }
    private void commit(List<Card> next) throws Exception {
        if (!healthy) throw new IOException("Kayıt dosyası okunamadı; mevcut dosya korunuyor.");
        if (next.size()>CardCodec.MAX_CARDS) throw new IOException("En fazla 5000 kayıt saklanabilir.");
        byte[] bytes = CardCodec.encode(next).getBytes(StandardCharsets.UTF_8);
        if (bytes.length>CardCodec.MAX_BYTES) throw new IOException("Kayıtlar 4 MB sınırını aşıyor.");
        FileOutputStream out = null;
        try {
            out=file.startWrite(); out.write(bytes); file.finishWrite(out); cards=next;
        } catch (Exception e) { if (out!=null) file.failWrite(out); throw e; }
    }
    public static String read(InputStream in) throws IOException {
        ByteArrayOutputStream out = new ByteArrayOutputStream(); byte[] buffer = new byte[8192]; int n;
        while ((n=in.read(buffer))!=-1) {
            if (out.size()+n > CardCodec.MAX_BYTES) throw new IOException("Dosya en fazla 4 MB olabilir.");
            out.write(buffer,0,n);
        }
        return out.toString("UTF-8");
    }
}
