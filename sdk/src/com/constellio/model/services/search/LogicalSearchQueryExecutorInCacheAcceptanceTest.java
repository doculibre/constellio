package com.constellio.model.services.search;

import com.constellio.model.entities.CorePermissions;
import com.constellio.model.entities.records.Record;
import com.constellio.model.entities.records.wrappers.User;
import com.constellio.model.entities.schemas.Metadata;
import com.constellio.model.entities.schemas.MetadataSchema;
import com.constellio.model.entities.schemas.MetadataValueType;
import com.constellio.model.entities.security.Role;
import com.constellio.model.entities.security.global.AuthorizationAddRequest;
import com.constellio.model.services.records.RecordServices;
import com.constellio.model.services.schemas.MetadataSchemaTypesAlteration;
import com.constellio.model.services.schemas.MetadataSchemasManager;
import com.constellio.model.services.schemas.builders.MetadataSchemaBuilder;
import com.constellio.model.services.schemas.builders.MetadataSchemaTypeBuilder;
import com.constellio.model.services.search.query.logical.LogicalSearchQuery;
import com.constellio.model.services.search.query.logical.LogicalSearchQueryOperators;
import com.constellio.model.services.search.query.logical.QueryExecutionMethod;
import com.constellio.model.services.security.AuthorizationsServices;
import com.constellio.model.services.users.UserServices;
import com.constellio.sdk.tests.ConstellioTest;
import com.constellio.sdk.tests.setups.Users;
import org.jetbrains.annotations.NotNull;
import org.junit.Before;
import org.junit.Test;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import static com.constellio.model.entities.security.global.AuthorizationAddRequest.authorizationForUsers;
import static org.assertj.core.api.Assertions.assertThat;

public class LogicalSearchQueryExecutorInCacheAcceptanceTest extends ConstellioTest {

	Metadata cacheIndex;
	Metadata notCacheIndex;
	Metadata unique;
	Metadata cacheIndexMultiValue;

	MetadataSchema testsSchemaDefault;

	Users users = new Users();

	LogicalSearchQueryExecutorInCache logicalSearchQueryExecutorInCache;

	MetadataSchemasManager metadataSchemasManager;
	RecordServices recordServices;
	SearchServices searchServices;


	Record record1;
	Record record2;
	Record record3;
	Record record4;

	@Before
	public void beforeTest() throws Exception {
		prepareSystem(withZeCollection().withAllTestUsers());

		UserServices userServices = getModelLayerFactory().newUserServices();

		users.using(userServices);

		metadataSchemasManager = getModelLayerFactory().getMetadataSchemasManager();

		metadataSchemasManager.modify(zeCollection, (MetadataSchemaTypesAlteration) types -> {
			MetadataSchemaTypeBuilder testSchemaBuilder = types.createNewSchemaType("testschema").setSecurity(true);

			MetadataSchemaBuilder defaultTestSchemaBuilder = testSchemaBuilder.getDefaultSchema();

			defaultTestSchemaBuilder.create("cacheIndex").setType(MetadataValueType.STRING).setCacheIndex(true);
			defaultTestSchemaBuilder.create("notCacheIndex").setType(MetadataValueType.STRING);
			defaultTestSchemaBuilder.create("cacheIndexMultiValue").setType(MetadataValueType.STRING).setCacheIndex(true)
					.setMultivalue(true);
			defaultTestSchemaBuilder.create("unique").setType(MetadataValueType.STRING).setCacheIndex(true);
		});

		recordServices = getModelLayerFactory().newRecordServices();
		searchServices = getModelLayerFactory().newSearchServices();
		testsSchemaDefault = metadataSchemasManager.getSchemaTypes(zeCollection).getSchemaType("testschema").getDefaultSchema();

		logicalSearchQueryExecutorInCache = new LogicalSearchQueryExecutorInCache(searchServices, searchServices.getConnectedRecordsCache(),
				metadataSchemasManager,
				getModelLayerFactory().getSearchConfigurationsManager(),
				getModelLayerFactory().getExtensions().getSystemWideExtensions(), getModelLayerFactory().getConfiguration().getMainDataLanguage());

		cacheIndex = testsSchemaDefault.getMetadata("cacheIndex");
		notCacheIndex = testsSchemaDefault.getMetadata("notCacheIndex");
		unique = testsSchemaDefault.getMetadata("unique");
		cacheIndexMultiValue = testsSchemaDefault.getMetadata("cacheIndexMultiValue");

		record1 = recordServices.newRecordWithSchema(testsSchemaDefault);
		record1.set(cacheIndex, "toBeFound1");
		record1.set(notCacheIndex, "nonCached1");
		record1.set(unique, "unique1");
		recordServices.add(record1);

		record2 = recordServices.newRecordWithSchema(testsSchemaDefault);
		record2.set(cacheIndex, "toBeFound2");
		record2.set(unique, "unique2");
		recordServices.add(record2);

		record3 = recordServices.newRecordWithSchema(testsSchemaDefault);
		record3.set(cacheIndex, "toBeFound3");
		record3.set(unique, "unique3");
		recordServices.add(record3);

		record4 = recordServices.newRecordWithSchema(testsSchemaDefault);
		record4.set(cacheIndex, "toBeFound3");
		record4.set(unique, "unique4");
		recordServices.add(record4);
	}

	@Test
	public void testUserFilterReadWithUser() throws Exception {
		User aliceInCollection = users.aliceIn(zeCollection);

		aliceInCollection.setCollectionReadAccess(false);
		recordServices.update(aliceInCollection);

		LogicalSearchQuery logicalSearchQuery = new LogicalSearchQuery(LogicalSearchQueryOperators
				.from(testsSchemaDefault).where(cacheIndex).isEqualTo("toBeFound3")).filteredWithUser(aliceInCollection);

		validateExecutableInCacheTrue(logicalSearchQuery);

		List<Record> recordsResult1 = logicalSearchQueryExecutorInCache.stream(logicalSearchQuery).collect(Collectors.toList());
		assertThat(recordsResult1.size()).isEqualTo(0);

		AuthorizationsServices authorizationsServices = getModelLayerFactory().newAuthorizationsServices();
		AuthorizationAddRequest authorizationAddRequest = authorizationForUsers(aliceInCollection).givingReadAccess().on(record3);

		authorizationsServices.add(authorizationAddRequest);

		List<Record> recordsResult2 = logicalSearchQueryExecutorInCache.stream(logicalSearchQuery).collect(Collectors.toList());
		assertThat(recordsResult2.size()).isEqualTo(1);
		assertThat(recordsResult2.get(0).getId()).isEqualTo(record3.getId());

	}

	@Test
	public void testIsQueryExecutableInCache() throws Exception {
		User aliceInCollection = users.aliceIn(zeCollection);

		aliceInCollection.setCollectionReadAccess(false);
		recordServices.update(aliceInCollection);

		LogicalSearchQuery logicalSearchQuery1 = createValidQuery().filteredWithUser(aliceInCollection, CorePermissions.VIEW_EVENTS);

		assertThat(logicalSearchQueryExecutorInCache.isQueryExecutableInCache(logicalSearchQuery1)).isFalse();

		LogicalSearchQuery logicalSearchQuery2 = createValidQuery().filteredWithUser(aliceInCollection, CorePermissions.MANAGE_LDAP);

		assertThat(logicalSearchQueryExecutorInCache.isQueryExecutableInCache(logicalSearchQuery2)).isFalse();

		LogicalSearchQuery logicalSearchQuery3 = createValidQuery().filteredWithUser(aliceInCollection, Role.READ);

		assertThat(logicalSearchQueryExecutorInCache.isQueryExecutableInCache(logicalSearchQuery3)).isTrue();

		LogicalSearchQuery logicalSearchQuery4 = createValidQuery().filteredWithUser(aliceInCollection, Role.WRITE);

		assertThat(logicalSearchQueryExecutorInCache.isQueryExecutableInCache(logicalSearchQuery4)).isTrue();

		LogicalSearchQuery logicalSearchQuery5 = createValidQuery().filteredWithUser(aliceInCollection, Role.DELETE);

		assertThat(logicalSearchQueryExecutorInCache.isQueryExecutableInCache(logicalSearchQuery5)).isTrue();

	}

	@NotNull
	private LogicalSearchQuery createValidQuery() {
		return new LogicalSearchQuery(LogicalSearchQueryOperators
				.from(testsSchemaDefault).where(cacheIndex).isEqualTo("toBeFound3"));
	}

	private void validateExecutableInCacheTrue(LogicalSearchQuery logicalSearchQuery) {
		boolean isExecutableInCache = logicalSearchQueryExecutorInCache.isQueryExecutableInCache(logicalSearchQuery.setQueryExecutionMethod(QueryExecutionMethod.USE_CACHE));
		assertThat(isExecutableInCache).isTrue();
	}

	@Test
	public void testCompositeLogicalSearchConditionIsEqualCriterionThenSmallBaseListUsed() {
		LogicalSearchQuery logicalSearchQuery = new LogicalSearchQuery(LogicalSearchQueryOperators
				.from(testsSchemaDefault).where(cacheIndex).isEqualTo("toBeFound3").andWhere(unique).isEqualTo("unique3"));

		validateExecutableInCacheTrue(logicalSearchQuery);

		List<Record> queryResult = logicalSearchQueryExecutorInCache.stream(logicalSearchQuery).collect(Collectors.toList());

		assertThat(queryResult.size()).isEqualTo(1);
		assertThat(queryResult.get(0).getId()).isEqualTo(record3.getId());

	}

	@Test
	public void testDataStoreFieldLogicalSearchConditionIsEqualCriterionThenSmallBaseListUsed() {
		LogicalSearchQuery logicalSearchQuery = new LogicalSearchQuery(LogicalSearchQueryOperators
				.from(testsSchemaDefault).where(cacheIndex).isEqualTo("toBeFound3"));

		validateExecutableInCacheTrue(logicalSearchQuery);

		List<Record> queryResult = logicalSearchQueryExecutorInCache.stream(logicalSearchQuery).collect(Collectors.toList());

		assertThat(queryResult).extracting("id").containsOnly(record3.getId(), record4.getId());

	}

	@Test
	public void testDataStoreFieldLogicalSearchConditionIsEqualCriterionOnNonCacheIndexThenSmallBaseListNotUsed() {
		LogicalSearchQuery logicalSearchQuery = new LogicalSearchQuery(LogicalSearchQueryOperators
				.from(testsSchemaDefault).where(notCacheIndex).isEqualTo("nonCached1"));

		validateExecutableInCacheTrue(logicalSearchQuery);

		List<Record> queryResult = logicalSearchQueryExecutorInCache.stream(logicalSearchQuery).collect(Collectors.toList());

		assertThat(queryResult.size()).isEqualTo(1);

	}

	@Test
	public void testCompositeLogicalSearchConditionIsEqualCriterionNotUsingCacheWhenWrongTypeThenSmallBaseListNotUsed() {
		LogicalSearchQuery logicalSearchQuery = new LogicalSearchQuery(LogicalSearchQueryOperators
				.from(testsSchemaDefault).where(cacheIndex).isEqualTo(Arrays.asList("toBeFound2")).andWhere(unique).isEqualTo(Arrays.asList("unique2")));

		validateExecutableInCacheTrue(logicalSearchQuery);

		List<Record> queryResult = logicalSearchQueryExecutorInCache.stream(logicalSearchQuery).collect(Collectors.toList());

		assertThat(queryResult.size()).isEqualTo(0);

	}

	@Test
	public void testDataStoreFieldLogicalSearchConditionIsEqualCriterionNotUsingCacheWhenWrongTypeThenSmallBaseListNotUsed() {
		LogicalSearchQuery logicalSearchQuery = new LogicalSearchQuery(LogicalSearchQueryOperators
				.from(testsSchemaDefault).where(cacheIndex).isEqualTo(Arrays.asList("unique3")));

		validateExecutableInCacheTrue(logicalSearchQuery);

		List<Record> queryResult = logicalSearchQueryExecutorInCache.stream(logicalSearchQuery).collect(Collectors.toList());

		assertThat(queryResult.size()).isEqualTo(0);

	}
}
