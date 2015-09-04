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

import static com.constellio.model.services.search.query.logical.LogicalSearchQueryOperators.from;
import static java.util.Arrays.asList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;

import com.constellio.app.modules.rm.RMTestRecords;
import com.constellio.app.modules.rm.services.RMSchemasRecordsServices;
import com.constellio.app.ui.entities.MetadataSchemaVO;
import com.constellio.app.ui.entities.RecordVO;
import com.constellio.app.ui.entities.RecordVO.VIEW_MODE;
import com.constellio.app.ui.framework.builders.MetadataSchemaToVOBuilder;
import com.constellio.app.ui.framework.builders.RecordToVOBuilder;
import com.constellio.app.ui.pages.base.SessionContext;
import com.constellio.model.entities.schemas.Schemas;
import com.constellio.model.services.search.query.logical.LogicalSearchQuery;
import com.constellio.sdk.tests.ConstellioTest;

public class RecordVODataProviderAcceptTest extends ConstellioTest {

	RMTestRecords records = new RMTestRecords(zeCollection);
	RMSchemasRecordsServices rm;
	@Mock SessionContext sessionContext;

	MetadataSchemaVO schema;
	RecordToVOBuilder voBuilder;

	@Test
	public void givenDataProviderThenReturnGoodSizeOfRecords()
			throws Exception {

		LogicalSearchQuery query = new LogicalSearchQuery(from(rm.folderSchemaType())
				.where(rm.folderCategory()).isEqualTo(records.getCategory_Z112()));

		RecordVODataProvider dataProvider = newDataProvider(query);

		assertThat(dataProvider.size()).isEqualTo(5);

	}

	@Test
	public void givenDataProviderThenReturnGoodRecords()
			throws Exception {

		LogicalSearchQuery query = new LogicalSearchQuery(from(rm.folderSchemaType())
				.where(rm.folderCategory()).isEqualTo(records.getCategory_Z112()));

		RecordVODataProvider dataProvider = newDataProvider(query.sortAsc(Schemas.TITLE));

		assertThat(dataProvider.getRecordVO(0).getTitle()).isEqualTo("Boeuf");
		assertThat(dataProvider.getRecordVO(1).getTitle()).isEqualTo("Bouc");
		assertThat(dataProvider.getRecordVO(2).getTitle()).isEqualTo("Buffle");
		assertThat(dataProvider.getRecordVO(3).getTitle()).isEqualTo("Carotte");
		assertThat(dataProvider.getRecordVO(4).getTitle()).isEqualTo("Citron");

	}

	@Test
	public void givenDataProviderWhenListRecordVOsThenReturnGoodRecords()
			throws Exception {

		LogicalSearchQuery query = new LogicalSearchQuery(from(rm.folderSchemaType())
				.where(rm.folderCategory()).isEqualTo(records.getCategory_Z112()));

		RecordVODataProvider dataProvider = newDataProvider(query.sortAsc(Schemas.TITLE));

		assertThat(titlesOf(dataProvider.listRecordVOs(0, 1))).isEqualTo(asList("Boeuf"));
		assertThat(titlesOf(dataProvider.listRecordVOs(0, 3))).isEqualTo(asList("Boeuf", "Bouc", "Buffle"));
		assertThat(titlesOf(dataProvider.listRecordVOs(1, 4))).isEqualTo(asList("Bouc", "Buffle", "Carotte"));

	}

	private List<String> titlesOf(List<RecordVO> list) {
		List<String> titles = new ArrayList<>();
		for (RecordVO item : list) {
			titles.add(item.getTitle());
		}
		return titles;
	}

	@Before
	public void setUp()
			throws Exception {
		prepareSystem(withZeCollection().withConstellioRMModule().withAllTestUsers().withRMTest(records)
				.withFoldersAndContainersOfEveryStatus());

		rm = new RMSchemasRecordsServices(zeCollection, getModelLayerFactory());
		when(sessionContext.getCurrentCollection()).thenReturn(zeCollection);

	}

	private RecordVODataProvider newDataProvider(final LogicalSearchQuery zeQuery) {
		return new RecordVODataProvider(folderSchemaVO(), new RecordToVOBuilder(), getModelLayerFactory(),
				sessionContext) {
			@Override
			protected LogicalSearchQuery getQuery() {
				return zeQuery;
			}
		};
	}

	private MetadataSchemaVO folderSchemaVO() {
		return new MetadataSchemaToVOBuilder().build(rm.defaultFolderSchema(), VIEW_MODE.TABLE, sessionContext);
	}

}
