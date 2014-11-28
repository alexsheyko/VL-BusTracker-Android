package ru.vlbustracker.helpers;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;

import ru.vlbustracker.models.Bus;
import ru.vlbustracker.models.Comment;
import ru.vlbustracker.models.Route;
import ru.vlbustracker.models.Stop;

public class CommentsDownloaderHelper implements DownloaderHelper {
    public static final String JSON_FILE = "commentsJson";

    @Override
    public void parse(JSONObject jsonObject) throws JSONException, IOException {
        if (jsonObject.has("data")) {
            Comment.parseJSON(jsonObject.getJSONObject("data"));
        }
        //Route.parseJSON(jsonObject.getJSONObject("data"));
        //Downloader.cache(JSON_FILE, jsonObject);
    }
    public void parseArray(JSONArray jsonObject) throws JSONException, IOException {
       // Bus.parseJSONA(jsonObject);
    }
}
