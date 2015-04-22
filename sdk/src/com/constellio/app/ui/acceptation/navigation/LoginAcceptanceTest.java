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
package com.constellio.app.ui.acceptation.navigation;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;

import org.apache.commons.io.IOUtils;
import org.junit.Before;
import org.junit.Test;

import com.constellio.data.io.streamFactories.StreamFactory;
import com.constellio.model.services.users.UserPhotosServices;
import com.constellio.sdk.tests.ConstellioTest;
import com.constellio.sdk.tests.FakeSessionContext;
import com.constellio.sdk.tests.annotations.InDevelopmentTest;
import com.constellio.sdk.tests.annotations.UiTest;
import com.constellio.sdk.tests.selenium.adapters.constellio.ConstellioWebDriver;

@UiTest
@InDevelopmentTest
public class LoginAcceptanceTest extends ConstellioTest {

	ConstellioWebDriver driver;

	@Before
	public void setUp()
			throws Exception {
		givenCollection(zeCollection).withConstellioRMModule().withAllTestUsers().andUsersWithWriteAndDeleteAccess(dakota);

		driver = newWebDriver(FakeSessionContext.edouardInCollection(zeCollection));
		driver.logUserInCollection(dakota, zeCollection);

	}

	@Test
	public void testLoginsAndLogouts()
			throws Exception {

		pokeChuckNorris();

		waitUntilICloseTheBrowsers();
		assertThat(driver.getCurrentUserTitle()).isEqualTo("Dakota L'indien");
		driver.logout();
		driver.logUserInCollection(edouard, zeCollection);
		assertThat(driver.getCurrentUserTitle()).isEqualTo("Edouard Lechat");

		waitUntilICloseTheBrowsers();
	}

	public static final String SHOW_USER_PHOTO = "Maclasse-ShowUserPhoto";

	//@Test
	public void exempleGetPhoto()
			throws Exception {

		//Copie les photos des utilisateurs dans ce répertoire
		File folder = new File("/Users/francisbaril/Downloads/");

		exempleGetPhotoInFolder(aliceWonderland, folder);
		exempleGetPhotoInFolder(bobGratton, folder);
		exempleGetPhotoInFolder(charlesFrancoisXavier, folder);
		exempleGetPhotoInFolder(dakota, folder);
		exempleGetPhotoInFolder(edouard, folder);
		exempleGetPhotoInFolder(gandalf, folder);
		exempleGetPhotoInFolder(chuckNorris, folder);

	}

	private void exempleGetPhotoInFolder(String user, File folder)
			throws Exception {
		UserPhotosServices userPhotosServices = getModelLayerFactory().newUserPhotosServices();
		StreamFactory<InputStream> streamFactory = userPhotosServices.getPhotoInputStream(user);

		File file = new File(folder, "test_" + user + ".jpg");
		file.delete();

		InputStream inputStream = null;
		OutputStream outputStream = null;
		try {
			inputStream = streamFactory.create(SHOW_USER_PHOTO);
			outputStream = new FileOutputStream(file);
			IOUtils.copy(inputStream, outputStream);
		} finally {
			IOUtils.closeQuietly(inputStream);
			IOUtils.closeQuietly(outputStream);
		}
	}

}
