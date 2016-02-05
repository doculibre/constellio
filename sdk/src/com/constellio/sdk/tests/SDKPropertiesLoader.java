package com.constellio.sdk.tests;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

import com.constellio.data.utils.PropertyFileUtils;

public class SDKPropertiesLoader {

	boolean locked;

	Map<String, String> sdkProperties;

	Map<String, String> getSDKProperties() {
		if (locked) {
			return new HashMap<String, String>();
		}
		if (sdkProperties == null) {
			sdkProperties = loadSDKProperties();
		}
		return sdkProperties;
	}

	private Map<String, String> loadSDKProperties() {
		File sdkProperties = new SDKFoldersLocator().getSDKProperties();

		if (!sdkProperties.exists()) {
			throw new RuntimeException("'" + sdkProperties.getAbsolutePath() + "' does not exist in project 'sdk'.");
		}

		return PropertyFileUtils.loadKeyValues(sdkProperties);
	}

	public void setLocked(boolean locked) {
		this.locked = locked;
	}
}
