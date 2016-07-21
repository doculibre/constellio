package com.constellio.app.api.cmis.accept;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.ArrayList;
import java.util.List;

import org.apache.chemistry.opencmis.client.api.Repository;
import org.apache.chemistry.opencmis.client.api.Session;
import org.junit.Before;
import org.junit.Test;

import com.constellio.model.services.users.UserServices;
import com.constellio.sdk.tests.ConstellioTest;
import com.constellio.sdk.tests.setups.Users;

public class CmisAuthenticationAcceptanceTest extends ConstellioTest {

	UserServices userServices;
	Users users = new Users();

	String bobServiceKey = "bobKey";
	String chuckNorrisServiceKey = "chuckKey";
	String adminServiceKey = "adminKey";
	String adminToken, bobToken, chuckNorrisToken;

	@Before
	public void setUp()
			throws Exception {

		prepareSystem(
				withZeCollection().withAllTest(users),
				withCollection("anotherCollection").withAllTestUsers());

		userServices = getModelLayerFactory().newUserServices();

		adminToken = userServices.generateToken(users.admin().getUsername());

		userServices.addUpdateUserCredential(users.bob().withServiceKey(bobServiceKey));
		bobToken = userServices.generateToken(users.bob().getUsername());

		userServices.addUpdateUserCredential(users.admin().withServiceKey(adminServiceKey));

		userServices
				.addUpdateUserCredential(users.chuckNorris().withServiceKey(chuckNorrisServiceKey).withSystemAdminPermission());
		chuckNorrisToken = userServices.generateToken(users.chuckNorris().getUsername());
	}

	@Test
	public void whenAuthenticatingToCmisThenBasedOnServiceKeyAndTokensAndRestrictedToSystemAdmins()
			throws Exception {

		//- whenSearchingWithAvalidServiceKeyFromAnotherUserThenException();
		//- whenSearchingWithInvalidTokenThenException();
		//- whenSearchingWithNoTokenThenException();
		//- whenSearchingWithNoServiceKeyThenException();

		assertThat(canAuthenticate(adminServiceKey, adminToken)).isTrue();
		assertThat(getRepositories(adminServiceKey, adminToken)).hasSize(2);

		//Because bob is not a system admin
		assertThat(canAuthenticate(bobServiceKey, bobToken)).isFalse();
		assertThat(getRepositories(bobServiceKey, bobToken)).hasSize(0);

		//Because chuck norris is a system admin
		assertThat(canAuthenticate(chuckNorrisServiceKey, chuckNorrisToken)).isTrue();
		assertThat(getRepositories(chuckNorrisServiceKey, chuckNorrisToken)).hasSize(2);

		//No service key
		assertThat(canAuthenticate(null, chuckNorrisToken)).isFalse();
		assertThat(getRepositories(null, chuckNorrisToken)).hasSize(0);

		//No service token
		assertThat(canAuthenticate(chuckNorrisServiceKey, null)).isFalse();
		assertThat(getRepositories(chuckNorrisServiceKey, null)).hasSize(0);

		//unmatched servicekey/token
		assertThat(canAuthenticate(chuckNorrisServiceKey, bobToken)).isFalse();
		assertThat(getRepositories(chuckNorrisServiceKey, bobToken)).hasSize(0);

		//unmatched servicekey/token
		assertThat(canAuthenticate(bobServiceKey, chuckNorrisToken)).isFalse();
		assertThat(getRepositories(bobServiceKey, chuckNorrisToken)).hasSize(0);

	}

	private boolean canAuthenticate(String serviceKey, String token) {
		try {
			Session session = newCmisSessionBuilder().authenticatedBy(serviceKey, token).onCollection(zeCollection).build();
			session.getRootFolder().getProperty("cmis:path").getValue();
			return true;
		} catch (Exception e) {
			return false;
		}
	}

	private List<Repository> getRepositories(String serviceKey, String token) {
		try {

			return newCmisSessionBuilder().authenticatedBy(serviceKey, token).getRepositories();
		} catch (Exception e) {
			return new ArrayList<>();
		}
	}
}
