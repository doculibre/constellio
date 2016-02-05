package com.constellio.model.services.appManagement;

import java.io.File;
import java.io.IOException;

public class InstallationService {

	public static final int CONFIG_FILE_COMPLETION_WAIT_TIME = 1000;
	public static final int CONFIG_FILE_COMPLETION_MAX_WAIT_TIME = 30000;
	private static final String GENCONFIG_BAT_FILE_NAME = "genConfig.bat";
	private static final String GENCONFIG_SH_FILE_NAME = "genConfig.sh";
	private static final String CONFIG_FILE_NAME = "wrapper.conf";
	private final PlatformService plateformService;
	private final WrapperConfigurationService wrapperConfigurationService;

	private final File constellioInstallationDir;

	public InstallationService(File constellioInstallationDir) {
		this(constellioInstallationDir, new PlatformService(), new WrapperConfigurationService());
	}

	public InstallationService(File constellioInstallationDir, PlatformService plateformService,
			WrapperConfigurationService wrapperConfigurationService) {
		this.plateformService = plateformService;
		this.wrapperConfigurationService = wrapperConfigurationService;
		this.constellioInstallationDir = constellioInstallationDir;
	}

	public void launchInstallation()
			throws IOException, InterruptedException {
		generateConfigurationFile();
		createDefaultFolders();
	}

	void generateConfigurationFile()
			throws IOException, InterruptedException {
		executeInstallScript();

		File configFile = getConfigFile();
		wrapperConfigurationService.configureForConstellio(configFile);
	}

	void executeWindowsInstallScript(String processID)
			throws IOException, InterruptedException {
		File genConfigBat = getConfigGeneratorBatScript();
		plateformService.runBatchScriptWithParameters(genConfigBat, processID);
		waitUntilConfigurationFileGenerated();
	}

	void executeLinuxInstallScript(String processID)
			throws IOException, InterruptedException {
		File genConfigScript = getConfigGeneratorSHScript();
		plateformService.runSHScriptWithParameters(genConfigScript, processID);
		waitUntilConfigurationFileGenerated();
	}

	void createDefaultFolders() {
		getLogFolder().mkdirs();
		getCommandFolder().mkdirs();
	}

	File getCommandFolder() {
		return new File(this.constellioInstallationDir, "command");
	}

	File getLogFolder() {
		return new File(this.constellioInstallationDir, "log");
	}

	void waitUntilConfigurationFileGenerated() {
		int totalWait = 0;
		File configFile = getConfigFile();
		while (configFile.length() <= 0) {
			plateformService.sleepWithoutExpectableException(CONFIG_FILE_COMPLETION_WAIT_TIME);
			totalWait += CONFIG_FILE_COMPLETION_WAIT_TIME;
			if (totalWait >= CONFIG_FILE_COMPLETION_MAX_WAIT_TIME) {
				throw new InstallationServiceRuntimeException.ConfigurationFileNotCreated();
			}
		}
	}

	void executeInstallScript()
			throws IOException, InterruptedException {
		String processID = plateformService.getProcessID();
		if (plateformService.isWindows()) {
			executeWindowsInstallScript(processID);
		} else {
			executeLinuxInstallScript(processID);
		}
	}

	File getConfigGeneratorSHScript() {
		return new File(constellioInstallationDir, "bin" + File.separator + GENCONFIG_SH_FILE_NAME);
	}

	File getConfigGeneratorBatScript() {
		return new File(constellioInstallationDir, "bat" + File.separator + GENCONFIG_BAT_FILE_NAME);
	}

	File getConfigFile() {
		return new File(constellioInstallationDir, "conf" + File.separator + CONFIG_FILE_NAME);
	}
}