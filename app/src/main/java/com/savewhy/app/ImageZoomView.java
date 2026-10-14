package com.savewhy.app;
import android.content.Context;
import android.graphics.*;
import android.view.*;
/** Fit-to-screen with bounded pinch zoom and drag. */
final class ImageZoomView extends View {
    private Bitmap bitmap;private float zoom=1,dx,dy,x,y;private final Paint paint=new Paint(Paint.ANTI_ALIAS_FLAG|Paint.FILTER_BITMAP_FLAG);private final ScaleGestureDetector scale;
    ImageZoomView(Context context){super(context);setBackgroundColor(Color.BLACK);scale=new ScaleGestureDetector(context,new ScaleGestureDetector.SimpleOnScaleGestureListener(){public boolean onScale(ScaleGestureDetector d){zoom=Math.max(1,Math.min(5,zoom*d.getScaleFactor()));clamp();invalidate();return true;}});}
    void image(Bitmap b){if(bitmap!=null&&bitmap!=b)bitmap.recycle();bitmap=b;zoom=1;dx=dy=0;invalidate();}
    @Override protected void onDetachedFromWindow(){if(bitmap!=null){bitmap.recycle();bitmap=null;}super.onDetachedFromWindow();}
    private float fit(){return bitmap==null?1:Math.min((float)getWidth()/bitmap.getWidth(),(float)getHeight()/bitmap.getHeight());}
    private void clamp(){if(bitmap==null)return;float s=fit()*zoom;float mx=Math.max(0,(bitmap.getWidth()*s-getWidth())/2),my=Math.max(0,(bitmap.getHeight()*s-getHeight())/2);dx=Math.max(-mx,Math.min(mx,dx));dy=Math.max(-my,Math.min(my,dy));}
    protected void onSizeChanged(int w,int h,int oldw,int oldh){clamp();}
    protected void onDraw(Canvas canvas){super.onDraw(canvas);if(bitmap==null)return;float s=fit()*zoom;canvas.save();canvas.translate((getWidth()-bitmap.getWidth()*s)/2+dx,(getHeight()-bitmap.getHeight()*s)/2+dy);canvas.scale(s,s);canvas.drawBitmap(bitmap,0,0,paint);canvas.restore();}
    public boolean onTouchEvent(MotionEvent event){scale.onTouchEvent(event);if(event.getActionMasked()==MotionEvent.ACTION_DOWN){x=event.getX();y=event.getY();return true;}if(event.getActionMasked()==MotionEvent.ACTION_MOVE&&!scale.isInProgress()&&event.getPointerCount()==1){dx+=event.getX()-x;dy+=event.getY()-y;clamp();invalidate();}x=event.getX();y=event.getY();if(event.getActionMasked()==MotionEvent.ACTION_UP)performClick();return true;}
    public boolean performClick(){return super.performClick();}
}
