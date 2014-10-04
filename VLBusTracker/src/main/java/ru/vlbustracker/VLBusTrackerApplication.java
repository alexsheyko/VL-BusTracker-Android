package ru.vlbustracker;

import android.app.Application;

import com.google.android.gms.analytics.GoogleAnalytics;
import com.google.android.gms.analytics.Tracker;
import ru.vlbustracker.R;
import ru.vlbustracker.activities.MainActivity;


public class VLBusTrackerApplication extends Application {
    Tracker tracker = null;

    public VLBusTrackerApplication() {
        super();
    }

    public synchronized Tracker getTracker() {
 /*       if (tracker == null) {
            GoogleAnalytics analytics = GoogleAnalytics.getInstance(this);
            tracker = analytics.newTracker(R.xml.app_tracker);
            String id = MainActivity.LOCAL_LOGV
                    ? getString(R.string.google_analytics_debug_tracker)
                    : getString(R.string.google_analytics_tracker);
            tracker.setAppId(id);
        }
        */
        return tracker;
    }
}
