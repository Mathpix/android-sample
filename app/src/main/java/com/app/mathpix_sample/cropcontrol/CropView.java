package com.app.mathpix_sample.cropcontrol;


import android.content.Context;
import android.util.AttributeSet;
import android.widget.RelativeLayout;

public class CropView extends RelativeLayout {
    public CropView(Context context, AttributeSet attrs) {
        super(context, attrs);
        setWillNotDraw(true);
    }
//
//    @Override
//    protected void onDraw(Canvas canvas) {
//        super.onDraw(canvas);
//
//        CornerView topLeft = (CornerView) findViewById(R.id.top_left_corner);
//        CornerView bottomRight = (CornerView) findViewById(R.id.bottom_right_corner);
//
//        Paint paint = new Paint();
//        paint.setColor(getResources().getColor(R.color.transluscent_white));
//        canvas.drawRect(
//                getCenterX(topLeft),
//                getCenterY(topLeft),
//                getCenterX(bottomRight),
//                getCenterY(bottomRight),
//                paint);
//    }
//
//    int getCenterX(View view){
//        return (view.getLeft() + view.getRight()) / 2;
//    }
//
//    int getCenterY(View view){
//        return (view.getTop() + view.getBottom()) / 2;
//    }
}
