package com.constellio.app.modules.rm.migrations;

import static java.util.Arrays.asList;

import com.constellio.app.entities.modules.MetadataSchemasAlterationHelper;
import com.constellio.app.entities.modules.MigrationHelper;
import com.constellio.app.entities.modules.MigrationResourcesProvider;
import com.constellio.app.entities.modules.MigrationScript;
import com.constellio.app.modules.rm.model.calculators.document.DocumentCaptionCalculator;
import com.constellio.app.modules.rm.model.calculators.folder.FolderCaptionCalculator;
import com.constellio.app.modules.rm.wrappers.Document;
import com.constellio.app.modules.rm.wrappers.Folder;
import com.constellio.app.modules.rm.wrappers.SIParchive;
import com.constellio.app.services.factories.AppLayerFactory;
import com.constellio.app.services.schemasDisplay.SchemasDisplayManager;
import com.constellio.model.entities.CorePermissions;
import com.constellio.model.entities.records.wrappers.TemporaryRecord;
import com.constellio.model.entities.schemas.Schemas;
import com.constellio.model.entities.security.Role;
import com.constellio.model.services.schemas.builders.MetadataSchemaTypesBuilder;

public class RMMigrationTo7_6 extends MigrationHelper implements MigrationScript {
	@Override
	public String getVersion() {
		return "7.6";
	}

	@Override
	public void migrate(String collection, MigrationResourcesProvider migrationResourcesProvider, AppLayerFactory appLayerFactory)
			throws Exception {
		new SchemaAlterationFor7_6(collection, migrationResourcesProvider, appLayerFactory).migrate();
		editTableMetadata(collection, appLayerFactory.getMetadataSchemasDisplayManager());

		migrateRoles(collection, appLayerFactory, new RolesAlteration() {

			@Override
			public Role alter(Role role) {
				if (role.hasOperationPermission(CorePermissions.MANAGE_METADATASCHEMAS)) {
					return role.withNewPermissions(asList(CorePermissions.MANAGE_EXCEL_REPORT));
				} else {
					return role;
				}
			}
		});

	}

	public void editTableMetadata(String collection, SchemasDisplayManager manager) {

		manager.saveSchema(manager.getSchema(collection, SIParchive.SCHEMA).withNewTableMetadatas(
				TemporaryRecord.DEFAULT_SCHEMA + "_" + Schemas.CREATED_BY.getLocalCode(),
				TemporaryRecord.DEFAULT_SCHEMA + "_" + Schemas.CREATED_ON.getLocalCode(),
				TemporaryRecord.DEFAULT_SCHEMA + "_" + TemporaryRecord.DESTRUCTION_DATE,
				TemporaryRecord.DEFAULT_SCHEMA + "_" + TemporaryRecord.CONTENT));
	}

	class SchemaAlterationFor7_6 extends MetadataSchemasAlterationHelper {

		protected SchemaAlterationFor7_6(String collection, MigrationResourcesProvider migrationResourcesProvider,
				AppLayerFactory appLayerFactory) {
			super(collection, migrationResourcesProvider, appLayerFactory);
		}

		@Override
		protected void migrate(MetadataSchemaTypesBuilder typesBuilder) {
			typesBuilder.getDefaultSchema(Folder.SCHEMA_TYPE).get(Schemas.CAPTION.getLocalCode())
					.defineDataEntry().asCalculated(FolderCaptionCalculator.class);

			typesBuilder.getDefaultSchema(Document.SCHEMA_TYPE).get(Schemas.CAPTION.getLocalCode())
					.defineDataEntry().asCalculated(DocumentCaptionCalculator.class);
		}

	}
}
