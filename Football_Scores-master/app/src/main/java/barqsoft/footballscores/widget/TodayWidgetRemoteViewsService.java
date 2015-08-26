package barqsoft.footballscores.widget;

import android.annotation.TargetApi;
import android.app.LoaderManager;
import android.app.PendingIntent;
import android.content.CursorLoader;
import android.content.Intent;
import android.content.Loader;
import android.database.Cursor;
import android.net.Uri;
import android.os.Binder;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.widget.AdapterView;
import android.widget.RemoteViews;
import android.widget.RemoteViewsService;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import barqsoft.footballscores.DatabaseContract;
import barqsoft.footballscores.MainActivity;
import barqsoft.footballscores.R;
import barqsoft.footballscores.Utilies;

/**
 * Created by Milind Bedekar on 8/24/2015.
 */
public class TodayWidgetRemoteViewsService extends RemoteViewsService{
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

    @Override
    public RemoteViewsFactory onGetViewFactory(Intent intent)  {
        return new RemoteViewsFactory() {
            private Cursor data = null;
            private ArrayList<String[]> list;
            @Override
            public void onCreate() {
            }

            @Override
            public void onDataSetChanged() {
                final long identityToken = Binder.clearCallingIdentity();
                Uri scoreWithDateUri = DatabaseContract.scores_table.buildScoreWithDate();
                Date date = new Date();
                SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd");
                String formattedDate = df.format(date.getTime());
                data = getContentResolver().query(scoreWithDateUri,SCORES_COLUMNS,null,
                        new String[]{formattedDate}, DatabaseContract.scores_table.HOME_GOALS_COL + " DESC");
                if(data!=null&&data.moveToFirst()) {
                    list = new ArrayList<>();
                    Log.e(TodayWidgetRemoteViewsService.class.getSimpleName(), data.getString(INDEX_HOME));
                    do {
                        list.add(new String[]{data.getString(INDEX_HOME),
                                data.getString(INDEX_AWAY),
                                Utilies.getScores(data.getInt(INDEX_HOME_GOALS), data.getInt(INDEX_AWAY_GOALS)),
                                data.getString(INDEX_TIME)});
                        Log.e(TodayWidgetRemoteViewsService.class.getSimpleName(),"works");
                    }
                    while (data.moveToNext());
                }
                else{
                    if(data!=null){
                        data.close();
                    }
                }
                Binder.restoreCallingIdentity(identityToken);
            }

            @Override
            public void onDestroy() {
                if (data != null) {
                    data.close();
                    data = null;
                }
            }

            @Override
            public int getCount() {
                return data == null ? 0 : data.getCount();
            }

            @Override
            public RemoteViews getViewAt(int i) {
                if (i == AdapterView.INVALID_POSITION) {
                    return null;
                }
                String array[] = list.get(i);
                String home = array[INDEX_HOME];
                String away = array[INDEX_AWAY];
                int homeCrest = Utilies.getTeamCrestByTeamName(home);
                int awayCrest = Utilies.getTeamCrestByTeamName(away);
                String score = array[2];
                String time = array[3];
                RemoteViews views = new RemoteViews(getPackageName(), R.layout.widget_today);
                views.setTextViewText(R.id.widget_home_name,home);
                views.setTextViewText(R.id.widget_away_name,away);
                views.setTextViewText(R.id.widget_score_textview,score);
                views.setTextViewText(R.id.widget_time_textview, time);
                Log.e(TodayWidgetRemoteViewsService.class.getSimpleName(), home);
                views.setImageViewResource(R.id.widget_home_crest, homeCrest);
                views.setImageViewResource(R.id.widget_away_crest,awayCrest);
                final Intent fillInIntent = new Intent();
                Uri uri = DatabaseContract.scores_table.buildScoreWithDate();
                fillInIntent.setData(uri);
                views.setOnClickFillInIntent(R.id.widget_list_item,fillInIntent);
                return views;
            }

            @Override
            public RemoteViews getLoadingView() {
                return new RemoteViews(getPackageName(), R.layout.widget_today);
            }

            @Override
            public int getViewTypeCount() {
                return 1;
            }

            @Override
            public long getItemId(int i) {
                return 0;
            }

            @Override
            public boolean hasStableIds() {
                return false;
            }
        };
    }

}
