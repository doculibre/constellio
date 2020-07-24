package com.constellio.app.ui.pages.globalGroup;

import com.constellio.app.ui.entities.GlobalGroupVO;
import com.constellio.app.ui.framework.builders.GlobalGroupToVOBuilder;
import com.constellio.app.ui.pages.base.BasePresenter;
import com.constellio.app.ui.params.ParamUtils;
import com.constellio.model.entities.CorePermissions;
import com.constellio.model.entities.records.wrappers.User;
import com.constellio.model.entities.security.global.GroupAddUpdateRequest;
import com.constellio.model.entities.security.global.SystemWideGroup;
import com.constellio.model.services.collections.CollectionsListManager;
import com.constellio.model.services.logging.LoggingServices;
import com.constellio.model.services.users.UserServices;
import com.constellio.model.services.users.UserServicesRuntimeException.UserServicesRuntimeException_NoSuchGroup;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class AddEditGlobalGroupPresenter extends BasePresenter<AddEditGlobalGroupView> {

	private static final Logger LOGGER = LoggerFactory.getLogger(AddEditGlobalGroupPresenter.class);
	transient UserServices userServices;
	private transient CollectionsListManager collectionsListManager;
	private transient LoggingServices loggingServices;
	private boolean editMode = false;
	private Map<String, String> paramsMap;
	private String code;
	private String breadCrumb;
	private Set<String> collections;

	public AddEditGlobalGroupPresenter(AddEditGlobalGroupView view) {
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
		collectionsListManager = modelLayerFactory.getCollectionsListManager();
		loggingServices = modelLayerFactory.newLoggingServices();
	}

	public GlobalGroupVO getGlobalGroupVO(String code) {
		SystemWideGroup globalGroup = null;
		if (StringUtils.isNotBlank(code)) {
			editMode = true;
			this.code = code;
			globalGroup = userServices.getGroup(code);
		}
		GlobalGroupToVOBuilder voBuilder = new GlobalGroupToVOBuilder();
		GlobalGroupVO globalGroupVO = globalGroup != null ? voBuilder.build(globalGroup) : new GlobalGroupVO();
		collections = globalGroupVO.getCollections();
		return globalGroupVO;
	}

	public void saveButtonClicked(GlobalGroupVO entity) {
		String code = entity.getCode();

		if (isEditMode()) {
			if (!getCode().equals(code)) {
				view.showErrorMessage("Cannot change code");
				return;
			}
		} else {
			try {
				userServices.getGroup(code);
				view.showErrorMessage("Global Group already exists!");
				return;
			} catch (UserServicesRuntimeException_NoSuchGroup e) {
				//Ok
				LOGGER.info(e.getMessage(), e);
			}
		}
		GroupAddUpdateRequest globalGroup = toGlobalGroup(entity);
		userServices.execute(globalGroup);

		//		if (!isEditMode()) {
		//			for (String collection : globalGroup.getUsersAutomaticallyAddedToCollections()) {
		//				Group group = userServices.getGroupInCollection(entity.getCode(), collection);
		//				loggingServices.addUserOrGroup(group.getWrappedRecord(), getCurrentUser(), collection);
		//			}
		//		} else {
		//			for (String collection : globalGroup.getUsersAutomaticallyAddedToCollections()) {
		//				Group group = userServices.getGroupInCollection(entity.getCode(), collection);
		//				if (entity.getCollections().contains(collection) && !collections.contains(collection)) {
		//					loggingServices.addUserOrGroup(group.getWrappedRecord(), getCurrentUser(), collection);
		//				}
		//			}
		//		}

		navigateToBackPage();
	}

	GroupAddUpdateRequest toGlobalGroup(GlobalGroupVO globalGroupVO) {
		List<String> collections = new ArrayList<>();
		if (globalGroupVO.getCollections() != null) {
			collections.addAll(globalGroupVO.getCollections());
		}
		GroupAddUpdateRequest newGlobalGroup = userServices.createGlobalGroup(globalGroupVO.getCode(), globalGroupVO.getName(),
				collections, globalGroupVO.getParent(), globalGroupVO.getStatus(), globalGroupVO.isLocallyCreated());
		return newGlobalGroup;
	}

	public void cancelButtonClicked() {
		navigateToBackPage();
	}

	public boolean isEditMode() {
		return editMode;
	}

	public String getCode() {
		return code;
	}

	public List<String> getAllCollections() {
		return collectionsListManager.getCollectionsExcludingSystem();
	}

	public void setParamsMap(Map<String, String> paramsMap) {
		this.paramsMap = paramsMap;
	}

	public void setBreadCrumb(String breadCrumb) {
		this.breadCrumb = breadCrumb;
	}

	public Map<String, String> getParamsMap() {
		return paramsMap;
	}

	private void navigateToBackPage() {
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
		parameters = cleanParameters(parameters);
		backPage = correctUrlSlash(backPage, parameters);
		view.navigate().to().url(backPage + parameters);
	}

	private String correctUrlSlash(String backPage, String parameters) {
		if (!backPage.endsWith("/") && !parameters.startsWith("/")) {
			backPage += "/";
		}
		return backPage;
	}

	private String cleanParameters(String parameters) {
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
		return userServices.has(user).globalPermissionInAnyCollection(CorePermissions.MANAGE_SYSTEM_GROUPS);
	}

}
