package com.tomagoyaky.shell;

import com.tomagoyaky.common.Constants;
import com.tomagoyaky.common.Logger;
import com.tomagoyaky.common.StackTraceUtil;

import android.app.Application;
import android.content.Context;

public class NativeService extends Application{

	@Override
	public void onCreate() {
		Logger.LOGD(StackTraceUtil.getMethodWithClassName());
		Logger.LOGI("0x04 »Ö¸´Application");
		String appClassName = CoreUtil.getOldApplication(this, Constants.ClassPath.application_meta_data);
		
		
		CoreUtil.ResumeApplicationWithJava(appClassName);
	}
	
	@Override
	protected void attachBaseContext(Context base) {
		Logger.LOGD(StackTraceUtil.getMethodWithClassName());
		super.attachBaseContext(base);
	}

	@Override
	public void onTerminate() {
		Logger.LOGW(StackTraceUtil.getMethodWithClassName());
		super.onTerminate();
	}
}
