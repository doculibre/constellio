package com.constellio.app.ui.framework.components.search;

import com.constellio.app.ui.entities.RecordVO;
import com.constellio.app.ui.framework.components.SearchResultTable;
import com.constellio.app.ui.framework.components.menuBar.RecordListMenuBar;
import com.constellio.app.ui.framework.components.table.BaseTable.SelectionManager;
import com.constellio.app.ui.framework.components.viewers.panel.ViewableRecordVOTablePanel;
import com.constellio.app.ui.framework.containers.RecordVOContainer;
import com.constellio.app.ui.framework.containers.RecordVOLazyContainer;
import com.constellio.app.ui.framework.containers.SearchResultContainer;
import com.constellio.app.ui.framework.containers.SearchResultVOLazyContainer;
import com.constellio.app.ui.pages.search.SearchPresenter;
import com.constellio.app.ui.pages.search.SearchView;
import com.vaadin.ui.Component;
import com.vaadin.ui.VerticalLayout;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static com.constellio.app.ui.i18n.i18n.$;

public class ViewableRecordVOSearchResultTable extends ViewableRecordVOTablePanel implements SearchResultTable {

	public static final String TABLE_STYLE = "viewable-record-search-result-table-panel";

	private Set<Object> selectedItemIds = new HashSet<>();
	private Set<Object> deselectedItemIds = new HashSet<>();
	private boolean allItemsSelected;

	private SearchPresenter<? extends SearchView> presenter;

	public ViewableRecordVOSearchResultTable(RecordVOContainer container, TableMode tableMode,
											 SearchPresenter<? extends SearchView> presenter,
											 RecordListMenuBar recordListMenuBar) {
		super(container, tableMode, recordListMenuBar);
		this.presenter = presenter;
		addStyleName(TABLE_STYLE);
		addStyleName(SEARCH_RESULT_TABLE_STYLE);
		setSelectionActionButtons();
	}

	@Override
	public void setTableMode(TableMode tableMode) {
		super.setTableMode(tableMode);
		if (tableMode == TableMode.TABLE) {
			removeStyleName(SEARCH_RESULT_TABLE_STYLE);
		} else {
			addStyleName(SEARCH_RESULT_TABLE_STYLE);
		}
	}

	@Override
	protected boolean isSelectColumn() {
		return true;
	}

	@Override
	protected SelectionManager newSelectionManager() {
		return new SelectionManager() {
			@Override
			public void selectionChanged(
					com.constellio.app.ui.framework.components.table.BaseTable.SelectionChangeEvent event) {
				RecordVOContainer recordVOContainer = getRecordVOContainer();
				if (event.getSelectedItemIds() != null) {
					List<Object> selectedItemIds = event.getSelectedItemIds();
					for (Object selectedItemId : selectedItemIds) {
						ViewableRecordVOSearchResultTable.this.selectedItemIds.add(selectedItemId);
						ViewableRecordVOSearchResultTable.this.deselectedItemIds.remove(selectedItemId);
					}
					presenter.fireSomeRecordsSelected();
				} else if (event.getDeselectedItemIds() != null) {
					List<Object> deselectedItemIds = event.getDeselectedItemIds();
					for (Object deselectedItemId : deselectedItemIds) {
						ViewableRecordVOSearchResultTable.this.selectedItemIds.remove(deselectedItemId);
						ViewableRecordVOSearchResultTable.this.deselectedItemIds.add(deselectedItemId);
					}
					if (selectedItemIds.isEmpty()) {
						presenter.fireNoRecordSelected();
					} else {
						presenter.fireSomeRecordsSelected();
					}
				} else if (event.isAllItemsSelected()) {
					ViewableRecordVOSearchResultTable.this.allItemsSelected = true;
					ViewableRecordVOSearchResultTable.this.selectedItemIds.addAll(recordVOContainer.getItemIds());
					ViewableRecordVOSearchResultTable.this.deselectedItemIds.clear();
					presenter.fireSomeRecordsSelected();
				} else if (event.isAllItemsDeselected()) {
					ViewableRecordVOSearchResultTable.this.allItemsSelected = false;
					ViewableRecordVOSearchResultTable.this.selectedItemIds.clear();
					ViewableRecordVOSearchResultTable.this.deselectedItemIds.clear();
					presenter.fireNoRecordSelected();
				}
			}

			@Override
			public List<Object> getAllSelectedItemIds() {
				List<Object> allSelectedItemIds;
				RecordVOContainer recordVOContainer = getRecordVOContainer();
				if (isAllItemsSelected()) {
					allSelectedItemIds = new ArrayList<>(recordVOContainer.getItemIds());
				} else {
					allSelectedItemIds = new ArrayList<>(selectedItemIds);
				}
				return allSelectedItemIds;
			}

			@Override
			public boolean isAllItemsSelected() {
				return allItemsSelected;
			}

			@Override
			public boolean isAllItemsDeselected() {
				return !allItemsSelected && selectedItemIds.isEmpty();
			}

			@Override
			public boolean isSelected(Object itemId) {
				RecordVO recordVO = getRecordVO(itemId);
				String recordId = recordVO.getId();
				return ViewableRecordVOSearchResultTable.this.isSelectAll() || ViewableRecordVOSearchResultTable.this.getSelectedRecordIds().contains(recordId);
			}
		};
	}

	public List<String> getSelectedRecordIds() {
		List<String> result = new ArrayList<>();
		for (Object itemId : selectedItemIds) {
			RecordVO recordVO = getRecordVO(itemId);
			result.add(recordVO.getId());
		}
		return result;
	}

	public List<String> getUnselectedRecordIds() {
		List<String> result = new ArrayList<>();
		for (Object itemId : deselectedItemIds) {
			RecordVO recordVO = getRecordVO(itemId);
			result.add(recordVO.getId());
		}
		return result;
	}

	public boolean isSelectAll() {
		return allItemsSelected;
	}

	public VerticalLayout createSummary(Component alwaysActive, final Component... extra) {
		return createSummary(Arrays.asList(alwaysActive), extra);
	}

	public VerticalLayout createSummary(List<Component> alwaysActive, final Component... extra) {
		return createSummary(alwaysActive, Arrays.asList(extra));
	}

	public VerticalLayout createSummary(List<Component> alwaysActive, final List<Component> extra) {
		RecordVOContainer recordVOContainer = getRecordVOContainer();

		double totalInSeconds = getLastCallQTime();
		String qtime = "" + totalInSeconds;

		int size = recordVOContainer.size();
		String key = size <= 1 ? "SearchResultTable.count1" : "SearchResultTable.counts";
		String totalCount = $(key, size, qtime);
		setCountCaption(totalCount);

		return null;
	}

	private double getLastCallQTime() {
		double lastCallQTime;
		RecordVOContainer recordVOContainer = getRecordVOContainer();
		if (recordVOContainer instanceof RecordVOLazyContainer) {
			lastCallQTime = ((RecordVOLazyContainer) recordVOContainer).getLastCallQTime();
		} else if (recordVOContainer instanceof SearchResultVOLazyContainer) {
			lastCallQTime = ((SearchResultVOLazyContainer) recordVOContainer).getLastCallQTime();
		} else {
			lastCallQTime = ((SearchResultContainer) recordVOContainer).getLastCallQTime();
		}
		return lastCallQTime;
	}

}
