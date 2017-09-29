package com.constellio.model.entities.schemas.entries;

import com.constellio.model.entities.schemas.Metadata;

import java.io.Serializable;
import java.util.List;

public interface AggregatedCalculator<T> extends Serializable {
    public T calculate(AggregatedValuesParams params);
    public List<String> getMetadataDependencies();
}
