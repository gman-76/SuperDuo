package it.jaschke.alexandria;

import android.app.Activity;
import android.content.Intent;
import android.support.v4.app.Fragment;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;

import com.google.android.gms.common.api.CommonStatusCodes;
import com.google.android.gms.vision.MultiProcessor;
import com.google.android.gms.vision.barcode.Barcode;
import com.google.android.gms.vision.barcode.BarcodeDetector;
import it.jaschke.alexandria.data.BarcodeTrackerFactory;
import it.jaschke.alexandria.ui.camera.CameraHolder;
import it.jaschke.alexandria.views.CameraPreview;
import it.jaschke.alexandria.views.Overlay;

/**
 * Created by Gerhard on 31/10/2015.
 */
public class ScanCode extends Activity {

    private final String LOGTAG = this.getClass().getSimpleName();

    public static final String BARCODE_RESULT = "BARCODE_RESULT";

    CameraHolder mCameraHolder;
    CameraPreview mPreview;
    Overlay mOverlay;

    public ScanCode() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.fragment_capture);

        Log.d(LOGTAG,"Creating capture fragment and opening camera");

        mPreview = (CameraPreview)findViewById(R.id.preview);
        mOverlay = (Overlay)findViewById(R.id.overlay);

        BarcodeDetector detector = new BarcodeDetector.Builder(this).build();
        BarcodeTrackerFactory factory = new BarcodeTrackerFactory(mOverlay);
        detector.setProcessor(new MultiProcessor.Builder<>(factory).build());

        mCameraHolder = new CameraHolder(this,detector);

        mPreview.setOnTouchListener(new View.OnTouchListener(){
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if(event.getAction()==MotionEvent.ACTION_DOWN){
                    Log.d(LOGTAG,"Touched at x,y : " + String.format("%d,%d",(int)event.getX(),(int)event.getY()));
                    Barcode code = mOverlay.getCodeFromTouch(event.getX(),event.getY());
                    if(code!=null){
                        Log.d(LOGTAG,"Found code " + code.displayValue + " at this position");
                        Intent i = new Intent();
                        i.putExtra(ScanCode.BARCODE_RESULT,code);
                        setResult(CommonStatusCodes.SUCCESS, i);
                        finish();
                    }else{
                        Log.d(LOGTAG,"No code at this position");
                    }
                }
                return true;
            }
        });
    }

    @Override
    public void onResume() {
        super.onResume();
        if(mPreview!=null){
            mPreview.start(mCameraHolder,mOverlay);
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        if(mPreview!=null){
            mPreview.stop();
        }
    }

    @Override
    public void onDestroy() {
        if(mPreview!=null){
            mPreview.release();
        }
        super.onDestroy();
    }

}
