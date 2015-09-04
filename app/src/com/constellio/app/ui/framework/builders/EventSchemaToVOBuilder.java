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

import static com.constellio.app.ui.i18n.i18n.$;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import com.constellio.app.entities.schemasDisplay.enums.MetadataInputType;
import com.constellio.app.ui.entities.MetadataSchemaVO;
import com.constellio.app.ui.entities.MetadataVO;
import com.constellio.model.entities.schemas.AllowedReferences;
import com.constellio.model.entities.schemas.MetadataValueType;
import com.constellio.model.entities.schemas.Schemas;
import com.constellio.model.entities.schemas.StructureFactory;

public class EventSchemaToVOBuilder extends MetadataSchemaToVOBuilder {

	@Override
	protected MetadataToVOBuilder newMetadataToVOBuilder() {
		return new MetadataToVOBuilder() {
			@Override
			protected MetadataVO newMetadataVO(String metadataCode, MetadataValueType type, String collection,
					MetadataSchemaVO schemaVO, boolean required, boolean multivalue, boolean readOnly,
					Map<Locale, String> labels, Class<? extends Enum<?>> enumClass, String[] taxonomyCodes,
					String schemaTypeCode, MetadataInputType metadataInputType, AllowedReferences allowedReferences,
					boolean enabled, StructureFactory structureFactory, String metadataGroup, Object defaultValue) {
				MetadataVO metadataVO;
				String modifiedOnCodeWithoutPrefix = MetadataVO.getCodeWithoutPrefix(Schemas.MODIFIED_ON.getCode());
				String metadataCodeWithoutPrefix = MetadataVO.getCodeWithoutPrefix(metadataCode);
				if (modifiedOnCodeWithoutPrefix.equals(metadataCodeWithoutPrefix)) {
					String newLabel = $("EventSchemaToVOBuilder.modifiedOn");
					Map<Locale, String> newLabels = new HashMap<>();
					for (Locale labelLocale : labels.keySet()) {
						newLabels.put(labelLocale, newLabel);
					}
					metadataVO = super.newMetadataVO(metadataCode, type, collection, schemaVO, required, multivalue, readOnly,
							newLabels, enumClass, taxonomyCodes, schemaTypeCode, metadataInputType, allowedReferences, enabled,
							structureFactory, metadataGroup, defaultValue);
				} else {
					metadataVO = super.newMetadataVO(metadataCode, type, collection, schemaVO, required, multivalue, readOnly,
							labels, enumClass, taxonomyCodes, schemaTypeCode, metadataInputType, allowedReferences, enabled,
							structureFactory, metadataGroup, defaultValue);
				}
				return metadataVO;
			}
		};
	}

}
