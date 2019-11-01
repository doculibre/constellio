package com.constellio.model.services.records.cache.cacheIndexHook.impl;

import com.constellio.model.entities.records.wrappers.User;
import com.constellio.model.entities.schemas.Metadata;
import com.constellio.model.entities.schemas.MetadataSchemaType;
import com.constellio.model.entities.security.SecurityModel;
import com.constellio.model.entities.security.SecurityModelAuthorization;
import com.constellio.model.services.factories.ModelLayerFactory;
import com.constellio.model.services.records.RecordId;
import com.constellio.model.services.records.cache.cacheIndexHook.RecordCountHookDataIndexRetriever;

import static com.constellio.model.services.records.RecordId.toId;
import static com.constellio.model.services.records.cache.cacheIndexHook.MetadataIndexCacheDataStoreHookUtils.combine;
import static com.constellio.model.services.records.cache.cacheIndexHook.MetadataIndexCacheDataStoreHookUtils.toLongKey;

public abstract class TaxonomyRecordsHookRetriever<K> {

	RecordCountHookDataIndexRetriever<K> retriever;
	ModelLayerFactory modelLayerFactory;

	public TaxonomyRecordsHookRetriever(
			RecordCountHookDataIndexRetriever<K> retriever, ModelLayerFactory modelLayerFactory) {
		this.retriever = retriever;
		this.modelLayerFactory = modelLayerFactory;
	}

	public boolean hasUserAccessToSomethingInSecondaryConcept(User user, RecordId conceptId,
															  MetadataSchemaType conceptSchemaType) {

		SecurityModel securityModel = modelLayerFactory.newRecordServices().getSecurityModel(user.getCollection());
		if (hasAccessToSomethingWithASpecificTokenInConcept(user, conceptId, securityModel)
			|| hasAccessWithConceptAuthInSecondaryConcept(user, conceptId, securityModel)) {
			return true;

		} else {

			Metadata parent = conceptSchemaType.getAllParentReferencesTo(conceptSchemaType.getCode()).get(0);
			return modelLayerFactory.getRecordsCaches().getRecordsByIndexedMetadata(
					conceptSchemaType, parent, conceptId.stringValue()).anyMatch((
					c) -> hasUserAccessToSomethingInSecondaryConcept(user, c.getRecordId(), conceptSchemaType));

		}
	}

	private boolean hasAccessWithConceptAuthInSecondaryConcept(User user, RecordId conceptId,
															   SecurityModel securityModel) {
		for (SecurityModelAuthorization auth : securityModel.getAuthorizationsToPrincipal(user.getId(), true)) {
			if (!auth.isSecurableRecord() && auth.getDetails().isActiveAuthorization()) {
				if (retriever.hasRecordsWith(key(toId(auth.getDetails().getTarget()), conceptId))) {
					return true;
				}
			}
		}
		return false;
	}

	private boolean hasAccessWithConceptAuth(User user, RecordId conceptId, SecurityModel securityModel) {
		return retriever.hasRecordsWith(key(conceptId));
	}

	private boolean hasAccessToSomethingWithASpecificTokenInConcept(User user, RecordId conceptId,
																	SecurityModel securityModel) {
		if (retriever.hasRecordsWith(key(user.getWrappedRecordId(), conceptId))) {
			return true;
		}

		for (String groupId : user.getUserGroupsOrEmpty()) {
			if (securityModel.isGroupActive(groupId)) {
				if (retriever.hasRecordsWith(key(toId(groupId), conceptId))) {
					return true;
				}
			}
		}
		return false;
	}

	public boolean hasUserAccessToSomethingInPrincipalConcept(User user, RecordId conceptId,
															  MetadataSchemaType conceptSchemaType) {

		SecurityModel securityModel = modelLayerFactory.newRecordServices().getSecurityModel(user.getCollection());
		if (hasAccessToSomethingWithASpecificTokenInConcept(user, conceptId, securityModel)
			|| hasAccessWithConceptAuth(user, conceptId, securityModel)) {
			return true;

		} else {
			Metadata parent = conceptSchemaType.getAllParentReferencesTo(conceptSchemaType.getCode()).get(0);
			return modelLayerFactory.getRecordsCaches().getRecordsByIndexedMetadata(
					conceptSchemaType, parent, conceptId.stringValue()).anyMatch((
					c) -> hasUserAccessToSomethingInPrincipalConcept(user, c.getRecordId(), conceptSchemaType));

		}
	}

	protected abstract K key(RecordId recordId);

	protected abstract K key(RecordId recordId1, RecordId recordId2);

	public static class TaxonomyRecordsHookRetriever_Optimized extends TaxonomyRecordsHookRetriever<Long> {

		public TaxonomyRecordsHookRetriever_Optimized(
				RecordCountHookDataIndexRetriever<Long> retriever,
				ModelLayerFactory modelLayerFactory) {
			super(retriever, modelLayerFactory);
		}

		@Override
		protected Long key(RecordId recordId) {
			return toLongKey(recordId);
		}

		@Override
		protected Long key(RecordId recordId1, RecordId recordId2) {
			return combine(recordId1, recordId2);
		}

	}

	public static class TaxonomyRecordsHookRetriever_Debug extends TaxonomyRecordsHookRetriever<String> {


		public TaxonomyRecordsHookRetriever_Debug(
				RecordCountHookDataIndexRetriever<String> retriever,
				ModelLayerFactory modelLayerFactory) {
			super(retriever, modelLayerFactory);
		}

		@Override
		protected String key(RecordId recordId) {
			return recordId.stringValue();
		}

		@Override
		protected String key(RecordId recordId1, RecordId recordId2) {
			return recordId1.stringValue() + "-" + recordId2.stringValue();
		}

	}
}

