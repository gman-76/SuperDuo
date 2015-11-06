package it.jaschke.alexandria.views;

import android.content.Context;
import android.graphics.Canvas;
import android.util.AttributeSet;
import android.view.View;

import com.google.android.gms.vision.barcode.Barcode;
import it.jaschke.alexandria.data.BarcodeGraphic;

import java.util.HashSet;
import java.util.Set;

/**
 * Created by Gerhard on 29/10/2015.
 */
public class Overlay extends View {

    private static final Object mLock =  new Object();

    private HashSet<BarcodeGraphic> graphics = new HashSet<>();
    private BarcodeGraphic firstGraphic;
    private int mPreviewWidth;
    private int mPreviewHeight;
    private int mFrontBack;

    public Overlay(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        synchronized (mLock){
            float xscale = 1.0f;
            float yscale = 1.0f;
            if(mPreviewWidth!=0 && mPreviewHeight!=0){
                xscale = (float)canvas.getWidth() / (float)mPreviewWidth;
                yscale = (float)canvas.getHeight() / (float)mPreviewHeight;
            }
            for(BarcodeGraphic bcg:graphics){
                bcg.Draw(canvas,xscale,yscale);
            }
        }
    }

    public void clear(){
        synchronized (mLock){
            graphics.clear();
            firstGraphic = null;
        }
        postInvalidate();
    }

    public void setCameraInfo(int previewWidth, int previewHeight, int cameraFrontBack) {
        synchronized (mLock) {
            mPreviewWidth = previewWidth;
            mPreviewHeight = previewHeight;
            mFrontBack = cameraFrontBack;
        }
        postInvalidate();
    }

    public void add(BarcodeGraphic bcg){
        synchronized (mLock){
            graphics.add(bcg);
            if(firstGraphic==null) firstGraphic=bcg;
        }
        postInvalidate();
    }

    public void delete(BarcodeGraphic bcg){
        synchronized (mLock){
            graphics.remove(bcg);
            if(firstGraphic!=null && firstGraphic.equals(bcg)) firstGraphic=null;
        }
        postInvalidate();
    }

    public Barcode getCodeFromTouch(float x,float y){
        synchronized (mLock) {
            if (graphics.size() > 0) {
                for (BarcodeGraphic bcg : graphics) {
                    //find the first graphic in which we fall and return
                    Barcode r = bcg.getCode(x, y);
                    if (r != null) return r;
                }
            }
        }
        return null;
    }
}
