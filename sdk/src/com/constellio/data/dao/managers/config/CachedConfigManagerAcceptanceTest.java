package com.constellio.data.dao.managers.config;

import com.constellio.data.dao.managers.config.events.ConfigUpdatedEventListener;
import com.constellio.sdk.tests.ConstellioTest;
import org.jdom2.Document;
import org.jdom2.Element;
import org.jetbrains.annotations.NotNull;
import org.junit.Before;
import org.junit.Test;

import java.util.HashMap;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

public class CachedConfigManagerAcceptanceTest extends ConstellioTest {

	CachedConfigManagerAcceptanceTest_ConfigEventListener serviceListeningConfigUpdate = new CachedConfigManagerAcceptanceTest_ConfigEventListener();

	ConfigManager configManager;
	ConfigManager cachedConfigManager;

	@Before
	public void setUp()
			throws Exception {

		cachedConfigManager = getDataLayerFactory().getConfigManager();
		configManager = ((CachedConfigManager) cachedConfigManager).getNestedConfigManager();
	}

	@Test
	public void givenCachedXMLConfigWhenUpdateFailedCauseByOptimisticLockingThenInvalidatedBeforeServiceReceiveException()
			throws Exception {

		assertThat(cachedConfigManager.exist("file.xml")).isFalse();
		configManager.registerListener("file.xml", serviceListeningConfigUpdate);

		cachedConfigManager.keepInCache("file.xml");
		cachedConfigManager.add("file.xml", newXMLDocumentWithValue("v1"));
		assertThat(cachedConfigManager.exist("file.xml")).isTrue();
		assertThat(cachedValueAttributeOf("file.xml")).isEqualTo("v1");

		configManager.updateXML("file.xml", modifyingXMLAttributeValueTo("v2"));
		assertThat(cachedValueAttributeOf("file.xml")).isEqualTo("v2");
		assertThat(serviceListeningConfigUpdate.lastRetrievedValue).isEqualTo("v2");

		cachedConfigManager.updateXML("file.xml", modifyingXMLAttributeValueTo("v3"));
		assertThat(cachedValueAttributeOf("file.xml")).isEqualTo("v3");
		assertThat(serviceListeningConfigUpdate.lastRetrievedValue).isEqualTo("v3");

		configManager.delete("file.xml");
		assertThat(cachedConfigManager.exist("file.xml")).isFalse();
		assertThat(cachedValueAttributeOf("file.xml")).isNull();
	}

	@Test
	public void givenCachedPropertiesConfigWhenUpdateFailedCauseByOptimisticLockingThenInvalidatedBeforeServiceReceiveException()
			throws Exception {

		assertThat(cachedConfigManager.exist("file.properties")).isFalse();
		configManager.registerListener("file.properties", serviceListeningConfigUpdate);

		cachedConfigManager.keepInCache("file.properties");
		cachedConfigManager.add("file.properties", newPropertiesWithValue("v1"));
		assertThat(cachedConfigManager.exist("file.properties")).isTrue();
		assertThat(cachedPropertyAttributeOf("file.properties")).isEqualTo("v1");

		configManager.updateProperties("file.properties", modifyingPropertyValueTo("v2"));
		assertThat(cachedPropertyAttributeOf("file.properties")).isEqualTo("v2");
		assertThat(serviceListeningConfigUpdate.lastRetrievedValue).isEqualTo("v2");

		cachedConfigManager.updateProperties("file.properties", modifyingPropertyValueTo("v3"));
		assertThat(cachedPropertyAttributeOf("file.properties")).isEqualTo("v3");
		assertThat(serviceListeningConfigUpdate.lastRetrievedValue).isEqualTo("v3");

		configManager.delete("file.properties");
		assertThat(cachedConfigManager.exist("file.properties")).isFalse();
		assertThat(cachedPropertyAttributeOf("file.properties")).isNull();

		cachedConfigManager.add("file.properties", newPropertiesWithValue("v1"));
		assertThat(cachedConfigManager.exist("file.properties")).isTrue();

	}

	@NotNull
	protected PropertiesAlteration modifyingPropertyValueTo(final String newValue) {
		return new PropertiesAlteration() {
			@Override
			public void alter(Map<String, String> document) {
				document.put("value", newValue);
			}
		};
	}

	@NotNull
	protected DocumentAlteration modifyingXMLAttributeValueTo(final String newValue) {
		return new DocumentAlteration() {
			@Override
			public void alter(Document document) {
				document.getRootElement().setAttribute("value", newValue);
			}
		};
	}

	private String cachedValueAttributeOf(String path) {
		try {
			return cachedConfigManager.getXML(path).getDocument().getRootElement().getAttributeValue("value");
		} catch (Exception E) {
			return null;
		}
	}

	private String cachedPropertyAttributeOf(String path) {
		try {
			return cachedConfigManager.getProperties(path).getProperties().get("value");
		} catch (Exception E) {
			return null;
		}
	}

	private Document newXMLDocumentWithValue(String value) {
		Document document = new Document();
		Element rootElement = new Element("rootElement");
		document.setRootElement(rootElement);
		rootElement.setAttribute("value", value);
		return document;
	}

	private Map<String, String> newPropertiesWithValue(String value) {
		Map<String, String> map = new HashMap<>();
		map.put("value", value);
		return map;
	}

	private class CachedConfigManagerAcceptanceTest_ConfigEventListener implements ConfigUpdatedEventListener {

		String lastRetrievedValue;

		@Override
		public void onConfigUpdated(String configPath) {
			if (configPath.endsWith(".xml")) {
				lastRetrievedValue = cachedValueAttributeOf(configPath);
			} else if (configPath.endsWith(".properties")) {
				lastRetrievedValue = cachedPropertyAttributeOf(configPath);
			}
		}
	}
}
