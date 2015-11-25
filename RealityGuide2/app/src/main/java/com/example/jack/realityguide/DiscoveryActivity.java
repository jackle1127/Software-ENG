package com.example.jack.realityguide;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.graphics.PixelFormat;
import android.hardware.SensorManager;
import android.location.LocationManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.opengl.GLSurfaceView;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.SeekBar;
import android.widget.TextView;

import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;

import java.util.Timer;
import java.util.TimerTask;

public class DiscoveryActivity extends AppCompatActivity {
    final Context currentContext = this;
    GLSurfaceView glSurfaceView;
    OpenGLRenderer openGLRenderer;
    CameraPreview cameraPreview;
    SensorsController sensorsController;
    long calibrationStartTime = 0;
    boolean gyroMode = true;
    Timer timer = new Timer();
    int lblMoreAnimationOffset = 0;
    GoogleMap googleMap;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        loadSettings();
        Settings.display = getWindowManager().getDefaultDisplay();
        Settings.sensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);
        Settings.locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);
        sensorsController = new SensorsController(calibrationRunnable,
                rotateRunnable, locationChange);

        setContentView(R.layout.activity_discovery);
        openGLRenderer = new OpenGLRenderer(this);
        glSurfaceView = new GLSurfaceView(this);
        glSurfaceView.setEGLConfigChooser(8, 8, 8, 8, 16, 0);
        glSurfaceView.getHolder().setFormat(PixelFormat.RGBA_8888);
        glSurfaceView.setRenderer(openGLRenderer);
        cameraPreview = new CameraPreview(this);
        addContentView(cameraPreview, new FrameLayout.LayoutParams(FrameLayout.LayoutParams.WRAP_CONTENT,
                FrameLayout.LayoutParams.WRAP_CONTENT));
        addContentView(glSurfaceView, new FrameLayout.LayoutParams(FrameLayout.LayoutParams.WRAP_CONTENT,
                FrameLayout.LayoutParams.WRAP_CONTENT));
        ((TextView)findViewById(R.id.lblMore)).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(currentContext, MenuActivity.class));
            }
        });
        findViewById(R.id.mainLayout).bringToFront();
        timer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        TextView lblMore = (TextView) findViewById(R.id.lblMore);
                        lblMore.setText(MathFunctions.expColor(lblMore.getText().toString(),
                                lblMoreAnimationOffset, 1.2, 0x444444, 0xFFFFFF));
                        lblMoreAnimationOffset++;
                        if (lblMoreAnimationOffset >= lblMore.getText().length()) {
                            lblMoreAnimationOffset = -lblMore.getText().length();
                        }
                    }
                });
            }
        }, 0, 120);
        setUpMap();
        locationChange.run();
        ((SeekBar) findViewById(R.id.sbrZoom)).setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                Settings.mapZoom = 19 - (float) (progress / 4);
                locationChange.run();
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });
        ((SeekBar) findViewById(R.id.sbrZoom)).setProgress(19 - (int) Settings.mapZoom);
    }

    public Runnable rotateRunnable = new Runnable() {
        @Override
        public void run() {
            openGLRenderer.x = sensorsController.angularRotation[0];
            openGLRenderer.y = sensorsController.angularRotation[1];
            openGLRenderer.z = sensorsController.angularRotation[2];
            openGLRenderer.rotate();
        }
    };
    public Runnable calibrationRunnable = new Runnable() {
        @Override
        public void run() {
            long currentTime = System.currentTimeMillis();
            if (currentTime - calibrationStartTime > 700 || !gyroMode) {
                openGLRenderer.calibrate(sensorsController.matrix);
                resizeCameraView();
            }
        }
    };
    public Runnable locationChange = new Runnable() {
        @Override
        public void run() {
            if (googleMap != null) {
                googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(
                        new LatLng(Settings.currentLat, Settings.currentLon), Settings.mapZoom));
                googleMap.getUiSettings().setAllGesturesEnabled(false);
                googleMap.getUiSettings().setIndoorLevelPickerEnabled(false);
            } else {
                setUpMap();
            }
        }
    };

    private void resizeCameraView() {
        if (Settings.camWidth > 0 && Settings.camHeight > 0) {
            int width, height;
            int orientation = Settings.display.getOrientation();
            if (orientation == 0 || orientation == 2) {
                height = Settings.display.getHeight();
                width = Settings.display.getHeight() * Settings.camHeight / Settings.camWidth;
            } else {
                width = Settings.display.getWidth();
                height = Settings.display.getWidth()
                        * Settings.camHeight / Settings.camWidth;
            }
            FrameLayout.LayoutParams layout = new FrameLayout.LayoutParams(width, height);
            layout.leftMargin = Settings.display.getWidth() / 2 - width / 2;
            layout.topMargin = Settings.display.getHeight() / 2 - height / 2;
            cameraPreview.setLayoutParams(layout);
            int rot = Settings.display.getRotation();
            if (Settings.screenOrientation != rot) {
                Settings.screenOrientation = rot;
                cameraPreview.changeCameraConfig();
            }
        }
    }

    public void loadSettings() {
        SharedPreferences preferences =
                getSharedPreferences(Settings.PREFERENCES_NAME, MODE_PRIVATE);
        String settingSaved = preferences.getString("settingSaved", null);
        if (settingSaved != null) {
            Settings.cameraQuality = preferences.getInt("cameraQuality", 0);
            Settings.gyroMode = preferences.getBoolean("gyroMode", true);
        }
    }

    public void setUpMap() {
        if (googleMap == null) {
            googleMap = ((SupportMapFragment) getSupportFragmentManager().
                    findFragmentById(R.id.googleMaps)).getMap();
            googleMap.setBuildingsEnabled(false);
            googleMap.setTrafficEnabled(true);
        }
    }

    public void onResume() {
        super.onResume();
        if (cameraPreview.ready == true) {
            cameraPreview.createCamera();
        }
        cameraPreview.changeCameraConfig();
        cameraPreview.startTheCamera();
        sensorsController.registerSensors();
    }

    public void onDestroy() {
        cameraPreview.releaseCamera();
        sensorsController.unregisterSensors();
        super.onDestroy();
    }
}
