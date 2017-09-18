package com.constellio.app.ui.pages.management.ExcelReport;

import com.constellio.app.ui.entities.RecordVO;
import com.constellio.app.ui.entities.ReportVO;
import com.constellio.app.ui.framework.builders.RecordToVOBuilder;
import com.constellio.app.ui.framework.builders.ReportToVOBuilder;
import com.constellio.app.ui.pages.base.BasePresenter;
import com.constellio.model.entities.CorePermissions;
import com.constellio.model.entities.records.wrappers.User;
import com.constellio.model.services.reports.ReportServices;

import java.util.Map;

public class DisplayExcelReportPresenter extends BasePresenter<DisplayExcelReportView> {
    private Map<String, String> parametersMap;

    public DisplayExcelReportPresenter(DisplayExcelReportView view) {
        super(view);
    }

    @Override
    protected boolean hasPageAccess(String params, User user) {
        return user.has(CorePermissions.MANAGE_EXCEL_REPORT).globally();
    }

    public RecordVO getReport() {
        RecordVO reportVO = null;
        if(this.parametersMap != null && this.parametersMap.containsKey("id")) {
            ReportServices reportServices = new ReportServices(modelLayerFactory, collection);
            String id = this.parametersMap.get("id");
            RecordToVOBuilder builder = new RecordToVOBuilder();
            reportVO = builder.build(reportServices.getRecordById(id), RecordVO.VIEW_MODE.DISPLAY, view.getSessionContext());
        }
        return reportVO;
    }

    public void setParametersMap(Map<String, String> map){
        this.parametersMap = map;
    }

    protected void backButtonClicked() {
        view.navigate().to().previousView();
    }
}
