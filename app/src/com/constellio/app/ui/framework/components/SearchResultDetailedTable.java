package com.constellio.app.ui.framework.components;

import com.constellio.app.ui.entities.RecordVO;
import com.constellio.app.ui.framework.components.table.BasePagedTable;
import com.constellio.app.ui.framework.components.table.TablePropertyCache.CellKey;
import com.constellio.app.ui.framework.components.table.columns.TableColumnsManager;
import com.constellio.app.ui.framework.containers.SearchResultContainer;
import com.constellio.app.ui.pages.search.batchProcessing.BatchProcessingButton;
import com.constellio.app.ui.pages.search.batchProcessing.BatchProcessingModifyingOneMetadataButton;
import com.constellio.app.ui.util.ComponentTreeUtils;
import com.constellio.data.utils.dev.Toggle;
import com.jensjansson.pagedtable.PagedTable;
import com.vaadin.data.Item;
import com.vaadin.data.Property;
import com.vaadin.data.util.ObjectProperty;
import com.vaadin.event.ItemClickEvent;
import com.vaadin.event.ItemClickEvent.ItemClickListener;
import com.vaadin.event.LayoutEvents.LayoutClickEvent;
import com.vaadin.event.LayoutEvents.LayoutClickListener;
import com.vaadin.event.MouseEvents.ClickEvent;
import com.vaadin.event.MouseEvents.ClickListener;
import com.vaadin.shared.MouseEventDetails;
import com.vaadin.shared.MouseEventDetails.MouseButton;
import com.vaadin.ui.AbstractOrderedLayout;
import com.vaadin.ui.Alignment;
import com.vaadin.ui.Button;
import com.vaadin.ui.CheckBox;
import com.vaadin.ui.Component;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Image;
import com.vaadin.ui.Label;
import com.vaadin.ui.MenuBar;
import com.vaadin.ui.Table;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.themes.ValoTheme;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import static com.constellio.app.ui.i18n.i18n.$;

public class SearchResultDetailedTable extends BasePagedTable<SearchResultContainer> implements SearchResultTable {

	public static final String CHECKBOX_PROPERTY = "checkbox";

	private Set<Object> selected;
	private Set<Object> deselected;
	private boolean selectAll;
	private Set<SelectionChangeListener> listeners;
	private boolean withCheckBoxes;


	public SearchResultDetailedTable(SearchResultContainer container, boolean withCheckBoxes, boolean withIdColumn) {
		super("SearchResultDetailedTable", container);

		addStyleName(ValoTheme.TABLE_BORDERLESS);

		addStyleName("search-result-table");

		if (Toggle.SEARCH_RESULTS_VIEWER.isEnabled()) {
			addStyleName("search-result-viewer-table");
		} else {
			addStyleName("search-result-detailed-table");
		}

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

		List<String> visibleColumns = new ArrayList<>();
		if (withCheckBoxes) {
			visibleColumns.add(CHECKBOX_PROPERTY);
			setColumnWidth(CHECKBOX_PROPERTY, 44);
		}

		if (Toggle.SEARCH_RESULTS_VIEWER.isEnabled()) {
			visibleColumns.add(SearchResultContainer.THUMBNAIL_PROPERTY);
			setColumnWidth(SearchResultContainer.THUMBNAIL_PROPERTY, SearchResultContainer.THUMBNAIL_WIDTH);
		}

		if (withIdColumn) {
			visibleColumns.add(SearchResultContainer.INDEX_PROPERTY_ID);
			setColumnWidth(SearchResultContainer.INDEX_PROPERTY_ID, 40);
			setColumnAlignment(SearchResultContainer.INDEX_PROPERTY_ID, Align.LEFT);
		}

		visibleColumns.add(SearchResultContainer.SEARCH_RESULT_PROPERTY);
		setVisibleColumns(visibleColumns.toArray());


		setColumnExpandRatio(SearchResultContainer.SEARCH_RESULT_PROPERTY, 1);

		setPageLength(Math.min(container.size(), DEFAULT_PAGE_LENGTH));
	}

	@Override
	protected Property<?> loadContainerProperty(final Object itemId, final Object propertyId) {
		Property<?> property = super.loadContainerProperty(itemId, propertyId);
		if (Toggle.SEARCH_RESULTS_VIEWER.isEnabled()) {
			if (SearchResultContainer.SEARCH_RESULT_PROPERTY.equals(propertyId)) {
				Object propertyValue = property.getValue();
				if (propertyValue instanceof AbstractOrderedLayout) {
					AbstractOrderedLayout layout = (AbstractOrderedLayout) propertyValue;
					layout.addLayoutClickListener(new LayoutClickListener() {
						@Override
						public void layoutClick(LayoutClickEvent event) {
							if (!(event.getSource() instanceof MenuBar)) {
								MouseEventDetails mouseEventDetails = new MouseEventDetails();
								mouseEventDetails.setButton(event.getButton());
								mouseEventDetails.setClientX(event.getClientX());
								mouseEventDetails.setClientY(event.getClientY());
								mouseEventDetails.setRelativeX(event.getRelativeX());
								mouseEventDetails.setRelativeY(event.getRelativeY());

								Item item = getItem(itemId);
								Collection<?> itemClickListeners = getListeners(ItemClickEvent.class);
								for (Object itemClickListenerObj : itemClickListeners) {
									ItemClickListener itemClickListener = (ItemClickListener) itemClickListenerObj;
									itemClickListener.itemClick(new ItemClickEvent(SearchResultDetailedTable.this, item, itemId, propertyId, mouseEventDetails));
								}
							}
						}
					});

					List<Button> buttons = ComponentTreeUtils.getChildren(layout, Button.class);
					for (Button button : buttons) {
						button.addClickListener(new Button.ClickListener() {
							@Override
							public void buttonClick(com.vaadin.ui.Button.ClickEvent event) {
								MouseEventDetails mouseEventDetails = new MouseEventDetails();
								mouseEventDetails.setButton(MouseButton.LEFT);
								mouseEventDetails.setClientX(event.getClientX());
								mouseEventDetails.setClientY(event.getClientY());
								mouseEventDetails.setRelativeX(event.getRelativeX());
								mouseEventDetails.setRelativeY(event.getRelativeY());

								Item item = getItem(itemId);
								Collection<?> itemClickListeners = getListeners(ItemClickEvent.class);
								for (Object itemClickListenerObj : itemClickListeners) {
									ItemClickListener itemClickListener = (ItemClickListener) itemClickListenerObj;
									itemClickListener.itemClick(new ItemClickEvent(SearchResultDetailedTable.this, item, itemId, propertyId, mouseEventDetails));
								}
							}
						});
					}
					property = new ObjectProperty<>(layout);
				}
			} else if (SearchResultContainer.THUMBNAIL_PROPERTY.equals(propertyId)) {
				Object propertyValue = property.getValue();
				if (propertyValue instanceof Image) {
					Image image = (Image) propertyValue;
					image.addClickListener(new ClickListener() {
						@Override
						public void click(ClickEvent event) {
							Collection<?> itemClickListeners = getListeners(ItemClickEvent.class);
							MouseEventDetails mouseEventDetails = new MouseEventDetails();
							mouseEventDetails.setButton(event.getButton());
							mouseEventDetails.setClientX(event.getClientX());
							mouseEventDetails.setClientY(event.getClientY());
							mouseEventDetails.setRelativeX(event.getRelativeX());
							mouseEventDetails.setRelativeY(event.getRelativeY());
							Item item = getItem(itemId);
							for (Object itemClickListenerObj : itemClickListeners) {
								ItemClickListener itemClickListener = (ItemClickListener) itemClickListenerObj;
								itemClickListener.itemClick(new ItemClickEvent(SearchResultDetailedTable.this, item, itemId, propertyId, mouseEventDetails));
							}
						}
					});
					property = new ObjectProperty<>(image);
				}
			}
		}
		return property;
	}

	@Override
	protected TableColumnsManager newColumnsManager() {
		List<Object> visibleColumns = new ArrayList<>(Arrays.asList(getVisibleColumns()));
		visibleColumns.remove(CHECKBOX_PROPERTY);
		visibleColumns.add(0, CHECKBOX_PROPERTY);
		setVisibleColumns(visibleColumns.toArray(new Object[0]));
		return super.newColumnsManager();
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
