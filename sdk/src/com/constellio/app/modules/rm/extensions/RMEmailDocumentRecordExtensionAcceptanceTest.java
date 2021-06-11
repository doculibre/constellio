package com.constellio.app.modules.rm.extensions;

import com.constellio.app.modules.rm.services.RMSchemasRecordsServices;
import com.constellio.model.entities.records.Record;
import com.constellio.model.entities.records.wrappers.User;
import com.constellio.model.services.records.RecordServices;
import com.constellio.sdk.tests.ConstellioTest;
import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class RMEmailDocumentRecordExtensionAcceptanceTest extends ConstellioTest {

	@Test
	public void whenCheckingIfEmailDocumentTypeLogicallyOrPhysicallyDeletableThenFalse()
			throws Exception {

		prepareSystem(withZeCollection().withConstellioRMModule().withAllTestUsers());
		RMSchemasRecordsServices rm = new RMSchemasRecordsServices(zeCollection, getAppLayerFactory());
		RecordServices recordServices = getModelLayerFactory().newRecordServices();

		Record emailDocumentType = rm.emailDocumentType().getWrappedRecord();

		assertThat(recordServices.validateLogicallyThenPhysicallyDeletable(emailDocumentType, User.GOD).isEmpty()).isFalse();

		recordServices.logicallyDelete(emailDocumentType, User.GOD);
		assertThat(recordServices.validatePhysicallyDeletable(emailDocumentType, User.GOD).isEmpty()).isFalse();

	}
}
