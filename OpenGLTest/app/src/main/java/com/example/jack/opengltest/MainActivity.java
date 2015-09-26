package com.example.jack.opengltest;

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
import android.opengl.GLSurfaceView;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Display;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.AbsoluteLayout;
import android.widget.FrameLayout.LayoutParams;
import android.widget.FrameLayout;

public class MainActivity extends AppCompatActivity {
    float lastX = 0, lastY = 0;
    long gyroStartTime;
    long calibrationStartTime = 0;
    SensorManager sensorManager;
    Sensor gyroSensor;
    Sensor magneticSensor;
    Sensor gravitySensor;
    OpenGLRenderer theRenderer;
    float dx, dy, dz;
    float[] anchorMatrix = new float[16];
    float[] anchorVectorX = new float[3];
    float[] anchorVectorY = new float[3];
    float[] anchorVectorZ = new float[3];
    float[] magneticVector = new float[3];
    float[] gravityVector = new float[3];
    WindowManager windowManager;
    Display display;
    Bitmap groundImg;
    CameraPreview cameraPreview;
    GLSurfaceView glView;
    FrameLayout mainLayout;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        glView = new GLSurfaceView(this);
        BitmapFactory.Options opt = new BitmapFactory.Options();
        opt.inPreferredConfig = Bitmap.Config.ARGB_8888;
        groundImg = dimBitmap(BitmapFactory.decodeResource(this.getResources(), R.drawable.map, opt), 180);
        theRenderer = new OpenGLRenderer(this, groundImg);
        glView.setEGLConfigChooser(8, 8, 8, 8, 16, 0);
        glView.getHolder().setFormat(PixelFormat.RGBA_8888);
        glView.setRenderer(theRenderer);
        setContentView(glView);

        sensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);
        gyroSensor = sensorManager.getDefaultSensor(Sensor.TYPE_GYROSCOPE);
        magneticSensor = sensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD);
        gravitySensor = sensorManager.getDefaultSensor(Sensor.TYPE_GRAVITY);

//        view.setOnTouchListener(onTouchListener);
        windowManager = (WindowManager) getSystemService(WINDOW_SERVICE);
        display = windowManager.getDefaultDisplay();
        cameraPreview = new CameraPreview(this, display);

        mainLayout = new FrameLayout(this);
        addContentView(cameraPreview, new LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT));
        theRenderer.orientation = display.getOrientation();
    }
//
//    View.OnTouchListener onTouchListener = new View.OnTouchListener() {
//        @Override
//        public boolean onTouch(View view, MotionEvent motionEvent) {
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
//            return true;
//        }
//    };

    private Bitmap dimBitmap (Bitmap src, int alpha) {
        Bitmap result = src.copy(Bitmap.Config.ARGB_8888, true);
        Canvas canvas = new Canvas(result);
        canvas.drawColor(Color.argb(alpha, 0, 0, 0), PorterDuff.Mode.DST_IN);
        return result.copy(Bitmap.Config.ARGB_8888, false);
    }
    public void onResume() {
        super.onResume();
        if (cameraPreview.ready == true) {
            cameraPreview.createCamera();
        }
        cameraPreview.startTheCamera();
        sensorManager.registerListener(gyroListener, gyroSensor, SensorManager.SENSOR_DELAY_FASTEST);
        sensorManager.registerListener(magneticListener, magneticSensor, SensorManager.SENSOR_DELAY_FASTEST);
        sensorManager.registerListener(gravityListener, gravitySensor, SensorManager.SENSOR_DELAY_FASTEST);
    }
    public void onPause() {
        super.onPause();
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
    public SensorEventListener magneticListener = new SensorEventListener() {
        @Override
        public void onSensorChanged(SensorEvent sensorEvent) {
            if (vectorLength(sensorEvent.values) > 40 && vectorLength(sensorEvent.values) < 50) {
                for (int i = 0; i < magneticVector.length; i++) {
                    magneticVector[i] = sensorEvent.values[i];
                }
                calibrate();
            }
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
        if (currentTime - calibrationStartTime > 700) {
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
            theRenderer.orientation = display.getOrientation();
            theRenderer.calibrate(anchorMatrix);
            resizeGLView();
        }
    }

    private void resizeGLView() {
        if (cameraPreview.camWidth > 0 && cameraPreview.camHeight > 0) {
            int width, height;
            int orientation = display.getOrientation();
            if (orientation == 0 || orientation == 2) {
                height = cameraPreview.getHeight();
                width = cameraPreview.getHeight() * cameraPreview.camHeight / cameraPreview.camWidth;
//                System.out.println("weird");
            } else {
                width = cameraPreview.getWidth();
                height = cameraPreview.getWidth()
                        * cameraPreview.camHeight / cameraPreview.camWidth;
            }
//            System.out.println(cameraPreview.camWidth + ", " + cameraPreview.camHeight);
//            System.out.println(cameraPreview.getWidth() + ", " + cameraPreview.getHeight());
//            System.out.println(mainLayout.getLeft() + ", " + mainLayout.getTop() + ", " + orientation);
            cameraPreview.setLayoutParams(new LayoutParams(width, height));
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
    @Override
    public void onConfigurationChanged(Configuration config) {
        super.onConfigurationChanged(config);
        theRenderer.orientation = display.getOrientation();
        theRenderer.calibrate(anchorMatrix);
        resizeGLView();

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
