package com.example.jack.opengltest;

import android.content.Context;
import android.hardware.Camera;
import android.view.Display;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

import java.util.List;

public class CameraPreview extends SurfaceView implements SurfaceHolder.Callback {
    private Camera camera;
    int camWidth = 0;
    int camHeight = 0;
    float angleOfView = -1.0f;
    boolean ready = false;
    Display display;
    static int cameraQuality = 0;
    int numOfCam = -1;

    public CameraPreview(Context context, Display ds) {
        super(context);
        getHolder().addCallback(this);
        getHolder().setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
        display = ds;
    }

    public void surfaceCreated(SurfaceHolder holder) {
        createCamera();
        ready = true;
    }

    static Camera getCamera() {
        Camera c = null;
        try {
            c = Camera.open();
        } catch (Exception e) {e.printStackTrace();}
        return c;
    }
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
        if (camera != null) {
            Camera.Parameters p = camera.getParameters();
            List<Camera.Size> previewSizes = p.getSupportedPreviewSizes();
            numOfCam = previewSizes.size();
            Setting.numOfCam = previewSizes.size();
            Camera.Size previewSize = previewSizes.get(cameraQuality);
            camWidth = previewSize.width;
            camHeight = previewSize.height;

            p.setFocusMode(Camera.Parameters.FOCUS_MODE_CONTINUOUS_PICTURE);
            p.setPreviewSize(camWidth, camHeight);
            angleOfView = p.getVerticalViewAngle();
            int orientation = display.getOrientation();
            if (orientation == 0) {
                camera.setDisplayOrientation(90);
            }
            if (orientation == 3) {
                camera.setDisplayOrientation(180);
            }
            camera.setParameters(p);
            try {
                camera.setPreviewDisplay(holder);
            } catch (Exception e) {
                e.printStackTrace();
            }
            camera.startPreview();
        }
    }

    public void surfaceDestroyed(SurfaceHolder holder) {
        releaseCamera();
        ready = false;
    }

    public void createCamera() {
        camera = getCamera();
        Runnable theRunnable = new Runnable() {
            @Override
            public void run() {
                while (camera == null);
                Camera.Parameters p = camera.getParameters();
                List<Camera.Size> previewSizes = p.getSupportedPreviewSizes();
                Camera.Size previewSize = previewSizes.get(0);
                camWidth = previewSize.width;
                camHeight = previewSize.height;
            }
        };
        Thread waitThread = new Thread(theRunnable);
        waitThread.start();
    }

    public void releaseCamera() {
        if (camera != null) {
            camera.stopPreview();
            camera.release();
            camera = null;
        }
    }

    public void changeCameraConfig() {
        if (camera != null) {
            camera.stopPreview();
            Camera.Parameters p = camera.getParameters();
            List<Camera.Size> previewSizes = p.getSupportedPreviewSizes();
            Camera.Size previewSize = previewSizes.get(cameraQuality);
            camWidth = previewSize.width;
            camHeight = previewSize.height;
            p.setFocusMode(Camera.Parameters.FOCUS_MODE_CONTINUOUS_PICTURE);
            p.setPreviewSize(camWidth, camHeight);
            angleOfView = p.getVerticalViewAngle();
            int orientation = display.getOrientation();
            if (orientation == 0) {
                camera.setDisplayOrientation(90);
            }
            if (orientation == 3) {
                camera.setDisplayOrientation(180);
            }
            camera.setParameters(p);
            camera.startPreview();
        }
    }
    public void startTheCamera() {
        Runnable theRunnable = new Runnable() {
            @Override
            public void run() {
                while (camera == null);
                camera.startPreview();
            }
        };
        Thread waitThread = new Thread(theRunnable);
        waitThread.start();
    }
}
