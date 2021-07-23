package com.jeffmony.opengldemo.render;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.opengl.GLES20;
import android.opengl.GLUtils;
import android.opengl.Matrix;
import android.util.Log;

import com.jeffmony.opengldemo.R;
import com.jeffmony.opengldemo.utils.JeffShaderUtils;
import com.jeffmony.opengldemo.view.JeffSurfaceView;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;

public class CustomTextureRender implements JeffSurfaceView.JeffRender {

    private static final String TAG = "CustomTextureRender";

    private static final int PICTURE_WIDTH = 1000;
    private static final int PICTURE_HEIGHT = 1333;

    private Context mContext;

    //顶点坐标的四个点
    private float[] mVertexData = {
            -1f, -1f,
            1f, -1f,
            -1f, 1f,
            1f, 1f,
    };
    private FloatBuffer mVertexBuffer;

    //纹理坐标
    private float[] mFragmentData = {
            0f, 1f,
            1f, 1f,
            0f, 0f,
            1f, 0f,
    };
    private FloatBuffer mFragmentBuffer;

    private int mVPosition;
    private int mFPosition;
    private int mProgram;
    private int mTextureId;
    private int mSampler;
    private int mVboId;
    private int mFboId;
    private int mImageTextureId;

    private FboRender mFboRender;

    private int mMatrixId;
    private float[] mMatrix = new float[16];

    public CustomTextureRender(Context context) {
        mContext = context;

        mFboRender = new FboRender(context);

        mVertexBuffer = ByteBuffer.allocateDirect(mVertexData.length * 4)
                .order(ByteOrder.nativeOrder())
                .asFloatBuffer()
                .put(mVertexData);
        mVertexBuffer.position(0);

        mFragmentBuffer = ByteBuffer.allocateDirect(mFragmentData.length * 4)
                .order(ByteOrder.nativeOrder())
                .asFloatBuffer()
                .put(mFragmentData);
        mFragmentBuffer.position(0);
    }


    @Override
    public void onSurfaceCreated() {

        mFboRender.onCreate();
        String vertexSource = JeffShaderUtils.getRawResource(mContext, R.raw.m_vertex_shader);
        String fragmentSource = JeffShaderUtils.getRawResource(mContext, R.raw.fragment_shader);
        mProgram = JeffShaderUtils.createProgram(vertexSource, fragmentSource);
        mVPosition = GLES20.glGetAttribLocation(mProgram, "v_Position");
        mFPosition = GLES20.glGetAttribLocation(mProgram, "f_Position");
        mSampler = GLES20.glGetUniformLocation(mProgram, "sTexture");
        mMatrixId = GLES20.glGetUniformLocation(mProgram, "u_Matrix");

        //VBO buffer模块
        int[] vbos = new int[1];
        //第一个参数是绘制的个数
        GLES20.glGenBuffers(1, vbos, 0);
        mVboId = vbos[0];
        GLES20.glBindBuffer(GLES20.GL_ARRAY_BUFFER, mVboId);
        GLES20.glBufferData(GLES20.GL_ARRAY_BUFFER, mVertexData.length * 4 + mFragmentData.length * 4, null, GLES20.GL_STATIC_DRAW);
        GLES20.glBufferSubData(GLES20.GL_ARRAY_BUFFER, 0, mVertexData.length * 4, mVertexBuffer);
        GLES20.glBufferSubData(GLES20.GL_ARRAY_BUFFER, mVertexData.length * 4, mFragmentData.length * 4, mFragmentBuffer);
        GLES20.glBindBuffer(GLES20.GL_ARRAY_BUFFER, 0);

        //FBO 模块
        int[] fbos = new int[1];
        GLES20.glGenBuffers(1, fbos, 0);
        mFboId = fbos[0];
        GLES20.glBindFramebuffer(GLES20.GL_FRAMEBUFFER, mFboId);  //绑定

        int[] textureIds = new int[1];
        GLES20.glGenTextures(1, textureIds, 0);
        mTextureId = textureIds[0];
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, mTextureId); //绑定
        GLES20.glActiveTexture(GLES20.GL_TEXTURE0);
        GLES20.glUniform1f(mSampler, 0);

        GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_WRAP_S, GLES20.GL_REPEAT);
        GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_WRAP_T, GLES20.GL_REPEAT);

        GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MIN_FILTER, GLES20.GL_LINEAR);
        GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MAG_FILTER, GLES20.GL_LINEAR);

        GLES20.glTexImage2D(GLES20.GL_TEXTURE_2D, 0, GLES20.GL_RGBA, 720, 1379, 0, GLES20.GL_RGBA, GLES20.GL_UNSIGNED_BYTE, null);
        GLES20.glFramebufferTexture2D(GLES20.GL_FRAMEBUFFER, GLES20.GL_COLOR_ATTACHMENT0, GLES20.GL_TEXTURE_2D, mTextureId, 0);
        if (GLES20.glCheckFramebufferStatus(GLES20.GL_FRAMEBUFFER) != GLES20.GL_FRAMEBUFFER_COMPLETE) {
            Log.e(TAG, "FBO error");
        } else {
            Log.e(TAG, "FBO success");
        }

        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, 0);  //解绑
        GLES20.glBindFramebuffer(GLES20.GL_FRAMEBUFFER, 0); //解绑FBO

        mImageTextureId = loadTexture(R.drawable.output);
    }

    private int loadTexture(int src) {
        int[] textureIds = new int[1];
        GLES20.glGenTextures(1, textureIds, 0);
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, textureIds[0]); //绑定
        GLES20.glActiveTexture(GLES20.GL_TEXTURE0);
        GLES20.glUniform1f(mSampler, 0);

        GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_WRAP_S, GLES20.GL_REPEAT);
        GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_WRAP_T, GLES20.GL_REPEAT);

        GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MIN_FILTER, GLES20.GL_LINEAR);
        GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MAG_FILTER, GLES20.GL_LINEAR);

        Bitmap bitmap = BitmapFactory.decodeResource(mContext.getResources(), src);
        GLUtils.texImage2D(GLES20.GL_TEXTURE_2D, 0, bitmap, 0);
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, 0);
        return textureIds[0];
    }

    @Override
    public void onSurfaceChanged(int width, int height) {
        Log.w(TAG, "width="+width+", height="+height);
        GLES20.glViewport(0, 0, width, height);
        mFboRender.onChange(width, height);

        //局针运算
        if (width > height) {
            Matrix.orthoM(mMatrix, 0, -width / (height * 1.0f / PICTURE_HEIGHT * PICTURE_WIDTH), width / (height * 1.0f / PICTURE_HEIGHT * PICTURE_WIDTH), -1f, 1f, -1f, 1f);
        } else {
            Matrix.orthoM(mMatrix, 0, -1f, 1f,  -height / (width * 1.0f / PICTURE_WIDTH * PICTURE_HEIGHT), height / (width * 1.0f / PICTURE_WIDTH * PICTURE_HEIGHT), -1f, 1f);
        }

        //上下翻转
        Matrix.rotateM(mMatrix, 0, 180, 1, 0, 0);
    }

    @Override
    public void onDrawFrame() {

        GLES20.glBindFramebuffer(GLES20.GL_FRAMEBUFFER, mFboId);
        GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT);
        GLES20.glClearColor(1.0f, 0f, 0f, 1.0f);
        GLES20.glUseProgram(mProgram);
        GLES20.glUniformMatrix4fv(mMatrixId, 1, false, mMatrix, 0);

        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, mImageTextureId);

        GLES20.glBindBuffer(GLES20.GL_ARRAY_BUFFER, mVboId);


        GLES20.glEnableVertexAttribArray(mVPosition);
        GLES20.glVertexAttribPointer(mVPosition, 2, GLES20.GL_FLOAT, false, 8, 0);

        GLES20.glEnableVertexAttribArray(mFPosition);
        GLES20.glVertexAttribPointer(mFPosition, 2, GLES20.GL_FLOAT, false, 8, mVertexData.length * 4);

        GLES20.glDrawArrays(GLES20.GL_TRIANGLE_STRIP, 0, 4);

        GLES20.glBindBuffer(GLES20.GL_ARRAY_BUFFER, 0);
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, 0);
        GLES20.glBindFramebuffer(GLES20.GL_FRAMEBUFFER, 0);
        mFboRender.onDraw(mTextureId);
    }
}
