package com.constellio.app.modules.rm.migrations;

import static java.util.Arrays.asList;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import com.constellio.app.entities.modules.MetadataSchemasAlterationHelper;
import com.constellio.app.entities.modules.MigrationResourcesProvider;
import com.constellio.app.entities.modules.MigrationScript;
import com.constellio.app.entities.schemasDisplay.SchemaTypeDisplayConfig;
import com.constellio.app.modules.rm.constants.RMPermissionsTo;
import com.constellio.app.modules.rm.constants.RMRoles;
import com.constellio.app.modules.rm.services.RMSchemasRecordsServices;
import com.constellio.app.modules.rm.wrappers.Document;
import com.constellio.app.modules.rm.wrappers.Folder;
import com.constellio.app.services.factories.AppLayerFactory;
import com.constellio.app.services.schemasDisplay.SchemaDisplayManagerTransaction;
import com.constellio.app.services.schemasDisplay.SchemasDisplayManager;
import com.constellio.model.entities.CorePermissions;
import com.constellio.model.entities.Language;
import com.constellio.model.entities.records.Transaction;
import com.constellio.model.entities.schemas.MetadataSchemaType;
import com.constellio.model.entities.schemas.MetadataSchemaTypes;
import com.constellio.model.entities.security.Role;
import com.constellio.model.services.factories.ModelLayerFactory;
import com.constellio.model.services.records.RecordServicesException;
import com.constellio.model.services.schemas.builders.MetadataBuilder;
import com.constellio.model.services.schemas.builders.MetadataSchemaTypeBuilder;
import com.constellio.model.services.schemas.builders.MetadataSchemaTypesBuilder;

public class RMMigrationTo5_0_2 implements MigrationScript {
	@Override
	public String getVersion() {
		return "5.0.2";
	}

	@Override
	public void migrate(String collection, MigrationResourcesProvider migrationResourcesProvider,
			AppLayerFactory appLayerFactory) {
		new SchemaAlterationFor5_0_2(collection, migrationResourcesProvider, appLayerFactory).migrate();
		createMediumTypes(collection, appLayerFactory.getModelLayerFactory(), migrationResourcesProvider);

		MetadataSchemaTypes types = appLayerFactory.getModelLayerFactory().getMetadataSchemasManager().getSchemaTypes(collection);
		SchemasDisplayManager schemasDisplayManager = appLayerFactory.getMetadataSchemasDisplayManager();

		setAllMetadatasInDefaultGroup(types, schemasDisplayManager, migrationResourcesProvider);
		givenNewPermissionsToExistingRMRoles(collection, appLayerFactory.getModelLayerFactory());

	}

	private void createMediumTypes(String collection, ModelLayerFactory modelLayerFactory,
			MigrationResourcesProvider migrationResourcesProvider) {
		Transaction transaction = new Transaction();

		RMSchemasRecordsServices schemas = new RMSchemasRecordsServices(collection, modelLayerFactory);

		String paperCode = migrationResourcesProvider.getDefaultLanguageString("MediumType.paperCode");
		String filmCode = migrationResourcesProvider.getDefaultLanguageString("MediumType.filmCode");
		String driveCode = migrationResourcesProvider.getDefaultLanguageString("MediumType.driveCode");

		if (schemas.getMediumTypeByCode(paperCode) == null) {
			transaction.add(schemas.newMediumType().setCode(paperCode)
					.setTitle(migrationResourcesProvider.getDefaultLanguageString("MediumType.paperTitle"))
					.setAnalogical(true));
		}

		if (schemas.getMediumTypeByCode(filmCode) == null) {
			transaction.add(schemas.newMediumType().setCode(filmCode)
					.setTitle(migrationResourcesProvider.getDefaultLanguageString("MediumType.filmTitle"))
					.setAnalogical(true));
		}

		if (schemas.getMediumTypeByCode(driveCode) == null) {
			transaction.add(schemas.newMediumType().setCode(driveCode)
					.setTitle(migrationResourcesProvider.getDefaultLanguageString("MediumType.driveTitle"))
					.setAnalogical(false));
		}

		try {
			modelLayerFactory.newRecordServices().execute(transaction);
		} catch (RecordServicesException e) {
			throw new RuntimeException(e);
		}

	}

	private void givenNewPermissionsToExistingRMRoles(String collection, ModelLayerFactory modelLayerFactory) {
		Role userRole = modelLayerFactory.getRolesManager().getRole(collection, RMRoles.USER);
		Role managerRole = modelLayerFactory.getRolesManager().getRole(collection, RMRoles.MANAGER);
		Role rgdRole = modelLayerFactory.getRolesManager().getRole(collection, RMRoles.RGD);

		List<String> newUserPermissions = new ArrayList<>(userRole.getOperationPermissions());
		newUserPermissions.add(RMPermissionsTo.SHARE_A_SEMIACTIVE_DOCUMENT);
		newUserPermissions.add(RMPermissionsTo.SHARE_A_SEMIACTIVE_FOLDER);
		newUserPermissions.add(RMPermissionsTo.UPLOAD_SEMIACTIVE_DOCUMENT);
		newUserPermissions.add(RMPermissionsTo.DELETE_SEMIACTIVE_DOCUMENT);
		newUserPermissions.add(RMPermissionsTo.DELETE_SEMIACTIVE_FOLDERS);

		List<String> newManagerPermissions = new ArrayList<>(managerRole.getOperationPermissions());
		newManagerPermissions.add(RMPermissionsTo.SHARE_A_SEMIACTIVE_DOCUMENT);
		newManagerPermissions.add(RMPermissionsTo.SHARE_A_SEMIACTIVE_FOLDER);
		newManagerPermissions.add(RMPermissionsTo.UPLOAD_SEMIACTIVE_DOCUMENT);
		newManagerPermissions.add(RMPermissionsTo.DELETE_SEMIACTIVE_DOCUMENT);
		newManagerPermissions.add(RMPermissionsTo.DELETE_SEMIACTIVE_FOLDERS);

		List<String> newRgdPermissions = new ArrayList<>();
		newRgdPermissions.addAll(RMPermissionsTo.PERMISSIONS.getAll());
		newRgdPermissions.addAll(CorePermissions.PERMISSIONS.getAll());

		modelLayerFactory.getRolesManager().updateRole(userRole.withPermissions(newUserPermissions));
		modelLayerFactory.getRolesManager().updateRole(managerRole.withPermissions(newManagerPermissions));
		modelLayerFactory.getRolesManager().updateRole(rgdRole.withPermissions(newRgdPermissions));

	}

	private void setAllMetadatasInDefaultGroup(MetadataSchemaTypes types, SchemasDisplayManager schemasDisplayManager,
			MigrationResourcesProvider migrationResourcesProvider) {

		String groupLabel = migrationResourcesProvider.getDefaultLanguageString("defaultGroupLabel");
		String classifiedInLabel = migrationResourcesProvider.getDefaultLanguageString("classifiedInGroupLabel");

		List<MetadataSchemaType> rmTypes = new ArrayList<>();
		rmTypes.add(types.getSchemaType("collection"));
		rmTypes.add(types.getSchemaType("event"));
		rmTypes.add(types.getSchemaType("user"));
		rmTypes.add(types.getSchemaType("folder"));
		rmTypes.add(types.getSchemaType("ddvFolderType"));
		rmTypes.add(types.getSchemaType("containerRecord"));
		rmTypes.add(types.getSchemaType("filingSpace"));
		rmTypes.add(types.getSchemaType("userDocument"));
		rmTypes.add(types.getSchemaType("decommissioningList"));
		rmTypes.add(types.getSchemaType("ddvMediumType"));
		rmTypes.add(types.getSchemaType("ddvStorageSpaceType"));
		rmTypes.add(types.getSchemaType("task"));
		rmTypes.add(types.getSchemaType("document"));
		rmTypes.add(types.getSchemaType("ddvDocumentType"));
		rmTypes.add(types.getSchemaType("retentionRule"));
		rmTypes.add(types.getSchemaType("storageSpace"));
		rmTypes.add(types.getSchemaType("uniformSubdivision"));
		rmTypes.add(types.getSchemaType("group"));
		rmTypes.add(types.getSchemaType("category"));
		rmTypes.add(types.getSchemaType("administrativeUnit"));
		rmTypes.add(types.getSchemaType("ddvContainerRecordType"));

		SchemaDisplayManagerTransaction transaction = new SchemaDisplayManagerTransaction();
		for (MetadataSchemaType schemaType : rmTypes) {
			SchemaTypeDisplayConfig typeConfig = schemasDisplayManager.getType(types.getCollection(), schemaType.getCode());

			Map<String, Map<Language, String>> groups;
			if (schemaType.getCode().equals(Folder.SCHEMA_TYPE) || schemaType.getCode().equals(Document.SCHEMA_TYPE)) {
				groups = migrationResourcesProvider.getLanguageMapWithKeys(asList("defaultGroupLabel", "classifiedInGroupLabel"));
			} else {
				groups = migrationResourcesProvider.getLanguageMapWithKeys(asList("defaultGroupLabel"));
			}

			transaction.getModifiedTypes().add(typeConfig.withMetadataGroup(groups));
		}

		schemasDisplayManager.execute(transaction);
	}

	class SchemaAlterationFor5_0_2 extends MetadataSchemasAlterationHelper {

		protected SchemaAlterationFor5_0_2(String collection, MigrationResourcesProvider migrationResourcesProvider,
				AppLayerFactory appLayerFactory) {
			super(collection, migrationResourcesProvider, appLayerFactory);
		}

		public String getVersion() {
			return "5.0.2";
		}

		@Override
		protected void migrate(MetadataSchemaTypesBuilder typesBuilder) {
			for (MetadataSchemaTypeBuilder typeBuilder : typesBuilder.getTypes()) {
				for (MetadataBuilder metadataBuilder : typeBuilder.getDefaultSchema().getMetadatas()) {
					if ("code".equals(metadataBuilder.getLocalCode())) {
						metadataBuilder.setUniqueValue(true);
						metadataBuilder.setDefaultRequirement(true);
					}
				}
			}

			typesBuilder.getSchemaType("collection").setSecurity(false);
			typesBuilder.getSchemaType("event").setSecurity(false);
			typesBuilder.getSchemaType("user").setSecurity(false);
			typesBuilder.getSchemaType("folder").setSecurity(true);
			typesBuilder.getSchemaType("ddvFolderType").setSecurity(false);
			typesBuilder.getSchemaType("containerRecord").setSecurity(true);
			typesBuilder.getSchemaType("filingSpace").setSecurity(false);
			typesBuilder.getSchemaType("userDocument").setSecurity(false);
			typesBuilder.getSchemaType("decommissioningList").setSecurity(false);
			typesBuilder.getSchemaType("ddvMediumType").setSecurity(false);
			typesBuilder.getSchemaType("ddvStorageSpaceType").setSecurity(false);
			typesBuilder.getSchemaType("task").setSecurity(false);
			typesBuilder.getSchemaType("document").setSecurity(true);
			typesBuilder.getSchemaType("ddvDocumentType").setSecurity(false);
			typesBuilder.getSchemaType("retentionRule").setSecurity(false);
			typesBuilder.getSchemaType("storageSpace").setSecurity(false);
			typesBuilder.getSchemaType("uniformSubdivision").setSecurity(false);
			typesBuilder.getSchemaType("group").setSecurity(false);
			typesBuilder.getSchemaType("category").setSecurity(false);
			typesBuilder.getSchemaType("administrativeUnit").setSecurity(true);
			typesBuilder.getSchemaType("ddvContainerRecordType").setSecurity(false);
			//
			//			for (MetadataSchemaTypeBuilder schemaTypeBuilder : typesBuilder.getTypes()) {
			//
			//				String code = schemaTypeBuilder.getCode();
			//				schemaTypeBuilder.setSecurity(
			//						code.equals(Folder.SCHEMA_TYPE)
			//								|| code.equals(Document.SCHEMA_TYPE)
			//								|| code.equals(AdministrativeUnit.SCHEMA_TYPE)
			//								|| code.equals(ContainerRecord.SCHEMA_TYPE));
			//			}
		}

	}

}
