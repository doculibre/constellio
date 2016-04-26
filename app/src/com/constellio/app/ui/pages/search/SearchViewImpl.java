package com.constellio.app.ui.pages.search;

import static com.constellio.app.ui.i18n.i18n.$;

import java.util.Arrays;
import java.util.List;
import java.util.Set;

import com.constellio.app.ui.application.ConstellioUI;
import com.constellio.app.ui.entities.FacetVO;
import com.constellio.app.ui.entities.FacetValueVO;
import com.constellio.app.ui.entities.MetadataVO;
import com.constellio.app.ui.framework.buttons.BaseButton;
import com.constellio.app.ui.framework.buttons.LabelsButton.RecordSelector;
import com.constellio.app.ui.framework.buttons.LinkButton;
import com.constellio.app.ui.framework.buttons.WindowButton;
import com.constellio.app.ui.framework.components.RecordDisplayFactory;
import com.constellio.app.ui.framework.components.ReportSelector;
import com.constellio.app.ui.framework.components.ReportViewer.DownloadStreamResource;
import com.constellio.app.ui.framework.components.SearchResultTable;
import com.constellio.app.ui.framework.components.fields.BaseTextField;
import com.constellio.app.ui.framework.containers.SearchResultContainer;
import com.constellio.app.ui.framework.containers.SearchResultVOLazyContainer;
import com.constellio.app.ui.pages.base.BaseViewImpl;
import com.constellio.app.ui.pages.search.SearchPresenter.SortOrder;
import com.constellio.data.utils.KeySetMap;
import com.jensjansson.pagedtable.PagedTable.PagedTableChangeEvent;
import com.vaadin.data.Property;
import com.vaadin.data.Property.ValueChangeEvent;
import com.vaadin.data.Property.ValueChangeListener;
import com.vaadin.navigator.ViewChangeListener.ViewChangeEvent;
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

public abstract class SearchViewImpl<T extends SearchPresenter> extends BaseViewImpl implements SearchView, RecordSelector {
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
		VerticalLayout layout = new VerticalLayout(buildSearchUI(), buildResultsUI());
		layout.setSpacing(true);
		if (presenter.mustDisplayResults()) {
			refreshSearchResultsAndFacets(true);
		}
		return layout;
	}

	@Override
	public void setSearchExpression(String expression) {
		ConstellioUI.getCurrent().getHeader().setSearchExpression(expression);
	}

	public void refreshSearchResultsAndFacets(boolean init) {
		refreshSearchResults(init);
		refreshFacets();
	}

	@Override
	public void refreshSearchResultsAndFacets() {
		refreshSearchResults();
		refreshFacets();
	}

	@Override
	public void refreshSearchResults() {
		presenter.saveTemporarySearch();

		suggestions.removeAllComponents();
		buildSuggestions();

		results = buildResultTable();

		summary.removeAllComponents();
		summary.addComponent(buildSummary(results));

		resultsArea.removeAllComponents();
		resultsArea.addComponents(results, results.createControls());
	}

	public void refreshSearchResults(boolean init) {
		if (!init) {
			presenter.saveTemporarySearch();
		}

		suggestions.removeAllComponents();
		buildSuggestions();

		results = buildResultTable();

		summary.removeAllComponents();
		summary.addComponent(buildSummary(results));

		resultsArea.removeAllComponents();
		resultsArea.addComponents(results, results.createControls());
	}

	@Override
	public void refreshFacets() {
		facetsArea.removeAllComponents();
		addFacetComponents(facetsArea);
		presenter.setPageNumber(1);
	}

	@Override
	public List<String> getSelectedRecordIds() {
		return results.getSelectedRecordIds();
	}

	protected abstract Component buildSearchUI();

	protected Component buildSummary(SearchResultTable results) {
		List<Component> actions = Arrays.asList(
				buildSelectAllButton(), buildSavedSearchButton(), (Component) new ReportSelector(presenter));
		Component zipButton = new Link($("ReportViewer.download", "(zip)"),
				new DownloadStreamResource(presenter.getZippedContents(), presenter.getZippedContentsFilename()));
		zipButton.addStyleName(ValoTheme.BUTTON_LINK);
		return results.createSummary(actions, zipButton);
	}

	private Component buildResultsUI() {
		suggestions = new CssLayout();
		suggestions.addStyleName("spell-checker");

		summary = new VerticalLayout();

		resultsArea = new VerticalLayout();
		resultsArea.setSpacing(true);

		facetsArea = new VerticalLayout();
		facetsArea.addStyleName("search-result-facets");
		facetsArea.setWidth("300px");
		facetsArea.setSpacing(true);

		HorizontalLayout body = new HorizontalLayout(resultsArea, facetsArea);
		body.setWidth("100%");
		body.setExpandRatio(resultsArea, 1);
		body.setSpacing(true);

		VerticalLayout main = new VerticalLayout(suggestions, summary, body);
		main.addStyleName("suggestions-summary-results-facets");
		main.setWidth("100%");
		main.setSpacing(true);

		return main;
	}

	private SearchResultTable buildResultTable() {
		SearchResultTable table = new SearchResultTable(buildResultContainer());
		table.setWidth("100%");
		table.setCurrentPage(presenter.getPageNumber());
		table.addListener(new SearchResultTable.PageChangeListener() {
			public void pageChanged(PagedTableChangeEvent event) {
				presenter.setPageNumber(event.getCurrentPage());
				presenter.saveTemporarySearch();
			}
		});

		return table;
	}

	private SearchResultContainer buildResultContainer() {
		RecordDisplayFactory displayFactory = new RecordDisplayFactory(getSessionContext().getCurrentUser());
		SearchResultVOLazyContainer results = new SearchResultVOLazyContainer(presenter.getSearchResults());
		return new SearchResultContainer(results, displayFactory);
	}

	@SuppressWarnings("unchecked")
	private void buildSuggestions() {
		if (!presenter.mustDisplaySuggestions()) {
			suggestions.setVisible(false);
			return;
		}

		Label caption = new Label($("SearchView.spellChecker"));
		caption.addStyleName(ValoTheme.LABEL_BOLD);
		suggestions.addComponent(caption);

		List<String> foundSuggestions = presenter.getSuggestions();
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
			suggestions.addComponent(activateSuggestion);
		}
		suggestions.setVisible(true);
	}

	private Component buildSortComponent() {
		Label sortBy = new Label($("SearchView.sortBy"));
		sortBy.addStyleName(ValoTheme.LABEL_BOLD);
		sortBy.addStyleName(SORT_TITLE_STYLE);

		final ComboBox criterion = new ComboBox();
		criterion.setItemCaptionMode(ItemCaptionMode.EXPLICIT);
		criterion.setWidth("100%");

		@SuppressWarnings("unchecked") List<MetadataVO> sortableMetadata = presenter.getMetadataAllowedInSort();
		for (MetadataVO metadata : sortableMetadata) {
			criterion.addItem(metadata.getCode());
			criterion.setItemCaption(metadata.getCode(), metadata.getLabel());
		}
		criterion.setPageLength(criterion.size());
		criterion.setValue(presenter.getSortCriterion());

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
	private void addFacetComponents(ComponentContainer container) {
		container.addComponent(buildSortComponent());

		List<FacetVO> facets = presenter.getFacets();
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
		return new LinkButton($("SearchView.selectCurrentPage")) {
			@Override
			protected void buttonClick(ClickEvent event) {
				results.selectCurrentPage();
			}
		};
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
}
