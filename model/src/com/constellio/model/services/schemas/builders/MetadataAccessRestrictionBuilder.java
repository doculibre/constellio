package com.constellio.model.services.schemas.builders;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import com.constellio.model.entities.schemas.MetadataAccessRestriction;

public class MetadataAccessRestrictionBuilder {

	List<String> requiredReadRoles;

	List<String> requiredWriteRoles;

	List<String> requiredModificationRoles;

	List<String> requiredDeleteRoles;

	public static MetadataAccessRestrictionBuilder modify(MetadataAccessRestriction accessRestriction) {
		MetadataAccessRestrictionBuilder builder = new MetadataAccessRestrictionBuilder();
		builder.requiredReadRoles = accessRestriction.getRequiredReadRoles();
		builder.requiredWriteRoles = accessRestriction.getRequiredWriteRoles();
		builder.requiredModificationRoles = accessRestriction.getRequiredModificationRoles();
		builder.requiredDeleteRoles = accessRestriction.getRequiredDeleteRoles();
		return builder;
	}

	public static MetadataAccessRestrictionBuilder create() {
		return modify(new MetadataAccessRestriction());
	}

	public List<String> getRequiredReadRoles() {
		return requiredReadRoles;
	}

	public List<String> getRequiredWriteRoles() {
		return requiredWriteRoles;
	}

	public List<String> getRequiredModificationRoles() {
		return requiredModificationRoles;
	}

	public List<String> getRequiredDeleteRoles() {
		return requiredDeleteRoles;
	}

	public MetadataAccessRestrictionBuilder withRequiredReadRole(String role) {
		requiredReadRoles.add(role);
		return this;
	}

	public MetadataAccessRestrictionBuilder withRequiredWriteRole(String role) {
		requiredWriteRoles.add(role);
		return this;
	}

	public MetadataAccessRestrictionBuilder withRequiredModificationRole(String role) {
		requiredModificationRoles.add(role);
		return this;
	}

	public MetadataAccessRestrictionBuilder withRequiredDeleteRole(String role) {
		requiredDeleteRoles.add(role);
		return this;
	}

	public MetadataAccessRestrictionBuilder withRequiredWriteAndDeleteRole(String role) {
		requiredWriteRoles.add(role);
		requiredDeleteRoles.add(role);
		return this;
	}

	public MetadataAccessRestrictionBuilder withRequiredRole(String role) {
		requiredReadRoles.add(role);
		requiredWriteRoles.add(role);
		requiredModificationRoles.add(role);
		requiredDeleteRoles.add(role);
		return this;
	}

	public MetadataAccessRestriction build() {
		List<String> unmodifiableReadRequiredRoles = Collections.unmodifiableList(withoutDuplicates(requiredReadRoles));
		List<String> unmodifiableWriteRequiredRoles = Collections.unmodifiableList(withoutDuplicates(requiredWriteRoles));
		List<String> unmodifiableModificationRequiredRoles = Collections
				.unmodifiableList(withoutDuplicates(requiredModificationRoles));
		List<String> unmodifiableDeleteRequiredRoles = Collections.unmodifiableList(withoutDuplicates(requiredDeleteRoles));
		return new MetadataAccessRestriction(unmodifiableReadRequiredRoles, unmodifiableWriteRequiredRoles,
				unmodifiableModificationRequiredRoles, unmodifiableDeleteRequiredRoles);
	}

	private List<String> withoutDuplicates(List<String> elements) {
		List<String> withoutDuplicates = new ArrayList<>();
		for (String element : elements) {
			if (!withoutDuplicates.contains(element)) {
				withoutDuplicates.add(element);
			}
		}
		return withoutDuplicates;
	}

	public boolean isEmpty() {
		return requiredReadRoles.isEmpty()
				&& requiredWriteRoles.isEmpty()
				&& requiredModificationRoles.isEmpty()
				&& requiredDeleteRoles.isEmpty();
	}

}
