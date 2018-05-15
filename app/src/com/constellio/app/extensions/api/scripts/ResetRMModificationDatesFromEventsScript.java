package com.constellio.app.extensions.api.scripts;

import com.constellio.app.modules.rm.services.RMSchemasRecordsServices;
import com.constellio.app.modules.rm.wrappers.Document;
import com.constellio.app.services.factories.AppLayerFactory;
import com.constellio.data.dao.services.contents.ContentDao;
import com.constellio.model.entities.records.ConditionnedActionExecutorInBatchBuilder;
import com.constellio.model.entities.records.Content;
import com.constellio.model.entities.records.Record;
import com.constellio.model.entities.records.wrappers.Event;
import com.constellio.model.entities.records.wrappers.EventType;
import com.constellio.model.entities.schemas.Schemas;
import com.constellio.model.services.contents.ContentManager;
import com.constellio.model.services.search.query.logical.LogicalSearchQuery;
import com.constellio.model.services.search.query.logical.LogicalSearchQueryOperators;
import org.joda.time.LocalDate;
import org.joda.time.LocalDateTime;

import java.util.HashMap;
import java.util.List;

import static com.constellio.model.services.search.query.logical.LogicalSearchQueryOperators.from;
import static java.util.Arrays.asList;

public class ResetRMModificationDatesFromEventsScript extends ScriptWithLogOutput {
    public ResetRMModificationDatesFromEventsScript(AppLayerFactory appLayerFactory) {
        super(appLayerFactory, "Record", "ResetRMModificationDatesFromEventsScript");
    }

    @Override
    protected void execute() throws Exception {
        for(String collection : appLayerFactory
                .getCollectionsManager().getCollectionCodesExcludingSystem()) {
            final RMSchemasRecordsServices rm = new RMSchemasRecordsServices(collection, appLayerFactory);
            final HashMap<String, LocalDateTime> recordsLastModificationDate = new HashMap<>();

            onCondition(LogicalSearchQueryOperators.from(rm.eventSchemaType())
            .where(rm.eventType()).isIn(asList(EventType.MODIFY_DOCUMENT, EventType.MODIFY_FOLDER)))
            .modifyingRecordsWithImpactHandling(new ConditionnedActionExecutorInBatchBuilder.RecordScript() {
                    @Override
                    public void modifyRecord(Record record) {
                        Event event = rm.wrapEvent(record);
                        String recordId = event.getRecordId();
                        LocalDateTime createdOn = event.getCreatedOn();
                        LocalDateTime previousDate = recordsLastModificationDate.get(recordId);
                        if(previousDate == null || previousDate.isBefore(createdOn)) {
                            recordsLastModificationDate.put(recordId, createdOn);
                        }
                    }
                }
            );

            onCondition(LogicalSearchQueryOperators.from(rm.documentSchemaType()).returnAll())
            .modifyingRecordsWithImpactHandling(new ConditionnedActionExecutorInBatchBuilder.RecordScript() {
                    @Override
                    public void modifyRecord(Record record) {
                        LocalDateTime lastModificationDate = recordsLastModificationDate.get(record.getId());
                        if(lastModificationDate != null) {
                            record.set(Schemas.MODIFIED_ON, lastModificationDate);
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
                            record.set(Schemas.MODIFIED_ON, lastModificationDate);
                        }
                    }
                }
            );
        }
    }
}
