package com.constellio.model.services.records;

import com.constellio.data.utils.ThreadList;
import com.constellio.model.entities.calculators.CalculatorParameters;
import com.constellio.model.entities.calculators.dependencies.Dependency;
import com.constellio.model.entities.calculators.dependencies.LocalDependency;
import com.constellio.model.entities.calculators.dependencies.ReferenceDependency;
import com.constellio.model.entities.records.Record;
import com.constellio.model.entities.records.SequenceValue;
import com.constellio.model.entities.records.Transaction;
import com.constellio.model.entities.schemas.Metadata;
import com.constellio.model.entities.schemas.entries.AdvancedSequenceCalculator;
import com.constellio.model.entities.schemas.validation.RecordMetadataValidator;
import com.constellio.sdk.tests.ConstellioTest;
import com.constellio.sdk.tests.TestRecord;
import com.constellio.sdk.tests.schemas.TestsSchemasSetup;
import com.constellio.sdk.tests.schemas.TestsSchemasSetup.AnotherSchemaMetadatas;
import com.constellio.sdk.tests.schemas.TestsSchemasSetup.ThirdSchemaMetadatas;
import com.constellio.sdk.tests.schemas.TestsSchemasSetup.ZeSchemaMetadatas;
import org.assertj.core.data.MapEntry;
import org.joda.time.LocalDate;
import org.junit.Before;
import org.junit.Test;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.atomic.AtomicReference;

import static com.constellio.sdk.tests.schemas.TestsSchemasSetup.withValidator;
import static org.apache.ignite.internal.util.lang.GridFunc.asList;
import static org.assertj.core.api.Assertions.assertThat;

public class RecordServicesAdvancedSequenceMetadataAcceptanceTest extends ConstellioTest {

	private static final LocalDate DATE_METADATA_VALUE = new LocalDate(2020, 10, 19);
	private static final LocalDate ANOTHER_DATE_METADATA_VALUE = new LocalDate(2020, 10, 20);
	private static final LocalDate THIRD_DATE_METADATA_VALUE = new LocalDate(2020, 9, 20);
	private static final String REFERENCED_RECORD_TITLE = "referencedRecord";
	private static final String REFERENCED_RECORD_STRING_METADATA = "referencedRecordStringMetadata";
	private static final String ANOTHER_REFERENCED_RECORD_STRING_METADATA = "anotherReferencedRecordStringMetadata";
	private static final String ANOTHER_REFERENCED_RECORD_TITLE = "anotherReferencedRecord";

	TestsSchemasSetup schemas = new TestsSchemasSetup(zeCollection);
	ZeSchemaMetadatas zeSchema = schemas.new ZeSchemaMetadatas();
	AnotherSchemaMetadatas anotherSchema = schemas.new AnotherSchemaMetadatas();
	ThirdSchemaMetadatas thirdSchema = schemas.new ThirdSchemaMetadatas();
	Record aReferenceRecord;
	Record anotherReferenceRecord;
	RecordServices recordServices;
	List<Integer> sequenceValuesList;

	@Before
	public void before() throws Exception {
		Transaction transaction = new Transaction();
		recordServices = getModelLayerFactory().newRecordServices();
		defineSchemasManager().using(schemas
				.withADateMetadata() //zeSchemaType_default_dateMetadata
				.withAReferenceMetadata((metadata, types)->metadata.defineReferencesTo(types.getSchemaType("zeSchemaType"))) //zeSchemaType_default_referenceFromAnotherSchemaToZeSchema
				.withAStringMetadataInAnotherSchema() //anotherSchemaType_default_stringMetadata
				.withAStringMetadata(withValidator(StringValueValidator.class)) //anotherSchemaType_default_stringMetadata
				.withAnAdvancedSequence("structureMetadata", DateBasedTestSequenceCalculator.class)
				.withAnAdvancedSequence("anotherStructureMetadata", ReferencedMetadataBasedTestSequenceCalculator.class)
		);
		sequenceValuesList = new ArrayList<>();
		aReferenceRecord = new TestRecord(anotherSchema);
		aReferenceRecord.set(anotherSchema.stringMetadata(), REFERENCED_RECORD_STRING_METADATA);
		aReferenceRecord.set(anotherSchema.titleMetadata(), REFERENCED_RECORD_TITLE);
		transaction.add(aReferenceRecord);
		anotherReferenceRecord = new TestRecord(anotherSchema);
		anotherReferenceRecord.set(anotherSchema.stringMetadata(), ANOTHER_REFERENCED_RECORD_STRING_METADATA);
		anotherReferenceRecord.set(anotherSchema.titleMetadata(), ANOTHER_REFERENCED_RECORD_TITLE);
		transaction.add(anotherReferenceRecord);
		recordServices.execute(transaction);
	}

	public static class DateBasedTestSequenceCalculator implements AdvancedSequenceCalculator {

		LocalDependency<LocalDate> DATE_METADATA_DEPENDENCY = LocalDependency.toADate("dateMetadata");

		@Override
		public List<? extends Dependency> getDependencies() {
			return asList(DATE_METADATA_DEPENDENCY);
		}

		@Override
		public String getSequenceGroupName(Metadata metadata) {
			return "This is a group of - property";
		}

		@Override
		public String computeSequenceTableId(CalculatorParameters parameters) {
			LocalDate dateMetadata = parameters.get(DATE_METADATA_DEPENDENCY);
			return dateMetadata == null ? null : ("" + dateMetadata.year().get());
		}

		@Override
		public String computeSequenceTableValue(CalculatorParameters parameters, int sequenceValue) {
			LocalDate dateMetadata = parameters.get(DATE_METADATA_DEPENDENCY);
			return "SEQ-" + dateMetadata.getYear() + "-" + dateMetadata.getMonthOfYear() + new DecimalFormat("#####").format(sequenceValue);
		}
	}

	public static class ReferencedMetadataBasedTestSequenceCalculator implements AdvancedSequenceCalculator {

		ReferenceDependency<String> REFERENCED_STRING_METADATA_DEPENDENCY = ReferenceDependency
				.toAString("zeSchemaType_default_referenceMetadata", "anotherSchemaType_default_stringMetadata");

		ReferenceDependency<String> REFERENCED_TITLE_DEPENDENCY = ReferenceDependency
				.toAString("zeSchemaType_default_referenceMetadata", "anotherSchemaType_default_title");

		@Override
		public List<? extends Dependency> getDependencies() {
			return asList(REFERENCED_STRING_METADATA_DEPENDENCY, REFERENCED_TITLE_DEPENDENCY);
		}

		@Override
		public String computeSequenceTableId(CalculatorParameters parameters) {
			return parameters.get(REFERENCED_STRING_METADATA_DEPENDENCY);
		}

		@Override
		public String computeSequenceTableValue(CalculatorParameters parameters, int sequenceValue) {
			String title = parameters.get(REFERENCED_TITLE_DEPENDENCY);
			return title + "-" + new DecimalFormat("####").format(sequenceValue);
		}


	}

	public static class StringValueValidator implements RecordMetadataValidator<String> {

		@Override
		public void validate(RecordMetadataValidatorParams recordMetadataValidatorParams) {
			String value = (String) recordMetadataValidatorParams.getValue();
			if (value != null && value.contains("@")) {
				recordMetadataValidatorParams.getValidationErrors().add(StringValueValidator.class, "Error");
			}
		}
	}

	@Test
	public void whenCreatingANewRecordThenReceiveNewSequenceNumber() throws RecordServicesException {
		Record record = new TestRecord(zeSchema);
		record.set(zeSchema.dateMetadata(), DATE_METADATA_VALUE);

		recordServices.add(record);

		String sequenceTableValue = "SEQ-" + DATE_METADATA_VALUE.getYear() + "-" + DATE_METADATA_VALUE.getMonthOfYear() + new DecimalFormat("#####").format(1);
		SequenceValue structureMetadata = record.get(zeSchema.structureMetadata());
		assertThat(structureMetadata.getDisplayValue()).isEqualTo(sequenceTableValue);

		assertThat(getDataLayerFactory().getSequencesManager().getSequences()).containsOnly(
				MapEntry.entry("This_is_a_group_of_-_property:2020", 1L)
		);
	}

	@Test
	public void whenCreatingANewRecordWithReferenceMetadataThenReceiveNewSequenceNumber()
			throws RecordServicesException {
		Record record = new TestRecord(zeSchema);
		record.set(zeSchema.referenceMetadata(), aReferenceRecord.getId());

		recordServices.add(record);

		String sequenceTableValue = REFERENCED_RECORD_TITLE + "-" + new DecimalFormat("####").format(1);
		SequenceValue structureMetadata = record.get(zeSchema.anotherStructureMetadata());
		assertThat(structureMetadata.getDisplayValue()).isEqualTo(sequenceTableValue);
		assertThat(structureMetadata.getSequenceTable()).isEqualTo(REFERENCED_RECORD_STRING_METADATA);

		assertThat(getDataLayerFactory().getSequencesManager().getSequences()).containsOnly(
				MapEntry.entry("zeSchemaType-anotherStructureMetadata:referencedRecordStringMetadata", 1L)
		);
	}

	//* Tester en modifiant une dépendance ne faisant pas changer la valeur sequentielle
	@Test
	public void whenModifiyingDependencyValueWithSameMonthAndYearThenSameSequenceNumber()
			throws RecordServicesException {
		Record record = new TestRecord(zeSchema);
		record.set(zeSchema.dateMetadata(), DATE_METADATA_VALUE);

		recordServices.add(record);

		String sequenceTableValue = "SEQ-" + DATE_METADATA_VALUE.getYear() + "-" + DATE_METADATA_VALUE.getMonthOfYear() + new DecimalFormat("#####").format(1);
		SequenceValue structureMetadata = record.get(zeSchema.structureMetadata());
		assertThat(structureMetadata.getDisplayValue()).isEqualTo(sequenceTableValue);

		record.set(zeSchema.dateMetadata(), ANOTHER_DATE_METADATA_VALUE);

		recordServices.add(record);

		String secondSequenceTableValue = "SEQ-" + ANOTHER_DATE_METADATA_VALUE.getYear() + "-" + ANOTHER_DATE_METADATA_VALUE.getMonthOfYear() + new DecimalFormat("#####").format(1);
		assertThat(sequenceTableValue.equals(secondSequenceTableValue));
		structureMetadata = record.get(zeSchema.structureMetadata());
		assertThat(structureMetadata.getDisplayValue()).isEqualTo(secondSequenceTableValue);

		assertThat(getDataLayerFactory().getSequencesManager().getSequences()).containsOnly(
				MapEntry.entry("This_is_a_group_of_-_property:2020", 1L)
		);

	}

	//* Tester en modifiant une dépendance ne faisant pas changer la valeur sequentielle
	@Test
	public void whenModifiyingDependencyValueWithDifferentMonthAndYearThenDifferentSequenceNumber()
			throws RecordServicesException {
		Record record = new TestRecord(zeSchema);
		record.set(zeSchema.dateMetadata(), DATE_METADATA_VALUE);

		recordServices.add(record);

		String sequenceTableValue = "SEQ-" + DATE_METADATA_VALUE.getYear() + "-" + DATE_METADATA_VALUE.getMonthOfYear() + new DecimalFormat("#####").format(1);
		SequenceValue structureMetadata = record.get(zeSchema.structureMetadata());
		assertThat(structureMetadata.getDisplayValue()).isEqualTo(sequenceTableValue);

		record.set(zeSchema.dateMetadata(), DATE_METADATA_VALUE.plusYears(2));

		recordServices.add(record);

		String secondSequenceTableValue = "SEQ-" + DATE_METADATA_VALUE.plusYears(2).getYear() + "-" + DATE_METADATA_VALUE.plusYears(2).getMonthOfYear() + new DecimalFormat("#####").format(1);
		assertThat(sequenceTableValue.equals(secondSequenceTableValue));
		structureMetadata = record.get(zeSchema.structureMetadata());
		assertThat(structureMetadata.getDisplayValue()).isEqualTo(secondSequenceTableValue);

		assertThat(getDataLayerFactory().getSequencesManager().getSequences()).containsOnly(
				MapEntry.entry("This_is_a_group_of_-_property:2020", 1L),
				MapEntry.entry("This_is_a_group_of_-_property:2022", 1L)
		);

	}

	@Test
	public void whenModifiyingReferenceMetadataWithSameMonthAndYearThenSameSequenceNumber()
			throws RecordServicesException {
		Record record = new TestRecord(zeSchema);
		record.set(zeSchema.referenceMetadata(), aReferenceRecord.getId());
		recordServices.add(record);

		String sequenceTableValue = REFERENCED_RECORD_TITLE + "-" + new DecimalFormat("####").format(1);
		SequenceValue structureMetadata = record.get(zeSchema.anotherStructureMetadata());
		assertThat(structureMetadata.getDisplayValue()).isEqualTo(sequenceTableValue);
		assertThat(structureMetadata.getSequenceTable()).isEqualTo(REFERENCED_RECORD_STRING_METADATA);

		record.set(zeSchema.referenceMetadata(), anotherReferenceRecord.getId());
		recordServices.add(record);

		sequenceTableValue = ANOTHER_REFERENCED_RECORD_TITLE + "-" + new DecimalFormat("####").format(1);
		structureMetadata = record.get(zeSchema.anotherStructureMetadata());
		assertThat(structureMetadata.getDisplayValue()).isEqualTo(sequenceTableValue);
		assertThat(structureMetadata.getSequenceTable()).isEqualTo(ANOTHER_REFERENCED_RECORD_STRING_METADATA);

		assertThat(getDataLayerFactory().getSequencesManager().getSequences()).containsOnly(
				MapEntry.entry("zeSchemaType-anotherStructureMetadata:referencedRecordStringMetadata", 1L),
				MapEntry.entry("zeSchemaType-anotherStructureMetadata:anotherReferencedRecordStringMetadata", 1L)


		);
	}

	//* Tester en modifiant une dépendance faisant changer la valeur sequentielle
	@Test
	public void whenModifiyingDependencyValueWithAnotherMonthOrYearThenSequenceNumberChanges()
			throws RecordServicesException {
		Record record = new TestRecord(zeSchema);
		record.set(zeSchema.dateMetadata(), DATE_METADATA_VALUE);

		recordServices.add(record);

		String sequenceTableValue = "SEQ-" + DATE_METADATA_VALUE.getYear() + "-" + DATE_METADATA_VALUE.getMonthOfYear() + new DecimalFormat("#####").format(1);
		SequenceValue structureMetadata = record.get(zeSchema.structureMetadata());
		assertThat(structureMetadata.getDisplayValue()).isEqualTo(sequenceTableValue);

		record.set(zeSchema.dateMetadata(), THIRD_DATE_METADATA_VALUE);

		recordServices.add(record);

		String secondSequenceTableValue = "SEQ-" + THIRD_DATE_METADATA_VALUE.getYear() + "-" + THIRD_DATE_METADATA_VALUE.getMonthOfYear() + new DecimalFormat("#####").format(1);
		assertThat(!sequenceTableValue.equals(secondSequenceTableValue));
		structureMetadata = record.get(zeSchema.structureMetadata());
		assertThat(structureMetadata.getDisplayValue()).isEqualTo(secondSequenceTableValue);

		assertThat(getDataLayerFactory().getSequencesManager().getSequences()).containsOnly(
				MapEntry.entry("This_is_a_group_of_-_property:2020", 1L)
		);
	}

	//* Sauvegarder plusieurs records en même temps
	@Test
	public void whenAddingManyRecordsInOneTransactionThenSequenceNumberIsSetCorrectly() throws RecordServicesException {
		Transaction transaction = new Transaction();
		Record firstRecord = new TestRecord(zeSchema);
		firstRecord.set(zeSchema.dateMetadata(), DATE_METADATA_VALUE);
		Record secondRecord = new TestRecord(zeSchema);
		secondRecord.set(zeSchema.dateMetadata(), ANOTHER_DATE_METADATA_VALUE);
		Record thirdRecord = new TestRecord(zeSchema);
		thirdRecord.set(zeSchema.dateMetadata(), THIRD_DATE_METADATA_VALUE);

		transaction.addUpdate(asList(firstRecord, secondRecord, thirdRecord));
		recordServices.execute(transaction);

		SequenceValue structureMetadata = firstRecord.get(zeSchema.structureMetadata());
		String sequenceTableValue = "SEQ-" + DATE_METADATA_VALUE.getYear() + "-" + DATE_METADATA_VALUE.getMonthOfYear();
		sequenceValuesList.add(structureMetadata.getSequenceValue());
		assertThat(structureMetadata.getDisplayValue()).startsWith(sequenceTableValue);

		SequenceValue secondStructureMetadata = secondRecord.get(zeSchema.structureMetadata());
		String secondSequenceTableValue = "SEQ-" + ANOTHER_DATE_METADATA_VALUE.getYear() + "-" + ANOTHER_DATE_METADATA_VALUE.getMonthOfYear();
		sequenceValuesList.add(secondStructureMetadata.getSequenceValue());
		assertThat(secondStructureMetadata.getDisplayValue()).startsWith(secondSequenceTableValue);

		SequenceValue thirdStructureMetadata = thirdRecord.get(zeSchema.structureMetadata());
		String thirdSequenceTableValue = "SEQ-" + THIRD_DATE_METADATA_VALUE.getYear() + "-" + THIRD_DATE_METADATA_VALUE.getMonthOfYear();
		sequenceValuesList.add(thirdStructureMetadata.getSequenceValue());
		assertThat(thirdStructureMetadata.getDisplayValue()).startsWith(thirdSequenceTableValue);

		Set<Integer> sequenceValues = new HashSet<>(sequenceValuesList);
		assertThat(sequenceValues.size()).isEqualTo(sequenceValuesList.size());

		assertThat(getDataLayerFactory().getSequencesManager().getSequences()).containsOnly(
				MapEntry.entry("This_is_a_group_of_-_property:2020", 3L)
		);
	}


	//* Enlever la valeur de la dépendance : on perd la valeur séquentielle
	@Test
	public void whenRemovingDependencyValueThenSequenceNumberIsNull() throws RecordServicesException {
		RecordServices recordServices = getModelLayerFactory().newRecordServices();
		Record record = new TestRecord(zeSchema);
		record.set(zeSchema.dateMetadata(), DATE_METADATA_VALUE);

		recordServices.add(record);

		String sequenceTableValue = "SEQ-" + DATE_METADATA_VALUE.getYear() + "-" + DATE_METADATA_VALUE.getMonthOfYear() + new DecimalFormat("#####").format(1);
		SequenceValue structureMetadata = record.get(zeSchema.structureMetadata());
		assertThat(structureMetadata.getDisplayValue()).isEqualTo(sequenceTableValue);

		record.set(zeSchema.dateMetadata(), null);

		recordServices.add(record);

		structureMetadata = record.get(zeSchema.structureMetadata());
		assertThat(structureMetadata).isNull();

		record = recordServices.get(record.getId());
		structureMetadata = record.get(zeSchema.structureMetadata());
		assertThat(structureMetadata).isNull();

		assertThat(getDataLayerFactory().getSequencesManager().getSequences()).containsOnly(
				MapEntry.entry("This_is_a_group_of_-_property:2020", 1L)
		);
	}

	//* Tester avec beaucoup de transactions concurrentes, valider que chaque record a un numéro unique
	@Test
	public void whenExecutingConcurrentTransactionsThenAllSequenceValuesAreDifferent() throws Exception {
		ThreadList<Thread> threads = new ThreadList<>();
		final AtomicReference<Exception> exceptionAtomicReference = new AtomicReference<>();
		for (int i = 0; i < 1000; i++) {
			int finalI = i;
			threads.add(new Thread("Thread #" + finalI) {

				public void run() {
					System.out.println("Run: " + getName());
					try {
						addRecordWithDateMetadata();
					} catch (Exception e) {
						e.printStackTrace();
						exceptionAtomicReference.set(e);
					}
				}
			});
		}
		threads.startAll();
		threads.joinAll();
		if (exceptionAtomicReference.get() != null) {
			throw exceptionAtomicReference.get();
		}
		Set<Integer> sequenceValues = new HashSet<>(sequenceValuesList);
		assertThat(sequenceValues.size()).isEqualTo(sequenceValuesList.size());

		assertThat(getDataLayerFactory().getSequencesManager().getSequences()).containsOnly(
				MapEntry.entry("This_is_a_group_of_-_property:2020", 1000L)
		);
	}

	@Test
	public void addRecordWithDateMetadata() throws RecordServicesException {
		RecordServices recordServices = getModelLayerFactory().newRecordServices();
		Record record = new TestRecord(zeSchema);
		record.set(zeSchema.dateMetadata(), DATE_METADATA_VALUE);

		recordServices.add(record);

		SequenceValue structureMetadata = record.get(zeSchema.structureMetadata());
		sequenceValuesList.add(structureMetadata.getSequenceValue());
	}

	//Pour la fin
	//* Essayer de sauvegarder un record avec un problème (métadonnée obligatoire), valider que non incrémenté
	@Test
	public void addRecordWithDateMetadataAndProblemThenSequenceValueIsNotIncremented() throws RecordServicesException {
		RecordServices recordServices = getModelLayerFactory().newRecordServices();
		Record record = new TestRecord(zeSchema);
		recordServices.add(record);

		record.set(zeSchema.dateMetadata(), DATE_METADATA_VALUE);
		record.set(zeSchema.stringMetadata(), "test@");

		try {
			recordServices.update(record);
		} catch (RecordServicesException e) {
			SequenceValue structureMetadata = record.get(zeSchema.structureMetadata());
			assertThat(structureMetadata).isNull();
		}

		assertThat(getDataLayerFactory().getSequencesManager().getSequences()).isEmpty();

	}

	//* Essayer de sauvegarder un record avec un problème d'extension (métadonnée obligatoire), valider que non incrémenté

	//* Si AdvancedSequenceCalculator.isSequenceTableDecrementedWhenDeletingLastValue et
	// qu'on supprime le record, et que c'est le dernier généré, on réattribue l'id

	//* ATTEND : Pouvoir setter manuellement des valeurs

}
