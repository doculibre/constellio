package com.constellio.app.modules.rm.reports.builders.search.stats;

import static com.constellio.app.ui.i18n.i18n.$;

import java.util.Map;

import com.constellio.app.modules.rm.reports.model.search.stats.StatsReportModel;
import com.constellio.app.modules.rm.services.RMSchemasRecordsServices;
import com.constellio.app.modules.rm.wrappers.Folder;
import com.constellio.app.services.factories.AppLayerFactory;
import com.constellio.app.ui.framework.reports.NewReportWriterFactory;
import com.constellio.app.ui.framework.reports.ReportWriter;
import com.constellio.app.ui.framework.reports.ReportWriterFactory;
import com.constellio.model.conf.FoldersLocator;
import com.constellio.model.entities.schemas.DataStoreField;
import com.constellio.model.services.factories.ModelLayerFactory;
import com.constellio.model.services.search.query.logical.LogicalSearchQuery;

public class StatsReportWriterFactory implements NewReportWriterFactory<StatsReportParameters> {
	protected AppLayerFactory appLayerFactory;

	public StatsReportWriterFactory(AppLayerFactory appLayerFactory) {
		this.appLayerFactory = appLayerFactory;
	}

	@Override
	public ReportWriter getReportBuilder(StatsReportParameters parameters) {
		FoldersLocator folderLocator = appLayerFactory.getModelLayerFactory().getFoldersLocator();
		return new StatsReportWriter(new StatsReportModel().setStats(parameters.getStatistics()), folderLocator);
	}

	@Override
	public String getFilename(StatsReportParameters parameters) {
		return $("Reports.FolderLinearMeasureStats" + "." + new StatsReportWriter(null, null).getFileExtension());
	}
}
