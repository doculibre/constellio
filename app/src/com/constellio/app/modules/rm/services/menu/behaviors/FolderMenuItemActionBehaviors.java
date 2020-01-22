package com.constellio.app.modules.rm.services.menu.behaviors;

import com.constellio.app.api.extensions.params.NavigateToFromAPageParams;
import com.constellio.app.api.extensions.taxonomies.FolderDeletionEvent;
import com.constellio.app.modules.rm.ConstellioRMModule;
import com.constellio.app.modules.rm.RMConfigs;
import com.constellio.app.modules.rm.RMEmailTemplateConstants;
import com.constellio.app.modules.rm.extensions.api.RMModuleExtensions;
import com.constellio.app.modules.rm.model.labelTemplate.LabelTemplate;
import com.constellio.app.modules.rm.navigation.RMNavigationConfiguration;
import com.constellio.app.modules.rm.navigation.RMViews;
import com.constellio.app.modules.rm.services.RMSchemasRecordsServices;
import com.constellio.app.modules.rm.services.actions.DocumentRecordActionsServices;
import com.constellio.app.modules.rm.services.actions.FolderRecordActionsServices;
import com.constellio.app.modules.rm.services.borrowingServices.BorrowingServices;
import com.constellio.app.modules.rm.services.borrowingServices.BorrowingType;
import com.constellio.app.modules.rm.services.decommissioning.DecommissioningService;
import com.constellio.app.modules.rm.services.menu.behaviors.util.BehaviorsUtil;
import com.constellio.app.modules.rm.services.menu.behaviors.util.RMUrlUtil;
import com.constellio.app.modules.rm.ui.buttons.BorrowWindowButton;
import com.constellio.app.modules.rm.ui.buttons.CartWindowButton;
import com.constellio.app.modules.rm.ui.buttons.CartWindowButton.AddedRecordType;
import com.constellio.app.modules.rm.ui.components.folder.fields.LookupFolderField;
import com.constellio.app.modules.rm.util.DecommissionNavUtil;
import com.constellio.app.modules.rm.util.RMNavigationUtils;
import com.constellio.app.modules.rm.wrappers.Document;
import com.constellio.app.modules.rm.wrappers.Folder;
import com.constellio.app.services.factories.AppLayerFactory;
import com.constellio.app.services.menu.behavior.MenuItemActionBehaviorParams;
import com.constellio.app.ui.entities.MetadataSchemaVO;
import com.constellio.app.ui.entities.RecordVO;
import com.constellio.app.ui.entities.RecordVO.VIEW_MODE;
import com.constellio.app.ui.framework.builders.MetadataSchemaToVOBuilder;
import com.constellio.app.ui.framework.builders.RecordToVOBuilder;
import com.constellio.app.ui.framework.buttons.AddButton;
import com.constellio.app.ui.framework.buttons.BaseButton;
import com.constellio.app.ui.framework.buttons.DeleteButton;
import com.constellio.app.ui.framework.buttons.DeleteWithJustificationButton;
import com.constellio.app.ui.framework.buttons.LinkButton;
import com.constellio.app.ui.framework.buttons.WindowButton;
import com.constellio.app.ui.framework.buttons.WindowButton.WindowConfiguration;
import com.constellio.app.ui.framework.buttons.report.LabelButtonV2;
import com.constellio.app.ui.framework.clipboard.CopyToClipBoard;
import com.constellio.app.ui.framework.components.BaseWindow;
import com.constellio.app.ui.framework.components.RMSelectionPanelReportPresenter;
import com.constellio.app.ui.framework.components.ReportTabButton;
import com.constellio.app.ui.framework.components.display.ReferenceDisplay;
import com.constellio.app.ui.framework.components.fields.date.JodaDateField;
import com.constellio.app.ui.framework.data.RecordVODataProvider;
import com.constellio.app.ui.i18n.i18n;
import com.constellio.app.ui.pages.base.BaseView;
import com.constellio.app.ui.pages.base.SchemaPresenterUtils;
import com.constellio.app.ui.pages.base.SessionContext;
import com.constellio.app.ui.util.MessageUtils;
import com.constellio.data.utils.Factory;
import com.constellio.data.utils.TimeProvider;
import com.constellio.model.entities.records.Record;
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
import com.constellio.model.services.records.RecordHierarchyServices;
import com.constellio.model.services.records.RecordServices;
import com.constellio.model.services.records.RecordServicesException;
import com.constellio.model.services.records.RecordServicesRuntimeException.RecordServicesRuntimeException_CannotLogicallyDeleteRecord;
import com.constellio.model.services.search.SearchServices;
import com.constellio.model.services.search.query.logical.LogicalSearchQuery;
import com.constellio.model.services.security.roles.Roles;
import com.vaadin.shared.ui.label.ContentMode;
import com.vaadin.ui.Alignment;
import com.vaadin.ui.Button;
import com.vaadin.ui.Component;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Label;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.themes.ValoTheme;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;
import org.joda.time.LocalDate;
import org.joda.time.LocalDateTime;
import org.vaadin.dialogs.ConfirmDialog;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

import static com.constellio.app.ui.framework.components.ErrorDisplayUtil.showErrorMessage;
import static com.constellio.app.ui.i18n.i18n.$;
import static com.constellio.app.ui.util.UrlUtil.getConstellioUrl;
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
	private SearchServices searchServices;
	private MetadataSchemaToVOBuilder schemaVOBuilder;
	private BorrowingServices borrowingServices;
	private RMConfigs rmConfigs;
	private ConstellioEIMConfigs eimConfigs;
	private MetadataSchemaTypes schemaTypes;
	private FolderRecordActionsServices folderRecordActionsServices;
	private DocumentRecordActionsServices documentRecordActionsServices;
	private RecordHierarchyServices recordHierarchyServices;

	public static final String USER_LOOKUP = "user-lookup";

	public FolderMenuItemActionBehaviors(String collection, AppLayerFactory appLayerFactory) {
		this.collection = collection;
		this.appLayerFactory = appLayerFactory;
		modelLayerFactory = appLayerFactory.getModelLayerFactory();
		recordServices = modelLayerFactory.newRecordServices();
		rm = new RMSchemasRecordsServices(collection, appLayerFactory);
		decommissioningService = new DecommissioningService(collection, appLayerFactory);
		rmModuleExtensions = appLayerFactory.getExtensions().forCollection(collection).forModule(ConstellioRMModule.ID);
		searchServices = modelLayerFactory.newSearchServices();
		schemaVOBuilder = new MetadataSchemaToVOBuilder();
		borrowingServices = new BorrowingServices(collection, modelLayerFactory);
		rmConfigs = new RMConfigs(modelLayerFactory.getSystemConfigurationsManager());
		eimConfigs = new ConstellioEIMConfigs(modelLayerFactory.getSystemConfigurationsManager());
		schemaTypes = modelLayerFactory.getMetadataSchemasManager().getSchemaTypes(collection);
		folderRecordActionsServices = new FolderRecordActionsServices(collection, appLayerFactory);
		documentRecordActionsServices = new DocumentRecordActionsServices(collection, appLayerFactory);
		recordHierarchyServices = new RecordHierarchyServices(modelLayerFactory);
	}

	public void getConsultationLink(Folder folder, MenuItemActionBehaviorParams params) {
		folder = loadingFullRecordIfSummary(folder);
		String constellioURL = getConstellioUrl(modelLayerFactory);
		CopyToClipBoard.copyToClipBoard(constellioURL + RMUrlUtil.getPathToConsultLinkForFolder(folder.getId()));
	}

	public void addToDocument(Folder folderSummary, MenuItemActionBehaviorParams params) {
		Folder folder = loadingFullRecordIfSummary(folderSummary);
		Button addDocumentButton = new AddButton($("DisplayFolderView.addDocument")) {
			@Override
			protected void buttonClick(ClickEvent event) {
				params.getView().navigate().to(RMViews.class).addDocument(folder.getId());
			}
		};
		addDocumentButton.click();
	}

	public void move(Folder folder, MenuItemActionBehaviorParams params) {
		Button moveInFolderButton = new WindowButton($("DisplayFolderView.parentFolder"),
				$("DisplayFolderView.parentFolder"), WindowButton.WindowConfiguration.modalDialog("570px", "160px")) {
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

							String currentFolderId = folder.getId();
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

	public void addSubFolder(Folder folder, MenuItemActionBehaviorParams params) {
		Button addSubFolderButton = new AddButton($("DisplayFolderView.addSubFolder")) {
			@Override
			protected void buttonClick(ClickEvent event) {
				params.getView().navigate().to(RMViews.class).addFolder(folder.getId());
			}
		};
		addSubFolderButton.click();
	}

	public void display(Folder folder, MenuItemActionBehaviorParams params) {
		RMNavigationUtils.navigateToDisplayFolder(folder.getId(), params.getFormParams(),
				appLayerFactory, collection);
	}

	public void edit(Folder folder, MenuItemActionBehaviorParams params) {
		RMNavigationUtils.navigateToEditFolder(folder.getId(), params.getFormParams(),
				appLayerFactory, collection);
	}

	public void delete(Folder folderSummary, MenuItemActionBehaviorParams params) {
		Folder folder = loadingFullRecordIfSummary(folderSummary);
		boolean needAReasonToDeleteFolder = new RMConfigs(modelLayerFactory.getSystemConfigurationsManager())
				.isNeedingAReasonBeforeDeletingFolders();

		Button deleteFolderButton;

		List<Record> folderAndDocument = recordHierarchyServices.getAllRecordsInHierarchy(folder.getWrappedRecord(), true);

		List<RecordVO> checkoutRecords = getCheckoutRecordsAsVO(folderAndDocument, params.getView().getSessionContext());
		if (!needAReasonToDeleteFolder) {
			if (checkoutRecords.isEmpty()) {
				deleteFolderButton = new DeleteButton($("DisplayFolderView.deleteFolder"), false) {
					@Override
					protected void confirmButtonClick(ConfirmDialog dialog) {
						deleteFolder(folder, null, params);
					}

					@Override
					protected String getConfirmDialogMessage() {
						return $("ConfirmDialog.confirmDeleteWithRecord", folder.getTitle());
					}
				};
			} else {
				deleteFolderButton = getDeleteButtonWithCheckoutElementsMessage(folder, params, checkoutRecords);
			}
		} else {
			deleteFolderButton = getDeleteWithJustificationButton(folder, params, checkoutRecords);
		}
		deleteFolderButton.click();
	}

	@NotNull
	private Button getDeleteWithJustificationButton(Folder folderSummary, MenuItemActionBehaviorParams params,
													List<RecordVO> checkoutRecords) {
		Folder folder = loadingFullRecordIfSummary(folderSummary);
		Button deleteFolderButton;
		deleteFolderButton = new DeleteWithJustificationButton($("DisplayFolderView.deleteFolder"), false, WindowConfiguration.modalDialog("650px", null)) {
			@Override
			protected void deletionConfirmed(String reason) {
				deleteFolder(folder, reason, params);
			}

			@Override
			public Component getRecordCaption() {
				return new ReferenceDisplay(params.getRecordVO());
			}

			@Override
			protected Component buildWindowContent() {
				Component content = super.buildWindowContent();
				content.addStyleName(BaseWindow.WINDOW_CONTENT_WITH_BOTTOM_MARGIN);
				return content;
			}

			@Override
			public Component getMessageComponent() {
				if (checkoutRecords.isEmpty()) {
					return super.getMessageComponent();
				} else {
					VerticalLayout messageLayout = new VerticalLayout();
					Label someRecordsAreCheckoutLabel = new Label(getSomeRecordsAreCheckoutMessage());
					someRecordsAreCheckoutLabel.setContentMode(ContentMode.HTML);

					messageLayout.addComponent(someRecordsAreCheckoutLabel);

					for (RecordVO currentRecordVO : checkoutRecords) {
						messageLayout.addComponent(new ReferenceDisplay(currentRecordVO));
					}

					messageLayout.addComponent(new Label("<br>", ContentMode.HTML));

					messageLayout.addComponent(super.getMessageComponent());

					return messageLayout;
				}
			}
		};
		return deleteFolderButton;
	}

	private List<RecordVO> getCheckoutRecordsAsVO(List<Record> folderAndDocument, SessionContext sessionContext) {

		List<RecordVO> checktedMessage = new ArrayList<>();
		RecordToVOBuilder recordToVOBuilder = new RecordToVOBuilder();

		for (Record currentRecord : folderAndDocument) {
			if (currentRecord.isOfSchemaType(Document.SCHEMA_TYPE)) {
				Document document = rm.wrapDocument(currentRecord);

				if (documentRecordActionsServices.isContentCheckedOut(document.getContent())) {
					checktedMessage.add(recordToVOBuilder.build(currentRecord, VIEW_MODE.DISPLAY, sessionContext));
				}
			} else if (currentRecord.isOfSchemaType(Folder.SCHEMA_TYPE)) {
				Folder subFolder = rm.wrapFolder(currentRecord);

				if (subFolder.getBorrowed() != null && subFolder.getBorrowed()) {
					checktedMessage.add(recordToVOBuilder.build(currentRecord, VIEW_MODE.DISPLAY, sessionContext));
				}
			}
		}
		return checktedMessage;
	}

	@NotNull
	private WindowButton getDeleteButtonWithCheckoutElementsMessage(Folder folder, MenuItemActionBehaviorParams params,
																	List<RecordVO> recordVOList) {
		return new WindowButton($("DisplayFolderView.deleteFolder"), $("DisplayFolderView.deleteFolder"),
				WindowConfiguration.modalDialog("650px", null)) {
			@Override
			protected Component buildWindowContent() {
				VerticalLayout mainLayout = new VerticalLayout();

				mainLayout.addStyleName(BaseWindow.WINDOW_CONTENT_WITH_BOTTOM_MARGIN);

				VerticalLayout messageLayout = new VerticalLayout();

				Label someRecordAreCheckoutLabel = new Label(getSomeRecordsAreCheckoutMessage());

				someRecordAreCheckoutLabel.setContentMode(ContentMode.HTML);

				messageLayout.addComponent(someRecordAreCheckoutLabel);

				for (RecordVO currentRecordVO : recordVOList) {
					messageLayout.addComponent(new ReferenceDisplay(currentRecordVO));
				}

				mainLayout.addComponent(messageLayout);
				mainLayout.setSpacing(true);

				HorizontalLayout buttonLayout = new HorizontalLayout();


				BaseButton cancel = new BaseButton($("cancel")) {
					@Override
					protected void buttonClick(ClickEvent event) {
						getWindow().close();
					}
				};

				cancel.addStyleName(DeleteWithJustificationButton.CANCEL_DELETION);

				BaseButton confirm = new BaseButton($("confirm")) {
					@Override
					protected void buttonClick(ClickEvent event) {
						deleteFolder(folder, null, params);
					}
				};
				confirm.addStyleName(ValoTheme.BUTTON_PRIMARY);

				buttonLayout.addComponent(confirm);
				buttonLayout.setWidth("100%");
				buttonLayout.setComponentAlignment(confirm, Alignment.MIDDLE_RIGHT);
				buttonLayout.addComponent(cancel);
				buttonLayout.setComponentAlignment(cancel, Alignment.MIDDLE_LEFT);
				buttonLayout.setSpacing(true);

				mainLayout.addComponent(buttonLayout);

				return mainLayout;
			}
		};
	}

	private String getSomeRecordsAreCheckoutMessage() {
		return $("FolderMenuItemActionBehaviors.deletionConfirmationMessageSomeRecordAreCheckout") + "<br><br>"
			   + $("FolderMenuItemActionBehaviors.borrowedRecords") + "<br>";
	}

	public void copy(Folder folderSummary, MenuItemActionBehaviorParams params) {
		Folder folder = loadingFullRecordIfSummary(folderSummary);
		Button duplicateFolderButton = new WindowButton($("DisplayFolderView.duplicateFolder"),
				$("DisplayFolderView.duplicateFolderOnlyOrHierarchy"),
				WindowConfiguration.modalDialog("50%", "20%")) {
			@Override
			protected Component buildWindowContent() {
				BaseButton folderButton = new BaseButton($("DisplayFolderView.folderOnly")) {
					@Override
					protected void buttonClick(ClickEvent event) {
						if (folderRecordActionsServices.isCopyActionPossible(folder.getWrappedRecord(), params.getUser())) {
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
						if (folderRecordActionsServices.isCopyActionPossible(folder.getWrappedRecord(), params.getUser())) {
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

				HorizontalLayout layout = new HorizontalLayout(folderButton, structure, cancel);
				layout.setSpacing(true);

				VerticalLayout wrapper = new VerticalLayout(layout);
				wrapper.setSizeFull();
				wrapper.setComponentAlignment(layout, Alignment.MIDDLE_CENTER);

				return wrapper;
			}
		};
		duplicateFolderButton.click();
	}

	public void addAuthorization(Folder folder, MenuItemActionBehaviorParams params) {
		Button addAuthorizationButton = new LinkButton($("DisplayFolderView.addAuthorization")) {
			@Override
			protected void buttonClick(ClickEvent event) {
				params.getView().navigate().to().listObjectAccessAndRoleAuthorizations(folder.getId());
			}
		};
		addAuthorizationButton.click();
	}

	public void share(Folder folderSummary, MenuItemActionBehaviorParams params) {
		Folder folder = loadingFullRecordIfSummary(folderSummary);
		Button shareFolderButton = new LinkButton($("DisplayFolderView.shareFolder")) {
			@Override
			protected void buttonClick(ClickEvent event) {
				if (!folderRecordActionsServices.isShareActionPossible(folder.getWrappedRecord(), params.getUser())) {
					return;
				}
				params.getView().navigate().to().shareContent(folder.getId());
			}
		};
		shareFolderButton.click();
	}

	public void addToCart(Folder folder, MenuItemActionBehaviorParams params) {
		folder = loadingFullRecordIfSummary(folder);
		CartWindowButton cartWindowButton = new CartWindowButton(folder.getWrappedRecord(), params, AddedRecordType.FOLDER);
		cartWindowButton.addToCart();
	}

	public void borrow(Folder folder, MenuItemActionBehaviorParams params) {
		borrow(Arrays.asList(folder), params);
	}

	public void borrow(List<Folder> folders, MenuItemActionBehaviorParams params) {
		List<Record> records = new ArrayList<>();
		for (Folder folder : folders) {
			records.add(folder.getWrappedRecord());
		}

		Button borrowButton = new BorrowWindowButton(records, params);
		borrowButton.click();
	}

	public void returnFolder(Folder folderSummary, MenuItemActionBehaviorParams params) {
		Folder folder = loadingFullRecordIfSummary(folderSummary);
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
						if (returnFolder(folder, returnLocalDate, params)) {
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

	public void sendReturnRemainder(Folder folderSummary, MenuItemActionBehaviorParams params) {
		Folder folder = loadingFullRecordIfSummary(folderSummary);
		Button reminderReturnFolderButton = new BaseButton($("DisplayFolderView.reminderReturnFolder")) {
			@Override
			protected void buttonClick(ClickEvent event) {
				try {
					EmailToSend emailToSend = newEmailToSend();
					String constellioUrl = eimConfigs.getConstellioUrl();
					User borrower = null;
					if (folder.getBorrowUserEntered() != null) {
						borrower = rm.getUser(folder.getBorrowUserEntered());
					} else {
						borrower = rm.getUser(folder.getBorrowUser());
					}

					EmailAddress borrowerAddress = new EmailAddress(borrower.getTitle(), borrower.getEmail());
					emailToSend.setTo(Arrays.asList(borrowerAddress));
					emailToSend.setSendOn(TimeProvider.getLocalDateTime());
					emailToSend.setSubject($("DisplayFolderView.returnFolderReminder") + folder.getTitle());
					emailToSend.setTemplate(RMEmailTemplateConstants.REMIND_BORROW_TEMPLATE_ID);
					List<String> parameters = new ArrayList<>();
					String previewReturnDate = folder.getBorrowPreviewReturnDate().toString();
					parameters.add("previewReturnDate" + EmailToSend.PARAMETER_SEPARATOR + previewReturnDate);
					parameters.add("borrower" + EmailToSend.PARAMETER_SEPARATOR + borrower.getUsername());
					String borrowedFolderTitle = folder.getTitle();
					parameters.add("borrowedFolderTitle" + EmailToSend.PARAMETER_SEPARATOR + borrowedFolderTitle);
					boolean isAddingRecordIdInEmails = eimConfigs.isAddingRecordIdInEmails();
					if (isAddingRecordIdInEmails) {
						parameters.add("title" + EmailToSend.PARAMETER_SEPARATOR + $("DisplayFolderView.returnFolderReminder") + " \""
									   + folder.getTitle() + "\" (" + folder.getId() + ")");
					} else {
						parameters.add("title" + EmailToSend.PARAMETER_SEPARATOR + $("DisplayFolderView.returnFolderReminder") + " \""
									   + folder.getTitle() + "\"");
					}

					parameters.add("constellioURL" + EmailToSend.PARAMETER_SEPARATOR + constellioUrl);
					parameters.add("recordURL" + EmailToSend.PARAMETER_SEPARATOR + constellioUrl + "#!"
								   + RMNavigationConfiguration.DISPLAY_FOLDER + "/" + folder.getId());
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

	public void sendAvailableAlert(Folder folderSummary, MenuItemActionBehaviorParams params) {
		Folder folder = loadingFullRecordIfSummary(folderSummary);
		Button alertWhenAvailableButton = new BaseButton($("RMObject.alertWhenAvailable")) {
			@Override
			protected void buttonClick(ClickEvent event) {
				try {
					List<String> usersToAlert = folder.getAlertUsersWhenAvailable();
					String currentUserId = folder.getId();
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

	public void printLabel(Folder folder, MenuItemActionBehaviorParams params) {
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

	public void generateReport(Folder folderSummary, MenuItemActionBehaviorParams params) {
		Folder folder = loadingFullRecordIfSummary(folderSummary);
		// FIXME refactor so that we don't we need to instanciate a presenter
		RMSelectionPanelReportPresenter reportPresenter =
				new RMSelectionPanelReportPresenter(appLayerFactory, collection, params.getUser()) {
					@Override
					public String getSelectedSchemaType() {
						return Folder.SCHEMA_TYPE;
					}

					@Override
					public List<String> getSelectedRecordIds() {
						return asList(folder.getId());
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

	public void addToSelection(Folder folder, MenuItemActionBehaviorParams params) {
		params.getView().getSessionContext().addSelectedRecordId(folder.getId(), Folder.SCHEMA_TYPE);
	}

	public void removeFromSelection(Folder folder, MenuItemActionBehaviorParams params) {
		params.getView().getSessionContext().removeSelectedRecordId(folder.getId(), Folder.SCHEMA_TYPE);
	}

	protected void deleteFolder(Folder folder, String reason, MenuItemActionBehaviorParams params) {
		String parentId = folder.get(Folder.PARENT_FOLDER);
		SchemaPresenterUtils presenterUtils = new SchemaPresenterUtils(Folder.DEFAULT_SCHEMA,
				params.getView().getConstellioFactories(), params.getView().getSessionContext());
		ValidationErrors validateLogicallyDeletable = recordServices.validateLogicallyDeletable(folder, params.getUser());
		if (validateLogicallyDeletable.isEmpty()) {
			appLayerFactory.getExtensions().forCollection(collection)
					.notifyFolderDeletion(new FolderDeletionEvent(folder));

			boolean isDeleteSuccessful = delete(presenterUtils, params.getView(), folder.getWrappedRecord(), reason, false, 1);
			if (isDeleteSuccessful) {
				if (BehaviorsUtil.reloadIfSearchView(params.getView())) {
					return;
				} else if (parentId != null) {
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
			params.getView().navigate().to(RMViews.class).duplicateFolderFromDecommission(folder.getId(), isStructure,
					DecommissionNavUtil.getSearchId(params.getFormParams()), DecommissionNavUtil.getSearchType(params.getFormParams()));
		} else if (rmModuleExtensions
				.navigateToDuplicateFolderWhileKeepingTraceOfPreviousView(
						new NavigateToFromAPageParams(params.getFormParams(), isStructure, folder.getId()))) {
		} else {
			params.getView().navigate().to(RMViews.class).duplicateFolder(folder.getId(), isStructure);
		}
	}

	private RecordVODataProvider getSharedCartsDataProvider(MenuItemActionBehaviorParams params) {
		final MetadataSchemaVO cartSchemaVO = schemaVOBuilder
				.build(rm.cartSchema(), VIEW_MODE.TABLE, params.getView().getSessionContext());
		return new RecordVODataProvider(cartSchemaVO, new RecordToVOBuilder(), modelLayerFactory, params.getView().getSessionContext()) {
			@Override
			public LogicalSearchQuery getQuery() {
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

	private boolean borrowFolder(Folder folder, LocalDate borrowingDate, LocalDate previewReturnDate, String userId,
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
				borrowingServices.borrowFolder(folder.getId(), borrowingDate, previewReturnDate,
						params.getUser(), borrowerEntered, borrowingType, true);
				RMNavigationUtils.navigateToDisplayFolder(folder.getId(), params.getFormParams(),
						appLayerFactory, collection);
				borrowed = true;
			} catch (RecordServicesException e) {
				log.error(e.getMessage(), e);
				params.getView().showErrorMessage($("DisplayFolderView.cannotBorrowFolder"));
				borrowed = false;
			}
		}
		if (returnDate != null) {
			return returnFolder(folder, returnDate, borrowingDate, params);
		}
		return borrowed;
	}

	private boolean returnFolder(Folder folder, LocalDate returnDate, LocalDate borrowingDate,
								 MenuItemActionBehaviorParams params) {
		String errorMessage = borrowingServices.validateReturnDate(returnDate, borrowingDate);
		if (errorMessage != null) {
			params.getView().showErrorMessage($(errorMessage));
			return false;
		}
		try {
			borrowingServices.returnFolder(folder.getId(), params.getUser(), returnDate, true);
			RMNavigationUtils.navigateToDisplayFolder(folder.getId(), params.getFormParams(),
					appLayerFactory, collection);
			return true;
		} catch (RecordServicesException e) {
			params.getView().showErrorMessage($("DisplayFolderView.cannotReturnFolder"));
			return false;
		}
	}

	public boolean returnFolder(Folder folder, LocalDate returnDate, MenuItemActionBehaviorParams params) {
		folder = loadingFullRecordIfSummary(folder);
		LocalDateTime borrowDateTime = folder.getBorrowDate();
		LocalDate borrowDate = borrowDateTime != null ? borrowDateTime.toLocalDate() : null;
		return returnFolder(folder, returnDate, borrowDate, params);
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

	private Folder loadingFullRecordIfSummary(Folder folder) {
		if (folder.isSummary()) {
			return rm.getFolder(folder.getId());
		} else {
			return folder;
		}

	}
}
