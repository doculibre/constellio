package com.constellio.app.modules.rm.migrations;

import com.constellio.app.entities.modules.MetadataSchemasAlterationHelper;
import com.constellio.app.entities.modules.MigrationResourcesProvider;
import com.constellio.app.entities.modules.MigrationScript;
import com.constellio.app.modules.rm.model.calculators.MessageHasLinkedDocumentCalculator;
import com.constellio.app.modules.rm.wrappers.DecommissioningList;
import com.constellio.app.modules.rm.wrappers.DisposalMode;
import com.constellio.app.modules.rm.wrappers.RMMessage;
import com.constellio.app.services.factories.AppLayerFactory;
import com.constellio.model.entities.records.wrappers.Message;
import com.constellio.model.services.schemas.builders.MetadataSchemaBuilder;
import com.constellio.model.services.schemas.builders.MetadataSchemaTypeBuilder;
import com.constellio.model.services.schemas.builders.MetadataSchemaTypesBuilder;

import static com.constellio.model.entities.schemas.MetadataValueType.BOOLEAN;
import static com.constellio.model.entities.schemas.MetadataValueType.ENUM;

public class RMMigrationTo9_3_2_2 implements MigrationScript {
	@Override
	public String getVersion() {
		return "9.3.2.2";
	}

	@Override
	public void migrate(String collection, MigrationResourcesProvider migrationResourcesProvider,
						AppLayerFactory appLayerFactory) throws Exception {
		new SchemaAlterationFor9_3_2_2(collection, migrationResourcesProvider, appLayerFactory).migrate();
	}

	private class SchemaAlterationFor9_3_2_2 extends MetadataSchemasAlterationHelper {
		SchemaAlterationFor9_3_2_2(String collection, MigrationResourcesProvider migrationResourcesProvider,
								   AppLayerFactory appLayerFactory) {
			super(collection, migrationResourcesProvider, appLayerFactory);
		}

		@Override
		protected void migrate(MetadataSchemaTypesBuilder typesBuilder) {
			MetadataSchemaTypeBuilder messageSchemaType = typesBuilder.getSchemaType(Message.SCHEMA_TYPE);
			MetadataSchemaBuilder messageDefaultSchema = messageSchemaType.getDefaultSchema();
			messageDefaultSchema.createUndeletable(RMMessage.HAS_LINKED_DOCUMENTS).setType(BOOLEAN)
					.defineDataEntry().asCalculated(MessageHasLinkedDocumentCalculator.class);

			MetadataSchemaBuilder decomListSchema =
					typesBuilder.getSchemaType(DecommissioningList.SCHEMA_TYPE).getDefaultSchema();

			decomListSchema.createUndeletable(DecommissioningList.EXTERNAL_LINK_DISPOSAL_MODE).setType(ENUM).defineAsEnum(DisposalMode.class);
		}
	}
}
