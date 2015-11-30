package com.augmentedcoders.realityguide;

import android.content.Context;
import android.opengl.GLSurfaceView;
import android.opengl.GLU;
import android.opengl.Matrix;
import android.view.Surface;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

public class OpenGLRenderer implements GLSurfaceView.Renderer {
    float rX = 0;
    float rY = 0;
    float rZ = 0;
    float currentFov = 45.0f;
    Context context;
    int globalWidth = 1;
    int globalHeight = 1;

    Axes axes = new Axes();
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
        Matrix.setIdentityM(Settings.anchorMatrix, 0);
    }

    public void calibrate(float[] matrix) {
        float difference = 0;
        for (int i = 0; i < matrix.length; i++) {
            difference += Math.abs(matrix[i] - Settings.anchorMatrix[i]);
        }
        if (difference > 1.0f || !Settings.gyroMode && difference > .08) {
            for (int i = 0; i < matrix.length; i++) {
                Settings.anchorMatrix[i] = matrix[i];
            }
        }
    }

    public void rotate () {
        if (Settings.gyroMode) {
            Matrix.rotateM(Settings.anchorMatrix, 0, rX, Settings.anchorMatrix[0], Settings.anchorMatrix[4], Settings.anchorMatrix[8]);
            Matrix.rotateM(Settings.anchorMatrix, 0, rY, Settings.anchorMatrix[1], Settings.anchorMatrix[5], Settings.anchorMatrix[9]);
            Matrix.rotateM(Settings.anchorMatrix, 0, rZ, Settings.anchorMatrix[2], Settings.anchorMatrix[6], Settings.anchorMatrix[10]);
        }
    }

    @Override
    public void onDrawFrame(GL10 gl) {
        if (Settings.angleOfView != currentFov) {
            changeFocalLength(gl);
        }
        gl.glClear(GL10.GL_COLOR_BUFFER_BIT | GL10.GL_DEPTH_BUFFER_BIT);
        gl.glLoadIdentity();
        gl.glMultMatrixf(Settings.anchorMatrix, 0);
        for (int i = 0; i < Settings.pointOfInterestPosts.size(); i++) {
            drawPost(gl, Settings.pointOfInterestPosts.get(i));
        }
        for (int i = 0; i < Settings.communityPosts.size(); i++) {
            drawPost(gl, Settings.communityPosts.get(i));
        }
        gl.glLoadIdentity();
//        gl.glPushMatrix();
//        gl.glTranslatef(0, 0, -4);
//        gl.glMultMatrixf(anchorMatrix, 0);
//        gl.glScalef(3.5f, 3.5f, 3.5f);
//        axes.draw(gl);
//        gl.glPopMatrix();
    }

    private void drawPost(GL10 gl, Post post) {
        if (!post.textured) post.createTexture(gl);
        if (post.getProjection()[1] > Settings.MINIMUM_DISTANCE) {
            gl.glPushMatrix();
            gl.glRotatef(post.getProjection()[0], 0, 1.0f, 0);
            gl.glRotatef(post.getProjection()[1] * Settings.POST_ANGLE_MULTIPLIER / 150, 1.0f, 0, 0);
            gl.glTranslatef(0, 0, -post.getProjection()[1]);
            gl.glScalef(Settings.POST_SIZE_MULTIPLIER,
                    Settings.POST_SIZE_MULTIPLIER, Settings.POST_SIZE_MULTIPLIER);
            post.draw(gl);
            gl.glPopMatrix();
        }
    }
    private void changeFocalLength(GL10 gl) {
        gl.glViewport(0, 0, globalWidth, globalHeight);
        gl.glMatrixMode(GL10.GL_PROJECTION);
        gl.glLoadIdentity();
        if (Settings.angleOfView > 0) currentFov = Settings.angleOfView;
        float distance = 600;
        if (Settings.display.getRotation() == Surface.ROTATION_90
                || Settings.display.getRotation() == Surface.ROTATION_270) {
            GLU.gluPerspective(gl, currentFov * (float) Settings.camHeight / (float) Settings.camWidth,
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
