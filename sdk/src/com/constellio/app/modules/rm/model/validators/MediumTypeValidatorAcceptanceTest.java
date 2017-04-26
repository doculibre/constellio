package com.constellio.app.modules.rm.model.validators;

import com.constellio.app.modules.rm.services.RMSchemasRecordsServices;
import com.constellio.app.modules.rm.wrappers.type.MediumType;
import com.constellio.model.services.records.RecordServices;
import com.constellio.model.services.records.RecordServicesException;
import com.constellio.sdk.tests.ConstellioTest;
import com.constellio.sdk.tests.TestUtils;
import org.junit.Before;
import org.junit.Test;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.data.MapEntry.entry;
import static org.junit.Assert.fail;

/**
 * Created by Charles Blanchette on 2017-03-23.
 */
public class MediumTypeValidatorAcceptanceTest extends ConstellioTest {

    RMSchemasRecordsServices rm;

    RecordServices recordServices;

    @Before
    public void setUp() {
        givenBackgroundThreadsEnabled();
        prepareSystem(
                withZeCollection().withConstellioRMModule().withAllTestUsers()
        );
        rm = new RMSchemasRecordsServices(zeCollection, getAppLayerFactory());
        recordServices = getModelLayerFactory().newRecordServices();
    }

    @Test
    public void givenMediumTypeCodeIsDMIsModified() throws Exception {
        try {
            MediumType mediumType = rm.getMediumTypeByCode("DM");
            mediumType.setCode("test");
            recordServices.add(mediumType);
            fail("No exception was thrown");
        } catch (RecordServicesException.ValidationException e) {
            assertThat(e.getErrors().getValidationErrors()).hasSize(1);
            Map<String, Object> params = e.getErrors().getValidationErrors().get(0).getParameters();
            assertThat(params).containsOnly(
                    entry("code", "test"),
                    entry("schemaCode", "ddvMediumType_default"));
            assertThat(TestUtils.frenchMessages(e.getErrors()).get(0)).isEqualTo("Le code \"DM\" ne peut pas être modifié");
        }
    }
}
