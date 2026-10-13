package com.savewhy.app;

import android.app.*;
import android.content.*;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.text.*;
import android.view.*;
import android.widget.*;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.util.*;

public class MainActivity extends Activity {
    private static final int EXPORT = 41, IMPORT = 42;
    private static final int INK = Color.rgb(26,48,40), GREEN = Color.rgb(26,112,87), MUTED = Color.rgb(101,117,108);
    private CardStore store;
    private LinearLayout list, filterRow;
    private TextView summary, status;
    private String query="", filter="Tümü";
    private AlertDialog editorDialog;
    private EditText editUrl, editTitle, editNote, editSteps;
    private Spinner editWhy;
    private String editingId="";

    @Override public void onCreate(Bundle state) {
        super.onCreate(state);
        store=new CardStore(getFilesDir());
        try { store.load(); } catch(Exception e) { toast("Kayıtlar okunamadı. Mevcut dosya korunuyor."); }
        if (state!=null) { query=state.getString("query",""); filter=state.getString("filter","Tümü"); }
        home();
        if (state!=null && state.containsKey("draft")) showEditor(state.getBundle("draft"));
        else receive(getIntent());
    }
    @Override protected void onSaveInstanceState(Bundle state) {
        super.onSaveInstanceState(state); state.putString("query",query); state.putString("filter",filter);
        if (editorDialog!=null && editorDialog.isShowing()) state.putBundle("draft",draft());
    }
    @Override protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent); setIntent(intent); receive(intent);
    }
    private void receive(Intent intent) {
        if (!Intent.ACTION_SEND.equals(intent.getAction())) return;
        String shared=intent.getStringExtra(Intent.EXTRA_TEXT);
        if (shared==null && intent.getClipData()!=null && intent.getClipData().getItemCount()>0)
            shared=String.valueOf(intent.getClipData().getItemAt(0).coerceToText(this));
        final String link=UrlTools.extract(shared);
        String subject=intent.getStringExtra(Intent.EXTRA_SUBJECT);
        intent.setAction(Intent.ACTION_MAIN);
        if (link.isEmpty()) { toast("Paylaşılan metinde geçerli bir bağlantı bulunamadı."); return; }
        Bundle draft=new Bundle(); draft.putString("url",link);
        draft.putString("title",subject==null?"":subject.substring(0,Math.min(160,subject.length())));
        if (editorDialog!=null && editorDialog.isShowing()) {
            new AlertDialog.Builder(this).setTitle("Yeni paylaşımı aç?")
                .setMessage("Açık formdaki kaydedilmemiş değişiklikler kapanacak.")
                .setNegativeButton("Formda kal",null).setPositiveButton("Yeni paylaşımı aç",(d,w)-> {
                    editorDialog.dismiss(); showEditor(draft);
                }).show();
        } else showEditor(draft);
    }

    private int dp(int n) { return Math.round(n*getResources().getDisplayMetrics().density); }
    private LinearLayout column() { LinearLayout l=new LinearLayout(this); l.setOrientation(LinearLayout.VERTICAL); return l; }
    private TextView text(String value,int size,int color) {
        TextView t=new TextView(this); t.setText(value); t.setTextSize(size); t.setTextColor(color);
        t.setPadding(0,dp(4),0,dp(4)); return t;
    }
    private TextView heading(String value,int size) { TextView t=text(value,size,INK); t.setTypeface(null,Typeface.BOLD); return t; }
    private GradientDrawable surface(int color,int radius) { GradientDrawable b=new GradientDrawable(); b.setColor(color); b.setCornerRadius(dp(radius)); return b; }
    private Button button(String label,boolean primary,Runnable action) {
        Button b=new Button(this); b.setText(label); b.setAllCaps(false); b.setTextSize(14);
        b.setTextColor(primary?Color.WHITE:GREEN); b.setBackground(surface(primary?GREEN:Color.rgb(231,240,234),14));
        b.setPadding(dp(14),dp(8),dp(14),dp(8)); b.setMinHeight(dp(48)); b.setMinimumHeight(dp(48));
        LinearLayout.LayoutParams p=new LinearLayout.LayoutParams(-1,-2); p.setMargins(0,dp(6),0,dp(6)); b.setLayoutParams(p);
        b.setOnClickListener(v->action.run()); return b;
    }
    private EditText input(String hint,String value,int max,boolean multiline) {
        EditText e=new EditText(this); e.setTextColor(INK); e.setHintTextColor(MUTED); e.setTextSize(16);
        e.setHint(hint); e.setText(value); e.setPadding(dp(12),dp(12),dp(12),dp(12));
        e.setBackground(surface(Color.rgb(241,244,239),12)); e.setSingleLine(!multiline);
        if(multiline) { e.setInputType(android.text.InputType.TYPE_CLASS_TEXT|android.text.InputType.TYPE_TEXT_FLAG_MULTI_LINE); e.setMinLines(3); e.setGravity(Gravity.TOP); }
        e.setFilters(new InputFilter[]{new InputFilter.LengthFilter(max)});
        LinearLayout.LayoutParams p=new LinearLayout.LayoutParams(-1,-2); p.setMargins(0,dp(4),0,dp(8)); e.setLayoutParams(p);
        return e;
    }
    private void home() {
        LinearLayout root=column(); root.setBackgroundColor(Color.rgb(246,245,239)); setContentView(root);
        root.setPadding(dp(20),dp(12),dp(20),dp(12));
        root.setOnApplyWindowInsetsListener((v,i)-> {
            root.setPadding(dp(20),dp(12)+i.getSystemWindowInsetTop(),dp(20),dp(12)+i.getSystemWindowInsetBottom()); return i;
        });
        LinearLayout top=new LinearLayout(this); top.setGravity(Gravity.CENTER_VERTICAL);
        top.addView(heading("SaveWhy",32),new LinearLayout.LayoutParams(0,-2,1));
        Button menu=button("•••",false,this::menu); menu.setContentDescription("Yedekleme ve uygulama bilgisi");
        top.addView(menu,new LinearLayout.LayoutParams(dp(58),dp(48))); root.addView(top);
        root.addView(text("Kaydettiğin içerik, yapacağın bir şeye dönüşsün.",14,MUTED));
        summary=text("",13,GREEN); root.addView(summary);
        root.addView(button("＋  Yeni içerik kaydet",true,()->showEditor(new Bundle())));
        EditText search=input("Başlık, neden veya adım ara…",query,200,false); root.addView(search);
        search.addTextChangedListener(new TextWatcher() {
            public void beforeTextChanged(CharSequence s,int start,int count,int after) {}
            public void afterTextChanged(Editable e) {}
            public void onTextChanged(CharSequence s,int start,int before,int count) { query=s.toString(); render(); }
        });
        HorizontalScrollView filters=new HorizontalScrollView(this); filters.setHorizontalScrollBarEnabled(false); filterRow=new LinearLayout(this);
        for(String name:new String[]{"Tümü","Yap","Git","Al","Öğren","Üret","Tamamlanan"}) {
            Button b=button(name,false,()-> {filter=name; render();});
            LinearLayout.LayoutParams p=new LinearLayout.LayoutParams(-2,dp(48)); p.setMargins(0,dp(4),dp(6),dp(4));
            b.setTag(name); filterRow.addView(b,p);
        }
        filters.addView(filterRow); root.addView(filters);
        status=text("",12,MUTED); root.addView(status);
        ScrollView scroll=new ScrollView(this); scroll.setFillViewport(true); list=column(); scroll.addView(list);
        root.addView(scroll,new LinearLayout.LayoutParams(-1,0,1)); render();
    }
    private void render() {
        if(list==null) return;
        list.removeAllViews(); List<Card> cards=store.all(); int completed=0, shown=0;
        for(Card c:cards) if(c.done) completed++;
        summary.setText(getString(R.string.card_counts,cards.size()-completed,completed));
        for(int i=0;i<filterRow.getChildCount();i++) {
            Button b=(Button)filterRow.getChildAt(i); boolean active=filter.equals(b.getTag());
            b.setBackground(surface(active?GREEN:Color.rgb(231,240,234),14)); b.setTextColor(active?Color.WHITE:GREEN);
        }
        for(Card c:cards) {
            if(filter.equals("Tamamlanan")?!c.done:c.done||(!filter.equals("Tümü")&&!filter.equals(c.why))) continue;
            if(!c.matches(query)) continue;
            shown++;
            LinearLayout box=column(); box.setPadding(dp(16),dp(14),dp(16),dp(14)); box.setBackground(surface(Color.WHITE,20));
            LinearLayout.LayoutParams p=new LinearLayout.LayoutParams(-1,-2); p.setMargins(0,dp(6),0,dp(8)); list.addView(box,p);
            box.addView(text(c.why.toUpperCase(Locale.forLanguageTag("tr"))+"  ·  "+UrlTools.source(c.url),12,GREEN));
            box.addView(heading(c.title,21));
            box.addView(text("NEDEN KAYDETTİM?",11,MUTED)); TextView reason=text(c.note,16,INK); reason.setMaxLines(3); reason.setEllipsize(TextUtils.TruncateAt.END); box.addView(reason);
            String progress=c.steps.isEmpty()?"İlk adımını ekle":c.completedSteps()+" / "+c.steps.size()+" adım tamamlandı";
            box.addView(text(c.done?"✓ Tamamlandı":progress,13,GREEN));
            box.addView(button("Kartı aç  →",false,()->detail(c.id)));
        }
        status.setText(store.isHealthy()?filter+" · "+shown+" kayıt · Cihazında saklanır":"Kayıt dosyası okunamadı. Değişiklik yapılamıyor.");
        if(shown==0) {
            LinearLayout empty=column(); empty.setPadding(dp(12),dp(30),dp(12),dp(20));
            empty.addView(heading(cards.isEmpty()?"İlk kaydınla başla.":"Burada henüz kayıt yok.",24));
            empty.addView(text(query.isEmpty()?"Instagram, TikTok veya YouTube'da Paylaş → SaveWhy. Niyetini seç, nedenini yaz, adımlarını belirle.":"Başka bir kelime veya niyet filtresi dene.",16,MUTED));
            list.addView(empty);
        }
    }
    private void detail(String id) {
        Card card=store.find(id); if(card==null) return;
        LinearLayout content=column(); content.setPadding(dp(20),dp(10),dp(20),dp(10));
        content.addView(text(card.why+" · "+UrlTools.source(card.url),13,GREEN));
        content.addView(heading("Neden kaydettim?",16)); content.addView(text(card.note,16,INK));
        content.addView(heading("Eylem adımları",16));
        if(card.steps.isEmpty()) content.addView(text("Düzenle'ye dokunarak ilk adımını ekle.",14,MUTED));
        for(int i=0;i<card.steps.size();i++) {
            final int index=i; CheckBox check=new CheckBox(this); check.setText(card.steps.get(i).text); check.setTextColor(INK);
            check.setMinHeight(dp(48)); check.setChecked(card.steps.get(i).done); content.addView(check);
            check.setOnClickListener(v-> {
                Card current=store.find(id); if(current==null) return;
                Card changed=current.copy(); changed.steps.get(index).done=check.isChecked();
                if(!save(changed)) check.setChecked(current.steps.get(index).done);
            });
        }
        content.addView(button("Orijinal içeriği aç ↗",true,()->openUrl(card.url)));
        if(card.createdAt>0) content.addView(text("Kaydedildi: "+new SimpleDateFormat("d MMM yyyy",Locale.forLanguageTag("tr")).format(new Date(card.createdAt)),12,MUTED));
        ScrollView scroll=new ScrollView(this); scroll.addView(content);
        AlertDialog dialog=new AlertDialog.Builder(this).setTitle(card.title).setView(scroll).setNegativeButton("Kapat",null)
                .setNeutralButton("Düzenle",(d,w)->editorFor(id)).setPositiveButton(card.done?"Geri al":"Tamamladım",(d,w)-> {
                    Card current=store.find(id); if(current!=null) { Card changed=current.copy(); changed.done=!changed.done; save(changed); }
                }).create();
        content.addView(button("Kaydı sil",false,()->new AlertDialog.Builder(this).setTitle("Kaydı sil?").setMessage(card.title)
                .setNegativeButton("Vazgeç",null).setPositiveButton("Sil",(d,w)-> {
                    try { store.delete(id); dialog.dismiss(); render(); } catch(Exception e) { error(e); }
                }).show()));
        dialog.show();
    }
    private boolean save(Card card) {
        try { store.save(card); render(); return true; } catch(Exception e) { error(e); return false; }
    }
    private void editorFor(String id) {
        Card c=store.find(id); if(c==null) return; Bundle b=new Bundle();
        b.putString("id",c.id); b.putString("url",c.url); b.putString("title",c.title); b.putString("note",c.note); b.putString("why",c.why);
        List<String> lines=new ArrayList<>(); for(Card.Step s:c.steps) lines.add(s.text); b.putString("steps",String.join("\n",lines)); showEditor(b);
    }
    private Bundle draft() {
        Bundle b=new Bundle(); b.putString("id",editingId); b.putString("url",editUrl.getText().toString());
        b.putString("title",editTitle.getText().toString()); b.putString("note",editNote.getText().toString());
        b.putString("why",editWhy.getSelectedItem().toString()); b.putString("steps",editSteps.getText().toString()); return b;
    }
    private void showEditor(Bundle b) {
        if(!store.isHealthy()) { toast("Kayıt dosyası okunamadığı için yeni kayıt açılamıyor."); return; }
        editingId=b.getString("id","");
        LinearLayout form=column(); form.setPadding(dp(20),dp(8),dp(20),dp(8));
        form.addView(text("İçerik bağlantısı",13,MUTED)); editUrl=input("https://…",b.getString("url",""),4096,false);
        editUrl.setInputType(android.text.InputType.TYPE_CLASS_TEXT|android.text.InputType.TYPE_TEXT_VARIATION_URI); form.addView(editUrl);
        form.addView(text("Başlık",13,MUTED)); editTitle=input("Örn. Hafta sonu deneyeceğim tarif",b.getString("title",""),160,false); form.addView(editTitle);
        form.addView(heading("Bunu ne için kaydediyorsun?",16)); editWhy=new Spinner(this);
        editWhy.setAdapter(new ArrayAdapter<>(this,android.R.layout.simple_spinner_dropdown_item,Card.INTENTS));
        editWhy.setSelection(Math.max(0,Arrays.asList(Card.INTENTS).indexOf(b.getString("why","Yap")))); form.addView(editWhy);
        form.addView(text("Kaydetme nedenin",13,MUTED)); editNote=input("Bununla ne yapmak istiyorsun?",b.getString("note",""),2000,true); form.addView(editNote);
        form.addView(text("Eylem adımları · her satıra bir adım",13,MUTED)); editSteps=input("İlk somut adımın ne?",b.getString("steps",""),10000,true); form.addView(editSteps);
        form.addView(button("Niyetime göre adım şablonu ekle",false,()-> {
            Runnable apply=()->editSteps.setText(Card.template(editWhy.getSelectedItem().toString()));
            if(editSteps.getText().toString().trim().isEmpty()) apply.run();
            else new AlertDialog.Builder(this).setTitle("Adımları şablonla değiştir?").setMessage("Yazdığın adımların yerine niyet şablonu gelecek.")
                    .setNegativeButton("Vazgeç",null).setPositiveButton("Değiştir",(d,w)->apply.run()).show();
        }));
        form.addView(text("Şablonlar başlangıç önerisidir. İçerik otomatik analiz edilmez.",12,MUTED));
        ScrollView scroll=new ScrollView(this); scroll.addView(form);
        editorDialog=new AlertDialog.Builder(this).setTitle(editingId.isEmpty()?"Neden kaydediyorsun?":"Kaydı düzenle")
                .setView(scroll).setNegativeButton("Vazgeç",null).setPositiveButton("Kaydet",null).create();
        editorDialog.setOnShowListener(d->editorDialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(v-> {
            try {
                String url=UrlTools.extract(editUrl.getText().toString());
                if(url.isEmpty()) { editUrl.setError("Geçerli bir http/https bağlantısı gir."); return; }
                if(editTitle.getText().toString().trim().isEmpty()) { editTitle.setError("Bir başlık ekle."); return; }
                if(editNote.getText().toString().trim().isEmpty()) { editNote.setError("Kaydetme nedenini ekle."); return; }
                Card previous=editingId.isEmpty()?null:store.find(editingId);
                List<Card.Step> steps=Card.parseSteps(editSteps.getText().toString(),previous==null?Collections.emptyList():previous.steps);
                Card card=new Card(previous==null?UUID.randomUUID().toString():previous.id,url,editTitle.getText().toString(),
                        editWhy.getSelectedItem().toString(),editNote.getText().toString(),previous==null?System.currentTimeMillis():previous.createdAt,
                        previous!=null&&previous.done,steps);
                if(save(card)) { editorDialog.dismiss(); toast("Nedeniyle birlikte kaydedildi."); }
            } catch(Exception e) { error(e); }
        })); editorDialog.show();
    }
    private void openUrl(String url) {
        try { startActivity(new Intent(Intent.ACTION_VIEW,Uri.parse(url))); }
        catch(ActivityNotFoundException e) { toast("Bağlantıyı açacak uygulama bulunamadı."); }
    }
    private void menu() {
        new AlertDialog.Builder(this).setTitle("SaveWhy").setItems(new String[]{"JSON yedeği oluştur","JSON yedeğini içe aktar","Uygulama hakkında"},(d,index)-> {
            if(index==0) exportBackup(); else if(index==1) importBackup();
            else new AlertDialog.Builder(this).setTitle("SaveWhy 0.2.0").setMessage("İçeriği niyetinle kaydet ve eyleme dönüştür.\n\nKayıtların bu cihazda saklanır. AI analizi, semantik arama ve bildirimler henüz eklenmedi.\n\nUygulamayı silmeden önce JSON yedeği oluştur.").setPositiveButton("Tamam",null).show();
        }).show();
    }
    private void exportBackup() {
        if(!store.isHealthy()) { toast("Kayıtlar okunamadı. Yedek oluşturulamıyor."); return; }
        Intent intent=new Intent(Intent.ACTION_CREATE_DOCUMENT).addCategory(Intent.CATEGORY_OPENABLE).setType("application/json");
        intent.putExtra(Intent.EXTRA_TITLE,"SaveWhy-"+new SimpleDateFormat("yyyy-MM-dd",Locale.ROOT).format(new Date())+".json");
        try { startActivityForResult(intent,EXPORT); } catch(ActivityNotFoundException e) { toast("Dosya seçici bulunamadı."); }
    }
    private void importBackup() {
        if(!store.isHealthy()) { toast("Kayıtlar okunamadı. Mevcut dosya korunuyor."); return; }
        Intent intent=new Intent(Intent.ACTION_OPEN_DOCUMENT).addCategory(Intent.CATEGORY_OPENABLE).setType("*/*");
        try { startActivityForResult(intent,IMPORT); } catch(ActivityNotFoundException e) { toast("Dosya seçici bulunamadı."); }
    }
    @Override protected void onActivityResult(int request,int result,Intent data) {
        super.onActivityResult(request,result,data);
        if(result!=RESULT_OK || data==null || data.getData()==null) return;
        Uri uri=data.getData();
        try {
            if(request==EXPORT) {
                byte[] bytes=store.backup().getBytes(StandardCharsets.UTF_8);
                try(OutputStream out=getContentResolver().openOutputStream(uri,"wt")) {
                    if(out==null) throw new IOException("Dosya açılamadı."); out.write(bytes);
                } toast("JSON yedeği oluşturuldu.");
            } else if(request==IMPORT) {
                List<Card> imported;
                try(InputStream in=getContentResolver().openInputStream(uri)) {
                    if(in==null) throw new IOException("Dosya açılamadı."); imported=CardCodec.decode(CardStore.read(in));
                }
                int additions=CardCodec.merge(store.all(),imported).size()-store.all().size();
                new AlertDialog.Builder(this).setTitle("Yedeği içe aktar?")
                        .setMessage(additions+" yeni kayıt eklenecek. Mevcut kayıtlar ve tamamlanma durumları korunur; aynı bağlantı ve niyet atlanır.")
                        .setNegativeButton("Vazgeç",null).setPositiveButton("İçe aktar",(d,w)-> {
                            try { int added=store.merge(imported); render(); toast(added+" kayıt eklendi."); } catch(Exception e) { error(e); }
                        }).show();
            }
        } catch(Exception e) { error(e); }
    }
    private void error(Exception e) { toast(e instanceof IllegalArgumentException?e.getMessage():"İşlem tamamlanamadı. Dosyayı ve kullanılabilir alanı kontrol et."); }
    private void toast(String message) { Toast.makeText(this,message,Toast.LENGTH_LONG).show(); }
}
