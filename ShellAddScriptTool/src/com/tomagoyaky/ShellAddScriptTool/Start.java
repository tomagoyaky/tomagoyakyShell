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

public class Start{

	public static class Debuggable{

		public static final boolean CMD_ENABLE = true;
		public static final boolean FUNCTION_TRACE = true;
		public static final boolean APK_INFO = true;
		public static final boolean ZIP_OPT_INFO = true; // 压缩操作
		
	};
	public static int Type_Default = 0; 
	public static int Type_Dynamic_Loading = 1;  // 进行dex的动态加载
	public static int Type_Modify_Classes = 2;   // 进行dex畸形加密 (揽括了前一步的所有操作)
	
	private static void Init() throws Exception {
		Date date = new Date();
		DateFormat format = new SimpleDateFormat("yyyyMMddHHmmss");
		Constants.dir_workplace = Constants.dir_pwd + File.separator + ".." + File.separator + "workplace_" + format.format(date) + "_" +Constants.CopyRightInfo.Hash;

		File workplaceFile = new File(Constants.dir_workplace);
		workplaceFile.mkdirs();
		Logger.LOGD("[mkdir] " + Constants.dir_workplace);
		FileUtil.MakeSureIsExist(Constants.dir_workplace);

		/*
		 * 错误:had used a different Lorg/osgi/framework/BundleActivator; during pre-verification
		 * 解决方式：文件只能引用不能编译到dex文件中,否则会出现类冲突的情况
		 * */
//		Constants.ShellTarget.apkFilePath = Constants.dir_pwd + File.separator + "target" + File.separator + "DemoActivity.apk"; // java.lang.NullPointerException at com.tomagoyaky.apkeditor.axmleditor.decode.StringBlock.prepare(StringBlock.java:212)
//		Constants.ShellTarget.apkFilePath = Constants.dir_pwd + File.separator + "target" + File.separator + "LibrarySecurity.apk";  //  Failure [INSTALL_PARSE_FAILED_MANIFEST_MALFORMED]
//		Constants.ShellTarget.apkFilePath = Constants.dir_pwd + File.separator + "target" + File.separator + "385bd20b5a5d6f8f697e2f294cb77f8d.apk"; // StringBlock.prepare error
//		Constants.ShellTarget.apkFilePath = Constants.dir_pwd + File.separator + "target" + File.separator + "e39b4212f3e8ef39acb0a709c5ad76d7.apk";  
//		Constants.ShellTarget.apkFilePath = Constants.dir_pwd + File.separator + "target" + File.separator + "com.yingyonghui.market_2904_30052763.apk";
//		Constants.ShellTarget.apkFilePath = Constants.dir_pwd + File.separator + "target" + File.separator + "com.sogou.health.apk";  //  Class ref in pre-verified class resolved to unexpected implementation 原因是Shell工程引用了Android4.2这个引用库
//		Constants.ShellTarget.apkFilePath = Constants.dir_pwd + File.separator + "target" + File.separator + "com.stealthcotper.networktools.apk";
//		Constants.ShellTarget.apkFilePath = Constants.dir_pwd + File.separator + "target" + File.separator + "tantan.apk";  // so files load error
		Constants.ShellTarget.apkFilePath = Constants.dir_pwd + File.separator + ".." + File.separator + "ShellTarget" + File.separator + "bin" + File.separator + "ShellTarget.apk";
		FileUtil.MakeSureIsExist(Constants.ShellTarget.apkFilePath);
		
		Constants.Shell.privateKey = getPrivateKey();
		Constants.Shell.sigPrefix = getSigPrefix();
		Constants.Shell.dir_ShellProject = Constants.dir_pwd + File.separator + ".." + File.separator + "Shell";
		Constants.Shell.file_local_classes_dex = Constants.Shell.dir_ShellProject + File.separator + "bin" + File.separator + "classes.dex";
		Constants.Shell.file_local_modify_classes_dex =  Constants.dir_workplace + File.separator + "modify_classes.dex";
		Constants.file_ChecksumDataConfigFile = Constants.dir_pwd + File.separator + "config" + File.separator + "checksum.cfg"; // config
		Constants.file_ChecksumDataSaveFile = Constants.dir_pwd + File.separator + "config" + File.separator + "checksum.rst";  //result
		FileUtil.MakeSureIsExist(Constants.Shell.dir_ShellProject);
		FileUtil.MakeSureIsExist(Constants.Shell.file_local_classes_dex);
	}

	public static void main(String[] args) {
		
		try {
			Start.Type_Default = Start.Type_Modify_Classes;  // 控制程序执行的功能.
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
			if(shell.PrepareDexFile(apk)){
				shell.ModifyDexFile(apk);		// 修改dex文件
//				shell.GetChecksumData(apk);		// 从dex文件中筛选出一些类来做校验,并保存在本地
				shell.GetDexHashCode(apk);
			}
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
        int same = (int) (file.length() - 64);  //当前-keysize 512

        byte[] buff = new byte[same];
        fis.read(buff, 0, same);
        fis.close();
        return new String(Base64.encodeBase64(buff), "UTF-8");
    }
}
