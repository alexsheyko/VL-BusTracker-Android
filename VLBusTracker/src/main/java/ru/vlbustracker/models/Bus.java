package ru.vlbustracker.models;

import android.util.Log;
import android.view.ViewDebug;

import com.google.android.gms.maps.model.LatLng;
import ru.vlbustracker.activities.MainActivity;
import ru.vlbustracker.helpers.BusManager;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

public class Bus {
    private String vehicleID = "";
    private LatLng loc;
    private String heading = "";
    private String route;
    private String title ="";
    private String body ="";
    ArrayList<Time> times = null;
    boolean hidden;
    private long lastUpdateTime;    //for Estimate time
    private long lastUpdateProg;       //for bus out in night
    private long lastUpdateBus;       //for bus out in night
    //private Integer last_update;

    public Bus(String mVehicleID) {
        vehicleID = mVehicleID;
        times = new ArrayList<Time>();
        lastUpdateProg=System.currentTimeMillis();
    }
    public static void parseJSONA(JSONArray devices) throws JSONException {
        BusManager sharedManager = BusManager.getBusManager();
        if (devices != null) {
            //hide all
            for (Bus b : sharedManager.getBuses()) {
                b.setHidden(true);
            }
            if (MainActivity.LOCAL_LOGV) Log.v(MainActivity.REFACTOR_LOG_TAG, "Bus parse : " + devices.length() + " | : ");
            //load
            for(int i=0;i<devices.length();i++){
                JSONArray d = devices.getJSONArray(i);
                if (d.length()>8){
                    /*
					'udid': d[0],
					'name': d[1],
					'id_route': d[2],
					'lon': d[3],
					'lat': d[4],
					'bearing': d[5],
					'num': d[6],
					'id_group': d[7],
					'last_update': d[8]
                     *  */
                    String busLat = d.getString(4);
                    String busLng = d.getString(3);
                    String busRoute = Integer.toString(d.getInt(2));
                    String vehicleID = d.getString(0);
                    String busHeading = d.getString(5);
                    String busTitle = d.getString(6);
                    String busBody = d.getString(1);
                    Long LastUpd = Long.parseLong(d.getString(8));
                    Boolean hide = true;
                    if (LastUpd<300) hide=false;
                    Bus b = sharedManager.getBus(vehicleID);
                    b.setHeading(busHeading)
                            .setLocation(busLat, busLng)
                            .setRoute(busRoute)
                            .setTitle(busTitle)
                            .setHidden(hide)
                            .setLastUpdateBus(LastUpd)
                            .setBody(busBody);
                    b.setLastUpdateProg(System.currentTimeMillis());

                }
            }

        }
    }

    public static void parseJSON(JSONObject vehiclesJson) throws JSONException {
        BusManager sharedManager = BusManager.getBusManager();
        if (vehiclesJson != null) {
            //hide all
            for (Bus b : sharedManager.getBuses()) {
                b.setHidden(true);
            }
            if (MainActivity.LOCAL_LOGV)
                Log.v(MainActivity.REFACTOR_LOG_TAG, "Bus parse : " + vehiclesJson.length() + " | : ");
            JSONArray devices = vehiclesJson.getJSONArray("anims");
            //load
            for (int i = 0; i < devices.length(); i++) {
                JSONObject d = devices.getJSONObject(i);
                //{"id":"1145",
                // "lon":131901657,
                // "lat":43118900,
                // "dir":46,
                // "lasttime":"28.11.2014 12:51:28",
                // "gos_num":"",
                // "rid":65,
                // "rnum":"16Р¦",
                // "rtype":"Рђ",
                // "anim_key":44165,
                // "big_jump":"0",
                // "anim_points":[]}
                Bus b = sharedManager.getBus(d.getString("id"));
                String busLat = d.getString("lat");
                String busLng = d.getString("lon");
                busLat=busLat.substring(0,2)+"."+busLat.substring(2);
                busLng=busLng.substring(0,3)+"."+busLng.substring(3);
                Long LastUpd = Long.parseLong("0");
                b.setHeading("0")                       //угол наклона
                        .setLocation(busLat, busLng)
                        .setRoute(d.getString("dir"))
                        .setTitle(d.getString("rnum"))
                        .setHidden(false)
                        .setLastUpdateBus(LastUpd)
                        .setBody(d.getString("rnum"));

                b.setLastUpdateProg(System.currentTimeMillis());
            }
        }
    }

    Bus setLocation(String lat, String lng) {
        loc = new LatLng(Double.parseDouble(lat), Double.parseDouble(lng));
        return this;
    }

    public void setLastUpdateTime(Long time) {
        this.lastUpdateTime = time;
    }

    public void setLastUpdateProg(Long time) {
        this.lastUpdateProg = time;
    }
    public Bus setLastUpdateBus(Long time) {
        lastUpdateBus = time;
        return this;
    }

    public long getLastUpdateBus() {
        return this.lastUpdateBus;
    }

    public String getLastUpdateBusStr() {
        if (lastUpdateBus<60){
            return Long.toString(lastUpdateBus)+"сек.";
        }else{
            if (lastUpdateBus<3600){
                return Long.toString(Math.round(lastUpdateBus/60))+"мин.";
            }else{
                return " >1ч.";
            }
        }
    }

    public void cleanTime() {
        times.clear();
    }

    public void addTime(Time t) {
        times.add(t);
    }

    public Boolean notExpireTime() {
        if((System.currentTimeMillis()-lastUpdateTime)>1000*60){
            return false;
        }else return true;

    }

    public Boolean outFromLine() {
        if((System.currentTimeMillis()-lastUpdateProg)>1000*60*30){
            return true;
        }else return false;

    }

    public ArrayList<Time> getTimes() {
        ArrayList<Time> result = new ArrayList<Time>();
        //if (MainActivity.LOCAL_LOGV) Log.v(MainActivity.LOG_TAG, "Times bus: (" + times.size() + " for " + this.getTitle() + " " + this.getID() + ")");
        for (Time t : times) {
            if (t.notExpire()) {
                result.add(t);
            }
        }

        /*for (Stop childStop : childStops) {
            result.addAll(childStop.getTimesOfRoute(route));
        }*/

        return result;
    }

    public LatLng getLocation() {
        return loc;
    }

    public String getRoute() {
        return route;
    }

    Bus setRoute(String mRoute) {
        route = mRoute;
        return this;
    }

    public Float getHeading() {
        try {
            return Float.parseFloat(heading);
        } catch (Exception e) {
            return 0f;
        }
    }

    Bus setHeading(String mHeading) {
        heading = mHeading;
        return this;
    }

    public String getTitle() {
        return title;
    }

    Bus setTitle(String mTitle) {
        title = mTitle;
        return this;
    }

    public String getBody() {
        return body;
    }

    Bus setBody(String mBody) {
        body = mBody;
        return this;
    }

    public boolean isHidden() {
        return hidden;
    }

    Bus setHidden(boolean hidden) {
        this.hidden = hidden;
        return this;
    }
    public String getID() {
        return vehicleID;
    }
}
