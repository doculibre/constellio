package com.constellio.app.ui.framework.components.converters;

import com.constellio.app.ui.application.ConstellioUI;
import com.constellio.app.ui.pages.base.SessionContext;
import com.constellio.app.ui.util.TaxonomyCaptionUtils;
import com.constellio.model.entities.Language;
import com.vaadin.data.util.converter.Converter;
import org.apache.commons.lang3.StringUtils;

import java.util.Locale;

public class TaxonomyCodeToCaptionConverter implements Converter<String, String> {

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
			SessionContext sessionContext = ConstellioUI.getCurrentSessionContext();
			String collection = sessionContext.getCurrentCollection();
			caption = TaxonomyCaptionUtils.getCaptionForTaxonomyCode(collection, value, Language.withCode(locale.getLanguage()));
		} else {
			caption = "";
		}
		return caption;
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
