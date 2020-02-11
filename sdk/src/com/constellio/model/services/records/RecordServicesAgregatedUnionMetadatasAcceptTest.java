package com.constellio.model.services.records;

import com.constellio.data.dao.services.bigVault.solr.BigVaultServer;
import com.constellio.data.dao.services.bigVault.solr.BigVaultServerTransaction;
import com.constellio.model.entities.records.Transaction;
import com.constellio.model.entities.schemas.Metadata;
import com.constellio.model.entities.schemas.RecordCacheType;
import com.constellio.model.entities.schemas.Schemas;
import com.constellio.model.services.schemas.builders.MetadataBuilder;
import com.constellio.model.services.schemas.builders.MetadataSchemaTypeBuilder;
import com.constellio.model.services.schemas.builders.MetadataSchemaTypesBuilder;
import com.constellio.sdk.tests.ConstellioTest;
import com.constellio.sdk.tests.TestRecord;
import com.constellio.sdk.tests.schemas.MetadataSchemaTypesConfigurator;
import com.constellio.sdk.tests.schemas.TestsSchemasSetup;
import com.constellio.sdk.tests.schemas.TestsSchemasSetup.AnotherSchemaMetadatas;
import com.constellio.sdk.tests.schemas.TestsSchemasSetup.ThirdSchemaMetadatas;
import com.constellio.sdk.tests.schemas.TestsSchemasSetup.ZeSchemaMetadatas;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.MethodSorters;
import org.junit.runners.Parameterized;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;

import static com.constellio.data.dao.dto.records.RecordsFlushing.NOW;
import static com.constellio.model.entities.schemas.MetadataValueType.STRING;
import static com.constellio.model.entities.schemas.RecordCacheType.FULLY_CACHED;
import static com.constellio.model.entities.schemas.RecordCacheType.NOT_CACHED;
import static com.constellio.model.services.records.RecordServicesAgregatedMetadatasMechanicAcceptTest.clearAggregateMetadatasThenReindexReturningQtyOfQueriesOf;
import static com.constellio.sdk.tests.TestUtils.getNetworkLinksOf;
import static com.constellio.sdk.tests.TestUtils.solrInputDocumentRemovingMetadatas;
import static java.util.Arrays.asList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.tuple;

@RunWith(Parameterized.class)
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class RecordServicesAgregatedUnionMetadatasAcceptTest extends ConstellioTest {

	TestsSchemasSetup schemas = new TestsSchemasSetup(zeCollection);
	ZeSchemaMetadatas zeSchema = schemas.new ZeSchemaMetadatas();
	AnotherSchemaMetadatas anotherSchema = schemas.new AnotherSchemaMetadatas();
	ThirdSchemaMetadatas thirdSchema = schemas.new ThirdSchemaMetadatas();
	RecordServicesAgregatedMetadatasAcceptTestRecords records = new RecordServicesAgregatedMetadatasAcceptTestRecords();

	boolean inputMetadatasEssentialInSummary;
	RecordCacheType recordCacheType;
	boolean usingCache;

	public RecordServicesAgregatedUnionMetadatasAcceptTest(String testCase) {
		if (testCase.equals("noCache")) {
			recordCacheType = NOT_CACHED;

		} else if (testCase.equals("fullyPermanent")) {
			recordCacheType = RecordCacheType.FULLY_CACHED;
			usingCache = true;

		} else if (testCase.equals("summaryPermanent") || testCase.equals("summaryPermanentWithInsufficientSummaryMetadatas")) {
			recordCacheType = RecordCacheType.SUMMARY_CACHED_WITHOUT_VOLATILE;
			usingCache = inputMetadatasEssentialInSummary = testCase.equals("summaryPermanent");

		}
	}

	@Parameterized.Parameters(name = "{0}")
	public static Collection<Object[]> testCases() {
		return Arrays.asList(new Object[][]{
				{"noCache"}, {"fullyPermanent"}, {"summaryPermanent"}, {"summaryPermanentWithInsufficientSummaryMetadatas"}});
	}

	@Test
	public void givenUnionOfSingleValueStringMetadatasThenAllStringsAreCopiedWithoutDuplicates()
			throws Exception {

		givenBackgroundThreadsEnabled();
		defineSchemasManager().using(schemas.with(new MetadataSchemaTypesConfigurator() {
			@Override
			public void configure(MetadataSchemaTypesBuilder schemaTypes) {
				MetadataSchemaTypeBuilder zeType = schemaTypes.getSchemaType(zeSchema.typeCode());
				MetadataSchemaTypeBuilder anotherType = schemaTypes.getSchemaType(anotherSchema.typeCode());
				MetadataSchemaTypeBuilder thirdType = schemaTypes.getSchemaType(thirdSchema.typeCode());
				MetadataBuilder zeSchema_value1 = zeType.createMetadata("stringValue1").setType(STRING)
						.setEssentialInSummary(inputMetadatasEssentialInSummary);
				MetadataBuilder zeSchema_Value2 = zeType.createMetadata("stringValue2").setType(STRING)
						.setMultivalue(true).setEssentialInSummary(inputMetadatasEssentialInSummary);
				MetadataBuilder zeSchema_zeRef = zeType.createMetadata("ref")
						.defineReferencesTo(anotherType).setCacheIndex(true);
				MetadataBuilder anotherSchema_stringValuesUnion = anotherType.createMetadata("stringValuesUnion")
						.setType(STRING).setEssentialInSummary(inputMetadatasEssentialInSummary)
						.setMultivalue(true).defineDataEntry().asUnion(zeSchema_zeRef, zeSchema_value1, zeSchema_Value2);
				MetadataBuilder anotherSchema_zeRef = anotherType.createMetadata("ref")
						.defineReferencesTo(thirdType).setCacheIndex(true);
				MetadataBuilder anotherSchema_value = anotherType.createMetadata("stringValue").setType(STRING)
						.setMultivalue(true).setEssentialInSummary(inputMetadatasEssentialInSummary);
				thirdType.createMetadata("stringValuesUnion")
						.setType(STRING).setMultivalue(true).setEssentialInSummary(inputMetadatasEssentialInSummary)
						.defineDataEntry().asUnion(anotherSchema_zeRef, anotherSchema_stringValuesUnion, anotherSchema_value);

				zeType.setRecordCacheType(recordCacheType);
				anotherType.setRecordCacheType(recordCacheType);
				thirdType.setRecordCacheType(recordCacheType);
			}
		}));

		getDataLayerFactory().getDataLayerLogger().setPrintAllQueriesLongerThanMS(0).setQueryDebuggingMode(true);
		assertThat(getNetworkLinksOf(zeCollection)).containsOnly(
				tuple("group_default_ancestors", "group_default_parent", 0),
				tuple("group_default_ancestors", "group_default_ancestors", 0),
				tuple("anotherSchemaType_default_stringValuesUnion", "zeSchemaType_default_ref", 1),
				tuple("aThirdSchemaType_default_stringValuesUnion", "anotherSchemaType_default_ref", 1),
				tuple("aThirdSchemaType_default_stringValuesUnion", "anotherSchemaType_default_stringValuesUnion", 1),
				tuple("aThirdSchemaType_default_stringValuesUnion", "anotherSchemaType_default_stringValue", 1),
				tuple("anotherSchemaType_default_stringValuesUnion", "zeSchemaType_default_stringValue1", 1),
				tuple("anotherSchemaType_default_stringValuesUnion", "zeSchemaType_default_stringValue2", 1)
		);

		Transaction tx = new Transaction();

		tx.add(new TestRecord(anotherSchema, "merge1").set("ref", "merge3"));
		tx.add(new TestRecord(anotherSchema, "merge2").set("ref", "merge3"));
		tx.add(new TestRecord(thirdSchema, "merge3"));
		getModelLayerFactory().newRecordServices().execute(tx);
		Metadata anotherSchema_stringValuesUnion = anotherSchema.metadata("stringValuesUnion");
		Metadata thirdSchema_stringValuesUnion = thirdSchema.metadata("stringValuesUnion");

		TestRecord r1 = new TestRecord(zeSchema, "r1").set("stringValue1", "value1").set("ref", "merge1");
		TestRecord r2 = new TestRecord(zeSchema, "r2").set("stringValue2", asList("value2")).set("ref", "merge1");
		TestRecord r3 = new TestRecord(zeSchema, "r3").set("stringValue1", "value3").set("stringValue2", asList("value5"))
				.set("ref", "merge2");
		TestRecord r4 = new TestRecord(zeSchema, "r4").set("stringValue2", asList("value4")).set("ref", "merge2");
		TestRecord r5 = new TestRecord(zeSchema, "r5").set("stringValue1", "value4").set("ref", "merge2");
		getModelLayerFactory().newRecordServices().execute(new Transaction(r1, r2, r3, r4, r5));
		waitForBatchProcess();
		assertThat(record("merge1").getList(anotherSchema_stringValuesUnion)).containsOnly("value1", "value2");
		assertThat(record("merge2").getList(anotherSchema_stringValuesUnion))
				.containsOnly("value3", "value4", "value4", "value5");
		assertThat(record("merge3").getList(thirdSchema_stringValuesUnion))
				.containsOnly("value1", "value2", "value3", "value4", "value5");

		tx = new Transaction();
		tx.add(r1.set("stringValue1", "value1new"));
		tx.add(r3.set("ref", "merge1"));
		getModelLayerFactory().newRecordServices().execute(tx);
		waitForBatchProcess();
		assertThat(record("merge1").getList(anotherSchema_stringValuesUnion))
				.containsOnly("value1new", "value2", "value3", "value5");
		assertThat(record("merge2").getList(anotherSchema_stringValuesUnion)).containsOnly("value4");
		assertThat(record("merge3").getList(thirdSchema_stringValuesUnion))
				.containsOnly("value1new", "value2", "value3", "value4", "value5");

		BigVaultServer bigVaultServer = getDataLayerFactory().newRecordDao().getBigVaultServer();
		bigVaultServer.addAll(new BigVaultServerTransaction(NOW()).setUpdatedDocuments(asList(
				solrInputDocumentRemovingMetadatas("merge1", anotherSchema.metadata("stringValuesUnion")),
				solrInputDocumentRemovingMetadatas("merge2", anotherSchema.metadata("stringValuesUnion")),
				solrInputDocumentRemovingMetadatas("merge3", anotherSchema.metadata("stringValuesUnion"))
		)));
		System.out.println("---- ---- ----");
		int queries = clearAggregateMetadatasThenReindexReturningQtyOfQueriesOf(zeSchema, anotherSchema, thirdSchema);
		assertThat(record("merge1").getList(anotherSchema_stringValuesUnion))
				.containsOnly("value1new", "value2", "value3", "value5");
		assertThat(record("merge2").getList(anotherSchema_stringValuesUnion)).containsOnly("value4");
		assertThat(record("merge3").getList(thirdSchema_stringValuesUnion))
				.containsOnly("value1new", "value2", "value3", "value4", "value5");

		assertThat(queries).isEqualTo(recordCacheType == FULLY_CACHED ? 2 : 15);

	}

	//TODO Infinite test@Test
	public void whenCreatingRecordWithoutValueToAggregateSourceThenParentNotMarkedToReindex()
			throws Exception {

		givenBackgroundThreadsEnabled();
		defineSchemasManager().using(schemas.with(new MetadataSchemaTypesConfigurator() {
			@Override
			public void configure(MetadataSchemaTypesBuilder schemaTypes) {
				MetadataSchemaTypeBuilder zeType = schemaTypes.getSchemaType(zeSchema.typeCode());
				MetadataSchemaTypeBuilder anotherType = schemaTypes.getSchemaType(anotherSchema.typeCode());
				MetadataSchemaTypeBuilder thirdType = schemaTypes.getSchemaType(thirdSchema.typeCode());
				MetadataBuilder zeSchema_value1 = zeType.createMetadata("stringValue1")
						.setType(STRING).setEssentialInSummary(inputMetadatasEssentialInSummary);
				MetadataBuilder zeSchema_Value2 = zeType.createMetadata("stringValue2")
						.setType(STRING).setMultivalue(true).setEssentialInSummary(inputMetadatasEssentialInSummary);
				MetadataBuilder zeSchema_zeRef = zeType.createMetadata("ref").defineReferencesTo(anotherType);
				MetadataBuilder anotherSchema_stringValuesUnion = anotherType.createMetadata("stringValuesUnion").setType(STRING)
						.setMultivalue(true).defineDataEntry().asUnion(zeSchema_zeRef, zeSchema_value1, zeSchema_Value2);
				MetadataBuilder anotherSchema_zeRef = anotherType.createMetadata("ref").defineReferencesTo(thirdType);
				MetadataBuilder anotherSchema_value = anotherType.createMetadata("stringValue")
						.setType(STRING).setEssentialInSummary(inputMetadatasEssentialInSummary)
						.setMultivalue(true);
				thirdType.createMetadata("stringValuesUnion").setType(STRING).setMultivalue(true)
						.setEssentialInSummary(inputMetadatasEssentialInSummary)
						.defineDataEntry().asUnion(anotherSchema_zeRef, anotherSchema_stringValuesUnion, anotherSchema_value);

				zeType.setRecordCacheType(recordCacheType);
				anotherType.setRecordCacheType(recordCacheType);
				thirdType.setRecordCacheType(recordCacheType);
			}
		}));

		Transaction tx = new Transaction();

		tx.add(new TestRecord(anotherSchema, "merge1").set("ref", "merge3"));
		tx.add(new TestRecord(anotherSchema, "merge2").set("ref", "merge3"));
		tx.add(new TestRecord(thirdSchema, "merge3"));
		getModelLayerFactory().newRecordServices().execute(tx);
		Metadata anotherSchema_stringValuesUnion = anotherSchema.metadata("stringValuesUnion");
		Metadata thirdSchema_stringValuesUnion = thirdSchema.metadata("stringValuesUnion");

		//1. Creating records with empty values

		assertThat(record("merge1").<Boolean>get(Schemas.MARKED_FOR_REINDEXING)).isNull();
		assertThat(record("merge2").<Boolean>get(Schemas.MARKED_FOR_REINDEXING)).isNull();

		TestRecord r1 = new TestRecord(zeSchema, "r1").set("stringValue1", null).set("ref", "merge1");
		TestRecord r2 = new TestRecord(zeSchema, "r2").set("stringValue2", new ArrayList<>()).set("ref", "merge1");
		TestRecord r3 = new TestRecord(zeSchema, "r3").set("stringValue1", null).set("stringValue2", new ArrayList<>())
				.set("ref", "merge2");

		getModelLayerFactory().newRecordServices().execute(new Transaction(r1, r2, r3));

		assertThat(record("merge1").<Boolean>get(Schemas.MARKED_FOR_REINDEXING)).isNull();
		assertThat(record("merge2").<Boolean>get(Schemas.MARKED_FOR_REINDEXING)).isNull();
		waitForBatchProcess();
		assertThat(record("merge1").getList(anotherSchema_stringValuesUnion)).isEmpty();
		assertThat(record("merge2").getList(anotherSchema_stringValuesUnion)).isEmpty();

		//2. Changing empty values with null, which is equivalent

		r1.set("stringValue1", null).set("ref", "merge1");
		r2.set("stringValue2", null).set("ref", "merge1");
		r3.set("stringValue1", null).set("stringValue2", null).set("ref", "merge2");
		getModelLayerFactory().newRecordServices().execute(new Transaction(r1, r2, r3));

		assertThat(record("merge1").<Boolean>get(Schemas.MARKED_FOR_REINDEXING)).isNull();
		assertThat(record("merge2").<Boolean>get(Schemas.MARKED_FOR_REINDEXING)).isNull();
		waitForBatchProcess();
		assertThat(record("merge1").getList(anotherSchema_stringValuesUnion)).isEmpty();
		assertThat(record("merge2").getList(anotherSchema_stringValuesUnion)).isEmpty();

		//3. empty list and null values become not nulls

		r1.set("stringValue1", "pouet").set("ref", "merge1");
		r2.set("stringValue2", asList("test")).set("ref", "merge1");
		r3.set("stringValue1", "norris").set("stringValue2", asList("chuck"))
				.set("ref", "merge2");

		getModelLayerFactory().newRecordServices().execute(new Transaction(r1, r2, r3));

		assertThat(record("merge1").<Boolean>get(Schemas.MARKED_FOR_REINDEXING)).isTrue();
		assertThat(record("merge2").<Boolean>get(Schemas.MARKED_FOR_REINDEXING)).isTrue();
		waitForBatchProcess();
		assertThat(record("merge1").getList(anotherSchema_stringValuesUnion)).containsOnly("pouet", "test");
		assertThat(record("merge2").getList(anotherSchema_stringValuesUnion)).containsOnly("chuck", "norris");


		//4. not nulls are replaced with empty ones

		r1.set("stringValue1", null).set("ref", "merge1");
		r2.set("stringValue2", null).set("ref", "merge1");
		r3.set("stringValue1", null).set("stringValue2", new ArrayList<>())
				.set("ref", "merge2");

		getModelLayerFactory().newRecordServices().execute(new Transaction(r1, r2, r3));

		assertThat(record("merge1").<Boolean>get(Schemas.MARKED_FOR_REINDEXING)).isTrue();
		assertThat(record("merge2").<Boolean>get(Schemas.MARKED_FOR_REINDEXING)).isTrue();
		waitForBatchProcess();
		assertThat(record("merge1").getList(anotherSchema_stringValuesUnion)).isEmpty();
		assertThat(record("merge2").getList(anotherSchema_stringValuesUnion)).isEmpty();
	}

	//TODO Infinite test@Test
	public void givenRecordAndTheirUnionsCreatedInSameTransactionThenOk()
			throws Exception {

		givenBackgroundThreadsEnabled();
		defineSchemasManager().using(schemas.with(new MetadataSchemaTypesConfigurator() {
			@Override
			public void configure(MetadataSchemaTypesBuilder schemaTypes) {
				MetadataSchemaTypeBuilder zeType = schemaTypes.getSchemaType(zeSchema.typeCode());
				MetadataSchemaTypeBuilder anotherType = schemaTypes.getSchemaType(anotherSchema.typeCode());
				MetadataSchemaTypeBuilder thirdType = schemaTypes.getSchemaType(thirdSchema.typeCode());
				MetadataBuilder zeSchema_valueMetadata = zeType.getDefaultSchema().create("stringValue")
						.setType(STRING).setEssentialInSummary(inputMetadatasEssentialInSummary);
				MetadataBuilder zeSchema_zeRef = zeType.getDefaultSchema().create("ref").defineReferencesTo(anotherType);

				MetadataBuilder anotherSchema_stringValuesUnion = anotherType.getDefaultSchema().create("stringValuesUnion")
						.setType(STRING).setMultivalue(true).setEssentialInSummary(inputMetadatasEssentialInSummary)
						.defineDataEntry().asUnion(zeSchema_zeRef, zeSchema_valueMetadata);
				MetadataBuilder anotherSchema_zeRef = anotherType.getDefaultSchema().create("ref").defineReferencesTo(thirdType);
				MetadataBuilder thirdSchema_stringValuesUnion = thirdType.getDefaultSchema().create("stringValuesUnion")
						.setType(STRING).setMultivalue(true).setEssentialInSummary(inputMetadatasEssentialInSummary)
						.defineDataEntry().asUnion(anotherSchema_zeRef, anotherSchema_stringValuesUnion);

				zeType.setRecordCacheType(recordCacheType);
				anotherType.setRecordCacheType(recordCacheType);
				thirdType.setRecordCacheType(recordCacheType);
			}
		}));
		Metadata anotherSchema_stringValuesUnion = anotherSchema.metadata("stringValuesUnion");
		Metadata thirdSchema_stringValuesUnion = thirdSchema.metadata("stringValuesUnion");

		assertThat(getNetworkLinksOf(zeCollection)).containsOnly(
				tuple("group_default_ancestors", "group_default_parent", 0),
				tuple("group_default_ancestors", "group_default_ancestors", 0),
				tuple("anotherSchemaType_default_stringValuesUnion", "zeSchemaType_default_ref", 1),
				tuple("aThirdSchemaType_default_stringValuesUnion", "anotherSchemaType_default_ref", 1),
				tuple("aThirdSchemaType_default_stringValuesUnion", "anotherSchemaType_default_stringValuesUnion", 1),
				tuple("anotherSchemaType_default_stringValuesUnion", "zeSchemaType_default_stringValue", 1));

		Transaction tx = new Transaction();

		tx.add(new TestRecord(anotherSchema, "merge1").set("ref", "merge3"));
		tx.add(new TestRecord(anotherSchema, "merge2").set("ref", "merge3"));
		tx.add(new TestRecord(thirdSchema, "merge3"));

		TestRecord r1 = (TestRecord) tx.add(new TestRecord(zeSchema, "r1").set("stringValue", "value1").set("ref", "merge1"));
		TestRecord r2 = (TestRecord) tx.add(new TestRecord(zeSchema, "r2").set("stringValue", "value2").set("ref", "merge1"));
		TestRecord r3 = (TestRecord) tx.add(new TestRecord(zeSchema, "r3").set("stringValue", "value3").set("ref", "merge2"));
		TestRecord r4 = (TestRecord) tx.add(new TestRecord(zeSchema, "r4").set("stringValue", "value4").set("ref", "merge2"));
		TestRecord r5 = (TestRecord) tx.add(new TestRecord(zeSchema, "r5").set("stringValue", "value4").set("ref", "merge2"));
		getModelLayerFactory().newRecordServices().execute(tx);
		waitForBatchProcess();
		assertThat(record("merge1").getList(anotherSchema_stringValuesUnion)).containsOnly("value1", "value2");
		assertThat(record("merge2").getList(anotherSchema_stringValuesUnion)).containsOnly("value3", "value4");
		assertThat(record("merge3").getList(thirdSchema_stringValuesUnion))
				.containsOnly("value1", "value2", "value3", "value4");

	}

}
