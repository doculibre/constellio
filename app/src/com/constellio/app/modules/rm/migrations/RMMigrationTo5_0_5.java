package com.constellio.app.modules.rm.migrations;

import static com.constellio.data.utils.LangUtils.withoutDuplicates;
import static java.util.Arrays.asList;

import java.util.ArrayList;
import java.util.List;

import com.constellio.app.entities.modules.MetadataSchemasAlterationHelper;
import com.constellio.app.entities.modules.MigrationResourcesProvider;
import com.constellio.app.entities.modules.MigrationScript;
import com.constellio.app.entities.schemasDisplay.enums.MetadataInputType;
import com.constellio.app.modules.rm.constants.RMPermissionsTo;
import com.constellio.app.modules.rm.constants.RMRoles;
import com.constellio.app.modules.rm.constants.RMTaxonomies;
import com.constellio.app.modules.rm.model.enums.DecommissioningMonth;
import com.constellio.app.modules.rm.wrappers.AdministrativeUnit;
import com.constellio.app.modules.rm.wrappers.Category;
import com.constellio.app.modules.rm.wrappers.ContainerRecord;
import com.constellio.app.modules.rm.wrappers.DecommissioningList;
import com.constellio.app.modules.rm.wrappers.Document;
import com.constellio.app.modules.rm.wrappers.Email;
import com.constellio.app.modules.rm.wrappers.FilingSpace;
import com.constellio.app.modules.rm.wrappers.Folder;
import com.constellio.app.modules.rm.wrappers.RetentionRule;
import com.constellio.app.modules.rm.wrappers.StorageSpace;
import com.constellio.app.modules.rm.wrappers.UniformSubdivision;
import com.constellio.app.services.factories.AppLayerFactory;
import com.constellio.app.services.schemasDisplay.SchemaDisplayManagerTransaction;
import com.constellio.app.services.schemasDisplay.SchemaTypesDisplayTransactionBuilder;
import com.constellio.app.services.schemasDisplay.SchemasDisplayManager;
import com.constellio.model.entities.Taxonomy;
import com.constellio.model.entities.records.wrappers.User;
import com.constellio.model.entities.schemas.Metadata;
import com.constellio.model.entities.schemas.MetadataSchema;
import com.constellio.model.entities.schemas.MetadataSchemaTypes;
import com.constellio.model.entities.schemas.MetadataValueType;
import com.constellio.model.entities.schemas.Schemas;
import com.constellio.model.entities.security.Role;
import com.constellio.model.services.factories.ModelLayerFactory;
import com.constellio.model.services.schemas.builders.MetadataSchemaBuilder;
import com.constellio.model.services.schemas.builders.MetadataSchemaTypesBuilder;
import com.constellio.model.services.taxonomies.TaxonomiesManager;

public class RMMigrationTo5_0_5 implements MigrationScript {
	@Override
	public String getVersion() {
		return "5.0.5";
	}

	@Override
	public void migrate(String collection, MigrationResourcesProvider migrationResourcesProvider,
			AppLayerFactory appLayerFactory) {
		new SchemaAlterationFor5_0_5(collection, migrationResourcesProvider, appLayerFactory).migrate();
		setupDisplayConfig(collection, appLayerFactory);
		setupRoles(collection, appLayerFactory.getModelLayerFactory());
		setAdminUnitLabel(collection, appLayerFactory.getModelLayerFactory(), migrationResourcesProvider);
	}

	private void setAdminUnitLabel(String collection, ModelLayerFactory modelLayerFactory,
			MigrationResourcesProvider migrationResourcesProvider) {
		TaxonomiesManager manager = modelLayerFactory.getTaxonomiesManager();
		Taxonomy adminUnitsTaxo = manager.getEnabledTaxonomyWithCode(collection, RMTaxonomies.ADMINISTRATIVE_UNITS);
		adminUnitsTaxo = adminUnitsTaxo.withTitle(migrationResourcesProvider.getDefaultLanguageString("taxo.admUnits"));
		manager.editTaxonomy(adminUnitsTaxo);

	}

	class SchemaAlterationFor5_0_5 extends MetadataSchemasAlterationHelper {

		MetadataSchemaTypes types;

		protected SchemaAlterationFor5_0_5(String collection, MigrationResourcesProvider migrationResourcesProvider,
				AppLayerFactory appLayerFactory) {
			super(collection, migrationResourcesProvider, appLayerFactory);
			types = appLayerFactory.getModelLayerFactory().getMetadataSchemasManager().getSchemaTypes(collection);
		}

		public String getVersion() {
			return "5.0.5";
		}

		@Override
		protected void migrate(MetadataSchemaTypesBuilder typesBuilder) {
			//AdministrativeUnit
			typesBuilder.getSchema(AdministrativeUnit.DEFAULT_SCHEMA).createUndeletable(
					AdministrativeUnit.DECOMMISSIONING_MONTH).defineAsEnum(DecommissioningMonth.class);

			//Document
			typesBuilder.getSchema(Document.DEFAULT_SCHEMA).createUndeletable(Document.FOLDER_BORROWED)
					.setType(MetadataValueType.BOOLEAN);

			//Folder
			typesBuilder.getSchema(Folder.DEFAULT_SCHEMA).createUndeletable(Folder.BORROWED)
					.setType(MetadataValueType.BOOLEAN);
			typesBuilder.getSchema(Folder.DEFAULT_SCHEMA).createUndeletable(Folder.BORROW_DATE)
					.setType(MetadataValueType.DATE_TIME);
			typesBuilder.getSchema(Folder.DEFAULT_SCHEMA).createUndeletable(Folder.BORROW_PREVIEW_RETURN_DATE)
					.setType(MetadataValueType.DATE);
			typesBuilder.getSchema(Folder.DEFAULT_SCHEMA).createUndeletable(Folder.BORROW_RETURN_DATE)
					.setType(MetadataValueType.DATE_TIME);
			typesBuilder.getSchema(Folder.DEFAULT_SCHEMA).createUndeletable(Folder.BORROW_USER)
					.setType(MetadataValueType.REFERENCE).defineReferencesTo(typesBuilder.getSchemaType(User.SCHEMA_TYPE));
			typesBuilder.getSchema(Folder.DEFAULT_SCHEMA).createUndeletable(Folder.BORROW_USER_ENTERED)
					.setType(MetadataValueType.REFERENCE).defineReferencesTo(typesBuilder.getSchemaType(User.SCHEMA_TYPE));

			//Container
			typesBuilder.getSchema(ContainerRecord.DEFAULT_SCHEMA).createUndeletable(ContainerRecord.BORROWED)
					.setType(MetadataValueType.BOOLEAN);

			makeAllMetadatasEssentialsInTypeExcept(typesBuilder.getSchema(AdministrativeUnit.DEFAULT_SCHEMA),
					AdministrativeUnit.ADRESS, AdministrativeUnit.COMMENTS, AdministrativeUnit.DECOMMISSIONING_MONTH,
					AdministrativeUnit.DESCRIPTION);

			makeAllMetadatasEssentialsInTypeExcept(typesBuilder.getSchema(Category.DEFAULT_SCHEMA),
					Category.COMMENTS, Category.KEYWORDS, Category.DESCRIPTION);

			makeAllMetadatasEssentialsInTypeExcept(typesBuilder.getSchema(ContainerRecord.DEFAULT_SCHEMA),
					ContainerRecord.COMMENTS, ContainerRecord.DESCRIPTION);

			makeAllMetadatasEssentialsInTypeExcept(typesBuilder.getSchema(DecommissioningList.DEFAULT_SCHEMA),
					DecommissioningList.COMMENTS, DecommissioningList.DESCRIPTION);

			makeAllMetadatasEssentialsInTypeExcept(typesBuilder.getSchema(Document.DEFAULT_SCHEMA),
					Document.COMMENTS, Document.DESCRIPTION, Document.KEYWORDS);

			makeAllMetadatasEssentialsInTypeExcept(typesBuilder.getSchema(Email.SCHEMA));

			makeAllMetadatasEssentialsInTypeExcept(typesBuilder.getSchema(FilingSpace.DEFAULT_SCHEMA),
					FilingSpace.COMMENTS, FilingSpace.DESCRIPTION);

			makeAllMetadatasEssentialsInTypeExcept(typesBuilder.getSchema(Folder.DEFAULT_SCHEMA),
					Category.COMMENTS, Category.KEYWORDS, Category.DESCRIPTION);

			makeAllMetadatasEssentialsInTypeExcept(typesBuilder.getSchema(RetentionRule.DEFAULT_SCHEMA),
					RetentionRule.CONFIDENTIAL_DOCUMENTS, RetentionRule.CORPUS, RetentionRule.CORPUS_RULE_NUMBER,
					RetentionRule.DESCRIPTION, RetentionRule.ESSENTIAL_DOCUMENTS, RetentionRule.GENERAL_COMMENT,
					RetentionRule.HISTORY, RetentionRule.JURIDIC_REFERENCE, RetentionRule.KEYWORDS);

			makeAllMetadatasEssentialsInTypeExcept(typesBuilder.getSchema(StorageSpace.DEFAULT_SCHEMA),
					StorageSpace.COMMENTS, StorageSpace.DESCRIPTION);

			makeAllMetadatasEssentialsInTypeExcept(typesBuilder.getSchema(UniformSubdivision.DEFAULT_SCHEMA),
					UniformSubdivision.COMMENTS, UniformSubdivision.DESCRIPTION);

			//			setEmailLabelsToDefaultValueInsteadOfInit(typesBuilder.getSchema("document_email"));
		}

		//		private void setEmailLabelsToDefaultValueInsteadOfInit(MetadataSchemaBuilder emailSchemaBuilder) {
		//
		//			for (MetadataBuilder metadata : emailSchemaBuilder.getMetadatas()) {
		//				if (metadata.getLabel() != null && metadata.getLabel().startsWith("init.") && metadata.getInheritance() != null) {
		//					metadata.setLabel(null);
		//				}
		//			}
		//		}

		private void makeAllMetadatasEssentialsInTypeExcept(MetadataSchemaBuilder schemaBuilder, String... except) {

			MetadataSchema schema = types.getSchema(schemaBuilder.getCode());

			List<String> exceptList = asList(except);

			for (Metadata metadata : schema.getMetadatas()) {
				if (!metadata.getLocalCode().startsWith("USR") && !exceptList.contains(metadata.getLocalCode())
						&& metadata.getInheritance() == null) {
					if (!Schemas.ERROR_ON_PHYSICAL_DELETION.hasSameCode(metadata)
							&& !Schemas.LOGICALLY_DELETED_ON.hasSameCode(metadata)) {
						schemaBuilder.getMetadata(metadata.getLocalCode()).setEnabled(true);
						schemaBuilder.getMetadata(metadata.getLocalCode()).setEssential(true);
					}
				}
			}

		}
	}

	private void setupDisplayConfig(String collection, AppLayerFactory appLayerFactory) {
		SchemasDisplayManager manager = appLayerFactory.getMetadataSchemasDisplayManager();
		ModelLayerFactory modelLayerFactory = appLayerFactory.getModelLayerFactory();

		manager.enableAllMetadatasInAdvancedSearch(collection, Folder.SCHEMA_TYPE);
		manager.enableAllMetadatasInAdvancedSearch(collection, Document.SCHEMA_TYPE);
		manager.enableAllMetadatasInAdvancedSearch(collection, ContainerRecord.SCHEMA_TYPE);

		SchemaTypesDisplayTransactionBuilder transactionBuilder = manager.newTransactionBuilderFor(collection);

		transactionBuilder.in(AdministrativeUnit.SCHEMA_TYPE)
				.addToDisplay(AdministrativeUnit.DECOMMISSIONING_MONTH, AdministrativeUnit.FILING_SPACES_ADMINISTRATORS)
				.atTheEnd();

		transactionBuilder.in(AdministrativeUnit.SCHEMA_TYPE)
				.addToForm(AdministrativeUnit.DECOMMISSIONING_MONTH)
				.afterMetadata(AdministrativeUnit.PARENT);

		transactionBuilder.in(Folder.SCHEMA_TYPE)
				.addToDisplay(Folder.BORROWED, Folder.BORROW_DATE, Folder.BORROW_USER_ENTERED, Folder.BORROW_PREVIEW_RETURN_DATE)
				.beforeTheHugeCommentMetadata();

		transactionBuilder.in(Folder.SCHEMA_TYPE)
				.addToForm(Folder.BORROW_PREVIEW_RETURN_DATE)
				.atTheEnd();

		manager.execute(transactionBuilder.build());

		SchemaDisplayManagerTransaction transaction = new SchemaDisplayManagerTransaction();
		transaction.add(manager.getMetadata(collection, Folder.DEFAULT_SCHEMA + "_" + Folder.MEDIUM_TYPES)
				.withInputType(MetadataInputType.CHECKBOXES));

		manager.execute(transaction);
	}

	private void setupRoles(String collection, ModelLayerFactory modelLayerFactory) {

		Role userRole = modelLayerFactory.getRolesManager().getRole(collection, RMRoles.USER);
		Role managerRole = modelLayerFactory.getRolesManager().getRole(collection, RMRoles.MANAGER);
		Role rgdRole = modelLayerFactory.getRolesManager().getRole(collection, RMRoles.RGD);

		List<String> userPermissions = new ArrayList<>(userRole.getOperationPermissions());
		userPermissions.add(RMPermissionsTo.BORROW_FOLDER);
		userPermissions.add(RMPermissionsTo.MODIFY_SEMIACTIVE_BORROWED_FOLDER);

		List<String> managerPermissions = new ArrayList<>(managerRole.getOperationPermissions());
		managerPermissions.add(RMPermissionsTo.BORROW_FOLDER);
		managerPermissions.add(RMPermissionsTo.MODIFY_SEMIACTIVE_BORROWED_FOLDER);

		List<String> newRgdPermissions = new ArrayList<>(rgdRole.getOperationPermissions());
		newRgdPermissions.add(RMPermissionsTo.BORROW_FOLDER);
		newRgdPermissions.add(RMPermissionsTo.MODIFY_SEMIACTIVE_BORROWED_FOLDER);
		newRgdPermissions.add(RMPermissionsTo.MODIFY_INACTIVE_BORROWED_FOLDER);

		modelLayerFactory.getRolesManager().updateRole(userRole.withPermissions(withoutDuplicates(userPermissions)));
		modelLayerFactory.getRolesManager().updateRole(managerRole.withPermissions(withoutDuplicates(managerPermissions)));
		modelLayerFactory.getRolesManager().updateRole(rgdRole.withPermissions(withoutDuplicates(newRgdPermissions)));
	}
}
