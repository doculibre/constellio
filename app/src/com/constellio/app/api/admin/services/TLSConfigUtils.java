package com.constellio.app.api.admin.services;

import com.constellio.data.io.services.facades.FileService;
import org.apache.commons.io.FileUtils;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.StringReader;

public class TLSConfigUtils {

	// Should not be used without changing parameter number because we already have a second item.
	@Deprecated
	public static void setSettingAdditional2EphemeralDHKeySize(File originalFile, FileService fileService) {
		setSetting(originalFile, fileService, "wrapper.java.additional.1", "wrapper.java.additional.2=-Djdk.tls.ephemeralDHKeySize=2048");
	}

	public static void setSettingAdditional2TemporaryDirectory(File originalFile, FileService fileService) {
		setSetting(originalFile, fileService, "wrapper.java.additional.1", "wrapper.java.additional.2=-Djava.io.tmpdir=/opt/constellio_tmp");
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
