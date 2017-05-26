package com.constellio.app.ui.framework.components;

import com.constellio.app.ui.entities.RecordVO;
import com.constellio.app.ui.framework.components.table.BasePagedTable;
import com.constellio.app.ui.framework.containers.SearchResultContainer;
import com.jensjansson.pagedtable.PagedTable;
import com.vaadin.data.Property;
import com.vaadin.ui.*;
import com.vaadin.ui.themes.ValoTheme;

import java.io.Serializable;
import java.util.*;

import static com.constellio.app.ui.i18n.i18n.$;

public class SearchResultDetailedTable extends BasePagedTable<SearchResultContainer> implements SearchResultTable{
	public static final String TABLE_STYLE = "search-result-table";
	public static final String CHECKBOX_PROPERTY = "checkbox";

	private Set<Object> selected;
	private Set<Object> deselected;
	private boolean selectAll;
	private Set<SelectionChangeListener> listeners;
	private boolean withCheckBoxes;

	public SearchResultDetailedTable(SearchResultContainer container) {
		this(container, true);
	}

	public SearchResultDetailedTable(SearchResultContainer container, boolean withCheckBoxes) {
		super(container);

		listeners = new HashSet<>();
		selected = new HashSet<>();
		deselected = new HashSet<>();
		this.withCheckBoxes = withCheckBoxes;
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
								deselected.remove(itemId);
							} else {
								selected.remove(itemId);
								deselected.add(itemId);
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
		List<String> result = new ArrayList<>();
		for (Object itemId : selected) {
			RecordVO record = container.getRecordVO((int) itemId);
			result.add(record.getId());
		}
		return result;
	}

	public List<String> getUnselectedRecordIds() {
		List<String> result = new ArrayList<>();
		for (Object itemId : deselected) {
			RecordVO record = container.getRecordVO((int) itemId);
			result.add(record.getId());
		}
		return result;
	}

	public void selectCurrentPage() {
		selectAll = true;
		selected.addAll(container.getItemIds((getCurrentPage() - 1) * getPageLength(), getPageLength()));
		deselected.removeAll(container.getItemIds((getCurrentPage() - 1) * getPageLength(), getPageLength()));
		refreshRowCache();
		fireSelectionChangeEvent();
	}

	public void deselectCurrentPage() {
		selectAll = false;
		selected.removeAll(container.getItemIds((getCurrentPage() - 1) * getPageLength(), getPageLength()));
		deselected.addAll(container.getItemIds((getCurrentPage() - 1) * getPageLength(), getPageLength()));
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
		selectedCount.setVisible(withCheckBoxes);

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
