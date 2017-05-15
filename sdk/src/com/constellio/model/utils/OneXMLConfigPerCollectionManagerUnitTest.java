package com.constellio.model.utils;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.Arrays;

import org.jdom2.Document;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;

import com.constellio.data.dao.managers.config.ConfigManager;
import com.constellio.data.dao.managers.config.values.XMLConfiguration;
import com.constellio.data.dao.services.cache.serialization.SerializationCheckCache;
import com.constellio.model.services.collections.CollectionsListManager;
import com.constellio.sdk.tests.ConstellioTest;

public class OneXMLConfigPerCollectionManagerUnitTest extends ConstellioTest {

	String aValue = aString();
	String aNewValue = aString();
	String anotherValue = aString();

	String collectionFolderRelativeConfigPath = "/subfolder/zeConfig.xml";
	@Mock XMLConfigReader<String> configReader;

	@Mock ConfigManager configManager;
	@Mock CollectionsListManager collectionsListManager;
	OneXMLConfigPerCollectionManager manager;

	@Mock OneXMLConfigPerCollectionManagerListener listener;
	
	SerializationCheckCache cache;

	@Before
	public void setUp()
			throws Exception {

		cache = new SerializationCheckCache("zeCache");
		when(collectionsListManager.getCollections()).thenReturn(new ArrayList<String>());
		manager = newManager();

	}

	@Test
	public void whenGetConfigPathWithCollectionConfigStartingWithSlashThenOk()
			throws Exception {

		assertThat(manager.getConfigPath("zeUltimateCollection")).isEqualTo("/zeUltimateCollection/subfolder/zeConfig.xml");

	}

	@Test
	public void whenGetConfigPathWithCollectionConfigStartingWithoutSlashThenOk()
			throws Exception {

		manager = new OneXMLConfigPerCollectionManager(
				configManager, collectionsListManager, "subFolder/zeConfig.xml", configReader, listener, cache);
		assertThat(manager.getConfigPath("zeUltimateCollection")).isEqualTo("/zeUltimateCollection/subFolder/zeConfig.xml");

	}

	@Test
	public void whenGetCollectionCodeThenOk()
			throws Exception {

		assertThat(manager.getCollectionCode("/zeUltimateCollection/subFolder/zeConfig.xml")).isEqualTo("zeUltimateCollection");

	}

	@Test
	public void whenInstanciatedThenRegisterForAllCurrentCollectionsAndFutureCollectionsThenReadAllCollectionValues()
			throws Exception {

		when(collectionsListManager.getCollections()).thenReturn(Arrays.asList("collection1", "collection2"));

		XMLConfiguration collection1XMLConfiguration = mock(XMLConfiguration.class, "collection1XMLConfiguration");
		XMLConfiguration collection2XMLConfiguration = mock(XMLConfiguration.class, "collection2XMLConfiguration");
		Document collection1Document = mock(Document.class, "collection1Document");
		Document collection2Document = mock(Document.class, "collection2Document");
		when(collection1XMLConfiguration.getDocument()).thenReturn(collection1Document);
		when(collection2XMLConfiguration.getDocument()).thenReturn(collection2Document);
		when(configManager.getXML("/collection1/subfolder/zeConfig.xml")).thenReturn(collection1XMLConfiguration);
		when(configManager.getXML("/collection2/subfolder/zeConfig.xml")).thenReturn(collection2XMLConfiguration);
		when(configReader.read("collection1", collection1Document)).thenReturn(aValue);
		when(configReader.read("collection2", collection2Document)).thenReturn(anotherValue);

		manager = newManager();
		verify(configManager).registerListener("/collection1/subfolder/zeConfig.xml", manager);
		verify(configManager).registerListener("/collection2/subfolder/zeConfig.xml", manager);
		verify(collectionsListManager).registerCollectionsListener(manager);
		assertThat(manager.get("collection1")).isEqualTo(aValue);
		assertThat(manager.get("collection2")).isEqualTo(anotherValue);

	}

	@Test
	public void whenReceiveNewCollectionUpdateThenListenXMLFileAndReadIt()
			throws Exception {

		XMLConfiguration collection3XMLConfiguration = mock(XMLConfiguration.class, "collection3XMLConfiguration");
		Document collection3Document = mock(Document.class, "collection3Document");
		when(collection3XMLConfiguration.getDocument()).thenReturn(collection3Document);
		when(configManager.getXML("/collection3/subfolder/zeConfig.xml")).thenReturn(collection3XMLConfiguration);
		when(configReader.read("collection3", collection3Document)).thenReturn(aValue);

		manager.onCollectionCreated("collection3");

		assertThat(manager.get("collection3")).isEqualTo(aValue);

	}

	@Test
	public void whenReceiveConfigUpdateNotificationThenReloadIt()
			throws Exception {

		when(collectionsListManager.getCollections()).thenReturn(Arrays.asList("collection3"));
		XMLConfiguration collection3XMLConfiguration = mock(XMLConfiguration.class, "collection3XMLConfiguration");
		Document collection3Document = mock(Document.class, "collection3Document");
		Document collection3UpdatedDocument = mock(Document.class, "collection3UpdatedDocument");
		when(collection3XMLConfiguration.getDocument()).thenReturn(collection3Document).thenReturn(collection3UpdatedDocument);
		when(configManager.getXML("/collection3/subfolder/zeConfig.xml")).thenReturn(collection3XMLConfiguration);
		when(configReader.read("collection3", collection3Document)).thenReturn(aValue);
		when(configReader.read("collection3", collection3UpdatedDocument)).thenReturn(aNewValue);

		manager = newManager();
		manager.onConfigUpdated("/collection3/subfolder/zeConfig.xml");

		verify(listener).onValueModified("collection3", aNewValue);
		assertThat(manager.get("collection3")).isEqualTo(aNewValue);

	}

	private OneXMLConfigPerCollectionManager newManager() {
		return new OneXMLConfigPerCollectionManager(configManager, collectionsListManager,
				collectionFolderRelativeConfigPath, configReader, listener, cache);
	}
}
