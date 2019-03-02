package com.constellio.model.services.event;

import com.constellio.data.io.services.facades.IOServices;
import com.constellio.data.io.services.zip.ZipService;
import com.constellio.data.io.services.zip.ZipServiceException;
import com.constellio.data.utils.TimeProvider;
import com.constellio.model.entities.schemas.Schemas;
import com.constellio.model.services.factories.ModelLayerFactory;
import com.constellio.model.services.migrations.ConstellioEIMConfigs;
import com.constellio.model.services.search.query.logical.LogicalSearchQuery;
import com.constellio.model.services.search.query.logical.condition.LogicalSearchCondition;
import org.jetbrains.annotations.NotNull;
import org.joda.time.LocalDateTime;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.Arrays;

import static com.constellio.model.services.search.query.logical.LogicalSearchQueryOperators.fromEveryTypesOfEveryCollection;

public class EventService implements Runnable {
	public static final String ROOT_EVENT_FOLDER = "rootEventFolder";
	public static final String EVENTS_ZIP = "eventsZip";
	public static final String EVENTS_ZIP_WITH_EXTENTION = "eventsZip.zip";
	ModelLayerFactory modelayerFactory;

	public static final String DATE_TIME_FORMAT = "dd/MM/yyyy HH:mm:ss";
	public static final String ENCODING = "UTF-8";
	public static final String FOLDER = "eventsBackup";
	public static final String IO_STREAM_NAME_BACKUP_EVENTS_IN_VAULT = "com.constellio.model.services.event.EventService#backupEventsInVault";
	public static final String IO_STREAM_NAME_CLOSE = "com.constellio.model.services.event.EventService#close";
	public static final LocalDateTime MIN_LOCAL_DATE_TIME = new LocalDateTime(-9999, 1, 1, 0, 0, 0);

	private IOServices ioServices;
	private ZipService zipService;

	public EventService(ModelLayerFactory modelayerFactory) {
		this.modelayerFactory = modelayerFactory;
		this.ioServices = modelayerFactory.getIOServicesFactory().newIOServices();
		this.zipService = modelayerFactory.getIOServicesFactory().newZipService();
	}

	public LocalDateTime getDeletetionDateCutOff() {
		Integer periodInMonth = modelayerFactory.getSystemConfigurationsManager()
				.getValue(ConstellioEIMConfigs.KEEP_EVENTS_FOR_X_MONTH);
		LocalDateTime nowLocalDateTime = TimeProvider.getLocalDateTime();
		nowLocalDateTime = nowLocalDateTime.withSecondOfMinute(0).withHourOfDay(0).withMillisOfSecond(0).withMinuteOfHour(0);
		LocalDateTime cutoffLocalDateTime;

		cutoffLocalDateTime = nowLocalDateTime.minusMonths(periodInMonth);

		return cutoffLocalDateTime;

	}

	public LocalDateTime getLastDayTimeArchived() {
		String dateTimeAsString = modelayerFactory.getSystemConfigurationsManager()
				.getValue(ConstellioEIMConfigs.LAST_BACKUP_DAY);
		LocalDateTime dateTime = null;

		if (dateTimeAsString != null) {
			DateTimeFormatter dateTimeFormatter = DateTimeFormat.forPattern(DATE_TIME_FORMAT);
			dateTime = dateTimeFormatter.parseLocalDateTime(dateTimeAsString);
		} else {
			dateTime = MIN_LOCAL_DATE_TIME;
		}

		return dateTime;
	}

	public void setLastArchivedDayTime(LocalDateTime lastDayTime) {
		modelayerFactory
				.getSystemConfigurationsManager().setValue(ConstellioEIMConfigs.LAST_BACKUP_DAY,
				lastDayTime.toString(DATE_TIME_FORMAT));
	}

	/**
	 *
	 * @return a zip file containing the backup created.
	 */
	public void backupAndRemove() {
		backupEventsInVault();
		removeOldEventFromSolr();
	}


	public LocalDateTime getArchivedUntilLocalDate() {
		return TimeProvider.getLocalDateTime().withTime(0, 0, 0, 0);
	}

	private File createNewFolder(String folderName) {
		return ioServices.newTemporaryFolder(folderName);
	}

	private File createNewFile(String fileName, String extention) {
		return ioServices.newTemporaryFile(fileName, extention);
	}

	public static final String EVENTS_XML_TAG = "Events";
	public static final String EVENT_XML_TAG = "Event";

	public void backupEventsInVault() {
		LocalDateTime lastDayTimeArchived = getLastDayTimeArchived();
		File rootFolder = createNewFolder(ROOT_EVENT_FOLDER);

		if (lastDayTimeArchived == null || lastDayTimeArchived.compareTo(getArchivedUntilLocalDate()) < 0) {
			AutoSplitByDayEventsExecutor autoSplitByDayEventsExecutor = new AutoSplitByDayEventsExecutor(rootFolder,
					modelayerFactory);
			autoSplitByDayEventsExecutor.addDateProcessedListener(new DayProcessedListener() {
				@Override
				public void lastDateProcessed(DayProcessedEvent dayProcessedEvent) {
					setLastArchivedDayTime(dayProcessedEvent.getLocalDateTime().withTime(0, 0, 0, 0).plusDays(1).minusMillis(1));

					String fileName = dateAsFileName(dayProcessedEvent.getLocalDateTime());
					File zipFile = createNewFile(fileName, ".zip");
					try {
						InputStream zipFileInputStream;

						zipService.zip(zipFile, Arrays.asList(dayProcessedEvent.getFile()));
						zipFileInputStream = ioServices.newFileInputStream(zipFile, IO_STREAM_NAME_CLOSE);
						modelayerFactory.getContentManager()
								.getContentDao().add(FOLDER + "/" + fileName + ".zip", zipFileInputStream);
						ioServices.deleteQuietly(zipFile);
					} catch (ZipServiceException e) {
						throw new RuntimeException("ZipService exception", e);
					} catch (FileNotFoundException e) {
						throw new RuntimeException("File not found exception", e);
					}
				}
			});

			autoSplitByDayEventsExecutor.writeEvents(getEventAfterLastArchivedDayAndBeforeLastDayToArchiveLogicalSearchQuery());

			ioServices.deleteQuietly(rootFolder);
		}
	}

	public static String dateAsFileName(LocalDateTime localDateTime) {

		if (localDateTime == null) {
			return "null";
		}

		return localDateTime.toString("yyyy-MM-dd");
	}

	@NotNull
	public LogicalSearchQuery getEventAfterLastArchivedDayAndBeforeLastDayToArchiveLogicalSearchQuery() {
		LogicalSearchQuery logicalSearchQuery = new LogicalSearchQuery();
		LogicalSearchCondition logicalSearchCondition = fromEveryTypesOfEveryCollection().where(Schemas.SCHEMA)
				.isStartingWithText("event_")
				.andWhere(Schemas.CREATED_ON).isGreaterThan(getLastDayTimeArchived()).andWhere(Schemas.CREATED_ON)
				.isLessThan(getArchivedUntilLocalDate());
		logicalSearchQuery.setCondition(logicalSearchCondition);
		logicalSearchQuery.sortAsc(Schemas.CREATED_ON);
		return logicalSearchQuery;
	}

	public void removeOldEventFromSolr() {
		deleteArchivedEvents();
	}

	public void deleteArchivedEvents() {
		//        RecordDao recordDao = modelayerFactory.getDataLayerFactory().newRecordDao();
		//
		//
		//        TransactionDTO transaction = new TransactionDTO(RecordsFlushing.NOW());
		//        ModifiableSolrParams modifiableSolrParams = new ModifiableSolrParams();
		//        String code  = "createdOn_dt";
		//
		//        String between = code + ":{* TO " + CriteriaUtils.toSolrStringValue(getDeletetionDateCutOff(), null) + "}";
		//
		//        modifiableSolrParams.set("q", between);
		//        modifiableSolrParams.add("fq", "schema_s:event_* ");
		//
		//        transaction = transaction.withDeletedByQueries(modifiableSolrParams);
		//        try {
		//            recordDao.execute(transaction);
		//        } catch (RecordDaoException.OptimisticLocking optimisticLocking) {
		//            throw new RuntimeException("Error while deleting archived eventRecord", optimisticLocking);
		//        }
	}

	@Override
	public void run() {
		backupAndRemove();
	}
}
