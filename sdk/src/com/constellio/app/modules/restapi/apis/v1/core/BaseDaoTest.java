package com.constellio.app.modules.restapi.apis.v1.core;

import com.constellio.app.modules.restapi.apis.v1.document.dao.DocumentDao;
import com.constellio.app.modules.restapi.core.util.Algorithms;
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

	private String data = "localhostidserviceKeyDOCUMENTGET20500101T080000Z36001.0";
	private String key = "token";

	private String expectedSignature = "vbTrKqAtjZGVeqku99GiqzV7S3pmQaD1gt7rng4GVQ8";

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

	@Test
	public void testSign() throws Exception {
		String signature = baseDao.sign(key, data);

		assertThat(signature).isEqualTo(expectedSignature);
	}

	@Test
	public void testSignWithAlgorithmParameter() throws Exception {
		String signature = baseDao.sign(key, data, Algorithms.HMAC_SHA_256);

		assertThat(signature).isEqualTo(expectedSignature);
	}

	@Test
	public void testSignWithWrongData() throws Exception {
		String signature = baseDao.sign(key, data.concat("fake"));

		assertThat(signature).isNotEqualTo(expectedSignature);
	}

	@Test(expected = UnsupportedOperationException.class)
	public void testSignWithInvalidAlgorithm() throws Exception {
		baseDao.sign(key, data, Algorithms.MD5);
	}
}
