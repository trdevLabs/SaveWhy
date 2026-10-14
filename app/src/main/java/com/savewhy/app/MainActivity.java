package com.savewhy.app;

import android.content.*;
import android.content.res.ColorStateList;
import android.graphics.Typeface;
import android.net.Uri;
import android.os.*;
import android.text.*;
import android.view.*;
import android.widget.*;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.core.content.ContextCompat;
import androidx.core.content.res.ResourcesCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.*;
import androidx.recyclerview.widget.*;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.card.MaterialCardView;
import com.google.android.material.checkbox.MaterialCheckBox;
import com.google.android.material.chip.*;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.floatingactionbutton.ExtendedFloatingActionButton;
import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.textfield.*;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.concurrent.*;

public class MainActivity extends AppCompatActivity {
    private static final int EXPORT=41, IMPORT=42, THUMBNAIL=43, IMAGE=44, VIDEO=45;
    private CardStore store;
    private FrameLayout root;
    private RecyclerView recycler;
    private CardAdapter adapter;
    private LinearLayout empty;
    private LinearLayout filterRegistry;
    private TextView status,usageLabel;private com.google.android.material.progressindicator.LinearProgressIndicator usageProgress;private LinearLayout usageBox;
    private Choices choices; private PurchaseManager purchases;
    private Bundle imagePickDraft;private LocalMedia localMedia;private VideoStore videos;private ImageStore images;private ImageOcr imageOcr;private volatile boolean destroyed;private final Set<String> ocrActive=new HashSet<>();
    private ThumbnailLoader thumbnails; private String thumbnailCardId; private BottomSheetDialog detailDialog;private String detailCardId;private TextView ocrStatus,ocrContent;private MaterialButton ocrRetryButton;
    private WindowInsetsCompat lastWindowInsets;
    private Typeface regular, bold;
    private String query="", filter="Bekleyen";
    private CaptureUi capture;
    private final Handler handler=new Handler(Looper.getMainLooper());
    private final ExecutorService worker=Executors.newSingleThreadExecutor();private final ExecutorService mediaWorker=Executors.newFixedThreadPool(2);private final Set<AutoCloseable> mediaResources=ConcurrentHashMap.newKeySet();
    private int generation;
    private Runnable searchTask;
    private String page="home"; private MaterialButton bell;private TextView badge;private int lastUnread=-1;
    private final Runnable inboxTick=new Runnable(){public void run(){refreshNotifications();if(page.equals("notifications")&&lastUnread>0)notifications();handler.postDelayed(this,30000);}};


    @Override public void onCreate(Bundle state) {
        int mode=getSharedPreferences("settings",MODE_PRIVATE).getInt("theme",AppCompatDelegate.MODE_NIGHT_YES);
        AppCompatDelegate.setDefaultNightMode(mode);
        setTheme(Palettes.STYLES[Palettes.index(this)]);
        super.onCreate(state);
        regular=LanguageOptions.font(this); bold=Typeface.create(regular,Typeface.BOLD);
        choices=new Choices(this);localMedia=new LocalMedia(this);thumbnails=new ThumbnailLoader(this);images=new ImageStore(this);videos=new VideoStore(this,images);imageOcr=new ImageOcr(this);
        purchases=new PurchaseManager(this);purchases.changed=()->{updateUsage();if(page.equals("purchase"))updatePaywall();};
        store=new CardStore(getFilesDir(),()->purchases.premium());
        try { store.load(); } catch(Exception e) { toast("Kayıtlar okunamadı. Mevcut dosya korunuyor."); }
        if(state!=null) { query=state.getString("query",""); filter=state.getString("filter","Bekleyen"); }
        if(store.isHealthy()){Set<String> retained=new HashSet<>();Set<String> retainedVideos=new HashSet<>();for(Card card:store.all()){if(card.hasVisual())retained.add(card.imageId);if(card.isVideo())retainedVideos.add(card.videoId());}if(state!=null)for(String key:new String[]{"draft","purchaseDraft","imagePickDraft"}){Bundle draft=state.getBundle(key);if(draft!=null){retained.add(draft.getString("imageId",""));retainedVideos.add(draft.getString("videoId",""));}}mediaWorker.execute(()->{images.cleanup(retained,System.currentTimeMillis());videos.cleanup(retainedVideos,System.currentTimeMillis());});}
        Reminders.restore(this,store.all());
        getOnBackPressedDispatcher().addCallback(this,new androidx.activity.OnBackPressedCallback(true){public void handleOnBackPressed(){if(page.equals("about"))settings();else if(!page.equals("home"))home();else{setEnabled(false);getOnBackPressedDispatcher().onBackPressed();setEnabled(true);}}});
        home();
        if(state!=null&&"settings".equals(state.getString("page")))settings();else if(state!=null&&"about".equals(state.getString("page")))about();else if(state!=null&&"notifications".equals(state.getString("page")))notifications();else if(state!=null&&"purchase".equals(state.getString("page")))purchaseScreen(state.getBundle("purchaseDraft"));
        String notificationId=getIntent().getStringExtra("cardId"); if(notificationId!=null) detail(notificationId);
        if(state!=null){thumbnailCardId=state.getString("thumbnailCardId");imagePickDraft=state.getBundle("imagePickDraft");}
        if(state==null&&Intent.ACTION_MAIN.equals(getIntent().getAction())&&getIntent().hasCategory(Intent.CATEGORY_LAUNCHER))startActivity(new Intent(this,SplashActivity.class).putExtra("overlay",true));
        if(state!=null && state.containsKey("draft")) showCapture(state.getBundle("draft")); else if(notificationId==null) receive(getIntent());
    }
    @Override protected void onSaveInstanceState(Bundle state) {
        super.onSaveInstanceState(state);if(imagePickDraft!=null)state.putBundle("imagePickDraft",imagePickDraft);state.putString("thumbnailCardId",thumbnailCardId); state.putString("query",query); state.putString("filter",filter);state.putString("page",page);if(purchaseDraft!=null)state.putBundle("purchaseDraft",purchaseDraft);
        if(capture!=null && capture.dialog.isShowing()) state.putBundle("draft",capture.bundle());
    }
    @Override protected void onResume(){super.onResume();if(store!=null){try{store.load();}catch(Exception e){error(e);}refreshNotifications();if(page.equals("home"))render();else if(page.equals("settings"))settings();}if(purchases!=null)purchases.refresh();if(store!=null)for(Card card:store.all())if(card.isImage()&&card.ocrState.equals("pending"))scanImage(card.id);handler.removeCallbacks(inboxTick);handler.postDelayed(inboxTick,30000);}
    @Override protected void onPause(){handler.removeCallbacks(inboxTick);if(bellAnimation!=null)bellAnimation.cancel();if(bell!=null){bell.animate().cancel();bell.setRotation(0);}super.onPause();}
    @Override protected void onDestroy() {
        destroyed=true;if(imageOcr!=null)imageOcr.close();handler.removeCallbacksAndMessages(null);if(thumbnails!=null)thumbnails.close();if(purchases!=null)purchases.close(); worker.shutdownNow();mediaWorker.shutdownNow();for(AutoCloseable resource:mediaResources)closeMedia(resource); super.onDestroy();
    }
    @Override protected void onNewIntent(Intent intent) { super.onNewIntent(intent); setIntent(intent); String id=intent.getStringExtra("cardId"); if(id!=null) detail(id); else receive(intent); }
    private void receive(Intent intent) {
        if(!Intent.ACTION_SEND.equals(intent.getAction())) return;
        if(intent.getType()!=null&&(intent.getType().startsWith("image/")||intent.getType().startsWith("video/"))){
            boolean video=intent.getType().startsWith("video/");Uri image=intent.getParcelableExtra(Intent.EXTRA_STREAM);if(image==null&&intent.getClipData()!=null&&intent.getClipData().getItemCount()>0)image=intent.getClipData().getItemAt(0).getUri();intent.setAction(Intent.ACTION_MAIN);if(image==null){toast("Görsel okunamadı. Başka bir görsel seç.");return;}acceptMedia(image,video);return;
        }
        CharSequence raw=intent.getCharSequenceExtra(Intent.EXTRA_TEXT);
        if(raw==null && intent.getClipData()!=null && intent.getClipData().getItemCount()>0)
            raw=intent.getClipData().getItemAt(0).coerceToText(this);
        String url=UrlTools.extract(raw==null?"":raw.toString());
        CharSequence subject=intent.getCharSequenceExtra(Intent.EXTRA_SUBJECT);
        if(subject==null) subject=intent.getCharSequenceExtra(Intent.EXTRA_TITLE);
        intent.setAction(Intent.ACTION_MAIN);
        if(url.isEmpty()) { toast("Bu paylaşımda geçerli bir bağlantı yok."); return; }
        Bundle b=new Bundle(); b.putString("url",url); b.putString("sharedTitle",QuickCapture.sharedTitle(raw==null?"":raw.toString(),subject==null?"":subject.toString()));
        if(capture!=null && capture.dialog.isShowing()) {
            new MaterialAlertDialogBuilder(this).setTitle(L("Yeni paylaşımı aç?")).setMessage(L("Açık taslak kapanacak."))
                .setNegativeButton(L("Taslakta kal"),null).setPositiveButton(L("Yeni paylaşımı aç"),(d,w)-> {capture.dialog.dismiss(); showCapture(b);}).show();
        } else showCapture(b);
    }
    private void pickImage(){chooseExistingMedia(false,capture!=null&&capture.dialog.isShowing()?capture.bundle():new Bundle(),null);}
    private void pickVideo(){chooseExistingMedia(true,capture!=null&&capture.dialog.isShowing()?capture.bundle():new Bundle(),null);}
    private void chooseExistingMedia(boolean video,Bundle draft,Uri hint){imagePickDraft=new Bundle(draft);Intent picker=new Intent(Intent.ACTION_OPEN_DOCUMENT).addCategory(Intent.CATEGORY_OPENABLE).setType(video?"video/*":"image/*").addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION|Intent.FLAG_GRANT_PERSISTABLE_URI_PERMISSION);if(hint!=null)picker.putExtra(android.provider.DocumentsContract.EXTRA_INITIAL_URI,hint);try{startActivityForResult(picker,video?VIDEO:IMAGE);}catch(ActivityNotFoundException e){imagePickDraft=null;toast("Dosya seçici bulunamadı.");}}
    private void relink(Card card){Bundle draft=editBundle(card);draft.putString("relinkId",card.id);chooseExistingMedia(card.isVideo(),draft,card.isReference()?Uri.parse(card.mediaUri()):null);}
    private void acceptImage(Uri uri){acceptMedia(uri,false);}
    private void acceptMedia(Uri uri,boolean video){
        if(!"content".equalsIgnoreCase(uri.getScheme())){toast("Dosyaya erişilemiyor. Dosyayı yeniden bağla.");return;}
        final Bundle draft=imagePickDraft==null?new Bundle():new Bundle(imagePickDraft);final boolean fromPicker=imagePickDraft!=null;imagePickDraft=null;if(!draft.containsKey("relinkId"))draft.remove("id");draft.remove("url");draft.remove("sharedTitle");
        final boolean persistent=localMedia.persist(uri);
        if(!persistent&&fromPicker){toast("Kalıcı dosya erişimi alınamadı. Başka bir dosya kaynağı seç.");return;}
        mediaWorker.execute(()->{try{
            if(!localMedia.accessible(uri))throw new IOException("Missing document");
            String name=localMedia.name(uri);
            if(persistent){String id=LocalMedia.identity(uri);draft.putString("imageId",id);draft.putString("videoId",video?id:"");draft.putString("mediaUri",uri.toString());draft.putString("mediaName",name);}
            else {
                try(InputStream input=getContentResolver().openInputStream(uri)){
                    if(video){VideoStore.Clip clip=videos.ingest(input);draft.putString("imageId",clip.poster);draft.putString("videoId",clip.id);}
                    else{draft.putString("imageId",images.ingest(input));draft.putString("videoId","");}
                }
                draft.putString("mediaUri","");draft.putString("mediaName","");
                if(draft.getString("title","").isEmpty())draft.putString("title",name);
            }
            handler.post(()->{if(destroyed)return;Runnable next=()->{if(capture!=null)capture.dialog.dismiss();showCapture(draft);};if(!fromPicker&&capture!=null&&capture.dialog.isShowing())new MaterialAlertDialogBuilder(this).setTitle(L("Yeni paylaşımı aç?")).setMessage(L("Açık taslak kapanacak.")).setNegativeButton(L("Taslakta kal"),null).setPositiveButton(L("Yeni paylaşımı aç"),(d,w)->next.run()).show();else next.run();});
        }catch(Exception e){handler.post(()->{if(!destroyed)toast("Paylaşılan dosya okunamadı veya dosya sınırını aşıyor.");});}});
    }

    private void bindThumbnail(ImageView view,Card card){if(card.isReference())localMedia.bind(view,card.mediaUri(),card.isVideo(),mediaWorker,handler);else if(card.hasVisual())images.bind(view,card.imageId,mediaWorker,handler);else thumbnails.bind(view,card.url,sourceIcon(card.url));}
    private void openVideo(Card card){if(card.isReference())openReferenceVideo(card.mediaUri());else openVideo(card.videoId());}
    private void openReferenceVideo(String value){try{Uri uri=Uri.parse(value);Intent open=new Intent(Intent.ACTION_VIEW).setDataAndType(uri,"video/*").addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);open.setClipData(ClipData.newRawUri("tosave video",uri));startActivity(open);}catch(Exception e){toast("Dosyaya erişilemiyor. Dosyayı yeniden bağla.");}}
    private void openVideo(String id){try{Uri uri=androidx.core.content.FileProvider.getUriForFile(this,getPackageName()+".videos",videos.file(id));Intent open=new Intent(Intent.ACTION_VIEW).setDataAndType(uri,"video/*").addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);open.setClipData(ClipData.newRawUri("tosave video",uri));startActivity(open);}catch(Exception e){toast("Video açılamadı. Cihazında bir video oynatıcı bulunmalı.");}}
    private void viewImage(String imageId){viewImage(imageId,"");}
    private void viewImage(String imageId,String uri){
        android.app.Dialog dialog=new android.app.Dialog(this,R.style.AppTheme);FrameLayout frame=new FrameLayout(this);frame.setBackgroundColor(android.graphics.Color.BLACK);ImageZoomView image=new ImageZoomView(this);image.setId(R.id.image_viewer);image.setContentDescription(L("Görseli büyüt"));frame.addView(image,new FrameLayout.LayoutParams(-1,-1));MaterialButton close=iconButton("Kapat",R.drawable.ic_close,dialog::dismiss);FrameLayout.LayoutParams closeParams=new FrameLayout.LayoutParams(dp(48),dp(48),Gravity.TOP|Gravity.END);frame.addView(close,closeParams);dialog.setContentView(frame);dialog.show();if(dialog.getWindow()!=null){dialog.getWindow().setLayout(-1,-1);dialog.getWindow().setStatusBarColor(android.graphics.Color.BLACK);dialog.getWindow().setNavigationBarColor(android.graphics.Color.BLACK);}
        ViewCompat.setOnApplyWindowInsetsListener(frame,(v,insets)->{Insets bars=insets.getInsets(WindowInsetsCompat.Type.systemBars()|WindowInsetsCompat.Type.displayCutout());v.setPadding(bars.left,bars.top,bars.right,bars.bottom);return insets;});frame.post(()->ViewCompat.requestApplyInsets(frame));mediaWorker.execute(()->{try{android.graphics.Bitmap bitmap=uri.isEmpty()?images.bitmap(imageId,2560):localMedia.bitmap(Uri.parse(uri),false,2560);handler.post(()->{if(!destroyed&&dialog.isShowing())image.image(bitmap);else if(bitmap!=null)bitmap.recycle();});}catch(Exception e){handler.post(()->{dialog.dismiss();if(!destroyed)toast("Görsel okunamadı. Başka bir görsel seç.");});}});
    }
    private void scanImage(String id){
        if(destroyed||ocrActive.contains(id))return;Card card=store.find(id);if(card==null||!card.isImage())return;ocrActive.add(id);mediaWorker.execute(()->{try{android.graphics.Bitmap bitmap=card.isReference()?localMedia.bitmap(Uri.parse(card.mediaUri()),false,2560):images.bitmap(card.imageId,2560);if(bitmap==null)throw new IOException("Missing image");handler.post(()->{if(destroyed){bitmap.recycle();return;}imageOcr.read(bitmap,(text,success)->finishScan(id,text,success));});}catch(Exception e){handler.post(()->finishScan(id,"",false));}});
    }
    private void finishScan(String id,String text,boolean success){ocrActive.remove(id);if(destroyed)return;try{store.load();Card current=store.find(id);if(current==null||!current.isImage())return;Card updated=current.copy();if(success)updated.recognizedText=text;updated.ocrState=success?"ready":"failed";store.save(updated,false);render();updateOcrDetail(updated);}catch(Exception e){error(e);}}
    private int dp(int n) { return Math.round(n*getResources().getDisplayMetrics().density); }
    private int color(int id) { return Palettes.color(this,id); }
    private LinearLayout column() { LinearLayout l=new LinearLayout(this); l.setOrientation(LinearLayout.VERTICAL); return l; }
    private LinearLayout row() { LinearLayout l=new LinearLayout(this); l.setGravity(Gravity.CENTER_VERTICAL); return l; }
    private TextView text(String value,int size,boolean strong) {
        TextView t=new TextView(this); t.setText(L(value)); t.setTextSize(size); t.setTypeface(strong?bold:regular);
        t.setTextColor(color(strong?R.color.app_text:R.color.app_muted)); t.setLineSpacing(dp(2),1); t.setPadding(0,dp(4),0,dp(4)); return t;
    }
    private TextView rawText(String value,int size,boolean strong) { TextView t=text("",size,strong); t.setText(value); return t; }
    private void gap(LinearLayout layout,int height) { View v=new View(this); layout.addView(v,new LinearLayout.LayoutParams(1,dp(height))); }
    private LinearLayout.LayoutParams full() { return new LinearLayout.LayoutParams(-1,-2); }
    private MaterialCardView surface() {
        MaterialCardView c=new MaterialCardView(this); c.setCardBackgroundColor(color(R.color.app_surface));
        c.setCardElevation(0); c.setRadius(dp(22)); c.setStrokeWidth(dp(1)); c.setStrokeColor(color(R.color.app_border)); return c;
    }
    private int icon(String why) {
        switch(why) {
            case "Yap": return R.drawable.ic_do; case "Git": return R.drawable.ic_go;
            case "Al": return R.drawable.ic_buy; case "Öğren": return R.drawable.ic_learn;
            case "Üret": return R.drawable.ic_create; case "İzle": return R.drawable.ic_watch;
            case "Bul": return R.drawable.ic_find; default: return R.drawable.ic_bookmark;
        }
    }
    private ColorStateList selectionColors(int checked,int normal) {
        return new ColorStateList(new int[][]{new int[]{android.R.attr.state_checked},new int[]{}},new int[]{color(checked),color(normal)});
    }
    private MaterialButton button(String label,int icon,boolean primary,Runnable action) {
        MaterialButton b=new MaterialButton(this); b.setText(L(label)); b.setAllCaps(false); b.setTypeface(bold); b.setTextSize(13);
        b.setCornerRadius(dp(12)); b.setMinHeight(dp(48)); b.setMinimumHeight(dp(48)); b.setMinWidth(0); b.setMinimumWidth(0);
        b.setInsetTop(0); b.setInsetBottom(0); b.setStrokeWidth(0); b.setPadding(dp(12),dp(6),dp(12),dp(6));
        b.setBackgroundTintList(new ColorStateList(new int[][]{new int[]{-android.R.attr.state_enabled},new int[]{}},
                new int[]{color(R.color.app_surface_soft),primary?color(R.color.app_primary):android.graphics.Color.TRANSPARENT}));
        b.setTextColor(new ColorStateList(new int[][]{new int[]{-android.R.attr.state_enabled},new int[]{}},
                new int[]{color(R.color.app_muted),color(primary?R.color.app_on_primary:R.color.app_primary)}));
        if(icon!=0) { b.setIconResource(icon); b.setIconSize(dp(20)); b.setIconPadding(dp(10)); b.setIconTint(b.getTextColors()); }
        b.setGravity(primary?Gravity.CENTER:Gravity.START|Gravity.CENTER_VERTICAL);b.setIconGravity(MaterialButton.ICON_GRAVITY_TEXT_START);
        b.setOnClickListener(v->action.run()); return b;
    }
    private MaterialButton iconButton(String label,int icon,Runnable action) {
        MaterialButton b=button("",icon,false,action); b.setContentDescription(L(label)); b.setIconPadding(0);
        b.setPadding(0,0,0,0);b.setGravity(Gravity.CENTER); b.setIconGravity(MaterialButton.ICON_GRAVITY_TEXT_START); b.setCornerRadius(dp(24));
        b.setLayoutParams(new LinearLayout.LayoutParams(dp(48),dp(48))); return b;
    }
    private Chip chip(String name,int icon,boolean choice) {
        Chip c=new Chip(this); c.setChipDrawable(ChipDrawable.createFromAttributes(this,null,0,com.google.android.material.R.style.Widget_Material3_Chip_Filter)); c.setText(intentLabel(name)); c.setTag(name); c.setTypeface(regular);
        c.setTextSize(13); c.setCheckable(choice); c.setCheckedIconVisible(false); c.setEnsureMinTouchTargetSize(true);
        c.setChipMinHeight(dp(32)); c.setChipStartPadding(dp(8));c.setChipEndPadding(dp(8));c.setChipCornerRadius(dp(10)); c.setChipStrokeWidth(dp(1)); c.setChipStrokeColor(ColorStateList.valueOf(color(R.color.app_border)));
        c.setChipBackgroundColor(selectionColors(R.color.app_primary,R.color.app_surface));
        c.setTextColor(selectionColors(R.color.app_on_primary,R.color.app_muted));
        if(icon!=0) { c.setChipIconResource(icon); c.setChipIconSize(dp(16)); c.setChipIconTint(selectionColors(R.color.app_on_primary,R.color.app_primary)); c.setChipIconVisible(true); }
        return c;
    }
    private TextInputLayout input(LinearLayout parent,String label,String value,int max,int lines,int id) {
        TextInputLayout box=new TextInputLayout(this,null,com.google.android.material.R.attr.textInputOutlinedStyle);
        TextView caption=text(label,13,true); parent.addView(caption);
        caption.setLabelFor(id); box.setTag(caption); box.setHintEnabled(false); box.setBoxBackgroundMode(TextInputLayout.BOX_BACKGROUND_OUTLINE);
        box.setBoxCornerRadii(dp(14),dp(14),dp(14),dp(14));
        TextInputEditText e=new TextInputEditText(box.getContext()); e.setId(id); e.setText(value); e.setPadding(dp(16),dp(14),dp(16),dp(14)); e.setGravity(Gravity.TOP|Gravity.START); e.setTypeface(regular); e.setTextSize(15);
        e.setTextColor(color(R.color.app_text)); e.setMinLines(lines); e.setMaxLines(lines==1?1:5);
        e.setInputType(android.text.InputType.TYPE_CLASS_TEXT|(lines>1?android.text.InputType.TYPE_TEXT_FLAG_MULTI_LINE:0));
        e.setFilters(new InputFilter[]{new InputFilter.LengthFilter(max)}); box.addView(e,new LinearLayout.LayoutParams(-1,-2));
        LinearLayout.LayoutParams p=full(); p.setMargins(0,dp(4),0,dp(6)); parent.addView(box,p); return box;
    }
    private void inputVisibility(TextInputLayout box,int visibility) { box.setVisibility(visibility); ((View)box.getTag()).setVisibility(visibility); }
    private void bindWindowInsets(FrameLayout target) {
        ViewCompat.setOnApplyWindowInsetsListener(target,(v,insets)->{lastWindowInsets=insets;Insets system=insets.getInsets(WindowInsetsCompat.Type.systemBars()|WindowInsetsCompat.Type.displayCutout());v.setPadding(system.left,system.top,system.right,system.bottom);return insets;});
        WindowInsetsCompat cached=lastWindowInsets!=null?lastWindowInsets:ViewCompat.getRootWindowInsets(getWindow().getDecorView());
        if(cached!=null)ViewCompat.dispatchApplyWindowInsets(target,cached);
        else {int id=getResources().getIdentifier("status_bar_height","dimen","android");target.setPadding(0,id==0?dp(24):getResources().getDimensionPixelSize(id),0,0);}
        target.addOnAttachStateChangeListener(new View.OnAttachStateChangeListener(){public void onViewAttachedToWindow(View view){ViewCompat.requestApplyInsets(view);view.post(()->ViewCompat.requestApplyInsets(view));view.removeOnAttachStateChangeListener(this);}public void onViewDetachedFromWindow(View view){}});
    }
    private void home() {
        if(page.equals("purchase"))purchaseDraft=null;page="home";
        WindowCompat.setDecorFitsSystemWindows(getWindow(),false);
        boolean light=getResources().getBoolean(R.bool.light_system_bars);
        WindowInsetsControllerCompat bars=WindowCompat.getInsetsController(getWindow(),getWindow().getDecorView());
        bars.setAppearanceLightStatusBars(light); bars.setAppearanceLightNavigationBars(light);
        getWindow().setNavigationBarColor(Build.VERSION.SDK_INT<27&&light?color(R.color.app_text):color(R.color.app_background));
        root=new FrameLayout(this); root.setBackgroundColor(color(R.color.app_background)); bindWindowInsets(root);setContentView(root);
        LinearLayout page=column(); root.addView(page,new FrameLayout.LayoutParams(-1,-1));
        LinearLayout header=row(); header.setPadding(dp(16),dp(8),dp(16),dp(6));
        ImageView logo=new ImageView(this); logo.setImageResource(R.drawable.splash_art); logo.setScaleType(ImageView.ScaleType.FIT_CENTER); logo.setImportantForAccessibility(View.IMPORTANT_FOR_ACCESSIBILITY_NO);
        int logoSize=Math.round(24*getResources().getDisplayMetrics().scaledDensity); LinearLayout.LayoutParams logoParams=new LinearLayout.LayoutParams(logoSize,logoSize); logoParams.setMarginEnd(dp(8)); header.addView(logo,logoParams);
        header.addView(text("tosave",24,true),new LinearLayout.LayoutParams(0,-2,1));
        FrameLayout notifications=new FrameLayout(this); bell=iconButton("Bildirimler",R.drawable.ic_bell,this::notifications);bell.setId(R.id.main_notifications);notifications.addView(bell);
        badge=text("",10,true);badge.setTextColor(color(R.color.app_on_primary));android.graphics.drawable.GradientDrawable dot=new android.graphics.drawable.GradientDrawable();dot.setColor(color(R.color.app_primary));dot.setCornerRadius(dp(10));badge.setBackground(dot);badge.setGravity(Gravity.CENTER);badge.setPadding(dp(4),0,dp(4),0);badge.setImportantForAccessibility(View.IMPORTANT_FOR_ACCESSIBILITY_NO);
        FrameLayout.LayoutParams bp=new FrameLayout.LayoutParams(-2,dp(18),Gravity.TOP|Gravity.END);notifications.addView(badge,bp);header.addView(notifications,new LinearLayout.LayoutParams(dp(48),dp(48)));

        View spacer=new View(this); header.addView(spacer,new LinearLayout.LayoutParams(dp(8),1));
        MaterialButton settingsButton=iconButton("Ayarlar",R.drawable.ic_settings,this::settings);settingsButton.setId(R.id.main_settings);header.addView(settingsButton); page.addView(header);
        LinearLayout top=column(); top.setPadding(dp(16),0,dp(16),dp(4));
        TextInputLayout search=input(top,"Kayıtlarında ara",query,200,1,R.id.main_search);
        ((View)search.getTag()).setVisibility(View.GONE); search.getEditText().setHint(L("Kayıtlarında ara"));
        search.setStartIconDrawable(R.drawable.ic_find); search.setEndIconMode(TextInputLayout.END_ICON_CLEAR_TEXT);
        search.getEditText().addTextChangedListener(watcher(()-> {
            query=search.getEditText().getText().toString(); scheduleRender();
        })); page.addView(top);addUsage(page);
        // Each group is kept in its own horizontally scrollable row; selecting one clears the other.
        LinearLayout filterRows=column();
        for(int group=0;group<2;group++) {
            HorizontalScrollView scroll=new HorizontalScrollView(this);scroll.setHorizontalScrollBarEnabled(false);ChipGroup rowGroup=new ChipGroup(this);rowGroup.setSingleLine(true);rowGroup.setChipSpacingHorizontal(dp(4));rowGroup.setPadding(dp(16),0,dp(16),0);
            for(String name:homeFilters()) {
                boolean state=Arrays.asList("Bekleyen","Unutulanlar","Tümü","Tamamlanan","Görseller","Ekran görüntüleri","Videolar","Sosyal").contains(name);if(state!=(group==0))continue;
                Chip c=chip(name,state?filterIcon(name):icon(name),true);c.setTag(name);c.setChecked(filter.equals(name));c.setOnClickListener(v->{filter=name;render();});rowGroup.addView(c);
            }
            scroll.addView(rowGroup);filterRows.addView(scroll);
        }
        page.addView(filterRows); filterRegistry=filterRows;
        status=text("",11,false); status.setPadding(dp(22),0,dp(20),dp(6)); page.addView(status);
        FrameLayout content=new FrameLayout(this); page.addView(content,new LinearLayout.LayoutParams(-1,0,1));
        recycler=new RecyclerView(this); recycler.setId(R.id.main_list); recycler.setLayoutManager(new LinearLayoutManager(this));
        recycler.setClipToPadding(false); recycler.setPadding(dp(16),dp(2),dp(16),dp(80));
        adapter=new CardAdapter(); recycler.setAdapter(adapter); content.addView(recycler,new FrameLayout.LayoutParams(-1,-1));
        empty=column(); empty.setPadding(dp(32),dp(30),dp(32),dp(90)); empty.setGravity(Gravity.CENTER);
        content.addView(empty,new FrameLayout.LayoutParams(-1,-1));
        ExtendedFloatingActionButton add=new ExtendedFloatingActionButton(this); add.setId(R.id.main_add); add.setText(L("Yeni kayıt")); add.setTypeface(bold);
        add.setIconResource(R.drawable.ic_add); add.setIconTint(ColorStateList.valueOf(color(R.color.app_on_primary)));
        add.setBackgroundTintList(ColorStateList.valueOf(color(R.color.app_primary))); add.setTextColor(color(R.color.app_on_primary));
        add.setTextSize(13);add.setMinHeight(dp(48));add.setMinimumHeight(dp(48));add.setPadding(dp(14),0,dp(14),0);add.setOnClickListener(v->showCapture(new Bundle()));
        FrameLayout.LayoutParams fab=new FrameLayout.LayoutParams(-2,dp(48),Gravity.BOTTOM|Gravity.END); fab.setMargins(dp(20),0,dp(20),dp(20)); root.addView(add,fab);
        render();refreshNotifications();
    }
    private TextWatcher watcher(Runnable action) {
        return new TextWatcher() { public void beforeTextChanged(CharSequence s,int a,int b,int c) {} public void afterTextChanged(Editable e) { action.run(); } public void onTextChanged(CharSequence s,int a,int b,int c) {} };
    }
    private void scheduleRender() {
        if(searchTask!=null) handler.removeCallbacks(searchTask); searchTask=this::render; handler.postDelayed(searchTask,120);
    }
    private void render() {
        updateUsage();if(adapter==null || isDestroyed()) return;
        List<Card> all=store.all(); final int forgottenDays=getSharedPreferences("settings",MODE_PRIVATE).getInt("forgottenDays",15); final long now=System.currentTimeMillis();
        syncFilterChecks(filterRegistry);
        final String selected=filter, term=query; final int ticket=++generation;
        final Map<String,String> intentNames=new HashMap<>();for(Card c:all)intentNames.put(c.why,intentLabel(c.why));
        final List<Card> previous=new ArrayList<>(adapter.items);
        worker.execute(()-> {
            List<Card> visible=new ArrayList<>(); for(Card c:all) if(c.visible(selected,"",forgottenDays,now) && (c.matches(term)||Card.fold(intentNames.get(c.why)).contains(Card.fold(term)))) visible.add(c);
            DiffUtil.DiffResult changes=DiffUtil.calculateDiff(new DiffUtil.Callback() {
                public int getOldListSize() { return previous.size(); }
                public int getNewListSize() { return visible.size(); }
                public boolean areItemsTheSame(int a,int b) { return previous.get(a).id.equals(visible.get(b).id); }
                public boolean areContentsTheSame(int a,int b) {
                    Card old=previous.get(a), now=visible.get(b);
                    return old.title.equals(now.title)&&old.url.equals(now.url)&&old.why.equals(now.why)&&old.note.equals(now.note)
                        &&old.imageId.equals(now.imageId)&&old.mediaUri().equals(now.mediaUri())&&old.mediaName().equals(now.mediaName())&&old.videoId().equals(now.videoId())&&old.tags.equals(now.tags)&&old.recognizedText.equals(now.recognizedText)&&old.ocrState.equals(now.ocrState)
                        &&old.done==now.done&&old.steps.size()==now.steps.size()&&old.completedSteps()==now.completedSteps();
                }
            });
            runOnUiThread(()-> {
                if(isDestroyed() || ticket!=generation) return;
                adapter.items=visible; changes.dispatchUpdatesTo(adapter);
                status.setVisibility(store.isHealthy()?View.GONE:View.VISIBLE); status.setText(L("Kayıt dosyası okunamadı. Mevcut dosya korunuyor."));
                empty.setVisibility(visible.isEmpty()?View.VISIBLE:View.GONE); recycler.setVisibility(visible.isEmpty()?View.GONE:View.VISIBLE);
                if(visible.isEmpty()) emptyState(all.isEmpty());
            });
        });
    }
    private void emptyState(boolean first) {
        empty.removeAllViews(); ImageView image=new ImageView(this); image.setImageResource(first?R.drawable.ic_bookmark:R.drawable.ic_find);
        image.setImageTintList(ColorStateList.valueOf(color(R.color.app_primary))); empty.addView(image,new LinearLayout.LayoutParams(dp(48),dp(48))); gap(empty,18);
        TextView heading=text(first?"İyi bir şey gördün mü?":"Burada henüz kayıt yok.",24,true); heading.setGravity(Gravity.CENTER); empty.addView(heading);
        TextView help=text(first?"Instagram, TikTok veya YouTube'da paylaş menüsünden tosave'ı seç. Niyetine dokun, kaydet.":"Başka bir niyet seç veya arama sözcüğünü değiştir.",14,false);
        help.setGravity(Gravity.CENTER); empty.addView(help); gap(empty,18);

    }
    private int filterIcon(String name){switch(name){case "Sosyal":return R.drawable.ic_social;case "Ekran görüntüleri":case "Görseller":return R.drawable.ic_image;case "Videolar":return R.drawable.ic_watch;case "Bekleyen":return R.drawable.ic_pending;case "Unutulanlar":return R.drawable.ic_clock;case "Tümü":return R.drawable.ic_grid;case "Tamamlanan":return R.drawable.ic_check;default:return R.drawable.ic_bookmark;}}
    private int sourceIcon(String url) {
        switch(UrlTools.source(url)) {
            case "Instagram": return R.drawable.ic_social_instagram;
            case "YouTube": return R.drawable.ic_social_youtube;
            case "TikTok": return R.drawable.ic_social_tiktok;
            case "Facebook": return R.drawable.ic_social_facebook;
            case "X": return R.drawable.ic_social_x;
            case "Pinterest": return R.drawable.ic_social_pinterest;
            case "Reddit": return R.drawable.ic_social_reddit;
            case "WhatsApp": return R.drawable.ic_social_whatsapp;
            case "Telegram": return R.drawable.ic_social_telegram;
            case "Threads": return R.drawable.ic_social_threads;
            case "Twitch": return R.drawable.ic_social_twitch;
            case "LinkedIn": return R.drawable.ic_social_linkedin;
            default: return R.drawable.ic_link;
        }
    }
    private class CardAdapter extends RecyclerView.Adapter<CardHolder> {
        List<Card> items=new ArrayList<>();
        @Override public CardHolder onCreateViewHolder(ViewGroup parent,int type){return new CardHolder();}
        @Override public void onBindViewHolder(CardHolder h,int index){
            Card c=items.get(index);bindThumbnail(h.image,c);h.title.setText(c.title);h.meta.setText(intentLabel(c.why));h.note.setText(c.hasVisual()?L(c.isVideo()?"Video":"Görsel")+(c.tags.isEmpty()?"":" · "+String.join(" · ",c.tags)):UrlTools.source(c.url));
            h.progress.setVisibility(c.done?View.VISIBLE:View.GONE);h.progress.setText(L("Tamamlandı"));h.card.setOnClickListener(v->detail(c.id));h.card.setContentDescription(c.title+", "+intentLabel(c.why));
        }
        @Override public int getItemCount(){return items.size();}
    }
    private class CardHolder extends RecyclerView.ViewHolder {
        MaterialCardView card;ImageView image;TextView meta,title,note,progress;
        CardHolder(){super(surface());card=(MaterialCardView)itemView;card.setRadius(dp(14));RecyclerView.LayoutParams params=new RecyclerView.LayoutParams(-1,-2);params.setMargins(0,0,0,dp(6));card.setLayoutParams(params);card.setClickable(true);card.setFocusable(true);
            LinearLayout body=row();body.setPadding(dp(8),dp(8),dp(8),dp(8));card.addView(body);
            MaterialCardView cover=new MaterialCardView(MainActivity.this);cover.setRadius(dp(8));cover.setCardElevation(0);cover.setCardBackgroundColor(color(R.color.app_surface_soft));image=new ImageView(MainActivity.this);image.setScaleType(ImageView.ScaleType.CENTER_CROP);image.setImportantForAccessibility(View.IMPORTANT_FOR_ACCESSIBILITY_NO);cover.addView(image,new FrameLayout.LayoutParams(-1,-1));body.addView(cover,new LinearLayout.LayoutParams(dp(76),dp(56)));
            LinearLayout labels=column();LinearLayout.LayoutParams lp=new LinearLayout.LayoutParams(0,-2,1);lp.setMarginStart(dp(8));body.addView(labels,lp);
            title=text("",14,true);title.setMaxLines(2);title.setEllipsize(TextUtils.TruncateAt.END);labels.addView(title);
            LinearLayout bottom=row();meta=text("",10,true);meta.setTextColor(color(R.color.app_primary));meta.setMaxWidth(dp(110));meta.setMaxLines(1);meta.setEllipsize(TextUtils.TruncateAt.END);meta.setPadding(dp(6),dp(2),dp(6),dp(2));android.graphics.drawable.GradientDrawable badgeShape=new android.graphics.drawable.GradientDrawable();badgeShape.setColor(color(R.color.app_surface_soft));badgeShape.setCornerRadius(dp(6));meta.setBackground(badgeShape);bottom.addView(meta,new LinearLayout.LayoutParams(-2,-2));
            note=text("",10,false);note.setMaxLines(1);note.setEllipsize(TextUtils.TruncateAt.END);LinearLayout.LayoutParams np=new LinearLayout.LayoutParams(0,-2,1);np.setMarginStart(dp(6));bottom.addView(note,np);labels.addView(bottom);
            progress=text("",10,false);labels.addView(progress);
        }
    }
    private BottomSheetDialog sheet(LinearLayout body) {
        BottomSheetDialog dialog=new BottomSheetDialog(this); androidx.core.widget.NestedScrollView scroll=new androidx.core.widget.NestedScrollView(this);
        scroll.setFillViewport(false); scroll.addView(body); dialog.setContentView(scroll);
        dialog.getBehavior().setMaxHeight((int)(getResources().getDisplayMetrics().heightPixels*0.92));
        dialog.getBehavior().setSkipCollapsed(true); dialog.setOnShowListener(d->dialog.getBehavior().setState(com.google.android.material.bottomsheet.BottomSheetBehavior.STATE_EXPANDED));
        return dialog;
    }
    private BottomSheetDialog captureSheet(LinearLayout body,LinearLayout footer) {
        BottomSheetDialog dialog=new BottomSheetDialog(this);
        LinearLayout panel=column(); androidx.core.widget.NestedScrollView scroll=new androidx.core.widget.NestedScrollView(this); scroll.addView(body);
        panel.addView(scroll,new LinearLayout.LayoutParams(-1,0,1)); panel.addView(footer,full()); dialog.setContentView(panel);
        ViewGroup.LayoutParams size=panel.getLayoutParams(); size.height=Math.min(dp(680),(int)(getResources().getDisplayMetrics().heightPixels*0.9)); panel.setLayoutParams(size);
        dialog.getBehavior().setMaxHeight((int)(getResources().getDisplayMetrics().heightPixels*0.92)); dialog.getBehavior().setSkipCollapsed(true);
        ViewCompat.setOnApplyWindowInsetsListener(panel,(v,insets)-> {
            Insets bars=insets.getInsets(WindowInsetsCompat.Type.systemBars()), keyboard=insets.getInsets(WindowInsetsCompat.Type.ime());
            int available=getResources().getDisplayMetrics().heightPixels-bars.top-Math.max(bars.bottom,keyboard.bottom);
            dialog.getBehavior().setMaxHeight(Math.max(dp(200),(int)(available*0.95))); return insets;
        });
        dialog.setOnShowListener(d->dialog.getBehavior().setState(com.google.android.material.bottomsheet.BottomSheetBehavior.STATE_EXPANDED)); return dialog;
    }
    private LinearLayout sheetBody() {
        LinearLayout body=column(); body.setPadding(dp(20),dp(16),dp(24),dp(28)); body.setFocusableInTouchMode(true);
        View handle=new View(this); android.graphics.drawable.GradientDrawable shape=new android.graphics.drawable.GradientDrawable();
        shape.setColor(color(R.color.app_border)); shape.setCornerRadius(dp(4)); handle.setBackground(shape);
        LinearLayout.LayoutParams hp=new LinearLayout.LayoutParams(dp(32),dp(4)); hp.gravity=Gravity.CENTER; hp.setMargins(0,0,0,dp(18)); body.addView(handle,hp); return body;
    }
    private void showCapture(Bundle draft) {
        if(draft.getString("id","").isEmpty()&&store.all().size()>=CardStore.FREE_LIMIT&&!purchases.premium()){purchaseScreen(draft);return;}
        if(!store.isHealthy()) { toast("Kayıt dosyası okunamadı. Yeni kayıt açılamıyor."); return; }
        capture=new CaptureUi(draft); capture.dialog.show(); capture.body.requestFocus();capture.readImage();
        if(capture.dialog.getWindow()!=null) capture.dialog.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE|WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);
    }
    private class CaptureUi {
        final BottomSheetDialog dialog; final LinearLayout body,details; final String id,sharedTitle,imageId,videoId,mediaUri,mediaName;
        final TextInputLayout urlBox,queryBox; final TextView noteHelp; final MaterialButton reminderButton; long reminderAt; final EditText url,title,note,steps,search,tags;
        final LinearLayout pasteActions;
        final ChipGroup intents,reasons; final MaterialButton save; MaterialButton examplesButton; final TextView source,purpose; final ImageView linkIcon;
        String why,reason,originalReason,originalWhy,originalQuickNote; boolean detailsOpen,keepQuickNote;
        String recognizedText="",ocrState="pending",savedCardId="";boolean draftReading;
        CaptureUi(Bundle b) {
            id=b.getString("id","");imageId=b.getString("imageId","");videoId=b.getString("videoId","");mediaUri=b.getString("mediaUri","");mediaName=b.getString("mediaName",""); sharedTitle=b.getString("sharedTitle",""); why=b.getString("why",""); reason=b.getString("reason",""); originalReason=b.getString("originalReason","");originalWhy=b.getString("originalWhy","");originalQuickNote=b.getString("originalQuickNote","");keepQuickNote=b.getBoolean("keepQuickNote",false);
            recognizedText=b.getString("recognizedText","");ocrState=b.getString("ocrState","pending");
            body=sheetBody(); LinearLayout titleRow=row(); titleRow.addView(text(id.isEmpty()?"Niyetini seç":"Kaydını düzenle",20,true),new LinearLayout.LayoutParams(0,-2,1));
            titleRow.addView(iconButton("Kapat",R.drawable.ic_close,()-> { if(capture!=null) capture.dialog.dismiss(); })); body.addView(titleRow);
            gap(body,4);
            source=text("",13,true); source.setMaxLines(1); source.setEllipsize(TextUtils.TruncateAt.END); LinearLayout sourceRow=row(); linkIcon=new ImageView(MainActivity.this); linkIcon.setImageResource(R.drawable.ic_link); linkIcon.setImageTintList(ColorStateList.valueOf(color(R.color.app_primary))); sourceRow.addView(linkIcon,new LinearLayout.LayoutParams(dp(18),dp(18)));
            LinearLayout.LayoutParams sp=new LinearLayout.LayoutParams(0,-2,1); sp.setMargins(dp(10),0,dp(8),0); sourceRow.addView(source,sp); body.addView(sourceRow);
            urlBox=input(body,"Bağlantı",b.getString("url",""),4096,1,R.id.capture_link); url=urlBox.getEditText();
            url.setInputType(android.text.InputType.TYPE_CLASS_TEXT|android.text.InputType.TYPE_TEXT_VARIATION_URI);
            inputVisibility(urlBox,url.getText().toString().isEmpty()?View.VISIBLE:View.GONE);
            pasteActions=row();
            MaterialButton paste=button("Panodan ekle",R.drawable.ic_clipboard,false,()->paste(url)); pasteActions.addView(paste,new LinearLayout.LayoutParams(0,-2,1));
            if(imageId.isEmpty())sourceRow.addView(iconButton("Bağlantıyı düzenle",R.drawable.ic_edit,()->inputVisibility(urlBox,View.VISIBLE))); body.addView(pasteActions);
            if(imageId.isEmpty()){LinearLayout mediaPick=row();mediaPick.addView(button("Görsel ekle",R.drawable.ic_image,false,MainActivity.this::pickImage),new LinearLayout.LayoutParams(0,-2,1));mediaPick.addView(button("Video ekle",R.drawable.ic_watch,false,MainActivity.this::pickVideo),new LinearLayout.LayoutParams(0,-2,1));body.addView(mediaPick);}
            else{inputVisibility(urlBox,View.GONE);ImageView preview=new ImageView(MainActivity.this);preview.setId(R.id.capture_image);preview.setContentDescription(L(videoId.isEmpty()?"Görseli büyüt":"Videoyu aç"));if(mediaUri.isEmpty())images.bind(preview,imageId,mediaWorker,handler);else localMedia.bind(preview,mediaUri,!videoId.isEmpty(),mediaWorker,handler);sourceRow.addView(preview,new LinearLayout.LayoutParams(dp(56),dp(56)));preview.setOnClickListener(v->{if(videoId.isEmpty())viewImage(imageId,mediaUri);else if(mediaUri.isEmpty())openVideo(videoId);else openReferenceVideo(mediaUri);});}
            if(!mediaUri.isEmpty())body.addView(text("Dosyaya bağlı · yeniden kopyalanmaz",12,false));else if(!imageId.isEmpty())body.addView(text("Uygulamada saklanır",12,false));gap(body,12);
            LinearLayout intentHeader=row(); intentHeader.addView(text("NE İÇİN?",11,false),new LinearLayout.LayoutParams(0,-2,1)); intentHeader.addView(iconButton("Niyetlerini düzenle",R.drawable.ic_edit,()->{Bundle draft=bundle();capture.dialog.dismiss();manageIntents(()->showCapture(draft));})); body.addView(intentHeader); intents=new ChipGroup(MainActivity.this); intents.setId(R.id.capture_intents); intents.setSingleSelection(true); intents.setSelectionRequired(true); intents.setChipSpacingHorizontal(dp(6));
            for(String name:captureIntents(why)) {
                Chip c=chip(name,icon(name),true); c.setId(View.generateViewId()); c.setTag(name); c.setChecked(name.equals(why));
                c.setOnClickListener(v-> { why=name; buildReasons(); guidance(); updateReminderLabel(); }); intents.addView(c);
            }
            body.addView(intents); purpose=text("Bir niyet seçerek başlayabilirsin.",13,false); purpose.setVisibility(View.GONE); body.addView(purpose); gap(body,4);
            LinearLayout tagArea=column();tagArea.setVisibility(imageId.isEmpty()?View.GONE:View.VISIBLE);LinearLayout tagHeader=row();tagHeader.addView(text("Etiketler",12,true),new LinearLayout.LayoutParams(0,-2,1));tagArea.addView(tagHeader);TextInputLayout tagBox=input(tagArea,"Etiket ekle · virgülle ayır",b.getString("tags",""),500,1,R.id.capture_tags);tags=tagBox.getEditText();inputVisibility(tagBox,View.GONE);tagHeader.addView(iconButton("Etiket ekle",R.drawable.ic_add,()->{inputVisibility(tagBox,View.VISIBLE);tags.requestFocus();}));
            ChipGroup tagChoices=new ChipGroup(MainActivity.this);tagChoices.setChipSpacingHorizontal(dp(4));LinkedHashSet<String> suggestions=new LinkedHashSet<>(Card.parseTags(tags.getText().toString()));for(Card card:store.all())suggestions.addAll(card.tags);for(String preset:new String[]{"Alışveriş","Tarif","İş","Fikir","Referans"})suggestions.add(L(preset));int count=0;for(String tag:suggestions){if(count++>=Math.max(8,Card.parseTags(tags.getText().toString()).size()))break;Chip chip=chip("",0,true);chip.setText(tag);chip.setChecked(Card.parseTags(tags.getText().toString()).contains(tag));chip.setOnClickListener(v->{try{List<String> selected=Card.parseTags(tags.getText().toString());if(chip.isChecked())selected.add(tag);else selected.removeIf(t->Card.fold(t).equals(Card.fold(tag)));tags.setText(String.join(", ",Card.parseTags(selected)));}catch(Exception e){chip.setChecked(false);error(e);}});tagChoices.addView(chip);}tagArea.addView(tagChoices);body.addView(tagArea);gap(body,8);
            LinearLayout reasonHeader=row(); reasonHeader.addView(text("HIZLI NEDEN",11,false),new LinearLayout.LayoutParams(0,-2,1)); reasonHeader.addView(iconButton("Nedenlerini düzenle",R.drawable.ic_edit,()->{if(why.isEmpty()){toast("Önce bir niyet seç.");return;} Bundle draft=bundle();capture.dialog.dismiss();manageReasons(why,()->showCapture(draft));})); body.addView(reasonHeader); reasons=new ChipGroup(MainActivity.this); reasons.setId(R.id.capture_reasons); reasons.setSingleSelection(true); reasons.setSelectionRequired(true); reasons.setChipSpacingHorizontal(dp(6)); body.addView(reasons); gap(body,12);
            reminderAt=b.getLong("reminderAt",0);
            reminderButton=button("Hatırlatıcı ekle",R.drawable.ic_clock,false,()->chooseReminder(reminderAt,value->{ reminderAt=value; updateReminderLabel(); }));
            reminderButton.setId(R.id.capture_reminder); body.addView(reminderButton); gap(body,12);
            details=column(); details.addView(text("Bu alanlar isteğe bağlı. Kaydetmek için doldurman gerekmez.",13,false)); title=input(details,"Başlık · isteğe bağlı",b.getString("title",""),160,1,R.id.capture_title).getEditText();
            note=input(details,"Özel not · isteğe bağlı",b.getString("note",""),2000,3,R.id.capture_note).getEditText();
            noteHelp=text("",12,false); details.addView(noteHelp);
            details.addView(text("Yapılacaklar listesi",17,true)); details.addView(text("Bu kayıtla ne yapmak istediğini küçük işlere böl. Tamamladıklarını kartta işaretleyebilirsin. İstersen boş bırak.",13,false));
            steps=input(details,"Yapılacaklar · her satıra bir iş",b.getString("steps",""),10000,3,R.id.capture_steps).getEditText();
            examplesButton=button("Örnek yapılacakları seç",R.drawable.ic_do,false,()-> {
                if(why.isEmpty()) { toast("Önce bir niyet seç."); return; }
                String[] examples=L(Card.template(why)).split("\n"); boolean[] selected=new boolean[examples.length];
                new MaterialAlertDialogBuilder(MainActivity.this).setTitle(L("Örnek yapılacakları seç"))
                    .setMultiChoiceItems(examples,selected,(d,index,checked)->selected[index]=checked)
                    .setNegativeButton(L("Vazgeç"),null).setPositiveButton(L("Ekle"),(d,w)-> {
                        LinkedHashSet<String> lines=new LinkedHashSet<>(); for(String line:steps.getText().toString().split("\n")) if(!line.trim().isEmpty()) lines.add(line.trim());
                        for(int i=0;i<examples.length;i++) if(selected[i]) lines.add(examples[i]); steps.setText(String.join("\n",lines));
                    }).show();
            }); examplesButton.setVisibility(Choices.custom(why)?View.GONE:View.VISIBLE); details.addView(examplesButton);
            queryBox=input(details,"Bul için arama sorgusu · isteğe bağlı",b.getString("searchQuery",""),300,1,R.id.capture_search_query); search=queryBox.getEditText();
            MaterialButton toggle=button("Not ve yapılacaklar · isteğe bağlı",R.drawable.ic_expand,false,()-> { detailsOpen=!detailsOpen; details.setVisibility(detailsOpen?View.VISIBLE:View.GONE); });
            toggle.setId(R.id.capture_details); body.addView(toggle); detailsOpen=b.getBoolean("detailsOpen",false); details.setVisibility(detailsOpen?View.VISIBLE:View.GONE); body.addView(details); gap(body,16);
            LinearLayout footer=column(); footer.setPadding(dp(20),dp(8),dp(20),dp(12)); footer.setBackgroundColor(color(R.color.app_surface));
            save=button(id.isEmpty()?"Kaydet":"Değişiklikleri kaydet",R.drawable.ic_check,true,this::save); save.setId(R.id.capture_save); footer.addView(save,full());

            dialog=captureSheet(body,footer); url.addTextChangedListener(watcher(this::guidance)); buildReasons(); guidance(); updateReminderLabel();
        }
        void updateReminderLabel() {
            reminderButton.setText(reminderAt==0?L("Hatırlatıcı ekle"):L("Hatırlatma")+" · "+java.text.DateFormat.getDateTimeInstance(java.text.DateFormat.SHORT,java.text.DateFormat.SHORT,getResources().getConfiguration().getLocales().get(0)).format(new Date(reminderAt)));
        }
        void readImage(){
            if(imageId.isEmpty()||!videoId.isEmpty()||!ocrState.equals("pending")||draftReading)return;
            Card existing=id.isEmpty()?null:store.find(id);
            if(existing!=null&&existing.imageId.equals(imageId)&&existing.mediaUri().equals(mediaUri)){recognizedText=existing.recognizedText;ocrState=existing.ocrState;return;}
            draftReading=true;
            mediaWorker.execute(()->{try{
                android.graphics.Bitmap bitmap=mediaUri.isEmpty()?images.bitmap(imageId,2560):localMedia.bitmap(Uri.parse(mediaUri),false,2560);
                if(bitmap==null)throw new IOException("Missing image");
                handler.post(()->{if(destroyed){bitmap.recycle();return;}imageOcr.read(bitmap,(value,success)->completeDraftScan(value,success));});
            }catch(Exception e){handler.post(()->completeDraftScan("",false));}});
        }
        void completeDraftScan(String value,boolean success){draftReading=false;recognizedText=value;ocrState=success?"ready":"failed";if(!savedCardId.isEmpty())finishScan(savedCardId,value,success);}
        void buildReasons() {
            reasons.removeAllViews(); if(why.isEmpty()) return;
            List<String> options=new ArrayList<>(); for(Choices.Option o:choices.reasons(why,true))options.add(o.key);
            if(!id.isEmpty()&&!reason.isEmpty()&&!options.contains(reason))for(Choices.Option o:choices.reasons(why,false))if(o.key.equals(reason))options.add(reason);
            if(!options.contains(reason))reason=options.isEmpty()?"":options.get(0);
            for(String value:options) {
                Chip c=chip("",0,true); c.setText(reasonText(why,value)); c.setTag(value); c.setId(View.generateViewId()); c.setChecked(value.equals(reason));
                c.setOnClickListener(v-> { reason=value; keepQuickNote=false; }); reasons.addView(c);
            }
        }
        void guidance() {
            String link=UrlTools.extract(url.getText().toString());
            linkIcon.setImageResource(!imageId.isEmpty()?R.drawable.ic_image:link.isEmpty()?R.drawable.ic_link:sourceIcon(link));
            pasteActions.setVisibility(link.isEmpty()&&imageId.isEmpty()?View.VISIBLE:View.GONE);
            updateReminderLabel();
            source.setText(!imageId.isEmpty()?L(videoId.isEmpty()?"Ekran görüntüsü":"Cihaz videosu"):link.isEmpty()?L("Bir bağlantı paylaş veya panodan ekle."):QuickCapture.title(link,sharedTitle));
            save.setEnabled((!imageId.isEmpty()||!link.isEmpty())&&!why.isEmpty());
            examplesButton.setVisibility(why.isEmpty()||Choices.custom(why)?View.GONE:View.VISIBLE);
            inputVisibility(queryBox,why.equals("Bul")?View.VISIBLE:View.GONE);
            if(!why.isEmpty()) {
                IntentCatalog.Spec spec=IntentCatalog.get(why); purpose.setText(L(spec.description)); title.setHint(null);
                note.setHint(null); noteHelp.setText(L(spec.noteHint)); save.setText(id.isEmpty()?intentLabel(why)+L(" olarak kaydet"):L("Değişiklikleri kaydet"));
            }
        }
        Bundle bundle() {
            Bundle b=new Bundle();b.putString("recognizedText",recognizedText);b.putString("ocrState",ocrState); b.putString("id",id);b.putString("imageId",imageId);b.putString("videoId",videoId);b.putString("mediaUri",mediaUri);b.putString("mediaName",mediaName);b.putString("tags",tags.getText().toString()); b.putString("sharedTitle",sharedTitle); b.putString("url",url.getText().toString());
            b.putString("why",why); b.putString("reason",reason); b.putString("title",title.getText().toString()); b.putString("note",note.getText().toString());
            b.putString("steps",steps.getText().toString()); b.putString("searchQuery",search.getText().toString()); b.putBoolean("detailsOpen",detailsOpen); b.putLong("reminderAt",reminderAt); b.putString("originalReason",originalReason);b.putString("originalWhy",originalWhy);b.putString("originalQuickNote",originalQuickNote);b.putBoolean("keepQuickNote",keepQuickNote);return b;
        }
        void save() {
            try {
                Card previous=id.isEmpty()?null:store.find(id);String quickReason=keepQuickNote&&reason.equals(originalReason)&&why.equals(originalWhy)?originalQuickNote:reason.isEmpty()?intentLabel(why):reasonText(why,reason);
                Card c;
                if(imageId.isEmpty())c=QuickCapture.create(url.getText().toString(),sharedTitle,why,quickReason,title.getText().toString(),note.getText().toString(),steps.getText().toString(),search.getText().toString(),previous);
                else {if(mediaUri.isEmpty()&&!images.exists(imageId))throw new IOException("Missing image");String caption=title.getText().toString().trim();if(caption.isEmpty()&&!mediaName.isEmpty())caption=mediaName;if(caption.isEmpty())caption=L(videoId.isEmpty()?"Ekran görüntüsü":"Cihaz videosu")+" · "+java.text.DateFormat.getDateTimeInstance(java.text.DateFormat.SHORT,java.text.DateFormat.SHORT).format(new Date());String description=note.getText().toString().trim();c=new Card(previous==null?UUID.randomUUID().toString():previous.id,"",caption,why,description.isEmpty()?quickReason:description,previous==null?System.currentTimeMillis():previous.createdAt,previous!=null&&previous.done,Card.parseSteps(steps.getText().toString(),previous==null?Collections.emptyList():previous.steps),search.getText().toString(),imageId,Card.parseTags(tags.getText().toString()),previous==null||!previous.imageId.equals(imageId)||!previous.mediaUri().equals(mediaUri)?recognizedText:previous.recognizedText,previous==null||!previous.imageId.equals(imageId)||!previous.mediaUri().equals(mediaUri)?(videoId.isEmpty()?ocrState:"none"):previous.ocrState);c.setVideo(videoId);c.setMediaReference(mediaUri,mediaName);if(mediaUri.isEmpty()&&!videoId.isEmpty()&&!videos.file(videoId).isFile())throw new IOException("Missing video");}
                if(reminderAt!=0 && reminderAt<=System.currentTimeMillis()) { toast("Gelecekte bir zaman seç."); return; }
                c.reminderAt=reminderAt; if(reminderAt>0)c.done=false; store.save(c); Reminders.sync(MainActivity.this,c); dialog.dismiss(); render();savedCardId=c.id;if(c.isImage()&&c.ocrState.equals("pending")&&!draftReading)scanImage(c.id);
                Snackbar.make(root,id.isEmpty()?L("Kaydedildi · ")+intentLabel(why):L("Kayıt güncellendi"),Snackbar.LENGTH_LONG).show();if(id.isEmpty())maybeFeedback();
            } catch(CardStore.LimitException e){Bundle draft=bundle();dialog.dismiss();purchaseScreen(draft);}catch(Exception e) { error(e); }
        }
    }
    private void paste(EditText target) {
        android.content.ClipboardManager clipboard=(android.content.ClipboardManager)getSystemService(CLIPBOARD_SERVICE);
        if(clipboard==null || !clipboard.hasPrimaryClip() || clipboard.getPrimaryClip()==null) { toast("Panoda bir bağlantı yok."); return; }
        CharSequence raw=clipboard.getPrimaryClip().getItemAt(0).coerceToText(this); String url=UrlTools.extract(raw==null?"":raw.toString());
        if(url.isEmpty()) { toast("Panodaki metinde geçerli bir bağlantı yok."); return; }
        target.setText(url);
    }
    private void editCard(String id) {
        Card c=store.find(id);if(c!=null)showCapture(editBundle(c));
    }
    private Bundle editBundle(Card c){Bundle b=new Bundle(); b.putString("id",c.id); b.putString("url",c.url); b.putString("why",c.why); b.putString("title",c.title);b.putString("imageId",c.imageId);b.putString("videoId",c.videoId());b.putString("mediaUri",c.mediaUri());b.putString("mediaName",c.mediaName());b.putString("tags",String.join(", ",c.tags));
        String preset=""; for(Choices.Option option:choices.reasons(c.why,false)) if(option.key.equals(c.note)||reasonText(c.why,option.key).equals(c.note))preset=option.key; boolean quick=!preset.isEmpty(); b.putString("reason",preset); b.putString("note",quick?"":c.note); b.putString("originalReason",preset);b.putString("originalWhy",c.why);b.putString("originalQuickNote",c.note);b.putBoolean("keepQuickNote",quick);
        b.putLong("reminderAt",c.reminderAt); b.putString("searchQuery",c.searchQuery); List<String> lines=new ArrayList<>(); for(Card.Step s:c.steps) lines.add(s.text); b.putString("steps",String.join("\n",lines)); return b;
    }
    private void detail(String id) {
        Card c=store.find(id); if(c==null) return;
        if(detailDialog!=null)detailDialog.dismiss();detailCardId=id;ocrStatus=null;ocrContent=null;ocrRetryButton=null;
        try { store.save(c); render(); } catch(Exception e){error(e);}
        LinearLayout body=sheetBody(); ImageView media=new ImageView(this);media.setId(R.id.detail_video);media.setContentDescription(L(c.hasVisual()?c.isVideo()?"Videoyu aç":"Görseli büyüt":"Orijinal içeriği aç"));bindThumbnail(media,c);int width=getResources().getDisplayMetrics().widthPixels-dp(48);body.addView(media,new LinearLayout.LayoutParams(-1,Math.min(dp(220),width*9/16)));media.setOnClickListener(v->{touchCard(id);if(c.isVideo())openVideo(c);else if(c.isImage())viewImage(c.imageId,c.mediaUri());else open(c.url);});gap(body,8);
        if(!c.hasVisual())body.addView(button("Küçük görsel seç",R.drawable.ic_edit,false,()->{thumbnailCardId=id;try{startActivityForResult(new Intent(Intent.ACTION_OPEN_DOCUMENT).addCategory(Intent.CATEGORY_OPENABLE).setType("image/*"),THUMBNAIL);}catch(ActivityNotFoundException e){toast("Dosya seçici bulunamadı.");}}));
        body.addView(text(intentLabel(c.why)+"  ·  "+(c.hasVisual()?L(c.isVideo()?"Video":"Görsel"):UrlTools.source(c.url)),11,false));body.addView(rawText(c.title,17,true));gap(body,6);
        if(c.hasVisual()){TextView binding=text(c.isReference()?"Dosyaya bağlı · yeniden kopyalanmaz":"Önceki sürümde saklanan kopya",12,false);body.addView(binding);if(c.isReference()){body.addView(rawText(c.mediaName(),12,false));mediaWorker.execute(()->{boolean accessible=localMedia.accessible(Uri.parse(c.mediaUri()));handler.post(()->{if(!destroyed&&!accessible){binding.setText(L("Dosyaya erişilemiyor. Dosyayı yeniden bağla."));binding.setTextColor(color(R.color.app_error));}});});}body.addView(button("Dosyayı yeniden bağla",R.drawable.ic_link,false,()->relink(c)));}
        body.addView(text("NİYETİM",11,false)); body.addView(rawText(c.note,13,false)); gap(body,8);
        if(c.hasVisual()){
            ChipGroup labels=new ChipGroup(this);for(String tag:c.tags){Chip chip=chip("",0,false);chip.setText(tag);chip.setOnClickListener(v->{detailDialog.dismiss();query=tag;filter=c.isVideo()?"Videolar":"Ekran görüntüleri";home();});labels.addView(chip);}body.addView(labels);
            if(c.isImage()){ocrStatus=text("",12,false);body.addView(ocrStatus);
            ocrContent=rawText(c.recognizedText,14,false);ocrContent.setId(R.id.detail_ocr_text);ocrContent.setTextIsSelectable(true);body.addView(ocrContent);
            ocrRetryButton=button("Metni yeniden tara",R.drawable.ic_find,false,()->{Card current=store.find(id);if(current==null)return;Card changed=current.copy();changed.ocrState="pending";try{store.save(changed,false);updateOcrDetail(changed);scanImage(id);}catch(Exception e){error(e);}});ocrRetryButton.setId(R.id.detail_ocr_retry);body.addView(ocrRetryButton);updateOcrDetail(c);}

        }
        TextView progress=text(c.steps.isEmpty()?"":c.completedSteps()+" / "+c.steps.size()+L(" adım tamamlandı"),12,false);progress.setVisibility(c.steps.isEmpty()?View.GONE:View.VISIBLE);body.addView(progress);
        for(int i=0;i<c.steps.size();i++) {
            final int index=i; MaterialCheckBox check=new MaterialCheckBox(this); check.setTypeface(regular); check.setText(c.steps.get(i).text); check.setChecked(c.steps.get(i).done); check.setMinHeight(dp(48)); body.addView(check);
            check.setOnClickListener(v-> {
                Card current=store.find(id); if(current==null) return; Card changed=current.copy(); changed.steps.get(index).done=check.isChecked();
                if(saveCard(changed)) progress.setText(changed.completedSteps()+" / "+changed.steps.size()+L(" adım tamamlandı")); else check.setChecked(current.steps.get(index).done);
            });
        }
        gap(body,12);
        TextView reminderStatus=text(c.reminderAt==0?"Hatırlatma yok":L("Hatırlatma")+" · "+java.text.DateFormat.getDateTimeInstance(java.text.DateFormat.SHORT,java.text.DateFormat.SHORT,getResources().getConfiguration().getLocales().get(0)).format(new Date(c.reminderAt)),13,false); body.addView(reminderStatus);
        body.addView(button("Hatırlatıcıyı düzenle",R.drawable.ic_clock,false,()->chooseReminder(c.reminderAt,time->{ Card current=store.find(id); if(current==null)return; Card changed=current.copy(); changed.reminderAt=time; if(time>0)changed.done=false; if(saveCard(changed)) reminderStatus.setText(time==0?L("Hatırlatma yok"):java.text.DateFormat.getDateTimeInstance(java.text.DateFormat.SHORT,java.text.DateFormat.SHORT,getResources().getConfiguration().getLocales().get(0)).format(new Date(time))); }))); gap(body,12);
        if(c.isVideo())body.addView(button("Videoyu aç",R.drawable.ic_watch,true,()->{touchCard(id);openVideo(c);}));
        else if(c.why.equals("Bul")) body.addView(button("Web'de ara",R.drawable.ic_find,true,()->searchFor(id)));
        else if(!c.hasVisual())body.addView(button(c.why.equals("İzle")?"İçeriği izle":"Orijinal içeriği aç",c.why.equals("İzle")?R.drawable.ic_watch:R.drawable.ic_external,true,()->{touchCard(id);open(c.url);}));
        gap(body,8);
        if(c.why.equals("Bul")&&!c.hasVisual()) { body.addView(button("Orijinal içeriği aç",R.drawable.ic_external,false,()->{touchCard(id);open(c.url);})); gap(body,8); }
        BottomSheetDialog dialog=sheet(body);detailDialog=dialog;
        body.addView(button(c.done?"Bekleyenlere geri al":IntentCatalog.get(c.why).completedLabel,R.drawable.ic_check,false,()-> {
            Card current=store.find(id); if(current==null) return; Card changed=current.copy(); changed.done=!changed.done; if(changed.done)changed.reminderAt=0;
            if(saveCard(changed)) { dialog.dismiss(); Snackbar.make(root,changed.done?L("Tamamlandı"):L("Bekleyenlere geri alındı"),Snackbar.LENGTH_SHORT).show(); }
        })); gap(body,8);
        LinearLayout actions=row(); actions.addView(button("Düzenle",R.drawable.ic_edit,false,()-> {dialog.dismiss(); editCard(id);}),new LinearLayout.LayoutParams(0,-2,1));
        MaterialButton delete=iconButton("Kaydı sil",R.drawable.ic_delete,()->new MaterialAlertDialogBuilder(this).setTitle(L("Kaydı sil?")).setMessage(c.title).setNegativeButton(L("Vazgeç"),null).setPositiveButton(L("Sil"),(d,w)-> {
            Card removed=store.find(id); if(removed==null) return;
            try { store.delete(id);if(removed.hasVisual())images.retainForUndo(removed.imageId);if(removed.isVideo())videos.retainForUndo(removed.videoId()); Reminders.cancel(this,id); dialog.dismiss(); render(); Snackbar.make(root,L("Kayıt silindi"),Snackbar.LENGTH_LONG).setAction(L("Geri al"),v->saveCard(removed)).show(); } catch(Exception e) { error(e); }
        }).show()); LinearLayout.LayoutParams p=new LinearLayout.LayoutParams(dp(48),dp(48)); p.setMargins(dp(8),0,0,0); actions.addView(delete,p); body.addView(actions); dialog.show();
    }
    private void updateOcrDetail(Card card){if(ocrStatus==null||!card.id.equals(detailCardId))return;ocrStatus.setText(L(card.ocrState.equals("pending")?"Görseldeki yazılar okunuyor…":card.ocrState.equals("failed")?"Metin okunamadı. Yeniden deneyebilirsin.":card.recognizedText.isEmpty()?"Görselde metin bulunamadı.":"Görseldeki metin"));ocrContent.setText(card.recognizedText);ocrContent.setVisibility(card.recognizedText.isEmpty()?View.GONE:View.VISIBLE);ocrRetryButton.setVisibility(card.ocrState.equals("pending")?View.GONE:View.VISIBLE);}
    private void searchFor(String id) {
        Card c=store.find(id); if(c==null) return; LinearLayout content=column(); content.setPadding(dp(24),dp(8),dp(24),0);
        TextInputLayout box=input(content,"Ne aramak istiyorsun?",c.effectiveSearchQuery(),300,1,View.generateViewId());
        androidx.appcompat.app.AlertDialog dialog=new MaterialAlertDialogBuilder(this).setTitle(L("Web'de ara")).setView(content)
            .setMessage(L("Yalnızca bu sorguyla Google araması açılır.")).setNegativeButton(L("Vazgeç"),null).setPositiveButton(L("Ara"),null).create();
        dialog.setOnShowListener(d->dialog.getButton(android.content.DialogInterface.BUTTON_POSITIVE).setOnClickListener(v-> {
            String term=box.getEditText().getText().toString().trim(); if(term.isEmpty()) { box.setError(L("Aramak istediğin şeyi yaz.")); return; }
            Card current=store.find(id); if(current==null) return;
            Card changed=new Card(current.id,current.url,current.title,current.why,current.note,current.createdAt,current.done,current.steps,term,current.imageId,current.tags,current.recognizedText,current.ocrState); changed.setVideo(current.videoId());changed.setMediaReference(current.mediaUri(),current.mediaName());changed.reminderAt=current.reminderAt;
            if(saveCard(changed)) { dialog.dismiss(); open(changed.webSearchUrl()); }
        })); dialog.show();
    }
    private void touchCard(String id) { Card current=store.find(id);if(current==null)return;try{store.save(current);render();}catch(Exception e){error(e);} }
    private boolean saveCard(Card card) { try { store.save(card); Reminders.sync(this,card); render(); return true; } catch(Exception e) { error(e); return false; } }
    private void open(String url) { try { startActivity(new Intent(Intent.ACTION_VIEW,Uri.parse(url))); } catch(ActivityNotFoundException e) { toast("Bağlantıyı açacak uygulama bulunamadı."); } }
    private String intentLabel(String key) {
        if(choices!=null && (Choices.custom(key)||Arrays.asList(Card.INTENTS).contains(key))) {
            String label=choices.label(key); return label.equals(key)?L(label):label;
        }
        return L(key);
    }
    private String reasonText(String why,String key) {
        for(Choices.Option option:choices.reasons(why,false))if(option.key.equals(key))return option.label.equals(key)?L(key):option.label;
        return Choices.fallback(key);
    }
    private List<String> homeFilters() {
        List<String> names=new ArrayList<>(Arrays.asList("Sosyal","Ekran görüntüleri","Videolar","Bekleyen","Unutulanlar","Tümü"));
        for(Choices.Option option:choices.intents(true))names.add(option.key);
        names.add("Tamamlanan"); if(!names.contains(filter))filter="Bekleyen"; return names;
    }
    private List<String> captureIntents(String selected) {
        List<String> names=new ArrayList<>();for(Choices.Option option:choices.intents(true))names.add(option.key);
        if(!selected.isEmpty()&&!names.contains(selected))names.add(selected);return names;
    }
    private void forgottenSettings() {
        LinearLayout body=sheetBody(); body.addView(text("Unutulanlar ayarı",23,true));
        body.addView(text("Tamamlanmamış bir kaydı açmadığın veya değiştirmediğin gün sayısı. Süreyi geçenler Unutulanlar sekmesinde görünür; kayıtların silinmez.",13,false));
        int days=getSharedPreferences("settings",MODE_PRIVATE).getInt("forgottenDays",15);
        TextInputLayout field=input(body,"Gün sayısı · 1–365",String.valueOf(days),3,1,View.generateViewId());field.getEditText().setInputType(android.text.InputType.TYPE_CLASS_NUMBER);
        BottomSheetDialog dialog=sheet(body); body.addView(button("Kaydet",R.drawable.ic_check,true,()-> {
            try { int value=Integer.parseInt(field.getEditText().getText().toString());if(value<1||value>365)throw new IllegalArgumentException();
                getSharedPreferences("settings",MODE_PRIVATE).edit().putInt("forgottenDays",value).apply();dialog.dismiss();render();
            } catch(Exception e){field.setError(L("1 ile 365 arasında gün sayısı gir."));}
        }));dialog.show();
    }
    private void manageIntents(Runnable done) { manageOptions(null,done); }
    private void manageReasons(String why,Runnable done) { manageOptions(why,done); }
    private void manageOptions(String why,Runnable done) {
        LinearLayout body=sheetBody();body.addView(text(why==null?"Niyetlerini düzenle":"Nedenlerini düzenle",23,true));
        body.addView(text("Seçenek ekle veya adını değiştir. İşaretli seçenekler uygulamada gösterilir. Gizlemek eski kayıtları değiştirmez.",13,false));
        LinearLayout list=column();body.addView(list);BottomSheetDialog dialog=sheet(body); boolean[] navigating={false};
        dialog.setOnDismissListener(d->{if(!navigating[0])done.run();});
        Runnable[] refresh=new Runnable[1];refresh[0]=()-> {
            list.removeAllViews();List<Choices.Option> options=why==null?choices.intents(false):choices.reasons(why,false);
            for(Choices.Option option:options) {
                LinearLayout line=row();MaterialCheckBox shown=new MaterialCheckBox(this);shown.setText(why==null?intentLabel(option.key):reasonText(why,option.key));shown.setTypeface(regular);shown.setChecked(option.visible);
                line.addView(shown,new LinearLayout.LayoutParams(0,-2,1));
                shown.setOnCheckedChangeListener((v,checked)->{try { if(why==null)choices.setIntent(option.key,option.label,checked);else choices.setReason(why,option.key,option.label,checked); home(); }catch(Exception e){shown.setChecked(option.visible);error(e);} });
                line.addView(iconButton("Düzenle",R.drawable.ic_edit,()->editOption(why,option,refresh[0]))); list.addView(line);
                if(why==null) {list.addView(button("Hızlı nedenler",0,false,()->{navigating[0]=true;dialog.dismiss();manageReasons(option.key,()->manageIntents(done));}));gap(list,8);}
            }
        };
        refresh[0].run();body.addView(button(why==null?"Yeni niyet ekle":"Yeni neden ekle",R.drawable.ic_add,true,()->editOption(why,null,refresh[0])));gap(body,8);
        body.addView(button("Bitti",R.drawable.ic_check,false,dialog::dismiss));dialog.show();
    }
    private void editOption(String why,Choices.Option option,Runnable refresh) {
        LinearLayout body=column();body.setPadding(dp(20),dp(8),dp(24),0);
        TextInputLayout field=input(body,"Seçenek adı",option==null?"":why==null?intentLabel(option.key):reasonText(why,option.key),60,1,View.generateViewId());
        androidx.appcompat.app.AlertDialog dialog=new MaterialAlertDialogBuilder(this).setTitle(L(option==null?"Yeni seçenek":"Seçeneği düzenle"))
            .setView(body).setNegativeButton(L("Vazgeç"),null).setPositiveButton(L("Kaydet"),null).create();
        dialog.setOnShowListener(d->dialog.getButton(DialogInterface.BUTTON_POSITIVE).setOnClickListener(v->{
            try { String label=field.getEditText().getText().toString().trim();String key=option==null?Choices.customKey(label):option.key;
                if(why==null)choices.setIntent(key,label,option==null||option.visible);else choices.setReason(why,key,label,option==null||option.visible);
                dialog.dismiss();refresh.run();home();
            } catch(Exception e){field.setError(L(e.getMessage()));}
        }));dialog.show();
    }
    private void addUsage(LinearLayout parent){usageBox=column();usageBox.setPadding(dp(16),dp(4),dp(16),dp(8));usageBox.setId(R.id.usage_box);usageLabel=text("",11,false);usageLabel.setId(R.id.usage_label);usageBox.addView(usageLabel);usageProgress=new com.google.android.material.progressindicator.LinearProgressIndicator(this);usageProgress.setId(R.id.usage_progress);usageProgress.setMax(CardStore.FREE_LIMIT);usageProgress.setTrackThickness(dp(3));usageProgress.setTrackCornerRadius(dp(2));usageProgress.setIndicatorColor(color(R.color.app_primary));usageProgress.setTrackColor(color(R.color.app_border));usageBox.addView(usageProgress,new LinearLayout.LayoutParams(-1,dp(6)));usageBox.setOnClickListener(v->{if(!page.equals("purchase"))purchaseScreen(null);});parent.addView(usageBox);updateUsage();}
    private void updateUsage(){if(usageBox==null||store==null||purchases==null)return;boolean premium=purchases.premium();usageBox.setVisibility(premium?View.GONE:View.VISIBLE);int count=store.all().size();usageLabel.setText(count+" / "+CardStore.FREE_LIMIT+" · "+L("Ücretsiz kayıt"));usageProgress.setProgress(Math.min(count,CardStore.FREE_LIMIT));usageProgress.setContentDescription(usageLabel.getText());}
    private TextView purchasePrice,purchaseStatus;private MaterialButton purchaseBuy;private Bundle purchaseDraft;
    private void purchaseScreen(Bundle draft) {
        purchaseDraft=draft==null?null:new Bundle(draft);
        LinearLayout body=subpage("purchase","tosave Sınırsız",R.id.purchase_screen);
        ImageView mark=new ImageView(this);mark.setImageResource(R.drawable.splash_art);mark.setScaleType(ImageView.ScaleType.FIT_CENTER);LinearLayout.LayoutParams image=new LinearLayout.LayoutParams(dp(112),dp(128));image.gravity=Gravity.CENTER;body.addView(mark,image);
        TextView headline=text("Kaydetmeye sınır koyma.",24,true);headline.setGravity(Gravity.CENTER);body.addView(headline);
        TextView explanation=text("İlk 20 kayıt ücretsiz. Tek seferlik satın almayla kayıt sınırını kaldır.",14,false);explanation.setGravity(Gravity.CENTER);body.addView(explanation);gap(body,16);addUsage(body);
        MaterialCardView features=surface();LinearLayout content=column();content.setPadding(dp(16),dp(12),dp(16),dp(12));features.addView(content);body.addView(features);
        for(String item:new String[]{"Sınırsız kayıt","Hatırlatmalar ve Unutulanlar","Kişisel niyetler ve hızlı nedenler","Ekran görüntüleri, videolar ve etiketler","Görseldeki yazıyla arama","Etiket ve not yedeği"}){LinearLayout line=row();ImageView check=new ImageView(this);check.setImageResource(R.drawable.ic_check);check.setImageTintList(ColorStateList.valueOf(color(R.color.app_primary)));line.addView(check,new LinearLayout.LayoutParams(dp(18),dp(18)));TextView label=text(item,13,false);LinearLayout.LayoutParams lp=new LinearLayout.LayoutParams(0,-2,1);lp.setMarginStart(dp(10));line.addView(label,lp);content.addView(line);}
        gap(body,14);body.addView(text("Tek seferlik ödeme. Abonelik yok.",14,true));body.addView(text("Ücretsiz sürümde en fazla 20 kayıt tutulur. Tamamlananlar da bu sayıya dahildir. Mevcut kayıtlarını her zaman açabilir, düzenleyebilir ve yedekleyebilirsin.",12,false));
        body.addView(text("Satın alma 20 kayıt sınırını kaldırır. Teknik kapasite: 5.000 kayıt / 4 MB.",11,false));
        purchasePrice=text("",20,true);purchasePrice.setId(R.id.purchase_price);body.addView(purchasePrice);purchaseStatus=text("",12,false);body.addView(purchaseStatus);gap(body,12);
        purchaseBuy=button("Satın al",R.drawable.ic_check,true,()->{if(purchases.premium()){Bundle resume=purchaseDraft;purchaseDraft=null;home();if(resume!=null)showCapture(resume);}else purchases.buy();});purchaseBuy.setId(R.id.purchase_buy);body.addView(purchaseBuy,full());
        body.addView(button("Satın almayı geri yükle",R.drawable.ic_download,false,()->{purchases.refresh();updatePaywall();}));body.addView(button("Şimdilik devam et",R.drawable.ic_close,false,this::home));updatePaywall();purchases.refresh();
    }
    private void updatePaywall(){if(purchaseBuy==null||!page.equals("purchase"))return;boolean premium=purchases.premium();purchasePrice.setText(premium?L("Sınırsız kayıt açık"):purchases.price().isEmpty()?PurchaseManager.BASE_PRICE_USD:purchases.price());purchasePrice.setTextSize(24);purchasePrice.setTextColor(color(R.color.app_primary));purchaseStatus.setText(L(purchases.status));purchaseBuy.setText(L(premium?"Devam et":"Satın al"));purchaseBuy.setEnabled(premium||purchases.available());}
    private void composeMail(String kind,String body) {
        String subject="savewhy - "+L(kind);Intent email=new Intent(Intent.ACTION_SENDTO,Uri.parse("mailto:tr.developer@hotmail.com?subject="+Uri.encode(subject)+"&body="+Uri.encode(body)));email.putExtra(Intent.EXTRA_SUBJECT,subject);email.putExtra(Intent.EXTRA_TEXT,body);
        try{startActivity(email);}catch(ActivityNotFoundException e){new MaterialAlertDialogBuilder(this).setTitle(L("İletişim ve destek")).setMessage("tr.developer@hotmail.com").setPositiveButton(L("Kapat"),null).show();}
    }
    private void maybeFeedback(){int saves=getSharedPreferences("feedback",MODE_PRIVATE).getInt("saves",0)+1;getSharedPreferences("feedback",MODE_PRIVATE).edit().putInt("saves",saves).apply();if(saves>=5&&!getSharedPreferences("feedback",MODE_PRIVATE).getBoolean("asked",false))handler.postDelayed(()->{if(!isFinishing()&&!isDestroyed()&&page.equals("home")&&getLifecycle().getCurrentState().isAtLeast(androidx.lifecycle.Lifecycle.State.RESUMED))feedback(false);},900);}
    private void feedback(boolean manual){
        getSharedPreferences("feedback",MODE_PRIVATE).edit().putBoolean("asked",true).apply();
        new MaterialAlertDialogBuilder(this).setTitle(L("Uygulamadan memnun musun?")).setMessage(L("Bir dakikalık görüşün tosave’ı geliştirmemize yardımcı olur."))
            .setPositiveButton(L("Memnunum"),(d,w)->feedbackMessage("Memnunum")).setNegativeButton(L("Geliştirilebilir"),(d,w)->feedbackMessage("Geliştirilebilir")).setNeutralButton(L("Şimdi değil"),(d,w)->{}).show();
    }
    private void feedbackMessage(String answer){getSharedPreferences("feedback",MODE_PRIVATE).edit().putString("answer",answer).apply();LinearLayout body=column();body.setPadding(dp(20),dp(8),dp(20),0);TextInputLayout note=input(body,"Görüşün · isteğe bağlı","",2000,3,View.generateViewId());
        new MaterialAlertDialogBuilder(this).setTitle(L("Geri bildirim")).setView(body).setMessage(L("E-posta uygulamasında taslak açılır. Göndermeden önce düzenleyebilirsin."))
            .setNegativeButton(L("Vazgeç"),null).setPositiveButton(L("E-posta ile paylaş"),(d,w)->composeMail("Geri bildirim",L("Uygulamadan memnun musun?")+" "+L(answer)+"\n\n"+note.getEditText().getText().toString()+"\n\ntosave "+BuildConfig.VERSION_NAME)).show();
    }
    private String L(String text) { return I18n.text(this,text); }
    private java.util.function.LongConsumer permissionChoice;
    private long permissionTime;
    private void acceptReminder(long time,java.util.function.LongConsumer apply) {
        if(time<=System.currentTimeMillis()) { toast("Gelecekte bir zaman seç."); return; }
        if(Build.VERSION.SDK_INT>=33 && ContextCompat.checkSelfPermission(this,android.Manifest.permission.POST_NOTIFICATIONS)!=android.content.pm.PackageManager.PERMISSION_GRANTED) {
            permissionChoice=apply; permissionTime=time; requestPermissions(new String[]{android.Manifest.permission.POST_NOTIFICATIONS},71); return;
        }
        if(!Reminders.enabled(this)) {
            new MaterialAlertDialogBuilder(this).setTitle(L("Bildirimler kapalı"))
                .setMessage(L("Hatırlatıcı için tosave bildirimlerini ayarlardan aç. Kaydın yine saklanır."))
                .setNegativeButton(L("Vazgeç"),null).setPositiveButton(L("Ayarları aç"),(d,w)->startActivity(new Intent(android.provider.Settings.ACTION_APP_NOTIFICATION_SETTINGS).putExtra(android.provider.Settings.EXTRA_APP_PACKAGE,getPackageName()))).show(); return;
        }
        apply.accept(time);
    }
    @Override public void onRequestPermissionsResult(int request,String[] permissions,int[] results) {
        super.onRequestPermissionsResult(request,permissions,results);
        if(request==71) {
            java.util.function.LongConsumer apply=permissionChoice; permissionChoice=null;
            if(results.length>0 && results[0]==android.content.pm.PackageManager.PERMISSION_GRANTED && apply!=null) acceptReminder(permissionTime,apply);
            else toast("Bildirim izni verilmedi. Hatırlatıcı eklenmedi.");
        }
    }
    private void chooseReminder(long current,java.util.function.LongConsumer apply) {
        LinearLayout body=sheetBody(); body.addView(text("Ne zaman hatırlatayım?",23,true));
        body.addView(text("Birlikte yapacaksan buluşma zamanını seç. Bildirim cihazında görünür; başka kişiye gönderilmez.",13,false));
        body.addView(text("Android pil tasarrufu nedeniyle bildirim seçilen saatten sonra gelebilir.",12,false));
        BottomSheetDialog dialog=sheet(body);
        String evening=Calendar.getInstance().get(Calendar.HOUR_OF_DAY)>=20?"Bu akşam · 20:00 geçti; yarın 20:00":"Bu akşam · 20:00";
        String[] labels={"1 saat sonra",evening,"Yarın · 09:00","Hafta sonu · 10:00"};
        for(int i=0;i<labels.length;i++) { final int choice=i; body.addView(button(labels[i],R.drawable.ic_clock,false,()-> {
            long time=ReminderTimes.quick(choice,System.currentTimeMillis(),TimeZone.getDefault()); dialog.dismiss(); acceptReminder(time,apply);
        })); gap(body,6); }
        body.addView(button("Tarih ve saat seç",R.drawable.ic_clock,false,()-> {
            Calendar c=Calendar.getInstance(); c.setTimeInMillis(current>System.currentTimeMillis()?current:System.currentTimeMillis()+3600000);
            new android.app.DatePickerDialog(this,(date,y,m,day)-> {
                c.set(y,m,day); new android.app.TimePickerDialog(this,(picker,h,min)-> {
                    c.set(Calendar.HOUR_OF_DAY,h); c.set(Calendar.MINUTE,min); c.set(Calendar.SECOND,0); c.set(Calendar.MILLISECOND,0);
                    dialog.dismiss(); acceptReminder(c.getTimeInMillis(),apply);
                },c.get(Calendar.HOUR_OF_DAY),c.get(Calendar.MINUTE),android.text.format.DateFormat.is24HourFormat(this)).show();
            },c.get(Calendar.YEAR),c.get(Calendar.MONTH),c.get(Calendar.DAY_OF_MONTH)).show();
        })); gap(body,8);
        body.addView(button("Hatırlatma yok",R.drawable.ic_close,false,()->{ apply.accept(0); dialog.dismiss(); })); dialog.show();
    }
    private void chooseLanguage() {
        String[] labels=LanguageOptions.NAMES.clone();labels[0]=L("Sistem dili");String[] codes=LanguageOptions.CODES;
        String current=AppCompatDelegate.getApplicationLocales().toLanguageTags(); int selected=LanguageOptions.index(current);
        new MaterialAlertDialogBuilder(this).setTitle(L("Uygulama dili")).setSingleChoiceItems(labels,selected,(dialog,index)-> {
            dialog.dismiss(); getSharedPreferences("settings",MODE_PRIVATE).edit().putString("language",codes[index]).apply();
            AppCompatDelegate.setApplicationLocales(androidx.core.os.LocaleListCompat.forLanguageTags(codes[index]));
        }).setNegativeButton(L("Vazgeç"),null).show();
    }
    private void chooseTheme() {
        LinearLayout body=sheetBody(); body.addView(text("Sana göre bir görünüm",24,true)); body.addView(text("AMOLED temada arka plan gerçek siyahtır.",13,false)); gap(body,12);
        BottomSheetDialog dialog=sheet(body); int selected=getSharedPreferences("settings",MODE_PRIVATE).getInt("theme",AppCompatDelegate.MODE_NIGHT_YES);
        String[] names={"Karanlık","Aydınlık","Sistem tercihi"}; int[] modes={AppCompatDelegate.MODE_NIGHT_YES,AppCompatDelegate.MODE_NIGHT_NO,AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM};
        for(int i=0;i<names.length;i++) {
            final int mode=modes[i]; MaterialButton b=button(L(names[i])+(selected==mode?L(" · seçili"):""),i==0?R.drawable.ic_moon:R.drawable.ic_sun,selected==mode,()-> {
                getSharedPreferences("settings",MODE_PRIVATE).edit().putInt("theme",mode).apply(); dialog.dismiss(); AppCompatDelegate.setDefaultNightMode(mode);
            }); body.addView(b,full()); gap(body,8);
        }
        gap(body,12); body.addView(rawText(getString(R.string.palette_title),20,true));
        String[] paletteNames=getResources().getStringArray(R.array.palette_names);
        for(int i=0;i<paletteNames.length;i++) {
            final int choice=i; MaterialButton b=button(paletteNames[i],R.drawable.ic_check,Palettes.index(this)==i,()->{
                getSharedPreferences("settings",MODE_PRIVATE).edit().putInt("palette",choice).apply();dialog.dismiss();recreate();
            });
            b.setIconTint(ColorStateList.valueOf(ContextCompat.getColor(this,Palettes.COLORS[i][5])));
            body.addView(b,full());gap(body,6);
        }
        dialog.show();
    }
    private void syncFilterChecks(View view) {
        if(view instanceof Chip){Chip chip=(Chip)view;chip.setChecked(filter.equals(chip.getTag()));}
        else if(view instanceof ViewGroup){ViewGroup group=(ViewGroup)view;for(int i=0;i<group.getChildCount();i++)syncFilterChecks(group.getChildAt(i));}
    }
    private android.animation.ObjectAnimator bellAnimation;
    private void refreshNotifications() {
        if(store==null)return;
        try {
            boolean due=false;for(Card card:store.all())if(!card.done&&card.reminderAt>0&&card.reminderAt<=System.currentTimeMillis()){Reminders.deliver(this,card.id,card.reminderAt);due=true;}
            if(due)store.load();int unread=NotificationInbox.unread(this);
            if(badge!=null){badge.setText(unread>99?"99+":String.valueOf(unread));badge.setVisibility(unread==0?View.GONE:View.VISIBLE);}
            if(bell!=null){bell.setContentDescription(L("Bildirimler")+" · "+unread);
                if(unread>0&&unread!=lastUnread&&page.equals("home")&&android.provider.Settings.Global.getFloat(getContentResolver(),android.provider.Settings.Global.ANIMATOR_DURATION_SCALE,1f)>0){
                    if(bellAnimation!=null)bellAnimation.cancel();bellAnimation=android.animation.ObjectAnimator.ofFloat(bell,"rotation",0,-12,12,-8,8,0);bellAnimation.setDuration(650);bellAnimation.start();
                }
            }
            lastUnread=unread;
        }catch(Exception e){/* Existing unreadable history is not reset. */}
    }
    private LinearLayout subpage(String name,String title,int id) {
        if(bellAnimation!=null)bellAnimation.cancel();
        page=name;root=new FrameLayout(this);root.setBackgroundColor(color(R.color.app_background));bindWindowInsets(root);setContentView(root);
        LinearLayout layout=column();root.addView(layout,new FrameLayout.LayoutParams(-1,-1));LinearLayout toolbar=row();toolbar.setPadding(dp(8),dp(4),dp(16),dp(4));
        MaterialButton back=iconButton("Geri",R.drawable.ic_chevron,()->{if(name.equals("about"))settings();else home();});back.setRotation(180);toolbar.addView(back);toolbar.addView(text(title,22,true));layout.addView(toolbar);
        androidx.core.widget.NestedScrollView scroll=new androidx.core.widget.NestedScrollView(this);layout.addView(scroll,new LinearLayout.LayoutParams(-1,0,1));LinearLayout body=column();body.setId(id);body.setPadding(dp(20),dp(8),dp(20),dp(24));scroll.addView(body);return body;
    }
    private void preferenceRow(LinearLayout body,String title,String subtitle,int icon,Runnable action) { preferenceRow(body,title,subtitle,icon,action,false); }
    private void preferenceRow(LinearLayout body,String title,String subtitle,int icon,Runnable action,boolean rawTitle) {
        LinearLayout line=row();line.setMinimumHeight(dp(64));line.setPadding(0,dp(8),0,dp(8));line.setBackgroundResource(android.R.drawable.list_selector_background);line.setClickable(true);line.setFocusable(true);line.setOnClickListener(v->action.run());
        ImageView image=new ImageView(this);image.setImageResource(icon);image.setImageTintList(ColorStateList.valueOf(color(R.color.app_primary)));image.setImportantForAccessibility(View.IMPORTANT_FOR_ACCESSIBILITY_NO);LinearLayout.LayoutParams ip=new LinearLayout.LayoutParams(dp(22),dp(22));ip.setMarginEnd(dp(16));line.addView(image,ip);
        LinearLayout texts=column();texts.addView(rawTitle?rawText(title,15,true):text(title,15,true));if(subtitle!=null&&!subtitle.isEmpty())texts.addView(text(subtitle,12,false));line.addView(texts,new LinearLayout.LayoutParams(0,-2,1));
        ImageView arrow=new ImageView(this);arrow.setImageResource(R.drawable.ic_chevron);arrow.setImageTintList(ColorStateList.valueOf(color(R.color.app_muted)));line.addView(arrow,new LinearLayout.LayoutParams(dp(16),dp(16)));body.addView(line);
    }
    private void section(LinearLayout body,String title){gap(body,16);TextView label=text(title,12,true);label.setTextColor(color(R.color.app_primary));body.addView(label);}
    private void settings() {
        LinearLayout body=subpage("settings","Ayarlar",R.id.settings_screen);
        preferenceRow(body,"tosave Sınırsız",purchases.premium()?"Sınırsız kayıt açık":"20 kayıt ücretsiz · Tek seferlik satın alma",R.drawable.ic_bookmark,()->purchaseScreen(null));
        section(body,"Genel");String language=AppCompatDelegate.getApplicationLocales().toLanguageTags();
        preferenceRow(body,"Uygulama dili",LanguageOptions.name(language,L("Sistem dili")),R.drawable.ic_info,this::chooseLanguage);
        int mode=getSharedPreferences("settings",MODE_PRIVATE).getInt("theme",AppCompatDelegate.MODE_NIGHT_YES);
        preferenceRow(body,"Görünüm",mode==AppCompatDelegate.MODE_NIGHT_YES?"Karanlık":mode==AppCompatDelegate.MODE_NIGHT_NO?"Aydınlık":"Sistem tercihi",R.drawable.ic_moon,this::chooseTheme);
        section(body,"Kayıtlar");int days=getSharedPreferences("settings",MODE_PRIVATE).getInt("forgottenDays",15);
        preferenceRow(body,"Unutulanlar ayarı",days+" "+L("gün"),R.drawable.ic_clock,this::forgottenSettings);
        preferenceRow(body,"Niyetler ve hızlı nedenler","Göster, gizle veya kişisel seçenek ekle.",R.drawable.ic_edit,()->manageIntents(()->settings()));
        section(body,"Bildirimler");preferenceRow(body,"Bildirim merkezi","Hatırlatmalarını burada gör.",R.drawable.ic_bell,this::notifications);
        preferenceRow(body,"Sistem bildirim ayarları",Reminders.enabled(this)?"Açık":"Kapalı",R.drawable.ic_settings,()->startActivity(new Intent(android.provider.Settings.ACTION_APP_NOTIFICATION_SETTINGS).putExtra(android.provider.Settings.EXTRA_APP_PACKAGE,getPackageName())));
        section(body,"Hakkında");preferenceRow(body,"tosave hakkında","tr.developer",R.drawable.ic_info,this::about);
        section(body,"Yedekleme");body.addView(text("Yeni yerel kayıtlar dosyaya bağlıdır. Yedek dosyanın aslını içermez.",12,false));preferenceRow(body,"Yedek oluştur","Etiketlerini, notlarını ve dosya bağlantılarını dışa aktar.",R.drawable.ic_download,this::exportBackup);
        preferenceRow(body,"Yedeği içe aktar","Mevcut kayıtların korunur.",R.drawable.ic_upload,this::importBackup);
    }
    private void about() {
        LinearLayout body=subpage("about","Hakkında",R.id.about_screen);gap(body,16);
        ImageView icon=new ImageView(this);icon.setId(R.id.about_icon);icon.setImageResource(R.mipmap.ic_launcher);icon.setContentDescription("tosave");icon.setScaleType(ImageView.ScaleType.FIT_CENTER);
        LinearLayout.LayoutParams imageParams=new LinearLayout.LayoutParams(dp(112),dp(112));imageParams.gravity=Gravity.CENTER_HORIZONTAL;body.addView(icon,imageParams);
        TextView name=rawText("tosave",24,true);name.setGravity(Gravity.CENTER);body.addView(name,full());
        TextView developer=rawText(getString(R.string.about_developer)+" · tr.developer",13,false);developer.setId(R.id.about_developer);developer.setGravity(Gravity.CENTER);body.addView(developer,full());
        TextView version=rawText(BuildConfig.VERSION_NAME,11,false);version.setGravity(Gravity.CENTER);body.addView(version,full());gap(body,16);
        aboutParagraph(body,R.string.about_purpose,R.id.about_purpose);
        section(body,"Birlikte katkı sunuyoruz");aboutParagraph(body,R.string.about_contribution,R.id.about_contribution);
        section(body,"Bağımsız geliştirme");aboutParagraph(body,R.string.about_independent,R.id.about_independent);
        section(body,"İletişim ve destek");preferenceRow(body,"İletişim ve destek","tr.developer@hotmail.com",R.drawable.ic_info,()->composeMail("Destek",""));body.getChildAt(body.getChildCount()-1).setId(R.id.about_support);
        preferenceRow(body,"Uygulamadan memnun musun?","Görüşünü bizimle paylaş.",R.drawable.ic_info,()->feedback(true));
    }
    private void aboutParagraph(LinearLayout body,int resource,int id){TextView paragraph=rawText(getString(resource),14,false);paragraph.setId(id);paragraph.setTextIsSelectable(true);paragraph.setLineSpacing(dp(4),1);body.addView(paragraph,full());gap(body,8);}
    private String reminderDate(long time){return java.text.DateFormat.getDateTimeInstance(java.text.DateFormat.SHORT,java.text.DateFormat.SHORT,getResources().getConfiguration().getLocales().get(0)).format(new Date(time));}
    private void notifications() {
        refreshNotifications();LinearLayout body=subpage("notifications","Bildirimler",R.id.notifications_screen);
        try {
            List<NotificationInbox.Entry> entries=NotificationInbox.entries(this);NotificationInbox.markRead(this,false);refreshNotifications();
            if(entries.isEmpty())body.addView(text("Henüz bildirim yok.",14,false));else {
                section(body,"Son bildirimler");for(NotificationInbox.Entry entry:entries)preferenceRow(body,entry.title,L("Hatırlatma")+" · "+reminderDate(entry.time),R.drawable.ic_bell,()->{
                    if(store.find(entry.cardId)==null){toast("Bu kayıt artık mevcut değil.");return;}home();detail(entry.cardId);
                },true);
                body.addView(button("Bildirim geçmişini temizle",R.drawable.ic_delete,false,()->{try{NotificationInbox.markRead(this,true);notifications();}catch(Exception e){error(e);}}));
            }
            List<Card> upcoming=new ArrayList<>();for(Card card:store.all())if(!card.done&&card.reminderAt>System.currentTimeMillis())upcoming.add(card);upcoming.sort(Comparator.comparingLong(card->card.reminderAt));
            if(!upcoming.isEmpty()){section(body,"Yaklaşan hatırlatmalar");for(Card card:upcoming)preferenceRow(body,card.title,reminderDate(card.reminderAt),R.drawable.ic_clock,()->{home();detail(card.id);},true);}
        }catch(Exception e){body.addView(text("Bildirimler okunamadı. Mevcut geçmiş korunuyor.",14,false));}
    }
    private void howItWorks() {
        LinearLayout body=sheetBody(); body.addView(text("Bir bağlantı. Bir niyet.",24,true)); gap(body,8);
        body.addView(text("1  İçeriği paylaş",18,true)); body.addView(text("Sosyal medya uygulamasında Paylaş → tosave.",14,false)); gap(body,12);
        body.addView(text("2  Niyetine dokun",18,true)); body.addView(text("Yap, Git, Al, Öğren, Üret, İzle veya Bul. Hızlı nedenlerden birini seçebilir veya doğrudan kaydedebilirsin.",14,false)); gap(body,12);
        body.addView(text("3  Zamanı gelince geri dön",18,true)); body.addView(text("Kaynağı aç, araştır veya adımlarını tamamla. Başlığı ve notları istediğin zaman düzenle.",14,false)); gap(body,12);
        body.addView(text("Başlık paylaşım bilgisinden veya bağlantıdan oluşturulur. Uygulama videonun içeriğini analiz etmez.",12,false));
        BottomSheetDialog dialog=sheet(body); body.addView(button("Anladım",R.drawable.ic_check,true,dialog::dismiss)); dialog.show();
    }
    private void exportBackup() {
        if(!store.isHealthy()) { toast("Kayıtlar okunamadı. Yedek oluşturulamıyor."); return; }
        Intent intent=new Intent(Intent.ACTION_CREATE_DOCUMENT).addCategory(Intent.CATEGORY_OPENABLE).setType("application/zip"); intent.putExtra(Intent.EXTRA_TITLE,"tosave-"+new java.text.SimpleDateFormat("yyyy-MM-dd",Locale.ROOT).format(new Date())+".zip");
        try { startActivityForResult(intent,EXPORT); } catch(ActivityNotFoundException e) { toast("Dosya seçici bulunamadı."); }
    }
    private void importBackup() {
        if(!store.isHealthy()) { toast("Kayıtlar okunamadı. Mevcut dosya korunuyor."); return; }
        try { startActivityForResult(new Intent(Intent.ACTION_OPEN_DOCUMENT).addCategory(Intent.CATEGORY_OPENABLE).setType("*/*"),IMPORT); } catch(ActivityNotFoundException e) { toast("Dosya seçici bulunamadı."); }
    }
    @Override protected void onActivityResult(int request,int result,Intent data) {
        super.onActivityResult(request,result,data);if((request==IMAGE||request==VIDEO)&&result!=RESULT_OK)imagePickDraft=null; if(result!=RESULT_OK || data==null || data.getData()==null) return; Uri uri=data.getData();
        try {
            if(request==IMAGE||request==VIDEO){acceptMedia(uri,request==VIDEO);return;}
            if(request==THUMBNAIL){Card card=store.find(thumbnailCardId);thumbnailCardId=null;if(card==null)return;thumbnails.select(card.url,()->getContentResolver().openInputStream(uri),success->{if(success){touchCard(card.id);render();detail(card.id);}else toast("Görsel okunamadı. Başka bir görsel seç.");});return;}
            if(request==EXPORT) {
                org.json.JSONObject backup=new org.json.JSONObject(store.backup());backup.put("customization",choices.snapshot());backup.put("forgottenDays",getSharedPreferences("settings",MODE_PRIVATE).getInt("forgottenDays",15));String json=backup.toString(2);List<Card> cards=store.all();toast("Yedek hazırlanıyor…");mediaWorker.execute(()->{try{File temporary=File.createTempFile("savewhy-backup-",".zip",getCacheDir());try{MediaBackup.write(new FileOutputStream(temporary),json,cards,images,new File(getFilesDir(),"previews"),videos);try(InputStream in=new FileInputStream(temporary);OutputStream out=getContentResolver().openOutputStream(uri,"wt")){if(out==null)throw new IOException("Cannot write backup");byte[] buffer=new byte[8192];int count;while((count=in.read(buffer))!=-1)out.write(buffer,0,count);}}finally{temporary.delete();}handler.post(()->{if(!destroyed)toast("Yedek oluşturuldu.");});}catch(Exception e){handler.post(()->{if(!destroyed)error(e);});}});
            } else if(request==IMPORT) {
                toast("Yedek kontrol ediliyor…");mediaWorker.execute(()->{try{MediaBackup.Pending pending=MediaBackup.read(getContentResolver().openInputStream(uri),getCacheDir(),images);mediaResources.add(pending);if(destroyed){closeMedia(pending);return;}handler.post(()->{if(destroyed){closeMedia(pending);return;}confirmImport(pending);});}catch(Exception e){handler.post(()->{if(!destroyed)error(e);});}});
            }
        } catch(Exception e) { error(e); }
    }
    private void confirmImport(MediaBackup.Pending pending){
        try{org.json.JSONObject preferences=null;int importDays=15;Object rootData=new org.json.JSONTokener(pending.json).nextValue();if(rootData instanceof org.json.JSONObject){org.json.JSONObject object=(org.json.JSONObject)rootData;if(object.has("customization"))preferences=Choices.validateSnapshot(object.getJSONObject("customization"));importDays=object.optInt("forgottenDays",15);if(importDays<1||importDays>365)throw new IllegalArgumentException("1 ile 365 arasında gün sayısı gir.");}if(preferences!=null)choices.previewMerge(preferences);final org.json.JSONObject importedPreferences=preferences;final int importedDays=importDays;
            int additions=CardCodec.merge(store.all(),pending.cards).size()-store.all().size();if(additions>0&&store.all().size()+additions>CardStore.FREE_LIMIT&&!purchases.premium()){closeMedia(pending);purchaseScreen(null);return;}
            androidx.appcompat.app.AlertDialog dialog=new MaterialAlertDialogBuilder(this).setTitle(L("Yedeği içe aktar?")).setMessage(additions+L(" yeni kayıt eklenecek. Mevcut kayıtlar korunur; tekrarlar atlanır.")+(pending.cards.stream().anyMatch(Card::isReference)?"\n\n"+L("Bağlı dosyalar ve erişim izinleri taşınmaz. Başka cihazda dosyaları yeniden bağlaman gerekir."):"")).setNegativeButton(L("Vazgeç"),null).setPositiveButton(L("İçe aktar"),null).create();dialog.setOnDismissListener(d->closeMedia(pending));dialog.setOnShowListener(d->dialog.getButton(android.content.DialogInterface.BUTTON_POSITIVE).setOnClickListener(v->{
                try{List<Card> merged=CardCodec.merge(store.all(),pending.cards);if(merged.size()>store.all().size()&&merged.size()>CardStore.FREE_LIMIT&&!purchases.premium()){dialog.dismiss();purchaseScreen(null);return;}dialog.getButton(android.content.DialogInterface.BUTTON_POSITIVE).setEnabled(false);dialog.setCancelable(false);dialog.getButton(android.content.DialogInterface.BUTTON_NEGATIVE).setEnabled(false);mediaWorker.execute(()->{try{MediaBackup.Install install=pending.install(images,new File(getFilesDir(),"previews"),merged,videos);mediaResources.add(install);if(destroyed){closeMedia(install);closeMedia(pending);return;}handler.post(()->{if(destroyed){closeMedia(install);closeMedia(pending);return;}try(install){store.load();int added=store.merge(pending.cards);install.commit();mediaResources.remove(install);if(importedPreferences!=null)choices.mergeSnapshot(importedPreferences);if(!getSharedPreferences("settings",MODE_PRIVATE).contains("forgottenDays"))getSharedPreferences("settings",MODE_PRIVATE).edit().putInt("forgottenDays",importedDays).apply();dialog.dismiss();home();Reminders.restore(this,store.all());for(Card card:store.all())if(card.isImage()&&card.ocrState.equals("pending"))scanImage(card.id);toast(added+L(" kayıt eklendi."));}catch(Exception e){dialog.dismiss();error(e);}});}catch(Exception e){handler.post(()->{dialog.dismiss();if(!destroyed)error(e);});}});
                }catch(Exception e){dialog.dismiss();error(e);}
            }));dialog.show();
        }catch(Exception e){closeMedia(pending);error(e);}
    }
    private void closeMedia(AutoCloseable resource){mediaResources.remove(resource);try{resource.close();}catch(Exception ignored){}}
    private void error(Exception e) { if(e instanceof CardStore.LimitException){purchaseScreen(null);return;}toast(e instanceof IllegalArgumentException?e.getMessage():e instanceof org.json.JSONException?"Geçerli bir tosave yedeği seç.":"İşlem tamamlanamadı. Dosyayı ve kullanılabilir alanı kontrol et."); }
    private void toast(String message) { Toast.makeText(this,L(message),Toast.LENGTH_LONG).show(); }
}
