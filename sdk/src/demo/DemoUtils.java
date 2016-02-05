package demo;

import static java.util.Arrays.asList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Map;

import org.apache.commons.io.FileUtils;
import org.apache.solr.client.solrj.SolrClient;
import org.apache.solr.client.solrj.impl.HttpSolrClient;

import com.constellio.app.entities.modules.InstallableModule;
import com.constellio.app.services.extensions.plugins.ConstellioPluginManager;
import com.constellio.app.services.factories.ConstellioFactories;
import com.constellio.app.start.ApplicationStarter;
import com.constellio.data.conf.DataLayerConfiguration;
import com.constellio.data.conf.PropertiesDataLayerConfiguration;
import com.constellio.data.io.services.facades.IOServices;
import com.constellio.data.io.services.zip.ZipService;
import com.constellio.data.utils.PropertyFileUtils;
import com.constellio.model.conf.FoldersLocator;
import com.constellio.model.services.extensions.ConstellioModulesManager;
import com.constellio.model.services.records.reindexing.ReindexationMode;
import com.constellio.model.services.records.reindexing.ReindexingServices;
import com.constellio.sdk.tests.DataLayerConfigurationAlteration;
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
		String solrUrl = new PropertiesDataLayerConfiguration(configs, null, null, null).getRecordsDaoHttpSolrServerUrl();

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

	public static void startDemoOn(int port, DemoInitScript initScript, String language)
			throws Exception {
		File configFile = new FoldersLocator().getConstellioProperties();

		boolean initialized = new File(getAppData(), "settings").exists();

		ConstellioFactories factories = ConstellioFactories.getInstance(configFile, getFactoriesDecorator(false, language));

		ConstellioPluginManager pluginManager = factories.getAppLayerFactory().getPluginManager();
		when(pluginManager.getActivePlugins()).thenReturn(initScript.getModules());
		ConstellioModulesManager modulesManager = factories.getAppLayerFactory().getModulesManager();
		for (InstallableModule module : initScript.getModules()) {
			if (!modulesManager.isInstalled(module)) {
				modulesManager.installValidModuleAndGetInvalidOnes(module,
						factories.getModelLayerFactory().getCollectionsListManager());
			}
		}

		if (!initialized) {
			initScript.setup(factories.getAppLayerFactory(), factories.getModelLayerFactory());
		}

		ApplicationStarter.startApplication(false, getWebContentDir(), port);
	}

	private static TestConstellioFactoriesDecorator getFactoriesDecorator(final boolean transactionLog, String language)
			throws IOException {

		File tempFolder = new File(getAppData(), "temp");

		StringBuilder setupPropertiesContent = new StringBuilder();
		setupPropertiesContent.append("admin.servicekey=adminkey\n");
		setupPropertiesContent.append("admin.password=password\n");
		File setupProperties = new File(tempFolder, "setup.properties");
		File pluginFolder = new File(tempFolder, "plugins");
		FileUtils.write(setupProperties, setupPropertiesContent);

		File appData = getAppData();
		TestConstellioFactoriesDecorator decorator = new TestConstellioFactoriesDecorator(true);
		decorator.setSetupProperties(setupProperties);
		decorator.setPluginsFolder(pluginFolder);
		decorator.setImportationFolder(new File(appData, "importation"));
		decorator.setConfigManagerFolder(new File(appData, "settings"));
		decorator.setAppTempFolder(tempFolder);
		decorator.setSystemLanguage(language);
		decorator.setContentFolder(new File(appData, "vault"));
		DataLayerConfigurationAlteration dataLayerConfigurationAlteration = new DataLayerConfigurationAlteration() {
			@Override
			public void alter(DataLayerConfiguration configuration) {
				if (transactionLog) {
					configuration.setSecondTransactionLogFolderEnabled(transactionLog);
				}
			}
		};
		decorator.setDataLayerConfigurationAlterations(asList(dataLayerConfigurationAlteration));
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

	public static void startDemoWithSaveState(int port, File saveState, String language) {
		File configFile = new FoldersLocator().getConstellioProperties();
		File settings = new File(getAppData(), "settings");

		boolean firstStart = !settings.exists();

		if (firstStart) {
			System.out.print("First start!");
			try {
				uncompressSaveState(saveState, language);
			} catch (Exception e) {
				throw new RuntimeException(e);
			}
		}
		ConstellioFactories factories;

		try {
			factories = ConstellioFactories.getInstance(configFile, getFactoriesDecorator(true, language));
		} catch (IOException e) {
			throw new RuntimeException(e);
		}

		if (firstStart) {
			ReindexingServices reindexingServices = factories.getModelLayerFactory().newReindexingServices();
			reindexingServices.reindexCollections(ReindexationMode.RECALCULATE_AND_REWRITE);
		}

		ApplicationStarter.startApplication(false, getWebContentDir(), port);
	}

	private static void uncompressSaveState(File saveState, String language)
			throws Exception {

		File appData = getAppData();
		File settings = new File(appData, "settings");
		File content = new File(appData, "vault");
		File tempFolder = new File(appData, "temp");

		File uncompressedSaveStateFolder;
		if (saveState.isFile()) {

			uncompressedSaveStateFolder = new File(tempFolder, "uncompressed-save-state");
			FileUtils.deleteDirectory(tempFolder);
			tempFolder.mkdirs();
			ZipService zipService = new ZipService(new IOServices(tempFolder));
			zipService.unzip(saveState, uncompressedSaveStateFolder);

		} else {
			uncompressedSaveStateFolder = saveState;
		}
		FileUtils.deleteQuietly(settings);
		FileUtils.deleteQuietly(content);
		FileUtils.moveDirectory(new File(uncompressedSaveStateFolder, "settings"), settings);
		FileUtils.moveDirectory(new File(uncompressedSaveStateFolder, "content"), content);

		if (saveState.isFile()) {
			FileUtils.deleteQuietly(tempFolder);
		}
	}
}
