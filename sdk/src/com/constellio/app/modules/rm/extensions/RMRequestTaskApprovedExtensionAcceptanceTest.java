package com.constellio.app.modules.rm.extensions;

import com.constellio.app.modules.rm.RMTestRecords;
import com.constellio.app.modules.rm.services.RMSchemasRecordsServices;
import com.constellio.app.modules.rm.services.borrowingServices.BorrowingType;
import com.constellio.app.modules.rm.wrappers.ContainerRecord;
import com.constellio.app.modules.rm.wrappers.Folder;
import com.constellio.app.modules.rm.wrappers.RMTask;
import com.constellio.app.modules.tasks.model.wrappers.request.RequestTask;
import com.constellio.app.modules.tasks.services.TasksSchemasRecordsServices;
import com.constellio.app.ui.pages.base.SessionContext;
import com.constellio.data.utils.TimeProvider;
import com.constellio.model.entities.records.Record;
import com.constellio.model.entities.records.wrappers.EmailToSend;
import com.constellio.model.entities.structures.EmailAddress;
import com.constellio.model.services.records.RecordServices;
import com.constellio.model.services.records.RecordServicesException;
import com.constellio.model.services.search.query.logical.condition.LogicalSearchCondition;
import com.constellio.model.services.users.UserServices;
import com.constellio.sdk.tests.ConstellioTest;
import com.constellio.sdk.tests.FakeSessionContext;
import com.constellio.sdk.tests.setups.Users;
import org.joda.time.LocalDate;
import org.joda.time.LocalDateTime;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;

import java.util.Arrays;
import java.util.Locale;

import static com.constellio.app.modules.rm.RMEmailTemplateConstants.*;
import static com.constellio.model.services.search.query.logical.LogicalSearchQueryOperators.from;
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
    LocalDateTime localDateTime;
    LocalDate localDate;
    Users users = new Users();

    @Before
    public void setup() {
        prepareSystem(
                withZeCollection().withConstellioRMModule().withAllTestUsers().withRMTest(records)
                        .withFoldersAndContainersOfEveryStatus().withAllTestUsers()
        );
        givenTimeIs(LocalDateTime.now());
        localDateTime = TimeProvider.getLocalDateTime();
        localDate = TimeProvider.getLocalDate();

        extension = spy(new RMRequestTaskApprovedExtension(zeCollection, getAppLayerFactory()));

        rm = new RMSchemasRecordsServices(zeCollection, getAppLayerFactory());
        taskSchemas = new TasksSchemasRecordsServices(zeCollection, getAppLayerFactory());
        users.setUp(new UserServices(getModelLayerFactory()));

        sessionContext = FakeSessionContext.adminInCollection(zeCollection);
        sessionContext.setCurrentLocale(Locale.FRENCH);

        recordServices = getModelLayerFactory().newRecordServices();
    }

    @Test
    public void givenBorrowRequestCompletedAndApprovedThenBorrowFolder() throws RecordServicesException {
        RMTask task = rm.wrapRMTask(taskSchemas.newBorrowFolderRequestTask(records.getChuckNorris().getId(),
                asList(records.getAdmin().getId(), records.getChuckNorris().getId()), records.folder_A42, 7, records.getFolder_A42().getTitle()).getWrappedRecord());
        recordServices.add(task.set(RequestTask.RESPONDANT, records.getAdmin().getId()));

        extension.completeBorrowRequest(task, true);

        Folder folder = records.getFolder_A42();
        assertThat(folder.getBorrowed()).isTrue();
        assertThat(folder.getBorrowUser()).isEqualTo(records.getChuckNorris().getId());

        EmailAddress adresseReceiver = new EmailAddress("Chuck Norris", "chuck@doculibre.com");
        EmailToSend emailToSend = getEmailToSend(ALERT_BORROWED_ACCEPTED);
        assertThat(emailToSend).isNotNull();
        assertThat(emailToSend.getTemplate()).isEqualTo(ALERT_BORROWED_ACCEPTED);
        assertThat(emailToSend.getTo()).isEqualTo(Arrays.asList(adresseReceiver));
        assertThat(emailToSend.getFrom()).isNull();
        assertThat(emailToSend.getSendOn()).isEqualTo(localDateTime);
        assertThat(emailToSend.getSubject()).isEqualTo("Demande d'emprunt du dossier: Crocodile");
        assertThat(emailToSend.getParameters()).hasSize(11);
        assertThat(emailToSend.getParameters()).containsOnly(
                "subject:" + task.getTitle(),
                "borrowingDate:" + localDate, "returnDate:" + folder.getBorrowPreviewReturnDate(),
                "currentUser:admin", "borrowingType:" + BorrowingType.BORROW,
                "borrowerEntered:chuck", "title:" + folder.getTitle(),
                "constellioURL:http://localhost:8080/constellio/",
                "recordURL:http://localhost:8080/constellio/#!displayFolder/" + folder.getId(),
                "recordType:folder",
                "isAccepted:Oui"
        );
    }

    @Test
    public void givenBorrowRequestCompletedAndDeniedThenDoNotBorrowFolder() throws RecordServicesException {
        RMTask task = rm.wrapRMTask(taskSchemas.newBorrowFolderRequestTask(records.getChuckNorris().getId(),
                asList(records.getAdmin().getId(), records.getChuckNorris().getId()), records.folder_A42, 7, records.getFolder_A42().getTitle()).getWrappedRecord());
        recordServices.add(task.set(RequestTask.RESPONDANT, records.getAdmin().getId()));

        extension.completeBorrowRequest(task, false);

        Folder folder = records.getFolder_A42();
        assertThat(folder.getBorrowed()).isNull();
        assertThat(folder.getBorrowUser()).isNull();

        EmailAddress adresseReceiver = new EmailAddress("Chuck Norris", "chuck@doculibre.com");
        EmailToSend emailToSend = getEmailToSend(ALERT_BORROWED_DENIED);
        assertThat(emailToSend).isNotNull();
        assertThat(emailToSend.getTemplate()).isEqualTo(ALERT_BORROWED_DENIED);
        assertThat(emailToSend.getTo()).isEqualTo(Arrays.asList(adresseReceiver));
        assertThat(emailToSend.getFrom()).isNull();
        assertThat(emailToSend.getSendOn()).isEqualTo(localDateTime);
        assertThat(emailToSend.getSubject()).isEqualTo("Demande d'emprunt du dossier: Crocodile");
        assertThat(emailToSend.getParameters()).hasSize(11);
        assertThat(emailToSend.getParameters()).containsOnly(
                "subject:" + task.getTitle(),
                "borrowingDate:" + localDate, "returnDate:" + LocalDate.now().plusDays(7),
                "currentUser:admin", "borrowingType:" + BorrowingType.BORROW,
                "borrowerEntered:chuck", "title:" + folder.getTitle(),
                "constellioURL:http://localhost:8080/constellio/",
                "recordURL:http://localhost:8080/constellio/#!displayFolder/" + folder.getId(),
                "recordType:folder",
                "isAccepted:Non"
        );
    }

    @Test
    public void givenBorrowRequestCompletedAndApprovedThenBorrowContainer() throws RecordServicesException {
        RMTask task = rm.wrapRMTask(taskSchemas.newBorrowContainerRequestTask(records.getChuckNorris().getId(),
                asList(records.getAdmin().getId(), records.getChuckNorris().getId()), records.containerId_bac13, 7, records.getContainerBac13().getTitle()).getWrappedRecord());
        recordServices.add(task.set(RequestTask.RESPONDANT, records.getAdmin().getId()));

        extension.completeBorrowRequest(task, true);

        ContainerRecord container = records.getContainerBac13();
        assertThat(container.getBorrowed()).isTrue();
        assertThat(container.getBorrower()).isEqualTo(records.getChuckNorris().getId());

        EmailAddress adresseReceiver = new EmailAddress("Chuck Norris", "chuck@doculibre.com");
        EmailToSend emailToSend = getEmailToSend(ALERT_BORROWED_ACCEPTED);
        assertThat(emailToSend).isNotNull();
        assertThat(emailToSend.getTemplate()).isEqualTo(ALERT_BORROWED_ACCEPTED);
        assertThat(emailToSend.getTo()).isEqualTo(Arrays.asList(adresseReceiver));
        assertThat(emailToSend.getFrom()).isNull();
        assertThat(emailToSend.getSendOn()).isEqualTo(localDateTime);
        assertThat(emailToSend.getSubject()).isEqualTo(task.getTitle());
        assertThat(emailToSend.getParameters()).hasSize(11);
        assertThat(emailToSend.getParameters()).containsOnly(
                "subject:" + task.getTitle(),
                "borrowingDate:" + localDate, "returnDate:" + container.getPlanifiedReturnDate(),
                "currentUser:admin", "borrowingType:" + BorrowingType.BORROW,
                "borrowerEntered:chuck", "title:" + container.getTitle(),
                "constellioURL:http://localhost:8080/constellio/",
                "recordURL:http://localhost:8080/constellio/#!displayContainer/" + container.getId(),
                "recordType:containerrecord",
                "isAccepted:Oui"
        );
    }

    @Test
    public void givenBorrowRequestCompletedAndDeniedThenDoNotBorrowContainer() throws RecordServicesException {
        RMTask task = rm.wrapRMTask(taskSchemas.newBorrowContainerRequestTask(records.getChuckNorris().getId(),
                asList(records.getAdmin().getId(), records.getChuckNorris().getId()), records.containerId_bac13, 7, records.getContainerBac13().getTitle()).getWrappedRecord());
        recordServices.add(task.set(RequestTask.RESPONDANT, records.getAdmin().getId()));

        extension.completeBorrowRequest(task, false);

        ContainerRecord container = records.getContainerBac13();
        assertThat(container.getBorrowed()).isNull();
        assertThat(container.getBorrower()).isNull();

        EmailAddress adresseReceiver = new EmailAddress("Chuck Norris", "chuck@doculibre.com");
        EmailToSend emailToSend = getEmailToSend(ALERT_BORROWED_DENIED);
        assertThat(emailToSend).isNotNull();
        assertThat(emailToSend.getTemplate()).isEqualTo(ALERT_BORROWED_DENIED);
        assertThat(emailToSend.getTo()).isEqualTo(Arrays.asList(adresseReceiver));
        assertThat(emailToSend.getFrom()).isNull();
        assertThat(emailToSend.getSendOn()).isEqualTo(localDateTime);
        assertThat(emailToSend.getSubject()).isEqualTo(task.getTitle());
        assertThat(emailToSend.getParameters()).hasSize(11);
        assertThat(emailToSend.getParameters()).containsOnly(
                "subject:" + task.getTitle(),
                "borrowingDate:" + localDate, "returnDate:" + LocalDate.now().plusDays(7),
                "currentUser:admin", "borrowingType:" + BorrowingType.BORROW,
                "borrowerEntered:chuck", "title:" + container.getTitle(),
                "constellioURL:http://localhost:8080/constellio/",
                "recordURL:http://localhost:8080/constellio/#!displayContainer/" + container.getId(),
                "recordType:containerrecord",
                "isAccepted:Non"
        );
    }

    @Test
    public void givenReturnRequestCompletedAndApprovedThenReturnFolder() throws RecordServicesException {
        recordServices.update(records.getFolder_A42().setBorrowed(true).setBorrowUser(records.getChuckNorris().getId()));
        RMTask task = rm.wrapRMTask(taskSchemas.newReturnFolderRequestTask(records.getChuckNorris().getId(),
                asList(records.getAdmin().getId(), records.getChuckNorris().getId()), records.folder_A42, records.getFolder_A42().getTitle()).getWrappedRecord());
        recordServices.add(task.set(RequestTask.RESPONDANT, records.getAdmin().getId()));


        Folder folder = records.getFolder_A42();
        assertThat(folder.getBorrowed()).isTrue();
        extension.completeReturnRequest(task, true);

        folder = records.getFolder_A42();
        assertThat(folder.getBorrowed()).isNull();

        EmailAddress adresseReceiver = new EmailAddress("Chuck Norris", "chuck@doculibre.com");
        EmailToSend emailToSend = getEmailToSend(ALERT_RETURNED_ACCEPTED);
        assertThat(emailToSend).isNotNull();
        assertThat(emailToSend.getTemplate()).isEqualTo(ALERT_RETURNED_ACCEPTED);
        assertThat(emailToSend.getTo()).isEqualTo(asList(adresseReceiver));
        assertThat(emailToSend.getFrom()).isNull();
        assertThat(emailToSend.getSendOn()).isEqualTo(localDateTime);
        assertThat(emailToSend.getSubject()).isEqualTo("Demande de retour du dossier: Crocodile");
        assertThat(emailToSend.getParameters()).hasSize(8);
        assertThat(emailToSend.getParameters()).containsOnly(
                "subject:" + task.getTitle(),
                "currentUser:admin", "returnDate:" + localDate,
                "title:" + folder.getTitle(), "constellioURL:http://localhost:8080/constellio/",
                "recordURL:http://localhost:8080/constellio/#!displayFolder/" + folder.getId(),
                "recordType:folder",
                "isAccepted:Oui"
        );
    }

    @Test
    public void givenReturnRequestCompletedAndDeniedThenDoNotReturnFolder() throws RecordServicesException {
        recordServices.update(records.getFolder_A42().setBorrowed(true).setBorrowUser(records.getChuckNorris().getId()));
        RMTask task = rm.wrapRMTask(taskSchemas.newReturnFolderRequestTask(records.getChuckNorris().getId(),
                asList(records.getAdmin().getId(), records.getChuckNorris().getId()), records.folder_A42, records.getFolder_A42().getTitle()).getWrappedRecord());
        recordServices.add(task.set(RequestTask.RESPONDANT, records.getAdmin().getId()));


        Folder folder = records.getFolder_A42();
        assertThat(folder.getBorrowed()).isTrue();
        extension.completeReturnRequest(task, false);

        folder = records.getFolder_A42();
        assertThat(folder.getBorrowed()).isTrue();

        EmailAddress adresseReceiver = new EmailAddress("Chuck Norris", "chuck@doculibre.com");
        EmailToSend emailToSend = getEmailToSend(ALERT_RETURNED_DENIED);
        assertThat(emailToSend).isNotNull();
        assertThat(emailToSend.getTemplate()).isEqualTo(ALERT_RETURNED_DENIED);
        assertThat(emailToSend.getTo()).isEqualTo(asList(adresseReceiver));
        assertThat(emailToSend.getFrom()).isNull();
        assertThat(emailToSend.getSendOn()).isEqualTo(localDateTime);
        assertThat(emailToSend.getSubject()).isEqualTo("Demande de retour du dossier: Crocodile");
        assertThat(emailToSend.getParameters()).hasSize(8);
        assertThat(emailToSend.getParameters()).containsOnly(
                "subject:" + task.getTitle(),
                "currentUser:admin", "returnDate:" + localDate,
                "title:" + folder.getTitle(), "constellioURL:http://localhost:8080/constellio/",
                "recordURL:http://localhost:8080/constellio/#!displayFolder/" + folder.getId(),
                "recordType:folder",
                "isAccepted:Non"
        );
    }

    @Test
    public void givenReturnRequestCompletedAndApprovedThenReturnContainer() throws RecordServicesException {
        recordServices.update(records.getContainerBac13().setBorrowed(true).setBorrower(records.getChuckNorris().getId()));
        RMTask task = rm.wrapRMTask(taskSchemas.newReturnContainerRequestTask(records.getChuckNorris().getId(),
                asList(records.getAdmin().getId(), records.getChuckNorris().getId()), records.containerId_bac13, records.getContainerBac13().getTitle()).getWrappedRecord());
        recordServices.add(task.set(RequestTask.RESPONDANT, records.getAdmin().getId()));

        ContainerRecord container = records.getContainerBac13();
        assertThat(container.getBorrowed()).isTrue();
        extension.completeReturnRequest(task, true);

        container = records.getContainerBac13();
        assertThat(container.getBorrowed()).isNull();

        EmailAddress adresseReceiver = new EmailAddress("Chuck Norris", "chuck@doculibre.com");
        EmailToSend emailToSend = getEmailToSend(ALERT_RETURNED_ACCEPTED);
        assertThat(emailToSend).isNotNull();
        assertThat(emailToSend.getTemplate()).isEqualTo(ALERT_RETURNED_ACCEPTED);
        assertThat(emailToSend.getTo()).isEqualTo(asList(adresseReceiver));
        assertThat(emailToSend.getFrom()).isNull();
        assertThat(emailToSend.getSendOn()).isEqualTo(localDateTime);
        assertThat(emailToSend.getSubject()).isEqualTo(task.getTitle());
        assertThat(emailToSend.getParameters()).hasSize(8);
        assertThat(emailToSend.getParameters()).containsOnly(
                "subject:" + task.getTitle(),
                "currentUser:admin", "returnDate:" + localDate,
                "title:" + container.getTitle(), "constellioURL:http://localhost:8080/constellio/",
                "recordURL:http://localhost:8080/constellio/#!displayContainer/" + container.getId(),
                "recordType:containerrecord",
                "isAccepted:Oui"
        );
    }

    @Test
    public void givenReturnRequestCompletedAndDeniedThenDoNotReturnContainer() throws RecordServicesException {
        recordServices.update(records.getContainerBac13().setBorrowed(true).setBorrower(records.getChuckNorris().getId()));
        RMTask task = rm.wrapRMTask(taskSchemas.newReturnContainerRequestTask(records.getChuckNorris().getId(),
                asList(records.getAdmin().getId(), records.getChuckNorris().getId()), records.containerId_bac13, records.getContainerBac13().getTitle()).getWrappedRecord());
        recordServices.add(task.set(RequestTask.RESPONDANT, records.getAdmin().getId()));

        ContainerRecord container = records.getContainerBac13();
        assertThat(container.getBorrowed()).isTrue();
        extension.completeReturnRequest(task, false);

        container = records.getContainerBac13();
        assertThat(container.getBorrowed()).isTrue();

        EmailAddress adresseReceiver = new EmailAddress("Chuck Norris", "chuck@doculibre.com");
        EmailToSend emailToSend = getEmailToSend(ALERT_RETURNED_DENIED);
        assertThat(emailToSend).isNotNull();
        assertThat(emailToSend.getTemplate()).isEqualTo(ALERT_RETURNED_DENIED);
        assertThat(emailToSend.getTo()).isEqualTo(asList(adresseReceiver));
        assertThat(emailToSend.getFrom()).isNull();
        assertThat(emailToSend.getSendOn()).isEqualTo(localDateTime);
        assertThat(emailToSend.getSubject()).isEqualTo(task.getTitle());
        assertThat(emailToSend.getParameters()).hasSize(8);
        assertThat(emailToSend.getParameters()).containsOnly(
                "subject:" + task.getTitle(),
                "currentUser:admin", "returnDate:" + localDate,
                "title:" + container.getTitle(), "constellioURL:http://localhost:8080/constellio/",
                "recordURL:http://localhost:8080/constellio/#!displayContainer/" + container.getId(),
                "recordType:containerrecord",
                "isAccepted:Non"
        );
    }

    @Test
    public void givenBorrowExtendedRequestCompletedAndApprovedThenExtendFolder() throws RecordServicesException {
        recordServices.update(records.getFolder_A42().setBorrowed(true).setBorrowUser(records.getChuckNorris().getId()).setBorrowPreviewReturnDate(LocalDate.now()));
        RMTask task = rm.wrapRMTask(taskSchemas.newBorrowFolderExtensionRequestTask(records.getChuckNorris().getId(),
                asList(records.getAdmin().getId(), records.getChuckNorris().getId()), records.folder_A42, records.getFolder_A42().getTitle(), LocalDate.now().plusDays(7)).getWrappedRecord());
        recordServices.add(task.set(RequestTask.RESPONDANT, records.getAdmin().getId()));


        Folder folder = records.getFolder_A42();
        assertThat(folder.getBorrowed()).isTrue();
        assertThat(folder.getBorrowPreviewReturnDate()).isEqualTo(LocalDate.now());
        extension.completeBorrowExtensionRequest(task, true);

        folder = records.getFolder_A42();
        assertThat(folder.getBorrowed()).isTrue();
        assertThat(folder.getBorrowPreviewReturnDate()).isEqualTo(LocalDate.now().plusDays(7));

        EmailAddress adresseReceiver = new EmailAddress("Chuck Norris", "chuck@doculibre.com");
        EmailToSend emailToSend = getEmailToSend(ALERT_BORROWING_EXTENTED_ACCEPTED);
        assertThat(emailToSend).isNotNull();
        assertThat(emailToSend.getTemplate()).isEqualTo(ALERT_BORROWING_EXTENTED_ACCEPTED);
        assertThat(emailToSend.getTo()).isEqualTo(Arrays.asList(adresseReceiver));
        assertThat(emailToSend.getFrom()).isNull();
        assertThat(emailToSend.getSendOn()).isEqualTo(localDateTime);
        assertThat(emailToSend.getSubject()).isEqualTo(task.getTitle());
        assertThat(emailToSend.getParameters()).hasSize(9);
        assertThat(emailToSend.getParameters()).containsOnly(
                "subject:" + task.getTitle(),
                "currentUser:admin", "returnDate:" + folder.getBorrowPreviewReturnDate(),
                "title:" + folder.getTitle(), "constellioURL:http://localhost:8080/constellio/",
                "recordURL:http://localhost:8080/constellio/#!displayFolder/" + folder.getId(),
                "recordType:folder", "extensionDate:" + localDate,
                "isAccepted:Oui"
        );
    }

    @Test
    public void givenBorrowExtendedRequestCompletedAndDeniedThenDoNotExtendFolder() throws RecordServicesException {
        recordServices.update(records.getFolder_A42().setBorrowed(true).setBorrowUser(records.getChuckNorris().getId()).setBorrowPreviewReturnDate(LocalDate.now()));
        RMTask task = rm.wrapRMTask(taskSchemas.newBorrowFolderExtensionRequestTask(records.getChuckNorris().getId(),
                asList(records.getAdmin().getId(), records.getChuckNorris().getId()), records.folder_A42, records.getFolder_A42().getTitle(), LocalDate.now().plusDays(7)).getWrappedRecord());
        recordServices.add(task.set(RequestTask.RESPONDANT, records.getAdmin().getId()));


        Folder folder = records.getFolder_A42();
        assertThat(folder.getBorrowed()).isTrue();
        assertThat(folder.getBorrowPreviewReturnDate()).isEqualTo(LocalDate.now());
        extension.completeBorrowExtensionRequest(task, false);

        folder = records.getFolder_A42();
        assertThat(folder.getBorrowed()).isTrue();
        assertThat(folder.getBorrowPreviewReturnDate()).isEqualTo(LocalDate.now());

        EmailAddress adresseReceiver = new EmailAddress("Chuck Norris", "chuck@doculibre.com");
        EmailToSend emailToSend = getEmailToSend(ALERT_BORROWING_EXTENTED_DENIED);
        assertThat(emailToSend).isNotNull();
        assertThat(emailToSend.getTemplate()).isEqualTo(ALERT_BORROWING_EXTENTED_DENIED);
        assertThat(emailToSend.getTo()).isEqualTo(Arrays.asList(adresseReceiver));
        assertThat(emailToSend.getFrom()).isNull();
        assertThat(emailToSend.getSendOn()).isEqualTo(localDateTime);
        assertThat(emailToSend.getSubject()).isEqualTo(task.getTitle());
        assertThat(emailToSend.getParameters()).hasSize(9);
        assertThat(emailToSend.getParameters()).containsOnly(
                "subject:" + task.getTitle(),
                "currentUser:admin", "returnDate:" + folder.getBorrowPreviewReturnDate().plusDays(7),
                "title:" + folder.getTitle(), "constellioURL:http://localhost:8080/constellio/",
                "recordURL:http://localhost:8080/constellio/#!displayFolder/" + folder.getId(),
                "recordType:folder", "extensionDate:" + localDate,
                "isAccepted:Non"
        );
    }

    @Test
    public void givenBorrowExtendedCompletedAndApprovedThenExtendContainer() throws RecordServicesException {
        recordServices.update(records.getContainerBac13().setBorrowed(true).setBorrower(records.getChuckNorris().getId()).setPlanifiedReturnDate(LocalDate.now()));
        RMTask task = rm.wrapRMTask(taskSchemas.newBorrowContainerExtensionRequestTask(records.getChuckNorris().getId(),
                asList(records.getAdmin().getId(), records.getChuckNorris().getId()), records.containerId_bac13, records.getContainerBac13().getTitle(), LocalDate.now().plusDays(7)).getWrappedRecord());
        recordServices.add(task.set(RequestTask.RESPONDANT, records.getAdmin().getId()));

        ContainerRecord containerRecord = records.getContainerBac13();
        assertThat(containerRecord.getBorrowed()).isTrue();
        assertThat(containerRecord.getPlanifiedReturnDate()).isEqualTo(LocalDate.now());
        extension.completeBorrowExtensionRequest(task, true);

        containerRecord = records.getContainerBac13();
        assertThat(containerRecord.getBorrowed()).isTrue();
        assertThat(containerRecord.getPlanifiedReturnDate()).isEqualTo(LocalDate.now().plusDays(7));

        EmailAddress adresseReceiver = new EmailAddress("Chuck Norris", "chuck@doculibre.com");
        EmailToSend emailToSend = getEmailToSend(ALERT_BORROWING_EXTENTED_ACCEPTED);
        assertThat(emailToSend).isNotNull();
        assertThat(emailToSend.getTemplate()).isEqualTo(ALERT_BORROWING_EXTENTED_ACCEPTED);
        assertThat(emailToSend.getTo()).isEqualTo(Arrays.asList(adresseReceiver));
        assertThat(emailToSend.getFrom()).isNull();
        assertThat(emailToSend.getSendOn()).isEqualTo(localDateTime);
        assertThat(emailToSend.getSubject()).isEqualTo("Demande de renouvellement d'emprunt du contenant: 10_A_06");
        assertThat(emailToSend.getParameters()).hasSize(9);
        assertThat(emailToSend.getParameters()).containsOnly(
                "subject:Demande de renouvellement d'emprunt du contenant: 10_A_06",
                "currentUser:admin", "returnDate:" + containerRecord.getPlanifiedReturnDate(),
                "title:" + containerRecord.getTitle(), "constellioURL:http://localhost:8080/constellio/",
                "recordURL:http://localhost:8080/constellio/#!displayContainer/" + containerRecord.getId(),
                "recordType:containerrecord", "extensionDate:" + localDate,
                "isAccepted:Oui"
        );
    }

    @Test
    public void givenBorrowExtendedCompletedAndApprovedThenDoNotExtendContainer() throws RecordServicesException {
        recordServices.update(records.getContainerBac13().setBorrowed(true).setBorrower(records.getChuckNorris().getId()).setPlanifiedReturnDate(LocalDate.now()));
        RMTask task = rm.wrapRMTask(taskSchemas.newBorrowContainerExtensionRequestTask(records.getChuckNorris().getId(),
                asList(records.getAdmin().getId(), records.getChuckNorris().getId()), records.containerId_bac13, records.getContainerBac13().getTitle(), LocalDate.now().plusDays(7)).getWrappedRecord());
        recordServices.add(task.set(RequestTask.RESPONDANT, records.getAdmin().getId()));

        ContainerRecord containerRecord = records.getContainerBac13();
        assertThat(containerRecord.getBorrowed()).isTrue();
        assertThat(containerRecord.getPlanifiedReturnDate()).isEqualTo(LocalDate.now());
        extension.completeBorrowExtensionRequest(task, false);

        containerRecord = records.getContainerBac13();
        assertThat(containerRecord.getBorrowed()).isTrue();
        assertThat(containerRecord.getPlanifiedReturnDate()).isEqualTo(LocalDate.now());

        EmailAddress adresseReceiver = new EmailAddress("Chuck Norris", "chuck@doculibre.com");
        EmailToSend emailToSend = getEmailToSend(ALERT_BORROWING_EXTENTED_DENIED);
        assertThat(emailToSend).isNotNull();
        assertThat(emailToSend.getTemplate()).isEqualTo(ALERT_BORROWING_EXTENTED_DENIED);
        assertThat(emailToSend.getTo()).isEqualTo(Arrays.asList(adresseReceiver));
        assertThat(emailToSend.getFrom()).isNull();
        assertThat(emailToSend.getSendOn()).isEqualTo(localDateTime);
        assertThat(emailToSend.getSubject()).isEqualTo("Demande de renouvellement d'emprunt du contenant: 10_A_06");
        assertThat(emailToSend.getParameters()).hasSize(9);
        assertThat(emailToSend.getParameters()).containsOnly(
                "subject:Demande de renouvellement d'emprunt du contenant: 10_A_06",
                "currentUser:admin", "returnDate:" + containerRecord.getPlanifiedReturnDate().plusDays(7),
                "title:" + containerRecord.getTitle(), "constellioURL:http://localhost:8080/constellio/",
                "recordURL:http://localhost:8080/constellio/#!displayContainer/" + containerRecord.getId(),
                "recordType:containerrecord", "extensionDate:" + localDate,
                "isAccepted:Non"
        );
    }

    private EmailToSend getEmailToSend(String template) {
        LogicalSearchCondition condition = from(rm.emailToSend())
                .where(rm.emailToSend().getMetadata(EmailToSend.TEMPLATE))
                .isEqualTo(template);
        Record emailRecord = getModelLayerFactory().newSearchServices().searchSingleResult(condition);
        if (emailRecord != null) {
            return rm.wrapEmailToSend(emailRecord);
        } else {
            return null;
        }
    }
}
