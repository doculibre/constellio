package com.constellio.model.services.records.cache.cacheIndexHook.impl;

import com.constellio.model.entities.Taxonomy;
import com.constellio.model.entities.records.Record;
import com.constellio.model.entities.schemas.Metadata;
import com.constellio.model.entities.schemas.MetadataSchemaType;
import com.constellio.model.entities.schemas.Schemas;
import com.constellio.model.services.factories.ModelLayerFactory;
import com.constellio.model.services.records.RecordId;
import com.constellio.model.services.records.cache.cacheIndexHook.MetadataIndexCacheDataStoreHook;
import com.constellio.model.services.schemas.MetadataSchemasManager;
import com.constellio.model.services.taxonomies.TaxonomiesManager;
import org.apache.commons.lang3.StringUtils;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static com.constellio.model.services.records.RecordId.toId;
import static com.constellio.model.services.records.cache.cacheIndexHook.MetadataIndexCacheDataStoreHookUtils.combine;
import static com.constellio.model.services.records.cache.cacheIndexHook.MetadataIndexCacheDataStoreHookUtils.toLongKey;

public abstract class TaxonomyRecordsHook<K> implements MetadataIndexCacheDataStoreHook<K> {

	TaxonomiesManager taxonomiesManager;
	MetadataSchemasManager schemasManager;

	public TaxonomyRecordsHook(ModelLayerFactory modelLayerFactory) {
		this.taxonomiesManager = modelLayerFactory.getTaxonomiesManager();
		this.schemasManager = modelLayerFactory.getMetadataSchemasManager();
	}

	@Override
	public boolean isHooked(MetadataSchemaType schemaType) {
		List<Taxonomy> taxonomies = taxonomiesManager.getEnabledTaxonomies(schemaType.getCollection());
		return !schemaType.getAllReferencesToTaxonomySchemas(taxonomies).isEmpty();
	}

	@Override
	public boolean requiresDataUpdate(Record record) {
		for (Metadata metadata : schemasManager.getSchemaOf(record).getMetadatas()) {
			if (metadata.isTaxonomyRelationship() && record.isModified(metadata)) {
				return true;
			}
		}
		return record.isModified(Schemas.TOKENS) || record.isModified(Schemas.ATTACHED_ANCESTORS);
	}

	@Override
	public Set<K> getKeys(Record record) {

		RecordId principalRecordId = null;

		List<RecordId> principalAndPrimaryTaxonomyIds = new ArrayList<>();
		List<RecordId> secondaryTaxonomiesConceptIds = new ArrayList<>();
		Boolean detached = record.get(Schemas.IS_DETACHED_AUTHORIZATIONS);

		for (String token : record.<String>getList(Schemas.TOKENS)) {
			if (token.contains("_")) {
				principalAndPrimaryTaxonomyIds.add(toId(StringUtils.substringAfterLast(token, "_")));
			}
		}

		for (Metadata metadata : schemasManager.getSchemaOf(record).getMetadatas()) {
			if (metadata.isTaxonomyRelationship()) {
				boolean principalTaxonomy = taxonomiesManager
						.isTypeInPrincipalTaxonomy(record.getCollection(), metadata.getReferencedSchemaType());
				for (String value : record.<String>getValues(metadata)) {
					RecordId recordId = toId(value);
					if (principalTaxonomy) {
						principalRecordId = recordId;
						principalAndPrimaryTaxonomyIds.add(recordId);
					} else {
						secondaryTaxonomiesConceptIds.add(recordId);
					}
				}
			}
		}
		Set<K> keys = new HashSet<>();

		if (principalRecordId != null && !Boolean.TRUE.equals(detached)) {
			keys.add(key(principalRecordId));
		}

		for (RecordId recordId : principalAndPrimaryTaxonomyIds) {
			for (RecordId secondaryTaxonomyRecordId : secondaryTaxonomiesConceptIds) {
				keys.add(key(recordId, secondaryTaxonomyRecordId));
			}
		}

		return keys;
	}

	protected abstract K key(RecordId recordId);

	protected abstract K key(RecordId recordId1, RecordId recordId2);

	public static class TaxonomyRecordsHook_Optimized extends TaxonomyRecordsHook<Long> {

		public TaxonomyRecordsHook_Optimized(ModelLayerFactory modelLayerFactory) {
			super(modelLayerFactory);
		}

		@Override
		protected Long key(RecordId recordId) {
			return toLongKey(recordId);
		}

		@Override
		protected Long key(RecordId recordId1, RecordId recordId2) {
			return combine(recordId1, recordId2);
		}

		@Override
		public Class<?> getKeyType() {
			return Long.class;
		}
	}

	public static class TaxonomyRecordsHook_Debug extends TaxonomyRecordsHook<String> {

		public TaxonomyRecordsHook_Debug(ModelLayerFactory modelLayerFactory) {
			super(modelLayerFactory);
		}

		@Override
		protected String key(RecordId recordId) {
			return recordId.stringValue();
		}

		@Override
		protected String key(RecordId recordId1, RecordId recordId2) {
			return recordId1.stringValue() + "-" + recordId2.stringValue();
		}

		@Override
		public Class<?> getKeyType() {
			return String.class;
		}
	}
}
