package com.augmentedcoders.realityguide;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.InputStream;
import java.net.URL;
import java.util.Locale;

public class ActivityPlacesDetails extends AppCompatActivity {
    final Context currentContext = this;

    GoogleMap googleMap;
    LatLng location;
    PointOfInterestPost post;
    JSONObject jsonContent;
    String name = "";
    String hours = "";
    String types= "";
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        if (!(Settings.selectedPost != null && Settings.selectedPost instanceof PointOfInterestPost)) {
            goBack();
            return;
        }
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_places_details);
        post = (PointOfInterestPost) Settings.selectedPost;
        jsonContent = post.jsonContent;
        try {
            JSONObject loc = jsonContent.getJSONObject("geometry").getJSONObject("location");
            location = new LatLng(loc.getDouble("lat"), loc.getDouble("lng"));

            name = jsonContent.getString("name");

            JSONArray typesJson = jsonContent.getJSONArray("types");
            for (int i = 0; i < typesJson.length(); i++) {
                types += typesJson.getString(i);
                if (i < typesJson.length() - 1) types += ", ";
            }
            types = "Type: " + types;

            Thread thread = new Thread(new Runnable() {
                @Override
                public void run() {
                    try {
                        Bitmap iconPicture = null;
                        String iconURL = jsonContent.getString("icon");
                        iconPicture = Bitmap.createScaledBitmap(
                                BitmapFactory.decodeStream((InputStream) new URL(iconURL).getContent())
                                , 105, 105, false);
                        ((ImageView) findViewById(R.id.imgDetailsIcon)).setImageBitmap(iconPicture);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            });
            thread.start();
            if (jsonContent.getJSONObject("opening_hours").getBoolean("open_now")) {
                hours = "Open now";
            } else {
                hours = "Closed right now";
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        ((TextView) findViewById(R.id.txtDetailsName)).setText(name);
        ((TextView) findViewById(R.id.txtDetailsTypes)).setText(types);
        ((TextView) findViewById(R.id.txtDetailsHours)).setText(hours);
        ((TextView) findViewById(R.id.txtDetailsLocation)).setText(location.latitude + ", "
                + location.longitude);
        googleMap = ((SupportMapFragment) getSupportFragmentManager().
                findFragmentById(R.id.detailsMap)).getMap();
        googleMap.setTrafficEnabled(true);
        MarkerOptions options = new MarkerOptions();
        options.position(location);
        googleMap.addMarker(options);
        googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(location, 17));
        findViewById(R.id.btnBack).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                goBack();
            }
        });
        findViewById(R.id.btnMapLauncher).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String uri = String.format(Locale.ENGLISH, "geo:%f,%f?q=%f,%f?z=19",
                        location.latitude, location.longitude,
                        location.latitude, location.longitude);
                currentContext.startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(uri)));
            }
        });
    }

    private void goBack() {
        startActivity(new Intent(this, ActivityDiscovery.class));
    }
}
