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

import com.constellio.app.ui.entities.RecordVO;
import com.constellio.app.ui.util.SchemaCaptionUtils;
import com.vaadin.data.util.converter.Converter;

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
			caption = SchemaCaptionUtils.getCaptionForRecordVO(value);
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
