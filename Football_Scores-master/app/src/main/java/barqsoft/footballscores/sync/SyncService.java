package barqsoft.footballscores.sync;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;

/**
 * Created by Gerhard on 05/11/2015.
 */
public class SyncService extends Service {

    private static final Object mLock = new Object();
    private static SyncAdapter mAdapter = null;

    @Override
    public void onCreate() {
        synchronized (mLock){
            if(mAdapter==null){
                mAdapter = new SyncAdapter(getApplicationContext(),true);
            }
        }
    }

    @Override
    public IBinder onBind(Intent intent) {
        return mAdapter.getSyncAdapterBinder();
    }
}
