package com.constellio.app.modules.logs;


import com.constellio.data.conf.FoldersLocator;
import com.constellio.data.utils.TenantUtils;
import com.constellio.sdk.tests.ConstellioTest;
import org.apache.commons.io.input.ReversedLinesFileReader;
import org.apache.commons.logging.LogFactory;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.junit.Before;
import org.junit.Test;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;

import static org.assertj.core.api.Assertions.assertThat;

public class LogsTenantsAcceptTest extends ConstellioTest {

	@Before
	public void setUp() throws Exception {

		givenTwoTenants();
	}

	@Test
	public void givenXMLConfigurationPlugin_whenSimpleLog_ThenLogsCorrectly() throws Exception {

		TenantUtils.setTenant("1");
		Logger logger = LogManager.getLogger(this.getClass());
		logger.debug("Debug log message");
		logger.info("Info log message");
		logger.error("Error log message tenant 1");

		TenantUtils.setTenant("2");
		logger.debug("Debug log message");
		logger.info("Info log message");
		logger.error("Error log message tenant 2");

		File logsFolder = new File(new FoldersLocator().getSDKProject(), "logs");
		File logsTenant1Folder = new File(logsFolder, "T01");
		File logsTenant2Folder = new File(logsFolder, "T02");

		assertThat(logsFolder.exists()).isTrue();
		assertThat(logsTenant1Folder.exists()).isTrue();
		assertThat(logsTenant2Folder.exists()).isTrue();

		File logsTenant1 = new File(logsTenant1Folder, "constellio.log");
		File logsTenant2 = new File(logsTenant2Folder, "constellio.log");

		assertThat(logsTenant1.exists()).isTrue();
		assertThat(logsTenant2.exists()).isTrue();

		assertThat(reverseLines(logsTenant1)).contains("Error log message tenant 1");
		assertThat(reverseLines(logsTenant2)).contains("Error log message tenant 2");

	}

	@Test
	public void givenXMLConfigurationPlugin_whenJULORUtilsLogORSLF4_ThenLogsCorrectlyToLog4J() throws Exception {

		TenantUtils.setTenant("1");

		org.slf4j.Logger slf4Logger = LoggerFactory.getLogger(this.getClass());
		org.apache.commons.logging.Log commonsLogger = LogFactory.getLog(this.getClass());
		java.util.logging.Logger jdkLogger = java.util.logging.Logger.getLogger(this.getClass().getName());
		Logger logger = LogManager.getLogger(this.getClass());

		jdkLogger.log(Level.SEVERE, "SEVERE JDK log message Tenant 1");
		commonsLogger.error("Error Apache commons log message Tenant 1");
		slf4Logger.error("Error slf4 log message Tenant 1");
		logger.error("Error LOG4J log message Tenant 1");

		TenantUtils.setTenant("2");
		jdkLogger.log(Level.SEVERE, "SEVERE JDK log message Tenant 2");
		commonsLogger.error("Error Apache commons log message Tenant 2");
		slf4Logger.error("Error slf4 log message Tenant 2");
		logger.error("Error LOG4J log message Tenant 2");

		File logsFolder = new File(new FoldersLocator().getSDKProject(), "logs");
		File logsTenant1 = new File(logsFolder, "T01/constellio.log");
		File logsTenant2 = new File(logsFolder, "T02/constellio.log");

		List<String> linesT01 = reverseLinesAndGetLastNumberOffLines(logsTenant1, 4);
		List<String> linesT02 = reverseLinesAndGetLastNumberOffLines(logsTenant2, 4);

		assertThat(linesT01.get(0)).contains("Error LOG4J log message Tenant 1");
		assertThat(linesT02.get(0)).contains("Error LOG4J log message Tenant 2");

		assertThat(linesT01.get(1)).contains("Error slf4 log message Tenant 1");
		assertThat(linesT02.get(1)).contains("Error slf4 log message Tenant 2");

		assertThat(linesT01.get(2)).contains("Error Apache commons log message Tenant 1");
		assertThat(linesT02.get(2)).contains("Error Apache commons log message Tenant 2");

		assertThat(linesT01.get(3)).contains("SEVERE JDK log message Tenant 1");
		assertThat(linesT02.get(3)).contains("SEVERE JDK log message Tenant 2");

	}

	@Test
	public void givenXMLConfigurationPlugin_whenInfo_ThenLogsCorrectlyINFOWARNDEBUGERRORANDSEVEREToLog4J()
			throws Exception {

		TenantUtils.setTenant("1");

		org.slf4j.Logger slf4Logger = LoggerFactory.getLogger(this.getClass());
		org.apache.commons.logging.Log commonsLogger = LogFactory.getLog(this.getClass());
		java.util.logging.Logger jdkLogger = java.util.logging.Logger.getLogger(this.getClass().getName());
		Logger logger = LogManager.getLogger(this.getClass());

		jdkLogger.log(Level.INFO, "INFO JDK log message Tenant 1");
		commonsLogger.fatal("FATAL Apache commons log message Tenant 1");
		slf4Logger.error("Error slf4 log message Tenant 1");
		logger.debug("DEBUG LOG4J log message Tenant 1");
		logger.fatal("Fatal LOG4J log message Tenant 1");
		logger.error("Error LOG4J log message Tenant 1");
		logger.info("Info LOG4J log message Tenant 1");
		logger.warn("WARN LOG4J log message Tenant 1");

		TenantUtils.setTenant("2");
		logger.trace("TRACE LOG4J log message Tenant 2");
		logger.debug("DEBUG LOG4J log message Tenant 2");
		logger.fatal("Fatal LOG4J log message Tenant 2");
		logger.error("Error LOG4J log message Tenant 2");
		logger.info("Info LOG4J log message Tenant 2");
		logger.warn("WARN LOG4J log message Tenant 2");

		File logsFolder = new File(new FoldersLocator().getSDKProject(), "logs");
		File logsTenant1 = new File(logsFolder, "T01/constellio.log");
		File logsTenant2 = new File(logsFolder, "T02/constellio.log");

		List<String> linesT01 = reverseLinesAndGetLastNumberOffLines(logsTenant1, 4);
		List<String> linesT02 = reverseLinesAndGetLastNumberOffLines(logsTenant2, 4);

		assertThat(linesT01.get(0)).contains("WARN LOG4J log message Tenant 1");
		assertThat(linesT02.get(0)).contains("WARN LOG4J log message Tenant 2");

		assertThat(linesT01.get(1)).contains("Info LOG4J log message Tenant 1");
		assertThat(linesT02.get(1)).contains("Info LOG4J log message Tenant 2");

	}

	@Test(expected = RuntimeException.class)
	public void givenXMLConfigurationPlugin_whenInfoExceptionThrown_thenLog() {
		TenantUtils.setTenant("1");
		Logger logger = LogManager.getLogger(this.getClass());
		logger.debug("Debug log message Tenant 1");
		System.out.println("sout : Hello from tenant 1");
		new RuntimeException("Exception from tenant 1").printStackTrace();

		TenantUtils.setTenant("2");
		logger.debug("Debug log message Tenant 1");
		System.out.println("sout : Hello from tenant 2");

		File logsFolder = new File(new FoldersLocator().getSDKProject(), "logs");
		File logsTenant1 = new File(logsFolder, "T01/constellio.log");
		File logsTenant2 = new File(logsFolder, "T02/constellio.log");
		List<String> linesT01 = reverseLinesAndGetLastNumberOffLines(logsTenant1, 4);
		List<String> linesT02 = reverseLinesAndGetLastNumberOffLines(logsTenant2, 4);

		assertThat(linesT01.get(0)).contains("Debug LOG4J log message Tenant 1");
		assertThat(linesT02.get(0)).contains("Debug LOG4J log message Tenant 2");
		throw new RuntimeException("Exception from tenant 2");

	}

	public String reverseLines(File file) {
		ReversedLinesFileReader object = null;
		String line = null;
		int i = 0;// just in case
		try {
			object = new ReversedLinesFileReader(file);
			line = object.readLine();
			while ((line == null || line.isEmpty()) && i < 10) {
				i++;
				line = object.readLine();
			}
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			try {
				object.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}

		return line;
	}

	public List<String> reverseLinesAndGetLastNumberOffLines(File file, int num) {
		ReversedLinesFileReader object = null;
		List<String> lines = new ArrayList<>();
		String line = null;
		int i = 0;// just in case
		try {
			object = new ReversedLinesFileReader(file);
			while (num > 0) {
				line = object.readLine();
				while ((line == null || line.isEmpty()) && i < 10) {
					i++;
					line = object.readLine();
				}
				lines.add(line);
				num--;
			}
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			try {
				object.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}

		return lines;
	}

}
