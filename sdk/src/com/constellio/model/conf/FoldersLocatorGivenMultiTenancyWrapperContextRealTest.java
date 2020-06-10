package com.constellio.model.conf;

import com.constellio.model.utils.TenantUtils;
import com.constellio.sdk.tests.ConstellioTest;
import org.junit.Before;
import org.junit.Test;

import java.io.File;
import java.io.IOException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.when;

public class FoldersLocatorGivenMultiTenancyWrapperContextRealTest extends ConstellioTest {

	private FoldersLocator foldersLocator;

	@Before
	public void setUp() throws IOException {
		givenTwoTenants();

		foldersLocator = spy(new FoldersLocator());

		when(foldersLocator.getFoldersLocatorMode()).thenReturn(FoldersLocatorMode.WRAPPER);
	}

	@Test
	public void getKeystoreFile() {
		TenantUtils.setTenant("1");
		File file = foldersLocator.getKeystoreFile();
		assertThat(file.getPath()).endsWith("conf/keystore.jks");

		TenantUtils.setTenant("2");
		File file2 = foldersLocator.getKeystoreFile();
		assertThat(file2.getPath()).endsWith("conf/keystore.jks");
	}

	@Test
	public void getSmtpMailFolder() {
		TenantUtils.setTenant("1");
		File file = foldersLocator.getSmtpMailFolder();
		assertThat(file.getPath()).endsWith("conf/tenant1/smtpMail");

		TenantUtils.setTenant("2");
		File file2 = foldersLocator.getSmtpMailFolder();
		assertThat(file2.getPath()).endsWith("conf/tenant2/smtpMail");
	}

	@Test
	public void getReindexingFolder() {
		TenantUtils.setTenant("1");
		File file = foldersLocator.getReindexingFolder();
		assertThat(file.getPath()).endsWith("work/tenant1/reindexing");

		TenantUtils.setTenant("2");
		File file2 = foldersLocator.getReindexingFolder();
		assertThat(file2.getPath()).endsWith("work/tenant2/reindexing");
	}

	@Test
	public void getReindexingAggregatedValuesFolder() {
		TenantUtils.setTenant("1");
		File file = foldersLocator.getReindexingAggregatedValuesFolder();
		assertThat(file.getPath()).endsWith("work/tenant1/reindexing/aggregatedValues");

		TenantUtils.setTenant("2");
		File file2 = foldersLocator.getReindexingAggregatedValuesFolder();
		assertThat(file2.getPath()).endsWith("work/tenant2/reindexing/aggregatedValues");
	}

	@Test
	public void getReindexationLock() {
		TenantUtils.setTenant("1");
		File file = foldersLocator.getReindexationLock();
		assertThat(file.getPath()).endsWith("work/tenant1/reindexing/reindexation.lock");

		TenantUtils.setTenant("2");
		File file2 = foldersLocator.getReindexationLock();
		assertThat(file2.getPath()).endsWith("work/tenant2/reindexing/reindexation.lock");
	}

	@Test
	public void getConstellioProperties() {
		TenantUtils.setTenant("1");
		File file = foldersLocator.getConstellioProperties();
		assertThat(file.getPath()).endsWith("conf/tenant1/constellio.properties");

		TenantUtils.setTenant("2");
		File file2 = foldersLocator.getConstellioProperties();
		assertThat(file2.getPath()).endsWith("conf/tenant2/constellio.properties");
	}

	@Test
	public void getLogsFolder() {
		TenantUtils.setTenant("1");
		File file = foldersLocator.getLogsFolder();
		assertThat(file.getPath()).endsWith("logs/tenant1");

		TenantUtils.setTenant("2");
		File file2 = foldersLocator.getLogsFolder();
		assertThat(file2.getPath()).endsWith("logs/tenant2");
	}

	@Test
	public void getDefaultTempFolder() {
		TenantUtils.setTenant("1");
		File file = foldersLocator.getDefaultTempFolder();
		assertThat(file.getPath()).endsWith("temp/tenant1");

		TenantUtils.setTenant("2");
		File file2 = foldersLocator.getDefaultTempFolder();
		assertThat(file2.getPath()).endsWith("temp/tenant2");
	}

	@Test
	public void getLastAlertFile() {
		TenantUtils.setTenant("1");
		File file = foldersLocator.getLastAlertFile();
		assertThat(file.getPath()).endsWith("temp/tenant1/lastAlert.pdf");

		TenantUtils.setTenant("2");
		File file2 = foldersLocator.getLastAlertFile();
		assertThat(file2.getPath()).endsWith("temp/tenant2/lastAlert.pdf");
	}

	@Test
	public void getDefaultSettingsFolder() {
		TenantUtils.setTenant("1");
		File file = foldersLocator.getDefaultSettingsFolder();
		assertThat(file.getPath()).endsWith("conf/tenant1/settings");

		TenantUtils.setTenant("2");
		File file2 = foldersLocator.getDefaultSettingsFolder();
		assertThat(file2.getPath()).endsWith("conf/tenant2/settings");
	}

	@Test
	public void getPluginsRepository() {
		TenantUtils.setTenant("1");
		File file = foldersLocator.getPluginsRepository();
		assertThat(file.getPath()).endsWith("constellio-plugins/tenant1");

		TenantUtils.setTenant("2");
		File file2 = foldersLocator.getPluginsRepository();
		assertThat(file2.getPath()).endsWith("constellio-plugins/tenant2");
	}

	@Test
	public void getPluginsResourcesFolder() {
		TenantUtils.setTenant("1");
		File file = foldersLocator.getPluginsResourcesFolder();
		assertThat(file.getPath()).endsWith("plugins-modules-resources/tenant1");

		TenantUtils.setTenant("2");
		File file2 = foldersLocator.getPluginsResourcesFolder();
		assertThat(file2.getPath()).endsWith("plugins-modules-resources/tenant2");
	}

	@Test
	public void getReportsResourceFolder() {
		TenantUtils.setTenant("1");
		File file = foldersLocator.getReportsResourceFolder();
		assertThat(file.getPath()).endsWith("resources/tenant1/reports");

		TenantUtils.setTenant("2");
		File file2 = foldersLocator.getReportsResourceFolder();
		assertThat(file2.getPath()).endsWith("resources/tenant2/reports");
	}

	@Test
	public void getConstellioEncryptionFile() {
		TenantUtils.setTenant("1");
		File file = foldersLocator.getConstellioEncryptionFile();
		assertThat(file.getPath()).endsWith("conf/tenant1/key.txt");

		TenantUtils.setTenant("2");
		File file2 = foldersLocator.getConstellioEncryptionFile();
		assertThat(file2.getPath()).endsWith("conf/tenant2/key.txt");
	}

	@Test
	public void getPluginsJarsFolder() {
		TenantUtils.setTenant("1");
		File file = foldersLocator.getPluginsJarsFolder();
		assertThat(file.getPath()).endsWith("WEB-INF/plugins/tenant1");

		TenantUtils.setTenant("2");
		File file2 = foldersLocator.getPluginsJarsFolder();
		assertThat(file2.getPath()).endsWith("WEB-INF/plugins/tenant2");
	}

	@Test
	public void getPluginsToMoveOnStartupFile() {
		TenantUtils.setTenant("1");
		File file = foldersLocator.getPluginsToMoveOnStartupFile();
		assertThat(file.getPath()).endsWith("WEB-INF/pluginsManagement/tenant1/toMoveOnStartup");

		TenantUtils.setTenant("2");
		File file2 = foldersLocator.getPluginsToMoveOnStartupFile();
		assertThat(file2.getPath()).endsWith("WEB-INF/pluginsManagement/tenant2/toMoveOnStartup");
	}

	@Test
	public void getUploadLicenseFile() {
		TenantUtils.setTenant("1");
		File file = foldersLocator.getUploadLicenseFile();
		assertThat(file.getPath()).endsWith("temp/tenant1/license.xml");

		TenantUtils.setTenant("2");
		File file2 = foldersLocator.getUploadLicenseFile();
		assertThat(file2.getPath()).endsWith("temp/tenant2/license.xml");
	}

	@Test
	public void getLicenseFile() {
		TenantUtils.setTenant("1");
		File file = foldersLocator.getLicenseFile();
		assertThat(file.getPath()).endsWith("conf/tenant1/license.xml");

		TenantUtils.setTenant("2");
		File file2 = foldersLocator.getLicenseFile();
		assertThat(file2.getPath()).endsWith("conf/tenant2/license.xml");
	}

	@Test
	public void getWorkFolder() {
		TenantUtils.setTenant("1");
		File file = foldersLocator.getWorkFolder();
		assertThat(file.getPath()).endsWith("work/tenant1");

		TenantUtils.setTenant("2");
		File file2 = foldersLocator.getWorkFolder();
		assertThat(file2.getPath()).endsWith("work/tenant2");
	}

	@Test
	public void getLocalConfigsFile() {
		TenantUtils.setTenant("1");
		File file = foldersLocator.getLocalConfigsFile();
		assertThat(file.getPath()).endsWith("conf/tenant1/local-configs.properties");

		TenantUtils.setTenant("2");
		File file2 = foldersLocator.getLocalConfigsFile();
		assertThat(file2.getPath()).endsWith("conf/tenant2/local-configs.properties");
	}


}
