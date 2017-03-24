package com.constellio.app.modules.rm.services.borrowingServices;

import com.constellio.app.modules.rm.RMTestRecords;
import com.constellio.app.modules.rm.model.enums.FolderStatus;
import com.constellio.app.modules.rm.services.RMSchemasRecordsServices;
import com.constellio.app.modules.rm.services.borrowingServices.BorrowingServicesRunTimeException.*;
import com.constellio.app.modules.rm.services.events.RMEventsSearchServices;
import com.constellio.app.modules.rm.wrappers.Folder;
import com.constellio.app.modules.tasks.model.wrappers.Task;
import com.constellio.app.modules.tasks.services.TasksSchemasRecordsServices;
import com.constellio.data.utils.TimeProvider;
import com.constellio.model.entities.records.Record;
import com.constellio.model.entities.records.wrappers.EmailToSend;
import com.constellio.model.entities.records.wrappers.Event;
import com.constellio.model.entities.records.wrappers.EventType;
import com.constellio.model.entities.schemas.MetadataSchema;
import com.constellio.model.entities.schemas.MetadataSchemaTypes;
import com.constellio.model.entities.structures.EmailAddress;
import com.constellio.model.services.records.RecordServices;
import com.constellio.model.services.records.RecordServicesException;
import com.constellio.model.services.search.SearchServices;
import com.constellio.model.services.search.query.logical.condition.LogicalSearchCondition;
import com.constellio.sdk.tests.ConstellioTest;
import com.constellio.sdk.tests.setups.Users;
import org.joda.time.LocalDate;
import org.joda.time.LocalDateTime;
import org.junit.Before;
import org.junit.Test;

import java.util.List;

import static com.constellio.app.modules.rm.RMEmailTemplateConstants.*;
import static com.constellio.model.services.search.query.logical.LogicalSearchQueryOperators.from;
import static java.util.Arrays.asList;
import static org.assertj.core.api.Assertions.assertThat;

public class BorrowingServicesAcceptTest extends ConstellioTest {

    RMSchemasRecordsServices rm;
    RMTestRecords records = new RMTestRecords(zeCollection);
    BorrowingServices borrowingServices;
    LocalDate nowDate = TimeProvider.getLocalDate();
    RMEventsSearchServices rmEventsSearchServices;
    SearchServices searchServices;
    RecordServices recordServices;
    Users users = new Users();
    private Task zeTask;
    private TasksSchemasRecordsServices tasksSchemas;
    private MetadataSchema emailToSendSchema;

    @Before
    public void setUp()
            throws Exception {

        prepareSystem(
                withZeCollection().withConstellioRMModule().withAllTestUsers().withRMTest(records)
                        .withFoldersAndContainersOfEveryStatus().withAllTest(users)
        );

        rm = new RMSchemasRecordsServices(zeCollection, getAppLayerFactory());
        borrowingServices = new BorrowingServices(zeCollection, getModelLayerFactory());
        rmEventsSearchServices = new RMEventsSearchServices(getModelLayerFactory(), zeCollection);
        searchServices = getModelLayerFactory().newSearchServices();
        recordServices = getModelLayerFactory().newRecordServices();
        tasksSchemas = new TasksSchemasRecordsServices(zeCollection, getAppLayerFactory());
        emailToSendSchema = tasksSchemas.emailToSend();

        givenTimeIs(nowDate);
    }

    @Test
    public void givenRecordsBorrowedFromTaskThenValidEmailToSendCreate() throws Exception {
        zeTask = tasksSchemas.newTask();
        LocalDate zeDate = new LocalDate(2017, 03, 24);
        LocalDateTime zeDateTime = new LocalDateTime(2017, 03, 24, 0, 0, 0);
        EmailAddress zeAdresse = new EmailAddress("Gandalf Leblanc", "gandalf@doculibre.com");
        recordServices.add(zeTask.setTitle("taskTitle"));
        borrowingServices.borrowRecordsFromTask(zeTask.getId(), zeDate, zeDate, users.charlesIn(zeCollection), users.gandalfIn(zeCollection), BorrowingType.BORROW);
        EmailToSend emailToSend = getEmailToSend(ALERT_BORROWED);
        assertThat(emailToSend).isNotNull();
        assertThat(emailToSend.getTo()).isEqualTo(asList(zeAdresse));
        assertThat(emailToSend.getSendOn()).isEqualTo(zeDateTime);
        assertThat(emailToSend.getSubject()).isEqualTo("Alerte lorsque le document est emprunté : taskTitle");
        assertThat(emailToSend.getParameters()).hasSize(10);
    }

    @Test
    public void givenRecordsReturnedFromTaskThenValidateEmailToSendCreate() throws Exception {
        zeTask = tasksSchemas.newTask();
        LocalDate zeDate = new LocalDate(2017, 03, 24);
        LocalDateTime zeDateTime = new LocalDateTime(2017, 03, 24, 0, 0, 0);
        EmailAddress zeAdresse = new EmailAddress("Charles-François Xavier", "charles@doculibre.com");
        recordServices.add(zeTask.setTitle("taskTitle"));
        borrowingServices.returnRecordsFromTask(zeTask.getId(), users.charlesIn(zeCollection), zeDate);
        EmailToSend emailToSend = getEmailToSend(ALERT_RETURNED);
        assertThat(emailToSend).isNotNull();
        assertThat(emailToSend.getTo()).isEqualTo(asList(zeAdresse));
        assertThat(emailToSend.getSendOn()).isEqualTo(zeDateTime);
        assertThat(emailToSend.getSubject()).isEqualTo("Alerte lorsque le document est emprunté : taskTitle");
        assertThat(emailToSend.getParameters()).hasSize(7);
    }

    private EmailToSend getEmailToSend(String template) {
        LogicalSearchCondition condition = from(emailToSendSchema)
                .where(tasksSchemas.emailToSend().getMetadata(EmailToSend.TEMPLATE))
                .isEqualTo(template);
        Record emailRecord = searchServices.searchSingleResult(condition);
        if (emailRecord != null) {
            return tasksSchemas.wrapEmailToSend(emailRecord);
        } else {
            return null;
        }
    }

    @Test
    public void givenSemiActiveFolderWhenBorrowItThenOk()
            throws Exception {

        givenBorrowedFolderC30ByAdmin();

        Folder folderC30 = records.getFolder_C30();
        assertThat(folderC30.getArchivisticStatus()).isEqualTo(FolderStatus.SEMI_ACTIVE);
        assertThat(folderC30.getBorrowed()).isTrue();
        assertThat(folderC30.getBorrowDate().toLocalDate()).isEqualTo(nowDate);
        assertThat(folderC30.getBorrowReturnDate()).isNull();
        assertThat(folderC30.getBorrowUser()).isEqualTo(records.getAdmin().getId());
        assertThat(folderC30.getBorrowUserEntered()).isEqualTo(records.getAdmin().getId());
        assertThat(folderC30.getBorrowType()).isEqualTo(BorrowingType.BORROW);
        assertThat(
                searchServices.getResultsCount(rmEventsSearchServices.newFindCurrentlyBorrowedFoldersQuery(records.getAdmin())))
                .isEqualTo(1);
    }

    @Test(expected = BorrowingServicesRunTimeException_InvalidBorrowingDate.class)
    public void givenSemiActiveFolderWithAFutureBorrowingDateWhenBorrowItThenCannotBorrow()
            throws Exception {

        LocalDate previewReturnDate = nowDate.plusDays(15);
        Folder folderC30 = records.getFolder_C30();

        try {
            borrowingServices
                    .borrowFolder(folderC30.getId(), nowDate.plusDays(1), previewReturnDate,
                            records.getAdmin(),
                            records.getAdmin(),
                            BorrowingType.BORROW, true);
        } finally {
            folderC30 = records.getFolder_C30();
            assertThat(folderC30.getArchivisticStatus()).isEqualTo(FolderStatus.SEMI_ACTIVE);
            assertThat(folderC30.getBorrowed()).isNull();
            assertThat(folderC30.getBorrowDate()).isNull();
            assertThat(folderC30.getBorrowReturnDate()).isNull();
            assertThat(folderC30.getBorrowUser()).isNull();
            assertThat(folderC30.getBorrowUserEntered()).isNull();
            assertThat(folderC30.getBorrowType()).isNull();
            assertThat(searchServices
                    .getResultsCount(rmEventsSearchServices.newFindCurrentlyBorrowedFoldersQuery(records.getAdmin())))
                    .isEqualTo(0);
        }
    }

    @Test
    public void givenInactiveFolderWhenBorrowItThenOk()
            throws Exception {

        LocalDate previewReturnDate = nowDate.plusDays(15);
        Folder folderA94 = records.getFolder_A94();

        borrowingServices
                .borrowFolder(folderA94.getId(), nowDate, previewReturnDate, records.getAdmin(),
                        records.getAlice(),
                        BorrowingType.CONSULTATION, true);
        folderA94 = records.getFolder_A94();

        assertThat(folderA94.getArchivisticStatus()).isEqualTo(FolderStatus.INACTIVE_DEPOSITED);
        assertThat(folderA94.getBorrowed()).isTrue();
        assertThat(folderA94.getBorrowDate().toLocalDate()).isEqualTo(nowDate);
        assertThat(folderA94.getBorrowReturnDate()).isNull();
        assertThat(folderA94.getBorrowUser()).isEqualTo(records.getAdmin().getId());
        assertThat(folderA94.getBorrowUserEntered()).isEqualTo(records.getAlice().getId());
        assertThat(folderA94.getBorrowType()).isEqualTo(BorrowingType.CONSULTATION);
        assertThat(
                searchServices.getResultsCount(rmEventsSearchServices.newFindCurrentlyBorrowedFoldersQuery(records.getAdmin())))
                .isEqualTo(0);
    }

    @Test(expected = BorrowingServicesRunTimeException_CannotBorrowActiveFolder.class)
    public void givenActiveFolderWhenBorrowItThenCannotBorrow()
            throws Exception {

        LocalDate previewReturnDate = nowDate.plusDays(15);
        Folder folderA16 = records.getFolder_A16();

        try {
            borrowingServices.borrowFolder(folderA16.getId(), nowDate, previewReturnDate, records.getAdmin(),
                    records.getAdmin(),
                    BorrowingType.BORROW, true);
        } finally {
            folderA16 = records.getFolder_A16();
            assertThat(folderA16.getArchivisticStatus()).isEqualTo(FolderStatus.ACTIVE);
            assertThat(folderA16.getBorrowed()).isNull();
            assertThat(folderA16.getBorrowDate()).isNull();
            assertThat(folderA16.getBorrowReturnDate()).isNull();
            assertThat(folderA16.getBorrowUser()).isNull();
            assertThat(folderA16.getBorrowUserEntered()).isNull();
            assertThat(folderA16.getBorrowType()).isNull();
            assertThat(searchServices
                    .getResultsCount(rmEventsSearchServices.newFindCurrentlyBorrowedFoldersQuery(records.getAdmin())))
                    .isEqualTo(0);
        }
    }

    @Test(expected = BorrowingServicesRunTimeException_FolderIsAlreadyBorrowed.class)
    public void givenBorrowedFolderWhenBorrowItThenCannotBorrow()
            throws Exception {

        LocalDate previewReturnDate = nowDate.plusDays(15);
        Folder folderC30 = records.getFolder_C30();
        borrowingServices
                .borrowFolder(folderC30.getId(), nowDate, previewReturnDate, records.getAdmin(),
                        records.getAdmin(),
                        BorrowingType.BORROW, true);

        try {
            borrowingServices.borrowFolder(folderC30.getId(), nowDate, previewReturnDate, records.getAdmin(),
                    records.getAdmin(),
                    BorrowingType.BORROW, true);
        } finally {
            folderC30 = records.getFolder_C30();
            assertThat(folderC30.getArchivisticStatus()).isEqualTo(FolderStatus.SEMI_ACTIVE);
            assertThat(folderC30.getBorrowed()).isTrue();
            assertThat(folderC30.getBorrowDate().toLocalDate()).isEqualTo(nowDate);
            assertThat(folderC30.getBorrowReturnDate()).isNull();
            assertThat(folderC30.getBorrowUser()).isEqualTo(records.getAdmin().getId());
            assertThat(folderC30.getBorrowUserEntered()).isEqualTo(records.getAdmin().getId());
            assertThat(folderC30.getBorrowType()).isEqualTo(BorrowingType.BORROW);
            assertThat(searchServices
                    .getResultsCount(rmEventsSearchServices.newFindCurrentlyBorrowedFoldersQuery(records.getAdmin())))
                    .isEqualTo(1);
        }
    }

    //
    @Test
    public void givenSemiActiveFolderWhenBorrowItWithAPastDateThenOk()
            throws Exception {

        LocalDate previewReturnDate = nowDate.plusDays(15);
        LocalDate borrowDate = nowDate.minusDays(5);
        borrowingServices
                .borrowFolder(records.getFolder_C30().getId(), borrowDate, previewReturnDate, records.getAdmin(),
                        records.getAdmin(),
                        BorrowingType.BORROW, true);

        Folder folderC30 = records.getFolder_C30();
        assertThat(folderC30.getArchivisticStatus()).isEqualTo(FolderStatus.SEMI_ACTIVE);
        assertThat(folderC30.getBorrowed()).isTrue();
        assertThat(folderC30.getBorrowDate().toLocalDate()).isEqualTo(borrowDate);
        assertThat(folderC30.getBorrowReturnDate()).isNull();
        assertThat(folderC30.getBorrowUser()).isEqualTo(records.getAdmin().getId());
        assertThat(folderC30.getBorrowUserEntered()).isEqualTo(records.getAdmin().getId());
        assertThat(folderC30.getBorrowType()).isEqualTo(BorrowingType.BORROW);
        assertThat(
                searchServices.getResultsCount(rmEventsSearchServices.newFindCurrentlyBorrowedFoldersQuery(records.getAdmin())))
                .isEqualTo(1);
    }

    //Return folder

    @Test
    public void whenReturnFolderThenOk()
            throws Exception {

        givenBorrowedFolderC30ByAdmin();
        Folder folderC30 = records.getFolder_C30();

        nowDate = nowDate.plusDays(1);
        givenTimeIs(nowDate);
        borrowingServices.returnFolder(folderC30.getId(), records.getAdmin(), nowDate, true);
        folderC30 = records.getFolder_C30();

        assertThat(folderC30.getArchivisticStatus()).isEqualTo(FolderStatus.SEMI_ACTIVE);
        assertThat(folderC30.getBorrowed()).isNull();
        assertThat(folderC30.getBorrowDate()).isNull();
        assertThat(folderC30.getBorrowReturnDate()).isNull();
        assertThat(folderC30.getBorrowUser()).isNull();
        assertThat(folderC30.getBorrowUserEntered()).isNull();
        assertThat(folderC30.getBorrowType()).isNull();
        assertThat(
                searchServices.getResultsCount(rmEventsSearchServices.newFindCurrentlyBorrowedFoldersQuery(records.getAdmin())))
                .isEqualTo(0);
        recordServices.flush();
        List<Record> records = searchServices.search(rmEventsSearchServices.newFindReturnedFoldersByDateRangeQuery(
                this.records.getAdmin(),
                TimeProvider.getLocalDateTime().minusDays(1), TimeProvider.getLocalDateTime().plusDays(1)));
        assertThat(records).hasSize(1);
        Event event = new Event(records.get(0), getSchemaTypes());
        assertThat(event.getUsername()).isEqualTo(this.records.getAdmin().getUsername());
        assertThat(event.getType()).isEqualTo(EventType.RETURN_FOLDER);
        assertThat(event.getCreatedOn().toLocalDate()).isEqualTo(nowDate);
    }

    @Test
    public void givenAdminUserBorrowFolderToUserWhenHeReturnsFolderThenOk()
            throws Exception {

        LocalDate previewReturnDate = nowDate.plusDays(15);
        borrowingServices
                .borrowFolder(records.getFolder_C30().getId(), nowDate, previewReturnDate, records.getAdmin(),
                        records.getBob_userInAC(),
                        BorrowingType.BORROW, true);
        Folder folderC30 = records.getFolder_C30();

        nowDate = nowDate.plusDays(1);
        givenTimeIs(nowDate);
        borrowingServices.returnFolder(folderC30.getId(), records.getBob_userInAC(), nowDate, true);
        folderC30 = records.getFolder_C30();

        assertThat(folderC30.getArchivisticStatus()).isEqualTo(FolderStatus.SEMI_ACTIVE);
        assertThat(folderC30.getBorrowed()).isNull();
        assertThat(folderC30.getBorrowDate()).isNull();
        assertThat(folderC30.getBorrowReturnDate()).isNull();
        assertThat(folderC30.getBorrowUser()).isNull();
        assertThat(folderC30.getBorrowUserEntered()).isNull();
        assertThat(folderC30.getBorrowType()).isNull();
        assertThat(
                searchServices.getResultsCount(rmEventsSearchServices.newFindCurrentlyBorrowedFoldersQuery(records.getAdmin())))
                .isEqualTo(0);
        recordServices.flush();
        List<Record> records = searchServices.search(rmEventsSearchServices.newFindReturnedFoldersByDateRangeQuery(
                this.records.getAdmin(),
                TimeProvider.getLocalDateTime().minusDays(1), TimeProvider.getLocalDateTime().plusDays(1)));
        assertThat(records).hasSize(1);
        Event event = new Event(records.get(0), getSchemaTypes());
        assertThat(event.getUsername()).isEqualTo(this.records.getBob_userInAC().getUsername());
        assertThat(event.getType()).isEqualTo(EventType.RETURN_FOLDER);
        assertThat(event.getCreatedOn().toLocalDate()).isEqualTo(nowDate);
    }

    @Test
    public void givenAdminUserBorrowFolderToBobWhenAdminReturnsFolderThenOk()
            throws Exception {

        LocalDate previewReturnDate = nowDate.plusDays(15);
        borrowingServices
                .borrowFolder(records.getFolder_C30().getId(), nowDate, previewReturnDate, records.getAdmin(),
                        records.getBob_userInAC(),
                        BorrowingType.BORROW, true);
        Folder folderC30 = records.getFolder_C30();

        nowDate = nowDate.plusDays(1);
        givenTimeIs(nowDate);
        borrowingServices.returnFolder(folderC30.getId(), records.getAdmin(), nowDate, true);
        folderC30 = records.getFolder_C30();

        assertThat(folderC30.getArchivisticStatus()).isEqualTo(FolderStatus.SEMI_ACTIVE);
        assertThat(folderC30.getBorrowed()).isNull();
        assertThat(folderC30.getBorrowDate()).isNull();
        assertThat(folderC30.getBorrowReturnDate()).isNull();
        assertThat(folderC30.getBorrowUser()).isNull();
        assertThat(folderC30.getBorrowUserEntered()).isNull();
        assertThat(
                searchServices.getResultsCount(rmEventsSearchServices.newFindCurrentlyBorrowedFoldersQuery(records.getAdmin())))
                .isEqualTo(0);

        recordServices.flush();
        List<Record> records = searchServices.search(rmEventsSearchServices.newFindReturnedFoldersByDateRangeQuery(
                this.records.getAdmin(),
                TimeProvider.getLocalDateTime().minusDays(1), TimeProvider.getLocalDateTime().plusDays(1)));
        assertThat(records).hasSize(1);
        Event event = new Event(records.get(0), getSchemaTypes());
        assertThat(event.getUsername()).isEqualTo(this.records.getAdmin().getUsername());
        assertThat(event.getType()).isEqualTo(EventType.RETURN_FOLDER);
        assertThat(event.getCreatedOn().toLocalDate()).isEqualTo(nowDate);
    }

    @Test(expected = BorrowingServicesRunTimeException_UserNotAllowedToReturnFolder.class)
    public void whenReturnFolderWithDifferentUserWithoutRGDRoleThenException()
            throws Exception {

        givenBorrowedFolderC30ByAdmin();
        Folder folderC30 = records.getFolder_C30();

        nowDate = nowDate.plusDays(1);
        givenTimeIs(nowDate);
        borrowingServices.returnFolder(folderC30.getId(), records.getBob_userInAC(), nowDate, true);
        assertThat(
                searchServices.getResultsCount(rmEventsSearchServices.newFindCurrentlyBorrowedFoldersQuery(records.getAdmin())))
                .isEqualTo(0);
        recordServices.flush();
        List<Record> records = searchServices.search(rmEventsSearchServices.newFindReturnedFoldersByDateRangeQuery(
                this.records.getAdmin(),
                TimeProvider.getLocalDateTime().minusDays(1), TimeProvider.getLocalDateTime().plusDays(1)));
        assertThat(records).isEmpty();
    }

    @Test(expected = BorrowingServicesRunTimeException_FolderIsNotBorrowed.class)
    public void givenNotBorrowedFolderWhenReturnItThenException()
            throws Exception {

        Folder folderC30 = records.getFolder_C30();

        borrowingServices.returnFolder(folderC30.getId(), records.getAdmin(), nowDate, true);

        assertThat(
                searchServices.getResultsCount(rmEventsSearchServices.newFindCurrentlyBorrowedFoldersQuery(records.getAdmin())))
                .isEqualTo(0);
        recordServices.flush();
        List<Record> records = searchServices.search(rmEventsSearchServices.newFindReturnedFoldersByDateRangeQuery(
                this.records.getAdmin(),
                TimeProvider.getLocalDateTime().minusDays(1), TimeProvider.getLocalDateTime().plusDays(1)));
        assertThat(records).isEmpty();
    }

    @Test(expected = BorrowingServicesRunTimeException_FolderIsNotBorrowed.class)
    public void givenFalseInBorrowedStatusFolderWhenReturnItThenException()
            throws Exception {

        Folder folderC30 = records.getFolder_C30();

        recordServices.update(folderC30.setBorrowed(false));

        borrowingServices.returnFolder(folderC30.getId(), records.getAdmin(), nowDate, true);

        assertThat(
                searchServices.getResultsCount(rmEventsSearchServices.newFindCurrentlyBorrowedFoldersQuery(records.getAdmin())))
                .isEqualTo(0);
        recordServices.flush();
        List<Record> records = searchServices.search(rmEventsSearchServices.newFindReturnedFoldersByDateRangeQuery(
                this.records.getAdmin(),
                TimeProvider.getLocalDateTime().minusDays(1), TimeProvider.getLocalDateTime().plusDays(1)));
        assertThat(records).isEmpty();
    }

    private void givenBorrowedFolderC30ByAdmin()
            throws RecordServicesException {
        LocalDate previewReturnDate = nowDate.plusDays(15);
        borrowingServices
                .borrowFolder(records.getFolder_C30().getId(), nowDate, previewReturnDate, records.getAdmin(),
                        records.getAdmin(),
                        BorrowingType.BORROW, true);
    }

    private MetadataSchemaTypes getSchemaTypes() {
        return getModelLayerFactory().getMetadataSchemasManager().getSchemaTypes(zeCollection);
    }
}
