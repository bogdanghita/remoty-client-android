package com.remoty.services;

import android.util.Log;

/**
 * Created by Bogdan on 8/22/2015.
 */
public class MyTimer {

    public final static String TAG_TIMER = "TIMER";

    private TimerThread mThread;

    public void setInterval(long interval) {

        if (mThread != null) {
            mThread.setInterval(interval);
        }
    }

    /**
     * Starts the execution of a task at the given interval. The interval can be modified using the setInterval(long) method
     *
     * @param runnable - the task that will be executed at the given interval.
     * @param interval - the interval at which the task is executed.
     * @throws IllegalArgumentException    - if interval is less than or equal to zero.
     * @throws IllegalThreadStateException - if the mThread was already started.
     */
    public void start(Runnable runnable, long interval) {

        if (mThread != null) {
            throw new IllegalThreadStateException();
        }

        if (interval <= 0) {
            throw new IllegalArgumentException();
        }

        mThread = new TimerThread(runnable, interval);

        mThread.start();
    }

    /**
     * @throws IllegalThreadStateException - if the mThread is not started or was already stopped
     */
    public void stop() {

        if (mThread == null) {
            throw new IllegalThreadStateException();
        }

        mThread.quit();

        Log.d(MyTimer.TAG_TIMER, "mThread quit. Waiting for mThread to join.");

        try {
            mThread.join();
        } catch (InterruptedException e) {
            e.printStackTrace();
            Log.d(MyTimer.TAG_TIMER, e.getClass().getName() + " | " + "exception on join()");
        }

        Log.d(MyTimer.TAG_TIMER, "mThread finished.");

        mThread = null;
    }
}
