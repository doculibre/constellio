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
package com.constellio.app.ui.framework.components;

import java.util.Arrays;
import java.util.List;

import org.apache.commons.lang3.StringUtils;

import com.constellio.app.ui.framework.buttons.SearchButton;
import com.constellio.app.ui.framework.components.fields.BaseTextField;
import com.constellio.app.ui.handlers.OnEnterKeyHandler;
import com.vaadin.data.Container.Filterable;
import com.vaadin.data.util.filter.SimpleStringFilter;
import com.vaadin.ui.Button;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.Button.ClickListener;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Table;
import com.vaadin.ui.TextField;

public class TableStringFilter extends HorizontalLayout {

	private TextField filterField;
	private Button filterButton;
	private Table table;

	public TableStringFilter(final Table table) {
		this.table = table;

		filterField = new BaseTextField();
		new OnEnterKeyHandler() {
			@Override
			public void onEnterKeyPressed() {
				addFilter();
			}
		}.installOn(filterField);

		filterButton = new SearchButton();
		filterButton.addClickListener(new ClickListener() {
			@Override
			public void buttonClick(ClickEvent event) {
				addFilter();
			}
		});

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
