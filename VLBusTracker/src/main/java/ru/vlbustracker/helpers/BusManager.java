package ru.vlbustracker.helpers;

import android.util.Log;

import com.google.android.gms.maps.model.PolylineOptions;
import com.google.maps.android.PolyUtil;
import ru.vlbustracker.activities.MainActivity;
import ru.vlbustracker.models.Bus;
import ru.vlbustracker.models.Comment;
import ru.vlbustracker.models.Route;
import ru.vlbustracker.models.Stop;
import ru.vlbustracker.models.Time;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Timer;
import java.util.TimerTask;

public final class BusManager {
    public static final String TAG_STOP = "stops";
    public static final String TAG_ROUTES = "routes";
    public static final String TAG_OTHER = "Other";
    private static final String TAG_ROUTE = "route";
    /*
    private static final String TAG_WEEKDAY = "Weekday";
    private static final String TAG_FRIDAY = "Friday";
    private static final String TAG_WEEKEND = "Weekend";
    */
    private static BusManager sharedBusManager = null;      // Singleton instance.
    private static ArrayList<Stop> stops = null;            // Hold all known stops.
    private static ArrayList<Route> routes = null;
    private static ArrayList<String> hideRoutes = null;     // Routes to not show the user.
    private static ArrayList<Bus> buses = null;
    private static ArrayList<Comment> comments = null;
    private static ArrayList<String> timesToDownload = null;
    private static HashMap<String, Integer> timesVersions = null;
    private static boolean isNotDuringSafeRide;

    private BusManager() {
        stops = new ArrayList<Stop>();
        routes = new ArrayList<Route>();
        hideRoutes = new ArrayList<String>();
        buses = new ArrayList<Bus>();
        comments = new ArrayList<Comment>();
        timesToDownload = new ArrayList<String>();
        timesVersions = new HashMap<String, Integer>();
        isNotDuringSafeRide = false;
    }

    public ArrayList<Stop> getStops() {
        ArrayList<Stop> result = new ArrayList<Stop>(stops);
        for (Stop stop : stops) {
            if (stop.isHidden()) { // || !stop.hasTimes()) {    Show stops without times for now.
                result.remove(stop);
            }
        }
        Collections.sort(result, Stop.compare);
        return result;
    }

    public ArrayList<Comment> getComments() {
        ArrayList<Comment> result = new ArrayList<Comment>(comments);
        /*for (Comment comment : comments) {
            //if (stop.isHidden()) { // || !stop.hasTimes()) {    Show stops without times for now.
                result.remove(stop);
            //}
        }
        */
        //Collections.sort(result, Stop.compare);
        return result;
    }

    public static void parseTime(JSONObject timesJson) throws JSONException {
        if (timesJson == null) return;      // Couldn't get the JSON. So, give up.
        final JSONObject routes = timesJson.getJSONObject(BusManager.TAG_ROUTES);
        final String stopID = timesJson.getString("stop_id");
        final Timer t = new Timer();
        t.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                Stop s = sharedBusManager.getStopByID(stopID);
                if (s != null) {
                    t.cancel();
                    for (int i = 0; i < s.getRoutes().size(); i++) {
                        if (routes.has(s.getRoutes().get(i).getID())) {
                            try {
                                JSONObject routeTimes = routes.getJSONObject(s.getRoutes().get(i).getID());
                                getAllTimes(s, s.getRoutes().get(i), routeTimes);
                            } catch (JSONException e) {
                                if (MainActivity.LOCAL_LOGV)
                                    Log.e("Greenwich", "Error parsing JSON...");
                                e.printStackTrace();
                            }
                        }
                    }
                }
            }
        }, 0L, 250L);
    }

    private static void getAllTimes(Stop s, Route r, JSONObject routeTimes) throws JSONException {
        /*
        getTimes(routeTimes, TAG_WEEKDAY, s, Time.TimeOfWeek.Weekday);
        getTimes(routeTimes, TAG_FRIDAY, s, Time.TimeOfWeek.Friday);
        getTimes(routeTimes, TAG_WEEKEND, s, Time.TimeOfWeek.Weekend);
        if (routeTimes.has(TAG_OTHER)) {
            //if (MainActivity.LOCAL_LOGV) Log.d("Greenwich", "********Other route!!! " + r.getOtherLongName());
            getAllTimes(s, r, routeTimes.getJSONObject(TAG_OTHER));
            String route = routeTimes.getJSONObject(TAG_OTHER).getString(TAG_ROUTE);
            s.setOtherRoute(route.substring(route.indexOf("Route ") + "Route ".length()));
            r.setOtherName(route.substring(route.indexOf("Route ") + "Route ".length()));
        }
        */

    }

    private static void getTimes(JSONObject routeTimes, String tag, Stop s, Time.TimeOfWeek timeOfWeek) throws JSONException {
        /*
        if (routeTimes.has(tag)) {
            JSONArray timesJson = routeTimes.getJSONArray(tag);
            if (MainActivity.LOCAL_LOGV) Log.v(MainActivity.LOG_TAG, "Found " + timesJson.length() + " " + timeOfWeek + " times.");
            String route = routeTimes.getString(BusManager.TAG_ROUTE);

            if (route.contains("Route ")) {
                route = route.substring(route.indexOf("Route ") + "Route ".length());
            }
            if (MainActivity.LOCAL_LOGV) Log.d(MainActivity.LOG_TAG, timesJson.length() + " times for " + s);
            for (int k = 0; k < timesJson.length(); k++) {
                s.addTime(new Time(timesJson.getString(k), timeOfWeek, route));
            }
        }
        */
    }

    public static void parseSegments(JSONObject segmentsJSON) throws JSONException {
        JSONObject jSegments = new JSONObject();
        if (segmentsJSON != null) jSegments = segmentsJSON.getJSONObject("data");
        BusManager sharedManager = BusManager.getBusManager();
        if (jSegments != null) {
            for (Route r : sharedManager.getRoutes()) {
                if (MainActivity.LOCAL_LOGV) Log.v(MainActivity.REFACTOR_LOG_TAG, "Parsing segments for " + r + " (" + r.getSegmentIDs() + ")");
                for (String seg : r.getSegmentIDs()) {
                    if (jSegments.has(seg)) {
                        r.getSegments().add(new PolylineOptions().addAll(PolyUtil.decode(jSegments.getString(seg))));
                    }
                }
            }
        }
    }

    public static synchronized BusManager getBusManager() {
        if (sharedBusManager == null) {
            sharedBusManager = new BusManager();
        }
        return sharedBusManager;
    }

    ArrayList<Route> getRoutes() {
        return routes;
    }

    public ArrayList<Route> getRouteList(Stop stop1,Stop stop2) {

        ArrayList<Route> result = new ArrayList<Route>();

        if (stop1!=null){
            for (Route r : stop1.getRoutesTo(stop2)) {
                result.add(r);
            }
        }else { //all route
            for (Route r : getRoutes()) {
                result.add(r);
            }
        }

        if (MainActivity.LOCAL_LOGV) Log.v(MainActivity.REFACTOR_LOG_TAG, "Found " + result.size() + " route.");
        return result;
    }

    public boolean isNotDuringSafeRide() {
        return isNotDuringSafeRide;
    }

    public void setIsNotDuringSafeRide(boolean state) {
        isNotDuringSafeRide = state;
    }

    public ArrayList<String> getTimesToDownload() {
        return timesToDownload;
    }

    public HashMap<String, Integer> getTimesVersions() {
        return timesVersions;
    }

    public boolean hasStops() {
        return stops != null && stops.size() > 0;
    }

    public ArrayList<Bus> getBuses() {
        return buses;
    }

    /*
    Given a bus ID, getBus returns either the existing Bus with that ID, or a new bus with that ID.
    This is used to parse the Bus JSON over and over to update location (called from Bus.parseJSON()).
     */
    public Bus getBus(String busID) {
        for (Bus b : buses) {
            if (b.getID().equals(busID)) {
                return b;
            }
        }
        Bus b = new Bus(busID);
        buses.add(b);
        return b;
    }

    /*
    Given the name of a stop (e.g. "715 Broadway"), getStopByName returns the Stop with that name.
     */
    public Stop getStopByName(String stopName) {
        for (Stop s : stops) {
            //if (MainActivity.LOCAL_LOGV) Log.v(MainActivity.REFACTOR_LOG_TAG, "Looking for " + stopName + " | " + s.getName());
            if (s.getName().equals(stopName)) {
                //if (MainActivity.LOCAL_LOGV) Log.v(MainActivity.REFACTOR_LOG_TAG, "Found it!");
                return s;
            }
        }
        return null;
    }

    /*
    Given a route ID, getStopsByRouteID returns an ArrayList of all Stops visited by that Route.
     */
    public ArrayList<Stop> getStopsByRouteID(String routeID) {
        ArrayList<Stop> result = new ArrayList<Stop>();
        for (Stop stop : stops) {
            //if (MainActivity.LOCAL_LOGV) Log.v("Debugging", "Number of routes of stop " + j + ": " + stop.routes.size());
            if (stop.hasRouteByString(routeID)) {
                result.add(stop);
            }
        }
        return result;
    }

    public boolean hasRoutes() {
        return routes != null && routes.size() > 0;
    }

    /*
    Given a Stop, getConnectedStops returns an array of Strings corresponding to every stop which has
    some route between it and the given stop.
     */
    public ArrayList<Stop> getConnectedStops(Stop stop) {
        ArrayList<Stop> result = new ArrayList<Stop>();
        for (Stop s1 : getStops()) {
            if (s1.getID()==MainActivity.STOP_ID_ANY){
                result.add(s1);
            }
        }

        if (stop != null) {
            ArrayList<Route> stopRoutes = stop.getRoutes();
            for (Route route : stopRoutes) {       // For every route servicing this stop:
                if (MainActivity.LOCAL_LOGV) Log.v(MainActivity.REFACTOR_LOG_TAG, route.toString() + " services this stop.");
                if (stop.getTimesOfRoute(route.getLongName()).size() > -1) {    // TODO: make this > 0.
                    for (Stop connectedStop : route.getStops()) {    // add all of that route's stops.
                        if (connectedStop != null && !connectedStop.getUltimateName().equals(stop.getName()) &&
                            !result.contains(connectedStop) &&
                            (!connectedStop.isHidden() || !connectedStop.isRelatedTo(stop))) {

                            while (connectedStop.getParent() != null) {
                                connectedStop = connectedStop.getParent();
                            }
                            boolean repeatStop = false;
                            for (Stop resultStop : result) {
                                if (resultStop.getName().equals(connectedStop.getName())) {
                                    repeatStop = true;
                                }
                            }

                            if (!repeatStop) {
                                result.add(connectedStop);
                                //if (MainActivity.LOCAL_LOGV) Log.v("Route Debugging","'" + connectedStop.getName() + "' is connected to '" + stop.getName() + "'");
                            }
                        }
                    }
                }
            }
            Collections.sort(result, Stop.compare);
            result.remove(stop);
        }
        if (MainActivity.LOCAL_LOGV) Log.v(MainActivity.REFACTOR_LOG_TAG, "Found " + result.size() + " connected stops.");
        return result;
    }

    /*
    addStop will add a Stop to our ArrayList of Stops, unless we're supposed to hide it.
     */
    public void addStop(Stop stop) {
        if (!stop.isHidden()) {
            stops.add(stop);
            //if (MainActivity.LOCAL_LOGV) Log.v("Debugging", "Added " + stop.toString() + " to list of stops (" + stops.size() + ")");
        }
    }

    public Stop getStop(String stopName, String stopLat, String stopLng, String stopID, String[] routes) {
        Stop s = getStopByID(stopID);
        if (s == null) {
            s = new Stop(stopName, stopLat, stopLng, stopID, routes);
            stops.add(s);
            //if (MainActivity.LOCAL_LOGV) Log.v(MainActivity.REFACTOR_LOG_TAG, "BusManager num stops: " + stops.size());
        }
        else {
            s.setValues(stopName, stopLat, stopLng, stopID, routes);
            if (!stops.contains(s)) stops.add(s);
        }
        return s;
    }

    public Comment getComment(String ID, String text) {
        Comment s = getCommentByID(ID);
        if (s == null) {
            s = new Comment(ID, text, "", "");
            comments.add(s);
        }
        else {
            //s.setValues(ID, text);
            if (!comments.contains(s)) comments.add(s);
        }
        return s;
    }

    public Comment getCommentByID(String ID) {
        for (Comment s : comments) {
            if (s.getID().equals(ID)) return s;
        }
        return null;
    }
    public int numStops() {
        return stops.size();
    }

    /*
    Given the ID of a stop, getStopByID returns the Stop with that ID.
     */
    public Stop getStopByID(String stopID) {
        for (Stop s : stops) {
            if (s.getID().equals(stopID)) return s;
        }
        return null;
    }

    /*
    addRoute will add a Route to our ArrayList of Routes, unless we're supposed to hide it.
     */
    public void addRoute(Route route) {
        if (!hideRoutes.contains(route.getID())) {
            //if (MainActivity.LOCAL_LOGV) Log.v("JSONDebug", "Adding route: " + route.getID());
            routes.add(route);
        }
    }

    public Route getRoute(String name, String id) {
        Route r;
        if ((r = getRouteByID(id)) == null) {
            return new Route(name, id);
        }
        else return r.setName(name);
    }

    /*
    Given an ID (e.g. "81374"), returns the Route with that ID.
     */
    public Route getRouteByID(String id) {
        if (routes != null) {
            for (Route route : routes) {
                if (route.getID().equals(id)) {
                    return route;
                }
            }
        }
        return null;
    }

    public int distanceBetween(Stop stop1, Stop stop2) {
        // Check these stops and their children.
        int result = 100;
        if (stop1 != null && stop2 != null) {
            for (Route r : routes) {
                if (r.hasStop(stop1) && r.hasStop(stop2)) {
                    int index1 = r.getStops().indexOf(stop1);
                    int index2 = r.getStops().indexOf(stop2);
                    result = index2 - index1;
                    if (result < 0) result += r.getStops().size();
                }
            }
            int children = 100;
            for (Stop s : stop1.getChildStops()) {
                int test = distanceBetween(s, stop2);
                if (test < children) children = test;
            }
            if (children < result) result = children;

            children = 100;
            for (Stop s : stop2.getChildStops()) {
                int test = distanceBetween(stop1, s);
                if (test < children) children = test;
            }
            if (children < result) result = children;
        }

        return result;
    }
}
