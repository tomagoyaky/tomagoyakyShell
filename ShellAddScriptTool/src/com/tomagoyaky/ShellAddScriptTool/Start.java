package com.tomagoyaky.ShellAddScriptTool;

import java.io.File;



import java.io.FileInputStream;
import java.io.IOException;
import java.net.URISyntaxException;
import java.security.Key;
import java.security.KeyStore;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

import org.apache.commons.io.FileUtils;

import com.tomagoyaky.AppCompress.Appc;
import com.tomagoyaky.ShellAddScriptTool.common.Constants;
import com.tomagoyaky.ShellAddScriptTool.common.CopyRight;
import com.tomagoyaky.ShellAddScriptTool.common.FileUtil;
import com.tomagoyaky.ShellAddScriptTool.common.Logger;
import com.tomagoyaky.apkeditor.utils.Base64;

public class Start {

	private static void Init() throws Exception {
		Date date = new Date();
		DateFormat format = new SimpleDateFormat("yyyyMMddHHmmss");
		Constants.dir_workplace = Constants.dir_pwd + File.separator + ".." + File.separator + "workplace_" + format.format(date) + "_" +Constants.CopyRightInfo.Hash;

		File workplaceFile = new File(Constants.dir_workplace);
		workplaceFile.mkdirs();
		Logger.LOGD("[mkdir] " + Constants.dir_workplace);
		FileUtil.MakeSureIsExist(Constants.dir_workplace);

//		Constants.ShellTarget.apkFilePath = Constants.dir_pwd + File.separator + "target" + File.separator + "tantan.apk";
		Constants.ShellTarget.apkFilePath = "F:\\Tomagoyaky\\workplace20160002\\ShellTarget\\bin\\ShellTarget.apk";
		Constants.Shell.privateKey = getPrivateKey();
		Constants.Shell.sigPrefix = getSigPrefix();
		Constants.Shell.dir_ShellProject = Constants.dir_pwd + File.separator + ".." + File.separator + "Shell";
		Constants.Shell.file_classes_dex = Constants.Shell.dir_ShellProject + File.separator + "bin" + File.separator + "classes.dex";
		FileUtil.MakeSureIsExist(Constants.Shell.dir_ShellProject);
		FileUtil.MakeSureIsExist(Constants.Shell.file_classes_dex);
	}

	public static void main(String[] args) {
		try {
			CopyRight.show();
			Init();

//			String apkFilePath = args[0]; 
			String apkFilePath = Constants.ShellTarget.apkFilePath;
			if(apkFilePath == Constants.ShellTarget.apkFilePath){
				Logger.LOGW("!!!!!!!!!!!! using default 'apkFilePath' !!!!!!!!!!!!");
				Logger.LOGW("default apkFilePath: " + apkFilePath);
			}
			Apk apk = new Apk(apkFilePath);
			Shell shell = new Shell();
			
			shell.Analysis(apk);			// 判断apk是否可以进行加壳
			shell.ModifyManifest(apk);		// 修改AndroidManifest.xml
			shell.ModifyFileSystem(apk);	// 修改文件系统
			
			// 应用压缩业务
			Appc.Compress(apk);
			
			// 签名生成最终的apk
			if(shell.CreateNewApk(apk)){
				Logger.LOGD("[Signature] create apk:" + apk.getFilePath_sign());
				shell.Running(apk);
			}
			Logger.LOGW("******************* Finish Successfully *******************");
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

    /**
     * 读取 PrivateKey
     * @return 
     * @throws Exception
     */
    public static String getPrivateKey() throws Exception {
        KeyStore keystore = KeyStore.getInstance(KeyStore.getDefaultType());
        FileInputStream fis = FileUtils.openInputStream(new File(Constants.dir_resource + File.separator + Constants.Shell.keystoreFileName));
        keystore.load(fis, Constants.Shell.keystorePassword.toCharArray());
        Key key = keystore.getKey(Constants.Shell.alias, Constants.Shell.keyPassword.toCharArray());
        return new String(Base64.encodeBase64(key.getEncoded()), "UTF-8");
    }
    /**
     * 签名前缀
     * 首先用上面生成的keystore签名任意一个apk，解压出这个apk里面 META-INF/CERT.RSA 的文件
     * @throws IOException
     */
    private static String getSigPrefix() throws IOException, URISyntaxException {
    	File file = new File(Constants.dir_resource + File.separator + Constants.Shell.rsaFileName);
        FileInputStream fis = FileUtils.openInputStream(file);
        /**
         * RSA-keysize signature-length
         # 512         64
         # 1024        128
         # 2048        256
         */

        int same = (int) (file.length() - 64);  //当前-keysize 512

        byte[] buff = new byte[same];
        fis.read(buff, 0, same);
        fis.close();
        return new String(Base64.encodeBase64(buff), "UTF-8");
    }
}
