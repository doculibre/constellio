package com.constellio.app.services.extensions.plugins;

import com.constellio.model.entities.modules.ConstellioPlugin;

@SuppressWarnings("serial")
public class PluginServicesRuntimeException extends RuntimeException {

	public PluginServicesRuntimeException(String message, Throwable cause) {
		super(message, cause);
	}

	public PluginServicesRuntimeException(String message) {
		super(message);
	}

	public PluginServicesRuntimeException(Throwable cause) {
		super(cause);
	}

	public static class CannotStartPlugin extends PluginServicesRuntimeException {

		public CannotStartPlugin(ConstellioPlugin plugin, Exception e) {
			super("Cannot start plugin '" + plugin.getName() + "' published by '" + plugin.getPublisher() + "'", e);
		}

	}

	public static class CannotStopPlugin extends PluginServicesRuntimeException {

		public CannotStopPlugin(ConstellioPlugin plugin, Exception e) {
			super("Cannot stop plugin '" + plugin.getName() + "' published by '" + plugin.getPublisher() + "'", e);
		}

	}

}
