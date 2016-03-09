package com.tomagoyaky.ShellAddScriptTool;

import java.io.File;
import java.io.IOException;
import java.nio.ByteBuffer;

import org.apache.commons.io.FileUtils;

import com.android.dexdeps.DexData;
import com.android.dexdeps.DexData.HeaderItem;
import com.tomagoyaky.ShellAddScriptTool.common.Logger;
import com.tomagoyaky.ShellAddScriptTool.common.StackTraceUtil;

public class Dex {

	public String savePath;
	public DexBufferUtil dexBufferUtil;
	public DexData dexData;
	public Dex(String classDexFilePath) throws IOException {
		StackTraceUtil.__trace__();
		
		this.savePath = classDexFilePath;
		File file = new File(classDexFilePath);
		file.createNewFile();
		Logger.LOGD("createNewFile:" + classDexFilePath);
	}

	/*
	 * Direct-mapped "header_item" struct.
	 */
	private final static int kSHA1DigestLen = 20;
    public static final byte[] DEX_FILE_MAGIC = {
        0x64, 0x65, 0x78, 0x0a, 0x30, 0x33, 0x36, 0x00 };
    public static final byte[] DEX_FILE_MAGIC_API_13 = {
        0x64, 0x65, 0x78, 0x0a, 0x30, 0x33, 0x35, 0x00 };
	public static class DexHeader {
		public byte[] magic = new byte[8];           /* includes version number */
		public int checksum;           /* adler32 checksum */
		public byte[] signature = new byte[kSHA1DigestLen]; /* SHA-1 hash */
		public int fileSize;           /* length of entire file */
		public int headerSize;         /* offset to start of next section */
		public int endianTag;
		public int linkSize;
		public int linkOff;
		public int mapOff;
		public int stringIdsSize;
		public int stringIdsOff;
		public int typeIdsSize;
		public int typeIdsOff;
		public int protoIdsSize;
		public int protoIdsOff;
		public int fieldIdsSize;
		public int fieldIdsOff;
		public int methodIdsSize;
		public int methodIdsOff;
		public int classDefsSize;
		public int classDefsOff;
		public int dataSize;
		public int dataOff;
	};
	
	public static class DexBufferUtil {
		
		public final static int __defalutValue__ = 0xFFFFFFFF;
		public String classes_dex;
		public DexHeader dexHeader;
		public ByteBuffer buffer;
		public DexBufferUtil(String file_local_classes_dex) throws Exception {
			StackTraceUtil.__trace__();
			this.classes_dex = file_local_classes_dex;
			long classes_dex_size = FileUtils.sizeOf(new File(this.classes_dex));
			this.buffer = ByteBuffer.allocate((int)classes_dex_size);
			if(this.buffer == null){
				throw new Exception("ByteBuffer allocate error.");
			}
		}
		
		public DexBufferUtil() {
			// TODO Auto-generated constructor stub
		}

		public void initMinSizeBuffer(DexData dexData) throws IOException {
			StackTraceUtil.__trace__();
			
			HeaderItem mHeaderItem = dexData.mHeaderItem;
			dexHeader = new DexHeader();
			dexData.seek(0); // magic
			byte[] magic = new byte[8];
			dexData.readBytes(magic);
			dexHeader.magic = magic;

			dexData.seek(8); // checksum
			dexHeader.checksum = dexData.readInt();

			dexData.seek(8+4); // signature
			byte[] signature = ByteBuffer.allocate(kSHA1DigestLen).array();
			dexData.readBytes(signature);
			dexHeader.signature = signature;
			
			dexHeader.fileSize = (int) FileUtils.sizeOf(new File(this.classes_dex));
			dexHeader.headerSize = mHeaderItem.headerSize;
			dexHeader.endianTag = mHeaderItem.endianTag;
			
			dexData.seek(8 + 4 + 20 + (4 * 3)); 	// linkOff
			dexHeader.linkOff = dexData.readInt();

			dexData.seek(8 + 4 + 20 + (4 * 3) + 4); // linkSize
			dexHeader.linkSize = dexData.readInt();

			dexData.seek(8 + 4 + 20 + (4 * 3) + 8); // mapOff
			dexHeader.mapOff = dexData.readInt();
			
			dexHeader.stringIdsSize = mHeaderItem.stringIdsSize;
			dexHeader.stringIdsOff = mHeaderItem.stringIdsOff;
			dexHeader.typeIdsSize = mHeaderItem.typeIdsSize;
			dexHeader.typeIdsOff = mHeaderItem.typeIdsOff;
			dexHeader.protoIdsSize = mHeaderItem.protoIdsSize;
			dexHeader.protoIdsOff = mHeaderItem.protoIdsOff;
			dexHeader.fieldIdsSize = mHeaderItem.fieldIdsSize;
			dexHeader.fieldIdsOff = mHeaderItem.fieldIdsOff;
			dexHeader.methodIdsSize = mHeaderItem.methodIdsSize;
			dexHeader.methodIdsOff = mHeaderItem.methodIdsOff;
			dexHeader.classDefsSize = mHeaderItem.classDefsSize;
			dexHeader.classDefsOff = mHeaderItem.classDefsOff;

			dexData.seek(8 + 4 + 20 + (4 * 3) + 8 + (4 * 12) + 4); // dataSize
			dexHeader.dataSize = dexData.readInt();
			dexData.seek(8 + 4 + 20 + (4 * 3) + 8 + (4 * 12) + 8); // dataOff
			dexHeader.dataOff = dexData.readInt();
			
			this.buffer.put(dexHeader.magic);
			this.buffer.putInt(ClassDefUtil.ConvertLittleEndianInt(dexHeader.checksum));
			this.buffer.put(dexHeader.signature);
			this.buffer.putInt(ClassDefUtil.ConvertLittleEndianInt(dexHeader.fileSize));
			this.buffer.putInt(ClassDefUtil.ConvertLittleEndianInt(dexHeader.headerSize));
			this.buffer.putInt(ClassDefUtil.ConvertLittleEndianInt(dexHeader.endianTag));
			
			this.buffer.putInt(ClassDefUtil.ConvertLittleEndianInt(dexHeader.linkOff));
			this.buffer.putInt(ClassDefUtil.ConvertLittleEndianInt(dexHeader.linkSize));
			this.buffer.putInt(ClassDefUtil.ConvertLittleEndianInt(dexHeader.mapOff));
			
			this.buffer.putInt(ClassDefUtil.ConvertLittleEndianInt(dexHeader.stringIdsSize));
			this.buffer.putInt(ClassDefUtil.ConvertLittleEndianInt(dexHeader.stringIdsOff));
			this.buffer.putInt(ClassDefUtil.ConvertLittleEndianInt(dexHeader.typeIdsSize));
			this.buffer.putInt(ClassDefUtil.ConvertLittleEndianInt(dexHeader.typeIdsOff));
			this.buffer.putInt(ClassDefUtil.ConvertLittleEndianInt(dexHeader.protoIdsSize));
			this.buffer.putInt(ClassDefUtil.ConvertLittleEndianInt(dexHeader.protoIdsOff));
			this.buffer.putInt(ClassDefUtil.ConvertLittleEndianInt(dexHeader.fieldIdsSize));
			this.buffer.putInt(ClassDefUtil.ConvertLittleEndianInt(dexHeader.fieldIdsOff));
			this.buffer.putInt(ClassDefUtil.ConvertLittleEndianInt(dexHeader.methodIdsSize));
			this.buffer.putInt(ClassDefUtil.ConvertLittleEndianInt(dexHeader.methodIdsOff));
			this.buffer.putInt(ClassDefUtil.ConvertLittleEndianInt(dexHeader.classDefsSize));
			this.buffer.putInt(ClassDefUtil.ConvertLittleEndianInt(dexHeader.classDefsOff));
			
			this.buffer.putInt(ClassDefUtil.ConvertLittleEndianInt(dexHeader.dataSize));
			this.buffer.putInt(ClassDefUtil.ConvertLittleEndianInt(dexHeader.dataOff));
		}
	}

	public void write(byte[] buffer) throws IOException {
		StackTraceUtil.__trace__();
		FileUtils.writeByteArrayToFile(new File(this.savePath), buffer);
		Logger.LOGD("save file:" + this.savePath);
	}

}
