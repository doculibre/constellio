package com.constellio.app.ui.framework.components.converters;

import com.constellio.app.services.factories.ConstellioFactories;
import com.constellio.model.entities.records.Record;
import com.constellio.model.entities.schemas.Schemas;
import com.constellio.model.services.factories.ModelLayerFactory;
import com.constellio.model.services.records.RecordServices;
import com.constellio.model.services.records.RecordServicesRuntimeException.NoSuchRecordWithId;
import com.vaadin.data.util.converter.Converter;
import org.apache.commons.lang3.StringUtils;

import java.util.Locale;

public class RecordIdToDescriptionContent implements Converter<String, String> {
	@Override
	public String convertToModel(String value, Class<? extends String> targetType, Locale locale)
			throws ConversionException {
		return value;
	}

	@Override
	public String convertToPresentation(String recordId, Class<? extends String> targetType, Locale locale)
			throws ConversionException {
		String description = "";

		if (StringUtils.isNotBlank(recordId)) {
			ConstellioFactories constellioFactories = ConstellioFactories.getInstance();
			ModelLayerFactory modelLayerFactory = constellioFactories.getModelLayerFactory();
			RecordServices recordServices = modelLayerFactory.newRecordServices();
			try {
				Record record = recordServices.getDocumentById(recordId);

				description = record.get(Schemas.DESCRIPTION_STRING);
				if (description == null) {
					description = record.get(Schemas.DESCRIPTION_TEXT);
				}
			} catch (NoSuchRecordWithId e) {
				description = "";
			}
		} else {
			description = "";
		}
		return description;
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
