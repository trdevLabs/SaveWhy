package com.savewhy.app;

import androidx.appcompat.app.AppCompatActivity;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.SystemClock;
import android.view.Gravity;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import androidx.core.content.res.ResourcesCompat;

/** Brief launch artwork; incoming shares go directly to MainActivity. */
public class SplashActivity extends AppCompatActivity {
    private final Handler handler = new Handler(Looper.getMainLooper());
    private long started;
    private boolean navigated;
    private final Runnable open = () -> {
        if (isFinishing() || navigated) return;
        navigated = true;
        startActivity(new Intent(this, MainActivity.class));
        finish();
    };
    @Override public void onCreate(Bundle state) {
        super.onCreate(state);
        started = state == null ? SystemClock.elapsedRealtime() : state.getLong("started", SystemClock.elapsedRealtime());
        getWindow().setStatusBarColor(Color.BLACK);
        getWindow().setNavigationBarColor(Color.BLACK);
        getWindow().getDecorView().setSystemUiVisibility(0);
        android.widget.FrameLayout frame=new android.widget.FrameLayout(this);frame.setBackgroundColor(Color.BLACK);
        LinearLayout root = new LinearLayout(this);
        root.setOrientation(LinearLayout.VERTICAL);
        root.setGravity(Gravity.CENTER);
        root.setBackgroundColor(Color.BLACK);
        int pad = dp(24); root.setPadding(pad, pad, pad, pad);
        ImageView art = new ImageView(this);
        art.setImageResource(R.drawable.splash_art);
        art.setScaleType(ImageView.ScaleType.FIT_CENTER);
        art.setImportantForAccessibility(android.view.View.IMPORTANT_FOR_ACCESSIBILITY_NO);
        root.addView(art, new LinearLayout.LayoutParams(dp(260), dp(300)));
        Typeface font = ResourcesCompat.getFont(this, R.font.manrope_family);
        TextView name = new TextView(this);
        name.setText("SaveWhy"); name.setTextSize(36); name.setTextColor(Color.WHITE);
        name.setTypeface(font, Typeface.BOLD); name.setGravity(Gravity.CENTER);
        root.addView(name);
        TextView tagline = new TextView(this);
        tagline.setText(I18n.text(this,"Kaydet. Nedenini unutma.")); tagline.setTextSize(15);
        tagline.setTextColor(Color.rgb(148, 230, 195)); tagline.setTypeface(font);
        tagline.setGravity(Gravity.CENTER); tagline.setPadding(0, dp(12), 0, 0);
        root.addView(tagline);
        frame.addView(root,new android.widget.FrameLayout.LayoutParams(-1,-1));
        TextView copyright=new TextView(this);copyright.setId(R.id.splash_copyright);copyright.setText("Copy Right 2026");copyright.setTextSize(12);copyright.setTypeface(font);copyright.setTextColor(Color.rgb(150,165,158));copyright.setGravity(Gravity.CENTER);copyright.setPadding(0,dp(8),0,dp(16));frame.addView(copyright,new android.widget.FrameLayout.LayoutParams(-1,-2,Gravity.BOTTOM));
        frame.setFocusable(true);frame.setClickable(true);frame.setContentDescription(I18n.text(this,"Devam etmek için dokun"));frame.setOnClickListener(v->open.run());
        androidx.core.view.ViewCompat.setOnApplyWindowInsetsListener(frame,(v,insets)->{androidx.core.graphics.Insets bars=insets.getInsets(androidx.core.view.WindowInsetsCompat.Type.systemBars()|androidx.core.view.WindowInsetsCompat.Type.displayCutout());v.setPadding(bars.left,bars.top,bars.right,bars.bottom);return insets;});
        setContentView(frame);frame.post(()->androidx.core.view.ViewCompat.requestApplyInsets(frame));
        if (state == null && android.provider.Settings.Global.getFloat(getContentResolver(), android.provider.Settings.Global.ANIMATOR_DURATION_SCALE, 1f) > 0) {
            art.setAlpha(0f); art.animate().alpha(1f).setDuration(250).start();
        }
    }
    @Override protected void onResume() {
        super.onResume(); handler.postDelayed(open, Math.max(0, 2200 - (SystemClock.elapsedRealtime() - started)));
    }
    @Override protected void onPause() { handler.removeCallbacks(open); super.onPause(); }
    @Override protected void onSaveInstanceState(Bundle state) { state.putLong("started", started); super.onSaveInstanceState(state); }
    @Override protected void onDestroy() { handler.removeCallbacksAndMessages(null); super.onDestroy(); }
    private int dp(int value) { return Math.round(value * getResources().getDisplayMetrics().density); }
}
