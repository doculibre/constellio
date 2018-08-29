package com.constellio.app.ui.framework.components.search;

import static com.constellio.app.ui.i18n.i18n.$;

import java.io.File;

import com.constellio.app.ui.framework.buttons.BaseButton;
import com.constellio.app.ui.framework.buttons.IconButton;
import com.constellio.app.ui.framework.components.layouts.I18NHorizontalLayout;
import com.constellio.app.ui.framework.components.table.BaseTable;
import com.constellio.app.ui.framework.components.table.RecordVOTable;
import com.constellio.app.ui.framework.components.table.SelectionTableAdapter;
import com.constellio.app.ui.framework.components.viewers.document.DocumentViewer;
import com.vaadin.data.Item;
import com.vaadin.data.util.IndexedContainer;
import com.vaadin.event.ItemClickEvent;
import com.vaadin.event.ItemClickEvent.ItemClickListener;
import com.vaadin.event.LayoutEvents.LayoutClickEvent;
import com.vaadin.event.LayoutEvents.LayoutClickListener;
import com.vaadin.server.ThemeResource;
import com.vaadin.ui.Alignment;
import com.vaadin.ui.Component;
import com.vaadin.ui.Label;
import com.vaadin.ui.MenuBar;
import com.vaadin.ui.MenuBar.Command;
import com.vaadin.ui.MenuBar.MenuItem;
import com.vaadin.ui.Panel;
import com.vaadin.ui.Table.ColumnHeaderMode;
import com.vaadin.ui.VerticalLayout;

public class MockViewableSearchResultsPanel extends I18NHorizontalLayout {
	
	private VerticalLayout actionsMenuBarTableLayout;
	
	private VerticalLayout closeButtonViewerMetadataLayout;
	
	private SelectionTableAdapter resultsTableAdapter;
	
	private ViewerMetadataPanel viewerMetadataPanel;
	
	private MenuBar actionsMenuBar;
	
	private BaseButton closeViewerButton;
	
	private Integer rowSelected;

	public MockViewableSearchResultsPanel() {
		buildUI();
	}
	
	private void buildUI() {
		setSizeFull();
		setSpacing(true);
		
		buildResultsTable();
		buildViewerMetadataPanel();
		buildActionsMenuBar();
		buildCloseViewerButton();
		
		actionsMenuBarTableLayout = new VerticalLayout(actionsMenuBar, resultsTableAdapter);
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
	
	void rowClicked(Object itemId) {
		rowSelected = (Integer) itemId;
		adjustTableExpansion();
	}
	
	void closeViewerButtonClicked() {
		rowSelected = null;
		adjustTableExpansion();
	}
	
	@SuppressWarnings("unchecked")
	private void buildResultsTable() {
		final IndexedContainer container = new IndexedContainer();
		container.addContainerProperty("image", String.class, null);
		container.addContainerProperty("metadatas", Component.class, null);
		container.addContainerProperty("actions", MenuBar.class, null);
		
		final BaseTable resultsTable = new BaseTable(getClass().getName());
		resultsTable.setContainerDataSource(container);
		resultsTable.addStyleName(RecordVOTable.CLICKABLE_ROW_STYLE_NAME);
		resultsTable.setWidth("100%");
		
		resultsTable.setColumnHeaderMode(ColumnHeaderMode.HIDDEN);
		resultsTable.setColumnWidth("image", 100);
		resultsTable.setColumnExpandRatio("metadatas", 1);
		
		for (int i = 0; i < 20; i++) {
			final Object itemId = resultsTable.addItem();
			Item item = resultsTable.getItem(itemId);
			
			String title = "Image " + (i + 1);
			VerticalLayout metadatas = new VerticalLayout();
			metadatas.addComponent(new Label("Titre " + (i + 1)));
			metadatas.addComponent(new Label("Extrait"));
			metadatas.addComponent(new Label("Metadata 1"));
			metadatas.addComponent(new Label("Metadata 2"));
			metadatas.addLayoutClickListener(new LayoutClickListener() {
				boolean toggle = false;
				@Override
				public void layoutClick(LayoutClickEvent event) {
					rowClicked(itemId);
					
					if (toggle) {
						container.addContainerProperty("test", String.class, "Test");
						resultsTable.setVisibleColumns("image", "metadatas", "actions", "test");
					} else {
						container.removeContainerProperty("test");
						resultsTable.setVisibleColumns("image", "metadatas", "actions");
					}
					toggle = !toggle;
				}
			});
			
			MenuBar actionsMenuBar = new MenuBar();
			
			item.getItemProperty("image").setValue(title);
			item.getItemProperty("metadatas").setValue(metadatas);
			item.getItemProperty("actions").setValue(actionsMenuBar);
		}
		
		resultsTable.addItemClickListener(new ItemClickListener() {
			@Override
			public void itemClick(ItemClickEvent event) {
				Object itemId = event.getItemId();
				rowClicked(itemId);
			}
		});
		
		resultsTableAdapter = new SelectionTableAdapter(resultsTable) {
			@Override
			public void setSelected(Object itemId, boolean selected) {
			}
			
			@Override
			public void selectAll() {
			}
			
			@Override
			public boolean isSelected(Object itemId) {
				return false;
			}
			
			@Override
			public boolean isAllItemsSelected() {
				return false;
			}
			
			@Override
			public boolean isAllItemsDeselected() {
				return false;
			}
			
			@Override
			public void deselectAll() {
				
			}
		};
		resultsTableAdapter.setWidth("100%");
	}
	
	private void buildViewerMetadataPanel() {
		viewerMetadataPanel = new ViewerMetadataPanel();
	}
	
	private void buildActionsMenuBar() {
		actionsMenuBar = new MenuBar();
		actionsMenuBar.addItem("Action 1", new Command() {
			@Override
			public void menuSelected(MenuItem selectedItem) {
				
			}
		});
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
		
		private DocumentViewer contentViewer;
		
		private Panel metadataPanel;
		
		public ViewerMetadataPanel() {
			buildUI();
		}
		
		private void buildUI() {
			setSizeFull();
			
			String filePath = "C:\\Users\\Vincent\\Desktop\\temp\\Demande de soutien - CSOB_Création de dossiers.pdf";
			
			contentViewer = new DocumentViewer(new File(filePath));
			
			metadataPanel = new Panel();
			metadataPanel.setCaption("Métadonnées");
			
			VerticalLayout metadataLayout = new VerticalLayout();
			for (int i = 0; i < 10; i++) {
				Label metadataLabel = new Label();
				metadataLabel.setCaption("Some text label");
				metadataLabel.setValue("Some text value");
				metadataLayout.addComponent(metadataLabel);
			}
			
			addComponents(contentViewer, metadataPanel);
			metadataPanel.setContent(metadataLayout);
			
			setExpandRatio(contentViewer, 1);
		}
		
	}

}
