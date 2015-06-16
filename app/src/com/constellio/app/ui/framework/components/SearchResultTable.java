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

import static com.constellio.app.ui.i18n.i18n.$;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.constellio.app.ui.entities.RecordVO;
import com.constellio.app.ui.framework.containers.SearchResultContainer;
import com.jensjansson.pagedtable.PagedTable;
import com.vaadin.data.Property;
import com.vaadin.data.validator.IntegerRangeValidator;
import com.vaadin.ui.Alignment;
import com.vaadin.ui.Button;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.Button.ClickListener;
import com.vaadin.ui.CheckBox;
import com.vaadin.ui.ComboBox;
import com.vaadin.ui.Component;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Label;
import com.vaadin.ui.Table;
import com.vaadin.ui.TextField;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.themes.ValoTheme;

public class SearchResultTable extends PagedTable {
	public static final String TABLE_STYLE = "search-result-table";
	public static final String CHECKBOX_PROPERTY = "checkbox";
	public static final int DEFAULT_PAGE_LENGTH = 25;

	private SearchResultContainer container;
	private Set<Object> selected;
	private Set<SelectionChangeListener> listeners;

	public SearchResultTable(SearchResultContainer container) {
		super();
		this.container = container;

		listeners = new HashSet<>();
		selected = new HashSet<>();
		addGeneratedColumn(CHECKBOX_PROPERTY, new ColumnGenerator() {
			@Override
			public Object generateCell(Table source, final Object itemId, Object columnId) {
				final CheckBox checkBox = new CheckBox();
				checkBox.setValue(selected.contains(itemId));
				checkBox.addValueChangeListener(new ValueChangeListener() {
					@Override
					public void valueChange(Property.ValueChangeEvent event) {
						if (checkBox.getValue()) {
							selected.add(itemId);
						} else {
							selected.remove(itemId);
						}
						fireSelectionChangeEvent();
					}
				});
				return checkBox;
			}
		});
		setColumnAlignment(CHECKBOX_PROPERTY, Align.CENTER);

		setContainerDataSource(container);
		setColumnHeaderMode(ColumnHeaderMode.HIDDEN);
		setVisibleColumns(CHECKBOX_PROPERTY, SearchResultContainer.SEARCH_RESULT_PROPERTY);
		setColumnExpandRatio(SearchResultContainer.SEARCH_RESULT_PROPERTY, 1);
		setPageLength(Math.min(container.size(), DEFAULT_PAGE_LENGTH));
		addStyleName(TABLE_STYLE);
	}

	public List<String> getSelectedRecordIds() {
		List<String> result = new ArrayList<>(selected.size());
		for (Object itemId : selected) {
			RecordVO record = container.getRecordVO((int) itemId);
			result.add(record.getId());
		}
		return result;
	}

	public void addSelectionChangeListener(SelectionChangeListener listener) {
		listeners.add(listener);
	}

	public VerticalLayout createSummary(final Component... extra) {
		Label count = new Label($("SearchResultTable.count", container.size()));
		count.addStyleName(ValoTheme.LABEL_BOLD);

		final Label selectedCount = new Label($("SearchResultTable.selection", selected.size()));
		selectedCount.setSizeUndefined();

		final HorizontalLayout selection = new HorizontalLayout(selectedCount);
		selection.setComponentAlignment(selectedCount, Alignment.MIDDLE_LEFT);
		selection.setSizeUndefined();
		selection.setSpacing(true);
		for (Component component : extra) {
			component.setEnabled(selected.size() > 0);
			selection.addComponent(component);
			selection.setComponentAlignment(component, Alignment.MIDDLE_LEFT);
		}

		VerticalLayout summaryBar = new VerticalLayout(count, selection);
		summaryBar.setWidth("100%");

		addSelectionChangeListener(new SelectionChangeListener() {
			@Override
			public void selectionChanged(SelectionChangeEvent event) {
				selectedCount.setValue($("SearchResultTable.selection", event.getSelectionSize()));
				for (Component component : extra) {
					component.setEnabled(event.getSelectionSize() > 0);
				}
			}
		});

		return summaryBar;
	}

	public HorizontalLayout createControls() {
		HorizontalLayout pageSize;

		Label itemsPerPageLabel = new Label($("SearchResultTable.itemsPerPage"));
		final ComboBox itemsPerPage = new ComboBox();
		itemsPerPage.addItem(DEFAULT_PAGE_LENGTH);
		if (container.size() >= 25) {
			itemsPerPage.addItem(25);
		}
		if (container.size() >= 50) {
			itemsPerPage.addItem(50);
		}
		if (container.size() >= 100) {
			itemsPerPage.addItem(100);
		}
		itemsPerPage.setNullSelectionAllowed(false);
		itemsPerPage.setWidth("85px");
		itemsPerPage.setValue(DEFAULT_PAGE_LENGTH);
		itemsPerPage.addValueChangeListener(new ValueChangeListener() {
			@Override
			public void valueChange(Property.ValueChangeEvent event) {
				setPageLength((int) itemsPerPage.getValue());
			}
		});
		itemsPerPage.setEnabled(itemsPerPage.size() > 1);

		pageSize = new HorizontalLayout(itemsPerPageLabel, itemsPerPage);
		pageSize.setComponentAlignment(itemsPerPageLabel, Alignment.MIDDLE_LEFT);
		pageSize.setComponentAlignment(itemsPerPage, Alignment.MIDDLE_LEFT);
		pageSize.setSpacing(true);

		Label page = new Label($("SearchResultTable.page"));
		final TextField currentPage = new TextField();
		currentPage.setConverter(Integer.class);
		currentPage.setConvertedValue(getCurrentPage());
		currentPage.setWidth("45px");
		currentPage.addValidator(
				new IntegerRangeValidator("Wrong page number", 1, getTotalAmountOfPages()));
		currentPage.addValueChangeListener(new ValueChangeListener() {
			@Override
			public void valueChange(Property.ValueChangeEvent event) {
				if (currentPage.isValid() && currentPage.getValue() != null) {
					setCurrentPage((int) currentPage.getConvertedValue());
				}
			}
		});
		currentPage.setEnabled(getTotalAmountOfPages() > 1);

		Label separator = new Label($("SearchResultTable.of"));
		final Label totalPages = new Label(String.valueOf(getTotalAmountOfPages()));

		final Button first = new Button("\uF100", new ClickListener() {
			public void buttonClick(ClickEvent event) {
				setCurrentPage(0);
			}
		});
		first.setStyleName(ValoTheme.BUTTON_LINK);
		first.setEnabled(getCurrentPage() > 1);

		final Button previous = new Button("\uF104", new ClickListener() {
			public void buttonClick(ClickEvent event) {
				previousPage();
			}
		});
		previous.setStyleName(ValoTheme.BUTTON_LINK);
		previous.setEnabled(getCurrentPage() > 1);

		final Button next = new Button("\uF105", new ClickListener() {
			public void buttonClick(ClickEvent event) {
				nextPage();
			}
		});
		next.setStyleName(ValoTheme.BUTTON_LINK);
		next.setEnabled(getCurrentPage() < getTotalAmountOfPages());

		final Button last = new Button("\uF101", new ClickListener() {
			public void buttonClick(ClickEvent event) {
				setCurrentPage(getTotalAmountOfPages());
			}
		});
		last.setStyleName(ValoTheme.BUTTON_LINK);
		last.setEnabled(getCurrentPage() < getTotalAmountOfPages());

		HorizontalLayout pageManagement = new HorizontalLayout(
				first, previous, page, currentPage, separator, totalPages, next, last);
		pageManagement.setComponentAlignment(first, Alignment.MIDDLE_LEFT);
		pageManagement.setComponentAlignment(previous, Alignment.MIDDLE_LEFT);
		pageManagement.setComponentAlignment(page, Alignment.MIDDLE_LEFT);
		pageManagement.setComponentAlignment(currentPage, Alignment.MIDDLE_LEFT);
		pageManagement.setComponentAlignment(separator, Alignment.MIDDLE_LEFT);
		pageManagement.setComponentAlignment(totalPages, Alignment.MIDDLE_LEFT);
		pageManagement.setComponentAlignment(next, Alignment.MIDDLE_LEFT);
		pageManagement.setComponentAlignment(last, Alignment.MIDDLE_LEFT);
		pageManagement.setSpacing(true);

		HorizontalLayout controlBar = new HorizontalLayout(pageSize, pageManagement);
		controlBar.setComponentAlignment(pageManagement, Alignment.MIDDLE_CENTER);
		controlBar.setExpandRatio(pageSize, 1);
		controlBar.setWidth("100%");

		addListener(new PageChangeListener() {
			public void pageChanged(PagedTable.PagedTableChangeEvent event) {
				first.setEnabled(getCurrentPage() > 1);
				previous.setEnabled(getCurrentPage() > 1);
				next.setEnabled(getCurrentPage() < getTotalAmountOfPages());
				last.setEnabled(getCurrentPage() < getTotalAmountOfPages());
				currentPage.setValue(String.valueOf(getCurrentPage()));
				currentPage.setEnabled(getTotalAmountOfPages() > 1);
				totalPages.setValue(String.valueOf(getTotalAmountOfPages()));
			}
		});

		return controlBar;
	}

	private void fireSelectionChangeEvent() {
		if (listeners.isEmpty()) {
			return;
		}

		SelectionChangeEvent event = new SelectionChangeEvent(this, selected.size());
		for (SelectionChangeListener listener : listeners) {
			listener.selectionChanged(event);
		}
	}

	public static class SelectionChangeEvent implements Serializable {
		private final SearchResultTable table;
		private final int selectionSize;

		public SelectionChangeEvent(SearchResultTable table, int selectionSize) {
			this.table = table;
			this.selectionSize = selectionSize;
		}

		public SearchResultTable getTable() {
			return table;
		}

		public int getSelectionSize() {
			return selectionSize;
		}
	}

	public interface SelectionChangeListener extends Serializable {
		void selectionChanged(SelectionChangeEvent event);
	}

	public interface PageChangeListener extends PagedTable.PageChangeListener, Serializable {
	}
}
