package com.constellio.model.services.schemas;

import static com.constellio.model.entities.schemas.MetadataValueType.DATE_TIME;
import static com.constellio.model.entities.schemas.MetadataValueType.NUMBER;
import static com.constellio.model.entities.schemas.MetadataValueType.REFERENCE;
import static com.constellio.model.entities.schemas.MetadataValueType.STRING;
import static com.constellio.model.entities.schemas.MetadataTransiency.TRANSIENT_LAZY;
import static com.constellio.sdk.tests.TestUtils.asList;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import com.constellio.model.entities.Taxonomy;
import com.constellio.model.entities.calculators.CalculatorParameters;
import com.constellio.model.entities.calculators.MetadataValueCalculator;
import com.constellio.model.entities.calculators.dependencies.Dependency;
import com.constellio.model.entities.calculators.dependencies.LocalDependency;
import com.constellio.model.entities.calculators.dependencies.ReferenceDependency;
import com.constellio.model.entities.schemas.Metadata;
import com.constellio.model.entities.schemas.MetadataValueType;
import com.constellio.model.entities.schemas.MetadataTransiency;
import com.constellio.model.entities.schemas.Schemas;
import com.constellio.model.services.schemas.builders.MetadataBuilder;
import com.constellio.sdk.tests.schemas.SchemasSetup;
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
				.setType(NUMBER).defineDataEntry()
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

	public ModificationImpactCalculatorAcceptSetup withComputedTitleSizeCopiedInAnotherSchema(MetadataTransiency mode)
			throws Exception {

		withAReferenceFromAnotherSchemaToZeSchema();

		MetadataBuilder titleLength = zeDefaultSchemaBuilder.create("titleLength").setType(NUMBER).setTransiency(mode)
				.defineDataEntry().asCalculated(TitleLengthCalculator.class);

		anOtherSchemaTypeBuilder.getDefaultSchema().create("copiedTitleLength").setType(NUMBER).defineDataEntry().asCopied(
				anOtherSchemaTypeBuilder.getDefaultSchema().get("referenceFromAnotherSchemaToZeSchema"), titleLength);

		return this;
	}

	public ModificationImpactCalculatorAcceptSetup withComputedTitleSizeCalculatedInAnotherSchema(MetadataTransiency mode)
			throws Exception {

		withAReferenceFromAnotherSchemaToZeSchema();

		zeDefaultSchemaBuilder.create("titleLength").setType(NUMBER).defineDataEntry().asCalculated(TitleLengthCalculator.class)
				.setTransiency(mode);

		anOtherSchemaTypeBuilder.getDefaultSchema().create("calculatedTitleLength").setType(NUMBER).defineDataEntry()
				.asCalculated(CalculatorCopyingZeSchemaTitleLengthPlusTwo.class);

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

	public static class CalculatorCopyingZeSchemaTitleLengthPlusTwo implements MetadataValueCalculator<Double>

	{

		ReferenceDependency<Double> reference = ReferenceDependency
				.toANumber("referenceFromAnotherSchemaToZeSchema", "titleLength").whichIsRequired();

		@Override
		public Double calculate(CalculatorParameters parameters) {
			Double parameter = parameters.get(reference);
			return parameter + 2;
		}

		@Override
		public Double getDefaultValue() {
			return 2.0;
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
			return Arrays.asList(reference);
		}

	}

	public static class CalculatorCopyingZeSchemaTitleLengths implements MetadataValueCalculator<List<Double>>

	{

		ReferenceDependency<List<Double>> reference = ReferenceDependency
				.toANumber("referenceFromAnotherSchemaToZeSchema", "titleLength").whichIsRequired().whichIsMultivalue();

		@Override
		public List<Double> calculate(CalculatorParameters parameters) {
			List<Double> parameter = parameters.get(reference);
			return parameter;
		}

		@Override
		public List<Double> getDefaultValue() {
			return new ArrayList<>();
		}

		@Override
		public MetadataValueType getReturnType() {
			return NUMBER;
		}

		@Override
		public boolean isMultiValue() {
			return true;
		}

		@Override
		public List<? extends Dependency> getDependencies() {
			return Arrays.asList(reference);
		}

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
			return NUMBER;
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

	public SchemasSetup withReferenceFromAnotherSchemaToZeSchemaComputedFromStringMetadata(
			MetadataTransiency transiency) {

		anOtherSchemaTypeBuilder.getDefaultSchema().create("aString").setType(STRING);
		anOtherSchemaTypeBuilder.getDefaultSchema().get("referenceFromAnotherSchemaToZeSchema").setTransiency(TRANSIENT_LAZY)
				.defineDataEntry().asCalculated(ZeReferenceToZeSchemaCalculator.class);

		return this;
	}

	public SchemasSetup withTransientMultivalueReferenceUsedByCopiedMetadata(
			MetadataTransiency transiency)
			throws Exception {

		withAReferenceFromAnotherSchemaToZeSchema();

		MetadataBuilder titleLength = zeDefaultSchemaBuilder.create("titleLength").setType(NUMBER).setTransiency(transiency)
				.defineDataEntry().asCalculated(TitleLengthCalculator.class);

		anOtherSchemaTypeBuilder.getDefaultSchema().create("copiedTitleLength").setType(NUMBER).setMultivalue(true)
				.defineDataEntry()
				.asCopied(anOtherSchemaTypeBuilder.getDefaultSchema().get("referenceFromAnotherSchemaToZeSchema"), titleLength);

		anOtherSchemaTypeBuilder.getDefaultSchema().create("aString").setType(STRING).setMultivalue(true);
		anOtherSchemaTypeBuilder.getDefaultSchema().get("referenceFromAnotherSchemaToZeSchema").setMultivalue(true)
				.setTransiency(transiency).defineDataEntry().asCalculated(MultivalueZeReferenceToZeSchemaCalculator.class);

		return this;
	}

	public SchemasSetup withTransientMultivalueReferenceUsedByCalculatedMetadata(
			MetadataTransiency transiency)
			throws Exception {

		withAReferenceFromAnotherSchemaToZeSchema();

		MetadataBuilder titleLength = zeDefaultSchemaBuilder.create("titleLength").setType(NUMBER).setTransiency(transiency)
				.defineDataEntry().asCalculated(TitleLengthCalculator.class);

		anOtherSchemaTypeBuilder.getDefaultSchema().create("calculatedTitleLength").setType(NUMBER).setMultivalue(true)
				.defineDataEntry()
				.asCalculated(CalculatorCopyingZeSchemaTitleLengths.class);

		anOtherSchemaTypeBuilder.getDefaultSchema().create("aString").setType(STRING).setMultivalue(true);
		anOtherSchemaTypeBuilder.getDefaultSchema().get("referenceFromAnotherSchemaToZeSchema").setMultivalue(true)
				.setTransiency(transiency).defineDataEntry().asCalculated(MultivalueZeReferenceToZeSchemaCalculator.class);

		return this;
	}

	public class ZeSchemaMetadatas extends TestsSchemasSetup.ZeSchemaMetadatas {

		public Metadata zeSchemaParent() {
			return getMetadata(code() + "_zeSchemaParent");
		}

	}

	public class AnotherSchemaMetadatas extends TestsSchemasSetup.AnotherSchemaMetadatas {

		public Metadata copiedZeSchemaTitleLength() {
			return getMetadata(code() + "_copiedTitleLength");
		}

		public Metadata calculatedZeSchemaTitleLengthPlusTwo() {
			return getMetadata(code() + "_calculatedTitleLength");
		}

		public Metadata calculatedZeSchemaTitlesLength() {
			return getMetadata(code() + "_calculatedTitleLength");
		}

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

	public static class ZeReferenceToZeSchemaCalculator implements MetadataValueCalculator<String> {

		LocalDependency<String> aStringDependency = LocalDependency.toAString("aString").whichIsRequired();

		@Override
		public String calculate(CalculatorParameters parameters) {
			String aString = parameters.get(aStringDependency);
			return aString;
		}

		@Override
		public String getDefaultValue() {
			return null;
		}

		@Override
		public MetadataValueType getReturnType() {
			return REFERENCE;
		}

		@Override
		public boolean isMultiValue() {
			return false;
		}

		@Override
		public List<? extends Dependency> getDependencies() {
			return Arrays.asList(aStringDependency);
		}
	}

	public static class MultivalueZeReferenceToZeSchemaCalculator implements MetadataValueCalculator<List<String>> {

		LocalDependency<List<String>> aStringDependency = LocalDependency.toAString("aString").whichIsRequired()
				.whichIsMultivalue();

		@Override
		public List<String> calculate(CalculatorParameters parameters) {
			List<String> aString = parameters.get(aStringDependency);
			return aString;
		}

		@Override
		public List<String> getDefaultValue() {
			return null;
		}

		@Override
		public MetadataValueType getReturnType() {
			return REFERENCE;
		}

		@Override
		public boolean isMultiValue() {
			return true;
		}

		@Override
		public List<? extends Dependency> getDependencies() {
			return Arrays.asList(aStringDependency);
		}
	}

	public static class TitleLengthCalculator implements MetadataValueCalculator<Double> {

		LocalDependency<String> titleDependency = LocalDependency.toAString(Schemas.TITLE_CODE).whichIsRequired();

		@Override
		public Double calculate(CalculatorParameters parameters) {
			String title = parameters.get(titleDependency);
			return Double.valueOf(title.length());
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
			return Arrays.asList(titleDependency);
		}
	}

}
