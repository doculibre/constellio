package com.constellio.app.modules.rm.ui.pages.document;

import com.constellio.app.api.extensions.params.DocumentFolderBreadCrumbParams;
import com.constellio.app.modules.rm.ConstellioRMModule;
import com.constellio.app.modules.rm.extensions.api.RMModuleExtensions;
import com.constellio.app.modules.rm.navigation.RMViews;
import com.constellio.app.modules.rm.services.decommissioning.SearchType;
import com.constellio.app.modules.rm.services.menu.DocumentMenuItemServices.DocumentMenuItemActionType;
import com.constellio.app.modules.rm.ui.components.RMMetadataDisplayFactory;
import com.constellio.app.modules.rm.ui.components.breadcrumb.FolderDocumentContainerBreadcrumbTrail;
import com.constellio.app.modules.rm.ui.entities.DocumentVO;
import com.constellio.app.modules.rm.ui.pages.decommissioning.breadcrumb.DecommissionBreadcrumbTrail;
import com.constellio.app.modules.rm.wrappers.Document;
import com.constellio.app.modules.tasks.model.wrappers.Task;
import com.constellio.app.modules.tasks.ui.components.fields.StarredFieldImpl;
import com.constellio.app.services.migrations.VersionsComparator;
import com.constellio.app.ui.application.Navigation;
import com.constellio.app.ui.entities.ContentVersionVO;
import com.constellio.app.ui.entities.MetadataVO;
import com.constellio.app.ui.entities.MetadataValueVO;
import com.constellio.app.ui.entities.RecordVO;
import com.constellio.app.ui.framework.buttons.ConfirmDialogButton;
import com.constellio.app.ui.framework.buttons.DisplayButton;
import com.constellio.app.ui.framework.buttons.EditButton;
import com.constellio.app.ui.framework.buttons.LinkButton;
import com.constellio.app.ui.framework.buttons.WindowButton;
import com.constellio.app.ui.framework.buttons.WindowButton.WindowConfiguration;
import com.constellio.app.ui.framework.components.ComponentState;
import com.constellio.app.ui.framework.components.RecordDisplay;
import com.constellio.app.ui.framework.components.breadcrumb.BaseBreadcrumbTrail;
import com.constellio.app.ui.framework.components.buttons.RecordVOActionButtonFactory;
import com.constellio.app.ui.framework.components.content.DownloadContentVersionLink;
import com.constellio.app.ui.framework.components.content.UpdateContentVersionWindowImpl;
import com.constellio.app.ui.framework.components.diff.DiffPanel;
import com.constellio.app.ui.framework.components.layouts.I18NHorizontalLayout;
import com.constellio.app.ui.framework.components.splitpanel.CollapsibleHorizontalSplitPanel;
import com.constellio.app.ui.framework.components.table.ContentVersionVOTable;
import com.constellio.app.ui.framework.components.table.RecordVOTable;
import com.constellio.app.ui.framework.components.table.columns.EventVOTableColumnsManager;
import com.constellio.app.ui.framework.components.table.columns.TableColumnsManager;
import com.constellio.app.ui.framework.components.table.columns.TaskVOTableColumnsManager;
import com.constellio.app.ui.framework.components.viewers.ContentViewer;
import com.constellio.app.ui.framework.containers.RecordVOLazyContainer;
import com.constellio.app.ui.framework.data.RecordVODataProvider;
import com.constellio.app.ui.framework.decorators.tabs.TabSheetDecorator;
import com.constellio.app.ui.framework.items.RecordVOItem;
import com.constellio.app.ui.pages.base.BaseViewImpl;
import com.constellio.data.utils.dev.Toggle;
import com.vaadin.event.ItemClickEvent;
import com.vaadin.event.ItemClickEvent.ItemClickListener;
import com.vaadin.event.dd.DragAndDropEvent;
import com.vaadin.event.dd.DropHandler;
import com.vaadin.event.dd.acceptcriteria.AcceptAll;
import com.vaadin.event.dd.acceptcriteria.AcceptCriterion;
import com.vaadin.navigator.ViewChangeListener.ViewChangeEvent;
import com.vaadin.server.Page;
import com.vaadin.server.ThemeResource;
import com.vaadin.ui.Button;
import com.vaadin.ui.Component;
import com.vaadin.ui.CustomComponent;
import com.vaadin.ui.Label;
import com.vaadin.ui.Notification;
import com.vaadin.ui.Panel;
import com.vaadin.ui.TabSheet;
import com.vaadin.ui.Table;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.Window;
import com.vaadin.ui.themes.ValoTheme;
import org.apache.commons.lang3.StringUtils;
import org.vaadin.dialogs.ConfirmDialog;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import static com.constellio.app.ui.i18n.i18n.$;

public class DisplayDocumentViewImpl extends BaseViewImpl implements DisplayDocumentView, DropHandler {

	private VerticalLayout mainLayout;

	private Label borrowedLabel;
	private RecordVO documentVO;
	private String taxonomyCode;
	private TabSheet tabSheet;
	private ContentViewer contentViewer;
	private RecordDisplay recordDisplay;
	private ContentVersionVOTable versionTable;
	private Component tasksComponent;
	private Component eventsComponent;
	private UpdateContentVersionWindowImpl uploadWindow;
	private DisplayButton displayDocumentButton;
	private LinkButton openDocumentButton;
	private DownloadContentVersionLink downloadDocumentButton;
	private EditButton editDocumentButton;
	private ConfirmDialogButton deleteSelectedVersions;

	private boolean contentViewerInitiallyVisible;
	private boolean waitForContentViewerToBecomeVisible;

	private List<TabSheetDecorator> tabSheetDecorators = new ArrayList<>();

	private DisplayDocumentPresenter presenter;

	private boolean nestedView;

	private List<Window.CloseListener> editWindowCloseListeners = new ArrayList<>();

	public DisplayDocumentViewImpl() {
		this(null, false, false);
	}

	public DisplayDocumentViewImpl(RecordVO recordVO, boolean nestedView, boolean inWindow) {
		this.nestedView = nestedView;
		presenter = new DisplayDocumentPresenter(this, recordVO, nestedView, inWindow);
	}

	public DisplayDocumentPresenter getDisplayDocumentPresenter() {
		return presenter;
	}

	@Override
	protected void initBeforeCreateComponents(ViewChangeEvent event) {
		if (event != null) {
			presenter.forParams(event.getParameters());
		}
	}

	@Override
	protected void afterViewAssembled(ViewChangeEvent event) {
		presenter.viewAssembled();
	}

	@Override
	public void setRecordVO(RecordVO documentVO) {
		this.documentVO = documentVO;
		if (recordDisplay != null) {
			recordDisplay.setRecordVO(documentVO);
		}
	}

	@Override
	public void setContentVersions(List<ContentVersionVO> contentVersions) {
		versionTable.setContentVersions(contentVersions);
	}

	@Override
	protected String getTitle() {
		return null;
	}

	private ContentViewer newContentViewer() {
		ContentVersionVO contentVersionVO = documentVO.get(Document.CONTENT);
		ContentViewer contentViewer = new ContentViewer(documentVO, Document.CONTENT, contentVersionVO);
		return contentViewer;
	}

	@Override
	public void refreshContentViewer() {
		ContentViewer newContentViewer = newContentViewer();
		if (newContentViewer.isViewerComponentVisible()) {
			mainLayout.replaceComponent(contentViewer, newContentViewer);
			contentViewer = newContentViewer;
			waitForContentViewerToBecomeVisible = false;
		} else if (contentViewerInitiallyVisible && !newContentViewer.isViewerComponentVisible()) {
			if (contentViewer.isVisible()) {
				contentViewer.setVisible(false);
			}
			waitForContentViewerToBecomeVisible = true;
		} else {
			waitForContentViewerToBecomeVisible = false;
		}
	}

	@Override
	protected Component buildMainComponent(ViewChangeEvent event) {
		addStyleName("display-document-view");

		mainLayout = new VerticalLayout();
		mainLayout.setSizeFull();

		borrowedLabel = new Label();
		borrowedLabel.setVisible(false);
		borrowedLabel.addStyleName(ValoTheme.LABEL_COLORED);
		borrowedLabel.addStyleName(ValoTheme.LABEL_BOLD);
		borrowedLabel.addStyleName("borrowed-document-message");

		contentViewer = newContentViewer();
		contentViewerInitiallyVisible = contentViewer.isViewerComponentVisible();

		tabSheet = new TabSheet();

		recordDisplay = new RecordDisplay(documentVO, new RMMetadataDisplayFactory(), Toggle.SEARCH_RESULTS_VIEWER.isEnabled());
		recordDisplay.setSizeFull();

		versionTable = new ContentVersionVOTable("DocumentVersions", presenter.getAppLayerFactory(), presenter.hasCurrentUserPermissionToViewFileSystemName()) {
			@Override
			protected boolean isSelectionColumn() {
				return isDeleteColumn();
			}

			@Override
			protected boolean isSelectionPossible(ContentVersionVO contentVersionVO) {
				return true;
			}

			@Override
			protected boolean isDeleteColumn() {
				return presenter.isDeleteContentVersionPossible();
			}

			@Override
			protected boolean isDeletePossible(ContentVersionVO contentVersionVO) {
				return presenter.isDeleteContentVersionPossible(contentVersionVO);
			}

			@Override
			protected void deleteButtonClick(ContentVersionVO contentVersionVO) {
				presenter.deleteContentVersionButtonClicked(contentVersionVO);
			}

			@Override
			protected void selectionUpdated() {
				if (deleteSelectedVersions != null) {
					deleteSelectedVersions.setVisible(deleteSelectedVersions.isVisible());
					deleteSelectedVersions.setEnabled(deleteSelectedVersions.isEnabled());
				}
			}
		};
		tasksComponent = new CustomComponent();
		versionTable.setSizeFull();

		Panel recordDisplayPanel = new Panel(recordDisplay);
		recordDisplayPanel.addStyleName(ValoTheme.PANEL_BORDERLESS);
		recordDisplayPanel.addStyleName("panel-no-scroll");
		recordDisplayPanel.setSizeFull();

		recordDisplayPanel.addStyleName(ValoTheme.PANEL_BORDERLESS);
		recordDisplayPanel.addStyleName(ValoTheme.PANEL_SCROLL_INDICATOR);
		if (contentViewerInitiallyVisible && nestedView) {
			tabSheet.addTab(contentViewer, $("DisplayDocumentView.tabs.contentViewer"));
		}
		tabSheet.addTab(recordDisplayPanel, $("DisplayDocumentView.tabs.metadata"));
		tabSheet.addTab(buildVersionTab(), $("DisplayDocumentView.tabs.versions"));
		tabSheet.addTab(tasksComponent, $("DisplayDocumentView.tabs.tasks", presenter.getTaskCount()));

		eventsComponent = new CustomComponent();
		tabSheet.addTab(eventsComponent, $("DisplayDocumentView.tabs.logs"));
		if (presenter.hasCurrentUserPermissionToViewEvents()) {
			tabSheet.getTab(eventsComponent).setEnabled(true);
		} else {
			tabSheet.getTab(eventsComponent).setEnabled(false);
		}

		tabSheet.addSelectedTabChangeListener(new TabSheet.SelectedTabChangeListener() {
			@Override
			public void selectedTabChange(TabSheet.SelectedTabChangeEvent event) {
				if (event.getTabSheet().getSelectedTab() == eventsComponent) {
					presenter.refreshEvents();
				}
			}
		});

		Component contentMetadataComponent;
		if (contentViewerInitiallyVisible && !nestedView) {
			CollapsibleHorizontalSplitPanel splitPanel = new CollapsibleHorizontalSplitPanel(DisplayDocumentViewImpl.class.getName());
			splitPanel.setFirstComponent(contentViewer);
			splitPanel.setSecondComponent(tabSheet);
			splitPanel.setSecondComponentWidth(700, Unit.PIXELS);
			contentMetadataComponent = splitPanel;
		} else {
			contentMetadataComponent = tabSheet;
		}
		mainLayout.addComponents(borrowedLabel, contentMetadataComponent);
		for (TabSheetDecorator tabSheetDecorator : tabSheetDecorators) {
			tabSheetDecorator.decorate(this, tabSheet);
		}
		mainLayout.setExpandRatio(contentMetadataComponent, 1);

		return mainLayout;
	}

	private Component buildVersionTab() {
		final VerticalLayout tabLayout = new VerticalLayout();
		I18NHorizontalLayout buttonsLayout = new I18NHorizontalLayout();
		buttonsLayout.setSpacing(true);

		WindowButton diffButton = new WindowButton($("DisplayDocumentView.differences"), $("DisplayDocumentView.differencesExplanation"), WindowConfiguration.modalDialog("90%", "90%")) {
			@Override
			public void buttonClick(ClickEvent event) {
				HashSet<ContentVersionVO> selectedContentVersions = versionTable.getSelectedContentVersions();
				if (selectedContentVersions.size() != 2) {
					Notification.show($("DisplayDocumentView.selectTwoVersions"));
				} else {
					super.buttonClick(event);
				}
			}

			@Override
			protected Component buildWindowContent() {
				List<ContentVersionVO> selectedContentVersions = new ArrayList<>(versionTable.getSelectedContentVersions());
				Collections.sort(selectedContentVersions, new Comparator<ContentVersionVO>() {
					@Override
					public int compare(ContentVersionVO o1, ContentVersionVO o2) {
						return new VersionsComparator().compare(o1.getVersion(), o2.getVersion());
					}

				});
				ContentVersionVO contentVersionVO1 = selectedContentVersions.get(0);
				ContentVersionVO contentVersionVO2 = selectedContentVersions.get(1);
				return new DiffPanel(contentVersionVO1, contentVersionVO2);
			}
		};
		diffButton.addStyleName(ValoTheme.BUTTON_LINK);

		deleteSelectedVersions = new ConfirmDialogButton($("delete.icon") + " " + $("DisplayDocumentView.deleteSelectedVersionsLabel")) {
			@Override
			protected void confirmButtonClick(ConfirmDialog dialog) {
				HashSet<ContentVersionVO> selectedContentVersions = versionTable.getSelectedContentVersions();
				for (ContentVersionVO contentVersionVO : selectedContentVersions) {
					presenter.deleteContentVersionButtonClicked(contentVersionVO);
				}
				versionTable.removeAllSelection();
			}

			@Override
			public boolean isVisible() {
				return presenter.isDeleteContentVersionPossible();
			}

			@Override
			public boolean isEnabled() {
				return versionTable.getContentVersions() != null && versionTable.getContentVersions().size() > 1 && !versionTable.getSelectedContentVersions().isEmpty();
			}

			@Override
			protected String getConfirmDialogMessage() {
				return $("DisplayDocumentView.deleteSelectedVersionsConfirmation");
			}
		};
		deleteSelectedVersions.setEnabled(deleteSelectedVersions.isEnabled());
		deleteSelectedVersions.addStyleName(ValoTheme.BUTTON_LINK);

		buttonsLayout.addComponents(diffButton, deleteSelectedVersions);

		tabLayout.addComponents(buttonsLayout, versionTable);
		return tabLayout;
	}

	@Override
	protected BaseBreadcrumbTrail buildBreadcrumbTrail() {
		String saveSearchDecommissioningId = null;
		String searchTypeAsString = null;
		String favGroupIdKey = null;

		if (presenter.getParams() != null) {
			if (presenter.getParams().get("decommissioningSearchId") != null) {
				saveSearchDecommissioningId = presenter.getParams().get("decommissioningSearchId");

			}

			if (presenter.getParams().get("decommissioningType") != null) {
				searchTypeAsString = presenter.getParams().get("decommissioningType");
			}

			favGroupIdKey = presenter.getParams().get(RMViews.FAV_GROUP_ID_KEY);
		}

		SearchType searchType = null;
		if (searchTypeAsString != null) {
			searchType = SearchType.valueOf((searchTypeAsString));
		}

		BaseBreadcrumbTrail breadcrumbTrail;

		RMModuleExtensions rmModuleExtensions = getConstellioFactories().getAppLayerFactory().getExtensions()
				.forCollection(getCollection()).forModule(ConstellioRMModule.ID);
		breadcrumbTrail = rmModuleExtensions.getBreadCrumbtrail(
				new DocumentFolderBreadCrumbParams(presenter.getDocument().getId(), presenter.getParams(), this));

		if (breadcrumbTrail != null) {
			return breadcrumbTrail;
		} else if (favGroupIdKey != null) {
			return new FolderDocumentContainerBreadcrumbTrail(documentVO.getId(), null, null, favGroupIdKey, this);
		} else if (saveSearchDecommissioningId != null && searchType != null) {
			return new DecommissionBreadcrumbTrail($("DecommissioningBuilderView.viewTitle." + searchType.name()), searchType,
					saveSearchDecommissioningId, presenter.getRecord().getId(), this);
		} else {
			String containerId = null;
			if (presenter.getParams() != null && presenter.getParams() instanceof Map) {
				containerId = presenter.getParams().get("containerId");
			}

			return new FolderDocumentContainerBreadcrumbTrail(documentVO.getId(), taxonomyCode, containerId, this);
		}
	}

	@Override
	public void refreshMetadataDisplay() {
		recordDisplay.refresh();
	}

	@Override
	public boolean isBackgroundViewMonitor() {
		return true;
	}

	@Override
	protected void onBackgroundViewMonitor() {
		presenter.backgroundViewMonitor();
		if (waitForContentViewerToBecomeVisible) {
			refreshContentViewer();
		}
	}

	//	@Override
	//	protected ClickListener getBackButtonClickListener() {
	//		return new ClickListener() {
	//			@Override
	//			public void buttonClick(ClickEvent event) {
	//				presenter.backButtonClicked();
	//			}
	//		};
	//	}

	@Override
	public void setTasks(final RecordVODataProvider dataProvider) {
		Table tasksTable = new RecordVOTable(dataProvider) {
			@Override
			protected Component buildMetadataComponent(Object itemId, MetadataValueVO metadataValue,
													   RecordVO recordVO) {
				if (Task.STARRED_BY_USERS.equals(metadataValue.getMetadata().getLocalCode())) {
					return new StarredFieldImpl(recordVO.getId(), (List<String>) metadataValue.getValue(), getSessionContext().getCurrentUser().getId()) {
						@Override
						public void updateTaskStarred(boolean isStarred, String taskId) {
							presenter.updateTaskStarred(isStarred, taskId, dataProvider);
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
					if (property != null && property instanceof MetadataVO && Task.STARRED_BY_USERS.equals(((MetadataVO) property).getLocalCode())) {
						iterator.remove();
					}
				}
				return sortableContainerPropertyIds;
			}
		};
		tasksTable.setSizeFull();
		tasksTable.addItemClickListener(new ItemClickListener() {
			@Override
			public void itemClick(ItemClickEvent event) {
				RecordVOItem item = (RecordVOItem) event.getItem();
				RecordVO recordVO = item.getRecord();
				presenter.taskClicked(recordVO);
			}
		});
		Component oldTasksComponent = tasksComponent;
		tasksComponent = tasksTable;
		tabSheet.replaceComponent(oldTasksComponent, tasksComponent);
	}

	@Override
	public void setEvents(RecordVODataProvider dataProvider) {
		RecordVOTable table = new RecordVOTable($("DisplayDocumentView.tabs.logs"), new RecordVOLazyContainer(dataProvider, false)) {
			@Override
			protected TableColumnsManager newColumnsManager() {
				return new EventVOTableColumnsManager();
			}
		};
		table.setSizeFull();

		tabSheet.replaceComponent(eventsComponent, table);
		eventsComponent = table;
	}

	@Override
	protected List<Button> getQuickActionMenuButtons() {
		List<Button> quickActionMenuButtons = new ArrayList<>();
		if (nestedView) {
			quickActionMenuButtons.add(displayDocumentButton);
		}
		quickActionMenuButtons.add(openDocumentButton);
		quickActionMenuButtons.add(editDocumentButton);
		return quickActionMenuButtons;
	}

	@Override
	protected List<Button> buildActionMenuButtons(ViewChangeEvent event) {
		displayDocumentButton = new DisplayButton($("DisplayDocumentView.displayDocument"), false) {
			@Override
			protected void buttonClick(ClickEvent event) {
				presenter.displayDocumentButtonClicked();
			}
		};
		displayDocumentButton.addStyleName(ValoTheme.BUTTON_LINK);
		displayDocumentButton.addStyleName("display-document-link");

		openDocumentButton = new LinkButton($("DisplayDocumentView.openDocument")) {
			@Override
			protected void buttonClick(ClickEvent event) {
				presenter.openDocumentButtonClicked();
			}
		};
		openDocumentButton.addStyleName(ValoTheme.BUTTON_LINK);
		openDocumentButton.addStyleName("open-document-link");

		if (((DocumentVO) documentVO).getContent() != null) {
			downloadDocumentButton = new DownloadContentVersionLink(((DocumentVO) documentVO).getContent(),
					$("DisplayDocumentView.downloadDocument"));
			downloadDocumentButton.addStyleName("download-document-link");

			openDocumentButton.setIcon(downloadDocumentButton.getIcon());
			downloadDocumentButton.setIcon(new ThemeResource("images/icons/actions/download.png"));
		}

		editDocumentButton = new EditButton($("DisplayDocumentView.editDocument")) {
			@Override
			protected void buttonClick(ClickEvent event) {
				presenter.editDocumentButtonClicked();
			}
		};

		List<String> excludedActionTypes = Arrays.asList(
				DocumentMenuItemActionType.DOCUMENT_DISPLAY.name(),
				DocumentMenuItemActionType.DOCUMENT_OPEN.name(),
				DocumentMenuItemActionType.DOCUMENT_EDIT.name());
		return new RecordVOActionButtonFactory(documentVO, excludedActionTypes).build();
	}

	@Override
	protected Component buildActionMenu(ViewChangeEvent event) {
		Component actionMenu = super.buildActionMenu(event);
		if (!nestedView && actionMenuBarLayout != null) {
			actionMenuBarLayout.addStyleName("not-nested-action-menu-bar-layout");
		}
		return actionMenu;
	}

	public void navigateToSelf() {
		presenter.navigateToSelf();
	}

	private void initUploadWindow() {
		if (uploadWindow == null) {
			if (documentVO != null) {
				Map<RecordVO, MetadataVO> record = new HashMap<>();
				record.put(documentVO, documentVO.getMetadata(Document.CONTENT));
				uploadWindow = new UpdateContentVersionWindowImpl(record) {
					@Override
					public void close() {
						super.close();
						presenter.updateWindowClosed();
						presenter.updateContentVersions();
						versionTable.refreshRowCache();
					}
				};
			}
		}
	}

	@Override
	public void drop(DragAndDropEvent event) {
		openUploadWindow(false);
		uploadWindow.drop(event);
	}

	@Override
	public AcceptCriterion getAcceptCriterion() {
		initUploadWindow();
		if (uploadWindow != null) {
			return uploadWindow.getAcceptCriterion();
		} else {
			return AcceptAll.get();
		}

	}

	@Override
	public void openUploadWindow(boolean checkingIn) {
		uploadWindow = null;
		initUploadWindow();
		uploadWindow.open(checkingIn);
	}

	@Override
	public void setCopyDocumentButtonState(ComponentState state) {
	}

	@Override
	public void setStartWorkflowButtonState(ComponentState state) {
	}

	@Override
	public void setUploadButtonState(ComponentState state) {
	}

	@Override
	public void setCheckInButtonState(ComponentState state) {
	}

	@Override
	public void setAlertWhenAvailableButtonState(ComponentState state) {
	}

	@Override
	public void setCheckOutButtonState(ComponentState state) {
	}

	@Override
	public void setCartButtonState(ComponentState state) {
	}

	@Override
	public void setAddToOrRemoveFromSelectionButtonState(ComponentState state) {
	}

	@Override
	public void setGenerateMetadataButtonState(ComponentState state) {
	}

	@Override
	public void setPublishButtonState(ComponentState state) {
	}

	@Override
	public void setFinalizeButtonState(ComponentState state) {
	}

	@Override
	public void setDisplayDocumentButtonState(ComponentState state) {
		displayDocumentButton.setVisible(state.isVisible());
		displayDocumentButton.setEnabled(state.isVisible());
		actionButtonStateChanged(displayDocumentButton);
	}

	@Override
	public void setOpenDocumentButtonState(ComponentState state) {
		openDocumentButton.setVisible(state.isVisible());
		openDocumentButton.setEnabled(state.isVisible());
		actionButtonStateChanged(openDocumentButton);
	}

	@Override
	public void setDownloadDocumentButtonState(ComponentState state) {
		if (downloadDocumentButton != null) {
			downloadDocumentButton.setVisible(state.isVisible());
			downloadDocumentButton.setEnabled(state.isVisible());
		}
	}

	@Override
	public void setEditDocumentButtonState(ComponentState state) {
		editDocumentButton.setVisible(state.isVisible());
		editDocumentButton.setEnabled(state.isEnabled());
		actionButtonStateChanged(editDocumentButton);
	}

	@Override
	public void setAddDocumentButtonState(ComponentState state) {
	}

	@Override
	public void setDeleteDocumentButtonState(ComponentState state) {
	}

	@Override
	public void setViewAuthorizationButtonState(ComponentState state) {
	}

	@Override
	public void setShareDocumentButtonState(ComponentState state) {
	}

	@Override
	public void setCreatePDFAButtonState(ComponentState state) {
	}

	@Override
	public void setBorrowedMessage(String borrowedMessageKey, String... args) {
		if (StringUtils.isNotBlank(borrowedMessageKey)) {
			borrowedLabel.setVisible(true);
			borrowedLabel.setValue($(borrowedMessageKey, (Object[]) args));
		} else {
			borrowedLabel.setVisible(false);
			borrowedLabel.setValue(null);
		}
	}

	@Override
	public void setPublishButtons(boolean published) {
	}

	@Override
	public void openAgentURL(String agentURL) {
		Page.getCurrent().open(agentURL, null);
	}

	@Override
	public void setTaxonomyCode(String taxonomyCode) {
		this.taxonomyCode = taxonomyCode;
	}

	public void addTabSheetDecorator(TabSheetDecorator decorator) {
		this.tabSheetDecorators.add(decorator);
	}

	public List<TabSheetDecorator> getTabSheetDecorators() {
		return this.tabSheetDecorators;
	}

	public void removeTabSheetDecorator(TabSheetDecorator decorator) {
		this.tabSheetDecorators.remove(decorator);
	}

	@Override
	public RecordVO getRecordVO() {
		return documentVO;
	}

	@Override
	public void refreshParent() {
		// No parent
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
	public void openInWindow() {
		DisplayDocumentViewImpl displayView = new DisplayDocumentViewImpl(documentVO, true, true);
		Window window = new DisplayDocumentWindow(displayView);
		for (Window.CloseListener closeListener : editWindowCloseListeners) {
			window.addCloseListener(closeListener);
		}
		getUI().addWindow(window);
	}

	@Override
	public void editInWindow() {
		AddEditDocumentViewImpl editView = new AddEditDocumentViewImpl(documentVO, true);
		Window window = new AddEditDocumentWindow(editView);
		for (Window.CloseListener closeListener : editWindowCloseListeners) {
			window.addCloseListener(closeListener);
		}
		getUI().addWindow(window);
	}

	public void addEditWindowCloseListener(Window.CloseListener closeListener) {
		this.editWindowCloseListeners.add(closeListener);
	}

	public void removeEditWindowCloseListener(Window.CloseListener closeListener) {
		this.editWindowCloseListeners.add(closeListener);
	}

	public List<Window.CloseListener> getEditWindowCloseListeners() {
		return this.editWindowCloseListeners;
	}

}
