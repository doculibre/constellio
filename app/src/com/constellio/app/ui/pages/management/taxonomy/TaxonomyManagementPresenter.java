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
package com.constellio.app.ui.pages.management.taxonomy;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import com.constellio.app.modules.rm.constants.RMTaxonomies;
import com.constellio.app.ui.entities.MetadataSchemaVO;
import com.constellio.app.ui.entities.RecordVO;
import com.constellio.app.ui.entities.RecordVO.VIEW_MODE;
import com.constellio.app.ui.entities.TaxonomyVO;
import com.constellio.app.ui.framework.builders.MetadataSchemaToVOBuilder;
import com.constellio.app.ui.framework.builders.RecordToVOBuilder;
import com.constellio.app.ui.framework.builders.TaxonomyToVOBuilder;
import com.constellio.app.ui.framework.data.RecordVODataProvider;
import com.constellio.app.ui.pages.base.BasePresenter;
import com.constellio.app.ui.params.ParamUtils;
import com.constellio.data.utils.Factory;
import com.constellio.model.entities.Taxonomy;
import com.constellio.model.entities.records.Record;
import com.constellio.model.entities.records.wrappers.User;
import com.constellio.model.entities.schemas.Metadata;
import com.constellio.model.entities.schemas.MetadataSchema;
import com.constellio.model.entities.schemas.MetadataSchemaTypes;
import com.constellio.model.services.search.query.logical.LogicalSearchQuery;
import com.constellio.model.services.search.query.logical.condition.SchemaFilters;
import com.constellio.model.services.taxonomies.TaxonomiesManager;
import com.constellio.model.services.taxonomies.TaxonomiesSearchOptions;

public class TaxonomyManagementPresenter extends BasePresenter<TaxonomyManagementView> {

	public static final String TAXONOMY_CODE = "taxonomyCode";
	public static final String CONCEPT_ID = "conceptId";
	public static final String PARENT_CONCEPT_ID = "parentConceptId";
	TaxonomyVO taxonomy;
	String conceptId;
	String parentConceptId;

	public TaxonomyManagementPresenter(TaxonomyManagementView view) {
		super(view);
	}

	public TaxonomyManagementPresenter forParams(String parameters) {

		Map<String, String> params = ParamUtils.getParamsMap(parameters);
		String taxonomyCode = params.get(TAXONOMY_CODE);
		conceptId = params.get(CONCEPT_ID);
		parentConceptId = params.get(PARENT_CONCEPT_ID);
		taxonomy = new TaxonomyToVOBuilder().build(fetchTaxonomy(taxonomyCode));
		return this;
	}

	public List<RecordVODataProvider> getDataProviders() {
		MetadataSchemaTypes schemaTypes = modelLayerFactory.getMetadataSchemasManager()
				.getSchemaTypes(taxonomy.getCollection());
		List<RecordVODataProvider> dataProviders = createDataProvidersForSchemas(schemaTypes);

		return dataProviders;
	}

	List<RecordVODataProvider> createDataProvidersForSchemas(MetadataSchemaTypes schemaTypes) {
		List<RecordVODataProvider> dataProviders = new ArrayList<>();
		MetadataSchema currentSchema = null;
		if (getCurrentConcept() != null) {
			currentSchema = schema(getCurrentConcept().getSchema().getCode());
		}
		for (String schemaType : taxonomy.getSchemaTypes()) {
			for (MetadataSchema schema : schemaTypes.getSchemaType(schemaType).getAllSchemas()) {
				createDataProviderIfSchemaCanBeChildOfCurrentConcept(dataProviders, currentSchema, schema);
			}
		}
		return dataProviders;
	}

	private void createDataProviderIfSchemaCanBeChildOfCurrentConcept(List<RecordVODataProvider> dataProviders,
			MetadataSchema currentSchema, MetadataSchema schema) {
		if (getCurrentConcept() != null) {
			for (Metadata reference : schema.getParentReferences()) {
				if (reference.getAllowedReferences().isAllowed(currentSchema)) {
					createDataProviderForSchema(dataProviders, schema);
					break;
				}
			}
		} else {
			createDataProviderForSchema(dataProviders, schema);
		}
	}

	void createDataProviderForSchema(List<RecordVODataProvider> dataProviders, MetadataSchema schema) {
		final String schemaCode = schema.getCode();
		Factory<LogicalSearchQuery> queryFactory = new Factory<LogicalSearchQuery>() {
			@Override
			public LogicalSearchQuery get() {
				LogicalSearchQuery query;
				if (conceptId != null) {
					Record record = recordServices().getDocumentById(conceptId);
					query = modelLayerFactory.newTaxonomiesSearchService()
							.getChildConceptsQuery(record, new TaxonomiesSearchOptions());
				} else {
					query = modelLayerFactory.newTaxonomiesSearchService()
							.getRootConceptsQuery(view.getSessionContext().getCurrentCollection(), taxonomy.getCode(),
									new TaxonomiesSearchOptions());
				}
				query = getLogicalSearchQueryForSchema(schema(schemaCode), query);
				return query;
			}
		};

		dataProviders.add(createDataProvider(queryFactory));
	}

	public LogicalSearchQuery getLogicalSearchQueryForSchema(MetadataSchema schema, LogicalSearchQuery query) {
		return new LogicalSearchQuery(query.getCondition().withFilters(new SchemaFilters(schema)));
	}

	RecordVODataProvider createDataProvider(final Factory<LogicalSearchQuery> queryFactory) {
		final String schemaCode = queryFactory.get().getSchemaCondition().getCode();

		List<String> metadataCodes = new ArrayList<String>();
		metadataCodes.add(schemaCode + "_id");
		metadataCodes.add(schemaCode + "_code");
		metadataCodes.add(schemaCode + "_title");

		MetadataSchemaVO schemaVO = new MetadataSchemaToVOBuilder()
				.build(schema(schemaCode), VIEW_MODE.TABLE, metadataCodes, view.getSessionContext());
		RecordToVOBuilder voBuilder = new RecordToVOBuilder();
		RecordVODataProvider dataProvider = new RecordVODataProvider(schemaVO, voBuilder, modelLayerFactory,
				view.getSessionContext()) {
			@Override
			protected LogicalSearchQuery getQuery() {
				return queryFactory.get();
			}
		};
		return dataProvider;
	}

	public RecordVO getCurrentConcept() {
		if (conceptId != null) {
			return presenterService().getRecordVO(conceptId, VIEW_MODE.DISPLAY);
		} else {
			return null;
		}
	}

	Taxonomy fetchTaxonomy(String taxonomyCode) {
		TaxonomiesManager taxonomiesManager = view.getConstellioFactories().getModelLayerFactory().getTaxonomiesManager();
		return taxonomiesManager.getEnabledTaxonomyWithCode(view.getCollection(), taxonomyCode);
	}

	public TaxonomyVO getTaxonomy() {
		return taxonomy;
	}

	public void displayButtonClicked(RecordVO recordVO) {
		view.navigateTo().taxonomyManagement(taxonomy.getCode(), recordVO.getId(), conceptId);
	}

	public void editButtonClicked(RecordVO recordVO) {
		view.navigateTo().editTaxonomyConcept(taxonomy.getCode(), recordVO.getId(), recordVO.getSchema().getCode());
	}

	public void addLinkClicked(String taxonomyCode, String schemaCode) {
		view.navigateTo().addTaxonomyConcept(taxonomyCode, conceptId, schemaCode);
	}

	public void manageAuthorizationsButtonClicked() {
		if (conceptId != null) {
			view.navigateTo().listObjectAuthorizations(conceptId);
		}
	}

	public boolean isPrincipalTaxonomy() {
		return taxonomy.getCode().equals(RMTaxonomies.ADMINISTRATIVE_UNITS);
	}

	public void backButtonClicked() {
		if (conceptId == null) {
			view.navigateTo().adminModule();
		} else if (parentConceptId == null) {
			view.navigateTo().taxonomyManagement(taxonomy.getCode());
		} else {
			view.navigateTo().taxonomyManagement(taxonomy.getCode(), parentConceptId, null);
		}
	}

	@Override
	protected boolean hasPageAccess(String parameters, final User user) {
		Map<String, String> params = ParamUtils.getParamsMap(parameters);
		String taxonomyCode = params.get(TAXONOMY_CODE);
		return new TaxonomyPresentersService(appLayerFactory).canManage(taxonomyCode, user);
	}

}
