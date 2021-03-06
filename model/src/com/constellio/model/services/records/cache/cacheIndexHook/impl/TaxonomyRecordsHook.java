package com.constellio.model.services.records.cache.cacheIndexHook.impl;

import com.constellio.data.dao.dto.records.RecordId;
import com.constellio.model.entities.Taxonomy;
import com.constellio.model.entities.records.Record;
import com.constellio.model.entities.schemas.MetadataSchemaType;
import com.constellio.model.services.factories.ModelLayerFactory;
import com.constellio.model.services.records.cache.cacheIndexHook.MetadataIndexCacheDataStoreHook;
import com.constellio.model.services.schemas.MetadataSchemasManager;
import com.constellio.model.services.taxonomies.TaxonomiesManager;
import org.apache.commons.lang3.StringUtils;

import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static com.constellio.data.dao.dto.records.RecordId.toId;
import static com.constellio.model.entities.schemas.Schemas.ATTACHED_PRINCIPAL_ANCESTORS_INT_IDS;
import static com.constellio.model.entities.schemas.Schemas.PRINCIPALS_ANCESTORS_INT_IDS;
import static com.constellio.model.entities.schemas.Schemas.PRINCIPAL_CONCEPTS_INT_IDS;
import static com.constellio.model.entities.schemas.Schemas.SECONDARY_CONCEPTS_INT_IDS;
import static com.constellio.model.entities.schemas.Schemas.TOKENS;
import static com.constellio.model.entities.schemas.Schemas.VISIBLE_IN_TREES;
import static com.constellio.model.services.records.cache.cacheIndexHook.impl.TaxonomyRecordsHookKey.attachedRecordInPrincipalConcept;
import static com.constellio.model.services.records.cache.cacheIndexHook.impl.TaxonomyRecordsHookKey.principalAccessOnRecordInConcept;
import static com.constellio.model.services.records.cache.cacheIndexHook.impl.TaxonomyRecordsHookKey.principalConceptAuthGivingAccessToRecordInSecondaryConceptKey;
import static com.constellio.model.services.records.cache.cacheIndexHook.impl.TaxonomyRecordsHookKey.recordInSecondaryConcept;
import static java.util.stream.Collectors.toList;

public class TaxonomyRecordsHook implements MetadataIndexCacheDataStoreHook<TaxonomyRecordsHookKey> {

	private ModelLayerFactory modelLayerFactory;
	private TaxonomiesManager taxonomiesManager;
	private MetadataSchemasManager schemasManager;
	private String collection;

	public TaxonomyRecordsHook(String collection, ModelLayerFactory modelLayerFactory) {
		this.collection = collection;
		this.modelLayerFactory = modelLayerFactory;
		this.taxonomiesManager = modelLayerFactory.getTaxonomiesManager();
		this.schemasManager = modelLayerFactory.getMetadataSchemasManager();
	}

	@Override
	public String getCollection() {
		return collection;
	}

	@Override
	public boolean isHooked(MetadataSchemaType schemaType) {
		List<Taxonomy> taxonomies = taxonomiesManager.getEnabledTaxonomies(schemaType.getCollection());
		//TODO Francis : Corriger cette passe de l'ours épouvantable
		return !schemaType.getAllReferencesToTaxonomySchemas(taxonomies).isEmpty() || "document".equals(schemaType.getCode());
	}

	@Override
	public boolean requiresDataUpdate(Record record) {
		return record.isAnyModified(TOKENS, VISIBLE_IN_TREES, ATTACHED_PRINCIPAL_ANCESTORS_INT_IDS, SECONDARY_CONCEPTS_INT_IDS);
	}

	@Override
	public Set<TaxonomyRecordsHookKey> getKeys(Record record) {

		if (record.isLogicallyDeleted()) {
			return Collections.emptySet();
		}

		boolean visible = !Boolean.FALSE.equals(record.get(VISIBLE_IN_TREES));

		List<RecordId> principalConcepts = record.<Integer>getList(PRINCIPALS_ANCESTORS_INT_IDS)
				.stream().map(RecordId::toId).collect(toList());
		List<RecordId> attachedPrincipalConcepts = record.<Integer>getList(ATTACHED_PRINCIPAL_ANCESTORS_INT_IDS)
				.stream().map(RecordId::toId).collect(toList());
		List<RecordId> secondaryConceptsIntIds = record.<Integer>getList(SECONDARY_CONCEPTS_INT_IDS)
				.stream().map(RecordId::toId).collect(toList());
		List<RecordId> principalConceptsIntIds = record.<Integer>getList(PRINCIPAL_CONCEPTS_INT_IDS)
				.stream().map(RecordId::toId).collect(toList());


		Set<RecordId> principalIdsWithTokenReadAccess = new HashSet<>();
		Set<RecordId> principalIdsWithTokenWriteAccess = new HashSet<>();

		for (String token : record.<String>getList(TOKENS)) {
			if (token != null) {
				if (token.startsWith("w_")) {
					RecordId recordId = toId(StringUtils.substringAfterLast(token, "_"));
					//if (principalConceptsIntIds.contains(recordId)) {
					principalIdsWithTokenWriteAccess.add(recordId);
					//}
				} else if (token.startsWith("r_")) {
					RecordId recordId = toId(StringUtils.substringAfterLast(token, "_"));
					//if (principalConceptsIntIds.contains(recordId)) {
					principalIdsWithTokenReadAccess.add(recordId);
					//}
				}
			}
		}

		Set<TaxonomyRecordsHookKey> keys = new HashSet<>();
		principalIdsWithTokenReadAccess.removeAll(principalIdsWithTokenWriteAccess);

		for (RecordId secondaryTaxonomyRecordId : secondaryConceptsIntIds) {
			keys.add(recordInSecondaryConcept(secondaryTaxonomyRecordId, visible));
			for (RecordId principalId : principalIdsWithTokenReadAccess) {
				keys.add(principalAccessOnRecordInConcept(principalId, secondaryTaxonomyRecordId, false, visible));
			}

			for (RecordId principalId : principalIdsWithTokenWriteAccess) {
				keys.add(principalAccessOnRecordInConcept(principalId, secondaryTaxonomyRecordId, true, visible));
			}

			for (RecordId principalRecordId : attachedPrincipalConcepts) {
				if (principalConceptsIntIds.contains(principalRecordId)) {
					keys.add(principalConceptAuthGivingAccessToRecordInSecondaryConceptKey(principalRecordId, secondaryTaxonomyRecordId, visible));
				}
			}
		}

		for (RecordId principalRecordId : principalConcepts) {
			for (RecordId principalId : principalIdsWithTokenReadAccess) {
				if (principalConceptsIntIds.contains(principalRecordId)) {
					keys.add(principalAccessOnRecordInConcept(principalId, principalRecordId, false, visible));
				}
			}

			for (RecordId principalId : principalIdsWithTokenWriteAccess) {
				if (principalConceptsIntIds.contains(principalRecordId)) {
					keys.add(principalAccessOnRecordInConcept(principalId, principalRecordId, true, visible));
				}
			}
		}

		for (RecordId principalRecordId : attachedPrincipalConcepts) {
			if (principalConceptsIntIds.contains(principalRecordId)) {
				keys.add(attachedRecordInPrincipalConcept(principalRecordId, visible));
			}
		}

		return keys;
	}


	@Override
	public Class<?> getKeyType() {
		return TaxonomyRecordsHookKey.class;
	}

	@Override
	public int getKeyMemoryLength() {
		return Integer.BYTES + Integer.BYTES + 1 + 1;
	}

}
