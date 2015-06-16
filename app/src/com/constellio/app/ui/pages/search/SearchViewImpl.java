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
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import com.constellio.app.ui.application.ConstellioUI;
import com.constellio.app.ui.entities.MetadataVO;
import com.constellio.app.ui.framework.buttons.LabelsButton.RecordSelector;
import com.constellio.app.ui.framework.components.MetadataDisplayFactory;
import com.constellio.app.ui.framework.components.RecordDisplayFactory;
import com.constellio.app.ui.framework.components.ReportSelector;
import com.constellio.app.ui.framework.components.SearchResultTable;
import com.constellio.app.ui.framework.containers.SearchResultContainer;
import com.constellio.app.ui.framework.containers.SearchResultVOLazyContainer;
import com.constellio.app.ui.pages.base.BaseViewImpl;
import com.constellio.app.ui.pages.search.SearchPresenter.SortOrder;
import com.constellio.data.dao.dto.records.FacetValue;
import com.constellio.model.services.schemas.MetadataSchemasManager;
import com.google.common.base.Strings;
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
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.themes.ValoTheme;

public abstract class SearchViewImpl<T extends SearchPresenter> extends BaseViewImpl implements SearchView, RecordSelector {
	public static final String EMPTY_FACET_VALUE = "__NULL__";
	public static final String FACET_BOX_STYLE = "facet-box";
	public static final String SUGGESTION_STYLE = "spell-checker-suggestion";

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
		//		Factory<List<LabelTemplate>> labelTemplatesFactory = new Factory<List<LabelTemplate>>() {
		//			@Override
		//			public List<LabelTemplate> get() {
		//				return presenter.getTemplates();
		//			}
		//		};
		//		LabelsButton labelsButton = new LabelsButton($("SearchView.labels"), $("SearchView.printLabels"), this,
		//				labelTemplatesFactory);
		//		labelsButton.addStyleName(ValoTheme.BUTTON_LINK);
		ReportSelector reportSelector = new ReportSelector(presenter);
		return results.createSummary(reportSelector);
		//		return results.createSummary(labelsButton, reportSelector);
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

	@SuppressWarnings("unchecked")
	private Component buildSortComponent() {
		Label sortBy = new Label($("SearchView.sortBy"));
		sortBy.addStyleName(ValoTheme.LABEL_BOLD);

		final ComboBox criterion = new ComboBox();
		criterion.setItemCaptionMode(ItemCaptionMode.EXPLICIT);

		List<MetadataVO> sortableMetadata = presenter.getMetadataAllowedInSort();
		for (MetadataVO metadata : sortableMetadata) {
			criterion.addItem(metadata.getCode());
			criterion.setItemCaption(metadata.getCode(), metadata.getLabel());
		}
		criterion.setPageLength(criterion.size());

		final OptionGroup order = new OptionGroup();
		order.addItem(SortOrder.ASCENDING);
		order.setItemCaption(SortOrder.ASCENDING, $("SearchView.sortAsc"));
		order.addItem(SortOrder.DESCENDING);
		order.setItemCaption(SortOrder.DESCENDING, $("SearchView.sortDesc"));
		order.setValue(SortOrder.ASCENDING);

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

		Map<MetadataVO, List<FacetValue>> facets = presenter.getFacets();
		Map<String, Set<String>> facetSelections = presenter.getFacetSelections();
		for (Entry<MetadataVO, List<FacetValue>> facet : facets.entrySet()) {
			Set<String> selectedFacetValues = facetSelections.get(facet.getKey().getCode());
			container.addComponent(buildFacetComponent(facet.getKey(), facet.getValue(), selectedFacetValues));
		}
	}

	private Component buildFacetComponent(MetadataVO facet, List<FacetValue> values, Set<String> selectedFacetValues) {
		Label title = new Label(facet.getLabel());
		title.addStyleName(ValoTheme.LABEL_BOLD);
		VerticalLayout layout = new VerticalLayout(title);
		layout.setSizeUndefined();

		MetadataDisplayFactory factory = new MetadataDisplayFactory();
		for (FacetValue facetValue : values) {
			CheckBox checkBox = new CheckBox();
			if (selectedFacetValues.contains(facetValue.getValue())) {
				checkBox.setValue(true);
			}
			checkBox.addValueChangeListener(new Selector(facet.getCode(), facetValue.getValue()));

			String caption;
			if (facet.getCode().endsWith("schema")) {
				MetadataSchemasManager manager = getConstellioFactories().getModelLayerFactory().getMetadataSchemasManager();
				caption = manager.getSchemaTypes(getCollection()).getSchema(facetValue.getValue()).getLabel();
			} else if (Strings.isNullOrEmpty(facetValue.getValue()) || EMPTY_FACET_VALUE.equals(facetValue.getValue())) {
				caption = $("SearchView.noValue");
			} else {
				Component displayComponent = factory.buildSingleValue(facet, facetValue.getValue());
				if (displayComponent instanceof Label) {
					caption = ((Label) displayComponent).getValue();
				} else {
					caption = displayComponent.getCaption();
				}
			}
			caption += " (" + facetValue.getQuantity() + ")";
			checkBox.setCaption(caption);

			layout.addComponent(checkBox);
		}

		layout.setVisible(layout.getComponentCount() > 1);
		layout.addStyleName(FACET_BOX_STYLE);
		return layout;
	}

	private class Selector implements ValueChangeListener {
		private final String metadataCode;
		private final String facetValue;

		public Selector(String metadataCode, String facetValue) {
			this.metadataCode = metadataCode;
			this.facetValue = facetValue;
		}

		@Override
		public void valueChange(ValueChangeEvent event) {
			if ((boolean) event.getProperty().getValue()) {
				presenter.facetValueSelected(metadataCode, facetValue);
			} else {
				presenter.facetValueDeselected(metadataCode, facetValue);
			}
		}
	}
}
