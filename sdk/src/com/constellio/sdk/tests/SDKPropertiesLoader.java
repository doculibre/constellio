package com.constellio.sdk.tests;

import com.constellio.data.utils.PropertyFileUtils;
import com.constellio.data.utils.TenantUtils;
import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Map;
import java.util.Properties;
import java.util.stream.Stream;

import static org.apache.commons.io.IOUtils.closeQuietly;

public class SDKPropertiesLoader {

	boolean locked;

	Map<String, String> sdkProperties;

	Map<String, String> getSDKProperties() {
		//		if (locked) {
		//			return new HashMap<String, String>();
		//		}
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
			copyDefaultSdkProperties(sdkProperties);
		}

		return PropertyFileUtils.loadKeyValues(sdkProperties);
	}

	public void createTenantsSDKProperties() {
		Stream.of(1, 2).forEach(tenantId -> {
			TenantUtils.setTenant(tenantId);
			File tenantSdkProperties = new SDKFoldersLocator().getSDKProperties();
			if (!tenantSdkProperties.exists()) {
				copyDefaultSdkProperties(tenantSdkProperties);
			}
			TenantUtils.setTenant(null);
		});
	}

	private void copyDefaultSdkProperties(File targetSdkProperties) {
		try {
			File sdkProperties = "2".equals(TenantUtils.getTenantId()) ?
								 new SDKFoldersLocator().getTenant2SdkProperties() :
								 new SDKFoldersLocator().getDefaultSdkProperties();
			FileUtils.copyFile(sdkProperties, targetSdkProperties);
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

	public void writeValues(Map<String, String> values) {
		File sdkProperties = new SDKFoldersLocator().getSDKProperties();
		Properties prop = new Properties();

		FileReader reader = null;
		FileWriter writer = null;
		try {
			reader = new FileReader(sdkProperties);
			prop.load(reader); // FileInputStream or whatever
			closeQuietly(reader);

			for (Map.Entry<String, String> entry : values.entrySet()) {
				prop.setProperty(entry.getKey(), entry.getValue());
			}
			writer = new FileWriter(sdkProperties);

			prop.store(writer, "");

		} catch (Exception e) {
			throw new RuntimeException(e);

		} finally {
			closeQuietly(reader);
			closeQuietly(writer);
		}

	}

	public void setLocked(boolean locked) {
		this.locked = locked;
	}
}
