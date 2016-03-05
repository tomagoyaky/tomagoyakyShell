package com.tomagoyaky.ShellAddScriptTool.common;

public class Logger {

	public static String LOGD(String msg){
		System.out.println(msg);
		return msg; 
	}

	public static String LOGW(String msg) {
		System.err.println("[+]" + msg);
		return msg; 
	}
	
	public static String LOGE(String msg) {
		System.err.println("[-]" + msg);
		System.exit(-1);
		return msg; 
	}
	public static Exception LOGE(Exception errobj) {
		if(errobj instanceof Exception){
			LOGE(((Exception) errobj).getMessage());
		}
		return errobj;
	}
}
