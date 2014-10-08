package ru.vlbustracker.activities;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.res.Configuration;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.Typeface;
import android.graphics.drawable.ColorDrawable;
import android.location.Location;
import android.location.LocationManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.support.v4.app.ActionBarDrawerToggle;
import android.support.v4.widget.DrawerLayout;
import android.text.Editable;
import android.text.TextWatcher;
import android.text.TextUtils;
import android.util.Log;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextSwitcher;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ViewSwitcher;

//import com.flurry.android.FlurryAgent;
import com.google.android.gms.analytics.GoogleAnalytics;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.maps.android.clustering.Cluster;
import com.google.maps.android.clustering.ClusterManager;

import ru.vlbustracker.VLBusTrackerApplication;
import ru.vlbustracker.R;
import ru.vlbustracker.adapters.RouteAdapter;
import ru.vlbustracker.adapters.StopAdapter;
import ru.vlbustracker.adapters.TimeAdapter;
import ru.vlbustracker.helpers.AggregateDownloaderHelper;
import ru.vlbustracker.helpers.BusDownloaderHelper;
import ru.vlbustracker.helpers.BusItem;
import ru.vlbustracker.helpers.BusClusterRenderer;
import ru.vlbustracker.helpers.BusManager;
import ru.vlbustracker.helpers.Downloader;
import ru.vlbustracker.helpers.DownloaderArray;
import ru.vlbustracker.helpers.DownloaderHelper;
import ru.vlbustracker.helpers.MultipleOrientationSlidingDrawer;
import ru.vlbustracker.helpers.Poster;
import ru.vlbustracker.helpers.RouteDownloaderHelper;
import ru.vlbustracker.helpers.SegmentDownloaderHelper;
import ru.vlbustracker.helpers.StopDownloaderHelper;
import ru.vlbustracker.helpers.TimeDownloaderHelper;
import ru.vlbustracker.helpers.VersionDownloaderHelper;
import ru.vlbustracker.models.Bus;
import ru.vlbustracker.models.Route;
import ru.vlbustracker.models.Stop;
import ru.vlbustracker.models.Time;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.security.KeyStore;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;

import se.emilsjolander.stickylistheaders.StickyListHeadersListView;

public class MainActivity extends Activity {
    public static final boolean LOCAL_LOGV = true;
    public static final boolean SHOW_CLUSTER = false;
    private static final String RUN_ONCE_PREF = "runOnce";
    private static final String STOP_PREF = "stops";
    private static final String START_STOP_PREF = "startStop";
    private static final String END_STOP_PREF = "endStop";
    private static final String ROUTE_PREF = "endStop";
    private static final String FIRST_TIME = "firstTime";
    public static final String REFACTOR_LOG_TAG = "refactor";
    public static final String LOG_TAG = "v_log_tag";
    private final CompoundButton.OnCheckedChangeListener cbListener = new CompoundButton.OnCheckedChangeListener() {
        @Override
        public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
            Stop s = (Stop) buttonView.getTag();
            s.setFavorite(isChecked);
            getSharedPreferences(Stop.FAVORITES_PREF, MODE_PRIVATE).edit().putBoolean(s.getID(), isChecked).commit();
        }
    };
    List<Time> timesBetweenStartAndEnd;        // List of all times between start and end.
    Time nextBusTime;
    static ProgressDialog progressDialog;
    private static SharedPreferences oncePreferences;
    LocationManager mLocationManager;
    double onStartTime;
    private Stop startStop;     // Stop object to keep track of the start location of the desired route.
    private Stop endStop;       // Keep track of the desired end location.
    private Route routeSelect;       // Keep track of the desired end location.
    private static List<Route> routesBetweenStartAndEnd;        // List of all routes between start and end.
    private HashMap<String, Boolean> clickableMapMarkers;   // Hash of all markers which are clickable (so we don't zoom in on buses).
    private HashMap<String, Marker> Bus2Mark;   //
    private HashMap<String, Marker> Stop2Mark;   //
    public String stopIdEstimate;
    public String busIdEstimate;
    private ArrayList<Marker> busesOnMap = new ArrayList<Marker>();
    private TextSwitcher mSwitcher;
    private String mSwitcherCurrentText;
    private TimeAdapter timesAdapter;
    private StickyListHeadersListView timesList;
    private Timer timeUntilTimer;  // Timer used to refresh the "time until next bus" every minute, on the minute.
    private Timer busRefreshTimer; // Timer used to refresh the bus locations every few seconds.
    private Timer busDownloadTimer; // Timer used to refresh the bus locations every few seconds.
    private GoogleMap mMap;     // Map to display all stops, segments, and buses.
    private boolean offline = true;
    private MultipleOrientationSlidingDrawer drawer;
    public static int downloadsOnTheWire = 0;
    public static Handler UIHandler;
    private static final int BUSES_RELOAD_TIMEOUT = 10; //// в секундах
    public static final String STOP_ID_ANY = "-any-"; //// в секундах
    private DrawerLayout mDrawerLayout;
    private ListView mDrawerList;
    private ActionBarDrawerToggle mDrawerToggle;
    private CharSequence mTitle;
    private ListView lv;
    StopAdapter adapter_start;
    StopAdapter adapter_end;
    RouteAdapter adapter_route;
    private ClusterManager<BusItem> mClusterManager;

    // Search EditText
    //EditText inputSearch;


    static {
        UIHandler = new Handler(Looper.getMainLooper());
    }

    public static void runOnUI(Runnable runnable) {
        UIHandler.post(runnable);
    }

    private static Bitmap rotateBitmap(Bitmap source, float angle) {
        Matrix matrix = new Matrix();
        matrix.postRotate(angle);
        return Bitmap.createBitmap(source, 0, 0, source.getWidth(), source.getHeight(), matrix, true);
    }

    private void createMap() {
        // First check if GPS is available.
        final LatLng BROADWAY = new LatLng(43.116593,131.882166); // VL_CENTER
        int retCode = GooglePlayServicesUtil.isGooglePlayServicesAvailable(getApplicationContext());
        if (retCode != ConnectionResult.SUCCESS) {
            GooglePlayServicesUtil.getErrorDialog(retCode, this, 1).show();
            return;
        }
        // Do a null check to confirm that we have not already instantiated the map.
        if (mMap == null) {
            MapFragment mFrag = ((MapFragment) getFragmentManager().findFragmentById(R.id.map));
            if (mFrag != null) mMap = mFrag.getMap();
            // Check if we were successful in obtaining the map.
            if (mMap != null) {
                // The Map is verified. It is now safe to manipulate the map.
                mMap.getUiSettings().setRotateGesturesEnabled(false);
                mMap.getUiSettings().setZoomControlsEnabled(true);
                mMap.getUiSettings().setMyLocationButtonEnabled(true);
                //mMap.setMyLocationEnabled(true);

                //https://github.com/googlemaps/android-maps-utils/blob/master/demo/src/com/google/maps/android/utils/demo/ClusteringDemoActivity.java
                if (SHOW_CLUSTER) {
                    mClusterManager = new ClusterManager<BusItem>(this, mMap);
                    //mMap.setOnCameraChangeListener(mClusterManager);
                    //mMap.setOnMarkerClickListener(mClusterManager);
                    mClusterManager.setRenderer(new BusClusterRenderer(this, mMap, mClusterManager));
                    mClusterManager.setOnClusterClickListener(new ClusterManager.OnClusterClickListener<BusItem>() {
                        @Override
                        public boolean onClusterClick(Cluster<BusItem> busItemCluster) {

                            return false;
                        }
                    });
                    mClusterManager.setOnClusterItemClickListener(new ClusterManager.OnClusterItemClickListener<BusItem>() {
                        @Override
                        public boolean onClusterItemClick(BusItem busItem) {

                            if (LOCAL_LOGV)
                                Log.v(REFACTOR_LOG_TAG, "Set bus Estimate " + busItem.Bus.getID() + "");
                            busIdEstimate = busItem.Bus.getID();
                            ((TextView) findViewById(R.id.right_layout_title)).setText("Загружаем");

                            return false;
                        }
                    });
                    mClusterManager.setOnClusterItemInfoWindowClickListener(new ClusterManager.OnClusterItemInfoWindowClickListener<BusItem>() {
                        @Override
                        public void onClusterItemInfoWindowClick(BusItem busItem) {

                        }
                    });
                }


                mMap.setOnMarkerClickListener(new GoogleMap.OnMarkerClickListener() {
                    @Override
                    public boolean onMarkerClick(Marker marker) {
                        if (SHOW_CLUSTER) {
                            mClusterManager.onMarkerClick(marker);
                        }

                        if (Stop2Mark.containsValue( marker)){
                            Iterator entries = Stop2Mark.entrySet().iterator();
                            String idmarker = marker.getId();
                            while (entries.hasNext()) {
                                Map.Entry thisEntry = (Map.Entry) entries.next();
                                Object key = thisEntry.getKey();

                                Marker mMarker = Stop2Mark.get(key);
                                if (mMarker != null) {
                                    if (idmarker.equals(mMarker.getId())){
                                        if (LOCAL_LOGV) Log.v(REFACTOR_LOG_TAG, "Set stop Estimate " + key.toString() + "");
                                        stopIdEstimate = key.toString();
                                        ((TextView) findViewById(R.id.left_layout_title)).setText("Загружаем");
                                    }
                                }
                            }
                        }

                        if (Bus2Mark.containsValue( marker)){
                            Iterator entries = Bus2Mark.entrySet().iterator();
                            String idmarker = marker.getId();
                            while (entries.hasNext()) {
                                Map.Entry thisEntry = (Map.Entry) entries.next();
                                Object key = thisEntry.getKey();
                                Marker mMarker = Bus2Mark.get(key);
                                if (mMarker != null) {
                                    if (idmarker.equals(mMarker.getId())){
                                        if (LOCAL_LOGV) Log.v(REFACTOR_LOG_TAG, "Set bus Estimate " + key.toString() + "");
                                        busIdEstimate = key.toString();
                                        ((TextView) findViewById(R.id.right_layout_title)).setText("Загружаем");
                                    }
                                }
                            }
                        }



                        marker.showInfoWindow();

                        return true;
                                //!clickableMapMarkers.get(marker.getId());    // Return true to consume the event.
                    }
                });



                mMap.setOnCameraChangeListener(new GoogleMap.OnCameraChangeListener(){
                   @Override
                    public void onCameraChange(com.google.android.gms.maps.model.CameraPosition position)
                   {

                       showBusOnMap();
                       showStopOnMap();
                       if (SHOW_CLUSTER) {
                           mClusterManager.onCameraChange(position);
                       }
                   }

                });
                CameraUpdate center=
                        CameraUpdateFactory.newLatLng(BROADWAY);
                CameraUpdate zoom=CameraUpdateFactory.zoomTo(15);

                mMap.moveCamera(center);
                mMap.animateCamera(zoom);
            }
        }//map null
    }

    String readSavedData(String fileName) throws JSONException {
        if (LOCAL_LOGV) Log.v(REFACTOR_LOG_TAG, "Reading saved data from " + fileName);
        StringBuilder buffer = new StringBuilder("");
        try {
            File path = new File(getFilesDir(), Downloader.CREATED_FILES_DIR);
            path.mkdir();
            File file = new File(path, fileName);

            if (LOCAL_LOGV) Log.v(REFACTOR_LOG_TAG, "File time " + file.lastModified() + ":" + System.currentTimeMillis());
            if ((System.currentTimeMillis()-file.lastModified()>1000*60*60*24)||(System.currentTimeMillis()-file.lastModified()<0)){
                if (LOCAL_LOGV) Log.v(REFACTOR_LOG_TAG, "Old file " + fileName + "...");
                throw new JSONException("Failed to read " + fileName);
            }
            FileInputStream inputStream = new FileInputStream(file);
            InputStreamReader streamReader = new InputStreamReader(inputStream);
            BufferedReader bufferedReader = new BufferedReader(streamReader);

            String readString = bufferedReader.readLine();
            while (readString != null) {
                buffer.append(readString);
                readString = bufferedReader.readLine();
            }

            inputStream.close();
        } catch (IOException e) {
            if (LOCAL_LOGV) Log.v(REFACTOR_LOG_TAG, "Failed to read " + fileName + "...");
            throw new JSONException("Failed to read " + fileName);
        }
        return buffer.toString();
    }

    private void downloadEverything(boolean block) {
        //deleteEverythingInMemory();
        ConnectivityManager connMgr = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = connMgr.getActiveNetworkInfo();
        if (networkInfo != null && networkInfo.isConnected()) {
            offline = false;
            // Download and parse everything, put it all in persistent memory, continue.
            //if (block) progressDialog = ProgressDialog.show(this, getString(R.string.downloading), getString(R.string.wait), true, false);
            //else progressDialog = null;
            setProgressBarIndeterminateVisibility(true);

            Context context = getApplicationContext();

            downloadsOnTheWire += 1; //4;
            //new Downloader(new StopDownloaderHelper(), context).execute(DownloaderHelper.STOPS_URL);
            //new Downloader(new RouteDownloaderHelper(), context).execute(DownloaderHelper.ROUTES_URL);
            //new Downloader(new SegmentDownloaderHelper(), context).execute(DownloaderHelper.SEGMENTS_URL);
            //new Downloader(new VersionDownloaderHelper(), context).execute(DownloaderHelper.VERSION_URL);
            new Downloader(new AggregateDownloaderHelper(), context, this).execute(DownloaderHelper.AGR_URL);
            //setProgressBarIndeterminateVisibility(false);
        }
        else if (!offline) {    // Only show the offline dialog once.
            offline = true;
            Context context = getApplicationContext();
            CharSequence text = getString(R.string.unable_to_connect);
            int duration = Toast.LENGTH_SHORT;

            if (context != null) {
                Toast.makeText(context, text, duration).show();
            }
        }
    }

    public static void pieceDownloadsTogether(final Context context) {
        downloadsOnTheWire--;
        if (LOCAL_LOGV) Log.v(REFACTOR_LOG_TAG, "Downloads on the wire: " + downloadsOnTheWire);
        if (downloadsOnTheWire == 0) {
            if (LOCAL_LOGV) Log.v(REFACTOR_LOG_TAG, "Downloading finished!");
            oncePreferences.edit().putBoolean(FIRST_TIME, false).apply();
            /*
            if (progressDialog != null) {

                runOnUI(new Runnable() {
                @Override
                public void run() {
                    //showStopOnMap(); !!
                    setProgressBarIndeterminateVisibility(false);


                    progressDialog.dismiss();
                    //Toast.makeText(context, "Downloading finished!", Toast.LENGTH_SHORT).show();
                    Stop broadway = BusManager.getBusManager().getStopByName("715 Broadway @ Washington Square");
                        if (broadway != null) {
                            context.getSharedPreferences(Stop.FAVORITES_PREF, MODE_PRIVATE).edit().putBoolean(broadway.getID(), true).apply();
                            broadway.setFavorite(true);
                        }
                    };

                });

            }
            */
        }
        // Else, we have nothing to do, since not all downloads are finished.
    }

    // ******************  MAIN UP MENU
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu items for use in the action bar
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.main_activity_actions, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (mDrawerToggle != null && mDrawerToggle.onOptionsItemSelected(item))
            return true;

        switch (item.getItemId()) {//UP-RIGHT MAIN MENU
            case R.id.action_search:
                openSearch();
                return true;
            case R.id.action_settings:
                cleanEstiamate();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
        /*if (item.getItemId() == R.id.action_about) {
            Toast.makeText(this, R.string.about, Toast.LENGTH_LONG).show();
            return true;
        }
        */
        //return super.onOptionsItemSelected(item);
    }


    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);

        if (mDrawerToggle != null)
            mDrawerToggle.onConfigurationChanged(newConfig);
    }

    private class OnItemClickListener implements ListView.OnItemClickListener {
        @Override
        public void onItemClick(AdapterView parent, View view, int position, long id) {
            if (mDrawerLayout != null)
                mDrawerLayout.closeDrawers();
            //selectItem(position);
            if (LOCAL_LOGV) Log.v(REFACTOR_LOG_TAG, "List:"+Integer.toString(position));
            switch (position){
                case 0:
                    openSearch();
                    return;
                case 1:
                    //createInfoDialog(null);
                    return;
                case 2:
                    createInfoDialog(null);
                    return;
                default:
                    //createInfoDialog();
            }

        }
    }

    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);

        if (mDrawerToggle != null)
            mDrawerToggle.syncState();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_INDETERMINATE_PROGRESS);

        if (LOCAL_LOGV) Log.v(REFACTOR_LOG_TAG, "onCreate!");
        setContentView(R.layout.activity_main1);

        //http://www.dimasokol.ru/drawerlayout-panel-from-google/
        //https://developer.android.com/training/implementing-navigation/nav-drawer.html
        mTitle = getTitle();
        mDrawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
        mDrawerList = (ListView) findViewById(R.id.left_drawer);

        mDrawerToggle = new ActionBarDrawerToggle(this, mDrawerLayout,
                R.drawable.ic_drawer, R.string.drawer_open, R.string.drawer_close){
            public void onDrawerClosed(View view) {
                getActionBar().setTitle(mTitle);
                invalidateOptionsMenu(); // creates call to onPrepareOptionsMenu()
            }

            public void onDrawerOpened(View drawerView) {
                getActionBar().setTitle(R.string.drawer_select);
                invalidateOptionsMenu(); // creates call to onPrepareOptionsMenu()
            }
        };

        mDrawerLayout.setDrawerListener(mDrawerToggle);
        //mDrawerLayout.setDrawerShadow(R.drawable.drawer_shadow, Gravity.START);

        // Включим кнопки на action bar
        getActionBar().setDisplayHomeAsUpEnabled(true);
        getActionBar().setHomeButtonEnabled(true);

        // Set the adapter for the list view
        //mDrawerList.setAdapter(new ArrayAdapter<String>(this,
        //        R.layout.drawer_list_item, mPlanetTitles));

        // Этот код обрабатывает нажатия на пункты списка в выезжающей панели.
        // По такому нажатию мы будем закрывать drawer.
        mDrawerList.setOnItemClickListener(new OnItemClickListener());

        //android.app.Application Appl = (VLBusTrackerApplication) getApplication();
        ((VLBusTrackerApplication) getApplication()).getTracker();

        //Tracker t = ((VLBusTrackerApplication) getActivity().getApplication()).getTracker(
        //        TrackerName.APP_TRACKER);

        Bus2Mark = new HashMap<String, Marker>();
        Stop2Mark = new HashMap<String, Marker>();
        oncePreferences = getSharedPreferences(RUN_ONCE_PREF, MODE_PRIVATE);

        createMap(); // Instantiates mMap, if it needs to be.

        // Singleton BusManager to keep track of all stops, routes, etc.
        final BusManager sharedManager = BusManager.getBusManager();

        /*
        mSwitcher = (TextSwitcher) findViewById(R.id.next_time);
        mSwitcherCurrentText = "";

        // Set the ViewFactory of the TextSwitcher that will create TextView object when asked
        mSwitcher.setFactory(new ViewSwitcher.ViewFactory() {
            public View makeView() {
                TextView myText = new TextView(MainActivity.this);
                myText.setTextSize(TypedValue.COMPLEX_UNIT_PX, getResources().getDimensionPixelSize(R.dimen.time_until_text_size));
                myText.setTextColor(getResources().getColor(R.color.main_text));
                myText.setGravity(Gravity.CENTER_VERTICAL | Gravity.LEFT);
                myText.setEllipsize(TextUtils.TruncateAt.END);
                myText.setSingleLine(true);
                return myText;
            }
        });

        // Declare the in and out animations and initialize them
        Animation in = AnimationUtils.loadAnimation(MainActivity.this, android.R.anim.slide_in_left);
        Animation out = AnimationUtils.loadAnimation(MainActivity.this, android.R.anim.slide_out_right);

        // set the animation type of textSwitcher
        mSwitcher.setInAnimation(in);
        mSwitcher.setOutAnimation(out);
*/

        //drawer = (MultipleOrientationSlidingDrawer) findViewById(R.id.sliding_drawer);
        //drawer.setAllowSingleTap(false);
        //drawer.lock();

        //timesList = (StickyListHeadersListView) findViewById(R.id.times_list);
        //timesAdapter = new TimeAdapter(getApplicationContext(), new ArrayList<Time>());
        //timesList.setAdapter(timesAdapter);

        //downloadEverything(true);

        if (oncePreferences.getBoolean(FIRST_TIME, true)) {
            if (LOCAL_LOGV) Log.v(REFACTOR_LOG_TAG, "Downloading because of first time");
            downloadEverything(true);
        }
        else {
                if (LOCAL_LOGV) Log.v(REFACTOR_LOG_TAG, "Parsing cached files...");
                try {
                    //JSONObject stopJson = new JSONObject(readSavedData(StopDownloaderHelper.STOP_JSON_FILE));
                    //JSONObject routeJson = new JSONObject(readSavedData(RouteDownloaderHelper.ROUTE_JSON_FILE));
                    //JSONObject segJson = new JSONObject(readSavedData(SegmentDownloaderHelper.SEGMENT_JSON_FILE));
                    //JSONObject verJson = new JSONObject(readSavedData(VersionDownloaderHelper.VERSION_JSON_FILE));
                    JSONObject argJson = new JSONObject(readSavedData(AggregateDownloaderHelper.ARG_JSON_FILE));
                    Stop.parseJSON(argJson.getJSONObject("data"));
                    Route.parseJSON(argJson.getJSONObject("data"));
                    restoreStopCache(); // down setStartAndEndStops();

                    //BusManager.parseSegments(segJson);
                    //BusManager.parseVersion(verJson);
                    Context context = getApplicationContext();
                    /*
                    for (String timeURL : sharedManager.getTimesToDownload()) {
                        String timeFileName = timeURL.substring(timeURL.lastIndexOf("/") + 1, timeURL.indexOf(".json"));
                        if (LOCAL_LOGV) Log.v(REFACTOR_LOG_TAG, "Trying to parse " + timeFileName);
                        try {
                            BusManager.parseTime(new JSONObject(readSavedData(timeFileName)));
                        } catch (JSONException e) {
                            if (LOCAL_LOGV)
                                Log.v(REFACTOR_LOG_TAG, "Didn't find time file, so downloading it: " + timeURL);
                            new Downloader(new TimeDownloaderHelper(), context).execute(timeURL);
                        }
                    }
                    */
                    /*
                    SharedPreferences favoritePreferences = getSharedPreferences(Stop.FAVORITES_PREF, MODE_PRIVATE);
                    if (LOCAL_LOGV) Log.v(REFACTOR_LOG_TAG, "Done parsing...");
                    for (Stop s : sharedManager.getStops()) {
                        boolean result = favoritePreferences.getBoolean(s.getID(), false);
                        s.setFavorite(result);
                    }
                    */
                    //new Downloader(new VersionDownloaderHelper(), context).execute(DownloaderHelper.VERSION_URL);
                    //setStartAndEndStops();

                    // Update the map to show the corresponding stops, buses, and segments.
                    //updateMapWithNewStartOrEnd();

                    // Get the location of the buses every 10 sec.
                    //renewBusRefreshTimer();
                    //renewTimeUntilTimer();
                    //setNextBusTime();
                } catch (JSONException e) {
                    if (LOCAL_LOGV) Log.v(REFACTOR_LOG_TAG, "Re-downloading because of an error.");
                    e.printStackTrace();
                    downloadEverything(true);
                }
        }
        renewBusRefreshTimer();
        createSearch();


    }

    @Override
    public void onStart() {
        super.onStart();
        //        if (LOCAL_LOGV) Log.v("General Debugging", "onStart!");
        onStartTime = System.currentTimeMillis();
        //GoogleAnalytics.getInstance(this).reportActivityStart(this);
        //FlurryAgent.onStartSession(this, getString(LOCAL_LOGV ? R.string.flurry_debug_api_key : R.string.flurry_api_key));
    }

    @Override
    public void onResume() {
        super.onResume();
        if (LOCAL_LOGV) Log.v(REFACTOR_LOG_TAG, "onResume!");

        renewBusRefreshTimer();
        createMap();

        if (endStop != null && startStop != null) {
            //renewTimeUntilTimer();
            restoreStopCache();
        }

    }

    @Override
    public void onPause() {
        super.onPause();
        if (LOCAL_LOGV) Log.v(REFACTOR_LOG_TAG, "onPause!");
        cacheStops();
        if (timeUntilTimer != null) timeUntilTimer.cancel();
        if (busRefreshTimer != null) busRefreshTimer.cancel();
        if (busDownloadTimer != null) busDownloadTimer.cancel();
    }

    @Override
    public void onStop() {
        super.onStop();
        //        if (LOCAL_LOGV) Log.v("General Debugging", "onStop!");
        //GoogleAnalytics.getInstance(this).reportActivityStop(this);
        //FlurryAgent.onEndSession(this);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (LOCAL_LOGV) Log.v(REFACTOR_LOG_TAG, "onDestroy!");
        cacheStops();      // Remember user's preferences across lifetimes.
        if (timeUntilTimer != null)
            timeUntilTimer.cancel();           // Don't need a timer anymore -- must be recreated onResume.
        if (busDownloadTimer != null) busDownloadTimer.cancel();

    }

    @Override
    public void onBackPressed() {
        //if (drawer.isOpened()) drawer.animateClose();
        //else
        if (findViewById(R.id.form_search).getVisibility() == View.VISIBLE) {
            closeSearch();
        } else {
            if (findViewById(R.id.form_comment).getVisibility() == View.VISIBLE) {
                closeComment();
            } else {
                super.onBackPressed();
            }
        }
    }

    void cacheStops() {
        if (endStop != null) {
            getSharedPreferences(STOP_PREF, MODE_PRIVATE).edit().putString(END_STOP_PREF, endStop.getName()).apply();         // Creates or updates cache file.
        }else{
            getSharedPreferences(STOP_PREF, MODE_PRIVATE).edit().putString(END_STOP_PREF, "").apply();
        }
        if (startStop != null) {
            getSharedPreferences(STOP_PREF, MODE_PRIVATE).edit().putString(START_STOP_PREF, startStop.getName()).apply();
        }else{
            getSharedPreferences(STOP_PREF, MODE_PRIVATE).edit().putString(START_STOP_PREF, "").apply();
        }
        if (routeSelect != null) {
            getSharedPreferences(STOP_PREF, MODE_PRIVATE).edit().putString(ROUTE_PREF, routeSelect.getID()).apply();
        }else{
            getSharedPreferences(STOP_PREF, MODE_PRIVATE).edit().putString(ROUTE_PREF, "").apply();
        }
    }

    void sayBusIsOffline() {
        //updateNextTimeSwitcher(getString(R.string.offline));
        //((TextView) findViewById(R.id.next_bus)).setText("");
        //((TextView) findViewById(R.id.next_route)).setText("");
    }

    /*
    renewTimeUntilTimer() creates a new timer that calls setNextBusTime() every minute on the minute.
     */
    private void renewTimeUntilTimer() {
        Calendar rightNow = Calendar.getInstance();

        if (timeUntilTimer != null) timeUntilTimer.cancel();

        timeUntilTimer = new Timer();
        timeUntilTimer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        if (startStop != null && endStop != null) setNextBusTime();
                    }
                });
            }
        }, (60 - rightNow.get(Calendar.SECOND)) * 1000, 60000);
    }

    private void renewBusRefreshTimer() {
        if (busRefreshTimer != null) busRefreshTimer.cancel();
        if (busDownloadTimer != null) busDownloadTimer.cancel();

        busDownloadTimer = new Timer();
        busDownloadTimer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        ConnectivityManager connMgr = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
                        NetworkInfo networkInfo = connMgr.getActiveNetworkInfo();
                        if (networkInfo != null && networkInfo.isConnected()) {
                            offline = false;
                            //asv
                            //new Downloader(new BusDownloaderHelper(), getApplicationContext()).execute(DownloaderHelper.VEHICLES_URL);
                            //setProgressBarIndeterminateVisibility(true);
                            new DownloaderArray(new BusDownloaderHelper(), getApplicationContext()).execute(DownloaderHelper.CUR_URL);
                            if ((stopIdEstimate!=null)&&(stopIdEstimate.length()>0)){
                                new DownloaderArray(new TimeDownloaderHelper(stopIdEstimate,1), getApplicationContext()).execute(DownloaderHelper.STOP_TIME_URL+stopIdEstimate);
                            }
                            if ((busIdEstimate!=null)&&(busIdEstimate.length()>0)){
                                new DownloaderArray(new TimeDownloaderHelper(busIdEstimate,2), getApplicationContext()).execute(DownloaderHelper.BUS_TIME_URL+busIdEstimate);
                            }

                            showBusOnMap();
                            //setProgressBarIndeterminateVisibility(false);
                            //if (LOCAL_LOGV) Log.v(REFACTOR_LOG_TAG, "Current start: " + startStop);
                            //if (LOCAL_LOGV) Log.v(REFACTOR_LOG_TAG, "Current end  : " + endStop);
                        }
                        else if (!offline) {
                            offline = true;
                            Context context = getApplicationContext();
                            CharSequence text = getString(R.string.unable_to_connect);
                            int duration = Toast.LENGTH_SHORT;

                            if (context != null) {
                                Toast.makeText(context, text, duration).show();
                            }
                        }
                    }
                });
            }
        }, 0, BUSES_RELOAD_TIMEOUT * 1000); //  1500L);

        busRefreshTimer = new Timer();
        busRefreshTimer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        showBusOnMap();
                    }
                });
            }
        }, 0, 1000); //  1500L);

    }



    /*
    Returns the best location we can, checking every available location provider.
    If no provider is available (e.g. all location services turned off), this will return null.
     */
    public Location getLocation() {
        Location bestLocation = null;
        mLocationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        for (String provider : mLocationManager.getProviders(true)) {
            Location l = mLocationManager.getLastKnownLocation(provider);
            //            Log.d(REFACTOR_LOG_TAG, "time of location " + l.getAccuracy() + " is " + (System.currentTimeMillis() - l.getTime()));
            if (l != null && (bestLocation == null || l.getAccuracy() < bestLocation.getAccuracy()) && (System.currentTimeMillis() - l.getTime()) < 120000) {
                bestLocation = l;
            }
        }
        return bestLocation;
    }

    void restoreStopCache() {
        String end = getSharedPreferences(STOP_PREF, MODE_PRIVATE).getString(END_STOP_PREF, "");         // Creates or updates cache file.
        String start = getSharedPreferences(STOP_PREF, MODE_PRIVATE).getString(START_STOP_PREF, "");
        String route = getSharedPreferences(STOP_PREF, MODE_PRIVATE).getString(ROUTE_PREF, "");
        if( (startStop == null) && (start!=null)) {
            setStartStop(BusManager.getBusManager().getStopByName(start));
        }else{
            startStop=null;
        }
        if ((endStop == null) && (end!=null)) {
            setEndStop(BusManager.getBusManager().getStopByName(end));
        }else{
            endStop=null;
        }
        if ((routeSelect == null) && (route!=null)) {
            setRoute(BusManager.getBusManager().getRouteByID(route));
        }else{
            routeSelect = null;
        }
        /*
        Location l = getLocation();
        if (l != null && System.currentTimeMillis() - onStartTime < 1000) {
            Location startLoc = new Location(""), endLoc = new Location("");
            startLoc.setLatitude(startStop.getLocation().latitude);
            startLoc.setLongitude(startStop.getLocation().longitude);
            endLoc.setLatitude(endStop.getLocation().latitude);
            endLoc.setLongitude(endStop.getLocation().longitude);
            if (l.distanceTo(startLoc) > l.distanceTo(endLoc)) {
                setStartStop(endStop);
            }
        }
        **/
    }


    // Clear the map of all buses and put them all back on in their new locations.
    private void updateMapWithNewBusLocations() {
        //showBusOnMap();

/*        if (routesBetweenStartAndEnd != null) {
            BusManager sharedManager = BusManager.getBusManager();
            for (Marker m : busesOnMap) {
                m.remove();
            }
            busesOnMap = new ArrayList<Marker>();
            if (clickableMapMarkers == null)
                clickableMapMarkers = new HashMap<String, Boolean>();  // New set of buses means new set of clickable markers!
            boolean somethingActive = false;    // Used to make sure we put at least one set of segments on the map.
            for (Route r : routesBetweenStartAndEnd) {
                somethingActive = somethingActive || r.isActive(startStop);
            }
            for (Route r : routesBetweenStartAndEnd) {
                if (r.isActive(startStop) || !somethingActive) {
                    somethingActive = true;
                    for (Bus b : sharedManager.getBuses()) {
                        //if (LOCAL_LOGV) Log.v("BusLocations", "bus id: " + b.getID() + ", bus route: " + b.getRoute() + " vs route: " + r.getID());
                        if (b.getRoute().equals(r.getID())) {
                            Marker mMarker = mMap.addMarker(new MarkerOptions()
                                    .position(b.getLocation())
                                    .icon(BitmapDescriptorFactory.fromBitmap(
                                            rotateBitmap(BitmapFactory.decodeResource(this.getResources(), R.drawable.ic_bus_arrow), b.getHeading())
                                    ))
                                    .anchor(0.5f, 0.5f)
                            );
                            clickableMapMarkers.put(mMarker.getId(), false);    // Unable to click on buses.
                            busesOnMap.add(mMarker);
                        }
                    }
                }
            }
        }
        else {
            mMap.clear();
        }
        */
    }

    // Clear the map, because we may have just changed what route we wish to display. Then, add everything back onto the map.
    private void updateMapWithNewStartOrEnd() {
        // Can't update without any routes...
        if (LOCAL_LOGV) Log.v(LOG_TAG, "routesBetween is " + ((routesBetweenStartAndEnd == null) ? "" : "not ") + "null");
        showStopOnMap();

        if (routesBetweenStartAndEnd == null) {
        //    mMap.clear();
            return;
        }

        if (LOCAL_LOGV) Log.v(REFACTOR_LOG_TAG, "Have " + routesBetweenStartAndEnd.size() + " routes to show.");
        //setUpMapIfNeeded();
        //mMap.clear();
        clickableMapMarkers = new HashMap<String, Boolean>();
        LatLngBounds.Builder builder = new LatLngBounds.Builder();
        boolean validBuilder = false;
        boolean somethingActive = false;    // Used to make sure we put at least one set of segments on the map.
        //for (Route r : routesBetweenStartAndEnd) {
        //    somethingActive = somethingActive || r.isActive(startStop);
        //}
        //if (LOCAL_LOGV) Log.v(REFACTOR_LOG_TAG, "Something active: " + somethingActive);
        for (Route r : routesBetweenStartAndEnd) {
            //if ((r.isActive(startStop) || !somethingActive) && !r.getSegments().isEmpty()) {
                somethingActive = true;
                if (LOCAL_LOGV) Log.v(REFACTOR_LOG_TAG, "Updating map with route: " + r.getLongName());
                /*
                for (Stop s : r.getStops()) {
                    for (Stop f : s.getFamily()) {
                        if ((!f.isHidden() && !f.isRelatedTo(startStop) && !f.isRelatedTo(endStop)) || (f == startStop || f == endStop)) {
                            // Only put one representative from a family of stops on the p
                            //if (LOCAL_LOGV) Log.v("MapDebugging", "Not hiding " + f);
                            Marker mMarker = mMap.addMarker(new MarkerOptions()      // Adds a balloon for every stop to the map.
                                                                .position(f.getLocation()).title(f.getName()).anchor(0.5f, 0.5f).icon(BitmapDescriptorFactory.fromBitmap(BitmapFactory.decodeResource(this.getResources(), R.drawable.ic_map_stop))));
                            clickableMapMarkers.put(mMarker.getId(), true);
                        }
                    }
                }
                */
               /*
                // Adds the segments of every Route to the map.
                if (LOCAL_LOGV) Log.v(REFACTOR_LOG_TAG, r + " has " + r.getSegments().size() + " segments.");
                for (PolylineOptions p : r.getSegments()) {
                    if (LOCAL_LOGV) Log.v(REFACTOR_LOG_TAG, "Trying to add a segment to the map");
                    if (p != null) {
                        for (LatLng loc : p.getPoints()) {
                            validBuilder = true;
                            builder.include(loc);
                        }
                        p.color(getResources().getColor(R.color.main_buttons));
                        mMap.addPolyline(p);
                        if (LOCAL_LOGV) Log.v(REFACTOR_LOG_TAG, "Success!");
                    }
                    else if (LOCAL_LOGV) Log.v(REFACTOR_LOG_TAG, "Segment was null for " + r.getID());
                }
                */
            //}
        }
        showBusOnMap();

        if (validBuilder) {
            LatLngBounds bounds = builder.build();
            try {
                //mMap.animateCamera(CameraUpdateFactory.newLatLngBounds(bounds, 30));
            } catch (IllegalStateException e) {      // In case the view is not done being created.
                //e.printStackTrace();
                //mMap.moveCamera(CameraUpdateFactory.newLatLngBounds(bounds, this.getResources().getDisplayMetrics().widthPixels, this.getResources().getDisplayMetrics().heightPixels, 100));
            }
        }
    }

    private void setRoute(Route route) {
        if (route == null) {
            //printStop();
            routeSelect = null;
            //timesBetweenStartAndEnd = null;
            //setNextBusTime();    // Don't set the next bus if we don't have a valid route.
            //updateMapWithNewStartOrEnd();
            //return;
        }
        routeSelect = route;
        setNextBusTime();    // Don't set the next bus if we don't have a valid route.
        updateMapWithNewStartOrEnd();

        /*
        List<Route> routes = startStop.getRoutesTo(stop);
        if (routes != null && routes.size() > 0 && stop != startStop) {
            endStop = stop;
            printStop();
            if (startStop != null) {
                setNextBusTime();    // Don't set the next bus if we don't have a valid route.
                updateMapWithNewStartOrEnd();
            }
        }
        else {
            ArrayList<Stop> connected = BusManager.getBusManager().getConnectedStops(startStop);
            if (connected.size() == 1) {
                displayStopError();
            }
            else {
                int stopIndex = 0;
                while (connected.size() > stopIndex && !checkStop(connected.get(stopIndex))) {
                    stopIndex++;
                }
                if (stopIndex < connected.size()) setEndStop(connected.get(stopIndex));
                //else downloadEverything(true);
            }
        }
        */
    }

    private void setEndStop(Stop stop) {
        if (stop == null) {
            //((TextView) findViewById(R.id.end_stop)).setText(getString(R.string.default_end));
            printStop();
            //if (drawer.isOpened()) drawer.animateClose();
            endStop = null;
            //routesBetweenStartAndEnd = null;
            timesBetweenStartAndEnd = null;
            setNextBusTime();    // Don't set the next bus if we don't have a valid route.
            updateMapWithNewStartOrEnd();
            //drawer.lock();
            //drawer.setAllowSingleTap(false);
            return;
        }
        // Check there is a route between these stops.
        List<Route> routes = startStop.getRoutesTo(stop);
        if (routes != null && routes.size() > 0 && stop != startStop) {
            endStop = stop;
            //((TextView) findViewById(R.id.end_stop)).setText(stop.getUltimateName());
            printStop();
            if (startStop != null) {
                if (LOCAL_LOGV) Log.v(REFACTOR_LOG_TAG, "Start stop: " + startStop);
                if (LOCAL_LOGV) Log.v(REFACTOR_LOG_TAG, "End stop: " + endStop);
                setNextBusTime();    // Don't set the next bus if we don't have a valid route.
                updateMapWithNewStartOrEnd();
            }
        }
        else {
            ArrayList<Stop> connected = BusManager.getBusManager().getConnectedStops(startStop);
            if (connected.size() == 1) {
                displayStopError();
            }
            else {
                int stopIndex = 0;
                while (connected.size() > stopIndex && !checkStop(connected.get(stopIndex))) {
                    stopIndex++;
                }
                if (stopIndex < connected.size()) setEndStop(connected.get(stopIndex));
                //else downloadEverything(true);
            }
        }
    }

    private boolean checkStop(Stop stop) {
        if (stop != null) {     // Make sure we actually have a stop!
            // Check there is a route between these stops.
            ArrayList<Route> routes = new ArrayList<Route>();               // All the routes connecting the two.
            for (Route r : startStop.getRoutes()) {
                if (r.hasStop(stop)) {
                    routes.add(r);
                }
            }
            if (routes.size() > 0 && stop != startStop) {
                return true;
            }
        }
        return false;
    }

    private void setStartStop(Stop stop) {
        printStop();
        if (stop == null) {
            startStop = null;
            setNextBusTime();
            return;
        }

        if (endStop == stop) {    // Selected the end as the new start.
            // Swap the start and end stops.
            Stop temp = startStop;
            startStop = endStop;
            //((TextView) findViewById(R.id.stop_name)).setText(startStop.getUltimateName());
            printStop();
            setEndStop(temp);
        }
        else { // We have a new start. So, we must ensure the end is actually connected. If not, pick the first connected stop.
            startStop = stop;
            //((TextView) findViewById(R.id.stop_name)).setText(stop.getUltimateName());
            printStop();
            if (endStop != null) {
                List<Route> routes = startStop.getRoutesTo(endStop);
                if (routes == null) {   // Stops aren't connected.
                    routes = startStop.getRoutes();     // Routes the stop actually has.
                    if (routes != null && !routes.isEmpty()){
                        List<Stop> stops = routes.get(0).getStops();
                        if (!stops.isEmpty() && stops.get(0) != endStop){
                            setEndStop(stops.get(0));
                            return;
                        }
                    }
                }
                else {
                    setNextBusTime();
                    updateMapWithNewStartOrEnd();
                    return;
                }
            }
            setEndStop(null);
        }
    }

    private void setNextBusTime() {
        if (timeUntilTimer != null) timeUntilTimer.cancel();
        //if (busRefreshTimer != null) busRefreshTimer.cancel();
        //if (busDownloadTimer != null) busDownloadTimer.cancel();

        if (startStop==null){
            routesBetweenStartAndEnd = null;
            //updateNextTimeSwitcher("");
            printRoutes("");
            return;
        }
        // Find the best pair of start and end related to this pair, since Stops can "combine"
        // and have child stops, like at 14th Street and 3rd Ave.
        Stop[] newStartAndEnd = Stop.getBestRelatedStartAndEnd(startStop, endStop);
        startStop = newStartAndEnd[0];
        endStop = newStartAndEnd[1];
        routesBetweenStartAndEnd = startStop.getRoutesTo(endStop);
        String Str="";
        if (routeSelect!=null){
            Str = Str + routeSelect.getLongName() +"";
        }else{
            for (Route r : routesBetweenStartAndEnd) {
                Str = Str + r.getLongName() +",";
            }
        }
        printRoutes(Str);
        //updateNextTimeSwitcher(Str);


        timesBetweenStartAndEnd = startStop.getTimesToOn(endStop, routesBetweenStartAndEnd);
        //timesAdapter.setDataSet(timesBetweenStartAndEnd);
        //timesAdapter.notifyDataSetChanged();
        if (routesBetweenStartAndEnd == null || // No routes between the two. Should not happen.
            timesBetweenStartAndEnd == null  || // Should definitely not be here. But, just in case.
            timesBetweenStartAndEnd.size() == 0){   // Have a route, but no time.
            if (LOCAL_LOGV) {
                Log.v(LOG_TAG, "Returning early!!!!");
                Log.v(LOG_TAG, "Routes Null: " + (routesBetweenStartAndEnd == null));
                Log.v(LOG_TAG, "Times Null: " + (timesBetweenStartAndEnd == null));
                Log.v(LOG_TAG, "No times: " + timesBetweenStartAndEnd.size());
            }
            //drawer.setAllowSingleTap(false);
            //drawer.lock();
            //sayBusIsOffline();
            //showSafeRideInfoIfNeeded(Time.getCurrentTime());
            return;
        }
        //drawer.setAllowSingleTap(true);
        //drawer.unlock();
        /*
        final Time currentTime = Time.getCurrentTime();
        ArrayList<Time> tempTimesBetweenStartAndEnd = new ArrayList<Time>(timesBetweenStartAndEnd);
        tempTimesBetweenStartAndEnd.add(currentTime);
        Collections.sort(tempTimesBetweenStartAndEnd, Time.compare);
        Collections.sort(timesBetweenStartAndEnd, Time.compare);
        */
/*
        int index = tempTimesBetweenStartAndEnd.indexOf(currentTime);
        if (LOCAL_LOGV) Log.v(REFACTOR_LOG_TAG, "Index: " + index + " | size: " + tempTimesBetweenStartAndEnd.size());
        int nextTimeTempIndex = (index + 1) % tempTimesBetweenStartAndEnd.size();
        nextBusTime = tempTimesBetweenStartAndEnd.get(nextTimeTempIndex);
        final int nextTimeIndex = timesBetweenStartAndEnd.indexOf(nextBusTime);

        //updateNextTimeSwitcher(currentTime.getTimeAsStringUntil(nextBusTime, getResources()));

        timesList.clearFocus();
        timesList.post(new Runnable() {
            @Override
            public void run() {
                timesList.setSelection(nextTimeIndex);
            }
        });
        */
        //timesAdapter.setTime(currentTime);
/*
        if (BusManager.getBusManager().isNotDuringSafeRide()) {
            String routeText;
            String[] routeArray = nextBusTime.getRoute().split("\\s");
            String route = nextBusTime.getRoute();
            if (routeArray[0].length() == 1) {      // We have the A, B, C, E, etc. So, prepend route.
                routeText = getString(R.string.route) + route;
            }
            else {
                routeText = route;
            }
            //((TextView) findViewById(R.id.next_route)).setText(getString(R.string.via) + routeText);
            //((TextView) findViewById(R.id.next_bus)).setText(getString(R.string.next_bus_in));
            //findViewById(R.id.safe_ride_button).setVisibility(View.GONE);
        }
        */
        //else showSafeRideInfoIfNeeded(currentTime);
        //renewBusRefreshTimer();
        renewTimeUntilTimer();
    }

    private void showSafeRideInfoIfNeeded(Time currentTime) {
        if (!BusManager.getBusManager().isNotDuringSafeRide()) {
            //((TextView) findViewById(R.id.next_route)).setText("");
            //((TextView) findViewById(R.id.next_bus)).setText("");
            /*
            if (currentTime.getHour() < 7) {
                findViewById(R.id.safe_ride_button).setVisibility(View.VISIBLE);
            } else {
                findViewById(R.id.safe_ride_button).setVisibility(View.GONE);
            }
            */
        }
    }

    private void printStop(){
        String str="";
        if (startStop==null){
            str =  ""; // R.string.default_start;
        }else{
            str =  startStop.getName();
        }
        if (endStop==null){
            //str =  str + " " + R.string.default_start;
        }else{
            str =  str + " - " +endStop.getName();
        }
        ((TextView) findViewById(R.id.stop_name)).setText(str);

    }

    private void printRoutes(final String newText){
        ((TextView) findViewById(R.id.transport_name)).setText(newText);

    }

    /*
    private void updateNextTimeSwitcher(final String newSwitcherText){
        if (LOCAL_LOGV) Log.v(REFACTOR_LOG_TAG, "Updating switcher to [" + newSwitcherText + "]");
        //if (drawer != null && !drawer.isMoving() &&
        //if(!mSwitcherCurrentText.equals(newSwitcherText)) {
        //    mSwitcher.setText(newSwitcherText);  // Pass resources so we return the proper string value.
        //    mSwitcherCurrentText = newSwitcherText;
        //}
        // Handle a bug where the time until text disappears when the drawer is being moved. So, just wait for it to finish.
        // We don't know if the drawer will end up open or closed, though. So handle both cases.
        /*
        else if (drawer != null && !mSwitcherCurrentText.equals(newSwitcherText)) {
            drawer.setOnDrawerCloseListener(new MultipleOrientationSlidingDrawer.OnDrawerCloseListener() {
                @Override
                public void onDrawerClosed() {
                    mSwitcher.setText(newSwitcherText);
                    mSwitcherCurrentText = newSwitcherText;
                }
            });
            drawer.setOnDrawerOpenListener(new MultipleOrientationSlidingDrawer.OnDrawerOpenListener() {
                @Override
                public void onDrawerOpened() {
                    mSwitcher.setText(newSwitcherText);
                    mSwitcherCurrentText = newSwitcherText;
                }
            });
        }**
    }
    */

    @SuppressWarnings("UnusedParameters")
    public void callSafeRide(View view) {
        //Intent callIntent = new Intent(Intent.ACTION_DIAL);
        //callIntent.setData(Uri.parse("tel:12129928267"));
        //startActivity(callIntent);
    }

    @SuppressWarnings("UnusedParameters")
    public void createRouteDialog(View view) {
        if (LOCAL_LOGV) Log.v(REFACTOR_LOG_TAG, "CreateRouteDialog.");

        // Get all stops connected to the start stop.
        //routesBetweenStartAndEnd = startStop.getRoutesTo(endStop);

        final ArrayList<Route> aRoute =  BusManager.getBusManager().getRouteList(startStop,endStop);

        if (aRoute.size() > 0) {

            //ListView listView = new ListView(this);
            ListView listView = (ListView) findViewById(R.id.route_1);
            adapter_route = new RouteAdapter(getApplicationContext(), aRoute, new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Route r = (Route) view.getTag();
                    //if (s.getID()==MainActivity.STOP_ID_ANY) {
                    //    s = null;
                    //    endStop = null;
                    //}
                    closeSearch();
                    setRoute(r);    // Actually set the start stop.
                    //dialog.dismiss();
                }
            }, cbListener);

            listView.setAdapter(adapter_route);

        }
        else if (startStop != null) {
            displayStopError();
        }
    }

    @SuppressWarnings("UnusedParameters")
    public void createEndDialog(View view) {
        if (LOCAL_LOGV) Log.v(REFACTOR_LOG_TAG, "CreateEndDialog.");
        // Get all stops connected to the start stop.
        final ArrayList<Stop> connectedStops = BusManager.getBusManager().getConnectedStops(startStop);
        if (connectedStops.size() > 0) {

            //ListView listView = new ListView(this);
            ListView listView = (ListView) findViewById(R.id.end_stop1);
            adapter_end = new StopAdapter(getApplicationContext(), connectedStops, new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Stop s = (Stop) view.getTag();
                    if (s.getID()==MainActivity.STOP_ID_ANY) {
                        s = null;
                        endStop = null;
                    }
                    closeSearch();
                    setEndStop(s);    // Actually set the start stop.
                    //dialog.dismiss();
                }
            }, cbListener);

            listView.setAdapter(adapter_end);

            /*ListView listView = new ListView(this);     // ListView to populate the dialog.
            listView.setId(R.id.end_stop_list);
            listView.setDivider(new ColorDrawable(getResources().getColor(R.color.time_list_background)));
            listView.setDividerHeight(2);
            AlertDialog.Builder builder = new AlertDialog.Builder(this);    // Used to build the dialog with the list of connected Stops.
            builder.setView(listView);
            final Dialog dialog = builder.create();
            // An adapter takes some data, then adapts it to fit into a view. The adapter supplies the individual view elements of
            // the list view. So, in this case, we supply the StopAdapter with a list of stops, and it gives us back the nice
            // views with a heart button to signify favorites and a TextView with the name of the stop.
            // We provide the onClickListeners to the adapter, which then attaches them to the respective views.
            StopAdapter adapter = new StopAdapter(getApplicationContext(), connectedStops, new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    // Clicked on a Stop. So, make it the end and dismiss the dialog.
                    Stop s = (Stop) view.getTag();
                    if (s.getID()==MainActivity.STOP_ID_ANY) {
                        s = null;
                        endStop = null;
                    }
                    setEndStop(s);  // Actually set the end stop.
                    dialog.dismiss();
                }
            }, cbListener);
            listView.setAdapter(adapter);
            dialog.setCanceledOnTouchOutside(true);
            dialog.show();  // Dismissed when a stop is clicked.
            */
        }
        else if (startStop != null) {
            displayStopError();
        }
    }

    @SuppressWarnings("UnusedParameters")
    public void createStartDialog(View view) {
        if (LOCAL_LOGV) Log.v(REFACTOR_LOG_TAG, "CreateStart.");

        final ArrayList<Stop> stops = BusManager.getBusManager().getStops();    // Show every stop as an option to start.
        if (stops.size() > 0) {

            lv = (ListView) findViewById(R.id.start_stop1);
            adapter_start = new StopAdapter(getApplicationContext(), stops, new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Stop s = (Stop) view.getTag();
                    if (s.getID()==MainActivity.STOP_ID_ANY) {
                        s = null;
                        startStop = null;
                    }
                    closeSearch();
                    setStartStop(s);    // Actually set the start stop.
                    //dialog.dismiss();
                }
            }, cbListener);

            //adapter = new ArrayAdapter<String>(this, R.layout.stop_list_item1, R.id.stop_text, stops);
            lv.setAdapter(adapter_start);

/*
            ListView listView = new ListView(this);
            listView.setId(R.id.start_stop);
            listView.setDivider(new ColorDrawable(getResources().getColor(R.color.time_list_background)));
            listView.setDividerHeight(1);
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setView(listView);
            final Dialog dialog = builder.create();
            StopAdapter adapter = new StopAdapter(getApplicationContext(), stops, new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Stop s = (Stop) view.getTag();
                    if (s.getID()==MainActivity.STOP_ID_ANY) {
                        s = null;
                        startStop = null;
                    }
                    setStartStop(s);    // Actually set the start stop.
                    dialog.dismiss();
                }
            }, cbListener);
            listView.setAdapter(adapter);
            dialog.setCanceledOnTouchOutside(true);
            dialog.show();
        */
        }
        else {
            displayStopError();
        }
    }

    public void displayStopError() {
        Context context = getApplicationContext();
        CharSequence text = getString(R.string.no_stops_available);
        int duration = Toast.LENGTH_LONG;

        if (context != null) {
            Toast.makeText(context, text, duration).show();
        }
    }

    public void  createSearch(){
        //************** START STOP
        EditText inputSearch = (EditText) findViewById(R.id.SearchStart);
        inputSearch.addTextChangedListener(new TextWatcher() {
            @Override
            public void onTextChanged(CharSequence cs, int arg1, int arg2, int arg3) {
                // When user changed the Text
                if (MainActivity.this.adapter_start!=null)
                    MainActivity.this.adapter_start.getFilter().filter(cs);
            }

            @Override
            public void beforeTextChanged(CharSequence arg0, int arg1, int arg2,int arg3) {
            }
            @Override
            public void afterTextChanged(Editable arg0) {
            }
        });
        inputSearch.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if(hasFocus){
                    //Toast.makeText(getApplicationContext(), "got the focus", Toast.LENGTH_LONG).show();
                    findViewById(R.id.start_stop1).setVisibility(View.VISIBLE);
                }else {
                    //Toast.makeText(getApplicationContext(), "lost the focus", Toast.LENGTH_LONG).show();
                    findViewById(R.id.start_stop1).setVisibility(View.GONE);

                }
            }
        });

        // ********* END STOP
        inputSearch = (EditText) findViewById(R.id.SearchEnd);
        inputSearch.addTextChangedListener(new TextWatcher() {

            @Override
            public void onTextChanged(CharSequence cs, int arg1, int arg2, int arg3) {
                // When user changed the Text
                if (MainActivity.this.adapter_end!=null)
                    MainActivity.this.adapter_end.getFilter().filter(cs);
            }

            @Override
            public void beforeTextChanged(CharSequence arg0, int arg1, int arg2,int arg3) {
            }
            @Override
            public void afterTextChanged(Editable arg0) {
            }
        });
        inputSearch.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if(hasFocus){
                    //Toast.makeText(getApplicationContext(), "got the focus", Toast.LENGTH_LONG).show();
                    findViewById(R.id.end_stop1).setVisibility(View.VISIBLE);
                }else {
                    //Toast.makeText(getApplicationContext(), "lost the focus", Toast.LENGTH_LONG).show();
                    findViewById(R.id.end_stop1).setVisibility(View.GONE);

                }
            }
        });
        //************** ROUTE search
        inputSearch = (EditText) findViewById(R.id.SearchRoute);
        inputSearch.addTextChangedListener(new TextWatcher() {
            @Override
            public void onTextChanged(CharSequence cs, int arg1, int arg2, int arg3) {
                if (MainActivity.this.adapter_route!=null)
                    MainActivity.this.adapter_route.getFilter().filter(cs);
            }

            @Override
            public void beforeTextChanged(CharSequence arg0, int arg1, int arg2,int arg3) {
            }
            @Override
            public void afterTextChanged(Editable arg0) {
            }
        });
        inputSearch.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if(hasFocus){
                    findViewById(R.id.route_1).setVisibility(View.VISIBLE);
                }else {
                    findViewById(R.id.route_1).setVisibility(View.GONE);

                }
            }
        });


        //********* Hide keybord
        setupUI(findViewById(R.id.form_search));

    }

    public static void hideSoftKeyboard(Activity activity) {
        InputMethodManager inputMethodManager = (InputMethodManager)  activity.getSystemService(Activity.INPUT_METHOD_SERVICE);
        inputMethodManager.hideSoftInputFromWindow(activity.getCurrentFocus().getWindowToken(), 0);
    }

    public void setupUI(View view) {

        //Set up touch listener for non-text box views to hide keyboard.
        if(!(view instanceof EditText)) {

            view.setOnTouchListener(new View.OnTouchListener() {

                public boolean onTouch(View v, MotionEvent event) {
                    hideSoftKeyboard(MainActivity.this);
                    return false;
                }

            });
        }

        //If a layout container, iterate over children and seed recursion.
        if (view instanceof ViewGroup) {

            for (int i = 0; i < ((ViewGroup) view).getChildCount(); i++) {

                View innerView = ((ViewGroup) view).getChildAt(i);

                setupUI(innerView);
            }
        }
    }

    public void closeComment() {
        findViewById(R.id.form_comment).setVisibility(View.GONE);
        findViewById(R.id.main_layout).setVisibility(View.VISIBLE);
    }

    public void closeSearch() {
        findViewById(R.id.form_search).setVisibility(View.GONE);
        findViewById(R.id.main_layout).setVisibility(View.VISIBLE);
        //createStartDialog(null);
    }

    public void openSearch(){
        //if stop load
        findViewById(R.id.form_search).setVisibility(View.VISIBLE);
        findViewById(R.id.main_layout).setVisibility(View.GONE);


        createStartDialog(null);
        createEndDialog(null);
        createRouteDialog(null);

        EditText inputSearch = (EditText) findViewById(R.id.SearchStart);
        if (startStop!=null){
            inputSearch.setText(startStop.getName());
        }else{
            inputSearch.setText("");
        }

        inputSearch = (EditText) findViewById(R.id.SearchEnd);
        if (endStop!=null){
            inputSearch.setText(endStop.getName());
        }else{
            inputSearch.setText("");
        }

        inputSearch = (EditText) findViewById(R.id.SearchRoute);
        if (routeSelect!=null){
            inputSearch.setText(routeSelect.getLongName());
        }else{
            inputSearch.setText("");
        }


/*
        AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
        LinearLayout linearLayout = (LinearLayout) getLayoutInflater()
                .inflate(
                        R.layout.search_layout,
                        (ViewGroup) ((ViewGroup) this.findViewById(android.R.id.content)).getChildAt(0),
                        false
                );
        builder.setView(linearLayout);
        Dialog dialog = builder.create();
        dialog.setCanceledOnTouchOutside(true);
        dialog.show();
*/
    }

    public void cleanEstiamate(){
        busIdEstimate = null;
        stopIdEstimate = null;
    }


    @SuppressWarnings("UnusedParameters")
    public void createInfoDialog(View view) {
        AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
        LinearLayout linearLayout = (LinearLayout) getLayoutInflater()
                .inflate(
                        R.layout.information_layout,
                        (ViewGroup) ((ViewGroup) this.findViewById(android.R.id.content)).getChildAt(0),
                        false
                );
        builder.setView(linearLayout);
        Dialog dialog = builder.create();
        dialog.setCanceledOnTouchOutside(true);
        dialog.show();
    }

    private Bitmap getIcoBus(String text,Float angle, Boolean hide) {
        //BitmapFactory.decodeResource(this.getResources(), R.drawable.ic_bus_arrow)

        Integer idres = R.drawable.ic_map_bus;
        if (hide){
            idres = R.drawable.ic_map_bus_hide;
        }
        Bitmap bm = BitmapFactory.decodeResource(this.getResources(), idres) //ic_bus_arrow
                .copy(Bitmap.Config.ARGB_8888, true);
        bm = rotateBitmap(bm,angle);

        Typeface tf = Typeface.create("Helvetica", Typeface.BOLD);

        Paint paint = new Paint();
        paint.setStyle(Paint.Style.FILL);
        //paint.setColor(Color.WHITE);
        paint.setTypeface(tf);
        //paint.setTextAlign(Align.CENTER);
        //paint.setTextSize(convertToPixels(context, 11));
        paint.setTextSize(11);

        Rect textRect = new Rect();
        paint.getTextBounds(text, 0, text.length(), textRect);

        Canvas canvas = new Canvas(bm);

        //If the text is bigger than the canvas , reduce the font size
        //if(textRect.width() >= (canvas.getWidth() - 4))     //the padding on either sides is considered as 4, so as to appropriately fit in the text
        //    paint.setTextSize(convertToPixels(context, 7));        //Scaling needs to be used for different dpi's

        //Calculate the positions
        //int xPos = (canvas.getWidth() / 2) - 2;     //-2 is for regulating the x position offset
        int xPos = (canvas.getWidth() / 2) - 3*text.length();     //-2 is for regulating the x position offset

        //"- ((paint.descent() + paint.ascent()) / 2)" is the distance from the baseline to the center.
        //int yPos = (int) ((canvas.getHeight() / 2)); //- ((paint.descent() + paint.ascent()) / 2)) ;
        int yPos = (int) ((canvas.getHeight() /2 ) - ((paint.descent() + paint.ascent()) / 2)) +2;

        canvas.drawText(text, xPos, yPos, paint);

        return  bm;
    }



    public static int convertToPixels(Context context, int nDP)
    {
        final float conversionScale = context.getResources().getDisplayMetrics().density;

        return (int) ((nDP * conversionScale) + 0.5f) ;

    }

    private void showBusOnMap(){

        Long t= System.currentTimeMillis();

        BusManager sharedManager = BusManager.getBusManager();
        if (sharedManager.getStops().size()>1){
            //stops load ok
            setProgressBarIndeterminateVisibility(false);
            showStopOnMap();
        }
        LatLngBounds bounds = mMap.getProjection().getVisibleRegion().latLngBounds;

        String st= "";
        st = st +"/"+ Integer.toString(sharedManager.getStops().size());
        st = st +"/"+ Integer.toString(sharedManager.getBuses().size());
        double left = bounds.southwest.longitude;
        double top = bounds.northeast.latitude;
        double right = bounds.northeast.longitude;
        double bottom = bounds.southwest.latitude;
        //st = st +"/"+ Double.toString(left);
        //st = st +"/"+ Double.toString(right);
        st = st +"/"+ Double.toString(right-left);
        ((TextView) findViewById(R.id.footer_text)).setText(st+"/"+t);

        //********** LEFT   ROUTE estiamte
        String str_times="";
        if ((stopIdEstimate!=null)&&(stopIdEstimate.length()>0)){
            Stop stopEst = sharedManager.getStopByID(stopIdEstimate);

            if (stopEst!=null){
                for (Time tim : stopEst.getTimes()){
                    Route route = sharedManager.getRouteByID(tim.getRoute());
                    if (route!=null){
                        String name = route.getLongName();
                        if ((name!=null)&&(name.length()>0)){
                            if (name.length()==1) name = name+"  ";
                            if (name.length()==2) name = name+" ";
                        }
                        str_times=str_times+" " + name + ":" + tim.getTimeAsString() + "\n";
                    }
                }
                if (stopEst.notExpireTime()){
                    ((TextView) findViewById(R.id.left_layout_title)).setText(stopEst.getName());
                }
            }
        }else{
            ((TextView) findViewById(R.id.left_layout_title)).setText("");
        }
        ((TextView) findViewById(R.id.left_layout_text)).setText(str_times);

        //********** RIGHT   BUS estiamte
        str_times="";
        if ((busIdEstimate!=null)&&(busIdEstimate.length()>0)){
            Bus busEst = sharedManager.getBus(busIdEstimate);

            if (busEst!=null){
                for (Time tim : busEst.getTimes()){
                    Stop stop = sharedManager.getStopByID(tim.getRoute());
                    Integer i = 0;

                    if (stop!=null) {
                        String name = stop.getName();
                        if (name.length()>12) {
                            name = name.substring(0, 10)+"..";
                        }
                        i=i+1;
                        if (i<=20) {
                            str_times = str_times + " " + tim.getTimeAsString() + ":" + name + "\n";
                        }
                    }
                }
                if (busEst.notExpireTime()){
                    ((TextView) findViewById(R.id.right_layout_title)).setText(busEst.getTitle() + "   "+ busEst.getBody() );
                }

            }
        }else{
            ((TextView) findViewById(R.id.right_layout_title)).setText("");
        }
        ((TextView) findViewById(R.id.right_layout_text)).setText(str_times);

        // cluster start
        if (SHOW_CLUSTER) {
            mClusterManager.clearItems();
        }

        // *************** BUS ON MAP
        Integer countBus = 0;
        for (Bus b : sharedManager.getBuses()) {
            //if (LOCAL_LOGV) Log.v("BusLocations", "bus id: " + b.getID() + ", bus route: " + b.getRoute() + " vs route: " + r.getID());
            Boolean ShowBus = Boolean.FALSE;

            // list route from STOP start-end
            if (routesBetweenStartAndEnd==null){
                ShowBus = Boolean.TRUE;
            }else {
                for (Route r : routesBetweenStartAndEnd) {
                    if (b.getRoute().equals(r.getID())) {
                        ShowBus = Boolean.TRUE;
                    }
                }
            }
            // only Selected route
            if (routeSelect!=null) {
                if (b.getRoute().equals(routeSelect.getID())) {
                    ShowBus = Boolean.TRUE;
                } else {
                    ShowBus = Boolean.FALSE;
                }
            }
            if (ShowBus){
                countBus++;
            }

            if(bounds.contains(b.getLocation())) {
                //sho is show
            }else{ //not in maps
                ShowBus = Boolean.FALSE;
            }


            // cluster start
            if (SHOW_CLUSTER) {
                if (ShowBus) {
                    BusItem offsetItem = new BusItem(b, null);
                    mClusterManager.addItem(offsetItem);
                    //mClusterManager.getMarkerManager().
                    //if (offsetItem.getPosition()!=b.getLocation()){
                    //    offsetItem.setPosition(b.getLocation());
                    //}
                    ShowBus = Boolean.FALSE;
                }
            }
            //cluster end


            if (ShowBus) {
                if(!Bus2Mark.containsKey(b.getID())) {


                    Marker mMarker = mMap.addMarker(new MarkerOptions()
                                    .position(b.getLocation())
                                    .icon(
                                            BitmapDescriptorFactory.fromBitmap(
//                                                    rotateBitmap(BitmapFactory.decodeResource(this.getResources(), R.drawable.ic_bus_arrow), b.getHeading()
                                                    //rotateBitmap(writeTextOnDrawable(b.getTitle()), b.getHeading()
                                                    getIcoBus(b.getTitle(),b.getHeading(),b.isHidden())
                                                    //)

                                            )
                                    )
                                    .anchor(0.5f, 0.5f)
                                    .title(b.getTitle())
                                    .snippet("№№: " + b.getBody())
                    );
                    Bus2Mark.put(b.getID(), mMarker);



                }else{ //change pos
                    Marker mMarker = Bus2Mark.get(b.getID());
                    if (mMarker!= null){
                        mMarker.setPosition(b.getLocation());
                    }
                }
                //Bus2Mark.put(b.getID(),mMarker);
            }else{
                if(!Bus2Mark.containsKey(b.getID())) {

                }else{
                    Marker mMarker = Bus2Mark.get(b.getID());
                    if (mMarker!= null){
                        mMarker.remove();
                        Bus2Mark.remove(b.getID());
                    }
                }
            }
        }//for bus
        if (SHOW_CLUSTER) {
            mClusterManager.cluster();
        }

        getActionBar().setTitle(mTitle +"  :"+Integer.toString(countBus));


    }

    private void showStopOnMap() {

        BusManager sharedManager = BusManager.getBusManager();
        LatLngBounds bounds = mMap.getProjection().getVisibleRegion().latLngBounds;
        double left = bounds.southwest.longitude;
        double top = bounds.northeast.latitude;
        double right = bounds.northeast.longitude;
        double bottom = bounds.southwest.latitude;

            for (Stop b : sharedManager.getStops()) {
                //if (LOCAL_LOGV) Log.v("BusLocations", "bus id: " + b.getID() + ", bus route: " + b.getRoute() + " vs route: " + r.getID());
                Boolean ShowStop = Boolean.FALSE;
                if (bounds.contains(b.getLocation())&&(right-left<0.05)) {
                    ShowStop = Boolean.TRUE;
                }
                if ((b == startStop) || (b == endStop)) {
                    ShowStop = Boolean.FALSE;
                }


                if (ShowStop) {
                    if (!Stop2Mark.containsKey(b.getID())) {
                        Marker mMarker = mMap.addMarker(new MarkerOptions()      // Adds a balloon for every stop to the map.
                                .position(b.getLocation()).title(b.getName()).anchor(0.5f, 0.5f)
                                .icon(BitmapDescriptorFactory.fromBitmap(BitmapFactory.decodeResource(this.getResources(), R.drawable.ic_map_stop))));

                        Stop2Mark.put(b.getID(), mMarker);
                    }
                } else {
                    if (!Stop2Mark.containsKey(b.getID())) {

                    } else {
                        Marker mMarker = Stop2Mark.get(b.getID());
                        if (mMarker != null) {
                            mMarker.remove();
                            Stop2Mark.remove(b.getID());
                        }
                    }

                }
            }
            if (startStop != null) {
                Marker mMarker = mMap.addMarker(new MarkerOptions()      // Adds a balloon for every stop to the map.
                        .position(startStop.getLocation()).title(startStop.getName()).anchor(0.5f, 0.5f)
                        .icon(BitmapDescriptorFactory.fromBitmap(BitmapFactory.decodeResource(this.getResources(), R.drawable.ic_map_stop_active))));
                Stop2Mark.put(startStop.getID(), mMarker);
            }
            if (endStop != null) {
                Marker mMarker = mMap.addMarker(new MarkerOptions()      // Adds a balloon for every stop to the map.
                        .position(endStop.getLocation()).title(endStop.getName()).anchor(0.5f, 0.5f)
                        .icon(BitmapDescriptorFactory.fromBitmap(BitmapFactory.decodeResource(this.getResources(), R.drawable.ic_map_stop_active))));
                Stop2Mark.put(endStop.getID(), mMarker);
            }

    }


    @SuppressWarnings("UnusedParameters")
    public void cleanSearchEnd(View view){
        EditText inputSearch = (EditText) findViewById(R.id.SearchEnd);
        inputSearch.setText("");
        endStop = null;
        setEndStop(null);

}

    @SuppressWarnings("UnusedParameters")
    public void cleanSearchStart(View view){
        EditText inputSearch = (EditText) findViewById(R.id.SearchStart);
        inputSearch.setText("");
        startStop = null;
        setStartStop(null);    // Actually set the start stop.
    }

    @SuppressWarnings("UnusedParameters")
    public void cleanSearchRoute(View view){
        EditText inputSearch = (EditText) findViewById(R.id.SearchRoute);
        inputSearch.setText("");
        routeSelect = null;
        setRoute(null);
    }

    @SuppressWarnings("UnusedParameters")
    public void centerMapStop(View view){
        if((startStop!=null)&&(mMap!=null)){
            CameraUpdate center=
                    CameraUpdateFactory.newLatLng(startStop.getLocation());
            CameraUpdate zoom=CameraUpdateFactory.zoomTo(15);

            mMap.moveCamera(center);
            mMap.animateCamera(zoom);
        }

    }

    @SuppressWarnings("UnusedParameters")
    public void sendCoomment(View view) {
        if (LOCAL_LOGV) Log.v(REFACTOR_LOG_TAG, "Send comment.");
        if (busIdEstimate!=null){
            BusManager sharedManager = BusManager.getBusManager();
            Bus busEst = sharedManager.getBus(busIdEstimate);

            EditText inputText = (EditText) findViewById(R.id.comment_text);
            String s = "";
            s = s + busIdEstimate + "|";
            s = s + busEst.getTitle() + "|";
            s = s + busEst.getBody() + "|";
            s = s + busEst.getRoute() + "|";
            s = s + inputText.getText().toString();

            new Poster().execute(s);
        }
    }


    @SuppressWarnings("UnusedParameters")
    public void openComments(View view){
        if (LOCAL_LOGV) Log.v(REFACTOR_LOG_TAG, "Open comment.");
        if (busIdEstimate!=null){
            BusManager sharedManager = BusManager.getBusManager();
            Bus busEst = sharedManager.getBus(busIdEstimate);

            findViewById(R.id.form_comment).setVisibility(View.VISIBLE);
            findViewById(R.id.main_layout).setVisibility(View.GONE);

            TextView input = (TextView) findViewById(R.id.comment_title);
            input.setText(getString(R.string.comments)+" " + busEst.getTitle() + " №"+ busEst.getBody());
        }
    }


    @SuppressWarnings("UnusedParameters")
    public void goToGitHub(View view) {
        String url = "https://github.com/alexsheyko/NYU-BusTracker-Android";
        Intent i = new Intent(Intent.ACTION_VIEW);
        i.setData(Uri.parse(url));
        startActivity(i);
    }

    @SuppressWarnings("UnusedParameters")
    public void goToWebsite(View view) {
        String url = "http://map.vl.ru";
        Intent i = new Intent(Intent.ACTION_VIEW);
        i.setData(Uri.parse(url));
        startActivity(i);
    }

    private void deleteEverythingInMemory() {
        if (LOCAL_LOGV) Log.v(REFACTOR_LOG_TAG, "Trying to delete all files.");
        File directory = new File(getFilesDir(), Downloader.CREATED_FILES_DIR);
        File[] files = directory.listFiles();
        if (files != null) {
            for (File f : files) {
                if (f.delete()) {
                    if (LOCAL_LOGV) Log.v(REFACTOR_LOG_TAG, "Deleted " + f.toString());
                }
                else if (LOCAL_LOGV) Log.v(REFACTOR_LOG_TAG, "Could not delete " + f.toString());
            }
        }
    }

    private void showErrorAndFinish() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage("Error downloading")
                .setCancelable(false)
                .setPositiveButton("Refresh", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        finish();
                    }
                })
                .setNegativeButton("Try again later", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        downloadEverything(true);
                    }
                });
        AlertDialog alert = builder.create();
        alert.show();
    }
}
