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
package com.constellio.app.ui.framework.data;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;

import com.constellio.app.services.collections.CollectionsManager;
import com.constellio.app.ui.framework.data.CollectionVODataProvider.CollectionVO;
import com.constellio.app.ui.pages.base.SessionContext;
import com.constellio.sdk.tests.ConstellioTest;
import com.constellio.sdk.tests.FakeSessionContext;
import com.constellio.sdk.tests.MockedFactories;

public class CollectionVODataProviderTest extends ConstellioTest {

	MockedFactories mockedFactories = new MockedFactories();
	CollectionVODataProvider dataProvider;
	@Mock SessionContext sessionContext;
	@Mock CollectionVO collectionVO1, collectionVO2, collectionVO3, collectionVO4;
	@Mock CollectionsManager collectionsManager;

	@Before
	public void setUp()
			throws Exception {

		List<CollectionVO> collectionVOs = new ArrayList<>();
		collectionVOs.add(collectionVO1);
		collectionVOs.add(collectionVO2);
		collectionVOs.add(collectionVO3);
		collectionVOs.add(collectionVO4);

		sessionContext = FakeSessionContext.dakotaInCollection(zeCollection);
		when(mockedFactories.getAppLayerFactory().getCollectionsManager()).thenReturn(collectionsManager);

		dataProvider = spy(new CollectionVODataProvider(mockedFactories.getAppLayerFactory()));

		doReturn(collectionVOs).when(dataProvider).getCollections();

	}

	@Test
	public void whenSubListSchemaVOThenOk()
			throws Exception {

		assertThat(dataProvider.getCollections(2, 1)).containsOnly(collectionVO3);
		assertThat(dataProvider.getCollections(1, 10)).containsOnly(collectionVO2, collectionVO3, collectionVO4);
		assertThat(dataProvider.getCollections(11, 10)).isEmpty();

	}

}