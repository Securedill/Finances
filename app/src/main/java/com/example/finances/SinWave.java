package com.example.finances;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Path;
import android.util.AttributeSet;
import android.view.View;

public class SinWave extends View {

    private float first_X = 50;
    private float first_Y = 230;
    private float end_X = 100;
    private float end_Y = 230;
    private float Max = 50;

    public SinWave(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        Paint paint = new Paint() {
            {
                setStyle(Paint.Style.STROKE);
                setStrokeCap(Paint.Cap.ROUND);
                setStrokeWidth(0.7f);
                setAntiAlias(true);
                setColor(0xFFFF00FF);
            }
        };
        final Path path = new Path();
        path.moveTo(first_X, first_Y);
        path.quadTo((first_X + end_X)/2, Max, end_X, end_Y);
        canvas.drawPath(path, paint);
    }
}