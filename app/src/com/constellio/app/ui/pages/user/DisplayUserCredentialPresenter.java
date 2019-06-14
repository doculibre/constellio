package com.constellio.app.ui.pages.user;

import com.constellio.app.ui.application.NavigatorConfigurationService;
import com.constellio.app.ui.entities.UserCredentialVO;
import com.constellio.app.ui.framework.builders.GlobalGroupToVOBuilder;
import com.constellio.app.ui.framework.builders.UserCredentialToVOBuilder;
import com.constellio.app.ui.framework.data.GlobalGroupVODataProvider;
import com.constellio.app.ui.pages.base.BasePresenter;
import com.constellio.app.ui.params.ParamUtils;
import com.constellio.model.entities.CorePermissions;
import com.constellio.model.entities.records.wrappers.User;
import com.constellio.model.entities.security.global.UserCredential;
import com.constellio.model.services.migrations.ConstellioEIMConfigs;
import com.constellio.model.services.users.UserServices;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@SuppressWarnings("serial")
public class DisplayUserCredentialPresenter extends BasePresenter<DisplayUserCredentialView> {

	private transient UserServices userServices;

	private Map<String, String> paramsMap;
	private String breadCrumb;

	public DisplayUserCredentialPresenter(DisplayUserCredentialView view) {
		super(view);
		init();
	}

	private void readObject(java.io.ObjectInputStream stream)
			throws IOException, ClassNotFoundException {
		stream.defaultReadObject();
		init();
	}

	private void init() {
		userServices = modelLayerFactory.newUserServices();
	}

	public UserCredentialVO getUserCredentialVO(String username) {
		UserCredential userCredential = userServices.getUserCredential(username);
		UserCredentialToVOBuilder voBuilder = newUserCredentialToVOBuilder();
		return voBuilder.build(userCredential);
	}

	public void backButtonClicked() {
		String viewNames[] = breadCrumb.split("/");
		String backPage = viewNames[viewNames.length - 1];
		configureBreadCrumb(backPage);
		String parameters = getParameters();
		if (!backPage.endsWith("/") && !parameters.startsWith("/")) {
			backPage += "/";
		}
		view.navigate().to().url(backPage + parameters);
	}

	public void editButtonClicked(UserCredentialVO entity) {
		paramsMap.put("username", entity.getUsername());
		String parameters = getParameters(NavigatorConfigurationService.USER_DISPLAY);
		view.navigate().to().editUserCredential(parameters);
	}

	public GlobalGroupVODataProvider getGlobalGroupVODataProvider() {
		GlobalGroupToVOBuilder voBuilder = newGlobalGroupVOBuilder();
		return newGlobalGroupVODataProvider(voBuilder);
	}

	public void displayGlobalGroupButtonClicked(String globalGroupCode, String username) {
		paramsMap.put("username", username);
		paramsMap.put("globalGroupCode", globalGroupCode);
		String parameters = getParameters(NavigatorConfigurationService.USER_DISPLAY);
		view.navigate().to().displayGlobalGroup(parameters);
	}

	public void editGlobalGroupButtonClicked(String globalGroupCode, String username) {
		paramsMap.put("globalGroupCode", globalGroupCode);
		paramsMap.put("username", username);
		String parameters = getParameters(NavigatorConfigurationService.USER_DISPLAY);
		view.navigate().to().editGlobalGroup(parameters);
	}

	public void deleteGlobalGroupButtonClicked(String username, String globalGroupCode) {
		userServices.removeUserFromGlobalGroup(username, globalGroupCode);
		view.refreshTable();
	}

	public void addGlobalGroupButtonClicked(String username, String globalGroupCode) {
		List<String> newGlobalGroups = new ArrayList<>();
		UserCredential userCredential = userServices.getUserCredential(username);
		List<String> globalGroups = userCredential.getGlobalGroups();
		newGlobalGroups.addAll(globalGroups);
		newGlobalGroups.add(globalGroupCode);
		userCredential = userCredential.setGlobalGroups(newGlobalGroups);
		userServices.addUpdateUserCredential(userCredential);
		view.refreshTable();
	}

	public void setParamsMap(Map<String, String> paramsMap) {
		this.paramsMap = paramsMap;
	}

	public void setBreadCrumb(String breadCrumb) {
		this.breadCrumb = breadCrumb;
	}

	UserCredentialToVOBuilder newUserCredentialToVOBuilder() {
		return new UserCredentialToVOBuilder();
	}

	GlobalGroupToVOBuilder newGlobalGroupVOBuilder() {
		return new GlobalGroupToVOBuilder();
	}

	GlobalGroupVODataProvider newGlobalGroupVODataProvider(GlobalGroupToVOBuilder voBuilder) {
		return new GlobalGroupVODataProvider(voBuilder, modelLayerFactory, true);
	}

	private void configureBreadCrumb(String backPage) {
		breadCrumb = breadCrumb.replace(backPage, "");
		if (breadCrumb.endsWith("/")) {
			breadCrumb = breadCrumb.substring(0, breadCrumb.length() - 1);
		}
	}

	String getParameters() {
		return getParameters("");
	}

	String getParameters(String viewName) {
		Map<String, Object> newParamsMap = new HashMap<>();
		newParamsMap.putAll(paramsMap);
		if (!viewName.isEmpty()) {
			viewName = "/" + viewName;
		}
		String parameters = ParamUtils.addParams(breadCrumb + viewName, newParamsMap);
		parameters = cleanParameters(parameters);
		return parameters;
	}

	public String cleanParameters(String parameters) {
		while (parameters.contains("//")) {
			parameters = parameters.replace("//", "/");
		}
		return parameters;
	}

	public boolean canAddOrModify() {
		return userServices.canAddOrModifyUserAndGroup();
	}

	@Override
	protected boolean hasPageAccess(String params, User user) {
		return userServices.has(user).globalPermissionInAnyCollection(CorePermissions.MANAGE_SYSTEM_USERS);
	}

	public boolean canModifyPassword(String usernameInEdition) {
		UserCredential userInEdition = userServices.getUserCredential(usernameInEdition);
		UserCredential currentUser = userServices.getUserCredential(view.getSessionContext().getCurrentUser().getUsername());
		return userServices.canModifyPassword(userInEdition, currentUser);
	}

	public String getServiceKey(String username) {
		String serviceKey = userServices.getUser(username).getServiceKey();
		if (serviceKey == null) {
			serviceKey = userServices.giveNewServiceToken(userServices.getUser(username));
		}
		return serviceKey;
	}

	public String generateToken(String username, String unitTime, int duration) {
		return userServices.generateToken(username, unitTime, duration);
	}

	public boolean userNotLDAPSynced(String username) {
		UserCredential userCredential = userServices.getUserCredential(username);
		return userCredential.getDn() == null && userServices.has(userCredential).globalPermissionInAnyCollection(CorePermissions.MANAGE_SECURITY);
	}

	public String getConstellioUrl() {
		return new ConstellioEIMConfigs(modelLayerFactory.getSystemConfigurationsManager()).getConstellioUrl();
	}

	public String getBreadCrumb() {
		return breadCrumb;
	}

	public UserCredential getUserCredential(String userName) {
		return userServices.getUserCredential(userName);
	}
}
