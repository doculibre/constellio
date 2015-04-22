/*Constellio Enterprise Information Management

Copyright (c) 2015 "Constellio inc."

This program is free software: you can redistribute it and/or modify
it under the terms of the GNU Affero General Public License as
published by the Free Software Foundation, either version 3 of the
License, or (at your option) any later version.

This program is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
GNU Affero General Public License for more details.

You should have received a copy of the GNU Affero General Public License
along with this program. If not, see <http://www.gnu.org/licenses/>.
*/
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
