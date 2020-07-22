package com.constellio.model.entities.calculators.dependencies;

import com.constellio.model.entities.Taxonomy;

import java.util.ArrayList;
import java.util.List;

/**
 * This class contains the metadatas of a taxonomy record assigned to the schema using the localMetadata
 * The metadatas of the taxonomy records are only those who have a taxonomy dependency
 */
public class HierarchyDependencyValue {

	private Taxonomy taxonomy;

	private List<String> paths = new ArrayList<>();
	private List<String> removedAuthorizationIds = new ArrayList<>();
	private List<String> attachedAncestors = new ArrayList<>();

	//private List<Integer> attachedPrincipalConceptsIntIds = new ArrayList<>();
	private List<Integer> attachedPrincipalConceptsIntIdsFromParent = new ArrayList<>();

	private List<Integer> principalAncestorsIntIds = new ArrayList<>();
	private List<Integer> principalAncestorsIntIdsFromParent = new ArrayList<>();

	private List<Integer> principalConceptsIntIds = new ArrayList<>();
	private List<Integer> principalConceptsIntIdsFromParent = new ArrayList<>();

	private List<Integer> secondaryConceptsIntIds = new ArrayList<>();
	private List<Integer> secondaryConceptsIntIdsFromParent = new ArrayList<>();

	public HierarchyDependencyValue(Taxonomy taxonomy) {
		this.taxonomy = taxonomy;
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

	public List<Integer> getAttachedPrincipalConceptsIntIdsFromParent() {
		return attachedPrincipalConceptsIntIdsFromParent;
	}

	public List<Integer> getPrincipalAncestorsIntIds() {
		return principalAncestorsIntIds;
	}

	public List<Integer> getPrincipalAncestorsIntIdsFromParent() {
		return principalAncestorsIntIdsFromParent;
	}

	public List<Integer> getPrincipalConceptsIntIds() {
		return principalConceptsIntIds;
	}

	public List<Integer> getPrincipalConceptsIntIdsFromParent() {
		return principalConceptsIntIdsFromParent;
	}

	public List<Integer> getSecondaryConceptsIntIds() {
		return secondaryConceptsIntIds;
	}

	public List<Integer> getSecondaryConceptsIntIdsFromParent() {
		return secondaryConceptsIntIdsFromParent;
	}
}
