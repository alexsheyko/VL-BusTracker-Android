package ru.vlbustracker.helpers;

import android.os.AsyncTask;
import android.util.Log;
import android.view.View;

import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.HttpClient;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import ru.vlbustracker.activities.MainActivity;

/**
 * Created by Шейко on 08.10.14.
 */

public class Poster extends AsyncTask<String, Integer, Double> {

    @Override
    protected Double doInBackground(String... params) {
// TODO Auto-generated method stub
        if (MainActivity.LOCAL_LOGV) Log.v(MainActivity.REFACTOR_LOG_TAG, "Poster url: " + params[0]);
        postData(params[0]);
        return null;
    }

    protected void onPostExecute(Double result){
        //pb.setVisibility(View.GONE);
        //Toast.makeText(getApplicationContext(), "command sent", Toast.LENGTH_LONG).show();
    }
    protected void onProgressUpdate(Integer... progress){
        //pb.setProgress(progress[0]);
    }

    public void postData(String valueIWantToSend) {
        HttpClient httpclient = new DefaultHttpClient();
        HttpPost httppost = new HttpPost(DownloaderHelper.COMMENT_URL);

        try {

            List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>();
            nameValuePairs.add(new BasicNameValuePair("myHttpData", valueIWantToSend));
            httppost.setEntity(new UrlEncodedFormEntity(nameValuePairs));

            HttpResponse response = httpclient.execute(httppost);

        } catch (ClientProtocolException e) {
// TODO Auto-generated catch block
        } catch (IOException e) {
// TODO Auto-generated catch block
        }
    }

}