package com.constellio.model.services.configs;

import com.constellio.model.entities.configs.SystemConfigurationScript;
import com.constellio.model.frameworks.validation.ValidationErrors;
import com.constellio.model.services.factories.ModelLayerFactory;

public class EnableThumbnailsScript implements SystemConfigurationScript<Boolean> {

	@Override
	public void onNewCollection(Boolean newValue, String collection, ModelLayerFactory modelLayerFactory) {
		//Nothing
	}

	@Override
	public void validate(Boolean newValue, ValidationErrors errors) {

	}

	@Override
	public void onValueChanged(Boolean previousValue, Boolean newValue, ModelLayerFactory modelLayerFactory) {

	}

	@Override
	public void onValueChanged(Boolean previousValue, Boolean newValue, ModelLayerFactory modelLayerFactory,
							   String collection) {

		Thread thread = new Thread() {
			@Override
			public void run() {
				if (Boolean.TRUE.equals(newValue) && Boolean.FALSE.equals(previousValue)) {
					new MarkForPreviewConversionFlagEnabler(collection, modelLayerFactory).enable();
				}
			}
		};
		thread.start();

	}

}
