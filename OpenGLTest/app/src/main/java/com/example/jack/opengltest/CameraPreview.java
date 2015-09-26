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
    public CameraPreview(Context context, Display ds) {
        super(context);
        getHolder().addCallback(this);
        getHolder().setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
        display = ds;
    }

    public void surfaceCreated(SurfaceHolder holder) {
        camera = getCamera();
        Camera.Parameters p = camera.getParameters();
        List<Camera.Size> previewSizes = p.getSupportedPreviewSizes();
        Camera.Size previewSize = previewSizes.get(0);
        camWidth = previewSize.width;
        camHeight = previewSize.height;
    }

    static Camera getCamera() {
        Camera c = null;
        try {
            c = Camera.open();
        } catch (Exception e) {e.printStackTrace();}
        return c;
    }
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {

        Camera.Parameters p = camera.getParameters();
        List<Camera.Size> previewSizes = p.getSupportedPreviewSizes();
        Camera.Size previewSize = previewSizes.get(previewSizes.size() * 4 / 5);
        camWidth = previewSize.width;
        camHeight = previewSize.height;
        ready = true;
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
        } catch(Exception e) {
            e.printStackTrace();
        }
        camera.startPreview();
    }

    public void surfaceDestroyed(SurfaceHolder holder) {
        camera.stopPreview();
        camera.release();
        camera = null;
    }
}
