package com.constellio.app.ui.framework.components.viewers.panel;

import static com.constellio.app.ui.i18n.i18n.$;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import com.constellio.app.modules.rm.ui.pages.document.DisplayDocumentViewImpl;
import com.constellio.app.modules.rm.wrappers.Document;
import com.constellio.app.modules.rm.wrappers.Folder;
import com.constellio.app.ui.application.ConstellioUI;
import com.constellio.app.ui.entities.ContentVersionVO;
import com.constellio.app.ui.entities.RecordVO;
import com.constellio.app.ui.entities.RecordVO.VIEW_MODE;
import com.constellio.app.ui.entities.SearchResultVO;
import com.constellio.app.ui.entities.UserVO;
import com.constellio.app.ui.framework.builders.RecordToVOBuilder;
import com.constellio.app.ui.framework.buttons.BaseButton;
import com.constellio.app.ui.framework.buttons.DisplayButton;
import com.constellio.app.ui.framework.buttons.IconButton;
import com.constellio.app.ui.framework.components.BaseCustomComponent;
import com.constellio.app.ui.framework.components.BaseWindow;
import com.constellio.app.ui.framework.components.RecordDisplay;
import com.constellio.app.ui.framework.components.RecordDisplayFactory;
import com.constellio.app.ui.framework.components.SearchResultDisplay;
import com.constellio.app.ui.framework.components.display.ReferenceDisplay;
import com.constellio.app.ui.framework.components.layouts.I18NHorizontalLayout;
import com.constellio.app.ui.framework.components.table.RecordVOTable;
import com.constellio.app.ui.framework.components.viewers.ContentViewer;
import com.constellio.app.ui.framework.containers.ContainerAdapter;
import com.constellio.app.ui.framework.containers.RecordVOLazyContainer;
import com.constellio.app.ui.framework.containers.SearchResultContainer;
import com.constellio.app.ui.framework.containers.SearchResultVOLazyContainer;
import com.constellio.model.entities.records.Record;
import com.constellio.model.services.records.RecordServices;
import com.constellio.model.services.schemas.SchemaUtils;
import com.jensjansson.pagedtable.PagedTableContainer;
import com.vaadin.data.Container;
import com.vaadin.event.ItemClickEvent;
import com.vaadin.event.ItemClickEvent.ItemClickListener;
import com.vaadin.server.Page;
import com.vaadin.server.ThemeResource;
import com.vaadin.ui.Alignment;
import com.vaadin.ui.Component;
import com.vaadin.ui.Panel;
import com.vaadin.ui.Table;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.Window.CloseEvent;
import com.vaadin.ui.Window.CloseListener;

//@JavaScript({ "theme://jquery/jquery-2.1.4.min.js", "theme://scroll/fix-vertical-scroll.js" })
public class ViewableRecordTablePanel extends I18NHorizontalLayout {
	
	private VerticalLayout tableLayout;
	
	private VerticalLayout buttonsViewerMetadataLayout;
	
	private Component tableComponent;
	
	private Table table;
	
	private ViewerMetadataPanel viewerMetadataPanel;
	
	private I18NHorizontalLayout buttonsLayout;
	
	private BaseButton previousButton;
	
	private BaseButton nextButton;
	
	private BaseButton closeViewerButton;
	
	private BaseButton displayButton;
	
	private BaseCustomComponent openDocumentComponent;
	
	private Integer rowSelected;
	
	private RecordVO selectedRecordVO;
	
	private Object previousItemId;
	
	private Object nextItemId;
	
	private List<TableCompressListener> tableCompressListeners = new ArrayList<>();

	public ViewableRecordTablePanel(Component resultsComponent) {
		this.tableComponent = resultsComponent;
		buildUI();
	}
	
	private void buildUI() {
		setSizeFull();
		setSpacing(true);
		
		addStyleName("viewable-record-table-panel");
		
		buildResultsTable();
		buildViewerMetadataPanel();
		buildPreviousButton();
		buildNextButton();
		buildDisplayButton();
		buildOpenDocumentComponent();
		buildCloseViewerButton();
		
		tableLayout = new VerticalLayout();
		if (tableComponent != null) {
			tableLayout.addComponent(tableComponent);
		} else {
			tableLayout.addComponent(table);
		}
		
		buttonsLayout = new I18NHorizontalLayout(previousButton, nextButton, displayButton, openDocumentComponent, closeViewerButton);
		buttonsLayout.setWidth("100%");
		buttonsLayout.setSpacing(true);
		buttonsLayout.setComponentAlignment(previousButton, Alignment.TOP_LEFT);
		buttonsLayout.setComponentAlignment(nextButton, Alignment.TOP_LEFT);
		buttonsLayout.setComponentAlignment(displayButton, Alignment.TOP_LEFT);
		buttonsLayout.setComponentAlignment(openDocumentComponent, Alignment.TOP_LEFT);
		buttonsLayout.setComponentAlignment(closeViewerButton, Alignment.TOP_RIGHT);
		buttonsLayout.setExpandRatio(displayButton, 1);
		buttonsLayout.setExpandRatio(openDocumentComponent, 1);
		
		buttonsViewerMetadataLayout = new VerticalLayout(buttonsLayout, viewerMetadataPanel);
		buttonsViewerMetadataLayout.addStyleName("close-button-viewer-metadata-layout");
		buttonsViewerMetadataLayout.setId("close-button-viewer-metadata-layout");
		buttonsViewerMetadataLayout.setHeight("100%");
//		closeButtonViewerMetadataLayout.setWidthUndefined();
		
		tableLayout.setHeight("100%");
		
		addComponents(tableLayout, buttonsViewerMetadataLayout);
		
		adjustTableExpansion();
	}
	
	void adjustTableExpansion() {
		String compressedStyleName = "viewable-record-table-compressed";
		if (rowSelected != null) {
			if (!buttonsViewerMetadataLayout.isVisible()) {
				if (table != null) {
					table.addStyleName(compressedStyleName);
				}
				buttonsViewerMetadataLayout.setVisible(true);
				tableLayout.setWidth("650px");
				setExpandRatio(tableLayout, 0);
				setExpandRatio(buttonsViewerMetadataLayout, 1);
			}
		} else {
			if (buttonsViewerMetadataLayout.isVisible()) {
				if (table != null) {
					table.removeStyleName(compressedStyleName);
				}	
				buttonsViewerMetadataLayout.setVisible(false);
				tableLayout.setWidth("100%");
				setExpandRatio(tableLayout, 1);
				setExpandRatio(buttonsViewerMetadataLayout, 0);
			}
		}
	}
	
	public void setTable(Table resultsTable) {
		this.table = resultsTable;
		resultsTable.addStyleName("viewable-record-table");
		resultsTable.addStyleName(RecordVOTable.CLICKABLE_ROW_STYLE_NAME);
		resultsTable.addItemClickListener(new ItemClickListener() {
			@Override
			public void itemClick(ItemClickEvent event) {
				rowClicked(event);
			}
		});
//		final CellStyleGenerator cellStyleGenerator = resultsTable.getCellStyleGenerator();
//		resultsTable.setCellStyleGenerator(new CellStyleGenerator() {
//			@Override
//			public String getStyle(Table source, Object itemId, Object propertyId) {
//				String baseStyle = cellStyleGenerator != null ? cellStyleGenerator.getStyle(source, itemId, propertyId) : "";
//				if (StringUtils.isNotBlank(baseStyle)) {
//					baseStyle += " ";
//				}
//				return baseStyle + "viewer-results-table-row-" + itemId;
//			}
//		});
	}
	
	void rowClicked(ItemClickEvent event) {
		Object itemId = event.getItemId();
		selectRecordVO(itemId, event);
	}	

	@SuppressWarnings("rawtypes")
	void selectRecordVO(Object itemId, ItemClickEvent event) {	
		Integer newRowSelected = (Integer) itemId;
		
		Boolean compressionChange;
		if (rowSelected == null && newRowSelected != null) {
			compressionChange = true;
		} else {
			compressionChange = null;
		}
		
		if (!newRowSelected.equals(this.rowSelected)) {
			rowSelected = newRowSelected;
			
			Container container = table.getContainerDataSource();
			while (container instanceof ContainerAdapter) {
				container = ((ContainerAdapter) container).getNestedContainer();
			}
			if (container instanceof RecordVOLazyContainer) {
				RecordVOLazyContainer recordVOLazyContainer = (RecordVOLazyContainer) container;
				selectedRecordVO = recordVOLazyContainer.getRecordVO(rowSelected);
				previousItemId = recordVOLazyContainer.prevItemId(itemId);
				nextItemId = recordVOLazyContainer.nextItemId(itemId);
			} else if (container instanceof SearchResultContainer) {
				SearchResultContainer searchResultContainer = (SearchResultContainer) container;
				selectedRecordVO = searchResultContainer.getRecordVO(rowSelected);
				previousItemId = searchResultContainer.prevItemId(itemId);
				nextItemId = searchResultContainer.nextItemId(itemId);
			} else if (container instanceof SearchResultVOLazyContainer) {
				SearchResultVOLazyContainer searchResultContainer = (SearchResultVOLazyContainer) container;
				selectedRecordVO = searchResultContainer.getRecordVO(rowSelected);
				previousItemId = searchResultContainer.prevItemId(itemId);
				nextItemId = searchResultContainer.nextItemId(itemId);
			} else if (container instanceof ViewableRecordVOContainer) {
				ViewableRecordVOContainer viewableRecordVOContainer = (ViewableRecordVOContainer) container;
				selectedRecordVO = viewableRecordVOContainer.getRecordVO(rowSelected);
				previousItemId = viewableRecordVOContainer.prevItemId(itemId);
				nextItemId = viewableRecordVOContainer.nextItemId(itemId);
			} else if (container instanceof PagedTableContainer) {
				PagedTableContainer pagedTableContainer = (PagedTableContainer) container;
				Container nestedContainer = pagedTableContainer.getContainer();
				if (nestedContainer instanceof RecordVOLazyContainer) {
					RecordVOLazyContainer recordVOLazyContainer = (RecordVOLazyContainer) nestedContainer;
					selectedRecordVO = recordVOLazyContainer.getRecordVO(rowSelected);
					previousItemId = recordVOLazyContainer.prevItemId(itemId);
					nextItemId = recordVOLazyContainer.nextItemId(itemId);
				} else if (nestedContainer instanceof SearchResultContainer) {
					SearchResultContainer searchResultContainer = (SearchResultContainer) nestedContainer;
					selectedRecordVO = searchResultContainer.getRecordVO(rowSelected);
					previousItemId = searchResultContainer.prevItemId(itemId);
					nextItemId = searchResultContainer.nextItemId(itemId);
				} else {
					selectedRecordVO = null;
					previousItemId = null;
					nextItemId = null;
				}	
			} else {
				selectedRecordVO = null;
				previousItemId = null;
				nextItemId = null;
			}
			previousButton.setEnabled(previousItemId != null);
			nextButton.setEnabled(nextItemId != null);
			
			viewerMetadataPanel.setRecordVO(selectedRecordVO);
			updateViewerActionButtons();
			
			if (compressionChange != null && event != null) {
				TableCompressEvent tableCompressEvent = new TableCompressEvent(event, compressionChange);
				for (TableCompressListener tableCompressListener : tableCompressListeners) {
					tableCompressListener.tableCompressChange(tableCompressEvent);
				}
			}
			
			adjustTableExpansion();
			
//			float spacerHeight = rowSelected * 100;
//			if (spacerHeight <= 0) {
//				closeButtonViewerMetadataSpacer.setVisible(false);
//			} else {
//				closeButtonViewerMetadataSpacer.setVisible(true);
//				closeButtonViewerMetadataSpacer.setHeight(spacerHeight, Unit.PIXELS);
//			}
		}
	}
	
	private void closeViewerButtonClicked() {
		rowSelected = null;
		
		TableCompressEvent tableCompressEvent = new TableCompressEvent(null, false);
		for (TableCompressListener tableCompressListener : tableCompressListeners) {
			tableCompressListener.tableCompressChange(tableCompressEvent);
		}
		
		adjustTableExpansion();
	}
	
	private void updateViewerActionButtons() {
		if (selectedRecordVO != null) {
			String schemaCode = selectedRecordVO.getSchema().getCode();
			String schemaTypeCode = SchemaUtils.getSchemaTypeCode(schemaCode);
			if (Document.SCHEMA_TYPE.equals(schemaTypeCode) && selectedRecordVO.get(Document.CONTENT) != null) {
				displayButton.setVisible(false);
			} else if (Document.SCHEMA_TYPE.equals(schemaTypeCode) || Folder.SCHEMA_TYPE.equals(schemaTypeCode)) {
				displayButton.setVisible(true);
			} else {
				displayButton.setVisible(false);
			}
		} else {
			displayButton.setVisible(false);
		}
		updateOpenDocumentComponent();
	}
	
	private void updateOpenDocumentComponent() {
		if (selectedRecordVO != null) {
			String schemaCode = selectedRecordVO.getSchema().getCode();
			String schemaTypeCode = SchemaUtils.getSchemaTypeCode(schemaCode);
			if (Document.SCHEMA_TYPE.equals(schemaTypeCode) && selectedRecordVO.get(Document.CONTENT) != null) {
				openDocumentComponent.setVisible(true);
				
				SearchResultVO searchResultVO = new SearchResultVO(selectedRecordVO, new HashMap<String, List<String>>());
				RecordDisplayFactory displayFactory = new RecordDisplayFactory(ConstellioUI.getCurrentSessionContext().getCurrentUser());
				SearchResultDisplay searchResultDisplay = displayFactory.build(searchResultVO, null, null, null, null);
				Component openDocumentLink = searchResultDisplay.getTitleComponent();
				openDocumentComponent.setCompositionRoot(openDocumentLink);
			} else {
				openDocumentComponent.setVisible(false);
			}
		} else {
			openDocumentComponent.setVisible(false);
		}
	}
	
	private void buildResultsTable() {
		if (tableComponent != null) {
			tableComponent.setWidth("100%");
		} else {
			table.setWidth("100%");
		}
	}
	
	private void buildViewerMetadataPanel() {
		viewerMetadataPanel = new ViewerMetadataPanel();
	}
	
	private void buildPreviousButton() {
		previousButton = new IconButton(new ThemeResource("images/icons/actions/navigate_left.png"), $("ViewableRecordTablePanel.previous")) {
			@Override
			protected void buttonClick(ClickEvent event) {
				if (previousItemId != null) {
					selectRecordVO(previousItemId, null);
				}
			}
		};
		previousButton.setWidth("24px");
	}
	
	private void buildNextButton() {
		nextButton = new IconButton(new ThemeResource("images/icons/actions/navigate_right.png"), $("ViewableRecordTablePanel.next")) {
			@Override
			protected void buttonClick(ClickEvent event) {
				if (nextItemId != null) {
					selectRecordVO(nextItemId, null);
				}
			}
		};
		nextButton.setWidth("24px");
	}
	
	private BaseWindow newWindow() {
		BaseWindow window = new BaseWindow();
		window.addStyleName("viewable-record-window");
		window.setHeight("99%");
		window.setWidth("95%");
		window.setResizable(true);
		window.setModal(false);
		window.center();
		window.addCloseListener(new CloseListener() {
			@Override
			public void windowClose(CloseEvent e) {
				refreshMetadata();
			}
		});
		return window;
	}
	
	private void buildDisplayButton() {
		displayButton = new DisplayButton($("display"), false) {
			@Override
			protected void buttonClick(ClickEvent event) {
				String schemaCode = selectedRecordVO.getSchema().getCode();
				String schemaTypeCode = SchemaUtils.getSchemaTypeCode(schemaCode);
				if (Document.SCHEMA_TYPE.equals(schemaTypeCode)) {
					DisplayDocumentViewImpl view = new DisplayDocumentViewImpl(selectedRecordVO, false);
					view.enter(null);
					BaseWindow window = newWindow();
					view.addStyleName("viewable-record-window-content");
					int browserWindowHeight = Page.getCurrent().getBrowserWindowHeight();
					int viewHeight = browserWindowHeight - 50;
					view.setHeight(viewHeight + "px");
					window.setContent(view);
					ConstellioUI.getCurrent().addWindow(window);
				} else {
					ReferenceDisplay referenceDisplay = new ReferenceDisplay(selectedRecordVO);
					referenceDisplay.click();
				}
			}
		};
		displayButton.setVisible(false);
	}
	
	private void buildOpenDocumentComponent() {
		openDocumentComponent = new BaseCustomComponent();
		openDocumentComponent.setWidth("100%");
	}
	
	private void buildCloseViewerButton() {
		closeViewerButton = new IconButton(new ThemeResource("images/commun/supprimer.gif"), $("FilteredSearchResultsViewerTable.closeViewer")) {
			@Override
			protected void buttonClick(ClickEvent event) {
				closeViewerButtonClicked();
			}
		};
	}

	private void refreshMetadata() {
		RecordServices recordServices = ConstellioUI.getCurrent().getConstellioFactories().getModelLayerFactory().newRecordServices();
		Record selectedRecord = recordServices.getDocumentById(selectedRecordVO.getId());
		selectedRecordVO = new RecordToVOBuilder().build(selectedRecord, VIEW_MODE.DISPLAY, ConstellioUI.getCurrentSessionContext());
		viewerMetadataPanel.setRecordVO(selectedRecordVO);
	}
	
	public List<TableCompressListener> getTableCompressListeners() {
		return tableCompressListeners;
	}
	
	public void addTableCompressListener(TableCompressListener listener) {
		if (!tableCompressListeners.contains(listener)) {
			tableCompressListeners.add(listener);
		}
	}
	
	public void removeTableCompressListener(TableCompressListener listener) {
		tableCompressListeners.remove(listener);
	}
	
	private static class ViewerMetadataPanel extends Panel {
		
		private VerticalLayout mainLayout;
		
		public ViewerMetadataPanel() {
			buildUI();
		}
		
		private void setRecordVO(RecordVO recordVO) {
			mainLayout.removeAllComponents();
			
			if (recordVO != null) {
				UserVO currentUser = ConstellioUI.getCurrentSessionContext().getCurrentUser();
				RecordDisplay recordDisplay = new RecordDisplayFactory(currentUser).build(recordVO, true);
				
				ContentViewer contentViewer;
				String schemaTypeCode = recordVO.getSchema().getTypeCode();
				if (Document.SCHEMA_TYPE.equals(schemaTypeCode)) {
					ContentVersionVO contentVersionVO = recordVO.get(Document.CONTENT);
					if (contentVersionVO != null) {
						contentViewer = new ContentViewer(recordVO, Document.CONTENT, contentVersionVO);
					} else {
						contentViewer = null;
					}
				} else {
					contentViewer = null;
				}

				if (contentViewer != null) {
					mainLayout.addComponents(contentViewer, recordDisplay);
					mainLayout.setExpandRatio(contentViewer, 1);
				} else {
					mainLayout.addComponent(recordDisplay);
				}
			}
		}
		
		private void buildUI() {
			addStyleName("viewer-metadata-panel");
			setSizeFull();
			
			mainLayout = new VerticalLayout();
			mainLayout.setSizeFull();
			setContent(mainLayout);
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

}
