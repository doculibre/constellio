package com.constellio.app.modules.rm.extensions;

import com.constellio.app.api.extensions.params.AvailableActionsParam;
import com.constellio.app.modules.rm.RMTestRecords;
import com.constellio.app.modules.rm.services.RMSchemasRecordsServices;
import com.constellio.app.modules.rm.wrappers.ContainerRecord;
import com.constellio.app.modules.rm.wrappers.Document;
import com.constellio.app.modules.rm.wrappers.Folder;
import com.constellio.app.modules.rm.wrappers.RMUserFolder;
import com.constellio.app.services.factories.AppLayerFactory;
import com.constellio.model.entities.records.Record;
import com.constellio.model.entities.records.Transaction;
import com.constellio.model.entities.records.wrappers.User;
import com.constellio.model.entities.records.wrappers.UserDocument;
import com.constellio.model.entities.records.wrappers.UserFolder;
import com.constellio.model.entities.schemas.Schemas;
import com.constellio.model.services.records.RecordServicesException;
import com.constellio.model.services.search.query.logical.LogicalSearchQuery;
import com.constellio.sdk.tests.ConstellioTest;
import com.constellio.sdk.tests.FakeSessionContext;
import com.vaadin.ui.VerticalLayout;
import org.junit.Before;
import org.junit.Test;

import java.util.List;

import static com.constellio.model.services.search.query.logical.LogicalSearchQueryOperators.fromAllSchemasIn;
import static com.constellio.model.services.search.query.logical.LogicalSearchQueryOperators.where;
import static com.constellio.sdk.tests.TestUtils.assertThatRecords;
import static java.util.Arrays.asList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.groups.Tuple.tuple;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.*;

/**
 * Created by Constellio on 2017-02-16.
 */
public class RMSelectionPanelExtensionAcceptanceTest extends ConstellioTest {
    AppLayerFactory appLayerFactory;
    RMTestRecords records = new RMTestRecords(zeCollection);
    VerticalLayout layout = new VerticalLayout();
    RMSelectionPanelExtension extension;

    @Before
    public void setup() {
        prepareSystem(
                withZeCollection().withConstellioRMModule().withConstellioESModule().withAllTestUsers()
                        .withRMTest(records).withFoldersAndContainersOfEveryStatus().withDocumentsDecommissioningList().withDocumentsHavingContent()
        );
        appLayerFactory = getAppLayerFactory();
        extension = spy(new RMSelectionPanelExtension(appLayerFactory, zeCollection));
        doReturn(FakeSessionContext.adminInCollection(zeCollection)).when(extension).getSessionContext();
        doNothing().when(extension).addCheckInButton(any(AvailableActionsParam.class));
        doNothing().when(extension).showErrorMessage(any(String.class));
        doNothing().when(extension).deleteUserFolder(any(AvailableActionsParam.class), any(RMUserFolder.class), any(User.class));
        doNothing().when(extension).deleteUserDocument(any(AvailableActionsParam.class), any(UserDocument.class), any(User.class));
        doReturn(getModelLayerFactory().newRecordServices()).when(extension).recordServices();
    }

    @Test
    public void givenMoveButtonClickedThenMovedCorrectly() throws RecordServicesException {
        AvailableActionsParam param = buildParamWithDocumentsAndFoldersAndContainers();
        extension.addAvailableActions(param);
        assertThatRecords(records.getDocumentWithContent_A79(), records.getDocumentWithContent_B33()).extracting(Document.FOLDER)
                .doesNotContain(records.folder_A20);
        assertThatRecords(records.getFolder_A01(), records.getFolder_A02()).extracting(Folder.PARENT_FOLDER)
        .doesNotContain(records.folder_A20);

        extension.parentFolderButtonClicked(records.folder_A20, param.getIds());
        assertThatRecords(records.getDocumentWithContent_A79(), records.getDocumentWithContent_B33()).extracting(Document.FOLDER)
                .containsOnly(records.folder_A20);
        assertThatRecords(records.getFolder_A01(), records.getFolder_A02()).extracting(Folder.PARENT_FOLDER)
                .containsOnly(records.folder_A20);
    }

    @Test
    public void givenDuplicateButtonClickedThenDuplicatedCorrectly() throws RecordServicesException {
        AvailableActionsParam param = buildParamWithDocumentsAndFoldersAndContainers();
        extension.addAvailableActions(param);
        assertThatRecords(records.getDocumentWithContent_A79(), records.getDocumentWithContent_B33()).extracting(Document.FOLDER)
                .doesNotContain(records.folder_A20);
        assertThatRecords(records.getFolder_A01(), records.getFolder_A02()).extracting(Folder.PARENT_FOLDER)
                .doesNotContain(records.folder_A20);
        List<String> existingIds = getModelLayerFactory().newSearchServices().searchRecordIds(new LogicalSearchQuery().setCondition(fromAllSchemasIn(zeCollection).
                where(Schemas.PATH).isStartingWithText(records.getFolder_A20().getPaths().get(0))
        ));

        extension.duplicateButtonClicked(records.folder_A20, param);
        assertThatRecords(records.getDocumentWithContent_A79(), records.getDocumentWithContent_B33()).extracting(Document.FOLDER)
                .doesNotContain(records.folder_A20);
        assertThatRecords(records.getFolder_A01(), records.getFolder_A02()).extracting(Folder.PARENT_FOLDER)
                .doesNotContain(records.folder_A20);

        List<Record> recordList = getModelLayerFactory().newSearchServices().search(new LogicalSearchQuery().setCondition(fromAllSchemasIn(zeCollection)
                .whereAllConditions(
                        where(Schemas.PATH).isStartingWithText(records.getFolder_A20().getPaths().get(0)),
                        where(Schemas.IDENTIFIER).isNotIn(existingIds)
                )
        ));
        assertThat(recordList.size()).isEqualTo(24);
        assertThatRecords(recordList).extractingMetadatas("title").containsOnly(
                tuple("Abeille - Document contrat numérique avec un autre exemplaire"),
                tuple("Aigle - Document procès verbal analogique avec un autre exemplaire"),
                tuple("Aigle (Copie)"), tuple("Abeille - Petit guide"),
                tuple("Abeille - Document procès verbal analogique avec un autre exemplaire"),
                tuple("Aigle - Document contrat analogique avec un autre exemplaire"),
                tuple("Aigle - Document numérique avec le même exemplaire"),
                tuple("Abeille (Copie)"), tuple("Abeille - Livre de recettes"), tuple("Aigle - Petit guide"),
                tuple("Aigle - Document procès verbal numérique avec un autre exemplaire"),
                tuple("Poire.odt (Copie)"), tuple("Abeille - Typologie"), tuple("Aigle - Histoire"), tuple("Abeille - Histoire"),
                tuple("Aigle - Document contrat numérique avec un autre exemplaire"),
                tuple("Abeille - Document analogique avec le même exemplaire"),
                tuple("Abeille - Document numérique avec le même exemplaire"),
                tuple("Aigle - Document analogique avec le même exemplaire"),
                tuple("Abeille - Document procès verbal numérique avec un autre exemplaire"), tuple("Lynx.odt (Copie)"),
                tuple("Aigle - Livre de recettes"), tuple("Abeille - Document contrat analogique avec un autre exemplaire"),
                tuple("Aigle - Typologie"));
    }

    @Test
    public void givenClassifyButtonClickedInFolderThenClassifedCorrectly() throws RecordServicesException {
        buildUserDocumentsAndUserFolders();
        AvailableActionsParam param = buildParamWithUserDocumentsAndUserFolders();
        extension.addAvailableActions(param);

        List<String> existingIds = getModelLayerFactory().newSearchServices().searchRecordIds(new LogicalSearchQuery().setCondition(fromAllSchemasIn(zeCollection).
                where(Schemas.PATH).isStartingWithText(records.getFolder_A20().getPaths().get(0))
        ));
        extension.classifyButtonClicked(records.folder_A20, "", null, true, param);

        List<Record> recordList = getModelLayerFactory().newSearchServices().search(new LogicalSearchQuery().setCondition(fromAllSchemasIn(zeCollection)
                .whereAllConditions(
                        where(Schemas.PATH).isStartingWithText(records.getFolder_A20().getPaths().get(0)),
                        where(Schemas.IDENTIFIER).isNotIn(existingIds)
                )
        ));

        assertThat(recordList.size()).isEqualTo(7);
        assertThatRecords(recordList).extractingMetadatas("title").containsOnly(tuple("UDoc1"), tuple("UDoc2"), tuple("UFol1"),
                tuple("UFol2"), tuple("USubFol1"), tuple("USubDoc1"), tuple("USubDoc2"));
    }

    @Test
    public void givenClassifyButtonClickedInCategoryThenClassifedCorrectly() throws RecordServicesException {
        buildUserDocumentsAndUserFolders();
        AvailableActionsParam param = buildParamWithUserFolders();
        extension.addAvailableActions(param);

        List<String> existingIds = getModelLayerFactory().newSearchServices().searchRecordIds(new LogicalSearchQuery().setCondition(fromAllSchemasIn(zeCollection).
                where(Schemas.PATH).isStartingWithText(records.getCategory_X().getPaths().get(0))
        ));
        extension.classifyButtonClicked("", records.categoryId_X, records.ruleId_1, false, param);

        List<Record> recordList = getModelLayerFactory().newSearchServices().search(new LogicalSearchQuery().setCondition(fromAllSchemasIn(zeCollection)
                .whereAllConditions(
                        where(Schemas.PATH).isStartingWithText(records.getCategory_X().getPaths().get(0)),
                        where(Schemas.IDENTIFIER).isNotIn(existingIds)
                )
        ));

        assertThat(recordList.size()).isEqualTo(5);
        assertThatRecords(recordList).extractingMetadatas("title").containsOnly(tuple("UFol1"),
                tuple("UFol2"), tuple("USubFol1"), tuple("USubDoc1"), tuple("USubDoc2"));
    }

    public AvailableActionsParam buildParamWithDocumentsAndFoldersAndContainers() {
        return new AvailableActionsParam(asList(records.document_A79, records.document_B33, records.folder_A01, records.folder_A02, records.containerId_bac01, records.containerId_bac02),
                asList(Document.SCHEMA_TYPE, Folder.SCHEMA_TYPE, ContainerRecord.SCHEMA_TYPE), records.getAdmin(), layout);
    }

    public void buildUserDocumentsAndUserFolders() throws RecordServicesException {
        RMSchemasRecordsServices rm = new RMSchemasRecordsServices(zeCollection, appLayerFactory);
        Transaction transaction = new Transaction();
        transaction.add(rm.newUserDocumentWithId("UDoc1").setContent(records.getDocumentWithContent_A19().getContent()).setTitle("UDoc1"));
        transaction.add(rm.newUserDocumentWithId("UDoc2").setContent(records.getDocumentWithContent_A19().getContent()).setTitle("UDoc2"));
        transaction.add(rm.newUserFolderWithId("UFol1").setTitle("UFol1"));
        transaction.add(rm.newUserFolderWithId("UFol2").setTitle("UFol2"));
        transaction.add(rm.newUserFolderWithId("USubFol1").setParent("UFol2").setTitle("USubFol1"));
        transaction.add(rm.newUserDocumentWithId("USubDoc1").setUserFolder("USubFol1").setContent(records.getDocumentWithContent_A19().getContent()).setTitle("USubDoc1"));
        transaction.add(rm.newUserDocumentWithId("USubDoc2").setUserFolder("USubFol1").setContent(records.getDocumentWithContent_A19().getContent()).setTitle("USubDoc2"));
        appLayerFactory.getModelLayerFactory().newRecordServices().execute(transaction);
    }

    public AvailableActionsParam buildParamWithUserDocumentsAndUserFolders() {
        return new AvailableActionsParam(asList("UDoc1", "UDoc2", "UFol1", "UFol2"),
                asList(UserDocument.SCHEMA_TYPE, UserFolder.SCHEMA_TYPE), records.getAdmin(), layout);
    }

    public AvailableActionsParam buildParamWithUserFolders() {
        return new AvailableActionsParam(asList("UFol1", "UFol2"),
                asList(UserFolder.SCHEMA_TYPE), records.getAdmin(), layout);
    }
}
