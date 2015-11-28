package com.example.jack.realityguide;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.opengl.GLUtils;

import com.google.android.gms.maps.model.LatLng;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;

import javax.microedition.khronos.opengles.GL10;

public class Post {
    private FloatBuffer verticesBuffer;
    private FloatBuffer textureBuffer;
    private LatLng latLng;
    private CartesianLocation location;
    private double theta = 0;
    private double distance = 0;
    private int[] textures = new int[1];
    private Bitmap content = null;
    private float[] vertices = {
            -2.0f,  0.5f, 0,
             2.0f,  0.5f, 0,
            -2.0f, -0.5f, 0,
             2.0f, -0.5f, 0
    };
    private float[] texture = {
            0.0f, 0.0f,
            1.0f, 0.0f,
            0.0f, 1.0f,
            1.0f, 1.0f
    };
    protected boolean textured = false;

    Post() {
        ByteBuffer byteBuffer = ByteBuffer.allocateDirect(vertices.length * 4);
        byteBuffer.order(ByteOrder.nativeOrder());
        verticesBuffer = byteBuffer.asFloatBuffer();
        verticesBuffer.put(vertices);
        verticesBuffer.position(0);

        byteBuffer = ByteBuffer.allocateDirect(texture.length * 4);
        byteBuffer.order(ByteOrder.nativeOrder());
        textureBuffer = byteBuffer.asFloatBuffer();
        textureBuffer.put(texture);
        textureBuffer.position(0);
    }

    protected void setLatLng(LatLng newLatLng) {
        latLng = newLatLng;
        location = MathFunctions.getLocationFromLatLng(newLatLng);
    }

    protected void setContent(Bitmap newContent) {
        content = newContent;
    }

    protected Bitmap getContent() {
        return content;
    }

    protected CartesianLocation getLocation() {
        return location;
    }

    protected LatLng getLatLng() {
        return latLng;
    }

    protected void createTexture(GL10 gl) {
        //Bitmap newTexture = BitmapFactory.decodeResource(Settings.resources, R.drawable.potato);
        gl.glGenTextures(1, textures, 0);
        gl.glBindTexture(GL10.GL_TEXTURE_2D, textures[0]);

        gl.glTexParameterf(GL10.GL_TEXTURE_2D, GL10.GL_TEXTURE_MIN_FILTER, GL10.GL_NEAREST);
        gl.glTexParameterf(GL10.GL_TEXTURE_2D, GL10.GL_TEXTURE_MAG_FILTER, GL10.GL_LINEAR);

        GLUtils.texImage2D(GL10.GL_TEXTURE_2D, 0, content, 0);
        textured = true;
    }

    protected void draw(GL10 gl) {
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
