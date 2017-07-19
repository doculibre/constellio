package com.constellio.app.ui.pages.management.Report;

import com.constellio.app.modules.rm.wrappers.Document;
import com.constellio.app.modules.rm.wrappers.Folder;
import com.constellio.app.modules.rm.wrappers.PrintableLabel;
import com.constellio.app.modules.rm.wrappers.PrintableReport;
import com.constellio.app.modules.tasks.model.wrappers.Task;
import com.constellio.app.ui.entities.MetadataSchemaVO;
import com.constellio.app.ui.entities.RecordVO;
import com.constellio.app.ui.framework.builders.MetadataSchemaToVOBuilder;
import com.constellio.app.ui.framework.builders.RecordToVOBuilder;
import com.constellio.app.ui.framework.data.RecordVODataProvider;
import com.constellio.app.ui.pages.base.SchemaPresenterUtils;
import com.constellio.app.ui.pages.base.SingleSchemaBasePresenter;
import com.constellio.model.conf.FoldersLocator;
import com.constellio.model.entities.CorePermissions;
import com.constellio.model.entities.records.Record;
import com.constellio.model.entities.records.wrappers.User;
import com.constellio.model.entities.schemas.MetadataSchema;
import com.constellio.model.services.search.query.logical.LogicalSearchQuery;
import com.vaadin.server.StreamResource;
import org.apache.commons.io.FileUtils;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

import static com.constellio.model.services.search.query.logical.LogicalSearchQueryOperators.from;

public class ListPrintableReportPresenter extends SingleSchemaBasePresenter<ListPrintableReportView> {
    private  Map<PrintableReportListPossibleType, String>  POSSIBLE_SCHEMA_TYPE = new HashMap<PrintableReportListPossibleType, String>(){{
        put(PrintableReportListPossibleType.FOLDER, Folder.SCHEMA_TYPE);
        put(PrintableReportListPossibleType.DOCUMENT, Document.SCHEMA_TYPE);
        put(PrintableReportListPossibleType.TASK, Task.SCHEMA_TYPE);
    }};

    private MetadataSchemaToVOBuilder schemaVOBuilder;
    private ListPrintableReportView view;

    ListPrintableReportPresenter(ListPrintableReportView view) {
        super(view);
        this.view = view;
        initTransientObjects();
    }

    private void initTransientObjects() {
        schemaVOBuilder = new MetadataSchemaToVOBuilder();
    }

    private RecordVODataProvider getDataProviderForSchemaType(final String schemaType){
        final MetadataSchemaVO printableReportVO =  schemaVOBuilder.build(
                modelLayerFactory.getMetadataSchemasManager().getSchemaTypes(collection).getSchema(PrintableReport.DEFAULT_SCHEMA),
                RecordVO.VIEW_MODE.TABLE,
                view.getSessionContext());
        return new RecordVODataProvider(printableReportVO, new RecordToVOBuilder(), modelLayerFactory, view.getSessionContext()) {
            @Override
            protected LogicalSearchQuery getQuery() {
                MetadataSchema metadataSchema = modelLayerFactory.getMetadataSchemasManager().getSchemaTypes(collection).getSchema(PrintableReport.SCHEMA_NAME);
                return schemaType == null ? null : new LogicalSearchQuery(from(metadataSchema).where(metadataSchema.getMetadata(PrintableReport.RECORD_TYPE)).isEqualTo(schemaType.toUpperCase()));
            }
        };
    }

    protected RecordVODataProvider getPrintableReportFolderDataProvider() {
        return getDataProviderForSchemaType(PrintableReportListPossibleType.FOLDER.toString());
    }

    protected RecordVODataProvider getPrintableReportDocumentDataProvider() {
        return getDataProviderForSchemaType(PrintableReportListPossibleType.DOCUMENT.toString());
    }

    protected RecordVODataProvider getPrintableReportTaskDataProvider() {
        return getDataProviderForSchemaType(PrintableReportListPossibleType.TASK.toString());
    }

    @Override
    protected boolean hasPageAccess(String params, User user) {
        return user.has(CorePermissions.MANAGE_PRINTABLE_REPORT).globally();
    }

    protected void addLabelButtonClicked() {
        view.navigate().to().addPrintableReport();
    }

    protected void editButtonClicked(String id, PrintableReportListPossibleType schema) {
        view.navigate().to().editPrintableReport(id);
    }

    protected void displayButtonClicked(String id, PrintableReportListPossibleType schema) {
        view.navigate().to().displayPrintableReport(id);
    }

    protected void removeRecord(String id, PrintableReportListPossibleType schema) {
        SchemaPresenterUtils utils = new SchemaPresenterUtils(PrintableLabel.SCHEMA_NAME, view.getConstellioFactories(), view.getSessionContext());
        Record record = utils.toRecord(getRecordsWithIndex(schema, id));
        delete(record);
        view.navigate().to().managePrintableReport();
    }

    public RecordVO getRecordsWithIndex(PrintableReportListPossibleType schema, String itemId) {
        RecordVODataProvider dataProvider = this.getDataProviderForSchemaType(schema.toString());
        return itemId == null ?  null : dataProvider.getRecordVO(Integer.parseInt(itemId));
    }

    protected StreamResource createResource() {
        return new StreamResource(new StreamResource.StreamSource() {
            @Override
            public InputStream getStream() {
                InputStream stream = null;
                try {
                    File file = new File(new FoldersLocator().getModuleResourcesFolder("rm"), "Template_PrintableReport.zip");
                    stream = new ByteArrayInputStream(FileUtils.readFileToByteArray(file));
                } catch (IOException e) {
                    e.printStackTrace();
                }
                return stream;
            }
        }, "templates.zip");
    }
}
