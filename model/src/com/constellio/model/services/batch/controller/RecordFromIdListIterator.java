package com.constellio.model.services.batch.controller;

import com.constellio.model.entities.records.Record;
import com.constellio.model.services.factories.ModelLayerFactory;
import com.constellio.model.services.records.RecordServices;

import java.util.Iterator;
import java.util.List;

/**
 * Created by Constellio on 2017-02-01.
 */
public class RecordFromIdListIterator implements Iterator<Record> {

    List<String> records;
    int nextIndex;
    RecordServices recordServices;

    RecordFromIdListIterator(List<String> records, ModelLayerFactory modelLayerFactory) {
        this.records = records;
        nextIndex = 0;
        recordServices = modelLayerFactory.newRecordServices();
    }

    @Override
    public boolean hasNext() {
        return nextIndex < records.size();
    }

    @Override
    public Record next() {
        return recordServices.getDocumentById(records.get(nextIndex++));
    }

    public void beginAfterId(String lastId) {
        nextIndex = records.indexOf(lastId) + 1;
    }
}
