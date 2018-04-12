package com.constellio.app.modules.rm.ui.pages.folder;

import com.constellio.app.ui.pages.base.BaseViewImpl;
import com.vaadin.navigator.ViewChangeListener;
import com.vaadin.ui.Component;
import com.vaadin.ui.Label;

public class RMFolderSummaryColumnViewImpl extends BaseViewImpl implements RMFolderSummaryColumnView {
    RMFolderSummaryColumnPresenter rmFolderSummaryColumnPresenter;

    @Override
    protected Component buildMainComponent(ViewChangeListener.ViewChangeEvent event) {
        rmFolderSummaryColumnPresenter = new RMFolderSummaryColumnPresenter(this);

        return new Label("lalala");
    }

    @Override
    protected void initBeforeCreateComponents(ViewChangeListener.ViewChangeEvent event) {
        if (event != null) {
            rmFolderSummaryColumnPresenter.forParams(event.getParameters());
        }
    }
}
