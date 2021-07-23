package com.jeffmony.opengldemo.render;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.opengl.GLES20;
import android.opengl.GLUtils;

import com.jeffmony.opengldemo.R;
import com.jeffmony.opengldemo.utils.JeffShaderUtils;
import com.jeffmony.opengldemo.view.JeffSurfaceView;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;

public class CustomTextureRender implements JeffSurfaceView.JeffRender {

    private static final String TAG = "CustomTextureRender";

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

    public CustomTextureRender(Context context) {
        mContext = context;

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
        String vertexSource = JeffShaderUtils.getRawResource(mContext, R.raw.vertex_shader);
        String fragmentSource = JeffShaderUtils.getRawResource(mContext, R.raw.fragment_shader);
        mProgram = JeffShaderUtils.createProgram(vertexSource, fragmentSource);
        mVPosition = GLES20.glGetAttribLocation(mProgram, "v_Position");
        mFPosition = GLES20.glGetAttribLocation(mProgram, "f_Position");
        mSampler = GLES20.glGetUniformLocation(mProgram, "sTexture");

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

        Bitmap bitmap = BitmapFactory.decodeResource(mContext.getResources(), R.drawable.output);
        GLUtils.texImage2D(GLES20.GL_TEXTURE_2D, 0, bitmap, 0);
        bitmap.recycle();
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, 0);  //解绑
    }

    @Override
    public void onSurfaceChanged(int width, int height) {
        GLES20.glViewport(0, 0, width, height);
    }

    @Override
    public void onDrawFrame() {
        GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT);
        GLES20.glClearColor(1.0f, 0f, 0f, 1.0f);
        GLES20.glUseProgram(mProgram);

        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, mTextureId);

        GLES20.glBindBuffer(GLES20.GL_ARRAY_BUFFER, mVboId);


        GLES20.glEnableVertexAttribArray(mVPosition);
        GLES20.glVertexAttribPointer(mVPosition, 2, GLES20.GL_FLOAT, false, 8, 0);

        GLES20.glEnableVertexAttribArray(mFPosition);
        GLES20.glVertexAttribPointer(mFPosition, 2, GLES20.GL_FLOAT, false, 8, mVertexData.length * 4);

        GLES20.glDrawArrays(GLES20.GL_TRIANGLE_STRIP, 0, 4);

        GLES20.glBindBuffer(GLES20.GL_ARRAY_BUFFER, 0);
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, 0);
    }
}
