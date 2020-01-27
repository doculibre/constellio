package com.constellio.app.ui.framework.components.search;

import com.constellio.app.ui.entities.FacetVO;
import com.constellio.app.ui.entities.FacetValueVO;
import com.constellio.app.ui.entities.MetadataVO;
import com.constellio.app.ui.framework.components.fields.BaseComboBox;
import com.constellio.app.ui.framework.components.layouts.I18NHorizontalLayout;
import com.constellio.app.ui.framework.components.table.BaseTable;
import com.constellio.app.ui.pages.search.SearchPresenter.SortOrder;
import com.constellio.data.utils.KeySetMap;
import com.constellio.data.utils.dev.Toggle;
import com.vaadin.data.Property;
import com.vaadin.data.Property.ValueChangeEvent;
import com.vaadin.data.Property.ValueChangeListener;
import com.vaadin.ui.AbstractSelect.ItemCaptionMode;
import com.vaadin.ui.Alignment;
import com.vaadin.ui.Button;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.Button.ClickListener;
import com.vaadin.ui.CheckBox;
import com.vaadin.ui.ComboBox;
import com.vaadin.ui.Component;
import com.vaadin.ui.Label;
import com.vaadin.ui.OptionGroup;
import com.vaadin.ui.Table;
import com.vaadin.ui.Table.ColumnHeaderMode;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.themes.ValoTheme;

import java.util.HashMap;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import static com.constellio.app.ui.i18n.i18n.$;
import static com.constellio.app.ui.i18n.i18n.isRightToLeft;

public abstract class FacetsPanel extends VerticalLayout {

	private final static KeySetMap<String, String> facetValuesSelectedMap = new KeySetMap<>();

	public FacetsPanel() {
		addStyleName("search-result-facets");
		setWidth("250px");
		setSpacing(true);
		//facetValuesSelectedMap.clear();
	}

	public void refresh(List<FacetVO> facets, KeySetMap<String, String> facetSelections,
						List<MetadataVO> sortableMetadata, String sortCriterionValue, SortOrder sortOrder) {
		removeAllComponents();
		addComponent(buildSortComponent(sortableMetadata, sortCriterionValue, sortOrder));

		for (FacetVO facet : facets) {
			addComponent(buildFacetComponent(facet, facetSelections.get(facet.getId())));
		}
	}

	private Component buildSortComponent(List<MetadataVO> sortableMetadata, String sortCriterionValue,
										 SortOrder sortOrder) {

		Label sortBy = new Label($("SearchView.sortBy"));
		sortBy.addStyleName(ValoTheme.LABEL_BOLD);
		sortBy.addStyleName("sort-title");

		final ComboBox criterion = new BaseComboBox();
		criterion.setItemCaptionMode(ItemCaptionMode.EXPLICIT);
		criterion.setWidth("100%");

		for (MetadataVO metadata : sortableMetadata) {
			criterion.addItem(metadata.getCode());
			criterion.setItemCaption(metadata.getCode(), metadata.getLabel());
		}
		//		criterion.setPageLength(criterion.size());
		criterion.setValue(sortCriterionValue);

		final OptionGroup order = new OptionGroup();
		order.addItem(SortOrder.ASCENDING);
		order.setItemCaption(SortOrder.ASCENDING, $("SearchView.sortAsc"));
		order.addItem(SortOrder.DESCENDING);
		order.setItemCaption(SortOrder.DESCENDING, $("SearchView.sortDesc"));
		order.setValue(sortOrder);

		ValueChangeListener listener = new ValueChangeListener() {
			@Override
			public void valueChange(ValueChangeEvent event) {
				String sortCriterion = (String) criterion.getValue();
				SortOrder sortOrder = (SortOrder) order.getValue();
				sortCriterionSelected(sortCriterion, sortOrder);
			}
		};

		criterion.addValueChangeListener(listener);
		order.addValueChangeListener(listener);

		VerticalLayout inner = new VerticalLayout(criterion, order);
		inner.setWidth("100%");
		inner.addStyleName("sort-box-content");

		VerticalLayout layout = new VerticalLayout(sortBy, inner);
		layout.setWidth("95%");
		if (Toggle.SEARCH_RESULTS_VIEWER.isEnabled()) {
			layout.addStyleName("sort-box-viewer");
		} else {
			layout.addStyleName("sort-box");
		}

		return layout;
	}

	private Component buildFacetComponent(final FacetVO facet, Set<String> selectedFacetValues) {
		Button apply = new Button($("apply"));
		CheckBox deselect = new CheckBox();
		deselect.setValue(!selectedFacetValues.isEmpty());
		deselect.setEnabled(!selectedFacetValues.isEmpty());
		deselect.addValueChangeListener(new ValueChangeListener() {
			@Override
			public void valueChange(ValueChangeEvent event) {
				facetValuesSelectedMap.remove(facet.getId());
				facetValuesChanged(facetValuesSelectedMap);
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
		captionBar.addStyleName("facet-title");

		I18NHorizontalLayout buttonBar = new I18NHorizontalLayout();

		VerticalLayout layout = new VerticalLayout(captionBar);
		layout.setWidth("95%");

		final Table table = new BaseTable("facet-" + facet.getId());
		table.setColumnHeaderMode(ColumnHeaderMode.HIDDEN);
		table.addContainerProperty("value", Component.class, null);
		table.setWidth("100%");

		apply.addStyleName(ValoTheme.BUTTON_SMALL);
		apply.addStyleName("facet-apply");
		apply.setVisible(false);
		apply.addClickListener(new ClickListener() {
			@Override
			public void buttonClick(ClickEvent event) {

				facetValuesChanged(facetValuesSelectedMap);
			}
		});

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
						facetValuesSelectedMap.add(facetValue.getFacetId(), facetValue.getValue());

					} else {
						facetValuesSelectedMap.remove(facetValue.getFacetId(), facetValue.getValue());
					}
					apply.setVisible(true);
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
					facetOpened(facet.getId());
				} else {
					toggle.setCaption("+");
					table.setVisible(false);
					facetClosed(facet.getId());
				}
			}
		});

		layout.addComponent(table);
		buttonBar.addComponent(apply);
		buttonBar.setComponentAlignment(apply, Alignment.BOTTOM_LEFT);
		buttonBar.setWidth("100%");
		buttonBar.addStyleName("facet-buttonBar");
		layout.addComponent(buttonBar);
		layout.setVisible(!facet.getValues().isEmpty());
		if (Toggle.SEARCH_RESULTS_VIEWER.isEnabled()) {
			layout.addStyleName("facet-box-viewer");
		} else {
			layout.addStyleName("facet-box");
		}
		return layout;
	}

	private void formHasChanged() {
	}

	private void formHasChanged(int facetCount) {
	}


	protected abstract void sortCriterionSelected(String sortCriterion, SortOrder sortOrder);

	protected abstract void facetDeselected(String id);

	protected abstract void facetValueSelected(String facetId, String value);

	protected abstract void facetValuesChanged(KeySetMap<String, String> facets);

	protected abstract void facetValueDeselected(String facetId, String value);

	protected abstract void facetOpened(String id);

	protected abstract void facetClosed(String id);

}
