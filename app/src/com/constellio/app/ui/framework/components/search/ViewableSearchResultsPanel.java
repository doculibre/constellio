package com.constellio.app.ui.framework.components.search;

import static com.constellio.app.ui.i18n.i18n.$;

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
import com.constellio.app.ui.framework.containers.RecordVOLazyContainer;
import com.constellio.app.ui.framework.containers.SearchResultContainer;
import com.jensjansson.pagedtable.PagedTableContainer;
import com.vaadin.data.Container;
import com.vaadin.event.ItemClickEvent;
import com.vaadin.event.ItemClickEvent.ItemClickListener;
import com.vaadin.server.ThemeResource;
import com.vaadin.ui.Alignment;
import com.vaadin.ui.Component;
import com.vaadin.ui.MenuBar;
import com.vaadin.ui.Panel;
import com.vaadin.ui.Table;
import com.vaadin.ui.VerticalLayout;

public class ViewableSearchResultsPanel extends I18NHorizontalLayout {
	
	private VerticalLayout actionsMenuBarTableLayout;
	
	private VerticalLayout closeButtonViewerMetadataLayout;
	
	private Component resultsComponent;
	
	private Table resultsTable;
	
	private ViewerMetadataPanel viewerMetadataPanel;
	
	private MenuBar actionsMenuBar;
	
	private BaseButton closeViewerButton;
	
	private Integer rowSelected;

	public ViewableSearchResultsPanel(Component resultsComponent) {
		this.resultsComponent = resultsComponent;
		buildUI();
	}
	
	private void buildUI() {
		setSizeFull();
		setSpacing(true);
		
		buildResultsTable();
		buildViewerMetadataPanel();
		buildActionsMenuBar();
		buildCloseViewerButton();
		
		actionsMenuBarTableLayout = new VerticalLayout(actionsMenuBar);
		if (resultsComponent != null) {
			actionsMenuBarTableLayout.addComponent(resultsComponent);
		} else {
			actionsMenuBarTableLayout.addComponent(resultsTable);
		}
		closeButtonViewerMetadataLayout = new VerticalLayout(closeViewerButton, viewerMetadataPanel);
		
		actionsMenuBarTableLayout.setHeight("100%");
		actionsMenuBarTableLayout.setComponentAlignment(actionsMenuBar, Alignment.TOP_RIGHT);
		
		closeButtonViewerMetadataLayout.setHeight("100%");
		closeButtonViewerMetadataLayout.setComponentAlignment(closeViewerButton, Alignment.TOP_RIGHT);
		
		addComponents(actionsMenuBarTableLayout, closeButtonViewerMetadataLayout);
		
		adjustTableExpansion();
	}
	
	void adjustTableExpansion() {
		if (rowSelected != null) {
			if (!closeButtonViewerMetadataLayout.isVisible()) {
				closeButtonViewerMetadataLayout.setVisible(true);
				actionsMenuBarTableLayout.setWidth("500px");
				setExpandRatio(actionsMenuBarTableLayout, 0);
				setExpandRatio(closeButtonViewerMetadataLayout, 1);
			}
		} else {
			if (closeButtonViewerMetadataLayout.isVisible()) {
				closeButtonViewerMetadataLayout.setVisible(false);
				actionsMenuBarTableLayout.setWidth("100%");
				setExpandRatio(actionsMenuBarTableLayout, 1);
				setExpandRatio(closeButtonViewerMetadataLayout, 0);
			}
		}
	}
	
	public void setTable(Table resultsTable) {
		this.resultsTable = resultsTable;
		resultsTable.addStyleName(RecordVOTable.CLICKABLE_ROW_STYLE_NAME);
		resultsTable.addItemClickListener(new ItemClickListener() {
			@Override
			public void itemClick(ItemClickEvent event) {
				Object itemId = event.getItemId();
				rowClicked(itemId);
			}
		});
	}
	
	void rowClicked(Object itemId) {
		Integer newRowSelected = (Integer) itemId;
		if (!newRowSelected.equals(this.rowSelected)) {
			rowSelected = newRowSelected;
			
			RecordVO recordVO;
			Container container = resultsTable.getContainerDataSource();
			if (container instanceof RecordVOLazyContainer) {
				RecordVOLazyContainer recordVOLazyContainer = (RecordVOLazyContainer) container;
				recordVO = recordVOLazyContainer.getRecordVO(rowSelected);
			} else if (container instanceof SearchResultContainer) {
				SearchResultContainer searchResultContainer = (SearchResultContainer) container;
				recordVO = searchResultContainer.getRecordVO(rowSelected);
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
			adjustTableExpansion();
		}
	}
	
	void closeViewerButtonClicked() {
		rowSelected = null;
		adjustTableExpansion();
	}
	
	private void buildResultsTable() {
		if (resultsComponent != null) {
			resultsComponent.setWidth("100%");
		} else {
			resultsTable.setWidth("100%");
		}
	}
	
	private void buildViewerMetadataPanel() {
		viewerMetadataPanel = new ViewerMetadataPanel();
	}
	
	private void buildActionsMenuBar() {
		actionsMenuBar = new MenuBar();
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
	
	private static class ViewerMetadataPanel extends VerticalLayout {
		
		private Panel metadataPanel;
		
		public ViewerMetadataPanel() {
			buildUI();
		}
		
		private void setRecordVO(RecordVO recordVO) {
			removeAllComponents();
			
			if (recordVO != null) {
				metadataPanel = new Panel();
				metadataPanel.setCaption("Métadonnées");
				
				UserVO currentUser = ConstellioUI.getCurrentSessionContext().getCurrentUser();
				RecordDisplay recordDisplay = new RecordDisplayFactory(currentUser).build(recordVO);
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
					addComponents(contentViewer, metadataPanel);
					setExpandRatio(contentViewer, 1);
				} else {
					addComponent(metadataPanel);
				}
			}
		}
		
		private void buildUI() {
			setSizeFull();
		}
		
	}

}
