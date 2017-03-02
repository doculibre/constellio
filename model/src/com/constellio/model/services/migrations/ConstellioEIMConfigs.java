package com.constellio.model.services.migrations;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import com.constellio.model.entities.configs.AbstractSystemConfigurationScript;
import com.constellio.model.entities.configs.SystemConfiguration;
import com.constellio.model.entities.configs.SystemConfigurationGroup;
import com.constellio.model.entities.configs.core.listeners.UserTitlePatternConfigScript;
import com.constellio.model.entities.enums.BatchProcessingMode;
import com.constellio.model.entities.enums.GroupAuthorizationsInheritance;
import com.constellio.model.entities.enums.MetadataPopulatePriority;
import com.constellio.model.entities.enums.SearchSortType;
import com.constellio.model.entities.enums.TitleMetadataPopulatePriority;
import com.constellio.model.services.configs.SystemConfigurationsManager;
import com.constellio.model.services.factories.ModelLayerFactory;

public class ConstellioEIMConfigs {

	private static List<SystemConfiguration> modifiableConfigs = new ArrayList<>();
	public static List<SystemConfiguration> configurations;

	//Retention calendar configs
	public static final SystemConfiguration USER_TITLE_PATTERN;

	public static final SystemConfiguration USER_ROLES_IN_AUTHORIZATIONS;
	public static final SystemConfiguration PARSED_CONTENT_MAX_LENGTH_IN_KILOOCTETS;
	public static final SystemConfiguration CONTENT_MAX_LENGTH_FOR_PARSING_IN_MEGAOCTETS;

	public static final SystemConfiguration METADATA_POPULATE_PRIORITY, TITLE_METADATA_POPULATE_PRIORITY;
	public static final SystemConfiguration LOGO;
	public static final SystemConfiguration LOGO_LINK;
	public static final SystemConfiguration CONSTELLIO_URL;
	public static final SystemConfiguration CLEAN_DURING_INSTALL;
	public static final SystemConfiguration IN_UPDATE_PROCESS;
	public static final SystemConfiguration BATCH_PROCESSING_MODE;

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

	public static final String DEFAULT_CKEDITOR_TOOLBAR_CONFIG = "" +
			"   { name: 'document', items: [ 'Source', 'NewPage', 'Preview', 'Print' ] },\n" +
			"	{ name: 'clipboard', items: [ 'Cut', 'Copy', 'Paste', 'PasteText', 'PasteFromWord', '-', 'Undo', 'Redo' ] },\n" +
			"	{ name: 'editing', items: [ 'Find', 'Replace', '-', 'SelectAll', '-' ] },\n" +
			"	'/',\n" +
			"	{ name: 'basicstyles', items: [ 'Bold', 'Italic', 'Underline', 'Strike', 'Subscript', 'Superscript', '-', 'RemoveFormat' ] },\n"
			+
			"	{ name: 'paragraph', items: [ 'NumberedList', 'BulletedList', '-', 'Outdent', 'Indent', '-', 'Blockquote', 'CreateDiv', '-', 'JustifyLeft', 'JustifyCenter', 'JustifyRight', 'JustifyBlock', '-', 'BidiLtr', 'BidiRtl'] },\n"
			+
			"	{ name: 'links', items: [ 'Link', 'Unlink', 'Anchor' ] },\n" +
			"	{ name: 'insert', items: [ 'Image', 'Table', 'HorizontalRule', 'SpecialChar', 'PageBreak' ] },\n" +
			"	'/',\n" +
			"	{ name: 'styles', items: [ 'Styles', 'Format', 'Font', 'FontSize' ] },\n" +
			"	{ name: 'colors', items: [ 'TextColor', 'BGColor' ] },\n" +
			"	{ name: 'tools', items: [ 'Maximize', 'ShowBlocks' ] }";

	public static final SystemConfiguration DEFAULT_START_TAB;

	public static final SystemConfiguration DEFAULT_TAXONOMY;

	static {
		SystemConfigurationGroup others = new SystemConfigurationGroup(null, "others");
		add(USER_TITLE_PATTERN = others.createString("userTitlePattern").scriptedBy(UserTitlePatternConfigScript.class)
				.withDefaultValue("${firstName} ${lastName}"));

		// Associer ou non des rôles utilisateur aux autorisations
		add(USER_ROLES_IN_AUTHORIZATIONS = others.createBooleanFalseByDefault("userRolesInAuthorizations"));

		add(LOGO = others.createBinary("logo"));
		add(LOGO_LINK = others.createString("logoLink", "http://www.constellio.com"));
		add(METADATA_POPULATE_PRIORITY = others.createEnum("metadataPopulatePriority", MetadataPopulatePriority.class)
				.withDefaultValue(MetadataPopulatePriority.STYLES_REGEX_PROPERTIES));
		add(TITLE_METADATA_POPULATE_PRIORITY = others
				.createEnum("titleMetadataPopulatePriority", TitleMetadataPopulatePriority.class)
				.withDefaultValue(TitleMetadataPopulatePriority.STYLES_FILENAME_PROPERTIES));
		add(CONSTELLIO_URL = others.createString("constellioUrl", "http://localhost:8080/constellio/"));

		add(DATE_FORMAT = others.createString("dateFormat").withDefaultValue("yyyy-MM-dd"));
		add(DATE_TIME_FORMAT = others.createString("dateTimeFormat").withDefaultValue("yyyy-MM-dd HH:mm:ss"));

		SystemConfigurationGroup advanced = new SystemConfigurationGroup(null, "advanced");
		add(PARSED_CONTENT_MAX_LENGTH_IN_KILOOCTETS = advanced.createInteger("parsedContentMaxLengthInKilooctets")
				.withDefaultValue(3000));
		add(CONTENT_MAX_LENGTH_FOR_PARSING_IN_MEGAOCTETS = advanced.createInteger("contentMaxLengthForParsingInMegaoctets")
				.withDefaultValue(30));

		add(CLEAN_DURING_INSTALL = advanced.createBooleanFalseByDefault("cleanDuringInstall"));

		SystemConfigurationGroup hiddenSystemConfigs = new SystemConfigurationGroup(null, "system");
		add(IN_UPDATE_PROCESS = hiddenSystemConfigs.createBooleanFalseByDefault("inUpdateProcess").whichIsHidden());
		add(BATCH_PROCESSING_MODE = others.createEnum("batchProcessingMode", BatchProcessingMode.class)
				.withDefaultValue(BatchProcessingMode.ALL_METADATA_OF_SCHEMA).whichIsHidden());
		add(TRASH_PURGE_DELAI = others.createInteger("trashPurgeDelaiInDays").withDefaultValue(30));
		add(DEFAULT_START_TAB = others.createString("defaultStartTab").withDefaultValue("taxonomies"));
		add(DEFAULT_TAXONOMY = others.createString("defaultTaxonomy"));

		SystemConfigurationGroup search = new SystemConfigurationGroup(null, "search");
		add(SEARCH_SORT_TYPE = search.createEnum("sortType", SearchSortType.class).withDefaultValue(SearchSortType.RELEVENCE));

		add(MAX_SELECTABLE_SEARCH_RESULTS = advanced.createInteger("maxSelectableSearchResults").withDefaultValue(500));
		add(WRITE_ZZRECORDS_IN_TLOG = advanced.createBooleanFalseByDefault("writeZZRecordsInTlog")
				.scriptedBy(WriteZZRecordsScript.class));
		add(CMIS_NEVER_RETURN_ACL = advanced.createBooleanTrueByDefault("cmisNeverReturnACL"));

		add(REMOVE_EXTENSION_FROM_RECORD_TITLE = advanced.createBooleanFalseByDefault("removeExtensionFromDocument"));

		//
		SystemConfigurationGroup icapConfigurationGroup = new SystemConfigurationGroup(null, "icapScan");
		add(ICAP_SCAN_ACTIVATED = icapConfigurationGroup.createBooleanFalseByDefault("icapScanActivated"));
		add(ICAP_SERVER_URL = icapConfigurationGroup.createString("icapServerUrl"));
		add(ICAP_RESPONSE_TIMEOUT = icapConfigurationGroup.createInteger("icapResponseTimeout").withDefaultValue(5000));

		add(CKEDITOR_TOOLBAR_CONFIG = others.createString("ckeditorToolbarConfig")
				.withDefaultValue(DEFAULT_CKEDITOR_TOOLBAR_CONFIG));

		add(GROUP_AUTHORIZATIONS_INHERITANCE = others
				.createEnum("groupAuthorizationsInheritance", GroupAuthorizationsInheritance.class)
				.withDefaultValue(GroupAuthorizationsInheritance.FROM_PARENT_TO_CHILD));

		//
		configurations = Collections.unmodifiableList(modifiableConfigs);
	}

	static void add(SystemConfiguration configuration) {
		modifiableConfigs.add(configuration);
	}

	SystemConfigurationsManager manager;

	public ConstellioEIMConfigs(SystemConfigurationsManager manager) {
		this.manager = manager;
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

	public BatchProcessingMode getBatchProcessingMode() {
		return manager.getValue(BATCH_PROCESSING_MODE);
	}

	public SearchSortType getSearchSortType() {
		return manager.getValue(SEARCH_SORT_TYPE);
	}

	public Boolean isCmisNeverReturnAcl() {
		return manager.getValue(CMIS_NEVER_RETURN_ACL);
	}

	public Boolean isRemoveExtensionFromRecordTitle(){
		return manager.getValue(REMOVE_EXTENSION_FROM_RECORD_TITLE);
	}

	public static Collection<? extends SystemConfiguration> getCoreConfigs() {
		return configurations;
	}

	public GroupAuthorizationsInheritance getGroupAuthorizationsInheritance() {
		return manager.getValue(GROUP_AUTHORIZATIONS_INHERITANCE);
	}

	public static class WriteZZRecordsScript extends AbstractSystemConfigurationScript<Boolean> {

		@Override
		public void onValueChanged(Boolean previousValue, Boolean newValue, ModelLayerFactory modelLayerFactory) {
			modelLayerFactory.getDataLayerFactory().getDataLayerConfiguration().setWriteZZRecords(newValue);
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

}
