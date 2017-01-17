package com.constellio.app.modules.rm;

import static com.constellio.app.ui.i18n.i18n.$;
import static com.constellio.model.services.search.query.logical.LogicalSearchQueryOperators.from;
import static java.util.Arrays.asList;

import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import com.constellio.app.entities.modules.ComboMigrationScript;
import com.constellio.app.entities.modules.InstallableSystemModule;
import com.constellio.app.entities.modules.MigrationScript;
import com.constellio.app.entities.modules.ModuleWithComboMigration;
import com.constellio.app.entities.navigation.NavigationConfig;
import com.constellio.app.extensions.AppLayerCollectionExtensions;
import com.constellio.app.modules.rm.constants.RMPermissionsTo;
import com.constellio.app.modules.rm.constants.RMRoles;
import com.constellio.app.modules.rm.extensions.RMCheckInAlertsRecordExtension;
import com.constellio.app.modules.rm.extensions.RMDocumentExtension;
import com.constellio.app.modules.rm.extensions.RMDownloadContentVersionLinkExtension;
import com.constellio.app.modules.rm.extensions.RMEmailDocumentRecordExtension;
import com.constellio.app.modules.rm.extensions.RMFolderExtension;
import com.constellio.app.modules.rm.extensions.RMGenericRecordPageExtension;
import com.constellio.app.modules.rm.extensions.RMModulePageExtension;
import com.constellio.app.modules.rm.extensions.RMOldSchemasBlockageRecordExtension;
import com.constellio.app.modules.rm.extensions.RMRecordAppExtension;
import com.constellio.app.modules.rm.extensions.RMRecordNavigationExtension;
import com.constellio.app.modules.rm.extensions.RMSchemasLogicalDeleteExtension;
import com.constellio.app.modules.rm.extensions.RMSearchPageExtension;
import com.constellio.app.modules.rm.extensions.RMSystemCheckExtension;
import com.constellio.app.modules.rm.extensions.RMTaxonomyPageExtension;
import com.constellio.app.modules.rm.extensions.RMUserRecordExtension;
import com.constellio.app.modules.rm.extensions.api.RMModuleExtensions;
import com.constellio.app.modules.rm.extensions.app.BatchProcessingRecordFactoryExtension;
import com.constellio.app.modules.rm.extensions.app.RMBatchProcessingExtension;
import com.constellio.app.modules.rm.extensions.app.RMCmisExtension;
import com.constellio.app.modules.rm.extensions.app.RMRecordExportExtension;
import com.constellio.app.modules.rm.extensions.imports.DocumentRuleImportExtension;
import com.constellio.app.modules.rm.extensions.imports.FolderRuleImportExtension;
import com.constellio.app.modules.rm.extensions.imports.RetentionRuleImportExtension;
import com.constellio.app.modules.rm.extensions.schema.RMTrashSchemaExtension;
import com.constellio.app.modules.rm.migrations.*;
import com.constellio.app.modules.rm.model.CopyRetentionRule;
import com.constellio.app.modules.rm.model.CopyRetentionRuleBuilder;
import com.constellio.app.modules.rm.navigation.RMNavigationConfiguration;
import com.constellio.app.modules.rm.services.RMSchemasRecordsServices;
import com.constellio.app.modules.rm.wrappers.AdministrativeUnit;
import com.constellio.app.modules.rm.wrappers.Category;
import com.constellio.app.modules.rm.wrappers.RetentionRule;
import com.constellio.app.modules.tasks.TaskModule;
import com.constellio.app.services.factories.AppLayerFactory;
import com.constellio.model.entities.configs.SystemConfiguration;
import com.constellio.model.entities.records.Record;
import com.constellio.model.entities.records.Transaction;
import com.constellio.model.entities.schemas.MetadataSchemaType;
import com.constellio.model.extensions.ModelLayerCollectionExtensions;
import com.constellio.model.services.factories.ModelLayerFactory;
import com.constellio.model.services.records.RecordServicesException;
import com.constellio.model.services.records.cache.CacheConfig;
import com.constellio.model.services.records.cache.RecordsCache;
import com.constellio.model.services.search.query.logical.LogicalSearchQuery;

public class ConstellioRMModule implements InstallableSystemModule, ModuleWithComboMigration {
	public static final String ID = "rm";
	public static final String NAME = "Constellio RM";

	public static final int DEFAULT_VOLATILE_FOLDER_CACHE_SIZE = 10000;
	public static final int DEFAULT_VOLATILE_DOCUMENTS_CACHE_SIZE = 100;

	@Override
	public String getName() {
		return NAME;
	}

	@Override
	public String getId() {
		return ID;
	}

	@Override
	public String getPublisher() {
		return "Constellio";
	}

	@Override
	public List<MigrationScript> getMigrationScripts() {
		return asList(
				new RMMigrationTo5_0_1(),
				new RMMigrationTo5_0_2(),
				new RMMigrationTo5_0_3(),
				new RMMigrationTo5_0_4(),
				new RMMigrationTo5_0_4_1(),
				new RMMigrationTo5_0_5(),
				new RMMigrationTo5_0_6(),
				new RMMigrationTo5_0_7(),
				new RMMigrationTo5_1_0_3(),
				new RMMigrationTo5_1_0_4(),
				new RMMigrationTo5_1_0_6(),
				new RMMigrationTo5_1_2(),
				new RMMigrationTo5_1_2_2(),
				new RMMigrationTo5_1_3(),
				new RMMigrationTo5_1_3(),
				new RMMigrationTo5_1_4_1(),
				new RMMigrationTo5_1_5(),
				new RMMigrationTo5_1_7(),
				new RMMigrationTo5_1_9(),
				new RMMigrationTo6_1(),
				new RMMigrationTo6_1_4(),
				new RMMigrationTo6_2(),
				new RMMigrationTo6_2_0_7(),
				new RMMigrationTo6_3(),
				new RMMigrationTo6_4(),
				new RMMigrationTo6_5(),
				new RMMigrationTo6_5_1(),
				new RMMigrationTo6_5_7(),
				new RMMigrationTo6_5_20(),
				new RMMigrationTo6_5_21(),
				new RMMigrationTo6_5_33(),
				new RMMigrationTo6_5_34(),
				new RMMigrationTo6_5_36(),
				new RMMigrationTo6_5_37(),
				new RMMigrationTo6_5_50(),
				new RMMigrationTo6_5_54(),
				new RMMigrationTo6_6(),
				new RMMigrationTo6_7()
		);
	}

	@Override
	public ComboMigrationScript getComboMigrationScript() {
		return new RMMigrationCombo();
	}

	@Override
	public List<SystemConfiguration> getConfigurations() {
		return Collections.unmodifiableList(RMConfigs.configurations);
	}

	@Override
	public Map<String, List<String>> getPermissions() {
		return RMPermissionsTo.PERMISSIONS.getGrouped();
	}

	@Override
	public List<String> getRolesForCreator() {
		return asList(RMRoles.RGD);
	}

	@Override
	public boolean isComplementary() {
		return false;
	}

	@Override
	public List<String> getDependencies() {
		return asList(TaskModule.ID);
	}

	@Override
	public void configureNavigation(NavigationConfig config) {
		RMNavigationConfiguration.configureNavigation(config);
	}

	@Override
	public void start(String collection, AppLayerFactory appLayerFactory) {
		setupModelLayerExtensions(collection, appLayerFactory);
		setupAppLayerExtensions(collection, appLayerFactory);
	}

	@Override
	public void stop(String collection, AppLayerFactory appLayerFactory) {
	}

	@Override
	public void addDemoData(String collection, AppLayerFactory appLayerFactory) {
		RMSchemasRecordsServices rm = new RMSchemasRecordsServices(collection, appLayerFactory);
		Transaction transaction = new Transaction();

		AdministrativeUnit adminUnit = rm.newAdministrativeUnit().setCode("1").setTitle($("RMDemoData.adminUnit"));
		transaction.add(adminUnit);

		CopyRetentionRuleBuilder copyBuilder = CopyRetentionRuleBuilder.sequential(appLayerFactory);
		CopyRetentionRule principal888_5_C = copyBuilder.newPrincipal(asList(rm.PA(), rm.DM()), "888-5-C");
		CopyRetentionRule secondary888_0_D = copyBuilder.newSecondary(asList(rm.PA(), rm.DM()), "888-0-D");
		RetentionRule retentionRule = rm.newRetentionRule().setCode("R1").setTitle($("RMDemoData.retentionRule"))
				.setAdministrativeUnits(asList(adminUnit.getId())).setApproved(true)
				.setCopyRetentionRules(asList(principal888_5_C, secondary888_0_D));
		transaction.add(retentionRule);

		Category category10 = rm.newCategory().setCode("10").setTitle($("RMDemoData.category10")).setRetentionRules(
				asList(retentionRule.getId()));
		transaction.add(category10);

		transaction.add(rm.newCategory().setCode("11").setTitle($("RMDemoData.category11"))
				.setParent(category10.getId()).setRetentionRules(asList(retentionRule.getId())));

		transaction.add(rm.newCategory().setCode("12").setTitle($("RMDemoData.category12"))
				.setParent(category10.getId()).setRetentionRules(asList(retentionRule.getId())));

		transaction.add(rm.newCategory().setCode("13").setTitle($("RMDemoData.category13"))
				.setParent(category10.getId()).setRetentionRules(asList(retentionRule.getId())));

		try {
			appLayerFactory.getModelLayerFactory().newRecordServices().execute(transaction);
		} catch (RecordServicesException e) {
			throw new RuntimeException(e);
		}
	}

	private void setupAppLayerExtensions(String collection, AppLayerFactory appLayerFactory) {
		AppLayerCollectionExtensions extensions = appLayerFactory.getExtensions().forCollection(collection);

		extensions.schemaTypeAccessExtensions.add(new RMGenericRecordPageExtension());
		extensions.taxonomyAccessExtensions.add(new RMTaxonomyPageExtension(collection));
		extensions.pageAccessExtensions.add(new RMModulePageExtension());
		extensions.downloadContentVersionLinkExtensions.add(new RMDownloadContentVersionLinkExtension());
		extensions.cmisExtensions.add(new RMCmisExtension(collection, appLayerFactory));
		extensions.recordAppExtensions.add(new RMRecordAppExtension(collection, appLayerFactory));
		extensions.recordNavigationExtensions.add(new RMRecordNavigationExtension());
		extensions.searchPageExtensions.add(new RMSearchPageExtension(appLayerFactory));
		extensions.batchProcessingExtensions.add(new RMBatchProcessingExtension(collection, appLayerFactory));
		extensions.recordFieldFactoryExtensions.add(new BatchProcessingRecordFactoryExtension());
		extensions.moduleExtensionsMap.put(ID, new RMModuleExtensions(appLayerFactory));
		extensions.systemCheckExtensions.add(new RMSystemCheckExtension(collection, appLayerFactory));
		extensions.recordExportExtensions.add(new RMRecordExportExtension(collection, appLayerFactory));
	}

	private void setupModelLayerExtensions(String collection, AppLayerFactory appLayerFactory) {
		ModelLayerFactory modelLayerFactory = appLayerFactory.getModelLayerFactory();
		ModelLayerCollectionExtensions extensions = modelLayerFactory.getExtensions().forCollection(collection);

		extensions.recordExtensions.add(new RMSchemasLogicalDeleteExtension(collection, modelLayerFactory));
		extensions.recordExtensions.add(new RMUserRecordExtension(collection, modelLayerFactory));
		extensions.recordExtensions.add(new RMEmailDocumentRecordExtension(collection, modelLayerFactory));
		extensions.recordExtensions.add(new RMOldSchemasBlockageRecordExtension());
		extensions.recordExtensions.add(new RMCheckInAlertsRecordExtension(collection, modelLayerFactory));
		extensions.recordExtensions.add(new RMFolderExtension(collection, modelLayerFactory));
		extensions.recordExtensions.add(new RMDocumentExtension(collection, appLayerFactory));
		extensions.recordImportExtensions.add(new RetentionRuleImportExtension(collection, modelLayerFactory));
		extensions.recordImportExtensions.add(new FolderRuleImportExtension(collection, modelLayerFactory));
		extensions.recordImportExtensions.add(new DocumentRuleImportExtension(collection, modelLayerFactory));
		extensions.schemaExtensions.add(new RMTrashSchemaExtension());

		RMSchemasRecordsServices rm = new RMSchemasRecordsServices(collection, modelLayerFactory);
		RecordsCache cache = modelLayerFactory.getRecordsCaches().getCache(collection);

		for (MetadataSchemaType type : rm.valueListSchemaTypes()) {
			if (cache.isConfigured(type)) {
				cache.removeCache(type.getCode());
			}
			cache.configureCache(CacheConfig.permanentCache(type));
		}

		if (cache.isConfigured(AdministrativeUnit.SCHEMA_TYPE)) {
			cache.removeCache(AdministrativeUnit.SCHEMA_TYPE);
		}
		cache.configureCache(CacheConfig.permanentCache(rm.administrativeUnit.schemaType()));

		if (cache.isConfigured(Category.SCHEMA_TYPE)) {
			cache.removeCache(Category.SCHEMA_TYPE);
		}
		cache.configureCache(CacheConfig.permanentCache(rm.category.schemaType()));

		cache.configureCache(CacheConfig.permanentCache(rm.retentionRule.schemaType()));
		cache.configureCache(CacheConfig.permanentCache(rm.uniformSubdivision.schemaType()));
		cache.configureCache(CacheConfig.permanentCache(rm.containerRecord.schemaType()));
		if (!cache.isConfigured(rm.authorizationDetails.schemaType())) {
			cache.configureCache(CacheConfig.permanentCache(rm.authorizationDetails.schemaType()));
			Iterator<Record> authsIterator = modelLayerFactory.newSearchServices().recordsIterator(new LogicalSearchQuery(
					from(rm.authorizationDetails.schemaType()).returnAll()), 10000);
			while (authsIterator.hasNext()) {
				authsIterator.next();
			}
		}
		cache.configureCache(CacheConfig.volatileCache(rm.folder.schemaType(), DEFAULT_VOLATILE_FOLDER_CACHE_SIZE));
		cache.configureCache(CacheConfig.volatileCache(rm.documentSchemaType(), DEFAULT_VOLATILE_DOCUMENTS_CACHE_SIZE));

	}

	@Override
	public void start(AppLayerFactory appLayerFactory) {
		RMNavigationConfiguration.configureNavigation(appLayerFactory.getNavigatorConfigurationService());
	}

	@Override
	public void stop(AppLayerFactory appLayerFactory) {

	}
}
