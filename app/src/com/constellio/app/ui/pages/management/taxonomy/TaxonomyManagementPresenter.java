package com.constellio.app.ui.pages.management.taxonomy;

import static com.constellio.app.ui.i18n.i18n.$;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import com.constellio.app.api.extensions.taxonomies.GetTaxonomyExtraFieldsParam;
import com.constellio.app.api.extensions.taxonomies.GetTaxonomyManagementClassifiedTypesParams;
import com.constellio.app.api.extensions.taxonomies.TaxonomyExtraField;
import com.constellio.app.api.extensions.taxonomies.TaxonomyManagementClassifiedType;
import com.constellio.app.modules.rm.navigation.RMViews;
import com.constellio.app.ui.entities.MetadataSchemaVO;
import com.constellio.app.ui.entities.RecordVO;
import com.constellio.app.ui.entities.RecordVO.VIEW_MODE;
import com.constellio.app.ui.entities.TaxonomyVO;
import com.constellio.app.ui.framework.builders.MetadataSchemaToVOBuilder;
import com.constellio.app.ui.framework.builders.RecordToVOBuilder;
import com.constellio.app.ui.framework.builders.TaxonomyToVOBuilder;
import com.constellio.app.ui.framework.data.RecordVODataProvider;
import com.constellio.app.ui.pages.base.BasePresenter;
import com.constellio.app.ui.pages.base.SchemaPresenterUtils;
import com.constellio.app.ui.params.ParamUtils;
import com.constellio.data.utils.Factory;
import com.constellio.model.entities.Taxonomy;
import com.constellio.model.entities.records.Record;
import com.constellio.model.entities.records.wrappers.User;
import com.constellio.model.entities.schemas.Metadata;
import com.constellio.model.entities.schemas.MetadataSchema;
import com.constellio.model.entities.schemas.MetadataSchemaTypes;
import com.constellio.model.entities.schemas.Schemas;
import com.constellio.model.services.search.StatusFilter;
import com.constellio.model.services.search.query.logical.LogicalSearchQuery;
import com.constellio.model.services.search.query.logical.condition.SchemaFilters;
import com.constellio.model.services.taxonomies.TaxonomiesManager;
import com.constellio.model.services.taxonomies.TaxonomiesSearchOptions;

public class TaxonomyManagementPresenter extends BasePresenter<TaxonomyManagementView> {
	public static final String TAXONOMY_CODE = "taxonomyCode";
	public static final String CONCEPT_ID = "conceptId";

	private MetadataSchemaToVOBuilder schemaVOBuilder = new MetadataSchemaToVOBuilder();
	TaxonomyVO taxonomy;
	String conceptId;
	String taxonomyCode;

	public TaxonomyManagementPresenter(TaxonomyManagementView view) {
		super(view);
	}

	public TaxonomyManagementPresenter forParams(String parameters) {
		Map<String, String> params = ParamUtils.getParamsMap(parameters);
		taxonomyCode = params.get(TAXONOMY_CODE);
		conceptId = params.get(CONCEPT_ID);
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
				return query.filteredByStatus(StatusFilter.ACTIVES);
			}
		};

		dataProviders.add(createDataProvider(queryFactory));
	}

	RecordVODataProvider createDataProvider(final Factory<LogicalSearchQuery> queryFactory) {
		final String schemaCode = queryFactory.get().getSchemaCondition().getCode();

		List<String> metadataCodes = new ArrayList<String>();
		metadataCodes.add(schemaCode + "_id");
		metadataCodes.add(schemaCode + "_code");
		metadataCodes.add(schemaCode + "_title");

		MetadataSchemaVO schemaVO = schemaVOBuilder.build(
				schema(schemaCode), VIEW_MODE.TABLE, metadataCodes, view.getSessionContext());
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

	public LogicalSearchQuery getLogicalSearchQueryForSchema(MetadataSchema schema, LogicalSearchQuery query) {
		return new LogicalSearchQuery(query.getCondition().withFilters(new SchemaFilters(schema)));
	}

	public RecordVO getCurrentConcept() {
		if (conceptId != null) {
			return presenterService().getRecordVO(conceptId, VIEW_MODE.DISPLAY, view.getSessionContext());
		} else {
			return null;
		}
	}

	Taxonomy fetchTaxonomy(String taxonomyCode) {
		TaxonomiesManager taxonomiesManager = modelLayerFactory.getTaxonomiesManager();
		return taxonomiesManager.getEnabledTaxonomyWithCode(view.getCollection(), taxonomyCode);
	}

	public TaxonomyVO getTaxonomy() {
		return taxonomy;
	}

	public void displayButtonClicked(RecordVO recordVO) {
		view.navigate().to().taxonomyManagement(taxonomy.getCode(), recordVO.getId());
	}

	public void editButtonClicked(RecordVO recordVO) {
		view.navigate().to().editTaxonomyConcept(taxonomy.getCode(), recordVO.getId(), recordVO.getSchema().getCode());
	}

	public void deleteButtonClicked(RecordVO recordVO) {
		if (isDeletable(recordVO)) {
			SchemaPresenterUtils utils = new SchemaPresenterUtils(recordVO.getSchema().getCode(), view.getConstellioFactories(),
					view.getSessionContext());
			utils.delete(utils.toRecord(recordVO), null, false);
			if (recordVO.getId().equals(conceptId)) {
				backButtonClicked();
			} else {
				view.refreshTable();
			}
		} else {
			view.showErrorMessage($("TaxonomyManagementView.cannotDelete"));
		}
	}

	public void addLinkClicked(String taxonomyCode, String schemaCode) {
		view.navigate().to().addTaxonomyConcept(taxonomyCode, conceptId, schemaCode);
	}

	public void manageAccessAuthorizationsButtonClicked() {
		if (conceptId != null) {
			view.navigate().to().listObjectAccessAuthorizations(conceptId);
		}
	}

	public void manageRoleAuthorizationsButtonClicked() {
		if (conceptId != null) {
			view.navigate().to().listObjectRoleAuthorizations(conceptId);
		}
	}

	public boolean isPrincipalTaxonomy() {
		TaxonomiesManager taxonomiesManager = modelLayerFactory.getTaxonomiesManager();
		Taxonomy principalTaxonomy = taxonomiesManager.getPrincipalTaxonomy(collection);
		return principalTaxonomy != null && principalTaxonomy.getCode().equals(taxonomy.getCode());
	}

	public void backButtonClicked() {
		if (conceptId == null) {
			view.navigate().to().adminModule();
		} else {
			SchemaPresenterUtils taxonomyPresenterUtils = new SchemaPresenterUtils(getCurrentConcept().getSchema().getCode(),
					view.getConstellioFactories(), view.getSessionContext());
			String[] pathParts = ((String) taxonomyPresenterUtils.getRecord(conceptId).getList(Schemas.PARENT_PATH).get(0))
					.split("/");
			String parentId = pathParts[pathParts.length - 1];
			if (taxonomyCode.equals(parentId)) {
				view.navigate().to().taxonomyManagement(taxonomyCode);
			} else {
				view.navigate().to().taxonomyManagement(taxonomyCode, parentId);
			}
		}
	}

	@Override
	protected boolean hasPageAccess(String parameters, final User user) {
		Map<String, String> params = ParamUtils.getParamsMap(parameters);
		String taxonomyCode = params.get(TAXONOMY_CODE);
		return new TaxonomyPresentersService(appLayerFactory).canManage(taxonomyCode, user);
	}

	public void tabElementClicked(RecordVO recordVO) {
		//TODO Refactoring
		view.navigate().to(RMViews.class).displayFolder(recordVO.getId());
	}

	public List<TaxonomyManagementClassifiedType> getClassifiedTypes() {
		if (conceptId == null) {
			return new ArrayList<>();

		} else {
			Taxonomy taxonomy = fetchTaxonomy(getTaxonomy().getCode());
			Record record = modelLayerFactory.newRecordServices().getDocumentById(conceptId);
			return appCollectionExtentions
					.getClassifiedTypes(new GetTaxonomyManagementClassifiedTypesParams(taxonomy, record, view));
		}

	}

	public void viewAssembled() {
		if (conceptId != null) {
			view.setTabs(getClassifiedTypes());
		}
	}

	public List<TaxonomyExtraField> getExtraFields() {
		Taxonomy taxonomy = fetchTaxonomy(getTaxonomy().getCode());
		Record record = modelLayerFactory.newRecordServices().getDocumentById(conceptId);
		return appCollectionExtentions.getTaxonomyExtraFields(new GetTaxonomyExtraFieldsParam(taxonomy, record, view));
	}

	public boolean isDeletable(RecordVO entity) {
		Record record = presenterService().getRecord(entity.getId());
		User user = getCurrentUser();
		return recordServices().isLogicallyThenPhysicallyDeletable(record, user);
	}

	public String getDefaultOrderField() {
		return Schemas.CODE.getLocalCode();
	}

	public void searchConcept(String freeText) {
		view.navigate().to().taxonomySearch(taxonomyCode, freeText);
	}
}
