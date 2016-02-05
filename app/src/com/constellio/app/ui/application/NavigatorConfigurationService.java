package com.constellio.app.ui.application;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import com.constellio.app.modules.es.ui.pages.ConnectorReportViewImpl;
import com.constellio.app.modules.es.ui.pages.DisplayConnectorInstanceViewImpl;
import com.constellio.app.modules.es.ui.pages.EditConnectorInstanceViewImpl;
import com.constellio.app.modules.es.ui.pages.ListConnectorInstancesViewImpl;
import com.constellio.app.modules.es.ui.pages.WizardConnectorInstanceViewImpl;
import com.constellio.app.modules.es.ui.pages.mapping.AddEditMappingViewImpl;
import com.constellio.app.modules.es.ui.pages.mapping.DisplayConnectorMappingsViewImpl;
import com.constellio.app.modules.rm.ui.pages.agent.AgentSetupViewImpl;
import com.constellio.app.modules.rm.ui.pages.agent.ListAgentLogsViewImpl;
import com.constellio.app.modules.rm.ui.pages.cart.CartViewImpl;
import com.constellio.app.modules.rm.ui.pages.containers.ContainersByAdministrativeUnitsViewImpl;
import com.constellio.app.modules.rm.ui.pages.containers.ContainersInAdministrativeUnitViewImpl;
import com.constellio.app.modules.rm.ui.pages.containers.ContainersInFilingSpaceViewImpl;
import com.constellio.app.modules.rm.ui.pages.containers.DisplayContainerViewImpl;
import com.constellio.app.modules.rm.ui.pages.containers.edit.AddEditContainerViewImpl;
import com.constellio.app.modules.rm.ui.pages.decommissioning.AddExistingContainerViewImpl;
import com.constellio.app.modules.rm.ui.pages.decommissioning.AddNewContainerViewImpl;
import com.constellio.app.modules.rm.ui.pages.decommissioning.DecommissioningBuilderViewImpl;
import com.constellio.app.modules.rm.ui.pages.decommissioning.DecommissioningListViewImpl;
import com.constellio.app.modules.rm.ui.pages.decommissioning.DecommissioningMainViewImpl;
import com.constellio.app.modules.rm.ui.pages.decommissioning.DocumentDecommissioningListViewImpl;
import com.constellio.app.modules.rm.ui.pages.decommissioning.EditDecommissioningListViewImpl;
import com.constellio.app.modules.rm.ui.pages.document.AddEditDocumentViewImpl;
import com.constellio.app.modules.rm.ui.pages.document.DisplayDocumentViewImpl;
import com.constellio.app.modules.rm.ui.pages.email.AddEmailAttachmentsToFolderViewImpl;
import com.constellio.app.modules.rm.ui.pages.folder.AddEditFolderViewImpl;
import com.constellio.app.modules.rm.ui.pages.folder.DisplayFolderViewImpl;
import com.constellio.app.modules.rm.ui.pages.management.ArchiveManagementViewImpl;
import com.constellio.app.modules.rm.ui.pages.reports.RMReportsViewImpl;
import com.constellio.app.modules.rm.ui.pages.retentionRule.AddEditRetentionRuleViewImpl;
import com.constellio.app.modules.rm.ui.pages.retentionRule.DisplayRetentionRuleViewImpl;
import com.constellio.app.modules.rm.ui.pages.retentionRule.ListRetentionRulesViewImpl;
import com.constellio.app.modules.rm.ui.pages.retentionRule.SearchRetentionRulesViewImpl;
import com.constellio.app.modules.rm.ui.pages.userDocuments.ListUserDocumentsViewImpl;
import com.constellio.app.modules.robots.ui.pages.AddEditRobotViewImpl;
import com.constellio.app.modules.robots.ui.pages.ListRootRobotsViewImpl;
import com.constellio.app.modules.robots.ui.pages.RobotConfigurationViewImpl;
import com.constellio.app.modules.robots.ui.pages.RobotLogsViewImpl;
import com.constellio.app.modules.tasks.ui.pages.AddEditTaskViewImpl;
import com.constellio.app.modules.tasks.ui.pages.DisplayTaskViewImpl;
import com.constellio.app.modules.tasks.ui.pages.TaskManagementViewImpl;
import com.constellio.app.modules.tasks.ui.pages.TasksLogsViewImpl;
import com.constellio.app.ui.pages.collection.CollectionGroupRolesViewImpl;
import com.constellio.app.ui.pages.collection.CollectionGroupViewImpl;
import com.constellio.app.ui.pages.collection.CollectionUserRolesViewImpl;
import com.constellio.app.ui.pages.collection.CollectionUserViewImpl;
import com.constellio.app.ui.pages.collection.ListCollectionUserViewImpl;
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
import com.constellio.app.ui.pages.imports.authorization.ImportAuthorizationsFileViewImpl;
import com.constellio.app.ui.pages.management.AdminViewImpl;
import com.constellio.app.ui.pages.management.app.AppManagementView;
import com.constellio.app.ui.pages.management.authorizations.ListContentAccessAuthorizationsViewImpl;
import com.constellio.app.ui.pages.management.authorizations.ListContentRoleAuthorizationsViewImpl;
import com.constellio.app.ui.pages.management.authorizations.ListPrincipalAccessAuthorizationsViewImpl;
import com.constellio.app.ui.pages.management.authorizations.ShareContentViewImpl;
import com.constellio.app.ui.pages.management.collections.AddEditCollectionViewImpl;
import com.constellio.app.ui.pages.management.collections.CollectionManagementViewImpl;
import com.constellio.app.ui.pages.management.configs.ConfigManagementViewImpl;
import com.constellio.app.ui.pages.management.email.EmailServerConfigViewImpl;
import com.constellio.app.ui.pages.management.extractors.AddEditMetadataExtractorViewImpl;
import com.constellio.app.ui.pages.management.extractors.ListMetadataExtractorsViewImpl;
import com.constellio.app.ui.pages.management.facet.AddEditFacetConfigurationViewImpl;
import com.constellio.app.ui.pages.management.facet.DisplayFacetConfigurationViewImpl;
import com.constellio.app.ui.pages.management.facet.ListFacetConfigurationViewImpl;
import com.constellio.app.ui.pages.management.facet.OrderFacetConfigurationViewImpl;
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
import com.constellio.app.ui.pages.management.taxonomy.AddEditTaxonomyConceptViewImpl;
import com.constellio.app.ui.pages.management.taxonomy.AddEditTaxonomyViewImpl;
import com.constellio.app.ui.pages.management.taxonomy.ListTaxonomyViewImpl;
import com.constellio.app.ui.pages.management.taxonomy.TaxonomyManagementSearchViewImpl;
import com.constellio.app.ui.pages.management.taxonomy.TaxonomyManagementViewImpl;
import com.constellio.app.ui.pages.management.updates.UpdateManagerViewImpl;
import com.constellio.app.ui.pages.management.valueDomains.ListValueDomainViewImpl;
import com.constellio.app.ui.pages.profile.ModifyProfileViewImpl;
import com.constellio.app.ui.pages.search.AdvancedSearchViewImpl;
import com.constellio.app.ui.pages.search.SearchBoostByMetadataViewImpl;
import com.constellio.app.ui.pages.search.SearchBoostByQueryViewImpl;
import com.constellio.app.ui.pages.search.SimpleSearchViewImpl;
import com.constellio.app.ui.pages.search.savedSearch.SavedSearchViewImpl;
import com.constellio.app.ui.pages.user.AddEditUserCredentialViewImpl;
import com.constellio.app.ui.pages.user.DisplayUserCredentialViewImpl;
import com.constellio.app.ui.pages.user.ListUsersCredentialsViewImpl;
import com.vaadin.navigator.Navigator;
import com.vaadin.navigator.Navigator.ClassBasedViewProvider;
import com.vaadin.navigator.View;
import com.vaadin.navigator.ViewProvider;

public class NavigatorConfigurationService implements Serializable {
	public static final String HOME = "";
	public static final String APP_MANAGEMENT = "appManagement";
	public static final String SIMPLE_SEARCH = "search";
	public static final String ADVANCED_SEARCH = "advancedSearch";
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
	public static final String USER_DISPLAY = "userDisplay";
	public static final String LIST_PRINCIPAL_ACCESS_AUTHORIZATIONS = "principalAccessAuthorizations";
	public static final String LIST_OBJECT_ACCESS_AUTHORIZATIONS = "objectAccessAuthorizations";
	public static final String LIST_OBJECT_ROLE_AUTHORIZATIONS = "objectRoleAuthorizations";
	public static final String SHARE_CONTENT = "shareContent";
	public static final String ADMIN_MODULE = "adminModule";
	public static final String COLLECTION_USER_LIST = "collectionSecurity";
	public static final String COLLECTION_USER = "collectionUser";
	public static final String COLLECTION_USER_ROLES = "collectionUserRoles";
	public static final String COLLECTION_GROUP = "collectionGroup";
	public static final String COLLECTION_GROUP_ROLES = "collectionGroupRoles";
	public static final String CONFIG_MANAGEMENT = "configManagement";
	public static final String PLUGIN_MANAGEMENT = "pluginManagement";
	public static final String EMAIL_SERVER_MANAGEMENT = "emailServerManagement";
	public static final String REPORTS = "reports";
	public static final String ARCHIVES_MANAGEMENT = "archivesManagement";
	public static final String DECOMMISSIONING = "decommissioning";
	public static final String DECOMMISSIONING_LIST_BUILDER = "decommissioningListBuilder";
	public static final String DECOMMISSIONING_LIST_DISPLAY = "decommissioningList";
	public static final String DOCUMENT_DECOMMISSIONING_LIST_DISPLAY = "documentDecommissioningList";
	public static final String DECOMMISSIONING_LIST_EDIT = "editDecommissioningList";
	public static final String DECOMMISSIONING_LIST_ADD_NEW_CONTAINER = "createContainerForDecommissioningList";
	public static final String DECOMMISSIONING_LIST_ADD_EXISTING_CONTAINER = "searchContainerForDecommissioningList";
	public static final String DISPLAY_SCHEMA_TYPE = "displaySchemaType";
	public static final String DISPLAY_SCHEMA = "displaySchema";
	public static final String ADD_EDIT_SCHEMA = "editSchema";
	public static final String ADD_EDIT_METADATA = "editMetadata";
	public static final String ADD_EDIT_SCHEMA_METADATA = "editSchemaMetadata";
	public static final String LIST_METADATA_GROUP = "listMetadataGroup";
	public static final String RECORDS_MANAGEMENT = "recordsManagement";
	public static final String ADD_FOLDER = "addFolder";
	public static final String EDIT_FOLDER = "editFolder";
	public static final String DISPLAY_FOLDER = "displayFolder";
	public static final String ADD_DOCUMENT = "addDocument";
	public static final String EDIT_DOCUMENT = "editDocument";
	public static final String DISPLAY_DOCUMENT = "displayDocument";
	public static final String EDIT_DISPLAY_FORM = "displayForm";
	public static final String SEARCH_DISPLAY_FORM = "searchDisplayForm";
	public static final String TABLE_DISPLAY_FORM = "tableDisplayForm";
	public static final String CONTAINERS_BY_ADMIN_UNITS = "containersByAdminUnits";
	public static final String CART = "cart";
	public static final String DISPLAY_ADMIN_UNIT_WITH_CONTAINERS = "displayAdminUnitWithContainers";
	public static final String FORM_DISPLAY_FORM = "formDisplayForm";
	public static final String DISPLAY_FILING_SPACE_WITH_CONTAINERS = "displayFilingSpaceWithContainers";
	public static final String DISPLAY_CONTAINER = "displayContainer";
	public static final String ADD_RETENTION_RULE = "addRetentionRule";
	public static final String EDIT_RETENTION_RULE = "editRetentionRule";
	public static final String DISPLAY_RETENTION_RULE = "displayRetentionRule";
	public static final String LIST_RETENTION_RULES = "listRetentionRules";
	public static final String LIST_USER_DOCUMENTS = "listUserDocuments";
	public static final String PERMISSION_MANAGEMENT = "permissionManagement";
	public static final String COLLECTION_MANAGEMENT = "listCollections";
	public static final String LIST_ONGLET = "listOnglet";
	public static final String MODIFY_PROFILE = "modifyProfile";
	public static final String UPDATE_MANAGER = "updateManager";
	public static final String LDAP_CONFIG_MANAGEMENT = "ldapConfigManagement";
	public static final String IMPORT_FILE = "importRecords";
	public static final String IMPORT_USERS = "importUsers";
	public static final String IMPORT_AUTHORIZATIONS = "importAuthorizations";
	public static final String IMPORT_GROUPS = "importGroups";
	public static final String EDIT_CONTAINER = "editContainer";
	public static final String IMPORT_SCHEMA_TYPES = "importSchemaTypes";
	public static final String AGENT_SETUP = "agentSetup";
	public static final String LIST_AGENT_LOGS = "listAgentLogs";
	public static final String EXPORTER = "export";
	public static final String REPORT_DISPLAY_FORM = "reportDisplayForm";
	public static final String LIST_CONNECTOR_INSTANCES = "connectorInstances";
	public static final String DISPLAY_CONNECTOR_INSTANCE = "displayConnectorInstance";
	public static final String EDIT_CONNECTOR_INSTANCE = "editConnectorInstance";
	public static final String WIZARD_CONNECTOR_INSTANCE = "wizardConnectorInstance";
	public static final String LIST_SAVED_SEARCHES = "listSavedSearches";
	public static final String TASKS_MANAGEMENT = "tasksManagement";
	public static final String ADD_TASK = "addTask";
	public static final String EDIT_TASK = "editTask";
	public static final String DISPLAY_TASK = "displayTask";
	public static final String LIST_TASKS_LOGS = "listTaksLogs";
	public static final String DISPLAY_CONNECTOR_MAPPINGS = "displayConnectorMappings";
	public static final String ADD_CONNECTOR_MAPPING = "addConnectorMapping";
	public static final String EDIT_CONNECTOR_MAPPING = "editConnectorMapping";
	public static final String CONNECTOR_REPORT = "connectorReport";
	public static final String SEARCH_BOOST_BY_METADATAS = "searchBoostByMetadatas";
	public static final String SEARCH_BOOST_BY_QUERYS = "searchBoostByQuery";
	public static final String FORCED_REINDEX = "reindex";

	public static final String ADD_FACET_CONFIGURATION = "addFacetConfiguration";
	public static final String EDIT_FACET_CONFIGURATION = "editFacetConfiguration";
	public static final String LIST_FACET_CONFIGURATION = "listFacetConfiguration";
	public static final String DISPLAY_FACET_CONFIGURATION = "displayFacetConfiguration";
	public static final String ORDER_FACET_CONFIGURATION = "orderFacetConfiguration";

	public static final String LIST_METADATA_EXTRACTORS = "listMetadataExtractors";
	public static final String ADD_METADATA_EXTRACTOR = "addMetadataExtractor";
	public static final String EDIT_METADATA_EXTRACTOR = "editMetadataExtractor";

	public static final String LIST_ROOT_ROBOTS = "listRootRobots";
	public static final String ROBOT_CONFIGURATION = "robotConfiguration";
	public static final String ADD_EDIT_ROBOT = "addEditRobot";
	public static final String ROBOT_LOGS = "robotLogs";

	public static final String TAXONOMY_SEARCH = "taxonomySearch";
	public static final String RETENTION_RULES_SEARCH = "retentionRulesSearch";
	public static final String ADD_EMAIL_ATTACHMENTS_TO_FOLDER = "addEmailAttachmentsToFolder";

	private List<ViewProvider> viewProviders = new ArrayList<>();

	public NavigatorConfigurationService() {
		//		viewProviders.add(new ClassBasedViewProvider(SIMPLE_SEARCH, SimpleSearchViewImpl.class));
		//		viewProviders.add(new ClassBasedViewProvider(ADVANCED_SEARCH, AdvancedSearchViewImpl.class));
		viewProviders.add(new OneInstanceViewProvider(SIMPLE_SEARCH) {
			@Override
			protected View newView(String viewName) {
				return new SimpleSearchViewImpl();
			}
		});
		viewProviders.add(new OneInstanceViewProvider(ADVANCED_SEARCH) {
			@Override
			protected View newView(String viewName) {
				return new AdvancedSearchViewImpl();
			}
		});

		viewProviders.add(new ClassBasedViewProvider(HOME, HomeViewImpl.class));
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
		viewProviders.add(new ClassBasedViewProvider(LIST_SCHEMA_RECORDS, ListSchemaRecordsViewImpl.class));
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
		viewProviders.add(new ClassBasedViewProvider(SHARE_CONTENT, ShareContentViewImpl.class));
		viewProviders.add(new ClassBasedViewProvider(COLLECTION_USER_LIST, ListCollectionUserViewImpl.class));
		viewProviders.add(new ClassBasedViewProvider(COLLECTION_USER, CollectionUserViewImpl.class));
		viewProviders.add(new ClassBasedViewProvider(COLLECTION_USER_ROLES, CollectionUserRolesViewImpl.class));
		viewProviders.add(new ClassBasedViewProvider(COLLECTION_GROUP, CollectionGroupViewImpl.class));
		viewProviders.add(new ClassBasedViewProvider(COLLECTION_GROUP_ROLES, CollectionGroupRolesViewImpl.class));
		viewProviders.add(new ClassBasedViewProvider(LIST_VALUE_DOMAINS, ListValueDomainViewImpl.class));
		viewProviders.add(new ClassBasedViewProvider(SEARCH_SCHEMA_RECORDS, SearchSchemaRecordsViewImpl.class));
		viewProviders.add(new ClassBasedViewProvider(LIST_TAXONOMY, ListTaxonomyViewImpl.class));
		viewProviders.add(new ClassBasedViewProvider(TAXONOMY_ADD_EDIT, AddEditTaxonomyViewImpl.class));
		viewProviders.add(new ClassBasedViewProvider(CONTAINERS_BY_ADMIN_UNITS, ContainersByAdministrativeUnitsViewImpl.class));
		viewProviders.add(new ClassBasedViewProvider(DISPLAY_ADMIN_UNIT_WITH_CONTAINERS,
				ContainersInAdministrativeUnitViewImpl.class));
		viewProviders
				.add(new ClassBasedViewProvider(DISPLAY_FILING_SPACE_WITH_CONTAINERS, ContainersInFilingSpaceViewImpl.class));
		viewProviders.add(new ClassBasedViewProvider(DISPLAY_CONTAINER, DisplayContainerViewImpl.class));
		viewProviders.add(new ClassBasedViewProvider(PERMISSION_MANAGEMENT, PermissionsManagementViewImpl.class));
		viewProviders.add(new ClassBasedViewProvider(EXPORTER, ExportViewImpl.class));
		viewProviders.add(new ClassBasedViewProvider(REPORT_DISPLAY_FORM, ReportConfigurationViewImpl.class));
		viewProviders.add(new ClassBasedViewProvider(EMAIL_SERVER_MANAGEMENT, EmailServerConfigViewImpl.class));
		viewProviders.add(new ClassBasedViewProvider(TAXONOMY_SEARCH, TaxonomyManagementSearchViewImpl.class));
		viewProviders.add(new ClassBasedViewProvider(RETENTION_RULES_SEARCH, SearchRetentionRulesViewImpl.class));
		viewProviders.add(new ClassBasedViewProvider(FORCED_REINDEX, ForcedReindexViewImpl.class));

		// TODO Use generic system to configure
		viewProviders.add(new ClassBasedViewProvider(ADMIN_MODULE, AdminViewImpl.class));
		viewProviders.add(new ClassBasedViewProvider(EVENTS_LIST, EventCategoriesViewImpl.class));
		viewProviders.add(new ClassBasedViewProvider(EVENT_CATEGORY, BaseEventCategoryViewImpl.class));
		viewProviders.add(new ClassBasedViewProvider(EVENT_DISPLAY, EventViewImpl.class));
		viewProviders.add(new ClassBasedViewProvider(REPORTS, RMReportsViewImpl.class));
		viewProviders.add(new ClassBasedViewProvider(ARCHIVES_MANAGEMENT, ArchiveManagementViewImpl.class));
		viewProviders.add(new ClassBasedViewProvider(DECOMMISSIONING, DecommissioningMainViewImpl.class));
		viewProviders.add(new ClassBasedViewProvider(DECOMMISSIONING_LIST_BUILDER, DecommissioningBuilderViewImpl.class));
		viewProviders.add(new ClassBasedViewProvider(DECOMMISSIONING_LIST_DISPLAY, DecommissioningListViewImpl.class));
		viewProviders.add(new ClassBasedViewProvider(DOCUMENT_DECOMMISSIONING_LIST_DISPLAY,
				DocumentDecommissioningListViewImpl.class));
		viewProviders.add(new ClassBasedViewProvider(DECOMMISSIONING_LIST_EDIT, EditDecommissioningListViewImpl.class));
		viewProviders.add(new ClassBasedViewProvider(DECOMMISSIONING_LIST_ADD_NEW_CONTAINER, AddNewContainerViewImpl.class));
		viewProviders.add(
				new ClassBasedViewProvider(DECOMMISSIONING_LIST_ADD_EXISTING_CONTAINER, AddExistingContainerViewImpl.class));

		viewProviders.add(new ClassBasedViewProvider(CART, CartViewImpl.class));

		viewProviders.add(new ClassBasedViewProvider(LIST_METADATA_GROUP, ListMetadataGroupSchemaTypeViewImpl.class));
		viewProviders.add(new ClassBasedViewProvider(LIST_ONGLET, ListMetadataGroupSchemaTypeViewImpl.class));

		viewProviders.add(new ClassBasedViewProvider(RECORDS_MANAGEMENT, HomeViewImpl.class));
		viewProviders.add(new ClassBasedViewProvider(ADD_FOLDER, AddEditFolderViewImpl.class));
		viewProviders.add(new ClassBasedViewProvider(EDIT_FOLDER, AddEditFolderViewImpl.class));
		viewProviders.add(new ClassBasedViewProvider(DISPLAY_FOLDER, DisplayFolderViewImpl.class));
		viewProviders.add(new ClassBasedViewProvider(ADD_DOCUMENT, AddEditDocumentViewImpl.class));
		viewProviders.add(new ClassBasedViewProvider(EDIT_DOCUMENT, AddEditDocumentViewImpl.class));
		viewProviders.add(new ClassBasedViewProvider(DISPLAY_DOCUMENT, DisplayDocumentViewImpl.class));
		viewProviders.add(new ClassBasedViewProvider(ADD_RETENTION_RULE, AddEditRetentionRuleViewImpl.class));
		viewProviders.add(new ClassBasedViewProvider(EDIT_RETENTION_RULE, AddEditRetentionRuleViewImpl.class));
		viewProviders.add(new ClassBasedViewProvider(DISPLAY_RETENTION_RULE, DisplayRetentionRuleViewImpl.class));

		viewProviders.add(new ClassBasedViewProvider(LIST_RETENTION_RULES, ListRetentionRulesViewImpl.class));
		viewProviders.add(new ClassBasedViewProvider(IMPORT_FILE, ImportFileViewImpl.class));
		viewProviders.add(new ClassBasedViewProvider(IMPORT_USERS, ImportUsersFileViewImpl.class));
		viewProviders.add(new ClassBasedViewProvider(IMPORT_SCHEMA_TYPES, ImportSchemaTypesFileViewImpl.class));
		viewProviders.add(new ClassBasedViewProvider(IMPORT_AUTHORIZATIONS, ImportAuthorizationsFileViewImpl.class));
		viewProviders.add(new ClassBasedViewProvider(IMPORT_GROUPS, ImportGroupsFileViewImpl.class));

		viewProviders.add(new ClassBasedViewProvider(LIST_USER_DOCUMENTS, ListUserDocumentsViewImpl.class));
		viewProviders.add(new ClassBasedViewProvider(COLLECTION_MANAGEMENT, CollectionManagementViewImpl.class));
		viewProviders.add(new ClassBasedViewProvider(COLLECTION_ADD_EDIT, AddEditCollectionViewImpl.class));
		viewProviders.add(new ClassBasedViewProvider(MODIFY_PROFILE, ModifyProfileViewImpl.class));

		viewProviders.add(new ClassBasedViewProvider(UPDATE_MANAGER, UpdateManagerViewImpl.class));
		viewProviders.add(new ClassBasedViewProvider(EDIT_CONTAINER, AddEditContainerViewImpl.class));
		viewProviders.add(new ClassBasedViewProvider(AGENT_SETUP, AgentSetupViewImpl.class));
		viewProviders.add(new ClassBasedViewProvider(LIST_AGENT_LOGS, ListAgentLogsViewImpl.class));
		viewProviders.add(new ClassBasedViewProvider(LIST_SAVED_SEARCHES, SavedSearchViewImpl.class));

		viewProviders.add(new ClassBasedViewProvider(ADD_FACET_CONFIGURATION, AddEditFacetConfigurationViewImpl.class));
		viewProviders.add(new ClassBasedViewProvider(EDIT_FACET_CONFIGURATION, AddEditFacetConfigurationViewImpl.class));
		viewProviders.add(new ClassBasedViewProvider(LIST_FACET_CONFIGURATION, ListFacetConfigurationViewImpl.class));
		viewProviders.add(new ClassBasedViewProvider(DISPLAY_FACET_CONFIGURATION, DisplayFacetConfigurationViewImpl.class));
		viewProviders.add(new ClassBasedViewProvider(ORDER_FACET_CONFIGURATION, OrderFacetConfigurationViewImpl.class));

		viewProviders.add(new ClassBasedViewProvider(LIST_METADATA_EXTRACTORS, ListMetadataExtractorsViewImpl.class));
		viewProviders.add(new ClassBasedViewProvider(ADD_METADATA_EXTRACTOR, AddEditMetadataExtractorViewImpl.class));
		viewProviders.add(new ClassBasedViewProvider(EDIT_METADATA_EXTRACTOR, AddEditMetadataExtractorViewImpl.class));
		viewProviders.add(new ClassBasedViewProvider(SEARCH_BOOST_BY_METADATAS, SearchBoostByMetadataViewImpl.class));
		viewProviders.add(new ClassBasedViewProvider(SEARCH_BOOST_BY_QUERYS, SearchBoostByQueryViewImpl.class));

		//Module ES
		viewProviders.add(new ClassBasedViewProvider(LIST_CONNECTOR_INSTANCES, ListConnectorInstancesViewImpl.class));
		viewProviders.add(new ClassBasedViewProvider(DISPLAY_CONNECTOR_INSTANCE, DisplayConnectorInstanceViewImpl.class));
		viewProviders.add(new ClassBasedViewProvider(EDIT_CONNECTOR_INSTANCE, EditConnectorInstanceViewImpl.class));
		viewProviders.add(new ClassBasedViewProvider(WIZARD_CONNECTOR_INSTANCE, WizardConnectorInstanceViewImpl.class));
		viewProviders.add(new ClassBasedViewProvider(DISPLAY_CONNECTOR_MAPPINGS, DisplayConnectorMappingsViewImpl.class));
		viewProviders.add(new ClassBasedViewProvider(ADD_CONNECTOR_MAPPING, AddEditMappingViewImpl.class));
		viewProviders.add(new ClassBasedViewProvider(EDIT_CONNECTOR_MAPPING, AddEditMappingViewImpl.class));
		viewProviders.add(new ClassBasedViewProvider(CONNECTOR_REPORT, ConnectorReportViewImpl.class));

		//Tasks Module
		viewProviders.add(new ClassBasedViewProvider(TASKS_MANAGEMENT, TaskManagementViewImpl.class));
		viewProviders.add(new ClassBasedViewProvider(EDIT_TASK, AddEditTaskViewImpl.class));
		viewProviders.add(new ClassBasedViewProvider(ADD_TASK, AddEditTaskViewImpl.class));
		viewProviders.add(new ClassBasedViewProvider(DISPLAY_TASK, DisplayTaskViewImpl.class));
		viewProviders.add(new ClassBasedViewProvider(LIST_TASKS_LOGS, TasksLogsViewImpl.class));

		//Robots Module
		viewProviders.add(new ClassBasedViewProvider(LIST_ROOT_ROBOTS, ListRootRobotsViewImpl.class));
		viewProviders.add(new ClassBasedViewProvider(ROBOT_CONFIGURATION, RobotConfigurationViewImpl.class));
		viewProviders.add(new ClassBasedViewProvider(ADD_EDIT_ROBOT, AddEditRobotViewImpl.class));
		viewProviders.add(new ClassBasedViewProvider(ROBOT_LOGS, RobotLogsViewImpl.class));
		
		viewProviders.add(new ClassBasedViewProvider(ADD_EMAIL_ATTACHMENTS_TO_FOLDER, AddEmailAttachmentsToFolderViewImpl.class));
	}

	public void configure(Navigator navigator) {
		for (ViewProvider viewProvider : viewProviders) {
			navigator.addProvider(viewProvider);
		}
	}
}
