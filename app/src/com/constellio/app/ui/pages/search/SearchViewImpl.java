package com.constellio.app.ui.pages.search;

import com.constellio.app.ui.application.ConstellioUI;
import com.constellio.app.ui.entities.FacetVO;
import com.constellio.app.ui.entities.FacetValueVO;
import com.constellio.app.ui.entities.MetadataVO;
import com.constellio.app.ui.entities.RecordVO;
import com.constellio.app.ui.entities.SearchResultVO;
import com.constellio.app.ui.framework.buttons.BaseButton;
import com.constellio.app.ui.framework.buttons.DeleteButton;
import com.constellio.app.ui.framework.buttons.SelectDeselectAllButton;
import com.constellio.app.ui.framework.buttons.WindowButton;
import com.constellio.app.ui.framework.buttons.WindowButton.WindowConfiguration;
import com.constellio.app.ui.framework.components.RecordDisplayFactory;
import com.constellio.app.ui.framework.components.ReportViewer.DownloadStreamResource;
import com.constellio.app.ui.framework.components.SearchResultDetailedTable;
import com.constellio.app.ui.framework.components.SearchResultSimpleTable;
import com.constellio.app.ui.framework.components.SearchResultTable;
import com.constellio.app.ui.framework.components.capsule.CapsuleComponent;
import com.constellio.app.ui.framework.components.fields.BaseComboBox;
import com.constellio.app.ui.framework.components.fields.BaseTextField;
import com.constellio.app.ui.framework.components.fields.list.ListAddRemoveRecordLookupField;
import com.constellio.app.ui.framework.components.layouts.I18NHorizontalLayout;
import com.constellio.app.ui.framework.components.search.FacetsPanel;
import com.constellio.app.ui.framework.components.splitpanel.CollapsibleHorizontalSplitPanel;
import com.constellio.app.ui.framework.components.table.BaseTable;
import com.constellio.app.ui.framework.components.table.SelectionTableAdapter;
import com.constellio.app.ui.framework.components.viewers.panel.ViewableRecordTablePanel;
import com.constellio.app.ui.framework.containers.SearchResultContainer;
import com.constellio.app.ui.framework.containers.SearchResultVOLazyContainer;
import com.constellio.app.ui.framework.data.SearchResultVODataProvider;
import com.constellio.app.ui.pages.base.BaseViewImpl;
import com.constellio.app.ui.pages.search.SearchPresenter.SortOrder;
import com.constellio.data.utils.KeySetMap;
import com.constellio.data.utils.dev.Toggle;
import com.constellio.model.entities.records.wrappers.Capsule;
import com.constellio.model.entities.records.wrappers.Group;
import com.constellio.model.entities.records.wrappers.SavedSearch;
import com.constellio.model.entities.records.wrappers.User;
import com.jensjansson.pagedtable.PagedTable.PagedTableChangeEvent;
import com.vaadin.data.Container.ItemSetChangeEvent;
import com.vaadin.data.Container.ItemSetChangeListener;
import com.vaadin.data.Property;
import com.vaadin.data.Property.ValueChangeEvent;
import com.vaadin.data.Property.ValueChangeListener;
import com.vaadin.event.ItemClickEvent;
import com.vaadin.event.ItemClickEvent.ItemClickListener;
import com.vaadin.navigator.ViewChangeListener.ViewChangeEvent;
import com.vaadin.server.FontAwesome;
import com.vaadin.shared.ui.label.ContentMode;
import com.vaadin.ui.AbstractSelect.ItemCaptionMode;
import com.vaadin.ui.Alignment;
import com.vaadin.ui.Button;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.Button.ClickListener;
import com.vaadin.ui.CheckBox;
import com.vaadin.ui.ComboBox;
import com.vaadin.ui.Component;
import com.vaadin.ui.ComponentContainer;
import com.vaadin.ui.CssLayout;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Label;
import com.vaadin.ui.Link;
import com.vaadin.ui.OptionGroup;
import com.vaadin.ui.Table;
import com.vaadin.ui.Table.ColumnHeaderMode;
import com.vaadin.ui.TextField;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.themes.ValoTheme;
import org.jetbrains.annotations.Nullable;
import org.vaadin.dialogs.ConfirmDialog;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static com.constellio.app.ui.framework.components.BaseForm.BUTTONS_LAYOUT;
import static com.constellio.app.ui.i18n.i18n.$;
import static com.constellio.app.ui.i18n.i18n.isRightToLeft;

public abstract class SearchViewImpl<T extends SearchPresenter<? extends SearchView>> extends BaseViewImpl implements SearchView {

	public static final String FACET_BOX_STYLE = "facet-box";
	public static final String FACET_TITLE_STYLE = "facet-title";
	public static final String SORT_BOX_STYLE = "sort-box";
	public static final String SORT_TITLE_STYLE = "sort-title";
	public static final String SAVE_SEARCH = "save-search";

	private enum ShareType {
		NONE, ALL, RESTRICTED
	}

	protected T presenter;
	private VerticalLayout thesaurusDisambiguation;
	private VerticalLayout spellCheckerSuggestions;
	private VerticalLayout summary;
	private Component resultsAndFacetsPanel;
	private VerticalLayout resultsArea;
	private FacetsPanel facetsArea;
	private VerticalLayout capsuleArea;

	private ViewableRecordTablePanel viewableSearchResultsPanel;
	private SearchResultTable resultsTable;
	private SelectDeselectAllButton selectDeselectAllButton;
	private Button addToSelectionButton;
	private HashMap<Integer, Boolean> hashMapAllSelection = new HashMap<>();
	private List<SaveSearchListener> saveSearchListenerList = new ArrayList<>();
	private Map<String, String> extraParameters = null;

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
		Component searchUIComponent = buildSearchUI();
		if (searchUIComponent != null) {
			layout.addComponent(searchUIComponent);
		}
		layout.addComponent(buildResultsUI());
		layout.addComponent(buildThesaurusSemanticNetwork());
		layout.addStyleName("search-main-container");
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
		SearchResultVODataProvider dataProvider = refreshSearchResults(temporarySave, true);
		refreshFacets(dataProvider);
	}

	@Override
	public void refreshSearchResultsAndFacets() {
		SearchResultVODataProvider dataProvider = refreshSearchResults(true, true);
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

		SearchResultVODataProvider dataProvider = presenter.getSearchResults(includeFacets);
		spellCheckerSuggestions.removeAllComponents();
		resultsTable = buildResultTable(dataProvider);

		List<String> disambiguationSuggestions = presenter.getDisambiguationSuggestions();
		buildThesaurusDisambiguation(disambiguationSuggestions);
		buildSpellCheckerSuggestions(dataProvider, disambiguationSuggestions);

		summary.removeAllComponents();
		summary.addComponent(buildSummary(resultsTable));

		if (Toggle.SEARCH_RESULTS_VIEWER.isEnabled()) {
			Table table;
			if (resultsTable instanceof Table) {
				table = (Table) resultsTable;
			} else {
				SelectionTableAdapter selectionTableAdapter = (SelectionTableAdapter) resultsTable;
				table = selectionTableAdapter.getTable();
			}
			viewableSearchResultsPanel.setTable(table);
		}


		if (isDetailedView()) {
			resultsArea.removeAllComponents();
			resultsArea.addComponents(resultsTable, ((SearchResultDetailedTable) resultsTable).createControls());
			((SearchResultDetailedTable) resultsTable).setItemsPerPageValue(presenter.getSelectedPageLength());
		} else {
			resultsArea.removeAllComponents();
			resultsArea.addComponent(resultsTable);
		}

		refreshCapsule();

		return dataProvider;
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
		facetsArea.refresh(facets, facetSelections, sortableMetadata, sortCriterionValue, sortOrder);
		presenter.setPageNumber(1);
	}

	@Override
	public List<String> getSelectedRecordIds() {
		return resultsTable.getSelectedRecordIds();
	}

	@Override
	public List<String> getUnselectedRecordIds() {
		return resultsTable.getUnselectedRecordIds();
	}

	protected abstract Component buildSearchUI();

	protected Component buildSummary(SearchResultTable results) {
		List<Component> actions = Arrays.asList(
				(Component) buildSelectAllButton(), buildAddToSelectionButton(), buildSavedSearchButton());
		Component zipButton = new Link($("ReportViewer.download", "(zip)"),
				new DownloadStreamResource(presenter.getZippedContents(), presenter.getZippedContentsFilename()));
		zipButton.addStyleName(ValoTheme.BUTTON_LINK);
		zipButton.setVisible(presenter.isAllowDownloadZip());
		return results.createSummary(actions, zipButton);
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
		facetsArea.addStyleName("search-result-facets");
		facetsArea.setWidth("250px");
		facetsArea.setSpacing(true);

		if (Toggle.SEARCH_RESULTS_VIEWER.isEnabled()) {
			viewableSearchResultsPanel = new ViewableRecordTablePanel(resultsArea);

			CollapsibleHorizontalSplitPanel body = new CollapsibleHorizontalSplitPanel("search-result-and-facets-container");
			body.addStyleName("search-result-and-facets-container");
			body.setSecondComponentWidth(250, Unit.PIXELS);
			body.setFirstComponent(viewableSearchResultsPanel);
			body.setSecondComponent(facetsArea);

			resultsAndFacetsPanel = body;
		} else {
			I18NHorizontalLayout body = new I18NHorizontalLayout(resultsArea, facetsArea);
			body.addStyleName("search-result-and-facets-container");
			body.setWidth("100%");
			body.setExpandRatio(resultsArea, 1);
			body.setSpacing(true);

			resultsAndFacetsPanel = body;
		}

		refreshCapsule();

		VerticalLayout main = new VerticalLayout(thesaurusDisambiguation, spellCheckerSuggestions, summary, capsuleArea);

		main.addComponent(resultsAndFacetsPanel);
		main.addStyleName("suggestions-summary-results-facets");
		main.setWidth("100%");
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
			if (!capsuleArea.isVisible()) {
				capsuleArea.setVisible(true);
			}
			capsuleArea.addComponent(capsuleComponent);
		} else {
			capsuleArea.setVisible(false);
		}
	}

	protected SearchResultTable buildResultTable(SearchResultVODataProvider dataProvider) {
		if (presenter.getResultsViewMode().equals(SearchResultsViewMode.TABLE)) {
			return buildSimpleResultsTable(dataProvider);
		} else {
			return buildDetailedResultsTable(dataProvider);
		}
	}

	protected SearchResultTable buildSimpleResultsTable(SearchResultVODataProvider dataProvider) {
		//Fixme : use dataProvider instead
		final SearchResultContainer container = buildResultContainer(dataProvider);
		SearchResultSimpleTable table = new SearchResultSimpleTable(container, presenter);
		table.setWidth("100%");
		table.getTable().addItemClickListener(new ItemClickListener() {
			@SuppressWarnings("rawtypes")
			@Override
			public void itemClick(ItemClickEvent event) {
				Object itemId = event.getItemId();
				RecordVO recordVO = container.getRecordVO((int) itemId);
				((SearchPresenter) presenter).searchResultClicked(recordVO);
			}
		});
		return table;
	}

	protected SearchResultTable buildDetailedResultsTable(SearchResultVODataProvider dataProvider) {
		SearchResultContainer container = buildResultContainer(dataProvider);
		SearchResultDetailedTable srTable = new SearchResultDetailedTable(container, presenter.isAllowDownloadZip(), presenter.isShowNumberingColumn()) {
			@Override
			protected void onPreviousPageButtonClicked() {
				super.onPreviousPageButtonClicked();
				scrollBackUp();
				presenter.searchNavigationButtonClicked();
			}

			@Override
			protected void onNextPageButtonClicked() {
				super.onNextPageButtonClicked();
				scrollBackUp();
				presenter.searchNavigationButtonClicked();
			}

			@Override
			protected void onSetPageButtonClicked(int page) {
				super.onSetPageButtonClicked(page);
				scrollBackUp();
				presenter.searchNavigationButtonClicked();
			}
		};

		int totalResults = container.size();
		int totalAmountOfPages = srTable.getTotalAmountOfPages();
		int currentPage = presenter.getPageNumber();

		int selectedPageLength = presenter.getSelectedPageLength();
		presenter.setSelectedPageLength(selectedPageLength);

		srTable.setPageLength(selectedPageLength);
		srTable.setItemsPerPageValue(selectedPageLength);
		srTable.setCurrentPage(currentPage);

		srTable.addListener(new SearchResultDetailedTable.PageChangeListener() {
			public void pageChanged(PagedTableChangeEvent event) {
				presenter.setPageNumber(event.getCurrentPage());

				presenter.saveTemporarySearch(false);
				if (selectDeselectAllButton != null) {
					hashMapAllSelection.put(presenter.getLastPageNumber(), selectDeselectAllButton.isSelectAllMode());
					Boolean objIsSelectAllMode = hashMapAllSelection.get(new Integer(presenter.getPageNumber()));
					boolean isSelectAllMode = true;
					if (objIsSelectAllMode != null) {
						isSelectAllMode = objIsSelectAllMode;
					}
					selectDeselectAllButton.setSelectAllMode(isSelectAllMode);
				}
			}
		});
		srTable.getItemsPerPageField().addValueChangeListener(new ValueChangeListener() {
			@Override
			public void valueChange(Property.ValueChangeEvent event) {
				presenter.setSelectedPageLength((int) event.getProperty().getValue());
				hashMapAllSelection = new HashMap<>();

				presenter.searchNavigationButtonClicked();
			}
		});

		srTable.setWidth("100%");

		return srTable;
	}

	private void scrollBackUp() {
		getUI().scrollIntoView(summary);
	}

	protected SearchResultContainer buildResultContainer(SearchResultVODataProvider dataProvider) {
		RecordDisplayFactory displayFactory = new RecordDisplayFactory(getSessionContext().getCurrentUser(), extraParameters);
		SearchResultVOLazyContainer results = new SearchResultVOLazyContainer(dataProvider);
		SearchResultContainer container = new SearchResultContainer(results, displayFactory,
				presenter.getSearchQuery().getFreeTextQuery()) {
			@Override
			protected ClickListener getClickListener(final SearchResultVO searchResultVO) {
				return new ClickListener() {
					@Override
					public void buttonClick(ClickEvent event) {
						presenter.searchResultClicked(searchResultVO.getRecordVO());
					}
				};
			}

			@Override
			protected ClickListener getElevationClickListener(final SearchResultVO searchResultVO) {
				return new ClickListener() {
					@Override
					public void buttonClick(ClickEvent event) {
						((SearchPresenter<?>) presenter).searchResultElevationClicked(searchResultVO.getRecordVO());
					}
				};
			}

			@Override
			protected ClickListener getExclusionClickListener(final SearchResultVO searchResultVO) {
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
				if (isDetailedView()) {
					((SearchResultDetailedTable) resultsTable).selectCurrentPage();
				} else {
					((SearchResultSimpleTable) resultsTable).askSelectionRange();
				}
			}

			@Override
			protected void onDeselectAll(ClickEvent event) {
				if (isDetailedView()) {
					((SearchResultDetailedTable) resultsTable).deselectCurrentPage();
				} else {
					((SearchResultSimpleTable) resultsTable).askSelectionRange();
				}
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
		addToSelectionButton = new BaseButton($("SearchView.addToSelection")) {
			@Override
			protected void buttonClick(ClickEvent event) {
				presenter.addToSelectionButtonClicked();
			}
		};
		addToSelectionButton.addStyleName(ValoTheme.BUTTON_LINK);
		addToSelectionButton.setVisible(presenter.isAllowDownloadZip());
		return addToSelectionButton;
	}

	protected Button buildSavedSearchButton() {
		WindowButton button = new WindowButton($("SearchView.saveSearch"), $("SearchView.saveSearch"),
				WindowConfiguration.modalDialog("50%", "70%")) {
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
		return button;
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

}
