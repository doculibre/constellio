package com.constellio.model.services.event;

import com.constellio.data.dao.services.bigVault.SearchResponseIterator;
import com.constellio.model.entities.records.Record;
import com.constellio.model.entities.schemas.Schemas;
import com.constellio.model.services.factories.ModelLayerFactory;
import com.constellio.model.services.search.SearchServices;
import com.constellio.model.services.search.query.logical.LogicalSearchQuery;
import com.constellio.model.services.search.query.logical.condition.LogicalSearchCondition;
import org.joda.time.LocalDateTime;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import static com.constellio.model.services.search.query.logical.LogicalSearchQueryOperators.fromEveryTypesOfEveryCollection;

public class AutoSplitByDayEventsExecutor {
	File folder;
	ModelLayerFactory modelLayerFactory;
	EventXMLWriter eventXMLWriter;
	List<DayProcessedListener> dayProcessedListenerList;

	public AutoSplitByDayEventsExecutor(File folder, ModelLayerFactory modelLayerFactory) {
		this.folder = folder;
		this.modelLayerFactory = modelLayerFactory;
		this.dayProcessedListenerList = new ArrayList<>();
	}

	public static LogicalSearchQuery getAllEventsQuery() {
		LogicalSearchQuery logicalSearchQuery = new LogicalSearchQuery();
		LogicalSearchCondition logicalSearchCondition = fromEveryTypesOfEveryCollection().where(Schemas.SCHEMA)
				.isStartingWithText("event_");
		logicalSearchQuery.setCondition(logicalSearchCondition);

		return logicalSearchQuery;
	}

	public void wrtieAllEvents() {
		writeEvents(getAllEventsQuery());
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

			while (searchResponseIterator.hasNext()) {
				currentEvent = searchResponseIterator.next();
				localDateTime = currentEvent.get(Schemas.CREATED_ON);

				if (!isSameDay(currentEvent, oldLocalDateTime)) {
					closeEventXMLWriter();
					if(oldLocalDateTime != null) {
						fireDateProcessedListener(oldLocalDateTime.withTime(0, 0, 0, 0).plusDays(1).minusMillis(1));
					}

					eventXMLWriter = new FileEventXMLWriter(
							getFolderFromDateTime((LocalDateTime) currentEvent.get(Schemas.CREATED_ON)), modelLayerFactory);
				}

				eventXMLWriter.write(currentEvent);

				oldLocalDateTime = localDateTime;
			}
		} finally {
			if(currentEvent != null) {
				LocalDateTime localDateTime = currentEvent.get(Schemas.CREATED_ON);
				fireDateProcessedListener(localDateTime.withTime(0, 0, 0, 0).plusDays(1).minusMillis(1));
			}

			closeEventXMLWriter();
		}
	}

	private void closeEventXMLWriter() {
		if(eventXMLWriter != null) {
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

		stringBuilder.append("data" + File.separator);
		stringBuilder.append(localDateTime.getYear() + File.separator);
		stringBuilder.append(localDateTime.getMonthOfYear() + File.separator);
		stringBuilder.append(localDateTime.getYear() + "-" + localDateTime.getMonthOfYear() + "-" + localDateTime
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

	public void fireDateProcessedListener(LocalDateTime localDateTime) {
		DayProcessedEvent dayProcessedEvent = new DayProcessedEvent(localDateTime);

		for(DayProcessedListener dayProcessedListener : dayProcessedListenerList) {
			dayProcessedListener.lastDateProcessed(dayProcessedEvent);
		}
	}
}
