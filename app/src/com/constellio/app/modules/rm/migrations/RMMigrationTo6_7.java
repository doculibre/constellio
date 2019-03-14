package com.constellio.app.modules.rm.migrations;

import com.constellio.app.entities.modules.MetadataSchemasAlterationHelper;
import com.constellio.app.entities.modules.MigrationResourcesProvider;
import com.constellio.app.entities.modules.MigrationScript;
import com.constellio.app.entities.schemasDisplay.enums.MetadataInputType;
import com.constellio.app.modules.rm.RMEmailTemplateConstants;
import com.constellio.app.modules.rm.constants.RMPermissionsTo;
import com.constellio.app.modules.rm.model.calculators.container.ContainerRecordAvailableSizeCalculator;
import com.constellio.app.modules.rm.model.calculators.container.ContainerRecordLinearSizeCalculator;
import com.constellio.app.modules.rm.model.calculators.storageSpace.StorageSpaceAvailableSizeCalculator;
import com.constellio.app.modules.rm.model.calculators.storageSpace.StorageSpaceLinearSizeCalculator;
import com.constellio.app.modules.rm.model.validators.ContainerRecordValidator;
import com.constellio.app.modules.rm.model.validators.StorageSpaceValidator;
import com.constellio.app.modules.rm.wrappers.ContainerRecord;
import com.constellio.app.modules.rm.wrappers.Folder;
import com.constellio.app.modules.rm.wrappers.StorageSpace;
import com.constellio.app.modules.rm.wrappers.type.ContainerRecordType;
import com.constellio.app.services.factories.AppLayerFactory;
import com.constellio.app.services.schemasDisplay.SchemaTypesDisplayTransactionBuilder;
import com.constellio.app.services.schemasDisplay.SchemasDisplayManager;
import com.constellio.data.dao.managers.config.ConfigManagerException;
import com.constellio.model.entities.schemas.MetadataValueType;
import com.constellio.model.entities.security.Role;
import com.constellio.model.services.schemas.builders.MetadataBuilder;
import com.constellio.model.services.schemas.builders.MetadataSchemaTypesBuilder;
import com.constellio.model.services.security.roles.RolesManager;
import org.apache.commons.io.IOUtils;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;

import static java.util.Arrays.asList;

public class RMMigrationTo6_7 implements MigrationScript {

	AppLayerFactory appLayerFactory;
	String collection;
	MigrationResourcesProvider migrationResourcesProvider;

	@Override
	public String getVersion() {
		return "6.7";
	}

	@Override
	public void migrate(String collection, MigrationResourcesProvider provider, AppLayerFactory factory)
			throws Exception {
		this.appLayerFactory = factory;
		this.collection = collection;
		migrationResourcesProvider = provider;
		new SchemaAlterationsFor6_7(collection, provider, factory).migrate();
		migrateDisplayConfigs(factory, collection);
		reloadEmailTemplates();
		updatePermissions();
	}

	public static class SchemaAlterationsFor6_7 extends MetadataSchemasAlterationHelper {

		protected SchemaAlterationsFor6_7(String collection, MigrationResourcesProvider provider,
										  AppLayerFactory factory) {
			super(collection, provider, factory);
		}

		@Override
		protected void migrate(MetadataSchemaTypesBuilder typesBuilder) {
			migrateContainerRecordMetadatas(typesBuilder);
			migrateStorageSpaceMetadatas(typesBuilder);
		}

		private void migrateContainerRecordMetadatas(MetadataSchemaTypesBuilder typesBuilder) {
			typesBuilder.getSchema(ContainerRecord.DEFAULT_SCHEMA).defineValidators().add(ContainerRecordValidator.class);

			typesBuilder.getDefaultSchema(ContainerRecord.SCHEMA_TYPE).create(ContainerRecord.LINEAR_SIZE_ENTERED)
					.setType(MetadataValueType.NUMBER).setEssential(false).setUndeletable(true);

			typesBuilder.getDefaultSchema(ContainerRecord.SCHEMA_TYPE).create(ContainerRecord.LINEAR_SIZE_SUM)
					.setType(MetadataValueType.NUMBER).setEssential(false).setUndeletable(true).defineDataEntry().asSum(
					typesBuilder.getDefaultSchema(Folder.SCHEMA_TYPE).getMetadata(Folder.CONTAINER),
					typesBuilder.getDefaultSchema(Folder.SCHEMA_TYPE).getMetadata(Folder.LINEAR_SIZE)
			);

			typesBuilder.getDefaultSchema(ContainerRecord.SCHEMA_TYPE).create(ContainerRecord.LINEAR_SIZE)
					.setType(MetadataValueType.NUMBER).setEssential(false).setUndeletable(true)
					.defineDataEntry().asCalculated(ContainerRecordLinearSizeCalculator.class);

			typesBuilder.getDefaultSchema(ContainerRecord.SCHEMA_TYPE).create(ContainerRecord.AVAILABLE_SIZE)
					.setType(MetadataValueType.NUMBER).setEssential(false).setUndeletable(true)
					.defineDataEntry().asCalculated(ContainerRecordAvailableSizeCalculator.class);

			typesBuilder.getDefaultSchema(ContainerRecord.SCHEMA_TYPE).getMetadata(ContainerRecord.FILL_RATIO_ENTRED).setEnabled(false);
		}

		private void migrateStorageSpaceMetadatas(MetadataSchemaTypesBuilder typesBuilder) {
			typesBuilder.getSchema(StorageSpace.DEFAULT_SCHEMA).defineValidators().add(StorageSpaceValidator.class);

			typesBuilder.getDefaultSchema(StorageSpace.SCHEMA_TYPE).create(StorageSpace.LINEAR_SIZE_ENTERED)
					.setType(MetadataValueType.NUMBER).setEssential(false).setUndeletable(true);

			typesBuilder.getDefaultSchema(StorageSpace.SCHEMA_TYPE).create(StorageSpace.LINEAR_SIZE_SUM)
					.setType(MetadataValueType.NUMBER).setEssential(false).setUndeletable(true).defineDataEntry().asSum(
					typesBuilder.getDefaultSchema(ContainerRecord.SCHEMA_TYPE).getMetadata(ContainerRecord.STORAGE_SPACE),
					typesBuilder.getDefaultSchema(ContainerRecord.SCHEMA_TYPE).getMetadata(ContainerRecord.CAPACITY)
			);

			typesBuilder.getDefaultSchema(StorageSpace.SCHEMA_TYPE).create(StorageSpace.CHILD_LINEAR_SIZE_SUM)
					.setType(MetadataValueType.NUMBER).setEssential(false).setUndeletable(true).defineDataEntry().asSum(
					typesBuilder.getDefaultSchema(StorageSpace.SCHEMA_TYPE).getMetadata(StorageSpace.PARENT_STORAGE_SPACE),
					typesBuilder.getDefaultSchema(StorageSpace.SCHEMA_TYPE).getMetadata(StorageSpace.CAPACITY)
			);

			typesBuilder.getDefaultSchema(StorageSpace.SCHEMA_TYPE).create(StorageSpace.LINEAR_SIZE)
					.setType(MetadataValueType.NUMBER).setEssential(false).setUndeletable(true)
					.defineDataEntry().asCalculated(StorageSpaceLinearSizeCalculator.class);

			typesBuilder.getDefaultSchema(StorageSpace.SCHEMA_TYPE).create(StorageSpace.AVAILABLE_SIZE)
					.setType(MetadataValueType.NUMBER).setEssential(false).setUndeletable(true)
					.defineDataEntry().asCalculated(StorageSpaceAvailableSizeCalculator.class);

			typesBuilder.getDefaultSchema(StorageSpace.SCHEMA_TYPE).create(StorageSpace.CONTAINER_TYPE)
					.setType(MetadataValueType.REFERENCE).setMultivalue(true).setEssential(false).setUndeletable(true)
					.defineReferencesTo(typesBuilder.getSchemaType(ContainerRecordType.SCHEMA_TYPE));

			MetadataBuilder parentStorage = typesBuilder.getDefaultSchema(StorageSpace.SCHEMA_TYPE).get(StorageSpace.PARENT_STORAGE_SPACE);
			typesBuilder.getDefaultSchema(StorageSpace.SCHEMA_TYPE).create(StorageSpace.NUMBER_OF_CHILD)
					.setType(MetadataValueType.NUMBER).setEssential(false).setUndeletable(true)
					.defineDataEntry().asReferenceCount(parentStorage);
		}
	}

	public void migrateDisplayConfigs(AppLayerFactory appLayerFactory, String collection) {
		SchemasDisplayManager manager = appLayerFactory.getMetadataSchemasDisplayManager();
		SchemaTypesDisplayTransactionBuilder transactionBuilder = manager.newTransactionBuilderFor(collection);

		transactionBuilder.add(manager.getMetadata(collection, StorageSpace.DEFAULT_SCHEMA + "_" + StorageSpace.CONTAINER_TYPE)
				.withInputType(MetadataInputType.LOOKUP));
		transactionBuilder.add(manager.getMetadata(collection, StorageSpace.DEFAULT_SCHEMA + "_" + StorageSpace.AVAILABLE_SIZE).withVisibleInAdvancedSearchStatus(true));
		transactionBuilder.add(manager.getSchema(collection, StorageSpace.DEFAULT_SCHEMA).withNewFormAndDisplayMetadatas(StorageSpace.DEFAULT_SCHEMA + "_" + StorageSpace.CONTAINER_TYPE));
		transactionBuilder.add(manager.getMetadata(collection, ContainerRecord.DEFAULT_SCHEMA + "_" + ContainerRecord.AVAILABLE_SIZE).withVisibleInAdvancedSearchStatus(true));

		manager.execute(transactionBuilder.build());
	}

	private void reloadEmailTemplates() {
		if (appLayerFactory.getModelLayerFactory().getCollectionsListManager().getCollectionLanguages(collection).get(0).equals("en")) {
			reloadEmailTemplate("alertWhenDecommissioningListCreatedTemplate_en.html", RMEmailTemplateConstants.DECOMMISSIONING_LIST_CREATION_TEMPLATE_ID);
		} else {
			reloadEmailTemplate("alertWhenDecommissioningListCreatedTemplate.html", RMEmailTemplateConstants.DECOMMISSIONING_LIST_CREATION_TEMPLATE_ID);
		}
	}

	private void reloadEmailTemplate(final String templateFileName, final String templateId) {
		final InputStream templateInputStream = migrationResourcesProvider.getStream(templateFileName);

		try {
			appLayerFactory.getModelLayerFactory().getEmailTemplatesManager().addCollectionTemplateIfInexistent(templateId, collection, templateInputStream);
		} catch (IOException | ConfigManagerException.OptimisticLockingConfiguration e) {
			throw new RuntimeException(e);
		} finally {
			IOUtils.closeQuietly(templateInputStream);
		}
	}

	private void updatePermissions() {
		RolesManager roleManager = appLayerFactory.getModelLayerFactory().getRolesManager();
		List<Role> allRoles = roleManager.getAllRoles(collection);
		for (Role role : allRoles) {
			if (roleManager.hasPermission(collection, role.getCode(), RMPermissionsTo.CREATE_DECOMMISSIONING_LIST)) {
				roleManager.updateRole(role.withNewPermissions(asList(RMPermissionsTo.CREATE_TRANSFER_DECOMMISSIONING_LIST,
						RMPermissionsTo.EDIT_TRANSFER_DECOMMISSIONING_LIST)));
			}
		}
	}
}