package com.constellio.model.services.records.cache.cacheIndexHook.impl;

import com.constellio.model.entities.records.Record;
import com.constellio.model.entities.records.wrappers.User;
import com.constellio.model.entities.schemas.Schemas;
import com.constellio.model.entities.security.SecurityModel;
import com.constellio.model.entities.security.SecurityModelAuthorization;
import com.constellio.model.services.factories.ModelLayerFactory;
import com.constellio.model.services.records.RecordId;
import com.constellio.model.services.records.cache.cacheIndexHook.RecordCountHookDataIndexRetriever;

import java.util.HashSet;
import java.util.Set;

import static com.constellio.model.entities.security.Role.READ;
import static com.constellio.model.entities.security.Role.WRITE;
import static com.constellio.model.services.records.RecordId.toId;
import static com.constellio.model.services.records.cache.cacheIndexHook.impl.TaxonomyRecordsHookKey.principalAccessOnRecordInConcept;
import static com.constellio.model.services.records.cache.cacheIndexHook.impl.TaxonomyRecordsHookKey.principalConceptAuthGivingAccessToRecordInSecondaryConceptKey;
import static com.constellio.model.services.records.cache.cacheIndexHook.impl.TaxonomyRecordsHookKey.recordInPrincipalConcept;

public class TaxonomyRecordsHookRetriever {

	RecordCountHookDataIndexRetriever<TaxonomyRecordsHookKey> retriever;
	ModelLayerFactory modelLayerFactory;

	public TaxonomyRecordsHookRetriever(
			RecordCountHookDataIndexRetriever<TaxonomyRecordsHookKey> retriever, ModelLayerFactory modelLayerFactory) {
		this.retriever = retriever;
		this.modelLayerFactory = modelLayerFactory;
	}

	public boolean hasUserAccessToSomethingInSecondaryConcept(User user, RecordId secondaryConceptId, boolean write,
															  boolean onlyVisible) {

		SecurityModel securityModel = modelLayerFactory.newRecordServices().getSecurityModel(user.getCollection());

		Set<RecordId> principalConceptsGivingAccess = new HashSet<>();
		if (user.hasCollectionAccess(write ? WRITE : READ)) {
			//TODO Temporaire : Ne fonctionne pas pour les unités sans autorisation
			for (SecurityModelAuthorization auth : securityModel.getAuthorizationsToPrincipal(user.getId(), true)) {
				if (!auth.isSecurableRecord()) {
					principalConceptsGivingAccess.add(toId(auth.getDetails().getTarget()));
				}
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
			if (retriever.hasRecordsWith(principalConceptAuthGivingAccessToRecordInSecondaryConceptKey(
					principalConceptId, secondaryConceptId, onlyVisible))) {
				return true;
			}
		}


		return hasAccessToSomethingClassifiedInConcept(user, secondaryConceptId, write, onlyVisible, securityModel);
	}

	private boolean hasAccessToSomethingClassifiedInConcept(User user, RecordId conceptId, boolean write,
															boolean onlyVisible, SecurityModel securityModel) {
		if (retriever.hasRecordsWith(principalAccessOnRecordInConcept(
				user.getWrappedRecordId(), conceptId, write, onlyVisible))) {
			return true;
		}

		for (String groupId : securityModel.getGroupsGivingAccessToUser(user.getId())) {
			if (retriever.hasRecordsWith(principalAccessOnRecordInConcept(
					toId(groupId), conceptId, write, onlyVisible))) {
				return true;
			}
		}
		return false;
	}


	public boolean hasUserAccessToSomethingInPrincipalConcept(User user, Record principalConcept, boolean write,
															  boolean onlyVisible) {


		SecurityModel securityModel = modelLayerFactory.newRecordServices().getSecurityModel(user.getCollection());

		Set<RecordId> principalConceptsGivingAccess = new HashSet<>();
		Set<Integer> ancestors = new HashSet<>(principalConcept.getList(Schemas.ATTACHED_PRINCIPAL_CONCEPTS_INT_IDS));
		ancestors.add(principalConcept.getRecordId().intValue());
		for (SecurityModelAuthorization auth : securityModel.getAuthorizationsToPrincipal(user.getId(), true)) {
			if (!auth.isSecurableRecord()) {
				RecordId recordId = auth.getTargetRecordId();
				if (recordId.isInteger() && ancestors.contains(recordId.intValue())) {
					ROLES:
					for (String role : auth.getDetails().getRoles()) {
						if (WRITE.equals(role)) {
							principalConceptsGivingAccess.add(recordId);
							break ROLES;

						} else if (READ.equals(role) && !write) {
							principalConceptsGivingAccess.add(recordId);
							break ROLES;

						}
					}
				}
			}
		}

		for (RecordId principalConceptId : principalConceptsGivingAccess) {
			if (retriever.hasRecordsWith(recordInPrincipalConcept(principalConceptId, onlyVisible))) {
				return true;
			}
		}

		return hasAccessToSomethingClassifiedInConcept(user, principalConcept.getRecordId(), write, onlyVisible, securityModel);

	}

}

