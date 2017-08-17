package com.constellio.app.modules.rm.model;

import com.constellio.app.modules.rm.RMTestRecords;
import com.constellio.app.modules.rm.services.RMSchemasRecordsServices;
import com.constellio.model.services.records.RecordServices;
import com.constellio.model.services.search.SearchServices;
import com.constellio.sdk.tests.ConstellioTest;
import org.junit.Before;
import org.junit.Test;

public class SIPArchivesGenerationAcceptanceTest extends ConstellioTest {

    RMTestRecords records = new RMTestRecords(zeCollection);

    RMSchemasRecordsServices rm;

    RecordServices recordServices;

    SearchServices searchServices;

    @Before
    public void setup() {
        prepareSystem(
                withZeCollection().withConstellioRMModule().withAllTestUsers()
                        .withRMTest(records).withFoldersAndContainersOfEveryStatus()
        );

        rm = new RMSchemasRecordsServices(zeCollection, getAppLayerFactory());
        recordServices = getModelLayerFactory().newRecordServices();
        searchServices = getModelLayerFactory().newSearchServices();
    }

    @Test
    public void testZipContainCorrectFiles(){

    }
}
