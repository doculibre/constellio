package com.constellio.app.modules.rm.migrations;

import com.constellio.app.entities.modules.MetadataSchemasAlterationHelper;
import com.constellio.app.entities.modules.MigrationResourcesProvider;
import com.constellio.app.entities.modules.MigrationScript;
import com.constellio.app.modules.rm.services.ValueListItemSchemaTypeBuilder;
import com.constellio.app.modules.rm.services.ValueListItemSchemaTypeBuilder.ValueListItemSchemaTypeBuilderOptions;
import com.constellio.app.modules.rm.wrappers.AdministrativeUnit;
import com.constellio.app.modules.rm.wrappers.Document;
import com.constellio.app.modules.rm.wrappers.Folder;
import com.constellio.app.modules.rm.wrappers.UserFunction;
import com.constellio.app.services.factories.AppLayerFactory;
import com.constellio.app.services.migrations.MigrationUtil;
import com.constellio.model.entities.Language;
import com.constellio.model.entities.records.wrappers.User;
import com.constellio.model.entities.schemas.MetadataValueType;
import com.constellio.model.services.schemas.builders.MetadataSchemaBuilder;
import com.constellio.model.services.schemas.builders.MetadataSchemaTypeBuilder;
import com.constellio.model.services.schemas.builders.MetadataSchemaTypesBuilder;

import java.util.Map;

import static com.constellio.model.entities.schemas.MetadataValueType.REFERENCE;

public class RMMigrationTo8_2_42 implements MigrationScript {

	@Override
	public String getVersion() {
		return "8.2.42";
	}

	@Override
	public void migrate(String collection, MigrationResourcesProvider provider, AppLayerFactory appLayerFactory)
			throws Exception {
		new SchemaAlterationFor8_2_42(collection, provider, appLayerFactory).migrate();
	}

	private class SchemaAlterationFor8_2_42 extends MetadataSchemasAlterationHelper {
		public SchemaAlterationFor8_2_42(String collection, MigrationResourcesProvider migrationResourcesProvider,
										 AppLayerFactory appLayerFactory) {
			super(collection, migrationResourcesProvider, appLayerFactory);
		}

		@Override
		protected void migrate(MetadataSchemaTypesBuilder builder) {

			Map<Language, String> mapLanguage = MigrationUtil.getLabelsByLanguage(collection, modelLayerFactory,
					migrationResourcesProvider, "init.ddvUserFunction");

			MetadataSchemaTypeBuilder admUnitFunctionSchemaType = new ValueListItemSchemaTypeBuilder(types())
					.createValueListItemSchema(UserFunction.SCHEMA_TYPE, mapLanguage,
							ValueListItemSchemaTypeBuilderOptions.codeMetadataDisabled()).setSecurity(false);

			MetadataSchemaTypeBuilder admUnitSchemaType = builder.getSchemaType(AdministrativeUnit.SCHEMA_TYPE);
			admUnitSchemaType.createMetadata(AdministrativeUnit.FUNCTIONS).setSystemReserved(true)
					.setType(REFERENCE).setMultivalue(true)
					.defineReferencesTo(admUnitFunctionSchemaType);

			admUnitSchemaType.createMetadata(AdministrativeUnit.FUNCTIONS_USERS).setSystemReserved(true)
					.setType(REFERENCE).setMultivalue(true)
					.defineReferencesTo(builder.getSchemaType(User.SCHEMA_TYPE));


			MetadataSchemaBuilder defaultFolderSchema = typesBuilder.getDefaultSchema(Folder.SCHEMA_TYPE);
			defaultFolderSchema.createUndeletable(Folder.IS_MODEL).setType(MetadataValueType.BOOLEAN).setSystemReserved(true)
					.setDefaultValue(false);

			MetadataSchemaBuilder defaultDocumentSchema = typesBuilder.getDefaultSchema(Document.SCHEMA_TYPE);
			defaultDocumentSchema.createUndeletable(Document.IS_MODEL).setType(MetadataValueType.BOOLEAN).setSystemReserved(true)
					.setDefaultValue(false);
		}
	}
}
