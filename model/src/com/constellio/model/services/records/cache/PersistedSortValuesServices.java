package com.constellio.model.services.records.cache;

import com.constellio.data.dao.dto.records.RecordId;
import com.constellio.data.dao.services.contents.ContentDao;
import com.constellio.data.dao.services.contents.ContentDaoException.ContentDaoException_NoSuchContent;
import com.constellio.data.io.services.facades.FileService;
import com.constellio.data.utils.TimeProvider;
import com.constellio.model.entities.records.Record;
import com.constellio.model.entities.schemas.Metadata;
import com.constellio.model.entities.schemas.MetadataSchemaType;
import com.constellio.model.services.collections.CollectionsListManager;
import com.constellio.model.services.factories.ModelLayerFactory;
import com.constellio.model.services.schemas.MetadataSchemasManager;
import com.constellio.model.services.search.StatusFilter;
import com.constellio.model.services.search.query.ReturnedMetadatasFilter;
import com.constellio.model.services.search.query.logical.LogicalSearchQuery;
import com.constellio.model.services.search.query.logical.QueryExecutionMethod;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.apache.solr.client.solrj.io.Tuple;
import org.apache.solr.client.solrj.io.stream.TupleStream;
import org.jetbrains.annotations.NotNull;
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
import java.util.function.Consumer;
import java.util.function.Supplier;

import static com.constellio.model.services.search.VisibilityStatusFilter.ALL;
import static com.constellio.model.services.search.query.logical.LogicalSearchQueryOperators.from;

public class PersistedSortValuesServices {

	private static final Duration VALUE_EXPIRATION = Duration.standardDays(3);

	private static final String PATH = "shared/sortValues.txt";
	private static final Logger LOGGER = LoggerFactory.getLogger(PersistedSortValuesServices.class);
	private static final String TEMP_SORT_VALUES_FILE_RESOURCE_NAME = "PersistedSortValuesServices-TempSortValuesFile";
	private static final String READ_SORT_FILE_INPUTSTREAM_RESOURCE_NAME = "PersistedSortValuesServices-ReadSortFileInputStream";

	ModelLayerFactory modelLayerFactory;
	ContentDao contentDao;
	FileService fileService;
	CollectionsListManager collectionsListManager;
	MetadataSchemasManager metadataSchemasManager;

	public PersistedSortValuesServices(ModelLayerFactory modelLayerFactory) {
		this.modelLayerFactory = modelLayerFactory;
		this.contentDao = modelLayerFactory.getDataLayerFactory().getContentsDao();
		this.fileService = modelLayerFactory.getIOServicesFactory().newFileService();
		this.collectionsListManager = modelLayerFactory.getCollectionsListManager();
		this.metadataSchemasManager = modelLayerFactory.getMetadataSchemasManager();
	}

	public LocalDateTime getLastVersionTimeStamp() {
		SortValueList valueList = readFromVault();
		return valueList == null ? null : valueList.timestamp;
	}

	public SortValueList retreiveAndRewriteSortValuesFile() {
		List<SortValue> sortValues = this.recordsIdSortedByTheirDefaultSort();
		SortValueList sortValueListFromSolr = new SortValueList(sortValues, true, TimeProvider.getLocalDateTime());
		File tempSortValuesFiles = fileService.newTemporaryFile(TEMP_SORT_VALUES_FILE_RESOURCE_NAME);
		try {

			writeToFile(sortValueListFromSolr, tempSortValuesFiles);

			try (InputStream inputStream = new BufferedInputStream(new FileInputStream(tempSortValuesFiles))) {
				contentDao.add(PATH, inputStream);

			} catch (IOException e) {
				throw new RuntimeException(e);
			}

			return sortValueListFromSolr;

		} finally {
			fileService.deleteQuietly(tempSortValuesFiles);
		}
	}

	private void writeToFile(SortValueList sortValueListFromSolr, File tempSortValuesFiles) {
		try (BufferedWriter writer = new BufferedWriter(new FileWriter(tempSortValuesFiles))) {
			writer.write(ISODateTimeFormat.dateHourMinuteSecondFraction().print(sortValueListFromSolr.timestamp));
			writer.newLine();
			for (SortValue sortValue : sortValueListFromSolr.getSortValues()) {
				writer.write(sortValue.valueHash() + ";" + sortValue.recordId().stringValue());
				writer.newLine();
			}

		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

	public SortValueList readSortValues() {
		SortValueList list = readFromVault();

		if (list == null || list.getTimestamp().plus(VALUE_EXPIRATION).isBefore(TimeProvider.getLocalDateTime())) {
			list = retreiveAndRewriteSortValuesFile();
		}

		return list;
	}

	private SortValueList readFromVault() {

		try (BufferedReader reader = new BufferedReader(new InputStreamReader(contentDao
				.getContentInputStream(PATH, READ_SORT_FILE_INPUTSTREAM_RESOURCE_NAME)))) {

			List<SortValue> sortValues = new ArrayList<>();

			String line = reader.readLine();
			LocalDateTime timestamp = ISODateTimeFormat.dateHourMinuteSecondFraction().parseLocalDateTime(line);
			while ((line = reader.readLine()) != null) {
				if (line.length() > 0) {
					int semicolonIndex = line.indexOf(";");
					int sortValueHashcode = Integer.valueOf(line.substring(0, semicolonIndex));
					RecordId recordId = RecordId.id(line.substring(semicolonIndex + 1));
					if (recordId.isInteger()) {
						sortValues.add(new SortValueWithHashAndIntId(recordId.intValue(), sortValueHashcode));
					} else {
						sortValues.add(new SortValueWithHashAndStringId(recordId.stringValue(), sortValueHashcode));
					}

				}
			}

			return new SortValueList(sortValues, false, timestamp);

		} catch (IOException e) {
			throw new RuntimeException(e);

		} catch (ContentDaoException_NoSuchContent ignored) {
			return null;
		}

	}

	@AllArgsConstructor
	public static class SortValueList {

		@Getter
		List<SortValue> sortValues;

		@Getter
		boolean obtainedFromSolr;

		@Getter
		LocalDateTime timestamp;

	}

	@AllArgsConstructor
	private static class SortValueWithHashAndIntId implements SortValue {

		int id;

		int valueHash;

		@Override
		public RecordId recordId() {
			return RecordId.id(id);
		}

		@Override
		public int valueHash() {
			return valueHash;
		}

		@Override
		public boolean equals(Object o) {
			if (this == o) {
				return true;
			}
			if (o == null || getClass() != o.getClass()) {
				return false;
			}

			SortValueWithHashAndIntId that = (SortValueWithHashAndIntId) o;

			if (id != that.id) {
				return false;
			}
			return valueHash == that.valueHash;
		}

		@Override
		public int hashCode() {
			int result = id;
			result = 31 * result + valueHash;
			return result;
		}
	}

	@AllArgsConstructor
	private static class SortValueWithHashAndStringId implements SortValue {

		String id;

		int valueHash;

		@Override
		public RecordId recordId() {
			return RecordId.id(id);
		}

		@Override
		public int valueHash() {
			return valueHash;
		}

		@Override
		public boolean equals(Object o) {
			if (this == o) {
				return true;
			}
			if (o == null || getClass() != o.getClass()) {
				return false;
			}

			SortValueWithHashAndStringId that = (SortValueWithHashAndStringId) o;

			if (valueHash != that.valueHash) {
				return false;
			}
			return id != null ? id.equals(that.id) : that.id == null;
		}

		@Override
		public int hashCode() {
			int result = id != null ? id.hashCode() : 0;
			result = 31 * result + valueHash;
			return result;
		}
	}

	public interface SortValue extends Supplier<RecordId> {

		RecordId recordId();

		int valueHash();

		default RecordId get() {
			return recordId();
		}

	}


	public List<SortValue> recordsIdSortedByTheirDefaultSort() {

		//Trier par code s'il n'y a pas ddv dans le type de schéma
		//Sinon par titre

		List<SortValue> returnedIds = new ArrayList<>();

		for (String collection : collectionsListManager.getCollections()) {
			for (MetadataSchemaType schemaType : metadataSchemasManager.getSchemaTypes(collection).getSchemaTypesInDisplayOrder()) {

				if (schemaType.getMainSortMetadata() != null) {

					boolean useTupleStream = modelLayerFactory.getSystemConfigs().isRunningWithSolr6()
											 && modelLayerFactory.getDataLayerFactory().getDataLayerConfiguration()
													 .useSolrTupleStreamsIfSupported();

					String stepName = "Loading sort values of '" + schemaType.getCode() + "' of collection '" + schemaType.getCollection() + "'"
									  + (useTupleStream ? " (using tuple streams)" : " (using iterator)");

					final long total = modelLayerFactory.newSearchServices().getResultsCount(from(schemaType).returnAll());
					Consumer<Integer> progressionConsumer = (current) -> {

						if (current % 50000 == 0 || current == total) {
							LOGGER.info(stepName + " - " + current + "/" + total);
						}
					};


					if (useTupleStream) {
						returnedIds.addAll(recordsIdSortedByTitleUsingTupleStream(schemaType, schemaType.getMainSortMetadata(), progressionConsumer));


					} else {
						returnedIds.addAll(recordsIdSortedByTitleUsingIterator(schemaType, schemaType.getMainSortMetadata(), progressionConsumer));
					}
				}

			}
		}
		return returnedIds;
	}


	public List<SortValue> recordsIdSortedByTitleUsingTupleStream(MetadataSchemaType schemaType,
																  Metadata metadata,
																  Consumer<Integer> progressionConsumer) {

		String message = "Fetching sortValues of schema type '" + schemaType.getCode() + "' of collection '" + schemaType.getCollection() + "' using tuple stream method";
		LOGGER.info(message + " - starting");
		Map<String, String> props = new HashMap<>();
		props.put("q", "schema_s:" + schemaType.getCode() + "_*");
		props.put("fq", "collection_s:" + schemaType.getCollection());

		StringBuilder fields = new StringBuilder("id,");
		StringBuilder sort = new StringBuilder();

		if (metadata.isSortable()) {
			sort.append(metadata.getSortMetadata().getDataStoreCode());
			sort.append(" asc");
			sort.append(", ");

			fields.append(metadata.getSortMetadata().getDataStoreCode());
			fields.append(", ");
		}

		sort.append(metadata.getDataStoreCode());
		sort.append(" asc");

		fields.append(metadata.getDataStoreCode());

		props.put("sort", sort.toString());
		props.put("fl", fields.toString());
		props.put("rows", "1000000000");

		TupleStream tupleStream = modelLayerFactory.getDataLayerFactory().newRecordDao().tupleStream(props);

		try {
			tupleStream.open();
		} catch (IOException e) {
			throw new RuntimeException(e);
		}

		AtomicInteger count = new AtomicInteger();

		List<SortValue> sortValues = new ArrayList<>();
		try {

			Tuple tuple = null;
			while (!(tuple = tupleStream.read()).EOF) {
				RecordId recordId = RecordId.toId(tuple.getString("id"));


				Object sortMetadataValue = tuple.get(metadata.getDataStoreCode());
				sortValues.add(toSortValue(recordId, sortMetadataValue));

				progressionConsumer.accept(count.incrementAndGet());
			}
		} catch (IOException e) {
			throw new RuntimeException(e);
		} finally {
			try {
				tupleStream.close();
			} catch (IOException e1) {
				throw new RuntimeException(e1);
			}
		}

		return sortValues;
	}

	@NotNull
	private SortValue toSortValue(RecordId recordId, Object sortMetadataValue) {
		int hashCode = sortMetadataValue == null ? 0 : sortMetadataValue.hashCode();

		SortValue sortValue;
		if (recordId.isInteger()) {
			sortValue = new SortValueWithHashAndIntId(recordId.intValue(), hashCode);
		} else {
			sortValue = new SortValueWithHashAndStringId(recordId.stringValue(), hashCode);
		}
		return sortValue;
	}


	public List<SortValue> recordsIdSortedByTitleUsingIterator(MetadataSchemaType schemaType, Metadata metadata,
															   Consumer<Integer> progressionConsumer) {

		LogicalSearchQuery query = new LogicalSearchQuery(from(schemaType).returnAll());
		if (metadata.isSortable()) {
			query.sortAsc(metadata.getSortField());
		}
		query.sortAsc(metadata);

		query.setReturnedMetadatas(ReturnedMetadatasFilter.onlyMetadatas(metadata));
		query.filteredByVisibilityStatus(ALL);
		query.filteredByStatus(StatusFilter.ALL);
		query.setQueryExecutionMethod(QueryExecutionMethod.USE_SOLR);
		Iterator<Record> recordIterator = modelLayerFactory.newSearchServices().recordsIteratorKeepingOrder(query, 25000);

		AtomicInteger progress = new AtomicInteger();
		List<SortValue> sortValues = new ArrayList<>();
		while (recordIterator.hasNext()) {
			Record record = recordIterator.next();
			Object value = record.get(metadata);
			sortValues.add(toSortValue(record.getRecordId(), value));
			progressionConsumer.accept(progress.incrementAndGet());
		}

		return sortValues;
	}
}
