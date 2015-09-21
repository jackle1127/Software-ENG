package com.example.jack.opengltest;

import android.content.Context;
import android.graphics.Bitmap;
import android.opengl.GLSurfaceView;
import android.opengl.GLU;
import android.opengl.Matrix;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

public class OpenGLRenderer implements GLSurfaceView.Renderer {
    private Cube mCube = new Cube();
    private Cube2 mCube2 = new Cube2();
    private Axes axes = new Axes();
    private Plane ground;
    float x = 0;
    float y = 0;
    float z = 0;
    float[] local = new float[16];
    Context context;
    Bitmap groundBitmap;

    OpenGLRenderer(Context context, Bitmap bm) {
        this.context = context;
        groundBitmap = bm;
        ground = new Plane(groundBitmap);
    }
    @Override
    public void onSurfaceCreated(GL10 gl, EGLConfig config) {
        ground.loadGlTexture(gl, this.context);

        gl.glEnable(GL10.GL_TEXTURE_2D);
        gl.glShadeModel(GL10.GL_SMOOTH);
        gl.glClearColor(0.0f, 0.0f, 0.0f, 0.5f);
        gl.glClearDepthf(1.0f);
        gl.glEnable(GL10.GL_DEPTH_TEST);
        gl.glDepthFunc(GL10.GL_LEQUAL);
        gl.glHint(GL10.GL_PERSPECTIVE_CORRECTION_HINT,
                GL10.GL_NICEST);
        Matrix.setIdentityM(local, 0);
    }

    public void calibrate(float[] matrix) {
        float difference = 0;
        for (int i = 0; i < matrix.length; i++) {
            difference += Math.abs(matrix[i] - local[i]);
        }
        if (difference > 0.3f) {
            for (int i = 0; i < matrix.length; i++) {
                local[i] = matrix[i];
            }
        }
    }

    public void rotate () {
//        Matrix.rotateM(local, 0, x, local[0], local[4], local[8]);
//        Matrix.rotateM(local, 0, y, local[1], local[5], local[9]);
//        Matrix.rotateM(local, 0, z, local[2], local[6], local[10]);
        Matrix.rotateM(local, 0, y, 0, 1, 0);
        Matrix.rotateM(local, 0, x, 1, 0, 0);
        Matrix.rotateM(local, 0, z, 0, 0, 1);
        System.out.println("\nBegin:");
        for (int i = 0; i < local.length; i++) {
            if (i % 4 == 0) {
                System.out.println();
            }
            System.out.print(local[i] + ", ");
        }
    }

    @Override
    public void onDrawFrame(GL10 gl) {
        gl.glClear(GL10.GL_COLOR_BUFFER_BIT | GL10.GL_DEPTH_BUFFER_BIT);
        gl.glLoadIdentity();
        gl.glMultMatrixf(local, 0);
        gl.glTranslatef(0, -3f, 0);

        gl.glPushMatrix();
        gl.glTranslatef(0, 0, -6f);
        mCube.draw(gl);
        gl.glPopMatrix();
        gl.glPushMatrix();
        gl.glTranslatef(0, 3f, -6f);
        mCube.draw(gl);
        gl.glPopMatrix();
        gl.glPushMatrix();
        gl.glTranslatef(0, 0, 6f);
        mCube.draw(gl);
        gl.glPopMatrix();
        gl.glPushMatrix();
        gl.glTranslatef(5f, 0, 0);
        mCube.draw(gl);
        gl.glPopMatrix();
        gl.glPushMatrix();
        gl.glTranslatef(0, -1f, 0);
        gl.glScalef(15.0f, 1.0f, 15.0f);
        ground.draw(gl);
        gl.glPopMatrix();
        gl.glLoadIdentity();
    }

    @Override
    public void onSurfaceChanged(GL10 gl, int width, int height) {
        gl.glViewport(0, 0, width, height);
        gl.glMatrixMode(GL10.GL_PROJECTION);
        GLU.gluPerspective(gl, 65.0f, (float) width / (float) height, 0.1f, 100.0f);
        gl.glViewport(0, 0, width, height);

        gl.glMatrixMode(GL10.GL_MODELVIEW);
    }
}
