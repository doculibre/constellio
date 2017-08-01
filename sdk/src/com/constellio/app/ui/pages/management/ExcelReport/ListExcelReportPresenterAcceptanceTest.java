package com.constellio.app.ui.pages.management.ExcelReport;

import com.constellio.app.modules.rm.RMTestRecords;
import com.constellio.model.entities.Language;
import com.constellio.model.entities.records.wrappers.Report;
import com.constellio.model.entities.schemas.MetadataSchemaTypes;
import com.constellio.model.services.reports.ReportServices;
import com.constellio.model.services.schemas.builders.MetadataSchemaTypesBuilder;
import com.constellio.sdk.tests.ConstellioTest;
import com.constellio.sdk.tests.FakeSessionContext;
import com.constellio.sdk.tests.MockedNavigation;
import com.constellio.sdk.tests.schemas.FakeDataStoreTypeFactory;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;

import java.util.List;
import java.util.Locale;
import java.util.Map;

import static com.constellio.sdk.tests.TestUtils.asList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

public class ListExcelReportPresenterAcceptanceTest extends ConstellioTest {
    ListExcelReportPresenter presenter;
    @Mock
    ListExcelReportView view;
    @Mock
    MockedNavigation navigator;

    RMTestRecords records = new RMTestRecords(zeCollection);

    @Before
    public void setup() {
        prepareSystem(
                withZeCollection().withConstellioRMModule().withRMTest(records).withFoldersAndContainersOfEveryStatus().withDocumentsHavingContent().withConstellioESModule()
        );

        navigator = new MockedNavigation();
        when(view.getConstellioFactories()).thenReturn(getConstellioFactories());
        when(view.getSessionContext()).thenReturn(FakeSessionContext.adminInCollection(zeCollection));
        when(view.navigate()).thenReturn(navigator);
        when(view.getCollection()).thenReturn(zeCollection);

        presenter = new ListExcelReportPresenter(view);
    }

    @Test
    public void testIfEveryPossibleReportTypeIsPresentOnInit() {
        Map<String, String> possibleReportType = presenter.initPossibleTab(Locale.FRENCH);
        assertThat(possibleReportType.keySet()).contains("Contenant", "Document", "Document sur Internet", "Document sur un partage réseau", "Dossier", "Emplacement", "Tâche", "Utilisateur");
        assertThat(possibleReportType.values()).contains("containerRecord", "document", "connectorHttpDocument", "connectorSmbDocument", "folder", "storageSpace", "userTask", "user");
    }

    @Test
    public void testGettingCorrectReportFromGivenSchema() {
        ReportServices reportServices = new ReportServices(getModelLayerFactory(), zeCollection);
        List<String> possibleSchema = asList("containerRecord", "document", "connectorHttpDocument", "connectorSmbDocument", "folder", "storageSpace", "userTask", "user");
        MetadataSchemaTypes metadataSchemaTypes = getModelLayerFactory().getMetadataSchemasManager().modify(zeCollection).build(new FakeDataStoreTypeFactory(), getModelLayerFactory());

        for(String schema : possibleSchema) {
            //Report report = new Report(admin, metadataSchemaTypes);
        }
    }
}
