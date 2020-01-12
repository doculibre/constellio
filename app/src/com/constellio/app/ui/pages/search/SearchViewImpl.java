package com.constellio.app.ui.pages.search;

import com.constellio.app.api.extensions.ExtraTabForSimpleSearchResultExtention.ExtraTabInfo;
import com.constellio.app.api.extensions.params.ExtraTabForSimpleSearchResultParams;
import com.constellio.app.services.menu.MenuItemFactory.MenuItemRecordProvider;
import com.constellio.app.ui.application.ConstellioUI;
import com.constellio.app.ui.entities.*;
import com.constellio.app.ui.framework.buttons.*;
import com.constellio.app.ui.framework.buttons.WindowButton.WindowConfiguration;
import com.constellio.app.ui.framework.components.RecordDisplayFactory;
import com.constellio.app.ui.framework.components.SearchResultDisplay;
import com.constellio.app.ui.framework.components.SearchResultTable;
import com.constellio.app.ui.framework.components.capsule.CapsuleComponent;
import com.constellio.app.ui.framework.components.fields.BaseComboBox;
import com.constellio.app.ui.framework.components.fields.BaseTextField;
import com.constellio.app.ui.framework.components.fields.list.ListAddRemoveRecordLookupField;
import com.constellio.app.ui.framework.components.layouts.I18NHorizontalLayout;
import com.constellio.app.ui.framework.components.menuBar.RecordListMenuBar;
import com.constellio.app.ui.framework.components.search.FacetsPanel;
import com.constellio.app.ui.framework.components.search.FacetsSliderPanel;
import com.constellio.app.ui.framework.components.search.ViewableRecordVOSearchResultTable;
import com.constellio.app.ui.framework.components.selection.SelectionComponent.SelectionChangeListener;
import com.constellio.app.ui.framework.components.table.BaseTable;
import com.constellio.app.ui.framework.components.table.BaseTable.ItemsPerPageChangeEvent;
import com.constellio.app.ui.framework.components.viewers.panel.ViewableRecordVOTablePanel;
import com.constellio.app.ui.framework.containers.SearchResultContainer;
import com.constellio.app.ui.framework.containers.SearchResultVOLazyContainer;
import com.constellio.app.ui.framework.data.SearchResultVODataProvider;
import com.constellio.app.ui.framework.stream.DownloadStreamResource;
import com.constellio.app.ui.pages.base.BaseViewImpl;
import com.constellio.app.ui.pages.search.SearchPresenter.SortOrder;
import com.constellio.data.utils.KeySetMap;
import com.constellio.data.utils.dev.Toggle;
import com.constellio.model.entities.records.Record;
import com.constellio.model.entities.records.wrappers.Capsule;
import com.constellio.model.entities.records.wrappers.Group;
import com.constellio.model.entities.records.wrappers.SavedSearch;
import com.constellio.model.entities.records.wrappers.User;
import com.constellio.model.services.search.query.logical.LogicalSearchQuery;
import com.vaadin.data.Container.ItemSetChangeEvent;
import com.vaadin.data.Container.ItemSetChangeListener;
import com.vaadin.data.Property;
import com.vaadin.data.Property.ValueChangeEvent;
import com.vaadin.data.Property.ValueChangeListener;
import com.vaadin.event.ItemClickEvent;
import com.vaadin.event.ItemClickEvent.ItemClickListener;
import com.vaadin.lazyloadwrapper.LazyLoadWrapper;
import com.vaadin.navigator.ViewChangeListener.ViewChangeEvent;
import com.vaadin.server.FontAwesome;
import com.vaadin.server.Page;
import com.vaadin.server.Page.BrowserWindowResizeEvent;
import com.vaadin.server.Page.BrowserWindowResizeListener;
import com.vaadin.server.Resource;
import com.vaadin.shared.ui.label.ContentMode;
import com.vaadin.ui.AbstractSelect.ItemCaptionMode;
import com.vaadin.ui.*;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.Button.ClickListener;
import com.vaadin.ui.TabSheet.SelectedTabChangeEvent;
import com.vaadin.ui.TabSheet.SelectedTabChangeListener;
import com.vaadin.ui.TabSheet.Tab;
import com.vaadin.ui.Table.ColumnHeaderMode;
import com.vaadin.ui.themes.ValoTheme;
import org.jetbrains.annotations.Nullable;
import org.vaadin.dialogs.ConfirmDialog;
import org.vaadin.sliderpanel.SliderPanel;
import org.vaadin.sliderpanel.client.SliderPanelListener;

import java.util.*;

import static com.constellio.app.ui.framework.components.BaseForm.BUTTONS_LAYOUT;
import static com.constellio.app.ui.i18n.i18n.$;
import static com.constellio.app.ui.i18n.i18n.isRightToLeft;

public abstract class SearchViewImpl<T extends SearchPresenter<? extends SearchView>> extends BaseViewImpl implements SearchView, BrowserWindowResizeListener {

	public static final String FACET_BOX_STYLE = "facet-box";
	public static final String FACET_TITLE_STYLE = "facet-title";
	public static final String SORT_BOX_STYLE = "sort-box";
	public static final String SORT_TITLE_STYLE = "sort-title";
	public static final String SAVE_SEARCH = "save-search";
	//public static final Resource SELECTION_ICON_RESOURCE = new ThemeResource("images/icons/clipboard_12x16.png");
	public static final Resource SELECTION_ICON_RESOURCE = FontAwesome.SHOPPING_BASKET;

	public enum ShareType {
		NONE, ALL, RESTRICTED
	}

	protected T presenter;
	private VerticalLayout thesaurusDisambiguation;
	private VerticalLayout spellCheckerSuggestions;
	private VerticalLayout summary;
	private Component resultsAndFacetsPanel;
	private VerticalLayout resultsArea;
	private SliderPanel facetsSliderPanel;
	private FacetsPanel facetsArea;
	private VerticalLayout capsuleArea;

	private SearchResultTable resultsTable;
	private SelectDeselectAllButton selectDeselectAllButton;
	private Button addToSelectionButton;
	private HashMap<Integer, Boolean> hashMapAllSelection = new HashMap<>();
	private List<SaveSearchListener> saveSearchListenerList = new ArrayList<>();
	private Map<String, String> extraParameters = null;
	private boolean lazyLoadedSearchResults;
	private List<SelectionChangeListener> selectionChangeListenerStorage = new ArrayList<>();
	private boolean facetsOpened;
	private TabSheet resultTabSheet;
	private boolean hideFacette;

	@Override
	public void attach() {
		super.attach();
		Page.getCurrent().addBrowserWindowResizeListener(this);
	}

	@Override
	public void detach() {
		Page.getCurrent().removeBrowserWindowResizeListener(this);
		super.detach();
	}

	public void addSaveSearchListenerList(SaveSearchListener saveSearchListener) {
		saveSearchListenerList.add(saveSearchListener);
	}

	@Override
	protected boolean isFullWidthIfActionMenuAbsent() {
		return true;
	}

	@Override
	protected String getTitle() {
		return null;
	}

	@Override
	protected void initBeforeCreateComponents(ViewChangeEvent event) {
		presenter.resetFacetSelection();
		presenter.forRequestParameters(event.getParameters());
	}

	@Override
	protected Component buildMainComponent(ViewChangeEvent event) {
		VerticalLayout layout = new VerticalLayout();
		layout.setId("search-view");
		layout.addStyleName(layout.getId());
		layout.addStyleName("search-main-container");
		Component searchUIComponent = buildSearchUI();
		if (searchUIComponent != null) {
			layout.addComponent(searchUIComponent);
		}
		layout.addComponent(buildResultsUI());
		layout.addComponent(buildThesaurusSemanticNetwork());
		layout.setSpacing(true);
		if (presenter.mustDisplayResults()) {
			refreshSearchResultsAndFacets(false);
		}
		return layout;
	}

	protected Map<String, String> getExtraParameters() {
		return extraParameters;
	}

	protected void setExtraParameters(Map<String, String> extraParameters) {
		this.extraParameters = extraParameters;
	}

	private void buildThesaurusDisambiguation(List<String> disambiguationSuggestions) {
		if (disambiguationSuggestions != null && disambiguationSuggestions.size() > 0) {
			thesaurusDisambiguation.setVisible(true);

			VerticalLayout suggestionsLayout = new VerticalLayout();
			suggestionsLayout.addStyleName("disambiguation-suggestions");

			Label title = new Label($("SearchView.disambiguation.title", presenter.getUserSearchExpression()));
			title.addStyleName("disambiguation-title");
			title.setContentMode(ContentMode.HTML);
			thesaurusDisambiguation.addComponent(title);
			thesaurusDisambiguation.addComponent(suggestionsLayout);

			for (final String entry : disambiguationSuggestions) {
				Button suggestionButton = new Button();
				suggestionButton.setCaption(entry);
				suggestionButton.addClickListener(new ClickListener() {
					@Override
					public void buttonClick(ClickEvent event) {
						presenter.disambiguationClicked(entry);
					}
				});
				suggestionButton.addStyleName(ValoTheme.BUTTON_LINK);
				suggestionButton.addStyleName("disambiguation-suggestion");
				suggestionsLayout.addComponent(suggestionButton);
			}
		} else {
			thesaurusDisambiguation.setVisible(false);
		}
	}

	public Component buildThesaurusSemanticNetwork() {
		List<String> semanticNetworkSuggestions = presenter.getThesaurusSemanticNetworkSuggestions();

		VerticalLayout semanticNetworkLayout = new VerticalLayout();
		semanticNetworkLayout.addStyleName("thesaurus-semantic-network");

		if (semanticNetworkSuggestions != null && semanticNetworkSuggestions.size() > 0) {
			Label title = new Label($("SearchView.suggestion.title", presenter.getUserSearchExpression()));
			title.setStyleName("thesaurus-semantic-network-title");
			semanticNetworkLayout.addComponent(title);

			int columnCount = 3;
			int columnIndex = 0;
			int suggestionIndex = 0;

			double suggestionsPerColumnDouble = (double) semanticNetworkSuggestions.size() / columnCount;
			int suggestionsPerColumnInt = (int) suggestionsPerColumnDouble;
			double suggestionsPerColumnDecimal = suggestionsPerColumnDouble - suggestionsPerColumnInt;
			if (suggestionsPerColumnDecimal > 0) {
				suggestionsPerColumnInt++;
			}

			CssLayout cssLayout = new CssLayout();
			cssLayout.addStyleName("thesaurus-semantic-network-columns");
			semanticNetworkLayout.addComponent(cssLayout);

			VerticalLayout currentColumnLayout = null;
			for (final String semanticNetworkSuggestion : semanticNetworkSuggestions) {
				if (currentColumnLayout == null || suggestionIndex % suggestionsPerColumnInt == 0) {
					currentColumnLayout = new VerticalLayout();
					currentColumnLayout.addStyleName("thesaurus-semantic-network-column");
					currentColumnLayout.addStyleName("thesaurus-semantic-network-column" + (columnIndex + 1));

					if (columnIndex == 2) {
						columnIndex = 0;
					}
					currentColumnLayout.setWidthUndefined();
					cssLayout.addComponent(currentColumnLayout);
				}
				suggestionIndex++;
				Button button = new Button();
				button.addStyleName("thesaurus-semantic-network-suggestion");
				button.setCaption(semanticNetworkSuggestion);
				button.addClickListener(new ClickListener() {
					@Override
					public void buttonClick(ClickEvent event) {
						presenter.thesaurusSemanticNetworkSuggestionClicked(semanticNetworkSuggestion);
					}
				});
				button.addStyleName(ValoTheme.BUTTON_LINK);
				currentColumnLayout.addComponent(button);
			}
		} else {
			semanticNetworkLayout.setVisible(false);
		}
		return semanticNetworkLayout;
	}

	@Override
	public void setSearchExpression(String expression) {
		ConstellioUI.getCurrent().getHeader().setSearchExpression(expression);
	}

	public void refreshSearchResultsAndFacets(boolean temporarySave) {
		SearchResultVODataProvider dataProvider = refreshSearchResults(temporarySave, facetsOpened);
		refreshFacets(dataProvider);
	}

	@Override
	public void refreshSearchResultsAndFacets() {
		SearchResultVODataProvider dataProvider = refreshSearchResults(true, facetsOpened);
		refreshFacets(dataProvider);
	}

	@Override
	public SearchResultVODataProvider refreshSearchResults(boolean temporarySave, boolean includeFacets) {
		if (temporarySave) {
			SavedSearch savedSearch = presenter.saveTemporarySearch(false);

			for (SaveSearchListener saveSearchListener : saveSearchListenerList) {
				saveSearchListener.save(new SaveSearchListener.Event(savedSearch));
			}
		}

		final SearchResultVODataProvider dataProvider = presenter.getSearchResults(includeFacets);
		summary.removeAllComponents();
		resultsArea.removeAllComponents();
		spellCheckerSuggestions.removeAllComponents();
		resultsTable = buildResultTable(dataProvider);

		List<String> disambiguationSuggestions = presenter.getDisambiguationSuggestions();
		buildThesaurusDisambiguation(disambiguationSuggestions);
		buildSpellCheckerSuggestions(dataProvider, disambiguationSuggestions);

		summary.removeAllComponents();
		Component summaryComponent = buildSummary(resultsTable);
		if (summaryComponent != null) {
			summary.setVisible(true);
			summary.addComponent(summaryComponent);
		} else {
			summary.setVisible(false);
		}

		/* Code from 8.3
		if (lazyLoadedSearchResults) {
			resultsArea.addComponent(new LazyLoadWrapper(results));
		} else {
			resultsArea.addComponent(results);
		}

		resultsArea.addComponent(new LazyLoadWrapper(resultsTable));
		if (isDetailedView()) {
			resultsArea.addComponent(((SearchResultDetailedTable) resultsTable).createControls());
			((SearchResultDetailedTable) resultsTable).setItemsPerPageValue(presenter.getSelectedPageLength());
		}*/


		boolean detailedView = isDetailedView();
		createResultArea();

		if (detailedView) {
			((ViewableRecordVOSearchResultTable) resultsTable).setItemsPerPageValue(presenter.getSelectedPageLength());
		}

		refreshCapsule();

		return dataProvider;
	}

	protected boolean getExtraTab() {
		return false;
	}

	private void createResultArea() {
		List<ExtraTabInfo> extraTabInfoList = null;

		if (getExtraTab()) {
			extraTabInfoList = getConstellioFactories().getAppLayerFactory().getExtensions()
					.forCollection(getCollection()).getExtraTabForSimpleSearchResult(new ExtraTabForSimpleSearchResultParams(presenter.getUserSearchExpression()));
		}
		resultsArea.removeAllComponents();
		if (extraTabInfoList == null || extraTabInfoList.isEmpty()) {
			if (lazyLoadedSearchResults) {
				resultsArea.addComponent(new LazyLoadWrapper(resultsTable));
			} else {
				resultsArea.addComponent(resultsTable);
			}
		} else {
			resultTabSheet = new TabSheet();
			Tab constellioTab;

			if (lazyLoadedSearchResults) {
				constellioTab = resultTabSheet.addTab(new LazyLoadWrapper(resultsTable), $("SearchView.constellioResultTab"));
			} else {
				constellioTab = resultTabSheet.addTab(resultsTable, $("SearchView.constellioResultTab"));
			}

			for (ExtraTabInfo currentExtraTab : extraTabInfoList) {
				resultTabSheet.addTab(currentExtraTab.getTabComponent(), currentExtraTab.getTabCaption());
			}

			resultTabSheet.addSelectedTabChangeListener(new SelectedTabChangeListener() {
				@Override
				public void selectedTabChange(SelectedTabChangeEvent event) {
					Component currentTab = event.getTabSheet().getSelectedTab();
					hideFacette = currentTab != constellioTab.getComponent();
					facetsSliderPanel.setVisible(!hideFacette);

					if (currentTab instanceof BaseViewImpl) {
						((BaseViewImpl) currentTab).enter(null);
					}

				}
			});

			resultsArea.addComponent(resultTabSheet);
		}
	}

	@Override
	public void setLazyLoadedSearchResults(boolean lazyLoadedSearchResults) {
		this.lazyLoadedSearchResults = lazyLoadedSearchResults;
	}

	private boolean isDetailedView() {
		return !SearchResultsViewMode.TABLE.equals(presenter.getResultsViewMode());
	}

	@Override
	public void refreshFacets(SearchResultVODataProvider dataProvider) {
		List<FacetVO> facets = presenter.getFacets(dataProvider);
		KeySetMap<String, String> facetSelections = presenter.getFacetSelections();
		List<MetadataVO> sortableMetadata = presenter.getMetadataAllowedInSort();
		String sortCriterionValue = presenter.getSortCriterionValueAmong(sortableMetadata);
		SortOrder sortOrder = presenter.getSortOrder();
		if (facetsOpened) {
			facetsArea.refresh(facets, facetSelections, sortableMetadata, sortCriterionValue, sortOrder);
			facetsSliderPanel.setHeightUndefined();
		}
		presenter.setPageNumber(1);
		facetsSliderPanel.setVisible(!hideFacette && (dataProvider.size() > 0 || !facetSelections.isEmpty()));
	}

	@Override
	public List<String> getSelectedRecordIds() {
		if (resultsTable != null) {
			return resultsTable.getSelectedRecordIds();
		} else {
			return Collections.emptyList();
		}
	}

	@Override
	public List<String> getUnselectedRecordIds() {
		return resultsTable.getUnselectedRecordIds();
	}

	protected abstract Component buildSearchUI();

	protected Component buildSummary(SearchResultTable results) {
		List<Component> actions = Arrays.asList(/*
				(Component) buildSelectAllButton(), buildAddToSelectionButton(), buildSavedSearchButton() */);
		return results.createSummary(actions, buildAddToSelectionButton(), buildZipButton());
	}

	private Component buildResultsUI() {
		spellCheckerSuggestions = new VerticalLayout();
		spellCheckerSuggestions.addStyleName("spell-checker");
		spellCheckerSuggestions.setWidth("100%");
		spellCheckerSuggestions.setVisible(false);

		thesaurusDisambiguation = new VerticalLayout();
		thesaurusDisambiguation.setWidth("100%");
		thesaurusDisambiguation.addStyleName("thesaurus-disambiguation");
		thesaurusDisambiguation.setVisible(false);

		summary = new VerticalLayout();
		summary.addStyleName("search-result-summary");

		resultsArea = new VerticalLayout();
		resultsArea.addStyleName("search-result-area");
		//		resultsArea.setWidth("100%");
		resultsArea.setSpacing(true);

		facetsArea = new FacetsPanel() {
			@Override
			protected void sortCriterionSelected(String sortCriterion, SortOrder sortOrder) {
				presenter.sortCriterionSelected(sortCriterion, sortOrder);
			}

			@Override
			protected void facetDeselected(String id) {
				presenter.facetDeselected(id);
			}

			@Override
			protected void facetValueSelected(String facetId, String value) {
				presenter.facetValueSelected(facetId, value);
			}

			@Override
			protected void facetValueDeselected(String facetId, String value) {
				presenter.facetValueDeselected(facetId, value);
			}

			@Override
			protected void facetOpened(String id) {
				presenter.facetOpened(id);
			}

			@Override
			protected void facetClosed(String id) {
				presenter.facetClosed(id);
			}

		};

		//		if (Toggle.SEARCH_RESULTS_VIEWER.isEnabled()) {
		//			viewableSearchResultsPanel = new ViewableRecordTablePanel(resultsArea);
		//
		//			CollapsibleHorizontalSplitPanel body = new CollapsibleHorizontalSplitPanel("search-result-and-facets-container");
		//			body.addStyleName("search-result-and-facets-container");
		//			body.setSecondComponentWidth(250, Unit.PIXELS);
		//			body.setFirstComponent(viewableSearchResultsPanel);
		//			body.setSecondComponent(facetsArea);
		//
		//			resultsAndFacetsPanel = body;
		//		} else {

		facetsSliderPanel = new FacetsSliderPanel(facetsArea);
		facetsSliderPanel.addListener((SliderPanelListener) (expand) -> {
			this.facetsOpened = expand;
			if (facetsOpened) {
				final SearchResultVODataProvider dataProvider = presenter.getSearchResults(true);
				refreshFacets(dataProvider);
			}
		});
		facetsSliderPanel.setHeight("800px");
		facetsSliderPanel.setVisible(!hideFacette);
		facetsSliderPanel.setImmediate(true);

		I18NHorizontalLayout body = new I18NHorizontalLayout(resultsArea, facetsSliderPanel);
		body.addStyleName("search-result-and-facets-container");
		body.setWidth("100%");
		body.setHeight("100%");
		body.setExpandRatio(resultsArea, 1);
		body.setSpacing(true);

		resultsAndFacetsPanel = body;
		//		}

		refreshCapsule();

		VerticalLayout main = new VerticalLayout(thesaurusDisambiguation, spellCheckerSuggestions, summary, capsuleArea);

		main.addComponent(resultsAndFacetsPanel);
		main.addStyleName("suggestions-summary-results-facets");
		main.setWidth("100%");
		main.setHeight("100%");
		main.setSpacing(true);

		return main;
	}

	@Nullable
	private void refreshCapsule() {
		if (capsuleArea == null) {
			capsuleArea = new VerticalLayout();
			capsuleArea.addStyleName("search-capsule");
		} else {
			capsuleArea.removeAllComponents();
		}

		Component capsuleComponent = null;
		if (Toggle.ADVANCED_SEARCH_CONFIGS.isEnabled()) {
			Capsule capsule = presenter.getCapsuleForCurrentSearch();
			if (capsule != null) {
				capsuleComponent = buildCapsuleUI(capsule);
			}
		}

		if (capsuleComponent != null) {
			capsuleArea.addComponent(capsuleComponent);
			capsuleArea.setVisible(true);
		} else {
			capsuleArea.setVisible(false);
		}
	}

	protected SearchResultTable buildResultTable(SearchResultVODataProvider dataProvider) {
		//		final SearchResultContainer container = buildResultContainer(dataProvider);
		final SearchResultVOLazyContainer container = new SearchResultVOLazyContainer(dataProvider);
		//        final RecordVOLazyContainer container = new RecordVOLazyContainer(presenter.getSearchResultsAsRecordVOs());
		container.addItemSetChangeListener(new ItemSetChangeListener() {
			@Override
			public void containerItemSetChange(ItemSetChangeEvent event) {
				presenter.searchNavigationButtonClicked();
			}
		});

		RecordDisplayFactory displayFactory = new RecordDisplayFactory(getSessionContext().getCurrentUser(), extraParameters);

		final boolean indexVisible = presenter.isShowNumberingColumn(dataProvider);
		ViewableRecordVOSearchResultTable.TableMode tableMode;
		if (this.resultsTable != null) {
			tableMode = ((ViewableRecordVOSearchResultTable) this.resultsTable).getTableMode();
		} else {
			tableMode = null;
		}
		ViewableRecordVOSearchResultTable viewerPanel = new ViewableRecordVOSearchResultTable(container, tableMode, presenter, getRecordListMenuBar(), this) {
			@Override
			public boolean isIndexVisible() {
				return indexVisible;
			}

			@Override
			protected boolean isPagedInListMode() {
				return true;
			}

			@Override
			public boolean isSelectionActionMenuBar() {
				return SearchViewImpl.this.isSelectionActionMenuBar();
			}

			@Override
			protected MenuItemRecordProvider getMenuItemProvider() {
				return new MenuItemRecordProvider() {
					@Override
					public List<Record> getRecords() {
						return getSelectedRecords();
					}

					@Override
					public LogicalSearchQuery getQuery() {
						return presenter.getSearchQuery();
					}
				};
			}

			@Override
			protected List<String> excludedMenuItemInDefaultSelectionActionButtons() {
				return menuItemToExcludeInSelectionMenu();
			}

			@Override
			protected Component newSearchResultComponent(Object itemId) {
				Integer index = (Integer) itemId;
				String query = presenter.getSearchQuery().getFreeTextQuery();
				SearchResultVO searchResultVO = container.getSearchResultVO(index);
				ClickListener elevationClickListener = getElevationClickListener(searchResultVO, index);
				ClickListener exclusionClickListener = getExclusionClickListener(searchResultVO, index);
				SearchResultDisplay searchResultDisplay = displayFactory.build(searchResultVO, query, null, elevationClickListener, exclusionClickListener);
				return searchResultDisplay;
			}

			private ClickListener getElevationClickListener(final SearchResultVO searchResultVO, Integer index) {
				return new ClickListener() {
					@Override
					public void buttonClick(ClickEvent event) {
						((SearchPresenter<?>) presenter).searchResultElevationClicked(searchResultVO.getRecordVO());
					}
				};
			}

			private ClickListener getExclusionClickListener(final SearchResultVO searchResultVO, Integer index) {
				return new ClickListener() {
					@Override
					public void buttonClick(ClickEvent event) {
						((SearchPresenter<?>) presenter).searchResultExclusionClicked(searchResultVO.getRecordVO());
					}
				};
			}
		};
		viewerPanel.setSearchTerm(presenter.getUserSearchExpression());
		viewerPanel.addItemClickListener(new ItemClickListener() {
			@Override
			public void itemClick(ItemClickEvent event) {
				Object itemId = event.getItemId();
				Integer index = (Integer) itemId;
				SearchResultVO searchResultVO = container.getSearchResultVO(index);
				presenter.searchResultClicked(searchResultVO.getRecordVO(), index);
			}
		});

		viewerPanel.setQuickActionButton(getQuickActionMenuButtons());
		selectionChangeListenerStorage.forEach(viewerPanel::addSelectionChangeListener);


		int currentPage = presenter.getPageNumber();
		int itemsPerPage = presenter.getSelectedPageLength();

		viewerPanel.setItemsPerPageValue(itemsPerPage);
		viewerPanel.getActualTable().setItemsPerPage(itemsPerPage);
		viewerPanel.getActualTable().setCurrentPage(currentPage);
		viewerPanel.getActualTable().addItemsPerPageChangeListener(new BaseTable.ItemsPerPageChangeListener() {
			@Override
			public void itemsPerPageChanged(ItemsPerPageChangeEvent event) {
				int newItemsPerPage = event.getNewItemsPerPage();
				container.getQueryView().getQueryDefinition().setBatchSize(newItemsPerPage);
				presenter.setSelectedPageLength(newItemsPerPage);
			}
		});
		viewerPanel.getActualTable().addPageChangeListener(new BaseTable.PageChangeListener() {
			public void pageChanged(BaseTable.PageChangeEvent event) {
				presenter.setPageNumber(event.getCurrentPage());
				presenter.saveTemporarySearch(false);
				//					if (selectDeselectAllButton != null) {
				//						hashMapAllSelection.put(presenter.getLastPageNumber(), selectDeselectAllButton.isSelectAllMode());
				//						Boolean objIsSelectAllMode = hashMapAllSelection.get(new Integer(presenter.getPageNumber()));
				//						boolean isSelectAllMode = true;
				//						if (objIsSelectAllMode != null) {
				//							isSelectAllMode = objIsSelectAllMode;
				//						}
				//						selectDeselectAllButton.setSelectAllMode(isSelectAllMode);
				//					}
			}
		});

		return viewerPanel;
	}

	public boolean isSelectionActionMenuBar() {
		return true;
	}

	@Override
	protected List<Button> getQuickActionMenuButtons() {
		return Arrays.asList(buildSavedSearchButton());
	}

	public void addSelectionChangeListener(SelectionChangeListener selectionChangeListener) {
		if (resultsTable == null) {
			selectionChangeListenerStorage.add(selectionChangeListener);
		} else {
			resultsTable.addSelectionChangeListener(selectionChangeListener);
			selectionChangeListenerStorage.add(selectionChangeListener);
		}
	}

	protected List<String> getSelectedRecords() {
		if (resultsTable == null) {
			return resultsTable.getSelectedRecordIds();
		} else {
			return Collections.emptyList();
		}
	}

	private void scrollBackUp() {
		getUI().scrollIntoView(summary);
	}

	protected SearchResultContainer buildResultContainer(SearchResultVODataProvider dataProvider) {
		RecordDisplayFactory displayFactory = new RecordDisplayFactory(getSessionContext().getCurrentUser(), extraParameters);
		SearchResultVOLazyContainer results = new SearchResultVOLazyContainer(dataProvider);
		SearchResultContainer container = new SearchResultContainer(results, displayFactory,
				presenter.getSearchQuery().getFreeTextQuery(), presenter.isShowNumberingColumn(dataProvider)) {
			@Override
			protected ClickListener getClickListener(final SearchResultVO searchResultVO, final Integer index) {
				return new ClickListener() {
					@Override
					public void buttonClick(ClickEvent event) {
						presenter.searchResultClicked(searchResultVO.getRecordVO(), index);
					}
				};
			}

			@Override
			protected ClickListener getElevationClickListener(final SearchResultVO searchResultVO, Integer index) {
				return new ClickListener() {
					@Override
					public void buttonClick(ClickEvent event) {
						((SearchPresenter<?>) presenter).searchResultElevationClicked(searchResultVO.getRecordVO());
					}
				};
			}

			@Override
			protected ClickListener getExclusionClickListener(final SearchResultVO searchResultVO, Integer index) {
				return new ClickListener() {
					@Override
					public void buttonClick(ClickEvent event) {
						((SearchPresenter<?>) presenter).searchResultExclusionClicked(searchResultVO.getRecordVO());
					}
				};
			}
		};

		container.addItemSetChangeListener(new ItemSetChangeListener() {
			@Override
			public void containerItemSetChange(ItemSetChangeEvent event) {
				presenter.searchNavigationButtonClicked();
			}
		});

		return container;
	}

	private void buildSpellCheckerSuggestions(SearchResultVODataProvider dataProvider,
											  List<String> disambiguationSuggestions) {
		if (!presenter.mustDisplaySpellCheckerSuggestions(dataProvider, disambiguationSuggestions)) {
			spellCheckerSuggestions.setVisible(false);
			return;
		}

		Label spellCheckerMessage = new Label($("SearchView.spellChecker"));
		spellCheckerMessage.addStyleName("spell-checker-message");
		spellCheckerMessage.setWidthUndefined();

		CssLayout suggestionsLayout = new CssLayout();
		suggestionsLayout.setWidth("100%");
		suggestionsLayout.addStyleName("spell-checker-suggestions");
		spellCheckerSuggestions.addComponent(suggestionsLayout);

		suggestionsLayout.addComponent(spellCheckerMessage);

		List<String> foundSuggestions = presenter.getAllNonExcluded(getCollection(), presenter.getSuggestions());
		for (final String suggestion : foundSuggestions) {
			HorizontalLayout suggestionLayout = new HorizontalLayout();
			suggestionLayout.addStyleName("spell-checker-suggestion-and-exclude");

			Button suggestionLink = new Button(suggestion);
			suggestionLink.addStyleName(ValoTheme.BUTTON_LINK);
			suggestionLink.addStyleName("spell-checker-suggestion");
			suggestionLink.addClickListener(new ClickListener() {
				@Override
				public void buttonClick(ClickEvent event) {
					presenter.suggestionSelected(suggestion);
				}
			});

			boolean removeButtonVisible = presenter.isSpellCheckerExcludeButtonVisible();
			DeleteButton removeButton = new DeleteButton(FontAwesome.REMOVE, $("SearchView.spellChecker.delete"), true) {
				@Override
				protected void confirmButtonClick(ConfirmDialog dialog) {
					presenter.deleteSuggestionButtonClicked(suggestion, getCollection());
				}
			};
			removeButton.setVisible(removeButtonVisible);
			removeButton.addStyleName(ValoTheme.BUTTON_BORDERLESS);
			removeButton.addStyleName("spell-checker-suggestion-remove");
			if (removeButtonVisible) {
				suggestionLayout.addStyleName("spell-checker-suggestion-remove-visible");
			}

			suggestionLayout.addComponent(suggestionLink);
			suggestionLayout.addComponent(removeButton);
			suggestionsLayout.addComponent(suggestionLayout);
		}
		if (foundSuggestions.size() > 0) {
			spellCheckerSuggestions.setVisible(true);
		} else {
			spellCheckerSuggestions.setVisible(false);
		}
	}

	private Component buildSortComponent() {
		Label sortBy = new Label($("SearchView.sortBy"));
		sortBy.addStyleName(ValoTheme.LABEL_BOLD);
		sortBy.addStyleName(SORT_TITLE_STYLE);

		final ComboBox criterion = new BaseComboBox();
		criterion.setItemCaptionMode(ItemCaptionMode.EXPLICIT);
		criterion.setWidth("100%");

		List<MetadataVO> sortableMetadata = presenter.getMetadataAllowedInSort();
		for (MetadataVO metadata : sortableMetadata) {
			criterion.addItem(metadata.getCode());
			criterion.setItemCaption(metadata.getCode(), metadata.getLabel());
		}
		//		criterion.setPageLength(criterion.size());
		criterion.addItem("SearchSortType.RELEVENCE");
		criterion.setNullSelectionItemId("SearchSortType.RELEVENCE");
		criterion.setItemCaption("SearchSortType.RELEVENCE", $("SearchSortType.RELEVENCE"));
		criterion.setValue(presenter.getSortCriterionValueAmong(sortableMetadata));

		final OptionGroup order = new OptionGroup();
		order.addItem(SortOrder.ASCENDING);
		order.setItemCaption(SortOrder.ASCENDING, $("SearchView.sortAsc"));
		order.addItem(SortOrder.DESCENDING);
		order.setItemCaption(SortOrder.DESCENDING, $("SearchView.sortDesc"));
		order.setValue(presenter.getSortOrder());

		ValueChangeListener listener = new ValueChangeListener() {
			@Override
			public void valueChange(ValueChangeEvent event) {
				String sortCriterion = (String) criterion.getValue();
				SortOrder sortOrder = (SortOrder) order.getValue();
				presenter.sortCriterionSelected(sortCriterion, sortOrder);
			}
		};

		criterion.addValueChangeListener(listener);
		order.addValueChangeListener(listener);

		VerticalLayout inner = new VerticalLayout(criterion, order);
		inner.setWidth("100%");
		inner.addStyleName("sort-box-content");

		VerticalLayout layout = new VerticalLayout(sortBy, inner);
		layout.setWidth("95%");
		layout.addStyleName(SORT_BOX_STYLE);

		return layout;
	}

	private void addFacetComponents(ComponentContainer container, SearchResultVODataProvider dataProvider) {
		container.addComponent(buildSortComponent());

		List<FacetVO> facets = presenter.getFacets(dataProvider);
		KeySetMap<String, String> facetSelections = presenter.getFacetSelections();
		for (FacetVO facet : facets) {
			container.addComponent(buildFacetComponent(facet, facetSelections.get(facet.getId())));
		}

	}

	private Component buildFacetComponent(final FacetVO facet, Set<String> selectedFacetValues) {
		CheckBox deselect = new CheckBox();
		deselect.setValue(!selectedFacetValues.isEmpty());
		deselect.setEnabled(!selectedFacetValues.isEmpty());
		deselect.addValueChangeListener(new ValueChangeListener() {
			@Override
			public void valueChange(ValueChangeEvent event) {
				presenter.facetDeselected(facet.getId());
			}
		});

		Label title = new Label(facet.getLabel());
		title.addStyleName(ValoTheme.LABEL_BOLD);

		final Button toggle = new Button(facet.isOpen() ? "—" : "+");
		toggle.addStyleName(ValoTheme.BUTTON_TINY);
		toggle.addStyleName(ValoTheme.BUTTON_BORDERLESS);

		I18NHorizontalLayout captionBar = new I18NHorizontalLayout(deselect, title, toggle);
		captionBar.setComponentAlignment(deselect, Alignment.MIDDLE_LEFT);
		captionBar.setComponentAlignment(title, Alignment.MIDDLE_LEFT);
		captionBar.setComponentAlignment(toggle, Alignment.MIDDLE_RIGHT);
		captionBar.setExpandRatio(title, 1);
		captionBar.setWidth("100%");
		captionBar.addStyleName(FACET_TITLE_STYLE);

		VerticalLayout layout = new VerticalLayout(captionBar);
		layout.setWidth("95%");

		final Table table = new BaseTable("facet-" + facet.getId());
		table.setColumnHeaderMode(ColumnHeaderMode.HIDDEN);
		table.addContainerProperty("value", Component.class, null);
		table.setWidth("100%");

		List<FacetValueVO> values = facet.getValues();
		for (final FacetValueVO facetValue : values) {
			final CheckBox checkBox = new CheckBox();
			checkBox.addStyleName("facet-value");
			if (selectedFacetValues.contains(facetValue.getValue())) {
				checkBox.setValue(true);
			}
			checkBox.addValueChangeListener(new ValueChangeListener() {
				@Override
				public void valueChange(ValueChangeEvent event) {
					if (checkBox.getValue()) {
						presenter.facetValueSelected(facetValue.getFacetId(), facetValue.getValue());
					} else {
						presenter.facetValueDeselected(facetValue.getFacetId(), facetValue.getValue());
					}
				}
			});

			String caption = facetValue.getLabel();
			if (isRightToLeft()) {
				caption = "(" + facetValue.getCount() + ") " + caption;
			} else {
				caption += " (" + facetValue.getCount() + ")";
			}
			checkBox.setCaption(caption);

			@SuppressWarnings("unchecked")
			Property<Component> value = (Property<Component>) table.addItem(checkBox).getItemProperty("value");
			value.setValue(checkBox);
		}

		table.setPageLength(Math.min(facet.getValuesPerPage(), values.size()));
		table.setVisible(facet.isOpen());

		toggle.addClickListener(new ClickListener() {
			@Override
			public void buttonClick(ClickEvent event) {
				if (toggle.getCaption().equals("+")) {
					toggle.setCaption("—");
					table.setVisible(true);
					presenter.facetOpened(facet.getId());
				} else {
					toggle.setCaption("+");
					table.setVisible(false);
					presenter.facetClosed(facet.getId());
				}
			}
		});

		layout.addComponent(table);
		layout.setVisible(!facet.getValues().isEmpty());
		layout.addStyleName(FACET_BOX_STYLE);
		return layout;
	}

	protected Button buildSelectAllButton() {
		String selectAllCaption;
		String deselectAllCaption;
		if (isDetailedView()) {
			selectAllCaption = $("SearchView.selectCurrentPage");
			deselectAllCaption = $("SearchView.deselectCurrentPage");
		} else {
			selectAllCaption = $("SearchView.selectRange");
			deselectAllCaption = $("SearchView.selectRange");
		}
		selectDeselectAllButton = new SelectDeselectAllButton(selectAllCaption, deselectAllCaption) {
			@Override
			protected void onSelectAll(ClickEvent event) {
				((ViewableRecordVOTablePanel) resultsTable).selectCurrentPage();
			}

			@Override
			protected void onDeselectAll(ClickEvent event) {
				((ViewableRecordVOTablePanel) resultsTable).deselectCurrentPage();
			}

			@Override
			protected void buttonClickCallBack(boolean selectAllMode) {
				hashMapAllSelection.put(presenter.getPageNumber(), selectAllMode);
			}
		};
		selectDeselectAllButton.addStyleName(ValoTheme.BUTTON_LINK);
		selectDeselectAllButton.setVisible(presenter.isAllowDownloadZip());

		return selectDeselectAllButton;
	}

	protected Button buildAddToSelectionButton() {
		addToSelectionButton = new BaseButton($("SearchView.addToSelection"), SELECTION_ICON_RESOURCE) {
			@Override
			protected void buttonClick(ClickEvent event) {
				presenter.addToSelectionButtonClicked();
			}
		};
		addToSelectionButton.addStyleName(ValoTheme.BUTTON_LINK);
		// FIXME
		addToSelectionButton.setEnabled(!getSelectedRecordIds().isEmpty());
		addToSelectionButton.setVisible(!getSelectedRecordIds().isEmpty());
		//		addToSelectionButton.setVisible(presenter.isAllowDownloadZip());
		return addToSelectionButton;
	}

	protected Component buildZipButton() {
		Component zipButton = new BaseLink($("ReportViewer.download", "(zip)"),
				new DownloadStreamResource(presenter.getZippedContents(), presenter.getZippedContentsFilename()));
		zipButton.setIcon(FontAwesome.FILE_ARCHIVE_O);
		zipButton.addStyleName(ValoTheme.BUTTON_LINK);
		zipButton.setVisible(presenter.isAllowDownloadZip());
		return zipButton;
	}

	protected BaseButton buildSavedSearchButton() {
		WindowButton button = new WindowButton($("SearchView.saveSearch"), $("SearchView.saveSearch"),
				WindowConfiguration.modalDialog("500px", "300px")) {
			@Override
			protected Component buildWindowContent() {
				final TextField titleField = new BaseTextField();
				titleField.setCaption($("SearchView.savedSearch.title"));
				titleField.setRequired(true);
				titleField.setId("title");
				titleField.addStyleName("title");

				final ListAddRemoveRecordLookupField users = new ListAddRemoveRecordLookupField(User.SCHEMA_TYPE);
				users.setCaption($("SearchView.savedSearch.users"));
				users.setVisible(false);

				final ListAddRemoveRecordLookupField groups = new ListAddRemoveRecordLookupField(Group.SCHEMA_TYPE);
				groups.setCaption($("SearchView.savedSearch.groups"));
				groups.setVisible(false);

				final OptionGroup shareOptions = new OptionGroup();
				shareOptions.addItems(ShareType.NONE, ShareType.ALL, ShareType.RESTRICTED);
				shareOptions.setItemCaption(ShareType.NONE, $("SearchView.savedSearch.share.none"));
				shareOptions.setItemCaption(ShareType.ALL, $("SearchView.savedSearch.share.all"));
				shareOptions.setItemCaption(ShareType.RESTRICTED, $("SearchView.savedSearch.share.restrict"));
				shareOptions.setValue(ShareType.NONE);
				shareOptions.addValueChangeListener(new Property.ValueChangeListener() {
					@Override
					public void valueChange(Property.ValueChangeEvent event) {
						boolean visible = event.getProperty().getValue().equals(ShareType.RESTRICTED);
						users.setVisible(visible);
						groups.setVisible(visible);

						if (visible) {
							getWindow().setHeight("540px");
						} else {
							getWindow().setHeight("300px");
						}

						if (!visible) {
							groups.clear();
							users.clear();
						}
					}
				});

				BaseButton saveSearchButton = new BaseButton($("SearchView.savedSearch.save")) {
					@Override
					protected void buttonClick(ClickEvent event) {
						boolean publicAccess = !shareOptions.getValue().equals(ShareType.NONE);
						if (presenter.saveSearch(titleField.getValue(), publicAccess, users.getValue(),
								groups.getValue())) {
							getWindow().close();
						}
					}
				};
				saveSearchButton.addStyleName(ValoTheme.BUTTON_PRIMARY);

				BaseButton cancelButton = new BaseButton($("cancel")) {
					@Override
					protected void buttonClick(ClickEvent event) {
						getWindow().close();
					}
				};

				I18NHorizontalLayout horizontalLayout = new I18NHorizontalLayout();
				horizontalLayout.setSpacing(true);
				horizontalLayout.addStyleName(BUTTONS_LAYOUT);
				horizontalLayout.addComponents(saveSearchButton, cancelButton);

				VerticalLayout verticalLayout = new VerticalLayout();
				verticalLayout.addComponents(titleField, shareOptions, groups, users, horizontalLayout);
				verticalLayout.setSpacing(true);

				return verticalLayout;
			}
		};
		button.addStyleName(ValoTheme.BUTTON_LINK);
		button.addStyleName("save-search-button");
		button.setIcon(FontAwesome.FLOPPY_O);
		return button;
	}

	public RecordListMenuBar getRecordListMenuBar() {
		return null;
	}

	private Component buildCapsuleUI(Capsule capsule) {
		VerticalLayout layout = new VerticalLayout();
		layout.setSpacing(true);
		layout.addStyleName("search-result-capsule");
		Component capsuleComponent = new CapsuleComponent(capsule.getTitle(), capsule.getHTML());
		capsuleComponent.setSizeFull();
		layout.addComponent(capsuleComponent);
		return layout;
	}

	public boolean isSelectAllMode() {
		return (selectDeselectAllButton == null) ? null : selectDeselectAllButton.isSelectAllMode();
	}

	public SearchResultTable getResult() {
		return this.resultsTable;
	}

	@Override
	public void fireSomeRecordsSelected() {

	}

	@Override
	public void fireNoRecordSelected() {
	}

	@Override
	public String getSavedSearchId() {
		return presenter.getSavedSearchId();
	}

	@Override
	public boolean scrollIntoView(Integer contentIndex, String recordId) {
		boolean scrolledIntoView;
		if (resultsTable != null) {
			scrolledIntoView = ((ViewableRecordVOTablePanel) resultsTable).scrollIntoView(contentIndex, recordId);
		} else {
			scrolledIntoView = false;
		}
		return scrolledIntoView;
	}

	@Override
	public Integer getReturnIndex() {
		return presenter.getReturnIndex();
	}

	@Override
	public RecordVO getReturnRecordVO() {
		return presenter.getReturnRecordVO();
	}

	@Override
	public void browserWindowResized(BrowserWindowResizeEvent event) {
		// TODO Auto-generated method stub
	}

	public List<String> menuItemToExcludeInSelectionMenu() {
		return Collections.emptyList();
	}
}
