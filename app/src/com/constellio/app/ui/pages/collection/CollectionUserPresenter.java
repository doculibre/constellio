package com.constellio.app.ui.pages.collection;

import com.constellio.app.modules.rm.wrappers.AdministrativeUnit;
import com.constellio.app.ui.entities.RecordVO;
import com.constellio.app.ui.entities.RecordVO.VIEW_MODE;
import com.constellio.app.ui.entities.RoleVO;
import com.constellio.app.ui.pages.base.SingleSchemaBasePresenter;
import com.constellio.model.entities.CorePermissions;
import com.constellio.model.entities.records.wrappers.User;
import com.constellio.model.entities.security.Role;
import com.constellio.model.entities.security.global.UserCredential;
import com.constellio.model.services.security.roles.RolesManager;
import com.constellio.model.services.users.UserServices;

import java.util.ArrayList;
import java.util.List;

public class CollectionUserPresenter extends SingleSchemaBasePresenter<CollectionUserView> {
	String recordId;

	public CollectionUserPresenter(CollectionUserView view) {
		super(view, User.DEFAULT_SCHEMA);
	}

	public void forRequestParams(String parameters) {
		recordId = parameters;
	}

	public RecordVO getUser() {
		return presenterService().getRecordVO(recordId, VIEW_MODE.DISPLAY, view.getSessionContext());
	}

	public String getRoleTitle(String roleCode) {
		return roleManager().getRole(view.getCollection(), roleCode).getTitle();
	}

	public List<RoleVO> getRoles() {
		List<RoleVO> result = new ArrayList<>();
		for (Role role : roleManager().getAllRoles(view.getCollection())) {
			result.add(new RoleVO(role.getCode(), role.getTitle(), role.getOperationPermissions()));
		}
		return result;
	}

	public void authorizationsButtonClicked() {
		view.navigate().to().listPrincipalAccessAuthorizations(recordId);
	}

	public void rolesButtonClicked() {
		view.navigate().to().editCollectionUserRoles(recordId);
	}

	public boolean isDeletionEnabled() {
		return !getCurrentUser().getId().equals(recordId);
	}

	public void deleteButtonClicked() {
		UserServices userServices = modelLayerFactory.newUserServices();
		User user = coreSchemas().getUser(recordId);
		UserCredential userCredential = userServices.getUserCredential(user.getUsername());
		userServices.removeUserFromCollection(userCredential, view.getCollection());
	}

	private RolesManager roleManager() {
		return modelLayerFactory.getRolesManager();
	}

	@Override
	protected boolean hasPageAccess(String params, User user) {
		return user.has(CorePermissions.MANAGE_SECURITY).globally();
	}

	public boolean isRMModuleEnabled() {
		return this.isSchemaExisting(AdministrativeUnit.DEFAULT_SCHEMA);
	}
}
