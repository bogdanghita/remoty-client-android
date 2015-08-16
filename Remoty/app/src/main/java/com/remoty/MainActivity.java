package com.remoty;

import android.app.FragmentTransaction;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;


public class MainActivity extends AppCompatActivity {

	// TODO: think if we want the action bar in all fragments
	// TODO: also do some research on the action bar and AppCompatActivity

	// TODO: think of a status entry in connect page (so that the user knows its connection state and host if connected)

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);

		// TODO: play with the savedInstanceState

		MainFragment mainFragment = new MainFragment();

		getFragmentManager().beginTransaction().add(R.id.activity_main, mainFragment).commit();
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

		// TODO: see what this comment is saying
		//noinspection SimplifiableIfStatement
//		if (id == R.id.action_settings) {
//			return true;
//		}

		switch (id) {
			case R.id.action_love:
				break;
			case R.id.action_share:
				break;
			case R.id.action_settings:
				break;
		}

		// TODO: see what return value means

		return super.onOptionsItemSelected(item);
	}

	public void buttonDrive(View view) {

		DriveFragment connectFragment = new DriveFragment();

		FragmentTransaction transaction = getFragmentManager().beginTransaction();

		transaction.replace(R.id.activity_main, connectFragment);

		// TODO: solve back button and back stack problems (this does not work)
		transaction.addToBackStack(null);

		transaction.commit();
	}

	public void buttonScore(View view) {

		ScoreFragment connectFragment = new ScoreFragment();

		FragmentTransaction transaction = getFragmentManager().beginTransaction();

		transaction.replace(R.id.activity_main, connectFragment);

		// TODO: solve back button and back stack problems (this does not work)
		transaction.addToBackStack(null);

		transaction.commit();
	}

	public void buttonConnect(View view) {

		ConnectFragment connectFragment = new ConnectFragment();

		FragmentTransaction transaction = getFragmentManager().beginTransaction();

		transaction.replace(R.id.activity_main, connectFragment);

		// TODO: solve back button and back stack problems (this does not work)
		transaction.addToBackStack(null);

		transaction.commit();
	}

	public void buttonSubscribe(View view) {

	}

	public void buttonHelp(View view) {

	}

	public void buttonManualConnection(View view) {

	}

}
