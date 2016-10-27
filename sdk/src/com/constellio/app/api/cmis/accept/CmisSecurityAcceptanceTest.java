package com.constellio.app.api.cmis.accept;

import com.constellio.app.modules.rm.RMTestRecords;
import com.constellio.model.entities.Taxonomy;
import com.constellio.model.entities.records.Record;
import com.constellio.model.entities.records.wrappers.User;
import com.constellio.model.entities.schemas.Schemas;
import com.constellio.model.entities.security.Authorization;
import com.constellio.model.entities.security.global.AuthorizationBuilder;
import com.constellio.model.services.migrations.ConstellioEIMConfigs;
import com.constellio.model.services.records.RecordServices;
import com.constellio.model.services.records.RecordServicesException;
import com.constellio.model.services.schemas.MetadataSchemaTypesAlteration;
import com.constellio.model.services.schemas.MetadataSchemasManager;
import com.constellio.model.services.schemas.builders.MetadataSchemaTypesBuilder;
import com.constellio.model.services.security.AuthorizationsServices;
import com.constellio.model.services.taxonomies.TaxonomiesManager;
import com.constellio.model.services.taxonomies.TaxonomiesSearchOptions;
import com.constellio.model.services.taxonomies.TaxonomiesSearchServices;
import com.constellio.model.services.taxonomies.TaxonomySearchRecord;
import com.constellio.model.services.users.UserServices;
import com.constellio.sdk.tests.ConstellioTest;
import com.constellio.sdk.tests.setups.Users;
import org.apache.chemistry.opencmis.client.api.*;
import org.apache.chemistry.opencmis.client.runtime.ObjectIdImpl;
import org.apache.chemistry.opencmis.commons.PropertyIds;
import org.apache.chemistry.opencmis.commons.data.Ace;
import org.apache.chemistry.opencmis.commons.enums.Action;
import org.apache.chemistry.opencmis.commons.spi.Holder;
import org.assertj.core.api.Condition;
import org.joda.time.LocalDate;
import org.joda.time.LocalDateTime;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

import static com.constellio.model.entities.security.Role.READ;
import static com.constellio.model.entities.security.Role.WRITE;
import static java.util.Arrays.asList;
import static org.apache.chemistry.opencmis.commons.enums.AclPropagation.OBJECTONLY;
import static org.apache.chemistry.opencmis.commons.enums.AclPropagation.REPOSITORYDETERMINED;
import static org.apache.chemistry.opencmis.commons.impl.CollectionsHelper.isNullOrEmpty;
import static org.assertj.core.api.Assertions.assertThat;

/**
 * Created by Constelio on 2016-10-26.
 */
public class CmisSecurityAcceptanceTest extends ConstellioTest {
    private static final Logger LOGGER = LoggerFactory.getLogger(CmisACLAcceptanceTest.class);

    UserServices userServices;
    TaxonomiesManager taxonomiesManager;
    MetadataSchemasManager metadataSchemasManager;
    RecordServices recordServices;
    Users users = new Users();
    CmisAcceptanceTestSetup zeCollectionSchemas = new CmisAcceptanceTestSetup(zeCollection);
    CmisAcceptanceTestSetup.Records zeCollectionRecords;
    TaxonomiesSearchServices taxonomiesSearchServices;

    List<String> R = asList("cmis:read");
    List<String> RW = asList("cmis:read", "cmis:write");
    List<String> RWD = asList("cmis:read", "cmis:write", "cmis:delete");

    Session session;

    AuthorizationsServices authorizationsServices;

    String aliceId, bobId, charlesId, dakotaId, edouardId, chuckId, gandalfId, robinId, heroesId;

    @Before
    public void setup()
            throws Exception {
        userServices = getModelLayerFactory().newUserServices();
        taxonomiesManager = getModelLayerFactory().getTaxonomiesManager();
        metadataSchemasManager = getModelLayerFactory().getMetadataSchemasManager();
        recordServices = getModelLayerFactory().newRecordServices();

        taxonomiesSearchServices = getModelLayerFactory().newTaxonomiesSearchService();

        users.setUp(userServices);

        defineSchemasManager().using(zeCollectionSchemas);
        taxonomiesManager.addTaxonomy(zeCollectionSchemas.getTaxonomy1(), metadataSchemasManager);
        taxonomiesManager.addTaxonomy(zeCollectionSchemas.getTaxonomy2(), metadataSchemasManager);
        taxonomiesManager.setPrincipalTaxonomy(zeCollectionSchemas.getTaxonomy2(), metadataSchemasManager);
        getModelLayerFactory().getMetadataSchemasManager().modify(zeCollection, new MetadataSchemaTypesAlteration() {
            @Override
            public void alter(MetadataSchemaTypesBuilder types) {
                types.getSchemaType(zeCollectionSchemas.administrativeUnit.type().getCode()).setSecurity(true);
                types.getSchemaType(zeCollectionSchemas.classificationStation.type().getCode()).setSecurity(true);
                types.getSchemaType(zeCollectionSchemas.documentFond.type().getCode()).setSecurity(false);
                types.getSchemaType(zeCollectionSchemas.category.type().getCode()).setSecurity(false);
                types.getSchemaType(zeCollectionSchemas.folderSchema.type().getCode()).setSecurity(true);
                types.getSchemaType(zeCollectionSchemas.documentSchema.type().getCode()).setSecurity(true);
            }
        });
        zeCollectionRecords = zeCollectionSchemas.givenRecords(recordServices);

        userServices.addUserToCollection(users.alice(), zeCollection);
        userServices.addUserToCollection(users.bob(), zeCollection);
        userServices.addUserToCollection(users.charles(), zeCollection);
        userServices.addUserToCollection(users.dakotaLIndien(), zeCollection);
        userServices.addUserToCollection(users.edouardLechat(), zeCollection);
        userServices.addUserToCollection(users.gandalfLeblanc(), zeCollection);
        userServices.addUserToCollection(users.chuckNorris(), zeCollection);
        userServices.addUserToCollection(users.sasquatch(), zeCollection);
        userServices.addUserToCollection(users.robin(), zeCollection);

        userServices.addUserToCollection(users.admin(), zeCollection);
        userServices.addUserToCollection(users.chuckNorris(), zeCollection);

        recordServices.update(users.adminIn(zeCollection).setCollectionAllAccess(true));
        recordServices.update(users.aliceIn(zeCollection).setCollectionReadAccess(true));
        recordServices.update(users.dakotaIn(zeCollection).setCollectionReadAccess(true).setCollectionWriteAccess(true));

        authorizationsServices = getModelLayerFactory().newAuthorizationsServices();
        aliceId = users.aliceIn(zeCollection).getId();
        bobId = users.bobIn(zeCollection).getId();
        charlesId = users.charlesIn(zeCollection).getId();
        dakotaId = users.dakotaIn(zeCollection).getId();
        edouardId = users.edouardIn(zeCollection).getId();
        gandalfId = users.gandalfIn(zeCollection).getId();
        chuckId = users.chuckNorrisIn(zeCollection).getId();
        heroesId = users.heroesIn(zeCollection).getId();
        robinId = users.robinIn(zeCollection).getId();

        givenConfig(ConstellioEIMConfigs.CMIS_NEVER_RETURN_ACL, false);
        givenFolderInheritingTaxonomyAuthorizations();
    }

    @After
    public void stopApplicationAfterTest() {
        stopApplication();
    }

    @Test
    public void whenNavigatingThenEverybodyHaveAccessToRoot() {
        //Test is passing, but not on workbench
        //FAIL ONLY WHEN GETDescendants
        session = newCMISSessionAsUserInZeCollection(aliceWonderland);
        CmisObject root = session.getObjectByPath("/");
        getChildren(root);
        assertThat(false).isTrue();
    }

    @Test
    public void whenNavigatingThenReturnOnlyReadableFolders()
            throws RecordServicesException {

        session = newCMISSessionAsUserInZeCollection(admin);
        Folder cmisFolder1_doc1 = cmisFolder(zeCollectionRecords.folder1_doc1);
        Folder cmisFolder2 = cmisFolder(zeCollectionRecords.folder2);
        cmisFolder1_doc1.addAcl(asList(ace(bobGratton, RW), ace(charlesFrancoisXavier, RW)), REPOSITORYDETERMINED);
        cmisFolder2.addAcl(asList(ace(bobGratton, RW)), REPOSITORYDETERMINED);

        CmisObject root = session.getObjectByPath("/");
        List<CmisObject> descendants = getDescendants(root);
        assertThat(descendants).hasSize(35);

        session = newCMISSessionAsUserInZeCollection(aliceWonderland);
        root = session.getObjectByPath("/");
        descendants = getDescendants(root);
        assertThat(descendants).hasSize(35);

        session = newCMISSessionAsUserInZeCollection(dakota);
        root = session.getObjectByPath("/");
        descendants = getDescendants(root);
        assertThat(descendants).hasSize(35);

        session = newCMISSessionAsUserInZeCollection(bobGratton);
        try {
            session.getObjectByPath("/taxo_taxo2/zetaxo2_unit1/zetaxo2_station2/zetaxo2_station2_1");
            assertThat(false).isTrue();
        } catch(Exception e) {

        }
        session.getObjectByPath("/taxo_taxo2/zetaxo2_unit1/zetaxo2_station2/folder1/folder1_doc1");
        root = session.getObjectByPath("/taxo_taxo2/zetaxo2_unit1/zetaxo2_station2/zetaxo2_station2_1/folder2");
        descendants = getDescendants(root);
        assertThat(descendants).hasSize(4);

        session = newCMISSessionAsUserInZeCollection(charlesFrancoisXavier);
        session.getObjectByPath("/taxo_taxo2/zetaxo2_unit1/zetaxo2_station2/folder1/folder1_doc1");
        try {
            session.getObjectByPath("/taxo_taxo2/zetaxo2_unit1/zetaxo2_station2/zetaxo2_station2_1/folder2");
            assertThat(false).isTrue();
        } catch(Exception e) {

        }
        assertThat(descendants).hasSize(1);
    }

    @Test
    public void whenCreatingInFolderThenOnlyWorksWithParentWriteAuthorization()
            throws RecordServicesException {
        String parentId = "folder2";
        
        session = newCMISSessionAsUserInZeCollection(admin);
        Folder parent = cmisFolder(zeCollectionRecords.folder2);
        parent.addAcl(asList(ace(bobGratton, RW), ace(charlesFrancoisXavier, R)), REPOSITORYDETERMINED);
        
        Record createdRecord = createNewRecordWithTestProperties(parentId);
        assertThat(createdRecord).isNotNull();

        session = newCMISSessionAsUserInZeCollection(aliceWonderland);
        createdRecord = createNewRecordWithTestProperties(parentId);
        assertThat(createdRecord).isNull();

        session = newCMISSessionAsUserInZeCollection(dakota);
        createdRecord = createNewRecordWithTestProperties(parentId);
        assertThat(createdRecord).isNotNull();

        session = newCMISSessionAsUserInZeCollection(bobGratton);
        createdRecord = createNewRecordWithTestProperties(parentId);
        assertThat(createdRecord).isNotNull();

        session = newCMISSessionAsUserInZeCollection(charlesFrancoisXavier);
        createdRecord = createNewRecordWithTestProperties(parentId);
        assertThat(createdRecord).isNull();
    }

    @Test
    public void whenCreatingInAdministrativeUnitThenOnlyWorksWithParentWriteAuthorization()
            throws RecordServicesException {
        String parentId = "zetaxo2_unit1";
        
        session = newCMISSessionAsUserInZeCollection(admin);
        Folder parent = cmisFolder(zeCollectionRecords.taxo2_unit1);
        parent.addAcl(asList(ace(bobGratton, RW), ace(charlesFrancoisXavier, R)), REPOSITORYDETERMINED);

        Record createdRecord = createNewRecordWithTestProperties(parentId);
        assertThat(createdRecord).isNotNull();

        session = newCMISSessionAsUserInZeCollection(aliceWonderland);
        createdRecord = createNewRecordWithTestProperties(parentId);
        assertThat(createdRecord).isNull();

        session = newCMISSessionAsUserInZeCollection(dakota);
        createdRecord = createNewRecordWithTestProperties(parentId);
        assertThat(createdRecord).isNotNull();

        session = newCMISSessionAsUserInZeCollection(bobGratton);
        createdRecord = createNewRecordWithTestProperties(parentId);
        assertThat(createdRecord).isNotNull();

        session = newCMISSessionAsUserInZeCollection(charlesFrancoisXavier);
        createdRecord = createNewRecordWithTestProperties(parentId);
        assertThat(createdRecord).isNull();
    }

    @Test
    public void whenEditingInAdministrativeUnitThenOnlyWorksWithParentWriteAuthorization()
            throws RecordServicesException {
        String path = "/taxo_taxo2/zetaxo2_unit1/zetaxo2_station2/zetaxo2_station2_1/folder2";

        session = newCMISSessionAsUserInZeCollection(admin);
        Folder parent = cmisFolder(zeCollectionRecords.taxo2_unit1);
        parent.addAcl(asList(ace(bobGratton, RW), ace(charlesFrancoisXavier, R)), REPOSITORYDETERMINED);

        CmisObject object = session.getObjectByPath(path);
        updateFolderWithTestProperties(asList(object), "folder_default");

        session = newCMISSessionAsUserInZeCollection(aliceWonderland);
        object = session.getObjectByPath(path);
        updateFolderWithTestProperties(asList(object), "folder_default");

        session = newCMISSessionAsUserInZeCollection(dakota);
        object = session.getObjectByPath(path);
        updateFolderWithTestProperties(asList(object), "folder_default");

        session = newCMISSessionAsUserInZeCollection(bobGratton);
        object = session.getObjectByPath(path);
        updateFolderWithTestProperties(asList(object), "folder_default");

        session = newCMISSessionAsUserInZeCollection(charlesFrancoisXavier);
        object = session.getObjectByPath(path);
        updateFolderWithTestProperties(asList(object), "folder_default");
    }

    @Test
    public void whenEditingInFolderThenOnlyWorksWithParentWriteAuthorization()
            throws RecordServicesException {
        String path = "/taxo_taxo2/zetaxo2_unit1/zetaxo2_station2/zetaxo2_station2_1/folder2";

        session = newCMISSessionAsUserInZeCollection(admin);
        Folder parent = cmisFolder(zeCollectionRecords.folder2);
        parent.addAcl(asList(ace(bobGratton, RW), ace(charlesFrancoisXavier, R)), REPOSITORYDETERMINED);

        CmisObject object = session.getObjectByPath(path);
        updateFolderWithTestProperties(asList(object), "folder_default");

        session = newCMISSessionAsUserInZeCollection(aliceWonderland);
        object = session.getObjectByPath(path);
        updateFolderWithTestProperties(asList(object), "folder_default");

        session = newCMISSessionAsUserInZeCollection(dakota);
        object = session.getObjectByPath(path);
        updateFolderWithTestProperties(asList(object), "folder_default");

        session = newCMISSessionAsUserInZeCollection(bobGratton);
        object = session.getObjectByPath(path);
        updateFolderWithTestProperties(asList(object), "folder_default");

        session = newCMISSessionAsUserInZeCollection(charlesFrancoisXavier);
        object = session.getObjectByPath(path);
        updateFolderWithTestProperties(asList(object), "folder_default");
    }

    @Test
    public void whenMovingInFolderThenOnlyWorksWithParentWriteAuthorization()
            throws RecordServicesException {

        session = newCMISSessionAsUserInZeCollection(admin);
        Folder oldParent = cmisFolder(zeCollectionRecords.folder1);
        oldParent.addAcl(asList(ace(bobGratton, RW), ace(charlesFrancoisXavier, RW)), REPOSITORYDETERMINED);
        Folder movedFolder = cmisFolder(zeCollectionRecords.folder1_doc1);
        movedFolder.addAcl(asList(ace(bobGratton, RW), ace(charlesFrancoisXavier, RW)), REPOSITORYDETERMINED);
        Folder newParent = cmisFolder(zeCollectionRecords.folder2);
        newParent.addAcl(asList(ace(bobGratton, RW), ace(charlesFrancoisXavier, R)), REPOSITORYDETERMINED);

        Record record = zeCollectionRecords.folder1_doc1;
        String newParentID = zeCollectionRecords.folder2.getId();

        assertThat(canBeMovedTo(record, newParentID)).isTrue();

        session = newCMISSessionAsUserInZeCollection(aliceWonderland);
        assertThat(canBeMovedTo(record, newParentID)).isFalse();

        session = newCMISSessionAsUserInZeCollection(dakota);
        assertThat(canBeMovedTo(record, newParentID)).isTrue();

        session = newCMISSessionAsUserInZeCollection(bobGratton);
        assertThat(canBeMovedTo(record, newParentID)).isTrue();

        session = newCMISSessionAsUserInZeCollection(charlesFrancoisXavier);
        assertThat(canBeMovedTo(record, newParentID)).isFalse();
    }

    @Test
    public void whenMovingInAdministrativeUnitThenOnlyWorksWithParentWriteAuthorization()
            throws RecordServicesException {

        session = newCMISSessionAsUserInZeCollection(admin);
        Folder oldParent = cmisFolder(zeCollectionRecords.folder1);
        oldParent.addAcl(asList(ace(bobGratton, RW), ace(charlesFrancoisXavier, RW)), REPOSITORYDETERMINED);
        Folder movedFolder = cmisFolder(zeCollectionRecords.folder1_doc1);
        movedFolder.addAcl(asList(ace(bobGratton, RW), ace(charlesFrancoisXavier, RW)), REPOSITORYDETERMINED);
        Folder newParent = cmisFolder(zeCollectionRecords.taxo2_unit1);
        newParent.addAcl(asList(ace(bobGratton, RW), ace(charlesFrancoisXavier, R)), REPOSITORYDETERMINED);

        Record record = zeCollectionRecords.folder1_doc1;
        String newParentID = zeCollectionRecords.taxo2_unit1.getId();

        assertThat(canBeMovedTo(record, newParentID)).isTrue();

        session = newCMISSessionAsUserInZeCollection(aliceWonderland);
        assertThat(canBeMovedTo(record, newParentID)).isFalse();

        session = newCMISSessionAsUserInZeCollection(dakota);
        assertThat(canBeMovedTo(record, newParentID)).isTrue();

        session = newCMISSessionAsUserInZeCollection(bobGratton);
        assertThat(canBeMovedTo(record, newParentID)).isTrue();

        session = newCMISSessionAsUserInZeCollection(charlesFrancoisXavier);
        assertThat(canBeMovedTo(record, newParentID)).isFalse();
    }

    @Test
    public void whenDeletingInFolderThenOnlyWorksWithParentDeleteAuthorization()
            throws RecordServicesException {

        session = newCMISSessionAsUserInZeCollection(admin);
        Folder parent = cmisFolder(zeCollectionRecords.folder1);
        parent.addAcl(asList(ace(bobGratton, RWD), ace(charlesFrancoisXavier, RW)), REPOSITORYDETERMINED);

        String parentID = parent.getId();

        
        Record deletedRecord = createAndDeleteNewRecord(parentID);
        assertThat(deletedRecord).isNull();

        session = newCMISSessionAsUserInZeCollection(aliceWonderland);
        deletedRecord = createAndDeleteNewRecord(parentID);
        assertThat(deletedRecord).isNotNull();

        session = newCMISSessionAsUserInZeCollection(dakota);
        deletedRecord = createAndDeleteNewRecord(parentID);
        assertThat(deletedRecord).isNull();

        session = newCMISSessionAsUserInZeCollection(bobGratton);
        deletedRecord = createAndDeleteNewRecord(parentID);
        assertThat(deletedRecord).isNull();

        session = newCMISSessionAsUserInZeCollection(charlesFrancoisXavier);
        deletedRecord = createAndDeleteNewRecord(parentID);
        assertThat(deletedRecord).isNotNull();
    }

    @Test
    public void whenDeletingInAdministrativeUnitThenOnlyWorksWithParentDeleteAuthorization()
            throws RecordServicesException {

        session = newCMISSessionAsUserInZeCollection(admin);
        Folder parent = cmisFolder(zeCollectionRecords.taxo2_unit1);
        parent.addAcl(asList(ace(bobGratton, RWD), ace(charlesFrancoisXavier, RW)), REPOSITORYDETERMINED);

        String parentID = parent.getId();

        Record deletedRecord = createAndDeleteNewRecord(parentID);
        assertThat(deletedRecord).isNull();

        session = newCMISSessionAsUserInZeCollection(aliceWonderland);
        deletedRecord = createAndDeleteNewRecord(parentID);
        assertThat(deletedRecord).isNotNull();

        session = newCMISSessionAsUserInZeCollection(dakota);
        deletedRecord = createAndDeleteNewRecord(parentID);
        assertThat(deletedRecord).isNull();

        session = newCMISSessionAsUserInZeCollection(bobGratton);
        deletedRecord = createAndDeleteNewRecord(parentID);
        assertThat(deletedRecord).isNull();

        session = newCMISSessionAsUserInZeCollection(charlesFrancoisXavier);
        deletedRecord = createAndDeleteNewRecord(parentID);
        assertThat(deletedRecord).isNotNull();
    }

    private List<CmisObject> getChildren(CmisObject parent) {
        List<CmisObject> children = new ArrayList<>();
        if (parent.getBaseTypeId().value().equals("CMIS_FOLDER")) {
            Iterator<CmisObject> childrenIterator = ((Folder) parent).getChildren().iterator();
            while (childrenIterator.hasNext()) {
                children.add(childrenIterator.next());
            }
        }

        return children;
    }

    private List<CmisObject> getDescendants (CmisObject parent) {
        List<CmisObject> children = new ArrayList<>();
        List<CmisObject> descendants = new ArrayList<>();
        Iterator<CmisObject> childrenIterator = ((Folder) parent).getChildren().iterator();
        while (childrenIterator.hasNext()) {
            children.add(childrenIterator.next());
        }
        for (CmisObject child : children) {
            descendants.addAll(getDescendants(child));
        }
        children.addAll(descendants);

        return children;
    }

    private void givenFolderInheritingTaxonomyAuthorizations() {
        Authorization authorization = new AuthorizationBuilder(zeCollection).forUsers(users.edouardIn(zeCollection))
                .on(zeCollectionRecords.taxo2_station2_1).givingReadWriteAccess();
        getModelLayerFactory().newAuthorizationsServices().add(authorization, users.adminIn(zeCollection));
        try {
            waitForBatchProcess();
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    private Ace ace(String principal, List<String> permissions) {
        return session.getObjectFactory().createAce(principal, permissions);
    }

    private Folder cmisFolder(Record record) {
        return (Folder) session.getObject(record.getId());
    }

    private String createNewFolderWithTestProperties(String parent, String objectType) {
        ObjectId parentFolderId = new ObjectIdImpl(parent);
        Map<String, Object> newFolderProperties = new HashMap<>();
        newFolderProperties.put(PropertyIds.OBJECT_TYPE_ID, objectType);
        newFolderProperties.put("title", "testFolder");
        newFolderProperties.put("numberMeta", 42.666);
        return session.createFolder(newFolderProperties, parentFolderId).getId();
    }

    private Record createNewRecordWithTestProperties(String parentId) {
        return recordServices.getDocumentById(createNewFolderWithTestProperties(parentId, "folder_default"));
    }

    private Record createAndDeleteNewRecord(String parentId) {
        String id = createNewFolderWithTestProperties(parentId, "folder_default");
        ObjectId objectId = new ObjectIdImpl(id);
        session.delete(objectId);
        return recordServices.getDocumentById(id);
    }

    private void updateFolderWithTestProperties(List<CmisObject> objectList, String objectType) {
        Map<String, Object> newFolderProperties = new HashMap<>();
        newFolderProperties.put(PropertyIds.OBJECT_TYPE_ID, objectType);
        newFolderProperties.put("title", "testFolder");
        newFolderProperties.put("numberMeta", 42.666);
        session.bulkUpdateProperties(objectList, newFolderProperties, null, null);
    }

    private void moveObject(Record record, String parentTargetId) {
        CmisObject object = session.getObject(record.getId());
        Holder<String> objectIdHolder = new Holder<String>(object.getId());

        session.getBinding().getObjectService()
                .moveObject(session.getRepositoryInfo().getId(), objectIdHolder, parentTargetId, record.getId(), null);

        recordServices.refresh(record);
    }

    private boolean canBeMovedTo(Record record, String parentTargetId) {
        String oldParentID = record.getParentId();

        moveObject(record, parentTargetId);
        boolean isMovable = record.getParentId().equals(parentTargetId);
        moveObject(record, oldParentID);

        return isMovable;
    }

    private void printTaxonomies(User user) {
        StringBuilder stringBuilder = new StringBuilder();
        for (Taxonomy taxonomy : taxonomiesManager.getEnabledTaxonomies(zeCollection)) {
            stringBuilder.append(taxonomy.getCode() + " : \n");
            for (Record record : taxonomiesSearchServices
                    .getRootConcept(zeCollection, taxonomy.getCode(), new TaxonomiesSearchOptions().setRows(100))) {

                printConcept(user, taxonomy.getCode(), record, 1, stringBuilder);
            }
            stringBuilder.append("\n\n");
        }
        System.out.println(stringBuilder.toString());
    }

    private void printConcept(User user, String taxonomy, Record record, int level, StringBuilder stringBuilder) {
        for (int i = 0; i < level; i++) {
            stringBuilder.append("\t");
        }
        stringBuilder.append(record.getId() + "\n");
        for (TaxonomySearchRecord child : taxonomiesSearchServices
                .getVisibleChildConcept(user, taxonomy, record, new TaxonomiesSearchOptions().setRows(100))) {

            printConcept(user, taxonomy, child.getRecord(), level + 1, stringBuilder);
        }
    }

    private Condition<? super CmisObject> property(final String key, final Object value) {
        return new Condition<CmisObject>() {
            @Override
            public boolean matches(CmisObject objectData) {
                assertThat(objectData.getPropertyValue(key)).describedAs(key).isEqualTo(value);
                return true;
            }
        };
    }
}
