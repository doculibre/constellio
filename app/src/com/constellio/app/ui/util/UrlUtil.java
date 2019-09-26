package com.constellio.app.ui.util;

import com.constellio.model.services.factories.ModelLayerFactory;
import com.constellio.model.services.migrations.ConstellioEIMConfigs;

public class UrlUtil {
	public static String getConstellioUrl(ModelLayerFactory modelLayerFactory) {
		return modelLayerFactory.getSystemConfigurationsManager().getValue(ConstellioEIMConfigs.CONSTELLIO_URL);
	}
}
