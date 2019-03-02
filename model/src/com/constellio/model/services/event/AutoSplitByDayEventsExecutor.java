package com.constellio.model.services.event;

import com.constellio.data.dao.services.bigVault.SearchResponseIterator;
import com.constellio.data.utils.KeySetMap;
import com.constellio.model.entities.records.Record;
import com.constellio.model.entities.records.wrappers.Event;
import com.constellio.model.entities.schemas.Schemas;
import com.constellio.model.services.factories.ModelLayerFactory;
import com.constellio.model.services.search.SearchServices;
import com.constellio.model.services.search.query.logical.LogicalSearchQuery;
import com.constellio.model.services.search.query.logical.condition.LogicalSearchCondition;
import org.apache.log4j.Logger;
import org.apache.log4j.Priority;
import org.joda.time.LocalDateTime;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static com.constellio.model.services.search.query.logical.LogicalSearchQueryOperators.from;

public class AutoSplitByDayEventsExecutor {
	File folder;
	ModelLayerFactory modelLayerFactory;
	EventXMLWriter eventXMLWriter;
	List<DayProcessedListener> dayProcessedListenerList;

	KeySetMap<String, String> savedRecords;
	KeySetMap<String, String> allWritenEventBySchema;
	int numberOfEventWrittenInFile;
	Logger logger = Logger.getLogger(AutoSplitByDayEventsExecutor.class);

	public AutoSplitByDayEventsExecutor(File folder, ModelLayerFactory modelLayerFactory) {
		this.folder = folder;
		this.modelLayerFactory = modelLayerFactory;
		this.dayProcessedListenerList = new ArrayList<>();
		this.allWritenEventBySchema = new KeySetMap<>();
		numberOfEventWrittenInFile = 0;
	}

	public static LogicalSearchQuery getAllEventsQuery(String collection) {
		LogicalSearchQuery logicalSearchQuery = new LogicalSearchQuery();
		LogicalSearchCondition logicalSearchCondition = from(Arrays.asList(Event.SCHEMA_TYPE), collection)
				.returnAll();
		logicalSearchQuery.setCondition(logicalSearchCondition);


		return logicalSearchQuery;
	}

	public void wrtieAllEvents(String collection) {
		writeEvents(getAllEventsQuery(collection));
	}

	public KeySetMap<String, String> getSavedRecords() {
		return allWritenEventBySchema;
	}

	/**
	 * Query must return records that are events or an exception will be thrown.
	 *
	 * @param logicalSearchQuery query
	 */
	public void writeEvents(LogicalSearchQuery logicalSearchQuery) {
		Record currentEvent = null;

		try {
			logicalSearchQuery.sortAsc(Schemas.CREATED_ON);

			SearchServices searchServices = modelLayerFactory.newSearchServices();

			SearchResponseIterator<Record> searchResponseIterator = searchServices
					.recordsIteratorKeepingOrder(logicalSearchQuery, 25000);

			LocalDateTime oldLocalDateTime = null;
			LocalDateTime localDateTime;

			logger.log(Priority.INFO, "searchResponseIterator.number : " +  searchResponseIterator.getNumFound());

			while (searchResponseIterator.hasNext()) {
				currentEvent = searchResponseIterator.next();
				localDateTime = currentEvent.get(Schemas.CREATED_ON);

				if (!isSameDay(currentEvent, oldLocalDateTime)) {
					closeEventXMLWriter();
					logger.log(Priority.INFO, "close event xml writer 1");
					if(oldLocalDateTime != null && eventXMLWriter != null) {
						fireDateProcessedListener(oldLocalDateTime, eventXMLWriter.getXMLFile());
					}

					eventXMLWriter = new FileEventXMLWriter(
							getFolderFromDateTime((LocalDateTime) currentEvent.get(Schemas.CREATED_ON)), modelLayerFactory);
				}

				eventXMLWriter.write(currentEvent);
				numberOfEventWrittenInFile++;

				oldLocalDateTime = localDateTime;
			}
		}catch(Throwable t) {
			throw new RuntimeException("error while writing events", t);
		} finally {
			logger.log(Priority.INFO, "close event xml writer 2");
			closeEventXMLWriter();
			if(currentEvent != null && eventXMLWriter != null && !eventXMLWriter.isClose()) {
				LocalDateTime localDateTime = currentEvent.get(Schemas.CREATED_ON);
				fireDateProcessedListener(localDateTime, eventXMLWriter.getXMLFile());
			}
		}
	}

	private void closeEventXMLWriter() {
		if(eventXMLWriter != null && !eventXMLWriter.isClose()) {
			allWritenEventBySchema.addAll(eventXMLWriter.getAllEventWrittenEventsBySchema());
			eventXMLWriter.close();
		}
	}

	public File getFolderFromDateTime(LocalDateTime localDateTime) {
		String path = getPathFromDateTime(localDateTime);

		File newXmlFile = new File(folder, path);

		return newXmlFile;
	}


	public static String getPathFromDateTime(LocalDateTime localDateTime) {
		StringBuilder stringBuilder = new StringBuilder();

		stringBuilder.append("data" + "/");
		stringBuilder.append("_" + localDateTime.getYear() + "/");
		stringBuilder.append("_" + localDateTime.getYear()+ "-" + localDateTime.getMonthOfYear() + "/");
		stringBuilder.append("_" + localDateTime.getYear() + "-" + localDateTime.getMonthOfYear() + "-" + localDateTime
				.getDayOfMonth() + ".xml");

		return stringBuilder.toString();
	}

	public boolean isSameDay(Record record, LocalDateTime localDateTime) {
		if(localDateTime == null) {
			return false;
		}

		LocalDateTime recordDateTime = record.get(Schemas.CREATED_ON);
		return recordDateTime.getMonthOfYear() == localDateTime.getMonthOfYear() && localDateTime.getYear() == recordDateTime.getYear()
				&& recordDateTime.getDayOfMonth() == localDateTime.getDayOfMonth();
	}

	public void addDateProcessedListener(DayProcessedListener dayProcessedListener) {
		dayProcessedListenerList.add(dayProcessedListener);
	}

	public void fireDateProcessedListener(LocalDateTime localDateTime, File file) {
		DayProcessedEvent dayProcessedEvent = new DayProcessedEvent(localDateTime, file, getPathFromDateTime(localDateTime), numberOfEventWrittenInFile);

		for(DayProcessedListener dayProcessedListener : dayProcessedListenerList) {
			dayProcessedListener.lastDateProcessed(dayProcessedEvent);
		}

		numberOfEventWrittenInFile = 0;
	}
}
