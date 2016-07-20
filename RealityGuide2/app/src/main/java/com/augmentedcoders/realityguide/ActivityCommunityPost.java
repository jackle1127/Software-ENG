package com.augmentedcoders.realityguide;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.text.InputType;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.LinearLayout.LayoutParams;
import android.widget.TextView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.InputStream;
import java.net.URL;
import java.text.NumberFormat;

public class ActivityCommunityPost extends AppCompatActivity {
    final Context currentContext = this;
    CommunityPost post;
    JSONObject jsonContent;
    JSONArray jsonLikes;
    Bitmap iconPicture;
    String pictureURL;
    String name = "";
    String timeStamp = "";
    String likes = "";
    String content = "";
    LayoutParams replyLayout = new LayoutParams(LayoutParams.MATCH_PARENT,
            LayoutParams.WRAP_CONTENT);
    LayoutParams separatorLayout = new LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
         1);

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        if (!(Settings.selectedPost != null && Settings.selectedPost instanceof CommunityPost)) {
            goBack();
            return;
        }
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_community_post);
        replyLayout.setMargins(20, 5, 20, 0);
        separatorLayout.setMargins(0, 15, 0, 10);

        findViewById(R.id.txtNewPostBack).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                goBack();
            }
        });
        post = (CommunityPost) Settings.selectedPost;
        jsonContent = post.jsonContent;
        pictureURL = post.pictureURL;
        try {
            name = jsonContent.getString("user");
            timeStamp = jsonContent.getString("ts");
            content = jsonContent.getString("content");
            Thread thread = new Thread(new Runnable() {
                @Override
                public void run() {
                    try {
                        iconPicture = Bitmap.createScaledBitmap(
                                BitmapFactory.decodeStream((InputStream) new URL(pictureURL).getContent())
                                , 120, 120, false);
                    } catch (Exception e) {
                        iconPicture = Bitmap.createScaledBitmap(BitmapFactory.decodeResource(Settings.resources,
                                R.drawable.default_profile)
                                , 120, 120, false);
                        e.printStackTrace();
                    }
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            ((ImageView) findViewById(R.id.imgProfileIcon)).setImageBitmap(iconPicture);
                        }
                    });
                }
            });
            thread.start();
        } catch (JSONException e) {
            e.printStackTrace();
        }
        ((TextView) findViewById(R.id.txtUsername)).setText(name);
        ((TextView) findViewById(R.id.txtTimeStamp)).setText(timeStamp);
        ((TextView) findViewById(R.id.txtContent)).setText(content);
        findViewById(R.id.btnReply).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Thread separate = new Thread(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            String replyContent = ((EditText) findViewById(R.id.txtReply)).getText()
                                    .toString().trim();
                            if (replyContent.length() == 0) return;
                            String toReply = "('";
                            toReply += jsonContent.getString("id") + "', '";
                            toReply += Settings.currentUser + "', \"";
                            toReply += replyContent + "\")";
                            DatabaseMethods.newReply(toReply);
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    ((EditText) findViewById(R.id.txtReply)).setText("");
                                }
                            });
                            refreshReplies();
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                });
                separate.start();
            }
        });
        findViewById(R.id.imgHeart).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Thread thread = new Thread(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            DatabaseMethods.LIKEME(jsonContent.getString("id"),
                                    Settings.currentUser);
                            refreshLikes();
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                });
                thread.start();
            }
        });
        refreshReplies();
        refreshLikes();
    }

    private void refreshLikes() {
        Thread thread = new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    String queriedLikes = DatabaseMethods.getLIKES(jsonContent.getString("id"));
                    jsonLikes = new JSONArray();
                    if (!queriedLikes.equals("null ")) {
                        jsonLikes = new JSONArray(queriedLikes);
                    }
                    likes = NumberFormat.getIntegerInstance()
                            .format(jsonLikes.length());
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            ((TextView) findViewById(R.id.txtLikes)).setText(likes);
                            ((ImageView) findViewById(R.id.imgHeart))
                                    .setImageResource(R.drawable.heart_medium);
                        }
                    });
                    for (int i = 0; i < jsonLikes.length(); i++) {
                        JSONObject currentLike = jsonLikes.getJSONObject(i);
                        String username = currentLike.getString("USER");
                        if (username.equals(Settings.currentUser)) {
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    ((ImageView) findViewById(R.id.imgHeart))
                                            .setImageResource(R.drawable.heart_medium_red);
                                }
                            });
                        }
                    }
                } catch (Exception e) {
                    iconPicture = Bitmap.createScaledBitmap(BitmapFactory.decodeResource(Settings.resources,
                            R.drawable.default_profile)
                            , 120, 120, false);
                    e.printStackTrace();
                }
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        ((ImageView) findViewById(R.id.imgProfileIcon)).setImageBitmap(iconPicture);
                    }
                });
            }
        });
        thread.start();
    }
    private void refreshReplies() {
        Thread thread = new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            ((LinearLayout) findViewById(R.id.layoutReplies)).removeAllViews();
                        }
                    });
                    String queriedReplies = DatabaseMethods.getReplies(
                            jsonContent.getString("id"));
                    JSONArray repliesArray = new JSONArray(queriedReplies);
                    for (int i = repliesArray.length() - 1;
                         i >= Math.max(0, repliesArray.length() - 10); i--) {
                        JSONObject jsonReply = repliesArray.getJSONObject(i);
                        final String userName = jsonReply.getString("user");
                        final String timeStamp = jsonReply.getString("ts");
                        final String content = jsonReply.getString("txt");
                        final boolean lastOne = (i == Math.max(0, repliesArray.length() - 10));
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                TextView txtUserName = new TextView(currentContext);
                                txtUserName.setTextSize(24);
                                txtUserName.setText(userName);
                                txtUserName.setTextColor(0xFF1155FF);
                                TextView txtTimeStamp = new TextView(currentContext);
                                txtTimeStamp.setTextSize(16);
                                txtTimeStamp.setText(timeStamp);
                                txtTimeStamp.setTextColor(0xFF999999);
                                TextView txtContent = new TextView(currentContext);
                                txtContent.setTextSize(20);
                                txtContent.setText(content);
                                txtContent.setTextColor(0xFF000000);
                                txtContent.setSingleLine(false);
                                ((LinearLayout) findViewById(R.id.layoutReplies)).addView(txtUserName
                                        , replyLayout);
                                ((LinearLayout) findViewById(R.id.layoutReplies)).addView(txtTimeStamp
                                        , replyLayout);
                                ((LinearLayout) findViewById(R.id.layoutReplies)).addView(txtContent
                                        , replyLayout);
                                if (!lastOne) {
                                    View separator = new View(currentContext);
                                    separator.setBackgroundColor(0xFF333333);
                                    ((LinearLayout) findViewById(R.id.layoutReplies)).addView(separator
                                            , separatorLayout);
                                }
                            }
                        });
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
        thread.start();
    }

    private void goBack() {
        if (Settings.goBackToAccount) {
            startActivity(new Intent(currentContext, ActivityAccount.class));
        } else {
            startActivity(new Intent(this, ActivityDiscovery.class));
        }
    }
}
