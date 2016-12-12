package com.constellio.app.modules.rm.reports.builders.search.stats;

import com.constellio.app.modules.rm.services.RMSchemasRecordsServices;
import com.constellio.app.modules.rm.wrappers.Folder;
import com.constellio.app.services.factories.AppLayerFactory;
import com.constellio.model.entities.schemas.DataStoreField;
import com.constellio.model.services.factories.ModelLayerFactory;
import com.constellio.model.services.search.query.logical.LogicalSearchQuery;

import java.util.Map;

/**
 * Created by Constelio on 2016-11-29.
 */
public class StatsReportParameters {

	private final Map<String, Object> statistics;

	public StatsReportParameters(String collection, AppLayerFactory appLayerFactory, LogicalSearchQuery query) {
		RMSchemasRecordsServices schemas = new RMSchemasRecordsServices(collection, appLayerFactory);
		DataStoreField folderLinearSizeMetadata = schemas.folder.schemaType().getDefaultSchema().getMetadata(Folder.LINEAR_SIZE);
		query.computeStatsOnField(folderLinearSizeMetadata);
		statistics = appLayerFactory.getModelLayerFactory().newSearchServices().query(query)
				.getStatValues(folderLinearSizeMetadata);
	}

	public Map<String, Object> getStatistics() {
		return statistics;
	}
}
