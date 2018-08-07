package com.constellio.model.entities.records.wrappers;

import com.constellio.model.entities.records.Record;
import com.constellio.model.entities.schemas.MetadataSchemaTypes;
import org.joda.time.LocalDateTime;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class BatchProcessReport extends TemporaryRecord {
    public static final String SCHEMA = "batchProcessReport";
    public static final String FULL_SCHEMA = SCHEMA_TYPE + "_" + SCHEMA;
    public static final String ERRORS = "errors";
    public static final String SKIPPED_RECORDS = "skippedRecords";
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
        String prefix = "";
        if(previousErrors != null) {
            errorFormatter.append(previousErrors);
            prefix = "\n";
        }

        if(newErrors != null) {
            for(String error: newErrors) {
                errorFormatter.append(prefix);
                errorFormatter.append(error);
                prefix = "\n";
            }
        }

        setErrors(errorFormatter.toString());

        return this;
    }

    public BatchProcessReport setErrors(String stringList) {
        set(ERRORS, stringList);
        return this;
    }

    public List<String> getSkippedRecords() {
        return get(SKIPPED_RECORDS);
    }

    public BatchProcessReport setSkippedRecords(List<String> stringList) {
        set(SKIPPED_RECORDS, stringList);
        return this;
    }

    public BatchProcessReport addSkippedRecords(List<String> stringList) {
        List<String> previousSkippedRecords = getList(SKIPPED_RECORDS);
        Set<Object> currentSkippedRecords = new HashSet<>();
        currentSkippedRecords.addAll(previousSkippedRecords);
        currentSkippedRecords.addAll(stringList);
        set(SKIPPED_RECORDS, new ArrayList<>(currentSkippedRecords));
        return this;
    }

    public BatchProcessReport addSkippedRecord(String string) {
        List<String> previousSkippedRecords = getList(SKIPPED_RECORDS);
        ArrayList<Object> currentSkippedRecords = new ArrayList<>();
        currentSkippedRecords.addAll(previousSkippedRecords);
        currentSkippedRecords.add(string);
        set(SKIPPED_RECORDS, currentSkippedRecords);
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
