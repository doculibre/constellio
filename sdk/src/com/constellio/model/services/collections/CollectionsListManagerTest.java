package com.constellio.model.services.collections;

import com.constellio.data.dao.managers.config.ConfigManager;
import com.constellio.data.dao.managers.config.DocumentAlteration;
import com.constellio.data.dao.services.factories.DataLayerFactory;
import com.constellio.model.conf.ModelLayerConfiguration;
import com.constellio.model.entities.CollectionInfo;
import com.constellio.model.services.factories.ModelLayerFactory;
import com.constellio.sdk.tests.ConstellioTest;
import org.junit.Test;
import org.mockito.InOrder;
import org.mockito.Mock;

import java.util.Arrays;
import java.util.List;

import static java.util.Arrays.asList;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class CollectionsListManagerTest extends ConstellioTest {

	@Mock ConfigManager configManager;
	private CollectionsListManager collectionsListManager;

	@Mock ModelLayerConfiguration modelLayerConfiguration;
	@Mock ModelLayerFactory modelLayerFactory;
	@Mock DataLayerFactory dataLayerFactory;

	@Test
	public void whenCreateCollectionListenerThenRegisterFileListenerInConfigManager()
			throws Exception {

		when(configManager.exist("/collections.xml")).thenReturn(true);
		when(modelLayerFactory.getConfiguration()).thenReturn(modelLayerConfiguration);
		when(modelLayerFactory.getDataLayerFactory()).thenReturn(dataLayerFactory);
		when(dataLayerFactory.getConfigManager()).thenReturn(configManager);

		collectionsListManager = spy(new CollectionsListManager(modelLayerFactory) {
			@Override
			public List<String> readCollections() {
				return Arrays.asList("collection1", "collection2");
			}

			@Override
			public CollectionInfo getCollectionInfo(String collectionCode) {
				if (collectionCode.equals("collection1")) {
					return new CollectionInfo((byte) 1, collectionCode, "fr", asList("fr"));
				} else if (collectionCode.equals("collection2")) {
					return new CollectionInfo((byte) 2, collectionCode, "fr", asList("fr"));
				}

				throw new IllegalStateException("Collection for testing not supported");
			}
		});
		collectionsListManager.initialize();

		InOrder inOrder = inOrder(configManager);
		inOrder.verify(configManager).createXMLDocumentIfInexistent(eq("/collections.xml"), any(DocumentAlteration.class));
		inOrder.verify(configManager).registerListener("/collections.xml", collectionsListManager);
	}

	@Test
	public void whenCollectionFileModifiedThenReadItAndOnlySendEventsForNewCollections()
			throws Exception {

		when(configManager.exist("/collections.xml")).thenReturn(true);
		when(modelLayerFactory.getConfiguration()).thenReturn(modelLayerConfiguration);
		when(modelLayerFactory.getDataLayerFactory()).thenReturn(dataLayerFactory);
		when(dataLayerFactory.getConfigManager()).thenReturn(configManager);

		collectionsListManager = spy(new CollectionsListManager(modelLayerFactory) {
			@Override
			public List<String> readCollections() {
				return Arrays.asList("collection1", "collection2");
			}

			@Override
			public CollectionInfo getCollectionInfo(String collectionCode) {
				if (collectionCode.equals("collection1")) {
					return new CollectionInfo((byte) 1, collectionCode, "fr", asList("fr"));
				} else if (collectionCode.equals("collection2")) {
					return new CollectionInfo((byte) 2, collectionCode, "fr", asList("fr"));
				}

				throw new IllegalStateException("Collection for testing not supported");
			}
		});


		collectionsListManager.initialize();

		CollectionsListManagerListener listener1 = mock(CollectionsListManagerListener.class);
		CollectionsListManagerListener listener2 = mock(CollectionsListManagerListener.class);

		collectionsListManager.registerCollectionsListener(listener1);
		collectionsListManager.registerCollectionsListener(listener2);

		doReturn(Arrays.asList("collection1", "collection2", "collection3"))
				.when(collectionsListManager).readCollections();

		collectionsListManager.onConfigUpdated("/collections.xml");

		verify(listener1, never()).onCollectionCreated("collection1");
		verify(listener1, never()).onCollectionCreated("collection2");
		verify(listener1).onCollectionCreated("collection3");
		verify(listener2, never()).onCollectionCreated("collection1");
		verify(listener2, never()).onCollectionCreated("collection2");
		verify(listener2).onCollectionCreated("collection3");

	}
}
