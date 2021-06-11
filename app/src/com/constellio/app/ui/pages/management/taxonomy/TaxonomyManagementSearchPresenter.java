package com.constellio.app.ui.pages.management.taxonomy;

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
import com.constellio.app.ui.util.MessageUtils;
import com.constellio.data.utils.Factory;
import com.constellio.model.entities.Language;
import com.constellio.model.entities.Taxonomy;
import com.constellio.model.entities.records.Record;
import com.constellio.model.entities.records.wrappers.User;
import com.constellio.model.entities.schemas.MetadataSchema;
import com.constellio.model.entities.schemas.MetadataSchemaTypes;
import com.constellio.model.entities.schemas.Schemas;
import com.constellio.model.frameworks.validation.OptimisticLockException;
import com.constellio.model.frameworks.validation.ValidationErrors;
import com.constellio.model.services.search.StatusFilter;
import com.constellio.model.services.search.query.logical.LogicalSearchQuery;
import com.constellio.model.services.search.query.logical.condition.LogicalSearchCondition;
import com.constellio.model.services.search.query.logical.condition.SchemaFilters;
import com.constellio.model.services.taxonomies.TaxonomiesManager;
import com.constellio.model.services.taxonomies.TaxonomiesSearchServices;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static com.constellio.model.services.records.RecordHierarchyServices.fromTypeIn;

public class TaxonomyManagementSearchPresenter extends BasePresenter<TaxonomyManagementSearchView> {
	private static Logger LOGGER = LoggerFactory.getLogger(TaxonomyManagementSearchPresenter.class);

	public static final String TAXONOMY_CODE = "taxonomyCode";
	public static final String QUERY = "q";
	private MetadataSchemaToVOBuilder schemaVOBuilder = new MetadataSchemaToVOBuilder();
	private transient TaxonomiesSearchServices taxonomiesSearchServices;
	private TaxonomyVO taxonomy;
	private String queryExpression;
	private Taxonomy retrievedTaxonomy;
	private Language language;
	private String taxonomyCode;

	public TaxonomyManagementSearchPresenter(TaxonomyManagementSearchView view) {
		super(view);
		this.language = Language.withCode(view.getSessionContext().getCurrentLocale().getLanguage());
	}

	public TaxonomyManagementSearchPresenter forParams(String parameters) {
		Map<String, String> params = ParamUtils.getParamsMap(parameters);
		String taxonomyCode = params.get(TAXONOMY_CODE);
		this.taxonomyCode = taxonomyCode;
		queryExpression = params.get(QUERY);
		taxonomy = new TaxonomyToVOBuilder().build(fetchTaxonomy(taxonomyCode));
		taxonomiesSearchServices = modelLayerFactory.newTaxonomiesSearchService();
		retrievedTaxonomy = fetchTaxonomy(taxonomy.getCode());
		return this;
	}

	public boolean canOnlyConsultTaxonomy() {
		TaxonomyPresentersService presentersService = new TaxonomyPresentersService(appLayerFactory);

		if (presentersService.canManage(taxonomyCode, getCurrentUser())) {
			return false;
		}

		return presentersService.canConsult(taxonomyCode, getCurrentUser());
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
					query.filteredWithUserRead(getCurrentUser());
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
			public LogicalSearchQuery getQuery() {
				return queryFactory.get();
			}
		};
		return dataProvider;
	}

	private LogicalSearchQuery getLogicalSearchQueryForSchema(MetadataSchema schema, LogicalSearchQuery query) {
		return new LogicalSearchQuery(
				query.getCondition().andWhere(Schemas.IDENTIFIER).isNotNull().withFilters(new SchemaFilters(schema)));
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
		ValidationErrors validationErrors = validateDeletable(recordVO);
		if (validationErrors.isEmpty()) {
			SchemaPresenterUtils utils = new SchemaPresenterUtils(recordVO.getSchema().getCode(), view.getConstellioFactories(),
					view.getSessionContext());
			try {
				utils.delete(utils.toRecord(recordVO), null, false);
			} catch (OptimisticLockException e) {
				LOGGER.error(e.getMessage());
				view.showErrorMessage(e.getMessage());
			}
			view.refreshTable();
		} else {
			displayErrorWindow(validationErrors);
		}
	}

	private void displayErrorWindow(ValidationErrors validationErrors) {
		MessageUtils.getCannotDeleteWindow(validationErrors).openWindow();
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
		return new TaxonomyPresentersService(appLayerFactory).canManage(taxonomyCode, user) ||
			   new TaxonomyPresentersService(appLayerFactory).canConsult(taxonomyCode, user);
	}

	public void viewAssembled() {
	}

	public ValidationErrors validateDeletable(RecordVO entity) {
		Record record = presenterService().getRecord(entity.getId());
		User user = getCurrentUser();
		return recordServices().validateLogicallyThenPhysicallyDeletable(record, user);
	}

	public String getDefaultOrderField() {
		Taxonomy taxonomy = fetchTaxonomy(getTaxonomy().getCode());
		return appCollectionExtentions.getSortMetadataCode(taxonomy);
	}

	public void searchConcept(String freeText) {
		view.navigate().to().taxonomySearch(taxonomy.getCode(), freeText);
	}

	public String getQueryExpression() {
		return queryExpression;
	}
}
