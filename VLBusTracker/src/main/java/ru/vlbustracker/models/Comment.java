package ru.vlbustracker.models;

import android.location.Location;
import android.util.Log;

import com.google.android.gms.maps.model.LatLng;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import ru.vlbustracker.activities.MainActivity;
import ru.vlbustracker.helpers.BusManager;

public class Comment {
    public static final Comparator<Comment> compare = new Comparator<Comment>() {
        @Override
        public int compare(Comment com1, Comment com2) {
            if (com1.timestamp > com2.timestamp){
                return 1;
            }else if (com1.timestamp < com2.timestamp){
                return -1;

            }else {
                return 0;
            }
        }
    };
    String id;
    String text;
    String busId;
    String busTitle;
    String busBody;
    String route;
    String date;
    long timestamp;
    LatLng loc;
    boolean favorite;
    boolean hidden;
    private long lastUpdateTime;

    public Comment(String mID, String mText, String mLat, String mLng) {
        text = cleanName(mText);
        //loc = new LatLng(Double.parseDouble(mLat), Double.parseDouble(mLng));
        id = mID;
        //routesString = mRoutes;
        //times = new ArrayList<Time>();
        //routes = new ArrayList<Route>();
        //otherRoute = "";
        //childStops = new ArrayList<Comment>();
    }

    public static String cleanName(String name) {
        name = name.replaceAll("at", "@");
        return name;
    }

    public static void parseJSON(JSONObject Json) throws JSONException {


        JSONArray jComent = new JSONArray();
        BusManager sharedManager = BusManager.getBusManager();

        if (Json != null) jComent = Json.getJSONArray("comments");


        if (MainActivity.LOCAL_LOGV) Log.v(MainActivity.REFACTOR_LOG_TAG, "Parsing # comments: " + jComent.length());
        for (int i = 0; i < jComent.length(); i++) {
            //JSONObject stopObject = jStops.getJSONObject(i);
            JSONArray item = jComent.getJSONArray(i);
            if (item.length()>6) {
                String ID = item.getString(0);
                String idbus = item.getString(1);
                String title = item.getString(2);
                String body = item.getString(3);
                String route = item.getString(4);
                String loc = item.getString(5);
                String time = item.getString(6);
                String text = item.getString(7);
                //String stopLat = stopObject.getString(3);
                //String stopLng = stopObject.getString(2);
                Comment s = sharedManager.getComment(ID, text, time);
                s.setValues(ID,text,idbus,title,body,route,loc,time);
                //Comment s = new Comment(ID, Name, "", "");
            }
            //if (MainActivity.LOCAL_LOGV) Log.v(MainActivity.REFACTOR_LOG_TAG, "Number of stops in manager: " + sharedManager.numStops());
            //if (MainActivity.LOCAL_LOGV) Log.v(MainActivity.REFACTOR_LOG_TAG, "___after adding " + s.name);
        }

    }

    public boolean isHidden() {
        return hidden;
    }

    public void setHidden(boolean hidden) {
        this.hidden = hidden;
    }

    public void setValues(String mID, String mText, String midbus, String mtitle, String mbody, String mroute, String mloc, String mtime ) {
        //if (name.equals("")) name = cleanName(mName);
        //if (loc == null) loc = new LatLng(Double.parseDouble(mLat), Double.parseDouble(mLng));
        id = mID;
        text = mText;
        busId = midbus;
        busTitle = mtitle;
        busBody = mbody;
        route = mroute;
        //loc = mloc; !!need str2loc
        timestamp = Long.parseLong(mtime);
        //["5710239819104256","280","49","С043КТ","155","lat/lng: (43.1174487312232,131.882848364423)","1412914794768760","рпч пятница1"],
    }

    public LatLng getLocation() {
        return loc;
    }

    public String toString() {
        return text;
    }

    public String getID() {
        return id;
    }

    public String getBusId() {
        return busId;
    }
    public String getText() {
        return text;
    }

    public String getBusTxt() {
        return busId + ": " + busTitle + " " + busBody;
    }


    public void setName(String name) {
        this.text = name;
    }

    public void setLastUpdateTime(Long time) {
        this.lastUpdateTime = time;
    }

    public Boolean notExpireTime() {
        if((System.currentTimeMillis()-lastUpdateTime)>1000*60){
            return false;
        }else return true;

    }


}

