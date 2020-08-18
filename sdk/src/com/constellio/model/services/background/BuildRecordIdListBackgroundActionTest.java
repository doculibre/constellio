package com.constellio.model.services.background;

import com.constellio.data.dao.dto.records.RecordId;
import com.constellio.data.dao.services.factories.DataLayerFactory;
import com.constellio.model.services.factories.ModelLayerFactory;
import com.constellio.model.services.search.SearchServices;
import com.constellio.sdk.tests.ConstellioTest;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

public class BuildRecordIdListBackgroundActionTest extends ConstellioTest {

	@Mock DataLayerFactory dataLayerFactory;
	@Mock ModelLayerFactory modelLayerFactory;
	@Mock SearchServices searchServices;

	@Before
	public void setUp() throws Exception {
		when(modelLayerFactory.newSearchServices()).thenReturn(searchServices);
		when(modelLayerFactory.getDataLayerFactory()).thenReturn(dataLayerFactory);
	}

	@Test
	public void whenLoadingIdsThenRemoveRecent() {

		List<RecordId> expectedRecordIds = new ArrayList<>();
		for (int i = 0; i < 10000; i += 2) {
			expectedRecordIds.add(RecordId.toId(i));
		}


		for (int i = 20000; i < 30000; i += 3) {
			expectedRecordIds.add(RecordId.toId(i));
		}

		List<RecordId> ids = new ArrayList<>(expectedRecordIds);
		ids.add(RecordId.toId(30001));
		ids.add(RecordId.toId(32001));
		ids.add(RecordId.toId(32099));
		ids.add(RecordId.toId(33000));

		when(dataLayerFactory.isDistributed()).thenReturn(true);
		when(searchServices.recordsIdIteratorExceptEvents()).thenReturn(ids.iterator());

		assertThat(new BuildRecordIdListAndSortValuesBackgroundAction(modelLayerFactory).loadRecordIds())
				.isEqualTo(expectedRecordIds);

		when(searchServices.recordsIdIteratorExceptEvents()).thenReturn(ids.iterator());
		when(dataLayerFactory.isDistributed()).thenReturn(false);
		expectedRecordIds.add(RecordId.toId(30001));
		assertThat(new BuildRecordIdListAndSortValuesBackgroundAction(modelLayerFactory).loadRecordIds())
				.isEqualTo(expectedRecordIds);
	}
}
