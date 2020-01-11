package com.constellio.app.modules.rm;

import com.constellio.app.modules.rm.configScripts.EnableOrDisableCalculatorsManualMetadataScript;
import com.constellio.app.modules.rm.configScripts.EnableOrDisableContainerMultiValueMetadataScript;
import com.constellio.app.modules.rm.configScripts.EnableOrDisableStorageSpaceTitleCalculatorScript;
import com.constellio.app.modules.rm.configScripts.EnableOrDisableTypeRestrictionInFolderScript;
import com.constellio.app.modules.rm.model.enums.AllowModificationOfArchivisticStatusAndExpectedDatesChoice;
import com.constellio.app.modules.rm.model.enums.CompleteDatesWhenAddingFolderWithManualStatusChoice;
import com.constellio.app.modules.rm.model.enums.DecommissioningDateBasedOn;
import com.constellio.app.modules.rm.model.enums.DefaultTabInFolderDisplay;
import com.constellio.app.modules.rm.model.enums.DocumentsTypeChoice;
import com.constellio.app.modules.rm.validator.EndYearValueCalculator;
import com.constellio.app.modules.rm.wrappers.RMDecommissioningTypeRequiredScript;
import com.constellio.app.services.factories.AppLayerFactory;
import com.constellio.model.entities.configs.SystemConfiguration;
import com.constellio.model.entities.configs.SystemConfigurationGroup;
import com.constellio.model.services.configs.SystemConfigurationsManager;
import com.constellio.model.services.factories.ModelLayerFactory;

import java.util.ArrayList;
import java.util.List;

import static com.constellio.app.modules.rm.ConstellioRMModule.ID;

public class RMConfigs {

	public static String decommissioningGroup = "decommissioning";

	public enum DecommissioningPhase {
		NEVER, ON_DEPOSIT, ON_TRANSFER_OR_DEPOSIT
	}

	static List<SystemConfiguration> configurations = new ArrayList<>();

	// Advanced
	public static final SystemConfiguration ALLOW_TO_EDIT_OLD_DOCUMENT_VERSION_ANNOTATION;

	// Retention calendar configs
	public static final SystemConfiguration DOCUMENT_RETENTION_RULES,
			CALCULATED_CLOSING_DATE,
			DECOMMISSIONING_DATE_BASED_ON,
			CALCULATED_CLOSING_DATE_NUMBER_OF_YEAR_WHEN_FIXED_RULE,
			CALCULATED_CLOSING_DATE_NUMBER_OF_YEAR_WHEN_VARIABLE_RULE,
			YEAR_END_DATE,
			REQUIRED_DAYS_BEFORE_YEAR_END_FOR_NOT_ADDING_A_YEAR,
			ADD_YEAR_IF_CALULATION_DATE_IS_END_IF_YEAR,
			CALCULATED_SEMIACTIVE_DATE_NUMBER_OF_YEAR_WHEN_VARIABLE_PERIOD,
			CALCULATED_INACTIVE_DATE_NUMBER_OF_YEAR_WHEN_VARIABLE_PERIOD,
			COPY_RULE_TYPE_ALWAYS_MODIFIABLE,
			COPY_RULE_PRINCIPAL_REQUIRED,
			MINOR_VERSIONS_PURGED_ON,
			ALSO_PURGE_CURRENT_VERSION_IF_MINOR,
			PDFA_CREATED_ON,
			DELETE_FOLDER_RECORDS_WITH_DESTRUCTION,
			DELETE_DOCUMENT_RECORDS_WITH_DESTRUCTION,
			REQUIRE_APPROVAL_FOR_CLOSING,
			REQUIRE_APPROVAL_FOR_TRANSFER,
			REQUIRE_APPROVAL_FOR_DEPOSIT_OF_ACTIVE,
			REQUIRE_APPROVAL_FOR_DEPOSIT_OF_SEMIACTIVE,
			REQUIRE_APPROVAL_FOR_DESTRUCTION_OF_ACTIVE,
			REQUIRE_APPROVAL_FOR_DESTRUCTION_OF_SEMIACTIVE,
			CONTAINER_RECYCLING_ALLOWED,
			MIXED_CONTAINERS_ALLOWED,
			ACTIVES_IN_CONTAINER_ALLOWED,
			BORROWING_DURATION_IN_DAYS,
			DOCUMENTS_TYPES_CHOICE,
			ENFORCE_CATEGORY_AND_RULE_RELATIONSHIP_IN_FOLDER,
			ALLOW_MODIFICATION_OF_ARCHIVISTIC_STATUS_AND_EXPECTED_DATES,
			CALCULATED_METADATAS_BASED_ON_FIRST_TIMERANGE_PART,
			DEFAULT_TAB_IN_FOLDER_DISPLAY,
			UNIFORM_SUBDIVISION_ENABLED,
			STORAGE_SPACE_TITLE_CALCULATOR_ENABLED,
			COMPLETE_DECOMMISSIONNING_DATE_WHEN_CREATING_FOLDER_WITH_MANUAL_STATUS,
			POPULATE_BORDEREAUX_WITH_COLLECTION,
			POPULATE_BORDEREAUX_WITH_LESSER_DISPOSITION_DATE,
			IS_CONTAINER_MULTIVALUE,
			FOLDER_ADMINISTRATIVE_UNIT_ENTERED_AUTOMATICALLY,
			CHECK_OUT_DOCUMENT_AFTER_CREATION,
			LOG_FOLDER_DOCUMENT_ACCESS_WITH_CMIS,
			ALLOW_TRANSFER_DATE_FIELD_WHEN_COPY_RULE_HAS_NO_SEMIACTIVE_STATE,
			COPY_RULES_ALWAYS_VISIBLE_IN_ADD_FORM,
			NEED_REASON_BEFORE_DELETING_FOLDERS,
			IS_DECOMMISSIONING_TYPE_REQUIRED_IN_CONTAINERS,
			DEPOSIT_AND_DESTRUCTION_DATES_BASED_ON_ACTUAL_TRANSFER_DATE,
			DECOMMISSIONING_LIST_WITH_SELECTED_FOLDERS,
			NUMBER_OF_DAYS_BEFORE_PREDICTED_DECOMMISSIONING_DATE,
			ALLOW_SORTING_IN_FOLDER_LIST_OF_DECOMMISSIONING,
			CREATE_MISSING_AUTHORIZATIONS_FOR_TASK,
			SUB_FOLDER_DECOMMISSIONING,
			DOCUMENT_SUMMARY_CACHE_ENABLED,
			IGNORE_VALIDATIONS_IN_BATCH_PROCESSING,
			ENABLE_TYPE_RESTRICTION_IN_FOLDER;

	// Category configs
	public static final SystemConfiguration LINKABLE_CATEGORY_MUST_NOT_BE_ROOT, LINKABLE_CATEGORY_MUST_HAVE_APPROVED_RULES;

	// Tree configs
	public static final SystemConfiguration DISPLAY_SEMI_ACTIVE_RECORDS_IN_TREES, DISPLAY_DEPOSITED_RECORDS_IN_TREES,
			DISPLAY_DESTROYED_RECORDS_IN_TREES, DISPLAY_CONTAINERS_IN_TREES;

	// Agent configs
	public static final SystemConfiguration AGENT_ENABLED, AGENT_SWITCH_USER_POSSIBLE, AGENT_DOWNLOAD_ALL_USER_CONTENT,
			AGENT_EDIT_USER_DOCUMENTS, AGENT_BACKUP_RETENTION_PERIOD_IN_DAYS, AGENT_TOKEN_DURATION_IN_HOURS, AGENT_READ_ONLY_WARNING, AGENT_DISABLED_UNTIL_FIRST_CONNECTION, AGENT_MOVE_IMPORTED_FILES_TO_TRASH, AGENT_CREATE_DROP_DIR_SHORTCUT;

	// other
	public static final SystemConfiguration OPEN_HOLDER, MAJOR_VERSION_FOR_NEW_FILE;

	static {
		//SystemConfigurationGroup beta = new SystemConfigurationGroup(ID, "beta");

		SystemConfigurationGroup advanced = new SystemConfigurationGroup(null, "advanced");
		add(ALLOW_TO_EDIT_OLD_DOCUMENT_VERSION_ANNOTATION = advanced.createBooleanFalseByDefault("allowToEditOldVersionAnnotation"));

		SystemConfigurationGroup decommissioning = new SystemConfigurationGroup(ID, decommissioningGroup);

		add(SUB_FOLDER_DECOMMISSIONING = decommissioning.createBooleanTrueByDefault("subfolderSeparateDecommissioning")
				.withReIndexationRequired());

		// Allow to enter retention rules for documents
		add(DOCUMENT_RETENTION_RULES = decommissioning.createBooleanFalseByDefault("documentRetentionRules")
				.withReIndexationRequired().scriptedBy(RMDocumentRetentionRulesScript.class).whichIsHidden());

		// Validation exception if a folder's rule and category are not linked
		add(ENFORCE_CATEGORY_AND_RULE_RELATIONSHIP_IN_FOLDER = decommissioning
				.createBooleanTrueByDefault("enforceCategoryAndRuleRelationshipInFolder"));

		// Is the closing date calculated or manual?
		add(CALCULATED_CLOSING_DATE = decommissioning.createBooleanTrueByDefault("calculatedCloseDate").withReIndexationRequired());

		// Years before closing for a fixed delay (if -1, then the same as the active delay)
		add(CALCULATED_CLOSING_DATE_NUMBER_OF_YEAR_WHEN_FIXED_RULE = decommissioning
				.createInteger("calculatedCloseDateNumberOfYearWhenFixedRule")
				.withDefaultValue(-1));

		// Years before closing for an open delay (if -1, then not automatically calculated)
		add(CALCULATED_CLOSING_DATE_NUMBER_OF_YEAR_WHEN_VARIABLE_RULE = decommissioning
				.createInteger("calculatedCloseDateNumberOfYearWhenVariableRule")
				.withDefaultValue(1));

		// Years before transfert to semi-active for an open delay (if -1, then not automatically calculated)
		add(CALCULATED_SEMIACTIVE_DATE_NUMBER_OF_YEAR_WHEN_VARIABLE_PERIOD = decommissioning
				.createInteger("calculatedSemiActiveDateNumberOfYearWhenOpenRule")
				.withDefaultValue(1));

		// Years before final disposition for a semi-active open delay (if -1, then not automatically calculated)
		add(CALCULATED_INACTIVE_DATE_NUMBER_OF_YEAR_WHEN_VARIABLE_PERIOD = decommissioning
				.createInteger("calculatedInactiveDateNumberOfYearWhenOpenRule")
				.withDefaultValue(1).withReIndexationRequired());

		// Delays are computed from the opening date (if true), or the closing date (if false)
		add(DECOMMISSIONING_DATE_BASED_ON = decommissioning
				.createEnum("decommissioningDateBasedOn", DecommissioningDateBasedOn.class)
				.withDefaultValue(DecommissioningDateBasedOn.CLOSE_DATE).withReIndexationRequired());

		// End of the civil year for the purposes of calculating the delays (MM/DD)
		add(YEAR_END_DATE = decommissioning.createString("yearEndDate").withDefaultValue("12/31")
				.scriptedBy(EndYearValueCalculator.class));

		//Nombre de jours devant précéder la date de fin d'année pour que celle-ci soit considérée dans le calcul des délais pour l'année en cours
		add(REQUIRED_DAYS_BEFORE_YEAR_END_FOR_NOT_ADDING_A_YEAR = decommissioning
				.createInteger("closeDateRequiredDaysBeforeYearEnd")
				.withDefaultValue(90));

		add(ADD_YEAR_IF_CALULATION_DATE_IS_END_IF_YEAR = decommissioning
				.createBooleanTrueByDefault("addYearIfCalculationDateIsEndOfYear"));

		// Delete (if true) or keep (if false) folder records upon destruction via decommissioning
		add(DELETE_FOLDER_RECORDS_WITH_DESTRUCTION = decommissioning
				.createBooleanFalseByDefault("deleteFolderRecordsWithDestruction"));

		// Delete (if true) or keep (if false) document records upon destruction via decommissioning
		add(DELETE_DOCUMENT_RECORDS_WITH_DESTRUCTION = decommissioning
				.createBooleanFalseByDefault("deleteDocumentRecordsWithDestruction"));

		add(COPY_RULE_TYPE_ALWAYS_MODIFIABLE = decommissioning.createBooleanFalseByDefault("copyRuleTypeAlwaysModifiable"));

		// Principal copy retention rule required
		add(COPY_RULE_PRINCIPAL_REQUIRED = decommissioning.createBooleanTrueByDefault("copyRulePrincipalRequired"));

		// When to purge minor versions of documents upon decommissioning
		add(MINOR_VERSIONS_PURGED_ON = decommissioning
				.createEnum("minorVersionsPurgedOn", DecommissioningPhase.class)
				.withDefaultValue(DecommissioningPhase.NEVER));

		// Purge the current version if minor when purging minor versions (Only applies when MINOR_VERSIONS_PURGED_ON != NEVER)
		add(ALSO_PURGE_CURRENT_VERSION_IF_MINOR = decommissioning.createBooleanFalseByDefault("alsoPurgeCurrentVersionIfMinor"));

		// When to create PDF/A versions of digital documents upon decommissioning
		add(PDFA_CREATED_ON = decommissioning
				.createEnum("PDFACreatedOn", DecommissioningPhase.class)
				.withDefaultValue(DecommissioningPhase.NEVER));

		add(REQUIRE_APPROVAL_FOR_CLOSING = decommissioning.createBooleanTrueByDefault("requireApprovalForClosing"));

		add(REQUIRE_APPROVAL_FOR_TRANSFER = decommissioning.createBooleanTrueByDefault("requireApprovalForTransfer"));

		add(REQUIRE_APPROVAL_FOR_DEPOSIT_OF_ACTIVE = decommissioning
				.createBooleanTrueByDefault("requireApprovalForDepositOfActive"));

		add(REQUIRE_APPROVAL_FOR_DEPOSIT_OF_SEMIACTIVE = decommissioning
				.createBooleanTrueByDefault("requireApprovalForDepositOfSemiActive"));

		add(REQUIRE_APPROVAL_FOR_DESTRUCTION_OF_ACTIVE = decommissioning
				.createBooleanTrueByDefault("requireApprovalForDestructionOfActive"));

		add(REQUIRE_APPROVAL_FOR_DESTRUCTION_OF_SEMIACTIVE = decommissioning
				.createBooleanTrueByDefault("requireApprovalForDestructionOfSemiActive"));

		// Allow to pick non-leaf categories
		add(LINKABLE_CATEGORY_MUST_NOT_BE_ROOT = decommissioning.createBooleanFalseByDefault("linkableCategoryMustNotBeRoot"));

		// Only allow to pick a category if it has at least one approved retention rule
		add(LINKABLE_CATEGORY_MUST_HAVE_APPROVED_RULES = decommissioning
				.createBooleanFalseByDefault("linkableCategoryMustHaveApprovedRules"));

		// Allow to empty and reuse a container (manually and upon destruction via decommissioning)
		add(CONTAINER_RECYCLING_ALLOWED = decommissioning.createBooleanFalseByDefault("containerRecyclingAllowed"));

		// Allow to put folders from different administrative units in a single container
		add(MIXED_CONTAINERS_ALLOWED = decommissioning.createBooleanFalseByDefault("mixedContainersAllowed"));

		add(ACTIVES_IN_CONTAINER_ALLOWED = decommissioning.createBooleanFalseByDefault("activesInContainerAllowed"));

		add(UNIFORM_SUBDIVISION_ENABLED = decommissioning.createBooleanFalseByDefault("uniformSubdivisionEnabled"));

		add(IS_DECOMMISSIONING_TYPE_REQUIRED_IN_CONTAINERS = decommissioning.createBooleanTrueByDefault("isDecommissioningTypeRequiredInContainers")
				.scriptedBy(RMDecommissioningTypeRequiredScript.class));

		add(NUMBER_OF_DAYS_BEFORE_PREDICTED_DECOMMISSIONING_DATE = decommissioning.createInteger("numberOfDaysBeforePredictedDecommissioningDate")
				.withDefaultValue(0));

		SystemConfigurationGroup trees = new SystemConfigurationGroup(ID, "trees");

		add(DISPLAY_SEMI_ACTIVE_RECORDS_IN_TREES = trees.createBooleanFalseByDefault("displaySemiActiveInTrees"));

		add(DISPLAY_DEPOSITED_RECORDS_IN_TREES = trees.createBooleanFalseByDefault("displayDepositedInTrees"));

		add(DISPLAY_DESTROYED_RECORDS_IN_TREES = trees.createBooleanFalseByDefault("displayDestroyedInTrees"));

		add(DISPLAY_CONTAINERS_IN_TREES = trees.createBooleanFalseByDefault("displayContainersInTrees"));

		SystemConfigurationGroup agent = new SystemConfigurationGroup(ID, "agent");

		add(AGENT_ENABLED = agent.createBooleanTrueByDefault("enabled"));

		add(AGENT_SWITCH_USER_POSSIBLE = agent.createBooleanTrueByDefault("switchUserPossible"));

		add(AGENT_DOWNLOAD_ALL_USER_CONTENT = agent.createBooleanTrueByDefault("downloadAllUserContent"));

		add(AGENT_EDIT_USER_DOCUMENTS = agent.createBooleanTrueByDefault("editUserDocuments"));

		add(AGENT_BACKUP_RETENTION_PERIOD_IN_DAYS = agent.createInteger("backupRetentionPeriodInDays").withDefaultValue(30));

		add(AGENT_TOKEN_DURATION_IN_HOURS = agent.createInteger("tokenDurationInHours").withDefaultValue(10));

		add(AGENT_READ_ONLY_WARNING = agent.createBooleanTrueByDefault("readOnlyWarning"));

		add(AGENT_DISABLED_UNTIL_FIRST_CONNECTION = agent.createBooleanFalseByDefault("agentDisabledUntilFirstConnection"));

		add(AGENT_MOVE_IMPORTED_FILES_TO_TRASH = agent.createBooleanTrueByDefault("agentMoveImportedFilesToTrash"));

		add(AGENT_CREATE_DROP_DIR_SHORTCUT = agent.createBooleanTrueByDefault("agentCreateDropDirShortcut"));

		SystemConfigurationGroup others = new SystemConfigurationGroup(ID, "others");

		add(ENABLE_TYPE_RESTRICTION_IN_FOLDER = others.createBooleanFalseByDefault("enableTypeRestrictionInFolder")
				.scriptedBy(EnableOrDisableTypeRestrictionInFolderScript.class).whichIsHidden());

		add(BORROWING_DURATION_IN_DAYS = others.createInteger("borrowingDurationDays").withDefaultValue(7));

		add(OPEN_HOLDER = others.createBooleanFalseByDefault("openHolder"));

		add(MAJOR_VERSION_FOR_NEW_FILE = others.createBooleanFalseByDefault("majorVersionForNewFile"));

		add(DOCUMENTS_TYPES_CHOICE = others.createEnum("documentsTypeChoice", DocumentsTypeChoice.class)
				.withDefaultValue(DocumentsTypeChoice.LIMIT_TO_SAME_DOCUMENTS_TYPES_OF_RETENTION_RULES));

		add(ALLOW_MODIFICATION_OF_ARCHIVISTIC_STATUS_AND_EXPECTED_DATES = decommissioning
				.createEnum("allowModificationOfArchivisticStatusAndExpectedDates",
						AllowModificationOfArchivisticStatusAndExpectedDatesChoice.class)
				.withDefaultValue(AllowModificationOfArchivisticStatusAndExpectedDatesChoice.DISABLED)
				.scriptedBy(EnableOrDisableCalculatorsManualMetadataScript.class));

		add(CALCULATED_METADATAS_BASED_ON_FIRST_TIMERANGE_PART = decommissioning
				.createBooleanTrueByDefault("calculatedMetadatasBasedOnFirstTimerangePart"));

		add(FOLDER_ADMINISTRATIVE_UNIT_ENTERED_AUTOMATICALLY = others
				.createBooleanTrueByDefault("folderAdministrativeUnitEnteredAutomatically"));

		add(STORAGE_SPACE_TITLE_CALCULATOR_ENABLED = others
				.createBooleanTrueByDefault("enableStorageSpaceTitleCalculator")
				.scriptedBy(EnableOrDisableStorageSpaceTitleCalculatorScript.class));

		add(DEFAULT_TAB_IN_FOLDER_DISPLAY = others.createString("defaultTabInFolderDisplay")
				.withDefaultValue(DefaultTabInFolderDisplay.CONTENT.getCode()));

		add(CHECK_OUT_DOCUMENT_AFTER_CREATION = others.createBooleanTrueByDefault("checkoutDocumentAfterCreation"));

		add(POPULATE_BORDEREAUX_WITH_COLLECTION = decommissioning.createBooleanTrueByDefault("populateBordereauxWithCollection"));

		add(POPULATE_BORDEREAUX_WITH_LESSER_DISPOSITION_DATE = decommissioning
				.createBooleanFalseByDefault("populateBordereauxWithLesserDispositionDate"));

		add(IS_CONTAINER_MULTIVALUE = decommissioning.createBooleanFalseByDefault("multipleContainerStorageSpaces")
				.scriptedBy(EnableOrDisableContainerMultiValueMetadataScript.class)
				.whichIsHidden());

		add(COMPLETE_DECOMMISSIONNING_DATE_WHEN_CREATING_FOLDER_WITH_MANUAL_STATUS =
				decommissioning.createEnum("completeDecommissioningDateWhenCreatingFolderWithManualStatus",
						CompleteDatesWhenAddingFolderWithManualStatusChoice.class)
						.withDefaultValue(CompleteDatesWhenAddingFolderWithManualStatusChoice.DISABLED));

		add(ALLOW_SORTING_IN_FOLDER_LIST_OF_DECOMMISSIONING = decommissioning
				.createBooleanFalseByDefault("allowFolderSortingOfDecommissioningList").whichIsHidden());

		add(LOG_FOLDER_DOCUMENT_ACCESS_WITH_CMIS = others.createBooleanFalseByDefault("logFolderDocumentAccessWithCMIS"));

		add(COPY_RULES_ALWAYS_VISIBLE_IN_ADD_FORM = decommissioning
				.createBooleanFalseByDefault("copyRulesAlwaysVisibleInAddForm"));

		add(ALLOW_TRANSFER_DATE_FIELD_WHEN_COPY_RULE_HAS_NO_SEMIACTIVE_STATE = decommissioning
				.createBooleanFalseByDefault("allowTransferDateFieldWhenCopyRuleHasNoSemiActiveState"));


		add(DEPOSIT_AND_DESTRUCTION_DATES_BASED_ON_ACTUAL_TRANSFER_DATE = decommissioning
				.createBooleanTrueByDefault("depositAndDestructionDatesBasedOnActualTransferDate").withReIndexationRequired());

		add(NEED_REASON_BEFORE_DELETING_FOLDERS = others.createBooleanTrueByDefault("needReasonBeforeDeletingFolders"));

		add(DECOMMISSIONING_LIST_WITH_SELECTED_FOLDERS = decommissioning
				.createBooleanFalseByDefault("decommissioningListWithSelectedFolders"));

		add(CREATE_MISSING_AUTHORIZATIONS_FOR_TASK = others.createBooleanFalseByDefault("createMissingAuthorizationsForTask"));

		add(IGNORE_VALIDATIONS_IN_BATCH_PROCESSING = others.createBooleanFalseByDefault("ignoreValidationsInBatchProcessing")
				.whichIsHidden());

		add(DOCUMENT_SUMMARY_CACHE_ENABLED = others.createBooleanTrueByDefault("documentSummaryCacheEnabled")
				.whichIsHidden().scriptedBy(RMDocumentSummaryCacheEnabledScript.class));

	}

	static void add(SystemConfiguration configuration) {
		configurations.add(configuration);
	}

	SystemConfigurationsManager manager;

	public RMConfigs(AppLayerFactory appLayerFactory) {
		this.manager = appLayerFactory.getModelLayerFactory().getSystemConfigurationsManager();
	}

	public RMConfigs(ModelLayerFactory modelLayerFactory) {
		this.manager = modelLayerFactory.getSystemConfigurationsManager();
	}

	public RMConfigs(SystemConfigurationsManager manager) {
		this.manager = manager;
	}

	public AllowModificationOfArchivisticStatusAndExpectedDatesChoice allowModificationOfArchivisticStatusAndExpectedDates() {
		return manager.getValue(ALLOW_MODIFICATION_OF_ARCHIVISTIC_STATUS_AND_EXPECTED_DATES);
	}

	public boolean isCalculatedClosingDate() {
		return manager.getValue(CALCULATED_CLOSING_DATE);
	}

	public DecommissioningDateBasedOn decommissioningDateBasedOn() {
		return manager.getValue(DECOMMISSIONING_DATE_BASED_ON);
	}

	public int calculatedClosingDateNumberOfYearWhenFixedRule() {
		return manager.getValue(CALCULATED_CLOSING_DATE_NUMBER_OF_YEAR_WHEN_FIXED_RULE);
	}

	public int calculatedClosingDateNumberOfYearWhenVariableRule() {
		return manager.getValue(CALCULATED_CLOSING_DATE_NUMBER_OF_YEAR_WHEN_VARIABLE_RULE);
	}

	public int calculatedSemiActiveDateNumberOfYearWhenVariablePeriod() {
		return manager.getValue(CALCULATED_SEMIACTIVE_DATE_NUMBER_OF_YEAR_WHEN_VARIABLE_PERIOD);
	}

	public int calculatedInactiveDateNumberOfYearWhenVariablePeriod() {
		return manager.getValue(CALCULATED_INACTIVE_DATE_NUMBER_OF_YEAR_WHEN_VARIABLE_PERIOD);
	}

	public String yearEnd() {
		return manager.getValue(YEAR_END_DATE);
	}

	public int requiredDaysBeforeYearEndForNotAddingAYear() {
		return manager.getValue(REQUIRED_DAYS_BEFORE_YEAR_END_FOR_NOT_ADDING_A_YEAR);
	}

	public boolean isEnforcedCategoryAndRuleRelationshipInFolder() {
		return manager.getValue(ENFORCE_CATEGORY_AND_RULE_RELATIONSHIP_IN_FOLDER);
	}

	public boolean isRulePrincipalCopyRequired() {
		return manager.getValue(COPY_RULE_PRINCIPAL_REQUIRED);
	}

	public boolean areDocumentRetentionRulesEnabled() {
		return manager.getValue(DOCUMENT_RETENTION_RULES);
	}

	public boolean isCopyRuleTypeAlwaysModifiable() {
		return manager.getValue(COPY_RULE_TYPE_ALWAYS_MODIFIABLE);
	}

	public boolean purgeMinorVersionsOnTransfer() {
		return manager.getValue(MINOR_VERSIONS_PURGED_ON) == DecommissioningPhase.ON_TRANSFER_OR_DEPOSIT;
	}

	public boolean isLoggingFolderDocumentAccessWithCMISEnable() {
		return manager.getValue(LOG_FOLDER_DOCUMENT_ACCESS_WITH_CMIS);
	}

	public boolean purgeMinorVersionsOnDeposit() {
		return manager.getValue(MINOR_VERSIONS_PURGED_ON) == DecommissioningPhase.ON_DEPOSIT;
	}

	public boolean purgeCurrentVersionIfMinor() {
		return manager.getValue(ALSO_PURGE_CURRENT_VERSION_IF_MINOR);
	}

	public boolean createPDFaOnTransfer() {
		return manager.getValue(PDFA_CREATED_ON) == DecommissioningPhase.ON_TRANSFER_OR_DEPOSIT;
	}

	public boolean isApprovalRequiredForClosing() {
		return manager.getValue(REQUIRE_APPROVAL_FOR_CLOSING);
	}

	public boolean isApprovalRequiredForTransfer() {
		return manager.getValue(REQUIRE_APPROVAL_FOR_TRANSFER);
	}

	public boolean isApprovalRequiredForDepositOfActive() {
		return manager.getValue(REQUIRE_APPROVAL_FOR_DEPOSIT_OF_ACTIVE);
	}

	public boolean isApprovalRequiredForDepositOfSemiActive() {
		return manager.getValue(REQUIRE_APPROVAL_FOR_DEPOSIT_OF_SEMIACTIVE);
	}

	public boolean isApprovalRequiredForDestructionOfActive() {
		return manager.getValue(REQUIRE_APPROVAL_FOR_DESTRUCTION_OF_ACTIVE);
	}

	public boolean isApprovalRequiredForDestructionOfSemiActive() {
		return manager.getValue(REQUIRE_APPROVAL_FOR_DESTRUCTION_OF_SEMIACTIVE);
	}

	public boolean createPDFaOnDeposit() {
		return manager.getValue(PDFA_CREATED_ON) == DecommissioningPhase.ON_DEPOSIT;
	}

	public boolean deleteFolderRecordsWithDestruction() {
		return manager.getValue(DELETE_FOLDER_RECORDS_WITH_DESTRUCTION);
	}

	public boolean deleteDocumentRecordsWithDestruction() {
		return manager.getValue(DELETE_DOCUMENT_RECORDS_WITH_DESTRUCTION);
	}

	public boolean isContainerRecyclingAllowed() {
		return manager.getValue(CONTAINER_RECYCLING_ALLOWED);
	}

	public boolean areMixedContainersAllowed() {
		return manager.getValue(MIXED_CONTAINERS_ALLOWED);
	}

	public boolean areActiveInContainersAllowed() {
		return manager.getValue(ACTIVES_IN_CONTAINER_ALLOWED);
	}

	public boolean isAgentEnabled() {
		return manager.getValue(AGENT_ENABLED);
	}

	public boolean isAgentSwitchUserPossible() {
		return manager.getValue(AGENT_SWITCH_USER_POSSIBLE);
	}

	public boolean isAgentDownloadAllUserContent() {
		return manager.getValue(AGENT_DOWNLOAD_ALL_USER_CONTENT);
	}

	public boolean isAgentEditUserDocuments() {
		return manager.getValue(AGENT_EDIT_USER_DOCUMENTS);
	}

	public int getAgentBackupRetentionPeriodInDays() {
		return manager.getValue(AGENT_BACKUP_RETENTION_PERIOD_IN_DAYS);
	}

	public int getAgentTokenDurationInHours() {
		return manager.getValue(AGENT_TOKEN_DURATION_IN_HOURS);
	}

	public boolean isAgentReadOnlyWarning() {
		return manager.getValue(AGENT_READ_ONLY_WARNING);
	}

	public boolean isAgentDisabledUntilFirstConnection() {
		return manager.getValue(AGENT_DISABLED_UNTIL_FIRST_CONNECTION);
	}

	public boolean isAgentMoveImportedFilesToTrash() {
		return manager.getValue(AGENT_MOVE_IMPORTED_FILES_TO_TRASH);
	}

	public boolean isAgentCreateDropDirShortcut() {
		return manager.getValue(AGENT_CREATE_DROP_DIR_SHORTCUT);
	}

	public int getBorrowingDurationDays() {
		return manager.getValue(BORROWING_DURATION_IN_DAYS);
	}

	public boolean isOpenHolder() {
		return manager.getValue(OPEN_HOLDER);
	}

	public boolean isMajorVersionForNewFile() {
		return manager.getValue(MAJOR_VERSION_FOR_NEW_FILE);
	}

	public boolean areUniformSubdivisionEnabled() {
		return manager.getValue(UNIFORM_SUBDIVISION_ENABLED);
	}

	public DocumentsTypeChoice getDocumentsTypesChoice() {
		return manager.getValue(DOCUMENTS_TYPES_CHOICE);
	}

	public boolean isCalculateOpenDateBasedOnFirstTimerangePart() {
		return manager.getValue(CALCULATED_METADATAS_BASED_ON_FIRST_TIMERANGE_PART);
	}

	public boolean isPopulateBordereauxWithCollection() {
		return manager.getValue(POPULATE_BORDEREAUX_WITH_COLLECTION);
	}

	public boolean isPopulateBordereauxWithLesserDispositionDate() {
		return manager.getValue(POPULATE_BORDEREAUX_WITH_LESSER_DISPOSITION_DATE);
	}

	public boolean isContainerMultivalue() {
		return manager.getValue(IS_CONTAINER_MULTIVALUE);
	}

	public String getDefaultTabInFolderDisplay() {
		return manager.getValue(DEFAULT_TAB_IN_FOLDER_DISPLAY);
	}

	public boolean areDocumentCheckedOutAfterCreation() {
		return manager.getValue(CHECK_OUT_DOCUMENT_AFTER_CREATION);
	}

	public boolean isFolderAdministrativeUnitEnteredAutomatically() {
		return manager.getValue(FOLDER_ADMINISTRATIVE_UNIT_ENTERED_AUTOMATICALLY);
	}

	public CompleteDatesWhenAddingFolderWithManualStatusChoice getCompleteDecommissioningDateWhenCreatingFolderWithManualStatus() {
		return manager.getValue(COMPLETE_DECOMMISSIONNING_DATE_WHEN_CREATING_FOLDER_WITH_MANUAL_STATUS);
	}

	public AllowModificationOfArchivisticStatusAndExpectedDatesChoice getAllowModificationOfArchivisticStatusAndExpectedDates() {
		return manager.getValue(ALLOW_MODIFICATION_OF_ARCHIVISTIC_STATUS_AND_EXPECTED_DATES);
	}

	public boolean isContainerMultipleValue() {
		return manager.getValue(IS_CONTAINER_MULTIVALUE);
	}

	public String getYearEndDate() {
		return manager.getValue(YEAR_END_DATE);
	}

	public boolean isCopyRulesAlwaysVisibleInAddForm() {
		return manager.getValue(COPY_RULES_ALWAYS_VISIBLE_IN_ADD_FORM);
	}

	public boolean isAllowTransferDateFieldWhenCopyRuleHasNoSemiActiveState() {
		return manager.getValue(ALLOW_TRANSFER_DATE_FIELD_WHEN_COPY_RULE_HAS_NO_SEMIACTIVE_STATE);
	}

	public boolean isDecommissioningTypeRequiredInContainers() {
		return manager.getValue(IS_DECOMMISSIONING_TYPE_REQUIRED_IN_CONTAINERS);
	}

	public boolean isNeedingAReasonBeforeDeletingFolders() {
		return manager.getValue(NEED_REASON_BEFORE_DELETING_FOLDERS);
	}

	public boolean isDepositAndDestructionDatesBasedOnActualTransferDate() {
		return manager.getValue(DEPOSIT_AND_DESTRUCTION_DATES_BASED_ON_ACTUAL_TRANSFER_DATE);
	}

	public boolean isDecommissioningListWithSelectedFolders() {
		return manager.getValue(DECOMMISSIONING_LIST_WITH_SELECTED_FOLDERS);
	}

	public int getNumberOfDaysBeforePredictedDecommissioningDate() {
		return manager.getValue(NUMBER_OF_DAYS_BEFORE_PREDICTED_DECOMMISSIONING_DATE);
	}

	public boolean isCreateMissingAuthorizationsForTask() {
		return manager.getValue(CREATE_MISSING_AUTHORIZATIONS_FOR_TASK);
	}

	public boolean isIgnoreValidationsInBatchProcessing() {
		return manager.getValue(IGNORE_VALIDATIONS_IN_BATCH_PROCESSING);
	}

	public boolean isTypeRestrictionEnabledInFolder() {
		return manager.getValue(ENABLE_TYPE_RESTRICTION_IN_FOLDER);
	}
}
