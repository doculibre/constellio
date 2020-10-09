package com.constellio.app.ui.framework.components.table;

import com.constellio.app.ui.application.ConstellioUI;
import com.constellio.app.ui.framework.buttons.BaseButton;
import com.constellio.app.ui.framework.buttons.SelectDeselectAllButton;
import com.constellio.app.ui.framework.components.BaseForm;
import com.constellio.app.ui.framework.components.BaseWindow;
import com.constellio.app.ui.framework.components.contextmenu.BaseContextMenuTableListener;
import com.constellio.app.ui.framework.components.fields.BaseComboBox;
import com.constellio.app.ui.framework.components.fields.number.BaseIntegerField;
import com.constellio.app.ui.framework.components.layouts.I18NHorizontalLayout;
import com.constellio.app.ui.framework.components.selection.SelectionComponent;
import com.constellio.app.ui.framework.components.table.TablePropertyCache.CellKey;
import com.constellio.app.ui.framework.components.table.columns.TableColumnsManager;
import com.constellio.app.ui.framework.containers.ContainerAdapter;
import com.constellio.app.ui.framework.containers.PreLoader;
import com.constellio.app.ui.framework.items.RecordVOItem;
import com.constellio.app.ui.util.ResponsiveUtils;
import com.constellio.model.frameworks.validation.ValidationErrors;
import com.constellio.model.frameworks.validation.ValidationException;
import com.constellio.model.services.factories.ModelLayerFactory;
import com.constellio.model.services.migrations.ConstellioEIMConfigs;
import com.vaadin.data.Container;
import com.vaadin.data.Item;
import com.vaadin.data.Property;
import com.vaadin.data.fieldgroup.PropertyId;
import com.vaadin.data.util.ObjectProperty;
import com.vaadin.data.util.converter.Converter.ConversionException;
import com.vaadin.server.Page;
import com.vaadin.server.Page.BrowserWindowResizeEvent;
import com.vaadin.server.Page.BrowserWindowResizeListener;
import com.vaadin.ui.Alignment;
import com.vaadin.ui.Button;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.Button.ClickListener;
import com.vaadin.ui.CheckBox;
import com.vaadin.ui.ComboBox;
import com.vaadin.ui.Component;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.JavaScript;
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
import static com.constellio.app.ui.pages.trash.TrashRecordsTable.TRASH_BUTTONS;

public class BaseTable extends Table implements SelectionComponent {

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

	private PagedBaseTableContainer pagedTableContainer;

	private List<PageChangeListener> pageChangeListeners = new ArrayList<>();

	private List<ItemsPerPageChangeListener> itemsPerPageChangeListeners = new ArrayList<>();

	private ContextMenu contextMenu;

	private int customPageLength = DEFAULT_PAGE_LENGTH;
	private PreLoader preloader;

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

	@Override
	public boolean isSelectable() {
		return false;
	}

	public boolean isUnknownEnd() {
		return false;
	}

	private void init() {
		addStyleName("base-table");
		if (isSelectColumn()) {
			columnGeneratorsAdded = true;

			if (addGeneratedSelectColumn()) {
				addGeneratedColumn(SELECT_PROPERTY_ID, newSelectColumnGenerator());

				setMultiSelect(true);
				addSelectionChangeListener(selectionManager = newSelectionManager());
				setColumnHeader(SELECT_PROPERTY_ID, "");
				setColumnWidth(SELECT_PROPERTY_ID, SELECT_PROPERTY_WIDTH);
				setColumnCollapsible(SELECT_PROPERTY_ID, false);
			}
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

	@Override
	public void setPageLength(int pageLength) {
		this.customPageLength = pageLength;
		super.setPageLength(pageLength);
	}

	// Wait until attach to set to make sure that the page length hasn't changed
	private Integer pagingCurrentPageFirstItemIndex;
	private Object pagingCurrentPageFirstItemId;

	private void scrollToFirstPagingItem() {
		if (isPaged()) {
			if (pagingCurrentPageFirstItemId != null && getCurrentPageFirstItemId() != pagingCurrentPageFirstItemId) {
				int indexOfItemId = ((Indexed) pagedTableContainer.getNestedContainer()).indexOfId(pagingCurrentPageFirstItemId);
				int currentPage = pagedTableContainer.getCurrentPage();
				int pageOfIndex = pagedTableContainer.getPageOfIndex(indexOfItemId);
				if (currentPage != pageOfIndex) {
					setCurrentPage(pageOfIndex);
				}
				super.setCurrentPageFirstItemId(pagingCurrentPageFirstItemId);
			} else if (pagingCurrentPageFirstItemIndex != null && getCurrentPageFirstItemIndex() != pagingCurrentPageFirstItemIndex) {
				int currentPage = pagedTableContainer.getCurrentPage();
				int pageOfIndex = pagedTableContainer.getPageOfIndex(pagingCurrentPageFirstItemIndex);
				if (currentPage != pageOfIndex) {
					setCurrentPage(pageOfIndex);
				}
				//				int adjustedFirstIndex = pagingCurrentPageFirstItemIndex - ((currentPage - 1) * getPageLength());
				super.setCurrentPageFirstItemIndex(pagingCurrentPageFirstItemIndex);
			} 
		}
	}

	protected void scrollToTop() {
		JavaScript.getCurrent().execute("document.getElementById('" + getId() + "').scrollIntoView();");
	}

	//	public void addRefreshRenderedCellsEventListener(RefreshRenderedCellsEvent refreshRenderedCellsEvent) {
	//		refreshRenderedCellsEventListenerList.add(refreshRenderedCellsEvent);
	//	}
	//
	//	public void fireAddRefreshRenderedCellsEvent(List<Object> selectedId, boolean areAllItemSelected) {
	//		if (refreshRenderedCellsEventListenerList == null) {
	//			return;
	//		}
	//
	//		for (RefreshRenderedCellsEvent currentRefreshRenderedCellsEvent : refreshRenderedCellsEventListenerList) {
	//			currentRefreshRenderedCellsEvent.refreshRenderedCellsEvent(new RefreshRenderedCellsEventParams(selectedId, areAllItemSelected));
	//		}
	//	}

	public void setCurrentPage(int currentPage) {
		if (isPaged()) {
			pagedTableContainer.setCurrentPage(currentPage);
			firePageChangedEvent();
		}
	}

	@Override
	public void refreshRenderedCells() {
		super.refreshRenderedCells();
	}

	public int getItemsPerPage() {
		return pagedTableContainer.getItemsPerPage();
	}

	public void setItemsPerPage(int itemsPerPage) {
		if (isPaged()) {
			pagedTableContainer.setItemsPerPage(itemsPerPage);
			adjustPageLengthBasedOnItemsPerPage();
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
			if (isAttached() && newIndex % getPageLength() != 0) {
				scrollToFirstPagingItem();
			}
		} else {
			super.setCurrentPageFirstItemIndex(newIndex);
		}
	}

	public boolean isSelectColumn() {
		return false;
	}

	public boolean addGeneratedSelectColumn() {
		return true;
	}

	public boolean isButtonsColumn() {
		return false;
	}

	protected ColumnGenerator newSelectColumnGenerator() {
		return new ColumnGenerator() {
			@Override
			public Object generateCell(Table source, Object itemId, Object columnId) {
				Property<?> containerProperty;
				Item item = getItem(itemId);

				if (item instanceof RecordVOItem) {
					RecordVOItem recordVOItem = (RecordVOItem) item;
					if (recordVOItem.getSearchResult() != null && recordVOItem.getSearchResult().isDeleted()) {
						CheckBox checkBox = newSelectionCheckBox(itemId);
						checkBox.setEnabled(false);
						return checkBox;
					}
				}

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
				return containerProperty.getValue();
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
		if ((isSelectColumn() || isIndexColumn() || isMenuBarColumn() || isButtonsColumn()) && columnGeneratorsAdded) {
			List<Object> visibleColumnsList = new ArrayList<>(Arrays.asList(visibleColumns));
			if (isIndexColumn() && visibleColumnsList.contains(INDEX_PROPERTY_ID)) {
				int columnIndex = isRightToLeft() ? columnIndex = visibleColumnsList.size() - 1 : 0;
				visibleColumnsList.remove(INDEX_PROPERTY_ID);
				visibleColumnsList.add(columnIndex, INDEX_PROPERTY_ID);
			}
			if (isSelectColumn() && visibleColumnsList.contains(SELECT_PROPERTY_ID)) {
				int columnIndex = isRightToLeft() ? columnIndex = visibleColumnsList.size() - 1 : 0;
				visibleColumnsList.remove(SELECT_PROPERTY_ID);
				visibleColumnsList.add(columnIndex, SELECT_PROPERTY_ID);
			}
			if (isMenuBarColumn() && isRightToLeft() && visibleColumnsList.contains(MENUBAR_PROPERTY_ID)) {
				visibleColumnsList.remove(MENUBAR_PROPERTY_ID);
				visibleColumnsList.add(0, MENUBAR_PROPERTY_ID);
			}
			if (isButtonsColumn() && visibleColumnsList.contains(TRASH_BUTTONS)) {
				int columnIndex = visibleColumnsList.size() - 1;
				visibleColumnsList.remove(TRASH_BUTTONS);
				visibleColumnsList.add(columnIndex, TRASH_BUTTONS);
			}
			if (isMenuBarColumn() && visibleColumnsList.contains(MENUBAR_PROPERTY_ID)) {
				int columnIndex = visibleColumnsList.size() - 1;
				visibleColumnsList.remove(MENUBAR_PROPERTY_ID);
				visibleColumnsList.add(columnIndex, MENUBAR_PROPERTY_ID);
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
			List<?> itemIds = pagedTableContainer.getCurrentPageItemIds();
			for (Object itemId : itemIds) {
				select(itemId);
			}
		} else {
			selectAll();
		}
	}

	public void deselectCurrentPage() {
		if (isPaged()) {
			List<?> itemIds = pagedTableContainer.getCurrentPageItemIds();
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
				new DefaultMaxLengthSelectDeselectAllButton(selectAllCaption, deselectAllCaption, !selectionManager.isAllItemsSelected());
		return toggleButton;
	}

	public SelectDeselectAllButton newDeselectAllButton(String deselectAllCaption, boolean initialyVisible) {
		SelectDeselectAllButton toggleButton =
				new DeselectAllButton(deselectAllCaption);

		addSelectionChangeListener(new SelectionChangeListener() {
			@Override
			public void selectionChanged(SelectionChangeEvent event) {
				if (event.isAllItemsDeselected()) {
					toggleButton.setVisible(false);
				} else {
					toggleButton.setVisible(true);
				}
			}
		});
		toggleButton.setVisible(initialyVisible);

		return toggleButton;
	}

	@Override
	public void containerItemSetChange(Container.ItemSetChangeEvent event) {
		cellProperties.clear();
		clearCheckBoxSelectionChangeListeners();
		super.containerItemSetChange(event);
		adjustPageLengthBasedOnItemsPerPage();
	}

	private void clearCheckBoxSelectionChangeListeners() {
		//		for (SelectionChangeListener listener : new ArrayList<>(selectionChangeListeners)) {
		//			if (listener instanceof SelectionCheckBox) {
		//				removeSelectionChangeListener(listener);
		//			}
		//		}
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
			int itemsPerPage = getPageLength();
			PagedBaseTableContainer pagedTableContainer = new PagedBaseTableContainer((Container.Indexed) newDataSource, itemsPerPage);
			super.setContainerDataSource(pagedTableContainer);
			this.pagedTableContainer = pagedTableContainer;
			firePageChangedEvent();
		} else {
			super.setContainerDataSource(newDataSource);
			adjustPageLengthBasedOnItemsPerPage();
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
			firePageChangedEvent();
		}
	}

	private void firePageChangedEvent() {
		if (isPaged() && pageChangeListeners != null) {
			int currentPage = pagedTableContainer.getCurrentPage();
			int numberOfPages = pagedTableContainer.getNumberOfPages();
			PageChangeEvent event = new PageChangeEvent(currentPage, numberOfPages);
			for (PageChangeListener listener : pageChangeListeners) {
				listener.pageChanged(event);
			}
			scrollToTop();
		}
	}

	private void fireItemsPerPageChangedEvent(int oldItemsPerPage, int newItemsPerPage) {
		if (isPaged() && itemsPerPageChangeListeners != null) {
			ItemsPerPageChangeEvent event = new ItemsPerPageChangeEvent(oldItemsPerPage, newItemsPerPage);
			for (ItemsPerPageChangeListener listener : itemsPerPageChangeListeners) {
				listener.itemsPerPageChanged(event);
			}
		}
	}

	public void nextPage() {
		int currentPage = pagedTableContainer.getCurrentPage();
		int newPage = currentPage + 1;
		pagedTableContainer.setCurrentPage(newPage);
		adjustPageLengthBasedOnItemsPerPage();
		firePageChangedEvent();
	}

	public void previousPage() {
		int currentPage = pagedTableContainer.getCurrentPage();
		int newPage = currentPage - 1;
		pagedTableContainer.setCurrentPage(newPage);
		adjustPageLengthBasedOnItemsPerPage();
		firePageChangedEvent();
	}

	protected void adjustPageLengthBasedOnItemsPerPage() {
		if (isPaged()) {
			int itemsPerPage = pagedTableContainer.getItemsPerPage();
			int currentPageSize = pagedTableContainer.getCurrentPageSize();
			if (currentPageSize < itemsPerPage) {
				super.setPageLength(currentPageSize);
			} else {
				super.setPageLength(itemsPerPage);
			}
			//		} else if (customPageLength > 0 && super.size() > 0) {
			//			int newPageLength = Math.min(super.size(), customPageLength);
			//			super.setPageLength(newPageLength);
		}
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

	public void addItemsPerPageChangeListener(ItemsPerPageChangeListener listener) {
		if (!itemsPerPageChangeListeners.contains(listener)) {
			itemsPerPageChangeListeners.add(listener);
		}
	}

	public void removeItemsPerPageChangeListener(ItemsPerPageChangeListener listener) {
		itemsPerPageChangeListeners.remove(listener);
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
		pagedTableContainer.setCurrentPage(page);
		adjustPageLengthBasedOnItemsPerPage();
		firePageChangedEvent();
	}

	@Override
	public void sort(Object[] propertyId, boolean[] ascending) throws UnsupportedOperationException {
		super.sort(propertyId, ascending);
		deselectAll();
	}

	private static class PagedBaseTableContainer extends ContainerAdapter implements ItemSetChangeNotifier {

		private int startIndex;

		private int itemsPerPage;

		@SuppressWarnings("unchecked")
		public PagedBaseTableContainer(Indexed container, int itemsPerPage) {
			super(container);
			this.itemsPerPage = itemsPerPage;
		}

		public int getStartIndex() {
			return startIndex;
		}

		public void setStartIndex(int startIndex) {
			this.startIndex = startIndex;
		}

		public int getItemsPerPage() {
			return itemsPerPage;
		}

		public void setItemsPerPage(int itemsPerPage) {
			boolean changed = this.itemsPerPage != itemsPerPage;
			this.itemsPerPage = itemsPerPage;
			if (changed) {
				fireItemSetChange();
			}
		}

		public int getNumberOfPages() {
			int size = getNestedContainer().size();
			double pageLength = itemsPerPage;
			int pageCount = (int) Math.ceil(size / pageLength);
			if (pageCount < 1) {
				pageCount = 1;
			}
			return pageCount;
		}

		/**
		 * @return Index starts at 1
		 */
		public int getCurrentPage() {
			int page = (int) Math.floor((double) getStartIndex() / getItemsPerPage()) + 1;
			if (page < 1) {
				page = 1;
			}
			return page;
		}

		public void setCurrentPage(int page) {
			boolean changed = getCurrentPage() != page;
			this.startIndex = (page - 1) * itemsPerPage;
			if (changed) {
				fireItemSetChange();
			}
		}

		public int getCurrentPageSize() {
			int rowsLeft = getNestedContainer().size() - startIndex;
			if (rowsLeft > itemsPerPage) {
				return itemsPerPage;
			} else {
				return rowsLeft;
			}
		}

		public List<?> getCurrentPageItemIds() {
			int numberOfItems = Math.min(startIndex + itemsPerPage, getRealSize());
			return ((Indexed) getNestedContainer()).getItemIds(startIndex, numberOfItems);
		}

		@Override
		public List<?> getItemIds(int start, int numberOfItems) {
			int adjustedStart = startIndex + start;
			return ((Indexed) getNestedContainer()).getItemIds(adjustedStart, numberOfItems);
		}

		public int getPageOfIndex(int index) {
			int pageOfIndex;
			if (index == 1) {
				pageOfIndex = 1;
			} else {
				double pageLength = itemsPerPage;
				pageOfIndex = (int) (index / pageLength) + 1;
			}
			return pageOfIndex;
		}

		@Override
		public int size() {
			return getCurrentPageSize();
		}

		public int getRealSize() {
			return getNestedContainer().size();
		}
	}

	private class DefaultMaxLengthSelectDeselectAllButton extends MaxLengthSelectDeselectAllButton implements SelectionChangeListener {

		public DefaultMaxLengthSelectDeselectAllButton(String selectAllCaption, String deselectAllCaption,
													   boolean selectAllMode) {
			super(selectAllCaption, deselectAllCaption, selectAllMode);
			addSelectionChangeListener(this);
		}

		@Override
		public void detach() {
			super.detach();
			removeSelectionChangeListener(this);
		}

		@Override
		public void selectionChanged(SelectionChangeEvent event) {
			if (!(event.getComponent() instanceof DefaultMaxLengthSelectDeselectAllButton)) {
				if (event.isAllItemsSelected()) {
					setSelectAllMode(false);
				} else {
					setSelectAllMode(true);
				}
			}
		}

	}

	private class SelectionCheckBox extends CheckBox implements SelectionChangeListener {

		private Object itemId;

		private SelectionCheckBox(final Object itemId) {
			this.itemId = itemId;
			boolean selected = selectionManager.isSelected(itemId);
			setValue(selected);
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

					if (selectionManager.isAllItemsSelected()) {
						SelectionChangeEvent selectAllEvent = new SelectionChangeEvent();
						selectAllEvent.setComponent(SelectionCheckBox.this);
						selectAllEvent.setAllItemsSelected(true);
						fireSelectionChangeEvent(selectAllEvent);
					} else if (selectionManager.isAllItemsDeselected()) {
						SelectionChangeEvent deselectAllEvent = new SelectionChangeEvent();
						deselectAllEvent.setComponent(SelectionCheckBox.this);
						deselectAllEvent.setAllItemsDeselected(true);
						fireSelectionChangeEvent(deselectAllEvent);
					}
				}
			});
		}

		@Override
		public Boolean getInternalValue() {
			return super.getInternalValue();
		}

		@Override
		public void setInternalValue(Boolean newValue) {
			super.setInternalValue(newValue);
		}

		@Override
		public void attach() {
			super.attach();
			addSelectionChangeListener(this);
		}

		@Override
		public void detach() {
			removeSelectionChangeListener(this);
			super.detach();
		}

		@Override
		public void selectionChanged(SelectionChangeEvent event) {
			if (event.getComponent() != SelectionCheckBox.this) {
				if (event.isAllItemsSelected() || (event.getSelectedItemIds() != null && event.getSelectedItemIds().contains(itemId))) {
					setInternalValue(true);
				} else if (event.isAllItemsDeselected() || (event.getDeselectedItemIds() != null && event.getDeselectedItemIds().contains(itemId))) {
					setInternalValue(false);
				}
				markAsDirty();
			}
		}

	}

	public static interface PageChangeListener {
		public void pageChanged(PageChangeEvent event);
	}

	public class PageChangeEvent {

		private int currentPage;
		private int numberOfPages;

		public PageChangeEvent(int currentPage, int numberOfPages) {
			this.currentPage = currentPage;
			this.numberOfPages = numberOfPages;
		}

		public BaseTable getTable() {
			return BaseTable.this;
		}

		public int getCurrentPage() {
			return currentPage;
		}

		public int getNumberOfPages() {
			return numberOfPages;
		}
	}

	public static interface ItemsPerPageChangeListener {
		public void itemsPerPageChanged(ItemsPerPageChangeEvent event);
	}

	public class ItemsPerPageChangeEvent {

		private int oldItemsPerPage;
		private int newItemsPerPage;

		public ItemsPerPageChangeEvent(int oldItemsPerPage, int newItemsPerPage) {
			this.oldItemsPerPage = oldItemsPerPage;
			this.newItemsPerPage = newItemsPerPage;
		}

		public BaseTable getTable() {
			return BaseTable.this;
		}

		public int getOldItemsPerPage() {
			return oldItemsPerPage;
		}

		public int getNewItemsPerPage() {
			return newItemsPerPage;
		}

	}

	public class PagingControls extends I18NHorizontalLayout implements BrowserWindowResizeListener {

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
			addStyleName("paging-controls");
			if (isPaged()) {
				itemsPerPageField = new BaseComboBox();
				itemsPerPageField.setValue(itemsPerPageValue);

				int numberOfPages = pagedTableContainer.getNumberOfPages();
				int currentPageFieldInitialValue = pagedTableContainer.getCurrentPage();

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
						int newItemsPerPage = (int) itemsPerPageField.getValue();
						int oldItemsPerPage = pagedTableContainer.getItemsPerPage();
						pagedTableContainer.setItemsPerPage((int) itemsPerPageField.getValue());
						adjustPageLengthBasedOnItemsPerPage();
						fireItemsPerPageChangedEvent(oldItemsPerPage, newItemsPerPage);
					}
				});
				itemsPerPageField.setEnabled(itemsPerPageField.size() > 1);

				pageSizeLayout = new I18NHorizontalLayout(itemsPerPageLabel, itemsPerPageField);
				pageSizeLayout.addStyleName("page-size-layout");
				pageSizeLayout.setSpacing(true);
				pageSizeLayout.setComponentAlignment(itemsPerPageLabel, Alignment.MIDDLE_LEFT);
				pageSizeLayout.setComponentAlignment(itemsPerPageField, Alignment.MIDDLE_LEFT);

				currentPageLabel = new Label($("SearchResultTable.page"));
				currentPageField = new TextField();
				currentPageField.setConverter(Integer.class);
				currentPageField.setConvertedValue(currentPageFieldInitialValue);
				currentPageField.setWidth("45px");
				currentPageField.addValueChangeListener(new ValueChangeListener() {
					@Override
					public void valueChange(Property.ValueChangeEvent event) {
						boolean valid;
						// https://bugs.java.com/bugdatabase/view_bug.do?bug_id=4510618
						String newValue = StringUtils.replace(currentPageField.getValue(), "\u00a0", "");
						if (StringUtils.isNotBlank(newValue)) {
							try {
								int newIntValue = Integer.parseInt(newValue);
								valid = newIntValue > 0 && newIntValue < pagedTableContainer.getNumberOfPages();
							} catch (NumberFormatException e) {
								valid = false;
							}
						} else {
							valid = false;
						}
						if (valid) {
							int newPage = (int) currentPageField.getConvertedValue();
							pagedTableContainer.setCurrentPage(newPage);
							adjustPageLengthBasedOnItemsPerPage();
						}
					}
				});
				currentPageField.setEnabled(numberOfPages > 1 && !isUnknownEnd());

				separator = new Label($("SearchResultTable.of"));
				totalPagesLabel = new Label(String.valueOf(numberOfPages));

				firstPageButton = new Button("\uF100", new ClickListener() {
					public void buttonClick(ClickEvent event) {
						onSetPageButtonClicked(1);
					}
				});
				firstPageButton.setStyleName(ValoTheme.BUTTON_LINK);
				firstPageButton.setEnabled(currentPageFieldInitialValue > 1);

				previousPageButton = new Button("\uF104", new ClickListener() {
					public void buttonClick(ClickEvent event) {
						onPreviousPageButtonClicked();
					}
				});
				previousPageButton.setStyleName(ValoTheme.BUTTON_LINK);
				previousPageButton.setEnabled(currentPageFieldInitialValue > 1);

				nextPageButton = new Button("\uF105", new ClickListener() {
					public void buttonClick(ClickEvent event) {
						onNextPageButtonClicked();
					}
				});
				nextPageButton.setStyleName(ValoTheme.BUTTON_LINK);
				nextPageButton.setEnabled(currentPageFieldInitialValue < numberOfPages);

				lastPageButton = new Button("\uF101", new ClickListener() {
					public void buttonClick(ClickEvent event) {
						int numberOfPages = pagedTableContainer.getNumberOfPages();
						onSetPageButtonClicked(numberOfPages);
					}
				});
				lastPageButton.setStyleName(ValoTheme.BUTTON_LINK);
				lastPageButton.setEnabled(currentPageFieldInitialValue < numberOfPages);

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

				pageManagementLayout = new I18NHorizontalLayout();
				pageManagementLayout.addComponent(firstPageButton);
				pageManagementLayout.addComponent(previousPageButton);
				pageManagementLayout.addComponent(currentPageLabel);
				pageManagementLayout.addComponent(currentPageField);
				currentPageField.setEnabled(!isUnknownEnd());
				if (!isUnknownEnd()) {
					pageManagementLayout.addComponent(separator);
					pageManagementLayout.setComponentAlignment(separator, Alignment.MIDDLE_LEFT);

					pageManagementLayout.addComponent(totalPagesLabel);
					pageManagementLayout.setComponentAlignment(totalPagesLabel, Alignment.MIDDLE_LEFT);
				}

				pageManagementLayout.addComponent(nextPageButton);

				if (!isUnknownEnd()) {
					pageManagementLayout.addComponent(lastPageButton);
					pageManagementLayout.setComponentAlignment(lastPageButton, Alignment.MIDDLE_LEFT);
				}

				pageManagementLayout.addStyleName("page-management-layout");
				pageManagementLayout.setSpacing(true);
				pageManagementLayout.setComponentAlignment(firstPageButton, Alignment.MIDDLE_LEFT);
				pageManagementLayout.setComponentAlignment(previousPageButton, Alignment.MIDDLE_LEFT);
				pageManagementLayout.setComponentAlignment(currentPageLabel, Alignment.MIDDLE_LEFT);
				pageManagementLayout.setComponentAlignment(currentPageField, Alignment.MIDDLE_LEFT);

				pageManagementLayout.setComponentAlignment(nextPageButton, Alignment.MIDDLE_LEFT);


				addComponents(pageSizeLayout, pageManagementLayout);
				setComponentAlignment(pageManagementLayout, Alignment.BOTTOM_CENTER);
				//				setExpandRatio(pageSizeLayout, 1);
				//				setWidth("100%");
				setSpacing(true);

				addPageChangeListener(new PageChangeListener() {
					public void pageChanged(PageChangeEvent event) {
						updateControls();
					}
				});
				addItemsPerPageChangeListener(new ItemsPerPageChangeListener() {
					@Override
					public void itemsPerPageChanged(ItemsPerPageChangeEvent event) {
						updateControls();
					}
				});
			} else {
				setVisible(false);
			}
		}

		private void updateControls() {
			int numberOfPages = pagedTableContainer.getNumberOfPages();
			int currentPageFieldValue = pagedTableContainer.getCurrentPage();

			firstPageButton.setEnabled(currentPageFieldValue > 1);
			previousPageButton.setEnabled(currentPageFieldValue > 1);
			nextPageButton.setEnabled(currentPageFieldValue < numberOfPages);
			lastPageButton.setEnabled(currentPageFieldValue < numberOfPages);
			currentPageField.setValue(String.valueOf(currentPageFieldValue));
			currentPageField.setEnabled(numberOfPages > 1 && !isUnknownEnd());
			totalPagesLabel.setValue(String.valueOf(numberOfPages));
		}

		public void setItemsPerPageValue(int value) {
			this.itemsPerPageValue = value;
			if (itemsPerPageField != null) {
				itemsPerPageField.setValue(value);
			}
		}

		@Override
		public void attach() {
			super.attach();
			Page.getCurrent().addBrowserWindowResizeListener(this);
			computeResponsive();
		}

		@Override
		public void detach() {
			Page.getCurrent().removeBrowserWindowResizeListener(this);
			super.detach();
		}

		private void computeResponsive() {
			if (ResponsiveUtils.isPhone()) {
				setHeight("85px");
			} else {
				setHeight(null);
			}
		}

		@Override
		public void browserWindowResized(BrowserWindowResizeEvent event) {
			computeResponsive();
		}

	}

	public void setPreLoader(PreLoader preloader) {
		this.preloader = preloader;
	}

	public class DeselectAllButton extends SelectDeselectAllButton {
		private DeselectAllButton(String deselectAllCaption) {
			super("", deselectAllCaption, false);
		}

		@Override
		protected void onSelectAll(ClickEvent event) {
			onDeselectAll(event);
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
			onDeselectAll(event);
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

		private int getMaxSelectableResults() {
			ModelLayerFactory modelLayerFactory = ConstellioUI.getCurrent().getConstellioFactories().getModelLayerFactory();
			return modelLayerFactory.getSystemConfigurationsManager().getValue(ConstellioEIMConfigs.MAX_SELECTABLE_SEARCH_RESULTS);
		}

		private boolean isAlwaysSelectIntervals() {
			ModelLayerFactory modelLayerFactory = ConstellioUI.getCurrent().getConstellioFactories().getModelLayerFactory();
			return modelLayerFactory.getSystemConfigurationsManager().getValue(ConstellioEIMConfigs.ALWAYS_SELECT_INTERVALS);
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
			int maxSelectableResults = getMaxSelectableResults();
			if (realSize <= maxSelectableResults && (!BaseTable.this.isPaged() || !isAlwaysSelectIntervals())) {
				super.buttonClick(event);
			} else {
				if (rangeStart == -1) {
					rangeStart = 1;
				}
				if (rangeEnd == -1 || rangeEnd > (rangeStart + (maxSelectableResults - 1))) {
					rangeEnd = rangeStart + (maxSelectableResults - 1);
					if (rangeEnd > size()) {
						rangeEnd = size();
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
							Integer newRangeEnd = newRangeStart + (maxSelectableResults - 1);
							if (newRangeEnd > size()) {
								newRangeEnd = size();
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
						} else if (realRangeEnd > (realRangeStart + maxSelectableResults - 1) || realRangeEnd > realSize - 1 || realRangeEnd < realRangeStart) {
							int rangeEndMin = rangeStart;
							int rangeEndMax = rangeStart + maxSelectableResults - 1;
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
							if (preloader != null) {
								int startIndex = pagedTableContainer.getStartIndex();
								preloader.load(startIndex + realRangeStart, realRangeEnd - realRangeStart + 1);
							}

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
