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
        File[] files = dir.listFiles(); // ���ļ�Ŀ¼���ļ�ȫ����������
		if (files != null) {
            for (int i = 0; i < files.length; i++) {
                String fileName = files[i].getName();
                if (files[i].isDirectory()) { // �ж����ļ������ļ���
                    getFileList(files[i].getAbsolutePath()); // ��ȡ�ļ�����·��
                } else if (fileName.endsWith(".java")) { // �ж��ļ����Ƿ���.java��β
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

		// ���ȳ�ʼ��һ���ַ����飬�������ÿ��16�����ַ�
		char[] hexDigits = { '0', '1', '2', '3', '4', '5', '6', '7', '8', '9',
				'A', 'B', 'C', 'D', 'E', 'F' };
		// newһ���ַ����飬�������������ɽ���ַ����ģ�����һ�£�һ��byte�ǰ�λ�����ƣ�Ҳ����2λʮ�������ַ���2��8�η�����16��2�η�����
		char[] resultCharArray = new char[byteArray.length * 2];
		// �����ֽ����飬ͨ��λ���㣨λ����Ч�ʸߣ���ת�����ַ��ŵ��ַ�������ȥ
		int index = 0;
		for (byte b : byteArray) {
			resultCharArray[index++] = hexDigits[b >>> 4 & 0xf];
			resultCharArray[index++] = hexDigits[b & 0xf];
		}
		// �ַ�������ϳ��ַ�������
		return new String(resultCharArray);
	}
	
	// ����Ŀ�е�java�ļ���hash
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
