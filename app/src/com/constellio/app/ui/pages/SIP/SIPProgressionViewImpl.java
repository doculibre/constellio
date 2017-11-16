package com.constellio.app.ui.pages.SIP;

import com.constellio.app.entities.modules.ProgressInfo;
import com.constellio.app.ui.framework.buttons.SIPButton.SIPBuildAsyncTask;
import com.constellio.app.ui.pages.progress.BaseProgressView;
import com.constellio.app.ui.params.ParamUtils;
import com.constellio.model.entities.batchprocess.AsyncTask;
import com.constellio.model.entities.batchprocess.AsyncTaskBatchProcess;
import com.vaadin.navigator.ViewChangeListener;
import com.vaadin.ui.Component;

import java.util.Map;

public class SIPProgressionViewImpl extends BaseProgressView {
    private SIPProgressionViewPresenter presenter;
    AsyncTaskBatchProcess task;
    @Override
    protected void initBeforeCreateComponents(ViewChangeListener.ViewChangeEvent event) {
        presenter = new SIPProgressionViewPresenter(this);
        if(!event.getParameters().isEmpty()) {
            Map<String, String> paramsMap = ParamUtils.getParamsMap(event.getParameters());
            presenter.setTask(paramsMap.get("id"));
        }
    }
}
