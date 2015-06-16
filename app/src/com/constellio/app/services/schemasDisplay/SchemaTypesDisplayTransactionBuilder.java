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
package com.constellio.app.services.schemasDisplay;

import com.constellio.app.entities.schemasDisplay.SchemaDisplayConfig;
import com.constellio.model.entities.schemas.MetadataSchema;
import com.constellio.model.entities.schemas.MetadataSchemaTypes;

public class SchemaTypesDisplayTransactionBuilder {

	MetadataSchemaTypes types;
	SchemasDisplayManager schemasDisplayManager;

	SchemaDisplayManagerTransaction transaction;

	public SchemaTypesDisplayTransactionBuilder(MetadataSchemaTypes types, SchemasDisplayManager schemasDisplayManager) {
		this.transaction = new SchemaDisplayManagerTransaction();
		this.types = types;
		this.schemasDisplayManager = schemasDisplayManager;
	}

	public SchemaTypeDisplayTransactionBuilder in(String typeCode) {
		return new SchemaTypeDisplayTransactionBuilder(types.getSchemaType(typeCode), schemasDisplayManager, this);
	}

	public SchemaDisplayManagerTransaction build() {
		return transaction;
	}

	public void updateAllSchemas(String typeCode, SchemaDisplayAlteration schemaDisplayAlteration) {
		for (MetadataSchema schema : types.getSchemaType(typeCode).getAllSchemas()) {
			SchemaDisplayConfig config = updateSchemaDisplayConfig(schema);

			SchemaDisplayConfig modifiedConfig = schemaDisplayAlteration.alter(config);
			if (modifiedConfig != config) {
				transaction.add(modifiedConfig);
			}
		}
	}

	public SchemaDisplayConfig updateSchemaDisplayConfig(MetadataSchema schema) {
		for (SchemaDisplayConfig config : transaction.modifiedSchemas) {
			if (config.getSchemaCode().equals(schema.getCode())) {
				return config;
			}
		}
		return schemasDisplayManager.getSchema(types.getCollection(), schema.getCode());
	}
}
