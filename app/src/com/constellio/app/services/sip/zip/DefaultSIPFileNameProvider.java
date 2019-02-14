package com.constellio.app.services.sip.zip;

import java.io.File;

public class DefaultSIPFileNameProvider implements SIPFileNameProvider {

	private File folder;

	private String sipName;

	public DefaultSIPFileNameProvider(File folder, String sipName) {
		this.folder = folder;
		this.sipName = sipName;
	}

	@Override
	public File newSIPFile(int index) {
		return new File(folder, newSIPName(index) + ".zip");
	}

	@Override
	public String newSIPName(int index) {
		return String.format("%s-%03d", sipName, index);
	}

}
