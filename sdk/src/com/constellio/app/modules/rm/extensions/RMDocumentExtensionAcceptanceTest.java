package com.constellio.app.modules.rm.extensions;

import com.constellio.app.modules.rm.RMTestRecords;
import com.constellio.app.modules.rm.services.RMSchemasRecordsServices;
import com.constellio.app.modules.rm.wrappers.Document;
import com.constellio.model.entities.records.Record;
import com.constellio.model.entities.records.wrappers.User;
import com.constellio.model.services.records.RecordServices;
import com.constellio.sdk.tests.ConstellioTest;
import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Created by constellios on 2017-04-03.
 */
public class RMDocumentExtensionAcceptanceTest extends ConstellioTest
{

    RMTestRecords records = new RMTestRecords(zeCollection);

    @Test
    public void whenCheckingIfDocumentDocumentTypeLogicallyOrPhysicallyDeletableThenFalse()
            throws Exception {
        prepareSystem(
                withZeCollection().withConstellioRMModule().withAllTestUsers()
                        .withRMTest(records).withFoldersAndContainersOfEveryStatus().withDocumentsDecommissioningList().withDocumentsHavingContent()
        );

        RMSchemasRecordsServices rm = new RMSchemasRecordsServices(zeCollection, getAppLayerFactory());
        RecordServices recordServices = getModelLayerFactory().newRecordServices();

        Document documentWithContent_a19 = records.getDocumentWithContent_A19();

        documentWithContent_a19.getContent().checkOut(records.getAdmin());

        recordServices.update(documentWithContent_a19);

        Record record = documentWithContent_a19.getWrappedRecord();

        assertThat(recordServices.isLogicallyDeletable(record, User.GOD)).isFalse();
        assertThat(recordServices.isLogicallyThenPhysicallyDeletable(record, User.GOD)).isFalse();
    }

}
