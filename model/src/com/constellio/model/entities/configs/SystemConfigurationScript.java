package com.constellio.model.entities.configs;

import com.constellio.model.frameworks.validation.ValidationErrors;
import com.constellio.model.services.factories.ModelLayerFactory;

public interface SystemConfigurationScript<T> {

	void validate(T newValue, ValidationErrors errors);

	void onValueChanged(T previousValue, T newValue, ModelLayerFactory modelLayerFactory);

	void onValueChanged(T previousValue, T newValue, ModelLayerFactory modelLayerFactory, String collection);

}
