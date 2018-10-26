package com.constellio.app.services.systemInformations;

import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.when;
import static org.mockito.MockitoAnnotations.initMocks;

public class LinuxCommandProcessorTest {

	@Mock LinuxCommandExecutor commandExecutor;
	@InjectMocks LinuxCommandProcessor commandProcessor;

	@Before
	public void setUp() throws Exception {
		initMocks(this);
	}

	@Test
	public void testGetLinuxVersion() throws Exception {
		when(commandExecutor.executeCommand(anyString())).thenReturn("3.10.0-693.11.6.el7.x86_64");

		String version = commandProcessor.getLinuxVersion();
		assertThat(version).isEqualTo("693.11.6");
	}

	@Test
	public void testGetJavaVersion() throws Exception {
		when(commandExecutor.executeCommand(anyString()))
				.thenReturn("java version \"1.7.0_101\"" +
							"OpenJDK Runtime Environment (rhel-2.6.6.1.el7_2-x86_64 u101-b00)" +
							"OpenJDK 64-Bit Server VM (build 24.95-b01, mixed mode)");

		String version = commandProcessor.getJavaVersion();
		assertThat(version).isEqualTo("1.7.0");
	}

	@Test
	public void testGetRepository() throws Exception {
		when(commandExecutor.executeCommand(anyString())).thenReturn("2");

		String repository = commandProcessor.getRepository();
		assertThat(repository).isEqualTo("2");
	}

	@Test
	public void testGetPIDConstellio() throws Exception {
		when(commandExecutor.executeCommand(anyString()))
				.thenReturn("Constellio EIM is running: PID:2680, Wrapper:STARTED, Java:STARTED");

		String repository = commandProcessor.getPIDConstellio();
		assertThat(repository).isEqualTo("2680");
	}

	@Test
	public void testGetPIDConstellioWhenConstellioIsNotRunning() throws Exception {
		when(commandExecutor.executeCommand(anyString())).thenReturn("Constellio EIM is not running.");

		String pid = commandProcessor.getPIDConstellio();
		assertThat(pid).isEmpty();
	}

	@Test
	public void testGetUser() throws Exception {
		when(commandExecutor.executeCommand(anyString())).thenReturn("USERroot");

		String user = commandProcessor.getUser("1234");
		assertThat(user).isEqualTo("root");
	}

	@Test
	public void testGetUserWithInvalidPID() throws Exception {
		when(commandExecutor.executeCommand(anyString())).thenReturn("USER");

		String user = commandProcessor.getUser("abc");
		assertThat(user).isEmpty();
	}

	@Test
	public void testGetPIDSolr() throws Exception {
		when(commandExecutor.executeCommand(anyString()))
				.thenReturn("Solr" + "1649" + "{" + "\"solr_home\":\"/var/solr/\"" + "\"version\":\"5.0.0" +
							"\"startTime\":\"2018-09-04T20:03:38.416Z\"," + "\"uptime\":\"51" + "\"memory\":\"403.8)");

		String pid = commandProcessor.getPIDSolr();
		assertThat(pid).isEqualTo("1649");
	}

	@Test
	public void testGetPIDSolrWhenSolrIsNotInstalled() throws Exception {
		when(commandExecutor.executeCommand(anyString()))
				.thenReturn("-bash: /opt/solr/bin/solr: No such file or directory");

		String pid = commandProcessor.getPIDConstellio();
		assertThat(pid).isEmpty();
	}

	@Test
	public void testGetDiskUsage() throws Exception {
		when(commandExecutor.executeCommand(anyString())).thenReturn("80%");

		String diskUsage = commandProcessor.getDiskUsage("/valid");
		assertThat(diskUsage).isEqualTo("80%");
	}

	@Test
	public void testGetDiskUsageWithInvalidPath() throws Exception {
		when(commandExecutor.executeCommand(anyString())).thenReturn("df: ‘/invalid’: No such file or directory");

		String diskUsage = commandProcessor.getDiskUsage("/invalid");
		assertThat(diskUsage).isEmpty();
	}
}
