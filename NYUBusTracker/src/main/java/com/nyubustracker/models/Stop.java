package com.nyubustracker.models;

import android.location.Location;
import android.util.Log;

import com.google.android.gms.maps.model.LatLng;
import com.nyubustracker.R;
import com.nyubustracker.activities.MainActivity;
import com.nyubustracker.helpers.BusManager;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public class Stop {
    public static final String FAVORITES_PREF = "favorites";
    public static final Comparator<Stop> compare = new Comparator<Stop>() {
        @Override
        public int compare(Stop stop, Stop stop2) {
            if (stop.getFavorite()) {
                if (stop2.getFavorite()) {
                    return compareStartingNumbers(stop.getName(), stop2.getName());
                }
                else return -1;
            }
            else if (stop2.getFavorite()) return 1;
            else return compareStartingNumbers(stop.getName(), stop2.getName());
        }
    };
    final ArrayList<Stop> childStops;
    String name, id;
    LatLng loc;
    String[] routesString;
    ArrayList<Route> routes = null;
    String otherRoute = null;
    ArrayList<Time> times = null;
    boolean favorite;
    Stop parent;
    Stop oppositeStop;
    boolean hidden;
    private long lastUpdateTime;


    public Stop(String mName, String mLat, String mLng, String mID, String[] mRoutes) {
        name = cleanName(mName);
        loc = new LatLng(Double.parseDouble(mLat), Double.parseDouble(mLng));
        id = mID;
        routesString = mRoutes;
        times = new ArrayList<Time>();
        routes = new ArrayList<Route>();
        otherRoute = "";
        childStops = new ArrayList<Stop>();
        BusManager sharedManager = BusManager.getBusManager();
        for (String s : mRoutes) {
            Route r = sharedManager.getRouteByID(s);
            if (r != null && !r.getStops().contains(this)) r.addStop(this);
        }
    }

    public static String cleanName(String name) {
        name = name.replaceAll("at", "@");
        //name = name.replaceAll("[Aa]venue", "Ave");
        //name = name.replaceAll("bound", "");
        //name = name.replaceAll("[Ss]treet", "St");
        return name;
    }

    public static int compareStartingNumbers(String stop, String stop2) {
        int stopN = getStartingNumber(stop);
        int stopN2 = getStartingNumber(stop2);
        if (stopN > -1 && stopN2 > -1) return Integer.signum(stopN - stopN2);
        if (stopN > -1) return -1;
        if (stopN2 > -1) return 1;
        return Integer.signum(stopN - stopN2);
    }

    public static int getStartingNumber(String s) {
        if (Character.isDigit(s.charAt(0))) {
            int n = 0;
            while (n < s.length() && Character.isDigit(s.charAt(n))) {
                n++;
            }
            return Integer.parseInt(s.substring(0, n));
        }
        else return -1;
    }

    public static void parseJSON(JSONObject stopsJson) throws JSONException {


        JSONArray jStops = new JSONArray();
        BusManager sharedManager = BusManager.getBusManager();

        String[] routes1 = new String[0];
        Stop sany = sharedManager.getStop("Любая остановка", "0", "0", MainActivity.STOP_ID_ANY,routes1);
        sany.setFavorite(true);

        if (stopsJson != null) jStops = stopsJson.getJSONArray(BusManager.TAG_STOP);
        //if (MainActivity.LOCAL_LOGV) Log.v(MainActivity.REFACTOR_LOG_TAG, "BusManager current # stops: " + sharedManager.getStops());
        if (MainActivity.LOCAL_LOGV) Log.v(MainActivity.REFACTOR_LOG_TAG, "Parsing # stops: " + jStops.length());
        for (int i = 0; i < jStops.length(); i++) {
            //JSONObject stopObject = jStops.getJSONObject(i);
            JSONArray stopObject = jStops.getJSONArray(i);
            String stopID = stopObject.getString(0);
            String stopName = stopObject.getString(1);
            //if (MainActivity.LOCAL_LOGV) Log.v(MainActivity.REFACTOR_LOG_TAG, "*   Stop: " + stopID + " | " + stopName);
            //JSONObject location = stopObject.getJSONObject(BusManager.TAG_LOCATION);
            String stopLat = stopObject.getString(3);
            String stopLng = stopObject.getString(2);
            JSONArray stopRoutes = stopObject.getJSONArray(4);
            String[] routes = new String[stopRoutes.length()];
            for (int j = 0; j < stopRoutes.length(); j++) {
                routes[j] = stopRoutes.getString(j);
            }
            Stop s = sharedManager.getStop(stopName, stopLat, stopLng, stopID, routes);
            //if (MainActivity.LOCAL_LOGV) Log.v(MainActivity.REFACTOR_LOG_TAG, "Number of stops in manager: " + sharedManager.numStops());
            //if (MainActivity.LOCAL_LOGV) Log.v(MainActivity.REFACTOR_LOG_TAG, "___after adding " + s.name);
        }
        Location loc1 = new Location("");
        Location loc2 = new Location("");
        for (Stop s1 : sharedManager.getStops()) {
            if (s1==sany)continue;
            if (s1.getOppositeStop()==null) {
                if (s1.getID().substring(0,1)=="-"){

                    String stopID = s1.getID().substring(1);
                    Stop s2 = null;
                    for (Stop s : sharedManager.getStops()) {
                        if (s.getID().equals(stopID)) {
                            s2 = s;
                            break;
                        }
                    }
                    if (s2!=null){
                        s1.setOppositeStop(s2);
                        s2.setOppositeStop(s1);
                    }
                }
/*
                loc1.setLatitude(s1.getLocation().latitude);
                loc1.setLongitude(s1.getLocation().longitude);

                for (Stop s2 : sharedManager.getStops()) {
                    if (s1==s2)                        continue;
                    if (s2==sany)continue;

                    loc2.setLatitude(s2.getLocation().latitude);
                    loc2.setLongitude(s2.getLocation().longitude);
                    if (loc1.distanceTo(loc2) < 30) {
                        s1.setOppositeStop(s2);
                    }

                }
                */
            }
        }


    }

    public Stop getOppositeStop() {
        return oppositeStop;
    }

    public void setOppositeStop(Stop stop) {
        oppositeStop = stop;
    }

    public boolean isHidden() {
        return hidden;
    }

    public void setHidden(boolean hidden) {
        this.hidden = hidden;
    }

    public boolean hasTimes() {
        if (times.size() > 0) return true;
        for (Stop childStop : childStops) {
            if (childStop.hasTimes()) return true;
        }
        return false;
    }
    public void cleanTime() {
        times.clear();
    }

    public String getOtherRoute() {
        return otherRoute;
    }

    public void setOtherRoute(String r) {
        otherRoute = r;
    }

    public void setParentStop(Stop parent) {
        this.parent = parent;
    }

    public Stop getUltimateParent() {
        Stop result = this;
        while (result.getParent() != null) {
            result = result.getParent();
        }
        return result;
    }

    public Stop getParent() {
        return parent;
    }

    public void addChildStop(Stop stop) {
        if (!childStops.contains(stop)) {
            childStops.add(stop);
        }
    }

    public ArrayList<Stop> getFamily() {
        ArrayList<Stop> result = new ArrayList<Stop>(childStops);
        if (parent != null) {
            result.add(parent);
            if (parent.oppositeStop != null) {
                result.add(parent.oppositeStop);
            }
        }
        if (oppositeStop != null) {
            result.add(oppositeStop);
        }
        result.add(this);
        return result;
    }

    public ArrayList<Stop> getChildStops() {
        return childStops;
    }

    public void setValues(String mName, String mLat, String mLng, String mID, String[] mRoutes) {
        if (name.equals("")) name = cleanName(mName);
        if (loc == null) loc = new LatLng(Double.parseDouble(mLat), Double.parseDouble(mLng));
        id = mID;
        if (routesString == null) routesString = mRoutes;
        BusManager sharedManager = BusManager.getBusManager();
        for (String s : mRoutes) {
            Route r = sharedManager.getRouteByID(s);
            if (r != null && !r.getStops().contains(this)) r.addStop(this);
        }
    }

    public LatLng getLocation() {
        return loc;
    }

    public String toString() {
        return name;
    }

    public boolean getFavorite() {
        return favorite;
    }

    public void setFavorite(boolean checked) {
        favorite = checked;
    }

    public boolean hasRouteByString(String routeID) {
        for (String route : routesString) {
            if (route.equals(routeID)) return true;
        }
        return false;
    }

    public ArrayList<Route> getRoutes() {
        ArrayList<Route> result = new ArrayList<Route>(routes);
        for (Stop child : childStops) {
            for (Route childRoute : child.getRoutes()) {
                if (!result.contains(childRoute)) {
                    result.add(childRoute);
                }
            }
        }
        if (parent != null) {
            for (Stop child : parent.getChildStops()) {
                if (child != this) {
                    for (Route childRoute : child.getRoutes()) {
                        if (!result.contains(childRoute)) {
                            result.add(childRoute);
                        }
                    }
                }
            }
        }
        if (oppositeStop != null) {
            for (Route r : oppositeStop.routes) {
                if (!result.contains(r)) {
                    result.add(r);
                }
            }
            for (Stop child : oppositeStop.getChildStops()) {
                if (child != this) {
                    for (Route childRoute : child.getRoutes()) {
                        if (!result.contains(childRoute)) {
                            result.add(childRoute);
                        }
                    }
                }
            }
        }
        return result;
    }

    public void addRoute(Route route) {
        routes.add(route);
    }

    public String getID() {
        return id;
    }

    public void addTime(Time t) {
        times.add(t);
    }

    public ArrayList<Time> getTimesOfRoute(String route) {
        ArrayList<Time> result = new ArrayList<Time>();
        if (MainActivity.LOCAL_LOGV) Log.v(MainActivity.LOG_TAG, "Times for route: " + route + " (" + times.size() + " possible for " + this.getName() + ")");
        for (Time t : times) {
            //if (MainActivity.LOCAL_LOGV) Log.v(MainActivity.LOG_TAG, "Time route: " + t.getRoute());
            if (t.getRoute().equals(route)) {
                result.add(t);
            }
        }
        for (Stop childStop : childStops) {
            result.addAll(childStop.getTimesOfRoute(route));
        }
        return result;
    }

    public ArrayList<Time> getTimes() {
        ArrayList<Time> result = new ArrayList<Time>();
        if (MainActivity.LOCAL_LOGV) Log.v(MainActivity.LOG_TAG, "Times : (" + times.size() + " for " + this.getName() +" "+this.getID()+ ")");
        for (Time t : times) {
                if (t.notExpire()) {
                    result.add(t);
                }
        }
        return result;
    }

    public boolean isRelatedTo(Stop stop) {
        if (stop==null) return false;
        return (this.getUltimateName().equals(stop.getUltimateName()));
    }

    public String getUltimateName() {
        Stop s = this;
        while (s.getParent() != null) {
            s = s.getParent();
        }
        return s.getName();
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setLastUpdateTime(Long time) {
        this.lastUpdateTime = time;
    }

    public Boolean notExpireTime() {
        if((System.currentTimeMillis()-lastUpdateTime)>1000*60){
            return false;
        }else return true;

    }

    public List<Route> getRoutesTo(Stop endStop) {
        Stop startStop = this;
        ArrayList<Route> startRoutes = startStop.getUltimateParent().getRoutes();        // All the routes leaving the start stop.
        if (endStop==null){
            return  startRoutes;
        }
        ArrayList<Route> endRoutes = endStop.getUltimateParent().getRoutes();

        boolean foundAValidRoute = false;
        ArrayList<Route> availableRoutes = new ArrayList<Route>();               // All the routes connecting the two.
        for (Route r : startRoutes) {
            if (MainActivity.LOCAL_LOGV) Log.v("Routes", "Start Route: " + r);
            if (endRoutes.contains(r) && !availableRoutes.contains(r)) {
                if (MainActivity.LOCAL_LOGV) Log.v("Greenwich", "*  " + r + " is available.");
                foundAValidRoute = true;
                availableRoutes.add(r);
            }
        }
        if (foundAValidRoute) return availableRoutes;
        else return null;
    }

    public List<Time> getTimesToOn(Stop endStop, List<Route> routes){
        if (routes == null) return new ArrayList<Time>();
        ArrayList<Time> timesBetweenStartAndEnd = new ArrayList<Time>();
        for (Route r : routes) {
            if (MainActivity.LOCAL_LOGV) Log.v(MainActivity.LOG_TAG, "  " + r + " is available");
            // Get the Times at this stop for this route.
            ArrayList<Time> times = this.getTimesOfRoute(r.getLongName());
            ArrayList<Time> otherTimes = this.getTimesOfRoute(r.getOtherLongName());
            if (MainActivity.LOCAL_LOGV) {
                Log.d(MainActivity.LOG_TAG, "  has " + times.size() + " times ");
                Log.d(MainActivity.LOG_TAG, "  has " + otherTimes.size() + " other times.");
            }
            if (!(endStop==null) && !otherTimes.isEmpty() && !endStop.getTimesOfRoute(r.getOtherLongName()).isEmpty()) {
                for (Time t : otherTimes) {
                    if (!timesBetweenStartAndEnd.contains(t)) {
                        timesBetweenStartAndEnd.add(t);
                    }
                }
            }
            else {
                for (Time t : times) {
                    if (!timesBetweenStartAndEnd.contains(t)) {
                        timesBetweenStartAndEnd.add(t);
                    }
                }
            }
        }
        return timesBetweenStartAndEnd;
    }

    public static Stop[] getBestRelatedStartAndEnd(Stop startStop, Stop endStop) {
        if (endStop!=null && startStop!=null) {
            BusManager sharedManager = BusManager.getBusManager();
            int bestDistance = sharedManager.distanceBetween(startStop, endStop);

            int testDistance = sharedManager.distanceBetween(startStop.getOppositeStop(), endStop.getOppositeStop());
            if (testDistance < bestDistance) {
                startStop = startStop.getOppositeStop();
                endStop = endStop.getOppositeStop();
            }

            testDistance = sharedManager.distanceBetween(startStop, endStop.getOppositeStop());
            if (testDistance < bestDistance) {
                endStop = endStop.getOppositeStop();
            }

            testDistance = sharedManager.distanceBetween(startStop.getOppositeStop(), endStop);
            if (testDistance < bestDistance) {
                startStop = startStop.getOppositeStop();
            }
        }
        return new Stop[] {startStop, endStop};
    }

}

