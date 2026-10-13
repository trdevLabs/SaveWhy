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
        shadowOf(Looper.getMainLooper()).idleFor(750, TimeUnit.MILLISECONDS);
        Intent next = shadowOf(activity).getNextStartedActivity();
        assertEquals(MainActivity.class.getName(), next.getComponent().getClassName());
        assertTrue(activity.isFinishing());
        shadowOf(Looper.getMainLooper()).idleFor(1, TimeUnit.SECONDS);
        assertNull(shadowOf(activity).peekNextStartedActivity());
        controller.pause().stop().destroy();
    }
    @Test public void pausedSplashDoesNotNavigateInBackground() {
        ActivityController<SplashActivity> controller = Robolectric.buildActivity(SplashActivity.class).setup();
        controller.pause();
        shadowOf(Looper.getMainLooper()).idleFor(1, TimeUnit.SECONDS);
        assertNull(shadowOf(controller.get()).peekNextStartedActivity());
        controller.resume();
        shadowOf(Looper.getMainLooper()).idle();
        assertNotNull(shadowOf(controller.get()).peekNextStartedActivity());
        controller.pause().stop().destroy();
    }
}
