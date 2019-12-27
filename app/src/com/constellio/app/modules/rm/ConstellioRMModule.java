package com.constellio.app.modules.rm;

import com.constellio.app.entities.modules.ComboMigrationScript;
import com.constellio.app.entities.modules.InstallableSystemModule;
import com.constellio.app.entities.modules.InstallableSystemModuleWithRecordMigrations;
import com.constellio.app.entities.modules.MigrationScript;
import com.constellio.app.entities.modules.ModuleWithComboMigration;
import com.constellio.app.entities.navigation.NavigationConfig;
import com.constellio.app.extensions.AppLayerCollectionExtensions;
import com.constellio.app.extensions.AppLayerSystemExtensions;
import com.constellio.app.modules.rm.constants.RMPermissionsTo;
import com.constellio.app.modules.rm.constants.RMRoles;
import com.constellio.app.modules.rm.extensions.LabelSchemaRestrictionPageExtension;
import com.constellio.app.modules.rm.extensions.RMCheckInAlertsRecordExtension;
import com.constellio.app.modules.rm.extensions.RMCleanAdministrativeUnitButtonExtension;
import com.constellio.app.modules.rm.extensions.RMContainerRecordExtension;
import com.constellio.app.modules.rm.extensions.RMCreateDecommissioningListExtension;
import com.constellio.app.modules.rm.extensions.RMDocumentExtension;
import com.constellio.app.modules.rm.extensions.RMDownloadContentVersionLinkExtension;
import com.constellio.app.modules.rm.extensions.RMEmailDocumentRecordExtension;
import com.constellio.app.modules.rm.extensions.RMEventRecordExtension;
import com.constellio.app.modules.rm.extensions.RMFolderExtension;
import com.constellio.app.modules.rm.extensions.RMGenericRecordPageExtension;
import com.constellio.app.modules.rm.extensions.RMListSchemaTypeExtension;
import com.constellio.app.modules.rm.extensions.RMManageAuthorizationsPageExtension;
import com.constellio.app.modules.rm.extensions.RMMediumTypeRecordExtension;
import com.constellio.app.modules.rm.extensions.RMMenuItemActionsExtension;
import com.constellio.app.modules.rm.extensions.RMMetadataMainCopyRuleFieldsExtension;
import com.constellio.app.modules.rm.extensions.RMModulePageExtension;
import com.constellio.app.modules.rm.extensions.RMOldSchemasBlockageRecordExtension;
import com.constellio.app.modules.rm.extensions.RMRecordAppExtension;
import com.constellio.app.modules.rm.extensions.RMRecordCaptionExtension;
import com.constellio.app.modules.rm.extensions.RMRecordNavigationExtension;
import com.constellio.app.modules.rm.extensions.RMRequestTaskApprovedExtension;
import com.constellio.app.modules.rm.extensions.RMRequestTaskButtonExtension;
import com.constellio.app.modules.rm.extensions.RMSchemaTypesPageExtension;
import com.constellio.app.modules.rm.extensions.RMSchemasLogicalDeleteExtension;
import com.constellio.app.modules.rm.extensions.RMSearchPageExtension;
import com.constellio.app.modules.rm.extensions.RMSelectionPanelExtension;
import com.constellio.app.modules.rm.extensions.RMSystemCheckExtension;
import com.constellio.app.modules.rm.extensions.RMTaskRecordExtension;
import com.constellio.app.modules.rm.extensions.RMTaxonomyPageExtension;
import com.constellio.app.modules.rm.extensions.RMUserHavePermissionOnRecordExtension;
import com.constellio.app.modules.rm.extensions.RMUserProfileFieldsExtension;
import com.constellio.app.modules.rm.extensions.RMUserRecordExtension;
import com.constellio.app.modules.rm.extensions.RemoveClickableNotificationsWhenChangingPage;
import com.constellio.app.modules.rm.extensions.SessionContextRecordExtension;
import com.constellio.app.modules.rm.extensions.api.RMModuleExtensions;
import com.constellio.app.modules.rm.extensions.app.BatchProcessingRecordFactoryExtension;
import com.constellio.app.modules.rm.extensions.app.RMAdministrativeUnitRecordFieldFactoryExtension;
import com.constellio.app.modules.rm.extensions.app.RMAdvancedSearchMenuItemActionsExtension;
import com.constellio.app.modules.rm.extensions.app.RMBatchProcessingExtension;
import com.constellio.app.modules.rm.extensions.app.RMBatchProcessingSpecialCaseExtension;
import com.constellio.app.modules.rm.extensions.app.RMCmisExtension;
import com.constellio.app.modules.rm.extensions.app.RMDecommissioningBuilderMenuItemActionsExtension;
import com.constellio.app.modules.rm.extensions.app.RMListSchemaExtention;
import com.constellio.app.modules.rm.extensions.app.RMMenuItemActionsRequestTaskExtension;
import com.constellio.app.modules.rm.extensions.app.RMRecordDisplayFactoryExtension;
import com.constellio.app.modules.rm.extensions.app.RMRecordExportExtension;
import com.constellio.app.modules.rm.extensions.app.RMSIPExtension;
import com.constellio.app.modules.rm.extensions.app.RMXmlGeneratorExtension;
import com.constellio.app.modules.rm.extensions.imports.DecommissioningListImportExtension;
import com.constellio.app.modules.rm.extensions.imports.DocumentRuleImportExtension;
import com.constellio.app.modules.rm.extensions.imports.EventImportExtension;
import com.constellio.app.modules.rm.extensions.imports.FolderRuleImportExtension;
import com.constellio.app.modules.rm.extensions.imports.ReportImportExtension;
import com.constellio.app.modules.rm.extensions.imports.RetentionRuleImportExtension;
import com.constellio.app.modules.rm.extensions.schema.RMAvailableCapacityExtension;
import com.constellio.app.modules.rm.extensions.schema.RMExcelReportSchemaExtension;
import com.constellio.app.modules.rm.extensions.schema.RMTrashSchemaExtension;
import com.constellio.app.modules.rm.extensions.ui.RMConstellioUIExtention;
import com.constellio.app.modules.rm.migrations.*;
import com.constellio.app.modules.rm.migrations.records.RMContainerRecordMigrationTo7_3;
import com.constellio.app.modules.rm.migrations.records.RMDocumentMigrationTo7_6_10;
import com.constellio.app.modules.rm.migrations.records.RMDocumentMigrationTo8_1_0_43;
import com.constellio.app.modules.rm.migrations.records.RMEmailMigrationTo7_7_1;
import com.constellio.app.modules.rm.migrations.records.RMFolderMigrationTo8_1_1_2;
import com.constellio.app.modules.rm.model.CopyRetentionRule;
import com.constellio.app.modules.rm.model.CopyRetentionRuleBuilder;
import com.constellio.app.modules.rm.navigation.RMNavigationConfiguration;
import com.constellio.app.modules.rm.services.RMSchemasRecordsServices;
import com.constellio.app.modules.rm.wrappers.AdministrativeUnit;
import com.constellio.app.modules.rm.wrappers.Category;
import com.constellio.app.modules.rm.wrappers.ContainerRecord;
import com.constellio.app.modules.rm.wrappers.RMTaskType;
import com.constellio.app.modules.rm.wrappers.RetentionRule;
import com.constellio.app.modules.rm.wrappers.StorageSpace;
import com.constellio.app.modules.rm.wrappers.type.DocumentType;
import com.constellio.app.modules.tasks.TaskModule;
import com.constellio.app.modules.tasks.model.wrappers.types.TaskStatus;
import com.constellio.app.services.factories.AppLayerFactory;
import com.constellio.model.entities.configs.SystemConfiguration;
import com.constellio.model.entities.records.RecordMigrationScript;
import com.constellio.model.entities.records.Transaction;
import com.constellio.model.entities.records.wrappers.User;
import com.constellio.model.entities.security.Role;
import com.constellio.model.extensions.ModelLayerCollectionExtensions;
import com.constellio.model.services.factories.ModelLayerFactory;
import com.constellio.model.services.records.RecordServicesException;
import com.constellio.model.services.records.cache.RecordsCache;
import com.constellio.model.services.security.GlobalSecuredTypeCondition;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import static com.constellio.app.ui.i18n.i18n.$;
import static java.util.Arrays.asList;

public class ConstellioRMModule implements InstallableSystemModule, ModuleWithComboMigration,
		InstallableSystemModuleWithRecordMigrations {

	private static final Logger LOGGER = LoggerFactory.getLogger(ConstellioRMModule.class);

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

		List<MigrationScript> scripts = new ArrayList<>();
		scripts.add(new RMMigrationTo5_0_1());
		scripts.add(new RMMigrationTo5_0_2());
		scripts.add(new RMMigrationTo5_0_3());
		scripts.add(new RMMigrationTo5_0_4());
		scripts.add(new RMMigrationTo5_0_4_1());
		scripts.add(new RMMigrationTo5_0_5());
		scripts.add(new RMMigrationTo5_0_6());
		scripts.add(new RMMigrationTo5_0_7());
		scripts.add(new RMMigrationTo5_1_0_3());
		scripts.add(new RMMigrationTo5_1_0_4());
		scripts.add(new RMMigrationTo5_1_0_6());
		scripts.add(new RMMigrationTo5_1_2());
		scripts.add(new RMMigrationTo5_1_2_2());
		scripts.add(new RMMigrationTo5_1_3());
		scripts.add(new RMMigrationTo5_1_3());
		scripts.add(new RMMigrationTo5_1_4_1());
		scripts.add(new RMMigrationTo5_1_5());
		scripts.add(new RMMigrationTo5_1_7());
		scripts.add(new RMMigrationTo5_1_9());
		scripts.add(new RMMigrationTo6_1());
		scripts.add(new RMMigrationTo6_1_4());
		scripts.add(new RMMigrationTo6_2());
		scripts.add(new RMMigrationTo6_2_0_7());
		scripts.add(new RMMigrationTo6_3());
		scripts.add(new RMMigrationTo6_4());
		scripts.add(new RMMigrationTo6_5());
		scripts.add(new RMMigrationTo6_5_1());
		scripts.add(new RMMigrationTo6_5_7());
		scripts.add(new RMMigrationTo6_5_20());
		scripts.add(new RMMigrationTo6_5_21());
		scripts.add(new RMMigrationTo6_5_33());
		scripts.add(new RMMigrationTo6_5_34());
		scripts.add(new RMMigrationTo6_5_36());
		scripts.add(new RMMigrationTo6_5_37());
		scripts.add(new RMMigrationTo6_5_50());
		scripts.add(new RMMigrationTo6_5_54());
		scripts.add(new RMMigrationTo6_6());
		scripts.add(new RMMigrationTo6_7());
		scripts.add(new RMMigrationTo7_0_5());
		scripts.add(new RMMigrationTo7_0_10_5());
		scripts.add(new RMMigrationTo7_1());
		scripts.add(new RMMigrationTo7_1_1());
		scripts.add(new RMMigrationTo7_1_2());
		scripts.add(new RMMigrationTo7_2());
		scripts.add(new RMMigrationTo7_2_0_1());
		scripts.add(new RMMigrationTo7_2_0_2());
		scripts.add(new RMMigrationTo7_2_0_3());
		scripts.add(new RMMigrationTo7_2_0_4());
		scripts.add(new RMMigrationTo7_3());
		scripts.add(new RMMigrationTo7_3_1());
		scripts.add(new RMMigrationTo7_4());
		scripts.add(new RMMigrationTo7_4_2());
		scripts.add(new RMMigrationTo7_4_48());
		scripts.add(new RMMigrationTo7_4_48_1());
		scripts.add(new RMMigrationTo7_4_49());
		scripts.add(new RMMigrationTo7_5());
		scripts.add(new RMMigrationTo7_5_2());
		scripts.add(new RMMigrationTo7_5_3());
		scripts.add(new RMMigrationTo7_5_5());
		scripts.add(new RMMigrationTo7_6());
		scripts.add(new RMMigrationTo7_6_2());
		scripts.add(new RMMigrationTo7_6_3());
		scripts.add(new RMMigrationTo7_6_6());
		scripts.add(new RMMigrationTo7_6_6_1());
		scripts.add(new RMMigrationTo7_6_6_2());
		scripts.add(new RMMigrationTo7_6_8());
		scripts.add(new RMMigrationTo7_6_9());
		scripts.add(new RMMigrationTo7_6_10());
		scripts.add(new RMMigrationTo7_6_11());
		scripts.add(new RMMigrationTo7_7());
		scripts.add(new RMMigrationTo7_7_0_42());
		scripts.add(new RMMigrationTo7_7_1());
		scripts.add(new RMMigrationTo7_7_2());
		scripts.add(new RMMigrationTo7_7_3());
		scripts.add(new RMMigrationTo7_7_4());
		scripts.add(new RMMigrationTo7_7_4_33());
		scripts.add(new RMMigrationTo7_7_5_4());
		scripts.add(new RMMigrationTo7_7_5_5());
		scripts.add(new RMMigrationTo8_0_1());
		scripts.add(new RMMigrationTo8_0_2());
		scripts.add(new RMMigrationTo8_0_3());
		scripts.add(new RMMigrationTo8_1());
		scripts.add(new RMMigrationTo8_1_0_1());
		scripts.add(new RMMigrationTo8_1_1());
		scripts.add(new RMMigrationTo8_1_1_1());
		scripts.add(new RMMigrationTo8_1_1_2());
		scripts.add(new RMMigrationTo8_1_1_6());
		scripts.add(new RMMigrationTo8_1_2());
		scripts.add(new RMMigrationTo8_1_4());
		scripts.add(new RMMigrationTo8_2());
		scripts.add(new RMMigrationTo8_2_42());
		scripts.add(new RMMigrationTo8_2_1_4());
		scripts.add(new RMMigrationTo8_2_1_5());
		scripts.add(new RMMigrationTo8_2_2_4());
		scripts.add(new RMMigrationTo8_2_2_5());
		scripts.add(new RMMigrationTo8_2_3());
		scripts.add(new RMMigrationTo8_3());
		scripts.add(new RMMigrationTo8_3_1());
		scripts.add(new RMMigrationTo8_3_1_1());
		scripts.add(new RMMigrationTo8_3_2());
		scripts.add(new RMMigrationTo8_3_2_1());
		scripts.add(new RMMigrationTo8_3_2_2());
		scripts.add(new RMMigrationTo9_0());
		scripts.add(new RMMigrationTo9_0_0_1());
		scripts.add(new RMMigrationTo8_2_1_5());
		scripts.add(new RMMigrationTo9_0_0_4());
		scripts.add(new RMMigrationTo9_0_0_33());
		scripts.add(new RMMigrationTo9_0_0_42());
		scripts.add(new RMMigrationTo9_0_46());

		//scripts.add(new RMMigrationTo9_0_666());

		return scripts;
	}

	@Override
	public List<RecordMigrationScript> getRecordMigrationScripts(String collection, AppLayerFactory appLayerFactory) {
		List<RecordMigrationScript> scripts = new ArrayList<>();

		scripts.add(new RMContainerRecordMigrationTo7_3(collection, appLayerFactory));
		scripts.add(new RMDocumentMigrationTo7_6_10(collection, appLayerFactory));
		scripts.add(new RMEmailMigrationTo7_7_1(collection, appLayerFactory));
		scripts.add(new RMFolderMigrationTo8_1_1_2(collection, appLayerFactory));
		scripts.add(new RMDocumentMigrationTo8_1_0_43(collection, appLayerFactory));

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
	public void start(final String collection, final AppLayerFactory appLayerFactory) {
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

	private void setupAppLayerSystemExtensions(AppLayerFactory appLayerFactory) {
		AppLayerSystemExtensions extensions = appLayerFactory.getExtensions().getSystemWideExtensions();
		extensions.constellioUIExtentions.add(new RMConstellioUIExtention(appLayerFactory));
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
		extensions.userHavePermissionOnRecordExtensions.add(new RMUserHavePermissionOnRecordExtension(collection, appLayerFactory));
		extensions.recordNavigationExtensions.add(new RMRecordNavigationExtension(appLayerFactory, collection));
		extensions.searchPageExtensions.add(new RMSearchPageExtension(collection, appLayerFactory));
		extensions.batchProcessingExtensions.add(new RMBatchProcessingExtension(collection, appLayerFactory));
		extensions.recordFieldFactoryExtensions.add(new BatchProcessingRecordFactoryExtension());
		extensions.registerModuleExtensionsPoint(ID, new RMModuleExtensions(appLayerFactory));
		extensions.systemCheckExtensions.add(new RMSystemCheckExtension(collection, appLayerFactory));
		extensions.recordExportExtensions.add(new RMRecordExportExtension(collection, appLayerFactory));
		extensions.pagesComponentsExtensions.add(new RMCleanAdministrativeUnitButtonExtension(collection, appLayerFactory));
		extensions.pagesComponentsExtensions.add(new RMRequestTaskButtonExtension(collection, appLayerFactory));
		extensions.pagesComponentsExtensions.add(new RemoveClickableNotificationsWhenChangingPage());
		extensions.pagesComponentsExtensions.add(new RMListSchemaTypeExtension());
		extensions.selectionPanelExtensions.add(new RMSelectionPanelExtension(appLayerFactory, collection));
		extensions.schemaTypesPageExtensions.add(new RMSchemaTypesPageExtension());
		extensions.recordDisplayFactoryExtensions.add(new RMRecordDisplayFactoryExtension(appLayerFactory, collection));
		extensions.listSchemaCommandExtensions.add(new RMListSchemaExtention());
		extensions.pagesComponentsExtensions.add(new RMUserProfileFieldsExtension(collection, appLayerFactory));
		extensions.pagesComponentsExtensions.add(new RMMetadataMainCopyRuleFieldsExtension(collection, appLayerFactory));
		extensions.sipExtensions.add(new RMSIPExtension(collection, appLayerFactory));
		extensions.recordFieldFactoryExtensions.add(new RMAdministrativeUnitRecordFieldFactoryExtension());
		extensions.xmlGeneratorExtensions.add(new RMXmlGeneratorExtension(collection, appLayerFactory));
		extensions.pagesComponentsExtensions.add(new RMManageAuthorizationsPageExtension(collection, appLayerFactory));
		extensions.sipExtensions.add(new RMSIPExtension(collection, appLayerFactory));

		extensions.lockedRecords.add(RMTaskType.SCHEMA_TYPE, RMTaskType.BORROW_REQUEST);
		extensions.lockedRecords.add(RMTaskType.SCHEMA_TYPE, RMTaskType.BORROW_EXTENSION_REQUEST);
		extensions.lockedRecords.add(RMTaskType.SCHEMA_TYPE, RMTaskType.RETURN_REQUEST);
		extensions.lockedRecords.add(RMTaskType.SCHEMA_TYPE, RMTaskType.REACTIVATION_REQUEST);

		extensions.lockedRecords.add(TaskStatus.SCHEMA_TYPE, TaskStatus.CLOSED_CODE);
		extensions.lockedRecords.add(TaskStatus.SCHEMA_TYPE, TaskStatus.STANDBY_CODE);
		extensions.lockedRecords.add(DocumentType.SCHEMA_TYPE, DocumentType.EMAIL_DOCUMENT_TYPE);

		extensions.menuItemActionsExtensions.add(new RMMenuItemActionsExtension(collection, appLayerFactory));
		extensions.menuItemActionsExtensions.add(new RMAdvancedSearchMenuItemActionsExtension(collection, appLayerFactory));
		extensions.menuItemActionsExtensions.add(new RMDecommissioningBuilderMenuItemActionsExtension(collection, appLayerFactory));
		// FIXME should be merged with RMMenuItemActionsExtensions
		extensions.menuItemActionsExtensions.add(new RMMenuItemActionsRequestTaskExtension(collection, appLayerFactory));
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
		extensions.recordExtensions.add(new RMMediumTypeRecordExtension(collection, appLayerFactory));
		extensions.recordExtensions.add(new RMEventRecordExtension(collection, modelLayerFactory));
		extensions.recordExtensions.add(new RMRecordCaptionExtension(collection, appLayerFactory));
		extensions.recordExtensions.add(new RMContainerRecordExtension(collection, appLayerFactory));
		extensions.schemaExtensions.add(new RMExcelReportSchemaExtension());
		extensions.recordExtensions.add(new RMTaskRecordExtension(collection, appLayerFactory));
		extensions.batchProcessingSpecialCaseExtensions
				.add(new RMBatchProcessingSpecialCaseExtension(collection, appLayerFactory));

		RMSchemasRecordsServices rm = new RMSchemasRecordsServices(collection, modelLayerFactory);
		RecordsCache cache = modelLayerFactory.getRecordsCaches().getCache(collection);

	}

	@Override
	public void start(AppLayerFactory appLayerFactory) {
		RMNavigationConfiguration.configureNavigation(appLayerFactory.getNavigatorConfigurationService());
		appLayerFactory.getModelLayerFactory().getSecurityTokenManager().registerPublicTypeWithCondition(
				ContainerRecord.SCHEMA_TYPE, new GlobalSecuredTypeCondition() {
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
				StorageSpace.SCHEMA_TYPE, new GlobalSecuredTypeCondition() {
					@Override
					public boolean hasGlobalAccess(User user, String access) {
						if (Role.READ.equals(access) || Role.WRITE.equals(access) || Role.DELETE.equals(access)) {
							return user.hasAny(RMPermissionsTo.MANAGE_STORAGE_SPACES).globally();
						}
						return false;
					}
				});

		setupAppLayerSystemExtensions(appLayerFactory);
	}

	@Override
	public void stop(AppLayerFactory appLayerFactory) {

	}
}
