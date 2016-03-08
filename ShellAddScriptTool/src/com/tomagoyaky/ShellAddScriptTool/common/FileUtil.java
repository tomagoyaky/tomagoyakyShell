package com.tomagoyaky.ShellAddScriptTool.common;

import java.io.File;
import java.util.ArrayList;

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

	public static void getFileList(File dir, ArrayList<String> fileList) throws Exception {
		File[] fs = dir.listFiles();
		if(fs != null){
			for (int i = 0; i < fs.length; i++) {
				if (fs[i].isDirectory()) {
					try {
						getFileList(fs[i], fileList);
					} catch (Exception e) {
						e.printStackTrace();
						return;
					}
				}else{
					fileList.add(fs[i].getAbsolutePath());
				}
			}
		}
	}
}
