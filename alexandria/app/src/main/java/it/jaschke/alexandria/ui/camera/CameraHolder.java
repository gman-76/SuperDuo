package it.jaschke.alexandria.ui.camera;

import android.content.Context;
import android.graphics.ImageFormat;
import android.hardware.Camera;
import android.util.Log;
import android.view.Surface;
import android.view.SurfaceHolder;
import android.view.View;
import android.view.WindowManager;

import com.google.android.gms.common.images.Size;
import com.google.android.gms.vision.Detector;
import it.jaschke.alexandria.data.FrameProcessor;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by Gerhard on 29/10/2015.
 */
public class CameraHolder {
    private final String LOGTAG = this.getClass().getSimpleName();

    private final int CAMERA_FACING = Camera.CameraInfo.CAMERA_FACING_BACK;

    private static final Object mCameraLock = new Object();

    private Context mContext;
    private Thread mProcessingThread;
    private FrameProcessor mFrameProcessor;
    private Size mPreviewSize;

    public Camera mCamera;
    public int mRotation;
    public Map<byte[], ByteBuffer> mBytesToByteBuffer = new HashMap<>();

    public CameraHolder(Context context,Detector<?> detector){
        mContext = context;
        mFrameProcessor = new FrameProcessor(detector,this);
    }

    public int getCameraFrontBack(){
        return CAMERA_FACING;
    }

    public Size getPreviewSize(){
        return mPreviewSize;
    }

    private class SizePair{
        public SizePair(android.hardware.Camera.Size pic, android.hardware.Camera.Size prev){
            pictureSize = new Size(pic.width,pic.height);
            previewSize = new Size(prev.width,prev.height);
        }
        public Size pictureSize;
        public Size previewSize;
    }

    public void start(SurfaceHolder holder){
        synchronized (mCameraLock){
            mCamera = Camera.open(CAMERA_FACING);
            Camera.Parameters params = mCamera.getParameters();

            List<Camera.Size> supportedPreviewSizes = params.getSupportedPreviewSizes();
            List<android.hardware.Camera.Size> supportedPictureSizes = params.getSupportedPictureSizes();
            List<SizePair> validPreviewSizes = new ArrayList<>();
            for (android.hardware.Camera.Size previewSize : supportedPreviewSizes) {
                float previewAspectRatio = (float) previewSize.width / (float) previewSize.height;
                // By looping through the picture sizes in order, we favor the higher resolutions.
                // We choose the highest resolution in order to support taking the full resolution
                // picture later.
                for (android.hardware.Camera.Size pictureSize : supportedPictureSizes) {
                    float pictureAspectRatio = (float) pictureSize.width / (float) pictureSize.height;
                    if (Math.abs(previewAspectRatio - pictureAspectRatio) < 0.01) {
                        validPreviewSizes.add(new SizePair(pictureSize, previewSize));
                        break;
                    }
                }
            }
            // If there are no picture sizes with the same aspect ratio as any preview sizes, allow all
            // of the preview sizes and hope that the camera can handle it.  Probably unlikely, but we
            // still account for it.
            if (validPreviewSizes.size() == 0) {
                Log.w(LOGTAG, "No preview sizes have a corresponding same-aspect-ratio picture size");
                for (android.hardware.Camera.Size previewSize : supportedPreviewSizes) {
                    // The null picture size will let us know that we shouldn't set a picture size.
                    validPreviewSizes.add(new SizePair(previewSize, null));
                }
            }

            int desiredWidth = 1600;
            int desiredHeight = 1024;
            SizePair selectedPair = null;
            int minDiff = Integer.MAX_VALUE;
            for (SizePair sizePair : validPreviewSizes) {
                Size size = sizePair.previewSize;
                int diff = Math.abs(size.getWidth() - desiredWidth) +
                        Math.abs(size.getHeight() - desiredHeight);
                if (diff < minDiff) {
                    selectedPair = sizePair;
                    minDiff = diff;
                }
            }
            mPreviewSize = selectedPair.previewSize;

            mCamera.setPreviewCallbackWithBuffer(new CameraCallBacks());
            params.setPreviewFormat(ImageFormat.NV21);
            params.setPictureSize(selectedPair.pictureSize.getWidth(), selectedPair.pictureSize.getHeight());
            params.setPreviewSize(mPreviewSize.getWidth(), mPreviewSize.getHeight());
            params.setFocusMode(Camera.Parameters.FOCUS_MODE_CONTINUOUS_PICTURE);
            setRotation(params);
            mCamera.setParameters(params);

            mCamera.addCallbackBuffer(createPreviewBuffer(mPreviewSize));
            mCamera.addCallbackBuffer(createPreviewBuffer(mPreviewSize));
            mCamera.addCallbackBuffer(createPreviewBuffer(mPreviewSize));
            mCamera.addCallbackBuffer(createPreviewBuffer(mPreviewSize));

            try {
                mCamera.setPreviewDisplay(holder);
                mCamera.startPreview();
            }catch(IOException e){
                Log.e(LOGTAG,"Unable to start camera preview: " + e.getMessage());
            }

            mProcessingThread = new Thread(mFrameProcessor);
            mFrameProcessor.setActive(true);
            mProcessingThread.start();
        }
    }

    public void stop(){
        synchronized (mCameraLock){
            mFrameProcessor.setActive(false);
            if(mProcessingThread!=null){
                try{
                    mProcessingThread.join();
                }catch(InterruptedException e){
                    Log.e(LOGTAG,"Exception waiting for processing thread to finish: " + e.getMessage());
                }
                mProcessingThread = null;
            }
            mBytesToByteBuffer.clear();
            if(mCamera!=null){
                try {
                    mCamera.stopPreview();
                    mCamera.setPreviewCallbackWithBuffer(null);
                    mCamera.setPreviewDisplay(null);
                    mCamera.release();
                    mCamera = null;
                }catch(IOException e){
                    Log.e(LOGTAG,"Unable to pause camera: " + e.getMessage());
                }
            }
        }
    }

    public void release(){
        synchronized (mCameraLock) {
            stop();
            mFrameProcessor.release();
        }
    }

    private class CameraCallBacks implements Camera.PreviewCallback{
        private final String LOGTAG = this.getClass().getSimpleName();
        @Override
        public void onPreviewFrame(byte[] data, Camera camera) {
            mFrameProcessor.setNextFrame(data);
        }
    }

    private int getCameraID(){
        Camera.CameraInfo ci = new Camera.CameraInfo();
        for(int i=0;i<Camera.getNumberOfCameras();++i){
            Camera.getCameraInfo(i,ci);
            if(ci.facing==CAMERA_FACING) return i;
        }
        return -1;
    }

    private void setRotation(Camera.Parameters params){
        WindowManager windowManager = (WindowManager)mContext.getSystemService(Context.WINDOW_SERVICE);
        int degrees = 0;
        int rotation = windowManager.getDefaultDisplay().getRotation();
        switch (rotation) {
            case Surface.ROTATION_0:
                degrees = 0;
                break;
            case Surface.ROTATION_90:
                degrees = 90;
                break;
            case Surface.ROTATION_180:
                degrees = 180;
                break;
            case Surface.ROTATION_270:
                degrees = 270;
                break;
            default:
                Log.e(LOGTAG, "Bad rotation value: " + rotation);
                return;
        }

        Camera.CameraInfo cameraInfo = new Camera.CameraInfo();
        Camera.getCameraInfo(getCameraID(), cameraInfo);

        int angle;
        int displayAngle;
        if (cameraInfo.facing == Camera.CameraInfo.CAMERA_FACING_FRONT) {
            angle = (cameraInfo.orientation + degrees) % 360;
            displayAngle = (360 - angle); // compensate for it being mirrored
        } else {  // back-facing
            angle = (cameraInfo.orientation - degrees + 360) % 360;
            displayAngle = angle;
        }
        mRotation = angle / 90;
        mCamera.setDisplayOrientation(displayAngle);
        params.setRotation(angle);
    }

    private byte[] createPreviewBuffer(com.google.android.gms.common.images.Size previewSize) {
        int bitsPerPixel = ImageFormat.getBitsPerPixel(ImageFormat.NV21);
        long sizeInBits = previewSize.getHeight() * previewSize.getWidth() * bitsPerPixel;
        int bufferSize = (int) Math.ceil(sizeInBits / 8.0d) + 1;

        //
        // NOTICE: This code only works when using play services v. 8.1 or higher.
        //

        // Creating the byte array this way and wrapping it, as opposed to using .allocate(),
        // should guarantee that there will be an array to work with.
        byte[] byteArray = new byte[bufferSize];
        ByteBuffer buffer = ByteBuffer.wrap(byteArray);
        if (!buffer.hasArray() || (buffer.array() != byteArray)) {
            // I don't think that this will ever happen.  But if it does, then we wouldn't be
            // passing the preview content to the underlying detector later.
            throw new IllegalStateException("Failed to create valid buffer for camera source.");
        }

        mBytesToByteBuffer.put(byteArray, buffer);
        return byteArray;
    }
}
