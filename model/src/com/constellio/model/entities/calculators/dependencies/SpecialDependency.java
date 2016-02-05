package com.constellio.model.entities.calculators.dependencies;

import com.constellio.model.entities.schemas.MetadataValueType;

public class SpecialDependency<T> implements Dependency {

	private String code;

	SpecialDependency(String code) {
		this.code = code;
	}

	@Override
	public MetadataValueType getReturnType() {
		return null;
	}

	@Override
	public boolean isMultivalue() {
		return false;
	}

	@Override
	public boolean isRequired() {
		return false;
	}

	@Override
	public String getLocalMetadataCode() {
		return null;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) {
			return true;
		}
		if (!(o instanceof SpecialDependency)) {
			return false;
		}

		SpecialDependency that = (SpecialDependency) o;

		if (code != null ? !code.equals(that.code) : that.code != null) {
			return false;
		}

		return true;
	}

	@Override
	public int hashCode() {
		return code.hashCode();
	}

	@Override
	public String toString() {
		return "SpecialDependency{" +
				"code='" + code + '\'' +
				'}';
	}
}
