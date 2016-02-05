package com.constellio.data.utils;

public class BigFileEntry {

	String fileName;

	byte[] bytes;

	public BigFileEntry(String fileName, byte[] bytes) {
		this.fileName = fileName;
		this.bytes = bytes;
	}

	public String getFileName() {
		return fileName;
	}

	public byte[] getBytes() {
		return bytes;
	}
}
