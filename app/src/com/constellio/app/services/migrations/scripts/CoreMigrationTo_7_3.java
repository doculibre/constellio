package com.constellio.app.services.migrations.scripts;

import com.constellio.app.entities.modules.MigrationResourcesProvider;
import com.constellio.app.entities.modules.MigrationScript;
import com.constellio.app.services.factories.AppLayerFactory;
import com.constellio.data.conf.ConfigManagerType;
import com.constellio.model.conf.ModelLayerConfiguration;
import org.apache.commons.io.FileUtils;
import org.apache.curator.RetryPolicy;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.CuratorFrameworkFactory;
import org.apache.curator.retry.ExponentialBackoffRetry;
import org.apache.curator.utils.CloseableUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;

public class CoreMigrationTo_7_3 implements MigrationScript {

    private static final Logger LOGGER = LoggerFactory.getLogger(CoreMigrationTo_7_1.class);

    @Override
    public String getVersion() {
        return "7.3";
    }

    @Override
    public void migrate(String collection, MigrationResourcesProvider migrationResourcesProvider, AppLayerFactory appLayerFactory)
            throws Exception {
        createKeyFile(appLayerFactory.getModelLayerFactory().getConfiguration());
    }

    private static void createKeyFile(ModelLayerConfiguration modelLayerConfiguration)
            throws IOException {
        File encryptionFile = modelLayerConfiguration.getConstellioEncryptionFile();
        if (modelLayerConfiguration.getDataLayerConfiguration().getSettingsConfigType().equals(ConfigManagerType.ZOOKEEPER)) {
            CuratorFramework client = null;
            try {
                RetryPolicy retryPolicy = new ExponentialBackoffRetry(1000, 10);
                client = CuratorFrameworkFactory.newClient(modelLayerConfiguration.getDataLayerConfiguration().getSettingsZookeeperAddress(), retryPolicy);
                client.start();
                if (encryptionFile.exists()) {
                    byte[] content = FileUtils.readFileToByteArray(encryptionFile);
                    String path = "/constellio/conf/" + encryptionFile.getName();
                    if (client.checkExists().forPath(path) == null) {
                        client.create().creatingParentsIfNeeded().forPath(path, content);
                    }
                } else {
                    String fileKeyPart =
                            "constellio_" + modelLayerConfiguration.getDataLayerConfiguration().createRandomUniqueKey() + "_ext";
                    client.create().creatingParentsIfNeeded().forPath("/constellio/conf/" + encryptionFile.getName(), fileKeyPart.getBytes());
                }
            } catch (Exception e) {
                throw new RuntimeException(e);
            } finally {
                CloseableUtils.closeQuietly(client);
            }
        }
    }
}