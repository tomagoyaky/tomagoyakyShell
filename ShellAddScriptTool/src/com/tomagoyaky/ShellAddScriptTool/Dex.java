package com.tomagoyaky.ShellAddScriptTool;

import java.io.File;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.Arrays;

import org.apache.commons.io.FileUtils;

import com.android.dexdeps.DexData;
import com.tomagoyaky.ShellAddScriptTool.common.Logger;
import com.tomagoyaky.ShellAddScriptTool.common.StackTraceUtil;

public class Dex {

	public String savePath;
	public DexBufferUtil dexBufferUtil;
	public DexData dexData;
	public Dex(){
		StackTraceUtil.__trace__();
	}
	public void setModifiedDexFileSavePath(String classDexFilePath) throws IOException {

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
		
		@Override
		public String toString() {
			return "DexHeader [magic=" + Arrays.toString(magic) + ", checksum="
					+ checksum + ", signature=" + Arrays.toString(signature)
					+ ", fileSize=" + fileSize + ", headerSize=" + headerSize
					+ ", endianTag=" + endianTag + ", linkSize=" + linkSize
					+ ", linkOff=" + linkOff + ", mapOff=" + mapOff
					+ ", stringIdsSize=" + stringIdsSize + ", stringIdsOff="
					+ stringIdsOff + ", typeIdsSize=" + typeIdsSize
					+ ", typeIdsOff=" + typeIdsOff + ", protoIdsSize="
					+ protoIdsSize + ", protoIdsOff=" + protoIdsOff
					+ ", fieldIdsSize=" + fieldIdsSize + ", fieldIdsOff="
					+ fieldIdsOff + ", methodIdsSize=" + methodIdsSize
					+ ", methodIdsOff=" + methodIdsOff + ", classDefsSize="
					+ classDefsSize + ", classDefsOff=" + classDefsOff
					+ ", dataSize=" + dataSize + ", dataOff=" + dataOff + "]";
		}
		
		@Override
		public int hashCode() {
			final int prime = 31;
			int result = 1;
			result = prime * result + checksum;
			result = prime * result + classDefsOff;
			result = prime * result + classDefsSize;
			result = prime * result + dataOff;
			result = prime * result + dataSize;
			result = prime * result + endianTag;
			result = prime * result + fieldIdsOff;
			result = prime * result + fieldIdsSize;
			result = prime * result + fileSize;
			result = prime * result + headerSize;
			result = prime * result + linkOff;
			result = prime * result + linkSize;
			result = prime * result + Arrays.hashCode(magic);
			result = prime * result + mapOff;
			result = prime * result + methodIdsOff;
			result = prime * result + methodIdsSize;
			result = prime * result + protoIdsOff;
			result = prime * result + protoIdsSize;
			result = prime * result + Arrays.hashCode(signature);
			result = prime * result + stringIdsOff;
			result = prime * result + stringIdsSize;
			result = prime * result + typeIdsOff;
			result = prime * result + typeIdsSize;
			return result;
		}
		@Override
		public boolean equals(Object obj) {
			if (this == obj) {
				return true;
			}
			if (obj == null) {
				return false;
			}
			if (getClass() != obj.getClass()) {
				return false;
			}
			DexHeader other = (DexHeader) obj;
			if (checksum != other.checksum) {
				return false;
			}
			if (classDefsOff != other.classDefsOff) {
				return false;
			}
			if (classDefsSize != other.classDefsSize) {
				return false;
			}
			if (dataOff != other.dataOff) {
				return false;
			}
			if (dataSize != other.dataSize) {
				return false;
			}
			if (endianTag != other.endianTag) {
				return false;
			}
			if (fieldIdsOff != other.fieldIdsOff) {
				return false;
			}
			if (fieldIdsSize != other.fieldIdsSize) {
				return false;
			}
			if (fileSize != other.fileSize) {
				return false;
			}
			if (headerSize != other.headerSize) {
				return false;
			}
			if (linkOff != other.linkOff) {
				return false;
			}
			if (linkSize != other.linkSize) {
				return false;
			}
			if (!Arrays.equals(magic, other.magic)) {
				return false;
			}
			if (mapOff != other.mapOff) {
				return false;
			}
			if (methodIdsOff != other.methodIdsOff) {
				return false;
			}
			if (methodIdsSize != other.methodIdsSize) {
				return false;
			}
			if (protoIdsOff != other.protoIdsOff) {
				return false;
			}
			if (protoIdsSize != other.protoIdsSize) {
				return false;
			}
			if (!Arrays.equals(signature, other.signature)) {
				return false;
			}
			if (stringIdsOff != other.stringIdsOff) {
				return false;
			}
			if (stringIdsSize != other.stringIdsSize) {
				return false;
			}
			if (typeIdsOff != other.typeIdsOff) {
				return false;
			}
			if (typeIdsSize != other.typeIdsSize) {
				return false;
			}
			return true;
		}
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
		
		public void ParseDexHeader(DexData dexData) throws IOException{

			StackTraceUtil.__trace__();
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
			dexHeader.headerSize = dexData.mHeaderItem.headerSize;
			dexHeader.endianTag = dexData.mHeaderItem.endianTag;
			
			dexData.seek(8 + 4 + 20 + (4 * 3)); 	// linkOff
			dexHeader.linkOff = dexData.readInt();

			dexData.seek(8 + 4 + 20 + (4 * 3) + 4); // linkSize
			dexHeader.linkSize = dexData.readInt();

			dexData.seek(8 + 4 + 20 + (4 * 3) + 8); // mapOff
			dexHeader.mapOff = dexData.readInt();
			
			dexHeader.stringIdsSize = dexData.mHeaderItem.stringIdsSize;
			dexHeader.stringIdsOff = dexData.mHeaderItem.stringIdsOff;
			dexHeader.typeIdsSize = dexData.mHeaderItem.typeIdsSize;
			dexHeader.typeIdsOff = dexData.mHeaderItem.typeIdsOff;
			dexHeader.protoIdsSize = dexData.mHeaderItem.protoIdsSize;
			dexHeader.protoIdsOff = dexData.mHeaderItem.protoIdsOff;
			dexHeader.fieldIdsSize = dexData.mHeaderItem.fieldIdsSize;
			dexHeader.fieldIdsOff = dexData.mHeaderItem.fieldIdsOff;
			dexHeader.methodIdsSize = dexData.mHeaderItem.methodIdsSize;
			dexHeader.methodIdsOff = dexData.mHeaderItem.methodIdsOff;
			dexHeader.classDefsSize = dexData.mHeaderItem.classDefsSize;
			dexHeader.classDefsOff = dexData.mHeaderItem.classDefsOff;

			dexData.seek(8 + 4 + 20 + (4 * 3) + 8 + (4 * 12) + 4); // dataSize
			dexHeader.dataSize = dexData.readInt();
			dexData.seek(8 + 4 + 20 + (4 * 3) + 8 + (4 * 12) + 8); // dataOff
			dexHeader.dataOff = dexData.readInt();
		}

		public void initMinSizeBuffer(DexData dexData) throws IOException {
			StackTraceUtil.__trace__();
			
			ParseDexHeader(dexData);
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
