package com.constellio.app.utils.scripts;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.File;
import java.io.IOException;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.junit.Before;
import org.junit.FixMethodOrder;
import org.junit.Rule;
import org.junit.Test;

import com.constellio.data.conf.PropertiesDataLayerConfiguration;
import com.constellio.data.utils.PropertyFileUtils;
import com.constellio.sdk.tests.ConstellioTest;
import com.constellio.sdk.tests.SDKFoldersLocator;
import org.junit.rules.TemporaryFolder;

public class ZookeeperBackupAcceptanceTest extends ConstellioTest {
	private String zkHost;
	private PropertiesDataLayerConfiguration props;

	private ZookeeperBackup backup;

	private File backupDir;

	@Rule
	public TemporaryFolder folder= new TemporaryFolder();

	public ZookeeperBackupAcceptanceTest() {
	}

	@Before
	public void setUp() throws IOException {
		props = loadProps();

		zkHost = props.getRecordsDaoCloudSolrServerZKHost();
		assertThat(StringUtils.isNotBlank(zkHost)).isTrue();

		backup = new ZookeeperBackup();

		backupDir = folder.newFolder("zookeeperBackup");
	}

	private PropertiesDataLayerConfiguration loadProps() {
		File configFile = new SDKFoldersLocator().getSDKProperties();
		Map<String, String> configs = PropertyFileUtils.loadKeyValues(configFile);

		return new PropertiesDataLayerConfiguration(configs, null, null, null);
	}

	@Test
	public void testExportOption() throws Exception {
		backup.exportOption(backupDir, zkHost);
		assertThat(backupDir.listFiles()).isNotEmpty();

		backup.importOption(backupDir, zkHost);
	}
}
