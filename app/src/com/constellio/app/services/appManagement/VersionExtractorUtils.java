package com.constellio.app.services.appManagement;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class VersionExtractorUtils {

	public static String getMajorVersion(String version) {
		String cleanedVersion = version
				.replace("beta.", "beta").replace("beta", "beta.")
				.replace("build.", "build").replace("build", "build.")
				.replace("rc.", "rc").replace("rc", "rc.")
				.replace("(LTS)", "").replace("(SLT)", "")
				.replace("LTS", "").replace("SLT", "").replace(" ", "");


		Pattern majorPartOnly = Pattern.compile("\\d*\\.?\\d*");
		Matcher completeVersion = majorPartOnly.matcher(cleanedVersion);
		if (completeVersion.find() && !completeVersion.group().isEmpty()) {
			return completeVersion.group();
		} else {
			return null;
		}
	}

}
