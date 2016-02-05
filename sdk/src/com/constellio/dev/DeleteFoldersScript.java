package com.constellio.dev;

import com.constellio.app.modules.rm.services.RMSchemasRecordsServices;
import com.constellio.app.services.factories.AppLayerFactory;
import com.constellio.model.entities.records.Record;
import com.constellio.model.entities.records.wrappers.User;
import com.constellio.model.services.factories.ModelLayerFactory;
import com.constellio.model.services.records.RecordServices;
import com.constellio.model.services.records.extractions.RecordPopulateServices;
import com.constellio.model.services.search.SearchServices;

import java.util.Arrays;

import static com.constellio.app.utils.ScriptsUtils.startLayerFactoriesWithoutBackgroundThreads;

public class DeleteFoldersScript {

    static String currentCollection;
    static AppLayerFactory appLayerFactory;
    static ModelLayerFactory modelLayerFactory;
    static SearchServices searchServices;
    static RecordServices recordServices;
    static RMSchemasRecordsServices rm;

    public static void main(String argv[])
            throws Exception {

        RecordPopulateServices.LOG_CONTENT_MISSING = false;
        appLayerFactory = startLayerFactoriesWithoutBackgroundThreads();
        modelLayerFactory = appLayerFactory.getModelLayerFactory();
        searchServices = modelLayerFactory.newSearchServices();
        recordServices = modelLayerFactory.newRecordServices();

        String collection = "collectionImportationCalendrier";
        for(String objectId : Arrays.asList(new String[]{"00000003606","00000007027"})){
        //for (String collection : collectionsListManager.getCollections()) {
            currentCollection = collection;
            rm = new RMSchemasRecordsServices(collection, modelLayerFactory);

            User adminUser = modelLayerFactory.newUserServices().getUserInCollection("admin", collection);
            Record folderRecord = recordServices.getDocumentById(objectId);
            recordServices.logicallyDelete(folderRecord, adminUser);
            recordServices.physicallyDelete(folderRecord, adminUser);
        //}
        }

    }
}
