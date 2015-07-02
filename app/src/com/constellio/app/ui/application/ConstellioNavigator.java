/*Constellio Enterprise Information Management

Copyright (c) 2015 "Constellio inc."

This program is free software: you can redistribute it and/or modify
it under the terms of the GNU Affero General Public License as
published by the Free Software Foundation, either version 3 of the
License, or (at your option) any later version.

This program is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
GNU Affero General Public License for more details.

You should have received a copy of the GNU Affero General Public License
along with this program. If not, see <http://www.gnu.org/licenses/>.
*/
package com.constellio.app.ui.application;

import java.util.Map;

import com.constellio.app.ui.pages.events.EventCategory;
import com.constellio.app.ui.pages.management.taxonomy.AddEditTaxonomyConceptPresenter;
import com.constellio.app.ui.params.ParamUtils;
import com.google.gwt.dev.util.collect.HashMap;
import com.vaadin.navigator.Navigator;

public class ConstellioNavigator {
	Navigator vaadinNavigator;

	public String getState() {
		return vaadinNavigator.getState();
	}

	public ConstellioNavigator(Navigator vaadinNavigator) {
		this.vaadinNavigator = vaadinNavigator;
	}

	public void home() {
		vaadinNavigator.navigateTo(NavigatorConfigurationService.HOME);
	}

	public void recordsManagement() {
		vaadinNavigator.navigateTo(NavigatorConfigurationService.RECORDS_MANAGEMENT);
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
		taxonomyManagement(taxonomyCode, null, null);
	}

	public void taxonomyManagement(String taxonomyCode, String conceptId) {
		taxonomyManagement(taxonomyCode, conceptId, null);
	}

	public void taxonomyManagement(String taxonomyCode, String conceptId, String parentConceptId) {
		Map<String, String> params = new HashMap<>();
		params.put("taxonomyCode", taxonomyCode);
		if (conceptId != null) {
			params.put("conceptId", conceptId);
		}
		if (parentConceptId != null) {
			params.put("parentConceptId", parentConceptId);
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

	public void simpleSearch() {
		vaadinNavigator.navigateTo(NavigatorConfigurationService.SIMPLE_SEARCH);
	}

	public void simpleSearch(String queryExpression) {
		vaadinNavigator.navigateTo(NavigatorConfigurationService.SIMPLE_SEARCH + "/" + queryExpression);
	}

	public void simpleSearch(String queryExpression, int pageNumber) {
		vaadinNavigator.navigateTo(NavigatorConfigurationService.SIMPLE_SEARCH + "/" + queryExpression + "/" + pageNumber);
	}

	public void advancedSearch() {
		vaadinNavigator.navigateTo(NavigatorConfigurationService.ADVANCED_SEARCH);
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

	public void showUser(String id) {
		vaadinNavigator.navigateTo(NavigatorConfigurationService.USER_LIST + "/" + id);
	}

	public void showEvents(String id) {
		vaadinNavigator.navigateTo(NavigatorConfigurationService.EVENTS_LIST);
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

	public void listPrincipalAuthorizations(String entityId) {
		vaadinNavigator.navigateTo(NavigatorConfigurationService.LIST_PRINCIPAL_AUTHORIZATIONS + "/" + entityId);
	}

	public void listObjectAuthorizations(String entityId) {
		vaadinNavigator.navigateTo(NavigatorConfigurationService.LIST_OBJECT_AUTHORIZATIONS + "/" + entityId);
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

	public void ldapConfigManagement() {
		vaadinNavigator.navigateTo(NavigatorConfigurationService.LDAP_CONFIG_MANAGEMENT);
	}

	public void listEvents() {
		vaadinNavigator.navigateTo(NavigatorConfigurationService.EVENTS_LIST);
	}

	public void listSchema(String params) {
		vaadinNavigator.navigateTo(NavigatorConfigurationService.DISPLAY_SCHEMA + "/" + params);
	}

	public void listSchemaType() {
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

	public void importFile() {
		vaadinNavigator.navigateTo(NavigatorConfigurationService.IMPORT_FILE);
	}

	public void importUsers() {
		vaadinNavigator.navigateTo(NavigatorConfigurationService.IMPORT_USERS);
	}

	public void importSchemaTypes() {
		vaadinNavigator.navigateTo(NavigatorConfigurationService.IMPORT_SCHEMA_TYPES);
	}

	public void exporter() {
		vaadinNavigator.navigateTo(NavigatorConfigurationService.EXPORTER);
	}

	public void agentSetup() {
		vaadinNavigator.navigateTo(NavigatorConfigurationService.AGENT_SETUP);
	}

	public void listAgentLogs() {
		vaadinNavigator.navigateTo(NavigatorConfigurationService.LIST_AGENT_LOGS);
	}

	public void editContainer(String params) {
		vaadinNavigator.navigateTo(NavigatorConfigurationService.EDIT_CONTAINER + "/" + params);
	}
}
