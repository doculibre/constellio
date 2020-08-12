package com.constellio.app.api.admin.services;

import com.constellio.data.io.services.facades.FileService;
import com.constellio.data.conf.FoldersLocator;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.StringReader;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;

import com.constellio.data.io.services.facades.FileService;
import com.constellio.model.conf.FoldersLocator;

public class WrapperConfUpdateUtils {

	// Set an important TLS attribute - DO NOT REMOVE
	public static void setSettingAdditionalEphemeralDHKeySize(File originalFile, FileService fileService) {
		if(!originalFile.exists()) {
			return;
		}

		int currentIndexOfAdditionalSetting = getLastAdditionalSettingNumber(originalFile);

		setSetting(originalFile, fileService, "wrapper.java.additional." + getLastAdditionalSettingNumber(originalFile), "wrapper.java.additional."
				+ (currentIndexOfAdditionalSetting + 1)
				+ "=-Djdk.tls.ephemeralDHKeySize=2048");
	}

	public static void setSettingAdditionalTemporaryDirectory(File originalFile, File parentDirectory, FileService fileService) {
		if(!originalFile.exists()) {
			return;
		}

		String path = new File(parentDirectory, FoldersLocator.CONSTELLIO_TMP).getAbsolutePath();

		int currentIndexOfAdditionalSetting = getLastAdditionalSettingNumber(originalFile);

		setSetting(originalFile, fileService, "wrapper.java.additional."
				+ currentIndexOfAdditionalSetting, "wrapper.java.additional."
				+ (currentIndexOfAdditionalSetting + 1) + "=-Djava.io.tmpdir=" + path);
	}

	private static int getLastAdditionalSettingNumber(File originalFile) {
		BufferedReader reader = null;
		int biggestNumberFound = 0;
		try {
			String content = FileUtils.readFileToString(originalFile, "UTF-8");
			reader = new BufferedReader(new StringReader(content));
			String line;


			while ((line = reader.readLine()) != null) {
				if(line.startsWith("wrapper.java.additional.")) {
					String number = line.replace("wrapper.java.additional.", "");
					number = number.substring(0,number.indexOf("="));
					if(StringUtils.isNumeric(number)) {
						int numFound = Integer.parseInt(number);
						if (numFound > biggestNumberFound) {
							biggestNumberFound = numFound;
						}
					}
				}
			}

		} catch (IOException ioe) {
			ioe.printStackTrace();
		} finally {
			if(reader != null) {
				try {
					reader.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}

		return biggestNumberFound;
	}

	public static void setSetting(File originalFile, FileService fileService, String lineBefore, String lineToAdd) {
		File backup = new File(originalFile.getAbsolutePath() + ".bak");
		File newFile = fileService.newTemporaryFile("newWraper.conf");

		try {
			BufferedWriter newWriter = new BufferedWriter(new FileWriter(newFile));
			String content = FileUtils.readFileToString(originalFile, "UTF-8");
			BufferedReader reader = new BufferedReader(new StringReader(content));
			String line;
			boolean found = false;
			while ((line = reader.readLine()) != null) {
				if (line.startsWith(lineBefore)) {
					found = true;
				} else if (found) {
					newWriter.write(lineToAdd);
					newWriter.newLine();
					found = false;
				}

				newWriter.write(line);
				newWriter.newLine();
			}

			newWriter.flush();
			newWriter.close();

			fileService.copyFile(originalFile, backup);
			fileService.copyFile(newFile, originalFile);

		} catch (IOException ioe) {
			ioe.printStackTrace();
		}
	}
}
