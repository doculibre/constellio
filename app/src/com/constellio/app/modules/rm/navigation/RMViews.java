package com.constellio.app.modules.rm.navigation;

import static com.constellio.app.ui.params.ParamUtils.addParams;

import java.util.Map;

import com.constellio.app.modules.rm.ui.components.breadcrumb.FolderDocumentBreadcrumbTrail;
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
		navigator.navigateTo(RMNavigationConfiguration.DISPLAY_FOLDER + "/" + id);
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
		navigator.navigateTo(addParams(RMNavigationConfiguration.ADD_FOLDER, params));
	}

	public void editFolder(String id) {
		Map<String, String> params = new HashMap<>();
		params.put("id", id);
		navigator.navigateTo(addParams(RMNavigationConfiguration.EDIT_FOLDER, params));
	}

	// DOCUMENT MANAGEMENT

	public void displayDocument(String id) {
		navigator.navigateTo(RMNavigationConfiguration.DISPLAY_DOCUMENT + "/" + id);
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
		navigator.navigateTo(addParams(RMNavigationConfiguration.ADD_DOCUMENT, params));
	}

	public void addDocumentWithContent(String id) {
		Map<String, String> params = new HashMap<>();
		params.put("idCopy", id);
		navigator.navigateTo(addParams(RMNavigationConfiguration.ADD_DOCUMENT, params));
	}

	public void editDocument(String id) {
		Map<String, String> params = new HashMap<>();
		params.put("id", id);
		navigator.navigateTo(addParams(RMNavigationConfiguration.EDIT_DOCUMENT, params));
	}

	public void editDocument(String id, String userDocumentId) {
		Map<String, String> params = new HashMap<>();
		params.put("id", id);
		params.put("userDocumentId", userDocumentId);
		navigator.navigateTo(addParams(RMNavigationConfiguration.EDIT_DOCUMENT, params));
	}

	// RETENTION RULES

	public void listRetentionRules() {
		navigator.navigateTo(RMNavigationConfiguration.LIST_RETENTION_RULES);
	}

	public void retentionRuleSearch(String queryExpression) {
		navigator.navigateTo(RMNavigationConfiguration.RETENTION_RULES_SEARCH + "/" + queryExpression);
	}

	// ARCHIVE MANAGEMENT

	public void archiveManagement() {
		navigator.navigateTo(RMNavigationConfiguration.ARCHIVES_MANAGEMENT);
	}

	// DECOMMISSIONING

	public void decommissioning() {
		navigator.navigateTo(RMNavigationConfiguration.DECOMMISSIONING);
	}

	public void decommissioningListBuilder(String type) {
		navigator.navigateTo(RMNavigationConfiguration.DECOMMISSIONING_LIST_BUILDER + "/" + type);
	}

	public void decommissioningListBuilderReplay(String type, String searchId) {
		navigator.navigateTo(RMNavigationConfiguration.DECOMMISSIONING_LIST_BUILDER + "/" + type + "/s/" + searchId);
	}

	public void displayDecommissioningList(String entityId) {
		navigator.navigateTo(RMNavigationConfiguration.DECOMMISSIONING_LIST_DISPLAY + "/" + entityId);
	}

	public void displayDocumentDecommissioningList(String entityId) {
		navigator.navigateTo(RMNavigationConfiguration.DOCUMENT_DECOMMISSIONING_LIST_DISPLAY + "/" + entityId);
	}

	// USER DOCUMENTS

	public void listUserDocuments() {
		navigator.navigateTo(RMNavigationConfiguration.LIST_USER_DOCUMENTS);
	}

	// CARTS

	public void listCarts() {
		navigator.navigateTo(RMNavigationConfiguration.LIST_CARTS);
	}

	public void cart() {
		navigator.navigateTo(RMNavigationConfiguration.CART);
	}

	public void cart(String cartId) {
		navigator.navigateTo(RMNavigationConfiguration.CART + "/" + cartId);
	}

	// AUDIT EVENTS

	public void eventAudit() {
		navigator.navigateTo(NavigatorConfigurationService.EVENTS_LIST);
	}

	// AGENT

	public void requestAgent() {
		navigator.navigateTo(RMNavigationConfiguration.REQUEST_AGENT);
	}

	public void listAgentLogs() {
		navigator.navigateTo(RMNavigationConfiguration.LIST_AGENT_LOGS);
	}

	//REPORTS

	public void reports() {
		navigator.navigateTo(RMNavigationConfiguration.REPORTS);
	}

    //CONTAINERS

    public void addContainer() {
        navigator.navigateTo(RMNavigationConfiguration.EDIT_CONTAINER);
    }

    public void editContainer(String containerId) {
        navigator.navigateTo(RMNavigationConfiguration.EDIT_CONTAINER + "/" + containerId);
    }

    public void containersByAdministrativeUnits() {
        navigator.navigateTo(RMNavigationConfiguration.CONTAINERS_BY_ADMIN_UNITS);
    }

    public void displayAdminUnitWithContainers(String tabName, String entityId) {
        navigator
                .navigateTo(RMNavigationConfiguration.DISPLAY_ADMIN_UNIT_WITH_CONTAINERS + "/" + tabName + "/" + entityId);
    }

    public void displayFilingSpaceWithContainers(String tabName, String adminUnitId, String filingSpaceId) {
        navigator.navigateTo(
                RMNavigationConfiguration.DISPLAY_FILING_SPACE_WITH_CONTAINERS + "/" + tabName + "/" + adminUnitId + "/"
                        + filingSpaceId);
    }

    public void displayContainer(String containerId) {
        navigator.navigateTo(RMNavigationConfiguration.DISPLAY_CONTAINER + "/" + containerId);
    }

    //DECOMMISSIONING

    public void searchContainerForDecommissioningList(String entityId) {
        navigator.navigateTo(RMNavigationConfiguration.DECOMMISSIONING_LIST_ADD_EXISTING_CONTAINER + "/" + entityId);
    }

	public void searchContainerForDecommissioningListReplay(String entityId, String searchId) {
		navigator.navigateTo(RMNavigationConfiguration.DECOMMISSIONING_LIST_ADD_EXISTING_CONTAINER + "/" + entityId + "/s/" + searchId);
	}

    public void createContainerForDecommissioningList(String entityId) {
        navigator.navigateTo(RMNavigationConfiguration.DECOMMISSIONING_LIST_ADD_NEW_CONTAINER + "/" + entityId);
    }

    public void editDecommissioningList(String entityId) {
        navigator.navigateTo(RMNavigationConfiguration.DECOMMISSIONING_LIST_EDIT + "/" + entityId);
    }

    //RETENTION RULE

    public void addRetentionRule() {
        navigator.navigateTo(RMNavigationConfiguration.ADD_RETENTION_RULE);
    }

    public void editRetentionRule(String id) {
        navigator.navigateTo(RMNavigationConfiguration.EDIT_RETENTION_RULE + "/" + id);
    }

    public void displayRetentionRule(String id) {
        navigator.navigateTo(RMNavigationConfiguration.DISPLAY_RETENTION_RULE + "/" + id);
    }
}
