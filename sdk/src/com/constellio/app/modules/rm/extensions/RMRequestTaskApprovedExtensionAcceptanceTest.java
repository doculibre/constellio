package com.constellio.app.modules.rm.extensions;

import com.constellio.app.modules.rm.RMTestRecords;
import com.constellio.app.modules.rm.services.RMSchemasRecordsServices;
import com.constellio.app.modules.rm.wrappers.ContainerRecord;
import com.constellio.app.modules.rm.wrappers.Folder;
import com.constellio.app.modules.rm.wrappers.RMTask;
import com.constellio.app.modules.tasks.services.TasksSchemasRecordsServices;
import com.constellio.app.ui.pages.base.SessionContext;
import com.constellio.model.services.records.RecordServices;
import com.constellio.model.services.records.RecordServicesException;
import com.constellio.model.services.users.UserServices;
import com.constellio.sdk.tests.ConstellioTest;
import com.constellio.sdk.tests.FakeSessionContext;
import com.constellio.sdk.tests.setups.Users;
import org.joda.time.LocalDate;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;

import java.util.Locale;

import static com.constellio.sdk.tests.TestUtils.asList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.spy;

/**
 * Created by Constellio on 2017-04-03.
 */
public class RMRequestTaskApprovedExtensionAcceptanceTest extends ConstellioTest {

    @Mock RMRequestTaskApprovedExtension extension;

    private SessionContext sessionContext;
    private RecordServices recordServices;
    private RMSchemasRecordsServices rm;
    private TasksSchemasRecordsServices taskSchemas;
    RMTestRecords records = new RMTestRecords(zeCollection);
    Users users = new Users();

    @Before
    public void setup() {
        prepareSystem(
                withZeCollection().withConstellioRMModule().withAllTestUsers().withRMTest(records)
                        .withFoldersAndContainersOfEveryStatus().withAllTestUsers()
        );

        extension = spy(new RMRequestTaskApprovedExtension(zeCollection, getAppLayerFactory()));

        rm = new RMSchemasRecordsServices(zeCollection, getAppLayerFactory());
        taskSchemas = new TasksSchemasRecordsServices(zeCollection, getAppLayerFactory());
        users.setUp(new UserServices(getModelLayerFactory()));

        sessionContext = FakeSessionContext.adminInCollection(zeCollection);
        sessionContext.setCurrentLocale(Locale.FRENCH);

        recordServices = getModelLayerFactory().newRecordServices();
    }

    @Test
    public void givenBorrowRequestCompletedThenBorrowFolder() throws RecordServicesException {
        RMTask task = rm.wrapRMTask(taskSchemas.newBorrowFolderRequestTask(records.getChuckNorris().getId(),
                asList(records.getAdmin().getId(), records.getChuckNorris().getId()), records.folder_A42).getWrappedRecord());
        recordServices.add(task);

        extension.completeBorrowRequest(task, true);

        Folder folder = records.getFolder_A42();
        assertThat(folder.getBorrowed()).isTrue();
        assertThat(folder.getBorrowUser()).isEqualTo(records.getChuckNorris().getId());
    }

    @Test
    public void givenBorrowRequestCompletedThenBorrowContainer() throws RecordServicesException {
        RMTask task = rm.wrapRMTask(taskSchemas.newBorrowContainerRequestTask(records.getChuckNorris().getId(),
                asList(records.getAdmin().getId(), records.getChuckNorris().getId()), records.containerId_bac13).getWrappedRecord());
        recordServices.add(task);

        extension.completeBorrowRequest(task, true);

        ContainerRecord container = records.getContainerBac13();
        assertThat(container.getBorrowed()).isTrue();
        assertThat(container.getBorrower()).isEqualTo(records.getChuckNorris().getId());
    }

    @Test
    public void givenReturnRequestCompletedThenBorrowFolder() throws RecordServicesException {
        recordServices.update(records.getFolder_A42().setBorrowed(true).setBorrowUser(records.getChuckNorris().getId()));
        RMTask task = rm.wrapRMTask(taskSchemas.newReturnFolderRequestTask(records.getChuckNorris().getId(),
                asList(records.getAdmin().getId(), records.getChuckNorris().getId()), records.folder_A42).getWrappedRecord());
        recordServices.add(task);


        Folder folder = records.getFolder_A42();
        assertThat(folder.getBorrowed()).isTrue();
        extension.completeReturnRequest(task, true);

        folder = records.getFolder_A42();
        assertThat(folder.getBorrowed()).isNull();
    }

    @Test
    public void givenReturnRequestCompletedThenBorrowContainer() throws RecordServicesException {
        recordServices.update(records.getContainerBac13().setBorrowed(true).setBorrower(records.getChuckNorris().getId()));
        RMTask task = rm.wrapRMTask(taskSchemas.newReturnContainerRequestTask(records.getChuckNorris().getId(),
                asList(records.getAdmin().getId(), records.getChuckNorris().getId()), records.containerId_bac13).getWrappedRecord());
        recordServices.add(task);

        ContainerRecord container = records.getContainerBac13();
        assertThat(container.getBorrowed()).isTrue();
        extension.completeReturnRequest(task, true);

        container = records.getContainerBac13();
        assertThat(container.getBorrowed()).isNull();
    }

    @Test
    public void givenBorrowExtendedRequestCompletedThenBorrowFolder() throws RecordServicesException {
        recordServices.update(records.getFolder_A42().setBorrowed(true).setBorrowUser(records.getChuckNorris().getId()).setBorrowPreviewReturnDate(LocalDate.now()));
        RMTask task = rm.wrapRMTask(taskSchemas.newBorrowFolderExtensionRequestTask(records.getChuckNorris().getId(),
                asList(records.getAdmin().getId(), records.getChuckNorris().getId()), records.folder_A42, LocalDate.now().plusDays(7)).getWrappedRecord());
        recordServices.add(task);


        Folder folder = records.getFolder_A42();
        assertThat(folder.getBorrowed()).isTrue();
        assertThat(folder.getBorrowPreviewReturnDate()).isEqualTo(LocalDate.now());
        extension.completeBorrowExtensionRequest(task, true);

        folder = records.getFolder_A42();
        assertThat(folder.getBorrowed()).isTrue();
        assertThat(folder.getBorrowPreviewReturnDate()).isEqualTo(LocalDate.now().plusDays(7));
    }

    @Test
    public void givenBorrowExtendedCompletedThenBorrowContainer() throws RecordServicesException {
        recordServices.update(records.getContainerBac13().setBorrowed(true).setBorrower(records.getChuckNorris().getId()).setPlanifiedReturnDate(LocalDate.now()));
        RMTask task = rm.wrapRMTask(taskSchemas.newBorrowContainerExtensionRequestTask(records.getChuckNorris().getId(),
                asList(records.getAdmin().getId(), records.getChuckNorris().getId()), records.containerId_bac13, LocalDate.now().plusDays(7)).getWrappedRecord());
        recordServices.add(task);

        ContainerRecord containerRecord = records.getContainerBac13();
        assertThat(containerRecord.getBorrowed()).isTrue();
        assertThat(containerRecord.getPlanifiedReturnDate()).isEqualTo(LocalDate.now());
        extension.completeBorrowExtensionRequest(task, true);

        containerRecord = records.getContainerBac13();
        assertThat(containerRecord.getBorrowed()).isTrue();
        assertThat(containerRecord.getPlanifiedReturnDate()).isEqualTo(LocalDate.now().plusDays(7));
    }
}
