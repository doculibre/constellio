package com.constellio.app.ui.pages.globalGroup;

import com.constellio.app.modules.rm.ui.builders.UserToVOBuilder;
import com.constellio.app.ui.application.NavigatorConfigurationService;
import com.constellio.app.ui.entities.GlobalGroupVO;
import com.constellio.app.ui.entities.RecordVO;
import com.constellio.app.ui.entities.RecordVO.VIEW_MODE;
import com.constellio.app.ui.entities.UserCredentialVO;
import com.constellio.app.ui.framework.builders.GlobalGroupToVOBuilder;
import com.constellio.app.ui.framework.builders.RecordToVOBuilder;
import com.constellio.app.ui.framework.builders.UserCredentialToVOBuilder;
import com.constellio.app.ui.framework.data.GlobalGroupVODataProvider;
import com.constellio.app.ui.framework.data.UserCredentialVODataProvider;
import com.constellio.app.ui.pages.base.BasePresenter;
import com.constellio.app.ui.params.ParamUtils;
import com.constellio.model.entities.CorePermissions;
import com.constellio.model.entities.records.wrappers.Group;
import com.constellio.model.entities.records.wrappers.User;
import com.constellio.model.entities.security.global.SystemWideGroup;
import com.constellio.model.entities.security.global.UserCredential;
import com.constellio.model.services.records.SchemasRecordsServices;
import com.constellio.model.services.users.UserAddUpdateRequest;
import com.constellio.model.services.users.UserServices;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

@SuppressWarnings("serial")
public class DisplayGlobalGroupPresenter extends BasePresenter<DisplayGlobalGroupView> {

	transient UserServices userServices;

	private Map<String, String> paramsMap;
	private String breadCrumb;
	private SchemasRecordsServices core;
	private RecordVO pageGroup;

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
		this.core = new SchemasRecordsServices(view.getCollection(), view.getConstellioFactories().getModelLayerFactory());
	}

	public GlobalGroupVO getGlobalGroupVO(String code) {
		SystemWideGroup globalGroup = userServices.getGroup(code);
		GlobalGroupToVOBuilder voBuilder = new GlobalGroupToVOBuilder();
		return voBuilder.build(globalGroup);
	}

	public void setPageGroup(String code) {
		Group group = userServices.getGroupInCollection(code, view.getCollection());

		this.pageGroup = new RecordToVOBuilder().build(group.getWrappedRecord(), VIEW_MODE.DISPLAY, view.getSessionContext());
	}

	public RecordVO getPageGroup() {
		return this.pageGroup;
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

	public Group getGroup(String groupCode) {
		return core.getGroupWithCode(groupCode);
	}

	public GlobalGroupVODataProvider getGlobalGroupVODataProvider() {
		GlobalGroupToVOBuilder voBuilder = newGlobalGroupVOBuilder();
		return newGlobalGroupVODataProvider(voBuilder);
	}

	public void displayUserCredentialButtonClicked(UserCredentialVO entity) {
		view.navigate().to().displayUserCredential(entity.getUsername());
	}

	public void editUserCredentialButtonClicked(UserCredentialVO entity) {
		view.navigate().to().editUserCredential(entity.getUsername());
	}

	public void deleteUserCredentialButtonClicked(UserCredentialVO userCredentialVO, String globalGroupCode) {
		UserAddUpdateRequest userAddUpdateRequest = userServices.addUpdate(userCredentialVO.getUsername());

		userAddUpdateRequest.removeFromGroupOfCollection(globalGroupCode, collection);
		view.refreshTable();
	}

	public void addUserCredentialButtonClicked(String globalGroupCode, String username) {
		UserAddUpdateRequest userCredentialRequest = userServices.addUpdate(username);
		userCredentialRequest.addToGroupInCollection(globalGroupCode, view.getCollection());
		userServices.execute(userCredentialRequest);
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

		return ParamUtils.addParams(NavigatorConfigurationService.GROUP_DISPLAY, newParamsMap);
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
		return new UserCredentialVODataProvider(voBuilder, modelLayerFactory, groupCode, view.getCollection());
	}

	UserToVOBuilder newUserVOBuilder() {
		return new UserToVOBuilder();
	}

	UserCredentialToVOBuilder newUserCredentialVOBuilder() {
		return new UserCredentialToVOBuilder();
	}

	GlobalGroupVODataProvider newGlobalGroupVODataProvider(GlobalGroupToVOBuilder voBuilder) {
		GlobalGroupVODataProvider globalGroupVODataProvider = new GlobalGroupVODataProvider(voBuilder, modelLayerFactory, true, view.getCollection());
		globalGroupVODataProvider.setCollection(view.getCollection());
		return globalGroupVODataProvider;
	}

	GlobalGroupToVOBuilder newGlobalGroupVOBuilder() {
		return new GlobalGroupToVOBuilder();
	}

	public void displaySubGroupCliked(GlobalGroupVO entity) {
		view.navigate().to().displayGlobalGroup(getParameters(entity));
	}

	public void editSubGroupButtonClicked(GlobalGroupVO entity) {
		view.navigate().to().editGlobalGroup(getParameters(entity));
	}

	public void deleteSubGroupButtonClicked(GlobalGroupVO entity) {
		UserServices userServices = modelLayerFactory.newUserServices();
		String username = view.getSessionContext().getCurrentUser().getUsername();
		UserCredential userCredential = userServices.getUserCredential(username);
		SystemWideGroup globalGroup = userServices.getGroup(entity.getCode());
		userServices.logicallyRemoveGroupHierarchy(userCredential.getUsername(), globalGroup);

		//TODO refresh table
		view.refreshTable();
	}

	private Map<String, String> getParameters(GlobalGroupVO entity) {
		Map<String, String> params = new HashMap<>();
		params.putAll(paramsMap);
		if (entity.getParent() != null) {
			params.put("parentGlobalGroupCode", entity.getParent());
		} else if (params.containsKey("parentGlobalGroupCode")) {
			params.remove("parentGlobalGroupCode");
		}
		params.put("globalGroupCode", entity.getCode());

		return params;
	}

	@Override
	protected boolean hasPageAccess(String params, User user) {
		return userServices.has(user).globalPermissionInAnyCollection(CorePermissions.MANAGE_SYSTEM_GROUPS);
	}

	public void editGroupButtonClicked() {
		view.navigate().to().editGlobalGroup(pageGroup.get(Group.CODE) + "");
	}
}
