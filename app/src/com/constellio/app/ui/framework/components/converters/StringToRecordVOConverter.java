package com.constellio.app.ui.framework.components.converters;

import java.util.Locale;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.constellio.app.services.factories.ConstellioFactories;
import com.constellio.app.ui.application.ConstellioUI;
import com.constellio.app.ui.entities.RecordVO;
import com.constellio.app.ui.entities.RecordVO.VIEW_MODE;
import com.constellio.app.ui.framework.builders.RecordToVOBuilder;
import com.constellio.model.entities.records.Record;
import com.constellio.model.services.factories.ModelLayerFactory;
import com.constellio.model.services.records.RecordServices;
import com.constellio.model.services.records.RecordServicesRuntimeException.NoSuchRecordWithId;
import com.vaadin.data.util.converter.Converter;

public class StringToRecordVOConverter implements Converter<String, RecordVO> {

	private static final Logger LOGGER = LoggerFactory.getLogger(StringToRecordVOConverter.class);

	private VIEW_MODE viewMode;

	public StringToRecordVOConverter(VIEW_MODE viewMode) {
		this.viewMode = viewMode;
	}

	@Override
	public RecordVO convertToModel(String value, Class<? extends RecordVO> targetType, Locale locale)
			throws com.vaadin.data.util.converter.Converter.ConversionException {
		RecordVO recordVO;
		if (StringUtils.isNotBlank(value)) {
			ConstellioFactories constellioFactories = ConstellioFactories.getInstance();
			ModelLayerFactory modelLayerFactory = constellioFactories.getModelLayerFactory();
			RecordServices recordServices = modelLayerFactory.newRecordServices();
			try {
				Record record = recordServices.getDocumentById(value);
				recordVO = new RecordToVOBuilder().build(record, viewMode, ConstellioUI.getCurrentSessionContext());
			} catch (NoSuchRecordWithId e) {
				LOGGER.warn(e.getMessage(), e);
				recordVO = null;
			}
		} else {
			recordVO = null;
		}
		return recordVO;
	}

	@Override
	public String convertToPresentation(RecordVO value, Class<? extends String> targetType, Locale locale)
			throws com.vaadin.data.util.converter.Converter.ConversionException {
		return value != null ? value.getId() : null;
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
