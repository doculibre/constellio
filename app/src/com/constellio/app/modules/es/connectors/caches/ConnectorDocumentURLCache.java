package com.constellio.app.modules.es.connectors.caches;

import com.constellio.app.modules.es.connectors.caches.ConnectorDocumentURLCacheRuntimeException.ConnectorDocumentURLCacheRuntimeException_CouldNotLockDocumentForFetching;
import com.constellio.app.modules.es.model.connectors.ConnectorDocument;
import com.constellio.app.modules.es.model.connectors.http.ConnectorHttpDocument;
import com.constellio.app.modules.es.services.ESSchemasRecordsServices;
import com.constellio.app.services.factories.AppLayerFactory;
import com.constellio.data.dao.services.cache.ConstellioCache;
import com.constellio.data.dao.services.cache.ConstellioCacheOptions;
import com.constellio.data.dao.services.cache.InsertionReason;
import com.constellio.data.dao.services.leaderElection.LeaderElectionManagerObserver;
import com.constellio.data.utils.TimeProvider;
import com.constellio.model.entities.records.Record;
import com.constellio.model.entities.schemas.MetadataSchema;
import com.constellio.model.entities.schemas.Schemas;
import com.constellio.model.extensions.behaviors.RecordExtension;
import com.constellio.model.extensions.events.records.RecordCreationEvent;
import com.constellio.model.extensions.events.records.RecordModificationEvent;
import com.constellio.model.services.caches.CollectionCache;
import com.constellio.model.services.records.RecordServices;
import org.joda.time.LocalDateTime;

import java.io.Serializable;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import static com.constellio.app.modules.es.connectors.caches.ConnectorDocumentURLCache.ConnectorDocumentStatus.CURRENTLY_FETCHED;
import static com.constellio.app.modules.es.connectors.caches.ConnectorDocumentURLCache.ConnectorDocumentStatus.FETCHED;
import static com.constellio.app.modules.es.connectors.caches.ConnectorDocumentURLCache.ConnectorDocumentStatus.NOT_FETCHED;

public class ConnectorDocumentURLCache implements CollectionCache, LeaderElectionManagerObserver {

	private static final String CACHE_NAME = "ConnectorDocumentURLCache";

	ConstellioCache cache;

	String collection;

	String connectorId;

	AppLayerFactory appLayerFactory;

	RecordServices recordServices;

	ESSchemasRecordsServices es;

	private boolean loadedCache;

	private boolean currentLeaderStatus;

	public ConnectorDocumentURLCache(String collection, String connectorId,
									 AppLayerFactory appLayerFactory) {
		this.collection = collection;
		this.connectorId = connectorId;
		this.appLayerFactory = appLayerFactory;
		this.recordServices = appLayerFactory.getModelLayerFactory().newRecordServices();
		this.es = new ESSchemasRecordsServices(collection, appLayerFactory);

		ConstellioCacheOptions options = new ConstellioCacheOptions();
		options.setInvalidateRemotelyWhenPutting(false);

		cache = this.appLayerFactory.getModelLayerFactory().getDataLayerFactory().getLocalCacheManager()
				.createCache(CACHE_NAME + "-" + collection + "-" + connectorId, options);
	}

	private synchronized void setNewDocumentStatus(Record record, ConnectorDocumentStatus status) {

		LocalDateTime fetchingStartTime = status == CURRENTLY_FETCHED ? TimeProvider.getLocalDateTime() : null;
		ConnectorDocumentURLCacheEntry value = new ConnectorDocumentURLCacheEntry(connectorId, status,
				fetchingStartTime, populateExtraCacheFields(record));

		cache.put(record.<String>get(Schemas.URL), value, InsertionReason.WAS_MODIFIED);
	}

	private ConnectorDocumentStatus getDocumentStatus(String connectorId, String url) {
		ConnectorDocumentURLCacheEntry entry = cache.get(url);
		return entry == null ? ConnectorDocumentStatus.NOT_FETCHED : entry.status;
	}

	private String getDocumentId(String url) {
		ConnectorDocumentURLCacheEntry entry = cache.get(url);
		return entry == null ? null : entry.id;
	}

	public boolean isLockableForFetching(String url)
			throws ConnectorDocumentURLCacheRuntimeException_CouldNotLockDocumentForFetching {

		boolean lockable = false;
		if (currentLeaderStatus) {


			ConnectorDocumentURLCacheEntry entry = cache.get(url);
			if (entry == null) {
				lockable = true;

			} else {
				if (entry.status == NOT_FETCHED) {
					lockable = true;
				}

				if (entry.status == CURRENTLY_FETCHED
					&& entry.fetchingStartTime.plusMinutes(10).isBefore(TimeProvider.getLocalDateTime())) {
					lockable = true;
				}
			}
		}

		return lockable;
	}

	public synchronized void lockDocumentForFetching(String url) {
		if (!isLockableForFetching(url)) {
			throw new ConnectorDocumentURLCacheRuntimeException_CouldNotLockDocumentForFetching(url, currentLeaderStatus);
		}

		ConnectorDocumentURLCacheEntry currentEntry = cache.get(url);
		String id = currentEntry == null ? null : currentEntry.id;

		LocalDateTime fetchingStartTime = TimeProvider.getLocalDateTime();
		ConnectorDocumentURLCacheEntry value = new ConnectorDocumentURLCacheEntry(id, CURRENTLY_FETCHED,
				fetchingStartTime, new HashMap<String, Object>());
		cache.put(url, value, InsertionReason.WAS_MODIFIED);
	}

	public void initialize() {

		appLayerFactory.getModelLayerFactory().getExtensions().forCollection(collection).recordExtensions.add(new RecordExtension() {
			@Override
			public void recordCreated(RecordCreationEvent event) {
				if (isConnectorDocument(event.getRecord())) {
					updateCacheFromNewRecordState(event.getRecord(), event.getSchema());
				}

			}

			@Override
			public void recordModified(RecordModificationEvent event) {
				if (isConnectorDocument(event.getRecord())) {
					updateCacheFromNewRecordState(event.getRecord(), event.getSchema());
				}
			}

			private void updateCacheFromNewRecordState(Record record, MetadataSchema schema) {
				boolean fetched = Boolean.TRUE.equals(record.get(Schemas.FETCHED));
				String connectorId = record.get(schema.get(ConnectorDocument.CONNECTOR));

				if (ConnectorDocumentURLCache.this.connectorId.equals(connectorId)) {
					setNewDocumentStatus(record, fetched ? FETCHED : NOT_FETCHED);
				}
			}

		});

		appLayerFactory.getModelLayerFactory().getDataLayerFactory().getLeaderElectionService().register(this);
	}

	private boolean isConnectorDocument(Record record) {
		return ConnectorHttpDocument.SCHEMA_TYPE.equals(record.getTypeCode());
	}

	public void onConnectorResume() {

	}

	public void onConnectorStart() {

	}

	public void onConnectorGetJobsCalled() {

	}

	public void onConnectorCleanup() {

	}

	protected Map<String, Object> populateExtraCacheFields(Record connectorDocumentRecord) {
		return Collections.emptyMap();
	}

	@Override
	public void invalidateAll() {
		cache.clear();
	}

	@Override
	public ConstellioCache getCache() {
		return cache;
	}

	@Override
	public void onLeaderStatusChanged(boolean newStatus) {
		currentLeaderStatus = newStatus;
		cache.clear();
	}


	enum ConnectorDocumentStatus {
		FETCHED,

		CURRENTLY_FETCHED,

		NOT_FETCHED,
	}


	public static class ConnectorDocumentURLCacheEntry implements Serializable {
		String id;
		ConnectorDocumentStatus status;
		Map<String, Object> extraParams;
		LocalDateTime fetchingStartTime;

		public ConnectorDocumentURLCacheEntry(String id, ConnectorDocumentStatus status,
											  LocalDateTime fetchingStartTime, Map<String, Object> extraParams) {
			this.id = id;
			this.status = status;
			this.extraParams = extraParams;
			this.fetchingStartTime = fetchingStartTime;
		}
	}


}
