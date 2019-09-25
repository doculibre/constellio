package com.constellio.app.ui.framework.components.table;

import com.constellio.app.ui.framework.buttons.BaseButton;
import com.constellio.app.ui.framework.buttons.SelectDeselectAllButton;
import com.constellio.app.ui.framework.components.BaseForm;
import com.constellio.app.ui.framework.components.BaseWindow;
import com.constellio.app.ui.framework.components.contextmenu.BaseContextMenuTableListener;
import com.constellio.app.ui.framework.components.fields.BaseComboBox;
import com.constellio.app.ui.framework.components.fields.number.BaseIntegerField;
import com.constellio.app.ui.framework.components.layouts.I18NHorizontalLayout;
import com.constellio.app.ui.framework.components.table.TablePropertyCache.CellKey;
import com.constellio.app.ui.framework.components.table.columns.TableColumnsManager;
import com.constellio.model.frameworks.validation.ValidationErrors;
import com.constellio.model.frameworks.validation.ValidationException;
import com.jensjansson.pagedtable.PagedTableContainer;
import com.vaadin.data.Container;
import com.vaadin.data.Property;
import com.vaadin.data.fieldgroup.PropertyId;
import com.vaadin.data.util.ObjectProperty;
import com.vaadin.data.util.converter.Converter.ConversionException;
import com.vaadin.ui.Alignment;
import com.vaadin.ui.Button;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.Button.ClickListener;
import com.vaadin.ui.CheckBox;
import com.vaadin.ui.ComboBox;
import com.vaadin.ui.Component;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Label;
import com.vaadin.ui.MenuBar;
import com.vaadin.ui.Table;
import com.vaadin.ui.TextField;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.themes.ValoTheme;
import org.apache.commons.lang3.StringUtils;
import org.vaadin.peter.contextmenu.ContextMenu;
import org.vaadin.peter.contextmenu.ContextMenu.ContextMenuOpenedOnTableFooterEvent;
import org.vaadin.peter.contextmenu.ContextMenu.ContextMenuOpenedOnTableHeaderEvent;
import org.vaadin.peter.contextmenu.ContextMenu.ContextMenuOpenedOnTableRowEvent;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.constellio.app.ui.i18n.i18n.$;
import static com.constellio.app.ui.i18n.i18n.isRightToLeft;

public class BaseTable extends Table {

	public static final int MAX_SELECTION_LENGTH = 1000;

	public static final int DEFAULT_PAGE_LENGTH = 10;

	public static final int SELECT_PROPERTY_WIDTH = 44;

	public static final int INDEX_PROPERTY_WIDTH = 44;

	public static final int MENUBAR_PROPERTY_WIDTH = 44;

	public static final String SELECT_PROPERTY_ID = "select";

	public static final String INDEX_PROPERTY_ID = "index";

	public static final String MENUBAR_PROPERTY_ID = "menuBar";

	private String tableId;

	private TableColumnsManager columnsManager;

	protected final TablePropertyCache cellProperties = new TablePropertyCache();

	private boolean columnGeneratorsAdded;

	private List<SelectionChangeListener> selectionChangeListeners = new ArrayList<>();

	private SelectionManager selectionManager;

	private PagedTableContainer pagedTableContainer;

	private List<PageChangeListener> pageChangeListeners = new ArrayList<>();

	private ContextMenu contextMenu;

	public BaseTable(String tableId) {
		super();
		this.tableId = tableId;
		init();
	}

	public BaseTable(String tableId, String caption) {
		super(caption);
		this.tableId = tableId;
		init();
	}

	public BaseTable(String tableId, String caption, Container dataSource) {
		super(caption, dataSource);
		this.tableId = tableId;
		init();
	}

	private void init() {
		addStyleName("base-table");
		if (isSelectColumn()) {
			addGeneratedColumn(SELECT_PROPERTY_ID, newSelectColumnGenerator());
			columnGeneratorsAdded = true;

			setMultiSelect(true);
			addSelectionChangeListener(selectionManager = newSelectionManager());
			setColumnHeader(SELECT_PROPERTY_ID, "");
			setColumnWidth(SELECT_PROPERTY_ID, SELECT_PROPERTY_WIDTH);
			setColumnCollapsible(SELECT_PROPERTY_ID, false);
		}

		if (isIndexColumn()) {
			addGeneratedColumn(INDEX_PROPERTY_ID, newIndexColumnGenerator());
			columnGeneratorsAdded = true;

			setColumnHeader(INDEX_PROPERTY_ID, "#");
			setColumnWidth(INDEX_PROPERTY_ID, INDEX_PROPERTY_WIDTH);
			setColumnCollapsible(INDEX_PROPERTY_ID, false);
		}
		
		addAttachListener(new AttachListener() {
			@Override
			public void attach(AttachEvent event) {
				if (isMenuBarColumn() && getColumnGenerator(MENUBAR_PROPERTY_ID) == null) {
					addGeneratedColumn(MENUBAR_PROPERTY_ID, newMenuBarColumnGenerator());
					columnGeneratorsAdded = true;

					setColumnHeader(MENUBAR_PROPERTY_ID, "");
					setColumnWidth(MENUBAR_PROPERTY_ID, MENUBAR_PROPERTY_WIDTH);
					setColumnCollapsible(MENUBAR_PROPERTY_ID, false);
				}

				if (isContextMenuPossible() && contextMenu == null) {
					contextMenu = newContextMenu();
					if (contextMenu != null) {
						contextMenu.setAsContextMenuOf(BaseTable.this);
						BaseContextMenuTableListener contextMenuTableListener = new BaseContextMenuTableListener() {
							@Override
							public void onContextMenuOpenFromFooter(ContextMenuOpenedOnTableFooterEvent event) {
							}

							@Override
							public void onContextMenuOpenFromHeader(ContextMenuOpenedOnTableHeaderEvent event) {
							}

							@Override
							public void onContextMenuOpenFromRow(ContextMenuOpenedOnTableRowEvent event) {
								Object itemId = event.getItemId();
								contextMenuOpened(BaseTable.this.contextMenu, itemId);
							}
						};
						contextMenu.addContextMenuTableListener(contextMenuTableListener);
					}
				}

				scrollToFirstPagingItem();
				
				String tableId = getTableId();
				if (tableId != null && columnsManager == null) {
					columnsManager = newColumnsManager();
					manageColumns(tableId);
				}
			}
		});
	}

	// Wait until attach to set to make sure that the page length hasn't changed
	private Integer pagingCurrentPageFirstItemIndex;
	private Object pagingCurrentPageFirstItemId;

	private void scrollToFirstPagingItem() {
		if (isPaged()) {
			if (pagingCurrentPageFirstItemId != null && getCurrentPageFirstItemId() != pagingCurrentPageFirstItemId) {
				int indexOfItemId = pagedTableContainer.getContainer().indexOfId(pagingCurrentPageFirstItemId);
				int currentPage = getCurrentPage();
				int pageOfIndex = getPageOfIndex(indexOfItemId);
				if (currentPage != pageOfIndex) {
					setCurrentPage(pageOfIndex);
				}
				super.setCurrentPageFirstItemId(pagingCurrentPageFirstItemId);
			} else if (pagingCurrentPageFirstItemIndex != null && getCurrentPageFirstItemIndex() != pagingCurrentPageFirstItemIndex) {
				int currentPage = getCurrentPage();
				int pageOfIndex = getPageOfIndex(pagingCurrentPageFirstItemIndex);
				if (currentPage != pageOfIndex) {
					setCurrentPage(pageOfIndex);
				}
				super.setCurrentPageFirstItemIndex(pagingCurrentPageFirstItemIndex);
			}
		}
	}

	@Override
	public void setCurrentPageFirstItemId(Object currentPageFirstItemId) {
		if (isPaged()) {
			pagingCurrentPageFirstItemId = currentPageFirstItemId;
			if (isAttached()) {
				scrollToFirstPagingItem();
			}
		} else {
			super.setCurrentPageFirstItemId(currentPageFirstItemId);
		}
	}

	@Override
	public void setCurrentPageFirstItemIndex(int newIndex) {
		if (isPaged()) {
			pagingCurrentPageFirstItemIndex = newIndex;
			if (isAttached()) {
				scrollToFirstPagingItem();
			}
		} else {
			super.setCurrentPageFirstItemIndex(newIndex);
		}
	}

	public boolean isSelectColumn() {
		return false;
	}

	protected ColumnGenerator newSelectColumnGenerator() {
		return new ColumnGenerator() {
			@Override
			public Object generateCell(Table source, Object itemId, Object columnId) {
				Property<?> containerProperty;
				CellKey cellKey = getCellKey(itemId, SELECT_PROPERTY_ID);
				if (cellKey != null) {
					containerProperty = cellProperties.get(cellKey);
					if (containerProperty == null) {
						containerProperty = newSelectionCheckBox(itemId);
						cellProperties.put(cellKey, containerProperty);
					}
				} else {
					containerProperty = newSelectionCheckBox(itemId);
				}
				return containerProperty;
			}
		};
	}

	public boolean isIndexColumn() {
		return false;
	}

	protected ColumnGenerator newIndexColumnGenerator() {
		return new ColumnGenerator() {
			@Override
			public Object generateCell(Table source, Object itemId, Object columnId) {
				Property<?> containerProperty;
				CellKey cellKey = getCellKey(itemId, INDEX_PROPERTY_ID);
				int index = indexOfId(itemId) + 1;
				if (cellKey != null) {
					containerProperty = cellProperties.get(cellKey);
					if (containerProperty == null) {
						containerProperty = new ObjectProperty<>(index);
						cellProperties.put(cellKey, new Label(index + ""));
					}
				} else {
					containerProperty = new ObjectProperty<>(new Label(index + ""));
				}
				return containerProperty;
			}
		};
	}

	public boolean isMenuBarColumn() {
		return false;
	}

	protected ColumnGenerator newMenuBarColumnGenerator() {
		return new ColumnGenerator() {
			@Override
			public Object generateCell(Table source, Object itemId, Object columnId) {
				Component cellContent;
				CellKey cellKey = new CellKey(itemId, columnId);
				Property<?> cellProperty = cellProperties.get(cellKey);
				if (cellProperty != null) {
					cellContent = (Component) cellProperty.getValue();
				} else {
					MenuBar menuBar = newMenuBar(itemId);
					if (menuBar == null) {
						cellContent = new Label("");
					} else {
						cellContent = menuBar;
					}
					cellProperties.put(cellKey, new ObjectProperty<Object>(cellContent));
				}
				return cellContent;
			}
		};
	}

	protected MenuBar newMenuBar(Object itemId) {
		return null;
	}

	public boolean isContextMenuPossible() {
		return false;
	}

	protected ContextMenu getContextMenu() {
		return contextMenu;
	}

	protected ContextMenu newContextMenu() {
		return null;
	}

	protected void contextMenuOpened(ContextMenu contextMenu, Object itemId) {
	}

	protected SelectionManager newSelectionManager() {
		return new ValueSelectionManager() {
			@Override
			protected Object getValue() {
				return BaseTable.this.getValue();
			}

			@Override
			protected void setValue(Object value) {
				BaseTable.this.setValue(value);
			}

			@Override
			protected Collection<?> getItemIds() {
				return BaseTable.this.getItemIds();
			}
		};
	}

	public SelectionManager getSelectionManager() {
		return selectionManager;
	}

	protected void manageColumns(String tableId) {
		if (columnsManager == null) {
			columnsManager = newColumnsManager();
		}
		columnsManager.manage(BaseTable.this, tableId);
	}

	protected TableColumnsManager newColumnsManager() {
		return new TableColumnsManager();
	}

	protected String getTableId() {
		return tableId;
	}

	protected CellKey getCellKey(Object itemId, Object propertyId) {
		return null;
	}

	@Override
	public void setVisibleColumns(Object... visibleColumns) {
		if ((isSelectColumn() || isIndexColumn()) && columnGeneratorsAdded) {
			List<Object> visibleColumnsList = new ArrayList<>(Arrays.asList(visibleColumns));
			if (isIndexColumn() && (!visibleColumnsList.contains(INDEX_PROPERTY_ID) || visibleColumnsList.get(0) != INDEX_PROPERTY_ID)) {
				visibleColumnsList.remove(INDEX_PROPERTY_ID);
				visibleColumnsList.add(0, INDEX_PROPERTY_ID);
			}
			if (isSelectColumn() && (!visibleColumnsList.contains(SELECT_PROPERTY_ID) || visibleColumnsList.get(0) != SELECT_PROPERTY_ID)) {
				visibleColumnsList.remove(SELECT_PROPERTY_ID);
				visibleColumnsList.add(0, SELECT_PROPERTY_ID);
			}
			super.setVisibleColumns(visibleColumnsList.toArray(new Object[0]));
		} else {
			super.setVisibleColumns(visibleColumns);
		}
	}

	public boolean isPaged() {
		return false;
	}

	protected CheckBox newSelectionCheckBox(Object itemId) {
		return new SelectionCheckBox(itemId);
	}

	public void select(Object itemId) {
		SelectionChangeEvent event = new SelectionChangeEvent();
		event.setSelectedItemId(itemId);
		fireSelectionChangeEvent(event);
	}

	public void deselect(Object itemId) {
		SelectionChangeEvent event = new SelectionChangeEvent();
		event.setDeselectedItemId(itemId);
		fireSelectionChangeEvent(event);
	}

	public void selectAll() {
		SelectionChangeEvent event = new SelectionChangeEvent();
		event.setAllItemsSelected(true);
		fireSelectionChangeEvent(event);
	}

	public void deselectAll() {
		SelectionChangeEvent event = new SelectionChangeEvent();
		event.setAllItemsDeselected(true);
		fireSelectionChangeEvent(event);
	}

	public void selectCurrentPage() {
		if (isPaged()) {
			List<?> itemIds = pagedTableContainer.getItemIds((getCurrentPage() - 1) * getPageLength(), getPageLength());
			for (Object itemId : itemIds) {
				select(itemId);
			}
		} else {
			selectAll();
		}
	}

	public void deselectCurrentPage() {
		if (isPaged()) {
			List<?> itemIds = pagedTableContainer.getItemIds((getCurrentPage() - 1) * getPageLength(), getPageLength());
			for (Object itemId : itemIds) {
				deselect(itemId);
			}
		} else {
			deselectAll();
		}
	}

	public List<SelectionChangeListener> getSelectionChangeListeners() {
		return selectionChangeListeners;
	}

	public void addSelectionChangeListener(SelectionChangeListener listener) {
		if (!selectionChangeListeners.contains(listener)) {
			selectionChangeListeners.add(listener);
		}
	}

	public void removeSelectionChangeListener(SelectionChangeListener listener) {
		selectionChangeListeners.remove(listener);
	}

	protected void fireSelectionChangeEvent(SelectionChangeEvent event) {
		for (SelectionChangeListener listener : selectionChangeListeners) {
			listener.selectionChanged(event);
		}
	}

	public SelectDeselectAllButton newSelectDeselectAllToggleButton() {
		return newSelectDeselectAllToggleButton($("selectAll"), $("deselectAll"));
	}

	public SelectDeselectAllButton newSelectDeselectAllToggleButton(String selectAllCaption,
																	String deselectAllCaption) {
		final SelectDeselectAllButton toggleButton =
				new MaxLengthSelectDeselectAllButton(selectAllCaption, deselectAllCaption, !selectionManager.isAllItemsSelected());
		addSelectionChangeListener(new SelectionChangeListener() {
			@Override
			public void selectionChanged(SelectionChangeEvent event) {
				if (event.getComponent() != toggleButton) {
					if (event.isAllItemsSelected()) {
						toggleButton.setSelectAllMode(true);
					} else if (event.isAllItemsDeselected()) {
						toggleButton.setSelectAllMode(false);
					}
				}
			}
		});
		return toggleButton;
	}

	@Override
	public void containerItemSetChange(Container.ItemSetChangeEvent event) {
		cellProperties.clear();
		clearCheckBoxSelectionChangeListeners();
		super.containerItemSetChange(event);
	}

	private void clearCheckBoxSelectionChangeListeners() {
		for (SelectionChangeListener listener : new ArrayList<>(selectionChangeListeners)) {
			if (listener instanceof SelectionCheckBox.CheckBoxSelectionChangeListener) {
				removeSelectionChangeListener(listener);
			}
		}
	}

	@Override
	public final Property<?> getContainerProperty(Object itemId, Object propertyId) {
		Property<?> containerProperty;
		CellKey cellKey = getCellKey(itemId, propertyId);
		if (cellKey != null) {
			containerProperty = cellProperties.get(cellKey);
			if (containerProperty == null) {
				containerProperty = loadContainerProperty(itemId, propertyId);
				cellProperties.put(cellKey, containerProperty);
			}
		} else {
			containerProperty = loadContainerProperty(itemId, propertyId);
		}
		return containerProperty;
	}

	protected Property<?> loadContainerProperty(final Object itemId, final Object propertyId) {
		return super.getContainerProperty(itemId, propertyId);
	}

	@Override
	public void resetPageBuffer() {
		super.resetPageBuffer();
	}

	@Override
	public void refreshRenderedCells() {
		super.refreshRenderedCells();
	}

	@Override
	public void enableContentRefreshing(boolean refreshContent) {
		super.enableContentRefreshing(refreshContent);
	}

	@Override
	public void setContainerDataSource(Container newDataSource) {
		if (isPaged()) {
			if (!(newDataSource instanceof Container.Indexed)) {
				throw new IllegalArgumentException(
						"PagedTable can only use containers that implement Container.Indexed");
			}
			PagedTableContainer pagedTableContainer = new PagedBaseTableContainer(
					(Container.Indexed) newDataSource);
			pagedTableContainer.setPageLength(getPageLength());
			super.setContainerDataSource(pagedTableContainer);
			this.pagedTableContainer = pagedTableContainer;
			firePagedChangedEvent();
		} else {
			super.setContainerDataSource(newDataSource);
		}
	}

	private void setPageFirstIndex(int firstIndex) {
		if (pagedTableContainer != null) {
			if (firstIndex <= 0) {
				firstIndex = 0;
			}
			if (firstIndex > pagedTableContainer.getRealSize() - 1) {
				int size = pagedTableContainer.getRealSize() - 1;
				int pages = 0;
				if (getPageLength() != 0) {
					pages = (int) Math.floor(0.0 + size / getPageLength());
				}
				firstIndex = pages * getPageLength();
			}
			pagedTableContainer.setStartIndex(firstIndex);
			setCurrentPageFirstItemIndex(firstIndex);
			containerItemSetChange(new Container.ItemSetChangeEvent() {
				private static final long serialVersionUID = -5083660879306951876L;

				public Container getContainer() {
					return pagedTableContainer;
				}
			});
			if (alwaysRecalculateColumnWidths) {
				for (Object columnId : pagedTableContainer.getContainerPropertyIds()) {
					setColumnWidth(columnId, -1);
				}
			}
			firePagedChangedEvent();
		}
	}

	private void firePagedChangedEvent() {
		if (pageChangeListeners != null) {
			PagedTableChangeEvent event = new PagedTableChangeEvent();
			for (PageChangeListener listener : pageChangeListeners) {
				listener.pageChanged(event);
			}
		}
	}

	@Override
	public void setPageLength(int pageLength) {
		if (isPaged()) {
			if (pageLength >= 0 && getPageLength() != pageLength) {
				pagedTableContainer.setPageLength(pageLength);
				super.setPageLength(pageLength);
				firePagedChangedEvent();
			}
		} else {
			super.setPageLength(pageLength);
		}
	}

	public void nextPage() {
		setPageFirstIndex(pagedTableContainer.getStartIndex() + getPageLength());
	}

	public void previousPage() {
		setPageFirstIndex(pagedTableContainer.getStartIndex() - getPageLength());
	}

	public int getPageOfIndex(int index) {
		int pageOfIndex;
		if (index == 0) {
			pageOfIndex = 1;
		} else if (isPaged()) {
			double pageLength = getPageLength();
			pageOfIndex = (int) (index / pageLength) + 1;
		} else {
			pageOfIndex = 1;
		}
		return pageOfIndex;
	}

	public int getCurrentPage() {
		double pageLength = getPageLength();
		int page = (int) Math.floor((double) pagedTableContainer.getStartIndex()
									/ pageLength) + 1;
		if (page < 1) {
			page = 1;
		}
		return page;
	}

	public void setCurrentPage(int page) {
		int newIndex = (page - 1) * getPageLength();
		if (newIndex < 0) {
			newIndex = 0;
		}
		if (newIndex >= 0 && newIndex != pagedTableContainer.getStartIndex()) {
			setPageFirstIndex(newIndex);
		}
	}

	public int getTotalAmountOfPages() {
		int size = pagedTableContainer.getContainer().size();
		double pageLength = getPageLength();
		int pageCount = (int) Math.ceil(size / pageLength);
		if (pageCount < 1) {
			pageCount = 1;
		}
		return pageCount;
	}

	public List<PageChangeListener> getPageChangeListeners() {
		return pageChangeListeners;
	}

	public void addPageChangeListener(PageChangeListener listener) {
		if (!pageChangeListeners.contains(listener)) {
			pageChangeListeners.add(listener);
		}
	}

	public void removePageChangeListener(PageChangeListener listener) {
		pageChangeListeners.remove(listener);
	}

	public void setAlwaysRecalculateColumnWidths(
			boolean alwaysRecalculateColumnWidths) {
		this.alwaysRecalculateColumnWidths = alwaysRecalculateColumnWidths;
	}

	public PagingControls createPagingControls() {
		return new PagingControls();
	}

	protected void onPreviousPageButtonClicked() {
		previousPage();
	}

	protected void onNextPageButtonClicked() {
		nextPage();
	}

	protected void onSetPageButtonClicked(int page) {
		setCurrentPage(page);
	}

	private static class PagedBaseTableContainer extends PagedTableContainer implements ItemSetChangeNotifier {

		public PagedBaseTableContainer(Indexed container) {
			super(container);
		}

		@Override
		public void addItemSetChangeListener(ItemSetChangeListener listener) {
			((ItemSetChangeNotifier) getContainer()).addItemSetChangeListener(listener);
		}

		@SuppressWarnings("deprecation")
		@Override
		public void addListener(ItemSetChangeListener listener) {
			((ItemSetChangeNotifier) getContainer()).addListener(listener);
		}

		@Override
		public void removeItemSetChangeListener(ItemSetChangeListener listener) {
			((ItemSetChangeNotifier) getContainer()).removeItemSetChangeListener(listener);
		}

		@SuppressWarnings("deprecation")
		@Override
		public void removeListener(ItemSetChangeListener listener) {
			((ItemSetChangeNotifier) getContainer()).removeListener(listener);
		}

		@Override
		public List<?> getItemIds(int startIndex, int numberOfItems) {
			return this.getContainer().getItemIds(startIndex, numberOfItems);
		}
	}

	private class SelectionCheckBox extends CheckBox {

		private Object itemId;

		private SelectionCheckBox(final Object itemId) {
			this.itemId = itemId;
			setValue(selectionManager.isSelected(itemId));
			addValueChangeListener(new ValueChangeListener() {
				@Override
				public void valueChange(com.vaadin.data.Property.ValueChangeEvent event) {
					SelectionChangeEvent selectionChangeEvent = new SelectionChangeEvent();
					selectionChangeEvent.setComponent(SelectionCheckBox.this);
					Boolean newValue = (Boolean) event.getProperty().getValue();
					if (newValue) {
						selectionChangeEvent.setSelectedItemId(itemId);
					} else {
						selectionChangeEvent.setDeselectedItemId(itemId);
					}
					fireSelectionChangeEvent(selectionChangeEvent);
				}
			});
			addSelectionChangeListener(new CheckBoxSelectionChangeListener());
		}

		@Override
		public Boolean getInternalValue() {
			return super.getInternalValue();
		}

		@Override
		public void setInternalValue(Boolean newValue) {
			super.setInternalValue(newValue);
		}

		private class CheckBoxSelectionChangeListener implements SelectionChangeListener {
			@Override
			public void selectionChanged(SelectionChangeEvent event) {
				if (event.getComponent() != SelectionCheckBox.this) {
					if (event.isAllItemsSelected() || (event.getSelectedItemIds() != null && event.getSelectedItemIds().contains(itemId))) {
						setInternalValue(true);
					} else if (event.isAllItemsDeselected() || (event.getDeselectedItemIds() != null && event.getDeselectedItemIds().contains(itemId))) {
						setInternalValue(false);
					}
				}
			}
		}

	}

	public static class SelectionChangeEvent {

		private Component component;

		private List<Object> selectedItemIds;

		private List<Object> deselectedItemIds;

		private boolean allItemsSelected;

		private boolean allItemsDeselected;

		public Component getComponent() {
			return component;
		}

		public void setComponent(Component component) {
			this.component = component;
		}

		public List<Object> getSelectedItemIds() {
			return selectedItemIds;
		}

		public void setSelectedItemIds(List<Object> selectedItemIds) {
			this.selectedItemIds = selectedItemIds;
		}

		@SuppressWarnings("unchecked")
		public void setSelectedItemId(Object selectedItemId) {
			if (selectedItemId instanceof List) {
				setSelectedItemIds((List<Object>) selectedItemId);
			} else if (selectedItemId != null) {
				setSelectedItemIds(Arrays.asList(selectedItemId));
			}
		}

		public List<Object> getDeselectedItemIds() {
			return deselectedItemIds;
		}

		public void setDeselectedItemIds(List<Object> deselectedItemIds) {
			this.deselectedItemIds = deselectedItemIds;
		}

		@SuppressWarnings("unchecked")
		public void setDeselectedItemId(Object deselectedItemId) {
			if (deselectedItemId instanceof List) {
				setDeselectedItemIds((List<Object>) deselectedItemId);
			} else if (deselectedItemId != null) {
				setDeselectedItemIds(Arrays.asList(deselectedItemId));
			}
		}

		public boolean isAllItemsSelected() {
			return allItemsSelected;
		}

		public void setAllItemsSelected(boolean allItemsSelected) {
			this.allItemsSelected = allItemsSelected;
		}

		public boolean isAllItemsDeselected() {
			return allItemsDeselected;
		}

		public void setAllItemsDeselected(boolean allItemsDeselected) {
			this.allItemsDeselected = allItemsDeselected;
		}

	}

	public static interface SelectionChangeListener {

		void selectionChanged(SelectionChangeEvent event);

	}

	public static interface SelectionManager extends SelectionChangeListener {

		List<Object> getAllSelectedItemIds();

		boolean isAllItemsSelected();

		boolean isAllItemsDeselected();

		boolean isSelected(Object itemId);

	}

	public static abstract class ValueSelectionManager implements SelectionManager {

		@SuppressWarnings({"rawtypes", "unchecked"})
		private List<Object> ensureListValue() {
			List<Object> listValue;
			Object objectValue = getValue();
			if (objectValue instanceof List) {
				listValue = (List) objectValue;
			} else {
				listValue = new ArrayList<>();
			}
			return listValue;
		}

		@Override
		public List<Object> getAllSelectedItemIds() {
			List<Object> allSelectedItemIds;
			if (isAllItemsSelected()) {
				allSelectedItemIds = new ArrayList<>(getItemIds());
			} else {
				allSelectedItemIds = ensureListValue();
			}
			return allSelectedItemIds;
		}

		@Override
		public boolean isAllItemsSelected() {
			List<Object> listValue = ensureListValue();
			return listValue.containsAll(getItemIds());
		}

		@Override
		public boolean isAllItemsDeselected() {
			List<Object> listValue = ensureListValue();
			return listValue.isEmpty();
		}

		@Override
		public boolean isSelected(Object itemId) {
			List<Object> listValue = ensureListValue();
			return listValue.contains(itemId);
		}

		@Override
		public void selectionChanged(SelectionChangeEvent event) {
			if (event.isAllItemsSelected()) {
				setValue(getItemIds());
			} else if (event.isAllItemsDeselected()) {
				setValue(new ArrayList<>());
			} else {
				List<Object> selectedItemIds = event.getSelectedItemIds();
				List<Object> deselectedItemIds = event.getDeselectedItemIds();
				List<Object> listValue = ensureListValue();
				if (selectedItemIds != null) {
					for (Object selectedItemId : selectedItemIds) {
						if (!listValue.contains(selectedItemId)) {
							listValue.add(selectedItemId);
						}
					}
				} else if (deselectedItemIds != null) {
					for (Object deselectedItemId : deselectedItemIds) {
						listValue.remove(deselectedItemId);
					}
				}
				setValue(listValue);
			}
		}

		protected abstract Object getValue();

		protected abstract void setValue(Object newValue);

		protected abstract Collection<?> getItemIds();

	}

	public static interface PageChangeListener {
		public void pageChanged(PagedTableChangeEvent event);
	}

	public class PagedTableChangeEvent {

		public BaseTable getTable() {
			return BaseTable.this;
		}

		public int getCurrentPage() {
			return BaseTable.this.getCurrentPage();
		}

		public int getTotalAmountOfPages() {
			return BaseTable.this.getTotalAmountOfPages();
		}
	}

	public class PagingControls extends I18NHorizontalLayout {

		private int itemsPerPageValue = getPageLength();

		private ComboBox itemsPerPageField;
		private Label itemsPerPageLabel;
		private HorizontalLayout pageSizeLayout;
		private Label currentPageLabel;
		private TextField currentPageField;
		private Label separator;
		private Label totalPagesLabel;
		private Button firstPageButton;
		private Button previousPageButton;
		private Button nextPageButton;
		private Button lastPageButton;
		private HorizontalLayout pageManagementLayout;

		public PagingControls() {
			if (isPaged()) {
				itemsPerPageField = new BaseComboBox();
				itemsPerPageField.setValue(itemsPerPageValue);

				int totalAmountOfPages = getTotalAmountOfPages();

				itemsPerPageLabel = new Label($("SearchResultTable.itemsPerPage"));
				itemsPerPageField.addItem(DEFAULT_PAGE_LENGTH);

				if (pagedTableContainer.getRealSize() >= 10) {
					itemsPerPageField.addItem(10);
				}
				if (pagedTableContainer.getRealSize() > 10) {
					itemsPerPageField.addItem(25);
				}
				if (pagedTableContainer.getRealSize() > 25) {
					itemsPerPageField.addItem(50);
				}
				if (pagedTableContainer.getRealSize() > 50) {
					itemsPerPageField.addItem(100);
				}
				itemsPerPageField.setNullSelectionAllowed(false);
				itemsPerPageField.setWidth("85px");

				itemsPerPageField.addValueChangeListener(new ValueChangeListener() {
					@Override
					public void valueChange(Property.ValueChangeEvent event) {
						setPageLength((int) itemsPerPageField.getValue());
					}
				});
				itemsPerPageField.setEnabled(itemsPerPageField.size() > 1);

				pageSizeLayout = new I18NHorizontalLayout(itemsPerPageLabel, itemsPerPageField);
				pageSizeLayout.setComponentAlignment(itemsPerPageLabel, Alignment.MIDDLE_LEFT);
				pageSizeLayout.setComponentAlignment(itemsPerPageField, Alignment.MIDDLE_LEFT);
				pageSizeLayout.setSpacing(true);

				currentPageLabel = new Label($("SearchResultTable.page"));
				currentPageField = new TextField();
				currentPageField.setConverter(Integer.class);
				currentPageField.setConvertedValue(getCurrentPage());
				currentPageField.setWidth("45px");
				currentPageField.addValueChangeListener(new ValueChangeListener() {
					@Override
					public void valueChange(Property.ValueChangeEvent event) {
						boolean valid;
						String newValue = currentPageField.getValue();
						if (StringUtils.isNotBlank(newValue)) {
							try {
								int newIntValue = Integer.parseInt(newValue);
								valid = newIntValue > 0 && newIntValue < getTotalAmountOfPages();
							} catch (NumberFormatException e) {
								valid = false;
							}
						} else {
							valid = false;
						}
						if (valid) {
							setCurrentPage((int) currentPageField.getConvertedValue());
						}
					}
				});
				currentPageField.setEnabled(totalAmountOfPages > 1);

				separator = new Label($("SearchResultTable.of"));
				totalPagesLabel = new Label(String.valueOf(totalAmountOfPages));

				firstPageButton = new Button("\uF100", new ClickListener() {
					public void buttonClick(ClickEvent event) {
						onSetPageButtonClicked(0);
					}
				});
				firstPageButton.setStyleName(ValoTheme.BUTTON_LINK);
				firstPageButton.setEnabled(getCurrentPage() > 1);

				previousPageButton = new Button("\uF104", new ClickListener() {
					public void buttonClick(ClickEvent event) {
						onPreviousPageButtonClicked();
					}
				});
				previousPageButton.setStyleName(ValoTheme.BUTTON_LINK);
				previousPageButton.setEnabled(getCurrentPage() > 1);

				nextPageButton = new Button("\uF105", new ClickListener() {
					public void buttonClick(ClickEvent event) {
						onNextPageButtonClicked();
					}
				});
				nextPageButton.setStyleName(ValoTheme.BUTTON_LINK);
				nextPageButton.setEnabled(getCurrentPage() < getTotalAmountOfPages());

				lastPageButton = new Button("\uF101", new ClickListener() {
					public void buttonClick(ClickEvent event) {
						onSetPageButtonClicked(getTotalAmountOfPages());
					}
				});
				lastPageButton.setStyleName(ValoTheme.BUTTON_LINK);
				lastPageButton.setEnabled(getCurrentPage() < getTotalAmountOfPages());

				if (isRightToLeft()) {
					String rtlFirstCaption = lastPageButton.getCaption();
					String rtlPreviousCaption = nextPageButton.getCaption();
					String rtlNextCaption = previousPageButton.getCaption();
					String rtlLastCaption = firstPageButton.getCaption();
					firstPageButton.setCaption(rtlFirstCaption);
					previousPageButton.setCaption(rtlPreviousCaption);
					nextPageButton.setCaption(rtlNextCaption);
					lastPageButton.setCaption(rtlLastCaption);
				}

				pageManagementLayout = new I18NHorizontalLayout(
						firstPageButton, previousPageButton, currentPageLabel, currentPageField, separator, totalPagesLabel, nextPageButton, lastPageButton);
				pageManagementLayout.setComponentAlignment(firstPageButton, Alignment.MIDDLE_LEFT);
				pageManagementLayout.setComponentAlignment(previousPageButton, Alignment.MIDDLE_LEFT);
				pageManagementLayout.setComponentAlignment(currentPageLabel, Alignment.MIDDLE_LEFT);
				pageManagementLayout.setComponentAlignment(currentPageField, Alignment.MIDDLE_LEFT);
				pageManagementLayout.setComponentAlignment(separator, Alignment.MIDDLE_LEFT);
				pageManagementLayout.setComponentAlignment(totalPagesLabel, Alignment.MIDDLE_LEFT);
				pageManagementLayout.setComponentAlignment(nextPageButton, Alignment.MIDDLE_LEFT);
				pageManagementLayout.setComponentAlignment(lastPageButton, Alignment.MIDDLE_LEFT);
				pageManagementLayout.setSpacing(true);

				addComponents(pageSizeLayout, pageManagementLayout);
				setComponentAlignment(pageManagementLayout, Alignment.MIDDLE_CENTER);
				setExpandRatio(pageSizeLayout, 1);
				setWidth("100%");

				addPageChangeListener(new PageChangeListener() {
					public void pageChanged(PagedTableChangeEvent event) {
						firstPageButton.setEnabled(getCurrentPage() > 1);
						previousPageButton.setEnabled(getCurrentPage() > 1);
						nextPageButton.setEnabled(getCurrentPage() < getTotalAmountOfPages());
						lastPageButton.setEnabled(getCurrentPage() < getTotalAmountOfPages());
						currentPageField.setValue(String.valueOf(getCurrentPage()));
						currentPageField.setEnabled(getTotalAmountOfPages() > 1);
						totalPagesLabel.setValue(String.valueOf(getTotalAmountOfPages()));
					}
				});
			} else {
				setVisible(false);
			}
		}

		public void setItemsPerPageValue(int value) {
			this.itemsPerPageValue = value;
			if (itemsPerPageField != null) {
				itemsPerPageField.setValue(value);
			}
		}

	}

	public class MaxLengthSelectDeselectAllButton extends SelectDeselectAllButton {

		private int rangeStart = -1;
		private int rangeEnd = -1;

		@PropertyId("rangeStart")
		private BaseIntegerField rangeStartField;
		@PropertyId("rangeEnd")
		private BaseIntegerField rangeEndField;

		private MaxLengthSelectDeselectAllButton(String selectAllCaption, String deselectAllCaption,
												 boolean selectAllMode) {
			super(selectAllCaption, deselectAllCaption, selectAllMode);
		}

		public int getRangeStart() {
			return rangeStart;
		}

		public void setRangeStart(int rangeStart) {
			this.rangeStart = rangeStart;
		}

		public int getRangeEnd() {
			return rangeEnd;
		}

		public void setRangeEnd(int rangeEnd) {
			this.rangeEnd = rangeEnd;
		}

		@Override
		protected void onSelectAll(ClickEvent event) {
			SelectionChangeEvent selectionChangeEvent = new SelectionChangeEvent();
			selectionChangeEvent.setComponent(this);
			selectionChangeEvent.setAllItemsSelected(true);
			fireSelectionChangeEvent(selectionChangeEvent);
		}

		@Override
		protected void onDeselectAll(ClickEvent event) {
			SelectionChangeEvent selectionChangeEvent = new SelectionChangeEvent();
			selectionChangeEvent.setComponent(this);
			selectionChangeEvent.setAllItemsDeselected(true);
			fireSelectionChangeEvent(selectionChangeEvent);
		}

		@Override
		protected void buttonClickCallBack(boolean selectAllMode) {
		}

		@SuppressWarnings({"rawtypes", "unchecked"})
		@Override
		protected void buttonClick(ClickEvent event) {
			int realSize;
			if (pagedTableContainer != null) {
				realSize = pagedTableContainer.getRealSize();
			} else {
				realSize = size();
			}
			if (realSize <= MAX_SELECTION_LENGTH) {
				super.buttonClick(event);
			} else {
				if (rangeStart == -1) {
					rangeStart = 1;
				}
				if (rangeEnd == -1 || rangeEnd > (rangeStart + (MAX_SELECTION_LENGTH - 1))) {
					rangeEnd = rangeStart + (MAX_SELECTION_LENGTH - 1);
					if (rangeEnd > size() - 1) {
						rangeEnd = size() - 1;
					}
				}

				final BaseWindow selectionRangeWindow = new BaseWindow($("BaseTable.selection.range"));
				selectionRangeWindow.setModal(true);
				selectionRangeWindow.center();
				selectionRangeWindow.setWidth("500px");

				VerticalLayout selectionRangeLayout = new VerticalLayout();
				selectionRangeLayout.addStyleName("selection-range-layout");
				selectionRangeLayout.setSpacing(true);

				rangeStartField = new BaseIntegerField($("BaseTable.selection.rangeStart"));
				rangeStartField.addStyleName("selection-start-start");
				rangeStartField.addValueChangeListener(new ValueChangeListener() {
					@Override
					public void valueChange(com.vaadin.data.Property.ValueChangeEvent event) {
						try {
							Integer newRangeStart = (Integer) rangeStartField.getConvertedValue();
							Integer newRangeEnd = newRangeStart + (MAX_SELECTION_LENGTH - 1);
							if (newRangeEnd > size() - 1) {
								newRangeEnd = size() - 1;
							}
							rangeEndField.setValue("" + newRangeEnd);
						} catch (ConversionException e) {
							// Ignore
						}
					}
				});

				rangeEndField = new BaseIntegerField($("BaseTable.selection.rangeEnd"));
				rangeEndField.addStyleName("selection-range-end");

				Button deselectAllButton = new BaseButton($("deselectAll")) {
					@Override
					protected void buttonClick(ClickEvent event) {
						SelectionChangeEvent deselectAllEvent = new SelectionChangeEvent();
						deselectAllEvent.setAllItemsDeselected(true);
						fireSelectionChangeEvent(deselectAllEvent);
						selectionRangeWindow.close();
					}
				};
				deselectAllButton.addStyleName(ValoTheme.BUTTON_LINK);
				deselectAllButton.addStyleName("selection-range-deselect-all-button");
				deselectAllButton.setVisible(!selectionManager.getAllSelectedItemIds().isEmpty());
				if (deselectAllButton.isVisible()) {
					selectionRangeWindow.setHeight("300px");
				} else {
					selectionRangeWindow.setHeight("260px");
				}

				BaseForm rangeForm = new BaseForm(this, this, rangeStartField, rangeEndField) {
					@Override
					protected void saveButtonClick(Object viewObject) throws ValidationException {
						int realRangeStart = rangeStart - 1;
						int realRangeEnd = rangeEnd - 1;
						int realSize;
						if (pagedTableContainer != null) {
							realSize = pagedTableContainer.getRealSize();
						} else {
							realSize = size();
						}
						ValidationErrors errors = new ValidationErrors();
						if (realRangeStart < 0) {
							Map<String, Object> parameters = new HashMap<>();
							parameters.put("rangeStart", rangeStart);
							errors.add(BaseTable.class, "rangeStartMustBePositive", parameters);
						} else if (realRangeEnd > (realRangeStart + MAX_SELECTION_LENGTH - 1) || realRangeEnd > realSize - 1 || realRangeEnd < realRangeStart) {
							int rangeEndMin = rangeStart;
							int rangeEndMax = rangeStart + MAX_SELECTION_LENGTH - 1;
							if (rangeEndMax > realSize) {
								rangeEndMax = realSize;
							}
							Map<String, Object> parameters = new HashMap<>();
							parameters.put("rangeEnd", rangeEnd);
							parameters.put("minValue", rangeEndMin);
							parameters.put("maxValue", rangeEndMax);
							errors.add(BaseTable.class, "rangeEndMustBeBetween", parameters);
						}
						if (errors.isEmpty()) {
							SelectionChangeEvent deselectAllEvent = new SelectionChangeEvent();
							deselectAllEvent.setAllItemsDeselected(true);
							fireSelectionChangeEvent(deselectAllEvent);

							List<Object> selectedItemIds = getItemIds(realRangeStart, realRangeEnd - realRangeStart + 1);
							SelectionChangeEvent newSelectionEvent = new SelectionChangeEvent();
							newSelectionEvent.setSelectedItemIds(selectedItemIds);
							fireSelectionChangeEvent(newSelectionEvent);
							selectionRangeWindow.close();
						} else {
							throw new ValidationException(errors);
						}
					}

					@Override
					protected void cancelButtonClick(Object viewObject) {
						selectionRangeWindow.close();
					}

					@Override
					protected String getSaveButtonCaption() {
						return $("select");
					}
				};

				selectionRangeLayout.addComponents(deselectAllButton, rangeForm);
				selectionRangeWindow.setContent(selectionRangeLayout);
				getUI().addWindow(selectionRangeWindow);
			}
		}

	}

}
