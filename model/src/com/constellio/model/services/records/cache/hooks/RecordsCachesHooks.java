package com.constellio.model.services.records.cache.hooks;

import com.constellio.model.entities.schemas.MetadataSchemaType;
import com.constellio.model.entities.schemas.MetadataSchemaTypes;
import com.constellio.model.services.collections.CollectionsListManager;
import com.constellio.model.services.factories.ModelLayerFactory;
import com.constellio.model.services.schemas.MetadataSchemasManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;

public class RecordsCachesHooks {

	private static final Logger LOGGER = LoggerFactory.getLogger(RecordsCachesHook.class);

	private RecordsCachesHook[][] hooks = new RecordsCachesHook[256][];

	private List<RecordsCachesHook> registerdHooks = new ArrayList<>();

	private MetadataSchemasManager metadataSchemasManager;
	private CollectionsListManager collectionsListManager;

	public RecordsCachesHooks(ModelLayerFactory modelLayerFactory) {
		this.collectionsListManager = modelLayerFactory.getCollectionsListManager();
		this.metadataSchemasManager = modelLayerFactory.getMetadataSchemasManager();
	}

	public void register(RecordsCachesHook hook) {
		for (String collection : collectionsListManager.getCollectionsExcludingSystem()) {
			MetadataSchemaTypes schemaTypes = metadataSchemasManager.getSchemaTypes(collection);
			RecordsCachesHook[] schemaTypesHooks = getRegisteredHooks(schemaTypes);
			List<String> hookedSchemaTypesCodes = hook.getHookedSchemaTypes(schemaTypes);
			if (hookedSchemaTypesCodes != null) {
				for (String hookedSchemaTypeCode : hookedSchemaTypesCodes) {
					if (schemaTypes.hasType(hookedSchemaTypeCode)) {
						MetadataSchemaType metadataSchemaType = schemaTypes.getSchemaType(hookedSchemaTypeCode);
						schemaTypesHooks[metadataSchemaType.getId()] = hook;
					} else {
						LOGGER.warn(hook.getClass().getName() + " not registered in collection for schema type '"
									+ hookedSchemaTypeCode + "' : Type does not exist in collection " + collection);
					}
				}
			}
		}

		this.registerdHooks.add(hook);
	}

	private RecordsCachesHook[] getRegisteredHooks(MetadataSchemaTypes schemaTypes) {

		RecordsCachesHook[] collectionHooks = hooks[schemaTypes.getCollectionInfo().getCollectionIndex()];
		if (collectionHooks == null) {
			collectionHooks = new RecordsCachesHook[MetadataSchemaTypes.LIMIT_OF_TYPES_IN_COLLECTION];

			for (RecordsCachesHook hook : registerdHooks) {
				List<String> hookedSchemaTypesCodes = hook.getHookedSchemaTypes(schemaTypes);
				if (hookedSchemaTypesCodes != null) {
					for (String hookedSchemaTypeCode : hookedSchemaTypesCodes) {
						if (schemaTypes.hasType(hookedSchemaTypeCode)) {
							MetadataSchemaType metadataSchemaType = schemaTypes.getSchemaType(hookedSchemaTypeCode);
							collectionHooks[metadataSchemaType.getId()] = hook;
						} else {
							LOGGER.warn(hook.getClass().getName() + " not registered in collection for schema type '"
										+ hookedSchemaTypeCode + "' : Type does not exist in collection " + schemaTypes.getCollection());
						}
					}
				}
			}

			hooks[schemaTypes.getCollectionInfo().getCollectionIndex()] = collectionHooks;
		}
		return collectionHooks;
	}

	public List<RecordsCachesHook> getRegisteredHooks() {
		return registerdHooks;
	}

	public RecordsCachesHook getSchemaTypeHook(MetadataSchemaTypes schemaTypes, short schemaTypeId) {
		return getRegisteredHooks(schemaTypes)[schemaTypeId];
	}


}
