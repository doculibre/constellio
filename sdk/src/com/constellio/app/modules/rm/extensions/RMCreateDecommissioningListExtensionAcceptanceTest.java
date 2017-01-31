package com.constellio.app.modules.rm.extensions;

import com.constellio.app.modules.rm.RMEmailTemplateConstants;
import com.constellio.app.modules.rm.RMTestRecords;
import com.constellio.app.modules.rm.model.enums.DecommissioningListType;
import com.constellio.app.modules.rm.services.RMSchemasRecordsServices;
import com.constellio.app.modules.rm.wrappers.DecommissioningList;
import com.constellio.model.entities.records.Record;
import com.constellio.model.entities.records.wrappers.EmailToSend;
import com.constellio.model.entities.structures.EmailAddress;
import com.constellio.model.services.migrations.ConstellioEIMConfigs;
import com.constellio.model.services.records.RecordServices;
import com.constellio.model.services.records.RecordServicesException;
import com.constellio.model.services.search.SearchServices;
import com.constellio.model.services.search.query.logical.condition.LogicalSearchCondition;
import com.constellio.model.services.users.UserServices;
import com.constellio.sdk.tests.ConstellioTest;
import com.constellio.sdk.tests.setups.Users;
import org.joda.time.LocalDate;
import org.joda.time.LocalDateTime;
import org.junit.Before;
import org.junit.Test;

import java.util.HashSet;
import java.util.Set;

import static com.constellio.model.services.search.query.logical.LogicalSearchQueryOperators.from;
import static com.constellio.model.services.search.query.logical.LogicalSearchQueryOperators.where;
import static java.util.Arrays.asList;
import static org.assertj.core.api.Assertions.assertThat;

public class RMCreateDecommissioningListExtensionAcceptanceTest extends ConstellioTest {
    private RMCreateDecommissioningListExtension rmCreateDecommissioningListExtension;

    Users users = new Users();
    RecordServices recordServices;
    UserServices userServices;
    ConstellioEIMConfigs eimConfigs;
    String constellioUrl;
    RMSchemasRecordsServices rm;
    private SearchServices searchServices;
    private LocalDateTime now = LocalDateTime.now();
    private DecommissioningList decommissioningList;
    private DecommissioningList documentDecommissioningList;
    private LocalDate newStartDate = LocalDate.now().minusDays(2);
    RMTestRecords records = new RMTestRecords(zeCollection);

    @Before
    public void setUp()
            throws Exception {
        givenTimeIs(now);
        prepareSystem(
                withZeCollection().withConstellioRMModule().withAllTestUsers()
                        .withRMTest(records).withFoldersAndContainersOfEveryStatus().withDocumentsDecommissioningList()
        );

        recordServices = getModelLayerFactory().newRecordServices();
        searchServices = getModelLayerFactory().newSearchServices();
        rmCreateDecommissioningListExtension = new RMCreateDecommissioningListExtension(zeCollection, getModelLayerFactory());
        userServices = getModelLayerFactory().newUserServices();
        eimConfigs = new ConstellioEIMConfigs(getModelLayerFactory().getSystemConfigurationsManager());
        constellioUrl = eimConfigs.getConstellioUrl();
        rm = new RMSchemasRecordsServices(zeCollection, getAppLayerFactory());
        initTasks();

    }

    private void initTasks()
            throws RecordServicesException {
        decommissioningList = rm.newDecommissioningListWithId("decommissioningListTest").setTitle("decommissioningListTest").setDecommissioningListType(DecommissioningListType.FOLDERS_TO_TRANSFER);
        documentDecommissioningList = rm.newDecommissioningListWithId("documentDecommissioningListTest").setTitle("documentDecommissioningListTest").setDecommissioningListType(DecommissioningListType.DOCUMENTS_TO_TRANSFER);
    }

    @Test
    public void givenFolderDecommissioningListThenEmailToSendParametersAreOk()
            throws RecordServicesException {
        recordServices.add(decommissioningList);
        recordServices.flush();
        EmailToSend emailToSend = getEmailToSendForFolderDecommissioningList();
        assertThat(getEmailToSendForDocumentDecommissioningList()).isNull();
        assertThat(emailToSend).isNotNull();
        assertThat(emailToSend.getTemplate()).isEqualTo(RMEmailTemplateConstants.DECOMMISSIONING_LIST_CREATION_TEMPLATE_ID);
        assertThat(emailToSend.getFrom()).isNull();
        assertThat(emailToSend.getSendOn()).isEqualTo(now);
        assertThat(emailToSend.getTo().size()).isEqualTo(2);

        final Set<String> expectedRecipients = new HashSet<>();
        for (EmailAddress emailAddress : emailToSend.getTo()) {
            expectedRecipients.add(emailAddress.getEmail());
        }
    }

    @Test
    public void givenDocumentDecommissioningListThenEmailToSendParametersAreOk()
            throws RecordServicesException {
        recordServices.add(documentDecommissioningList);
        recordServices.flush();
        EmailToSend emailToSend = getEmailToSendForDocumentDecommissioningList();
        assertThat(getEmailToSendForFolderDecommissioningList()).isNull();
        assertThat(emailToSend).isNotNull();
        assertThat(emailToSend.getTemplate()).isEqualTo(RMEmailTemplateConstants.DECOMMISSIONING_LIST_CREATION_TEMPLATE_ID);
        assertThat(emailToSend.getFrom()).isNull();
        assertThat(emailToSend.getSendOn()).isEqualTo(now);
        assertThat(emailToSend.getTo().size()).isEqualTo(2);

        final Set<String> expectedRecipients = new HashSet<>();
        for (EmailAddress emailAddress : emailToSend.getTo()) {
            expectedRecipients.add(emailAddress.getEmail());
        }
    }

    private EmailToSend getEmailToSendForFolderDecommissioningList() {
        LogicalSearchCondition condition = from(rm.emailToSend())
                .whereAllConditions(
                        where(rm.emailToSend().getMetadata(EmailToSend.TEMPLATE)).isEqualTo(RMEmailTemplateConstants.DECOMMISSIONING_LIST_CREATION_TEMPLATE_ID),
                        where(rm.emailToSend().getMetadata(EmailToSend.PARAMETERS)).isContaining(asList("title" + EmailToSend.PARAMETER_SEPARATOR + "decommissioningListTest"))
                );
        Record emailRecord = searchServices.searchSingleResult(condition);
        if (emailRecord != null) {
            return rm.wrapEmailToSend(emailRecord);
        } else {
            return null;
        }
    }

    private EmailToSend getEmailToSendForDocumentDecommissioningList() {
        LogicalSearchCondition condition = from(rm.emailToSend())
                .whereAllConditions(
                        where(rm.emailToSend().getMetadata(EmailToSend.TEMPLATE)).isEqualTo(RMEmailTemplateConstants.DECOMMISSIONING_LIST_CREATION_TEMPLATE_ID),
                        where(rm.emailToSend().getMetadata(EmailToSend.PARAMETERS)).isContaining(asList("title" + EmailToSend.PARAMETER_SEPARATOR + "documentDecommissioningListTest"))
                );
        Record emailRecord = searchServices.searchSingleResult(condition);
        if (emailRecord != null) {
            return rm.wrapEmailToSend(emailRecord);
        } else {
            return null;
        }
    }

}
