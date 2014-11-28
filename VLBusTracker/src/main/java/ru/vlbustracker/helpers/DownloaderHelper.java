package ru.vlbustracker.helpers;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;

public interface DownloaderHelper {
    //static final String TRANSLOC_URL = "https://transloc-api-1-2.p.mashape.com";
    //static final String QUERY = Downloader.makeQuery("agencies", "72", "UTF-8");
    //static final String STOPS_URL = TRANSLOC_URL + "/stops.json?" + QUERY;
    //static final String ROUTES_URL = TRANSLOC_URL + "/routes.json?" + QUERY;
    //static final String SEGMENTS_URL = TRANSLOC_URL + "/segments.json?" + QUERY;
    //static final String VEHICLES_URL = TRANSLOC_URL + "/vehicles.json?" + QUERY;
    //static final String AMAZON_URL = "https://s3.amazonaws.com/nyubustimes/1.0/";
    //static final String VERSION_URL = AMAZON_URL + "version.json";

    //VLBus
    static final String QUERY_age = Downloader.makeQuery("age", "1800", "UTF-8");
    static final String BUS_VL_URL = "http://map.vl.ru/api/transport/";
    static final String CUR_URL         = BUS_VL_URL+"bus/current?id_route=false&"+QUERY_age;
    static final String AGR_URL         = BUS_VL_URL+"bus/aggregate";
    static final String STOP_TIME_URL   = BUS_VL_URL+"stop/estimates?id_node=";
    static final String BUS_TIME_URL    = BUS_VL_URL+"bus/estimates?udid=";

    static final String BUS125_URL= "http://bus125.ru/php/";
    static final String BUS_VM_URL    = BUS125_URL+"getVehiclesMarkers.php?rids=28-0,29-0,98-0,99-0,61-0,62-0,100-0,101-0,1-1,2-1,20-0,21-0,97-0,34-0,35-0,57-0,58-0,112-0,96-0,65-0,66-0,88-0,89-0,26-0,27-0,10-0,11-0,8-0,9-0,93-0,1-0,2-0,22-0,23-0,108-0,109-0,32-0,40-0,41-0,36-0,37-0,90-0,91-0,77-0,78-0,81-0,82-0,104-0,105-0,4-0,5-0,53-0,54-0,12-0,13-0,75-0,76-0,94-0,95-0,59-0,60-0,6-0,7-0,19-0,16-0,49-0,50-0,43-0,44-0,18-0,17-0,107-0,106-0,102-0,103-0,63-0,64-0,67-0,68-0,73-0,74-0,45-0,46-0,79-0,80-0,85-0,86-0,110-0,111-0,14-0,15-0,51-0,52-0,47-0,48-0,42-0,92-0&lat0=0&lng0=0&lat1=90&lng1=180&curk=0&city=vladivostok&info=01234&_=";

    static final String COMMENT_ADD_URL    = "http://busvlru.appspot.com/add/comment/";
    static final String COMMENT_GET_URL    = "http://busvlru.appspot.com/get/comment/?last=";

    public abstract void parse(JSONObject jsonObject) throws JSONException, IOException;
    public abstract void parseArray(JSONArray jsonObject) throws JSONException, IOException;
}
