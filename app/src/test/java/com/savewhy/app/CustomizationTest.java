package com.savewhy.app;
import android.content.Context;
import java.util.*;
import org.json.*;
import org.junit.*;
import org.junit.runner.RunWith;
import org.robolectric.*;
import org.robolectric.annotation.Config;
import static org.junit.Assert.*;

@RunWith(RobolectricTestRunner.class)
@Config(sdk=28)
public class CustomizationTest {
    private Context context;
    @Before public void setup(){context=RuntimeEnvironment.getApplication();}
    private Card card(){return new Card("saved","https://example.com/saved","My save","İzle","My private note",1000,false,Collections.emptyList());}
    @Test public void forgottenUsesLastActivityAndStrictThreshold(){
        Card c=card();long now=1000+15*86400000L;assertFalse(c.forgotten(now,15));assertTrue(c.forgotten(now+1,15));
        c.lastActivityAt=now;assertFalse(c.forgotten(now+1,15));c.lastActivityAt=1000;c.done=true;assertFalse(c.forgotten(now+1,15));
    }
    @Test public void forgottenMovesOutOfPendingButRemainsInAll(){
        Card c=card();long now=1000+16*86400000L;
        assertTrue(c.visible("Unutulanlar","",15,now));assertFalse(c.visible("Bekleyen","",15,now));assertTrue(c.visible("Tümü","",15,now));
        assertFalse(c.visible("Unutulanlar","different",15,now));assertFalse(c.visible("Unutulanlar","",30,now));
    }
    @Test public void saveTouchesButAutomatedReminderDoesNot()throws Exception {
        Card c=card();CardStore store=new CardStore(context.getFilesDir());store.load();store.save(c,false);assertEquals(1000,store.find(c.id).lastActivityAt);
        store.save(c);assertTrue(store.find(c.id).lastActivityAt>1000);assertEquals(1000,c.lastActivityAt);
    }
    @Test public void backupAndLegacyMigrationPreserveActivity()throws Exception {
        Card c=card();c.lastActivityAt=2000;assertEquals(2000,CardCodec.decode(CardCodec.encode(Collections.singletonList(c))).get(0).lastActivityAt);
        JSONObject old=new JSONObject(CardCodec.encode(Collections.singletonList(c)));old.put("version",3);old.getJSONArray("cards").getJSONObject(0).remove("lastActivityAt");
        assertEquals(1000,CardCodec.decode(old.toString()).get(0).lastActivityAt);
    }
    @Test public void renameAndHideKeepExistingKeys()throws Exception {
        Choices choices=new Choices(context);choices.setIntent("İzle","Dinle",false);
        assertFalse(choices.intents(true).stream().anyMatch(o->o.key.equals("İzle")));assertEquals("Dinle",new Choices(context).label("İzle"));
        assertEquals("İzle",CardCodec.decode(CardCodec.encode(Collections.singletonList(card()))).get(0).why);
    }
    @Test public void customIntentSurvivesBackupWithoutPreferences()throws Exception {
        String key=Choices.customKey("Arşivle");Card c=new Card("custom","https://example.com","Content",key,"My reason",1,false,Collections.emptyList());
        Card restored=CardCodec.decode(CardCodec.encode(Collections.singletonList(c))).get(0);assertEquals(key,restored.why);assertEquals("Arşivle",IntentCatalog.get(key).label);
        assertTrue(QuickCapture.reasons(key).isEmpty());
    }
    @Test public void reasonVisibilityIsScopedToItsIntent(){
        Choices choices=new Choices(context);String key=Choices.customKey("Ailemle paylaşacağım");choices.setReason("İzle",key,"Ailemle paylaşacağım",true);
        assertTrue(choices.reasons("İzle",true).stream().anyMatch(o->o.key.equals(key)));assertFalse(choices.reasons("Yap",true).stream().anyMatch(o->o.key.equals(key)));
        choices.setReason("İzle",key,"Ailemle paylaşacağım",false);assertFalse(choices.reasons("İzle",true).stream().anyMatch(o->o.key.equals(key)));
    }
    @Test public void emptyReasonListCanStillSaveCustomIntent(){
        String key=Choices.customKey("Arşivle");Card c=QuickCapture.create("https://example.com","",key,"","","","","",null);assertEquals("Arşivle",c.note);
    }
    @Test public void preferencesMergePreservesCurrentChoices()throws Exception {
        Choices choices=new Choices(context);choices.setIntent("İzle","Videolarım",false);JSONObject imported=new JSONObject();imported.put("İzle",new JSONObject().put("kind","intent").put("label","Watch").put("visible",true));
        String custom=Choices.customKey("Arşivle");imported.put(custom,new JSONObject().put("kind","intent").put("label","Arşivle").put("visible",true));choices.mergeSnapshot(imported);
        assertEquals("Videolarım",choices.label("İzle"));assertTrue(choices.intents(true).stream().anyMatch(o->o.key.equals(custom)));
    }
    @Test public void invalidPreferenceAndNegativeActivityAreRejected()throws Exception {
        assertThrows(IllegalArgumentException.class,()->Choices.customKey(""));assertFalse(Choices.custom("custom:not-a-uuid:test"));
        JSONObject bad=new JSONObject().put("unknown",new JSONObject().put("kind","intent").put("label","Bad").put("visible",true));assertThrows(IllegalArgumentException.class,()->Choices.validateSnapshot(bad));
        JSONObject backup=new JSONObject(CardCodec.encode(Collections.singletonList(card())));backup.getJSONArray("cards").getJSONObject(0).put("lastActivityAt",-1);assertThrows(IllegalArgumentException.class,()->CardCodec.decode(backup.toString()));
    }
}
