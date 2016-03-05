package com.tomagoyaky.ShellTarget;

import android.util.Log;
public class Logger {

	public static final String TAG = "tomagoyaky_java";
	public static String LOGD(String msg){
		Log.d(TAG, msg);
		return msg; 
	}
	public static String LOGV(String msg){
		Log.v(TAG, msg);
		return msg; 
	}
	public static String LOGI(String msg){
		Log.i(TAG, msg);
		return msg; 
	}
	public static String LOGW(String msg){
		Log.w(TAG, msg);
		return msg; 
	}
	public static String LOGE(String msg){
		Log.e(TAG, msg);
		return msg; 
	}
}
