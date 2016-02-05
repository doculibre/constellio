package com.constellio.model.services.security;

import java.io.Serializable;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;

public final class ContentPermission implements Serializable {

	private final String code;

	private final Set<String> dependencies;

	ContentPermission(String code, ContentPermission... dependentContentPermissions) {
		this.code = code;

		Set<String> dependencies = new HashSet<>();
		for (ContentPermission contentPermission : dependentContentPermissions) {
			dependencies.add(contentPermission.getCode());
			dependencies.addAll(contentPermission.getDependencies());
		}
		this.dependencies = Collections.unmodifiableSet(dependencies);
	}

	public String getCode() {
		return code;
	}

	public Set<String> getDependencies() {
		return dependencies;
	}

	@Override
	public String toString() {
		return code + " including " + dependencies;
	}

	@Override
	public int hashCode() {
		return HashCodeBuilder.reflectionHashCode(this);
	}

	@Override
	public boolean equals(Object obj) {
		return EqualsBuilder.reflectionEquals(this, obj);
	}

}
