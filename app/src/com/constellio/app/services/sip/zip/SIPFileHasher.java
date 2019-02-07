package com.constellio.app.services.sip.zip;

import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.io.IOUtils;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;

public class SIPFileHasher {

	public String getFunctionName() {
		return "SHA-256";
	}

	public String computeHash(File input, String sipPath) throws IOException {

		FileInputStream fileInputStream = null;
		try {
			fileInputStream = new FileInputStream(input);
			return DigestUtils.sha256Hex(fileInputStream);

		} finally {
			IOUtils.closeQuietly(fileInputStream);
		}

	}
}
