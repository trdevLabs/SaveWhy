package com.savewhy.app;
import android.content.Context;
import java.util.*;
import org.junit.*;
import org.junit.runner.RunWith;
import org.robolectric.*;
import org.robolectric.annotation.Config;
import static org.junit.Assert.*;
@RunWith(RobolectricTestRunner.class)
@Config(sdk=28)
public class InboxTest {
    private Context context;private Card card;
    @Before public void setup(){context=RuntimeEnvironment.getApplication();card=new Card("inbox","https://example.com","Saved title","İzle","Private note",1,false,Collections.emptyList());}
    @Test public void deliveryIsDeduplicatedAndReadStatePersists()throws Exception {
        NotificationInbox.record(context,card,42);NotificationInbox.record(context,card,42);assertEquals(1,NotificationInbox.unread(context));
        NotificationInbox.markRead(context,false);NotificationInbox.record(context,card,42);assertEquals(0,NotificationInbox.unread(context));assertEquals("Saved title",NotificationInbox.entries(context).get(0).title);
        NotificationInbox.record(context,card,43);assertEquals(1,NotificationInbox.unread(context));
    }
    @Test public void clearingHistoryDoesNotDeleteCardOrRecreateOldEvent()throws Exception {
        CardStore store=new CardStore(context.getFilesDir());store.load();store.save(card);NotificationInbox.record(context,card,42);NotificationInbox.markRead(context,true);NotificationInbox.record(context,card,42);
        assertTrue(NotificationInbox.entries(context).isEmpty());assertNotNull(store.find(card.id));
    }
    @Test public void historyIsBoundedAndNewestFirst()throws Exception {
        for(int i=0;i<205;i++)NotificationInbox.record(context,card,i);assertEquals(200,NotificationInbox.entries(context).size());assertEquals(204,NotificationInbox.entries(context).get(0).time);
    }
    @Test public void corruptHistoryIsPreserved()throws Exception {
        context.getSharedPreferences("inbox",0).edit().putString("events","broken").commit();assertThrows(org.json.JSONException.class,()->NotificationInbox.record(context,card,1));assertEquals("broken",context.getSharedPreferences("inbox",0).getString("events",""));
    }
}
