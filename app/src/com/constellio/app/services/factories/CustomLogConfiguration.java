package com.constellio.app.services.factories;

import com.constellio.model.services.tenant.TenantService;
import org.apache.logging.log4j.core.LoggerContext;
import org.apache.logging.log4j.core.config.ConfigurationSource;
import org.apache.logging.log4j.core.config.xml.XmlConfiguration;

public class CustomLogConfiguration extends XmlConfiguration {

	private static TenantService tenantService = null;

	CustomLogConfiguration(LoggerContext context, ConfigurationSource configSource) {

		super(context, configSource);

	}

	@Override
	protected void doConfigure() {

		super.doConfigure();

	}
}
