package com.constellio.app.services.importExport.settings.model;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

public class ImportedDataEntry {

	private String type;
	private String calculator;
	private String fixedSequenceCode;
	private String metadataProvidingSequenceCode;
	private String advancedSequenceCalculatorClass;
	private String pattern;
	private String referencedMetadata;
	private String copiedMetadata;

	private ImportedDataEntry() {
	}

	public static ImportedDataEntry asManual() {
		return new ImportedDataEntry().withType("manual");
	}

	public static ImportedDataEntry asFixedSequence(String fixedSequenceCode) {
		return new ImportedDataEntry().withType("sequence").withFixedSequenceCode(fixedSequenceCode);
	}

	public static ImportedDataEntry asMetadataProvidingSequence(String metadataProvidingSequenceCode) {
		return new ImportedDataEntry().withType("sequence").withMetadataProvidingSequenceCode(metadataProvidingSequenceCode);
	}

	public static ImportedDataEntry asAdvancedSequence(String advancedSequenceCalculatorClass) {
		return new ImportedDataEntry().withType("advancedSequence").withAdvancedSequenceCalculatorClass(advancedSequenceCalculatorClass);
	}

	public static ImportedDataEntry asJEXLScript(String pattern) {
		return new ImportedDataEntry().withType("jexl").withPattern(pattern);
	}

	public static ImportedDataEntry asCalculated(String calculatorQualifiedName) {
		return new ImportedDataEntry().withType("calculated").withCalculator(calculatorQualifiedName);
	}

	public static ImportedDataEntry asCopied(String referenceMetadata, String copiedMetadata) {
		return new ImportedDataEntry().withType("copied").withReferencedMetadata(referenceMetadata)
				.withCopiedMetadata(copiedMetadata);
	}

	public String getType() {
		return type;
	}

	public ImportedDataEntry withType(String type) {
		this.type = type;
		return this;
	}

	public String getCalculator() {
		return calculator;
	}

	public ImportedDataEntry withCalculator(String calculator) {
		this.calculator = calculator;
		return this;
	}

	public String getFixedSequenceCode() {
		return fixedSequenceCode;
	}

	public ImportedDataEntry withFixedSequenceCode(String fixedSequenceCode) {
		this.fixedSequenceCode = fixedSequenceCode;
		return this;
	}

	public String getMetadataProvidingSequenceCode() {
		return metadataProvidingSequenceCode;
	}

	public ImportedDataEntry withMetadataProvidingSequenceCode(String metadataProvidingSequenceCode) {
		this.metadataProvidingSequenceCode = metadataProvidingSequenceCode;
		return this;
	}

	public String getAdvancedSequenceCalculatorClass() {
		return advancedSequenceCalculatorClass;
	}

	public ImportedDataEntry withAdvancedSequenceCalculatorClass(String advancedSequenceCalculatorClass) {
		this.advancedSequenceCalculatorClass = advancedSequenceCalculatorClass;
		return this;
	}

	public String getPattern() {
		return pattern;
	}

	public ImportedDataEntry withPattern(String pattern) {
		this.pattern = pattern;
		return this;
	}

	private ImportedDataEntry withReferencedMetadata(String referenceMetadata) {
		this.referencedMetadata = referenceMetadata;
		return this;
	}

	public String getReferencedMetadata() {
		return referencedMetadata;
	}

	public ImportedDataEntry withCopiedMetadata(String copiedMetadata) {
		this.copiedMetadata = copiedMetadata;
		return this;
	}

	public String getCopiedMetadata() {
		return copiedMetadata;
	}

	@Override
	public boolean equals(Object obj) {
		return EqualsBuilder.reflectionEquals(this, obj);

	}

	@Override
	public int hashCode() {
		return HashCodeBuilder.reflectionHashCode(this);
	}

	@Override
	public String toString() {
		return ToStringBuilder.reflectionToString(this, ToStringStyle.SHORT_PREFIX_STYLE);
	}

}
