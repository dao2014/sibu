package com.socialbusiness.dev.orangebusiness.util;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class KWTimer {
	
	private int mStartSec;
	private TimerListener mListener;
	private KWTimerThread mTimerThread;
	private ExecutorService mTimerThreadPool;
	
	public KWTimer(int startSec) {
		mStartSec = startSec;
	}
	
	public void setTimerListener(TimerListener l) {
		mListener = l;
	}
	
	public void start() {
		if(mTimerThread != null) {
			mTimerThread.die();
		}
		if(mTimerThreadPool == null || mTimerThreadPool.isTerminated()) {
			mTimerThreadPool = Executors.newSingleThreadExecutor();
		}
		mTimerThread = new KWTimerThread(mStartSec);
		mTimerThread.setTimerListener(mListener);
		mTimerThreadPool.execute(mTimerThread);
	}
	
	public void stop() {
		if(mTimerThread != null) {
			mTimerThread.die();
		}
		if(mListener != null) {
			mListener.onStop();
		}
	}
	
	public void destroy() {
		if(mTimerThread != null) {
			mTimerThread.die();
			mTimerThread = null;
		}
		if(mTimerThreadPool != null) {
			mTimerThreadPool.shutdown();
			mTimerThreadPool = null;
		}
		mListener = null;
	}
	
	public static interface TimerListener {
		void onRunning(int currentSeconds);
		void onStop();
	}
	
	private static class KWTimerThread extends Thread {
		
		private boolean isRunning;
		private int mRemainSec;
		private TimerListener mListener;
		
		public KWTimerThread(int startSec) {
			mRemainSec = startSec;
		}
		
		public void setTimerListener(TimerListener l) {
			mListener = l;
		}

		@Override
		public void run() {
			isRunning = true;
			while(isRunning && mRemainSec > 1) {
				try {
					mRemainSec--;
					if(mListener != null) {
						mListener.onRunning(mRemainSec);
					}
					Thread.sleep(1000);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
			if(mListener != null) {
				mListener.onStop();
			}
			mListener = null;
		}

		public void die() {
			isRunning = false;
			mListener = null;
		}
	}
}


