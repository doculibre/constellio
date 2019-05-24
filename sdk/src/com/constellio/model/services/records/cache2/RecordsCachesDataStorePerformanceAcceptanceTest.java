package com.constellio.model.services.records.cache2;

import com.constellio.data.dao.dto.records.RecordDTO;
import com.constellio.data.dao.dto.records.SolrRecordDTO;
import com.constellio.model.entities.schemas.RecordCacheType;
import com.constellio.model.services.collections.CollectionsListManager;
import com.constellio.model.services.factories.ModelLayerFactory;
import com.constellio.model.services.schemas.MetadataSchemaTypesAlteration;
import com.constellio.model.services.schemas.builders.MetadataSchemaTypeBuilder;
import com.constellio.model.services.schemas.builders.MetadataSchemaTypesBuilder;
import com.constellio.sdk.tests.ConstellioTest;
import org.apache.commons.lang3.StringUtils;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.io.File;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Predicate;

import static com.constellio.model.entities.schemas.MetadataValueType.BOOLEAN;
import static com.constellio.model.services.records.cache2.CacheRecordDTOUtils.toIntKey;
import static org.assertj.core.api.Assertions.assertThat;

public class RecordsCachesDataStorePerformanceAcceptanceTest extends ConstellioTest {

	RecordsCachesDataStore dataStore;

	@Before
	public void setUp() throws Exception {
		SummaryCacheSingletons.dataStore = new FileSystemRecordsValuesCacheDataStore(new File(newTempFolder(), "test.db"));
	}

	@After
	public void tearDown() throws Exception {
		SummaryCacheSingletons.dataStore.close();
	}

	@Test
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
						typesBuilder.createNewSchemaType(type).getDefaultSchema().create("boolean1").setType(BOOLEAN)
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

			SolrRecordDTO dto = new SolrRecordDTO(strId, fields, true);
			ByteArrayRecordDTO byteArrayRecordDTO = ByteArrayRecordDTO.create(modelLayerFactory, dto);
			structureBytes += 8;
			structureBytes += 8; //version
			structureBytes += 2; //byte array length

			structureBytes += 2; //schema id
			structureBytes += 4; //id
			dataBytes += byteArrayRecordDTO.data.length;
			dataStore.insertWithoutReservingSpaceForPreviousIds(byteArrayRecordDTO);

		}

		Thread.sleep(1_000_000_000);

	}


	@Test
	public void given10MRecordsSplittedOn10CollectionsAnd30TypesThenLookupByCollectionsAndTypesVeryFast()
			throws Exception {

		prepareSystem(withZeCollection(), withCollection("collection2"), withCollection("collection3"), withCollection("collection4"));
		String[] collections = new String[]{"zeCollection", "collection2", "collection3", "collection4",};

		String[] types = new String[]{"schemaType01", "schemaType02"};

		for (String collection : collections) {
			getModelLayerFactory().getMetadataSchemasManager().modify(collection, new MetadataSchemaTypesAlteration() {
				@Override
				public void alter(MetadataSchemaTypesBuilder typesBuilder) {
					for (String type : types) {
						MetadataSchemaTypeBuilder typeBuilder = typesBuilder.createNewSchemaType(type);
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

			SolrRecordDTO dto = new SolrRecordDTO(strId, fields, true);
			ByteArrayRecordDTO byteArrayRecordDTO = ByteArrayRecordDTO.create(modelLayerFactory, dto);
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

		double getByIdBySecond = ids.size() * calculateOpsPerSecondsOver(() -> {
			for (int i = 0; i < ids.size(); i++) {
				dataStore.get(ids.get(i));
			}
		}, 5000);
		System.out.println("getById/sec : " + (int) getByIdBySecond);

		double getByIdBySecondSupplyingCollection = ids.size() * calculateOpsPerSecondsOver(() -> {
			for (int i = 0; i < ids.size(); i++) {
				String strId = ids.get(i);
				int intKey = toIntKey(strId);
				byte collectionId = collectionIds[intKey % collections.length];
				dataStore.get(collectionId, strId);
			}
		}, 5000);
		System.out.println("getById/sec when supplying collection : " + (int) getByIdBySecondSupplyingCollection);

		double streamsBySecond = calculateOpsPerSecondsOver(() -> {
			dataStore.stream().filter(onlyTrue).count();
		}, 5000);

		System.out.println("Streams/sec : " + (int) streamsBySecond);

		double streamsBySecondSupplyingCollection = calculateOpsPerSecondsOver(() -> {
			dataStore.stream(collectionIds[3]).filter(onlyTrue).count();
		}, 5000);

		System.out.println("Streams/min when supplying collection : " + (int) streamsBySecondSupplyingCollection);

	}

	private double calculateOpsPerSecondsOver(Runnable op, int msToBench) {
		double loopCount = 0;
		long start = new Date().getTime();
		long end = start;
		while ((start + msToBench > end)) {
			op.run();
			loopCount++;
			end = new Date().getTime();
		}

		double elapsedMs = end - start;
		return loopCount / (1000.0 / elapsedMs);
	}

	@Test
	public void given1KRecordsSplittedOn10CollectionsAnd30TypesThenLookupByCollectionsAndTypesVeryFast()
			throws Exception {

		prepareSystem(withZeCollection(), withCollection("collection2"), withCollection("collection3"), withCollection("collection4"));
		String[] collections = new String[]{"zeCollection", "collection2", "collection3", "collection4",};

		String[] types = new String[]{"schemaType01", "schemaType02"};

		for (String collection : collections) {
			getModelLayerFactory().getMetadataSchemasManager().modify(collection, new MetadataSchemaTypesAlteration() {
				@Override
				public void alter(MetadataSchemaTypesBuilder typesBuilder) {
					for (String type : types) {
						MetadataSchemaTypeBuilder typeBuilder = typesBuilder.createNewSchemaType(type);
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

			SolrRecordDTO dto = new SolrRecordDTO(strId, fields, true);
			ByteArrayRecordDTO byteArrayRecordDTO = ByteArrayRecordDTO.create(modelLayerFactory, dto);
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


		double getByIdBySecond = ids.size() * calculateOpsPerSecondsOver(() -> {
			for (int i = 0; i < ids.size(); i++) {
				dataStore.get(ids.get(i));
			}
		}, 5000);
		System.out.println("getById/sec : " + (int) getByIdBySecond);

		double getByIdBySecondSupplyingCollection = ids.size() * calculateOpsPerSecondsOver(() -> {
			for (int i = 0; i < ids.size(); i++) {
				String strId = ids.get(i);
				int intKey = toIntKey(strId);
				byte collectionId = collectionIds[intKey % collections.length];
				dataStore.get(collectionId, strId);
			}
		}, 5000);
		System.out.println("getById/sec when supplying collection : " + (int) getByIdBySecondSupplyingCollection);

		double streamsBySecond = calculateOpsPerSecondsOver(() -> {
			dataStore.stream().filter(onlyTrue).count();
		}, 5000);

		System.out.println("Streams/sec : " + (int) streamsBySecond);

		double streamsBySecondSupplyingCollection = calculateOpsPerSecondsOver(() -> {
			dataStore.stream(collectionIds[3]).filter(onlyTrue).count();
		}, 5000);

		System.out.println("Streams/sec when supplying collection : " + (int) streamsBySecondSupplyingCollection);


		short typeId = getModelLayerFactory().getMetadataSchemasManager().getSchemaTypes(collectionIds[0])
				.getSchemaType("schemaType01").getId();
		assertThat(dataStore.stream(collectionIds[0], typeId).filter(onlyTrue).count()).isEqualTo(333L);

		double streamsBySecondSupplyingType = calculateOpsPerSecondsOver(() -> {
			dataStore.stream(collectionIds[0], typeId).filter(onlyTrue).count();
		}, 50000);

		System.out.println("Streams/sec when supplying type : " + (int) streamsBySecondSupplyingType);

	}

}
