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

import com.constellio.app.ui.entities.MetadataVO;
import com.constellio.app.ui.framework.builders.MetadataToVOBuilder;
import com.constellio.app.ui.pages.base.SessionContext;
import com.constellio.model.services.schemas.MetadataSchemasManager;
import com.constellio.sdk.tests.ConstellioTest;
import com.constellio.sdk.tests.FakeSessionContext;
import com.constellio.sdk.tests.MockedFactories;

public class MetadataVODataProviderTest extends ConstellioTest {

	MockedFactories mockedFactories = new MockedFactories();
	MetadataVODataProvider dataProvider;
	@Mock MetadataToVOBuilder voBuilder;
	@Mock SessionContext sessionContext;
	@Mock MetadataVO metadataVO1, metadataVO2, metadataVO3, metadataVO4;
	@Mock MetadataSchemasManager metadataSchemasManager;

	@Before
	public void setUp()
			throws Exception {

		List<MetadataVO> metadataVOs = new ArrayList<>();
		metadataVOs.add(metadataVO1);
		metadataVOs.add(metadataVO2);
		metadataVOs.add(metadataVO3);
		metadataVOs.add(metadataVO4);

		sessionContext = FakeSessionContext.dakotaInCollection(zeCollection);
		when(mockedFactories.getModelLayerFactory().getMetadataSchemasManager()).thenReturn(metadataSchemasManager);

		dataProvider = spy(
				new MetadataVODataProvider(voBuilder, mockedFactories.getModelLayerFactory(), zeCollection, "folder_default"));

		doReturn(metadataVOs).when(dataProvider).listMetadataVO();

	}

	@Test
	public void whenSubListSchemaVOThenOk()
			throws Exception {

		assertThat(dataProvider.listMetadataVO(2, 1)).containsOnly(metadataVO3);
		assertThat(dataProvider.listMetadataVO(1, 10)).containsOnly(metadataVO2, metadataVO3, metadataVO4);
		assertThat(dataProvider.listMetadataVO(11, 10)).isEmpty();

	}
}