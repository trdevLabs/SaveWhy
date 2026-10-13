package com.savewhy.app;
import android.content.*;
public class ReminderReceiver extends BroadcastReceiver {
    @Override public void onReceive(Context context,Intent intent) {
        if("com.savewhy.REMIND".equals(intent.getAction())) Reminders.deliver(context,intent.getStringExtra("id"),intent.getLongExtra("time",0));
        else if(Intent.ACTION_BOOT_COMPLETED.equals(intent.getAction()) || Intent.ACTION_MY_PACKAGE_REPLACED.equals(intent.getAction()) || Intent.ACTION_TIME_CHANGED.equals(intent.getAction()) || Intent.ACTION_TIMEZONE_CHANGED.equals(intent.getAction())) Reminders.restore(context);
    }
}
