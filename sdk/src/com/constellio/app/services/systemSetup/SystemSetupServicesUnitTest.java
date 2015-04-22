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
package com.constellio.app.services.systemSetup;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.verifyZeroInteractions;
import static org.mockito.Mockito.when;

import java.io.File;
import java.util.Arrays;
import java.util.List;
import java.util.Properties;

import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;

import com.constellio.app.conf.AppLayerConfiguration;
import com.constellio.app.entities.modules.InstallableModule;
import com.constellio.app.services.collections.CollectionsManager;
import com.constellio.app.services.extensions.ConstellioModulesManagerImpl;
import com.constellio.app.services.extensions.ConstellioPluginManager;
import com.constellio.app.services.factories.AppLayerFactory;
import com.constellio.app.services.systemSetup.SystemSetupServiceRuntimeException.SystemSetupServiceRuntimeException_InvalidSetupFileProperty;
import com.constellio.data.dao.services.factories.DataLayerFactory;
import com.constellio.model.conf.ModelLayerConfiguration;
import com.constellio.model.entities.security.global.UserCredential;
import com.constellio.model.services.collections.CollectionsListManager;
import com.constellio.model.services.factories.ModelLayerFactory;
import com.constellio.model.services.security.authentification.AuthenticationService;
import com.constellio.model.services.users.UserServices;
import com.constellio.model.utils.InstanciationUtils;
import com.constellio.sdk.tests.ConstellioTest;

public class SystemSetupServicesUnitTest extends ConstellioTest {

	@Mock InstallableModule constellioModule1, constellioModule2, constellioModule3;
	@Mock AppLayerFactory appLayerFactory;
	@Mock ModelLayerFactory modelLayerFactory;
	@Mock ModelLayerConfiguration modelLayerConfiguration;
	@Mock DataLayerFactory dataLayerFactory;
	@Mock Properties properties;

	@Mock UserServices userServices;
	@Mock AuthenticationService authenticationService;
	@Mock CollectionsManager collectionsManager;
	@Mock CollectionsListManager collectionsListManager;
	@Mock SystemGlobalConfigsManager systemGlobalConfigsManager;
	@Mock ConstellioPluginManager constellioPluginManager;
	@Mock ConstellioModulesManagerImpl constellioModulesManager;
	@Mock InstanciationUtils instanciationUtils;
	@Mock AppLayerConfiguration appLayerConfiguration;

	SystemSetupService systemSetupService;

	@Before
	public void setUp()
			throws Exception {
		File propertiesFile = mock(File.class);
		when(appLayerConfiguration.getSetupProperties()).thenReturn(propertiesFile);

		when(appLayerFactory.getModelLayerFactory()).thenReturn(modelLayerFactory);
		when(modelLayerFactory.getDataLayerFactory()).thenReturn(dataLayerFactory);
		when(modelLayerFactory.getConfiguration()).thenReturn(modelLayerConfiguration);

		systemSetupService = spy(
				new SystemSetupService(appLayerFactory, appLayerConfiguration));
		doReturn(properties).when(systemSetupService).readProperties();
		when(modelLayerFactory.newUserServices()).thenReturn(userServices);
		when(modelLayerFactory.newAuthenticationService()).thenReturn(authenticationService);
		when(appLayerFactory.getCollectionsManager()).thenReturn(collectionsManager);
		when(modelLayerFactory.getCollectionsListManager()).thenReturn(collectionsListManager);
		when(appLayerFactory.getModulesManager()).thenReturn(constellioModulesManager);
		doReturn(instanciationUtils).when(systemSetupService).newInstanciationUtils();
		doNothing().when(systemSetupService).addAdminToTheNewCollection(anyString());
		when(modelLayerConfiguration.getMainDataLanguage()).thenReturn("zeLanguage");
	}

	@Test(expected = SystemSetupServiceRuntimeException_InvalidSetupFileProperty.class)
	public void givenNoAdminServiceKeyWhenSetupThenExceptionThrown() {
		when(properties.getProperty("admin.servicekey")).thenReturn(null);
		when(properties.getProperty("admin.password")).thenReturn("zePassword");

		systemSetupService.setup();
	}

	@Test(expected = SystemSetupServiceRuntimeException_InvalidSetupFileProperty.class)
	public void givenEmptyAdminServiceKeyWhenSetupThenExceptionThrown() {
		when(properties.getProperty("admin.servicekey")).thenReturn("");
		when(properties.getProperty("admin.password")).thenReturn("zePassword");

		systemSetupService.setup();
	}

	@Test(expected = SystemSetupServiceRuntimeException_InvalidSetupFileProperty.class)
	public void givenNoAdminPasswordKeyWhenSetupThenExceptionThrown() {
		when(properties.getProperty("admin.servicekey")).thenReturn("zeServiceKey");
		when(properties.getProperty("admin.password")).thenReturn(null);

		systemSetupService.setup();
	}

	@Test(expected = SystemSetupServiceRuntimeException_InvalidSetupFileProperty.class)
	public void givenEmptyAdminPasswordKeyWhenSetupThenExceptionThrown() {
		when(properties.getProperty("admin.servicekey")).thenReturn("zeServiceKey");
		when(properties.getProperty("admin.password")).thenReturn("");

		systemSetupService.setup();
	}

	@Test
	public void givenAllRequiredFieldsThenAddTheAdminUserWithSpecifiedPassword() {
		when(properties.getProperty("admin.servicekey")).thenReturn("zeServiceKey");
		when(properties.getProperty("admin.password")).thenReturn("zePassword");
		when(authenticationService.supportPasswordChange()).thenReturn(true);
		ArgumentCaptor<UserCredential> userCredentialArgumentCaptor = ArgumentCaptor.forClass(UserCredential.class);

		systemSetupService.setup();

		verify(userServices).addUpdateUserCredential(userCredentialArgumentCaptor.capture());
		assertThat(userCredentialArgumentCaptor.getValue().getUsername()).isEqualTo("admin");
		assertThat(userCredentialArgumentCaptor.getValue().getServiceKey()).isEqualTo("zeServiceKey");
		verify(authenticationService).changePassword("admin", "zePassword");

	}

	@Test
	public void givenAllRequiredFieldsAndLdapAuthenticationManagerThenAddTheAdminUserWithoutPassword() {
		when(properties.getProperty("admin.servicekey")).thenReturn("zeServiceKey");
		when(properties.getProperty("admin.password")).thenReturn("zePassword");
		when(authenticationService.supportPasswordChange()).thenReturn(false);
		ArgumentCaptor<UserCredential> userCredentialArgumentCaptor = ArgumentCaptor.forClass(UserCredential.class);

		systemSetupService.setup();

		verify(userServices).addUpdateUserCredential(userCredentialArgumentCaptor.capture());
		assertThat(userCredentialArgumentCaptor.getValue().getUsername()).isEqualTo("admin");
		assertThat(userCredentialArgumentCaptor.getValue().getServiceKey()).isEqualTo("zeServiceKey");
		verify(authenticationService, never()).changePassword("admin", "zePassword");

	}

	@Test
	public void givenNullCollectionsThenCreateNoCollections() {
		when(properties.getProperty("admin.servicekey")).thenReturn("zeServiceKey");
		when(properties.getProperty("admin.password")).thenReturn("zePassword");
		when(properties.getProperty("collections")).thenReturn(null);

		systemSetupService.setup();

		verifyZeroInteractions(collectionsManager);

	}

	@Test
	public void givenEmptyCollectionsThenCreateNoCollections() {
		when(properties.getProperty("admin.servicekey")).thenReturn("zeServiceKey");
		when(properties.getProperty("admin.password")).thenReturn("zePassword");
		when(properties.getProperty("collections")).thenReturn("");

		systemSetupService.setup();

		verifyZeroInteractions(collectionsManager);

	}

	@Test
	public void givenNonNullCollectionsThenCreateCollections() {
		when(properties.getProperty("admin.servicekey")).thenReturn("zeServiceKey");
		when(properties.getProperty("admin.password")).thenReturn("zePassword");
		when(properties.getProperty("collections")).thenReturn("collection1, collection2");

		systemSetupService.setup();

		List<String> expectedCollectionLanguages = Arrays.asList("zeLanguage");
		verify(collectionsManager).createCollectionInCurrentVersion("collection1", expectedCollectionLanguages);
		verify(collectionsManager).createCollectionInCurrentVersion("collection2", expectedCollectionLanguages);
		verifyNoMoreInteractions(collectionsManager);
		verify(systemSetupService).addAdminToTheNewCollection("collection1");
		verify(systemSetupService).addAdminToTheNewCollection("collection2");
	}

	@Test
	public void givenNoCollectionModulesThenNoModulesInstalled() {
		when(properties.getProperty("admin.servicekey")).thenReturn("zeServiceKey");
		when(properties.getProperty("admin.password")).thenReturn("zePassword");
		when(properties.getProperty("collections")).thenReturn("collection1, collection2");
		when(properties.getProperty("collection.collection1.modules")).thenReturn("");
		when(properties.getProperty("collection.collection2.modules")).thenReturn(null);

		systemSetupService.setup();

		verifyZeroInteractions(constellioModulesManager);
		verify(systemSetupService).addAdminToTheNewCollection("collection1");
		verify(systemSetupService).addAdminToTheNewCollection("collection2");
	}

	@Test
	public void givenCollectionWithModulesThenInstallAllRequiredModulesThenEnableModulesInCollections()
			throws Exception {
		when(properties.getProperty("admin.servicekey")).thenReturn("zeServiceKey");
		when(properties.getProperty("admin.password")).thenReturn("zePassword");
		when(properties.getProperty("collections")).thenReturn("collection1, collection2");
		when(properties.getProperty("collection.collection1.modules")).thenReturn("com.constellioModule1, com.constellioModule2");
		when(properties.getProperty("collection.collection2.modules")).thenReturn("com.constellioModule2, com.constellioModule3");
		when(instanciationUtils.instanciate("com.constellioModule1")).thenReturn(constellioModule1);
		when(instanciationUtils.instanciate("com.constellioModule2")).thenReturn(constellioModule2);
		when(instanciationUtils.instanciate("com.constellioModule3")).thenReturn(constellioModule3);
		when(constellioModulesManager.isInstalled(constellioModule2)).thenReturn(true);

		systemSetupService.setup();

		verify(constellioModulesManager).installModule(constellioModule1, collectionsListManager);
		verify(constellioModulesManager).installModule(constellioModule3, collectionsListManager);

		verify(constellioModulesManager).enableModule("collection1", constellioModule1);
		verify(constellioModulesManager).enableModule("collection1", constellioModule2);
		verify(constellioModulesManager).enableModule("collection2", constellioModule2);
		verify(constellioModulesManager).enableModule("collection2", constellioModule3);
		verify(systemSetupService).addAdminToTheNewCollection("collection1");
		verify(systemSetupService).addAdminToTheNewCollection("collection2");
	}
}
