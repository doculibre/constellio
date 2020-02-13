package com.constellio.app.ui.pages.management.shares;

import com.constellio.app.ui.entities.AuthorizationVO;
import com.constellio.app.ui.framework.builders.AuthorizationToVOBuilder;
import com.constellio.app.ui.pages.base.BasePresenter;
import com.constellio.model.entities.CorePermissions;
import com.constellio.model.entities.records.wrappers.Authorization;
import com.constellio.model.entities.records.wrappers.User;
import com.constellio.model.services.extensions.ConstellioModulesManager;
import com.constellio.model.services.security.AuthorizationsServices;

import java.util.ArrayList;
import java.util.List;

public class ShareManagementPresenter extends BasePresenter<ShareManagementView> {

	public ShareManagementPresenter(ShareManagementView view) {
		super(view);
	}

	@Override
	protected boolean hasPageAccess(String params, User user) {
		return user.has(CorePermissions.MANAGE_SECURITY).globally();
	}

	public List<String> getPermissionGroups() {
		return moduleManager().getPermissionGroups(view.getCollection());
	}

	public List<String> getPermissionsInGroup(String group) {
		return moduleManager().getPermissionsInGroup(view.getCollection(), group);
	}

	public List<AuthorizationVO> getShares(User user) {
		AuthorizationToVOBuilder builder = new AuthorizationToVOBuilder(modelLayerFactory);
		List<AuthorizationVO> result = new ArrayList<>();
		for (Authorization authorization : authorizationsServices().getAllAuthorizationUserShared(user)) {
			result.add(builder.build(authorization));
		}
		return result;
	}

	public void authCreationRequested(AuthorizationVO authVo, String recordId) {
		//		AuthorizationAddRequest request = new AuthorizationAddRequest(Authorization);
		//		authorizationsServices().add(request);
		//		view.addShare(authVo);
	}

	public void shareChanged(boolean dirty) {
		view.setSaveButton(dirty);
	}

	private ConstellioModulesManager moduleManager() {
		return appLayerFactory.getModulesManager();
	}

	private AuthorizationsServices authorizationsServices() {
		return modelLayerFactory.newAuthorizationsServices();
	}
}
