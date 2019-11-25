package com.constellio.app.modules.restapi.core.service;

import com.constellio.app.modules.restapi.core.exception.RecordLogicallyDeletedException;
import com.constellio.app.modules.restapi.core.exception.RecordNotFoundException;
import com.constellio.app.modules.restapi.document.DocumentService;
import com.constellio.app.modules.restapi.document.dao.DocumentDao;
import com.constellio.app.modules.restapi.validation.exception.UnauthenticatedUserException;
import com.constellio.model.entities.records.Record;
import com.constellio.model.entities.records.wrappers.User;
import com.constellio.model.entities.schemas.Schemas;
import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.when;
import static org.mockito.MockitoAnnotations.initMocks;

public class BaseServiceTest {

	@Mock Record record;
	@Mock User user;
	@Mock DocumentDao documentDao;

	@InjectMocks @Spy BaseService baseService = new DocumentService();

	@Before
	public void setUp() {
		initMocks(this);

		when(documentDao.getUser(anyString(), anyString())).thenReturn(user);
		when(documentDao.getRecordById(anyString(), anyString())).thenReturn(record);

		when(baseService.getDao()).thenReturn(documentDao);
	}

	@Test
	public void testGetRecord() {
		Record record = baseService.getRecord("id", false);
		assertThat(record).isNotNull().isEqualTo(record);
	}

	@Test(expected = RecordNotFoundException.class)
	public void testGetRecordWithInvalidId() {
		when(documentDao.getRecordById("fakeId", null)).thenReturn(null);
		baseService.getRecord("fakeId", false);
	}

	@Test
	public void testGetRecordWithCheckLogicallyDeleted() {
		when(record.get(Schemas.LOGICALLY_DELETED_STATUS)).thenReturn(false);
		Record record = baseService.getRecord("id", true);
		assertThat(record).isNotNull().isEqualTo(record);
	}

	@Test(expected = RecordLogicallyDeletedException.class)
	public void testGetRecordWithCheckLogicallyDeletedAndLogicallyDeletedRecord() {
		when(record.get(Schemas.LOGICALLY_DELETED_STATUS)).thenReturn(true);
		baseService.getRecord("id", true);
	}

	@Test
	public void testGetUser() {
		User user = baseService.getUser("serviceKey", "collection");
		assertThat(user).isNotNull().isEqualTo(user);
	}

	@Test(expected = UnauthenticatedUserException.class)
	public void testGetUserWithInvalidId() {
		when(documentDao.getUser("fakeServiceKey", "collection")).thenReturn(null);
		baseService.getUser("fakeServiceKey", "collection");
	}

}
