package com.constellio.dev;

import com.constellio.app.modules.rm.services.RMSchemasRecordsServices;
import com.constellio.app.modules.rm.wrappers.Document;
import com.constellio.app.services.factories.AppLayerFactory;
import com.constellio.model.entities.records.ActionExecutorInBatch;
import com.constellio.model.entities.records.ContentVersion;
import com.constellio.model.entities.records.Record;
import com.constellio.model.entities.schemas.Metadata;
import com.constellio.model.entities.schemas.MetadataSchema;
import com.constellio.model.services.collections.CollectionsListManager;
import com.constellio.model.services.factories.ModelLayerFactory;
import com.constellio.model.services.records.RecordServices;
import com.constellio.model.services.records.extractions.RecordPopulateServices;
import com.constellio.model.services.search.SearchServices;
import com.constellio.model.services.search.query.ReturnedMetadatasFilter;
import com.constellio.model.services.search.query.logical.LogicalSearchQuery;
import com.constellio.model.services.search.query.logical.LogicalSearchQueryOperators;
import com.constellio.model.services.search.query.logical.condition.LogicalSearchCondition;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import static com.constellio.app.utils.ScriptsUtils.startLayerFactoriesWithoutBackgroundThreads;
import static com.constellio.sdk.tests.TestUtils.asList;

public class ExtractImportationLogsScript {

    static String currentCollection;
    static AppLayerFactory appLayerFactory;
    static ModelLayerFactory modelLayerFactory;
    static SearchServices searchServices;
    static RecordServices recordServices;
    static RMSchemasRecordsServices rm;

    static Path path;
    static String type =  "all";
    static int batchSize = 5000;

    static AtomicInteger counter = new AtomicInteger();

    public static void main(String argv[])
            throws Exception {

        if(argv.length < 2){
            System.out.println("Sample call : sudo java -Xmx5120m -classpath ./classes:./lib/* com.constellio.dev.ExtractImportationLogsScript <type> <batchSize>");
            return;
        }


        type = argv[0];
        try {
            batchSize = Integer.valueOf(argv[1]);
        }catch (Exception e){
            e.printStackTrace();;

            System.out.println("Invalid batch size value");

            return;
        }
        initLogger();

        RecordPopulateServices.LOG_CONTENT_MISSING = false;
        appLayerFactory = startLayerFactoriesWithoutBackgroundThreads();
        modelLayerFactory = appLayerFactory.getModelLayerFactory();
        searchServices = modelLayerFactory.newSearchServices();
        recordServices = modelLayerFactory.newRecordServices();
        CollectionsListManager collectionsListManager = modelLayerFactory.getCollectionsListManager();
        for (String collection : collectionsListManager.getCollections()) {
            currentCollection = collection;
            rm = new RMSchemasRecordsServices(collection, modelLayerFactory);
            runScriptForCurrentCollection();
        }

    }

    private static void runScriptForCurrentCollection() throws Exception {

        if("folder".equalsIgnoreCase(type)) {
            for (MetadataSchema folderSchema : rm.folderSchemaType().getAllSchemas()) {
                runScriptForRMObject(folderSchema);
            }
        } else if ("document".equalsIgnoreCase(type)){
            for (MetadataSchema documentSchema : rm.documentSchemaType().getAllSchemas()) {
                runScriptForRMObject(documentSchema);
            }
        }

    }

    private static void runScriptForRMObject(final MetadataSchema schema)
            throws Exception {

        final Metadata legacyIdMetadata = schema.get("legacyIdentifier");
        final Metadata titleMetadata = schema.get("title");
        // TODO get document contents Metadata

        LogicalSearchCondition condition = LogicalSearchQueryOperators.from(schema).whereAnyCondition(
                LogicalSearchQueryOperators.allConditions(
                        LogicalSearchQueryOperators.where(legacyIdMetadata).isNotNull(),
                        LogicalSearchQueryOperators.where(legacyIdMetadata).isNotEqual("__NULL__")
                )
        );


        if("folder".equalsIgnoreCase(type)){
            new ActionExecutorInBatch(searchServices, "Update '" + schema.getCode() + "'", batchSize) {

                @Override
                public void doActionOnBatch(List<Record> records)
                        throws Exception {


                    for (Record record : records) {

                        String legacyId = record.get(legacyIdMetadata);
                        String title = record.get(titleMetadata);

                        String recordString = "#" + counter.intValue() + " LegacyId:" + legacyId + ":" + title;
                        System.out.println(recordString);

                        append(recordString+"\n");

                        counter.incrementAndGet();
                    }
                }
            }.execute(new LogicalSearchQuery(condition).setReturnedMetadatas(ReturnedMetadatasFilter.onlyMetadatas(asList(legacyIdMetadata, titleMetadata))));
        } else if("document".equalsIgnoreCase(type)) {
            new ActionExecutorInBatch(searchServices, "Update '" + schema.getCode() + "'", batchSize) {

                @Override
                public void doActionOnBatch(List<Record> records)
                        throws Exception {


                    for (Record record : records) {

                        String legacyId = record.get(legacyIdMetadata);
                        String title = record.get(titleMetadata);

                        String recordString = "#" + counter.intValue() + " LegacyId:" + legacyId + ":" + title + ", versions:";

                        Document document = rm.wrapDocument(record);

                        int versionsCounter = 0;
                        try {
                            for (ContentVersion version : document.getContent().getVersions()) {
                                if (versionsCounter > 0) {
                                    recordString += ", ";
                                }
                                recordString += version.getVersion();
                                versionsCounter++;
                            }
                        }catch (NullPointerException e){
                            e.printStackTrace();
                        }

                        System.out.println(recordString);

                        append(recordString + "\n");

                        counter.incrementAndGet();
                    }
                }
            }.execute(new LogicalSearchQuery(condition));//setReturnedMetadatas(ReturnedMetadatasFilter.onlyMetadatas(asList(legacyIdMetadata, titleMetadata))));
        }
    }

    private static void initLogger() throws IOException {
        String datePart = new SimpleDateFormat("yyyy-MM-dd-HHmmss").format(new Date());
        path = Paths.get("identifiants-"+type+"-importes-" + datePart + ".txt");

        if (!Files.exists(path)) {
            Files.createFile(path);
        }
    }

    public static void append(String message) throws IOException {
        Files.write(path, message.getBytes(), StandardOpenOption.APPEND);
    }
}
