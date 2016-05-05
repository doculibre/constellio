package com.constellio.app.ui.framework.components.converters;

import java.util.Locale;

import org.apache.commons.lang3.StringUtils;

import com.constellio.app.ui.application.ConstellioUI;
import com.constellio.app.ui.pages.base.SessionContext;
import com.constellio.model.entities.Language;
import com.constellio.model.entities.schemas.Metadata;
import com.constellio.model.entities.schemas.MetadataSchemaTypes;
import com.constellio.model.services.factories.ModelLayerFactory;
import com.constellio.model.services.schemas.MetadataSchemasManager;
import com.vaadin.data.util.converter.Converter;

public class MetadataCodeToStringConverter implements Converter<String, String> {

	@Override
	public String convertToModel(String value, Class<? extends String> targetType, Locale locale)
			throws com.vaadin.data.util.converter.Converter.ConversionException {
		return value;
	}

	@Override
	public String convertToPresentation(String value, Class<? extends String> targetType, Locale locale)
			throws com.vaadin.data.util.converter.Converter.ConversionException {
		String label;
		if (StringUtils.isNotBlank(value)) {
			SessionContext sessionContext = ConstellioUI.getCurrentSessionContext();
			String collection = sessionContext.getCurrentCollection();
			// TODO Cleaner way
			ModelLayerFactory modelLayerFactory = ConstellioUI.getCurrent().getConstellioFactories().getModelLayerFactory();
			MetadataSchemasManager metadataSchemasManager = modelLayerFactory.getMetadataSchemasManager();
			MetadataSchemaTypes types = metadataSchemasManager.getSchemaTypes(collection);
			Metadata metadata = types.getMetadata(value);
			Language language = Language.withCode(locale.getLanguage());
			label = metadata.getLabel(language);
		} else {
			label = "";
		}
		return label;
	}

	@Override
	public Class<String> getModelType() {
		return String.class;
	}

	@Override
	public Class<String> getPresentationType() {
		return String.class;
	}

}
