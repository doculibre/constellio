package com.constellio.model.services.records.cache2;

import com.constellio.data.dao.dto.records.SolrRecordDTO;
import com.constellio.model.entities.schemas.RecordCacheType;
import com.constellio.model.services.collections.CollectionsListManager;
import com.constellio.model.services.records.RecordServices;
import com.constellio.model.services.records.reindexing.ReindexingServices;
import com.constellio.model.services.schemas.MetadataSchemaTypesAlteration;
import com.constellio.model.services.schemas.MetadataSchemasManager;
import com.constellio.model.services.schemas.builders.MetadataSchemaTypesBuilder;
import com.constellio.sdk.tests.ConstellioTest;
import com.constellio.sdk.tests.schemas.TestsSchemasSetup;
import org.apache.commons.lang3.StringUtils;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.io.File;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

import static com.constellio.sdk.tests.schemas.TestsSchemasSetup.whichIsEssentialInSummary;
import static java.util.stream.Collectors.toList;
import static org.assertj.core.api.Assertions.assertThat;

public class RecordsCachesDataStoreAcceptanceTest extends ConstellioTest {

	MemoryEfficientRecordsCachesDataStore dataStore;

	TestsSchemasSetup setup = new TestsSchemasSetup(zeCollection);
	TestsSchemasSetup.ZeSchemaMetadatas zeSchema = setup.new ZeSchemaMetadatas();
	TestsSchemasSetup.AnotherSchemaMetadatas anotherSchema = setup.new AnotherSchemaMetadatas();

	MetadataSchemasManager schemasManager;
	RecordServices recordServices;
	ReindexingServices reindexingServices;

	@Before
	public void setUp() throws Exception {
		schemasManager = getModelLayerFactory().getMetadataSchemasManager();
		recordServices = getModelLayerFactory().newRecordServices();
		reindexingServices = getModelLayerFactory().newReindexingServices();

		SummaryCacheSingletons.dataStore = new FileSystemRecordsValuesCacheDataStore(new File(newTempFolder(), "test.db"));
	}


	@After
	public void tearDown() throws Exception {
		SummaryCacheSingletons.dataStore.close();
	}


	MetadataSchemasManager metadataSchemasManager;
	CollectionsListManager collectionsListManager;

	byte zeCollectionId;
	byte anotherCollectionId;
	int zeCollectionIndex;
	int anotherCollectionIndex;
	short zeCollectionType1Id;
	short zeCollectionType2Id;
	short zeSchemaDefaultId;

	String[] collections = new String[]{"zeCollection", "anotherCollection"};
	String[] types = new String[]{"type1", "type2"};

	private void initTestVariables() {
		collectionsListManager = getModelLayerFactory().getCollectionsListManager();
		zeCollectionId = collectionsListManager.getCollectionId(collections[0]);
		anotherCollectionId = collectionsListManager.getCollectionId(collections[1]);

		metadataSchemasManager = getModelLayerFactory().getMetadataSchemasManager();

		zeCollectionType1Id = metadataSchemasManager.getSchemaTypes(zeCollectionId).getSchemaType(zeSchema.typeCode()).getId();
		zeCollectionType2Id = metadataSchemasManager.getSchemaTypes(zeCollectionId).getSchemaType(anotherSchema.typeCode()).getId();
		zeCollectionIndex = zeCollectionId - Byte.MIN_VALUE;
		anotherCollectionIndex = anotherCollectionId - Byte.MIN_VALUE;
		dataStore = new MemoryEfficientRecordsCachesDataStore(getModelLayerFactory());
		zeSchemaDefaultId = zeSchema.instance().getId();
	}

	@Test
	public void whenPreloadingCacheWithSpacedIdsThenNoSpaceReservedForIdsBetween() throws Exception {
		defineSchemasManager().using(setup.withABooleanMetadata(whichIsEssentialInSummary));

		getModelLayerFactory().getMetadataSchemasManager().modify(zeCollection, new MetadataSchemaTypesAlteration() {
			@Override
			public void alter(MetadataSchemaTypesBuilder types) {
				types.getSchemaType(zeSchema.typeCode()).setRecordCacheType(RecordCacheType.SUMMARY_CACHED_WITH_VOLATILE);
			}
		});
		initTestVariables();

		ByteArrayRecordDTO dto1, dto2, dto3, dto6, dto7, dto8;
		dto1 = create(new SolrRecordDTO(zeroPadded(1), 12L, fields("zeCollection", zeSchema.code()), true));
		dto3 = create(new SolrRecordDTO(zeroPadded(3), 23L, fields("zeCollection", zeSchema.code()), true));
		dto6 = create(new SolrRecordDTO(zeroPadded(6), 34L, fields("zeCollection", zeSchema.code()), true));
		dto8 = create(new SolrRecordDTO(zeroPadded(8), 45L, fields("zeCollection", zeSchema.code()), true));

		dataStore.set(1, dto1, false);
		dataStore.set(3, dto3, false);
		dataStore.set(6, dto6, false);
		dataStore.set(8, dto8, true);

		assertThat(dataStore.ids[zeCollectionIndex][zeCollectionType1Id].stream().collect(toList()))
				.containsExactly(1, 3, 6, 7, 8);

		assertThat(dataStore.versions[zeCollectionIndex][zeCollectionType1Id].stream().collect(toList()))
				.containsExactly(12L, 23L, 34L, 0L, 45L);

		assertThat(dataStore.schema[zeCollectionIndex][zeCollectionType1Id].stream().collect(toList()))
				.containsExactly(zeSchemaDefaultId, zeSchemaDefaultId, zeSchemaDefaultId, (short) 0, zeSchemaDefaultId);

		assertThat(dataStore.stream(zeCollectionId, zeCollectionType1Id).collect(Collectors.toList()))
				.containsExactly(dto1, dto3, dto6, dto8);


	}


	@Test
	public void givenRecordAddedAfterPreloadingTheAddedReservingSpaceAllowingToAddTheRecordsWithoutReallocate()
			throws Exception {
		defineSchemasManager().using(setup.withABooleanMetadata(whichIsEssentialInSummary));

		getModelLayerFactory().getMetadataSchemasManager().modify(zeCollection, new MetadataSchemaTypesAlteration() {
			@Override
			public void alter(MetadataSchemaTypesBuilder types) {
				types.getSchemaType(zeSchema.typeCode()).setRecordCacheType(RecordCacheType.SUMMARY_CACHED_WITHOUT_VOLATILE);
			}
		});
		initTestVariables();

		ByteArrayRecordDTO dto1, dto2, dto3, dto6, dto7, dto8, dto10, dto12;
		dto1 = create(new SolrRecordDTO(zeroPadded(1), 12L, fields("zeCollection", zeSchema.code()), true));
		dto3 = create(new SolrRecordDTO(zeroPadded(3), 23L, fields("zeCollection", zeSchema.code()), true));
		dto6 = create(new SolrRecordDTO(zeroPadded(6), 34L, fields("zeCollection", zeSchema.code()), true));
		dto7 = create(new SolrRecordDTO(zeroPadded(7), 56L, fields("zeCollection", zeSchema.code()), true));
		dto8 = create(new SolrRecordDTO(zeroPadded(8), 67L, fields("zeCollection", zeSchema.code()), true));
		dto10 = create(new SolrRecordDTO(zeroPadded(10), 111L, fields("zeCollection", zeSchema.code()), true));
		dto12 = create(new SolrRecordDTO(zeroPadded(12), 222L, fields("zeCollection", zeSchema.code()), true));

		dataStore.set(1, dto1, false);
		dataStore.set(3, dto3, false);
		dataStore.set(6, dto6, false);
		dataStore.set(8, dto8, true);
		dataStore.set(7, dto7, true);

		assertThat(dataStore.ids[zeCollectionIndex][zeCollectionType1Id].stream().collect(toList()))
				.containsExactly(1, 3, 6, 7, 8);

		assertThat(dataStore.versions[zeCollectionIndex][zeCollectionType1Id].stream().collect(toList()))
				.containsExactly(12L, 23L, 34L, 56L, 67L);

		assertThat(dataStore.schema[zeCollectionIndex][zeCollectionType1Id].stream().collect(toList()))
				.containsExactly(zeSchemaDefaultId, zeSchemaDefaultId, zeSchemaDefaultId, zeSchemaDefaultId, zeSchemaDefaultId);

		assertThat(dataStore.stream(zeCollectionId, zeCollectionType1Id).collect(Collectors.toList()))
				.containsExactly(dto1, dto3, dto6, dto7, dto8);

		dataStore.set(10, dto10, true);

		assertThat(dataStore.ids[zeCollectionIndex][zeCollectionType1Id].stream().collect(toList()))
				.containsExactly(1, 3, 6, 7, 8, 9, 10);

		assertThat(dataStore.versions[zeCollectionIndex][zeCollectionType1Id].stream().collect(toList()))
				.containsExactly(12L, 23L, 34L, 56L, 67L, 0L, 111L);

		assertThat(dataStore.schema[zeCollectionIndex][zeCollectionType1Id].stream().collect(toList()))
				.containsExactly(zeSchemaDefaultId, zeSchemaDefaultId, zeSchemaDefaultId, zeSchemaDefaultId, zeSchemaDefaultId, (short) 0, zeSchemaDefaultId);

		assertThat(dataStore.stream(zeCollectionId, zeCollectionType1Id).collect(Collectors.toList()))
				.containsExactly(dto1, dto3, dto6, dto7, dto8, dto10);

		dataStore.set(12, dto12, false);

		assertThat(dataStore.ids[zeCollectionIndex][zeCollectionType1Id].stream().collect(toList()))
				.containsExactly(1, 3, 6, 7, 8, 9, 10, 12);

		assertThat(dataStore.versions[zeCollectionIndex][zeCollectionType1Id].stream().collect(toList()))
				.containsExactly(12L, 23L, 34L, 56L, 67L, 0L, 111L, 222L);

		assertThat(dataStore.schema[zeCollectionIndex][zeCollectionType1Id].stream().collect(toList()))
				.containsExactly(zeSchemaDefaultId, zeSchemaDefaultId, zeSchemaDefaultId, zeSchemaDefaultId, zeSchemaDefaultId, (short) 0, zeSchemaDefaultId, zeSchemaDefaultId);

		assertThat(dataStore.stream(zeCollectionId, zeCollectionType1Id).collect(Collectors.toList()))
				.containsExactly(dto1, dto3, dto6, dto7, dto8, dto10, dto12);

	}


	@Test
	public void whenSavingSolrDTOThenPersistedInHeapWithoutConversion()
			throws Exception {
		defineSchemasManager().using(setup.withABooleanMetadata(whichIsEssentialInSummary));

		getModelLayerFactory().getMetadataSchemasManager().modify(zeCollection, new MetadataSchemaTypesAlteration() {
			@Override
			public void alter(MetadataSchemaTypesBuilder types) {
				types.getSchemaType(zeSchema.typeCode()).setRecordCacheType(RecordCacheType.FULLY_CACHED);
			}
		});
		initTestVariables();

		SolrRecordDTO dto1, dto3, dto6, dto7, dto8, dto9, dto12, dto14;
		dto1 = new SolrRecordDTO(zeroPadded(1), 12L, fields("zeCollection", zeSchema.code()), false);
		dto3 = new SolrRecordDTO(zeroPadded(3), 23L, fields("zeCollection", zeSchema.code()), false);
		dto6 = new SolrRecordDTO(zeroPadded(6), 34L, fields("zeCollection", zeSchema.code()), false);
		dto7 = new SolrRecordDTO(zeroPadded(7), 56L, fields("zeCollection", zeSchema.code()), false);
		dto8 = new SolrRecordDTO(zeroPadded(8), 67L, fields("zeCollection", zeSchema.code()), false);
		dto9 = new SolrRecordDTO(zeroPadded(9), 45L, fields("zeCollection", zeSchema.code()), false);
		dto12 = new SolrRecordDTO(zeroPadded(12), 111L, fields("zeCollection", zeSchema.code()), false);
		dto14 = new SolrRecordDTO(zeroPadded(14), 222L, fields("zeCollection", zeSchema.code()), false);

		dataStore.set(1, dto1, false);
		dataStore.set(3, dto3, false);
		dataStore.set(6, dto6, false);
		dataStore.set(9, dto9, true);
		dataStore.set(7, dto7, true);

		assertThat(dataStore.ids[zeCollectionIndex][zeCollectionType1Id].stream().collect(toList()))
				.containsExactly(1, 3, 6, 7, 8, 9);

		assertThat(dataStore.versions[zeCollectionIndex][zeCollectionType1Id].stream().collect(toList()))
				.containsExactly(12L, 23L, 34L, 56L, 0L, 45L);

		assertThat(dataStore.schema[zeCollectionIndex][zeCollectionType1Id].stream().collect(toList()))
				.containsExactly(zeSchemaDefaultId, zeSchemaDefaultId, zeSchemaDefaultId, zeSchemaDefaultId, (short) 0, zeSchemaDefaultId);

		assertThat(dataStore.stream(zeCollectionId, zeCollectionType1Id).collect(Collectors.toList()))
				.containsExactly(dto1, dto3, dto6, dto7, dto9);

		dataStore.set(8, dto8, true);

		assertThat(dataStore.stream(zeCollectionId, zeCollectionType1Id).collect(Collectors.toList()))
				.containsExactly(dto1, dto3, dto6, dto7, dto8, dto9);

		dataStore.set(12, dto12, true);

		assertThat(dataStore.ids[zeCollectionIndex][zeCollectionType1Id].stream().collect(toList()))
				.containsExactly(1, 3, 6, 7, 8, 9, 10, 11, 12);

		assertThat(dataStore.versions[zeCollectionIndex][zeCollectionType1Id].stream().collect(toList()))
				.containsExactly(12L, 23L, 34L, 56L, 67L, 45L, 0L, 0L, 111L);

		assertThat(dataStore.stream(zeCollectionId, zeCollectionType1Id).collect(Collectors.toList()))
				.containsExactly(dto1, dto3, dto6, dto7, dto8, dto9, dto12);


		dataStore.set(14, dto14, false);

		assertThat(dataStore.ids[zeCollectionIndex][zeCollectionType1Id].stream().collect(toList()))
				.containsExactly(1, 3, 6, 7, 8, 9, 10, 11, 12, 14);

		assertThat(dataStore.versions[zeCollectionIndex][zeCollectionType1Id].stream().collect(toList()))
				.containsExactly(12L, 23L, 34L, 56L, 67L, 45L, 0L, 0L, 111L, 222L);

		assertThat(dataStore.stream(zeCollectionId, zeCollectionType1Id).collect(Collectors.toList()))
				.containsExactly(dto1, dto3, dto6, dto7, dto8, dto9, dto12, dto14);
	}


	@Test
	public void whenAddingAFullyCachedRecordInASpaceThatWasNotReservedThenRecreateArraysAddingNewReservedIndex()
			throws Exception {
		defineSchemasManager().using(setup.withABooleanMetadata(whichIsEssentialInSummary));

		getModelLayerFactory().getMetadataSchemasManager().modify(zeCollection, new MetadataSchemaTypesAlteration() {
			@Override
			public void alter(MetadataSchemaTypesBuilder types) {
				types.getSchemaType(zeSchema.typeCode()).setRecordCacheType(RecordCacheType.FULLY_CACHED);
			}
		});
		initTestVariables();

		SolrRecordDTO dto1, dto2, dto3, dto6, dto7, dto8;
		dto1 = new SolrRecordDTO(zeroPadded(1), 12L, fields("zeCollection", zeSchema.code()), false);
		dto2 = new SolrRecordDTO(zeroPadded(2), 89L, fields("zeCollection", zeSchema.code()), false);
		dto3 = new SolrRecordDTO(zeroPadded(3), 23L, fields("zeCollection", zeSchema.code()), false);
		dto6 = new SolrRecordDTO(zeroPadded(6), 34L, fields("zeCollection", zeSchema.code()), false);
		dto7 = new SolrRecordDTO(zeroPadded(7), 11L, fields("zeCollection", zeSchema.code()), false);
		dto8 = new SolrRecordDTO(zeroPadded(8), 67L, fields("zeCollection", zeSchema.code()), false);

		dataStore.set(1, dto1, false);
		dataStore.set(3, dto3, false);
		dataStore.set(6, dto6, false);

		assertThat(dataStore.ids[zeCollectionIndex][zeCollectionType1Id].stream().collect(toList()))
				.containsExactly(1, 3, 6);
		dataStore.set(8, dto8, false);

		//There is no space for record 2, which is a normal case
		assertThat(dataStore.ids[zeCollectionIndex][zeCollectionType1Id].stream().collect(toList()))
				.containsExactly(1, 3, 6, 8);

		assertThat(dataStore.versions[zeCollectionIndex][zeCollectionType1Id].stream().collect(toList()))
				.containsExactly(12L, 23L, 34L, 67L);

		assertThat(dataStore.schema[zeCollectionIndex][zeCollectionType1Id].stream().collect(toList()))
				.containsExactly(zeSchemaDefaultId, zeSchemaDefaultId, zeSchemaDefaultId, zeSchemaDefaultId);

		assertThat(dataStore.stream(zeCollectionId, zeCollectionType1Id).collect(Collectors.toList()))
				.containsExactly(dto1, dto3, dto6, dto8);

		dataStore.set(2, dto2, false);

		assertThat(dataStore.ids[zeCollectionIndex][zeCollectionType1Id].stream().collect(toList()))
				.containsExactly(1, 2, 3, 6, 8);

		assertThat(dataStore.versions[zeCollectionIndex][zeCollectionType1Id].stream().collect(toList()))
				.containsExactly(12L, 89L, 23L, 34L, 67L);

		assertThat(dataStore.schema[zeCollectionIndex][zeCollectionType1Id].stream().collect(toList()))
				.containsExactly(zeSchemaDefaultId, zeSchemaDefaultId, zeSchemaDefaultId, zeSchemaDefaultId, zeSchemaDefaultId);

		assertThat(dataStore.stream(zeCollectionId, zeCollectionType1Id).collect(Collectors.toList()))
				.containsExactly(dto1, dto2, dto3, dto6, dto8);

		dataStore.set(7, dto7, false);

		assertThat(dataStore.ids[zeCollectionIndex][zeCollectionType1Id].stream().collect(toList()))
				.containsExactly(1, 2, 3, 6, 7, 8);

		assertThat(dataStore.versions[zeCollectionIndex][zeCollectionType1Id].stream().collect(toList()))
				.containsExactly(12L, 89L, 23L, 34L, 11L, 67L);

		assertThat(dataStore.schema[zeCollectionIndex][zeCollectionType1Id].stream().collect(toList()))
				.containsExactly(zeSchemaDefaultId, zeSchemaDefaultId, zeSchemaDefaultId, zeSchemaDefaultId, zeSchemaDefaultId, zeSchemaDefaultId);

		assertThat(dataStore.stream(zeCollectionId, zeCollectionType1Id).collect(Collectors.toList()))
				.containsExactly(dto1, dto2, dto3, dto6, dto7, dto8);
	}


	@Test
	public void whenAddingASummaryCachedRecordInASpaceThatWasNotReservedThenRecreateArraysAddingNewReservedIndex()
			throws Exception {
		//TODO : Boss de la fin
		defineSchemasManager().using(setup.withABooleanMetadata(whichIsEssentialInSummary));


		getModelLayerFactory().getMetadataSchemasManager().modify(zeCollection, new MetadataSchemaTypesAlteration() {
			@Override
			public void alter(MetadataSchemaTypesBuilder types) {
				types.getSchemaType(zeSchema.typeCode()).setRecordCacheType(RecordCacheType.SUMMARY_CACHED_WITHOUT_VOLATILE);
			}
		});
		initTestVariables();

		ByteArrayRecordDTO dto1, dto2, dto3, dto6, dto8;
		dto1 = create(new SolrRecordDTO(zeroPadded(1), 12L, fields("zeCollection", zeSchema.code()), true));
		dto3 = create(new SolrRecordDTO(zeroPadded(3), 23L, fields("zeCollection", zeSchema.code()), true));
		dto6 = create(new SolrRecordDTO(zeroPadded(6), 34L, fields("zeCollection", zeSchema.code()), true));
		dto8 = create(new SolrRecordDTO(zeroPadded(8), 45L, fields("zeCollection", zeSchema.code()), true));

		dataStore.set(1, dto1, false);
		dataStore.set(3, dto3, false);
		dataStore.set(6, dto6, false);
		dataStore.set(8, dto8, true);

		//There is no space for record 2, which is a normal case
		assertThat(dataStore.ids[zeCollectionIndex][zeCollectionType1Id].stream().collect(toList()))
				.containsExactly(1, 3, 6, 7, 8);

		//No space was reserved for id 2, recreating all arrays with a space for 2
		dto2 = create(new SolrRecordDTO(zeroPadded(2), 56L, fields("zeCollection", zeSchema.code(), "booleanMetadata_s", false), true));
		dataStore.set(2, dto2, false);

		assertThat(dataStore.ids[zeCollectionIndex][zeCollectionType1Id].stream().collect(toList()))
				.containsExactly(1, 2, 3, 6, 7, 8);

		assertThat(dataStore.versions[zeCollectionIndex][zeCollectionType1Id].stream().collect(toList()))
				.containsExactly(12L, 56L, 23L, 34L, 0L, 45L);

		assertThat(dataStore.schema[zeCollectionIndex][zeCollectionType1Id].stream().collect(toList()))
				.containsExactly(zeSchemaDefaultId, zeSchemaDefaultId, zeSchemaDefaultId, zeSchemaDefaultId, (short) 0, zeSchemaDefaultId);

		assertThat(dataStore.stream(zeCollectionId, zeCollectionType1Id).collect(Collectors.toList()))
				.containsExactly(dto1, dto2, dto3, dto6, dto8);

	}

	@Test
	public void whenInvalidatingSchemaTypeThenInvalidateAllRecordsUsingPredicate()
			throws Exception {
		defineSchemasManager().using(setup.withABooleanMetadata(whichIsEssentialInSummary));


		getModelLayerFactory().getMetadataSchemasManager().modify(zeCollection, new MetadataSchemaTypesAlteration() {
			@Override
			public void alter(MetadataSchemaTypesBuilder types) {
				types.getSchemaType(zeSchema.typeCode()).setRecordCacheType(RecordCacheType.SUMMARY_CACHED_WITHOUT_VOLATILE);
			}
		});
		initTestVariables();

		ByteArrayRecordDTO dto1, dto2, dto3, dto6, dto8;
		dto1 = create(new SolrRecordDTO(zeroPadded(1), 12L, fields("zeCollection", zeSchema.code()), true));
		dto2 = create(new SolrRecordDTO(zeroPadded(2), 56L, fields("zeCollection", zeSchema.code()), true));
		dto3 = create(new SolrRecordDTO(zeroPadded(3), 23L, fields("zeCollection", zeSchema.code()), true));
		dto6 = create(new SolrRecordDTO(zeroPadded(6), 34L, fields("zeCollection", zeSchema.code()), true));
		dto8 = create(new SolrRecordDTO(zeroPadded(8), 45L, fields("zeCollection", zeSchema.code()), true));

		dataStore.set(1, dto1, false);
		dataStore.set(2, dto2, false);
		dataStore.set(3, dto3, false);
		dataStore.set(6, dto6, false);
		dataStore.set(8, dto8, true);

		assertThat(dataStore.ids[zeCollectionIndex][zeCollectionType1Id].stream().collect(toList()))
				.containsExactly(1, 2, 3, 6, 7, 8);
		assertThat(dataStore.stream(zeCollectionId, zeCollectionType1Id).collect(Collectors.toList()))
				.containsExactly(dto1, dto2, dto3, dto6, dto8);

		//Invalidating other collection and types : Nothing happens
		dataStore.invalidate(anotherCollectionId, zeCollectionType1Id, (r) -> r.getVersion() % 2 == 0);
		dataStore.invalidate(zeCollectionId, zeCollectionType2Id, (r) -> r.getVersion() % 2 == 0);

		assertThat(dataStore.stream(zeCollectionId, zeCollectionType1Id).collect(Collectors.toList()))
				.containsExactly(dto1, dto2, dto3, dto6, dto8);


		dataStore.invalidate(zeCollectionId, zeCollectionType1Id, (r) -> r.getVersion() % 2 == 0);

		assertThat(dataStore.ids[zeCollectionIndex][zeCollectionType1Id].stream().collect(toList()))
				.containsExactly(1, 2, 3, 6, 7, 8);

		assertThat(dataStore.versions[zeCollectionIndex][zeCollectionType1Id].stream().collect(toList()))
				.containsExactly(0L, 0L, 23L, 0L, 0L, 45L);

		assertThat(dataStore.schema[zeCollectionIndex][zeCollectionType1Id].stream().collect(toList()))
				.containsExactly((short) 0, (short) 0, zeSchemaDefaultId, (short) 0, (short) 0, zeSchemaDefaultId);

		assertThat(dataStore.stream(zeCollectionId, zeCollectionType1Id).collect(Collectors.toList()))
				.containsExactly(dto3, dto8);

	}


	@Test
	public void whenInvalidatingCollectionThenInvalidateAllRecordsUsingPredicate()
			throws Exception {
		defineSchemasManager().using(setup.withABooleanMetadata(whichIsEssentialInSummary));


		getModelLayerFactory().getMetadataSchemasManager().modify(zeCollection, new MetadataSchemaTypesAlteration() {
			@Override
			public void alter(MetadataSchemaTypesBuilder types) {
				types.getSchemaType(zeSchema.typeCode()).setRecordCacheType(RecordCacheType.SUMMARY_CACHED_WITHOUT_VOLATILE);
			}
		});
		initTestVariables();

		ByteArrayRecordDTO dto1, dto2, dto3, dto6, dto8;
		dto1 = create(new SolrRecordDTO(zeroPadded(1), 12L, fields("zeCollection", zeSchema.code()), true));
		dto2 = create(new SolrRecordDTO(zeroPadded(2), 56L, fields("zeCollection", zeSchema.code()), true));
		dto3 = create(new SolrRecordDTO(zeroPadded(3), 23L, fields("zeCollection", zeSchema.code()), true));
		dto6 = create(new SolrRecordDTO(zeroPadded(6), 34L, fields("zeCollection", zeSchema.code()), true));
		dto8 = create(new SolrRecordDTO(zeroPadded(8), 45L, fields("zeCollection", zeSchema.code()), true));

		dataStore.set(1, dto1, false);
		dataStore.set(2, dto2, false);
		dataStore.set(3, dto3, false);
		dataStore.set(6, dto6, false);
		dataStore.set(8, dto8, true);

		assertThat(dataStore.ids[zeCollectionIndex][zeCollectionType1Id].stream().collect(toList()))
				.containsExactly(1, 2, 3, 6, 7, 8);
		assertThat(dataStore.stream(zeCollectionId, zeCollectionType1Id).collect(Collectors.toList()))
				.containsExactly(dto1, dto2, dto3, dto6, dto8);

		//Invalidating other collection and types : Nothing happens
		dataStore.invalidate(anotherCollectionId, (r) -> r.getVersion() % 2 == 0);

		assertThat(dataStore.stream(zeCollectionId, zeCollectionType1Id).collect(Collectors.toList()))
				.containsExactly(dto1, dto2, dto3, dto6, dto8);


		dataStore.invalidate(zeCollectionId, (r) -> r.getVersion() % 2 == 0);

		assertThat(dataStore.ids[zeCollectionIndex][zeCollectionType1Id].stream().collect(toList()))
				.containsExactly(1, 2, 3, 6, 7, 8);

		assertThat(dataStore.versions[zeCollectionIndex][zeCollectionType1Id].stream().collect(toList()))
				.containsExactly(0L, 0L, 23L, 0L, 0L, 45L);

		assertThat(dataStore.schema[zeCollectionIndex][zeCollectionType1Id].stream().collect(toList()))
				.containsExactly((short) 0, (short) 0, zeSchemaDefaultId, (short) 0, (short) 0, zeSchemaDefaultId);

		assertThat(dataStore.stream(zeCollectionId, zeCollectionType1Id).collect(Collectors.toList()))
				.containsExactly(dto3, dto8);

	}


	@Test
	public void whenInvalidatingAllCollectionsThenInvalidateAllRecordsUsingPredicate()
			throws Exception {
		defineSchemasManager().using(setup.withABooleanMetadata(whichIsEssentialInSummary));


		getModelLayerFactory().getMetadataSchemasManager().modify(zeCollection, new MetadataSchemaTypesAlteration() {
			@Override
			public void alter(MetadataSchemaTypesBuilder types) {
				types.getSchemaType(zeSchema.typeCode()).setRecordCacheType(RecordCacheType.SUMMARY_CACHED_WITHOUT_VOLATILE);
			}
		});
		initTestVariables();

		ByteArrayRecordDTO dto1, dto2, dto3, dto6, dto8;
		dto1 = create(new SolrRecordDTO(zeroPadded(1), 12L, fields("zeCollection", zeSchema.code()), true));
		dto2 = create(new SolrRecordDTO(zeroPadded(2), 56L, fields("zeCollection", zeSchema.code()), true));
		dto3 = create(new SolrRecordDTO(zeroPadded(3), 23L, fields("zeCollection", zeSchema.code()), true));
		dto6 = create(new SolrRecordDTO(zeroPadded(6), 34L, fields("zeCollection", zeSchema.code()), true));
		dto8 = create(new SolrRecordDTO(zeroPadded(8), 45L, fields("zeCollection", zeSchema.code()), true));

		dataStore.set(1, dto1, false);
		dataStore.set(2, dto2, false);
		dataStore.set(3, dto3, false);
		dataStore.set(6, dto6, false);
		dataStore.set(8, dto8, true);

		assertThat(dataStore.ids[zeCollectionIndex][zeCollectionType1Id].stream().collect(toList()))
				.containsExactly(1, 2, 3, 6, 7, 8);
		assertThat(dataStore.stream(zeCollectionId, zeCollectionType1Id).collect(Collectors.toList()))
				.containsExactly(dto1, dto2, dto3, dto6, dto8);


		dataStore.invalidate((r) -> r.getVersion() % 2 == 0);

		assertThat(dataStore.ids[zeCollectionIndex][zeCollectionType1Id].stream().collect(toList()))
				.containsExactly(1, 2, 3, 6, 7, 8);

		assertThat(dataStore.versions[zeCollectionIndex][zeCollectionType1Id].stream().collect(toList()))
				.containsExactly(0L, 0L, 23L, 0L, 0L, 45L);

		assertThat(dataStore.schema[zeCollectionIndex][zeCollectionType1Id].stream().collect(toList()))
				.containsExactly((short) 0, (short) 0, zeSchemaDefaultId, (short) 0, (short) 0, zeSchemaDefaultId);

		assertThat(dataStore.stream(zeCollectionId, zeCollectionType1Id).collect(Collectors.toList()))
				.containsExactly(dto3, dto8);

	}


	private ByteArrayRecordDTO create(SolrRecordDTO solrRecordDTO) {
		return ByteArrayRecordDTO.create(getModelLayerFactory(), solrRecordDTO);
	}

	private String zeroPadded(int i) {
		return StringUtils.leftPad("" + i, 11, '0');
	}

	private Map<String, Object> fields(String collection, String schema, Object... extraFields) {
		Map<String, Object> fields = new HashMap<>();
		fields.put("collection_s", collection);
		fields.put("schema_s", schema);


		for (int i = 0; i < extraFields.length; i += 2) {
			fields.put((String) extraFields[i], extraFields[i + 1]);
		}

		return fields;
	}
}
