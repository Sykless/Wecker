package com.fpalud.wecker;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.support.v4.content.ContextCompat;
import android.view.View;

public class Circle extends View {

    private static final int START_ANGLE_POINT = 270;

    private final Paint paint;
    private final RectF rect;

    private float angle;

    public Circle(Context context, int size, boolean isLittle)
    {
        super(context);

        final int strokeWidth = isLittle ? 10 : 16;

        System.out.println("Draw circle");

        paint = new Paint();
        paint.setAntiAlias(true);
        paint.setStyle(Paint.Style.STROKE);
        paint.setStrokeWidth(strokeWidth);
        paint.setColor(ContextCompat.getColor(context, R.color.blueBackground));

        int realSize = size - strokeWidth/2;
        rect = new RectF(strokeWidth/2 - 1, strokeWidth/2 - 1, realSize + 1, realSize + 1);
        angle = 0;
    }

    public Circle(Context context, int size, boolean isLittle, int newAngle)
    {
        super(context);

        final int strokeWidth = isLittle ? 8 : 14;

        System.out.println("Draw circle");

        paint = new Paint();
        paint.setAntiAlias(true);
        paint.setStyle(Paint.Style.STROKE);
        paint.setStrokeWidth(strokeWidth);
        paint.setColor(Color.WHITE);

        rect = new RectF(strokeWidth/2, strokeWidth/2, size - strokeWidth/2, size - strokeWidth/2);
        angle = newAngle;
    }

    @Override
    protected void onDraw(Canvas canvas)
    {
        super.onDraw(canvas);
        canvas.drawArc(rect, START_ANGLE_POINT, angle, false, paint);
    }

    public float getAngle() {
        return angle;
    }

    public void setAngle(float angle) {
        this.angle = angle;
    }
}