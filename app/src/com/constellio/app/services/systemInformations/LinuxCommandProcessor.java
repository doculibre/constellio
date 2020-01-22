package com.constellio.app.services.systemInformations;

import com.constellio.app.services.systemInformations.exceptions.LinuxCommandExecutionFailedException;
import org.apache.commons.lang.StringUtils;

public class LinuxCommandProcessor {

	private LinuxCommandExecutor commandExecutor;

	private final static String LINUX_VERSION_COMMAND = "uname -r";
	private final static String JAVA_VERSION_COMMAND = "rpm -qa | grep 'jdk-1'";
	private final static String REPOSITORY_COMMAND = "yum repolist 2>&1 | grep -c \"constellio_constellio-updates\"";
	private final static String PID_CONSTELLIO_COMMAND = "/opt/constellio/startup status";
	private final static String USER_COMMAND = "ps -u -p %s | cut -d \" \" -f 1";
	private final static String PID_SOLR_COMMAND = "/opt/solr/bin/solr status | cut -d \" \" -f 3";
	private final static String DISK_USAGE_COMMAND = "df -k %s | tail -1 | tr -s ' ' | cut -d ' ' -f 5";
	private final static String DISK_USAGE_EXTENDED_COMMAND = "df -k -h %s | tail -l | tr -s ' ' | sed '$!d' | cut -d ' ' -f 2-5";

	public LinuxCommandProcessor() {
		commandExecutor = new LinuxCommandExecutor();
	}

	public String getLinuxVersion() throws LinuxCommandExecutionFailedException {
		String output = executeCommand(LINUX_VERSION_COMMAND);
		String subVersion = StringUtils.substringAfter(output, "-");
		return StringUtils.substringBefore(subVersion, ".el7");
	}

	public String getJavaVersion() throws LinuxCommandExecutionFailedException {
		String output = executeCommand(JAVA_VERSION_COMMAND);
		String subString = StringUtils.substringAfter(output, "java-");
		return StringUtils.substringBefore(subString, "-");
	}

	public String getRepository() throws LinuxCommandExecutionFailedException {
		return executeCommand(REPOSITORY_COMMAND);
	}

	public String getPIDConstellio() throws LinuxCommandExecutionFailedException {
		String output = executeCommand(PID_CONSTELLIO_COMMAND);
		String subResult = StringUtils.substringAfter(output, "PID:");
		return StringUtils.substringBefore(subResult, ",");
	}

	public String getUser(String pid) throws LinuxCommandExecutionFailedException {
		String output = executeCommand(String.format(USER_COMMAND, pid));
		return StringUtils.substringAfter(output, "USER");
	}

	public String getPIDSolr() throws LinuxCommandExecutionFailedException {
		String output = executeCommand(PID_SOLR_COMMAND);
		String subResult = StringUtils.substringAfter(output, "Solr");
		return StringUtils.substringBefore(subResult, "{");
	}

	public String getDiskUsage(String path) throws LinuxCommandExecutionFailedException {
		String output = executeCommand(String.format(DISK_USAGE_COMMAND, path));
		if (!output.contains("%")) {
			output = "";
		}
		return output;
	}

	public String getDiskUsageExtended(String path) throws LinuxCommandExecutionFailedException {
		//		String output = executeCommand(String.format(DISK_AVAILABILITY_COMMAND, path));
		//		if (!output.contains("%")) {
		//			output = "";
		//		}
		//		return output;
		return executeCommand(String.format(DISK_USAGE_EXTENDED_COMMAND, path));
	}

	private String executeCommand(String command) throws LinuxCommandExecutionFailedException {
		return commandExecutor.executeCommand(command);
	}

}
