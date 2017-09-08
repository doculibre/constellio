package com.constellio.app.ui.framework.buttons.report;

import com.constellio.app.ui.framework.buttons.WindowButton;
import com.vaadin.ui.Component;
import com.vaadin.ui.Panel;

public class ReportConfigurationButton extends WindowButton {
    private final String caption;
    private final String collection;
    protected ReportConfigurationPresenter presenter;

    public ReportConfigurationButton(String caption, String windowCaption, ReportConfigurationPresenter presenter, String collection) {
        super(caption, windowCaption, WindowConfiguration.modalDialog("75%", "75%"));
        this.caption = caption;
        this.presenter = presenter;
        this.collection = collection;
    }

    @Override
    protected Component buildWindowContent() {
        Panel reportConfigPanel = new ReportConfigurationPanel(caption, this.presenter, collection);
        return reportConfigPanel;
    }

}