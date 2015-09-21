package com.example.jack.opengltest;

import android.content.Context;
import android.graphics.Bitmap;
import android.opengl.GLUtils;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;

import javax.microedition.khronos.opengles.GL10;

public class Plane {
    private FloatBuffer verticesBuffer;
    private FloatBuffer textureBuffer;
    private int[] textures = new int[1];
    Bitmap theBitmap;
    private float[] vertices = {
            -1.0f, 0,  1.0f,
            -1.0f, 0, -1.0f,
             1.0f, 0,  1.0f,
             1.0f, 0, -1.0f
    };
    private float[] texture = {
            0.0f, 1.0f,
            0.0f, 0.0f,
            1.0f, 1.0f,
            1.0f, 0.0f
    };

    Plane(Bitmap bm) {
        ByteBuffer theByteBuffer = ByteBuffer.allocateDirect(vertices.length * 4);
        theByteBuffer.order(ByteOrder.nativeOrder());
        verticesBuffer = theByteBuffer.asFloatBuffer();
        verticesBuffer.put(vertices);
        verticesBuffer.position(0);

        theByteBuffer = ByteBuffer.allocateDirect(texture.length * 4);
        theByteBuffer.order(ByteOrder.nativeOrder());
        textureBuffer = theByteBuffer.asFloatBuffer();
        textureBuffer.put(texture);
        textureBuffer.position(0);

        theBitmap = bm;
    }

    void loadGlTexture(GL10 gl, Context context) {
        gl.glGenTextures(1, textures, 0);
        gl.glBindTexture(GL10.GL_TEXTURE_2D, textures[0]);

        gl.glTexParameterf(GL10.GL_TEXTURE_2D, GL10.GL_TEXTURE_MIN_FILTER, GL10.GL_NEAREST);
        gl.glTexParameterf(GL10.GL_TEXTURE_2D, GL10.GL_TEXTURE_MAG_FILTER, GL10.GL_LINEAR);

        GLUtils.texImage2D(GL10.GL_TEXTURE_2D, 0, theBitmap, 0);
        theBitmap.recycle();
    }

    void draw(GL10 gl) {
        gl.glBindTexture(GL10.GL_TEXTURE_2D, textures[0]);

        gl.glEnableClientState(GL10.GL_VERTEX_ARRAY);
        gl.glEnableClientState(GL10.GL_TEXTURE_COORD_ARRAY);

        gl.glFrontFace(GL10.GL_CW);

        gl.glVertexPointer(3, GL10.GL_FLOAT, 0, verticesBuffer);
        gl.glTexCoordPointer(2, GL10.GL_FLOAT, 0, textureBuffer);

        gl.glDrawArrays(GL10.GL_TRIANGLE_STRIP, 0, vertices.length / 3);

        gl.glDisableClientState(GL10.GL_VERTEX_ARRAY);
        gl.glDisableClientState(GL10.GL_TEXTURE_COORD_ARRAY);
    }
}
