package ru.vlbustracker.helpers;

import ru.vlbustracker.models.Bus;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;

public class BusDownloaderHelper implements DownloaderHelper {
    @Override
    public void parse(JSONObject jsonObject) throws JSONException, IOException {
        Bus.parseJSON(jsonObject);
    }
    public void parseArray(JSONArray jsonObject) throws JSONException, IOException {
        Bus.parseJSONA(jsonObject);
    }
}
