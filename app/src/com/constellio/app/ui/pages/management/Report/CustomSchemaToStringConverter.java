package com.constellio.app.ui.pages.management.Report;

import com.constellio.app.services.factories.AppLayerFactory;
import com.constellio.app.services.factories.ConstellioFactories;
import com.constellio.model.entities.schemas.MetadataSchema;
import com.vaadin.data.util.converter.Converter;
import org.apache.commons.lang.StringUtils;

import java.util.Locale;

public class CustomSchemaToStringConverter implements Converter<Object, String> {
    private String collection;
    private AppLayerFactory factory;
    public CustomSchemaToStringConverter(String collection, AppLayerFactory factory){
        this.collection = collection;
        this.factory = factory;
    }

    @Override
    public String convertToModel(Object value, Class<? extends String> targetType, Locale locale) throws ConversionException {
        return value == null ? null : ((MetadataSchema) value).getCode();
    }

    @Override
    public Object convertToPresentation(String value, Class<?> targetType, Locale locale) throws ConversionException {
        return StringUtils.isEmpty(value) ? "" : this.factory.getModelLayerFactory().getMetadataSchemasManager().getSchemaTypes(this.collection).getSchema(value);
    }

    @Override
    public Class<String> getModelType() {
        return null;
    }

    @Override
    public Class<Object> getPresentationType() {
        return null;
    }
}
