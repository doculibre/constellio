package com.constellio.model.entities.calculators.dependencies;

import com.constellio.model.entities.configs.SystemConfiguration;
import com.constellio.model.entities.schemas.MetadataValueType;

public class ConfigDependency<T> implements Dependency {

	private SystemConfiguration configuration;

	public ConfigDependency(SystemConfiguration configuration) {
		this.configuration = configuration;
	}

	public SystemConfiguration getConfiguration() {
		return configuration;
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
		if (!(o instanceof ConfigDependency)) {
			return false;
		}

		ConfigDependency that = (ConfigDependency) o;

		if (configuration != null ? !configuration.equals(that.configuration) : that.configuration != null) {
			return false;
		}

		return true;
	}

	@Override
	public int hashCode() {
		return configuration.hashCode();
	}

	@Override
	public String toString() {
		return "ConfigDependency{" +
				"configuration=" + configuration.getCode() +
				'}';
	}
}
