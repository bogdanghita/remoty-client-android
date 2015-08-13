package com.example.bogdan.protocoltester;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.view.View;

/**
 * Created by Bogdan on 8/9/2015.
 */
public class ScreenView extends View {

	Bitmap image;

	public ScreenView(Context context) {
		super(context);
	}

	@Override
	protected void onDraw(Canvas canvas) {

		if(image == null) {
			return;
		}

		canvas.drawBitmap(image, 0, 0, null);
	}

	public void updateScreen(Bitmap image) {

		this.image = image;

		invalidate();
	}
}
