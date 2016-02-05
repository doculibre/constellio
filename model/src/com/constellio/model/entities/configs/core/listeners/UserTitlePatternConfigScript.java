package com.constellio.model.entities.configs.core.listeners;

import com.constellio.model.entities.configs.SystemConfigurationScript;
import com.constellio.model.frameworks.validation.ValidationErrors;
import com.constellio.model.services.factories.ModelLayerFactory;

public class UserTitlePatternConfigScript implements SystemConfigurationScript<String> {

	@Override
	public void validate(String newValue, ValidationErrors errors) {
	}

	@Override
	public void onValueChanged(String previousValue, String newValue, ModelLayerFactory modelLayerFactory) {
	}

	@Override
	public void onValueChanged(String previousValue, String newValue, ModelLayerFactory modelLayerFactory, String collection) {
	}
}
