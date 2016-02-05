package com.constellio.app.api.cmis.accept;

import static org.assertj.core.api.Assertions.assertThat;

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
	String adminServiceKey, adminToken, bobToken, chuckNorrisToken;

	@Before
	public void setUp()
			throws Exception {

		prepareSystem(
				withZeCollection().withAllTest(users),
				withCollection("anotherCollection").withAllTestUsers());

		userServices = getModelLayerFactory().newUserServices();

		adminServiceKey = users.admin().getServiceKey();
		adminToken = userServices.generateToken(users.admin().getUsername());

		userServices.addUpdateUserCredential(users.bob().withServiceKey(bobServiceKey));
		bobToken = userServices.generateToken(users.bob().getUsername());

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

		//Because bob is not a system admin
		assertThat(canAuthenticate(bobServiceKey, bobToken)).isFalse();

		//Because chuck norris is a system admin
		assertThat(canAuthenticate(chuckNorrisServiceKey, chuckNorrisToken)).isTrue();

		//No service key
		assertThat(canAuthenticate(null, chuckNorrisToken)).isFalse();

		//No service token
		assertThat(canAuthenticate(chuckNorrisServiceKey, null)).isFalse();

		//unmatched servicekey/token
		assertThat(canAuthenticate(chuckNorrisServiceKey, bobToken)).isFalse();

		//unmatched servicekey/token
		assertThat(canAuthenticate(bobServiceKey, chuckNorrisToken)).isFalse();

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
}
