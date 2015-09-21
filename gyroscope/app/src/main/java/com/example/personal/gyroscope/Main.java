package com.example.personal.gyroscope;

import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.app.Activity;
import android.content.Context;
import android.view.View;
import android.widget.TextView;
import android.graphics.Matrix;
import android.widget.ImageView;

public class Main extends Activity {
    TextView textX, textY, textZ;
    SensorManager sensorManager;
    Sensor sensor;
    long previousTime = 0;
    double oriZ = 0;
    ImageView imageView;
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        imageView = (ImageView) findViewById(R.id.imageView);
        sensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        sensor = sensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD);

        textX = (TextView) findViewById(R.id.textX);
        textY = (TextView) findViewById(R.id.textY);
        textZ = (TextView) findViewById(R.id.textZ);
        imageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                oriZ = 0;
            }
        });
    }

    public void onResume() {
        super.onResume();
        sensorManager.registerListener(gyroListener, sensor,
                SensorManager.SENSOR_DELAY_NORMAL);
    }

    public void onStop() {
        super.onStop();
        sensorManager.unregisterListener(gyroListener);
    }

    public SensorEventListener gyroListener = new SensorEventListener() {
        public void onAccuracyChanged(Sensor sensor, int acc) { }

        public void onSensorChanged(SensorEvent event) {
            float x = event.values[0];
            float y = event.values[1];
            float z = event.values[2];

            textX.setText("X : " + (int)x);
            textY.setText("Y : " + (int)y);
            textZ.setText("Z : " + (int)z);
            long currentTime = System.currentTimeMillis();
            float deltaTime = currentTime - previousTime;
            if (previousTime == 0) deltaTime = 0;
            deltaTime /= 1000;
            oriZ = Math.atan2(y, -x) * 180 / Math.PI - 90;
//            oriZ = -x;
            Matrix matrix = new Matrix();
            imageView.setScaleType(ImageView.ScaleType.MATRIX);   //required
            matrix.setRotate((float) oriZ, imageView.getWidth()/2, imageView.getHeight()/2);
            imageView.setImageMatrix(matrix);
//            previousTime = currentTime;
        }
    };
}