package com.tomagoyaky.ShellAddScriptTool.common;

import java.io.File;

public class Constants {

	public static class CopyRightInfo{
		public static String Author = "Author: tomagoyaky";
		public static String Email = " Email: tomagoyaky@gmail.com";
		public static String Hash;
	}
	public static String dir_workplace = "";
	public static final String dir_pwd = System.getProperty("user.dir");
	public static final String dir_Shell_project = dir_pwd + File.separator + "Shell";
	public static final String dir_resource = dir_pwd + File.separator + "resources";
	
	public static class Shell{

		public static String dir_ShellProject = "";
		public static final String rsaFileName = "CERT.RSA";
		public static final String keystoreFileName = "demo.keystore";
		public static final String keystorePassword = "123456";
		public static final String alias = "mytestkey";
		public static final String keyPassword = "123456";
        
		public static String privateKey;
		public static String sigPrefix;
		public static String file_classes_dex = "";
		public static final String jar_apktool = dir_pwd + File.separator + "lib" + File.separator + "AXMLPrinter2.jar";
		public static final String jar_AXMLPrinter2 = dir_pwd + File.separator + "lib" + File.separator + "AXMLPrinter2.jar";
		public static final String ShellApplicationName = "com.tomagoyaky.shell.ProxyApplication";
		
	}

	public static class ShellTarget{
		public static String apkFilePath = "";
	}
}