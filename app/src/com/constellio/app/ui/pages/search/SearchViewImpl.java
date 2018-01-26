package com.constellio.app.ui.pages.search;

import static com.constellio.app.ui.i18n.i18n.$;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Set;

import org.vaadin.dialogs.ConfirmDialog;

import com.constellio.app.ui.application.ConstellioUI;
import com.constellio.app.ui.entities.FacetVO;
import com.constellio.app.ui.entities.FacetValueVO;
import com.constellio.app.ui.entities.MetadataVO;
import com.constellio.app.ui.entities.SearchResultVO;
import com.constellio.app.ui.framework.buttons.BaseButton;
import com.constellio.app.ui.framework.buttons.DeleteButton;
import com.constellio.app.ui.framework.buttons.SelectDeselectAllButton;
import com.constellio.app.ui.framework.buttons.WindowButton;
import com.constellio.app.ui.framework.components.RecordDisplayFactory;
import com.constellio.app.ui.framework.components.ReportViewer.DownloadStreamResource;
import com.constellio.app.ui.framework.components.SearchResultDetailedTable;
import com.constellio.app.ui.framework.components.SearchResultSimpleTable;
import com.constellio.app.ui.framework.components.SearchResultTable;
import com.constellio.app.ui.framework.components.fields.BaseTextField;
import com.constellio.app.ui.framework.containers.SearchResultContainer;
import com.constellio.app.ui.framework.containers.SearchResultVOLazyContainer;
import com.constellio.app.ui.framework.data.SearchResultVODataProvider;
import com.constellio.app.ui.pages.base.BaseViewImpl;
import com.constellio.app.ui.pages.search.SearchPresenter.SortOrder;
import com.constellio.data.utils.KeySetMap;
import com.constellio.data.utils.dev.Toggle;
import com.constellio.model.entities.records.wrappers.Capsule;
import com.jensjansson.pagedtable.PagedTable.PagedTableChangeEvent;
import com.vaadin.data.Container.ItemSetChangeEvent;
import com.vaadin.data.Container.ItemSetChangeListener;
import com.vaadin.data.Property;
import com.vaadin.data.Property.ValueChangeEvent;
import com.vaadin.data.Property.ValueChangeListener;
import com.vaadin.event.ItemClickEvent;
import com.vaadin.navigator.ViewChangeListener.ViewChangeEvent;
import com.vaadin.server.ThemeResource;
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
import com.vaadin.ui.Panel;
import com.vaadin.ui.Table;
import com.vaadin.ui.Table.ColumnHeaderMode;
import com.vaadin.ui.TextField;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.themes.ValoTheme;

public abstract class SearchViewImpl<T extends SearchPresenter<? extends SearchView>> extends BaseViewImpl implements SearchView {
	public static final String SUGGESTION_STYLE = "spell-checker-suggestion";
	public static final String FACET_BOX_STYLE = "facet-box";
	public static final String FACET_TITLE_STYLE = "facet-title";
	public static final String SORT_BOX_STYLE = "sort-box";
	public static final String SORT_TITLE_STYLE = "sort-title";
	public static final String SAVE_SEARCH = "save-search";

	protected T presenter;
	private CssLayout suggestions;
	private VerticalLayout summary;
	private VerticalLayout resultsArea;
	private VerticalLayout facetsArea;
	private SearchResultTable results;
	private SelectDeselectAllButton selectDeselectAllButton;
	private Button addToSelectionButton;
	private HashMap<Integer, Boolean> hashMapAllSelection = new HashMap<>();

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
		layout.addComponent(buildSearchUI());
		layout.addComponent(buildResultsUI());
		layout.addStyleName("search-main-container");
		layout.setSpacing(true);
		if (presenter.mustDisplayResults()) {
			refreshSearchResultsAndFacets(false);
		}
		return layout;
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
			presenter.saveTemporarySearch(false);
		}

		SearchResultVODataProvider dataProvider = presenter.getSearchResults(includeFacets);
		suggestions.removeAllComponents();
		results = buildResultTable(dataProvider);
		buildSuggestions(dataProvider);

		summary.removeAllComponents();
		summary.addComponent(buildSummary(results));

		if (isDetailedView()) {
			resultsArea.removeAllComponents();
			resultsArea.addComponents(results, ((SearchResultDetailedTable) results).createControls());
			((SearchResultDetailedTable) results).setItemsPerPageValue(presenter.getSelectedPageLength());
		} else {
			resultsArea.removeAllComponents();
			resultsArea.addComponent(results);
		}

		return dataProvider;
	}

	private boolean isDetailedView() {
		return !SearchResultsViewMode.TABLE.equals(presenter.getResultsViewMode());
	}

	@Override
	public void refreshFacets(SearchResultVODataProvider dataProvider) {
		facetsArea.removeAllComponents();
		addFacetComponents(facetsArea, dataProvider);
		presenter.setPageNumber(1);
	}

	@Override
	public List<String> getSelectedRecordIds() {
		return results.getSelectedRecordIds();
	}

	@Override
	public List<String> getUnselectedRecordIds() {
		return results.getUnselectedRecordIds();
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
		suggestions = new CssLayout();
		suggestions.addStyleName("spell-checker");

		summary = new VerticalLayout();
		summary.addStyleName("search-result-summary");

		resultsArea = new VerticalLayout();
		resultsArea.addStyleName("search-result-area");
		//		resultsArea.setWidth("100%");
		resultsArea.setSpacing(true);

		facetsArea = new VerticalLayout();
		facetsArea.addStyleName("search-result-facets");
		facetsArea.setWidth("300px");
		facetsArea.setSpacing(true);

		HorizontalLayout body = new HorizontalLayout(resultsArea, facetsArea);
		body.addStyleName("search-result-and-facets-container");
		body.setWidth("100%");
		body.setExpandRatio(resultsArea, 1);
		body.setSpacing(true);
		Component capsuleComponent = null;
		if (Toggle.ADVANCED_SEARCH_CONFIGS.isEnabled()) {
			List<Capsule> capsules = presenter.getCapsuleForCurrentSearch();
			if (!capsules.isEmpty()) {
				capsuleComponent = buildCapsuleIU(capsules);
			}
		}

		VerticalLayout main = new VerticalLayout(suggestions, summary);
		if (capsuleComponent != null) {
			main.addComponent(capsuleComponent);
		}
		main.addComponent(body);
		main.addStyleName("suggestions-summary-results-facets");
		main.setWidth("100%");
		main.setSpacing(true);

		return main;
	}

	protected SearchResultTable buildResultTable(SearchResultVODataProvider dataProvider) {
		return buildDetailedResultsTable(dataProvider);
	}

	protected SearchResultTable buildDetailedResultsTable(SearchResultVODataProvider dataProvider) {
		SearchResultContainer container = buildResultContainer(dataProvider);
		SearchResultDetailedTable srTable = new SearchResultDetailedTable(container, presenter.isAllowDownloadZip()) {
			@Override
			protected void onPreviousPageButtonClicked() {
				super.onPreviousPageButtonClicked();
				presenter.searchNavigationButtonClicked();
			}

			@Override
			protected void onNextPageButtonClicked() {
				super.onNextPageButtonClicked();
				presenter.searchNavigationButtonClicked();
			}

			@Override
			protected void onSetPageButtonClicked(int page) {
				super.onSetPageButtonClicked(page);
				presenter.searchNavigationButtonClicked();
			}
		};

		int totalResults = container.size();
		int totalAmountOfPages = srTable.getTotalAmountOfPages();
		int currentPage = presenter.getPageNumber();

		int selectedPageLength = presenter.getSelectedPageLength();
		if (selectedPageLength == 0) {
			selectedPageLength = Math.min(totalResults, presenter.getDefaultPageLength());
		}
		presenter.setSelectedPageLength(selectedPageLength);

		srTable.setPageLength(selectedPageLength);
		srTable.setItemsPerPageValue(selectedPageLength);
		srTable.setCurrentPage(currentPage);

		srTable.addItemClickListener(new ItemClickEvent.ItemClickListener() {
			@Override
			public void itemClick(ItemClickEvent event) {
				System.out.println("Jonathan Plamndon");
			}
		});

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
			}
		});

		srTable.setWidth("100%");
		return srTable;
	}

	protected SearchResultContainer buildResultContainer(SearchResultVODataProvider dataProvider) {
		RecordDisplayFactory displayFactory = new RecordDisplayFactory(getSessionContext().getCurrentUser());
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


	private void buildSuggestions(SearchResultVODataProvider dataProvider) {
		if (!presenter.mustDisplaySuggestions(dataProvider)) {
			suggestions.setVisible(false);
			return;
		}

        Label caption = new Label($("SearchView.spellChecker"));
        caption.addStyleName(ValoTheme.LABEL_BOLD);
        suggestions.addComponent(caption);

		List<String> foundSuggestions = presenter.getAllNonExcluded(getCollection(), presenter.getSuggestions());
		for (final String suggestion : foundSuggestions) {

				Button activateSuggestion = new Button(suggestion);
				activateSuggestion.addStyleName(ValoTheme.BUTTON_LINK);
				activateSuggestion.addStyleName(SUGGESTION_STYLE);
				activateSuggestion.addClickListener(new ClickListener() {
					@Override
					public void buttonClick(ClickEvent event) {
						presenter.suggestionSelected(suggestion);
					}
				});
			HorizontalLayout horizontalLayout = new HorizontalLayout();

			final DeleteButton excludeButton = new DeleteButton() {
				@Override
				protected void confirmButtonClick(ConfirmDialog dialog) {
					presenter.deleteSuggestionButtonClicked(suggestion, getCollection());
					updateUI();
				}
			};
			excludeButton.setData(suggestion);

			excludeButton.addStyleName(ValoTheme.BUTTON_BORDERLESS);

			excludeButton.setIcon(new ThemeResource("images/icons/actions/delete.png"));
			horizontalLayout.addComponent(activateSuggestion);
			horizontalLayout.addComponent(excludeButton);
			suggestions.addComponent(horizontalLayout);
		}
		if(foundSuggestions.size() > 0) {
			suggestions.setVisible(true);
		}
		else {
			suggestions.setVisible(false);
		}
	}

	private Component buildSortComponent() {
		Label sortBy = new Label($("SearchView.sortBy"));
		sortBy.addStyleName(ValoTheme.LABEL_BOLD);
		sortBy.addStyleName(SORT_TITLE_STYLE);

		final ComboBox criterion = new ComboBox();
		criterion.setItemCaptionMode(ItemCaptionMode.EXPLICIT);
		criterion.setWidth("100%");

		List<MetadataVO> sortableMetadata = presenter.getMetadataAllowedInSort();
		for (MetadataVO metadata : sortableMetadata) {
			criterion.addItem(metadata.getCode());
			criterion.setItemCaption(metadata.getCode(), metadata.getLabel());
		}
		//		criterion.setPageLength(criterion.size());
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

	@SuppressWarnings("unchecked")
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

		HorizontalLayout captionBar = new HorizontalLayout(deselect, title, toggle);
		captionBar.setComponentAlignment(deselect, Alignment.MIDDLE_LEFT);
		captionBar.setComponentAlignment(title, Alignment.MIDDLE_LEFT);
		captionBar.setComponentAlignment(toggle, Alignment.MIDDLE_RIGHT);
		captionBar.setExpandRatio(title, 1);
		captionBar.setWidth("100%");
		captionBar.addStyleName(FACET_TITLE_STYLE);

		VerticalLayout layout = new VerticalLayout(captionBar);
		layout.setWidth("95%");

		final Table table = new Table();
		table.setColumnHeaderMode(ColumnHeaderMode.HIDDEN);
		table.addContainerProperty("value", Component.class, null);
		table.setWidth("100%");

		List<FacetValueVO> values = facet.getValues();
		for (final FacetValueVO facetValue : values) {
			final CheckBox checkBox = new CheckBox();
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
			caption += " (" + facetValue.getCount() + ")";
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
					((SearchResultDetailedTable) results).selectCurrentPage();
				} else {
					((SearchResultSimpleTable) results).askSelectionRange();
				}
			}

			@Override
			protected void onDeselectAll(ClickEvent event) {
				if (isDetailedView()) {
					((SearchResultDetailedTable) results).deselectCurrentPage();
				} else {
					((SearchResultSimpleTable) results).askSelectionRange();
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
		WindowButton button = new WindowButton($("SearchView.saveSearch"),
				$("SearchView.saveSearch")) {
			@Override
			protected Component buildWindowContent() {

				final TextField titleField = new BaseTextField();
				titleField.setCaption($("SearchView.savedSearch.title"));
				titleField.setRequired(true);
				titleField.setId("title");
				titleField.addStyleName("title");

				final CheckBox publicField = new CheckBox();
				publicField.setCaption($("SearchView.savedSearch.public"));
				publicField.setRequired(true);
				publicField.setId("public");
				publicField.addStyleName("public");

				BaseButton saveSearchButton = new BaseButton($("SearchView.savedSearch.save")) {
					@Override
					protected void buttonClick(ClickEvent event) {
						if (presenter.saveSearch(titleField.getValue(), (publicField.getValue()))) {
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

				HorizontalLayout horizontalLayout = new HorizontalLayout();
				horizontalLayout.setSpacing(true);
				horizontalLayout.addComponents(saveSearchButton, cancelButton);

				VerticalLayout verticalLayout = new VerticalLayout();
				verticalLayout.addComponents(titleField, publicField, horizontalLayout);
				verticalLayout.setSpacing(true);

				return verticalLayout;
			}
		};
		button.addStyleName(ValoTheme.BUTTON_LINK);
		button.addStyleName(SAVE_SEARCH);
		return button;
	}

	private Component buildCapsuleIU(List<Capsule> capsules) {
		VerticalLayout layout = new VerticalLayout();
		layout.setSpacing(true);
		for (Capsule capsule : capsules) {
			Panel panel = new Panel();
			panel.setSizeFull();
			Label label = new Label(capsule.getHTML(), ContentMode.HTML);
			panel.setContent(label);
			panel.setWidth("100%");
			panel.setCaption(capsule.getTitle());
			layout.addComponent(panel);
		}
		return layout;
	}

	public boolean isSelectAllMode() {
		return (selectDeselectAllButton == null) ? null : selectDeselectAllButton.isSelectAllMode();
	}

	public SearchResultTable getResult() {
		return this.results;
	}
}
