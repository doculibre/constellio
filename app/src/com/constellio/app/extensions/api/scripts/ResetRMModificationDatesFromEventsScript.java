package com.constellio.app.extensions.api.scripts;

import com.constellio.app.modules.rm.services.RMSchemasRecordsServices;
import com.constellio.app.services.factories.AppLayerFactory;
import com.constellio.data.dao.services.bigVault.SearchResponseIterator;
import com.constellio.data.conf.FoldersLocator;
import com.constellio.model.entities.records.ActionExecutorInBatch;
import com.constellio.model.entities.records.Record;
import com.constellio.model.entities.records.RecordUpdateOptions;
import com.constellio.model.entities.records.Transaction;
import com.constellio.model.entities.records.wrappers.Event;
import com.constellio.model.entities.records.wrappers.EventType;
import com.constellio.model.entities.schemas.Schemas;
import com.constellio.model.services.factories.ModelLayerFactory;
import com.constellio.model.services.records.RecordServices;
import com.constellio.model.services.schemas.SchemaUtils;
import com.constellio.model.services.search.SearchServices;
import com.constellio.model.services.search.query.logical.LogicalSearchQuery;
import com.constellio.model.services.search.query.logical.LogicalSearchQueryOperators;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.joda.time.Duration;
import org.joda.time.LocalDateTime;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

import static com.constellio.app.utils.ScriptsUtils.startLayerFactoriesWithoutBackgroundThreads;

public class ResetRMModificationDatesFromEventsScript {

	//    private static ScriptParameter TIME_UNIT = new ScriptParameter(ScriptParameterType.COMBOBOX, "Time unit", true);
	//    private static ScriptParameter TIME_VALUE = new ScriptParameter(ScriptParameterType.STRING, "Time value", true);
	static AppLayerFactory appLayerFactory;
	static ModelLayerFactory modelLayerFactory;
	static SearchServices searchServices;
	static RecordServices recordServices;
	static File tempFile;
	static BufferedWriter writer;


	private static void startBackend() {
		appLayerFactory = startLayerFactoriesWithoutBackgroundThreads();
	}

	public static void execute(AppLayerFactory appLayerFactory) throws Exception {
		try {
			modelLayerFactory = appLayerFactory.getModelLayerFactory();
			searchServices = modelLayerFactory.newSearchServices();
			recordServices = modelLayerFactory.newRecordServices();
			String filename = "script-" + new SimpleDateFormat("yyyy-MM-dd hh.mm").format(new Date()) + ".txt";
			tempFile = new File(new FoldersLocator().getWorkFolder(), filename);

			if (!tempFile.exists()) {
				tempFile.createNewFile();
			} else {
				if (tempFile.delete()) {
					tempFile.createNewFile();
				}
			}
			//            IOUtils.closeQuietly();
			writer = new BufferedWriter(new FileWriter(tempFile, true));

			final Duration isEqualRange = Duration.standardMinutes(5);

			final RoundedLocalDateTimeComparator timeComparator = new RoundedLocalDateTimeComparator();
			for (String collection : appLayerFactory
					.getCollectionsManager().getCollectionCodesExcludingSystem()) {
				final RMSchemasRecordsServices rm = new RMSchemasRecordsServices(collection, appLayerFactory);
				final HashMap<String, LocalDateTime> recordsLastModificationDate = new HashMap<>();

				SearchResponseIterator<Record> eventIterator = searchServices.recordsIterator(
						LogicalSearchQueryOperators.from(rm.eventSchemaType())
								.where(rm.eventType()).is(EventType.MODIFY_DOCUMENT), 100);
				while (eventIterator.hasNext()) {
					Event event = rm.wrapEvent(eventIterator.next());
					String recordId = event.getRecordId();
					LocalDateTime createdOn = event.getCreatedOn();
					LocalDateTime previousDate = recordsLastModificationDate.get(recordId);
					if (previousDate == null || previousDate.isBefore(createdOn)) {
						recordsLastModificationDate.put(recordId, createdOn);
					}
				}

				ActionExecutorInBatch actionExecutorInBatch = new ActionExecutorInBatch(searchServices, "", 100) {

					@Override
					public void doActionOnBatch(List<Record> records)
							throws Exception {
						Transaction transaction = new Transaction();
						RecordUpdateOptions recordUpdateOptions = new RecordUpdateOptions()
								.setSkipMaskedMetadataValidations(true).setUnicityValidationsEnabled(false)
								.setSkippingReferenceToLogicallyDeletedValidation(true).setSkipUSRMetadatasRequirementValidations(true)
								.setCatchExtensionsExceptions(true).setCatchExtensionsValidationsErrors(true).setCatchBrokenReferenceErrors(true)
								.setOverwriteModificationDateAndUser(false);
						transaction.setOptions(recordUpdateOptions);

						for (Record record : records) {
							LocalDateTime lastModificationDate = recordsLastModificationDate.get(record.getId());
							if (lastModificationDate != null) {
								LocalDateTime previousModificationDate = record.get(Schemas.MODIFIED_ON);
								if (timeComparator.compare(previousModificationDate, lastModificationDate, isEqualRange) != 0) {
									printChangesToOutputLogger(record, previousModificationDate, lastModificationDate);
									record.set(Schemas.MODIFIED_ON, lastModificationDate);
								}
							}
							//                            if(record.get(Schemas.MODIFIED_ON) == null) {
							//                                printChangesToOutputLogger(record, null, LocalDateTime.now());
							//                                record.set(Schemas.MODIFIED_ON, LocalDateTime.now());
							//                            } else {
							//                                printChangesToOutputLogger(record, (LocalDateTime) record.get(Schemas.MODIFIED_ON), ((LocalDateTime) record.get(Schemas.MODIFIED_ON)).minusSeconds(1));
							//                                record.set(Schemas.MODIFIED_ON, ((LocalDateTime) record.get(Schemas.MODIFIED_ON)).minusSeconds(1));
							//                            }

							transaction.update(record);
						}

						if (transaction.getRecords() != null && !transaction.getRecords().isEmpty()) {
							recordServices.execute(transaction);
						}
					}
				};

				actionExecutorInBatch
						.execute(new LogicalSearchQuery(LogicalSearchQueryOperators.from(rm.documentSchemaType()).returnAll()));
			}
		} finally {
			IOUtils.closeQuietly(writer);
		}
	}

	private static void printChangesToOutputLogger(Record record, LocalDateTime previousModificationDate,
												   LocalDateTime lastModificationDate) {
		try {
			writer.append("Changed modification date for " + SchemaUtils.getSchemaTypeCode(record.getSchemaCode())
						  + " " + record.getId() + " - " + record.getTitle()
						  + " from " + StringUtils.defaultIfBlank(previousModificationDate.toString(), "null")
						  + " to " + lastModificationDate.toString());
			writer.newLine();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	private static class RoundedLocalDateTimeComparator implements Comparator<LocalDateTime> {

		public int compare(LocalDateTime o1, LocalDateTime o2, Duration isEqualRange) {
			if (o1 == null && o2 == null) {
				return 0;
			} else if (o1 == null) {
				return -1;
			} else if (o2 == null) {
				return 1;
			} else {
				if (isEqualRange == null) {
					return o1.compareTo(o2);
				} else {
					LocalDateTime lowerLimit = o1.minus(isEqualRange);
					LocalDateTime upperLimit = o1.plus(isEqualRange);

					if (lowerLimit.isBefore(o2) && upperLimit.isAfter(o2)) {
						return 0;
					} else {
						return o1.compareTo(o2);
					}
				}
			}
		}

		@Override
		public int compare(LocalDateTime o1, LocalDateTime o2) {
			return compare(o1, o2, null);
		}
	}

	public static void main(String argv[])
			throws Exception {
		startBackend();
		execute(appLayerFactory);
	}
}
