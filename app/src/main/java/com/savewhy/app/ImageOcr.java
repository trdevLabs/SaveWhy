package com.savewhy.app;
import android.graphics.Bitmap;
import com.google.mlkit.vision.common.InputImage;
import com.google.mlkit.vision.text.TextRecognition;
import com.google.mlkit.vision.text.TextRecognizer;
import com.google.mlkit.vision.text.latin.TextRecognizerOptions;
import java.util.function.BiConsumer;
/** Bundled Latin-script model: images never go to a cloud OCR service. */
class ImageOcr implements AutoCloseable {
    private TextRecognizer recognizer;
    void read(Bitmap bitmap,BiConsumer<String,Boolean> result){
        try{if(recognizer==null)recognizer=TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS);recognizer.process(InputImage.fromBitmap(bitmap,0)).addOnSuccessListener(text->{String value=text.getText();result.accept(value.substring(0,Math.min(4000,value.length())),true);}).addOnFailureListener(error->result.accept("",false)).addOnCompleteListener(task->bitmap.recycle());}catch(Exception|LinkageError e){bitmap.recycle();result.accept("",false);}
    }
    public void close(){if(recognizer!=null)recognizer.close();}
}
