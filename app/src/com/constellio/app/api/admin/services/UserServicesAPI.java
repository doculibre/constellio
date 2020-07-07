package com.constellio.app.api.admin.services;

import com.constellio.app.client.entities.GlobalGroupResource;
import com.constellio.app.client.entities.GroupResource;
import com.constellio.app.client.entities.UserResource;
import com.constellio.model.entities.records.wrappers.Group;
import com.constellio.model.entities.security.global.GlobalGroup;
import com.constellio.model.entities.security.global.GlobalGroupStatus;
import com.constellio.model.entities.security.global.UserCredential;
import com.constellio.model.services.users.UserServices;
import org.joda.time.LocalDateTime;

import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

@Path("users")
@Consumes("application/xml")
@Produces("application/xml")
public class UserServicesAPI {

	@POST
	@Path("addUpdateUserCredential")
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.TEXT_PLAIN)
	public String addUpdateUserCredential(@Context HttpHeaders httpHeaders, UserResource userResource) {
		UserCredential userCredential = toCredential(userResource);
		userServices().addUpdateUserCredential(userCredential);
		return "OK";
	}

	@POST
	@Path("addUpdateGlobalGroup")
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.TEXT_PLAIN)
	public String addUpdateGlobalGroup(GlobalGroupResource globalGroupResource) {
		GlobalGroup group = userServices().createGlobalGroup(globalGroupResource.getCode(), globalGroupResource.getName(),
				globalGroupResource.getUsersAutomaticallyAddedToCollections(), globalGroupResource.getParent(),
				globalGroupResource.getStatus(), globalGroupResource.isLocallyCreated());
		userServices().addUpdateGlobalGroup(group);
		return "OK";
	}

	@POST
	@Path("addUserToCollection")
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.TEXT_PLAIN)
	public String addUserToCollection(UserResource resource) {
		UserCredential userCredential = userServices().getUser(resource.getUsername());
		userServices().addUserToCollection(userCredential, resource.getCollections().get(0));
		return "OK";
	}

	@POST
	@Path("generateServiceKeyForUser")
	@Consumes(MediaType.TEXT_PLAIN)
	@Produces(MediaType.TEXT_PLAIN)
	public String generateServiceKeyForUser(String username) {
		UserCredential userCredential = userServices().getUser(username);
		return userServices().giveNewServiceToken(userCredential);
	}

	@POST
	@Path("setGlobalGroupUsers")
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.TEXT_PLAIN)
	public String setGlobalGroupUsers(GlobalGroupResource resource) {
		List<UserCredential> userCredentials = new ArrayList<>();
		for (String username : resource.getUsersAutomaticallyAddedToCollections()) {
			userCredentials.add(userServices().getUser(username));
		}
		userServices().setGlobalGroupUsers(resource.getCode(), userCredentials);
		return "OK";
	}

	@GET
	@Path("getGlobalGroupUsers")
	@Produces(MediaType.APPLICATION_JSON)
	public List<String> getGlobalGroupUsers(@QueryParam("groupCode") String groupCode) {
		List<String> globalGroupUsernames = new ArrayList<>();
		for (UserCredential userCredential : userServices().getGlobalGroupActifUsers(groupCode)) {
			globalGroupUsernames.add(userCredential.getUsername());
		}
		return globalGroupUsernames;
	}

	@GET
	@Path("getUser")
	@Produces(MediaType.APPLICATION_JSON)
	public UserResource getUser(@QueryParam("username") String username) {
		UserCredential userCredential = userServices().getUser(username);
		return toData(userCredential);
	}

	@GET
	@Path("getGlobalGroup")
	@Produces(MediaType.APPLICATION_JSON)
	public GlobalGroupResource getGlobalGroup(@QueryParam("groupCode") String groupCode) {
		GlobalGroup globalGroup = userServices().getGroup(groupCode);

		if (globalGroup.getStatus() == GlobalGroupStatus.INACTIVE) {
			throw new RuntimeException("Group is inactive");
		}

		return toGlobalGroupResource(globalGroup);
	}

	@POST
	@Path("removeUserFromCollection")
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.TEXT_PLAIN)
	public String removeUserFromCollection(UserResource resource) {
		UserCredential userCredential = userServices().getUser(resource.getUsername());
		userServices().removeUserFromCollection(userCredential, resource.getCollections().get(0));
		return "OK";
	}

	@POST
	@Path("removeGlobalGroup")
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.TEXT_PLAIN)
	public String removeGlobalGroup(@Context HttpHeaders httpHeaders, GlobalGroupResource resource) {
		GlobalGroup globalGroup = userServices().getGroup(resource.getCode());
		String user = AdminServiceAuthenticator.getAuthenticatedUser(httpHeaders);
		UserCredential userCredential = AdminServicesUtils.modelServicesFactory().newUserServices().getUser(user);
		userServices().logicallyRemoveGroupHierarchy(userCredential, globalGroup);
		return "OK";
	}

	@POST
	@Path("removeCollectionGroup")
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.TEXT_PLAIN)
	public String removeCollectionGroup(@Context HttpHeaders httpHeaders, GroupResource resource) {
		String user = AdminServiceAuthenticator.getAuthenticatedUser(httpHeaders);
		UserCredential userCredential = AdminServicesUtils.modelServicesFactory().newUserServices().getUser(user);
		userServices().removeGroupFromCollections(userCredential, resource.getCode(), Arrays.asList(resource.getCollection()));
		return "OK";
	}

	@POST
	@Path("createCustomGroupInCollectionWithCodeAndName")
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.TEXT_PLAIN)
	public String createCustomGroupInCollectionWithCodeAndName(GroupResource resource) {
		userServices().createCustomGroupInCollectionWithCodeAndName(resource.getCollection(), resource.getCode(),
				resource.getName());
		return "OK";
	}

	@GET
	@Path("getCustomGroupsInCollections")
	@Produces(MediaType.APPLICATION_JSON)
	public List<String> getCustomGroupsInCollections(@QueryParam("collection") String collection) {
		List<String> groupCodes = new ArrayList<>();
		for (Group group : userServices().getCollectionGroups(collection)) {
			groupCodes.add(group.getCode());
		}
		return groupCodes;
	}

	@POST
	@Path("removeUserFromGlobalGroup")
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.TEXT_PLAIN)
	public String removeUserFromGlobalGroup(@Context HttpHeaders httpHeaders, UserResource resource) {
		String groupCode = resource.getGlobalGroups().get(0);
		String username = resource.getUsername();
		userServices().removeUserFromGlobalGroup(username, groupCode);
		return "OK";
	}

	private UserServices userServices() {
		return AdminServicesUtils.modelServicesFactory().newUserServices();
	}

	private UserCredential toCredential(UserResource userResource) {
		Map<String, LocalDateTime> tokens = new HashMap<>();
		for (Entry<String, String> token : userResource.getTokens().entrySet()) {
			tokens.put(token.getKey(), LocalDateTime.parse(token.getValue()));

		}

		return userServices().addEdit(userResource.getUsername())
				.setFirstName(userResource.getFirstName())
				.setLastName(userResource.getLastName())
				.setEmail(userResource.getEmail())
				.setServiceKey(userResource.getServiceKey())
				.setSystemAdmin(userResource.isSystemAdmin())
				.setGlobalGroups(userResource.getGlobalGroups())
				.setCollections(userResource.getCollections())
				.setAccessTokens(tokens)
				.setStatus(userResource.getStatus())
				.setDomain(userResource.getDomain())
				.setMsExchDelegateListBL(Arrays.asList(""))
				.setDn(null);
	}

	private UserResource toData(UserCredential userCredential) {
		UserResource userResource = new UserResource();
		userResource.setUsername(userCredential.getUsername());
		userResource.setFirstName(userCredential.getFirstName());
		userResource.setLastName(userCredential.getLastName());
		userResource.setEmail(userCredential.getEmail());
		userResource.setGlobalGroups(userCredential.getGlobalGroups());
		userResource.setCollections(userCredential.getCollections());
		userResource.setServiceKey(userCredential.getServiceKey());
		userResource.setSystemAdmin(userCredential.isSystemAdmin());
		Map<String, String> tokens = new HashMap<String, String>();
		for (Entry<String, LocalDateTime> token : userCredential.getAccessTokens().entrySet()) {
			tokens.put(token.getKey(), token.getValue().toString());
		}
		userResource.setTokens(tokens);
		return userResource;
	}

	private GlobalGroupResource toGlobalGroupResource(GlobalGroup group) {
		GlobalGroupResource resource = new GlobalGroupResource();
		resource.setCode(group.getCode());
		resource.setName(group.getName());
		resource.setUsersAutomaticallyAddedToCollections(group.getUsersAutomaticallyAddedToCollections());
		return resource;
	}
}
