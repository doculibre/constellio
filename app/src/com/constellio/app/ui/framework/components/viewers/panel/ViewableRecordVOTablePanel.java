package com.constellio.app.ui.framework.components.viewers.panel;

import com.constellio.app.api.extensions.params.SchemaDisplayParams;
import com.constellio.app.extensions.AppLayerCollectionExtensions;
import com.constellio.app.extensions.ui.ViewableRecordVOTablePanelExtension.ViewableRecordVOTablePanelExtensionParams;
import com.constellio.app.services.factories.AppLayerFactory;
import com.constellio.app.services.menu.MenuItemFactory.MenuItemRecordProvider;
import com.constellio.app.ui.application.ConstellioUI;
import com.constellio.app.ui.entities.MetadataSchemaVO;
import com.constellio.app.ui.entities.RecordVO;
import com.constellio.app.ui.entities.RecordVO.VIEW_MODE;
import com.constellio.app.ui.entities.UserVO;
import com.constellio.app.ui.framework.builders.RecordToVOBuilder;
import com.constellio.app.ui.framework.buttons.BaseButton;
import com.constellio.app.ui.framework.buttons.IconButton;
import com.constellio.app.ui.framework.buttons.SelectDeselectAllButton;
import com.constellio.app.ui.framework.components.RecordDisplayFactory;
import com.constellio.app.ui.framework.components.SearchResultDisplay;
import com.constellio.app.ui.framework.components.ViewWindow;
import com.constellio.app.ui.framework.components.display.ReferenceDisplay;
import com.constellio.app.ui.framework.components.layouts.I18NCssLayout;
import com.constellio.app.ui.framework.components.layouts.I18NHorizontalLayout;
import com.constellio.app.ui.framework.components.menuBar.RecordListMenuBar;
import com.constellio.app.ui.framework.components.mouseover.NiceTitle;
import com.constellio.app.ui.framework.components.selection.SelectionComponent.SelectionChangeEvent;
import com.constellio.app.ui.framework.components.selection.SelectionComponent.SelectionChangeListener;
import com.constellio.app.ui.framework.components.selection.SelectionComponent.SelectionManager;
import com.constellio.app.ui.framework.components.table.BaseTable;
import com.constellio.app.ui.framework.components.table.BaseTable.DeselectAllButton;
import com.constellio.app.ui.framework.components.table.BaseTable.ItemsPerPageChangeListener;
import com.constellio.app.ui.framework.components.table.BaseTable.PageChangeListener;
import com.constellio.app.ui.framework.components.table.BaseTable.PagingControls;
import com.constellio.app.ui.framework.components.table.RecordVOTable;
import com.constellio.app.ui.framework.components.table.RecordVOTable.RecordVOSelectionManager;
import com.constellio.app.ui.framework.components.table.TableModeManager;
import com.constellio.app.ui.framework.containers.ContainerAdapter;
import com.constellio.app.ui.framework.containers.PreLoader;
import com.constellio.app.ui.framework.containers.RecordVOContainer;
import com.constellio.app.ui.framework.exception.UserException.UserDoesNotHaveAccessException;
import com.constellio.app.ui.pages.base.BaseView;
import com.constellio.app.ui.pages.management.schemaRecords.DisplaySchemaRecordWindow;
import com.constellio.app.ui.util.ComponentTreeUtils;
import com.constellio.app.ui.util.ResponsiveUtils;
import com.constellio.model.entities.enums.TableMode;
import com.constellio.model.entities.records.Record;
import com.constellio.model.services.factories.ModelLayerFactory;
import com.constellio.model.services.migrations.ConstellioEIMConfigs;
import com.constellio.model.services.records.RecordServices;
import com.constellio.model.services.search.query.logical.LogicalSearchQuery;
import com.vaadin.data.Container.ItemSetChangeEvent;
import com.vaadin.data.Item;
import com.vaadin.data.Property;
import com.vaadin.data.util.ObjectProperty;
import com.vaadin.event.ItemClickEvent;
import com.vaadin.event.ItemClickEvent.ItemClickListener;
import com.vaadin.event.Transferable;
import com.vaadin.event.dd.DragAndDropEvent;
import com.vaadin.event.dd.DropHandler;
import com.vaadin.event.dd.acceptcriteria.AcceptAll;
import com.vaadin.event.dd.acceptcriteria.AcceptCriterion;
import com.vaadin.navigator.ViewChangeListener;
import com.vaadin.server.FontAwesome;
import com.vaadin.server.Page;
import com.vaadin.server.Page.BrowserWindowResizeEvent;
import com.vaadin.server.Page.BrowserWindowResizeListener;
import com.vaadin.server.Resource;
import com.vaadin.shared.MouseEventDetails.MouseButton;
import com.vaadin.shared.ui.dd.VerticalDropLocation;
import com.vaadin.ui.AbstractSelect.AbstractSelectTargetDetails;
import com.vaadin.ui.Alignment;
import com.vaadin.ui.Button;
import com.vaadin.ui.Button.ClickListener;
import com.vaadin.ui.Component;
import com.vaadin.ui.DragAndDropWrapper;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Label;
import com.vaadin.ui.MenuBar;
import com.vaadin.ui.Table;
import com.vaadin.ui.Table.CellStyleGenerator;
import com.vaadin.ui.Table.TableDragMode;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.Window;
import com.vaadin.ui.Window.CloseEvent;
import com.vaadin.ui.themes.ValoTheme;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.vaadin.peter.contextmenu.ContextMenu;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static com.constellio.app.ui.i18n.i18n.$;
import static com.constellio.app.ui.i18n.i18n.isRightToLeft;

//@com.vaadin.annotations.JavaScript({ "theme://jquery/jquery-2.1.4.min.js" })
@Slf4j
public class ViewableRecordVOTablePanel extends I18NHorizontalLayout implements BrowserWindowResizeListener, DropHandler, ViewChangeListener {

	public static final int MAX_SELECTION_SIZE = 10000;

	private VerticalLayout tableLayout;

	private I18NCssLayout tableButtonsLayout;

	private I18NHorizontalLayout selectionButtonsLayout;

	private I18NHorizontalLayout actionAndModeButtonsLayout;

	private I18NHorizontalLayout actionButtonsLayout;

	private VerticalLayout closeButtonViewerMetadataLayout;

	private RecordVOContainer recordVOContainer;

	private BaseTable table;

	private ViewerMetadataPanel viewerMetadataPanel;

	private BaseButton previousButton;

	private BaseButton nextButton;

	private ListModeButton listModeButton;

	private TableModeButton tableModeButton;

	private SelectDeselectAllButton selectDeselectAllToggleButton;

	private Label countLabel;

	private Label selectedItemCountLabel;

	private BaseButton closeViewerButton;

	private Object selectedItemId;

	private RecordVO selectedRecordVO;

	private Object previousItemId;

	private Object nextItemId;

	private List<SelectionChangeListener> selectionChangeListeners = new ArrayList<>();

	private List<ItemClickListener> itemClickListeners = new ArrayList<>();

	private List<TableCompressListener> tableCompressListeners = new ArrayList<>();

	private List<TableModeChangeListener> tableModeChangeListeners = new ArrayList<>();

	private TableMode tableMode;

	private PagingControls pagingControls;

	private List<Object> tableModeVisibleColumns = new ArrayList<>();

	private Map<Object, String> tableModeColumnHeaders = new HashMap<>();

	private Map<Object, Integer> tableModeColumnExpandRatios = new HashMap<>();

	private RecordListMenuBar selectionActionsMenuBar;

	private RecordListMenuBar initialSelectionActionsMenuBar = null;

	private boolean allItemsVisible = false;

	private ViewWindow viewWindow;
	private boolean canChangeTableMode;

	private String searchTerm = null;

	private TableModeManager tableModeManager;

	private String panelId = null;

	private Integer itemsPerPage;
	private Integer currentPage;

	private List<PageChangeListener> pageChangeListeners = new ArrayList();
	private List<ItemsPerPageChangeListener> itemsPerPageChangeListeners = new ArrayList();

	public ViewableRecordVOTablePanel(RecordVOContainer container) {
		this(container, null, null, true);
	}

	public ViewableRecordVOTablePanel(RecordVOContainer container, RecordListMenuBar recordListMenuBar,
									  TableMode tableMode, boolean canChangeTableMode) {
		this.recordVOContainer = container;
		this.initialSelectionActionsMenuBar = recordListMenuBar;
		this.canChangeTableMode = canChangeTableMode;

		tableModeManager = new TableModeManager();
		panelId = getPanelId();
		this.tableMode = tableMode != null ? tableMode : tableModeManager.getTableModeForCurrentUser(panelId);

		buildUI();
	}

	@Override
	public void attach() {
		super.attach();
		Page.getCurrent().addBrowserWindowResizeListener(this);
		ConstellioUI.getCurrent().getNavigator().addViewChangeListener(this);
	}

	@Override
	public void detach() {
		Page.getCurrent().removeBrowserWindowResizeListener(this);
		ConstellioUI.getCurrent().getNavigator().removeViewChangeListener(this);
		super.detach();
	}

	public RecordVOContainer getRecordVOContainer() {
		return recordVOContainer;
	}


	public void setSearchTerm(String searchTerm) {
		this.searchTerm = searchTerm;
	}

	private void buildUI() {
		setWidth("100%");
		//		setSpacing(true);
		addStyleName("viewable-record-table-panel");
		setId(UUID.randomUUID().toString());

		boolean empty = recordVOContainer.size() == 0;
		table = buildResultsTable();
		if (isSelectColumn()) {
			selectDeselectAllToggleButton = newSelectDeselectAllToggleButton();
			selectDeselectAllToggleButton.addStyleName(ValoTheme.BUTTON_LINK);
			if (!(selectDeselectAllToggleButton instanceof DeselectAllButton)) {
				selectDeselectAllToggleButton.setVisible(!empty);
			}
		}

		countLabel = new Label();
		countLabel.addStyleName("count-label");
		countLabel.setVisible(false);

		selectedItemCountLabel = new Label();
		selectedItemCountLabel.addStyleName("count-label");
		selectedItemCountLabel.setVisible(false);

		viewerMetadataPanel = buildViewerMetadataPanel();

		listModeButton = buildListModeButton();
		listModeButton.setVisible(this.canChangeTableMode);

		tableModeButton = buildTableModeButton();
		tableModeButton.setVisible(this.canChangeTableMode);

		previousButton = buildPreviousButton();
		nextButton = buildNextButton();
		closeViewerButton = buildCloseViewerButton();

		tableLayout = new VerticalLayout();
		tableLayout.addStyleName("viewable-record-panel-table-layout");
		//		tableLayout.setHeight("100%");

		tableButtonsLayout = new I18NCssLayout();
		tableButtonsLayout.addStyleName("table-buttons-layout");
		tableButtonsLayout.setWidth("100%");
		//		tableButtonsLayout.setSpacing(true);

		selectionButtonsLayout = new I18NHorizontalLayout();
		selectionButtonsLayout.addStyleName("selection-buttons-layout");
		selectionButtonsLayout.setSpacing(true);

		actionButtonsLayout = new I18NHorizontalLayout();
		actionButtonsLayout.addStyleName("action-buttons-layout");
		actionButtonsLayout.setSpacing(true);

		actionAndModeButtonsLayout = new I18NHorizontalLayout();
		actionAndModeButtonsLayout.addStyleName("action-mode-buttons-layout");
		actionAndModeButtonsLayout.addComponents(actionButtonsLayout);

		if (isSelectColumn()) {
			selectionButtonsLayout.addComponent(selectDeselectAllToggleButton);
		}
		selectionButtonsLayout.addComponents(selectedItemCountLabel, countLabel);
		selectionButtonsLayout.addComponents(previousButton, nextButton);

		actionAndModeButtonsLayout.addComponents(listModeButton, tableModeButton);

		tableButtonsLayout.addComponents(selectionButtonsLayout, actionAndModeButtonsLayout);

		//		tableButtonsLayout.setComponentAlignment(selectionButtonsLayout, Alignment.TOP_LEFT);
		//		tableButtonsLayout.setComponentAlignment(actionAndModeButtonsLayout, Alignment.TOP_RIGHT);

		tableLayout.addComponent(tableButtonsLayout);
		tableLayout.addComponent(table);
		if (table.isPaged()) {
			pagingControls = table.createPagingControls();
			if (!ConstellioUI.getCurrent().isNested()) {
				ConstellioUI.getCurrent().setStaticFooterContent(pagingControls);
			} else {
				tableLayout.addComponent(pagingControls);
			}
		}
		Label spacer = new Label("");
		spacer.setHeight("50px");
		tableLayout.addComponent(spacer);

		closeButtonViewerMetadataLayout = new VerticalLayout(closeViewerButton, viewerMetadataPanel);
		closeButtonViewerMetadataLayout.addStyleName("close-button-viewer-metadata-layout");
		closeButtonViewerMetadataLayout.setId("close-button-viewer-metadata-layout");
		//		closeButtonViewerMetadataLayout.setHeight("100%");
		closeButtonViewerMetadataLayout.setComponentAlignment(closeViewerButton, Alignment.TOP_RIGHT);
		//		closeButtonViewerMetadataLayout.setWidthUndefined();

		addComponents(tableLayout, closeButtonViewerMetadataLayout);
		addTableCompressListener(new TableCompressListener() {
			@Override
			public void tableCompressChange(TableCompressEvent event) {
				if (isCompressionSupported()) {
					((ViewableRecordVOTable) table).setCompressed(event.isCompressed());
				}
			}
		});
		closeButtonViewerMetadataLayout.setVisible(isCompressionSupported());
		adjustTableExpansion();
	}

	public boolean isAllItemsVisible() {
		return allItemsVisible;
	}

	public void setAllItemsVisible(boolean allItemsVisible) {
		this.allItemsVisible = allItemsVisible;
		adjustResultsTableHeight();
	}

	private void adjustResultsTableHeight() {
		if (allItemsVisible && (table != null && (tableMode == TableMode.TABLE || !isPagedInListMode())) && table.getPageLength() != table.size()) {
			table.setPageLength(table.size());
		}
	}

	public void setCountCaption(String caption) {
		if (!isUnknownEnd()) {
			countLabel.setValue(caption);
			countLabel.setVisible(StringUtils.isNotBlank(caption));
		}
	}

	public boolean isUnknownEnd() {
		return false;
	}

	public void setSelectedCountCaption(int numberOfSelected) {
		String key = numberOfSelected <= 1 ? "ViewableRecordVOTablePanel.nbSelectedElement1" : "ViewableRecordVOTablePanel.nbSelectedElements";
		String totalCount = $(key, numberOfSelected);

		selectedItemCountLabel.setValue(totalCount);
		selectedItemCountLabel.setVisible(numberOfSelected > 0);
	}

	public void setQuickActionButtons(List<Button> buttons) {
		for (Button button : buttons) {
			setQuickActionButton(button);
		}
	}

	public void setQuickActionButton(Button quickActionButton) {
		quickActionButton.addStyleName(ValoTheme.BUTTON_LINK);
		quickActionButton.addStyleName("quick-action-button");
		if (quickActionButton instanceof BaseButton) {
			((BaseButton) quickActionButton).setCaptionVisibleOnMobile(false);
		}
		actionButtonsLayout.addComponent(quickActionButton, 0);
		actionButtonsLayout.setComponentAlignment(quickActionButton, Alignment.TOP_LEFT);
	}

	public void setQuickActionButtonsVisible(boolean visible) {
		for (int i = 0; i < actionButtonsLayout.getComponentCount(); i++) {
			Component actionButtonComponent = actionButtonsLayout.getComponent(i);
			if (actionButtonComponent.getStyleName() != null && actionButtonComponent.getStyleName().contains("quick-action-button")) {
				actionButtonComponent.setVisible(visible);
			}
		}
	}

	public void setSelectionActionButtons() {
		if (isSelectColumn()) {
			if (initialSelectionActionsMenuBar == null) {
				selectionActionsMenuBar = new RecordListMenuBar(getMenuItemProvider(), $("ViewableRecordVOTablePanel.selectionActions"), excludedMenuItemInDefaultSelectionActionButtons(), getMainView());
			} else {
				selectionActionsMenuBar = initialSelectionActionsMenuBar;
				selectionActionsMenuBar.setRecordProvider(getMenuItemProvider());
			}

			addSelectionActionsMenuBarToView();
		}
	}

	protected boolean isHideQuickActionButtonsOnSelection() {
		return true;
	}

	protected List<String> excludedMenuItemInDefaultSelectionActionButtons() {
		return Collections.emptyList();
	}

	protected MenuItemRecordProvider getMenuItemProvider() {
		return new MenuItemRecordProvider() {
			@Override
			public List<Record> getRecords() {
				return ViewableRecordVOTablePanel.this.getSelectedRecords();
			}

			@Override
			public LogicalSearchQuery getQuery() {
				return null;
			}
		};
	}

	private void addSelectionActionsMenuBarToView() {
		if (!isSelectionActionMenuBar()) {
			return;
		}

		selectionActionsMenuBar.addStyleName("selection-action-menu-bar");
		selectionActionsMenuBar.setAutoOpen(false);
		actionButtonsLayout.addComponent(selectionActionsMenuBar, 0);
		actionButtonsLayout.setComponentAlignment(selectionActionsMenuBar, Alignment.TOP_RIGHT);

		addSelectionChangeListener(new SelectionChangeListener() {
			@Override
			public void selectionChanged(SelectionChangeEvent event) {
				selectionActionsMenuBar.buildMenuItems();

				int selectedCount;
				SelectionManager selectionManager = table.getSelectionManager();
				if (selectionManager.isAllItemsSelected()) {
					selectedCount = recordVOContainer.size();
				} else if (selectionManager.isAllItemsDeselected()) {
					selectedCount = 0;
				} else {
					selectedCount = selectionManager.getAllSelectedItemIds().size();
				}
				setSelectedCountCaption(selectedCount);
			}
		});
	}

	public boolean isSelectionActionMenuBar() {
		return true;
	}

	public List<RecordVO> getSelectedRecordVOs() {
		List<RecordVO> selectedRecords;
		if (table.getSelectionManager() instanceof RecordVOSelectionManager) {
			RecordVOSelectionManager recordVOSelectionManager = (RecordVOSelectionManager) table.getSelectionManager();
			selectedRecords = recordVOSelectionManager.getSelectedRecordVOs();
		} else {
			List<Object> selectedItemIds = table.getSelectionManager().getAllSelectedItemIds();
			selectedRecords = recordVOContainer.getRecordsVO(selectedItemIds);
		}
		return selectedRecords;
	}

	public List<Record> getSelectedRecords() {
		List<Record> selectedRecords;
		if (table.getSelectionManager() instanceof RecordVOSelectionManager) {
			RecordVOSelectionManager recordVOSelectionManager = (RecordVOSelectionManager) table.getSelectionManager();
			selectedRecords = recordVOSelectionManager.getSelectedRecords();
		} else {
			selectedRecords = new ArrayList<>();
			List<Object> selectedItemIds = table.getSelectionManager().getAllSelectedItemIds();
			List<RecordVO> recordVOS = recordVOContainer.getRecordsVO(selectedItemIds);
			for (RecordVO recordVO : recordVOS) {
				selectedRecords.add(recordVO.getRecord());
			}
		}
		return selectedRecords;
	}

	protected int computeCompressedWidth() {
		return Page.getCurrent().getBrowserWindowWidth() > 1400 ? 650 : 500;
	}

	protected boolean isCompressionSupported() {
		return ResponsiveUtils.isDesktop() && !isNested() && tableMode == TableMode.LIST;
	}

	public boolean isNested() {
		return false;
	}

	protected Component newSearchResultComponent(Object itemId) {
		return null;
	}

	//	private void ensureHeight(Object itemId) {
	//		int l = table.getPageLength();
	//		int index = table.indexOfId(itemId);
	//		int indexToSelectAbove = index - (l / 2);
	//		if (indexToSelectAbove < 0) {
	//			indexToSelectAbove = 0;
	//		}
	//		table.setCurrentPageFirstItemIndex(indexToSelectAbove);
	//	}

	void adjustTableExpansion() {
		if (tableMode == TableMode.LIST) {
			if (isCompressionSupported()) {
				String compressedStyleName = "viewable-record-table-compressed";
				if (selectedItemId != null) {
					if (!closeButtonViewerMetadataLayout.isVisible()) {
						int compressedWidth = computeCompressedWidth();
						if (table != null) {
							int searchResultPropertyWidth = compressedWidth - BaseTable.SELECT_PROPERTY_WIDTH - 3;
							if (isShowThumbnailCol()) {
								searchResultPropertyWidth -= ViewableRecordVOContainer.THUMBNAIL_WIDTH;
							}
							table.setColumnWidth(ViewableRecordVOContainer.SEARCH_RESULT_PROPERTY, searchResultPropertyWidth);
							table.addStyleName(compressedStyleName);
						}
						closeButtonViewerMetadataLayout.setVisible(true);
						tableLayout.setWidth(compressedWidth + "px");
						setExpandRatio(tableLayout, 0);
						setExpandRatio(closeButtonViewerMetadataLayout, 1);
					}
				} else {
					if (closeButtonViewerMetadataLayout.isVisible()) {
						if (table != null) {
							table.setColumnWidth(ViewableRecordVOContainer.SEARCH_RESULT_PROPERTY, -1);
							table.removeStyleName(compressedStyleName);
						}
						closeButtonViewerMetadataLayout.setVisible(false);
						tableLayout.setWidth("100%");
						setExpandRatio(tableLayout, 1);
						setExpandRatio(closeButtonViewerMetadataLayout, 0);
					}
				}
			}
		} else {
			closeButtonViewerMetadataLayout.setVisible(false);
			tableLayout.setWidth("100%");
			setExpandRatio(tableLayout, 1);
			setExpandRatio(closeButtonViewerMetadataLayout, 0);
		}
		//		adjustHeight();
	}

	protected boolean isShowThumbnailCol() {
		return true;
	}

	protected boolean isNewMenuBarDefined() {
		return false;
	}

	protected MenuBar newMenuBar(RecordVO itemId) {
		return null;
	}

	private BaseTable buildResultsTable() {
		BaseTable resultsTable;
		if (tableMode == TableMode.LIST) {
			resultsTable = buildListModeComponent();
		} else {
			resultsTable = buildTableModeComponent();
		}

		final CellStyleGenerator cellStyleGenerator = resultsTable.getCellStyleGenerator();
		resultsTable.setCellStyleGenerator(new CellStyleGenerator() {
			@Override
			public String getStyle(Table source, Object itemId, Object propertyId) {
				String baseStyle = cellStyleGenerator != null ? cellStyleGenerator.getStyle(source, itemId, propertyId) : "";
				if (StringUtils.isNotBlank(baseStyle)) {
					baseStyle += " ";
				}
				String tableModeName = tableMode == TableMode.LIST ? "listMode" : "tableMode";
				baseStyle += "viewer-results-table-" + tableModeName + " ";
				baseStyle += "viewer-results-table-" + propertyId + " ";
				baseStyle += "viewer-results-table-" + tableModeName + "-" + propertyId + " ";
				return baseStyle + "viewer-results-table-row-" + itemId;
			}
		});

		resultsTable.addItemClickListener(new ItemClickListener() {
			@Override
			public void itemClick(ItemClickEvent event) {
				if (ResponsiveUtils.isMobile() || event.getButton() == MouseButton.LEFT) {
					rowClicked(event);
					
					// Set title style without waiting to rebuild the table.
					Property<?> property = resultsTable.getContainerProperty(event.getItemId(), ViewableRecordVOContainer.SEARCH_RESULT_PROPERTY);
					if (property != null) {
						Component cellComponent = (Component) property.getValue();
						SearchResultDisplay display = ComponentTreeUtils.getFirstChild(cellComponent, SearchResultDisplay.class);
						if (display != null) {
							display.addVisitedStyleNameToTitle();
						}
					}
				}
			}
		});
		for (ItemClickListener listener : itemClickListeners) {
			resultsTable.addItemClickListener(listener);
		}
		for (SelectionChangeListener listener : selectionChangeListeners) {
			resultsTable.addSelectionChangeListener(listener);
		}
		resultsTable.removeStyleName(RecordVOTable.CLICKABLE_ROW_STYLE_NAME);
		//resultsTable.setAlwaysRecalculateColumnWidths(true);

		if (isRowDragSupported()) {
			resultsTable.setDragMode(TableDragMode.ROW);
			resultsTable.setDropHandler(this);
		}

		return resultsTable;
	}

	protected BaseTable buildListModeComponent() {
		BaseTable resultsTable;

		ViewableRecordVOContainer viewableRecordVOContainer = new ViewableRecordVOContainer(recordVOContainer) {
			@Override
			protected Component getRecordDisplay(Object itemId) {
				Component recordDisplay = ViewableRecordVOTablePanel.this.newSearchResultComponent(itemId);
				if (recordDisplay == null) {
					recordDisplay = super.getRecordDisplay(itemId);
				}
				return recordDisplay;
			}

			@Override
			protected boolean isShowThumbnailCol() {
				return ViewableRecordVOTablePanel.this.isShowThumbnailCol();
			}
		};
		
		final ViewableRecordVOTable viewableRecordVOTable = new ViewableRecordVOTable(viewableRecordVOContainer) {
			@Override
			public boolean isSelectColumn() {
				return ViewableRecordVOTablePanel.this.isSelectColumn();
			}

			@Override
			public boolean isDragColumn() {
				return ViewableRecordVOTablePanel.this.isRowDragSupported();
			}

			@Override
			protected SelectionManager newSelectionManager() {
				SelectionManager selectionManager = ViewableRecordVOTablePanel.this.newSelectionManager();
				if (selectionManager == null) {
					selectionManager = super.newSelectionManager();
				}
				SelectionManager finalSelectionManager = createSelectionManagerWithSelectedCountCaption(selectionManager);
				return finalSelectionManager;
			}

			@Override
			protected MenuBar newMenuBar(Object itemId) {
				if (isNewMenuBarDefined()) {
					Item item = getItem(itemId);
					RecordVO recordVO = getRecordVOForTitleColumn(item);

					return ViewableRecordVOTablePanel.this.newMenuBar(recordVO);
				} else {
					return super.newMenuBar(itemId);
				}
			}

			@Override
			public boolean isUnknownEnd() {
				return ViewableRecordVOTablePanel.this.isUnknownEnd();
			}

			private SelectionManager createSelectionManagerWithSelectedCountCaption(
					SelectionManager selectionManager) {
				final SelectionManager finalSelectionManager = selectionManager;
				SelectionManager selectionManagerWithSelectedCount = new SelectionManager() {
					@Override
					public List<Object> getAllSelectedItemIds() {
						return finalSelectionManager.getAllSelectedItemIds();
					}

					@Override
					public boolean isAllItemsSelected() {
						return finalSelectionManager.isAllItemsSelected();
					}

					@Override
					public boolean isAllItemsDeselected() {
						return finalSelectionManager.isAllItemsDeselected();
					}

					@Override
					public boolean isSelected(Object itemId) {
						return finalSelectionManager.isSelected(itemId);
					}

					@Override
					public void selectionChanged(SelectionChangeEvent event) {
						finalSelectionManager.selectionChanged(event);
						setSelectedCountCaption(getSelectedSize());
					}
				};
				return selectionManagerWithSelectedCount;
			}

			@Override
			public boolean isPaged() {
				return ViewableRecordVOTablePanel.this.isPagedInListMode();
			}

			@Override
			protected void scrollToTop() {
				ConstellioUI.getCurrent().scrollToTop();
			}

			@Override
			protected RecordVO getRecordVOForTitleColumn(Item item) {
				RecordVO recordVO = ViewableRecordVOTablePanel.this.getRecordVOForTitleColumn(item);
				if (recordVO == null) {
					recordVO = super.getRecordVOForTitleColumn(item);
				}
				return recordVO;
			}

			@Override
			public void containerItemSetChange(ItemSetChangeEvent event) {
				super.containerItemSetChange(event);
				if (!isPaged() && allItemsVisible) {
					adjustResultsTableHeight();
					//					} else {
					//						scrollToTop();
				}
				int newSize = size();
				if (selectDeselectAllToggleButton != null) {
					selectDeselectAllToggleButton.setVisible(newSize > 0);
				}
				if (countLabel != null) {
					countLabel.setVisible(newSize > 0);
				}
			}
		};
		viewableRecordVOTable.setWidth("100%");
		if (recordVOContainer instanceof PreLoader) {
			viewableRecordVOTable.setPreLoader((PreLoader) recordVOContainer);
		}

//		recordVOContainer.sort(new Object[0], new boolean[0]);
		
		resultsTable = viewableRecordVOTable;
		resultsTable.setContainerDataSource(new ContainerAdapter(viewableRecordVOContainer) {
			@Override
			public Property getContainerProperty(Object itemId, Object propertyId) {
				Property result = super.getContainerProperty(itemId, propertyId);
				Object propertyValue = result.getValue();
				if (propertyValue instanceof SearchResultDisplay) {
					SearchResultDisplay searchResultDisplay = (SearchResultDisplay) propertyValue;
					for (ClickListener clickListener : searchResultDisplay.getClickListeners()) {
						searchResultDisplay.removeClickListener(clickListener);
					}
				}
				//					if (propertyValue instanceof Component) {
				//						List<ReferenceDisplay> referenceDisplays = ComponentTreeUtils.getChildren((Component) propertyValue, ReferenceDisplay.class);
				//						for (ReferenceDisplay referenceDisplay : referenceDisplays) {
				//							for (Object listenerObject : new ArrayList<>(referenceDisplay.getListeners(ClickEvent.class))) {
				//								referenceDisplay.removeClickListener((ClickListener) listenerObject);
				//							}
				//						}
				//						List<ConstellioAgentLink> constellioAgentLinks = ComponentTreeUtils.getChildren((Component) propertyValue, ConstellioAgentLink.class);
				//						for (ConstellioAgentLink constellioAgentLink : constellioAgentLinks) {
				//							for (Object listenerObject : new ArrayList<>(constellioAgentLink.getAgentLink().getListeners(ClickEvent.class))) {
				//								constellioAgentLink.getAgentLink().removeClickListener((ClickListener) listenerObject);
				//							}
				//						}
				//					}
				return new ObjectProperty<>(propertyValue);
			}
		});

		resultsTable.setSelectable(true);
		resultsTable.setMultiSelect(false);
		if (resultsTable.isPaged()) {
			if (this.currentPage != null) {
				resultsTable.setCurrentPage(this.currentPage);
			}
			if (this.itemsPerPage != null) {
				resultsTable.setItemsPerPage(this.itemsPerPage);
			}
			for (PageChangeListener listener : pageChangeListeners) {
				resultsTable.addPageChangeListener(listener);
			}
			for (ItemsPerPageChangeListener listener : itemsPerPageChangeListeners) {
				resultsTable.addItemsPerPageChangeListener(listener);
			}
		} else if (allItemsVisible) {
			resultsTable.setPageLength(resultsTable.size());
		}
		if (isIndexVisible()) {
			addStyleName("viewable-record-table-panel-with-index");
		}

		return resultsTable;
	}

	protected BaseTable buildTableModeComponent() {
		BaseTable resultsTable;

		resultsTable = new RecordVOTable(recordVOContainer) {
			@Override
			public String getTableId() {
				String tableId = super.getTableId();
				if (tableId == null) {
					tableId = getClass().getName() + ".tableMode";
				}
				return tableId;
			}

			@Override
			protected RecordVO getRecordVOForTitleColumn(Item item) {
				RecordVO recordVO = ViewableRecordVOTablePanel.this.getRecordVOForTitleColumn(item);
				if (recordVO == null) {
					recordVO = super.getRecordVOForTitleColumn(item);
				}
				return recordVO;
			}

			@Override
			public boolean isContextMenuPossible() {
				return false;
			}

			@Override
			public boolean isSelectColumn() {
				return ViewableRecordVOTablePanel.this.isSelectColumn();
			}

			@Override
			public boolean isIndexColumn() {
				return ViewableRecordVOTablePanel.this.isIndexVisible();
			}

			@Override
			protected SelectionManager newSelectionManager() {
				SelectionManager selectionManager = ViewableRecordVOTablePanel.this.newSelectionManager();
				if (selectionManager == null) {
					selectionManager = super.newSelectionManager();
				}
				return selectionManager;
			}

			@Override
			public boolean isPaged() {
				// Never paged in table mode
				return false;
			}

			@Override
			public boolean isMenuBarColumn() {
				return ViewableRecordVOTablePanel.this.isMenuBarColumn();
			}

			@Override
			public boolean isSortPersisted() {
				return ViewableRecordVOTablePanel.this.isSortPersisted();
			}

			@Override
			public void containerItemSetChange(ItemSetChangeEvent event) {
				super.containerItemSetChange(event);
				if (allItemsVisible) {
					adjustResultsTableHeight();
				}
				int newSize = size();
				if (selectDeselectAllToggleButton != null) {
					selectDeselectAllToggleButton.setVisible(newSize > 0);
				}
				if (countLabel != null) {
					countLabel.setVisible(newSize > 0);
				}
			}
		};
		resultsTable.setWidth("100%");
		resultsTable.addStyleName("viewable-record-table-table-mode");

		if (!tableModeVisibleColumns.isEmpty()) {
			resultsTable.setVisibleColumns(tableModeVisibleColumns.toArray(new Object[0]));
		}
		for (Object propertyId : tableModeColumnHeaders.keySet()) {
			resultsTable.setColumnHeader(propertyId, tableModeColumnHeaders.get(propertyId));
		}
		for (Object propertyId : tableModeColumnExpandRatios.keySet()) {
			resultsTable.setColumnExpandRatio(propertyId, tableModeColumnExpandRatios.get(propertyId));
		}
		if (allItemsVisible) {
			resultsTable.setPageLength(resultsTable.size());
		}

		final CellStyleGenerator cellStyleGenerator = resultsTable.getCellStyleGenerator();
		resultsTable.setCellStyleGenerator(new CellStyleGenerator() {
			@Override
			public String getStyle(Table source, Object itemId, Object propertyId) {
				String baseStyle = cellStyleGenerator != null ? cellStyleGenerator.getStyle(source, itemId, propertyId) : "";
				if (StringUtils.isNotBlank(baseStyle)) {
					baseStyle += " ";
				}
				String tableModeName = tableMode == TableMode.LIST ? "listMode" : "tableMode";
				baseStyle += "viewer-results-table-" + tableModeName + " ";
				baseStyle += "viewer-results-table-" + propertyId + " ";
				baseStyle += "viewer-results-table-" + tableModeName + "-" + propertyId + " ";
				return baseStyle + "viewer-results-table-row-" + itemId;
			}
		});

		for (ItemClickListener listener : itemClickListeners) {
			resultsTable.addItemClickListener(listener);
		}
		for (SelectionChangeListener listener : selectionChangeListeners) {
			resultsTable.addSelectionChangeListener(listener);
		}
		resultsTable.removeStyleName(RecordVOTable.CLICKABLE_ROW_STYLE_NAME);
		//resultsTable.setAlwaysRecalculateColumnWidths(true);

		if (isRowDragSupported()) {
			resultsTable.setDragMode(TableDragMode.ROW);
			resultsTable.setDropHandler(this);
		}

		return resultsTable;
	}

	protected int getSelectedSize() {
		return table.getSelectionManager().getAllSelectedItemIds().size();
	}

	public boolean isIndexVisible() {
		boolean indexVisible;
		if (recordVOContainer != null) {
			ModelLayerFactory modelLayerFactory = ConstellioUI.getCurrent().getConstellioFactories().getModelLayerFactory();
			int size = recordVOContainer.size();
			int maxSelectableResults = modelLayerFactory.getSystemConfigurationsManager().getValue(ConstellioEIMConfigs.MAX_SELECTABLE_SEARCH_RESULTS);
			if (getTableMode() == TableMode.LIST) {
				boolean showResultsNumberingInListView = modelLayerFactory.getSystemConfigs().isShowResultsNumberingInListView();
				if (isPagedInListMode()) {
					boolean alwaysSelectIntervals = modelLayerFactory.getSystemConfigurationsManager().getValue(ConstellioEIMConfigs.ALWAYS_SELECT_INTERVALS);
					indexVisible = showResultsNumberingInListView || alwaysSelectIntervals || size > maxSelectableResults;
				} else {
					indexVisible = showResultsNumberingInListView || size > maxSelectableResults;
				}
			} else {
				indexVisible = true;
			}
		} else {
			indexVisible = false;
		}
		return indexVisible;
	}

	public boolean isMenuBarColumn() {
		return true;
	}

	public boolean isSortPersisted() {
		return true;
	}

	public TableMode getTableMode() {
		return tableMode;
	}

	public void setTableMode(TableMode tableMode) {
		if (tableMode == null) {
			tableMode = TableMode.LIST;
		}
		if (tableMode != this.tableMode) {
			this.tableMode = tableMode;
			if (table != null) {
				if (tableMode == TableMode.TABLE) {
					closePanel();
				}

				BaseTable tableBefore = table;
				table = buildResultsTable();
				if (tableBefore != null) {
					table.setValue(tableBefore.getValue());
					for (SelectionChangeListener selectionChangeListener : tableBefore.getSelectionChangeListeners()) {
						table.addSelectionChangeListener(selectionChangeListener);
					}
				}

				tableLayout.replaceComponent(tableBefore, table);
				if (pagingControls != null) {
					ConstellioUI.getCurrent().setStaticFooterContent(null);
				}
				if (table.isPaged()) {
					pagingControls = table.createPagingControls();
					if (this.itemsPerPage != null) {
						pagingControls.setItemsPerPageValue(this.itemsPerPage);
					}
					if (!ConstellioUI.getCurrent().isNested()) {
						ConstellioUI.getCurrent().setStaticFooterContent(pagingControls);
					} else {
						tableLayout.addComponent(pagingControls);
					}
				}
				adjustTableExpansion();

				updateTableButtonsAfterTableModeChange(tableBefore);
			}

			TableModeChangeEvent event = new TableModeChangeEvent(tableMode, null);
			for (TableModeChangeListener listener : tableModeChangeListeners) {
				listener.tableModeChanged(event);
			}
		}
	}

	protected SelectDeselectAllButton newSelectDeselectAllToggleButton() {
		return table.newSelectDeselectAllToggleButton("", "");
	}

	private void updateTableButtonsAfterTableModeChange(BaseTable tableBefore) {
		if (isSelectColumn()) {
			SelectDeselectAllButton selectDeselectAllToggleButtonBefore = selectDeselectAllToggleButton;
			selectDeselectAllToggleButton = newSelectDeselectAllToggleButton();
			selectDeselectAllToggleButton.addStyleName(ValoTheme.BUTTON_LINK);
			//			if (selectDeselectAllToggleButtonBefore != null && selectDeselectAllToggleButtonBefore.isSelectAllMode() != selectDeselectAllToggleButton.isSelectAllMode()) {
			//				selectDeselectAllToggleButton.setSelectAllMode(selectDeselectAllToggleButtonBefore.isSelectAllMode());
			//			}
			selectionButtonsLayout.replaceComponent(selectDeselectAllToggleButtonBefore, selectDeselectAllToggleButton);
		}
	}

	void rowClicked(ItemClickEvent event) {
		Object itemId = event.getItemId();
		selectRecordVO(itemId, event, false);
		if (isCompressionSupported()) {
			previousButton.setVisible(itemId != null);
			nextButton.setVisible(itemId != null);
			if (isHideQuickActionButtonsOnSelection()) {
				setQuickActionButtonsVisible(false);
			}
		}
//		String rowScrollStyleClass = "viewer-results-table-row-" + recordVOContainer.indexOfId(itemId);
//		JavaScript.getCurrent().execute("document.getElementById('" + rowScrollStyleClass + "').scrollIntoView();");
	}

	boolean isSelected(Object itemId) {
		return selectedItemId != null && selectedItemId.equals(itemId);
	}

	Object getSelectedItemId() {
		return selectedItemId;
	}

	public RecordVO getRecordVO(Object itemId) {
		return recordVOContainer.getRecordVO(itemId);
	}

	public List<MetadataSchemaVO> getSchemas() {
		return recordVOContainer.getSchemas();
	}

	private void displayRecordVOInWindow(RecordVO recordVO) {
		String schemaTypeCode = recordVO.getSchema().getTypeCode();

		try {
			ViewWindow viewWindowFromExt = ConstellioUI.getCurrent().getConstellioFactories().getAppLayerFactory().getExtensions().getSystemWideExtensions().getWindowDisplay(new SchemaDisplayParams(schemaTypeCode, recordVO, searchTerm, ViewableRecordVOTablePanel.this));
			if (viewWindowFromExt != null) {
				viewWindow = viewWindowFromExt;
			} else {
				viewWindow = new DisplaySchemaRecordWindow(recordVO);
			}

			viewWindow.addCloseListener(new Window.CloseListener() {
				@Override
				public void windowClose(CloseEvent e) {
					if (getPanelContent() != null) {
						if (tableMode == TableMode.LIST) {
							refreshMetadata();
						}
						recordVOContainer.forceRefresh();
					} else {
						selectRecordVO(null, null, true);
					}
				}
			});
			ConstellioUI.getCurrent().addWindow(viewWindow);
		} catch (UserDoesNotHaveAccessException e) {
			log.error(e.getMessage(), e);
		}
	}

	@Override
	public boolean beforeViewChange(ViewChangeEvent event) {
		if (viewWindow != null) {
			viewWindow.close();
		}
		return true;
	}

	@Override
	public void afterViewChange(ViewChangeEvent event) {
	}

	private void navigateToRecordVO(RecordVO recordVO) {
		AppLayerFactory appLayerFactory = ConstellioUI.getCurrent().getConstellioFactories().getAppLayerFactory();
		AppLayerCollectionExtensions extensions = appLayerFactory.getExtensions().forCollection(recordVO.getRecord().getCollection());
		boolean navigationHandledByExtensions = extensions.navigateFromViewerToRecordVO(new ViewableRecordVOTablePanelExtensionParams(recordVO, searchTerm, this));
		if (!navigationHandledByExtensions) {
			new ReferenceDisplay(recordVO).click();
		}
	}

	protected boolean isDisplayInWindowOnSelection(RecordVO recordVO) {
		Boolean displayInWindowOnSelection;

		AppLayerFactory appLayerFactory = ConstellioUI.getCurrent().getConstellioFactories().getAppLayerFactory();
		AppLayerCollectionExtensions extensions = appLayerFactory.getExtensions().forCollection(recordVO.getRecord().getCollection());
		displayInWindowOnSelection = extensions.isDisplayInWindowOnSelection(new ViewableRecordVOTablePanelExtensionParams(recordVO, searchTerm, this));

		if (displayInWindowOnSelection == null) {
			displayInWindowOnSelection = !ResponsiveUtils.isDesktop();
		}
		return displayInWindowOnSelection;
	}

	private void displayInWindowOrNavigate(RecordVO recordVO) {
		if (isDisplayInWindowOnSelection(recordVO)) {
			displayRecordVOInWindow(recordVO);
		} else {
			navigateToRecordVO(recordVO);
		}
	}

	protected boolean isSelectionPossible(RecordVO recordVO) {
		Boolean selectionPossible;
		if (recordVO != null) {
			AppLayerFactory appLayerFactory = ConstellioUI.getCurrent().getConstellioFactories().getAppLayerFactory();
			AppLayerCollectionExtensions extensions = appLayerFactory.getExtensions().forCollection(recordVO.getRecord().getCollection());
			selectionPossible = extensions.isViewerSelectionPossible(new ViewableRecordVOTablePanelExtensionParams(recordVO, searchTerm, this));
		} else {
			selectionPossible = false;
		}
		return selectionPossible != null ? selectionPossible.booleanValue() : true;
	}

	void selectRecordVO(Object itemId, ItemClickEvent event, boolean reload) {
		Object newSelectedItemId = itemId;
		table.setValue(newSelectedItemId);
		table.refreshRenderedCells();

		Boolean compressionChange;
		if (selectedItemId == null && newSelectedItemId != null) {
			compressionChange = true;
		} else {
			compressionChange = null;
		}

		boolean selectedItemIdChanged;
		if (newSelectedItemId == null && this.selectedItemId != null) {
			selectedItemIdChanged = true;
		} else if (newSelectedItemId != null && !newSelectedItemId.equals(this.selectedItemId)) {
			selectedItemIdChanged = true;
		} else {
			selectedItemIdChanged = false;
		} 
		if (selectedItemIdChanged || reload) {
			this.selectedItemId = newSelectedItemId;
			if (reload) {
				recordVOContainer.forceRefresh();
			}

			if (this.selectedItemId != null) {
				selectedRecordVO = getRecordVO(selectedItemId);
			} else {
				selectedRecordVO = null;
			}
			if (selectedRecordVO == null) {
				previousItemId = null;
				nextItemId = null;
			} else {
				previousItemId = recordVOContainer.prevItemId(itemId);
				nextItemId = recordVOContainer.nextItemId(itemId);

				if ((!isCompressionSupported() && selectedRecordVO != null)
					|| (selectedRecordVO != null && !isSelectionPossible(selectedRecordVO))) {
					displayInWindowOrNavigate(selectedRecordVO);
				} else {
					previousButton.setEnabled(previousItemId != null);
					nextButton.setEnabled(nextItemId != null);

					viewerMetadataPanel.setRecordVO(selectedRecordVO);
					if (compressionChange != null && event != null) {
						TableCompressEvent tableCompressEvent = new TableCompressEvent(event, compressionChange);
						for (TableCompressListener tableCompressListener : tableCompressListeners) {
							tableCompressListener.tableCompressChange(tableCompressEvent);
						}
					}
					adjustTableExpansion();
				}
			}
		}
	}

	public void closePanel() {
		selectRecordVO(null, null, false);

		TableCompressEvent tableCompressEvent = new TableCompressEvent(null, false);
		for (TableCompressListener tableCompressListener : tableCompressListeners) {
			tableCompressListener.tableCompressChange(tableCompressEvent);
		}

		adjustTableExpansion();
		previousButton.setVisible(false);
		nextButton.setVisible(false);
		if (isHideQuickActionButtonsOnSelection()) {
			setQuickActionButtonsVisible(true);
		}
	}

	protected boolean isSelectColumn() {
		return false;
	}

	protected SelectionManager newSelectionManager() {
		return null;
	}

	protected boolean isPagedInListMode() {
		return false;
	}

	private ViewerMetadataPanel buildViewerMetadataPanel() {
		return new ViewerMetadataPanel();
	}

	private ListModeButton buildListModeButton() {
		return new ListModeButton();
	}

	private TableModeButton buildTableModeButton() {
		return new TableModeButton();
	}

	private BaseButton buildPreviousButton() {
		String caption = $("ViewableRecordVOTablePanel.previous");
		Resource icon;
		if (isRightToLeft()) {
			icon = FontAwesome.CHEVRON_RIGHT;
		} else {
			icon = FontAwesome.CHEVRON_LEFT;
		}
		BaseButton previousButton = new IconButton(icon, caption) {
			@Override
			protected void buttonClick(ClickEvent event) {
				if (previousItemId != null) {
					selectRecordVO(previousItemId, null, false);
				}
			}
		};
		previousButton.addStyleName("chevron");
		previousButton.addStyleName("previous-button");
		previousButton.setWidth("16px");
		previousButton.addExtension(new NiceTitle(caption, false));
		previousButton.setVisible(false);
		return previousButton;
	}

	private BaseButton buildNextButton() {
		String caption = $("ViewableRecordVOTablePanel.next");
		Resource icon;
		if (isRightToLeft()) {
			icon = FontAwesome.CHEVRON_LEFT;
		} else {
			icon = FontAwesome.CHEVRON_RIGHT;
		}
		BaseButton nextButton = new IconButton(icon, caption) {
			@Override
			protected void buttonClick(ClickEvent event) {
				if (nextItemId != null) {
					selectRecordVO(nextItemId, null, false);
				}
			}
		};
		nextButton.addStyleName("chevron");
		nextButton.addStyleName("next-button");
		nextButton.setWidth("16px");
		nextButton.addExtension(new NiceTitle(caption, false));
		nextButton.setVisible(false);
		return nextButton;
	}

	private BaseButton buildCloseViewerButton() {
		BaseButton closeViewerButton = new IconButton(FontAwesome.TIMES, $("ViewableRecordVOTablePanel.closeViewer")) {
			@Override
			protected void buttonClick(ClickEvent event) {
				closePanel();
			}
		};
		closeViewerButton.setId("close-viewer-button");
		closeViewerButton.addStyleName(closeViewerButton.getId());
		return closeViewerButton;
	}

	public void refreshMetadata() {
		RecordServices recordServices = ConstellioUI.getCurrent().getConstellioFactories().getModelLayerFactory().newRecordServices();
		Record selectedRecord = recordServices.getDocumentById(selectedRecordVO.getId());
		selectedRecordVO = new RecordToVOBuilder().build(selectedRecord, VIEW_MODE.DISPLAY, ConstellioUI.getCurrentSessionContext());
		selectRecordVO(selectedItemId, null, true);
	}

	public List<TableCompressListener> getTableCompressListeners() {
		return tableCompressListeners;
	}

	public void addTableCompressListener(TableCompressListener listener) {
		if (isCompressionSupported() && !tableCompressListeners.contains(listener)) {
			tableCompressListeners.add(listener);
		}
	}

	public void removeTableCompressListener(TableCompressListener listener) {
		tableCompressListeners.remove(listener);
	}

	public List<TableModeChangeListener> getTableModeChangeListeners() {
		return tableModeChangeListeners;
	}

	public void addTableModeChangeListener(TableModeChangeListener listener) {
		if (!tableModeChangeListeners.contains(listener)) {
			tableModeChangeListeners.add(listener);
		}
	}

	public void removeTableModeChangeListener(TableModeChangeListener listener) {
		tableModeChangeListeners.remove(listener);
	}

	public List<ItemClickListener> getItemClickListeners() {
		return itemClickListeners;
	}

	public void addItemClickListener(ItemClickListener listener) {
		if (!itemClickListeners.contains(listener)) {
			itemClickListeners.add(listener);
			if (table != null) {
				table.addItemClickListener(listener);
			}
		}
	}

	public void removeItemClickListener(ItemClickListener listener) {
		itemClickListeners.remove(listener);
		if (table != null) {
			table.removeItemClickListener(listener);
		}
	}

	public List<SelectionChangeListener> getSelectionChangeListeners() {
		return selectionChangeListeners;
	}

	public void addSelectionChangeListener(SelectionChangeListener listener) {
		if (!selectionChangeListeners.contains(listener)) {
			selectionChangeListeners.add(listener);
			if (table != null) {
				table.addSelectionChangeListener(listener);
			}
		}
	}

	public void removeSelectionChangeListener(SelectionChangeListener listener) {
		selectionChangeListeners.remove(listener);
		if (table != null) {
			table.removeSelectionChangeListener(listener);
		}
	}

	public HorizontalLayout createPagingControls() {
		return table.createPagingControls();
	}

	public void setItemsPerPage(int value) {
		this.itemsPerPage = value;
		if (table.isPaged()) {
			table.setItemsPerPage(value);
			pagingControls.setItemsPerPageValue(value);
		}
	}

	public void setCurrentPage(int value) {
		this.currentPage = value;
		if (table.isPaged()) {
			table.setCurrentPage(value);
		}
	}

	public List<PageChangeListener> getPageChangeListeners() {
		return pageChangeListeners;
	}

	public void addPageChangeListener(PageChangeListener listener) {
		if (!pageChangeListeners.contains(listener)) {
			pageChangeListeners.add(listener);
			table.addPageChangeListener(listener);
		}
	}

	public void removePageChangeListener(PageChangeListener listener) {
		pageChangeListeners.remove(listener);
		table.removePageChangeListener(listener);
	}

	public List<ItemsPerPageChangeListener> getItemsPerPageChangeListeners() {
		return itemsPerPageChangeListeners;
	}

	public void addItemsPerPageChangeListener(ItemsPerPageChangeListener listener) {
		if (!itemsPerPageChangeListeners.contains(listener)) {
			itemsPerPageChangeListeners.add(listener);
			table.addItemsPerPageChangeListener(listener);
		}
	}

	public void removeItemsPerPageChangeListener(ItemsPerPageChangeListener listener) {
		itemsPerPageChangeListeners.remove(listener);
		table.removeItemsPerPageChangeListener(listener);
	}

	public void select(Object itemId) {
		table.select(itemId);
	}

	public void deselect(Object itemId) {
		table.deselect(itemId);
	}

	public void selectAll() {
		table.selectAll();
	}

	public void deselectAll() {
		table.deselectAll();
	}

	public void selectCurrentPage() {
		table.selectCurrentPage();
	}

	public void deselectCurrentPage() {
		table.deselectCurrentPage();
	}

	protected boolean contextMenuOpened(ContextMenu contextMenu, Object itemId) {
		return false;
	}

	public void setVisibleColumns(Object... visibleColumns) {
		if (visibleColumns != null) {
			tableModeVisibleColumns = Arrays.asList(visibleColumns);
		} else {
			tableModeVisibleColumns = Collections.emptyList();
		}
		if (table != null && tableMode == TableMode.TABLE) {
			table.setVisibleColumns(visibleColumns);
		}
	}

	public void setColumnHeader(Object propertyId, String header) {
		tableModeColumnHeaders.put(propertyId, header);
		if (table != null && tableMode == TableMode.TABLE) {
			table.setColumnHeader(propertyId, header);
		}
	}

	public void setColumnExpandRatio(Object propertyId, int expandRatio) {
		tableModeColumnExpandRatios.put(propertyId, expandRatio);
		if (table != null && tableMode == TableMode.TABLE) {
			table.setColumnExpandRatio(propertyId, expandRatio);
		}
	}

	protected RecordVO getRecordVOForTitleColumn(Item item) {
		return null;
	}

	public BaseTable getActualTable() {
		return table;
	}

	public boolean scrollIntoView(Integer itemIndex, String recordId) {
		boolean scrolledIntoView;
		if (itemIndex < recordVOContainer.size()) {
			List<?> itemIds = recordVOContainer.getItemIds(itemIndex, 1);
			if (!itemIds.isEmpty()) {
				Object itemId = itemIds.get(0);
				RecordVO recordVO = recordVOContainer.getRecordVO(itemIndex);
				if (recordVO != null && recordVO.getId().equals(recordId)) {
					table.setCurrentPageFirstItemIndex(itemIndex);
					if (isCompressionSupported()) {
						selectRecordVO(itemId, null, false);
					}
					scrolledIntoView = true;
				} else {
					scrolledIntoView = false;
				}
			} else {
				scrolledIntoView = false;
			}
		} else {
			scrolledIntoView = false;
		}
		return scrolledIntoView;
	}

	@Override
	public void browserWindowResized(BrowserWindowResizeEvent event) {
		// TODO Auto-generated method stub
	}

	public BaseView getMainView() {
		return null;
	}

	public boolean isDropSupported() {
		return selectedItemId != null && viewerMetadataPanel.getPanelContent() instanceof DropHandler;
	}

	public boolean isRowDragSupported() {
		return false;
	}

	@Override
	public void drop(DragAndDropEvent event) {
		// Limitation: https://vaadin.com/forum/thread/986110/table-drag-and-drop-with-layout-in-a-cell-doesn-t-work
		if (event.getTargetDetails() instanceof AbstractSelectTargetDetails) {
			Transferable t = event.getTransferable();
			if (t.getSourceComponent() != table || table.size() <= 1) {
				return;
			}

			AbstractSelectTargetDetails target = (AbstractSelectTargetDetails) event.getTargetDetails();
			Object sourceItemId = t.getData("itemId");
			Object targetItemId = target.getItemIdOver();

			Boolean above;
			if (target.getDropLocation().equals(VerticalDropLocation.TOP)) {
				above = true;
			} else if (target.getDropLocation().equals(VerticalDropLocation.MIDDLE) && targetItemId.equals(table.firstItemId())) {
				above = true;
			} else {
				above = false;
			}
			RecordVO sourceRecordVO = recordVOContainer.getRecordVO(sourceItemId);
			RecordVO targetRecordVO = recordVOContainer.getRecordVO(targetItemId);

			List<RecordVO> droppedRecordVOs = new ArrayList<>(getSelectedRecordVOs());
			if (!droppedRecordVOs.contains(sourceRecordVO)) {
				droppedRecordVOs.add(0, sourceRecordVO);
			}
			droppedRecordVOs.remove(targetRecordVO);
			recordsDroppedOn(droppedRecordVOs, targetRecordVO, above);
		} else {
			Component panelContent = viewerMetadataPanel.getPanelContent();
			if (panelContent instanceof DropHandler) {
				((DropHandler) panelContent).drop(event);
			}
		}
	}

	protected void recordsDroppedOn(List<RecordVO> sourceRecordVOs, RecordVO targetRecordVO, Boolean above) {
	}

	@Override
	public AcceptCriterion getAcceptCriterion() {
		return AcceptAll.get();
	}

	private String getPanelId() {
		List<MetadataSchemaVO> schemaVOs = recordVOContainer.getSchemas();

		StringBuilder schemaVOSuffix = new StringBuilder();
		for (MetadataSchemaVO schemaVO : schemaVOs) {
			if (schemaVOSuffix.length() > 0) {
				schemaVOSuffix.append("_");
			}
			schemaVOSuffix.append(schemaVO.getCode());
		}
		String navigatorState = ConstellioUI.getCurrent().getNavigator().getState();
		String navigatorStateWithoutParams;
		if (navigatorState.contains("/")) {
			navigatorStateWithoutParams = StringUtils.substringBefore(navigatorState, "/");
		} else {
			navigatorStateWithoutParams = navigatorState;
		}
		return navigatorStateWithoutParams + "." + schemaVOSuffix;
	}

	public Component getPanelContent() {
		return viewerMetadataPanel != null ? viewerMetadataPanel.getPanelContent() : null;
	}

	public RecordVO getPanelRecordVO() {
		return selectedRecordVO;
	}

	public Integer getPanelRecordIndex() {
		return selectedRecordVO != null ? recordVOContainer.indexOfId(selectedItemId) : null;
	}

	public Button getCloseViewerButton() {
		return closeViewerButton;
	}

	private class ViewerMetadataPanel extends VerticalLayout {

		private VerticalLayout mainLayout;
		private Component panelContent;

		public ViewerMetadataPanel() {
			buildUI();
		}

		private void setRecordVO(RecordVO recordVO) {
			mainLayout.removeAllComponents();
			this.removeStyleName("nested-view");

			if (recordVO != null) {
				String schemaTypeCode = recordVO.getSchema().getTypeCode();
				Component displayComponent = ConstellioUI.getCurrent().getConstellioFactories().getAppLayerFactory().getExtensions().getSystemWideExtensions().getSchemaDisplay(new SchemaDisplayParams(schemaTypeCode, recordVO, searchTerm, ViewableRecordVOTablePanel.this));
				if (displayComponent != null) {
					panelContent = displayComponent;
				} else {
					UserVO currentUser = ConstellioUI.getCurrentSessionContext().getCurrentUser();
					panelContent = new RecordDisplayFactory(currentUser).build(recordVO, true);
					this.addStyleName("nested-view");
				}
				if (panelContent instanceof DropHandler) {
					DragAndDropWrapper dragAndDropWrapper = new DragAndDropWrapper(panelContent) {
						@Override
						public void setDropHandler(DropHandler dropHandler) {
							if (ResponsiveUtils.isDesktop()) {
								super.setDropHandler(dropHandler);
							}
						}
					};
					dragAndDropWrapper.setSizeFull();
					dragAndDropWrapper.setDropHandler((DropHandler) panelContent);
					mainLayout.addComponent(dragAndDropWrapper);
				} else {
					mainLayout.addComponent(panelContent);
				}
				Label spacer = new Label("");
				spacer.setHeight("100px");
				mainLayout.addComponent(spacer);
				adjustViewerPanelPositionToScroll();
			} else {
				panelContent = null;
			}
		}

		private void adjustViewerPanelPositionToScroll() {
			String js = "setTimeout(function(){ adjustViewerPanelPositionToScroll(); }, 100);";
			Page.getCurrent().getJavaScript().execute(js);
		}

		private void buildUI() {
			setId(UUID.randomUUID().toString());
			setWidth("100%");
			addStyleName(ValoTheme.PANEL_BORDERLESS);
			addStyleName("viewer-metadata-panel");

			mainLayout = new VerticalLayout();
			mainLayout.addStyleName("viewer-metadata-panel-main-layout");
			mainLayout.setSizeFull();
			addComponent(mainLayout);
		}

		@Override
		public void beforeClientResponse(boolean initial) {
			super.beforeClientResponse(initial);
			//			adjustHeight();
		}

		public Component getPanelContent() {
			return panelContent;
		}

	}

	public class TableCompressEvent implements Serializable {

		private final ItemClickEvent itemClickEvent;

		private final boolean compressed;

		public TableCompressEvent(ItemClickEvent itemClickEvent, boolean compressed) {
			this.itemClickEvent = itemClickEvent;
			this.compressed = compressed;
		}

		public ItemClickEvent getItemClickEvent() {
			return itemClickEvent;
		}

		public boolean isCompressed() {
			return compressed;
		}

	}

	public interface TableCompressListener extends Serializable {

		void tableCompressChange(TableCompressEvent event);

	}

	public class TableModeChangeEvent implements Serializable {

		private Component component;

		private TableMode tableMode;

		public TableModeChangeEvent(TableMode tableMode, Component component) {
			this.tableMode = tableMode;
			this.component = component;
		}

		public TableMode getTableMode() {
			return tableMode;
		}

		public Component getComponent() {
			return component;
		}
	}

	public interface TableModeChangeListener extends Serializable {

		void tableModeChanged(TableModeChangeEvent event);

	}

	private class ListModeButton extends IconButton {

		public ListModeButton() {
			super(FontAwesome.ALIGN_JUSTIFY, $("ViewableRecordVOTablePanel.tableMode.list"));
			addStyleName(ValoTheme.BUTTON_LINK);
			addStyleName("list-mode-button");
			updateState();
			if (!isEnabled()) {
				saveTableMode();
			}
			addTableModeChangeListener(new TableModeChangeListener() {
				@Override
				public void tableModeChanged(TableModeChangeEvent event) {
					updateState();
				}
			});
		}

		private void updateState() {
			setEnabled(tableMode == TableMode.TABLE);
		}

		private void saveTableMode() {
			tableModeManager.saveTableModeForCurrentUser(panelId, tableMode);
		}

		@Override
		protected void buttonClick(ClickEvent event) {
			setTableMode(TableMode.LIST);
			saveTableMode();
		}

	}

	private class TableModeButton extends IconButton {

		public TableModeButton() {
			super(FontAwesome.TH, $("ViewableRecordVOTablePanel.tableMode.table"));
			addStyleName(ValoTheme.BUTTON_LINK);
			addStyleName("table-mode-button");
			updateState();
			if (!isEnabled()) {
				saveTableMode();
			}
			addTableModeChangeListener(new TableModeChangeListener() {
				@Override
				public void tableModeChanged(TableModeChangeEvent event) {
					updateState();
				}
			});
		}

		private void updateState() {
			setEnabled(tableMode == TableMode.LIST);
		}

		private void saveTableMode() {
			tableModeManager.saveTableModeForCurrentUser(panelId, tableMode);
		}

		@Override
		protected void buttonClick(ClickEvent event) {
			setTableMode(TableMode.TABLE);
			saveTableMode();
		}

	}
}
