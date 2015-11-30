package com.augmentedcoders.realityguide;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;

import com.google.android.gms.maps.model.LatLng;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.InputStream;
import java.net.URL;

public class PointOfInterestPost extends Post {
    public Bitmap iconPicture = null;
    protected JSONObject jsonContent;

    PointOfInterestPost() {
        super();
    }
    public PointOfInterestPost(JSONObject content, Bitmap icon) {
        super();
        setData(content, icon);
    }

    public void setData(JSONObject content, Bitmap icon) {
        if (icon != null) {
            iconPicture = Bitmap.createScaledBitmap(icon, 105, 105, false);
        }
        jsonContent = content;
        createContent();
    }

    public void createIconFromURL (final String url) {
        Thread separateThread = new Thread(new Runnable() {
            @Override
            public void run() {
                Bitmap iconBitmap = null;
                try {
                    System.out.println("Loading icon for " + jsonContent.getString("name"));
                    iconBitmap = BitmapFactory.decodeStream((InputStream) new URL(url).getContent());
                    System.out.println("Loaded");
                    iconPicture = Bitmap.createScaledBitmap(iconBitmap, 105, 105, false);
                    textured = false;
                    createContent();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
        separateThread.start();
    }

    public void createContent() {
        Bitmap bmpContent = Bitmap.createBitmap(400, 200, Bitmap.Config.ARGB_8888);
        Paint paint = new Paint();
        Canvas canvas = new Canvas(bmpContent);

        String name = "";
        String hours = "";
        String types= "";
        try {
            JSONObject location = jsonContent.getJSONObject("geometry").getJSONObject("location");
            setLatLng(new LatLng(location.getDouble("lat"), location.getDouble("lng")));
            name = jsonContent.getString("name");
            if (iconPicture == null) createIconFromURL(jsonContent.getString("icon"));
            JSONArray typesJson = jsonContent.getJSONArray("types");
            for (int i = 0; i < typesJson.length(); i++) {
                types += typesJson.getString(i);
                if (i < typesJson.length() - 1) types += ", ";
            }
            types = "Type: " + types;
            if (jsonContent.getJSONObject("opening_hours").getBoolean("open_now")) {
                hours = "Open now";
            } else {
                hours = "Closed right now";
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }

        canvas.drawColor(0xEE4499FF);
        paint.setStyle(Paint.Style.STROKE);
        paint.setColor(0xFFFFFFFF);
        paint.setStrokeWidth(2);
        canvas.drawRect(2, 2, 398, 198, paint);

        paint.setStyle(Paint.Style.FILL);
        if (iconPicture != null) {
            paint.setFilterBitmap(true);
            canvas.drawBitmap(iconPicture, 8, 8, paint);
        }

        paint.setTextSize(26);
        paint.setFakeBoldText(true);
        paint.setColor(0xEEE5E100);
        drawTextInBound(canvas, name, new Rect(127, 8, 386, 57), paint, false);

        paint.setTextSize(20);
        paint.setFakeBoldText(false);
        paint.setColor(0xEEFFFFFF);
        drawTextInBound(canvas, hours, new Rect(127, 69, 386, 111), paint, false);

        paint.setTextSize(18);
        paint.setFakeBoldText(false);
        paint.setColor(0xEE000000);
        drawTextInBound(canvas, types, new Rect(9, 121, 386, 194), paint, true);

        setContent(bmpContent);
        textured = false;
    }
}
