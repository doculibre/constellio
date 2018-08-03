package com.constellio.app.modules.rm.wrappers.utils;

import com.constellio.app.modules.rm.RMTestRecords;
import com.constellio.app.modules.rm.services.RMSchemasRecordsServices;
import com.constellio.app.modules.rm.wrappers.Document;
import com.constellio.app.modules.rm.wrappers.Folder;
import com.constellio.model.entities.records.Transaction;
import com.constellio.model.services.records.RecordServices;
import com.constellio.model.services.records.RecordServicesException;
import com.constellio.sdk.tests.ConstellioTest;
import com.constellio.sdk.tests.setups.Users;
import org.junit.Before;
import org.junit.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

public class FolderUtilAcceptanceTest extends ConstellioTest {
    public static final String FOLDER_TITLE = "Folder title";
    protected RMTestRecords records = new RMTestRecords(zeCollection);
    protected RecordServices recordServices;
    protected RMSchemasRecordsServices rm;
    protected Users users = new Users();

    @Before
    public void setUp() {
        prepareSystem(
                withZeCollection()
                        .withConstellioRMModule()
                        .withAllTestUsers()
                        .withRMTest(records)
                        .withFoldersAndContainersOfEveryStatus()
                        .withDocumentsDecommissioningList()
        );

        inCollection(zeCollection).setCollectionTitleTo("Collection de test");

        recordServices = getModelLayerFactory().newRecordServices();
    }

    @Test
    public void givenSubFolderAddedWhenGettingSubFoldersByTitleThenFound() throws RecordServicesException {
        Transaction tx = new Transaction();
        Folder parent = records.getFolder_A02();
        Folder folder = tx.add(records.newChildFolderIn(parent));
        folder.setTitle(FOLDER_TITLE);
        recordServices.execute(tx);
        assertThat(folder).isNotNull();
        assertThat(folder.getId()).isNotNull();
        assertThat(folder.getParentFolder()).isEqualTo(parent.getId());

        Folder withSameTitle = FolderUtil.getSubFolderWithSameTitle(getAppLayerFactory(), parent, FOLDER_TITLE);
        assertThat(withSameTitle).isNotNull();
        assertThat(withSameTitle.getId()).isEqualTo(folder.getId());
    }

    @Test
    public void givenSubFolderAddedWhenGettingSubFoldersThenFound() throws RecordServicesException {
        Transaction tx = new Transaction();
        Folder parent = records.getFolder_A02();
        Folder folder = tx.add(records.newChildFolderIn(parent));
        recordServices.execute(tx);
        assertThat(folder).isNotNull();
        assertThat(folder.getId()).isNotNull();
        assertThat(folder.getParentFolder()).isEqualTo(parent.getId());

        List<Folder> subFolders = FolderUtil.getSubFolders(getAppLayerFactory(), parent);
        assertThat(subFolders).isNotEmpty();
        assertThat(subFolders).extracting("id").contains(folder.getId());
    }

    @Test
    public void givenFolderWhenCopiedThenManualPropertiesEqual() {
        Folder newFolder = FolderUtil.createNewFolder(zeCollection, getAppLayerFactory());
        assertThat(newFolder).isNotNull();
        assertThat(newFolder.getId()).isNotNull();

        newFolder.setTitle("Fodler title");
        newFolder.setType(records.folderTypeOther());
        newFolder.setDescription("Folder description");

        Folder copyFrom = FolderUtil.createCopyFrom(newFolder, getAppLayerFactory());
        assertThat(copyFrom).isNotNull();
        assertThat(copyFrom.getId()).isNotNull().isNotEqualTo(newFolder.getId());
        assertThat(copyFrom.getType()).isEqualTo(newFolder.getType());
        assertThat(copyFrom.getDescription()).isEqualTo(newFolder.getDescription());
    }

    @Test
    public void whenNewFolderCreatedThenIdIsNotNull() {
        Folder newFolder = FolderUtil.createNewFolder(zeCollection, getAppLayerFactory());
        assertThat(newFolder).isNotNull();
        assertThat(newFolder.getId()).isNotNull();
    }
}
