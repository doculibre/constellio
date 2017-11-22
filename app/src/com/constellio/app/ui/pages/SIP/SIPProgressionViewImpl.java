package com.constellio.app.ui.pages.SIP;

import com.constellio.app.ui.pages.base.BaseViewImpl;
import com.constellio.app.ui.pages.progress.BaseProgressView;
import com.constellio.app.ui.params.ParamUtils;
import com.constellio.model.entities.batchprocess.AsyncTaskBatchProcess;
import com.vaadin.navigator.ViewChangeListener;
import com.vaadin.ui.Component;

import java.util.Map;

public class SIPProgressionViewImpl extends BaseViewImpl {
    private SIPProgressionViewPresenter presenter;
    AsyncTaskBatchProcess task;

    @Override
    protected void initBeforeCreateComponents(ViewChangeListener.ViewChangeEvent event) {
        if(!event.getParameters().isEmpty()) {
            Map<String, String> paramsMap = ParamUtils.getParamsMap(event.getParameters());
            presenter = new SIPProgressionViewPresenter(this, paramsMap.get("id"));
        }
    }

    @Override
    protected Component buildMainComponent(ViewChangeListener.ViewChangeEvent event) {
        return null;
    }
}
