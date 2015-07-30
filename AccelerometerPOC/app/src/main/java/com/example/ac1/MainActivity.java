package com.example.ac1;

import android.app.Activity;
import android.content.Context;
import android.graphics.Color;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.view.View;
import android.widget.LinearLayout;

import com.example.transf.ITransfStrategy;
import com.example.transf.BasicPointTransf;

public class MainActivity extends Activity implements SensorEventListener {

	private SensorManager mSensorManager;
	private Sensor mAccelerometerSensor;

	private AccelerometerManager mAccelerometerManager;

	private BallDrawer mBallDrawerView;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		LinearLayout mainLayout = (LinearLayout) View.inflate(this, R.layout.activity_main, null);
		setContentView(mainLayout);

		// Adding ball drawer view
		mBallDrawerView = new BallDrawer(this);
		mBallDrawerView.setBackgroundColor(Color.LTGRAY);

		LinearLayout.LayoutParams viewParams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT,
				LinearLayout.LayoutParams.MATCH_PARENT);

		mainLayout.addView(mBallDrawerView, viewParams);

		// Obtaining accelerometer sensor
		mSensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
		mAccelerometerSensor = mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);

		// Setting transformation strategy
		ITransfStrategy transf = new BasicPointTransf();
//		ITransfStrategy transf = new SteeringWheelTransf();

		// Creating accelerometer manager
		mAccelerometerManager = new AccelerometerManager(mBallDrawerView, transf);
	}

	@Override
	protected void onResume() {
		super.onResume();

		mSensorManager.registerListener(this, mAccelerometerSensor, SensorManager.SENSOR_DELAY_GAME);
//		mSensorManager.registerListener(this, mAccelerometerSensor, 2000 * 1000);
	}

	@Override
	protected void onPause() {
		super.onPause();

		mSensorManager.unregisterListener(this);
	}

	@Override
	public void onAccuracyChanged(Sensor sensor, int accuracy) {

		mAccelerometerManager.accuracyChanged(accuracy);
	}

	@Override
	public void onSensorChanged(SensorEvent event) {

		mAccelerometerManager.triggerEvent(event);
	}
}
