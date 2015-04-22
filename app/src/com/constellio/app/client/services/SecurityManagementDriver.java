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
package com.constellio.app.client.services;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.ws.rs.client.Entity;
import javax.ws.rs.client.Invocation.Builder;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.MediaType;

import com.constellio.app.client.AdminServicesConstants;
import com.constellio.app.client.entities.AuthorizationResource;
import com.constellio.app.client.entities.GroupCollectionPermissionsResource;
import com.constellio.app.client.entities.RoleResource;
import com.constellio.app.client.entities.UserCollectionPermissionsResource;

public class SecurityManagementDriver {

	final WebTarget target;
	final String token;
	final String serviceKey;
	final String collection;

	SecurityManagementDriver(WebTarget target, String token, String serviceKey, String collection) {
		this.target = target;
		this.token = token;
		this.serviceKey = serviceKey;
		this.collection = collection;
	}

	public String addAuthorization(AuthorizationResource resource, boolean keepAttached) {
		String keepAttachedStr = keepAttached ? "true" : "false";
		Map<String, String> queryParams = new HashMap<String, String>();
		queryParams.put("keepAttached", keepAttachedStr);
		return requestString("addAuthorization", queryParams).post(Entity.json(resource), String.class);
	}

	public AuthorizationResource getAuthorization(String authId) {
		Map<String, String> queryParams = new HashMap<String, String>();
		queryParams.put("authId", authId);
		return requestJson("getAuthorization", queryParams).get(AuthorizationResource.class);
	}

	public String removeAuthorizationOnRecord(String authorizationId, String recordId, boolean keepAttached) {
		AuthorizationResource resource = new AuthorizationResource();
		resource.setRecordIds(Arrays.asList(recordId));
		String keepAttachedStr = keepAttached ? "true" : "false";
		Map<String, String> queryParams = new HashMap<String, String>();
		queryParams.put("authorizationId", authorizationId);
		queryParams.put("keepAttached", keepAttachedStr);
		return requestString("removeAuthorizationOnRecord", queryParams).post(Entity.json(resource), String.class);
	}

	public String modify(String authorizationId, AuthorizationResource resource, boolean keepAttached) {
		String keepAttachedStr = keepAttached ? "true" : "false";
		Map<String, String> queryParams = new HashMap<String, String>();
		queryParams.put("authorizationId", authorizationId);
		queryParams.put("keepAttached", keepAttachedStr);
		return requestString("modify", queryParams).post(Entity.json(resource), String.class);

	}

	public List<String> getRecordAuthorizationCodes(String recordId) {
		Map<String, String> queryParams = new HashMap<String, String>();
		queryParams.put("recordId", recordId);
		return requestJson("getRecordAuthorizationCodes", queryParams).get(List.class);
	}

	public String reset(String recordId) {
		return requestString("reset").post(Entity.text(recordId), String.class);
	}

	public boolean hasRestaurationPermissionOnHierarchy(String username, String recordId) {
		AuthorizationResource resource = new AuthorizationResource();
		resource.setRecordIds(Arrays.asList(recordId));
		Map<String, String> queryParams = new HashMap<String, String>();
		queryParams.put("username", username);
		return requestString("hasRestaurationPermissionOnHierarchy", queryParams).post(Entity.json(resource), Boolean.class);
	}

	public String changePassword(String username, String newPassword) {
		Map<String, String> queryParams = new HashMap<String, String>();
		queryParams.put("username", username);
		queryParams.put("newPassword", newPassword);
		return requestString("changePassword", queryParams).get(String.class);
	}

	public String changePassword(String username, String oldPassword, String newPassword) {
		Map<String, String> queryParams = new HashMap<String, String>();
		queryParams.put("username", username);
		queryParams.put("oldPassword", oldPassword);
		queryParams.put("newPassword", newPassword);
		return requestString("changeOldPassword", queryParams).get(String.class);
	}

	public String addRole(String code, String title, List<String> permissions) {
		RoleResource resource = new RoleResource();
		resource.setId(code);
		resource.setName(title);
		resource.setPermissions(permissions);
		return requestString("addRole").post(Entity.json(resource), String.class);
	}

	public List<String> getRoles() {
		return requestJson("getRoles").get(List.class);
	}

	public RoleResource getRole(String code) {
		Map<String, String> queryParams = new HashMap<String, String>();
		queryParams.put("code", code);
		return requestJson("getRole", queryParams).get(RoleResource.class);
	}

	public boolean canRead(String username, String recordId) {
		AuthorizationResource resource = new AuthorizationResource();
		resource.setRecordIds(Arrays.asList(recordId));
		Map<String, String> queryParams = new HashMap<String, String>();
		queryParams.put("username", username);
		return requestString("canRead", queryParams).post(Entity.json(resource), Boolean.class);
	}

	public boolean canWrite(String username, String recordId) {
		AuthorizationResource resource = new AuthorizationResource();
		resource.setRecordIds(Arrays.asList(recordId));
		Map<String, String> queryParams = new HashMap<String, String>();
		queryParams.put("username", username);
		return requestString("canWrite", queryParams).post(Entity.json(resource), Boolean.class);
	}

	public boolean canDelete(String username, String recordId) {
		AuthorizationResource resource = new AuthorizationResource();
		resource.setRecordIds(Arrays.asList(recordId));
		Map<String, String> queryParams = new HashMap<String, String>();
		queryParams.put("username", username);
		return requestString("canDelete", queryParams).post(Entity.json(resource), Boolean.class);
	}

	public boolean hasPermission(String username, String recordId, String permission) {
		AuthorizationResource resource = new AuthorizationResource();
		resource.setRecordIds(Arrays.asList(recordId));
		Map<String, String> queryParams = new HashMap<String, String>();
		queryParams.put("username", username);
		queryParams.put("permission", permission);
		return requestString("hasContentPermission", queryParams).post(Entity.json(resource), Boolean.class);
	}

	public boolean hasDeletePermissionOnPrincipalConceptHierarchyAndIncludedRecords(String username, String principalConceptId) {
		AuthorizationResource resource = new AuthorizationResource();
		resource.setRecordIds(Arrays.asList(principalConceptId));
		Map<String, String> queryParams = new HashMap<String, String>();
		queryParams.put("username", username);
		return requestString("hasDeletePermissionOnPrincipalConceptHierarchyAndIncludedRecords", queryParams).post(
				Entity.json(resource), Boolean.class);
	}

	public boolean hasDeletePermissionOnHierarchy(String username, String recordId) {
		AuthorizationResource resource = new AuthorizationResource();
		resource.setRecordIds(Arrays.asList(recordId));
		Map<String, String> queryParams = new HashMap<String, String>();
		queryParams.put("username", username);
		return requestString("hasDeletePermissionOnHierarchy", queryParams).post(Entity.json(resource), Boolean.class);
	}

	public UserCollectionPermissionsResource getUserCollectionPermissions(String username) {
		Map<String, String> queryParams = new HashMap<String, String>();
		queryParams.put("username", username);
		return requestJson("getUserCollectionPermissions", queryParams).get(UserCollectionPermissionsResource.class);
	}

	public String setUserCollectionPermissions(UserCollectionPermissionsResource resource) {
		return requestString("setUserCollectionPermissions").post(Entity.json(resource), String.class);
	}

	public GroupCollectionPermissionsResource getGroupCollectionPermissions(String groupCode) {
		Map<String, String> queryParams = new HashMap<String, String>();
		queryParams.put("group", groupCode);
		return requestJson("getGroupCollectionPermissions", queryParams).get(GroupCollectionPermissionsResource.class);
	}

	public String setGroupCollectionPermissions(GroupCollectionPermissionsResource resource) {
		return requestString("setGroupCollectionPermissions").post(Entity.json(resource), String.class);
	}

	private Builder requestJson(String service, Map<String, String> queryParams) {
		return path(service, queryParams).request(MediaType.APPLICATION_JSON_TYPE)
				.header(AdminServicesConstants.AUTH_TOKEN, token).header(AdminServicesConstants.SERVICE_KEY, serviceKey);
	}

	private Builder requestJson(String service) {
		return path(service, new HashMap<String, String>()).request(MediaType.APPLICATION_JSON_TYPE)
				.header(AdminServicesConstants.AUTH_TOKEN, token).header(AdminServicesConstants.SERVICE_KEY, serviceKey);
	}

	private Builder requestString(String service, Map<String, String> queryParams) {
		return path(service, queryParams).request(MediaType.TEXT_PLAIN).header(AdminServicesConstants.AUTH_TOKEN, token)
				.header(AdminServicesConstants.SERVICE_KEY, serviceKey);
	}

	private Builder request(String service, Map<String, String> queryParams) {
		return path(service, queryParams).request().header(AdminServicesConstants.AUTH_TOKEN, token)
				.header(AdminServicesConstants.SERVICE_KEY, serviceKey);
	}

	private Builder requestString(String service) {
		return path(service, new HashMap<String, String>()).request(MediaType.TEXT_PLAIN)
				.header(AdminServicesConstants.AUTH_TOKEN, token).header(AdminServicesConstants.SERVICE_KEY, serviceKey);
	}

	private WebTarget path(String service, Map<String, String> queryParams) {

		WebTarget target = this.target.queryParam("collection", collection);
		for (Map.Entry<String, String> queryParam : queryParams.entrySet()) {
			target = target.queryParam(queryParam.getKey(), queryParam.getValue());
		}
		return target.path(service);
	}
}