package com.savewhy.app;
import java.util.*;
public final class ReminderTimes {
    private ReminderTimes() {}
    public static long quick(int choice,long now,TimeZone zone) {
        if(choice==0)return now+3600000;
        Calendar c=Calendar.getInstance(zone); c.setTimeInMillis(now);
        c.set(Calendar.SECOND,0); c.set(Calendar.MILLISECOND,0); c.set(Calendar.MINUTE,0);
        if(choice==1) { c.set(Calendar.HOUR_OF_DAY,20); if(c.getTimeInMillis()<=now)c.add(Calendar.DAY_OF_YEAR,1); }
        else if(choice==2) { c.add(Calendar.DAY_OF_YEAR,1); c.set(Calendar.HOUR_OF_DAY,9); }
        else if(choice==3) { c.set(Calendar.HOUR_OF_DAY,10); while(c.get(Calendar.DAY_OF_WEEK)!=Calendar.SATURDAY || c.getTimeInMillis()<=now)c.add(Calendar.DAY_OF_YEAR,1); }
        else throw new IllegalArgumentException("Unknown reminder choice");
        return c.getTimeInMillis();
    }
}
