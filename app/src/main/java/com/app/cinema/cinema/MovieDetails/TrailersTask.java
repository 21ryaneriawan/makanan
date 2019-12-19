package com.app.cinema.cinema.MovieDetails;

import android.os.AsyncTask;
import android.support.v7.app.AlertDialog;
import android.util.Log;

import com.app.cinema.cinema.BuildConfig;
import com.app.cinema.cinema.Dashboard;
import com.app.cinema.cinema.MovieComponents.Trailers;
import com.app.cinema.cinema.R;
import com.app.cinema.cinema.utilities.NetworkUtils;
import com.google.gson.Gson;

import org.apache.commons.io.IOUtils;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

public class TrailersTask extends AsyncTask<Long, Void, List<Trailers>> {

    public static String LOG_TAG = TrailersTask.class.getSimpleName();

    private final Listener mListener;

    public TrailersTask(Listener mListener) {
        this.mListener = mListener;
    }

    interface Listener {
        void onLoadFinished(List<Trailers> trailers);
    }

    @Override
    protected List<Trailers> doInBackground(Long... params) {
        List<Trailers> trailers = new ArrayList<>();
        // If there's no movie id, there's nothing to look up.
        if (params.length == 0) {
            return null;
        }
            try {

                URL url;
                url = new URL("https://kitsu.io/api/edge/anime");
                HttpURLConnection connection = (HttpURLConnection) url.openConnection(); //Opening a http connection  to the remote object
                connection.connect();

                InputStream inputStream = connection.getInputStream(); //reading from the object
                String results = IOUtils.toString(inputStream);  //IOUtils to convert inputstream objects into Strings type
                parseJson(results,trailers);
                inputStream.close();
                return trailers;
            } catch (IOException e) {
                e.printStackTrace();
            }


        return null;
    }

    public static void parseJson(String data, List<Trailers> list){
        Trailers trailers = new Trailers();

        try {
            JSONObject mainObject = new JSONObject(data);
            //Log.v(LOG_TAG,mainObject.toString());
            JSONArray resArray = mainObject.getJSONArray("data"); //Getting the results object
            for (int i = 0; i < resArray.length(); i++) {
                JSONObject jsonObject = resArray.getJSONObject(i);
                trailers.setmId(jsonObject.getString("id"));
                JSONObject tr=jsonObject.getJSONObject("attributes");
                trailers.setmKey(tr.getString("canonicalTitle"));
                trailers.setmName(tr.getString("ratingRank"));
                trailers.setmSite(tr.getString("popularityRank"));
                trailers.setmSize(tr.getString("subtype"));
                //Log.e(LOG_TAG, jsonObject.toString());
                list.add(trailers);
            }
        } catch (JSONException e) {
            e.printStackTrace();
            Log.e(LOG_TAG, "Error occurred during JSON Parsing");
        }

    }

    @Override
    protected void onPostExecute(List<Trailers> trailers) {
        if (trailers != null) {
            mListener.onLoadFinished(trailers);
        } else {
            mListener.onLoadFinished(new ArrayList<Trailers>());
        }
    }


    private String readStream(InputStream in) {
        BufferedReader reader = null;
        StringBuffer response = new StringBuffer();
        try {
            reader = new BufferedReader(new InputStreamReader(in));
            String line = "";
            while ((line = reader.readLine()) != null) {
                response.append(line);
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (reader != null) {
                try {
                    reader.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        return response.toString();
    }
}
