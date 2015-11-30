package com.augmentedcoders.realityguide;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;
import android.opengl.GLUtils;

import com.google.android.gms.maps.model.LatLng;

import org.json.JSONObject;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;

import javax.microedition.khronos.opengles.GL10;

public class Post {
    private FloatBuffer verticesBuffer;
    private FloatBuffer textureBuffer;
    private FloatBuffer colorBuffer;
    private LatLng latLng;
    private CartesianLocation location;
    private float[] projection;
    private int[] textures = new int[1];
    private Bitmap content = null;
    private float[] vertices = {
            -2.0f,  1.0f, 0,
             2.0f,  1.0f, 0,
            -2.0f, -1.0f, 0,
             2.0f, -1.0f, 0
    };
    private float[] texture = {
            0.0f, 0.0f,
            1.0f, 0.0f,
            0.0f, 1.0f,
            1.0f, 1.0f
    };
    private float[] uniqueColor = new float[16];
    protected boolean textured = false;
    private int id = 0;


    Post() {
        id = Settings.postID++;
        if (Settings.postID >= 0xFFFFFF - 1) Settings.postID = 1;
        float[] color = new float[3];
        color[2] = (float) (id >>> 16 & 0xFF) / 255;
        color[1] = (float) (id >>> 8 & 0xFF) / 255;
        color[0] = (float) (id & 0xFF) / 255;
        for (int i = 0; i < uniqueColor.length; i += 4) {
            for (int j = 0; j < 3; j++){
                uniqueColor[i + j] = color[j];
            }
            uniqueColor[i + 3] = 1.0f;
        }

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

        byteBuffer = ByteBuffer.allocateDirect(uniqueColor.length * 4);
        byteBuffer.order(ByteOrder.nativeOrder());
        colorBuffer = byteBuffer.asFloatBuffer();
        colorBuffer.put(uniqueColor);
        colorBuffer.position(0);
    }

    protected void setLatLng(LatLng newLatLng) {
        latLng = newLatLng;
        location = MathFunctions.getLocationFromLatLng(newLatLng);
        projection = MathFunctions.getProjection(location);
    }

    protected void refreshLocation() {
        location = MathFunctions.getLocationFromLatLng(latLng);
        projection = MathFunctions.getProjection(location);
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

    protected float[] getProjection() {
        return projection;
    }

    protected void createTexture(GL10 gl) {
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

    protected void drawID(GL10 gl) {
        gl.glEnableClientState(GL10.GL_VERTEX_ARRAY);
        gl.glEnableClientState(GL10.GL_COLOR_ARRAY);

        gl.glFrontFace(GL10.GL_CW);

        gl.glVertexPointer(3, GL10.GL_FLOAT, 0, verticesBuffer);
        gl.glColorPointer(4, GL10.GL_FLOAT, 0, colorBuffer);

        gl.glDrawArrays(GL10.GL_TRIANGLE_STRIP, 0, vertices.length / 3);

        gl.glDisableClientState(GL10.GL_VERTEX_ARRAY);
        gl.glDisableClientState(GL10.GL_COLOR_ARRAY);
    }

    protected void drawTextInBound(Canvas canvas, String text,
                                   Rect bound, Paint paint, boolean multilines) {
        Rect heightRect = new Rect();
        paint.getTextBounds(text, 0, text.length(), heightRect);
        int textHeight = heightRect.height() + 2;
        paint.setTextAlign(Paint.Align.LEFT);
        if (!multilines) {
            String newText = text;
            for (int i = 0; i < text.length(); i++) {
                if (paint.measureText(text, 0, i) >= bound.width()) {
                    newText = text.substring(0, i - 3) + "...";
                    break;
                }
            }
            canvas.drawText(newText, bound.left, bound.top + textHeight, paint);
        } else {
            int y = textHeight;
            for (int i = 0; i < text.length(); i++) {
                if (paint.measureText(text, 0, i) >= bound.width() || i == text.length() - 1) {
                    canvas.drawText(text.substring(0, i), bound.left, bound.top + y,
                            paint);
                    y += textHeight;
                    text = text.substring(i);
                    i = -1;
                    if (y > bound.height()) break;
                }
            }
        }
    }

    protected int getId() {
        return id;
    }
}
