package com.remoty;

import android.app.Activity;
import android.app.Fragment;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

/**
 * Created by Bogdan on 8/21/2015.
 */
public class DebugFragment extends Fragment {

    public final static String LIFECYCLE = "LIFECYCLE";

    @Override
    public void onAttach (Activity activity) {
        super.onAttach(activity);

        Log.d(LIFECYCLE, this.getClass().getName() + " - " + "onAttach");
    }

    @Override
    public void onCreate (Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Log.d(LIFECYCLE, this.getClass().getName() + " - " + "onCreate");
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View ret = super.onCreateView(inflater, container, savedInstanceState);

        Log.d(LIFECYCLE, this.getClass().getName() + " - " + "onCreateView");

        return ret;
    }

    @Override
    public void onActivityCreated (Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        Log.d(LIFECYCLE, this.getClass().getName() + " - " + "onActivityCreated");
    }

    @Override
    public void onStart() {
        super.onStart();

        Log.d(LIFECYCLE, this.getClass().getName() + " - " + "onStart");
    }

    @Override
    public void onResume() {
        super.onResume();

        Log.d(LIFECYCLE, this.getClass().getName() + " - " + "onResume");
    }

    @Override
    public void onPause() {
        super.onPause();

        Log.d(LIFECYCLE, this.getClass().getName() + " - " + "onPause");
    }

    @Override
    public void onStop() {
        super.onStop();

        Log.d(LIFECYCLE, this.getClass().getName() + " - " + "onStop");
    }

    @Override
    public void onDestroyView () {
        super.onDestroyView();

        Log.d(LIFECYCLE, this.getClass().getName() + " - " + "onDestroyView");
    }

    @Override
    public void onDestroy() {
        super.onDestroy();

        Log.d(LIFECYCLE, this.getClass().getName() + " - " + "onDestroy");
    }

    @Override
    public void onDetach () {
        super.onDetach();

        Log.d(LIFECYCLE, this.getClass().getName() + " - " + "onDetach");
    }
}
