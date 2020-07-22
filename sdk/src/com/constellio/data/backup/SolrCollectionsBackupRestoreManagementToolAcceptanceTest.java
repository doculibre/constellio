package com.constellio.data.backup;

import com.constellio.data.conf.PropertiesDataLayerConfiguration;
import com.constellio.data.utils.PropertyFileUtils;
import com.constellio.model.conf.FoldersLocator;
import com.constellio.sdk.tests.ConstellioTest;
import com.constellio.sdk.tests.SDKFoldersLocator;
import com.constellio.sdk.tests.annotations.CloudTest;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.Predicate;
import org.apache.commons.lang3.StringUtils;
import org.apache.solr.client.solrj.SolrServerException;
import org.apache.solr.client.solrj.response.CollectionAdminResponse;
import org.apache.solr.common.util.NamedList;
import org.apache.solr.core.snapshots.CollectionSnapshotMetaData;
import org.apache.solr.core.snapshots.SolrSnapshotManager;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assume.assumeTrue;

@CloudTest
// Confirm @SlowTest
public class SolrCollectionsBackupRestoreManagementToolAcceptanceTest extends ConstellioTest {

	private static final int OK = 0;
	private final String COLLECTION_CLOUD = "recordsZookeeperConfigManagerAcceptanceTest";

	@Rule public TemporaryFolder tempFolder = new TemporaryFolder();

	private PropertiesDataLayerConfiguration props;
	private SolrCollectionsBackupRestoreManagementTool tool;

	@Before
	public void setUp()
			throws Exception {
		props = loadProps();

		File configFile = new FoldersLocator().getConstellioProperties();
		Map<String, String> configs = PropertyFileUtils.loadKeyValues(configFile);
		String zkHost = new PropertiesDataLayerConfiguration(configs, null, null, null).getRecordsDaoCloudSolrServerZKHost();

		assumeTrue("zkHost seems not to be declared", StringUtils.isNotBlank(zkHost));

		tool = new SolrCollectionsBackupRestoreManagementTool(zkHost);
	}

	private boolean isSolrInstalledOnLocalhost() {
		String solrUrl = props.getRecordsDaoHttpSolrServerUrl();

		return StringUtils.contains(solrUrl, "localhost") || StringUtils.contains(solrUrl, "127.0.0.1");
	}

	private PropertiesDataLayerConfiguration loadProps() {
		File configFile = new SDKFoldersLocator().getSDKProperties();
		Map<String, String> configs = PropertyFileUtils.loadKeyValues(configFile);

		return new PropertiesDataLayerConfiguration(configs, null, null, null);
	}

	private String getSnapshotFolder()
			throws IOException {
		if (isSolrInstalledOnLocalhost()) {
			return tempFolder.newFolder(String.valueOf(System.currentTimeMillis())).getAbsolutePath();
		} else {
			return "/tmp/";
		}
	}

	@Test
	public void testCreateSnapshot()
			throws SolrServerException, IOException {
		String snapshotName = COLLECTION_CLOUD + System.currentTimeMillis();

		CollectionAdminResponse resp = tool.createSnapshot(COLLECTION_CLOUD, snapshotName);
		assertThat(resp.getStatus()).isEqualTo(OK);

		resp = tool.deleteSnapshot(COLLECTION_CLOUD, snapshotName);
		assertThat(resp.getStatus()).isEqualTo(OK);
	}

	@Test
	public void testRestoreSnapshot()
			throws SolrServerException, IOException {
		String folder = getSnapshotFolder();
		String snapshotName = COLLECTION_CLOUD + System.currentTimeMillis();

		CollectionAdminResponse resp = tool.createSnapshot(COLLECTION_CLOUD, snapshotName);
		assertThat(resp.getStatus()).isEqualTo(OK);

		resp = tool.exportSnapshot(COLLECTION_CLOUD, snapshotName, folder);
		assertThat(resp.getStatus()).isEqualTo(OK);

		verifyLocalSnapshotDirectory(folder, snapshotName);

		tool.restoreSnapshot(snapshotName, snapshotName, folder);
		assertThat(resp.getStatus()).isEqualTo(OK);

		resp = tool.deleteCollection(snapshotName);
		assertThat(resp.getStatus()).isEqualTo(OK);

		resp = tool.deleteSnapshot(COLLECTION_CLOUD, snapshotName);
		assertThat(resp.getStatus()).isEqualTo(OK);
	}

	@Test
	@SuppressWarnings({"rawtypes", "unchecked"})
	public void testListSnapshots()
			throws SolrServerException, IOException {
		final String snapshotName = COLLECTION_CLOUD + System.currentTimeMillis();

		CollectionAdminResponse resp = tool.createSnapshot(COLLECTION_CLOUD, snapshotName);
		assertThat(resp.getStatus()).isEqualTo(OK);

		resp = tool.listSnapshots(COLLECTION_CLOUD);
		assertThat(resp.getStatus()).isEqualTo(OK);

		NamedList namedList = (NamedList) resp.getResponse().get(SolrSnapshotManager.SNAPSHOTS_INFO);
		assertThat(namedList).isNotNull();
		assertThat(namedList.size()).isGreaterThan(OK);

		Collection<CollectionSnapshotMetaData> result = new ArrayList<>();
		for (int i = 0; i < namedList.size(); i++) {
			result.add(new CollectionSnapshotMetaData((NamedList<Object>) namedList.getVal(i)));
		}

		assertThat(CollectionUtils.exists(result, new Predicate() {
			@Override
			public boolean evaluate(Object pArg0) {
				CollectionSnapshotMetaData c = (CollectionSnapshotMetaData) pArg0;
				return snapshotName.equals(c.getName());
			}
		})).isTrue();

		resp = tool.deleteSnapshot(COLLECTION_CLOUD, snapshotName);
		assertThat(resp.getStatus()).isEqualTo(OK);
	}

	@Test
	public void testExportSnapshot()
			throws SolrServerException, IOException {
		String folder = getSnapshotFolder();
		String snapshotName = COLLECTION_CLOUD + System.currentTimeMillis();

		CollectionAdminResponse resp = tool.createSnapshot(COLLECTION_CLOUD, snapshotName);
		assertThat(resp.getStatus()).isEqualTo(OK);

		resp = tool.exportSnapshot(COLLECTION_CLOUD, snapshotName, folder);
		assertThat(resp.getStatus()).isEqualTo(OK);

		verifyLocalSnapshotDirectory(folder, snapshotName);

		resp = tool.deleteSnapshot(COLLECTION_CLOUD, snapshotName);
		assertThat(resp.getStatus()).isEqualTo(OK);
	}

	private void verifyLocalSnapshotDirectory(String folder, String snapshotName) {
		if (isSolrInstalledOnLocalhost()) {
			File f = new File(folder, snapshotName);

			assertThat(f.isDirectory()).isTrue();
			assertThat(f.listFiles()).isNotEmpty();
		}
	}
}
