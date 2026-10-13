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
    private static final int EXPORT=41, IMPORT=42;
    private CardStore store;
    private FrameLayout root;
    private RecyclerView recycler;
    private CardAdapter adapter;
    private LinearLayout empty;
    private ChipGroup filters;
    private TextView count, status;
    private Typeface regular, bold;
    private String query="", filter="Bekleyen";
    private CaptureUi capture;
    private final Handler handler=new Handler(Looper.getMainLooper());
    private final ExecutorService worker=Executors.newSingleThreadExecutor();
    private int generation;
    private Runnable searchTask;

    @Override public void onCreate(Bundle state) {
        int mode=getSharedPreferences("settings",MODE_PRIVATE).getInt("theme",AppCompatDelegate.MODE_NIGHT_YES);
        AppCompatDelegate.setDefaultNightMode(mode);
        super.onCreate(state);
        regular=ResourcesCompat.getFont(this,R.font.manrope_family); bold=Typeface.create(regular,Typeface.BOLD);
        store=new CardStore(getFilesDir());
        try { store.load(); } catch(Exception e) { toast("Kayıtlar okunamadı. Mevcut dosya korunuyor."); }
        if(state!=null) { query=state.getString("query",""); filter=state.getString("filter","Bekleyen"); }
        Reminders.restore(this,store.all());
        home();
        String notificationId=getIntent().getStringExtra("cardId"); if(notificationId!=null) detail(notificationId);
        if(state!=null && state.containsKey("draft")) showCapture(state.getBundle("draft")); else if(notificationId==null) receive(getIntent());
    }
    @Override protected void onSaveInstanceState(Bundle state) {
        super.onSaveInstanceState(state); state.putString("query",query); state.putString("filter",filter);
        if(capture!=null && capture.dialog.isShowing()) state.putBundle("draft",capture.bundle());
    }
    @Override protected void onDestroy() {
        handler.removeCallbacksAndMessages(null); worker.shutdownNow(); super.onDestroy();
    }
    @Override protected void onNewIntent(Intent intent) { super.onNewIntent(intent); setIntent(intent); String id=intent.getStringExtra("cardId"); if(id!=null) detail(id); else receive(intent); }
    private void receive(Intent intent) {
        if(!Intent.ACTION_SEND.equals(intent.getAction())) return;
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
    private int dp(int n) { return Math.round(n*getResources().getDisplayMetrics().density); }
    private int color(int id) { return ContextCompat.getColor(this,id); }
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
        MaterialButton b=new MaterialButton(this); b.setText(L(label)); b.setAllCaps(false); b.setTypeface(bold); b.setTextSize(14);
        b.setCornerRadius(dp(16)); b.setMinHeight(dp(52)); b.setMinimumHeight(dp(52)); b.setMinWidth(0); b.setMinimumWidth(0);
        b.setInsetTop(0); b.setInsetBottom(0); b.setStrokeWidth(0); b.setPadding(dp(16),dp(10),dp(16),dp(10));
        b.setBackgroundTintList(new ColorStateList(new int[][]{new int[]{-android.R.attr.state_enabled},new int[]{}},
                new int[]{color(R.color.app_surface_soft),color(primary?R.color.app_primary:R.color.app_surface_soft)}));
        b.setTextColor(new ColorStateList(new int[][]{new int[]{-android.R.attr.state_enabled},new int[]{}},
                new int[]{color(R.color.app_muted),color(primary?R.color.app_on_primary:R.color.app_primary)}));
        if(icon!=0) { b.setIconResource(icon); b.setIconSize(dp(20)); b.setIconPadding(dp(10)); b.setIconTint(b.getTextColors()); }
        b.setOnClickListener(v->action.run()); return b;
    }
    private MaterialButton iconButton(String label,int icon,Runnable action) {
        MaterialButton b=button("",icon,false,action); b.setContentDescription(L(label)); b.setIconPadding(0);
        b.setPadding(0,0,0,0); b.setIconGravity(MaterialButton.ICON_GRAVITY_TEXT_START); b.setCornerRadius(dp(24));
        b.setLayoutParams(new LinearLayout.LayoutParams(dp(48),dp(48))); return b;
    }
    private Chip chip(String name,int icon,boolean choice) {
        Chip c=new Chip(this); c.setChipDrawable(ChipDrawable.createFromAttributes(this,null,0,com.google.android.material.R.style.Widget_Material3_Chip_Filter)); c.setText(L(name)); c.setTag(name); c.setTypeface(regular);
        c.setTextSize(13); c.setCheckable(choice); c.setCheckedIconVisible(false); c.setEnsureMinTouchTargetSize(true);
        c.setChipCornerRadius(dp(14)); c.setChipStrokeWidth(dp(1)); c.setChipStrokeColor(ColorStateList.valueOf(color(R.color.app_border)));
        c.setChipBackgroundColor(selectionColors(R.color.app_primary,R.color.app_surface));
        c.setTextColor(selectionColors(R.color.app_on_primary,R.color.app_muted));
        if(icon!=0) { c.setChipIconResource(icon); c.setChipIconSize(dp(18)); c.setChipIconTint(selectionColors(R.color.app_on_primary,R.color.app_primary)); c.setChipIconVisible(true); }
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
        LinearLayout.LayoutParams p=full(); p.setMargins(0,dp(6),0,dp(8)); parent.addView(box,p); return box;
    }
    private void inputVisibility(TextInputLayout box,int visibility) { box.setVisibility(visibility); ((View)box.getTag()).setVisibility(visibility); }
    private void home() {
        WindowCompat.setDecorFitsSystemWindows(getWindow(),false);
        boolean light=getResources().getBoolean(R.bool.light_system_bars);
        WindowInsetsControllerCompat bars=WindowCompat.getInsetsController(getWindow(),getWindow().getDecorView());
        bars.setAppearanceLightStatusBars(light); bars.setAppearanceLightNavigationBars(light);
        getWindow().setNavigationBarColor(Build.VERSION.SDK_INT<27&&light?color(R.color.app_text):color(R.color.app_background));
        root=new FrameLayout(this); root.setBackgroundColor(color(R.color.app_background)); setContentView(root);
        ViewCompat.setOnApplyWindowInsetsListener(root,(v,insets)-> {
            Insets system=insets.getInsets(WindowInsetsCompat.Type.systemBars()); v.setPadding(0,system.top,0,system.bottom); return insets;
        });
        LinearLayout page=column(); root.addView(page,new FrameLayout.LayoutParams(-1,-1));
        LinearLayout header=row(); header.setPadding(dp(20),dp(16),dp(20),dp(12));
        header.addView(text("SaveWhy",28,true),new LinearLayout.LayoutParams(0,-2,1));
        header.addView(iconButton("Tema seç",R.drawable.ic_moon,this::chooseTheme));
        View spacer=new View(this); header.addView(spacer,new LinearLayout.LayoutParams(dp(8),1));
        header.addView(iconButton("Ayarlar ve yedekler",R.drawable.ic_more,this::settings)); page.addView(header);
        LinearLayout top=column(); top.setPadding(dp(20),0,dp(20),dp(4));
        top.addView(text("Kaydet. Nedenini hatırla. Harekete geç.",13,false));
        count=text("",13,false); top.addView(count);
        TextInputLayout search=input(top,"Kayıtlarında ara",query,200,1,R.id.main_search);
        search.setStartIconDrawable(R.drawable.ic_find); search.setEndIconMode(TextInputLayout.END_ICON_CLEAR_TEXT);
        search.getEditText().addTextChangedListener(watcher(()-> {
            query=search.getEditText().getText().toString(); scheduleRender();
        })); page.addView(top);
        HorizontalScrollView horizontal=new HorizontalScrollView(this); horizontal.setHorizontalScrollBarEnabled(false);
        filters=new ChipGroup(this); filters.setSingleLine(true); filters.setSingleSelection(true); filters.setSelectionRequired(true);
        filters.setChipSpacingHorizontal(dp(6)); filters.setPadding(dp(20),0,dp(20),dp(4));
        for(String name:IntentCatalog.filters()) {
            Chip c=chip(name,Arrays.asList(Card.INTENTS).contains(name)?icon(name):0,true); c.setTag(name); c.setChecked(filter.equals(name));
            c.setOnClickListener(v-> { filter=name; render(); }); filters.addView(c);
        }
        horizontal.addView(filters); page.addView(horizontal);
        status=text("",11,false); status.setPadding(dp(22),0,dp(20),dp(6)); page.addView(status);
        FrameLayout content=new FrameLayout(this); page.addView(content,new LinearLayout.LayoutParams(-1,0,1));
        recycler=new RecyclerView(this); recycler.setId(R.id.main_list); recycler.setLayoutManager(new LinearLayoutManager(this));
        recycler.setClipToPadding(false); recycler.setPadding(dp(20),dp(4),dp(20),dp(100));
        adapter=new CardAdapter(); recycler.setAdapter(adapter); content.addView(recycler,new FrameLayout.LayoutParams(-1,-1));
        empty=column(); empty.setPadding(dp(32),dp(30),dp(32),dp(90)); empty.setGravity(Gravity.CENTER);
        content.addView(empty,new FrameLayout.LayoutParams(-1,-1));
        ExtendedFloatingActionButton add=new ExtendedFloatingActionButton(this); add.setId(R.id.main_add); add.setText(L("Yeni kayıt")); add.setTypeface(bold);
        add.setIconResource(R.drawable.ic_add); add.setIconTint(ColorStateList.valueOf(color(R.color.app_on_primary)));
        add.setBackgroundTintList(ColorStateList.valueOf(color(R.color.app_primary))); add.setTextColor(color(R.color.app_on_primary));
        add.setOnClickListener(v->showCapture(new Bundle()));
        FrameLayout.LayoutParams fab=new FrameLayout.LayoutParams(-2,-2,Gravity.BOTTOM|Gravity.END); fab.setMargins(dp(20),0,dp(20),dp(20)); root.addView(add,fab);
        render();
    }
    private TextWatcher watcher(Runnable action) {
        return new TextWatcher() { public void beforeTextChanged(CharSequence s,int a,int b,int c) {} public void afterTextChanged(Editable e) { action.run(); } public void onTextChanged(CharSequence s,int a,int b,int c) {} };
    }
    private void scheduleRender() {
        if(searchTask!=null) handler.removeCallbacks(searchTask); searchTask=this::render; handler.postDelayed(searchTask,120);
    }
    private void render() {
        if(adapter==null || isDestroyed()) return;
        List<Card> all=store.all(); int done=0; for(Card c:all) if(c.done) done++;
        count.setText(getString(R.string.card_counts,all.size()-done,done));
        for(int i=0;i<filters.getChildCount();i++) { Chip c=(Chip)filters.getChildAt(i); c.setChecked(filter.equals(c.getTag())); }
        final String selected=filter, term=query; final int ticket=++generation;
        final List<Card> previous=new ArrayList<>(adapter.items);
        worker.execute(()-> {
            List<Card> visible=new ArrayList<>(); for(Card c:all) if(c.visible(selected,term)) visible.add(c);
            DiffUtil.DiffResult changes=DiffUtil.calculateDiff(new DiffUtil.Callback() {
                public int getOldListSize() { return previous.size(); }
                public int getNewListSize() { return visible.size(); }
                public boolean areItemsTheSame(int a,int b) { return previous.get(a).id.equals(visible.get(b).id); }
                public boolean areContentsTheSame(int a,int b) {
                    Card old=previous.get(a), now=visible.get(b);
                    return old.title.equals(now.title)&&old.url.equals(now.url)&&old.why.equals(now.why)&&old.note.equals(now.note)
                        &&old.done==now.done&&old.steps.size()==now.steps.size()&&old.completedSteps()==now.completedSteps();
                }
            });
            runOnUiThread(()-> {
                if(isDestroyed() || ticket!=generation) return;
                adapter.items=visible; changes.dispatchUpdatesTo(adapter);
                status.setText(store.isHealthy()?L(selected)+" · "+visible.size()+L(" kayıt"):L("Kayıt dosyası okunamadı. Mevcut dosya korunuyor."));
                empty.setVisibility(visible.isEmpty()?View.VISIBLE:View.GONE); recycler.setVisibility(visible.isEmpty()?View.GONE:View.VISIBLE);
                if(visible.isEmpty()) emptyState(all.isEmpty());
            });
        });
    }
    private void emptyState(boolean first) {
        empty.removeAllViews(); ImageView image=new ImageView(this); image.setImageResource(first?R.drawable.ic_bookmark:R.drawable.ic_find);
        image.setImageTintList(ColorStateList.valueOf(color(R.color.app_primary))); empty.addView(image,new LinearLayout.LayoutParams(dp(48),dp(48))); gap(empty,18);
        TextView heading=text(first?"İyi bir şey gördün mü?":"Burada henüz kayıt yok.",24,true); heading.setGravity(Gravity.CENTER); empty.addView(heading);
        TextView help=text(first?"Instagram, TikTok veya YouTube'da paylaş menüsünden SaveWhy'ı seç. Niyetine dokun, kaydet.":"Başka bir niyet seç veya arama sözcüğünü değiştir.",14,false);
        help.setGravity(Gravity.CENTER); empty.addView(help); gap(empty,18);
        if(first) empty.addView(button("Nasıl çalışır?",R.drawable.ic_info,false,this::howItWorks));
    }
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
        @Override public CardHolder onCreateViewHolder(ViewGroup parent,int type) { return new CardHolder(); }
        @Override public void onBindViewHolder(CardHolder h,int index) {
            Card c=items.get(index); h.symbol.setImageResource(icon(c.why)); h.symbol.setImageTintList(ColorStateList.valueOf(color(R.color.app_primary)));
            h.brand.setImageResource(sourceIcon(c.url)); h.brand.setImageTintList(ColorStateList.valueOf(color(R.color.app_text)));
            h.meta.setText(L(c.why)+"  ·  "+UrlTools.source(c.url)); h.title.setText(c.title); h.note.setText(c.note);
            h.progress.setText(c.done?L("Tamamlandı"):c.steps.isEmpty()?L("Niyetiyle kaydedildi"):c.completedSteps()+" / "+c.steps.size()+L(" adım"));
            h.card.setOnClickListener(v->detail(c.id)); h.card.setContentDescription(c.title+", "+c.why+", "+c.note);
        }
        @Override public int getItemCount() { return items.size(); }
    }
    private class CardHolder extends RecyclerView.ViewHolder {
        MaterialCardView card; ImageView symbol,brand; TextView meta,title,note,progress;
        CardHolder() { super(surface()); card=(MaterialCardView)itemView;
            RecyclerView.LayoutParams p=new RecyclerView.LayoutParams(-1,-2); p.setMargins(0,0,0,dp(12)); card.setLayoutParams(p);
            card.setClickable(true); card.setFocusable(true);
            LinearLayout body=column(); body.setPadding(dp(18),dp(16),dp(18),dp(16)); card.addView(body);
            LinearLayout head=row(); symbol=new ImageView(MainActivity.this); head.addView(symbol,new LinearLayout.LayoutParams(dp(22),dp(22)));
            meta=text("",12,false); LinearLayout.LayoutParams mp=new LinearLayout.LayoutParams(0,-2,1); mp.setMargins(dp(10),0,0,0); head.addView(meta,mp);
            brand=new ImageView(MainActivity.this); head.addView(brand,new LinearLayout.LayoutParams(dp(18),dp(18)));
            ImageView arrow=new ImageView(MainActivity.this); arrow.setImageResource(R.drawable.ic_chevron); arrow.setImageTintList(ColorStateList.valueOf(color(R.color.app_muted))); head.addView(arrow,new LinearLayout.LayoutParams(dp(18),dp(18))); body.addView(head); gap(body,6);
            title=text("",18,true); title.setMaxLines(2); title.setEllipsize(TextUtils.TruncateAt.END); body.addView(title);
            note=text("",13,false); note.setMaxLines(1); note.setEllipsize(TextUtils.TruncateAt.END); body.addView(note);
            progress=text("",11,false); progress.setTextColor(color(R.color.app_primary)); body.addView(progress);
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
        LinearLayout body=column(); body.setPadding(dp(24),dp(16),dp(24),dp(28)); body.setFocusableInTouchMode(true);
        View handle=new View(this); android.graphics.drawable.GradientDrawable shape=new android.graphics.drawable.GradientDrawable();
        shape.setColor(color(R.color.app_border)); shape.setCornerRadius(dp(4)); handle.setBackground(shape);
        LinearLayout.LayoutParams hp=new LinearLayout.LayoutParams(dp(32),dp(4)); hp.gravity=Gravity.CENTER; hp.setMargins(0,0,0,dp(18)); body.addView(handle,hp); return body;
    }
    private void showCapture(Bundle draft) {
        if(!store.isHealthy()) { toast("Kayıt dosyası okunamadı. Yeni kayıt açılamıyor."); return; }
        capture=new CaptureUi(draft); capture.dialog.show(); capture.body.requestFocus();
        if(capture.dialog.getWindow()!=null) capture.dialog.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE|WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);
    }
    private class CaptureUi {
        final BottomSheetDialog dialog; final LinearLayout body,details; final String id,sharedTitle;
        final TextInputLayout urlBox,queryBox; final TextView noteHelp; final MaterialButton reminderButton; long reminderAt; final EditText url,title,note,steps,search;
        final LinearLayout pasteActions;
        final ChipGroup intents,reasons; final MaterialButton save; final TextView source,purpose; final ImageView linkIcon;
        String why,reason; boolean detailsOpen;
        CaptureUi(Bundle b) {
            id=b.getString("id",""); sharedTitle=b.getString("sharedTitle",""); why=b.getString("why",""); reason=b.getString("reason","");
            body=sheetBody(); LinearLayout titleRow=row(); titleRow.addView(text(id.isEmpty()?"Neden kaydediyorsun?":"Kaydını düzenle",24,true),new LinearLayout.LayoutParams(0,-2,1));
            titleRow.addView(iconButton("Kapat",R.drawable.ic_close,()-> { if(capture!=null) capture.dialog.dismiss(); })); body.addView(titleRow);
            body.addView(text("Niyetini seç. Başlık ve detayları sonra da düzenleyebilirsin.",13,false)); gap(body,12);
            source=text("",13,true); source.setMaxLines(1); source.setEllipsize(TextUtils.TruncateAt.END); LinearLayout sourceRow=row(); linkIcon=new ImageView(MainActivity.this); linkIcon.setImageResource(R.drawable.ic_link); linkIcon.setImageTintList(ColorStateList.valueOf(color(R.color.app_primary))); sourceRow.addView(linkIcon,new LinearLayout.LayoutParams(dp(18),dp(18)));
            LinearLayout.LayoutParams sp=new LinearLayout.LayoutParams(0,-2,1); sp.setMargins(dp(10),0,dp(8),0); sourceRow.addView(source,sp); body.addView(sourceRow);
            urlBox=input(body,"Bağlantı",b.getString("url",""),4096,1,R.id.capture_link); url=urlBox.getEditText();
            url.setInputType(android.text.InputType.TYPE_CLASS_TEXT|android.text.InputType.TYPE_TEXT_VARIATION_URI);
            inputVisibility(urlBox,url.getText().toString().isEmpty()?View.VISIBLE:View.GONE);
            pasteActions=row();
            MaterialButton paste=button("Panodan ekle",R.drawable.ic_clipboard,false,()->paste(url)); pasteActions.addView(paste,new LinearLayout.LayoutParams(0,-2,1));
            sourceRow.addView(iconButton("Bağlantıyı düzenle",R.drawable.ic_edit,()->inputVisibility(urlBox,View.VISIBLE))); body.addView(pasteActions); gap(body,16);
            body.addView(text("NE İÇİN?",11,false)); intents=new ChipGroup(MainActivity.this); intents.setId(R.id.capture_intents); intents.setSingleSelection(true); intents.setSelectionRequired(true); intents.setChipSpacingHorizontal(dp(6));
            for(String name:Card.INTENTS) {
                Chip c=chip(name,icon(name),true); c.setId(View.generateViewId()); c.setTag(name); c.setChecked(name.equals(why));
                c.setOnClickListener(v-> { why=name; buildReasons(); guidance(); updateReminderLabel(); }); intents.addView(c);
            }
            body.addView(intents); purpose=text("Bir niyet seçerek başlayabilirsin.",13,false); body.addView(purpose); gap(body,12);
            body.addView(text("HIZLI NEDEN",11,false)); reasons=new ChipGroup(MainActivity.this); reasons.setId(R.id.capture_reasons); reasons.setSingleSelection(true); reasons.setSelectionRequired(true); reasons.setChipSpacingHorizontal(dp(6)); body.addView(reasons); gap(body,12);
            reminderAt=b.getLong("reminderAt",0);
            reminderButton=button("Hatırlatıcı ekle",R.drawable.ic_clock,false,()->chooseReminder(reminderAt,value->{ reminderAt=value; updateReminderLabel(); }));
            reminderButton.setId(R.id.capture_reminder); body.addView(reminderButton); gap(body,12);
            details=column(); details.addView(text("Bu alanlar isteğe bağlı. Kaydetmek için doldurman gerekmez.",13,false)); title=input(details,"Başlık · isteğe bağlı",b.getString("title",""),160,1,R.id.capture_title).getEditText();
            note=input(details,"Özel not · isteğe bağlı",b.getString("note",""),2000,3,R.id.capture_note).getEditText();
            noteHelp=text("",12,false); details.addView(noteHelp);
            details.addView(text("Yapılacaklar listesi",17,true)); details.addView(text("Bu kayıtla ne yapmak istediğini küçük işlere böl. Tamamladıklarını kartta işaretleyebilirsin. İstersen boş bırak.",13,false));
            steps=input(details,"Yapılacaklar · her satıra bir iş",b.getString("steps",""),10000,3,R.id.capture_steps).getEditText();
            details.addView(button("Örnek yapılacakları seç",R.drawable.ic_do,false,()-> {
                if(why.isEmpty()) { toast("Önce bir niyet seç."); return; }
                String[] examples=L(Card.template(why)).split("\n"); boolean[] selected=new boolean[examples.length];
                new MaterialAlertDialogBuilder(MainActivity.this).setTitle(L("Örnek yapılacakları seç"))
                    .setMultiChoiceItems(examples,selected,(d,index,checked)->selected[index]=checked)
                    .setNegativeButton(L("Vazgeç"),null).setPositiveButton(L("Ekle"),(d,w)-> {
                        LinkedHashSet<String> lines=new LinkedHashSet<>(); for(String line:steps.getText().toString().split("\n")) if(!line.trim().isEmpty()) lines.add(line.trim());
                        for(int i=0;i<examples.length;i++) if(selected[i]) lines.add(examples[i]); steps.setText(String.join("\n",lines));
                    }).show();
            }));
            queryBox=input(details,"Bul için arama sorgusu · isteğe bağlı",b.getString("searchQuery",""),300,1,R.id.capture_search_query); search=queryBox.getEditText();
            MaterialButton toggle=button("Not ve yapılacaklar · isteğe bağlı",R.drawable.ic_expand,false,()-> { detailsOpen=!detailsOpen; details.setVisibility(detailsOpen?View.VISIBLE:View.GONE); });
            toggle.setId(R.id.capture_details); body.addView(toggle); detailsOpen=b.getBoolean("detailsOpen",false); details.setVisibility(detailsOpen?View.VISIBLE:View.GONE); body.addView(details); gap(body,16);
            LinearLayout footer=column(); footer.setPadding(dp(24),dp(12),dp(24),dp(20)); footer.setBackgroundColor(color(R.color.app_surface));
            save=button(id.isEmpty()?"Kaydet":"Değişiklikleri kaydet",R.drawable.ic_check,true,this::save); save.setId(R.id.capture_save); footer.addView(save,full());
            footer.addView(text("Cihazında saklanır. İnternet bağlantısı gerekmez.",11,false));
            dialog=captureSheet(body,footer); url.addTextChangedListener(watcher(this::guidance)); buildReasons(); guidance(); updateReminderLabel();
        }
        void updateReminderLabel() {
            reminderButton.setText(reminderAt==0?L("Hatırlatıcı ekle"):L("Hatırlatma")+" · "+java.text.DateFormat.getDateTimeInstance(java.text.DateFormat.SHORT,java.text.DateFormat.SHORT,getResources().getConfiguration().getLocales().get(0)).format(new Date(reminderAt)));
        }
        void buildReasons() {
            reasons.removeAllViews(); if(why.isEmpty()) return;
            List<String> options=QuickCapture.reasons(why); if(!options.contains(reason)) reason=options.get(0);
            for(String value:options) {
                Chip c=chip(value,0,true); c.setId(View.generateViewId()); c.setChecked(value.equals(reason));
                c.setOnClickListener(v-> { reason=value; }); reasons.addView(c);
            }
        }
        void guidance() {
            String link=UrlTools.extract(url.getText().toString());
            linkIcon.setImageResource(link.isEmpty()?R.drawable.ic_link:sourceIcon(link));
            pasteActions.setVisibility(link.isEmpty()?View.VISIBLE:View.GONE);
            updateReminderLabel();
            source.setText(link.isEmpty()?L("Bir bağlantı paylaş veya panodan ekle."):QuickCapture.title(link,sharedTitle));
            save.setEnabled(!link.isEmpty()&&!why.isEmpty());
            inputVisibility(queryBox,why.equals("Bul")?View.VISIBLE:View.GONE);
            if(!why.isEmpty()) {
                IntentCatalog.Spec spec=IntentCatalog.get(why); purpose.setText(L(spec.description)); title.setHint(null);
                note.setHint(null); noteHelp.setText(L(spec.noteHint)); save.setText(id.isEmpty()?L(why)+L(" olarak kaydet"):L("Değişiklikleri kaydet"));
            }
        }
        Bundle bundle() {
            Bundle b=new Bundle(); b.putString("id",id); b.putString("sharedTitle",sharedTitle); b.putString("url",url.getText().toString());
            b.putString("why",why); b.putString("reason",reason); b.putString("title",title.getText().toString()); b.putString("note",note.getText().toString());
            b.putString("steps",steps.getText().toString()); b.putString("searchQuery",search.getText().toString()); b.putBoolean("detailsOpen",detailsOpen); b.putLong("reminderAt",reminderAt); return b;
        }
        void save() {
            try {
                Card c=QuickCapture.create(url.getText().toString(),sharedTitle,why,L(reason),title.getText().toString(),note.getText().toString(),steps.getText().toString(),search.getText().toString(),id.isEmpty()?null:store.find(id));
                if(reminderAt!=0 && reminderAt<=System.currentTimeMillis()) { toast("Gelecekte bir zaman seç."); return; }
                c.reminderAt=reminderAt; if(reminderAt>0)c.done=false; store.save(c); Reminders.sync(MainActivity.this,c); dialog.dismiss(); render();
                Snackbar.make(root,id.isEmpty()?L("Kaydedildi · ")+L(why):L("Kayıt güncellendi"),Snackbar.LENGTH_LONG).show();
            } catch(Exception e) { error(e); }
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
        Card c=store.find(id); if(c==null) return; Bundle b=new Bundle(); b.putString("id",c.id); b.putString("url",c.url); b.putString("why",c.why); b.putString("title",c.title);
        String preset=""; for(String option:QuickCapture.reasons(c.why)) if(option.equals(c.note)||L(option).equals(c.note))preset=option; boolean quick=!preset.isEmpty(); b.putString("reason",preset); b.putString("note",quick?"":c.note);
        b.putLong("reminderAt",c.reminderAt); b.putString("searchQuery",c.searchQuery); List<String> lines=new ArrayList<>(); for(Card.Step s:c.steps) lines.add(s.text); b.putString("steps",String.join("\n",lines)); showCapture(b);
    }
    private void detail(String id) {
        Card c=store.find(id); if(c==null) return;
        LinearLayout body=sheetBody(); body.addView(text(L(c.why)+"  ·  "+UrlTools.source(c.url),12,false)); body.addView(rawText(c.title,24,true)); gap(body,8);
        body.addView(text("NİYETİM",11,false)); body.addView(rawText(c.note,16,true)); gap(body,14);
        TextView progress=text(c.steps.isEmpty()?"":c.completedSteps()+" / "+c.steps.size()+L(" adım tamamlandı"),12,false); body.addView(progress);
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
        if(c.why.equals("Bul")) body.addView(button("Web'de ara",R.drawable.ic_find,true,()->searchFor(id)));
        else body.addView(button(c.why.equals("İzle")?"İçeriği izle":"Orijinal içeriği aç",c.why.equals("İzle")?R.drawable.ic_watch:R.drawable.ic_external,true,()->open(c.url)));
        gap(body,8);
        if(c.why.equals("Bul")) { body.addView(button("Orijinal içeriği aç",R.drawable.ic_external,false,()->open(c.url))); gap(body,8); }
        BottomSheetDialog dialog=sheet(body);
        body.addView(button(c.done?"Bekleyenlere geri al":IntentCatalog.get(c.why).completedLabel,R.drawable.ic_check,false,()-> {
            Card current=store.find(id); if(current==null) return; Card changed=current.copy(); changed.done=!changed.done; if(changed.done)changed.reminderAt=0;
            if(saveCard(changed)) { dialog.dismiss(); Snackbar.make(root,changed.done?L("Tamamlandı"):L("Bekleyenlere geri alındı"),Snackbar.LENGTH_SHORT).show(); }
        })); gap(body,8);
        LinearLayout actions=row(); actions.addView(button("Düzenle",R.drawable.ic_edit,false,()-> {dialog.dismiss(); editCard(id);}),new LinearLayout.LayoutParams(0,-2,1));
        MaterialButton delete=iconButton("Kaydı sil",R.drawable.ic_delete,()->new MaterialAlertDialogBuilder(this).setTitle(L("Kaydı sil?")).setMessage(c.title).setNegativeButton(L("Vazgeç"),null).setPositiveButton(L("Sil"),(d,w)-> {
            Card removed=store.find(id); if(removed==null) return;
            try { store.delete(id); Reminders.cancel(this,id); dialog.dismiss(); render(); Snackbar.make(root,L("Kayıt silindi"),Snackbar.LENGTH_LONG).setAction(L("Geri al"),v->saveCard(removed)).show(); } catch(Exception e) { error(e); }
        }).show()); LinearLayout.LayoutParams p=new LinearLayout.LayoutParams(dp(48),dp(48)); p.setMargins(dp(8),0,0,0); actions.addView(delete,p); body.addView(actions); dialog.show();
    }
    private void searchFor(String id) {
        Card c=store.find(id); if(c==null) return; LinearLayout content=column(); content.setPadding(dp(24),dp(8),dp(24),0);
        TextInputLayout box=input(content,"Ne aramak istiyorsun?",c.effectiveSearchQuery(),300,1,View.generateViewId());
        androidx.appcompat.app.AlertDialog dialog=new MaterialAlertDialogBuilder(this).setTitle(L("Web'de ara")).setView(content)
            .setMessage(L("Yalnızca bu sorguyla Google araması açılır.")).setNegativeButton(L("Vazgeç"),null).setPositiveButton(L("Ara"),null).create();
        dialog.setOnShowListener(d->dialog.getButton(android.content.DialogInterface.BUTTON_POSITIVE).setOnClickListener(v-> {
            String term=box.getEditText().getText().toString().trim(); if(term.isEmpty()) { box.setError(L("Aramak istediğin şeyi yaz.")); return; }
            Card current=store.find(id); if(current==null) return;
            Card changed=new Card(current.id,current.url,current.title,current.why,current.note,current.createdAt,current.done,current.steps,term); changed.reminderAt=current.reminderAt;
            if(saveCard(changed)) { dialog.dismiss(); open(changed.webSearchUrl()); }
        })); dialog.show();
    }
    private boolean saveCard(Card card) { try { store.save(card); Reminders.sync(this,card); render(); return true; } catch(Exception e) { error(e); return false; } }
    private void open(String url) { try { startActivity(new Intent(Intent.ACTION_VIEW,Uri.parse(url))); } catch(ActivityNotFoundException e) { toast("Bağlantıyı açacak uygulama bulunamadı."); } }
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
                .setMessage(L("Hatırlatıcı için SaveWhy bildirimlerini ayarlardan aç. Kaydın yine saklanır."))
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
        String[] labels={L("Sistem dili"),"Türkçe","English"}; String[] codes={"","tr","en"};
        String current=AppCompatDelegate.getApplicationLocales().toLanguageTags(); int selected=current.startsWith("tr")?1:current.startsWith("en")?2:0;
        new MaterialAlertDialogBuilder(this).setTitle(L("Uygulama dili")).setSingleChoiceItems(labels,selected,(dialog,index)-> {
            dialog.dismiss(); getSharedPreferences("settings",MODE_PRIVATE).edit().putString("language",codes[index]).apply();
            AppCompatDelegate.setApplicationLocales(androidx.core.os.LocaleListCompat.forLanguageTags(codes[index]));
        }).setNegativeButton(L("Vazgeç"),null).show();
    }
    private void chooseTheme() {
        LinearLayout body=sheetBody(); body.addView(text("Sana göre bir görünüm",24,true)); body.addView(text("AMOLED temada arka plan gerçek siyahtır.",13,false)); gap(body,12);
        BottomSheetDialog dialog=sheet(body); int selected=getSharedPreferences("settings",MODE_PRIVATE).getInt("theme",AppCompatDelegate.MODE_NIGHT_YES);
        String[] names={"Simsiyah AMOLED","Aydınlık","Sistem tercihi"}; int[] modes={AppCompatDelegate.MODE_NIGHT_YES,AppCompatDelegate.MODE_NIGHT_NO,AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM};
        for(int i=0;i<names.length;i++) {
            final int mode=modes[i]; MaterialButton b=button(L(names[i])+(selected==mode?L(" · seçili"):""),i==0?R.drawable.ic_moon:R.drawable.ic_sun,selected==mode,()-> {
                getSharedPreferences("settings",MODE_PRIVATE).edit().putInt("theme",mode).apply(); dialog.dismiss(); AppCompatDelegate.setDefaultNightMode(mode);
            }); body.addView(b,full()); gap(body,8);
        } dialog.show();
    }
    private void settings() {
        LinearLayout body=sheetBody(); body.addView(text("SaveWhy",24,true)); body.addView(text("Kayıtların senin cihazında. Kontrol sende.",13,false)); gap(body,14);
        BottomSheetDialog dialog=sheet(body);
        body.addView(button("Uygulama dili",R.drawable.ic_info,false,()-> { dialog.dismiss(); chooseLanguage(); })); gap(body,8);
        body.addView(button("Görünüm",R.drawable.ic_moon,false,()-> { dialog.dismiss(); chooseTheme(); })); gap(body,8);
        body.addView(button("JSON yedeği oluştur",R.drawable.ic_download,false,()-> { dialog.dismiss(); exportBackup(); })); gap(body,8);
        body.addView(button("Yedeği içe aktar",R.drawable.ic_upload,false,()-> { dialog.dismiss(); importBackup(); })); gap(body,8);
        body.addView(button("Nasıl çalışır?",R.drawable.ic_info,false,()-> { dialog.dismiss(); howItWorks(); })); gap(body,12);
        body.addView(button("Font lisansı",R.drawable.ic_info,false,()-> {
            try(InputStream in=getResources().openRawResource(R.raw.manrope_license)) {
                String license=CardStore.read(in); new MaterialAlertDialogBuilder(this).setTitle("Manrope · SIL OFL 1.1").setMessage(license).setPositiveButton(L("Kapat"),null).show();
            } catch(Exception e) { error(e); }
        })); gap(body,12);
        body.addView(text("v0.4.0 · Hatırlatmalar ve dil tercihi\nAI içerik analizi henüz eklenmedi.",11,false)); dialog.show();
    }
    private void howItWorks() {
        LinearLayout body=sheetBody(); body.addView(text("Bir bağlantı. Bir niyet.",24,true)); gap(body,8);
        body.addView(text("1  İçeriği paylaş",18,true)); body.addView(text("Sosyal medya uygulamasında Paylaş → SaveWhy.",14,false)); gap(body,12);
        body.addView(text("2  Niyetine dokun",18,true)); body.addView(text("Yap, Git, Al, Öğren, Üret, İzle veya Bul. Hızlı nedenlerden birini seçebilir veya doğrudan kaydedebilirsin.",14,false)); gap(body,12);
        body.addView(text("3  Zamanı gelince geri dön",18,true)); body.addView(text("Kaynağı aç, araştır veya adımlarını tamamla. Başlığı ve notları istediğin zaman düzenle.",14,false)); gap(body,12);
        body.addView(text("Başlık paylaşım bilgisinden veya bağlantıdan oluşturulur. Uygulama videonun içeriğini analiz etmez.",12,false));
        BottomSheetDialog dialog=sheet(body); body.addView(button("Anladım",R.drawable.ic_check,true,dialog::dismiss)); dialog.show();
    }
    private void exportBackup() {
        if(!store.isHealthy()) { toast("Kayıtlar okunamadı. Yedek oluşturulamıyor."); return; }
        Intent intent=new Intent(Intent.ACTION_CREATE_DOCUMENT).addCategory(Intent.CATEGORY_OPENABLE).setType("application/json"); intent.putExtra(Intent.EXTRA_TITLE,"SaveWhy-"+new java.text.SimpleDateFormat("yyyy-MM-dd",Locale.ROOT).format(new Date())+".json");
        try { startActivityForResult(intent,EXPORT); } catch(ActivityNotFoundException e) { toast("Dosya seçici bulunamadı."); }
    }
    private void importBackup() {
        if(!store.isHealthy()) { toast("Kayıtlar okunamadı. Mevcut dosya korunuyor."); return; }
        try { startActivityForResult(new Intent(Intent.ACTION_OPEN_DOCUMENT).addCategory(Intent.CATEGORY_OPENABLE).setType("*/*"),IMPORT); } catch(ActivityNotFoundException e) { toast("Dosya seçici bulunamadı."); }
    }
    @Override protected void onActivityResult(int request,int result,Intent data) {
        super.onActivityResult(request,result,data); if(result!=RESULT_OK || data==null || data.getData()==null) return; Uri uri=data.getData();
        try {
            if(request==EXPORT) {
                byte[] bytes=store.backup().getBytes(StandardCharsets.UTF_8);
                try(OutputStream out=getContentResolver().openOutputStream(uri,"wt")) { if(out==null) throw new IOException("Dosya açılamadı."); out.write(bytes); }
                toast("Yedek oluşturuldu.");
            } else if(request==IMPORT) {
                List<Card> imported; try(InputStream in=getContentResolver().openInputStream(uri)) { if(in==null) throw new IOException("Dosya açılamadı."); imported=CardCodec.decode(CardStore.read(in)); }
                int additions=CardCodec.merge(store.all(),imported).size()-store.all().size();
                new MaterialAlertDialogBuilder(this).setTitle(L("Yedeği içe aktar?")).setMessage(additions+L(" yeni kayıt eklenecek. Mevcut kayıtlar korunur; tekrarlar atlanır."))
                    .setNegativeButton(L("Vazgeç"),null).setPositiveButton(L("İçe aktar"),(d,w)-> { try { int added=store.merge(imported); Reminders.restore(this,store.all()); render(); toast(added+L(" kayıt eklendi.")); } catch(Exception e) { error(e); } }).show();
            }
        } catch(Exception e) { error(e); }
    }
    private void error(Exception e) { toast(e instanceof IllegalArgumentException?e.getMessage():e instanceof org.json.JSONException?"Geçerli bir SaveWhy yedeği seç.":"İşlem tamamlanamadı. Dosyayı ve kullanılabilir alanı kontrol et."); }
    private void toast(String message) { Toast.makeText(this,L(message),Toast.LENGTH_LONG).show(); }
}
