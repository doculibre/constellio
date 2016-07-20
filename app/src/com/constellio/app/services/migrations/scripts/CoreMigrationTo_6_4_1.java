package com.constellio.app.services.migrations.scripts;

import com.constellio.app.entities.modules.MetadataSchemasAlterationHelper;
import com.constellio.app.entities.modules.MigrationResourcesProvider;
import com.constellio.app.entities.modules.MigrationScript;
import com.constellio.app.services.factories.AppLayerFactory;
import com.constellio.model.entities.records.wrappers.Collection;
import com.constellio.model.entities.security.global.SolrUserCredential;
import com.constellio.model.services.schemas.builders.MetadataSchemaBuilder;
import com.constellio.model.services.schemas.builders.MetadataSchemaTypesBuilder;
import com.constellio.model.services.schemas.builders.MetadataSchemaTypesBuilderRuntimeException;

public class CoreMigrationTo_6_4_1 implements MigrationScript {

	@Override
	public String getVersion() {
		return "6.4.1";
	}

	@Override
	public void migrate(String collection, MigrationResourcesProvider migrationResourcesProvider,
			AppLayerFactory appLayerFactory) {
		if(collection.equals(Collection.SYSTEM_COLLECTION)){
			new CoreSchemaAlterationFor6_4_1(collection, migrationResourcesProvider, appLayerFactory).migrate();
		}
	}

	class CoreSchemaAlterationFor6_4_1 extends MetadataSchemasAlterationHelper {

		protected CoreSchemaAlterationFor6_4_1(String collection,
				MigrationResourcesProvider migrationResourcesProvider, AppLayerFactory appLayerFactory) {
			super(collection, migrationResourcesProvider, appLayerFactory);
		}

		@Override
		protected void migrate(MetadataSchemaTypesBuilder typesBuilder) {
			nonUniqueEmailMetadata(typesBuilder);
		}

		private void nonUniqueEmailMetadata(MetadataSchemaTypesBuilder typesBuilder) {
			try{
				MetadataSchemaBuilder userCredential = typesBuilder
						.getSchema(SolrUserCredential.DEFAULT_SCHEMA);
				userCredential.getMetadata(SolrUserCredential.EMAIL).setUniqueValue(false);
			}catch(MetadataSchemaTypesBuilderRuntimeException.NoSuchSchemaType e){
				//OK
				System.out.println(collection);
			}

		}
	}
}
