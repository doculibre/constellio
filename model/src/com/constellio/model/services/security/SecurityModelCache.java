package com.constellio.model.services.security;

import com.constellio.data.events.Event;
import com.constellio.data.events.EventBus;
import com.constellio.data.events.EventBusListener;
import com.constellio.data.utils.ImpossibleRuntimeException;
import com.constellio.model.entities.records.Record;
import com.constellio.model.entities.records.wrappers.Authorization;
import com.constellio.model.entities.records.wrappers.Group;
import com.constellio.model.entities.records.wrappers.User;
import com.constellio.model.entities.security.SingletonSecurityModel;
import com.constellio.model.entities.security.global.SystemWideGroup;
import com.constellio.model.extensions.behaviors.RecordExtension;
import com.constellio.model.extensions.events.records.RecordPhysicalDeletionEvent;
import com.constellio.model.extensions.events.records.TransactionExecutedEvent;
import com.constellio.model.services.factories.ModelLayerFactory;
import com.constellio.model.services.records.SchemasRecordsServices;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.constellio.data.events.EventBusEventsExecutionStrategy.EXECUTED_LOCALLY_THEN_SENT_REMOTELY;

public class SecurityModelCache implements EventBusListener {

	public static final String cacheName = "securityModelCache";

	public static final String INVALIDATE_EVENT_TYPE = "removeFromAllCaches";
	public static final String UPDATE_CACHE_EVENT_TYPE = "authsCreated";
	public static final String REMOVE_AUTH_EVENT_TYPE = "authDeleted";

	Map<String, SingletonSecurityModel> models = new HashMap<>();
	EventBus eventBus;
	ModelLayerFactory modelLayerFactory;

	public SecurityModelCache(final ModelLayerFactory modelLayerFactory) {
		this.modelLayerFactory = modelLayerFactory;
		eventBus = modelLayerFactory.getDataLayerFactory().getEventBusManager()
				.createEventBus(cacheName, EXECUTED_LOCALLY_THEN_SENT_REMOTELY);
		eventBus.register(this);

		modelLayerFactory.getExtensions().getSystemWideExtensions().recordExtensions.add(new SecurityModelCacheRecordExtension());

	}


	public SingletonSecurityModel getCached(String collection) {
		return models.get(collection);
	}

	public void insert(SingletonSecurityModel model) {
		this.models.put(model.getCollection(), model);
	}

	private void invalidateIfLoaded(String collection) {
		if (models.containsKey(collection)) {
			eventBus.send(INVALIDATE_EVENT_TYPE, collection);
		}
	}

	private void updateCache(String collection, List<String> createdAuthsIds, List<String> modifiedAuthsIds) {
		if (models.containsKey(collection)) {
			Map<String, Object> values = new HashMap<>();
			values.put("collection", collection);
			values.put("createdAuthsIds", createdAuthsIds);
			values.put("modifiedAuthsIds", modifiedAuthsIds);
			eventBus.send(UPDATE_CACHE_EVENT_TYPE, values);
		}
	}

	private void removeAuth(String collection, String authId) {
		if (models.containsKey(collection)) {
			Map<String, Object> values = new HashMap<>();
			values.put("collection", collection);
			values.put("authId", authId);
			eventBus.send(REMOVE_AUTH_EVENT_TYPE, values);
		}
	}

	@Override
	public void onEventReceived(Event event) {
		switch (event.getType()) {
			case INVALIDATE_EVENT_TYPE:
				String collection = event.getData();
				this.models.remove(collection);
				break;

			case UPDATE_CACHE_EVENT_TYPE:
				collection = event.getData("collection");
				SingletonSecurityModel securityModel = this.models.get(collection);
				if (securityModel != null) {
					SchemasRecordsServices schemas = new SchemasRecordsServices(collection, modelLayerFactory);
					securityModel.updateCache(
							schemas.getSolrAuthorizationDetailss(event.<List<String>>getData("createdAuthsIds")),
							schemas.getSolrAuthorizationDetailss(event.<List<String>>getData("modifiedAuthsIds")));
				}
				break;

			case REMOVE_AUTH_EVENT_TYPE:
				securityModel = this.models.get(event.<String>getData("collection"));
				if (securityModel != null) {
					securityModel.removeAuth(event.<String>getData("authId"));
				}
				break;

			default:
				throw new ImpossibleRuntimeException("Unsupported event type '" + event.getType() + "'");
		}
	}

	public void invalidate(String collection) {
		invalidateIfLoaded(collection);
	}

	public class SecurityModelCacheRecordExtension extends RecordExtension {

		@Override
		public void transactionExecuted(TransactionExecutedEvent event) {

			List<String> authCreated = new ArrayList<>();
			List<String> authModified = new ArrayList<>();

			boolean allCollectionInvalidateRequired = false;
			boolean fullInvalidateRequired = false;

			for (Record newRecord : event.getNewRecords()) {

				switch (newRecord.getTypeCode()) {
					case Group.SCHEMA_TYPE:
						fullInvalidateRequired = true;
						break;
					case User.SCHEMA_TYPE:
						fullInvalidateRequired = true;
						break;

					case Authorization.SCHEMA_TYPE:
						authCreated.add(newRecord.getId());
						break;
				}
			}

			for (Record modifiedRecord : event.getUpdatedRecords()) {
				switch (modifiedRecord.getTypeCode()) {
					case Group.SCHEMA_TYPE:
						fullInvalidateRequired = event.getModifiedMetadataListOf(modifiedRecord).containsMetadataWithLocalCode(Group.PARENT);
						break;

					case SystemWideGroup.SCHEMA_TYPE:
						allCollectionInvalidateRequired = event.getModifiedMetadataListOf(modifiedRecord).containsMetadataWithLocalCode(SystemWideGroup.STATUS);
						break;

					case User.SCHEMA_TYPE:
						allCollectionInvalidateRequired = event.getModifiedMetadataListOf(modifiedRecord).containsMetadataWithLocalCode(User.GROUPS);
						break;

					case Authorization.SCHEMA_TYPE:
						authModified.add(modifiedRecord.getId());
						break;
				}
			}

			if (allCollectionInvalidateRequired) {
				for (String collection : modelLayerFactory.getCollectionsListManager().getCollections()) {
					invalidateIfLoaded(collection);
				}

			} else if (fullInvalidateRequired) {
				invalidateIfLoaded(event.getTransaction().getCollection());

			} else if (!authCreated.isEmpty() || !authModified.isEmpty()) {
				updateCache(event.getTransaction().getCollection(), authCreated, authModified);
			}
		}

		@Override
		public void recordPhysicallyDeleted(RecordPhysicalDeletionEvent event) {

			switch (event.getRecord().getTypeCode()) {
				case Group.SCHEMA_TYPE:
					invalidateIfLoaded(event.getRecord().getCollection());
					break;

				case Authorization.SCHEMA_TYPE:
					removeAuth(event.getRecord().getCollection(), event.getRecord().getId());
					break;
			}
		}


	}
}
