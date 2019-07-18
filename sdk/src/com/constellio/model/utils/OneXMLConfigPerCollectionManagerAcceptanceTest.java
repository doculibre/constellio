package com.constellio.model.utils;

import com.constellio.data.dao.managers.config.ConfigManager;
import com.constellio.data.dao.managers.config.DocumentAlteration;
import com.constellio.data.dao.managers.config.FileSystemConfigManager;
import com.constellio.data.dao.services.cache.ConstellioCache;
import com.constellio.data.dao.services.cache.ConstellioCacheOptions;
import com.constellio.data.dao.services.cache.serialization.SerializationCheckCache;
import com.constellio.data.events.EventBus;
import com.constellio.data.extensions.DataLayerExtensions;
import com.constellio.data.extensions.DataLayerSystemExtensions;
import com.constellio.data.io.services.facades.IOServices;
import com.constellio.data.utils.hashing.HashingService;
import com.constellio.model.services.collections.CollectionsListManager;
import com.constellio.sdk.tests.ConstellioTest;
import org.jdom2.Document;
import org.jdom2.Element;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;

import java.util.Arrays;
import java.util.List;

import static com.constellio.data.conf.HashingEncoding.BASE64;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class OneXMLConfigPerCollectionManagerAcceptanceTest extends ConstellioTest {

	String filePath = "/subFolder/zeConfig.xml";

	XMLConfigReader<String> configReader;
	OneXMLConfigPerCollectionManager<String> manager;
	ConfigManager configManager;
	DocumentAlteration createEmptyFileDocumentAlteration;
	CollectionsListManager collectionsListManager;
	@Mock OneXMLConfigPerCollectionManagerListener managerListener, otherManagerListener;
	@Mock DataLayerExtensions dataLayerExtensions;
	@Mock DataLayerSystemExtensions dataLayerSystemExtensions;
	@Mock EventBus eventBus;
	List<String> languages = Arrays.asList("fr");
	SerializationCheckCache cache;

	@Before
	public void setUp()
			throws Exception {
		cacheIntegrityCheckedAfterTest = false;
		when(dataLayerExtensions.getSystemWideExtensions()).thenReturn(dataLayerSystemExtensions);
		cache = new SerializationCheckCache("zeCache", new ConstellioCacheOptions());

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
		ConstellioCache cache = new SerializationCheckCache("zeCache", new ConstellioCacheOptions());
		configManager = new FileSystemConfigManager(newTempFolder(), ioServices, hashingServices, cache, dataLayerExtensions,
				eventBus);

		collectionsListManager = new CollectionsListManager(getModelLayerFactory());
		collectionsListManager.initialize();

		manager = newManager(managerListener);

	}

	@Test
	public void whenCollectionsAddedThenLoadValues()
			throws Exception {

		createCollectionFile("collection1");
		createCollectionFile("collection2");
		collectionsListManager.addCollection("collection1", languages, (byte) 42);
		collectionsListManager.addCollection("collection2", languages, (byte) 68);

		assertThat(manager.get("collection1")).isEqualTo("A");
		assertThat(manager.get("collection2")).isEqualTo("A");
	}

	@Test
	public void whenInstanciatedThenLoadValues()
			throws Exception {

		createCollectionFile("collection1");
		createCollectionFile("collection2");
		collectionsListManager.addCollection("collection1", languages, (byte) 42);
		collectionsListManager.addCollection("collection2", languages, (byte) 68);

		manager = newManager(managerListener);
		assertThat(manager.get("collection1")).isEqualTo("A");
		assertThat(manager.get("collection2")).isEqualTo("A");
	}

	@Test
	public void whenValuesModifiedThenLoaded()
			throws Exception {

		createCollectionFile("collection1");
		createCollectionFile("collection2");
		collectionsListManager.addCollection("collection1", languages, (byte) 42);
		collectionsListManager.addCollection("collection2", languages, (byte) 68);

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
		collectionsListManager.addCollection("collection1", languages, (byte) 42);
		collectionsListManager.addCollection("collection2", languages, (byte) 68);

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
		collectionsListManager.addCollection("collection1", languages, (byte) 42);
		collectionsListManager.addCollection("collection2", languages, (byte) 68);

		assertThat(otherManager.get("collection1")).isEqualTo("A");
		assertThat(otherManager.get("collection2")).isEqualTo("A");
	}

	@Test
	public void givenValuesModifiedByOtherManagerThenLoadedAnyway()
			throws Exception {

		OneXMLConfigPerCollectionManager<String> otherManager = newManager(otherManagerListener);
		createCollectionFile("collection1");
		createCollectionFile("collection2");
		collectionsListManager.addCollection("collection1", languages, (byte) 42);
		collectionsListManager.addCollection("collection2", languages, (byte) 68);

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
		collectionsListManager.addCollection("collection1", languages, (byte) 42);
		collectionsListManager.addCollection("collection2", languages, (byte) 68);

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
		return new OneXMLConfigPerCollectionManager(configManager, collectionsListManager, filePath, configReader, listener,
				cache);
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
