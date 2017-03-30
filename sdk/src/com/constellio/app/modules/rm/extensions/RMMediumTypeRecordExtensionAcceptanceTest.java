package com.constellio.app.modules.rm.extensions;

import com.constellio.app.modules.rm.services.RMSchemasRecordsServices;
import com.constellio.model.entities.records.Record;
import com.constellio.model.entities.records.wrappers.User;
import com.constellio.model.services.records.RecordServices;
import com.constellio.sdk.tests.ConstellioTest;
import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Created by Charles Blanchette on 2017-03-23.
 */
public class RMMediumTypeRecordExtensionAcceptanceTest extends ConstellioTest {

    @Test
    public void whenCheckingIfMediumTypeLogicallyDeletableThenFalse() throws Exception {
        prepareSystem(withZeCollection().withConstellioRMModule().withAllTestUsers());
        RMSchemasRecordsServices rm = new RMSchemasRecordsServices(zeCollection, getAppLayerFactory());
        RecordServices recordServices = getModelLayerFactory().newRecordServices();

        Record mediumType = rm.getMediumTypeByCode("DM").getWrappedRecord();
        assertThat(recordServices.isLogicallyDeletable(mediumType, User.GOD)).isFalse();

        mediumType = rm.getMediumTypeByCode("FI").getWrappedRecord();
        assertThat(recordServices.isLogicallyDeletable(mediumType, User.GOD)).isTrue();
    }
}
