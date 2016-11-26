package com.constellio.app.modules.rm.reports.builders.search.stats;

import static com.constellio.app.ui.i18n.i18n.$;

import java.util.Map;

import com.constellio.app.modules.rm.reports.model.search.stats.StatsReportModel;
import com.constellio.app.modules.rm.services.RMSchemasRecordsServices;
import com.constellio.app.modules.rm.wrappers.Folder;
import com.constellio.app.ui.framework.reports.ReportWriter;
import com.constellio.app.ui.framework.reports.ReportWriterFactory;
import com.constellio.model.conf.FoldersLocator;
import com.constellio.model.entities.schemas.DataStoreField;
import com.constellio.model.services.factories.ModelLayerFactory;
import com.constellio.model.services.search.query.logical.LogicalSearchQuery;

public class StatsReportWriterFactory implements ReportWriterFactory {
	private final Map<String, Object> statistics;

	public StatsReportWriterFactory(String collection, ModelLayerFactory modelLayerFactory, LogicalSearchQuery query) {
		RMSchemasRecordsServices schemas = new RMSchemasRecordsServices(collection, modelLayerFactory);
		DataStoreField folderLinearSizeMetadata = schemas.folder.schemaType().getDefaultSchema().getMetadata(Folder.LINEAR_SIZE);
		query.computeStatsOnField(folderLinearSizeMetadata.getDataStoreCode());
		statistics = modelLayerFactory.newSearchServices().query(query)
				.getStatValues(folderLinearSizeMetadata.getDataStoreCode());
	}

	@Override
	public ReportWriter getReportBuilder(ModelLayerFactory modelLayerFactory) {
		FoldersLocator folderLocator = modelLayerFactory.getFoldersLocator();
		return new StatsReportWriter(new StatsReportModel().setStats(statistics), folderLocator);
	}

	@Override
	public String getFilename() {
		return $("Reports.FolderLinearMeasureStats" + "." + new StatsReportWriter(null, null).getFileExtension());
	}

	public Map<String, Object> getStatistics() {
		return statistics;
	}
}
