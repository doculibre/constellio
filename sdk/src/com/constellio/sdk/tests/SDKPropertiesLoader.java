package com.constellio.sdk.tests;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.io.FileUtils;

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

			for (String propertyName : System.getProperties().stringPropertyNames()) {
				if (propertyName.startsWith("skip.")) {
					sdkProperties.put(propertyName, System.getProperty(propertyName));
				}
			}
		}
		return sdkProperties;
	}

	private Map<String, String> loadSDKProperties() {
		File sdkProperties = new SDKFoldersLocator().getSDKProperties();

		if (!sdkProperties.exists()) {
			File defaultSdkProperties = new File(sdkProperties.getParentFile(), "sdk.properties.default");
			try {
				FileUtils.copyFile(defaultSdkProperties, sdkProperties);
			} catch (IOException e) {
				throw new RuntimeException(e);
			}
		}

		return PropertyFileUtils.loadKeyValues(sdkProperties);
	}

	public void setLocked(boolean locked) {
		this.locked = locked;
	}
}
