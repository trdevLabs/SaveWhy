package com.savewhy.app;

import android.app.Dialog;
import android.content.Intent;
import android.graphics.*;
import android.os.*;
import android.view.View;
import android.widget.EditText;
import androidx.appcompat.app.AppCompatDelegate;
import com.google.android.material.chip.*;
import org.junit.*;
import org.junit.runner.RunWith;
import org.robolectric.*;
import org.robolectric.android.controller.ActivityController;
import org.robolectric.annotation.*;
import org.robolectric.shadows.ShadowDialog;
import java.io.*;
import static org.junit.Assert.*;

@RunWith(RobolectricTestRunner.class)
@Config(sdk=28,qualifiers="w411dp-h891dp-xhdpi")
@GraphicsMode(GraphicsMode.Mode.NATIVE)
public class UiTest {
    private ActivityController<MainActivity> controller;
    @After public void tearDown() { if(controller!=null) controller.pause().stop().destroy(); }
    private MainActivity launch(boolean share,int theme) {
        RuntimeEnvironment.getApplication().getSharedPreferences("settings",0).edit().putInt("theme",theme).commit();
        Intent intent=new Intent(); if(share) intent.setAction(Intent.ACTION_SEND).setType("text/plain").putExtra(Intent.EXTRA_TEXT,"https://youtu.be/demo");
        controller=Robolectric.buildActivity(MainActivity.class,intent).setup(); return controller.get();
    }
    private void tap(ChipGroup group,String label) {
        for(int i=0;i<group.getChildCount();i++) { Chip c=(Chip)group.getChildAt(i); if(c.getText().toString().equals(label)) { c.performClick(); return; } }
        fail("Missing choice: "+label);
    }
    private void snapshot(View view,String name) throws IOException {
        int w=view.getResources().getDisplayMetrics().widthPixels,h=view.getResources().getDisplayMetrics().heightPixels;
        view.measure(View.MeasureSpec.makeMeasureSpec(w,View.MeasureSpec.EXACTLY),View.MeasureSpec.makeMeasureSpec(h,View.MeasureSpec.EXACTLY)); view.layout(0,0,w,h);
        Bitmap bitmap=Bitmap.createBitmap(w,h,Bitmap.Config.ARGB_8888); view.draw(new Canvas(bitmap));
        File directory=new File("build/reports/ui"); directory.mkdirs();
        try(OutputStream out=new FileOutputStream(new File(directory,name+".png"))) { assertTrue(bitmap.compress(Bitmap.CompressFormat.PNG,100,out)); }
    }
    @Test public void splashArtworkRendersOnBlack() throws Exception {
        ActivityController<SplashActivity> splash=Robolectric.buildActivity(SplashActivity.class).setup();
        snapshot(splash.get().getWindow().getDecorView(),"splash");
        splash.pause().stop().destroy();
    }
    @Test public void sharedLinkSavesWithIntentAndNoTypedFields() throws Exception {
        MainActivity activity=launch(true,AppCompatDelegate.MODE_NIGHT_YES); Dialog dialog=ShadowDialog.getLatestDialog(); assertNotNull(dialog);
        assertFalse(dialog.findViewById(R.id.capture_save).isEnabled());
        tap(dialog.findViewById(R.id.capture_intents),"İzle");
        assertEquals("",((EditText)dialog.findViewById(R.id.capture_title)).getText().toString());
        assertEquals("",((EditText)dialog.findViewById(R.id.capture_note)).getText().toString());
        assertTrue(dialog.findViewById(R.id.capture_save).isEnabled()); snapshot(dialog.getWindow().getDecorView(),"amoled-capture");
        dialog.findViewById(R.id.capture_save).performClick();
        CardStore persisted=new CardStore(activity.getFilesDir()); persisted.load(); assertEquals(1,persisted.all().size());
        assertEquals("İzle",persisted.all().get(0).why); assertEquals("Sonra izleyeceğim",persisted.all().get(0).note);
    }
    @Test public void amoledHomeUsesActualBlackAndEmbeddedFont() throws Exception {
        MainActivity activity=launch(false,AppCompatDelegate.MODE_NIGHT_YES);
        assertEquals(Color.BLACK,activity.getResources().getColor(R.color.app_background));
        assertFalse(activity.getResources().getBoolean(R.bool.light_system_bars)); snapshot(activity.getWindow().getDecorView(),"amoled-home");
    }
    @Test public void lightPreferenceAppliesToHomeComponents() throws Exception {
        MainActivity activity=launch(false,AppCompatDelegate.MODE_NIGHT_NO);
        assertNotEquals(Color.BLACK,activity.getResources().getColor(R.color.app_background));
        assertTrue(activity.getResources().getBoolean(R.bool.light_system_bars)); snapshot(activity.getWindow().getDecorView(),"light-home");
    }
    @Test public void captureChoicesAndOptionalDetailsSurviveRecreation() throws Exception {
        launch(true,AppCompatDelegate.MODE_NIGHT_YES); Dialog dialog=ShadowDialog.getLatestDialog();
        tap(dialog.findViewById(R.id.capture_intents),"Üret"); tap(dialog.findViewById(R.id.capture_reasons),"İlham aldım");
        dialog.findViewById(R.id.capture_details).performClick(); ((EditText)dialog.findViewById(R.id.capture_note)).setText("Kendi açımla anlatacağım");
        Bundle saved=new Bundle(); controller.saveInstanceState(saved).pause().stop().destroy(); controller=null;
        controller=Robolectric.buildActivity(MainActivity.class,new Intent()).create(saved).start().restoreInstanceState(saved).resume().visible();
        Dialog restored=ShadowDialog.getLatestDialog(); assertNotNull(restored);
        assertEquals("Kendi açımla anlatacağım",((EditText)restored.findViewById(R.id.capture_note)).getText().toString());
        restored.findViewById(R.id.capture_save).performClick();
        CardStore store=new CardStore(controller.get().getFilesDir()); store.load(); assertEquals("Üret",store.all().get(0).why); assertEquals("Kendi açımla anlatacağım",store.all().get(0).note);
    }
    @Test public void savedCardsUseARecycledList() throws Exception {
        CardStore data=new CardStore(RuntimeEnvironment.getApplication().getFilesDir());
        for(int i=0;i<40;i++) data.save(new Card("fixture-"+i,"https://youtu.be/example"+i,
            i%2==0?"Akşam için kısa belgesel":"Kendi videom için anlatım fikri",i%2==0?"İzle":"Üret",
            i%2==0?"Akşam için":"İlham aldım",i,false,java.util.Collections.emptyList()));
        MainActivity activity=launch(false,AppCompatDelegate.MODE_NIGHT_YES);
        androidx.recyclerview.widget.RecyclerView list=activity.findViewById(R.id.main_list);
        for(int i=0;i<20 && list.getAdapter().getItemCount()!=40;i++) {
            Thread.sleep(20); org.robolectric.Shadows.shadowOf(Looper.getMainLooper()).idle();
        }
        assertEquals(40,list.getAdapter().getItemCount()); snapshot(activity.getWindow().getDecorView(),"amoled-cards");
        assertTrue(list.getChildCount()<40);
    }
    @Test @Config(qualifiers="w360dp-h640dp-xhdpi") public void saveButtonStaysVisibleOnSmallScreen() throws Exception {
        launch(true,AppCompatDelegate.MODE_NIGHT_YES); Dialog dialog=ShadowDialog.getLatestDialog();
        tap(dialog.findViewById(R.id.capture_intents),"İzle"); snapshot(dialog.getWindow().getDecorView(),"amoled-small-capture");
        View button=dialog.findViewById(R.id.capture_save); Rect visible=new Rect(); assertTrue(button.getGlobalVisibleRect(visible));
        assertEquals(button.getHeight(),visible.height());
    }
}
