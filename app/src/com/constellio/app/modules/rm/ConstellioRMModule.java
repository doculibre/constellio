package com.constellio.app.modules.rm;

import static com.constellio.app.ui.i18n.i18n.$;
import static com.constellio.model.services.records.cache.VolatileCacheInvalidationMethod.FIFO;
import static java.util.Arrays.asList;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import com.constellio.app.modules.rm.extensions.schema.RMExcelReportSchemaExtension;
import com.constellio.app.modules.rm.migrations.*;
import com.constellio.model.entities.records.wrappers.*;
import com.constellio.app.modules.rm.migrations.records.RMEmailMigrationTo7_7_1;
import com.constellio.app.modules.rm.migrations.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.constellio.app.entities.modules.ComboMigrationScript;
import com.constellio.app.entities.modules.InstallableSystemModule;
import com.constellio.app.entities.modules.InstallableSystemModuleWithRecordMigrations;
import com.constellio.app.entities.modules.MigrationScript;
import com.constellio.app.entities.modules.ModuleWithComboMigration;
import com.constellio.app.entities.navigation.NavigationConfig;
import com.constellio.app.extensions.AppLayerCollectionExtensions;
import com.constellio.app.modules.rm.constants.RMPermissionsTo;
import com.constellio.app.modules.rm.constants.RMRoles;
import com.constellio.app.modules.rm.extensions.*;
import com.constellio.app.modules.rm.extensions.api.RMModuleExtensions;
import com.constellio.app.modules.rm.extensions.app.BatchProcessingRecordFactoryExtension;
import com.constellio.app.modules.rm.extensions.app.RMBatchProcessingExtension;
import com.constellio.app.modules.rm.extensions.app.RMCmisExtension;
import com.constellio.app.modules.rm.extensions.app.RMRecordDisplayFactoryExtension;
import com.constellio.app.modules.rm.extensions.app.RMRecordExportExtension;
import com.constellio.app.modules.rm.extensions.imports.DecommissioningListImportExtension;
import com.constellio.app.modules.rm.extensions.imports.DocumentRuleImportExtension;
import com.constellio.app.modules.rm.extensions.imports.EventImportExtension;
import com.constellio.app.modules.rm.extensions.imports.FolderRuleImportExtension;
import com.constellio.app.modules.rm.extensions.imports.ReportImportExtension;
import com.constellio.app.modules.rm.extensions.imports.RetentionRuleImportExtension;
import com.constellio.app.modules.rm.extensions.schema.RMAvailableCapacityExtension;
import com.constellio.app.modules.rm.extensions.schema.RMMediumTypeRecordExtension;
import com.constellio.app.modules.rm.extensions.schema.RMTrashSchemaExtension;
import com.constellio.app.modules.rm.migrations.records.RMContainerRecordMigrationTo7_3;
import com.constellio.app.modules.rm.migrations.records.RMDocumentMigrationTo7_6_10;
import com.constellio.app.modules.rm.model.CopyRetentionRule;
import com.constellio.app.modules.rm.model.CopyRetentionRuleBuilder;
import com.constellio.app.modules.rm.navigation.RMNavigationConfiguration;
import com.constellio.app.modules.rm.services.RMSchemasRecordsServices;
import com.constellio.app.modules.rm.wrappers.AdministrativeUnit;
import com.constellio.app.modules.rm.wrappers.Category;
import com.constellio.app.modules.rm.wrappers.ContainerRecord;
import com.constellio.app.modules.rm.wrappers.Printable;
import com.constellio.app.modules.rm.wrappers.RMTaskType;
import com.constellio.app.modules.rm.wrappers.RetentionRule;
import com.constellio.app.modules.rm.wrappers.StorageSpace;
import com.constellio.app.modules.rm.wrappers.type.DocumentType;
import com.constellio.app.modules.tasks.TaskModule;
import com.constellio.app.modules.tasks.model.wrappers.types.TaskStatus;
import com.constellio.app.services.factories.AppLayerFactory;
import com.constellio.data.utils.dev.Toggle;
import com.constellio.model.entities.configs.SystemConfiguration;
import com.constellio.model.entities.records.RecordMigrationScript;
import com.constellio.model.entities.records.Transaction;
import com.constellio.model.entities.schemas.MetadataSchemaType;
import com.constellio.model.entities.schemas.MetadataSchemaTypes;
import com.constellio.model.entities.security.Role;
import com.constellio.model.extensions.ModelLayerCollectionExtensions;
import com.constellio.model.services.factories.ModelLayerFactory;
import com.constellio.model.services.records.RecordServicesException;
import com.constellio.model.services.records.cache.CacheConfig;
import com.constellio.model.services.records.cache.RecordsCache;
import com.constellio.model.services.records.cache.ignite.RecordsCacheIgniteImpl;
import com.constellio.model.services.security.GlobalSecurizedTypeCondition;

public class ConstellioRMModule implements InstallableSystemModule, ModuleWithComboMigration,
										   InstallableSystemModuleWithRecordMigrations {

	private static final Logger LOGGER = LoggerFactory.getLogger(RecordsCacheIgniteImpl.class);

	public static final String ID = "rm";
	public static final String NAME = "Constellio RM";

	public static final int DEFAULT_VOLATILE_EVENTS_CACHE_SIZE = 10000;
	public static final int DEFAULT_VOLATILE_FOLDERS_CACHE_SIZE = 100000;
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
				new RMMigrationTo6_7(),
				new RMMigrationTo7_0_5(),
				new RMMigrationTo7_0_10_5(),
				new RMMigrationTo7_1(),
				new RMMigrationTo7_1_1(),
				new RMMigrationTo7_1_2(),
				new RMMigrationTo7_2(),
				new RMMigrationTo7_2_0_1(),
				new RMMigrationTo7_2_0_2(),
				new RMMigrationTo7_2_0_3(),
				new RMMigrationTo7_2_0_4(),
				new RMMigrationTo7_3(),
				new RMMigrationTo7_3_1(),
				new RMMigrationTo7_4(),
				new RMMigrationTo7_4_2(),
				new RMMigrationTo7_4_48(),
				new RMMigrationTo7_4_48_1(),
				new RMMigrationTo7_4_49(),
				new RMMigrationTo7_5(),
				new RMMigrationTo7_5_2(),
				new RMMigrationTo7_5_3(),
				new RMMigrationTo7_5_5(),
				new RMMigrationTo7_6(),
				new RMMigrationTo7_6_2(),
				new RMMigrationTo7_6_3(),
				new RMMigrationTo7_6_6(),
				new RMMigrationTo7_6_6_1(),
				new RMMigrationTo7_6_6_2(),
				new RMMigrationTo7_6_8(),
				new RMMigrationTo7_6_9(),
				new RMMigrationTo7_6_10(),
				new RMMigrationTo7_6_11(),
				new RMMigrationTo7_7(),
				new RMMigrationTo7_7_0_42(),
				new RMMigrationTo7_7_1(),
				new RMMigrationTo7_7_2(),
				new RMMigrationTo7_7_3(),
				new RMMigrationTo7_7_4(),
				new RMMigrationTo8_1()
		);
	}

	@Override
	public List<RecordMigrationScript> getRecordMigrationScripts(String collection, AppLayerFactory appLayerFactory) {
		List<RecordMigrationScript> scripts = new ArrayList<>();

		scripts.add(new RMContainerRecordMigrationTo7_3(collection, appLayerFactory));
		scripts.add(new RMDocumentMigrationTo7_6_10(collection, appLayerFactory));
		scripts.add(new RMEmailMigrationTo7_7_1(collection, appLayerFactory));

		return scripts;
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
		extensions.schemaTypeAccessExtensions.add(new LabelSchemaRestrictionPageExtension());
		extensions.taxonomyAccessExtensions.add(new RMTaxonomyPageExtension(collection));
		extensions.pageAccessExtensions.add(new RMModulePageExtension());
		extensions.downloadContentVersionLinkExtensions.add(new RMDownloadContentVersionLinkExtension());
		extensions.cmisExtensions.add(new RMCmisExtension(collection, appLayerFactory));
		extensions.recordAppExtensions.add(new RMRecordAppExtension(collection, appLayerFactory));
		extensions.recordNavigationExtensions.add(new RMRecordNavigationExtension(appLayerFactory, collection));
		extensions.searchPageExtensions.add(new RMSearchPageExtension(collection, appLayerFactory));
		extensions.batchProcessingExtensions.add(new RMBatchProcessingExtension(collection, appLayerFactory));
		extensions.recordFieldFactoryExtensions.add(new BatchProcessingRecordFactoryExtension());
		extensions.moduleExtensionsMap.put(ID, new RMModuleExtensions(appLayerFactory));
		extensions.systemCheckExtensions.add(new RMSystemCheckExtension(collection, appLayerFactory));
		extensions.recordExportExtensions.add(new RMRecordExportExtension(collection, appLayerFactory));
		extensions.pagesComponentsExtensions.add(new RMCleanAdministrativeUnitButtonExtension(collection, appLayerFactory));
		extensions.pagesComponentsExtensions.add(new RMRequestTaskButtonExtension(collection, appLayerFactory));
		extensions.pagesComponentsExtensions.add(new RemoveClickableNotificationsWhenChangingPage());
		extensions.pagesComponentsExtensions.add(new RMListSchemaTypeExtension());
		extensions.selectionPanelExtensions.add(new RMSelectionPanelExtension(appLayerFactory, collection));
		extensions.schemaTypesPageExtensions.add(new RMSchemaTypesPageExtension());
		extensions.recordDisplayFactoryExtensions.add(new RMRecordDisplayFactoryExtension(appLayerFactory, collection));

		extensions.lockedRecords.add(RMTaskType.SCHEMA_TYPE, RMTaskType.BORROW_REQUEST);
		extensions.lockedRecords.add(RMTaskType.SCHEMA_TYPE, RMTaskType.BORROW_EXTENSION_REQUEST);
		extensions.lockedRecords.add(RMTaskType.SCHEMA_TYPE, RMTaskType.RETURN_REQUEST);
		extensions.lockedRecords.add(RMTaskType.SCHEMA_TYPE, RMTaskType.REACTIVATION_REQUEST);

		extensions.lockedRecords.add(TaskStatus.SCHEMA_TYPE, TaskStatus.CLOSED_CODE);
		extensions.lockedRecords.add(TaskStatus.SCHEMA_TYPE, TaskStatus.STANDBY_CODE);
		extensions.lockedRecords.add(DocumentType.SCHEMA_TYPE, DocumentType.EMAIL_DOCUMENT_TYPE);
	}

	private void setupModelLayerExtensions(String collection, AppLayerFactory appLayerFactory) {
		ModelLayerFactory modelLayerFactory = appLayerFactory.getModelLayerFactory();
		ModelLayerCollectionExtensions extensions = modelLayerFactory.getExtensions().forCollection(collection);

		extensions.recordExtensions.add(new RMSchemasLogicalDeleteExtension(collection, appLayerFactory));
		extensions.recordExtensions.add(new RMUserRecordExtension(collection, modelLayerFactory));
		extensions.recordExtensions.add(new RMEmailDocumentRecordExtension(collection, modelLayerFactory));
		extensions.recordExtensions.add(new RMOldSchemasBlockageRecordExtension());
		extensions.recordExtensions.add(new RMCheckInAlertsRecordExtension(collection, modelLayerFactory));
		extensions.recordExtensions.add(new RMFolderExtension(collection, modelLayerFactory));
		extensions.recordExtensions.add(new RMCreateDecommissioningListExtension(collection, modelLayerFactory));
		extensions.recordExtensions.add(new RMDocumentExtension(collection, appLayerFactory));
		extensions.recordExtensions.add(new SessionContextRecordExtension());
		extensions.recordImportExtensions.add(new RetentionRuleImportExtension(collection, modelLayerFactory));
		extensions.recordImportExtensions.add(new FolderRuleImportExtension(collection, modelLayerFactory));
		extensions.recordImportExtensions.add(new EventImportExtension(collection, modelLayerFactory));
		extensions.recordImportExtensions.add(new DocumentRuleImportExtension(collection, modelLayerFactory));
		extensions.recordImportExtensions.add(new DecommissioningListImportExtension(collection, modelLayerFactory));
		extensions.recordImportExtensions.add(new ReportImportExtension(collection, modelLayerFactory));
		extensions.schemaExtensions.add(new RMTrashSchemaExtension(collection, appLayerFactory));
		extensions.recordExtensions.add(new RMAvailableCapacityExtension(collection, appLayerFactory));
		extensions.recordExtensions.add(new RMRequestTaskApprovedExtension(collection, appLayerFactory));
		extensions.recordExtensions.add(new RMMediumTypeRecordExtension(collection, modelLayerFactory));
		extensions.recordExtensions.add(new RMEventRecordExtension(collection, modelLayerFactory));
		extensions.recordExtensions.add(new RMRecordCaptionExtension(collection, appLayerFactory));
		extensions.schemaExtensions.add(new RMExcelReportSchemaExtension());

		RMSchemasRecordsServices rm = new RMSchemasRecordsServices(collection, modelLayerFactory);
		RecordsCache cache = modelLayerFactory.getRecordsCaches().getCache(collection);

		for (MetadataSchemaType type : rm.valueListSchemaTypes()) {
			if (cache.isConfigured(type)) {
				cache.removeCache(type.getCode());
			}
			cache.configureCache(CacheConfig.permanentCache(type));
		}

		if (!cache.isConfigured(Facet.SCHEMA_TYPE)) {
			cache.configureCache(CacheConfig.permanentCache(rm.facet.schemaType()));
		}

		if (cache.isConfigured(AdministrativeUnit.SCHEMA_TYPE)) {
			cache.removeCache(AdministrativeUnit.SCHEMA_TYPE);
		}
		cache.configureCache(CacheConfig.permanentCache(rm.administrativeUnit.schemaType()));

		if (!cache.isConfigured(Printable.SCHEMA_TYPE)) {
			cache.configureCache(CacheConfig.permanentCache(rm.printable.schemaType()));
		}
		if (!cache.isConfigured(Report.SCHEMA_TYPE)) {
			cache.configureCache(CacheConfig.permanentCache(rm.report.schemaType()));
		}

		if (cache.isConfigured(Category.SCHEMA_TYPE)) {
			cache.removeCache(Category.SCHEMA_TYPE);
		}

		if (!cache.isConfigured(ThesaurusConfig.SCHEMA_TYPE)) {
			cache.configureCache(CacheConfig.permanentCache(rm.thesaurusConfig.schemaType()));
		}

		cache.configureCache(CacheConfig.permanentCache(rm.category.schemaType()));

		cache.configureCache(CacheConfig.permanentCache(rm.retentionRule.schemaType()));
		cache.configureCache(CacheConfig.permanentCache(rm.uniformSubdivision.schemaType()));
		cache.configureCache(CacheConfig.permanentCache(rm.containerRecord.schemaType()));
		cache.configureCache(CacheConfig.permanentCache(rm.decommissioningList.schemaType()));

		if (!cache.isConfigured(rm.authorizationDetails.schemaType())) {
			cache.configureCache(CacheConfig.permanentCache(rm.authorizationDetails.schemaType()));
		}

		MetadataSchemaTypes types = modelLayerFactory.getMetadataSchemasManager().getSchemaTypes(collection);
		if (Toggle.CACHES_ENABLED.isEnabled()) {
			cache.configureCache(CacheConfig.volatileCache(rm.event.schemaType(), DEFAULT_VOLATILE_EVENTS_CACHE_SIZE));
			cache.configureCache(CacheConfig.volatileCache(rm.folder.schemaType(), DEFAULT_VOLATILE_FOLDERS_CACHE_SIZE));
			cache.configureCache(CacheConfig.volatileCache(rm.documentSchemaType(), DEFAULT_VOLATILE_DOCUMENTS_CACHE_SIZE));

			if (!cache.isConfigured(SavedSearch.SCHEMA_TYPE)) {
				cache.configureCache(CacheConfig.volatileCache(types.getSchemaType(SavedSearch.SCHEMA_TYPE), 1000, FIFO));
			}
		}
	}

	@Override
	public void start(AppLayerFactory appLayerFactory) {
		RMNavigationConfiguration.configureNavigation(appLayerFactory.getNavigatorConfigurationService());
		appLayerFactory.getModelLayerFactory().getSecurityTokenManager().registerPublicTypeWithCondition(
				ContainerRecord.SCHEMA_TYPE, new GlobalSecurizedTypeCondition() {
					@Override
					public boolean hasGlobalAccess(User user, String access) {
						if (Role.READ.equals(access)) {
							return user.hasAny(RMPermissionsTo.DISPLAY_CONTAINERS, RMPermissionsTo.MANAGE_CONTAINERS)
									.onSomething();
						} else if (Role.WRITE.equals(access)) {
							return user.has(RMPermissionsTo.MANAGE_CONTAINERS).onSomething();
						} else if (Role.DELETE.equals(access)) {
							return user.has(RMPermissionsTo.DELETE_CONTAINERS).onSomething();
						}
						return false;
					}
				});

		appLayerFactory.getModelLayerFactory().getSecurityTokenManager().registerPublicTypeWithCondition(
				StorageSpace.SCHEMA_TYPE, new GlobalSecurizedTypeCondition() {
					@Override
					public boolean hasGlobalAccess(User user, String access) {
						if (Role.READ.equals(access) || Role.WRITE.equals(access) || Role.DELETE.equals(access)) {
							return user.hasAny(RMPermissionsTo.MANAGE_STORAGE_SPACES).globally();
						}
						return false;
					}
				});
	}

	@Override
	public void stop(AppLayerFactory appLayerFactory) {

	}
}
