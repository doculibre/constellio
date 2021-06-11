package com.constellio.model.services.search;

import com.constellio.app.modules.rm.model.enums.FolderStatus;
import com.constellio.data.utils.dev.Toggle;
import com.constellio.model.entities.records.Record;
import com.constellio.model.entities.records.Transaction;
import com.constellio.model.entities.schemas.Metadata;
import com.constellio.model.entities.schemas.MetadataSchema;
import com.constellio.model.services.collections.CollectionsListManager;
import com.constellio.model.services.records.RecordServices;
import com.constellio.model.services.schemas.MetadataSchemaTypesAlteration;
import com.constellio.model.services.schemas.MetadataSchemasManager;
import com.constellio.model.services.schemas.builders.MetadataSchemaBuilder;
import com.constellio.model.services.schemas.builders.MetadataSchemaTypesBuilder;
import com.constellio.model.services.search.SearchServicesRuntimeException.TooManyRecordsInSingleSearchResult;
import com.constellio.model.services.search.query.logical.LogicalSearchQuery;
import com.constellio.sdk.tests.ConstellioTest;
import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.NotNull;
import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

import static com.constellio.model.entities.schemas.MetadataValueType.*;
import static com.constellio.model.entities.schemas.RecordCacheType.FULLY_CACHED;
import static com.constellio.model.entities.schemas.RecordCacheType.SUMMARY_CACHED_WITHOUT_VOLATILE;
import static com.constellio.model.services.search.query.logical.LogicalSearchQueryOperators.from;
import static java.util.Arrays.*;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.fail;

public class SearchServiceCacheAcceptanceTest extends ConstellioTest {

	SearchServices searchService;
	MetadataSchemasManager schemasManager;
	RecordServices recordServices;
	CollectionsListManager collectionsListManager;
	byte zeCollectionId;

	@Before
	public void setUp() {
		// USE_CACHE_FOR_QUERY_EXECUTION

		schemasManager = getModelLayerFactory().getMetadataSchemasManager();
		recordServices = getModelLayerFactory().newRecordServices();
		searchService = getModelLayerFactory().newSearchServices();
		collectionsListManager = getModelLayerFactory().getCollectionsListManager();

		givenCollection(zeCollection);

		zeCollectionId = collectionsListManager.getCollectionId(zeCollection);
	}


	@Test
	public void whenInsertingFullyCachedZeroPaddedRecordsThenRecordsFindableUsingMetadata() throws Exception {
		whenInsertingRecordsThenRecordsFindableUsingMetadata(false,true, true);
	}


	private void whenInsertingRecordsThenRecordsFindableUsingMetadata(boolean isUseCacheForQueryExecution, boolean fullyCached, boolean zeroPaddedId)
			throws Exception {

		if(isUseCacheForQueryExecution) {
			Toggle.USE_CACHE_FOR_QUERY_EXECUTION.enable();
		}

		schemasManager.modify(zeCollection, new MetadataSchemaTypesAlteration() {
			@Override
			public void alter(MetadataSchemaTypesBuilder types) {

				MetadataSchemaBuilder typeSchema1 = types.createNewSchemaTypeWithSecurity("type1")
						.setRecordCacheType(fullyCached ? FULLY_CACHED : SUMMARY_CACHED_WITHOUT_VOLATILE).getDefaultSchema();

				MetadataSchemaBuilder typeSchema2 = types.createNewSchemaTypeWithSecurity("type2")
						.setRecordCacheType(fullyCached ? FULLY_CACHED : SUMMARY_CACHED_WITHOUT_VOLATILE).getDefaultSchema();


				//typeSchema1.create("uniqueIntegerMetadata").setType(INTEGER).setUniqueValue(true);
				typeSchema1.create("uniqueStringMetadata").setType(STRING).setUniqueValue(true);

				//typeSchema1.create("integersMetadata").setType(INTEGER).setMultivalue(true);
				typeSchema1.create("numbersMetadata").setType(NUMBER).setMultivalue(true);
				typeSchema1.create("booleanMetadata").setType(BOOLEAN);
				typeSchema1.create("stringsMetadata").setType(STRING).setMultivalue(true);
				typeSchema1.create("enumsMetadata").setType(ENUM).setMultivalue(true).defineAsEnum(FolderStatus.class);
				typeSchema1.create("referencesMetadata").setType(REFERENCE).setMultivalue(true)
						.defineReferencesTo(types.getSchemaType("type2"));

				//typeSchema2.create("uniqueIntegerMetadata").setType(INTEGER).setUniqueValue(true);
				typeSchema2.create("uniqueStringMetadata").setType(STRING).setUniqueValue(true);
			}
		});

		short collection1Type1 = schemasManager.getSchemaTypes(zeCollectionId).getSchemaType("type1").getId();
		short collection1Type2 = schemasManager.getSchemaTypes(zeCollectionId).getSchemaType("type2").getId();

		String id1 = zeroPaddedId ? zeroPadded(11) : "11";
		String id2 = zeroPaddedId ? zeroPadded(12) : "12";
		String id3 = zeroPaddedId ? zeroPadded(13) : "13";
		String id4 = zeroPaddedId ? zeroPadded(14) : "14";
		String id5 = zeroPaddedId ? zeroPadded(15) : "15";
		String id6 = zeroPaddedId ? zeroPadded(16) : "16";
		String id7 = zeroPaddedId ? zeroPadded(17) : "17";
		String id8 = zeroPaddedId ? zeroPadded(18) : "18";
		String id9 = zeroPaddedId ? zeroPadded(19) : "19";


		MetadataSchema metadataSchemaType1 = schemasManager.getSchemaTypes(zeCollectionId).getDefaultSchema("type1");
		MetadataSchema metadataSchemaType2 = schemasManager.getSchemaTypes(zeCollectionId).getDefaultSchema("type2");


		List<Record> recordToInsert = new ArrayList<>();

//		Metadata uniqueIntegerMetadataType1 = metadataSchemaType1.get("uniqueIntegerMetadata");
		Metadata uniqueStringMetadataType1 = metadataSchemaType1.get("uniqueStringMetadata");
//		Metadata integersMetadataType1 = metadataSchemaType1.get("integersMetadata");
		Metadata numbersMetadataType1 = metadataSchemaType1.get("numbersMetadata");
		Metadata booleanMetadataType1 =metadataSchemaType1.get("booleanMetadata");
		Metadata stringsMetadataType1= metadataSchemaType1.get("stringsMetadata");
		Metadata enumsMetadataMetadataType1 = metadataSchemaType1.get("enumsMetadata");
		Metadata referencesMetadataType1 = metadataSchemaType1.get("referencesMetadata");

//		Metadata uniqueIntegerMetadataType2 = metadataSchemaType2.get("uniqueIntegerMetadata");
		Metadata uniqueStringMetadataType2 = metadataSchemaType2.get("uniqueStringMetadata");


		recordToInsert.add(recordServices.newRecordWithSchema(metadataSchemaType2, id1)
				//.set(uniqueIntegerMetadataType2, 1)
				.set(uniqueStringMetadataType2, "A"));

		recordToInsert.add(recordServices.newRecordWithSchema(metadataSchemaType2, id2)
				//.set(uniqueIntegerMetadataType2, 2)
				.set(uniqueStringMetadataType2, "B"));

		recordToInsert.add(recordServices.newRecordWithSchema(metadataSchemaType2, id3)
				//.set(uniqueIntegerMetadataType2, 3)
				.set(uniqueStringMetadataType2, "C"));

		recordToInsert.add(recordServices.newRecordWithSchema(metadataSchemaType1, id4)
				//.set(uniqueIntegerMetadataType1, 1)
				.set(uniqueStringMetadataType1, "A")
				//.set(integersMetadataType1,  asList(42, 56))
				.set(numbersMetadataType1, asList(12.3, 45.6))
				.set(booleanMetadataType1, true)
				.set(stringsMetadataType1, asList("abc", "def"))
				.set(enumsMetadataMetadataType1, asList(FolderStatus.SEMI_ACTIVE))
				.set(referencesMetadataType1, asList(id1)));

		recordToInsert.add(recordServices.newRecordWithSchema(metadataSchemaType1, id5)
				//.set(uniqueIntegerMetadataType1, 2)
				.set(uniqueStringMetadataType1, "B")
				//.set(integersMetadataType1,  asList(123, 456))
				.set(numbersMetadataType1, asList(11.1, 75.10))
				.set(booleanMetadataType1, false)
				.set(stringsMetadataType1, asList("gh", "ij"))
				.set(enumsMetadataMetadataType1, asList(FolderStatus.ACTIVE))
				.set(referencesMetadataType1, asList(id1, id2)));

		recordToInsert.add(recordServices.newRecordWithSchema(metadataSchemaType1, id6)
				//.set(uniqueIntegerMetadataType1, 3)
				.set(uniqueStringMetadataType1, "C")
				//.set(integersMetadataType1,  asList(444, 555))
				.set(numbersMetadataType1, asList(1000.0001, 2000.0002))
				.set(booleanMetadataType1, false)
				.set(stringsMetadataType1, asList("yyyy", "zzzz"))
				.set(enumsMetadataMetadataType1, asList(FolderStatus.INACTIVE_DEPOSITED))
				.set(referencesMetadataType1, asList(id1)));

		recordToInsert.add(recordServices.newRecordWithSchema(metadataSchemaType1, id7)
				//.set(uniqueIntegerMetadataType1, 4)
				.set(uniqueStringMetadataType1, "D")
				//.set(integersMetadataType1,  asList(460, 461))
				.set(numbersMetadataType1, asList(1300.0001, 21000.0002))
				.set(booleanMetadataType1, false)
				.set(stringsMetadataType1, asList("pfpfp", "asdas"))
				.set(enumsMetadataMetadataType1, asList(FolderStatus.INACTIVE_DEPOSITED))
				.set(referencesMetadataType1, asList(id3)));

		recordToInsert.add(recordServices.newRecordWithSchema(metadataSchemaType1, id8)
				//.set(uniqueIntegerMetadataType1, 5)
				.set(uniqueStringMetadataType1, "D")
				//.set(integersMetadataType1,  asList(460, 461))
				.set(numbersMetadataType1, asList(1300.0001, 21000.0002))
				.set(booleanMetadataType1, false)
				.set(stringsMetadataType1, asList("pfpfp", "asdas"))
				.set(enumsMetadataMetadataType1, asList(FolderStatus.INACTIVE_DEPOSITED))
				.set(referencesMetadataType1, asList(id3)));

		Transaction transaction = new Transaction();
		transaction.addAll(recordToInsert);
		recordServices.execute(transaction);

//		queryOneMetadata(metadataSchemaType2, uniqueIntegerMetadataType2, 1, id1);
//
//		queryOneMetadata(metadataSchemaType2, uniqueIntegerMetadataType2, 2, id2);
//
//		queryOneMetadata(metadataSchemaType2, uniqueIntegerMetadataType2, 3, id3);
//
//		queryOneMetadata(metadataSchemaType1, uniqueIntegerMetadataType1, 1, id4);
//
//		queryOneMetadata(metadataSchemaType1, uniqueIntegerMetadataType1, 2, id5);
//
//		queryOneMetadata(metadataSchemaType1, uniqueIntegerMetadataType1, 3, id6);
//
//		queryOneMetadata(metadataSchemaType1, uniqueIntegerMetadataType1, 42);

		queryOneMetadata(metadataSchemaType2, uniqueStringMetadataType2, "A", id1);

		queryOneMetadata(metadataSchemaType2, uniqueStringMetadataType2, "B", id2);

		queryOneMetadata(metadataSchemaType2, uniqueStringMetadataType2, "C", id3);

		queryOneMetadata(metadataSchemaType1, uniqueStringMetadataType1, "A" ,id4);

		queryOneMetadata(metadataSchemaType1, uniqueStringMetadataType1, "B", id5);

		queryOneMetadata(metadataSchemaType1, uniqueStringMetadataType1, "C", id6);

//		queryOneMetadata(metadataSchemaType1, integersMetadataType1, asList(42, 56), id4);
//
//		queryOneMetadata(metadataSchemaType1, integersMetadataType1, asList(123, 456), id5);
//
//		queryOneMetadata(metadataSchemaType1, integersMetadataType1, asList(444, 555), id6);
//
//		queryOneMetadata(metadataSchemaType1, integersMetadataType1, asList(460, 461), id7, id8);

		queryOneMetadata(metadataSchemaType1, numbersMetadataType1,  45.6, id4);

		queryOneMetadata(metadataSchemaType1, numbersMetadataType1, 75.10, id5);

		queryOneMetadata(metadataSchemaType1, numbersMetadataType1, 1000.0001, id6);

		queryOneMetadata(metadataSchemaType1, numbersMetadataType1, 1300.0001, id7, id8);

		queryOneMetadata(metadataSchemaType1, booleanMetadataType1, true, id4);

		queryOneMetadata(metadataSchemaType1, booleanMetadataType1, false, id5, id6, id7, id8);

		queryOneMetadata(metadataSchemaType1, stringsMetadataType1, "abc", id4);

		queryOneMetadata(metadataSchemaType1, stringsMetadataType1, "gh", id5);

		queryOneMetadata(metadataSchemaType1, stringsMetadataType1, "yyyy", id6);

		queryOneMetadata(metadataSchemaType1, stringsMetadataType1, "pfpfp", id7, id8);

		queryOneMetadata(metadataSchemaType1, enumsMetadataMetadataType1, FolderStatus.SEMI_ACTIVE, id4);

		queryOneMetadata(metadataSchemaType1, enumsMetadataMetadataType1, FolderStatus.INACTIVE_DEPOSITED, id6, id7, id8);

		queryOneMetadata(metadataSchemaType1, referencesMetadataType1, id2, id5);

		queryOneMetadata(metadataSchemaType1, referencesMetadataType1, id3, id7, id8);

		Record recordToUpdate = recordServices.getDocumentById(id4);

		recordToUpdate.set(uniqueStringMetadataType1, "W")
				//.set(uniqueIntegerMetadataType1, 14)
				//.set(integersMetadataType1, asList(4212, 564))
				.set(numbersMetadataType1, asList(12.332, 45.9023))
				.set(booleanMetadataType1, false)
				.set(stringsMetadataType1, asList("lalala", "lalal2"))
				.set(enumsMetadataMetadataType1, asList(FolderStatus.ACTIVE))
				.set(referencesMetadataType1, asList(id3));

		recordServices.update(recordToUpdate);

//		queryOneMetadata(metadataSchemaType1, uniqueIntegerMetadataType1, 1);
		queryOneMetadata(metadataSchemaType1, uniqueStringMetadataType1, "A");
		queryOneMetadata(metadataSchemaType1, numbersMetadataType1, 12.3);
		queryOneMetadata(metadataSchemaType1, numbersMetadataType1, 45.6);
//		queryOneMetadata(metadataSchemaType1, integersMetadataType1, 42);
//		queryOneMetadata(metadataSchemaType1, integersMetadataType1, 56);
		queryOneMetadata(metadataSchemaType1, booleanMetadataType1, true);
		queryOneMetadata(metadataSchemaType1, stringsMetadataType1, "abc");
		queryOneMetadata(metadataSchemaType1, stringsMetadataType1, "def");
		queryOneMetadata(metadataSchemaType1, enumsMetadataMetadataType1, FolderStatus.SEMI_ACTIVE);
		queryOneMetadata(metadataSchemaType1, enumsMetadataMetadataType1, id1);

		//		queryOneMetadata(metadataSchemaType1, uniqueIntegerMetadataType1, 14);
		queryOneMetadata(metadataSchemaType1, uniqueStringMetadataType1, "W", id4);
		queryOneMetadata(metadataSchemaType1, numbersMetadataType1, 12.332, id4);
		queryOneMetadata(metadataSchemaType1, numbersMetadataType1, 45.9023, id4);
		//		queryOneMetadata(metadataSchemaType1, integersMetadataType1, 4212, id4);
		//		queryOneMetadata(metadataSchemaType1, integersMetadataType1, 564, id4);
		queryOneMetadata(metadataSchemaType1, booleanMetadataType1, false, id5, id6, id7, id8, id4);
		queryOneMetadata(metadataSchemaType1, stringsMetadataType1, "lalala", id4);
		queryOneMetadata(metadataSchemaType1, stringsMetadataType1, "lalal2", id4);
		queryOneMetadata(metadataSchemaType1, enumsMetadataMetadataType1, FolderStatus.ACTIVE, id5, id4);
		queryOneMetadata(metadataSchemaType1, enumsMetadataMetadataType1, id3);
	}



	private void queryOneMetadata(MetadataSchema metadataSchema, Metadata metadata, Object value, String ... ids) {

		// query method test
		LogicalSearchQuery logicalSearchQuery = new LogicalSearchQuery(from(metadataSchema).where(metadata).isEqualTo(value));

		SPEQueryResponse speQueryResponse = searchService.query(logicalSearchQuery);

		List<String> idListOfQuery = recordsToIdList(speQueryResponse.getRecords());

		if(ids == null || ids.length == 0) {
			assertThat(idListOfQuery).isNullOrEmpty();
		} else {
			assertThat(idListOfQuery).containsOnly(ids);
		}


		// search method test
		List<Record> searchRecordsResult = searchService.search(logicalSearchQuery);

		List<String> idListOfSearch = recordsToIdList(searchRecordsResult);


		if(ids == null) {
			assertThat(idListOfSearch).isNullOrEmpty();
		} else {
			assertThat(idListOfSearch).containsOnly(ids);
		}

		// uniqueRecord
		if(ids == null || ids.length == 0) {
			Record singleResult = searchService.searchSingleResult(logicalSearchQuery.getCondition());
			assertThat(singleResult).isNull();
		} else if(ids.length == 1) {
			Record singleResult = searchService.searchSingleResult(logicalSearchQuery.getCondition());
			assertThat(singleResult).isNotNull();
			assertThat(singleResult.getId()).isEqualTo(ids[0]);
		} else {
			try {
				searchService.searchSingleResult(logicalSearchQuery.getCondition());
				fail("should throw an exception because there is more than one result expected");
			} catch(TooManyRecordsInSingleSearchResult tooManyRecordsInSingleSearchResult) {
				// Ok
			}
		}

		// count method
		assertThat(searchService.getResultsCount(logicalSearchQuery)).isEqualTo(ids == null ? 0 : ids.length);
		assertThat(searchService.getResultsCount(logicalSearchQuery.getCondition())).isEqualTo(ids == null ? 0 : ids.length);
	}

	@NotNull
	private List<String> recordsToIdList(List<Record> records) {
		List<String> idList = new ArrayList<>();

		for(Record record : records) {
			idList.add(record.getId());
		}
		return idList;
	}

	private String zeroPadded(int i) {
		return StringUtils.leftPad("" + i, 12 - ("" + i).length(), '0');
	}
}
