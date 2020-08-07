package com.constellio.app.api.cmis.accept;

import com.constellio.model.entities.CorePermissions;
import com.constellio.model.entities.security.Role;
import com.constellio.model.services.users.UserServices;
import com.constellio.sdk.tests.ConstellioTest;
import com.constellio.sdk.tests.setups.Users;
import org.apache.chemistry.opencmis.client.api.Repository;
import org.apache.chemistry.opencmis.client.api.Session;
import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

import static java.util.Arrays.asList;
import static org.assertj.core.api.Assertions.assertThat;

public class CmisAuthenticationAcceptanceTest extends ConstellioTest {

	UserServices userServices;
	Users users = new Users();

	String bobServiceKey = "bobKey";
	String chuckNorrisServiceKey = "chuckKey";
	String adminServiceKey = "adminKey";
	String dakotaServiceKey = "dakotaKey";
	String robinServiceKey = "robinKey";
	String adminToken, bobToken, chuckNorrisToken, dakotaToken, robinToken;

	@Before
	public void setUp()
			throws Exception {

		prepareSystem(
				withZeCollection().withAllTest(users),
				withCollection("anotherCollection").withAllTestUsers());

		userServices = getModelLayerFactory().newUserServices();

		Role role = new Role(zeCollection, "zeRole", asList(CorePermissions.USE_EXTERNAL_APIS_FOR_COLLECTION));
		getModelLayerFactory().getRolesManager().addRole(role);

		Role anotherCollectionRole = new Role("anotherCollection", "zeRole", asList(CorePermissions.USE_EXTERNAL_APIS_FOR_COLLECTION));
		getModelLayerFactory().getRolesManager().addRole(anotherCollectionRole);

		userServices.execute(users.bobAddUpdateRequest().setServiceKey(bobServiceKey));
		bobToken = userServices.generateToken(users.bob().getUsername());

		userServices.execute(users.dakotaAddUpdateRequest().setServiceKey(dakotaServiceKey));
		dakotaToken = userServices.generateToken(users.dakotaLIndien().getUsername());
		getModelLayerFactory().newRecordServices().update(users.dakotaLIndienIn(zeCollection).setUserRoles(role.getCode()));

		userServices.execute(users.robinAddUpdateRequest().setServiceKey(robinServiceKey));
		robinToken = userServices.generateToken(users.robin().getUsername());
		getModelLayerFactory().newRecordServices().update(users.robinIn(zeCollection).setUserRoles(role.getCode()));
		getModelLayerFactory().newRecordServices().update(users.robinIn("anotherCollection").setUserRoles(role.getCode()));

		adminToken = userServices.generateToken(users.admin().getUsername());
		userServices.execute(users.adminAddUpdateRequest().setServiceKey(adminServiceKey).setSystemAdminEnabled());

		robinToken = userServices.generateToken(users.robin().getUsername());
		userServices.execute(users.robinAddUpdateRequest().setServiceKey(robinServiceKey).setSystemAdminEnabled());

		userServices
				.execute(users.chuckNorrisAddUpdateRequest().setServiceKey(chuckNorrisServiceKey).setSystemAdminEnabled()
						.setCollections(asList(zeCollection)));
		chuckNorrisToken = userServices.generateToken(users.chuckNorris().getUsername());

		userServices.execute(users.admin().getUsername(), (req) -> req.addToCollection(zeCollection));
		userServices.execute(users.admin().getUsername(), (req) -> req.addToCollection("anotherCollection"));
		userServices.execute(users.chuckNorris().getUsername(), (req) -> req.addToCollection(zeCollection));
		userServices.execute(users.robin().getUsername(), (req) -> req.addToCollection(zeCollection));

		userServices.execute(users.robin().getUsername(), (req) -> req.addToCollection(zeCollection));
		userServices.execute(users.robin().getUsername(), (req) -> req.addToCollection("anotherCollection"));
	}

	@Test
	public void whenAuthenticatingToCmisThenBasedOnServiceKeyAndTokensAndRestrictedToSystemAdminsAndUserWithPermission()
			throws Exception {

		//- whenSearchingWithAvalidServiceKeyFromAnotherUserThenException();
		//- whenSearchingWithInvalidTokenThenException();
		//- whenSearchingWithNoTokenThenException();
		//- whenSearchingWithNoServiceKeyThenException();

		assertThat(canAuthenticate(adminServiceKey, adminToken)).isTrue();
		assertThat(getRepositories(adminServiceKey, adminToken)).hasSize(2);

		//Bob is a typical user without the use cmis permission
		assertThat(canAuthenticate(bobServiceKey, bobToken)).isFalse();
		assertThat(getRepositories(bobServiceKey, bobToken)).hasSize(0);

		//Dakota l'Indien is a typical user with the use cmis permission
		assertThat(canAuthenticate(dakotaServiceKey, dakotaToken)).isTrue();
		assertThat(getRepositories(dakotaServiceKey, dakotaToken)).hasSize(1);

		//Robin is a typical user in two collections with the use cmis permission in one
		assertThat(canAuthenticate(robinServiceKey, robinToken)).isTrue();
		assertThat(getRepositories(robinServiceKey, robinToken)).hasSize(2);

		//Chuck Norris is an other system admin
		assertThat(canAuthenticate(chuckNorrisServiceKey, chuckNorrisToken)).isTrue();
		assertThat(getRepositories(chuckNorrisServiceKey, chuckNorrisToken)).hasSize(1);

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
