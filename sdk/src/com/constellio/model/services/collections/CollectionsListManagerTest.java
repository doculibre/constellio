package com.constellio.model.services.collections;

import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Arrays;
import java.util.List;

import org.junit.Test;
import org.mockito.InOrder;
import org.mockito.Mock;

import com.constellio.data.dao.managers.config.ConfigManager;
import com.constellio.data.dao.managers.config.DocumentAlteration;
import com.constellio.sdk.tests.ConstellioTest;

public class CollectionsListManagerTest extends ConstellioTest {

	@Mock ConfigManager configManager;
	private CollectionsListManager collectionsListManager;

	@Test
	public void whenCreateCollectionListenerThenRegisterFileListenerInConfigManager()
			throws Exception {

		when(configManager.exist("/collections.xml")).thenReturn(true);

		collectionsListManager = spy(new CollectionsListManager(configManager) {
			@Override
			public List<String> readCollections() {
				return Arrays.asList("collection1", "collection2");
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

		collectionsListManager = spy(new CollectionsListManager(configManager) {
			@Override
			public List<String> readCollections() {
				return Arrays.asList("collection1", "collection2");
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
