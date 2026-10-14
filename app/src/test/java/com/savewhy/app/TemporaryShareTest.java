package com.savewhy.app;
import android.content.*;
import android.net.Uri;
import android.app.Activity;
import org.junit.*;
import org.junit.runner.RunWith;
import org.robolectric.*;
import org.robolectric.annotation.*;
import org.robolectric.android.controller.ActivityController;
import org.robolectric.shadows.ShadowContentResolver;
import static org.junit.Assert.*;
@RunWith(RobolectricTestRunner.class) @Config(sdk=28,shadows=TemporaryShareTest.TemporaryResolver.class)
public class TemporaryShareTest {
    @Implements(ContentResolver.class) public static class TemporaryResolver extends ShadowContentResolver {
        @Implementation protected void takePersistableUriPermission(Uri uri,int flags){throw new SecurityException("Only temporary access");}
    }
    @Test public void temporaryShareUsesDocumentPickerInsteadOfCopyOrFakePersistentRecord()throws Exception{
        Uri uri=Uri.parse("content://temporary.gallery/share/photo");ActivityController<MainActivity> controller=Robolectric.buildActivity(MainActivity.class,new Intent(Intent.ACTION_SEND).setType("image/png").putExtra(Intent.EXTRA_STREAM,uri)).setup();MainActivity activity=controller.get();Intent picker=org.robolectric.Shadows.shadowOf(activity).getNextStartedActivity();assertNotNull(picker);assertEquals(Intent.ACTION_OPEN_DOCUMENT,picker.getAction());assertEquals("image/*",picker.getType());assertTrue((picker.getFlags()&Intent.FLAG_GRANT_PERSISTABLE_URI_PERMISSION)!=0);assertEquals(uri,picker.getParcelableExtra(android.provider.DocumentsContract.EXTRA_INITIAL_URI));CardStore store=new CardStore(activity.getFilesDir());store.load();assertTrue(store.all().isEmpty());assertFalse(new ImageStore(activity).exists(LocalMedia.identity(uri)));controller.pause().stop().destroy();
    }
    @Test public void refusedPickerGrantDoesNotSaveAnUnreliableReference()throws Exception{
        ActivityController<MainActivity> controller=Robolectric.buildActivity(MainActivity.class).setup();MainActivity activity=controller.get();java.lang.reflect.Method picker=MainActivity.class.getDeclaredMethod("pickImage");picker.setAccessible(true);picker.invoke(activity);org.robolectric.Shadows.shadowOf(activity).getNextStartedActivity();activity.onActivityResult(44,Activity.RESULT_OK,new Intent().setData(Uri.parse("content://temporary.gallery/photo")));assertNull(org.robolectric.Shadows.shadowOf(activity).getNextStartedActivity());CardStore store=new CardStore(activity.getFilesDir());store.load();assertTrue(store.all().isEmpty());controller.pause().stop().destroy();
    }
}
