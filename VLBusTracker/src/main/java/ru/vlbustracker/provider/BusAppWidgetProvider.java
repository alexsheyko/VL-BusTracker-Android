package ru.vlbustracker.provider;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.app.Service;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.util.Log;
import android.widget.RemoteViews;
import android.os.IBinder;


import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Random;

import ru.vlbustracker.R;
import ru.vlbustracker.activities.MainActivity;
import ru.vlbustracker.helpers.AlarmManagerWidget;

/**
 * Created by s on 12.10.14.
 * http://www.vogella.com/tutorials/AndroidWidgets/article.html
 * http://forum.xda-developers.com/showthread.php?t=1732939
 */
public class BusAppWidgetProvider extends AppWidgetProvider {
/*
    @Override
    public void onDeleted(Context context, int[] appWidgetIds) {
        //Toast.makeText(context, "TimeWidgetRemoved id(s):"+appWidgetIds, Toast.LENGTH_SHORT).show();
        super.onDeleted(context, appWidgetIds);
    }

    @Override
    public void onDisabled(Context context) {
        //Toast.makeText(context, "onDisabled():last widget instance removed", Toast.LENGTH_SHORT).show();
        Intent intent = new Intent(context, AlarmManagerWidget.class);
        PendingIntent sender = PendingIntent.getBroadcast(context, 0, intent, 0);
        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        alarmManager.cancel(sender);
        super.onDisabled(context);
    }

    @Override
    public void onEnabled(Context context) {
        if (MainActivity.LOCAL_LOGV) Log.v(MainActivity.REFACTOR_LOG_TAG, "Widget enable.");
        super.onEnabled(context);
        AlarmManager am=(AlarmManager)context.getSystemService(Context.ALARM_SERVICE);
        Intent intent = new Intent(context, AlarmManagerWidget.class);
        PendingIntent pi = PendingIntent.getBroadcast(context, 0, intent, 0);
        //After after 3 seconds
        am.setRepeating(AlarmManager.RTC_WAKEUP, System.currentTimeMillis()+ 1000 * 3, 1000 , pi);
    }*/

    @Override
    public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
        final int N = appWidgetIds.length;
        if (MainActivity.LOCAL_LOGV) Log.v(MainActivity.REFACTOR_LOG_TAG, "Widget update."+Integer.toString(N));

        // Perform this loop procedure for each App Widget that belongs to this provider
        for (int i=0; i<N; i++) {
            /*
            int appWidgetId = appWidgetIds[i];
            int number = (new Random().nextInt(100));


            Date mdate = new Date(); //System.currentTimeMillis()
            String CurDate = new SimpleDateFormat("HH:mm:ss dd-MM-yyyy").format(mdate);

            // Create an Intent to launch ExampleActivity


            // Get the layout for the App Widget and attach an on-click listener
            // to the button
            RemoteViews views = new RemoteViews(context.getPackageName(), R.layout.appwidget);
            views.setTextViewText(R.id.empty_view, CurDate);

            // Register an onClickListener
            Intent intent = new Intent(context, BusAppWidgetProvider.class);
            PendingIntent pendingIntent = PendingIntent.getActivity(context, 0, intent, 0);
            views.setOnClickPendingIntent(R.id.widget_button, pendingIntent);

            // Tell the AppWidgetManager to perform an update on the current app widget
            appWidgetManager.updateAppWidget(appWidgetId, views);
            */
            //no widget context.startService(new Intent(context, UpdateService.class));


        }
    }

    public static class UpdateService extends Service {
        @Override
        public int onStartCommand(Intent intent, int flags, int startId) {
            if (MainActivity.LOCAL_LOGV) Log.v(MainActivity.REFACTOR_LOG_TAG, "Widget onStart.");

            // Build the widget update for today
            RemoteViews updateViews = buildUpdate(this);
            //Log.d("WordWidget.UpdateService", "update built");

            // Push update for this widget to the home screen
            ComponentName thisWidget = new ComponentName(this, BusAppWidgetProvider.class);
            AppWidgetManager manager = AppWidgetManager.getInstance(this);
            manager.updateAppWidget(thisWidget, updateViews);
            //Log.d("WordWidget.UpdateService", "widget updated");
            return START_STICKY;

        }

        @Override
        public IBinder onBind(Intent intent) {
            return null;
        }

        /**
         * Build a widget update to show the current Wiktionary
         * "Word of the day." Will block until the online API returns.
         */
        public RemoteViews buildUpdate(Context context) {
            // Pick out month names from resources
            Resources res = context.getResources();

            RemoteViews views = null;

                // Build an update that holds the updated widget contents
                views = new RemoteViews(context.getPackageName(), R.layout.appwidget);
                views.setTextViewText(R.id.tvTime, Long.toString(System.currentTimeMillis()));


                /*
                // When user clicks on widget, launch to Wiktionary definition page
                String definePage = String.format("%s://%s/%s", ExtendedWikiHelper.WIKI_AUTHORITY,
                        ExtendedWikiHelper.WIKI_LOOKUP_HOST, wordTitle);
                Intent defineIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(definePage));
                PendingIntent pendingIntent = PendingIntent.getActivity(context,
                        0 , defineIntent, 0 );
                views.setOnClickPendingIntent(R.id.widget, pendingIntent);
                */
/*
            } else {
                // Didn't find word of day, so show error message
                views = new RemoteViews(context.getPackageName(), R.layout.appwidget);
                views.setTextViewText(R.id.tvTime, "err");
            }*/
            return views;
        }
    }

}