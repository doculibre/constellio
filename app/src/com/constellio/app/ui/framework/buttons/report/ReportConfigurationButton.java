package com.constellio.app.ui.framework.buttons.report;

import com.constellio.app.ui.framework.buttons.LabelsButton.RecordSelector;
import com.constellio.app.ui.framework.buttons.WindowButton;
import com.vaadin.ui.Component;
import com.vaadin.ui.Panel;

public class ReportConfigurationButton extends WindowButton {
    private final RecordSelector selector;
    private final String caption;
    private final String collection;
    protected ReportConfigurationPresenter presenter;

    public ReportConfigurationButton(String caption, String windowCaption, RecordSelector recordSelector, ReportConfigurationPresenter presenter, String collection) {
        super(caption, windowCaption, WindowConfiguration.modalDialog("75%", "75%"));
        this.caption = caption;
        this.selector = recordSelector;
        this.presenter = presenter;
        this.collection = collection;
    }

    @Override
    protected Component buildWindowContent() {
        Panel reportConfigPanel = new ReportConfigurationPanel(caption, this.presenter, collection);
        return reportConfigPanel;
    }

}