package com.constellio.model.entities.security;

import com.constellio.app.modules.rm.RMTestRecords;
import com.constellio.app.modules.rm.wrappers.Folder;
import com.constellio.model.entities.records.wrappers.Authorization;
import com.constellio.model.entities.records.wrappers.User;
import com.constellio.model.entities.schemas.MetadataSchemaType;
import com.constellio.model.entities.schemas.Schemas;
import com.constellio.model.services.records.SchemasRecordsServices;
import com.constellio.model.services.search.SearchServices;
import com.constellio.model.services.search.query.logical.LogicalSearchQuery;
import com.constellio.sdk.tests.ConstellioTest;
import com.constellio.sdk.tests.setups.Users;
import org.junit.Test;

import java.util.List;

import static com.constellio.model.entities.security.SolrAuthorizationDetailsAcceptanceTestResources.getExpectedAdminDelete;
import static com.constellio.model.entities.security.SolrAuthorizationDetailsAcceptanceTestResources.getExpectedAdminRead;
import static com.constellio.model.entities.security.SolrAuthorizationDetailsAcceptanceTestResources.getExpectedAdminWrite;
import static com.constellio.model.entities.security.SolrAuthorizationDetailsAcceptanceTestResources.getExpectedAliceDelete;
import static com.constellio.model.entities.security.SolrAuthorizationDetailsAcceptanceTestResources.getExpectedAliceRead;
import static com.constellio.model.entities.security.SolrAuthorizationDetailsAcceptanceTestResources.getExpectedAliceWrite;
import static com.constellio.model.entities.security.SolrAuthorizationDetailsAcceptanceTestResources.getExpectedBobDelete;
import static com.constellio.model.entities.security.SolrAuthorizationDetailsAcceptanceTestResources.getExpectedBobRead;
import static com.constellio.model.entities.security.SolrAuthorizationDetailsAcceptanceTestResources.getExpectedBobWrite;
import static com.constellio.model.entities.security.SolrAuthorizationDetailsAcceptanceTestResources.getExpectedCharlesDelete;
import static com.constellio.model.entities.security.SolrAuthorizationDetailsAcceptanceTestResources.getExpectedCharlesRead;
import static com.constellio.model.entities.security.SolrAuthorizationDetailsAcceptanceTestResources.getExpectedCharlesWrite;
import static com.constellio.model.entities.security.SolrAuthorizationDetailsAcceptanceTestResources.getExpectedChuckDelete;
import static com.constellio.model.entities.security.SolrAuthorizationDetailsAcceptanceTestResources.getExpectedChuckRead;
import static com.constellio.model.entities.security.SolrAuthorizationDetailsAcceptanceTestResources.getExpectedChuckWrite;
import static com.constellio.model.entities.security.SolrAuthorizationDetailsAcceptanceTestResources.getExpectedDakotaDelete;
import static com.constellio.model.entities.security.SolrAuthorizationDetailsAcceptanceTestResources.getExpectedDakotaRead;
import static com.constellio.model.entities.security.SolrAuthorizationDetailsAcceptanceTestResources.getExpectedDakotaWrite;
import static com.constellio.model.entities.security.SolrAuthorizationDetailsAcceptanceTestResources.getExpectedEdouardDelete;
import static com.constellio.model.entities.security.SolrAuthorizationDetailsAcceptanceTestResources.getExpectedEdouardRead;
import static com.constellio.model.entities.security.SolrAuthorizationDetailsAcceptanceTestResources.getExpectedEdouardWrite;
import static com.constellio.model.entities.security.SolrAuthorizationDetailsAcceptanceTestResources.getExpectedGandalfDelete;
import static com.constellio.model.entities.security.SolrAuthorizationDetailsAcceptanceTestResources.getExpectedGandalfRead;
import static com.constellio.model.entities.security.SolrAuthorizationDetailsAcceptanceTestResources.getExpectedGandalfWrite;
import static com.constellio.model.entities.security.SolrAuthorizationDetailsAcceptanceTestResources.getExpectedRobinDelete;
import static com.constellio.model.entities.security.SolrAuthorizationDetailsAcceptanceTestResources.getExpectedRobinRead;
import static com.constellio.model.entities.security.SolrAuthorizationDetailsAcceptanceTestResources.getExpectedRobinWrite;
import static com.constellio.model.entities.security.SolrAuthorizationDetailsAcceptanceTestResources.getExpectedSasquatchDelete;
import static com.constellio.model.entities.security.SolrAuthorizationDetailsAcceptanceTestResources.getExpectedSasquatchRead;
import static com.constellio.model.entities.security.SolrAuthorizationDetailsAcceptanceTestResources.getExpectedSasquatchWrite;
import static com.constellio.model.services.search.query.logical.LogicalSearchQueryOperators.from;
import static java.util.Arrays.asList;
import static org.assertj.core.api.Assertions.assertThat;

public class AuthorizationAcceptanceTest extends ConstellioTest {

	RMTestRecords records = new RMTestRecords(zeCollection);
	Users users = new Users();

	@Test
	public void whenQueryingFoldersFilteredByUserThenReturnsGoodFolders()
			throws Exception {

		prepareSystem(
				withZeCollection().withConstellioRMModule().withAllTestUsers()
						.withRMTest(records).withFoldersAndContainersOfEveryStatus().withDocumentsDecommissioningList()
		);

		users.setUp(getModelLayerFactory().newUserServices(), zeCollection);
		waitForBatchProcess();

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

	@Test
	public void givenAuthorizationWithTimeRangeThenActiveAtTheRightTime()
			throws Exception {

		prepareSystem(
				withZeCollection().withConstellioRMModule().withAllTestUsers()
		);

		SchemasRecordsServices schemas = new SchemasRecordsServices(zeCollection, getModelLayerFactory());
		Authorization auth = schemas.newSolrAuthorizationDetails();

		givenTimeIs(date(2017, 11, 16));

		//Auth is now active
		auth.setStartDate(date(2017, 11, 15));
		auth.setEndDate(date(2017, 11, 17));
		auth.setLastTokenRecalculate(date(2017, 11, 14));
		assertThat(auth.isActiveAuthorization()).isTrue();
		assertThat(auth.hasModifiedStatusSinceLastTokenRecalculate()).isTrue();

		//Auth is active and has accurate tokens
		auth.setStartDate(date(2017, 11, 15));
		auth.setEndDate(date(2017, 11, 17));
		auth.setLastTokenRecalculate(date(2017, 11, 15));
		assertThat(auth.isActiveAuthorization()).isTrue();
		assertThat(auth.hasModifiedStatusSinceLastTokenRecalculate()).isFalse();

		//Auth is active and has accurate tokens
		auth.setStartDate(date(2017, 11, 15));
		auth.setEndDate(date(2017, 11, 17));
		auth.setLastTokenRecalculate(date(2017, 11, 16));
		assertThat(auth.isActiveAuthorization()).isTrue();
		assertThat(auth.hasModifiedStatusSinceLastTokenRecalculate()).isFalse();

		//Auth is active and has accurate tokens
		auth.setStartDate(date(2017, 11, 15));
		auth.setEndDate(date(2017, 11, 17));
		auth.setLastTokenRecalculate(date(2017, 11, 17));
		assertThat(auth.isActiveAuthorization()).isTrue();
		assertThat(auth.hasModifiedStatusSinceLastTokenRecalculate()).isFalse();

		//Auth was already inactive the last time it was recalculated
		auth.setStartDate(date(2017, 11, 15));
		auth.setEndDate(date(2017, 11, 17));
		auth.setLastTokenRecalculate(date(2017, 11, 17));
		assertThat(auth.isActiveAuthorization()).isTrue();
		assertThat(auth.hasModifiedStatusSinceLastTokenRecalculate()).isFalse();

		//Auth is now inactive
		auth.setStartDate(date(2017, 11, 12));
		auth.setEndDate(date(2017, 11, 15));
		auth.setLastTokenRecalculate(date(2017, 11, 12));
		assertThat(auth.isActiveAuthorization()).isFalse();
		assertThat(auth.hasModifiedStatusSinceLastTokenRecalculate()).isTrue();

		//Auth is not yet active
		auth.setStartDate(date(2017, 11, 17));
		auth.setEndDate(null);
		auth.setLastTokenRecalculate(date(2017, 11, 15));
		assertThat(auth.isActiveAuthorization()).isFalse();
		assertThat(auth.hasModifiedStatusSinceLastTokenRecalculate()).isFalse();

		//Auth is now active
		auth.setStartDate(date(2017, 11, 16));
		auth.setEndDate(null);
		auth.setLastTokenRecalculate(date(2017, 11, 15));
		assertThat(auth.isActiveAuthorization()).isTrue();
		assertThat(auth.hasModifiedStatusSinceLastTokenRecalculate()).isTrue();

		//Auth is already active
		auth.setStartDate(date(2017, 11, 16));
		auth.setEndDate(null);
		auth.setLastTokenRecalculate(date(2017, 11, 16));
		assertThat(auth.isActiveAuthorization()).isTrue();
		assertThat(auth.hasModifiedStatusSinceLastTokenRecalculate()).isFalse();

		//Auth is already active
		auth.setStartDate(date(2017, 11, 16));
		auth.setEndDate(null);
		auth.setLastTokenRecalculate(date(2017, 11, 17));
		assertThat(auth.isActiveAuthorization()).isTrue();
		assertThat(auth.hasModifiedStatusSinceLastTokenRecalculate()).isFalse();

		//Auth is active
		auth.setStartDate(null);
		auth.setEndDate(date(2017, 11, 17));
		auth.setLastTokenRecalculate(date(2017, 11, 15));
		assertThat(auth.isActiveAuthorization()).isTrue();
		assertThat(auth.hasModifiedStatusSinceLastTokenRecalculate()).isFalse();

		//Auth is active
		auth.setStartDate(null);
		auth.setEndDate(date(2017, 11, 16));
		auth.setLastTokenRecalculate(date(2017, 11, 15));
		assertThat(auth.isActiveAuthorization()).isTrue();
		assertThat(auth.hasModifiedStatusSinceLastTokenRecalculate()).isFalse();

		//Auth is now inactive
		auth.setStartDate(null);
		auth.setEndDate(date(2017, 11, 15));
		auth.setLastTokenRecalculate(date(2017, 11, 14));
		assertThat(auth.isActiveAuthorization()).isFalse();
		assertThat(auth.hasModifiedStatusSinceLastTokenRecalculate()).isTrue();

		//Auth is already inactive
		auth.setStartDate(null);
		auth.setEndDate(date(2017, 11, 15));
		auth.setLastTokenRecalculate(date(2017, 11, 15));
		assertThat(auth.isActiveAuthorization()).isFalse();
		assertThat(auth.hasModifiedStatusSinceLastTokenRecalculate()).isTrue();

	}

	public List<List<String>> getReadWriteDeleteRecordsForUser(User user) {
		return asList(getReadRecordsForUser(user), getWriteRecordsForUser(user), getDeleteRecordsForUser(user));
	}

	public List<String> getReadRecordsForUser(User user) {
		SearchServices searchServices = getModelLayerFactory().newSearchServices();
		MetadataSchemaType type = getModelLayerFactory().getMetadataSchemasManager().getSchemaTypes(zeCollection)
				.getSchemaType(Folder.SCHEMA_TYPE);
		return searchServices.searchRecordIds(new LogicalSearchQuery().setCondition(
				from(type).returnAll()).filteredWithUser(user).sortAsc(Schemas.IDENTIFIER));
	}

	public List<String> getWriteRecordsForUser(User user) {
		SearchServices searchServices = getModelLayerFactory().newSearchServices();
		MetadataSchemaType type = getModelLayerFactory().getMetadataSchemasManager().getSchemaTypes(zeCollection)
				.getSchemaType(Folder.SCHEMA_TYPE);
		return searchServices.searchRecordIds(new LogicalSearchQuery().setCondition(
				from(type).returnAll()).filteredWithUserWrite(user).sortAsc(Schemas.IDENTIFIER));
	}

	public List<String> getDeleteRecordsForUser(User user) {
		SearchServices searchServices = getModelLayerFactory().newSearchServices();
		MetadataSchemaType type = getModelLayerFactory().getMetadataSchemasManager().getSchemaTypes(zeCollection)
				.getSchemaType(Folder.SCHEMA_TYPE);
		return searchServices.searchRecordIds(new LogicalSearchQuery().setCondition(
				from(type).returnAll()).filteredWithUserDelete(user).sortAsc(Schemas.IDENTIFIER));
	}

}
