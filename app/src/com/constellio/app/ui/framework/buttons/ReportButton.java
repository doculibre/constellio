package com.constellio.app.ui.framework.buttons;

import static com.constellio.app.ui.i18n.i18n.$;

import com.constellio.app.ui.framework.components.NewReportPresenter;
import com.constellio.app.ui.framework.components.ReportPresenter;
import com.constellio.app.ui.framework.components.ReportViewer;
import com.constellio.app.ui.framework.reports.NewReportWriterFactory;
import com.constellio.app.ui.framework.reports.ReportWriter;
import com.vaadin.server.ThemeResource;
import com.vaadin.ui.Component;
import com.vaadin.ui.Label;
import com.vaadin.ui.themes.ValoTheme;

public class ReportButton extends WindowButton {
	private final String report;
	//	private final RMReportsPresenter presenter;
	private final ReportPresenter presenter;
	private final NewReportPresenter newPresenter;

	public ReportButton(String report, ReportPresenter presenter) {
		super($(report), $(report), new WindowConfiguration(true, true, "75%", "90%"));
		this.report = report;
		this.presenter = presenter;
		this.newPresenter = null;

		String iconPathKey = report + ".icon";
		String iconPath = $(iconPathKey);
		if (!iconPathKey.equals(iconPath)) {
			setIcon(new ThemeResource(iconPath));
		}
		addStyleName(ValoTheme.BUTTON_ICON_ALIGN_TOP);
		addStyleName(ValoTheme.BUTTON_BORDERLESS);
	}

	public ReportButton(String report, NewReportPresenter presenter) {
		super($(report), $(report), new WindowConfiguration(true, true, "75%", "90%"));
		this.report = report;
		this.presenter = null;
		this.newPresenter = presenter;

		String iconPathKey = report + ".icon";
		String iconPath = $(iconPathKey);
		if (!iconPathKey.equals(iconPath)) {
			setIcon(new ThemeResource(iconPath));
		}
		addStyleName(ValoTheme.BUTTON_ICON_ALIGN_TOP);
		addStyleName(ValoTheme.BUTTON_BORDERLESS);
	}

	@Override
	protected Component buildWindowContent() {
		if (presenter != null) {
			return new ReportViewer(presenter.getReport(report).getReportBuilder(newPresenter.getReportParameters(report)),
					presenter.getReport(report).getFilename(newPresenter.getReportParameters(report)));
		} else {
			NewReportWriterFactory<Object> reportBuilderFactory = (NewReportWriterFactory<Object>) newPresenter
					.getReport(report);

			if (reportBuilderFactory == null) {
				return new Label($("ReportViewer.noReportFactoryAvailable"));
			} else {

				Object parameters = newPresenter.getReportParameters(report);
				String filename = reportBuilderFactory.getFilename(parameters);
				ReportWriter reportWriter = reportBuilderFactory.getReportBuilder(parameters);

				return new ReportViewer(reportWriter, reportBuilderFactory.getFilename(parameters));
			}
		}
	}
}
