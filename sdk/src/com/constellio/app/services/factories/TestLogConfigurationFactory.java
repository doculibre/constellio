package com.constellio.app.services.factories;

import org.apache.log4j.ConsoleAppender;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.core.Filter;
import org.apache.logging.log4j.core.LoggerContext;
import org.apache.logging.log4j.core.config.Configuration;
import org.apache.logging.log4j.core.config.ConfigurationFactory;
import org.apache.logging.log4j.core.config.ConfigurationSource;
import org.apache.logging.log4j.core.config.Order;
import org.apache.logging.log4j.core.config.builder.api.AppenderComponentBuilder;
import org.apache.logging.log4j.core.config.builder.api.ConfigurationBuilder;
import org.apache.logging.log4j.core.config.builder.impl.BuiltConfiguration;
import org.apache.logging.log4j.core.config.plugins.Plugin;

import java.net.URI;

@Plugin(name = "ConstellioLogConfigurationFactory", category = ConfigurationFactory.CATEGORY)
@Order(50)
public class TestLogConfigurationFactory extends ConfigurationFactory {
	static Configuration createConfiguration(final String name, ConfigurationBuilder<BuiltConfiguration> builder) {
		builder.setConfigurationName(name);
		builder.setStatusLevel(Level.ERROR);
		builder.add(builder.newFilter("ThresholdFilter", Filter.Result.ACCEPT, Filter.Result.NEUTRAL).
				addAttribute("level", Level.DEBUG));
		AppenderComponentBuilder appenderBuilder = builder.newAppender("Stdout", "CONSOLE").
				addAttribute("target", ConsoleAppender.SYSTEM_OUT);
		appenderBuilder.add(builder.newLayout("PatternLayout").
				addAttribute("pattern", "%d{HH:mm:ss.SSS} [%t] %-5level %logger{36} - %msg%n"));
		appenderBuilder.add(builder.newFilter("MarkerFilter", Filter.Result.DENY,
				Filter.Result.NEUTRAL).addAttribute("marker", "FLOW"));
		builder.add(appenderBuilder);

		AppenderComponentBuilder file
				= builder.newAppender("log", "File").addAttribute("fileName", "log\\logging.log");
		file.add(builder.newLayout("PatternLayout").
				addAttribute("pattern", "%d{HH:mm:ss.SSS} [%t] %-5level %logger{36} - %msg%n"));
		file.add(builder.newFilter("MarkerFilter", Filter.Result.DENY,
				Filter.Result.NEUTRAL).addAttribute("marker", "FLOW"));
		builder.add(file);

		AppenderComponentBuilder rollingFile
				= builder.newAppender("rolling", "RollingFile").addAttribute("fileName", "log/test-log.log");
		rollingFile.addAttribute("filePattern", "log/zip/rolling-%d{MM-dd-yy}.log.gz");
		rollingFile.add(builder.newLayout("PatternLayout").
				addAttribute("pattern", "%d{HH:mm:ss.SSS} [%t] %-5level %logger{36} - %msg%n"));
		rollingFile.add(builder.newLayout("Policies").addComponent(builder.newLayout("OnStartupTriggeringPolicy"))
				.addComponent(builder.newLayout("SizeBasedTriggeringPolicy").addAttribute("size", "100 KB")));
		rollingFile.add(builder.newFilter("MarkerFilter", Filter.Result.DENY,
				Filter.Result.NEUTRAL).addAttribute("marker", "FLOW"));
		builder.add(rollingFile);

		builder.add(builder.newLogger("org.apache.logging.log4j", Level.DEBUG).add(builder.newAppenderRef("rolling")).
				addAttribute("additivity", false));
		builder.add(builder.newRootLogger(Level.ERROR).add(builder.newAppenderRef("rolling")));
		return builder.build();
	}

	@Override
	public Configuration getConfiguration(final LoggerContext loggerContext, final ConfigurationSource source) {
		return getConfiguration(loggerContext, source.toString(), null);
	}

	@Override
	public Configuration getConfiguration(final LoggerContext loggerContext, final String name,
										  final URI configLocation) {
		ConfigurationBuilder<BuiltConfiguration> builder = newConfigurationBuilder();
		return createConfiguration(name, builder);
	}

	@Override
	protected String[] getSupportedTypes() {
		return new String[]{"*"};
	}
}