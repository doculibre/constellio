package com.constellio.app.modules.restapi.core.dao;

import com.constellio.app.modules.restapi.document.dao.DocumentDao;
import com.constellio.model.entities.records.Record;
import com.constellio.model.services.records.RecordServices;
import com.constellio.model.services.records.RecordServicesRuntimeException;
import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Matchers.anyLong;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.when;
import static org.mockito.MockitoAnnotations.initMocks;

public class BaseDaoTest {

	@Mock RecordServices recordServices;
	@Mock Record record;

	@InjectMocks BaseDao baseDao = new DocumentDao();

	@Before
	public void setUp() {
		initMocks(this);

		when(recordServices.realtimeGetRecordById(anyString())).thenReturn(record);
		when(recordServices.realtimeGetRecordById(anyString(), anyLong())).thenReturn(record);
	}

	@Test
	public void testGetRecordbyId() {
		Record record = baseDao.getRecordById("id");
		assertThat(record).isNotNull().isEqualTo(record);
	}

	@Test
	public void testGetRecordByIdWithInvalidId() {
		String id = "fakeId";
		when(recordServices.realtimeGetRecordById(id, null))
				.thenThrow(new RecordServicesRuntimeException.NoSuchRecordWithId(null, null, null));

		Record record = baseDao.getRecordById(id);
		assertThat(record).isNull();
	}

}
