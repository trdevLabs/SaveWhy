package com.savewhy.app;

import android.content.Intent;
import android.os.Looper;
import java.util.concurrent.TimeUnit;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.Robolectric;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.android.controller.ActivityController;
import org.robolectric.annotation.Config;
import static org.junit.Assert.*;
import static org.robolectric.Shadows.*;

@RunWith(RobolectricTestRunner.class)
@Config(sdk=28)
public class SplashTest {
    @Test public void opensMainOnceAfterBriefDisplay() {
        ActivityController<SplashActivity> controller = Robolectric.buildActivity(SplashActivity.class).setup();
        SplashActivity activity = controller.get();
        assertNull(shadowOf(activity).peekNextStartedActivity());
        shadowOf(Looper.getMainLooper()).idleFor(2300, TimeUnit.MILLISECONDS);
        Intent next = shadowOf(activity).getNextStartedActivity();
        assertEquals(MainActivity.class.getName(), next.getComponent().getClassName());
        assertTrue(activity.isFinishing());
        shadowOf(Looper.getMainLooper()).idleFor(1, TimeUnit.SECONDS);
        assertNull(shadowOf(activity).peekNextStartedActivity());
        controller.pause().stop().destroy();
    }
    @Test public void tappingSkipsLongerSplashImmediately() {
        ActivityController<SplashActivity> controller=Robolectric.buildActivity(SplashActivity.class).setup();
        android.view.ViewGroup content=controller.get().findViewById(android.R.id.content); content.getChildAt(0).performClick();
        assertNotNull(shadowOf(controller.get()).peekNextStartedActivity());
        controller.pause().stop().destroy();
    }
    @Test public void copyrightReplacesVisibleTapText() {
        ActivityController<SplashActivity> controller=Robolectric.buildActivity(SplashActivity.class).setup();android.widget.TextView copyright=controller.get().findViewById(R.id.splash_copyright);assertNotNull(copyright);assertEquals("Copy Right 2026",copyright.getText().toString());controller.pause().stop().destroy();
    }
    @Test public void overlayReturnsToExistingMainWithoutLaunchingAnotherActivity(){
        ActivityController<SplashActivity> controller=Robolectric.buildActivity(SplashActivity.class,new Intent().putExtra("overlay",true)).setup();shadowOf(Looper.getMainLooper()).idleFor(2300,TimeUnit.MILLISECONDS);assertTrue(controller.get().isFinishing());assertNull(shadowOf(controller.get()).peekNextStartedActivity());controller.pause().stop().destroy();
    }
    @Test public void launcherShareAndNotificationsUseSameMainTask()throws Exception{
        android.content.Context context=org.robolectric.RuntimeEnvironment.getApplication();android.content.pm.ActivityInfo main=context.getPackageManager().getActivityInfo(new android.content.ComponentName(context,MainActivity.class),0);assertEquals(android.content.pm.ActivityInfo.LAUNCH_SINGLE_TASK,main.launchMode);assertEquals(android.content.pm.ActivityInfo.DOCUMENT_LAUNCH_NEVER,main.documentLaunchMode);
        android.content.pm.ResolveInfo launcher=context.getPackageManager().resolveActivity(new Intent(Intent.ACTION_MAIN).addCategory(Intent.CATEGORY_LAUNCHER).setPackage(context.getPackageName()),0);assertNotNull(launcher);assertEquals(MainActivity.class.getName(),launcher.activityInfo.name);
    }
    @Test public void pausedSplashDoesNotNavigateInBackground() {
        ActivityController<SplashActivity> controller = Robolectric.buildActivity(SplashActivity.class).setup();
        controller.pause();
        shadowOf(Looper.getMainLooper()).idleFor(3, TimeUnit.SECONDS);
        assertNull(shadowOf(controller.get()).peekNextStartedActivity());
        controller.resume();
        shadowOf(Looper.getMainLooper()).idle();
        assertNotNull(shadowOf(controller.get()).peekNextStartedActivity());
        controller.pause().stop().destroy();
    }
}
