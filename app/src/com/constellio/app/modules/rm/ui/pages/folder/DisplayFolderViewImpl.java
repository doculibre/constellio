package com.constellio.app.modules.rm.ui.pages.folder;

import com.constellio.app.modules.rm.model.labelTemplate.LabelTemplate;
import com.constellio.app.modules.rm.services.borrowingServices.BorrowingType;
import com.constellio.app.modules.rm.ui.components.RMMetadataDisplayFactory;
import com.constellio.app.modules.rm.ui.components.content.DocumentContentVersionWindowImpl;
import com.constellio.app.modules.rm.ui.components.folder.fields.LookupFolderField;
import com.constellio.app.modules.rm.ui.entities.DocumentVO;
import com.constellio.app.modules.rm.ui.entities.FolderVO;
import com.constellio.app.modules.rm.ui.pages.cart.DefaultFavoritesTable;
import com.constellio.app.modules.rm.wrappers.Cart;
import com.constellio.app.modules.rm.wrappers.Document;
import com.constellio.app.modules.tasks.model.wrappers.Task;
import com.constellio.app.modules.tasks.ui.components.fields.StarredFieldImpl;
import com.constellio.app.ui.application.Navigation;
import com.constellio.app.ui.entities.ContentVersionVO;
import com.constellio.app.ui.entities.MetadataVO;
import com.constellio.app.ui.entities.MetadataValueVO;
import com.constellio.app.ui.entities.RecordVO;
import com.constellio.app.ui.framework.buttons.AddButton;
import com.constellio.app.ui.framework.buttons.AddToOrRemoveFromSelectionButton;
import com.constellio.app.ui.framework.buttons.BaseButton;
import com.constellio.app.ui.framework.buttons.DeleteButton;
import com.constellio.app.ui.framework.buttons.DeleteWithJustificationButton;
import com.constellio.app.ui.framework.buttons.DisplayButton;
import com.constellio.app.ui.framework.buttons.DownloadLink;
import com.constellio.app.ui.framework.buttons.EditButton;
import com.constellio.app.ui.framework.buttons.IconButton;
import com.constellio.app.ui.framework.buttons.LinkButton;
import com.constellio.app.ui.framework.buttons.WindowButton;
import com.constellio.app.ui.framework.buttons.WindowButton.WindowConfiguration;
import com.constellio.app.ui.framework.buttons.report.LabelButtonV2;
import com.constellio.app.ui.framework.components.BaseWindow;
import com.constellio.app.ui.framework.components.ComponentState;
import com.constellio.app.ui.framework.components.RecordDisplay;
import com.constellio.app.ui.framework.components.ReportTabButton;
import com.constellio.app.ui.framework.components.breadcrumb.BaseBreadcrumbTrail;
import com.constellio.app.ui.framework.components.content.ContentVersionVOResource;
import com.constellio.app.ui.framework.components.content.UpdateContentVersionWindowImpl;
import com.constellio.app.ui.framework.components.display.ReferenceDisplay;
import com.constellio.app.ui.framework.components.fields.BaseComboBox;
import com.constellio.app.ui.framework.components.fields.BaseTextField;
import com.constellio.app.ui.framework.components.fields.date.JodaDateField;
import com.constellio.app.ui.framework.components.fields.lookup.LookupRecordField;
import com.constellio.app.ui.framework.components.fields.upload.ContentVersionUploadField;
import com.constellio.app.ui.framework.components.table.RecordVOSelectionTableAdapter;
import com.constellio.app.ui.framework.components.table.RecordVOTable;
import com.constellio.app.ui.framework.components.table.columns.EventVOTableColumnsManager;
import com.constellio.app.ui.framework.components.table.columns.RecordVOTableColumnsManager;
import com.constellio.app.ui.framework.components.table.columns.TableColumnsManager;
import com.constellio.app.ui.framework.containers.ButtonsContainer;
import com.constellio.app.ui.framework.containers.ButtonsContainer.ContainerButton;
import com.constellio.app.ui.framework.containers.RecordVOLazyContainer;
import com.constellio.app.ui.framework.data.RecordVODataProvider;
import com.constellio.app.ui.framework.items.RecordVOItem;
import com.constellio.app.ui.pages.base.BaseViewImpl;
import com.constellio.app.ui.util.MessageUtils;
import com.constellio.data.utils.Factory;
import com.constellio.data.utils.TimeProvider;
import com.constellio.model.entities.records.wrappers.User;
import com.vaadin.data.Property.ValueChangeEvent;
import com.vaadin.data.Property.ValueChangeListener;
import com.vaadin.event.ItemClickEvent;
import com.vaadin.event.ItemClickEvent.ItemClickListener;
import com.vaadin.event.dd.DragAndDropEvent;
import com.vaadin.event.dd.DropHandler;
import com.vaadin.event.dd.acceptcriteria.AcceptAll;
import com.vaadin.event.dd.acceptcriteria.AcceptCriterion;
import com.vaadin.navigator.ViewChangeListener.ViewChangeEvent;
import com.vaadin.server.Page;
import com.vaadin.server.Resource;
import com.vaadin.server.ThemeResource;
import com.vaadin.shared.MouseEventDetails.MouseButton;
import com.vaadin.ui.Alignment;
import com.vaadin.ui.Button;
import com.vaadin.ui.ComboBox;
import com.vaadin.ui.Component;
import com.vaadin.ui.CustomComponent;
import com.vaadin.ui.Field;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Label;
import com.vaadin.ui.TabSheet;
import com.vaadin.ui.TabSheet.Tab;
import com.vaadin.ui.Table;
import com.vaadin.ui.UI;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.Window;
import com.vaadin.ui.themes.ValoTheme;
import org.joda.time.LocalDate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.vaadin.dialogs.ConfirmDialog;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import static com.constellio.app.ui.framework.buttons.WindowButton.WindowConfiguration.modalDialog;
import static com.constellio.app.ui.i18n.i18n.$;

public class DisplayFolderViewImpl extends BaseViewImpl implements DisplayFolderView, DropHandler {
	private final static Logger LOGGER = LoggerFactory.getLogger(DisplayFolderViewImpl.class);
	public static final String STYLE_NAME = "display-folder";
	public static final String USER_LOOKUP = "user-lookup";
	private RecordVO recordVO;
	private String taxonomyCode;
	private VerticalLayout mainLayout;
	private ContentVersionUploadField uploadField;
	private TabSheet tabSheet;
	private RecordDisplay recordDisplay;
	private Component folderContentComponent;
	private Component tasksComponent;
	private Component eventsComponent;
	private DisplayFolderPresenter presenter;
	private boolean dragNDropAllowed;
	private Button deleteFolderButton, duplicateFolderButton, editFolderButton, addSubFolderButton, addDocumentButton,
			addAuthorizationButton, shareFolderButton, printLabelButton, linkToFolderButton, borrowButton, returnFolderButton,
			reminderReturnFolderButton, alertWhenAvailableButton, addToCartButton, addToOrRemoveFromSelectionButton, startWorkflowButton, reportGeneratorButton;
	WindowButton moveInFolderButton;
	private Label borrowedLabel;

	private Window documentVersionWindow;

	private List<RecordVODataProvider> folderContentDataProviders;
	private RecordVODataProvider tasksDataProvider;
	private RecordVODataProvider eventsDataProvider;

	public DisplayFolderViewImpl() {
		this(null, false);
	}

	public DisplayFolderViewImpl(RecordVO recordVO, boolean popup) {
		presenter = new DisplayFolderPresenter(this, recordVO, popup);
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
		mainLayout.setSizeFull();
		mainLayout.setSpacing(true);

		uploadField = new ContentVersionUploadField() {
			@Override
			public boolean fireValueChangeWhenEqual() {
				return true;
			}
		} ;
		uploadField.setVisible(false);
		uploadField.setImmediate(true);
		uploadField.setMultiValue(false);
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
		tabSheet.addTab(recordDisplay, $("DisplayFolderView.tabs.metadata"));
		tabSheet.addTab(folderContentComponent,
				$("DisplayFolderView.tabs.folderContent", presenter.getFolderContentCount()));
		tabSheet.addTab(tasksComponent, $("DisplayFolderView.tabs.tasks", presenter.getTaskCount()));

		eventsComponent = new CustomComponent();
		tabSheet.addTab(eventsComponent, $("DisplayFolderView.tabs.logs"));
		if (presenter.hasCurrentUserPermissionToViewEvents()) {
			tabSheet.getTab(eventsComponent).setEnabled(true);
		} else {
			tabSheet.getTab(eventsComponent).setEnabled(false);
		}

		tabSheet.addSelectedTabChangeListener(new TabSheet.SelectedTabChangeListener() {
			@Override
			public void selectedTabChange(TabSheet.SelectedTabChangeEvent event) {
				Component selectedTab = tabSheet.getSelectedTab();
				if (selectedTab == recordDisplay) {
					presenter.metadataTabSelected();
				} else if (selectedTab == folderContentComponent) {
					presenter.folderContentTabSelected();
				} else if (selectedTab == tasksComponent) {
					presenter.tasksTabSelected();
				} else if (selectedTab == eventsComponent) {
					presenter.eventsTabSelected();
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

		mainLayout.addComponents(borrowedLabel, uploadField, tabSheet);
		presenter.selectInitialTabForUser();
		return mainLayout;
	}

	@Override
	protected BaseBreadcrumbTrail buildBreadcrumbTrail() {
		return presenter.getBreadCrumbTrail();
	}


	@Override
	protected List<Button> buildActionMenuButtons(ViewChangeEvent event) {
		List<Button> actionMenuButtons = new ArrayList<Button>();

		if (!presenter.isLogicallyDeleted()) {
			addDocumentButton = new AddButton($("DisplayFolderView.addDocument")) {
				@Override
				protected void buttonClick(ClickEvent event) {
					presenter.addDocumentButtonClicked();
				}
			};

			moveInFolderButton = new WindowButton($("DisplayFolderView.parentFolder"), $("DisplayFolderView.parentFolder")
					, WindowButton.WindowConfiguration.modalDialog("50%", "20%")) {
				@Override
				protected Component buildWindowContent() {
					VerticalLayout verticalLayout = new VerticalLayout();
					verticalLayout.setSpacing(true);
					final LookupFolderField field = new LookupFolderField();
					verticalLayout.addComponent(field);
					BaseButton saveButton = new BaseButton($("save")) {
						@Override
						protected void buttonClick(ClickEvent event) {
							String parentId = field.getValue();
							try {
								presenter.parentFolderButtonClicked(parentId);
							} catch (Throwable e) {
								LOGGER.warn("error when trying to modify folder parent to " + parentId, e);
								showErrorMessage("DisplayFolderView.parentFolderException");
							}
							moveInFolderButton.getWindow().close();
						}
					};
					saveButton.addStyleName(ValoTheme.BUTTON_PRIMARY);
					HorizontalLayout hLayout = new HorizontalLayout();
					hLayout.setSizeFull();
					hLayout.addComponent(saveButton);
					hLayout.setComponentAlignment(saveButton, Alignment.BOTTOM_RIGHT);
					verticalLayout.addComponent(hLayout);
					return verticalLayout;
				}
			};

			addSubFolderButton = new AddButton($("DisplayFolderView.addSubFolder")) {
				@Override
				protected void buttonClick(ClickEvent event) {
					presenter.addSubFolderButtonClicked();
				}
			};

			editFolderButton = new EditButton($("DisplayFolderView.editFolder")) {
				@Override
				protected void buttonClick(ClickEvent event) {
					presenter.editFolderButtonClicked();
				}
			};

			deleteFolderButton = new Button();
			if(!presenter.isNeedingAReasonToDeleteFolder()) {
				deleteFolderButton = new DeleteButton($("DisplayFolderView.deleteFolder"), false) {
					@Override
					protected void confirmButtonClick(ConfirmDialog dialog) {
						presenter.deleteFolderButtonClicked(null);
					}

					@Override
					protected String getConfirmDialogMessage() {
						return $("ConfirmDialog.confirmDeleteWithRecord", recordVO.getTitle());
					}
				};
			} else {
				deleteFolderButton = new DeleteWithJustificationButton($("DisplayFolderView.deleteFolder"), false) {
					@Override
					protected void deletionConfirmed(String reason) {
						presenter.deleteFolderButtonClicked(reason);
					}

					@Override
					public Component getRecordCaption() {
						return new ReferenceDisplay(recordVO);
					}
				};
			}


			duplicateFolderButton = new WindowButton($("DisplayFolderView.duplicateFolder"),
					$("DisplayFolderView.duplicateFolderOnlyOrHierarchy")) {
				@Override
				protected Component buildWindowContent() {
					BaseButton folder = new BaseButton($("DisplayFolderView.folderOnly")) {
						@Override
						protected void buttonClick(ClickEvent event) {
							presenter.duplicateFolderButtonClicked();
						}
					};

					BaseButton structure = new BaseButton($("DisplayFolderView.hierarchy")) {
						@Override
						protected void buttonClick(ClickEvent event) {
							presenter.duplicateStructureButtonClicked();
						}
					};

					BaseButton cancel = new BaseButton($("cancel")) {
						@Override
						protected void buttonClick(ClickEvent event) {
							getWindow().close();
						}
					};
					cancel.addStyleName(ValoTheme.BUTTON_LINK);

					HorizontalLayout layout = new HorizontalLayout(folder, structure, cancel);
					layout.setComponentAlignment(folder, Alignment.TOP_LEFT);
					layout.setComponentAlignment(structure, Alignment.TOP_LEFT);
					layout.setComponentAlignment(cancel, Alignment.TOP_RIGHT);
					layout.setExpandRatio(cancel, 1);

					layout.setWidth("95%");
					layout.setSpacing(true);

					VerticalLayout wrapper = new VerticalLayout(layout);
					wrapper.setSizeFull();

					return wrapper;
				}
			};

			linkToFolderButton = new LinkButton($("DisplayFolderView.linkToFolder")) {
				@Override
				protected void buttonClick(ClickEvent event) {
					presenter.linkToFolderButtonClicked();
				}
			};
			linkToFolderButton.setVisible(false);

			addAuthorizationButton = new LinkButton($("DisplayFolderView.addAuthorization")) {
				@Override
				protected void buttonClick(ClickEvent event) {
					presenter.addAuthorizationButtonClicked();
				}
			};

			shareFolderButton = new LinkButton($("DisplayFolderView.shareFolder")) {
				@Override
				protected void buttonClick(ClickEvent event) {
					presenter.shareFolderButtonClicked();
				}
			};

			addToCartButton = buildAddToCartButton();

			addToOrRemoveFromSelectionButton = new AddToOrRemoveFromSelectionButton(recordVO,
					getSessionContext().getSelectedRecordIds().contains(recordVO.getId()));

			Factory<List<LabelTemplate>> customLabelTemplatesFactory = new Factory<List<LabelTemplate>>() {
				@Override
				public List<LabelTemplate> get() {
					return presenter.getCustomTemplates();
				}
			};
			Factory<List<LabelTemplate>> defaultLabelTemplatesFactory = new Factory<List<LabelTemplate>>() {
				@Override
				public List<LabelTemplate> get() {
					return presenter.getDefaultTemplates();
				}
			};
			try {
				printLabelButton = new LabelButtonV2($("DisplayFolderView.printLabel"),
						$("DisplayFolderView.printLabel"), customLabelTemplatesFactory, defaultLabelTemplatesFactory,
						getConstellioFactories().getAppLayerFactory(), getSessionContext().getCurrentCollection(), getSessionContext().getCurrentUser(), recordVO);
			} catch (Exception e) {
				showErrorMessage(e.getMessage());
			}

			borrowButton = buildBorrowButton();

			returnFolderButton = buildReturnFolderButton();

			reminderReturnFolderButton = new BaseButton($("DisplayFolderView.reminderReturnFolder")) {
				@Override
				protected void buttonClick(ClickEvent event) {
					presenter.reminderReturnFolder();
				}
			};

			alertWhenAvailableButton = new BaseButton($("RMObject.alertWhenAvailable")) {
				@Override
				protected void buttonClick(ClickEvent event) {
					presenter.alertWhenAvailable();
				}
			};

			reportGeneratorButton = new ReportTabButton($("SearchView.metadataReportTitle"), $("SearchView.metadataReportTitle"), presenter.getApplayerFactory(),
					getCollection(), false, false, presenter.buildReportPresenter(), getSessionContext()) {
				@Override
				public void buttonClick(ClickEvent event) {
					setRecordVoList(recordVO);
					super.buttonClick(event);
				}
			};

			startWorkflowButton = new StartWorkflowButton();
			startWorkflowButton.setVisible(presenter.hasPermissionToStartWorkflow());

			//		actionMenuButtons.add(addDocumentButton);
			//		actionMenuButtons.add(addSubFolderButton);

			boolean isAFolderAndDestroyed = (recordVO instanceof FolderVO
											 && ((FolderVO) recordVO).getArchivisticStatus().isDestroyed());
			actionMenuButtons.add(editFolderButton);

			if (!isAFolderAndDestroyed) {
				actionMenuButtons.add(moveInFolderButton);
			}
			actionMenuButtons.add(deleteFolderButton);
			if (!isAFolderAndDestroyed) {
				actionMenuButtons.add(duplicateFolderButton);
				actionMenuButtons.add(linkToFolderButton);
				actionMenuButtons.add(addAuthorizationButton);
				actionMenuButtons.add(shareFolderButton);
				if (presenter.hasCurrentUserPermissionToUseCart()) {
					actionMenuButtons.add(addToCartButton);
				}
				actionMenuButtons.add(addToOrRemoveFromSelectionButton);
			}
			if (!isAFolderAndDestroyed) {
				actionMenuButtons.add(printLabelButton);
				actionMenuButtons.add(borrowButton);
			}
			actionMenuButtons.add(returnFolderButton);
			actionMenuButtons.add(reminderReturnFolderButton);

			if (!isAFolderAndDestroyed) {

				actionMenuButtons.add(alertWhenAvailableButton);
			}

			if (presenter.hasPermissionToStartWorkflow() && !isAFolderAndDestroyed) {
				actionMenuButtons.add(startWorkflowButton);
			}

			actionMenuButtons.add(reportGeneratorButton);
		}

		return actionMenuButtons;
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

	private WindowButton buildAddToCartButton() {
		WindowConfiguration configuration = new WindowConfiguration(true, true, "50%", "750px");
		return new WindowButton($("DisplayFolderView.addToCart"), $("DisplayFolderView.selectCart"), configuration) {
			@Override
			protected Component buildWindowContent() {
				VerticalLayout layout = new VerticalLayout();
				layout.setSizeFull();

				HorizontalLayout newCartLayout = new HorizontalLayout();
				newCartLayout.setSpacing(true);
				newCartLayout.addComponent(new Label($("CartView.newCart")));
				final BaseTextField newCartTitleField;
				newCartLayout.addComponent(newCartTitleField = new BaseTextField());
				newCartTitleField.setRequired(true);
				BaseButton saveButton;
				newCartLayout.addComponent(saveButton = new BaseButton($("save")) {
					@Override
					protected void buttonClick(ClickEvent event) {
						try {
							presenter.createNewCartAndAddToItRequested(newCartTitleField.getValue());
							getWindow().close();
						} catch (Exception e) {
							showErrorMessage(MessageUtils.toMessage(e));
						}
					}
				});
				saveButton.addStyleName(ValoTheme.BUTTON_PRIMARY);

				TabSheet tabSheet = new TabSheet();
				Table ownedCartsTable = buildOwnedFavoritesTable(getWindow());

				final RecordVOLazyContainer sharedCartsContainer = new RecordVOLazyContainer(
						presenter.getSharedCartsDataProvider());
				RecordVOTable sharedCartsTable = new RecordVOTable($("CartView.sharedCarts"), sharedCartsContainer);
				sharedCartsTable.addItemClickListener(new ItemClickListener() {
					@Override
					public void itemClick(ItemClickEvent event) {
						presenter.addToCartRequested(sharedCartsContainer.getRecordVO((int) event.getItemId()));
						getWindow().close();
					}
				});

				sharedCartsTable.setWidth("100%");
				tabSheet.addTab(ownedCartsTable);
				tabSheet.addTab(sharedCartsTable);
				layout.addComponents(newCartLayout, tabSheet);
				layout.setExpandRatio(tabSheet, 1);
				return layout;
			}
		};
	}

	private DefaultFavoritesTable buildOwnedFavoritesTable(final Window window) {
		List<DefaultFavoritesTable.CartItem> cartItems = new ArrayList<>();
		cartItems.add(new DefaultFavoritesTable.CartItem($("CartView.defaultFavorites")));
		for (Cart cart : presenter.getOwnedCarts()) {
			cartItems.add(new DefaultFavoritesTable.CartItem(cart, cart.getTitle()));
		}
		final DefaultFavoritesTable.FavoritesContainer container = new DefaultFavoritesTable.FavoritesContainer(DefaultFavoritesTable.CartItem.class, cartItems);
		DefaultFavoritesTable defaultFavoritesTable = new DefaultFavoritesTable("favoritesTableFolderDisplay", container, presenter.getSchema());
		defaultFavoritesTable.setCaption($("CartView.ownedCarts"));
		defaultFavoritesTable.addItemClickListener(new ItemClickListener() {
			@Override
			public void itemClick(ItemClickEvent event) {
				Cart cart = container.getCart((DefaultFavoritesTable.CartItem) event.getItemId());
				if (cart == null) {
					presenter.addToDefaultFavorite();
				} else {
					presenter.addToCartRequested(cart);
				}
				window.close();
			}
		});
		container.removeContainerProperty(DefaultFavoritesTable.CartItem.DISPLAY_BUTTON);
		defaultFavoritesTable.setWidth("100%");
		return defaultFavoritesTable;
	}

	@Override
	public void setEvents(final RecordVODataProvider dataProvider) {
		this.eventsDataProvider = dataProvider;
	}

	@Override
	public void setFolderContent(List<RecordVODataProvider> dataProviders) {
		this.folderContentDataProviders = dataProviders;
	}

	@Override
	public void selectMetadataTab() {
		tabSheet.setSelectedTab(recordDisplay);
	}

	@Override
	public void selectFolderContentTab() {
		if (!(folderContentComponent instanceof Table)) {
			final RecordVOLazyContainer nestedContainer = new RecordVOLazyContainer(folderContentDataProviders);

			ButtonsContainer<RecordVOLazyContainer> container = new ButtonsContainer<RecordVOLazyContainer>(nestedContainer);
			container.addButton(new ContainerButton() {
				@Override
				protected Button newButtonInstance(Object itemId, ButtonsContainer<?> container) {
					int index = (int) itemId;
					final RecordVO record = nestedContainer.getRecordVO(index);
					Button button = new EditButton() {
						@Override
						protected void buttonClick(ClickEvent event) {
							presenter.editDocumentButtonClicked(record);
						}
					};
					if (presenter.isDocument(record)) {
						button.setEnabled(presenter.canModifyDocument(record));
					} else {
						button.setVisible(false);
					}
					return button;
				}
			});
			container.addButton(new ContainerButton() {
				@Override
				protected Button newButtonInstance(Object itemId, ButtonsContainer<?> container) {
					int index = (int) itemId;
					final RecordVO record = nestedContainer.getRecordVO(index);
					Button button = new IconButton(new ThemeResource("images/icons/actions/download.png"),
							$("DisplayFolderView.download")) {
						@Override
						protected void buttonClick(ClickEvent event) {
							presenter.downloadDocumentButtonClicked(record);
						}
					};
					if (presenter.isDocument(record)) {
						button.setEnabled(record.get(Document.CONTENT) != null);
					} else {
						button.setVisible(false);
					}
					return button;
				}
			});
			container.addButton(new ContainerButton() {
				@Override
				protected Button newButtonInstance(Object itemId, ButtonsContainer<?> container) {
					int index = (int) itemId;
					final RecordVO record = nestedContainer.getRecordVO(index);
					Button button = new DisplayButton() {
						@Override
						protected void buttonClick(ClickEvent event) {
							if (presenter.isDocument(record)) {
								presenter.displayDocumentButtonClicked(record);
							} else {
								presenter.navigateToFolder(record.getId());
							}
						}
					};
					return button;
				}
			});
			final Table table = new RecordVOTable();
			table.addStyleName("folder-content-table");
			table.setSizeFull();
			table.setColumnHeader(ButtonsContainer.DEFAULT_BUTTONS_PROPERTY_ID, "");
			table.addItemClickListener(new ItemClickListener() {
				@Override
				public void itemClick(ItemClickEvent event) {
					if (event.getButton() == MouseButton.LEFT) {
						RecordVOItem item = (RecordVOItem) event.getItem();
						RecordVO recordVO = item.getRecord();
						if (presenter.isDocument(recordVO)) {
							presenter.documentClicked(recordVO);
						} else {
							presenter.navigateToFolder(recordVO.getId());
						}
					}
				}
			});
			table.setContainerDataSource(nestedContainer);

			RecordVOSelectionTableAdapter tableAdapter = new RecordVOSelectionTableAdapter(table) {
				@Override
				public void selectAll() {
					presenter.selectAllClicked();
				}

				@Override
				public void deselectAll() {
					presenter.deselectAllClicked();
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
					RecordVOItem item = (RecordVOItem) table.getItem(itemId);
					RecordVO recordVO = item.getRecord();
					return presenter.isSelected(recordVO);
				}

				@Override
				public void setSelected(Object itemId, boolean selected) {
					RecordVOItem item = (RecordVOItem) table.getItem(itemId);
					RecordVO recordVO = item.getRecord();
					presenter.recordSelectionChanged(recordVO, selected);
					adjustSelectAllButton(selected);
				}
			};
			tabSheet.replaceComponent(folderContentComponent, tableAdapter);
			folderContentComponent = tableAdapter;

		}
		tabSheet.setSelectedTab(folderContentComponent);
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
				@Override
				protected Component buildMetadataComponent(MetadataValueVO metadataValue, RecordVO recordVO) {
					if (Task.STARRED_BY_USERS.equals(metadataValue.getMetadata().getLocalCode())) {
						return new StarredFieldImpl(recordVO.getId(), (List<String>) metadataValue.getValue(),
								getSessionContext().getCurrentUser().getId()) {
							@Override
							public void updateTaskStarred(boolean isStarred, String taskId) {
								presenter.updateTaskStarred(isStarred, taskId, tasksDataProvider);
							}
						};
					} else {
						return super.buildMetadataComponent(metadataValue, recordVO);
					}
				}

				@Override
				protected TableColumnsManager newColumnsManager() {
					return new RecordVOTableColumnsManager() {
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
		deleteFolderButton.setVisible(state.isVisible());
		deleteFolderButton.setEnabled(state.isEnabled());
	}

	@Override
	public void setMoveInFolderState(ComponentState state) {
		moveInFolderButton.setVisible(state.isVisible());
		moveInFolderButton.setEnabled(state.isEnabled());
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

	@Override
	public void setAddSubFolderButtonState(ComponentState state) {
		addSubFolderButton.setVisible(state.isVisible());
		addSubFolderButton.setEnabled(state.isEnabled());
	}

	@Override
	public void setDuplicateFolderButtonState(ComponentState state) {
		duplicateFolderButton.setVisible(state.isVisible());
		duplicateFolderButton.setEnabled(state.isEnabled());
	}

	@Override
	public void setPrintButtonState(ComponentState state) {
		printLabelButton.setVisible(state.isVisible());
		printLabelButton.setEnabled(state.isEnabled());
	}

	@Override
	public void setShareFolderButtonState(ComponentState state) {
		shareFolderButton.setVisible(state.isVisible());
		shareFolderButton.setEnabled(state.isEnabled());
	}

	@Override
	public void setAuthorizationButtonState(ComponentState state) {
		addAuthorizationButton.setVisible(state.isVisible());
		addAuthorizationButton.setEnabled(state.isEnabled());
	}

	@Override
	public void setBorrowButtonState(ComponentState state) {
		borrowButton.setVisible(state.isVisible());
		borrowButton.setEnabled(state.isEnabled());
	}

	@Override
	public void setReturnFolderButtonState(ComponentState state) {
		returnFolderButton.setVisible(state.isVisible());
		returnFolderButton.setEnabled(state.isEnabled());

	}

	@Override
	public void setReminderReturnFolderButtonState(ComponentState state) {
		reminderReturnFolderButton.setVisible(state.isVisible());
		reminderReturnFolderButton.setEnabled(state.isEnabled());

	}

	@Override
	public void setAlertWhenAvailableButtonState(ComponentState state) {
		alertWhenAvailableButton.setVisible(state.isVisible());
		alertWhenAvailableButton.setEnabled(state.isEnabled());
	}

	@Override
	public void setStartWorkflowButtonState(ComponentState state) {
		startWorkflowButton.setVisible(state.isVisible());
		startWorkflowButton.setEnabled(state.isEnabled());
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

	private Button buildBorrowButton() {
		return new WindowButton($("DisplayFolderView.borrow"),
				$("DisplayFolderView.borrow"), new WindowConfiguration(true, true, "50%", "500px")) {
			@Override
			protected Component buildWindowContent() {
				final JodaDateField borrowDatefield = new JodaDateField();
				borrowDatefield.setCaption($("DisplayFolderView.borrowDate"));
				borrowDatefield.setRequired(true);
				borrowDatefield.setId("borrowDate");
				borrowDatefield.addStyleName("borrowDate");
				borrowDatefield.setValue(TimeProvider.getLocalDate().toDate());

				final Field<?> lookupUser = new LookupRecordField(User.SCHEMA_TYPE);
				lookupUser.setCaption($("DisplayFolderView.borrower"));
				lookupUser.setId("borrower");
				lookupUser.addStyleName(USER_LOOKUP);
				lookupUser.setRequired(true);

				final ComboBox borrowingTypeField = new BaseComboBox();
				borrowingTypeField.setCaption($("DisplayFolderView.borrowingType"));
				for (BorrowingType borrowingType : BorrowingType.values()) {
					borrowingTypeField.addItem(borrowingType);
					borrowingTypeField
							.setItemCaption(borrowingType, $("DisplayFolderView.borrowingType." + borrowingType.getCode()));
				}
				borrowingTypeField.setRequired(true);
				borrowingTypeField.setNullSelectionAllowed(false);

				final JodaDateField previewReturnDatefield = new JodaDateField();
				previewReturnDatefield.setCaption($("DisplayFolderView.previewReturnDate"));
				previewReturnDatefield.setRequired(true);
				previewReturnDatefield.setId("previewReturnDate");
				previewReturnDatefield.addStyleName("previewReturnDate");

				final JodaDateField returnDatefield = new JodaDateField();
				returnDatefield.setCaption($("DisplayFolderView.returnDate"));
				returnDatefield.setRequired(false);
				returnDatefield.setId("returnDate");
				returnDatefield.addStyleName("returnDate");

				borrowDatefield.addValueChangeListener(new ValueChangeListener() {
					@Override
					public void valueChange(ValueChangeEvent event) {
						previewReturnDatefield.setValue(
								presenter.getPreviewReturnDate(borrowDatefield.getValue(), borrowingTypeField.getValue()));
					}
				});
				borrowingTypeField.addValueChangeListener(new ValueChangeListener() {
					@Override
					public void valueChange(ValueChangeEvent event) {
						previewReturnDatefield.setValue(
								presenter.getPreviewReturnDate(borrowDatefield.getValue(), borrowingTypeField.getValue()));
					}
				});

				BaseButton borrowButton = new BaseButton($("DisplayFolderView.borrow")) {
					@Override
					protected void buttonClick(ClickEvent event) {
						String userId = null;
						BorrowingType borrowingType = null;
						if (lookupUser.getValue() != null) {
							userId = (String) lookupUser.getValue();
						}
						if (borrowingTypeField.getValue() != null) {
							borrowingType = BorrowingType.valueOf(borrowingTypeField.getValue().toString());
						}
						LocalDate borrowLocalDate = null;
						LocalDate previewReturnLocalDate = null;
						LocalDate returnLocalDate = null;
						if (borrowDatefield.getValue() != null) {
							borrowLocalDate = LocalDate.fromDateFields(borrowDatefield.getValue());
						}
						if (previewReturnDatefield.getValue() != null) {
							previewReturnLocalDate = LocalDate.fromDateFields(previewReturnDatefield.getValue());
						}
						if (returnDatefield.getValue() != null) {
							returnLocalDate = LocalDate.fromDateFields(returnDatefield.getValue());
						}
						if (presenter.borrowFolder(borrowLocalDate, previewReturnLocalDate, userId,
								borrowingType, returnLocalDate)) {
							getWindow().close();
						}
					}
				};
				borrowButton.addStyleName(ValoTheme.BUTTON_PRIMARY);

				BaseButton cancelButton = new BaseButton($("cancel")) {
					@Override
					protected void buttonClick(ClickEvent event) {
						getWindow().close();
					}
				};
				cancelButton.addStyleName(ValoTheme.BUTTON_PRIMARY);

				HorizontalLayout horizontalLayout = new HorizontalLayout();
				horizontalLayout.setSpacing(true);
				horizontalLayout.addComponents(borrowButton, cancelButton);

				VerticalLayout verticalLayout = new VerticalLayout();
				verticalLayout
						.addComponents(borrowDatefield, borrowingTypeField, lookupUser, previewReturnDatefield, returnDatefield,
								horizontalLayout);
				verticalLayout.setSpacing(true);
				verticalLayout.addStyleName("no-scroll");

				return verticalLayout;
			}
		};
	}

	private Button buildReturnFolderButton() {
		return new WindowButton($("DisplayFolderView.returnFolder"),
				$("DisplayFolderView.returnFolder")) {
			@Override
			protected Component buildWindowContent() {

				final JodaDateField returnDatefield = new JodaDateField();
				returnDatefield.setCaption($("DisplayFolderView.returnDate"));
				returnDatefield.setRequired(false);
				returnDatefield.setId("returnDate");
				returnDatefield.addStyleName("returnDate");
				returnDatefield.setValue(TimeProvider.getLocalDate().toDate());

				BaseButton returnFolderButton = new BaseButton($("DisplayFolderView.returnFolder")) {
					@Override
					protected void buttonClick(ClickEvent event) {
						LocalDate returnLocalDate = null;
						if (returnDatefield.getValue() != null) {
							returnLocalDate = LocalDate.fromDateFields(returnDatefield.getValue());
						}
						if (presenter.returnFolder(returnLocalDate)) {
							getWindow().close();
						}
					}
				};
				returnFolderButton.addStyleName(ValoTheme.BUTTON_PRIMARY);

				BaseButton cancelButton = new BaseButton($("cancel")) {
					@Override
					protected void buttonClick(ClickEvent event) {
						getWindow().close();
					}
				};
				cancelButton.addStyleName(ValoTheme.BUTTON_PRIMARY);

				HorizontalLayout horizontalLayout = new HorizontalLayout();
				horizontalLayout.setSpacing(true);
				horizontalLayout.addComponents(returnFolderButton, cancelButton);

				VerticalLayout verticalLayout = new VerticalLayout();
				verticalLayout
						.addComponents(returnDatefield, horizontalLayout);
				verticalLayout.setSpacing(true);

				return verticalLayout;
			}
		};
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

	private class StartWorkflowButton extends WindowButton {
		public StartWorkflowButton() {
			super($("TasksManagementView.startWorkflowBeta"), $("TasksManagementView.startWorkflow"), modalDialog("75%", "75%"));
		}

		@Override
		protected Component buildWindowContent() {
			RecordVOTable table = new RecordVOTable(presenter.getWorkflows());
			table.setWidth("98%");
			table.addItemClickListener(new ItemClickListener() {
				@Override
				public void itemClick(ItemClickEvent event) {
					RecordVOItem item = (RecordVOItem) event.getItem();
					presenter.workflowStartRequested(item.getRecord());
					getWindow().close();
				}
			});
			return table;
		}

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
}
