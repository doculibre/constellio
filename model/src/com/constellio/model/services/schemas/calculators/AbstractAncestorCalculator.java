package com.constellio.model.services.schemas.calculators;

import com.constellio.model.entities.calculators.AbstractMetadataValueCalculator;
import com.constellio.model.entities.calculators.CalculatorParameters;
import com.constellio.model.entities.calculators.DynamicDependencyValues;
import com.constellio.model.entities.calculators.dependencies.Dependency;
import com.constellio.model.entities.calculators.dependencies.DynamicLocalDependency;
import com.constellio.model.entities.calculators.dependencies.HierarchyDependencyValue;
import com.constellio.model.entities.calculators.dependencies.LocalDependency;
import com.constellio.model.entities.calculators.dependencies.SpecialDependencies;
import com.constellio.model.entities.calculators.dependencies.SpecialDependency;
import com.constellio.model.entities.schemas.Metadata;
import com.constellio.model.entities.schemas.MetadataValueType;
import com.constellio.model.services.records.RecordId;
import com.constellio.model.services.schemas.builders.CommonMetadataBuilder;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.StringTokenizer;

import static com.constellio.model.entities.schemas.MetadataValueType.INTEGER;
import static com.constellio.model.services.schemas.builders.CommonMetadataBuilder.ATTACHED_ANCESTORS;

public abstract class AbstractAncestorCalculator extends AbstractMetadataValueCalculator<List<Integer>> {

	SpecialDependency<HierarchyDependencyValue> taxonomiesParam = SpecialDependencies.HIERARCHY;
	LocalDependency<List<String>> pathsParam = LocalDependency.toAStringList(CommonMetadataBuilder.PATH);
	LocalDependency<String> principalPathParam = LocalDependency.toAString(CommonMetadataBuilder.PRINCIPAL_PATH);
	LocalDependency<List<String>> attachedAncestorsParam = LocalDependency.toAStringList(ATTACHED_ANCESTORS);

	DynamicLocalDependency localConceptsParam = new DynamicLocalDependency() {
		@Override
		public boolean isDependentOf(Metadata metadata, Metadata caclulatedMetadata) {
			return metadata.isTaxonomyRelationship();
		}
	};

	protected List<Integer> getAttachedAncestors(CalculatorParameters parameters) {
		List<Integer> intValues = new ArrayList<>();
		parameters.get(attachedAncestorsParam).forEach((attachedAncestor) -> {
			if (attachedAncestor != null) {
				intValues.add(RecordId.toIntId(attachedAncestor));
			}
		});
		intValues.sort(null);

		return intValues;
	}

	@NotNull
	protected List<Integer> getPathParts(CalculatorParameters parameters) {
		HierarchyDependencyValue paramValue = parameters.get(taxonomiesParam);

		List<Integer> intValues = new ArrayList<>(paramValue.getPrincipalConceptAncestors());
		intValues.addAll(paramValue.getSecondaryConceptAncestors());

		DynamicDependencyValues localConcepts = parameters.get(localConceptsParam);

		List<String> idsToAdd = null;
		for (Metadata metadata : localConcepts.getAvailableMetadatasWithAValue()) {
			if (idsToAdd == null) {
				idsToAdd = new ArrayList<>();
			}

			if (metadata.isMultivalue()) {
				idsToAdd.addAll(localConcepts.<List<String>>getValue(metadata));
			} else {
				idsToAdd.add(localConcepts.getValue(metadata));
			}
		}

		List<String> paths = parameters.get(pathsParam);
		if (idsToAdd != null && !idsToAdd.isEmpty()) {
			Set<Integer> intValuesSet = new HashSet<>(intValues);
			for (String idToAdd : idsToAdd) {
				extractIdsAndParentIdsUsingPaths(parameters, paths, intValuesSet, idToAdd);
			}
			intValues = new ArrayList<>(intValuesSet);
		}

		intValues.sort(null);
		return intValues;
	}

	private void extractIdsAndParentIdsUsingPaths(CalculatorParameters parameters, List<String> paths,
												  Set<Integer> intValuesSet, String idToAdd) {
		for (String path : paths) {
			String suffix = "/" + idToAdd + "/" + parameters.getId();
			if (path.endsWith(suffix)) {
				int secondSlash = path.indexOf("/", 1);
				int from = secondSlash + 1;
				int to = path.length() - suffix.length();

				if (from < to) {
					String parents = path.substring(from, to);
					intValuesSet.add(RecordId.toIntId(idToAdd));
					if (!parents.isEmpty()) {
						for (String aParent : parents.split("/")) {
							intValuesSet.add(RecordId.toIntId(aParent));
						}
					}
				} else {
					intValuesSet.add(RecordId.toIntId(idToAdd));
				}
			}
		}
	}

	protected List<Integer> parsePrincipalPathNodes(CalculatorParameters parameters) {
		String principalPath = parameters.get(principalPathParam);
		List<Integer> nodes = new ArrayList<>();

		if (principalPath != null) {
			StringTokenizer st = new StringTokenizer(principalPath, "/");
			st.nextToken();
			while (st.hasMoreTokens()) {
				String token = st.nextToken();
				if (!parameters.getId().equals(token)) {
					nodes.add(RecordId.toIntId(token));
				}
			}
		}

		nodes.sort(null);

		return nodes;
	}

	@Override
	public List<? extends Dependency> getDependencies() {
		return Arrays.asList(taxonomiesParam, localConceptsParam, pathsParam, attachedAncestorsParam, principalPathParam);
	}

	@Override
	public List<Integer> getDefaultValue() {
		return new ArrayList<>();
	}

	@Override
	public MetadataValueType getReturnType() {
		return INTEGER;
	}

	@Override
	public boolean isMultiValue() {
		return true;
	}

	protected boolean isTaxonomyNode(CalculatorParameters parameters) {
		return parameters.get(taxonomiesParam).getTaxonomy() != null;
	}
}
