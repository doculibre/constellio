package com.constellio.app.extensions.api.scripts;

import com.constellio.app.modules.rm.services.RMSchemasRecordsServices;
import com.constellio.app.modules.rm.wrappers.Document;
import com.constellio.app.services.factories.AppLayerFactory;
import com.constellio.data.dao.services.bigVault.SearchResponseIterator;
import com.constellio.data.io.services.facades.IOServices;
import com.constellio.model.entities.records.Record;
import com.constellio.model.entities.schemas.Schemas;
import com.constellio.model.services.contents.ContentManager;
import com.constellio.model.services.search.SearchServices;
import com.constellio.model.services.search.query.logical.LogicalSearchQuery;
import com.constellio.model.services.search.query.logical.LogicalSearchQueryOperators;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import static com.constellio.model.services.migrations.ConstellioEIMConfigs.PARSED_CONTENT_MAX_LENGTH_IN_KILOOCTETS;

public class ResetParsedContentScript extends ScriptWithLogOutput {
    public ResetParsedContentScript(AppLayerFactory appLayerFactory) {
        super(appLayerFactory, "Records", "ResetParsedContentScript");
    }

    @Override
    protected void execute() throws Exception {
        List<File> documentToSuppress = new ArrayList<>();
        SearchServices searchServices = modelLayerFactory.newSearchServices();
        IOServices ioServices = dataLayerFactory.getIOServicesFactory().newIOServices();

        for(String collection : appLayerFactory
                .getCollectionsManager().getCollectionCodesExcludingSystem())
        {
            RMSchemasRecordsServices rmSchemasRecordsServices = new RMSchemasRecordsServices(collection, appLayerFactory);
            LogicalSearchQuery logicalSearchQuery = new LogicalSearchQuery();
            logicalSearchQuery.setCondition(LogicalSearchQueryOperators.from(rmSchemasRecordsServices.documentSchemaType()).returnAll());
            SearchResponseIterator<Record> searchResponseIterator = searchServices.recordsIterator(logicalSearchQuery, 2000);
            ContentManager contentManager = modelLayerFactory.getContentManager();

            while(searchResponseIterator.hasNext()) {
                 Record currentRecord = searchResponseIterator.next();
                 Document document = rmSchemasRecordsServices.wrapDocument(currentRecord);

                 String fileName = contentManager.getParsedContentFileName(document.getContent().getCurrentVersion().getHash());
                 File parsedFilePath = contentManager.getContentDao().getFileOf(fileName);

                 if(parsedFilePath.exists() && (parsedFilePath.length() / 1024) > (int)  modelLayerFactory.getSystemConfigurationsManager().getValue(PARSED_CONTENT_MAX_LENGTH_IN_KILOOCTETS)) {
                     documentToSuppress.add(parsedFilePath);
                     document.set(Schemas.MARKED_FOR_PARSING, true);
                     document.set(Schemas.MARKED_FOR_REINDEXING, true);
                 }
            }

            for(File currentFileToSuppress : documentToSuppress) {
                if (currentFileToSuppress.exists()) {
                    ioServices.deleteQuietly(currentFileToSuppress);
                    outputLogger.appendToFile("Deleted file : " + currentFileToSuppress.getName() + "\n");
                }
            }
        }

        outputLogger.appendToFile("Execution done. Deleted : " + documentToSuppress.size() + " parsed document");
    }
}
