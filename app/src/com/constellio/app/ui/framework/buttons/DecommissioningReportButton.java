package com.constellio.app.ui.framework.buttons;

import com.constellio.app.ui.framework.components.NewReportPresenter;
import com.constellio.app.ui.framework.reports.ReportWithCaptionVO;
import com.vaadin.server.ThemeResource;
import com.vaadin.ui.Component;
import com.vaadin.ui.themes.ValoTheme;

import static com.constellio.app.ui.i18n.i18n.$;

public class DecommissioningReportButton extends WindowButton {
    private final String report;
    private final NewReportPresenter newPresenter;
    private Object params;

    public DecommissioningReportButton(ReportWithCaptionVO report, NewReportPresenter presenter) {
        super(report.getCaption(), report.getCaption(), new WindowConfiguration(true, true, "75%", "90%"));
        this.report = report.getTitle();
        this.newPresenter = presenter;

        String iconPathKey = report.getTitle() + ".icon";
        String iconPath = $(iconPathKey);
        if (!iconPathKey.equals(iconPath)) {
            setIcon(new ThemeResource(iconPath));
        }
        addStyleName(ValoTheme.BUTTON_ICON_ALIGN_TOP);
        addStyleName(ValoTheme.BUTTON_BORDERLESS);
    }

    @Override
    protected Component buildWindowContent() {
        return null;
    }
}
