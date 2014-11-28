package ru.vlbustracker.helpers;

import ru.vlbustracker.models.Bus;
import ru.vlbustracker.models.Route;
import ru.vlbustracker.models.Stop;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;

/**
 *
 */
public class AggregateDownloaderHelper  implements DownloaderHelper {
    public static final String ARG_JSON_FILE = "aggregateJson";

    @Override
    public void parse(JSONObject jsonObject) throws JSONException, IOException {
        //Bus.parseJSON(jsonObject);
        if (jsonObject.has("data")) {
            Stop.parseJSON(jsonObject.getJSONObject("data"));
            Route.parseJSON(jsonObject.getJSONObject("data"));
            Downloader.cache(ARG_JSON_FILE, jsonObject);
        }
    }
    public void parseArray(JSONArray jsonObject) throws JSONException, IOException {
        //Bus.parseJSONA(jsonObject);
    }
}
