package com.constellio.model.entities.calculators.dependencies;

import com.constellio.model.entities.Taxonomy;

import java.util.List;

/**
 * This class contains the metadatas of a taxonomy record assigned to the schema using the localMetadata
 * The metadatas of the taxonomy records are only those who have a taxonomy dependency
 */
public class HierarchyDependencyValue {

	private Taxonomy taxonomy;

	private List<String> paths;
	private List<String> removedAuthorizationIds;
	private List<String> attachedAncestors;
	private List<String> inheritedNonTaxonomyAuthorizations;

	public HierarchyDependencyValue(Taxonomy taxonomy, List<String> paths,
									List<String> removedAuthorizationIds,
									List<String> inheritedNonTaxonomyAuthorizations,
									List<String> attachedAncestors) {
		this.taxonomy = taxonomy;
		this.paths = paths;
		this.removedAuthorizationIds = removedAuthorizationIds;
		this.attachedAncestors = attachedAncestors;
		this.inheritedNonTaxonomyAuthorizations = inheritedNonTaxonomyAuthorizations;
	}

	public Taxonomy getTaxonomy() {
		return taxonomy;
	}

	public List<String> getPaths() {
		return paths;
	}

	public List<String> getRemovedAuthorizationIds() {
		return removedAuthorizationIds;
	}

	public List<String> getAttachedAncestors() {
		return attachedAncestors;
	}

	public List<String> getInheritedNonTaxonomyAuthorizations() {
		return inheritedNonTaxonomyAuthorizations;
	}
}
