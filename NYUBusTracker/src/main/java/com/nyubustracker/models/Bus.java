package com.nyubustracker.models;

import android.view.ViewDebug;

import com.google.android.gms.maps.model.LatLng;
import com.nyubustracker.helpers.BusManager;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class Bus {
    private String vehicleID = "";
    private LatLng loc;
    private String heading = "";
    private String route;
    private String title ="";

    public Bus(String mVehicleID) {
        vehicleID = mVehicleID;
    }
    public static void parseJSONA(JSONArray devices) throws JSONException {
        BusManager sharedManager = BusManager.getBusManager();
        if (devices != null) {
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
                    String busHeading = "45";
                    String busTitle = d.getString(1);
                    Bus b = sharedManager.getBus(vehicleID);
                    b.setHeading(busHeading).setLocation(busLat, busLng).setRoute(busRoute).setTitle(busTitle);


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

    public String getID() {
        return vehicleID;
    }
}
