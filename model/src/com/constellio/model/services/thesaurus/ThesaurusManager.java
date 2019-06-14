package com.constellio.model.services.thesaurus;

import com.constellio.data.dao.managers.StatefulService;
import com.constellio.data.dao.services.cache.ConstellioCacheManager;
import com.constellio.data.dao.services.contents.ContentDaoException;
import com.constellio.data.events.Event;
import com.constellio.data.events.EventBus;
import com.constellio.data.events.EventBusEventsExecutionStrategy;
import com.constellio.data.events.EventBusListener;
import com.constellio.model.entities.records.Record;
import com.constellio.model.entities.records.wrappers.ThesaurusConfig;
import com.constellio.model.services.collections.CollectionsListManager;
import com.constellio.model.services.factories.ModelLayerFactory;
import com.constellio.model.services.records.RecordServices;
import com.constellio.model.services.records.RecordServicesException;
import com.constellio.model.services.records.SchemasRecordsServices;
import com.constellio.model.services.search.SearchServices;
import com.constellio.model.services.search.query.logical.LogicalSearchQuery;
import com.constellio.model.services.search.query.logical.LogicalSearchQueryOperators;
import com.constellio.model.services.thesaurus.exception.ThesaurusInvalidFileFormat;
import org.apache.tika.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.xml.bind.DatatypeConverter;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.security.DigestInputStream;
import java.security.MessageDigest;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ThesaurusManager implements StatefulService, EventBusListener {

	private static final String INVALIDATE_THESAURUS_CACHE = "invalidate";
	final String FILE_INPUT_STREAM_NAME = "ThesaurusManager.ThesaurusFile";
	private CollectionsListManager collectionsListManager;
	private SearchServices searchServices;
	private RecordServices recordServices;

	private Map<String, ThesaurusService> thesaurusServiceByChecksumCache;
	private Map<String, ThesaurusService> thesaurusServiceByCollectionCache;
	private EventBus eventBus;
	private ModelLayerFactory modelLayerFactory;
	private static final Logger LOGGER = LoggerFactory.getLogger(ThesaurusManager.class);

	public ThesaurusManager(ModelLayerFactory modelLayerFactory) {
		this.modelLayerFactory = modelLayerFactory;

		this.eventBus = modelLayerFactory.getDataLayerFactory().getEventBusManager()
				.createEventBus("thesaurusManager", EventBusEventsExecutionStrategy.EXECUTED_LOCALLY_THEN_SENT_REMOTELY);

		this.eventBus.register(this);

		ConstellioCacheManager recordsCacheManager = this.modelLayerFactory.getDataLayerFactory().getDistributedCacheManager();
		thesaurusServiceByCollectionCache = new HashMap<>();
		thesaurusServiceByChecksumCache = new HashMap<>();

		collectionsListManager = this.modelLayerFactory.getCollectionsListManager();
		searchServices = this.modelLayerFactory.newSearchServices();
		recordServices = this.modelLayerFactory.newRecordServices();
	}

	/**
	 * Gets Thesaurus in non persistent memory for a given collection.
	 *
	 * @param collection
	 * @return
	 */
	public  ThesaurusService get(String collection) {
		return thesaurusServiceByCollectionCache.get(collection);
	}

	/**
	 * Sets Thesaurus in non persistent memory for a given collection.
	 *
	 * @param
	 */
	public void set(InputStream inputStream, String collection)
			throws ThesaurusInvalidFileFormat {

		byte[] thesaurusBytes;
		try {
			thesaurusBytes = IOUtils.toByteArray(inputStream);
		} catch (IOException e) {
			throw new RuntimeException(e);
		}

		Map<String, Object> parameters = new HashMap<>();
		parameters.put("bytes", thesaurusBytes);
		parameters.put("collection", collection);

		eventBus.send(INVALIDATE_THESAURUS_CACHE, parameters);
	}

	/**
	 * Saves Thesaurus to persistent memory.
	 *
	 * @return true if success
	 */
	public boolean save() {
		// TODO continue
		return false;
	}

	/**
	 * Initialize a service per existing collection.
	 */
	@Override
	public synchronized void initialize() {

		List<String> collections = collectionsListManager.getCollectionsExcludingSystem();

		for (String collection : collections) {
			ThesaurusConfig thesaurusConfig = getThesaurusConfigs(collection);
			if (thesaurusConfig != null) {
				InputStream inputStream = getThesaurusFile(thesaurusConfig);
				if (inputStream != null) {
					ThesaurusService thesaurusService = getThesaurusService(inputStream);
					if (thesaurusService != null) {
						LOGGER.info("ThesaurusService initialized for collection : " + collection);
						thesaurusService.setDeniedTerms(thesaurusConfig.getDenidedWords());
						thesaurusServiceByCollectionCache.put(collection, thesaurusService);
					}
				}
			}
		}
	}

	private InputStream getThesaurusFile(ThesaurusConfig thesaurusConfig) {

		InputStream thesaurusFile = null;

		try {
			thesaurusFile = modelLayerFactory.getContentManager().getContentDao()
					.getContentInputStream(thesaurusConfig.getContent().getCurrentVersion().getHash(), FILE_INPUT_STREAM_NAME);
		} catch (NullPointerException | ContentDaoException.ContentDaoException_NoSuchContent e) {
			LOGGER.error("Failed to get thesaurus service", e);
			thesaurusFile = IOUtils.toInputStream("");
		}

		return thesaurusFile;
	}

	private ThesaurusConfig getThesaurusConfigs(String collection) {
		ThesaurusConfig thesaurusConfig = null;
		SchemasRecordsServices schemas = new SchemasRecordsServices(collection, modelLayerFactory);

		List<Record> thesaurusConfigRecordFound = searchServices.cachedSearch(
				new LogicalSearchQuery(LogicalSearchQueryOperators.from(schemas.thesaurusConfig.schemaType()).returnAll()));

		if (thesaurusConfigRecordFound != null && thesaurusConfigRecordFound.size() >= 1) {
			thesaurusConfig = schemas.wrapThesaurusConfig(thesaurusConfigRecordFound.get(0));
		}

		return thesaurusConfig;
	}

	private ThesaurusService getThesaurusService(InputStream thesaurusFile) {
		ThesaurusService thesaurusService = null;
		try {
			DigestInputStream dis = new DigestInputStream(thesaurusFile, MessageDigest.getInstance("SHA-1"));
			ByteArrayOutputStream bos = new ByteArrayOutputStream();
			IOUtils.copy(dis, bos);

			String hash = DatatypeConverter.printBase64Binary(dis.getMessageDigest().digest());
			dis.close();

			if (thesaurusServiceByChecksumCache.containsKey(hash)) {
				return thesaurusServiceByChecksumCache.get(hash);
			}

			thesaurusService =
					ThesaurusServiceBuilder.getThesaurus(new ByteArrayInputStream(bos.toByteArray()));

			thesaurusServiceByChecksumCache.put(hash, thesaurusService);

			return thesaurusService;
		} catch (Exception e) {
			LOGGER.error("Failed to get thesaurus service", e);
		}
		return thesaurusService;
	}

	@Override
	public void close() {
		// nothing to close (no threads to kill)
	}

	@Override
	public void onEventReceived(Event event) {
		switch (event.getType()) {
			case INVALIDATE_THESAURUS_CACHE:

				String collection = event.getData("collection");
				byte[] thesaurusBytes = event.getData("bytes");

				ThesaurusService thesaurusService = getThesaurusService(new ByteArrayInputStream(thesaurusBytes));
				thesaurusServiceByCollectionCache.put(collection, thesaurusService);
				break;
		}
	}
}
