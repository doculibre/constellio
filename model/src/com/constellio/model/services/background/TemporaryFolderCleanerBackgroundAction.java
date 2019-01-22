package com.constellio.model.services.background;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.attribute.BasicFileAttributes;
import java.nio.file.attribute.FileTime;

public class TemporaryFolderCleanerBackgroundAction implements Runnable {

	Logger logger =  LoggerFactory.getLogger(TemporaryFolderCleanerBackgroundAction.class);

	public static final String DEFAULT_TEMP_FILE = "/tmp";
	public static final long DEFAULT_TIME_TO_DELETION_MILLI = 1800000;
	public static final String PREFIX_OF_FILE_TO_DELETE = "lu";
	public static final String SUFFIX_OF_FILE_TO_DELETE = ".tmp";

	private File tempFolder;
	private long timeInMilliSecondBeforeDeletion;


	public TemporaryFolderCleanerBackgroundAction() {
		setTempFolder(DEFAULT_TEMP_FILE);
		setWaitTimeToDelete(DEFAULT_TIME_TO_DELETION_MILLI);
	}

	public void setTempFolder(String tempFolder) {
		this.tempFolder = new File(tempFolder);
	}

	public void setTempFolder(File tempFolder) {
		this.tempFolder = tempFolder;
	}

	public void setWaitTimeToDelete(long timeToDeleteMilli) {
		this.timeInMilliSecondBeforeDeletion = timeToDeleteMilli;
	}

	public long getcurrentTimeInMillis() {
		return System.currentTimeMillis();
	}

	public synchronized void run() {
		if(tempFolder.exists()) {
			for(File file : tempFolder.listFiles()) {
				try {
					BasicFileAttributes attrs = Files.readAttributes(file.toPath(), BasicFileAttributes.class);
					FileTime fileTime = attrs.lastAccessTime();

					//2 if to make it easyer to test.
					if(file.getName().startsWith(PREFIX_OF_FILE_TO_DELETE) && file.getName().endsWith(SUFFIX_OF_FILE_TO_DELETE)) {
						long differenceInMilli = getcurrentTimeInMillis() - fileTime.toMillis();

						if (differenceInMilli > timeInMilliSecondBeforeDeletion){
							file.delete();
						}
					}
				} catch (IOException e) {
					logger.warn("IOException on read file properties", e);
				}
			}
		}
	}
}
