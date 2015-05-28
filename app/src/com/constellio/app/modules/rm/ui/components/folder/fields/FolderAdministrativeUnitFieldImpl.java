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
package com.constellio.app.modules.rm.ui.components.folder.fields;

import java.util.List;

import com.constellio.app.ui.framework.components.converters.RecordIdToCaptionConverter;
import com.constellio.app.ui.util.EventUtils;
import com.vaadin.ui.ComboBox;

public class FolderAdministrativeUnitFieldImpl extends ComboBox implements FolderAdministrativeUnitField {

	private RecordIdToCaptionConverter captionConverter = new RecordIdToCaptionConverter();

	@Override
	public String getItemCaption(Object itemId) {
		return captionConverter.convertToPresentation((String) itemId, String.class, getLocale());
	}

	@Override
	public String getFieldValue() {
		return (String) getConvertedValue();
	}

	@Override
	public void setFieldValue(Object value) {
		setInternalValue(value);
	}

	@SuppressWarnings("unchecked")
	@Override
	public List<String> getOptions() {
		return (List<String>) getItemIds();
	}

	@Override
	public void setOptions(List<String> options) {
		EventUtils.setOptionsWithoutNotifyingListeners(this, options);
	}

}
