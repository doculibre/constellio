package com.constellio.app.ui.pages.management.labels;

import com.constellio.app.modules.rm.ConstellioRMModule;
import com.constellio.app.services.factories.AppLayerFactory;
import com.constellio.app.ui.pages.base.BaseView;
import com.constellio.app.ui.pages.base.BaseViewImpl;
import com.constellio.app.ui.pages.viewGroups.AdminViewGroup;
import com.constellio.model.entities.CorePermissions;
import com.constellio.model.entities.records.wrappers.User;
import com.constellio.model.services.extensions.ConstellioModulesManager;
import com.vaadin.navigator.ViewChangeListener;
import com.vaadin.ui.Button;
import com.vaadin.ui.Button.ClickListener;
import com.vaadin.ui.Component;
import com.vaadin.ui.CssLayout;

import static com.constellio.app.ui.i18n.i18n.$;

// TODO DECIDE IF THE LABELS AND PRINTABLE REPORTS GOES IN RM OR CORE AND SPLIT IT CORRECTLY (PERMISSIONS, VIEWS, WRAPPERS)
public class ReportConfigViewImpl extends BaseViewImpl implements AdminViewGroup {
	User user;
	ConstellioModulesManager modulesManager;
	String collection;

	@Override
	protected Component buildMainComponent(ViewChangeListener.ViewChangeEvent event) {
		AppLayerFactory appLayerFactory = getConstellioFactories().getAppLayerFactory();
		modulesManager = appLayerFactory.getModulesManager();
		collection = ((BaseView) event.getNewView()).getCollection();
		user = appLayerFactory.getModelLayerFactory().newUserServices().getUserInCollection(getSessionContext().getCurrentUser().getUsername(), getCollection());
		CssLayout layout = new CustomCssLayout();
		Button manageLabels = newLabelManagementLink();
		Button managePrintableReport = newPrintableReportManagementLink();
		Button manageExcelReport = newExcelReportManagementLink();
		layout.addComponents(manageLabels, manageExcelReport, managePrintableReport);
		return layout;
	}

	private Button newLabelManagementLink() {
		return modulesManager.isModuleEnabled(collection, ConstellioRMModule.ID) && user.has(CorePermissions.MANAGE_LABELS).globally() ?
			   createLink($("LabelViewImpl.title"), (ClickListener) event -> {
				   navigate().to().manageLabels();
			   }, "labels") : null;
	}

	private Button newPrintableReportManagementLink() {
		return modulesManager.isModuleEnabled(collection, ConstellioRMModule.ID) && user.has(CorePermissions.MANAGE_PRINTABLE_REPORT).globally() ?
			   createLink($("PrintableReport.title"), (ClickListener) event -> {
				   navigate().to().managePrintableReport();
			   }, "report-print") : null;
	}

	private Button newExcelReportManagementLink() {
		return user.has(CorePermissions.MANAGE_EXCEL_REPORT).globally() ?
			   createLink($("ExcelReport.title"), (ClickListener) event -> {
				   navigate().to().manageExcelReport();
			   }, "excel-templates") : null;
	}

	@Override
	public String getTitle() {
		return $("ReportConfig.title");
	}
}
