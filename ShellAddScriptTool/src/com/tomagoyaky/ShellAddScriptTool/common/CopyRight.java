package com.tomagoyaky.ShellAddScriptTool.common;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.security.MessageDigest;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

public class CopyRight {

	public static String getTime() {
		Date date = new Date();
		DateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		return format.format(date);
	}

	public static ArrayList<File> filelist = new ArrayList<File>();
	public static ArrayList<File> getFileList(String strPath) {
        File dir = new File(strPath);
        File[] files = dir.listFiles(); // 该文件目录下文件全部放入数组
		if (files != null) {
            for (int i = 0; i < files.length; i++) {
                String fileName = files[i].getName();
                if (files[i].isDirectory()) { // 判断是文件还是文件夹
                    getFileList(files[i].getAbsolutePath()); // 获取文件绝对路径
                } else if (fileName.endsWith(".java")) { // 判断文件名是否以.java结尾
                    filelist.add(files[i]);
                } else {
                    continue;
                }
            }

        }
        return filelist;
    }
	
	public static String createChecksum(String filename) throws Exception {
        InputStream fis =  new FileInputStream(filename);

        byte[] buffer = new byte[1024];
        MessageDigest messageDigest = MessageDigest.getInstance("MD5");
        int numRead;

        do {
            numRead = fis.read(buffer);
            if (numRead > 0) {
                messageDigest.update(buffer, 0, numRead);
            }
        } while (numRead != -1);

        fis.close();
        return byteArrayToHex(messageDigest.digest());
    }

	public static String byteArrayToHex(byte[] byteArray) {

		// 首先初始化一个字符数组，用来存放每个16进制字符
		char[] hexDigits = { '0', '1', '2', '3', '4', '5', '6', '7', '8', '9',
				'A', 'B', 'C', 'D', 'E', 'F' };
		// new一个字符数组，这个就是用来组成结果字符串的（解释一下：一个byte是八位二进制，也就是2位十六进制字符（2的8次方等于16的2次方））
		char[] resultCharArray = new char[byteArray.length * 2];
		// 遍历字节数组，通过位运算（位运算效率高），转换成字符放到字符数组中去
		int index = 0;
		for (byte b : byteArray) {
			resultCharArray[index++] = hexDigits[b >>> 4 & 0xf];
			resultCharArray[index++] = hexDigits[b & 0xf];
		}
		// 字符数组组合成字符串返回
		return new String(resultCharArray);
	}
	
	// 对项目中的java文件做hash
	public static String getProjectHash(String projectPath) throws Exception{
		ArrayList<File> filelist = getFileList(projectPath + File.separator + "src");
		if(filelist == null){
			return null;
		}
		StringBuilder hashValueBuilder = new StringBuilder();
		for (File file : filelist) {
			try {
				hashValueBuilder.append(createChecksum(file.getAbsolutePath()));
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		byte[] hashValueByte = hashValueBuilder.toString().getBytes();
        MessageDigest messageDigest = MessageDigest.getInstance("MD5");
        messageDigest.update(hashValueByte);
        return byteArrayToHex(messageDigest.digest());
	}

	public static void show() throws Exception{
		Constants.CopyRightInfo.Hash = getProjectHash(Constants.dir_pwd);
		Logger.LOGD("=========================================");
		Logger.LOGD("| " + Constants.CopyRightInfo.Author);
		Logger.LOGD("| " + Constants.CopyRightInfo.Email);
		Logger.LOGD("| " + getTime());
		Logger.LOGD("| " + Constants.CopyRightInfo.Hash);
		Logger.LOGD("=========================================");
	}
}
