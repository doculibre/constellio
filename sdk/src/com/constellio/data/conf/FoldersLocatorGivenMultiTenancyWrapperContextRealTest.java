package com.constellio.data.conf;

import com.constellio.data.utils.TenantUtils;
import com.constellio.sdk.tests.ConstellioTest;
import org.assertj.core.api.Assertions;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

import java.io.File;
import java.io.IOException;

public class FoldersLocatorGivenMultiTenancyWrapperContextRealTest extends ConstellioTest {

	private FoldersLocator foldersLocator;

	@Before
	public void setUp() throws IOException {
		givenTwoTenants();

		foldersLocator = Mockito.spy(new FoldersLocator());

		Mockito.when(foldersLocator.getFoldersLocatorMode()).thenReturn(FoldersLocatorMode.WRAPPER);
	}

	@Test
	public void getKeystoreFile() {
		TenantUtils.setTenant("1");
		File file = foldersLocator.getKeystoreFile();
		Assertions.assertThat(file.getPath()).endsWith("conf/keystore.jks");

		TenantUtils.setTenant("2");
		File file2 = foldersLocator.getKeystoreFile();
		Assertions.assertThat(file2.getPath()).endsWith("conf/keystore.jks");
	}

	@Test
	public void getSmtpMailFolder() {
		TenantUtils.setTenant("1");
		File file = foldersLocator.getSmtpMailFolder();
		Assertions.assertThat(file.getPath()).endsWith("conf/tenant1/smtpMail");

		TenantUtils.setTenant("2");
		File file2 = foldersLocator.getSmtpMailFolder();
		Assertions.assertThat(file2.getPath()).endsWith("conf/tenant2/smtpMail");
	}

	@Test
	public void getReindexingFolder() {
		TenantUtils.setTenant("1");
		File file = foldersLocator.getReindexingFolder();
		Assertions.assertThat(file.getPath()).endsWith("work/tenant1/reindexing");

		TenantUtils.setTenant("2");
		File file2 = foldersLocator.getReindexingFolder();
		Assertions.assertThat(file2.getPath()).endsWith("work/tenant2/reindexing");
	}

	@Test
	public void getReindexingAggregatedValuesFolder() {
		TenantUtils.setTenant("1");
		File file = foldersLocator.getReindexingAggregatedValuesFolder();
		Assertions.assertThat(file.getPath()).endsWith("work/tenant1/reindexing/aggregatedValues");

		TenantUtils.setTenant("2");
		File file2 = foldersLocator.getReindexingAggregatedValuesFolder();
		Assertions.assertThat(file2.getPath()).endsWith("work/tenant2/reindexing/aggregatedValues");
	}

	@Test
	public void getReindexationLock() {
		TenantUtils.setTenant("1");
		File file = foldersLocator.getReindexationLock();
		Assertions.assertThat(file.getPath()).endsWith("work/tenant1/reindexing/reindexation.lock");

		TenantUtils.setTenant("2");
		File file2 = foldersLocator.getReindexationLock();
		Assertions.assertThat(file2.getPath()).endsWith("work/tenant2/reindexing/reindexation.lock");
	}

	@Test
	public void getConstellioProperties() {
		TenantUtils.setTenant("1");
		File file = foldersLocator.getConstellioProperties();
		Assertions.assertThat(file.getPath()).endsWith("conf/tenant1/constellio.properties");

		TenantUtils.setTenant("2");
		File file2 = foldersLocator.getConstellioProperties();
		Assertions.assertThat(file2.getPath()).endsWith("conf/tenant2/constellio.properties");
	}

	@Test
	public void getLogsFolder() {
		TenantUtils.setTenant("1");
		File file = foldersLocator.getLogsFolder();
		Assertions.assertThat(file.getPath()).endsWith("logs");

		TenantUtils.setTenant("2");
		File file2 = foldersLocator.getLogsFolder();
		Assertions.assertThat(file2.getPath()).endsWith("logs");
	}

	@Test
	public void getDefaultTempFolder() {
		TenantUtils.setTenant("1");
		File file = foldersLocator.getDefaultTempFolder();
		Assertions.assertThat(file.getPath()).endsWith("temp/tenant1");

		TenantUtils.setTenant("2");
		File file2 = foldersLocator.getDefaultTempFolder();
		Assertions.assertThat(file2.getPath()).endsWith("temp/tenant2");
	}

	@Test
	public void getLastAlertFile() {
		TenantUtils.setTenant("1");
		File file = foldersLocator.getLastAlertFile();
		Assertions.assertThat(file.getPath()).endsWith("temp/tenant1/lastAlert.pdf");

		TenantUtils.setTenant("2");
		File file2 = foldersLocator.getLastAlertFile();
		Assertions.assertThat(file2.getPath()).endsWith("temp/tenant2/lastAlert.pdf");
	}

	@Test
	public void getDefaultSettingsFolder() {
		TenantUtils.setTenant("1");
		File file = foldersLocator.getDefaultSettingsFolder();
		Assertions.assertThat(file.getPath()).endsWith("conf/tenant1/settings");

		TenantUtils.setTenant("2");
		File file2 = foldersLocator.getDefaultSettingsFolder();
		Assertions.assertThat(file2.getPath()).endsWith("conf/tenant2/settings");
	}

	@Test
	public void getPluginsRepository() {
		TenantUtils.setTenant("1");
		File file = foldersLocator.getPluginsRepository();
		Assertions.assertThat(file.getPath()).endsWith("constellio-plugins/tenant1");

		TenantUtils.setTenant("2");
		File file2 = foldersLocator.getPluginsRepository();
		Assertions.assertThat(file2.getPath()).endsWith("constellio-plugins/tenant2");
	}

	@Test
	public void getPluginsResourcesFolder() {
		TenantUtils.setTenant("1");
		File file = foldersLocator.getPluginsResourcesFolder();
		Assertions.assertThat(file.getPath()).endsWith("plugins-modules-resources/tenant1");

		TenantUtils.setTenant("2");
		File file2 = foldersLocator.getPluginsResourcesFolder();
		Assertions.assertThat(file2.getPath()).endsWith("plugins-modules-resources/tenant2");
	}

	@Test
	public void getReportsResourceFolder() {
		TenantUtils.setTenant("1");
		File file = foldersLocator.getReportsResourceFolder();
		Assertions.assertThat(file.getPath()).endsWith("resources/tenant1/reports");

		TenantUtils.setTenant("2");
		File file2 = foldersLocator.getReportsResourceFolder();
		Assertions.assertThat(file2.getPath()).endsWith("resources/tenant2/reports");
	}

	@Test
	public void getConstellioEncryptionFile() {
		TenantUtils.setTenant("1");
		File file = foldersLocator.getConstellioEncryptionFile();
		Assertions.assertThat(file.getPath()).endsWith("conf/tenant1/key.txt");

		TenantUtils.setTenant("2");
		File file2 = foldersLocator.getConstellioEncryptionFile();
		Assertions.assertThat(file2.getPath()).endsWith("conf/tenant2/key.txt");
	}

	@Test
	public void getPluginsJarsFolder() {
		TenantUtils.setTenant("1");
		File file = foldersLocator.getPluginsJarsFolder();
		Assertions.assertThat(file.getPath()).endsWith("WEB-INF/plugins");

		TenantUtils.setTenant("2");
		File file2 = foldersLocator.getPluginsJarsFolder();
		Assertions.assertThat(file2.getPath()).endsWith("WEB-INF/plugins");
	}

	@Test
	public void getPluginsToMoveOnStartupFile() {
		TenantUtils.setTenant("1");
		File file = foldersLocator.getPluginsToMoveOnStartupFile();
		Assertions.assertThat(file.getPath()).endsWith("WEB-INF/pluginsManagement/toMoveOnStartup");

		TenantUtils.setTenant("2");
		File file2 = foldersLocator.getPluginsToMoveOnStartupFile();
		Assertions.assertThat(file2.getPath()).endsWith("WEB-INF/pluginsManagement/toMoveOnStartup");
	}

	@Test
	public void getUploadLicenseFile() {
		TenantUtils.setTenant("1");
		File file = foldersLocator.getUploadLicenseFile();
		Assertions.assertThat(file.getPath()).endsWith("temp/tenant1/license.xml");

		TenantUtils.setTenant("2");
		File file2 = foldersLocator.getUploadLicenseFile();
		Assertions.assertThat(file2.getPath()).endsWith("temp/tenant2/license.xml");
	}

	@Test
	public void getLicenseFile() {
		TenantUtils.setTenant("1");
		File file = foldersLocator.getLicenseFile();
		Assertions.assertThat(file.getPath()).endsWith("conf/tenant1/license.xml");

		TenantUtils.setTenant("2");
		File file2 = foldersLocator.getLicenseFile();
		Assertions.assertThat(file2.getPath()).endsWith("conf/tenant2/license.xml");
	}

	@Test
	public void getWorkFolder() {
		TenantUtils.setTenant("1");
		File file = foldersLocator.getWorkFolder();
		Assertions.assertThat(file.getPath()).endsWith("work/tenant1");

		TenantUtils.setTenant("2");
		File file2 = foldersLocator.getWorkFolder();
		Assertions.assertThat(file2.getPath()).endsWith("work/tenant2");
	}

	@Test
	public void getLocalConfigsFile() {
		TenantUtils.setTenant("1");
		File file = foldersLocator.getLocalConfigsFile();
		Assertions.assertThat(file.getPath()).endsWith("conf/tenant1/local-configs.properties");

		TenantUtils.setTenant("2");
		File file2 = foldersLocator.getLocalConfigsFile();
		Assertions.assertThat(file2.getPath()).endsWith("conf/tenant2/local-configs.properties");
	}


}
