package com.tomagoyaky.ShellAddScriptTool;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.RandomAccessFile;
import java.util.zip.ZipEntry;
import java.util.zip.ZipException;
import java.util.zip.ZipFile;

import com.android.dexdeps.ClassRef;
import com.android.dexdeps.DexData;
import com.android.dexdeps.FieldRef;
import com.android.dexdeps.MethodRef;
import com.tomagoyaky.ShellAddScriptTool.common.Constants;
import com.tomagoyaky.ShellAddScriptTool.common.Logger;
import com.tomagoyaky.ShellAddScriptTool.common.StackTraceUtil;

public class ClassDefUtil {

	public static final int level_empty_every_file = 1;
	/**
	 * 对dex文件进行处理,主要是挖空和加密
	 * 此功能主要参考了android源码中：com.android.dexdeps项目的代码,对DexData类的成员的访问权限由private全部变为了public
	 * @throws IOException 
	 * */
	public static void start(Apk apk, String file_local_classes_dex) throws IOException {

		StackTraceUtil.__trace__();
        Dex dex = null;
        Dex.DexBufferUtil dexBufferUtil = null;
		try {
			// XXX 加载并解析dex文件
			Logger.LOGD("parse dex file: " + file_local_classes_dex);
			RandomAccessFile raf = openInputFile(file_local_classes_dex);
			DexData dexData = new DexData(raf);
            dexData.load();

            // 0x01 创建buffer
	        dex = new Dex(Constants.Shell.file_local_modify_classes_dex);
	        dexBufferUtil = new Dex.DexBufferUtil(file_local_classes_dex);
	        dex.dexBufferUtil = dexBufferUtil;
	        dex.dexData = dexData;
	        dexBufferUtil.initMinSizeBuffer(dexData);  			//创建dex文件头等信息
            // 0x02 更新数据 (可在这些update 函数里面添加增删改操作)
	        updateStringPool(dex);
	        updateTypePool(dex);
	        updateProtoPool(dex);
	        updateFieldPool(dex);
	        updateMethodPool(dex);
	        updateClassPool(dex);
	        updateMapPool(dex);
	        // 0x03 更新索引
	        
            long class_num = 0;
            long field_num = 0;
            long method_num = 0;

            ClassRef[] externClassRefs = dexData.getExternalReferences();
        	class_num += externClassRefs.length;
            for (int i = 0; i < externClassRefs.length; i++) {

                ClassRef ref = externClassRefs[i];
	            FieldRef[] fields = ref.getFieldArray();
	            MethodRef[] methods = ref.getMethodArray();
	            
            	field_num += fields.length;
            	method_num += methods.length;
                if(ref.getName().startsWith("Lcom" /*apk.getPackageName().replace(".", "/")*/) || ref.getName().contains("yingyonghui")){
                    Logger.LOGW(ref.getName());
                }
//              Logger.LOGW(ref.getName());
            
	            for (int j = 0; j < fields.length; j++) {
	                FieldRef fieldRef = fields[j];
	            }
	            
	            for (int j = 0; j < methods.length; j++) {
	                MethodRef methodRef = methods[j];
	                
	                int key = level_empty_every_file;
					// XXX 修改函数(挖空并置换)
	                switch (key ) {
					case level_empty_every_file:
						// 获取方法的内存区间
						break;

					default:
						break;
					}
	            }
            }
            // XXX 对dex数据本地化为dex文件
            dex.write(dexBufferUtil.buffer.array());
            // XXX 对dex文件头进行修正
            
            // XXX 对dex文件尝试验证
            
            // XXX 对dex文件进行变异 (防止直接静态被加载)
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private static void updateMapPool(Dex dex) {
		// TODO Auto-generated method stub
		
	}

	private static void updateClassPool(Dex dex) {
		// TODO Auto-generated method stub
		
	}

	private static void updateMethodPool(Dex dex) {
		// TODO Auto-generated method stub
		
	}

	private static void updateFieldPool(Dex dex) {
		// TODO Auto-generated method stub
		
	}

	private static void updateProtoPool(Dex dex) {
		// TODO Auto-generated method stub
		
	}

	private static void updateTypePool(Dex dex) {
		// TODO Auto-generated method stub
		
	}

	private static void updateStringPool(Dex dex) {
		// TODO Auto-generated method stub
		
		// 测试替换一个字符串索引
		int baseStringOffset = dex.dexBufferUtil.dexHeader.stringIdsOff;
        for (int i = 0; i < dex.dexData.mStrings.length; i++) {
        	Logger.LOGD(dex.dexData.mStrings[i]);
        	baseStringOffset += dex.dexData.mStrings[i].length();
		}
	}

	public static int byteArrayToInt(byte[] b, int offset) {
		int value = 0;
		for (int i = 0; i < 4; i++) {
			int shift = (4 - 1 - i) * 8;
			value += (b[i + offset] & 0x000000FF) << shift;// 往高位游
		}
		return value;
	}
	
	public static byte[] intToByteArray(final int integer) {
		int byteNum = (40 - Integer.numberOfLeadingZeros(integer < 0 ? ~integer : integer)) / 8;
		byte[] byteArray = new byte[4];

		for (int n = 0; n < byteNum; n++)
			byteArray[3 - n] = (byte) (integer >>> (n * 8));

		return (byteArray);
	}

	public static int ConvertLittleEndianInt(int value){
		byte[] tmpBuf = intToByteArray(value);
		return (tmpBuf[0] & 0xff) | ((tmpBuf[1] & 0xff) << 8) |
            ((tmpBuf[2] & 0xff) << 16) | ((tmpBuf[3] & 0xff) << 24);
	}
	/**
	 * Opens an input file, which could be a .dex or a .jar/.apk with a
	 * classes.dex inside. If the latter, we extract the contents to a temporary
	 * file.
	 * 
	 * @param fileName
	 *            the name of the file to open
	 */
	public static RandomAccessFile openInputFile(String fileName)
			throws IOException {
		RandomAccessFile raf;

		raf = openInputFileAsZip(fileName);
		if (raf == null) {
			File inputFile = new File(fileName);
			raf = new RandomAccessFile(inputFile, "r");
		}

		return raf;
	}

	/**
	 * Tries to open an input file as a Zip archive (jar/apk) with a
	 * "classes.dex" inside.
	 * 
	 * @param fileName
	 *            the name of the file to open
	 * @return a RandomAccessFile for classes.dex, or null if the input file is
	 *         not a zip archive
	 * @throws IOException
	 *             if the file isn't found, or it's a zip and classes.dex isn't
	 *             found inside
	 */
	public static RandomAccessFile openInputFileAsZip(String fileName)
			throws IOException {
		ZipFile zipFile;

		/*
		 * Try it as a zip file.
		 */
		try {
			zipFile = new ZipFile(fileName);
		} catch (FileNotFoundException fnfe) {
			/* not found, no point in retrying as non-zip */
			System.err.println("Unable to open '" + fileName + "': "
					+ fnfe.getMessage());
			throw fnfe;
		} catch (ZipException ze) {
			/* not a zip */
			return null;
		}

		/*
		 * We know it's a zip; see if there's anything useful inside. A failure
		 * here results in some type of IOException (of which ZipException is a
		 * subclass).
		 */
		ZipEntry entry = zipFile.getEntry("classes.dex");
		if (entry == null) {
			System.err.println("Unable to find '" + "classes.dex" + "' in '"
					+ fileName + "'");
			zipFile.close();
			throw new ZipException();
		}

		InputStream zis = zipFile.getInputStream(entry);

		/*
		 * Create a temp file to hold the DEX data, open it, and delete it to
		 * ensure it doesn't hang around if we fail.
		 */
		File tempFile = File.createTempFile("dexdeps", ".dex");
		// System.out.println("+++ using temp " + tempFile);
		RandomAccessFile raf = new RandomAccessFile(tempFile, "rw");
		tempFile.delete();

		/*
		 * Copy all data from input stream to output file.
		 */
		byte copyBuf[] = new byte[32768];
		int actual;

		while (true) {
			actual = zis.read(copyBuf);
			if (actual == -1)
				break;

			raf.write(copyBuf, 0, actual);
		}

		zis.close();
		raf.seek(0);
		return raf;
	}

}
