package com.constellio.sdk.tests;

import com.constellio.data.dao.managers.config.ConfigManagerException.OptimisticLockingConfiguration;
import com.constellio.data.dao.services.bigVault.solr.BigVaultServer;
import com.constellio.data.io.concurrent.data.DataWithVersion;
import com.constellio.data.io.concurrent.data.TextView;
import com.constellio.data.io.concurrent.filesystem.AtomicFileSystem;
import com.constellio.model.services.search.SynonymFeatureAcceptanceTest;
import com.constellio.sdk.tests.annotations.SlowTest;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static org.assertj.core.api.Assertions.assertThat;

// Confirm @SlowTest
public class SolrServerFactoryAcceptanceTest extends SynonymFeatureAcceptanceTest {
	private static Logger LOGGER = LoggerFactory.getLogger(SolrServerFactoryAcceptanceTest.class);

	@Test
	public void whenSettingUpSolrCollectionsThenTheirConfigurationAreStoredInThePathStartsWithTheCollectionName() {
		for (BigVaultServer server : getConstellioFactories().getDataLayerFactory().getSolrServers().getServers()) {
			AtomicFileSystem configFileSystem = server.getSolrServerFactory().getConfigFileSystem();
			assertThat(configFileSystem.exists("/" + server.getName()) ||    //SolrCloud configuration
					   configFileSystem.exists("/" + server.getName() + "_configs")    //HttpSolr configuration
			).isTrue();
		}
	}

	@Test
	public void whenCreatingASolrServerThenTheWriteAccessIsProvided()
			throws OptimisticLockingConfiguration {
		String testFilePath = "/writeAccessFile.txt";
		String fileContent = "Some content";

		BigVaultServer aServer = getConstellioFactories().getDataLayerFactory().getSolrServers().getServers().iterator().next();
		AtomicFileSystem solrFileSystem = aServer.getSolrFileSystem();
		if (solrFileSystem.exists(testFilePath)) {
			solrFileSystem.delete(testFilePath, null);
		}

		assertThat(solrFileSystem.exists(testFilePath)).isFalse();
		solrFileSystem.writeData(testFilePath, new DataWithVersion(fileContent.getBytes(), null));
		DataWithVersion readData = solrFileSystem.readData(testFilePath);

		assertThat(readData.getView(new TextView()).getData()).isEqualTo(fileContent);

	}

	@Test
	public void whenCreateASolrServerThenItCanBeReloaded()
			throws Exception {
		//In this test we do a very basic modification, reload the server, and check if the modification has affected on the search.
		//Is there any better way to test this ?
		givenSynonymFeatureEnabledWhenIndexingDocumentsAndSearchForAWordThenAllDocumentsContainTheWordAndItsSynonymAreReturned();
	}

	@Test
	public void whenChangingSolrConfiguraitonInTestThenOtherTestsAreNotAffectedFirst()
			throws OptimisticLockingConfiguration {
		doAModification();
	}

	@Test
	public void whenChangingSolrConfiguraitonInTestThenOtherTestsAreNotAffectedSecond()
			throws OptimisticLockingConfiguration {
		doAModification();
	}

	public void doAModification() {
		LOGGER.info("Do a modification in the solr configuration.");
		String testFilePath = "/cleanUpFile.txt";
		String flag = "This content should not be in this file.";

		//for (BigVaultServer server : getConstellioFactories().getDataLayerFactory().getSolrServers().getServers()) {
		BigVaultServer server = getConstellioFactories().getDataLayerFactory().getRecordsVaultServer();
		AtomicFileSystem solrFileSystem = server.getSolrFileSystem();
		assertThat(solrFileSystem.exists(testFilePath)).isFalse();
		solrFileSystem.writeData(testFilePath, new DataWithVersion(flag.getBytes(), null));
		//	}
	}

}
