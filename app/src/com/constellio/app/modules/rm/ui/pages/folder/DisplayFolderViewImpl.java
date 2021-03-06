package com.constellio.app.modules.rm.ui.pages.folder;

import static com.constellio.app.modules.rm.constants.RMPermissionsTo.MANAGE_FOLDER_AUTHORIZATIONS;
import static com.constellio.app.modules.rm.services.menu.behaviors.util.DocumentUtil.getEmailDocumentFileNameValidator;
import static com.constellio.app.ui.i18n.i18n.$;
import static com.constellio.app.ui.pages.management.authorizations.ListAuthorizationsViewImpl.DisplayMode.PRINCIPALS;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;

import com.constellio.app.ui.framework.components.PlaceHolder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.vaadin.dialogs.ConfirmDialog;
import org.vaadin.sliderpanel.client.SliderPanelListener;

import com.constellio.app.modules.restapi.core.util.ListUtils;
import com.constellio.app.modules.rm.ConstellioRMModule;
import com.constellio.app.modules.rm.constants.RMPermissionsTo;
import com.constellio.app.modules.rm.extensions.api.RMModuleExtensions;
import com.constellio.app.modules.rm.services.menu.FolderMenuItemServices.FolderMenuItemActionType;
import com.constellio.app.modules.rm.ui.components.RMMetadataDisplayFactory;
import com.constellio.app.modules.rm.wrappers.Document;
import com.constellio.app.modules.tasks.model.wrappers.Task;
import com.constellio.app.modules.tasks.ui.components.fields.StarredFieldImpl;
import com.constellio.app.services.menu.MenuItemAction;
import com.constellio.app.services.menu.MenuItemFactory.MenuItemRecordProvider;
import com.constellio.app.ui.application.ConstellioUI;
import com.constellio.app.ui.application.Navigation;
import com.constellio.app.ui.entities.AuthorizationVO;
import com.constellio.app.ui.entities.ContentVersionVO;
import com.constellio.app.ui.entities.FacetVO;
import com.constellio.app.ui.entities.MetadataVO;
import com.constellio.app.ui.entities.MetadataValueVO;
import com.constellio.app.ui.entities.RecordVO;
import com.constellio.app.ui.framework.buttons.AddButton;
import com.constellio.app.ui.framework.buttons.BaseButton;
import com.constellio.app.ui.framework.buttons.DeleteButton;
import com.constellio.app.ui.framework.buttons.DisplayButton;
import com.constellio.app.ui.framework.buttons.DownloadLink;
import com.constellio.app.ui.framework.buttons.EditButton;
import com.constellio.app.ui.framework.buttons.LinkButton;
import com.constellio.app.ui.framework.buttons.SearchButton;
import com.constellio.app.ui.framework.components.BaseWindow;
import com.constellio.app.ui.framework.components.ComponentState;
import com.constellio.app.ui.framework.components.PlaceHolder;
import com.constellio.app.ui.framework.components.RecordDisplay;
import com.constellio.app.ui.framework.components.breadcrumb.BaseBreadcrumbTrail;
import com.constellio.app.ui.framework.components.buttons.RecordVOActionButtonFactory;
import com.constellio.app.ui.framework.components.content.ContentVersionVOResource;
import com.constellio.app.ui.framework.components.content.UpdateContentVersionWindowImpl;
import com.constellio.app.ui.framework.components.content.UpdateContentVersionWindowImpl.ValidateFileName;
import com.constellio.app.ui.framework.components.fields.autocomplete.StringAutocompleteField;
import com.constellio.app.ui.framework.components.fields.upload.ContentVersionUploadField;
import com.constellio.app.ui.framework.components.layouts.I18NHorizontalLayout;
import com.constellio.app.ui.framework.components.menuBar.ActionMenuDisplay;
import com.constellio.app.ui.framework.components.search.FacetsPanel;
import com.constellio.app.ui.framework.components.search.FacetsSliderPanel;
import com.constellio.app.ui.framework.components.selection.SelectionComponent.SelectionChangeEvent;
import com.constellio.app.ui.framework.components.selection.SelectionComponent.SelectionManager;
import com.constellio.app.ui.framework.components.table.BaseTable;
import com.constellio.app.ui.framework.components.table.RecordVOTable;
import com.constellio.app.ui.framework.components.table.columns.EventVOTableColumnsManager;
import com.constellio.app.ui.framework.components.table.columns.TableColumnsManager;
import com.constellio.app.ui.framework.components.table.columns.TaskVOTableColumnsManager;
import com.constellio.app.ui.framework.components.viewers.panel.ViewableRecordVOTablePanel;
import com.constellio.app.ui.framework.containers.ButtonsContainer;
import com.constellio.app.ui.framework.containers.ButtonsContainer.ContainerButton;
import com.constellio.app.ui.framework.containers.RecordVOLazyContainer;
import com.constellio.app.ui.framework.data.RecordVODataProvider;
import com.constellio.app.ui.framework.items.RecordVOItem;
import com.constellio.app.ui.handlers.OnEnterKeyHandler;
import com.constellio.app.ui.pages.base.BaseViewImpl;
import com.constellio.app.ui.pages.base.NavigationParams;
import com.constellio.app.ui.pages.management.authorizations.ListAuthorizationsViewImpl.AuthorizationSource;
import com.constellio.app.ui.pages.management.authorizations.ListAuthorizationsViewImpl.Authorizations;
import com.constellio.app.ui.pages.management.authorizations.ListAuthorizationsViewImpl.EditAuthorizationButton;
import com.constellio.app.ui.pages.search.SearchPresenter.SortOrder;
import com.constellio.data.dao.services.Stats;
import com.constellio.data.utils.KeySetMap;
import com.constellio.data.utils.dev.Toggle;
import com.vaadin.data.Container;
import com.vaadin.data.Property.ValueChangeListener;
import com.vaadin.data.util.BeanItemContainer;
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
import com.vaadin.ui.CheckBox;
import com.vaadin.ui.Component;
import com.vaadin.ui.CustomComponent;
import com.vaadin.ui.Html5File;
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
import org.vaadin.dialogs.ConfirmDialog;
import org.vaadin.sliderpanel.client.SliderPanelListener;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Supplier;

import static com.constellio.app.modules.rm.constants.RMPermissionsTo.MANAGE_FOLDER_AUTHORIZATIONS;
import static com.constellio.app.modules.rm.services.menu.behaviors.util.DocumentUtil.getEmailDocumentFileNameValidator;
import static com.constellio.app.ui.i18n.i18n.$;
import static com.constellio.app.ui.pages.management.authorizations.ListAuthorizationsViewImpl.DisplayMode.PRINCIPALS;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.vaadin.dialogs.ConfirmDialog;
import org.vaadin.sliderpanel.client.SliderPanelListener;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;

import static com.constellio.app.modules.rm.constants.RMPermissionsTo.MANAGE_FOLDER_AUTHORIZATIONS;
import static com.constellio.app.modules.rm.services.menu.behaviors.util.DocumentUtil.getEmailDocumentFileNameValidator;
import static com.constellio.app.ui.i18n.i18n.$;
import static com.constellio.app.ui.pages.management.authorizations.ListAuthorizationsViewImpl.DisplayMode.PRINCIPALS;

public class DisplayFolderViewImpl extends BaseViewImpl implements DisplayFolderView, DropHandler, BrowserWindowResizeListener, NavigationParams {

	private final static Logger LOGGER = LoggerFactory.getLogger(DisplayFolderViewImpl.class);

	public static final String STYLE_NAME = "display-folder";

	public static final String USER_LOOKUP = "user-lookup";
	private RecordVO summaryRecordVO;
	private String taxonomyCode;
	private VerticalLayout mainLayout;
	private ContentVersionUploadField uploadField;
	private TabSheet tabSheet;
	private Component recordDisplay;
	private FacetsSliderPanel facetsSliderPanel;
	private Component folderContentComponent;
	private ViewableRecordVOTablePanel viewerPanel;
	private Component tasksComponent;
	private Component eventsComponent;
	private Component sharesComponent;
	private DisplayFolderPresenter presenter;
	private RMModuleExtensions rmModuleExtensions;
	private boolean dragNDropAllowed;
	private boolean dragRowsEnabled;
	private Button displayFolderButton, editFolderButton, addDocumentButton, addSubfolderButton;
	private Label borrowedLabel;
	private StringAutocompleteField<String> searchField;
	private SearchButton searchButton;
	private CheckBox includeTreeCheckBox;
	private BaseButton clearSearchButton;
	private VerticalLayout searchLayout;
	private boolean disableMetadataTypeCheck = false;

	private Window documentVersionWindow;

	private I18NHorizontalLayout contentAndFacetsLayout;

	private RecordVODataProvider folderContentDataProvider;
	private RecordVODataProvider tasksDataProvider;
	private RecordVODataProvider eventsDataProvider;
	private RecordVODataProvider sharesDataProvider;

	private FacetsPanel facetsPanel;
	private boolean facetsPanelLoaded;

	private boolean nestedView;

	private boolean inWindow;

	private TabSheet.SelectedTabChangeListener selectedTabChangeListener;

	private boolean allContentItemsVisible;

	private BaseWindow bulkUploadWindow;
	private Boolean tableRebuildRequired;

	public DisplayFolderViewImpl() {
		this(null, false, false);
	}

	public DisplayFolderViewImpl(RecordVO recordVO, boolean nestedView, boolean inWindow) {
		this.nestedView = nestedView;
		this.inWindow = inWindow;

		presenter = Stats.compilerFor(getClass().getSimpleName()).log(() -> {
			return new DisplayFolderPresenter(this, recordVO, nestedView, inWindow);
		});
		rmModuleExtensions = getConstellioFactories().getAppLayerFactory()
				.getExtensions().forCollection(getCollection()).forModule(ConstellioRMModule.ID);
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

			addSubfolderButton = newAddSubfolderButton();
			addDocumentButton = newAddAddDocumentButton();
			displayFolderButton = newDisplayFolderButton();
			editFolderButton = newEditFolderButton();
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
	public RecordVO getSummaryRecord() {
		return summaryRecordVO;
	}

	@Override
	public void setSummaryRecord(RecordVO recordVO) {
		this.summaryRecordVO = recordVO;
	}

	@Override
	protected String getTitle() {
		return null;
	}

	@Override
	protected Component buildMainComponent(ViewChangeEvent event) {
		addStyleName("display-folder-view");
		addStyleName("nested-view-" + nestedView);

		mainLayout = new VerticalLayout();
		mainLayout.addStyleName("display-folder-view-main-layout");
		mainLayout.setSizeFull();
		mainLayout.setSpacing(true);

		final AtomicInteger uploadMaxCount = new AtomicInteger(0);
		final AtomicInteger streamProcessed = new AtomicInteger(0);

		uploadField = new ContentVersionUploadField(null, null) {
			@Override
			public boolean fireValueChangeWhenEqual() {
				return true;
			}

			@Override
			protected void onUploadWindowClosed(CloseEvent e) {
				presenter.uploadWindowClosed();
			}

			@Override
			public boolean mustUploadWithMinimumUIUpdatePossible(int fileToUploadCount) {
				uploadMaxCount.set(fileToUploadCount);
				return uploadMaxCount.get() > 1;
			}

			@Override
			public void streamingStarted(Html5File html5File, boolean isInterrupted) {
				super.streamingStarted(html5File, isInterrupted);

				if (streamProcessed.get() == 0 && uploadMaxCount.get() > 0 && !bulkUploadWindow.isAttached()) {
					ConstellioUI.getCurrent().addWindow(bulkUploadWindow);
				}
			}

			@Override
			public void streamingFinished(Html5File html5File, boolean isInterrupted) {
				super.streamingFinished(html5File, isInterrupted);

				if (streamProcessed.addAndGet(1) == uploadMaxCount.get()) {
					bulkUploadWindow.close();
					streamProcessed.set(0);
					uploadMaxCount.set(0);
				}
			}
		};

		uploadField.addStyleName("display-folder-upload-field");
		uploadField.setVisible(false);
		uploadField.setImmediate(true);
		uploadField.setMultiValue(false);
		uploadField.setMajorVersionFieldVisible(false);

		uploadField.addValueChangeListener(createValueChangeListener(uploadMaxCount));

		recordDisplay = new RecordDisplay(presenter.getRecordVOForDisplay(), new RMMetadataDisplayFactory(), Toggle.SEARCH_RESULTS_VIEWER.isEnabled());
		folderContentComponent = new PlaceHolder();
		tasksComponent = new CustomComponent();
		sharesComponent = new CustomComponent();

		tabSheet = new TabSheet();
		tabSheet.addStyleName(STYLE_NAME);
		tabSheet.addTab(folderContentComponent,
				$("DisplayFolderView.tabs.folderContent", presenter.getFolderContentCount()));
		tabSheet.addTab(recordDisplay, $("DisplayFolderView.tabs.metadata"));
		tabSheet.addTab(tasksComponent, $("DisplayFolderView.tabs.tasks", presenter.getTaskCount()));
		tabSheet.addTab(sharesComponent, $("DisplayFolderView.tabs.shares"));

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
				} else if (selectedTab == sharesComponent) {
					presenter.sharesTabSelected();
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
		contentAndFacetsLayout.addStyleName("folder-content-and-facets-layout");
		contentAndFacetsLayout.setWidth("100%");
		contentAndFacetsLayout.setExpandRatio(tabSheet, 1);

		mainLayout.addComponents(borrowedLabel, uploadField, contentAndFacetsLayout);

		return mainLayout;
	}

	private ValueChangeListener createValueChangeListener(AtomicInteger uploadMaxCount) {

		final List<ContentVersionVO> contentToAdd = new ArrayList<>();

		final String popupCssRoot = "display-folder-view-file-upload-bulk-popup";
		bulkUploadWindow = new BaseWindow();
		bulkUploadWindow.addStyleName(popupCssRoot);

		VerticalLayout windowLayout = new VerticalLayout();
		windowLayout.addStyleName(popupCssRoot + "-layout");

		final Label title = new Label();
		title.addStyleName("display-folder-view-file-upload-bulk-popup-title");
		title.setValue($("DisplayFolderView.ContentVersion.BulkUpload.title"));

		final Label uploadedFile = new Label();
		final Label remaningFiles = new Label();
		windowLayout.addComponents(title, uploadedFile, remaningFiles);


		I18NHorizontalLayout layout = new I18NHorizontalLayout();
		layout.setSizeUndefined();

		Label spinner = new Label();
		spinner.addStyleName(popupCssRoot + "-loading");

		layout.addComponents(spinner, windowLayout);

		bulkUploadWindow.setContent(layout);

		bulkUploadWindow.setModal(true);
		bulkUploadWindow.setResizable(false);

		bulkUploadWindow.addCloseListener(event -> {
			new Thread(() -> {
				presenter.contentVersionUploaded(contentToAdd);
				contentToAdd.clear();
			}).run();
		});

		return event -> {
			ContentVersionVO contentVersionVO = (ContentVersionVO) uploadField.getValue();
			contentToAdd.add(contentVersionVO);

			int uploadMaxCountValue = uploadMaxCount.get();

			ConstellioUI.getCurrent().access(() -> {
				uploadedFile.setValue($("DisplayFolderView.ContentVersion.BulkUpload.lastUploadedFileName", contentVersionVO.getFileName()));

				int remainingFiles = uploadMaxCountValue - contentToAdd.size();

				if (remainingFiles > 1) {
					remaningFiles.setValue($("DisplayFolderView.ContentVersion.BulkUpload.remaining", remainingFiles));
				} else {
					remaningFiles.setValue($("DisplayFolderView.ContentVersion.BulkUpload.remaining.single"));
				}
			});
		};
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

	private Button newAddSubfolderButton() {
		BaseButton addSubfolderButton;
		if (!presenter.isLogicallyDeleted()) {
			addSubfolderButton = new AddButton($("DisplayFolderView.addSubFolder")) {
				@Override
				protected void buttonClick(ClickEvent event) {
					presenter.addSubfolderButtonClicked();
				}
			};
			addSubfolderButton.setCaptionVisibleOnMobile(false);
		} else {
			addSubfolderButton = null;
		}
		return addSubfolderButton;
	}

	private Button newAddAddDocumentButton() {
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
	protected List<MenuItemAction> buildMenuItemActions(ViewChangeEvent event) {
		List<String> excludedActionTypes = new ArrayList<>();

		if (!isNestedView()) {
			excludedActionTypes.add(FolderMenuItemActionType.FOLDER_DISPLAY.name());
		}

		List<MenuItemAction> menuItemActions = buildRecordVOActionButtonFactory(excludedActionTypes).buildMenuItemActions();

		menuItemActions.stream()
				.filter(menuItemAction -> menuItemAction.getType().equals(FolderMenuItemActionType.FOLDER_ADD_SUBFOLDER))
				.forEach(menuItemAction -> updateMenuActionBasedOnButton(menuItemAction, addSubfolderButton));

		menuItemActions.stream()
				.filter(menuItemAction -> menuItemAction.getType().equals(FolderMenuItemActionType.FOLDER_ADD_DOCUMENT))
				.forEach(menuItemAction -> updateMenuActionBasedOnButton(menuItemAction, addDocumentButton));

		menuItemActions.stream()
				.filter(menuItemAction -> menuItemAction.getType().equals(FolderMenuItemActionType.FOLDER_DISPLAY))
				.forEach(menuItemAction -> updateMenuActionBasedOnButton(menuItemAction, displayFolderButton));

		menuItemActions.stream()
				.filter(menuItemAction -> menuItemAction.getType().equals(FolderMenuItemActionType.FOLDER_EDIT))
				.forEach(menuItemAction -> updateMenuActionBasedOnButton(menuItemAction, editFolderButton));

		return ListUtils.flatMapFilteringNull(
				super.buildMenuItemActions(event),
				menuItemActions
		);
	}

	@Override
	protected ActionMenuDisplay buildActionMenuDisplay(ActionMenuDisplay defaultActionMenuDisplay) {

		ActionMenuDisplay actionMenuDisplay = new ActionMenuDisplay(defaultActionMenuDisplay) {
			@Override
			public Supplier<String> getSchemaTypeCodeSupplier() {
				return presenter.getSchema()::getTypeCode;
			}

			@Override
			public Supplier<MenuItemRecordProvider> getMenuItemRecordProviderSupplier() {
				return buildRecordVOActionButtonFactory()::buildMenuItemRecordProvider;
			}

			@Override
			public int getQuickActionCount() {
				return isNestedView() ? 2 : ActionMenuDisplay.QUICK_ACTION_COUNT_DEFAULT;
			}
		};

		return actionMenuDisplay;
	}

	private RecordVOActionButtonFactory buildRecordVOActionButtonFactory() {

		return buildRecordVOActionButtonFactory(Collections.emptyList());
	}

	private RecordVOActionButtonFactory buildRecordVOActionButtonFactory(List<String> excludedActionTypes) {
		excludedActionTypes.addAll(rmModuleExtensions.getFilteredActionsForFolders());
		return new RecordVOActionButtonFactory(summaryRecordVO, excludedActionTypes);
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
	public RecordVODataProvider getFolderContentDataProvider() {
		return folderContentDataProvider;
	}

	@Override
	public void setFolderContent(RecordVODataProvider dataProvider) {
		if(folderContentDataProvider != null){
			tableRebuildRequired = true;
		}
		folderContentDataProvider = dataProvider;
	}

	@Override
	public void selectMetadataTab() {
		if (!(recordDisplay instanceof RecordDisplay) && !disableMetadataTypeCheck) {
			RecordDisplay newRecordDisplay = new RecordDisplay(presenter.getLazyFullFolderVO(), new RMMetadataDisplayFactory());
			tabSheet.replaceComponent(recordDisplay, newRecordDisplay);
			recordDisplay = newRecordDisplay;
		}

		tabSheet.setSelectedTab(recordDisplay);
	}


	@Override
	public void selectFolderContentTab() {
		tabSheet.removeSelectedTabChangeListener(selectedTabChangeListener);
		if (folderContentComponent instanceof PlaceHolder || tableRebuildRequired){
			final RecordVOLazyContainer recordVOContainer = new RecordVOLazyContainer(folderContentDataProvider);
			facetsPanel = new FacetsPanel(presenter.isFacetApplyButtonEnabled()) {
				@Override
				protected void sortCriterionSelected(String sortCriterion, SortOrder sortOrder) {
					presenter.sortCriterionSelected(sortCriterion, sortOrder);
				}

				@Override
				protected void facetValueSelected(String facetId, String value) {
					presenter.facetValueSelected(facetId, value);
				}

				@Override
				protected void facetValuesChanged(KeySetMap<String, String> facets) {
					presenter.facetValuesChanged(facets);
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

			viewerPanel = new ViewableRecordVOTablePanel(recordVOContainer) {
				@Override
				protected boolean isSelectColumn() {
					return !nestedView;
				}

				@Override
				public boolean isNested() {
					return nestedView;
				}

				@Override
				public void setQuickActionButtonsVisible(boolean visible) {
					super.setQuickActionButtonsVisible(visible);
					DisplayFolderViewImpl.this.setQuickActionButtonsVisible(visible);
				}

				@Override
				public boolean isDropSupported() {
					return dragNDropAllowed;
				}

				@Override
				public boolean isRowDragSupported() {
					return !isNested() && dragRowsEnabled;
				}

				@Override
				protected void recordsDroppedOn(List<RecordVO> sourceRecordVOs, RecordVO targetRecordVO,
												Boolean above) {
					if (dragNDropAllowed) {
						presenter.recordsDroppedOn(sourceRecordVOs, targetRecordVO);
					}
				}

				@Override
				protected SelectionManager newSelectionManager() {
					return new SelectionManager() {

						private Set<Object> selectedItemIds = new HashSet<>();

						@Override
						public List<Object> getAllSelectedItemIds() {
							return new ArrayList<>(selectedItemIds);
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
								selectedItemIds.addAll(getRecordVOContainer().getItemIds());
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
			viewerPanel.setAllItemsVisible(this.allContentItemsVisible);

			if (!nestedView && (folderContentDataProvider.size() > 0 || !folderContentDataProvider.getFieldFacetValues().isEmpty())) {
				if (facetsSliderPanel != null && facetsSliderPanel.getParent() != null) {
					contentAndFacetsLayout.removeComponent(facetsSliderPanel);
				}
				facetsSliderPanel = new FacetsSliderPanel(facetsPanel);
				facetsSliderPanel.addListener((SliderPanelListener) expand -> {
					if (expand && !facetsPanelLoaded) {
						refreshFacets(folderContentDataProvider);
						facetsPanelLoaded = true;
					}
				});
				contentAndFacetsLayout.addComponent(facetsSliderPanel);
			}

			if (includeTreeCheckBox == null) {
				includeTreeCheckBox = new CheckBox($("DisplayFolderView.includeTree"));
				includeTreeCheckBox.addStyleName("folder-search-include-tree");
			}
			if (clearSearchButton == null) {
				clearSearchButton = new LinkButton($("DisplayFolderView.clearSearch")) {
					@Override
					protected void buttonClick(ClickEvent event) {
						presenter.clearSearch();
						searchField.setValue("");
						includeTreeCheckBox.setValue(false);
					}
				};
				clearSearchButton.addStyleName("folder-search-clear");
			}
			BaseButton searchInFolderButton = new LinkButton($("DisplayFolderView.showSearchInFolder")) {
				@Override
				protected void buttonClick(ClickEvent event) {
					if (searchLayout != null) {
						if (searchLayout.isVisible()) {
							setCaption($("DisplayFolderView.showSearchInFolder"));
						} else {
							setCaption($("DisplayFolderView.hideSearchInFolder"));
						}
						searchLayout.setVisible(!searchLayout.isVisible());
						searchField.focus();
					}
				}
			};
			searchInFolderButton.addStyleName("search-in-folder-button");
			if (searchField == null) {
				searchField = new StringAutocompleteField<String>(new StringAutocompleteField.AutocompleteSuggestionsProvider<String>() {
					@Override
					public List<String> suggest(String text) {
						return presenter.getAutocompleteSuggestions(text);
					}

					@Override
					public Class<String> getModelType() {
						return String.class;
					}

					@Override
					public int getBufferSize() {
						return presenter.getAutocompleteBufferSize();
					}
				});
				searchField.setWidth("100%");
				searchField.addStyleName("folder-search-field");
				searchButton = new SearchButton() {
					@Override
					protected void buttonClick(ClickEvent event) {
						String value = searchField.getValue();
						presenter.changeFolderContentDataProvider(value, includeTreeCheckBox.getValue());
					}
				};
				searchButton.addStyleName("folder-search-button");
				searchButton.addStyleName(ValoTheme.BUTTON_PRIMARY);
				searchButton.setIconOnly(true);
				OnEnterKeyHandler onEnterHandler = new OnEnterKeyHandler() {
					@Override
					public void onEnterKeyPressed() {
						String value = searchField.getValue();
						presenter.changeFolderContentDataProvider(value, includeTreeCheckBox.getValue());
					}
				};
				onEnterHandler.installOn(searchField);
			}

			if (searchLayout == null) {
				searchLayout = new VerticalLayout();
				searchLayout.addStyleName("folder-search-layout");
				searchLayout.setSpacing(true);
				searchLayout.setWidth("50%");
				searchLayout.setVisible(false);

				I18NHorizontalLayout searchFieldAndButtonLayout = new I18NHorizontalLayout(searchField, searchButton);
				searchFieldAndButtonLayout.addStyleName("folder-search-field-and-button-layout");
				searchFieldAndButtonLayout.setWidth("100%");
				searchFieldAndButtonLayout.setExpandRatio(searchField, 1);

				I18NHorizontalLayout extraFieldsSearchLayout = new I18NHorizontalLayout(includeTreeCheckBox, clearSearchButton);
				extraFieldsSearchLayout.addStyleName("folder-search-extra-fields-layout");
				extraFieldsSearchLayout.setSpacing(true);

				searchLayout.addComponents(searchFieldAndButtonLayout, extraFieldsSearchLayout);
			}

			VerticalLayout searchToggleAndFieldsLayout = new VerticalLayout();
			searchToggleAndFieldsLayout.addStyleName("search-folder-toggle-and-fields-layout");
			searchToggleAndFieldsLayout.addComponent(searchInFolderButton);
			searchToggleAndFieldsLayout.addComponent(searchLayout);
			searchToggleAndFieldsLayout.addComponent(viewerPanel);
			tabSheet.replaceComponent(folderContentComponent, folderContentComponent = searchToggleAndFieldsLayout);
			viewerPanel.setSelectionActionButtons();
		}
		tabSheet.setSelectedTab(folderContentComponent);
		tabSheet.addSelectedTabChangeListener(selectedTabChangeListener);
		tableRebuildRequired = false;
	}

	public void setFacetsPanelVisible(boolean visible) {
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
									setColumnHeader(propertyId, " ");
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
		if (displayFolderButton != null) {
			displayFolderButton.setVisible(state.isVisible());
			displayFolderButton.setEnabled(state.isEnabled());

			refreshActionMenu();
		}
	}

	@Override
	public void setEditButtonState(ComponentState state) {
		if (editFolderButton != null) {
			editFolderButton.setVisible(state.isVisible());
			editFolderButton.setEnabled(state.isEnabled());

			refreshActionMenu();
		}
	}

	@Override
	public void setAddDocumentButtonState(ComponentState state) {
		dragNDropAllowed = state.isEnabled();

		if (addDocumentButton != null) {
			addDocumentButton.setVisible(state.isVisible());
			addDocumentButton.setEnabled(state.isEnabled());

			refreshActionMenu();
		}
	}

	@Override
	public void setAddSubfolderButtonState(ComponentState state) {
		dragNDropAllowed = state.isEnabled();

		if (addSubfolderButton != null) {
			addSubfolderButton.setVisible(state.isVisible());
			addSubfolderButton.setEnabled(state.isEnabled());

			refreshActionMenu();
		}
	}

	@Override
	public void setDragRowsEnabled(boolean enabled) {
		this.dragRowsEnabled = enabled;
	}

	@Override
	public void drop(DragAndDropEvent event) {
		if (dragNDropAllowed) {
			uploadField.drop(event);
		}
	}

	@Override
	public void showVersionUpdateWindow(final RecordVO recordVO, ContentVersionVO contentVersionVO) {
		final Map<RecordVO, MetadataVO> record = new HashMap<>();
		record.put(recordVO, recordVO.getMetadata(Document.CONTENT));

		ValidateFileName validateFileName = getEmailDocumentFileNameValidator(recordVO.getSchemaCode());

		UpdateContentVersionWindowImpl uploadField = new UpdateContentVersionWindowImpl(record, false, validateFileName) {
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
	public void closeDocumentContentVersionWindow() {
		documentVersionWindow.close();
	}

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
	protected String getActionMenuBarCaption() {
		return null;
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
		refreshFacets(folderContentDataProvider);
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

	@Override
	public Map<String, String> getNavigationParams() {
		return presenter.getParams();
	}

	@Override
	public void selectSharesTab() {
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
		new Authorizations(source, PRINCIPALS, false, true, true, presenter.seeSourceField(),
				false, getSessionContext().getCurrentLocale()).attachTo(table, false);
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
				EditAuthorizationButton button = new EditAuthorizationButton(authorization,
						presenter.getRecordVOForDisplay().getRecord(), presenter.getUser()) {
					@Override
					protected void onSaveButtonClicked(AuthorizationVO authorizationVO) {
						presenter.onAutorizationModified(authorization);
					}

					@Override
					public boolean isVisible() {
						return super.isVisible() && presenter.getUser().getId().equals(authorization.getSharedBy());
					}
				};
				button.setVisible(inherited || (!authorization.isSynched() && !authorization.isNested()));
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
								presenter.getUser().hasAny(RMPermissionsTo.MANAGE_SHARE, MANAGE_FOLDER_AUTHORIZATIONS).on(getSummaryRecord().getRecord()));
					}
				};
				deleteButton.setVisible(inherited || (!authorization.isSynched() && !authorization.isNested()));
				return deleteButton;
			}
		});
		return container;
	}

	@Override
	public Integer getSelectedFolderContentIndex() {
		Integer selectedFolderContentIndex;
		if (viewerPanel != null && viewerPanel.getPanelRecordIndex() != null) {
			selectedFolderContentIndex = viewerPanel.getPanelRecordIndex();
		} else {
			selectedFolderContentIndex = null;
		}
		return selectedFolderContentIndex;
	}

	@Override
	public RecordVO getSelectedFolderContentRecordVO() {
		RecordVO selectedFolderContentRecordVO;
		if (viewerPanel != null && viewerPanel.getPanelRecordVO() != null) {
			selectedFolderContentRecordVO = viewerPanel.getPanelRecordVO();
		} else {
			selectedFolderContentRecordVO = null;
		}
		return selectedFolderContentRecordVO;
	}

	@Override
	public DisplayFolderView getNestedDisplayFolderView() {
		DisplayFolderView nestedDisplayFolderView;
		if (viewerPanel != null && viewerPanel.getPanelContent() instanceof DisplayFolderViewImpl) {
			nestedDisplayFolderView = (DisplayFolderViewImpl) viewerPanel.getPanelContent();
		} else {
			nestedDisplayFolderView = null;
		}
		return nestedDisplayFolderView;
	}

	@Override
	public void closeViewerPanel() {
		if (viewerPanel != null) {
			viewerPanel.closePanel();
		}
	}

	@Override
	public void setAllContentItemsVisible(boolean visible) {
		this.allContentItemsVisible = visible;
	}

	public Component getFolderContentComponent() {
		return folderContentComponent;
	}

	public Component getMetadataComponent() {
		return recordDisplay;
	}

	public boolean isDisableMetadataTypeCheck() {
		return disableMetadataTypeCheck;
	}

	public void setDisableMetadataTypeCheck(boolean disableMetadataTypeCheck) {
		this.disableMetadataTypeCheck = disableMetadataTypeCheck;
	}

	public TabSheet getTabSheet() {
		return tabSheet;
	}

	public boolean isNestedView() {
		return nestedView;
	}

	public void addFacetsPanel(FacetsSliderPanel facetsSliderPanel) {
		if (facetsSliderPanel != null && facetsSliderPanel.getParent() != null) {
			contentAndFacetsLayout.removeComponent(facetsSliderPanel);
		}

		contentAndFacetsLayout.addComponents(facetsSliderPanel);
	}
}
