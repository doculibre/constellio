package com.constellio.app.ui.framework.components.converters;

import com.vaadin.data.util.converter.Converter;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class ConverterWithCache<PRESENTATION, MODEL> implements Converter<PRESENTATION, MODEL> {

	Locale locale;

	Map<MODEL, PRESENTATION> cache = new HashMap<>();

	Converter<PRESENTATION, MODEL> converter;

	public ConverterWithCache(Converter<PRESENTATION, MODEL> converter) {
		this.converter = converter;
	}

	private void ensureSameLocale(Locale locale) {
		if (this.locale != null && locale != null && !this.locale.getISO3Language().equals(locale.getISO3Language())) {
			cache.clear();
		}
		this.locale = locale;
	}

	public void preload(MODEL model, PRESENTATION presentation) {
		cache.put(model, presentation);
	}

	public MODEL convertToModel(PRESENTATION value, Class<? extends MODEL> targetType, Locale locale)
			throws ConversionException {

		ensureSameLocale(locale);
		List<MODEL> cachedModels = new ArrayList<>();
		for (Map.Entry<MODEL, PRESENTATION> entry : cache.entrySet()) {
			if (value != null && value.equals(entry.getValue())) {
				cachedModels.add(entry.getKey());
			}
		}

		if (cachedModels.size() == 1) {
			return cachedModels.get(0);
		} else {
			return converter.convertToModel(value, targetType, locale);
		}
	}

	public PRESENTATION convertToPresentation(MODEL value, Class<? extends PRESENTATION> targetType, Locale locale)
			throws ConversionException {

		ensureSameLocale(locale);

		PRESENTATION cachedPresentation = cache.get(value);

		if (cachedPresentation == null) {
			cachedPresentation = converter.convertToPresentation(value, targetType, locale);
		}
		cache.put(value, cachedPresentation);

		return cachedPresentation;
	}

	public Class<MODEL> getModelType() {
		return converter.getModelType();
	}

	public Class<PRESENTATION> getPresentationType() {
		return converter.getPresentationType();
	}
}
