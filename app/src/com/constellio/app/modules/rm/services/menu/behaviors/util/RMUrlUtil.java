package com.constellio.app.modules.rm.services.menu.behaviors.util;

import com.constellio.app.modules.rm.navigation.RMNavigationConfiguration;
import com.constellio.app.ui.application.NavigatorConfigurationService;
import com.constellio.app.ui.pages.management.taxonomy.TaxonomyManagementPresenter;
import com.constellio.app.ui.params.ParamUtils;

import java.util.HashMap;
import java.util.Map;

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

	public static String getPathToConsultLinkForStorageSpace(String id) {
		Map<String, String> params = new HashMap<>();
		params.put(TaxonomyManagementPresenter.TAXONOMY_CODE, "containers");
		params.put(TaxonomyManagementPresenter.CONCEPT_ID, id);
		return "#!" + ParamUtils.addParams(NavigatorConfigurationService.TAXONOMY_MANAGEMENT, params);
	}
}
