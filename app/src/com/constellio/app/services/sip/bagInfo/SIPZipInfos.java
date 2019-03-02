package com.constellio.app.services.sip.bagInfo;

import com.constellio.data.utils.KeyIntMap;
import org.joda.time.LocalDateTime;

public class SIPZipInfos {

	String sipName;

	long uncompressedLengthOfFiles;

	int eadFilesCount;

	int contentFilesCount;

	LocalDateTime creationTime;

	KeyIntMap<String> extensionAndCount = new KeyIntMap<>();

	public SIPZipInfos(String sipName, LocalDateTime creationTime) {
		this.sipName = sipName;
		this.creationTime = creationTime;
	}

	public void logFile(String extension, long length) {
		extensionAndCount.increment(extension);
		uncompressedLengthOfFiles += length;
		contentFilesCount++;
	}

	public String getSipName() {
		return sipName;
	}

	public long getUncompressedLengthOfFiles() {
		return uncompressedLengthOfFiles;
	}

	public int getEadFilesCount() {
		return eadFilesCount;
	}

	public int getContentFilesCount() {
		return contentFilesCount;
	}

	public int getTotalFilesCount() {
		return contentFilesCount + eadFilesCount;
	}

	public LocalDateTime getCreationTime() {
		return creationTime;
	}

	public KeyIntMap<String> getExtensionAndCount() {
		return extensionAndCount;
	}
}
