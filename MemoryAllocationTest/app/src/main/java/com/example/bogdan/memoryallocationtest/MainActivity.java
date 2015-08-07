package com.example.bogdan.memoryallocationtest;

import android.app.Activity;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;

import java.util.Random;


public class MainActivity extends Activity {

	public final static String TIME = "TIME";

	public static final int NB_SAMPLES = 100;

	public static int[] values = {1000000, 500000, 400000, 300000, 200000, 100000, 50000};

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.menu_main, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// Handle action bar item clicks here. The action bar will
		// automatically handle clicks on the Home/Up button, so long
		// as you specify a parent activity in AndroidManifest.xml.
		int id = item.getItemId();

		//noinspection SimplifiableIfStatement
		if (id == R.id.action_settings) {
			return true;
		}

		return super.onOptionsItemSelected(item);
	}

	public void onResume() {
		super.onResume();

		runTests();
	}

	public byte[] alloc(int size) {
		byte[] data = new byte[size];

		return data;
	}

	public void allocationTest(int size, int count) {
		long min = Integer.MAX_VALUE;
		long max = Integer.MIN_VALUE;
		long average;
		long totalTime = 0;

		for (int i = 0; i < count; i++) {
			long start = System.currentTimeMillis();

			byte[] data = alloc(size);

			long end = System.currentTimeMillis();

			long duration = end - start;

			totalTime += duration;
			average = totalTime / (i + 1);

			if (duration < min) {
				min = duration;
			}
			if (duration > max) {
				max = duration;
			}

			Log.d(TIME, "Duarion: " + duration + " | Average: " + average + " , Min: " + min + " , Max: " + max + " | Size: " + data.length / 1000 + " KB");

//			Random rand = new Random();
//
//			for (int j = 0; j < data.length; j++) {
//				data[j] = (byte) rand.nextInt();
//			}

//			int randCnt = 40;
//			for (int j = 0; j < randCnt; j++) {
//				Log.d(TIME, data[rand.nextInt(data.length - 1)] + " ");
//			}
		}
	}

	public void runTests() {
		for (int size : values) {
			Log.d(TIME, "Running test with: " + NB_SAMPLES + " samples | " + size / 1000 + " KB size");

			allocationTest(size, NB_SAMPLES);
		}
	}
}
