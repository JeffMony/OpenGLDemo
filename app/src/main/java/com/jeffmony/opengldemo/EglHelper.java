package com.jeffmony.opengldemo;

import android.view.Surface;

import javax.microedition.khronos.egl.EGL;
import javax.microedition.khronos.egl.EGL10;
import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.egl.EGLContext;
import javax.microedition.khronos.egl.EGLDisplay;
import javax.microedition.khronos.egl.EGLSurface;

/**
 * EGL环境创建的类
 */
public class EglHelper {
    private EGL10 mEgl10;
    private EGLContext mEglContext;
    private EGLDisplay mEglDisplay;
    private EGLSurface mEglSurface;

    public void initEgl(Surface surface, EGLContext context) {
        //1.
        mEgl10 = (EGL10) EGLContext.getEGL();

        //2.
        mEglDisplay = mEgl10.eglGetDisplay(EGL10.EGL_DEFAULT_DISPLAY);
        if (mEglDisplay == EGL10.EGL_NO_DISPLAY) {
            throw new RuntimeException("eglGetDisplay failed");
        }

        //3.
        int[] version = new int[2];
        if (!mEgl10.eglInitialize(mEglDisplay, version)) {
            throw new RuntimeException("eglInitialize failed");
        }

        //4.设置属性
        int[] attributes = new int[]{
                EGL10.EGL_RED_SIZE, 8,
                EGL10.EGL_GREEN_SIZE, 8,
                EGL10.EGL_BLUE_SIZE, 8,
                EGL10.EGL_ALPHA_SIZE, 8,
                EGL10.EGL_DEPTH_SIZE, 8,
                EGL10.EGL_STENCIL_SIZE, 8,
                EGL10.EGL_RENDERABLE_TYPE, 4,
                EGL10.EGL_NONE
        };

        int[] numConfigs = new int[1];
        if (!mEgl10.eglChooseConfig(mEglDisplay, attributes, null, 1, numConfigs)) {
            throw new RuntimeException("eglChooseConfig failed");
        }

        int numConfig = numConfigs[0];
        if (numConfig <= 0) {
            throw new RuntimeException("No match configs");
        }

        //5.
        EGLConfig[] configs = new EGLConfig[numConfig];
        if (!mEgl10.eglChooseConfig(mEglDisplay, attributes, configs, numConfig, numConfigs)) {
            throw new RuntimeException("eglChooseConfig failed final");
        }

        //6.
        if (context != null) {
            mEglContext = mEgl10.eglCreateContext(mEglDisplay, configs[0], context, null);
        } else {
            mEglContext = mEgl10.eglCreateContext(mEglDisplay, configs[0], EGL10.EGL_NO_CONTEXT, null);
        }

        //7.
        mEglSurface = mEgl10.eglCreateWindowSurface(mEglDisplay, configs[0], surface, null);

        //8.
        if (!mEgl10.eglMakeCurrent(mEglDisplay, mEglSurface, mEglSurface, mEglContext)) {
            throw new RuntimeException("eglMakeCurrent failed");
        }
    }

    //刷新数据
    public boolean swapBuffers() {
        if (mEgl10 != null) {
            return mEgl10.eglSwapBuffers(mEglDisplay, mEglSurface);
        } else {
            throw new RuntimeException("EGL is empty");
        }
    }

    public EGLContext getEglContext() {
        return mEglContext;
    }

    public void destoryEgl() {
        if (mEgl10 != null) {
            mEgl10.eglMakeCurrent(mEglDisplay, EGL10.EGL_NO_SURFACE, EGL10.EGL_NO_SURFACE, EGL10.EGL_NO_CONTEXT);
            mEgl10.eglDestroySurface(mEglDisplay, mEglSurface);
            mEglSurface = null;

            mEgl10.eglDestroyContext(mEglDisplay, mEglContext);
            mEglContext = null;

            mEgl10.eglTerminate(mEglDisplay);
            mEglDisplay = null;
            mEgl10 = null;
        }
    }
}
