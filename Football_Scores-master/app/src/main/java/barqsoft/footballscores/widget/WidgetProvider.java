package barqsoft.footballscores.widget;

import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.RemoteViews;

import barqsoft.footballscores.MainActivity;
import barqsoft.footballscores.R;
import barqsoft.footballscores.sync.SyncAdapter;

/**
 * Created by Gerhard on 05/11/2015.
 */
public class WidgetProvider extends AppWidgetProvider {
    private static final String LOGTAG = WidgetProvider.class.getSimpleName();
    @Override
    public void onReceive(Context context, Intent intent) {
        Log.d(LOGTAG,"onReceive()");
        super.onReceive(context, intent);
        if(SyncAdapter.ON_DATA_UPDATE.equals(intent.getAction())){
            context.startService(new Intent(context,WidgetService.class));
        }
    }

    @Override
    public void onAppWidgetOptionsChanged(Context context, AppWidgetManager appWidgetManager, int appWidgetId, Bundle newOptions) {
        Log.d(LOGTAG,"onOptionsChanged()");
        context.startService(new Intent(context,WidgetService.class));
    }

    @Override
    public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
        Log.d(LOGTAG,"onUpdate()");
        context.startService(new Intent(context,WidgetService.class));
    }
}
