package com.savewhy.app;

import android.app.*;
import android.content.*;
import android.net.Uri;
import android.os.Build;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

/** Local, one-shot inexact alarms: no server, model key or exact-alarm permission. */
public final class Reminders {
    static final String CHANNEL="saved_intents";
    private Reminders() {}
    static void channel(Context context) {
        NotificationChannel channel=new NotificationChannel(CHANNEL,I18n.text(I18n.context(context),"Hatırlatmalar"),NotificationManager.IMPORTANCE_DEFAULT);
        channel.setDescription(I18n.text(I18n.context(context),"Seçtiğin zamanda kayıtlarına dön."));
        context.getSystemService(NotificationManager.class).createNotificationChannel(channel);
    }
    static boolean enabled(Context context) {
        channel(context);
        NotificationChannel channel=context.getSystemService(NotificationManager.class).getNotificationChannel(CHANNEL);
        return NotificationManagerCompat.from(context).areNotificationsEnabled() && channel.getImportance()!=NotificationManager.IMPORTANCE_NONE;
    }
    private static PendingIntent alarm(Context context,String id,long time) {
        Intent intent=new Intent(context,ReminderReceiver.class).setAction("com.savewhy.REMIND").setData(Uri.parse("savewhy://reminder/"+Uri.encode(id))).putExtra("id",id).putExtra("time",time);
        return PendingIntent.getBroadcast(context,0,intent,PendingIntent.FLAG_UPDATE_CURRENT|PendingIntent.FLAG_IMMUTABLE);
    }
    public static void cancel(Context context,String id) {
        context.getSystemService(AlarmManager.class).cancel(alarm(context,id,0));
        NotificationManagerCompat.from(context).cancel(id,0);
    }
    public static void sync(Context context,Card card) {
        cancel(context,card.id);
        if(card.done || card.reminderAt<=0) return;
        context.getSystemService(AlarmManager.class).setAndAllowWhileIdle(AlarmManager.RTC_WAKEUP,Math.max(System.currentTimeMillis()+1000,card.reminderAt),alarm(context,card.id,card.reminderAt));
    }
    public static void restore(Context context) {
        try { CardStore store=new CardStore(context.getFilesDir()); store.load(); restore(context,store.all()); }
        catch(Exception ignored) { /* Corrupt data stays untouched. */ }
    }
    public static void restore(Context context,Iterable<Card> cards) { for(Card c:cards) if(c.reminderAt>0)sync(context,c); }
    static void deliver(Context context,String id,long expectedTime) {
        try {
            CardStore store=new CardStore(context.getFilesDir()); store.load(); Card card=store.find(id);
            if(card==null || card.done || card.reminderAt==0 || card.reminderAt!=expectedTime || card.reminderAt>System.currentTimeMillis()) return;
            if(!enabled(context)) return;
            if(Build.VERSION.SDK_INT>=33 && androidx.core.content.ContextCompat.checkSelfPermission(context,android.Manifest.permission.POST_NOTIFICATIONS)!=android.content.pm.PackageManager.PERMISSION_GRANTED) return;
            Context localized=I18n.context(context);
            Intent open=new Intent(context,MainActivity.class).setAction("com.savewhy.OPEN_CARD").setData(Uri.parse("savewhy://card/"+Uri.encode(id))).putExtra("cardId",id).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK|Intent.FLAG_ACTIVITY_CLEAR_TOP);
            PendingIntent click=PendingIntent.getActivity(context,0,open,PendingIntent.FLAG_UPDATE_CURRENT|PendingIntent.FLAG_IMMUTABLE);
            NotificationCompat.Builder notification=new NotificationCompat.Builder(context,CHANNEL)
                .setSmallIcon(R.drawable.ic_bookmark).setContentTitle(card.title)
                .setContentText(I18n.text(localized,"Kaydına dönme zamanı.")).setContentIntent(click).setAutoCancel(true)
                .setVisibility(NotificationCompat.VISIBILITY_PRIVATE);
            NotificationManagerCompat.from(context).notify(id,0,notification.build());
            Card consumed=card.copy(); consumed.reminderAt=0; store.save(consumed,false);
        } catch(Exception ignored) { /* Never erase a failed or unreadable save. */ }
    }
}
