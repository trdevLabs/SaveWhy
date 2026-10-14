package com.savewhy.app;
import android.app.Dialog;
import android.content.*;
import android.content.res.Configuration;
import android.graphics.*;
import android.os.*;
import android.view.*;
import android.widget.*;
import androidx.appcompat.app.*;
import androidx.core.os.LocaleListCompat;
import com.google.android.material.chip.*;
import java.io.*;
import java.util.*;
import org.junit.*;
import org.junit.runner.RunWith;
import org.robolectric.*;
import org.robolectric.annotation.*;
import org.robolectric.android.controller.ActivityController;
import org.robolectric.shadows.ShadowDialog;
import static org.junit.Assert.*;
@RunWith(RobolectricTestRunner.class)
@Config(sdk=28,qualifiers="w411dp-h891dp-xhdpi")
@GraphicsMode(GraphicsMode.Mode.NATIVE)
public class UpdatedUiTest {
 private ActivityController<MainActivity> controller;
 @Before public void language(){AppCompatDelegate.setApplicationLocales(LocaleListCompat.forLanguageTags("tr"));}
 @After public void close(){if(controller!=null)controller.pause().stop().destroy();}
 private MainActivity launch(){controller=Robolectric.buildActivity(MainActivity.class).setup();return controller.get();}
 private Object call(MainActivity a,String name)throws Exception{java.lang.reflect.Method m=MainActivity.class.getDeclaredMethod(name);m.setAccessible(true);return m.invoke(a);}
 private ChipGroup chips(View v){if(v instanceof ChipGroup)return (ChipGroup)v;if(v instanceof ViewGroup)for(int i=0;i<((ViewGroup)v).getChildCount();i++){ChipGroup c=chips(((ViewGroup)v).getChildAt(i));if(c!=null)return c;}return null;}
 private boolean has(View v,String text){if(v instanceof TextView&&((TextView)v).getText().toString().equals(text))return true;if(v instanceof ViewGroup)for(int i=0;i<((ViewGroup)v).getChildCount();i++)if(has(((ViewGroup)v).getChildAt(i),text))return true;return false;}
 private void waitUi(java.util.function.BooleanSupplier done)throws Exception{long end=System.currentTimeMillis()+5000;while(!done.getAsBoolean()&&System.currentTimeMillis()<end){Thread.sleep(20);Shadows.shadowOf(Looper.getMainLooper()).idle();}assertTrue(done.getAsBoolean());}
 private void snapshot(MainActivity a,String name)throws Exception{View v=a.getWindow().getDecorView();int w=v.getResources().getDisplayMetrics().widthPixels,h=v.getResources().getDisplayMetrics().heightPixels;v.measure(View.MeasureSpec.makeMeasureSpec(w,View.MeasureSpec.EXACTLY),View.MeasureSpec.makeMeasureSpec(h,View.MeasureSpec.EXACTLY));v.layout(0,0,w,h);Bitmap b=Bitmap.createBitmap(w,h,Bitmap.Config.ARGB_8888);v.draw(new Canvas(b));File d=new File("build/reports/ui");d.mkdirs();try(OutputStream out=new FileOutputStream(new File(d,name+".png"))){b.compress(Bitmap.CompressFormat.PNG,100,out);}finally{b.recycle();}}
 @Test public void firstSixFiltersAreOrderedAndAllHaveIcons()throws Exception{
  MainActivity a=launch();ChipGroup row=chips(a.getWindow().getDecorView());String[] expected={"Sosyal","Ekran görüntüleri","Videolar","Bekleyen","Unutulanlar","Tümü"};
  for(int i=0;i<expected.length;i++){Chip c=(Chip)row.getChildAt(i);assertEquals(expected[i],c.getTag());assertNotNull(c.getChipIcon());assertTrue(c.isChipIconVisible());}
  Chip social=(Chip)row.getChildAt(0);social.performClick();java.lang.reflect.Field filter=MainActivity.class.getDeclaredField("filter");filter.setAccessible(true);assertEquals("Sosyal",filter.get(a));
  ((Chip)row.getChildAt(2)).performClick();assertEquals("Videolar",filter.get(a));assertNotNull(((Chip)row.getChildAt(2)).getChipIcon());
 }
 @Test public void emptyHomeUsesApprovedCopyInLightTheme()throws Exception{
  RuntimeEnvironment.getApplication().getSharedPreferences("settings",0).edit().putInt("theme",AppCompatDelegate.MODE_NIGHT_NO).commit();MainActivity a=launch();waitUi(()->has(a.getWindow().getDecorView(),"Henüz bir şey kaydetmedin"));assertFalse(has(a.getWindow().getDecorView(),"İyi bir şey gördün mü?"));assertTrue(has(a.getWindow().getDecorView(),"Bir bağlantıyı, fotoğrafı veya videoyu Paylaş menüsünden tosave’a gönder. Neden kaydettiğini seç, daha sonra kolayca bul."));snapshot(a,"home-light-v015");
 }
 @Test public void localizedEmptyCopyAndPurposeLabelWorkInEveryLanguage(){
  String[][] titles={{"tr","Henüz bir şey kaydetmedin"},{"en","You haven't saved anything yet"},{"de","Noch nichts gespeichert"},{"es","Todavía no has guardado nada"},{"fr","Vous n'avez encore rien enregistré"},{"pt","Você ainda não salvou nada"},{"id","Belum ada yang Anda simpan"},{"zh-Hans","你还没有保存任何内容"},{"ar","لم تحفظ أي شيء بعد"},{"hi","आपने अभी कुछ सहेजा नहीं है"},{"bn","এখনও কিছু সংরক্ষণ করেননি"},{"ur","آپ نے ابھی کچھ محفوظ نہیں کیا"}};
  for(String[] row:titles){Context base=RuntimeEnvironment.getApplication();Configuration c=new Configuration(base.getResources().getConfiguration());c.setLocale(Locale.forLanguageTag(row[0]));Context context=base.createConfigurationContext(c);assertEquals(row[0],row[1],I18n.text(context,"İyi bir şey gördün mü?"));String help=I18n.text(context,"Instagram, TikTok veya YouTube'da paylaş menüsünden tosave'ı seç. Niyetine dokun, kaydet.");assertTrue(row[0],help.contains("tosave"));assertFalse(row[0],help.contains("Instagram"));assertFalse(row[0],context.getString(R.string.about_purpose).contains("niyet"));}
  assertEquals(13,LanguageOptions.CODES.length);assertEquals("Deutsch",LanguageOptions.name("de-AT","System"));
 }
 @Test public void aboutDescribesSharedContentAndPreservesDeveloperSupport()throws Exception{
  MainActivity a=launch();call(a,"about");assertTrue(((TextView)a.findViewById(R.id.about_purpose)).getText().toString().startsWith("tosave, paylaştığınız bağlantıları"));assertEquals("Geliştirici · tr.developer",((TextView)a.findViewById(R.id.about_developer)).getText().toString());assertNotNull(a.findViewById(R.id.about_icon));String contribution=((TextView)a.findViewById(R.id.about_contribution)).getText().toString();assertTrue(contribution.contains("SMA"));assertTrue(contribution.contains("fidan"));a.findViewById(R.id.about_support).performClick();assertEquals(Intent.ACTION_SENDTO,Shadows.shadowOf(a).getNextStartedActivity().getAction());a.getOnBackPressedDispatcher().onBackPressed();assertNotNull(a.findViewById(R.id.settings_screen));
 }
 @Test public void aboutIsRestoredOnRecreation()throws Exception{MainActivity a=launch();call(a,"about");controller.recreate();assertNotNull(controller.get().findViewById(R.id.about_support));}
 @Test public void inlineOcrRescanUpdatesOpenCardAndKeepsTurkishCharacters()throws Exception{
  MainActivity a=launch();Bitmap b=Bitmap.createBitmap(600,800,Bitmap.Config.ARGB_8888);b.eraseColor(Color.WHITE);ByteArrayOutputStream out=new ByteArrayOutputStream();b.compress(Bitmap.CompressFormat.PNG,100,out);b.recycle();String imageId=new ImageStore(a).ingest(new ByteArrayInputStream(out.toByteArray()));
  Card card=new Card("ocr-inline","","Fotoğraf","Al","Sonra",1,false,Collections.emptyList(),"",imageId,Collections.emptyList(),"ılık ışık","ready");CardStore store=new CardStore(a.getFilesDir());store.load();store.save(card);controller.pause().resume();java.lang.reflect.Method detail=MainActivity.class.getDeclaredMethod("detail",String.class);detail.setAccessible(true);detail.invoke(a,card.id);Dialog dialog=ShadowDialog.getLatestDialog();TextView text=dialog.findViewById(R.id.detail_ocr_text);assertEquals("ılık ışık",text.getText().toString());
  java.lang.reflect.Field ocr=MainActivity.class.getDeclaredField("imageOcr");ocr.setAccessible(true);ocr.set(a,new ImageOcr(){void read(Bitmap bitmap,java.util.function.BiConsumer<String,Boolean> result){bitmap.recycle();result.accept("sıcaklık, çalışma, çığ",true);}});dialog.findViewById(R.id.detail_ocr_retry).performClick();waitUi(()->text.getText().toString().equals("sıcaklık, çalışma, çığ"));store.load();assertEquals("sıcaklık, çalışma, çığ",store.find(card.id).recognizedText);
 }
 @Test public void choosingLanguageCanStillReturnToSystem()throws Exception{MainActivity a=launch();call(a,"chooseLanguage");AlertDialog dialog=(AlertDialog)ShadowDialog.getLatestDialog();ListView list=dialog.getListView();assertEquals(13,list.getAdapter().getCount());int de=LanguageOptions.index("de");list.performItemClick(null,de,list.getAdapter().getItemId(de));assertEquals("de",a.getSharedPreferences("settings",0).getString("language",""));call(a,"chooseLanguage");dialog=(AlertDialog)ShadowDialog.getLatestDialog();list=dialog.getListView();list.performItemClick(null,0,list.getAdapter().getItemId(0));assertTrue(AppCompatDelegate.getApplicationLocales().isEmpty());}
}
