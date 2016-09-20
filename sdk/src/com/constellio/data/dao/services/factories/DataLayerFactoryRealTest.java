package com.constellio.data.dao.services.factories;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.File;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;

import com.constellio.data.conf.DataLayerConfiguration;
import com.constellio.data.conf.PropertiesDataLayerConfiguration.InMemoryDataLayerConfiguration;
import com.constellio.data.dao.managers.config.ConfigManager;
import com.constellio.data.dao.services.bigVault.BigVaultRecordDao;
import com.constellio.data.dao.services.idGenerator.UniqueIdGenerator;
import com.constellio.data.dao.services.solr.SolrDataStoreTypesFactory;
import com.constellio.data.dao.services.solr.SolrServers;
import com.constellio.data.dao.services.transactionLog.XMLSecondTransactionLogManager;
import com.constellio.data.io.IOServicesFactory;
import com.constellio.sdk.tests.ConstellioTest;
import com.constellio.sdk.tests.DataLayerConfigurationAlteration;

public class DataLayerFactoryRealTest extends ConstellioTest {

	@Mock SolrServers solrServers;

	@Mock IOServicesFactory ioServicesFactory;

	@Mock DataLayerConfiguration dataLayerConfiguration;

	private DataLayerFactory factory;

	@Before
	public void setUp()
			throws Exception {

	}

	@Test
	public void whenGettingSettingsManagerThenAlwaysSameInstance()
			throws Exception {
		factory = getDataLayerFactory();
		ConfigManager settingsManager1 = factory.getConfigManager();
		ConfigManager settingsManager2 = factory.getConfigManager();

		assertThat(settingsManager1).isNotNull().isSameAs(settingsManager2);
	}

	@Test
	public void whenGettingTypesFactoryThenReturnSolrTypesFactory()
			throws Exception {
		factory = getDataLayerFactory();
		assertThat(factory.newTypesFactory()).isInstanceOf(SolrDataStoreTypesFactory.class);
	}

	@Test
	public void whenGetUniqueIdGeneratorThenAlwaysSameInstance()
			throws Exception {
		factory = getDataLayerFactory();
		UniqueIdGenerator uniqueIdGenerator1 = factory.getUniqueIdGenerator();
		UniqueIdGenerator uniqueIdGenerator2 = factory.getUniqueIdGenerator();

		assertThat(uniqueIdGenerator1).isNotNull().isSameAs(uniqueIdGenerator2);
	}

	@Test
	public void givenSecondTransactionLogDisabledThenDisabledInEachCollection() {
		configure(new DataLayerConfigurationAlteration() {
			@Override
			public void alter(InMemoryDataLayerConfiguration configuration) {
				configuration.setSecondTransactionLogEnabled(false);
			}
		});

		factory = getDataLayerFactory();

		assertThat(((BigVaultRecordDao) factory.newRecordDao()).getSecondTransactionLogManager()).isNull();
		assertThat(((BigVaultRecordDao) factory.newEventsDao()).getSecondTransactionLogManager()).isNull();
		assertThat(((BigVaultRecordDao) factory.newNotificationsDao()).getSecondTransactionLogManager()).isNull();
	}

	@Test
	public void givenSecondTransactionLogEnabledThenOnlyEnabledForRecordSolrCollection() {
		final File secondTransactionLogBaseFolder = newTempFolder();
		configure(new DataLayerConfigurationAlteration() {
			@Override
			public void alter(InMemoryDataLayerConfiguration configuration) {
				configuration.setSecondTransactionLogEnabled(true);
				configuration.setSecondTransactionLogBaseFolder(secondTransactionLogBaseFolder);
			}
		});

		factory = getDataLayerFactory();

		XMLSecondTransactionLogManager transactionLog = (XMLSecondTransactionLogManager) ((BigVaultRecordDao) factory
				.newRecordDao()).getSecondTransactionLogManager();
		assertThat(transactionLog.getFolder()).isEqualTo(secondTransactionLogBaseFolder);
		assertThat(((BigVaultRecordDao) factory.newEventsDao()).getSecondTransactionLogManager()).isNull();
		assertThat(((BigVaultRecordDao) factory.newNotificationsDao()).getSecondTransactionLogManager()).isNull();
	}

}
