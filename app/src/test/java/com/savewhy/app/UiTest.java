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
    @Before public void language() { AppCompatDelegate.setApplicationLocales(androidx.core.os.LocaleListCompat.forLanguageTags("tr")); }
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
    private boolean hasText(View view,String text) {
        if(view instanceof android.widget.TextView && ((android.widget.TextView)view).getText().toString().equals(text))return true;
        if(view instanceof android.view.ViewGroup) {android.view.ViewGroup group=(android.view.ViewGroup)view;for(int i=0;i<group.getChildCount();i++)if(hasText(group.getChildAt(i),text))return true;}
        return false;
    }
    @Test public void swappingPagesReplaysSafeAreaInsetsImmediately() throws Exception {
        MainActivity activity=launch(false,AppCompatDelegate.MODE_NIGHT_YES);android.view.ViewGroup content=activity.findViewById(android.R.id.content);View root=content.getChildAt(0);
        androidx.core.view.WindowInsetsCompat insets=new androidx.core.view.WindowInsetsCompat.Builder().setInsets(androidx.core.view.WindowInsetsCompat.Type.systemBars(),androidx.core.graphics.Insets.of(0,48,0,30)).build();
        androidx.core.view.ViewCompat.dispatchApplyWindowInsets(root,insets);activity.findViewById(R.id.main_notifications).performClick();assertEquals(48,content.getChildAt(0).getPaddingTop());assertEquals(30,content.getChildAt(0).getPaddingBottom());
    }
    @Test public void youtubeDetailHasSmallTitleAndExternalPreview() throws Exception {
        String url="https://youtube.com/shorts/abcDEF123_-";Card card=new Card("video","https://youtube.com/shorts/abcDEF123_-","A shared video title","İzle","Later",1,false,java.util.Collections.emptyList());
        java.io.File cache=new java.io.File(RuntimeEnvironment.getApplication().getCacheDir(),"thumbnails");cache.mkdirs();Bitmap preview=Bitmap.createBitmap(320,180,Bitmap.Config.ARGB_8888);preview.eraseColor(Color.DKGRAY);try(java.io.OutputStream out=new java.io.FileOutputStream(new java.io.File(cache,"abcDEF123_-.jpg"))){preview.compress(Bitmap.CompressFormat.JPEG,85,out);}
        CardStore saved=new CardStore(RuntimeEnvironment.getApplication().getFilesDir());saved.load();saved.save(card);MainActivity activity=launch(false,AppCompatDelegate.MODE_NIGHT_YES);
        java.lang.reflect.Method detail=MainActivity.class.getDeclaredMethod("detail",String.class);detail.setAccessible(true);detail.invoke(activity,card.id);Dialog dialog=ShadowDialog.getLatestDialog();assertNotNull(dialog.findViewById(R.id.detail_video));assertTrue(hasText(dialog.getWindow().getDecorView(),"İçeriği izle"));assertFalse(hasText(dialog.getWindow().getDecorView(),"Videoyu oynat"));snapshot(dialog.getWindow().getDecorView(),"youtube-detail-v07");
    }
    private android.webkit.WebView embeddedView(android.view.ViewGroup group) {
        for(int i=0;i<group.getChildCount();i++){View child=group.getChildAt(i);if(child instanceof android.webkit.WebView)return (android.webkit.WebView)child;if(child instanceof android.view.ViewGroup){android.webkit.WebView result=embeddedView((android.view.ViewGroup)child);if(result!=null)return result;}}
        return null;
    }
    @Test public void allPlatformsOpenExternalLinksWithoutWebViews() throws Exception {
        MainActivity activity=launch(false,AppCompatDelegate.MODE_NIGHT_YES);
        for(String url:new String[]{"https://youtu.be/abcDEF123_-","https://www.instagram.com/reel/demo/","https://www.facebook.com/watch/?v=123","https://www.tiktok.com/@demo/video/123"}){
            Card card=new Card("external",url,"Saved video","İzle","Later",1,false,java.util.Collections.emptyList());CardStore saved=new CardStore(RuntimeEnvironment.getApplication().getFilesDir());saved.load();saved.save(card);controller.pause().resume();
            java.lang.reflect.Method detail=MainActivity.class.getDeclaredMethod("detail",String.class);detail.setAccessible(true);detail.invoke(activity,card.id);Dialog dialog=ShadowDialog.getLatestDialog();assertNull(embeddedView((android.view.ViewGroup)dialog.getWindow().getDecorView()));dialog.findViewById(R.id.detail_video).performClick();Intent intent=org.robolectric.Shadows.shadowOf(activity).getNextStartedActivity();assertEquals(Intent.ACTION_VIEW,intent.getAction());assertEquals(url,intent.getDataString());dialog.dismiss();
        }
    }
    private String screenshotAsset() throws Exception {
        Bitmap b=Bitmap.createBitmap(1080,2400,Bitmap.Config.ARGB_8888);b.eraseColor(Color.WHITE);ByteArrayOutputStream out=new ByteArrayOutputStream();b.compress(Bitmap.CompressFormat.PNG,100,out);b.recycle();return new ImageStore(RuntimeEnvironment.getApplication()).ingest(new ByteArrayInputStream(out.toByteArray()));
    }
    private void awaitUi(java.util.function.BooleanSupplier ready)throws Exception{long until=System.currentTimeMillis()+5000;while(!ready.getAsBoolean()&&System.currentTimeMillis()<until){Thread.sleep(20);org.robolectric.Shadows.shadowOf(Looper.getMainLooper()).idle();}assertTrue(ready.getAsBoolean());}
    @Test public void imageCaptureSavesWithoutUrlAndOcrIsSearchable() throws Exception {
        MainActivity activity=launch(false,AppCompatDelegate.MODE_NIGHT_YES);java.lang.reflect.Field scanner=MainActivity.class.getDeclaredField("imageOcr");scanner.setAccessible(true);scanner.set(activity,new ImageOcr(){void read(Bitmap bitmap,java.util.function.BiConsumer<String,Boolean> result){bitmap.recycle();result.accept("Kablosuz kulaklık fiyatı",true);}});
        Bundle draft=new Bundle();draft.putString("imageId",screenshotAsset());draft.putString("why","Al");draft.putString("tags","Alışveriş, Fikir");java.lang.reflect.Method show=MainActivity.class.getDeclaredMethod("showCapture",Bundle.class);show.setAccessible(true);show.invoke(activity,draft);Dialog dialog=ShadowDialog.getLatestDialog();assertNotNull(dialog.findViewById(R.id.capture_image));assertTrue(dialog.findViewById(R.id.capture_save).isEnabled());assertEquals("",((EditText)dialog.findViewById(R.id.capture_link)).getText().toString());awaitUi(()->((android.widget.ImageView)dialog.findViewById(R.id.capture_image)).getDrawable() instanceof android.graphics.drawable.BitmapDrawable);snapshot(dialog.getWindow().getDecorView(),"screenshot-capture-v010");dialog.findViewById(R.id.capture_save).performClick();CardStore store=new CardStore(activity.getFilesDir());
        awaitUi(()->{try{store.load();return store.all().size()==1&&store.all().get(0).ocrState.equals("ready");}catch(Exception e){return false;}});Card card=store.all().get(0);assertTrue(card.isImage());assertEquals(java.util.Arrays.asList("Alışveriş","Fikir"),card.tags);assertTrue(card.matches("kulaklik"));assertEquals("",card.url);assertTrue(new ImageStore(activity).exists(card.imageId));
    }
    @Test public void galleryEntryUsesSystemPickerWithoutStoragePermission() throws Exception {
        MainActivity activity=launch(false,AppCompatDelegate.MODE_NIGHT_YES);java.lang.reflect.Method pick=MainActivity.class.getDeclaredMethod("pickImage");pick.setAccessible(true);pick.invoke(activity);Intent intent=org.robolectric.Shadows.shadowOf(activity).getNextStartedActivity();assertEquals(Intent.ACTION_OPEN_DOCUMENT,intent.getAction());assertEquals("image/*",intent.getType());assertTrue(intent.hasCategory(Intent.CATEGORY_OPENABLE));
    }
    @Test public void imageShareIsAcceptedAndStoredBeforeCapture() throws Exception {
        android.net.Uri uri=android.net.Uri.parse("content://com.example.test/screenshots/one");Bitmap bitmap=Bitmap.createBitmap(320,600,Bitmap.Config.ARGB_8888);ByteArrayOutputStream out=new ByteArrayOutputStream();bitmap.compress(Bitmap.CompressFormat.PNG,100,out);org.robolectric.Shadows.shadowOf(RuntimeEnvironment.getApplication().getContentResolver()).registerInputStream(uri,new ByteArrayInputStream(out.toByteArray()));Intent intent=new Intent(Intent.ACTION_SEND).setType("image/png").putExtra(Intent.EXTRA_STREAM,uri);controller=Robolectric.buildActivity(MainActivity.class,intent).setup();awaitUi(()->{Dialog dialog=ShadowDialog.getLatestDialog();return dialog!=null&&dialog.findViewById(R.id.capture_image)!=null;});Dialog dialog=ShadowDialog.getLatestDialog();assertFalse(dialog.findViewById(R.id.capture_save).isEnabled());tap(dialog.findViewById(R.id.capture_intents),"Al");assertTrue(dialog.findViewById(R.id.capture_save).isEnabled());
    }
    @Test public void imageDetailOpensLocalZoomInsteadOfExternalUrl() throws Exception {
        String asset=screenshotAsset();Card card=new Card("local-image","","Screenshot","Al","Later",1,false,java.util.Collections.emptyList(),"",asset,java.util.Collections.singletonList("Tarif"),"ingredients","ready");CardStore store=new CardStore(RuntimeEnvironment.getApplication().getFilesDir());store.load();store.save(card);MainActivity activity=launch(false,AppCompatDelegate.MODE_NIGHT_YES);java.lang.reflect.Method detail=MainActivity.class.getDeclaredMethod("detail",String.class);detail.setAccessible(true);detail.invoke(activity,card.id);Dialog dialog=ShadowDialog.getLatestDialog();assertFalse(hasText(dialog.getWindow().getDecorView(),"Orijinal içeriği aç"));assertTrue(hasText(dialog.getWindow().getDecorView(),"Tarif"));dialog.findViewById(R.id.detail_video).performClick();assertNotNull(ShadowDialog.getLatestDialog().findViewById(R.id.image_viewer));assertNull(org.robolectric.Shadows.shadowOf(activity).getNextStartedActivity());
    }
    @Test public void usageBarCountsRecordsAndPurchaseShowsConfiguredPrice()throws Exception{
        CardStore store=new CardStore(RuntimeEnvironment.getApplication().getFilesDir());store.load();for(int i=0;i<3;i++)store.save(new Card("use"+i,"https://example.com/"+i,"Saved","Al","Later",1,false,java.util.Collections.emptyList()));MainActivity activity=launch(false,AppCompatDelegate.MODE_NIGHT_YES);com.google.android.material.progressindicator.LinearProgressIndicator bar=activity.findViewById(R.id.usage_progress);assertEquals(20,bar.getMax());assertEquals(3,bar.getProgress());assertTrue(((android.widget.TextView)activity.findViewById(R.id.usage_label)).getText().toString().startsWith("3 / 20"));activity.findViewById(R.id.usage_box).performClick();assertEquals("$4.99",((android.widget.TextView)activity.findViewById(R.id.purchase_price)).getText().toString());assertTrue(hasText(activity.getWindow().getDecorView(),"Ekran görüntüleri, videolar ve etiketler"));assertFalse(activity.findViewById(R.id.purchase_buy).isEnabled());snapshot(activity.getWindow().getDecorView(),"purchase-v010-price");
    }
    @Test public void twentyFirstCaptureShowsRealPurchaseScreenWithoutUnlock() throws Exception {
        CardStore store=new CardStore(RuntimeEnvironment.getApplication().getFilesDir());store.load();for(int i=0;i<20;i++)store.save(new Card("limit"+i,"https://example.com/"+i,"Saved","İzle","Later",1,false,java.util.Collections.emptyList()));
        MainActivity activity=launch(true,AppCompatDelegate.MODE_NIGHT_YES);assertNotNull(activity.findViewById(R.id.purchase_screen));assertTrue(hasText(activity.getWindow().getDecorView(),"Tek seferlik ödeme. Abonelik yok."));assertFalse(activity.findViewById(R.id.purchase_buy).isEnabled());store.load();assertEquals(20,store.all().size());snapshot(activity.getWindow().getDecorView(),"purchase-v08");
    }
    @Test public void supportEmailUsesRequestedRecipientAndSubjectPrefix() throws Exception {
        MainActivity activity=launch(false,AppCompatDelegate.MODE_NIGHT_YES);java.lang.reflect.Method email=MainActivity.class.getDeclaredMethod("composeMail",String.class,String.class);email.setAccessible(true);email.invoke(activity,"Destek","My feedback");android.content.Intent intent=org.robolectric.Shadows.shadowOf(activity).getNextStartedActivity();assertNotNull(intent);assertEquals("tr.developer@hotmail.com",android.net.MailTo.parse(intent.getDataString()).getTo());assertTrue(android.net.MailTo.parse(intent.getDataString()).getSubject().startsWith("savewhy"));assertTrue(intent.getStringExtra(android.content.Intent.EXTRA_SUBJECT).startsWith("savewhy"));assertEquals(android.content.Intent.ACTION_SENDTO,intent.getAction());
    }
    @Test public void homeHasBellAndSettingsOpensFullScreenRows() throws Exception {
        MainActivity activity=launch(false,AppCompatDelegate.MODE_NIGHT_YES);View home=activity.getWindow().getDecorView();
        assertFalse(hasText(home,"Nasıl çalışır?"));assertNotNull(home.findViewById(R.id.main_notifications));
        home.findViewById(R.id.main_settings).performClick();View screen=activity.findViewById(R.id.settings_screen);assertNotNull(screen);
        assertTrue(hasText(screen,"Genel"));assertTrue(hasText(screen,"Yedekleme"));snapshot(activity.getWindow().getDecorView(),"settings-v06");
    }
    @Test public void bellShowsHistoryAndMarksItRead() throws Exception {
        Card card=new Card("notice","https://example.com","Saved title","İzle","Reason",1,false,java.util.Collections.emptyList());NotificationInbox.record(RuntimeEnvironment.getApplication(),card,42);
        MainActivity activity=launch(false,AppCompatDelegate.MODE_NIGHT_YES);activity.findViewById(R.id.main_notifications).performClick();
        assertNotNull(activity.findViewById(R.id.notifications_screen));assertTrue(hasText(activity.getWindow().getDecorView(),"Saved title"));assertEquals(0,NotificationInbox.unread(activity));snapshot(activity.getWindow().getDecorView(),"notifications-v06");
    }
    @Test public void simplifiedHomeUsesInlineSearchAndForgottenTab() throws Exception {
        MainActivity activity=launch(false,AppCompatDelegate.MODE_NIGHT_YES);View view=activity.getWindow().getDecorView();
        assertFalse(hasText(view,"Kaydet. Nedenini hatırla. Harekete geç."));
        assertEquals("Kayıtlarında ara",((EditText)view.findViewById(R.id.main_search)).getHint().toString());assertTrue(hasText(view,"Unutulanlar"));
        snapshot(view,"amoled-home-v05");
    }
    @Test public void editingWithHiddenReasonKeepsOriginalNote() throws Exception {
        Choices choices=new Choices(RuntimeEnvironment.getApplication());choices.setReason("İzle","Sonra izleyeceğim","Later",false);
        Card card=new Card("hidden","https://example.com","Title","İzle","Sonra izleyeceğim",1,false,java.util.Collections.emptyList());
        CardStore persisted=new CardStore(RuntimeEnvironment.getApplication().getFilesDir());persisted.load();persisted.save(card);
        MainActivity activity=launch(false,AppCompatDelegate.MODE_NIGHT_YES);java.lang.reflect.Method edit=MainActivity.class.getDeclaredMethod("editCard",String.class);edit.setAccessible(true);edit.invoke(activity,card.id);
        Dialog dialog=ShadowDialog.getLatestDialog();dialog.findViewById(R.id.capture_save).performClick();persisted.load();assertEquals("Sonra izleyeceğim",persisted.find(card.id).note);
    }
    @Test public void customIntentAndReasonSaveWithoutTypedFields() throws Exception {
        Choices choices=new Choices(RuntimeEnvironment.getApplication());String key=Choices.customKey("Arşivle");choices.setIntent(key,"Arşivle",true);
        String reason=Choices.customKey("Ailem için");choices.setReason(key,reason,"Ailem için",true);
        MainActivity activity=launch(true,AppCompatDelegate.MODE_NIGHT_YES);Dialog dialog=ShadowDialog.getLatestDialog();tap(dialog.findViewById(R.id.capture_intents),"Arşivle");
        assertTrue(hasText(dialog.getWindow().getDecorView(),"Ailem için"));dialog.findViewById(R.id.capture_save).performClick();
        CardStore persisted=new CardStore(activity.getFilesDir());persisted.load();assertEquals(key,persisted.all().get(0).why);assertEquals("Ailem için",persisted.all().get(0).note);
    }
    @Test public void englishQuickCaptureUsesTranslatedChoicesWithoutTyping() throws Exception {
        AppCompatDelegate.setApplicationLocales(androidx.core.os.LocaleListCompat.forLanguageTags("en"));
        MainActivity activity=launch(true,AppCompatDelegate.MODE_NIGHT_YES); Dialog dialog=ShadowDialog.getLatestDialog();
        tap(dialog.findViewById(R.id.capture_intents),"Watch");
        assertEquals("Watch · save",((android.widget.TextView)dialog.findViewById(R.id.capture_save)).getText().toString());
        dialog.findViewById(R.id.capture_save).performClick();
        CardStore persisted=new CardStore(activity.getFilesDir());persisted.load();assertEquals("İzle",persisted.all().get(0).why);
        assertEquals("I will watch later",persisted.all().get(0).note);
    }
    @Test public void optionalDetailsUseSeparateLabelsWithoutOverlappingHints() throws Exception {
        launch(true,AppCompatDelegate.MODE_NIGHT_YES); Dialog dialog=ShadowDialog.getLatestDialog(); tap(dialog.findViewById(R.id.capture_intents),"İzle");
        dialog.findViewById(R.id.capture_details).performClick();
        EditText note=dialog.findViewById(R.id.capture_note);
        com.google.android.material.textfield.TextInputLayout box=(com.google.android.material.textfield.TextInputLayout)note.getParent().getParent();
        assertFalse(box.isHintEnabled()); assertNull(note.getHint());
        assertNotNull(dialog.findViewById(R.id.capture_reminder));
        snapshot(dialog.getWindow().getDecorView(),"amoled-details");
        note.requestRectangleOnScreen(new android.graphics.Rect(0,0,note.getWidth(),note.getHeight()),true);
        snapshot(dialog.getWindow().getDecorView(),"amoled-details-note");
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
