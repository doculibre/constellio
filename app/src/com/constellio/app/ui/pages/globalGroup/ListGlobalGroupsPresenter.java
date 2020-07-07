package com.constellio.app.ui.pages.globalGroup;

import com.constellio.app.ui.application.NavigatorConfigurationService;
import com.constellio.app.ui.entities.GlobalGroupVO;
import com.constellio.app.ui.framework.builders.GlobalGroupToVOBuilder;
import com.constellio.app.ui.framework.data.GlobalGroupVODataProvider;
import com.constellio.app.ui.pages.base.BasePresenter;
import com.constellio.app.ui.params.ParamUtils;
import com.constellio.model.entities.CorePermissions;
import com.constellio.model.entities.records.wrappers.User;
import com.constellio.model.entities.security.global.GlobalGroup;
import com.constellio.model.entities.security.global.UserCredential;
import com.constellio.model.services.users.UserServices;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ListGlobalGroupsPresenter extends BasePresenter<ListGlobalGroupsView> {

	private transient UserServices userServices;

	public static final String ACTIVE = "active";
	public static final String INACTIVE = "inactive";


	public ListGlobalGroupsPresenter(ListGlobalGroupsView view) {
		super(view);
		init();
	}

	public List<String> getTabs() {
		List<String> tabs = new ArrayList<>();
		tabs.add(ACTIVE);
		tabs.add(INACTIVE);

		return tabs;
	}

	public String getTabCaption(String tabId) {
		switch (tabId) {
			case ACTIVE:
				return "Actif";
			case INACTIVE:
				return "Inactif";
			default:
				return null;
		}
	}

	private void init() {
		userServices = modelLayerFactory.newUserServices();
	}

	public GlobalGroupVODataProvider getDataProvider() {
		GlobalGroupToVOBuilder voBuilder = new GlobalGroupToVOBuilder();
		return new GlobalGroupVODataProvider(voBuilder, modelLayerFactory, true);
	}

	public void addButtonClicked() {
		String params = ParamUtils.addParams(NavigatorConfigurationService.GROUP_LIST, null);
		view.navigate().to().addGlobalGroup(params);
	}

	public void editButtonClicked(GlobalGroupVO entity) {
		String parameters = getParameters(entity);
		view.navigate().to().editGlobalGroup(parameters);
	}

	public void displayButtonClicked(GlobalGroupVO entity) {
		String parameters = getParameters(entity);
		view.navigate().to().displayGlobalGroup(parameters);
	}

	public void backButtonClicked() {
		view.navigate().to().adminModule();
	}

	public void deleteButtonClicked(GlobalGroupVO globalGroupVO) {
		UserServices userServices = modelLayerFactory.newUserServices();
		String username = view.getSessionContext().getCurrentUser().getUsername();
		UserCredential userCredential = userServices.getUserCredential(username);
		GlobalGroup globalGroup = userServices.getGroup(globalGroupVO.getCode());
		userServices.logicallyRemoveGroupHierarchy(userCredential, globalGroup);
		view.refreshTable();
	}

	private String getParameters(GlobalGroupVO entity) {
		Map<String, Object> params = new HashMap<>();
		params.put("globalGroupCode", entity.getCode());
		return ParamUtils.addParams(NavigatorConfigurationService.GROUP_LIST, params);
	}

	public boolean canAddOrModify() {
		return userServices.canAddOrModifyUserAndGroup();
	}

	@Override
	protected boolean hasPageAccess(String params, User user) {
		return userServices.has(user).globalPermissionInAnyCollection(CorePermissions.MANAGE_SYSTEM_GROUPS);
	}

	public boolean isSystemAdmin() {
		return userServices.isSystemAdmin(getCurrentUser());
	}
}
