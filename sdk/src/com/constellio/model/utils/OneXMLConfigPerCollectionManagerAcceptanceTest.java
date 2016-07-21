package com.constellio.model.utils;

import static com.constellio.data.conf.HashingEncoding.BASE64;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

import java.util.Arrays;
import java.util.List;

import org.jdom2.Document;
import org.jdom2.Element;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;

import com.constellio.data.dao.managers.config.ConfigManager;
import com.constellio.data.dao.managers.config.DocumentAlteration;
import com.constellio.data.dao.managers.config.FileSystemConfigManager;
import com.constellio.data.io.services.facades.IOServices;
import com.constellio.data.utils.hashing.HashingService;
import com.constellio.model.services.collections.CollectionsListManager;
import com.constellio.sdk.tests.ConstellioTest;

public class OneXMLConfigPerCollectionManagerAcceptanceTest extends ConstellioTest {

	String filePath = "/subFolder/zeConfig.xml";

	XMLConfigReader<String> configReader;
	OneXMLConfigPerCollectionManager<String> manager;
	ConfigManager configManager;
	DocumentAlteration createEmptyFileDocumentAlteration;
	CollectionsListManager collectionsListManager;
	@Mock OneXMLConfigPerCollectionManagerListener managerListener, otherManagerListener;
	List<String> languages = Arrays.asList("fr");

	@Before
	public void setUp()
			throws Exception {

		configReader = new XMLConfigReader<String>() {
			@Override
			public String read(String collection, Document document) {
				return document.getRootElement().getAttributeValue("value");
			}
		};

		createEmptyFileDocumentAlteration = new DocumentAlteration() {

			@Override
			public void alter(Document document) {
				Element element = new Element("root");
				element.setAttribute("value", "A");
				document.addContent(element);
			}
		};

		IOServices ioServices = getIOLayerFactory().newIOServices();
		HashingService hashingServices = getIOLayerFactory().newHashingService(BASE64);
		configManager = new FileSystemConfigManager(newTempFolder(), ioServices, hashingServices);

		collectionsListManager = new CollectionsListManager(configManager);
		collectionsListManager.initialize();

		manager = newManager(managerListener);
	}

	@Test
	public void whenCollectionsAddedThenLoadValues()
			throws Exception {

		createCollectionFile("collection1");
		createCollectionFile("collection2");
		collectionsListManager.addCollection("collection1", languages);
		collectionsListManager.addCollection("collection2", languages);

		assertThat(manager.get("collection1")).isEqualTo("A");
		assertThat(manager.get("collection2")).isEqualTo("A");
	}

	@Test
	public void whenInstanciatedThenLoadValues()
			throws Exception {

		createCollectionFile("collection1");
		createCollectionFile("collection2");
		collectionsListManager.addCollection("collection1", languages);
		collectionsListManager.addCollection("collection2", languages);

		manager = newManager(managerListener);
		assertThat(manager.get("collection1")).isEqualTo("A");
		assertThat(manager.get("collection2")).isEqualTo("A");
	}

	@Test
	public void whenValuesModifiedThenLoaded()
			throws Exception {

		createCollectionFile("collection1");
		createCollectionFile("collection2");
		collectionsListManager.addCollection("collection1", languages);
		collectionsListManager.addCollection("collection2", languages);

		updateCollectionValue("collection1", "B");
		updateCollectionValue("collection2", "C");

		assertThat(manager.get("collection1")).isEqualTo("B");
		assertThat(manager.get("collection2")).isEqualTo("C");
	}

	@Test
	public void givenModifiedValuesWhenInstanciatedThenLoadModifiedValues()
			throws Exception {

		createCollectionFile("collection1");
		createCollectionFile("collection2");
		collectionsListManager.addCollection("collection1", languages);
		collectionsListManager.addCollection("collection2", languages);

		updateCollectionValue("collection1", "B");
		updateCollectionValue("collection2", "C");

		manager = newManager(managerListener);
		assertThat(manager.get("collection1")).isEqualTo("B");
		assertThat(manager.get("collection2")).isEqualTo("C");
	}

	@Test
	public void givenCollectionCreatedByOtherManagerThenLoadedAnyway()
			throws Exception {

		OneXMLConfigPerCollectionManager<String> otherManager = newManager(otherManagerListener);
		createCollectionFile("collection1");
		createCollectionFile("collection2");
		collectionsListManager.addCollection("collection1", languages);
		collectionsListManager.addCollection("collection2", languages);

		assertThat(otherManager.get("collection1")).isEqualTo("A");
		assertThat(otherManager.get("collection2")).isEqualTo("A");
	}

	@Test
	public void givenValuesModifiedByOtherManagerThenLoadedAnyway()
			throws Exception {

		OneXMLConfigPerCollectionManager<String> otherManager = newManager(otherManagerListener);
		createCollectionFile("collection1");
		createCollectionFile("collection2");
		collectionsListManager.addCollection("collection1", languages);
		collectionsListManager.addCollection("collection2", languages);

		updateCollectionValue("collection1", "B");
		updateCollectionValue("collection2", "C");

		assertThat(otherManager.get("collection1")).isEqualTo("B");
		assertThat(otherManager.get("collection2")).isEqualTo("C");
	}

	@Test
	public void givenValuesModifiedThenAllListenersOfManagersAreNotified()
			throws Exception {

		OneXMLConfigPerCollectionManager<String> otherManager = newManager(otherManagerListener);

		verify(managerListener, never()).onValueModified(anyString(), anyString());

		verify(otherManagerListener, never()).onValueModified(anyString(), anyString());

		createCollectionFile("collection1");
		createCollectionFile("collection2");
		collectionsListManager.addCollection("collection1", languages);
		collectionsListManager.addCollection("collection2", languages);

		updateCollectionValue("collection1", "B");
		updateCollectionValue("collection2", "C");

		verify(managerListener).onValueModified("collection1", "B");
		verify(managerListener).onValueModified("collection2", "C");

		verify(otherManagerListener).onValueModified("collection1", "B");
		verify(otherManagerListener).onValueModified("collection2", "C");
	}

	private void createCollectionFile(String collection) {
		manager.createCollectionFile(collection, createEmptyFileDocumentAlteration);
	}

	private OneXMLConfigPerCollectionManager<String> newManager(OneXMLConfigPerCollectionManagerListener listener) {
		return new OneXMLConfigPerCollectionManager(configManager, collectionsListManager, filePath, configReader, listener);
	}

	private void updateCollectionValue(String collection, final String newValue) {
		manager.updateXML(collection, new DocumentAlteration() {
			@Override
			public void alter(Document document) {
				document.getRootElement().setAttribute("value", newValue);
			}
		});
	}
}
