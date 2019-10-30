package com.constellio.app.modules.rm.ui.pages.folder;

import com.constellio.app.modules.rm.services.menu.FolderMenuItemServices.FolderMenuItemActionType;
import com.constellio.app.modules.rm.ui.components.RMMetadataDisplayFactory;
import com.constellio.app.modules.rm.ui.components.content.DocumentContentVersionWindowImpl;
import com.constellio.app.modules.rm.ui.entities.DocumentVO;
import com.constellio.app.modules.rm.wrappers.Document;
import com.constellio.app.modules.tasks.model.wrappers.Task;
import com.constellio.app.modules.tasks.ui.components.fields.StarredFieldImpl;
import com.constellio.app.ui.application.Navigation;
import com.constellio.app.ui.entities.ContentVersionVO;
import com.constellio.app.ui.entities.FacetVO;
import com.constellio.app.ui.entities.MetadataVO;
import com.constellio.app.ui.entities.MetadataValueVO;
import com.constellio.app.ui.entities.RecordVO;
import com.constellio.app.ui.framework.buttons.AddButton;
import com.constellio.app.ui.framework.buttons.BaseButton;
import com.constellio.app.ui.framework.buttons.DisplayButton;
import com.constellio.app.ui.framework.buttons.DownloadLink;
import com.constellio.app.ui.framework.buttons.EditButton;
import com.constellio.app.ui.framework.components.BaseWindow;
import com.constellio.app.ui.framework.components.ComponentState;
import com.constellio.app.ui.framework.components.RecordDisplay;
import com.constellio.app.ui.framework.components.breadcrumb.BaseBreadcrumbTrail;
import com.constellio.app.ui.framework.components.buttons.RecordVOActionButtonFactory;
import com.constellio.app.ui.framework.components.content.ContentVersionVOResource;
import com.constellio.app.ui.framework.components.content.UpdateContentVersionWindowImpl;
import com.constellio.app.ui.framework.components.fields.upload.ContentVersionUploadField;
import com.constellio.app.ui.framework.components.layouts.I18NHorizontalLayout;
import com.constellio.app.ui.framework.components.search.FacetsPanel;
import com.constellio.app.ui.framework.components.search.FacetsSliderPanel;
import com.constellio.app.ui.framework.components.selection.SelectionComponent.SelectionChangeEvent;
import com.constellio.app.ui.framework.components.selection.SelectionComponent.SelectionManager;
import com.constellio.app.ui.framework.components.table.RecordVOTable;
import com.constellio.app.ui.framework.components.table.columns.EventVOTableColumnsManager;
import com.constellio.app.ui.framework.components.table.columns.TableColumnsManager;
import com.constellio.app.ui.framework.components.table.columns.TaskVOTableColumnsManager;
import com.constellio.app.ui.framework.components.viewers.panel.ViewableRecordVOTablePanel;
import com.constellio.app.ui.framework.containers.RecordVOContainer;
import com.constellio.app.ui.framework.containers.RecordVOLazyContainer;
import com.constellio.app.ui.framework.data.RecordVODataProvider;
import com.constellio.app.ui.framework.items.RecordVOItem;
import com.constellio.app.ui.pages.base.BaseViewImpl;
import com.constellio.app.ui.pages.search.SearchPresenter.SortOrder;
import com.constellio.data.utils.KeySetMap;
import com.vaadin.data.Property.ValueChangeEvent;
import com.vaadin.data.Property.ValueChangeListener;
import com.vaadin.event.ItemClickEvent;
import com.vaadin.event.ItemClickEvent.ItemClickListener;
import com.vaadin.event.dd.DragAndDropEvent;
import com.vaadin.event.dd.DropHandler;
import com.vaadin.event.dd.acceptcriteria.AcceptAll;
import com.vaadin.event.dd.acceptcriteria.AcceptCriterion;
import com.vaadin.navigator.ViewChangeListener.ViewChangeEvent;
import com.vaadin.server.FontAwesome;
import com.vaadin.server.Page;
import com.vaadin.server.Page.BrowserWindowResizeEvent;
import com.vaadin.server.Page.BrowserWindowResizeListener;
import com.vaadin.server.Resource;
import com.vaadin.ui.Button;
import com.vaadin.ui.Component;
import com.vaadin.ui.CustomComponent;
import com.vaadin.ui.Label;
import com.vaadin.ui.TabSheet;
import com.vaadin.ui.TabSheet.Tab;
import com.vaadin.ui.Table;
import com.vaadin.ui.UI;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.Window;
import com.vaadin.ui.Window.CloseEvent;
import com.vaadin.ui.themes.ValoTheme;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static com.constellio.app.ui.i18n.i18n.$;

public class DisplayFolderViewImpl extends BaseViewImpl implements DisplayFolderView, DropHandler, BrowserWindowResizeListener {

	private final static Logger LOGGER = LoggerFactory.getLogger(DisplayFolderViewImpl.class);

	public static final String STYLE_NAME = "display-folder";
	public static final String USER_LOOKUP = "user-lookup";
	private RecordVO recordVO;
	private String taxonomyCode;
	private VerticalLayout mainLayout;
	private ContentVersionUploadField uploadField;
	private TabSheet tabSheet;
	private RecordDisplay recordDisplay;
	private FacetsSliderPanel facetsSliderPanel;
	private Component folderContentComponent;
	private ViewableRecordVOTablePanel viewerPanel;
	private Component tasksComponent;
	private Component eventsComponent;
	private DisplayFolderPresenter presenter;
	private boolean dragNDropAllowed;
	private Button displayFolderButton, editFolderButton, addDocumentButton;
	private Label borrowedLabel;

	private Window documentVersionWindow;

	private I18NHorizontalLayout contentAndFacetsLayout; 

	private RecordVODataProvider folderContentDataProvider;
	private RecordVODataProvider tasksDataProvider;
	private RecordVODataProvider eventsDataProvider;

	private FacetsPanel facetsPanel;

	private boolean nestedView;

	private boolean inWindow;

	private TabSheet.SelectedTabChangeListener selectedTabChangeListener;

	public DisplayFolderViewImpl() {
		this(null, false, false);
	}

	public DisplayFolderViewImpl(RecordVO recordVO, boolean nestedView, boolean inWindow) {
		this.nestedView = nestedView;
		this.inWindow = inWindow;
		presenter = new DisplayFolderPresenter(this, recordVO, nestedView, inWindow);
	}

	@Override
	public void attach() {
		super.attach();
		Page.getCurrent().addBrowserWindowResizeListener(this);
	}

	@Override
	public void detach() {
		Page.getCurrent().removeBrowserWindowResizeListener(this);
		super.detach();
	}

	@Override
	protected void initBeforeCreateComponents(ViewChangeEvent event) {
		if (event != null) {
			presenter.forParams(event.getParameters());
		}
	}

	public String getTaxonomyCode() {
		return taxonomyCode;
	}

	@Override
	protected void afterViewAssembled(ViewChangeEvent event) {
		presenter.viewAssembled();
	}

	@Override
	public RecordVO getRecord() {
		return recordVO;
	}

	@Override
	public void setRecord(RecordVO recordVO) {
		this.recordVO = recordVO;
	}

	@Override
	protected String getTitle() {
		return null;
	}

	@Override
	protected Component buildMainComponent(ViewChangeEvent event) {
		mainLayout = new VerticalLayout();
		mainLayout.addStyleName("display-folder-view");
		mainLayout.setSizeFull();
		mainLayout.setSpacing(true);

		uploadField = new ContentVersionUploadField() {
			@Override
			public boolean fireValueChangeWhenEqual() {
				return true;
			}

			@Override
			protected void onUploadWindowClosed(CloseEvent e) {
				presenter.refreshDocuments();
			}
		};
		uploadField.addStyleName("display-folder-upload-field");
		uploadField.setVisible(false);
		uploadField.setImmediate(true);
		uploadField.setMultiValue(false);
		uploadField.setMajorVersionFieldVisible(false);
		uploadField.addValueChangeListener(new ValueChangeListener() {
			@Override
			public void valueChange(ValueChangeEvent event) {
				ContentVersionVO uploadedContentVO = (ContentVersionVO) uploadField.getValue();
				presenter.contentVersionUploaded(uploadedContentVO);
			}
		});

		recordDisplay = new RecordDisplay(recordVO, new RMMetadataDisplayFactory());
		folderContentComponent = new CustomComponent();
		tasksComponent = new CustomComponent();

		tabSheet = new TabSheet();
		tabSheet.addStyleName(STYLE_NAME);
		tabSheet.addTab(folderContentComponent,
				$("DisplayFolderView.tabs.folderContent", presenter.getFolderContentCount()));
		tabSheet.addTab(recordDisplay, $("DisplayFolderView.tabs.metadata"));
		tabSheet.addTab(tasksComponent, $("DisplayFolderView.tabs.tasks", presenter.getTaskCount()));

		eventsComponent = new CustomComponent();
		tabSheet.addTab(eventsComponent, $("DisplayFolderView.tabs.logs"));
		if (presenter.hasCurrentUserPermissionToViewEvents()) {
			tabSheet.getTab(eventsComponent).setEnabled(true);
		} else {
			tabSheet.getTab(eventsComponent).setEnabled(false);
		}

		tabSheet.addSelectedTabChangeListener(selectedTabChangeListener = new TabSheet.SelectedTabChangeListener() {
			@Override
			public void selectedTabChange(TabSheet.SelectedTabChangeEvent event) {
				Component selectedTab = tabSheet.getSelectedTab();
				if (selectedTab == recordDisplay) {
					presenter.metadataTabSelected();
					setFacetsPanelVisible(false);
				} else if (selectedTab == folderContentComponent) {
					presenter.folderContentTabSelected();
					setFacetsPanelVisible(facetsSliderPanel != null);
				} else if (selectedTab == tasksComponent) {
					presenter.tasksTabSelected();
					setFacetsPanelVisible(false);
				} else if (selectedTab == eventsComponent) {
					presenter.eventsTabSelected();
					setFacetsPanelVisible(false);
				}
			}
		});

		borrowedLabel = new Label();
		borrowedLabel.setVisible(false);
		borrowedLabel.addStyleName(ValoTheme.LABEL_COLORED);
		borrowedLabel.addStyleName(ValoTheme.LABEL_BOLD);

		documentVersionWindow = new BaseWindow($("DocumentContentVersionWindow.windowTitle"));
		documentVersionWindow.setWidth("400px");
		documentVersionWindow.center();
		documentVersionWindow.setModal(true);

		contentAndFacetsLayout = new I18NHorizontalLayout(tabSheet);
		//contentAndFacetsLayout.addStyleName("content-and-facets-layout");
		contentAndFacetsLayout.setWidth("100%");
		contentAndFacetsLayout.setExpandRatio(tabSheet, 1);

		mainLayout.addComponents(borrowedLabel, uploadField, contentAndFacetsLayout);
		presenter.selectInitialTabForUser();
		return mainLayout;
	}

	public void addComponentAfterMenu(Component component) {
		mainLayout.addComponent(component, 0);
	}

	@Override
	protected BaseBreadcrumbTrail buildBreadcrumbTrail() {
		return presenter.getBreadCrumbTrail();
	}

	public void navigateToSelf() {
		presenter.navigateToSelf();
	}

	private Button newDisplayFolderButton() {
		BaseButton displayFolderButton;
		if (!presenter.isLogicallyDeleted()) {
			displayFolderButton = new DisplayButton($("DisplayFolderView.displayFolder"), false) {
				@Override
				protected void buttonClick(ClickEvent event) {
					presenter.displayFolderButtonClicked();
				}
			};
			displayFolderButton.setCaptionVisibleOnMobile(false);
		} else {
			displayFolderButton = null;
		}
		return displayFolderButton;
	}

	private Button newEditFolderButton() {
		BaseButton editFolderButton;
		if (!presenter.isLogicallyDeleted()) {
			editFolderButton = new EditButton($("DisplayFolderView.editFolder")) {
				@Override
				protected void buttonClick(ClickEvent event) {
					presenter.editFolderButtonClicked();
				}
			};
			editFolderButton.setCaptionVisibleOnMobile(false);
		} else {
			editFolderButton = null;
		}
		return editFolderButton;
	}

	private Button newAddDocumentButton() {
		BaseButton addDocumentButton;
		if (!presenter.isLogicallyDeleted()) {
			addDocumentButton = new AddButton($("DisplayFolderView.addDocument")) {
				@Override
				protected void buttonClick(ClickEvent event) {
					presenter.addDocumentButtonClicked();
				}
			};
			addDocumentButton.setIcon(FontAwesome.FILE_O);
			addDocumentButton.setCaptionVisibleOnMobile(false);
		} else {
			addDocumentButton = null;
		}
		return addDocumentButton;
	}

	@Override
	protected List<Button> buildActionMenuButtons(ViewChangeEvent event) {
		if (!presenter.isLogicallyDeleted()) {
			addDocumentButton = newAddDocumentButton();

			displayFolderButton = newDisplayFolderButton();
			editFolderButton = newEditFolderButton();
		}

		List<String> excludedActionTypes = Arrays.asList(
				FolderMenuItemActionType.FOLDER_DISPLAY.name(),
				FolderMenuItemActionType.FOLDER_EDIT.name(),
				FolderMenuItemActionType.FOLDER_ADD_DOCUMENT.name());
		return new RecordVOActionButtonFactory(recordVO, excludedActionTypes).build();
	}

	@Override
	public void hideAllActionMenuButtons() {
		List<Button> actionMenuButtons = getActionMenuButtons();
		if (actionMenuButtons != null) {
			for (Button button : actionMenuButtons) {
				button.setVisible(false);
				button.setEnabled(false);
			}
		}
	}

	@Override
	public String getFolderOrSubFolderButtonTitle(String key) {
		return key;
	}

	@Override
	public String getFolderOrSubFolderButtonKey(String key) {
		return key;
	}

	@Override
	public void setEvents(final RecordVODataProvider dataProvider) {
		this.eventsDataProvider = dataProvider;
	}

	@Override
	public void setFolderContent(RecordVODataProvider dataProvider) {
		this.folderContentDataProvider = dataProvider;
	}

	@Override
	public void selectMetadataTab() {
		tabSheet.setSelectedTab(recordDisplay);
	}

	@Override
	public void selectFolderContentTab() {
		tabSheet.removeSelectedTabChangeListener(selectedTabChangeListener);
		if (!(folderContentComponent instanceof Table)) {
			final RecordVOLazyContainer recordVOContainer = new RecordVOLazyContainer(folderContentDataProvider);
			facetsPanel = new FacetsPanel() {
				@Override
				protected void sortCriterionSelected(String sortCriterion, SortOrder sortOrder) {
					presenter.sortCriterionSelected(sortCriterion, sortOrder);
				}

				@Override
				protected void facetValueSelected(String facetId, String value) {
					presenter.facetValueSelected(facetId, value);
				}

				@Override
				protected void facetValueDeselected(String facetId, String value) {
					presenter.facetValueDeselected(facetId, value);
				}

				@Override
				protected void facetOpened(String id) {
					presenter.facetOpened(id);
				}

				@Override
				protected void facetDeselected(String id) {
					presenter.facetDeselected(id);
				}

				@Override
				protected void facetClosed(String id) {
					presenter.facetClosed(id);
				}
			};
			refreshFacets(folderContentDataProvider);

			viewerPanel = new ViewableRecordVOTablePanel(recordVOContainer) {
				@Override
				protected boolean isSelectColumn() {
					return !nestedView;
				}

				@Override
				protected boolean isNested() {
					return nestedView;
				}

				@Override
				protected SelectionManager newSelectionManager() {
					return new SelectionManager() {

						private Set<Object> selectedItemIds = new HashSet<>();

						@Override
						public List<Object> getAllSelectedItemIds() {
							List<Object> allSelectedItemIds;
							RecordVOContainer recordVOContainer = getRecordVOContainer();
							if (isAllItemsSelected()) {
								allSelectedItemIds = new ArrayList<>(recordVOContainer.getItemIds());
							} else {
								allSelectedItemIds = new ArrayList<>(selectedItemIds);
							}
							return allSelectedItemIds;
						}

						@Override
						public boolean isAllItemsSelected() {
							return presenter.isAllItemsSelected();
						}

						@Override
						public boolean isAllItemsDeselected() {
							return presenter.isAllItemsDeselected();
						}

						@Override
						public boolean isSelected(Object itemId) {
							RecordVO recordVO = recordVOContainer.getRecordVO((int) itemId);
							return presenter.isSelected(recordVO);
						}

						@Override
						public void selectionChanged(SelectionChangeEvent event) {
							if (event.isAllItemsSelected()) {
								presenter.selectAllClicked();
							} else if (event.isAllItemsDeselected()) {
								selectedItemIds.clear();
								presenter.deselectAllClicked();
							} else if (event.getSelectedItemIds() != null) {
								List<Object> selectedItemIds = event.getSelectedItemIds();
								for (Object selectedItemId : selectedItemIds) {
									this.selectedItemIds.add(selectedItemId);
									RecordVO recordVO = getRecordVO(selectedItemId);
									presenter.recordSelectionChanged(recordVO, true);
								}
							} else if (event.getDeselectedItemIds() != null) {
								List<Object> deselectedItemIds = event.getDeselectedItemIds();
								for (Object deselectedItemId : deselectedItemIds) {
									this.selectedItemIds.remove(deselectedItemId);
									RecordVO recordVO = getRecordVO(deselectedItemId);
									presenter.recordSelectionChanged(recordVO, false);
								}
							}
						}
					};
				}
			};
			viewerPanel.addItemClickListener(new ItemClickListener() {
				@Override
				public void itemClick(ItemClickEvent event) {
					Object itemId = event.getItemId();
					Integer index = (Integer) itemId;
					RecordVO recordVO = recordVOContainer.getRecordVO(itemId);
					presenter.itemClicked(recordVO, index);
				}
			});
			viewerPanel.addStyleName("folder-content-table");

			if (!nestedView && (folderContentDataProvider.size() > 0 || !folderContentDataProvider.getFieldFacetValues().isEmpty())) {
				if (facetsSliderPanel != null && facetsSliderPanel.getParent() != null) {
					contentAndFacetsLayout.removeComponent(facetsSliderPanel);
				}
				facetsSliderPanel = new FacetsSliderPanel(facetsPanel);
				contentAndFacetsLayout.addComponent(facetsSliderPanel);
			}
			tabSheet.replaceComponent(folderContentComponent, folderContentComponent = viewerPanel);
			viewerPanel.setSelectionActionButtons();
		}
		tabSheet.setSelectedTab(folderContentComponent);
		tabSheet.addSelectedTabChangeListener(selectedTabChangeListener);
	}

	private void setFacetsPanelVisible(boolean visible) {
		if (facetsSliderPanel != null) {
			facetsSliderPanel.setVisible(visible);
		}
	}

	@Override
	public void refreshFolderContentTab() {
		Tab folderContentTab = tabSheet.getTab(folderContentComponent);
		folderContentTab.setCaption($("DisplayFolderView.tabs.folderContent", presenter.getFolderContentCount()));
	}

	@Override
	public void selectTasksTab() {
		if (!(tasksComponent instanceof Table)) {
			Table table = new RecordVOTable(tasksDataProvider) {
				@SuppressWarnings("unchecked")
				@Override
				protected Component buildMetadataComponent(Object itemId, MetadataValueVO metadataValue,
														   RecordVO recordVO) {
					if (Task.STARRED_BY_USERS.equals(metadataValue.getMetadata().getLocalCode())) {
						return new StarredFieldImpl(recordVO.getId(), (List<String>) metadataValue.getValue(),
								getSessionContext().getCurrentUser().getId()) {
							@Override
							public void updateTaskStarred(boolean isStarred, String taskId) {
								presenter.updateTaskStarred(isStarred, taskId, tasksDataProvider);
							}
						};
					} else {
						return super.buildMetadataComponent(itemId, metadataValue, recordVO);
					}
				}

				@Override
				protected TableColumnsManager newColumnsManager() {
					return new TaskVOTableColumnsManager() {
						@Override
						protected String toColumnId(Object propertyId) {
							if (propertyId instanceof MetadataVO) {
								if (Task.STARRED_BY_USERS.equals(((MetadataVO) propertyId).getLocalCode())) {
									setColumnHeader(propertyId, "");
									setColumnWidth(propertyId, 60);
								}
							}
							return super.toColumnId(propertyId);
						}
					};
				}

				@Override
				public Collection<?> getSortableContainerPropertyIds() {
					Collection<?> sortableContainerPropertyIds = super.getSortableContainerPropertyIds();
					Iterator<?> iterator = sortableContainerPropertyIds.iterator();
					while (iterator.hasNext()) {
						Object property = iterator.next();
						if (property != null && property instanceof MetadataVO && Task.STARRED_BY_USERS
								.equals(((MetadataVO) property).getLocalCode())) {
							iterator.remove();
						}
					}
					return sortableContainerPropertyIds;
				}
			};
			table.setSizeFull();
			table.addItemClickListener(new ItemClickListener() {
				@Override
				public void itemClick(ItemClickEvent event) {
					RecordVOItem item = (RecordVOItem) event.getItem();
					RecordVO recordVO = item.getRecord();
					presenter.taskClicked(recordVO);
				}
			});
			table.setPageLength(Math.min(15, tasksDataProvider.size()));
			tabSheet.replaceComponent(tasksComponent, table);
			tasksComponent = table;
		}
		tabSheet.setSelectedTab(tasksComponent);
	}

	@Override
	protected List<Button> getQuickActionMenuButtons() {
		List<Button> quickActionMenuButtons = new ArrayList<>();
		if (!nestedView) {
			if (editFolderButton != null) {
				quickActionMenuButtons.add(editFolderButton);
			}
			if (addDocumentButton != null) {
				quickActionMenuButtons.add(addDocumentButton);
			}
		} else {
			if (displayFolderButton != null) {
				quickActionMenuButtons.add(displayFolderButton);
			}
			if (editFolderButton != null) {
				quickActionMenuButtons.add(editFolderButton);
			}
		}
		return quickActionMenuButtons;
	}

	@Override
	public void setTasks(RecordVODataProvider dataProvider) {
		this.tasksDataProvider = dataProvider;
	}

	@Override
	public void selectEventsTab() {
		if (!(eventsComponent instanceof Table)) {
			RecordVOTable table = new RecordVOTable($("DisplayFolderView.tabs.logs"),
					new RecordVOLazyContainer(eventsDataProvider, false)) {
				@Override
				protected TableColumnsManager newColumnsManager() {
					return new EventVOTableColumnsManager();
				}
			};
			table.setSizeFull();
			tabSheet.replaceComponent(eventsComponent, table);
			eventsComponent = table;

		}
		tabSheet.setSelectedTab(eventsComponent);
	}

	@Override
	public void setLogicallyDeletable(ComponentState state) {
	}

	@Override
	public void setDisplayButtonState(ComponentState state) {
		displayFolderButton.setVisible(state.isVisible());
		displayFolderButton.setEnabled(state.isEnabled());
	}

	@Override
	public void setEditButtonState(ComponentState state) {
		editFolderButton.setVisible(state.isVisible());
		editFolderButton.setEnabled(state.isEnabled());
	}

	@Override
	public void setAddDocumentButtonState(ComponentState state) {
		addDocumentButton.setVisible(state.isVisible());
		addDocumentButton.setEnabled(state.isEnabled());
		dragNDropAllowed = state.isEnabled();
	}

	//@Override
	//public void setStartWorkflowButtonState(ComponentState state) {
	//startWorkflowButton.setVisible(state.isVisible());
	//startWorkflowButton.setEnabled(state.isEnabled());
	//}

	@Override
	public void drop(DragAndDropEvent event) {
		boolean handledByViewer;
		if (viewerPanel != null && viewerPanel.isDropSupported()) {
			viewerPanel.drop(event);
			handledByViewer = true;
		} else {
			handledByViewer = false;
		}
		if (!handledByViewer && dragNDropAllowed) {
			uploadField.drop(event);
		}
	}

	@Override
	public void showVersionUpdateWindow(final RecordVO recordVO, ContentVersionVO contentVersionVO) {
		final Map<RecordVO, MetadataVO> record = new HashMap<>();
		record.put(recordVO, recordVO.getMetadata(Document.CONTENT));

		UpdateContentVersionWindowImpl uploadField = new UpdateContentVersionWindowImpl(record) {
			@Override
			public String getDocumentTitle() {
				return recordVO.getTitle();
			}
		};
		uploadField.setHeight("375px");
		uploadField.setWidth("900px");
		uploadField.setContentVersion(contentVersionVO);
		UI.getCurrent().addWindow(uploadField);
	}

	@Override
	public AcceptCriterion getAcceptCriterion() {
		return uploadField != null ? uploadField.getAcceptCriterion() : AcceptAll.get();
	}

	@Override
	public void setBorrowedMessage(String borrowedMessage) {
		if (borrowedMessage != null) {
			borrowedLabel.setVisible(true);
			borrowedLabel.setValue($(borrowedMessage));
		} else {
			borrowedLabel.setVisible(false);
			borrowedLabel.setValue(null);
		}
	}

	@Override
	public void openDocumentContentVersiontWindow(DocumentVO documentVO, ContentVersionVO contentVersionVO) {
		documentVersionWindow
				.setContent(new DocumentContentVersionWindowImpl(documentVO, contentVersionVO, presenter.getParams()));
		UI.getCurrent().addWindow(documentVersionWindow);
	}

	@Override
	public void closeDocumentContentVersionWindow() {
		documentVersionWindow.close();
	}

	//	@Override
	//	public void openAgentURL(String agentURL) {
	//		Page.getCurrent().open(agentURL, null);
	//	}

	@SuppressWarnings("deprecation")
	@Override
	public void downloadContentVersion(RecordVO recordVO, ContentVersionVO contentVersionVO) {
		ContentVersionVOResource contentVersionResource = new ContentVersionVOResource(contentVersionVO);
		Resource downloadedResource = DownloadLink.wrapForDownload(contentVersionResource);
		Page.getCurrent().open(downloadedResource, null, false);
	}

	@Override
	public void setTaxonomyCode(String taxonomyCode) {
		this.taxonomyCode = taxonomyCode;
	}

	@Override
	public void clearUploadField() {
		uploadField.setInternalValue(null);
	}

	@Override
	public Navigation navigate() {
		Navigation navigation = super.navigate();
		closeAllWindows();
		return navigation;
	}

	@Override
	protected boolean isActionMenuBar() {
		return true;
	}

	@Override
	protected boolean isBreadcrumbsVisible() {
		return !nestedView;
	}

	@Override
	protected boolean isFullWidthIfActionMenuAbsent() {
		return true;
	}

	@Override
	public void refreshFolderContentAndFacets() {

	}

	@Override
	public void refreshFolderContent() {
	}

	//	@Override
	public void refreshFacets(RecordVODataProvider dataProvider) {
		List<FacetVO> facets = presenter.getFacets(dataProvider);
		KeySetMap<String, String> facetSelections = presenter.getFacetSelections();
		List<MetadataVO> sortableMetadata = presenter.getMetadataAllowedInSort();
		String sortCriterionValue = presenter.getSortCriterionValueAmong(sortableMetadata);
		SortOrder sortOrder = presenter.getSortOrder();
		facetsPanel.refresh(facets, facetSelections, sortableMetadata, sortCriterionValue, sortOrder);
	}

	@Override
	public boolean scrollIntoView(Integer contentIndex, String recordId) {
		boolean scrolledIntoView;
		if (viewerPanel != null) {
			scrolledIntoView = viewerPanel.scrollIntoView(contentIndex, recordId);
		} else {
			scrolledIntoView = false;
		}
		return scrolledIntoView;
	}

	@Override
	public Integer getReturnIndex() {
		return presenter.getReturnIndex();
	}

	@Override
	public RecordVO getReturnRecordVO() {
		return presenter.getReturnRecordVO();
	}

	@Override
	public void browserWindowResized(BrowserWindowResizeEvent event) {
		// TODO Auto-generated method stub
	}

}
