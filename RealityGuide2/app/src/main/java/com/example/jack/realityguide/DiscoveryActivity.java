package com.example.jack.realityguide;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PixelFormat;
import android.graphics.Point;
import android.graphics.PorterDuff;
import android.hardware.SensorManager;
import android.location.LocationManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.opengl.GLSurfaceView;
import android.view.Surface;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import java.util.Timer;
import java.util.TimerTask;

public class DiscoveryActivity extends AppCompatActivity {
    final Context currentContext = this;
    GLSurfaceView glSurfaceView;
    OpenGLRenderer openGLRenderer;
    CameraPreview cameraPreview;
    SensorsController sensorsController;
    long calibrationStartTime = 0;
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
        Settings.apiKey = getString(R.string.google_maps_key);
        Settings.serverKey = getString(R.string.google_server_key);
        Settings.resources = getResources();
        sensorsController = new SensorsController(calibrationRunnable,
                rotateRunnable, locationChange);

        setContentView(R.layout.activity_discovery);
        openGLRenderer = new OpenGLRenderer(this);
        glSurfaceView = new GLSurfaceView(this);
        glSurfaceView.setEGLConfigChooser(8, 8, 8, 8, 16, 0);
        glSurfaceView.getHolder().setFormat(PixelFormat.RGBA_8888);
        glSurfaceView.setRenderer(openGLRenderer);
        cameraPreview = new CameraPreview(this);
        addContentView(glSurfaceView, new FrameLayout.LayoutParams(FrameLayout.LayoutParams.WRAP_CONTENT,
                FrameLayout.LayoutParams.WRAP_CONTENT));
        addContentView(cameraPreview, new FrameLayout.LayoutParams(FrameLayout.LayoutParams.WRAP_CONTENT,
                FrameLayout.LayoutParams.WRAP_CONTENT));
        findViewById(R.id.lblMore).setOnClickListener(new View.OnClickListener() {
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
        Settings.mapOverlay = Bitmap.createBitmap(200, 200,
                Bitmap.Config.ARGB_8888);
        Settings.mapOverlayCanvas = new Canvas(Settings.mapOverlay);
        Settings.mapOverlayPaint = new Paint();
        Settings.mapOverlapPath = new Path();
        Settings.mapOverlapPath.setFillType(Path.FillType.EVEN_ODD);
        ((ImageView) findViewById(R.id.imgMapOverlay)).setImageBitmap(Settings.mapOverlay);
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
            if (currentTime - calibrationStartTime > 700 || !Settings.gyroMode) {
                openGLRenderer.calibrate(sensorsController.matrix);
                resizeCameraView();
                calibrationStartTime = currentTime;
            }

            // Runs whenever sensor updates to draw the V on the map
            float x = sensorsController.vectorZ[0];
            float y = sensorsController.vectorZ[1];
            if (Settings.display.getRotation() == Surface.ROTATION_270) x = -x;
            if (x * x + y * y > 0) {
                double angle = Math.atan2(y, x) + Math.PI / 2;
                Point center = new Point(Settings.mapOverlay.getWidth() / 2,
                        Settings.mapOverlay.getHeight() / 2);
                Point v1 = new Point(center.x - (int) (Math.cos(angle - .3) * 1000)
                        , center.y - (int) (Math.sin(angle - .3) * 1000));
                Point v2 = new Point(center.x - (int) (Math.cos(angle + .3) * 1000)
                        , center.y - (int) (Math.sin(angle + .3) * 1000));
                Settings.mapOverlayCanvas.drawColor(0, PorterDuff.Mode.CLEAR);
                Settings.mapOverlayPaint.setColor(0x992266FF);
                Settings.mapOverlayPaint.setStrokeWidth(3);
                Settings.mapOverlapPath.rewind();
                Settings.mapOverlapPath.moveTo(center.x, center.y);
                Settings.mapOverlapPath.lineTo(v1.x, v1.y);
                Settings.mapOverlapPath.lineTo(v2.x, v2.y);
                Settings.mapOverlapPath.close();
                Settings.mapOverlayCanvas.drawPath(Settings.mapOverlapPath,
                        Settings.mapOverlayPaint);
                ((ImageView) findViewById(R.id.imgMapOverlay)).setImageBitmap(Settings.mapOverlay);
            }
        }
    };
    public Runnable locationChange = new Runnable() {
        @Override
        public void run() {
            if (googleMap != null){
                if (Math.abs(Settings.prevLat - Settings.currentLat) +
                        Math.abs(Settings.prevLon - Settings.currentLon) > .0001) {
                    queryGooglePlaces();
                }
                googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(
                        new LatLng(Settings.currentLat, Settings.currentLon), Settings.mapZoom));
                googleMap.getUiSettings().setAllGesturesEnabled(false);
                googleMap.getUiSettings().setIndoorLevelPickerEnabled(false);
            } else {
                setUpMap();
            }
        }
    };

    private void queryGooglePlaces() {
        if (Settings.placesReady) {
            Settings.prevLat = Settings.currentLat;
            Settings.prevLon = Settings.currentLon;
            GooglePlacesQuery googlePlacesQuery = new GooglePlacesQuery();
            googlePlacesQuery.execute(createDots);
            Settings.placesReady = false;
        }
    }
    public Runnable createDots = new Runnable() {
        @Override
        public void run() {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    googleMap.clear();
                    for (Post newDot: Settings.latLngs) {
                        MarkerOptions options = new MarkerOptions();
                        options.position(newDot.getLatLng());
                        //options.icon(BitmapDescriptorFactory.fromResource(R.drawable.poimarker));
                        options.icon(BitmapDescriptorFactory.fromBitmap(newDot.getContent()));
                        googleMap.addMarker(options.flat(true));
                    }
                    googleMap.setOnMarkerClickListener(new GoogleMap.OnMarkerClickListener() {
                        @Override
                        public boolean onMarkerClick(Marker marker) {
                            return true;
                        }
                    });
                }
            });
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
            googleMap.setOnMarkerClickListener(null);
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
        queryGooglePlaces();
    }

    public void onDestroy() {
        releaseStuff();
        super.onDestroy();
    }

    public void onPause() {
        releaseStuff();
        super.onPause();
    }

    public void onStop() {
        releaseStuff();
        super.onStop();
    }

    private void releaseStuff() {
        cameraPreview.releaseCamera();
        sensorsController.unregisterSensors();
    }
}
