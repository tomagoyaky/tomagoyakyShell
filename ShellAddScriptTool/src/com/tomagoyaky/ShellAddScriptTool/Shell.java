package com.tomagoyaky.ShellAddScriptTool;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.zip.Inflater;

import org.apache.commons.io.FileUtils;
import org.jf.smali.smaliParser.integer_literal_return;

import com.android.dexdeps.ClassRef;
import com.android.dexdeps.DexData;
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
import com.tomagoyaky.parsedex.ParseDexUtils;
import com.tomagoyaky.parsedex.Utils;
import com.tomagoyaky.parsedex.struct.ClassDataItem;
import com.tomagoyaky.parsedex.struct.ClassDefItem;
import com.tomagoyaky.parsedex.struct.CodeItem;
import com.tomagoyaky.parsedex.struct.EncodedField;
import com.tomagoyaky.parsedex.struct.EncodedMethod;
import com.tomagoyaky.parsedex.struct.FieldIdsItem;
import com.tomagoyaky.parsedex.struct.HeaderType;
import com.tomagoyaky.parsedex.struct.MethodIdsItem;
import com.tomagoyaky.parsedex.struct.ProtoIdsItem;
import com.tomagoyaky.parsedex.struct.StringIdsItem;
import com.tomagoyaky.parsedex.struct.TypeIdsItem;

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

		ArrayList<String> filelist = new ArrayList<String>();
		do{
			// XXX 准备lib目录
			ZipManager.extraDirtoryZipEntry(new File(apk.getApkFilePath()), "lib", Constants.ShellTarget.dir_local_lib);
	        FileUtil.getFileList(new File(Constants.ShellTarget.dir_local_lib), filelist);
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

	public boolean PrepareDexFile(Apk apk) throws IOException{
		StackTraceUtil.__trace__();

		boolean flag = false;
		/**
		 * 准备dex文件
		 */
		do{
			if(Start.Type_Default == Start.Type_Dynamic_Loading){
				Logger.LOGW("ignore, dex file modify is ignored.");
				break;
			}
			flag = true;
			// XXX classes.dex
			ZipManager.extraZipEntry(new File(apk.getApkFilePath()), 
				new String[]{"classes.dex"}, 
				new String[]{Constants.ShellTarget.file_local_classes_dex}
			);
			FileUtil.MakeSureIsExist(Constants.ShellTarget.file_local_classes_dex);
		}while(false);
		return flag;
	}
	
	public void ModifyDexFile(Apk apk) throws IOException {
		ClassDefUtil.start(apk, Constants.ShellTarget.file_local_classes_dex); // 没有解析出所有的类
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

	public void GetChecksumData(Apk apk) {

		boolean N = false;
		boolean Y = true;
		int checkDexHeader 	= N ? 1 << 1 : 0;
		int checkClassRefs 	= Y ? 1 << 2 : 0;
		int checkMethodRefs = Y ? 1 << 3 : 0;
		int checkMethodName = Y ? 1 << 4 : 0;
		int checkMask = checkDexHeader | checkClassRefs | checkMethodRefs | checkMethodName;
		int OffetMaxValue = (int) FileUtils.sizeOf(new File(Constants.ShellTarget.file_local_classes_dex));
		try {
			// XXX 加载配置文件
//			List<String> lines = FileUtils.readLines(new File(Constants.file_ChecksumDataConfigFile));
			ArrayList<String> classList = new ArrayList<String>();
			ArrayList<String> methodList = new ArrayList<String>();
			
			
			HashMap<String, String> checksumMap = new HashMap<String, String>();
			// XXX 加载并解析dex文件
			Logger.LOGD("parse dex file: " + Constants.ShellTarget.file_local_classes_dex);
            DexData dexData = ClassDefUtil.getDexData(Constants.ShellTarget.file_local_classes_dex);

            Dex dex = new Dex();
            dex.dexData = dexData;
            Dex.DexBufferUtil dexBufferUtil = new Dex.DexBufferUtil(Constants.ShellTarget.file_local_classes_dex);
	        dex.dexBufferUtil = dexBufferUtil;
            dex.dexBufferUtil.ParseDexHeader(dexData);

        	if((checkMask & checkDexHeader) != 0){
		        // ========================================================
		        int dexHeaderHashCodeValue = dex.dexBufferUtil.dexHeader.hashCode();
		        checksumMap.put("dexHeader", Integer.toHexString(dexHeaderHashCodeValue));
        	}
	        // ========================================================
        	ClassRef[] classRefs = (ClassRef[])dex.dexData.getExternalReferences(); // getExternalReferences
        	if((checkMask & checkClassRefs) != 0){
		        checksumMap.put("classRefsCount", String.valueOf(classRefs.length));
		        for (ClassRef classRef : classRefs) {

		        }
//		        checksumMap.put("classRefsNameHashCode", Integer.toHexString(nameBuilder.toString().hashCode()));
        	}
		}catch(Exception ex){
			ex.printStackTrace();
		}
	}

	public void GetDexHashCode(Apk apk) throws IOException {

		byte[] dexByte = FileUtils.readFileToByteArray(new File(Constants.ShellTarget.file_local_classes_dex));
		try {
			// XXX 加载配置文件
//			List<String> lines = FileUtils.readLines(new File(Constants.file_ChecksumDataConfigFile));
			HashMap<String, String> checksumMap = new HashMap<String, String>();
			
			// XXX 加载并解析dex文件头信息
			ArrayList<ClassDefItem> classDefItemList = new ArrayList<ClassDefItem>();
			ArrayList<ClassDataItem> classDataItemList = new ArrayList<ClassDataItem>();
			HashMap<Integer, ClassDefItem> ClassDefItemHashMap = new HashMap<Integer, ClassDefItem>();
			HeaderType headerType = ParseDexUtils.praseDexHeader(dexByte);

			ArrayList<EncodedMethod> methodList = new ArrayList<EncodedMethod>();
			// XXX 解析StringIds
			ArrayList<StringIdsItem> stringIdsList = new ArrayList<StringIdsItem>();
			do {
				for (int i = 0; i < headerType.string_ids_size; i++) {
					byte[] dexOffsetByteArry = Utils.copyByte(dexByte, headerType.string_ids_off + i * StringIdsItem.getSize(), StringIdsItem.getSize());
					StringIdsItem stringIdsItem = new StringIdsItem();
					byte[] idsByte = Utils.copyByte(dexOffsetByteArry, 0, 4);
					stringIdsItem.string_data_off = Utils.byte2int(idsByte);
					stringIdsList.add(stringIdsItem);
				}
			} while (false);
			
			// XXX 解析StringList
			ArrayList<String> stringList = new ArrayList<String>();
			do {
				for(StringIdsItem stringIdsItem : stringIdsList){
					byte size = dexByte[stringIdsItem.string_data_off];
					byte[] strByte = Utils.copyByte(dexByte, stringIdsItem.string_data_off + 1, size);
					if(strByte != null)
						stringList.add(new String(strByte, "UTF-8"));
					else
						stringList.add(null);
				}
			} while (false);
			
			// XXX 解析TypeIds
			ArrayList<TypeIdsItem> typeIdsList = new ArrayList<TypeIdsItem>();
			do {
				int countIds = headerType.type_ids_size;
				for (int i = 0; i < countIds; i++) {
					byte[] dexOffsetByteArry = Utils.copyByte(dexByte, headerType.type_ids_off + i * TypeIdsItem.getSize(), TypeIdsItem.getSize());
//					typeIdsList.add(parseTypeIdsItem(dexOffsetByteArry));
					TypeIdsItem typeIdsItem = new TypeIdsItem();
					byte[] descriptorIdxByte = Utils.copyByte(dexOffsetByteArry, 0, 4);
					typeIdsItem.descriptor_idx = Utils.byte2int(descriptorIdxByte);
					typeIdsList.add(typeIdsItem);
				}
			} while (false);
			
			// XXX 解析ProtoIds
			ArrayList<ProtoIdsItem> protoIdsList = new ArrayList<ProtoIdsItem>();
			do {
				for (int i = 0; i < headerType.proto_ids_size; i++) {
					byte[] dexOffsetByteArry = Utils.copyByte(dexByte, headerType.proto_ids_off + i * ProtoIdsItem.getSize(), ProtoIdsItem.getSize());

					ProtoIdsItem protoIdsItem = new ProtoIdsItem();
					byte[] shortyIdxByte = Utils.copyByte(dexOffsetByteArry, 0, 4);
					protoIdsItem.shorty_idx = Utils.byte2int(shortyIdxByte);
					
					byte[] returnTypeIdxByte = Utils.copyByte(dexOffsetByteArry, 4, 8);
					protoIdsItem.return_type_idx = Utils.byte2int(returnTypeIdxByte);
					
					byte[] parametersOffByte = Utils.copyByte(dexOffsetByteArry, 8, 4);
					protoIdsItem.parameters_off = Utils.byte2int(parametersOffByte);
					
					protoIdsList.add(protoIdsItem);
				}
			} while (false);

			// XXX FieldIds解析
			ArrayList<FieldIdsItem> fieldIdsList = new ArrayList<FieldIdsItem>();
			do {
				for (int i = 0; i < headerType.field_ids_size; i++) {
					byte[] dexOffsetByteArray = Utils.copyByte(dexByte, headerType.field_ids_off + i * FieldIdsItem.getSize(), FieldIdsItem.getSize());
					FieldIdsItem fieldIdsItem = new FieldIdsItem();
					
					byte[] classIdxByte = Utils.copyByte(dexOffsetByteArray, 0, 2);
					fieldIdsItem.class_idx = Utils.byte2Short(classIdxByte);
					
					byte[] typeIdxByte = Utils.copyByte(dexOffsetByteArray, 2, 2);
					fieldIdsItem.type_idx = Utils.byte2Short(typeIdxByte);
					
					byte[] nameIdxByte = Utils.copyByte(dexOffsetByteArray, 4, 4);
					fieldIdsItem.name_idx = Utils.byte2int(nameIdxByte);
					
					fieldIdsList.add(fieldIdsItem);
					
//					Logger.LOGD("->class:" + stringList.get(typeIdsList.get(fieldIdsItem.class_idx).descriptor_idx)
//							+ ",name:" + stringList.get(fieldIdsItem.name_idx)
//							+ ",type:" + stringList.get(typeIdsList.get(fieldIdsItem.type_idx).descriptor_idx));
				}
			} while (false);

			// XXX MethodIds解析
			ArrayList<MethodIdsItem> methodIdsList = new ArrayList<MethodIdsItem>();
			do {
				for (int i = 0; i < headerType.method_ids_size; i++) {
					byte[] dexOffsetByteArray = Utils.copyByte(dexByte, headerType.method_ids_off + i * MethodIdsItem.getSize(), MethodIdsItem.getSize());
					MethodIdsItem methodIdsItem = new MethodIdsItem();
					
					byte[] classIdxByte = Utils.copyByte(dexOffsetByteArray, 0, 2);
					methodIdsItem.class_idx = Utils.byte2Short(classIdxByte);
					
					byte[] protoIdxByte = Utils.copyByte(dexOffsetByteArray, 2, 2);
					methodIdsItem.proto_idx = Utils.byte2Short(protoIdxByte);
					
					byte[] nameIdxByte = Utils.copyByte(dexOffsetByteArray, 4, 4);
					methodIdsItem.name_idx = Utils.byte2int(nameIdxByte);
					
					methodIdsList.add(methodIdsItem);
					ProtoIdsItem protoIdsItem = protoIdsList.get(methodIdsItem.proto_idx);
					String classValue = stringList.get(typeIdsList.get(methodIdsItem.class_idx).descriptor_idx);
					String nameValue = stringList.get(methodIdsItem.name_idx);
					String return_type = stringList.get(protoIdsItem.return_type_idx);
					String shortyValue = stringList.get(protoIdsItem.shorty_idx);
					StringBuilder paramValue = new StringBuilder();
					
					for (int j = 0; j < protoIdsItem.parametersList.size(); j++) {
						paramValue.append(protoIdsItem.parametersList.get(j) + " ");
					}
							
//					Logger.LOGD("[MethodIdsItem] " + shortyValue + " " + return_type + " " + classValue + "->" + nameValue + "("
//							+ paramValue.toString()
//							+ ")");
				}
			} while (false);
			
			// XXX ClassIds解析
			ArrayList<ClassDefItem> classDefList = new ArrayList<ClassDefItem>();
			do {
				for (int i = 0; i < headerType.class_defs_size; i++) {
					byte[] dexOffsetByteArray = Utils.copyByte(dexByte, headerType.class_defs_off + i * ClassDefItem.getSize(), ClassDefItem.getSize());
					ClassDefItem classDefItem = new ClassDefItem();
					
					byte[] classIdxByte = Utils.copyByte(dexOffsetByteArray, 0, 4);
					classDefItem.class_idx = Utils.byte2int(classIdxByte);
					
					byte[] accessFlagsByte = Utils.copyByte(dexOffsetByteArray, 4, 4);
					classDefItem.access_flags = Utils.byte2int(accessFlagsByte);
					
					byte[] superClassIdxByte = Utils.copyByte(dexOffsetByteArray, 8, 4);
					classDefItem.superclass_idx = Utils.byte2int(superClassIdxByte);
					
					//这里如果class没有interfaces的话，这里就为0
					byte[] iterfacesOffByte = Utils.copyByte(dexOffsetByteArray, 12, 4);
					classDefItem.iterfaces_off = Utils.byte2int(iterfacesOffByte);
					
					//如果此项信息缺失，值为0xFFFFFF
					byte[] sourceFileIdxByte = Utils.copyByte(dexOffsetByteArray, 16, 4);
					classDefItem.source_file_idx = Utils.byte2int(sourceFileIdxByte);
					
					byte[] annotationsOffByte = Utils.copyByte(dexOffsetByteArray, 20, 4);
					classDefItem.annotations_off = Utils.byte2int(annotationsOffByte);
					
					byte[] classDataOffByte = Utils.copyByte(dexOffsetByteArray, 24, 4);
					classDefItem.class_data_off = Utils.byte2int(classDataOffByte);
					
					byte[] staticValueOffByte = Utils.copyByte(dexOffsetByteArray, 28, 4);
					classDefItem.static_value_off = Utils.byte2int(staticValueOffByte);
					
					classDefList.add(classDefItem);
					Logger.LOGD("class:" + stringList.get(typeIdsList.get(classDefItem.class_idx).descriptor_idx));
					
					classDefList.add(classDefItem);
				}
			} while (false);
			
			// XXX 解析ClassIds信息
//			ParseDexUtils.parseClassIds(srcByte);
			for (int i = 0; i < headerType.class_defs_size; i++) {
				ClassDefItem classDefItem = new ClassDefItem();
				
				byte[] dexOffsetByteArry = Utils.copyByte(dexByte, headerType.class_defs_off + i * ClassDefItem.getSize(), ClassDefItem.getSize());
				classDefItem.class_idx = Utils.byte2int(Utils.copyByte(dexOffsetByteArry, 0, 4));
				
				byte[] accessFlagsByte = Utils.copyByte(dexOffsetByteArry, 4, 4);
				classDefItem.access_flags = Utils.byte2int(accessFlagsByte);
				
				byte[] superClassIdxByte = Utils.copyByte(dexOffsetByteArry, 8, 4);
				classDefItem.superclass_idx = Utils.byte2int(superClassIdxByte);
				
				//这里如果class没有interfaces的话，这里就为0
				byte[] iterfacesOffByte = Utils.copyByte(dexOffsetByteArry, 12, 4);
				classDefItem.iterfaces_off = Utils.byte2int(iterfacesOffByte);
				
				//如果此项信息缺失，值为0xFFFFFF
				byte[] sourceFileIdxByte = Utils.copyByte(dexOffsetByteArry, 16, 4);
				classDefItem.source_file_idx = Utils.byte2int(sourceFileIdxByte);
				
				byte[] annotationsOffByte = Utils.copyByte(dexOffsetByteArry, 20, 4);
				classDefItem.annotations_off = Utils.byte2int(annotationsOffByte);
				
				byte[] classDataOffByte = Utils.copyByte(dexOffsetByteArry, 24, 4);
				classDefItem.class_data_off = Utils.byte2int(classDataOffByte);
				
				byte[] staticValueOffByte = Utils.copyByte(dexOffsetByteArry, 28, 4);
				classDefItem.static_value_off = Utils.byte2int(staticValueOffByte);
				// 保存 在链表中
				classDefItemList.add(classDefItem);
				
//				// 保存 在HashMap中
				ClassDefItemHashMap.put(classDefItem.toString().hashCode(), classDefItem);
			}
			// ===================================================================================
			// XXX 解析ClassData信息
//			ParseDexUtils.parseClassData(srcByte);
			for(int key : ClassDefItemHashMap.keySet()){
				
				ClassDefItem classDefItem = ClassDefItemHashMap.get(key);
				int dataOffset = classDefItem.class_data_off;

				TypeIdsItem typeItem = typeIdsList.get(classDefItem.class_idx);
				TypeIdsItem superTypeItem = typeIdsList.get(classDefItem.superclass_idx);
				String sourceFile = stringList.get(classDefItem.source_file_idx);
				Logger.LOGW("[source]:" + sourceFile + ",[typeItem]:" + stringList.get(typeItem.descriptor_idx) + ",[superTypeItem]:" + stringList.get(superTypeItem.descriptor_idx));
//				if(sourceFile != null)
//					continue;
				do{
					ClassDataItem item = new ClassDataItem();
					for (int i = 0; i < 4; i++) {
						byte[] byteArry = Utils.readUnsignedLeb128(dexByte, dataOffset);
						dataOffset += byteArry.length;
						int size = 0;
						if(byteArry.length == 1){
							size = byteArry[0];
						}else if(byteArry.length == 2){
							size = Utils.byte2Short(byteArry);
						}else if(byteArry.length == 4){
							size = Utils.byte2int(byteArry);
						}
						if(i == 0){
							item.static_fields_size = size;
						}else if(i == 1){
							item.instance_fields_size = size;
						}else if(i == 2){
							item.direct_methods_size = size;
						}else if(i == 3){
							item.virtual_methods_size = size;
						}
					}
					
					//解析static_fields数组
					EncodedField[] staticFieldArry = new EncodedField[item.static_fields_size];
					for (int i = 0; i < item.static_fields_size; i++) {
						/**
						 *  public int filed_idx_diff;
							public int access_flags;
						 */
						EncodedField staticField = new EncodedField();
						staticField.filed_idx_diff = Utils.readUnsignedLeb128(dexByte, dataOffset);
						dataOffset += staticField.filed_idx_diff.length;
						staticField.access_flags = Utils.readUnsignedLeb128(dexByte, dataOffset);
						dataOffset += staticField.access_flags.length;
						staticFieldArry[i] = staticField;
					}
					
					//解析instance_fields数组
					EncodedField[] instanceFieldArry = new EncodedField[item.instance_fields_size];
					for (int i = 0; i < item.instance_fields_size; i++) {
						/**
						 *  public int filed_idx_diff;
							public int access_flags;
						 */
						EncodedField instanceField = new EncodedField();
						instanceField.filed_idx_diff = Utils.readUnsignedLeb128(dexByte, dataOffset);
						dataOffset += instanceField.filed_idx_diff.length;
						instanceField.access_flags = Utils.readUnsignedLeb128(dexByte, dataOffset);
						dataOffset += instanceField.access_flags.length;
						instanceFieldArry[i] = instanceField;
					}
					
					//解析static_methods数组
					EncodedMethod[] staticMethodsArry = new EncodedMethod[item.direct_methods_size];
					for (int i = 0; i < item.direct_methods_size; i++) {
						/**
						 *  public byte[] method_idx_diff;
							public byte[] access_flags;
							public byte[] code_off;
						 */
						EncodedMethod directMethod = new EncodedMethod();
						directMethod.method_idx_diff = Utils.readUnsignedLeb128(dexByte, dataOffset);
						dataOffset += directMethod.method_idx_diff.length;
						directMethod.access_flags = Utils.readUnsignedLeb128(dexByte, dataOffset);
						dataOffset += directMethod.access_flags.length;
						directMethod.code_off = Utils.readUnsignedLeb128(dexByte, dataOffset);
						dataOffset += directMethod.code_off.length;
						staticMethodsArry[i] = directMethod;
						
						methodList.add(directMethod);
					}
					
					//解析virtual_methods数组
					EncodedMethod[] instanceMethodsArry = new EncodedMethod[item.virtual_methods_size];
					for (int i = 0; i < item.virtual_methods_size; i++) {
						/**
						 *  public byte[] method_idx_diff;
							public byte[] access_flags;
							public byte[] code_off;
						 */
						EncodedMethod instanceMethod = new EncodedMethod();
						instanceMethod.method_idx_diff = Utils.readUnsignedLeb128(dexByte, dataOffset);
						dataOffset += instanceMethod.method_idx_diff.length;
						instanceMethod.access_flags = Utils.readUnsignedLeb128(dexByte, dataOffset);
						dataOffset += instanceMethod.access_flags.length;
						instanceMethod.code_off = Utils.readUnsignedLeb128(dexByte, dataOffset);
						dataOffset += instanceMethod.code_off.length;
						instanceMethodsArry[i] = instanceMethod;
						
						methodList.add(instanceMethod);
					}
					
					item.static_fields = staticFieldArry;
					item.instance_fields = instanceFieldArry;
					item.direct_methods = staticMethodsArry;
					item.virtual_methods = instanceMethodsArry;
					classDataItemList.add(item);
				}while(false);
			}
//
//			// XXX 解析代码
//			int code_off = Utils.decodeUleb128(methodList.get(i).code_off);
//			if(code_off == -1)
//				continue;
//			CodeItem codeItem = new CodeItem();
//			
//			byte[] regSizeByte = Utils.copyByte(dexByte, code_off, 2);
//			if(regSizeByte == null)
//				continue;
//			codeItem.registers_size = Utils.byte2Short(regSizeByte);
//			
//			byte[] insSizeByte = Utils.copyByte(dexByte, code_off + 2, 2);
//			codeItem.ins_size = Utils.byte2Short(insSizeByte);
//			
//			byte[] outsSizeByte = Utils.copyByte(dexByte, code_off + 4, 2);
//			codeItem.outs_size = Utils.byte2Short(outsSizeByte);
//			
//			byte[] triesSizeByte = Utils.copyByte(dexByte, code_off + 6, 2);
//			codeItem.tries_size = Utils.byte2Short(triesSizeByte);
//			
//			byte[] debugInfoByte = Utils.copyByte(dexByte, code_off + 8, 4);
//			codeItem.debug_info_off = Utils.byte2int(debugInfoByte);
//			
//			byte[] insnsSizeByte = Utils.copyByte(dexByte, code_off + 12, 4);
//			codeItem.insns_size = Utils.byte2int(insnsSizeByte);
//			
//			short[] insnsArry = new short[codeItem.insns_size];
//			int aryOffset = code_off + 16;
//			for (int j = 0; j < codeItem.insns_size; j++) {
//				byte[] insnsByte = Utils.copyByte(dexByte, aryOffset + j * 2, 2);
//				insnsArry[j] = Utils.byte2Short(insnsByte);
//			}
//			codeItem.insns = insnsArry;
		}catch(Exception ex){
			ex.printStackTrace();
		}
	}
}
