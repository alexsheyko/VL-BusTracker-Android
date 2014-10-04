package ru.vlbustracker.helpers;

import android.util.Log;

import ru.vlbustracker.activities.MainActivity;
import ru.vlbustracker.models.Time;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

public class TimeDownloaderHelper implements DownloaderHelper {
    String parentId;
    Integer typeId;

    public TimeDownloaderHelper(String mstopId, Integer mtypeid) {
        typeId = mtypeid;
        parentId = mstopId;
    }
    @Override
    public void parse(JSONObject jsonObject) throws JSONException, IOException {
        /*
        if (jsonObject != null && jsonObject.toString().length() > 0) {
            BusManager.parseTime(jsonObject);
            //if (MainActivity.LOCAL_LOGV) {
            //    Log.v(MainActivity.LOG_TAG, "Creating time cache file: " + jsonObject.getString("stop_id"));
            //    Log.v(MainActivity.LOG_TAG, "*   result: " + jsonObject.toString());
            //}
            //Downloader.cache(jsonObject.getString("stop_id"), jsonObject);
        }
        else {
            throw new JSONException(jsonObject == null
                    ? "TimeDownloaderHelper#parse given null jsonObject"
                    : "TimeDownloaderHelper#parse given empty jsonObject");
        }
        */
    }
    public void parseArray(JSONArray jsonObject) throws JSONException, IOException {
        //BusManager.parseTime(jsonObject);
        Time.parseJSONA(jsonObject,parentId, typeId);
    }

}
