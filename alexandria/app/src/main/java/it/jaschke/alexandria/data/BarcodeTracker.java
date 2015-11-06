package it.jaschke.alexandria.data;

import android.util.Log;

import com.google.android.gms.vision.Detector;
import com.google.android.gms.vision.Tracker;
import com.google.android.gms.vision.barcode.Barcode;
import it.jaschke.alexandria.views.Overlay;

/**
 * Created by Gerhard on 29/10/2015.
 */
public class BarcodeTracker extends Tracker<Barcode> {

    private final String LOGTAG = this.getClass().getSimpleName();

    Overlay mOverlay;
    BarcodeGraphic mGraphic;

    public BarcodeTracker(Overlay overlay, BarcodeGraphic graphic){
        mOverlay = overlay;
        mGraphic = graphic;
    }

    @Override
    public void onNewItem(int id, Barcode item) {
        Log.d(LOGTAG,"New barcode item detected");
        mGraphic.id = id;
    }

    @Override
    public void onUpdate(Detector.Detections<Barcode> detections, Barcode item) {
        mOverlay.add(mGraphic);
        mGraphic.mBarCode = item;
    }

    @Override
    public void onMissing(Detector.Detections<Barcode> detections) {
        mOverlay.delete(mGraphic);
    }

    @Override
    public void onDone() {
        mOverlay.delete(mGraphic);
    }
}
