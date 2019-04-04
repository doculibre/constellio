package com.constellio.model.services.background;

import com.constellio.data.utils.TimeProvider;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.filefilter.AgeFileFilter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileFilter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.attribute.BasicFileAttributes;
import java.nio.file.attribute.FileTime;
import java.util.Date;

public class TemporaryFolderCleanerBackgroundAction implements Runnable {

	Logger LOGGER =  LoggerFactory.getLogger(TemporaryFolderCleanerBackgroundAction.class);

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

	public synchronized void run() {
		if(tempFolder.exists()) {
			File[] files = tempFolder.listFiles((FileFilter) new AgeFileFilter(System.currentTimeMillis() - DEFAULT_TIME_TO_DELETION_MILLI));
			if(files != null) {
				for(File file : files) {
					try {
						if(file.getName().startsWith(PREFIX_OF_FILE_TO_DELETE) && file.getName().endsWith(SUFFIX_OF_FILE_TO_DELETE)) {
							boolean deleted = FileUtils.deleteQuietly(file);
							if(!deleted) {
								LOGGER.error("Could not delete file " + file.getName());
							}
						}
					} catch (Exception e) {
						e.printStackTrace();
					}
				}
			}
		}
	}
}
