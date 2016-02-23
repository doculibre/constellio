package com.constellio.app.services.systemSetup;

import static com.constellio.model.entities.records.wrappers.Collection.SYSTEM_COLLECTION;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;

import com.constellio.app.conf.AppLayerConfiguration;
import com.constellio.app.entities.modules.InstallableModule;
import com.constellio.app.modules.rm.ConstellioRMModule;
import com.constellio.app.modules.rm.constants.RMPermissionsTo;
import com.constellio.app.modules.tasks.TaskModule;
import com.constellio.app.services.extensions.plugins.ConstellioPluginManager;
import com.constellio.model.entities.modules.Module;
import com.constellio.model.entities.records.Record;
import com.constellio.model.entities.records.wrappers.User;
import com.constellio.model.entities.security.global.UserCredential;
import com.constellio.model.services.security.authentification.AuthenticationService;
import com.constellio.sdk.tests.ConstellioTest;
import com.constellio.sdk.tests.annotations.SlowTest;

@SlowTest
public class SystemSetupServicesAcceptTest extends ConstellioTest {

	SystemSetupService systemSetupService;

	File setupFile;

	ConstellioPluginManager pluginManager;

	@Mock AppLayerConfiguration appLayerConfiguration;

	@Before
	public void setUp()
			throws Exception {
		withSpiedServices(ConstellioPluginManager.class);
		setupFile = newTempFileWithContent("constellio.setup.properties", "");

		when(appLayerConfiguration.getSetupProperties()).thenReturn(setupFile);
		systemSetupService = new SystemSetupService(getAppLayerFactory(),
				appLayerConfiguration);

		List<InstallableModule> modules = new ArrayList<>();
		modules.add(new ConstellioRMModule());
		modules.add(new TaskModule());

		pluginManager = getAppLayerFactory().getPluginManager();
		when(pluginManager.getRegistredModulesAndActivePlugins()).thenReturn(modules);
	}

	@Test
	public void whenSetupSystemWithCollectionThenInitializeCorrectly()
			throws Exception {

		String content = "#These configs are used when Constellio is started the first time\n"
				+ "\n"
				+ "admin.servicekey=zeAdminKey\n"
				+ "admin.password=zePassword\n"
				+ "collections=myCollection1, myCollection2\n"
				+ "collection.myCollection1.modules=com.constellio.app.modules.rm.ConstellioRMModule";

		getIOLayerFactory().newFileService().replaceFileContent(setupFile, content);

		systemSetupService.setup();

		thenHasAdminUserWithCorrectServiceKeyAndPassword();
		thenMyCollectionIsCreatedWithModulesAndAdminUser();

	}

	@Test
	public void whenSetupSystemWithoutCollectionThenInitializeCorrectly()
			throws Exception {

		String content = "#These configs are used when Constellio is started the first time\n"
				+ "\n"
				+ "admin.servicekey=zeAdminKey\n"
				+ "admin.password=zePassword";

		getIOLayerFactory().newFileService().replaceFileContent(setupFile, content);

		systemSetupService.setup();

		thenHasAdminUserWithCorrectServiceKeyAndPassword();
		thenHasNoCollectionExceptSystem();

	}

	private void thenHasNoCollectionExceptSystem() {
		assertThat(getModelLayerFactory().getCollectionsListManager().getCollections()).containsOnly(SYSTEM_COLLECTION);
	}

	private void thenMyCollectionIsCreatedWithModulesAndAdminUser() {

		Record aRecord = mock(Record.class);
		assertThat(getModelLayerFactory().getCollectionsListManager().getCollections())
				.containsOnly("myCollection1", "myCollection2", SYSTEM_COLLECTION);

		List<? extends Module> modules = getAppLayerFactory().getModulesManager().getEnabledModules("myCollection1");
		assertThat(modules).hasSize(2);
		assertThat(modules).extractingResultOf("getClass").containsOnly(ConstellioRMModule.class, TaskModule.class);

		User admin = getModelLayerFactory().newUserServices().getUserInCollection("admin", "myCollection1");
		assertThat(admin.isSystemAdmin()).isTrue();
		assertThat(admin.hasCollectionReadAccess()).isTrue();
		assertThat(admin.hasCollectionWriteAccess()).isTrue();
		assertThat(admin.hasCollectionDeleteAccess()).isTrue();
		assertThat(getModelLayerFactory().newUserServices().has("admin")
				.allGlobalPermissionsInAnyCollection(RMPermissionsTo.PERMISSIONS.getAll())).isTrue();
		assertThat(admin.hasAll(RMPermissionsTo.PERMISSIONS.getAll()).globally()).isTrue();
		assertThat(admin.hasAll(RMPermissionsTo.PERMISSIONS.getAll()).on(aRecord)).isTrue();
		assertThat(admin.hasAll(RMPermissionsTo.PERMISSIONS.getAll()).onAll(aRecord, aRecord)).isTrue();
		assertThat(admin.hasAll(RMPermissionsTo.PERMISSIONS.getAll()).onAny(aRecord, aRecord)).isTrue();
	}

	private void thenHasAdminUserWithCorrectServiceKeyAndPassword() {
		UserCredential admin = getModelLayerFactory().newUserServices().getUser("admin");
		assertThat(admin).isNotNull();
		assertThat(admin.getServiceKey()).isEqualTo("zeAdminKey");

		AuthenticationService authenticationService = getModelLayerFactory().newAuthenticationService();
		if (authenticationService.supportPasswordChange()) {
			//Otherwise, the password is not set in setup
			assertThat(authenticationService.authenticate("admin", "zePassword")).isTrue();
		}

	}
}
