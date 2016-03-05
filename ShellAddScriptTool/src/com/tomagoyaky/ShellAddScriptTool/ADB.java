package com.tomagoyaky.ShellAddScriptTool;

import com.tomagoyaky.ShellAddScriptTool.common.CMD;
import com.tomagoyaky.ShellAddScriptTool.common.Constants;
import com.tomagoyaky.ShellAddScriptTool.common.Logger;
import com.tomagoyaky.ShellAddScriptTool.common.StackTraceUtil;

public class ADB {

	public static boolean HaveBeenInstalled(String packageName) {
		/**
		 * 解决方案二:读取并解析文件/data/system/packages.xml
		 * */
		Logger.LOGD(StackTraceUtil.getMethodWithClassName());
		String retVal = CMD.execute("adb shell su -c \"ls /data/app/\"", Constants.dir_pwd, 0);
		String[] retValArray = retVal.split("\r\r\n");
		boolean flag = false;
		for (String item : retValArray) {
			if(item.contains(packageName)){
				flag = true;
			}
		}
		return flag;
	}

	public static void WaitForDevice(){
		Logger.LOGD(StackTraceUtil.getMethodWithClassName());
		Logger.LOGW("wait-for-device ...");
		CMD.execute("adb wait-for-device", Constants.dir_pwd, 0, CMD.nullOS);
	}
	
	public static void ReInstall(String filePath_sign) {
		Logger.LOGD(StackTraceUtil.getMethodWithClassName());
		String retVal = CMD.execute("adb install -r \"" + filePath_sign + "\"", Constants.dir_pwd, 0);
		Logger.LOGD(retVal.replace("\r\r\n", "\n"));
	}

	public static void Running(String packageName, String launcherActivity) {
		Logger.LOGD(StackTraceUtil.getMethodWithClassName());
		String retVal = CMD.execute("adb shell su -c \"" + "am start -n " + packageName + "/" + launcherActivity + "\"", Constants.dir_pwd, 0);
		Logger.LOGD(retVal.replace("\r\r\n", "\n"));
	}

	public static void Uninstall(String packageName) {
		Logger.LOGD(StackTraceUtil.getMethodWithClassName());
		String retVal = CMD.execute("adb uninstall \"" + packageName + "\"", Constants.dir_pwd, 0);
		Logger.LOGD(retVal.replace("\r\r\n", "\n"));
	}

	public static void Install(Apk apk) {
		Logger.LOGD(StackTraceUtil.getMethodWithClassName());
		int trycount = 1;
		while(true){
			String retVal = CMD.execute("adb install \"" + apk.getFilePath_sign() + "\"", Constants.dir_pwd, 0);
			if(retVal != null){
				if(retVal.contains("INSTALL_FAILED_UID_CHANGED")){
					/**
					 * 针对此种错误的解决方法
					 * */
					if(trycount == 3){
						Logger.LOGE("Installation operation terminated because of the 'UID_CHANGED', After trying " + trycount + " times ");
						System.exit(-1);
					}
					CMD.execute("adb shell su -c \"" + "rm -rf /data/data/" + apk.getPackageName() + "\"", Constants.dir_pwd, 0, CMD.nullOS);
					Logger.LOGW("'UID_CHANGED':try again (" + trycount + ").");
					trycount++;
				}else if(retVal.contains("Failure") && retVal.toUpperCase().contains("FAILED")){
					Logger.LOGE(retVal.replace("\r\r\n", "\n"));
				}else{
					Logger.LOGD(retVal.replace("\r\r\n", "\n").replace("\r\n", "\n"));
					break;
				}
			}
		}
	}

}
