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
import com.constellio.model.services.users.UserAddUpdateRequest;
import com.constellio.model.services.users.UserServices;

import java.io.IOException;
import java.util.HashMap;
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
		UserAddUpdateRequest userUpdateRequest = userServices.addUpdate(username);
		userUpdateRequest.addToGroupInEachCollection(globalGroupCode);
		userServices.execute(userUpdateRequest);
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

	public String getServiceKey(String username) {
		String serviceKey = userServices.getUserInfos(username).getServiceKey();
		if (serviceKey == null) {
			serviceKey = userServices.giveNewServiceKey(username);
		}
		return serviceKey;
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
