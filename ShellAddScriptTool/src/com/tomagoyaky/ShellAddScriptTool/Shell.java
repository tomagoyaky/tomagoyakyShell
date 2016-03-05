package com.tomagoyaky.ShellAddScriptTool;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;

import org.apache.commons.io.FileUtils;

import com.tomagoyaky.ShellAddScriptTool.common.Constants;
import com.tomagoyaky.ShellAddScriptTool.common.FileUtil;
import com.tomagoyaky.ShellAddScriptTool.common.Logger;
import com.tomagoyaky.ShellAddScriptTool.common.StackTraceUtil;
import com.tomagoyaky.apkeditor.apksigner.SignApk;
import com.tomagoyaky.apkeditor.apksigner.ZipManager;
import com.tomagoyaky.apkeditor.axmleditor.decode.AXMLDoc;
import com.tomagoyaky.apkeditor.axmleditor.editor.ApplicationInfoEditor;
import com.tomagoyaky.apkeditor.axmleditor.editor.MetaDataEditor;

public class Shell {

	public void Analysis(Apk apk) {
		Logger.LOGD(StackTraceUtil.getMethodWithClassName());
		
	}

	/**
	 * 修改AndroidManifest.xml文件
	 * */
	public void ModifyManifest(Apk apk) throws Exception {
		Logger.LOGD(StackTraceUtil.getMethodWithClassName());
		
		AXMLDoc doc = new AXMLDoc();
        doc.parse(new FileInputStream(apk.getFilePath_androidManifest_xml()));

        ApplicationInfoEditor applicationInfoEditor = new ApplicationInfoEditor(doc);
        applicationInfoEditor.setEditorInfo(ApplicationInfoEditor.EditorInfo.getInstance()
    		.label_ValueReplaceWith("Mod_" + apk.getLabelName())
    		.name_ValueReplaceWith(Constants.Shell.ShellApplicationName)
//        	.debugable_switch(1)
		);
        applicationInfoEditor.commit();

        MetaDataEditor metaDataEditor = new MetaDataEditor(doc);
        metaDataEditor.setEditorInfo(new MetaDataEditor.EditorInfo("APPLICATION_CLASS_NAME", apk.getPackageName()));
        metaDataEditor.commit();

        doc.build(new FileOutputStream(apk.getFilePath_androidManifest_xml()));
        doc.release();
        
        // replace AndroidManifest.xml that into apk file.
        FileUtils.copyFile(new File(apk.getApkFilePath()), new File(apk.getFilePath_unsign()));
        ZipManager.replaceZipEntry(new File(apk.getFilePath_unsign()), new String[]{"AndroidManifest.xml"},
                new String[]{apk.getFilePath_androidManifest_xml()});
	}

	public void ModifyFileSystem(Apk apk) throws Exception {
		Logger.LOGD(StackTraceUtil.getMethodWithClassName());

		String org_classes_dex = Constants.dir_workplace + File.separator + "classes.dex";
		String shell_classes_dex = Constants.Shell.file_classes_dex;
		FileUtil.MakeSureIsExist(shell_classes_dex);
		ZipManager.extraZipEntry(new File(apk.getApkFilePath()), 
			new String[]{"classes.dex"}, 
			new String[]{org_classes_dex}
		);

		/**
		 * TODO 文件加密 
		 * */
		/*
		 * DO SOMETHING ....
		 * */
		
		// 修改文件系统
		ZipManager.deleteZipEntry(new File(apk.getFilePath_unsign()), new String[]{"classes.dex"});
        ZipManager.addEntrys(new File(apk.getFilePath_unsign()), 
    		new String[]{
        		"classes.dex"
    		},new String[]{
        		shell_classes_dex
        	});
        ZipManager.addEntrys(new File(apk.getFilePath_unsign()),
    		new String[]{
	        	"assets/tomagoyaky_classes.dex"
	        }, new String[]{
	        	org_classes_dex
	        }); 
	}

	public boolean CreateNewApk(Apk apk) throws Exception {
		Logger.LOGD(StackTraceUtil.getMethodWithClassName());

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
		Logger.LOGD(StackTraceUtil.getMethodWithClassName());
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
