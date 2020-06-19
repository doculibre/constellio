package com.constellio.app.modules.logs;

import com.constellio.model.utils.TenantUtils;
import com.constellio.sdk.tests.ConstellioTest;
import org.apache.commons.logging.LogFactory;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.core.LoggerContext;
import org.junit.Before;
import org.junit.Test;
import org.slf4j.LoggerFactory;

import java.util.logging.Level;

public class LogsTenantsAcceptTest extends ConstellioTest {

	@Before
	public void setUp() throws Exception {
		givenTwoTenants();
	}

	@Test
	public void givenXMLConfigurationPlugin_whenSimpleLog_ThenLogsCorrectly() throws Exception {

		TenantUtils.setTenant("1");
		Logger logger = LogManager.getLogger(this.getClass());
		LoggerContext ctx = (LoggerContext) LogManager.getContext();
		logger.debug("Debug log message");
		logger.info("Info log message");
		logger.error("Error log message");

		TenantUtils.setTenant("2");
		logger.debug("Debug log message");
		logger.info("Info log message");
		logger.error("Error log message");
	}

	@Test
	public void givenXMLConfigurationPlugin_whenJULORUtilsLogORSLF4_ThenLogsCorrectlyToLog4J() throws Exception {

		TenantUtils.setTenant("1");

		org.slf4j.Logger slf4Logger = LoggerFactory.getLogger(this.getClass());
		org.apache.commons.logging.Log commonsLogger = LogFactory.getLog(this.getClass());
		java.util.logging.Logger jdkLogger = java.util.logging.Logger.getLogger(this.getClass().getName());
		Logger logger = LogManager.getLogger(this.getClass());

		jdkLogger.log(Level.SEVERE, "SEVERE JDK log message");
		commonsLogger.error("Error Apache commons log message");
		slf4Logger.error("Error slf4 log message");
		logger.error("Error LOG4J log message");

		TenantUtils.setTenant("2");
		jdkLogger.log(Level.SEVERE, "SEVERE JDK log message");
		commonsLogger.error("Error Apache commons log message");
		slf4Logger.error("Error slf4 log message");
		logger.error("Error LOG4J log message");
	}

	@Test
	public void givenXMLConfigurationPlugin_whenInfo_ThenLogsCorrectlyINFOWARNDEBUGERRORANDSEVEREToLog4J()
			throws Exception {

		TenantUtils.setTenant("1");

		org.slf4j.Logger slf4Logger = LoggerFactory.getLogger(this.getClass());
		org.apache.commons.logging.Log commonsLogger = LogFactory.getLog(this.getClass());
		java.util.logging.Logger jdkLogger = java.util.logging.Logger.getLogger(this.getClass().getName());
		Logger logger = LogManager.getLogger(this.getClass());

		jdkLogger.log(Level.INFO, "INFO JDK log message");
		commonsLogger.fatal("FATAL Apache commons log message");
		slf4Logger.error("Error slf4 log message");
		logger.debug("DEBUG LOG4J log message");
		logger.fatal("Fatal LOG4J log message");
		logger.error("Error LOG4J log message");
		logger.info("Info LOG4J log message");
		logger.warn("WARN LOG4J log message");

		TenantUtils.setTenant("2");
		logger.trace("TRACE LOG4J log message");
		logger.debug("DEBUG LOG4J log message");
		logger.fatal("Fatal LOG4J log message");
		logger.error("Error LOG4J log message");
		logger.info("Info LOG4J log message");
		logger.warn("WARN LOG4J log message");
	}

	@Test(expected = RuntimeException.class)
	public void givenXMLConfigurationPlugin_whenInfoExceptionThrown_thenLog() {
		TenantUtils.setTenant("1");
		Logger logger = LogManager.getLogger(this.getClass());
		logger.debug("Debug log message");
		System.out.println("sout : Hello from tenant 1");
		new RuntimeException("Exception from tenant 1").printStackTrace();

		TenantUtils.setTenant("2");
		logger.debug("Debug log message");
		System.out.println("sout : Hello from tenant 2");
		throw new RuntimeException("Exception from tenant 2");

	}

}
