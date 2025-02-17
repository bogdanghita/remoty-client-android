package com.remoty.services.threading;

import android.os.Handler;
import android.os.Message;
import android.util.Log;

import com.remoty.common.other.Constants;

import java.util.concurrent.atomic.AtomicLong;


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

		if (msg.what == Constants.MSG_SCHEDULE) {

			this.sendMessageDelayed(this.obtainMessage(Constants.MSG_SCHEDULE), mInterval.longValue());
			Log.d(TaskScheduler.TAG_TIMER, "Sent with delay: " + mInterval);

			mThread.handler.post(mRunnable);

		}
		else {
			super.handleMessage(msg);
		}
	}
}