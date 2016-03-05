package com.tomagoyaky.shell;

import java.io.File;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Iterator;

import android.app.Application;
import android.app.Instrumentation;
import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.os.Bundle;
import android.util.ArrayMap;

import com.tomagoyaky.common.AssetsUtil;
import com.tomagoyaky.common.Constants;
import com.tomagoyaky.common.Logger;
import com.tomagoyaky.common.RefInvokeUtil;
import com.tomagoyaky.common.StackTraceUtil;

import dalvik.system.BaseDexClassLoader;

public class ProxyApplication extends Application{

	protected String dexPath;
	protected String odexPath;
	protected String libPath;
	private File odex;
	private File libs;
	private File dex;
	
	@Override
	protected void attachBaseContext(Context baseContext) {
		super.attachBaseContext(baseContext);

		try {
			Logger.LOGD(StackTraceUtil.getMethodWithClassName());
			Logger.LOGI("0x01 释放dex文件");
			ReleaseDexFiles(baseContext);
			
			Logger.LOGI("0x02 加载dex文件");
			DexClassLoaderWithJava(baseContext);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	@Override
	public void onCreate() {
		Logger.LOGI("0x03 恢复Application");
		String appClassName = getOldApplication(this, Constants.ClassPath.application_meta_data);
		ResumeApplicationWithJava(appClassName);
	}
	
	private void ReleaseDexFiles(Context baseContext) {
		Logger.LOGD(StackTraceUtil.getMethodWithClassName());

		odex = baseContext.getDir(Constants.payload_odex, 	MODE_PRIVATE);
		libs = baseContext.getDir(Constants.payload_lib, 	MODE_PRIVATE);
		dex  = baseContext.getDir(Constants.payload_dex, 	MODE_PRIVATE);
		
		this.dexPath 	= dex.getAbsolutePath() + "/" + Constants.dexFileName;
		this.odexPath 	= odex.getAbsolutePath();
		this.libPath 	= libs.getAbsolutePath();
		
		releaseAssetsFile(baseContext, Constants.dexFileName, dex.getAbsolutePath());
//		releaseAssetsFile(baseContext, "libshell.so", 	libs.getAbsolutePath());
	}

	@SuppressWarnings("unchecked")
	private void DexClassLoaderWithJava(Context baseContext) throws ClassNotFoundException {
		Logger.LOGD(StackTraceUtil.getMethodWithClassName());
		BaseDexClassLoader dexLoader = new BaseDexClassLoader(
				this.dexPath, 
				odex,
				this.libPath, 
				baseContext.getClassLoader());
		Logger.LOGI("设置mClassLoader");
		Object currentActivityThread = RefInvokeUtil.invokeStaticMethod(Constants.ClassPath.android_app_ActivityThread, "currentActivityThread", new Class[] {}, new Object[] {});
		String packageName = baseContext.getPackageName();
		ArrayMap<String, WeakReference<?>> mPackages = (ArrayMap<String, WeakReference<?>>) 
				RefInvokeUtil.getFieldObject(Constants.ClassPath.android_app_ActivityThread, currentActivityThread, "mPackages");
		WeakReference<?> wr = (WeakReference<?>) mPackages.get(packageName);
		RefInvokeUtil.setFieldObject(Constants.ClassPath.android_app_LoadedApk, "mClassLoader", wr.get(), dexLoader);
	}

	/**
	 * 恢复的原理:
	 * 当我们把壳中的任务做完，就需要把运行上下文替换为原本apk的Context.
	 * 原本apk的Context现在还没有创建，Context的创建时机可以参考下面的链接
	 * xref:http://www.codeceo.com/article/android-context.html
	 * 根据生命周期，Application先被创建,创建后我们就得到了原本的Context
	 * */
	@SuppressWarnings("unchecked")
	protected void ResumeApplicationWithJava(String appClassName) {
		Logger.LOGD(StackTraceUtil.getMethodWithClassName());

		Logger.LOGI("从AndroidManifest.xml中的aplication节点中读取meta-data");
		Logger.LOGI("appClassName:[" + appClassName + "]");
		if(appClassName != null){

			Object currentActivityThread = RefInvokeUtil.invokeStaticMethod(Constants.ClassPath.android_app_ActivityThread, "currentActivityThread", new Class[]{}, new Object[]{});
			Object mBoundApplication = RefInvokeUtil.getFieldObject(Constants.ClassPath.android_app_ActivityThread, currentActivityThread, "mBoundApplication");
			Object loadedApkInfo = RefInvokeUtil.getFieldObject("android.app.ActivityThread$AppBindData", mBoundApplication, "info");			
			
			Logger.LOGI("把当前进程的mApplication 设置成了null");
			RefInvokeUtil.setFieldObject(Constants.ClassPath.android_app_LoadedApk, "mApplication", loadedApkInfo, null); //ProxyApplication
			Application oldApplication = (Application)RefInvokeUtil.getFieldObject(Constants.ClassPath.android_app_ActivityThread, currentActivityThread, "mInitialApplication"); // ProxyApplication
			
			Logger.LOGI("删除oldApplication");
			ArrayList<Application> mAllApplications = (ArrayList<Application>)RefInvokeUtil.getFieldObject(
					Constants.ClassPath.android_app_ActivityThread,currentActivityThread, "mAllApplications");
			mAllApplications.remove(oldApplication);

			Logger.LOGI("为原来的Application创建");
			ApplicationInfo appinfo_In_LoadedApk = (ApplicationInfo) RefInvokeUtil.getFieldObject(Constants.ClassPath.android_app_LoadedApk, loadedApkInfo, "mApplicationInfo");
			ApplicationInfo appinfo_In_AppBindData = (ApplicationInfo) RefInvokeUtil.getFieldObject("android.app.ActivityThread$AppBindData", mBoundApplication, "appInfo");
			appinfo_In_LoadedApk.className = appClassName;
			appinfo_In_AppBindData.className = appClassName;
			
			Logger.LOGI("执行 makeApplication(false,null)");
			Application newApplication = (Application) RefInvokeUtil.invokeMethod(Constants.ClassPath.android_app_LoadedApk, "makeApplication", loadedApkInfo,
				new Class[] { boolean.class, Instrumentation.class },
				new Object[] { false, null });
			RefInvokeUtil.setFieldObject("android.app.ActivityThread", "mInitialApplication", currentActivityThread, newApplication);

			Logger.LOGI("设置上下文");
			ArrayMap<Object, Object> mProviderMap = (ArrayMap<Object, Object>) RefInvokeUtil.getFieldObject(Constants.ClassPath.android_app_ActivityThread, currentActivityThread, "mProviderMap");
			Iterator<Object> it = mProviderMap.values().iterator();
			while (it.hasNext()){
				Object providerClientRecord = it.next();
				Object localProvider = RefInvokeUtil.getFieldObject("android.app.ActivityThread$ProviderClientRecord", providerClientRecord, "mLocalProvider");
				RefInvokeUtil.setFieldObject("android.content.ContentProvider", "mContext", localProvider, newApplication);
			}
			Logger.LOGI("调用Application中的onCreate()");
			newApplication.onCreate();
		}else{
			Logger.LOGE("getOldApplication() return NULL.");
		}
	}

	private void releaseAssetsFile(Context baseContext, String fileName, String dstDir){
		Logger.LOGD(StackTraceUtil.getMethodWithClassName());

		String FilePath = dstDir + File.separator + fileName;
		File targetFile = new File(FilePath);
		if(!targetFile.exists()){
			Logger.LOGW("release file from apk's assets:" + fileName);
		}else{
			targetFile.delete();
		}
		AssetsUtil.CopyAssertJarToFile(baseContext, fileName, targetFile);
		if(targetFile.exists())
			Logger.LOGI("filePath:" + FilePath);
		else
			Logger.LOGE("Not exist, filePath:" + FilePath);
	}

	private String getOldApplication(Context context, String keyName){
		String value = null;
		ApplicationInfo ai = null;
		try {
			ai = context.getPackageManager().getApplicationInfo(context.getPackageName(),PackageManager.GET_META_DATA);
			Bundle bundle = ai.metaData;
			if (bundle != null && bundle.containsKey(keyName)){
				value = bundle.getString(keyName);
			}else{
				return null;
			}
		} catch (NameNotFoundException e) {
			e.printStackTrace();
		}
		return value;
	}
}