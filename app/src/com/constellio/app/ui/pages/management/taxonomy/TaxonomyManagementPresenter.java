package com.constellio.app.ui.pages.management.taxonomy;

import static com.constellio.app.ui.i18n.i18n.$;
import static com.constellio.model.services.records.RecordUtils.parentPaths;
import static com.constellio.model.services.search.query.logical.LogicalSearchQueryOperators.containingText;
import static com.constellio.model.services.search.query.logical.LogicalSearchQueryOperators.from;
import static com.constellio.model.services.search.query.logical.LogicalSearchQueryOperators.where;
import static com.constellio.model.services.taxonomies.ConceptNodesTaxonomySearchServices.childNodesQuery;
import static java.util.Arrays.asList;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import com.constellio.app.api.extensions.taxonomies.GetTaxonomyExtraFieldsParam;
import com.constellio.app.api.extensions.taxonomies.GetTaxonomyManagementClassifiedTypesParams;
import com.constellio.app.api.extensions.taxonomies.TaxonomyExtraField;
import com.constellio.app.api.extensions.taxonomies.TaxonomyManagementClassifiedType;
import com.constellio.app.modules.rm.navigation.RMViews;
import com.constellio.app.modules.rm.services.RMRecordDeletionServices;
import com.constellio.app.modules.rm.services.RMSchemasRecordsServices;
import com.constellio.app.modules.rm.wrappers.AdministrativeUnit;
import com.constellio.app.modules.rm.wrappers.DecommissioningList;
import com.constellio.app.modules.rm.wrappers.Folder;
import com.constellio.app.modules.rm.wrappers.RMTask;
import com.constellio.app.modules.rm.wrappers.RetentionRule;
import com.constellio.app.modules.tasks.services.TasksSchemasRecordsServices;
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
import com.constellio.data.dao.services.bigVault.SearchResponseIterator;
import com.constellio.data.utils.Factory;
import com.constellio.model.entities.Taxonomy;
import com.constellio.model.entities.records.Record;
import com.constellio.model.entities.records.wrappers.User;
import com.constellio.model.entities.schemas.Metadata;
import com.constellio.model.entities.schemas.MetadataSchema;
import com.constellio.model.entities.schemas.MetadataSchemaTypes;
import com.constellio.model.entities.schemas.Schemas;
import com.constellio.model.services.records.RecordUtils;
import com.constellio.model.services.search.SearchServices;
import com.constellio.model.services.search.StatusFilter;
import com.constellio.model.services.search.query.logical.LogicalSearchQuery;
import com.constellio.model.services.search.query.logical.condition.SchemaFilters;
import com.constellio.model.services.taxonomies.ConceptNodesTaxonomySearchServices;
import com.constellio.model.services.taxonomies.TaxonomiesManager;
import com.constellio.model.services.taxonomies.TaxonomiesSearchOptions;

public class TaxonomyManagementPresenter extends BasePresenter<TaxonomyManagementView> {

	public static final String TAXONOMY_CODE = "taxonomyCode";
	public static final String CONCEPT_ID = "conceptId";

	private MetadataSchemaToVOBuilder schemaVOBuilder = new MetadataSchemaToVOBuilder();
	TaxonomyVO taxonomy;
	String conceptId;
	String taxonomyCode;

	private transient SequenceServices sequenceServices;

	public TaxonomyManagementPresenter(TaxonomyManagementView view) {
		super(view);
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
					query = childNodesQuery(record, new TaxonomiesSearchOptions());
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
			utils.delete(utils.toRecord(recordVO), null, true);
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

	public void cleanAdministrativeUnitButtonClicked() {
		if (hasCurrentUserRequiredRightsToCleanAdminUnitChilds()) {
			RecordVO recordVO = getCurrentConcept();
			RMRecordDeletionServices.cleanAdministrativeUnit(view.getCollection(), recordVO.getId(), appLayerFactory);
		}
	}

	private boolean hasCurrentUserRequiredRightsToCleanAdminUnitChilds() {
		RMSchemasRecordsServices rm = new RMSchemasRecordsServices(collection, appLayerFactory);
		SearchServices searchServices = appLayerFactory.getModelLayerFactory().newSearchServices();

		AdministrativeUnit administrativeUnit = rm.wrapAdministrativeUnit(searchServices.
				searchSingleResult(from(rm.administrativeUnit.schema()).where(Schemas.IDENTIFIER)
						.isEqualTo(getCurrentConcept().getId())));

		boolean hasAllRights = hasCurrentUserDeletionRightsToCleanTasks(administrativeUnit);
		if (hasAllRights) {
			hasAllRights = hasCurrentUserDeletionRightsToCleanFolders(administrativeUnit);
		}
		if (hasAllRights) {
			hasAllRights = hasCurrentUserDeletionRightsToCleanContainers(administrativeUnit);
		}
		return hasAllRights;
	}

	private boolean hasCurrentUserDeletionRightsToCleanFolders(AdministrativeUnit administrativeUnit) {

		RMSchemasRecordsServices rm = new RMSchemasRecordsServices(collection, appLayerFactory);
		SearchServices searchServices = appLayerFactory.getModelLayerFactory().newSearchServices();
		TasksSchemasRecordsServices taskSchemas = new TasksSchemasRecordsServices(collection, appLayerFactory);

		SearchResponseIterator<Record> documentIterator = searchServices
				.recordsIterator(new LogicalSearchQuery().setCondition(from(rm.document.schema())
						.where(Schemas.PRINCIPAL_PATH).isContainingText(administrativeUnit.getId()))
						.sortDesc(Schemas.PRINCIPAL_PATH));
		SearchResponseIterator<Record> folderIterator = searchServices
				.recordsIterator(new LogicalSearchQuery().setCondition(from(rm.folder.schema())
						.where(Schemas.PRINCIPAL_PATH).isContainingText(administrativeUnit.getId()))
						.sortDesc(Schemas.PRINCIPAL_PATH));
		List<Record> taskList = searchServices.search(new LogicalSearchQuery().setCondition(from(taskSchemas.userTask.schema())
				.where(Schemas.PRINCIPAL_PATH).isNot(containingText(administrativeUnit.getId())))
				.sortDesc(Schemas.PRINCIPAL_PATH));

		while (documentIterator.hasNext()) {
			Record document = documentIterator.next();
			if (!hasCurrentUserWriteRightsToUnlinkDocumentFromDecommissioningLists(document)) {
				return false;
			}
			if (!hasCurrentUserWriteRightsToUnlinkDocumentFromTask(document, taskList)) {
				return false;
			}
			if (!getCurrentUser().hasDeleteAccess().on(document)) {
				return false;
			}
		}
		while (folderIterator.hasNext()) {
			Record folder = folderIterator.next();
			if (!hasCurrentUserWriteRightsToUnlinkFolderFromDecommissioningLists(folder)) {
				return false;
			}
			if (!hasCurrentUserWriteRightsToUnlinkFolderFromTask(folder, taskList)) {
				return false;
			}
			if (!getCurrentUser().hasDeleteAccess().on(folder)) {
				return false;
			}
		}
		return true;
	}

	private boolean hasCurrentUserDeletionRightsToCleanContainers(AdministrativeUnit administrativeUnit) {

		RMSchemasRecordsServices rm = new RMSchemasRecordsServices(collection, appLayerFactory);
		SearchServices searchServices = appLayerFactory.getModelLayerFactory().newSearchServices();

		SearchResponseIterator<Record> containerIterator = searchServices
				.recordsIterator(new LogicalSearchQuery().setCondition(from(rm.containerRecord.schema())
						.where(Schemas.PRINCIPAL_PATH).isContainingText(administrativeUnit.getId()))
						.sortDesc(Schemas.PRINCIPAL_PATH));

		while (containerIterator.hasNext()) {
			Record container = containerIterator.next();
			if (!hasCurrentUserWriteRightsToUnlinkContainerFromDecommissioningLists(container)) {
				return false;
			}
			if (!getCurrentUser().hasDeleteAccess().on(container)) {
				return false;
			}
		}
		return true;
	}

	private boolean hasCurrentUserDeletionRightsToCleanTasks(AdministrativeUnit administrativeUnit) {

		SearchServices searchServices = searchServices();
		TasksSchemasRecordsServices schemas = new TasksSchemasRecordsServices(collection, appLayerFactory);
		LogicalSearchQuery query = new LogicalSearchQuery().setCondition(from(schemas.userTask.schema())
				.where(Schemas.PRINCIPAL_PATH).isContainingText(administrativeUnit.getId())).sortDesc(Schemas.PRINCIPAL_PATH);

		SearchResponseIterator<Record> userTaskIterator = searchServices.recordsIterator(query);
		while (userTaskIterator.hasNext()) {
			Record userTask = userTaskIterator.next();
			if (!getCurrentUser().hasDeleteAccess().on(userTask)) {
				return false;
			}
		}
		return true;
	}

	private boolean hasCurrentUserWriteRightsToUnlinkDocumentFromDecommissioningLists(Record document) {

		RMSchemasRecordsServices rm = new RMSchemasRecordsServices(collection, appLayerFactory);
		List<DecommissioningList> decommissioningLists = rm.searchDecommissioningLists(
				where(rm.decommissioningList.documents()).isContaining(asList(document.getId())));
		for (DecommissioningList decommissioningList : decommissioningLists) {
			if (!getCurrentUser().hasWriteAccess().on(decommissioningList)) {
				return false;
			}
		}
		return true;
	}

	private boolean hasCurrentUserWriteRightsToUnlinkFolderFromDecommissioningLists(Record folder) {

		RMSchemasRecordsServices rm = new RMSchemasRecordsServices(collection, appLayerFactory);
		List<DecommissioningList> decommissioningLists = rm.searchDecommissioningLists(
				where(rm.decommissioningList.folders()).isContaining(asList(folder.getId())));
		for (DecommissioningList decommissioningList : decommissioningLists) {
			if (!getCurrentUser().hasWriteAccess().on(decommissioningList)) {
				return false;
			}
		}
		return true;
	}

	private boolean hasCurrentUserWriteRightsToUnlinkContainerFromDecommissioningLists(Record container) {

		RMSchemasRecordsServices rm = new RMSchemasRecordsServices(collection, appLayerFactory);
		List<DecommissioningList> decommissioningLists = rm.searchDecommissioningLists(
				where(rm.decommissioningList.containers()).isContaining(asList(container.getId())));
		for (DecommissioningList decommissioningList : decommissioningLists) {
			if (!getCurrentUser().hasWriteAccess().on(decommissioningList)) {
				return false;
			}
		}
		return true;
	}

	private boolean hasCurrentUserWriteRightsToUnlinkDocumentFromTask(Record document, List<Record> taskList) {

		for (Record task : taskList) {
			MetadataSchema curTaskSchema = appLayerFactory.getModelLayerFactory().getMetadataSchemasManager()
					.getSchemaTypes(collection).getSchema(task.getSchemaCode());
			List<String> linkedDocumentsIDs = task.get(curTaskSchema.getMetadata(RMTask.LINKED_DOCUMENTS));
			linkedDocumentsIDs = new ArrayList<>(linkedDocumentsIDs);
			if (linkedDocumentsIDs.contains(document.getId())) {
				if (!getCurrentUser().hasWriteAccess().on(task)) {
					return false;
				}
			}
		}
		return true;
	}

	private boolean hasCurrentUserWriteRightsToUnlinkFolderFromTask(Record folder, List<Record> taskList) {

		for (Record task : taskList) {
			MetadataSchema curTaskSchema = appLayerFactory.getModelLayerFactory().getMetadataSchemasManager()
					.getSchemaTypes(collection).getSchema(task.getSchemaCode());
			List<String> linkedDocumentsIDs = task.get(curTaskSchema.getMetadata(RMTask.LINKED_DOCUMENTS));
			linkedDocumentsIDs = new ArrayList<>(linkedDocumentsIDs);
			if (linkedDocumentsIDs.contains(folder.getId())) {
				if (!getCurrentUser().hasWriteAccess().on(task)) {
					return false;
				}
			}
		}
		return true;
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
		return new TaxonomyPresentersService(appLayerFactory).canManage(taxonomyCode, user);
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

	public boolean isSequenceTable(RecordVO recordVO) {
		return !sequenceServices.getAvailableSequences(recordVO.getId()).isEmpty();
	}

}
