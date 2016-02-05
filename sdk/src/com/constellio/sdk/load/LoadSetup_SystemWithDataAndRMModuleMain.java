package com.constellio.sdk.load;

import static java.util.Arrays.asList;
import static org.assertj.core.api.Assertions.assertThat;

import java.io.File;
import java.util.List;

import org.apache.solr.common.params.ModifiableSolrParams;

import com.constellio.app.entities.modules.InstallableModule;
import com.constellio.app.services.extensions.plugins.ConstellioPluginManager;
import com.constellio.app.services.factories.AppLayerFactory;
import com.constellio.app.start.ApplicationStarter;
import com.constellio.app.utils.ScriptsUtils;
import com.constellio.data.conf.DataLayerConfiguration;
import com.constellio.data.dao.dto.records.RecordsFlushing;
import com.constellio.data.dao.dto.records.TransactionDTO;
import com.constellio.data.dao.services.factories.DataLayerFactory;
import com.constellio.model.conf.FoldersLocator;
import com.constellio.model.services.extensions.ConstellioModulesManager;
import com.constellio.sdk.load.script.SystemWithDataAndRMModuleScript;
import com.constellio.sdk.load.script.preparators.AdministrativeUnitTaxonomyPreparator;
import com.constellio.sdk.load.script.preparators.CategoriesTaxonomyPreparator;
import com.constellio.sdk.load.script.preparators.DefaultUsersPreparator;

public class LoadSetup_SystemWithDataAndRMModuleMain {

	static List<String> COLLECTIONS = asList("zeCollection", "anotherCollection");
	static int NUMBER_OF_USERS = 10;
	static int NUMBER_OF_GROUPS = 10;

	public static void main(String[] argv)
			throws Exception {

		SystemWithDataAndRMModuleScript script = new SystemWithDataAndRMModuleScript();
		script.setBigFilesFolder(new File("/Volumes/Raid1/wiki-extract/bigfiles"));
		//script.setNumberOfRootFolders(1);
		script.setNumberOfRootFolders(500);
		script.setSubFoldersPerFolder(50);
		script.setSubSubFoldersPerFolder(40);
		script.setNumberOfDocuments(10_000_000);
		script.setCollections(COLLECTIONS);
		script.setUserPreparator(new DefaultUsersPreparator(COLLECTIONS, NUMBER_OF_USERS, NUMBER_OF_GROUPS));
		script.setAdministrativeUnitsTaxonomy(new AdministrativeUnitTaxonomyPreparator());
		script.setCategoriesTaxonomy(new CategoriesTaxonomyPreparator());
		startWith(script, 7070);
		System.out.println("FINISHED!!!!");

		AppLayerFactory factory = ScriptsUtils.startLayerFactoriesWithoutBackgroundThreads();

		//factory.getExtensions().getSystemWideExtensions().pagesComponentsExtensions = new TestPagesComponentsExtensions(factory);
	}

	private static void startWith(SystemWithDataAndRMModuleScript initScript, int port) throws Exception {
		File configFile = new FoldersLocator().getConstellioProperties();

		ModifiableSolrParams params = new ModifiableSolrParams();
		params.set("q", "*:*");

		AppLayerFactory appLayerFactory= ScriptsUtils.startLayerFactoriesWithoutBackgroundThreads();
		DataLayerFactory dataLayerFactory = appLayerFactory.getModelLayerFactory().getDataLayerFactory();
		dataLayerFactory.newRecordDao().execute(new TransactionDTO(RecordsFlushing.NOW()).withDeletedByQueries(params));

		DataLayerConfiguration dataLayerConfiguration = appLayerFactory.getModelLayerFactory().getDataLayerFactory().getDataLayerConfiguration();
		boolean initialized = false;//dataLayerConfiguration.getSettingsFileSystemBaseFolder().exists();

		ConstellioPluginManager pluginManager = appLayerFactory.getPluginManager();
		ConstellioModulesManager modulesManager = appLayerFactory.getModulesManager();
		for (InstallableModule module : initScript.getModules()) {
			if (!modulesManager.isInstalled(module)) {
				modulesManager.installValidModuleAndGetInvalidOnes(module,
						appLayerFactory.getModelLayerFactory().getCollectionsListManager());
			}
		}

		if (!initialized) {
			initScript.setup(appLayerFactory, appLayerFactory.getModelLayerFactory());
		}

		ApplicationStarter.startApplication(false, getWebContentDir(), port);
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

}
