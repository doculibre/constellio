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

import com.constellio.app.entities.modules.MetadataSchemasAlterationHelper;
import com.constellio.app.entities.modules.MigrationResourcesProvider;
import com.constellio.app.entities.modules.MigrationScript;
import com.constellio.app.services.factories.AppLayerFactory;
import com.constellio.model.entities.records.wrappers.User;
import com.constellio.model.services.schemas.builders.MetadataSchemaTypesBuilder;

public class CoreMigrationTo_5_0_5 implements MigrationScript {

	@Override
	public String getVersion() {
		return "5.0.5";
	}

	@Override
	public void migrate(String collection, MigrationResourcesProvider migrationResourcesProvider,
			AppLayerFactory appLayerFactory) {
		new CoreSchemaAlterationFor5_0_4(collection, migrationResourcesProvider, appLayerFactory).migrate();
	}

	class CoreSchemaAlterationFor5_0_4 extends MetadataSchemasAlterationHelper {

		protected CoreSchemaAlterationFor5_0_4(String collection,
				MigrationResourcesProvider migrationResourcesProvider, AppLayerFactory appLayerFactory) {
			super(collection, migrationResourcesProvider, appLayerFactory);
		}

		@Override
		protected void migrate(MetadataSchemaTypesBuilder typesBuilder) {
			addMetadataToUserSchema(typesBuilder);
		}

		private void addMetadataToUserSchema(MetadataSchemaTypesBuilder typesBuilder) {
			typesBuilder.getSchema(User.DEFAULT_SCHEMA).getMetadata(User.EMAIL).setUniqueValue(false);
		}
	}
}
