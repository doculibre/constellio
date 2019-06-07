package com.constellio.app.services.migrations;

import com.constellio.app.entities.modules.ComboMigrationScript;
import com.constellio.app.entities.modules.MetadataSchemasAlterationHelper;
import com.constellio.app.entities.modules.MigrationResourcesProvider;
import com.constellio.app.entities.modules.MigrationScript;
import com.constellio.app.services.factories.AppLayerFactory;
import com.constellio.app.services.migrations.scripts.CoreMigrationTo_5_0_1;
import com.constellio.app.services.migrations.scripts.CoreMigrationTo_5_0_4;
import com.constellio.app.services.migrations.scripts.CoreMigrationTo_5_0_5;
import com.constellio.app.services.migrations.scripts.CoreMigrationTo_5_0_6_6;
import com.constellio.app.services.migrations.scripts.CoreMigrationTo_5_0_7;
import com.constellio.app.services.migrations.scripts.CoreMigrationTo_5_1_0;
import com.constellio.app.services.migrations.scripts.CoreMigrationTo_5_1_1_3;
import com.constellio.app.services.migrations.scripts.CoreMigrationTo_5_1_2;
import com.constellio.app.services.migrations.scripts.CoreMigrationTo_5_1_3;
import com.constellio.app.services.migrations.scripts.CoreMigrationTo_5_1_4;
import com.constellio.app.services.migrations.scripts.CoreMigrationTo_5_1_6;
import com.constellio.app.services.migrations.scripts.CoreMigrationTo_5_1_7;
import com.constellio.app.services.migrations.scripts.CoreMigrationTo_5_2;
import com.constellio.app.services.migrations.scripts.CoreMigrationTo_6_0;
import com.constellio.app.services.migrations.scripts.CoreMigrationTo_6_1;
import com.constellio.app.services.migrations.scripts.CoreMigrationTo_6_3;
import com.constellio.app.services.migrations.scripts.CoreMigrationTo_6_4;
import com.constellio.app.services.migrations.scripts.CoreMigrationTo_6_4_1;
import com.constellio.app.services.migrations.scripts.CoreMigrationTo_6_5;
import com.constellio.app.services.migrations.scripts.CoreMigrationTo_6_5_14;
import com.constellio.app.services.migrations.scripts.CoreMigrationTo_6_5_19;
import com.constellio.app.services.migrations.scripts.CoreMigrationTo_6_5_21;
import com.constellio.app.services.migrations.scripts.CoreMigrationTo_6_5_22;
import com.constellio.app.services.migrations.scripts.CoreMigrationTo_6_5_42;
import com.constellio.app.services.migrations.scripts.CoreMigrationTo_6_5_50;
import com.constellio.app.services.migrations.scripts.CoreMigrationTo_6_6;
import com.constellio.app.services.migrations.scripts.CoreMigrationTo_7_0;
import com.constellio.app.services.migrations.scripts.CoreMigrationTo_7_0_1;
import com.constellio.app.services.migrations.scripts.CoreMigrationTo_7_1;
import com.constellio.app.services.migrations.scripts.CoreMigrationTo_7_1_1;
import com.constellio.app.services.migrations.scripts.CoreMigrationTo_7_1_3_1;
import com.constellio.app.services.migrations.scripts.CoreMigrationTo_7_2;
import com.constellio.app.services.migrations.scripts.CoreMigrationTo_7_3;
import com.constellio.app.services.migrations.scripts.CoreMigrationTo_7_3_0_1;
import com.constellio.app.services.migrations.scripts.CoreMigrationTo_7_4;
import com.constellio.app.services.migrations.scripts.CoreMigrationTo_7_4_2;
import com.constellio.app.services.migrations.scripts.CoreMigrationTo_7_4_3;
import com.constellio.app.services.migrations.scripts.CoreMigrationTo_7_5;
import com.constellio.app.services.migrations.scripts.CoreMigrationTo_7_6;
import com.constellio.app.services.migrations.scripts.CoreMigrationTo_7_6_2;
import com.constellio.app.services.migrations.scripts.CoreMigrationTo_7_6_2_1;
import com.constellio.app.services.migrations.scripts.CoreMigrationTo_7_6_6;
import com.constellio.app.services.migrations.scripts.CoreMigrationTo_7_6_6_45;
import com.constellio.app.services.migrations.scripts.CoreMigrationTo_7_6_9;
import com.constellio.app.services.migrations.scripts.CoreMigrationTo_7_7_0_2;
import com.constellio.app.services.migrations.scripts.CoreMigrationTo_7_7_1;
import com.constellio.app.services.migrations.scripts.CoreMigrationTo_7_7_1_2;
import com.constellio.app.services.migrations.scripts.CoreMigrationTo_7_7_1_6;
import com.constellio.app.services.migrations.scripts.CoreMigrationTo_7_7_2;
import com.constellio.app.services.migrations.scripts.CoreMigrationTo_7_7_4;
import com.constellio.app.services.migrations.scripts.CoreMigrationTo_7_7_4_11;
import com.constellio.app.services.migrations.scripts.CoreMigrationTo_7_7_5;
import com.constellio.app.services.migrations.scripts.CoreMigrationTo_7_7_6;
import com.constellio.app.services.migrations.scripts.CoreMigrationTo_7_7_7;
import com.constellio.app.services.migrations.scripts.CoreMigrationTo_8_0_1;
import com.constellio.app.services.migrations.scripts.CoreMigrationTo_8_0_2;
import com.constellio.app.services.migrations.scripts.CoreMigrationTo_8_1;
import com.constellio.app.services.migrations.scripts.CoreMigrationTo_8_1_0_1;
import com.constellio.app.services.migrations.scripts.CoreMigrationTo_8_1_2;
import com.constellio.app.services.migrations.scripts.CoreMigrationTo_8_1_3;
import com.constellio.app.services.migrations.scripts.CoreMigrationTo_8_2;
import com.constellio.app.services.migrations.scripts.CoreMigrationTo_8_2_0;
import com.constellio.app.services.schemasDisplay.SchemaTypesDisplayTransactionBuilder;
import com.constellio.app.services.schemasDisplay.SchemasDisplayManager;
import com.constellio.model.entities.records.Transaction;
import com.constellio.model.entities.records.wrappers.Collection;
import com.constellio.model.entities.schemas.MetadataSchemaTypes;
import com.constellio.model.entities.schemas.Schemas;
import com.constellio.model.services.factories.ModelLayerFactory;
import com.constellio.model.services.migrations.ConstellioEIMConfigs;
import com.constellio.model.services.records.RecordServices;
import com.constellio.model.services.records.SchemasRecordsServices;
import com.constellio.model.services.schemas.builders.MetadataSchemaTypesBuilder;
import com.constellio.model.services.search.entities.SearchBoost;

import java.util.ArrayList;
import java.util.List;

import static com.constellio.app.ui.i18n.i18n.$;
import static java.util.Arrays.asList;

public class CoreMigrationCombo implements ComboMigrationScript {
	@Override
	public List<MigrationScript> getVersions() {
		List<MigrationScript> scripts = new ArrayList<>();

		scripts.add(new CoreMigrationTo_5_0_1());
		scripts.add(new CoreMigrationTo_5_0_4());
		scripts.add(new CoreMigrationTo_5_0_5());
		scripts.add(new CoreMigrationTo_5_0_6_6());
		scripts.add(new CoreMigrationTo_5_0_7());
		scripts.add(new CoreMigrationTo_5_1_0());
		scripts.add(new CoreMigrationTo_5_1_1_3());
		scripts.add(new CoreMigrationTo_5_1_2());
		scripts.add(new CoreMigrationTo_5_1_3());
		scripts.add(new CoreMigrationTo_5_1_4());
		scripts.add(new CoreMigrationTo_5_1_6());
		scripts.add(new CoreMigrationTo_5_1_7());
		scripts.add(new CoreMigrationTo_5_2());
		scripts.add(new CoreMigrationTo_6_0());
		scripts.add(new CoreMigrationTo_6_1());
		scripts.add(new CoreMigrationTo_6_3());
		scripts.add(new CoreMigrationTo_6_4());
		scripts.add(new CoreMigrationTo_6_4_1());
		scripts.add(new CoreMigrationTo_6_5());
		scripts.add(new CoreMigrationTo_6_5_14());
		scripts.add(new CoreMigrationTo_6_5_19());
		scripts.add(new CoreMigrationTo_6_5_21());
		scripts.add(new CoreMigrationTo_6_5_50());
		scripts.add(new CoreMigrationTo_6_5_22());
		scripts.add(new CoreMigrationTo_6_5_42());
		scripts.add(new CoreMigrationTo_6_6());
		scripts.add(new CoreMigrationTo_7_0());
		scripts.add(new CoreMigrationTo_7_0_1());
		scripts.add(new CoreMigrationTo_7_1());
		scripts.add(new CoreMigrationTo_7_1_1());
		scripts.add(new CoreMigrationTo_7_1_3_1());
		scripts.add(new CoreMigrationTo_7_2());
		scripts.add(new CoreMigrationTo_7_3());
		scripts.add(new CoreMigrationTo_7_3_0_1());
		scripts.add(new CoreMigrationTo_7_4());
		scripts.add(new CoreMigrationTo_7_4_2());
		scripts.add(new CoreMigrationTo_7_4_3());
		scripts.add(new CoreMigrationTo_7_5());
		scripts.add(new CoreMigrationTo_7_6());
		scripts.add(new CoreMigrationTo_7_6_2());
		scripts.add(new CoreMigrationTo_7_6_2_1());
		scripts.add(new CoreMigrationTo_7_6_6());
		scripts.add(new CoreMigrationTo_7_6_6_45());
		scripts.add(new CoreMigrationTo_7_6_9());
		scripts.add(new CoreMigrationTo_7_7_0_2());
		scripts.add(new CoreMigrationTo_7_7_1());
		scripts.add(new CoreMigrationTo_7_7_1_2());
		scripts.add(new CoreMigrationTo_7_7_1_6());
		scripts.add(new CoreMigrationTo_7_7_2());
		scripts.add(new CoreMigrationTo_7_7_4());
		scripts.add(new CoreMigrationTo_7_7_5());
		scripts.add(new CoreMigrationTo_7_7_4_11());
		scripts.add(new CoreMigrationTo_7_7_6());
		scripts.add(new CoreMigrationTo_7_7_7());
		scripts.add(new CoreMigrationTo_8_0_1());
		scripts.add(new CoreMigrationTo_8_0_2());
		scripts.add(new CoreMigrationTo_8_1());
		scripts.add(new CoreMigrationTo_8_1_0_1());
		scripts.add(new CoreMigrationTo_8_1_2());
		scripts.add(new CoreMigrationTo_8_1_3());
		scripts.add(new CoreMigrationTo_8_2());
		scripts.add(new CoreMigrationTo_8_2_0());

		return scripts;
	}

	GeneratedCoreMigrationCombo generatedFastCoreMigration;
	GeneratedSystemMigrationCombo generatedSystemMigrationCombo;

	@Override
	public String getVersion() {
		return getVersions().get(getVersions().size() - 1).getVersion();
	}

	@Override
	public void migrate(String collection, MigrationResourcesProvider migrationResourcesProvider,
						AppLayerFactory appLayerFactory)
			throws Exception {
		ModelLayerFactory modelLayerFactory = appLayerFactory.getModelLayerFactory();
		generatedFastCoreMigration = new GeneratedCoreMigrationCombo(collection, appLayerFactory,
				migrationResourcesProvider);
		generatedSystemMigrationCombo = new GeneratedSystemMigrationCombo(collection, appLayerFactory,
				migrationResourcesProvider);

		new SchemaAlteration(collection, migrationResourcesProvider, appLayerFactory).migrate();
		CoreMigrationTo_7_3.createKeyFileInZookeeperIfNeeded(collection, modelLayerFactory.getConfiguration());
		CoreMigrationTo_5_1_3.initEncryption(collection, migrationResourcesProvider, appLayerFactory);
		if (collection.equals(Collection.SYSTEM_COLLECTION)) {
			generatedSystemMigrationCombo.applyGeneratedRoles();
			generatedSystemMigrationCombo.applySchemasDisplay(appLayerFactory.getMetadataSchemasDisplayManager());
		} else {
			generatedFastCoreMigration.applyGeneratedRoles();
			generatedFastCoreMigration.applySchemasDisplay(appLayerFactory.getMetadataSchemasDisplayManager());
		}
		adjustRoles(collection, modelLayerFactory);
		applySchemasDisplay2(collection, appLayerFactory.getMetadataSchemasDisplayManager());

		appLayerFactory.getModelLayerFactory().getSearchBoostManager().add(collection,
				new SearchBoost(SearchBoost.METADATA_TYPE, "title_s", $("title"), 20.0));
		RecordServices recordServices = appLayerFactory.getModelLayerFactory().newRecordServices();
		MetadataSchemaTypes types = appLayerFactory.getModelLayerFactory().getMetadataSchemasManager().getSchemaTypes(collection);

		recordServices.execute(createRecordTransaction(collection, migrationResourcesProvider, appLayerFactory, types));

		if (Collection.SYSTEM_COLLECTION.equals(collection)) {

			new CoreMigrationTo_6_0().createAdminUser(modelLayerFactory);
			//appLayerFactory.getModelLayerFactory().newAuthenticationService().changePassword("admin", "password");

		} else {

		}

		appLayerFactory.getModelLayerFactory().getSystemConfigurationsManager()
				.setValue(ConstellioEIMConfigs.TRASH_PURGE_DELAI, 90);

		//	changeAdminRoleNameIfMultilingualCollection(appLayerFactory, collection);
	}

	//	private void changeAdminRoleNameIfMultilingualCollection(AppLayerFactory appLayerFactory, String collection) {
	//		RolesManager rolesManager = appLayerFactory.getModelLayerFactory().getRolesManager();
	//		if (appLayerFactory.getCollectionsManager().getCollectionInfo(collection).getCollectionLanguages().size() > 1) {
	//			rolesManager.updateRole(rolesManager.getRole(collection, CoreRoles.ADMINISTRATOR)
	//					.withTitle("Administrateur / Administrator"));
	//		}
	//	}

	private void adjustRoles(String collection, ModelLayerFactory modelLayerFactory) {
		//			RolesManager rolesManager = modelLayerFactory.getRolesManager();
		//			Role role = rolesManager.getRole(collection, "ADM");
		//
		//			List<String> permissions = new ArrayList<>(role.getOperationPermissions());
		//			permissions.add("core.accessDeleteAllTemporaryRecords");
		//			permissions.add("core.deletePublicSavedSearch");
		//			permissions.add("core.manageSystemGroupsActivation");
		//			permissions.remove("core.manageSearchReports");
		//
		//			rolesManager.updateRole(role.withPermissions(permissions));

	}

	private void applySchemasDisplay2(String collection, SchemasDisplayManager manager) {
		SchemaTypesDisplayTransactionBuilder transaction = manager.newTransactionBuilderFor(collection);

		transaction.add(manager.getSchema(collection, "user_default").withDisplayMetadataCodes(asList(
				"user_default_username",
				"user_default_firstname",
				"user_default_lastname",
				"user_default_title",
				"user_default_email",
				"user_default_userroles",
				"user_default_groups",
				"user_default_jobTitle",
				"user_default_phone",
				"user_default_status",
				"user_default_createdOn",
				"user_default_modifiedOn",
				"user_default_allroles"
		)));

		transaction.in("savedSearch").addToDisplay("resultsViewMode").beforeMetadata("schemaFilter");

		transaction.in("savedSearch").addToForm("resultsViewMode").beforeMetadata("schemaFilter");

	}

	private Transaction createRecordTransaction(String collection,
												MigrationResourcesProvider migrationResourcesProvider,
												AppLayerFactory appLayerFactory, MetadataSchemaTypes types) {
		Transaction transaction = new Transaction();

		SchemasRecordsServices schemas = new SchemasRecordsServices(types.getCollection(), appLayerFactory.getModelLayerFactory());

		transaction.add(schemas.newFacetField().setOrder(0)
				.setTitles(migrationResourcesProvider.getLanguagesString("init.facet.type"))
				.setActive(true)
				.setOpenByDefault(true)
				.setFieldDataStoreCode(Schemas.SCHEMA.getDataStoreCode()));

		transaction.add(schemas.newFacetQuery().setOrder(1)
				.setTitles(migrationResourcesProvider.getLanguagesString("init.facet.createModification"))
				.setActive(false)
				.setOpenByDefault(true)
				.withQuery("modifiedOn_dt:[NOW-1MONTH TO NOW]", "Modifiés les 30 derniers jours")
				.withQuery("modifiedOn_dt:[NOW-7DAY TO NOW]", "Modifiés les 7 derniers jours")
				.withQuery("createdOn_dt:[NOW-1MONTH TO NOW]", "Créés les 30 derniers jours")
				.withQuery("createdOn_dt:[NOW-7DAY TO NOW]", "Créés les 7 derniers jours"));

		if (!Collection.SYSTEM_COLLECTION.equals(collection)) {
			CoreMigrationTo_8_0_1.createDefaultCapsuleLanguages(migrationResourcesProvider, transaction, schemas);
		}

		return transaction;
	}

	class SchemaAlteration extends MetadataSchemasAlterationHelper {

		protected SchemaAlteration(String collection,
								   MigrationResourcesProvider migrationResourcesProvider,
								   AppLayerFactory appLayerFactory) {
			super(collection, migrationResourcesProvider, appLayerFactory);
		}

		@Override
		protected void migrate(MetadataSchemaTypesBuilder typesBuilder) {

			if (Collection.SYSTEM_COLLECTION.equals(typesBuilder.getCollection())) {

				generatedSystemMigrationCombo.applyGeneratedSchemaAlteration(typesBuilder);
			} else {
				generatedFastCoreMigration.applyGeneratedSchemaAlteration(typesBuilder);

			}

			//
			//
			//			typesBuilder.getDefaultSchema(User.SCHEMA_TYPE).get(User.ADDRESS).addLabel(Language.French, "Adresse");
			//			typesBuilder.getDefaultSchema(User.SCHEMA_TYPE).get(User.FAX).addLabel(Language.French, "Télécopieur");
		}

	}
}
