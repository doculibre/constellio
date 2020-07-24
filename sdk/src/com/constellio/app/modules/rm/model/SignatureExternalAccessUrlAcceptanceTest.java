package com.constellio.app.modules.rm.model;

import com.constellio.app.modules.rm.services.RMSchemasRecordsServices;
import com.constellio.app.modules.rm.wrappers.SignatureExternalAccessUrl;
import com.constellio.model.entities.records.wrappers.structure.ExternalAccessUrlStatus;
import com.constellio.model.services.records.RecordServices;
import com.constellio.model.services.records.RecordServicesException;
import com.constellio.sdk.tests.ConstellioTest;
import org.joda.time.LocalDate;
import org.junit.Before;
import org.junit.Test;

import static junit.framework.TestCase.fail;
import static org.assertj.core.api.Assertions.assertThat;

public class SignatureExternalAccessUrlAcceptanceTest extends ConstellioTest {

	RMSchemasRecordsServices rm;
	RecordServices recordServices;

	@Before
	public void setUp()
			throws Exception {
		prepareSystem(withZeCollection().withConstellioRMModule());
		rm = new RMSchemasRecordsServices(zeCollection, getAppLayerFactory());
		recordServices = getModelLayerFactory().newRecordServices();
	}

	@Test
	public void whenCreatingSignatureAccessThenCreated()
			throws Exception {

		SignatureExternalAccessUrl access = rm.newSignatureExternalAccessUrlWithId("zeAccessId");
		access.setExpirationDate(LocalDate.now());
		access.setStatus(ExternalAccessUrlStatus.OPEN);
		access.setToken("zeToken");
		access.setFullname("Mister X");
		access.setEmail("noreply.doculibre2@gmail.com");
		access.setAccessRecord("zeRecordId");

		recordServices.add(access);

		SignatureExternalAccessUrl newAccess = rm.getSignatureExternalAccessUrl("zeAccessId");

		assertThat(newAccess).isEqualToComparingFieldByField(access);
	}

	@Test
	public void whenCreatingSignatureAccessWithoutEmailThenExceptionIsThrown()
			throws Exception {

		try {
			SignatureExternalAccessUrl access = rm.newSignatureExternalAccessUrlWithId("zeAccessId");
			access.setExpirationDate(LocalDate.now());
			access.setStatus(ExternalAccessUrlStatus.OPEN);
			access.setToken("zeToken");
			access.setFullname("Mister X");
			access.setAccessRecord("zeRecordId");

			recordServices.add(access);
			fail("whenCreatingSignatureAccessWithoutEmailThenExceptionIsThrown should throw an exception");
		} catch (RecordServicesException e) {
			//ok
		}
	}
}
