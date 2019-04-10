package com.constellio.app.modules.es.connectors.caches;

import com.constellio.app.modules.es.model.connectors.ConnectorDocument;
import com.constellio.app.modules.es.model.connectors.ConnectorInstance;
import com.constellio.app.modules.es.services.ESSchemasRecordsServices;
import com.constellio.app.services.factories.AppLayerFactory;
import com.constellio.data.dao.services.bigVault.SearchResponseIterator;
import com.constellio.data.dao.services.cache.ConstellioCache;
import com.constellio.data.dao.services.cache.InsertionReason;
import com.constellio.data.dao.services.leaderElection.LeaderElectionManagerObserver;
import com.constellio.data.dao.services.leaderElection.ObservableLeaderElectionManager;
import com.constellio.data.utils.TimeProvider;
import com.constellio.model.entities.records.Record;
import com.constellio.model.entities.schemas.Metadata;
import com.constellio.model.entities.schemas.MetadataSchema;
import com.constellio.model.entities.schemas.MetadataSchemaType;
import com.constellio.model.extensions.behaviors.RecordExtension;
import com.constellio.model.extensions.events.records.RecordCreationEvent;
import com.constellio.model.extensions.events.records.RecordModificationEvent;
import com.constellio.model.services.caches.CollectionCache;
import com.constellio.model.services.records.RecordServices;
import com.constellio.model.services.search.query.ReturnedMetadatasFilter;
import com.constellio.model.services.search.query.logical.LogicalSearchQuery;
import org.jetbrains.annotations.NotNull;
import org.joda.time.LocalDateTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.constellio.app.modules.es.connectors.caches.ConnectorDocumentURLCacheStatus.CURRENTLY_FETCHED;
import static com.constellio.app.modules.es.connectors.caches.ConnectorDocumentURLCacheStatus.NOT_FETCHED;
import static com.constellio.app.modules.es.model.connectors.ConnectorDocument.URL;
import static com.constellio.data.dao.services.cache.InsertionReason.WAS_MODIFIED;
import static com.constellio.data.dao.services.cache.InsertionReason.WAS_OBTAINED;
import static com.constellio.model.services.search.query.logical.LogicalSearchQueryOperators.from;
import static org.apache.commons.lang3.StringUtils.isBlank;

public class ConnectorDocumentURLCache implements CollectionCache, LeaderElectionManagerObserver {

	private static final Logger LOGGER = LoggerFactory.getLogger(ConnectorDocumentURLCache.class);

	private static final String CACHE_NAME = "ConnectorDocumentURLCache";

	ConstellioCache cache;

	String collection;

	String connectorId;

	String connectorCode;

	AppLayerFactory appLayerFactory;

	RecordServices recordServices;

	ESSchemasRecordsServices es;

	private boolean loadedCache;

	private boolean currentLeaderStatus;

	private List<String> cachedSchemaTypes;

	private List<String> cachedMetadatas = new ArrayList<>();

	public ConnectorDocumentURLCache(ConnectorInstance connectorInstance,
									 AppLayerFactory appLayerFactory, List<String> cachedSchemaTypes) {
		this(connectorInstance.getCollection(), connectorInstance.getId(), connectorInstance.getCode(), appLayerFactory, cachedSchemaTypes);
	}

	public ConnectorDocumentURLCache(String collection, String connectorId, String connectorCode,
									 AppLayerFactory appLayerFactory, List<String> cachedSchemaTypes) {
		this.collection = collection;
		this.connectorCode = connectorCode;
		this.connectorId = connectorId;
		this.appLayerFactory = appLayerFactory;
		this.recordServices = appLayerFactory.getModelLayerFactory().newRecordServices();
		this.cachedSchemaTypes = cachedSchemaTypes;
		this.es = new ESSchemasRecordsServices(collection, appLayerFactory);

		cache = this.appLayerFactory.getModelLayerFactory().getDataLayerFactory().getLocalCacheManager()
				.getCache(CACHE_NAME + "-" + collection + "-" + connectorId);

		initialize();
	}

	public void addCachedMetadata(String cachedMetadata) {
		if (loadedCache) {
			throw new IllegalStateException("Cannot add cached metadata once the cache is loaded");
		}

		if (URL.equals(cachedMetadata) || ConnectorDocument.FETCHED.equals(cachedMetadata) || isBlank(cachedMetadata)) {
			throw new IllegalStateException("Invalid metadata : " + cachedMetadata);
		}
		this.cachedMetadatas.add(cachedMetadata);
	}


	public boolean isLockableForFetching(String url) {

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

	public ConnectorDocumentURLCacheEntry getEntry(String url) {
		return cache.get(url);
	}

	public boolean exists(String url) {
		return cache.get(url) != null;
	}


	public synchronized boolean tryLockingDocumentForFetching(String url) {
		if (!isLockableForFetching(url)) {
			return false;
		}

		ConnectorDocumentURLCacheEntry currentEntry = cache.get(url);
		String id = currentEntry == null ? null : currentEntry.id;

		LocalDateTime fetchingStartTime = TimeProvider.getLocalDateTime();
		ConnectorDocumentURLCacheEntry value = new ConnectorDocumentURLCacheEntry(id, CURRENTLY_FETCHED,
				fetchingStartTime, new HashMap<String, Object>());
		insertInCache(url, value, WAS_MODIFIED);
		return true;
	}

	private void initialize() {

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
				String connectorId = record.get(schema.get(ConnectorDocument.CONNECTOR));

				if (ConnectorDocumentURLCache.this.connectorId.equals(connectorId)) {
					String url = record.get(es.connectorHttpDocument.url());
					insertInCache(url, newEntryFromRecord(schema, record), WAS_MODIFIED);
				}
			}

		});

		ObservableLeaderElectionManager electionManager = appLayerFactory.getModelLayerFactory().getDataLayerFactory()
				.getLeaderElectionService();
		electionManager.register(this);
		currentLeaderStatus = electionManager.isCurrentNodeLeader();
	}

	private boolean isConnectorDocument(Record record) {
		return cachedSchemaTypes.contains(record.getTypeCode());
	}

	public void onConnectorResume() {
		invalidateAll();
	}

	public void onConnectorStart() {
		invalidateAll();
	}

	public void onConnectorStop() {
		invalidateAll();
	}

	public void onConnectorGetJobsCalled() {
		if (!loadedCache) {
			synchronized (this) {
				if (!loadedCache) {
					loadCache();
				}
			}
		}
	}

	private void loadCache() {
		for (String cachedSchemaType : cachedSchemaTypes) {

			LogicalSearchQuery query = new LogicalSearchQuery(from(es.connectorHttpDocument.schemaType())
					.where(es.connectorHttpDocument.connector()).isEqualTo(connectorId));

			List<Metadata> metadatas = new ArrayList<>();
			metadatas.add(es.connectorHttpDocument.url());
			metadatas.add(es.connectorHttpDocument.fetched());

			MetadataSchemaType schemaType = es.schemaType(cachedSchemaType);
			MetadataSchema schema = schemaType.getDefaultSchema();

			for (String extraCachedMetadata : cachedMetadatas) {
				if (schema.hasMetadataWithCode(extraCachedMetadata)) {
					metadatas.add(schema.getMetadata(extraCachedMetadata));
				}
			}

			query.setReturnedMetadatas(ReturnedMetadatasFilter.onlyMetadatas(metadatas));
			SearchResponseIterator<Record> iterator = es.getModelLayerFactory().newSearchServices().recordsIterator(query, 5000);
			int count = 0;
			long total = iterator.getNumFound();
			while (iterator.hasNext()) {
				Record record = iterator.next();
				String url = record.get(es.connectorHttpDocument.url());
				insertInCache(url, newEntryFromRecord(schema, record), WAS_OBTAINED);
				count++;
				if (count % 10000 == 0 || count == total) {
					LOGGER.info("Loading cache of connector '" + connectorId + "' of collection '" + collection + "' : "
								+ count + "/" + total);
				}
			}
		}
		loadedCache = true;
	}

	@NotNull
	private ConnectorDocumentURLCacheEntry newEntryFromRecord(MetadataSchema schema, Record record) {
		String id = record.getId();
		boolean fetched = Boolean.TRUE.equals(record.get(es.connectorHttpDocument.fetched()));
		ConnectorDocumentURLCacheStatus status = fetched ? ConnectorDocumentURLCacheStatus.FETCHED : NOT_FETCHED;
		Map<String, Object> extraMetadataValues = populateExtraCacheFields(record, schema);
		return new ConnectorDocumentURLCacheEntry(id, status, null, extraMetadataValues);
	}

	public void onAllDocumentsDeleted() {
		invalidateAll();
	}

	private Map<String, Object> populateExtraCacheFields(Record connectorDocumentRecord, MetadataSchema schema) {

		Map<String, Object> extraCachedMetadataValues = new HashMap<>();
		for (String extraCachedMetadata : cachedMetadatas) {
			if (schema.hasMetadataWithCode(extraCachedMetadata)) {
				Metadata metadata = schema.getMetadata(extraCachedMetadata);
				extraCachedMetadataValues.put(extraCachedMetadata, connectorDocumentRecord.get(metadata));
			}
		}

		return extraCachedMetadataValues;
	}

	@Override
	public void invalidateAll() {
		cache.clear();
		loadedCache = false;
	}

	@Override
	public ConstellioCache getCache() {
		return cache;
	}

	@Override
	public void onLeaderStatusChanged(boolean newStatus) {
		currentLeaderStatus = newStatus;
		LOGGER.info(newStatus ? "Server is now the leader" : "Server is no more the leader");
		invalidateAll();
	}


	protected void insertInCache(String url, ConnectorDocumentURLCacheEntry entry, InsertionReason insertionReason) {
		cache.put(url, entry, insertionReason);
	}


}
