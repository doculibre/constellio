package com.constellio.model.entities.configs;

import com.constellio.model.frameworks.validation.ValidationErrors;
import com.constellio.model.services.factories.ModelLayerFactory;

public abstract class AbstractSystemConfigurationScript<T> implements SystemConfigurationScript<T> {

	@Override
	public void validate(T newValue, ValidationErrors errors) {

	}

	@Override
	public void onValueChanged(T previousValue, T newValue, ModelLayerFactory modelLayerFactory) {

	}

	@Override
	public void onValueChanged(T previousValue, T newValue, ModelLayerFactory modelLayerFactory, String collection) {

	}
}
