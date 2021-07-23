package com.jeffmony.opengldemo.view;

import android.content.Context;
import android.util.AttributeSet;

import com.jeffmony.opengldemo.render.CustomTextureRender;

public class CustomTextureView extends JeffSurfaceView {

    public CustomTextureView(Context context) {
        this(context, null);
    }

    public CustomTextureView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public CustomTextureView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        setRender(new CustomTextureRender(context));
    }
}
