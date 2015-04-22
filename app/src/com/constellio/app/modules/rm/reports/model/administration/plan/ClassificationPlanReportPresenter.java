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
package com.constellio.app.modules.rm.reports.model.administration.plan;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.slf4j.LoggerFactory;

import com.constellio.app.modules.rm.constants.RMTaxonomies;
import com.constellio.app.modules.rm.reports.model.administration.plan.ClassificationPlanReportModel.ClassificationPlanReportModel_Category;
import com.constellio.app.modules.rm.wrappers.Category;
import com.constellio.model.conf.FoldersLocator;
import com.constellio.model.entities.records.Record;
import com.constellio.model.entities.records.wrappers.User;
import com.constellio.model.entities.schemas.MetadataSchemaTypes;
import com.constellio.model.services.factories.ModelLayerFactory;
import com.constellio.model.services.search.query.ReturnedMetadatasFilter;
import com.constellio.model.services.taxonomies.TaxonomiesSearchOptions;
import com.constellio.model.services.taxonomies.TaxonomiesSearchServices;
import com.constellio.model.services.taxonomies.TaxonomySearchRecord;

public class ClassificationPlanReportPresenter {

	private static final org.slf4j.Logger LOGGER = LoggerFactory.getLogger(ClassificationPlanReportPresenter.class);

	private String collection;
	private ModelLayerFactory modelLayerFactory;
	private MetadataSchemaTypes types;
	private TaxonomiesSearchOptions searchOptions;
	private TaxonomiesSearchServices searchService;
	private boolean detailed;

	public ClassificationPlanReportPresenter(String collection, ModelLayerFactory modelLayerFactory) {

		this.collection = collection;
		this.modelLayerFactory = modelLayerFactory;
		detailed = false;
	}

	public ClassificationPlanReportPresenter(String collection, ModelLayerFactory modelLayerFactory, boolean detailed) {

		this.collection = collection;
		this.modelLayerFactory = modelLayerFactory;
		this.detailed = detailed;
	}

	public ClassificationPlanReportModel build() {
		init();

		ClassificationPlanReportModel model = new ClassificationPlanReportModel();
		model.setDetailed(detailed);

		List<ClassificationPlanReportModel_Category> rootCategories = model.getRootCategories();

		List<TaxonomySearchRecord> taxonomySearchRecords = searchService.getLinkableRootConcept(User.GOD, collection,
				RMTaxonomies.CLASSIFICATION_PLAN, Category.SCHEMA_TYPE, searchOptions);

		if (taxonomySearchRecords != null) {
			for (TaxonomySearchRecord taxonomyRecord : taxonomySearchRecords) {

				if (taxonomyRecord != null) {
					Record record = taxonomyRecord.getRecord();
					if (record != null) {
						Category recordCategory = new Category(record, types);

						if (recordCategory != null) {
							ClassificationPlanReportModel_Category modelCategory = new ClassificationPlanReportModel_Category();

							String code = StringUtils.defaultString(recordCategory.getCode());
							modelCategory.setCode(code);

							String title = StringUtils.defaultString(recordCategory.getTitle());
							modelCategory.setLabel(title);

							String description = StringUtils.defaultString(recordCategory.getDescription());
							modelCategory.setDescription(description);

							modelCategory.setCategories(getCategoriesForRecord(record));

							rootCategories.add(modelCategory);
						}
					}
				}
			}
		}

		return model;
	}

	private void init() {
		types = modelLayerFactory.getMetadataSchemasManager().getSchemaTypes(collection);
		searchOptions = new TaxonomiesSearchOptions().setReturnedMetadatasFilter(ReturnedMetadatasFilter.all());
		searchService = modelLayerFactory.newTaxonomiesSearchService();
	}

	private List<ClassificationPlanReportModel_Category> getCategoriesForRecord(Record record) {
		List<ClassificationPlanReportModel_Category> modelCategories = new ArrayList<>();

		List<TaxonomySearchRecord> children = searchService.getLinkableChildConcept(User.GOD, record,
				RMTaxonomies.CLASSIFICATION_PLAN, Category.SCHEMA_TYPE, searchOptions);

		if (children != null) {
			for (TaxonomySearchRecord child : children) {
				if (child != null) {
					try {
						Record childRecord = child.getRecord();
						if (childRecord != null) {
							Category recordCategory = new Category(childRecord, types);

							if (recordCategory != null) {
								ClassificationPlanReportModel_Category modelCategory = new ClassificationPlanReportModel_Category();

								String categoryCode = StringUtils.defaultString(recordCategory.getCode());
								modelCategory.setCode(categoryCode);

								String categoryTitle = StringUtils.defaultString(recordCategory.getTitle());
								modelCategory.setLabel(categoryTitle);

								String categoryDescription = StringUtils.defaultString(recordCategory.getDescription());
								modelCategory.setDescription(categoryDescription);

								Record childChildRecord = child.getRecord();

								modelCategory.setCategories(getCategoriesForRecord(childChildRecord));

								modelCategories.add(modelCategory);
							}
						}
					} catch (Exception e) {
						// throw new RuntimeException(e);
						LOGGER.info("This is not a category. It's a " + child.getRecord().getSchemaCode());
					}
				}
			}
		}
		return modelCategories;
	}

	public FoldersLocator getFoldersLocator() {
		return modelLayerFactory.getFoldersLocator();
	}
}