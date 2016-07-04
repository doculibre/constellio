package com.constellio.app.modules.rm;

import static com.constellio.app.modules.rm.ConstellioRMModule.ID;

import java.util.ArrayList;
import java.util.List;

import com.constellio.app.modules.rm.model.enums.DecommissioningDateBasedOn;
import com.constellio.app.modules.rm.model.enums.DocumentsTypeChoice;
import com.constellio.model.entities.configs.SystemConfiguration;
import com.constellio.model.entities.configs.SystemConfigurationGroup;
import com.constellio.model.services.configs.SystemConfigurationsManager;

public class RMConfigs {

	public enum DecommissioningPhase {
		NEVER, ON_DEPOSIT, ON_TRANSFER_OR_DEPOSIT
	}

	static List<SystemConfiguration> configurations = new ArrayList<>();

	// Retention calendar configs
	public static final SystemConfiguration DOCUMENT_RETENTION_RULES,
			CALCULATED_CLOSING_DATE,
			DECOMMISSIONING_DATE_BASED_ON,
			CALCULATED_CLOSING_DATE_NUMBER_OF_YEAR_WHEN_FIXED_RULE,
			CALCULATED_CLOSING_DATE_NUMBER_OF_YEAR_WHEN_VARIABLE_RULE,
			YEAR_END_DATE, REQUIRED_DAYS_BEFORE_YEAR_END_FOR_NOT_ADDING_A_YEAR,
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
			WORKFLOWS_ENABLED,
			ENFORCE_CATEGORY_AND_RULE_RELATIONSHIP_IN_FOLDER;

	// Category configs
	public static final SystemConfiguration LINKABLE_CATEGORY_MUST_NOT_BE_ROOT, LINKABLE_CATEGORY_MUST_HAVE_APPROVED_RULES;

	// Tree configs
	public static final SystemConfiguration DISPLAY_SEMI_ACTIVE_RECORDS_IN_TREES, DISPLAY_DEPOSITED_RECORDS_IN_TREES,
			DISPLAY_DESTROYED_RECORDS_IN_TREES, DISPLAY_CONTAINERS_IN_TREES;

	// Agent configs
	public static final SystemConfiguration AGENT_ENABLED, AGENT_SWITCH_USER_POSSIBLE, AGENT_DOWNLOAD_ALL_USER_CONTENT,
			AGENT_EDIT_USER_DOCUMENTS, AGENT_BACKUP_RETENTION_PERIOD_IN_DAYS, AGENT_TOKEN_DURATION_IN_HOURS;

	// other
	public static final SystemConfiguration OPEN_HOLDER;

	static {
		//SystemConfigurationGroup beta = new SystemConfigurationGroup(ID, "beta");

		SystemConfigurationGroup decommissioning = new SystemConfigurationGroup(ID, "decommissioning");

		// Allow to enter retention rules for documents
		add(DOCUMENT_RETENTION_RULES = decommissioning.createBooleanFalseByDefault("documentRetentionRules"));

		// Validation exception if a folder's rule and category are not linked
		add(ENFORCE_CATEGORY_AND_RULE_RELATIONSHIP_IN_FOLDER = decommissioning
				.createBooleanTrueByDefault("enforceCategoryAndRuleRelationshipInFolder"));

		// Is the closing date calculated or manual?
		add(CALCULATED_CLOSING_DATE = decommissioning.createBooleanTrueByDefault("calculatedCloseDate"));

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
				.withDefaultValue(1));

		// Delays are computed from the opening date (if true), or the closing date (if false)
		add(DECOMMISSIONING_DATE_BASED_ON = decommissioning
				.createEnum("decommissioningDateBasedOn", DecommissioningDateBasedOn.class)
				.withDefaultValue(DecommissioningDateBasedOn.CLOSE_DATE));

		// End of the civil year for the purposes of calculating the delays (MM/DD)
		add(YEAR_END_DATE = decommissioning.createString("yearEndDate").withDefaultValue("12/31"));

		//Nombre de jours devant précéder la date de fin d'année pour que celle-ci soit considérée dans le calcul des délais pour l'année en cours
		add(REQUIRED_DAYS_BEFORE_YEAR_END_FOR_NOT_ADDING_A_YEAR = decommissioning
				.createInteger("closeDateRequiredDaysBeforeYearEnd")
				.withDefaultValue(90));

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

		SystemConfigurationGroup others = new SystemConfigurationGroup(ID, "others");

		add(BORROWING_DURATION_IN_DAYS = others.createInteger("borrowingDurationDays").withDefaultValue(7));

		add(OPEN_HOLDER = others.createBooleanFalseByDefault("openHolder"));

		add(DOCUMENTS_TYPES_CHOICE = others.createEnum("documentsTypeChoice", DocumentsTypeChoice.class)
				.withDefaultValue(DocumentsTypeChoice.LIMIT_TO_SAME_DOCUMENTS_TYPES_OF_RETENTION_RULES));

		add(WORKFLOWS_ENABLED = others.createBooleanFalseByDefault("workflowsEnabled"));

	}

	static void add(SystemConfiguration configuration) {
		configurations.add(configuration);
	}

	SystemConfigurationsManager manager;

	public RMConfigs(SystemConfigurationsManager manager) {
		this.manager = manager;
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

	public int getBorrowingDurationDays() {
		return manager.getValue(BORROWING_DURATION_IN_DAYS);
	}

	public boolean isOpenHolder() {
		return manager.getValue(OPEN_HOLDER);
	}

	public boolean areWorkflowsEnabled() {
		return manager.getValue(WORKFLOWS_ENABLED);
	}

	public DocumentsTypeChoice getDocumentsTypesChoice() {
		return manager.getValue(DOCUMENTS_TYPES_CHOICE);
	}
}
