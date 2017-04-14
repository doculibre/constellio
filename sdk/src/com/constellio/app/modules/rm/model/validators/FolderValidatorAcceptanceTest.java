package com.constellio.app.modules.rm.model.validators;

import com.constellio.app.modules.rm.RMTestRecords;
import com.constellio.app.modules.rm.services.RMSchemasRecordsServices;
import com.constellio.app.modules.rm.wrappers.Folder;
import com.constellio.model.entities.records.Record;
import com.constellio.model.entities.schemas.Schemas;
import com.constellio.model.services.records.RecordServices;
import com.constellio.model.services.records.RecordServicesException;
import com.constellio.model.services.search.SearchServices;
import com.constellio.sdk.tests.ConstellioTest;
import org.joda.time.LocalDate;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;
import org.junit.Before;
import org.junit.Test;

import static com.constellio.model.services.search.query.logical.LogicalSearchQueryOperators.from;
import static org.assertj.core.api.Assertions.assertThat;

/**
 * Created by constellios on 2017-04-06.
 */
public class FolderValidatorAcceptanceTest extends ConstellioTest {
    public static final LocalDate DEFAULT_OPENING_DATE = new LocalDate(2001,01,05);
    public static final LocalDate CLOSING_DATE_ENTERED_BEFORE = new LocalDate(2001,01,04);
    public static final String FOLDER_TITLE = "FolderTest";
    public static final String FOLDER_ID = "folderTest";

    RMTestRecords records = new RMTestRecords(zeCollection);

    RMSchemasRecordsServices rm;

    RecordServices recordServices;

    SearchServices searchServices;

    @Before
    public void setUp() {
        givenBackgroundThreadsEnabled();
        prepareSystem(
                withZeCollection().withConstellioRMModule().withAllTestUsers()
                        .withRMTest(records).withFoldersAndContainersOfEveryStatus().withDocumentsDecommissioningList()
        );

        rm = new RMSchemasRecordsServices(zeCollection, getAppLayerFactory());
        recordServices = getModelLayerFactory().newRecordServices();
        searchServices = getModelLayerFactory().newSearchServices();
    }

    @Test(expected = RecordServicesException.ValidationException.class)
    public void testSmallerOpeningDateThanEndingDateThenThrowException() throws RecordServicesException {
        Folder folder = buildDefaultFolder().setCloseDateEntered(CLOSING_DATE_ENTERED_BEFORE);
        recordServices.add(folder.getWrappedRecord());
    }

    @Test
    public void testGreaterOpingDateThanEndingDateThenOk() throws RecordServicesException {
        Folder folder = buildDefaultFolder().setCloseDateEntered(DEFAULT_OPENING_DATE);

        recordServices.add(folder.getWrappedRecord());
        getModelLayerFactory().getBatchProcessesManager().waitUntilAllFinished();
        Record record = searchServices.searchSingleResult(from(rm.folder.schemaType()).where(Schemas.IDENTIFIER).isEqualTo(FOLDER_ID));

        assertThat(rm.wrapFolder(record).getCloseDateEntered()).isEqualTo(DEFAULT_OPENING_DATE);
        assertThat(rm.wrapFolder(record).getOpeningDate()).isEqualTo(DEFAULT_OPENING_DATE);
        assertThat(rm.wrapFolder(record).getTitle()).isEqualTo(FOLDER_TITLE);
    }

    public Folder buildDefaultFolder() {
        return rm.newFolderWithId(FOLDER_ID).setTitle(FOLDER_TITLE).setAdministrativeUnitEntered(records.getUnit10())
                .setRetentionRuleEntered(records.getRule1()).setOpenDate(DEFAULT_OPENING_DATE).setCategoryEntered(records.categoryId_X);
    }

}
