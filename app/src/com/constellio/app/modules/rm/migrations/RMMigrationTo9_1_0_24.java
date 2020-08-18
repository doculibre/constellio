package com.constellio.app.modules.rm.migrations;

import com.constellio.app.entities.modules.MetadataSchemasAlterationHelper;
import com.constellio.app.entities.modules.MigrationResourcesProvider;
import com.constellio.app.entities.modules.MigrationScript;
import com.constellio.app.modules.rm.model.ExternalLinkType;
import com.constellio.app.modules.rm.wrappers.ExternalLink;
import com.constellio.app.services.factories.AppLayerFactory;
import com.constellio.model.entities.schemas.Schemas;
import com.constellio.model.services.schemas.builders.MetadataSchemaBuilder;
import com.constellio.model.services.schemas.builders.MetadataSchemaTypesBuilder;

import static com.constellio.model.entities.schemas.MetadataValueType.STRING;

public class RMMigrationTo9_1_0_24 implements MigrationScript {
	@Override
	public String getVersion() {
		return "9.1.0.24";
	}

	@Override
	public void migrate(String collection, MigrationResourcesProvider migrationResourcesProvider,
						AppLayerFactory appLayerFactory) throws Exception {
		new SchemaAlterationFor9_1_0_24(collection, migrationResourcesProvider, appLayerFactory).migrate();
	}

	private class SchemaAlterationFor9_1_0_24 extends MetadataSchemasAlterationHelper {
		SchemaAlterationFor9_1_0_24(String collection, MigrationResourcesProvider migrationResourcesProvider,
									AppLayerFactory appLayerFactory) {
			super(collection, migrationResourcesProvider, appLayerFactory);
		}

		@Override
		protected void migrate(MetadataSchemaTypesBuilder typesBuilder) {
			MetadataSchemaBuilder externalLinkSchemaType = typesBuilder.getDefaultSchema(ExternalLink.SCHEMA_TYPE);
			MetadataSchemaBuilder externalLinkTypeSchemaType = typesBuilder.getDefaultSchema(ExternalLinkType.SCHEMA_TYPE);
			externalLinkSchemaType.createUndeletable(ExternalLink.TYPE_CODE).setType(STRING).defineDataEntry()
					.asCopied(externalLinkSchemaType.getMetadata(ExternalLink.TYPE),
							externalLinkTypeSchemaType.getMetadata(Schemas.CODE.getLocalCode()));
		}
	}
}
