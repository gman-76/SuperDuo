package barqsoft.footballscores.widget;

import android.app.IntentService;
import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.content.ComponentName;
import android.content.Intent;
import android.database.Cursor;
import android.util.Log;
import android.widget.RemoteViews;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

import barqsoft.footballscores.DatabaseContract;
import barqsoft.footballscores.MainActivity;
import barqsoft.footballscores.R;
import barqsoft.footballscores.ScoresProvider;
import barqsoft.footballscores.Utilies;
import barqsoft.footballscores.scoresAdapter;

/**
 * Created by Gerhard on 05/11/2015.
 */
public class WidgetService extends IntentService {

    public WidgetService() {
        super("widgetintentservice");
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        Log.d("WIDGET","Handle widget intent");
        AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(this);
        int[] appWidgetIds = appWidgetManager.getAppWidgetIds(new ComponentName(this, WidgetProvider.class));

        Cursor data = getContentResolver().query(DatabaseContract.BASE_CONTENT_URI,null,null,null,DatabaseContract.scores_table.DATE_COL + " DESC," +
                                            DatabaseContract.scores_table.TIME_COL + " DESC");
        if(data==null || !data.moveToFirst()) {
            Log.d("WIDGET","No scores to update widget with");
            return;
        }

        for(int wID : appWidgetIds){
            RemoteViews views = new RemoteViews(getPackageName(), R.layout.widget);

            //update view elements with first element
            views.setTextViewText(R.id.widget_home_team,data.getString(scoresAdapter.COL_HOME));
            views.setTextViewText(R.id.widget_away_team,data.getString(scoresAdapter.COL_AWAY));
            views.setTextViewText(R.id.widget_score,Utilies.getScores(data.getInt(scoresAdapter.COL_HOME_GOALS),data.getInt(scoresAdapter.COL_AWAY_GOALS)));

            //find latest score if available and rather set views to that
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm");
            long currentTime = System.currentTimeMillis();
            Date currentDate = new Date(currentTime);
            do{
                Log.d("WIDGETDATA", data.getString(scoresAdapter.COL_DATE) + " | " + data.getString(scoresAdapter.COL_MATCHTIME) + " " +
                        data.getString(scoresAdapter.COL_HOME) + " vs " + data.getString(scoresAdapter.COL_AWAY));

                String mDate = data.getString(scoresAdapter.COL_DATE) + " " + data.getString(scoresAdapter.COL_MATCHTIME);
                Date matchDate = null;
                try{
                    matchDate = sdf.parse(mDate);
                    if(matchDate.getTime()<currentDate.getTime()){
                        Log.d("WIDGETDATA","Chosen " + matchDate.toString() + " as first date less than current " + currentDate.toString());
                        views.setTextViewText(R.id.widget_home_team,data.getString(scoresAdapter.COL_HOME));
                        views.setTextViewText(R.id.widget_away_team,data.getString(scoresAdapter.COL_AWAY));
                        views.setTextViewText(R.id.widget_score,Utilies.getScores(data.getInt(scoresAdapter.COL_HOME_GOALS),data.getInt(scoresAdapter.COL_AWAY_GOALS)));
                        break;
                    }
                }catch(ParseException pe){
                    Log.e("WIDGETDATA","Unable to parse date " + mDate + ": " + pe.getMessage());
                }
            }while(data.moveToNext());

            Intent clickIntent = new Intent(this, MainActivity.class);
            PendingIntent pi = PendingIntent.getActivity(this,0,clickIntent,0);
            views.setOnClickPendingIntent(R.id.widget,pi);

            appWidgetManager.updateAppWidget(wID,views);
        }
    }
}
