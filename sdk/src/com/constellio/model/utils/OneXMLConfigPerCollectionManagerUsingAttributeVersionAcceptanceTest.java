package com.constellio.model.utils;

import com.constellio.data.dao.managers.config.ConfigManager;
import com.constellio.data.dao.managers.config.ConfigManagerException;
import com.constellio.data.dao.managers.config.ConfigManagerException.OptimisticLockingConfiguration;
import com.constellio.data.dao.managers.config.DocumentAlteration;
import com.constellio.data.dao.managers.config.FileSystemConfigManager;
import com.constellio.data.dao.services.cache.ConstellioCache;
import com.constellio.data.dao.services.cache.ConstellioCacheOptions;
import com.constellio.data.dao.services.cache.serialization.SerializationCheckCache;
import com.constellio.data.dao.services.factories.DataLayerFactory;
import com.constellio.data.dao.services.idGenerator.UUIDV1Generator;
import com.constellio.data.events.EventBus;
import com.constellio.data.extensions.DataLayerExtensions;
import com.constellio.data.extensions.DataLayerSystemExtensions;
import com.constellio.data.io.services.facades.IOServices;
import com.constellio.data.utils.hashing.HashingService;
import com.constellio.model.services.collections.CollectionsListManager;
import com.constellio.model.services.factories.ModelLayerFactory;
import com.constellio.sdk.tests.ConstellioTest;
import org.apache.commons.io.FileUtils;
import org.jdom2.Document;
import org.jdom2.Element;
import org.jdom2.output.Format;
import org.jdom2.output.XMLOutputter;
import org.junit.Assume;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;

import java.io.File;
import java.io.Serializable;
import java.util.List;

import static com.constellio.data.conf.HashingEncoding.BASE64;
import static java.util.Arrays.asList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.fail;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class OneXMLConfigPerCollectionManagerUsingAttributeVersionAcceptanceTest extends ConstellioTest {

	int idGenerator = 0;
	File collection1ZeConfigFile;
	String filePath = "/subFolder/zeConfig.xml";

	XMLConfigReader<Data> configReader;
	OneXMLConfigPerCollectionManager<Data> manager;
	ConfigManager configManager;
	DocumentAlteration createEmptyFileDocumentAlteration;
	CollectionsListManager collectionsListManager;
	@Mock OneXMLConfigPerCollectionManagerListener managerListener, otherManagerListener;
	@Mock DataLayerExtensions dataLayerExtensions;
	@Mock DataLayerSystemExtensions dataLayerSystemExtensions;
	@Mock EventBus eventBus;
	List<String> languages = asList("fr");
	SerializationCheckCache cache;

	@Before
	public void setUp()
			throws Exception {

		when(dataLayerExtensions.getSystemWideExtensions()).thenReturn(dataLayerSystemExtensions);
		cache = new SerializationCheckCache("zeCache", new ConstellioCacheOptions());

		configReader = new XMLConfigReader<Data>() {
			@Override
			public Data read(String collection, Document document) {
				Data data = new Data();
				data.value = document.getRootElement().getAttributeValue("value");
				data.version = document.getRootElement().getAttributeValue("version");
				return data;
			}
		};

		createEmptyFileDocumentAlteration = new DocumentAlteration() {

			@Override
			public void alter(Document document) {
				Element element = new Element("root");
				element.setAttribute("value", "A");
				element.setAttribute("version", "1");
				document.addContent(element);
			}
		};

		IOServices ioServices = getIOLayerFactory().newIOServices();
		HashingService hashingServices = getIOLayerFactory().newHashingService(BASE64);
		ConstellioCache cache = new SerializationCheckCache("zeCache", new ConstellioCacheOptions());

		File tempFolder = newTempFolder();
		configManager = new FileSystemConfigManager(tempFolder, ioServices, hashingServices, cache, dataLayerExtensions,
				eventBus);
		collection1ZeConfigFile = new File(tempFolder,
				"collection1" + File.separator + "subFolder" + File.separator + "zeConfig.xml");

		ModelLayerFactory modelLayerFactory = mock(ModelLayerFactory.class);
		DataLayerFactory dataLayerFactory = mock(DataLayerFactory.class);
		when(modelLayerFactory.getDataLayerFactory()).thenReturn(dataLayerFactory);
		when(dataLayerFactory.getConfigManager()).thenReturn(configManager);

		collectionsListManager = new CollectionsListManager(modelLayerFactory);
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

		assertThat(manager.get("collection1").value).isEqualTo("A");
		assertThat(manager.get("collection2").value).isEqualTo("A");
	}

	@Test
	public void whenInstanciatedThenLoadValues()
			throws Exception {

		createCollectionFile("collection1");
		createCollectionFile("collection2");
		collectionsListManager.addCollection("collection1", languages, (byte) 42);
		collectionsListManager.addCollection("collection2", languages, (byte) 68);

		manager = newManager(managerListener);
		assertThat(manager.get("collection1").value).isEqualTo("A");
		assertThat(manager.get("collection2").value).isEqualTo("A");
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

		assertThat(manager.get("collection1").value).isEqualTo("B");
		assertThat(manager.get("collection2").value).isEqualTo("C");
	}

	@Test
	public void facegivenModifiedValuesWhenInstanciatedThenLoadModifiedValues()
			throws Exception {

		createCollectionFile("collection1");
		createCollectionFile("collection2");
		collectionsListManager.addCollection("collection1", languages, (byte) 42);
		collectionsListManager.addCollection("collection2", languages, (byte) 68);

		updateCollectionValue("collection1", "B");
		updateCollectionValue("collection2", "C");

		manager = newManager(managerListener);
		assertThat(manager.get("collection1").value).isEqualTo("B");
		assertThat(manager.get("collection2").value).isEqualTo("C");
	}

	@Test
	public void givenModifiedValuesAndCacheUpdateMissedWhenInstanciatedThenLoadModifiedValues()
			throws Exception {

		Assume.assumeTrue(configManager instanceof FileSystemConfigManager);

		createCollectionFile("collection1");
		createCollectionFile("collection2");
		collectionsListManager.addCollection("collection1", languages, (byte) 42);
		collectionsListManager.addCollection("collection2", languages, (byte) 68);

		String versionBefore = manager.get("collection1").version;

		XMLOutputter xmlOutput = new XMLOutputter(Format.getPrettyFormat());
		FileUtils.write(collection1ZeConfigFile, xmlOutput.outputString(createDocument("Z")));

		//		configManager.updateXML(manager.getConfigPath("collection1"), new DocumentAlteration() {
		//			@Override
		//			public void alter(Document document) {
		//				document.getRootElement().setAttribute("value", "Z");
		//				document.getRootElement().setAttribute("version", UUIDV1Generator.newRandomId());
		//			}
		//		});
		assertThat(manager.get("collection1").version).isEqualTo(versionBefore);
		assertThat(manager.get("collection1").value).isNotEqualTo("Z");

		try {
			updateCollectionValue("collection1", "B");
			fail("Optimistic locking expected");
		} catch (ConfigManagerException.OptimisticLockingConfiguration e) {

		}

		assertThat(manager.get("collection1").version).isNotEqualTo(versionBefore);
		assertThat(manager.get("collection1").value).isEqualTo("Z");

		//Retrying
		updateCollectionValue("collection1", "B");

		assertThat(manager.get("collection1").value).isEqualTo("B");
	}

	@Test
	public void givenCollectionCreatedByOtherManagerThenLoadedAnyway()
			throws Exception {

		OneXMLConfigPerCollectionManager<Data> otherManager = newManager(otherManagerListener);
		createCollectionFile("collection1");
		createCollectionFile("collection2");
		collectionsListManager.addCollection("collection1", languages, (byte) 42);
		collectionsListManager.addCollection("collection2", languages, (byte) 68);

		assertThat(otherManager.get("collection1").value).isEqualTo("A");
		assertThat(otherManager.get("collection2").value).isEqualTo("A");
	}

	@Test
	public void givenValuesModifiedByOtherManagerThenLoadedAnyway()
			throws Exception {

		OneXMLConfigPerCollectionManager<Data> otherManager = newManager(otherManagerListener);
		createCollectionFile("collection1");
		createCollectionFile("collection2");
		collectionsListManager.addCollection("collection1", languages, (byte) 42);
		collectionsListManager.addCollection("collection2", languages, (byte) 68);

		updateCollectionValue("collection1", "B");
		updateCollectionValue("collection2", "C");

		assertThat(otherManager.get("collection1").value).isEqualTo("B");
		assertThat(otherManager.get("collection2").value).isEqualTo("C");
	}

	@Test
	public void givenValuesModifiedThenAllListenersOfManagersAreNotified()
			throws Exception {

		OneXMLConfigPerCollectionManager<Data> otherManager = newManager(otherManagerListener);

		verify(managerListener, never()).onValueModified(anyString(), anyString());

		verify(otherManagerListener, never()).onValueModified(anyString(), anyString());

		createCollectionFile("collection1");
		createCollectionFile("collection2");
		collectionsListManager.addCollection("collection1", languages, (byte) 42);
		collectionsListManager.addCollection("collection2", languages, (byte) 68);

		updateCollectionValue("collection1", "B");
		updateCollectionValue("collection2", "C");

		ArgumentCaptor<Data> dataArgumentCaptor = ArgumentCaptor.forClass(Data.class);

		verify(managerListener).onValueModified(eq("collection1"), dataArgumentCaptor.capture());
		verify(managerListener).onValueModified(eq("collection2"), dataArgumentCaptor.capture());

		verify(otherManagerListener).onValueModified(eq("collection1"), dataArgumentCaptor.capture());
		verify(otherManagerListener).onValueModified(eq("collection2"), dataArgumentCaptor.capture());

		assertThat(dataArgumentCaptor.getAllValues()).extracting("value").isEqualTo(asList("B", "C", "B", "C"));
	}

	private void createCollectionFile(String collection) {
		manager.createCollectionFile(collection, createEmptyFileDocumentAlteration);
	}

	private OneXMLConfigPerCollectionManager<Data> newManager(OneXMLConfigPerCollectionManagerListener listener) {
		return new OneXMLConfigPerCollectionManager(configManager, collectionsListManager, filePath, configReader, listener,
				cache);
	}

	private void updateCollectionValue(String collection, final String newValue)
			throws OptimisticLockingConfiguration {
		Data data = manager.get(collection);
		manager.update(collection, data.version, createDocument(newValue));
	}

	private Document createDocument(String value) {
		Document document = new Document();
		document.setRootElement(new Element("root"));
		document.getRootElement().setAttribute("version", UUIDV1Generator.newRandomId());
		document.getRootElement().setAttribute("value", value);
		return document;
	}

	private static class Data implements Serializable {
		String version;

		String value;
	}
}
