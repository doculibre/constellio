package com.constellio.app.ui.framework.buttons;

import com.constellio.app.modules.rm.reports.builders.administration.plan.ConservationRulesReportParameters;
import com.constellio.app.modules.rm.ui.pages.decommissioning.DecommissioningListPresenter;
import com.constellio.app.modules.rm.ui.pages.reports.RMNewReportsPresenter;
import com.constellio.app.ui.framework.components.NewReportPresenter;
import com.constellio.app.ui.framework.components.ReportSelector;
import com.constellio.app.ui.framework.components.ReportViewer;
import com.constellio.app.ui.framework.reports.NewReportWriterFactory;
import com.constellio.app.ui.framework.reports.ReportWriter;
import com.vaadin.server.ThemeResource;
import com.vaadin.ui.Component;
import com.vaadin.ui.Label;
import com.vaadin.ui.TabSheet;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.themes.ValoTheme;

import java.util.List;

import static com.constellio.app.ui.i18n.i18n.$;

public class DecommissioningListReportButton extends WindowButton {
	private List<String> reports;
	private final NewReportPresenter newPresenter;
	private Object params;

	public DecommissioningListReportButton(DecommissioningListPresenter presenter) {
		super($("DecommissioningReportButton.DecommissioningList"), $("DecommissioningReportButton.DecommissioningList"),
				new WindowConfiguration(true, true, "75%", "90%"));

		this.reports = presenter.getReports();

		this.newPresenter = presenter;

		String iconPathKey = "Reports.DecommissioningList.icon";
		String iconPath = $(iconPathKey);
		if (!iconPathKey.equals(iconPath)) {
			setIcon(new ThemeResource(iconPath));
		}
		addStyleName(ValoTheme.BUTTON_ICON_ALIGN_TOP);
		addStyleName(ValoTheme.BUTTON_BORDERLESS);
	}

	@Override
	protected Component buildWindowContent() {
		VerticalLayout verticalLayout = new VerticalLayout();
		verticalLayout.setHeight("100%");
		verticalLayout.setWidth("100%");
		verticalLayout.addStyleName("no-scroll");

		TabSheet tabSheet = new TabSheet();
		tabSheet.setHeight("100%");
		tabSheet.setWidth("100%");

		for (String report : reports) {
			if (report.equals("Reports.DecommissioningList")) {
				tabSheet.addTab(createPDFTab(report), $("ReportTabButton.PDFReport"));
			} else if (report.equals("Reports.DecommissioningListExcelFormat")) {
				tabSheet.addTab(createExcelTab(), $("ReportTabButton.ExcelReport"));
			} else {
				throw new RuntimeException("BUG: Unknown report: " + report);
			}
		}

		verticalLayout.addComponent(tabSheet);
		verticalLayout.setExpandRatio(tabSheet, 1);

		return verticalLayout;
	}

	private Component createPDFTab(String report) {
		NewReportWriterFactory<Object> reportBuilderFactory = (NewReportWriterFactory<Object>) newPresenter
				.getReport(report);

		if (reportBuilderFactory == null) {
			return new Label($("ReportViewer.noReportFactoryAvailable"));
		} else {
			Object parameters = newPresenter.getReportParameters(report);

			if (parameters instanceof ConservationRulesReportParameters && newPresenter instanceof RMNewReportsPresenter) {
				((ConservationRulesReportParameters) parameters).setAdministrativeUnit(((RMNewReportsPresenter) newPresenter).getSchemaTypeValue());
			}

			ReportWriter reportWriter = reportBuilderFactory.getReportBuilder(parameters);

			return new ReportViewer(reportWriter, reportBuilderFactory.getFilename(parameters));
		}
	}

	private Component createExcelTab() {
		VerticalLayout verticalLayout = new VerticalLayout();

		if (newPresenter.getSupportedReports().isEmpty()) {
			verticalLayout.addComponent(createErrorTab());
		} else {
			Label label = new Label($("ReportTabButton.selectTemplate"));
			label.addStyleName(ValoTheme.LABEL_BOLD);
			verticalLayout.addComponent(label);
			verticalLayout.addComponent(new ReportSelector(newPresenter, false));
		}

		return verticalLayout;
	}

	private VerticalLayout createErrorTab() {
		VerticalLayout verticalLayout = new VerticalLayout();

		Label label = new Label($("ReportTabButton.noReportTemplateForCondition"));

		verticalLayout.addComponent(label);

		return verticalLayout;
	}

	public Object getParams() {
		return params;
	}

	public void setParams(Object params) {
		this.params = params;
	}
}
