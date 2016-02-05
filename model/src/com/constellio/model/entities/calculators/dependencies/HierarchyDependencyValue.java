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

	public HierarchyDependencyValue(Taxonomy taxonomy, List<String> paths,
			List<String> authorizationIds) {
		this.taxonomy = taxonomy;
		this.paths = paths;
		this.authorizationIds = authorizationIds;
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
}
