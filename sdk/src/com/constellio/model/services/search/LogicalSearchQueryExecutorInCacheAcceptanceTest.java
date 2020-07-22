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
import com.constellio.model.services.search.query.logical.QueryExecutionMethod;
import com.constellio.model.services.security.AuthorizationsServices;
import com.constellio.model.services.users.UserServices;
import com.constellio.sdk.tests.ConstellioTest;
import com.constellio.sdk.tests.QueryCounter;
import com.constellio.sdk.tests.setups.Users;
import org.jetbrains.annotations.NotNull;
import org.junit.Before;
import org.junit.Test;

import java.util.List;

import static com.constellio.model.entities.security.global.AuthorizationAddRequest.authorizationForUsers;
import static com.constellio.model.services.search.query.logical.LogicalSearchQueryOperators.from;
import static com.constellio.model.services.search.query.logical.QueryExecutionMethod.USE_CACHE;
import static com.constellio.model.services.search.query.logical.QueryExecutionMethod.USE_SOLR;
import static java.util.Arrays.asList;
import static java.util.stream.Collectors.toList;
import static org.assertj.core.api.Assertions.assertThat;

public class LogicalSearchQueryExecutorInCacheAcceptanceTest extends ConstellioTest {

	Metadata cacheIndexMetadata;
	Metadata notCacheIndexMetadata;
	Metadata uniqueMetadata;
	Metadata cacheIndexMultiValue;

	MetadataSchema testsSchemaDefault;
	MetadataSchema testsAnotherSchemaDefault;

	Users users = new Users();

	LogicalSearchQueryExecutorInCache logicalSearchQueryExecutorInCache;

	MetadataSchemasManager metadataSchemasManager;
	RecordServices recordServices;
	SearchServices searchServices;


	Record record1;
	Record record2;
	Record record3;
	Record record4;

	Record anotherSchemaRecord1;
	Record anotherSchemaRecord2;
	Record anotherSchemaRecord3;
	Record anotherSchemaRecord4;

	@Before
	public void beforeTest() throws Exception {
		prepareSystem(withZeCollection().withAllTestUsers());

		UserServices userServices = getModelLayerFactory().newUserServices();

		users.using(userServices);

		metadataSchemasManager = getModelLayerFactory().getMetadataSchemasManager();

		metadataSchemasManager.modify(zeCollection, (MetadataSchemaTypesAlteration) types -> {
			MetadataSchemaTypeBuilder testSchemaBuilder = types.createNewSchemaType("testschema").setSecurity(true);
			MetadataSchemaTypeBuilder testSchema2Builder = types.createNewSchemaType("testschema2").setSecurity(true);

			MetadataSchemaBuilder defaultTestSchemaBuilder = testSchemaBuilder.getDefaultSchema();
			defaultTestSchemaBuilder.get("title").setSortable(true);
			defaultTestSchemaBuilder.create("cacheIndexMetadata").setType(MetadataValueType.STRING).setCacheIndex(true);
			defaultTestSchemaBuilder.create("notCacheIndexMetadata").setType(MetadataValueType.STRING);
			defaultTestSchemaBuilder.create("cacheIndexMultiValue").setType(MetadataValueType.STRING).setCacheIndex(true)
					.setMultivalue(true);
			defaultTestSchemaBuilder.create("uniqueMetadata").setType(MetadataValueType.STRING).setCacheIndex(true);
		});

		recordServices = getModelLayerFactory().newRecordServices();
		searchServices = getModelLayerFactory().newSearchServices();
		testsSchemaDefault = metadataSchemasManager.getSchemaTypes(zeCollection).getSchemaType("testschema").getDefaultSchema();
		testsAnotherSchemaDefault = metadataSchemasManager.getSchemaTypes(zeCollection).getSchemaType("testschema2").getDefaultSchema();

		logicalSearchQueryExecutorInCache = new LogicalSearchQueryExecutorInCache(searchServices, searchServices.getConnectedRecordsCache(),
				metadataSchemasManager,
				getModelLayerFactory().getSearchConfigurationsManager(),
				getModelLayerFactory().getExtensions().getSystemWideExtensions(), getModelLayerFactory().getSystemConfigs(), getModelLayerFactory().getConfiguration().getMainDataLanguage());

		cacheIndexMetadata = testsSchemaDefault.getMetadata("cacheIndexMetadata");
		notCacheIndexMetadata = testsSchemaDefault.getMetadata("notCacheIndexMetadata");
		uniqueMetadata = testsSchemaDefault.getMetadata("uniqueMetadata");
		cacheIndexMultiValue = testsSchemaDefault.getMetadata("cacheIndexMultiValue");

		record1 = recordServices.newRecordWithSchema(testsSchemaDefault);
		record1.set(cacheIndexMetadata, "toBeFound1");
		record1.set(notCacheIndexMetadata, "nonCached1");
		record1.set(uniqueMetadata, "unique1");
		recordServices.add(record1);

		record2 = recordServices.newRecordWithSchema(testsSchemaDefault);
		record2.set(cacheIndexMetadata, "toBeFound2");
		record2.set(uniqueMetadata, "unique2");
		recordServices.add(record2);

		record3 = recordServices.newRecordWithSchema(testsSchemaDefault);
		record3.set(cacheIndexMetadata, "toBeFound3");
		record3.set(uniqueMetadata, "unique3");
		recordServices.add(record3);

		record4 = recordServices.newRecordWithSchema(testsSchemaDefault);
		record4.set(cacheIndexMetadata, "toBeFound3");
		record4.set(uniqueMetadata, "unique4");
		recordServices.add(record4);


		anotherSchemaRecord1 = recordServices.newRecordWithSchema(testsAnotherSchemaDefault);
		recordServices.add(anotherSchemaRecord1);

		anotherSchemaRecord2 = recordServices.newRecordWithSchema(testsAnotherSchemaDefault);
		recordServices.add(anotherSchemaRecord2);

		anotherSchemaRecord3 = recordServices.newRecordWithSchema(testsAnotherSchemaDefault);
		recordServices.add(anotherSchemaRecord3);

		anotherSchemaRecord4 = recordServices.newRecordWithSchema(testsAnotherSchemaDefault);
		recordServices.add(anotherSchemaRecord4);
	}

	@Test
	public void testUserFilterReadWithUser() throws Exception {
		User aliceInCollection = users.aliceIn(zeCollection);

		aliceInCollection.setCollectionReadAccess(false);
		recordServices.update(aliceInCollection);

		LogicalSearchQuery logicalSearchQuery = new LogicalSearchQuery(from(testsSchemaDefault).where(cacheIndexMetadata).isEqualTo("toBeFound3")).filteredWithUser(aliceInCollection);

		validateExecutableInCacheTrue(logicalSearchQuery);

		List<Record> recordsResult1 = logicalSearchQueryExecutorInCache.stream(logicalSearchQuery).collect(toList());
		assertThat(recordsResult1.size()).isEqualTo(0);

		AuthorizationsServices authorizationsServices = getModelLayerFactory().newAuthorizationsServices();
		AuthorizationAddRequest authorizationAddRequest = authorizationForUsers(aliceInCollection).givingReadAccess().on(record3);

		authorizationsServices.add(authorizationAddRequest);

		List<Record> recordsResult2 = logicalSearchQueryExecutorInCache.stream(logicalSearchQuery).collect(toList());
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
		return new LogicalSearchQuery(from(testsSchemaDefault).where(cacheIndexMetadata).isEqualTo("toBeFound3"));
	}

	private void validateExecutableInCacheTrue(LogicalSearchQuery logicalSearchQuery) {
		boolean isExecutableInCache = logicalSearchQueryExecutorInCache.isQueryExecutableInCache(logicalSearchQuery.setQueryExecutionMethod(QueryExecutionMethod.USE_CACHE));
		assertThat(isExecutableInCache).isTrue();
	}

	@Test
	public void testCompositeLogicalSearchConditionIsEqualCriterionThenSmallBaseListUsed() throws Exception {
		LogicalSearchQuery logicalSearchQuery = new LogicalSearchQuery(from(testsSchemaDefault).where(cacheIndexMetadata).isEqualTo("toBeFound3").andWhere(uniqueMetadata).isEqualTo("unique3"));

		validateExecutableInCacheTrue(logicalSearchQuery);

		List<Record> queryResult = logicalSearchQueryExecutorInCache.stream(logicalSearchQuery).collect(toList());

		assertThat(queryResult.size()).isEqualTo(1);
		assertThat(queryResult.get(0).getId()).isEqualTo(record3.getId());

	}

	@Test
	public void testDataStoreFieldLogicalSearchConditionIsEqualCriterionThenSmallBaseListUsed() throws Exception {
		LogicalSearchQuery logicalSearchQuery = new LogicalSearchQuery(from(testsSchemaDefault).where(cacheIndexMetadata).isEqualTo("toBeFound3"));

		validateExecutableInCacheTrue(logicalSearchQuery);

		List<Record> queryResult = logicalSearchQueryExecutorInCache.stream(logicalSearchQuery).collect(toList());

		assertThat(queryResult).extracting("id").containsOnly(record3.getId(), record4.getId());

	}

	@Test
	public void testDataStoreFieldLogicalSearchConditionIsEqualCriterionOnNonCacheIndexThenSmallBaseListNotUsed()
			throws Exception {
		LogicalSearchQuery logicalSearchQuery = new LogicalSearchQuery(from(testsSchemaDefault).where(notCacheIndexMetadata).isEqualTo("nonCached1"));

		validateExecutableInCacheTrue(logicalSearchQuery);

		List<Record> queryResult = logicalSearchQueryExecutorInCache.stream(logicalSearchQuery).collect(toList());

		assertThat(queryResult.size()).isEqualTo(1);

	}

	@Test
	public void whenStreamingMultipleTypesWithSubQueriesThenOk() {
		LogicalSearchQuery logicalSearchQuery = new LogicalSearchQuery(from(testsSchemaDefault.getSchemaType(), testsAnotherSchemaDefault.getSchemaType())
				.returnAll());
		logicalSearchQuery.getCacheableQueries().add(new LogicalSearchQuery(from(testsSchemaDefault.getSchemaType()).returnAll()));
		logicalSearchQuery.getCacheableQueries().add(new LogicalSearchQuery(from(testsAnotherSchemaDefault.getSchemaType()).returnAll()));

		QueryCounter counter = newQueryCounter();

		logicalSearchQuery.setQueryExecutionMethod(USE_SOLR);
		assertThat(searchServices.stream(logicalSearchQuery).collect(toList())).extracting("id").isEqualTo(asList(
				record1.getId(),
				record2.getId(),
				record3.getId(),
				record4.getId(),
				anotherSchemaRecord1.getId(),
				anotherSchemaRecord2.getId(),
				anotherSchemaRecord3.getId(),
				anotherSchemaRecord4.getId()
		));
		assertThat(counter.newQueryCalls()).isEqualTo(1);

		logicalSearchQuery.setQueryExecutionMethod(USE_CACHE);
		assertThat(searchServices.stream(logicalSearchQuery).collect(toList())).extracting("id").isEqualTo(asList(
				record1.getId(),
				record2.getId(),
				record3.getId(),
				record4.getId(),
				anotherSchemaRecord1.getId(),
				anotherSchemaRecord2.getId(),
				anotherSchemaRecord3.getId(),
				anotherSchemaRecord4.getId()
		));
		assertThat(counter.newQueryCalls()).isEqualTo(0);
	}

	@Test
	public void testCompositeLogicalSearchConditionIsEqualCriterionNotUsingCacheWhenWrongTypeThenSmallBaseListNotUsed()
			throws Exception {
		LogicalSearchQuery logicalSearchQuery = new LogicalSearchQuery(from(testsSchemaDefault).where(cacheIndexMetadata).isEqualTo(asList("toBeFound2")).andWhere(uniqueMetadata).isEqualTo(asList("unique2")));

		validateExecutableInCacheTrue(logicalSearchQuery);

		List<Record> queryResult = logicalSearchQueryExecutorInCache.stream(logicalSearchQuery).collect(toList());

		assertThat(queryResult.size()).isEqualTo(0);

	}

	@Test
	public void testDataStoreFieldLogicalSearchConditionIsEqualCriterionNotUsingCacheWhenWrongTypeThenSmallBaseListNotUsed()
			throws Exception {
		LogicalSearchQuery logicalSearchQuery = new LogicalSearchQuery(from(testsSchemaDefault).where(cacheIndexMetadata).isEqualTo(asList("unique3")));

		validateExecutableInCacheTrue(logicalSearchQuery);

		List<Record> queryResult = logicalSearchQueryExecutorInCache.stream(logicalSearchQuery).collect(toList());

		assertThat(queryResult.size()).isEqualTo(0);

	}

	@Test
	public void givenQueryIsSortingOnTitleThenUsingMainSortFieldInsteadTitleSortNormalizer() throws Exception {
		LogicalSearchQuery logicalSearchQuery = new LogicalSearchQuery(from(testsSchemaDefault).where(cacheIndexMetadata).isEqualTo(asList("unique3")));

		validateExecutableInCacheTrue(logicalSearchQuery);

		List<Record> queryResult = logicalSearchQueryExecutorInCache.stream(logicalSearchQuery).collect(toList());

		assertThat(queryResult.size()).isEqualTo(0);

	}
}
