package com.constellio.app.ui.framework.components.search;

import com.constellio.app.ui.entities.RecordVO;
import com.constellio.app.ui.framework.components.SearchResultTable;
import com.constellio.app.ui.framework.components.table.BaseTable;
import com.constellio.app.ui.framework.components.table.BaseTable.SelectionManager;
import com.constellio.app.ui.framework.components.viewers.panel.ViewableRecordVOTablePanel;
import com.constellio.app.ui.framework.containers.RecordVOContainer;
import com.constellio.app.ui.framework.containers.RecordVOLazyContainer;
import com.constellio.app.ui.framework.containers.SearchResultContainer;
import com.constellio.app.ui.framework.containers.SearchResultVOLazyContainer;
import com.constellio.app.ui.pages.search.SearchPresenter;
import com.constellio.app.ui.pages.search.SearchView;
import com.constellio.app.ui.pages.search.batchProcessing.BatchProcessingButton;
import com.constellio.app.ui.pages.search.batchProcessing.BatchProcessingModifyingOneMetadataButton;
import com.vaadin.ui.Alignment;
import com.vaadin.ui.Component;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Label;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.themes.ValoTheme;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static com.constellio.app.ui.i18n.i18n.$;

public class ViewableRecordVOSearchResultTable extends ViewableRecordVOTablePanel implements SearchResultTable {

	public static final String TABLE_STYLE = "search-result-table";

	private static final int MAX_SELECTION_RANGE = 100;

	private Set<Object> selectedItemIds = new HashSet<>();
	private Set<Object> deselectedItemIds = new HashSet<>();
	private boolean allItemsSelected;

	private SearchPresenter<? extends SearchView> presenter;

	public ViewableRecordVOSearchResultTable(RecordVOContainer container, TableMode tableMode,
											 SearchPresenter<? extends SearchView> presenter) {
		super(container, tableMode);
		this.presenter = presenter;
		addStyleName(TABLE_STYLE);
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
				if (event.getSelectedItemId() != null) {
					Object selectedItemId = event.getSelectedItemId();
					selectedItemIds.add(selectedItemId);
					deselectedItemIds.remove(selectedItemId);
					presenter.fireSomeRecordsSelected();
				} else if (event.getDeselectedItemId() != null) {
					Object deselectedItemId = event.getDeselectedItemId();
					selectedItemIds.remove(deselectedItemId);
					deselectedItemIds.add(deselectedItemId);
					if (selectedItemIds.isEmpty()) {
						presenter.fireNoRecordSelected();
					} else {
						presenter.fireSomeRecordsSelected();
					}
				} else if (event.isAllItemsSelected()) {
					allItemsSelected = true;
					selectedItemIds.clear();
					deselectedItemIds.clear();
					presenter.fireSomeRecordsSelected();
				} else if (event.isAllItemsDeselected()) {
					allItemsSelected = false;
					selectedItemIds.clear();
					deselectedItemIds.clear();
					presenter.fireNoRecordSelected();
				}
			}

			@Override
			public boolean isAllItemsSelected() {
				return ViewableRecordVOSearchResultTable.this.isSelectAll();
			}

			@Override
			public boolean isAllItemsDeselected() {
				return !ViewableRecordVOSearchResultTable.this.isSelectAll() && ViewableRecordVOSearchResultTable.this.getSelectedRecordIds().isEmpty();
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

		final HorizontalLayout selection = new HorizontalLayout();
		selection.setSizeUndefined();
		selection.setSpacing(true);
		for (Component component : extra) {
			if (component instanceof BatchProcessingButton || component instanceof BatchProcessingModifyingOneMetadataButton) {
				component.setEnabled(recordVOContainer != null && recordVOContainer.size() > 0);
			} else {
				component.setEnabled(selectedItemIds.size() > 0);
			}
			selection.addComponent(component);
			selection.setComponentAlignment(component, Alignment.MIDDLE_LEFT);
		}

		VerticalLayout summaryBar = new VerticalLayout(count, selection);
		summaryBar.setWidth("100%");

		addSelectionChangeListener(new BaseTable.SelectionChangeListener() {
			@Override
			public void selectionChanged(BaseTable.SelectionChangeEvent event) {
				boolean somethingSelected = event.isAllItemsSelected() || event.getSelectedItemId() != null;
				for (Component component : extra) {
					if (component instanceof BatchProcessingButton
						|| component instanceof BatchProcessingModifyingOneMetadataButton) {
						component.setEnabled(recordVOContainer != null && recordVOContainer.size() > 0);
					} else {
						component.setEnabled(somethingSelected);
					}
				}
			}
		});

		return summaryBar;
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
