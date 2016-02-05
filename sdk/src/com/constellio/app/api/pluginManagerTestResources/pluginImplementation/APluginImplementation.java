package com.constellio.app.api.pluginManagerTestResources.pluginImplementation;

import net.xeoh.plugins.base.annotations.PluginImplementation;

import com.constellio.app.api.APlugin;

@PluginImplementation
public class APluginImplementation implements APlugin {

	public static final String ID = "A plugin implementation id";
	public static final String NAME = "A plugin implementation";
	private static boolean started = false;

	public static boolean isStarted() {
		return started;
	}

	@Override
	public String getId() {
		return ID;
	}

	@Override
	public String getName() {
		return NAME;
	}

	@Override
	public String getPublisher() {
		return DOCULIBRE;
	}

	@Override
	public void doSomething(String withParameter) {
	}
}
