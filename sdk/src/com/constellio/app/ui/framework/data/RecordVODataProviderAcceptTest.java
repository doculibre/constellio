package com.constellio.app.ui.framework.data;

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
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import static com.constellio.model.services.search.query.logical.LogicalSearchQueryOperators.from;
import static java.util.Arrays.asList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

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
				.where(rm.folder.category()).isEqualTo(records.getCategory_Z112()));

		RecordVODataProvider dataProvider = newDataProvider(query);

		assertThat(dataProvider.size()).isEqualTo(5);

	}

	@Test
	public void givenDataProviderThenReturnGoodRecords()
			throws Exception {

		LogicalSearchQuery query = new LogicalSearchQuery(from(rm.folderSchemaType())
				.where(rm.folder.category()).isEqualTo(records.getCategory_Z112()));

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
				.where(rm.folder.category()).isEqualTo(records.getCategory_Z112()));

		RecordVODataProvider dataProvider = newDataProvider(query.sortAsc(Schemas.TITLE));

		assertThat(titlesOf(dataProvider.listRecordVOs(0, 1))).isEqualTo(asList("Boeuf"));
		assertThat(titlesOf(dataProvider.listRecordVOs(0, 3))).isEqualTo(asList("Boeuf", "Bouc", "Buffle"));
		assertThat(titlesOf(dataProvider.listRecordVOs(1, 3))).isEqualTo(asList("Bouc", "Buffle", "Carotte"));
		assertThat(titlesOf(dataProvider.listRecordVOs(1, 4))).isEqualTo(asList("Bouc", "Buffle", "Carotte", "Citron"));

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

		rm = new RMSchemasRecordsServices(zeCollection, getAppLayerFactory());
		when(sessionContext.getCurrentCollection()).thenReturn(zeCollection);
		when(sessionContext.getCurrentLocale()).thenReturn(Locale.FRENCH);

	}

	private RecordVODataProvider newDataProvider(final LogicalSearchQuery zeQuery) {
		return new RecordVODataProvider(folderSchemaVO(), new RecordToVOBuilder(), getModelLayerFactory(),
				sessionContext) {
			@Override
			public LogicalSearchQuery getQuery() {
				return zeQuery;
			}
		};
	}

	private MetadataSchemaVO folderSchemaVO() {
		return new MetadataSchemaToVOBuilder().build(rm.defaultFolderSchema(), VIEW_MODE.TABLE, sessionContext);
	}

}
