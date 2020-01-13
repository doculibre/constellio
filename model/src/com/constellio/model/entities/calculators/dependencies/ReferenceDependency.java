package com.constellio.model.entities.calculators.dependencies;

import com.constellio.model.entities.schemas.MetadataValueType;
import org.joda.time.LocalDate;
import org.joda.time.LocalDateTime;

import java.util.List;
import java.util.SortedMap;

public class ReferenceDependency<T> implements Dependency {

	final String referenceMetadataCode;

	final String dependentMetadataCode;

	final boolean required;

	final boolean multivalue;

	final MetadataValueType returnType;

	final boolean groupedByReference;

	final boolean metadataCreatedLater;

	private ReferenceDependency(String referenceMetadataCode, String dependentMetadataCode,
								MetadataValueType returnType) {
		super();
		this.referenceMetadataCode = referenceMetadataCode;
		this.dependentMetadataCode = dependentMetadataCode;
		this.required = false;
		this.multivalue = false;
		this.returnType = returnType;
		this.groupedByReference = false;
		this.metadataCreatedLater = false;
	}

	public ReferenceDependency(String referenceMetadataCode, String dependentMetadataCode, boolean required,
							   boolean multivalue,
							   MetadataValueType returnType, boolean groupedByReference, boolean metadataCreatedLater) {
		super();
		this.referenceMetadataCode = referenceMetadataCode;
		this.dependentMetadataCode = dependentMetadataCode;
		this.required = required;
		this.multivalue = multivalue;
		this.returnType = returnType;
		this.groupedByReference = groupedByReference;
		this.metadataCreatedLater = metadataCreatedLater;
	}

	public boolean isGroupedByReference() {
		return groupedByReference;
	}

	public boolean isMetadataCreatedLater() {
		return metadataCreatedLater;
	}

	public <Z> ReferenceDependency<Z> whichIsCreatedLater() {
		return new ReferenceDependency<>(referenceMetadataCode, dependentMetadataCode, required, multivalue, returnType,
				groupedByReference, true);
	}

	public <Z> ReferenceDependency<Z> whichIsRequired() {
		return new ReferenceDependency<>(referenceMetadataCode, dependentMetadataCode, true, multivalue, returnType,
				groupedByReference, metadataCreatedLater);
	}

	public <Z> ReferenceDependency<SortedMap<String, List<Z>>> whichAreReferencedMultiValueGroupedByReference() {
		return new ReferenceDependency<>(referenceMetadataCode, dependentMetadataCode, required, true, returnType, true,
				metadataCreatedLater);
	}

	public <Z> ReferenceDependency<SortedMap<String, Z>> whichAreReferencedSingleValueGroupedByReference() {
		return new ReferenceDependency<>(referenceMetadataCode, dependentMetadataCode, required, true, returnType, true,
				metadataCreatedLater);
	}

	public <Z> ReferenceDependency<List<Z>> whichIsMultivalue() {
		return new ReferenceDependency<>(referenceMetadataCode, dependentMetadataCode, required, true, returnType,
				groupedByReference, metadataCreatedLater);
	}

	//@formatter:off

	public static <Z> ReferenceDependency<Z> toAnEnum(String referenceMetadataCode, String dependentMetadataCode) {
		return new ReferenceDependency<>(referenceMetadataCode, dependentMetadataCode, false, false, MetadataValueType.ENUM, false, false);
	}

	public static ReferenceDependency<String> toAString(String referenceMetadataCode, String dependentMetadataCode) {
		return new ReferenceDependency<>(referenceMetadataCode, dependentMetadataCode, false, false, MetadataValueType.STRING, false, false);
	}

	public static ReferenceDependency<String> toAReference(String referenceMetadataCode, String dependentMetadataCode) {
		return new ReferenceDependency<>(referenceMetadataCode, dependentMetadataCode, false, false, MetadataValueType.REFERENCE, false, false);
	}

	public static ReferenceDependency<Boolean> toABoolean(String referenceMetadataCode, String dependentMetadataCode) {
		return new ReferenceDependency<>(referenceMetadataCode, dependentMetadataCode, false, false, MetadataValueType.BOOLEAN, false, false);
	}

	public static ReferenceDependency<Double> toANumber(String referenceMetadataCode, String dependentMetadataCode) {
		return new ReferenceDependency<>(referenceMetadataCode, dependentMetadataCode, false, false, MetadataValueType.NUMBER, false, false);
	}

	public static ReferenceDependency<LocalDate> toADate(String referenceMetadataCode, String dependentMetadataCode) {
		return new ReferenceDependency<>(referenceMetadataCode, dependentMetadataCode, false, false, MetadataValueType.DATE, false, false);
	}

	public static ReferenceDependency<LocalDateTime> toADateTime(String referenceMetadataCode,
																 String dependentMetadataCode) {
		return new ReferenceDependency<>(referenceMetadataCode, dependentMetadataCode, false, false, MetadataValueType.DATE_TIME, false, false);
	}

	public static <T> ReferenceDependency<T> toAStructure(String referenceMetadataCode, String dependentMetadataCode) {
		return new ReferenceDependency<>(referenceMetadataCode, dependentMetadataCode, false, false, MetadataValueType.STRUCTURE, false, false);
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
	public boolean equals(Object o) {
		if (this == o) {
			return true;
		}
		if (o == null || getClass() != o.getClass()) {
			return false;
		}

		ReferenceDependency<?> that = (ReferenceDependency<?>) o;

		if (!referenceMetadataCode.equals(that.referenceMetadataCode)) {
			return false;
		}
		return dependentMetadataCode.equals(that.dependentMetadataCode);
	}

	@Override
	public String toString() {
		return "ReferenceDependency{" +
			   "referenceMetadataCode='" + referenceMetadataCode + '\'' +
			   ", dependentMetadataCode='" + dependentMetadataCode + '\'' +
			   '}';
	}
}
