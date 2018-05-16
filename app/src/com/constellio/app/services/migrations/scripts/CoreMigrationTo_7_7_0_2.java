package com.constellio.app.services.migrations.scripts;

import com.constellio.app.entities.modules.MetadataSchemasAlterationHelper;
import com.constellio.app.entities.modules.MigrationResourcesProvider;
import com.constellio.app.entities.modules.MigrationScript;
import com.constellio.app.services.factories.AppLayerFactory;
import com.constellio.data.dao.services.records.DataStore;
import com.constellio.model.entities.records.wrappers.BatchProcessReport;
import com.constellio.model.entities.records.wrappers.SearchEvent;
import com.constellio.model.entities.records.wrappers.SolrAuthorizationDetails;
import com.constellio.model.entities.records.wrappers.structure.ScriptReport;
import com.constellio.model.entities.schemas.MetadataValueType;
import com.constellio.model.services.schemas.builders.MetadataSchemaBuilder;
import com.constellio.model.services.schemas.builders.MetadataSchemaTypeBuilder;
import com.constellio.model.services.schemas.builders.MetadataSchemaTypesBuilder;

import static com.constellio.model.entities.records.wrappers.SolrAuthorizationDetails.OVERRIDE_INHERITED;
import static com.constellio.model.entities.schemas.MetadataValueType.*;

public class CoreMigrationTo_7_7_0_2 implements MigrationScript {
	@Override
	public String getVersion() {
		return "7.7.0.2";
	}

	@Override
	public void migrate(String collection, MigrationResourcesProvider migrationResourcesProvider, AppLayerFactory appLayerFactory)
			throws Exception {
		new CoreSchemaAlterationFor_7_7_0_2(collection, migrationResourcesProvider, appLayerFactory).migrate();
	}

	class CoreSchemaAlterationFor_7_7_0_2 extends MetadataSchemasAlterationHelper {

		protected CoreSchemaAlterationFor_7_7_0_2(String collection,
												  MigrationResourcesProvider migrationResourcesProvider,
												  AppLayerFactory appLayerFactory) {
			super(collection, migrationResourcesProvider, appLayerFactory);
		}

		@Override
		protected void migrate(MetadataSchemaTypesBuilder typesBuilder) {
			MetadataSchemaTypeBuilder searchEvent = typesBuilder.getSchemaType(SearchEvent.SCHEMA_TYPE);
			if (!searchEvent.getDefaultSchema().hasMetadata(SearchEvent.DWELL_TIME)) {
				searchEvent.createMetadata(SearchEvent.DWELL_TIME).setType(NUMBER).setDefaultValue(0);
			}
		}
	}
}
