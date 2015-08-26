package barqsoft.footballscores.widget;

import android.app.IntentService;
import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.content.ComponentName;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.provider.ContactsContract;
import android.util.Log;
import android.widget.RemoteViews;

import java.sql.Time;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;

import barqsoft.footballscores.DatabaseContract;
import barqsoft.footballscores.MainActivity;
import barqsoft.footballscores.R;
import barqsoft.footballscores.Utilies;

/**
 * Created by Milind Bedekar on 8/22/2015.
 */
public class TodayWidgetIntentService extends IntentService {
    private static final String [] SCORES_COLUMNS = {
            DatabaseContract.scores_table.HOME_COL,
            DatabaseContract.scores_table.AWAY_COL,
            DatabaseContract.scores_table.HOME_GOALS_COL,
            DatabaseContract.scores_table.AWAY_GOALS_COL,
            DatabaseContract.scores_table.TIME_COL,
    };
    private static final int INDEX_HOME = 0;
    private static final int INDEX_AWAY = 1;
    private static final int INDEX_HOME_GOALS = 2;
    private static final int INDEX_AWAY_GOALS = 3;
    private static final int INDEX_TIME = 4;

    public TodayWidgetIntentService() {
        super("TodayWidgetIntentService");
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(this);
        int [] appWidgetIds = appWidgetManager.getAppWidgetIds(
                new ComponentName(this, TodayWidgetProvider.class));

        Uri scoreWithDateUri = DatabaseContract.scores_table.buildScoreWithDate();
        Calendar c = Calendar.getInstance();

        Date date = new Date();
        SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd ");
        String formattedDate = df.format(date.getTime());
        Log.e(TodayWidgetIntentService.class.getCanonicalName(),formattedDate);

        Cursor data = getContentResolver().query(scoreWithDateUri,SCORES_COLUMNS,null,
                new String[]{formattedDate}, DatabaseContract.scores_table.HOME_GOALS_COL + " DESC");
        if(data == null){
            return;
        }
        if(!data.moveToFirst()){
            data.close();
            Log.e(TodayWidgetIntentService.class.getSimpleName(),"error");
            return;
        }
        String home = data.getString(INDEX_HOME);
        String away = data.getString(INDEX_AWAY);
        int homeCrest = Utilies.getTeamCrestByTeamName(home);
        int awayCrest = Utilies.getTeamCrestByTeamName(away);
        String score = Utilies.getScores(data.getInt(INDEX_HOME_GOALS), data.getInt(INDEX_AWAY_GOALS));
        String time = data.getString(INDEX_TIME);
        data.close();

        for(int appWidgetId:appWidgetIds){
            RemoteViews views = new RemoteViews(getPackageName(), R.layout.widget_today);
            views.setTextViewText(R.id.widget_home_name,home);
            views.setTextViewText(R.id.widget_away_name,away);
            views.setTextViewText(R.id.widget_score_textview,score);
            views.setTextViewText(R.id.widget_time_textview,time);
            views.setImageViewResource(R.id.widget_home_crest, homeCrest);
            views.setImageViewResource(R.id.widget_away_crest,awayCrest);
            Intent launchIntent = new Intent(this, MainActivity.class);
            PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, launchIntent, 0);
            views.setOnClickPendingIntent(R.id.widget,pendingIntent);
            appWidgetManager.updateAppWidget(appWidgetId, views);
        }
    }

}
