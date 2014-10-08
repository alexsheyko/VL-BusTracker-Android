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
    String id;
    String text;
    String busId;
    String busTitle;
    String busBody;
    String route;
    String date;
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
            if (item.length()>1) {
                String ID = item.getString(0);
                String Name = item.getString(1);
                //String stopLat = stopObject.getString(3);
                //String stopLng = stopObject.getString(2);

                Comment s = sharedManager.getComment(ID, Name);
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

    public void setValues(String mID, String mText) {
        //if (name.equals("")) name = cleanName(mName);
        //if (loc == null) loc = new LatLng(Double.parseDouble(mLat), Double.parseDouble(mLng));
        id = mID;
        text = mText;
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

    public String getText() {
        return text;
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

