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
package com.constellio.app.modules.rm.reports.model.search.stats;

import com.constellio.app.modules.rm.services.RMSchemasRecordsServices;
import com.constellio.model.conf.FoldersLocator;
import com.constellio.model.entities.schemas.MetadataSchemaTypes;
import com.constellio.model.services.factories.ModelLayerFactory;
import com.constellio.model.services.search.query.ReturnedMetadatasFilter;
import com.constellio.model.services.taxonomies.TaxonomiesSearchOptions;
import com.constellio.model.services.taxonomies.TaxonomiesSearchServices;

public class FolderLinearMeasureStatsReportPresenter {
	private String collection;
	private ModelLayerFactory modelLayerFactory;
	private MetadataSchemaTypes types;
	private TaxonomiesSearchOptions searchOptions;
	private TaxonomiesSearchServices searchService;
	private RMSchemasRecordsServices rmSchemasRecordsServices;
	private boolean withUsers;

	public FolderLinearMeasureStatsReportPresenter(String collection, ModelLayerFactory modelLayerFactory) {

		this.collection = collection;
		this.modelLayerFactory = modelLayerFactory;
		this.withUsers = true;
	}

	public FolderLinearMeasureStatsReportPresenter(String collection, ModelLayerFactory modelLayerFactory, boolean withUsers) {

		this.collection = collection;
		this.modelLayerFactory = modelLayerFactory;
		this.withUsers = withUsers;
	}

	public StatsReportModel build() {
		init();

		/*AdministrativeUnitReportModel model = new AdministrativeUnitReportModel();

		List<TaxonomySearchRecord> taxonomySearchRecords = searchService.getLinkableRootConcept(User.GOD, collection,
				RMTaxonomies.ADMINISTRATIVE_UNITS, AdministrativeUnit.SCHEMA_TYPE, searchOptions);

		List<AdministrativeUnitReportModel_AdministrativeUnit> modelAdministrativeUnits = getUnits(taxonomySearchRecords);

		model.setAdministrativeUnits(modelAdministrativeUnits);*/

		return null;
	}

	private void init() {
		types = modelLayerFactory.getMetadataSchemasManager().getSchemaTypes(collection);
		searchOptions = new TaxonomiesSearchOptions().setReturnedMetadatasFilter(ReturnedMetadatasFilter.all());
		searchService = modelLayerFactory.newTaxonomiesSearchService();
		rmSchemasRecordsServices = new RMSchemasRecordsServices(collection, modelLayerFactory);
	}

	public boolean isWithUsers() {
		return withUsers;
	}

	public FoldersLocator getFoldersLocator() {
		return modelLayerFactory.getFoldersLocator();
	}
}