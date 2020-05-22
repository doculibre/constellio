package com.constellio.model.entities.records.wrappers;

import com.constellio.model.entities.records.wrappers.structure.ExternalAccessUrlStatus;
import com.constellio.model.services.records.RecordServices;
import com.constellio.model.services.records.SchemasRecordsServices;
import com.constellio.sdk.tests.ConstellioTest;
import org.joda.time.LocalDate;
import org.junit.Before;
import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class ExternalAccessUrlAcceptanceTest extends ConstellioTest {

	SchemasRecordsServices recordsServices;
	RecordServices recordServices;

	@Before
	public void setUp()
			throws Exception {
		prepareSystem(withZeCollection());
		recordsServices = new SchemasRecordsServices(zeCollection, getModelLayerFactory());
		recordServices = getModelLayerFactory().newRecordServices();
	}

	@Test
	public void whenCreatingAccessThenCreated()
			throws Exception {

		ExternalAccessUrl access = recordsServices.newExternalAccessUrlWithId("zeAccessId")
				.setExpirationDate(LocalDate.now())
				.setStatus(ExternalAccessUrlStatus.OPEN)
				.setToken("zeToken")
				.setAccessRecord("zeRecordId");

		recordServices.add(access);

		ExternalAccessUrl newAccess = recordsServices.getExternalAccessUrl("zeAccessId");

		assertThat(newAccess).isEqualToComparingFieldByField(access);
	}

	@Test
	public void whenCreatingSignatureAccessThenCreated()
			throws Exception {

		ExternalAccessUrl access = recordsServices.newSignatureExternalAccessUrlWithId("zeAccessId")
				.setExpirationDate(LocalDate.now())
				.setStatus(ExternalAccessUrlStatus.OPEN)
				.setToken("zeToken")
				.setAccessRecord("zeRecordId");

		recordServices.add(access);

		ExternalAccessUrl newAccess = recordsServices.getSignatureExternalAccessUrl("zeAccessId");

		assertThat(newAccess).isEqualToComparingFieldByField(access);
	}
}
