package com.constellio.model.services.thesaurus;

import com.constellio.data.dao.managers.StatefulService;
import com.constellio.data.dao.services.cache.ConstellioCache;
import com.constellio.data.dao.services.cache.ConstellioCacheManager;
import com.constellio.data.dao.services.contents.ContentDaoException;
import com.constellio.model.entities.records.Record;
import com.constellio.model.entities.records.wrappers.ThesaurusConfig;
import com.constellio.model.services.collections.CollectionsListManager;
import com.constellio.model.services.factories.ModelLayerFactory;
import com.constellio.model.services.records.SchemasRecordsServices;
import com.constellio.model.services.search.SearchServices;
import com.constellio.model.services.search.query.logical.LogicalSearchQuery;
import com.constellio.model.services.search.query.logical.LogicalSearchQueryOperators;
import com.constellio.model.services.thesaurus.exception.ThesaurusInvalidFileFormat;

import java.io.InputStream;
import java.util.List;

public class ThesaurusManager implements StatefulService {

    final String FILE_INPUT_STREAM_NAME = "ThesaurusManager.ThesaurusFile";

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
        return cache.get(collection);
    }

    public void set(InputStream inputStream, String collection) throws ThesaurusInvalidFileFormat {
        initializeServiceForCollection(inputStream, collection);
    }

    /**
     * Saves Thesaurus to persistent memory.
     * @return true if success
     */
    public boolean save(){
        // TODO continue
        return false;
    }

    public void initializeServiceForCollection(InputStream inputStream, String collection) throws ThesaurusInvalidFileFormat {
        ThesaurusService thesaurusService = ThesaurusBuilder.getThesaurus(inputStream);
        cache.put(collection, thesaurusService);
    }

    @Override
    public void initialize() {


        final CollectionsListManager collectionsListManager = modelLayerFactory.getCollectionsListManager();
        final SearchServices searchServices = modelLayerFactory.newSearchServices();

        List<String> collectionCodes = collectionsListManager.getCollectionsExcludingSystem();

        for(String collectionCode : collectionCodes){
            ThesaurusConfig thesaurusConfig;


            SchemasRecordsServices schemas = new SchemasRecordsServices(collectionCode, modelLayerFactory);
            List<Record> thesaurusConfigRecordFound = searchServices.cachedSearch(new LogicalSearchQuery(LogicalSearchQueryOperators.from(schemas.thesaurusConfig.schemaType()).returnAll()));
                if(thesaurusConfigRecordFound != null && thesaurusConfigRecordFound.size() ==  1) {
                    thesaurusConfig = schemas.wrapThesaurusConfig(thesaurusConfigRecordFound.get(0));
                    InputStream inputStream;
                    if(thesaurusConfig.getContent()!= null) {
                        try {
                            inputStream = modelLayerFactory.getContentManager().getContentDao()
                                    .getContentInputStream(thesaurusConfig.getContent()
                                            .getCurrentVersion().getHash(), FILE_INPUT_STREAM_NAME);
                            initializeServiceForCollection(inputStream, collectionCode);
                        } catch (ContentDaoException.ContentDaoException_NoSuchContent contentDaoException_noSuchContent) {
                            contentDaoException_noSuchContent.printStackTrace();
                            throw new RuntimeException("Thesaurus noSuchContent during system start up. Should never happen.");
                        } catch (ThesaurusInvalidFileFormat thesaurusInvalidFileFormat) {
                            thesaurusInvalidFileFormat.printStackTrace();
                            throw new RuntimeException("Thesaurus invalid file format during system start up. Should never happen.");
                        }

                    }
                } else if (thesaurusConfigRecordFound.size() > 1) {
                    throw new RuntimeException("There should only have one thesaurusConfig per collection.");
                }
            }
    }


    @Override
    public void close() {
        // nothing to close (no threads to kill)
    }
}
