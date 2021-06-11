package com.constellio.model.services.records.cache;

import com.constellio.data.dao.dto.records.RecordDTO;
import com.constellio.data.dao.dto.records.RecordDTOMode;
import com.constellio.data.dao.dto.records.SolrRecordDTO;
import com.constellio.data.utils.dev.Toggle;
import com.constellio.model.entities.schemas.RecordCacheType;
import com.constellio.model.services.collections.CollectionsListManager;
import com.constellio.model.services.factories.ModelLayerFactory;
import com.constellio.model.services.records.cache.dataStore.RecordsCachesDataStore;
import com.constellio.model.services.records.cache.offHeapCollections.OffHeapMemoryAllocator;
import com.constellio.model.services.schemas.MetadataSchemaTypesAlteration;
import com.constellio.model.services.schemas.builders.MetadataSchemaTypeBuilder;
import com.constellio.model.services.schemas.builders.MetadataSchemaTypesBuilder;
import com.constellio.sdk.tests.ConstellioTest;
import com.constellio.sdk.tests.annotations.PerformanceTest;
import org.apache.commons.lang3.StringUtils;
import org.junit.Test;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Predicate;

import static com.constellio.model.entities.schemas.MetadataValueType.BOOLEAN;
import static com.constellio.model.services.records.RecordUtils.toIntKey;
import static com.constellio.sdk.tests.TestUtils.calculateOpsPerSecondsOver;
import static org.assertj.core.api.Assertions.assertThat;

@PerformanceTest
public class RecordsCachesDataStorePerformanceAcceptanceTest extends ConstellioTest {

	RecordsCachesDataStore dataStore;

	//@Test
	public void given150MRecordsSplittedOn10CollectionsAnd30TypesThenLookupByCollectionsAndTypesVeryFast()
			throws Exception {

		prepareSystem(withZeCollection(), withCollection("collection1"), withCollection("collection2"), withCollection("collection3"), withCollection("collection4")
				, withCollection("collection5"), withCollection("collection6"), withCollection("collection7"), withCollection("collection8"), withCollection("collection9"));
		String[] collections = new String[]{"zeCollection", "collection1", "collection2", "collection3", "collection4",
											"collection5", "collection6", "collection7", "collection8", "collection9"};

		//		prepareSystem(withZeCollection());
		//		String[] collections = new String[]{"zeCollection"};

		String[]
				types = new String[]{"schemaType01", "schemaType02"};

		for (String collection : collections) {
			getModelLayerFactory().getMetadataSchemasManager().modify(collection, new MetadataSchemaTypesAlteration() {
				@Override
				public void alter(MetadataSchemaTypesBuilder typesBuilder) {
					for (String type : types) {
						typesBuilder.createNewSchemaTypeWithSecurity(type).getDefaultSchema().create("boolean1").setType(BOOLEAN)
								.setEssentialInSummary(true);
					}
				}
			});
		}


		ModelLayerFactory modelLayerFactory = getModelLayerFactory();
		dataStore = new RecordsCachesDataStore(modelLayerFactory);
		int id = 1;

		long dataBytes = 0;

		long structureBytes = 0;

		Map<String, Object> fields = new HashMap<>();
		List<String> ids = null;

		for (int createdRecord = 0; createdRecord < 300_000_000; createdRecord++) {

			id += 1;
			if (createdRecord % 1000000 == 0) {
				long megaBytesOfData = dataBytes / 1024 / 1024;
				long megaBytesOfStructure = structureBytes / 1024 / 1024;
				long allocatedOffHeapMemoryForCache = OffHeapMemoryAllocator.getAllocatedMemory() / 1024 / 1024;
				System.out.println("Records created : " + createdRecord + " Totalling estimated" + megaBytesOfData + "Mb of data and " + megaBytesOfStructure + "Mb of structure. Off-heap allocated memory : " + allocatedOffHeapMemoryForCache + "Mb");
			}
			String strId = StringUtils.leftPad("" + id, 11, '0');
			fields.put("collection_s", collections[id % collections.length]);
			fields.put("schema_s", types[id % types.length] + "_default");
			fields.put("boolean1_s", createdRecord % 3 == 0);

			SolrRecordDTO dto = new SolrRecordDTO(strId, fields, RecordDTOMode.SUMMARY);
			ByteArrayRecordDTO byteArrayRecordDTO = ByteArrayRecordDTOUtilsAcceptanceTest.create(modelLayerFactory, dto);
			structureBytes += 8;
			structureBytes += 8; //version
			structureBytes += 2; //byte array length

			structureBytes += 2; //schema id
			structureBytes += 4; //id
			dataBytes += byteArrayRecordDTO.data.length;
			dataStore.insertWithoutReservingSpaceForPreviousIds(byteArrayRecordDTO);

		}

		//runBenchmark(ids, collections, "schemaType01");
	}


	@Test
	public void given4MRecordsSplittedOn10CollectionsAnd30TypesThenLookupByCollectionsAndTypesVeryFast()
			throws Exception {

		Toggle.USE_MMAP_WITHMAP_DB_FOR_LOADING.enable();
		Toggle.USE_MMAP_WITHMAP_DB_FOR_RUNTIME.enable();
		prepareSystem(withZeCollection(), withCollection("collection2"), withCollection("collection3"), withCollection("collection4"));
		String[] collections = new String[]{"zeCollection", "collection2", "collection3", "collection4",};

		String[] types = new String[]{"schemaType01", "schemaType02"};

		for (String collection : collections) {
			getModelLayerFactory().getMetadataSchemasManager().modify(collection, new MetadataSchemaTypesAlteration() {
				@Override
				public void alter(MetadataSchemaTypesBuilder typesBuilder) {
					for (String type : types) {
						MetadataSchemaTypeBuilder typeBuilder = typesBuilder.createNewSchemaTypeWithSecurity(type);
						typeBuilder.setRecordCacheType(RecordCacheType.SUMMARY_CACHED_WITHOUT_VOLATILE);
						typeBuilder.getDefaultSchema().create("boolean1").setType(BOOLEAN)
								.setEssentialInSummary(true);
					}
				}
			});
		}


		ModelLayerFactory modelLayerFactory = getModelLayerFactory();
		dataStore = new RecordsCachesDataStore(modelLayerFactory);
		int id = 1;

		long dataBytes = 0;
		long structureBytes = 0;

		Map<String, Object> fields = new HashMap<>();

		List<String> ids = new ArrayList<>();

		for (int createdRecord = 0; createdRecord < 4_000_000; createdRecord++) {

			id += 1;
			if (createdRecord % 1000000 == 0) {
				long megaBytesOfData = dataBytes / 1024 / 1024;
				long megaBytesOfStructure = structureBytes / 1024 / 1024;
				long allocatedOffHeapMemoryForCache = OffHeapMemoryAllocator.getAllocatedMemory() / 1024 / 1024;
				System.out.println("Records created : " + createdRecord + " Totalling estimated" + megaBytesOfData + "Mb of data and " + megaBytesOfStructure + "Mb of structure. Off-heap allocated memory : " + allocatedOffHeapMemoryForCache + "Mb");
			}
			String strId = StringUtils.leftPad("" + id, 11, '0');
			fields.put("collection_s", collections[id % collections.length]);
			fields.put("schema_s", types[id % types.length] + "_default");
			fields.put("boolean1_s", createdRecord % 3 == 0);

			SolrRecordDTO dto = new SolrRecordDTO(strId, fields, RecordDTOMode.SUMMARY);
			ByteArrayRecordDTO byteArrayRecordDTO = ByteArrayRecordDTOUtilsAcceptanceTest.create(modelLayerFactory, dto);
			structureBytes += 8;
			structureBytes += 8; //version
			structureBytes += 2; //byte array length

			structureBytes += 2; //schema id
			structureBytes += 4; //id
			dataBytes += byteArrayRecordDTO.data.length;
			dataStore.insertWithoutReservingSpaceForPreviousIds(byteArrayRecordDTO);
			ids.add(strId);

		}

		CollectionsListManager collectionsListManager = getModelLayerFactory().getCollectionsListManager();
		byte[] collectionIds = new byte[]{
				collectionsListManager.getCollectionId(collections[0]),
				collectionsListManager.getCollectionId(collections[1]),
				collectionsListManager.getCollectionId(collections[2]),
				collectionsListManager.getCollectionId(collections[3])
		};


		assertThat(dataStore.stream().count()).isEqualTo(4_000_000);
		assertThat(dataStore.stream(collectionIds[0]).count()).isEqualTo(1_000_000);
		assertThat(dataStore.stream(collectionIds[1]).count()).isEqualTo(1_000_000);
		assertThat(dataStore.stream(collectionIds[2]).count()).isEqualTo(1_000_000);
		assertThat(dataStore.stream(collectionIds[3]).count()).isEqualTo(1_000_000);

		Predicate<RecordDTO> onlyTrue = (r) -> Boolean.TRUE.equals(r.getFields().get("boolean1_s"));
		Predicate<RecordDTO> onlyFalse = (r) -> Boolean.FALSE.equals(r.getFields().get("boolean1_s"));

		assertThat(dataStore.stream(collectionIds[0]).filter(onlyTrue).count()).isEqualTo(333_333);
		assertThat(dataStore.stream(collectionIds[0]).filter(onlyFalse).count()).isEqualTo(666_667);
		assertThat(dataStore.stream(collectionIds[1]).filter(onlyTrue).count()).isEqualTo(333334L);
		assertThat(dataStore.stream(collectionIds[1]).filter(onlyFalse).count()).isEqualTo(666_666);
		assertThat(dataStore.stream(collectionIds[2]).filter(onlyTrue).count()).isEqualTo(333_334);
		assertThat(dataStore.stream(collectionIds[2]).filter(onlyFalse).count()).isEqualTo(666_666);
		assertThat(dataStore.stream(collectionIds[3]).filter(onlyTrue).count()).isEqualTo(333_333);
		assertThat(dataStore.stream(collectionIds[3]).filter(onlyFalse).count()).isEqualTo(666_667);

		runBenchmark(ids, collections, "schemaType01");

	}

	@Test
	public void given4KRecordsSplittedOn10CollectionsAnd30TypesThenLookupByCollectionsAndTypesVeryFast()
			throws Exception {

		prepareSystem(withZeCollection(), withCollection("collection2"), withCollection("collection3"), withCollection("collection4"));
		String[] collections = new String[]{"zeCollection", "collection2", "collection3", "collection4",};

		String[] types = new String[]{"schemaType01", "schemaType02"};

		for (String collection : collections) {
			getModelLayerFactory().getMetadataSchemasManager().modify(collection, new MetadataSchemaTypesAlteration() {
				@Override
				public void alter(MetadataSchemaTypesBuilder typesBuilder) {
					for (String type : types) {
						MetadataSchemaTypeBuilder typeBuilder = typesBuilder.createNewSchemaTypeWithSecurity(type);
						typeBuilder.setRecordCacheType(RecordCacheType.SUMMARY_CACHED_WITHOUT_VOLATILE);
						typeBuilder.getDefaultSchema().create("boolean1").setType(BOOLEAN)
								.setEssentialInSummary(true);
					}
				}
			});
		}


		ModelLayerFactory modelLayerFactory = getModelLayerFactory();
		dataStore = new RecordsCachesDataStore(modelLayerFactory);
		int id = 1;

		long dataBytes = 0;
		long structureBytes = 0;

		Map<String, Object> fields = new HashMap<>();

		List<String> ids = new ArrayList<>();

		for (int createdRecord = 0; createdRecord < 4_000; createdRecord++) {

			id += 1;
			String strId = StringUtils.leftPad("" + id, 11, '0');
			fields.put("collection_s", collections[id % collections.length]);
			fields.put("schema_s", types[id % types.length] + "_default");
			fields.put("boolean1_s", createdRecord % 3 == 0);

			SolrRecordDTO dto = new SolrRecordDTO(strId, fields, RecordDTOMode.SUMMARY);
			ByteArrayRecordDTO byteArrayRecordDTO = ByteArrayRecordDTOUtilsAcceptanceTest.create(modelLayerFactory, dto);
			structureBytes += 8;
			structureBytes += 8; //version
			structureBytes += 2; //byte array length

			structureBytes += 2; //schema id
			structureBytes += 4; //id
			dataBytes += byteArrayRecordDTO.data.length;
			dataStore.insertWithoutReservingSpaceForPreviousIds(byteArrayRecordDTO);
			ids.add(strId);

		}

		CollectionsListManager collectionsListManager = getModelLayerFactory().getCollectionsListManager();
		byte[] collectionIds = new byte[]{
				collectionsListManager.getCollectionId(collections[0]),
				collectionsListManager.getCollectionId(collections[1]),
				collectionsListManager.getCollectionId(collections[2]),
				collectionsListManager.getCollectionId(collections[3])
		};


		assertThat(dataStore.stream().count()).isEqualTo(4_000);
		assertThat(dataStore.stream(collectionIds[0]).count()).isEqualTo(1_000);
		assertThat(dataStore.stream(collectionIds[1]).count()).isEqualTo(1_000);
		assertThat(dataStore.stream(collectionIds[2]).count()).isEqualTo(1_000);
		assertThat(dataStore.stream(collectionIds[3]).count()).isEqualTo(1_000);

		Predicate<RecordDTO> onlyTrue = (r) -> Boolean.TRUE.equals(r.getFields().get("boolean1_s"));
		Predicate<RecordDTO> onlyFalse = (r) -> Boolean.FALSE.equals(r.getFields().get("boolean1_s"));

		assertThat(dataStore.stream(collectionIds[0]).filter(onlyTrue).count()).isEqualTo(333);
		assertThat(dataStore.stream(collectionIds[0]).filter(onlyFalse).count()).isEqualTo(667);
		assertThat(dataStore.stream(collectionIds[1]).filter(onlyTrue).count()).isEqualTo(334L);
		assertThat(dataStore.stream(collectionIds[1]).filter(onlyFalse).count()).isEqualTo(666);
		assertThat(dataStore.stream(collectionIds[2]).filter(onlyTrue).count()).isEqualTo(334);
		assertThat(dataStore.stream(collectionIds[2]).filter(onlyFalse).count()).isEqualTo(666);
		assertThat(dataStore.stream(collectionIds[3]).filter(onlyTrue).count()).isEqualTo(333);
		assertThat(dataStore.stream(collectionIds[3]).filter(onlyFalse).count()).isEqualTo(667);


		runBenchmark(ids, collections, "schemaType01");


	}


	private void runBenchmark(List<String> ids, String[] collections, String schemaTypeCode) {

		Predicate<RecordDTO> onlyTrue = (r) -> Boolean.TRUE.equals(r.getFields().get("boolean1_s"));

		byte[] collectionIds = new byte[collections.length];

		System.out.println("\nRunning benchmarks... (30sec/benchmark)\n");

		CollectionsListManager collectionsListManager = getModelLayerFactory().getCollectionsListManager();
		for (int i = 0; i < collections.length; i++) {
			collectionIds[i] = collectionsListManager.getCollectionId(collections[i]);
		}

		double getByIdBySecond = ids.size() * calculateOpsPerSecondsOver(() -> {
			for (int i = 0; i < ids.size(); i++) {
				dataStore.get(ids.get(i));
			}
		}, 20000);
		System.out.println("getById/sec : " + (int) getByIdBySecond);

		double getByIdBySecondSupplyingCollection = ids.size() * calculateOpsPerSecondsOver(() -> {
			for (int i = 0; i < ids.size(); i++) {
				String strId = ids.get(i);
				int intKey = toIntKey(strId);
				byte collectionId = collectionIds[intKey % collections.length];
				dataStore.get(collectionId, strId);
			}
		}, 20000);
		System.out.println("getById/sec when supplying collection : " + (int) getByIdBySecondSupplyingCollection);

		double streamsBySecond = calculateOpsPerSecondsOver(() -> {
			dataStore.stream().filter(onlyTrue).count();
		}, 20000);

		System.out.println("Streams/sec : " + (int) streamsBySecond);

		double streamsBySecondWithoutAutoClose = calculateOpsPerSecondsOver(() -> {
			dataStore.stream(false).filter(onlyTrue).count();
		}, 20000);

		System.out.println("Streams/sec without autoclose : " + (int) streamsBySecondWithoutAutoClose);

		double streamsBySecondSupplyingCollection = calculateOpsPerSecondsOver(() -> {
			dataStore.stream(collectionIds[collectionIds.length - 1]).filter(onlyTrue).count();
		}, 20000);

		System.out.println("Streams/sec when supplying collection : " + (int) streamsBySecondSupplyingCollection);


		short typeId = getModelLayerFactory().getMetadataSchemasManager().getSchemaTypes(collectionIds[0])
				.getSchemaType(schemaTypeCode).getId();

		double streamsBySecondSupplyingType = calculateOpsPerSecondsOver(() -> {
			dataStore.stream(collectionIds[0], typeId).filter(onlyTrue).count();
		}, 20000);

		System.out.println("Streams/sec when supplying type : " + (int) streamsBySecondSupplyingType);
	}


}
