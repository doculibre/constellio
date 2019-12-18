package com.constellio.app.modules.rm.model;

import com.constellio.app.modules.rm.RMTestRecords;
import com.constellio.app.modules.rm.services.RMSchemasRecordsServices;
import com.constellio.app.modules.rm.wrappers.Email;
import com.constellio.model.entities.records.Record;
import com.constellio.model.entities.records.RecordUpdateOptions;
import com.constellio.model.services.records.RecordServices;
import com.constellio.model.services.records.RecordServicesException;
import com.constellio.sdk.tests.ConstellioTest;
import com.constellio.sdk.tests.setups.Users;
import org.junit.Before;
import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.fail;

public class DocumentTypeAcceptanceTest extends ConstellioTest {

	RMSchemasRecordsServices rm;
	RMTestRecords records = new RMTestRecords(zeCollection);
	Users users = new Users();
	RecordServices recordServices;

	@Before
	public void setUp()
			throws Exception {

		prepareSystem(
				withZeCollection().withConstellioRMModule().withRMTest(records)
						.withFoldersAndContainersOfEveryStatus().withAllTest(users)
		);

		rm = new RMSchemasRecordsServices(zeCollection, getAppLayerFactory());
		recordServices = getModelLayerFactory().newRecordServices();

		try {
			recordServices.update(rm.emailDocumentType().setLinkedSchema("").getWrappedRecord(),
					RecordUpdateOptions.validationExceptionSafeOptions());
		} catch (Exception e) {
			fail("Setup failed", e);
		}
	}

	@Test
	public void whenTryingToModifyDocumentTypeOnEmptyUnmodifiableDdvWithNoRecordUpdateOptions()
			throws RecordServicesException {
		Record emailDocumentTypeDdv = rm.emailDocumentType().setLinkedSchema(Email.DOCUMENT_TYPE).getWrappedRecord();
		try {
			recordServices.update(emailDocumentTypeDdv);
			fail("DDV is supposed to be unmodifiable");
		} catch (RecordServicesException e) {
			Record updatedEmailDocumentTypeDdv = recordServices.getDocumentById(emailDocumentTypeDdv.getId());
			assertThat(updatedEmailDocumentTypeDdv.get(rm.ddvDocumentType.linkedSchema())).isNull();
		}
	}

	@Test
	public void whenTryingToModifyDocumentTypeOnEmptyUnmodifiableDdvWithRecordUpdateOptions()
			throws RecordServicesException {
		Record emailDocumentTypeDdv = rm.emailDocumentType().setLinkedSchema(Email.DOCUMENT_TYPE).getWrappedRecord();
		try {
			recordServices.update(emailDocumentTypeDdv, RecordUpdateOptions.validationExceptionSafeOptions());
		} catch (RecordServicesException e) {
			fail("Transaction should succeed with validationExceptionSafeOptions");
		}

		Record updatedEmailDocumentTypeDdv = recordServices.getDocumentById(emailDocumentTypeDdv.getId());
		assertThat(updatedEmailDocumentTypeDdv.get(rm.ddvDocumentType.linkedSchema())).isEqualTo(Email.DOCUMENT_TYPE);
	}
}
