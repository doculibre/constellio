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
package com.constellio.app.ui.framework.builders;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import com.constellio.app.ui.application.ConstellioUI;
import com.constellio.app.ui.entities.MetadataSchemaTypeVO;
import com.constellio.model.entities.schemas.MetadataSchemaType;

@SuppressWarnings("serial")
public class MetadataSchemaTypeToVOBuilder implements Serializable {

	public MetadataSchemaTypeVO build(MetadataSchemaType type) {
		Map<Locale, String> labels = new HashMap<>();
		labels.put(ConstellioUI.getCurrentSessionContext().getCurrentLocale(), type.getLabel());
		return new MetadataSchemaTypeVO(type.getCode(), labels);
	}
}
