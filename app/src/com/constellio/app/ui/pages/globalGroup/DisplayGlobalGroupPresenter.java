package com.constellio.app.ui.pages.globalGroup;

import com.constellio.app.ui.application.NavigatorConfigurationService;
import com.constellio.app.ui.entities.GlobalGroupVO;
import com.constellio.app.ui.entities.UserCredentialVO;
import com.constellio.app.ui.framework.builders.GlobalGroupToVOBuilder;
import com.constellio.app.ui.framework.builders.UserCredentialToVOBuilder;
import com.constellio.app.ui.framework.data.GlobalGroupVODataProvider;
import com.constellio.app.ui.framework.data.UserCredentialVODataProvider;
import com.constellio.app.ui.pages.base.BasePresenter;
import com.constellio.app.ui.params.ParamUtils;
import com.constellio.model.entities.CorePermissions;
import com.constellio.model.entities.records.wrappers.User;
import com.constellio.model.entities.security.global.GlobalGroup;
import com.constellio.model.entities.security.global.UserCredential;
import com.constellio.model.services.users.UserServices;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@SuppressWarnings("serial")
public class DisplayGlobalGroupPresenter extends BasePresenter<DisplayGlobalGroupView> {

	transient UserServices userServices;

	private Map<String, String> paramsMap;
	private String breadCrumb;

	public DisplayGlobalGroupPresenter(DisplayGlobalGroupView view) {
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

	public GlobalGroupVO getGlobalGroupVO(String code) {
		GlobalGroup globalGroup = userServices.getGroup(code);
		GlobalGroupToVOBuilder voBuilder = new GlobalGroupToVOBuilder();
		return voBuilder.build(globalGroup);
	}

	public void backButtonClicked() {
		navigateToBackPage();
	}

	public void cleanInvalidBackPages() {
		if (paramsMap.containsKey("globalGroupCode")) {
			paramsMap.remove("globalGroupCode");
		}
		breadCrumb = breadCrumb.replace(NavigatorConfigurationService.GROUP_ADD_EDIT, "");
		breadCrumb = breadCrumb.replace(NavigatorConfigurationService.GROUP_DISPLAY, "");
		breadCrumb = breadCrumb.replace("//", "/");
	}

	public UserCredentialVODataProvider getUserCredentialVODataProvider(String groupCode) {
		UserCredentialToVOBuilder voBuilder = newUserCredentialVOBuilder();
		return newUserCredentialVODataProvider(groupCode, voBuilder);
	}

	public GlobalGroupVODataProvider getGlobalGroupVODataProvider() {
		GlobalGroupToVOBuilder voBuilder = newGlobalGroupVOBuilder();
		return newGlobalGroupVODataProvider(voBuilder);
	}

	public void displayUserCredentialButtonClicked(UserCredentialVO entity, String globalGroupCode) {
		paramsMap.put("username", entity.getUsername());
		paramsMap.put("globalGroupCode", globalGroupCode);
		String parameters = getParameters();
		view.navigate().to().displayUserCredential(parameters);

	}

	public void editUserCredentialButtonClicked(UserCredentialVO entity, String globalGroupCode) {
		paramsMap.put("username", entity.getUsername());
		paramsMap.put("globalGroupCode", globalGroupCode);
		String parameters = getParameters();
		view.navigate().to().editUserCredential(parameters);
	}

	public void deleteUserCredentialButtonClicked(UserCredentialVO userCredentialVO, String globalGroupCode) {
		userServices.removeUserFromGlobalGroup(userCredentialVO.getUsername(), globalGroupCode);
		view.refreshTable();
	}

	public void addUserCredentialButtonClicked(String globalGroupCode, String username) {
		List<String> newGlobalGroups = new ArrayList<>();
		UserCredential userCredential = userServices.getUserCredential(username);
		List<String> globalGroups = userCredential.getGlobalGroups();
		newGlobalGroups.addAll(globalGroups);
		newGlobalGroups.add(globalGroupCode);
		userCredential = userCredential.setGlobalGroups(newGlobalGroups);
		userServices.addUpdateUserCredential(userCredential);
		view.refreshTable();
	}

	public void addSubGroupClicked(GlobalGroupVO entity) {
		paramsMap.put("parentGlobalGroupCode", entity.getCode());
		paramsMap.remove("globalGroupCode");
		String parameters = ParamUtils.addParams(breadCrumb + "/" + NavigatorConfigurationService.GROUP_DISPLAY, paramsMap);
		view.navigate().to().addGlobalGroup(parameters);
	}

	public void setParamsMap(Map<String, String> paramsMap) {
		this.paramsMap = paramsMap;
	}

	public void setBreadCrumb(String breadCrumb) {
		this.breadCrumb = breadCrumb;
	}

	public String getBreadCrumb() {
		return this.breadCrumb;
	}

	private String getParameters() {
		Map<String, Object> newParamsMap = new HashMap<>();
		newParamsMap.putAll(paramsMap);
		return ParamUtils.addParams(breadCrumb + "/" + NavigatorConfigurationService.GROUP_DISPLAY, newParamsMap);
	}

	public void navigateToBackPage() {
		String viewNames[] = breadCrumb.split("/");
		String backPage = viewNames[viewNames.length - 1];
		breadCrumb = breadCrumb.replace(backPage, "");
		if (breadCrumb.endsWith("/")) {
			breadCrumb = breadCrumb.substring(0, breadCrumb.length() - 1);
		}
		if (paramsMap.containsKey("parentGlobalGroupCode")) {
			paramsMap.put("globalGroupCode", paramsMap.get("parentGlobalGroupCode"));
			paramsMap.remove("parentGlobalGroupCode");
		}
		Map<String, Object> newParamsMap = new HashMap<>();
		newParamsMap.putAll(paramsMap);
		String parameters = ParamUtils.addParams(breadCrumb, newParamsMap);
		parameters = correctSlashs(parameters);
		backPage = correctUrl(backPage, parameters);

		view.navigate().to().url(backPage + parameters);
	}

	private String correctUrl(String backPage, String parameters) {
		if (!backPage.endsWith("/") && !parameters.startsWith("/")) {
			backPage += "/";
		}
		return backPage;
	}

	private String correctSlashs(String parameters) {
		while (parameters.contains("//")) {
			parameters = parameters.replace("//", "/");
		}
		return parameters;
	}

	UserCredentialVODataProvider newUserCredentialVODataProvider(String groupCode,
																 UserCredentialToVOBuilder voBuilder) {
		return new UserCredentialVODataProvider(voBuilder, modelLayerFactory, groupCode);
	}

	UserCredentialToVOBuilder newUserCredentialVOBuilder() {
		return new UserCredentialToVOBuilder();
	}

	GlobalGroupVODataProvider newGlobalGroupVODataProvider(GlobalGroupToVOBuilder voBuilder) {
		return new GlobalGroupVODataProvider(voBuilder, modelLayerFactory, true);
	}

	GlobalGroupToVOBuilder newGlobalGroupVOBuilder() {
		return new GlobalGroupToVOBuilder();
	}

	public void displaySubGroupCliked(GlobalGroupVO entity) {
		String parameters = getParameters(entity);
		view.navigate().to().displayGlobalGroup(parameters);
	}

	public void editSubGroupButtonClicked(GlobalGroupVO entity) {
		String parameters = getParameters(entity);
		view.navigate().to().editGlobalGroup(parameters);
	}

	public void deleteSubGroupButtonClicked(GlobalGroupVO entity) {
		UserServices userServices = modelLayerFactory.newUserServices();
		String username = view.getSessionContext().getCurrentUser().getUsername();
		UserCredential userCredential = modelLayerFactory.getUserCredentialsManager().getUserCredential(username);
		GlobalGroup globalGroup = userServices.getGroup(entity.getCode());
		userServices.logicallyRemoveGroupHierarchy(userCredential, globalGroup);

		//TODO refresh table
		view.refreshTable();
	}

	private String getParameters(GlobalGroupVO entity) {
		Map<String, Object> params = new HashMap<>();
		params.putAll(paramsMap);
		if (entity.getParent() != null) {
			params.put("parentGlobalGroupCode", entity.getParent());
		} else if (params.containsKey("parentGlobalGroupCode")) {
			params.remove("parentGlobalGroupCode");
		}
		params.put("globalGroupCode", entity.getCode());
		return ParamUtils.addParams(breadCrumb + "/" + NavigatorConfigurationService.GROUP_DISPLAY, params);
	}

	@Override
	protected boolean hasPageAccess(String params, User user) {
		return userServices.has(user).globalPermissionInAnyCollection(CorePermissions.MANAGE_SYSTEM_GROUPS);
	}
}
