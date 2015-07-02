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
package com.constellio.app.modules.rm.services;

import static com.constellio.app.ui.i18n.i18n.$;

import com.constellio.app.modules.rm.wrappers.HierarchicalValueListItem;
import com.constellio.app.modules.rm.wrappers.ValueListItem;
import com.constellio.app.modules.rm.wrappers.structures.CommentFactory;
import com.constellio.model.entities.schemas.MetadataValueType;
import com.constellio.model.entities.schemas.Schemas;
import com.constellio.model.services.schemas.builders.MetadataBuilder;
import com.constellio.model.services.schemas.builders.MetadataSchemaBuilder;
import com.constellio.model.services.schemas.builders.MetadataSchemaTypeBuilder;
import com.constellio.model.services.schemas.builders.MetadataSchemaTypesBuilder;

public class ValueListItemSchemaTypeBuilder {

	public enum ValueListItemSchemaTypeCodeMode {REQUIRED_AND_UNIQUE, FACULTATIVE, DISABLED}

	MetadataSchemaTypesBuilder metadataSchemaTypesBuilder;

	public ValueListItemSchemaTypeBuilder(
			MetadataSchemaTypesBuilder metadataSchemaTypesBuilder) {
		this.metadataSchemaTypesBuilder = metadataSchemaTypesBuilder;
	}

	public MetadataSchemaTypeBuilder createValueListItemSchema(String code, String label,
			ValueListItemSchemaTypeCodeMode codeMode) {
		MetadataSchemaTypeBuilder typeBuilder = metadataSchemaTypesBuilder.createNewSchemaType(code);
		typeBuilder.setLabel(label);
		typeBuilder.setSecurity(false);
		MetadataSchemaBuilder defaultSchemaBuilder = typeBuilder.getDefaultSchema();

		defaultSchemaBuilder.getMetadata(Schemas.TITLE_CODE).setUniqueValue(true).setDefaultRequirement(true);

		MetadataBuilder codeMetadata = defaultSchemaBuilder.create(ValueListItem.CODE).setType(
				MetadataValueType.STRING).setSearchable(true).setUndeletable(true).setSchemaAutocomplete(true);
		codeMetadata.setLabel($("init.valuelist.default.code"));
		codeMetadata.setSchemaAutocomplete(true);
		if (codeMode == ValueListItemSchemaTypeCodeMode.REQUIRED_AND_UNIQUE) {
			codeMetadata.setUniqueValue(true).setDefaultRequirement(true);

		} else if (codeMode == ValueListItemSchemaTypeCodeMode.DISABLED) {
			codeMetadata.setEnabled(false);
		}

		defaultSchemaBuilder.create(ValueListItem.DESCRIPTION).setType(MetadataValueType.TEXT).setSearchable(true)
				.setUndeletable(true).setLabel($("init.valuelist.default.description"));
		defaultSchemaBuilder.create(ValueListItem.COMMENTS).setMultivalue(true)
				.setType(MetadataValueType.ENUM).defineStructureFactory(CommentFactory.class);

		defaultSchemaBuilder.getMetadata(Schemas.TITLE.getLocalCode()).setSearchable(true).setLabel(
				$("init.valuelist.default.title"));
		return typeBuilder;
	}

	public MetadataSchemaTypeBuilder createHierarchicalValueListItemSchema(String code, String label,
			ValueListItemSchemaTypeCodeMode codeMode) {
		MetadataSchemaTypeBuilder typeBuilder = createValueListItemSchema(code, label, codeMode);
		MetadataSchemaBuilder defaultSchemaBuilder = typeBuilder.getDefaultSchema();
		defaultSchemaBuilder.create(HierarchicalValueListItem.PARENT).defineChildOfRelationshipToType(typeBuilder)
				.setUndeletable(true).setLabel($("init.valuelist.default.parent"));

		return typeBuilder;
	}

}
