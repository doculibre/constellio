package com.constellio.app.modules.rm.services.menu.behaviors.util;

import com.constellio.app.modules.rm.navigation.RMNavigationConfiguration;

public class RMUrlUtil {
	public static String getPathToConsultLinkForDocument(String id) {
		return "#!" + RMNavigationConfiguration.DISPLAY_DOCUMENT + "/" + id;
	}

	public static String getPathToConsultLinkForFolder(String id) {
		return "#!" + RMNavigationConfiguration.DISPLAY_FOLDER + "/" + id;
	}

	public static String getPathToConsultLinkForContainerRecord(String id) {
		return "#!" + RMNavigationConfiguration.DISPLAY_CONTAINER + "/" + id;
	}
}
