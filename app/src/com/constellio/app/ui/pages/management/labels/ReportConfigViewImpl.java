package com.constellio.app.ui.pages.management.labels;

import com.constellio.app.ui.pages.base.BaseViewImpl;
import com.constellio.app.ui.pages.viewGroups.AdminViewGroup;
import com.constellio.model.entities.CorePermissions;
import com.constellio.model.entities.records.wrappers.User;
import com.vaadin.navigator.ViewChangeListener;
import com.vaadin.ui.Button;
import com.vaadin.ui.Component;
import com.vaadin.ui.CssLayout;

import static com.constellio.app.ui.i18n.i18n.$;

public class ReportConfigViewImpl extends BaseViewImpl implements AdminViewGroup {
	User user;

	@Override
	protected Component buildMainComponent(ViewChangeListener.ViewChangeEvent event) {
		user = getConstellioFactories().getAppLayerFactory().getModelLayerFactory().newUserServices().getUserInCollection(getSessionContext().getCurrentUser().getUsername(), getCollection());
		CssLayout layout = new CustomCssLayout();
		Button manageLabels = newLabelManagementLink();
		Button managePrintableReport = newPrintableReportManagementLink();
		Button manageExcelReport = newExcelReportManagementLink();
		layout.addComponents(manageLabels, manageExcelReport, managePrintableReport);
		return layout;
	}

	private Button newLabelManagementLink() {
		return user.has(CorePermissions.MANAGE_LABELS).globally() ? createLink($("LabelViewImpl.title"), new Button.ClickListener() {

			@Override
			public void buttonClick(Button.ClickEvent event) {
				navigate().to().manageLabels();
			}
		}, "labels") : null;
	}

	private Button newPrintableReportManagementLink() {
		return user.has(CorePermissions.MANAGE_PRINTABLE_REPORT).globally() ? createLink($("PrintableReport.title"), new Button.ClickListener() {

			@Override
			public void buttonClick(Button.ClickEvent event) {
				navigate().to().managePrintableReport();
			}
		}, "report-print") : null;
	}

	private Button newExcelReportManagementLink() {
		return user.has(CorePermissions.MANAGE_EXCEL_REPORT).globally() ? createLink($("ExcelReport.title"), new Button.ClickListener() {
			@Override
			public void buttonClick(Button.ClickEvent event) {
				navigate().to().manageExcelReport();
			}
		}, "excel-templates") : null;
	}

	@Override
	public String getTitle() {
		return $("ReportConfig.title");
	}
}
