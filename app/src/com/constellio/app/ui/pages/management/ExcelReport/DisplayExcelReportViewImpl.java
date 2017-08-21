package com.constellio.app.ui.pages.management.ExcelReport;

import com.constellio.app.ui.framework.components.RecordDisplay;
import com.constellio.app.ui.pages.base.BaseViewImpl;
import com.constellio.app.ui.params.ParamUtils;
import com.vaadin.navigator.ViewChangeListener;
import com.vaadin.ui.Component;

public class DisplayExcelReportViewImpl extends BaseViewImpl implements DisplayExcelReportView {
    private DisplayExcelReportPresenter presenter;

    @Override
    protected Component buildMainComponent(ViewChangeListener.ViewChangeEvent event) {
        presenter = new DisplayExcelReportPresenter(this);
        presenter.setParametersMap(ParamUtils.getParamsMap(event.getParameters()));
        return new RecordDisplay(presenter.getReport());
    }
}
