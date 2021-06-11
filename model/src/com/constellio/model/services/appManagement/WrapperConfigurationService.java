package com.constellio.model.services.appManagement;

import com.constellio.data.io.services.facades.FileService;
import org.apache.commons.lang3.StringUtils;

import java.io.File;
import java.io.IOException;

public class WrapperConfigurationService {

	public static final String ASSIGNING_VALUE = "=";
	public static final String COMMENT = "#";
	public static final String CARRIAGE_RETURN = "\n";
	public static final String LOGFILE = "wrapper.logfile";
	public static final String LOGFILE_VALUE = "../log/wrapper.log";
	public static final String CONSOLE_TITLE = "wrapper.console.title";
	public static final String NTSERVICE_NAME = "wrapper.ntservice.name";
	public static final String NTSERVICE_DISPLAYNAME = "wrapper.ntservice.displayname";
	public static final String NTSERVICE_DESCRIPTION = "wrapper.ntservice.description";
	public static final String BLOCK_TITLE_SEPARATOR = "#********************************************************************";
	public static final String COMMAND_CONDITION_BLOCK_TITLE = "# commandCondition:";
	public static final String CONDITION_SCRIPT = "wrapper.condition.script";
	public static final String CONDITION_SCRIPT_VALUE = "scripts/commandCondition.gv";
	public static final String CONDITION_SCRIPT_ARGS = "wrapper.condition.script.args";
	public static final String CONDITION_SCRIPT_ARGS_VALUE = "command/cmd.txt";
	public static final String CONDITION_SCRIPT_CYCLE = "wrapper.condition.cycle";
	public static final String CONDITION_SCRIPT_CYCLE_VALUE = "1";
	public static final String RESTART_RELOAD_CONFIGURATION = "wrapper.restart.reload_configuration";
	public static final String RESTART_RELOAD_CONFIGURATION_VALUE = "TRUE";
	public static final String RESTART_RELOAD_CACHE = "wrapper.restart.reload_cache";
	public static final String RESTART_RELOAD_CACHE_VALUE = "TRUE";
	public static final String CONTROL = "wrapper.control";
	public static final String CONTROL_VALUE = "APPLICATION";
	private static final String SOFTWARE_NAME = "Constellio";
	public static final String CONSOLE_TITLE_VALUE = SOFTWARE_NAME;
	public static final String NTSERVICE_NAME_VALUE = SOFTWARE_NAME;
	public static final String NTSERVICE_DISPLAYNAME_VALUE = SOFTWARE_NAME;
	public static final String NTSERVICE_DESCRIPTION_VALUE = SOFTWARE_NAME;

	public void configureForConstellio(File defaultConfigFile)
			throws IOException {

		FileService fileService = new FileService(null);

		String configFileContent;
		try {
			configFileContent = fileService.readFileToString(defaultConfigFile);
		} catch (IOException e) {
			throw new IOException("Error while reading configuration file", e);
		}

		String modifiedConfigFileContent = modifyFileContent(configFileContent);
		try {
			fileService.replaceFileContent(defaultConfigFile, modifiedConfigFileContent);
		} catch (IOException e) {
			throw new IOException("Error while writing in configuration file", e);
		}

	}

	public void addJavaAdditionnalProperty(File configFile, String property, String value)
			throws IOException {

		FileService fileService = new FileService(null);

		StringBuilder newContent = new StringBuilder();

		boolean added = false;
		boolean inAdditionnalSection = false;
		int lastIndex = 0;
		for (String line : fileService.readFileToLinesWithoutExpectableIOException(configFile)) {
			if (line.startsWith("wrapper.java.additional") && !line.startsWith("wrapper.java.additional.auto")) {
				inAdditionnalSection = true;
				String additionalParam = StringUtils.substringAfter(line, "=");
				lastIndex++;
				newContent.append("wrapper.java.additional." + lastIndex + "=" + additionalParam + "\n");

			} else {
				if (inAdditionnalSection) {
					inAdditionnalSection = false;
					if (!added) {
						added = true;
						lastIndex++;
						newContent.append("wrapper.java.additional." + lastIndex + "=-D" + property + "=" + value + "\n");
					}

				}
				newContent.append(line + "\n");
			}
		}

		try {
			fileService.replaceFileContent(configFile, newContent.toString());
		} catch (IOException e) {
			throw new IOException("Error while writing in configuration file", e);
		}
	}

	private String modifyFileContent(String configFileContent) {
		StringBuilder configFileContentBuffer = new StringBuilder();

		configureExistingProperties(configFileContent, configFileContentBuffer);
		addProperties(configFileContentBuffer);

		return configFileContentBuffer.toString();
	}

	private void addProperties(StringBuilder configFileContentBuffer) {
		configFileContentBuffer.append(BLOCK_TITLE_SEPARATOR + CARRIAGE_RETURN);
		configFileContentBuffer.append(COMMAND_CONDITION_BLOCK_TITLE + CARRIAGE_RETURN);
		configFileContentBuffer.append(BLOCK_TITLE_SEPARATOR + CARRIAGE_RETURN);
		configFileContentBuffer.append(CONDITION_SCRIPT + ASSIGNING_VALUE + CONDITION_SCRIPT_VALUE + CARRIAGE_RETURN);
		configFileContentBuffer.append(CONDITION_SCRIPT_ARGS + ASSIGNING_VALUE + CONDITION_SCRIPT_ARGS_VALUE + CARRIAGE_RETURN);
		configFileContentBuffer.append(CONDITION_SCRIPT_CYCLE + ASSIGNING_VALUE + CONDITION_SCRIPT_CYCLE_VALUE + CARRIAGE_RETURN);
		configFileContentBuffer.append(RESTART_RELOAD_CONFIGURATION + ASSIGNING_VALUE + RESTART_RELOAD_CONFIGURATION_VALUE
									   + CARRIAGE_RETURN);
		configFileContentBuffer.append(RESTART_RELOAD_CACHE + ASSIGNING_VALUE + RESTART_RELOAD_CACHE_VALUE + CARRIAGE_RETURN);
		configFileContentBuffer.append(CONTROL + ASSIGNING_VALUE + CONTROL_VALUE + CARRIAGE_RETURN);
	}

	private void configureExistingProperties(String configFileContent, StringBuilder configFileContentBuffer) {
		String[] configFileLines = configFileContent.split(CARRIAGE_RETURN);
		for (String configFileLine : configFileLines) {
			String modifiedConfigFileLine = replaceValue(configFileLine) + CARRIAGE_RETURN;
			configFileContentBuffer.append(modifiedConfigFileLine);
		}
	}

	private String replaceValue(String configFileLine) {
		String returnedLine = configFileLine;
		if (!configFileLine.contains(COMMENT)) {
			returnedLine = replaceConfigLine(configFileLine);
		}
		return returnedLine;
	}

	private String replaceConfigLine(String configFileLine) {
		String returnedLine = configFileLine;
		String keyConfigFileLine = configFileLine.split(ASSIGNING_VALUE)[0];
		if (keyConfigFileLine.equals(LOGFILE)) {
			returnedLine = LOGFILE + ASSIGNING_VALUE + LOGFILE_VALUE;
		} else if (keyConfigFileLine.equals(CONSOLE_TITLE)) {
			returnedLine = CONSOLE_TITLE + ASSIGNING_VALUE + CONSOLE_TITLE_VALUE;
		} else if (keyConfigFileLine.equals(NTSERVICE_NAME)) {
			returnedLine = NTSERVICE_NAME + ASSIGNING_VALUE + NTSERVICE_NAME_VALUE;
		} else if (keyConfigFileLine.equals(NTSERVICE_DISPLAYNAME)) {
			returnedLine = NTSERVICE_DISPLAYNAME + ASSIGNING_VALUE + NTSERVICE_DISPLAYNAME_VALUE;
		} else if (keyConfigFileLine.equals(NTSERVICE_DESCRIPTION)) {
			returnedLine = NTSERVICE_DESCRIPTION + ASSIGNING_VALUE + NTSERVICE_DESCRIPTION_VALUE;
		}
		return returnedLine;
	}
}