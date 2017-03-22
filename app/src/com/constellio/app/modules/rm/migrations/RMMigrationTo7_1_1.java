package com.constellio.app.modules.rm.migrations;

import static com.constellio.model.entities.schemas.MetadataTransiency.PERSISTED;
import static com.constellio.model.entities.schemas.Schemas.SCHEMA_AUTOCOMPLETE_FIELD;

import java.util.ArrayList;
import java.util.List;

import com.constellio.app.entities.modules.MetadataSchemasAlterationHelper;
import com.constellio.app.entities.modules.MigrationHelper;
import com.constellio.app.entities.modules.MigrationResourcesProvider;
import com.constellio.app.entities.modules.MigrationScript;
import com.constellio.app.modules.rm.constants.RMRoles;
import com.constellio.app.modules.rm.model.calculators.document.DocumentAutocompleteFieldCalculator;
import com.constellio.app.modules.rm.model.calculators.folder.FolderAutocompleteFieldCalculator;
import com.constellio.app.modules.rm.wrappers.AdministrativeUnit;
import com.constellio.app.modules.rm.wrappers.Category;
import com.constellio.app.modules.rm.wrappers.Document;
import com.constellio.app.modules.rm.wrappers.Folder;
import com.constellio.app.modules.rm.wrappers.RMUserFolder;
import com.constellio.app.modules.rm.wrappers.RetentionRule;
import com.constellio.app.services.factories.AppLayerFactory;
import com.constellio.model.entities.CorePermissions;
import com.constellio.model.entities.records.wrappers.UserFolder;
import com.constellio.model.entities.schemas.MetadataValueType;
import com.constellio.model.entities.security.Role;
import com.constellio.model.services.factories.ModelLayerFactory;
import com.constellio.model.services.schemas.builders.MetadataSchemaBuilder;
import com.constellio.model.services.schemas.builders.MetadataSchemaTypesBuilder;

public class RMMigrationTo7_1_1 extends MigrationHelper implements MigrationScript {
	@Override
	public String getVersion() {
		return "7.1.1";
	}

	@Override
	public void migrate(String collection, MigrationResourcesProvider migrationResourcesProvider, AppLayerFactory appLayerFactory)
			throws Exception {
		new SchemaAlterationFor7_1_1(collection, migrationResourcesProvider, appLayerFactory).migrate();
		alterRole(collection, appLayerFactory.getModelLayerFactory());

		appLayerFactory.getSystemGlobalConfigsManager().setReindexingRequired(true);
	}

	private void alterRole(String collection, ModelLayerFactory modelLayerFactory) {
		Role RGDrole = modelLayerFactory.getRolesManager().getRole(collection, RMRoles.RGD);
		List<String> newAdmPermissions = new ArrayList<>();
		newAdmPermissions.add(CorePermissions.VIEW_SYSTEM_BATCH_PROCESSES);
		modelLayerFactory.getRolesManager().updateRole(RGDrole.withNewPermissions(newAdmPermissions));
	}

	private class SchemaAlterationFor7_1_1 extends MetadataSchemasAlterationHelper {

		protected SchemaAlterationFor7_1_1(String collection, MigrationResourcesProvider migrationResourcesProvider,
				AppLayerFactory appLayerFactory) {
			super(collection, migrationResourcesProvider, appLayerFactory);
		}

		@Override
		protected void migrate(MetadataSchemaTypesBuilder typesBuilder) {

			MetadataSchemaBuilder documentSchema = typesBuilder.getSchemaType(Document.SCHEMA_TYPE).getDefaultSchema();

			MetadataSchemaBuilder folderSchema = typesBuilder.getSchemaType(Folder.SCHEMA_TYPE).getDefaultSchema();
			folderSchema.getMetadata(Folder.ACTIVE_RETENTION_CODE).setTransiency(PERSISTED);
			folderSchema.getMetadata(Folder.SEMIACTIVE_RETENTION_CODE).setTransiency(PERSISTED);
			folderSchema.getMetadata(Folder.SEMIACTIVE_RETENTION_TYPE).setTransiency(PERSISTED);
			folderSchema.getMetadata(Folder.COPY_RULES_EXPECTED_TRANSFER_DATES).setTransiency(PERSISTED);
			folderSchema.getMetadata(Folder.COPY_RULES_EXPECTED_DEPOSIT_DATES).setTransiency(PERSISTED);
			folderSchema.getMetadata(Folder.COPY_RULES_EXPECTED_DESTRUCTION_DATES).setTransiency(PERSISTED);
			folderSchema.getMetadata(Folder.MAIN_COPY_RULE).setTransiency(PERSISTED);
			folderSchema.getMetadata(Folder.DECOMMISSIONING_DATE).setTransiency(PERSISTED);
			folderSchema.getMetadata(SCHEMA_AUTOCOMPLETE_FIELD.getLocalCode())
					.defineDataEntry().asCalculated(FolderAutocompleteFieldCalculator.class);
			typesBuilder.getSchema(Document.DEFAULT_SCHEMA).getMetadata(SCHEMA_AUTOCOMPLETE_FIELD.getLocalCode())
					.defineDataEntry().asCalculated(DocumentAutocompleteFieldCalculator.class);

			MetadataSchemaBuilder defaultSchema = typesBuilder.getSchemaType(UserFolder.SCHEMA_TYPE).getDefaultSchema();
			defaultSchema.create(RMUserFolder.ADMINISTRATIVE_UNIT).setType(MetadataValueType.REFERENCE).setEssential(false)
					.defineReferencesTo(typesBuilder.getSchemaType(AdministrativeUnit.SCHEMA_TYPE));
			defaultSchema.create(RMUserFolder.CATEGORY).setType(MetadataValueType.REFERENCE).setEssential(false)
					.defineReferencesTo(typesBuilder.getSchemaType(Category.SCHEMA_TYPE));
			defaultSchema.create(RMUserFolder.RETENTION_RULE).setType(MetadataValueType.REFERENCE).setEssential(false)
					.defineReferencesTo(typesBuilder.getSchemaType(RetentionRule.SCHEMA_TYPE));
			defaultSchema.create(RMUserFolder.PARENT_FOLDER).setType(MetadataValueType.REFERENCE).setEssential(false)
					.defineReferencesTo(typesBuilder.getSchemaType(Folder.SCHEMA_TYPE));

		}
	}
}
