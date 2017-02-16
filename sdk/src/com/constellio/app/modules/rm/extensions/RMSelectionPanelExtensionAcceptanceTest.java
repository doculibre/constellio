package com.constellio.app.modules.rm.extensions;

import com.constellio.app.api.extensions.params.AvailableActionsParam;
import com.constellio.app.modules.rm.RMTestRecords;
import com.constellio.app.modules.rm.wrappers.Document;
import com.constellio.app.modules.rm.wrappers.Folder;
import com.constellio.app.services.factories.AppLayerFactory;
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
    }

    @Test
    public void givenMoveButtonClickedThenMovedCorrectly() throws RecordServicesException {
        AvailableActionsParam param = buildParamWithDocumentsAndFolders();
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
        AvailableActionsParam param = buildParamWithDocumentsAndFolders();
        extension.addAvailableActions(param);
        assertThatRecords(records.getDocumentWithContent_A79(), records.getDocumentWithContent_B33()).extracting(Document.FOLDER)
                .doesNotContain(records.folder_A20);
        assertThatRecords(records.getFolder_A01(), records.getFolder_A02()).extracting(Folder.PARENT_FOLDER)
                .doesNotContain(records.folder_A20);
        List<String> existingIds = getModelLayerFactory().newSearchServices().searchRecordIds(new LogicalSearchQuery().setCondition(fromAllSchemasIn(zeCollection).
                where(Schemas.PATH).isStartingWithText(records.getFolder_A20().getPaths().get(0))
        ));

        extension.duplicateButtonClicked(records.folder_A20, param.getIds());
        assertThatRecords(records.getDocumentWithContent_A79(), records.getDocumentWithContent_B33()).extracting(Document.FOLDER)
                .doesNotContain(records.folder_A20);
        assertThatRecords(records.getFolder_A01(), records.getFolder_A02()).extracting(Folder.PARENT_FOLDER)
                .doesNotContain(records.folder_A20);

        long numberOfNewDocumentAndFolderInFolderA20 = getModelLayerFactory().newSearchServices().getResultsCount(new LogicalSearchQuery().setCondition(fromAllSchemasIn(zeCollection)
                .whereAllConditions(
                        where(Schemas.PATH).isStartingWithText(records.getFolder_A20().getPaths().get(0)),
                        where(Schemas.IDENTIFIER).isNotIn(existingIds)
                )
        ));
        assertThat(numberOfNewDocumentAndFolderInFolderA20).isEqualTo(4);
    }

    public AvailableActionsParam buildParamWithDocumentsAndFolders() {
        return new AvailableActionsParam(asList(records.document_A79, records.document_B33, records.folder_A01, records.folder_A02),
                asList(Document.SCHEMA_TYPE, Folder.SCHEMA_TYPE), records.getAdmin(), layout);
    }

    public AvailableActionsParam buildParamWithDocumentsOnly() {
        return new AvailableActionsParam(asList(records.document_A79, records.document_B33),
                asList(Document.SCHEMA_TYPE), records.getAdmin(), layout);
    }

    public AvailableActionsParam buildParamWithFoldersOnly() {
        return new AvailableActionsParam(asList(records.folder_A01, records.folder_A02),
                asList(Folder.SCHEMA_TYPE), records.getAdmin(), layout);
    }
}
