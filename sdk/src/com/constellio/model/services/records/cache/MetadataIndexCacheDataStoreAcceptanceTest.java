package com.constellio.model.services.records.cache;

import com.constellio.model.entities.CollectionInfo;
import com.constellio.model.entities.EnumWithSmallCode;
import com.constellio.model.entities.records.Record;
import com.constellio.model.entities.schemas.Metadata;
import com.constellio.model.entities.schemas.MetadataSchema;
import com.constellio.model.entities.schemas.MetadataSchemaType;
import com.constellio.model.entities.schemas.MetadataValueType;
import com.constellio.model.services.records.RecordServices;
import com.constellio.model.services.schemas.MetadataSchemaTypesAlteration;
import com.constellio.model.services.schemas.MetadataSchemasManager;
import com.constellio.model.services.schemas.builders.MetadataSchemaBuilder;
import com.constellio.model.services.schemas.builders.MetadataSchemaTypeBuilder;
import com.constellio.sdk.tests.ConstellioTest;
import org.junit.Before;
import org.junit.Test;

import java.util.Arrays;

import static com.constellio.model.services.records.cache.MetadataIndexCacheDataStoreAcceptanceTest.TestEnumClassWithSmallCode.VALUE1;
import static com.constellio.model.services.records.cache.MetadataIndexCacheDataStoreAcceptanceTest.TestEnumClassWithSmallCode.VALUE2;
import static org.assertj.core.api.Assertions.assertThat;

public class MetadataIndexCacheDataStoreAcceptanceTest extends ConstellioTest {

	Metadata cacheIndex;
	Metadata cacheEnumIndex;
	Metadata notCacheIndex;
	Metadata unique;
	Metadata cacheIndexMultiValue;

	MetadataSchema testsSchemaDefault;
	MetadataSchemaType testsSchemaTypeDefault;

	MetadataSchemasManager metadataSchemasManager;
	RecordServices recordServices;

	Record record1;
	Record record2;
	Record record3;
	Record record4;
	Record record1Update;


	public enum TestEnumClassWithSmallCode implements EnumWithSmallCode {
		VALUE1("1"), VALUE2("2");

		private String code;

		TestEnumClassWithSmallCode(String code) {
			this.code = code;
		}

		@Override
		public String getCode() {
			return code;
		}
	}

	@Before
	public void setup() throws Exception {
		prepareSystem(withZeCollection(), withCollection("secondCollection"));


		metadataSchemasManager = getModelLayerFactory().getMetadataSchemasManager();

		metadataSchemasManager.modify(zeCollection, (MetadataSchemaTypesAlteration) types -> {
			MetadataSchemaTypeBuilder testSchemaBuilder = types.createNewSchemaType("testschema");

			MetadataSchemaBuilder defaultTestSchemaBuilder = testSchemaBuilder.getDefaultSchema();

			defaultTestSchemaBuilder.create("cacheIndex").setType(MetadataValueType.STRING).setCacheIndex(true);
			defaultTestSchemaBuilder.create("cacheEnumIndex").defineAsEnum(TestEnumClassWithSmallCode.class).setCacheIndex(true);
			defaultTestSchemaBuilder.create("notCacheIndex").setType(MetadataValueType.STRING);
			defaultTestSchemaBuilder.create("cacheIndexMultiValue").setType(MetadataValueType.STRING).setCacheIndex(true)
					.setMultivalue(true);
			defaultTestSchemaBuilder.create("unique").setType(MetadataValueType.STRING).setUniqueValue(true);
		});

		testsSchemaTypeDefault = metadataSchemasManager.getSchemaTypes(zeCollection).getSchemaType("testschema");
		testsSchemaDefault = testsSchemaTypeDefault.getDefaultSchema();

		recordServices = getModelLayerFactory().newRecordServices();

		cacheIndex = testsSchemaDefault.getMetadata("cacheIndex");
		cacheEnumIndex = testsSchemaDefault.getMetadata("cacheEnumIndex");
		notCacheIndex = testsSchemaDefault.getMetadata("notCacheIndex");
		unique = testsSchemaDefault.getMetadata("unique");
		cacheIndexMultiValue = testsSchemaDefault.getMetadata("cacheIndexMultiValue");

		record1 = recordServices.newRecordWithSchema(testsSchemaDefault);
		record1.set(cacheIndex, "toBeFound1");
		record1.set(cacheEnumIndex, VALUE1);
		record1.set(notCacheIndex, "notCached");
		recordServices.add(record1);

		record2 = recordServices.newRecordWithSchema(testsSchemaDefault);
		record2.set(cacheIndex, "toBeFound2");
		record2.set(cacheEnumIndex, VALUE1);
		record2.set(notCacheIndex, "notCached2");
		recordServices.add(record2);

		record3 = recordServices.newRecordWithSchema(testsSchemaDefault);
		record3.set(cacheIndex, "toBeFound3");
		record3.set(cacheEnumIndex, VALUE2);
		record3.set(notCacheIndex, "notCached3");
		recordServices.add(record3);

		record4 = recordServices.newRecordWithSchema(testsSchemaDefault);
		record4.set(cacheIndex, "toBeFound3");
		record4.set(notCacheIndex, "notCached3");
		recordServices.add(record4);

		record1Update = recordServices.newRecordWithSchema(testsSchemaDefault, record1.getId());
		record1Update.set(cacheIndex, "updatedValue");
		record1Update.set(cacheEnumIndex, VALUE2);
		record1Update.set(notCacheIndex, "updatedValue");

	}


	@Test
	public void addRecordsToCacheIndexServiceThenValidateWhatIsInCache() {
		MetadataIndexCacheDataStore metadataIndexCacheDataStore = new MetadataIndexCacheDataStore();

		addRecordsToData(metadataIndexCacheDataStore);

		areAllZeCollectionInitialTestRecordThere(metadataIndexCacheDataStore);

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
		MetadataIndexCacheDataStore metadataIndexCacheDataStore = new MetadataIndexCacheDataStore();

		setInitialRecordsWithUniqueValues();

		addRecordsToData(metadataIndexCacheDataStore);

		assertThat(metadataIndexCacheDataStore.search(testsSchemaTypeDefault, unique, "unique1")).containsAll(Arrays.asList(record1.getId()));
		assertThat(metadataIndexCacheDataStore.search(testsSchemaTypeDefault, unique, "unique2")).containsAll(Arrays.asList(record2.getId()));
		assertThat(metadataIndexCacheDataStore.search(testsSchemaTypeDefault, unique, "unique3")).containsAll(Arrays.asList(record3.getId()));
		assertThat(metadataIndexCacheDataStore.search(testsSchemaTypeDefault, unique, "unique4")).containsAll(Arrays.asList(record4.getId()));

		assertThat(metadataIndexCacheDataStore.countByIterating()).isEqualTo(4);
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
		MetadataIndexCacheDataStore metadataIndexCacheDataStore = new MetadataIndexCacheDataStore();

		setInitialRecordsWithMultiValueValues();

		addRecordsToData(metadataIndexCacheDataStore);

		assertThat(metadataIndexCacheDataStore.search(testsSchemaTypeDefault, cacheIndexMultiValue, "multi1")).containsAll(Arrays.asList(record1.getId()));
		assertThat(metadataIndexCacheDataStore.search(testsSchemaTypeDefault, cacheIndexMultiValue, "multi2")).containsAll(Arrays.asList(record1.getId()));
		assertThat(metadataIndexCacheDataStore.search(testsSchemaTypeDefault, cacheIndexMultiValue, "multi3")).containsAll(Arrays.asList(record2.getId(), record3.getId()));
		assertThat(metadataIndexCacheDataStore.search(testsSchemaTypeDefault, cacheIndexMultiValue, "multi5")).containsAll(Arrays.asList(record3.getId(), record4.getId()));

		assertThat(metadataIndexCacheDataStore.countByIterating()).isEqualTo(8);
	}

	@Test
	public void updateRecordsWinMultiValueMetadataDataToCacheIndexServiceThenValidateWhatIsInCache() {
		MetadataIndexCacheDataStore metadataIndexCacheDataStore = new MetadataIndexCacheDataStore();

		setInitialRecordsWithMultiValueValues();

		addRecordsToData(metadataIndexCacheDataStore);

		assertThat(metadataIndexCacheDataStore.search(testsSchemaTypeDefault, cacheIndexMultiValue, "multi1")).containsAll(Arrays.asList(record1.getId()));
		assertThat(metadataIndexCacheDataStore.search(testsSchemaTypeDefault, cacheIndexMultiValue, "multi2")).containsAll(Arrays.asList(record1.getId()));

		assertThat(metadataIndexCacheDataStore.search(testsSchemaTypeDefault, cacheIndexMultiValue, "multi3")).contains(record3.getId());
		assertThat(metadataIndexCacheDataStore.search(testsSchemaTypeDefault, cacheIndexMultiValue, "multi4")).contains(record3.getId());
		assertThat(metadataIndexCacheDataStore.search(testsSchemaTypeDefault, cacheIndexMultiValue, "multi5")).contains(record3.getId());

		assertThat(metadataIndexCacheDataStore.countByIterating()).isEqualTo(8);

		record1Update = recordServices.newRecordWithSchema(testsSchemaDefault, record1.getId());
		record1Update.set(cacheIndexMultiValue, Arrays.asList("value2Multi1", "value2Multi2"));

		Record record3Update = recordServices.newRecordWithSchema(testsSchemaDefault, record3.getId());
		record3Update.set(cacheIndexMultiValue, Arrays.asList("multi5"));

		metadataIndexCacheDataStore.addUpdate(record1, record1Update, testsSchemaTypeDefault, testsSchemaDefault);
		metadataIndexCacheDataStore.addUpdate(record3, record3Update, testsSchemaTypeDefault, testsSchemaDefault);

		assertThat(metadataIndexCacheDataStore.search(testsSchemaTypeDefault, cacheIndexMultiValue, "multi1")).isEmpty();
		assertThat(metadataIndexCacheDataStore.search(testsSchemaTypeDefault, cacheIndexMultiValue, "multi2")).isEmpty();
		assertThat(metadataIndexCacheDataStore.search(testsSchemaTypeDefault, cacheIndexMultiValue, "multi3")).doesNotContain(record3.getId());
		assertThat(metadataIndexCacheDataStore.search(testsSchemaTypeDefault, cacheIndexMultiValue, "multi4")).isEmpty();
		assertThat(metadataIndexCacheDataStore.search(testsSchemaTypeDefault, cacheIndexMultiValue, "multi5")).containsAll(Arrays.asList(record3.getId(), record4.getId()));

		assertThat(metadataIndexCacheDataStore.search(testsSchemaTypeDefault, cacheIndexMultiValue, "value2Multi1")).containsAll(Arrays.asList(record1.getId()));
		assertThat(metadataIndexCacheDataStore.search(testsSchemaTypeDefault, cacheIndexMultiValue, "value2Multi2")).containsAll(Arrays.asList(record1.getId()));

		assertThat(metadataIndexCacheDataStore.countByIterating()).isEqualTo(6);
	}

	private void areAllZeCollectionInitialTestRecordThere(MetadataIndexCacheDataStore metadataIndexCacheDataStore) {
		assertThat(metadataIndexCacheDataStore.search(testsSchemaTypeDefault, cacheIndex, "toBeFound1")).containsAll(Arrays.asList(record1.getId()));
		assertThat(metadataIndexCacheDataStore.search(testsSchemaTypeDefault, cacheIndex, "toBeFound2")).containsAll(Arrays.asList(record2.getId()));
		assertThat(metadataIndexCacheDataStore.search(testsSchemaTypeDefault, cacheIndex, "toBeFound3")).containsAll(Arrays.asList(record3.getId(), record4.getId()));
		assertThat(metadataIndexCacheDataStore.search(testsSchemaTypeDefault, cacheEnumIndex, VALUE1)).containsOnly(record1.getId(), record2.getId());
		assertThat(metadataIndexCacheDataStore.search(testsSchemaTypeDefault, cacheEnumIndex, VALUE2)).containsOnly(record3.getId());
	}

	private void addRecordsToData(MetadataIndexCacheDataStore metadataIndexCacheDataStore) {
		metadataIndexCacheDataStore.addUpdate(null, record1, testsSchemaTypeDefault, testsSchemaDefault);
		metadataIndexCacheDataStore.addUpdate(null, record2, testsSchemaTypeDefault, testsSchemaDefault);
		metadataIndexCacheDataStore.addUpdate(null, record3, testsSchemaTypeDefault, testsSchemaDefault);
		metadataIndexCacheDataStore.addUpdate(null, record4, testsSchemaTypeDefault, testsSchemaDefault);
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

		MetadataIndexCacheDataStore metadataIndexCacheDataStore = new MetadataIndexCacheDataStore();

		addRecordsToData(metadataIndexCacheDataStore);

		metadataIndexCacheDataStore.addUpdate(null, recordSecondSchemaType1, testsSchemaTypeDefault, testsSchemaDefaultSecond);
		metadataIndexCacheDataStore.addUpdate(null, recordSecondSchemaType2, testsSchemaTypeDefault, testsSchemaDefaultSecond);

		metadataIndexCacheDataStore.addUpdate(record1, null, testsSchemaTypeDefault, testsSchemaDefault);
		metadataIndexCacheDataStore.addUpdate(record2, null, testsSchemaTypeDefault, testsSchemaDefault);
		metadataIndexCacheDataStore.addUpdate(record3, null, testsSchemaTypeDefault, testsSchemaDefault);
		metadataIndexCacheDataStore.addUpdate(record4, null, testsSchemaTypeDefault, testsSchemaDefault);
	}

	@Test
	public void updateMetadataFromCacheToCacheIndexServiceThenValidateWhatIsInCache() {
		MetadataIndexCacheDataStore metadataIndexCacheDataStore = new MetadataIndexCacheDataStore();

		addRecordsToData(metadataIndexCacheDataStore);

		metadataIndexCacheDataStore.addUpdate(record1, record1Update, testsSchemaTypeDefault, testsSchemaDefault);

		System.out.println("toBeFound1".hashCode());
		System.out.println("updatedValue".hashCode());

		assertThat(metadataIndexCacheDataStore.search(testsSchemaTypeDefault, cacheIndex, "toBeFound1")).isEmpty();
		assertThat(metadataIndexCacheDataStore.search(testsSchemaTypeDefault, cacheIndex, "updatedValue")).containsAll(Arrays.asList(record1.getId()));

		assertThat(metadataIndexCacheDataStore.search(testsSchemaTypeDefault, cacheEnumIndex, VALUE1)).containsOnly(record2.getId());
		assertThat(metadataIndexCacheDataStore.search(testsSchemaTypeDefault, cacheEnumIndex, VALUE2)).containsOnly(record1.getId(), record3.getId());

	}

	@Test
	public void removeMetadataFromCacheToCacheIndexServiceThenValidateWhatIsInCache() {
		MetadataIndexCacheDataStore metadataIndexCacheDataStore = new MetadataIndexCacheDataStore();

		addRecordsToData(metadataIndexCacheDataStore);

		assertThat(metadataIndexCacheDataStore.search(testsSchemaTypeDefault, cacheIndex, "toBeFound1")).containsAll(Arrays.asList(record1.getId()));

		metadataIndexCacheDataStore.addUpdate(record1, null, testsSchemaTypeDefault, testsSchemaDefault);

		assertThat(metadataIndexCacheDataStore.search(testsSchemaTypeDefault, cacheIndex, "toBeFound1")).isEmpty();

		assertThat(metadataIndexCacheDataStore.search(testsSchemaTypeDefault, cacheIndex, "toBeFound3")).containsAll(Arrays.asList(record3.getId(), record4.getId()));

		assertThat(metadataIndexCacheDataStore.search(testsSchemaTypeDefault, cacheEnumIndex, VALUE1)).containsOnly(record2.getId());
		assertThat(metadataIndexCacheDataStore.search(testsSchemaTypeDefault, cacheEnumIndex, VALUE2)).containsOnly(record3.getId());

		metadataIndexCacheDataStore.addUpdate(record3, null, testsSchemaTypeDefault, testsSchemaDefault);

		assertThat(metadataIndexCacheDataStore.search(testsSchemaTypeDefault, cacheIndex, "toBeFound3")).containsAll(Arrays.asList(record4.getId()));

		assertThat(metadataIndexCacheDataStore.search(testsSchemaTypeDefault, cacheEnumIndex, VALUE1)).containsOnly(record2.getId());
		assertThat(metadataIndexCacheDataStore.search(testsSchemaTypeDefault, cacheEnumIndex, VALUE2)).isEmpty();

	}

	@Test
	public void givenRecordsIn2CollectionsThenClearOneCollection() throws Exception {

		metadataSchemasManager.modify("secondCollection", (MetadataSchemaTypesAlteration) types -> {
			MetadataSchemaTypeBuilder testSchemaBuilder = types.createNewSchemaType("testschema");

			MetadataSchemaBuilder defaultTestSchemaBuilder = testSchemaBuilder.getDefaultSchema();

			defaultTestSchemaBuilder.create("cacheIndex").setType(MetadataValueType.STRING).setCacheIndex(true);
		});


		MetadataSchemaType testsSchemaTypeSecondCollection = metadataSchemasManager.getSchemaTypes("secondCollection").getSchemaType("testschema");
		MetadataSchema testsSchemaDefaultSecondCollection = testsSchemaTypeSecondCollection.getDefaultSchema();
		Metadata cacheIndexSecondCollection = testsSchemaDefaultSecondCollection.getMetadata("cacheIndex");

		Record recordSecondCollection1 = recordServices.newRecordWithSchema(testsSchemaDefaultSecondCollection);
		recordSecondCollection1.set(cacheIndexSecondCollection, "toBeFound1");


		Record recordSecondCollection2 = recordServices.newRecordWithSchema(testsSchemaDefaultSecondCollection);
		recordSecondCollection2.set(cacheIndex, "toBeFound2");


		MetadataIndexCacheDataStore metadataIndexCacheDataStore = new MetadataIndexCacheDataStore();

		addRecordsToData(metadataIndexCacheDataStore);

		metadataIndexCacheDataStore.addUpdate(null, recordSecondCollection1, testsSchemaTypeSecondCollection, testsSchemaDefaultSecondCollection);
		metadataIndexCacheDataStore.addUpdate(null, recordSecondCollection2, testsSchemaTypeSecondCollection, testsSchemaDefaultSecondCollection);

		// Test before clear state
		areAllZeCollectionInitialTestRecordThere(metadataIndexCacheDataStore);
		assertThat(metadataIndexCacheDataStore.search(testsSchemaTypeSecondCollection, cacheIndexSecondCollection, "toBeFound1")).containsAll(Arrays.asList(recordSecondCollection1.getId()));
		assertThat(metadataIndexCacheDataStore.search(testsSchemaTypeSecondCollection, cacheIndexSecondCollection, "toBeFound2")).containsAll(Arrays.asList(recordSecondCollection2.getId()));

		CollectionInfo secondCollectionInfo = getModelLayerFactory().getCollectionsListManager().getCollectionInfo("secondCollection");

		metadataIndexCacheDataStore.clear(secondCollectionInfo);

		// Test after clear
		areAllZeCollectionInitialTestRecordThere(metadataIndexCacheDataStore);
		assertThat(metadataIndexCacheDataStore.search(testsSchemaTypeSecondCollection, cacheIndexSecondCollection, "toBeFound1")).isEmpty();
		assertThat(metadataIndexCacheDataStore.search(testsSchemaTypeSecondCollection, cacheIndexSecondCollection, "toBeFound2")).isEmpty();
	}


	@Test
	public void givenRecordsInTwoSchemaTypeThenClearOneShemaType() throws Exception {
		metadataSchemasManager.modify(zeCollection, (MetadataSchemaTypesAlteration) types -> {
			MetadataSchemaTypeBuilder testSchemaBuilder = types.createNewSchemaType("testSecondSchema");


			MetadataSchemaBuilder defaultTestSchemaBuilder = testSchemaBuilder.getDefaultSchema();

			defaultTestSchemaBuilder.create("cacheIndexSecond").setType(MetadataValueType.STRING).setCacheIndex(true);
		});


		MetadataIndexCacheDataStore metadataIndexCacheDataStore = new MetadataIndexCacheDataStore();

		addRecordsToData(metadataIndexCacheDataStore);

		MetadataSchemaType secondTestSchemaType = metadataSchemasManager.getSchemaTypes(zeCollection).getSchemaType("testSecondSchema");
		MetadataSchema testsSchemaDefaultSecond = secondTestSchemaType.getDefaultSchema();


		Metadata cacheIndexSecond = testsSchemaDefaultSecond.getMetadata("cacheIndexSecond");

		Record recordSecondSchemaType1 = recordServices.newRecordWithSchema(testsSchemaDefaultSecond);
		recordSecondSchemaType1.set(cacheIndexSecond, "lalala1");

		Record recordSecondSchemaType2 = recordServices.newRecordWithSchema(testsSchemaDefaultSecond);
		recordSecondSchemaType2.set(cacheIndexSecond, "lalala2");

		metadataIndexCacheDataStore.addUpdate(null, recordSecondSchemaType1, secondTestSchemaType, testsSchemaDefaultSecond);
		metadataIndexCacheDataStore.addUpdate(null, recordSecondSchemaType2, secondTestSchemaType, testsSchemaDefaultSecond);

		// Test before remove state.
		areAllZeCollectionInitialTestRecordThere(metadataIndexCacheDataStore);

		assertThat(metadataIndexCacheDataStore.search(secondTestSchemaType, cacheIndexSecond, "lalala1")).containsAll(Arrays.asList(recordSecondSchemaType1.getId()));
		assertThat(metadataIndexCacheDataStore.search(secondTestSchemaType, cacheIndexSecond, "lalala2")).containsAll(Arrays.asList(recordSecondSchemaType2.getId()));


		metadataIndexCacheDataStore.clear(secondTestSchemaType);

		areAllZeCollectionInitialTestRecordThere(metadataIndexCacheDataStore);
		assertThat(metadataIndexCacheDataStore.search(secondTestSchemaType, cacheIndexSecond, "lalala1")).isEmpty();
		assertThat(metadataIndexCacheDataStore.search(secondTestSchemaType, cacheIndexSecond, "lalala2")).isEmpty();
	}

	@Test(expected = IllegalArgumentException.class)
	public void searchWithNonCacheIndexMetadataThenExceptionThrown() {
		MetadataIndexCacheDataStore metadataIndexCacheDataStore = new MetadataIndexCacheDataStore();

		addRecordsToData(metadataIndexCacheDataStore);

		metadataIndexCacheDataStore.search(testsSchemaTypeDefault, notCacheIndex, "toBeFound1");
	}

	@Test
	public void givenNullValueOnSearchThenReturnNull() {
		MetadataIndexCacheDataStore metadataIndexCacheDataStore = new MetadataIndexCacheDataStore();

		assertThat(metadataIndexCacheDataStore.search(testsSchemaTypeDefault, cacheIndex, null)).isEmpty();
	}

	@Test(expected = IllegalArgumentException.class)
	public void givenNullRecordsThenThrow() {
		MetadataIndexCacheDataStore metadataIndexCacheDataStore = new MetadataIndexCacheDataStore();

		metadataIndexCacheDataStore.addUpdate(null, null, testsSchemaTypeDefault, testsSchemaDefault);
	}

	@Test(expected = IllegalArgumentException.class)
	public void givenNullMetadataParameterThenSearchThrow() {
		MetadataIndexCacheDataStore metadataIndexCacheDataStore = new MetadataIndexCacheDataStore();

		metadataIndexCacheDataStore.search(testsSchemaTypeDefault, null, "Lala");
	}
}
