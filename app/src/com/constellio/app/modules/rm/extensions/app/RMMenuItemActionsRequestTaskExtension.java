package com.constellio.app.modules.rm.extensions.app;

import com.constellio.app.extensions.menu.MenuItemActionsExtension;
import com.constellio.app.modules.rm.ConstellioRMModule;
import com.constellio.app.modules.rm.RMConfigs;
import com.constellio.app.modules.rm.constants.RMPermissionsTo;
import com.constellio.app.modules.rm.extensions.api.RMModuleExtensions;
import com.constellio.app.modules.rm.model.enums.FolderStatus;
import com.constellio.app.modules.rm.services.RMSchemasRecordsServices;
import com.constellio.app.modules.rm.services.borrowingServices.BorrowingServices;
import com.constellio.app.modules.rm.services.decommissioning.DecommissioningService;
import com.constellio.app.modules.rm.ui.pages.containers.DisplayContainerViewImpl;
import com.constellio.app.modules.rm.wrappers.ContainerRecord;
import com.constellio.app.modules.rm.wrappers.Folder;
import com.constellio.app.modules.tasks.model.wrappers.Task;
import com.constellio.app.modules.tasks.model.wrappers.TaskStatusType;
import com.constellio.app.modules.tasks.model.wrappers.request.BorrowRequest;
import com.constellio.app.modules.tasks.model.wrappers.request.ExtensionRequest;
import com.constellio.app.modules.tasks.model.wrappers.request.ReactivationRequest;
import com.constellio.app.modules.tasks.model.wrappers.request.RequestTask;
import com.constellio.app.modules.tasks.model.wrappers.request.ReturnRequest;
import com.constellio.app.modules.tasks.services.TasksSchemasRecordsServices;
import com.constellio.app.services.factories.AppLayerFactory;
import com.constellio.app.services.menu.MenuItemAction;
import com.constellio.app.services.menu.MenuItemActionState;
import com.constellio.app.ui.framework.buttons.BaseButton;
import com.constellio.app.ui.framework.buttons.WindowButton;
import com.constellio.app.ui.framework.components.BaseForm;
import com.constellio.app.ui.framework.components.fields.number.BaseIntegerField;
import com.constellio.app.ui.pages.base.BaseView;
import com.constellio.model.entities.records.Record;
import com.constellio.model.entities.records.RecordUpdateOptions;
import com.constellio.model.entities.records.Transaction;
import com.constellio.model.entities.records.wrappers.User;
import com.constellio.model.entities.schemas.Metadata;
import com.constellio.model.entities.schemas.MetadataSchemaType;
import com.constellio.model.entities.schemas.Schemas;
import com.constellio.model.frameworks.validation.ValidationException;
import com.constellio.model.services.factories.ModelLayerFactory;
import com.constellio.model.services.records.RecordServicesException;
import com.constellio.model.services.search.SearchServices;
import com.constellio.model.services.search.query.logical.LogicalSearchQueryOperators;
import com.constellio.model.services.search.query.logical.condition.LogicalSearchCondition;
import com.vaadin.data.fieldgroup.PropertyId;
import com.vaadin.ui.Component;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.InlineDateField;
import com.vaadin.ui.Label;
import com.vaadin.ui.TextArea;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.themes.ValoTheme;
import org.joda.time.LocalDate;

import java.util.Collections;
import java.util.Date;
import java.util.List;

import static com.constellio.app.modules.rm.extensions.app.RMMenuItemActionsRequestTaskExtension.RequestTypeMenuItem.REACTIVATION_BUTTON;
import static com.constellio.app.modules.rm.extensions.app.RMMenuItemActionsRequestTaskExtension.RequestTypeMenuItem.REQUEST_BORROW_BUTTON;
import static com.constellio.app.modules.rm.extensions.app.RMMenuItemActionsRequestTaskExtension.RequestTypeMenuItem.REQUEST_BORROW_EXTENSION_BUTTON;
import static com.constellio.app.modules.rm.extensions.app.RMMenuItemActionsRequestTaskExtension.RequestTypeMenuItem.RETURN_REQUEST_BUTTON;
import static com.constellio.app.ui.i18n.i18n.$;
import static com.constellio.model.services.search.query.logical.LogicalSearchQueryOperators.from;

public class RMMenuItemActionsRequestTaskExtension extends MenuItemActionsExtension {
	enum RequestType {
		EXTENSION, BORROW, REACTIVATION
	}

	enum RequestTypeMenuItem {
		REQUEST_BORROW_BUTTON, REQUEST_BORROW_EXTENSION_BUTTON, REACTIVATION_BUTTON, RETURN_REQUEST_BUTTON
	}

	private RMSchemasRecordsServices rm;
	private String collection;
	private AppLayerFactory appLayerFactory;
	private ModelLayerFactory modelLayerFactory;
	private BorrowingServices borrowingServices;
	private DecommissioningService decommissioningService;
	private TasksSchemasRecordsServices task;

	public RMMenuItemActionsRequestTaskExtension(String collection, AppLayerFactory appLayerFactory) {
		this.rm = new RMSchemasRecordsServices(collection, appLayerFactory);
		this.collection = collection;
		this.appLayerFactory = appLayerFactory;
		this.modelLayerFactory = appLayerFactory.getModelLayerFactory();
		this.borrowingServices = new BorrowingServices(collection, modelLayerFactory);
		this.decommissioningService = new DecommissioningService(collection, appLayerFactory);
		this.task = new TasksSchemasRecordsServices(collection, appLayerFactory);
	}

	@Override
	public void addMenuItemActionsForRecord(MenuItemActionExtensionAddMenuItemActionsForRecordParams params) {
		Record record = params.getRecord();
		User user = params.getBehaviorParams().getUser();

		if (params.getRecord().isOfSchemaType(ContainerRecord.SCHEMA_TYPE)) {

			Folder folder = getFolderOrNull(record);

			if (folder == null || folder.getArchivisticStatus() != FolderStatus.INACTIVE_DESTROYED) {
				boolean isBorrowRequestActionPossible = isBorrowRequestActionPossible(record, user);

				params.getMenuItemActions().add(MenuItemAction.builder()
						.type(REQUEST_BORROW_BUTTON.name())
						.state(toState(isBorrowRequestActionPossible))
						.caption($("RMRequestTaskButtonExtension.borrowRequest"))
						.command((ids) -> borrowRequest(params))
						.recordsLimit(1)
						.group(1)
						.priority(3000)
						.build());

				boolean isRequestBorrowExtensionActionPossible = isExtensionRequestActionPossible(record, user);

				params.getMenuItemActions().add(MenuItemAction.builder()
						.type(REQUEST_BORROW_EXTENSION_BUTTON.name())
						.state(toState(isRequestBorrowExtensionActionPossible))
						.caption($("RMRequestTaskButtonExtension.borrowExtensionRequest"))
						.command((ids) -> borrowExtensionRequested(params))
						.recordsLimit(1)
						.group(1)
						.priority(3002)
						.build());

				boolean isReactivationActionPossible = isReactivationRequestActionPossible(record, user);

				params.getMenuItemActions().add(MenuItemAction.builder()
						.type(REACTIVATION_BUTTON.name())
						.state(toState(isReactivationActionPossible))
						.caption($("RMRequestTaskButtonExtension.reactivationRequest"))
						.command((ids) -> reactivationRequested(params))
						.recordsLimit(1)
						.group(1)
						.priority(3004)
						.build());
			}

			String returnConfirmMessage = params.getBehaviorParams().getView() instanceof DisplayContainerViewImpl ?
										  $("DisplayFolderView.confirmReturnContainerMessage") :
										  $("DisplayFolderView.confirmReturnMessage");

			boolean isReturnRequestActionPossible =
					isReturnRequestActionPossible(record, user);

			params.getMenuItemActions().add(MenuItemAction.builder()
					.type(RETURN_REQUEST_BUTTON.name())
					.state(toState(isReturnRequestActionPossible))
					.caption($("RMRequestTaskButtonExtension.returnRequest"))
					.confirmMessage(returnConfirmMessage)
					.command((ids) -> returnRequest(params))
					.recordsLimit(1)
					.group(-1)
					.priority(3006)
					.build());
		}
	}

	@Override
	public MenuItemActionState getActionStateForRecord(MenuItemActionExtensionGetActionStateForRecordParams params) {
		String actionType = params.getMenuItemActionType();
		Record record = params.getRecord();
		User user = params.getBehaviorParams().getUser();

		if (actionType.equals(REQUEST_BORROW_BUTTON.name())) {
			return toState(isBorrowRequestActionPossible(record, user));
		} else if (actionType.equals(REQUEST_BORROW_EXTENSION_BUTTON.name())) {
			return toState(isExtensionRequestActionPossible(record, user));
		} else if (actionType.equals(REACTIVATION_BUTTON.name())) {
			return toState(isReactivationRequestActionPossible(record, user));
		} else if (actionType.equals(RETURN_REQUEST_BUTTON.name())) {
			return toState(isReactivationRequestActionPossible(record, user));
		}

		return null;
	}

	public void reactivationRequested(MenuItemActionExtensionAddMenuItemActionsForRecordParams params) {
		WindowButton reactivationRequestButton = new WindowButton($("RMRequestTaskButtonExtension.reactivationRequest"),
				$("RMRequestTaskButtonExtension.reactivationRequest")) {
			@PropertyId("value")
			private InlineDateField datefield;

			@Override
			protected Component buildWindowContent() {
				Label dateLabel = new Label($("DisplayFolderView.confirmReactivationMessage"));

				datefield = new InlineDateField();
				List<BaseForm.FieldAndPropertyId> fields = Collections
						.singletonList(new BaseForm.FieldAndPropertyId(datefield, "value"));
				Request req = new Request(new Date(), RequestType.REACTIVATION);
				BaseForm form = new BaseForm<Request>(req, this, datefield) {

					@Override
					protected void saveButtonClick(Request viewObject)
							throws ValidationException {
						viewObject.setValue(datefield.getValue());
						reactivationRequested(params, viewObject);
						getWindow().close();
					}

					@Override
					protected void cancelButtonClick(Request viewObject) {
						getWindow().close();
					}
				};
				return form;
			}

			public void setDatefield(InlineDateField datefield) {
				this.datefield = datefield;
			}

			public InlineDateField getDatefield() {
				return this.datefield;
			}
		};
		reactivationRequestButton.setId(ReactivationRequest.SCHEMA_NAME);
		reactivationRequestButton.click();
	}

	private void reactivationRequested(MenuItemActionExtensionAddMenuItemActionsForRecordParams params, Request req) {
		Folder folder = getFolderOrNull(params.getRecord());
		ContainerRecord container = getContainerRecordOrNull(params.getRecord(), folder);
		User currentUser = params.getBehaviorParams().getUser();
		BaseView view = params.getBehaviorParams().getView();
		LocalDate localDate = new LocalDate(req.getValue());

		try {
			long recordResult = getNumberOfRequestFromUser(ReactivationRequest.FULL_SCHEMA_NAME, currentUser, folder, container);

			if (recordResult > 0) {
				view.showErrorMessage($("RMRequestTaskButtonExtension.taskAlreadyCreated"));
			} else {
				if (folder != null) {
					String folderId = folder.getId();
					Task reactivationRequest = task
							.newReactivateFolderRequestTask(currentUser.getId(), getAssigneesForFolder(folderId), folderId, folder.getTitle(),
									localDate);
					addRecordWithUserSafeOption(reactivationRequest);
				} else if (container != null) {
					String containerId = container.getId();
					Task reactivationRequest = task
							.newReactivationContainerRequestTask(currentUser.getId(), getAssigneesForContainer(containerId), containerId,
									container.getTitle(), localDate);
					addRecordWithUserSafeOption(reactivationRequest);
				} else {
					view.showErrorMessage($("RMRequestTaskButtonExtension.errorWhileCreatingTask"));
				}
				view.showMessage($("RMRequestTaskButtonExtension.reactivationSuccess"));
			}
		} catch (RecordServicesException e) {
			e.printStackTrace();
			view.showErrorMessage($("RMRequestTaskButtonExtension.errorWhileCreatingTask"));
		}
	}

	public void borrowExtensionRequested(MenuItemActionExtensionAddMenuItemActionsForRecordParams params) {
		WindowButton borrowExtensionRequestButton = new WindowButton($("RMRequestTaskButtonExtension.borrowExtensionRequest"),
				$("RMRequestTaskButtonExtension.borrowExtensionRequestTitle")) {
			@PropertyId("value")
			private InlineDateField datefield;

			@Override
			protected Component buildWindowContent() {
				Label dateLabel = new Label($("DisplayFolderView.chooseDateToExtends"));

				datefield = new InlineDateField();
				List<BaseForm.FieldAndPropertyId> fields = Collections
						.singletonList(new BaseForm.FieldAndPropertyId(datefield, "value"));
				Request req = new Request(new Date(), RequestType.EXTENSION);
				BaseForm form = new BaseForm<Request>(req, this, datefield) {

					@Override
					protected void saveButtonClick(Request viewObject)
							throws ValidationException {
						viewObject.setValue(datefield.getValue());
						borrowExtensionRequested(params, viewObject);
						getWindow().close();
					}

					@Override
					protected void cancelButtonClick(Request viewObject) {
						getWindow().close();
					}
				};
				return form;
			}

			public void setDatefield(InlineDateField datefield) {
				this.datefield = datefield;
			}

			public InlineDateField getDatefield() {
				return this.datefield;
			}
		};
		borrowExtensionRequestButton.setId(ExtensionRequest.SCHEMA_NAME);
		borrowExtensionRequestButton.click();
	}

	public void borrowExtensionRequested(MenuItemActionExtensionAddMenuItemActionsForRecordParams params, Request req) {
		Folder folder = getFolderOrNull(params.getRecord());
		ContainerRecord container = getContainerRecordOrNull(params.getRecord(), folder);
		User currentUser = params.getBehaviorParams().getUser();
		BaseView view = params.getBehaviorParams().getView();

		try {
			long recordResult = getNumberOfRequestFromUser(ExtensionRequest.FULL_SCHEMA_NAME, currentUser, folder, container);

			if (recordResult > 0) {
				view.showErrorMessage($("RMRequestTaskButtonExtension.taskAlreadyCreated"));
			} else {
				if (folder != null) {
					String folderId = folder.getId();
					Task borrowExtensionRequest = task
							.newBorrowFolderExtensionRequestTask(currentUser.getId(), getAssigneesForFolder(folderId), folderId,
									folder.getTitle(), new LocalDate(req.getValue()));
					addRecordWithUserSafeOption(borrowExtensionRequest);
				} else if (container != null) {
					String containerId = container.getId();
					Task borrowExtensionRequest = task
							.newBorrowContainerExtensionRequestTask(currentUser.getId(), getAssigneesForContainer(containerId), containerId,
									container.getTitle(), new LocalDate(req.getValue()));
					addRecordWithUserSafeOption(borrowExtensionRequest);
				} else {
					view.showErrorMessage($("RMRequestTaskButtonExtension.errorWhileCreatingTask"));
				}
				view.showMessage($("RMRequestTaskButtonExtension.borrowExtensionSuccess"));
			}
		} catch (RecordServicesException e) {
			e.printStackTrace();
			view.showErrorMessage($("RMRequestTaskButtonExtension.errorWhileCreatingTask"));
		}
	}


	static public class Request {

		@PropertyId("value")
		public Object value;

		@PropertyId("type")
		public RequestType type;

		public Request() {

		}

		public Request(Object value, RequestType requestType) {
			this.type = requestType;
			this.value = value;
		}

		public Object getValue() {
			return value;
		}

		public Request setValue(Object value) {
			this.value = value;
			return this;
		}

		public RequestType getType() {
			return type;
		}

		public Request setType(RequestType type) {
			this.type = type;
			return this;
		}
	}

	public void returnRequest(MenuItemActionExtensionAddMenuItemActionsForRecordParams params) {
		Folder folder = getFolderOrNull(params.getRecord());
		ContainerRecord container = getContainerRecordOrNull(params.getRecord(), folder);
		User currentUser = params.getBehaviorParams().getUser();
		BaseView view = params.getBehaviorParams().getView();

		try {

			long recordResult = getNumberOfRequestFromUser(ReturnRequest.FULL_SCHEMA_NAME, currentUser, folder, container);

			if (recordResult > 0) {
				view.showErrorMessage($("RMRequestTaskButtonExtension.taskAlreadyCreated"));
			} else if (folder != null) {
				String folderId = folder.getId();
				Task returnRequest = task
						.newReturnFolderRequestTask(currentUser.getId(), getAssigneesForFolder(folderId), folderId, folder.getTitle());
				addRecordWithUserSafeOption(returnRequest);
				view.showMessage($("RMRequestTaskButtonExtension.returnSuccess"));
			} else if (container != null) {
				String containerId = container.getId();
				Task returnRequest = task
						.newReturnContainerRequestTask(currentUser.getId(), getAssigneesForContainer(containerId), containerId,
								container.getTitle());
				addRecordWithUserSafeOption(returnRequest);
				view.showMessage($("RMRequestTaskButtonExtension.returnSuccess"));
			} else {
				view.showErrorMessage($("RMRequestTaskButtonExtension.errorWhileCreatingTask"));
			}

		} catch (RecordServicesException e) {
			e.printStackTrace();
			view.showErrorMessage($("RMRequestTaskButtonExtension.errorWhileCreatingTask"));
		}
	}

	public boolean isBorrowRequestActionPossible(Record record, User user) {
		Folder folder = getFolderOrNull(record);

		if (folder != null) {
			ContainerRecord containerRecord = null;
			if (folder.getContainer() != null) {
				containerRecord = rm.getContainerRecord(folder.getContainer());
			}

			return isFolderBorrowable(folder, containerRecord, user, collection);
		} else {
			ContainerRecord containerRecord = rm.wrapContainerRecord(record);

			return isContainerBorrowable(containerRecord, user);
		}
	}

	public void borrowRequest(MenuItemActionExtensionAddMenuItemActionsForRecordParams params) {
		WindowButton borrowRequestButton = new WindowButton($("RMRequestTaskButtonExtension.borrowRequest"),
				$("RMRequestTaskButtonExtension.requestBorrowButtonTitle")) {
			@Override
			protected Component buildWindowContent() {
				getWindow().setHeight("250px");
				Folder folder = getFolderOrNull(params.getRecord());
				ContainerRecord container = getContainerRecordOrNull(params.getRecord(), folder);
				User currentUser = params.getBehaviorParams().getUser();
				VerticalLayout mainLayout = new VerticalLayout();
				final BaseIntegerField borrowDurationField = new BaseIntegerField(
						$("RMRequestTaskButtonExtension.borrowDuration"));

				borrowDurationField.setValue(String.valueOf(
						new RMConfigs(modelLayerFactory.getSystemConfigurationsManager()).getBorrowingDurationDays()));
				HorizontalLayout buttonLayout = new HorizontalLayout();

				BaseButton borrowFolderButton = new BaseButton($("RMRequestTaskButtonExtension.confirmBorrowFolder")) {
					@Override
					protected void buttonClick(ClickEvent event) {
						borrowRequest(params, false, borrowDurationField.getValue());
						getWindow().close();
					}

					@Override
					public boolean isVisible() {
						return isFolderBorrowable(folder, container, currentUser, params.getBehaviorParams().getView().getCollection());
					}
				};
				borrowFolderButton.setStyleName(ValoTheme.BUTTON_PRIMARY);
				BaseButton borrowContainerButton = new BaseButton($("RMRequestTaskButtonExtension.confirmBorrowContainer")) {
					@Override
					protected void buttonClick(ClickEvent event) {
						borrowRequest(params, true, borrowDurationField.getValue());
						getWindow().close();
					}

					@Override
					public boolean isVisible() {
						return isContainerBorrowable(container, currentUser);
					}
				};
				borrowContainerButton.setStyleName(ValoTheme.BUTTON_PRIMARY);
				BaseButton cancelButton = new BaseButton($("cancel")) {
					@Override
					protected void buttonClick(ClickEvent event) {
						getWindow().close();
					}
				};

				TextArea messageField = new TextArea();
				messageField.setHeight("50%");
				messageField.setWidth("100%");
				messageField.addStyleName(ValoTheme.TEXTAREA_BORDERLESS);
				if (borrowFolderButton.isVisible() && borrowContainerButton.isVisible()) {
					messageField.setValue($("RMRequestTaskButtonExtension.requestBorrowFolderOrContainerMessage"));
				} else if (borrowFolderButton.isVisible()) {
					messageField.setValue($("RMRequestTaskButtonExtension.requestBorrowFolderMessage"));
				} else {
					messageField.setValue($("RMRequestTaskButtonExtension.requestBorrowContainerMessage"));
				}
				messageField.setReadOnly(true);

				buttonLayout.setSpacing(true);
				buttonLayout.addComponents(borrowContainerButton, borrowFolderButton, cancelButton);

				mainLayout.setHeight("100%");
				mainLayout.setWidth("100%");
				mainLayout.setSpacing(true);
				mainLayout.addComponents(messageField, borrowDurationField, buttonLayout);

				return mainLayout;
			}
		};

		borrowRequestButton.click();
	}

	public void borrowRequest(MenuItemActionExtensionAddMenuItemActionsForRecordParams params, boolean isContainer,
							  String inputForNumberOfDays) {
		Folder folder = getFolderOrNull(params.getRecord());
		ContainerRecord container = getContainerRecordOrNull(params.getRecord(), folder);
		User currentUser = params.getBehaviorParams().getUser();
		BaseView view = params.getBehaviorParams().getView();

		int numberOfDays = 1;
		if (inputForNumberOfDays != null && inputForNumberOfDays.matches("^-?\\d+$")) {
			numberOfDays = Integer.parseInt(inputForNumberOfDays);

			if (numberOfDays <= 0) {
				params.getBehaviorParams().getView().showErrorMessage($("RMRequestTaskButtonExtension.invalidBorrowDuration"));
				return;
			}
		} else {
			view.showErrorMessage($("RMRequestTaskButtonExtension.invalidBorrowDuration"));
			return;
		}
		try {
			long recordResult = getNumberOfRequestFromUser(BorrowRequest.FULL_SCHEMA_NAME, currentUser, folder, container);

			if (recordResult > 0) {
				view.showErrorMessage($("RMRequestTaskButtonExtension.taskAlreadyCreated"));
			} else {
				Task borrowRequest;
				if (isContainer) {
					String recordId = container.getId();
					borrowRequest = task
							.newBorrowContainerRequestTask(currentUser.getId(), getAssigneesForContainer(recordId), recordId, numberOfDays,
									container.getTitle());
				} else {
					String recordId = folder.getId();
					borrowRequest = task
							.newBorrowFolderRequestTask(currentUser.getId(), getAssigneesForFolder(recordId), recordId, numberOfDays,
									folder.getTitle());
				}
				addRecordWithUserSafeOption(borrowRequest);
				view.showMessage($("RMRequestTaskButtonExtension.borrowSuccess"));
			}

		} catch (RecordServicesException e) {
			e.printStackTrace();
			view.showErrorMessage($("RMRequestTaskButtonExtension.errorWhileCreatingTask"));
		}
	}

	private void addRecordWithUserSafeOption(Task task) throws RecordServicesException {
		Transaction transaction = new Transaction();
		transaction.setOptions(RecordUpdateOptions.userModificationsSafeOptions());
		transaction.add(task);
		modelLayerFactory.newRecordServices().execute(transaction);
	}

	private List<String> getAssigneesForFolder(String recordId) {
		return modelLayerFactory.newAuthorizationsServices()
				.getUserIdsWithPermissionOnRecord(RMPermissionsTo.MANAGE_REQUEST_ON_FOLDER,
						modelLayerFactory.newRecordServices().getDocumentById(recordId));
	}

	private List<String> getAssigneesForContainer(String recordId) {
		return modelLayerFactory.newAuthorizationsServices()
				.getUserIdsWithPermissionOnRecord(RMPermissionsTo.MANAGE_REQUEST_ON_CONTAINER,
						modelLayerFactory.newRecordServices().getDocumentById(recordId));
	}

	public long getNumberOfRequestFromUser(String fullSchemaName, User currentUser, Folder folder,
										   ContainerRecord containerRecord) {
		SearchServices searchServices = modelLayerFactory.newSearchServices();

		TasksSchemasRecordsServices tasksSchemasRecordsServices = new TasksSchemasRecordsServices(collection, appLayerFactory);
		MetadataSchemaType taskSchemaType = tasksSchemasRecordsServices.taskSchemaType();
		Metadata metadataLinkedFolder = taskSchemaType.getAllMetadatas().getMetadataWithLocalCode(Task.LINKED_FOLDERS);
		Metadata metadataLinkedContainer = taskSchemaType.getAllMetadatas().getMetadataWithLocalCode(Task.LINKED_CONTAINERS);
		Metadata metadataStatus = taskSchemaType.getAllMetadatas().getMetadataWithLocalCode(Task.STATUS_TYPE);
		Metadata metadataApplicant = taskSchemaType.getAllMetadatas().getMetadataWithLocalCode(RequestTask.APPLICANT);
		LogicalSearchCondition logicalSearchCondition;

		if (folder != null) {
			logicalSearchCondition = from(taskSchemaType).where(Schemas.SCHEMA).isEqualTo(fullSchemaName)
					.andWhere(metadataLinkedFolder).isEqualTo(folder).andWhere(metadataStatus).isEqualTo(TaskStatusType.STANDBY).andWhere(metadataApplicant).isEqualTo(currentUser.getId());
		} else {
			logicalSearchCondition = from(taskSchemaType).where(Schemas.SCHEMA).isEqualTo(fullSchemaName)
					.andWhere(metadataLinkedContainer).isEqualTo(containerRecord).andWhere(metadataStatus).isEqualTo(TaskStatusType.STANDBY).andWhere(metadataApplicant).isEqualTo(currentUser.getId());
		}

		return searchServices.getResultsCount(logicalSearchCondition);
	}


	private Folder getFolderOrNull(Record record) {

		if (record.getTypeCode().equals(Folder.SCHEMA_TYPE)) {
			return rm.wrapFolder(record);
		}

		return null;
	}

	private ContainerRecord getContainerRecordOrNull(Record record, Folder folder) {
		if (folder == null) {
			return rm.wrapContainerRecord(record);
		} else {
			if (folder.getContainer() != null) {
				return rm.getContainerRecord(folder.getContainer());
			} else {
				return null;
			}
		}
	}

	private boolean isFolderBorrowable(Folder folder, ContainerRecord container, User currentUser, String collection) {
		if (folder != null) {
			try {
				this.borrowingServices.validateCanBorrow(currentUser, folder, LocalDate.now());
			} catch (Exception e) {
				return false;
			}
		}
		RMModuleExtensions rmModuleExtensions = appLayerFactory.getExtensions().forCollection(collection).forModule(ConstellioRMModule.ID);
		;
		return folder != null && currentUser.hasAll(RMPermissionsTo.BORROW_FOLDER, RMPermissionsTo.BORROWING_REQUEST_ON_FOLDER)
				.on(folder)
			   && !(container != null && Boolean.TRUE.equals(container.getBorrowed())) && rmModuleExtensions.isBorrowingActionPossibleOnFolder(folder, currentUser);
	}

	private boolean isContainerBorrowable(ContainerRecord container, User currentUser) {
		if (container != null) {
			try {
				this.borrowingServices.validateCanBorrow(currentUser, container, LocalDate.now());
			} catch (Exception e) {
				return false;
			}
		}
		return container != null && currentUser
				.hasAll(RMPermissionsTo.BORROW_CONTAINER, RMPermissionsTo.BORROWING_REQUEST_ON_CONTAINER).on(container);
	}

	public boolean isReturnRequestActionPossible(Record record, User user) {
		Folder folderOrNull = getFolderOrNull(record);
		return isPrincipalRecordReturnable(folderOrNull, getContainerRecordOrNull(record, folderOrNull), user);
	}

	private boolean isPrincipalRecordReturnable(Folder folder, ContainerRecord container, User currentUser) {
		if (folder != null) {
			return folder != null && Boolean.TRUE.equals(folder.getBorrowed()) && currentUser.getId()
					.equals(folder.getBorrowUserEntered());
		} else {
			return container != null && Boolean.TRUE.equals(container.getBorrowed()) && currentUser.getId()
					.equals(container.getBorrower());
		}
	}

	public boolean isReactivationRequestActionPossible(Record record, User user) {
		Folder folder = getFolderOrNull(record);

		if (folder != null) {
			return isFolderReactivable(folder, user);
		} else {
			return isContainerReactivable(getContainerRecordOrNull(record, folder), user);
		}
	}

	private boolean isFolderReactivable(Folder folder, User currentUser) {
		return decommissioningService.isFolderReactivable(folder, currentUser);
	}

	private boolean isContainerReactivable(ContainerRecord containerRecord, User currentUser) {
		List<Folder> folders = rm.searchFolders(
				LogicalSearchQueryOperators.from(rm.folder.schemaType()).where(rm.folder.container())
						.isEqualTo(containerRecord.getId()));
		if (folders != null) {
			for (Folder folder : folders) {
				if (isFolderReactivable(folder, currentUser)) {
					return true;
				}
			}
		}
		return false;
	}

	public boolean isExtensionRequestActionPossible(Record record, User user) {
		return isReturnRequestActionPossible(record, user);
	}
}
