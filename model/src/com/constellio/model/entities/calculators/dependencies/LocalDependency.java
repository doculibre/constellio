package com.constellio.model.entities.calculators.dependencies;

import java.util.List;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.joda.time.LocalDate;
import org.joda.time.LocalDateTime;

import com.constellio.model.entities.records.Content;
import com.constellio.model.entities.schemas.MetadataValueType;

public class LocalDependency<T> implements Dependency {

	final String metadataCode;

	final boolean multivalue;
	final boolean required;
	final MetadataValueType returnType;

	public LocalDependency(String metadataCode, boolean required, boolean multivalue, MetadataValueType returnType) {
		super();
		this.metadataCode = metadataCode;
		this.required = required;
		this.multivalue = multivalue;
		this.returnType = returnType;
	}

	public <Z> LocalDependency<Z> whichIsRequired() {
		return new LocalDependency<>(metadataCode, true, multivalue, returnType);
	}

	public <Z> LocalDependency<List<Z>> whichIsMultivalue() {
		return new LocalDependency<>(metadataCode, required, true, returnType);
	}

	public static LocalDependency<String> toAString(String metadataCode) {
		return new LocalDependency<>(metadataCode, false, false, MetadataValueType.STRING);
	}

	public static <T> LocalDependency<T> toAnEnum(String metadataCode) {
		return new LocalDependency<>(metadataCode, false, false, MetadataValueType.ENUM);
	}

	public static LocalDependency<List<String>> toAStringList(String metadataCode) {
		return new LocalDependency<>(metadataCode, false, true, MetadataValueType.STRING);
	}

	public static LocalDependency<List<String>> toARequiredStringList(String metadataCode) {
		return new LocalDependency<>(metadataCode, true, true, MetadataValueType.STRING);
	}

	public static LocalDependency<String> toAReference(String metadataCode) {
		return new LocalDependency<>(metadataCode, false, false, MetadataValueType.REFERENCE);
	}

	public static LocalDependency<Boolean> toABoolean(String metadataCode) {
		return new LocalDependency<>(metadataCode, false, false, MetadataValueType.BOOLEAN);
	}

	public static LocalDependency<Double> toANumber(String metadataCode) {
		return new LocalDependency<>(metadataCode, false, false, MetadataValueType.NUMBER);
	}

	public static LocalDependency<LocalDate> toADate(String metadataCode) {
		return new LocalDependency<>(metadataCode, false, false, MetadataValueType.DATE);
	}

	public static LocalDependency<LocalDateTime> toADateTime(String metadataCode) {
		return new LocalDependency<>(metadataCode, false, false, MetadataValueType.DATE_TIME);
	}

	public static <T> LocalDependency<T> toAStructure(String metadataCode) {
		return new LocalDependency<>(metadataCode, false, false, MetadataValueType.STRUCTURE);
	}

	public static LocalDependency<Content> toAContent(String metadataCode) {
		return new LocalDependency<>(metadataCode, false, false, MetadataValueType.CONTENT);
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
