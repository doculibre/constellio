package com.constellio.model.services.records;

import static com.constellio.model.entities.schemas.MetadataValueType.NUMBER;
import static com.constellio.model.entities.schemas.MetadataValueType.REFERENCE;
import static com.constellio.model.entities.schemas.MetadataValueType.STRING;

import java.util.Arrays;
import java.util.List;

import com.constellio.model.entities.calculators.CalculatorParameters;
import com.constellio.model.entities.calculators.MetadataValueCalculator;
import com.constellio.model.entities.calculators.dependencies.Dependency;
import com.constellio.model.entities.calculators.dependencies.LocalDependency;
import com.constellio.model.entities.schemas.MetadataValueType;
import com.constellio.model.services.schemas.builders.MetadataBuilder;
import com.constellio.model.services.schemas.builders.MetadataSchemaTypeBuilder;
import com.constellio.model.services.schemas.builders.MetadataSchemaTypesBuilder;
import com.constellio.sdk.tests.schemas.MetadataBuilderConfigurator;

public class RecordServicesAcceptanceTestUtils {

	public static MetadataBuilderConfigurator calculatedTextFromDummyCalculator() {
		return new MetadataBuilderConfigurator() {

			@Override
			public void configure(MetadataBuilder builder, MetadataSchemaTypesBuilder schemaTypes) {
				schemaTypes.getSchema("zeSchemaType_default").create("other").setType(STRING);
				builder.defineDataEntry().asCalculated(DummyTextCalculator.class);
			}
		};
	}

	public static MetadataBuilderConfigurator calculatedTextListFromDummyCalculator() {
		return new MetadataBuilderConfigurator() {

			@Override
			public void configure(MetadataBuilder builder, MetadataSchemaTypesBuilder schemaTypes) {
				schemaTypes.getSchema("zeSchemaType_default").create("other").setType(STRING).setMultivalue(true);
				builder.setMultivalue(true).defineDataEntry().asCalculated(DummyTextListCalculator.class);
			}
		};
	}

	public static MetadataBuilderConfigurator calculatedTextListFromDummyCalculatorReturningInvalidType() {
		return new MetadataBuilderConfigurator() {

			@Override
			public void configure(MetadataBuilder builder, MetadataSchemaTypesBuilder schemaTypes) {
				schemaTypes.getSchema("zeSchemaType_default").create("other").setType(NUMBER);
				builder.defineDataEntry().asCalculated(DummyCalculatorReturningInvalidType.class.getName());
			}
		};
	}

	public static MetadataBuilderConfigurator calculatedReferenceFromDummyCalculatorUsingOtherMetadata() {
		return new MetadataBuilderConfigurator() {

			@Override
			public void configure(MetadataBuilder builder, MetadataSchemaTypesBuilder schemaTypes) {
				MetadataSchemaTypeBuilder zeType = schemaTypes.getSchemaType("zeSchemaType");
				MetadataSchemaTypeBuilder anotherType = schemaTypes.getSchemaType("anotherSchemaType");
				MetadataSchemaTypeBuilder thirdType = schemaTypes.getSchemaType("aThirdSchemaType");

				schemaTypes.getSchema("zeSchemaType_default").create("other").setType(REFERENCE)
						.defineReferences().set(anotherType);
				builder.defineDataEntry().asCalculated(DummyCalculatorReturningReference.class.getName());
			}
		};
	}

	public static class DummyTextCalculator implements MetadataValueCalculator<String> {

		LocalDependency<String> dependency = LocalDependency.toAString("other");

		@Override
		public String calculate(CalculatorParameters parameters) {
			return parameters.get(dependency);
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
		public List<? extends Dependency> getDependencies() {
			return Arrays.asList(dependency);
		}

		@Override
		public boolean isMultiValue() {
			return false;
		}

	}

	public static class DummyTextListCalculator implements MetadataValueCalculator<List<String>> {

		LocalDependency<List<String>> dependency = LocalDependency.toAStringList("other");

		@Override
		public List<String> calculate(CalculatorParameters parameters) {
			return parameters.get(dependency);
		}

		@Override
		public List<String> getDefaultValue() {
			return null;
		}

		@Override
		public MetadataValueType getReturnType() {
			return MetadataValueType.STRING;
		}

		@Override
		public List<? extends Dependency> getDependencies() {
			return Arrays.asList(dependency);
		}

		@Override
		public boolean isMultiValue() {
			return true;
		}
	}

	@SuppressWarnings("rawtypes")
	public static class DummyCalculatorReturningInvalidType implements MetadataValueCalculator {

		LocalDependency dependency = LocalDependency.toANumber("other").whichIsRequired();

		@SuppressWarnings("unchecked")
		@Override
		public Object calculate(CalculatorParameters parameters) {
			return parameters.get(dependency);
		}

		@Override
		public Object getDefaultValue() {
			return null;
		}

		@Override
		public MetadataValueType getReturnType() {
			return MetadataValueType.STRING;
		}

		@Override
		public List<? extends Dependency> getDependencies() {
			return Arrays.asList(dependency);
		}

		@Override
		public boolean isMultiValue() {
			return false;
		}
	}

	public static class DummyCalculatorReturningReference implements MetadataValueCalculator<String> {

		LocalDependency<String> dependency = LocalDependency.toAReference("other");

		@Override
		public String calculate(CalculatorParameters parameters) {
			return parameters.get(dependency);
		}

		@Override
		public String getDefaultValue() {
			return null;
		}

		@Override
		public MetadataValueType getReturnType() {
			return MetadataValueType.REFERENCE;
		}

		@Override
		public List<? extends Dependency> getDependencies() {
			return Arrays.asList(dependency);
		}

		@Override
		public boolean isMultiValue() {
			return false;
		}
	}

}
