/*Constellio Enterprise Information Management

Copyright (c) 2015 "Constellio inc."

This program is free software: you can redistribute it and/or modify
it under the terms of the GNU Affero General Public License as
published by the Free Software Foundation, either version 3 of the
License, or (at your option) any later version.

This program is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
GNU Affero General Public License for more details.

You should have received a copy of the GNU Affero General Public License
along with this program. If not, see <http://www.gnu.org/licenses/>.
*/
package com.constellio.app.api.admin.services;

import java.util.ArrayList;
import java.util.List;

import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;

import org.joda.time.LocalDate;

import com.constellio.app.api.admin.services.SecurityServicesException.SecurityServicesException_CannotUpdateGroup;
import com.constellio.app.api.admin.services.SecurityServicesException.SecurityServicesException_CannotUpdateUser;
import com.constellio.app.client.entities.AuthorizationResource;
import com.constellio.app.client.entities.GroupCollectionPermissionsResource;
import com.constellio.app.client.entities.RoleResource;
import com.constellio.app.client.entities.UserCollectionPermissionsResource;
import com.constellio.model.entities.records.Record;
import com.constellio.model.entities.records.wrappers.Group;
import com.constellio.model.entities.records.wrappers.User;
import com.constellio.model.entities.security.Authorization;
import com.constellio.model.entities.security.AuthorizationDetails;
import com.constellio.model.entities.security.CustomizedAuthorizationsBehavior;
import com.constellio.model.entities.security.Role;
import com.constellio.model.services.records.RecordServices;
import com.constellio.model.services.records.RecordServicesException;
import com.constellio.model.services.schemas.MetadataSchemasManager;
import com.constellio.model.services.security.AuthorizationsServices;
import com.constellio.model.services.security.authentification.AuthenticationService;
import com.constellio.model.services.security.roles.RolesManager;
import com.constellio.model.services.security.roles.RolesManagerRuntimeException;
import com.constellio.model.services.users.UserServices;

@Path("security")
@Consumes("application/xml")
@Produces("application/xml")
public class SecurityServices {

	@POST
	@Path("addAuthorization")
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.TEXT_PLAIN)
	public String addAuthorization(@QueryParam("collection") String collection, AuthorizationResource resource,
			@QueryParam("keepAttached") String keepAttached)
			throws RolesManagerRuntimeException {
		List<String> roles = new ArrayList<>();
		for (String roleCode : resource.getRoleIds()) {
			roles.add(rolesManager().getRole(collection, roleCode).getCode());
		}
		LocalDate startDate = null;
		LocalDate endDate = null;
		if (resource.getStartDate() != null && resource.getEndDate() != null) {
			startDate = LocalDate.fromDateFields(resource.getStartDate());
			endDate = LocalDate.fromDateFields(resource.getEndDate());
		}
		AuthorizationDetails authorizationDetails = AuthorizationDetails
				.create(getUniqueId(), roles, startDate, endDate, collection);
		Authorization authorization = new Authorization(authorizationDetails, resource.getPrincipalIds(),
				resource.getRecordIds());
		CustomizedAuthorizationsBehavior behavior = getCustomizedAuthorizationsBehavior(keepAttached);
		authorizationsServices().add(authorization, behavior, null);
		return authorizationDetails.getId();
	}

	@GET
	@Path("getAuthorization")
	@Produces(MediaType.APPLICATION_JSON)
	public AuthorizationResource getAuthorization(@QueryParam("collection") String collection,
			@QueryParam("authId") String authId) {
		Authorization authorization = authorizationsServices().getAuthorization(collection, authId);
		return toAuthorizationResource(authorization);
	}

	@POST
	@Path("removeAuthorizationOnRecord")
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.TEXT_PLAIN)
	public String removeAuthorizationOnRecord(@QueryParam("collection") String collection,
			@QueryParam("authorizationId") String authorizationId, @QueryParam("keepAttached") String keepAttached,
			AuthorizationResource resource)
			throws RolesManagerRuntimeException {
		Authorization authorization = authorizationsServices().getAuthorization(collection, authorizationId);
		Record record = recordServices().getDocumentById(resource.getRecordIds().get(0));
		authorizationsServices().removeAuthorizationOnRecord(authorization, record,
				getCustomizedAuthorizationsBehavior(keepAttached));
		return "Ok";
	}

	@POST
	@Path("modify")
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.TEXT_PLAIN)
	public String modify(@QueryParam("collection") String collection, @QueryParam("authorizationId") String authorizationId,
			@QueryParam("keepAttached") String keepAttached, AuthorizationResource resource)
			throws RolesManagerRuntimeException {
		Authorization authorization = authorizationsServices().getAuthorization(collection, authorizationId);
		if (resource.getRecordIds() != null) {
			authorization.setGrantedOnRecords(resource.getRecordIds());
		}
		if (resource.getPrincipalIds() != null) {
			authorization.setGrantedToPrincipals(resource.getPrincipalIds());
		}
		CustomizedAuthorizationsBehavior behavior = getCustomizedAuthorizationsBehavior(keepAttached);
		authorizationsServices().modify(authorization, behavior, null);
		return "Ok";
	}

	@POST
	@Path("hasContentPermission")
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.TEXT_PLAIN)
	public boolean hasPermission(@QueryParam("collection") String collection, @QueryParam("permission") String permission,
			@QueryParam("username") String username, AuthorizationResource resource) {
		User user = userServices().getUserInCollection(username, collection);
		Record record = recordServices().getDocumentById(resource.getRecordIds().get(0));

		if (Role.WRITE.equals(permission.toUpperCase())) {
			return authorizationsServices().canWrite(user, record);

		} else if (Role.READ.equals(permission.toUpperCase())) {
			return authorizationsServices().canRead(user, record);

		} else if (Role.DELETE.equals(permission.toUpperCase())) {
			return authorizationsServices().canDelete(user, record);
		}

		return false;
	}

	@POST
	@Path("hasDeletePermissionOnPrincipalConceptHierarchyAndIncludedRecords")
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.TEXT_PLAIN)
	public boolean hasDeletePermissionOnPrincipalConceptHierarchyAndIncludedRecords(@QueryParam("collection") String collection,
			@QueryParam("username") String username, AuthorizationResource resource) {
		User user = userServices().getUserInCollection(username, collection);
		Record record = recordServices().getDocumentById(resource.getRecordIds().get(0));
		return authorizationsServices().hasDeletePermissionOnPrincipalConceptHierarchy(user, record, true, schemasManager());
	}

	@POST
	@Path("hasDeletePermissionOnHierarchy")
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.TEXT_PLAIN)
	public boolean hasDeletePermissionOnHierarchy(@QueryParam("collection") String collection,
			@QueryParam("username") String username,
			AuthorizationResource resource) {
		User user = userServices().getUserInCollection(username, collection);
		Record record = recordServices().getDocumentById(resource.getRecordIds().get(0));
		return authorizationsServices().hasDeletePermissionOnHierarchy(user, record);
	}

	@POST
	@Path("hasRestaurationPermissionOnHierarchy")
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.TEXT_PLAIN)
	public boolean hasRestaurationPermissionOnHierarchy(@QueryParam("collection") String collection,
			@QueryParam("username") String username,
			AuthorizationResource resource) {
		User user = userServices().getUserInCollection(username, collection);
		Record record = recordServices().getDocumentById(resource.getRecordIds().get(0));
		return authorizationsServices().hasRestaurationPermissionOnHierarchy(user, record);
	}

	@POST
	@Path("reset")
	@Consumes(MediaType.TEXT_PLAIN)
	@Produces(MediaType.TEXT_PLAIN)
	public String reset(String recordId) {
		Record record = recordServices().getDocumentById(recordId);
		authorizationsServices().reset(record);
		return "Ok";
	}

	@GET
	@Path("getRecordAuthorizationCodes")
	@Produces(MediaType.APPLICATION_JSON)
	public List<String> getRecordAuthorizationCodes(@QueryParam("recordId") String recordId) {
		Record record = recordServices().getDocumentById(recordId);
		List<Authorization> authorizations = authorizationsServices().getRecordAuthorizations(record);
		List<String> authorizationsCodes = new ArrayList<>();
		for (Authorization authorization : authorizations) {
			authorizationsCodes.add(authorization.getDetail().getId());
		}
		return authorizationsCodes;
	}

	@POST
	@Path("addRole")
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.TEXT_PLAIN)
	public String addRole(@QueryParam("collection") String collection, RoleResource resource)
			throws RolesManagerRuntimeException {
		List<String> contentPermissions = new ArrayList<>();
		for (String permissionStr : resource.getPermissions()) {
			contentPermissions.add(permissionStr);
		}
		Role role = new Role(collection, resource.getId(), resource.getName(), contentPermissions);
		rolesManager().addRole(role);
		return role.getCode();
	}

	@GET
	@Path("getRoles")
	@Produces(MediaType.APPLICATION_JSON)
	public List<String> getRoles(@QueryParam("collection") String collection) {
		List<Role> roles = rolesManager().getAllRoles(collection);
		List<String> roleCodes = new ArrayList<>();
		for (Role role : roles) {
			roleCodes.add(role.getCode());
		}
		return roleCodes;
	}

	@GET
	@Path("getRole")
	@Produces(MediaType.APPLICATION_JSON)
	public RoleResource getRole(@QueryParam("collection") String collection, @QueryParam("code") String code) {
		Role role;
		try {
			role = rolesManager().getRole(collection, code);
		} catch (RolesManagerRuntimeException e) {
			return null;
		}
		return toRoleResource(role);
	}

	@GET
	@Path("changePassword")
	@Produces(MediaType.TEXT_PLAIN)
	public String changePassword(@QueryParam("username") String username, @QueryParam("newPassword") String newPassword) {
		authenticationService().changePassword(username, newPassword);
		return "Ok";
	}

	@GET
	@Path("changeOldPassword")
	@Produces(MediaType.TEXT_PLAIN)
	public String changeOldPassword(@QueryParam("username") String username, @QueryParam("oldPassword") String oldPassword,
			@QueryParam("newPassword") String newPassword) {
		authenticationService().changePassword(username, oldPassword, newPassword);
		return "Ok";
	}

	@POST
	@Path("canRead")
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.TEXT_PLAIN)
	public boolean canRead(@QueryParam("collection") String collection, @QueryParam("username") String username,
			AuthorizationResource resource) {
		User user = userServices().getUserInCollection(username, collection);
		Record record = recordServices().getDocumentById(resource.getRecordIds().get(0));
		return authorizationsServices().canRead(user, record);
	}

	@POST
	@Path("canWrite")
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.TEXT_PLAIN)
	public boolean canWrite(@QueryParam("collection") String collection, @QueryParam("username") String username,
			AuthorizationResource resource) {
		User user = userServices().getUserInCollection(username, collection);
		Record record = recordServices().getDocumentById(resource.getRecordIds().get(0));
		return authorizationsServices().canWrite(user, record);
	}

	@POST
	@Path("canDelete")
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.TEXT_PLAIN)
	public boolean canDelete(@QueryParam("collection") String collection, @QueryParam("username") String username,
			AuthorizationResource resource) {
		User user = userServices().getUserInCollection(username, collection);
		Record record = recordServices().getDocumentById(resource.getRecordIds().get(0));
		return authorizationsServices().canDelete(user, record);
	}

	@GET
	@Path("getUserCollectionPermissions")
	@Produces(MediaType.APPLICATION_JSON)
	public UserCollectionPermissionsResource getUserCollectionPermissions(@QueryParam("collection") String collection,
			@QueryParam("username") String username) {
		User user = userServices().getUserInCollection(username, collection);
		return toUserCollectionPermissionsResource(user);
	}

	@POST
	@Path("setUserCollectionPermissions")
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.TEXT_PLAIN)
	public String setUserCollectionPermissions(@QueryParam("collection") String collection,
			UserCollectionPermissionsResource resource)
			throws RolesManagerRuntimeException {

		User user = userServices().getUserInCollection(resource.getUsername(), collection);
		user.setCollectionReadAccess(resource.isReadAccess());
		user.setCollectionWriteAccess(resource.isWriteAccess());
		user.setCollectionDeleteAccess(resource.isDeleteAccess());
		user.setUserRoles(resource.getRoles());

		try {
			recordServices().update(user.getWrappedRecord());
		} catch (RecordServicesException e) {
			throw new SecurityServicesException_CannotUpdateUser(user.getUsername(), e);
		}
		return "Ok";
	}

	@GET
	@Path("getGroupCollectionPermissions")
	@Produces(MediaType.APPLICATION_JSON)
	public GroupCollectionPermissionsResource getGroupCollectionPermissions(@QueryParam("collection") String collection,
			@QueryParam("group") String groupCode) {
		Group group = userServices().getGroupInCollection(groupCode, collection);
		return toGroupCollectionPermissionsResource(group);
	}

	@POST
	@Path("setGroupCollectionPermissions")
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.TEXT_PLAIN)
	public String setGroupCollectionPermissions(@QueryParam("collection") String collection,
			GroupCollectionPermissionsResource resource)
			throws RolesManagerRuntimeException {

		Group group = userServices().getGroupInCollection(resource.getGroupCode(), collection);
		group.setRoles(resource.getRoles());
		group.setTitle(resource.getName());

		try {
			recordServices().update(group.getWrappedRecord());
		} catch (RecordServicesException e) {
			throw new SecurityServicesException_CannotUpdateGroup(group.getCode(), e);
		}
		return "Ok";
	}

	private GroupCollectionPermissionsResource toGroupCollectionPermissionsResource(Group group) {
		GroupCollectionPermissionsResource groupCollectionPermissionsResource = new GroupCollectionPermissionsResource();
		groupCollectionPermissionsResource.setGroupCode(group.getCode());
		// groupCollectionPermissionsResource.setCollection(group.getCollection());
		groupCollectionPermissionsResource.setRoles(group.getRoles());
		groupCollectionPermissionsResource.setName(group.getTitle());
		return groupCollectionPermissionsResource;
	}

	private UserCollectionPermissionsResource toUserCollectionPermissionsResource(User user) {
		UserCollectionPermissionsResource userCollectionPermissionsResource = new UserCollectionPermissionsResource();
		if (user.hasCollectionReadAccess()) {
			userCollectionPermissionsResource.setReadAccess(true);
		}
		if (user.hasCollectionDeleteAccess()) {
			userCollectionPermissionsResource.setDeleteAccess(true);
		}
		if (user.hasCollectionWriteAccess()) {
			userCollectionPermissionsResource.setWriteAccess(true);
		}
		userCollectionPermissionsResource.setRoles(user.getAllRoles());
		userCollectionPermissionsResource.setCollection(user.getCollection());
		userCollectionPermissionsResource.setUsername(user.getUsername());
		return userCollectionPermissionsResource;
	}

	private AuthorizationResource toAuthorizationResource(Authorization authorization) {
		AuthorizationResource authorizationResource = new AuthorizationResource();
		authorizationResource.setPrincipalIds(authorization.getGrantedToPrincipals());
		authorizationResource.setRecordIds(authorization.getGrantedOnRecords());
		if (authorization.getDetail().getStartDate() != null && authorization.getDetail().getEndDate() != null) {
			authorizationResource.setStartDate(authorization.getDetail().getStartDate().toDate());
			authorizationResource.setEndDate(authorization.getDetail().getEndDate().toDate());
		}
		authorizationResource.setCollection((authorization.getDetail().getCollection()));
		authorizationResource.setRoleIds(authorization.getDetail().getRoles());
		return authorizationResource;
	}

	private RoleResource toRoleResource(Role role) {
		List<String> permissions = new ArrayList<>();
		for (String operationPermision : role.getOperationPermissions()) {
			permissions.add(operationPermision);
		}
		RoleResource resource = new RoleResource();
		resource.setId(role.getCode());
		resource.setName(role.getTitle());
		resource.setPermissions(permissions);
		return resource;
	}

	private AuthorizationsServices authorizationsServices() {
		return AdminServicesUtils.modelServicesFactory().newAuthorizationsServices();
	}

	private CustomizedAuthorizationsBehavior getCustomizedAuthorizationsBehavior(boolean keepAttached) {
		CustomizedAuthorizationsBehavior behavior = keepAttached ? CustomizedAuthorizationsBehavior.KEEP_ATTACHED
				: CustomizedAuthorizationsBehavior.DETACH;
		return behavior;
	}

	private CustomizedAuthorizationsBehavior getCustomizedAuthorizationsBehavior(String keepAttached) {
		CustomizedAuthorizationsBehavior behavior = keepAttached.equals("true") ? CustomizedAuthorizationsBehavior.KEEP_ATTACHED
				: CustomizedAuthorizationsBehavior.DETACH;
		return behavior;
	}

	private RecordServices recordServices() {
		return AdminServicesUtils.modelServicesFactory().newRecordServices();
	}

	private UserServices userServices() {
		return AdminServicesUtils.modelServicesFactory().newUserServices();
	}

	private RolesManager rolesManager() {
		return AdminServicesUtils.modelServicesFactory().getRolesManager();
	}

	private MetadataSchemasManager schemasManager() {
		return AdminServicesUtils.modelServicesFactory().getMetadataSchemasManager();
	}

	private AuthenticationService authenticationService() {
		return AdminServicesUtils.modelServicesFactory().newAuthenticationService();
	}

	private String getUniqueId() {
		return AdminServicesUtils.daosServicesFactory().getUniqueIdGenerator().next();
	}

}
