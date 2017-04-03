package com.constellio.app.modules.tasks.model.wrappers.request;

import com.constellio.app.modules.tasks.model.wrappers.Task;
import com.constellio.model.entities.records.Record;
import com.constellio.model.entities.schemas.MetadataSchemaTypes;

/**
 * Created by Constellio on 2017-04-03.
 */
abstract public class RequestTask extends Task{
    public static final String ACCEPTED = "accepted";
    public static final String APPLICANT = "applicant";

    public RequestTask(Record record, MetadataSchemaTypes types) {
        super(record, types);
    }

    public boolean isAccepted() {
        return Boolean.TRUE.equals(get(ACCEPTED));
    }

    public RequestTask setAccepted(boolean accepted) {
        set(ACCEPTED, accepted);
        return this;
    }

    public String getApplicant() {
        return (String) get(APPLICANT);
    }

    public RequestTask setApplicant(String applicant) {
        set(APPLICANT, applicant);
        return this;
    }
}
