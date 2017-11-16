package com.constellio.app.ui.pages.SIP;

import com.constellio.app.ui.pages.progress.BaseProgressView;
import com.vaadin.navigator.ViewChangeListener;

public class SIPProgressionViewImpl extends BaseProgressView {
    private SIPProgressionViewPresenter presenter;
    @Override
    protected void initBeforeCreateComponents(ViewChangeListener.ViewChangeEvent event) {
        presenter = new SIPProgressionViewPresenter(this);
    }
}
