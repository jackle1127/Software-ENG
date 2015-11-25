package com.example.jack.realityguide;

import android.content.Context;
import android.graphics.Bitmap;
import android.opengl.GLSurfaceView;
import android.opengl.GLU;
import android.opengl.Matrix;
import android.view.Display;
import android.view.SurfaceView;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

public class OpenGLRenderer implements GLSurfaceView.Renderer {
    /* 1 = 1m
     * Distance from ground: 1.8m
     * Map m/px = 0.19
     * Map size = 1200 x 1200
     */
    float[] rotationMatrix = new float[16];
    float[] tempRotationMatrix = new float[16];
    float x = 0;
    float y = 0;
    float z = 0;
    float[] anchorMatrix = new float[16];
    float[] tempMatrix = new float[16];
    float currentFov = 45.0f;
    int orientation = 0;
    Context context;
    int globalWidth = 1;
    int globalHeight = 1;
    int camWidth = 1;
    int camHeight = 1;
    boolean gyroMode = false;

    public OpenGLRenderer(Context context) {
        this.context = context;
    }
    @Override
    public void onSurfaceCreated(GL10 gl, EGLConfig config) {
        gl.glEnable(GL10.GL_TEXTURE_2D);
        gl.glShadeModel(GL10.GL_SMOOTH);
        gl.glClearColor(0.0f, 0.0f, 0.0f, 0.0f);
        gl.glClearDepthf(1.0f);
        gl.glEnable(GL10.GL_DEPTH_TEST);
        gl.glDepthFunc(GL10.GL_LEQUAL);
        gl.glHint(GL10.GL_PERSPECTIVE_CORRECTION_HINT,
                GL10.GL_NICEST);
        Matrix.setIdentityM(anchorMatrix, 0);
    }

    public void calibrate(float[] matrix) {
        float difference = 0;
        for (int i = 0; i < matrix.length; i++) {
            difference += Math.abs(matrix[i] - anchorMatrix[i]);
        }
        if (difference > .4f || !gyroMode && difference > .15) {
            for (int i = 0; i < matrix.length; i++) {
                anchorMatrix[i] = matrix[i];
            }
        }
    }

    public void rotate () {
        if (gyroMode) {
            Matrix.rotateM(anchorMatrix, 0, x, anchorMatrix[0], anchorMatrix[4], anchorMatrix[8]);
            Matrix.rotateM(anchorMatrix, 0, y, anchorMatrix[1], anchorMatrix[5], anchorMatrix[9]);
            Matrix.rotateM(anchorMatrix, 0, z, anchorMatrix[2], anchorMatrix[6], anchorMatrix[10]);
        }
    }

    @Override
    public void onDrawFrame(GL10 gl) {
        if (Settings.angleOfView != currentFov) {
            changeFocalLength(gl);
        }
        gl.glClear(GL10.GL_COLOR_BUFFER_BIT | GL10.GL_DEPTH_BUFFER_BIT);
        gl.glLoadIdentity();
        for (int i = 0; i < anchorMatrix.length; i++) {
            tempMatrix[i] = anchorMatrix[i];
        }
        if (orientation == 1) {
            Matrix.rotateM(tempMatrix, 0, 90, tempMatrix[2], tempMatrix[6], tempMatrix[10]);
        } else if (orientation == 3) {
            Matrix.rotateM(tempMatrix, 0, -90, tempMatrix[2], tempMatrix[6], tempMatrix[10]);
        } else if (orientation == 2) {
            Matrix.rotateM(tempMatrix, 0, 180, tempMatrix[2], tempMatrix[6], tempMatrix[10]);
        }

    }

    private void changeFocalLength(GL10 gl) {
        gl.glViewport(0, 0, globalWidth, globalHeight);
        gl.glMatrixMode(GL10.GL_PROJECTION);
        gl.glLoadIdentity();
        if (Settings.angleOfView > 0) currentFov = Settings.angleOfView;
        float distance = (float) Math.sqrt(Settings.mapScale * Settings.mapScale / 4
                + Settings.distanceFromGround * Settings.distanceFromGround);
        if (orientation == 1 || orientation == 3) {
            GLU.gluPerspective(gl, currentFov * (float) camHeight / (float) camWidth,
                    (float) globalWidth / (float) globalHeight, 0.1f, distance);
        } else {
            GLU.gluPerspective(gl, currentFov, (float) globalWidth / (float) globalHeight, 0.1f, distance);
        }
        gl.glViewport(0, 0, globalWidth, globalHeight);
        gl.glMatrixMode(GL10.GL_MODELVIEW);
    }

    @Override
    public void onSurfaceChanged(GL10 gl, int width, int height) {
        globalWidth = width;
        globalHeight = height;
        changeFocalLength(gl);
    }

    private void rotateMatrix(float[] who, float[] whichTemp, float degree, float rx, float ry, float rz) {
        for (int i = 0; i < tempRotationMatrix.length; i++) {
            tempRotationMatrix[i] = who[i];
        }
        Matrix.setRotateM(whichTemp, 0, degree, rx, ry, rz);
        Matrix.multiplyMM(who, 0, tempRotationMatrix, 0, rotationMatrix, 0);
    }
}
