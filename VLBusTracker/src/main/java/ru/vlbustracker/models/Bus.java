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
    private long lastUpdateTime;
    //private Integer last_update;

    public Bus(String mVehicleID) {
        vehicleID = mVehicleID;
        times = new ArrayList<Time>();
    }
    public static void parseJSONA(JSONArray devices) throws JSONException {
        BusManager sharedManager = BusManager.getBusManager();
        if (devices != null) {
            //hide all
            for (Bus b : sharedManager.getBuses()) {
                b.setHidden(true);
            }
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
                    Integer LastUpd = Integer.parseInt(d.getString(8));
                    Boolean hide = true;
                    if (LastUpd<300) hide=false;
                    Bus b = sharedManager.getBus(vehicleID);
                    b.setHeading(busHeading)
                            .setLocation(busLat, busLng)
                            .setRoute(busRoute)
                            .setTitle(busTitle)
                            .setHidden(hide)
                            .setBody(busBody);


                }
            }

        }
    }

    public static void parseJSON(JSONObject vehiclesJson) throws JSONException {
        BusManager sharedManager = BusManager.getBusManager();
        if (vehiclesJson != null) {

            //Log.v("BusLocations", " : " + vehiclesJson.length() + " | : ");
            /*
            //String str=EntityUtils.toString(rp.getEntity());
            JSONArray devices = new JSONArray(str);
            //js.length()
            for(int i=0;i<devices.length();i++){
                String d = devices.getString(i);
                d = d.substring(1,-1);
                String[] items = d.split(",");

                Log.e(":", d);
                Log.e(":", items[0]);
                //Log.e(":", d[1]);
                if (i>5){
                    break;
                }
            }
            */

            /*
            JSONObject jVehiclesData = vehiclesJson.getJSONObject(BusManager.TAG_DATA);
            if (jVehiclesData != null && jVehiclesData.has("72")) {
                JSONArray jVehicles = jVehiclesData.getJSONArray("72");
                for (int j = 0; j < jVehicles.length(); j++) {
                    JSONObject busObject = jVehicles.getJSONObject(j);
                    JSONObject busLocation = busObject.getJSONObject(BusManager.TAG_LOCATION);
                    String busLat = busLocation.getString(BusManager.TAG_LAT);
                    String busLng = busLocation.getString(BusManager.TAG_LNG);
                    String busRoute = busObject.getString(BusManager.TAG_ROUTE_ID);
                    String vehicleID = busObject.getString(BusManager.TAG_VEHICLE_ID);
                    String busHeading = busObject.getString(BusManager.TAG_HEADING);
                    // getBus will either return an existing bus, or create a new one for us. We'll have to parse the bus JSON often.
                    Bus b = sharedManager.getBus(vehicleID);
                    b.setHeading(busHeading).setLocation(busLat, busLng).setRoute(busRoute);
                    //if (MainActivity.LOCAL_LOGV) Log.v("BusLocations", "Parsing buses: bus id: " + vehicleID + " | bus' route: " + busRoute);
                    //if (MainActivity.LOCAL_LOGV) Log.v("JSONDebug", "Bus ID: " + vehicleID + " | Heading: " + busHeading + " | (" + busLat + ", " + busLng + ")");
                }
            }
            */
        }
    }

    Bus setLocation(String lat, String lng) {
        loc = new LatLng(Double.parseDouble(lat), Double.parseDouble(lng));
        return this;
    }

    public void setLastUpdateTime(Long time) {
        this.lastUpdateTime = time;
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

    public ArrayList<Time> getTimes() {
        ArrayList<Time> result = new ArrayList<Time>();
        if (MainActivity.LOCAL_LOGV) Log.v(MainActivity.LOG_TAG, "Times bus: (" + times.size() + " for " + this.getTitle() + " " + this.getID() + ")");
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
