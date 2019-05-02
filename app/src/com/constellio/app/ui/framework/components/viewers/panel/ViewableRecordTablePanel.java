package com.constellio.app.ui.framework.components.viewers.panel;

import com.constellio.app.modules.rm.ui.components.content.ConstellioAgentLink;
import com.constellio.app.modules.rm.ui.pages.document.DisplayDocumentViewImpl;
import com.constellio.app.modules.rm.ui.pages.folder.DisplayFolderViewImpl;
import com.constellio.app.modules.rm.wrappers.Document;
import com.constellio.app.modules.rm.wrappers.Folder;
import com.constellio.app.ui.application.ConstellioUI;
import com.constellio.app.ui.entities.RecordVO;
import com.constellio.app.ui.entities.RecordVO.VIEW_MODE;
import com.constellio.app.ui.entities.UserVO;
import com.constellio.app.ui.framework.builders.RecordToVOBuilder;
import com.constellio.app.ui.framework.buttons.BaseButton;
import com.constellio.app.ui.framework.buttons.IconButton;
import com.constellio.app.ui.framework.components.RecordDisplayFactory;
import com.constellio.app.ui.framework.components.display.ReferenceDisplay;
import com.constellio.app.ui.framework.components.layouts.I18NHorizontalLayout;
import com.constellio.app.ui.framework.components.table.RecordVOTable;
import com.constellio.app.ui.framework.containers.ContainerAdapter;
import com.constellio.app.ui.framework.containers.RecordVOLazyContainer;
import com.constellio.app.ui.framework.containers.SearchResultContainer;
import com.constellio.app.ui.framework.containers.SearchResultVOLazyContainer;
import com.constellio.app.ui.util.ComponentTreeUtils;
import com.constellio.app.ui.util.ResponsiveUtils;
import com.constellio.model.entities.records.Record;
import com.constellio.model.services.records.RecordServices;
import com.jensjansson.pagedtable.PagedTableContainer;
import com.vaadin.data.Container;
import com.vaadin.data.Property;
import com.vaadin.data.util.ObjectProperty;
import com.vaadin.event.ItemClickEvent;
import com.vaadin.event.ItemClickEvent.ItemClickListener;
import com.vaadin.server.Page;
import com.vaadin.server.ThemeResource;
import com.vaadin.ui.Alignment;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.Button.ClickListener;
import com.vaadin.ui.Component;
import com.vaadin.ui.Panel;
import com.vaadin.ui.Table;
import com.vaadin.ui.Table.CellStyleGenerator;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.themes.ValoTheme;
import org.apache.commons.lang3.StringUtils;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import static com.constellio.app.ui.i18n.i18n.$;

//@JavaScript({ "theme://jquery/jquery-2.1.4.min.js", "theme://scroll/fix-vertical-scroll.js" })
public class ViewableRecordTablePanel extends I18NHorizontalLayout {
	
	private VerticalLayout tableLayout;

	private VerticalLayout closeButtonViewerMetadataLayout;
	
	private Component tableComponent;
	
	private Table table;
	
	private ViewerMetadataPanel viewerMetadataPanel;

	private I18NHorizontalLayout previousNextButtonsLayout;
	
	private BaseButton previousButton;
	
	private BaseButton nextButton;
	
	private BaseButton closeViewerButton;
	
	private Integer rowSelected;
	
	private RecordVO selectedRecordVO;
	
	private Object previousItemId;
	
	private Object nextItemId;
	
	private List<TableCompressListener> tableCompressListeners = new ArrayList<>();

	private boolean navigateOnItemClick = false;

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
		buildCloseViewerButton();

		previousNextButtonsLayout = new I18NHorizontalLayout(previousButton, nextButton);
		previousNextButtonsLayout.addStyleName("previous-next-buttons-layout");
		previousNextButtonsLayout.setSpacing(true);
		previousNextButtonsLayout.setVisible(false);
		
		tableLayout = new VerticalLayout();
		tableLayout.addComponent(previousNextButtonsLayout);
		if (tableComponent != null) {
			tableLayout.addComponent(tableComponent);
		} else {
			tableLayout.addComponent(table);
		}
		tableLayout.setComponentAlignment(previousNextButtonsLayout, Alignment.TOP_RIGHT);

		closeButtonViewerMetadataLayout = new VerticalLayout(closeViewerButton, viewerMetadataPanel);
		closeButtonViewerMetadataLayout.addStyleName("close-button-viewer-metadata-layout");
		closeButtonViewerMetadataLayout.setId("close-button-viewer-metadata-layout");
		closeButtonViewerMetadataLayout.setHeight("100%");
		closeButtonViewerMetadataLayout.setComponentAlignment(closeViewerButton, Alignment.TOP_RIGHT);
//		closeButtonViewerMetadataLayout.setWidthUndefined();
		
		tableLayout.setHeight("100%");

		addComponent(tableLayout);
		if (isCompressionSupported()) {
			addComponent(closeButtonViewerMetadataLayout);
		}
		adjustTableExpansion();
	}

	int computeCompressedWidth() {
		return Page.getCurrent().getBrowserWindowWidth() > 1400 ? 650 : 500;
	}

	boolean isCompressionSupported() {
		return ResponsiveUtils.isDesktop() && !navigateOnItemClick;
	}

	public boolean isNavigateOnItemClick() {
		return navigateOnItemClick;
	}

	public void setNavigateOnItemClick(boolean navigateOnItemClick) {
		this.navigateOnItemClick = navigateOnItemClick;
	}

	void adjustTableExpansion() {
		if (isCompressionSupported()) {
			String compressedStyleName = "viewable-record-table-compressed";
			if (rowSelected != null) {
				if (!closeButtonViewerMetadataLayout.isVisible()) {
					if (table != null) {
						table.addStyleName(compressedStyleName);
					}
					closeButtonViewerMetadataLayout.setVisible(true);
					tableLayout.setWidth(computeCompressedWidth() + "px");
					setExpandRatio(tableLayout, 0);
					setExpandRatio(closeButtonViewerMetadataLayout, 1);
				}
			} else {
				if (closeButtonViewerMetadataLayout.isVisible()) {
					if (table != null) {
						table.removeStyleName(compressedStyleName);
					}
					closeButtonViewerMetadataLayout.setVisible(false);
					tableLayout.setWidth("100%");
					setExpandRatio(tableLayout, 1);
					setExpandRatio(closeButtonViewerMetadataLayout, 0);
				}
			}
		}
	}

	@SuppressWarnings({"unchecked", "rawtypes"})
	public void setTable(Table resultsTable) {
		this.table = resultsTable;

		resultsTable.setContainerDataSource(new ContainerAdapter(resultsTable.getContainerDataSource()) {
			@Override
			public Property getContainerProperty(Object itemId, Object propertyId) {
				Property result = super.getContainerProperty(itemId, propertyId);
				Object propertyValue = result.getValue();
				if (propertyValue instanceof Component) {
					List<ReferenceDisplay> referenceDisplays = ComponentTreeUtils.getChildren((Component) propertyValue, ReferenceDisplay.class);
					for (ReferenceDisplay referenceDisplay : referenceDisplays) {
						for (Object listenerObject : new ArrayList<>(referenceDisplay.getListeners(ClickEvent.class))) {
							referenceDisplay.removeClickListener((ClickListener) listenerObject);
						}
					}
					List<ConstellioAgentLink> constellioAgentLinks = ComponentTreeUtils.getChildren((Component) propertyValue, ConstellioAgentLink.class);
					for (ConstellioAgentLink constellioAgentLink : constellioAgentLinks) {
						for (Object listenerObject : new ArrayList<>(constellioAgentLink.getAgentLink().getListeners(ClickEvent.class))) {
							constellioAgentLink.getAgentLink().removeClickListener((ClickListener) listenerObject);
						}
					}
				}
				return new ObjectProperty<>(propertyValue);
			}
		});

		resultsTable.setSelectable(true);
		resultsTable.setMultiSelect(false);

		resultsTable.addStyleName("viewable-record-table");
		resultsTable.addStyleName(RecordVOTable.CLICKABLE_ROW_STYLE_NAME);
		resultsTable.addItemClickListener(new ItemClickListener() {
			@Override
			public void itemClick(ItemClickEvent event) {
				rowClicked(event);
			}
		});
		final CellStyleGenerator cellStyleGenerator = resultsTable.getCellStyleGenerator();
		resultsTable.setCellStyleGenerator(new CellStyleGenerator() {
			@Override
			public String getStyle(Table source, Object itemId, Object propertyId) {
				String baseStyle = cellStyleGenerator != null ? cellStyleGenerator.getStyle(source, itemId, propertyId) : "";
				if (StringUtils.isNotBlank(baseStyle)) {
					baseStyle += " ";
				}
				return baseStyle + "viewer-results-table-row-" + itemId;
			}
		});
	}
	
	void rowClicked(ItemClickEvent event) {
		Object itemId = event.getItemId();
		selectRecordVO(itemId, event);
		previousNextButtonsLayout.setVisible(itemId != null);
	}

	boolean isSelected(Object itemId) {
		return rowSelected != null && rowSelected.equals(itemId);
	}

	Object getSelectedItemId() {
		return rowSelected;
	}

	@SuppressWarnings("rawtypes")
	void selectRecordVO(Object itemId, ItemClickEvent event) {	
		Integer newRowSelected = (Integer) itemId;
		table.setValue(newRowSelected);
		
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
				while (nestedContainer instanceof ContainerAdapter) {
					nestedContainer = ((ContainerAdapter) nestedContainer).getNestedContainer();
				}
				if (nestedContainer instanceof PagedTableContainer) {
					nestedContainer = ((PagedTableContainer) nestedContainer).getContainer();
				}
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

			if (navigateOnItemClick && selectedRecordVO != null) {
				new ReferenceDisplay(selectedRecordVO).click();
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
	
	private void closeViewerButtonClicked() {
		rowSelected = null;
		
		TableCompressEvent tableCompressEvent = new TableCompressEvent(null, false);
		for (TableCompressListener tableCompressListener : tableCompressListeners) {
			tableCompressListener.tableCompressChange(tableCompressEvent);
		}
		
		adjustTableExpansion();
		previousNextButtonsLayout.setVisible(false);
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
	
	private void buildCloseViewerButton() {
		closeViewerButton = new IconButton(new ThemeResource("images/commun/supprimer.gif"), $("FilteredSearchResultsViewerTable.closeViewer")) {
			@Override
			protected void buttonClick(ClickEvent event) {
				closeViewerButtonClicked();
			}
		};
		closeViewerButton.addStyleName("close-viewer-button");
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
		if (isCompressionSupported() && !tableCompressListeners.contains(listener)) {
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
				Component panelContent;
				String schemaTypeCode = recordVO.getSchema().getTypeCode();
				if (Document.SCHEMA_TYPE.equals(schemaTypeCode)) {
					DisplayDocumentViewImpl view = new DisplayDocumentViewImpl(recordVO, true);
					view.enter(null);
					panelContent = view;
				} else if (Folder.SCHEMA_TYPE.equals(schemaTypeCode)) {
					DisplayFolderViewImpl view = new DisplayFolderViewImpl(recordVO, true);
					view.enter(null);
					panelContent = view;
				} else {
					UserVO currentUser = ConstellioUI.getCurrentSessionContext().getCurrentUser();
					panelContent = new RecordDisplayFactory(currentUser).build(recordVO, true);
				}
				mainLayout.addComponent(panelContent);
			}
		}
		
		private void buildUI() {
			addStyleName(ValoTheme.PANEL_BORDERLESS);
			addStyleName(ValoTheme.PANEL_SCROLL_INDICATOR);
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
