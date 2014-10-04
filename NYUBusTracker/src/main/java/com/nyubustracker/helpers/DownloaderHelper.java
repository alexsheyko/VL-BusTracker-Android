package com.nyubustracker.helpers;

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
    static final String AMAZON_URL = "https://s3.amazonaws.com/nyubustimes/1.0/";
    //static final String VERSION_URL = AMAZON_URL + "version.json";

    //VLBus
    static final String QUERY_age = Downloader.makeQuery("age", "1800", "UTF-8");
    static final String BUS_VL_URL = "http://map.vl.ru/api/transport/";
    static final String CUR_URL         = BUS_VL_URL+"bus/current?id_route=false&"+QUERY_age;
    static final String AGR_URL         = BUS_VL_URL+"bus/aggregate";
    static final String STOP_TIME_URL   = BUS_VL_URL+"stop/estimates?id_node=";
    static final String BUS_TIME_URL    = BUS_VL_URL+"bus/estimates?udid=";


    public abstract void parse(JSONObject jsonObject) throws JSONException, IOException;
    public abstract void parseArray(JSONArray jsonObject) throws JSONException, IOException;
}
