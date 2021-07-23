package com.jeffmony.opengldemo;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {

    private Button mEglCreateBtn;
    private Button mTriangleBtn;
    private Button mCustomSurfaceViewBtn;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mEglCreateBtn = findViewById(R.id.eglcreate_btn);
        mTriangleBtn = findViewById(R.id.triangle_btn);
        mCustomSurfaceViewBtn = findViewById(R.id.glsurfaceview_btn);

        mEglCreateBtn.setOnClickListener(this);
        mTriangleBtn.setOnClickListener(this);
        mCustomSurfaceViewBtn.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        if (v == mEglCreateBtn) {
            Intent intent = new Intent(MainActivity.this, EglCreateActivity.class);
            startActivity(intent);
        } else if (v == mTriangleBtn) {
            Intent intent = new Intent(MainActivity.this, TriangleActivity.class);
            startActivity(intent);
        } else if (v == mCustomSurfaceViewBtn) {
            Intent intent = new Intent(MainActivity.this, EglSurfaceViewActivity.class);
            startActivity(intent);
        }
    }
}
