package it.jaschke.alexandria.data;

import android.graphics.ImageFormat;
import android.hardware.Camera;
import android.util.Log;

import com.google.android.gms.vision.Detector;
import com.google.android.gms.vision.Frame;
import it.jaschke.alexandria.ui.camera.CameraHolder;

import java.nio.ByteBuffer;

/**
 * Created by Gerhard on 29/10/2015.
 */
public class FrameProcessor implements Runnable {
    private final String LOGTAG = this.getClass().getSimpleName();

    private static final Object mLock =  new Object();

    private Detector<?> mDetector;
    private boolean mActive;
    private ByteBuffer mFrameToProcess;
    private CameraHolder mCameraHolder;

    public FrameProcessor(Detector<?> detector,CameraHolder ch){
        mDetector = detector;
        mActive = false;
        mCameraHolder = ch;
    }

    public void setActive(boolean active){
        synchronized (mLock){
            mActive = active;
            mLock.notifyAll();
        }
    }

    public void setNextFrame(byte[] data){
        synchronized (mLock){
            if (mFrameToProcess != null) {
                mCameraHolder.mCamera.addCallbackBuffer(mFrameToProcess.array());
                mFrameToProcess = null;
            }
            mFrameToProcess = mCameraHolder.mBytesToByteBuffer.get(data);
            mLock.notifyAll();
        }
    }

    @Override
    public void run() {
        Frame outputFrame;
        ByteBuffer data;
        while(true){
            synchronized (mLock){
                if(mActive && mFrameToProcess==null){
                    try{
                        Log.d(LOGTAG,"entering wait lock");
                        mLock.wait();
                    }catch(InterruptedException e){
                        Log.e(LOGTAG,"Exception waiting for frame: " +  e.getMessage());
                    }
                }
                if(!mActive) return;
                outputFrame = new Frame.Builder()
                        .setImageData(mFrameToProcess, mCameraHolder.getPreviewSize().getWidth(),
                                mCameraHolder.getPreviewSize().getHeight(), ImageFormat.NV21)
                        .setRotation(mCameraHolder.mRotation)
                        .build();
                data = mFrameToProcess;
                mFrameToProcess = null;
            }
            try {
                mDetector.receiveFrame(outputFrame);
            }catch(Throwable t){
                Log.e(LOGTAG,"Error from detector: " + t.getMessage());
            }finally{
                mCameraHolder.mCamera.addCallbackBuffer(data.array());
            }

        }
    }

    public void release(){
        mDetector.release();
        mDetector=null;
    }
}
