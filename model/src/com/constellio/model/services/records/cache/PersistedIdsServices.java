package com.constellio.model.services.records.cache;

import com.constellio.data.dao.dto.records.RecordId;
import com.constellio.data.dao.services.contents.ContentDao;
import com.constellio.data.dao.services.contents.ContentDaoException.ContentDaoException_NoSuchContent;
import com.constellio.data.io.services.facades.FileService;
import com.constellio.data.utils.LazyIterator;
import com.constellio.data.utils.TimeProvider;
import com.constellio.model.entities.schemas.Schemas;
import com.constellio.model.services.collections.CollectionsListManager;
import com.constellio.model.services.factories.ModelLayerFactory;
import com.constellio.model.services.migrations.ConstellioEIMConfigs;
import com.constellio.model.services.schemas.MetadataSchemasManager;
import com.constellio.model.services.search.SearchServices;
import com.constellio.model.services.search.StatusFilter;
import com.constellio.model.services.search.query.ReturnedMetadatasFilter;
import com.constellio.model.services.search.query.logical.LogicalSearchQuery;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.apache.solr.client.solrj.io.Tuple;
import org.apache.solr.client.solrj.io.stream.TupleStream;
import org.joda.time.Duration;
import org.joda.time.LocalDateTime;
import org.joda.time.format.ISODateTimeFormat;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

import static com.constellio.model.services.search.VisibilityStatusFilter.ALL;
import static com.constellio.model.services.search.query.logical.LogicalSearchQueryOperators.fromEveryTypesOfEveryCollection;
import static com.constellio.model.services.search.query.logical.LogicalSearchQueryOperators.startingWithText;
import static org.apache.ignite.internal.util.lang.GridFunc.asList;

public class PersistedIdsServices {

	private static final Duration VALUE_EXPIRATION = Duration.standardDays(2);

	private static final String FILE_PATH = "shared/recordIds.txt";
	private static final Logger LOGGER = LoggerFactory.getLogger(PersistedIdsServices.class);
	private static final String TEMP_IDS_FILE_RESOURCE_NAME = "PersistedIdsServices-TempIdsFile";
	private static final String READ_IDS_FILE_INPUTSTREAM_RESOURCE_NAME = "PersistedIdsServices-ReadIdFileInputStream";

	private static boolean running;

	ModelLayerFactory modelLayerFactory;
	ContentDao contentDao;
	FileService fileService;
	CollectionsListManager collectionsListManager;
	MetadataSchemasManager metadataSchemasManager;
	SearchServices searchServices;
	ConstellioEIMConfigs systemConfigs;

	public PersistedIdsServices(ModelLayerFactory modelLayerFactory) {
		this.modelLayerFactory = modelLayerFactory;
		this.contentDao = modelLayerFactory.getDataLayerFactory().getContentsDao();
		this.fileService = modelLayerFactory.getIOServicesFactory().newFileService();
		this.collectionsListManager = modelLayerFactory.getCollectionsListManager();
		this.metadataSchemasManager = modelLayerFactory.getMetadataSchemasManager();
		this.searchServices = modelLayerFactory.newSearchServices();
		this.systemConfigs = modelLayerFactory.getSystemConfigs();
	}

	public LocalDateTime getLastVersionTimeStamp() {
		RecordIdsIterator idsFromVault = readFromVault();
		return idsFromVault == null ? null : idsFromVault.timestamp;
	}

	public LocalDateTime retreiveAndRewriteRecordIdsFile() {
		Iterator<RecordId> idsIterator = this.recordsIdIteratorExceptEvents();

		running = true;

		LocalDateTime timestamp = new LocalDateTime();
		File tempSortValuesFiles = fileService.newTemporaryFile(TEMP_IDS_FILE_RESOURCE_NAME);
		try {
			writeToFile(idsIterator, tempSortValuesFiles, timestamp);


			try (InputStream inputStream = new BufferedInputStream(new FileInputStream(tempSortValuesFiles))) {
				contentDao.add(FILE_PATH, inputStream);

			} catch (IOException e) {
				throw new RuntimeException(e);
			}

			return timestamp;
		} finally {
			running = false;
			fileService.deleteQuietly(tempSortValuesFiles);
		}
	}

	public static boolean isRunning() {
		return running;
	}

	private void writeToFile(Iterator<RecordId> idsIterator, File idsFile, LocalDateTime timestamp) {
		try (BufferedWriter writer = new BufferedWriter(new FileWriter(idsFile))) {
			writer.write(ISODateTimeFormat.dateHourMinuteSecondFraction().print(timestamp));
			writer.newLine();
			while (idsIterator.hasNext()) {
				writer.write(idsIterator.next().stringValue());
				writer.newLine();
			}

		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

	public RecordIdsIterator getRecordIds() {
		RecordIdsIterator idsFromVault = readFromVault();

		if (idsFromVault == null || idsFromVault.timestamp.plus(VALUE_EXPIRATION).isBefore(TimeProvider.getLocalDateTime())) {
			retreiveAndRewriteRecordIdsFile();
			RecordIdsIterator iteratorFromVault = readFromVault();
			//Technically they aren't obtained from solr, but theses ids have just been wrote
			return new RecordIdsIterator(iteratorFromVault.iterator, true, iteratorFromVault.timestamp);
		} else {
			return idsFromVault;
		}
	}

	public void clear() {
		contentDao.delete(asList(FILE_PATH));
	}

	@AllArgsConstructor
	public static class RecordIdsIterator {

		@Getter
		Iterator<RecordId> iterator;

		@Getter
		boolean obtainedFromSolr;

		@Getter
		LocalDateTime timestamp;

	}

	private RecordIdsIterator readFromVault() {

		try (BufferedReader reader = new BufferedReader(new InputStreamReader(contentDao
				.getContentInputStream(FILE_PATH, READ_IDS_FILE_INPUTSTREAM_RESOURCE_NAME)))) {

			List<RecordId> ids = new ArrayList<>();

			String line = reader.readLine();
			LocalDateTime timestamp = ISODateTimeFormat.dateHourMinuteSecondFraction().parseLocalDateTime(line);

			while ((line = reader.readLine()) != null) {
				if (line.length() > 0) {
					ids.add(RecordId.id(line));
				}
			}

			return new RecordIdsIterator(ids.iterator(), false, timestamp);

		} catch (IOException e) {
			throw new RuntimeException(e);

		} catch (ContentDaoException_NoSuchContent ignored) {
			return null;
		}

	}

	public Iterator<RecordId> recordsIdIteratorExceptEvents() {
		if ((systemConfigs.isRunningWithSolr6() || modelLayerFactory.getDataLayerFactory().isDistributed()) && modelLayerFactory.getDataLayerFactory().getDataLayerConfiguration()
				.useSolrTupleStreamsIfSupported()) {
			return recordsIdIteratorExceptEventsUsingTupleStream();

		} else {
			return recordsIdIteratorExceptEventsUsingQueryIterator();
		}
	}

	public Iterator<RecordId> recordsIdIteratorExceptEventsUsingQueryIterator() {
		LogicalSearchQuery query = new LogicalSearchQuery(fromEveryTypesOfEveryCollection()
				.where(Schemas.SCHEMA).isNot(startingWithText("event_")));
		query.sortAsc(Schemas.IDENTIFIER);
		query.setReturnedMetadatas(ReturnedMetadatasFilter.idVersionSchema());
		query.filteredByVisibilityStatus(ALL);
		query.filteredByStatus(StatusFilter.ALL);
		Iterator<String> idIterator = searchServices.recordsIdsIterator(query);

		long rows = searchServices.getResultsCount(query);
		AtomicInteger progress = new AtomicInteger();
		return new LazyIterator<RecordId>() {
			@Override
			protected RecordId getNextOrNull() {

				if (progress.incrementAndGet() % 100000 == 0) {
					LOGGER.info("loading ids " + progress.get() + "/" + rows);
				}
				return idIterator.hasNext() ? RecordId.toId(idIterator.next()) : null;
			}
		};

	}

	public Iterator<RecordId> recordsIdIteratorExceptEventsUsingTupleStream() {

		LOGGER.info("Fetching ids using tuple stream method...");
		Map<String, String> props = new HashMap<>();
		props.put("q", "-schema_s:" + "event_*");
		props.put("sort", "id asc");
		props.put("fl", "id");
		props.put("rows", "1000000000");

		TupleStream tupleStream = modelLayerFactory.getDataLayerFactory().newRecordDao().tupleStream(props);

		try {
			tupleStream.open();
		} catch (IOException e) {
			throw new RuntimeException(e);
		}

		AtomicInteger count = new AtomicInteger();

		return new LazyIterator<RecordId>() {

			@Override
			protected RecordId getNextOrNull() {

				try {

					Tuple tuple = tupleStream.read();
					if (tuple.EOF) {
						LOGGER.info("Fetching ids using tuple stream method : " + count.get() + " (finished)");
						tupleStream.close();
						return null;
					} else {
						if (count.get() % 50000 == 0) {
							LOGGER.info("Fetching ids using tuple stream method : " + count.get());
						}
						count.incrementAndGet();
						return RecordId.toId(tuple.getString("id"));
					}
				} catch (IOException e) {
					try {
						tupleStream.close();
					} catch (IOException e1) {
						throw new RuntimeException(e1);
					}
					throw new RuntimeException(e);
				}
			}
		};


	}

}
