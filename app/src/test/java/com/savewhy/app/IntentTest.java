package com.savewhy.app;

import org.json.*;
import org.junit.Test;
import java.net.URI;
import java.net.URLDecoder;
import java.util.*;
import static org.junit.Assert.*;

public class IntentTest {
    private Card card(String why,String query) {
        return new Card("saved","https://youtu.be/example","Videodaki lamba",why,
            "Kişisel notum web'e gönderilmemeli",17,false,Collections.emptyList(),query);
    }
    @Test public void everyIntentSurvivesBackupWithItsCompletionState() throws Exception {
        for(String label:IntentCatalog.labels()) {
            Card original=card(label,""); original.done=true;
            Card restored=CardCodec.decode(CardCodec.encode(Collections.singletonList(original))).get(0);
            assertEquals(label,restored.why); assertTrue(restored.done);
        }
    }
    @Test public void watchCanBeSavedWithoutActionSteps() {
        Card c=card("İzle",""); assertTrue(c.steps.isEmpty()); assertEquals("İzledim",IntentCatalog.get(c.why).completedLabel);
    }
    @Test public void findSearchDoesNotIncludePrivateReasonOrSourceUrl() throws Exception {
        Card c=card("Bul","pirinç lamba"); String decoded=URLDecoder.decode(URI.create(c.webSearchUrl()).getRawQuery(),"UTF-8");
        assertEquals("q=pirinç lamba",decoded); assertFalse(decoded.contains(c.note)); assertFalse(decoded.contains(c.url));
    }
    @Test public void searchInputCannotInjectAnotherParameterOrFragment() throws Exception {
        String input="lamba & site:örnek.com #model?";
        URI uri=URI.create(card("Bul",input).webSearchUrl()); assertNull(uri.getFragment());
        assertEquals(1,uri.getRawQuery().split("&").length);
        assertEquals("q="+input,URLDecoder.decode(uri.getRawQuery(),"UTF-8"));
    }
    @Test public void emptySearchQueryUsesOnlyCardTitle() throws Exception {
        Card c=card("Bul",""); assertEquals(c.title,c.effectiveSearchQuery());
        assertEquals("q="+c.title,URLDecoder.decode(URI.create(c.webSearchUrl()).getRawQuery(),"UTF-8"));
    }
    @Test public void searchActionIsUnavailableForOtherIntents() {
        assertThrows(IllegalArgumentException.class,()->card("İzle","").webSearchUrl());
    }
    @Test public void copyAndBackupKeepResearchQuery() throws Exception {
        Card c=card("Bul","mantar masa lambası");
        assertEquals(c.searchQuery,c.copy().searchQuery);
        assertEquals(c.searchQuery,CardCodec.decode(CardCodec.encode(Collections.singletonList(c))).get(0).searchQuery);
    }
    @Test public void mergeKeepsResearchQueryWhenIdentityConflicts() {
        Card old=card("Yap",""); Card added=card("Bul","mantar lamba");
        List<Card> result=CardCodec.merge(Collections.singletonList(old),Collections.singletonList(added));
        assertEquals(2,result.size()); assertNotEquals(old.id,result.get(1).id); assertEquals(added.searchQuery,result.get(1).searchQuery);
    }
    @Test public void versionOneBackupMigratesAllOriginalFields() throws Exception {
        String v1="{\"app\":\"SaveWhy\",\"version\":1,\"cards\":[{\"id\":\"old\",\"url\":\"https://youtu.be/abc\",\"title\":\"Tarif\",\"why\":\"Yap\",\"note\":\"Pazar günü dene\",\"createdAt\":1234,\"done\":true,\"steps\":[{\"text\":\"Malzemeleri al\",\"done\":true}]}]}";
        Card c=CardCodec.decode(v1).get(0); assertEquals("",c.searchQuery); assertEquals(1234,c.createdAt);
        assertTrue(c.done); assertTrue(c.steps.get(0).done); assertEquals("Pazar günü dene",c.note);
    }
    @Test public void versionTwoRequiresResearchFieldToAvoidSilentDataLoss() throws Exception {
        JSONObject root=new JSONObject(CardCodec.encode(Collections.singletonList(card("Bul","lamba"))));
        root.getJSONArray("cards").getJSONObject(0).remove("searchQuery");
        assertThrows(JSONException.class,()->CardCodec.decode(root.toString()));
    }
    @Test public void currentBackupVersionIsFive() throws Exception {
        assertEquals(5,new JSONObject(CardCodec.encode(Collections.emptyList())).getInt("version"));
    }
    @Test public void oversizedResearchQueryIsRejected() {
        assertThrows(IllegalArgumentException.class,()->card("Bul",String.join("",Collections.nCopies(301,"a"))));
    }
    @Test public void localSearchFindsResearchTermsWithTurkishCharacters() {
        assertTrue(card("Bul","ışıklı pirinç gövde").matches("ISIKLI PIRINC"));
    }
    @Test public void allAndPendingFiltersHaveDifferentMeaning() {
        Card c=card("İzle",""); c.done=true;
        assertTrue(c.visible("Tümü","")); assertFalse(c.visible("Bekleyen","")); assertTrue(c.visible("Tamamlanan",""));
    }
    @Test public void intentFilterKeepsCompletedCardsSearchable() {
        Card c=card("Bul","mantar lamba"); c.done=true;
        assertTrue(c.visible("Bul","mantar")); assertFalse(c.visible("Al","mantar")); assertFalse(c.visible("Bul","olmayan"));
    }
    @Test public void intentAndStatusFiltersCoverAllSevenIntents() {
        assertEquals(7,IntentCatalog.all().size());
        for(String label:IntentCatalog.labels()) assertTrue(IntentCatalog.filters().contains(label));
        assertTrue(IntentCatalog.filters().containsAll(Arrays.asList("Bekleyen","Tümü","Tamamlanan")));
    }
}
