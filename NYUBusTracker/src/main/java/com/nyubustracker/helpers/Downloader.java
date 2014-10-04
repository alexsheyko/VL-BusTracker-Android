package com.nyubustracker.helpers;

import android.app.Activity;
import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;

import com.nyubustracker.R;
import com.nyubustracker.activities.MainActivity;

import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONArray;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;

public class Downloader extends AsyncTask<String, Void, JSONObject> {
    final DownloaderHelper helper;
    public static final String CREATED_FILES_DIR = "VLBusCachedFiles";
    static Context context;
    //MainActivity main;

    public static Context getContext() {
        return context;
    }

    public static MainActivity getMain() {
        return null;
    }

    public Downloader(DownloaderHelper helper, Context mContext, MainActivity mMain) {
        this.helper = helper;
        context = mContext;
        //main = mMain;
    }

    @Override
    public JSONObject doInBackground(String... urls) {
        try {
            if (MainActivity.LOCAL_LOGV) Log.v(MainActivity.REFACTOR_LOG_TAG, "First url: " + urls[0]);
            String res = downloadUrl(urls[0]);
            //JSONArray jsa = new JSONArray(res);
            JSONObject jso = new JSONObject(res);
            if (MainActivity.LOCAL_LOGV) Log.v(MainActivity.REFACTOR_LOG_TAG, "end: " + urls[0]);

            return jso; //new JSONObject(res);
        } catch (IOException e) {
            //Log.e("JSON", "DownloadURL IO error.");
            e.printStackTrace();
        } catch (JSONException e) {
            //Log.e("JSON", "DownloadURL JSON error.");
            e.printStackTrace();
        }
        return null;
    }

    @Override
    protected void onPostExecute(JSONObject result) {
        try {
            if (MainActivity.LOCAL_LOGV) Log.v(MainActivity.REFACTOR_LOG_TAG, "helper class: " + helper.getClass() + " (" + MainActivity.downloadsOnTheWire + ")");
            helper.parse(result);
            if (MainActivity.LOCAL_LOGV) Log.v(MainActivity.REFACTOR_LOG_TAG, "helper end: " + helper.getClass() + " (" + MainActivity.downloadsOnTheWire + ")");
            /*if (main!=null) {
                ((MainActivity) main).setProgressBarIndeterminate(false); //WTF
            }*/

            if (!helper.getClass().toString().contains("BusDownloaderHelper")) MainActivity.pieceDownloadsTogether(context);
        } catch (JSONException e) {
            Log.d(MainActivity.REFACTOR_LOG_TAG, "JSON Exception while parsing in onPostExecute.");
            e.printStackTrace();
        } catch (IOException e) {
            Log.d(MainActivity.REFACTOR_LOG_TAG, "IO Exception while parsing in onPostExecute.");
        }
    }

    // Given a URL, establishes an HttpUrlConnection and retrieves
    // the web page content as a InputStream, which it returns as
    // a string.
    private String downloadUrl(String myUrl) throws IOException {
        InputStream is = null;

        try {
            URL url = new URL(myUrl);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setReadTimeout(10000 /* milliseconds */);
            conn.setConnectTimeout(15000 /* milliseconds */);
            conn.setRequestMethod("GET");
            conn.setDoInput(true);
            //conn.setRequestProperty("X-Mashape-Authorization", context.getString(R.string.mashape_api_key));
            // Starts the QUERY
            conn.connect();
            //int response = conn.getResponseCode();
            //Log.d("JSON", "The response is: " + response);
            is = conn.getInputStream();

            // Convert the InputStream into a string
            return readIt(is);

            // Makes sure that the InputStream is closed after the app is
            // finished using it.
        } finally {
            if (is != null) {
                is.close();
            }
        }
    }

    // Reads an InputStream and converts it to a String.
    private String readIt(InputStream stream) throws IOException {
        BufferedReader reader = new BufferedReader(new InputStreamReader(stream, "iso-8859-1"), 128);
        StringBuilder sb = new StringBuilder();
        String line;
        while ((line = reader.readLine()) != null) {
            sb.append(line);
        }
        return sb.toString();
    }

    public static void cache(String fileName, JSONObject jsonObject) throws IOException {
        if (jsonObject != null && !jsonObject.toString().isEmpty()) {
            File path = new File(context.getFilesDir(), CREATED_FILES_DIR);
            path.mkdir();
            File file = new File(path, fileName);
            BufferedWriter bufferedWriter = new BufferedWriter(new FileWriter(file));
            bufferedWriter.write(jsonObject.toString());
            bufferedWriter.close();
        }
    }

    public static String makeQuery(String param, String value, String charset) {
        try {
            return String.format(param + "=" + URLEncoder.encode(value, charset));
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        return "";
    }
}
