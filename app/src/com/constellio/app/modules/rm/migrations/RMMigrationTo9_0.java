package com.constellio.app.modules.rm.migrations;

import com.constellio.app.entities.modules.MetadataSchemasAlterationHelper;
import com.constellio.app.entities.modules.MigrationResourcesProvider;
import com.constellio.app.entities.modules.MigrationScript;
import com.constellio.app.modules.rm.constants.RMPermissionsTo;
import com.constellio.app.modules.rm.model.calculators.folder.FolderActualDepositDateCalculator;
import com.constellio.app.modules.rm.model.calculators.folder.FolderActualDestructionDateCalculator;
import com.constellio.app.modules.rm.model.calculators.folder.FolderActualTransferDateCalculator;
import com.constellio.app.modules.rm.model.calculators.folder.FolderOpeningDateCalculator;
import com.constellio.app.modules.rm.wrappers.AdministrativeUnit;
import com.constellio.app.modules.rm.wrappers.Folder;
import com.constellio.app.modules.rm.wrappers.PrintableLabel;
import com.constellio.app.modules.rm.wrappers.type.FolderType;
import com.constellio.app.services.factories.AppLayerFactory;
import com.constellio.app.services.migrations.CoreRoles;
import com.constellio.app.services.schemasDisplay.SchemasDisplayManager;
import com.constellio.model.entities.CorePermissions;
import com.constellio.model.entities.security.Role;
import com.constellio.model.services.factories.ModelLayerFactory;
import com.constellio.model.services.schemas.builders.MetadataSchemaBuilder;
import com.constellio.model.services.schemas.builders.MetadataSchemaTypesBuilder;
import com.constellio.model.services.security.roles.RolesManager;

import java.util.ArrayList;
import java.util.List;

import static java.util.Arrays.asList;

//9.0
public class RMMigrationTo9_0 implements MigrationScript {


	@Override
	public String getVersion() {
		return "9.0";
	}

	@Override
	public void migrate(String collection, MigrationResourcesProvider migrationResourcesProvider,
						AppLayerFactory appLayerFactory)
			throws Exception {

		new SchemaAlterationFor9_0(collection, migrationResourcesProvider, appLayerFactory).migrate();

		ModelLayerFactory modelLayerFactory = appLayerFactory.getModelLayerFactory();

		RolesManager rolesManager = modelLayerFactory.getRolesManager();
		List<Role> roleList1 = rolesManager.getAllRoles(collection);
		for (Role role : roleList1) {
			if (role.hasOperationPermission(RMPermissionsTo.PROCESS_DECOMMISSIONING_LIST)) {
				rolesManager.updateRole(role.withNewPermissions(asList(RMPermissionsTo.CREATE_DECOMMISSIONING_LIST)));
			}
		}

		List<Role> roleList2 = rolesManager.getAllRoles(collection);
		for (Role role : roleList2) {
			rolesManager.updateRole(role.withNewPermissions(asList(RMPermissionsTo.CART_BATCH_DELETE,
					CorePermissions.MODIFY_RECORDS_USING_BATCH_PROCESS)));
		}

		Role adminRole = rolesManager.getRole(collection, CoreRoles.ADMINISTRATOR);
		adminRole = adminRole.withTitle(migrationResourcesProvider.getValuesOfAllLanguagesWithSeparator("init.roles.ADM", " / "));
		rolesManager.updateRole(adminRole);

		//new MarkForPreviewConversionFlagEnabler(collection, modelLayerFactory).enable();
	}

	private class SchemaAlterationFor9_0 extends MetadataSchemasAlterationHelper {
		public SchemaAlterationFor9_0(String collection, MigrationResourcesProvider migrationResourcesProvider,
									  AppLayerFactory appLayerFactory) {
			super(collection, migrationResourcesProvider, appLayerFactory);
		}

		@Override
		protected void migrate(MetadataSchemaTypesBuilder builder) {
			MetadataSchemaBuilder printLabelDefaultSchema = typesBuilder.getSchemaType(PrintableLabel.SCHEMA_TYPE).getSchema(PrintableLabel.SCHEMA_LABEL);

			printLabelDefaultSchema.getMetadata(PrintableLabel.COLONNE).setDefaultRequirement(true);
			printLabelDefaultSchema.getMetadata(PrintableLabel.LIGNE).setDefaultRequirement(true);

			MetadataSchemaBuilder defaultFolderSchema = builder.getDefaultSchema(Folder.SCHEMA_TYPE);

			defaultFolderSchema.get(Folder.OPENING_DATE).defineDataEntry()
					.asCalculated(FolderOpeningDateCalculator.class);
			defaultFolderSchema.get(Folder.ACTUAL_TRANSFER_DATE).defineDataEntry()
					.asCalculated(FolderActualTransferDateCalculator.class);
			defaultFolderSchema.get(Folder.ACTUAL_DEPOSIT_DATE).defineDataEntry()
					.asCalculated(FolderActualDepositDateCalculator.class);
			defaultFolderSchema.get(Folder.ACTUAL_DESTRUCTION_DATE).defineDataEntry()
					.asCalculated(FolderActualDestructionDateCalculator.class);

			SchemasDisplayManager displayManager = appLayerFactory.getMetadataSchemasDisplayManager();
			displayManager.saveSchema(displayManager.getSchema(collection, AdministrativeUnit.DEFAULT_SCHEMA)
					.withRemovedFormMetadatas(AdministrativeUnit.DEFAULT_SCHEMA + "_" + AdministrativeUnit.FUNCTIONS));

			List<String> folderTypeFormMetadataCodes = displayManager.getSchema(collection, FolderType.DEFAULT_SCHEMA).getFormMetadataCodes();
			String code = FolderType.DEFAULT_SCHEMA + "_" + FolderType.CODE;
			String title = FolderType.DEFAULT_SCHEMA + "_" + FolderType.TITLE;
			if (folderTypeFormMetadataCodes.containsAll(asList(code, title))) {
				folderTypeFormMetadataCodes = new ArrayList<>(folderTypeFormMetadataCodes);
				folderTypeFormMetadataCodes.removeAll(asList(code, title));
				folderTypeFormMetadataCodes.addAll(0, asList(code, title));
			}

			displayManager.saveSchema(displayManager.getSchema(collection, FolderType.DEFAULT_SCHEMA).withFormMetadataCodes(folderTypeFormMetadataCodes));
		}

	}
}