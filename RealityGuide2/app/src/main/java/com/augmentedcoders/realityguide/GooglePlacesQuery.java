package com.augmentedcoders.realityguide;

import android.graphics.BitmapFactory;

import com.augmentedcoders.realityguide.R;

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
                try {
                    String dummy = "{\n" +
                            "         \"id\" : \"14\",\n" +
                            "         \"user\" : \"Jack the man\",\n" +
                            "         \"content\" : \"Imma a potato ahhhhhhhhh\",\n" +
                            "         \"lat\" : 33.7510001,\n" +
                            "         \"lng\" : -84.3858811,\n" +
                            "         \"ts\" : \"11/27/2030\"\n" +
                            "      }";
                    String likes =  "{\"likes\": [ \"park\", \"point_of_interest\", \"establishment\" ]}";
                    JSONObject dummyObject = new JSONObject(dummy);
                    JSONObject dummyLikes = new JSONObject(likes);
                    String pic = "https://scontent.xx.fbcdn.net/hphotos-xtf1/v/t1.0-9/11254663_10207737724942539_3145118592722163480_n.jpg?oh=b661326c97e86a13e7f1a05ca2207487&oe=56DA00D9";
                    CommunityPost newCom = new CommunityPost(dummyObject, dummyLikes, pic, null);
                    Settings.communityPosts.add(newCom);
                } catch (Exception e) {
                    e.printStackTrace();
                }
                InputStream inputStream = null;
                HttpURLConnection http = null;
                String result = "";
                try {
                    String urlString = "https://maps.googleapis.com/maps/api/place/nearbysearch/json?";
                    urlString += "location=" + Settings.currentLat + "," + Settings.currentLon;
                    urlString += "&radius=" + Settings.QUERY_RADIUS;
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
            Settings.pointOfInterestPosts.clear();
            for (int i = 0; i < jsonArray.length(); i++) {
                JSONObject newPlace = jsonArray.getJSONObject(i);
                System.out.println("Adding " + newPlace.getString("name"));
                PointOfInterestPost newPost = new PointOfInterestPost(newPlace, null);
                Settings.pointOfInterestPosts.add(newPost);
                System.out.println(i + " - " + newPost.getLocation().x + ", " + newPost.getLocation().z);
            }
            processResult.run();
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }
}