package com.tomagoyaky.ShellTarget;
import android.app.Application;
import android.widget.Toast;

public class CustomApplication extends Application{
	
	@Override
	public void onCreate() {
		Toast.makeText(this, "CustomApplication", Toast.LENGTH_LONG).show();
		Logger.LOGD("CustomApplication.onCreate()");
		
		
	}
}
