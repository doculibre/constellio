package com.constellio.model.services.migrations;

import com.constellio.data.utils.TimeProvider;
import com.constellio.model.entities.configs.AbstractSystemConfigurationScript;
import com.constellio.model.entities.configs.SystemConfiguration;
import com.constellio.model.entities.configs.SystemConfigurationGroup;
import com.constellio.model.entities.configs.core.listeners.UserTitlePatternConfigScript;
import com.constellio.model.entities.enums.AutocompleteSplitCriteria;
import com.constellio.model.entities.enums.EmailTextFormat;
import com.constellio.model.entities.enums.GroupAuthorizationsInheritance;
import com.constellio.model.entities.enums.MemoryConsumptionLevel;
import com.constellio.model.entities.enums.MetadataPopulatePriority;
import com.constellio.model.entities.enums.ParsingBehavior;
import com.constellio.model.entities.enums.SearchSortType;
import com.constellio.model.entities.enums.TitleMetadataPopulatePriority;
import com.constellio.model.frameworks.validation.ValidationErrors;
import com.constellio.model.services.configs.EnableThumbnailsScript;
import com.constellio.model.services.configs.SystemConfigurationsManager;
import com.constellio.model.services.factories.ModelLayerFactory;
import org.apache.commons.lang.StringUtils;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static com.constellio.model.services.migrations.TimeScheduleConfigurationValidator.isCurrentlyInSchedule;

public class ConstellioEIMConfigs {

	private static List<SystemConfiguration> modifiableConfigs = new ArrayList<>();
	public static List<SystemConfiguration> configurations;

	//Retention calendar configs

	public static final SystemConfiguration DEFAULT_PARSING_BEHAVIOR;

	public static final SystemConfiguration INCLUDE_CONTENTS_IN_SAVESTATE;

	public static final SystemConfiguration ENABLE_ADMIN_USER_PASSWORD_CHANGE;
	public static final SystemConfiguration USER_TITLE_PATTERN;

	public static final SystemConfiguration USER_ROLES_IN_AUTHORIZATIONS;
	public static final SystemConfiguration PARSED_CONTENT_MAX_LENGTH_IN_KILOOCTETS;
	public static final SystemConfiguration CONTENT_MAX_LENGTH_FOR_PARSING_IN_MEGAOCTETS;
	public static final SystemConfiguration FILE_EXTENSIONS_EXCLUDED_FROM_PARSING;
	public static final SystemConfiguration PDFTRON_LICENSE;

	public static final SystemConfiguration METADATA_POPULATE_PRIORITY, TITLE_METADATA_POPULATE_PRIORITY;
	public static final SystemConfiguration LOGO;
	public static final SystemConfiguration LOGO_LINK;
	public static final SystemConfiguration AUTHENTIFICATION_IMAGE;
	public static final SystemConfiguration CONSTELLIO_URL;
	public static final SystemConfiguration CLEAN_DURING_INSTALL;
	public static final SystemConfiguration IN_UPDATE_PROCESS;
	public static final SystemConfiguration BATCH_PROCESSING_LIMIT;

	public static final SystemConfiguration CMIS_NEVER_RETURN_ACL;

	public static final SystemConfiguration DATE_FORMAT;
	public static final SystemConfiguration DATE_TIME_FORMAT;
	public static final SystemConfiguration TRASH_PURGE_DELAI;

	public static final SystemConfiguration MAX_SELECTABLE_SEARCH_RESULTS;
	public static final SystemConfiguration WRITE_ZZRECORDS_IN_TLOG;

	public static final SystemConfiguration SEARCH_SORT_TYPE;

	public static final SystemConfiguration ICAP_SCAN_ACTIVATED;

	public static final SystemConfiguration ICAP_SERVER_URL;

	public static final SystemConfiguration ICAP_RESPONSE_TIMEOUT;

	public static final SystemConfiguration CKEDITOR_TOOLBAR_CONFIG;

	public static final SystemConfiguration GROUP_AUTHORIZATIONS_INHERITANCE;

	public static final SystemConfiguration REMOVE_EXTENSION_FROM_RECORD_TITLE;

	public static final SystemConfiguration TABLE_DYNAMIC_CONFIGURATION;

	public static final SystemConfiguration TRANSACTION_DELAY;

	public static final SystemConfiguration REPLACE_SPACES_IN_SIMPLE_SEARCH_FOR_ANDS;

	public static final SystemConfiguration UPDATE_SERVER_CONNECTION_ENABLED;

	public static final SystemConfiguration ADD_COMMENTS_WHEN_READ_AUTHORIZATION;

	public static final SystemConfiguration SEARCH_RESULTS_HIGHLIGHTING_ENABLED;
	;

	public static final String DEFAULT_CKEDITOR_TOOLBAR_CONFIG = "" +
																 "  { name: 'basicstyles', items: [ 'Bold', 'Italic', 'Underline', 'Strike', '-', 'RemoveFormat' ] },\r\n" +
																 "	{ name: 'paragraph', items: [ 'NumberedList', 'BulletedList', '-', 'JustifyLeft', 'JustifyCenter', 'JustifyRight', 'JustifyBlock'] },\r\n" +
																 "	{ name: 'links', items: [ 'Link', 'Unlink', 'Anchor' ] },\r\n" +
																 "	{ name: 'styles', items: [ 'Styles', 'Format', 'Font', 'FontSize' ] },\r\n" +
																 "	{ name: 'colors', items: [ 'TextColor', 'BGColor' ] }";

	public static final SystemConfiguration DEFAULT_START_TAB;

	public static final SystemConfiguration DEFAULT_TAXONOMY;

	public static final SystemConfiguration TAXONOMY_ORDER_IN_HOME_VIEW;

	public static final SystemConfiguration LAZY_TREE_BUFFER_SIZE;

	public static final SystemConfiguration AUTOCOMPLETE_SIZE;
	public static final SystemConfiguration AUTOCOMPLETE_SPLIT_CRITERIA;

	//public static final SystemConfiguration DEFAULT_FONT_SIZE;

	public static final SystemConfiguration LAST_BACKUP_DAY, KEEP_EVENTS_FOR_X_MONTH;

	public static final SystemConfiguration SHOW_TRIANGLE_ONLY_WHEN_FOLDER_HAS_CONTENT;

	public static final SystemConfiguration MEMORY_CONSUMPTION_LEVEL;

	public static final SystemConfiguration CONTENT_PARSING_SCHEDULE;
	public static final SystemConfiguration VIEWER_CONTENTS_CONVERSION_SCHEDULE;
	public static final SystemConfiguration UNREFERENCED_CONTENTS_DELETE_SCHEDULE;
	public static final SystemConfiguration ENABLE_STATISTIC_REPORT;
	public static final SystemConfiguration REMOVE_TAB_AND_NEW_LINE_ON_DELTA_FIELD_IN_EDIT_REPORT;
	public static final SystemConfiguration BATCH_PROCESSES_SCHEDULE;
	public static final SystemConfiguration IS_RUNNING_WITH_SOLR_6;
	public static final SystemConfiguration PRIVACY_POLICY;
	public static final SystemConfiguration LOGIN_NOTIFICATION_ALERT;
	public static final SystemConfiguration ADD_SECONDARY_SORT_WHEN_SORTING_BY_SCORE;
	public static final SystemConfiguration INCLUDE_FROM_FIELD_WHEN_GENERATING_EMAILS;

	public static final SystemConfiguration SEIZE_MULTILANGUAL_VALUES;
	public static final SystemConfiguration ARE_ALL_MULTI_LANGUAL_VALUES_MANDATORY;
	public static final SystemConfiguration ENABLE_ESSENTIAL_METADATA_HIDING;

	public static final SystemConfiguration ENABLE_INACTIF_SCHEMAS_IN_SEARCH;

	public static final SystemConfiguration SPACE_QUOTA_FOR_USER_DOCUMENTS;

	public static final SystemConfiguration BATCH_PROCESSES_MAXIMUM_HISTORY_SIZE;

	public static final SystemConfiguration ADD_RECORD_ID_IN_EMAILS;

	public static final SystemConfiguration GENERATED_EMAIL_FORMAT;

	public static final SystemConfiguration IS_TRASH_THREAD_EXECUTING;

	public static final SystemConfiguration ENABLE_SYSTEM_STATE_MEMORY_ALLOCATION;
	public static final SystemConfiguration ENABLE_SYSTEM_STATE_OPT_DISK_USAGE;
	public static final SystemConfiguration ENABLE_SYSTEM_STATE_SOLR_DISK_USAGE;
	public static final SystemConfiguration ENABLE_SYSTEM_STATE_LICENSE;

	public static final SystemConfiguration ENABLE_THUMBNAIL_GENERATION;

	public static final SystemConfiguration SHOW_RESULTS_NUMBERING_IN_LIST_VIEW;

	public static final SystemConfiguration SHOW_PATH_TO_RESULT;

	public static final SystemConfiguration ENABLE_LEARN_TO_RANK_FEATURE;

	public static final SystemConfiguration NO_LINKS_IN_SEARCH_RESULTS;
	public static final SystemConfiguration LAZY_LOADED_SEARCH_RESULTS;
	public static final SystemConfiguration LEGACY_IDENTIFIER_INDEXED_IN_MEMORY;
	public static final SystemConfiguration ENABLE_FACETS_APPLY_BUTTON;


	public static final SystemConfiguration DISPLAY_ONLY_SUMMARY_METADATAS_IN_TABLES;

	public static final SystemConfiguration SEARCH_USING_EDISMAX;
	public static final SystemConfiguration SEARCH_USING_TERMS_IN_BQ;

	static {
		SystemConfigurationGroup others = new SystemConfigurationGroup(null, "others");
		add(DEFAULT_PARSING_BEHAVIOR = others.createEnum("defaultParsingBehavior", ParsingBehavior.class)
				.withDefaultValue(ParsingBehavior.SYNC_PARSING_FOR_ALL_CONTENTS));
		add(INCLUDE_CONTENTS_IN_SAVESTATE = others.createBooleanFalseByDefault("includeContentsInSavestate"));
		add(USER_TITLE_PATTERN = others.createString("userTitlePattern").scriptedBy(UserTitlePatternConfigScript.class)
				.withDefaultValue("${firstName} ${lastName}"));


		// Associer ou non des rôles utilisateur aux autorisations
		add(USER_ROLES_IN_AUTHORIZATIONS = others.createBooleanFalseByDefault("userRolesInAuthorizations"));

		add(LOGO = others.createBinary("logo"));
		add(LOGO_LINK = others.createString("logoLink", "http://www.constellio.com"));
		add(AUTHENTIFICATION_IMAGE = others.createBinary("authentificationImage"));
		add(PRIVACY_POLICY = others.createBinary("privacyPolicy"));
		add(METADATA_POPULATE_PRIORITY = others.createEnum("metadataPopulatePriority", MetadataPopulatePriority.class)
				.withDefaultValue(MetadataPopulatePriority.STYLES_REGEX_PROPERTIES));
		add(TITLE_METADATA_POPULATE_PRIORITY = others
				.createEnum("titleMetadataPopulatePriority", TitleMetadataPopulatePriority.class)
				.withDefaultValue(TitleMetadataPopulatePriority.STYLES_FILENAME_PROPERTIES));
		add(CONSTELLIO_URL = others.createString("constellioUrl", "http://localhost:8080/constellio/"));
		add(INCLUDE_FROM_FIELD_WHEN_GENERATING_EMAILS = others.createBooleanTrueByDefault("includeFromFieldWhenGeneratingEmails"));

		add(DATE_FORMAT = others.createString("dateFormat").withDefaultValue("yyyy-MM-dd"));
		add(DATE_TIME_FORMAT = others.createString("dateTimeFormat").withDefaultValue("yyyy-MM-dd HH:mm:ss"));

		SystemConfigurationGroup advanced = new SystemConfigurationGroup(null, "advanced");
		add(PARSED_CONTENT_MAX_LENGTH_IN_KILOOCTETS = advanced.createInteger("parsedContentMaxLengthInKilooctets").whichIsHidden()
				.withDefaultValue(1024));
		add(CONTENT_MAX_LENGTH_FOR_PARSING_IN_MEGAOCTETS = advanced.createInteger("contentMaxLengthForParsingInMegaoctets")
				.withDefaultValue(30));
		add(FILE_EXTENSIONS_EXCLUDED_FROM_PARSING = advanced.createString("fileExtensionsExcludedFromParsing").withReIndexationRequired());
		add(PDFTRON_LICENSE = advanced.createString("pdftronLicense"));

		add(CLEAN_DURING_INSTALL = advanced.createBooleanFalseByDefault("cleanDuringInstall"));

		add(LEGACY_IDENTIFIER_INDEXED_IN_MEMORY = advanced.createBooleanFalseByDefault("legacyIdentifierIndexedInMemory")
				.whichRequiresReboot());

		SystemConfigurationGroup hiddenSystemConfigs = new SystemConfigurationGroup(null, "system");
		add(IN_UPDATE_PROCESS = hiddenSystemConfigs.createBooleanFalseByDefault("inUpdateProcess").whichIsHidden());
		add(LOGIN_NOTIFICATION_ALERT = hiddenSystemConfigs.createBinary("loginNotificationAlert").whichIsHidden());
		add(BATCH_PROCESSING_LIMIT = others.createInteger("batchProcessingLimit").withDefaultValue(-1));
		add(TRASH_PURGE_DELAI = others.createInteger("trashPurgeDelaiInDays").withDefaultValue(30));
		add(DEFAULT_START_TAB = others.createString("defaultStartTab").withDefaultValue("taxonomies"));
		add(DEFAULT_TAXONOMY = others.createString("defaultTaxonomy"));
		add(TAXONOMY_ORDER_IN_HOME_VIEW = others.createString("taxonomyOrderInHomeView"));

		add(LAZY_TREE_BUFFER_SIZE = others.createInteger("lazyTreeBufferSize").withDefaultValue(50)
				.scriptedBy(LazyTreeBufferSizeValidationScript.class));

		add(AUTOCOMPLETE_SIZE = others.createInteger("autocompleteSize").withDefaultValue(15)
				.scriptedBy(AutocompleteSizeValidationScript.class));

		SystemConfigurationGroup search = new SystemConfigurationGroup(null, "search");
		add(SEARCH_SORT_TYPE = search.createEnum("sortType", SearchSortType.class).withDefaultValue(SearchSortType.RELEVENCE));
		add(REPLACE_SPACES_IN_SIMPLE_SEARCH_FOR_ANDS = search.createBooleanFalseByDefault("replaceSpacesInSimpleSearchForAnds"));
		add(IS_RUNNING_WITH_SOLR_6 = search.createBooleanFalseByDefault("isRunningWithSolr6").whichIsHidden());
		add(SHOW_RESULTS_NUMBERING_IN_LIST_VIEW = search.createBooleanFalseByDefault("showResultsNumberingInListView"));
		add(SHOW_PATH_TO_RESULT = search.createBooleanFalseByDefault("showPathToResult"));

		add(AUTOCOMPLETE_SPLIT_CRITERIA = search.createEnum("autocompleteSplitCriteria", AutocompleteSplitCriteria.class)
				.withDefaultValue(AutocompleteSplitCriteria.SPACE).withReIndexationRequired());

		add(MAX_SELECTABLE_SEARCH_RESULTS = advanced.createInteger("maxSelectableSearchResults").withDefaultValue(1000));
		add(WRITE_ZZRECORDS_IN_TLOG = advanced.createBooleanFalseByDefault("writeZZRecordsInTlog")
				.scriptedBy(WriteZZRecordsScript.class).whichIsHidden());
		add(CMIS_NEVER_RETURN_ACL = advanced.createBooleanTrueByDefault("cmisNeverReturnACL"));

		add(REMOVE_EXTENSION_FROM_RECORD_TITLE = advanced.createBooleanFalseByDefault("removeExtensionFromDocument"));

		add(TABLE_DYNAMIC_CONFIGURATION = advanced.createBooleanTrueByDefault("tableDynamicConfiguration"));

		add(ADD_SECONDARY_SORT_WHEN_SORTING_BY_SCORE = search.createBooleanTrueByDefault("addSecondarySortWhenSortingByScore")
				.whichIsHidden());

		add(ENABLE_INACTIF_SCHEMAS_IN_SEARCH = search.createBooleanTrueByDefault("enableInactifSchemasInSearch"));

		add(ENABLE_LEARN_TO_RANK_FEATURE = search.createBooleanFalseByDefault("enableLearnToRankFeature")
				.whichIsHidden());

		SystemConfigurationGroup icapConfigurationGroup = new SystemConfigurationGroup(null, "icapScan");
		add(ICAP_SCAN_ACTIVATED = icapConfigurationGroup.createBooleanFalseByDefault("icapScanActivated"));
		add(ICAP_SERVER_URL = icapConfigurationGroup.createString("icapServerUrl"));
		add(ICAP_RESPONSE_TIMEOUT = icapConfigurationGroup.createInteger("icapResponseTimeout").withDefaultValue(5000));

		add(CKEDITOR_TOOLBAR_CONFIG = others.createString("ckeditorToolbarConfig")
				.withDefaultValue(DEFAULT_CKEDITOR_TOOLBAR_CONFIG));

		add(GROUP_AUTHORIZATIONS_INHERITANCE = others
				.createEnum("groupAuthorizationsInheritance", GroupAuthorizationsInheritance.class)
				.withDefaultValue(GroupAuthorizationsInheritance.FROM_PARENT_TO_CHILD)
				.scriptedBy(GroupAuthorizationsInheritanceScript.class));

		add(TRANSACTION_DELAY = others.createInteger("transactionDelay").withDefaultValue(3));
		//add(DEFAULT_FONT_SIZE = others.createInteger("defaultFontSize").withDefaultValue(16));
		//
		add(LAST_BACKUP_DAY = others.createString("lastBackupDay").whichIsHidden());

		add(KEEP_EVENTS_FOR_X_MONTH = others.createInteger("eventKeptPeriod").withDefaultValue(99999).whichIsHidden());

		add(IS_TRASH_THREAD_EXECUTING = others.createBooleanTrueByDefault("eventKeptPeriod").whichIsHidden());

		SystemConfigurationGroup trees = new SystemConfigurationGroup(null, "trees");

		add(SHOW_TRIANGLE_ONLY_WHEN_FOLDER_HAS_CONTENT = trees
				.createBooleanFalseByDefault("showTriangleOnlyWhenFolderHasContent"));

		add(MEMORY_CONSUMPTION_LEVEL = advanced.createEnum("memoryConsumptionLevel", MemoryConsumptionLevel.class)
				.withDefaultValue(MemoryConsumptionLevel.NORMAL).whichRequiresReboot().whichIsHidden());

		add(CONTENT_PARSING_SCHEDULE = advanced.createString("contentParsingSchedule")
				.scriptedBy(TimeScheduleConfigurationValidator.class).whichIsHidden());

		add(VIEWER_CONTENTS_CONVERSION_SCHEDULE = advanced.createString("viewerConversionSchedule")
				.scriptedBy(TimeScheduleConfigurationValidator.class).whichIsHidden());

		add(UNREFERENCED_CONTENTS_DELETE_SCHEDULE = advanced.createString("unreferencedContentsDeleteSchedule")
				.withDefaultValue("18-06").scriptedBy(TimeScheduleConfigurationValidator.class).whichIsHidden());

		add(BATCH_PROCESSES_SCHEDULE = advanced.createString("batchProcessesSchedule")
				.scriptedBy(TimeScheduleConfigurationValidator.class).whichIsHidden());

		add(SEIZE_MULTILANGUAL_VALUES = advanced.createBooleanFalseByDefault("seizeMultiLangual"));


		SystemConfigurationGroup reports = new SystemConfigurationGroup(null, "reports");

		add(ENABLE_STATISTIC_REPORT = reports.createBooleanTrueByDefault("enableStatisticReport"));
		add(REMOVE_TAB_AND_NEW_LINE_ON_DELTA_FIELD_IN_EDIT_REPORT = reports
				.createBooleanTrueByDefault("removeTabAndNewLineOnDeltaFieldInEditReport"));

		add(ARE_ALL_MULTI_LANGUAL_VALUES_MANDATORY = advanced.createBooleanFalseByDefault("areMultiLangualValuesMandatory"));

		add(ENABLE_ESSENTIAL_METADATA_HIDING = advanced.createBooleanFalseByDefault("enableEssentialMetadataHiding").whichIsHidden());

		add(ENABLE_ADMIN_USER_PASSWORD_CHANGE = others.createBooleanTrueByDefault("enableAdminUserPasswordChange")
				.whichIsHidden());

		add(BATCH_PROCESSES_MAXIMUM_HISTORY_SIZE = advanced.createInteger("batchProcessMaximumHistorySize")
				.withDefaultValue(20).whichIsHidden());


		add(SPACE_QUOTA_FOR_USER_DOCUMENTS = others.createInteger("spaceQuotaForUserDocuments").withDefaultValue(-1));

		add(ADD_RECORD_ID_IN_EMAILS = others.createBooleanFalseByDefault("addRecordIdInEmails"));
		add(GENERATED_EMAIL_FORMAT = others.createEnum("generatedEmailFormat", EmailTextFormat.class).withDefaultValue(EmailTextFormat.PLAIN_TEXT));

		add(ENABLE_THUMBNAIL_GENERATION = others.createBooleanTrueByDefault("enableThumbnailGeneration")
				.scriptedBy(EnableThumbnailsScript.class));
		add(ADD_COMMENTS_WHEN_READ_AUTHORIZATION = others.createBooleanTrueByDefault("addCommentsWhenReadAuthorization"));

		add(UPDATE_SERVER_CONNECTION_ENABLED = advanced.createBooleanTrueByDefault("updateServerConnectionEnabled").whichIsHidden());

		add(NO_LINKS_IN_SEARCH_RESULTS = search.createBooleanFalseByDefault("noLinksInSearchResults"));
		add(LAZY_LOADED_SEARCH_RESULTS = search.createBooleanTrueByDefault("lazyLoadedSearchResults"));
		add(SEARCH_RESULTS_HIGHLIGHTING_ENABLED = search.createBooleanTrueByDefault("searchResultsHighlightingEnabled").whichIsHidden());

		configurations = Collections.unmodifiableList(modifiableConfigs);

		SystemConfigurationGroup systemState = new SystemConfigurationGroup(null, "systemState");
		add(ENABLE_SYSTEM_STATE_LICENSE = systemState.createBooleanTrueByDefault("enableSystemStateLicense"));
		add(ENABLE_SYSTEM_STATE_MEMORY_ALLOCATION = systemState.createBooleanTrueByDefault("enableSystemStateMemoryAllocation"));
		add(ENABLE_SYSTEM_STATE_OPT_DISK_USAGE = systemState.createBooleanTrueByDefault("enableSystemStateOptDiskUsage"));
		add(ENABLE_SYSTEM_STATE_SOLR_DISK_USAGE = systemState.createBooleanTrueByDefault("enableSystemStateSolrDiskUsage"));

		add(DISPLAY_ONLY_SUMMARY_METADATAS_IN_TABLES = search.createBooleanFalseByDefault("displayOnlySummaryMetadatasInTables"));

		add(SEARCH_USING_EDISMAX = search.createBooleanTrueByDefault("searchUsingEDismax").whichIsHidden());
		add(SEARCH_USING_TERMS_IN_BQ = search.createBooleanTrueByDefault("searchUsingBQ").whichIsHidden());

		add(ENABLE_FACETS_APPLY_BUTTON = search.createBooleanFalseByDefault("applyMultipleFacets"));
	}

	static void add(SystemConfiguration configuration) {
		modifiableConfigs.add(configuration);
	}

	SystemConfigurationsManager manager;

	public ConstellioEIMConfigs(SystemConfigurationsManager manager) {
		this.manager = manager;
	}

	public ConstellioEIMConfigs(ModelLayerFactory modelLayerFactory) {
		this.manager = modelLayerFactory.getSystemConfigurationsManager();
	}

	public MetadataPopulatePriority getMetadataPopulatePriority() {
		return manager.getValue(METADATA_POPULATE_PRIORITY);
	}

	public TitleMetadataPopulatePriority getTitleMetadataPopulatePriority() {
		return manager.getValue(TITLE_METADATA_POPULATE_PRIORITY);
	}

	public String getUserTitlePattern() {
		return manager.getValue(USER_TITLE_PATTERN);
	}


	public boolean seeUserRolesInAuthorizations() {
		return manager.getValue(USER_ROLES_IN_AUTHORIZATIONS);
	}

	public String getConstellioUrl() {
		return manager.getValue(CONSTELLIO_URL);
	}

	public Boolean isWriteZZRecordsInTlog() {
		return manager.getValue(WRITE_ZZRECORDS_IN_TLOG);
	}

	public Boolean isTableDynamicConfiguration() {
		return manager.getValue(TABLE_DYNAMIC_CONFIGURATION);
	}

	public Boolean areInactifSchemasEnabledInSearch() {
		return manager.getValue(ENABLE_INACTIF_SCHEMAS_IN_SEARCH);
	}

	public Boolean isCleanDuringInstall() {
		return manager.getValue(CLEAN_DURING_INSTALL);
	}

	public Boolean isInUpdateProcess() {
		return manager.getValue(IN_UPDATE_PROCESS);
	}

	public String getDateFormat() {
		return manager.getValue(DATE_FORMAT);
	}

	public String getDateTimeFormat() {
		return manager.getValue(DATE_TIME_FORMAT);
	}

	public Integer getTrashPurgeDelai() {
		return manager.getValue(TRASH_PURGE_DELAI);
	}

	public Integer getBatchProcessingLimit() {
		return manager.getValue(BATCH_PROCESSING_LIMIT);
	}

	public SearchSortType getSearchSortType() {
		return manager.getValue(SEARCH_SORT_TYPE);
	}

	public Boolean isCmisNeverReturnAcl() {
		return manager.getValue(CMIS_NEVER_RETURN_ACL);
	}

	public Boolean isRemoveExtensionFromRecordTitle() {
		return manager.getValue(REMOVE_EXTENSION_FROM_RECORD_TITLE);
	}

	public boolean isShowPathToResult() {
		return manager.getValue(SHOW_PATH_TO_RESULT);
	}

	public boolean isSearchUsingTermsInBQ() {
		return manager.getValue(SEARCH_USING_TERMS_IN_BQ);
	}

	public boolean isSearchUsingEDismax() {
		return manager.getValue(SEARCH_USING_EDISMAX);
	}

	public boolean isShowResultsNumberingInListView() {
		return manager.getValue(SHOW_RESULTS_NUMBERING_IN_LIST_VIEW);
	}

	public ParsingBehavior getDefaultParsingBehavior() {
		return manager.getValue(DEFAULT_PARSING_BEHAVIOR);
	}

	public static Collection<? extends SystemConfiguration> getCoreConfigs() {
		return configurations;
	}

	public GroupAuthorizationsInheritance getGroupAuthorizationsInheritance() {
		return manager.getValue(GROUP_AUTHORIZATIONS_INHERITANCE);
	}

	public boolean isIncludingFromFieldWhenGeneratingEmails() {
		return !Boolean.FALSE.equals(manager.getValue(INCLUDE_FROM_FIELD_WHEN_GENERATING_EMAILS));
	}

	public static class WriteZZRecordsScript extends AbstractSystemConfigurationScript<Boolean> {

		@Override
		public void onValueChanged(Boolean previousValue, Boolean newValue, ModelLayerFactory modelLayerFactory) {
			modelLayerFactory.getDataLayerFactory().getDataLayerConfiguration().setWriteZZRecords(newValue);
		}

	}

	public static class LazyTreeBufferSizeValidationScript extends AbstractSystemConfigurationScript<Integer> {

		@Override
		public void validate(Integer newValue, ValidationErrors errors) {
			int max = 100;
			if (newValue < 0 || newValue > 100) {
				errors.add(LazyTreeBufferSizeValidationScript.class, "invalidLazyTreeBufferSize", max(max));
			}
		}

		private Map<String, Object> max(int max) {
			Map<String, Object> parameters = new HashMap<>();
			parameters.put("maxValue", max);
			return parameters;
		}

	}

	public static class AutocompleteSizeValidationScript extends AbstractSystemConfigurationScript<Integer> {

		@Override
		public void validate(Integer newValue, ValidationErrors errors) {
			int max = 100;
			if (newValue < 0 || newValue > 100) {
				errors.add(LazyTreeBufferSizeValidationScript.class, "invalidAutocompleteSize", max(max));
			}
		}

		private Map<String, Object> max(int max) {
			Map<String, Object> parameters = new HashMap<>();
			parameters.put("maxValue", max);
			return parameters;
		}

	}

	public boolean getIcapScanActivated() {
		return manager.getValue(ICAP_SCAN_ACTIVATED);
	}

	public String getIcapServerUrl() {
		return manager.getValue(ICAP_SERVER_URL);
	}

	public int getIcapResponseTimeout() {
		return manager.getValue(ICAP_RESPONSE_TIMEOUT);
	}

	public String getCKEditorToolbarConfig() {
		return manager.getValue(CKEDITOR_TOOLBAR_CONFIG);
	}

	public String getDefaultStartTab() {
		return manager.getValue(DEFAULT_START_TAB);
	}

	public String getDefaultTaxonomy() {
		return manager.getValue(DEFAULT_TAXONOMY);
	}

	public int getTransactionDelay() {
		return manager.getValue(TRANSACTION_DELAY);
	}

	public int getLazyTreeBufferSize() {
		return manager.getValue(LAZY_TREE_BUFFER_SIZE);
	}

	public int getAutocompleteSize() {
		return manager.getValue(AUTOCOMPLETE_SIZE);
	}

	public AutocompleteSplitCriteria getAutocompleteSplitCriteria() {
		return manager.getValue(AUTOCOMPLETE_SPLIT_CRITERIA);
	}

	public boolean isIncludeContentsInSavestate() {
		return manager.getValue(INCLUDE_CONTENTS_IN_SAVESTATE);
	}

	public boolean isReplaceSpacesInSimpleSearchForAnds() {
		return manager.getValue(REPLACE_SPACES_IN_SIMPLE_SEARCH_FOR_ANDS);
	}

	public boolean isRunningWithSolr6() {
		return manager.getValue(IS_RUNNING_WITH_SOLR_6);
	}

	public String getTaxonomyOrderInHomeView() {
		return manager.getValue(TAXONOMY_ORDER_IN_HOME_VIEW);
	}
	//public int getDefaultFontSize() { return manager.getValue(DEFAULT_FONT_SIZE); }

	public MemoryConsumptionLevel getMemoryConsumptionLevel() {
		return manager.getValue(MEMORY_CONSUMPTION_LEVEL);
	}

	public boolean isInContentParsingSchedule() {
		return isCurrentlyInSchedule(manager.<String>getValue(CONTENT_PARSING_SCHEDULE));
	}

	public boolean isInUnreferencedContentsDeleteSchedule() {
		return isCurrentlyInSchedule(manager.<String>getValue(UNREFERENCED_CONTENTS_DELETE_SCHEDULE));
	}

	public boolean isInViewerContentsConversionSchedule() {
		return isCurrentlyInSchedule(manager.<String>getValue(VIEWER_CONTENTS_CONVERSION_SCHEDULE));
	}

	public boolean isInScanVaultContentsSchedule() {
		return TimeProvider.getLocalDate().getDayOfWeek() >= 6;
	}

	public boolean isInBatchProcessesSchedule() {
		return isCurrentlyInSchedule(manager.<String>getValue(BATCH_PROCESSES_SCHEDULE));
	}

	public boolean isStatisticReportEnabled() {
		return manager.getValue(ENABLE_STATISTIC_REPORT);
	}

	public boolean isAddingSecondarySortWhenSortingByScore() {
		return manager.getValue(ADD_SECONDARY_SORT_WHEN_SORTING_BY_SCORE);
	}

	public boolean isAdminPasswordChangeEnabled() {
		return manager.getValue(ENABLE_ADMIN_USER_PASSWORD_CHANGE);
	}

	public int getSpaceQuotaForUserDocuments() {
		return manager.getValue(SPACE_QUOTA_FOR_USER_DOCUMENTS);
	}

	public int getBatchProcessMaximumHistorySize() {
		return manager.getValue(BATCH_PROCESSES_MAXIMUM_HISTORY_SIZE);
	}

	public boolean isAddingRecordIdInEmails() {
		return manager.getValue(ADD_RECORD_ID_IN_EMAILS);
	}

	public boolean isThumbnailGenerationEnabled() {
		return manager.getValue(ENABLE_THUMBNAIL_GENERATION);
	}

	public EmailTextFormat getGeneratedEmailFormat() {
		return manager.getValue(GENERATED_EMAIL_FORMAT);
	}

	public Set<String> getFileExtensionsExcludedFromParsing() {
		String extensionsAsString = manager.getValue(FILE_EXTENSIONS_EXCLUDED_FROM_PARSING);
		Set<String> extensionSet = new HashSet<>();
		if (!StringUtils.isBlank(extensionsAsString)) {
			String[] splittedExtensions = extensionsAsString.split(",");
			for (String currentExtension : splittedExtensions) {
				String formattedExtension = currentExtension.trim().toLowerCase();
				if (formattedExtension.startsWith(".")) {
					extensionSet.add(formattedExtension.substring(1));
				} else {
					extensionSet.add(formattedExtension);
				}
			}
		}
		return extensionSet;
	}

	public boolean isSystemStateLicenseValidationEnabled() {
		return manager.getValue(ENABLE_SYSTEM_STATE_LICENSE);
	}

	public boolean isSystemStateMemoryAllocationValidationEnabled() {
		return manager.getValue(ENABLE_SYSTEM_STATE_MEMORY_ALLOCATION);
	}

	public boolean isSystemStateOptDiskUsageValidationEnabled() {
		return manager.getValue(ENABLE_SYSTEM_STATE_OPT_DISK_USAGE);
	}

	public boolean isSystemStateSolrDiskUsageValidationEnabled() {
		return manager.getValue(ENABLE_SYSTEM_STATE_SOLR_DISK_USAGE);
	}

	public boolean isApplyMultipleFacetButtonEnabled() {
		return manager.getValue(ENABLE_FACETS_APPLY_BUTTON);
	}

	public boolean isUpdateServerConnectionEnabled() {
		return manager.getValue(UPDATE_SERVER_CONNECTION_ENABLED);
	}

	public boolean isNoLinksInSearchResults() {
		return manager.getValue(NO_LINKS_IN_SEARCH_RESULTS);
	}

	public boolean isLazyLoadedSearchResults() {
		return manager != null && manager.<Boolean>getValue(LAZY_LOADED_SEARCH_RESULTS);
	}

	public boolean isLearnToRankFeatureActivated() {
		return manager.getValue(ENABLE_LEARN_TO_RANK_FEATURE);
	}

	public boolean isAddCommentsWhenReadAuthorization() {
		return manager.getValue(ADD_COMMENTS_WHEN_READ_AUTHORIZATION);
	}

	public boolean isOnlySummaryMetadatasDisplayedInTables() {
		return manager.getValue(DISPLAY_ONLY_SUMMARY_METADATAS_IN_TABLES);
	}

	public boolean isSearchResultsHighlightingEnabled() {
		return manager.getValue(SEARCH_RESULTS_HIGHLIGHTING_ENABLED);
	}

	public boolean isLegacyIdentifierIndexedInMemory() {
		return manager.getValue(LEGACY_IDENTIFIER_INDEXED_IN_MEMORY);
	}

}
