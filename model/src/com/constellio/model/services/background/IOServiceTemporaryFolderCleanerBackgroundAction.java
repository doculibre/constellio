package com.constellio.model.services.background;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.filefilter.AgeFileFilter;

import java.io.File;
import java.io.FileFilter;


@Slf4j
public class IOServiceTemporaryFolderCleanerBackgroundAction implements Runnable {

	public static final long DELAI_FOR_DELETION_IN_MILLI = 1000 * 60 * 60 * 24;

	private File tempFolder;

	public IOServiceTemporaryFolderCleanerBackgroundAction(File tempFolder) {
		this.tempFolder = tempFolder;
	}

	public synchronized void run() {
		if (tempFolder.exists()) {
			deleteFilesThatExpired();
		}
	}

	private void deleteFilesThatExpired() {
		deleteFilesThatExpired(tempFolder, false);
	}

	private void deleteFilesThatExpired(File folder, boolean canDeleteFolder) {
		File[] files = folder.listFiles((FileFilter) new AgeFileFilter(System.currentTimeMillis() - DELAI_FOR_DELETION_IN_MILLI));
		if (files != null) {
			for (File file : files) {
				try {
					if (!file.isDirectory()) {
						boolean deleted = FileUtils.deleteQuietly(file);
						if (!deleted) {
							log.error("Could not delete file " + file.getName());
						}
					} else {
						deleteFilesThatExpired(file, true);
					}
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		}
		if (canDeleteFolder) {
			File[] filesAfterSuppression = folder.listFiles();
			if (filesAfterSuppression.length == 0) {
				FileUtils.deleteQuietly(folder);
			}
		}
	}
}
