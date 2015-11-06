package it.jaschke.alexandria.data;

import android.util.Log;

import com.google.android.gms.vision.MultiProcessor;
import com.google.android.gms.vision.Tracker;
import com.google.android.gms.vision.barcode.Barcode;
import it.jaschke.alexandria.views.Overlay;

/**
 * Created by Gerhard on 29/10/2015.
 */
public class BarcodeTrackerFactory implements MultiProcessor.Factory<Barcode>{

    private final String LOGTAG = this.getClass().getSimpleName();

    Overlay mOverlay;

    public BarcodeTrackerFactory(Overlay overlay){
        mOverlay = overlay;
    }

    @Override
    public Tracker<Barcode> create(Barcode barcode) {
        Log.d(LOGTAG,"Barcode Factory creating new graphic with barcode");
        BarcodeGraphic graphic = new BarcodeGraphic(barcode);
        return new BarcodeTracker(mOverlay,graphic);
    }
}
