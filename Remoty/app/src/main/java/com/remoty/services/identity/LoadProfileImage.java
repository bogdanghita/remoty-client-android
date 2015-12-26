package com.remoty.services.identity;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.widget.ImageView;

import com.remoty.R;

import java.io.InputStream;


/**
 * Background Async task to load user profile picture from url
 */
public class LoadProfileImage extends AsyncTask<String, Void, Bitmap> {

	ImageView bmImage;

	public LoadProfileImage(ImageView bmImage) {
		this.bmImage = bmImage;
	}

	protected Bitmap doInBackground(String... urls) {

		String urldisplay = urls[0];
		Bitmap mIcon11 = null;

		try {
			InputStream in = new java.net.URL(urldisplay).openStream();
			mIcon11 = BitmapFactory.decodeStream(in);
		}
		catch (Exception e) {
			Log.e("Error", e.getMessage());
			e.printStackTrace();
		}
		return mIcon11;
	}

	protected void onPostExecute(Bitmap result) {

		if (result != null) {
			bmImage.setImageBitmap(result);
		}
		else {
			Drawable drawable = ContextCompat.getDrawable(bmImage.getContext(), R.drawable.ic_account_circle_white_48dp);
			bmImage.setImageDrawable(drawable);
		}
	}
}