package com.constellio.app.ui.pages.user;

import java.util.HashMap;
import java.util.Map;

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

public class ListUserCredentialsPresenter extends BasePresenter<ListUsersCredentialsView> {
	private transient UserServices userServices;

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
		return userServices.canAddOrModifyUserAndGroup();
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
}
