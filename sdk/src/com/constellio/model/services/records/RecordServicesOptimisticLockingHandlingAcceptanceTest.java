package com.constellio.model.services.records;

import com.constellio.data.dao.dto.records.OptimisticLockingResolution;
import com.constellio.data.utils.ThreadList;
import com.constellio.model.entities.calculators.CalculatorParameters;
import com.constellio.model.entities.calculators.MetadataValueCalculator;
import com.constellio.model.entities.calculators.dependencies.Dependency;
import com.constellio.model.entities.calculators.dependencies.LocalDependency;
import com.constellio.model.entities.calculators.dependencies.ReferenceDependency;
import com.constellio.model.entities.records.Record;
import com.constellio.model.entities.records.Transaction;
import com.constellio.model.entities.schemas.Metadata;
import com.constellio.model.entities.schemas.MetadataValueType;
import com.constellio.model.services.records.RecordServicesException.UnresolvableOptimisticLockingConflict;
import com.constellio.model.services.schemas.builders.MetadataBuilder;
import com.constellio.sdk.tests.ConstellioTest;
import com.constellio.sdk.tests.TestRecord;
import com.constellio.sdk.tests.annotations.SlowTest;
import com.constellio.sdk.tests.schemas.TestsSchemasSetup;
import org.junit.Before;
import org.junit.Test;

import java.util.Arrays;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import static com.constellio.model.entities.schemas.MetadataValueType.NUMBER;
import static com.constellio.sdk.tests.TestUtils.asList;
import static junit.framework.TestCase.fail;
import static org.assertj.core.api.Assertions.assertThat;

@SlowTest
public class RecordServicesOptimisticLockingHandlingAcceptanceTest extends ConstellioTest {

	RecordServices recordServices;

	OptimisticLockingTestSchemasSetup schemas = new OptimisticLockingTestSchemasSetup();
	OptimisticLockingTestSchemasSetup.ZeSchemaMetadatas zeSchema = schemas.new ZeSchemaMetadatas();
	OptimisticLockingTestSchemasSetup.AnotherSchemaMetadatas anotherSchema = schemas.new AnotherSchemaMetadatas();

	String zeSchemaRecord1Id;
	String zeSchemaRecord2Id;
	String zeSchemaRecord3Id;
	String anotherSchemaRecordId;

	@Before
	public void setUp()
			throws Exception {
		recordServices = getModelLayerFactory().newRecordServices();
	}

	@Test
	public void givenOptimisticLockingWhenExecutingSecondTransactionThenMerge_run1()
			throws Exception {
		givenOptimisticLockingWhenExecutingSecondTransactionThenMerge();
	}

	@Test
	public void givenOptimisticLockingWhenExecutingSecondTransactionThenMerge_run2()
			throws Exception {
		givenOptimisticLockingWhenExecutingSecondTransactionThenMerge();
	}

	@Test
	public void givenOptimisticLockingWhenExecutingSecondTransactionThenMerge_run3()
			throws Exception {
		givenOptimisticLockingWhenExecutingSecondTransactionThenMerge();
	}

	@Test
	public void givenOptimisticLockingWhenExecutingSecondTransactionThenMerge_run4()
			throws Exception {
		givenOptimisticLockingWhenExecutingSecondTransactionThenMerge();
	}

	@Test
	public void givenOptimisticLockingWhenExecutingSecondTransactionThenMerge_run5()
			throws Exception {
		givenOptimisticLockingWhenExecutingSecondTransactionThenMerge();
	}

	@Test
	public void givenOptimisticLockingWhenExecutingSecondTransactionThenMerge_run6()
			throws Exception {
		givenOptimisticLockingWhenExecutingSecondTransactionThenMerge();
	}

	@Test
	public void givenOptimisticLockingWhenExecutingSecondTransactionThenMerge_run7()
			throws Exception {
		givenOptimisticLockingWhenExecutingSecondTransactionThenMerge();
	}

	@Test
	public void givenOptimisticLockingWhenExecutingSecondTransactionThenMerge_run8()
			throws Exception {
		givenOptimisticLockingWhenExecutingSecondTransactionThenMerge();
	}

	@Test
	public void givenOptimisticLockingWhenExecutingSecondTransactionThenMerge_run9()
			throws Exception {
		givenOptimisticLockingWhenExecutingSecondTransactionThenMerge();
	}

	@Test
	public void givenOptimisticLockingWhenExecutingSecondTransactionThenMerge_run10()
			throws Exception {
		givenOptimisticLockingWhenExecutingSecondTransactionThenMerge();
	}

	private void givenOptimisticLockingWhenExecutingSecondTransactionThenMerge()
			throws Exception {
		givenSchemasAndInitialRecords();

		Transaction transactionModifyingFirstNumberAndAnotherSchema = modifyFirstNumberAndAnotherSchema();
		Transaction transactionModifyingSecondNumber = modifySecondNumber();

		recordServices.execute(transactionModifyingFirstNumberAndAnotherSchema);

		transactionModifyingSecondNumber.setOptimisticLockingResolution(OptimisticLockingResolution.TRY_MERGE);
		recordServices.execute(transactionModifyingSecondNumber);

		assertThat((Object) getRecord1().get(zeSchema.calculatedNumber())).isEqualTo(221.0);
		assertThat((Object) getRecord2().get(zeSchema.calculatedNumber())).isEqualTo(222.0);
		assertThat((Object) getRecord3().get(zeSchema.calculatedNumber())).isEqualTo(212.0);

	}

	@Test
	public void givenOptimisticLockingHandledWithKeepOlderWhenExecutingSecondTransactionThenKeepOlder_run1()
			throws Exception {
		givenOptimisticLockingHandledWithKeepOlderWhenExecutingSecondTransactionThenKeepOlder();
	}

	@Test
	public void givenOptimisticLockingHandledWithKeepOlderWhenExecutingSecondTransactionThenKeepOlder_run2()
			throws Exception {
		givenOptimisticLockingHandledWithKeepOlderWhenExecutingSecondTransactionThenKeepOlder();
	}

	@Test
	public void givenOptimisticLockingHandledWithKeepOlderWhenExecutingSecondTransactionThenKeepOlder_run3()
			throws Exception {
		givenOptimisticLockingHandledWithKeepOlderWhenExecutingSecondTransactionThenKeepOlder();
	}

	@Test
	public void givenOptimisticLockingHandledWithKeepOlderWhenExecutingSecondTransactionThenKeepOlder_run4()
			throws Exception {
		givenOptimisticLockingHandledWithKeepOlderWhenExecutingSecondTransactionThenKeepOlder();
	}

	@Test
	public void givenOptimisticLockingHandledWithKeepOlderWhenExecutingSecondTransactionThenKeepOlder_run5()
			throws Exception {
		givenOptimisticLockingHandledWithKeepOlderWhenExecutingSecondTransactionThenKeepOlder();
	}

	private void givenOptimisticLockingHandledWithKeepOlderWhenExecutingSecondTransactionThenKeepOlder()
			throws Exception {
		givenSchemasAndInitialRecords();

		Transaction transactionModifyingFirstNumberAndAnotherSchema = modifyFirstNumberAndAnotherSchema();
		Transaction transactionModifyingSecondNumber = modifySecondNumber();

		recordServices.execute(transactionModifyingFirstNumberAndAnotherSchema);

		transactionModifyingSecondNumber.setOptimisticLockingResolution(OptimisticLockingResolution.KEEP_OLDER);
		recordServices.execute(transactionModifyingSecondNumber);

		assertThat((Object) getRecord1().get(zeSchema.calculatedNumber())).isEqualTo(221.0);
		assertThat((Object) getRecord2().get(zeSchema.calculatedNumber())).isEqualTo(221.0);
		assertThat((Object) getRecord3().get(zeSchema.calculatedNumber())).isEqualTo(211.0);

	}

	@Test
	public void givenOptimisticLockingHandledWithExceptionWhenExecutingSecondTransactionThenThrowException_run1()
			throws Exception {
		givenOptimisticLockingHandledWithExceptionWhenExecutingSecondTransactionThenThrowException();
	}

	@Test
	public void givenOptimisticLockingHandledWithExceptionWhenExecutingSecondTransactionThenThrowException_run2()
			throws Exception {
		givenOptimisticLockingHandledWithExceptionWhenExecutingSecondTransactionThenThrowException();
	}

	@Test
	public void givenOptimisticLockingHandledWithExceptionWhenExecutingSecondTransactionThenThrowException_run3()
			throws Exception {
		givenOptimisticLockingHandledWithExceptionWhenExecutingSecondTransactionThenThrowException();
	}

	@Test
	public void givenOptimisticLockingHandledWithExceptionWhenExecutingSecondTransactionThenThrowException_run4()
			throws Exception {
		givenOptimisticLockingHandledWithExceptionWhenExecutingSecondTransactionThenThrowException();
	}

	@Test
	public void givenOptimisticLockingHandledWithExceptionWhenExecutingSecondTransactionThenThrowException_run5()
			throws Exception {
		givenOptimisticLockingHandledWithExceptionWhenExecutingSecondTransactionThenThrowException();
	}

	private void givenOptimisticLockingHandledWithExceptionWhenExecutingSecondTransactionThenThrowException()
			throws Exception {
		givenSchemasAndInitialRecords();

		Transaction transactionModifyingFirstNumberAndAnotherSchema = modifyFirstNumberAndAnotherSchema();
		Transaction transactionModifyingSecondNumber = modifySecondNumber();

		recordServices.execute(transactionModifyingFirstNumberAndAnotherSchema);

		transactionModifyingSecondNumber.setOptimisticLockingResolution(OptimisticLockingResolution.EXCEPTION);

		try {
			recordServices.execute(transactionModifyingSecondNumber);
			fail("exception expected");
		} catch (RecordServicesException.OptimisticLocking e) {
			// OK
		}

		assertThat((Object) getRecord1().get(zeSchema.calculatedNumber())).isEqualTo(221.0);
		assertThat((Object) getRecord2().get(zeSchema.calculatedNumber())).isEqualTo(221.0);
		assertThat((Object) getRecord3().get(zeSchema.calculatedNumber())).isEqualTo(211.0);

	}

	@Test
	public void givenRecordWithCopiedValuesWhenMergingThenCopiedMetadatasMerged()
			throws Exception {
		final int numberOfThreads = 10;
		final int numberOfIncrements = 20;
		defineSchemasManager().using(schemas.withCopiedMetadatas(numberOfThreads));

		final AtomicInteger exceptionsCounter = new AtomicInteger();
		final AtomicInteger cannotMergeCounter = new AtomicInteger();
		final AtomicInteger corruptionCounter = new AtomicInteger();

		final String zeRecordId = "zeRecord";

		Transaction transaction = new Transaction();
		Record zeRecord = new TestRecord(zeSchema, zeRecordId);
		for (int i = 0; i < numberOfThreads; i++) {
			zeRecord.set(zeSchema.reference(i), "anotherRecord" + i);
			transaction.add(new TestRecord(anotherSchema, "anotherRecord" + i).set(anotherSchema.number(), 0));
		}
		transaction.add(zeRecord);
		recordServices.execute(transaction);

		ThreadList<Thread> threads = new ThreadList<>();
		for (int i = 0; i < numberOfThreads; i++) {
			final int index = i;
			threads.add(new Thread() {
				@Override
				public void run() {

					for (int j = 1; j < numberOfIncrements; j++) {
						while (true) {
							if (index == numberOfThreads - 1) {
								//	System.out.println("Progression : " + j + " / " + numberOfIncrements);
							}
							try {
								double currentCopiedNumber = recordServices.getDocumentById(zeRecordId)
										.get(zeSchema.number(index));
								double expectedNumber = j - 1;
								if (currentCopiedNumber != expectedNumber) {
									System.out.println(" **** Field #" + index + " : Expected '" + expectedNumber + "' but was '"
													   + currentCopiedNumber + "'");
									corruptionCounter.incrementAndGet();
								}

								Record anotherRecord = recordServices.getDocumentById("anotherRecord" + index);
								Transaction transaction = new Transaction("threadTransaction_" + index + "_" + j);

								transaction.add(anotherRecord.set(anotherSchema.number(), j));

								recordServices.execute(transaction);
								break;

							} catch (UnresolvableOptimisticLockingConflict e) {
								cannotMergeCounter.incrementAndGet();

							} catch (Throwable e) {
								exceptionsCounter.incrementAndGet();
							}
						}
					}

				}
			});
		}

		threads.startAll();
		threads.joinAll();
		assertThat(corruptionCounter.get()).isZero();
		assertThat(cannotMergeCounter.get()).isZero();
		assertThat(exceptionsCounter.get()).isZero();

	}

	@Test
	public void givenRecordWithCalculatedValuesWhenMergingThenCalculatedMetadatasMerged()
			throws Exception {
		final int numberOfThreads = 5;
		final int numberOfIncrements = 20;
		defineSchemasManager().using(schemas.withFiveCalculatedMetadatas());

		final AtomicInteger exceptionsCounter = new AtomicInteger();
		final AtomicInteger cannotMergeCounter = new AtomicInteger();
		final AtomicInteger corruptionCounter = new AtomicInteger();

		final String zeRecordId = "zeRecord";

		Transaction transaction = new Transaction();
		Record zeRecord = new TestRecord(zeSchema, zeRecordId);
		for (int i = 0; i < numberOfThreads; i++) {
			zeRecord.set(zeSchema.reference(i), "anotherRecord" + i);
			transaction.add(new TestRecord(anotherSchema, "anotherRecord" + i).set(anotherSchema.number(), 0));
		}
		transaction.add(zeRecord);
		recordServices.execute(transaction);

		ThreadList<Thread> threads = new ThreadList<>();
		for (int i = 0; i < numberOfThreads; i++) {
			final int index = i;
			threads.add(new Thread() {
				@Override
				public void run() {

					for (int j = 1; j < numberOfIncrements; j++) {
						while (true) {
							if (index == numberOfThreads - 1) {
								//	System.out.println("Progression : " + j + " / " + numberOfIncrements);
							}
							try {
								double currentCopiedNumber = recordServices.getDocumentById(zeRecordId)
										.get(zeSchema.number(index));
								double expectedNumber = j - 1;
								if (currentCopiedNumber != expectedNumber) {
									System.out.println(" **** Field #" + index + " : Expected '" + expectedNumber + "' but was '"
													   + currentCopiedNumber + "'");
									corruptionCounter.incrementAndGet();
								}

								Record anotherRecord = recordServices.getDocumentById("anotherRecord" + index);
								Transaction transaction = new Transaction("threadTransaction_" + index + "_" + j);

								transaction.add(anotherRecord.set(anotherSchema.number(), j));

								recordServices.execute(transaction);
								break;

							} catch (UnresolvableOptimisticLockingConflict e) {
								cannotMergeCounter.incrementAndGet();

							} catch (Throwable e) {
								exceptionsCounter.incrementAndGet();
							}
						}
					}

				}
			});
		}

		threads.startAll();
		threads.joinAll();
		assertThat(corruptionCounter.get()).isZero();
		assertThat(cannotMergeCounter.get()).isZero();
		assertThat(exceptionsCounter.get()).isZero();

	}

	@Test
	public void givenRecordWithManualValuesWhenMergingThenMetadatasCorrectlyMerged()
			throws Exception {
		final int numberOfThreads = 10;
		final int numberOfIncrements = 20;
		defineSchemasManager().using(schemas.withManualNumberMetadatasInZeSchemas(numberOfThreads));

		final AtomicInteger exceptionsCounter = new AtomicInteger();
		final AtomicInteger cannotMergeCounter = new AtomicInteger();
		final AtomicInteger corruptionCounter = new AtomicInteger();

		final String zeRecordId = "zeRecord";

		Transaction transaction = new Transaction();
		Record zeRecord = new TestRecord(zeSchema, zeRecordId);
		for (int i = 0; i < numberOfThreads; i++) {
			zeRecord.set(zeSchema.number(i), 0);
		}
		transaction.add(zeRecord);
		recordServices.execute(transaction);

		ThreadList<Thread> threads = new ThreadList<>();
		for (int i = 0; i < numberOfThreads; i++) {
			final int index = i;
			threads.add(new Thread() {
				@Override
				public void run() {

					for (int j = 1; j < numberOfIncrements; j++) {
						while (true) {
							if (index == numberOfThreads - 1) {
								//	System.out.println("Progression : " + j + " / " + numberOfIncrements);
							}
							try {
								double currentCopiedNumber = recordServices.getDocumentById(zeRecordId)
										.get(zeSchema.number(index));
								double expectedNumber = j - 1;
								if (currentCopiedNumber != expectedNumber) {
									System.out.println(" **** Field #" + index + " : Expected '" + expectedNumber + "' but was '"
													   + currentCopiedNumber + "' **** ");
									corruptionCounter.incrementAndGet();
								}

								Record zeRecord = recordServices.getDocumentById(zeRecordId);
								Transaction transaction = new Transaction();

								transaction.add(zeRecord.set(zeSchema.number(index), j));

								recordServices.execute(transaction);
								break;

							} catch (UnresolvableOptimisticLockingConflict e) {
								cannotMergeCounter.incrementAndGet();

							} catch (Throwable e) {
								exceptionsCounter.incrementAndGet();
							}
						}
					}

				}
			});
		}

		threads.startAll();
		threads.joinAll();
		assertThat(corruptionCounter.get()).isZero();
		assertThat(cannotMergeCounter.get()).isZero();
		assertThat(exceptionsCounter.get()).isZero();

	}

	@Test
	public void givenOptimisticLockingWhenExecutingSecondTransactionWithConflictThenFail_run1()
			throws Exception {
		givenOptimisticLockingWhenExecutingSecondTransactionWithConflictThenFail();
	}

	@Test
	public void givenOptimisticLockingWhenExecutingSecondTransactionWithConflictThenFail_run2()
			throws Exception {
		givenOptimisticLockingWhenExecutingSecondTransactionWithConflictThenFail();
	}

	@Test
	public void givenOptimisticLockingWhenExecutingSecondTransactionWithConflictThenFail_run3()
			throws Exception {
		givenOptimisticLockingWhenExecutingSecondTransactionWithConflictThenFail();
	}

	@Test
	public void givenOptimisticLockingWhenExecutingSecondTransactionWithConflictThenFail_run4()
			throws Exception {
		givenOptimisticLockingWhenExecutingSecondTransactionWithConflictThenFail();
	}

	@Test
	public void givenOptimisticLockingWhenExecutingSecondTransactionWithConflictThenFail_run5()
			throws Exception {
		givenOptimisticLockingWhenExecutingSecondTransactionWithConflictThenFail();
	}

	// -----------------------------------------------------------------

	private void givenOptimisticLockingWhenExecutingSecondTransactionWithConflictThenFail()
			throws Exception {
		givenSchemasAndInitialRecords();

		Transaction transactionModifyingFirstNumberAndAnotherSchema = modifyFirstNumberAndAnotherSchema();
		Transaction modifyFirstNumberWithOtherValues = modifyFirstNumberWithOtherValues();

		recordServices.execute(transactionModifyingFirstNumberAndAnotherSchema);

		modifyFirstNumberWithOtherValues.setOptimisticLockingResolution(OptimisticLockingResolution.TRY_MERGE);
		try {
			recordServices.execute(modifyFirstNumberWithOtherValues);
			fail("exception expected");
		} catch (RecordServicesException.UnresolvableOptimisticLockingConflict e) {
			// OK
		}

		assertThat((Object) getRecord1().get(zeSchema.calculatedNumber())).isEqualTo(221.0);
		assertThat((Object) getRecord2().get(zeSchema.calculatedNumber())).isEqualTo(221.0);
		assertThat((Object) getRecord3().get(zeSchema.calculatedNumber())).isEqualTo(211.0);
	}

	private Transaction modifySecondNumber() {
		Transaction transaction = new Transaction();
		transaction.addUpdate(getRecord2().set(zeSchema.number2(), 2.0));
		transaction.addUpdate(getRecord3().set(zeSchema.number2(), 2.0));
		transaction.addUpdate(getAnotherSchemaRecord().set(anotherSchema.number(), 200.0));

		return transaction;
	}

	private Transaction modifyFirstNumberAndAnotherSchema() {
		Transaction transaction = new Transaction();

		transaction.addUpdate(getRecord1().set(zeSchema.number1(), 20.0));
		transaction.addUpdate(getRecord2().set(zeSchema.number1(), 20.0));
		transaction.addUpdate(getAnotherSchemaRecord().set(anotherSchema.number(), 200.0));

		return transaction;
	}

	private Transaction modifyFirstNumberWithOtherValues() {
		Transaction transaction = new Transaction();

		transaction.addUpdate(getRecord1().set(zeSchema.number1(), 30.0));
		transaction.addUpdate(getRecord2().set(zeSchema.number1(), 30.0));

		return transaction;
	}

	private Record getRecord1() {
		return recordServices.getDocumentById(zeSchemaRecord1Id);
	}

	private Record getRecord2() {
		return recordServices.getDocumentById(zeSchemaRecord2Id);
	}

	private Record getRecord3() {
		return recordServices.getDocumentById(zeSchemaRecord3Id);
	}

	private Record getAnotherSchemaRecord() {
		return recordServices.getDocumentById(anotherSchemaRecordId);
	}

	private void givenSchemasAndInitialRecords()
			throws RecordServicesException {

		defineSchemasManager().using(schemas.withCalculatorUsingOtherNumbers());

		Record anotherSchemaRecord = new TestRecord(anotherSchema);
		recordServices.add(anotherSchemaRecord.set(anotherSchema.number(), 100.0));
		anotherSchemaRecordId = anotherSchemaRecord.getId();

		Transaction transaction = new Transaction();

		Record zeSchemaRecord1 = recordServices.newRecordWithSchema(zeSchema.instance());
		transaction.addUpdate(zeSchemaRecord1.set(zeSchema.number1(), 10.0).set(zeSchema.number2(), 1.0)
				.set(zeSchema.refToAnotherSchema(), anotherSchemaRecordId));

		Record zeSchemaRecord2 = recordServices.newRecordWithSchema(zeSchema.instance());
		transaction.addUpdate(zeSchemaRecord2.set(zeSchema.number1(), 10.0).set(zeSchema.number2(), 1.0)
				.set(zeSchema.refToAnotherSchema(), anotherSchemaRecordId));

		Record zeSchemaRecord3 = recordServices.newRecordWithSchema(zeSchema.instance());
		transaction.addUpdate(zeSchemaRecord3.set(zeSchema.number1(), 10.0).set(zeSchema.number2(), 1.0)
				.set(zeSchema.refToAnotherSchema(), anotherSchemaRecordId));

		recordServices.execute(transaction);

		zeSchemaRecord1Id = zeSchemaRecord1.getId();
		zeSchemaRecord2Id = zeSchemaRecord2.getId();
		zeSchemaRecord3Id = zeSchemaRecord3.getId();

	}

	private static class OptimisticLockingTestSchemasSetup extends TestsSchemasSetup {

		private OptimisticLockingTestSchemasSetup withCalculatorUsingOtherNumbers() {
			anOtherDefaultSchemaBuilder.create("number").setType(NUMBER);

			zeDefaultSchemaBuilder.create("number1").setType(NUMBER);
			zeDefaultSchemaBuilder.create("number2").setType(NUMBER);
			zeDefaultSchemaBuilder.create("refToAnotherSchema").defineReferencesTo(anOtherSchemaTypeBuilder);
			zeDefaultSchemaBuilder.create("calculatedNumber").setType(NUMBER).defineDataEntry()
					.asCalculated(SumOfOtherMetadatas.class);

			return this;
		}

		private OptimisticLockingTestSchemasSetup withCopiedMetadatas(int quantity) {
			MetadataBuilder anotherSchemaNumber = anOtherDefaultSchemaBuilder.create("number").setType(NUMBER);

			for (int i = 0; i < quantity; i++) {
				MetadataBuilder reference = zeDefaultSchemaBuilder.create("reference" + i)
						.defineReferencesTo(anOtherSchemaTypeBuilder);
				zeDefaultSchemaBuilder.create("number" + i).setType(NUMBER)
						.defineDataEntry().asCopied(reference, anotherSchemaNumber);
			}

			return this;
		}

		private OptimisticLockingTestSchemasSetup withFiveCalculatedMetadatas() {
			anOtherDefaultSchemaBuilder.create("number").setType(NUMBER);

			zeDefaultSchemaBuilder.create("reference0").defineReferencesTo(anOtherSchemaTypeBuilder);
			zeDefaultSchemaBuilder.create("reference1").defineReferencesTo(anOtherSchemaTypeBuilder);
			zeDefaultSchemaBuilder.create("reference2").defineReferencesTo(anOtherSchemaTypeBuilder);
			zeDefaultSchemaBuilder.create("reference3").defineReferencesTo(anOtherSchemaTypeBuilder);
			zeDefaultSchemaBuilder.create("reference4").defineReferencesTo(anOtherSchemaTypeBuilder);
			zeDefaultSchemaBuilder.create("number0").setType(NUMBER)
					.defineDataEntry().asCalculated(RecordServicesOptimisticLockingHandlingAcceptanceTest_Calculator1.class);
			zeDefaultSchemaBuilder.create("number1").setType(NUMBER)
					.defineDataEntry().asCalculated(RecordServicesOptimisticLockingHandlingAcceptanceTest_Calculator2.class);
			zeDefaultSchemaBuilder.create("number2").setType(NUMBER)
					.defineDataEntry().asCalculated(RecordServicesOptimisticLockingHandlingAcceptanceTest_Calculator3.class);
			zeDefaultSchemaBuilder.create("number3").setType(NUMBER)
					.defineDataEntry().asCalculated(RecordServicesOptimisticLockingHandlingAcceptanceTest_Calculator4.class);
			zeDefaultSchemaBuilder.create("number4").setType(NUMBER)
					.defineDataEntry().asCalculated(RecordServicesOptimisticLockingHandlingAcceptanceTest_Calculator5.class);

			return this;
		}

		private OptimisticLockingTestSchemasSetup withManualNumberMetadatasInZeSchemas(int quantity) {

			for (int i = 0; i < quantity; i++) {
				zeDefaultSchemaBuilder.create("number" + i).setType(NUMBER);
			}

			return this;
		}

		private class ZeSchemaMetadatas extends TestsSchemasSetup.ZeSchemaMetadatas {

			private Metadata reference(int n) {
				return getMetadata(code() + "_reference" + n);
			}

			private Metadata number(int n) {
				return getMetadata(code() + "_number" + n);
			}

			private Metadata number1() {
				return getMetadata(code() + "_number1");
			}

			private Metadata number2() {
				return getMetadata(code() + "_number2");
			}

			private Metadata refToAnotherSchema() {
				return getMetadata(code() + "_refToAnotherSchema");
			}

			private Metadata calculatedNumber() {
				return getMetadata(code() + "_calculatedNumber");
			}
		}

		private class AnotherSchemaMetadatas extends TestsSchemasSetup.AnotherSchemaMetadatas {

			private Metadata number() {
				return getMetadata(code() + "_number");
			}
		}

	}

	public static class SumOfOtherMetadatas implements MetadataValueCalculator<Double> {

		LocalDependency<Double> number1Param = LocalDependency.toANumber("number1");
		LocalDependency<Double> number2Param = LocalDependency.toANumber("number2");
		ReferenceDependency<Double> number3Param = ReferenceDependency.toANumber("refToAnotherSchema", "number");

		@Override
		public Double calculate(CalculatorParameters parameters) {
			Double number1 = parameters.get(number1Param);
			Double number2 = parameters.get(number2Param);
			Double number3 = parameters.get(number3Param);
			return number1 + number2 + number3;
		}

		@Override
		public Double getDefaultValue() {
			return 0.0;
		}

		@Override
		public MetadataValueType getReturnType() {
			return NUMBER;
		}

		@Override
		public boolean isMultiValue() {
			return false;
		}

		@Override
		public List<? extends Dependency> getDependencies() {
			return Arrays.asList(number1Param, number2Param, number3Param);
		}
	}

	public static class RecordServicesOptimisticLockingHandlingAcceptanceTest_Calculator1
			extends RecordServicesOptimisticLockingHandlingAcceptanceTest_Calculator implements MetadataValueCalculator<Double> {

		@Override
		protected String getReferenceCode() {
			return "reference0";
		}
	}

	public static class RecordServicesOptimisticLockingHandlingAcceptanceTest_Calculator2
			extends RecordServicesOptimisticLockingHandlingAcceptanceTest_Calculator implements MetadataValueCalculator<Double> {

		@Override
		protected String getReferenceCode() {
			return "reference1";
		}
	}

	public static class RecordServicesOptimisticLockingHandlingAcceptanceTest_Calculator3
			extends RecordServicesOptimisticLockingHandlingAcceptanceTest_Calculator implements MetadataValueCalculator<Double> {

		@Override
		protected String getReferenceCode() {
			return "reference2";
		}
	}

	public static class RecordServicesOptimisticLockingHandlingAcceptanceTest_Calculator4
			extends RecordServicesOptimisticLockingHandlingAcceptanceTest_Calculator implements MetadataValueCalculator<Double> {

		@Override
		protected String getReferenceCode() {
			return "reference3";
		}
	}

	public static class RecordServicesOptimisticLockingHandlingAcceptanceTest_Calculator5
			extends RecordServicesOptimisticLockingHandlingAcceptanceTest_Calculator implements MetadataValueCalculator<Double> {

		@Override
		protected String getReferenceCode() {
			return "reference4";
		}
	}

	public static abstract class RecordServicesOptimisticLockingHandlingAcceptanceTest_Calculator
			implements MetadataValueCalculator<Double> {

		ReferenceDependency<Double> referenceDependency = ReferenceDependency.toANumber(getReferenceCode(), "number");

		@Override
		public Double calculate(CalculatorParameters parameters) {
			return parameters.get(referenceDependency);
		}

		@Override
		public Double getDefaultValue() {
			return null;
		}

		@Override
		public MetadataValueType getReturnType() {
			return MetadataValueType.NUMBER;
		}

		@Override
		public boolean isMultiValue() {
			return false;
		}

		@Override
		public List<? extends Dependency> getDependencies() {
			return asList(referenceDependency);
		}

		protected abstract String getReferenceCode();
	}
}
