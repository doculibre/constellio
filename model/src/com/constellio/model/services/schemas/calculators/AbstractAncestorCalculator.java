package com.constellio.model.services.schemas.calculators;

import com.constellio.data.utils.LangUtils;
import com.constellio.data.utils.Pair;
import com.constellio.model.entities.calculators.CalculatorParameters;
import com.constellio.model.entities.calculators.DynamicDependencyValues;
import com.constellio.model.entities.calculators.MultiMetadatasValueCalculator;
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
import com.constellio.data.dao.dto.records.RecordId;
import com.constellio.model.services.schemas.builders.CommonMetadataBuilder;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.StringTokenizer;
import java.util.stream.Collectors;

import static com.constellio.model.entities.schemas.MetadataValueType.INTEGER;
import static com.constellio.model.entities.security.TransactionSecurityModel.hasActiveOverridingAuth;
import static com.constellio.model.services.schemas.builders.CommonMetadataBuilder.ATTACHED_PRINCIPAL_ANCESTORS_INT_IDS;
import static com.constellio.model.services.schemas.builders.CommonMetadataBuilder.DETACHED_AUTHORIZATIONS;
import static com.constellio.model.services.schemas.builders.CommonMetadataBuilder.DETACHED_PRINCIPALS_ANCESTORS_INT_IDS;
import static com.constellio.model.services.schemas.builders.CommonMetadataBuilder.PRINCIPALS_ANCESTORS_INT_IDS;
import static com.constellio.model.services.schemas.builders.CommonMetadataBuilder.PRINCIPAL_CONCEPTS_INT_IDS;
import static com.constellio.model.services.schemas.builders.CommonMetadataBuilder.SECONDARY_CONCEPTS_INT_IDS;

public class AbstractAncestorCalculator implements MultiMetadatasValueCalculator {

	private static final Logger LOGGER = LoggerFactory.getLogger(AbstractAncestorCalculator.class);

	protected SpecialDependency<HierarchyDependencyValue> taxonomiesParam = SpecialDependencies.HIERARCHY;
	protected LocalDependency<List<String>> pathsParam = LocalDependency.toAStringList(CommonMetadataBuilder.PATH);
	protected LocalDependency<String> principalPathParam = LocalDependency.toAString(CommonMetadataBuilder.PRINCIPAL_PATH);
	protected LocalDependency<Boolean> detachedParam = LocalDependency.toABoolean(DETACHED_AUTHORIZATIONS);
	protected SpecialDependency<SecurityModel> securityModelDependency = SpecialDependencies.SECURITY_MODEL;
	protected MetadatasProvidingSecurityDynamicDependency metadatasProvidingSecurityParams = new MetadatasProvidingSecurityDynamicDependency();

	DynamicLocalDependency localConceptsParam = new DynamicLocalDependency() {
		@Override
		public boolean isDependentOf(Metadata metadata, Metadata caclulatedMetadata) {
			return metadata.isTaxonomyRelationship();
		}
	};

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

	@Override
	public Map<String, Object> calculate(CalculatorParameters parameters) {
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

			if (!isDetachedAuths && !hasActiveOverridingAuth(authsOnMetadatas)) {
				ids.addAll(hierarchyDependencyValue.getPrincipalAncestorsIntIds());
				hierarchyDependencyValue.getAttachedPrincipalConceptsIntIdsFromParent()
						.forEach((intId) -> ids.add(RecordId.toId(intId).intValue()));
			} else {
				hierarchyDependencyValue.getAttachedPrincipalConceptsIntIdsFromParent()
						.forEach((intId) -> possiblyDetachedAncestors.add(intId));
			}


			if (!isDetachedAuths) {
				Iterator<Pair<Metadata, Object>> iterator = values.iterateWithValues();
				while (iterator.hasNext()) {
					Pair<Metadata, Object> entry = iterator.next();
					Metadata metadata = entry.getKey();
					if (metadata.isMultivalue()) {
						ids.addAll(((List<String>) entry.getValue()).stream().map((s) -> RecordId.toId(s).intValue()).collect(Collectors.toList()));
					} else {
						ids.add(RecordId.toId((String) entry.getValue()).intValue());
					}
				}
			}

			ids.add(RecordId.toId(parameters.getId()).intValue());

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

			for (Integer possiblyDetachedAncestor : possiblyDetachedAncestors) {
				if (!ids.contains(possiblyDetachedAncestor)) {
					detachedAncestors.add(possiblyDetachedAncestor);
				}
			}

		}

		Map<String, Object> returnedValues = new HashMap<>();
		returnedValues.put(ATTACHED_PRINCIPAL_ANCESTORS_INT_IDS, LangUtils.toSortedList(ids));
		returnedValues.put(DETACHED_PRINCIPALS_ANCESTORS_INT_IDS, LangUtils.toSortedList(detachedAncestors));


		Set<Integer> secondaryConceptIntIds = new HashSet<>();
		secondaryConceptIntIds.addAll(hierarchyDependencyValue.getSecondaryConceptsIntIds());
		secondaryConceptIntIds.addAll(hierarchyDependencyValue.getSecondaryConceptsIntIdsFromParent());
		returnedValues.put(SECONDARY_CONCEPTS_INT_IDS, LangUtils.toSortedList(secondaryConceptIntIds));

		Set<Integer> principalConceptIntIds = new HashSet<>();
		principalConceptIntIds.addAll(hierarchyDependencyValue.getPrincipalConceptsIntIds());
		principalConceptIntIds.addAll(hierarchyDependencyValue.getPrincipalConceptsIntIdsFromParent());
		returnedValues.put(PRINCIPAL_CONCEPTS_INT_IDS, LangUtils.toSortedList(principalConceptIntIds));

		List<Integer> principalPathNodes = parsePrincipalPathNodes(parameters);
		List<Integer> pathParts = getPathParts(parameters);
		returnedValues.put(PRINCIPALS_ANCESTORS_INT_IDS, LangUtils.findMatchesInSortedLists(principalPathNodes, pathParts));

		return returnedValues;
	}

	@Override
	public List<? extends Dependency> getDependencies() {
		return Arrays.asList(taxonomiesParam, localConceptsParam, pathsParam,
				principalPathParam, detachedParam, securityModelDependency, metadatasProvidingSecurityParams);
	}


	@Override
	public Map<String, Object> getDefaultValue() {
		Map<String, Object> defaultValues = new HashMap<>();
		defaultValues.put(ATTACHED_PRINCIPAL_ANCESTORS_INT_IDS, Collections.emptyList());
		defaultValues.put(DETACHED_PRINCIPALS_ANCESTORS_INT_IDS, Collections.emptyList());
		defaultValues.put(PRINCIPAL_CONCEPTS_INT_IDS, Collections.emptyList());
		defaultValues.put(SECONDARY_CONCEPTS_INT_IDS, Collections.emptyList());
		defaultValues.put(PRINCIPALS_ANCESTORS_INT_IDS, Collections.emptyList());
		return defaultValues;
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
