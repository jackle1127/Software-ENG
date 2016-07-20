package com.augmentedcoders.realityguide;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;

import com.augmentedcoders.realityguide.R;
import com.google.android.gms.maps.model.LatLng;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.InputStream;
import java.net.URL;
import java.text.NumberFormat;

public class CommunityPost extends Post {
    protected static Bitmap likeIcon = Bitmap.createScaledBitmap(
            BitmapFactory.decodeResource(Settings.resources,
                    R.drawable.heart_small), 27, 27, false);
    public Bitmap iconPicture = null;
    protected JSONObject jsonContent;
    protected JSONArray jsonLikes;
    protected String pictureURL = "";
    public CommunityPost() {
        super();
    }

    public CommunityPost(JSONObject content, JSONArray likes, String pic, Bitmap profile) {
        super();
        setData(content, likes, pic, profile);
    }

    public void setData(JSONObject content, JSONArray likes, String pic, Bitmap profile) {
        jsonContent = content;
        jsonLikes = likes;
        pictureURL = pic;
        if (profile != null) {
            iconPicture = Bitmap.createScaledBitmap(profile, 86, 86, false);
        }
        createContent();
    }

    public void createIconFromURL (final String url) {
        Bitmap iconBitmap = BitmapFactory.decodeResource(Settings.resources,
                R.drawable.default_profile);
        iconPicture = Bitmap.createScaledBitmap(iconBitmap, 86, 86, false);
        Thread separateThread = new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    System.out.println("Loading icon for " + jsonContent.getString("user"));
                    Bitmap iconBitmap = BitmapFactory.decodeStream((InputStream) new URL(url).getContent());
                    iconPicture = Bitmap.createScaledBitmap(iconBitmap, 86, 86, false);
                    System.out.println("Loaded icon of " + jsonContent.getString("user"));
                    createContent();
                    textured = false;
                    return;
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
        String timeStamp = "";
        String likes = "";
        String content = "";
        try {
            setLatLng(new LatLng(jsonContent.getDouble("lat"), jsonContent.getDouble("lng")));
            name = jsonContent.getString("user");
            timeStamp = jsonContent.getString("ts");
            content = jsonContent.getString("content");
            likes = NumberFormat.getIntegerInstance()
                    .format(jsonLikes.length());
            if (iconPicture == null) createIconFromURL(pictureURL);
        } catch (Exception e) {
            e.printStackTrace();
        }

        canvas.drawColor(0xEEE0E0E0);
        paint.setStyle(Paint.Style.STROKE);
        paint.setColor(0xFF777777);
        paint.setStrokeWidth(2);
        canvas.drawRect(2, 2, 398, 198, paint);

        paint.setStyle(Paint.Style.FILL);
        paint.setFilterBitmap(true);
        if (iconPicture != null) {
            canvas.drawBitmap(iconPicture, 8, 8, paint);
        }
        canvas.drawBitmap(likeIcon, 102, 71, paint);

        paint.setTextSize(26);
        paint.setFakeBoldText(true);
        paint.setColor(0xEE4488FF);
        drawTextInBound(canvas, name, new Rect(102, 8, 391, 42), paint, false);

        paint.setTextSize(15);
        paint.setFakeBoldText(false);
        paint.setColor(0xEE777777);
        drawTextInBound(canvas, timeStamp, new Rect(102, 45, 391, 67), paint, false);

        paint.setTextSize(18);
        paint.setFakeBoldText(false);
        paint.setColor(0xEE000000);
        drawTextInBound(canvas, likes, new Rect(135, 74, 391, 93), paint, false);

        paint.setTextSize(20);
        paint.setFakeBoldText(false);
        paint.setColor(0xEE444444);
        drawTextInBound(canvas, content, new Rect(8, 101, 391, 193), paint, true);

        super.setContent(bmpContent);
    }
}
