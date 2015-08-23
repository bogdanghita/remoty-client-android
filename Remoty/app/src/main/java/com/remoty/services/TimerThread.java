package com.remoty.services;

import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.util.Log;

import java.util.concurrent.atomic.AtomicLong;

/**
 * Created by Bogdan on 8/23/2015.
 */
public class TimerThread extends LooperThread {

    private LooperThread mThread;

    private Runnable mRunnable;
    private AtomicLong mInterval = new AtomicLong();

    public TimerThread(Runnable runnable, long interval) {

        this.mRunnable = runnable;
        this.mInterval.set(interval);
    }

    public void setInterval(long interval) {

        mInterval.set(interval);

        if (handler != null) {
            ((TimerHandler) handler).setInterval(interval);
        }
    }

    @Override
    public void run() {

        Looper.prepare();

        mThread = new LooperThread();
        mThread.start();

        handler = new TimerHandler(mThread, mRunnable, mInterval.longValue());

        // Sleeping until the handler of mThread is created
        while (mThread.handler == null) {
        }

        handler.sendMessage(handler.obtainMessage(TimerHandler.TIMER_MSG));

        Looper.loop();
    }

    @Override
    public void quit() {
        super.quit();

        mThread.quit();

        Log.d(MyTimer.TAG_TIMER, "mThread quit. Waiting for mThread to join.");

        try {
            mThread.join();
        } catch (InterruptedException e) {
            e.printStackTrace();
            Log.d(MyTimer.TAG_TIMER, e.getClass().getName() + " | " + "exception on join()");
        }

        Log.d(MyTimer.TAG_TIMER, "mThread finished.");
    }
}

class TimerHandler extends Handler {

    public final static int TIMER_MSG = 1000;

    LooperThread mThread;
    Runnable mRunnable;
    private AtomicLong mInterval = new AtomicLong();

    public TimerHandler(LooperThread thread, Runnable runnable, long interval) {

        this.mThread = thread;
        this.mRunnable = runnable;
        this.mInterval.set(interval);
    }

    public void setInterval(long interval) {

        this.mInterval.set(interval);
    }

    @Override
    public void handleMessage(Message msg) {

        this.sendMessageDelayed(this.obtainMessage(TIMER_MSG), mInterval.longValue());
        Log.d(MyTimer.TAG_TIMER, "Sent with delay: " + mInterval);

        mThread.handler.post(mRunnable);
    }
}