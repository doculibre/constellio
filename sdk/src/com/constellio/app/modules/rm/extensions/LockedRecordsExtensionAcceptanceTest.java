package com.constellio.app.modules.rm.extensions;

import com.constellio.app.modules.rm.services.RMSchemasRecordsServices;
import com.constellio.app.modules.tasks.model.wrappers.types.TaskType;
import com.constellio.app.modules.tasks.services.TasksSchemasRecordsServices;
import com.constellio.model.services.records.RecordServices;
import com.constellio.model.services.records.RecordServicesException;
import com.constellio.sdk.tests.ConstellioTest;
import com.constellio.sdk.tests.TestUtils;
import org.junit.Before;
import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.fail;

/**
 * Created by Charles Blanchette on 2017-03-31.
 */
public class LockedRecordsExtensionAcceptanceTest extends ConstellioTest {

    RMSchemasRecordsServices rm;
    RecordServices recordServices;
    TasksSchemasRecordsServices tasksSchemasRecordsServices;

    @Before
    public void setUp() {
        prepareSystem(withZeCollection().withConstellioRMModule().withAllTestUsers());
        rm = new RMSchemasRecordsServices(zeCollection, getAppLayerFactory());
        recordServices = getModelLayerFactory().newRecordServices();
        tasksSchemasRecordsServices = new TasksSchemasRecordsServices(zeCollection, getAppLayerFactory());
    }

    @Test
    public void whenCheckingIfLockedRecordCodeModifiedThenFalse() throws Exception {
        try {
            TaskType taskType = tasksSchemasRecordsServices.getTaskTypeByCode("borrowRequest");
            taskType.setCode("test");
            recordServices.add(taskType);
            fail("No exception was thrown");
        } catch (RecordServicesException.ValidationException e) {
            assertThat(e.getErrors().getValidationErrors()).hasSize(1);
            assertThat(TestUtils.frenchMessages(e.getErrors()).get(0)).isEqualTo("Le code ou le schéma relié ne peuvent pas être modifiés");
        }
    }

    @Test
    public void whenCheckingIfLockedRecordLinkedSchemaModifiedThenFalse() throws Exception {
        try {
            TaskType taskType = tasksSchemasRecordsServices.getTaskTypeByCode("borrowRequest");
            taskType.setLinkedSchema("test");
            recordServices.add(taskType);
            fail("No exception was thrown");
        } catch (RecordServicesException.ValidationException e) {
            assertThat(e.getErrors().getValidationErrors()).hasSize(1);
            assertThat(TestUtils.frenchMessages(e.getErrors()).get(0)).isEqualTo("Le code ou le schéma relié ne peuvent pas être modifiés");
        }
    }
}
