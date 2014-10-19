package ru.vlbustracker.helpers;

import android.appwidget.AppWidgetManager;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.os.PowerManager;
import android.util.Log;
import android.widget.RemoteViews;

import java.text.SimpleDateFormat;
import java.util.Date;

import ru.vlbustracker.R;
import ru.vlbustracker.activities.MainActivity;
import ru.vlbustracker.provider.BusAppWidgetProvider;

/**
 * Created by s on 13.10.14.
 */

public class AlarmManagerWidget extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        if (MainActivity.LOCAL_LOGV) Log.v(MainActivity.REFACTOR_LOG_TAG, "Widget receive.");

        PowerManager pm = (PowerManager) context.getSystemService(Context.POWER_SERVICE);
        PowerManager.WakeLock wl = pm.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "YOUR TAG");
        //Acquire the lock
        wl.acquire();

        Date mdate = new Date(); //System.currentTimeMillis()
        String CurDate = new SimpleDateFormat("HH:mm:ss dd-MM-yyyy").format(mdate);

        //You can do the processing here update the widget/remote views.
        RemoteViews views = new RemoteViews(context.getPackageName(),                R.layout.appwidget);

        views.setTextViewText(R.id.empty_view, CurDate);

        views.setTextViewText(R.id.tvTime, Long.toString(System.currentTimeMillis()));

        //views.setTextViewText(R.id.tvTime, Utility.getCurrentTime("hh:mm:ss a"));
        ComponentName thiswidget = new ComponentName(context, BusAppWidgetProvider.class);
        AppWidgetManager manager = AppWidgetManager.getInstance(context);
        manager.updateAppWidget(thiswidget, views);
        //Release the lock
        wl.release();
    }
}