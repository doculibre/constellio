package com.constellio.model.entities.calculators.dependencies;

import com.constellio.model.entities.records.Content;
import com.constellio.model.entities.schemas.MetadataValueType;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.joda.time.LocalDate;
import org.joda.time.LocalDateTime;

import java.util.List;

public class LocalDependency<T> implements Dependency {

	final String metadataCode;

	final boolean multivalue;
	final boolean required;
	final MetadataValueType returnType;
	final boolean metadataCreatedLater;

	public LocalDependency(String metadataCode, boolean required, boolean multivalue, MetadataValueType returnType,
						   boolean metadataCreatedLater) {
		super();
		this.metadataCode = metadataCode;
		this.required = required;
		this.multivalue = multivalue;
		this.returnType = returnType;
		this.metadataCreatedLater = metadataCreatedLater;
	}

	public <Z> LocalDependency<Z> whichIsRequired() {
		return new LocalDependency<>(metadataCode, true, multivalue, returnType, metadataCreatedLater);
	}

	public <Z> LocalDependency<Z> whichIsCreatedLater() {
		return new LocalDependency<>(metadataCode, required, multivalue, returnType, true);
	}

	public <Z> LocalDependency<List<Z>> whichIsMultivalue() {
		return new LocalDependency<>(metadataCode, required, true, returnType, metadataCreatedLater);
	}

	public static LocalDependency<String> toAString(String metadataCode) {
		return new LocalDependency<>(metadataCode, false, false, MetadataValueType.STRING, false);
	}

	public static <T> LocalDependency<T> toAnEnum(String metadataCode) {
		return new LocalDependency<>(metadataCode, false, false, MetadataValueType.ENUM, false);
	}

	public static <T> LocalDependency<List<T>> toAnEnumList(String metadataCode) {
		return new LocalDependency<>(metadataCode, false, true, MetadataValueType.ENUM, false);
	}

	public static LocalDependency<List<String>> toAStringList(String metadataCode) {
		return new LocalDependency<>(metadataCode, false, true, MetadataValueType.STRING, false);
	}

	public static LocalDependency<List<String>> toARequiredStringList(String metadataCode) {
		return new LocalDependency<>(metadataCode, true, true, MetadataValueType.STRING, false);
	}

	public static LocalDependency<String> toAReference(String metadataCode) {
		return new LocalDependency<>(metadataCode, false, false, MetadataValueType.REFERENCE, false);
	}

	public static LocalDependency<List<String>> toAReferenceList(String metadataCode) {
		return new LocalDependency<>(metadataCode, false, true, MetadataValueType.REFERENCE, false);
	}

	public static LocalDependency<Boolean> toABoolean(String metadataCode) {
		return new LocalDependency<>(metadataCode, false, false, MetadataValueType.BOOLEAN, false);
	}

	public static LocalDependency<List<Boolean>> toABooleanList(String metadataCode) {
		return new LocalDependency<>(metadataCode, false, true, MetadataValueType.BOOLEAN, false);
	}

	public static LocalDependency<Double> toANumber(String metadataCode) {
		return new LocalDependency<>(metadataCode, false, false, MetadataValueType.NUMBER, false);
	}

	public static LocalDependency<LocalDate> toADate(String metadataCode) {
		return new LocalDependency<>(metadataCode, false, false, MetadataValueType.DATE, false);
	}

	public static LocalDependency<List<LocalDate>> toADateList(String metadataCode) {
		return new LocalDependency<>(metadataCode, false, true, MetadataValueType.DATE, false);
	}

	public static LocalDependency<LocalDateTime> toADateTime(String metadataCode) {
		return new LocalDependency<>(metadataCode, false, false, MetadataValueType.DATE_TIME, false);
	}

	public static <T> LocalDependency<T> toAStructure(String metadataCode) {
		return new LocalDependency<>(metadataCode, false, false, MetadataValueType.STRUCTURE, false);
	}

	public static LocalDependency<Content> toAContent(String metadataCode) {
		return new LocalDependency<>(metadataCode, false, false, MetadataValueType.CONTENT, false);
	}

	@Override
	public String getLocalMetadataCode() {
		return metadataCode;
	}

	@Override
	public MetadataValueType getReturnType() {
		return returnType;
	}

	@Override
	public boolean isRequired() {
		return required;
	}

	@Override
	public boolean isMultivalue() {
		return multivalue;
	}

	public boolean isMetadataCreatedLater() {
		return metadataCreatedLater;
	}

	@Override
	public int hashCode() {
		return metadataCode.hashCode();
	}

	@Override
	public boolean equals(Object obj) {
		return EqualsBuilder.reflectionEquals(this, obj);
	}

	@Override
	public String toString() {
		return "LocalDependency{" +
			   "metadataCode='" + metadataCode + '\'' +
			   '}';
	}

}
