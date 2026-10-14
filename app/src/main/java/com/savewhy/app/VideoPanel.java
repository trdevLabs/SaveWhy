package com.savewhy.app;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.graphics.Color;
import android.view.*;
import android.webkit.*;
import android.widget.*;
import com.google.android.material.button.MaterialButton;
import java.util.*;

/** The official player loads only after a user tap; no bridge, scraping or background playback. */
public final class VideoPanel extends FrameLayout {
    private final String id,url;private final ImageView cover;private final MaterialButton play;private WebView web;
    public VideoPanel(Context context,String url,ThumbnailLoader thumbnails){
        super(context);this.url=url;id=VideoLinks.youtubeId(url);setBackgroundColor(Color.BLACK);
        cover=new ImageView(context);cover.setScaleType(ImageView.ScaleType.CENTER_CROP);cover.setImportantForAccessibility(View.IMPORTANT_FOR_ACCESSIBILITY_NO);addView(cover,new LayoutParams(-1,-1));thumbnails.bind(cover,url,R.drawable.ic_watch);
        play=new MaterialButton(context);play.setText(I18n.text(context,"Videoyu oynat"));play.setIconResource(R.drawable.ic_watch);play.setAllCaps(false);play.setOnClickListener(v->play());LayoutParams p=new LayoutParams(-2,-2,Gravity.CENTER);addView(play,p);
    }
    public void play(){
        if(id.isEmpty()||web!=null)return;
        try {
            WebView candidate=new WebView(getContext());web=candidate;WebSettings settings=web.getSettings();settings.setJavaScriptEnabled(true);settings.setDomStorageEnabled(true);settings.setAllowFileAccess(false);settings.setAllowContentAccess(false);settings.setMixedContentMode(WebSettings.MIXED_CONTENT_NEVER_ALLOW);settings.setMediaPlaybackRequiresUserGesture(false);
            web.setBackgroundColor(Color.BLACK);web.setWebChromeClient(new WebChromeClient());
            web.setWebViewClient(new WebViewClient(){
                @Override public boolean shouldOverrideUrlLoading(WebView view,WebResourceRequest request){if(!request.isForMainFrame())return false;String host=request.getUrl().getHost();if("https".equals(request.getUrl().getScheme())&&("www.youtube.com".equals(host)||"youtube.com".equals(host))&&request.getUrl().getPath()!=null&&request.getUrl().getPath().startsWith("/embed/"))return false;try{getContext().startActivity(new Intent(Intent.ACTION_VIEW,Uri.parse(url)));}catch(Exception ignored){}return true;}
                @Override public void onReceivedError(WebView view,WebResourceRequest request,WebResourceError error){if(request.isForMainFrame())post(()->{stop();Toast.makeText(getContext(),I18n.text(getContext(),"Video yüklenemedi. Kaynağında açabilirsin."),Toast.LENGTH_LONG).show();});}
            });
            cover.setVisibility(GONE);play.setVisibility(GONE);addView(web,new LayoutParams(-1,-1));requestRectangleOnScreen(new android.graphics.Rect(0,0,getWidth(),getHeight()),true);
            web.loadUrl(VideoLinks.embed(id),Collections.singletonMap("Referer","https://"+getContext().getPackageName()));
        }catch(Exception e){stop();Toast.makeText(getContext(),I18n.text(getContext(),"Video yüklenemedi. Kaynağında açabilirsin."),Toast.LENGTH_LONG).show();}
    }
    public void stop(){if(web!=null){WebView old=web;web=null;old.stopLoading();old.loadUrl("about:blank");old.onPause();removeView(old);old.destroy();}cover.setVisibility(VISIBLE);play.setVisibility(VISIBLE);}
}
