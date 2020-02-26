package com.constellio.app.modules.rm.services.menu.behaviors;

import com.constellio.app.api.extensions.params.NavigateToFromAPageParams;
import com.constellio.app.api.extensions.taxonomies.FolderDeletionEvent;
import com.constellio.app.modules.rm.ConstellioRMModule;
import com.constellio.app.modules.rm.RMConfigs;
import com.constellio.app.modules.rm.extensions.api.RMModuleExtensions;
import com.constellio.app.modules.rm.model.labelTemplate.LabelTemplate;
import com.constellio.app.modules.rm.navigation.RMViews;
import com.constellio.app.modules.rm.services.RMSchemasRecordsServices;
import com.constellio.app.modules.rm.services.actions.DocumentRecordActionsServices;
import com.constellio.app.modules.rm.services.actions.FolderRecordActionsServices;
import com.constellio.app.modules.rm.services.decommissioning.DecommissioningService;
import com.constellio.app.modules.rm.services.menu.behaviors.ui.SendReturnReminderEmailButton;
import com.constellio.app.modules.rm.services.menu.behaviors.util.BehaviorsUtil;
import com.constellio.app.modules.rm.services.menu.behaviors.util.RMUrlUtil;
import com.constellio.app.modules.rm.ui.buttons.BorrowWindowButton;
import com.constellio.app.modules.rm.ui.buttons.CartWindowButton;
import com.constellio.app.modules.rm.ui.buttons.CartWindowButton.AddedRecordType;
import com.constellio.app.modules.rm.ui.buttons.ReturnWindowButton;
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
import com.constellio.app.ui.framework.data.RecordVODataProvider;
import com.constellio.app.ui.i18n.i18n;
import com.constellio.app.ui.pages.base.BaseView;
import com.constellio.app.ui.pages.base.SchemaPresenterUtils;
import com.constellio.app.ui.pages.base.SessionContext;
import com.constellio.app.ui.util.MessageUtils;
import com.constellio.data.utils.Factory;
import com.constellio.model.entities.records.Record;
import com.constellio.model.entities.records.wrappers.User;
import com.constellio.model.entities.schemas.Schemas;
import com.constellio.model.frameworks.validation.ValidationErrors;
import com.constellio.model.services.factories.ModelLayerFactory;
import com.constellio.model.services.records.RecordDeleteServicesRuntimeException;
import com.constellio.model.services.records.RecordHierarchyServices;
import com.constellio.model.services.records.RecordServices;
import com.constellio.model.services.records.RecordServicesException;
import com.constellio.model.services.records.RecordServicesRuntimeException.RecordServicesRuntimeException_CannotLogicallyDeleteRecord;
import com.constellio.model.services.search.query.logical.LogicalSearchQuery;
import com.vaadin.shared.ui.MarginInfo;
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
import org.vaadin.dialogs.ConfirmDialog;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
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
	private MetadataSchemaToVOBuilder schemaVOBuilder;
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
		schemaVOBuilder = new MetadataSchemaToVOBuilder();
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
				$("DisplayFolderView.parentFolder"), WindowButton.WindowConfiguration.modalDialog("570px", "140px")) {
			@Override
			protected Component buildWindowContent() {
				VerticalLayout verticalLayout = new VerticalLayout();
				verticalLayout.setSpacing(true);
				final LookupFolderField field = new LookupFolderField(true);
				verticalLayout.addComponent(field);
				verticalLayout.setMargin(new MarginInfo(true, true, false, true));
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

	public void borrow(Folder folderSummary, MenuItemActionBehaviorParams params) {
		Folder folder = loadingFullRecordIfSummary(folderSummary);
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
		Button returnButton = new ReturnWindowButton(appLayerFactory, collection,
				Collections.singletonList(folder.getWrappedRecord()), params, true);
		returnButton.click();
	}

	public void sendReturnRemainder(Folder folderSummary, MenuItemActionBehaviorParams params) {
		Folder folder = loadingFullRecordIfSummary(folderSummary);
		User borrower = null;
		if (folder.getBorrowUserEntered() != null) {
			borrower = rm.getUser(folder.getBorrowUserEntered());
		} else {
			borrower = rm.getUser(folder.getBorrowUser());
		}
		String previewReturnDate = folder.getBorrowPreviewReturnDate().toString();

		Button reminderReturnFolderButton = new SendReturnReminderEmailButton(collection, appLayerFactory,
				params.getView(), Folder.SCHEMA_TYPE, folder.get(), borrower, previewReturnDate);
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

	private Folder loadingFullRecordIfSummary(Folder folder) {
		if (folder.isSummary()) {
			return rm.getFolder(folder.getId());
		} else {
			return folder;
		}
	}
}
