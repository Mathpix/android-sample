package com.app.mathpix_sample.cropcontrol;

import android.graphics.Point;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;

import com.app.mathpix_sample.R;

public class CropController {
    final RelativeLayout cropControl;
    final TouchStateListener listener;
    public CropController(RelativeLayout cropControl, TouchStateListener listener) {
        this.cropControl = cropControl;
        this.listener = listener;

        setupCropControl();
    }

    private void setupCropControl() {
        for (int i = 0; i < cropControl.getChildCount(); i++) {
            View view = cropControl.getChildAt(i);
            if (view instanceof CornerView) {
                view.setOnTouchListener(createCornerTouchListener());
            }
        }
    }

    private View.OnTouchListener createCornerTouchListener() {
        return new View.OnTouchListener() {

            @Override
            public boolean onTouch(View v, MotionEvent event) {
                final CornerView cornerView = (CornerView) v;
                Point touchPoint = new Point((int) event.getRawX(), (int) event.getRawY());
                Point viewCenter = cropViewCenter();

                switch (event.getAction() & MotionEvent.ACTION_MASK) {
                    case MotionEvent.ACTION_DOWN:
                        listener.onDragBegan();
                        break;
                    case MotionEvent.ACTION_UP:
                        listener.onDragEnded();
                        break;
                    case MotionEvent.ACTION_POINTER_DOWN:
                        break;
                    case MotionEvent.ACTION_POINTER_UP:
                        break;
                    case MotionEvent.ACTION_MOVE:
                        int offsetX = cornerView.getOffsetX();
                        int offsetY = cornerView.getOffsetY();
                        //Size is the distance from the center of the cropview, to the touch point
                        int width = 2 * Math.abs(touchPoint.x - viewCenter.x);
                        int height = 2 * Math.abs(touchPoint.y - viewCenter.y);
                        //Enfoce a minimum size for the width
                        int cornerWidth = (int) cropControl.getResources().getDimension(R.dimen.crop_corner_width);
                        width = (int) Math.max(cornerWidth * 1.1, width); //NFI why width needs a lower multiplier.
                        height = (int) Math.max(cornerWidth * 2.1, height);
                        cornerMoved(width + offsetX, height + offsetY);
                        break;
                }
                return true;
            }
        };
    }

    private Point cropViewCenter() {
        int cropCenterX = (cropControl.getLeft() + cropControl.getRight()) / 2;
        int cropCenterY = (cropControl.getTop() + cropControl.getBottom()) / 2;
        return new Point(cropCenterX, cropCenterY);
    }

    private void cornerMoved(int width, int height) {
        ViewGroup.LayoutParams lp = cropControl.getLayoutParams();
        lp.width = width;
        lp.height = height;
        cropControl.setLayoutParams(lp);
    }

    public interface TouchStateListener {
        void onDragBegan();

        void onDragEnded();
    }
}
