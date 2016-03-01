package com.constellio.sdk.tests;

import static org.mockito.Mockito.spy;

import java.io.File;
import java.util.Arrays;

import org.junit.After;
import org.junit.Before;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.constellio.data.dao.services.bigVault.solr.BigVaultServer;
import com.constellio.data.dao.services.factories.DataLayerFactory;
import com.constellio.data.dao.services.records.RecordDao;
import com.constellio.data.io.concurrent.filesystem.AtomicFileSystem;
import com.constellio.data.io.concurrent.filesystem.AtomicFileSystemUtils;
import com.constellio.data.io.concurrent.filesystem.AtomicLocalFileSystem;
import com.constellio.data.io.concurrent.filesystem.ChildAtomicFileSystem;
import com.constellio.model.conf.FoldersLocator;
import com.constellio.model.entities.Language;
import com.constellio.model.entities.records.Record;
import com.constellio.model.entities.records.Transaction;
import com.constellio.model.services.records.RecordServices;
import com.constellio.model.services.search.SearchServiceAcceptanceTestSchemas;
import com.constellio.model.services.search.SearchServices;
import com.constellio.model.services.search.query.logical.condition.LogicalSearchCondition;
import com.constellio.model.services.search.query.logical.valueCondition.ConditionTemplateFactory;
import com.constellio.sdk.tests.annotations.SlowTest;

@SlowTest
public class SolrSafeConstellioAcceptanceTest extends ConstellioTest {
	private static Logger LOGGER = LoggerFactory.getLogger(SolrSafeConstellioAcceptanceTest.class);
	protected SearchServiceAcceptanceTestSchemas schema = new SearchServiceAcceptanceTestSchemas(zeCollection);
	protected SearchServiceAcceptanceTestSchemas.ZeSchemaMetadatas zeSchema = schema.new ZeSchemaMetadatas();

	protected LogicalSearchCondition condition;
	protected SearchServices searchServices;
	protected RecordServices recordServices;
	protected RecordDao recordDao;

	protected Transaction transaction;
	protected ConditionTemplateFactory factory;

	private String getServerConfigurations(String coreName) {
		File configFld = new File(new FoldersLocator().getSolrHomeConfFolder(), "configsets");
		for (File configFile : configFld.listFiles()) {
			if (configFile.getName().startsWith(coreName))
				return new File(configFile, "conf").getAbsolutePath();
		}
		return null;
	}

	@Before
	public void setUp() throws Exception{
		syncSolrConfigurationFiles();

		givenCollection(zeCollection, Arrays.asList(Language.French.getCode(), Language.English.getCode()));
		recordServices = getModelLayerFactory().newRecordServices();
		recordDao = spy(getDataLayerFactory().newRecordDao());
		searchServices = new SearchServices(recordDao, getModelLayerFactory());

		transaction = new Transaction();
		factory = new ConditionTemplateFactory(getModelLayerFactory(), zeCollection);
	}
	
	@After
	public void cleanup(){
		syncSolrConfigurationFiles();
	}

	private void syncSolrConfigurationFiles() {
		DataLayerFactory dataLayerFactory = getDataLayerFactory();
		for (BigVaultServer server : dataLayerFactory.getSolrServers().getServers()) {
			AtomicFileSystem serverFileSystem = server.getSolrFileSystem().getDecoratedFileSystem();
			AtomicFileSystem defaultConfiguration = new ChildAtomicFileSystem(
					new AtomicLocalFileSystem(dataLayerFactory.getIOServicesFactory().newHashingService()),
					getServerConfigurations(server.getName()));

			LOGGER.info("Syncing the <{}> configurations...", server.getName());
			if (!AtomicFileSystemUtils.sync(defaultConfiguration, serverFileSystem)) {
				server.reload();
				LOGGER.info("Reloading the <{}> server", server.getName());
			} else 
				LOGGER.info("No reloading for the <{}> server", server.getName());

		}
	}

	protected Record newRecordOfZeSchema() {
		return recordServices.newRecordWithSchema(zeSchema.instance());
	}

}
