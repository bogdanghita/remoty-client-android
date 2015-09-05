package com.remoty.services.threading;

import android.os.Handler;
import android.os.Message;
import android.util.Log;

import com.remoty.gui.MainActivity;

import java.util.concurrent.atomic.AtomicLong;

/**
 * Created by Bogdan on 8/23/2015.
 */
public class SchedulerHandler extends Handler {

    LooperThread mThread;
    Runnable mRunnable;
    private AtomicLong mInterval = new AtomicLong();

    public SchedulerHandler(LooperThread thread, Runnable runnable, long interval) {

        this.mThread = thread;
        this.mRunnable = runnable;
        this.mInterval.set(interval);
    }

    public void setInterval(long interval) {

        this.mInterval.set(interval);
    }

    @Override
    public void handleMessage(Message msg) {

        if (msg.what == MainActivity.MSG_SCHEDULE) {

            this.sendMessageDelayed(this.obtainMessage(MainActivity.MSG_SCHEDULE), mInterval.longValue());
            Log.d(TaskScheduler.TAG_TIMER, "Sent with delay: " + mInterval);

            mThread.handler.post(mRunnable);

        } else {
            super.handleMessage(msg);
        }
    }
}