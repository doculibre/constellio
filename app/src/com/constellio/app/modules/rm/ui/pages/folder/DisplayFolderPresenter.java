package com.constellio.app.modules.rm.ui.pages.folder;

import com.constellio.app.api.extensions.params.DocumentFolderBreadCrumbParams;
import com.constellio.app.modules.rm.ConstellioRMModule;
import com.constellio.app.modules.rm.RMConfigs;
import com.constellio.app.modules.rm.constants.RMPermissionsTo;
import com.constellio.app.modules.rm.extensions.api.RMModuleExtensions;
import com.constellio.app.modules.rm.model.enums.DefaultTabInFolderDisplay;
import com.constellio.app.modules.rm.model.labelTemplate.LabelTemplate;
import com.constellio.app.modules.rm.navigation.RMNavigationConfiguration;
import com.constellio.app.modules.rm.navigation.RMViews;
import com.constellio.app.modules.rm.services.EmailParsingServices;
import com.constellio.app.modules.rm.services.RMSchemasRecordsServices;
import com.constellio.app.modules.rm.services.borrowingServices.BorrowingServices;
import com.constellio.app.modules.rm.services.borrowingServices.BorrowingType;
import com.constellio.app.modules.rm.services.decommissioning.SearchType;
import com.constellio.app.modules.rm.services.events.RMEventsSearchServices;
import com.constellio.app.modules.rm.ui.builders.DocumentToVOBuilder;
import com.constellio.app.modules.rm.ui.builders.FolderToVOBuilder;
import com.constellio.app.modules.rm.ui.components.breadcrumb.FolderDocumentContainerBreadcrumbTrail;
import com.constellio.app.modules.rm.ui.components.content.ConstellioAgentClickHandler;
import com.constellio.app.modules.rm.ui.entities.FolderVO;
import com.constellio.app.modules.rm.ui.pages.decommissioning.DecommissioningBuilderViewImpl;
import com.constellio.app.modules.rm.ui.pages.decommissioning.breadcrumb.DecommissionBreadcrumbTrail;
import com.constellio.app.modules.rm.ui.util.ConstellioAgentUtils;
import com.constellio.app.modules.rm.util.RMNavigationUtils;
import com.constellio.app.modules.rm.wrappers.Cart;
import com.constellio.app.modules.rm.wrappers.ContainerRecord;
import com.constellio.app.modules.rm.wrappers.Document;
import com.constellio.app.modules.rm.wrappers.Folder;
import com.constellio.app.modules.rm.wrappers.RMTask;
import com.constellio.app.modules.tasks.TasksPermissionsTo;
import com.constellio.app.modules.tasks.model.wrappers.BetaWorkflow;
import com.constellio.app.modules.tasks.model.wrappers.Task;
import com.constellio.app.modules.tasks.navigation.TaskViews;
import com.constellio.app.modules.tasks.services.BetaWorkflowServices;
import com.constellio.app.modules.tasks.services.TasksSchemasRecordsServices;
import com.constellio.app.services.factories.AppLayerFactory;
import com.constellio.app.ui.application.Navigation;
import com.constellio.app.ui.entities.ContentVersionVO;
import com.constellio.app.ui.entities.ContentVersionVO.InputStreamProvider;
import com.constellio.app.ui.entities.FacetVO;
import com.constellio.app.ui.entities.MetadataSchemaVO;
import com.constellio.app.ui.entities.MetadataVO;
import com.constellio.app.ui.entities.RecordVO;
import com.constellio.app.ui.entities.RecordVO.VIEW_MODE;
import com.constellio.app.ui.framework.builders.EventToVOBuilder;
import com.constellio.app.ui.framework.builders.MetadataSchemaToVOBuilder;
import com.constellio.app.ui.framework.builders.MetadataToVOBuilder;
import com.constellio.app.ui.framework.builders.RecordToVOBuilder;
import com.constellio.app.ui.framework.components.ComponentState;
import com.constellio.app.ui.framework.components.RMSelectionPanelReportPresenter;
import com.constellio.app.ui.framework.components.breadcrumb.BaseBreadcrumbTrail;
import com.constellio.app.ui.framework.data.RecordVODataProvider;
import com.constellio.app.ui.pages.base.SchemaPresenterUtils;
import com.constellio.app.ui.pages.base.SingleSchemaBasePresenter;
import com.constellio.app.ui.pages.search.SearchPresenter.SortOrder;
import com.constellio.app.ui.pages.search.SearchPresenterService;
import com.constellio.app.ui.params.ParamUtils;
import com.constellio.data.dao.services.bigVault.SearchResponseIterator;
import com.constellio.data.io.services.facades.IOServices;
import com.constellio.data.utils.KeySetMap;
import com.constellio.data.utils.TimeProvider;
import com.constellio.model.entities.CorePermissions;
import com.constellio.model.entities.records.Content;
import com.constellio.model.entities.records.ContentVersion;
import com.constellio.model.entities.records.Record;
import com.constellio.model.entities.records.RecordUpdateOptions;
import com.constellio.model.entities.records.Transaction;
import com.constellio.model.entities.records.wrappers.EmailToSend;
import com.constellio.model.entities.records.wrappers.Facet;
import com.constellio.model.entities.records.wrappers.User;
import com.constellio.model.entities.records.wrappers.structure.FacetType;
import com.constellio.model.entities.schemas.Metadata;
import com.constellio.model.entities.schemas.MetadataSchema;
import com.constellio.model.entities.schemas.MetadataSchemaType;
import com.constellio.model.entities.schemas.MetadataSchemaTypes;
import com.constellio.model.entities.schemas.Schemas;
import com.constellio.model.extensions.ModelLayerCollectionExtensions;
import com.constellio.model.services.configs.SystemConfigurationsManager;
import com.constellio.model.services.contents.ContentManager;
import com.constellio.model.services.contents.ContentManager.UploadOptions;
import com.constellio.model.services.contents.ContentVersionDataSummary;
import com.constellio.model.services.contents.icap.IcapException;
import com.constellio.model.services.migrations.ConstellioEIMConfigs;
import com.constellio.model.services.records.RecordServices;
import com.constellio.model.services.records.RecordServicesException;
import com.constellio.model.services.records.RecordServicesRuntimeException;
import com.constellio.model.services.records.SchemasRecordsServices;
import com.constellio.model.services.schemas.MetadataSchemasManager;
import com.constellio.model.services.schemas.SchemaUtils;
import com.constellio.model.services.search.SearchServices;
import com.constellio.model.services.search.StatusFilter;
import com.constellio.model.services.search.query.ReturnedMetadatasFilter;
import com.constellio.model.services.search.query.logical.FunctionLogicalSearchQuerySort;
import com.constellio.model.services.search.query.logical.LogicalSearchQuery;
import com.constellio.model.services.search.query.logical.LogicalSearchQueryFacetFilters;
import com.constellio.model.services.search.query.logical.LogicalSearchQueryOperators;
import com.constellio.model.services.search.query.logical.LogicalSearchQuerySort;
import com.constellio.model.services.search.query.logical.QueryExecutionMethod;
import com.constellio.model.services.search.query.logical.condition.LogicalSearchCondition;
import com.constellio.model.utils.Lazy;
import org.apache.commons.lang3.StringUtils;
import org.joda.time.LocalDate;
import org.joda.time.LocalDateTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import static com.constellio.app.modules.tasks.model.wrappers.Task.STARRED_BY_USERS;
import static com.constellio.app.ui.i18n.i18n.$;
import static com.constellio.model.services.contents.ContentFactory.isFilename;
import static com.constellio.model.services.search.query.logical.LogicalSearchQueryOperators.from;
import static java.util.Arrays.asList;
import static org.apache.commons.lang.StringUtils.isNotBlank;

public class DisplayFolderPresenter extends SingleSchemaBasePresenter<DisplayFolderView> {

	private static final int WAIT_ONE_SECOND = 1;
	private static final long NUMBER_OF_FOLDERS_IN_CART_LIMIT = 1000;
	private static Logger LOGGER = LoggerFactory.getLogger(DisplayFolderPresenter.class);

	private RecordVODataProvider folderContentDataProvider;
	//	private RecordVODataProvider subFoldersDataProvider;
	//	private RecordVODataProvider documentsDataProvider;
	private RecordVODataProvider tasksDataProvider;
	private RecordVODataProvider eventsDataProvider;
	private MetadataSchemaToVOBuilder schemaVOBuilder = new MetadataSchemaToVOBuilder();
	private FolderToVOBuilder folderVOBuilder;
	private DocumentToVOBuilder documentVOBuilder;
	private List<String> documentTitles = new ArrayList<>();

	private FolderVO summaryFolderVO;
	private Lazy<FolderVO> lazyFullFolderVO;

	private MetadataSchemaVO tasksSchemaVO;

	private transient RMConfigs rmConfigs;
	private transient RMSchemasRecordsServices rmSchemasRecordsServices;
	private transient BorrowingServices borrowingServices;
	private transient MetadataSchemasManager metadataSchemasManager;
	private transient RecordServices recordServices;
	private transient ModelLayerCollectionExtensions extensions;
	private transient RMModuleExtensions rmModuleExtensions;
	private transient ConstellioEIMConfigs eimConfigs;
	private String taxonomyCode;
	private User user;
	transient SearchPresenterService service;
	private SchemaPresenterUtils presenterUtilsForDocument;

	protected RecordToVOBuilder voBuilder = new RecordToVOBuilder();

	private Set<String> selectedRecordIds = new HashSet<>();

	Boolean allItemsSelected = false;

	Boolean allItemsDeselected = false;

	private boolean nestedView;

	private boolean applyButtonFacetEnabled = false;

	private boolean inWindow;

	private Map<String, String> params = null;

	KeySetMap<String, String> facetSelections = new KeySetMap<>();
	Map<String, Boolean> facetStatus = new HashMap<>();
	String sortCriterion;
	SortOrder sortOrder = SortOrder.ASCENDING;

	private RecordVO returnRecordVO;
	private Integer returnIndex;

	public DisplayFolderPresenter(DisplayFolderView view, RecordVO recordVO, boolean nestedView, boolean inWindow) {
		super(view, Folder.DEFAULT_SCHEMA);
		this.nestedView = nestedView;
		this.inWindow = inWindow;
		presenterUtilsForDocument = new SchemaPresenterUtils(Document.DEFAULT_SCHEMA, view.getConstellioFactories(), view.getSessionContext());
		initTransientObjects();
		if (recordVO != null) {
			this.taxonomyCode = recordVO.getId();
			forParams(recordVO.getId());
		}
	}

	private void readObject(java.io.ObjectInputStream stream)
			throws IOException, ClassNotFoundException {
		stream.defaultReadObject();
		initTransientObjects();
	}

	private void initTransientObjects() {
		rmSchemasRecordsServices = new RMSchemasRecordsServices(collection, appLayerFactory);
		borrowingServices = new BorrowingServices(collection, modelLayerFactory);
		folderVOBuilder = new FolderToVOBuilder();
		documentVOBuilder = new DocumentToVOBuilder(modelLayerFactory);
		metadataSchemasManager = modelLayerFactory.getMetadataSchemasManager();
		recordServices = modelLayerFactory.newRecordServices();
		extensions = modelLayerFactory.getExtensions().forCollection(collection);
		rmModuleExtensions = appLayerFactory.getExtensions().forCollection(collection).forModule(ConstellioRMModule.ID);
		rmConfigs = new RMConfigs(modelLayerFactory.getSystemConfigurationsManager());
		eimConfigs = new ConstellioEIMConfigs(modelLayerFactory.getSystemConfigurationsManager());
		applyButtonFacetEnabled = getCurrentUser().isApplyFacetsEnabled();
		user = appLayerFactory.getModelLayerFactory().newUserServices().getUserInCollection(view.getSessionContext().getCurrentUser().getUsername(), collection);
		List<MetadataSchemaType> types = Arrays.asList(getFoldersSchemaType(), getDocumentsSchemaType());
		service = new SearchPresenterService(collection, user, modelLayerFactory, types);
	}

	public RecordVODataProvider getFolderContentDataProvider() {
		return folderContentDataProvider;
	}

	public User getUser() {
		return user;
	}

	protected void setTaxonomyCode(String taxonomyCode) {
		this.taxonomyCode = taxonomyCode;
	}

	@Override
	protected boolean hasPageAccess(String params, User user) {
		return true;
	}

	public void forParams(String params) {
		String id;

		Map<String, String> lParamsAsMap = ParamUtils.getParamsMap(params);
		if (lParamsAsMap.size() > 0) {
			this.params = ParamUtils.getParamsMap(params);
			id = this.params.get("id");
		} else {
			id = params;
		}

		view.getSessionContext().addVisited(id);

		String taxonomyCode = view.getUIContext().getAttribute(FolderDocumentContainerBreadcrumbTrail.TAXONOMY_CODE);
		this.setTaxonomyCode(taxonomyCode);
		view.setTaxonomyCode(taxonomyCode);

		Record summaryRecord;
		try {
			summaryRecord = modelLayerFactory.newRecordServices().realtimeGetRecordSummaryById(id);

		} catch (RecordServicesRuntimeException.NoSuchRecordWithId ignored) {
			summaryRecord = getRecord(id);
		}
		this.summaryFolderVO = folderVOBuilder.build(summaryRecord, VIEW_MODE.TABLE, view.getSessionContext());
		this.lazyFullFolderVO = new Lazy<FolderVO>() {
			@Override
			protected FolderVO load() {
				Record record = getRecord(id);
				return folderVOBuilder.build(record, VIEW_MODE.DISPLAY, view.getSessionContext());
			}
		};

		setSchemaCode(summaryRecord.getSchemaCode());
		view.setSummaryRecord(summaryFolderVO);

		MetadataSchemaVO foldersSchemaVO = schemaVOBuilder.build(defaultSchema(), VIEW_MODE.TABLE, view.getSessionContext());
		MetadataSchema documentsSchema = getDocumentsSchema();
		MetadataSchemaVO documentsSchemaVO = schemaVOBuilder.build(documentsSchema, VIEW_MODE.TABLE, view.getSessionContext());

		Map<String, RecordToVOBuilder> voBuilders = new HashMap<>();
		voBuilders.put(foldersSchemaVO.getCode(), folderVOBuilder);
		voBuilders.put(documentsSchemaVO.getCode(), documentVOBuilder);
		folderContentDataProvider = new RecordVODataProvider(Arrays.asList(foldersSchemaVO, documentsSchemaVO), voBuilders, modelLayerFactory, view.getSessionContext()) {
			@Override
			public LogicalSearchQuery getQuery() {
				return getFolderContentQuery();
			}

			@Override
			public boolean isSearchCache() {
				return eimConfigs.isOnlySummaryMetadatasDisplayedInTables();
			}
		};
		//		folderContentDataProvider = new SearchResultVODataProvider(new RecordToVOBuilder(), appLayerFactory, view.getSessionContext()) {
		//			@Override
		//			public LogicalSearchQuery getQuery() {
		//				return getFolderContentQuery();
		//			}
		//		};

		tasksSchemaVO = schemaVOBuilder
				.build(getTasksSchema(), VIEW_MODE.TABLE, Arrays.asList(STARRED_BY_USERS), view.getSessionContext(), true);
		tasksDataProvider = new RecordVODataProvider(
				tasksSchemaVO, folderVOBuilder, modelLayerFactory, view.getSessionContext()) {
			@Override
			public LogicalSearchQuery getQuery() {
				LogicalSearchQuery query = getTasksQuery();

				if (searchServices().hasResults(query)) {
					addStarredSortToQuery(query);
					query.sortDesc(Schemas.MODIFIED_ON);
					return query;
				} else {
					return LogicalSearchQuery.returningNoResults();
				}
			}

			@Override
			protected void clearSort(LogicalSearchQuery query) {
				super.clearSort(query);
				addStarredSortToQuery(query);
			}
		};

		view.setFolderContent(folderContentDataProvider);
		view.setTasks(tasksDataProvider);

		if (hasCurrentUserPermissionToViewEvents()) {
			eventsDataProvider = getEventsDataProvider();
			view.setEvents(eventsDataProvider);
		}
	}

	public Map<String, String> getParams() {
		return params;
	}

	public String getFolderId() {
		return summaryFolderVO.getId();
	}

	LogicalSearchQuery getDocumentsQuery() {

		RMSchemasRecordsServices rm = new RMSchemasRecordsServices(collection, appLayerFactory);
		Folder folder = rm.getFolderSummary(summaryFolderVO.getId());
		List<String> referencedDocuments = new ArrayList<>();
		for (Metadata folderMetadata : folder.getSchema().getMetadatas().onlyReferencesToType(Document.SCHEMA_TYPE)) {
			referencedDocuments.addAll(folder.getWrappedRecord().<String>getValues(folderMetadata));
		}

		LogicalSearchCondition condition = from(rm.document.schemaType()).where(rm.document.folder()).is(folder);

		if (!referencedDocuments.isEmpty()) {
			condition = condition.orWhere(Schemas.IDENTIFIER).isIn(referencedDocuments);
		}

		LogicalSearchQuery query = new LogicalSearchQuery(condition);
		query.filteredWithUser(getCurrentUser());
		query.filteredByStatus(StatusFilter.ACTIVES);
		query.sortAsc(Schemas.TITLE);
		return query;
	}

	private LogicalSearchQuery getFolderContentQuery() {
		RMSchemasRecordsServices rm = new RMSchemasRecordsServices(collection, appLayerFactory);
		Folder folder = rm.getFolderSummary(summaryFolderVO.getId());

		List<String> referencedDocuments = new ArrayList<>();
		for (Metadata folderMetadata : folder.getSchema().getMetadatas().onlyReferencesToType(Document.SCHEMA_TYPE)) {
			referencedDocuments.addAll(folder.getWrappedRecord().<String>getValues(folderMetadata));
		}

		MetadataSchemaType foldersSchemaType = getFoldersSchemaType();
		MetadataSchemaType documentsSchemaType = getDocumentsSchemaType();

		LogicalSearchQuery query = new LogicalSearchQuery();

		LogicalSearchCondition condition = from(foldersSchemaType, documentsSchemaType).where(rm.folder.parentFolder()).is(folder).orWhere(rm.document.folder()).is(folder);

		if (!referencedDocuments.isEmpty()) {
			condition = condition.orWhere(Schemas.IDENTIFIER).isIn(referencedDocuments);
		}
		query.setCondition(condition);

		service.configureQueryToComputeFacets(query);

		SchemasRecordsServices schemas = new SchemasRecordsServices(collection, modelLayerFactory);
		LogicalSearchQueryFacetFilters filters = query.getFacetFilters();
		filters.clear();
		for (Entry<String, Set<String>> selection : facetSelections.getMapEntries()) {
			try {
				Facet facet = schemas.getFacet(selection.getKey());
				if (!selection.getValue().isEmpty()) {
					if (facet.getFacetType() == FacetType.FIELD) {
						filters.selectedFieldFacetValues(facet.getFieldDataStoreCode(), selection.getValue());
					} else if (facet.getFacetType() == FacetType.QUERY) {
						filters.selectedQueryFacetValues(facet.getId(), selection.getValue());
					}
				}
			} catch (RecordServicesRuntimeException.NoSuchRecordWithId id) {
				LOGGER.warn("Facet '" + id + "' has been deleted");
			}
		}

		query.filteredWithUser(getCurrentUser());
		query.filteredByStatus(StatusFilter.ACTIVES);
		// Folder, Document

		query.sortDesc(Schemas.SCHEMA);

		addSortCriteriaForFolderContentQuery(query);

		if (eimConfigs.isOnlySummaryMetadatasDisplayedInTables()) {
			LogicalSearchQuery folderCacheableQuery = new LogicalSearchQuery(from(foldersSchemaType)
					.where(rm.folder.parentFolder()).is(folder))
					.filteredWithUser(getCurrentUser())
					.filteredByStatus(StatusFilter.ACTIVES)
					.setReturnedMetadatas(ReturnedMetadatasFilter.onlySummaryFields());
			LogicalSearchQuery documentCacheableQuery = new LogicalSearchQuery(from(documentsSchemaType)
					.where(rm.document.folder()).is(folder))
					.filteredWithUser(getCurrentUser())
					.filteredByStatus(StatusFilter.ACTIVES)
					.setReturnedMetadatas(ReturnedMetadatasFilter.onlySummaryFields());

			addSortCriteriaForFolderContentQuery(folderCacheableQuery);
			addSortCriteriaForFolderContentQuery(documentCacheableQuery);

			query.setCacheableQueries(asList(folderCacheableQuery, documentCacheableQuery));
		}

		return query;
	}

	private void addSortCriteriaForFolderContentQuery(LogicalSearchQuery query) {
		if (sortCriterion == null) {
			if (sortOrder == SortOrder.ASCENDING) {
				query.sortAsc(Schemas.TITLE);
			} else {
				query.sortDesc(Schemas.TITLE);
			}
		} else {
			Metadata metadata = getMetadata(sortCriterion);
			if (sortOrder == SortOrder.ASCENDING) {
				query.sortAsc(metadata);
			} else {
				query.sortDesc(metadata);
			}
		}
	}

	private LogicalSearchQuery getTasksQuery() {
		TasksSchemasRecordsServices tasks = new TasksSchemasRecordsServices(collection, appLayerFactory);
		Metadata taskFolderMetadata = tasks.userTask.schema().getMetadata(RMTask.LINKED_FOLDERS);
		LogicalSearchQuery query = new LogicalSearchQuery();
		query.setCondition(from(tasks.userTask.schemaType()).where(taskFolderMetadata).is(summaryFolderVO.getId()));
		query.filteredByStatus(StatusFilter.ACTIVES);
		query.filteredWithUser(getCurrentUser());

		return query;
	}

	public void selectInitialTabForUser() {
		SystemConfigurationsManager systemConfigurationsManager = modelLayerFactory.getSystemConfigurationsManager();
		RMConfigs rmConfigs = new RMConfigs(systemConfigurationsManager);

		String userDefaultTabInFolderDisplayCode = getCurrentUser().getDefaultTabInFolderDisplay();
		String configDefaultTabInFolderDisplayCode = rmConfigs.getDefaultTabInFolderDisplay();
		String defaultTabInFolderDisplayCode = StringUtils.isNotBlank(userDefaultTabInFolderDisplayCode) ?
											   userDefaultTabInFolderDisplayCode :
											   configDefaultTabInFolderDisplayCode;
		if (isNotBlank(defaultTabInFolderDisplayCode)) {
			if (DefaultTabInFolderDisplay.METADATA.getCode().equals(defaultTabInFolderDisplayCode)) {
				view.selectMetadataTab();
			} else if (DefaultTabInFolderDisplay.CONTENT.getCode().equals(defaultTabInFolderDisplayCode)) {
				view.selectFolderContentTab();
			}
		}
	}

	public BaseBreadcrumbTrail getBreadCrumbTrail() {
		String saveSearchDecommissioningId = null;
		String searchTypeAsString = null;
		String favoritesId = null;

		Map<String, String> params = getParams();

		if (params != null) {
			if (params.get("decommissioningSearchId") != null) {
				saveSearchDecommissioningId = params.get("decommissioningSearchId");
				view.getUIContext()
						.setAttribute(DecommissioningBuilderViewImpl.SAVE_SEARCH_DECOMMISSIONING, saveSearchDecommissioningId);
			}

			if (params.get("decommissioningType") != null) {
				searchTypeAsString = params.get("decommissioningType");
				view.getUIContext().setAttribute(DecommissioningBuilderViewImpl.DECOMMISSIONING_BUILDER_TYPE, searchTypeAsString);
			}
			favoritesId = params.get(RMViews.FAV_GROUP_ID_KEY);
		}

		SearchType searchType = null;
		if (searchTypeAsString != null) {
			searchType = SearchType.valueOf((searchTypeAsString));
		}
		BaseBreadcrumbTrail breadcrumbTrail;

		RMModuleExtensions rmModuleExtensions = view.getConstellioFactories().getAppLayerFactory().getExtensions()
				.forCollection(view.getCollection()).forModule(ConstellioRMModule.ID);
		breadcrumbTrail = rmModuleExtensions
				.getBreadCrumbtrail(new DocumentFolderBreadCrumbParams(getFolderId(), params, view));

		if (breadcrumbTrail != null) {
			return breadcrumbTrail;
		} else if (favoritesId != null) {
			return new FolderDocumentContainerBreadcrumbTrail(view.getSummaryRecord().getId(), null, null, favoritesId, this.view);
		} else if (saveSearchDecommissioningId == null) {
			String containerId = null;
			if (params != null && params instanceof Map) {
				containerId = params.get("containerId");
			}
			return new FolderDocumentContainerBreadcrumbTrail(view.getSummaryRecord().getId(), taxonomyCode, containerId, this.view);
		} else {
			return new DecommissionBreadcrumbTrail($("DecommissioningBuilderView.viewTitle." + searchType.name()),
					searchType, saveSearchDecommissioningId, view.getSummaryRecord().getId(), this.view);
		}
	}

	public int getFolderContentCount() {
		return folderContentDataProvider.size();
	}

	public int getTaskCount() {
		LogicalSearchQuery query = new LogicalSearchQuery(tasksDataProvider.getQuery());
		query.setQueryExecutionMethod(QueryExecutionMethod.USE_CACHE);
		return (int) searchServices().getResultsCount(query);
	}

	public RecordVODataProvider getWorkflows() {
		MetadataSchemaVO schemaVO = new MetadataSchemaToVOBuilder().build(
				schema(BetaWorkflow.DEFAULT_SCHEMA), VIEW_MODE.TABLE, view.getSessionContext());

		return new RecordVODataProvider(schemaVO, new RecordToVOBuilder(), modelLayerFactory, view.getSessionContext()) {
			@Override
			public LogicalSearchQuery getQuery() {
				return new BetaWorkflowServices(view.getCollection(), appLayerFactory).getWorkflowsQuery();
			}
		};
	}

	public void workflowStartRequested(RecordVO record) {
		Map<String, List<String>> parameters = new HashMap<>();
		parameters.put(RMTask.LINKED_FOLDERS, asList(summaryFolderVO.getId()));
		BetaWorkflow workflow = new TasksSchemasRecordsServices(view.getCollection(), appLayerFactory)
				.getBetaWorkflow(record.getId());
		new BetaWorkflowServices(view.getCollection(), appLayerFactory).start(workflow, getCurrentUser(), parameters);
	}

	@Override
	protected boolean hasRestrictedRecordAccess(String params, User user, Record restrictedRecord) {
		return user.hasReadAccess().on(restrictedRecord);
	}

	@Override
	protected List<String> getRestrictedRecordIds(String params) {
		return asList(summaryFolderVO.getId());
	}

	private void disableMenuItems(Folder folder) {
		if (!folder.isLogicallyDeletedStatus()) {
			User user = getCurrentUser();
			view.setDisplayButtonState(getDisplayButtonState(user, folder));
			view.setEditButtonState(getEditButtonState(user, folder));
			view.setAddDocumentButtonState(getAddDocumentButtonState(user, folder));
			view.setBorrowedMessage(getBorrowMessageState(folder));
		}
	}

	String getBorrowMessageState(Folder folder) {
		String borrowedMessage = null;
		if (folder.getBorrowed() != null && folder.getBorrowed()) {
			String borrowUserEntered = folder.getBorrowUserEntered();
			if (borrowUserEntered != null) {
				String userTitle = rmSchemasRecordsServices.getUser(borrowUserEntered).getTitle();
				LocalDateTime borrowDateTime = folder.getBorrowDate();
				LocalDate borrowDate = borrowDateTime != null ? borrowDateTime.toLocalDate() : null;
				borrowedMessage = $("DisplayFolderView.borrowedFolder", userTitle, borrowDate);
			} else {
				borrowedMessage = $("DisplayFolderView.borrowedByNullUserFolder");
			}
		} else if (folder.getContainer() != null) {
			try {
				ContainerRecord containerRecord = rmSchemasRecordsServices.getContainerRecord(folder.getContainer());
				boolean borrowed = Boolean.TRUE.equals(containerRecord.getBorrowed());
				String borrower = containerRecord.getBorrower();
				if (borrowed && borrower != null) {
					String userTitle = rmSchemasRecordsServices.getUser(borrower).getTitle();
					LocalDate borrowDate = containerRecord.getBorrowDate();
					borrowedMessage = $("DisplayFolderView.borrowedContainer", userTitle, borrowDate);
				} else if (borrowed) {
					borrowedMessage = $("DisplayFolderView.borrowedByNullUserContainer");
				}
			} catch (Exception e) {
				LOGGER.error("Could not find linked container");
			}
		}
		return borrowedMessage;
	}

	ComponentState getDisplayButtonState(User user, Folder folder) {
		if (view.isInWindow()) {
			return ComponentState.INVISIBLE;
		} else {
			return ComponentState.visibleIf(nestedView && user.hasReadAccess().on(folder));
		}
	}

	ComponentState getEditButtonState(User user, Folder folder) {
		if (isNotBlank(folder.getLegacyId()) && !user.has(RMPermissionsTo.MODIFY_IMPORTED_FOLDERS).on(folder)) {
			return ComponentState.INVISIBLE;
		}
		return ComponentState.visibleIf(user.hasWriteAccess().on(folder)
										&& !extensions.isModifyBlocked(folder.getWrappedRecord(), user) && extensions
												.isRecordModifiableBy(folder.getWrappedRecord(), user));
	}

	ComponentState getAddDocumentButtonState(User user, Folder folder) {
		if (user.hasWriteAccess().on(folder) &&
			user.has(RMPermissionsTo.CREATE_DOCUMENTS).on(folder)) {
			if (folder.getPermissionStatus().isInactive()) {
				if (folder.getBorrowed() != null && folder.getBorrowed()) {
					return ComponentState.visibleIf(user.has(RMPermissionsTo.MODIFY_INACTIVE_BORROWED_FOLDER).on(folder) && user
							.has(RMPermissionsTo.CREATE_INACTIVE_DOCUMENT).on(folder));
				}
				return ComponentState.visibleIf(user.has(RMPermissionsTo.CREATE_INACTIVE_DOCUMENT).on(folder));
			}
			if (folder.getPermissionStatus().isSemiActive()) {
				if (folder.getBorrowed() != null && folder.getBorrowed()) {
					return ComponentState.visibleIf(user.has(RMPermissionsTo.MODIFY_SEMIACTIVE_BORROWED_FOLDER).on(folder) && user
							.has(RMPermissionsTo.CREATE_SEMIACTIVE_DOCUMENT).on(folder));
				}
				return ComponentState.visibleIf(user.has(RMPermissionsTo.CREATE_SEMIACTIVE_DOCUMENT).on(folder));
			}
			return ComponentState.ENABLED;
		}
		return ComponentState.INVISIBLE;
	}

	private MetadataSchemaType getFoldersSchemaType() {
		return schemaType(Folder.SCHEMA_TYPE);
	}

	private MetadataSchemaType getDocumentsSchemaType() {
		return schemaType(Document.SCHEMA_TYPE);
	}

	private MetadataSchema getFoldersSchema() {
		return schema(Folder.DEFAULT_SCHEMA);
	}

	private MetadataSchema getDocumentsSchema() {
		return schema(Document.DEFAULT_SCHEMA);
	}

	private MetadataSchema getTasksSchema() {
		return schema(Task.DEFAULT_SCHEMA);
	}

	public void viewAssembled() {
		view.setFolderContent(folderContentDataProvider);
		view.setTasks(tasksDataProvider);
		view.setEvents(eventsDataProvider);

		RMSchemasRecordsServices schemas = new RMSchemasRecordsServices(collection, appLayerFactory);
		Folder folder = schemas.getFolderSummary(summaryFolderVO.getId());
		disableMenuItems(folder);
		modelLayerFactory.newLoggingServices().logRecordView(folder.getWrappedRecord(), getCurrentUser());
	}

	public void updateTaskStarred(boolean isStarred, String taskId, RecordVODataProvider dataProvider) {
		TasksSchemasRecordsServices taskSchemas = new TasksSchemasRecordsServices(collection, appLayerFactory);
		Task task = taskSchemas.getTask(taskId);
		if (isStarred) {
			task.addStarredBy(getCurrentUser().getId());
		} else {
			task.removeStarredBy(getCurrentUser().getId());
		}
		try {
			recordServices().update(task);
		} catch (RecordServicesException e) {
			e.printStackTrace();
		}
		dataProvider.fireDataRefreshEvent();
	}

	private Navigation navigate() {
		return view.navigate();
	}

	public void backButtonClicked() {
		navigate().to().previousView();
	}

	public void addDocumentButtonClicked() {
		navigate().to(RMViews.class).addDocument(summaryFolderVO.getId());
	}

	public void navigateToSelf() {
		navigateToFolder(this.summaryFolderVO.getId());
	}

	public void displayFolderButtonClicked() {
		navigateToSelf();
	}

	public void editFolderButtonClicked() {
		RMNavigationUtils.navigateToEditFolder(summaryFolderVO.getId(), params, appLayerFactory, collection);
	}

	public void documentClicked(RecordVO recordVO) {
		ContentVersionVO contentVersionVO = recordVO.get(Document.CONTENT);
		if (contentVersionVO == null) {
			navigateToDocument(recordVO);
			return;
		}
		String agentURL = ConstellioAgentUtils.getAgentURL(recordVO, contentVersionVO);
		if (agentURL != null) {
			//			view.openAgentURL(agentURL);
			new ConstellioAgentClickHandler().handleClick(agentURL, recordVO, contentVersionVO, params);
		} else {
			navigateToDocument(recordVO);
		}
	}

	protected void navigateToDocument(RecordVO recordVO) {
		RMNavigationUtils.navigateToDisplayDocument(recordVO.getId(), params, appLayerFactory,
				collection);
	}

	protected void navigateToFolder(String folderId) {
		RMNavigationUtils.navigateToDisplayFolder(folderId, params, appLayerFactory, collection);
	}

	public void taskClicked(RecordVO taskVO) {
		navigate().to(TaskViews.class).displayTask(taskVO.getId());
	}

	private RMSchemasRecordsServices rmSchemasRecordsServices() {
		return new RMSchemasRecordsServices(getCurrentUser().getCollection(), appLayerFactory);
	}

	private List<String> getAllDocumentTitles() {
		if (documentTitles != null) {
			return documentTitles;
		} else {
			//TODO replace with SearchServices.stream in Constellio 9.0
			documentTitles = new ArrayList<>();
			RMSchemasRecordsServices rm = new RMSchemasRecordsServices(collection, appLayerFactory);
			LogicalSearchQuery query = new LogicalSearchQuery()
					.setCondition(from(rm.document.schemaType()).where(rm.document.folder()).is(summaryFolderVO.getId()))
					.filteredByStatus(StatusFilter.ACTIVES)
					.setReturnedMetadatas(ReturnedMetadatasFilter.onlyMetadatas(Schemas.TITLE));

			List<Record> documents = modelLayerFactory.newSearchServices().search(query);
			for (Record document : documents) {
				documentTitles.add(document.getId());
			}
			return documentTitles;
		}
	}

	private SearchResponseIterator<Record> getExistingDocumentInCurrentFolder(String fileName) {

		MetadataSchemaType documentsSchemaType = getDocumentsSchemaType();
		MetadataSchema documentsSchema = getDocumentsSchema();

		Metadata folderMetadata = documentsSchema.getMetadata(Document.FOLDER);
		LogicalSearchQuery query = new LogicalSearchQuery();
		LogicalSearchCondition queryCondition = from(documentsSchemaType).where(folderMetadata).is(summaryFolderVO.getId())
				.andWhere(Schemas.LOGICALLY_DELETED_STATUS).isFalseOrNull().andWhere(rmSchemasRecordsServices.documentContent()).is(isFilename(fileName));
		query.setCondition(queryCondition);

		SearchServices searchServices = modelLayerFactory.newSearchServices();

		SearchResponseIterator<Record> speQueryResponse = searchServices.recordsIterator(query, 100);

		return speQueryResponse;
	}

	private Record currentFullFolder() {
		return recordServices.getDocumentById(getLazyFullFolderVO().getId());
	}

	public void contentVersionUploaded(ContentVersionVO uploadedContentVO) {
		view.selectFolderContentTab();
		String fileName = uploadedContentVO.getFileName();
		SearchResponseIterator<Record> existingDocument = getExistingDocumentInCurrentFolder(fileName);
		if (existingDocument.getNumFound() == 0 && !extensions.isModifyBlocked(currentFullFolder(), getCurrentUser())) {
			try {
				if (Boolean.TRUE.equals(uploadedContentVO.hasFoundDuplicate())) {
					RMSchemasRecordsServices rm = new RMSchemasRecordsServices(collection, appLayerFactory);
					LogicalSearchQuery duplicateDocumentsQuery = new LogicalSearchQuery()
							.setCondition(LogicalSearchQueryOperators.from(rm.documentSchemaType())
									.where(rm.document.contentHashes()).isEqualTo(uploadedContentVO.getDuplicatedHash())
							).filteredByStatus(StatusFilter.ACTIVES)
							.setReturnedMetadatas(ReturnedMetadatasFilter.onlyMetadatas(Schemas.IDENTIFIER, Schemas.TITLE))
							.setNumberOfRows(100).filteredWithUser(getCurrentUser());
					List<Document> duplicateDocuments = rm.searchDocuments(duplicateDocumentsQuery);
					if (duplicateDocuments.size() > 0) {
						StringBuilder message = new StringBuilder(
								$("ContentManager.hasFoundDuplicateWithConfirmation", StringUtils.defaultIfBlank(fileName, "")));
						message.append("<br>");
						for (Document document : duplicateDocuments) {
							message.append("<br>-");
							message.append(document.getTitle());
							message.append(": ");
							message.append(generateDisplayLink(document));
						}
						view.showClickableMessage(message.toString());
					}
				}
				uploadedContentVO.setMajorVersion(true);
				Document document;
				if (rmSchemasRecordsServices().isEmail(fileName)) {
					InputStreamProvider inputStreamProvider = uploadedContentVO.getInputStreamProvider();
					InputStream in = inputStreamProvider.getInputStream(DisplayFolderPresenter.class + ".contentVersionUploaded");
					document = new EmailParsingServices(rmSchemasRecordsServices).newEmail(fileName, in);
				} else {
					document = rmSchemasRecordsServices.newDocument();
				}
				document.setFolder(summaryFolderVO.getId());
				document.setTitle(fileName);
				InputStream inputStream = null;
				ContentVersionDataSummary contentVersionDataSummary;
				try {
					inputStream = uploadedContentVO.getInputStreamProvider().getInputStream("SchemaPresenterUtils-VersionInputStream");
					UploadOptions options = new UploadOptions().setFileName(fileName);
					ContentManager.ContentVersionDataSummaryResponse uploadResponse = uploadContent(inputStream, options);
					contentVersionDataSummary = uploadResponse.getContentVersionDataSummary();
					document.setContent(appLayerFactory.getModelLayerFactory().getContentManager().createMajor(getCurrentUser(), fileName, contentVersionDataSummary));
					Transaction transaction = new Transaction();
					transaction.add(document);
					transaction.setUser(getCurrentUser());
					appLayerFactory.getModelLayerFactory().newRecordServices().executeWithoutImpactHandling(transaction);
					documentTitles.add(document.getTitle());
				} finally {
					IOServices ioServices = modelLayerFactory.getIOServicesFactory().newIOServices();
					ioServices.closeQuietly(inputStream);
				}
			} catch (final IcapException e) {
				view.showErrorMessage(e.getMessage());
			} catch (Exception e) {
				LOGGER.error(e.getMessage(), e);
			} finally {
				view.clearUploadField();
			}
		} else if (existingDocument.getNumFound() > 0) {
			StringBuilder message = new StringBuilder();
			boolean refreshDocument = false;

			while (existingDocument.hasNext()) {
				Record currentRecord = existingDocument.next();
				Content content = currentRecord.get(rmSchemasRecordsServices.document.content());
				ContentVersion contentVersion = content.getCurrentVersion();
				if (contentVersion.getHash() != null && !uploadedContentVO.getHash().equals(contentVersion.getHash())) {
					refreshDocument = true;
					if (!hasWritePermission(currentRecord)) {
						message.append($("displayFolderView.noWritePermission", currentRecord) + "</br>");
					} else if (isCheckedOutByOtherUser(currentRecord)) {
						message.append($("displayFolderView.checkoutByAnOtherUser", currentRecord) + "</br>");
					} else {
						view.showVersionUpdateWindow(voBuilder.build(currentRecord,
								VIEW_MODE.DISPLAY, view.getSessionContext()), uploadedContentVO);
					}
				} else {
					message.append($("displayfolderview.unchangeFile", currentRecord.getTitle()) + "</br>");
				}
			}
			if (message.length() > 0) {
				view.showErrorMessage(message.toString());
			}

			if (refreshDocument) {
				//documentsDataProvider.fireDataRefreshEvent();
			}
		}
		folderContentDataProvider.fireDataRefreshEvent();
	}

	private boolean hasWritePermission(Record record) {
		User currentUser = presenterUtilsForDocument.getCurrentUser();
		return currentUser.hasWriteAccess().on(record);
	}

	private boolean isCheckedOutByOtherUser(Record recordVO) {
		Content content = recordVO.get(rmSchemasRecordsServices.document.content());
		if (recordVO.getTypeCode().equals(Document.SCHEMA_TYPE) && content != null) {
			User currentUser = presenterUtilsForDocument.getCurrentUser();
			String checkOutUserId = content.getCheckoutUserId();
			return checkOutUserId != null && !checkOutUserId.equals(currentUser.getId());
		} else {
			return false;
		}
	}

	public boolean borrowFolder(LocalDate borrowingDate, LocalDate previewReturnDate, String userId,
								BorrowingType borrowingType,
								LocalDate returnDate) {
		boolean borrowed;
		String errorMessage = borrowingServices
				.validateBorrowingInfos(userId, borrowingDate, previewReturnDate, borrowingType, returnDate);
		if (errorMessage != null) {
			view.showErrorMessage($(errorMessage));
			borrowed = false;
		} else {
			Record record = recordServices().getDocumentById(userId);
			User borrowerEntered = wrapUser(record);
			try {
				borrowingServices
						.borrowFolder(summaryFolderVO.getId(), borrowingDate, previewReturnDate, getCurrentUser(), borrowerEntered,
								borrowingType, true);
				navigateToFolder(summaryFolderVO.getId());
				borrowed = true;
			} catch (RecordServicesException e) {
				LOGGER.error(e.getMessage(), e);
				view.showErrorMessage($("DisplayFolderView.cannotBorrowFolder"));
				borrowed = false;
			}
		}
		if (returnDate != null) {
			return returnFolder(returnDate, borrowingDate);
		}
		return borrowed;
	}

	public boolean returnFolder(LocalDate returnDate) {
		LocalDateTime borrowDateTime = summaryFolderVO.getBorrowDate();
		LocalDate borrowDate = borrowDateTime != null ? borrowDateTime.toLocalDate() : null;
		return returnFolder(returnDate, borrowDate);
	}

	protected boolean returnFolder(LocalDate returnDate, LocalDate borrowingDate) {
		String errorMessage = borrowingServices.validateReturnDate(returnDate, borrowingDate);
		if (errorMessage != null) {
			view.showErrorMessage($(errorMessage));
			return false;
		}
		try {
			borrowingServices.returnFolder(summaryFolderVO.getId(), getCurrentUser(), returnDate, true);
			navigateToFolder(summaryFolderVO.getId());
			return true;
		} catch (RecordServicesException e) {
			view.showErrorMessage($("DisplayFolderView.cannotReturnFolder"));
			return false;
		}
	}

	private EmailToSend newEmailToSend() {
		MetadataSchemaTypes types = metadataSchemasManager.getSchemaTypes(getCurrentUser().getCollection());
		MetadataSchema schema = types.getSchemaType(EmailToSend.SCHEMA_TYPE).getDefaultSchema();
		Record emailToSendRecord = recordServices.newRecordWithSchema(schema);
		return new EmailToSend(emailToSendRecord, types);
	}
	//
	//	public void reminderReturnFolder() {
	//
	//		try {
	//			EmailToSend emailToSend = newEmailToSend();
	//			String constellioUrl = eimConfigs.getConstellioUrl();
	//			User borrower = null;
	//			if (folderVO.getBorrowUserEnteredId() != null) {
	//				borrower = rmSchemasRecordsServices.getUser(folderVO.getBorrowUserEnteredId());
	//			} else {
	//				borrower = rmSchemasRecordsServices.getUser(folderVO.getBorrowUserId());
	//			}
	//
	//			EmailAddress borrowerAddress = new EmailAddress(borrower.getTitle(), borrower.getEmail());
	//			emailToSend.setTo(Arrays.asList(borrowerAddress));
	//			emailToSend.setSendOn(TimeProvider.getLocalDateTime());
	//			emailToSend.setSubject($("DisplayFolderView.returnFolderReminder") + folderVO.getTitle());
	//			emailToSend.setTemplate(RMEmailTemplateConstants.REMIND_BORROW_TEMPLATE_ID);
	//			List<String> parameters = new ArrayList<>();
	//			String previewReturnDate = folderVO.getPreviewReturnDate().toString();
	//			parameters.add("previewReturnDate" + EmailToSend.PARAMETER_SEPARATOR + previewReturnDate);
	//			parameters.add("borrower" + EmailToSend.PARAMETER_SEPARATOR + borrower.getUsername());
	//			String borrowedFolderTitle = folderVO.getTitle();
	//			parameters.add("borrowedFolderTitle" + EmailToSend.PARAMETER_SEPARATOR + borrowedFolderTitle);
	//			boolean isAddingRecordIdInEmails = eimConfigs.isAddingRecordIdInEmails();
	//			if (isAddingRecordIdInEmails) {
	//				parameters.add("title" + EmailToSend.PARAMETER_SEPARATOR + $("DisplayFolderView.returnFolderReminder") + " \""
	//							   + folderVO.getTitle() + "\" (" + folderVO.getId() + ")");
	//			} else {
	//				parameters.add("title" + EmailToSend.PARAMETER_SEPARATOR + $("DisplayFolderView.returnFolderReminder") + " \""
	//							   + folderVO.getTitle() + "\"");
	//			}
	//
	//			parameters.add("constellioURL" + EmailToSend.PARAMETER_SEPARATOR + constellioUrl);
	//			parameters.add("recordURL" + EmailToSend.PARAMETER_SEPARATOR + constellioUrl + "#!"
	//						   + RMNavigationConfiguration.DISPLAY_FOLDER + "/" + folderVO.getId());
	//			emailToSend.setParameters(parameters);
	//
	//			recordServices.add(emailToSend);
	//			view.showMessage($("DisplayFolderView.reminderEmailSent"));
	//		} catch (RecordServicesException e) {
	//			LOGGER.error("DisplayFolderView.cannotSendEmail", e);
	//			view.showMessage($("DisplayFolderView.cannotSendEmail"));
	//		}
	//	}

	public void alertWhenAvailable() {
		try {
			RMSchemasRecordsServices schemas = new RMSchemasRecordsServices(view.getCollection(), appLayerFactory);
			Folder folder = schemas.getFolder(summaryFolderVO.getId());
			List<String> usersToAlert = folder.getAlertUsersWhenAvailable();
			String currentUserId = getCurrentUser().getId();
			if (!currentUserId.equals(folder.getBorrowUser()) && !currentUserId.equals(folder.getBorrowUserEntered())) {
				List<String> newUsersToAlert = new ArrayList<>();
				newUsersToAlert.addAll(usersToAlert);
				if (!newUsersToAlert.contains(currentUserId)) {
					newUsersToAlert.add(currentUserId);
					folder.setAlertUsersWhenAvailable(newUsersToAlert);
					addOrUpdate(folder.getWrappedRecord());
				}
			}
			view.showMessage($("RMObject.createAlert"));
		} catch (Exception e) {
			LOGGER.error("RMObject.cannotCreateAlert", e);
			view.showErrorMessage($("RMObject.cannotCreateAlert"));
		}
	}

	public List<LabelTemplate> getCustomTemplates() {
		return appLayerFactory.getLabelTemplateManager().listExtensionTemplates(Folder.SCHEMA_TYPE);
	}

	public List<LabelTemplate> getDefaultTemplates() {
		return appLayerFactory.getLabelTemplateManager().listTemplates(Folder.SCHEMA_TYPE);
	}

	public Date getPreviewReturnDate(Date borrowDate, Object borrowingTypeValue) {
		BorrowingType borrowingType;
		Date previewReturnDate = TimeProvider.getLocalDate().toDate();
		if (borrowDate != null && borrowingTypeValue != null) {
			borrowingType = (BorrowingType) borrowingTypeValue;
			if (borrowingType == BorrowingType.BORROW) {
				int addDays = rmConfigs.getBorrowingDurationDays();
				previewReturnDate = LocalDate.fromDateFields(borrowDate).plusDays(addDays).toDate();
			} else {
				previewReturnDate = borrowDate;
			}
		}
		return previewReturnDate;
	}

	boolean isDocument(RecordVO record) {
		return record.getSchema().getCode().startsWith("document");
	}

	public boolean canModifyDocument(RecordVO record) {
		boolean hasContent = record.get(Document.CONTENT) != null;
		boolean hasAccess = getCurrentUser().hasWriteAccess().on(getRecord(record.getId()));
		return hasContent && hasAccess;
	}

	public void addToCartRequested(RecordVO recordVO) {
		Cart cart = rmSchemasRecordsServices.getCart(recordVO.getId());
		addToCartRequested(cart);
	}

	public void addToCartRequested(Cart cart) {
		if (rmSchemasRecordsServices.numberOfFoldersInFavoritesReachesLimit(cart.getId(), 1)) {
			view.showMessage($("DisplayFolderViewImpl.cartCannotContainMoreThanAThousandFolders"));
		} else {
			Folder folder = rmSchemasRecordsServices.wrapFolder(getLazyFullFolderVO().getRecord());
			folder.addFavorite(cart.getId());
			try {
				recordServices().update(folder.getWrappedRecord(), RecordUpdateOptions.validationExceptionSafeOptions());
				view.showMessage($("DisplayFolderView.addedToCart"));
			} catch (RecordServicesException e) {
				e.printStackTrace();
				throw new RuntimeException(e);
			}
		}
	}

	public RecordVODataProvider getOwnedCartsDataProvider() {
		final MetadataSchemaVO cartSchemaVO = schemaVOBuilder
				.build(rmSchemasRecordsServices.cartSchema(), VIEW_MODE.TABLE, view.getSessionContext());
		return new RecordVODataProvider(cartSchemaVO, new RecordToVOBuilder(), modelLayerFactory, view.getSessionContext()) {
			@Override
			public LogicalSearchQuery getQuery() {
				return new LogicalSearchQuery(
						from(rmSchemasRecordsServices.cartSchema()).where(rmSchemasRecordsServices.cartOwner())
								.isEqualTo(getCurrentUser().getId())).sortAsc(Schemas.TITLE);
			}
		};
	}

	public RecordVODataProvider getSharedCartsDataProvider() {
		final MetadataSchemaVO cartSchemaVO = schemaVOBuilder
				.build(rmSchemasRecordsServices.cartSchema(), VIEW_MODE.TABLE, view.getSessionContext());
		return new RecordVODataProvider(cartSchemaVO, new RecordToVOBuilder(), modelLayerFactory, view.getSessionContext()) {
			@Override
			public LogicalSearchQuery getQuery() {
				return new LogicalSearchQuery(
						from(rmSchemasRecordsServices.cartSchema()).where(rmSchemasRecordsServices.cartSharedWithUsers())
								.isContaining(asList(getCurrentUser().getId()))).sortAsc(Schemas.TITLE);
			}
		};
	}

	public void parentFolderButtonClicked(String parentId)
			throws RecordServicesException {
		RMSchemasRecordsServices rmSchemas = new RMSchemasRecordsServices(collection, appLayerFactory);

		String currentFolderId = summaryFolderVO.getId();
		if (isNotBlank(parentId)) {
			try {
				recordServices.update(rmSchemas.getFolder(currentFolderId).setParentFolder(parentId));
				navigate().to(RMViews.class).displayFolder(currentFolderId);
			} catch (RecordServicesException.ValidationException e) {
				view.showErrorMessage($(e.getErrors()));
			}
		}
	}

	public RecordVODataProvider getEventsDataProvider() {
		final MetadataSchemaVO eventSchemaVO = schemaVOBuilder
				.build(rmSchemasRecordsServices.eventSchema(), VIEW_MODE.TABLE, view.getSessionContext());
		return new RecordVODataProvider(eventSchemaVO, new EventToVOBuilder(), modelLayerFactory, view.getSessionContext()) {
			@Override
			public LogicalSearchQuery getQuery() {
				RMEventsSearchServices rmEventsSearchServices = new RMEventsSearchServices(modelLayerFactory, collection);
				return rmEventsSearchServices.newFindEventByRecordIDQuery(getCurrentUser(), summaryFolderVO.getId());
			}
		};
	}

	protected boolean hasCurrentUserPermissionToViewEvents() {
		Folder folder = rmSchemasRecordsServices.getFolderSummary(summaryFolderVO.getId());
		return getCurrentUser().has(CorePermissions.VIEW_EVENTS).on(folder);
	}

	void metadataTabSelected() {
		view.selectMetadataTab();
	}

	void folderContentTabSelected() {
		view.selectFolderContentTab();
	}

	void tasksTabSelected() {
		if (tasksDataProvider == null) {
			tasksDataProvider = new RecordVODataProvider(
					tasksSchemaVO, folderVOBuilder, modelLayerFactory, view.getSessionContext()) {
				@Override
				public LogicalSearchQuery getQuery() {
					LogicalSearchQuery query = getTasksQuery();
					addStarredSortToQuery(query);
					query.sortDesc(Schemas.MODIFIED_ON);
					return query;
				}

				@Override
				public boolean isSearchCache() {
					return eimConfigs.isOnlySummaryMetadatasDisplayedInTables();
				}

				@Override
				protected void clearSort(LogicalSearchQuery query) {
					super.clearSort(query);
					addStarredSortToQuery(query);
				}
			};
			view.setTasks(tasksDataProvider);
		}

		view.selectTasksTab();
	}

	void eventsTabSelected() {
		view.selectEventsTab();
	}

	public boolean hasCurrentUserPermissionToUseCartGroup() {
		return getCurrentUser().has(RMPermissionsTo.USE_GROUP_CART).globally();
	}


	public boolean hasCurrentUserPermissionToUseMyCart() {
		return getCurrentUser().has(RMPermissionsTo.USE_MY_CART).globally();
	}

	public boolean hasPermissionToStartWorkflow() {
		return getCurrentUser().has(TasksPermissionsTo.START_WORKFLOWS).globally();
	}

	public boolean isSelected(RecordVO recordVO) {
		return allItemsSelected || selectedRecordIds.contains(recordVO.getId());
	}

	public boolean isFacetApplyButtonEnabled() {
		return getCurrentUser().isApplyFacetsEnabled();
	}

	public void recordSelectionChanged(RecordVO recordVO, Boolean selected) {
		String recordId = recordVO.getId();
		if (selected) {
			selectedRecordIds.add(recordId);
		} else {
			selectedRecordIds.remove(recordId);
		}
	}

	boolean isAllItemsSelected() {
		return allItemsSelected;
	}

	boolean isAllItemsDeselected() {
		return allItemsDeselected;
	}

	void selectAllClicked() {
		allItemsSelected = true;
		allItemsDeselected = false;
		selectedRecordIds.clear();
	}

	void deselectAllClicked() {
		allItemsSelected = false;
		allItemsDeselected = true;
		selectedRecordIds.clear();
	}

	String generateDisplayLink(Document document) {
		String constellioUrl = eimConfigs.getConstellioUrl();
		String displayURL = RMNavigationConfiguration.DISPLAY_DOCUMENT;
		String url = constellioUrl + "#!" + displayURL + "/" + document.getId();
		return "<a href=\"" + url + "\">" + url + "</a>";
	}

	public boolean isLogicallyDeleted() {
		return Boolean.TRUE
				.equals(summaryFolderVO.getMetadataValue(summaryFolderVO.getMetadata(Schemas.LOGICALLY_DELETED_STATUS.getLocalCode()))
						.getValue());
	}

	private void addStarredSortToQuery(LogicalSearchQuery query) {
		Metadata metadata = types().getSchema(Task.DEFAULT_SCHEMA).getMetadata(STARRED_BY_USERS);
		LogicalSearchQuerySort sortField = new FunctionLogicalSearchQuerySort(
				"termfreq(" + metadata.getDataStoreCode() + ",\'" + getCurrentUser().getId() + "\')", false);
		query.sortFirstOn(sortField);
	}

	public void addToDefaultFavorite() {
		if (rmSchemasRecordsServices.numberOfFoldersInFavoritesReachesLimit(getCurrentUser().getId(), 1)) {
			view.showMessage($("DisplayFolderViewImpl.cartCannotContainMoreThanAThousandFolders"));
		} else {
			Folder folder = rmSchemasRecordsServices.wrapFolder(getLazyFullFolderVO().getRecord());
			folder.addFavorite(getCurrentUser().getId());
			try {
				recordServices().update(folder.getWrappedRecord(), RecordUpdateOptions.validationExceptionSafeOptions());
				view.showMessage($("DisplayFolderViewImpl.folderAddedToDefaultFavorites"));
			} catch (RecordServicesException e) {
				e.printStackTrace();
				throw new RuntimeException(e);
			}
		}
	}

	public RMSelectionPanelReportPresenter buildReportPresenter() {
		return new RMSelectionPanelReportPresenter(appLayerFactory, collection, getCurrentUser()) {
			@Override
			public String getSelectedSchemaType() {
				return Folder.SCHEMA_TYPE;
			}

			@Override
			public List<String> getSelectedRecordIds() {
				return asList(summaryFolderVO.getId());
			}
		};
	}

	public AppLayerFactory getApplayerFactory() {
		return appLayerFactory;
	}

	public boolean isNeedingAReasonToDeleteFolder() {
		return new RMConfigs(modelLayerFactory.getSystemConfigurationsManager()).isNeedingAReasonBeforeDeletingFolders();
	}

	public void refreshDocuments() {
		//documentsDataProvider.fireDataRefreshEvent();
		view.refreshFolderContentTab();
	}

	public void facetValueSelected(String facetId, String facetValue) {
		facetSelections.get(facetId).add(facetValue);
		folderContentDataProvider.fireDataRefreshEvent();
		view.refreshFolderContentAndFacets();
	}

	public void facetValuesChanged(KeySetMap<String, String> facets) {
		facetSelections.clear();
		facetSelections.addAll(facets);
		folderContentDataProvider.fireDataRefreshEvent();
		view.refreshFolderContentAndFacets();
	}

	public void facetValueDeselected(String facetId, String facetValue) {
		facetSelections.get(facetId).remove(facetValue);
		folderContentDataProvider.fireDataRefreshEvent();
		view.refreshFolderContentAndFacets();
	}

	public void facetDeselected(String facetId) {
		facetSelections.get(facetId).clear();
		folderContentDataProvider.fireDataRefreshEvent();
		view.refreshFolderContentAndFacets();
	}

	public void facetOpened(String facetId) {
		facetStatus.put(facetId, true);
	}

	public void facetClosed(String facetId) {
		facetStatus.put(facetId, false);
	}

	public KeySetMap<String, String> getFacetSelections() {
		return facetSelections;
	}

	public void setFacetSelections(Map<String, Set<String>> facetSelections) {
		this.facetSelections.putAll(facetSelections);
	}

	public void sortCriterionSelected(String sortCriterion, SortOrder sortOrder) {
		this.sortCriterion = sortCriterion;
		this.sortOrder = sortOrder;
		folderContentDataProvider.fireDataRefreshEvent();
		view.refreshFolderContent();
	}

	public List<FacetVO> getFacets(RecordVODataProvider dataProvider) {
		//Call #1
		if (dataProvider == null /* || dataProvider.getFieldFacetValues() == null */) {
			return service.getFacets(getFolderContentQuery(), facetStatus, getCurrentLocale());
		} else {
			return service.buildFacetVOs(dataProvider.getFieldFacetValues(), dataProvider.getQueryFacetsValues(),
					facetStatus, getCurrentLocale());
		}
	}

	public String getSortCriterion() {
		return sortCriterion;
	}

	public SortOrder getSortOrder() {
		return sortOrder;
	}

	protected List<MetadataVO> getMetadataAllowedInSort(String schemaTypeCode) {
		MetadataSchemaType schemaType = schemaType(schemaTypeCode);
		return getMetadataAllowedInSort(schemaType);
	}

	protected List<MetadataVO> getMetadataAllowedInSort(MetadataSchemaType schemaType) {
		MetadataToVOBuilder builder = new MetadataToVOBuilder();

		List<MetadataVO> result = new ArrayList<>();
		for (Metadata metadata : schemaType.getAllMetadatas()) {
			if (metadata.isSortable()) {
				result.add(builder.build(metadata, view.getSessionContext()));
			}
		}
		return result;
	}

	public List<MetadataVO> getMetadataAllowedInSort() {
		List<MetadataSchemaType> schemaTypes = new ArrayList<>();
		schemaTypes.add(rmSchemasRecordsServices.folderSchemaType());
		schemaTypes.add(rmSchemasRecordsServices.documentSchemaType());
		return getCommonMetadataAllowedInSort(schemaTypes);
	}

	private List<MetadataVO> getCommonMetadataAllowedInSort(List<MetadataSchemaType> schemaTypes) {
		List<MetadataVO> result = new ArrayList<>();
		Set<String> resultCodes = new HashSet<>();
		for (MetadataSchemaType metadataSchemaType : schemaTypes) {
			for (MetadataVO metadata : getMetadataAllowedInSort(metadataSchemaType)) {
				if (resultCodes.add(metadata.getLocalCode())) {
					result.add(metadata);
				}
			}
		}
		return result;
	}

	public String getSortCriterionValueAmong(List<MetadataVO> sortableMetadata) {
		if (this.sortCriterion == null) {
			return null;
		}
		if (!this.sortCriterion.startsWith("global_")) {
			return this.sortCriterion;
		} else {
			String localCode = new SchemaUtils().getLocalCodeFromMetadataCode(this.sortCriterion);
			for (MetadataVO metadata : sortableMetadata) {
				if (metadata.getLocalCode().equals(localCode)) {
					return metadata.getCode();
				}
			}
		}
		return this.sortCriterion;
	}


	public List<Cart> getOwnedCarts() {
		return rmSchemasRecordsServices().wrapCarts(searchServices().search(new LogicalSearchQuery(from(rmSchemasRecordsServices().cartSchema()).where(rmSchemasRecordsServices().cart.owner())
				.isEqualTo(getCurrentUser().getId())).sortAsc(Schemas.TITLE)));
	}

	public MetadataSchemaVO getSchema() {
		return new MetadataSchemaToVOBuilder().build(schema(Cart.DEFAULT_SCHEMA), RecordVO.VIEW_MODE.TABLE, view.getSessionContext());
	}

	public void itemClicked(RecordVO recordVO, Integer index) {
		this.returnIndex = index;
		this.returnRecordVO = recordVO;
	}

	public Integer getReturnIndex() {
		return returnIndex;
	}

	public RecordVO getReturnRecordVO() {
		return returnRecordVO;
	}

	public FolderVO getSummaryFolderVO() {
		return summaryFolderVO;
	}

	public FolderVO getLazyFullFolderVO() {
		return lazyFullFolderVO.get();
	}
}
