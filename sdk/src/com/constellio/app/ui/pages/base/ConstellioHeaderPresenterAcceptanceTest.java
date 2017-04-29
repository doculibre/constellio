package com.constellio.app.ui.pages.base;

import com.constellio.app.modules.rm.RMTestRecords;
import com.constellio.app.modules.rm.model.enums.CopyType;
import com.constellio.app.modules.rm.services.RMSchemasRecordsServices;
import com.constellio.app.modules.rm.wrappers.Folder;
import com.constellio.app.services.schemasDisplay.SchemasDisplayManager;
import com.constellio.app.ui.application.CoreViews;
import com.constellio.app.ui.entities.MetadataVO;
import com.constellio.model.entities.Taxonomy;
import com.constellio.model.entities.schemas.MetadataValueType;
import com.constellio.model.entities.schemas.Schemas;
import com.constellio.model.services.records.RecordServices;
import com.constellio.model.services.records.RecordServicesException;
import com.constellio.model.services.schemas.MetadataSchemaTypesAlteration;
import com.constellio.model.services.schemas.MetadataSchemasManager;
import com.constellio.model.services.schemas.builders.MetadataSchemaTypeBuilder;
import com.constellio.model.services.schemas.builders.MetadataSchemaTypesBuilder;
import com.constellio.model.services.search.SearchServices;
import com.constellio.sdk.tests.ConstellioTest;
import com.constellio.sdk.tests.FakeSessionContext;
import com.constellio.sdk.tests.setups.Users;
import org.joda.time.LocalDate;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import static com.constellio.model.entities.schemas.Schemas.AUTHORIZATIONS;
import static com.constellio.model.entities.schemas.Schemas.IS_DETACHED_AUTHORIZATIONS;
import static java.util.Arrays.asList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Matchers.anyList;
import static org.mockito.Mockito.*;

/**
 * Created by Constelio on 2016-11-04.
 */
public class ConstellioHeaderPresenterAcceptanceTest extends ConstellioTest {

    Users users = new Users();
    @Mock
    ConstellioHeaderImpl header;
    @Mock
    CoreViews navigator;
    RMTestRecords rmRecords = new RMTestRecords(zeCollection);
    RMSchemasRecordsServices schemasRecordsServices;
    @Mock ConstellioHeaderPresenter presenter;
    SessionContext sessionContext;
    RecordServices recordServices;

    MetadataSchemasManager metadataSchemasManager;
    SearchServices searchServices;

    @Before
    public void setUp()
            throws Exception {
        prepareSystem(
                withZeCollection().withConstellioRMModule().withAllTest(users).withRMTest(rmRecords)
                        .withFoldersAndContainersOfEveryStatus().withDocumentsHavingContent()
        );
        inCollection(zeCollection).giveWriteAccessTo(aliceWonderland);

        schemasRecordsServices = new RMSchemasRecordsServices(zeCollection, getModelLayerFactory());
        recordServices = getModelLayerFactory().newRecordServices();
        metadataSchemasManager = getModelLayerFactory().getMetadataSchemasManager();
        searchServices = getModelLayerFactory().newSearchServices();

        inCollection(zeCollection).setCollectionTitleTo("Collection de test");

        sessionContext = FakeSessionContext.chuckNorrisInCollection(zeCollection);
        sessionContext.setCurrentLocale(Locale.FRENCH);

        when(header.getSessionContext()).thenReturn(sessionContext);
        when(header.getCollection()).thenReturn(zeCollection);
        when(header.getConstellioFactories()).thenReturn(getConstellioFactories());

        presenter = spy(new ConstellioHeaderPresenter(header));
        doNothing().when(presenter).sort(anyList());
    }

    @Test
    public void givenAdvanceSearchThenMetadataChoiceIsLimitedByUsedSchemas() throws RecordServicesException {
        connectWithAdmin();
        List<MetadataVO> baseMetadatas = presenter.getMetadataAllowedInCriteria();

        getModelLayerFactory().getMetadataSchemasManager().modify(zeCollection, new MetadataSchemaTypesAlteration() {
            @Override
            public void alter(MetadataSchemaTypesBuilder types) {
                types.getSchemaType(Folder.SCHEMA_TYPE).createCustomSchema("customSchema").create("newSearchableMetadata")
                        .setType(MetadataValueType.STRING).setSearchable(true);
            }
        });

        SchemasDisplayManager metadataSchemasDisplayManager = getAppLayerFactory().getMetadataSchemasDisplayManager();
        metadataSchemasDisplayManager.saveMetadata(metadataSchemasDisplayManager.getMetadata(zeCollection, "folder_customSchema_newSearchableMetadata")
                .withVisibleInAdvancedSearchStatus(true));

        assertThat(baseMetadatas).containsAll(presenter.getMetadataAllowedInCriteria());
        recordServices.add(newFolder("testFolder").changeSchemaTo("folder_customSchema"));
        recordServices.update(recordServices.getDocumentById("testFolder").set(IS_DETACHED_AUTHORIZATIONS, true).set(AUTHORIZATIONS, new ArrayList<>()));

        List<MetadataVO> newMetadatas = presenter.getMetadataAllowedInCriteria();
        newMetadatas.removeAll(baseMetadatas);
        assertThat(newMetadatas.size()).isEqualTo(1);
        assertThat(newMetadatas.get(0).getCode()).isEqualTo("folder_customSchema_newSearchableMetadata");

        connectWithBob();
        assertThat(baseMetadatas).containsAll(presenter.getMetadataAllowedInCriteria());
    }

    @Test
    public void givenAdvanceSearchWithTaxonomiesThenIsLimitedByPermission() throws RecordServicesException {
        RMSchemasRecordsServices rm = new RMSchemasRecordsServices(zeCollection, getAppLayerFactory());
        getModelLayerFactory().getMetadataSchemasManager().modify(zeCollection, new MetadataSchemaTypesAlteration() {
            @Override
            public void alter(MetadataSchemaTypesBuilder types) {
                MetadataSchemaTypeBuilder justeadmin = types.createNewSchemaType("justeadmin");
                justeadmin.getDefaultSchema().create("code").setType(MetadataValueType.STRING);
            }
        });

        MetadataSchemasManager metadataSchemasManager = getModelLayerFactory().getMetadataSchemasManager();
        Taxonomy hiddenInHomePage = Taxonomy.createHiddenInHomePage("justeadmin", "justeadmin", zeCollection,
                "justeadmin").withUserIds(asList(rmRecords.getAdmin().getId()));
        getModelLayerFactory().getTaxonomiesManager().addTaxonomy(hiddenInHomePage, metadataSchemasManager);

        recordServices.add(rm.newHierarchicalValueListItem("justeadmin_default").setCode("J01").set(Schemas.TITLE, "J01"));

        connectWithAdmin();
        List<MetadataVO> baseMetadatas = presenter.getMetadataAllowedInCriteria();

        getModelLayerFactory().getMetadataSchemasManager().modify(zeCollection, new MetadataSchemaTypesAlteration() {
            @Override
            public void alter(MetadataSchemaTypesBuilder types) {
                types.getDefaultSchema(Folder.SCHEMA_TYPE).create("newSearchableMetadata")
                        .setType(MetadataValueType.REFERENCE).defineReferencesTo(types.getDefaultSchema("justeadmin")).setSearchable(true);
            }
        });

        SchemasDisplayManager metadataSchemasDisplayManager = getAppLayerFactory().getMetadataSchemasDisplayManager();
        metadataSchemasDisplayManager.saveMetadata(metadataSchemasDisplayManager.getMetadata(zeCollection, "folder_default_newSearchableMetadata")
                .withVisibleInAdvancedSearchStatus(true));

        List<MetadataVO> newMetadatas = presenter.getMetadataAllowedInCriteria();
        newMetadatas.removeAll(baseMetadatas);
        assertThat(newMetadatas.size()).isEqualTo(1);
        assertThat(newMetadatas.get(0).getCode()).isEqualTo("folder_default_newSearchableMetadata");

        connectWithBob();
        assertThat(baseMetadatas).containsAll(presenter.getMetadataAllowedInCriteria());
    }

    @Test
    public void givenAdvanceSearchThenDoNotShowDisabledMetadatas() throws RecordServicesException {
        connectWithAdmin();
        getModelLayerFactory().getMetadataSchemasManager().modify(zeCollection, new MetadataSchemaTypesAlteration() {
            @Override
            public void alter(MetadataSchemaTypesBuilder types) {
                types.getDefaultSchema(Folder.SCHEMA_TYPE).get(Folder.BORROWED)
                        .setEnabled(false);
            }
        });

        List<MetadataVO> baseMetadatas = presenter.getMetadataAllowedInCriteria();

        getModelLayerFactory().getMetadataSchemasManager().modify(zeCollection, new MetadataSchemaTypesAlteration() {
            @Override
            public void alter(MetadataSchemaTypesBuilder types) {
                types.getDefaultSchema(Folder.SCHEMA_TYPE).get(Folder.BORROWED)
                        .setEnabled(true);
            }
        });

        List<MetadataVO> newMetadatas = presenter.getMetadataAllowedInCriteria();
        newMetadatas.removeAll(baseMetadatas);
        assertThat(newMetadatas.size()).isEqualTo(1);
        assertThat(newMetadatas.get(0).getCode()).isEqualTo("folder_default_" + Folder.BORROWED);
    }

    private void connectWithAdmin() {
        sessionContext = FakeSessionContext.adminInCollection(zeCollection);
        sessionContext.setCurrentLocale(Locale.FRENCH);
        when(header.getSessionContext()).thenReturn(sessionContext);
        presenter = spy(new ConstellioHeaderPresenter(header));
        presenter.schemaTypeSelected(Folder.SCHEMA_TYPE);
        doNothing().when(presenter).sort(anyList());
    }

    private void connectWithBob() {
        sessionContext = FakeSessionContext.bobInCollection(zeCollection);
        sessionContext.setCurrentLocale(Locale.FRENCH);
        when(header.getSessionContext()).thenReturn(sessionContext);
        presenter = spy(new ConstellioHeaderPresenter(header));
        presenter.schemaTypeSelected(Folder.SCHEMA_TYPE);
        doNothing().when(presenter).sort(anyList());
    }

    private Folder newFolder(String title) {
        return schemasRecordsServices.newFolderWithId("testFolder").setTitle(title).setOpenDate(LocalDate.now())
                .setAdministrativeUnitEntered(rmRecords.unitId_10a)
                .setCategoryEntered(rmRecords.categoryId_X110)
                .setRetentionRuleEntered(rmRecords.getRule2())
                .setCopyStatusEntered(CopyType.PRINCIPAL);
    }
}
