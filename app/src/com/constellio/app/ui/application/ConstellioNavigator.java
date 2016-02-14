package com.constellio.app.ui.application;

import java.util.Map;

import org.apache.commons.lang3.StringUtils;

import com.constellio.app.modules.es.ui.pages.ConnectorReportView;
import com.constellio.app.ui.pages.events.EventCategory;
import com.constellio.app.ui.pages.management.taxonomy.AddEditTaxonomyConceptPresenter;
import com.constellio.app.ui.pages.management.taxonomy.TaxonomyManagementPresenter;
import com.constellio.app.ui.params.ParamUtils;
import com.google.gwt.dev.util.collect.HashMap;
import com.vaadin.navigator.Navigator;
import com.vaadin.ui.JavaScript;

public class ConstellioNavigator {
	Navigator vaadinNavigator;

	public String getState() {
		return vaadinNavigator.getState();
	}

	public ConstellioNavigator(Navigator vaadinNavigator) {
		this.vaadinNavigator = vaadinNavigator;
	}

	public void previousView() {
		JavaScript javascript = JavaScript.getCurrent();
		javascript.execute("window.history.back();");
	}

	public void home() {
		vaadinNavigator.navigateTo(NavigatorConfigurationService.HOME);
	}

	public void recordsManagement() {
		vaadinNavigator.navigateTo(NavigatorConfigurationService.RECORDS_MANAGEMENT);
	}

	public void cart() {
		vaadinNavigator.navigateTo(NavigatorConfigurationService.CART);
	}

	public void addFolder(String parentFolderId, String typeId) {
		Map<String, String> params = new HashMap<String, String>();
		if (parentFolderId != null) {
			params.put("parentId", parentFolderId);
		}
		if (typeId != null) {
			params.put("typeId", typeId);
		}
		String viewPath = ParamUtils.addParams(NavigatorConfigurationService.ADD_FOLDER, params);
		vaadinNavigator.navigateTo(viewPath);
	}

	public void editFolder(String id) {
		Map<String, String> params = new HashMap<>();
		params.put("id", id);
		String viewPath = ParamUtils.addParams(NavigatorConfigurationService.EDIT_FOLDER, params);
		vaadinNavigator.navigateTo(viewPath);
	}

	public void displayFolder(String id) {
		vaadinNavigator.navigateTo(NavigatorConfigurationService.DISPLAY_FOLDER + "/" + id);
	}

	public void addDocument(String folderId, String typeId) {
		Map<String, String> params = new HashMap<>();
		if (folderId != null) {
			params.put("parentId", folderId);
		}
		if (typeId != null) {
			params.put("typeId", typeId);
		}
		String viewPath = ParamUtils.addParams(NavigatorConfigurationService.ADD_DOCUMENT, params);
		vaadinNavigator.navigateTo(viewPath);
	}

	public void addDocumentWithContent(String id) {
		Map<String, String> params = new HashMap<>();
		params.put("idCopy", id);
		String viewPath = ParamUtils.addParams(NavigatorConfigurationService.ADD_DOCUMENT, params);
		vaadinNavigator.navigateTo(viewPath);
	}

	public void editDocument(String id) {
		Map<String, String> params = new HashMap<>();
		params.put("id", id);
		String viewPath = ParamUtils.addParams(NavigatorConfigurationService.EDIT_DOCUMENT, params);
		vaadinNavigator.navigateTo(viewPath);
	}

	public void displayDocument(String id) {
		vaadinNavigator.navigateTo(NavigatorConfigurationService.DISPLAY_DOCUMENT + "/" + id);
	}

	public void appManagement() {
		vaadinNavigator.navigateTo(NavigatorConfigurationService.APP_MANAGEMENT);
	}

	public void addGlobalGroup(String params) {
		vaadinNavigator.navigateTo(NavigatorConfigurationService.GROUP_ADD_EDIT + "/" + params);
	}

	public void editTaxonomyConcept(String taxonomyCode, String conceptId, String schemaCode) {
		vaadinNavigator.navigateTo(
				NavigatorConfigurationService.TAXONOMY_CONCEPT_ADD_EDIT + "/" + AddEditTaxonomyConceptPresenter.EDIT + "/"
						+ taxonomyCode + "/" + schemaCode + "/" + conceptId);
	}

	public void addTaxonomyConcept(String taxonomyCode, String conceptId, String schemaCode) {
		String url = NavigatorConfigurationService.TAXONOMY_CONCEPT_ADD_EDIT + "/" + AddEditTaxonomyConceptPresenter.ADD + "/"
				+ taxonomyCode + "/" + schemaCode;
		if (conceptId != null) {
			url += "/" + conceptId;
		}
		vaadinNavigator.navigateTo(url);

	}

	public void addTaxonomy() {
		vaadinNavigator.navigateTo(NavigatorConfigurationService.TAXONOMY_ADD_EDIT);
	}

	public void editTaxonomy(String taxonomyCode) {
		Map<String, String> params = new java.util.HashMap<>();
		params.put("taxonomyCode", taxonomyCode);
		String fragment = ParamUtils.addParams(NavigatorConfigurationService.TAXONOMY_ADD_EDIT, params);
		vaadinNavigator.navigateTo(fragment);
	}

	public void listTaxonomies() {
		vaadinNavigator.navigateTo(NavigatorConfigurationService.LIST_TAXONOMY);
	}

	public void taxonomyManagement(String taxonomyCode) {
		taxonomyManagement(taxonomyCode, null);
	}

	public void taxonomyManagement(String taxonomyCode, String conceptId) {
		Map<String, String> params = new HashMap<>();
		params.put(TaxonomyManagementPresenter.TAXONOMY_CODE, taxonomyCode);
		if (conceptId != null) {
			params.put(TaxonomyManagementPresenter.CONCEPT_ID, conceptId);
		}
		String viewPath = ParamUtils.addParams(NavigatorConfigurationService.TAXONOMY_MANAGEMENT, params);
		vaadinNavigator.navigateTo(viewPath);
	}

	public void collectionSecurity() {
		vaadinNavigator.navigateTo(NavigatorConfigurationService.COLLECTION_USER_LIST);
	}

	public void displayCollectionUser(String entityId) {
		vaadinNavigator.navigateTo(NavigatorConfigurationService.COLLECTION_USER + "/" + entityId);
	}

	public void editCollectionUserRoles(String enityId) {
		vaadinNavigator.navigateTo(NavigatorConfigurationService.COLLECTION_USER_ROLES + "/" + enityId);
	}

	public void displayCollectionGroup(String entityId) {
		vaadinNavigator.navigateTo(NavigatorConfigurationService.COLLECTION_GROUP + "/" + entityId);
	}

	public void editCollectionGroupRoles(String entityId) {
		vaadinNavigator.navigateTo(NavigatorConfigurationService.COLLECTION_GROUP_ROLES + "/" + entityId);
	}

	public void permissionManagement() {
		vaadinNavigator.navigateTo(NavigatorConfigurationService.PERMISSION_MANAGEMENT);
	}

	public void listValueDomains() {
		vaadinNavigator.navigateTo(NavigatorConfigurationService.LIST_VALUE_DOMAINS);
	}

	public void searchSchemaRecords(String schemaCode, String queryExpression) {
		Map<String, String> params = new HashMap<>();
		params.put("q", queryExpression);
		params.put("schemaCode", schemaCode);
		String viewPath = ParamUtils.addParams(NavigatorConfigurationService.SEARCH_SCHEMA_RECORDS, params);
		vaadinNavigator.navigateTo(viewPath);
	}

	public void simpleSearch(String queryExpression) {
		vaadinNavigator.navigateTo(NavigatorConfigurationService.SIMPLE_SEARCH + "/q/" + queryExpression);
	}

	public void simpleSearch(String queryExpression, int pageNumber) {
		vaadinNavigator.navigateTo(NavigatorConfigurationService.SIMPLE_SEARCH + "/q/" + queryExpression + "/" + pageNumber);
	}

	public void simpleSearchReplay(String searchId) {
		vaadinNavigator.navigateTo(NavigatorConfigurationService.SIMPLE_SEARCH + "/s/" + searchId);
	}

	public void advancedSearch() {
		vaadinNavigator.navigateTo(NavigatorConfigurationService.ADVANCED_SEARCH);
	}

	public void advancedSearchReplay(String searchId) {
		vaadinNavigator.navigateTo(NavigatorConfigurationService.ADVANCED_SEARCH + "/s/" + searchId);
	}

	public void taxonomySearch(String taxonomyCode, String queryExpression) {
		Map<String, String> params = new HashMap<>();
		params.put("taxonomyCode", taxonomyCode);
		params.put("q", queryExpression);
		String viewPath = ParamUtils.addParams(NavigatorConfigurationService.TAXONOMY_SEARCH, params);
		vaadinNavigator.navigateTo(viewPath);
	}

	public void retentionRulesSearch(String queryExpression) {
		vaadinNavigator.navigateTo(NavigatorConfigurationService.RETENTION_RULES_SEARCH + "/" + queryExpression);
	}

	public void listGlobalGroups() {
		vaadinNavigator.navigateTo(NavigatorConfigurationService.GROUP_LIST);
	}

	public void editGlobalGroup(String params) {
		vaadinNavigator.navigateTo(NavigatorConfigurationService.GROUP_ADD_EDIT + "/" + params);
	}

	public void displayGlobalGroup(String params) {
		vaadinNavigator.navigateTo(NavigatorConfigurationService.GROUP_DISPLAY + "/" + params);
	}

	public void listUserCredentials() {
		vaadinNavigator.navigateTo(NavigatorConfigurationService.USER_LIST);
	}

	public void displayUserCredential(String params) {
		vaadinNavigator.navigateTo(NavigatorConfigurationService.USER_DISPLAY + "/" + params);
	}

	public void showEventCategory(EventCategory specificEvent) {
		vaadinNavigator.navigateTo(NavigatorConfigurationService.EVENT_CATEGORY + "/" + specificEvent);
	}

	public void showEventCategory(Map<String, Object> params) {
		String viewWithParamsString = ParamUtils.addParams(NavigatorConfigurationService.EVENT_CATEGORY, params);
		vaadinNavigator.navigateTo(viewWithParamsString);
	}

	public void showEvent(Map<String, Object> params) {
		String viewWithParamsString = ParamUtils.addParams(NavigatorConfigurationService.EVENT_DISPLAY, params);
		vaadinNavigator.navigateTo(viewWithParamsString);
	}

	public void editUserCredential(String params) {
		vaadinNavigator.navigateTo(NavigatorConfigurationService.USER_ADD_EDIT + "/" + params);
	}

	public void addSchemaRecord(String schema) {
		vaadinNavigator.navigateTo(NavigatorConfigurationService.ADD_EDIT_SCHEMA_RECORD + "/" + schema);
	}

	public void editSchemaRecord(String schemaName, String recordId) {
		vaadinNavigator.navigateTo(NavigatorConfigurationService.ADD_EDIT_SCHEMA_RECORD + "/" + schemaName + "/" + recordId);
	}

	public void displaySchemaRecord(String recordId) {
		vaadinNavigator.navigateTo(NavigatorConfigurationService.DISPLAY_SCHEMA_RECORD + "/" + recordId);
	}

	public void listSchemaRecords(String schema) {
		vaadinNavigator.navigateTo(NavigatorConfigurationService.LIST_SCHEMA_RECORDS + "/" + schema);
	}

	public void addUserCredential(String params) {
		vaadinNavigator.navigateTo(NavigatorConfigurationService.USER_ADD_EDIT + "/" + params);
	}

	public void url(String url) {
		vaadinNavigator.navigateTo(url);
	}

	public void adminModule() {
		vaadinNavigator.navigateTo(NavigatorConfigurationService.ADMIN_MODULE);
	}

	public void reports() {
		vaadinNavigator.navigateTo(NavigatorConfigurationService.REPORTS);
	}

	public void listPrincipalAccessAuthorizations(String entityId) {
		vaadinNavigator.navigateTo(NavigatorConfigurationService.LIST_PRINCIPAL_ACCESS_AUTHORIZATIONS + "/" + entityId);
	}

	public void listObjectAccessAuthorizations(String entityId) {
		vaadinNavigator.navigateTo(NavigatorConfigurationService.LIST_OBJECT_ACCESS_AUTHORIZATIONS + "/" + entityId);
	}

	public void listObjectRoleAuthorizations(String entityId) {
		vaadinNavigator.navigateTo(NavigatorConfigurationService.LIST_OBJECT_ROLE_AUTHORIZATIONS + "/" + entityId);
	}

	public void shareContent(String entityId) {
		vaadinNavigator.navigateTo(NavigatorConfigurationService.SHARE_CONTENT + "/" + entityId);
	}

	public void archivesManagement() {
		vaadinNavigator.navigateTo(NavigatorConfigurationService.ARCHIVES_MANAGEMENT);
	}

	public void decommissioning() {
		vaadinNavigator.navigateTo(NavigatorConfigurationService.DECOMMISSIONING);
	}

	public void decommissioningListBuilder(String type) {
		vaadinNavigator.navigateTo(NavigatorConfigurationService.DECOMMISSIONING_LIST_BUILDER + "/" + type);
	}

	public void displayDecommissioningList(String entityId) {
		vaadinNavigator.navigateTo(NavigatorConfigurationService.DECOMMISSIONING_LIST_DISPLAY + "/" + entityId);
	}

	public void displayDocumentDecommissioningList(String entityId) {
		vaadinNavigator.navigateTo(NavigatorConfigurationService.DOCUMENT_DECOMMISSIONING_LIST_DISPLAY + "/" + entityId);
	}

	public void editDecommissioningList(String entityId) {
		vaadinNavigator.navigateTo(NavigatorConfigurationService.DECOMMISSIONING_LIST_EDIT + "/" + entityId);
	}

	public void createContainerForDecommissioningList(String entityId) {
		vaadinNavigator.navigateTo(NavigatorConfigurationService.DECOMMISSIONING_LIST_ADD_NEW_CONTAINER + "/" + entityId);
	}

	public void searchContainerForDecommissioningList(String entityId) {
		vaadinNavigator.navigateTo(NavigatorConfigurationService.DECOMMISSIONING_LIST_ADD_EXISTING_CONTAINER + "/" + entityId);
	}

	public void configManagement() {
		vaadinNavigator.navigateTo(NavigatorConfigurationService.CONFIG_MANAGEMENT);
	}

	public void pluginManagement() {
		vaadinNavigator.navigateTo(NavigatorConfigurationService.PLUGIN_MANAGEMENT);
	}

	public void emailServerManagement() {
		vaadinNavigator.navigateTo(NavigatorConfigurationService.EMAIL_SERVER_MANAGEMENT);
	}

	public void ldapConfigManagement() {
		vaadinNavigator.navigateTo(NavigatorConfigurationService.LDAP_CONFIG_MANAGEMENT);
	}

	public void listEvents() {
		vaadinNavigator.navigateTo(NavigatorConfigurationService.EVENTS_LIST);
	}

	public void listSchema(String params) {
		vaadinNavigator.navigateTo(NavigatorConfigurationService.DISPLAY_SCHEMA + "/" + params);
	}

	public void listSchemaTypes() {
		vaadinNavigator.navigateTo(NavigatorConfigurationService.DISPLAY_SCHEMA_TYPE);
	}

	public void editMetadata(String params) {
		vaadinNavigator.navigateTo(NavigatorConfigurationService.ADD_EDIT_METADATA + "/" + params);
	}

	public void addMetadata(String params) {
		vaadinNavigator.navigateTo(NavigatorConfigurationService.ADD_EDIT_METADATA + "/" + params);
	}

	public void listSchemaMetadata(String params) {
		vaadinNavigator.navigateTo(NavigatorConfigurationService.ADD_EDIT_SCHEMA_METADATA + "/" + params);
	}

	public void addSchema(String params) {
		vaadinNavigator.navigateTo(NavigatorConfigurationService.ADD_EDIT_SCHEMA + "/" + params);
	}

	public void editSchema(String params) {
		vaadinNavigator.navigateTo(NavigatorConfigurationService.ADD_EDIT_SCHEMA + "/" + params);
	}

	public void editDisplayForm(String schemaCode) {
		vaadinNavigator.navigateTo(NavigatorConfigurationService.EDIT_DISPLAY_FORM + "/" + schemaCode);
	}

	public void searchDisplayForm(String schemaCode) {
		vaadinNavigator.navigateTo(NavigatorConfigurationService.SEARCH_DISPLAY_FORM + "/" + schemaCode);
	}

	public void tableDisplayForm(String schemaCode) {
		vaadinNavigator.navigateTo(NavigatorConfigurationService.TABLE_DISPLAY_FORM + "/" + schemaCode);
	}

	public void reportDisplayForm(String schemaCode) {
		vaadinNavigator.navigateTo(NavigatorConfigurationService.REPORT_DISPLAY_FORM + "/" + schemaCode);
	}

	public void formDisplayForm(String schemaCode) {
		vaadinNavigator.navigateTo(NavigatorConfigurationService.FORM_DISPLAY_FORM + "/" + schemaCode);
	}

	public void containersByAdministrativeUnits() {
		vaadinNavigator.navigateTo(NavigatorConfigurationService.CONTAINERS_BY_ADMIN_UNITS);
	}

	public void displayAdminUnitWithContainers(String tabName, String entityId) {
		vaadinNavigator
				.navigateTo(NavigatorConfigurationService.DISPLAY_ADMIN_UNIT_WITH_CONTAINERS + "/" + tabName + "/" + entityId);
	}

	public void displayFilingSpaceWithContainers(String tabName, String adminUnitId, String filingSpaceId) {
		vaadinNavigator.navigateTo(
				NavigatorConfigurationService.DISPLAY_FILING_SPACE_WITH_CONTAINERS + "/" + tabName + "/" + adminUnitId + "/"
						+ filingSpaceId);
	}

	public void displayContainer(String containerId) {
		vaadinNavigator.navigateTo(NavigatorConfigurationService.DISPLAY_CONTAINER + "/" + containerId);
	}

	public void addContainer() {
		vaadinNavigator.navigateTo(NavigatorConfigurationService.EDIT_CONTAINER);
	}

	public void editContainer(String containerId) {
		vaadinNavigator.navigateTo(NavigatorConfigurationService.EDIT_CONTAINER + "/" + containerId);
	}

	public void listRetentionRules() {
		vaadinNavigator.navigateTo(NavigatorConfigurationService.LIST_RETENTION_RULES);
	}

	public void displayRetentionRule(String id) {
		vaadinNavigator.navigateTo(NavigatorConfigurationService.DISPLAY_RETENTION_RULE + "/" + id);
	}

	public void addRetentionRule() {
		vaadinNavigator.navigateTo(NavigatorConfigurationService.ADD_RETENTION_RULE);
	}

	public void editRetentionRule(String id) {
		vaadinNavigator.navigateTo(NavigatorConfigurationService.EDIT_RETENTION_RULE + "/" + id);
	}

	public void listUserDocuments() {
		vaadinNavigator.navigateTo(NavigatorConfigurationService.LIST_USER_DOCUMENTS);
	}

	public void declareUserDocument(String userDocumentId) {
		Map<String, String> params = new HashMap<>();
		params.put("userDocumentId", userDocumentId);
		String viewPath = ParamUtils.addParams(NavigatorConfigurationService.ADD_DOCUMENT, params);
		vaadinNavigator.navigateTo(viewPath);
	}

	public void manageCollections() {
		vaadinNavigator.navigateTo(NavigatorConfigurationService.COLLECTION_MANAGEMENT);
	}

	public void addEditCollection(String params) {
		vaadinNavigator.navigateTo(NavigatorConfigurationService.COLLECTION_ADD_EDIT + "/" + params);
	}

	public void listTabDisplayForm(String params) {
		vaadinNavigator.navigateTo(NavigatorConfigurationService.LIST_ONGLET + "/" + params);
	}

	public void modifyProfil(String params) {
		String path = NavigatorConfigurationService.MODIFY_PROFILE;
		if (params != null) {
			path += "/" + params;
		}
		vaadinNavigator.navigateTo(path);
	}

	public void updateManager() {
		vaadinNavigator.navigateTo(NavigatorConfigurationService.UPDATE_MANAGER);
	}

	public void importRecords() {
		vaadinNavigator.navigateTo(NavigatorConfigurationService.IMPORT_FILE);
	}

	public void importUsers() {
		vaadinNavigator.navigateTo(NavigatorConfigurationService.IMPORT_USERS);
	}

	public void importAuthorizations() {
		vaadinNavigator.navigateTo(NavigatorConfigurationService.IMPORT_AUTHORIZATIONS);
	}

	public void importGroups() {
		vaadinNavigator.navigateTo(NavigatorConfigurationService.IMPORT_GROUPS);
	}

	public void importSchemaTypes() {
		vaadinNavigator.navigateTo(NavigatorConfigurationService.IMPORT_SCHEMA_TYPES);
	}

	public void exporter() {
		vaadinNavigator.navigateTo(NavigatorConfigurationService.EXPORTER);
	}

	public void agentRequestPage() {
		vaadinNavigator.navigateTo(NavigatorConfigurationService.REQUEST_AGENT);
	}

	public void agentSetup() {
		vaadinNavigator.navigateTo(NavigatorConfigurationService.AGENT_SETUP);
	}

	public void listAgentLogs() {
		vaadinNavigator.navigateTo(NavigatorConfigurationService.LIST_AGENT_LOGS);
	}

	public void editFacetConfiguration(String params) {
		vaadinNavigator.navigateTo(NavigatorConfigurationService.EDIT_FACET_CONFIGURATION + "/" + params);
	}

	public void addFacetConfiguration() {
		vaadinNavigator.navigateTo(NavigatorConfigurationService.ADD_FACET_CONFIGURATION);
	}

	public void orderFacetConfiguration() {
		vaadinNavigator.navigateTo(NavigatorConfigurationService.ORDER_FACET_CONFIGURATION);
	}

	public void listFacetConfiguration() {
		vaadinNavigator.navigateTo(NavigatorConfigurationService.LIST_FACET_CONFIGURATION);
	}

	public void displayFacetConfiguration(String params) {
		vaadinNavigator.navigateTo(NavigatorConfigurationService.DISPLAY_FACET_CONFIGURATION + "/" + params);
	}

	public void listSavedSearches() {
		vaadinNavigator.navigateTo(NavigatorConfigurationService.LIST_SAVED_SEARCHES);
	}

	public void searchBoostByMetadatas() {
		vaadinNavigator.navigateTo(NavigatorConfigurationService.SEARCH_BOOST_BY_METADATAS);
	}

	public void searchBoostByQuerys() {
		vaadinNavigator.navigateTo(NavigatorConfigurationService.SEARCH_BOOST_BY_QUERYS);
	}

	//Module ES
	public void listConnectorInstances() {
		vaadinNavigator.navigateTo(NavigatorConfigurationService.LIST_CONNECTOR_INSTANCES);
	}

	public void displayConnectorInstance(String params) {
		vaadinNavigator.navigateTo(NavigatorConfigurationService.DISPLAY_CONNECTOR_INSTANCE + "/" + params);
	}

	public void wizardConnectorInstance() {
		vaadinNavigator.navigateTo(NavigatorConfigurationService.WIZARD_CONNECTOR_INSTANCE);
	}

	public void editConnectorInstances(String params) {
		vaadinNavigator.navigateTo(NavigatorConfigurationService.EDIT_CONNECTOR_INSTANCE + "/" + params);
	}

	public void connectorIndexationReport(String connectorId) {
		Map<String, String> params = new HashMap<>();
		if (connectorId != null) {
			params.put(ConnectorReportView.CONNECTOR_ID, connectorId);
		}
		params.put(ConnectorReportView.REPORT_MODE, ConnectorReportView.INDEXATION);
		String viewPath = ParamUtils.addParams(NavigatorConfigurationService.CONNECTOR_REPORT, params);
		vaadinNavigator.navigateTo(viewPath);
	}

	public void connectorErrorsReport(String connectorId) {
		Map<String, String> params = new HashMap<>();
		if (connectorId != null) {
			params.put(ConnectorReportView.CONNECTOR_ID, connectorId);
		}
		params.put(ConnectorReportView.REPORT_MODE, ConnectorReportView.ERRORS);
		String viewPath = ParamUtils.addParams(NavigatorConfigurationService.CONNECTOR_REPORT, params);
		vaadinNavigator.navigateTo(viewPath);
	}

	//tasks module
	public void tasksManagement() {
		vaadinNavigator.navigateTo(NavigatorConfigurationService.TASKS_MANAGEMENT);
	}

	public void addTask(String parentTaskId) {
		Map<String, String> params = new HashMap<>();
		if (parentTaskId != null) {
			params.put("parentId", parentTaskId);
		}
		String viewPath = ParamUtils.addParams(NavigatorConfigurationService.ADD_TASK, params);
		vaadinNavigator.navigateTo(viewPath);
	}

	public void editTask(String taskId, String workflowId, boolean completeTask) {
		Map<String, String> params = new HashMap<>();
		params.put("id", taskId);
		if (completeTask) {
			params.put("completeTask", "" + true);
		}
		if (StringUtils.isNotBlank(workflowId)) {
			params.put("workflowId", workflowId);
		}
		String viewPath = ParamUtils.addParams(NavigatorConfigurationService.EDIT_TASK, params);
		vaadinNavigator.navigateTo(viewPath);
	}

	public void editTask(String taskId, boolean completeTask) {
		editTask(taskId, null, completeTask);
	}

	public void editTask(String taskId, String workflowId) {
		editTask(taskId, workflowId, false);
	}

	public void editTask(String taskId) {
		editTask(taskId, null);
	}

	public void displayTask(String id) {
		vaadinNavigator.navigateTo(NavigatorConfigurationService.DISPLAY_TASK + "/" + id);
	}

	public void listWorkflows() {
		vaadinNavigator.navigateTo(NavigatorConfigurationService.LIST_WORKFLOWS);
	}

	public void addWorkflow() {
		vaadinNavigator.navigateTo(NavigatorConfigurationService.ADD_WORKFLOW);
	}

	public void displayWorkflow(String id) {
		vaadinNavigator.navigateTo(NavigatorConfigurationService.DISPLAY_WORKFLOW + "/" + id);
	}

	public void editWorkflow(String id) {
		vaadinNavigator.navigateTo(NavigatorConfigurationService.EDIT_WORKFLOW + "/" + id);
	}

	public void displayWorkflowInstance(String id) {
		vaadinNavigator.navigateTo(NavigatorConfigurationService.DISPLAY_WORKFLOW_INSTANCE + "/" + id);
	}

	public void displayConnectorMappings(String entityId) {
		vaadinNavigator.navigateTo(NavigatorConfigurationService.DISPLAY_CONNECTOR_MAPPINGS + "/" + entityId);
	}

	public void addConnectorMapping(String connectorInstance, String documentType) {
		vaadinNavigator.navigateTo(NavigatorConfigurationService.ADD_CONNECTOR_MAPPING +
				"/" + connectorInstance + "/" + documentType);
	}

	public void editConnectorMapping(String connectorInstance, String documentType, String metadata) {
		vaadinNavigator.navigateTo(NavigatorConfigurationService.ADD_CONNECTOR_MAPPING +
				"/" + connectorInstance + "/" + documentType + "/" + metadata);
	}

	public void listTasksLogs() {
		vaadinNavigator.navigateTo(NavigatorConfigurationService.LIST_TASKS_LOGS);
	}

	public void listMetadataExtractors() {
		vaadinNavigator.navigateTo(NavigatorConfigurationService.LIST_METADATA_EXTRACTORS);
	}

	public void addMetadataExtractor() {
		vaadinNavigator.navigateTo(NavigatorConfigurationService.ADD_METADATA_EXTRACTOR);
	}

	public void editMetadataExtractor(String metadataCode) {
		vaadinNavigator.navigateTo(NavigatorConfigurationService.EDIT_METADATA_EXTRACTOR + "/" + metadataCode);
	}

	// Robots module
	public void listRootRobots() {
		vaadinNavigator.navigateTo(NavigatorConfigurationService.LIST_ROOT_ROBOTS);
	}

	public void robotConfiguration(String rootRobotId) {
		vaadinNavigator.navigateTo(NavigatorConfigurationService.ROBOT_CONFIGURATION + "/" + rootRobotId);
	}

	public void addRobot(String parentId) {
		Map<String, String> params = new HashMap<>();
		params.put("pageMode", "add");
		if (parentId != null) {
			params.put("parentId", parentId);
		}
		String viewPath = ParamUtils.addParams(NavigatorConfigurationService.ADD_EDIT_ROBOT, params);
		vaadinNavigator.navigateTo(viewPath);
	}

	public void editRobot(String robotId) {
		Map<String, String> params = new HashMap<>();
		params.put("pageMode", "edit");
		params.put("robotId", robotId);
		String viewPath = ParamUtils.addParams(NavigatorConfigurationService.ADD_EDIT_ROBOT, params);
		vaadinNavigator.navigateTo(viewPath);
	}

	public void displayRobotLogs(String entityId) {
		vaadinNavigator.navigateTo(NavigatorConfigurationService.ROBOT_LOGS + "/" + entityId);
	}

	public void addEmailAttachmentsToFolder(String userDocumentId) {
		Map<String, String> params = new HashMap<>();
		params.put("userDocumentId", userDocumentId);
		String viewPath = ParamUtils.addParams(NavigatorConfigurationService.ADD_EMAIL_ATTACHMENTS_TO_FOLDER, params);
		vaadinNavigator.navigateTo(viewPath);
	}
}
