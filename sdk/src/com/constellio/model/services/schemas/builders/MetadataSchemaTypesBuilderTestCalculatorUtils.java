package com.constellio.model.services.schemas.builders;

import com.constellio.model.entities.calculators.CalculatorParameters;
import com.constellio.model.entities.calculators.MetadataValueCalculator;
import com.constellio.model.entities.calculators.dependencies.Dependency;
import com.constellio.model.entities.calculators.dependencies.LocalDependency;
import com.constellio.model.entities.calculators.dependencies.ReferenceDependency;
import com.constellio.model.entities.schemas.MetadataValueType;

import java.util.Arrays;
import java.util.List;

public class MetadataSchemaTypesBuilderTestCalculatorUtils {

	public static class CalculatorUsingM2 implements MetadataValueCalculator<String> {

		LocalDependency<String> dependency = LocalDependency.toAString("m2").whichIsRequired();

		@Override
		public String calculate(CalculatorParameters parameters) {
			throw new UnsupportedOperationException("TODO");
		}

		@Override
		public String getDefaultValue() {
			throw new UnsupportedOperationException("TODO");
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

	public static class CalculatorUsingM1 implements MetadataValueCalculator<String> {

		LocalDependency<String> dependency = LocalDependency.toAString("m1").whichIsRequired();

		@Override
		public String calculate(CalculatorParameters parameters) {
			throw new UnsupportedOperationException("TODO");
		}

		@Override
		public String getDefaultValue() {
			throw new UnsupportedOperationException("TODO");
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

	public static class DummyTextCalculatorUsingNumber implements MetadataValueCalculator<String> {

		LocalDependency<Double> dependency = LocalDependency.toANumber("other");

		@Override
		public String calculate(CalculatorParameters parameters) {
			return parameters.get(dependency) + "";
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

	public static class DummyNumberCalculator implements MetadataValueCalculator<Number> {

		LocalDependency<Double> dependency = LocalDependency.toANumber("other");

		@Override
		public Number calculate(CalculatorParameters parameters) {
			return parameters.get(dependency);
		}

		@Override
		public Number getDefaultValue() {
			return -1;
		}

		@Override
		public MetadataValueType getReturnType() {
			return MetadataValueType.NUMBER;
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

	public static class DummyNumberCalculator2 implements MetadataValueCalculator<Number> {

		LocalDependency<Double> dependency1 = LocalDependency.toANumber("dependency1");
		LocalDependency<Double> dependency2 = LocalDependency.toANumber("dependency2");

		@Override
		public Number calculate(CalculatorParameters parameters) {
			return parameters.get(dependency1) - parameters.get(dependency2);
		}

		@Override
		public Number getDefaultValue() {
			return -1;
		}

		@Override
		public MetadataValueType getReturnType() {
			return MetadataValueType.NUMBER;
		}

		@Override
		public List<? extends Dependency> getDependencies() {
			return Arrays.asList(dependency1, dependency2);
		}

		@Override
		public boolean isMultiValue() {
			return false;
		}
	}

	public static class DummyReferenceNumberCalculator implements MetadataValueCalculator<Number> {

		ReferenceDependency<Double> dependencyParam = ReferenceDependency.toANumber("dependencyRef", "dependencyMeta");
		LocalDependency<Double> localDependencyMeta = LocalDependency.toANumber("localDependencyMeta");

		@Override
		public Number calculate(CalculatorParameters parameters) {
			return parameters.get(dependencyParam) - parameters.get(localDependencyMeta);
		}

		@Override
		public Number getDefaultValue() {
			return -1;
		}

		@Override
		public MetadataValueType getReturnType() {
			return MetadataValueType.NUMBER;
		}

		@Override
		public List<? extends Dependency> getDependencies() {
			return Arrays.asList(dependencyParam, localDependencyMeta);
		}

		@Override
		public boolean isMultiValue() {
			return false;
		}
	}

	public static class DummyNoDependenciesCalculator implements MetadataValueCalculator<String> {

		@Override
		public String calculate(CalculatorParameters parameters) {
			return null;
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
			return Arrays.asList();
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
}
