package com.constellio.model.services.records;

import com.constellio.model.entities.calculators.AbstractMetadataValueCalculator;
import com.constellio.model.entities.calculators.CalculatorParameters;
import com.constellio.model.entities.calculators.DynamicDependencyValues;
import com.constellio.model.entities.calculators.dependencies.Dependency;
import com.constellio.model.entities.calculators.dependencies.DynamicLocalDependency;
import com.constellio.model.entities.records.Record;
import com.constellio.model.entities.schemas.Metadata;
import com.constellio.model.entities.schemas.MetadataValueType;
import com.constellio.model.services.records.RecordServicesRuntimeException.RecordServicesRuntimeException_CalculatorIsUsingAnForbiddenMetadata;
import com.constellio.model.services.records.RecordServicesRuntimeException.RecordServicesRuntimeException_ExceptionWhileCalculating;
import com.constellio.model.services.schemas.MetadataSchemaTypesAlteration;
import com.constellio.model.services.schemas.builders.MetadataSchemaBuilderRuntimeException;
import com.constellio.model.services.schemas.builders.MetadataSchemaTypesBuilder;
import com.constellio.sdk.tests.ConstellioTest;
import com.constellio.sdk.tests.TestRecord;
import com.constellio.sdk.tests.schemas.TestsSchemasSetup;
import com.constellio.sdk.tests.schemas.TestsSchemasSetup.AnotherSchemaMetadatas;
import com.constellio.sdk.tests.schemas.TestsSchemasSetup.ThirdSchemaMetadatas;
import com.constellio.sdk.tests.schemas.TestsSchemasSetup.ZeSchemaMetadatas;
import org.apache.commons.lang3.StringUtils;
import org.joda.time.LocalDate;
import org.joda.time.LocalDateTime;
import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

import static com.constellio.model.entities.schemas.MetadataValueType.NUMBER;
import static com.constellio.model.entities.schemas.MetadataValueType.STRING;
import static com.constellio.sdk.tests.schemas.TestsSchemasSetup.whichIsMultivalue;
import static com.constellio.sdk.tests.schemas.TestsSchemasSetup.whichIsReferencing;
import static java.util.Arrays.asList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.fail;

public class DynamicDependencyCalculatorAcceptanceTest extends ConstellioTest {

	private static AtomicInteger recalculationCounter = new AtomicInteger();

	private static LocalDate aDate = new LocalDate(2016, 11, 4);
	private static LocalDateTime aDateTime = new LocalDateTime(2016, 11, 4, 1, 2, 3);

	private String calculatedMetadata = "calculatedMetadata";
	private String anotherCalculatedMetadata = "anotherCalculatedMetadata";

	TestsSchemasSetup schemas = new TestsSchemasSetup(zeCollection);
	static ZeSchemaMetadatas zeSchema;
	static AnotherSchemaMetadatas anotherSchema;
	static ThirdSchemaMetadatas thirdSchema;

	String anotherSchemaRecordId = "42";

	RecordServices recordServices;

	@Before
	public void setUp()
			throws Exception {
		defineSchemasManager().using(schemas
				.withAStringMetadata()
				.withAnotherStringMetadata(whichIsMultivalue)
				.withADateMetadata()
				.withADateTimeMetadata()
				.withABooleanMetadata()
				.withAReferenceMetadata(whichIsReferencing("anotherSchemaType")));

		zeSchema = schemas.new ZeSchemaMetadatas();
		anotherSchema = schemas.new AnotherSchemaMetadatas();
		thirdSchema = schemas.new ThirdSchemaMetadatas();

		recordServices = getModelLayerFactory().newRecordServices();
		recordServices.add(new TestRecord(anotherSchema, anotherSchemaRecordId));
		recalculationCounter = new AtomicInteger();
	}

	@Test
	public void givenMetadataCalculatorHasADynamicDependencyOnSomeMetadatasThenOnlyRecalculatedWhenThoseMetadatasAreModified()
			throws Exception {

		schemas.modify(new MetadataSchemaTypesAlteration() {
			@Override
			public void alter(MetadataSchemaTypesBuilder types) {
				types.getSchema(zeSchema.code()).create(calculatedMetadata).setType(STRING)
						.defineDataEntry().asCalculated(CalculatorDependentOfSomeMetadatas.class);
			}
		});

		Record record = new TestRecord(zeSchema)
				.set(zeSchema.stringMetadata(), "toto");

		recordServices.add(record);
		assertThat(recalculationCounter.get()).isEqualTo(1);
		assertThat(record.<String>get(zeSchema.metadata(calculatedMetadata))).isEqualTo("toto,");

		recordServices.update(record.set(zeSchema.anotherStringMetadata(), asList("edouard")));
		assertThat(recalculationCounter.get()).isEqualTo(1);
		assertThat(record.<String>get(zeSchema.metadata(calculatedMetadata))).isEqualTo("toto,");

		recordServices.update(record.set(zeSchema.stringMetadata(), "test"));
		assertThat(recalculationCounter.get()).isEqualTo(2);
		assertThat(record.<String>get(zeSchema.metadata(calculatedMetadata))).isEqualTo("test,");

		recordServices.update(record.set(zeSchema.dateTimeMetadata(), aDateTime));
		assertThat(recalculationCounter.get()).isEqualTo(2);
		assertThat(record.<String>get(zeSchema.metadata(calculatedMetadata))).isEqualTo("test,");

		recordServices.update(record.set(zeSchema.booleanMetadata(), true));
		assertThat(recalculationCounter.get()).isEqualTo(3);
		assertThat(record.<String>get(zeSchema.metadata(calculatedMetadata))).isEqualTo("test,true");

		recordServices.update(record.set(zeSchema.referenceMetadata(), anotherSchemaRecordId));
		assertThat(recalculationCounter.get()).isEqualTo(3);
		assertThat(record.<String>get(zeSchema.metadata(calculatedMetadata))).isEqualTo("test,true");

		recordServices.update(record.set(zeSchema.booleanMetadata(), false));
		assertThat(recalculationCounter.get()).isEqualTo(4);
		assertThat(record.<String>get(zeSchema.metadata(calculatedMetadata))).isEqualTo("test,false");
	}

	@Test
	public void givenACalculatorTryToObtainAMetadataWhichIsNotIncludedInADynamicDependencyThenException()
			throws Exception {

		schemas.modify(new MetadataSchemaTypesAlteration() {
			@Override
			public void alter(MetadataSchemaTypesBuilder types) {
				types.getSchema(zeSchema.code()).create(calculatedMetadata).setType(STRING)
						.defineDataEntry().asCalculated(BadCalculator.class);
			}
		});

		Record record = new TestRecord(zeSchema)
				.set(zeSchema.stringMetadata(), "toto");

		try {
			recordServices.add(record);
			fail("Exception expected");
		} catch (RecordServicesRuntimeException_ExceptionWhileCalculating e) {
			assertThat(e)
					.hasRootCauseExactlyInstanceOf(RecordServicesRuntimeException_CalculatorIsUsingAnForbiddenMetadata.class);
		}
	}

	@Test
	public void givenMetadataCalculatorHasADynamicDependencyOnAllMetadatasThenAlwaysRecalculated()
			throws Exception {

		schemas.modify(new MetadataSchemaTypesAlteration() {
			@Override
			public void alter(MetadataSchemaTypesBuilder types) {
				types.getSchema(zeSchema.code()).create(calculatedMetadata).setType(STRING)
						.defineDataEntry().asCalculated(CalculatorDependentOfEveryMetadatas.class);
			}
		});

		Record record = new TestRecord(zeSchema)
				.set(zeSchema.stringMetadata(), "toto");

		recordServices.add(record);
		assertThat(recalculationCounter.get()).isEqualTo(1);
		assertThat(record.<String>get(zeSchema.metadata(calculatedMetadata)))
				.isEqualTo("toto,[],,,,");

		recordServices.update(record.set(zeSchema.anotherStringMetadata(), asList("edouard")));
		assertThat(recalculationCounter.get()).isEqualTo(2);
		assertThat(record.<String>get(zeSchema.metadata(calculatedMetadata)))
				.isEqualTo("toto,[edouard],,,,");

		recordServices.update(record.set(zeSchema.dateMetadata(), aDate));
		assertThat(recalculationCounter.get()).isEqualTo(3);
		assertThat(record.<String>get(zeSchema.metadata(calculatedMetadata)))
				.isEqualTo("toto,[edouard],2016-11-04,,,");

		recordServices.update(record.set(zeSchema.dateTimeMetadata(), aDateTime));
		assertThat(recalculationCounter.get()).isEqualTo(4);
		assertThat(record.<String>get(zeSchema.metadata(calculatedMetadata)))
				.isEqualTo("toto,[edouard],2016-11-04,2016-11-04T01:02:03.000,,");

		recordServices.update(record.set(zeSchema.booleanMetadata(), true));
		assertThat(recalculationCounter.get()).isEqualTo(5);
		assertThat(record.<String>get(zeSchema.metadata(calculatedMetadata)))
				.isEqualTo("toto,[edouard],2016-11-04,2016-11-04T01:02:03.000,true,");

		recordServices.update(record.set(zeSchema.referenceMetadata(), anotherSchemaRecordId));
		assertThat(recalculationCounter.get()).isEqualTo(6);
		assertThat(record.<String>get(zeSchema.metadata(calculatedMetadata)))
				.isEqualTo("toto,[edouard],2016-11-04,2016-11-04T01:02:03.000,true,42");
	}

	@Test
	public void givenMetadataCalculatorReturningAllAvailableMetadatasThenReturnAllAvailableEvenIfNullValue()
			throws Exception {

		schemas.modify(new MetadataSchemaTypesAlteration() {
			@Override
			public void alter(MetadataSchemaTypesBuilder types) {
				types.getSchema(zeSchema.code()).create(calculatedMetadata).setType(STRING)
						.defineDataEntry().asCalculated(CalculatorWhichReturnListOfAllAvailableMetadataLocalCodes.class);
			}
		});

		Record record = new TestRecord(zeSchema)
				.set(zeSchema.stringMetadata(), "toto");

		recordServices.add(record);
		assertThat(record.<String>get(zeSchema.metadata(calculatedMetadata)))
				.isEqualTo(
						"anotherStringMetadata,booleanMetadata,dateMetadata,dateTimeMetadata,referenceMetadata,stringMetadata");

	}

	@Test
	public void givenMetadataCalculatorReturningAllAvailableNonEmptyMetadatasThenReturnAllAvailableNonEmpty()
			throws Exception {

		schemas.modify(new MetadataSchemaTypesAlteration() {
			@Override
			public void alter(MetadataSchemaTypesBuilder types) {
				types.getSchema(zeSchema.code()).create(calculatedMetadata).setType(STRING)
						.defineDataEntry().asCalculated(CalculatorWhichReturnListOfAllAvailableMetadataWithValueLocalCodes.class);
			}
		});

		Record record = new TestRecord(zeSchema)
				.set(zeSchema.stringMetadata(), "toto");

		recordServices.add(record);
		assertThat(record.<String>get(zeSchema.metadata(calculatedMetadata)))
				.isEqualTo("stringMetadata");

		recordServices.add(record
				.set(zeSchema.anotherStringMetadata(), asList("aValue"))
				.set(zeSchema.booleanMetadata(), true));
		assertThat(record.<String>get(zeSchema.metadata(calculatedMetadata)))
				.isEqualTo("anotherStringMetadata,booleanMetadata,stringMetadata");

	}

	@Test(expected = MetadataSchemaBuilderRuntimeException.CyclicDependenciesInMetadata.class)
	public void whenConfiguringTwoCalculatedMetadataDynamicallyDependingOnEachOtherThenFails()
			throws Exception {

		schemas.modify(new MetadataSchemaTypesAlteration() {
			@Override
			public void alter(MetadataSchemaTypesBuilder types) {
				types.getSchema(zeSchema.code()).create(calculatedMetadata).setType(STRING)
						.defineDataEntry().asCalculated(CalculatorDependentOfEveryMetadatas.class);
				types.getSchema(zeSchema.code()).create(anotherCalculatedMetadata).setType(STRING)
						.defineDataEntry().asCalculated(CalculatorDependentOfEveryMetadatas.class);
			}
		});

	}

	@Test
	public void givenFiveCalculatorDependingOnEachOthersWithoutCyclicDependencyThenCalculatedCorrectly()
			throws Exception {

		schemas.modify(new MetadataSchemaTypesAlteration() {
			@Override
			public void alter(MetadataSchemaTypesBuilder types) {
				types.getSchema(zeSchema.code()).create("calculated1").setType(NUMBER)
						.defineDataEntry().asCalculated(Metadata1CalculatorDependingOn2_3_4.class);

				types.getSchema(zeSchema.code()).create("calculated2").setType(NUMBER)
						.defineDataEntry().asCalculated(Metadata2CalculatorDependingOnD_5.class);

				types.getSchema(zeSchema.code()).create("calculated3").setType(NUMBER)
						.defineDataEntry().asCalculated(Metadata3CalculatorDependingOn2_4.class);

				types.getSchema(zeSchema.code()).create("calculated4").setType(NUMBER)
						.defineDataEntry().asCalculated(Metadata4CalculatorDependingOnA_5.class);

				types.getSchema(zeSchema.code()).create("calculated5").setType(NUMBER)
						.defineDataEntry().asCalculated(Metadata5CalculatorDependingOnB_C.class);

				types.getSchema(zeSchema.code()).create("metadataA").setType(NUMBER);
				types.getSchema(zeSchema.code()).create("metadataB").setType(NUMBER);
				types.getSchema(zeSchema.code()).create("metadataC").setType(NUMBER);
				types.getSchema(zeSchema.code()).create("metadataD").setType(NUMBER);

			}
		});

		Record record = new TestRecord(zeSchema);
		record.set(zeSchema.metadata("metadataA"), 1);
		record.set(zeSchema.metadata("metadataB"), 4);
		record.set(zeSchema.metadata("metadataC"), 6);
		record.set(zeSchema.metadata("metadataD"), 100);
		recordServices.add(record);

		assertThat(record.<Double>get(zeSchema.metadata("calculated1"))).isEqualTo(242.0);
		assertThat(record.<Double>get(zeSchema.metadata("calculated2"))).isEqualTo(110.0);
		assertThat(record.<Double>get(zeSchema.metadata("calculated3"))).isEqualTo(121.0);
		assertThat(record.<Double>get(zeSchema.metadata("calculated4"))).isEqualTo(11.0);
		assertThat(record.<Double>get(zeSchema.metadata("calculated5"))).isEqualTo(10.0);

	}

	@Test(expected = MetadataSchemaBuilderRuntimeException.CyclicDependenciesInMetadata.class)
	public void givenFiveDynamicCalculatorDependingOnEachOthersWithCyclicDependencyThenCalculatedCorrectly()
			throws Exception {

		schemas.modify(new MetadataSchemaTypesAlteration() {
			@Override
			public void alter(MetadataSchemaTypesBuilder types) {
				types.getSchema(zeSchema.code()).create("calculated1").setType(NUMBER)
						.defineDataEntry().asJexlScript("calculated2+calculated3+calculated4");

				types.getSchema(zeSchema.code()).create("calculated2").setType(NUMBER)
						.defineDataEntry().asJexlScript("metadataD+calculated5");

				types.getSchema(zeSchema.code()).create("calculated3").setType(NUMBER)
						.defineDataEntry().asJexlScript("calculated2+calculated4");

				types.getSchema(zeSchema.code()).create("calculated4").setType(NUMBER)
						.defineDataEntry().asJexlScript("metadataA+calculated5");

				types.getSchema(zeSchema.code()).create("calculated5").setType(NUMBER)
						.defineDataEntry().asJexlScript("metadataB+metadataC+calculated1");

				types.getSchema(zeSchema.code()).create("metadataA").setType(NUMBER);

				types.getSchema(zeSchema.code()).create("metadataB").setType(NUMBER);
				types.getSchema(zeSchema.code()).create("metadataC").setType(NUMBER);
				types.getSchema(zeSchema.code()).create("metadataD").setType(NUMBER);

			}
		});
	}

	@Test
	public void givenFiveDynamicCalculatorDependingOnEachOthersWithoutCyclicDependencyThenCalculatedCorrectly()
			throws Exception {

		schemas.modify(new MetadataSchemaTypesAlteration() {
			@Override
			public void alter(MetadataSchemaTypesBuilder types) {
				types.getSchema(zeSchema.code()).create("calculated1").setType(NUMBER)
						.defineDataEntry().asJexlScript("calculated2+calculated3+calculated4");

				types.getSchema(zeSchema.code()).create("calculated2").setType(NUMBER)
						.defineDataEntry().asJexlScript("metadataD+calculated5");

				types.getSchema(zeSchema.code()).create("calculated3").setType(NUMBER)
						.defineDataEntry().asJexlScript("calculated2+calculated4");

				types.getSchema(zeSchema.code()).create("calculated4").setType(NUMBER)
						.defineDataEntry().asJexlScript("metadataA+calculated5");

				types.getSchema(zeSchema.code()).create("calculated5").setType(NUMBER)
						.defineDataEntry().asJexlScript("metadataB+metadataC");

				types.getSchema(zeSchema.code()).create("metadataA").setType(NUMBER);
				types.getSchema(zeSchema.code()).create("metadataB").setType(NUMBER);
				types.getSchema(zeSchema.code()).create("metadataC").setType(NUMBER);
				types.getSchema(zeSchema.code()).create("metadataD").setType(NUMBER);

			}
		});

		Record record = new TestRecord(zeSchema);
		record.set(zeSchema.metadata("metadataA"), 1);
		record.set(zeSchema.metadata("metadataB"), 4);
		record.set(zeSchema.metadata("metadataC"), 6);
		record.set(zeSchema.metadata("metadataD"), 100);
		recordServices.add(record);

		assertThat(record.<Double>get(zeSchema.metadata("calculated1"))).isEqualTo(242.0);
		assertThat(record.<Double>get(zeSchema.metadata("calculated2"))).isEqualTo(110.0);
		assertThat(record.<Double>get(zeSchema.metadata("calculated3"))).isEqualTo(121.0);
		assertThat(record.<Double>get(zeSchema.metadata("calculated4"))).isEqualTo(11.0);
		assertThat(record.<Double>get(zeSchema.metadata("calculated5"))).isEqualTo(10.0);

	}

	public static final class CalculatorDependentOfEveryMetadatas extends AbstractMetadataValueCalculator<String> {

		DynamicLocalDependency dynamicLocalDependency = new DynamicLocalDependency() {
			@Override
			public boolean isDependentOf(Metadata metadata, Metadata calculatedMetadata) {
				return true;
			}
		};

		@Override
		public String calculate(CalculatorParameters parameters) {

			DynamicDependencyValues values = parameters.get(dynamicLocalDependency);
			String stringMetadataValue = values.getValue(zeSchema.stringMetadata().getLocalCode());
			List<String> anotherStringMetadataValue = values.getValue(zeSchema.anotherStringMetadata().getLocalCode());
			LocalDate dateMetadataValue = values.getValue(zeSchema.dateMetadata().getLocalCode());
			LocalDateTime dateTimeMetadataValue = values.getValue(zeSchema.dateTimeMetadata().getLocalCode());
			Boolean booleanMetadataValue = values.getValue(zeSchema.booleanMetadata().getLocalCode());
			String referenceMetadataValue = values.getValue(zeSchema.referenceMetadata().getLocalCode());
			recalculationCounter.incrementAndGet();
			return StringUtils.join(new Object[]{stringMetadataValue, anotherStringMetadataValue, dateMetadataValue,
												 dateTimeMetadataValue, booleanMetadataValue, referenceMetadataValue}, ",");
		}

		@Override
		public String getDefaultValue() {
			return null;
		}

		@Override
		public MetadataValueType getReturnType() {
			return MetadataValueType.STRING;
		}

		@Override
		public boolean isMultiValue() {
			return false;
		}

		@Override
		public List<? extends Dependency> getDependencies() {
			return asList(dynamicLocalDependency);
		}
	}

	public static final class CalculatorDependentOfSomeMetadatas extends AbstractMetadataValueCalculator<String> {

		DynamicLocalDependency dynamicLocalDependency = new DynamicLocalDependency() {
			@Override
			public boolean isDependentOf(Metadata metadata, Metadata calculatedMetadata) {
				return metadata.getLocalCode().equals(zeSchema.stringMetadata().getLocalCode())
					   || metadata.getLocalCode().equals(zeSchema.booleanMetadata().getLocalCode());
			}
		};

		@Override
		public String calculate(CalculatorParameters parameters) {

			DynamicDependencyValues values = parameters.get(dynamicLocalDependency);
			String stringMetadataValue = values.getValue(zeSchema.stringMetadata().getLocalCode());
			Boolean booleanMetadataValue = values.getValue(zeSchema.booleanMetadata().getLocalCode());
			recalculationCounter.incrementAndGet();
			return StringUtils.join(new Object[]{stringMetadataValue, booleanMetadataValue}, ",");
		}

		@Override
		public String getDefaultValue() {
			return null;
		}

		@Override
		public MetadataValueType getReturnType() {
			return MetadataValueType.STRING;
		}

		@Override
		public boolean isMultiValue() {
			return false;
		}

		@Override
		public List<? extends Dependency> getDependencies() {
			return asList(dynamicLocalDependency);
		}
	}

	public static final class BadCalculator extends AbstractMetadataValueCalculator<String> {

		DynamicLocalDependency dynamicLocalDependency = new DynamicLocalDependency() {
			@Override
			public boolean isDependentOf(Metadata metadata, Metadata calculatedMetadata) {
				return metadata.getLocalCode().equals(zeSchema.stringMetadata().getLocalCode());
			}
		};

		@Override
		public String calculate(CalculatorParameters parameters) {

			DynamicDependencyValues values = parameters.get(dynamicLocalDependency);
			String stringMetadataValue = values.getValue(zeSchema.stringMetadata().getLocalCode());
			Boolean booleanMetadataValue = values.getValue(zeSchema.booleanMetadata().getLocalCode());
			return StringUtils.join(new Object[]{stringMetadataValue, booleanMetadataValue}, ",");
		}

		@Override
		public String getDefaultValue() {
			return null;
		}

		@Override
		public MetadataValueType getReturnType() {
			return MetadataValueType.STRING;
		}

		@Override
		public boolean isMultiValue() {
			return false;
		}

		@Override
		public List<? extends Dependency> getDependencies() {
			return asList(dynamicLocalDependency);
		}
	}

	public static final class CalculatorWhichReturnListOfAllAvailableMetadataLocalCodes
			extends AbstractMetadataValueCalculator<String> {

		DynamicLocalDependency dynamicLocalDependency = new DynamicLocalDependency() {
			@Override
			public boolean isDependentOf(Metadata metadata, Metadata calculatedMetadata) {
				return true;
			}
		};

		@Override
		public String calculate(CalculatorParameters parameters) {

			DynamicDependencyValues values = parameters.get(dynamicLocalDependency);

			List<Metadata> allMetadatas = values.getAvailableMetadatas();

			List<String> codes = new ArrayList<>(allMetadatas.stream().map((m) -> m.getLocalCode()).collect(Collectors.toList()));
			Collections.sort(codes);
			return StringUtils.join(codes, ",");
		}

		@Override
		public String getDefaultValue() {
			return null;
		}

		@Override
		public MetadataValueType getReturnType() {
			return MetadataValueType.STRING;
		}

		@Override
		public boolean isMultiValue() {
			return false;
		}

		@Override
		public List<? extends Dependency> getDependencies() {
			return asList(dynamicLocalDependency);
		}
	}

	public static final class CalculatorWhichReturnListOfAllAvailableMetadataWithValueLocalCodes
			extends AbstractMetadataValueCalculator<String> {

		DynamicLocalDependency dynamicLocalDependency = new DynamicLocalDependency() {
			@Override
			public boolean isDependentOf(Metadata metadata, Metadata calculatedMetadata) {
				return true;
			}
		};

		@Override
		public String calculate(CalculatorParameters parameters) {

			DynamicDependencyValues values = parameters.get(dynamicLocalDependency);

			List<Metadata> allMetadatas = values.getAvailableMetadatasWithAValue();

			List<String> codes = new ArrayList<>(allMetadatas.stream().map((m) -> m.getLocalCode()).collect(Collectors.toList()));
			Collections.sort(codes);
			return StringUtils.join(codes, ",");
		}

		@Override
		public String getDefaultValue() {
			return null;
		}

		@Override
		public MetadataValueType getReturnType() {
			return MetadataValueType.STRING;
		}

		@Override
		public boolean isMultiValue() {
			return false;
		}

		@Override
		public List<? extends Dependency> getDependencies() {
			return asList(dynamicLocalDependency);
		}
	}

	public static final class Metadata4CalculatorDependingOnA_5 extends AbstractCalculatorSumOfDependencies {

		public Metadata4CalculatorDependingOnA_5() {
			dynamicLocalDependency = new DynamicLocalDependency() {
				@Override
				public boolean isDependentOf(Metadata metadata, Metadata calculatedMetadata) {
					return asList("metadataA", "calculated5").contains(metadata.getLocalCode());
				}
			};
		}
	}

	public static final class Metadata2CalculatorDependingOnD_5 extends AbstractCalculatorSumOfDependencies {

		public Metadata2CalculatorDependingOnD_5() {
			dynamicLocalDependency = new DynamicLocalDependency() {
				@Override
				public boolean isDependentOf(Metadata metadata, Metadata calculatedMetadata) {
					return asList("metadataD", "calculated5").contains(metadata.getLocalCode());
				}
			};
		}
	}

	public static final class Metadata3CalculatorDependingOn2_4 extends AbstractCalculatorSumOfDependencies {

		public Metadata3CalculatorDependingOn2_4() {
			dynamicLocalDependency = new DynamicLocalDependency() {
				@Override
				public boolean isDependentOf(Metadata metadata, Metadata calculatedMetadata) {
					return asList("calculated2", "calculated4").contains(metadata.getLocalCode());
				}
			};
		}
	}

	public static final class Metadata1CalculatorDependingOn2_3_4 extends AbstractCalculatorSumOfDependencies {

		public Metadata1CalculatorDependingOn2_3_4() {
			dynamicLocalDependency = new DynamicLocalDependency() {
				@Override
				public boolean isDependentOf(Metadata metadata, Metadata calculatedMetadata) {
					return asList("calculated2", "calculated3", "calculated4").contains(metadata.getLocalCode());
				}
			};
		}
	}

	public static final class Metadata5CalculatorDependingOnB_C extends AbstractCalculatorSumOfDependencies {

		public Metadata5CalculatorDependingOnB_C() {
			dynamicLocalDependency = new DynamicLocalDependency() {
				@Override
				public boolean isDependentOf(Metadata metadata, Metadata calculatedMetadata) {
					return asList("metadataB", "metadataC").contains(metadata.getLocalCode());
				}
			};
		}
	}

	public static abstract class AbstractCalculatorSumOfDependencies
			extends AbstractMetadataValueCalculator<Double> {

		protected DynamicLocalDependency dynamicLocalDependency;

		@Override
		public Double calculate(CalculatorParameters parameters) {

			DynamicDependencyValues values = parameters.get(dynamicLocalDependency);

			List<Metadata> allMetadatas = values.getAvailableMetadatasWithAValue();

			double sum = 0;
			for (Metadata metadata : allMetadatas) {
				double value = values.getValue(metadata);
				sum += value;
			}

			return sum;
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
			return asList(dynamicLocalDependency);
		}
	}

}
