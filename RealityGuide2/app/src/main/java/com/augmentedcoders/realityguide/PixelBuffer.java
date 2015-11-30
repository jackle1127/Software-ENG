package com.augmentedcoders.realityguide;

import android.opengl.GLSurfaceView.Renderer;

import java.nio.IntBuffer;

import javax.microedition.khronos.opengles.GL10;
import javax.microedition.khronos.egl.EGL10;
import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.egl.EGLContext;
import javax.microedition.khronos.egl.EGLDisplay;
import javax.microedition.khronos.egl.EGLSurface;

public class PixelBuffer {
    Renderer currentRenderer;

    EGL10 egl10;
    EGLDisplay eglDisplay;
    EGLConfig[] eglConfigs;
    EGLConfig eglConfig;
    EGLContext eglContext;
    EGLSurface eglSurface;
    GL10 gl10;
    int width, height;

    public PixelBuffer() {
        width = Settings.display.getWidth();
        height = Settings.display.getHeight();

        int[] version = new int[2];
        int[] attributeList = new int[] {
                EGL10.EGL_WIDTH, width,
                EGL10.EGL_HEIGHT, height,
                EGL10.EGL_NONE
        };

        egl10 = (EGL10) EGLContext.getEGL();
        eglDisplay = egl10.eglGetDisplay(EGL10.EGL_DEFAULT_DISPLAY);
        egl10.eglInitialize(eglDisplay, version);
        eglConfig = chooseConfig();
        eglContext = egl10.eglCreateContext(eglDisplay, eglConfig, EGL10.EGL_NO_CONTEXT, null);
        eglSurface = egl10.eglCreatePbufferSurface(eglDisplay, eglConfig, attributeList);
        egl10.eglMakeCurrent(eglDisplay, eglSurface, eglSurface, eglContext);
        gl10 = (GL10) eglContext.getGL();
    }
    public void setRenderer(Renderer renderer) {
        currentRenderer = renderer;
        currentRenderer.onSurfaceCreated(gl10, eglConfig);
        currentRenderer.onSurfaceChanged(gl10, width, height);
    }

    public EGLConfig chooseConfig() {
        int[] attList = new int[] {
                EGL10.EGL_DEPTH_SIZE, 0,
                EGL10.EGL_STENCIL_SIZE, 0,
                EGL10.EGL_RED_SIZE, 8,
                EGL10.EGL_GREEN_SIZE, 8,
                EGL10.EGL_BLUE_SIZE, 8,
                EGL10.EGL_ALPHA_SIZE, 8,
                EGL10.EGL_NONE
        };

        int[] configCount = new int[1];
        egl10.eglChooseConfig(eglDisplay, attList, null, 0, configCount);
        int configSize = configCount[0];
        eglConfigs = new EGLConfig[configSize];
        egl10.eglChooseConfig(eglDisplay, attList, eglConfigs, configSize, configCount);

        return eglConfigs[0];
    }

    public int getColor(int x, int y) {
        if (currentRenderer != null) {
            currentRenderer.onDrawFrame(gl10);
            IntBuffer intBuffer = IntBuffer.allocate(1);
            gl10.glReadPixels(x, height - y - 1, 1, 1, GL10.GL_RGBA, GL10.GL_UNSIGNED_BYTE, intBuffer);
            return intBuffer.get(0) & 0xFFFFFF;
        }
        return -1;
    }
}
