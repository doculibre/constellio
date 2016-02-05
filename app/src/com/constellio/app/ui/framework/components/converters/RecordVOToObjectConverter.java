package com.constellio.app.ui.framework.components.converters;

import java.util.Locale;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.constellio.app.services.factories.ConstellioFactories;
import com.constellio.app.ui.entities.RecordVO;
import com.constellio.app.ui.entities.RecordVO.VIEW_MODE;
import com.constellio.app.ui.framework.builders.RecordToVOBuilder;
import com.constellio.model.entities.records.Record;
import com.constellio.model.services.factories.ModelLayerFactory;
import com.constellio.model.services.records.RecordServices;
import com.constellio.model.services.records.RecordServicesRuntimeException.NoSuchRecordWithId;
import com.vaadin.data.util.converter.Converter;

public class RecordVOToObjectConverter implements Converter<RecordVO, Object> {

	private static final Logger LOGGER = LoggerFactory.getLogger(RecordVOToObjectConverter.class);
	
	private VIEW_MODE viewMode;
	
	public RecordVOToObjectConverter(VIEW_MODE viewMode) {
		this.viewMode = viewMode;
	}

	@Override
	public Object convertToModel(RecordVO value, Class<? extends Object> targetType, Locale locale)
			throws com.vaadin.data.util.converter.Converter.ConversionException {
		return value;
	}

	@Override
	public RecordVO convertToPresentation(Object value, Class<? extends RecordVO> targetType, Locale locale)
			throws com.vaadin.data.util.converter.Converter.ConversionException {
		RecordVO recordVO;
		if ((value instanceof String) && StringUtils.isNotBlank((String) value)) {
			ConstellioFactories constellioFactories = ConstellioFactories.getInstance();
			ModelLayerFactory modelLayerFactory = constellioFactories.getModelLayerFactory();
			RecordServices recordServices = modelLayerFactory.newRecordServices();
			try {
				Record record = recordServices.getDocumentById((String) value);
				recordVO = new RecordToVOBuilder().build(record, viewMode);
			} catch (NoSuchRecordWithId e) {
				LOGGER.warn(e.getMessage(), e);
				recordVO = null;
			}
		} else if (value instanceof RecordVO) {
			recordVO = (RecordVO) value;
		} else {
			recordVO = null;
		}
		return recordVO;
	}

	@Override
	public Class<Object> getModelType() {
		return Object.class;
	}

	@Override
	public Class<RecordVO> getPresentationType() {
		return RecordVO.class;
	}

}
