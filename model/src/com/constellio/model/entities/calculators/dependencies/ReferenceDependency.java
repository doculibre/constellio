package com.constellio.model.entities.calculators.dependencies;

import java.util.List;
import java.util.SortedMap;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.joda.time.LocalDate;
import org.joda.time.LocalDateTime;

import com.constellio.model.entities.schemas.MetadataValueType;

public class ReferenceDependency<T> implements Dependency {

	final String referenceMetadataCode;

	final String dependentMetadataCode;

	final boolean required;

	final boolean multivalue;

	final MetadataValueType returnType;

	final boolean groupedByReference;

	private ReferenceDependency(String referenceMetadataCode, String dependentMetadataCode, MetadataValueType returnType) {
		super();
		this.referenceMetadataCode = referenceMetadataCode;
		this.dependentMetadataCode = dependentMetadataCode;
		this.required = false;
		this.multivalue = false;
		this.returnType = returnType;
		this.groupedByReference = false;
	}

	private ReferenceDependency(String referenceMetadataCode, String dependentMetadataCode, boolean required, boolean multivalue,
			MetadataValueType returnType, boolean groupedByReference) {
		super();
		this.referenceMetadataCode = referenceMetadataCode;
		this.dependentMetadataCode = dependentMetadataCode;
		this.required = required;
		this.multivalue = multivalue;
		this.returnType = returnType;
		this.groupedByReference = groupedByReference;
	}

	public boolean isGroupedByReference() {
		return groupedByReference;
	}

	public <Z> ReferenceDependency<Z> whichIsRequired() {
		return new ReferenceDependency<>(referenceMetadataCode, dependentMetadataCode, true, multivalue, returnType,
				groupedByReference);
	}

	public <Z> ReferenceDependency<SortedMap<String, List<Z>>> whichAreReferencedMultiValueGroupedByReference() {
		return new ReferenceDependency<>(referenceMetadataCode, dependentMetadataCode, required, true, returnType, true);
	}

	public <Z> ReferenceDependency<SortedMap<String, Z>> whichAreReferencedSingleValueGroupedByReference() {
		return new ReferenceDependency<>(referenceMetadataCode, dependentMetadataCode, required, true, returnType, true);
	}

	public <Z> ReferenceDependency<List<Z>> whichIsMultivalue() {
		return new ReferenceDependency<>(referenceMetadataCode, dependentMetadataCode, required, true, returnType,
				groupedByReference);
	}

	//@formatter:off

		public static <Z> ReferenceDependency<Z> toAnEnum(String referenceMetadataCode, String dependentMetadataCode) {
		return new ReferenceDependency<>(referenceMetadataCode, dependentMetadataCode, false, false, MetadataValueType.ENUM, false);
	}

	public static ReferenceDependency<String> toAString(String referenceMetadataCode, String dependentMetadataCode) {
		return new ReferenceDependency<>(referenceMetadataCode, dependentMetadataCode, false, false, MetadataValueType.STRING, false);
	}

	public static ReferenceDependency<String> toAReference(String referenceMetadataCode, String dependentMetadataCode) {
		return new ReferenceDependency<>(referenceMetadataCode, dependentMetadataCode, false, false, MetadataValueType.REFERENCE, false);
	}

	public static ReferenceDependency<Boolean> toABoolean(String referenceMetadataCode, String dependentMetadataCode) {
		return new ReferenceDependency<>(referenceMetadataCode, dependentMetadataCode, false, false, MetadataValueType.BOOLEAN, false);
	}

	public static ReferenceDependency<Double> toANumber(String referenceMetadataCode, String dependentMetadataCode) {
		return new ReferenceDependency<>(referenceMetadataCode, dependentMetadataCode, false, false, MetadataValueType.NUMBER, false);
	}

	public static ReferenceDependency<LocalDate> toADate(String referenceMetadataCode, String dependentMetadataCode) {
		return new ReferenceDependency<>(referenceMetadataCode, dependentMetadataCode, false, false, MetadataValueType.DATE, false);
	}

	public static ReferenceDependency<LocalDateTime> toADateTime(String referenceMetadataCode, String dependentMetadataCode) {
		return new ReferenceDependency<>(referenceMetadataCode, dependentMetadataCode, false, false, MetadataValueType.DATE_TIME, false);
	}

	public static <T> ReferenceDependency<T> toAStructure(String referenceMetadataCode, String dependentMetadataCode) {
		return new ReferenceDependency<>(referenceMetadataCode, dependentMetadataCode, false, false, MetadataValueType.STRUCTURE, false);
	}


	//@formatter:on

	@Override
	public String getLocalMetadataCode() {
		return referenceMetadataCode;
	}

	public String getDependentMetadataCode() {
		return dependentMetadataCode;
	}

	@Override
	public MetadataValueType getReturnType() {
		return returnType;
	}

	@Override
	public boolean isMultivalue() {
		return multivalue;
	}

	@Override
	public boolean isRequired() {
		return required;
	}

	@Override
	public int hashCode() {
		return (referenceMetadataCode + dependentMetadataCode).hashCode();
	}

	@Override
	public boolean equals(Object obj) {
		return EqualsBuilder.reflectionEquals(this, obj);
	}

	@Override
	public String toString() {
		return "ReferenceDependency{" +
				"referenceMetadataCode='" + referenceMetadataCode + '\'' +
				", dependentMetadataCode='" + dependentMetadataCode + '\'' +
				'}';
	}
}
