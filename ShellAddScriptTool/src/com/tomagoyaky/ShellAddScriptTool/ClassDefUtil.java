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
	 * @throws IOException 
	 * */
	public static void start(Apk apk, String file_local_classes_dex) throws IOException {

		StackTraceUtil.__trace__();
        Dex dex = new Dex(Constants.Shell.file_local_modify_classes_dex);
        Dex.DexBuffer dexBuffer = new Dex.DexBuffer();
        dexBuffer.initMinSizeBuffer();  //创建dex文件头等信息
		try {
			// XXX 遍历dex文件
			Logger.LOGD("parse dex file: " + file_local_classes_dex);
			RandomAccessFile raf = openInputFile(file_local_classes_dex);
			DexData dexData = new DexData(raf);
            dexData.load();

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
//                Logger.LOGW(ref.getName());
            
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
            dex.write(dexBuffer);
            // XXX 对dex文件头进行修正
            
            // XXX 对dex文件尝试验证
            
            // XXX 对dex文件进行变异 (防止直接静态被加载)
		} catch (IOException e) {
			e.printStackTrace();
		}
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
