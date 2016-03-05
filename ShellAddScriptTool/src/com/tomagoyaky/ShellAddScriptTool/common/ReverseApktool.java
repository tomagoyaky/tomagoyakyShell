package com.tomagoyaky.ShellAddScriptTool.common;

import java.io.File;
import java.io.IOException;

import org.apache.commons.io.FileUtils;

public class ReverseApktool {

	public static boolean decompile(String filePath, String dir_decompile) {
		// java -jar %ResourcePath%\apktool.jar d -f %APK_pro% -o %apktoolTempDir%
		return CMD.execute("java -jar \"" + Constants.Shell.jar_apktool + "\" d -f \"" + filePath + "\" -o \"" + dir_decompile + "\"" , 
				Constants.dir_pwd, 0, System.out);
	}

	public static boolean bacompile(String dir_decompile, String filePath) {
		// java -jar %ResourcePath%\apktool.jar b %apktoolTempDir% -o %APK_tmp%
		return CMD.execute("java -jar \"" + Constants.Shell.jar_apktool + "\" b \"" + dir_decompile + "\" -o \"" + filePath + "\"" , 
				Constants.dir_pwd, 0, System.out);
	}

	public static boolean DecodeAndroidManifestXML(
			String fileName_androidManifest_xml,
			String fileName_androidManifest_txt) throws IOException {
		String resultdata = "";
		// java -jar %ResourcePath%\AXMLPrinter2.jar .\UnzipPackage\AndroidManifest.xml > .\DismPackage\AndroidManifest.txt
		boolean flag = (resultdata = CMD.execute("java -jar \"" + Constants.Shell.jar_AXMLPrinter2 + "\" \"" + fileName_androidManifest_xml + "\"" , 
				Constants.dir_pwd, 0)) != null;
		FileUtils.write(new File(fileName_androidManifest_txt), resultdata);
		Logger.LOGD("save: " + fileName_androidManifest_txt);
		return flag;
	}

	
}
