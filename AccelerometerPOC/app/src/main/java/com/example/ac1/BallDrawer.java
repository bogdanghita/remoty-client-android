package com.example.ac1;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.util.Log;
import android.view.View;

public class BallDrawer extends View implements IAccelerometerListener {

	public final static String BALL = "BALL";

	Paint paint;

	float mCurrentX, mCurrentY, mCurrentZ;

	public BallDrawer(Context context) {
		super(context);

		paint = new Paint();
		paint.setStyle(Paint.Style.FILL);
		paint.setColor(Color.RED);

		mCurrentX = 0;
		mCurrentY = 0;
		mCurrentZ = 0;
	}

	@Override
	protected void onDraw(Canvas canvas) {

		canvas.drawCircle(getWidth() / 2 - mCurrentX, getHeight() / 2 + mCurrentY, 50 + mCurrentZ, paint);

		// Logging and debugging
		Log.d(BALL, "Drawing with modifiers: x: " + mCurrentX + " y: " + mCurrentY + " z: " + mCurrentZ);
	}

	@Override
	public void notify(Point p) {

		updateBall(p);
	}

	public void updateBall(Point p) {

		mCurrentX = p.x;
		mCurrentY = p.y;
		mCurrentZ = p.z;

		invalidate();
	}
}
