package com.constellio.app.client.services;

import com.constellio.app.client.AdminServicesConstants;
import com.constellio.app.client.entities.GlobalGroupResource;
import com.constellio.app.client.entities.GroupResource;
import com.constellio.app.client.entities.UserResource;

import javax.ws.rs.client.Entity;
import javax.ws.rs.client.Invocation.Builder;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.MediaType;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class UserServicesClient {

	final WebTarget target;
	final String token;
	final String serviceKey;

	UserServicesClient(WebTarget target, String token, String serviceKey) {
		this.target = target;
		this.token = token;
		this.serviceKey = serviceKey;
	}

	public String addUpdateUserCredential(UserResource userResource) {
		return requestString("execute").post(Entity.json(userResource), String.class);
	}

	public String addUpdateGlobalGroup(GlobalGroupResource resource) {
		return requestString("addUpdateGlobalGroup").post(Entity.json(resource), String.class);
	}

	public String addUserToCollection(String user, String collection) {
		UserResource resource = new UserResource();
		resource.setUsername(user);
		resource.setCollections(Arrays.asList(collection));
		return requestString("addUserToCollection").post(Entity.json(resource), String.class);
	}

	public UserResource getUser(String username) {
		Map<String, String> queryParams = new HashMap<String, String>();
		queryParams.put("username", username);
		return request("getUser", queryParams).get(UserResource.class);
	}

	public String generateServiceKeyForUser(String username) {
		return requestString("generateServiceKeyForUser").post(Entity.text(username), String.class);
	}

	public String setGlobalGroupUsers(String groupCode, List<String> usernames) {
		GlobalGroupResource resource = new GlobalGroupResource();
		resource.setCode(groupCode);
		resource.setUsersAutomaticallyAddedToCollections(usernames);
		return requestString("setGlobalGroupUsers").post(Entity.json(resource), String.class);
	}

	public List<String> getGlobalGroupUsers(String groupCode) {
		Map<String, String> queryParams = new HashMap<String, String>();
		queryParams.put("groupCode", groupCode);
		return requestJson("getGlobalGroupUsers", queryParams).get(List.class);
	}

	public GlobalGroupResource getGlobalGroup(String groupCode) {
		Map<String, String> queryParams = new HashMap<String, String>();
		queryParams.put("groupCode", groupCode);
		return requestJson("getGlobalGroup", queryParams).get(GlobalGroupResource.class);
	}

	public String removeUserFromCollection(String username, String collection) {
		UserResource resource = new UserResource();
		resource.setCollections(Arrays.asList(collection));
		resource.setUsername(username);
		return requestString("removeUserFromCollection").post(Entity.json(resource), String.class);
	}

	public String removeGlobalGroup(String group) {
		GlobalGroupResource resource = new GlobalGroupResource();
		resource.setCode(group);
		return requestString("removeGlobalGroup").post(Entity.json(resource), String.class);
	}

	public String removeUserFromGlobalGroup(String username, String groupCode) {
		UserResource resource = new UserResource();
		resource.setGlobalGroups(Arrays.asList(groupCode));
		resource.setUsername(username);
		return requestString("removeUserFromGlobalGroup").post(Entity.json(resource), String.class);
	}

	public String createCollectionGroup(String collection, String code, String name) {
		GroupResource resource = new GroupResource();
		resource.setCollection(collection);
		resource.setCode(code);
		resource.setName(name);
		return requestString("createCustomGroupInCollectionWithCodeAndName").post(Entity.json(resource), String.class);
	}

	public String removeCollectionGroup(String collection, String groupCode) {
		GroupResource resource = new GroupResource();
		resource.setCollection(collection);
		resource.setCode(groupCode);
		return requestString("removeCollectionGroup").post(Entity.json(resource), String.class);
	}

	public List<String> getCollectionGroups(String collection) {
		Map<String, String> queryParams = new HashMap<String, String>();
		queryParams.put("collection", collection);
		return requestJson("getCustomGroupsInCollections", queryParams).get(List.class);
	}

	private Builder requestJson(String service) {
		return target.path(service).request(MediaType.APPLICATION_JSON_TYPE).header(AdminServicesConstants.AUTH_TOKEN, token)
				.header(AdminServicesConstants.SERVICE_KEY, serviceKey);
	}

	private Builder requestString(String service) {
		return target.path(service).request(MediaType.TEXT_PLAIN).header(AdminServicesConstants.AUTH_TOKEN, token)
				.header(AdminServicesConstants.SERVICE_KEY, serviceKey);
	}

	private Builder request(String service) {
		return target.path(service).request().header(AdminServicesConstants.AUTH_TOKEN, token)
				.header(AdminServicesConstants.SERVICE_KEY, serviceKey);
	}

	private Builder requestJson(String service, Map<String, String> queryParams) {
		return path(service, queryParams).request(MediaType.APPLICATION_JSON_TYPE)
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

	private WebTarget path(String service, Map<String, String> queryParams) {

		WebTarget target = this.target;
		for (Map.Entry<String, String> queryParam : queryParams.entrySet()) {
			target = target.queryParam(queryParam.getKey(), queryParam.getValue());
		}
		return target.path(service);
	}
}
