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
package com.constellio.model.entities.calculators.dependencies;

import java.util.List;

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

	private ReferenceDependency(String referenceMetadataCode, String dependentMetadataCode, MetadataValueType returnType) {
		super();
		this.referenceMetadataCode = referenceMetadataCode;
		this.dependentMetadataCode = dependentMetadataCode;
		this.required = false;
		this.multivalue = false;
		this.returnType = returnType;
	}

	private ReferenceDependency(String referenceMetadataCode, String dependentMetadataCode, boolean required, boolean multivalue,
			MetadataValueType returnType) {
		super();
		this.referenceMetadataCode = referenceMetadataCode;
		this.dependentMetadataCode = dependentMetadataCode;
		this.required = required;
		this.multivalue = multivalue;
		this.returnType = returnType;
	}

	public <Z> ReferenceDependency<Z> whichIsRequired() {
		return new ReferenceDependency<>(referenceMetadataCode, dependentMetadataCode, true, multivalue, returnType);
	}

	public <Z> ReferenceDependency<List<Z>> whichIsMultivalue() {
		return new ReferenceDependency<>(referenceMetadataCode, dependentMetadataCode, required, true, returnType);
	}

	//@formatter:off

		public static ReferenceDependency<String> toAnEnum(String referenceMetadataCode, String dependentMetadataCode) {
		return new ReferenceDependency<>(referenceMetadataCode, dependentMetadataCode, false, false, MetadataValueType.ENUM);
	}

	public static ReferenceDependency<String> toAString(String referenceMetadataCode, String dependentMetadataCode) {
		return new ReferenceDependency<>(referenceMetadataCode, dependentMetadataCode, false, false, MetadataValueType.STRING);
	}

	public static ReferenceDependency<String> toAReference(String referenceMetadataCode, String dependentMetadataCode) {
		return new ReferenceDependency<>(referenceMetadataCode, dependentMetadataCode, false, false, MetadataValueType.REFERENCE);
	}

	public static ReferenceDependency<Boolean> toABoolean(String referenceMetadataCode, String dependentMetadataCode) {
		return new ReferenceDependency<>(referenceMetadataCode, dependentMetadataCode, false, false, MetadataValueType.BOOLEAN);
	}

	public static ReferenceDependency<Double> toANumber(String referenceMetadataCode, String dependentMetadataCode) {
		return new ReferenceDependency<>(referenceMetadataCode, dependentMetadataCode, false, false, MetadataValueType.NUMBER);
	}

	public static ReferenceDependency<LocalDate> toADate(String referenceMetadataCode, String dependentMetadataCode) {
		return new ReferenceDependency<>(referenceMetadataCode, dependentMetadataCode, false, false, MetadataValueType.DATE);
	}

	public static ReferenceDependency<LocalDateTime> toADateTime(String referenceMetadataCode, String dependentMetadataCode) {
		return new ReferenceDependency<>(referenceMetadataCode, dependentMetadataCode, false, false, MetadataValueType.DATE_TIME);
	}

	public static ReferenceDependency<?> toAStructure(String referenceMetadataCode, String dependentMetadataCode) {
		return new ReferenceDependency<>(referenceMetadataCode, dependentMetadataCode, false, false, MetadataValueType.STRUCTURE);
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
