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
        TextView skip=new TextView(this); skip.setText(I18n.text(this,"Devam etmek için dokun")); skip.setTextColor(Color.rgb(160,170,165)); skip.setTextSize(12); skip.setTypeface(font); skip.setPadding(0,dp(32),0,0); root.addView(skip);
        root.setFocusable(true); root.setClickable(true); root.setContentDescription(I18n.text(this,"Devam etmek için dokun")); root.setOnClickListener(v->open.run());
        setContentView(root);
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
