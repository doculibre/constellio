package com.constellio.model.services.collections;

import com.constellio.app.services.extensions.plugins.ConstellioPluginManager;
import com.constellio.model.services.collections.exceptions.NoMoreCollectionAvalibleException;
import com.constellio.sdk.tests.ConstellioTest;
import org.junit.Before;
import org.junit.Test;

import static com.constellio.model.entities.records.wrappers.Collection.SYSTEM_COLLECTION;
import static java.util.Arrays.asList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.fail;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

public class CollectionsListManagerAcceptanceTest extends ConstellioTest {

	public static final String RE_ADDED_COLLECTION_NAME = "reAddedCollection";
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

	@Test(expected = NoMoreCollectionAvalibleException.class)
	public void whenAdd256CollectionThenThrow() throws NoMoreCollectionAvalibleException {
		cacheIntegrityCheckedAfterTest = false;
		for (int i = 0; i < 256; i++) {
			String code = "collection" + i;
			byte collectionId = collectionsListManager.registerPendingCollectionInfo(code, "fr", asList("fr"));
			collectionsListManager.addCollection(code, asList("fr"), collectionId);
		}
	}

	@Test()
	public void whenAdd255CollectionRemoveOneAndAddAgainThenNextCollectionTakeRemovedCollectionId()
			throws NoMoreCollectionAvalibleException {
		cacheIntegrityCheckedAfterTest = false;
		for (int i = 0; i < 255; i++) {
			String code = "collection" + i;
			byte collectionId = collectionsListManager.registerPendingCollectionInfo(code, "fr", asList("fr"));
			collectionsListManager.addCollection(code, asList("fr"), collectionId);
		}

		try {

			byte collectionId = collectionsListManager.registerPendingCollectionInfo("collectionShouldNotBeAdded", "fr", asList("fr"));
			fail("This add should fail since there is not more space for an other collection.");
		} catch (NoMoreCollectionAvalibleException e) {
			// Ok
		}

		// Remove the first collection after _system_
		collectionsListManager.remove("collection" + 42);

		byte collectionId = collectionsListManager.registerPendingCollectionInfo(RE_ADDED_COLLECTION_NAME, "fr", asList("fr"));
		collectionsListManager.addCollection(RE_ADDED_COLLECTION_NAME, asList("fr"), collectionId);

		assertThat(collectionsListManager.getCollectionId(RE_ADDED_COLLECTION_NAME)).isEqualTo(collectionId);
	}

	@Test
	public void whenAddCollectionsThenNotifyListeners()
			throws Exception {

		CollectionsListManagerListener listener1 = mock(CollectionsListManagerListener.class);
		CollectionsListManagerListener listener2 = mock(CollectionsListManagerListener.class);

		collectionsListManager.registerCollectionsListener(listener1);
		collectionsListManager.registerCollectionsListener(listener2);

		givenSpecialCollection("zeUltimateCollection", asList("fr"));

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
