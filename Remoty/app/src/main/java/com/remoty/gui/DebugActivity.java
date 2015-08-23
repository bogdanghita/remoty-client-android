package com.remoty.gui;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;

/**
 * Created by Bogdan on 8/21/2015.
 */
public class DebugActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Log.d(DebugFragment.LIFECYCLE, this.getClass().getName() + " - " + "onCreate");
    }

    @Override
    public void onStart() {
        super.onStart();

        Log.d(DebugFragment.LIFECYCLE, this.getClass().getName() + " - " + "onStart");
    }

    @Override
    public void onResume() {
        super.onResume();

        Log.d(DebugFragment.LIFECYCLE, this.getClass().getName() + " - " + "onResume");
    }

    @Override
    public void onPause() {
        super.onPause();

        Log.d(DebugFragment.LIFECYCLE, this.getClass().getName() + " - " + "onPause");
    }

    @Override
    public void onStop() {
        super.onStop();

        Log.d(DebugFragment.LIFECYCLE, this.getClass().getName() + " - " + "onStop");
    }

    @Override
    public void onDestroy() {
        super.onDestroy();

        Log.d(DebugFragment.LIFECYCLE, this.getClass().getName() + " - " + "onDestroy");
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();

        Log.d(DebugFragment.LIFECYCLE, this.getClass().getName() + " - " + "onBackPressed");
    }

}
