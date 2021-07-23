package com.jeffmony.opengldemo;

import android.content.Context;
import android.opengl.GLES20;
import android.opengl.GLSurfaceView;
import android.util.Log;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

public class TriangleRenderer implements GLSurfaceView.Renderer {

    // Member variables
    private int mProgramObject;
    private int mWidth;
    private int mHeight;
    private FloatBuffer mVertices;
    private static String TAG = "HelloTriangleRenderer";

    private final float[] mVerticesData =
            { 0.0f, 0.5f, 0.0f, -0.5f, -0.5f, 0.0f, 0.5f, -0.5f, 0.0f };

    public TriangleRenderer(Context context) {
        mVertices = ByteBuffer.allocateDirect(mVerticesData.length * 4)
                .order(ByteOrder.nativeOrder()).asFloatBuffer();
        mVertices.put(mVerticesData).position(0);
    }

    // Initialize the shader and program object
    @Override
    public void onSurfaceCreated(GL10 gl, EGLConfig config) {
        String vShaderStr =
                "attribute vec4 vPosition;    \n"
                        + "void main()                  \n"
                        + "{                            \n"
                        + "   gl_Position = vPosition;  \n"
                        + "}                            \n";

        String fShaderStr =
                "precision mediump float;					  \n"
                        + "void main()                                  \n"
                        + "{                                            \n"
                        + "  gl_FragColor = vec4 ( 1.0, 0.0, 0.0, 1.0 );\n"
                        + "}                                            \n";

        int vertexShader;
        int fragmentShader;
        int programObject;
        int[] linked = new int[1];

        // Load the vertex/fragment shaders
        vertexShader = LoadShader(GLES20.GL_VERTEX_SHADER, vShaderStr);
        fragmentShader = LoadShader(GLES20.GL_FRAGMENT_SHADER, fShaderStr);

        // Create the program object
        programObject = GLES20.glCreateProgram();

        if (programObject == 0)
            return;

        GLES20.glAttachShader(programObject, vertexShader);
        GLES20.glAttachShader(programObject, fragmentShader);

        // Bind vPosition to attribute 0
        GLES20.glBindAttribLocation(programObject, 0, "vPosition");

        // Link the program
        GLES20.glLinkProgram(programObject);

        // Check the link status
        GLES20.glGetProgramiv(programObject, GLES20.GL_LINK_STATUS, linked, 0);

        if (linked[0] == 0)
        {
            Log.e(TAG, "Error linking program:");
            Log.e(TAG, GLES20.glGetProgramInfoLog(programObject));
            GLES20.glDeleteProgram(programObject);
            return;
        }

        // Store the program object
        mProgramObject = programObject;

        GLES20.glClearColor(0.0f, 0.0f, 1.0f, 0.0f);
    }

    // Create a shader object, load the shader source, and
    // compile the shader.
    private int LoadShader(int type, String shaderSrc)
    {
        int shader;
        int[] compiled = new int[1];

        // Create the shader object
        shader = GLES20.glCreateShader(type);

        if (shader == 0)
            return 0;

        // Load the shader source
        GLES20.glShaderSource(shader, shaderSrc);

        // Compile the shader
        GLES20.glCompileShader(shader);

        // Check the compile status
        GLES20.glGetShaderiv(shader, GLES20.GL_COMPILE_STATUS, compiled, 0);

        if (compiled[0] == 0)
        {
            Log.e(TAG, GLES20.glGetShaderInfoLog(shader));
            GLES20.glDeleteShader(shader);
            return 0;
        }
        return shader;
    }

    // Handle surface changes
    @Override
    public void onSurfaceChanged(GL10 gl, int width, int height) {
        mWidth = width;
        mHeight = height;
    }


    // Draw a triangle using the shader pair created in onSurfaceCreated()
    @Override
    public void onDrawFrame(GL10 gl) {
        // Set the viewport
        GLES20.glViewport(0, 0, mWidth, mHeight);

        // Clear the color buffer
        GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT);

        // Use the program object
        GLES20.glUseProgram(mProgramObject);

        // Load the vertex data
        GLES20.glVertexAttribPointer(0, 3, GLES20.GL_FLOAT, false, 0, mVertices);
        GLES20.glEnableVertexAttribArray(0);

        GLES20.glDrawArrays(GLES20.GL_TRIANGLES, 0, 3);
    }
}
