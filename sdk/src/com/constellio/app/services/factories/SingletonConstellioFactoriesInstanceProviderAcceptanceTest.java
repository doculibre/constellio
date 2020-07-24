package com.constellio.app.services.factories;

import com.constellio.data.conf.ConfigManagerType;
import com.constellio.data.conf.ContentDaoType;
import com.constellio.data.conf.DataLayerConfiguration;
import com.constellio.data.conf.PropertiesDataLayerConfiguration;
import com.constellio.data.conf.PropertiesDataLayerConfiguration.InMemoryDataLayerConfiguration;
import com.constellio.data.conf.SolrServerType;
import com.constellio.data.dao.managers.StatefulService;
import com.constellio.sdk.tests.ConstellioTest;
import org.apache.commons.io.FileUtils;
import org.apache.solr.client.solrj.SolrClient;
import org.junit.After;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import java.io.File;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

import static org.assertj.core.api.Assertions.assertThat;

public class SingletonConstellioFactoriesInstanceProviderAcceptanceTest extends ConstellioTest {

	public @Rule TemporaryFolder contentFolder = new TemporaryFolder();
	public @Rule TemporaryFolder settingsFolder = new TemporaryFolder();
	public @Rule TemporaryFolder tempFolder = new TemporaryFolder();

	@Test
	public void givenACalledToSingletonIsDoneDuringInitializeThenCurrentInstanceReturnedEvenIfNotFullyInitialized()
			throws Exception {

		assumeNotSolrCloud();
		ConstellioFactories.instanceProvider = new SingletonConstellioFactoriesInstanceProvider();
		final File constellioProperties = tempFolder.newFile("constellio.properties");
		FileUtils.write(constellioProperties, "");

		final ConstellioFactoriesDecorator configurationDecorator = new ConstellioFactoriesDecorator() {

			@Override
			public DataLayerConfiguration decorateDataLayerConfiguration(
					DataLayerConfiguration dataLayerConfiguration) {
				InMemoryDataLayerConfiguration configuration = new InMemoryDataLayerConfiguration(
						(PropertiesDataLayerConfiguration) dataLayerConfiguration);

				configuration.setContentDaoType(ContentDaoType.FILESYSTEM);
				configuration.setContentDaoFileSystemFolder(contentFolder.getRoot());

				configuration.setSettingsConfigType(ConfigManagerType.FILESYSTEM);
				configuration.setSettingsFileSystemBaseFolder(settingsFolder.getRoot());

				configuration.setTempFolder(tempFolder.getRoot());

				configuration.setRecordsDaoSolrServerType(SolrServerType.HTTP);
				configuration.setRecordsDaoHttpSolrServerUrl(sdkProperties.get("dao.records.http.url"));
				return configuration;
			}
		};

		ConstellioFactoriesDecorator configurationAndServicesDecorator = new ConstellioFactoriesDecorator() {

			@Override
			public DataLayerConfiguration decorateDataLayerConfiguration(
					DataLayerConfiguration dataLayerConfiguration) {
				return configurationDecorator.decorateDataLayerConfiguration(dataLayerConfiguration);
			}

			@Override
			public AppLayerFactory decorateAppServicesFactory(AppLayerFactory appLayerFactory) {
				appLayerFactory.registerSystemWideManager("test", "test", new StatefulService() {
					@Override
					public void initialize() {
						//Calling the singleton while it is being constructed
						System.out.println("OK");
						System.out.println("OK");
						System.out.println("OK");
						ConstellioFactories.getInstance(() -> constellioProperties, configurationDecorator).getAppLayerFactory();
					}

					@Override
					public void close() {

					}
				});
				return appLayerFactory;
			}
		};

		ConstellioFactories constellioFactories = ConstellioFactories
				.getInstance(() -> constellioProperties, configurationAndServicesDecorator);

	}

	@Test
	public void whileFactoriesAreBeingBuiltGetInstanceCallsFromOtherThreadWillBlock()
			throws Exception {

		assumeNotSolrCloud();

		ConstellioFactories.instanceProvider = new SingletonConstellioFactoriesInstanceProvider();
		final File constellioProperties = tempFolder.newFile("constellio.properties");
		FileUtils.write(constellioProperties, "");

		final AtomicInteger numberOfTimeThatAnOtherThreadHasObtainedInstances = new AtomicInteger();
		final AtomicBoolean initializeFinished = new AtomicBoolean();

		Thread otherThread = new Thread() {

			@Override
			public void run() {
				while (!initializeFinished.get()) {
					try {
						Thread.sleep(1000);
					} catch (InterruptedException e) {
						throw new RuntimeException(e);
					}
					ConstellioFactories.getInstance().getAppLayerFactory();
					numberOfTimeThatAnOtherThreadHasObtainedInstances.incrementAndGet();
				}
			}
		};
		otherThread.start();

		final ConstellioFactoriesDecorator configurationAndServicesDecorator = new ConstellioFactoriesDecorator() {

			@Override
			public DataLayerConfiguration decorateDataLayerConfiguration(
					DataLayerConfiguration dataLayerConfiguration) {
				InMemoryDataLayerConfiguration configuration = new InMemoryDataLayerConfiguration(
						(PropertiesDataLayerConfiguration) dataLayerConfiguration);

				configuration.setContentDaoType(ContentDaoType.FILESYSTEM);
				configuration.setContentDaoFileSystemFolder(contentFolder.getRoot());

				configuration.setSettingsConfigType(ConfigManagerType.FILESYSTEM);
				configuration.setSettingsFileSystemBaseFolder(settingsFolder.getRoot());

				configuration.setTempFolder(tempFolder.getRoot());

				configuration.setRecordsDaoSolrServerType(SolrServerType.HTTP);
				configuration.setRecordsDaoHttpSolrServerUrl(sdkProperties.get("dao.records.http.url"));
				return configuration;
			}

			@Override
			public AppLayerFactory decorateAppServicesFactory(AppLayerFactory appLayerFactory) {
				appLayerFactory.registerSystemWideManager("test", "test", new StatefulService() {
					@Override
					public void initialize() {
						//Waiting 10 seconds

						try {
							Thread.sleep(7000);
						} catch (InterruptedException e) {
							throw new RuntimeException(e);
						}
						initializeFinished.set(true);
					}

					@Override
					public void close() {

					}
				});
				return appLayerFactory;
			}
		};

		ConstellioFactories constellioFactories = ConstellioFactories
				.getInstance(() -> constellioProperties, configurationAndServicesDecorator);

		otherThread.join(30_000);
		assertThat(numberOfTimeThatAnOtherThreadHasObtainedInstances.get()).isBetween(1, 2);
	}

	@After
	public void tearDown()
			throws Exception {

		if (ConstellioFactories.isInitialized()) {
			SolrClient solrClient = ConstellioFactories.getInstance().getDataLayerFactory().newRecordDao().getBigVaultServer()
					.getNestedSolrServer();

			solrClient.deleteByQuery("*:*");
			solrClient.commit(true, true, true);
		}

		try {
			ConstellioFactories.clear();
		} catch (Exception e) {

		}

	}
}
