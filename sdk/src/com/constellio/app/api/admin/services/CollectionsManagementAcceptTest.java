package com.constellio.app.api.admin.services;

import com.constellio.app.client.entities.CollectionResource;
import com.constellio.app.client.services.AdminServicesSession;
import com.constellio.app.client.services.CollectionServicesClient;
import com.constellio.app.services.collections.CollectionsManager;
import com.constellio.model.services.security.authentification.AuthenticationService;
import com.constellio.model.services.users.UserServices;
import com.constellio.sdk.tests.ConstellioTest;
import com.constellio.sdk.tests.setups.Users;
import org.junit.Before;

import java.util.Arrays;

import static org.assertj.core.api.Assertions.assertThat;

public class CollectionsManagementAcceptTest extends ConstellioTest {

	String alicePassword = "p1";
	String bobPassword = "p2";

	String aliceServiceKey;
	String bobServiceKey;

	Users users = new Users();
	UserServices userServices;
	CollectionsManager collectionsManager;
	AuthenticationService authService;
	CollectionServicesClient collectionServicesClient;

	String collectionConstellio = "constellio";

	@Before
	public void setUp()
			throws Exception {

		prepareSystem(withZeCollection());

		userServices = getModelLayerFactory().newUserServices();
		authService = getModelLayerFactory().newAuthenticationService();
		collectionsManager = getAppLayerFactory().getCollectionsManager();

		users.setUp(userServices, zeCollection);

		userServices.givenSystemAdminPermissionsToUser(users.alice());
		userServices.givenSystemAdminPermissionsToUser(users.bob());

		aliceServiceKey = userServices.giveNewServiceKey(users.alice().getUsername());
		bobServiceKey = userServices.giveNewServiceKey(users.bob().getUsername());

		authService.changePassword(users.alice().getUsername(), alicePassword);
		authService.changePassword(users.bob().getUsername(), bobPassword);

		AdminServicesSession bobSession = newRestClient(bobServiceKey, users.bob().getUsername(), bobPassword);
		collectionServicesClient = bobSession.newCollectionServices();
	}

	//This test is runned by AllAdminServicesAcceptTest
	public void whenCreateCollectionThenItIsCreated()
			throws Exception {
		CollectionResource collectionResource = new CollectionResource();
		collectionResource.setCollection(collectionConstellio);
		collectionResource.setLanguages(Arrays.asList("fr", "en"));
		collectionServicesClient.createCollection(collectionResource);

		assertThat(collectionsManager.getCollection(collectionConstellio)).isNotNull();
		assertThat(collectionsManager.getCollectionLanguages(collectionConstellio)).isEqualTo(Arrays.asList("fr", "en"));
	}
}
