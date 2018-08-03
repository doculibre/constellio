package com.constellio.sdk.tests;

import org.openqa.selenium.firefox.FirefoxProfile;

import java.io.File;
import java.io.Reader;

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
