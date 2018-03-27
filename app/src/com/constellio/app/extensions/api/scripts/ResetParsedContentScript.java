package com.constellio.app.extensions.api.scripts;

import com.constellio.app.modules.rm.services.RMSchemasRecordsServices;
import com.constellio.app.modules.rm.wrappers.Document;
import com.constellio.app.services.factories.AppLayerFactory;
import com.constellio.data.dao.services.bigVault.SearchResponseIterator;
import com.constellio.data.io.services.facades.IOServices;
import com.constellio.model.entities.records.ConditionnedActionExecutorInBatchBuilder;
import com.constellio.model.entities.records.Content;
import com.constellio.model.entities.records.Record;
import com.constellio.model.entities.records.Transaction;
import com.constellio.model.entities.records.wrappers.UserDocument;
import com.constellio.model.entities.schemas.Schemas;
import com.constellio.model.services.contents.ContentManager;
import com.constellio.model.services.search.SearchServices;
import com.constellio.model.services.search.query.logical.LogicalSearchQuery;
import com.constellio.model.services.search.query.logical.LogicalSearchQueryOperators;

import java.io.File;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static com.constellio.model.services.migrations.ConstellioEIMConfigs.PARSED_CONTENT_MAX_LENGTH_IN_KILOOCTETS;

public class ResetParsedContentScript extends ScriptWithLogOutput {
    public ResetParsedContentScript(AppLayerFactory appLayerFactory) {
        super(appLayerFactory, "Content", "ResetParsedContentScript");
    }

    @Override
    protected void execute() throws Exception {
        final IOServices ioServices = dataLayerFactory.getIOServicesFactory().newIOServices();
        final ContentManager contentManager = modelLayerFactory.getContentManager();
        final int maxParsedContentSize = modelLayerFactory.getSystemConfigurationsManager().getValue(PARSED_CONTENT_MAX_LENGTH_IN_KILOOCTETS);
        final Set<String> deletedHash = new HashSet<>();

        for(String collection : appLayerFactory
                .getCollectionsManager().getCollectionCodesExcludingSystem()) {
            final RMSchemasRecordsServices rm = new RMSchemasRecordsServices(collection, appLayerFactory);

            onCondition(LogicalSearchQueryOperators.from(rm.documentSchemaType()).returnAll())
            .modifyingRecordsWithImpactHandling(new ConditionnedActionExecutorInBatchBuilder.RecordScript() {
                    @Override
                    public void modifyRecord(Record record) {
                        Document document = rm.wrapDocument(record);
                        Content content = document.getContent();
                        if(content != null) {
                            String currentHash = content.getCurrentVersion().getHash();
                            String parsedContentFileName = contentManager.getParsedContentFileName(currentHash);
                            File parsedFilePath = contentManager.getContentDao().getFileOf(parsedContentFileName);

                            if(parsedFilePath.exists() && (parsedFilePath.length() / 1024) > maxParsedContentSize) {
                                String filename = parsedFilePath.getName();
                                double fileSizeInKb = parsedFilePath.length() / 1024;
                                try {
                                    if(parsedFilePath.delete()) {
                                        outputLogger.appendToFile("Deleted file : " + filename + " with size : " + fileSizeInKb + "\n");
                                        deletedHash.add(currentHash);
                                        document.set(Schemas.MARKED_FOR_PARSING, true);
                                        document.set(Schemas.MARKED_FOR_REINDEXING, true);
                                    } else {
                                        outputLogger.appendToFile("Failed to delete : " + filename + "\n");
                                    }
                                } catch (Exception e) {
                                    outputLogger.appendToFile("Failed to delete : " + filename + "\n");
                                    e.printStackTrace();
                                }
                            } else if(deletedHash.contains(currentHash)) {
                                document.set(Schemas.MARKED_FOR_PARSING, true);
                                document.set(Schemas.MARKED_FOR_REINDEXING, true);
                            }
                        }
                    }
                }
            );
        }
    }
}
