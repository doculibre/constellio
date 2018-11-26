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
import com.constellio.model.extensions.behaviors.RecordExtension;
import com.constellio.model.extensions.events.records.RecordCreationEvent;
import com.constellio.model.extensions.events.records.RecordModificationEvent;
import com.constellio.model.extensions.events.records.RecordPhysicalDeletionEvent;
import com.constellio.model.services.factories.ModelLayerFactory;
import com.constellio.model.services.schemas.MetadataList;

import java.util.HashMap;
import java.util.Map;

import static com.constellio.data.events.EventBusEventsExecutionStrategy.EXECUTED_LOCALLY_THEN_SENT_REMOTELY;

public class SecurityModelCache implements EventBusListener {

	public static final String cacheName = "securityModelCache";

	public static final String INVALIDATE_EVENT_TYPE = "invalidate";

	Map<String, SingletonSecurityModel> models = new HashMap<>();
	EventBus eventBus;

	public SecurityModelCache(final ModelLayerFactory modelLayerFactory) {
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

	@Override
	public void onEventReceived(Event event) {
		switch (event.getType()) {
			case INVALIDATE_EVENT_TYPE:
				String collection = event.getData();
				this.models.remove(collection);
				break;

			default:
				throw new ImpossibleRuntimeException("Unsupported event type '" + event.getType() + "'");
		}
	}

	public class SecurityModelCacheRecordExtension extends RecordExtension {

		@Override
		public void recordCreated(RecordCreationEvent event) {
			if (isRequiringSecurityModelInvalidation(event.getRecord(), null, true)) {
				invalidateIfLoaded(event.getRecord().getCollection());
			}
		}

		@Override
		public void recordModified(RecordModificationEvent event) {
			if (isRequiringSecurityModelInvalidation(event.getRecord(), event.getModifiedMetadatas(), false)) {
				invalidateIfLoaded(event.getRecord().getCollection());
			}
		}

		@Override
		public void recordPhysicallyDeleted(RecordPhysicalDeletionEvent event) {
			if (isRequiringSecurityModelInvalidation(event.getRecord(), null, true)) {
				invalidateIfLoaded(event.getRecord().getCollection());
			}
		}

		private boolean isRequiringSecurityModelInvalidation(Record record, MetadataList modifiedMetadatas,
															 boolean createdOrDeleted) {

			String schemaType = record.getTypeCode();

			switch (schemaType) {
				case User.SCHEMA_TYPE:
					return createdOrDeleted;


				case Group.SCHEMA_TYPE:
					return createdOrDeleted || (modifiedMetadatas != null && modifiedMetadatas.containsMetadataWithLocalCode(Group.PARENT));

				case Authorization.SCHEMA_TYPE:
					return true;

				default:
					return false;
			}
		}

	}
}
