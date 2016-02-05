package com.constellio.model.entities.records.wrappers;

import static org.assertj.core.api.Assertions.assertThat;

import org.joda.time.LocalDate;
import org.junit.Before;
import org.junit.Test;

import com.constellio.model.services.records.RecordServices;
import com.constellio.model.services.records.RecordServicesException;
import com.constellio.model.services.records.SchemasRecordsServices;
import com.constellio.model.services.search.SearchServices;
import com.constellio.sdk.tests.ConstellioTest;
import com.constellio.sdk.tests.setups.Users;

public class EmailToSendAcceptanceTest extends ConstellioTest {
	Users users = new Users();
	RecordServices recordServices;
	SearchServices searchServices;
	SchemasRecordsServices schemas;
	private LocalDate now = LocalDate.now();
	@Before
	public void setUp()
			throws Exception {
		prepareSystem(withZeCollection().withTasksModule().withAllTest(users));
		givenTimeIs(now);

		recordServices = getModelLayerFactory().newRecordServices();
		searchServices = getModelLayerFactory().newSearchServices();
		schemas = new SchemasRecordsServices(zeCollection, getModelLayerFactory());
	}

	@Test
	public void givenNewEmailToSendThenTryingCountEqualsZero()
			throws Exception {
		EmailToSend emailToSend = schemas.newEmailToSend();
		assertThat(emailToSend.getTryingCount()).isEqualTo(0d);
	}
}
