package com.constellio.app.modules.rm.services.menu.behaviors;

import static com.constellio.app.ui.framework.clipboard.CopyToClipBoard.copyConsultationLinkToClipBoard;
import static com.constellio.app.ui.i18n.i18n.$;
import static com.constellio.app.ui.util.UrlUtil.getConstellioUrl;
import static com.constellio.model.services.search.query.logical.LogicalSearchQueryOperators.from;
import static java.util.Arrays.asList;
import static org.apache.commons.lang.StringUtils.isNotBlank;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.mail.MessagingException;
import javax.mail.internet.MimeMessage;

import org.apache.commons.io.IOUtils;
import org.joda.time.LocalDate;
import org.joda.time.LocalDateTime;
import org.vaadin.dialogs.ConfirmDialog;

import com.constellio.app.api.extensions.params.EmailMessageParams;
import com.constellio.app.modules.rm.RMConfigs;
import com.constellio.app.modules.rm.constants.RMPermissionsTo;
import com.constellio.app.modules.rm.model.labelTemplate.LabelTemplate;
import com.constellio.app.modules.rm.services.RMSchemasRecordsServices;
import com.constellio.app.modules.rm.services.actions.ContainerRecordActionsServices;
import com.constellio.app.modules.rm.services.actions.DocumentRecordActionsServices;
import com.constellio.app.modules.rm.services.actions.FolderRecordActionsServices;
import com.constellio.app.modules.rm.services.borrowingServices.BorrowingServices;
import com.constellio.app.modules.rm.services.cart.CartEmailService;
import com.constellio.app.modules.rm.services.cart.CartEmailServiceRuntimeException;
import com.constellio.app.modules.rm.services.decommissioning.DecommissioningService;
import com.constellio.app.modules.rm.services.menu.behaviors.ui.SendReturnReminderEmailButton;
import com.constellio.app.modules.rm.services.menu.behaviors.util.RMMessageUtil;
import com.constellio.app.modules.rm.services.menu.behaviors.util.RMUrlUtil;
import com.constellio.app.modules.rm.ui.builders.DocumentToVOBuilder;
import com.constellio.app.modules.rm.ui.builders.UserToVOBuilder;
import com.constellio.app.modules.rm.ui.buttons.BorrowRequestWindowButton;
import com.constellio.app.modules.rm.ui.buttons.BorrowWindowButton;
import com.constellio.app.modules.rm.ui.buttons.CartWindowButton;
import com.constellio.app.modules.rm.ui.buttons.CartWindowButton.AddedRecordType;
import com.constellio.app.modules.rm.ui.buttons.ReturnWindowButton;
import com.constellio.app.modules.rm.ui.components.folder.fields.LookupFolderField;
import com.constellio.app.modules.rm.ui.entities.DocumentVO;
import com.constellio.app.modules.rm.ui.pages.pdf.ConsolidatedPdfButton;
import com.constellio.app.modules.rm.wrappers.ContainerRecord;
import com.constellio.app.modules.rm.wrappers.Document;
import com.constellio.app.modules.rm.wrappers.Folder;
import com.constellio.app.modules.rm.wrappers.RMTask;
import com.constellio.app.modules.rm.wrappers.StorageSpace;
import com.constellio.app.modules.tasks.model.wrappers.Task;
import com.constellio.app.modules.tasks.model.wrappers.TaskStatusType;
import com.constellio.app.modules.tasks.model.wrappers.request.RequestTask;
import com.constellio.app.modules.tasks.model.wrappers.request.ReturnRequest;
import com.constellio.app.modules.tasks.navigation.TaskViews;
import com.constellio.app.modules.tasks.services.TasksSchemasRecordsServices;
import com.constellio.app.modules.tasks.services.menu.behaviors.util.TaskUrlUtil;
import com.constellio.app.services.factories.AppLayerFactory;
import com.constellio.app.services.factories.ConstellioFactories;
import com.constellio.app.services.menu.behavior.MenuItemActionBehaviorParams;
import com.constellio.app.ui.application.ConstellioUI;
import com.constellio.app.ui.entities.MetadataVO;
import com.constellio.app.ui.entities.RecordVO;
import com.constellio.app.ui.entities.RecordVO.VIEW_MODE;
import com.constellio.app.ui.entities.UserVO;
import com.constellio.app.ui.framework.builders.RecordToVOBuilder;
import com.constellio.app.ui.framework.buttons.BaseButton;
import com.constellio.app.ui.framework.buttons.BaseLink;
import com.constellio.app.ui.framework.buttons.ContainersButton;
import com.constellio.app.ui.framework.buttons.ContainersButton.ContainersAssigner;
import com.constellio.app.ui.framework.buttons.DeleteButton;
import com.constellio.app.ui.framework.buttons.DeleteWithJustificationButton;
import com.constellio.app.ui.framework.buttons.WindowButton;
import com.constellio.app.ui.framework.buttons.SIPButton.SIPButtonImpl;
import com.constellio.app.ui.framework.buttons.report.LabelButtonV2;
import com.constellio.app.ui.framework.components.RMSelectionPanelReportPresenter;
import com.constellio.app.ui.framework.components.ReportTabButton;
import com.constellio.app.ui.framework.components.content.UpdateContentVersionWindowImpl;
import com.constellio.app.ui.framework.stream.DownloadStreamResource;
import com.constellio.app.ui.framework.window.ConsultLinkWindow.ConsultLinkParams;
import com.constellio.app.ui.i18n.i18n;
import com.constellio.app.ui.pages.base.BaseView;
import com.constellio.app.ui.pages.base.SchemaPresenterUtils;
import com.constellio.app.ui.pages.base.SessionContext;
import com.constellio.app.ui.util.MessageUtils;
import com.constellio.data.io.services.facades.IOServices;
import com.constellio.data.utils.Factory;
import com.constellio.model.entities.records.Content;
import com.constellio.model.entities.records.ContentVersion;
import com.constellio.model.entities.records.Record;
import com.constellio.model.entities.records.RecordUpdateOptions;
import com.constellio.model.entities.records.Transaction;
import com.constellio.model.entities.records.wrappers.Authorization;
import com.constellio.model.entities.records.wrappers.User;
import com.constellio.model.entities.schemas.Metadata;
import com.constellio.model.entities.schemas.MetadataSchemaType;
import com.constellio.model.entities.schemas.Schemas;
import com.constellio.model.entities.security.global.AuthorizationDeleteRequest;
import com.constellio.model.extensions.ModelLayerCollectionExtensions;
import com.constellio.model.frameworks.validation.ValidationErrors;
import com.constellio.model.services.contents.ContentManager;
import com.constellio.model.services.contents.ContentVersionDataSummary;
import com.constellio.model.services.emails.EmailServices;
import com.constellio.model.services.emails.EmailServices.EmailMessage;
import com.constellio.model.services.factories.ModelLayerFactory;
import com.constellio.model.services.migrations.ConstellioEIMConfigs;
import com.constellio.model.services.records.RecordDeleteServicesRuntimeException;
import com.constellio.model.services.records.RecordServices;
import com.constellio.model.services.records.RecordServicesException;
import com.constellio.model.services.records.RecordServicesRuntimeException;
import com.constellio.model.services.search.query.logical.condition.LogicalSearchCondition;
import com.constellio.model.services.search.zipContents.ZipContentsService;
import com.constellio.model.services.search.zipContents.ZipContentsService.NoContentToZipRuntimeException;
import com.nimbusds.oauth2.sdk.util.CollectionUtils;
import com.vaadin.server.Page;
import com.vaadin.server.Resource;
import com.vaadin.server.StreamResource;
import com.vaadin.server.StreamResource.StreamSource;
import com.vaadin.shared.ui.MarginInfo;
import com.vaadin.shared.ui.label.ContentMode;
import com.vaadin.ui.Alignment;
import com.vaadin.ui.Button;
import com.vaadin.ui.Component;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.themes.ValoTheme;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class RMRecordsMenuItemBehaviors {

	private String collection;
	private AppLayerFactory appLayerFactory;
	private ModelLayerFactory modelLayerFactory;
	private RecordServices recordServices;
	private RMSchemasRecordsServices rm;
	private DecommissioningService decommissioningService;
	private IOServices ioServices;
	private RMConfigs rmConfigs;
	private TasksSchemasRecordsServices taskServices;
	private BorrowingServices borrowingServices;

	private FolderRecordActionsServices folderRecordActionsServices;
	private DocumentRecordActionsServices documentRecordActionsServices;
	private ContainerRecordActionsServices containerRecordActionsServices;
	private ModelLayerCollectionExtensions modelCollectionExtensions;

	private static final String ZIP_CONTENT_RESOURCE = "zipContentsFolder";

	public RMRecordsMenuItemBehaviors(String collection, AppLayerFactory appLayerFactory) {
		this.collection = collection;
		this.appLayerFactory = appLayerFactory;
		modelLayerFactory = appLayerFactory.getModelLayerFactory();
		recordServices = modelLayerFactory.newRecordServices();
		rm = new RMSchemasRecordsServices(collection, appLayerFactory);
		decommissioningService = new DecommissioningService(collection, appLayerFactory);
		ioServices = modelLayerFactory.getDataLayerFactory().getIOServicesFactory().newIOServices();
		modelCollectionExtensions = modelLayerFactory.getExtensions().forCollection(collection);
		rmConfigs = new RMConfigs(modelLayerFactory.getSystemConfigurationsManager());
		taskServices = new TasksSchemasRecordsServices(collection, appLayerFactory);
		borrowingServices = new BorrowingServices(collection, modelLayerFactory);

		folderRecordActionsServices = new FolderRecordActionsServices(collection, appLayerFactory);
		documentRecordActionsServices = new DocumentRecordActionsServices(collection, appLayerFactory);
		containerRecordActionsServices = new ContainerRecordActionsServices(collection, appLayerFactory);
		rm = new RMSchemasRecordsServices(collection, appLayerFactory);
	}

	public void addToCart(List<String> recordIds, MenuItemActionBehaviorParams params) {
		CartWindowButton cartWindowButton =
				new CartWindowButton(getSelectedRecords(recordIds), params, AddedRecordType.MULTIPLE);
		cartWindowButton.addToCart();
	}

	public void move(List<String> recordIds, MenuItemActionBehaviorParams params) {
		WindowButton moveInFolderButton = new WindowButton($("ConstellioHeader.selection.actions.moveInFolder"),
				$("ConstellioHeader.selection.actions.moveInFolder"),
				WindowButton.WindowConfiguration.modalDialog("50%", "140px")) {
			@Override
			protected Component buildWindowContent() {
				VerticalLayout verticalLayout = new VerticalLayout();
				verticalLayout.addStyleName("no-scroll");
				verticalLayout.setSpacing(true);
				verticalLayout.setMargin(new MarginInfo(true, true, false, true));
				final LookupFolderField field = new LookupFolderField(true);
				field.focus();
				verticalLayout.addComponent(field);
				BaseButton saveButton = new BaseButton($("save")) {
					@Override
					protected void buttonClick(ClickEvent event) {
						String parentId = (String) field.getValue();
						try {
							moveFolder(parentId, getSelectedRecords(recordIds), params.getUser(), params.getView());
						} catch (Throwable e) {
							e.printStackTrace();
						}
						getWindow().close();
						Page.getCurrent().reload();
					}
				};
				saveButton.addStyleName(ValoTheme.BUTTON_PRIMARY);
				HorizontalLayout hLayout = new HorizontalLayout();
				hLayout.setSpacing(true);
				hLayout.setSizeFull();
				hLayout.addComponent(saveButton);
				hLayout.setComponentAlignment(saveButton, Alignment.BOTTOM_RIGHT);
				verticalLayout.addComponent(hLayout);
				return verticalLayout;
			}
		};
		moveInFolderButton.click();
	}

	public void copy(List<String> recordIds, MenuItemActionBehaviorParams params) {
		WindowButton duplicateButton = new WindowButton($("ConstellioHeader.selection.actions.duplicate"),
				$("ConstellioHeader.selection.actions.duplicate"),
				WindowButton.WindowConfiguration.modalDialog("550px", "200px")) {
			@Override
			protected Component buildWindowContent() {
				VerticalLayout verticalLayout = new VerticalLayout();
				verticalLayout.setSizeFull();
				verticalLayout.setSpacing(true);
				verticalLayout.setMargin(new MarginInfo(true, true, false, true));
				final LookupFolderField field = new LookupFolderField(true);
				field.focus();
				verticalLayout.addComponent(field);
				BaseButton saveButton = new BaseButton($("save")) {
					@Override
					protected void buttonClick(ClickEvent event) {
						String parentId = (String) field.getValue();
						duplicateButtonClicked(parentId, getSelectedRecords(recordIds), params.getUser(), params.getView());
						getWindow().close();
					}
				};
				saveButton.addStyleName(ValoTheme.BUTTON_PRIMARY);
				HorizontalLayout hLayout = new HorizontalLayout();
				hLayout.setSizeFull();
				hLayout.addComponent(saveButton);
				hLayout.setComponentAlignment(saveButton, Alignment.MIDDLE_CENTER);
				verticalLayout.addComponent(hLayout);
				return verticalLayout;
			}
		};
		duplicateButton.click();
	}


	public void createSipArchive(List<String> recordIds, MenuItemActionBehaviorParams params) {
		SIPButtonImpl sipButton = new SIPButtonImpl($("SIPButton.caption"), $("SIPButton.caption"),
				ConstellioUI.getCurrent().getHeader(), true) {
			@Override
			public void buttonClick(ClickEvent event) {
				RecordVO[] recordVOS = getRecordVOList(recordIds, params.getView()).toArray(new RecordVO[0]);
				setAllObject(recordVOS);
				super.buttonClick(event);
			}
		};
		sipButton.click();
	}

	public void sendEmail(List<String> recordIds, MenuItemActionBehaviorParams params) {
		Button button = new Button($("ConstellioHeader.selection.actions.prepareEmail"));
		button.addClickListener((event) -> {
			EmailMessage emailMessage = createEmail(recordIds, params.getUser(), params.getView());
			String filename = emailMessage.getFilename();
			InputStream stream = emailMessage.getInputStream();
			startDownload(stream, filename);
		});
		button.click();
	}

	public void createPdf(List<String> recordIds, MenuItemActionBehaviorParams params) {
		WindowButton pdfButton = new ConsolidatedPdfButton(recordIds) {
			@Override
			public void buttonClick(ClickEvent event) {
				List<Record> records = recordServices.getRecordsById(collection, recordIds);
				for (Record record : records) {
					if (!documentRecordActionsServices.isCreatePdfActionPossible(record, params.getUser())) {
						return;
					}
				}
				super.buttonClick(event);
			}
		};
		pdfButton.click();
	}

	public void checkOut(List<String> recordIds, MenuItemActionBehaviorParams params) {
		List<Record> records = recordServices.getRecordsById(collection, recordIds);
		Record record = records.get(0);

		if (record.isOfSchemaType(Document.SCHEMA_TYPE)) {
			checkOutDocuments(records, params);
		} else if (record.isOfSchemaType(Folder.SCHEMA_TYPE) || record.isOfSchemaType(ContainerRecord.SCHEMA_TYPE)) {
			Button borrowButton = new BorrowWindowButton(records, params);
			borrowButton.click();
		}
	}

	private void checkOutDocuments(List<Record> records, MenuItemActionBehaviorParams params) {
		List<Document> documents = rm.wrapDocuments(records);
		new DocumentMenuItemActionBehaviors(collection, appLayerFactory).checkOut(documents, params);

		try {
			recordServices.update(documents, new RecordUpdateOptions().setOverwriteModificationDateAndUser(false), params.getUser());

			params.getView().refreshActionMenu();
			params.getView().showMessage($("DocumentActionsComponent.multipleCheckOut"));
		} catch (RecordServicesException e) {
			params.getView().showErrorMessage(MessageUtils.toMessage(e));
		}
	}

	public void checkOutRequest(List<String> recordIds, MenuItemActionBehaviorParams params) {
		List<Record> records = recordServices.getRecordsById(collection, recordIds);
		Record record = records.get(0);
		boolean isFolder = record.isOfSchemaType(Folder.SCHEMA_TYPE);

		WindowButton borrowRequestButton =
				new BorrowRequestWindowButton(appLayerFactory, collection, records, params, isFolder);
		borrowRequestButton.click();
	}

	public List<String> getRequestFromUser(String schemaName, User currentUser, List<Record> linkedRecords,
										   Metadata metadataLinkedRecord) {
		MetadataSchemaType taskSchemaType = taskServices.taskSchemaType();
		Metadata metadataStatus = taskSchemaType.getAllMetadatas().getMetadataWithLocalCode(Task.STATUS_TYPE);
		Metadata metadataApplicant = taskSchemaType.getAllMetadatas().getMetadataWithLocalCode(RequestTask.APPLICANT);
		LogicalSearchCondition logicalSearchCondition = from(taskSchemaType)
				.where(Schemas.SCHEMA).isEqualTo(schemaName)
				.andWhere(metadataLinkedRecord).isIn(linkedRecords)
				.andWhere(metadataStatus).isEqualTo(TaskStatusType.STANDBY)
				.andWhere(metadataApplicant).isEqualTo(currentUser.getId());

		return modelLayerFactory.newSearchServices().searchRecordIds(logicalSearchCondition);
	}

	public List<String> getAssigneesForFolder(String recordId) {
		return modelLayerFactory.newAuthorizationsServices()
				.getUserIdsWithPermissionOnRecord(RMPermissionsTo.MANAGE_REQUEST_ON_FOLDER,
						recordServices.getDocumentById(recordId));
	}

	public List<String> getAssigneesForContainer(String recordId) {
		return modelLayerFactory.newAuthorizationsServices()
				.getUserIdsWithPermissionOnRecord(RMPermissionsTo.MANAGE_REQUEST_ON_CONTAINER,
						recordServices.getDocumentById(recordId));
	}

	public void addTasksWithUserSafeOption(List<Task> tasks) throws RecordServicesException {
		Transaction transaction = new Transaction();
		transaction.setOptions(RecordUpdateOptions.userModificationsSafeOptions());
		transaction.addAll(tasks);
		recordServices.execute(transaction);
	}

	public void checkIn(List<String> recordIds, MenuItemActionBehaviorParams params) {
		List<Record> records = recordServices.getRecordsById(collection, recordIds);
		Record record = records.get(0);

		if (record.isOfSchemaType(Document.SCHEMA_TYPE)) {
			checkInDocuments(recordIds, params);
		} else if (record.isOfSchemaType(Folder.SCHEMA_TYPE)) {
			Button returnButton = new ReturnWindowButton(appLayerFactory, collection, records, params, true);
			returnButton.click();
		} else if (record.isOfSchemaType(ContainerRecord.SCHEMA_TYPE)) {
			Button returnButton = new ReturnWindowButton(appLayerFactory, collection, records, params, false);
			returnButton.click();
		}
	}

	public boolean returnRecords(List<Record> records, LocalDate returnDate, MenuItemActionBehaviorParams params,
								 boolean isFolder) {
		LocalDateTime borrowDateTime = null;
		if (isFolder) {
			List<Folder> folders = rm.wrapFolders(records);
			for (Folder folder : folders) {
				LocalDateTime folderBorrowDate = folder.getBorrowDate();
				if (borrowDateTime == null || folderBorrowDate.isAfter(borrowDateTime)) {
					borrowDateTime = folderBorrowDate;
				}
			}
		} else {
			List<ContainerRecord> containers = rm.wrapContainerRecords(records);
			for (ContainerRecord container : containers) {
				LocalDate containerBorrowDate = container.getBorrowDate();
				if (borrowDateTime == null || containerBorrowDate.toDateTimeAtStartOfDay().toLocalDateTime().isAfter(borrowDateTime)) {
					borrowDateTime = containerBorrowDate.toDateTimeAtStartOfDay().toLocalDateTime();
				}
			}
		}
		LocalDate borrowDate = borrowDateTime != null ? borrowDateTime.toLocalDate() : null;
		return returnRecords(records, returnDate, borrowDate, params, isFolder);
	}

	public boolean returnRecords(List<Record> records, LocalDate returnDate, LocalDate borrowingDate,
								 MenuItemActionBehaviorParams params, boolean isFolder) {
		String errorMessage = borrowingServices.validateReturnDate(returnDate, borrowingDate);
		if (errorMessage != null) {
			params.getView().showErrorMessage($(errorMessage));
			return false;
		}
		try {
			if (isFolder) {
				borrowingServices.returnFolders(records, params.getUser(), returnDate, true);
				deletePendingReturnRequestForRecords(records, params.getUser(),
						taskServices.taskSchemaType().getAllMetadatas().getMetadataWithLocalCode(Task.LINKED_FOLDERS));
			} else {
				borrowingServices.returnContainers(records, params.getUser(), returnDate, true);
				deletePendingReturnRequestForRecords(records, params.getUser(),
						taskServices.taskSchemaType().getAllMetadatas().getMetadataWithLocalCode(Task.LINKED_CONTAINERS));
			}
			params.getView().updateUI();
			params.getView().refreshActionMenu();
			params.getView().showMessage($(isFolder ? "DisplayFolderView.multipleCheckIn"
													: "DisplayContainerView.multipleCheckIn"));
			return true;
		} catch (RecordServicesException e) {
			params.getView().showErrorMessage($(isFolder ? "DisplayFolderView.cannotReturnFolder"
														 : "DisplayContainerView.cannotReturnContainer"));
			return false;
		}
	}

	private void deletePendingReturnRequestForRecords(List<Record> records, User user, Metadata metadataLinkedRecord) {
		List<String> pendingRequests = getPendingReturnRequestForRecords(records, metadataLinkedRecord);

		if (CollectionUtils.isNotEmpty(pendingRequests)) {
			for (String recordId : pendingRequests) {
				Record record = recordServices.getDocumentById(recordId);
				recordServices.logicallyDelete(record, user);
				modelLayerFactory.newLoggingServices().logDeleteRecordWithJustification(record, user, "");
			}
		}
	}

	private List<String> getPendingReturnRequestForRecords(List<Record> records, Metadata metadataLinkedRecord) {
		MetadataSchemaType taskSchemaType = taskServices.taskSchemaType();
		Metadata metadataStatus = taskSchemaType.getAllMetadatas().getMetadataWithLocalCode(Task.STATUS_TYPE);
		LogicalSearchCondition logicalSearchCondition = from(taskSchemaType)
				.where(Schemas.SCHEMA).isEqualTo(ReturnRequest.FULL_SCHEMA_NAME)
				.andWhere(metadataLinkedRecord).isIn(records)
				.andWhere(metadataStatus).isEqualTo(TaskStatusType.STANDBY);

		return modelLayerFactory.newSearchServices().searchRecordIds(logicalSearchCondition);
	}

	public void checkInRequest(List<String> recordIds, MenuItemActionBehaviorParams params) {
		List<Record> records = recordServices.getRecordsById(collection, recordIds);
		Record record = records.get(0);

		if (record.isOfSchemaType(Folder.SCHEMA_TYPE)) {
			returnRecordsRequest(records, params, true);
		} else if (record.isOfSchemaType(ContainerRecord.SCHEMA_TYPE)) {
			returnRecordsRequest(records, params, false);
		}
	}

	private void returnRecordsRequest(List<Record> records, MenuItemActionBehaviorParams params, boolean isFolder) {
		try {
			List<String> linkedRecordIds = new ArrayList<>();
			Metadata metadataLinkedRecord = taskServices.taskSchemaType().getAllMetadatas()
					.getMetadataWithLocalCode(isFolder ? Task.LINKED_FOLDERS : Task.LINKED_CONTAINERS);
			List<String> pendingRequestIds = getRequestFromUser(ReturnRequest.FULL_SCHEMA_NAME, params.getUser(),
					records, metadataLinkedRecord);
			if (pendingRequestIds.size() > 0) {
				List<Record> pendingRequests = rm.get(pendingRequestIds);
				for (Record pendingRequest : pendingRequests) {
					linkedRecordIds.addAll(pendingRequest.getList(metadataLinkedRecord));
				}
			}

			List<Task> tasks = new ArrayList<>();
			for (Record record : records) {
				if (!linkedRecordIds.contains(record.getId())) {
					Task request;
					if (isFolder) {
						request = taskServices.newReturnFolderRequestTask(params.getUser().getId(),
								getAssigneesForFolder(record.getId()), record.getId(), record.getTitle());
					} else {
						request = taskServices.newReturnContainerRequestTask(params.getUser().getId(),
								getAssigneesForContainer(record.getId()), record.getId(), record.getTitle());
					}
					tasks.add(request);
				}
			}

			if (tasks.size() > 0) {
				addTasksWithUserSafeOption(tasks);
				params.getView().showMessage($("RMRequestTaskButtonExtension.returnSuccess"));
			} else {
				params.getView().showErrorMessage($("RMRequestTaskButtonExtension.taskAlreadyCreated"));
			}

		} catch (RecordServicesException e) {
			e.printStackTrace();
			params.getView().showErrorMessage($("RMRequestTaskButtonExtension.errorWhileCreatingTask"));
		}
	}

	public void sendReturnRemainder(List<String> recordIds, MenuItemActionBehaviorParams params) {
		List<Record> records = recordServices.getRecordsById(collection, recordIds);
		Record record = records.get(0);

		if (record.isOfSchemaType(Document.SCHEMA_TYPE)) {
			sendDocumentsReturnRemainder(records, params);
		} else if (record.isOfSchemaType(Folder.SCHEMA_TYPE)) {
			sendFoldersReturnRemainder(records, params);
		} else if (record.isOfSchemaType(ContainerRecord.SCHEMA_TYPE)) {
			sendContainersReturnRemainder(records, params);
		}
	}

	private void sendDocumentsReturnRemainder(List<Record> records, MenuItemActionBehaviorParams params) {
		List<Document> documents = rm.wrapDocuments(records);
		for (Document document : documents) {
			User borrower = null;
			if (document.getContentCheckedOutBy() != null) {
				borrower = rm.getUser(document.getContentCheckedOutBy());
			}
			String previewReturnDate = document.getContentCheckedOutDate().plusDays(getBorrowingDuration()).toString();

			Button reminderReturnDocumentButton = new SendReturnReminderEmailButton(collection, appLayerFactory,
					params.getView(), Document.SCHEMA_TYPE, document.get(), borrower, previewReturnDate);
			reminderReturnDocumentButton.click();
		}
	}

	private void sendFoldersReturnRemainder(List<Record> records, MenuItemActionBehaviorParams params) {
		List<Folder> folders = rm.wrapFolders(records);
		for (Folder folder : folders) {
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
	}

	private void sendContainersReturnRemainder(List<Record> records, MenuItemActionBehaviorParams params) {
		List<ContainerRecord> containers = rm.wrapContainerRecords(records);
		for (ContainerRecord container : containers) {
			User borrower = null;
			if (container.getBorrower() != null) {
				borrower = rm.getUser(container.getBorrower());
			}
			String previewReturnDate = container.getPlanifiedReturnDate().toString();

			Button reminderReturnContainerButton = new SendReturnReminderEmailButton(collection, appLayerFactory,
					params.getView(), ContainerRecord.SCHEMA_TYPE, container.get(), borrower, previewReturnDate);
			reminderReturnContainerButton.click();
		}
	}

	private int getBorrowingDuration() {
		return new RMConfigs(appLayerFactory.getModelLayerFactory().getSystemConfigurationsManager()).getDocumentBorrowingDurationDays();
	}

	public void checkInDocuments(List<String> recordIds, MenuItemActionBehaviorParams params) {
		List<Document> documents = rm.wrapDocuments(recordServices.realtimeGetRecordById(recordIds));
		UpdateContentVersionWindowImpl uploadWindow =
				createUpdateContentVersionWindow(documents, params);

		boolean hasUpdate = false;
		for (Document document : documents) {
			if (!isCheckedOutDocumentContentChanged(document)) {
				hasUpdate = true;
				break;
			}
		}

		if (hasUpdate) {
			uploadWindow.open(false);
		} else {
			uploadWindow.saveWithSameVersion();
			params.getView().updateUI();
		}
	}

	private boolean isCheckedOutDocumentContentChanged(Document document) {
		Content content = document.getContent();
		return content != null && content.getCurrentVersion().getHash().equals(content.getCurrentCheckedOutVersion().getHash());
	}

	private UpdateContentVersionWindowImpl createUpdateContentVersionWindow(List<Document> documents,
																			MenuItemActionBehaviorParams params) {
		final Map<RecordVO, MetadataVO> recordMap = new HashMap<>();
		for (Document document : documents) {
			RecordVO recordVO = getDocumentVO(params, document);
			recordMap.put(recordVO, recordVO.getMetadata(Document.CONTENT));
		}

		return new UpdateContentVersionWindowImpl(recordMap) {
			@Override
			public void close() {
				super.close();
				params.getView().updateUI();
			}

			@Override
			public void showMessage(String message) {
				params.getView().showMessage(message);
			}
		};
	}

	private DocumentVO getDocumentVO(MenuItemActionBehaviorParams params, Document document) {
		return new DocumentToVOBuilder(appLayerFactory.getModelLayerFactory()).build(document.getWrappedRecord(),
				VIEW_MODE.DISPLAY, params.getView().getSessionContext());
	}

	public void generateReport(List<String> recordIds, MenuItemActionBehaviorParams params) {
		if (recordIds.isEmpty()) {
			return;
		}

		String schemaType = recordServices.getDocumentById(recordIds.get(0)).getSchemaCode().split("_")[0];

		RMSelectionPanelReportPresenter rmSelectionPanelReportPresenter = new RMSelectionPanelReportPresenter(appLayerFactory, collection, params.getUser()) {
			@Override
			public String getSelectedSchemaType() {
				return schemaType;
			}

			@Override
			public List<String> getSelectedRecordIds() {
				return recordIds;
			}


		};

		ReportTabButton reportGeneratorButton = new ReportTabButton($("SearchView.metadataReportTitle"),
				$("SearchView.metadataReportTitle"),
				appLayerFactory, collection, false, false,
				rmSelectionPanelReportPresenter, params.getView().getSessionContext()) {

		};
		List<RecordVO> recordVOList = getRecordVOList(recordIds, params.getView());
		reportGeneratorButton.setRecordVoList(recordVOList.toArray(new RecordVO[0]));
		reportGeneratorButton.click();
	}

	public void printLabels(List<String> recordIds, MenuItemActionBehaviorParams params) {
		if (recordIds.isEmpty()) {
			return;
		}
		String schemaType = recordServices.getDocumentById(recordIds.get(0)).getSchemaCode().split("_")[0];

		Factory<List<LabelTemplate>> customLabelTemplatesFactory =
				() -> appLayerFactory.getLabelTemplateManager().listExtensionTemplates(schemaType);
		Factory<List<LabelTemplate>> defaultLabelTemplatesFactory =
				() -> appLayerFactory.getLabelTemplateManager().listTemplates(schemaType);
		UserToVOBuilder userToVOBuilder = new UserToVOBuilder();
		UserVO userVO = userToVOBuilder.build(params.getUser().getWrappedRecord(),
				VIEW_MODE.DISPLAY, params.getView().getSessionContext());

		final LabelButtonV2 labelsButton = new LabelButtonV2($("SearchView.printLabels"),
				$("SearchView.printLabels"),
				customLabelTemplatesFactory,
				defaultLabelTemplatesFactory,
				appLayerFactory, collection, userVO);
		labelsButton.setSchemaType(schemaType);
		labelsButton.addClickListener(new Button.ClickListener() {
			@Override
			public void buttonClick(Button.ClickEvent event) {
				labelsButton.setElementsWithIds(recordIds, schemaType, params.getView().getSessionContext());
			}
		});
		labelsButton.click();
	}

	public void addToSelection(List<String> recordIds, MenuItemActionBehaviorParams params) {
		BaseView view = params.getView();

		SessionContext sessionContext = view.getSessionContext();
		boolean someElementsNotAdded = false;
		for (String selectedRecordId : recordIds) {
			Record record = recordServices.getDocumentById(selectedRecordId);

			if (asList(Folder.SCHEMA_TYPE, Document.SCHEMA_TYPE, ContainerRecord.SCHEMA_TYPE).contains(record.getTypeCode())) {
				sessionContext.addSelectedRecordId(selectedRecordId, record.getTypeCode());
			} else {
				someElementsNotAdded = true;
			}
		}

		if (someElementsNotAdded) {
			view.showErrorMessage($("ConstellioHeader.selection.cannotAddRecords"));
		}
	}

	public void removeFromSelection(List<String> recordIds, MenuItemActionBehaviorParams params) {
		BaseView view = params.getView();

		SessionContext sessionContext = view.getSessionContext();
		boolean someElementsNotRemoved = false;
		List<String> recordIdsRemoved = new ArrayList<>();
		for (String selectedRecordId : recordIds) {
			Record record = recordServices.getDocumentById(selectedRecordId);

			if (asList(Folder.SCHEMA_TYPE, Document.SCHEMA_TYPE, ContainerRecord.SCHEMA_TYPE).contains(record.getTypeCode())) {
				sessionContext.removeSelectedRecordId(selectedRecordId, record.getTypeCode());
				recordIdsRemoved.add(selectedRecordId);
			} else {
				someElementsNotRemoved = true;
			}
		}

		ConstellioUI.getCurrent().getHeader().removeItems(recordIdsRemoved);

		if (someElementsNotRemoved) {
			view.showErrorMessage($("ConstellioHeader.selection.cannotRemoveRecords"));
		}
	}

	public void downloadZip(List<String> recordIds, MenuItemActionBehaviorParams params) {
		BaseView view = params.getView();

		StreamSource streamSource = () -> {
			File folder = ioServices.newTemporaryFolder(ZIP_CONTENT_RESOURCE);
			File file = new File(folder, $("SearchView.contentZip"));
			try {
				new ZipContentsService(modelLayerFactory, collection)
						.zipContentsOfRecords(recordIds, file);
				return new FileInputStream(file);
			} catch (NoContentToZipRuntimeException e) {
				log.error("Error while zipping", e);
				view.showErrorMessage($("SearchView.noContentInSelectedRecords"));
				return null;
			} catch (Exception e) {
				log.error("Error while zipping", e);
				view.showErrorMessage($("SearchView.zipContentsError"));
				return null;
			}
		};

		BaseLink zipButton = new BaseLink($("ReportViewer.download", "(zip)"),
				new DownloadStreamResource(streamSource, $("SearchView.contentZip")));
		zipButton.click();
	}

	public boolean isNeedingAReasonToDeleteRecords() {
		return rmConfigs.isNeedingAReasonBeforeDeletingFolders();
	}

	private int countPerShemaType(String schemaType, List<String> recordIds) {
		int counter = 0;

		if (recordIds == null) {
			return 0;
		}

		for (Record record : recordServices.getRecordsById(collection, recordIds)) {
			if (record.getSchemaCode().startsWith(schemaType)) {
				counter++;
			}
		}

		return counter;
	}

	public void showConsultLink(List<String> recordIds, MenuItemActionBehaviorParams params) {
		String constellioURL = getConstellioUrl(modelLayerFactory);

		List<ConsultLinkParams> linkList = new ArrayList<>();

		List<Record> recordList = recordServices.getRecordsById(collection, recordIds);

		for (Record currentRecord : recordList) {
			if (currentRecord.getSchemaCode().startsWith(Document.SCHEMA_TYPE)) {
				linkList.add(new ConsultLinkParams(constellioURL + RMUrlUtil.getPathToConsultLinkForDocument(currentRecord.getId()),
						currentRecord.getTitle()));
			} else if (currentRecord.getSchemaCode().startsWith(Folder.SCHEMA_TYPE)) {
				linkList.add(new ConsultLinkParams(constellioURL + RMUrlUtil.getPathToConsultLinkForFolder(currentRecord.getId()),
						currentRecord.getTitle()));
			} else if (currentRecord.getSchemaCode().startsWith(ContainerRecord.SCHEMA_TYPE)) {
				linkList.add(new ConsultLinkParams(constellioURL + RMUrlUtil.getPathToConsultLinkForContainerRecord(currentRecord.getId()),
						currentRecord.getTitle()));
			} else if (currentRecord.getSchemaCode().startsWith(RMTask.SCHEMA_TYPE)) {
				linkList.add(new ConsultLinkParams(constellioURL + TaskUrlUtil.getPathToConsultLinkForTask(currentRecord.getId()),
						currentRecord.getTitle()));
			} else if (currentRecord.getSchemaCode().startsWith(StorageSpace.SCHEMA_TYPE)) {
				linkList.add(new ConsultLinkParams(constellioURL + RMUrlUtil.getPathToConsultLinkForStorageSpace(currentRecord.getId()),
						currentRecord.getTitle()));
			}
		}

		copyConsultationLinkToClipBoard(linkList);
	}

	public void batchDelete(List<String> recordIds, MenuItemActionBehaviorParams params) {
		Button button;
		if (!isNeedingAReasonToDeleteRecords()) {
			button = new DeleteButton(false) {
				@Override
				protected void confirmButtonClick(ConfirmDialog dialog) {
					try {
						deletionRequested(null, recordIds, params);
					} catch (RecordDeleteServicesRuntimeException e) {
						params.getView().showMessage(i18n.$("deletionFailed") + "\n" + MessageUtils.toMessage(e));
						return;
					}
					Page.getCurrent().reload();
				}

				@Override
				protected String getConfirmDialogMessage() {
					int folderCount = countPerShemaType(Folder.SCHEMA_TYPE, recordIds);
					int documentCount = countPerShemaType(Document.SCHEMA_TYPE, recordIds);
					int containerCount = countPerShemaType(ContainerRecord.SCHEMA_TYPE, recordIds);

					StringBuilder stringBuilder = RMMessageUtil.getRecordCountByTypeAsText(folderCount, documentCount, containerCount);

					return $("CartView.deleteConfirmationMessageWithoutJustification", stringBuilder.toString());
				}
			};
		} else {
			button = new DeleteWithJustificationButton(false) {
				@Override
				protected void deletionConfirmed(String reason) {
					try {
						deletionRequested(reason, recordIds, params);
					} catch (RecordDeleteServicesRuntimeException e) {
						params.getView().showMessage(i18n.$("deletionFailed") + "\n" + MessageUtils.toMessage(e));
						return;
					}
					Page.getCurrent().reload();
				}

				@Override
				protected String getConfirmDialogMessage() {
					int folderCount = countPerShemaType(Folder.SCHEMA_TYPE, recordIds);
					int documentCount = countPerShemaType(Document.SCHEMA_TYPE, recordIds);
					int containerCount = countPerShemaType(ContainerRecord.SCHEMA_TYPE, recordIds);

					StringBuilder stringBuilder = RMMessageUtil.getRecordCountByTypeAsText(folderCount, documentCount, containerCount);

					return $("CartView.deleteConfirmationMessage", stringBuilder.toString());
				}
			};
			((DeleteWithJustificationButton) button).setMessageContentMode(ContentMode.HTML);
		}

		button.click();
	}

	public void createTask(List<String> ids, MenuItemActionBehaviorParams params) {
		params.getView().navigate().to(TaskViews.class).addLinkedRecordsToTask(ids);
	}

	public void batchUnPublishDocument(List<String> recordIds, MenuItemActionBehaviorParams params) {
		Button button = new DeleteButton(false) {
			@Override
			protected void confirmButtonClick(ConfirmDialog dialog) {
				batchUnPublishDocumentConfirmed(recordIds, params.getUser(), params.getView());
			}

			@Override
			protected String getConfirmDialogMessage() {
				return $("DocumentContextMenu.batchUnPublishConfirmationMsg");
			}
		};

		button.click();
	}

	public void batchUnPublishDocumentConfirmed(List<String> recordIds, User user, BaseView baseView) {
		List<Document> documentToUnPublish = rm.getDocuments(recordIds);

		for (Document document : documentToUnPublish) {
			document.setPublished(false);
			document.setPublishingEndDate(null);
			document.setPublishingStartDate(null);
		}

		try {
			recordServices.update(documentToUnPublish, user);
			baseView.refreshActionMenu();
			baseView.partialRefresh();
		} catch (RecordServicesException e) {
			baseView.showErrorMessage(MessageUtils.toMessage(e));
		}
	}

	public void batchUnshare(List<String> recordIds, MenuItemActionBehaviorParams params) {
		Button button = new DeleteButton(false) {
			@Override
			protected void confirmButtonClick(ConfirmDialog dialog) {
				unshareFolderButtonClicked(recordIds, params.getUser());
				params.getView().refreshActionMenu();
				params.getView().partialRefresh();
			}

			@Override
			protected String getConfirmDialogMessage() {
				return $("DocumentContextMenu.batchUnshareConfirmationMsg");
			}
		};

		button.click();
	}

	public void unshareFolderButtonClicked(List<String> ids, User user) {

		List<Authorization> authorizations = rm.getMultipleSolrAuthorizationDetails(user, ids);

		for (Authorization authorization : authorizations) {
			try {
				rm.getModelLayerFactory()
						.newAuthorizationsServices().execute(toAuthorizationDeleteRequest(authorization, user));

			} catch (RecordServicesRuntimeException.RecordServicesRuntimeException_CannotLogicallyDeleteRecord e) {
				return;
			}
		}
	}

	private AuthorizationDeleteRequest toAuthorizationDeleteRequest(Authorization authorization, User user) {
		String authId = authorization.getId();

		AuthorizationDeleteRequest request = AuthorizationDeleteRequest.authorizationDeleteRequest(authId, user.getCollection());

		return request;

	}

	private boolean isBatchDeletePossible(List<String> recordIds, MenuItemActionBehaviorParams params) {
		return documentRecordActionsServices.canDeleteDocuments(recordIds, params.getUser())
			   && folderRecordActionsServices.canDeleteFolders(recordIds, params.getUser())
			   && containerRecordActionsServices.canDeleteContainers(recordIds, params.getUser());
	}

	private void deletionRequested(String reason, List<String> recordIds, MenuItemActionBehaviorParams params) {
		if (!isBatchDeletePossible(recordIds, params)) {
			params.getView().showErrorMessage($("cannotDelete"));
			return;
		}
		List<Record> recordsById = recordServices.getRecordsById(collection, recordIds);
		for (Record record : recordsById) {
			ValidationErrors validateDeleteAuthorized = modelCollectionExtensions.validateDeleteAuthorized(record, params.getUser());
			if (!validateDeleteAuthorized.isEmpty()) {
				MessageUtils.getCannotDeleteWindow(validateDeleteAuthorized).openWindow();
				return;
			}
		}

		for (Record record : recordsById) {
			delete(record, reason, params);
		}
	}

	protected final void delete(Record record, String reason,
								MenuItemActionBehaviorParams menuItemActionBehaviorParams) {
		delete(record, reason, true, false, menuItemActionBehaviorParams);
	}


	protected final void delete(Record record, String reason, boolean physically, boolean throwException,
								MenuItemActionBehaviorParams params) {
		SchemaPresenterUtils presenterUtils = new SchemaPresenterUtils(Document.DEFAULT_SCHEMA,
				params.getView().getConstellioFactories(), params.getView().getSessionContext());
		presenterUtils.delete(record, null, true, 1);
	}

	//
	// PRIVATE
	//

	private void moveFolder(String parentId, List<Record> records, User user, BaseView view)
			throws RecordServicesException {
		List<String> couldNotMove = new ArrayList<>();
		if (isNotBlank(parentId)) {
			List<Record> recordsToMove = new ArrayList<>();
			for (Record record : records) {
				try {
					switch (record.getTypeCode()) {
						case Folder.SCHEMA_TYPE:
							Folder folder = rm.wrapFolder(record);
							if (!folderRecordActionsServices.isMoveActionPossible(record, user)) {
								couldNotMove.add(record.getTitle());
								break;
							}
							Transaction txValidateFolder = new Transaction(folder.setParentFolder(parentId));
							txValidateFolder.setOptions(RecordUpdateOptions.userModificationsSafeOptions());
							recordServices.validateTransaction(txValidateFolder);
							recordsToMove.add(record);

							break;
						case Document.SCHEMA_TYPE:
							if (!documentRecordActionsServices.isMoveActionPossible(record, user)) {
								couldNotMove.add(record.getTitle());
								break;
							}
							Transaction txValidateDocument = new Transaction(rm.wrapDocument(record).setFolder(parentId));
							txValidateDocument.setOptions(RecordUpdateOptions.userModificationsSafeOptions());
							recordServices.validateTransaction(txValidateDocument);
							recordsToMove.add(record);

							break;
						default:
							couldNotMove.add(record.getTitle());
					}
				} catch (RecordServicesException.ValidationException e) {
					e.printStackTrace();
					couldNotMove.add(record.getTitle());
				}
			}

			Transaction tx = new Transaction(recordsToMove);
			tx.setOptions(RecordUpdateOptions.userModificationsSafeOptions());
			recordServices.executeHandlingImpactsAsync(tx);
		}


		if (couldNotMove.isEmpty()) {
			view.showErrorMessage($("ConstellioHeader.selection.actions.actionCompleted", records.size()));
		} else {
			int successCount = records.size() - couldNotMove.size();
			view.showErrorMessage($("ConstellioHeader.selection.actions.couldNotMove", successCount, records.size()));
		}
	}

	private void duplicateButtonClicked(String parentId, List<Record> records, User user, BaseView view) {
		List<String> couldNotDuplicate = new ArrayList<>();
		if (isNotBlank(parentId)) {
			for (Record record : records) {
				try {
					switch (record.getTypeCode()) {
						case Folder.SCHEMA_TYPE:
							if (!folderRecordActionsServices.isCopyActionPossible(record, user)) {
								couldNotDuplicate.add(record.getTitle());
								break;
							}
							Folder oldFolder = rm.wrapFolder(record);
							Folder newFolder = decommissioningService.duplicateStructureAndDocuments(oldFolder, user, false);
							newFolder.setParentFolder(parentId);
							recordServices.add(newFolder);
							break;
						case Document.SCHEMA_TYPE:
							if (!documentRecordActionsServices.isCopyActionPossible(record, user)) {
								couldNotDuplicate.add(record.getTitle());
								break;
							}
							Document oldDocument = rm.wrapDocument(record);
							Document newDocument = rm.newDocumentWithType(oldDocument.getType());
							for (Metadata metadata : oldDocument.getSchema().getMetadatas().onlyNonSystemReserved().onlyManuals().onlyDuplicable()) {
								newDocument.set(metadata, record.get(metadata));
							}
							LocalDateTime now = LocalDateTime.now();
							newDocument.setFormCreatedBy(user);
							newDocument.setFormCreatedOn(now);
							newDocument.setCreatedBy(user.getId()).setModifiedBy(user.getId());
							newDocument.setCreatedOn(now).setModifiedOn(now);
							if (newDocument.getContent() != null) {
								Content content = newDocument.getContent();
								ContentVersion contentVersion = content.getCurrentVersion();
								String filename = contentVersion.getFilename();
								ContentManager contentManager = modelLayerFactory.getContentManager();
								ContentVersionDataSummary contentVersionDataSummary = contentManager.getContentVersionSummary(contentVersion.getHash()).getContentVersionDataSummary();
								Content newContent = contentManager.createMajor(user, filename, contentVersionDataSummary);
								newDocument.setContent(newContent);
							}
							if (Boolean.TRUE == newDocument.getBorrowed()) {
								newDocument.setBorrowed(false);
							}
							String title = record.getTitle() + " (" + $("AddEditDocumentViewImpl.copy") + ")";
							newDocument.setTitle(title);
							newDocument.setFolder(parentId);
							recordServices.add(newDocument);
							break;
						default:
							couldNotDuplicate.add(record.getTitle());
					}
				} catch (RecordServicesException e) {
					couldNotDuplicate.add(record.getTitle());
				}
			}
		}

		if (couldNotDuplicate.isEmpty()) {
			view.showErrorMessage($("ConstellioHeader.selection.actions.actionCompleted", records.size()));
		} else {
			int successCount = records.size() - couldNotDuplicate.size();
			view.showErrorMessage($("ConstellioHeader.selection.actions.couldNotDuplicate", successCount, records.size()));
		}
	}

	private EmailMessage createEmail(List<String> recordIds, User user, BaseView view) {
		File newTempFile = null;
		try {
			newTempFile = ioServices.newTemporaryFile("RMSelectionPanelExtension-emailFile");
			return createEmail(recordIds, user, view, newTempFile);
		} catch (Exception e) {
			throw new RuntimeException(e);
		} finally {
			ioServices.deleteQuietly(newTempFile);
		}
	}

	private EmailMessage createEmail(List<String> recordIds, User user, BaseView view, File messageFile) {
		try (OutputStream outputStream = ioServices.newFileOutputStream(messageFile, RMRecordsMenuItemBehaviors.class.getSimpleName() + ".createMessage.out")) {
			String signature = user.getSignature() != null ? user.getSignature() : user.getTitle();
			String subject = "";
			String from = user.getEmail();
			//FIXME current version get only cart documents attachments
			List<EmailServices.MessageAttachment> attachments = getDocumentsAttachments(recordIds);
			if (attachments == null || attachments.isEmpty()) {
				view.showErrorMessage($("ConstellioHeader.selection.actions.noApplicableRecords"));
				return null;
			} else if (attachments.size() != recordIds.size()) {
				view.showErrorMessage($("ConstellioHeader.selection.actions.couldNotSendEmail", attachments.size(), recordIds.size()));
			}

			AppLayerFactory appLayerFactory = ConstellioFactories.getInstance().getAppLayerFactory();
			EmailMessageParams params = new EmailMessageParams("selection", signature, subject, from, attachments);
			EmailMessage emailMessage = appLayerFactory.getExtensions().getSystemWideExtensions().newEmailMessage(params);
			if (emailMessage == null) {
				EmailServices emailServices = new EmailServices();
				ConstellioEIMConfigs configs = new ConstellioEIMConfigs(modelLayerFactory);
				MimeMessage message = emailServices.createMimeMessage(from, subject, signature, attachments, configs);
				message.writeTo(outputStream);
				String filename = "cart.eml";
				InputStream inputStream = ioServices.newFileInputStream(messageFile, CartEmailService.class.getSimpleName() + ".createMessageForCart.in");
				emailMessage = new EmailMessage(filename, inputStream);
				attachments.forEach(attachment -> {
					ioServices.closeQuietly(attachment.getInputStream());
					IOUtils.closeQuietly(attachment.getInputStream());
				});
			}
			if (attachments.size() == recordIds.size()) {
				view.showErrorMessage($("ConstellioHeader.selection.actions.actionCompleted", attachments.size()));
			}
			return emailMessage;
		} catch (MessagingException | IOException e) {
			throw new RuntimeException(e);
		}
	}

	private List<EmailServices.MessageAttachment> getDocumentsAttachments(List<String> recordIds) {
		List<EmailServices.MessageAttachment> returnList = new ArrayList<>();
		for (String currentDocumentId : recordIds) {
			Record record = recordServices.getDocumentById(currentDocumentId);
			if (record.isOfSchemaType(Document.SCHEMA_TYPE)) {
				try {
					Document document = rm.wrapDocument(record);
					if (document.getContent() != null) {
						EmailServices.MessageAttachment contentFile = createAttachment(document);
						returnList.add(contentFile);
					}
				} catch (RecordServicesRuntimeException.NoSuchRecordWithId e) {
					throw new CartEmailServiceRuntimeException.CartEmlServiceRuntimeException_InvalidRecordId(e);
				}
			}
		}
		return returnList;
	}

	private EmailServices.MessageAttachment createAttachment(Document document) {
		Content content = document.getContent();
		String hash = content.getCurrentVersion().getHash();
		ContentManager contentManager = modelLayerFactory.getContentManager();
		InputStream inputStream = contentManager.getContentInputStream(hash, content.getCurrentVersion().getFilename());
		String mimeType = content.getCurrentVersion().getMimetype();
		String attachmentName = content.getCurrentVersion().getFilename();
		return new EmailServices.MessageAttachment().setMimeType(mimeType).setAttachmentName(attachmentName).setInputStream(inputStream);
	}

	private void startDownload(final InputStream stream, String filename) {
		Resource resource = new DownloadStreamResource(new StreamResource.StreamSource() {
			@Override
			public InputStream getStream() {
				return stream;
			}
		}, filename);
		Page.getCurrent().open(resource, null, false);
	}

	private List<RecordVO> getRecordVOList(List<String> ids, BaseView view) {
		List<RecordVO> recordsVO = new ArrayList<>();
		RecordToVOBuilder builder = new RecordToVOBuilder();
		for (String id : ids) {
			recordsVO.add(builder.build(recordServices.getDocumentById(id), RecordVO.VIEW_MODE.FORM, view.getSessionContext()));
		}
		return recordsVO;
	}

	private List<Record> getSelectedRecords(List<String> selectedRecordIds) {
		return recordServices.getRecordsById(collection, selectedRecordIds);
	}

	public void putInContainer(List<String> ids, MenuItemActionBehaviorParams params) {
		ContainersAssigner selector = new ContainersAssigner() {
			@Override
			public void putRecordsInContainer(String containerId) {
				Transaction transaction = new Transaction();
				if (containerId == null) {
					log.error("Invalid null container", containerId);
					return;
				}
				ContainerRecord container = rm.getContainerRecord(containerId);
				if (container == null) {
					log.error("Invalid null container", containerId);
					return;
				}
				//TODO by batch
				for (String recordId : ids) {
					transaction.add(rm.getFolder(recordId).setContainer(container));
				}
				try {
					recordServices.executeHandlingImpactsAsync(transaction);
				} catch (RecordServicesException e) {
					throw new RuntimeException(e);
				}
			}
		};
		new ContainersButton(selector).click();
	}
}
