package com.app.mathpix_sample.cropcontrol;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.view.View;

import com.app.mathpix_sample.R;


public class CornerView extends View {

    Corner corner;

    public CornerView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        setupLines(context, attrs);
    }

    public CornerView(Context context, AttributeSet attrs) {
        super(context, attrs);

        setupLines(context, attrs);
    }

    public int getOffsetX() {
        return (int) (getWidth() * corner.centerOffsetX);
    }

    public int getOffsetY() {
        return (int) (getHeight() * corner.centerOffsetY);
    }

    private void setupLines(Context context, AttributeSet attrs) {
        TypedArray a = context.getTheme().obtainStyledAttributes(
                attrs,
                R.styleable.CornerView,
                0, 0);
        try {
            corner = Corner.fromInt(a.getInt(R.styleable.CornerView_corner, -10));

        } finally {
            a.recycle();
        }
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        drawLine(canvas, corner.horizontalLine);
        drawLine(canvas, corner.verticalLine);
    }

    void drawLine(Canvas canvas, LineDirection line) {
        int lineWidth = 10;
        int lineLength = getWidth();

        int centerXOffset = (int) ((line.x * lineWidth) * -0.5f);
        int centerYOffset = (int) ((line.y * lineWidth) * -0.5f);

        int centerX = (getWidth() / 2) + centerXOffset;
        int centerY = (getHeight() / 2) + centerYOffset;

        int xPoint = centerX + (line.x * lineLength);
        int yPoint = centerY + (line.y * lineLength);

        Paint paint = new Paint();
        paint.setColor(getResources().getColor(R.color.white));
        paint.setStrokeWidth(lineWidth);

        canvas.drawLine(centerX, centerY, xPoint, yPoint, paint);
    }

    enum Corner {
        TOP_LEFT(LineDirection.DOWN, LineDirection.RIGHT, 1f, 2f),
        TOP_RIGHT(LineDirection.DOWN, LineDirection.LEFT, 1f, 2f),
        BOTTOM_LEFT(LineDirection.UP, LineDirection.RIGHT, 1f, 0f),
        BOTTOM_RIGHT(LineDirection.UP, LineDirection.LEFT, 1f, 0f);
        final LineDirection horizontalLine;
        final LineDirection verticalLine;
        final float centerOffsetX;
        final float centerOffsetY;

        Corner(LineDirection horizontalLine, LineDirection verticalLine, float centerOffsetX, float centerOffsetY) {
            this.horizontalLine = horizontalLine;
            this.verticalLine = verticalLine;
            this.centerOffsetX = centerOffsetX;
            this.centerOffsetY = centerOffsetY;
        }

        public static Corner fromInt(int anInt) {
            switch (anInt) {
                case 0:
                    return TOP_LEFT;
                case 1:
                    return TOP_RIGHT;
                case 2:
                    return BOTTOM_LEFT;
                case 3:
                    return BOTTOM_RIGHT;
                default:
                    throw new RuntimeException("Invalid corner " + String.valueOf(anInt));
            }
        }
    }

    enum LineDirection {
        UP(0, -1), DOWN(0, 1), LEFT(-1, 0), RIGHT(1, 0);
        final int x;
        final int y;

        LineDirection(int x, int y) {
            this.x = x;
            this.y = y;
        }
    }
}