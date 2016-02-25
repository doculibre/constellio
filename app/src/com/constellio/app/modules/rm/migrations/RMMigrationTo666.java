package com.constellio.app.modules.rm.migrations;

import static com.constellio.app.modules.rm.migrations.RMMigrationTo420.add420Record;
import static com.constellio.app.modules.rm.migrations.RMMigrationTo420.createSchema420Type;
import static com.constellio.app.modules.rm.migrations.RMMigrationTo420.deleteAllFacetsExceptOneThatWillBeModified;

import com.constellio.app.entities.modules.MetadataSchemasAlterationHelper;
import com.constellio.app.entities.modules.MigrationResourcesProvider;
import com.constellio.app.entities.modules.MigrationScript;
import com.constellio.app.services.factories.AppLayerFactory;
import com.constellio.model.services.schemas.builders.MetadataSchemaTypesBuilder;

public class RMMigrationTo666 implements MigrationScript {
	@Override
	public String getVersion() {
		return "666.0";
	}

	@Override
	public void migrate(String collection, MigrationResourcesProvider provider, AppLayerFactory appLayerFactory)
			throws Exception {
		new SchemaAlterationsFor666(collection, provider, appLayerFactory).migrate();
		createSchema420Type(collection, appLayerFactory.getModelLayerFactory());

		deleteAllFacetsExceptOneThatWillBeModified(collection, appLayerFactory);
		add420Record(collection, appLayerFactory);
	}

	public static class SchemaAlterationsFor666 extends MetadataSchemasAlterationHelper {

		protected SchemaAlterationsFor666(String collection, MigrationResourcesProvider provider, AppLayerFactory factory) {
			super(collection, provider, factory);
		}

		@Override
		protected void migrate(MetadataSchemaTypesBuilder typesBuilder) {
			throw new RuntimeException("Migration script 666 always throws exception");
		}
	}
}
