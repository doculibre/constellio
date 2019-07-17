package com.constellio.model.services.cache;

import com.constellio.model.entities.CollectionInfo;
import com.constellio.model.entities.records.Record;
import com.constellio.model.entities.schemas.Metadata;
import com.constellio.model.entities.schemas.MetadataSchema;
import com.constellio.model.entities.schemas.MetadataSchemaType;
import com.constellio.model.entities.schemas.MetadataValueType;
import com.constellio.model.services.caches.CacheIndexService;
import com.constellio.model.services.records.RecordServices;
import com.constellio.model.services.schemas.MetadataSchemaTypesAlteration;
import com.constellio.model.services.schemas.MetadataSchemasManager;
import com.constellio.model.services.schemas.builders.MetadataSchemaBuilder;
import com.constellio.model.services.schemas.builders.MetadataSchemaTypeBuilder;
import com.constellio.sdk.tests.ConstellioTest;
import org.junit.Before;
import org.junit.Test;

import java.util.Arrays;

import static org.assertj.core.api.Assertions.assertThat;

public class CacheIndexServiceAcceptanceTest extends ConstellioTest {

	Metadata cacheIndex;
	Metadata notCacheIndex;
	Metadata unique;
	Metadata cacheIndexMultiValue;

	MetadataSchema testsSchemaDefault;

	MetadataSchemasManager metadataSchemasManager;
	RecordServices recordServices;

	Record record1;
	Record record2;
	Record record3;
	Record record4;
	Record record1Update;

	@Before
	public void setup() throws Exception {
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

		testsSchemaDefault = metadataSchemasManager.getSchemaTypes(zeCollection).getSchemaType("testschema").getDefaultSchema();

		recordServices = getModelLayerFactory().newRecordServices();

		cacheIndex = testsSchemaDefault.getMetadata("cacheIndex");
		notCacheIndex = testsSchemaDefault.getMetadata("notCacheIndex");
		unique = testsSchemaDefault.getMetadata("unique");
		cacheIndexMultiValue = testsSchemaDefault.getMetadata("cacheIndexMultiValue");

		record1 = recordServices.newRecordWithSchema(testsSchemaDefault);
		record1.set(cacheIndex, "toBeFound1");
		record1.set(notCacheIndex, "notCached");
		recordServices.add(record1);

		record2 = recordServices.newRecordWithSchema(testsSchemaDefault);
		record2.set(cacheIndex, "toBeFound2");
		record2.set(notCacheIndex, "notCached2");
		recordServices.add(record2);

		record3 = recordServices.newRecordWithSchema(testsSchemaDefault);
		record3.set(cacheIndex, "toBeFound3");
		record3.set(notCacheIndex, "notCached3");
		recordServices.add(record3);

		record4 = recordServices.newRecordWithSchema(testsSchemaDefault);
		record4.set(cacheIndex, "toBeFound3");
		record4.set(notCacheIndex, "notCached3");
		recordServices.add(record4);

		record1Update = recordServices.newRecordWithSchema(testsSchemaDefault, record1.getId());
		record1Update.set(cacheIndex, "updatedValue");
		record1Update.set(notCacheIndex, "updatedValue");

	}


	@Test
	public void addRecordsToCacheIndexServiceThenValidateWhatIsInCache() {
		CacheIndexService cacheIndexService = new CacheIndexService();

		addRecordsToData(cacheIndexService);

		areAllZeCollectionInitialTestRecordThere(cacheIndexService);

		assertThat(cacheIndexService.countByIterating()).isEqualTo(4);
	}

	private void setInitialRecordsWithUniqueValues() {
		record1 = recordServices.newRecordWithSchema(testsSchemaDefault);
		record1.set(cacheIndex, null);
		record1.set(unique, "unique1");

		record2 = recordServices.newRecordWithSchema(testsSchemaDefault);
		record2.set(cacheIndex, null);
		record2.set(unique, "unique2");

		record3 = recordServices.newRecordWithSchema(testsSchemaDefault);
		record3.set(notCacheIndex, null);
		record3.set(unique, "unique3");

		record4 = recordServices.newRecordWithSchema(testsSchemaDefault);
		record4.set(notCacheIndex, null);
		record4.set(unique, "unique4");
	}

	@Test
	public void addRecordsWithUniqueValueToCacheIndexServiceThenValidateWhatIsInCache() {
		CacheIndexService cacheIndexService = new CacheIndexService();

		setInitialRecordsWithUniqueValues();

		addRecordsToData(cacheIndexService);

		assertThat(cacheIndexService.search(unique, "unique1")).containsAll(Arrays.asList(record1.getId()));
		assertThat(cacheIndexService.search(unique, "unique2")).containsAll(Arrays.asList(record2.getId()));
		assertThat(cacheIndexService.search(unique, "unique3")).containsAll(Arrays.asList(record3.getId()));
		assertThat(cacheIndexService.search(unique, "unique4")).containsAll(Arrays.asList(record4.getId()));

		assertThat(cacheIndexService.countByIterating()).isEqualTo(4);
	}

	private void setInitialRecordsWithMultiValueValues() {
		record1 = recordServices.newRecordWithSchema(testsSchemaDefault);
		record1.set(cacheIndex, null);
		record1.set(cacheIndexMultiValue, Arrays.asList("multi1", "multi2"));

		record2 = recordServices.newRecordWithSchema(testsSchemaDefault);
		record2.set(cacheIndex, null);
		record2.set(cacheIndexMultiValue, Arrays.asList("multi3"));

		record3 = recordServices.newRecordWithSchema(testsSchemaDefault);
		record3.set(notCacheIndex, null);
		record3.set(cacheIndexMultiValue, Arrays.asList("multi3", "multi4", "multi5"));

		record4 = recordServices.newRecordWithSchema(testsSchemaDefault);
		record4.set(notCacheIndex, null);
		record4.set(cacheIndexMultiValue, Arrays.asList("multi5", "multi6"));
	}

	@Test
	public void addRecordsWithMultiValueToCacheIndexServiceThenValidateWhatIsInCache() {
		CacheIndexService cacheIndexService = new CacheIndexService();

		setInitialRecordsWithMultiValueValues();

		addRecordsToData(cacheIndexService);

		assertThat(cacheIndexService.search(cacheIndexMultiValue, "multi1")).containsAll(Arrays.asList(record1.getId()));
		assertThat(cacheIndexService.search(cacheIndexMultiValue, "multi2")).containsAll(Arrays.asList(record1.getId()));
		assertThat(cacheIndexService.search(cacheIndexMultiValue, "multi3")).containsAll(Arrays.asList(record2.getId(), record3.getId()));
		assertThat(cacheIndexService.search(cacheIndexMultiValue, "multi5")).containsAll(Arrays.asList(record3.getId(), record4.getId()));

		assertThat(cacheIndexService.countByIterating()).isEqualTo(8);
	}

	@Test
	public void updateRecordsWinMultiValueMetadataDataToCacheIndexServiceThenValidateWhatIsInCache() {
		CacheIndexService cacheIndexService = new CacheIndexService();

		setInitialRecordsWithMultiValueValues();

		addRecordsToData(cacheIndexService);

		assertThat(cacheIndexService.search(cacheIndexMultiValue, "multi1")).containsAll(Arrays.asList(record1.getId()));
		assertThat(cacheIndexService.search(cacheIndexMultiValue, "multi2")).containsAll(Arrays.asList(record1.getId()));

		assertThat(cacheIndexService.search(cacheIndexMultiValue, "multi3")).contains(record3.getId());
		assertThat(cacheIndexService.search(cacheIndexMultiValue, "multi4")).contains(record3.getId());
		assertThat(cacheIndexService.search(cacheIndexMultiValue, "multi5")).contains(record3.getId());

		assertThat(cacheIndexService.countByIterating()).isEqualTo(8);

		record1Update = recordServices.newRecordWithSchema(testsSchemaDefault, record1.getId());
		record1Update.set(cacheIndexMultiValue, Arrays.asList("value2Multi1", "value2Multi2"));

		Record record3Update = recordServices.newRecordWithSchema(testsSchemaDefault, record3.getId());
		record3Update.set(cacheIndexMultiValue, Arrays.asList("multi5"));

		cacheIndexService.addUpdate(record1, record1Update, testsSchemaDefault);
		cacheIndexService.addUpdate(record3, record3Update, testsSchemaDefault);

		assertThat(cacheIndexService.search(cacheIndexMultiValue, "multi1")).isNull();
		assertThat(cacheIndexService.search(cacheIndexMultiValue, "multi2")).isNull();
		assertThat(cacheIndexService.search(cacheIndexMultiValue, "multi3")).doesNotContain(record3.getId());
		assertThat(cacheIndexService.search(cacheIndexMultiValue, "multi4")).isNull();
		assertThat(cacheIndexService.search(cacheIndexMultiValue, "multi5")).containsAll(Arrays.asList(record3.getId(), record4.getId()));

		assertThat(cacheIndexService.search(cacheIndexMultiValue, "value2Multi1")).containsAll(Arrays.asList(record1.getId()));
		assertThat(cacheIndexService.search(cacheIndexMultiValue, "value2Multi2")).containsAll(Arrays.asList(record1.getId()));

		assertThat(cacheIndexService.countByIterating()).isEqualTo(6);
	}

	private void areAllZeCollectionInitialTestRecordThere(CacheIndexService cacheIndexService) {
		assertThat(cacheIndexService.search(cacheIndex, "toBeFound1")).containsAll(Arrays.asList(record1.getId()));
		assertThat(cacheIndexService.search(cacheIndex, "toBeFound2")).containsAll(Arrays.asList(record2.getId()));
		assertThat(cacheIndexService.search(cacheIndex, "toBeFound3")).containsAll(Arrays.asList(record3.getId(), record4.getId()));
	}

	private void addRecordsToData(CacheIndexService cacheIndexService) {
		cacheIndexService.addUpdate(null, record1, testsSchemaDefault);
		cacheIndexService.addUpdate(null, record2, testsSchemaDefault);
		cacheIndexService.addUpdate(null, record3, testsSchemaDefault);
		cacheIndexService.addUpdate(null, record4, testsSchemaDefault);
	}

	@Test
	public void noEmptyMapAfterRemovalThenOk() {
		CacheIndexService cacheIndexService = new CacheIndexService();

		addRecordsToData(cacheIndexService);

		cacheIndexService.addUpdate(record1, null, testsSchemaDefault);
		cacheIndexService.addUpdate(record2, null, testsSchemaDefault);
		cacheIndexService.addUpdate(record3, null, testsSchemaDefault);
		cacheIndexService.addUpdate(record4, null, testsSchemaDefault);

		assertThat(cacheIndexService.numberOfEmptyMap()).isEqualTo(0);
	}

	@Test
	public void noEmptyMapAfterRemovalOfAllDataOfOneSchemaTypeAndOtherShcemaTypeDataStillThereThenOk() {
		metadataSchemasManager.modify(zeCollection, (MetadataSchemaTypesAlteration) types -> {
			MetadataSchemaTypeBuilder testSchemaBuilder = types.createNewSchemaType("testSecondSchema");


			MetadataSchemaBuilder defaultTestSchemaBuilder = testSchemaBuilder.getDefaultSchema();

			defaultTestSchemaBuilder.create("cacheIndexSecond").setType(MetadataValueType.STRING).setCacheIndex(true);
		});


		MetadataSchemaType secondTestSchemaType = metadataSchemasManager.getSchemaTypes(zeCollection).getSchemaType("testSecondSchema");
		MetadataSchema testsSchemaDefaultSecond = secondTestSchemaType.getDefaultSchema();


		Metadata cacheIndexSecond = testsSchemaDefaultSecond.getMetadata("cacheIndexSecond");

		Record recordSecondSchemaType1 = recordServices.newRecordWithSchema(testsSchemaDefaultSecond);
		recordSecondSchemaType1.set(cacheIndexSecond, "lalala1");

		Record recordSecondSchemaType2 = recordServices.newRecordWithSchema(testsSchemaDefaultSecond);
		recordSecondSchemaType2.set(cacheIndexSecond, "lalala2");

		CacheIndexService cacheIndexService = new CacheIndexService();

		addRecordsToData(cacheIndexService);

		cacheIndexService.addUpdate(null, recordSecondSchemaType1, testsSchemaDefaultSecond);
		cacheIndexService.addUpdate(null, recordSecondSchemaType2, testsSchemaDefaultSecond);

		assertThat(cacheIndexService.countByIterating()).isEqualTo(6);

		cacheIndexService.addUpdate(record1, null, testsSchemaDefault);
		cacheIndexService.addUpdate(record2, null, testsSchemaDefault);
		cacheIndexService.addUpdate(record3, null, testsSchemaDefault);
		cacheIndexService.addUpdate(record4, null, testsSchemaDefault);

		assertThat(cacheIndexService.numberOfEmptyMap()).isEqualTo(0);
		assertThat(cacheIndexService.countByIterating()).isEqualTo(2);
	}

	@Test
	public void updateMetadataFromCacheToCacheIndexServiceThenValidateWhatIsInCache() {
		CacheIndexService cacheIndexService = new CacheIndexService();

		addRecordsToData(cacheIndexService);

		cacheIndexService.addUpdate(record1, record1Update, testsSchemaDefault);

		assertThat(cacheIndexService.search(cacheIndex, "toBeFound1")).isNull();
		assertThat(cacheIndexService.search(cacheIndex, "updatedValue")).containsAll(Arrays.asList(record1.getId()));

		assertThat(cacheIndexService.countByIterating()).isEqualTo(4);
	}

	@Test
	public void removeMetadataFromCacheToCacheIndexServiceThenValidateWhatIsInCache() {
		CacheIndexService cacheIndexService = new CacheIndexService();

		addRecordsToData(cacheIndexService);

		assertThat(cacheIndexService.countByIterating()).isEqualTo(4);

		assertThat(cacheIndexService.search(cacheIndex, "toBeFound1")).containsAll(Arrays.asList(record1.getId()));

		cacheIndexService.addUpdate(record1, null, testsSchemaDefault);

		assertThat(cacheIndexService.countByIterating()).isEqualTo(3);

		assertThat(cacheIndexService.search(cacheIndex, "toBeFound1")).isNull();

		assertThat(cacheIndexService.search(cacheIndex, "toBeFound3")).containsAll(Arrays.asList(record3.getId(), record4.getId()));

		cacheIndexService.addUpdate(record3, null, testsSchemaDefault);

		assertThat(cacheIndexService.countByIterating()).isEqualTo(2);

		assertThat(cacheIndexService.search(cacheIndex, "toBeFound3")).containsAll(Arrays.asList(record4.getId()));
	}

	@Test
	public void givenRecordsIn2CollectionsThenClearOneCollection() throws Exception {

		metadataSchemasManager.modify("secondCollection", (MetadataSchemaTypesAlteration) types -> {
			MetadataSchemaTypeBuilder testSchemaBuilder = types.createNewSchemaType("testschema");

			MetadataSchemaBuilder defaultTestSchemaBuilder = testSchemaBuilder.getDefaultSchema();

			defaultTestSchemaBuilder.create("cacheIndex").setType(MetadataValueType.STRING).setCacheIndex(true);
		});


		MetadataSchema testsSchemaDefaultSecondCollection = metadataSchemasManager.getSchemaTypes("secondCollection").getSchemaType("testschema").getDefaultSchema();
		Metadata cacheIndexSecondCollection = testsSchemaDefaultSecondCollection.getMetadata("cacheIndex");

		Record recordSecondCollection1 = recordServices.newRecordWithSchema(testsSchemaDefaultSecondCollection);
		recordSecondCollection1.set(cacheIndexSecondCollection, "toBeFound1");


		Record recordSecondCollection2 = recordServices.newRecordWithSchema(testsSchemaDefaultSecondCollection);
		recordSecondCollection2.set(cacheIndex, "toBeFound2");


		CacheIndexService cacheIndexService = new CacheIndexService();

		addRecordsToData(cacheIndexService);

		cacheIndexService.addUpdate(null, recordSecondCollection1, testsSchemaDefaultSecondCollection);
		cacheIndexService.addUpdate(null, recordSecondCollection2, testsSchemaDefaultSecondCollection);

		assertThat(cacheIndexService.countByIterating()).isEqualTo(6);

		// Test before clear state
		areAllZeCollectionInitialTestRecordThere(cacheIndexService);
		assertThat(cacheIndexService.search(cacheIndexSecondCollection, "toBeFound1")).containsAll(Arrays.asList(recordSecondCollection1.getId()));
		assertThat(cacheIndexService.search(cacheIndexSecondCollection, "toBeFound2")).containsAll(Arrays.asList(recordSecondCollection2.getId()));

		CollectionInfo secondCollectionInfo = getModelLayerFactory().getCollectionsListManager().getCollectionInfo("secondCollection");

		cacheIndexService.clear(secondCollectionInfo);

		// Test after clear
		areAllZeCollectionInitialTestRecordThere(cacheIndexService);
		assertThat(cacheIndexService.search(cacheIndexSecondCollection, "toBeFound1")).isNull();
		assertThat(cacheIndexService.search(cacheIndexSecondCollection, "toBeFound2")).isNull();

		assertThat(cacheIndexService.countByIterating()).isEqualTo(4);
	}


	@Test
	public void givenRecordsInTwoSchemaTypeThenClearOneShemaType() throws Exception {
		metadataSchemasManager.modify(zeCollection, (MetadataSchemaTypesAlteration) types -> {
			MetadataSchemaTypeBuilder testSchemaBuilder = types.createNewSchemaType("testSecondSchema");


			MetadataSchemaBuilder defaultTestSchemaBuilder = testSchemaBuilder.getDefaultSchema();

			defaultTestSchemaBuilder.create("cacheIndexSecond").setType(MetadataValueType.STRING).setCacheIndex(true);
		});


		CacheIndexService cacheIndexService = new CacheIndexService();

		addRecordsToData(cacheIndexService);

		assertThat(cacheIndexService.countByIterating()).isEqualTo(4);

		MetadataSchemaType secondTestSchemaType = metadataSchemasManager.getSchemaTypes(zeCollection).getSchemaType("testSecondSchema");
		MetadataSchema testsSchemaDefaultSecond = secondTestSchemaType.getDefaultSchema();


		Metadata cacheIndexSecond = testsSchemaDefaultSecond.getMetadata("cacheIndexSecond");

		Record recordSecondSchemaType1 = recordServices.newRecordWithSchema(testsSchemaDefaultSecond);
		recordSecondSchemaType1.set(cacheIndexSecond, "lalala1");

		Record recordSecondSchemaType2 = recordServices.newRecordWithSchema(testsSchemaDefaultSecond);
		recordSecondSchemaType2.set(cacheIndexSecond, "lalala2");

		cacheIndexService.addUpdate(null, recordSecondSchemaType1, testsSchemaDefaultSecond);
		cacheIndexService.addUpdate(null, recordSecondSchemaType2, testsSchemaDefaultSecond);

		assertThat(cacheIndexService.countByIterating()).isEqualTo(6);

		// Test before remove state.
		areAllZeCollectionInitialTestRecordThere(cacheIndexService);

		assertThat(cacheIndexService.search(cacheIndexSecond, "lalala1")).containsAll(Arrays.asList(recordSecondSchemaType1.getId()));
		assertThat(cacheIndexService.search(cacheIndexSecond, "lalala2")).containsAll(Arrays.asList(recordSecondSchemaType2.getId()));


		cacheIndexService.clear(secondTestSchemaType);

		areAllZeCollectionInitialTestRecordThere(cacheIndexService);
		assertThat(cacheIndexService.search(cacheIndexSecond, "lalala1")).isNull();
		assertThat(cacheIndexService.search(cacheIndexSecond, "lalala2")).isNull();

		assertThat(cacheIndexService.countByIterating()).isEqualTo(4);
	}

	@Test(expected = IllegalArgumentException.class)
	public void searchWithNonCacheIndexMetadataThenExceptionThrown() {
		CacheIndexService cacheIndexService = new CacheIndexService();

		addRecordsToData(cacheIndexService);

		cacheIndexService.search(notCacheIndex, "toBeFound1");
	}

	@Test
	public void givenNullValueOnSearchThenReturnNull() {
		CacheIndexService cacheIndexService = new CacheIndexService();

		assertThat(cacheIndexService.search(cacheIndex, null)).isNull();
	}

	@Test(expected = IllegalArgumentException.class)
	public void givenNullRecordsThenThrow() {
		CacheIndexService cacheIndexService = new CacheIndexService();

		cacheIndexService.addUpdate(null, null, testsSchemaDefault);
	}

	@Test(expected = IllegalArgumentException.class)
	public void givenNullSchemaParameterThenAddUpdateThrow() {
		CacheIndexService cacheIndexService = new CacheIndexService();

		cacheIndexService.addUpdate(null, record1, null);
	}

	@Test(expected = IllegalArgumentException.class)
	public void givenNullMetadataParameterThenSearchThrow() {
		CacheIndexService cacheIndexService = new CacheIndexService();

		cacheIndexService.search(null, "Lala");
	}
}
