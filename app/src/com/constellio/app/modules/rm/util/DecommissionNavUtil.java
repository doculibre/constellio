package com.constellio.app.modules.rm.util;

import java.util.Map;

import com.constellio.app.services.factories.AppLayerFactory;
import com.constellio.model.services.migrations.ConstellioEIMConfigs;
import com.jgoodies.common.base.Strings;

public class DecommissionNavUtil {
	public static boolean areTypeAndSearchIdPresent(Map<String, String> params) {
		if (params == null) {
			return false;
		}
		return Strings.isNotBlank(params.get("decommissioningType")) && Strings.isNotBlank(params.get("decommissioningSearchId"));
	}

	public static String getSearchId(Map<String, String> params) {
		if (params == null) {
			return null;
		}

		return params.get("decommissioningSearchId");
	}

	public static String getSearchType(Map<String, String> params) {
		if (params == null) {
			return null;
		}

		return params.get("decommissioningType");
	}

	public static String getHomeUri(AppLayerFactory appLayerFactory) {
		ConstellioEIMConfigs configs = new ConstellioEIMConfigs(appLayerFactory.getModelLayerFactory());

		return configs.getConstellioUrl();
	}
}
