package com.augmentedcoders.realityguide;

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
            setConfigs();
            try {
                camera.setPreviewDisplay(holder);
            } catch (Exception e) {
                e.printStackTrace();
            }
            camera.startPreview();
        }
    }

    public void changeCameraConfig() {
        if (camera != null) {
            camera.stopPreview();
            setConfigs();
            camera.startPreview();
        }
    }

    private void setConfigs() {
        Camera.Parameters p = camera.getParameters();
        List<Camera.Size> previewSizes = p.getSupportedPreviewSizes();
        Camera.Size previewSize = previewSizes.get(Settings.cameraQuality);
        Settings.numberOfCameras = previewSizes.size();
        Settings.camWidth = previewSize.width;
        Settings.camHeight = previewSize.height;
        p.setFocusMode(Camera.Parameters.FOCUS_MODE_CONTINUOUS_PICTURE);
        p.setPreviewSize(Settings.camWidth, Settings.camHeight);
        Settings.angleOfView = p.getVerticalViewAngle();
        int orientation = Settings.display.getRotation();
        if (orientation == 1) {
            camera.setDisplayOrientation(0);
        } else if (orientation == 3) {
            camera.setDisplayOrientation(180);
        }
        camera.setParameters(p);
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
                long timeElapsed = System.currentTimeMillis();
                boolean valid = true;
                while (camera == null) {
                    if ((System.currentTimeMillis() - timeElapsed) % 400 < 150)
                        System.out.println("lookings fo cam");
                    if (System.currentTimeMillis() - timeElapsed > 1500) {
                        valid = false;
                        break;
                    }
                }
                if (valid) {
                    Camera.Parameters p = camera.getParameters();
                    List<Camera.Size> previewSizes = p.getSupportedPreviewSizes();
                    Camera.Size previewSize = previewSizes.get(0);
                    Settings.camWidth = previewSize.width;
                    Settings.camHeight = previewSize.height;
                }
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
            System.out.println("release");
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
