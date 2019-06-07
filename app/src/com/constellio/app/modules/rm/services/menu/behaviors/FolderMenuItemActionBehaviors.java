package com.constellio.app.modules.rm.services.menu.behaviors;

import com.constellio.app.api.extensions.params.NavigateToFromAPageParams;
import com.constellio.app.api.extensions.taxonomies.FolderDeletionEvent;
import com.constellio.app.modules.rm.ConstellioRMModule;
import com.constellio.app.modules.rm.RMConfigs;
import com.constellio.app.modules.rm.RMEmailTemplateConstants;
import com.constellio.app.modules.rm.constants.RMPermissionsTo;
import com.constellio.app.modules.rm.extensions.api.RMModuleExtensions;
import com.constellio.app.modules.rm.model.labelTemplate.LabelTemplate;
import com.constellio.app.modules.rm.navigation.RMNavigationConfiguration;
import com.constellio.app.modules.rm.navigation.RMViews;
import com.constellio.app.modules.rm.services.RMSchemasRecordsServices;
import com.constellio.app.modules.rm.services.actions.FolderRecordActionsServices;
import com.constellio.app.modules.rm.services.borrowingServices.BorrowingServices;
import com.constellio.app.modules.rm.services.borrowingServices.BorrowingType;
import com.constellio.app.modules.rm.services.decommissioning.DecommissioningService;
import com.constellio.app.modules.rm.ui.components.folder.fields.LookupFolderField;
import com.constellio.app.modules.rm.ui.entities.FolderVO;
import com.constellio.app.modules.rm.ui.pages.cart.DefaultFavoritesTable;
import com.constellio.app.modules.rm.ui.pages.cart.DefaultFavoritesTable.CartItem;
import com.constellio.app.modules.rm.util.DecommissionNavUtil;
import com.constellio.app.modules.rm.util.RMNavigationUtils;
import com.constellio.app.modules.rm.wrappers.Cart;
import com.constellio.app.modules.rm.wrappers.Folder;
import com.constellio.app.services.factories.AppLayerFactory;
import com.constellio.app.ui.entities.MetadataSchemaVO;
import com.constellio.app.ui.entities.RecordVO;
import com.constellio.app.ui.entities.RecordVO.VIEW_MODE;
import com.constellio.app.ui.framework.builders.MetadataSchemaToVOBuilder;
import com.constellio.app.ui.framework.builders.RecordToVOBuilder;
import com.constellio.app.ui.framework.buttons.AddButton;
import com.constellio.app.ui.framework.buttons.BaseButton;
import com.constellio.app.ui.framework.buttons.DeleteButton;
import com.constellio.app.ui.framework.buttons.DeleteWithJustificationButton;
import com.constellio.app.ui.framework.buttons.DisplayButton;
import com.constellio.app.ui.framework.buttons.EditButton;
import com.constellio.app.ui.framework.buttons.LinkButton;
import com.constellio.app.ui.framework.buttons.WindowButton;
import com.constellio.app.ui.framework.buttons.WindowButton.WindowConfiguration;
import com.constellio.app.ui.framework.buttons.report.LabelButtonV2;
import com.constellio.app.ui.framework.components.RMSelectionPanelReportPresenter;
import com.constellio.app.ui.framework.components.ReportTabButton;
import com.constellio.app.ui.framework.components.display.ReferenceDisplay;
import com.constellio.app.ui.framework.components.fields.BaseComboBox;
import com.constellio.app.ui.framework.components.fields.BaseTextField;
import com.constellio.app.ui.framework.components.fields.date.JodaDateField;
import com.constellio.app.ui.framework.components.fields.lookup.LookupRecordField;
import com.constellio.app.ui.framework.components.table.RecordVOTable;
import com.constellio.app.ui.framework.containers.RecordVOLazyContainer;
import com.constellio.app.ui.framework.data.RecordVODataProvider;
import com.constellio.app.ui.i18n.i18n;
import com.constellio.app.ui.pages.base.BaseView;
import com.constellio.app.ui.pages.base.SchemaPresenterUtils;
import com.constellio.app.ui.util.MessageUtils;
import com.constellio.data.utils.Factory;
import com.constellio.data.utils.TimeProvider;
import com.constellio.model.entities.records.Record;
import com.constellio.model.entities.records.RecordUpdateOptions;
import com.constellio.model.entities.records.Transaction;
import com.constellio.model.entities.records.wrappers.EmailToSend;
import com.constellio.model.entities.records.wrappers.User;
import com.constellio.model.entities.schemas.MetadataSchema;
import com.constellio.model.entities.schemas.MetadataSchemaTypes;
import com.constellio.model.entities.schemas.Schemas;
import com.constellio.model.entities.structures.EmailAddress;
import com.constellio.model.frameworks.validation.ValidationErrors;
import com.constellio.model.services.factories.ModelLayerFactory;
import com.constellio.model.services.migrations.ConstellioEIMConfigs;
import com.constellio.model.services.records.RecordDeleteServicesRuntimeException;
import com.constellio.model.services.records.RecordServices;
import com.constellio.model.services.records.RecordServicesException;
import com.constellio.model.services.records.RecordServicesRuntimeException.RecordServicesRuntimeException_CannotLogicallyDeleteRecord;
import com.constellio.model.services.search.SearchServices;
import com.constellio.model.services.search.query.logical.LogicalSearchQuery;
import com.constellio.model.services.security.roles.Roles;
import com.vaadin.data.Property.ValueChangeEvent;
import com.vaadin.data.Property.ValueChangeListener;
import com.vaadin.event.ItemClickEvent;
import com.vaadin.event.ItemClickEvent.ItemClickListener;
import com.vaadin.ui.Alignment;
import com.vaadin.ui.Button;
import com.vaadin.ui.ComboBox;
import com.vaadin.ui.Component;
import com.vaadin.ui.Field;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Label;
import com.vaadin.ui.TabSheet;
import com.vaadin.ui.Table;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.Window;
import com.vaadin.ui.themes.ValoTheme;
import lombok.extern.slf4j.Slf4j;
import org.joda.time.LocalDate;
import org.joda.time.LocalDateTime;
import org.vaadin.dialogs.ConfirmDialog;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

import static com.constellio.app.ui.framework.components.ErrorDisplayUtil.showErrorMessage;
import static com.constellio.app.ui.i18n.i18n.$;
import static com.constellio.model.services.search.query.logical.LogicalSearchQueryOperators.from;
import static java.util.Arrays.asList;
import static org.apache.commons.lang.StringUtils.isNotBlank;

@Slf4j
public class FolderMenuItemActionBehaviors {

	private String collection;
	private AppLayerFactory appLayerFactory;
	private ModelLayerFactory modelLayerFactory;
	private RecordServices recordServices;
	private RMSchemasRecordsServices rm;
	private DecommissioningService decommissioningService;
	private RMModuleExtensions rmModuleExtensions;
	private FolderRecordActionsServices folderRecordActionsServices;
	private SearchServices searchServices;
	private MetadataSchemaToVOBuilder schemaVOBuilder;
	private BorrowingServices borrowingServices;
	private RMConfigs rmConfigs;
	private ConstellioEIMConfigs eimConfigs;
	private MetadataSchemaTypes schemaTypes;

	public static final String USER_LOOKUP = "user-lookup";

	public FolderMenuItemActionBehaviors(String collection, AppLayerFactory appLayerFactory) {
		this.collection = collection;
		this.appLayerFactory = appLayerFactory;
		modelLayerFactory = appLayerFactory.getModelLayerFactory();
		recordServices = modelLayerFactory.newRecordServices();
		rm = new RMSchemasRecordsServices(collection, appLayerFactory);
		decommissioningService = new DecommissioningService(collection, appLayerFactory);
		rmModuleExtensions = appLayerFactory.getExtensions().forCollection(collection).forModule(ConstellioRMModule.ID);
		folderRecordActionsServices = new FolderRecordActionsServices(collection, appLayerFactory);
		searchServices = modelLayerFactory.newSearchServices();
		schemaVOBuilder = new MetadataSchemaToVOBuilder();
		borrowingServices = new BorrowingServices(collection, modelLayerFactory);
		rmConfigs = new RMConfigs(modelLayerFactory.getSystemConfigurationsManager());
		eimConfigs = new ConstellioEIMConfigs(modelLayerFactory.getSystemConfigurationsManager());
		schemaTypes = modelLayerFactory.getMetadataSchemasManager().getSchemaTypes(collection);
	}

	public void addToDocument(MenuItemActionBehaviorParams params) {
		Button addDocumentButton = new AddButton($("DisplayFolderView.addDocument")) {
			@Override
			protected void buttonClick(ClickEvent event) {
				params.getView().navigate().to(RMViews.class).addDocument(params.getRecordVO().getId());
			}
		};
		addDocumentButton.click();
	}

	public void move(MenuItemActionBehaviorParams params) {
		Button moveInFolderButton = new WindowButton($("DisplayFolderView.parentFolder"),
				$("DisplayFolderView.parentFolder"), WindowButton.WindowConfiguration.modalDialog("50%", "20%")) {
			@Override
			protected Component buildWindowContent() {
				VerticalLayout verticalLayout = new VerticalLayout();
				verticalLayout.setSpacing(true);
				final LookupFolderField field = new LookupFolderField();
				verticalLayout.addComponent(field);
				BaseButton saveButton = new BaseButton($("save")) {
					@Override
					protected void buttonClick(ClickEvent event) {
						String parentId = (String) field.getValue();
						try {
							RMSchemasRecordsServices rmSchemas = new RMSchemasRecordsServices(collection, appLayerFactory);

							String currentFolderId = params.getRecordVO().getId();
							if (isNotBlank(parentId)) {
								try {
									recordServices.update(rmSchemas.getFolder(currentFolderId).setParentFolder(parentId));
									params.getView().navigate().to(RMViews.class).displayFolder(currentFolderId);
								} catch (RecordServicesException.ValidationException e) {
									params.getView().showErrorMessage($(e.getErrors()));
								}
							}
						} catch (Throwable e) {
							log.warn("error when trying to modify folder parent to " + parentId, e);
							showErrorMessage("DisplayFolderView.parentFolderException");
						}
						getWindow().close();
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
		moveInFolderButton.click();
	}

	public void addSubFolder(MenuItemActionBehaviorParams params) {
		Button addSubFolderButton = new AddButton($("DisplayFolderView.addSubFolder")) {
			@Override
			protected void buttonClick(ClickEvent event) {
				params.getView().navigate().to(RMViews.class).addFolder(params.getRecordVO().getId());
			}
		};
		addSubFolderButton.click();
	}

	public void display(MenuItemActionBehaviorParams params) {
		Button displayFolderButton = new DisplayButton($("DisplayFolderView.displayFolder")) {
			@Override
			protected void buttonClick(ClickEvent event) {
				RMNavigationUtils.navigateToDisplayFolder(params.getRecordVO().getId(), params.getFormParams(),
						appLayerFactory, collection);
			}
		};
		displayFolderButton.click();
	}

	public void edit(MenuItemActionBehaviorParams params) {
		Button editFolderButton = new EditButton($("DisplayFolderView.editFolder")) {
			@Override
			protected void buttonClick(ClickEvent event) {
				RMNavigationUtils.navigateToEditFolder(params.getRecordVO().getId(), params.getFormParams(),
						appLayerFactory, collection);
			}
		};
		editFolderButton.click();
	}

	public void delete(MenuItemActionBehaviorParams params) {
		boolean needAReasonToDeleteFolder = new RMConfigs(modelLayerFactory.getSystemConfigurationsManager())
				.isNeedingAReasonBeforeDeletingFolders();

		Button deleteFolderButton = new Button();
		if (!needAReasonToDeleteFolder) {
			deleteFolderButton = new DeleteButton($("DisplayFolderView.deleteFolder"), false) {
				@Override
				protected void confirmButtonClick(ConfirmDialog dialog) {
					deleteFolder(null, params);
				}

				@Override
				protected String getConfirmDialogMessage() {
					return $("ConfirmDialog.confirmDeleteWithRecord", params.getRecordVO().getTitle());
				}
			};
		} else {
			deleteFolderButton = new DeleteWithJustificationButton($("DisplayFolderView.deleteFolder"), false) {
				@Override
				protected void deletionConfirmed(String reason) {
					deleteFolder(reason, params);
				}

				@Override
				public Component getRecordCaption() {
					return new ReferenceDisplay(params.getRecordVO());
				}
			};
		}
		deleteFolderButton.click();
	}

	public void copy(MenuItemActionBehaviorParams params) {
		Button duplicateFolderButton = new WindowButton($("DisplayFolderView.duplicateFolder"),
				$("DisplayFolderView.duplicateFolderOnlyOrHierarchy")) {
			@Override
			protected Component buildWindowContent() {
				BaseButton folder = new BaseButton($("DisplayFolderView.folderOnly")) {
					@Override
					protected void buttonClick(ClickEvent event) {
						Folder folder = rm.getFolder(params.getRecordVO().getId());
						if (folderRecordActionsServices.isDuplicateActionPossible(folder.getWrappedRecord(), params.getUser())) {
							navigateToDuplicateFolder(folder, false, params);
						}
						if (!params.isNestedView()) {
							params.getView().closeAllWindows();
						}
					}
				};

				BaseButton structure = new BaseButton($("DisplayFolderView.hierarchy")) {
					@Override
					protected void buttonClick(ClickEvent event) {
						Folder folder = rm.getFolder(params.getRecordVO().getId());
						if (folderRecordActionsServices.isDuplicateActionPossible(folder.getWrappedRecord(), params.getUser())) {
							try {
								decommissioningService.validateDuplicateStructure(folder, params.getUser(), false);
								navigateToDuplicateFolder(folder, true, params);
							} catch (RecordServicesException.ValidationException e) {
								params.getView().showErrorMessage($(e.getErrors()));
							} catch (Exception e) {
								params.getView().showErrorMessage(e.getMessage());
							}
						}
						if (!params.isNestedView()) {
							params.getView().closeAllWindows();
						}
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
		duplicateFolderButton.click();
	}

	public void addAuthorization(MenuItemActionBehaviorParams params) {
		Button addAuthorizationButton = new LinkButton($("DisplayFolderView.addAuthorization")) {
			@Override
			protected void buttonClick(ClickEvent event) {
				params.getView().navigate().to().listObjectAccessAndRoleAuthorizations(params.getRecordVO().getId());
			}
		};
		addAuthorizationButton.click();
	}

	public void share(MenuItemActionBehaviorParams params) {
		Button shareFolderButton = new LinkButton($("DisplayFolderView.shareFolder")) {
			@Override
			protected void buttonClick(ClickEvent event) {
				Folder folder = rm.getFolder(params.getRecordVO().getId());
				if (!folderRecordActionsServices.isShareActionPossible(folder.getWrappedRecord(), params.getUser())) {
					return;
				}
				params.getView().navigate().to().shareContent(params.getRecordVO().getId());
			}
		};
		shareFolderButton.click();
	}

	public void addToCart(MenuItemActionBehaviorParams params) {
		CartWindowButton cartWindowButton = new CartWindowButton(params);
		cartWindowButton.addToCart();
	}

	public void borrow(MenuItemActionBehaviorParams params) {
		Button borrowButton = new WindowButton($("DisplayFolderView.borrow"),
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
								getPreviewReturnDate(borrowDatefield.getValue(), borrowingTypeField.getValue()));
					}
				});
				borrowingTypeField.addValueChangeListener(new ValueChangeListener() {
					@Override
					public void valueChange(ValueChangeEvent event) {
						previewReturnDatefield.setValue(
								getPreviewReturnDate(borrowDatefield.getValue(), borrowingTypeField.getValue()));
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
						if (borrowFolder(borrowLocalDate, previewReturnLocalDate, userId,
								borrowingType, returnLocalDate, params)) {
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
		borrowButton.click();
	}

	public void returnFolder(MenuItemActionBehaviorParams params) {
		Button returnButton = new WindowButton($("DisplayFolderView.returnFolder"),
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
						if (returnFolder(returnLocalDate, params)) {
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
		returnButton.click();
	}

	public void sendReturnRemainder(MenuItemActionBehaviorParams params) {
		Button reminderReturnFolderButton = new BaseButton($("DisplayFolderView.reminderReturnFolder")) {
			@Override
			protected void buttonClick(ClickEvent event) {
				try {
					FolderVO folderVO = ((FolderVO) params.getRecordVO());

					EmailToSend emailToSend = newEmailToSend();
					String constellioUrl = eimConfigs.getConstellioUrl();
					User borrower = null;
					if (folderVO.getBorrowUserEnteredId() != null) {
						borrower = rm.getUser(folderVO.getBorrowUserEnteredId());
					} else {
						borrower = rm.getUser(folderVO.getBorrowUserId());
					}

					EmailAddress borrowerAddress = new EmailAddress(borrower.getTitle(), borrower.getEmail());
					emailToSend.setTo(Arrays.asList(borrowerAddress));
					emailToSend.setSendOn(TimeProvider.getLocalDateTime());
					emailToSend.setSubject($("DisplayFolderView.returnFolderReminder") + folderVO.getTitle());
					emailToSend.setTemplate(RMEmailTemplateConstants.REMIND_BORROW_TEMPLATE_ID);
					List<String> parameters = new ArrayList<>();
					String previewReturnDate = folderVO.getPreviewReturnDate().toString();
					parameters.add("previewReturnDate" + EmailToSend.PARAMETER_SEPARATOR + previewReturnDate);
					parameters.add("borrower" + EmailToSend.PARAMETER_SEPARATOR + borrower.getUsername());
					String borrowedFolderTitle = folderVO.getTitle();
					parameters.add("borrowedFolderTitle" + EmailToSend.PARAMETER_SEPARATOR + borrowedFolderTitle);
					boolean isAddingRecordIdInEmails = eimConfigs.isAddingRecordIdInEmails();
					if (isAddingRecordIdInEmails) {
						parameters.add("title" + EmailToSend.PARAMETER_SEPARATOR + $("DisplayFolderView.returnFolderReminder") + " \""
									   + folderVO.getTitle() + "\" (" + folderVO.getId() + ")");
					} else {
						parameters.add("title" + EmailToSend.PARAMETER_SEPARATOR + $("DisplayFolderView.returnFolderReminder") + " \""
									   + folderVO.getTitle() + "\"");
					}

					parameters.add("constellioURL" + EmailToSend.PARAMETER_SEPARATOR + constellioUrl);
					parameters.add("recordURL" + EmailToSend.PARAMETER_SEPARATOR + constellioUrl + "#!"
								   + RMNavigationConfiguration.DISPLAY_FOLDER + "/" + folderVO.getId());
					emailToSend.setParameters(parameters);

					recordServices.add(emailToSend);
					params.getView().showMessage($("DisplayFolderView.reminderEmailSent"));
				} catch (RecordServicesException e) {
					log.error("DisplayFolderView.cannotSendEmail", e);
					params.getView().showMessage($("DisplayFolderView.cannotSendEmail"));
				}
			}
		};
		reminderReturnFolderButton.click();
	}

	public void sendAvailableAlert(MenuItemActionBehaviorParams params) {
		Button alertWhenAvailableButton = new BaseButton($("RMObject.alertWhenAvailable")) {
			@Override
			protected void buttonClick(ClickEvent event) {
				try {
					Folder folder = rm.getFolder(params.getRecordVO().getId());
					List<String> usersToAlert = folder.getAlertUsersWhenAvailable();
					String currentUserId = params.getRecordVO().getId();
					if (!currentUserId.equals(folder.getBorrowUser()) && !currentUserId.equals(folder.getBorrowUserEntered())) {
						List<String> newUsersToAlert = new ArrayList<>();
						newUsersToAlert.addAll(usersToAlert);
						if (!newUsersToAlert.contains(currentUserId)) {
							newUsersToAlert.add(currentUserId);
							folder.setAlertUsersWhenAvailable(newUsersToAlert);
							recordServices.update(folder.getWrappedRecord());
						}
					}
					params.getView().showMessage($("RMObject.createAlert"));
				} catch (Exception e) {
					log.error("RMObject.cannotCreateAlert", e);
					params.getView().showErrorMessage($("RMObject.cannotCreateAlert"));
				}
			}
		};
		alertWhenAvailableButton.click();
	}

	public void printLabel(MenuItemActionBehaviorParams params) {
		Factory<List<LabelTemplate>> customLabelTemplatesFactory = new Factory<List<LabelTemplate>>() {
			@Override
			public List<LabelTemplate> get() {
				return appLayerFactory.getLabelTemplateManager().listExtensionTemplates(Folder.SCHEMA_TYPE);
			}
		};
		Factory<List<LabelTemplate>> defaultLabelTemplatesFactory = new Factory<List<LabelTemplate>>() {
			@Override
			public List<LabelTemplate> get() {
				return appLayerFactory.getLabelTemplateManager().listTemplates(Folder.SCHEMA_TYPE);
			}
		};
		try {
			Button printLabelButton = new LabelButtonV2($("DisplayFolderView.printLabel"),
					$("DisplayFolderView.printLabel"), customLabelTemplatesFactory, defaultLabelTemplatesFactory,
					appLayerFactory, collection, params.getView().getSessionContext().getCurrentUser(), params.getRecordVO());
			printLabelButton.click();
		} catch (Exception e) {
			showErrorMessage(e.getMessage());
		}
	}

	public void generateReport(MenuItemActionBehaviorParams params) {
		// FIXME refactor so that we don't we need to instanciate a presenter
		RMSelectionPanelReportPresenter reportPresenter =
				new RMSelectionPanelReportPresenter(appLayerFactory, collection, params.getUser()) {
					@Override
					public String getSelectedSchemaType() {
						return Folder.SCHEMA_TYPE;
					}

					@Override
					public List<String> getSelectedRecordIds() {
						return asList(params.getRecordVO().getId());
					}
				};

		Button reportGeneratorButton = new ReportTabButton($("SearchView.metadataReportTitle"), $("SearchView.metadataReportTitle"), appLayerFactory,
				collection, false, false, reportPresenter, params.getView().getSessionContext()) {
			@Override
			public void buttonClick(ClickEvent event) {
				setRecordVoList(params.getRecordVO());
				super.buttonClick(event);
			}
		};
		reportGeneratorButton.click();
	}

	public void addToSelection(MenuItemActionBehaviorParams params) {
		params.getView().getSessionContext().addSelectedRecordId(params.getRecordVO().getId(),
				params.getRecordVO().getSchema().getTypeCode());
	}

	public void removeToSelection(MenuItemActionBehaviorParams params) {
		params.getView().getSessionContext().removeSelectedRecordId(params.getRecordVO().getId(),
				params.getRecordVO().getSchema().getTypeCode());
	}

	private void addToCartButton(MenuItemActionBehaviorParams params) {
		WindowConfiguration configuration = new WindowConfiguration(true, true, "50%", "750px");
		Button addToCartButton = new WindowButton($("DisplayFolderView.addToCart"), $("DisplayFolderView.selectCart"), configuration) {
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
							createNewCartAndAddToItRequested(newCartTitleField.getValue(), params);
							getWindow().close();
						} catch (Exception e) {
							showErrorMessage(MessageUtils.toMessage(e));
						}
					}
				});
				saveButton.addStyleName(ValoTheme.BUTTON_PRIMARY);

				TabSheet tabSheet = new TabSheet();
				Table ownedCartsTable = buildOwnedFavoritesTable(getWindow(), params);

				final RecordVOLazyContainer sharedCartsContainer = new RecordVOLazyContainer(getSharedCartsDataProvider(params));
				RecordVOTable sharedCartsTable = new RecordVOTable($("CartView.sharedCarts"), sharedCartsContainer);
				sharedCartsTable.addItemClickListener(new ItemClickListener() {
					@Override
					public void itemClick(ItemClickEvent event) {
						RecordVO currentRecordVO = sharedCartsContainer.getRecordVO((int) event.getItemId());
						Cart cart = rm.getCart(currentRecordVO.getId());
						if (rm.numberOfFoldersInFavoritesReachesLimit(cart.getId(), 1)) {
							params.getView().showMessage($("DisplayFolderViewImpl.cartCannotContainMoreThanAThousandFolders"));
						} else {
							Folder folder = rm.wrapFolder(params.getRecordVO().getRecord());
							folder.addFavorite(cart.getId());
							try {
								recordServices.update(folder.getWrappedRecord(), RecordUpdateOptions.validationExceptionSafeOptions());
								params.getView().showMessage($("DisplayFolderView.addedToCart"));
							} catch (RecordServicesException e) {
								e.printStackTrace();
								throw new RuntimeException(e);
							}
						}
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
		addToCartButton.click();
	}

	private void addToMyCartButton(MenuItemActionBehaviorParams params) {
		Button button = new BaseButton($("DisplayFolderView.addToCart")) {
			@Override
			protected void buttonClick(ClickEvent event) {
				if (rm.numberOfFoldersInFavoritesReachesLimit(params.getUser().getId(), 1)) {
					params.getView().showMessage($("DisplayFolderViewImpl.cartCannotContainMoreThanAThousandFolders"));
				} else {
					Folder folder = rm.wrapFolder(params.getRecordVO().getRecord());
					folder.addFavorite(params.getUser().getId());
					try {
						recordServices.update(folder.getWrappedRecord(), RecordUpdateOptions.validationExceptionSafeOptions());
						params.getView().showMessage($("DisplayFolderViewImpl.folderAddedToDefaultFavorites"));
					} catch (RecordServicesException e) {
						e.printStackTrace();
						throw new RuntimeException(e);
					}
				}
			}
		};
		button.click();
	}

	private void createNewCartAndAddToItRequested(String title, MenuItemActionBehaviorParams params) {
		Cart cart = rm.newCart();
		Folder folder = rm.wrapFolder(params.getRecordVO().getRecord());
		cart.setTitle(title);
		cart.setOwner(params.getUser());
		try {
			folder.addFavorite(cart.getId());
			recordServices.execute(new Transaction(cart.getWrappedRecord()).setUser(params.getUser()));
			recordServices.update(folder.getWrappedRecord(), RecordUpdateOptions.validationExceptionSafeOptions());
			params.getView().showMessage($("DisplayFolderView.addedToCart"));
		} catch (RecordServicesException e) {
			e.printStackTrace();
			throw new RuntimeException(e);
		}
	}

	private Table buildOwnedFavoritesTable(Window window, MenuItemActionBehaviorParams params) {
		List<CartItem> cartItems = new ArrayList<>();
		if (params.getUser().has(RMPermissionsTo.USE_MY_CART).globally()) {
			cartItems.add(new DefaultFavoritesTable.CartItem($("CartView.defaultFavorites")));
		}
		List<Cart> ownedCarts = rm.wrapCarts(searchServices.search(new LogicalSearchQuery(from(rm.cartSchema()).where(rm.cart.owner())
				.isEqualTo(params.getUser().getId())).sortAsc(Schemas.TITLE)));
		for (Cart cart : ownedCarts) {
			cartItems.add(new DefaultFavoritesTable.CartItem(cart, cart.getTitle()));
		}
		MetadataSchema cartSchema = rm.cartSchema();
		MetadataSchemaVO cartSchemaVO = new MetadataSchemaToVOBuilder().build(cartSchema, RecordVO.VIEW_MODE.TABLE,
				params.getView().getSessionContext());
		final DefaultFavoritesTable.FavoritesContainer container =
				new DefaultFavoritesTable.FavoritesContainer(DefaultFavoritesTable.CartItem.class, cartItems);
		DefaultFavoritesTable defaultFavoritesTable = new DefaultFavoritesTable("favoritesTableFolderDisplay", container, cartSchemaVO);
		defaultFavoritesTable.setCaption($("CartView.ownedCarts"));
		defaultFavoritesTable.addItemClickListener(new ItemClickListener() {
			@Override
			public void itemClick(ItemClickEvent event) {
				Cart cart = container.getCart((DefaultFavoritesTable.CartItem) event.getItemId());
				if (cart == null) {
					addToDefaultFavorite(params);
				} else {
					addToCartRequested(cart, params);
				}
				window.close();
			}
		});
		container.removeContainerProperty(DefaultFavoritesTable.CartItem.DISPLAY_BUTTON);
		defaultFavoritesTable.setWidth("100%");
		return defaultFavoritesTable;
	}

	private void addToDefaultFavorite(MenuItemActionBehaviorParams params) {
		if (rm.numberOfFoldersInFavoritesReachesLimit(params.getRecordVO().getId(), 1)) {
			params.getView().showMessage($("DisplayFolderViewImpl.cartCannotContainMoreThanAThousandFolders"));
		} else {
			Folder folder = rm.wrapFolder(params.getRecordVO().getRecord());
			folder.addFavorite(params.getRecordVO().getId());
			try {
				recordServices.update(folder.getWrappedRecord(), RecordUpdateOptions.validationExceptionSafeOptions());
				params.getView().showMessage($("DisplayFolderViewImpl.folderAddedToDefaultFavorites"));
			} catch (RecordServicesException e) {
				e.printStackTrace();
				throw new RuntimeException(e);
			}
		}
	}

	private void addToCartRequested(Cart cart, MenuItemActionBehaviorParams params) {
		if (rm.numberOfFoldersInFavoritesReachesLimit(cart.getId(), 1)) {
			params.getView().showMessage($("DisplayFolderViewImpl.cartCannotContainMoreThanAThousandFolders"));
		} else {
			Folder folder = rm.wrapFolder(params.getRecordVO().getRecord());
			folder.addFavorite(cart.getId());
			try {
				recordServices.update(folder.getWrappedRecord(), RecordUpdateOptions.validationExceptionSafeOptions());
				params.getView().showMessage($("DisplayFolderView.addedToCart"));
			} catch (RecordServicesException e) {
				e.printStackTrace();
				throw new RuntimeException(e);
			}
		}
	}

	private void deleteFolder(String reason, MenuItemActionBehaviorParams params) {
		String parentId = params.getRecordVO().get(Folder.PARENT_FOLDER);
		SchemaPresenterUtils presenterUtils = new SchemaPresenterUtils(Folder.DEFAULT_SCHEMA,
				params.getView().getConstellioFactories(), params.getView().getSessionContext());
		Record record = presenterUtils.toRecord(params.getRecordVO());
		ValidationErrors validateLogicallyDeletable = recordServices.validateLogicallyDeletable(record, params.getUser());
		if (validateLogicallyDeletable.isEmpty()) {
			appLayerFactory.getExtensions().forCollection(collection)
					.notifyFolderDeletion(new FolderDeletionEvent(rm.wrapFolder(record)));

			boolean isDeleteSuccessful = delete(presenterUtils, params.getView(), record, reason, false, 1);
			if (isDeleteSuccessful) {
				if (parentId != null) {
					RMNavigationUtils.navigateToDisplayFolder(parentId, params.getFormParams(), appLayerFactory, collection);
				} else {
					params.getView().navigate().to().home();
				}
			}
		} else {
			MessageUtils.getCannotDeleteWindow(validateLogicallyDeletable).openWindow();
		}
	}

	private boolean delete(SchemaPresenterUtils presenterUtils, BaseView view, Record record, String reason,
						   boolean physically, int waitSeconds) {
		boolean isDeletetionSuccessful = false;
		try {
			presenterUtils.delete(record, reason, physically, waitSeconds);
			isDeletetionSuccessful = true;
		} catch (RecordServicesRuntimeException_CannotLogicallyDeleteRecord exception) {
			view.showErrorMessage(MessageUtils.toMessage(exception));
		} catch (RecordDeleteServicesRuntimeException exception) {
			view.showErrorMessage(i18n.$("deletionFailed") + "\n" + MessageUtils.toMessage(exception));
		}

		return isDeletetionSuccessful;
	}

	private void navigateToDuplicateFolder(Folder folder, boolean isStructure, MenuItemActionBehaviorParams params) {
		boolean areTypeAndSearchIdPresent = DecommissionNavUtil.areTypeAndSearchIdPresent(params.getFormParams());

		if (areTypeAndSearchIdPresent) {
			params.getView().navigate().to(RMViews.class).duplicateFolderFromDecommission(params.getRecordVO().getId(), isStructure,
					DecommissionNavUtil.getSearchId(params.getFormParams()), DecommissionNavUtil.getSearchType(params.getFormParams()));
		} else if (rmModuleExtensions
				.navigateToDuplicateFolderWhileKeepingTraceOfPreviousView(
						new NavigateToFromAPageParams(params.getFormParams(), isStructure, params.getRecordVO().getId()))) {
		} else {
			params.getView().navigate().to(RMViews.class).duplicateFolder(folder.getId(), isStructure);
		}
	}

	private RecordVODataProvider getSharedCartsDataProvider(MenuItemActionBehaviorParams params) {
		final MetadataSchemaVO cartSchemaVO = schemaVOBuilder
				.build(rm.cartSchema(), VIEW_MODE.TABLE, params.getView().getSessionContext());
		return new RecordVODataProvider(cartSchemaVO, new RecordToVOBuilder(), modelLayerFactory, params.getView().getSessionContext()) {
			@Override
			protected LogicalSearchQuery getQuery() {
				return new LogicalSearchQuery(from(rm.cartSchema()).where(rm.cartSharedWithUsers())
						.isContaining(asList(params.getUser().getId()))).sortAsc(Schemas.TITLE);
			}
		};
	}

	private Date getPreviewReturnDate(Date borrowDate, Object borrowingTypeValue) {
		BorrowingType borrowingType;
		Date previewReturnDate = TimeProvider.getLocalDate().toDate();
		if (borrowDate != null && borrowingTypeValue != null) {
			borrowingType = (BorrowingType) borrowingTypeValue;
			if (borrowingType == BorrowingType.BORROW) {
				int addDays = rmConfigs.getBorrowingDurationDays();
				previewReturnDate = LocalDate.fromDateFields(borrowDate).plusDays(addDays).toDate();
			} else {
				previewReturnDate = borrowDate;
			}
		}
		return previewReturnDate;
	}

	private boolean borrowFolder(LocalDate borrowingDate, LocalDate previewReturnDate, String userId,
								 BorrowingType borrowingType, LocalDate returnDate,
								 MenuItemActionBehaviorParams params) {
		boolean borrowed;
		String errorMessage = borrowingServices
				.validateBorrowingInfos(userId, borrowingDate, previewReturnDate, borrowingType, returnDate);
		if (errorMessage != null) {
			params.getView().showErrorMessage($(errorMessage));
			borrowed = false;
		} else {
			Record record = recordServices.getDocumentById(userId);
			User borrowerEntered = wrapUser(record);
			try {
				borrowingServices.borrowFolder(params.getRecordVO().getId(), borrowingDate, previewReturnDate,
						params.getUser(), borrowerEntered, borrowingType, true);
				RMNavigationUtils.navigateToDisplayFolder(params.getRecordVO().getId(), params.getFormParams(),
						appLayerFactory, collection);
				borrowed = true;
			} catch (RecordServicesException e) {
				log.error(e.getMessage(), e);
				params.getView().showErrorMessage($("DisplayFolderView.cannotBorrowFolder"));
				borrowed = false;
			}
		}
		if (returnDate != null) {
			return returnFolder(returnDate, borrowingDate, params);
		}
		return borrowed;
	}

	private boolean returnFolder(LocalDate returnDate, LocalDate borrowingDate, MenuItemActionBehaviorParams params) {
		String errorMessage = borrowingServices.validateReturnDate(returnDate, borrowingDate);
		if (errorMessage != null) {
			params.getView().showErrorMessage($(errorMessage));
			return false;
		}
		try {
			borrowingServices.returnFolder(params.getRecordVO().getId(), params.getUser(), returnDate, true);
			RMNavigationUtils.navigateToDisplayFolder(params.getRecordVO().getId(), params.getFormParams(),
					appLayerFactory, collection);
			return true;
		} catch (RecordServicesException e) {
			params.getView().showErrorMessage($("DisplayFolderView.cannotReturnFolder"));
			return false;
		}
	}

	public boolean returnFolder(LocalDate returnDate, MenuItemActionBehaviorParams params) {
		LocalDateTime borrowDateTime = rm.wrapFolder(params.getRecordVO().getRecord()).getBorrowDate();
		LocalDate borrowDate = borrowDateTime != null ? borrowDateTime.toLocalDate() : null;
		return returnFolder(returnDate, borrowDate, params);
	}

	private User wrapUser(Record record) {
		return new User(record, schemaTypes, getCollectionRoles());
	}

	private Roles getCollectionRoles() {
		return modelLayerFactory.getRolesManager().getCollectionRoles(collection);
	}

	private EmailToSend newEmailToSend() {
		MetadataSchema schema = schemaTypes.getSchemaType(EmailToSend.SCHEMA_TYPE).getDefaultSchema();
		Record emailToSendRecord = recordServices.newRecordWithSchema(schema);
		return new EmailToSend(emailToSendRecord, schemaTypes);
	}
}
