package com.savewhy.app;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.os.Looper;
import android.widget.ImageView;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.RuntimeEnvironment;
import org.robolectric.annotation.Config;
import org.robolectric.annotation.GraphicsMode;
import java.io.*;
import java.util.concurrent.atomic.AtomicReference;
import static org.junit.Assert.*;
import static org.robolectric.Shadows.*;
@RunWith(RobolectricTestRunner.class) @Config(sdk=28) @GraphicsMode(GraphicsMode.Mode.NATIVE)
public class ThumbnailLoaderTest {
    private byte[] image()throws Exception{Bitmap bitmap=Bitmap.createBitmap(320,180,Bitmap.Config.ARGB_8888);ByteArrayOutputStream out=new ByteArrayOutputStream();bitmap.compress(Bitmap.CompressFormat.JPEG,85,out);return out.toByteArray();}
    private void await(java.util.function.BooleanSupplier ready)throws Exception{long end=System.currentTimeMillis()+5000;while(!ready.getAsBoolean()&&System.currentTimeMillis()<end){Thread.sleep(20);shadowOf(Looper.getMainLooper()).idle();}assertTrue(ready.getAsBoolean());}
    @Test public void cachedRealImagesBindForEverySocialSource()throws Exception{
        android.content.Context context=RuntimeEnvironment.getApplication();File directory=new File(context.getCacheDir(),"thumbnails");directory.mkdirs();
        try(ThumbnailLoader loader=new ThumbnailLoader(context)){
            for(String url:new String[]{"https://youtu.be/abcDEF123_-","https://www.instagram.com/reel/thumbtest/","https://www.facebook.com/watch/?v=456","https://www.tiktok.com/@demo/video/456"}){
                try(FileOutputStream out=new FileOutputStream(new File(directory,SocialPreview.key(url)+".jpg"))){out.write(image());}
                ImageView view=new ImageView(context);loader.bind(view,url,R.drawable.ic_watch);await(()->view.getDrawable() instanceof BitmapDrawable);assertEquals(ImageView.ScaleType.CENTER_CROP,view.getScaleType());assertNull(view.getImageTintList());
            }
        }
    }
    @Test public void selectedThumbnailPersistsAndWorksWithoutPlatformAccess()throws Exception{
        android.content.Context context=RuntimeEnvironment.getApplication();String url="https://www.instagram.com/private/selected-test";AtomicReference<Boolean> result=new AtomicReference<>();byte[] bytes=image();
        try(ThumbnailLoader loader=new ThumbnailLoader(context)){loader.select(url,()->new ByteArrayInputStream(bytes),result::set);await(()->result.get()!=null);assertTrue(result.get());}
        try(ThumbnailLoader loader=new ThumbnailLoader(context)){ImageView view=new ImageView(context);loader.bind(view,url,R.drawable.ic_watch);await(()->view.getDrawable() instanceof BitmapDrawable);assertEquals(ImageView.ScaleType.CENTER_CROP,view.getScaleType());}
    }
    @Test public void invalidSelectionCannotCreateThumbnail()throws Exception{
        android.content.Context context=RuntimeEnvironment.getApplication();String url="https://facebook.com/watch/?v=badtest";AtomicReference<Boolean> result=new AtomicReference<>();
        try(ThumbnailLoader loader=new ThumbnailLoader(context)){loader.select(url,()->new ByteArrayInputStream(new byte[]{1,2,3}),result::set);await(()->result.get()!=null);assertFalse(result.get());assertFalse(new File(context.getFilesDir(),"previews/"+SocialPreview.key(url)+".jpg").exists());}
    }
}
