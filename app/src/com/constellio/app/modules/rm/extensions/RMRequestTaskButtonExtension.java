package com.constellio.app.modules.rm.extensions;

import com.constellio.app.api.extensions.PagesComponentsExtension;
import com.constellio.app.api.extensions.params.DecorateMainComponentAfterInitExtensionParams;
import com.constellio.app.modules.rm.ConstellioRMModule;
import com.constellio.app.modules.rm.RMConfigs;
import com.constellio.app.modules.rm.constants.RMPermissionsTo;
import com.constellio.app.modules.rm.extensions.api.RMModuleExtensions;
import com.constellio.app.modules.rm.services.RMSchemasRecordsServices;
import com.constellio.app.modules.rm.services.borrowingServices.BorrowingServices;
import com.constellio.app.modules.rm.services.decommissioning.DecommissioningService;
import com.constellio.app.modules.rm.ui.pages.containers.DisplayContainerViewImpl;
import com.constellio.app.modules.rm.ui.pages.folder.DisplayFolderViewImpl;
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
import com.constellio.app.ui.framework.buttons.BaseButton;
import com.constellio.app.ui.framework.buttons.ConfirmDialogButton;
import com.constellio.app.ui.framework.buttons.WindowButton;
import com.constellio.app.ui.framework.components.BaseForm;
import com.constellio.app.ui.framework.components.fields.number.BaseIntegerField;
import com.constellio.app.ui.pages.base.BasePresenterUtils;
import com.constellio.app.ui.pages.base.BaseView;
import com.constellio.app.ui.pages.base.BaseViewImpl;
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
import com.vaadin.ui.Button;
import com.vaadin.ui.Component;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.InlineDateField;
import com.vaadin.ui.Label;
import com.vaadin.ui.TextArea;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.themes.ValoTheme;
import org.joda.time.LocalDate;
import org.vaadin.dialogs.ConfirmDialog;

import java.util.Collections;
import java.util.Date;
import java.util.List;

import static com.constellio.app.ui.i18n.i18n.$;
import static com.constellio.model.services.search.query.logical.LogicalSearchQueryOperators.from;

/**
 * Created by Constellio on 2017-03-16.
 */
public class RMRequestTaskButtonExtension extends PagesComponentsExtension {

	enum RequestType {
		EXTENSION, BORROW, REACTIVATION
	}

	String collection;
	AppLayerFactory appLayerFactory;
	ModelLayerFactory modelLayerFactory;
	TasksSchemasRecordsServices taskSchemas;
	RMSchemasRecordsServices rmSchemas;
	BorrowingServices borrowingServices;
	DecommissioningService decommissioningService;
	SearchServices searchServices;

	public RMRequestTaskButtonExtension(String collection, AppLayerFactory appLayerFactory) {
		this.collection = collection;
		this.appLayerFactory = appLayerFactory;
		this.modelLayerFactory = appLayerFactory.getModelLayerFactory();
		this.taskSchemas = new TasksSchemasRecordsServices(collection, appLayerFactory);
		this.rmSchemas = new RMSchemasRecordsServices(collection, appLayerFactory);
		this.borrowingServices = new BorrowingServices(collection, modelLayerFactory);
		this.decommissioningService = new DecommissioningService(collection, appLayerFactory);
		this.searchServices = modelLayerFactory.newSearchServices();
	}

	@Override
	public void decorateMainComponentAfterViewAssembledOnViewEntered(
			DecorateMainComponentAfterInitExtensionParams params) {
		super.decorateMainComponentAfterViewAssembledOnViewEntered(params);
		Component mainComponent = params.getMainComponent();
		Folder folder;
		ContainerRecord container;
		User currentUser;
		if (mainComponent instanceof DisplayFolderViewImpl) {
			DisplayFolderViewImpl view = (DisplayFolderViewImpl) mainComponent;
			folder = rmSchemas.getFolderSummary(view.getSummaryRecord().getId());
			if (folder.getContainer() != null) {
				container = rmSchemas.getContainerRecord(folder.getContainer());
			} else {
				container = null;
			}
			currentUser = getCurrentUser(view);
			adjustButtons(view, folder, container, currentUser);
		} else if (mainComponent instanceof DisplayContainerViewImpl) {
			DisplayContainerViewImpl view = (DisplayContainerViewImpl) mainComponent;
			folder = null;
			container = rmSchemas.getContainerRecord(view.getPresenter().getContainerId());
			currentUser = getCurrentUser(view);
			adjustButtons(view, folder, container, currentUser);
		}
	}

	private String getCurrentCollection(BaseView view) {
		return view.getCollection();
	}

	public void adjustButtons(BaseViewImpl view, Folder folder, ContainerRecord containerRecord, User currentUser) {
		List<Button> actionMenuButtons = view.getActionMenuButtons();
		String collection = getCurrentCollection(view);
		for (Button button : actionMenuButtons) {
			if (button.getId() != null) {
				switch (button.getId()) {
					case BorrowRequest.SCHEMA_NAME:
						button.setVisible(isPrincipalRecordBorrowable(folder, containerRecord, currentUser, collection));
						break;
					case ReturnRequest.SCHEMA_NAME:
						button.setVisible(isPrincipalRecordReturnable(folder, containerRecord, currentUser));
						break;
					case ReactivationRequest.SCHEMA_NAME:
						button.setVisible(isPrincipalRecordReativable(folder, containerRecord, currentUser));
						break;
					case ExtensionRequest.SCHEMA_NAME:
						button.setVisible(isPrincipalRecordReturnable(folder, containerRecord, currentUser));
						break;
				}
			}
		}
	}

	private boolean isPrincipalRecordReativable(Folder folder, ContainerRecord containerRecord, User currentUser) {
		if (folder != null) {
			return isFolderReactivable(folder, currentUser);
		} else {
			return isContainerReactivable(containerRecord, currentUser);
		}
	}

	private boolean isContainerReactivable(ContainerRecord containerRecord, User currentUser) {
		List<Folder> folders = rmSchemas.searchFolders(
				LogicalSearchQueryOperators.from(rmSchemas.folder.schemaType()).where(rmSchemas.folder.container())
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

	private boolean isPrincipalRecordBorrowable(Folder folder, ContainerRecord container, User currentUser,
												String collection) {
		if (folder != null) {
			return isFolderBorrowable(folder, container, currentUser, collection);
		} else {
			return isContainerBorrowable(container, currentUser);
		}
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

	private boolean isFolderReactivable(Folder folder, User currentUser) {
		return decommissioningService.isFolderReactivable(folder, currentUser);
	}

	@Override
	public void decorateMainComponentBeforeViewInstanciated(DecorateMainComponentAfterInitExtensionParams params) {

	}

	@Override
	public void decorateMainComponentBeforeViewAssembledOnViewEntered(
			DecorateMainComponentAfterInitExtensionParams params) {
	}

	private User getCurrentUser(BaseView view) {
		BasePresenterUtils basePresenterUtils = new BasePresenterUtils(view.getConstellioFactories(), view.getSessionContext());
		return basePresenterUtils.getCurrentUser();
	}

	private Button buildRequestBorrowButton(final BaseViewImpl view) {
		WindowButton borrowRequestButton = new WindowButton($("RMRequestTaskButtonExtension.borrowRequest"),
				$("RMRequestTaskButtonExtension.requestBorrowButtonTitle")) {
			@Override
			protected Component buildWindowContent() {
				getWindow().setHeight("250px");
				final Context context = buildContext(view);
				final Folder folder = context.getFolder();
				final ContainerRecord container = context.getContainer();
				final User currentUser = context.getCurrentUser();
				VerticalLayout mainLayout = new VerticalLayout();
				final BaseIntegerField borrowDurationField = new BaseIntegerField(
						$("RMRequestTaskButtonExtension.borrowDuration"));

				borrowDurationField.setValue(String.valueOf(
						new RMConfigs(modelLayerFactory.getSystemConfigurationsManager()).getBorrowingDurationDays()));
				HorizontalLayout buttonLayout = new HorizontalLayout();

				BaseButton borrowFolderButton = new BaseButton($("RMRequestTaskButtonExtension.confirmBorrowFolder")) {
					@Override
					protected void buttonClick(ClickEvent event) {
						borrowRequest(view, false, borrowDurationField.getValue());
						getWindow().close();
					}

					@Override
					public boolean isVisible() {
						return isFolderBorrowable(folder, container, currentUser, view.getCollection());
					}
				};
				borrowFolderButton.setStyleName(ValoTheme.BUTTON_PRIMARY);
				BaseButton borrowContainerButton = new BaseButton($("RMRequestTaskButtonExtension.confirmBorrowContainer")) {
					@Override
					protected void buttonClick(ClickEvent event) {
						borrowRequest(view, true, borrowDurationField.getValue());
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
		borrowRequestButton.setId(BorrowRequest.SCHEMA_NAME);
		return borrowRequestButton;
	}

	private Button buildRequestReturnButton(final BaseViewImpl view) {
		ConfirmDialogButton returnRequestButton = new ConfirmDialogButton($("RMRequestTaskButtonExtension.returnRequest")) {

			@Override
			protected String getConfirmDialogMessage() {
				if (view instanceof DisplayContainerViewImpl) {
					return $("DisplayFolderView.confirmReturnContainerMessage");
				} else {
					return $("DisplayFolderView.confirmReturnMessage");
				}
			}

			@Override
			protected void confirmButtonClick(ConfirmDialog dialog) {
				returnRequest(view);
			}
		};
		returnRequestButton.setId(ReturnRequest.SCHEMA_NAME);
		return returnRequestButton;
	}

	private Button buildRequestReactivationButton(final BaseViewImpl view) {
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
						reactivationRequested(view, viewObject);
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
		return reactivationRequestButton;
	}

	private Button buildRequestBorrowExtensionButton(final BaseViewImpl view) {
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
						borrowExtensionRequested(view, viewObject);
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
		return borrowExtensionRequestButton;
	}

	public void borrowRequest(BaseViewImpl view, boolean isContainer, String inputForNumberOfDays) {
		Context context = buildContext(view);
		Folder folder = context.getFolder();
		ContainerRecord container = context.getContainer();
		User currentUser = context.getCurrentUser();
		int numberOfDays = 1;
		if (inputForNumberOfDays != null && inputForNumberOfDays.matches("^-?\\d+$")) {
			numberOfDays = Integer.parseInt(inputForNumberOfDays);

			if (numberOfDays <= 0) {
				view.showErrorMessage($("RMRequestTaskButtonExtension.invalidBorrowDuration"));
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
					borrowRequest = taskSchemas
							.newBorrowContainerRequestTask(currentUser.getId(), getAssigneesForContainer(recordId), recordId, numberOfDays,
									container.getTitle());
				} else {
					String recordId = folder.getId();
					borrowRequest = taskSchemas
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

	public void returnRequest(BaseViewImpl view) {
		Context context = buildContext(view);
		Folder folder = context.getFolder();
		ContainerRecord container = context.getContainer();
		User currentUser = context.getCurrentUser();

		try {

			long recordResult = getNumberOfRequestFromUser(ReturnRequest.FULL_SCHEMA_NAME, currentUser, folder, container);

			if (recordResult > 0) {
				view.showErrorMessage($("RMRequestTaskButtonExtension.taskAlreadyCreated"));
			} else if (folder != null) {
				String folderId = folder.getId();
				Task returnRequest = taskSchemas
						.newReturnFolderRequestTask(currentUser.getId(), getAssigneesForFolder(folderId), folderId, folder.getTitle());
				addRecordWithUserSafeOption(returnRequest);
				view.showMessage($("RMRequestTaskButtonExtension.returnSuccess"));
			} else if (container != null) {
				String containerId = container.getId();
				Task returnRequest = taskSchemas
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

	private void addRecordWithUserSafeOption(Task task) throws RecordServicesException {
		Transaction transaction = new Transaction();
		transaction.setOptions(RecordUpdateOptions.userModificationsSafeOptions());
		transaction.add(task);
		modelLayerFactory.newRecordServices().execute(transaction);
	}

	public void reactivationRequested(BaseViewImpl view, Request req) {
		Context context = buildContext(view);
		Folder folder = context.getFolder();
		ContainerRecord container = context.getContainer();
		User currentUser = context.getCurrentUser();
		LocalDate localDate = new LocalDate(req.getValue());
		try {
			long recordResult = getNumberOfRequestFromUser(ReactivationRequest.FULL_SCHEMA_NAME, currentUser, folder, container);

			if (recordResult > 0) {
				view.showErrorMessage($("RMRequestTaskButtonExtension.taskAlreadyCreated"));
			} else {
				if (folder != null) {
					String folderId = folder.getId();
					Task reactivationRequest = taskSchemas
							.newReactivateFolderRequestTask(currentUser.getId(), getAssigneesForFolder(folderId), folderId, folder.getTitle(),
									localDate);
					addRecordWithUserSafeOption(reactivationRequest);
				} else if (container != null) {
					String containerId = container.getId();
					Task reactivationRequest = taskSchemas
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

	public void borrowExtensionRequested(BaseViewImpl view, Request req) {
		Context context = buildContext(view);
		Folder folder = context.getFolder();
		ContainerRecord container = context.getContainer();
		User currentUser = context.getCurrentUser();
		try {
			long recordResult = getNumberOfRequestFromUser(ExtensionRequest.FULL_SCHEMA_NAME, currentUser, folder, container);

			if (recordResult > 0) {
				view.showErrorMessage($("RMRequestTaskButtonExtension.taskAlreadyCreated"));
			} else {
				if (folder != null) {
					String folderId = folder.getId();
					Task borrowExtensionRequest = taskSchemas
							.newBorrowFolderExtensionRequestTask(currentUser.getId(), getAssigneesForFolder(folderId), folderId,
									folder.getTitle(), new LocalDate(req.getValue()));
					addRecordWithUserSafeOption(borrowExtensionRequest);
				} else if (container != null) {
					String containerId = container.getId();
					Task borrowExtensionRequest = taskSchemas
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

	public class Context {
		Folder folder;
		ContainerRecord container;
		User currentUser;

		public Context(Folder folder, ContainerRecord container, User currentUser) {
			this.folder = folder;
			this.container = container;
			this.currentUser = currentUser;
		}

		public Folder getFolder() {
			return folder;
		}

		public ContainerRecord getContainer() {
			return container;
		}

		public User getCurrentUser() {
			return currentUser;
		}
	}

	public Context buildContext(BaseViewImpl baseView) {
		Folder folder;
		ContainerRecord container;
		User currentUser;
		if (baseView instanceof DisplayFolderViewImpl) {
			DisplayFolderViewImpl view = (DisplayFolderViewImpl) baseView;
			folder = rmSchemas.getFolderSummary(view.getSummaryRecord().getId());
			if (folder.getContainer() != null) {
				container = rmSchemas.getContainerRecord(folder.getContainer());
			} else {
				container = null;
			}
			currentUser = getCurrentUser(view);

		} else if (baseView instanceof DisplayContainerViewImpl) {
			DisplayContainerViewImpl view = (DisplayContainerViewImpl) baseView;
			folder = null;
			container = rmSchemas.getContainerRecord(view.getPresenter().getContainerId());
			currentUser = getCurrentUser(view);
		} else {
			return null;
		}
		return new Context(folder, container, currentUser);
	}
}
