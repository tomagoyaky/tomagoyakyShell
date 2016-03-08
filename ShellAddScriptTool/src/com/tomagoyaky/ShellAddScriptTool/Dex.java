package com.tomagoyaky.ShellAddScriptTool;

import java.io.IOException;

public class Dex {

	public Dex(String classDexFilePath) throws IOException {
		
		throw new IOException();
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
		public byte[]  magic = new byte[8];           /* includes version number */
		public int  checksum;           /* adler32 checksum */
		public byte[]  signature = new byte[kSHA1DigestLen]; /* SHA-1 hash */
		public int  fileSize;           /* length of entire file */
		public int  headerSize;         /* offset to start of next section */
		public int  endianTag;
		public int  linkSize;
		public int  linkOff;
		public int  mapOff;
		public int  stringIdsSize;
		public int  stringIdsOff;
		public int  typeIdsSize;
		public int  typeIdsOff;
		public int  protoIdsSize;
		public int  protoIdsOff;
		public int  fieldIdsSize;
		public int  fieldIdsOff;
		public int  methodIdsSize;
		public int  methodIdsOff;
		public int  classDefsSize;
		public int  classDefsOff;
		public int  dataSize;
		public int  dataOff;
	};
	
	public static class DexBuffer {
		

		public DexHeader dexHeader;
		public void append(String data){
			
		}

		public void initMinSizeBuffer() {
			dexHeader = new DexHeader();
			dexHeader.magic = DEX_FILE_MAGIC;
		}

	}

	public void write(DexBuffer dexBuffer) {
		// TODO Auto-generated method stub
		
	}

}
