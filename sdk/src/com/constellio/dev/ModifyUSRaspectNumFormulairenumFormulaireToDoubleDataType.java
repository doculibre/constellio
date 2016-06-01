package com.constellio.dev;

import com.constellio.app.modules.rm.services.RMSchemasRecordsServices;
import com.constellio.app.modules.rm.wrappers.Document;
import com.constellio.app.services.factories.AppLayerFactory;
import com.constellio.data.dao.dto.records.OptimisticLockingResolution;
import com.constellio.model.entities.records.ActionExecutorInBatch;
import com.constellio.model.entities.records.Record;
import com.constellio.model.entities.records.Transaction;
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
import org.apache.log4j.Logger;

import java.util.List;

import static com.constellio.model.services.search.query.logical.LogicalSearchQueryOperators.from;

public class ModifyUSRaspectNumFormulairenumFormulaireToDoubleDataType {

    private static Logger LOG = Logger.getLogger(ModifyUSRaspectNumFormulairenumFormulaireToDoubleDataType.class);
    static int BATCH_SIZE = 5000;

    static String currentCollection;
    static AppLayerFactory appLayerFactory;
    static ModelLayerFactory modelLayerFactory;
    static SearchServices searchServices;
    static RecordServices recordServices;
    static RMSchemasRecordsServices rm;

    private static void startBackend() {
        //TODO

        //Only enable this line to run in production
        //appLayerFactory = startLayerFactoriesWithoutBackgroundThreads();

        //Only enable this line to run on developer workstation
        appLayerFactory = SDKScriptUtils.startApplicationWithoutBackgroundProcessesAndAuthentication();

    }

    private static LogicalSearchQuery getQuery(MetadataSchema folderSchema) {
        //TODO Build a query to find records to modify or to return all records
        //return new LogicalSearchQuery(from(rm.folderSchemaType())
        //        .where(rm.containerAdministrativeUnit()).isEqualTo("42"));

        final Metadata aspectNumFormulairenumFormulaire = folderSchema.getMetadata("USRaspectNumFormulairenumFormulaire");

        LogicalSearchCondition condition = LogicalSearchQueryOperators.from(folderSchema).whereAllConditions(
                LogicalSearchQueryOperators.where(aspectNumFormulairenumFormulaire).isNotNull(),
                LogicalSearchQueryOperators.where(aspectNumFormulairenumFormulaire).isNotEqual("__NULL__"));

        return new LogicalSearchQuery(from(rm.documentSchemaType()).where(condition));

    }

    private static void runScriptForCurrentCollection() throws Exception {

        int counter = 0;
        for (MetadataSchema folderSchema : rm.documentSchemaType().getAllSchemas()) {
            if(folderSchema.hasMetadataWithCode("USRaspectNumFormulairenumFormulaire") &&
                    folderSchema.getMetadata("USRaspectNumFormulairenumFormulaire").isEnabled()) {
                System.out.println(counter + ": " + folderSchema.getCode());

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
                            //document.setBorrowed(false);

                            String metadataStringValue = document.get("USRaspectNumFormulairenumFormulaire");
                            try {
                                System.out.println("value: " + metadataStringValue);
                                double doubleValue = Double.valueOf(metadataStringValue);
                                document.set("USRaspectNumFormulairenumFormulaireCopie", doubleValue);
                            } catch (Exception e) {
                                LOG.error("Identififiant document: " + document.getId() + "; valeur: " +
                                        metadataStringValue, e);
                                e.printStackTrace();
                            }

                            transaction.add(document);
                        }

                        try {
                            recordServices.execute(transaction);
                        } catch (RecordServicesException e) {
                            throw new RuntimeException(e);
                        }
                    }

                }.execute(getQuery(folderSchema));

                counter++;
            }
        }
    }

    public static void main(String argv[]) throws Exception {

        RecordPopulateServices.LOG_CONTENT_MISSING = false;

        startBackend();

        modelLayerFactory = appLayerFactory.getModelLayerFactory();
        searchServices = modelLayerFactory.newSearchServices();
        recordServices = modelLayerFactory.newRecordServices();

        for (String collection : modelLayerFactory.getCollectionsListManager().getCollections()) {
            currentCollection = collection;
            rm = new RMSchemasRecordsServices(collection, appLayerFactory);
            runScriptForCurrentCollection();
        }

    }
}
