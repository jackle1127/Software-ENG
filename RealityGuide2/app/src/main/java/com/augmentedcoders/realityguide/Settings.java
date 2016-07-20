package com.augmentedcoders.realityguide;

import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Path;
import android.hardware.SensorManager;
import android.location.LocationManager;
import android.view.Display;

import com.google.android.gms.maps.model.LatLng;

import java.util.ArrayList;

public class Settings {
    // Common objects
    protected static Display display;
    protected static LocationManager locationManager;
    protected static SensorManager sensorManager;

    // Common variables (unchangeable by users)
    protected static String currentUser = null;
    protected static Bitmap currentProfilePhoto;
    protected static Resources resources;
    protected static String apiKey;
    protected static String serverKey;
    protected static int camWidth = 0;
    protected static int camHeight = 0;
    protected static float angleOfView = -1.0f;
    protected static int numberOfCameras = -1;
    protected static int cameraQuality = 0;
    protected static double currentLat = 33.7508001;
    protected static double currentLon = -84.3858811;
    protected static double prevLat = 0;
    protected static double prevLon = 0;
    protected static float mapZoom = 17;
    protected static int screenOrientation = 1;
    protected static Bitmap mapOverlay;
    protected static Canvas mapOverlayCanvas;
    protected static Paint mapOverlayPaint;
    protected static Path mapOverlapPath;
    protected static boolean placesReady = true;
    protected static boolean postReady = true;
    protected static ArrayList<CommunityPost> communityPosts = new ArrayList();
    protected static ArrayList<PointOfInterestPost> pointOfInterestPosts = new ArrayList();
    protected static int postID = 1;
    protected static float[] anchorMatrix = new float[16];
    protected static Post selectedPost = null;
    protected static boolean goBackToAccount = false;
    // Common settings (changeable by users)
    protected static boolean gyroMode = true;

    // Constants
    protected static final String PREFERENCES_NAME = "RG_PREFS";
    protected static final float EARTH_RADIUS = 6371000;
    protected static final float QUERY_RADIUS = 40;
    protected static final float POST_SIZE_MULTIPLIER = 1.5f;
    protected static final float POST_ANGLE_MULTIPLIER = 30;
    protected static final float MINIMUM_DISTANCE = 6;
    protected static final float MAXIMUM_DISTANCE = 600;
    protected static final LatLng MOCK_1 = new LatLng(33.7513646, -84.3854675);
    protected static final LatLng MOCK_2 = new LatLng(33.7512475, -84.3855815);
    protected static final LatLng MOCK_3 = new LatLng(33.7515549, -84.3856433);

}
