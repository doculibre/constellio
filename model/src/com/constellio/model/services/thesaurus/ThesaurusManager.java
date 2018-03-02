package com.constellio.model.services.thesaurus;

import com.constellio.data.dao.managers.StatefulService;
import com.constellio.data.dao.services.cache.ConstellioCache;
import com.constellio.data.dao.services.cache.ConstellioCacheManager;
import com.constellio.data.dao.services.contents.ContentDaoException;
import com.constellio.data.threads.BackgroundThreadConfiguration;
import com.constellio.data.threads.BackgroundThreadExceptionHandling;
import com.constellio.data.threads.BackgroundThreadsManager;
import com.constellio.model.entities.records.Record;
import com.constellio.model.entities.records.wrappers.ThesaurusConfig;
import com.constellio.model.services.collections.CollectionsListManager;
import com.constellio.model.services.factories.ModelLayerFactory;
import com.constellio.model.services.logging.SearchEventServices;
import com.constellio.model.services.records.SchemasRecordsServices;
import com.constellio.model.services.search.SearchServices;
import com.constellio.model.services.search.query.logical.LogicalSearchQuery;
import com.constellio.model.services.search.query.logical.LogicalSearchQueryOperators;
import com.constellio.model.services.thesaurus.exception.ThesaurusInvalidFileFormat;

import java.io.InputStream;
import java.util.List;

public class ThesaurusManager implements StatefulService {

    final String FILE_INPUT_STREAM_NAME = "ThesaurusManager.ThesaurusFile";
    private CollectionsListManager collectionsListManager;
    private SearchServices searchServices;

    private ConstellioCache cache;
    private ModelLayerFactory modelLayerFactory;


    public ThesaurusManager(ModelLayerFactory modelLayerFactory) {
        this.modelLayerFactory = modelLayerFactory;

        ConstellioCacheManager recordsCacheManager = this.modelLayerFactory.getDataLayerFactory().getRecordsCacheManager();
        cache = recordsCacheManager.getCache("parsedThesaurus"); // this is a map!

        collectionsListManager = this.modelLayerFactory.getCollectionsListManager();
        searchServices = this.modelLayerFactory.newSearchServices();
    }

    /**
     * Gets Thesaurus in non persistent memory for a given collection.
     * @param collection
     * @return
     */
    public ThesaurusService get(String collection) {
        return cache.get(collection);
    }

    /**
     * Sets Thesaurus in non persistent memory for a given collection.
     * @param
     */
    public void set(InputStream inputStream, String collection) throws ThesaurusInvalidFileFormat {
        ThesaurusService thesaurusService = getThesaurusService(inputStream);
        thesaurusService.setSearchEventServices(new SearchEventServices(collection, modelLayerFactory));
        cache.put(collection, thesaurusService);
    }

    /**
     * Saves Thesaurus to persistent memory.
     * @return true if success
     */
    public boolean save(){
        // TODO continue
        return false;
    }

    /**
     * Initialize a service per existing collection.
     */
    @Override
    public void initialize() {

        List<String> collections = collectionsListManager.getCollectionsExcludingSystem();

        for(String collection : collections){
            ThesaurusConfig thesaurusConfig = getThesaurusConfigs(collection);

            if(thesaurusConfig!=null) {
                ThesaurusService thesaurusService = getThesaurusService(getThesaurusFile(thesaurusConfig));
                thesaurusService.setDeniedTerms(thesaurusConfig.getDenidedWords());
                thesaurusService.setSearchEventServices(new SearchEventServices(collection, modelLayerFactory));
                cache.put(collection, thesaurusService);
            }
        }

    }

    private InputStream getThesaurusFile(ThesaurusConfig thesaurusConfig) {

        InputStream thesaurusFile = null;

            // getting Thesaurus file

            try {
                thesaurusFile = modelLayerFactory.getContentManager().getContentDao().getContentInputStream(thesaurusConfig.getContent().getCurrentVersion().getHash(), FILE_INPUT_STREAM_NAME);
            } catch (ContentDaoException.ContentDaoException_NoSuchContent contentDaoException_noSuchContent) {
                new RuntimeException("Invalid Thesaurus file content in DAO.");
            }

        return thesaurusFile;
    }

    private ThesaurusConfig getThesaurusConfigs(String collection) {
        ThesaurusConfig thesaurusConfig = null;
        SchemasRecordsServices schemas = new SchemasRecordsServices(collection, modelLayerFactory);

        List<Record> thesaurusConfigRecordFound = searchServices.cachedSearch(new LogicalSearchQuery(LogicalSearchQueryOperators.from(schemas.thesaurusConfig.schemaType()).returnAll()));

        if(thesaurusConfigRecordFound != null && thesaurusConfigRecordFound.size() >  1) {
            thesaurusConfig = schemas.wrapThesaurusConfig(thesaurusConfigRecordFound.get(0));
        }

        return thesaurusConfig;
    }

    private ThesaurusService getThesaurusService(InputStream thesaurusFile){
        ThesaurusService thesaurusService = null;

        try {
            thesaurusService = ThesaurusServiceBuilder.getThesaurus(thesaurusFile);
        } catch (ThesaurusInvalidFileFormat thesaurusInvalidFileFormat) {
            new RuntimeException("Invalid Thesaurus file format.");
        }

        return thesaurusService;
    }

    @Override
    public void close() {
        // nothing to close (no threads to kill)
    }
}
