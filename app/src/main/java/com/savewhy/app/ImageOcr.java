package com.savewhy.app;
import android.content.Context;
import android.graphics.Bitmap;
import android.os.Handler;
import android.os.Looper;
import com.google.mlkit.vision.common.InputImage;
import com.google.mlkit.vision.text.TextRecognition;
import com.google.mlkit.vision.text.TextRecognizer;
import com.google.mlkit.vision.text.latin.TextRecognizerOptions;
import com.googlecode.tesseract.android.TessBaseAPI;
import java.io.*;
import java.util.concurrent.*;
import java.util.function.BiConsumer;
/** Bundled Turkish + English OCR, with Latin fallback. No network calls. */
class ImageOcr implements AutoCloseable {
    private TextRecognizer recognizer;
    private final Context context;
    private final ExecutorService worker=Executors.newSingleThreadExecutor();
    private final Handler main=new Handler(Looper.getMainLooper());
    private volatile boolean closed;
    ImageOcr(){context=null;}
    ImageOcr(Context context){this.context=context.getApplicationContext();}
    void read(Bitmap bitmap,BiConsumer<String,Boolean> result){
        if(closed){bitmap.recycle();return;}
        if(context==null){readLatin(bitmap,result);return;}
        try{worker.execute(()->{
            String text="";TessBaseAPI tess=null;
            try{
                File root=new File(context.getFilesDir(),"ocr"),data=new File(root,"tessdata");
                if(!data.isDirectory()&&!data.mkdirs())throw new IOException("OCR directory");
                for(String language:new String[]{"tur","eng"}){
                    File destination=new File(data,language+".traineddata");
                    if(!destination.isFile()){
                        File temp=new File(data,language+".tmp");
                        try(InputStream in=context.getAssets().open("tessdata/"+language+".traineddata");OutputStream out=new FileOutputStream(temp)){byte[] buffer=new byte[8192];int count;while((count=in.read(buffer))!=-1)out.write(buffer,0,count);}
                        if(!temp.renameTo(destination))throw new IOException("OCR model installation");
                    }
                }
                tess=new TessBaseAPI();if(!tess.init(root.getAbsolutePath(),"tur+eng"))throw new IOException("OCR initialization");
                tess.setPageSegMode(TessBaseAPI.PageSegMode.PSM_AUTO);tess.setImage(bitmap);String value=tess.getUTF8Text();text=value==null?"":value.trim();
            }catch(Exception|LinkageError ignored){}finally{if(tess!=null)tess.recycle();}
            final String value=text;main.post(()->{if(closed){bitmap.recycle();return;}if(value.isEmpty())readLatin(bitmap,result);else{bitmap.recycle();result.accept(limit(value),true);}});
        });}catch(RejectedExecutionException ignored){bitmap.recycle();}
    }
    private void readLatin(Bitmap bitmap,BiConsumer<String,Boolean> result){
        try{if(recognizer==null)recognizer=TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS);recognizer.process(InputImage.fromBitmap(bitmap,0)).addOnSuccessListener(text->result.accept(limit(text.getText()),true)).addOnFailureListener(error->result.accept("",false)).addOnCompleteListener(task->bitmap.recycle());}catch(Exception|LinkageError e){bitmap.recycle();result.accept("",false);}
    }
    private static String limit(String value){return value.substring(0,Math.min(4000,value.length()));}
    public void close(){closed=true;worker.shutdown();if(recognizer!=null)recognizer.close();}
}
