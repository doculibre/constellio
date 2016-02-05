package com.constellio.app.ui.pages.management.permissions;

import static com.constellio.app.ui.i18n.i18n.$;

import java.util.ArrayList;
import java.util.List;

import com.constellio.app.ui.entities.RoleVO;
import com.constellio.app.ui.pages.base.BasePresenter;
import com.constellio.model.entities.CorePermissions;
import com.constellio.model.entities.records.wrappers.User;
import com.constellio.model.entities.security.Role;
import com.constellio.model.services.extensions.ConstellioModulesManager;
import com.constellio.model.services.security.roles.RolesManager;

public class PermissionsManagementPresenter extends BasePresenter<PermissionsManagementView> {

	public PermissionsManagementPresenter(PermissionsManagementView view) {
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

	public List<RoleVO> getRoles() {
		List<RoleVO> result = new ArrayList<>();
		for (Role role : roleManager().getAllRoles(view.getCollection())) {
			result.add(new RoleVO(role.getCode(), role.getTitle(), role.getOperationPermissions()));
		}
		return result;
	}

	public void roleCreationRequested(RoleVO roleVO) {
		Role role = new Role(view.getCollection(), roleVO.getCode(), roleVO.getTitle(), roleVO.getPermissions());
		roleManager().addRole(role);
		view.addRole(roleVO);
	}

	public void saveRequested(List<RoleVO> modifiedRoles) {
		for (RoleVO roleVO : modifiedRoles) {
			Role role = roleManager().getRole(view.getCollection(), roleVO.getCode())
					.withPermissions(roleVO.getPermissions());
			roleManager().updateRole(role);
			roleVO.markClean();
		}
		view.setSaveAndRevertButtonStatus(false);
		view.refreshView();
		view.showMessage($("PermissionsManagementView.rolesSaved"));
	}

	public void revertRequested() {
		view.setSaveAndRevertButtonStatus(false);
		view.refreshView();
	}

	public void permissionsChanged(boolean dirty) {
		view.setSaveAndRevertButtonStatus(dirty);
	}

	private ConstellioModulesManager moduleManager() {
		return appLayerFactory.getModulesManager();
	}

	private RolesManager roleManager() {
		return modelLayerFactory.getRolesManager();
	}
}
