package com.constellio.model.services.schemas.calculators;

import com.constellio.data.utils.LangUtils;
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
import com.constellio.model.entities.security.SecurityModel;
import com.constellio.model.entities.security.SecurityModelAuthorization;
import com.constellio.model.services.records.RecordId;
import com.constellio.model.services.schemas.builders.CommonMetadataBuilder;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.StringTokenizer;
import java.util.stream.Collectors;

import static com.constellio.model.entities.schemas.MetadataValueType.INTEGER;
import static com.constellio.model.entities.security.TransactionSecurityModel.hasActiveOverridingAuth;
import static com.constellio.model.services.schemas.builders.CommonMetadataBuilder.DETACHED_AUTHORIZATIONS;

public abstract class AbstractAncestorCalculator extends AbstractMetadataValueCalculator<List<Integer>> {

	protected SpecialDependency<HierarchyDependencyValue> taxonomiesParam = SpecialDependencies.HIERARCHY;
	protected LocalDependency<List<String>> pathsParam = LocalDependency.toAStringList(CommonMetadataBuilder.PATH);
	protected LocalDependency<String> principalPathParam = LocalDependency.toAString(CommonMetadataBuilder.PRINCIPAL_PATH);
	//protected LocalDependency<List<String>> attachedAncestorsParam = LocalDependency.toAStringList(ATTACHED_ANCESTORS);
	protected LocalDependency<Boolean> detachedParam = LocalDependency.toABoolean(DETACHED_AUTHORIZATIONS);
	protected SpecialDependency<SecurityModel> securityModelDependency = SpecialDependencies.SECURITY_MODEL;
	protected MetadatasProvidingSecurityDynamicDependency metadatasProvidingSecurityParams = new MetadatasProvidingSecurityDynamicDependency();

	DynamicLocalDependency localConceptsParam = new DynamicLocalDependency() {
		@Override
		public boolean isDependentOf(Metadata metadata, Metadata caclulatedMetadata) {
			return metadata.isTaxonomyRelationship();
		}
	};


	//	protected List<Integer> getAttachedAncestors(CalculatorParameters parameters) {
	//		List<Integer> intValues = new ArrayList<>();
	//
	//		for (String attachedAncestor : parameters.get(attachedAncestorsParam)) {
	//			if (attachedAncestor != null) {
	//				intValues.add(RecordId.toIntId(attachedAncestor));
	//			}
	//		}
	//
	//		intValues.sort(null);
	//
	//		return intValues;
	//	}

	protected List<Integer> getAttachedPrincipalConceptsIntIdsFromParent(CalculatorParameters parameters) {
		if (!Boolean.TRUE.equals(parameters.get(detachedParam))) {
			return parameters.get(taxonomiesParam).getAttachedPrincipalConceptsIntIdsFromParent();
		} else {
			return Collections.emptyList();
		}
	}

	protected List<Integer> getPrincipalAncestorsIntIdsFromParent(CalculatorParameters parameters) {
		if (!Boolean.TRUE.equals(parameters.get(detachedParam))) {
			return parameters.get(taxonomiesParam).getPrincipalAncestorsIntIdsFromParent();
		} else {
			return Collections.emptyList();
		}
	}


	@NotNull
	protected List<Integer> getPathParts(CalculatorParameters parameters) {
		HierarchyDependencyValue paramValue = parameters.get(taxonomiesParam);

		List<Integer> intValues = new ArrayList<>();
		intValues.addAll(paramValue.getPrincipalConceptsIntIdsFromParent());
		intValues.addAll(paramValue.getPrincipalAncestorsIntIdsFromParent());
		intValues.addAll(paramValue.getSecondaryConceptsIntIdsFromParent());

		intValues.addAll(paramValue.getPrincipalConceptsIntIds());
		intValues.addAll(paramValue.getPrincipalAncestorsIntIds());
		intValues.addAll(paramValue.getSecondaryConceptsIntIds());

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

	//TODO Call only once using a shared mecanism
	protected Map<String, Object> calculateAttachedAndDetached(CalculatorParameters parameters) {
		HierarchyDependencyValue hierarchyDependencyValue = parameters.get(taxonomiesParam);

		Set<Integer> ids = new HashSet<>();
		Set<Integer> possiblyDetachedAncestors = new HashSet<>();
		Set<Integer> detachedAncestors = new HashSet<>();
		//

		boolean isDetachedAuths = Boolean.TRUE.equals(parameters.get(detachedParam));
		boolean hasSecurity = parameters.getSchemaType().hasSecurity();

		if (hierarchyDependencyValue.getPrincipalConceptsIntIds().contains(RecordId.toIntId(parameters.getId()))) {
			ids.addAll(hierarchyDependencyValue.getPrincipalConceptsIntIds());
		}

		if (hasSecurity) {
			SecurityModel securityModel = parameters.get(securityModelDependency);


			DynamicDependencyValues values = parameters.get(metadatasProvidingSecurityParams);
			List<SecurityModelAuthorization> authsOnMetadatas = securityModel.getAuthorizationDetailsOnMetadatasProvidingSecurity(values);

			if (hierarchyDependencyValue != null) {
				if (!isDetachedAuths && !hasActiveOverridingAuth(authsOnMetadatas)) {
					ids.addAll(hierarchyDependencyValue.getPrincipalAncestorsIntIds());
					hierarchyDependencyValue.getAttachedPrincipalConceptsIntIdsFromParent()
							.forEach((intId) -> ids.add(RecordId.toId(intId).intValue()));
				} else {
					hierarchyDependencyValue.getAttachedPrincipalConceptsIntIdsFromParent()
							.forEach((intId) -> possiblyDetachedAncestors.add(intId));
				}

			}

			if (!isDetachedAuths) {
				for (Metadata metadata : values.getAvailableMetadatasWithAValue()) {
					if (metadata.isMultivalue()) {
						ids.addAll(values.<List<String>>getValue(metadata).stream().map((s) -> RecordId.toId(s).intValue()).collect(Collectors.toList()));
					} else {
						ids.add(RecordId.toId(values.<String>getValue(metadata)).intValue());
					}
				}
			}

			ids.add(RecordId.toId(parameters.getId()).intValue());

			if (hierarchyDependencyValue != null) {
				for (int ancestorIntId : hierarchyDependencyValue.getPrincipalAncestorsIntIds()) {
					if (!hierarchyDependencyValue.getPrincipalConceptsIntIds().contains(ancestorIntId)) {
						possiblyDetachedAncestors.add(ancestorIntId);
					}
				}

				for (int ancestorIntId : hierarchyDependencyValue.getPrincipalAncestorsIntIdsFromParent()) {
					if (!hierarchyDependencyValue.getPrincipalConceptsIntIdsFromParent().contains(ancestorIntId)) {
						possiblyDetachedAncestors.add(ancestorIntId);
					}
				}
			}
			//			possiblyDetachedAncestors.addAll(hierarchyDependencyValue.getPrincipalAncestorsIntIds());
			//			possiblyDetachedAncestors.addAll(hierarchyDependencyValue.getPrincipalConceptsIntIdsFromParent());

			for (Integer possiblyDetachedAncestor : possiblyDetachedAncestors) {
				if (!ids.contains(possiblyDetachedAncestor)) {
					detachedAncestors.add(possiblyDetachedAncestor);
				}
			}

		}

		//return ancestors;

		Map<String, Object> returnedValues = new HashMap<>();
		returnedValues.put("attached", LangUtils.toSortedList(ids));
		returnedValues.put("detached", LangUtils.toSortedList(detachedAncestors));
		return returnedValues;
	}

	@Override
	public List<? extends Dependency> getDependencies() {
		return Arrays.asList(taxonomiesParam, localConceptsParam, pathsParam,
				principalPathParam, detachedParam, securityModelDependency, metadatasProvidingSecurityParams);
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
