/*Constellio Enterprise Information Management

Copyright (c) 2015 "Constellio inc."

This program is free software: you can redistribute it and/or modify
it under the terms of the GNU Affero General Public License as
published by the Free Software Foundation, either version 3 of the
License, or (at your option) any later version.

This program is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
GNU Affero General Public License for more details.

You should have received a copy of the GNU Affero General Public License
along with this program. If not, see <http://www.gnu.org/licenses/>.
*/
package com.constellio.app.modules.rm.ui.pages.reports;

import java.util.Arrays;
import java.util.List;

import com.constellio.app.modules.rm.constants.RMPermissionsTo;
import com.constellio.app.modules.rm.reports.builders.administration.plan.AdministrativeUnitReportViewImpl;
import com.constellio.app.modules.rm.reports.builders.administration.plan.ClassificationPlanReportViewImpl;
import com.constellio.app.modules.rm.reports.builders.administration.plan.ConservationRulesReportViewImpl;
import com.constellio.app.modules.rm.reports.builders.administration.plan.UserReportViewImpl;
import com.constellio.app.modules.rm.reports.factories.ExampleReportFactoryWithoutRecords;
import com.constellio.app.reports.builders.administration.plan.ReportBuilderFactory;
import com.constellio.app.ui.framework.components.ReportPresenter;
import com.constellio.app.ui.pages.base.BasePresenter;
import com.constellio.model.entities.records.wrappers.User;

public class RMReportsPresenter extends BasePresenter<RMReportsView> implements ReportPresenter {

	private static final boolean withAdministrativeUnit = true;

	public RMReportsPresenter(RMReportsView view) {
		super(view);
	}

	@Override
	public List<String> getSupportedReports() {
		return Arrays.asList("Reports.ClassificationPlan",
				"Reports.DetailedClassificationPlan",
				"Reports.ConservationRulesList",
				"Reports.ConservationRulesListByAdministrativeUnit",
				"Reports.AdministrativeUnits",
				"Reports.AdministrativeUnitsAndUsers",
				"Reports.Users");
	}

	public ReportBuilderFactory getReport(String report) {
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
			return new ConservationRulesReportViewImpl(withAdministrativeUnit);
		case "Reports.AdministrativeUnits":
			return new AdministrativeUnitReportViewImpl();
		case "Reports.AdministrativeUnitsAndUsers":
			return new AdministrativeUnitReportViewImpl(true);
		case "Reports.Users":
			return new UserReportViewImpl();
		}
		throw new RuntimeException("BUG: Unknown report: " + report);
	}

	public void backButtonClicked() {
		view.navigateTo().archivesManagement();
	}

	@Override
	protected boolean hasPageAccess(String params, User user) {
		return user.has(RMPermissionsTo.MANAGE_REPORTS).globally();
	}

}