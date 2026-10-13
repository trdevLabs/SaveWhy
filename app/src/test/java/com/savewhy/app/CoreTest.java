package com.savewhy.app;

import org.junit.Test;
import org.json.*;
import java.util.*;
import static org.junit.Assert.*;

public class CoreTest {
    private Card card(String id,String url,String why) {
        return new Card(id,url,"İçerik üretimi","Üret".equals(why)?"Üret":why,"Hafta sonu çalışacağım",1234,false,
                Arrays.asList(new Card.Step("Örneği dene",true),new Card.Step("Sonucu yaz",false)));
    }
    @Test public void sharedTextExtractsFirstValidLink() {
        assertEquals("https://youtu.be/abc",UrlTools.extract("Bunu izle (https://youtu.be/abc?si=tracking)."));
    }
    @Test public void meaningfulParametersSurviveTrackingCleanup() {
        assertEquals("https://www.youtube.com/watch?v=abc&t=12",UrlTools.normalize("HTTPS://WWW.YOUTUBE.COM:443/watch?v=abc&utm_source=share&si=xyz&t=12"));
    }
    @Test public void encodedQueryIsNotDoubleEncoded() {
        assertEquals("https://example.org/?q=a%20b",UrlTools.normalize("https://example.org/?q=a%20b"));
    }
    @Test public void unrelatedSitesKeepParametersWithSimilarNames() {
        assertEquals("https://example.org/?si=lesson&igsh=content",UrlTools.normalize("https://example.org/?si=lesson&igsh=content"));
    }
    @Test public void trailingGarbageIsRejected() {
        assertThrows(IllegalArgumentException.class,()->CardCodec.decode("[] unexpected"));
    }
    @Test public void balancedPathParenthesesSurvive() {
        assertEquals("https://example.org/wiki/Test_(thing)",UrlTools.extract("https://example.org/wiki/Test_(thing)."));
    }
    @Test public void fragmentsSurvive() {
        assertEquals("https://example.org/#lesson-2",UrlTools.normalize("https://example.org/#lesson-2"));
    }
    @Test public void urlsWithCredentialsAreRejected() {
        assertThrows(IllegalArgumentException.class,()->UrlTools.normalize("https://user:pass@example.org/"));
    }
    @Test public void invalidPortAndScriptAreRejected() {
        assertThrows(IllegalArgumentException.class,()->UrlTools.normalize("https://example.org:99999/"));
        assertThrows(IllegalArgumentException.class,()->UrlTools.normalize("javascript:alert(1)"));
    }
    @Test public void lookalikeHostDoesNotBecomeInstagram() {
        assertEquals("instagram.com.evil.org",UrlTools.source("https://instagram.com.evil.org/a"));
        assertEquals("TikTok",UrlTools.source("https://vm.tiktok.com/abc"));
    }
    @Test public void searchFoldsTurkishCharactersAndIncludesSteps() {
        Card c=card("a","https://example.org/a","Üret");
        assertTrue(c.matches("icerik")); assertTrue(c.matches("CALISACAGIM")); assertTrue(c.matches("ornegi")); assertFalse(c.matches("olmayan"));
    }
    @Test public void changedStepsRetainMatchingCompletionInOrder() {
        List<Card.Step> steps=Card.parseSteps("Sonucu yaz\nÖrneği dene\nYeni adım",card("a","https://example.org/","Yap").steps);
        assertFalse(steps.get(0).done); assertTrue(steps.get(1).done); assertFalse(steps.get(2).done);
    }
    @Test public void repeatedStepsDoNotDuplicateCompletion() {
        List<Card.Step> steps=Card.parseSteps("Dene\nDene",Collections.singletonList(new Card.Step("Dene",true)));
        assertTrue(steps.get(0).done); assertFalse(steps.get(1).done);
    }
    @Test public void moreThanThirtyStepsRejected() {
        assertThrows(IllegalArgumentException.class,()->Card.parseSteps(String.join("\n",Collections.nCopies(31,"Adım")),Collections.emptyList()));
    }
    @Test public void copiedCardsDoNotMutateLiveSteps() {
        Card c=card("a","https://example.org/","Yap"); Card copy=c.copy(); copy.steps.get(0).done=false;
        assertTrue(c.steps.get(0).done);
    }
    @Test public void backupRoundTripsAllFields() throws Exception {
        Card c=card("a","https://example.org/a","Üret"); c.done=true;
        Card restored=CardCodec.decode(CardCodec.encode(Collections.singletonList(c))).get(0);
        assertEquals(c.id,restored.id); assertEquals(c.note,restored.note); assertEquals(1234,restored.createdAt);
        assertTrue(restored.done); assertEquals(2,restored.steps.size()); assertTrue(restored.steps.get(0).done);
    }
    @Test public void oldArrayMigratesWithoutInventingContent() throws Exception {
        String json="[{\"id\":\"old\",\"url\":\"https://youtu.be/a\",\"title\":\"Eski\",\"why\":\"Öğren\",\"note\":\"Dene\",\"done\":true}]";
        Card c=CardCodec.decode(json).get(0); assertTrue(c.done); assertEquals(0,c.createdAt); assertTrue(c.steps.isEmpty());
    }
    @Test public void futureVersionIsRejected() {
        assertThrows(IllegalArgumentException.class,()->CardCodec.decode("{\"app\":\"SaveWhy\",\"version\":99,\"cards\":[]}"));
    }
    @Test public void invalidImportCannotReturnPartialCards() throws Exception {
        JSONObject backup=new JSONObject(CardCodec.encode(Collections.singletonList(card("a","https://example.org/","Yap"))));
        backup.getJSONArray("cards").put(new JSONObject().put("id","bad"));
        assertThrows(JSONException.class,()->CardCodec.decode(backup.toString()));
    }
    @Test public void mergePreservesExistingCompletionAndSkipsTrackingDuplicate() {
        Card current=card("a","https://youtu.be/abc","Yap"); current.done=true;
        Card incoming=card("b","https://youtu.be/abc?si=tracking","Yap");
        List<Card> result=CardCodec.merge(Collections.singletonList(current),Collections.singletonList(incoming));
        assertEquals(1,result.size()); assertTrue(result.get(0).done);
    }
    @Test public void sameLinkWithDifferentIntentIsKept() {
        List<Card> result=CardCodec.merge(Collections.singletonList(card("a","https://youtu.be/abc","Yap")),
                Collections.singletonList(card("b","https://youtu.be/abc","Üret"))); assertEquals(2,result.size());
    }
    @Test public void conflictingIdsAreAssignedNewIdentityOnMerge() {
        List<Card> result=CardCodec.merge(Collections.singletonList(card("a","https://example.org/a","Yap")),
                Collections.singletonList(card("a","https://example.org/b","Yap")));
        assertEquals(2,result.size()); assertNotEquals(result.get(0).id,result.get(1).id);
    }
    @Test public void importedDuplicateIdsAreRejected() throws Exception {
        String encoded=CardCodec.encode(Arrays.asList(card("a","https://example.org/a","Yap"),card("a","https://example.org/b","Yap")));
        assertThrows(IllegalArgumentException.class,()->CardCodec.decode(encoded));
    }
    @Test public void blankReasonAndUnknownIntentAreRejected() {
        assertThrows(IllegalArgumentException.class,()->new Card("a","https://example.org/","Başlık","Yap"," ",0,false,Collections.emptyList()));
        assertThrows(IllegalArgumentException.class,()->card("a","https://example.org/","Bilinmeyen"));
    }
    @Test public void everyIntentHasActionTemplate() {
        for(String why:Card.INTENTS) assertFalse(Card.parseSteps(Card.template(why),Collections.emptyList()).isEmpty());
    }
}
