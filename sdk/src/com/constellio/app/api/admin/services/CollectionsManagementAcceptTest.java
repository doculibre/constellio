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

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Arrays;

import org.junit.Before;

import com.constellio.app.client.entities.CollectionResource;
import com.constellio.app.client.services.AdminServicesSession;
import com.constellio.app.client.services.CollectionServicesClient;
import com.constellio.app.services.collections.CollectionsManager;
import com.constellio.model.services.security.authentification.AuthenticationService;
import com.constellio.model.services.users.UserServices;
import com.constellio.sdk.tests.ConstellioTest;
import com.constellio.sdk.tests.setups.Users;

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

		givenCollection("zeCollection");

		userServices = getModelLayerFactory().newUserServices();
		authService = getModelLayerFactory().newAuthenticationService();
		collectionsManager = getAppLayerFactory().getCollectionsManager();

		users.setUp(userServices);

		userServices.givenSystemAdminPermissionsToUser(users.alice());
		userServices.givenSystemAdminPermissionsToUser(users.bob());

		aliceServiceKey = userServices.giveNewServiceToken(users.alice());
		bobServiceKey = userServices.giveNewServiceToken(users.bob());

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
