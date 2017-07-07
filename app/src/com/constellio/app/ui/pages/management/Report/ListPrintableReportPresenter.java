package com.constellio.app.ui.pages.management.Report;

import com.constellio.app.modules.rm.RMConfigs;
import com.constellio.app.modules.rm.services.RMSchemasRecordsServices;
import com.constellio.app.modules.rm.services.borrowingServices.BorrowingServices;
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
import com.constellio.model.entities.records.Record;
import com.constellio.model.entities.records.wrappers.User;
import com.constellio.model.entities.schemas.MetadataSchema;
import com.constellio.model.extensions.ModelLayerCollectionExtensions;
import com.constellio.model.services.records.RecordServices;
import com.constellio.model.services.schemas.MetadataSchemasManager;
import com.constellio.model.services.search.query.logical.LogicalSearchQuery;
import com.vaadin.server.StreamResource;
import net.didion.jwnl.data.POS;
import org.apache.commons.io.FileUtils;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

import static com.constellio.model.services.search.query.logical.LogicalSearchQueryOperators.from;

/**
 * Created by Marco on 2017-07-07.
 */
public class ListPrintableReportPresenter extends SingleSchemaBasePresenter<ListPrintableReportView> {
    private  Map<PrintableReportListPossibleView, String>  POSSIBLE_SCHEMA_TYPE = new HashMap<PrintableReportListPossibleView, String>(){{
        put(PrintableReportListPossibleView.FOLDER, Folder.SCHEMA_TYPE);
        put(PrintableReportListPossibleView.DOCUMENT, Document.SCHEMA_TYPE);
        put(PrintableReportListPossibleView.TASK, Task.SCHEMA_TYPE);
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
                return new LogicalSearchQuery(from(metadataSchema).where(metadataSchema.getMetadata(PrintableReport.REPORT_TYPE)).isEqualTo(schemaType));
            }
        };
    }

    protected RecordVODataProvider getPrintableReportFolderDataProvider() {
        return getDataProviderForSchemaType(Folder.SCHEMA_TYPE);
    }

    protected RecordVODataProvider getPrintableReportDocumentDataProvider() {
        return getDataProviderForSchemaType(Document.SCHEMA_TYPE);
    }

    protected RecordVODataProvider getPrintableReportTaskDataProvider() {
        return getDataProviderForSchemaType(Task.SCHEMA_TYPE);
    }

    @Override
    protected boolean hasPageAccess(String params, User user) {
        return true;
    }

    protected void addLabelButtonClicked() {
        view.navigate().to().addPrintableReport();
    }

    protected void editButtonClicked(String id, PrintableReportListPossibleView schema) {
        view.navigate().to().editPrintableReport(id);
    }

    protected void displayButtonClicked(String id, PrintableReportListPossibleView schema) {
        view.navigate().to().displayPrintableReport(id);
    }

    protected void removeRecord(String id, PrintableReportListPossibleView schema) {
        SchemaPresenterUtils utils = new SchemaPresenterUtils(PrintableLabel.SCHEMA_NAME, view.getConstellioFactories(), view.getSessionContext());
        Record record = utils.toRecord(getRecordsWithIndex(schema, id));
        delete(record);
        view.navigate().to().managePrintableReport();
    }

    public RecordVO getRecordsWithIndex(PrintableReportListPossibleView schema, String itemId) {
        RecordVODataProvider dataProvider = this.getDataProviderForSchemaType(POSSIBLE_SCHEMA_TYPE.get(schema));
        return dataProvider.getRecordVO(Integer.parseInt(itemId));
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
