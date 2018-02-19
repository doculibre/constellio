package com.constellio.app.api.extensions.params;

import com.constellio.model.entities.records.Record;
import com.constellio.model.entities.records.wrappers.User;

public class DocumentViewButtonExtensionParam {

    private Record record;

    private User user;

    public DocumentViewButtonExtensionParam(Record record, User user) {
        this.record = record;
        this.user = user;
    }

    public User getUser(){
        return user;
    }

    public Record getRecord() {
        return record;
    }
}
