package com.constellio.model.entities.calculators;

import com.constellio.model.entities.calculators.dependencies.Dependency;
import com.constellio.model.entities.calculators.dependencies.LocalDependency;
import com.constellio.model.entities.calculators.dependencies.ReferenceDependency;
import com.constellio.model.entities.records.Record;
import com.constellio.model.entities.records.Transaction;
import com.constellio.model.entities.schemas.entries.CalculatedDataEntry;
import com.constellio.sdk.tests.ConstellioTest;
import com.constellio.sdk.tests.TestRecord;
import com.constellio.sdk.tests.schemas.TestsSchemasSetup;
import com.constellio.sdk.tests.schemas.TestsSchemasSetup.*;
import org.junit.Test;

import java.util.List;

import static com.constellio.sdk.tests.schemas.TestsSchemasSetup.*;
import static java.util.Arrays.asList;
import static org.assertj.core.api.Assertions.assertThat;

public class JEXLMetadataValueCalculatorAcceptanceTest extends ConstellioTest {

	TestsSchemasSetup setup = new TestsSchemasSetup();
	ZeSchemaMetadatas zeSchema = setup.new ZeSchemaMetadatas();
	AnotherSchemaMetadatas anotherSchema = setup.new AnotherSchemaMetadatas();

	@Test
	public void givenStringCalculatedFromPatternThenHasValidCalculatorDependencies()
			throws Exception {

		String pattern = "'Prefixe' + referenceMetadata.stringMetadata + ' - ' + stringMetadata + ' Suffixe'";
		defineSchemasManager().using(setup
				.withAStringMetadata()
				.withAReferenceMetadata(whichAllowsAnotherDefaultSchema)
				.withAnotherSchemaStringMetadata()
				.withAnotherStringMetadata(whichIsCalculatedUsingPattern(pattern)));

		MetadataValueCalculator<?> calculator = ((CalculatedDataEntry) zeSchema.anotherStringMetadata().getDataEntry())
				.getCalculator();

		assertThat((List<Dependency>) calculator.getDependencies()).containsOnly(
				LocalDependency.toAString("stringMetadata"),
				LocalDependency.toAReference("referenceMetadata"),
				ReferenceDependency.toAString("referenceMetadata", "stringMetadata")
		);

	}

	@Test
	public void givenStringCalculatedFromPatternUsingMultivalueReferenceAndStringLocalThenHasValidCalculatorDependencies()
			throws Exception {

		String pattern = "'Prefixe ' + referenceMetadata.stringMetadata + ' - ' + referenceMetadata.stringMetadata[0] + stringMetadata + ' Suffixe'";
		defineSchemasManager().using(setup
				.withAStringMetadata()
				.withAReferenceMetadata(whichAllowsAnotherDefaultSchema, whichIsMultivalue)
				.withAnotherSchemaStringMetadata()
				.withAnotherStringMetadata(whichIsCalculatedUsingPattern(pattern)));

		MetadataValueCalculator<?> calculator = ((CalculatedDataEntry) zeSchema.anotherStringMetadata().getDataEntry())
				.getCalculator();

		assertThat((List<Dependency>) calculator.getDependencies()).containsOnly(
				LocalDependency.toAString("stringMetadata"),
				LocalDependency.toAReference("referenceMetadata").whichIsMultivalue(),
				ReferenceDependency.toAString("referenceMetadata", "stringMetadata").whichIsMultivalue()
		);

	}

	@Test
	public void givenStringCalculatedFromPatternUsingMultivalueReferenceThenHasValidCalculatorDependencies()
			throws Exception {

		String pattern = "'Prefixe ' + referenceMetadata.stringMetadata + ' - ' + referenceMetadata.stringMetadata[0] + ' Suffixe'";
		defineSchemasManager().using(setup
				.withAStringMetadata()
				.withAReferenceMetadata(whichAllowsAnotherDefaultSchema, whichIsMultivalue)
				.withAnotherSchemaStringMetadata()
				.withAnotherStringMetadata(whichIsCalculatedUsingPattern(pattern)));

		MetadataValueCalculator<?> calculator = ((CalculatedDataEntry) zeSchema.anotherStringMetadata().getDataEntry())
				.getCalculator();

		assertThat((List<Dependency>) calculator.getDependencies()).containsOnly(
				LocalDependency.toAReference("referenceMetadata").whichIsMultivalue(),
				ReferenceDependency.toAString("referenceMetadata", "stringMetadata").whichIsMultivalue()
		);

	}

	@Test
	public void givenStringCalculatedFromPatternThenOnlyCalculatedIfEveryDependenciesNotNull()
			throws Exception {

		String pattern = "'Prefixe ' + referenceMetadata.stringMetadata + ' - ' + stringMetadata + ' Suffixe'";
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

		assertThat(zeSchemaRecord.<String>get(zeSchema.anotherStringMetadata())).isEqualTo("Prefixe zeValue - 666 Suffixe");
		assertThat(zeSchemaRecordWithoutReferencedStringMetadata.<String>get(zeSchema.anotherStringMetadata()))
				.isEqualTo("Prefixe  - zeValue Suffixe");
		assertThat(zeSchemaRecordWithoutStringMetadata.<String>get(zeSchema.anotherStringMetadata()))
				.isEqualTo("Prefixe zeValue -  Suffixe");
		assertThat(zeSchemaRecordWithoutReference.<String>get(zeSchema.anotherStringMetadata())).isEqualTo("Prefixe  - 42 Suffixe");
	}

	@Test
	public void givenMultivalueReferencedDependenciesThenCalculated()
			throws Exception {

		String pattern = "'Prefixe ' + referenceMetadata.stringMetadata + ' - ' + referenceMetadata.stringMetadata[0] +' Suffixe'";
		defineSchemasManager().using(setup
				.withAStringMetadata()
				.withAReferenceMetadata(whichAllowsAnotherDefaultSchema, whichIsMultivalue)
				.withAnotherSchemaStringMetadata()
				.withAnotherStringMetadata(whichIsCalculatedUsingPattern(pattern)));

		Transaction transaction = new Transaction();
		Record zeSchemaRecordWithOneReference = transaction.add(new TestRecord(zeSchema, "record1")
				.set(zeSchema.stringMetadata(), "666")
				.set(zeSchema.referenceMetadata(), asList("anotherSchemaRecord1")));

		Record zeSchemaRecordWithTwoReference = transaction.add(new TestRecord(zeSchema, "record2")
				.set(zeSchema.stringMetadata(), "666")
				.set(zeSchema.referenceMetadata(), asList("anotherSchemaRecord1", "anotherSchemaRecord2")));

		Record zeSchemaRecordWithoutReferencedStringMetadata = transaction.add(new TestRecord(zeSchema, "record3")
				.set(zeSchema.stringMetadata(), "zeValue")
				.set(zeSchema.referenceMetadata(), asList("anotherSchemaRecordWithoutStringMetadata")));

		Record zeSchemaRecordWithoutStringMetadata = transaction.add(new TestRecord(zeSchema, "record4")
				.set(zeSchema.referenceMetadata(), asList("anotherSchemaRecord1")));

		Record zeSchemaRecordWithoutReference = transaction.add(new TestRecord(zeSchema, "record5")
				.set(zeSchema.stringMetadata(), "42"));

		transaction.add(new TestRecord(anotherSchema, "anotherSchemaRecord1")
				.set(anotherSchema.stringMetadata(), "value1"));

		transaction.add(new TestRecord(anotherSchema, "anotherSchemaRecord2")
				.set(anotherSchema.stringMetadata(), "value2"));

		transaction.add(new TestRecord(anotherSchema, "anotherSchemaRecordWithoutStringMetadata"));

		getModelLayerFactory().newRecordServices().execute(transaction);

		assertThat(zeSchemaRecordWithOneReference.<String>get(zeSchema.anotherStringMetadata()))
				.isEqualTo("Prefixe [value1] - value1 Suffixe");
		assertThat(zeSchemaRecordWithTwoReference.<String>get(zeSchema.anotherStringMetadata()))
				.isEqualTo("Prefixe [value1, value2] - value1 Suffixe");
		assertThat(zeSchemaRecordWithoutReferencedStringMetadata.<String>get(zeSchema.anotherStringMetadata()))
				.isEqualTo("Prefixe [null] -  Suffixe");
		assertThat(zeSchemaRecordWithoutStringMetadata.<String>get(zeSchema.anotherStringMetadata()))
				.isEqualTo("Prefixe [value1] - value1 Suffixe");
		assertThat(zeSchemaRecordWithoutReference.<String>get(zeSchema.anotherStringMetadata())).isEqualTo("Prefixe [] -  Suffixe");
	}

	@Test
	public void givenMultivalueReferencedDependenciesWhenUsingFirstOrEmptyThenCalculated()
			throws Exception {

		String pattern = "'Prefixe ' + referenceMetadata.stringMetadata + ' - ' + utils.firstOrEmptyString(referenceMetadata.stringMetadata) +' Suffixe'";
		defineSchemasManager().using(setup
				.withAStringMetadata()
				.withAReferenceMetadata(whichAllowsAnotherDefaultSchema, whichIsMultivalue)
				.withAnotherSchemaStringMetadata()
				.withAnotherStringMetadata(whichIsCalculatedUsingPattern(pattern)));

		Transaction transaction = new Transaction();
		Record zeSchemaRecordWithOneReference = transaction.add(new TestRecord(zeSchema, "record1")
				.set(zeSchema.stringMetadata(), "666")
				.set(zeSchema.referenceMetadata(), asList("anotherSchemaRecord1")));

		Record zeSchemaRecordWithTwoReference = transaction.add(new TestRecord(zeSchema, "record2")
				.set(zeSchema.stringMetadata(), "666")
				.set(zeSchema.referenceMetadata(), asList("anotherSchemaRecord1", "anotherSchemaRecord2")));

		Record zeSchemaRecordWithoutReferencedStringMetadata = transaction.add(new TestRecord(zeSchema, "record3")
				.set(zeSchema.stringMetadata(), "zeValue")
				.set(zeSchema.referenceMetadata(), asList("anotherSchemaRecordWithoutStringMetadata")));

		Record zeSchemaRecordWithoutStringMetadata = transaction.add(new TestRecord(zeSchema, "record4")
				.set(zeSchema.referenceMetadata(), asList("anotherSchemaRecord1")));

		Record zeSchemaRecordWithoutReference = transaction.add(new TestRecord(zeSchema, "record5")
				.set(zeSchema.stringMetadata(), "42"));

		transaction.add(new TestRecord(anotherSchema, "anotherSchemaRecord1")
				.set(anotherSchema.stringMetadata(), "value1"));

		transaction.add(new TestRecord(anotherSchema, "anotherSchemaRecord2")
				.set(anotherSchema.stringMetadata(), "value2"));

		transaction.add(new TestRecord(anotherSchema, "anotherSchemaRecordWithoutStringMetadata"));

		getModelLayerFactory().newRecordServices().execute(transaction);

		assertThat(zeSchemaRecordWithOneReference.<String>get(zeSchema.anotherStringMetadata()))
				.isEqualTo("Prefixe [value1] - value1 Suffixe");
		assertThat(zeSchemaRecordWithTwoReference.<String>get(zeSchema.anotherStringMetadata()))
				.isEqualTo("Prefixe [value1, value2] - value1 Suffixe");
		assertThat(zeSchemaRecordWithoutReferencedStringMetadata.<String>get(zeSchema.anotherStringMetadata()))
				.isEqualTo("Prefixe [null] -  Suffixe");
		assertThat(zeSchemaRecordWithoutStringMetadata.<String>get(zeSchema.anotherStringMetadata()))
				.isEqualTo("Prefixe [value1] - value1 Suffixe");
		assertThat(zeSchemaRecordWithoutReference.<String>get(zeSchema.anotherStringMetadata())).isEqualTo("Prefixe [] -  Suffixe");
	}

	@Test
	public void givenStrictMultivalueReferencedDependenciesWhenUsingFirstOrEmptyThenCalculated()
			throws Exception {

		String pattern = "#STRICT:'Prefixe ' + referenceMetadata.stringMetadata + ' - ' + utils.firstOrEmptyString(referenceMetadata.stringMetadata) +' Suffixe'";
		defineSchemasManager().using(setup
				.withAStringMetadata()
				.withAReferenceMetadata(whichAllowsAnotherDefaultSchema, whichIsMultivalue)
				.withAnotherSchemaStringMetadata()
				.withAnotherStringMetadata(whichIsCalculatedUsingPattern(pattern)));

		Transaction transaction = new Transaction();
		Record zeSchemaRecordWithOneReference = transaction.add(new TestRecord(zeSchema, "record1")
				.set(zeSchema.stringMetadata(), "666")
				.set(zeSchema.referenceMetadata(), asList("anotherSchemaRecord1")));

		Record zeSchemaRecordWithTwoReference = transaction.add(new TestRecord(zeSchema, "record2")
				.set(zeSchema.stringMetadata(), "666")
				.set(zeSchema.referenceMetadata(), asList("anotherSchemaRecord1", "anotherSchemaRecord2")));

		Record zeSchemaRecordWithoutReferencedStringMetadata = transaction.add(new TestRecord(zeSchema, "record3")
				.set(zeSchema.stringMetadata(), "zeValue")
				.set(zeSchema.referenceMetadata(), asList("anotherSchemaRecordWithoutStringMetadata")));

		Record zeSchemaRecordWithoutReference = transaction.add(new TestRecord(zeSchema, "record5")
				.set(zeSchema.stringMetadata(), "42"));

		transaction.add(new TestRecord(anotherSchema, "anotherSchemaRecord1")
				.set(anotherSchema.stringMetadata(), "value1"));

		transaction.add(new TestRecord(anotherSchema, "anotherSchemaRecord2")
				.set(anotherSchema.stringMetadata(), "value2"));

		transaction.add(new TestRecord(anotherSchema, "anotherSchemaRecordWithoutStringMetadata"));

		getModelLayerFactory().newRecordServices().execute(transaction);

		assertThat(zeSchemaRecordWithOneReference.<String>get(zeSchema.anotherStringMetadata()))
				.isEqualTo("Prefixe [value1] - value1 Suffixe");
		assertThat(zeSchemaRecordWithTwoReference.<String>get(zeSchema.anotherStringMetadata()))
				.isEqualTo("Prefixe [value1, value2] - value1 Suffixe");
		assertThat(zeSchemaRecordWithoutReferencedStringMetadata.<String>get(zeSchema.anotherStringMetadata())).isNull();
		assertThat(zeSchemaRecordWithoutReference.<String>get(zeSchema.anotherStringMetadata())).isEqualTo("Prefixe [] -  Suffixe");
	}

	@Test
	public void givenMultivalueReferencedDependenciesAndLocalDependencyThenCalculated()
			throws Exception {

		String pattern = "'Prefixe ' + referenceMetadata.stringMetadata + ' - ' + stringMetadata + ' Suffixe'";
		defineSchemasManager().using(setup
				.withAStringMetadata()
				.withAReferenceMetadata(whichAllowsAnotherDefaultSchema, whichIsMultivalue)
				.withAnotherSchemaStringMetadata()
				.withAnotherStringMetadata(whichIsCalculatedUsingPattern(pattern)));

		Transaction transaction = new Transaction();
		Record zeSchemaRecordWithOneReference = transaction.add(new TestRecord(zeSchema, "record1")
				.set(zeSchema.stringMetadata(), "666")
				.set(zeSchema.referenceMetadata(), asList("anotherSchemaRecord1")));

		Record zeSchemaRecordWithTwoReference = transaction.add(new TestRecord(zeSchema, "record2")
				.set(zeSchema.stringMetadata(), "666")
				.set(zeSchema.referenceMetadata(), asList("anotherSchemaRecord1", "anotherSchemaRecord2")));

		Record zeSchemaRecordWithoutReferencedStringMetadata = transaction.add(new TestRecord(zeSchema, "record3")
				.set(zeSchema.stringMetadata(), "zeValue")
				.set(zeSchema.referenceMetadata(), asList("anotherSchemaRecordWithoutStringMetadata")));

		Record zeSchemaRecordWithoutReference = transaction.add(new TestRecord(zeSchema, "record5")
				.set(zeSchema.stringMetadata(), "42"));

		transaction.add(new TestRecord(anotherSchema, "anotherSchemaRecord1")
				.set(anotherSchema.stringMetadata(), "value1"));

		transaction.add(new TestRecord(anotherSchema, "anotherSchemaRecord2")
				.set(anotherSchema.stringMetadata(), "value2"));

		transaction.add(new TestRecord(anotherSchema, "anotherSchemaRecordWithoutStringMetadata"));

		getModelLayerFactory().newRecordServices().execute(transaction);

		assertThat(zeSchemaRecordWithOneReference.<String>get(zeSchema.anotherStringMetadata()))
				.isEqualTo("Prefixe [value1] - 666 Suffixe");
		assertThat(zeSchemaRecordWithTwoReference.<String>get(zeSchema.anotherStringMetadata()))
				.isEqualTo("Prefixe [value1, value2] - 666 Suffixe");
		assertThat(zeSchemaRecordWithoutReferencedStringMetadata.<String>get(zeSchema.anotherStringMetadata()))
				.isEqualTo("Prefixe [null] - zeValue Suffixe");
		assertThat(zeSchemaRecordWithoutReference.<String>get(zeSchema.anotherStringMetadata()))
				.isEqualTo("Prefixe [] - 42 Suffixe");
	}
}
