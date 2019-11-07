package com.constellio.app.ui.framework.components;

import com.constellio.app.ui.framework.buttons.SearchButton;
import com.constellio.app.ui.framework.components.fields.BaseTextField;
import com.constellio.app.ui.handlers.OnEnterKeyHandler;
import com.vaadin.data.Container.Filterable;
import com.vaadin.data.util.filter.SimpleStringFilter;
import com.vaadin.ui.Button;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Table;
import com.vaadin.ui.TextField;
import org.apache.commons.lang3.StringUtils;

import java.util.Arrays;
import java.util.List;

public class TableStringFilter extends HorizontalLayout {

	private TextField filterField;
	private Button filterButton;
	private Table table;

	public TableStringFilter(final Table table) {
		setSpacing(true);

		this.table = table;

		filterField = new BaseTextField();
		new OnEnterKeyHandler() {
			@Override
			public void onEnterKeyPressed() {
				addFilter();
			}
		}.installOn(filterField);

		filterButton = new SearchButton() {
			@Override
			protected void buttonClick(ClickEvent event) {
				addFilter();
			}
		};
		addComponents(filterField, filterButton);
	}

	private void addFilter() {
		List<String> captionColumnHeaders = Arrays.asList(table.getColumnHeaders());

		Filterable filterableContainer = (Filterable) table.getContainerDataSource();
		String filterValue = filterField.getValue();
		filterableContainer.removeAllContainerFilters();
		if (StringUtils.isNotBlank(filterValue)) {
			filterableContainer.addContainerFilter(new SimpleStringFilter(null, filterValue, false, false));
		}
		table.setContainerDataSource(null);
		table.setContainerDataSource(filterableContainer);

		for (int i = 0; i < table.getColumnHeaders().length; i++) {
			table.setColumnHeader(table.getColumnHeaders()[i], captionColumnHeaders.get(i));
		}
	}

	public final TextField getFilterField() {
		return filterField;
	}

	public final Button getFilterButton() {
		return filterButton;
	}

	public final Table getTable() {
		return table;
	}

}
