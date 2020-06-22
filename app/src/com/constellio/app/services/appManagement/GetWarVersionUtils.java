package com.constellio.app.services.appManagement;

import com.constellio.app.utils.GradleFileVersionParser;
import com.constellio.data.conf.FoldersLocator;
import com.constellio.data.conf.FoldersLocatorRuntimeException;
import org.apache.commons.lang3.StringUtils;

import java.io.File;

public class GetWarVersionUtils {

	public static String getWarVersionUsingGradleAsFallback(File webAppFolder) {
		String currentWarVersion = getWarVersion(webAppFolder);
		if (currentWarVersion == null || currentWarVersion.equals("5.0.5")) {
			currentWarVersion = GradleFileVersionParser.getVersion();
		}
		return currentWarVersion;
	}

	public static String getWarVersion(File webAppFolder) {
		FoldersLocator foldersLocator = new FoldersLocator();
		try {
			File webappLibs;
			if (webAppFolder == null) {
				webappLibs = foldersLocator.getLibFolder();
			} else {
				webappLibs = foldersLocator.getLibFolder(webAppFolder);
			}

			if (webappLibs.exists()) {
				for (File lib : webappLibs.listFiles()) {
					if (lib.getName().startsWith("core-model-") && lib.getName().endsWith(".jar")) {
						return lib.getName().replace("core-model-", "").replace(".jar", "");
					}
				}
			}
		} catch (FoldersLocatorRuntimeException.NotAvailableInGitMode e) {
		}

		return "5.0.0";
	}

	public static String getWarVersionFromFileName(File webAppFolder) {
		String folderName = webAppFolder.getName();
		if (folderName.startsWith("webapp-")) {
			return StringUtils.substringAfter(folderName, "webapp-");

		} else {
			return getWarVersion(webAppFolder);
		}
	}

}
