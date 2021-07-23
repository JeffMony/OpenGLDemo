package com.jeffmony.opengldemo.view;

import android.content.Context;
import android.util.AttributeSet;
import android.view.Surface;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

import com.jeffmony.opengldemo.egl.EglHelper;

import java.lang.ref.WeakReference;

import javax.microedition.khronos.egl.EGLContext;

public class JeffSurfaceView extends SurfaceView implements SurfaceHolder.Callback {

    public final static int RENDERMODE_WHEN_DIRTY = 0;
    public final static int RENDERMODE_CONTINUOUSLY = 1;

    private Surface mSurface;
    private EGLContext mEglContext;
    private JeffGLThread mGLThread;
    private JeffRender mRender;
    private int mRenderMode;

    public JeffSurfaceView(Context context) {
        this(context, null);
    }

    public JeffSurfaceView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public JeffSurfaceView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        getHolder().addCallback(this);
    }

    //设置外部的surface和EGLContext
    public void setSurfaceAndEglContext(Surface surface, EGLContext context) {
        mSurface = surface;
        mEglContext = context;
    }

    public void setRender(JeffRender render) {
        mRender = render;
    }

    public void setRenderMode(int mode) {
        if (mRender == null) {
            throw new RuntimeException("Must set render");
        }
        mRenderMode = mode;
    }

    public EGLContext getEglContext() {
        if (mGLThread != null) {
            mGLThread.getEglContext();
        }
        return null;
    }

    @Override
    public void surfaceCreated(SurfaceHolder holder) {
        if (mSurface == null) {
            mSurface = holder.getSurface();
        }

        mGLThread = new JeffGLThread(new WeakReference<>(this));
        mGLThread.mIsCreated = true;
        mGLThread.start();
    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
        mGLThread.mWidth = width;
        mGLThread.mHeight = height;
        mGLThread.mIsChanged = true;
    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {
        mGLThread.onDestory();
        mGLThread = null;
        mSurface = null;
        mEglContext = null;
    }

    public interface JeffRender {
        void onSurfaceCreated();
        void onSurfaceChanged(int width, int height);
        void onDrawFrame();
    }

    //创建静态内部类
    static class JeffGLThread extends Thread {

        private WeakReference<JeffSurfaceView> mJeffSurfaceViewReference;
        private EglHelper mEglHelper;
        boolean mIsExit = false;
        boolean mIsCreated = false;
        boolean mIsChanged = false;
        boolean mIsStarted = false;
        private int mWidth;
        private int mHeight;
        private Object mLock = null;


        public JeffGLThread(WeakReference<JeffSurfaceView> reference) {
            mJeffSurfaceViewReference = reference;
        }

        @Override
        public void run() {
            super.run();
            mLock = new Object();
            mIsExit = false;
            mEglHelper = new EglHelper();
            mEglHelper.initEgl(mJeffSurfaceViewReference.get().mSurface, mJeffSurfaceViewReference.get().mEglContext);

            while (true) {
                if (mIsExit) {

                    //释放资源
                    release();
                    break;
                }

                if (mIsStarted) {
                    //手动刷新
                    if (mJeffSurfaceViewReference.get().mRenderMode == RENDERMODE_WHEN_DIRTY) {
                        synchronized (mLock) {
                            try {
                                mLock.wait();
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        }
                    } else if (mJeffSurfaceViewReference.get().mRenderMode == RENDERMODE_CONTINUOUSLY) {
                        //自动刷新
                        try {
                            Thread.sleep(1000 / 60);
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    } else {
                        throw new RuntimeException("Please set the correct render mode");
                    }
                }

                //只会执行一次
                onCreated();
                onChanged(mWidth, mHeight);

                onDraw();

                mIsStarted = true;
            }
        }

        private void onCreated() {
            if (mIsCreated && mJeffSurfaceViewReference.get().mRender != null) {
                mJeffSurfaceViewReference.get().mRender.onSurfaceCreated();
            }
        }

        private void onChanged(int width, int height) {
            if (mIsChanged && mJeffSurfaceViewReference.get().mRender != null) {
                mJeffSurfaceViewReference.get().mRender.onSurfaceChanged(width, height);
            }
        }

        private void onDraw() {
            if (mJeffSurfaceViewReference.get().mRender != null) {
                mJeffSurfaceViewReference.get().mRender.onDrawFrame();
                if (!mIsStarted) {
                    mJeffSurfaceViewReference.get().mRender.onDrawFrame();
                }
                mEglHelper.swapBuffers();
            }
        }

        private void requestRender() {
            if (mLock != null) {
                synchronized (mLock) {
                    mLock.notifyAll();
                }
            }
        }

        public void onDestory() {
            mIsExit = true;
            requestRender();
        }

        public void release() {
            if (mEglHelper != null) {
                mEglHelper.destoryEgl();
                mEglHelper = null;
                mLock = null;
                mJeffSurfaceViewReference = null;
            }
        }

        public EGLContext getEglContext() {
            if (mEglHelper != null) {
                return mEglHelper.getEglContext();
            }
            return null;
        }
    }
}
