package com.tomagoyaky.parsedex;

import java.io.File;
import java.io.IOException;

import org.apache.commons.io.FileUtils;

public class ParseDexMain {
	
	public static void main(String[] args){
		
		byte[] srcByte = null;
		try {
			srcByte = FileUtils.readFileToByteArray(new File("C:\\Users\\peng\\Desktop\\org_classes.dex"));
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		System.out.println("ParseHeader:");
		ParseDexUtils.praseDexHeader(srcByte);
		System.out.println("++++++++++++++++++++++++++++++++++++++++");
		
		System.out.println("Parse StringIds:");
		ParseDexUtils.parseStringIds(srcByte);
		System.out.println("++++++++++++++++++++++++++++++++++++++++");
		
		System.out.println("Parse StringList:");
		ParseDexUtils.parseStringList(srcByte);
		System.out.println("++++++++++++++++++++++++++++++++++++++++");
		
		System.out.println("Parse TypeIds:");
		ParseDexUtils.parseTypeIds(srcByte);
		System.out.println("++++++++++++++++++++++++++++++++++++++++");
		
		System.out.println("Parse ProtoIds:");
		ParseDexUtils.parseProtoIds(srcByte);
		System.out.println("++++++++++++++++++++++++++++++++++++++++");
		
		System.out.println("Parse FieldIds:");
		ParseDexUtils.parseFieldIds(srcByte);
		System.out.println("++++++++++++++++++++++++++++++++++++++++");
		
		System.out.println("Parse MethodIds:");
		ParseDexUtils.parseMethodIds(srcByte);
		System.out.println("++++++++++++++++++++++++++++++++++++++++");
		
		System.out.println("Parse ClassIds:");
		ParseDexUtils.parseClassIds(srcByte);
		System.out.println("++++++++++++++++++++++++++++++++++++++++");
		
		System.out.println("Parse MapList:");
		ParseDexUtils.parseMapItemList(srcByte);
		System.out.println("++++++++++++++++++++++++++++++++++++++++");
		
		System.out.println("Parse Class Data:");
		ParseDexUtils.parseClassData(srcByte);
		System.out.println("++++++++++++++++++++++++++++++++++++++++");
		
		System.out.println("Parse Code Content:");
		ParseDexUtils.parseCode(srcByte);
		System.out.println("++++++++++++++++++++++++++++++++++++++++");
		
	}

}
