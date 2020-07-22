package com.constellio.app.services.menu.behavior;

import com.constellio.app.services.factories.AppLayerFactory;
import com.constellio.app.ui.application.NavigatorConfigurationService;
import com.constellio.app.ui.entities.GlobalGroupVO;
import com.constellio.app.ui.pages.globalGroup.DisplayGlobalGroupView;
import com.constellio.app.ui.params.ParamUtils;
import com.constellio.model.entities.security.global.GlobalGroup;
import com.constellio.model.entities.security.global.UserCredential;
import com.constellio.model.services.factories.ModelLayerFactory;
import com.constellio.model.services.records.SchemasRecordsServices;
import com.constellio.model.services.users.UserServices;

import java.util.HashMap;
import java.util.Map;

public class GlobalGroupMenuItemActionBehaviors {
	private ModelLayerFactory modelLayerFactory;
	private SchemasRecordsServices core;
	private UserServices userServices;


	public GlobalGroupMenuItemActionBehaviors(AppLayerFactory appLayerFactory) {
		this.modelLayerFactory = appLayerFactory.getModelLayerFactory();
		this.core = new SchemasRecordsServices(null, modelLayerFactory);
		this.userServices = modelLayerFactory.newUserServices();
	}

	private Map<String, String> clone(Map<String, String> map) {
		if (map == null) {
			return null;
		}

		Map<String, String> newMap = new HashMap<>();

		newMap.putAll(map);

		return newMap;
	}
	// FIXME C'est 3 méthode ne devrais pas avoir a caster la vu pour obtenir des variable et le presenter.

	public void groupAddSubGroup(MenuItemActionBehaviorParams params) {
		Map<String, String> mapParams = clone(params.getFormParams());
		GlobalGroupVO globalGroupVO = (GlobalGroupVO) params.getObjectRecordVO();

		GlobalGroup globalGroup = core.getGlobalGroupWithCode(globalGroupVO.getCode());

		mapParams.put("parentGlobalGroupCode", globalGroup.getCode());
		mapParams.remove("globalGroupCode");

		DisplayGlobalGroupView displayGlobalGroupView = (DisplayGlobalGroupView) params.getView();

		String parameters = ParamUtils.addParams(displayGlobalGroupView.getBreadCrumb() + "/" + NavigatorConfigurationService.GROUP_DISPLAY, mapParams);
		params.getView().navigate().to().addGlobalGroup(parameters);
	}

	public void edit(MenuItemActionBehaviorParams params) {
		GlobalGroupVO globalGroupVO = (GlobalGroupVO) params.getObjectRecordVO();

		Map<String, String> mapParams = clone(params.getFormParams());
		GlobalGroup globalGroup = core.getGlobalGroupWithCode(globalGroupVO.getCode());
		DisplayGlobalGroupView displayGlobalGroupView = (DisplayGlobalGroupView) params.getView();

		mapParams.put("globalGroupCode", globalGroup.getCode());
		String parameters = ParamUtils.addParams(displayGlobalGroupView.getBreadCrumb() + "/" + NavigatorConfigurationService.GROUP_DISPLAY, mapParams);
		params.getView().navigate().to().editGlobalGroup(parameters);
	}

	public void delete(MenuItemActionBehaviorParams params) {
		GlobalGroupVO globalGroupVO = (GlobalGroupVO) params.getObjectRecordVO();

		GlobalGroup globalGroup = core.getGlobalGroupWithCode(globalGroupVO.getCode());
		DisplayGlobalGroupView displayGlobalGroupView = (DisplayGlobalGroupView) params.getView();

		String username = params.getView().getSessionContext().getCurrentUser().getUsername();
		UserCredential userCredential = userServices.getUserCredential(username);
		userServices.logicallyRemoveGroupHierarchy(userCredential.getUsername(), globalGroup);

		displayGlobalGroupView.getPresenter().cleanInvalidBackPages();

		displayGlobalGroupView.getPresenter().navigateToBackPage();
	}

}
