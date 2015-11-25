package com.example.jack.realityguide;

import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;

import java.lang.reflect.Method;
import java.util.LinkedList;

public class SensorsController {
    private int NUMBER_OF_GRAVITY_DATA = 15;
    private Sensor gyroSensor;
    private Sensor magneticSensor;
    private Sensor accelerometerSensor;
    private long gyroStartTime = 0;
    private Runnable calibrateRunnable;
    private Runnable rotatingRunnable;
    private Runnable locationChange;
    private LinkedList<Float>[] gravityAccumulativeList = new LinkedList[3];
    private float[] gravityAccumulation = new float[3];
    private float[] magneticVector = new float[3];
    private float[] gravityVector = new float[3];
    public float[] angularRotation = new float[3];
    public float[] vectorX = new float[3];
    public float[] vectorY = new float[3];
    public float[] vectorZ = new float[3];
    public float[] matrix = new float[16];

    SensorsController(Runnable cal, Runnable rot, Runnable loc) {
        for (int i = 0; i < gravityAccumulativeList.length; i++) {
            gravityAccumulativeList[i] = new LinkedList<>();
        }
        calibrateRunnable = cal;
        rotatingRunnable = rot;
        locationChange = loc;
        gyroSensor = Settings.sensorManager.getDefaultSensor(Sensor.TYPE_GYROSCOPE);
        magneticSensor = Settings.sensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD);
        accelerometerSensor = Settings.sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
    }

    public void registerSensors() throws SecurityException {
        Settings.sensorManager.registerListener(magneticListener, magneticSensor, SensorManager.SENSOR_DELAY_NORMAL);
        Settings.sensorManager.registerListener(gravityListener, accelerometerSensor, SensorManager.SENSOR_DELAY_NORMAL);
        Settings.sensorManager.registerListener(gyroListener, gyroSensor, SensorManager.SENSOR_DELAY_FASTEST);
        Settings.locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, locationListener);
    }
    public void unregisterSensors() throws SecurityException {
        Settings.sensorManager.unregisterListener(gyroListener);
        Settings.sensorManager.unregisterListener(magneticListener);
        Settings.sensorManager.unregisterListener(gravityListener);
        Settings.locationManager.removeUpdates(locationListener);
    }

    private void calibrate() {
        vectorY = MathFunctions.normalize(MathFunctions.flipVector(gravityVector));
        vectorX = MathFunctions.normalize(MathFunctions.crossProduct(gravityVector, magneticVector));
        vectorZ = MathFunctions.normalize(MathFunctions.crossProduct(gravityVector,
                MathFunctions.crossProduct(gravityVector, magneticVector)));
        for (int i = 0; i < vectorX.length; i++) {
            matrix[i] = vectorX[i];
        }
        for (int i = 0; i < vectorY.length; i++) {
            matrix[i + 4] = vectorY[i];
        }
        for (int i = 0; i < vectorZ.length; i++) {
            matrix[i + 8] = vectorZ[i];
        }
        matrix[15] = 1;
        if (gravityAccumulativeList[0].size() >= NUMBER_OF_GRAVITY_DATA) {
            calibrateRunnable.run();
        }
    }
    private LocationListener locationListener = new LocationListener() {
        @Override
        public void onLocationChanged(Location location) {
            Settings.currentLat = location.getLatitude();
            Settings.currentLon = location.getLongitude();
            locationChange.run();
        }

        @Override
        public void onStatusChanged(String provider, int status, Bundle extras) {

        }

        @Override
        public void onProviderEnabled(String provider) {

        }

        @Override
        public void onProviderDisabled(String provider) {

        }
    };
    private SensorEventListener gyroListener = new SensorEventListener() {
        @Override
        public void onSensorChanged(SensorEvent sensorEvent) {
            long currentTime = System.currentTimeMillis();
            if (gyroStartTime != 0) {
                float deltaTime = (float)(currentTime - gyroStartTime) / 1000;
                float valueX = sensorEvent.values[0];
                float valueY = sensorEvent.values[1];
                float valueZ = sensorEvent.values[2];
                angularRotation[0] = -(float) (valueX * 180 / Math.PI) * deltaTime;
                angularRotation[1] = -(float) (valueY * 180 / Math.PI) * deltaTime;
                angularRotation[2] = -(float) (valueZ * 180 / Math.PI) * deltaTime;
            }
            rotatingRunnable.run();
            gyroStartTime = currentTime;
        }

        @Override
        public void onAccuracyChanged(Sensor sensor, int i) {
        }
    };
    private SensorEventListener magneticListener = new SensorEventListener() {
        @Override
        public void onSensorChanged(SensorEvent sensorEvent) {
            for (int i = 0; i < magneticVector.length; i++) {
                magneticVector[i] = sensorEvent.values[i];
            }
        }

        @Override
        public void onAccuracyChanged(Sensor sensor, int i) {

        }
    };
    private SensorEventListener gravityListener = new SensorEventListener() {
        @Override
        public void onSensorChanged(SensorEvent sensorEvent) {
            for (int i = 0; i < gravityAccumulativeList.length; i++) {
                gravityAccumulativeList[i].add(-sensorEvent.values[i]);
                gravityAccumulation[i] += -sensorEvent.values[i];
            }
            if (gravityAccumulativeList[0].size() > NUMBER_OF_GRAVITY_DATA) {
                for (int i = 0; i < gravityAccumulativeList.length; i++) {
                    gravityAccumulation[i] -= gravityAccumulativeList[i].getFirst();
                    gravityAccumulativeList[i].removeFirst();
                    gravityVector[i] = gravityAccumulation[i] / NUMBER_OF_GRAVITY_DATA;
                }
            }
            calibrate();
        }

        @Override
        public void onAccuracyChanged(Sensor sensor, int i) {

        }
    };
}
