package com.constellio.app.ui.framework.components;

import static com.constellio.app.ui.i18n.i18n.$;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.constellio.app.ui.entities.RecordVO;
import com.constellio.app.ui.framework.components.table.BasePagedTable;
import com.constellio.app.ui.framework.containers.SearchResultContainer;
import com.jensjansson.pagedtable.PagedTable;
import com.vaadin.data.Property;
import com.vaadin.ui.Alignment;
import com.vaadin.ui.CheckBox;
import com.vaadin.ui.Component;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Label;
import com.vaadin.ui.Table;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.themes.ValoTheme;

public class SearchResultDetailedTable extends BasePagedTable<SearchResultContainer> implements SearchResultTable{
	public static final String TABLE_STYLE = "search-result-table";
	public static final String CHECKBOX_PROPERTY = "checkbox";

	private Set<Object> selected;
	private Set<SelectionChangeListener> listeners;

	public SearchResultDetailedTable(SearchResultContainer container) {
		this(container, true);
	}

	public SearchResultDetailedTable(SearchResultContainer container, boolean withCheckBoxes) {
		super(container);

		listeners = new HashSet<>();
		selected = new HashSet<>();
		if (withCheckBoxes) {
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
		}
		setContainerDataSource(container);
		setColumnHeaderMode(ColumnHeaderMode.HIDDEN);
		if (withCheckBoxes) {
			setVisibleColumns(CHECKBOX_PROPERTY, SearchResultContainer.SEARCH_RESULT_PROPERTY);
		} else {
			setVisibleColumns(SearchResultContainer.SEARCH_RESULT_PROPERTY);
		}
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

	public void selectCurrentPage() {
		selected.addAll(container.getItemIds((getCurrentPage() - 1) * getPageLength(), getPageLength()));
		refreshRowCache();
		fireSelectionChangeEvent();
	}

	public void deselectCurrentPage() {
		selected.removeAll(container.getItemIds((getCurrentPage() - 1) * getPageLength(), getPageLength()));
		refreshRowCache();
		fireSelectionChangeEvent();
	}

	public void addSelectionChangeListener(SelectionChangeListener listener) {
		listeners.add(listener);
	}

	public VerticalLayout createSummary(Component alwaysActive, final Component... extra) {
		return createSummary(Arrays.asList(alwaysActive), extra);
	}

	public VerticalLayout createSummary(List<Component> alwaysActive, final Component... extra) {
		return createSummary(alwaysActive, Arrays.asList(extra));
	}

	public VerticalLayout createSummary(List<Component> alwaysActive, final List<Component> extra) {
		Label totalCount = new Label($("SearchResultTable.count", container.size()));
		totalCount.addStyleName(ValoTheme.LABEL_BOLD);

		HorizontalLayout count = new HorizontalLayout(totalCount);
		count.setComponentAlignment(totalCount, Alignment.MIDDLE_LEFT);
		count.setSizeUndefined();
		count.setSpacing(true);

		for (Component component : alwaysActive) {
			count.addComponent(component);
			count.setComponentAlignment(component, Alignment.MIDDLE_LEFT);
		}

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
		private final SearchResultDetailedTable table;
		private final int selectionSize;

		public SelectionChangeEvent(SearchResultDetailedTable table, int selectionSize) {
			this.table = table;
			this.selectionSize = selectionSize;
		}

		public SearchResultDetailedTable getTable() {
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
