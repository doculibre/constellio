package com.constellio.app.modules.rm.navigation;

import static com.constellio.app.ui.params.ParamUtils.addParams;

import java.util.Map;

import com.constellio.app.ui.application.CoreViews;
import com.constellio.app.ui.application.NavigatorConfigurationService;
import com.google.gwt.dev.util.collect.HashMap;
import com.vaadin.navigator.Navigator;

public class RMViews extends CoreViews {
	public RMViews(Navigator navigator) {
		super(navigator);
	}

	// FOLDER MANAGEMENT

	public void displayFolder(String id) {
		navigator.navigateTo(NavigatorConfigurationService.DISPLAY_FOLDER + "/" + id);
	}

	public void addFolder() {
		addFolder(null, null);
	}

	public void addFolder(String parentFolderId) {
		addFolder(parentFolderId, null);
	}

	public void addFolder(String parentFolderId, String typeId) {
		Map<String, String> params = new HashMap<>();
		if (parentFolderId != null) {
			params.put("parentId", parentFolderId);
		}
		if (typeId != null) {
			params.put("typeId", typeId);
		}
		navigator.navigateTo(addParams(NavigatorConfigurationService.ADD_FOLDER, params));
	}

	public void editFolder(String id) {
		Map<String, String> params = new HashMap<>();
		params.put("id", id);
		navigator.navigateTo(addParams(NavigatorConfigurationService.EDIT_FOLDER, params));
	}

	// DOCUMENT MANAGEMENT

	public void displayDocument(String id) {
		navigator.navigateTo(NavigatorConfigurationService.DISPLAY_DOCUMENT + "/" + id);
	}

	public void addDocument() {
		addDocument(null, null);
	}

	public void addDocument(String folderId) {
		addDocument(folderId, null);
	}

	public void addDocument(String folderId, String typeId) {
		Map<String, String> params = new HashMap<>();
		if (folderId != null) {
			params.put("parentId", folderId);
		}
		if (typeId != null) {
			params.put("typeId", typeId);
		}
		navigator.navigateTo(addParams(NavigatorConfigurationService.ADD_DOCUMENT, params));
	}

	public void addDocumentWithContent(String id) {
		Map<String, String> params = new HashMap<>();
		params.put("idCopy", id);
		navigator.navigateTo(addParams(NavigatorConfigurationService.ADD_DOCUMENT, params));
	}

	public void editDocument(String id) {
		Map<String, String> params = new HashMap<>();
		params.put("id", id);
		navigator.navigateTo(addParams(NavigatorConfigurationService.EDIT_DOCUMENT, params));
	}

	public void editDocument(String id, String userDocumentId) {
		Map<String, String> params = new HashMap<>();
		params.put("id", id);
		params.put("userDocumentId", userDocumentId);
		navigator.navigateTo(addParams(NavigatorConfigurationService.EDIT_DOCUMENT, params));
	}

	// RETENTION RULES

	public void listRetentionRules() {
		navigator.navigateTo(NavigatorConfigurationService.LIST_RETENTION_RULES);
	}

	public void retentionRuleSearch(String queryExpression) {
		navigator.navigateTo(NavigatorConfigurationService.RETENTION_RULES_SEARCH + "/" + queryExpression);
	}

	// ARCHIVE MANAGEMENT

	public void archiveManagement() {
		navigator.navigateTo(NavigatorConfigurationService.ARCHIVES_MANAGEMENT);
	}

	// DECOMMISSIONING

	public void decommissioning() {
		navigator.navigateTo(NavigatorConfigurationService.DECOMMISSIONING);
	}

	public void decommissioningListBuilder(String type) {
		navigator.navigateTo(NavigatorConfigurationService.DECOMMISSIONING_LIST_BUILDER + "/" + type);
	}

	public void displayDecommissioningList(String entityId) {
		navigator.navigateTo(NavigatorConfigurationService.DECOMMISSIONING_LIST_DISPLAY + "/" + entityId);
	}

	public void displayDocumentDecommissioningList(String entityId) {
		navigator.navigateTo(NavigatorConfigurationService.DOCUMENT_DECOMMISSIONING_LIST_DISPLAY + "/" + entityId);
	}

	// USER DOCUMENTS

	public void listUserDocuments() {
		navigator.navigateTo(NavigatorConfigurationService.LIST_USER_DOCUMENTS);
	}

	// CART

	public void cart() {
		navigator.navigateTo(NavigatorConfigurationService.CART);
	}

	// AUDIT EVENTS

	public void eventAudit() {
		navigator.navigateTo(NavigatorConfigurationService.EVENTS_LIST);
	}

	// AGENT

	public void requestAgent() {
		navigator.navigateTo(NavigatorConfigurationService.REQUEST_AGENT);
	}
}
