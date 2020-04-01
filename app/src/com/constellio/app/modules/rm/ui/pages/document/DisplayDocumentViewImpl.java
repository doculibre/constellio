package com.constellio.app.modules.rm.ui.pages.document;

import com.constellio.app.api.extensions.params.DocumentFolderBreadCrumbParams;
import com.constellio.app.modules.rm.ConstellioRMModule;
import com.constellio.app.modules.rm.constants.RMPermissionsTo;
import com.constellio.app.modules.rm.extensions.api.RMModuleExtensions;
import com.constellio.app.modules.rm.navigation.RMViews;
import com.constellio.app.modules.rm.services.decommissioning.SearchType;
import com.constellio.app.modules.rm.services.menu.DocumentMenuItemServices.DocumentMenuItemActionType;
import com.constellio.app.modules.rm.ui.components.RMMetadataDisplayFactory;
import com.constellio.app.modules.rm.ui.components.breadcrumb.FolderDocumentContainerBreadcrumbTrail;
import com.constellio.app.modules.rm.ui.components.breadcrumb.FolderDocumentContainerPresenterParam;
import com.constellio.app.modules.rm.ui.entities.DocumentVO;
import com.constellio.app.modules.rm.ui.pages.decommissioning.breadcrumb.DecommissionBreadcrumbTrail;
import com.constellio.app.modules.rm.ui.pages.extrabehavior.ProvideSecurityWithNoUrlParamSupport;
import com.constellio.app.modules.rm.ui.pages.extrabehavior.SecurityWithNoUrlParamSupport;
import com.constellio.app.modules.rm.wrappers.Document;
import com.constellio.app.modules.tasks.model.wrappers.Task;
import com.constellio.app.modules.tasks.ui.components.fields.StarredFieldImpl;
import com.constellio.app.services.migrations.VersionsComparator;
import com.constellio.app.ui.application.Navigation;
import com.constellio.app.ui.entities.AuthorizationVO;
import com.constellio.app.ui.entities.ContentVersionVO;
import com.constellio.app.ui.entities.MetadataVO;
import com.constellio.app.ui.entities.MetadataValueVO;
import com.constellio.app.ui.entities.RecordVO;
import com.constellio.app.ui.framework.buttons.ConfirmDialogButton;
import com.constellio.app.ui.framework.buttons.DeleteButton;
import com.constellio.app.ui.framework.buttons.DisplayButton;
import com.constellio.app.ui.framework.buttons.EditButton;
import com.constellio.app.ui.framework.buttons.LinkButton;
import com.constellio.app.ui.framework.buttons.WindowButton;
import com.constellio.app.ui.framework.buttons.WindowButton.WindowConfiguration;
import com.constellio.app.ui.framework.components.ComponentState;
import com.constellio.app.ui.framework.components.RecordDisplay;
import com.constellio.app.ui.framework.components.ViewWindow;
import com.constellio.app.ui.framework.components.breadcrumb.BaseBreadcrumbTrail;
import com.constellio.app.ui.framework.components.buttons.RecordVOActionButtonFactory;
import com.constellio.app.ui.framework.components.content.DownloadContentVersionLink;
import com.constellio.app.ui.framework.components.content.UpdateContentVersionWindowImpl;
import com.constellio.app.ui.framework.components.diff.DiffPanel;
import com.constellio.app.ui.framework.components.layouts.I18NHorizontalLayout;
import com.constellio.app.ui.framework.components.splitpanel.CollapsibleHorizontalSplitPanel;
import com.constellio.app.ui.framework.components.table.BaseTable;
import com.constellio.app.ui.framework.components.table.ContentVersionVOTable;
import com.constellio.app.ui.framework.components.table.RecordVOTable;
import com.constellio.app.ui.framework.components.table.columns.EventVOTableColumnsManager;
import com.constellio.app.ui.framework.components.table.columns.TableColumnsManager;
import com.constellio.app.ui.framework.components.table.columns.TaskVOTableColumnsManager;
import com.constellio.app.ui.framework.components.viewers.ContentViewer;
import com.constellio.app.ui.framework.containers.ButtonsContainer;
import com.constellio.app.ui.framework.containers.ButtonsContainer.ContainerButton;
import com.constellio.app.ui.framework.containers.RecordVOLazyContainer;
import com.constellio.app.ui.framework.data.RecordVODataProvider;
import com.constellio.app.ui.framework.decorators.tabs.TabSheetDecorator;
import com.constellio.app.ui.framework.exception.UserException.UserDoesNotHaveAccessException;
import com.constellio.app.ui.framework.items.RecordVOItem;
import com.constellio.app.ui.pages.base.BaseViewImpl;
import com.constellio.app.ui.pages.management.authorizations.ListAuthorizationsViewImpl.AuthorizationSource;
import com.constellio.app.ui.pages.management.authorizations.ListAuthorizationsViewImpl.Authorizations;
import com.constellio.app.ui.pages.management.authorizations.ListAuthorizationsViewImpl.EditAuthorizationButton;
import com.constellio.app.ui.util.ComponentTreeUtils;
import com.constellio.app.ui.util.ResponsiveUtils;
import com.constellio.data.dao.services.Stats;
import com.constellio.data.utils.dev.Toggle;
import com.vaadin.data.Container;
import com.vaadin.data.util.BeanItemContainer;
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
import com.vaadin.ui.JavaScript;
import com.vaadin.ui.JavaScriptFunction;
import com.vaadin.ui.Label;
import com.vaadin.ui.Notification;
import com.vaadin.ui.Panel;
import com.vaadin.ui.TabSheet;
import com.vaadin.ui.Table;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.Window;
import com.vaadin.ui.themes.ValoTheme;
import elemental.json.JsonArray;
import lombok.extern.slf4j.Slf4j;
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

import static com.constellio.app.modules.rm.constants.RMPermissionsTo.MANAGE_DOCUMENT_AUTHORIZATIONS;
import static com.constellio.app.ui.i18n.i18n.$;
import static com.constellio.app.ui.pages.management.authorizations.ListAuthorizationsViewImpl.DisplayMode.PRINCIPALS;

@Slf4j
public class DisplayDocumentViewImpl extends BaseViewImpl implements DisplayDocumentView, DropHandler, ProvideSecurityWithNoUrlParamSupport {

	public static final int RECORD_DISPLAY_WIDTH = 50;
	public static final Unit RECORD_DISPLAY_WIDTH_UNIT = Unit.PERCENTAGE;
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
	private Component sharesComponent;
	private UpdateContentVersionWindowImpl uploadWindow;
	private DisplayButton displayDocumentButton;
	private LinkButton openDocumentButton;
	private DownloadContentVersionLink downloadDocumentButton;
	private EditButton editDocumentButton;
	private ConfirmDialogButton deleteSelectedVersions;
	private boolean isContentViewerInSplitPanel = false;
	private boolean isInSeparateTab = false;

	private boolean contentViewerInitiallyVisible;
	private boolean waitForContentViewerToBecomeVisible;

	private List<TabSheetDecorator> tabSheetDecorators = new ArrayList<>();

	private DisplayDocumentPresenter presenter;

	private boolean nestedView;
	private boolean inWindow;

	private List<Window.CloseListener> editWindowCloseListeners = new ArrayList<>();
	private Component contentMetadataComponent;

	private CollapsibleHorizontalSplitPanel splitPanel;
	private String searchTerm;


	public DisplayDocumentViewImpl() {
		this(null, false, false);
	}

	public DisplayDocumentViewImpl(RecordVO recordVO, boolean nestedView, boolean inWindow) {
		this.nestedView = nestedView;
		this.inWindow = inWindow;
		presenter = Stats.compilerFor(getClass()).log(() -> new DisplayDocumentPresenter(this, recordVO, nestedView, inWindow));
	}

	public DisplayDocumentPresenter getDisplayDocumentPresenter() {
		return presenter;
	}

	public void setSearchTerm(String searchTerm) {
		this.searchTerm = searchTerm;
	}

	@Override
	protected void initBeforeCreateComponents(ViewChangeEvent event) {
		if (event != null) {
			presenter.forParams(event.getParameters());
		}
	}

	@Override
	protected void afterViewAssembled(ViewChangeEvent event) {
		if (!contentViewer.isViewerComponentVisible()
			&& contentMetadataComponent instanceof CollapsibleHorizontalSplitPanel
			&& ((CollapsibleHorizontalSplitPanel) contentMetadataComponent).getRealFirstComponent() == contentViewer) {
			mainLayout.replaceComponent(contentMetadataComponent, tabSheet);
		}
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

	private ContentViewer newContentViewer() {
		ContentVersionVO contentVersionVO = documentVO.get(Document.CONTENT);
		final ContentViewer contentViewer = new ContentViewer(presenter.getAppLayerFactory(),
				documentVO, Document.CONTENT, contentVersionVO);

		contentViewer.setSearchTerm(searchTerm);

		if (inWindow && !isViewerInSeparateTab()) {
			//			int viewerHeight = Page.getCurrent().getBrowserWindowHeight() - 125;
			//			contentViewer.setHeight(viewerHeight + "px");

			final String functionId = "adjustContentViewerHeight";
			JavaScript.getCurrent().addFunction(functionId,
					new JavaScriptFunction() {
						@Override
						public void call(JsonArray arguments) {
							int splitterDivHeight = (int) arguments.getNumber(0);
							int newViewerHeight = splitterDivHeight - 40;
							contentViewer.setHeight(newViewerHeight + "px");
							splitPanel.setFirstComponentHeight(newViewerHeight, Unit.PIXELS);

							if (contentViewer.isVerticalScroll()) {
								splitPanel.addStyleName("first-component-no-vertical-scroll");
							}
						}
					});

			StringBuilder js = new StringBuilder();
			js.append("  var splitterDiv =  document.getElementsByClassName('v-splitpanel-hsplitter')[0];");
			js.append("  var splitterDivHeight =  constellio_getHeight(splitterDiv);");
			js.append(functionId + "(splitterDivHeight);");
			JavaScript.getCurrent().execute(js.toString());
		}

		if (inWindow) {
			contentViewer.setSpecialCaseHeight("100%");
			contentViewer.setHeight("100%");
			mainLayout.setHeight("100%");
		}

		return contentViewer;
	}

	@Override
	public void refreshContentViewer() {
		ContentViewer newContentViewer = newContentViewer();
		if (newContentViewer.isViewerComponentVisible()) {

			if (!isInSeparateTab && !isContentViewerInSplitPanel) {
				if (isViewerInSeparateTab()) {
					tabSheet.addTab(newContentViewer, 0);
					contentViewer = newContentViewer;
					isInSeparateTab = true;
				} else {
					isContentViewerInSplitPanel = true;
					contentViewer = newContentViewer;
					Component splitPanel = createSplitPanel();

					mainLayout.replaceComponent(contentMetadataComponent, splitPanel);
					contentMetadataComponent = splitPanel;
				}
			} else if (isContentViewerInSplitPanel) {
				splitPanel.setFirstComponent(newContentViewer);
				contentViewer = newContentViewer;
			} else {
				tabSheet.replaceComponent(contentViewer, newContentViewer);
				contentViewer = newContentViewer;
				waitForContentViewerToBecomeVisible = false;
			}
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

		ContentVersionVO contentVersionVO
				= (ContentVersionVO) documentVO.get(Document.CONTENT);
		versionTable = new ContentVersionVOTable("DocumentVersions", new ArrayList<>(), presenter.getAppLayerFactory(),
				presenter.hasCurrentUserPermissionToViewFileSystemName(),
				getRecordVO().getId(),
				Document.CONTENT,
				presenter.canEditOldVersion() && presenter.hasWritePermission(), contentVersionVO != null ? contentVersionVO.getVersion() : null) {
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
		sharesComponent = new CustomComponent();
		versionTable.setSizeFull();

		Panel recordDisplayPanel = new Panel(recordDisplay);
		recordDisplayPanel.addStyleName(ValoTheme.PANEL_BORDERLESS);
		//		recordDisplayPanel.addStyleName("panel-no-scroll");
		recordDisplayPanel.setSizeFull();

		recordDisplayPanel.addStyleName(ValoTheme.PANEL_BORDERLESS);
		recordDisplayPanel.addStyleName(ValoTheme.PANEL_SCROLL_INDICATOR);
		if (contentViewerInitiallyVisible && isViewerInSeparateTab()) {
			tabSheet.addTab(contentViewer, $("DisplayDocumentView.tabs.contentViewer"));
			isInSeparateTab = true;

		}
		tabSheet.addTab(recordDisplayPanel, $("DisplayDocumentView.tabs.metadata"));
		tabSheet.addTab(buildVersionTab(), $("DisplayDocumentView.tabs.versions"));
		tabSheet.addTab(tasksComponent, $("DisplayDocumentView.tabs.tasks"));
		tabSheet.addTab(sharesComponent, $("DisplayDocumentView.tabs.shares"));

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

				} else if (event.getTabSheet().getSelectedTab() == tasksComponent) {
					presenter.tasksTabSelected();

				} else if (event.getTabSheet().getSelectedTab() == sharesComponent) {
					sharesTabSelected();

				} else if (event.getTabSheet().getSelectedTab() == contentViewer) {
					contentViewer.refresh();
				}
			}
		});

		if (contentViewerInitiallyVisible && !isViewerInSeparateTab()) {
			createSplitPanel();
			isContentViewerInSplitPanel = true;
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

	private Component createSplitPanel() {
		splitPanel = new CollapsibleHorizontalSplitPanel(DisplayDocumentViewImpl.class.getName());
		splitPanel.setFirstComponent(contentViewer);
		splitPanel.setSecondComponent(tabSheet);
		splitPanel.setSecondComponentWidth(RECORD_DISPLAY_WIDTH, RECORD_DISPLAY_WIDTH_UNIT);

		if (inWindow) {
			splitPanel.getFirstComponentContainer().setHeightUndefined();
		}

		return splitPanel;
	}

	protected boolean isViewerInSeparateTab() {
		return nestedView || !ResponsiveUtils.isDesktop();
	}

	public void addComponentAfterMenu(Component component) {
		mainLayout.addComponent(component, 0);
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
	public void refreshActionMenu() {
		super.refreshActionMenu();
		com.vaadin.ui.JavaScript.eval("setTimeout(function () { if(typeof resetToCurrentValues  === \"function\") { resetToCurrentValues(); }}, 650)");
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

			return new FolderDocumentContainerBreadcrumbTrail(new FolderDocumentContainerPresenterParam(documentVO.getId(), null, null, favGroupIdKey, this));
		} else if (saveSearchDecommissioningId != null && searchType != null) {
			return new DecommissionBreadcrumbTrail($("DecommissioningBuilderView.viewTitle." + searchType.name()), searchType,
					saveSearchDecommissioningId, presenter.getRecord().getId(), this, false);
		} else {
			String containerId = null;
			if (presenter.getParams() != null && presenter.getParams() instanceof Map) {
				containerId = presenter.getParams().get("containerId");
			}

			return new FolderDocumentContainerBreadcrumbTrail(new FolderDocumentContainerPresenterParam(documentVO.getId(), taxonomyCode, containerId, null, this));
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
		tabSheet.replaceComponent(oldTasksComponent, tasksTable);
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
				contentViewer.releaseRessource();
				presenter.displayDocumentButtonClicked();
			}
		};
		displayDocumentButton.addStyleName(ValoTheme.BUTTON_LINK);
		displayDocumentButton.addStyleName("display-document-link");
		displayDocumentButton.setCaptionVisibleOnMobile(false);

		openDocumentButton = new LinkButton($("DisplayDocumentView.openDocument")) {
			@Override
			protected void buttonClick(ClickEvent event) {
				presenter.openDocumentButtonClicked();
			}
		};
		openDocumentButton.addStyleName(ValoTheme.BUTTON_LINK);
		openDocumentButton.addStyleName("open-document-link");
		openDocumentButton.setCaptionVisibleOnMobile(false);

		if (((DocumentVO) documentVO).getContent() != null) {
			downloadDocumentButton = new DownloadContentVersionLink(((DocumentVO) documentVO).getContent(),
					$("DisplayDocumentView.downloadDocument"), documentVO.getId(), Document.CONTENT, false);
			downloadDocumentButton.addStyleName("download-document-link");

			openDocumentButton.setIcon(downloadDocumentButton.getIcon());
			downloadDocumentButton.setIcon(new ThemeResource("images/icons/actions/download.png"));
		}

		editDocumentButton = new EditButton($("DisplayDocumentView.editDocument")) {
			@Override
			protected void buttonClick(ClickEvent event) {
				contentViewer.releaseRessource();
				presenter.editDocumentButtonClicked();
			}
		};
		editDocumentButton.setCaptionVisibleOnMobile(false);

		List<String> excludedActionTypes = Arrays.asList(
				DocumentMenuItemActionType.DOCUMENT_DISPLAY.name(),
				DocumentMenuItemActionType.DOCUMENT_OPEN.name(),
				DocumentMenuItemActionType.DOCUMENT_EDIT.name());
		List<Button> actionMenuButtons = new RecordVOActionButtonFactory(documentVO, this, excludedActionTypes).build();

		return actionMenuButtons;
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
	public void setUnshareDocumentButtonState(ComponentState state) {

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
	public String getTitle() {
		if (!nestedView) {
			return $("DisplayDocumentView.viewTitle");
		} else {
			return null;
		}
	}

	@Override
	protected boolean isActionMenuBar() {
		return true;
	}

	@Override
	protected boolean isBreadcrumbsVisible() {
		return !nestedView && !presenter.isInWindow();
	}

	@Override
	protected boolean isFullWidthIfActionMenuAbsent() {
		return true;
	}

	@Override
	public void openInWindow() {
		DisplayDocumentViewImpl displayView = new DisplayDocumentViewImpl(documentVO, false, true);
		Window window = null;
		try {
			window = new DisplayDocumentWindow(displayView);
		} catch (UserDoesNotHaveAccessException e) {
			log.error(e.getMessage(), e);
			return;
		}
		for (Window.CloseListener closeListener : editWindowCloseListeners) {
			window.addCloseListener(closeListener);
		}
		getUI().addWindow(window);
	}

	@Override
	public void editInWindow() {
		AddEditDocumentViewImpl editView = new AddEditDocumentViewImpl(documentVO, true);
		ViewWindow window;
		try {
			if (inWindow) {
				window = ComponentTreeUtils.findParent(this, ViewWindow.class);
				if (window instanceof ViewWindow) {
					window.setView(editView);
				}
			} else {
				window = new AddEditDocumentWindow(editView);

				for (Window.CloseListener closeListener : editWindowCloseListeners) {
					window.addCloseListener(closeListener);
				}
				getUI().addWindow(window);
			}
		} catch (UserDoesNotHaveAccessException e) {
			log.error(e.getMessage(), e);
			return;
		}
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

	@Override
	public SecurityWithNoUrlParamSupport getSecurityWithNoUrlParamSupport() {
		return presenter;
	}

	@Override
	public void sharesTabSelected() {
		Table table = buildAuthorizationTable(presenter.getSharedAuthorizations(), AuthorizationSource.OWN);
		tabSheet.replaceComponent(sharesComponent, table);
		sharesComponent = table;
		tabSheet.setSelectedTab(sharesComponent);
	}

	@Override
	public void removeAuthorization(AuthorizationVO authorizationVO) {
		if (sharesComponent instanceof Table) {
			((Table) sharesComponent).removeItem(authorizationVO);
		}
	}

	private Table buildAuthorizationTable(List<AuthorizationVO> authorizationVOs, AuthorizationSource source) {
		Container container = buildAuthorizationContainer(authorizationVOs, source);
		String tableCaption = "";
		Table table = new BaseTable(getClass().getName(), tableCaption, container);
		table.setPageLength(container.size());
		new Authorizations(source, PRINCIPALS, false, true, true, false, getSessionContext().getCurrentLocale()).attachTo(table, false);
		return table;
	}

	private Container buildAuthorizationContainer(List<AuthorizationVO> authorizationVOs, AuthorizationSource source) {
		BeanItemContainer<AuthorizationVO> authorizations = new BeanItemContainer<>(AuthorizationVO.class, authorizationVOs);
		return source == AuthorizationSource.OWN || source == AuthorizationSource.SHARED ?
			   addButtons(authorizations, source == AuthorizationSource.INHERITED) :
			   authorizations;
	}

	private Container addButtons(BeanItemContainer<AuthorizationVO> authorizations, final boolean inherited) {
		ButtonsContainer container = new ButtonsContainer<>(authorizations, Authorizations.BUTTONS);
		container.addButton(new ContainerButton() {
			@Override
			protected Button newButtonInstance(Object itemId, ButtonsContainer<?> container) {
				final AuthorizationVO authorization = (AuthorizationVO) itemId;
				EditAuthorizationButton button = new EditAuthorizationButton(authorization) {
					@Override
					protected void onSaveButtonClicked(AuthorizationVO authorizationVO) {
						presenter.onAutorizationModified(authorization);
					}

					@Override
					public boolean isVisible() {
						return super.isVisible() && getSessionContext().getCurrentUser().getId().equals(authorization.getSharedBy());
					}
				};
				button.setVisible(inherited || !authorization.isSynched());
				return button;
			}
		});
		container.addButton(new ContainerButton() {
			@Override
			protected Button newButtonInstance(final Object itemId, ButtonsContainer<?> container) {
				final AuthorizationVO authorization = (AuthorizationVO) itemId;
				DeleteButton deleteButton = new DeleteButton() {
					@Override
					protected void confirmButtonClick(ConfirmDialog dialog) {
						presenter.deleteAutorizationButtonClicked(authorization);
					}

					@Override
					public boolean isVisible() {
						return super.isVisible() &&
							   (presenter.getUser().getId().equals(authorization.getSharedBy()) ||
								presenter.getUser().hasAny(RMPermissionsTo.MANAGE_SHARE, MANAGE_DOCUMENT_AUTHORIZATIONS).on(getRecordVO().getRecord()));
					}
				};
				deleteButton.setVisible(inherited || !authorization.isSynched());
				return deleteButton;
			}
		});
		return container;
	}
}
