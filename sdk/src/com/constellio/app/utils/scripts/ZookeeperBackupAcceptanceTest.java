package com.constellio.app.utils.scripts;

import static org.junit.Assume.assumeTrue;

import java.io.File;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.junit.Before;
import org.junit.Test;

import com.constellio.app.utils.scripts.ZookeeperBackup;
import com.constellio.data.conf.PropertiesDataLayerConfiguration;
import com.constellio.data.utils.PropertyFileUtils;
import com.constellio.sdk.tests.ConstellioTest;
import com.constellio.sdk.tests.SDKFoldersLocator;

public class ZookeeperBackupAcceptanceTest extends ConstellioTest {
	private String zkHost;
	private PropertiesDataLayerConfiguration props;

	private ZookeeperBackup backup;

	public ZookeeperBackupAcceptanceTest() {
	}

	@Before
	public void setUp() {
		props = loadProps();

		zkHost = props.getRecordsDaoCloudSolrServerZKHost();
		assumeTrue("zkHost seems not to be declared", StringUtils.isNotBlank(zkHost));

		backup = new ZookeeperBackup();
	}

	private PropertiesDataLayerConfiguration loadProps() {
		File configFile = new SDKFoldersLocator().getSDKProperties();
		Map<String, String> configs = PropertyFileUtils.loadKeyValues(configFile);

		return new PropertiesDataLayerConfiguration(configs, null, null, null);
	}

	@Test
	public void testExportOption() throws Exception {
		//backup.exportOption(new File("D:/testZookeeper"), zkHost);
	}

	@Test
	public void testImportOption() throws Exception {
		//backup.exportOption(new File("D:/testZookeeper"), zkHost);
	}
}
