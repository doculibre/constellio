package com.constellio.model.services.records;

import com.constellio.model.entities.records.Transaction;
import com.constellio.model.entities.schemas.Metadata;
import com.constellio.model.entities.schemas.MetadataNetworkLink;
import com.constellio.model.entities.schemas.MetadataValueType;
import com.constellio.model.services.records.aggregations.MaxMetadataAggregationHandler;
import com.constellio.model.services.records.aggregations.MinMetadataAggregationHandler;
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
import org.assertj.core.groups.Tuple;
import org.joda.time.LocalDate;
import org.joda.time.LocalDateTime;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

import static com.constellio.model.entities.schemas.MetadataValueType.DATE;
import static com.constellio.model.entities.schemas.MetadataValueType.DATE_TIME;
import static com.constellio.model.entities.schemas.MetadataValueType.NUMBER;
import static com.constellio.model.services.records.RecordServicesAgregatedMetadatasMechanicAcceptTest.clearAggregateMetadatasThenReindexReturningQtyOfQueriesOf;
import static java.util.Arrays.asList;
import static org.assertj.core.api.Assertions.assertThat;

public class RecordServicesAgregatedMinMaxMetadatasAcceptTest extends ConstellioTest {

	TestsSchemasSetup schemas = new TestsSchemasSetup(zeCollection);
	ZeSchemaMetadatas zeSchema = schemas.new ZeSchemaMetadatas();
	AnotherSchemaMetadatas anotherSchema = schemas.new AnotherSchemaMetadatas();
	ThirdSchemaMetadatas thirdSchema = schemas.new ThirdSchemaMetadatas();
	RecordServicesAgregatedMetadatasAcceptTestRecords records = new RecordServicesAgregatedMetadatasAcceptTestRecords();

	@Test
	public void givenMinOfMultiValueNumberMetadatasThenThenCalculatedAccurately()
			throws Exception {

		givenBackgroundThreadsEnabled();
		defineSchemasManager().using(schemas.with(new MetadataSchemaTypesConfigurator() {
			@Override
			public void configure(MetadataSchemaTypesBuilder schemaTypes) {
				MetadataSchemaTypeBuilder zeType = schemaTypes.getSchemaType(zeSchema.typeCode());
				MetadataSchemaTypeBuilder anotherType = schemaTypes.getSchemaType(anotherSchema.typeCode());
				MetadataSchemaTypeBuilder thirdType = schemaTypes.getSchemaType(thirdSchema.typeCode());
				MetadataBuilder zeSchema_valueMetadata = zeType.getDefaultSchema().create("numberValue").setType(NUMBER)
						.setMultivalue(true);
				MetadataBuilder zeSchema_zeRef = zeType.getDefaultSchema().create("ref").defineReferencesTo(anotherType);

				MetadataBuilder anotherSchema_minValue = anotherType.getDefaultSchema().create("minValue")
						.setType(NUMBER).defineDataEntry().asMin(zeSchema_zeRef, zeSchema_valueMetadata);
				MetadataBuilder anotherSchema_maxValue = anotherType.getDefaultSchema().create("maxValue")
						.setType(NUMBER).defineDataEntry().asMax(zeSchema_zeRef, zeSchema_valueMetadata);
				MetadataBuilder anotherSchema_zeRef = anotherType.getDefaultSchema().create("ref").defineReferencesTo(thirdType);
				MetadataBuilder thirdSchema_minValue = thirdType.getDefaultSchema().create("minValue")
						.setType(NUMBER).defineDataEntry().asMin(anotherSchema_zeRef, anotherSchema_minValue);
				MetadataBuilder thirdSchema_maxValue = thirdType.getDefaultSchema().create("maxValue")
						.setType(NUMBER).defineDataEntry().asMax(anotherSchema_zeRef, anotherSchema_maxValue);

			}
		}));
		Metadata zeSchema_numberValue = zeSchema.metadata("numberValue");
		Metadata anotherSchema_minValue = anotherSchema.metadata("minValue");
		Metadata thirdSchema_minValue = thirdSchema.metadata("minValue");
		Metadata anotherSchema_maxValue = anotherSchema.metadata("maxValue");
		Metadata thirdSchema_maxValue = thirdSchema.metadata("maxValue");

		Transaction tx = new Transaction();

		tx.add(new TestRecord(anotherSchema, "merge1").set("ref", "merge3"));
		tx.add(new TestRecord(anotherSchema, "merge2").set("ref", "merge3"));
		tx.add(new TestRecord(thirdSchema, "merge3"));
		getModelLayerFactory().newRecordServices().execute(tx);

		tx = new Transaction();
		TestRecord r1 = new TestRecord(zeSchema, "r1").set("numberValue", asList(0.7, 1.4)).set("ref", "merge1");
		TestRecord r2 = new TestRecord(zeSchema, "r2").set("numberValue", asList(0.9, 0.5)).set("ref", "merge1");
		TestRecord r3 = new TestRecord(zeSchema, "r3").set("numberValue", asList(0.1, 1.6)).set("ref", "merge2");
		TestRecord r4 = new TestRecord(zeSchema, "r4").set("numberValue", asList(0.2, 1.5)).set("ref", "merge2");
		TestRecord r5 = new TestRecord(zeSchema, "r5").set("numberValue", asList(0.3, 1.4)).set("ref", "merge2");
		getModelLayerFactory().newRecordServices().execute(tx.addAll(r1, r2, r3, r4, r5));
		waitForBatchProcess();
		assertThat(record("merge1").<Double>get(anotherSchema_minValue)).isEqualTo(0.5);
		assertThat(record("merge2").<Double>get(anotherSchema_minValue)).isEqualTo(0.1);
		assertThat(record("merge3").<Double>get(thirdSchema_minValue)).isEqualTo(0.1);
		assertThat(record("merge1").<Double>get(anotherSchema_maxValue)).isEqualTo(1.4);
		assertThat(record("merge2").<Double>get(anotherSchema_maxValue)).isEqualTo(1.6);
		assertThat(record("merge3").<Double>get(thirdSchema_maxValue)).isEqualTo(1.6);

		tx = new Transaction();
		tx.add(r1.set("numberValue", asList(0.7, 0.05)));
		tx.add(r3.set("ref", "merge1"));
		getModelLayerFactory().newRecordServices().execute(tx);
		waitForBatchProcess();
		assertThat(record("merge1").<Double>get(anotherSchema_minValue)).isEqualTo(0.05);
		assertThat(record("merge2").<Double>get(anotherSchema_minValue)).isEqualTo(0.2);
		assertThat(record("merge3").<Double>get(thirdSchema_minValue)).isEqualTo(0.05);
		assertThat(record("merge1").<Double>get(anotherSchema_maxValue)).isEqualTo(1.6);
		assertThat(record("merge2").<Double>get(anotherSchema_maxValue)).isEqualTo(1.5);
		assertThat(record("merge3").<Double>get(thirdSchema_maxValue)).isEqualTo(1.6);

		int nbQueries = clearAggregateMetadatasThenReindexReturningQtyOfQueriesOf(zeSchema, anotherSchema, thirdSchema);
		assertThat(record("merge1").<Double>get(anotherSchema_minValue)).isEqualTo(0.05);
		assertThat(record("merge2").<Double>get(anotherSchema_minValue)).isEqualTo(0.2);
		assertThat(record("merge3").<Double>get(thirdSchema_minValue)).isEqualTo(0.05);
		assertThat(record("merge1").<Double>get(anotherSchema_maxValue)).isEqualTo(1.6);
		assertThat(record("merge2").<Double>get(anotherSchema_maxValue)).isEqualTo(1.5);
		assertThat(record("merge3").<Double>get(thirdSchema_maxValue)).isEqualTo(1.6);
		assertThat(nbQueries).isEqualTo(8);
	}

	@Test
	public void givenMinMaxOfMultiValueLocalDateMetadatasThenAllNumbersAreCopiedWithoutDuplicates()
			throws Exception {

		givenBackgroundThreadsEnabled();
		defineSchemasManager().using(schemas.with(new MetadataSchemaTypesConfigurator() {
			@Override
			public void configure(MetadataSchemaTypesBuilder schemaTypes) {
				MetadataSchemaTypeBuilder zeType = schemaTypes.getSchemaType(zeSchema.typeCode());
				MetadataSchemaTypeBuilder anotherType = schemaTypes.getSchemaType(anotherSchema.typeCode());
				MetadataSchemaTypeBuilder thirdType = schemaTypes.getSchemaType(thirdSchema.typeCode());
				MetadataBuilder zeSchema_valueMetadata = zeType.getDefaultSchema().create("dateValue").setType(DATE)
						.setMultivalue(true);
				MetadataBuilder zeSchema_zeRef = zeType.getDefaultSchema().create("ref").defineReferencesTo(anotherType);

				MetadataBuilder anotherSchema_minValue = anotherType.getDefaultSchema().create("minValue")
						.setType(DATE_TIME).defineDataEntry().asMin(zeSchema_zeRef, zeSchema_valueMetadata);
				MetadataBuilder anotherSchema_maxValue = anotherType.getDefaultSchema().create("maxValue")
						.setType(DATE).defineDataEntry().asMax(zeSchema_zeRef, zeSchema_valueMetadata);
				MetadataBuilder anotherSchema_zeRef = anotherType.getDefaultSchema().create("ref").defineReferencesTo(thirdType);
				MetadataBuilder thirdSchema_minValue = thirdType.getDefaultSchema().create("minValue")
						.setType(DATE).defineDataEntry().asMin(anotherSchema_zeRef, anotherSchema_minValue);
				MetadataBuilder thirdSchema_maxValue = thirdType.getDefaultSchema().create("maxValue")
						.setType(DATE_TIME).defineDataEntry().asMax(anotherSchema_zeRef, anotherSchema_maxValue);

			}
		}));
		Metadata anotherSchema_minValue = anotherSchema.metadata("minValue");
		Metadata thirdSchema_minValue = thirdSchema.metadata("minValue");
		Metadata anotherSchema_maxValue = anotherSchema.metadata("maxValue");
		Metadata thirdSchema_maxValue = thirdSchema.metadata("maxValue");

		Transaction tx = new Transaction();

		tx.add(new TestRecord(anotherSchema, "merge1").set("ref", "merge3"));
		tx.add(new TestRecord(anotherSchema, "merge2").set("ref", "merge3"));
		tx.add(new TestRecord(thirdSchema, "merge3"));
		getModelLayerFactory().newRecordServices().execute(tx);

		tx = new Transaction();
		TestRecord r1 = new TestRecord(zeSchema, "r1")
				.set("dateValue", asList(date(2010, 8, 1), date(2011, 5, 1))).set("ref", "merge1");
		TestRecord r2 = new TestRecord(zeSchema, "r2")
				.set("dateValue", asList(date(2010, 10, 1), date(2010, 6, 1))).set("ref", "merge1");
		TestRecord r3 = new TestRecord(zeSchema, "r3")
				.set("dateValue", asList(date(2010, 2, 1), date(2011, 7, 1))).set("ref", "merge2");
		TestRecord r4 = new TestRecord(zeSchema, "r4")
				.set("dateValue", asList(date(2010, 3, 1), date(2011, 6, 1))).set("ref", "merge2");
		TestRecord r5 = new TestRecord(zeSchema, "r5")
				.set("dateValue", asList(date(2010, 4, 1), date(2011, 5, 1))).set("ref", "merge2");
		getModelLayerFactory().newRecordServices().execute(tx.addAll(r1, r2, r3, r4, r5));
		waitForBatchProcess();
		assertThat(record("merge1").<LocalDateTime>get(anotherSchema_minValue)).isEqualTo(dateTime(2010, 6, 1, 0, 0, 0));
		assertThat(record("merge2").<LocalDateTime>get(anotherSchema_minValue)).isEqualTo(dateTime(2010, 2, 1, 0, 0, 0));
		assertThat(record("merge3").<LocalDate>get(thirdSchema_minValue)).isEqualTo(date(2010, 2, 1));

		tx = new Transaction();
		tx.add(r1.set("dateValue", asList(date(2010, 8, 1), date(2010, 1, 6))));
		tx.add(r3.set("ref", "merge1"));
		getModelLayerFactory().newRecordServices().execute(tx);
		waitForBatchProcess();
		assertThat(record("merge1").<LocalDateTime>get(anotherSchema_minValue)).isEqualTo(dateTime(2010, 1, 6, 0, 0, 0));
		assertThat(record("merge2").<LocalDateTime>get(anotherSchema_minValue)).isEqualTo(dateTime(2010, 3, 1, 0, 0, 0));
		assertThat(record("merge3").<LocalDate>get(thirdSchema_minValue)).isEqualTo(date(2010, 1, 6));
		assertThat(record("merge1").<LocalDate>get(anotherSchema_maxValue)).isEqualTo(date(2011, 7, 1));
		assertThat(record("merge2").<LocalDate>get(anotherSchema_maxValue)).isEqualTo(date(2011, 6, 1));
		assertThat(record("merge3").<LocalDateTime>get(thirdSchema_maxValue)).isEqualTo(dateTime(2011, 7, 1, 0, 0, 0));

		int nbQueries = clearAggregateMetadatasThenReindexReturningQtyOfQueriesOf(zeSchema, anotherSchema, thirdSchema);
		assertThat(record("merge1").<LocalDateTime>get(anotherSchema_minValue)).isEqualTo(dateTime(2010, 1, 6, 0, 0, 0));
		assertThat(record("merge2").<LocalDateTime>get(anotherSchema_minValue)).isEqualTo(dateTime(2010, 3, 1, 0, 0, 0));
		assertThat(record("merge3").<LocalDate>get(thirdSchema_minValue)).isEqualTo(date(2010, 1, 6));
		assertThat(record("merge1").<LocalDate>get(anotherSchema_maxValue)).isEqualTo(date(2011, 7, 1));
		assertThat(record("merge2").<LocalDate>get(anotherSchema_maxValue)).isEqualTo(date(2011, 6, 1));
		assertThat(record("merge3").<LocalDateTime>get(thirdSchema_maxValue)).isEqualTo(dateTime(2011, 7, 1, 0, 0, 0));
		assertThat(nbQueries).isEqualTo(8);
	}

	@Test
	public void whenComparingValuesOfMultipleTypesThenRetrieveMinAndMax()
			throws Exception {

		LocalDate localDate1 = date(2011, 1, 1);
		LocalDate localDate2 = date(2012, 1, 1);
		LocalDateTime localDateTime1 = dateTime(2013, 1, 1, 2, 3, 4);
		LocalDateTime localDateTime2 = dateTime(2014, 1, 1, 2, 3, 4);
		List<Object> values = asList((Object) localDate1, localDate2, localDateTime1, localDateTime2);

		assertThat(MinMetadataAggregationHandler.getMin(MetadataValueType.DATE, values)).isEqualTo(date(2011, 1, 1));
		assertThat(MaxMetadataAggregationHandler.getMax(MetadataValueType.DATE, values)).isEqualTo(date(2014, 1, 1));
		assertThat(MinMetadataAggregationHandler.getMin(MetadataValueType.DATE_TIME, values))
				.isEqualTo(dateTime(2011, 1, 1, 0, 0, 0));
		assertThat(MaxMetadataAggregationHandler.getMax(MetadataValueType.DATE_TIME, values))
				.isEqualTo(dateTime(2014, 1, 1, 2, 3, 4));

		localDateTime1 = dateTime(2011, 1, 1, 2, 3, 4);
		localDateTime2 = dateTime(2012, 1, 1, 2, 3, 4);
		localDate1 = date(2013, 1, 1);
		localDate2 = date(2014, 1, 1);
		values = asList((Object) localDate1, localDate2, localDateTime1, localDateTime2);

		assertThat(MinMetadataAggregationHandler.getMin(MetadataValueType.DATE, values)).isEqualTo(date(2011, 1, 1));
		assertThat(MaxMetadataAggregationHandler.getMax(MetadataValueType.DATE, values)).isEqualTo(date(2014, 1, 1));
		assertThat(MinMetadataAggregationHandler.getMin(MetadataValueType.DATE_TIME, values))
				.isEqualTo(dateTime(2011, 1, 1, 2, 3, 4));
		assertThat(MaxMetadataAggregationHandler.getMax(MetadataValueType.DATE_TIME, values))
				.isEqualTo(dateTime(2014, 1, 1, 0, 0, 0));

		values = asList((Object) 1.2, 1, 4.0, 4.4);
		assertThat(MinMetadataAggregationHandler.getMin(MetadataValueType.NUMBER, values)).isEqualTo(1.0);
		assertThat(MaxMetadataAggregationHandler.getMax(MetadataValueType.NUMBER, values)).isEqualTo(4.4);

	}

	private List<Tuple> getNetworkLinks() {

		List<Tuple> tuples = new ArrayList();

		for (MetadataNetworkLink link : schemas.getTypes().getMetadataNetwork().getLinks()) {

			if (!link.getToMetadata().isGlobal()
				&& !link.getFromMetadata().isGlobal()
				&& !link.getFromMetadata().getCode().startsWith("user_")
				&& !link.getFromMetadata().getCode().startsWith("user_")
				&& !link.getFromMetadata().getCode().startsWith("temporaryRecord_")) {
				Tuple tuple = new Tuple();
				tuple.addData(link.getFromMetadata().getCode());
				tuple.addData(link.getToMetadata().getCode());
				tuple.addData(link.getLevel());
				tuples.add(tuple);
			}
		}

		return tuples;
	}
}
