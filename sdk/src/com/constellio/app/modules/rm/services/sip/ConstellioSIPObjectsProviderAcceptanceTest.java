package com.constellio.app.modules.rm.services.sip;

import com.constellio.app.modules.rm.RMTestRecords;
import com.constellio.app.modules.rm.services.RMSchemasRecordsServices;
import com.constellio.model.services.search.SearchServices;
import com.constellio.sdk.tests.ConstellioTest;
import org.junit.Before;
import org.junit.Test;

public class ConstellioSIPObjectsProviderAcceptanceTest extends ConstellioTest {
    RMTestRecords records = new RMTestRecords(zeCollection);
    SearchServices searchServices;
    RMSchemasRecordsServices rm;

    @Before
    public void setup(){
        prepareSystem(
                withZeCollection().withConstellioRMModule().withRMTest(records)
                        .withFoldersAndContainersOfEveryStatus().withAllTestUsers()
        );
        this.searchServices = getModelLayerFactory().newSearchServices();
        this.rm = new RMSchemasRecordsServices(zeCollection, getAppLayerFactory());
    }

    @Test
    public void testSIPGenerationWithEmail(){

    }

}
