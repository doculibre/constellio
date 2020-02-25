package com.constellio.model.services.records.cache.cacheIndexHook.impl;

import com.constellio.data.dao.dto.records.RecordId;
import com.constellio.data.dao.managers.StatefulService;
import com.constellio.model.entities.records.Record;
import com.constellio.model.entities.records.wrappers.User;
import com.constellio.model.entities.schemas.MetadataSchemaType;
import com.constellio.model.entities.security.SecurityModel;
import com.constellio.model.entities.security.SecurityModelAuthorization;
import com.constellio.model.services.factories.ModelLayerFactory;
import com.constellio.model.services.records.cache.cacheIndexHook.RecordCountHookDataIndexRetriever;
import com.constellio.model.services.schemas.MetadataSchemasManager;
import lombok.AllArgsConstructor;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import static com.constellio.data.dao.dto.records.RecordId.toId;
import static com.constellio.model.entities.records.Record.GetMetadataOption.DIRECT_GET_FROM_DTO;
import static com.constellio.model.entities.schemas.Schemas.ATTACHED_PRINCIPAL_ANCESTORS_INT_IDS;
import static com.constellio.model.entities.schemas.Schemas.PATH_PARTS;
import static com.constellio.model.entities.security.Role.READ;
import static com.constellio.model.entities.security.Role.WRITE;
import static com.constellio.model.services.records.cache.cacheIndexHook.impl.TaxonomyRecordsHookKey.attachedRecordInPrincipalConcept;
import static com.constellio.model.services.records.cache.cacheIndexHook.impl.TaxonomyRecordsHookKey.principalAccessOnRecordInConcept;
import static com.constellio.model.services.records.cache.cacheIndexHook.impl.TaxonomyRecordsHookKey.principalConceptAuthGivingAccessToRecordInSecondaryConceptKey;
import static com.constellio.model.services.records.cache.cacheIndexHook.impl.TaxonomyRecordsHookKey.recordInSecondaryConcept;
import static com.constellio.model.services.records.cache.dataStore.StreamCacheOption.STREAM_FULLY;

public class TaxonomyRecordsHookRetriever implements StatefulService {

	RecordCountHookDataIndexRetriever<TaxonomyRecordsHookKey> retriever;
	ModelLayerFactory modelLayerFactory;
	MetadataSchemasManager schemasManager;

	public TaxonomyRecordsHookRetriever(
			RecordCountHookDataIndexRetriever<TaxonomyRecordsHookKey> retriever, ModelLayerFactory modelLayerFactory) {
		this.retriever = retriever;
		this.modelLayerFactory = modelLayerFactory;
		this.schemasManager = modelLayerFactory.getMetadataSchemasManager();
	}

	public boolean hasUserAccessToSomethingInSecondaryConcept(User user, RecordId secondaryConceptId, boolean write,
															  boolean onlyVisible) {

		SecurityModel securityModel = modelLayerFactory.newRecordServices().getSecurityModel(user.getCollection());

		Set<RecordId> principalConceptsGivingAccess = new HashSet<>();
		if (user.hasCollectionAccess(write ? WRITE : READ)) {
			if (retriever.hasRecordsWith(recordInSecondaryConcept(secondaryConceptId, true))
				|| (!onlyVisible && retriever.hasRecordsWith(recordInSecondaryConcept(secondaryConceptId, false)))) {
				return true;
			}

		} else {
			for (SecurityModelAuthorization auth : securityModel.getAuthorizationsToPrincipal(user.getId(), true)) {
				if (!auth.isSecurableRecord()) {
					if (auth.getDetails().getRoles().contains(WRITE)) {
						principalConceptsGivingAccess.add(toId(auth.getDetails().getTarget()));
					} else if (auth.getDetails().getRoles().contains(READ) && !write) {
						principalConceptsGivingAccess.add(toId(auth.getDetails().getTarget()));
					}
				}
			}
		}

		for (RecordId principalConceptId : principalConceptsGivingAccess) {
			if (retriever.hasRecordsWith(principalConceptAuthGivingAccessToRecordInSecondaryConceptKey(principalConceptId, secondaryConceptId, true))
				|| (!onlyVisible && retriever.hasRecordsWith(principalConceptAuthGivingAccessToRecordInSecondaryConceptKey(principalConceptId, secondaryConceptId, false)))) {
				return true;
			}
		}


		return hasAccessToSomethingClassifiedInConcept(user, secondaryConceptId, write, onlyVisible, securityModel);
	}

	private boolean hasAccessToSomethingClassifiedInConcept(User user, RecordId conceptId, boolean write,
															boolean onlyVisible, SecurityModel securityModel) {
		if (retriever.hasRecordsWith(principalAccessOnRecordInConcept(user.getWrappedRecordId(), conceptId, write, true))
			|| (!onlyVisible && retriever.hasRecordsWith(principalAccessOnRecordInConcept(user.getWrappedRecordId(), conceptId, write, false)))) {
			return true;
		}

		for (String groupId : securityModel.getGroupsGivingAccessToUser(user.getId())) {
			if (retriever.hasRecordsWith(principalAccessOnRecordInConcept(toId(groupId), conceptId, write, true))
				|| (!onlyVisible && retriever.hasRecordsWith(principalAccessOnRecordInConcept(toId(groupId), conceptId, write, false)))) {
				return true;
			}
		}
		return false;
	}

	private long ancestorsSelfAndChildrenCacheValueSecurityModelVersion = 0;
	private Map<Integer, AncestorsSelfAndChildrenCacheValue> ancestorsSelfAndChildrenCache = new HashMap<>();

	@AllArgsConstructor
	private static class AncestorsSelfAndChildrenCacheValue {
		Set<Integer> ancestorsSelfAndChildren;
		Set<Integer> ancestorsSelf;
	}


	public boolean hasUserAccessToSomethingInPrincipalConcept(User user, Record principalConcept, boolean write,
															  boolean onlyVisible) {

		if (user.hasCollectionAccess(write ? WRITE : READ)) {
			if (retriever.hasRecordsWith(attachedRecordInPrincipalConcept(principalConcept.getRecordId(), true))
				|| (!onlyVisible && retriever.hasRecordsWith(attachedRecordInPrincipalConcept(principalConcept.getRecordId(), false)))) {
				return true;
			}
		}

		SecurityModel securityModel = modelLayerFactory.newRecordServices().getSecurityModel(user.getCollection());

		Map<Integer, AncestorsSelfAndChildrenCacheValue> currentAncestorsSelfAndChildrenCache = ancestorsSelfAndChildrenCache;
		if (ancestorsSelfAndChildrenCacheValueSecurityModelVersion != securityModel.getVersion()) {
			synchronized (this) {
				if (ancestorsSelfAndChildrenCacheValueSecurityModelVersion != securityModel.getVersion()) {
					ancestorsSelfAndChildrenCacheValueSecurityModelVersion = securityModel.getVersion();
					ancestorsSelfAndChildrenCache = new HashMap<>();
					currentAncestorsSelfAndChildrenCache = ancestorsSelfAndChildrenCache;
				}
			}
		}

		AncestorsSelfAndChildrenCacheValue cachedValue = currentAncestorsSelfAndChildrenCache.get(principalConcept.getRecordId().intValue());
		if (cachedValue == null) {

			Set<Integer> ancestors = new HashSet<>(principalConcept.getList(ATTACHED_PRINCIPAL_ANCESTORS_INT_IDS, DIRECT_GET_FROM_DTO));
			ancestors.add(principalConcept.getRecordId().intValue());

			Set<Integer> ancestorsSelfAndChildren = new HashSet<>(ancestors);
			MetadataSchemaType schemaType = schemasManager.getSchemaTypeOf(principalConcept);
			//TODO Cache this!
			ancestorsSelfAndChildren.addAll(modelLayerFactory.getRecordsCaches().stream(schemaType, STREAM_FULLY)
					.filter((r) -> r.getList(PATH_PARTS, DIRECT_GET_FROM_DTO).contains(principalConcept.getId()))
					.map((r) -> r.getRecordId().intValue())
					.collect(Collectors.toList()));
			cachedValue = new AncestorsSelfAndChildrenCacheValue(ancestorsSelfAndChildren, ancestors);
			synchronized (currentAncestorsSelfAndChildrenCache) {
				currentAncestorsSelfAndChildrenCache.put(principalConcept.getRecordId().intValue(), cachedValue);
			}
		}

		Set<RecordId> principalConceptsGivingAccess = new HashSet<>();
		//boolean hasAuthGivingAccessOnPrincipalConceptOn
		for (SecurityModelAuthorization auth : securityModel.getAuthorizationsToPrincipal(user.getId(), true)) {
			if (!auth.isSecurableRecord()) {
				RecordId recordId = auth.getTargetRecordId();
				if (cachedValue.ancestorsSelfAndChildren.contains(recordId.intValue())) {
					boolean add = false;
					ROLES:
					for (String role : auth.getDetails().getRoles()) {
						if (WRITE.equals(role)) {
							add = true;
							break ROLES;

						} else if (READ.equals(role) && !write) {
							add = true;
							break ROLES;

						}
					}

					if (add) {
						if (cachedValue.ancestorsSelf.contains(recordId.intValue())) {
							principalConceptsGivingAccess.add(principalConcept.getRecordId());
						} else {
							principalConceptsGivingAccess.add(recordId);
						}

					}
				}
			}
		}

		for (RecordId principalConceptId : principalConceptsGivingAccess) {
			if (retriever.hasRecordsWith(attachedRecordInPrincipalConcept(principalConceptId, true))
				|| (!onlyVisible && retriever.hasRecordsWith(attachedRecordInPrincipalConcept(principalConceptId, false)))) {
				return true;
			}
		}

		return hasAccessToSomethingClassifiedInConcept(user, principalConcept.getRecordId(), write, onlyVisible, securityModel);
	}

	@Override
	public void initialize() {

	}

	@Override
	public void close() {

	}
}

