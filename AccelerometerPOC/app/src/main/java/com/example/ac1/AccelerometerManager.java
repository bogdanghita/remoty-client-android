package com.example.ac1;

import android.hardware.SensorEvent;
import android.util.Log;

import com.example.transf.BasicPointTransf;
import com.example.transf.ITransfStrategy;
import com.example.transf.SteeringWheelTransf;

/**
 * Created by Bogdan on 7/30/2015.
 */
public class AccelerometerManager {

	public final static String ACC_SENSOR = "ACC_SENSOR";
	public final static String ACC_TRANSFORMED = "ACC_TRANSFORMED";

	private ITransfStrategy transf;

	private IAccelerometerListener listener;

	private long lastSensorEventTime;

	public AccelerometerManager(IAccelerometerListener listener, ITransfStrategy transf) {

		this.listener = listener;
		this.transf = transf;

		lastSensorEventTime = System.currentTimeMillis();
	}

	public void accuracyChanged(int accuracy) {

		// TODO: ...
	}

	public void triggerEvent(SensorEvent event) {

		// Obtaining values
		Point p = new Point(event.values[0], event.values[1], event.values[2]);

		// Transforming values
		transf.transform(p);

		// Triggering event
		listener.notify(p);

		// Logging and debugging
		long newSensorEventTime = System.currentTimeMillis();
		long duration = newSensorEventTime - lastSensorEventTime;
		lastSensorEventTime = newSensorEventTime;
		Log.d(ACC_SENSOR, "Accelerometer: x: " + event.values[0] + " y: " + event.values[1] + " z: " + event.values[2] + " duration: " + duration + " ms");
		Log.d(ACC_TRANSFORMED, "Accelerometer: x: " + p.x + " y: " + p.y + " z: " + p.z);
	}
}
