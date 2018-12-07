package com.constellio.app.ui.framework.components;

import com.constellio.app.ui.entities.RecordVO;
import com.constellio.app.ui.framework.components.table.BasePagedTable;
import com.constellio.app.ui.framework.components.table.TablePropertyCache.CellKey;
import com.constellio.app.ui.framework.containers.SearchResultContainer;
import com.constellio.app.ui.pages.search.batchProcessing.BatchProcessingButton;
import com.constellio.app.ui.pages.search.batchProcessing.BatchProcessingModifyingOneMetadataButton;
import com.jensjansson.pagedtable.PagedTable;
import com.vaadin.data.Property;
import com.vaadin.data.util.ObjectProperty;
import com.vaadin.ui.Alignment;
import com.vaadin.ui.CheckBox;
import com.vaadin.ui.Component;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Label;
import com.vaadin.ui.Table;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.themes.ValoTheme;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import static com.constellio.app.ui.i18n.i18n.$;

public class SearchResultDetailedTable extends BasePagedTable<SearchResultContainer> implements SearchResultTable {
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
		super("SearchResultDetailedTable", container);

		listeners = new HashSet<>();
		selected = new LinkedHashSet<>();
		deselected = new LinkedHashSet<>();
		this.withCheckBoxes = withCheckBoxes;
		if (withCheckBoxes) {
			addGeneratedColumn(CHECKBOX_PROPERTY, new ColumnGenerator() {
				@Override
				public Object generateCell(Table source, final Object itemId, Object columnId) {
					final CheckBox checkBox;
					CellKey cellKey = new CellKey(itemId, columnId);
					Property<?> cellProperty = cellProperties.get(cellKey);
					if (cellProperty != null) {
						checkBox = (CheckBox) cellProperty.getValue();
					} else {
						checkBox = new CheckBox();
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
						cellProperties.put(cellKey, new ObjectProperty<Object>(checkBox));
					}
					return checkBox;
				}
			});
			setColumnAlignment(CHECKBOX_PROPERTY, Align.CENTER);
		}
		setContainerDataSource(container);
		setColumnHeaderMode(ColumnHeaderMode.HIDDEN);
		if (withCheckBoxes) {
			setVisibleColumns(CHECKBOX_PROPERTY, SearchResultContainer.INDEX_PROPERTY_ID, SearchResultContainer.SEARCH_RESULT_PROPERTY);
			setColumnWidth(CHECKBOX_PROPERTY, 44);
		} else {
			setVisibleColumns(SearchResultContainer.INDEX_PROPERTY_ID, SearchResultContainer.SEARCH_RESULT_PROPERTY);
		}
		setColumnWidth(SearchResultContainer.INDEX_PROPERTY_ID, 40);
		setColumnExpandRatio(SearchResultContainer.SEARCH_RESULT_PROPERTY, 1);
		setColumnAlignment(SearchResultContainer.INDEX_PROPERTY_ID, Align.LEFT);
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
		List<?> itemIds = container.getItemIds((getCurrentPage() - 1) * getPageLength(), getPageLength());
		for (Object itemId : itemIds) {
			CellKey cellKey = new CellKey(itemId, CHECKBOX_PROPERTY);
			Property<?> checkBoxProperty = cellProperties.get(cellKey);
			if (checkBoxProperty != null) {
				((CheckBox) checkBoxProperty.getValue()).setValue(true);
			}
		}
		selected.addAll(itemIds);
		deselected.removeAll(itemIds);
		refreshRowCache();
		fireSelectionChangeEvent();
	}

	public void deselectCurrentPage() {
		selectAll = false;
		List<?> itemIds = container.getItemIds((getCurrentPage() - 1) * getPageLength(), getPageLength());
		for (Object itemId : itemIds) {
			CellKey cellKey = new CellKey(itemId, CHECKBOX_PROPERTY);
			Property<?> checkBoxProperty = cellProperties.get(cellKey);
			if (checkBoxProperty != null) {
				((CheckBox) checkBoxProperty.getValue()).setValue(false);
			}
		}
		selected.removeAll(itemIds);
		deselected.addAll(itemIds);
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

		int total = container.getDataProvider().getQTime();

		double totalInSeconds;

		if (total < 10) {
			totalInSeconds = total / 1000.0;
		} else {
			totalInSeconds = Math.round(total / 10.0) / 100.0;
		}
		String qtime = "" + totalInSeconds;

		int size = container.size();
		String key = size <= 1 ? "SearchResultTable.count1" : "SearchResultTable.counts";
		Label totalCount = new Label($(key, size, qtime));
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
			if (isComponentDisabledBySelection(component)) {
				component.setEnabled(selected.size() > 0);
			}
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
					if (isComponentDisabledBySelection(component)) {
						component.setEnabled(event.getSelectionSize() > 0);
					} else if (component instanceof BatchProcessingButton) {
						((BatchProcessingButton) component).hasResultSelected(event.getSelectionSize() > 0);
					} else if (component instanceof BatchProcessingModifyingOneMetadataButton) {
						((BatchProcessingModifyingOneMetadataButton) component).hasResultSelected(event.getSelectionSize() > 0);
					}
				}
			}
		});

		return summaryBar;
	}

	private boolean isComponentDisabledBySelection(Component component) {
		return !(component instanceof BatchProcessingButton || component instanceof BatchProcessingModifyingOneMetadataButton || component instanceof ReportTabButton);
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

	@Override
	protected CellKey getCellKey(Object itemId, Object propertyId) {
		RecordVO recordVO = container.getRecordVO((int) itemId);
		return new CellKey(recordVO.getId(), propertyId);
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
