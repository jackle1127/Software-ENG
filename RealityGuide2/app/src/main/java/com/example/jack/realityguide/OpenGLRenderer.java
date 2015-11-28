package com.example.jack.realityguide;

import android.content.Context;
import android.opengl.GLSurfaceView;
import android.opengl.GLU;
import android.opengl.Matrix;
import android.view.Surface;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

public class OpenGLRenderer implements GLSurfaceView.Renderer {
    float x = 0;
    float y = 0;
    float z = 0;
    float[] anchorMatrix = new float[16];
    float[] tempMatrix = new float[16];
    float currentFov = 45.0f;
    Context context;
    int globalWidth = 1;
    int globalHeight = 1;
    int camWidth = 1;
    int camHeight = 1;

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
        if (difference > .4f || !Settings.gyroMode && difference > .15) {
            for (int i = 0; i < matrix.length; i++) {
                anchorMatrix[i] = matrix[i];
            }
        }
    }

    public void rotate () {
        if (Settings.gyroMode) {
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

        if (Settings.display.getRotation() == Surface.ROTATION_90) {
            Matrix.rotateM(tempMatrix, 0, 90, tempMatrix[2], tempMatrix[6], tempMatrix[10]);
        } else if (Settings.display.getRotation() == Surface.ROTATION_270) {
            Matrix.rotateM(tempMatrix, 0, -90, tempMatrix[2], tempMatrix[6], tempMatrix[10]);
        }
        gl.glMultMatrixf(tempMatrix, 0);
        for (int i = 0; i < Settings.latLngs.size(); i++) {
            Post post = Settings.latLngs.get(i);
            if (!post.textured) post.createTexture(gl);
            gl.glPushMatrix();
            gl.glTranslatef(-post.getLocation().x, 0, -post.getLocation().z);
            gl.glScalef(2.5f, 2.5f, 2.5f);
            post.draw(gl);
            gl.glPopMatrix();
        }
    }

    private void changeFocalLength(GL10 gl) {
        gl.glViewport(0, 0, globalWidth, globalHeight);
        gl.glMatrixMode(GL10.GL_PROJECTION);
        gl.glLoadIdentity();
        if (Settings.angleOfView > 0) currentFov = Settings.angleOfView;
        float distance = 200;
        if (Settings.display.getRotation() == Surface.ROTATION_90
                || Settings.display.getRotation() == Surface.ROTATION_270) {
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
}
