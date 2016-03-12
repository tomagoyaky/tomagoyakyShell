package com.tomagoyaky.shell;

import android.app.Application;
import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.os.StrictMode;

import com.tomagoyaky.common.Logger;
import com.tomagoyaky.common.StackTraceUtil;

public class ProxyApplication extends Application{

	@Override
	protected void attachBaseContext(Context baseContext) {
		Logger.LOGD(StackTraceUtil.getMethodWithClassName());
		super.attachBaseContext(baseContext);

		try {
			Logger.LOGI("0x01 释放dex文件");
			CoreUtil.ReleaseDexFiles(baseContext);

			Logger.LOGI("0x02 加载dex文件");
			CoreUtil.DexClassLoaderWithJava(this);
			
			/**
			 * 使用Native Service的方式在native层加载so
			 * */
			Logger.LOGI("0x03 开启Native Service");
			CoreUtil.StartNativeService();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	@Override
	public void onCreate() {
		Logger.LOGD(StackTraceUtil.getMethodWithClassName());
		// check if android:debuggable is set to true  
		try {
			if ((this.getApplicationInfo().flags & ApplicationInfo.FLAG_DEBUGGABLE) != 0) {  
			    StrictMode.setThreadPolicy(new StrictMode.ThreadPolicy.Builder()  
			        .detectDiskReads()  
			        .detectDiskWrites()  
			        .detectNetwork()  
			        .penaltyLog()
			        .penaltyDialog()
			        .build());  
			    StrictMode.setVmPolicy(new StrictMode.VmPolicy.Builder()  
			        .detectLeakedSqlLiteObjects()  
			        .penaltyLog()
			        .penaltyDeath()
			        .build());  
			}
		} catch (Exception e) {
			Logger.LOGW("StrictMode android 2.3 is not available. error:" + e.getMessage());
		}
	}
	
	@Override
	public void onTerminate() {
		Logger.LOGW(StackTraceUtil.getMethodWithClassName());
		super.onTerminate();
	}
}