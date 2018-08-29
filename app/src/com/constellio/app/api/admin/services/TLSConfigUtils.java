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

	public static void setAdditionalSettings(File originalFile, FileService fileService) {
		File backup = new File(originalFile.getAbsolutePath() + ".bak");
		File newFile = fileService.newTemporaryFile("newWraper.conf");

		try {
			BufferedWriter newWriter = new BufferedWriter(new FileWriter(newFile));
			String content = FileUtils.readFileToString(originalFile, "UTF-8");
			BufferedReader reader = new BufferedReader(new StringReader(content));
			String line;
			boolean found = false;
			while ((line = reader.readLine()) != null) {
				if (line.startsWith("wrapper.java.additional.1")) {
					found = true;
				} else if (found) {
					newWriter.write("wrapper.java.additional.2=-Djdk.tls.ephemeralDHKeySize=2048");
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
