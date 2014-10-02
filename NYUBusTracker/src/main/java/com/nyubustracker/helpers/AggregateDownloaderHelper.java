package com.nyubustracker.helpers;

import com.nyubustracker.models.Bus;
import com.nyubustracker.models.Route;
import com.nyubustracker.models.Stop;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;

/**
 *
 */
public class AggregateDownloaderHelper  implements DownloaderHelper {
    @Override
    public void parse(JSONObject jsonObject) throws JSONException, IOException {
        //Bus.parseJSON(jsonObject);
        Stop.parseJSON(jsonObject.getJSONObject("data"));
        Route.parseJSON(jsonObject.getJSONObject("data"));
        //Stop.parseJSON(jsonObject);
    }
    public void parseArray(JSONArray jsonObject) throws JSONException, IOException {
        //Bus.parseJSONA(jsonObject);
    }
}
