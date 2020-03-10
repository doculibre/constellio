package com.constellio.app.modules.rm.migrations;

import com.constellio.app.entities.modules.MetadataSchemasAlterationHelper;
import com.constellio.app.entities.modules.MigrationResourcesProvider;
import com.constellio.app.entities.modules.MigrationScript;
import com.constellio.app.modules.rm.model.ExternalLinkType;
import com.constellio.app.modules.rm.services.ValueListItemSchemaTypeBuilder;
import com.constellio.app.modules.rm.services.ValueListItemSchemaTypeBuilder.ValueListItemSchemaTypeBuilderOptions;
import com.constellio.app.modules.rm.wrappers.ExternalLink;
import com.constellio.app.services.factories.AppLayerFactory;
import com.constellio.app.services.migrations.MigrationUtil;
import com.constellio.model.entities.Language;
import com.constellio.model.entities.schemas.MetadataValueType;
import com.constellio.model.services.schemas.builders.MetadataSchemaBuilder;
import com.constellio.model.services.schemas.builders.MetadataSchemaTypeBuilder;
import com.constellio.model.services.schemas.builders.MetadataSchemaTypesBuilder;

import java.util.Map;

public class RMMigrationTo9_1_1000 implements MigrationScript {
	@Override
	public String getVersion() {
		return "9.1.1000";
	}

	@Override
	public void migrate(String collection, MigrationResourcesProvider migrationResourcesProvider,
						AppLayerFactory appLayerFactory) throws Exception {
		new SchemaAlterationFor9_1_1000(collection, migrationResourcesProvider, appLayerFactory).migrate();
	}

	private class SchemaAlterationFor9_1_1000 extends MetadataSchemasAlterationHelper {
		SchemaAlterationFor9_1_1000(String collection, MigrationResourcesProvider migrationResourcesProvider,
									AppLayerFactory appLayerFactory) {
			super(collection, migrationResourcesProvider, appLayerFactory);
		}

		@Override
		protected void migrate(MetadataSchemaTypesBuilder typesBuilder) {
			MetadataSchemaBuilder externalLinkTypeSchema = setupExternalLinkTypeSchema().getDefaultSchema();

			MetadataSchemaBuilder externalLinkSchema =
					typesBuilder.createNewSchemaType(ExternalLink.SCHEMA_TYPE).getDefaultSchema();

			externalLinkSchema.createUndeletable(ExternalLink.TYPE)
					.setType(MetadataValueType.REFERENCE)
					.defineReferencesTo(externalLinkTypeSchema);
		}

		private MetadataSchemaTypeBuilder setupExternalLinkTypeSchema() {
			Map<Language, String> mapLanguage = MigrationUtil.getLabelsByLanguage(collection, modelLayerFactory,
					migrationResourcesProvider, "init.externalLinkType");

			MetadataSchemaTypeBuilder schemaType = new ValueListItemSchemaTypeBuilder(types())
					.createValueListItemSchema(ExternalLinkType.SCHEMA_TYPE, mapLanguage,
							ValueListItemSchemaTypeBuilderOptions.codeMetadataRequiredAndUnique())
					.setSecurity(false);

			MetadataSchemaBuilder defaultSchema = schemaType.getDefaultSchema();
			defaultSchema.createUndeletable(ExternalLinkType.LINKED_SCHEMA).setType(MetadataValueType.STRING);

			return schemaType;
		}
	}
}
