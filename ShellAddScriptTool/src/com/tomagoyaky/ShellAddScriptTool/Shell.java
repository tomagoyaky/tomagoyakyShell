package com.tomagoyaky.ShellAddScriptTool;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.util.ArrayList;

import org.apache.commons.io.FileUtils;

import com.tomagoyaky.ShellAddScriptTool.common.Constants;
import com.tomagoyaky.ShellAddScriptTool.common.FileUtil;
import com.tomagoyaky.ShellAddScriptTool.common.Logger;
import com.tomagoyaky.ShellAddScriptTool.common.ReverseApktool;
import com.tomagoyaky.ShellAddScriptTool.common.StackTraceUtil;
import com.tomagoyaky.apkeditor.apksigner.SignApk;
import com.tomagoyaky.apkeditor.apksigner.ZipManager;
import com.tomagoyaky.apkeditor.axmleditor.decode.AXMLDoc;
import com.tomagoyaky.apkeditor.axmleditor.editor.ApplicationInfoEditor;
import com.tomagoyaky.apkeditor.axmleditor.editor.MetaDataEditor;

public class Shell {

	public void Analysis(Apk apk) {
		StackTraceUtil.__trace__();
		
	}

	/**
	 * 修改AndroidManifest.xml文件
	 * */
	public void ModifyManifest(Apk apk) throws Exception {
		StackTraceUtil.__trace__();
		
		AXMLDoc doc = new AXMLDoc();
        doc.parse(new FileInputStream(apk.getFilePath_androidManifest_xml()));
        
        ApplicationInfoEditor applicationInfoEditor = new ApplicationInfoEditor(doc);
        applicationInfoEditor.setEditorInfo(ApplicationInfoEditor.EditorInfo.getInstance()
    		.label_ValueReplaceWith("Mod_" + apk.getLabelName())	// 修改应用名称
    		.name_ValueReplaceWith(Constants.Shell.ShellApplicationName)	// 修改application="com.tomagoyaky.shell.ProxyApplication"
//        	.debugable_switch(1)
		);
        applicationInfoEditor.commit();

        MetaDataEditor metaDataEditor = new MetaDataEditor(doc);
        metaDataEditor.setEditorInfo(new MetaDataEditor.EditorInfo("APPLICATION_CLASS_NAME", apk.getApplication()));
        metaDataEditor.commit();

        doc.build(new FileOutputStream(apk.getFilePath_androidManifest_xml()));
        String fileName_androidManifest_txt = apk.getFilePath_androidManifest_txt().replace(".txt", "_000.txt");
		ReverseApktool.DecodeAndroidManifestXML(apk.getFilePath_androidManifest_xml(), fileName_androidManifest_txt);
        doc.release();
        
        // replace AndroidManifest.xml that into apk file.
        FileUtils.copyFile(new File(apk.getApkFilePath()), new File(apk.getFilePath_unsign()));
        ZipManager.replaceZipEntry(new File(apk.getFilePath_unsign()), new String[]{"AndroidManifest.xml"},
                new String[]{apk.getFilePath_androidManifest_xml()});
	}

	public void ModifyFileSystem(Apk apk) throws Exception {
		StackTraceUtil.__trace__();

		/**
		 * 准备文件
		 */
        ArrayList<String> filelist = new ArrayList<String>();
		do{
			// XXX classes.dex
			ZipManager.extraZipEntry(new File(apk.getApkFilePath()), 
				new String[]{"classes.dex"}, 
				new String[]{Constants.ShellTarget.file_local_classes_dex}
			);
			FileUtil.MakeSureIsExist(Constants.ShellTarget.file_local_classes_dex);
			// XXX lib目录
			ZipManager.extraDirtoryZipEntry(new File(apk.getApkFilePath()), "lib", Constants.ShellTarget.dir_local_lib);
	        FileUtil.getFileList(new File(Constants.ShellTarget.dir_local_lib), filelist);
		}while(false);

        // ****************************************************************************************
		/**
		 * 文件修改
		 * */
		do{
			if(Start.Type_Default == Start.Type_Dynamic_Loading){
				Logger.LOGW("ignore, dex file modify is ignored.");
				break;
			}
			ClassDefUtil.start(apk, Constants.ShellTarget.file_local_classes_dex); // 没有解析出所有的类
		}while(false);

        // ****************************************************************************************
		/**
		 * 修改文件系统
		 * */
		do{
			// XXX DEX文件
			ZipManager.deleteZipEntry(new File(apk.getFilePath_unsign()), new String[]{"classes.dex"});
			ZipManager.addEntrys(new File(apk.getFilePath_unsign()), 
	    		new String[]{
	        		"classes.dex"
	    		},new String[]{
	        		Constants.Shell.file_local_classes_dex
	        	});
	        ZipManager.addEntrys(new File(apk.getFilePath_unsign()),
	    		new String[]{
		        	"assets/classes.dex"
		        }, new String[]{
	        		Constants.ShellTarget.file_local_classes_dex
		        });
	        
	        // XXX SO文件
			for (int i = 0; i < filelist.size(); i++) {
				String filePath = filelist.get(i);
				String entryName = filePath.replace(Constants.ShellTarget.dir_local_lib, "lib").replace("\\", "/");
				ZipManager.deleteZipEntry(new File(apk.getFilePath_unsign()),
					new String[]{
						entryName
					});
		        ZipManager.addEntrys(new File(apk.getFilePath_unsign()), 
		    		new String[]{
		        		entryName.replace("lib/", "assets/lib/")
		    		},new String[]{
		        		filePath
		        	});
			}
		}while(false);
        
	}

	public boolean CreateNewApk(Apk apk) throws Exception {
		StackTraceUtil.__trace__();

        //重新签名
		ZipManager.deleteEntryDir(new File(apk.getFilePath_unsign()), "META-INF");
        SignApk signApk = new SignApk(Constants.Shell.privateKey, Constants.Shell.sigPrefix);
        boolean signed= signApk.sign(apk.getFilePath_unsign(), apk.getFilePath_sign());
        if (signed){
            return SignApk.verifyJar(apk.getFilePath_sign());
        }
		return false;
	}

	public void Running(Apk apk) {
		StackTraceUtil.__trace__();
		ADB.WaitForDevice();
		boolean mHasBeenInstall = ADB.HaveBeenInstalled(apk.getPackageName());
		if(mHasBeenInstall){
			Logger.LOGW("'" + apk.getPackageName() + "' has been installed, remove it ...");
//			ADB.ReInstall(apk.getFilePath_sign());
			ADB.Uninstall(apk.getPackageName());
		}
		Logger.LOGW("'" + apk.getPackageName() + "' is setting up ...");
		ADB.Install(apk);
		
		if(ADB.HaveBeenInstalled(apk.getPackageName())){
			ADB.Running(apk.getPackageName(), apk.getLauncherActivity());
		}else{
			Logger.LOGE("Installation failed !");
		}
	}
}
