/*Constellio Enterprise Information Management

Copyright (c) 2015 "Constellio inc."

This program is free software: you can redistribute it and/or modify
it under the terms of the GNU Affero General Public License as
published by the Free Software Foundation, either version 3 of the
License, or (at your option) any later version.

This program is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
GNU Affero General Public License for more details.

You should have received a copy of the GNU Affero General Public License
along with this program. If not, see <http://www.gnu.org/licenses/>.
*/
package com.constellio.app.ui.pages.search;

import static com.constellio.app.ui.i18n.i18n.$;

import java.util.List;
import java.util.Set;

import com.constellio.app.ui.application.ConstellioUI;
import com.constellio.app.ui.entities.FacetVO;
import com.constellio.app.ui.entities.FacetValueVO;
import com.constellio.app.ui.entities.MetadataVO;
import com.constellio.app.ui.framework.buttons.BaseButton;
import com.constellio.app.ui.framework.buttons.LabelsButton.RecordSelector;
import com.constellio.app.ui.framework.buttons.WindowButton;
import com.constellio.app.ui.framework.components.RecordDisplayFactory;
import com.constellio.app.ui.framework.components.ReportSelector;
import com.constellio.app.ui.framework.components.SearchResultTable;
import com.constellio.app.ui.framework.components.fields.BaseTextField;
import com.constellio.app.ui.framework.containers.SearchResultContainer;
import com.constellio.app.ui.framework.containers.SearchResultVOLazyContainer;
import com.constellio.app.ui.pages.base.BaseViewImpl;
import com.constellio.app.ui.pages.search.SearchPresenter.SortOrder;
import com.constellio.data.utils.KeySetMap;
import com.vaadin.data.Property.ValueChangeEvent;
import com.vaadin.data.Property.ValueChangeListener;
import com.vaadin.navigator.ViewChangeListener.ViewChangeEvent;
import com.vaadin.ui.AbstractSelect.ItemCaptionMode;
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
import com.vaadin.ui.OptionGroup;
import com.vaadin.ui.TextField;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.themes.ValoTheme;

public abstract class SearchViewImpl<T extends SearchPresenter> extends BaseViewImpl implements SearchView, RecordSelector {
	public static final String FACET_BOX_STYLE = "facet-box";
	public static final String SUGGESTION_STYLE = "spell-checker-suggestion";
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
			refreshSearchResultsAndFacets();
		}
		return layout;
	}

	@Override
	public void setSearchExpression(String expression) {
		ConstellioUI.getCurrent().getHeader().setSearchExpression(expression);
	}

	@Override
	public void refreshSearchResultsAndFacets() {
		refreshSearchResults();
		refreshFacets();
	}

	@Override
	public void refreshSearchResults() {
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
	}

	@Override
	public List<String> getSelectedRecordIds() {
		return results.getSelectedRecordIds();
	}

	protected abstract Component buildSearchUI();

	protected Component buildSummary(SearchResultTable results) {
		return results.createSummary(buildSavedSearchButton(), new ReportSelector(presenter));
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

		final ComboBox criterion = new ComboBox();
		criterion.setItemCaptionMode(ItemCaptionMode.EXPLICIT);

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

		VerticalLayout layout = new VerticalLayout(sortBy, criterion, order);
		layout.setSizeUndefined();
		layout.setSpacing(true);

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

	private Component buildFacetComponent(FacetVO facet, Set<String> selectedFacetValues) {
		Label title = new Label(facet.getLabel());
		title.addStyleName(ValoTheme.LABEL_BOLD);
		VerticalLayout layout = new VerticalLayout(title);
		layout.setSizeUndefined();

		for (FacetValueVO facetValue : facet.getValues()) {
			CheckBox checkBox = new CheckBox();
			if (selectedFacetValues.contains(facetValue.getValue())) {
				checkBox.setValue(true);
			}
			checkBox.addValueChangeListener(new Selector(facetValue));

			String caption = facetValue.getLabel();
			caption += " (" + facetValue.getCount() + ")";
			checkBox.setCaption(caption);

			layout.addComponent(checkBox);
		}

		layout.setVisible(layout.getComponentCount() > 1);
		layout.addStyleName(FACET_BOX_STYLE);
		return layout;
	}

	private class Selector implements ValueChangeListener {
		private final FacetValueVO facetValue;

		public Selector(FacetValueVO facetValue) {
			this.facetValue = facetValue;
		}

		@Override
		public void valueChange(ValueChangeEvent event) {
			if ((boolean) event.getProperty().getValue()) {
				presenter.facetValueSelected(facetValue.getFacetId(), facetValue.getValue());
			} else {
				presenter.facetValueDeselected(facetValue.getFacetId(), facetValue.getValue());
			}
		}
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
