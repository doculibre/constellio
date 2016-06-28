package com.constellio.model.entities.calculators;

import static com.constellio.sdk.tests.schemas.TestsSchemasSetup.whichAllowsAnotherDefaultSchema;
import static com.constellio.sdk.tests.schemas.TestsSchemasSetup.whichIsCalculatedUsingPattern;
import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;

import org.junit.Test;

import com.constellio.model.entities.calculators.dependencies.Dependency;
import com.constellio.model.entities.calculators.dependencies.LocalDependency;
import com.constellio.model.entities.calculators.dependencies.ReferenceDependency;
import com.constellio.model.entities.records.Record;
import com.constellio.model.entities.records.Transaction;
import com.constellio.model.entities.schemas.entries.CalculatedDataEntry;
import com.constellio.sdk.tests.ConstellioTest;
import com.constellio.sdk.tests.TestRecord;
import com.constellio.sdk.tests.schemas.TestsSchemasSetup;
import com.constellio.sdk.tests.schemas.TestsSchemasSetup.AnotherSchemaMetadatas;
import com.constellio.sdk.tests.schemas.TestsSchemasSetup.ZeSchemaMetadatas;

public class StringPatternMetadataValueCalculatorAcceptanceTest extends ConstellioTest {

	TestsSchemasSetup setup = new TestsSchemasSetup();
	ZeSchemaMetadatas zeSchema = setup.new ZeSchemaMetadatas();
	AnotherSchemaMetadatas anotherSchema = setup.new AnotherSchemaMetadatas();

	@Test
	public void givenStringCalculatedFromPatternThenHasValidCalculatorDependencies()
			throws Exception {

		String pattern = "Prefixe {referenceMetadata.stringMetadata} - {stringMetadata} Suffixe";
		defineSchemasManager().using(setup
				.withAStringMetadata()
				.withAReferenceMetadata(whichAllowsAnotherDefaultSchema)
				.withAnotherSchemaStringMetadata()
				.withAnotherStringMetadata(whichIsCalculatedUsingPattern(pattern)));

		MetadataValueCalculator<?> calculator = ((CalculatedDataEntry) zeSchema.anotherStringMetadata().getDataEntry())
				.getCalculator();

		assertThat((List<Dependency>) calculator.getDependencies()).containsOnly(
				LocalDependency.toAString("stringMetadata").whichIsRequired(),
				ReferenceDependency.toAString("referenceMetadata", "stringMetadata").whichIsRequired()
		);

	}

	@Test
	public void givenStringCalculatedFromPatternThenOnlyCalculatedIfEveryDependenciesNotNull()
			throws Exception {

		String pattern = "Prefixe {referenceMetadata.stringMetadata} - {stringMetadata} Suffixe";
		defineSchemasManager().using(setup
				.withAStringMetadata()
				.withAReferenceMetadata(whichAllowsAnotherDefaultSchema)
				.withAnotherSchemaStringMetadata()
				.withAnotherStringMetadata(whichIsCalculatedUsingPattern(pattern)));

		Transaction transaction = new Transaction();
		Record zeSchemaRecord = transaction.add(new TestRecord(zeSchema, "record1")
				.set(zeSchema.stringMetadata(), "666")
				.set(zeSchema.referenceMetadata(), "anotherSchemaRecord"));

		Record zeSchemaRecordWithoutReferencedStringMetadata = transaction.add(new TestRecord(zeSchema, "record2")
				.set(zeSchema.stringMetadata(), "zeValue")
				.set(zeSchema.referenceMetadata(), "anotherSchemaRecordWithoutStringMetadata"));

		Record zeSchemaRecordWithoutStringMetadata = transaction.add(new TestRecord(zeSchema, "record3")
				.set(zeSchema.referenceMetadata(), "anotherSchemaRecord"));

		Record zeSchemaRecordWithoutReference = transaction.add(new TestRecord(zeSchema, "record4")
				.set(zeSchema.stringMetadata(), "42"));

		transaction.add(new TestRecord(anotherSchema, "anotherSchemaRecord")
				.set(anotherSchema.stringMetadata(), "zeValue"));

		transaction.add(new TestRecord(anotherSchema, "anotherSchemaRecordWithoutStringMetadata"));

		getModelLayerFactory().newRecordServices().execute(transaction);

		assertThat(zeSchemaRecord.get(zeSchema.anotherStringMetadata())).isEqualTo("Prefixe 42 - 666 Suffixe");
		assertThat(zeSchemaRecordWithoutReferencedStringMetadata.get(zeSchema.anotherStringMetadata())).isNull();
		assertThat(zeSchemaRecordWithoutStringMetadata.get(zeSchema.anotherStringMetadata())).isNull();
		assertThat(zeSchemaRecordWithoutReference.get(zeSchema.anotherStringMetadata())).isNull();
	}
}
