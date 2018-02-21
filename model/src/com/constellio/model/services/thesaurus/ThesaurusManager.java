package com.constellio.model.services.thesaurus;

import com.constellio.data.dao.managers.StatefulService;
import com.constellio.data.dao.services.cache.ConstellioCache;
import com.constellio.data.dao.services.cache.ConstellioCacheManager;
import com.constellio.data.dao.services.cache.ignite.ConstellioIgniteCacheManager;
import com.constellio.model.entities.records.Record;
import com.constellio.model.entities.records.wrappers.ThesaurusConfig;
import com.constellio.model.services.collections.CollectionsListManager;
import com.constellio.model.services.factories.ModelLayerFactory;
import com.constellio.model.services.records.RecordServices;
import com.constellio.model.services.records.SchemasRecordsServices;
import com.constellio.model.services.search.SearchServices;
import com.constellio.model.services.search.query.logical.LogicalSearchQuery;
import com.constellio.model.services.search.query.logical.LogicalSearchQueryOperators;
import com.constellio.model.services.thesaurus.exception.ThesaurusInvalidFileFormat;

import java.io.FileInputStream;
import java.io.InputStream;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ThesaurusManager implements StatefulService {

    final String FILE_INPUT_STREAM_NAME = "ThesaurusManager.ThesaurusFile";

    private ThesaurusService thesaurusService;
    ConstellioCache cache;
    ModelLayerFactory modelLayerFactory;

    public ThesaurusManager(ModelLayerFactory modelLayerFactory) {
        this.modelLayerFactory = modelLayerFactory;

        ConstellioCacheManager recordsCacheManager = this.modelLayerFactory.getDataLayerFactory().getRecordsCacheManager();
        cache = recordsCacheManager.getCache("parsedThesaurus"); // this is a map!
    }

    /**
     * Gets thesaurus in non persistent memory.
     *
     * @return
     */
    public ThesaurusService get(String collection) {
        return thesaurusService;
    }

    public void set(InputStream fileInputStream) {
//        skosFileStream = fileInputStream;
        initialize();
    }

    /**
     * Saves Thesaurus to persistent memory.
     * @return true if success
     */
    public boolean save(){
        // TODO continue
        return false;
    }

    @Override
    public void initialize() {


        final CollectionsListManager collectionsListManager = modelLayerFactory.getCollectionsListManager();
        final SearchServices searchServices = modelLayerFactory.newSearchServices();
        final RecordServices recordServices = modelLayerFactory.newRecordServices();

        List<String> collectionCodes = collectionsListManager.getCollectionsExcludingSystem(); // codes de collection

        for(String collectionCode : collectionCodes){ // parcour chaque code collection

            // getting
            List<String> collectionLanguages = collectionsListManager.getCollectionLanguages(collectionCode); // TODO problem : does not return a single lang! (what's up for??
            ThesaurusConfig thesaurusConfig;

            // getting thesaurus configs

            SchemasRecordsServices schemas = new SchemasRecordsServices(collectionCode, modelLayerFactory);
            List<Record> thesaurusConfigRecordFound = searchServices.cachedSearch(new LogicalSearchQuery(LogicalSearchQueryOperators.from(schemas.thesaurusConfig.schemaType()).returnAll()));

                if(thesaurusConfigRecordFound != null && thesaurusConfigRecordFound.size() ==  1) {
                    thesaurusConfig = schemas.wrapThesaurusConfig(thesaurusConfigRecordFound.get(0));
                }
//                modelLayerFactory.getContentManager().getContentDao().getContentInputStream(thesaurusConfig.getContent().getCurrentVersion().getHash(), FILE_INPUT_STREAM_NAME);

//                cache.put(new ThesaurusService(thesaurusConfig);
            }

            // aller chercher config de la collection courante : version, contentDAO, obtenir inputStream

//        initializeThesaurus();
//        if(skosFileStream != null) {
//            try {
//                this.thesaurusService = ThesaurusBuilder.getThesaurus(skosFileStream);
//            } catch (ThesaurusInvalidFileFormat thesaurusInvalidFileFormat) {
//                thesaurusInvalidFileFormat.printStackTrace();
//            }
//        }
    }

    private void initializeThesaurus() {
        initializeThesaurus();
    }

    @Override
    public void close() {
        // nothing to close (no threads to kill)
    }
}
