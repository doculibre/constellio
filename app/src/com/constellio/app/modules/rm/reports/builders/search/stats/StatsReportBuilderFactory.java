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
package com.constellio.app.modules.rm.reports.builders.search.stats;

import com.constellio.app.modules.rm.reports.model.search.stats.StatsReportModel;
import com.constellio.app.modules.rm.services.RMSchemasRecordsServices;
import com.constellio.app.modules.rm.wrappers.Folder;
import com.constellio.app.reports.builders.administration.plan.ReportBuilder;
import com.constellio.app.reports.builders.administration.plan.ReportBuilderFactory;
import com.constellio.model.conf.FoldersLocator;
import com.constellio.model.entities.schemas.DataStoreField;
import com.constellio.model.services.factories.ModelLayerFactory;
import com.constellio.model.services.search.query.logical.LogicalSearchQuery;

import java.util.Map;
import static com.constellio.app.ui.i18n.i18n.$;

public class StatsReportBuilderFactory implements ReportBuilderFactory {
    private final Map<String, Object> statistics;

    public StatsReportBuilderFactory(String collection, ModelLayerFactory modelLayerFactory, LogicalSearchQuery query) {
        RMSchemasRecordsServices schemas = new RMSchemasRecordsServices(collection, modelLayerFactory);
        DataStoreField folderLinearSizeMetadata = schemas.folderSchemaType().getDefaultSchema().getMetadata(Folder.LINEAR_SIZE);
        query.computeStatsOnField(folderLinearSizeMetadata);
        statistics = modelLayerFactory.newSearchServices().query(query).getStatValues(folderLinearSizeMetadata);
    }

    @Override
    public ReportBuilder getReportBuilder(ModelLayerFactory modelLayerFactory) {
        FoldersLocator folderLocator = modelLayerFactory.getFoldersLocator();
        return new StatsReportBuilder(new StatsReportModel().setStats(statistics), folderLocator);
    }

    @Override
    public String getFilename() {
        return $("Reports.FolderLinearMeasureStats" + "." + new StatsReportBuilder(null, null).getFileExtension());
    }

    public Map<String, Object> getStatistics() {
        return statistics;
    }
}
