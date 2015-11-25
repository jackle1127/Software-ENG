package com.example.jack.realityguide;

import android.hardware.SensorManager;
import android.location.LocationManager;
import android.view.Display;

public class Settings {
    // Constants
    protected static final String PREFERENCES_NAME = "RG_PREFS";

    // Common objects
    protected static Display display;
    protected static LocationManager locationManager;
    protected static SensorManager sensorManager;

    // Common variables (unchangeable by users)
    protected static float meterPerPixel = 1.0f;
    protected static float mapScale = 100;
    protected static int camWidth = 0;
    protected static int camHeight = 0;
    protected static float angleOfView = -1.0f;
    protected static int numberOfCameras = -1;
    protected static int cameraQuality = 0;
    protected static double currentLat = 33.7523778;
    protected static double currentLon = -84.38681;
    protected static float mapZoom = 19;
    protected static int screenOrientation = 1;
    // Common settings (changeable by users)
    protected static boolean gyroMode = true;
    protected static float distanceFromGround = 19.5f;
}
