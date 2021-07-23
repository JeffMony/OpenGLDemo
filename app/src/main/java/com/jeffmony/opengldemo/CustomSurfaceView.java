package com.jeffmony.opengldemo;

import android.content.Context;
import android.util.AttributeSet;

public class CustomSurfaceView extends JeffSurfaceView {
    public CustomSurfaceView(Context context) {
        this(context, null);
    }

    public CustomSurfaceView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public CustomSurfaceView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        setRender(new CustomRender());
        setRenderMode(JeffSurfaceView.RENDERMODE_WHEN_DIRTY);
    }
}
