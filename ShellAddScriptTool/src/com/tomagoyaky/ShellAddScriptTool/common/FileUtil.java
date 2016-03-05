package com.tomagoyaky.ShellAddScriptTool.common;

import java.io.File;

public class FileUtil {

	public static void MakeSureIsExist(String path){
		File file = new File(path);
		if(!file.exists()){
			Logger.LOGE("error, not exist '" + path
					.replace(Constants.dir_workplace, "{dir_workplace}")
					.replace(Constants.dir_Shell_project, "{dir_Shell_project}")
					.replace(Constants.dir_workplace, "{dir_workplace}")
					.replace(Constants.dir_pwd, "{dir_pwd}") + "'.");
			System.exit(-1);
		}
	}
}
