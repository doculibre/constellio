package com.constellio.app.modules.rm.ui.pages.reports;

import java.util.Arrays;
import java.util.List;

import com.constellio.app.modules.rm.navigation.RMViews;
import com.constellio.app.modules.rm.constants.RMPermissionsTo;
import com.constellio.app.modules.rm.reports.builders.administration.plan.AdministrativeUnitReportViewImpl;
import com.constellio.app.modules.rm.reports.builders.administration.plan.ClassificationPlanReportViewImpl;
import com.constellio.app.modules.rm.reports.builders.administration.plan.ConservationRulesReportViewImpl;
import com.constellio.app.modules.rm.reports.builders.administration.plan.UserReportViewImpl;
import com.constellio.app.modules.rm.reports.factories.ExampleReportFactoryWithoutRecords;
import com.constellio.app.modules.rm.wrappers.AdministrativeUnit;
import com.constellio.app.ui.framework.components.ReportPresenter;
import com.constellio.app.ui.framework.reports.ReportWriterFactory;
import com.constellio.app.ui.pages.base.BasePresenter;
import com.constellio.model.entities.records.wrappers.User;

public class RMReportsPresenter extends BasePresenter<RMReportsView> implements ReportPresenter {

	private static final boolean BY_ADMINISTRATIVE_UNIT = true;
	private String schemaTypeValue;

	public RMReportsPresenter(RMReportsView view) {
		super(view);
	}

	@Override
	public List<String> getSupportedReports() {
		return Arrays.asList("Reports.ClassificationPlan",
				"Reports.DetailedClassificationPlan",
				"Reports.ClassificationPlanByAdministrativeUnit",
				"Reports.ConservationRulesList",
				"Reports.ConservationRulesListByAdministrativeUnit",
				"Reports.AdministrativeUnits",
				"Reports.AdministrativeUnitsAndUsers",
				"Reports.Users");
	}

	public ReportWriterFactory getReport(String report) {
		switch (report) {
		case "Reports.fakeReport2":
			return new ExampleReportFactoryWithoutRecords();
		case "Reports.ClassificationPlan":
			return new ClassificationPlanReportViewImpl(false);
		case "Reports.DetailedClassificationPlan":
			return new ClassificationPlanReportViewImpl(true);
		case "Reports.ConservationRulesList":
			return new ConservationRulesReportViewImpl();
		case "Reports.ConservationRulesListByAdministrativeUnit":
			return new ConservationRulesReportViewImpl(BY_ADMINISTRATIVE_UNIT, schemaTypeValue);
		case "Reports.AdministrativeUnits":
			return new AdministrativeUnitReportViewImpl();
		case "Reports.AdministrativeUnitsAndUsers":
			return new AdministrativeUnitReportViewImpl(true);
		case "Reports.Users":
			return new UserReportViewImpl();
		case "Reports.ClassificationPlanByAdministrativeUnit":
			return new ClassificationPlanReportViewImpl(false, schemaTypeValue);
		}

		throw new RuntimeException("BUG: Unknown report: " + report);

	}

	public boolean isWithSchemaType(String report) {
		switch (report) {
		case "Reports.ConservationRulesListByAdministrativeUnit":
		case "Reports.ClassificationPlanByAdministrativeUnit":
			return true;
		default:
			return false;
		}
	}

	public String getSchemaTypeValue(String report) {
		switch (report) {
		case "Reports.ConservationRulesListByAdministrativeUnit":
		case "Reports.ClassificationPlanByAdministrativeUnit":
			return AdministrativeUnit.SCHEMA_TYPE;
		default:
			return null;
		}
	}

	public void setSchemaTypeValue(String schemaTypeValue) {
		this.schemaTypeValue = schemaTypeValue;
	}

	public void backButtonClicked() {
		view.navigate().to(RMViews.class).archiveManagement();
	}

	@Override
	protected boolean hasPageAccess(String params, User user) {
		return user.has(RMPermissionsTo.MANAGE_REPORTS).globally();
	}
}
