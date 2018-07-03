package com.constellio.app.modules.rm.model.calculators.folder;

import com.constellio.app.modules.rm.RMTestRecords;
import com.constellio.app.modules.rm.model.enums.CopyType;
import com.constellio.app.modules.rm.services.RMSchemasRecordsServices;
import com.constellio.app.modules.rm.wrappers.Folder;
import com.constellio.app.ui.entities.MetadataVO;
import com.constellio.app.ui.framework.builders.MetadataToVOBuilder;
import com.constellio.app.ui.pages.base.SessionContext;
import com.constellio.app.ui.pages.unicitymetadataconf.FolderUnicityMetadataParams;
import com.constellio.app.ui.pages.unicitymetadataconf.FolderUnicityMetadataPresenter;
import com.constellio.app.ui.pages.unicitymetadataconf.FolderUnicityMetadataView;
import com.constellio.model.entities.schemas.Metadata;
import com.constellio.model.services.records.RecordServices;
import com.constellio.model.services.records.RecordServicesException;
import com.constellio.model.services.schemas.MetadataList;
import com.constellio.model.services.schemas.MetadataSchemasManager;
import com.constellio.sdk.tests.ConstellioTest;
import com.constellio.sdk.tests.MockedNavigation;
import com.constellio.sdk.tests.setups.Users;
import org.jetbrains.annotations.NotNull;
import org.joda.time.LocalDate;
import org.joda.time.LocalDateTime;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;

import java.util.Locale;

import static java.util.Arrays.asList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

public class FolderUnicityCalculatorAcceptanceTest extends ConstellioTest {
    RMTestRecords records = new RMTestRecords(zeCollection);
    Users users = new Users();
    RMSchemasRecordsServices rmRecordSchemaManager;
    RecordServices recordServices;

    @Mock
    FolderUnicityMetadataView view;
    MockedNavigation navigator;
    @Mock
    SessionContext sessionContext;

    FolderUnicityMetadataPresenter folderUnicityMetadataPresenter;

    @Before
    public void setUp() {
        prepareSystem(withZeCollection().withConstellioRMModule().withRMTest(records).withAllTest(users));
        rmRecordSchemaManager = new RMSchemasRecordsServices(zeCollection, getAppLayerFactory());

        navigator = new MockedNavigation();

        when(view.getConstellioFactories()).thenReturn(getConstellioFactories());
        when(view.getCollection()).thenReturn(zeCollection);
        when(view.getSessionContext()).thenReturn(sessionContext);
        when(sessionContext.getCurrentCollection()).thenReturn(zeCollection);
        when(sessionContext.getCurrentLocale()).thenReturn(Locale.FRENCH);

        folderUnicityMetadataPresenter = new FolderUnicityMetadataPresenter(view, Folder.DEFAULT_SCHEMA);
        recordServices = getModelLayerFactory().newRecordServices();
    }

    @Test
    public void givenFolderWithMetadataValueAndReferenceSummaryColumnParameterThenSummaryMetadataHaveAValueTest()
            throws Exception {
        FolderUnicityMetadataParams summaryColumnParams = new FolderUnicityMetadataParams();

        summaryColumnParams.setMetadataVO(findMetadata(Folder.ADMINISTRATIVE_UNIT));

        folderUnicityMetadataPresenter.addMetadaForUnicity(summaryColumnParams);

        Folder folder = createFolder();
        recordServices.add(folder);

        Folder resultFolder = rmRecordSchemaManager.getFolder(folder.getId());

        String unicity = resultFolder.get(Folder.UNICITY);

        assertThat(unicity).isEqualTo("unitId_11b");
    }

    @Test
    public void givenFolderWithMetadataValueAndReferenceSummaryColumnTitleParameterThenSummaryMetadataHaveAValueTest()
            throws Exception {
        FolderUnicityMetadataParams folderUnicityMetadataParms = new FolderUnicityMetadataParams();

        folderUnicityMetadataParms.setMetadataVO(findMetadata(Folder.ADMINISTRATIVE_UNIT));

        folderUnicityMetadataPresenter.addMetadaForUnicity(folderUnicityMetadataParms);

        FolderUnicityMetadataParams folderUnicityMetadataParms2 = new FolderUnicityMetadataParams();

        folderUnicityMetadataParms2.setMetadataVO(findMetadata(Folder.FORM_CREATED_ON));

        folderUnicityMetadataPresenter.addMetadaForUnicity(folderUnicityMetadataParms2);

        Folder folder = createFolder();
        folder.setFormCreatedOn(new LocalDateTime(2014, 11, 4,1,1));
        recordServices.add(folder);

        Folder resultFolder = rmRecordSchemaManager.getFolder(folder.getId());

        String unicity = resultFolder.get(Folder.UNICITY);

        assertThat(unicity).isEqualTo("unitId_11b, 1415080860000");
    }

    @Test
    public void givenOneFolderWithValueAndDynamicDependancyThenSummaryMetadataHaveAValueTest()
            throws Exception {
        FolderUnicityMetadataParams folderUnicityMetadata = new FolderUnicityMetadataParams();

        folderUnicityMetadata.setMetadataVO(findMetadata(Folder.TITLE));

        folderUnicityMetadataPresenter.addMetadaForUnicity(folderUnicityMetadata);

        Folder folder = createFolder();
        recordServices.add(folder);

        Folder resultFolder = rmRecordSchemaManager.getFolder(folder.getId());

        String unicity = resultFolder.get(Folder.UNICITY);

        // Les paramèetres sont setter.
        assertThat(unicity).isEqualTo("Ze folder");
    }

    @Test
    public void givenOneFolderWithValueAndDynamicDependancyMultiValueThenSummaryMetadataHaveAValueTest()
            throws Exception {
        FolderUnicityMetadataParams folderUnicityMetadata = new FolderUnicityMetadataParams();

        folderUnicityMetadata.setMetadataVO(findMetadata(Folder.KEYWORDS));

        folderUnicityMetadataPresenter.addMetadaForUnicity(folderUnicityMetadata);

        FolderUnicityMetadataParams folderUnicityMetadata2 = new FolderUnicityMetadataParams();

        folderUnicityMetadata2.setMetadataVO(findMetadata(Folder.OPENING_DATE));

        folderUnicityMetadataPresenter.addMetadaForUnicity(folderUnicityMetadata2);

        Folder folder = createFolder();

        folder.setKeywords(asList("keyword1, keyword2, keyword3"));
        folder.setOpenDate(new LocalDate(2014, 11, 4));

        recordServices.add(folder);

        Folder resultFolder = rmRecordSchemaManager.getFolder(folder.getId());

        String unicity = resultFolder.get(Folder.UNICITY);

        assertThat(unicity).isEqualTo("keyword1, keyword2, keyword3, 1415077200000");
    }

    @Test
    public void givenOneFolderWithValueDynamicDependancyMultiValueAndTwoDynamicDepencySingleThenSummaryMetadataHaveAValueTest()
            throws Exception {

        FolderUnicityMetadataParams folderUnicityMetadata1 = new FolderUnicityMetadataParams();

        folderUnicityMetadata1.setMetadataVO(findMetadata(Folder.KEYWORDS));

        folderUnicityMetadataPresenter.addMetadaForUnicity(folderUnicityMetadata1);

        FolderUnicityMetadataParams folderUnicityMetadata2 = new FolderUnicityMetadataParams();

        folderUnicityMetadata2.setMetadataVO(findMetadata(Folder.ADMINISTRATIVE_UNIT));

        folderUnicityMetadataPresenter.addMetadaForUnicity(folderUnicityMetadata2);

        FolderUnicityMetadataParams folderUnicityMetadata3 = new FolderUnicityMetadataParams();

        folderUnicityMetadata3.setMetadataVO(findMetadata(Folder.DESCRIPTION));

        folderUnicityMetadataPresenter.addMetadaForUnicity(folderUnicityMetadata3);

        Folder folder = createFolder();

        folder.setKeywords(asList("keyword1, keyword2, keyword3"));
        folder.setOpenDate(new LocalDate(2014, 11, 4));

        recordServices.add(folder);

        Folder resultFolder = rmRecordSchemaManager.getFolder(folder.getId());

        String unicity = resultFolder.get(Folder.UNICITY);

        assertThat(unicity).isEqualTo("keyword1, keyword2, keyword3, unitId_11b");
    }

    private MetadataVO findMetadata(String localCode) {
        MetadataSchemasManager schemasManager = getModelLayerFactory().getMetadataSchemasManager();
        MetadataList list = schemasManager.getSchemaTypes(zeCollection).getSchema(Folder.DEFAULT_SCHEMA).getMetadatas();

        MetadataToVOBuilder builder = new MetadataToVOBuilder();
        for (Metadata metadata : list) {
            if (metadata.getLocalCode().equalsIgnoreCase(localCode)) {
                return builder.build(metadata, view.getSessionContext());
            }
        }

        return null;
    }

    @NotNull
    private Folder createFolder() throws RecordServicesException {
        Folder folder = rmRecordSchemaManager.newFolder();

        folder.setAdministrativeUnitEntered(records.unitId_11b);
        folder.setCategoryEntered(records.categoryId_X110);
        folder.setTitle("Ze folder");
        folder.setRetentionRuleEntered(records.ruleId_2);
        folder.setCopyStatusEntered(CopyType.PRINCIPAL);
        folder.setOpenDate(new LocalDate(2014, 11, 4));

        return folder;
    }
}