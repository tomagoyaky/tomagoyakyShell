package com.tomagoyaky.shell;

import java.io.File;
import java.io.IOException;
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

import dalvik.system.DexClassLoader;

public class CoreUtil {

	protected static String dexPath;
	protected static String odexPath;
	protected static String libPath;
	public static File odex;
	public static File libs;
	public static File dex;
	
	public final static boolean DEVELOPER_MODE = true;
	public static void ReleaseDexFiles(Context baseContext) throws IOException {
		Logger.LOGD(StackTraceUtil.getMethodWithClassName());

		odex = baseContext.getDir(Constants.payload_odex, 	Context.MODE_PRIVATE);
		libs = baseContext.getDir(Constants.payload_lib, 	Context.MODE_PRIVATE);
		dex  = baseContext.getDir(Constants.payload_dex, 	Context.MODE_PRIVATE);
		
		dexPath 	= dex.getAbsolutePath() + "/" + Constants.dexFileName;
		odexPath 	= odex.getAbsolutePath();
		libPath 	= libs.getAbsolutePath();

		releaseAssetsFile(baseContext, Constants.dexFileName, dex.getAbsolutePath());
		releaseAssetsDirtory(baseContext, "lib/armeabi", libPath);
	}

	public static void StartNativeService(){
		ResumeApplicationWithJava("com.tomagoyaky.shell.NativeService");
	}
	
	public static void DexClassLoaderWithJava(Context baseContext) throws ClassNotFoundException {
		Logger.LOGD(StackTraceUtil.getMethodWithClassName());

		Object currentActivityThread = RefInvokeUtil.invokeStaticMethod(Constants.ClassPath.android_app_ActivityThread, "currentActivityThread", new Class[] {}, new Object[] {});
		String packageName = baseContext.getPackageName();
		@SuppressWarnings("unchecked")
		ArrayMap<String, WeakReference<?>> mPackages = (ArrayMap<String, WeakReference<?>>) 
				RefInvokeUtil.getFieldObject(Constants.ClassPath.android_app_ActivityThread, currentActivityThread, "mPackages");
		WeakReference<?> wr = (WeakReference<?>) mPackages.get(packageName);
		FatherDexClassLoader dexLoader = new FatherDexClassLoader(
				dexPath + File.pathSeparator + dex.getAbsolutePath(),  // ʹ��File.pathSeparator�ָ���Զ�����·��
				odexPath,
				libPath, 
				(ClassLoader) RefInvokeUtil.getFieldObject(Constants.ClassPath.android_app_LoadedApk, wr.get(), "mClassLoader")
		);
		Logger.LOGI("dexPath:" 	+ dexPath);
		Logger.LOGI("odexPath:" + odexPath);
		Logger.LOGI("libPath:" 	+ libPath);
		
		Logger.LOGI("����mClassLoader");
		RefInvokeUtil.setFieldObject(Constants.ClassPath.android_app_LoadedApk, "mClassLoader", wr.get(), dexLoader);
		
 		// ���ø����еķ���
		try {
			@SuppressWarnings("unused")
			Class<?> appClass = dexLoader.loadClass("android.util.Log");
		} catch (Exception e) {
			Logger.LOGE("error:" + e.getMessage());
		}
	}

	/**
	 * �ָ���ԭ��:
	 * �����ǰѿ��е��������꣬����Ҫ�������������滻Ϊԭ��apk��Context.
	 * ԭ��apk��Context���ڻ�û�д�����Context�Ĵ���ʱ�����Բο����������
	 * xref:http://www.codeceo.com/article/android-context.html
	 * �����������ڣ�Application�ȱ�����,���������Ǿ͵õ���ԭ����Context
	 * */
	@SuppressWarnings("unchecked")
	public static void ResumeApplicationWithJava(String appClassName) {
		Logger.LOGD(StackTraceUtil.getMethodWithClassName());

		Logger.LOGI("��AndroidManifest.xml�е�aplication�ڵ��ж�ȡmeta-data");
		Logger.LOGI("appClassName:[" + appClassName + "]");
		if(appClassName != null){

			Object currentActivityThread = RefInvokeUtil.invokeStaticMethod(Constants.ClassPath.android_app_ActivityThread, "currentActivityThread", new Class[]{}, new Object[]{});
			Object mBoundApplication = RefInvokeUtil.getFieldObject(Constants.ClassPath.android_app_ActivityThread, currentActivityThread, "mBoundApplication");
			Object loadedApkInfo = RefInvokeUtil.getFieldObject("android.app.ActivityThread$AppBindData", mBoundApplication, "info");			
			
			Logger.LOGI("�ѵ�ǰ���̵�mApplication ���ó���null");
			RefInvokeUtil.setFieldObject(Constants.ClassPath.android_app_LoadedApk, "mApplication", loadedApkInfo, null); //ProxyApplication
			Application oldApplication = (Application)RefInvokeUtil.getFieldObject(Constants.ClassPath.android_app_ActivityThread, currentActivityThread, "mInitialApplication"); // ProxyApplication
			
			Logger.LOGI("ɾ��oldApplication");
			ArrayList<Application> mAllApplications = (ArrayList<Application>)RefInvokeUtil.getFieldObject(
					Constants.ClassPath.android_app_ActivityThread,currentActivityThread, "mAllApplications");
			mAllApplications.remove(oldApplication);

			Logger.LOGI("Ϊԭ����Application����");
			ApplicationInfo appinfo_In_LoadedApk = (ApplicationInfo) RefInvokeUtil.getFieldObject(Constants.ClassPath.android_app_LoadedApk, loadedApkInfo, "mApplicationInfo");
			ApplicationInfo appinfo_In_AppBindData = (ApplicationInfo) RefInvokeUtil.getFieldObject("android.app.ActivityThread$AppBindData", mBoundApplication, "appInfo");
			appinfo_In_LoadedApk.className = appClassName;
			appinfo_In_AppBindData.className = appClassName;
			
			Logger.LOGI("ִ�� makeApplication(false,null)");
			Application newApplication = (Application) RefInvokeUtil.invokeMethod(Constants.ClassPath.android_app_LoadedApk, "makeApplication", loadedApkInfo,
				new Class[] { boolean.class, Instrumentation.class },
				new Object[] { false, null });
			if(newApplication == null){
				Logger.LOGE("makeApplication() error.");
				return;
			}
			RefInvokeUtil.setFieldObject("android.app.ActivityThread", "mInitialApplication", currentActivityThread, newApplication);

			Logger.LOGI("����������");
			ArrayMap<Object, Object> mProviderMap = (ArrayMap<Object, Object>) RefInvokeUtil.getFieldObject(Constants.ClassPath.android_app_ActivityThread, currentActivityThread, "mProviderMap");
			Iterator<Object> it = mProviderMap.values().iterator();
			while (it.hasNext()){
				Object providerClientRecord = it.next();
				Object localProvider = RefInvokeUtil.getFieldObject("android.app.ActivityThread$ProviderClientRecord", providerClientRecord, "mLocalProvider");
				RefInvokeUtil.setFieldObject("android.content.ContentProvider", "mContext", localProvider, newApplication);
			}
			Logger.LOGI("����Application�е�onCreate()");
			newApplication.onCreate();
		}else{
			Logger.LOGE("getOldApplication() return NULL.");
		}
	}

	public static void releaseAssetsFile(Context baseContext, String fileName, String dstDir){
		Logger.LOGD(StackTraceUtil.getMethodWithClassName());

		String FilePath = dstDir + File.separator + fileName;
		File targetFile = new File(FilePath);
		if(!targetFile.exists()){
			Logger.LOGW("release file from apk's assets:" + fileName);
		}else{
			targetFile.delete();
		}
		if(!targetFile.getParentFile().exists())
			targetFile.getParentFile().mkdirs();
		AssetsUtil.CopyAssertJarToFile(baseContext, fileName, targetFile);
		if(targetFile.exists())
			Logger.LOGI("filePath:" + FilePath);
		else
			Logger.LOGE("Not exist, filePath:" + FilePath);
	}
	
	/**
	 * �ͷ�assetsĿ¼�µ��ļ��е�Ŀ��Ŀ¼
	 * baseContext, "assets/lib", libPath
	 * */
	public static void releaseAssetsDirtory(Context baseContext, String source, String target){
		String[] files;
        try {
            // ���Assetsһ���м����ļ�
            files = baseContext.getResources().getAssets().list(source);
            for (int i = 0; i < files.length; i++) {
            	releaseAssetsFile(baseContext, source + "/" + files[i], target); // ����ķָ��ΪassetsĿ¼���
			}
        } catch (IOException e1) {
            e1.printStackTrace();
        }
	}

	@SuppressWarnings("unused")
	public static String getOldApplication(Context context, String keyName){
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
