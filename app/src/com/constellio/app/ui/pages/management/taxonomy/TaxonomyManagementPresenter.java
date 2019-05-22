package com.constellio.app.ui.pages.management.taxonomy;

import com.constellio.app.api.extensions.taxonomies.GetTaxonomyExtraFieldsParam;
import com.constellio.app.api.extensions.taxonomies.GetTaxonomyManagementClassifiedTypesParams;
import com.constellio.app.api.extensions.taxonomies.TaxonomyExtraField;
import com.constellio.app.api.extensions.taxonomies.TaxonomyManagementClassifiedType;
import com.constellio.app.modules.rm.navigation.RMViews;
import com.constellio.app.modules.rm.wrappers.Folder;
import com.constellio.app.modules.rm.wrappers.RetentionRule;
import com.constellio.app.services.factories.ConstellioFactories;
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
import com.constellio.app.ui.pages.base.SessionContext;
import com.constellio.app.ui.pages.management.sequence.SequenceServices;
import com.constellio.app.ui.params.ParamUtils;
import com.constellio.app.ui.util.MessageUtils;
import com.constellio.data.utils.Factory;
import com.constellio.model.entities.Language;
import com.constellio.model.entities.Taxonomy;
import com.constellio.model.entities.records.Record;
import com.constellio.model.entities.records.wrappers.User;
import com.constellio.model.entities.schemas.Metadata;
import com.constellio.model.entities.schemas.MetadataSchema;
import com.constellio.model.entities.schemas.MetadataSchemaTypes;
import com.constellio.model.entities.schemas.Schemas;
import com.constellio.model.frameworks.validation.ValidationErrors;
import com.constellio.model.services.records.SchemasRecordsServices;
import com.constellio.model.services.search.StatusFilter;
import com.constellio.model.services.search.query.logical.LogicalSearchQuery;
import com.constellio.model.services.search.query.logical.condition.SchemaFilters;
import com.constellio.model.services.taxonomies.ConceptNodesTaxonomySearchServices;
import com.constellio.model.services.taxonomies.TaxonomiesManager;
import com.constellio.model.services.taxonomies.TaxonomiesSearchOptions;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static com.constellio.model.services.records.RecordUtils.parentPaths;
import static com.constellio.model.services.taxonomies.ConceptNodesTaxonomySearchServices.childNodesQuery;

public class TaxonomyManagementPresenter extends BasePresenter<TaxonomyManagementView> {

	public static final String TAXONOMY_CODE = "taxonomyCode";
	public static final String CONCEPT_ID = "conceptId";

	private MetadataSchemaToVOBuilder schemaVOBuilder = new MetadataSchemaToVOBuilder();
	TaxonomyVO taxonomy;
	String conceptId;
	String taxonomyCode;
	SchemasRecordsServices schemasRecordsServices;
	Language language;

	private transient SequenceServices sequenceServices;

	public TaxonomyManagementPresenter(TaxonomyManagementView view) {

		super(view);
		language = Language.withCode(view.getSessionContext().getCurrentLocale().getLanguage());
		initTransientObjects();
	}

	private void readObject(java.io.ObjectInputStream stream)
			throws IOException, ClassNotFoundException {
		stream.defaultReadObject();
		initTransientObjects();
	}

	private void initTransientObjects() {
		ConstellioFactories constellioFactories = view.getConstellioFactories();
		SessionContext sessionContext = view.getSessionContext();
		sequenceServices = new SequenceServices(constellioFactories, sessionContext);
		schemasRecordsServices = new SchemasRecordsServices(collection, modelLayerFactory);
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
			for (MetadataSchema schema : schemaTypes.getSchemaType(schemaType).getAllSchemasSortedByCode()) {
				createDataProviderIfSchemaCanBeChildOfCurrentConcept(dataProviders, currentSchema, schema);
			}
		}
		return dataProviders;
	}

	private void createDataProviderIfSchemaCanBeChildOfCurrentConcept(List<RecordVODataProvider> dataProviders,
																	  MetadataSchema currentSchema,
																	  MetadataSchema schema) {
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
		final String collection = schema.getCollection();
		Factory<LogicalSearchQuery> queryFactory = new Factory<LogicalSearchQuery>() {
			@Override
			public LogicalSearchQuery get() {
				MetadataSchemaTypes types = modelLayerFactory.getMetadataSchemasManager().getSchemaTypes(collection);
				LogicalSearchQuery query;
				if (conceptId != null) {
					Record record = recordServices().getDocumentById(conceptId);
					query = childNodesQuery(record, new TaxonomiesSearchOptions(), types);
				} else {
					query = new ConceptNodesTaxonomySearchServices(modelLayerFactory)
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

		MetadataSchemaVO schemaVO = schemaVOBuilder.build(
				schema(schemaCode), VIEW_MODE.TABLE, null, view.getSessionContext());
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

	public boolean canOnlyConsultTaxonomy() {
		TaxonomyPresentersService presentersService = new TaxonomyPresentersService(appLayerFactory);

		if(presentersService.canManage(taxonomyCode, getCurrentUser())) {
			return false;
		}

		return presentersService.canConsult(taxonomyCode, getCurrentUser());
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

	public String getMultiLangualTitle() {
		if (conceptId == null) {
			return null;
		}
		MetadataSchema metadataSchema = schemasRecordsServices.schema(getCurrentConcept().getRecord().getSchemaCode());
		Metadata metadata = metadataSchema.getMetadata(Schemas.TITLE_CODE);

		return getCurrentConcept().getRecord().get(metadata, view.getSessionContext().getCurrentLocale());
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
			utils.delete(utils.toRecord(recordVO), null, true);
			if (recordVO.getId().equals(conceptId)) {
				backButtonClicked();
			} else {
				view.refreshTable();
			}
		} else {
			displayErrorWindow(validationErrors);
		}
	}

	protected void displayErrorWindow(ValidationErrors validationErrors) {
		MessageUtils.getCannotDeleteWindow(validationErrors).openWindow();
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
			String[] pathParts = ((String) parentPaths(taxonomyPresenterUtils.getRecord(conceptId)).get(0)).split("/");
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
		return new TaxonomyPresentersService(appLayerFactory).canManage(taxonomyCode, user) ||
				new TaxonomyPresentersService(appLayerFactory).canConsult(taxonomyCode, user);
	}

	public void tabElementClicked(RecordVO recordVO) {
		//TODO BIG BIG Refactoring!

		if (recordVO.getSchema().getCode().contains(Folder.SCHEMA_TYPE)) {
			view.navigate().to(RMViews.class).displayFolder(recordVO.getId());
		}

		if (recordVO.getSchema().getCode().contains(RetentionRule.SCHEMA_TYPE)) {
			view.navigate().to(RMViews.class).displayRetentionRule(recordVO.getId());
		}

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

	public boolean hasCurrentUserAccessToCurrentConcept() {
		if (conceptId == null) {
			return true;

		} else {
			Record record = modelLayerFactory.newRecordServices().getDocumentById(conceptId);
			return getCurrentUser().hasDeleteAccess().on(record);
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

	public ValidationErrors validateDeletable(RecordVO entity) {
		Record record = presenterService().getRecord(entity.getId());
		User user = getCurrentUser();
		return recordServices().validateLogicallyThenPhysicallyDeletable(record, user);
	}

	public String getDefaultOrderField() {
		return Schemas.CODE.getLocalCode();
	}

	public void searchConcept(String freeText) {
		view.navigate().to().taxonomySearch(taxonomyCode, freeText);
	}

	public boolean isSequenceTable(RecordVO recordVO) {
		return !sequenceServices.getAvailableSequences(recordVO.getId()).isEmpty();
	}

}
