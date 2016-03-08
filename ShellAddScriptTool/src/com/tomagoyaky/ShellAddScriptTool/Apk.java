package com.tomagoyaky.ShellAddScriptTool;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.apache.commons.compress.archivers.ArchiveException;
import org.apache.commons.io.FileUtils;
import org.w3c.dom.Document;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import com.tomagoyaky.ShellAddScriptTool.common.Constants;
import com.tomagoyaky.ShellAddScriptTool.common.FileUtil;
import com.tomagoyaky.ShellAddScriptTool.common.Logger;
import com.tomagoyaky.ShellAddScriptTool.common.ReverseApktool;
import com.tomagoyaky.ShellAddScriptTool.common.StackTraceUtil;
import com.tomagoyaky.apkeditor.apksigner.ZipManager;


public class Apk {

	private String packageName;
	private String dir_decompile;
	private String application;
	
	private String apkFilePath;
	private String fileName;
	private String filePath_sign;
	private String filePath_unsign;
	private String filePath_androidManifest_xml;
	private String filePath_androidManifest_txt;

	private String VersionCode;
	private String VersionName;
	private String LauncherActivity;
	private String LabelName;
	private String minSdkVersion;
	private String targetSdkVersion;
	private String parentPath;
	private String icon;
	
	private ArrayList<String> eabi;
	private ArrayList<String> activitys;
	private ArrayList<String> permissions;
	private ArrayList<String> receivers;
	private ArrayList<String> services;
	private ArrayList<String> meta_datas;
	private ArrayList<String> uses_features;
	
	public Apk(String _apkFilePath) throws Exception {

		StackTraceUtil.__trace__();
		this.apkFilePath = _apkFilePath;
		if(apkFilePath.endsWith(".apk")){
			this.fileName 			= new File(apkFilePath).getName();
			this.filePath_sign 		= Constants.dir_workplace + File.separator + fileName.replace(".apk", "") + "_sign.apk";
			this.filePath_unsign 	= Constants.dir_workplace + File.separator + fileName.replace(".apk", "") + "_unsign.apk";
			this.dir_decompile 		= Constants.dir_workplace + File.separator + fileName.replace(".apk", "");
			
			this.filePath_androidManifest_xml = dir_decompile + File.separator + "AndroidManifest.xml";
			this.filePath_androidManifest_txt = Constants.dir_workplace + File.separator + "AndroidManifest.txt";

	        this.packageName = null;
	        this.application = null;
	        this.VersionCode = null;
	        this.VersionName = null;
	        this.LauncherActivity = null;
	        this.LabelName = null;
	        this.minSdkVersion = null;
	        this.targetSdkVersion = null;
	        this.eabi = new ArrayList<String>();
	        this.parentPath = new File(this.apkFilePath).getParent();

	        this.activitys = new ArrayList<String>();
	        this.permissions = new ArrayList<String>();
	        this.receivers = new ArrayList<String>();
	        this.services = new ArrayList<String>();
	        this.meta_datas = new ArrayList<String>();
	        this.uses_features = new ArrayList<String>();
	        
	        new File(this.dir_decompile).mkdirs();
	        
	        ZipManager.extraZipEntry(new File(this.apkFilePath),
	        		new String[]{"AndroidManifest.xml"}, 
	        		new String[]{filePath_androidManifest_xml});
	        FileUtil.MakeSureIsExist(filePath_androidManifest_xml);
	        
	        ReverseApktool.DecodeAndroidManifestXML(filePath_androidManifest_xml, filePath_androidManifest_txt);
	        FileUtil.MakeSureIsExist(filePath_androidManifest_txt);
	        
			if(ParseAndroidManifestXml(filePath_androidManifest_txt) == false){
				throw new Exception("AndroidManifest.xml file parse error.");
			}
			do {
				if(this.LabelName == null){
					Logger.LOGE("LabelName is null.");
					System.exit(-2);
				}
				if(this.packageName == null){
					Logger.LOGE("packageName is null.");
					System.exit(-2);
				}
				if(this.LauncherActivity == null){
					Logger.LOGE("LauncherActivity is null.");
					System.exit(-3);
				}
			} while (false);
			
			if(Start.Debuggable.APK_INFO){
				Logger.LOGD(this.toString());
			}
		}
	}
	
	/**
	 * 解析AndroidManifest.xml文件，并给成员赋值
	 * */
	private boolean ParseAndroidManifestXml(String _filePath_androidManifest_txt) throws ArchiveException, IOException, ParserConfigurationException, SAXException {

		boolean flag = false;
		String fileData = FileUtils.readFileToString(new File(_filePath_androidManifest_txt));

		NodeList nodeList = null;
		InputStream is = new ByteArrayInputStream(fileData.getBytes("utf-8"));
		DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance(); 
		DocumentBuilder db = dbf.newDocumentBuilder(); 
		Document doc = db.parse(is);
		if(doc != null){
			Node rootNode = doc.getChildNodes().item(0);
			NamedNodeMap rootAttributes = rootNode.getAttributes();
	
			if(rootAttributes != null){
				if(rootAttributes.getNamedItem("package") != null)
					this.packageName = rootAttributes.getNamedItem("package").getNodeValue();
				if(rootAttributes.getNamedItem("android:versionCode") != null)
					this.VersionCode = rootAttributes.getNamedItem("android:versionCode").getNodeValue();
				if(rootAttributes.getNamedItem("android:versionName") != null)
					this.VersionName = rootAttributes.getNamedItem("android:versionName").getNodeValue();
			}
			
			nodeList = doc.getElementsByTagName("uses-permission");
			for (int i = 0; nodeList != null && i < nodeList.getLength(); i++) {
				this.permissions.add(nodeList.item(i).getAttributes().getNamedItem("android:name").getNodeValue());
			}
			
			nodeList = doc.getElementsByTagName("activity");
			for (int i = 0; nodeList != null && i < nodeList.getLength(); i++) {
				boolean is_action_MAIN = false;
				boolean is_category_LAUNCHER = false;
				Node activityNode = nodeList.item(i);
				if(activityNode != null
						&& activityNode.getAttributes() != null
						&& activityNode.getAttributes().getNamedItem("android:name") !=null ){
					String activityName = activityNode.getAttributes().getNamedItem("android:name").getNodeValue();
					if(activityName.startsWith("."))
						this.activitys.add(this.packageName + activityName);
					else
						this.activitys.add(activityName);
						
					if(activityNode.hasChildNodes()){
						NodeList activityChildNodeList = activityNode.getChildNodes();
						for (int j = 0; j < activityChildNodeList.getLength(); j++) {
							Node activityChildNode = activityChildNodeList.item(j);
							if(activityChildNode.getNodeName().equals("intent-filter")){

								Node intent_filter = activityChildNode;
								if(intent_filter.hasChildNodes()){
									NodeList intent_filterChildNodeList = intent_filter.getChildNodes();
									for (int k = 0; k < intent_filterChildNodeList.getLength(); k++) {
										Node intent_filterChildNode = intent_filterChildNodeList.item(k);
										if(intent_filterChildNode.getNodeName().equals("action") 
												&& intent_filterChildNode.getAttributes().getNamedItem("android:name").getNodeValue()
												.equals("android.intent.action.MAIN")){
											is_action_MAIN = true;
										}
										if(intent_filterChildNode.getNodeName().equals("category") 
												&& intent_filterChildNode.getAttributes().getNamedItem("android:name").getNodeValue()
												.equals("android.intent.category.LAUNCHER")){
											is_category_LAUNCHER = true;
										}
										
									}
								}
							}
						}
					}

					if(is_action_MAIN && is_category_LAUNCHER){
						/*
						 * label
						 * */
						if(activityNode.getAttributes() != null && 
								activityNode.getAttributes().getNamedItem("android:label") != null){
							String LabelValue = activityNode.getAttributes().getNamedItem("android:label").getNodeValue().replace("@", "");
	//						this.LabelName = Arsc.getNameFromResId(this, Integer.parseInt(LabelValue, 16));
							this.LabelName = "tomagoyaky";
						}else{
							this.LabelName = "unknow_label";
						}
						
						/*
						 * LauncherActivity
						 * */
						if(activityName.startsWith("."))
							this.LauncherActivity = this.packageName + activityName;
						else
							this.LauncherActivity = activityName;
					}
				}
			}
	
			nodeList = doc.getElementsByTagName("uses-sdk");
			for (int i = 0; nodeList != null && i < nodeList.getLength(); i++) {
				this.minSdkVersion    = nodeList.item(i).getAttributes().getNamedItem("android:minSdkVersion").getNodeValue();
				this.targetSdkVersion = nodeList.item(i).getAttributes().getNamedItem("android:targetSdkVersion").getNodeValue();
			}
	
			nodeList = doc.getElementsByTagName("receiver");
			for (int i = 0; nodeList != null && i < nodeList.getLength(); i++) {
				String receiverName = nodeList.item(i).getAttributes().getNamedItem("android:name").getNodeValue();
				if(receiverName.startsWith("."))
					this.receivers.add(this.packageName + receiverName);
				else
					this.receivers.add(receiverName);
			}
	
			nodeList = doc.getElementsByTagName("service");
			for (int i = 0; nodeList != null && i < nodeList.getLength(); i++) {
				String servicesName = nodeList.item(i).getAttributes().getNamedItem("android:name").getNodeValue();
				if(servicesName.startsWith("."))
					this.services.add(this.packageName + servicesName);
				else
					this.services.add(servicesName);
			}
	
			nodeList = doc.getElementsByTagName("uses-feature");
			for (int i = 0; nodeList != null && i < nodeList.getLength(); i++) {
				this.uses_features.add(nodeList.item(i).getAttributes().getNamedItem("android:name").getNodeValue());
			}
			
			nodeList = doc.getElementsByTagName("application");
			for (int i = 0; nodeList != null && i < nodeList.getLength(); i++) {
				String applicationName = null;
				Node nameNode = nodeList.item(i).getAttributes().getNamedItem("android:name");
				if(nameNode != null){
					applicationName = nameNode.getNodeValue();
					if(applicationName.startsWith("."))
						this.application = this.packageName + applicationName;
					else
						this.application = applicationName;
				}

				Node iconNode = nodeList.item(i).getAttributes().getNamedItem("android:icon");
				String iconStr = iconNode.getNodeValue();
				if(iconStr.startsWith("@")){
					this.icon = iconStr.replace("@", "");
				}
			}
			flag = true;
		}// doc is null
		else{
			flag = false;
		}
		return flag;
	}
	
	public String getPackageName() {
		return packageName;
	}

	public void setPackageName(String packageName) {
		this.packageName = packageName;
	}

	public String getDir_decompile() {
		return dir_decompile;
	}

	public void setDir_decompile(String dir_decompile) {
		this.dir_decompile = dir_decompile;
	}

	public String getApplication() {
		return application;
	}

	public void setApplication(String application) {
		this.application = application;
	}

	public String getApkFilePath() {
		return apkFilePath;
	}

	public void setApkFilePath(String apkFilePath) {
		this.apkFilePath = apkFilePath;
	}

	public String getFileName() {
		return fileName;
	}

	public void setFileName(String fileName) {
		this.fileName = fileName;
	}

	public String getFilePath_sign() {
		return filePath_sign;
	}

	public void setFilePath_sign(String filePath_sign) {
		this.filePath_sign = filePath_sign;
	}

	public String getFilePath_unsign() {
		return filePath_unsign;
	}

	public void setFilePath_unsign(String filePath_unsign) {
		this.filePath_unsign = filePath_unsign;
	}

	public String getFilePath_androidManifest_xml() {
		return filePath_androidManifest_xml;
	}

	public void setFilePath_androidManifest_xml(String filePath_androidManifest_xml) {
		this.filePath_androidManifest_xml = filePath_androidManifest_xml;
	}

	public String getFilePath_androidManifest_txt() {
		return filePath_androidManifest_txt;
	}

	public void setFilePath_androidManifest_txt(String filePath_androidManifest_txt) {
		this.filePath_androidManifest_txt = filePath_androidManifest_txt;
	}

	public String getVersionCode() {
		return VersionCode;
	}

	public void setVersionCode(String versionCode) {
		VersionCode = versionCode;
	}

	public String getVersionName() {
		return VersionName;
	}

	public void setVersionName(String versionName) {
		VersionName = versionName;
	}

	public String getLauncherActivity() {
		return LauncherActivity;
	}

	public void setLauncherActivity(String launcherActivity) {
		LauncherActivity = launcherActivity;
	}

	public String getLabelName() {
		return LabelName;
	}

	public void setLabelName(String labelName) {
		LabelName = labelName;
	}

	public String getMinSdkVersion() {
		return minSdkVersion;
	}

	public void setMinSdkVersion(String minSdkVersion) {
		this.minSdkVersion = minSdkVersion;
	}

	public String getTargetSdkVersion() {
		return targetSdkVersion;
	}

	public void setTargetSdkVersion(String targetSdkVersion) {
		this.targetSdkVersion = targetSdkVersion;
	}

	public String getParentPath() {
		return parentPath;
	}

	public void setParentPath(String parentPath) {
		this.parentPath = parentPath;
	}

	public String getIcon() {
		return icon;
	}

	public void setIcon(String icon) {
		this.icon = icon;
	}

	public ArrayList<String> getEabi() {
		return eabi;
	}

	public void setEabi(ArrayList<String> eabi) {
		this.eabi = eabi;
	}

	public ArrayList<String> getActivitys() {
		return activitys;
	}

	public void setActivitys(ArrayList<String> activitys) {
		this.activitys = activitys;
	}

	public ArrayList<String> getPermissions() {
		return permissions;
	}

	public void setPermissions(ArrayList<String> permissions) {
		this.permissions = permissions;
	}

	public ArrayList<String> getReceivers() {
		return receivers;
	}

	public void setReceivers(ArrayList<String> receivers) {
		this.receivers = receivers;
	}

	public ArrayList<String> getServices() {
		return services;
	}

	public void setServices(ArrayList<String> services) {
		this.services = services;
	}

	public ArrayList<String> getMeta_datas() {
		return meta_datas;
	}

	public void setMeta_datas(ArrayList<String> meta_datas) {
		this.meta_datas = meta_datas;
	}

	public ArrayList<String> getUses_features() {
		return uses_features;
	}

	public void setUses_features(ArrayList<String> uses_features) {
		this.uses_features = uses_features;
	}

	@Override
	public String toString() {
		return "Apk [packageName=" + packageName + "\n dir_decompile="
				+ dir_decompile + "\n application=" + application
				+ "\n apkFilePath=" + apkFilePath + "\n fileName=" + fileName
				+ "\n filePath_sign=" + filePath_sign + "\n filePath_unsign="
				+ filePath_unsign + "\n filePath_androidManifest_xml="
				+ filePath_androidManifest_xml
				+ "\n filePath_androidManifest_txt="
				+ filePath_androidManifest_txt + "\n VersionCode=" + VersionCode
				+ "\n VersionName=" + VersionName + "\n LauncherActivity="
				+ LauncherActivity + "\n LabelName=" + LabelName
				+ "\n minSdkVersion=" + minSdkVersion + "\n targetSdkVersion="
				+ targetSdkVersion + "\n parentPath=" + parentPath + "\n icon="
				+ icon + "\n eabi=" + eabi + "\n activitys=" + activitys
				+ "\n permissions=" + permissions + "\n receivers=" + receivers
				+ "\n services=" + services + "\n meta_datas=" + meta_datas
				+ "\n uses_features=" + uses_features + "\n getPackageName()="
				+ getPackageName() + "\n getDir_decompile()="
				+ getDir_decompile() + "\n getApplication()=" + getApplication()
				+ "\n getApkFilePath()=" + getApkFilePath() + "\n getFileName()="
				+ getFileName() + "\n getFilePath_sign()=" + getFilePath_sign()
				+ "\n getFilePath_unsign()=" + getFilePath_unsign()
				+ "\n getFilePath_androidManifest_xml()="
				+ getFilePath_androidManifest_xml()
				+ "\n getFilePath_androidManifest_txt()="
				+ getFilePath_androidManifest_txt() + "\n getVersionCode()="
				+ getVersionCode() + "\n getVersionName()=" + getVersionName()
				+ "\n getLauncherActivity()=" + getLauncherActivity()
				+ "\n getLabelName()=" + getLabelName()
				+ "\n getMinSdkVersion()=" + getMinSdkVersion()
				+ "\n getTargetSdkVersion()=" + getTargetSdkVersion()
				+ "\n getParentPath()=" + getParentPath() + "\n getIcon()="
				+ getIcon() + "\n getEabi()=" + getEabi() + "\n getActivitys()="
				+ getActivitys() + "\n getPermissions()=" + getPermissions()
				+ "\n getReceivers()=" + getReceivers() + "\n getServices()="
				+ getServices() + "\n getMeta_datas()=" + getMeta_datas()
				+ "\n getUses_features()=" + getUses_features()
				+ "\n getClass()=" + getClass() + "\n hashCode()=" + hashCode()
				+ "\n toString()=" + super.toString() + "]";
	}
}
