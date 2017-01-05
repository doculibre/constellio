package com.constellio.app.ui.pages.collection;

import static com.constellio.model.entities.security.global.AuthorizationDeleteRequest.authorizationDeleteRequest;

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
import com.constellio.model.entities.records.wrappers.User;
import com.constellio.model.entities.security.Authorization;
import com.constellio.model.entities.security.AuthorizationDetails;
import com.constellio.model.entities.security.Role;
import com.constellio.model.entities.security.global.AuthorizationBuilder;
import com.constellio.model.services.security.AuthorizationsServices;
import com.constellio.model.services.security.roles.RolesManager;

public class CollectionUserRolesPresenter extends SingleSchemaBasePresenter<CollectionUserRolesView> {
	String recordId;

	public CollectionUserRolesPresenter(CollectionUserRolesView view) {
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

	public void roleAdditionRequested(String roleCode) {
		User user = coreSchemas().getUser(recordId);
		List<String> roles = new ArrayList<>(user.getUserRoles());
		if (!roles.contains(roleCode)) {
			roles.add(roleCode);
			user.setUserRoles(roles);
			addOrUpdate(user.getWrappedRecord());
		}
	}

	public void roleRemovalRequested(String roleCode) {
		User user = coreSchemas().getUser(recordId);
		List<String> roles = new ArrayList<>(user.getUserRoles());
		roles.remove(roleCode);
		user.setUserRoles(roles);
		addOrUpdate(user.getWrappedRecord());
	}

	public void backButtonClicked() {
		view.navigate().to().displayCollectionUser(recordId);
	}

	private RolesManager roleManager() {
		return modelLayerFactory.getRolesManager();
	}

	@Override
	protected boolean hasPageAccess(String params, User user) {
		return user.has(CorePermissions.MANAGE_SECURITY).globally();
	}

	public List<RoleAuthVO> getInheritedRoles() {
		List<RoleAuthVO> inheritedRoles = new ArrayList<>();
		User user = coreSchemas().getUser(recordId);

		for (String globalRole : user.getAllRoles()) {
			if (!user.getUserRoles().contains(globalRole)) {
				inheritedRoles.add(new RoleAuthVO(null, null, Arrays.asList(globalRole)));
			}
		}

		for (Authorization roleAuth : modelLayerFactory.newAuthorizationsServices().getRecordAuthorizations(user)) {
			if (isRoleAuthorization(roleAuth) && !isOwnAuthorization(roleAuth)) {
				inheritedRoles.add(toRoleAuthVO(roleAuth));
			}
		}
		return inheritedRoles;
	}

	public List<RoleAuthVO> getSpecificRoles() {
		List<RoleAuthVO> specificRoles = new ArrayList<>();
		User user = coreSchemas().getUser(recordId);

		for (String globalRole : user.getUserRoles()) {
			specificRoles.add(new RoleAuthVO(null, null, Arrays.asList(globalRole)));
		}

		for (Authorization roleAuth : modelLayerFactory.newAuthorizationsServices().getRecordAuthorizations(user)) {
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
			AuthorizationDetails authorizationDetails = authorizationsServices.getAuthorization(collection, roleAuthVO.getId())
					.getDetail();
			authorizationsServices.execute(authorizationDeleteRequest(authorizationDetails).setExecutedBy(getCurrentUser()));
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
		String firstRecord = roleAuth.getGrantedOnRecords().isEmpty() ? null : roleAuth.getGrantedOnRecords().get(0);
		return new RoleAuthVO(roleAuth.getDetail().getId(), firstRecord, roleAuth.getDetail().getRoles());
	}

	boolean isTargetFieldVisible() {
		return modelLayerFactory.getTaxonomiesManager().getPrincipalTaxonomy(collection) != null;
	}
}
