package com.constellio.app.services.migrations.scripts;

import com.constellio.app.entities.modules.MetadataSchemasAlterationHelper;
import com.constellio.app.entities.modules.MigrationResourcesProvider;
import com.constellio.app.entities.modules.MigrationScript;
import com.constellio.app.services.factories.AppLayerFactory;
import com.constellio.model.entities.records.wrappers.Group;
import com.constellio.model.entities.records.wrappers.RecordAuthorization;
import com.constellio.model.services.records.SchemasRecordsServices;
import com.constellio.model.services.schemas.builders.MetadataSchemaTypesBuilder;
import com.constellio.model.services.schemas.calculators.GroupAncestorsCalculator;
import com.constellio.model.services.search.SearchServices;
import org.apache.poi.ss.formula.functions.T;

import static com.constellio.model.entities.schemas.MetadataValueType.BOOLEAN;
import static com.constellio.model.entities.schemas.MetadataValueType.STRING;
import static com.constellio.model.services.search.query.logical.LogicalSearchQueryOperators.from;

public class CoreMigrationTo_8_1_2 implements MigrationScript {
	@Override
	public String getVersion() {
		return "8.1.2";
	}

	@Override
	public void migrate(String collection, MigrationResourcesProvider migrationResourcesProvider,
						AppLayerFactory appLayerFactory)
			throws Exception {
		new CoreSchemaAlterationFor_8_1_2(collection, migrationResourcesProvider, appLayerFactory).migrate();

		SearchServices searchServices = appLayerFactory.getModelLayerFactory().newSearchServices();

		SchemasRecordsServices schemas = new SchemasRecordsServices(collection, appLayerFactory.getModelLayerFactory());
		if (searchServices.hasResults(from(schemas.group.schemaType()).where(schemas.group.parent()).<T>isNotNull())) {
			appLayerFactory.getSystemGlobalConfigsManager().setReindexingRequired(true);
		}
	}

	class CoreSchemaAlterationFor_8_1_2 extends MetadataSchemasAlterationHelper {

		protected CoreSchemaAlterationFor_8_1_2(String collection,
												MigrationResourcesProvider migrationResourcesProvider,
												AppLayerFactory appLayerFactory) {
			super(collection, migrationResourcesProvider, appLayerFactory);
		}

		@Override
		protected void migrate(MetadataSchemaTypesBuilder typesBuilder) {
			typesBuilder.getSchema(RecordAuthorization.DEFAULT_SCHEMA).create(RecordAuthorization.NEGATIVE)
					.setType(BOOLEAN);

			typesBuilder.getSchema(Group.DEFAULT_SCHEMA).create(Group.ANCESTORS)
					.setType(STRING).setMultivalue(true).defineDataEntry().asCalculated(GroupAncestorsCalculator.class);
		}
	}
}
