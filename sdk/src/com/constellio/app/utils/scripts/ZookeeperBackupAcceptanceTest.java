package com.constellio.app.utils.scripts;

import com.constellio.data.conf.PropertiesDataLayerConfiguration;
import com.constellio.data.utils.PropertyFileUtils;
import com.constellio.model.conf.FoldersLocator;
import com.constellio.sdk.tests.ConstellioTest;
import com.constellio.sdk.tests.annotations.CloudTest;
import org.apache.commons.lang3.StringUtils;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import java.io.File;
import java.io.IOException;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

@CloudTest
// Confirm @SlowTest
public class ZookeeperBackupAcceptanceTest extends ConstellioTest {
	private String zkHost;
	private PropertiesDataLayerConfiguration props;

	private ZookeeperBackup backup;

	private File backupDir;

	@Rule
	public TemporaryFolder folder = new TemporaryFolder();

	public ZookeeperBackupAcceptanceTest() {
	}

	@Before
	public void setUp() throws IOException {
		File configFile = new FoldersLocator().getConstellioProperties();
		Map<String, String> configs = PropertyFileUtils.loadKeyValues(configFile);
		zkHost = new PropertiesDataLayerConfiguration(configs, null, null, null).getRecordsDaoCloudSolrServerZKHost();

		assertThat(StringUtils.isNotBlank(zkHost)).isTrue();

		backup = new ZookeeperBackup();

		backupDir = folder.newFolder("zookeeperBackup");
	}

	@Test
	public void testExportOption() throws Exception {
		backup.exportOption(backupDir, zkHost);
		assertThat(backupDir.listFiles()).isNotEmpty();

		backup.importOption(backupDir, zkHost);
	}
}
