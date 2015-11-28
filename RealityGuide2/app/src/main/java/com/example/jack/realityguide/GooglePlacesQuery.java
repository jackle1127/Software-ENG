package com.example.jack.realityguide;

import android.os.AsyncTask;

import com.google.android.gms.maps.model.LatLng;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

public class GooglePlacesQuery {
    public void execute(final Runnable processResult) {
        Runnable run = new Runnable() {
            @Override
            public void run() {
                InputStream inputStream = null;
                HttpURLConnection http = null;
                String result = "";
                try {
                    String urlString = "https://maps.googleapis.com/maps/api/place/nearbysearch/json?";
                    urlString += "location=" + Settings.currentLat + "," + Settings.currentLon;
                    urlString += "&radius=50";
                    //urlString += "&types=restaurant";
                    urlString += "&key=" + Settings.serverKey;
                    urlString += "&sensor=true";
                    System.out.println(urlString);
                    URL url = new URL(urlString);
                    http = (HttpURLConnection) url.openConnection();
                    System.out.println("opened");
                    http.connect();
                    System.out.println("connected");
                    inputStream = http.getInputStream();
                    BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
                    StringBuffer stringBuffer = new StringBuffer();
                    String line = "";
                    while ((line = reader.readLine()) != null) {
                        stringBuffer.append(line);
                    }
                    result = stringBuffer.toString();
                    reader.close();
                } catch (Exception e) {
                    e.printStackTrace();
                } finally {
                    try {
                        if (inputStream != null) {
                            inputStream.close();
                            http.disconnect();
                        }
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
                done(processResult, result);
            }
        };
        Thread thread = new Thread(run);
        thread.start();
    }

    protected void done(Runnable processResult, String result) {
        try {
            System.out.println(result);
            Settings.placesReady = true;
            JSONObject jsonObject = new JSONObject(result);
            JSONArray jsonArray = jsonObject.getJSONArray("results");
            Settings.latLngs.clear();
            for (int i = 0; i < jsonArray.length(); i++) {
                JSONObject newPlace = jsonArray.getJSONObject(i);
                JSONObject location = newPlace.getJSONObject("geometry").getJSONObject("location");
                Post newPost = new Post();
                newPost.setContent(MathFunctions.testLabel(i + ""));
                newPost.setLatLng(new LatLng(location.getDouble("lat"), location.getDouble("lng")));
                Settings.latLngs.add(newPost);
                System.out.println(newPost.getLocation().x + ", " + newPost.getLocation().z);
            }
            processResult.run();
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }
}
