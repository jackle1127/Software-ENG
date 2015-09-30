package com.example.jack.opengltest;
import android.content.Intent;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.PixelFormat;
import android.graphics.PorterDuff;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.opengl.GLSurfaceView;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.Layout;
import android.view.Display;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.FrameLayout.LayoutParams;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;

public class MainActivity extends AppCompatActivity {
    int ZOOM_LEVEL = 20;
    float meterPerPixel = 1.0f;
    float lastX = 0, lastY = 0;
    long gyroStartTime;
    long calibrationStartTime = 0;
    SensorManager sensorManager;
    LocationManager locationManager;
    Sensor gyroSensor;
    Sensor magneticSensor;
    Sensor gravitySensor;
    Sensor tempSensor;
    OpenGLRenderer theRenderer;
    boolean gyroMode = true;
    float dx, dy, dz;
    float[] anchorMatrix = new float[16];
    float[] anchorVectorX = new float[3];
    float[] anchorVectorY = new float[3];
    float[] anchorVectorZ = new float[3];
    float[] magneticVector = new float[3];
    float[] gravityVector = new float[3];
    float[] tempVector = new float[3];
    double prevLat = 0;
    double prevLng = 0;
    WindowManager windowManager;
    Display display;
    Bitmap groundImg;
    CameraPreview cameraPreview;
    GLSurfaceView glView;
    TextView textView;
    ImageView mapImage;
    Intent settingIntent;
    boolean listenForSetting = false;
    GoogleMap googleMap;
    static String log = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        initializeMap();
        glView = new GLSurfaceView(this);
        BitmapFactory.Options opt = new BitmapFactory.Options();
        opt.inPreferredConfig = Bitmap.Config.ARGB_8888;
        groundImg = dimBitmap(BitmapFactory.decodeResource(this.getResources(), R.drawable.map, opt), 180);
        theRenderer = new OpenGLRenderer(this, groundImg);
        glView.setEGLConfigChooser(8, 8, 8, 8, 16, 0);
        glView.getHolder().setFormat(PixelFormat.RGBA_8888);
        glView.setRenderer(theRenderer);
        addContentView(glView, new LayoutParams(LayoutParams.MATCH_PARENT,
                LayoutParams.MATCH_PARENT));

        locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);
        sensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);
        gyroSensor = sensorManager.getDefaultSensor(Sensor.TYPE_GYROSCOPE);
        magneticSensor = sensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD);
        gravitySensor = sensorManager.getDefaultSensor(Sensor.TYPE_GRAVITY);

        tempSensor = sensorManager.getDefaultSensor(Sensor.TYPE_PRESSURE);


        glView.setOnTouchListener(onTouchListener);
        windowManager = (WindowManager) getSystemService(WINDOW_SERVICE);
        display = windowManager.getDefaultDisplay();
        cameraPreview = new CameraPreview(this, display);

        addContentView(cameraPreview, new LayoutParams(LayoutParams.WRAP_CONTENT,
                LayoutParams.WRAP_CONTENT));
        theRenderer.orientation = display.getOrientation();
        theRenderer.gyroMode = gyroMode;

        textView = new TextView(this);
        textView.setText("helloes");
        textView.setTextColor(0x99FFFF00);

        Button settingButton = new Button(this);
        settingButton.setText("Settings");
        settingButton.setOnClickListener(settingClicked);
        LinearLayout theLayout = new LinearLayout(this);
        theLayout.addView(settingButton);
        theLayout.addView(textView);
        theLayout.setOrientation(LinearLayout.VERTICAL);
        addContentView(theLayout, new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.WRAP_CONTENT,
                RelativeLayout.LayoutParams.WRAP_CONTENT));
        settingIntent = new Intent(this, Setting.class);
        mapImage = new ImageView(this);

        RelativeLayout rl = new RelativeLayout(this);
        RelativeLayout.LayoutParams rlp = new RelativeLayout.LayoutParams(
                200, 200);
        rlp.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM);
        rlp.addRule(RelativeLayout.ALIGN_PARENT_RIGHT);
        mapImage.setMaxWidth(100);
        mapImage.setMaxHeight(100);
        rl.addView(mapImage, rlp);
        addContentView(rl, new RelativeLayout.LayoutParams(
                RelativeLayout.LayoutParams.MATCH_PARENT, RelativeLayout.LayoutParams.MATCH_PARENT));


    }

    View.OnClickListener settingClicked = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            listenForSetting = true;
            startActivity(settingIntent);
        }
    };


    View.OnTouchListener onTouchListener = new View.OnTouchListener() {
        @Override
        public boolean onTouch(View view, MotionEvent motionEvent) {
//            switch (motionEvent.getAction()) {
//                case MotionEvent.ACTION_DOWN:
//                    lastX = motionEvent.getX();
//                    lastY = motionEvent.getY();
//                    break;
//                case MotionEvent.ACTION_MOVE:
//                    float currentX = motionEvent.getX();
//                    float currentY = motionEvent.getY();
//                    dy = -(currentX - lastX) / 4;
//                    dx = -(currentY - lastY) / 4;
//                    dz = 0;
//                    lastX = currentX;
//                    lastY = currentY;
//                    glRotate();
//            }
            updateMap();
            return true;
        }
    };

    LocationListener theLocationListener = new LocationListener() {
        @Override
        public void onLocationChanged(Location location) {
            if (prevLat != location.getLatitude() || prevLng != location.getLongitude()) {
                prevLat = location.getLatitude();
                prevLng = location.getLongitude();
                updateMap();
            }
        }

        @Override
        public void onStatusChanged(String s, int i, Bundle bundle) {

        }

        @Override
        public void onProviderEnabled(String s) {

        }

        @Override
        public void onProviderDisabled(String s) {

        }
    };
    private Bitmap dimBitmap (Bitmap src, int alpha) {
        Bitmap result = src.copy(Bitmap.Config.ARGB_8888, true);
        Canvas canvas = new Canvas(result);
        canvas.drawColor(Color.argb(alpha, 0, 0, 0), PorterDuff.Mode.DST_IN);
        return result;
    }
    public void onResume() throws SecurityException{
        super.onResume();
        sensorManager.registerListener(magneticListener, magneticSensor, SensorManager.SENSOR_DELAY_GAME);
        sensorManager.registerListener(gravityListener, gravitySensor, SensorManager.SENSOR_DELAY_GAME);
        sensorManager.registerListener(gyroListener, gyroSensor, SensorManager.SENSOR_DELAY_FASTEST);
        sensorManager.registerListener(tempListener, tempSensor, SensorManager.SENSOR_DELAY_FASTEST);
        locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, theLocationListener);
        if (cameraPreview.ready == true) {
            cameraPreview.createCamera();
        }
        cameraPreview.startTheCamera();
        gyroMode = Setting.useGyro;
        if (listenForSetting) {
            if (cameraPreview.cameraQuality != Setting.cameraQuality) {
                cameraPreview.cameraQuality = Setting.cameraQuality;
                cameraPreview.changeCameraConfig();
            }
            listenForSetting = false;
        }
    }
    public void onPause() {
        super.onPause();
        cameraPreview.releaseCamera();
        sensorManager.unregisterListener(gyroListener);
        sensorManager.unregisterListener(magneticListener);
        sensorManager.unregisterListener(gravityListener);

    }
    public void onDestroy() {
        super.onDestroy();
        cameraPreview.releaseCamera();
        sensorManager.unregisterListener(gyroListener);
        sensorManager.unregisterListener(magneticListener);
        sensorManager.unregisterListener(gravityListener);
    }
    public void glRotate() {
        theRenderer.x = dx;
        theRenderer.y = dy;
        theRenderer.z = dz;
        theRenderer.rotate();
    }
    public SensorEventListener gyroListener = new SensorEventListener() {
        @Override
        public void onSensorChanged(SensorEvent sensorEvent) {
            long currentTime = System.currentTimeMillis();
            if (gyroStartTime != 0) {
                float deltaTime = (float)(currentTime - gyroStartTime) / 1000;
                float valueX = sensorEvent.values[0];
                float valueY = sensorEvent.values[1];
                float valueZ = sensorEvent.values[2];
                dx = -(float) (valueX * 180 / Math.PI) * deltaTime;
                dy = -(float) (valueY * 180 / Math.PI) * deltaTime;
                dz = -(float) (valueZ * 180 / Math.PI) * deltaTime;
            }
            glRotate();
            gyroStartTime = currentTime;
        }

        @Override
        public void onAccuracyChanged(Sensor sensor, int i) {

        }
    };
    public SensorEventListener tempListener = new SensorEventListener() {
        @Override
        public void onSensorChanged(SensorEvent sensorEvent) {
            for (int i = 0; i < tempVector.length; i++) {
                tempVector[i] = sensorEvent.values[i];
            }
            calibrate();
        }

        @Override
        public void onAccuracyChanged(Sensor sensor, int i) {

        }
    };
    public SensorEventListener magneticListener = new SensorEventListener() {
        @Override
        public void onSensorChanged(SensorEvent sensorEvent) {
            for (int i = 0; i < magneticVector.length; i++) {
                magneticVector[i] = sensorEvent.values[i];
            }
            calibrate();
        }

        @Override
        public void onAccuracyChanged(Sensor sensor, int i) {

        }
    };
    public SensorEventListener gravityListener = new SensorEventListener() {
        @Override
        public void onSensorChanged(SensorEvent sensorEvent) {
            for (int i = 0; i < gravityVector.length; i++) {
                gravityVector[i] = -sensorEvent.values[i];
            }
            calibrate();
        }

        @Override
        public void onAccuracyChanged(Sensor sensor, int i) {

        }
    };

    private void calibrate() {
        long currentTime = System.currentTimeMillis();
        theRenderer.gyroMode = gyroMode;
        if (currentTime - calibrationStartTime > 700 || !gyroMode) {
            calibrationStartTime = currentTime;
            anchorVectorX[0] = 1;
            anchorVectorY[1] = 1;
            anchorVectorZ[2] = 1;
            anchorVectorY = normalize(flipVector(gravityVector));
            anchorVectorX = normalize(crossProduct(gravityVector, magneticVector));
            anchorVectorZ = normalize(crossProduct(gravityVector,
                    crossProduct(gravityVector, magneticVector)));
            for (int i = 0; i < anchorVectorX.length; i++) {
                anchorMatrix[i] = anchorVectorX[i];
            }
            for (int i = 0; i < anchorVectorY.length; i++) {
                anchorMatrix[i + 4] = anchorVectorY[i];
            }
            for (int i = 0; i < anchorVectorZ.length; i++) {
                anchorMatrix[i + 8] = anchorVectorZ[i];
            }
            anchorMatrix[15] = 1;
            theRenderer.angleOfView = cameraPreview.angleOfView;
            theRenderer.camWidth = cameraPreview.camWidth;
            theRenderer.camHeight = cameraPreview.camHeight;
            theRenderer.orientation = display.getOrientation();
            theRenderer.calibrate(anchorMatrix);
            resizeCameraView();
        }
    }

    private void resizeCameraView() {
        if (cameraPreview.camWidth > 0 && cameraPreview.camHeight > 0) {
            int width, height;
            int orientation = display.getOrientation();
            if (orientation == 0 || orientation == 2) {
                height = display.getHeight();
                width = display.getHeight() * cameraPreview.camHeight / cameraPreview.camWidth;
            } else {
                width = display.getWidth();
                height = display.getWidth()
                        * cameraPreview.camHeight / cameraPreview.camWidth;
            }
            LayoutParams layout = new LayoutParams(width, height);
            layout.leftMargin = display.getWidth() / 2 - width / 2;
            layout.topMargin = display.getHeight() / 2 - height / 2;
            cameraPreview.setLayoutParams(layout);
            textView.setText(gravityVector[0] + ", " + gravityVector[1] + ", " + gravityVector[2]
                    + "\n" + magneticVector[0] + ", " + magneticVector[1] + ", " + magneticVector[2]
                    + "\n" + tempVector[0] + ", " + tempVector[1] + ", " + tempVector[2]
                    + "\n" + anchorVectorX[0] + ", " + anchorVectorX[1] + ", " + anchorVectorX[2]
                    + "\n" + anchorVectorY[0] + ", " + anchorVectorY[1] + ", " + anchorVectorY[2]
                    + "\n" + anchorVectorZ[0] + ", " + anchorVectorZ[1] + ", " + anchorVectorZ[2]
                    + "\n" + cameraPreview.getWidth() + ", " + cameraPreview.getHeight()
                    + "\n" + cameraPreview.getLeft() + ", " + cameraPreview.getTop()
                    + "\n" + display.getWidth() + ", " + display.getHeight()
                    + "\n" + prevLng + ", " + prevLat
                    + "\n" + meterPerPixel + ", " + theRenderer.mapScale
                    + "\n" + cameraPreview.angleOfView + ", " + theRenderer.angleOfView
                    + ", " + theRenderer.currentFov
                    + log
                    + "\npotato!!");
        }
    }
    private float[] flipVector(float[] a) {
        float[] result = new float[3];
        result[0] = -a[0];
        result[1] = -a[1];
        result[2] = -a[2];
        return result;
    }
    private float[] crossProduct(float[] a, float[] b) {
        float[] result = new float[3];
        result[0] = a[1] * b[2] - a[2] * b[1];
        result[1] = a[2] * b[0] - a[0] * b[2];
        result[2] = a[0] * b[1] - a[1] * b[0];
        return result;
    }
    private float[] normalize(float[] vector) {
        float[] result = new float[3];
        for (int i = 0; i < vector.length; i++) {
            result[i] = vector[i] / vectorLength(vector);
        }
        return result;
    }
    private float vectorLength(float[] vector) {
        return (float) Math.sqrt(
                vector[0] * vector[0] +
                        vector[1] * vector[1] +
                        vector[2] * vector[2]
        );
    }
    private void initializeMap() {
        googleMap = ((SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map))
                .getMap();
        if (googleMap != null) {
            googleMap.setOnMapClickListener(null);
            googleMap.setOnMapLongClickListener(null);
            googleMap.setBuildingsEnabled(false);
            googleMap.setTrafficEnabled(true);
        }
    }
    private void updateMap() {
        if (googleMap != null) {
            meterPerPixel = (float) ((Math.cos(prevLat * Math.PI/180) * 2 * Math.PI * 6378137)
                    / (256 * Math.pow(2, ZOOM_LEVEL)));
            theRenderer.meterPerPixel = meterPerPixel;
            theRenderer.mapScale = theRenderer.MAP_SIZE * theRenderer.meterPerPixel;
            LatLng latLng = new LatLng(prevLat, prevLng);
            googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng, ZOOM_LEVEL));
            googleMap.snapshot(new GoogleMap.SnapshotReadyCallback() {
                    @Override
                public void onSnapshotReady(Bitmap bitmap) {
                    try {
                        if (bitmap.getWidth() > 0) {
                            Bitmap newMap = dimBitmap(bitmap, 180);
                            mapImage.setImageBitmap(newMap);
                            theRenderer.updateGround(newMap);
                        }
                    } catch (Exception e) {
                        log += "\n" + e.getMessage();
                    }
                }
            });
        }
    }
    @Override
    public void onConfigurationChanged(Configuration config) {
        super.onConfigurationChanged(config);
        theRenderer.orientation = display.getOrientation();
        theRenderer.calibrate(anchorMatrix);
        resizeCameraView();
    }
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        return false;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.action_settings) {
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}