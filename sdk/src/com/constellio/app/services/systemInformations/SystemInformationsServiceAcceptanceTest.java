package com.constellio.app.services.systemInformations;

import com.constellio.sdk.tests.ConstellioTest;
import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.when;
import static org.mockito.MockitoAnnotations.initMocks;

public class SystemInformationsServiceAcceptanceTest extends ConstellioTest {

	@Mock LinuxCommandProcessor linuxCommandProcessor;
	@InjectMocks SystemInformationsService systemInformationsService;

	private final static String JAVA_VERSION = "1.7.0";
	private final static String LINUX_VERSION = "693.11.6";
	private final static String PID_CONSTELLIO = "963";
	private final static String PID_SOLR = "735";
	private final static String USER = "root";
	private final static String DISK_USAGE = "80%";
	private final static String REPOSITORY = "2";

	@Before
	public void setUp() throws Exception {
		initMocks(this);

		when(linuxCommandProcessor.getJavaVersion()).thenReturn(JAVA_VERSION);
		when(linuxCommandProcessor.getLinuxVersion()).thenReturn(LINUX_VERSION);
		when(linuxCommandProcessor.getDiskUsage(anyString())).thenReturn(DISK_USAGE);
		when(linuxCommandProcessor.getPIDConstellio()).thenReturn(PID_CONSTELLIO);
		when(linuxCommandProcessor.getPIDSolr()).thenReturn(PID_SOLR);
		when(linuxCommandProcessor.getRepository()).thenReturn(REPOSITORY);
		when(linuxCommandProcessor.getUser(anyString())).thenReturn(USER);
	}

	@Test
	public void givenJavaVersionIsNullThenDeprecatedIsTrue() {
		String version = null;
		assertThat(systemInformationsService.isJavaVersionDeprecated(version)).isTrue();
	}

	@Test
	public void givenJavaVersionIsLowerThenDeprecatedIsTrue() {
		String version = "1.10.0";
		assertThat(systemInformationsService.isJavaVersionDeprecated(version)).isTrue();
	}

	@Test
	public void givenJavaVersionIsEqualsThenDeprecatedIsFalse() {
		String version = "1.11.0";
		assertThat(systemInformationsService.isJavaVersionDeprecated(version)).isFalse();
	}

	@Test
	public void givenJavaVersionIsHigherThenDeprecatedIsFalse() {
		String version = "1.11.1";
		assertThat(systemInformationsService.isJavaVersionDeprecated(version)).isFalse();
	}

	@Test
	public void givenLinuxVersionIsNullThenDeprecatedIsTrue() {
		String version = null;
		assertThat(systemInformationsService.isLinuxVersionDeprecated(version)).isTrue();
	}

	@Test
	public void givenLinuxVersionIsLowerThenDeprecatedIsTrue() {
		String version = "692.11.6";
		assertThat(systemInformationsService.isLinuxVersionDeprecated(version)).isTrue();
	}

	@Test
	public void givenLinuxVersionIsEqualsThenDeprecatedIsFalse() {
		String version = "862.3.2";
		assertThat(systemInformationsService.isLinuxVersionDeprecated(version)).isFalse();
	}

	@Test
	public void givenLinuxVersionIsHigherThenDeprecatedIsFalse() {
		String version = "862.4.2";
		assertThat(systemInformationsService.isLinuxVersionDeprecated(version)).isFalse();
	}

	@Test
	public void givenSolrVersionIsNullThenDeprecatedIsTrue() {
		String version = null;
		assertThat(systemInformationsService.isSolrVersionDeprecated(version)).isTrue();
	}

	@Test
	public void givenSolrVersionIsLowerThenDeprecatedIsTrue() {
		String version = "0.7.0";
		assertThat(systemInformationsService.isSolrVersionDeprecated(version)).isTrue();
	}

	@Test
	public void givenSolrVersionIsEqualsThenDeprecatedIsFalse() {
		String version = "1.7.0";
		assertThat(systemInformationsService.isSolrVersionDeprecated(version)).isFalse();
	}

	@Test
	public void givenSolrVersionIsHigherThenDeprecatedIsFalse() {
		String version = "1.7.49";
		assertThat(systemInformationsService.isSolrVersionDeprecated(version)).isFalse();
	}

	@Test
	public void givenDiskUsageIsLowerThenProblematicIsFalse() {
		String diskUsage = "79%";
		assertThat(systemInformationsService.isDiskUsageProblematic(diskUsage)).isFalse();
	}

	@Test
	public void givenDiskUsageIsEqualsThenProblematicIsFalse() {
		String diskUsage = "80%";
		assertThat(systemInformationsService.isDiskUsageProblematic(diskUsage)).isFalse();
	}

	@Test
	public void givenDiskUsageIsHighThenProblematicIsTrue() {
		String diskUsage = "81%";
		assertThat(systemInformationsService.isDiskUsageProblematic(diskUsage)).isTrue();
	}

	@Test
	public void givenPrivateRepositoryNotInstalledThenRepositoryIsNotInstalled() throws Exception {
		when(linuxCommandProcessor.getRepository()).thenReturn("0");

		assertThat(systemInformationsService.isPrivateRepositoryInstalled()).isFalse();
	}

	@Test
	public void givenPrivateRepositoryInstalledThenRepositoryIsInstalled() {
		assertThat(systemInformationsService.isPrivateRepositoryInstalled()).isTrue();
	}

	@Test
	public void testGetWrapperJavaVersion() {
		String version = systemInformationsService.getWrapperJavaVersion();
		assertThat(version).isNotNull().doesNotContain("_");
	}

	@Test
	public void testGetSolrUser() {
		String user = systemInformationsService.getSolrUser();
		assertThat(user).isEqualTo(USER);
	}

	@Test
	public void testGetConstellioUser() {
		String user = systemInformationsService.getConstellioUser();
		assertThat(user).isEqualTo(USER);
	}

	@Test
	public void testGetLinuxVersion() {
		String version = systemInformationsService.getLinuxVersion();
		assertThat(version).isEqualTo(LINUX_VERSION);
	}

	@Test
	public void testGetSolrVersion() {
		String version = systemInformationsService.getSolrVersion();
		assertThat(version).isNotNull();
	}

	@Test
	public void testGetJavaVersion() {
		String version = systemInformationsService.getJavaVersion();
		assertThat(version).isEqualTo(JAVA_VERSION);
	}

	@Test
	public void testGetDiskUsage() {
		String diskUsage = systemInformationsService.getDiskUsage("/test");
		assertThat(diskUsage).isEqualTo(DISK_USAGE);
	}

}