package com.tomagoyaky.parseresource;

import java.io.ByteArrayOutputStream;
import java.io.FileInputStream;

import com.tomagoyaky.ShellAddScriptTool.common.Logger;

public class ParseResourceMain {
	
	private static boolean flag = true;
	public static void main(String[] args){
		
		byte[] srcByte = null;
		FileInputStream fis = null;
		ByteArrayOutputStream bos = null;
		try{
			fis = new FileInputStream("resource/resources_gdt1.arsc");
			bos = new ByteArrayOutputStream();
			byte[] buffer = new byte[1024];
			int len = 0;
			while((len=fis.read(buffer)) != -1){
				bos.write(buffer, 0, len);
			}
			srcByte = bos.toByteArray();
		}catch(Exception e){
			if(flag)Logger.LOGD("read res file error:"+e.toString());
		}finally{
			try{
				fis.close();
				bos.close();
			}catch(Exception e){
				if(flag)Logger.LOGD("close file error:"+e.toString());
			}
		}
		
		if(srcByte == null){
			if(flag)Logger.LOGD("get src error...");
			return;
		}
		
		ParseResourceUtils.setDebug(true);
		if(flag)Logger.LOGD("parse restable header...");
		ParseResourceUtils.parseResTableHeaderChunk(srcByte);
		if(flag)Logger.LOGD("++++++++++++++++++++++++++++++++++++++");
		
		if(flag)Logger.LOGD("parse resstring pool chunk...");
		ParseResourceUtils.parseResStringPoolChunk(srcByte);
		if(flag)Logger.LOGD("++++++++++++++++++++++++++++++++++++++");
		
		if(flag)Logger.LOGD("parse package chunk...");
		ParseResourceUtils.parsePackage(srcByte);
		if(flag)Logger.LOGD("++++++++++++++++++++++++++++++++++++++");
		
		if(flag)Logger.LOGD("parse typestring pool chunk...");
		ParseResourceUtils.parseTypeStringPoolChunk(srcByte);
		if(flag)Logger.LOGD("++++++++++++++++++++++++++++++++++++++");
		
		if(flag)Logger.LOGD("parse keystring pool chunk...");
		ParseResourceUtils.parseKeyStringPoolChunk(srcByte);
		if(flag)Logger.LOGD("++++++++++++++++++++++++++++++++++++++");
		
		int resCount = 0;
		while(!ParseResourceUtils.isEnd(srcByte.length)){
			resCount++;
			boolean isSpec = ParseResourceUtils.isTypeSpec(srcByte);
			if(isSpec){
				if(flag)Logger.LOGD("parse restype spec chunk...");
				ParseResourceUtils.parseResTypeSpec(srcByte);
				if(flag)Logger.LOGD("++++++++++++++++++++++++++++++++++++++");
			}else{
				if(flag)Logger.LOGD("parse restype info chunk...");
				ParseResourceUtils.parseResTypeInfo(srcByte);
				if(flag)Logger.LOGD("++++++++++++++++++++++++++++++++++++++");
			}
		}
		if(flag)Logger.LOGD("res count:"+resCount);
		
	}

}
