package com.constellio.app.ui.framework.components.converters;

import java.util.Locale;

import org.apache.commons.lang3.StringUtils;

import com.constellio.app.services.factories.ConstellioFactories;
import com.constellio.model.entities.records.wrappers.Collection;
import com.vaadin.data.util.converter.Converter;

public class CollectionCodeToLabelConverter implements Converter<String, String> {

	@Override
	public String convertToModel(String value, Class<? extends String> targetType, Locale locale)
			throws com.vaadin.data.util.converter.Converter.ConversionException {
		return value;
	}

	@Override
	public String convertToPresentation(String value, Class<? extends String> targetType, Locale locale)
			throws com.vaadin.data.util.converter.Converter.ConversionException {
		String caption;
		if (StringUtils.isNotBlank(value)) {
			caption = getCollectionCaption(value);
		} else {
			caption = "";
		}
		return caption;
	}

	public String getCollectionCaption(String collectionCode) {
		ConstellioFactories constellioFactories = ConstellioFactories.getInstance();
		Collection collection = constellioFactories.getAppLayerFactory().getCollectionsManager().getCollection(collectionCode); 
		String collectionTitle = collection.getTitle();
		String collectionName = collection.getName();
		return StringUtils.isNotBlank(collectionTitle) ? collectionTitle : collectionName;
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
