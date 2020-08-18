package com.constellio.app.modules;

import com.constellio.app.entities.modules.locators.DefaultModuleResourcesLocator;
import com.constellio.app.entities.modules.locators.ModuleResourcesLocator;
import com.constellio.app.entities.modules.locators.ProjectModeModuleResourcesLocator;
import com.constellio.data.io.services.facades.IOServices;
import com.constellio.data.io.services.zip.ZipService;
import com.constellio.data.conf.FoldersLocator;
import com.constellio.sdk.tests.ConstellioTest;
import org.junit.Test;

import java.io.File;
import java.util.Locale;

import static java.util.Locale.ENGLISH;
import static java.util.Locale.FRENCH;
import static org.assertj.core.api.Assertions.assertThat;

public class ModuleResourcesLocatorAcceptanceTest extends ConstellioTest {

	File fakeWebapp;

	@Test
	public void whenInProjectModeThenLocateEverything()
			throws Exception {

		ProjectModeModuleResourcesLocator locator = new ProjectModeModuleResourcesLocator();

		assertThat(locator.getModuleResourcesFolder("rm")).isEqualTo(constellioFile("modules-resources/rm"));
		assertThat(locator.getModuleResourcesFolder(null)).isEqualTo(constellioFile("resources"));

		assertThat(locator.getModuleResource("rm", "newFile/NewFile.doc"))
				.isEqualTo(constellioFile("modules-resources/rm/newFile/NewFile.doc"));
		assertThat(locator.getModuleResource(null, "UserImportServices-user.xml"))
				.isEqualTo(constellioFile("resources/UserImportServices-user.xml"));

		assertThat(locator.getModuleI18nFolder("rm")).isNull();
		assertThat(locator.getModuleI18nFolder("es")).isNull();
		assertThat(locator.getModuleI18nFolder("tasks")).isNull();
		assertThat(locator.getModuleI18nFolder("robots")).isNull();
		assertThat(locator.getModuleI18nFolder(null)).isEqualTo(constellioFile("resources_i18n/"));

		assertThat(locator.getModuleI18nBundle("rm")).isNull();

		assertThat(locator.getModuleI18nBundle(null)).isNotNull();

		assertThat(locator.getModuleMigrationResourcesFolder("rm", "6.1"))
				.isEqualTo(constellioFile("resources_i18n/migrations/rm/6_1"));

		assertThat(locator.getModuleMigrationResourcesFolder("es", "5.1.6"))
				.isEqualTo(constellioFile("resources_i18n/migrations/es/5_1_6"));

		assertThat(locator.getModuleMigrationResourcesFolder(null, "5.1.6"))
				.isEqualTo(constellioFile("resources_i18n/migrations/core/5_1_6"));

		assertThat(locator.getModuleMigrationResource("rm", "5.0.7", "alertAvailableTemplate.html"))
				.isEqualTo(constellioFile("resources_i18n/migrations/rm/5_0_7/alertAvailableTemplate.html"));

		assertThat(locator.getModuleMigrationI18nBundle("rm", "5.0.7").getBundle(FRENCH).getString("init.facet.category")).
				isEqualTo("Catégories");

		assertThat(
				locator.getModuleMigrationI18nBundle("es", "5.1.3").getBundle(Locale.ENGLISH).getString("init.connector")).
				isEqualTo("Connector");
	}

	@Test
	public void whenInSimulatedProductionModeThenLocateEverything()
			throws Exception {

		ModuleResourcesLocator locator = givenLocatorBasedOnFakeWebapp("fakeWebapp.zip");

		assertThat(locator.getModuleResourcesFolder("rm")).isEqualTo(webInf("modules-resources/rm"));
		//assertThat(locator.getModuleResourcesFolder(null)).isEqualTo(webInf("resources"));

		assertThat(locator.getModuleResource("rm", "newFile/NewFile.doc"))
				.isEqualTo(webInf("modules-resources/rm/newFile/NewFile.doc"));
		//		assertThat(locator.getModuleResource(null, "UserImportServices-user.xml"))
		//				.isEqualTo(constellioFile("resources/UserImportServices-user.xml"));

		assertThat(locator.getModuleI18nFolder("rm")).isNull();
		assertThat(locator.getModuleI18nFolder("es")).isNull();
		assertThat(locator.getModuleI18nFolder("tasks")).isNull();
		assertThat(locator.getModuleI18nFolder("robots")).isNull();
		assertThat(locator.getModuleI18nFolder(null)).isEqualTo(webInf("resources_i18n/"));

		assertThat(locator.getModuleI18nBundle("rm")).isNull();

		assertThat(locator.getModuleI18nBundle(null).getBundle(ENGLISH).getString("init.allTypes.allSchemas.principalpath"))
				.isEqualTo("Main path");

		assertThat(locator.getModuleMigrationResourcesFolder("rm", "6.1"))
				.isEqualTo(webInf("resources_i18n/migrations/rm/6_1"));

		assertThat(locator.getModuleMigrationResourcesFolder("es", "5.1.6"))
				.isEqualTo(webInf("resources_i18n/migrations/es/5_1_6"));

		assertThat(locator.getModuleMigrationResourcesFolder(null, "5.1.6"))
				.isEqualTo(webInf("resources_i18n/migrations/core/5_1_6"));

		assertThat(locator.getModuleMigrationResource("rm", "5.0.7", "alertAvailableTemplate.html"))
				.isEqualTo(webInf("resources_i18n/migrations/rm/5_0_7/alertAvailableTemplate.html"));

		assertThat(locator.getModuleMigrationI18nBundle("rm", "5.0.7").getBundle(FRENCH).getString("init.facet.category")).
				isEqualTo("Catégories");

		assertThat(
				locator.getModuleMigrationI18nBundle("es", "5.1.3").getBundle(Locale.ENGLISH).getString("init.connector")).
				isEqualTo("Connector");
	}

	protected ModuleResourcesLocator givenLocatorBasedOnFakeWebapp(String zipResource)
			throws Exception {
		fakeWebapp = newTempFolder();
		File fakeWebappZip = getTestResourceFile(zipResource);

		new ZipService(new IOServices(newTempFolder())).unzip(fakeWebappZip, fakeWebapp);

		File i18nFolder = new File(fakeWebapp, path("WEB-INF/resources_i18n"));
		File modulesResources = new File(fakeWebapp, path("WEB-INF/modules-resources"));
		File pluginsResourcesFolder = new File(fakeWebapp, path("WEB-INF/plugins-modules-resources"));

		return new DefaultModuleResourcesLocator(pluginsResourcesFolder, modulesResources, i18nFolder);
	}

	protected String path(String path) {
		return path.replace("/", File.separator);
	}

	protected File webInf(String path) {
		File file = new File(fakeWebapp, path("WEB-INF/" + path));
		assertThat(file).exists();
		return file;
	}

	protected File constellioFile(String path) {
		FoldersLocator foldersLocator = new FoldersLocator();
		File file = new File(foldersLocator.getConstellioProject(), path(path));
		assertThat(file).exists();
		return file;
	}

}
