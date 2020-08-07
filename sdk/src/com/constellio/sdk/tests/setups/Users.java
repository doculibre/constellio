package com.constellio.sdk.tests.setups;

import com.constellio.model.entities.records.wrappers.Group;
import com.constellio.model.entities.records.wrappers.User;
import com.constellio.model.entities.security.global.GlobalGroupStatus;
import com.constellio.model.entities.security.global.GroupAddUpdateRequest;
import com.constellio.model.entities.security.global.SystemWideGroup;
import com.constellio.model.entities.security.global.UserCredentialStatus;
import com.constellio.model.entities.security.global.UserSyncMode;
import com.constellio.model.services.security.authentification.AuthenticationService;
import com.constellio.model.services.users.SystemWideUserInfos;
import com.constellio.model.services.users.UserPhotosServices;
import com.constellio.model.services.users.UserServices;
import org.apache.commons.io.IOUtils;

import java.io.InputStream;
import java.util.Arrays;
import java.util.List;

import static org.apache.ignite.internal.util.lang.GridFunc.asList;

public class Users {

	String admin = "admin";
	String heroes = "heroes";
	String legends = "legends";
	String sidekicks = "sidekicks";
	String rumors = "rumors";
	String chuckNorrisUsername = "chuck";
	String aliceUsername = "alice";
	String bobGrattonUsername = "bob";
	String charlesFrancoisXavierUsername = "charles";
	String dakotaLindienUsername = "dakota";
	String edouardLechatUsername = "edouard";
	String gandalfLeblancUsername = "gandalf";
	String robinUsername = "robin";
	String sasquatchUsername = "sasquatch";

	UserServices userServices;

	String collection;

	public SystemWideUserInfos chuckNorris() {
		return getUser(chuckNorrisUsername);
	}

	public com.constellio.model.services.users.UserAddUpdateRequest chuckNorrisAddUpdateRequest() {
		return addUpdateRequest(chuckNorrisUsername);
	}

	public User chuckNorrisIn(String collection) {
		return getUser(chuckNorrisUsername, collection);
	}

	public SystemWideUserInfos admin() {
		return userServices.getUserInfos(admin);
	}

	public com.constellio.model.services.users.UserAddUpdateRequest adminAddUpdateRequest() {
		return addUpdateRequest(admin);
	}


	public User adminIn(String collection) {
		return userServices.getUserInCollection(admin, collection);
	}

	public SystemWideUserInfos alice() {
		return getUser(aliceUsername);
	}

	public com.constellio.model.services.users.UserAddUpdateRequest aliceAddUpdateRequest() {
		return addUpdateRequest(aliceUsername);
	}


	public User aliceIn(String collection) {
		return getUser(aliceUsername, collection);
	}

	public SystemWideUserInfos bob() {
		return getUser(bobGrattonUsername);
	}

	public com.constellio.model.services.users.UserAddUpdateRequest bobAddUpdateRequest() {
		return addUpdateRequest(bobGrattonUsername);
	}


	public User bobIn(String collection) {
		return getUser(bobGrattonUsername, collection);
	}

	public SystemWideUserInfos charles() {
		return getUser(charlesFrancoisXavierUsername);
	}

	public com.constellio.model.services.users.UserAddUpdateRequest charlesAddUpdateRequest() {
		return addUpdateRequest(charlesFrancoisXavierUsername);
	}

	public User charlesIn(String collection) {
		return getUser(charlesFrancoisXavierUsername, collection);
	}

	public SystemWideUserInfos dakotaLIndien() {
		return getUser(dakotaLindienUsername);
	}

	public com.constellio.model.services.users.UserAddUpdateRequest dakotaAddUpdateRequest() {
		return addUpdateRequest(dakotaLindienUsername);
	}


	public User dakotaIn(String collection) {
		return dakotaLIndienIn(collection);
	}

	public User dakotaLIndienIn(String collection) {
		return getUser(dakotaLindienUsername, collection);
	}

	public SystemWideUserInfos edouardLechat() {
		return getUser(edouardLechatUsername);
	}

	public com.constellio.model.services.users.UserAddUpdateRequest edouardAddUpdateRequest() {
		return addUpdateRequest(edouardLechatUsername);
	}


	public User edouardIn(String collection) {
		return edouardLechatIn(collection);
	}

	public User edouardLechatIn(String collection) {
		return getUser(edouardLechatUsername, collection);
	}

	public SystemWideUserInfos gandalfLeblanc() {
		return getUser(gandalfLeblancUsername);
	}

	public com.constellio.model.services.users.UserAddUpdateRequest gandalfAddUpdateRequest() {
		return addUpdateRequest(gandalfLeblancUsername);
	}


	public SystemWideUserInfos robin() {
		return getUser(robinUsername);
	}

	public com.constellio.model.services.users.UserAddUpdateRequest robinAddUpdateRequest() {
		return addUpdateRequest(robinUsername);
	}


	public SystemWideUserInfos sasquatch() {
		return getUser(sasquatchUsername);
	}

	public com.constellio.model.services.users.UserAddUpdateRequest sasquatchAddUpdateRequest() {
		return addUpdateRequest(sasquatchUsername);
	}


	public User gandalfIn(String collection) {
		return gandalfLeblancIn(collection);
	}

	public User gandalfLeblancIn(String collection) {
		return getUser(gandalfLeblancUsername, collection);
	}

	public User robinIn(String collection) {
		return getUser(robinUsername, collection);
	}

	public User sasquatchIn(String collection) {
		return getUser(sasquatchUsername, collection);
	}

	public List<SystemWideUserInfos> getAllUsers() {
		return Arrays.asList(
				chuckNorris(), admin(), alice(), bob(), charles(), dakotaLIndien(), edouardLechat(), gandalfLeblanc(), robin(),
				sasquatch());
	}

	public SystemWideGroup heroes() {
		return getGroup(heroes);
	}

	public GroupAddUpdateRequest heroesRequest() {
		return getGroupRequest(heroes);
	}


	public Group heroesIn(String collection) {
		return getGroup(heroes, collection);
	}

	public SystemWideGroup legends() {
		return getGroup(legends);
	}

	public GroupAddUpdateRequest legendsRequest() {
		return getGroupRequest(legends);
	}


	public Group legendsIn(String collection) {
		return getGroup(legends, collection);
	}

	public SystemWideGroup sidekicks() {
		return getGroup(sidekicks);
	}


	public GroupAddUpdateRequest sidekicksRequest() {
		return getGroupRequest(sidekicks);
	}

	public Group sidekicksIn(String collection) {
		return getGroup(sidekicks, collection);
	}

	public SystemWideGroup rumors() {
		return getGroup(rumors);
	}

	public GroupAddUpdateRequest rumorsRequest() {
		return getGroupRequest(rumors);
	}

	public Group rumorsIn(String collection) {
		return getGroup(rumors, collection);
	}

	public Users using(UserServices userServices) {
		this.userServices = userServices;
		return this;
	}

	public Users setUp(UserServices userServices) {
		this.userServices = userServices;
		if (userServices.getUserCredential(dakotaLindienUsername) == null) {

			if ("true".equals(System.getProperty("normalUsers"))) {
				addGroup(heroes, "The heroes", null);
				addGroup(legends, "The legends", null);
				addGroup(sidekicks, "The sidekicks", heroes);
				addGroup(rumors, "The rumors", legends);

				addUser(chuckNorrisUsername, "Chuck", "Norris");
				addUser(aliceUsername, "Alice", "Wonderland", "legends");
				addUser(bobGrattonUsername, "Bob 'Elvis'", "Gratton");
				addUser(charlesFrancoisXavierUsername, "Charles-François", "Xavier", "heroes");
				addUser(dakotaLindienUsername, "John", "Smith", "heroes");
				addUser(edouardLechatUsername, "Eddie", "Murphy", "legends");
				addUser(gandalfLeblancUsername, "Gandalf", "Leblanc", "legends", "heroes");
				addUser(robinUsername, "Good Guy", "Robin", "sidekicks");
				addUser(sasquatchUsername, "Big", "Foot", "rumors");

			} else {
				addGroup(heroes, "The heroes", null);
				addGroup(legends, "The legends", null);
				addGroup(sidekicks, "The sidekicks", heroes);
				addGroup(rumors, "The rumors", legends);

				addUser(chuckNorrisUsername, "Chuck", "Norris");
				addUser(aliceUsername, "Alice", "Wonderland", "legends");
				addUser(bobGrattonUsername, "Bob 'Elvis'", "Gratton");
				addUser(charlesFrancoisXavierUsername, "Charles-François", "Xavier", "heroes");
				addUser(dakotaLindienUsername, "Dakota", "L'Indien", "heroes");
				addUser(edouardLechatUsername, "Edouard", "Lechat", "legends");
				addUser(gandalfLeblancUsername, "Gandalf", "Leblanc", "legends", "heroes");
				addUser(robinUsername, "Good Guy", "Robin", "sidekicks");
				addUser(sasquatchUsername, "Big", "Foot", "rumors");
			}
		}
		return this;
	}

	public Users withPhotos(UserPhotosServices userPhotosServices) {
		try {
			//addUserPhoto(userPhotosServices, chuckNorrisUsername);
			//addUserPhoto(userPhotosServices, aliceUsername);
			//addUserPhoto(userPhotosServices, bobGrattonUsername);
			//addUserPhoto(userPhotosServices, charlesFrancoisXavierUsername);
			//addUserPhoto(userPhotosServices, dakotaLindienUsername);
			//addUserPhoto(userPhotosServices, edouardLechatUsername);
			//addUserPhoto(userPhotosServices, gandalfLeblancUsername);
			//addUserPhoto(userPhotosServices, sasquatchUsername);
			//addUserPhoto(userPhotosServices, robinUsername);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return this;
	}

	private void addUserPhoto(UserPhotosServices userPhotosServices, String userName) {
		InputStream inputStream = Users.class.getResourceAsStream("Users-" + userName + ".jpg");
		try {
			userPhotosServices.changePhoto(inputStream, userName);
		} finally {
			IOUtils.closeQuietly(inputStream);
		}

	}

	private SystemWideUserInfos getUser(String username) {
		return userServices.getUserInfos(username);
	}

	private com.constellio.model.services.users.UserAddUpdateRequest addUpdateRequest(String username) {
		return userServices.addUpdate(username);
	}

	private User getUser(String username, String collection) {
		return userServices.getUserInCollection(username, collection);
	}

	private SystemWideGroup getGroup(String code) {
		return userServices.getGroup(code);
	}

	private GroupAddUpdateRequest getGroupRequest(String code) {
		return userServices.request(code);
	}

	private Group getGroup(String code, String collection) {
		return userServices.getGroupInCollection(code, collection);
	}

	private void addUser(String username, String firstName, String lastName, String... groups) {
		String email = (username + "@doculibre.com").toLowerCase();
		List<String> globalGroups = Arrays.asList(groups);
		List<String> collections = asList(collection);
		com.constellio.model.services.users.UserAddUpdateRequest credential = userServices.addUpdate(username)
				.setFirstName(firstName)
				.setLastName(lastName)
				.setEmail(email)
				.setServiceKey(null)
				.setSystemAdmin(false)
				.addToGroupsInEachCollection(globalGroups)
				.addToCollections(collections)
				.setStatusForAllCollections(UserCredentialStatus.ACTIVE)
				.setSyncMode(UserSyncMode.LOCALLY_CREATED)
				.setDomain("domain")
				.setMsExchDelegateListBL(Arrays.asList(""))
				.setDn(null);

		userServices.execute(credential);
	}

	private void addGroup(String code, String title, String parent) {
		GroupAddUpdateRequest group = userServices.createGlobalGroup(
				code, title, asList(collection), parent, GlobalGroupStatus.ACTIVE, true);
		userServices.execute(group);
	}

	public Users withPasswords(AuthenticationService authenticationService) {
		authenticationService.changePassword(chuckNorrisUsername, "password");
		authenticationService.changePassword(aliceUsername, "password");
		authenticationService.changePassword(bobGrattonUsername, "password");
		authenticationService.changePassword(charlesFrancoisXavierUsername, "password");
		authenticationService.changePassword(dakotaLindienUsername, "password");
		authenticationService.changePassword(edouardLechatUsername, "password");
		authenticationService.changePassword(gandalfLeblancUsername, "password");
		authenticationService.changePassword(robinUsername, "password");
		authenticationService.changePassword(sasquatchUsername, "password");

		return this;
	}

	public Users withCollection(String collection) {
		this.collection = collection;
		return this;
	}
}
