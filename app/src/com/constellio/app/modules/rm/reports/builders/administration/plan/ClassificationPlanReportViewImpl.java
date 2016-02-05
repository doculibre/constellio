package com.constellio.app.modules.rm.reports.builders.administration.plan;

import static com.constellio.app.ui.i18n.i18n.$;

import org.apache.commons.lang.StringUtils;

import com.constellio.app.modules.rm.reports.model.administration.plan.ClassificationPlanReportPresenter;
import com.constellio.app.ui.application.ConstellioUI;
import com.constellio.app.ui.framework.reports.ReportBuilder;
import com.constellio.app.ui.framework.reports.ReportBuilderFactory;
import com.constellio.app.ui.pages.base.SessionContext;
import com.constellio.model.services.factories.ModelLayerFactory;

public class ClassificationPlanReportViewImpl implements ReportBuilderFactory {

	boolean detail;
	private String administrativeUnitId;

	public ClassificationPlanReportViewImpl(boolean detail) {
		this.detail = detail;
	}

	public ClassificationPlanReportViewImpl(boolean detail, String administrativeUnitId) {
		this.detail = detail;
		this.administrativeUnitId = administrativeUnitId;
	}

	@Override
	public ReportBuilder getReportBuilder(ModelLayerFactory modelLayerFactory) {
		String collection = getSessionContext().getCurrentCollection();
		ClassificationPlanReportPresenter presenter = new ClassificationPlanReportPresenter(collection, modelLayerFactory,
				detail, administrativeUnitId);
		return new ClassificationPlanReportBuilder(presenter.build(), presenter.getFoldersLocator());
	}

	@Override
	public String getFilename() {
		if (StringUtils.isNotBlank(administrativeUnitId)) {
			return $("Report.ClassificationPlanByAdministrativeUnit") + ".pdf";
		} else if (detail) {
			return $("Report.DetailedClassificationPlan") + ".pdf";
		} else {
			return $("Report.ClassificationPlan") + ".pdf";
		}
	}

	SessionContext getSessionContext() {
		return ConstellioUI.getCurrentSessionContext();
	}
}
