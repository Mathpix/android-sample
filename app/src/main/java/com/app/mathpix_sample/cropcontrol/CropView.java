package com.app.mathpix_sample.cropcontrol;


import android.content.Context;
import android.util.AttributeSet;
import android.widget.RelativeLayout;

public class CropView extends RelativeLayout {
    public CropView(Context context, AttributeSet attrs) {
        super(context, attrs);
        setWillNotDraw(true);
    }
}
