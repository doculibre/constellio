package com.constellio.app.ui.framework.components.converters;

import com.constellio.app.ui.entities.RecordVO;
import com.constellio.app.ui.util.SchemaCaptionUtils;
import com.vaadin.data.util.converter.Converter;

import java.util.Locale;

public class RecordVOToCaptionConverter implements Converter<String, RecordVO> {

	@Override
	public RecordVO convertToModel(String value, Class<? extends RecordVO> targetType, Locale locale)
			throws com.vaadin.data.util.converter.Converter.ConversionException {
		throw new UnsupportedOperationException("Cannot conver a caption to a RecordVO");
	}

	@Override
	public String convertToPresentation(RecordVO value, Class<? extends String> targetType, Locale locale)
			throws com.vaadin.data.util.converter.Converter.ConversionException {
		String caption;
		if (value != null) {
			caption = SchemaCaptionUtils.getCaptionForRecordVO(value, locale, true);
		} else {
			caption = "";
		}
		return caption;
	}

	@Override
	public Class<RecordVO> getModelType() {
		return RecordVO.class;
	}

	@Override
	public Class<String> getPresentationType() {
		return String.class;
	}


}
