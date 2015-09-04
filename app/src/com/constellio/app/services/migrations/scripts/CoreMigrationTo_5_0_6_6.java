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
package com.constellio.app.services.migrations.scripts;

import com.constellio.app.entities.modules.MigrationResourcesProvider;
import com.constellio.app.entities.modules.MigrationScript;
import com.constellio.app.entities.schemasDisplay.SchemaDisplayConfig;
import com.constellio.app.services.factories.AppLayerFactory;
import com.constellio.app.services.schemasDisplay.SchemasDisplayManager;
import com.constellio.model.entities.schemas.MetadataSchema;
import com.constellio.model.entities.schemas.MetadataSchemaType;
import com.constellio.model.entities.schemas.MetadataSchemaTypes;
import com.constellio.model.entities.schemas.Schemas;

public class CoreMigrationTo_5_0_6_6 implements MigrationScript {

	@Override
	public String getVersion() {
		return "5.0.6.6";
	}

	@Override
	public void migrate(String collection, MigrationResourcesProvider migrationResourcesProvider,
			AppLayerFactory appLayerFactory) {

		SchemasDisplayManager manager = appLayerFactory.getMetadataSchemasDisplayManager();

		MetadataSchemaTypes types = appLayerFactory.getModelLayerFactory().getMetadataSchemasManager().getSchemaTypes(collection);
		for (MetadataSchemaType type : types.getSchemaTypes()) {
			for (MetadataSchema schema : type.getCustomSchemas()) {
				SchemaDisplayConfig schemaConfig = manager.getSchema(collection, schema.getCode());
				if (schemaConfig.getDisplayMetadataCodes().contains(schema.getCode() + "_" + Schemas.TOKENS.getLocalCode())) {
					manager.resetSchema(collection, schema.getCode());
				}
			}
		}

	}

}


