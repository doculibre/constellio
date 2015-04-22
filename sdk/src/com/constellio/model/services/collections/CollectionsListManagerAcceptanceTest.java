/*Constellio Enterprise Information Management

Copyright (c) 2015 "Constellio inc."

This program is free software: you can redistribute it and/or modify
it under the terms of the GNU Affero General Public License as
published by the Free Software Foundation, either version 3 of the
License, or (at your option) any later version.

This program is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
GNU Affero General Public License for more details.

You should have received a copy of the GNU Affero General Public License
along with this program. If not, see <http://www.gnu.org/licenses/>.
*/
package com.constellio.model.services.collections;

import static java.util.Arrays.asList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

import org.junit.Before;
import org.junit.Test;

import com.constellio.app.services.extensions.ConstellioPluginManager;
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

		givenCollection("zeUltimateCollection");

		verify(listener1).onCollectionCreated("zeUltimateCollection");
		verify(listener2).onCollectionCreated("zeUltimateCollection");

		givenCollection("anotherCollection", asList("fr", "en"));

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

		givenCollection("zeUltimateCollection1");
		givenCollection("zeUltimateCollection2");

		assertThat(collectionsListManager.getCollections()).containsOnly("zeUltimateCollection1", "zeUltimateCollection2");
	}

	@Test
	public void givenCollectionsWhenRemoveCollectionThenRemoveFromCollectionsList()
			throws Exception {

		givenCollection("zeUltimateCollection1");
		givenCollection("zeUltimateCollection2");

		collectionsListManager.remove("zeUltimateCollection1");

		assertThat(collectionsListManager.getCollections()).containsOnly("zeUltimateCollection2");
	}

}
