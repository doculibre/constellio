package com.constellio.app.ui.pages.collection;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.apache.commons.lang.StringUtils;

import com.constellio.app.ui.entities.RecordVO;
import com.constellio.app.ui.entities.RecordVO.VIEW_MODE;
import com.constellio.app.ui.entities.RoleAuthVO;
import com.constellio.app.ui.entities.RoleVO;
import com.constellio.app.ui.pages.base.SingleSchemaBasePresenter;
import com.constellio.model.entities.CorePermissions;
import com.constellio.model.entities.records.wrappers.Group;
import com.constellio.model.entities.records.wrappers.User;
import com.constellio.model.entities.security.Authorization;
import com.constellio.model.entities.security.XMLAuthorizationDetails;
import com.constellio.model.entities.security.Role;
import com.constellio.model.entities.security.global.AuthorizationBuilder;
import com.constellio.model.services.security.AuthorizationsServices;
import com.constellio.model.services.security.roles.RolesManager;

public class CollectionGroupRolesPresenter extends SingleSchemaBasePresenter<CollectionGroupRolesView> {
	String recordId;

	public CollectionGroupRolesPresenter(CollectionGroupRolesView view) {
		super(view, Group.DEFAULT_SCHEMA);
	}

	public void forRequestParams(String parameters) {
		recordId = parameters;
	}

	public RecordVO getGroup() {
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

	public void roleAdditionRequested(String roleCode) {
		Group group = coreSchemas().getGroup(recordId);
		List<String> roles = new ArrayList<>(group.getRoles());
		if (!roles.contains(roleCode)) {
			roles.add(roleCode);
			group.setRoles(roles);
			addOrUpdate(group.getWrappedRecord());
		}
	}

	public void roleRemovalRequested(String roleCode) {
		Group group = coreSchemas().getGroup(recordId);
		List<String> roles = new ArrayList<>(group.getRoles());
		roles.remove(roleCode);
		group.setRoles(roles);
		addOrUpdate(group.getWrappedRecord());
	}

	private RolesManager roleManager() {
		return modelLayerFactory.getRolesManager();
	}

	public List<RoleAuthVO> getInheritedRoles() {
		List<RoleAuthVO> inheritedRoles = new ArrayList<>();
		Group group = coreSchemas().getGroup(recordId);
		for (Authorization roleAuth : modelLayerFactory.newAuthorizationsServices().getRecordAuthorizations(group)) {
			if (isRoleAuthorization(roleAuth) && !isOwnAuthorization(roleAuth)) {
				inheritedRoles.add(toRoleAuthVO(roleAuth));
			}
		}
		return inheritedRoles;
	}

	public List<RoleAuthVO> getSpecificRoles() {
		List<RoleAuthVO> specificRoles = new ArrayList<>();
		Group group = coreSchemas().getGroup(recordId);

		for (String globalRole : group.getRoles()) {
			specificRoles.add(new RoleAuthVO(null, null, Arrays.asList(globalRole)));
		}

		for (Authorization roleAuth : modelLayerFactory.newAuthorizationsServices().getRecordAuthorizations(group)) {
			if (isRoleAuthorization(roleAuth) && isOwnAuthorization(roleAuth)) {
				specificRoles.add(toRoleAuthVO(roleAuth));
			}
		}
		return specificRoles;
	}

	public String getPrincipalTaxonomySchemaCode() {
		return modelLayerFactory.getTaxonomiesManager().getPrincipalTaxonomy(collection).getSchemaTypes().get(0);
	}

	public RoleAuthVO newRoleAuthVO() {
		return new RoleAuthVO(null, null, new ArrayList<String>());
	}

	public void addRoleButtonClicked(RoleAuthVO roleAuthVO) {
		if (StringUtils.isBlank(roleAuthVO.getTarget())) {
			for (String role : roleAuthVO.getRoles()) {
				roleAdditionRequested(role);
			}
		} else {
			AuthorizationsServices authorizationsServices = modelLayerFactory.newAuthorizationsServices();
			Authorization authorization = new AuthorizationBuilder(collection).forUsers(recordId).on(roleAuthVO.getTarget())
					.giving(roleAuthVO.getRoles().toArray(new String[roleAuthVO.getRoles().size()]));
			authorizationsServices.add(authorization, getCurrentUser());
		}
		view.refreshTable();
	}

	public void deleteRoleButtonClicked(RoleAuthVO roleAuthVO) {
		if (StringUtils.isBlank(roleAuthVO.getId())) {
			for (String role : roleAuthVO.getRoles()) {
				roleRemovalRequested(role);
			}
		} else {
			AuthorizationsServices authorizationsServices = modelLayerFactory.newAuthorizationsServices();
			XMLAuthorizationDetails xmlAuthorizationDetails = authorizationsServices.getAuthorization(collection, roleAuthVO.getId())
					.getDetail();
			authorizationsServices.delete(xmlAuthorizationDetails, getCurrentUser());
		}
		view.refreshTable();
	}

	protected boolean isRoleAuthorization(Authorization auth) {
		for (String role : auth.getDetail().getRoles()) {
			if (!isAccessRole(role)) {
				return true;
			}
		}
		return false;
	}

	private boolean isAccessRole(String role) {
		return role.equals(Role.READ) || role.equals(Role.WRITE) || role.equals(Role.DELETE);
	}

	private boolean isOwnAuthorization(Authorization authorization) {
		return authorization.getGrantedToPrincipals().contains(recordId);
	}

	public RoleAuthVO toRoleAuthVO(Authorization roleAuth) {
		return new RoleAuthVO(roleAuth.getDetail().getId(), roleAuth.getGrantedOnRecords().get(0),
				roleAuth.getDetail().getRoles());
	}

	public void backButtonClicked() {
		view.navigate().to().displayCollectionGroup(recordId);
	}

	@Override
	protected boolean hasPageAccess(String params, User user) {
		return user.has(CorePermissions.MANAGE_SECURITY).globally();
	}
}
