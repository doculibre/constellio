package com.constellio.app.modules.rm.migrations;

import com.constellio.app.entities.modules.MigrationHelper;
import com.constellio.app.entities.modules.MigrationResourcesProvider;
import com.constellio.app.entities.modules.MigrationScript;
import com.constellio.app.modules.rm.RMEmailTemplateConstants;
import com.constellio.app.modules.rm.wrappers.ContainerRecord;
import com.constellio.app.modules.rm.wrappers.Document;
import com.constellio.app.modules.rm.wrappers.Folder;
import com.constellio.app.modules.rm.wrappers.PrintableReport;
import com.constellio.app.modules.tasks.model.wrappers.Task;
import com.constellio.app.services.factories.AppLayerFactory;
import com.constellio.app.ui.pages.management.Report.PrintableReportListPossibleType;
import com.constellio.data.dao.managers.config.ConfigManagerException;
import com.constellio.model.entities.records.Record;
import com.constellio.model.entities.records.Transaction;
import com.constellio.model.entities.schemas.Metadata;
import com.constellio.model.entities.schemas.MetadataSchema;
import com.constellio.model.services.search.SearchServices;
import com.constellio.model.services.search.query.logical.LogicalSearchQuery;
import com.constellio.model.services.search.query.logical.LogicalSearchQueryOperators;
import org.apache.chemistry.opencmis.commons.impl.IOUtils;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;

import static com.constellio.model.services.search.query.logical.LogicalSearchQueryOperators.from;

public class RMMigrationTo7_6_11 extends MigrationHelper implements MigrationScript {
    @Override
    public String getVersion() {
        return "7.6.11";
    }

    @Override
    public void migrate(String collection, MigrationResourcesProvider migrationResourcesProvider, AppLayerFactory appLayerFactory) throws Exception {
        SearchServices searchServices = appLayerFactory.getModelLayerFactory().newSearchServices();
        MetadataSchema printableReportSchema = appLayerFactory.getModelLayerFactory().getMetadataSchemasManager().getSchemaTypes(collection).getSchema(PrintableReport.SCHEMA_NAME);
        Metadata recordTypeMetadata = printableReportSchema.getMetadata(PrintableReport.RECORD_TYPE);
        List<Record> reportList = searchServices.search(new LogicalSearchQuery().setCondition(from(printableReportSchema).returnAll()));

        Transaction transaction = new Transaction();
        for(Record report:reportList) {
            transaction.addUpdate(report.set(recordTypeMetadata, getRealRecordType((String) report.get(recordTypeMetadata))));
        }

    }

    private String getRealRecordType(String previousRecordType) {
        if(previousRecordType == null) {
            return null;
        }
        switch (previousRecordType) {
            case "FOLDER":
                return Folder.SCHEMA_TYPE;
            case "DOCUMENT":
                return Document.SCHEMA_TYPE;
            case "CONTAINER":
                return ContainerRecord.SCHEMA_TYPE;
            case "TASK":
                return Task.SCHEMA_TYPE;
            default:
                return previousRecordType;
        }
    }
}
