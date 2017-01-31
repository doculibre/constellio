package com.constellio.app.services.migrations.scripts;

import com.constellio.app.entities.modules.MetadataSchemasAlterationHelper;
import com.constellio.app.entities.modules.MigrationResourcesProvider;
import com.constellio.app.entities.modules.MigrationScript;
import com.constellio.app.services.factories.AppLayerFactory;
import com.constellio.data.conf.ConfigManagerType;
import com.constellio.data.conf.DataLayerConfiguration;
import com.constellio.data.dao.dto.records.OptimisticLockingResolution;
import com.constellio.data.utils.BatchBuilderIterator;
import com.constellio.data.utils.KeySetMap;
import com.constellio.model.conf.ModelLayerConfiguration;
import com.constellio.model.entities.records.ActionExecutorInBatch;
import com.constellio.model.entities.records.Record;
import com.constellio.model.entities.records.RecordUpdateOptions;
import com.constellio.model.entities.records.Transaction;
import com.constellio.model.entities.records.wrappers.Facet;
import com.constellio.model.entities.records.wrappers.Group;
import com.constellio.model.entities.records.wrappers.SolrAuthorizationDetails;
import com.constellio.model.entities.records.wrappers.User;
import com.constellio.model.entities.records.wrappers.structure.FacetType;
import com.constellio.model.entities.schemas.Schemas;
import com.constellio.model.entities.schemas.entries.DataEntry;
import com.constellio.model.entities.security.XMLAuthorizationDetails;
import com.constellio.model.entities.security.global.AuthorizationDetails;
import com.constellio.model.services.records.RecordServicesException;
import com.constellio.model.services.records.SchemasRecordsServices;
import com.constellio.model.services.schemas.builders.MetadataSchemaBuilder;
import com.constellio.model.services.schemas.builders.MetadataSchemaTypeBuilder;
import com.constellio.model.services.schemas.builders.MetadataSchemaTypesBuilder;
import com.constellio.model.services.search.SearchServices;
import com.constellio.model.services.security.AuthorizationDetailsManager;
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
import java.util.*;

import static com.constellio.model.entities.records.wrappers.SolrAuthorizationDetails.*;
import static com.constellio.model.entities.schemas.MetadataValueType.*;
import static com.constellio.model.entities.schemas.Schemas.AUTHORIZATIONS;
import static com.constellio.model.services.schemas.builders.CommonMetadataBuilder.TOKENS;
import static com.constellio.model.services.search.query.logical.LogicalSearchQuery.query;
import static com.constellio.model.services.search.query.logical.LogicalSearchQueryOperators.*;
import static java.util.Arrays.asList;

public class CoreMigrationTo_7_1 implements MigrationScript {

	private static final Logger LOGGER = LoggerFactory.getLogger(CoreMigrationTo_7_1.class);

	@Override
	public String getVersion() {
		return "7.1";
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
					client.create().creatingParentsIfNeeded().forPath("/constellio/conf/" + encryptionFile.getName(), content);
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
