package com.tomagoyaky.ShellAddScriptTool;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;

import com.tomagoyaky.ShellAddScriptTool.common.Constants;
import com.tomagoyaky.ShellAddScriptTool.common.Logger;
import com.tomagoyaky.apkeditor.apksigner.ZipManager;
import com.tomagoyaky.parseresource.ParseResourceUtils;
import com.tomagoyaky.parseresource.Utils;
import com.tomagoyaky.parseresource.type.ResTableConfig;
import com.tomagoyaky.parseresource.type.ResTableEntry;
import com.tomagoyaky.parseresource.type.ResTableMap;
import com.tomagoyaky.parseresource.type.ResTableMapEntry;
import com.tomagoyaky.parseresource.type.ResTableType;
import com.tomagoyaky.parseresource.type.ResTableTypeSpec;
import com.tomagoyaky.parseresource.type.ResValue;

public class Arsc {

	public static byte[] getSource(String resourceFileName){
		byte[] srcByte = null;
		FileInputStream fis = null;
		ByteArrayOutputStream bos = null;
		try{
			fis = new FileInputStream(resourceFileName);
			bos = new ByteArrayOutputStream();
			byte[] buffer = new byte[1024];
			int len = 0;
			while((len=fis.read(buffer)) != -1){
				bos.write(buffer, 0, len);
			}
			srcByte = bos.toByteArray();
		}catch(Exception e){
			Logger.LOGE("read res file error:"+e.toString());
		}finally{
			try{
				fis.close();
				bos.close();
			}catch(Exception e){
				Logger.LOGE("close file error:"+e.toString());
			}
		}
		
		if(srcByte == null){
			Logger.LOGE("get src error...");
			return null;
		}
		return srcByte;
	}
	
	/**
	 * 获取应用的名称
	 * 参考:http://blog.csdn.net/jiangwei0910410003/article/details/50628894
	 * */

	// 资源包的id和类型id
	public static String getNameFromResId(Apk apk, int resIdvalue) throws IOException {
		
		String result = null;
		ZipManager.extraZipEntry(new File(apk.getApkFilePath()), 
			new String[]{"resources.arsc"}, 
			new String[]{Constants.dir_workplace + File.separator + "resources.arsc"}
		);
		byte[] srcByte = getSource(Constants.dir_workplace + File.separator + "resources.arsc");
		ParseResourceUtils.setDebug(false);
		ParseResourceUtils.parsePackage(srcByte);
		ParseResourceUtils.parseResTableHeaderChunk(srcByte);
		ParseResourceUtils.parseResStringPoolChunk(srcByte);
		ParseResourceUtils.parsePackage(srcByte);
		ParseResourceUtils.parseTypeStringPoolChunk(srcByte);
		ParseResourceUtils.parseKeyStringPoolChunk(srcByte);
		while(!ParseResourceUtils.isEnd(srcByte.length)){
			boolean isSpec = ParseResourceUtils.isTypeSpec(srcByte);
			if(isSpec){
//				===============================================
//				ParseResourceUtils.parseResTypeSpec(srcByte);
//				===============================================
				ResTableTypeSpec typeSpec = new ResTableTypeSpec();
				// 解析头部信息
				typeSpec.header = ParseResourceUtils.parseResChunkHeader(srcByte, ParseResourceUtils.resTypeOffset);
				int offset = (ParseResourceUtils.resTypeOffset + typeSpec.header.getHeaderSize());

				// 解析id类型
				byte[] idByte = Utils.copyByte(srcByte, offset, 1);
				typeSpec.id = (byte) (idByte[0] & 0xFF);
				ParseResourceUtils.resTypeId = ParseResourceUtils.getResId(typeSpec.id);
				/**
				 * 在这里对resIdvalue进行比对
				 * */
				if(resIdvalue == ParseResourceUtils.resTypeId){
					Logger.LOGE("resIdvalue:" + resIdvalue);
				}

				// 解析res0字段,这个字段是备用的，始终是0
				byte[] res0Byte = Utils.copyByte(srcByte, offset + 1, 1);
				typeSpec.res0 = (byte) (res0Byte[0] & 0xFF);

				// 解析res1字段，这个字段是备用的，始终是0
				byte[] res1Byte = Utils.copyByte(srcByte, offset + 2, 2);
				typeSpec.res1 = Utils.byte2Short(res1Byte);

				// entry的总个数
				byte[] entryCountByte = Utils.copyByte(srcByte, offset + 4, 4);
				typeSpec.entryCount = Utils.byte2int(entryCountByte);
				// 获取entryCount个int数组
				int[] intAry = new int[typeSpec.entryCount];
				int intAryOffset = ParseResourceUtils.resTypeOffset + typeSpec.header.headerSize;
				for (int i = 0; i < typeSpec.entryCount; i++) {
					int element = Utils.byte2int(Utils.copyByte(srcByte, intAryOffset + i * 4, 4));
					intAry[i] = element;
				}

				ParseResourceUtils.resTypeOffset += typeSpec.header.size;

			}else{
//				===============================================
//				ParseResourceUtils.parseResTypeInfo(srcByte);
//				===============================================
				ResTableType type = new ResTableType();
				// 解析头部信息
				type.header = ParseResourceUtils.parseResChunkHeader(srcByte, ParseResourceUtils.resTypeOffset);
				int offset = (ParseResourceUtils.resTypeOffset + type.header.getHeaderSize());

				// 解析type的id值
				byte[] idByte = Utils.copyByte(srcByte, offset, 1);
				type.id = (byte) (idByte[0] & 0xFF);

				// 解析res0字段的值，备用字段，始终是0
				byte[] res0 = Utils.copyByte(srcByte, offset + 1, 1);
				type.res0 = (byte) (res0[0] & 0xFF);

				// 解析res1字段的值，备用字段，始终是0
				byte[] res1 = Utils.copyByte(srcByte, offset + 2, 2);
				type.res1 = Utils.byte2Short(res1);

				byte[] entryCountByte = Utils.copyByte(srcByte, offset + 4, 4);
				type.entryCount = Utils.byte2int(entryCountByte);

				byte[] entriesStartByte = Utils.copyByte(srcByte, offset + 8, 4);
				type.entriesStart = Utils.byte2int(entriesStartByte);

				ResTableConfig resConfig = new ResTableConfig();
				resConfig = ParseResourceUtils.parseResTableConfig(Utils.copyByte(srcByte, offset + 12,
						resConfig.getSize()));

				// 先获取entryCount个int数组
				int[] intAry = new int[type.entryCount];
				for (int i = 0; i < type.entryCount; i++) {
					int element = Utils.byte2int(Utils.copyByte(srcByte, ParseResourceUtils.resTypeOffset
							+ type.header.headerSize + i * 4, 4));
					intAry[i] = element;
				}

				// 这里开始解析后面对应的ResEntry和ResValue
				int entryAryOffset = ParseResourceUtils.resTypeOffset + type.entriesStart;
				ResTableEntry[] tableEntryAry = new ResTableEntry[type.entryCount];
				ResValue[] resValueAry = new ResValue[type.entryCount];

				// 这里存在一个问题就是如果是ResMapEntry的话，偏移值是不一样的，所以这里需要计算不同的偏移值
				int bodySize = 0, valueOffset = entryAryOffset;
				for (int i = 0; i < type.entryCount; i++) {
					ParseResourceUtils.resTypeId = ParseResourceUtils.getResId(i);
					/**
					 * 在这里对resIdvalue进行比对
					 * */
					if(resIdvalue == ParseResourceUtils.resTypeId){
						Logger.LOGE("resIdvalue:" + resIdvalue);
					}
					ResTableEntry entry = new ResTableEntry();
					ResValue value = new ResValue();
					valueOffset += bodySize;
					entry = ParseResourceUtils.parseResEntry(Utils.copyByte(srcByte, valueOffset,
							entry.getSize()));

					// 这里需要注意的是，先判断entry的flag变量是否为1,如果为1的话，那就ResTable_map_entry
					if (entry.flags == 1) {
						// 这里是复杂类型的value
						ResTableMapEntry mapEntry = new ResTableMapEntry();
						mapEntry = ParseResourceUtils.parseResMapEntry(Utils.copyByte(srcByte, valueOffset,
								mapEntry.getSize()));
						ResTableMap resMap = new ResTableMap();
						for (int j = 0; j < mapEntry.count; j++) {
							int mapOffset = valueOffset + mapEntry.getSize()
									+ resMap.getSize() * j;
							resMap = ParseResourceUtils.parseResTableMap(Utils.copyByte(srcByte, mapOffset,
									resMap.getSize()));
						}
						bodySize = mapEntry.getSize() + resMap.getSize()
								* mapEntry.count;
					} else {
						// 这里是简单的类型的value
						value = ParseResourceUtils.parseResValue(Utils.copyByte(srcByte,
								valueOffset + entry.getSize(), value.getSize()));
						bodySize = entry.getSize() + value.getSize();
					}

					tableEntryAry[i] = entry;
					resValueAry[i] = value;
				}
				ParseResourceUtils.resTypeOffset += type.header.size;
			}
		}
		return result;
	}
}
