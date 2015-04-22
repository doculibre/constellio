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
package demo;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

import java.io.File;
import java.io.IOException;
import java.util.Map;

import org.apache.commons.io.FileUtils;
import org.apache.solr.client.solrj.SolrClient;
import org.apache.solr.client.solrj.impl.HttpSolrClient;

import com.constellio.app.entities.modules.InstallableModule;
import com.constellio.app.services.extensions.ConstellioPluginManager;
import com.constellio.app.services.factories.ConstellioFactories;
import com.constellio.app.start.ApplicationStarter;
import com.constellio.data.conf.PropertiesDataLayerConfiguration;
import com.constellio.data.utils.PropertyFileUtils;
import com.constellio.model.conf.FoldersLocator;
import com.constellio.model.services.extensions.ConstellioModulesManager;
import com.constellio.sdk.tests.TestConstellioFactoriesDecorator;

public class DemoUtils {

	public static void clearData()
			throws Exception {

		File appData = getAppData();

		if (appData.exists()) {
			FileUtils.forceDelete(appData);
		}

		File configFile = new FoldersLocator().getConstellioProperties();
		Map<String, String> configs = PropertyFileUtils.loadKeyValues(configFile);
		String solrUrl = new PropertiesDataLayerConfiguration(configs, null, null).getRecordsDaoHttpSolrServerUrl();

		clearDataIn(solrUrl + "records");
		clearDataIn(solrUrl + "events");
		clearDataIn(solrUrl + "notifications");

	}

	private static void clearDataIn(String solrUrl)
			throws Exception {
		SolrClient solrServer = new HttpSolrClient(solrUrl);
		solrServer.deleteByQuery("*:*");
		solrServer.commit();
	}

	public static void startDemoOn(int port, DemoInitScript initScript)
			throws Exception {
		File configFile = new FoldersLocator().getConstellioProperties();

		boolean initialized = new File(getAppData(), "settings").exists();

		ConstellioFactories factories = ConstellioFactories.getInstance(configFile, getFactoriesDecorator(configFile));

		ConstellioPluginManager pluginManager = factories.getAppLayerFactory().getPluginManager();
		when(pluginManager.getPlugins(InstallableModule.class)).thenReturn(initScript.getModules());
		ConstellioModulesManager modulesManager = factories.getAppLayerFactory().getModulesManager();
		for (InstallableModule module : initScript.getModules()) {
			if (!modulesManager.isInstalled(module)) {
				modulesManager.installModule(module, factories.getModelLayerFactory().getCollectionsListManager());
			}
		}

		if (!initialized) {
			initScript.setup(factories.getAppLayerFactory(), factories.getModelLayerFactory());
		}

		ApplicationStarter.startApplication(false, getWebContentDir(), port);
	}

	private static TestConstellioFactoriesDecorator getFactoriesDecorator(File configFile)
			throws IOException {

		File tempFolder = new File(getAppData(), "temp");

		StringBuilder setupPropertiesContent = new StringBuilder();
		setupPropertiesContent.append("admin.servicekey=adminkey\n");
		setupPropertiesContent.append("admin.password=password\n");
		File setupProperties = new File(tempFolder, "setup.properties");
		FileUtils.write(setupProperties, setupPropertiesContent);

		File appData = getAppData();
		TestConstellioFactoriesDecorator decorator = new TestConstellioFactoriesDecorator();
		decorator.setSetupProperties(setupProperties);
		decorator.setImportationFolder(new File(appData, "importation"));
		decorator.setConfigManagerFolder(new File(appData, "settings"));
		decorator.setAppTempFolder(tempFolder);
		decorator.setContentFolder(new File(appData, "vault"));
		return decorator;
	}

	private static File getAppData() {
		return new File(getSolrHome(), "appData");
	}

	private static File getWebContentDir() {
		File webContent = new FoldersLocator().getAppProjectWebContent();

		assertThat(webContent).exists().isDirectory();

		File webInf = new File(webContent, "WEB-INF");
		assertThat(webInf).exists().isDirectory();
		assertThat(new File(webInf, "web.xml")).exists();
		assertThat(new File(webInf, "sun-jaxws.xml")).exists();

		File cmis11 = new File(webInf, "cmis11");
		assertThat(cmis11).exists().isDirectory();
		assertThat(cmis11.listFiles()).isNotEmpty();
		return webContent;
	}

	public static void printConfiguration()
			throws IOException {
		FoldersLocator foldersLocator = new FoldersLocator();

		File constellioProperties = foldersLocator.getConstellioProperties();
		System.out.println("========================================================");
		System.out.println(FileUtils.readFileToString(constellioProperties));
		System.out.println("========================================================");

	}

	public static File getSolrHome() {
		File configFile = new FoldersLocator().getConstellioProperties();
		Map<String, String> configs = PropertyFileUtils.loadKeyValues(configFile);
		String solrHomePath = configs.get("solr.home");

		if (solrHomePath != null) {
			File solrHome = new File(solrHomePath);

			if (solrHome.exists() && new File(solrHome, "records").exists()) {
				return solrHome;
			}

			throw new RuntimeException("Property 'solr.home' is invalid : " + solrHomePath);
		} else {
			throw new RuntimeException("Property 'solr.home' is required");
		}
	}
}
