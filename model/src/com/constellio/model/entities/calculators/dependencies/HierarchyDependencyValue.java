package com.constellio.model.entities.calculators.dependencies;

import java.util.List;

import com.constellio.model.entities.Taxonomy;

/**
 * This class contains the metadatas of a taxonomy record assigned to the schema using the localMetadata
 * The metadatas of the taxonomy records are only those who have a taxonomy dependency
 */
public class HierarchyDependencyValue {

	private Taxonomy taxonomy;

	private List<String> paths;
	private List<String> authorizationIds;
	private List<String> removedAuthorizationIds;

	public HierarchyDependencyValue(Taxonomy taxonomy, List<String> paths,
			List<String> authorizationIds, List<String> removedAuthorizationIds) {
		this.taxonomy = taxonomy;
		this.paths = paths;
		this.authorizationIds = authorizationIds;
		this.removedAuthorizationIds = removedAuthorizationIds;
	}

	public Taxonomy getTaxonomy() {
		return taxonomy;
	}

	public List<String> getPaths() {
		return paths;
	}

	public List<String> getParentAuthorizations() {
		return authorizationIds;
	}

	public List<String> getRemovedAuthorizationIds() {
		return removedAuthorizationIds;
	}
}
