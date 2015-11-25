package com.example.jack.realityguide;

import android.content.Context;
import android.hardware.Camera;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import java.util.List;

public class CameraPreview extends SurfaceView implements SurfaceHolder.Callback {
    private Camera camera;
    boolean ready = false;

    public CameraPreview(Context context) {
        super(context);
        getHolder().addCallback(this);
        getHolder().setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
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
            Settings.numberOfCameras = previewSizes.size();
            Camera.Size previewSize = previewSizes.get(Settings.cameraQuality);
            Settings.camWidth = previewSize.width;
            Settings.camHeight = previewSize.height;

            p.setFocusMode(Camera.Parameters.FOCUS_MODE_CONTINUOUS_PICTURE);
            p.setPreviewSize(Settings.camWidth, Settings.camHeight);
            Settings.angleOfView = p.getVerticalViewAngle();
            int orientation = Settings.display.getOrientation();
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
                Settings.camWidth = previewSize.width;
                Settings.camHeight = previewSize.height;
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
            Camera.Size previewSize = previewSizes.get(Settings.cameraQuality);
            Settings.camWidth = previewSize.width;
            Settings.camHeight = previewSize.height;
            p.setFocusMode(Camera.Parameters.FOCUS_MODE_CONTINUOUS_PICTURE);
            p.setPreviewSize(Settings.camWidth, Settings.camHeight);
            Settings.angleOfView = p.getVerticalViewAngle();
            int orientation = Settings.display.getOrientation();
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
