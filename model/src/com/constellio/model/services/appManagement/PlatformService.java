package com.constellio.model.services.appManagement;

import java.io.File;
import java.io.IOException;
import java.lang.management.ManagementFactory;

import org.apache.commons.lang3.StringUtils;

public class PlatformService {

	private static final String SPACE_CHAR = " ";

	public boolean isWindows() {
		return System.getProperties().getProperty("os.name").contains("Windows");
	}

	public void launchDosCommand(String command)
			throws IOException {
		String dosCommandToExecute = getDosCommandToLaunchScript(command);
		getRuntime().exec(dosCommandToExecute);
	}

	String getDosCommandToLaunchScript(String command) {
		return "cmd /c " + command;
	}

	void launchShellCommand(String command)
			throws IOException {
		getRuntime().exec(getShellCommandToLaunchScript(command));
	}

	String[] getShellCommandToLaunchScript(String command) {
		return new String[] { "/bin/sh", "-c", command };
	}

	public String getProcessID() {
		return ManagementFactory.getRuntimeMXBean().getName().split("@")[0];
	}

	public void sleepWithoutExpectableException(long miliseconds) {
		try {
			sleep(miliseconds);
		} catch (InterruptedException e) {
			throw new PlatformServiceRuntimeException.Interrupted(e);
		}
	}

	public void sleep(long miliseconds)
			throws InterruptedException {
		Thread.sleep(miliseconds);
	}

	public void runSHScriptWithParameters(File theSHScript, String... parameters)
			throws IOException {
		String runningCommand = getSHScriptCommand(theSHScript, parameters);
		launchShellCommand(runningCommand);
	}

	String getSHScriptCommand(File theSHScript, String... parameters) {
		String parentAbsolutePath = theSHScript.getParentFile().getAbsolutePath();

		String parametersString = StringUtils.join(parameters, SPACE_CHAR);

		return "cd " + parentAbsolutePath + "; ./" + theSHScript.getName() + SPACE_CHAR + parametersString;
	}

	public void runBatchScriptWithParameters(File theBatScript, String... parameters)
			throws IOException {
		String runningCommand = getBatScriptCommand(theBatScript, parameters);
		launchDosCommand(runningCommand);
	}

	String getBatScriptCommand(File theBatScript, String... parameters)
			throws IOException {
		String parametersString = StringUtils.join(parameters, SPACE_CHAR);

		return "call \"" + theBatScript.getCanonicalPath() + "\" " + parametersString;
	}

	Runtime getRuntime() {
		return Runtime.getRuntime();
	}

}
