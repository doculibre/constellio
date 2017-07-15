package com.constellio.app.services.migrations.scripts;

import java.io.File;
import java.io.IOException;

import org.apache.commons.io.FileUtils;
import org.apache.curator.RetryPolicy;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.CuratorFrameworkFactory;
import org.apache.curator.retry.ExponentialBackoffRetry;
import org.apache.curator.utils.CloseableUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.constellio.app.entities.modules.MigrationResourcesProvider;
import com.constellio.app.entities.modules.MigrationScript;
import com.constellio.app.services.factories.AppLayerFactory;
import com.constellio.data.conf.ConfigManagerType;
import com.constellio.model.conf.ModelLayerConfiguration;
import com.constellio.model.entities.records.wrappers.Collection;

public class CoreMigrationTo_7_3 implements MigrationScript {

	private static final Logger LOGGER = LoggerFactory.getLogger(CoreMigrationTo_7_3.class);

	@Override
	public String getVersion() {
		return "7.3";
	}

	@Override
	public void migrate(String collection, MigrationResourcesProvider migrationResourcesProvider, AppLayerFactory appLayerFactory)
			throws Exception {
		createKeyFileInZookeeperIfNeeded(collection, appLayerFactory.getModelLayerFactory().getConfiguration());
	}

	public static void createKeyFileInZookeeperIfNeeded(String collection, ModelLayerConfiguration modelLayerConfiguration)
			throws IOException {
		File encryptionFile = modelLayerConfiguration.getConstellioEncryptionFile();
		if (modelLayerConfiguration.getDataLayerConfiguration().getSettingsConfigType().equals(ConfigManagerType.ZOOKEEPER)
				&& Collection.SYSTEM_COLLECTION.equals(collection)) {
			CuratorFramework client = null;
			try {
				RetryPolicy retryPolicy = new ExponentialBackoffRetry(1000, 10);
				client = CuratorFrameworkFactory
						.newClient(modelLayerConfiguration.getDataLayerConfiguration().getSettingsZookeeperAddress(),
								retryPolicy);
				client.start();
				String path = "/constellio/conf/" + encryptionFile.getName();
				if (client.checkExists().forPath(path) == null) {
					if (encryptionFile.exists()) {
						byte[] content = FileUtils.readFileToByteArray(encryptionFile);
						client.create().creatingParentsIfNeeded().forPath(path, content);
					} else {
						String fileKeyPart =
								"constellio_" + modelLayerConfiguration.getDataLayerConfiguration().createRandomUniqueKey() + "_ext";
						client.create().creatingParentsIfNeeded().forPath(path, fileKeyPart.getBytes());
					}
				}
			} catch (Exception e) {
				throw new RuntimeException(e);
			} finally {
				CloseableUtils.closeQuietly(client);
			}
		}
	}
}