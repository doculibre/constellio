package com.constellio.app.services.migrations.scripts;

import com.constellio.app.modules.rm.RMTestRecords;
import com.constellio.model.entities.records.Record;
import com.constellio.model.entities.records.wrappers.SavedSearch;
import com.constellio.model.entities.schemas.Metadata;
import com.constellio.model.entities.schemas.MetadataSchema;
import com.constellio.model.entities.schemas.MetadataSchemaTypes;
import com.constellio.model.entities.schemas.MetadataValueType;
import com.constellio.model.services.schemas.MetadataSchemasManager;
import com.constellio.model.services.search.SearchServices;
import com.constellio.sdk.tests.ConstellioTest;
import javafx.util.Pair;
import org.apache.solr.common.SolrDocument;
import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.constellio.model.services.search.query.logical.LogicalSearchQuery.query;
import static com.constellio.model.services.search.query.logical.LogicalSearchQueryOperators.from;
import static com.constellio.sdk.tests.TestUtils.assertThatRecord;
import static java.util.Arrays.asList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Fail.fail;

public class CoreMigrationTo_8_3_1_AcceptanceTest extends ConstellioTest {

	RMTestRecords records;
	SearchServices searchServices;
	MetadataSchemasManager manager;
	MetadataSchemaTypes schemaTypes;
	MetadataSchema savedSearchSchema;
	Metadata sharedGroupsId;
	Metadata sharedUsersId;
	Metadata sharedGroups;
	Metadata sharedUsers;

	Map<String, Pair<List<String>, List<String>>> correctValues;

	@Before
	public void setup() {
		records = new RMTestRecords(zeCollection);
		givenTransactionLogIsEnabled();

		getCurrentTestSession().getFactoriesTestFeatures()
				.givenSystemInState(getTestResourceFile("savestateWithSavedSearchToConvert.zip"))
				.withPasswordsReset().withFakeEncryptionServices();

		searchServices = getModelLayerFactory().newSearchServices();
		manager = getModelLayerFactory().getMetadataSchemasManager();
		schemaTypes = manager.getSchemaTypes(zeCollection);
		savedSearchSchema = schemaTypes.getSchema(SavedSearch.DEFAULT_SCHEMA);
		sharedGroupsId = savedSearchSchema.getMetadata("sharedGroupsId");
		sharedUsersId = savedSearchSchema.getMetadata("sharedUsersId");
		sharedGroups = savedSearchSchema.getMetadata(SavedSearch.SHARED_GROUPS);
		sharedUsers = savedSearchSchema.getMetadata(SavedSearch.SHARED_USERS);

		List<String> emptyList = new ArrayList<>();
		correctValues = new HashMap<>();
		correctValues.put("Abeille", new Pair<>(emptyList, emptyList));
		correctValues.put("Abricot", new Pair<>(asList("00000000045"), emptyList));
		correctValues.put("Aigle", new Pair<>(asList("00000000045", "00000000051"), emptyList));
		correctValues.put("Canard", new Pair<>(asList("00000000042"), asList("00000000062")));
		correctValues.put("Castor", new Pair<>(asList("00000000051", "00000000045"), asList("00000000070")));
		correctValues.put("Chat", new Pair<>(asList("00000000051"), asList("00000000065", "00000000066")));
		correctValues.put("Kiwi", new Pair<>(emptyList, asList("00000000065")));
		correctValues.put("Poire", new Pair<>(emptyList, asList("00000000068", "00000000069")));
	}

	@Test
	public void whenMigratingTo8_3_1ThenSavedSearchUsersAndGroupsAreTheSame() throws Exception {
		assertThat(sharedGroupsId).isNotNull();
		assertThat(sharedGroups).isNotNull();
		assertThat(sharedGroups.getType()).isEqualTo(MetadataValueType.REFERENCE);
		assertThat(sharedUsersId).isNotNull();
		assertThat(sharedUsers).isNotNull();
		assertThat(sharedUsers.getType()).isEqualTo(MetadataValueType.REFERENCE);

		List<Record> savedSearches = searchServices.search(query(
				from(schemaTypes.getSchemaType(SavedSearch.SCHEMA_TYPE)).returnAll()));

		for (Record record : savedSearches) {
			Pair<List<String>, List<String>> savedSearchTitleAndGroupsAndUsersSharedWith = correctValues.get(record.getTitle());
			if (savedSearchTitleAndGroupsAndUsersSharedWith == null) {
				fail("Invalid saved search title: " + record.getTitle());
			}

			assertThatRecord(record).extracting(sharedGroups, sharedUsers)
					.isEqualTo(asList(savedSearchTitleAndGroupsAndUsersSharedWith.getKey(),
							savedSearchTitleAndGroupsAndUsersSharedWith.getValue()));

			SolrDocument document = getModelLayerFactory().getDataLayerFactory().getRecordsVaultServer().realtimeGet(record.getId(), false);

			assertThat(document.get("sharedGroupsId_ss"))
					.isEqualTo(savedSearchTitleAndGroupsAndUsersSharedWith.getKey().isEmpty() ? null : savedSearchTitleAndGroupsAndUsersSharedWith.getKey());
			assertThat(document.get("sharedUsersId_ss"))
					.isEqualTo(savedSearchTitleAndGroupsAndUsersSharedWith.getValue().isEmpty() ? null : savedSearchTitleAndGroupsAndUsersSharedWith.getValue());
			assertThat(document.get("sharedGroups_ss")).isNull();
			assertThat(document.get("sharedUsers_ss")).isNull();

			//			assertThat(document.get("sharedGroups_ss")).isNull();
			//			assertThat(document.get("sharedUsers_ss")).isNull();
		}
	}
}
