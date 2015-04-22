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
package com.constellio.model.services.schemas.builders;

import java.util.HashSet;
import java.util.Set;

import com.constellio.model.entities.schemas.AllowedReferences;
import com.constellio.model.entities.schemas.MetadataSchema;

public class AllowedReferencesBuilder {

	private final Set<String> schemas = new HashSet<String>();
	private String schemaType;

	public AllowedReferencesBuilder() {
		super();
	}

	public AllowedReferencesBuilder(AllowedReferences references) {
		super();
		schemaType = references.getAllowedSchemaType();
		schemas.addAll(references.getAllowedSchemas());
	}

	public String getSchemaType() {
		return schemaType;
	}

	public Set<String> getSchemas() {
		return schemas;
	}

	public AllowedReferencesBuilder add(MetadataSchema schema) {
		addCompleteSchemaCode(schema.getCode());
		return this;
	}

	public AllowedReferencesBuilder add(MetadataSchemaBuilder schemaBuilder) {
		addCompleteSchemaCode(schemaBuilder.getCode());
		return this;
	}

	public AllowedReferencesBuilder set(MetadataSchemaTypeBuilder schemaTypeBuilder) {
		setCompleteSchemaTypeCode(schemaTypeBuilder.getCode());
		return this;
	}

	public AllowedReferencesBuilder setCompleteSchemaTypeCode(String code) {
		if (schemaType != null) {
			throw new AllowedReferencesBuilderRuntimeException.SchemaTypeAlreadySet();
		} else if (!schemas.isEmpty()) {
			throw new AllowedReferencesBuilderRuntimeException.CannotHaveBothATypeAndSchemas();
		} else {
			schemaType = code;
		}
		return this;
	}

	public AllowedReferencesBuilder addCompleteSchemaCode(String code) {
		if (schemaType != null) {
			throw new AllowedReferencesBuilderRuntimeException.CannotHaveBothATypeAndSchemas();
		} else if (!isOfSameType(code)) {
			throw new AllowedReferencesBuilderRuntimeException.AllSchemasMustBeOfSameType();
		} else {
			schemas.add(code);
		}
		return this;
	}

	private boolean isOfSameType(String code) {
		boolean isOfSameType = true;
		for (String schemaCode : schemas) {
			if (!(schemaCode.split("_")[0].equals(code.split("_")[0]))) {
				isOfSameType = false;
			}
		}
		return isOfSameType;
	}

	public AllowedReferences build() {
		return new AllowedReferences(schemaType, schemas);
	}

	@Override
	public String toString() {
		return "AllowedReferencesBuilder [schemaTypes=" + schemaType + ", schemas=" + schemas + "]";
	}

	public String getMetadataCompleteCode(String simpleCode) {
		if (schemaType != null) {
			return schemaType + "_default_" + simpleCode;
		} else {
			return schemas.iterator().next() + "_" + simpleCode;
		}
	}

}
