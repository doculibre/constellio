package com.constellio.app.services.systemSetup;

import com.constellio.data.dao.managers.StatefulService;
import com.constellio.data.utils.PropertyFileUtils;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

public class SystemLocalConfigsManager implements StatefulService {

	public final static String SYSTEM_GLOBAL_PROPERTIES = "/globalProperties";

	final static String RESTART_REQUIRED = "restartRequired";
	final static String MARKED_FOR_REINDEXING = "markedForReindexing";
	final static String MARKED_FOR_CACHE_REBUILD = "markedForCacheRebuild";
	final static String LOCAL_CACHE_VERSION = "localCacheVersion";
	final static String FAIL_SAFE_MODE_ENABLED = "failSafeModeEnabled";

	private File propertyFile;
	private Map<String, String> properties;

	private SystemGlobalConfigsManager systemGlobalConfigsManager;

	public SystemLocalConfigsManager(File propertyFile, SystemGlobalConfigsManager systemGlobalConfigsManager) {
		this.propertyFile = propertyFile;
		this.systemGlobalConfigsManager = systemGlobalConfigsManager;
	}

	@Override
	public void initialize() {
		if (propertyFile.exists()) {
			this.properties = new HashMap<>(PropertyFileUtils.loadKeyValues(propertyFile));
		} else {
			this.properties = new HashMap<>();
		}
	}

	public boolean isMarkedForReindexing() {
		return "true".equals(properties.get(MARKED_FOR_REINDEXING));
	}

	public void setMarkedForReindexing(boolean value) {
		setProperty(MARKED_FOR_REINDEXING, value ? "true" : "false");
	}

	public boolean isMarkedForCacheRebuild() {
		return "true".equals(properties.get(MARKED_FOR_CACHE_REBUILD));
	}

	public void setLocalCacheVersion(String version) {
		setProperty(LOCAL_CACHE_VERSION, version);
	}

	public boolean isCacheRebuildRequired() {
		return systemGlobalConfigsManager.getExpectedLocalCacheVersion() != null
			   && !systemGlobalConfigsManager.getExpectedLocalCacheVersion().equals(properties.get(LOCAL_CACHE_VERSION));
	}

	public void setMarkedForCacheRebuild(boolean value) {
		setProperty(MARKED_FOR_CACHE_REBUILD, value ? "true" : "false");
	}

	public boolean isFailSafeModeEnabled() {
		return "true".equals(properties.get(FAIL_SAFE_MODE_ENABLED));
	}

	public void setFailSafeModeEnabled(boolean value) {
		setProperty(FAIL_SAFE_MODE_ENABLED, value ? "true" : "false");
	}

	public boolean isRestartRequired() {
		return "true".equals(properties.get(RESTART_REQUIRED));
	}

	public void setRestartRequired(boolean value) {
		setProperty(RESTART_REQUIRED, value ? "true" : "false");
	}

	private synchronized void setProperty(final String key, final String value) {
		properties.put(key, value);
		PropertyFileUtils.writeMap(propertyFile, properties);
	}

	@Override
	public void close() {

	}

	public void markLocalCacheAsRebuilt() {
		setLocalCacheVersion(systemGlobalConfigsManager.getExpectedLocalCacheVersion());

	}
}
