package com.constellio.model.entities.security;

import com.constellio.app.modules.rm.RMTestRecords;
import com.constellio.app.modules.rm.wrappers.Folder;
import com.constellio.model.entities.records.wrappers.User;
import com.constellio.model.entities.schemas.MetadataSchemaType;
import com.constellio.model.services.search.SearchServices;
import com.constellio.model.services.search.query.logical.LogicalSearchQuery;
import com.constellio.sdk.tests.ConstellioTest;
import com.constellio.sdk.tests.setups.Users;
import org.junit.Before;
import org.junit.Test;

import java.util.List;

import static com.constellio.model.entities.security.SolrAuthorizationDetailsAcceptanceTestResources.*;
import static com.constellio.model.services.search.query.logical.LogicalSearchQueryOperators.from;
import static java.util.Arrays.asList;
import static org.assertj.core.api.Assertions.assertThat;

public class SolrAuthorizationDetailsAcceptanceTest extends ConstellioTest {

    RMTestRecords records = new RMTestRecords(zeCollection);
    Users users = new Users();

    @Before
    public void setup()
            throws Exception {
        prepareSystem(
                withZeCollection().withConstellioRMModule().withAllTestUsers()
                        .withRMTest(records).withFoldersAndContainersOfEveryStatus().withDocumentsDecommissioningList()
        );
        inCollection(zeCollection).setCollectionTitleTo("Collection de test");

        users.setUp(getModelLayerFactory().newUserServices());
    }

    @Test
    public void whenQueryingFoldersFilteredByUserThenReturnsGoodFolders() {

        assertThat(getReadWriteDeleteRecordsForUser(users.adminIn(zeCollection)))
            .containsExactly(getExpectedAdminRead(), getExpectedAdminWrite(), getExpectedAdminDelete());

        assertThat(getReadWriteDeleteRecordsForUser(users.adminIn(zeCollection)))
            .containsExactly(getExpectedAdminRead(), getExpectedAdminWrite(), getExpectedAdminDelete());

        assertThat(getReadWriteDeleteRecordsForUser(users.aliceIn(zeCollection)))
            .containsExactly(getExpectedAliceRead(), getExpectedAliceWrite(), getExpectedAliceDelete());

        assertThat(getReadWriteDeleteRecordsForUser(users.bobIn(zeCollection)))
            .containsExactly(getExpectedBobRead(), getExpectedBobWrite(), getExpectedBobDelete());

        assertThat(getReadWriteDeleteRecordsForUser(users.charlesIn(zeCollection)))
            .containsExactly(getExpectedCharlesRead(), getExpectedCharlesWrite(), getExpectedCharlesDelete());

        assertThat(getReadWriteDeleteRecordsForUser(users.chuckNorrisIn(zeCollection)))
            .containsExactly(getExpectedChuckRead(), getExpectedChuckWrite(), getExpectedChuckDelete());

        assertThat(getReadWriteDeleteRecordsForUser(users.dakotaIn(zeCollection)))
            .containsExactly(getExpectedDakotaRead(), getExpectedDakotaWrite(), getExpectedDakotaDelete());

        assertThat(getReadWriteDeleteRecordsForUser(users.edouardIn(zeCollection)))
            .containsExactly(getExpectedEdouardRead(), getExpectedEdouardWrite(), getExpectedEdouardDelete());

        assertThat(getReadWriteDeleteRecordsForUser(users.gandalfIn(zeCollection)))
            .containsExactly(getExpectedGandalfRead(), getExpectedGandalfWrite(), getExpectedGandalfDelete());

        assertThat(getReadWriteDeleteRecordsForUser(users.robinIn(zeCollection)))
            .containsExactly(getExpectedRobinRead(), getExpectedRobinWrite(), getExpectedRobinDelete());

        assertThat(getReadWriteDeleteRecordsForUser(users.sasquatchIn(zeCollection)))
            .containsExactly(getExpectedSasquatchRead(), getExpectedSasquatchWrite(), getExpectedSasquatchDelete());
    }

    public List<List<String>> getReadWriteDeleteRecordsForUser(User user) {
        return asList(getReadRecordsForUser(user), getWriteRecordsForUser(user), getDeleteRecordsForUser(user));
    }

    public List<String> getReadRecordsForUser(User user) {
        SearchServices searchServices = getModelLayerFactory().newSearchServices();
        MetadataSchemaType type = getModelLayerFactory().getMetadataSchemasManager().getSchemaTypes(zeCollection).getSchemaType(Folder.SCHEMA_TYPE);
        return searchServices.searchRecordIds(new LogicalSearchQuery().setCondition(
                from(type).returnAll()).filteredWithUser(user));
    }

    public List<String> getWriteRecordsForUser(User user) {
        SearchServices searchServices = getModelLayerFactory().newSearchServices();
        MetadataSchemaType type = getModelLayerFactory().getMetadataSchemasManager().getSchemaTypes(zeCollection).getSchemaType(Folder.SCHEMA_TYPE);
        return searchServices.searchRecordIds(new LogicalSearchQuery().setCondition(
                from(type).returnAll()).filteredWithUserWrite(user));
    }

    public List<String> getDeleteRecordsForUser(User user) {
        SearchServices searchServices = getModelLayerFactory().newSearchServices();
        MetadataSchemaType type = getModelLayerFactory().getMetadataSchemasManager().getSchemaTypes(zeCollection).getSchemaType(Folder.SCHEMA_TYPE);
        return searchServices.searchRecordIds(new LogicalSearchQuery().setCondition(
                from(type).returnAll()).filteredWithUserDelete(user));
    }
}
