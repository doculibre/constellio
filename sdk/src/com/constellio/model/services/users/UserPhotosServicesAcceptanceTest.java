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
package com.constellio.model.services.users;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;

import org.junit.Before;
import org.junit.Test;

import com.constellio.model.entities.security.global.UserCredential;
import com.constellio.model.entities.security.global.UserCredentialStatus;
import com.constellio.model.services.users.UserPhotosServicesRuntimeException.UserPhotosServicesRuntimeException_UserHasNoPhoto;
import com.constellio.sdk.tests.ConstellioTest;

public class UserPhotosServicesAcceptanceTest extends ConstellioTest {

	UserPhotosServices userPhotosServices;
	UserCredentialsManager userCredentialsManager;

	@Before
	public void setUp()
			throws Exception {
		userCredentialsManager = getModelLayerFactory().getUserCredentialsManager();
		userPhotosServices = getModelLayerFactory().newUserPhotosServices();

		UserCredential user = new UserCredential("zeUser", "ze", "user", "ze.user@gmail.com", new ArrayList<String>(),
				new ArrayList<String>(), UserCredentialStatus.ACTIVE, "domain");

		userCredentialsManager.addUpdate(user);
	}

	@Test(expected = UserPhotosServicesRuntimeException_UserHasNoPhoto.class)
	public void givenUserWithoutPhotoWhenGetPhotoThenException()
			throws Exception {

		userPhotosServices.getPhotoInputStream("zeUser");

	}

	@Test
	public void givenUserWithoutPhotoWhenChangePhotoThenPhotoAvailable()
			throws Exception {

		userPhotosServices.changePhoto(firstPhotoInputStream(), "zeUser");
		InputStream theUserPhotoInputStream = userPhotosServices.getPhotoInputStream("zeUser").create(SDK_STREAM);

		assertThat(theUserPhotoInputStream).hasContentEqualTo(firstPhotoInputStream());

	}

	@Test
	public void givenUserWithAPhotoWhenChangePhotoThenNewPhotoAvailable()
			throws Exception {

		userPhotosServices.changePhoto(firstPhotoInputStream(), "zeUser");

		userPhotosServices.changePhoto(secondPhotoInputStream(), "zeUser");
		InputStream theUserPhotoInputStream = userPhotosServices.getPhotoInputStream("zeUser").create(SDK_STREAM);

		assertThat(theUserPhotoInputStream).hasContentEqualTo(secondPhotoInputStream());

	}

	private InputStream firstPhotoInputStream()
			throws IOException {
		return getTestResourceInputStreamFactory("firstPhoto.jpg").create(SDK_STREAM);
	}

	private InputStream secondPhotoInputStream()
			throws IOException {
		return getTestResourceInputStreamFactory("secondPhoto.jpg").create(SDK_STREAM);
	}
}
