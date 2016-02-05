package com.constellio.model.services.batch.controller;

import static com.constellio.model.entities.schemas.MetadataValueType.NUMBER;
import static com.constellio.model.entities.schemas.MetadataValueType.REFERENCE;
import static com.constellio.model.entities.schemas.MetadataValueType.STRING;

import java.util.Arrays;
import java.util.List;

import com.constellio.model.entities.calculators.CalculatorParameters;
import com.constellio.model.entities.calculators.MetadataValueCalculator;
import com.constellio.model.entities.calculators.dependencies.Dependency;
import com.constellio.model.entities.calculators.dependencies.LocalDependency;
import com.constellio.model.entities.schemas.Metadata;
import com.constellio.model.entities.schemas.MetadataValueType;
import com.constellio.model.services.schemas.builders.MetadataBuilder;
import com.constellio.sdk.tests.schemas.TestsSchemasSetup;

public class BatchProcessControllerAcceptanceTestSchemasSetup extends TestsSchemasSetup {

	public BatchProcessControllerAcceptanceTestSchemasSetup withCopiedTextMetadataFromAnotherSchema() {
		MetadataBuilder zeSchemaText = zeDefaultSchemaBuilder.create("text").setType(STRING);

		MetadataBuilder referenceToZeSchema = anOtherDefaultSchemaBuilder.create("referenceToZeSchema");
		referenceToZeSchema.setType(REFERENCE).defineReferences().set(zeSchemaTypeBuilder);

		MetadataBuilder anotherSchemaCopiedText = anOtherDefaultSchemaBuilder.create("copiedTextMetadata")
				.setType(STRING).defineDataEntry().asCopied(referenceToZeSchema, zeSchemaText);
		anOtherDefaultSchemaBuilder.create("copiedTextLengthMetadata").setType(NUMBER).defineDataEntry()
				.asCalculated(TextMetadataLengthCalculator.class);

		MetadataBuilder referenceToAnotherSchema = athirdDefaultSchemaBuilder
				.create("referenceToAnotherSchema");
		referenceToAnotherSchema.setType(REFERENCE).defineReferences().set(anOtherSchemaTypeBuilder);

		athirdDefaultSchemaBuilder.create("copiedTextMetadata").setType(STRING).defineDataEntry()
				.asCopied(referenceToAnotherSchema, anotherSchemaCopiedText);
		athirdDefaultSchemaBuilder.create("copiedTextLengthMetadata").setType(NUMBER).defineDataEntry()
				.asCalculated(TextMetadataLengthCalculator.class);

		return this;
	}

	public static class TextMetadataLengthCalculator implements MetadataValueCalculator<Double> {

		LocalDependency<String> copiedTextDependency = LocalDependency.toAString("copiedTextMetadata").whichIsRequired();

		@Override
		public Double calculate(CalculatorParameters parameters) {
			return (double) parameters.get(copiedTextDependency).length();
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
		public List<? extends Dependency> getDependencies() {
			return Arrays.asList(copiedTextDependency);
		}

		@Override
		public boolean isMultiValue() {
			return false;
		}

	}

	public class ZeSchemaMetadatas extends TestsSchemasSetup.ZeSchemaMetadatas {

		public Metadata text() {
			return getMetadata(code() + "_text");
		}

	}

	public class AnotherSchemaMetadatas extends TestsSchemasSetup.AnotherSchemaMetadatas {

		public Metadata copiedText() {
			return getMetadata(code() + "_copiedTextMetadata");
		}

		public Metadata copiedTextLength() {
			return getMetadata(code() + "_copiedTextLengthMetadata");
		}

		public Metadata referenceToZeSchema() {
			return getMetadata(code() + "_referenceToZeSchema");
		}

	}

	public class ThirdSchemaMetadatas extends TestsSchemasSetup.ThirdSchemaMetadatas {

		public Metadata copiedText() {
			return getMetadata(code() + "_copiedTextMetadata");
		}

		public Metadata copiedTextLength() {
			return getMetadata(code() + "_copiedTextLengthMetadata");
		}

		public Metadata referenceToAnotherSchema() {
			return getMetadata(code() + "_referenceToAnotherSchema");
		}

	}
}
