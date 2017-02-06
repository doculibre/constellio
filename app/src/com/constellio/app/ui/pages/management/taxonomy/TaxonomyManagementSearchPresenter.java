package com.constellio.app.ui.pages.management.taxonomy;

import static com.constellio.app.ui.i18n.i18n.$;
import static com.constellio.model.services.taxonomies.ConceptNodesTaxonomySearchServices.fromTypeIn;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

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
import com.constellio.model.entities.schemas.MetadataSchema;
import com.constellio.model.entities.schemas.MetadataSchemaTypes;
import com.constellio.model.entities.schemas.Schemas;
import com.constellio.model.services.search.StatusFilter;
import com.constellio.model.services.search.query.logical.LogicalSearchQuery;
import com.constellio.model.services.search.query.logical.condition.LogicalSearchCondition;
import com.constellio.model.services.search.query.logical.condition.SchemaFilters;
import com.constellio.model.services.taxonomies.TaxonomiesManager;
import com.constellio.model.services.taxonomies.TaxonomiesSearchServices;

public class TaxonomyManagementSearchPresenter extends BasePresenter<TaxonomyManagementSearchView> {

	public static final String TAXONOMY_CODE = "taxonomyCode";
	public static final String QUERY = "q";
	private MetadataSchemaToVOBuilder schemaVOBuilder = new MetadataSchemaToVOBuilder();
	private transient TaxonomiesSearchServices taxonomiesSearchServices;
	private TaxonomyVO taxonomy;
	private String queryExpression;
	private Taxonomy retrievedTaxonomy;

	public TaxonomyManagementSearchPresenter(TaxonomyManagementSearchView view) {
		super(view);
	}

	public TaxonomyManagementSearchPresenter forParams(String parameters) {
		Map<String, String> params = ParamUtils.getParamsMap(parameters);
		String taxonomyCode = params.get(TAXONOMY_CODE);
		queryExpression = params.get(QUERY);
		taxonomy = new TaxonomyToVOBuilder().build(fetchTaxonomy(taxonomyCode));
		taxonomiesSearchServices = modelLayerFactory.newTaxonomiesSearchService();
		retrievedTaxonomy = fetchTaxonomy(taxonomy.getCode());
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
		for (String schemaType : taxonomy.getSchemaTypes()) {
			for (MetadataSchema schema : schemaTypes.getSchemaType(schemaType).getAllSchemas()) {
				createDataProviderForSchema(dataProviders, schema);
			}
		}
		return dataProviders;
	}

	void createDataProviderForSchema(List<RecordVODataProvider> dataProviders, MetadataSchema schema) {
		final String schemaCode = schema.getCode();
		Factory<LogicalSearchQuery> queryFactory = new Factory<LogicalSearchQuery>() {
			@Override
			public LogicalSearchQuery get() {
				LogicalSearchCondition condition = fromTypeIn(retrievedTaxonomy).returnAll();
				LogicalSearchQuery query = getLogicalSearchQueryForSchema(schema(schemaCode),
						new LogicalSearchQuery().setCondition(condition));
				query.setFreeTextQuery(queryExpression);
				if (isPrincipalTaxonomy()) {
					query.filteredWithUser(getCurrentUser());
				}
				return query.filteredByStatus(StatusFilter.ACTIVES);
			}
		};
		dataProviders.add(createDataProvider(queryFactory));
	}

	RecordVODataProvider createDataProvider(final Factory<LogicalSearchQuery> queryFactory) {
		final String schemaCode = queryFactory.get().getSchemaCondition().getCode();

		List<String> metadataCodes = new ArrayList<>();
		metadataCodes.add(schemaCode + "_id");
		metadataCodes.add(schemaCode + "_code");
		metadataCodes.add(schemaCode + "_title");

		MetadataSchemaVO schemaVO = schemaVOBuilder
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

	private LogicalSearchQuery getLogicalSearchQueryForSchema(MetadataSchema schema, LogicalSearchQuery query) {
		return new LogicalSearchQuery(query.getCondition().withFilters(new SchemaFilters(schema)));
	}

	Taxonomy fetchTaxonomy(String taxonomyCode) {
		TaxonomiesManager taxonomiesManager = view.getConstellioFactories().getModelLayerFactory().getTaxonomiesManager();
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
			view.refreshTable();
		} else {
			view.showErrorMessage($("TaxonomyManagementView.cannotDelete"));
		}
	}

	public boolean isPrincipalTaxonomy() {
		TaxonomiesManager taxonomiesManager = view.getConstellioFactories().getModelLayerFactory().getTaxonomiesManager();
		Taxonomy principalTaxonomy = taxonomiesManager.getPrincipalTaxonomy(collection);
		return principalTaxonomy != null && principalTaxonomy.getCode().equals(taxonomy.getCode());
	}

	public void backButtonClicked() {
		view.navigate().to().taxonomyManagement(taxonomy.getCode());
	}

	@Override
	protected boolean hasPageAccess(String parameters, final User user) {
		Map<String, String> params = ParamUtils.getParamsMap(parameters);
		String taxonomyCode = params.get(TAXONOMY_CODE);
		return new TaxonomyPresentersService(appLayerFactory).canManage(taxonomyCode, user);
	}

	public void viewAssembled() {
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
		view.navigate().to().taxonomySearch(taxonomy.getCode(), freeText);
	}

	public String getQueryExpression() {
		return queryExpression;
	}
}
