package com.constellio.dev;

import com.constellio.app.modules.rm.services.RMSchemasRecordsServices;
import com.constellio.app.modules.rm.wrappers.Document;
import com.constellio.app.modules.rm.wrappers.type.DocumentType;
import com.constellio.app.services.factories.AppLayerFactory;
import com.constellio.data.dao.dto.records.OptimisticLockingResolution;
import com.constellio.model.entities.records.ActionExecutorInBatch;
import com.constellio.model.entities.records.Record;
import com.constellio.model.entities.records.Transaction;
import com.constellio.model.entities.records.wrappers.User;
import com.constellio.model.entities.schemas.Metadata;
import com.constellio.model.entities.schemas.MetadataSchema;
import com.constellio.model.services.factories.ModelLayerFactory;
import com.constellio.model.services.records.RecordServices;
import com.constellio.model.services.records.RecordServicesException;
import com.constellio.model.services.records.extractions.RecordPopulateServices;
import com.constellio.model.services.search.SearchServices;
import com.constellio.model.services.search.query.logical.LogicalSearchQuery;
import com.constellio.model.services.search.query.logical.LogicalSearchQueryOperators;
import com.constellio.model.services.search.query.logical.condition.LogicalSearchCondition;
import com.constellio.sdk.SDKScriptUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.List;

import static com.constellio.model.services.search.query.logical.LogicalSearchQueryOperators.from;
import static org.assertj.core.api.Assertions.assertThat;

public class DeleteAllDocumentTypesScript {

    private static Logger LOG = Logger.getLogger(DeleteAllDocumentTypesScript.class);
    static int BATCH_SIZE = 5000;

    static String currentCollection;
    static AppLayerFactory appLayerFactory;
    static ModelLayerFactory modelLayerFactory;
    static SearchServices searchServices;
    static RecordServices recordServices;
    static RMSchemasRecordsServices rm;
    private static LogicalSearchQuery documentTypesQuery;

    private static void startBackend() {
        //TODO

        //Only enable this line to run in production
        //appLayerFactory = startLayerFactoriesWithoutBackgroundThreads();

        //Only enable this line to run on developer workstation
        appLayerFactory = SDKScriptUtils.startApplicationWithoutBackgroundProcessesAndAuthentication();

    }

    private static LogicalSearchQuery getQuery(MetadataSchema metadataSchema) {
        //TODO Build a query to find records to modify or to return all records
        //return new LogicalSearchQuery(from(rm.folderSchemaType())
        //        .where(rm.containerAdministrativeUnit()).isEqualTo("42"));

        final Metadata documentTypeMetadata = metadataSchema.getMetadata("type");

        LogicalSearchCondition condition = LogicalSearchQueryOperators.from(metadataSchema).whereAllConditions(
                LogicalSearchQueryOperators.where(documentTypeMetadata).isNotNull(),
                LogicalSearchQueryOperators.where(documentTypeMetadata).isNotEqual("__NULL__"));

        return new LogicalSearchQuery(from(rm.documentSchemaType()).where(condition));

    }

    public static LogicalSearchQuery getDocumentTypesQuery() {

        final Metadata documentTypeMetadata = rm.documentTypeSchema().getMetadata("code");

        LogicalSearchCondition condition = LogicalSearchQueryOperators
                .from(rm.documentTypeSchema())
                .whereAllConditions(
                        LogicalSearchQueryOperators.where(documentTypeMetadata).isNotNull(),
                        LogicalSearchQueryOperators.where(documentTypeMetadata).isNotEqual("__NULL__"));

        return new LogicalSearchQuery(from(rm.documentTypeSchema()).where(condition));
    }

    private static void runScriptForCurrentCollection() throws Exception {

        // Delete all document type references from documents
        deleteDocumentsDocumentTypeReferences();

        //computeAdminId();

        // Delete documentTypes logically
        deleteDocumentTypes();

    }

    private static void initDocumentTypeCodes(File file) {
        documentTypeCodes = new ArrayList<>();

        try (BufferedReader bufferedReader = new BufferedReader(new FileReader(file))) {
            String line = "";
            while (StringUtils.isNotBlank(line = bufferedReader.readLine())) {
               if (StringUtils.isNotBlank(line)) {
                    documentTypeCodes.add(line.trim());
               }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static void deleteDocumentTypes() throws Exception {

        new ActionExecutorInBatch(searchServices, "The name of the task", BATCH_SIZE) {

            @Override
            public void doActionOnBatch(List<Record> records) {


                //TODO Wrap the records
                List<DocumentType> documentTypes = rm.wrapDocumentTypes(records);

                User adminUser = getAdminUser();
                int index = 0;
                for (DocumentType document : documentTypes) {
                    if(documentTypeCodes.contains(document.getCode())){
                        try {
                            recordServices.logicallyDelete(records.get(index), adminUser);
                        } catch (Exception e) {
                            e.printStackTrace();
                            throw new RuntimeException(e);
                        }
                    }
                    index++;
                }
            }

        }.execute(getDocumentTypesQuery());
    }

    private static User getAdminUser() {
        LogicalSearchCondition condition = LogicalSearchQueryOperators
                .from(rm.userSchema())
                .whereAllConditions(
                        LogicalSearchQueryOperators.where(rm.userUsername()).isNotNull(),
                        LogicalSearchQueryOperators.where(rm.userUsername()).is("admin"));

        LogicalSearchQuery query = new LogicalSearchQuery(condition);
        List<Record> userRecords = searchServices.search(query);
        assertThat(userRecords).isNotEmpty().hasSize(1);

        List<User> users = rm.wrapUsers(userRecords);
        return users.get(0);
    }

    private static void deleteDocumentsDocumentTypeReferences() throws Exception {

        for (MetadataSchema metadataSchema : rm.documentSchemaType().getAllSchemas()) {

            System.out.println("metadataSchema: " + metadataSchema.getCode());

            new ActionExecutorInBatch(searchServices, "The name of the task", BATCH_SIZE) {

                @Override
                public void doActionOnBatch(List<Record> records) {

                    //TODO Wrap the records
                    List<Document> documents = rm.wrapDocuments(records);

                    Transaction transaction = new Transaction();
                    transaction.setSkippingRequiredValuesValidation(true);
                    transaction.setOptimisticLockingResolution(OptimisticLockingResolution.EXCEPTION);

                    for (Document document : documents) {

                        System.out.println(document.getId() + ":" + document.getTitle());

                        //TODO Do the modification on a record
                        Boolean isBorrowed = document.getBorrowed();
                        document.setBorrowed(false);

                        String metadataStringValue = document.get("type");
                        try {
                            System.out.println("value: " + metadataStringValue);
                            document.set("type", null);
                        } catch (Exception e) {
                            LOG.error("Identififiant document: " + document.getId() + "; valeur: " +
                                    metadataStringValue, e);
                            e.printStackTrace();
                        }

                        if(isBorrowed != null) {
                            document.setBorrowed(isBorrowed.booleanValue());
                        }

                        transaction.add(document);
                    }

                    try {
                        recordServices.execute(transaction);
                    } catch (RecordServicesException e) {
                        throw new RuntimeException(e);
                    }
                }

            }.execute(getQuery(metadataSchema));
        }
    }

    private static List<String> documentTypeCodes;

    public static void main(String argv[]) throws Exception {

        if(argv.length < 1) {
            System.out.println("Sample call 'sudo java -Xmx5120m -classpath ./classes:./lib/* com.constellio.dev.DeleteAllDocumentTypesScript <document_types_codes_file>'");
        }

        File file = new File(argv[0]);
        assertThat(file).exists();

        initDocumentTypeCodes(file);

        RecordPopulateServices.LOG_CONTENT_MISSING = false;

        startBackend();

        modelLayerFactory = appLayerFactory.getModelLayerFactory();
        searchServices = modelLayerFactory.newSearchServices();
        recordServices = modelLayerFactory.newRecordServices();

        for (String collection : modelLayerFactory.getCollectionsListManager().getCollections()) {
            currentCollection = collection;
            rm = new RMSchemasRecordsServices(collection, modelLayerFactory);
            runScriptForCurrentCollection();
        }

    }
}
