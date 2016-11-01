package com.constellio.dev;

import com.constellio.app.modules.rm.RMTestRecords;
import com.constellio.app.modules.rm.services.RMSchemasRecordsServices;
import com.constellio.app.modules.rm.wrappers.DecommissioningList;
import com.constellio.app.ui.pages.management.schemas.metadata.AddEditMetadataPresenter;
import com.constellio.model.entities.records.Record;
import com.constellio.model.entities.schemas.Schemas;
import com.constellio.model.services.search.SearchServices;
import com.constellio.model.services.search.query.logical.LogicalSearchQuery;
import com.constellio.model.services.search.query.logical.LogicalSearchQueryOperators;
import com.constellio.sdk.tests.ConstellioTest;
import com.constellio.sdk.tests.FakeSessionContext;
import com.constellio.sdk.tests.MockedNavigation;
import com.constellio.sdk.tests.annotations.InDevelopmentTest;
import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.constellio.model.services.search.query.logical.LogicalSearchQueryOperators.*;
import static com.constellio.sdk.tests.schemas.TestsSchemasSetup.*;
import static java.util.Arrays.asList;
import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.when;

/**
 * Created by Constelio on 2016-10-31.
 */
public class AdministrativeUnitCleanerAcceptanceTest extends ConstellioTest{
    RMTestRecords records = new RMTestRecords(zeCollection);
    SearchServices searchServices;
    RMSchemasRecordsServices rm;

    @Before
    public void setup() {
        prepareSystem(
                withZeCollection().withConstellioRMModule().withConstellioESModule().withAllTestUsers()
                        .withRMTest(records).withFoldersAndContainersOfEveryStatus().withDocumentsDecommissioningList()
        );
        inCollection(zeCollection).setCollectionTitleTo("Collection de test");

        rm = new RMSchemasRecordsServices(zeCollection, getAppLayerFactory());
        searchServices = getModelLayerFactory().newSearchServices();
    }

    @Test
    public void whenCleaningAnAdministrativeUnitThenFoldersAndDocumentsNoLongerExist() {
        Record administrativeUnit = searchServices.searchSingleResult(from(rm.administrativeUnit.schema())
                .where(rm.administrativeUnit.code()).isEqualTo("30C"));

        long oldTotalNumFolder = searchServices.getResultsCount(from(rm.folder.schema()).returnAll());
        long oldTotalNumDocument = searchServices.getResultsCount(from(rm.document.schema()).returnAll());
        long oldNumFolderInAdminUnit = searchServices.getResultsCount(from(rm.folder.schema()).where(Schemas.PRINCIPAL_PATH)
                .isContainingText(administrativeUnit.getId()));
        long oldNumDocumentInAdminUnit = searchServices.getResultsCount(from(rm.document.schema()).where(Schemas.PRINCIPAL_PATH)
                .isContainingText(administrativeUnit.getId()));
        assertThat(oldTotalNumFolder).isNotEqualTo(0);
        assertThat(oldTotalNumDocument).isNotEqualTo(0);
        assertThat(oldNumFolderInAdminUnit).isNotEqualTo(0);
        assertThat(oldNumDocumentInAdminUnit).isNotEqualTo(0);

        AdministrativeUnitCleaner.cleanAdministrativeUnit(zeCollection, administrativeUnit.getId(), getAppLayerFactory());
        long newTotalNumFolder = searchServices.getResultsCount(from(rm.folder.schema()).returnAll());
        long newTotalNumDocument = searchServices.getResultsCount(from(rm.document.schema()).returnAll());
        long newNumFolderInAdminUnit = searchServices.getResultsCount(from(rm.folder.schema()).where(Schemas.PRINCIPAL_PATH)
                .isContainingText(administrativeUnit.getId()));
        long newNumDocumentInAdminUnit = searchServices.getResultsCount(from(rm.document.schema()).where(Schemas.PRINCIPAL_PATH)
                .isContainingText(administrativeUnit.getId()));

        assertThat(newNumFolderInAdminUnit).isEqualTo(0);
        assertThat(newNumDocumentInAdminUnit).isEqualTo(0);
        assertThat(newTotalNumFolder).isEqualTo(oldTotalNumFolder-oldNumFolderInAdminUnit);
        assertThat(newTotalNumDocument).isEqualTo(oldTotalNumDocument-oldNumDocumentInAdminUnit);
    }

    @Test
    public void whenCleaningAnAdministrativeUnitThenContainersNoLongerExist() {
        Record administrativeUnit = searchServices.searchSingleResult(from(rm.administrativeUnit.schema())
                .where(rm.administrativeUnit.code()).isEqualTo("30C"));

        long oldTotalNumContainer = searchServices.getResultsCount(from(rm.containerRecord.schema()).returnAll());
        long oldNumContainerInAdminUnit = searchServices.getResultsCount(from(rm.containerRecord.schema()).where(Schemas.PRINCIPAL_PATH)
                .isContainingText(administrativeUnit.getId()));
        assertThat(oldTotalNumContainer).isNotEqualTo(0);
        assertThat(oldNumContainerInAdminUnit).isNotEqualTo(0);

        AdministrativeUnitCleaner.cleanAdministrativeUnit(zeCollection, administrativeUnit.getId(), getAppLayerFactory());
        long newTotalNumContainer = searchServices.getResultsCount(from(rm.containerRecord.schema()).returnAll());
        long newNumContainerInAdminUnit = searchServices.getResultsCount(from(rm.containerRecord.schema()).where(Schemas.PRINCIPAL_PATH)
                .isContainingText(administrativeUnit.getId()));

        assertThat(newNumContainerInAdminUnit).isEqualTo(0);
        assertThat(newTotalNumContainer).isEqualTo(oldTotalNumContainer-oldNumContainerInAdminUnit);
    }

    @Test
    public void whenCleaningAnAdministrativeUnitThenDecommissioningListStillExistButRemovedRecords() {
        Record administrativeUnit = searchServices.searchSingleResult(from(rm.administrativeUnit.schema())
                .where(rm.administrativeUnit.code()).isEqualTo("10A"));
        Record folder = searchServices.search(new LogicalSearchQuery().setCondition(
                from(rm.folder.schema()).where(Schemas.IDENTIFIER).isEqualTo("A49"))).get(0);
        Record document = searchServices.search(new LogicalSearchQuery().setCondition(
                from(rm.document.schema()).where(Schemas.TITLE)
                        .isEqualTo("Grenouille - Document procès verbal numérique avec un autre exemplaire"))).get(0);
        Record container = searchServices.search(new LogicalSearchQuery().setCondition(
                from(rm.containerRecord.schema()).where(Schemas.TITLE).isEqualTo("10_A_04"))).get(0);

        List<DecommissioningList> decommissioningLists = rm.searchDecommissioningLists(anyConditions(
                where(rm.decommissioningList.folders()).isContaining(asList(folder.getId())),
                where(rm.decommissioningList.documents()).isContaining(asList(document.getId())),
                where(rm.decommissioningList.containers()).isContaining(asList(container.getId()))
        ));

        assertThat(decommissioningLists).isNotEmpty();

        AdministrativeUnitCleaner.cleanAdministrativeUnit(zeCollection, administrativeUnit.getId(), getAppLayerFactory());

        List<DecommissioningList> decommissioningListsAfterCleaning = rm.searchDecommissioningLists(
                where(Schemas.IDENTIFIER).isIn(extractIdentifier(decommissioningLists))
        );

        assertThat(decommissioningListsAfterCleaning).hasSameSizeAs(decommissioningLists);
        long numberOfDecomListContainingRecords = searchServices.getResultsCount(from(rm.decommissioningList.schema()).whereAnyCondition(
                where(rm.decommissioningList.folders()).isContaining(asList(folder.getId())),
                where(rm.decommissioningList.documents()).isContaining(asList(document.getId())),
                where(rm.decommissioningList.containers()).isContaining(asList(container.getId()))
        ));

        assertThat(numberOfDecomListContainingRecords).isEqualTo(0);
    }

    public List<String> extractIdentifier(List<DecommissioningList> decommissioningLists) {
        List<String> identifierList = new ArrayList<>();
        for(DecommissioningList decommissioningList: decommissioningLists) {
            identifierList.add(decommissioningList.getId());
        }
        return identifierList;
    }
}
