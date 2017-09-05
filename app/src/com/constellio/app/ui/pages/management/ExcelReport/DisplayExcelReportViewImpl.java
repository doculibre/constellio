package com.constellio.app.ui.pages.management.ExcelReport;

import com.constellio.app.ui.framework.components.RecordDisplay;
import com.constellio.app.ui.pages.base.BaseViewImpl;
import com.constellio.app.ui.pages.management.Report.DisplayPrintableReportPresenter;
import com.constellio.app.ui.params.ParamUtils;
import com.vaadin.navigator.ViewChangeListener;
import com.vaadin.ui.Button;
import com.vaadin.ui.Component;
import org.apache.commons.lang.StringUtils;

import java.util.Map;

import static com.constellio.app.ui.i18n.i18n.$;

public class DisplayExcelReportViewImpl extends BaseViewImpl implements DisplayExcelReportView {
    private DisplayExcelReportPresenter presenter;

    @Override
    protected Component buildMainComponent(ViewChangeListener.ViewChangeEvent event) {
        presenter = new DisplayExcelReportPresenter(this);
        presenter.setParametersMap(ParamUtils.getParamsMap(event.getParameters()));
        return new RecordDisplay(presenter.getReport());
    }

    @Override
    protected void initBeforeCreateComponents(ViewChangeListener.ViewChangeEvent event) {
        presenter = new DisplayExcelReportPresenter(this);
        presenter.setParametersMap(ParamUtils.getParamsMap(event.getParameters()));
    }

    @Override
    protected Button.ClickListener getBackButtonClickListener() {
        return new Button.ClickListener() {
            @Override
            public void buttonClick(Button.ClickEvent event) {
                presenter.backButtonClicked();
            }
        };
    }

    @Override
    protected String getTitle() {
        return $("DisplayExcelReport.title") + " : " + presenter.getReport().getTitle();
    }
}
