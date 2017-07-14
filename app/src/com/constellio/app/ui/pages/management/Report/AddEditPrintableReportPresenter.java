package com.constellio.app.ui.pages.management.Report;

import com.constellio.app.modules.rm.RMConfigs;
import com.constellio.app.modules.rm.services.RMSchemasRecordsServices;
import com.constellio.app.modules.rm.services.borrowingServices.BorrowingServices;
import com.constellio.app.modules.rm.wrappers.PrintableLabel;
import com.constellio.app.modules.rm.wrappers.PrintableReport;
import com.constellio.app.ui.entities.RecordVO;
import com.constellio.app.ui.framework.builders.MetadataSchemaToVOBuilder;
import com.constellio.app.ui.framework.builders.RecordToVOBuilder;
import com.constellio.app.ui.pages.base.BaseView;
import com.constellio.app.ui.pages.base.SchemaPresenterUtils;
import com.constellio.app.ui.pages.base.SingleSchemaBasePresenter;
import com.constellio.model.entities.records.Record;
import com.constellio.model.entities.records.Transaction;
import com.constellio.model.entities.records.wrappers.User;
import com.constellio.model.entities.schemas.MetadataSchema;
import com.constellio.model.entities.schemas.Schemas;
import com.constellio.model.extensions.ModelLayerCollectionExtensions;
import com.constellio.model.services.records.RecordServices;
import com.constellio.model.services.schemas.MetadataSchemasManager;
import com.constellio.model.services.search.query.logical.condition.LogicalSearchCondition;

import java.util.ArrayList;
import java.util.List;

import static com.constellio.model.services.search.query.logical.LogicalSearchQueryOperators.from;

/**
 * Created by Marco on 2017-07-07.
 */
public class AddEditPrintableReportPresenter extends SingleSchemaBasePresenter<AddEditPrintableReportView> {
    private transient MetadataSchemasManager metadataSchemasManager;
    protected RecordVO container;
    private PrintableReportListPossibleView currentType;

    AddEditPrintableReportPresenter(AddEditPrintableReportView view) {
        super(view);
        initTransientObjects();
    }

    private void initTransientObjects() {
        metadataSchemasManager = modelLayerFactory.getMetadataSchemasManager();
    }

    @Override
    protected boolean hasPageAccess(String params, User user) {
        return true;
    }

    public void saveButtonClicked(RecordVO rvo) throws Exception {
        RecordServices rs = appLayerFactory.getModelLayerFactory().newRecordServices();
        SchemaPresenterUtils utils = new SchemaPresenterUtils(PrintableReport.SCHEMA_NAME, view.getConstellioFactories(), view.getSessionContext());
        Record record = utils.toRecord(rvo);
        record.set(metadataSchemasManager.getSchemaTypes(collection).getMetadata(PrintableReport.SCHEMA_NAME + "_" + PrintableReport.ISDELETABLE), true);
        Transaction trans = new Transaction();
        trans.update(record);
        rs.execute(trans);
        view.navigate().to().previousView();
    }

    public void cancelButtonClicked() {
        view.navigate().to().previousView();
    }

    @Override
    protected Record newRecord() {
        super.setSchemaCode(PrintableReport.SCHEMA_NAME);
        return super.newRecord();
    }

    public RecordVO getRecordVO(String id, RecordVO.VIEW_MODE mode) {
        LogicalSearchCondition condition = from(modelLayerFactory.getMetadataSchemasManager().getSchemaTypes(collection).getSchema(PrintableReport.SCHEMA_NAME)).where(Schemas.IDENTIFIER).isEqualTo(id);
        Record r = searchServices().searchSingleResult(condition);
        return new RecordToVOBuilder().build(r, mode, view.getSessionContext());
    }

    public PrintableReportListPossibleView getCurrentType() {
        return currentType;
    }

    public void setCurrentType(PrintableReportListPossibleView type) {
        this.currentType = type;
    }

    public List<MetadataSchema> getSchemasForCurrentType() {
        return currentType != null ? modelLayerFactory.getMetadataSchemasManager().getSchemaTypes(collection).getSchemaType(currentType.toString()).getAllSchemas() : new ArrayList<MetadataSchema>();
    }
}
