package com.constellio.app.services.migrations.scripts;

import com.constellio.app.entities.modules.MetadataSchemasAlterationHelper;
import com.constellio.app.entities.modules.MigrationResourcesProvider;
import com.constellio.app.entities.modules.MigrationScript;
import com.constellio.app.services.factories.AppLayerFactory;
import com.constellio.model.entities.records.wrappers.Group;
import com.constellio.model.entities.records.wrappers.SolrAuthorizationDetails;
import com.constellio.model.services.schemas.builders.MetadataSchemaTypesBuilder;
import com.constellio.model.services.schemas.calculators.GroupAncestorsCalculator;

import static com.constellio.model.entities.schemas.MetadataValueType.BOOLEAN;
import static com.constellio.model.entities.schemas.MetadataValueType.STRING;

public class CoreMigrationTo_8_1_2 implements MigrationScript {
	@Override
	public String getVersion() {
		return "8.1.2";
	}

	@Override
	public void migrate(String collection, MigrationResourcesProvider migrationResourcesProvider,
						AppLayerFactory appLayerFactory)
			throws Exception {
		new CoreSchemaAlterationFor_8_0(collection, migrationResourcesProvider, appLayerFactory).migrate();
	}

	class CoreSchemaAlterationFor_8_0 extends MetadataSchemasAlterationHelper {

		protected CoreSchemaAlterationFor_8_0(String collection, MigrationResourcesProvider migrationResourcesProvider,
											  AppLayerFactory appLayerFactory) {
			super(collection, migrationResourcesProvider, appLayerFactory);
		}

		@Override
		protected void migrate(MetadataSchemaTypesBuilder typesBuilder) {
			typesBuilder.getSchema(SolrAuthorizationDetails.DEFAULT_SCHEMA).create(SolrAuthorizationDetails.NEGATIVE)
					.setType(BOOLEAN);

			typesBuilder.getSchema(Group.DEFAULT_SCHEMA).create(Group.ANCESTORS)
					.setType(STRING).setMultivalue(true).defineDataEntry().asCalculated(GroupAncestorsCalculator.class);
		}
	}
}
