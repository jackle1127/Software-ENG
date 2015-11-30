package com.augmentedcoders.realityguide;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.widget.ImageView;
import android.widget.TextView;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.InputStream;
import java.net.URL;
import java.text.NumberFormat;

public class ActivityCommunityPost extends AppCompatActivity {

    CommunityPost post;
    JSONObject jsonContent;
    JSONObject jsonLikes;
    String pictureURL;
    String name = "";
    String timeStamp = "";
    String likes = "";
    String content = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        if (!(Settings.selectedPost != null && Settings.selectedPost instanceof CommunityPost)) {
            goBack();
            return;
        }
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_community_post);

        post = (CommunityPost) Settings.selectedPost;
        jsonContent = post.jsonContent;
        jsonLikes = post.jsonLikes;
        pictureURL = post.pictureURL;
        try {
            name = jsonContent.getString("user");
            timeStamp = jsonContent.getString("ts");
            content = jsonContent.getString("content");
            likes = NumberFormat.getIntegerInstance()
                    .format(jsonLikes.getJSONArray("likes").length());
            Thread thread = new Thread(new Runnable() {
                @Override
                public void run() {
                    try {
                        Bitmap iconPicture = null;
                        iconPicture = Bitmap.createScaledBitmap(
                                BitmapFactory.decodeStream((InputStream) new URL(pictureURL).getContent())
                                , 120, 120, false);
                        ((ImageView) findViewById(R.id.imgProfileIcon)).setImageBitmap(iconPicture);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            });
            thread.start();
        } catch (JSONException e) {
            e.printStackTrace();
        }
        ((TextView) findViewById(R.id.txtUsername)).setText(name);
        ((TextView) findViewById(R.id.txtTimeStamp)).setText(timeStamp);
        ((TextView) findViewById(R.id.txtLikes)).setText(likes);
        ((TextView) findViewById(R.id.txtContent)).setText(content);
    }

    private void goBack() {
        startActivity(new Intent(this, ActivityDiscovery.class));
    }

}
