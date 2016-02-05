package com.constellio.sdk.tests;

import java.io.File;
import java.io.Reader;

import org.openqa.selenium.firefox.FirefoxProfile;

public class ZeUltimateFirefoxProfile extends FirefoxProfile {

	public ZeUltimateFirefoxProfile() {
	}

	public ZeUltimateFirefoxProfile(File profileDir) {
		super(profileDir);
	}

	public ZeUltimateFirefoxProfile(Reader defaultsReader, File profileDir) {
		super(defaultsReader, profileDir);
	}

	//ultimate mode!
	public void cleanTemporaryModel() {
		super.cleanTemporaryModel();
	}
}
