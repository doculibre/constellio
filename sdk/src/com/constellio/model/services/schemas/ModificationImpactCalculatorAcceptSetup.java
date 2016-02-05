package com.constellio.model.services.schemas;

import static com.constellio.model.entities.schemas.MetadataValueType.DATE_TIME;
import static com.constellio.model.entities.schemas.MetadataValueType.STRING;
import static com.constellio.sdk.tests.TestUtils.asList;

import java.util.Arrays;
import java.util.List;

import com.constellio.model.entities.Taxonomy;
import com.constellio.model.entities.calculators.CalculatorParameters;
import com.constellio.model.entities.calculators.MetadataValueCalculator;
import com.constellio.model.entities.calculators.dependencies.Dependency;
import com.constellio.model.entities.calculators.dependencies.ReferenceDependency;
import com.constellio.model.entities.schemas.Metadata;
import com.constellio.model.entities.schemas.MetadataValueType;
import com.constellio.model.services.schemas.builders.MetadataBuilder;
import com.constellio.sdk.tests.schemas.TestsSchemasSetup;

public class ModificationImpactCalculatorAcceptSetup extends TestsSchemasSetup {

	public List<Taxonomy> taxonomies;

	public ModificationImpactCalculatorAcceptSetup withStringAndDateMetadataUsedForCopyAndCalculationInOtherSchemas()
			throws Exception {

		withABooleanMetadata();

		zeDefaultSchemaBuilder.create("stringMetadata").setType(STRING);
		MetadataBuilder dateMetadata = zeDefaultSchemaBuilder.create("dateTimeMetadata").setType(DATE_TIME);

		anOtherDefaultSchemaBuilder.create("reference1ToZeSchema")
				.defineReferencesTo(zeSchemaTypeBuilder);

		MetadataBuilder anotherSchemaReference2ToZeSchema = anOtherDefaultSchemaBuilder
				.create("reference2ToZeSchema").defineReferencesTo(zeSchemaTypeBuilder);

		MetadataBuilder aThirdSchemaReferenceToZeSchema = athirdDefaultSchemaBuilder.create(
				"referenceToZeSchema").defineReferencesTo(zeSchemaTypeBuilder);

		anOtherDefaultSchemaBuilder.create("calculatedNumberMetadata")
				.setType(MetadataValueType.NUMBER).defineDataEntry()
				.asCalculated(CalculatorUsingZeSchemaStringMetadata.class);

		aThirdSchemaTypeBuilder.getDefaultSchema().create("copiedDate")
				.setType(DATE_TIME).defineDataEntry().asCopied(aThirdSchemaReferenceToZeSchema, dateMetadata);

		aThirdSchemaTypeBuilder.createCustomSchema("custom1").create("custom1CopiedDate")
				.setType(DATE_TIME).defineDataEntry().asCopied(aThirdSchemaReferenceToZeSchema, dateMetadata);

		aThirdSchemaTypeBuilder.createCustomSchema("custom2").create("custom2CopiedDate")
				.setType(DATE_TIME).defineDataEntry().asCopied(aThirdSchemaReferenceToZeSchema, dateMetadata);

		anOtherDefaultSchemaBuilder.create("copiedDateMetadata").setType(DATE_TIME).defineDataEntry()
				.asCopied(anotherSchemaReference2ToZeSchema, dateMetadata);

		return this;
	}

	public ModificationImpactCalculatorAcceptSetup withTaxonomyOfZeSchemaAndAnotherSchemaTypes() {

		MetadataBuilder zeSchemaParent = zeSchemaTypeBuilder.getDefaultSchema().create("zeSchemaParent")
				.defineReferencesTo(zeSchemaTypeBuilder);
		MetadataBuilder anotherSchemaAnotherSchemaParent = zeSchemaTypeBuilder.getDefaultSchema()
				.create("anotherSchemaAnotherSchemaParent").defineReferencesTo(anOtherSchemaTypeBuilder);
		MetadataBuilder anotherSchemaZeSchemaParent = anOtherSchemaTypeBuilder.getDefaultSchema()
				.create("anotherSchemaZeSchemaParent").defineReferencesTo(zeSchemaTypeBuilder);

		List<String> relations = Arrays.asList(zeSchemaParent.getCode(),
				anotherSchemaAnotherSchemaParent.getCode(), anotherSchemaZeSchemaParent.getCode());
		List<String> taxonomySchemaTypes = Arrays.asList("zeSchemaType", "anotherSchemaType");
		Taxonomy taxonomy = Taxonomy.createPublic("myTaxonomy", "myTaxonomy", "zeCollection", taxonomySchemaTypes);
		taxonomies = Arrays.asList(taxonomy);
		return this;

	}

	public static class CalculatorUsingZeSchemaStringMetadata implements MetadataValueCalculator<Double>

	{

		ReferenceDependency<String> reference = ReferenceDependency
				.toAString("reference1ToZeSchema", "stringMetadata");

		@Override
		public Double calculate(CalculatorParameters parameters) {
			String parameter = parameters.get(reference);
			return Double.valueOf(parameter.length());
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
			return Arrays.asList(reference);
		}

	}

	public class ZeSchemaMetadatas extends TestsSchemasSetup.ZeSchemaMetadatas {

		public Metadata zeSchemaParent() {
			return getMetadata(code() + "_zeSchemaParent");
		}

	}

	public class AnotherSchemaMetadatas extends TestsSchemasSetup.AnotherSchemaMetadatas {

		public Metadata anotherSchemaParent() {
			return getMetadata(code() + "_anotherSchemaAnotherSchemaParent");
		}

		public Metadata zeSchemaParent() {
			return getMetadata(code() + "_anotherSchemaZeSchemaParent");
		}

		public List<Metadata> metadataUsingZeSchemaDateAndString() {

			return asList(anotherDefaultSchema().getMetadata("calculatedNumberMetadata"),
					anotherDefaultSchema().getMetadata("copiedDateMetadata"));
		}

		public Metadata reference1ToZeSchema() {

			return anotherDefaultSchema().getMetadata("reference1ToZeSchema");
		}

		public Metadata reference2ToZeSchema() {

			return anotherDefaultSchema().getMetadata("reference2ToZeSchema");
		}

	}

	public class ThirdSchemaMetadatas extends TestsSchemasSetup.ThirdSchemaMetadatas {
		public List<Metadata> metadataUsingZeSchemaDateAndString() {
			Metadata copiedDate = aThirdDefaultSchema().getMetadata("copiedDate");
			Metadata custom1CopiedDate = getSchema("aThirdSchemaType_custom1").getMetadata("custom1CopiedDate");
			Metadata custom2CopiedDate = getSchema("aThirdSchemaType_custom2").getMetadata("custom2CopiedDate");

			return asList(copiedDate, custom1CopiedDate, custom2CopiedDate);
		}

		public Metadata referenceToZeSchema() {

			return aThirdDefaultSchema().getMetadata("referenceToZeSchema");
		}

	}
}
