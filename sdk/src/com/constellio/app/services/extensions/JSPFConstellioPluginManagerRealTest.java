package com.constellio.app.services.extensions;

import com.constellio.app.api.pluginManagerTestResources.pluginImplementation.APluginImplementation;
import com.constellio.app.services.extensions.plugins.ConstellioPluginConfigurationManager;
import com.constellio.app.services.extensions.plugins.JSPFConstellioPluginManager;
import com.constellio.app.services.factories.AppLayerFactory;
import com.constellio.data.io.services.facades.IOServices;
import com.constellio.model.services.collections.CollectionsListManager;
import com.constellio.sdk.tests.ConstellioTestWithGlobalContext;
import com.constellio.sdk.tests.annotations.SlowTest;
import org.apache.commons.io.FileUtils;
import org.junit.After;
import org.junit.Before;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runners.MethodSorters;
import org.mockito.Mock;

import java.io.File;
import java.util.Arrays;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.when;

// Confirm @SlowTest
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class JSPFConstellioPluginManagerRealTest extends ConstellioTestWithGlobalContext {

	static File pluginsDirectory, pluginsManagementFile;
	@Mock AppLayerFactory appLayerFactory;
	@Mock ConstellioPluginConfigurationManager pluginConfigManger;
	JSPFConstellioPluginManager pluginManager;
	@Mock CollectionsListManager collectionsListManager;

	@Before
	public void setUp() {
		getAppLayerFactory();
		IOServices ioServices = new IOServices(newTempFolder());
		pluginManager = spy(new JSPFConstellioPluginManager(pluginsDirectory,
				getAppLayerFactory().getAppLayerConfiguration().getPluginsManagementOnStartupFile(), ioServices, pluginConfigManger));
		when(collectionsListManager.getCollections()).thenReturn(Arrays.asList("firstCollection", "secondCollection"));
		assertThat(APluginImplementation.isStarted()).isFalse();

	}

	@After
	public void tearDown()
			throws Exception {
		if (pluginManager != null) {
			pluginManager.close();
		}
	}

	@Test
	public void __prepareTests__()
			throws Exception {

		File jarzzFile = getTestResourceFile("constellio-custom.jarzz");
		File jarFile = new File(jarzzFile.getParentFile(), jarzzFile.getName().replace("jarzz", "jar"));
		FileUtils.copyFile(jarzzFile, jarFile);
		pluginsDirectory = jarFile.getParentFile();
	}

	@Test(expected = RuntimeException.class)
	public void givenUnstartedPluginManagerWhenGetPluginsThenRuntimeExceptionThrown()
			throws Exception {
		pluginManager.getRegistredModulesAndActivePlugins();
	}

	@Test
	public void givenStartedPluginManagerWhenStopingThenStopPlugins()
			throws Exception {
		pluginManager.detectPlugins();
		pluginManager.initialize();

		pluginManager.close();

		assertThat(APluginImplementation.isStarted()).isFalse();
	}

	@Test(expected = RuntimeException.class)
	public void givenStoppedPluginManagerWhenGetPluginsThenExceptionThrown()
			throws Exception {
		pluginManager.detectPlugins();
		pluginManager.initialize();
		pluginManager.close();

		pluginManager.getRegistredModulesAndActivePlugins();
	}
}
