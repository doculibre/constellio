package com.constellio.model.services.collections;

import static com.constellio.model.entities.records.wrappers.Collection.SYSTEM_COLLECTION;
import static java.util.Arrays.asList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

import org.junit.Before;
import org.junit.Test;

import com.constellio.app.services.extensions.plugins.ConstellioPluginManager;
import com.constellio.sdk.tests.ConstellioTest;

public class CollectionsListManagerAcceptanceTest extends ConstellioTest {

	private ConstellioPluginManager pluginManager;
	private com.constellio.app.services.collections.CollectionsManager collectionsManager;
	private CollectionsListManager collectionsListManager;

	@Before
	public void setUp()
			throws Exception {
		pluginManager = getAppLayerFactory().getPluginManager();
		collectionsManager = getAppLayerFactory().getCollectionsManager();
		collectionsListManager = getModelLayerFactory().getCollectionsListManager();

	}

	@Test
	public void whenAddCollectionsThenNotifyListeners()
			throws Exception {

		CollectionsListManagerListener listener1 = mock(CollectionsListManagerListener.class);
		CollectionsListManagerListener listener2 = mock(CollectionsListManagerListener.class);

		collectionsListManager.registerCollectionsListener(listener1);
		collectionsListManager.registerCollectionsListener(listener2);

		givenSpecialCollection("zeUltimateCollection");

		verify(listener1).onCollectionCreated("zeUltimateCollection");
		verify(listener2).onCollectionCreated("zeUltimateCollection");

		givenSpecialCollection("anotherCollection", asList("fr", "en"));

		verify(listener1).onCollectionCreated("zeUltimateCollection");
		verify(listener2).onCollectionCreated("zeUltimateCollection");
		verify(listener1).onCollectionCreated("anotherCollection");
		verify(listener2).onCollectionCreated("anotherCollection");

		assertThat(collectionsListManager.getCollectionLanguages("zeUltimateCollection")).containsOnly("fr");
		assertThat(collectionsListManager.getCollectionLanguages("anotherCollection")).containsOnly("fr", "en");
	}

	@Test
	public void whenAddCollectionsThenInCollectionsList()
			throws Exception {

		givenSpecialCollection("zeUltimateCollection1");
		givenSpecialCollection("zeUltimateCollection2");

		assertThat(collectionsListManager.getCollections())
				.containsOnly("zeUltimateCollection1", "zeUltimateCollection2", SYSTEM_COLLECTION);
	}

	@Test
	public void givenCollectionsWhenRemoveCollectionThenRemoveFromCollectionsList()
			throws Exception {

		givenSpecialCollection("zeUltimateCollection1");
		givenSpecialCollection("zeUltimateCollection2");

		collectionsListManager.remove("zeUltimateCollection1");

		assertThat(collectionsListManager.getCollections()).containsOnly("zeUltimateCollection2", SYSTEM_COLLECTION);
	}

}
