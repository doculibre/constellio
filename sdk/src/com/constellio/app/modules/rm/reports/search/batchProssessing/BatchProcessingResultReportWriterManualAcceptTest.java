package com.constellio.app.modules.rm.reports.search.batchProssessing;

import com.constellio.app.modules.rm.RMTestRecords;
import com.constellio.app.modules.rm.reports.builders.BatchProssessing.BatchProcessingResultModel;
import com.constellio.app.modules.rm.reports.builders.BatchProssessing.BatchProcessingResultReportWriter;
import com.constellio.app.modules.rm.wrappers.ContainerRecord;
import com.constellio.app.modules.rm.wrappers.Document;
import com.constellio.app.modules.rm.wrappers.Folder;
import com.constellio.app.modules.tasks.model.wrappers.Task;
import com.constellio.app.reports.builders.administration.plan.ReportBuilderTestFramework;
import com.constellio.app.ui.pages.search.batchProcessing.entities.BatchProcessPossibleImpact;
import com.constellio.app.ui.pages.search.batchProcessing.entities.BatchProcessRecordFieldModification;
import com.constellio.app.ui.pages.search.batchProcessing.entities.BatchProcessRecordModifications;
import com.constellio.app.ui.pages.search.batchProcessing.entities.BatchProcessResults;
import com.constellio.model.entities.Language;
import com.constellio.model.entities.schemas.Metadata;
import com.constellio.model.entities.schemas.MetadataSchema;
import com.constellio.model.entities.schemas.MetadataSchemaType;
import com.constellio.model.entities.schemas.MetadataSchemaTypes;
import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class BatchProcessingResultReportWriterManualAcceptTest extends ReportBuilderTestFramework {
    BatchProcessingResultModel model;
    RMTestRecords records = new RMTestRecords(zeCollection);

    @Before
    public void setUp()
            throws Exception {
        prepareSystem(
                withZeCollection().withConstellioRMModule().withAllTestUsers().withRMTest(records)
                        .withFoldersAndContainersOfEveryStatus()
        );
    }

    @Test
    public void whenBuildEmptyReportThenOk() {
        model = new BatchProcessingResultModel(new BatchProcessResults(new ArrayList<BatchProcessRecordModifications>()), Language.French);
        build(new BatchProcessingResultReportWriter(model, new Locale("fr")));
    }

    @Test
    public void whenBuildReportWithResultsThenOk() {
        model = configModel();
        build(new BatchProcessingResultReportWriter(model, new Locale("fr")));
    }

    private BatchProcessingResultModel configModel() {
        ArrayList<BatchProcessRecordModifications> modif = new ArrayList<BatchProcessRecordModifications>();
        List<BatchProcessPossibleImpact> impacts1 = new ArrayList<>();
        MetadataSchemaTypes types = getModelLayerFactory().getMetadataSchemasManager().getSchemaTypes(zeCollection);
        MetadataSchemaType schema11 = types.getSchemaType(Folder.SCHEMA_TYPE);
        MetadataSchemaType schema12 = types.getSchemaType(Document.SCHEMA_TYPE);
        impacts1.add(new BatchProcessPossibleImpact(11, schema11));
        impacts1.add(new BatchProcessPossibleImpact(12, schema12));
        List<BatchProcessRecordFieldModification> fieldsModifs1 = new ArrayList<>();
        MetadataSchema folderSchema = schema11.getDefaultSchema();
        Metadata metadata1 = folderSchema.getMetadata(Folder.CATEGORY);
        Metadata metadata2 = folderSchema.getMetadata(Folder.FILING_SPACE);
        Metadata metadata3 = folderSchema.getMetadata(Folder.PARENT_FOLDER);
        fieldsModifs1.add(0, new BatchProcessRecordFieldModification("lol11", "lol11After", metadata1));
        fieldsModifs1.add(1, new BatchProcessRecordFieldModification("lol12", "lol12After", metadata2));
        fieldsModifs1.add(2, new BatchProcessRecordFieldModification("lol13", "lol13After", metadata3));
        BatchProcessRecordModifications modif1 = new BatchProcessRecordModifications("id1", "title1", impacts1, fieldsModifs1);
        modif.add(0, modif1);
        List<BatchProcessPossibleImpact> impacts2 = new ArrayList<>();
        MetadataSchemaType schema21 = types.getSchemaType(ContainerRecord.SCHEMA_TYPE);
        MetadataSchemaType schema22 = types.getSchemaType(Task.SCHEMA_TYPE);
        impacts2.add(new BatchProcessPossibleImpact(21, schema21));
        impacts2.add(new BatchProcessPossibleImpact(22, schema22));
        List<BatchProcessRecordFieldModification> fieldsModifs2 = new ArrayList<>();
        fieldsModifs2.add(0, new BatchProcessRecordFieldModification("lol21", "lol21After", metadata1));
        fieldsModifs2.add(1, new BatchProcessRecordFieldModification("lol22", "lol22After", metadata2));
        fieldsModifs2.add(2, new BatchProcessRecordFieldModification("lol23", "lol23After", metadata3));
        BatchProcessRecordModifications modif2 = new BatchProcessRecordModifications("id2", "title2", impacts2, fieldsModifs2);
        modif.add(1, modif2);

        return new BatchProcessingResultModel(new BatchProcessResults(modif), Language.French);
    }
}

