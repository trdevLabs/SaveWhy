package com.savewhy.app;

import org.junit.Test;
import java.util.*;
import static org.junit.Assert.*;

public class QuickCaptureTest {
    private Card save(String why) { return QuickCapture.create("https://youtu.be/abc123","",why,"","","","","",null); }
    @Test public void allSevenIntentsSaveWithoutTypingTitleOrNote() {
        for(String why:Card.INTENTS) {
            Card c=save(why); assertFalse(c.title.isEmpty()); assertFalse(c.note.isEmpty()); assertTrue(c.steps.isEmpty()); assertEquals(why,c.why);
        }
    }
    @Test public void watchNeedsOnlySharedLinkAndIntent() {
        Card c=save("İzle"); assertEquals("Sonra izleyeceğim",c.note); assertEquals("YouTube · abc123",c.title);
    }
    @Test public void selectedQuickReasonIsStored() {
        Card c=QuickCapture.create("https://example.org/a","","Al","Hediye fikri","","","","",null);
        assertEquals("Hediye fikri",c.note);
    }
    @Test public void optionalCustomNoteAndTitleOverrideDefaults() {
        Card c=QuickCapture.create("https://youtu.be/a","Paylaşılan başlık","İzle","Akşam için","Kendi başlığım","Ailemle izleyeceğim","","",null);
        assertEquals("Kendi başlığım",c.title); assertEquals("Ailemle izleyeceğim",c.note);
    }
    @Test public void sharedTitleBecomesTitleWithoutExtraTyping() {
        assertEquals("Bir belgesel",QuickCapture.create("https://youtu.be/a","Bir belgesel","İzle","","","","","",null).title);
    }
    @Test public void bareUrlIsNotUsedAsSharedTitle() {
        assertEquals("YouTube · abc",QuickCapture.title("https://youtu.be/abc","https://youtu.be/abc"));
    }
    @Test public void longSharedTitleIsSafelyShortened() {
        String longTitle=String.join("",Collections.nCopies(200,"a")); assertEquals(160,QuickCapture.title("https://example.org/",longTitle).length());
    }
    @Test public void quickEditKeepsIdentityCreationAndStepStatus() {
        Card old=new Card("saved","https://youtu.be/abc","Eski","İzle","Akşam için",7,true,Collections.singletonList(new Card.Step("İzle",true)));
        Card edited=QuickCapture.create(old.url,"","İzle","Sonra izleyeceğim",old.title,"","İzle","",old);
        assertEquals("saved",edited.id); assertEquals(7,edited.createdAt); assertTrue(edited.done); assertTrue(edited.steps.get(0).done);
    }
    @Test public void missingLinkAndIntentAreStillRejected() {
        assertThrows(IllegalArgumentException.class,()->QuickCapture.create("","","İzle","","","","","",null));
        assertThrows(IllegalArgumentException.class,()->save(""));
    }
    @Test public void quickSaveRoundTripsThroughPortableBackup() throws Exception {
        Card c=save("Bul"); Card restored=CardCodec.decode(CardCodec.encode(Collections.singletonList(c))).get(0);
        assertEquals(c.title,restored.title); assertEquals(c.note,restored.note); assertEquals("Bul",restored.why);
    }
    @Test public void everyIntentHasFourUserFacingQuickChoices() {
        for(String why:Card.INTENTS) assertEquals(4,new HashSet<>(QuickCapture.reasons(why)).size());
    }
    @Test public void watchQueryUsesVideoIdentityNotGenericWatchPath() {
        assertEquals("YouTube · xyz123",QuickCapture.title("https://www.youtube.com/watch?v=xyz123&t=10",""));
    }
    @Test public void captionInSharedTextSuppliesTitleWithoutTyping() {
        assertEquals("Akşam için belgesel",QuickCapture.sharedTitle("Akşam için belgesel https://youtu.be/abc",null));
        assertEquals("",QuickCapture.sharedTitle("https://youtu.be/abc",null));
        assertEquals("Paylaşılan başlık",QuickCapture.sharedTitle("Başka açıklama https://youtu.be/abc","Paylaşılan başlık"));
    }
}
