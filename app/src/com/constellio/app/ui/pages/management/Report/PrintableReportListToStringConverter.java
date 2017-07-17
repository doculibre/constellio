package com.constellio.app.ui.pages.management.Report;
import com.vaadin.data.util.converter.Converter;
import org.apache.commons.lang.StringUtils;

import java.util.Locale;

public class PrintableReportListToStringConverter implements Converter<Object, String> {

    @Override
    public String convertToModel(Object value, Class<? extends String> targetType, Locale locale) throws ConversionException {
        return value.toString();
    }

    @Override
    public Object convertToPresentation(String value, Class<?> targetType, Locale locale) throws ConversionException {
        return StringUtils.isEmpty(value) ? null : PrintableReportListPossibleType.getValue(value);
    }

    @Override
    public Class<String> getModelType() {
        return String.class;
    }

    @Override
    public Class<Object> getPresentationType() {
        return Object.class;
    }
}