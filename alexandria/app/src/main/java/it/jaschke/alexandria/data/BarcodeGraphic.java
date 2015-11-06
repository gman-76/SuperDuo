package it.jaschke.alexandria.data;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.util.Log;

import com.google.android.gms.vision.barcode.Barcode;

/**
 * Created by Gerhard on 29/10/2015.
 */
public class BarcodeGraphic {

    private final String LOGTAG = this.getClass().getSimpleName();

    private Paint paint;
    private Rect scaledRect;

    public Barcode mBarCode;
    public int id;

    public BarcodeGraphic(Barcode bc){
        mBarCode = bc;
        paint = new Paint();
        paint.setColor(Color.CYAN);
        paint.setStyle(Paint.Style.STROKE);
        paint.setStrokeWidth(4);
    }

    public void Draw(Canvas canvas,float xScale, float yScale){
        Rect box = mBarCode.getBoundingBox();
        scaledRect = new Rect((int)(box.left*xScale),
                (int)(box.top*yScale),
                (int)(box.right*xScale),
                (int)(box.bottom*yScale));
        canvas.drawRect(scaledRect,paint);
    }

    public Barcode getCode(float x,float y){
        if(scaledRect.contains((int)x,(int)y)) return mBarCode;
        return null;
    }
}
