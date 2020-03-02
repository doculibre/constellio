package com.constellio.app.ui.application;

import com.constellio.app.modules.rm.ui.pages.decommissioning.OrderDecommissioningListViewImpl;
import com.constellio.app.modules.rm.ui.pages.personalspace.PersonnalSpaceViewImpl;
import com.constellio.app.modules.rm.ui.pages.systemCheck.SystemCheckViewImpl;
import com.constellio.app.ui.pages.SIP.SIPProgressionViewImpl;
import com.constellio.app.ui.pages.base.BaseViewImpl;
import com.constellio.app.ui.pages.batchprocess.ListBatchProcessesViewImpl;
import com.constellio.app.ui.pages.collection.CollectionGroupRolesViewImpl;
import com.constellio.app.ui.pages.collection.CollectionGroupViewImpl;
import com.constellio.app.ui.pages.collection.CollectionUserRolesViewImpl;
import com.constellio.app.ui.pages.collection.CollectionUserViewImpl;
import com.constellio.app.ui.pages.collection.ListCollectionUserViewImpl;
import com.constellio.app.ui.pages.elevations.EditElevationViewImpl;
import com.constellio.app.ui.pages.events.BaseEventCategoryViewImpl;
import com.constellio.app.ui.pages.events.EventCategoriesViewImpl;
import com.constellio.app.ui.pages.events.EventViewImpl;
import com.constellio.app.ui.pages.globalGroup.AddEditGlobalGroupViewImpl;
import com.constellio.app.ui.pages.globalGroup.DisplayGlobalGroupViewImpl;
import com.constellio.app.ui.pages.globalGroup.ListGlobalGroupsViewImpl;
import com.constellio.app.ui.pages.home.HomeViewImpl;
import com.constellio.app.ui.pages.imports.ExportViewImpl;
import com.constellio.app.ui.pages.imports.ImportFileViewImpl;
import com.constellio.app.ui.pages.imports.ImportGroupsFileViewImpl;
import com.constellio.app.ui.pages.imports.ImportSchemaTypesFileViewImpl;
import com.constellio.app.ui.pages.imports.ImportUsersFileViewImpl;
import com.constellio.app.ui.pages.imports.ListImportExportViewImpl;
import com.constellio.app.ui.pages.imports.authorization.ImportAuthorizationsFileViewImpl;
import com.constellio.app.ui.pages.imports.settings.ImportSettingsViewImpl;
import com.constellio.app.ui.pages.management.AdminViewImpl;
import com.constellio.app.ui.pages.management.ExcelReport.DisplayExcelReportViewImpl;
import com.constellio.app.ui.pages.management.ExcelReport.ListExcelReportViewImpl;
import com.constellio.app.ui.pages.management.Report.AddEditPrintableReportViewImpl;
import com.constellio.app.ui.pages.management.Report.DisplayPrintableReportViewImpl;
import com.constellio.app.ui.pages.management.Report.ListPrintableReportViewImpl;
import com.constellio.app.ui.pages.management.TemporaryRecord.ListTemporaryRecordViewImpl;
import com.constellio.app.ui.pages.management.app.AppManagementView;
import com.constellio.app.ui.pages.management.authorizations.ListContentAccessAndRoleAuthorizationsViewImpl;
import com.constellio.app.ui.pages.management.authorizations.ListContentAccessAuthorizationsViewImpl;
import com.constellio.app.ui.pages.management.authorizations.ListContentRoleAuthorizationsViewImpl;
import com.constellio.app.ui.pages.management.authorizations.ListPrincipalAccessAuthorizationsViewImpl;
import com.constellio.app.ui.pages.management.authorizations.PublishDocumentViewImpl;
import com.constellio.app.ui.pages.management.authorizations.ShareContentListViewImpl;
import com.constellio.app.ui.pages.management.authorizations.ShareContentViewImpl;
import com.constellio.app.ui.pages.management.bagInfo.AddEditBagInfo.AddEditBagInfoViewImpl;
import com.constellio.app.ui.pages.management.bagInfo.DisplayBagInfo.DisplayBagInfoViewImpl;
import com.constellio.app.ui.pages.management.bagInfo.ListBagInfo.ListBagInfoViewImpl;
import com.constellio.app.ui.pages.management.capsule.addEdit.AddEditCapsuleViewImpl;
import com.constellio.app.ui.pages.management.capsule.display.DisplayCapsuleViewImpl;
import com.constellio.app.ui.pages.management.capsule.list.ListCapsuleViewImpl;
import com.constellio.app.ui.pages.management.collections.AddEditCollectionViewImpl;
import com.constellio.app.ui.pages.management.collections.CollectionManagementViewImpl;
import com.constellio.app.ui.pages.management.configs.ConfigManagementViewImpl;
import com.constellio.app.ui.pages.management.email.EmailServerConfigViewImpl;
import com.constellio.app.ui.pages.management.extractors.AddEditMetadataExtractorViewImpl;
import com.constellio.app.ui.pages.management.extractors.ListMetadataExtractorsViewImpl;
import com.constellio.app.ui.pages.management.extractors.PropertiesAnalyserViewImpl;
import com.constellio.app.ui.pages.management.facet.AddEditFacetConfigurationViewImpl;
import com.constellio.app.ui.pages.management.facet.DisplayFacetConfigurationViewImpl;
import com.constellio.app.ui.pages.management.facet.ListFacetConfigurationViewImpl;
import com.constellio.app.ui.pages.management.facet.OrderFacetConfigurationViewImpl;
import com.constellio.app.ui.pages.management.labels.AddEditLabelViewImpl;
import com.constellio.app.ui.pages.management.labels.LabelViewImpl;
import com.constellio.app.ui.pages.management.labels.ListLabelViewImpl;
import com.constellio.app.ui.pages.management.labels.ReportConfigViewImpl;
import com.constellio.app.ui.pages.management.ldap.LDAPConfigManagementViewImpl;
import com.constellio.app.ui.pages.management.permissions.PermissionsManagementViewImpl;
import com.constellio.app.ui.pages.management.plugin.PluginManagementViewImpl;
import com.constellio.app.ui.pages.management.reindexation.ForcedReindexViewImpl;
import com.constellio.app.ui.pages.management.schemaRecords.AddEditSchemaRecordViewImpl;
import com.constellio.app.ui.pages.management.schemaRecords.DisplaySchemaRecordViewImpl;
import com.constellio.app.ui.pages.management.schemaRecords.ListSchemaRecordsViewImpl;
import com.constellio.app.ui.pages.management.schemaRecords.SearchSchemaRecordsViewImpl;
import com.constellio.app.ui.pages.management.schemas.ListSchemaTypeViewImpl;
import com.constellio.app.ui.pages.management.schemas.display.display.DisplayConfigViewImpl;
import com.constellio.app.ui.pages.management.schemas.display.form.FormDisplayConfigViewImpl;
import com.constellio.app.ui.pages.management.schemas.display.group.ListMetadataGroupSchemaTypeViewImpl;
import com.constellio.app.ui.pages.management.schemas.display.report.ReportConfigurationViewImpl;
import com.constellio.app.ui.pages.management.schemas.display.search.SearchDisplayConfigViewImpl;
import com.constellio.app.ui.pages.management.schemas.display.table.TableDisplayConfigViewImpl;
import com.constellio.app.ui.pages.management.schemas.metadata.AddEditMetadataViewImpl;
import com.constellio.app.ui.pages.management.schemas.schema.AddEditSchemaMetadataViewImpl;
import com.constellio.app.ui.pages.management.schemas.schema.AddEditSchemaViewImpl;
import com.constellio.app.ui.pages.management.schemas.type.ListSchemaViewImpl;
import com.constellio.app.ui.pages.management.searchConfig.SearchConfigurationViewImpl;
import com.constellio.app.ui.pages.management.taxonomy.AddEditTaxonomyConceptViewImpl;
import com.constellio.app.ui.pages.management.taxonomy.AddEditTaxonomyViewImpl;
import com.constellio.app.ui.pages.management.taxonomy.ListTaxonomyViewImpl;
import com.constellio.app.ui.pages.management.taxonomy.TaxonomyManagementSearchViewImpl;
import com.constellio.app.ui.pages.management.taxonomy.TaxonomyManagementViewImpl;
import com.constellio.app.ui.pages.management.thesaurus.ThesaurusConfigurationViewImpl;
import com.constellio.app.ui.pages.management.updates.UpdateManagerViewImpl;
import com.constellio.app.ui.pages.management.valueDomains.ListValueDomainRecordsViewImpl;
import com.constellio.app.ui.pages.management.valueDomains.ListValueDomainViewImpl;
import com.constellio.app.ui.pages.profile.ModifyProfileViewImpl;
import com.constellio.app.ui.pages.search.AdvancedSearchViewImpl;
import com.constellio.app.ui.pages.search.SearchBoostByMetadataViewImpl;
import com.constellio.app.ui.pages.search.SearchBoostByQueryViewImpl;
import com.constellio.app.ui.pages.search.SimpleSearchViewImpl;
import com.constellio.app.ui.pages.search.SolrFeatureViewImpl;
import com.constellio.app.ui.pages.search.savedSearch.SavedSearchViewImpl;
import com.constellio.app.ui.pages.spellchecker.DeleteSpellCheckerExclusionsViewImpl;
import com.constellio.app.ui.pages.statistic.StatisticsViewImpl;
import com.constellio.app.ui.pages.summaryconfig.SummaryConfigViewImpl;
import com.constellio.app.ui.pages.synonyms.DisplaySynonymsViewImpl;
import com.constellio.app.ui.pages.synonyms.EditSynonymsViewImpl;
import com.constellio.app.ui.pages.trash.TrashViewImpl;
import com.constellio.app.ui.pages.unicitymetadataconf.FolderUniqueKeyConfiguratorViewImpl;
import com.constellio.app.ui.pages.user.AddEditUserCredentialViewImpl;
import com.constellio.app.ui.pages.user.DisplayUserCredentialViewImpl;
import com.constellio.app.ui.pages.user.ListUsersCredentialsViewImpl;
import com.vaadin.navigator.Navigator;
import com.vaadin.navigator.Navigator.ClassBasedViewProvider;
import com.vaadin.navigator.ViewProvider;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class NavigatorConfigurationService implements Serializable {
	public static final String HOME = "";
	public static final String APP_MANAGEMENT = "appManagement";
	public static final String SIMPLE_SEARCH = "search";
	public static final String ADVANCED_SEARCH = "advancedSearch";
	public static final String PERSONNAL_SPACE = "personalSpace";
	public static final String LIST_TAXONOMY = "taxonomies";
	public static final String TAXONOMY_ADD_EDIT = "taxonomyAddEdit";
	public static final String TAXONOMY_MANAGEMENT = "taxonomyManagement";
	public static final String TAXONOMY_CONCEPT_ADD_EDIT = "taxonomyConceptAddEdit";
	public static final String LIST_VALUE_DOMAINS = "valueDomains";
	public static final String SEARCH_SCHEMA_RECORDS = "searchSchemaRecords";
	public static final String GROUP_LIST = "groupList";
	public static final String GROUP_ADD_EDIT = "groupAddEdit";
	public static final String COLLECTION_ADD_EDIT = "collectionAddEdit";
	public static final String GROUP_DISPLAY = "groupDisplay";
	public static final String USER_LIST = "userList";
	public static final String USER_ADD_EDIT = "userAddEdit";
	public static final String EVENTS_LIST = "eventsList";
	public static final String EVENT_CATEGORY = "eventCategory";
	public static final String EVENT_DISPLAY = "eventDisplay";
	public static final String ADD_EDIT_SCHEMA_RECORD = "addSchemaRecord";
	public static final String DISPLAY_SCHEMA_RECORD = "displaySchemaRecord";
	public static final String LIST_SCHEMA_RECORDS = "listSchemaRecords";
	public static final String LIST_VALUE_LIST_RECORDS = "listValueListRecords";
	public static final String USER_DISPLAY = "userDisplay";
	public static final String LIST_PRINCIPAL_ACCESS_AUTHORIZATIONS = "principalAccessAuthorizations";
	public static final String LIST_OBJECT_ACCESS_AUTHORIZATIONS = "objectAccessAuthorizations";
	public static final String LIST_OBJECT_ROLE_AUTHORIZATIONS = "objectRoleAuthorizations";
	public static final String LIST_OBJECT_ACCESS_AND_ROLE_AUTHORIZATIONS = "objectAccessAndRoleAuthorizations";
	public static final String SHARE_CONTENT = "shareContent";
	public static final String MODIFY_SHARE = "modifyShare";
	public static final String UNSHARE_CONTENT = "unshareContent";
	public static final String ADMIN_MODULE = "adminModule";
	public static final String TRASH = "trash";
	public static final String COLLECTION_USER_LIST = "collectionSecurity";
	public static final String COLLECTION_USER = "collectionUser";
	public static final String COLLECTION_USER_ROLES = "collectionUserRoles";
	public static final String COLLECTION_GROUP = "collectionGroup";
	public static final String COLLECTION_GROUP_ROLES = "collectionGroupRoles";
	public static final String CONFIG_MANAGEMENT = "configManagement";
	public static final String PLUGIN_MANAGEMENT = "pluginManagement";
	public static final String EMAIL_SERVER_MANAGEMENT = "emailServerManagement";
	public static final String DISPLAY_SCHEMA_TYPE = "displaySchemaType";
	public static final String DISPLAY_SCHEMA = "displaySchema";
	public static final String ADD_EDIT_SCHEMA = "editSchema";
	public static final String ADD_EDIT_METADATA = "editMetadata";
	public static final String ADD_EDIT_SCHEMA_METADATA = "editSchemaMetadata";
	public static final String LIST_METADATA_GROUP = "listMetadataGroup";
	public static final String RECORDS_MANAGEMENT = "recordsManagement";
	public static final String EDIT_DISPLAY_FORM = "displayForm";
	public static final String SEARCH_DISPLAY_FORM = "searchDisplayForm";
	public static final String TABLE_DISPLAY_FORM = "tableDisplayForm";
	public static final String FORM_DISPLAY_FORM = "formDisplayForm";
	public static final String PERMISSION_MANAGEMENT = "permissionManagement";
	public static final String SHARE_MANAGEMENT = "shareManagement";
	public static final String COLLECTION_MANAGEMENT = "listCollections";
	public static final String LIST_ONGLET = "listOnglet";
	public static final String MODIFY_PROFILE = "modifyProfile";
	public static final String UPDATE_MANAGER = "updateManager";
	public static final String LDAP_CONFIG_MANAGEMENT = "ldapConfigManagement";
	public static final String IMPORT_FILE = "importRecords";
	public static final String IMPORT_USERS = "importUsers";
	public static final String IMPORT_AUTHORIZATIONS = "importAuthorizations";
	public static final String IMPORT_GROUPS = "importGroups";
	public static final String IMPORT_SCHEMA_TYPES = "importSchemaTypes";
	public static final String IMPORT_SETTINGS = "importSettings";
	public static final String EXPORTER = "export";
	public static final String REPORT_DISPLAY_FORM = "reportDisplayForm";
	public static final String LIST_SAVED_SEARCHES = "listSavedSearches";
	public static final String SEARCH_BOOST_BY_METADATAS = "searchBoostByMetadatas";
	public static final String SEARCH_BOOST_BY_QUERYS = "searchBoostByQuery";
	public static final String SOLR_FEATURES = "solrFeatures";
	public static final String FORCED_REINDEX = "reindex";
	public static final String LABEL_CONFIG = "labelConfig";
	public static final String LABEL_AJOUT = "addLabel";
	public static final String LABEL_VIEW = "viewLabel";
	public static final String REPORT_VIEW = "viewReport";
	public static final String PRINTABLE_REPORT_VIEW = "printableReportView";
	public static final String PRINTABLE_REPORT_ADD = "pritnableReportAdd";
	public static final String PRINTABLE_REPORT_MANAGE = "printableReportManage";
	public static final String PUBLISH_DOCUMENT ="publishDocument";
	public static final String EXCEL_REPORT_MANAGE = "excelReportManage";
	public static final String EXCEL_REPORT_DISPLAY = "excelReportDisplay";

	public static final String LIST_BAG_INFO = "listBagInfo";
	public static final String DISPLAY_BAG_INFO = "displayBagInfo";
	public static final String ADD_EDIT_BAG_INFO = "addEditBagInfo";
	public static final String SIP_PROGRESSION = "sipProgression";

	public static final String ADD_FACET_CONFIGURATION = "addFacetConfiguration";
	public static final String EDIT_FACET_CONFIGURATION = "editFacetConfiguration";
	public static final String LIST_FACET_CONFIGURATION = "listFacetConfiguration";
	public static final String DISPLAY_FACET_CONFIGURATION = "displayFacetConfiguration";
	public static final String ORDER_FACET_CONFIGURATION = "orderFacetConfiguration";

	public static final String ORDER_DECOMMISSIONING_LIST_CONFIGURATION = "orderDecommissioningList";

	public static final String LIST_METADATA_EXTRACTORS = "listMetadataExtractors";
	public static final String ADD_METADATA_EXTRACTOR = "addMetadataExtractor";
	public static final String EDIT_METADATA_EXTRACTOR = "editMetadataExtractor";

	public static final String TAXONOMY_SEARCH = "taxonomySearch";

	public static final String SYSTEM_CHECK = "systemCheck";
	public static final String LIST_BATCH_PROCESSES = "batchProcesses";
	public static final String LIST_IMPORT_EXPORT = "importExport";
	public static final String LIST_TEMPORARY_RECORDS = "listTemporaryRecords";
	public static final String SEARCH_CONFIGURATION = "searchConfiguration";

	public static final String LIST_CAPSULE = "listCapsule";
	public static final String ADD_EDIT_CAPSULE = "addEditCapsule";
	public static final String DISPLAY_CAPSULE = "displayCapsule";

	public static final String EDIT_SYNONYMS = "editSynonyms";
	public static final String DISPLAY_SYNONYMS = "displaysynonyms";

	public static final String EDIT_ELEVATION = "editElevation";
	public static final String STATISTICS = "statistics";
	public static final String DELETE_EXCLUSION = "correctorExclusion";
	public static final String THESAURUS_CONFIGURATION = "thesaurusConfiguration";
	public static final String PROPERTIES_ANALYSER = "propertiesAnalyser";
	public static final String SUMMARY_CONFIGURATOR = "summaryconfig";
	public static final String FOLDER_UNIQUE_KEY_METADATA_CONFIGURATOR = "FolderUniqueKeyMetadataConfigurator";
	public static final String DISPLAY_INFOS = "displayInfos";

	private List<ViewProvider> viewProviders = new ArrayList<>();

	public NavigatorConfigurationService() {
		viewProviders.add(new ClassBasedViewProvider(SIMPLE_SEARCH, SimpleSearchViewImpl.class));
		viewProviders.add(new ClassBasedViewProvider(ADVANCED_SEARCH, AdvancedSearchViewImpl.class));
		viewProviders.add(new ClassBasedViewProvider(HOME, HomeViewImpl.class));
		viewProviders.add(new ClassBasedViewProvider(PERSONNAL_SPACE, PersonnalSpaceViewImpl.class));
		viewProviders.add(new ClassBasedViewProvider(APP_MANAGEMENT, AppManagementView.class));
		viewProviders.add(new ClassBasedViewProvider(TAXONOMY_MANAGEMENT, TaxonomyManagementViewImpl.class));
		viewProviders.add(new ClassBasedViewProvider(TAXONOMY_CONCEPT_ADD_EDIT, AddEditTaxonomyConceptViewImpl.class));
		viewProviders.add(new ClassBasedViewProvider(GROUP_LIST, ListGlobalGroupsViewImpl.class));
		viewProviders.add(new ClassBasedViewProvider(GROUP_ADD_EDIT, AddEditGlobalGroupViewImpl.class));
		viewProviders.add(new ClassBasedViewProvider(GROUP_DISPLAY, DisplayGlobalGroupViewImpl.class));
		viewProviders.add(new ClassBasedViewProvider(USER_ADD_EDIT, AddEditUserCredentialViewImpl.class));
		viewProviders.add(new ClassBasedViewProvider(USER_DISPLAY, DisplayUserCredentialViewImpl.class));
		viewProviders.add(new ClassBasedViewProvider(USER_LIST, ListUsersCredentialsViewImpl.class));
		viewProviders.add(new ClassBasedViewProvider(ADD_EDIT_SCHEMA_RECORD, AddEditSchemaRecordViewImpl.class));
		viewProviders.add(new ClassBasedViewProvider(DISPLAY_SCHEMA_RECORD, DisplaySchemaRecordViewImpl.class));
		viewProviders.add(new ClassBasedViewProvider(LIST_SCHEMA_RECORDS, ListValueDomainRecordsViewImpl.class));
		viewProviders.add(new ClassBasedViewProvider(LIST_VALUE_LIST_RECORDS, ListSchemaRecordsViewImpl.class));
		viewProviders.add(new ClassBasedViewProvider(DISPLAY_SCHEMA_TYPE, ListSchemaTypeViewImpl.class));
		viewProviders.add(new ClassBasedViewProvider(DISPLAY_SCHEMA, ListSchemaViewImpl.class));
		viewProviders.add(new ClassBasedViewProvider(ADD_EDIT_SCHEMA, AddEditSchemaViewImpl.class));
		viewProviders.add(new ClassBasedViewProvider(ADD_EDIT_METADATA, AddEditMetadataViewImpl.class));
		viewProviders.add(new ClassBasedViewProvider(ADD_EDIT_SCHEMA_METADATA, AddEditSchemaMetadataViewImpl.class));
		viewProviders.add(new ClassBasedViewProvider(CONFIG_MANAGEMENT, ConfigManagementViewImpl.class));
		viewProviders.add(new ClassBasedViewProvider(PLUGIN_MANAGEMENT, PluginManagementViewImpl.class));
		viewProviders.add(new ClassBasedViewProvider(LDAP_CONFIG_MANAGEMENT, LDAPConfigManagementViewImpl.class));
		viewProviders.add(new ClassBasedViewProvider(EDIT_DISPLAY_FORM, DisplayConfigViewImpl.class));
		viewProviders.add(new ClassBasedViewProvider(SEARCH_DISPLAY_FORM, SearchDisplayConfigViewImpl.class));
		viewProviders.add(new ClassBasedViewProvider(TABLE_DISPLAY_FORM, TableDisplayConfigViewImpl.class));
		viewProviders.add(new ClassBasedViewProvider(FORM_DISPLAY_FORM, FormDisplayConfigViewImpl.class));
		viewProviders.add(new ClassBasedViewProvider(LIST_PRINCIPAL_ACCESS_AUTHORIZATIONS,
				ListPrincipalAccessAuthorizationsViewImpl.class));
		viewProviders.add(new ClassBasedViewProvider(LIST_OBJECT_ACCESS_AUTHORIZATIONS,
				ListContentAccessAuthorizationsViewImpl.class));
		viewProviders.add(new ClassBasedViewProvider(LIST_OBJECT_ROLE_AUTHORIZATIONS,
				ListContentRoleAuthorizationsViewImpl.class));
		viewProviders.add(new ClassBasedViewProvider(LIST_OBJECT_ACCESS_AND_ROLE_AUTHORIZATIONS,
				ListContentAccessAndRoleAuthorizationsViewImpl.class));
		viewProviders.add(new ClassBasedViewProvider(SHARE_CONTENT, ShareContentViewImpl.class));
		viewProviders.add(new ClassBasedViewProvider(MODIFY_SHARE, ShareContentViewImpl.class));
		viewProviders.add(new ClassBasedViewProvider(PUBLISH_DOCUMENT, PublishDocumentViewImpl.class));
		viewProviders.add(new ClassBasedViewProvider(COLLECTION_USER_LIST, ListCollectionUserViewImpl.class));
		viewProviders.add(new ClassBasedViewProvider(COLLECTION_USER, CollectionUserViewImpl.class));
		viewProviders.add(new ClassBasedViewProvider(COLLECTION_USER_ROLES, CollectionUserRolesViewImpl.class));
		viewProviders.add(new ClassBasedViewProvider(COLLECTION_GROUP, CollectionGroupViewImpl.class));
		viewProviders.add(new ClassBasedViewProvider(COLLECTION_GROUP_ROLES, CollectionGroupRolesViewImpl.class));
		viewProviders.add(new ClassBasedViewProvider(LIST_VALUE_DOMAINS, ListValueDomainViewImpl.class));
		viewProviders.add(new ClassBasedViewProvider(SEARCH_SCHEMA_RECORDS, SearchSchemaRecordsViewImpl.class));
		viewProviders.add(new ClassBasedViewProvider(LIST_TAXONOMY, ListTaxonomyViewImpl.class));
		viewProviders.add(new ClassBasedViewProvider(TAXONOMY_ADD_EDIT, AddEditTaxonomyViewImpl.class));
		viewProviders.add(new ClassBasedViewProvider(PERMISSION_MANAGEMENT, PermissionsManagementViewImpl.class));
		viewProviders.add(new ClassBasedViewProvider(SHARE_MANAGEMENT, ShareContentListViewImpl.class));
		viewProviders.add(new ClassBasedViewProvider(EXPORTER, ExportViewImpl.class));
		viewProviders.add(new ClassBasedViewProvider(REPORT_DISPLAY_FORM, ReportConfigurationViewImpl.class));
		viewProviders.add(new ClassBasedViewProvider(EMAIL_SERVER_MANAGEMENT, EmailServerConfigViewImpl.class));
		viewProviders.add(new ClassBasedViewProvider(TAXONOMY_SEARCH, TaxonomyManagementSearchViewImpl.class));
		viewProviders.add(new ClassBasedViewProvider(FORCED_REINDEX, ForcedReindexViewImpl.class));
		viewProviders.add(new ClassBasedViewProvider(DELETE_EXCLUSION, DeleteSpellCheckerExclusionsViewImpl.class));

		// TODO Use generic system to configure
		viewProviders.add(new ClassBasedViewProvider(ADMIN_MODULE, AdminViewImpl.class));
		viewProviders.add(new ClassBasedViewProvider(TRASH, TrashViewImpl.class));
		viewProviders.add(new ClassBasedViewProvider(EVENTS_LIST, EventCategoriesViewImpl.class));
		viewProviders.add(new ClassBasedViewProvider(EVENT_CATEGORY, BaseEventCategoryViewImpl.class));
		viewProviders.add(new ClassBasedViewProvider(EVENT_DISPLAY, EventViewImpl.class));

		viewProviders.add(new ClassBasedViewProvider(LIST_METADATA_GROUP, ListMetadataGroupSchemaTypeViewImpl.class));
		viewProviders.add(new ClassBasedViewProvider(LIST_ONGLET, ListMetadataGroupSchemaTypeViewImpl.class));

		viewProviders.add(new ClassBasedViewProvider(RECORDS_MANAGEMENT, HomeViewImpl.class));

		viewProviders.add(new ClassBasedViewProvider(IMPORT_FILE, ImportFileViewImpl.class));
		viewProviders.add(new ClassBasedViewProvider(IMPORT_USERS, ImportUsersFileViewImpl.class));
		viewProviders.add(new ClassBasedViewProvider(IMPORT_SCHEMA_TYPES, ImportSchemaTypesFileViewImpl.class));
		viewProviders.add(new ClassBasedViewProvider(IMPORT_AUTHORIZATIONS, ImportAuthorizationsFileViewImpl.class));
		viewProviders.add(new ClassBasedViewProvider(IMPORT_GROUPS, ImportGroupsFileViewImpl.class));
		viewProviders.add(new ClassBasedViewProvider(IMPORT_SETTINGS, ImportSettingsViewImpl.class));

		viewProviders.add(new ClassBasedViewProvider(COLLECTION_MANAGEMENT, CollectionManagementViewImpl.class));
		viewProviders.add(new ClassBasedViewProvider(COLLECTION_ADD_EDIT, AddEditCollectionViewImpl.class));
		viewProviders.add(new ClassBasedViewProvider(MODIFY_PROFILE, ModifyProfileViewImpl.class));

		viewProviders.add(new ClassBasedViewProvider(UPDATE_MANAGER, UpdateManagerViewImpl.class));
		viewProviders.add(new ClassBasedViewProvider(LIST_SAVED_SEARCHES, SavedSearchViewImpl.class));

		viewProviders.add(new ClassBasedViewProvider(ADD_FACET_CONFIGURATION, AddEditFacetConfigurationViewImpl.class));
		viewProviders.add(new ClassBasedViewProvider(EDIT_FACET_CONFIGURATION, AddEditFacetConfigurationViewImpl.class));
		viewProviders.add(new ClassBasedViewProvider(LIST_FACET_CONFIGURATION, ListFacetConfigurationViewImpl.class));
		viewProviders.add(new ClassBasedViewProvider(DISPLAY_FACET_CONFIGURATION, DisplayFacetConfigurationViewImpl.class));
		viewProviders.add(new ClassBasedViewProvider(ORDER_FACET_CONFIGURATION, OrderFacetConfigurationViewImpl.class));

		viewProviders.add(new ClassBasedViewProvider(LIST_METADATA_EXTRACTORS, ListMetadataExtractorsViewImpl.class));
		viewProviders.add(new ClassBasedViewProvider(ADD_METADATA_EXTRACTOR, AddEditMetadataExtractorViewImpl.class));
		viewProviders.add(new ClassBasedViewProvider(EDIT_METADATA_EXTRACTOR, AddEditMetadataExtractorViewImpl.class));
		viewProviders.add(new ClassBasedViewProvider(PROPERTIES_ANALYSER, PropertiesAnalyserViewImpl.class));
		viewProviders.add(new ClassBasedViewProvider(SEARCH_BOOST_BY_METADATAS, SearchBoostByMetadataViewImpl.class));
		viewProviders.add(new ClassBasedViewProvider(SEARCH_BOOST_BY_QUERYS, SearchBoostByQueryViewImpl.class));
		viewProviders.add(new ClassBasedViewProvider(SOLR_FEATURES, SolrFeatureViewImpl.class));
		viewProviders.add(new ClassBasedViewProvider(ORDER_DECOMMISSIONING_LIST_CONFIGURATION,
				OrderDecommissioningListViewImpl.class));

		viewProviders.add(new ClassBasedViewProvider(SYSTEM_CHECK, SystemCheckViewImpl.class));

		viewProviders.add(new ClassBasedViewProvider(LABEL_CONFIG, ListLabelViewImpl.class));
		viewProviders.add(new ClassBasedViewProvider(LABEL_AJOUT, AddEditLabelViewImpl.class));
		viewProviders.add(new ClassBasedViewProvider(LABEL_VIEW, LabelViewImpl.class));
		viewProviders.add(new ClassBasedViewProvider(PRINTABLE_REPORT_MANAGE, ListPrintableReportViewImpl.class));
		viewProviders.add(new ClassBasedViewProvider(PRINTABLE_REPORT_ADD, AddEditPrintableReportViewImpl.class));
		viewProviders.add(new ClassBasedViewProvider(PRINTABLE_REPORT_VIEW, DisplayPrintableReportViewImpl.class));
		viewProviders.add(new ClassBasedViewProvider(REPORT_VIEW, ReportConfigViewImpl.class));
		viewProviders.add(new ClassBasedViewProvider(EXCEL_REPORT_MANAGE, ListExcelReportViewImpl.class));
		viewProviders.add(new ClassBasedViewProvider(EXCEL_REPORT_DISPLAY, DisplayExcelReportViewImpl.class));
		viewProviders.add(new ClassBasedViewProvider(LIST_BATCH_PROCESSES, ListBatchProcessesViewImpl.class));
		viewProviders.add(new ClassBasedViewProvider(LIST_IMPORT_EXPORT, ListImportExportViewImpl.class));
		viewProviders.add(new ClassBasedViewProvider(LIST_TEMPORARY_RECORDS, ListTemporaryRecordViewImpl.class));
		viewProviders.add(new ClassBasedViewProvider(SEARCH_CONFIGURATION, SearchConfigurationViewImpl.class));

		viewProviders.add(new ClassBasedViewProvider(LIST_CAPSULE, ListCapsuleViewImpl.class));
		viewProviders.add(new ClassBasedViewProvider(ADD_EDIT_CAPSULE, AddEditCapsuleViewImpl.class));
		viewProviders.add(new ClassBasedViewProvider(DISPLAY_CAPSULE, DisplayCapsuleViewImpl.class));
		viewProviders.add(new ClassBasedViewProvider(EDIT_SYNONYMS, EditSynonymsViewImpl.class));
		viewProviders.add(new ClassBasedViewProvider(DISPLAY_SYNONYMS, DisplaySynonymsViewImpl.class));
		viewProviders.add(new ClassBasedViewProvider(EDIT_ELEVATION, EditElevationViewImpl.class));
		viewProviders.add(new ClassBasedViewProvider(STATISTICS, StatisticsViewImpl.class));

		viewProviders.add(new ClassBasedViewProvider(DISPLAY_BAG_INFO, DisplayBagInfoViewImpl.class));
		viewProviders.add(new ClassBasedViewProvider(LIST_BAG_INFO, ListBagInfoViewImpl.class));
		viewProviders.add(new ClassBasedViewProvider(ADD_EDIT_BAG_INFO, AddEditBagInfoViewImpl.class));
		viewProviders.add(new ClassBasedViewProvider(SIP_PROGRESSION, SIPProgressionViewImpl.class));
		viewProviders.add(new ClassBasedViewProvider(DELETE_EXCLUSION, DeleteSpellCheckerExclusionsViewImpl.class));
		viewProviders.add(new ClassBasedViewProvider(THESAURUS_CONFIGURATION, ThesaurusConfigurationViewImpl.class));
		viewProviders.add(new ClassBasedViewProvider(SUMMARY_CONFIGURATOR, SummaryConfigViewImpl.class));
		viewProviders.add(new ClassBasedViewProvider(FOLDER_UNIQUE_KEY_METADATA_CONFIGURATOR, FolderUniqueKeyConfiguratorViewImpl.class));

	}

	public void configure(Navigator navigator) {
		for (ViewProvider viewProvider : viewProviders) {
			navigator.addProvider(viewProvider);
		}
	}

	public void register(String code, Class<? extends BaseViewImpl> clazz) {
		viewProviders.add(new ClassBasedViewProvider(code, clazz));
	}
}
