package com.constellio.app.ui.framework.components.viewers.panel;

import static com.constellio.app.ui.i18n.i18n.$;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import com.constellio.app.modules.rm.wrappers.Document;
import com.constellio.app.ui.application.ConstellioUI;
import com.constellio.app.ui.entities.ContentVersionVO;
import com.constellio.app.ui.entities.RecordVO;
import com.constellio.app.ui.entities.UserVO;
import com.constellio.app.ui.framework.buttons.BaseButton;
import com.constellio.app.ui.framework.buttons.IconButton;
import com.constellio.app.ui.framework.components.RecordDisplay;
import com.constellio.app.ui.framework.components.RecordDisplayFactory;
import com.constellio.app.ui.framework.components.layouts.I18NHorizontalLayout;
import com.constellio.app.ui.framework.components.table.RecordVOTable;
import com.constellio.app.ui.framework.components.viewers.ContentViewer;
import com.constellio.app.ui.framework.containers.ContainerAdapter;
import com.constellio.app.ui.framework.containers.RecordVOLazyContainer;
import com.constellio.app.ui.framework.containers.SearchResultContainer;
import com.constellio.app.ui.framework.containers.SearchResultVOLazyContainer;
import com.jensjansson.pagedtable.PagedTableContainer;
import com.vaadin.data.Container;
import com.vaadin.event.ItemClickEvent;
import com.vaadin.event.ItemClickEvent.ItemClickListener;
import com.vaadin.server.ThemeResource;
import com.vaadin.ui.Alignment;
import com.vaadin.ui.Component;
import com.vaadin.ui.Label;
import com.vaadin.ui.MenuBar;
import com.vaadin.ui.Panel;
import com.vaadin.ui.Table;
import com.vaadin.ui.VerticalLayout;

//@JavaScript({ "theme://jquery/jquery-2.1.4.min.js", "theme://scroll/fix-vertical-scroll.js" })
public class ViewableRecordTablePanel extends I18NHorizontalLayout {
	
	private VerticalLayout actionsMenuBarTableLayout;
	
	private VerticalLayout closeButtonViewerMetadataLayout;
	
	private Component closeButtonViewerMetadataSpacer;
	
	private Component tableComponent;
	
	private Table table;
	
	private ViewerMetadataPanel viewerMetadataPanel;
	
	private MenuBar actionsMenuBar;
	
	private BaseButton closeViewerButton;
	
	private Integer rowSelected;
	
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
		buildActionsMenuBar();
		buildCloseViewerButton();
		
		actionsMenuBarTableLayout = new VerticalLayout(actionsMenuBar);
		if (tableComponent != null) {
			actionsMenuBarTableLayout.addComponent(tableComponent);
		} else {
			actionsMenuBarTableLayout.addComponent(table);
		}
		
		closeButtonViewerMetadataSpacer = new Label();
		closeButtonViewerMetadataSpacer.setId("close-button-viewer-metadata-spacer");
		closeButtonViewerMetadataSpacer.setVisible(false);
		
		closeButtonViewerMetadataLayout = new VerticalLayout(closeButtonViewerMetadataSpacer, closeViewerButton, viewerMetadataPanel);
		closeButtonViewerMetadataLayout.addStyleName("close-button-viewer-metadata-layout");
		closeButtonViewerMetadataLayout.setId("close-button-viewer-metadata-layout");
		closeButtonViewerMetadataLayout.setHeight("100%");
//		closeButtonViewerMetadataLayout.setWidthUndefined();
		
		actionsMenuBarTableLayout.setHeight("100%");
		actionsMenuBarTableLayout.setComponentAlignment(actionsMenuBar, Alignment.TOP_RIGHT);
		
		closeButtonViewerMetadataLayout.setComponentAlignment(closeViewerButton, Alignment.TOP_RIGHT);
		
		addComponents(actionsMenuBarTableLayout, closeButtonViewerMetadataLayout);
		
		adjustTableExpansion();
	}
	
	void adjustTableExpansion() {
		String compressedStyleName = "viewable-record-table-compressed";
		if (rowSelected != null) {
			if (!closeButtonViewerMetadataLayout.isVisible()) {
				if (table != null) {
					table.addStyleName(compressedStyleName);
				}
				closeButtonViewerMetadataLayout.setVisible(true);
				actionsMenuBarTableLayout.setWidth("500px");
				setExpandRatio(actionsMenuBarTableLayout, 0);
				setExpandRatio(closeButtonViewerMetadataLayout, 1);
			}
		} else {
			if (closeButtonViewerMetadataLayout.isVisible()) {
				if (table != null) {
					table.removeStyleName(compressedStyleName);
				}	
				closeButtonViewerMetadataLayout.setVisible(false);
				actionsMenuBarTableLayout.setWidth("100%");
				setExpandRatio(actionsMenuBarTableLayout, 1);
				setExpandRatio(closeButtonViewerMetadataLayout, 0);
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
	
	@SuppressWarnings("rawtypes")
	void rowClicked(ItemClickEvent event) {
		Object itemId = event.getItemId();
		Integer newRowSelected = (Integer) itemId;
		
		Boolean compressionChange;
		if (rowSelected == null && newRowSelected != null) {
			compressionChange = true;
		} else {
			compressionChange = null;
		}
		
		if (!newRowSelected.equals(this.rowSelected)) {
			rowSelected = newRowSelected;
			
			RecordVO recordVO;
			Container container = table.getContainerDataSource();
			while (container instanceof ContainerAdapter) {
				container = ((ContainerAdapter) container).getNestedContainer();
			}
			if (container instanceof RecordVOLazyContainer) {
				RecordVOLazyContainer recordVOLazyContainer = (RecordVOLazyContainer) container;
				recordVO = recordVOLazyContainer.getRecordVO(rowSelected);
			} else if (container instanceof SearchResultContainer) {
				SearchResultContainer searchResultContainer = (SearchResultContainer) container;
				recordVO = searchResultContainer.getRecordVO(rowSelected);
			} else if (container instanceof SearchResultVOLazyContainer) {
				SearchResultVOLazyContainer searchResultContainer = (SearchResultVOLazyContainer) container;
				recordVO = searchResultContainer.getRecordVO(rowSelected);
			} else if (container instanceof ViewableRecordVOContainer) {
				ViewableRecordVOContainer testContainer = (ViewableRecordVOContainer) container;
				recordVO = testContainer.getRecordVO(rowSelected);
			} else if (container instanceof PagedTableContainer) {
				PagedTableContainer pagedTableContainer = (PagedTableContainer) container;
				Container nestedContainer = pagedTableContainer.getContainer();
				if (nestedContainer instanceof RecordVOLazyContainer) {
					RecordVOLazyContainer recordVOLazyContainer = (RecordVOLazyContainer) nestedContainer;
					recordVO = recordVOLazyContainer.getRecordVO(rowSelected);
				} else if (nestedContainer instanceof SearchResultContainer) {
					SearchResultContainer searchResultContainer = (SearchResultContainer) nestedContainer;
					recordVO = searchResultContainer.getRecordVO(rowSelected);
				} else {
					recordVO = null;
				}	
			} else {
				recordVO = null;
			}
			
			viewerMetadataPanel.setRecordVO(recordVO);
			
			if (compressionChange != null) {
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
	
	void closeViewerButtonClicked() {
		rowSelected = null;
		closeButtonViewerMetadataSpacer.setVisible(false);
		
		TableCompressEvent tableCompressEvent = new TableCompressEvent(null, false);
		for (TableCompressListener tableCompressListener : tableCompressListeners) {
			tableCompressListener.tableCompressChange(tableCompressEvent);
		}
		
		adjustTableExpansion();
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
	
	private void buildActionsMenuBar() {
		actionsMenuBar = new MenuBar();
		actionsMenuBar.addStyleName("viewable-record-table-panel-actions-menu");
//		actionsMenuBar.addItem("Action 1", new Command() {
//			@Override
//			public void menuSelected(MenuItem selectedItem) {
//				
//			}
//		});
		actionsMenuBar.setVisible(false);
	}
	
	private void buildCloseViewerButton() {
		closeViewerButton = new IconButton(new ThemeResource("images/commun/supprimer.gif"), $("FilteredSearchResultsViewerTable.closeViewer")) {
			@Override
			protected void buttonClick(ClickEvent event) {
				closeViewerButtonClicked();
			}
		};
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
		
		private Panel metadataPanel;
		
		public ViewerMetadataPanel() {
			buildUI();
		}
		
		private void setRecordVO(RecordVO recordVO) {
			mainLayout.removeAllComponents();
			
			if (recordVO != null) {
				metadataPanel = new Panel();
				metadataPanel.setCaption("Métadonnées");
				
				UserVO currentUser = ConstellioUI.getCurrentSessionContext().getCurrentUser();
				RecordDisplay recordDisplay = new RecordDisplayFactory(currentUser).build(recordVO, true);
				
				metadataPanel.setContent(recordDisplay);
				
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
					mainLayout.addComponents(contentViewer, metadataPanel);
					mainLayout.setExpandRatio(contentViewer, 1);
				} else {
					mainLayout.addComponent(metadataPanel);
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
