package com.constellio.model.services.logging;

import com.constellio.data.dao.dto.records.RecordsFlushing;
import com.constellio.model.entities.records.Transaction;
import com.constellio.model.entities.records.wrappers.SearchEvent;
import com.constellio.model.entities.records.wrappers.SearchFeature;
import com.constellio.model.services.factories.ModelLayerFactory;
import com.constellio.model.services.records.RecordServicesException;
import com.constellio.model.services.records.SchemasRecordsServices;

import java.util.List;

import static java.util.Arrays.asList;

public class SearchFeatureServices {
    private ModelLayerFactory modelLayerFactory;
    private String collection;
    private SchemasRecordsServices schemas;

    public SearchFeatureServices(String collection, ModelLayerFactory modelLayerFactory) {
        this.modelLayerFactory = modelLayerFactory;
        this.collection = collection;
        this.schemas = new SchemasRecordsServices(collection, modelLayerFactory);
    }

    public ModelLayerFactory getModelLayerFactory() {
        return modelLayerFactory;
    }

    public String getCollection() {
        return collection;
    }

    public SchemasRecordsServices getSchemas() {
        return schemas;
    }

    public void save(SearchFeature searchFeature) {
        save(asList(searchFeature));
    }

    public void save(List<SearchFeature> searchFeatures) {
        Transaction tx = new Transaction();
        tx.addAll(searchFeatures);
        tx.setRecordFlushing(RecordsFlushing.ADD_LATER());

        try {
            modelLayerFactory.newRecordServices().execute(tx);
        } catch (RecordServicesException e) {
            throw new RuntimeException(e);
        }
    }
}
