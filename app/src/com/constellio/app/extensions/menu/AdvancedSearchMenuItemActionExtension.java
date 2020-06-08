package com.constellio.app.extensions.menu;

import com.constellio.app.api.extensions.params.SearchPageConditionParam;
import com.constellio.app.extensions.AppLayerCollectionExtensions;
import com.constellio.app.modules.rm.wrappers.ContainerRecord;
import com.constellio.app.modules.rm.wrappers.StorageSpace;
import com.constellio.app.services.factories.AppLayerFactory;
import com.constellio.app.services.menu.MenuItemAction;
import com.constellio.app.services.menu.MenuItemActionState;
import com.constellio.app.services.menu.behavior.MenuItemActionBehaviorParams;
import com.constellio.app.services.menu.behavior.ui.AdvancedViewBatchProcessingPresenter;
import com.constellio.app.services.menu.behavior.ui.AdvancedViewBatchProcessingViewImpl;
import com.constellio.app.ui.application.ConstellioUI;
import com.constellio.app.ui.framework.buttons.BaseButton;
import com.constellio.app.ui.framework.buttons.WindowButton;
import com.constellio.app.ui.framework.components.BaseWindow;
import com.constellio.app.ui.framework.components.ReportTabButton;
import com.constellio.app.ui.pages.search.AdvancedSearchViewImpl;
import com.constellio.app.ui.pages.search.batchProcessing.BatchProcessingButton;
import com.constellio.app.ui.pages.search.criteria.ConditionBuilder;
import com.constellio.app.ui.pages.search.criteria.ConditionException;
import com.constellio.app.ui.pages.search.criteria.Criterion;
import com.constellio.model.entities.records.wrappers.Facet;
import com.constellio.model.entities.records.wrappers.SavedSearch;
import com.constellio.model.entities.records.wrappers.User;
import com.constellio.model.entities.records.wrappers.structure.FacetType;
import com.constellio.model.entities.schemas.MetadataSchema;
import com.constellio.model.entities.schemas.MetadataSchemaType;
import com.constellio.model.entities.schemas.MetadataSchemaTypes;
import com.constellio.model.entities.schemas.Schemas;
import com.constellio.model.services.factories.ModelLayerFactory;
import com.constellio.model.services.records.RecordServices;
import com.constellio.model.services.records.RecordServicesRuntimeException;
import com.constellio.model.services.records.SchemasRecordsServices;
import com.constellio.model.services.schemas.MetadataSchemasManager;
import com.constellio.model.services.search.SearchServices;
import com.constellio.model.services.search.StatusFilter;
import com.constellio.model.services.search.query.ReturnedMetadatasFilter;
import com.constellio.model.services.search.query.logical.LogicalSearchQuery;
import com.constellio.model.services.search.query.logical.LogicalSearchQueryFacetFilters;
import com.constellio.model.services.search.query.logical.condition.LogicalSearchCondition;
import com.vaadin.ui.Component;
import com.vaadin.ui.Label;
import com.vaadin.ui.Panel;
import com.vaadin.ui.VerticalLayout;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import static com.constellio.app.ui.i18n.i18n.$;
import static com.constellio.model.services.search.query.logical.LogicalSearchQueryOperators.from;

@Slf4j
public abstract class AdvancedSearchMenuItemActionExtension extends MenuItemActionsExtension {
	protected String collection;
	protected AppLayerFactory appLayerFactory;
	protected ModelLayerFactory modelLayerFactory;
	private SearchServices searchServices;
	private AppLayerCollectionExtensions appCollectionExtensions;
	protected RecordServices recordServices;

	private boolean batchProcessOnAllSearchResults;
	private List<String> listSearchableMetadataSchemaType;
	private Map<String, String[]> extraSolrParams = new HashMap<>();

	private static final String RECORDS_GENERATE_REPORT = "RECORDS_GENERATE_REPORT";
	private static final String RECORDS_BATCH = "RECORDS_GENERATE_REPORT";

	public AdvancedSearchMenuItemActionExtension(String collection, AppLayerFactory appLayerFactory) {
		this.collection = collection;
		this.appLayerFactory = appLayerFactory;

		modelLayerFactory = appLayerFactory.getModelLayerFactory();
		searchServices = modelLayerFactory.newSearchServices();
		appCollectionExtensions = appLayerFactory.getExtensions().forCollection(collection);
		recordServices = modelLayerFactory.newRecordServices();
	}

	@Override
	public void addMenuItemActionsForQuery(MenuItemActionExtensionAddMenuItemActionsForQueryParams params) {
		if (!(params.getBehaviorParams().getView() instanceof AdvancedSearchViewImpl)) {
			return;
		}

		MenuItemAction menuItemAction = MenuItemAction.builder()
				.type(RECORDS_BATCH)
				.state(getActionStateForBatchProcessingInternal(params.getQuery(), params.getBehaviorParams().getUser(), params.isReturnedResults()))
				.caption($("AdvancedSearchView.batchProcessing"))
				.icon(null)
				.group(-1)
				.priority(1100)
				.recordsLimit(-1)
				.command((ids) -> batchProcess(params.getQuery(), params.getBehaviorParams()))
				.build();
		params.getMenuItemActions().add(menuItemAction);

	}

	@Override
	public MenuItemActionState getActionStateForQuery(MenuItemActionExtensionGetActionStateForQueryParams params) {
		if (params.getMenuItemActionType().equals(RECORDS_GENERATE_REPORT)) {
			return getActionStateForBatchProcessing(params.getQuery(), params.getBehaviorParams().getUser());
		} else if (params.getMenuItemActionType().equals(RECORDS_BATCH)) {
			return getActionStateForReports(params.getQuery());
		}
		return null;
	}

	private MenuItemActionState getActionStateForBatchProcessingInternal(LogicalSearchQuery logicalSearchQuery,
																		 User user, boolean hasResults) {
		if (logicalSearchQuery == null || !hasResults) {
			return MenuItemActionState.visibleOrHidden(false);
		}

		return getActionStateForBatchProcessing(logicalSearchQuery, user);
	}

	private MenuItemActionState getActionStateForReportInternal(LogicalSearchQuery logicalSearchQuery,
																boolean hasResults) {
		if (logicalSearchQuery == null || !hasResults) {
			return MenuItemActionState.visibleOrHidden(false);
		}

		return getActionStateForReports(logicalSearchQuery);
	}

	protected abstract MenuItemActionState getActionStateForBatchProcessing(LogicalSearchQuery query, User user);

	protected abstract MenuItemActionState getActionStateForReports(LogicalSearchQuery logicalSearchQuery);

	protected abstract boolean noPDFButton(String schemaType);

	private void batchProcess(LogicalSearchQuery query, MenuItemActionBehaviorParams params) {
		AdvancedViewBatchProcessingPresenter batchProcessingPresenter =
				new AdvancedViewBatchProcessingPresenter(appLayerFactory, (AdvancedSearchViewImpl) params.getView(),
						params.getUser(), query);
		AdvancedViewBatchProcessingViewImpl batchProcessingView =
				new AdvancedViewBatchProcessingViewImpl(batchProcessingPresenter);

		WindowButton button = new BatchProcessingButton(batchProcessingPresenter, batchProcessingView)
					.hasResultSelected(!batchProcessingView.getSelectedRecordIds().isEmpty());
		button.click();
	}

	private void generateReport(LogicalSearchQuery searchQuery, MenuItemActionBehaviorParams params) {
		User user = params.getUser();
		AdvancedSearchViewImpl view = ((AdvancedSearchViewImpl) params.getView());
		List<String> selectedRecordIds = view.getSelectedRecordIds();
		List<String> unselectedRecordIds = view.getUnselectedRecordIds();
		String schemaType = getSchemaType(searchQuery);

		BaseButton reportButton = new ReportTabButton($("SearchView.metadataReportTitle"), $("SearchView.metadataReportTitle"), view,
				!getListSearchableMetadataSchemaType().contains(schemaType),
				noPDFButton(schemaType)) {

			BaseWindow querySelectionWindow;

			@Override
			public void buttonClick(ClickEvent event) {
				if (querySelectionWindow == null || !(ConstellioUI.getCurrent().getWindows() != null && ConstellioUI.getCurrent().getWindows().contains(querySelectionWindow))) {
					querySelectionWindow = new BaseWindow($("com.constellio.app.extensions.WorkflowPageExtension_confirmationTitle"));
					querySelectionWindow.setWidth("50%");
					querySelectionWindow.setHeight("220px");
					querySelectionWindow.center();
					querySelectionWindow.setModal(true);
					querySelectionWindow.setContent(buildQuerySelectionWindow());
					ConstellioUI.getCurrent().addWindow(querySelectionWindow);
					querySelectionWindow.focus();
				}
			}

			@Override
			protected LogicalSearchQuery getLogicalSearchQuery(String selectedSchemaFilter) {
				LogicalSearchQuery query = buildReportLogicalSearchQuery(user, view, selectedRecordIds,
						unselectedRecordIds, schemaType, searchQuery);
				if (selectedSchemaFilter != null) {
					query.setCondition(query.getCondition().andWhere(Schemas.SCHEMA).isEqualTo(selectedSchemaFilter));
				}
				return query;
			}

			private Component buildQuerySelectionWindow() {
				Panel panel = new Panel();
				VerticalLayout vLayout = new VerticalLayout();
				vLayout.setSpacing(true);

				Label questionLabel = new Label($("AdvancedSearch.reportRecordSelection"));

				BaseButton allSearchResultsButton = new BaseButton($("AdvancedSearchView.allSearchResults")) {
					@Override
					protected void buttonClick(ClickEvent event) {
						batchProcessOnAllSearchResults = true;
						querySelectionWindow.close();
						proceedToReportSelection(event);
					}
				};

				BaseButton selectedSearchResultsButton = new BaseButton($("AdvancedSearchView.selectedSearchResults")) {
					@Override
					protected void buttonClick(ClickEvent event) {
						batchProcessOnAllSearchResults = false;
						querySelectionWindow.close();
						proceedToReportSelection(event);
					}
				};

				if (selectedRecordIds == null || selectedRecordIds.isEmpty()) {
					selectedSearchResultsButton.setEnabled(false);
				}

				vLayout.addComponents(questionLabel, allSearchResultsButton, selectedSearchResultsButton);

				panel.setContent(vLayout);
				panel.setSizeFull();
				return panel;
			}

			private void proceedToReportSelection(ClickEvent event) {
				super.buttonClick(event);
			}
		};
		reportButton.click();
	}


	private LogicalSearchQuery buildReportLogicalSearchQuery(User user, AdvancedSearchViewImpl view,
															 List<String> selectedRecordIds,
															 List<String> unselectedRecordIds, String schemaType,
															 LogicalSearchQuery searchQuery) {
		return buildLogicalSearchQuery(user, view, selectedRecordIds, unselectedRecordIds, schemaType, searchQuery)
				.filteredWithUser(user);
	}

	private LogicalSearchQuery buildLogicalSearchQuery(User user, AdvancedSearchViewImpl view,
													   List<String> selectedRecordIds, List<String> unselectedRecordIds,
													   String schemaType, LogicalSearchQuery searchQuery) {
		LogicalSearchQuery query = null;
		if (ContainerRecord.SCHEMA_TYPE.equals(schemaType) || StorageSpace.SCHEMA_TYPE.equals(schemaType)) {
			if (!batchProcessOnAllSearchResults) {
				query = buildUnsecuredLogicalSearchQueryWithSelectedIds(user, view, selectedRecordIds, schemaType);
			} else if (selectedRecordIds != null && !selectedRecordIds.isEmpty()) {
				query = buildUnsecuredLogicalSearchQueryWithUnselectedIds(user, view, unselectedRecordIds, schemaType);
			} else {
				query = buildUnsecuredLogicalSearchQueryWithAllRecords(user, view, schemaType);
			}
		} else {
			if (!batchProcessOnAllSearchResults) {
				query = buildLogicalSearchQueryWithSelectedIds(user, view, selectedRecordIds, schemaType);
			} else if (selectedRecordIds != null && !selectedRecordIds.isEmpty()) {
				query = buildLogicalSearchQueryWithUnselectedIds(user, view, unselectedRecordIds, schemaType);
			} else {
				query = buildLogicalSearchQueryWithAllRecords(user, view, schemaType);
			}
		}

		String searchExpression = StringUtils.stripToNull(view.getSearchExpression());
		if (searchExpression != null && !searchExpression.isEmpty()) {
			query.setFreeTextQuery(searchExpression);
		}

		if (!searchQuery.getSortFields().isEmpty()) {
			query.sortOn(searchQuery.getSortFields().get(0));
		}

		return query;
	}

	private LogicalSearchQuery buildLogicalSearchQueryWithSelectedIds(User user, AdvancedSearchViewImpl view,
																	  List<String> selectedRecordIds,
																	  String schemaType) {
		LogicalSearchQuery query = getSearchQuery(user, view, schemaType);
		query.setCondition(query.getCondition().andWhere(Schemas.IDENTIFIER).isIn(selectedRecordIds)
				.andWhere(Schemas.LOGICALLY_DELETED_STATUS).isFalseOrNull())
				.filteredWithUser(user).filteredWithUserWrite(user)
				.setPreferAnalyzedFields(isPreferAnalyzedFields());
		return query;
	}

	private LogicalSearchQuery buildLogicalSearchQueryWithUnselectedIds(User user, AdvancedSearchViewImpl view,
																		List<String> unselectedRecordIds,
																		String schemaType) {
		LogicalSearchQuery query = getSearchQuery(user, view, schemaType);
		query.setCondition(query.getCondition().andWhere(Schemas.IDENTIFIER).isNotIn(unselectedRecordIds)
				.andWhere(Schemas.LOGICALLY_DELETED_STATUS).isFalseOrNull())
				.filteredWithUser(user).filteredWithUserWrite(user)
				.setPreferAnalyzedFields(isPreferAnalyzedFields());
		return query;
	}

	private LogicalSearchQuery buildLogicalSearchQueryWithAllRecords(User user, AdvancedSearchViewImpl view,
																	 String schemaType) {
		LogicalSearchQuery query = getSearchQuery(user, view, schemaType);
		query.setCondition(query.getCondition().andWhere(Schemas.LOGICALLY_DELETED_STATUS).isFalseOrNull())
				.filteredWithUser(user).filteredWithUserWrite(user)
				.setPreferAnalyzedFields(isPreferAnalyzedFields());
		return query;
	}

	private LogicalSearchQuery buildUnsecuredLogicalSearchQueryWithSelectedIds(User user, AdvancedSearchViewImpl view,
																			   List<String> selectedRecordIds,
																			   String schemaType) {
		LogicalSearchQuery query = getSearchQuery(user, view, schemaType);
		query.setCondition(query.getCondition().andWhere(Schemas.IDENTIFIER).isIn(selectedRecordIds)
				.andWhere(Schemas.LOGICALLY_DELETED_STATUS).isFalseOrNull())
				.setPreferAnalyzedFields(isPreferAnalyzedFields());
		return query;
	}

	private LogicalSearchQuery buildUnsecuredLogicalSearchQueryWithUnselectedIds(User user, AdvancedSearchViewImpl view,
																				 List<String> unselectedRecordIds,
																				 String schemaType) {
		LogicalSearchQuery query = getSearchQuery(user, view, schemaType);
		query.setCondition(query.getCondition().andWhere(Schemas.IDENTIFIER).isNotIn(unselectedRecordIds)
				.andWhere(Schemas.LOGICALLY_DELETED_STATUS).isFalseOrNull())
				.setPreferAnalyzedFields(isPreferAnalyzedFields());
		return query;
	}

	private LogicalSearchQuery buildUnsecuredLogicalSearchQueryWithAllRecords(User user, AdvancedSearchViewImpl view,
																			  String schemaType) {
		LogicalSearchQuery query = getSearchQuery(user, view, schemaType);
		query.setCondition(query.getCondition().andWhere(Schemas.LOGICALLY_DELETED_STATUS).isFalseOrNull())
				.setPreferAnalyzedFields(isPreferAnalyzedFields());
		return query;
	}

	private LogicalSearchQuery getSearchQuery(User user, AdvancedSearchViewImpl view, String schemaType) {
		String userSearchExpression = view.getSearchExpression();
		LogicalSearchQuery query = new LogicalSearchQuery(getSearchCondition(user, view, schemaType))
				.setOverridedQueryParams(extraSolrParams)
				.setFreeTextQuery(userSearchExpression)
				.filteredWithUser(user)
				.filteredByStatus(StatusFilter.ACTIVES)
				.setPreferAnalyzedFields(isPreferAnalyzedFields());

		query.setReturnedMetadatas(ReturnedMetadatasFilter.allExceptContentAndLargeText());

		SchemasRecordsServices schemas = new SchemasRecordsServices(collection, appLayerFactory.getModelLayerFactory());
		LogicalSearchQueryFacetFilters filters = query.getFacetFilters();
		filters.clear();

		SavedSearch savedSearch = new SavedSearch(recordServices.getDocumentById(view.getSavedSearchId()), types());
		Map<String, Set<String>> facetSelections = savedSearch.getSelectedFacets();

		for (Entry<String, Set<String>> selection : facetSelections.entrySet()) {
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
				log.warn("Facet '" + id + "' has been deleted");
			}
		}
		return query;
	}

	private LogicalSearchCondition getSearchCondition(User user, AdvancedSearchViewImpl view, String schemaType) {
		try {
			return buildSearchCondition(user, view, schemaType);
		} catch (ConditionException e) {
			throw new RuntimeException("Unexpected exception (should be unreachable)", e);
		}
	}

	private LogicalSearchCondition buildSearchCondition(User user, AdvancedSearchViewImpl view, String schemaType)
			throws ConditionException {
		List<Criterion> searchCriteria = view.getSearchCriteria();

		String languageCode = searchServices.getLanguageCode(collection);
		MetadataSchemaType type = metadataSchemaType(schemaType);
		LogicalSearchCondition condition = (searchCriteria.isEmpty()) ?
										   from(type).returnAll() :
										   new ConditionBuilder(type, languageCode).build(searchCriteria);
		return appCollectionExtensions.adjustSearchPageCondition(new SearchPageConditionParam((Component) view, condition, user));
	}

	private boolean isPreferAnalyzedFields() {
		return false;
	}

	public final MetadataSchemaTypes types() {
		MetadataSchemasManager metadataSchemasManager = modelLayerFactory.getMetadataSchemasManager();
		return metadataSchemasManager.getSchemaTypes(collection);
	}

	public final MetadataSchema metadataSchema(String code) {
		return types().getSchema(code);
	}

	public final MetadataSchemaType metadataSchemaType(String code) {
		return types().getSchemaType(code);
	}

	private List<String> getListSearchableMetadataSchemaType() {
		if (listSearchableMetadataSchemaType == null) {
			listSearchableMetadataSchemaType = new ArrayList<>();
			for (MetadataSchemaType schemaType : appLayerFactory.getModelLayerFactory().getMetadataSchemasManager()
					.getSchemaTypes(collection).getSchemaTypes()) {
				if (isMetadataSchemaTypesSearchable(schemaType)) {
					listSearchableMetadataSchemaType.add(schemaType.getCode());
				}
			}
		}
		return listSearchableMetadataSchemaType;
	}

	private boolean isMetadataSchemaTypesSearchable(MetadataSchemaType types) {
		return appLayerFactory.getMetadataSchemasDisplayManager().getType(collection, types.getCode()).isAdvancedSearch();
	}

	protected String getSchemaType(LogicalSearchQuery query) {
		List<String> schemaTypes = query.getCondition().getFilterSchemaTypesCodes();
		return schemaTypes != null && !schemaTypes.isEmpty() ? schemaTypes.get(0) : null;
	}
}
