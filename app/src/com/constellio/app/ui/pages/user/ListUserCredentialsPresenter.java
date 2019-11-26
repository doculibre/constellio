package com.constellio.app.ui.pages.user;

import com.constellio.app.ui.application.NavigatorConfigurationService;
import com.constellio.app.ui.entities.UserCredentialVO;
import com.constellio.app.ui.framework.builders.UserCredentialToVOBuilder;
import com.constellio.app.ui.framework.data.UserCredentialVODataProvider;
import com.constellio.app.ui.pages.base.BasePresenter;
import com.constellio.app.ui.params.ParamUtils;
import com.constellio.model.entities.CorePermissions;
import com.constellio.model.entities.records.wrappers.User;
import com.constellio.model.entities.security.global.UserCredential;
import com.constellio.model.services.users.UserServices;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.constellio.app.ui.i18n.i18n.$;

public class ListUserCredentialsPresenter extends BasePresenter<ListUsersCredentialsView> {
	private transient UserServices userServices;

	public static final String ACTIVE = "active";
	public static final String PENDING = "pending";
	public static final String SUSPENDED = "suspended";
	public static final String DELETED = "deleted";

	public ListUserCredentialsPresenter(ListUsersCredentialsView view) {
		super(view);
		init();
	}

	private void init() {
		userServices = modelLayerFactory.newUserServices();
	}

	public UserCredentialVODataProvider getDataProvider() {
		UserCredentialToVOBuilder voBuilder = new UserCredentialToVOBuilder();
		return new UserCredentialVODataProvider(voBuilder, modelLayerFactory, null);
	}

	public void addButtonClicked() {
		String params = ParamUtils.addParams(NavigatorConfigurationService.USER_LIST, null);
		view.navigate().to().addUserCredential(params);
	}

	public void editButtonClicked(UserCredentialVO entity) {
		String parameters = getParameters(entity);
		view.navigate().to().editUserCredential(parameters);
	}

	public void displayButtonClicked(UserCredentialVO entity) {
		String parameters = getParameters(entity);
		view.navigate().to().displayUserCredential(parameters);
	}

	public void backButtonClicked() {
		view.navigate().to().adminModule();
	}

	private String getParameters(UserCredentialVO entity) {
		Map<String, Object> params = new HashMap<>();
		params.put("username", entity.getUsername());
		return ParamUtils.addParams(NavigatorConfigurationService.USER_LIST, params);
	}

	public boolean canAddOrModify() {
		return true;
	}

	public boolean canModifyPassword(String usernameInEdition) {
		UserCredential userInEdition = userServices.getUserCredential(usernameInEdition);
		UserCredential currentUser = userServices.getUserCredential(view.getSessionContext().getCurrentUser().getUsername());
		return userServices.canModifyPassword(userInEdition, currentUser);
	}

	@Override
	protected boolean hasPageAccess(String params, User user) {
		return userServices.has(user).globalPermissionInAnyCollection(CorePermissions.MANAGE_SYSTEM_USERS);
	}

	public List<String> getTabs() {
		List<String> tabs = new ArrayList<>();
		tabs.add(ACTIVE);
		tabs.add(PENDING);
		tabs.add(SUSPENDED);
		tabs.add(DELETED);

		return tabs;
	}

	public String getTabCaption(String tabId) {
		switch (tabId) {
			case ACTIVE:
				return $("ListUsersCredentialsView.active");
			case PENDING:
				return $("ListUsersCredentialsView.pending");
			case SUSPENDED:
				return $("ListUsersCredentialsView.suspended");
			case DELETED:
				return $("ListUsersCredentialsView.deleted");
			default:
				return null;
		}
	}
}
