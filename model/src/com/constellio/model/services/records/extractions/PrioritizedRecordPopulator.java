package com.constellio.model.services.records.extractions;

import com.constellio.model.entities.records.Record;

import java.util.Comparator;
import java.util.List;

public class PrioritizedRecordPopulator implements RecordPopulator{
    public PrioritizedRecordPopulator(List<? extends RecordPopulator> populators, Comparator<? extends RecordPopulator> comparator){
        //TODO
    }

    @Override
    public Object getPopulationValue(Record record) {
        //TODO
        return null;
    }
}
