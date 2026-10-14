package com.savewhy.app;
import android.app.*;
import android.content.*;
import android.os.*;
import java.util.*;
import org.junit.*;
import org.junit.runner.RunWith;
import org.robolectric.*;
import org.robolectric.annotation.Config;
import static org.junit.Assert.*;
import static org.robolectric.Shadows.*;

@RunWith(RobolectricTestRunner.class)
@Config(sdk=28)
public class ReminderTest {
    private Context context;
    @Before public void setup() { context=RuntimeEnvironment.getApplication(); }
    private Card card() { return new Card("reminder-card","https://example.com/real","My title","İzle","My private note",1,false,Collections.emptyList()); }
    @Test public void reminderRoundTripsAndCopyPreservesIt() throws Exception {
        Card card=card(); card.reminderAt=System.currentTimeMillis()+3600000;
        assertEquals(card.reminderAt,CardCodec.decode(CardCodec.encode(Collections.singletonList(card))).get(0).reminderAt);
        assertEquals(card.reminderAt,card.copy().reminderAt);
        Card changed=QuickCapture.create(card.url,"","İzle","Sonra izleyeceğim","New title","","","",card); assertEquals(card.reminderAt,changed.reminderAt);
    }
    @Test public void olderBackupDefaultsToNoReminder() throws Exception {
        org.json.JSONObject json=new org.json.JSONObject(CardCodec.encode(Collections.singletonList(card()))); json.put("version",2);
        json.getJSONArray("cards").getJSONObject(0).remove("reminderAt"); assertEquals(0,CardCodec.decode(json.toString()).get(0).reminderAt);
    }
    @Test public void negativeDateRejected() throws Exception {
        org.json.JSONObject json=new org.json.JSONObject(CardCodec.encode(Collections.singletonList(card())));
        json.getJSONArray("cards").getJSONObject(0).put("reminderAt",-1);
        assertThrows(IllegalArgumentException.class,()->CardCodec.decode(json.toString()));
    }
    @Test public void completedCardCancelsAlarm() {
        Card card=card();card.reminderAt=System.currentTimeMillis()+3600000; Reminders.sync(context,card);
        assertEquals(1,shadowOf(context.getSystemService(AlarmManager.class)).getScheduledAlarms().size());
        card.done=true;Reminders.sync(context,card);assertEquals(0,shadowOf(context.getSystemService(AlarmManager.class)).getScheduledAlarms().size());
    }
    @Test public void notificationOpensCardAndConsumesOneShot() throws Exception {
        Card card=card();card.reminderAt=System.currentTimeMillis()-1000; CardStore store=new CardStore(context.getFilesDir());store.load();store.save(card);
        Reminders.deliver(context,card.id,card.reminderAt);
        assertEquals(1,shadowOf(context.getSystemService(NotificationManager.class)).getAllNotifications().size());
        store.load();assertEquals(0,store.find(card.id).reminderAt);
        Reminders.deliver(context,card.id,card.reminderAt);assertEquals(1,shadowOf(context.getSystemService(NotificationManager.class)).getAllNotifications().size());
    }
    @Test public void staleAlarmDoesNotNotify() throws Exception {
        Card card=card();card.reminderAt=System.currentTimeMillis()+3600000; CardStore store=new CardStore(context.getFilesDir());store.load();store.save(card);
        Reminders.deliver(context,card.id,card.reminderAt-1000);
        assertEquals(0,shadowOf(context.getSystemService(NotificationManager.class)).getAllNotifications().size());
        store.load();assertEquals(card.reminderAt,store.find(card.id).reminderAt);
    }
    @Test public void reschedulingKeepsOneAlarmAndRemovalCancelsIt() {
        Card card=card();card.reminderAt=System.currentTimeMillis()+3600000;Reminders.sync(context,card);
        card.reminderAt+=3600000;Reminders.sync(context,card);assertEquals(1,shadowOf(context.getSystemService(AlarmManager.class)).getScheduledAlarms().size());
        Reminders.cancel(context,card.id);assertEquals(0,shadowOf(context.getSystemService(AlarmManager.class)).getScheduledAlarms().size());
    }
    @Test public void futureAlarmCannotDeliverEarly() throws Exception {
        Card card=card();card.reminderAt=System.currentTimeMillis()+3600000;CardStore store=new CardStore(context.getFilesDir());store.load();store.save(card);
        Reminders.deliver(context,card.id,card.reminderAt);assertEquals(0,shadowOf(context.getSystemService(NotificationManager.class)).getAllNotifications().size());
    }
    @Test public void blockedSystemNotificationsStillDeliverToLocalInbox() throws Exception {
        Card card=card();card.reminderAt=System.currentTimeMillis()-1000;CardStore store=new CardStore(context.getFilesDir());store.load();store.save(card);
        shadowOf(context.getSystemService(NotificationManager.class)).setNotificationsEnabled(false);
        Reminders.deliver(context,card.id,card.reminderAt);store.load();assertEquals(0,store.find(card.id).reminderAt);assertEquals(1,NotificationInbox.unread(context));
        assertEquals(0,shadowOf(context.getSystemService(NotificationManager.class)).getAllNotifications().size());
    }
    @Test public void rebootRestoresPendingOnly() throws Exception {
        Card card=card();card.reminderAt=System.currentTimeMillis()+3600000;CardStore store=new CardStore(context.getFilesDir());store.load();store.save(card);
        new ReminderReceiver().onReceive(context,new Intent(Intent.ACTION_BOOT_COMPLETED));
        assertEquals(1,shadowOf(context.getSystemService(AlarmManager.class)).getScheduledAlarms().size());
    }
    @Test public void eveningRollsToNextDayAndWeekendToSaturday() {
        TimeZone zone=TimeZone.getTimeZone("Europe/Istanbul"); Calendar c=Calendar.getInstance(zone);c.clear();c.set(2026,9,13,23,0);
        long now=c.getTimeInMillis(); c.setTimeInMillis(ReminderTimes.quick(1,now,zone));assertEquals(14,c.get(Calendar.DAY_OF_MONTH));assertEquals(20,c.get(Calendar.HOUR_OF_DAY));
        c.setTimeInMillis(ReminderTimes.quick(3,now,zone));assertEquals(Calendar.SATURDAY,c.get(Calendar.DAY_OF_WEEK));assertEquals(10,c.get(Calendar.HOUR_OF_DAY));
        assertEquals(now+3600000,ReminderTimes.quick(0,now,zone));
    }
}
