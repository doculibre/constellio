package com.constellio.model.services.appManagement;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Matchers;

import com.constellio.sdk.tests.ConstellioTest;

public class PlatformServiceTest extends ConstellioTest {

	private PlatformService platformService;

	private Runtime runtime;

	@Test
	public void givenBatScriptFileAndParametersThenCommandIsGenerated()
			throws IOException {
		File theBatScript = aFile();

		String parameters = aString();

		String theBatScriptAbsolutePath = theBatScript.getAbsolutePath();
		String command = "call \"" + theBatScriptAbsolutePath + "\" " + parameters;
		assertEquals(command, platformService.getBatScriptCommand(theBatScript, parameters));
	}

	@Test
	public void givenDosCommandWithScripFiletAndArgumentsThenCommandToExecuteTheScriptIsGenerated() {
		assertEquals("cmd /c toto.bat", platformService.getDosCommandToLaunchScript("toto.bat"));
	}

	@Test
	public void givenShellCommandWithScripFiletAndArgumentsThenCommandToExecuteTheScriptIsGenerated() {
		String command = aString();
		String[] commandToExecute = { "/bin/sh", "-c", command };

		assertTrue(Arrays.equals(commandToExecute, platformService.getShellCommandToLaunchScript(command)));
	}

	@Test
	public void givenSHScriptFileAndParametersThenCommandIsGenerated() {
		File scriptFolder = mock(File.class);
		File theSHScript = mock(File.class);
		doReturn("file.sh").when(theSHScript).getName();
		doReturn(scriptFolder).when(theSHScript).getParentFile();
		doReturn("/home/bob/").when(scriptFolder).getAbsolutePath();

		String[] parameters = { "param1", "param2" };

		String command = "cd /home/bob/; ./file.sh param1 param2";
		assertEquals(command, platformService.getSHScriptCommand(theSHScript, parameters));
	}

	@Before
	public void setup() {
		platformService = spy(new PlatformService());
		runtime = mock(Runtime.class);

		doReturn(runtime).when(platformService).getRuntime();
	}

	@Test
	public void whenLaunchingDosCommandThenGetRuntimeIsCalled()
			throws IOException {
		String command = aString();
		platformService.launchDosCommand(command);

		verify(platformService, times(1)).getDosCommandToLaunchScript(Matchers.anyString());
		verify(platformService, times(1)).getRuntime();

	}

	@Test
	public void whenLaunchingSHCommandThenGetRuntimeIsCalled()
			throws IOException {
		String command = aString();
		platformService.launchShellCommand(command);

		verify(platformService, times(1)).getShellCommandToLaunchScript(Matchers.anyString());
		verify(platformService, times(1)).getRuntime();
	}

	@Test
	public void whenRunningBatScriptThenRuntimeExecIsCalled()
			throws IOException {
		File theBatScript = aFile();

		String[] parameters = aStringArray();
		platformService.runBatchScriptWithParameters(theBatScript, parameters);

		verify(platformService, times(1)).getBatScriptCommand(Matchers.any(File.class), (String[]) Matchers.anyVararg());
		verify(platformService, times(1)).launchDosCommand(Matchers.anyString());
	}

	@Test
	public void whenRunningSHScriptThenRuntimeExecIsCalled()
			throws IOException {
		File scriptFolder = aFile();
		File theSHScript = aFile(scriptFolder);

		String[] parameters = aStringArray();
		platformService.runSHScriptWithParameters(theSHScript, parameters);

		verify(platformService, times(1)).getSHScriptCommand(Matchers.any(File.class), (String[]) Matchers.anyVararg());
		verify(platformService, times(1)).launchShellCommand(Matchers.anyString());
	}
}
