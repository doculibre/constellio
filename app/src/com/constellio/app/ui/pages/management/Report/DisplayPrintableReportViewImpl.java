package com.constellio.app.ui.pages.management.Report;

import com.constellio.app.ui.entities.RecordVO;
import com.constellio.app.ui.framework.components.RecordDisplay;
import com.constellio.app.ui.pages.base.BaseViewImpl;
import com.constellio.app.ui.pages.management.labels.AddEditLabelPresenter;
import com.constellio.app.ui.params.ParamUtils;
import com.vaadin.navigator.ViewChangeListener;
import com.vaadin.ui.Button;
import com.vaadin.ui.Component;
import com.vaadin.ui.VerticalLayout;
import org.apache.commons.lang.StringUtils;

import java.util.Map;

import static com.constellio.app.ui.i18n.i18n.$;

/**
 * Created by Marco on 2017-07-07.
 */
public class DisplayPrintableReportViewImpl extends BaseViewImpl implements DisplayPrintableReportView {
    private DisplayPrintableReportPresenter presenter;
    private RecordVO recordVO;


    @Override
    protected Component buildMainComponent(ViewChangeListener.ViewChangeEvent event) {
        VerticalLayout layout = new VerticalLayout();
        layout.setWidth("100%");
        layout.setSpacing(true);

        layout.addComponent(new RecordDisplay(recordVO));
        return layout;
    }

    @Override
    protected void initBeforeCreateComponents(ViewChangeListener.ViewChangeEvent event) {
        presenter = new DisplayPrintableReportPresenter(this);
        if (StringUtils.isNotEmpty(event.getParameters())) {
            Map<String, String> paramsMap = ParamUtils.getParamsMap(event.getParameters());
            recordVO = presenter.getRecordVO(paramsMap.get("id"), RecordVO.VIEW_MODE.DISPLAY);
        }
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
        return $("LabelDisplayViewImpl.title") + " : " + recordVO.getTitle();
    }
}
