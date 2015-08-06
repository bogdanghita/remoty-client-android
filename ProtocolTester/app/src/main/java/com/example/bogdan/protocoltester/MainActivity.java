package com.example.bogdan.protocoltester;

import android.app.Activity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.SeekBar;
import android.widget.TextView;


public class MainActivity extends Activity {

	private final static String PROTOCOL_TESTER = "PROTOCOL TESTER";

	private TextView displayValue;
	private final int[] values = {1, 5, 10, 15, 20, 25, 30, 40, 50, 60, 70, 80, 90, 100, 500, 1000, 2000, 3000, 4000, 5000};
	private int currentIndex;
	public static int interval;

	private static Thread netThread;
	private static Runnable actionRunnable;

	public static boolean stopFlag = true;

	// Defining the SeekBar listener
	SeekBar.OnSeekBarChangeListener scrollSensitivitySeekBarListener = new SeekBar.OnSeekBarChangeListener() {

		@Override
		public void onStartTrackingTouch(SeekBar seekBar) {
		}

		@Override
		public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
			currentIndex = seekBar.getProgress();
			interval = values[currentIndex];
			displayValue.setText(interval + " ms");
		}

		@Override
		public void onStopTrackingTouch(SeekBar seekBar) {
			// Setting unsaved progress
			currentIndex = seekBar.getProgress();
			interval = values[currentIndex];
			displayValue.setText(interval + " ms");
		}
	};

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);

		currentIndex = values.length - 1;
		interval = values[currentIndex];

		LinearLayout mainLayout = (LinearLayout) View.inflate(this, R.layout.activity_main, null);
		setContentView(mainLayout);

		displayValue = new TextView(this);
		displayValue.setText(values[currentIndex] + " ms");
		displayValue.setLayoutParams(new LinearLayout.LayoutParams(
				LinearLayout.LayoutParams.WRAP_CONTENT,
				LinearLayout.LayoutParams.WRAP_CONTENT));
		mainLayout.addView(displayValue);

		// Creating seekBar
		SeekBar seekBar = new SeekBar(this);

		// Setting seek bar progress and state
		seekBar.setMax(values.length - 1);
		seekBar.setProgress(currentIndex);

		// Setting seekBar params
		LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
		params.setMargins(10, 0, 10, 0);
		seekBar.setLayoutParams(params);

		// Setting seek bar listener
		seekBar.setOnSeekBarChangeListener(scrollSensitivitySeekBarListener);

		mainLayout.addView(seekBar);

		final Button startButton = new Button(this);
		startButton.setText("Start");
		startButton.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {

				stopFlag = false;

				startButton.setEnabled(false);

				actionRunnable = new TCPRunnableApiTest();
				netThread = new Thread(actionRunnable);
				netThread.start();
			}
		});

		mainLayout.addView(startButton);
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

	public void onDestroy() {
		super.onDestroy();

		stopFlag = true;

		Log.d(PROTOCOL_TESTER, "Waiting for thread to stop...");

		try {
			netThread.join();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}

		Log.d(PROTOCOL_TESTER, "Thread stopped.");
	}
}
