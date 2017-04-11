package com.constellio.app.modules.rm.extensions;

import com.constellio.app.modules.rm.RMTestRecords;
import com.constellio.app.modules.rm.model.enums.DecommissioningListType;
import com.constellio.app.modules.rm.services.RMSchemasRecordsServices;
import com.constellio.app.modules.rm.services.decommissioning.DecommissioningService;
import com.constellio.app.modules.rm.services.logging.DecommissioningLoggingService;
import com.constellio.app.modules.rm.wrappers.DecommissioningList;
import com.constellio.model.entities.records.Record;
import com.constellio.model.entities.records.wrappers.Event;
import com.constellio.model.entities.records.wrappers.EventType;
import com.constellio.model.entities.records.wrappers.User;
import com.constellio.model.entities.records.wrappers.UserFolder;
import com.constellio.model.entities.schemas.Schemas;
import com.constellio.model.services.records.RecordServices;
import com.constellio.model.services.records.RecordServicesException;
import com.constellio.model.services.search.SearchServices;
import com.constellio.model.services.search.query.logical.LogicalSearchQuery;
import com.constellio.sdk.tests.ConstellioTest;
import com.constellio.sdk.tests.setups.Users;
import org.junit.Before;
import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;

import static com.constellio.model.services.search.query.logical.LogicalSearchQueryOperators.from;

/**
 * Created by constellios on 2017-04-11.
 */
public class RMEventRecordExtensionAcceptanceTest extends ConstellioTest {

    DecommissioningLoggingService decommisioningLoggingService;
    RMSchemasRecordsServices rm;
    RMTestRecords records = new RMTestRecords(zeCollection);
    RecordServices recordServices;
    SearchServices searchServices;

    public static final String DECOMMISSIONING_LIST = "DECOMMISSIONING_LIST";

    @Before
    public void setUp()
            throws Exception {

        prepareSystem(
                withZeCollection().withConstellioRMModule().withAllTestUsers().withRMTest(records)
                        .withFoldersAndContainersOfEveryStatus().withAllTestUsers()
        );

        rm = new RMSchemasRecordsServices(zeCollection, getAppLayerFactory());
        recordServices = getModelLayerFactory().newRecordServices();
        searchServices = getModelLayerFactory().newSearchServices();
        decommisioningLoggingService = new DecommissioningLoggingService(getModelLayerFactory());
    }


    @Test
    public void givenEventFolderDestructionAdministrativeUnitInPath() throws RecordServicesException, InterruptedException {
        DecommissioningList decommissioningList = getDecommissioningList(DecommissioningListType.FOLDERS_TO_DESTROY);
        assertLastEvent(decommissioningList, EventType.FOLDER_DESTRUCTION);
    }

    @Test
    public void givenEventFolderDepositAdministrativeUnitInPath() throws RecordServicesException, InterruptedException {
        DecommissioningList decommissioningList = getDecommissioningList(DecommissioningListType.FOLDERS_TO_DEPOSIT);
        assertLastEvent(decommissioningList, EventType.FOLDER_DEPOSIT);
    }

    @Test
    public void givenEventFolderToTransferAdministrativeUnitInPath() throws RecordServicesException, InterruptedException {
        DecommissioningList decommissioningList = getDecommissioningList(DecommissioningListType.FOLDERS_TO_TRANSFER);
        assertLastEvent(decommissioningList, EventType.FOLDER_RELOCATION);
    }

    @Test
    public void given()
    {
        decommisioningLoggingService.logPdfAGeneration(records.getDocumentWithContent_A19(), records.getAdmin());


    }

    private void assertLastEvent(DecommissioningList decommissioningList, String decomissioningListType)
    {
        LogicalSearchQuery query = new LogicalSearchQuery(from(rm.event.schemaType()).returnAll()).sortDesc(Schemas.CREATED_ON);

        List<Record> listRecord = searchServices.search(query);

        Event event = rm.wrapEvent(listRecord.get(0));

        assertThat(event.getRecordId()).isEqualTo(decommissioningList.getId());
        assertThat(event.getEventPrincipalPath()).isEqualTo("/admUnits/unitId_10/" + event.getId());
        assertThat(event.getType()).isEqualTo(decomissioningListType);
    }

    private DecommissioningList getDecommissioningList(DecommissioningListType decomissioningListType) throws RecordServicesException, InterruptedException {
        DecommissioningList decommissioningList = rm.newDecommissioningList();
        decommissioningList.setTitle(DECOMMISSIONING_LIST);

        decommissioningList.setAdministrativeUnit(records.unitId_10);
        decommissioningList.setDecommissioningListType(decomissioningListType);

        recordServices.add(decommissioningList.getWrappedRecord());

        decommisioningLoggingService.logDecommissioning(decommissioningList, records.getAdmin());

        waitForBatchProcess();

        return decommissioningList;
    }


}
