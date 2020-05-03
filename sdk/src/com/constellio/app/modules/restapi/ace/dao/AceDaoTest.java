package com.constellio.app.modules.restapi.ace.dao;

import com.constellio.app.modules.restapi.core.exception.UnresolvableOptimisticLockException;
import com.constellio.app.modules.restapi.resource.dto.AceDto;
import com.constellio.model.entities.records.Record;
import com.constellio.model.entities.records.wrappers.Authorization;
import com.constellio.model.entities.records.wrappers.User;
import com.constellio.model.entities.security.global.AuthorizationAddRequest;
import com.constellio.model.entities.security.global.AuthorizationDeleteRequest;
import com.constellio.model.entities.security.global.AuthorizationModificationRequest;
import com.constellio.model.services.records.RecordServicesException;
import com.constellio.model.services.security.AuthorizationsServices;
import com.constellio.model.services.security.AuthorizationsServicesRuntimeException;
import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;

import java.util.List;

import static java.util.Collections.singleton;
import static java.util.Collections.singletonList;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.when;
import static org.mockito.MockitoAnnotations.initMocks;

public class AceDaoTest {

	@Mock private AuthorizationsServices authorizationsServices;
	@Mock private User user;
	@Mock private Record record;
	@Mock private Authorization authorization;
	@Mock private Authorization authorizationDetails;
	@Mock private AuthorizationModificationRequest authorizationModificationRequest;

	@InjectMocks @Spy private AceDao aceDao;

	private List<AceDto> aces;
	private String id = "id";

	@Before
	public void setUp() {
		initMocks(this);

		aces = singletonList(AceDto.builder().principals(singleton("id")).permissions(singleton("READ")).build());

		when(authorizationsServices.getAuthorization(anyString(), anyString())).thenReturn(authorization);

		doReturn(record).when(aceDao).getUserRecordByUsername(anyString(), anyString());
	}

	@Test(expected = UnresolvableOptimisticLockException.class)
	public void testAddAcesUnresolvableOptimisticLockException() {
		when(authorizationsServices.add(any(AuthorizationAddRequest.class), any(User.class))).thenThrow(
				new AuthorizationsServicesRuntimeException.AuthServices_RecordServicesException(
						new RecordServicesException.UnresolvableOptimisticLockingConflict(id)));

		aceDao.addAces(user, record, aces);
	}

	@Test(expected = UnresolvableOptimisticLockException.class)
	public void testUpdateAcesUnresolvableOptimisticLockException() {
		doThrow(new AuthorizationsServicesRuntimeException.AuthServices_RecordServicesException(
				new RecordServicesException.UnresolvableOptimisticLockingConflict(id)))
				.when(authorizationsServices).execute(any(AuthorizationModificationRequest.class));

		aceDao.updateAces(user, record, aces);
	}

	@Test(expected = UnresolvableOptimisticLockException.class)
	public void testRemoveAcesUnresolvableOptimisticLockException() {
		doThrow(new AuthorizationsServicesRuntimeException.AuthServices_RecordServicesException(
				new RecordServicesException.UnresolvableOptimisticLockingConflict(id)))
				.when(authorizationsServices).execute(any(AuthorizationDeleteRequest.class));

		aceDao.removeAces(user, record, aces);
	}

}
