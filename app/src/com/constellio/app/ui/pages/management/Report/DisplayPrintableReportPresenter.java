package com.constellio.app.ui.pages.management.Report;

import com.constellio.app.modules.rm.wrappers.PrintableLabel;
import com.constellio.app.modules.rm.wrappers.PrintableReport;
import com.constellio.app.ui.entities.RecordVO;
import com.constellio.app.ui.framework.builders.RecordToVOBuilder;
import com.constellio.app.ui.pages.base.SingleSchemaBasePresenter;
import com.constellio.model.entities.records.Record;
import com.constellio.model.entities.records.wrappers.User;
import com.constellio.model.entities.schemas.Schemas;
import com.constellio.model.services.search.query.logical.condition.LogicalSearchCondition;

import static com.constellio.model.services.search.query.logical.LogicalSearchQueryOperators.from;

/**
 * Created by Marco on 2017-07-07.
 */
public class DisplayPrintableReportPresenter extends SingleSchemaBasePresenter<DisplayPrintableReportView> {

    public DisplayPrintableReportPresenter(DisplayPrintableReportView view) {
        super(view);
    }

    public RecordVO getRecordVO(String id, RecordVO.VIEW_MODE mode) {
        LogicalSearchCondition condition = from(modelLayerFactory.getMetadataSchemasManager().getSchemaTypes(collection).getSchema(PrintableReport.SCHEMA_NAME)).where(Schemas.IDENTIFIER).isEqualTo(id);
        Record r = searchServices().searchSingleResult(condition);
        RecordToVOBuilder voBuilder = new RecordToVOBuilder();
        return voBuilder.build(r, mode, view.getSessionContext());
    }

    @Override
    protected boolean hasPageAccess(String params, User user) {
        return true;
    }

    protected void backButtonClicked() {
        view.navigate().to().previousView();
    }
}
