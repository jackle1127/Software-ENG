package com.augmentedcoders.realityguide;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Picture;
import android.graphics.PixelFormat;
import android.graphics.Point;
import android.graphics.PorterDuff;
import android.hardware.SensorManager;
import android.location.LocationManager;
import android.provider.ContactsContract;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.opengl.GLSurfaceView;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;

import com.augmentedcoders.realityguide.R;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.InputStream;
import java.net.URL;
import java.util.Timer;
import java.util.TimerTask;

public class ActivityDiscovery extends AppCompatActivity {
    final Context currentContext = this;
    GLSurfaceView glSurfaceView;
    OpenGLRenderer openGLRenderer;
    OpenGLTouchDetector openGLTouchDetector;
    PixelBuffer pixelBufferTouchDetector;
    CameraPreview cameraPreview;
    SensorsController sensorsController;
    long calibrationStartTime = 0;
    Timer timer = new Timer();
    int lblMoreAnimationOffset = 0;
    GoogleMap googleMap;
    boolean nothingTapped = true;
    Timer databaseQueryTimer;

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
        openGLTouchDetector = new OpenGLTouchDetector(this);
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
                nothingTapped = false;
                startActivity(new Intent(currentContext, ActivityMenu.class));
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
        final ImageView testImg = new ImageView(this);
        addContentView(testImg, new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT
                , ViewGroup.LayoutParams.MATCH_PARENT));
        pixelBufferTouchDetector = new PixelBuffer();
        pixelBufferTouchDetector.setRenderer(openGLTouchDetector);
        glSurfaceView.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                int x = (int) event.getX();
                int y = (int) event.getY();
                int id = pixelBufferTouchDetector.getColor(x, y);
                if (id > 0) {
                    for (int i = 0; i < Settings.pointOfInterestPosts.size(); i++) {
                        if (Settings.pointOfInterestPosts.get(i).getId() == id) {
                            Settings.selectedPost = Settings.pointOfInterestPosts.get(i);
                            nothingTapped = false;
                            startActivity(new Intent(currentContext, ActivityPlacesDetails.class));
                            break;
                        }
                    }
                    for (int i = 0; i < Settings.communityPosts.size(); i++) {
                        if (Settings.communityPosts.get(i).getId() == id) {
                            Settings.selectedPost = Settings.communityPosts.get(i);
                            nothingTapped = false;
                            Settings.goBackToAccount = false;
                            startActivity(new Intent(currentContext, ActivityCommunityPost.class));
                            break;
                        }
                    }
                } else {
                    Settings.selectedPost = null;
                }
                return false;
            }
        });
        databaseQueryTimer = new Timer();
        databaseQueryTimer.schedule(new TimerTask() {
            @Override
            public void run() {
                queryDatabase();
            }
        }, 10000, 10000);
        Settings.communityPosts.clear();
    }

    public Runnable rotateRunnable = new Runnable() {
        @Override
        public void run() {
            openGLRenderer.rX = sensorsController.angularRotation[0];
            openGLRenderer.rY = sensorsController.angularRotation[1];
            openGLRenderer.rZ = sensorsController.angularRotation[2];
            openGLRenderer.rotate();
            rotateCompass();
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
            rotateCompass();
        }
    };

    private void rotateCompass() {
        float x = Settings.anchorMatrix[0];
        float y = Settings.anchorMatrix[2];
        if (x * x + y * y > 0) {
            double angle = Math.atan2(y, x) + Math.PI / 2;
            Point center = new Point(Settings.mapOverlay.getWidth() / 2,
                    Settings.mapOverlay.getHeight() / 2);
            double wideAngle = .7;
            Point v1 = new Point(center.x + (int) (Math.cos(angle - wideAngle) * 1000)
                    , center.y - (int) (Math.sin(angle - wideAngle) * 1000));
            Point v2 = new Point(center.x + (int) (Math.cos(angle + wideAngle) * 1000)
                    , center.y - (int) (Math.sin(angle + wideAngle) * 1000));
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
    public Runnable locationChange = new Runnable() {
        @Override
        public void run() {
            if (googleMap != null){
                if (Math.abs(Settings.prevLat - Settings.currentLat) +
                        Math.abs(Settings.prevLon - Settings.currentLon) > .0001) {
                    queryBoth();
                }
                googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(
                        new LatLng(Settings.currentLat, Settings.currentLon), Settings.mapZoom));
                googleMap.getUiSettings().setAllGesturesEnabled(false);
                googleMap.getUiSettings().setIndoorLevelPickerEnabled(false);
                for (int i = 0; i < Settings.pointOfInterestPosts.size(); i++) {
                    Settings.pointOfInterestPosts.get(i).refreshLocation();
                }
                for (int i = 0; i < Settings.communityPosts.size(); i++) {
                    Settings.communityPosts.get(i).refreshLocation();
                }
            } else {
                setUpMap();
            }
        }
    };

    private void queryBoth() {
        queryGooglePlaces();
        queryDatabase();
    }
    private void queryGooglePlaces() {
        if (Settings.placesReady) {
            Settings.placesReady = false;
            Settings.prevLat = Settings.currentLat;
            Settings.prevLon = Settings.currentLon;
            GooglePlacesQuery googlePlacesQuery = new GooglePlacesQuery();
            googlePlacesQuery.execute(refreshMap);
        }
    }

    private void queryDatabase() {
        if (Settings.postReady) {
            Settings.postReady = false;
            Thread separateThread = new Thread(new Runnable() {
                @Override
                public void run() {
                    String queriedPosts = DatabaseMethods.getRadius(0.0372823, Settings.currentLat,
                            Settings.currentLon);
                    try {
                        if (queriedPosts != null && !queriedPosts.equals("null ")) {
                            JSONArray postArray = new JSONArray(queriedPosts);
                            for (int i = 0; i < postArray.length(); i++) {
                                try {
                                    String id = postArray.getJSONObject(i).getString("id");
                                    boolean exist = false;
                                    for (int j = 0; j < Settings.communityPosts.size(); j++) {
                                        if (Settings.communityPosts.get(j).jsonContent.getString("id")
                                                .equals(id)) {
                                            exist = true;
                                            break;
                                        }
                                    }
                                    if (!exist) {
                                        String username = postArray.getJSONObject(i).getString("user");
                                        System.out.println("Getting likes for post " + id);
                                        String queriedLikes = DatabaseMethods.getLIKES(id);
                                        System.out.println("Getting user");
                                        String user = DatabaseMethods.getUser(username);
                                        JSONArray likes = new JSONArray();
                                        if (!queriedLikes.equals("null ")) {
                                            likes = new JSONArray(queriedLikes);
                                        }
                                        String pictureURL = "";
                                        if (!user.equals("null ")) {
                                            pictureURL = new JSONArray(user).getJSONObject(0).getString("photo");
                                            pictureURL = "http://45.55.44.240/userPics/" + pictureURL + ".jpg";
                                        }
                                        CommunityPost newPost = new CommunityPost(postArray.getJSONObject(i),
                                                likes, pictureURL, null);
                                        Settings.communityPosts.add(newPost);
                                    }
                                } catch (Exception e) {
                                    e.printStackTrace();
                                }
                            }
                            if (postArray.length() != Settings.communityPosts.size()) {
                                for (int i = 0; i < Settings.communityPosts.size(); i++) {
                                    boolean exist = false;
                                    for (int j = 0; j < postArray.length(); j++) {
                                        String currentId = postArray.getJSONObject(j).getString("id");
                                        if (Settings.communityPosts.get(i).jsonContent.getString("id")
                                                .equals(currentId)) {
                                            exist = true;
                                            break;
                                        }
                                    }
                                    if (!exist) {
                                        Settings.communityPosts.remove(i);
                                    }
                                }
                            }
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    Settings.postReady = true;
                    refreshMap.run();
                }
            });
            separateThread.start();
        }
    }

    public Runnable refreshMap = new Runnable() {
        @Override
        public void run() {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    googleMap.clear();
                    for (PointOfInterestPost newDot: Settings.pointOfInterestPosts) {
                        MarkerOptions options = new MarkerOptions();
                        options.position(newDot.getLatLng());
                        options.icon(BitmapDescriptorFactory.fromResource(R.drawable.poimarker));
                        googleMap.addMarker(options.flat(true));
                    }
                    for (CommunityPost newDot: Settings.communityPosts) {
                        MarkerOptions options = new MarkerOptions();
                        options.position(newDot.getLatLng());
                        options.icon(BitmapDescriptorFactory.fromResource(R.drawable.postmarker));
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
        queryBoth();
    }

    public void onDestroy() {
        releaseStuff();
        super.onDestroy();
    }

    public void onPause() {
        releaseStuff();
        if (nothingTapped) {
            startActivity(new Intent(this, ActivityMenu.class));
        }
        super.onPause();
    }

    public void onStop() {
        releaseStuff();
        super.onStop();
    }

    private void releaseStuff() {
        cameraPreview.releaseCamera();
        sensorsController.unregisterSensors();
        databaseQueryTimer.cancel();
    }

    @Override
    public void onBackPressed() {
    }

}
