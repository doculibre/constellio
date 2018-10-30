package com.constellio.app.services.systemInformations;

import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class LinuxCommandExecutorTest {

	LinuxCommandExecutor commandExecutor = new LinuxCommandExecutor();

	@Test
	public void testExecuteCommand() {
		String output = commandExecutor.executeCommand("java -version");
		assertThat(output).isNotEmpty();
	}

}
