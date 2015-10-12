package com.socialbusiness.dev.orangebusiness.util;

import android.os.Handler;
import android.os.Message;

public abstract class KWAsyncTask {

	private static final String TAG="KWAsyncTask";
	
	private static final int WHAT_onPreExecute=10;
	private static final int WHAT_onProgressUpdate=20;
	private static final int WHAT_onPostExecute=60;
	private static final int WHAT_onCancelled=88;
	
	private boolean isCancelled=false;
	private Object[] parameters;
	
	public final void execute(Object ... parameters){
		isCancelled=false;
		this.parameters=parameters;
		handler.obtainMessage(WHAT_onPreExecute).sendToTarget();
	}
	
	public final void start(Object ... parameters){
		isCancelled=false;
		this.parameters=parameters;
		handler.obtainMessage(WHAT_onPreExecute).sendToTarget();
	}
	
	public final void cancel(){
		isCancelled=true;
		handler.obtainMessage(WHAT_onCancelled).sendToTarget();
	}
	
	public final boolean isCancelled(){
		return isCancelled;
	}
	
	public final Object[] getParameters(){
		return parameters;
	}
	
	protected void onPreExecute(){
		
	}
	
	protected abstract Object doInBackground(Object ... parameters);
	
	protected void onProgressUpdate(int value){
		
	}
	protected void onPostExecute(Object result){
		
	}
	protected void onCancelled(){
		
	}
	
	protected final void publishProgress(int value){
		handler.obtainMessage(WHAT_onProgressUpdate,value).sendToTarget();
	}
	
	private Handler handler=new Handler(){
		public void handleMessage(Message msg){
			switch(msg.what){
			case WHAT_onPreExecute:
				onPreExecute();
				new Thread(new DoInBackgroundThread()).start();
				break;
			case WHAT_onProgressUpdate:
				onProgressUpdate(((Integer)msg.obj).intValue());
				break;
			case WHAT_onPostExecute:
				onPostExecute(msg.obj);
				break;
			case WHAT_onCancelled:
				onCancelled();
				break;
			}
		}
	};
	
	private class DoInBackgroundThread implements Runnable{
		public void run(){
			if(isCancelled()){
				return;
			}
			Object result = doInBackground(getParameters());
			if(isCancelled()){
				return;
			}
			handler.obtainMessage(WHAT_onPostExecute, result).sendToTarget();
			parameters = null;
		}
	}
}
