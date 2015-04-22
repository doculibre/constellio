/*Constellio Enterprise Information Management

Copyright (c) 2015 "Constellio inc."

This program is free software: you can redistribute it and/or modify
it under the terms of the GNU Affero General Public License as
published by the Free Software Foundation, either version 3 of the
License, or (at your option) any later version.

This program is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
GNU Affero General Public License for more details.

You should have received a copy of the GNU Affero General Public License
along with this program. If not, see <http://www.gnu.org/licenses/>.
*/
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
				recordVO = new RecordToVOBuilder().build(record, viewMode);
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
