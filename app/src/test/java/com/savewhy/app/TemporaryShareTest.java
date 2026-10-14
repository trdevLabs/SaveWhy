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
@RunWith(RobolectricTestRunner.class) @Config(sdk=28,shadows=TemporaryShareTest.TemporaryResolver.class) @GraphicsMode(GraphicsMode.Mode.NATIVE)
public class TemporaryShareTest {
    @Implements(ContentResolver.class) public static class TemporaryResolver extends ShadowContentResolver {
        @Implementation protected void takePersistableUriPermission(Uri uri,int flags){throw new SecurityException("Only temporary access");}
    }
    @Test public void temporaryShareOpensExactImageWithoutReselectingAndKeepsReadableCopy()throws Exception{
        Uri uri=Uri.parse("content://temporary.gallery/share/photo");
        android.graphics.Bitmap photo=android.graphics.Bitmap.createBitmap(600,800,android.graphics.Bitmap.Config.ARGB_8888);photo.eraseColor(android.graphics.Color.WHITE);
        java.io.ByteArrayOutputStream bytes=new java.io.ByteArrayOutputStream();photo.compress(android.graphics.Bitmap.CompressFormat.PNG,100,bytes);photo.recycle();
        org.robolectric.Shadows.shadowOf(RuntimeEnvironment.getApplication().getContentResolver()).registerInputStreamSupplier(uri,()->new java.io.ByteArrayInputStream(bytes.toByteArray()));
        ActivityController<MainActivity> controller=Robolectric.buildActivity(MainActivity.class).setup();
        try{
            MainActivity activity=controller.get();java.lang.reflect.Field scanner=MainActivity.class.getDeclaredField("imageOcr");scanner.setAccessible(true);scanner.set(activity,new ImageOcr(){void read(android.graphics.Bitmap bitmap,java.util.function.BiConsumer<String,Boolean> result){bitmap.recycle();result.accept("ılık ışık",true);}});activity.onNewIntent(new Intent(Intent.ACTION_SEND).setType("image/png").putExtra(Intent.EXTRA_STREAM,uri));
            android.app.Dialog dialog=null;long end=System.currentTimeMillis()+5000;
            while(System.currentTimeMillis()<end){org.robolectric.Shadows.shadowOf(android.os.Looper.getMainLooper()).idle();dialog=org.robolectric.shadows.ShadowDialog.getLatestDialog();if(dialog!=null&&dialog.findViewById(R.id.capture_image)!=null)break;Thread.sleep(20);}
            assertNotNull(dialog);assertNotNull(dialog.findViewById(R.id.capture_image));assertNull(org.robolectric.Shadows.shadowOf(activity).getNextStartedActivity());
            com.google.android.material.chip.ChipGroup choices=dialog.findViewById(R.id.capture_intents);((com.google.android.material.chip.Chip)choices.getChildAt(0)).performClick();dialog.findViewById(R.id.capture_save).performClick();
            CardStore store=new CardStore(activity.getFilesDir());store.load();assertEquals(1,store.all().size());Card card=store.all().get(0);assertFalse(card.isReference());assertTrue(new ImageStore(activity).exists(card.imageId));assertTrue(card.isImage());
        }finally{controller.pause().stop().destroy();}
    }
    @Test public void refusedPickerGrantDoesNotSaveAnUnreliableReference()throws Exception{
        ActivityController<MainActivity> controller=Robolectric.buildActivity(MainActivity.class).setup();MainActivity activity=controller.get();java.lang.reflect.Method picker=MainActivity.class.getDeclaredMethod("pickImage");picker.setAccessible(true);picker.invoke(activity);org.robolectric.Shadows.shadowOf(activity).getNextStartedActivity();activity.onActivityResult(44,Activity.RESULT_OK,new Intent().setData(Uri.parse("content://temporary.gallery/photo")));assertNull(org.robolectric.Shadows.shadowOf(activity).getNextStartedActivity());CardStore store=new CardStore(activity.getFilesDir());store.load();assertTrue(store.all().isEmpty());controller.pause().stop().destroy();
    }
}
