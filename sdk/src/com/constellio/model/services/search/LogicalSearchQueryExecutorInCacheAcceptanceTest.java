package com.constellio.model.services.search;

import com.constellio.model.entities.records.Record;
import com.constellio.model.entities.schemas.Metadata;
import com.constellio.model.entities.schemas.MetadataSchema;
import com.constellio.model.entities.schemas.MetadataValueType;
import com.constellio.model.services.records.RecordServices;
import com.constellio.model.services.schemas.MetadataSchemaTypesAlteration;
import com.constellio.model.services.schemas.MetadataSchemasManager;
import com.constellio.model.services.schemas.builders.MetadataSchemaBuilder;
import com.constellio.model.services.schemas.builders.MetadataSchemaTypeBuilder;
import com.constellio.model.services.search.query.logical.LogicalSearchQuery;
import com.constellio.model.services.search.query.logical.LogicalSearchQueryOperators;
import com.constellio.sdk.tests.ConstellioTest;
import org.junit.Before;
import org.junit.Test;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.assertThat;

public class LogicalSearchQueryExecutorInCacheAcceptanceTest extends ConstellioTest {

	Metadata cacheIndex;
	Metadata notCacheIndex;
	Metadata unique;
	Metadata cacheIndexMultiValue;

	MetadataSchema testsSchemaDefault;

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
		prepareSystem(withZeCollection(), withCollection("secondCollection"));

		metadataSchemasManager = getModelLayerFactory().getMetadataSchemasManager();

		metadataSchemasManager.modify(zeCollection, (MetadataSchemaTypesAlteration) types -> {
			MetadataSchemaTypeBuilder testSchemaBuilder = types.createNewSchemaType("testschema");

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
				metadataSchemasManager, getModelLayerFactory().getExtensions().getSystemWideExtensions(), getModelLayerFactory().getConfiguration().getMainDataLanguage());

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
	public void testCompositeLogicalSearchConditionIsEqualCriterionThenSmallBaseListUsed() {
		LogicalSearchQuery logicalSearchQuery = new LogicalSearchQuery(LogicalSearchQueryOperators
				.from(testsSchemaDefault).where(cacheIndex).isEqualTo("toBeFound3").andWhere(unique).isEqualTo("unique3"));

		boolean isExecutableInCache = logicalSearchQueryExecutorInCache.isQueryExecutableInCache(logicalSearchQuery);
		assertThat(isExecutableInCache).isTrue();

		List<Record> queryResult = logicalSearchQueryExecutorInCache.stream(logicalSearchQuery).collect(Collectors.toList());

		assertThat(logicalSearchQueryExecutorInCache.getLastStreamInitialBaseRecordSize()).isEqualTo(2);
		assertThat(queryResult.size()).isEqualTo(1);
		assertThat(queryResult.get(0).getId()).isEqualTo(record3.getId());

	}

	@Test
	public void testDataStoreFieldLogicalSearchConditionIsEqualCriterionThenSmallBaseListUsed() {
		LogicalSearchQuery logicalSearchQuery = new LogicalSearchQuery(LogicalSearchQueryOperators
				.from(testsSchemaDefault).where(cacheIndex).isEqualTo("toBeFound3"));

		boolean isExecutableInCache = logicalSearchQueryExecutorInCache.isQueryExecutableInCache(logicalSearchQuery);
		assertThat(isExecutableInCache).isTrue();

		List<Record> queryResult = logicalSearchQueryExecutorInCache.stream(logicalSearchQuery).collect(Collectors.toList());

		assertThat(logicalSearchQueryExecutorInCache.getLastStreamInitialBaseRecordSize()).isEqualTo(2);
		assertThat(queryResult.size()).isEqualTo(2);
		assertThat(queryResult.get(0).getId()).isEqualTo(record3.getId());
		assertThat(queryResult.get(1).getId()).isEqualTo(record4.getId());

	}

	@Test
	public void testDataStoreFieldLogicalSearchConditionIsEqualCriterionOnNonCacheIndexThenSmallBaseListNotUsed() {
		LogicalSearchQuery logicalSearchQuery = new LogicalSearchQuery(LogicalSearchQueryOperators
				.from(testsSchemaDefault).where(notCacheIndex).isEqualTo("nonCached1"));

		boolean isExecutableInCache = logicalSearchQueryExecutorInCache.isQueryExecutableInCache(logicalSearchQuery);
		assertThat(isExecutableInCache).isTrue();

		List<Record> queryResult = logicalSearchQueryExecutorInCache.stream(logicalSearchQuery).collect(Collectors.toList());

		assertThat(logicalSearchQueryExecutorInCache.getLastStreamInitialBaseRecordSize()).isEqualTo(-1);
		assertThat(queryResult.size()).isEqualTo(1);

	}

	@Test
	public void testCompositeLogicalSearchConditionIsEqualCriterionNotUsingCacheWhenWrongTypeThenSmallBaseListNotUsed() {
		LogicalSearchQuery logicalSearchQuery = new LogicalSearchQuery(LogicalSearchQueryOperators
				.from(testsSchemaDefault).where(cacheIndex).isEqualTo(Arrays.asList("toBeFound2")).andWhere(unique).isEqualTo(Arrays.asList("unique2")));

		boolean isExecutableInCache = logicalSearchQueryExecutorInCache.isQueryExecutableInCache(logicalSearchQuery);
		assertThat(isExecutableInCache).isTrue();

		List<Record> queryResult = logicalSearchQueryExecutorInCache.stream(logicalSearchQuery).collect(Collectors.toList());

		assertThat(logicalSearchQueryExecutorInCache.getLastStreamInitialBaseRecordSize()).isEqualTo(-1);
		assertThat(queryResult.size()).isEqualTo(0);

	}

	@Test
	public void testDataStoreFieldLogicalSearchConditionIsEqualCriterionNotUsingCacheWhenWrongTypeThenSmallBaseListNotUsed() {
		LogicalSearchQuery logicalSearchQuery = new LogicalSearchQuery(LogicalSearchQueryOperators
				.from(testsSchemaDefault).where(cacheIndex).isEqualTo(Arrays.asList("unique3")));

		boolean isExecutableInCache = logicalSearchQueryExecutorInCache.isQueryExecutableInCache(logicalSearchQuery);
		assertThat(isExecutableInCache).isTrue();

		List<Record> queryResult = logicalSearchQueryExecutorInCache.stream(logicalSearchQuery).collect(Collectors.toList());

		assertThat(logicalSearchQueryExecutorInCache.getLastStreamInitialBaseRecordSize()).isEqualTo(-1);
		assertThat(queryResult.size()).isEqualTo(0);

	}
}
