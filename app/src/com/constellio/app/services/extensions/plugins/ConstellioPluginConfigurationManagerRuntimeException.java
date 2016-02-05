package com.constellio.app.services.extensions.plugins;

public class ConstellioPluginConfigurationManagerRuntimeException extends RuntimeException {

	public ConstellioPluginConfigurationManagerRuntimeException(String moduleId) {
		super(moduleId);
	}

	public static class ConstellioPluginConfigurationManagerRuntimeException_NoSuchPlugin
			extends ConstellioPluginConfigurationManagerRuntimeException {
		public ConstellioPluginConfigurationManagerRuntimeException_NoSuchPlugin(String moduleId) {
			super(moduleId);
		}
	}

	public static class ConstellioPluginConfigurationManagerRuntimeException_InvalidPluginWithNoStatus
			extends ConstellioPluginConfigurationManagerRuntimeException {

		public ConstellioPluginConfigurationManagerRuntimeException_InvalidPluginWithNoStatus(String moduleId) {
			super(moduleId);
		}
	}

	public static class ConstellioPluginConfigurationManagerRuntimeException_CouldNotEnableInvalidPlugin
			extends ConstellioPluginConfigurationManagerRuntimeException {

		public ConstellioPluginConfigurationManagerRuntimeException_CouldNotEnableInvalidPlugin(String moduleId) {
			super(moduleId);
		}
	}

	public static class ConstellioPluginConfigurationManagerRuntimeException_CouldNotDisableInvalidPlugin
			extends ConstellioPluginConfigurationManagerRuntimeException {

		public ConstellioPluginConfigurationManagerRuntimeException_CouldNotDisableInvalidPlugin(String moduleId) {
			super(moduleId);
		}
	}

	public static class ConstellioPluginConfigurationManagerRuntimeException_CouldNotDisableReadyToInstallPlugin
			extends ConstellioPluginConfigurationManagerRuntimeException {

		public ConstellioPluginConfigurationManagerRuntimeException_CouldNotDisableReadyToInstallPlugin(String moduleId) {
			super(moduleId);
		}
	}
}
