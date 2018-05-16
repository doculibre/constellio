package com.constellio.app.extensions.api.scripts;

import com.constellio.app.modules.rm.services.RMSchemasRecordsServices;
import com.constellio.app.modules.rm.wrappers.Document;
import com.constellio.app.services.factories.AppLayerFactory;
import com.constellio.data.dao.services.bigVault.SearchResponseIterator;
import com.constellio.data.dao.services.contents.ContentDao;
import com.constellio.model.entities.records.ConditionnedActionExecutorInBatchBuilder;
import com.constellio.model.entities.records.Content;
import com.constellio.model.entities.records.Record;
import com.constellio.model.entities.records.wrappers.Event;
import com.constellio.model.entities.records.wrappers.EventType;
import com.constellio.model.entities.schemas.Schemas;
import com.constellio.model.services.contents.ContentManager;
import com.constellio.model.services.schemas.SchemaUtils;
import com.constellio.model.services.search.query.logical.LogicalSearchQuery;
import com.constellio.model.services.search.query.logical.LogicalSearchQueryOperators;
import org.apache.commons.lang3.StringUtils;
import org.joda.time.Duration;
import org.joda.time.LocalDate;
import org.joda.time.LocalDateTime;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;

import static com.constellio.model.services.search.query.logical.LogicalSearchQueryOperators.from;
import static java.util.Arrays.asList;

public class ResetRMModificationDatesFromEventsScript extends ScriptWithLogOutput {

    private static ScriptParameter TIME_UNIT = new ScriptParameter(ScriptParameterType.COMBOBOX, "Time unit", true);
    private static ScriptParameter TIME_VALUE = new ScriptParameter(ScriptParameterType.STRING, "Time value", true);

    public ResetRMModificationDatesFromEventsScript(AppLayerFactory appLayerFactory) {
        super(appLayerFactory, "Record", "ResetRMModificationDatesFromEventsScript");
    }

    @Override
    protected void execute() throws Exception {
        String timeUnit = parameterValues.get(TIME_UNIT);
        String timeValue = parameterValues.get(TIME_VALUE);
        if(timeUnit == null || timeValue == null) {
            return;
        }

        final Duration isEqualRange = buildDurationRange(timeUnit, timeValue);

        final RoundedLocalDateTimeComparator timeComparator = new RoundedLocalDateTimeComparator();
        for(String collection : appLayerFactory
                .getCollectionsManager().getCollectionCodesExcludingSystem()) {
            final RMSchemasRecordsServices rm = new RMSchemasRecordsServices(collection, appLayerFactory);
            final HashMap<String, LocalDateTime> recordsLastModificationDate = new HashMap<>();

            SearchResponseIterator<Record> eventIterator = searchServices.recordsIterator(
                    LogicalSearchQueryOperators.from(rm.eventSchemaType())
                    .where(rm.eventType()).isIn(asList(EventType.MODIFY_DOCUMENT, EventType.MODIFY_FOLDER)), 100);
            while (eventIterator.hasNext()) {
                Event event = rm.wrapEvent(eventIterator.next());
                String recordId = event.getRecordId();
                LocalDateTime createdOn = event.getCreatedOn();
                LocalDateTime previousDate = recordsLastModificationDate.get(recordId);
                if(previousDate == null || previousDate.isBefore(createdOn)) {
                    recordsLastModificationDate.put(recordId, createdOn);
                }
            }

            onCondition(LogicalSearchQueryOperators.from(rm.documentSchemaType()).returnAll())
            .modifyingRecordsWithImpactHandling(new ConditionnedActionExecutorInBatchBuilder.RecordScript() {
                    @Override
                    public void modifyRecord(Record record) {
                        LocalDateTime lastModificationDate = recordsLastModificationDate.get(record.getId());
                        if(lastModificationDate != null) {
                            LocalDateTime previousModificationDate = record.get(Schemas.MODIFIED_ON);
                            if(timeComparator.compare(previousModificationDate, lastModificationDate, isEqualRange) != 0) {
                                printChangesToOutputLogger(record, lastModificationDate);
                                record.set(Schemas.MODIFIED_ON, lastModificationDate);
                            }
                        }
                    }
                }
            );

            onCondition(LogicalSearchQueryOperators.from(rm.folderSchemaType()).returnAll())
            .modifyingRecordsWithImpactHandling(new ConditionnedActionExecutorInBatchBuilder.RecordScript() {
                    @Override
                    public void modifyRecord(Record record) {
                        LocalDateTime lastModificationDate = recordsLastModificationDate.get(record.getId());
                        if(lastModificationDate != null) {
                            LocalDateTime previousModificationDate = record.get(Schemas.MODIFIED_ON);
                            if(timeComparator.compare(previousModificationDate, lastModificationDate, isEqualRange) != 0) {
                                printChangesToOutputLogger(record, lastModificationDate);
                                record.set(Schemas.MODIFIED_ON, lastModificationDate);
                            }
                        }
                    }
                }
            );
        }
    }

    private Duration buildDurationRange(String timeUnit, String timeValue) {
        long value = Long.parseLong(timeValue);
        switch (timeUnit) {
            case "Day":
                return Duration.standardDays(value);
            case "Hour":
                return Duration.standardHours(value);
            case "Minute":
                return Duration.standardMinutes(value);
            default:
                return Duration.standardSeconds(value);
        }
    }

    private void printChangesToOutputLogger(Record record, LocalDateTime lastModificationDate) {
        outputLogger.appendToFile("Changed modification date for " + SchemaUtils.getSchemaTypeCode(record.getSchemaCode())
                + " " + record.getId() + " - " + record.getTitle()
                + " from " + StringUtils.defaultIfBlank(record.get(Schemas.MODIFIED_ON).toString(), "null")
                + " to " + lastModificationDate.toString() + "\n");
    }

    private class RoundedLocalDateTimeComparator implements Comparator<LocalDateTime> {

        public int compare(LocalDateTime o1, LocalDateTime o2, Duration isEqualRange) {
            if(o1 == null && o2 == null) {
                return 0;
            } else if(o1 == null) {
                return -1;
            } else if(o2 == null) {
                return 1;
            } else {
                if(isEqualRange == null) {
                    return o1.compareTo(o2);
                } else {
                    LocalDateTime lowerLimit = o1.minus(isEqualRange);
                    LocalDateTime upperLimit = o1.plus(isEqualRange);

                    if(lowerLimit.isBefore(o2) && upperLimit.isAfter(o2)) {
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

    @Override
    public List<ScriptParameter> getParameters() {
        List<ScriptParameter> scriptParameters = new ArrayList<>();
        scriptParameters.add(TIME_UNIT.setOptions(asList("Day", "Hour", "Minute", "Second")));
        scriptParameters.add(TIME_VALUE);
        return scriptParameters;
    }
}
