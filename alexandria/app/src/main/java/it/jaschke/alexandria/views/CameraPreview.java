package it.jaschke.alexandria.views;

import android.content.Context;
import android.content.res.Configuration;
import android.util.AttributeSet;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.ViewGroup;
import android.view.WindowManager;

import com.google.android.gms.common.images.Size;
import it.jaschke.alexandria.ui.camera.CameraHolder;

/**
 * Created by Gerhard on 29/10/2015.
 */
public class CameraPreview extends ViewGroup {

    private final String LOGTAG = this.getClass().getSimpleName();

    private Context mContext;
    private CameraHolder mCameraHolder;
    private Overlay mOverlay;
    private SurfaceView mSurfaceView;
    private boolean hasSurface;
    private boolean requestedStart;

    public CameraPreview(Context context, AttributeSet attrs) {
        super(context, attrs);
        mContext = context;
        hasSurface = false;
        requestedStart=false;

        mSurfaceView = new SurfaceView(context);
        mSurfaceView.getHolder().addCallback(new SurfaceCallbacks());
        addView(mSurfaceView);
    }

    public void start(CameraHolder cameraHolder,Overlay overlay){
        mOverlay = overlay;
        if(cameraHolder==null){
            stop();
            return;
        }
        mCameraHolder = cameraHolder;
        requestedStart=true;
        Log.d(LOGTAG,"Requested start via start()");
        AttemptStart();
    }

    public void stop(){
        if(mCameraHolder!=null){
            mCameraHolder.stop();
        }
    }

    public void release(){
        if(mCameraHolder!=null){
            mCameraHolder.release();
            mCameraHolder = null;
        }
    }

    private void AttemptStart(){
        if(hasSurface && requestedStart){
            Log.d(LOGTAG,"Attempting start of preview");
            mCameraHolder.start(mSurfaceView.getHolder());
            if(mOverlay!=null){
                Size size = mCameraHolder.getPreviewSize();
                int min = Math.min(size.getWidth(), size.getHeight());
                int max = Math.max(size.getWidth(), size.getHeight());
                if (isPortraitMode()) {
                    // Swap width and height sizes when in portrait, since it will be rotated by
                    // 90 degrees
                    mOverlay.setCameraInfo(min, max, mCameraHolder.getCameraFrontBack());
                } else {
                    mOverlay.setCameraInfo(max, min, mCameraHolder.getCameraFrontBack());
                }
                mOverlay.clear();
            }
            requestedStart=false;
        }
    }

    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b) {
        int width = 320;
        int height = 240;
        if (mCameraHolder != null) {
            Size size = mCameraHolder.getPreviewSize();
            if (size != null) {
                width = size.getWidth();
                height = size.getHeight();
            }
        }
        // Swap width and height sizes when in portrait, since it will be rotated 90 degrees
        if (isPortraitMode()) {
            int tmp = width;
            //noinspection SuspiciousNameCombination
            width = height;
            height = tmp;
        }

        final int layoutWidth = r - l;
        final int layoutHeight = b - t;

        // Computes height and width for potentially doing fit width.
        int childWidth = layoutWidth;
        int childHeight = (int)(((float) layoutWidth / (float) width) * height);

        // If height is too tall using fit width, does fit height instead.
        if (childHeight > layoutHeight) {
            childHeight = layoutHeight;
            childWidth = (int)(((float) layoutHeight / (float) height) * width);
        }

        for (int i = 0; i < getChildCount(); ++i) {
            getChildAt(i).layout(0, 0, childWidth, childHeight);
        }
        AttemptStart();
    }

    private boolean isPortraitMode(){
        return mContext.getResources().getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT;
    }

    private class SurfaceCallbacks implements SurfaceHolder.Callback{

        @Override
        public void surfaceCreated(SurfaceHolder holder) {
            hasSurface=true;
            Log.d(LOGTAG,"Surface created");
            AttemptStart();
        }

        @Override
        public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {

        }

        @Override
        public void surfaceDestroyed(SurfaceHolder holder) {
            hasSurface=false;
        }
    }
}
