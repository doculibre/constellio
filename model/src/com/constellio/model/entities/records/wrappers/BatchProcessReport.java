package com.constellio.model.entities.records.wrappers;

import com.constellio.model.entities.records.Record;
import com.constellio.model.entities.schemas.MetadataSchemaTypes;
import org.joda.time.LocalDateTime;

import java.util.List;

public class BatchProcessReport extends TemporaryRecord {
    public static final String SCHEMA = "batchProcessReport";
    public static final String FULL_SCHEMA = SCHEMA_TYPE + "_" + SCHEMA;
    public static final String ERRORS = "errors";
    public static final String MESSAGES = "messages";
    public static final String LINKED_BATCH_PROCESS = "linkedBatchProcess";

    public BatchProcessReport(Record record, MetadataSchemaTypes types) {
        super(record, types);
    }

    public String getErrors() {
        return get(ERRORS);
    }

    public BatchProcessReport appendErrors(List<String> newErrors) {
        String previousErrors = getErrors();
        StringBuilder errorFormatter = new StringBuilder();
        if(previousErrors != null) {
            errorFormatter.append(previousErrors);
        }

        if(newErrors != null) {
            for(String error: newErrors) {
                errorFormatter.append("\n");
                errorFormatter.append(error);
            }
        }

        setErrors(errorFormatter.toString());

        return this;
    }

    public BatchProcessReport setErrors(String stringList) {
        set(ERRORS, stringList);
        return this;
    }

    public List<String> getMessages() {
        return get(MESSAGES);
    }

    public BatchProcessReport setMessages(List<String> stringList) {
        set(MESSAGES, stringList);
        return this;
    }

    public BatchProcessReport addMessages(List<String> stringList) {
        List<String> previousMessages = getList(MESSAGES);
        previousMessages.addAll(stringList);
        set(MESSAGES, previousMessages);
        return this;
    }

    public BatchProcessReport addMessage(String string) {
        List<String> previousMessages = getList(MESSAGES);
        previousMessages.add(string);
        set(MESSAGES, previousMessages);
        return this;
    }

    public String getLinkedBatchProcess() {
        return get(LINKED_BATCH_PROCESS);
    }

    public BatchProcessReport setLinkedBatchProcess(String batchProcessId) {
        set(LINKED_BATCH_PROCESS, batchProcessId);
        return this;
    }
}
