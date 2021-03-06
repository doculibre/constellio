package com.constellio.app.modules.rm.extensions.api;

import java.util.ArrayList;
import java.util.List;

import com.constellio.app.api.extensions.DocumentFolderBreadCrumbExtention;
import com.constellio.app.api.extensions.NavigateToFromAPageImportExtension;
import com.constellio.app.api.extensions.params.DocumentFolderBreadCrumbParams;
import com.constellio.app.api.extensions.params.NavigateToFromAPageParams;
import com.constellio.app.extensions.ModuleExtensions;
import com.constellio.app.modules.rm.extensions.ExternalLinkServicesExtension;
import com.constellio.app.modules.rm.extensions.api.CartExtensions.CartExtensionActionPossibleParams;
import com.constellio.app.modules.rm.extensions.api.ContainerRecordExtension.ContainerRecordExtensionActionPossibleParams;
import com.constellio.app.modules.rm.extensions.api.DocumentExtension.DocumentExtensionActionPossibleParams;
import com.constellio.app.modules.rm.extensions.api.FolderExtension.FolderExtensionActionPossibleParams;
import com.constellio.app.modules.rm.extensions.api.RMExternalLinkVOExtension.RMExternalLinkVOExtensionParams;
import com.constellio.app.modules.rm.extensions.api.StorageSpaceExtension.StorageSpaceExtensionActionPossibleParams;
import com.constellio.app.modules.rm.extensions.api.reports.RMReportBuilderFactories;
import com.constellio.app.modules.rm.extensions.ui.RMAddEditDocumentPageExtension;
import com.constellio.app.modules.rm.wrappers.Cart;
import com.constellio.app.modules.rm.wrappers.ContainerRecord;
import com.constellio.app.modules.rm.wrappers.Document;
import com.constellio.app.modules.rm.wrappers.Folder;
import com.constellio.app.modules.rm.wrappers.StorageSpace;
import com.constellio.app.modules.tasks.extensions.TaskAddEditTaskPresenterExtension;
import com.constellio.app.modules.tasks.extensions.TaskManagementPresenterExtension;
import com.constellio.app.modules.tasks.extensions.TaskPreCompletionExtention;
import com.constellio.app.modules.tasks.extensions.param.PromptUserParam;
import com.constellio.app.services.factories.AppLayerFactory;
import com.constellio.app.ui.entities.RecordVO;
import com.constellio.app.ui.framework.components.breadcrumb.BaseBreadcrumbTrail;
import com.constellio.data.frameworks.extensions.ExtensionBooleanResult;
import com.constellio.data.frameworks.extensions.ExtensionUtils;
import com.constellio.data.frameworks.extensions.ExtensionUtils.BooleanCaller;
import com.constellio.data.frameworks.extensions.VaultBehaviorsList;
import com.constellio.model.entities.records.wrappers.User;
import com.constellio.model.frameworks.validation.ValidationException;
import com.constellio.model.services.extensions.ModelLayerExtensions;

public class RMModuleExtensions implements ModuleExtensions {

	private RMReportBuilderFactories rmReportBuilderFactories;
	private VaultBehaviorsList<DecommissioningBuilderPresenterExtension> decommissioningBuilderPresenterExtensions;
	private DecommissioningListFolderTableExtension decommissioningListFolderTableExtension;
	private VaultBehaviorsList<DecommissioningListPresenterExtension> decommissioningListPresenterExtensions;
	private VaultBehaviorsList<ExternalLinkServicesExtension> externalLinkServicesExtensions;
	private VaultBehaviorsList<TaskManagementPresenterExtension> taskManagementPresenterExtensions;
	private VaultBehaviorsList<TaskAddEditTaskPresenterExtension> taskAddEditTaskPresenterExtension;
	private VaultBehaviorsList<DocumentExtension> documentExtensions;
	private VaultBehaviorsList<FolderExtension> folderExtensions;
	private VaultBehaviorsList<ContainerRecordExtension> containerRecordExtensions;
	private VaultBehaviorsList<StorageSpaceExtension> storageSpaceExtensions;
	private VaultBehaviorsList<AdvancedSearchPresenterExtension> advancedSearchPresenterExtensions;
	private VaultBehaviorsList<DocumentFolderBreadCrumbExtention> documentBreadcrumExtentions;
	private VaultBehaviorsList<NavigateToFromAPageImportExtension> navigateToFromAPageExtensions;
	private VaultBehaviorsList<TaskPreCompletionExtention> taskPreCompletionExetention;
	private VaultBehaviorsList<CartExtensions> cartExtensions;
	private VaultBehaviorsList<FilteredActionsExtension> filteredActionsExtension;
	private VaultBehaviorsList<RMExternalLinkVOExtension> externalLinkVOExtensions;
	private VaultBehaviorsList<RMAddEditDocumentPageExtension> addEditDocumentPageExtensions;
	private AgentExtension agentExtensions;

	private ModelLayerExtensions modelLayerExtensions;

	public RMModuleExtensions(AppLayerFactory appLayerFactory) {
		rmReportBuilderFactories = new RMReportBuilderFactories(appLayerFactory);
		decommissioningBuilderPresenterExtensions = new VaultBehaviorsList<>();
		decommissioningListPresenterExtensions = new VaultBehaviorsList<>();
		externalLinkServicesExtensions = new VaultBehaviorsList<>();
		taskManagementPresenterExtensions = new VaultBehaviorsList<>();
		taskAddEditTaskPresenterExtension = new VaultBehaviorsList<>();
		documentExtensions = new VaultBehaviorsList<>();
		folderExtensions = new VaultBehaviorsList<>();
		advancedSearchPresenterExtensions = new VaultBehaviorsList<>();
		documentBreadcrumExtentions = new VaultBehaviorsList<>();
		navigateToFromAPageExtensions = new VaultBehaviorsList<>();
		taskPreCompletionExetention = new VaultBehaviorsList<>();
		cartExtensions = new VaultBehaviorsList<>();
		modelLayerExtensions = appLayerFactory.getModelLayerFactory().getExtensions();
		containerRecordExtensions = new VaultBehaviorsList<>();
		storageSpaceExtensions = new VaultBehaviorsList<>();
		filteredActionsExtension = new VaultBehaviorsList<>();
		agentExtensions = new AgentExtension();
		externalLinkVOExtensions = new VaultBehaviorsList<>();
		addEditDocumentPageExtensions = new VaultBehaviorsList<>();
	}

	public List<String> getFilteredActionsForContainers() {
		List<String> filteredActions = new ArrayList<>();

		for (FilteredActionsExtension actionExtensions : filteredActionsExtension) {
			filteredActions.addAll(actionExtensions.getFilteredActionsForContainers());
		}

		return filteredActions;
	}

	public List<String> getFilteredActionsForFolders() {
		List<String> filteredActions = new ArrayList<>();

		for (FilteredActionsExtension actionExtensions : filteredActionsExtension) {
			filteredActions.addAll(actionExtensions.getFilteredActionsForFolders());
		}

		return filteredActions;
	}

	public RMReportBuilderFactories getReportBuilderFactories() {
		return rmReportBuilderFactories;
	}

	public VaultBehaviorsList<DecommissioningBuilderPresenterExtension> getDecommissioningBuilderPresenterExtensions() {
		return decommissioningBuilderPresenterExtensions;
	}

	public VaultBehaviorsList<TaskPreCompletionExtention> getTaskPreCompletionExetention() {
		return taskPreCompletionExetention;
	}

	public DecommissioningListFolderTableExtension getDecommissioningListFolderTableExtension() {
		return decommissioningListFolderTableExtension;
	}

	public void setDecommissioningListFolderTableExtension(
			DecommissioningListFolderTableExtension decommissioningListFolderTableExtension) {
		this.decommissioningListFolderTableExtension = decommissioningListFolderTableExtension;
	}

	public VaultBehaviorsList<FilteredActionsExtension> getFilteredActionsExtension() {
		return filteredActionsExtension;
	}

	public VaultBehaviorsList<NavigateToFromAPageImportExtension> getNavigateToFromAPageExtensions() {
		return this.navigateToFromAPageExtensions;
	}

	public VaultBehaviorsList<DecommissioningListPresenterExtension> getDecommissioningListPresenterExtensions() {
		return decommissioningListPresenterExtensions;
	}

	public VaultBehaviorsList<ExternalLinkServicesExtension> getExternalLinkServicesExtensions() {
		return externalLinkServicesExtensions;
	}

	public VaultBehaviorsList<TaskManagementPresenterExtension> getTaskManagementPresenterExtensions() {
		return taskManagementPresenterExtensions;
	}

	public VaultBehaviorsList<TaskAddEditTaskPresenterExtension> getTaskAddEditTaskPresenterExtension() {
		return taskAddEditTaskPresenterExtension;
	}

	public VaultBehaviorsList<CartExtensions> getCartExtensions() {
		return cartExtensions;
	}

	public VaultBehaviorsList<DocumentExtension> getDocumentExtensions() {
		return documentExtensions;
	}

	public VaultBehaviorsList<FolderExtension> getFolderExtensions() {
		return folderExtensions;
	}

	public VaultBehaviorsList<AdvancedSearchPresenterExtension> getAdvancedSearchPresenterExtensions() {
		return advancedSearchPresenterExtensions;
	}

	public VaultBehaviorsList<DocumentFolderBreadCrumbExtention> getDocumentBreadcrumExtentions() {
		return documentBreadcrumExtentions;
	}

	public VaultBehaviorsList<RMExternalLinkVOExtension> getExternalLinkVOExtensions() {
		return externalLinkVOExtensions;
	}

	public AgentExtension getAgentExtensions() {
		return agentExtensions;
	}
	
	public VaultBehaviorsList<RMAddEditDocumentPageExtension> getAddEditDocumentPageExtensions() {
		return addEditDocumentPageExtensions;
	}

	public boolean isCopyActionPossibleOnFolder(final Folder folder, final User user) {
		return folderExtensions.getBooleanValue(true,
				(behavior) -> behavior.isCopyActionPossible(new FolderExtensionActionPossibleParams(folder, user)));
	}

	public boolean isBatchDuplicateActionPossibleOnFolder(final Folder folder, final User user) {
		return folderExtensions.getBooleanValue(true,
				(behavior) -> behavior.isBatchDuplicateActionPossible(new FolderExtensionActionPossibleParams(folder, user)));
	}

	public boolean isDownloadActionPossibleOnFolder(final Folder folder, final User user) {
		return folderExtensions.getBooleanValue(true,
				(behavior) -> behavior.isDownloadActionPossible(new FolderExtensionActionPossibleParams(folder, user)));
	}

	public boolean isCreateSipActionPossibleOnFolder(final Folder folder, final User user) {
		return folderExtensions.getBooleanValue(true,
				(behavior) -> behavior.isCreateSipActionPossible(new FolderExtensionActionPossibleParams(folder, user)));
	}

	public boolean isPromptUser(PromptUserParam taskPreCompletionParam) throws ValidationException {
		for (TaskPreCompletionExtention taskPreCompletion : taskPreCompletionExetention) {
			if (taskPreCompletion.isPromptUser(taskPreCompletionParam)) {
				return true;
			}
		}
		return false;
	}

	public boolean isRenameActionPossibleOnCart(final Cart cart, final User user) {
		return cartExtensions.getBooleanValue(true,
				(behavior) -> behavior.isRenameActionPossible(new CartExtensionActionPossibleParams(cart, user)));
	}

	public boolean isPrepareEmailActionPossibleOnCart(final Cart cart, final User user) {
		return cartExtensions.getBooleanValue(true,
				(behavior) -> behavior.isPrepareEmailActionPossible(new CartExtensionActionPossibleParams(cart, user)));
	}

	public boolean isBatchDuplicateActionPossibleOnCart(final Cart cart, final User user) {
		return cartExtensions.getBooleanValue(true,
				(behavior) -> behavior.isBatchDuplicateActionPossible(new CartExtensionActionPossibleParams(cart, user)));
	}

	public boolean isDocumentBatchProcessingActionPossibleOnCart(final Cart cart, final User user) {
		return cartExtensions.getBooleanValue(true,
				(behavior) -> behavior.isDocumentBatchProcessingActionPossible(new CartExtensionActionPossibleParams(cart, user)));
	}

	public boolean isFolderBatchProcessingActionPossibleOnCart(final Cart cart, final User user) {
		return cartExtensions.getBooleanValue(true,
				(behavior) -> behavior.isFolderBatchProcessingActionPossible(new CartExtensionActionPossibleParams(cart, user)));
	}

	public boolean isContainerRecordBatchProcessingActionPossibleOnCart(final Cart cart, final User user) {
		return cartExtensions.getBooleanValue(true,
				(behavior) -> behavior.isContainerRecordBatchProcessingActionPossible(new CartExtensionActionPossibleParams(cart, user)));
	}

	public boolean isFoldersLabelsActionPossibleOnCart(final Cart cart, final User user) {
		return cartExtensions.getBooleanValue(true,
				(behavior) -> behavior.isFoldersLabelsActionPossible(new CartExtensionActionPossibleParams(cart, user)));
	}

	public boolean isDocumentLabelsActionPossibleOnCart(final Cart cart, final User user) {
		return cartExtensions.getBooleanValue(true,
				(behavior) -> behavior.isDocumentLabelsActionPossible(new CartExtensionActionPossibleParams(cart, user)));
	}

	public boolean isContainerLabelsActionPossibleOnCart(final Cart cart, final User user) {
		return cartExtensions.getBooleanValue(true,
				(behavior) -> behavior.isContainersLabelsActionPossible(new CartExtensionActionPossibleParams(cart, user)));
	}

	public boolean isBatchDeleteActionPossibleOnCart(final Cart cart, final User user) {
		return cartExtensions.getBooleanValue(true,
				(behavior) -> behavior.isBatchDeleteActionPossible(new CartExtensionActionPossibleParams(cart, user)));
	}

	public boolean isEmptyActionPossibleOnCart(final Cart cart, final User user) {
		return cartExtensions.getBooleanValue(true,
				(behavior) -> behavior.isEmptyActionPossible(new CartExtensionActionPossibleParams(cart, user)));
	}

	public boolean isShareActionPossibleOnCart(final Cart cart, final User user) {
		return cartExtensions.getBooleanValue(true,
				(behavior) -> behavior.isShareActionPossible(new CartExtensionActionPossibleParams(cart, user)));
	}

	public boolean isDecommissionActionPossibleOnCart(final Cart cart, final User user) {
		return cartExtensions.getBooleanValue(true,
				(behavior) -> behavior.isDecommissionActionPossible(new CartExtensionActionPossibleParams(cart, user)));
	}

	public boolean isPrintMetadataReportActionPossibleOnCart(final Cart cart, final User user) {
		return cartExtensions.getBooleanValue(true,
				(behavior) -> behavior.isPrintMetadataReportActionPossible(new CartExtensionActionPossibleParams(cart, user)));
	}

	public boolean isCreateSIPArchvesActionPossibleOnCart(final Cart cart, final User user) {
		return cartExtensions.getBooleanValue(true,
				(behavior) -> behavior.isCreateSIPArchvesActionPossible(new CartExtensionActionPossibleParams(cart, user)));
	}

	public boolean isConsolidatedPdfActionPossibleOnCart(final Cart cart, final User user) {
		return cartExtensions.getBooleanValue(true,
				(behavior) -> behavior.isConsolidatedPdfActionPossible(new CartExtensionActionPossibleParams(cart, user)));
	}

	public boolean isRecordBorrowActionPossibleOnCart(final Cart cart, final User user) {
		return cartExtensions.getBooleanValue(true,
				(behavior) -> behavior.isRecordBorrowActionPossible(new CartExtensionActionPossibleParams(cart, user)));
	}

	public boolean isAddDocumentActionPossibleOnFolder(final Folder folder, final User user) {
		if (!folderExtensions.iterator().hasNext()) {
			return user.hasWriteAccess().on(folder);
		} else {
			return folderExtensions.getBooleanValue(true,
					(behavior) -> behavior.isAddDocumentActionPossible(new FolderExtensionActionPossibleParams(folder, user)));
		}
	}

	public boolean isMoveActionPossibleOnFolder(final Folder folder, final User user) {
		return user.hasWriteAccess().on(folder) && folderExtensions.getBooleanValue(true,
				(behavior) -> behavior.isMoveActionPossible(new FolderExtensionActionPossibleParams(folder, user)));
	}

	public boolean isAddSubFolderActionPossibleOnFolder(final Folder folder, final User user) {
		return user.hasWriteAccess().on(folder) && folderExtensions.getBooleanValue(true,
				(behavior) -> behavior.isAddSubFolderActionPossible(new FolderExtensionActionPossibleParams(folder, user)));
	}

	public boolean isDisplayActionPossibleOnFolder(final Folder folder, final User user) {
		return folderExtensions.getBooleanValue(true,
				(behavior) -> behavior.isDisplayActionPossible(new FolderExtensionActionPossibleParams(folder, user)));
	}

	public boolean isEditActionPossibleOnFolder(final Folder folder, final User user) {
		return user.hasWriteAccess().on(folder) && folderExtensions.getBooleanValue(true,
				(behavior) -> behavior.isEditActionPossible(new FolderExtensionActionPossibleParams(folder, user)));
	}

	public boolean isDuplicateActionPossibleOnFolder(final Folder folder, final User user) {
		return folderExtensions.getBooleanValue(true,
				(behavior) -> behavior.isDuplicateActionPossible(new FolderExtensionActionPossibleParams(folder, user)));
	}

	public boolean isDeleteActionPossibleOnFolder(final Folder folder, final User user) {
		return user.hasDeleteAccess().on(folder) && folderExtensions.getBooleanValue(true,
				(behavior) -> behavior.isDeleteActionPossible(new FolderExtensionActionPossibleParams(folder, user)));
	}

	public boolean isViewOrAddAuthorizationActionPossibleOnFolder(final Folder folder, final User user) {
		return user.hasReadAccess().on(folder) && folderExtensions.getBooleanValue(true,
				(behavior) -> behavior.isViewOrAddAuthorizationActionPossible(new FolderExtensionActionPossibleParams(folder, user)));
	}

	// TODO adapt to use lambda

	public boolean isShareActionPossibleOnFolder(final Folder folder, final User user) {
		return user.hasReadAccess().on(folder) && folderExtensions.getBooleanValue(true,
				(behavior) -> behavior.isShareActionPossible(new FolderExtensionActionPossibleParams(folder, user)));
	}

	public boolean isAddToCartActionPossibleOnFolder(final Folder folder, final User user) {
		return folderExtensions.getBooleanValue(true,
				(behavior) -> behavior.isAddToCartActionPossible(new FolderExtensionActionPossibleParams(folder, user)));
	}

	public boolean isDecommissioningActionPossibleOnFolder(final Folder folder, final User user) {
		return user.hasWriteAccess().on(folder) && folderExtensions.getBooleanValue(true, new ExtensionUtils.BooleanCaller<FolderExtension>() {
			@Override
			public ExtensionBooleanResult call(FolderExtension behavior) {
				return behavior.isDecommissioningActionPossible(
						new FolderExtensionActionPossibleParams(folder, user));
			}
		});
	}

	public boolean isCreateDecommissioningListActionPossibleOnFolder(Folder folder, User user) {
		return user.hasWriteAccess().on(folder) && folderExtensions.getBooleanValue(true,
				(behavior) -> behavior.isCreateDecommissioningListActionPossible(
						new FolderExtensionActionPossibleParams(folder, user)));
	}

	public boolean isBorrowingActionPossibleOnFolder(final Folder folder, final User user) {
		return user.hasWriteAccess().on(folder) && folderExtensions.getBooleanValue(true,
				(behavior) -> behavior.isBorrowingActionPossible(new FolderExtensionActionPossibleParams(folder, user)));
	}

	public boolean isReturnActionPossibleOnFolder(final Folder folder, final User user) {
		return user.hasWriteAccess().on(folder) && folderExtensions.getBooleanValue(true,
				(behavior) -> behavior.isReturnActionPossible(new FolderExtensionActionPossibleParams(folder, user)));
	}

	public boolean isPrintLabelActionPossibleOnFolder(final Folder folder, final User user) {
		return folderExtensions.getBooleanValue(true,
				(behavior) -> behavior.isPrintLabelActionPossible(new FolderExtensionActionPossibleParams(folder, user)));
	}

	public boolean isGenerateReportActionPossibleOnFolder(final Folder folder, final User user) {
		return folderExtensions.getBooleanValue(true,
				(behavior) -> behavior.isGenerateReportActionPossible(new FolderExtensionActionPossibleParams(folder, user)));
	}

	public boolean isConsultLinkActionPossibleOnFolder(Folder folder, User user) {
		return folderExtensions.getBooleanValue(true, (behavior -> behavior.isConsultLinkActionPossible(new FolderExtensionActionPossibleParams(folder, user))));
	}

	public boolean isDisplayActionPossibleOnDocument(final Document document, final User user) {
		return documentExtensions.getBooleanValue(true,
				(behavior) -> behavior.isDisplayActionPossible(
						new DocumentExtension.DocumentExtensionActionPossibleParams(document, user)));
	}

	public boolean isAddAuthorizationActionPossibleOnDocument(final Document document, final User user) {
		return user.hasWriteAccess().on(document) && documentExtensions.getBooleanValue(true,
				(behavior) -> behavior.isAddAuthorizationActionPossible(new DocumentExtensionActionPossibleParams(document, user)));
	}

	public boolean isViewOrAddAuthorizationActionPossibleOnDocument(final Document document, final User user) {
		return user.hasWriteAccess().on(document) && documentExtensions.getBooleanValue(true,
				(behavior) -> behavior.isViewOrAddAuthorizationActionPossible(new DocumentExtensionActionPossibleParams(document, user)));
	}

	public boolean isGenerateReportActionPossibleOnDocument(final Document document, final User user) {
		return documentExtensions.getBooleanValue(true,
				(behavior) -> behavior.isGenerateReportActionPossible(new DocumentExtensionActionPossibleParams(document, user)));
	}

	public boolean isOpenActionPossibleOnDocument(final Document document, final User user) {
		return documentExtensions.getBooleanValue(true,
				(behavior) -> behavior.isOpenActionPossible(
						new DocumentExtension.DocumentExtensionActionPossibleParams(document, user)));
	}

	public boolean isEditActionPossibleOnDocument(final Document document, final User user) {
		return user.hasWriteAccess().on(document) && documentExtensions.getBooleanValue(true,
				(behavior) -> behavior.isEditActionPossible(
						new DocumentExtension.DocumentExtensionActionPossibleParams(document, user)));
	}

	public boolean isRenameActionPossibleOnDocument(final Document document, final User user) {
		return documentExtensions.getBooleanValue(true,
				(behavior) -> behavior.isRenameActionPossible(
						new DocumentExtension.DocumentExtensionActionPossibleParams(document, user)));
	}

	public boolean isDownloadActionPossibleOnDocument(final Document document, final User user) {
		return documentExtensions.getBooleanValue(true,
				(behavior) -> behavior.isDownloadActionPossible(
						new DocumentExtension.DocumentExtensionActionPossibleParams(document, user)));
	}

	public boolean isCheckOutActionPossibleOnDocument(final Document document, final User user) {
		return user.hasWriteAccess().on(document) && documentExtensions.getBooleanValue(true,
				(behavior) -> behavior.isCheckOutActionPossible(
						new DocumentExtension.DocumentExtensionActionPossibleParams(document, user)));
	}

	public boolean isCheckInActionPossibleOnDocument(final Document document, final User user) {
		return user.hasWriteAccess().on(document) && documentExtensions.getBooleanValue(true,
				(behavior) -> behavior.isCheckInActionPossible(
						new DocumentExtension.DocumentExtensionActionPossibleParams(document, user)));
	}

	public boolean isCopyActionPossibleOnDocument(final Document document, final User user) {
		return user.hasWriteAccess().on(document) && documentExtensions.getBooleanValue(true,
				(behavior) -> behavior.isCopyActionPossible(
						new DocumentExtension.DocumentExtensionActionPossibleParams(document, user)));
	}

	public boolean isRenameContentActionPossibleOnDocument(final Document document, final User user) {
		return user.hasWriteAccess().on(document) && document.hasContent() && documentExtensions.getBooleanValue(true,
				(behavior) -> behavior.isRenameContentActionPossible(
						new DocumentExtension.DocumentExtensionActionPossibleParams(document, user)));
	}

	public boolean isCreateSipActionPossibleOnDocument(final Document document, final User user) {
		return documentExtensions.getBooleanValue(true,
				(behavior) -> behavior.isCreateSipActionPossible(
						new DocumentExtension.DocumentExtensionActionPossibleParams(document, user)));
	}

	public boolean isSendEmailActionPossibleOnDocument(final Document document, final User user) {
		return documentExtensions.getBooleanValue(true,
				(behavior) -> behavior.isSendEmailActionPossible(
						new DocumentExtension.DocumentExtensionActionPossibleParams(document, user)));
	}

	// TODO adapt methods below to use a lambda

	public boolean isCreatePDFAActionPossibleOnDocument(final Document document, final User user) {
		return user.hasWriteAccess().on(document) && documentExtensions.getBooleanValue(true, new ExtensionUtils.BooleanCaller<DocumentExtension>() {
			@Override
			public ExtensionBooleanResult call(DocumentExtension behavior) {
				return behavior.isCreatePDFAActionPossible(
						new DocumentExtension.DocumentExtensionActionPossibleParams(document, user));
			}
		});
	}

	public boolean isAddToCartActionPossibleOnDocument(final Document document, final User user) {
		return documentExtensions.getBooleanValue(true, new BooleanCaller<DocumentExtension>() {
			@Override
			public ExtensionBooleanResult call(DocumentExtension behavior) {
				return behavior.isAddSelectionActionPossible(new DocumentExtensionActionPossibleParams(document, user));
			}
		});
	}

	public boolean isAddRemoveToSelectionActionPossibleOnDocument(final Document document, final User user) {
		return documentExtensions.getBooleanValue(true, new BooleanCaller<DocumentExtension>() {
			@Override
			public ExtensionBooleanResult call(DocumentExtension behavior) {
				return behavior.isAddSelectionActionPossible(new DocumentExtensionActionPossibleParams(document, user));
			}
		});
	}

	public boolean isUploadActionPossibleOnDocument(final Document document, final User user) {
		return user.hasWriteAccess().on(document) && documentExtensions.getBooleanValue(true, new BooleanCaller<DocumentExtension>() {
			@Override
			public ExtensionBooleanResult call(DocumentExtension behavior) {
				return behavior.isUploadActionPossible(new DocumentExtensionActionPossibleParams(document, user));
			}
		});
	}

	public boolean isFinalizeActionPossibleOnDocument(final Document document, final User user) {
		return user.hasWriteAccess().on(document) && documentExtensions.getBooleanValue(true, new ExtensionUtils.BooleanCaller<DocumentExtension>() {
			@Override
			public ExtensionBooleanResult call(DocumentExtension behavior) {
				return behavior.isFinalizeActionPossible(
						new DocumentExtension.DocumentExtensionActionPossibleParams(document, user));
			}
		});
	}

	public boolean isMoveActionPossibleOnDocument(final Document document, final User user) {
		return user.hasWriteAccess().on(document) && documentExtensions.getBooleanValue(true, new ExtensionUtils.BooleanCaller<DocumentExtension>() {
			@Override
			public ExtensionBooleanResult call(DocumentExtension behavior) {
				return behavior.isMoveActionPossible(
						new DocumentExtension.DocumentExtensionActionPossibleParams(document, user));
			}
		});
	}

	public boolean isUnPublishActionPossibleOnDocument(final Document document, final User user) {
		return user.hasWriteAccess().on(document) && documentExtensions.getBooleanValue(true, new ExtensionUtils.BooleanCaller<DocumentExtension>() {
			@Override
			public ExtensionBooleanResult call(DocumentExtension behavior) {
				return behavior.isUnPublishActionPossible(
						new DocumentExtension.DocumentExtensionActionPossibleParams(document, user));
			}
		});
	}

	public boolean isPrintLabelActionPossibleOnDocument(final Document document, final User user) {
		return documentExtensions.getBooleanValue(true, new ExtensionUtils.BooleanCaller<DocumentExtension>() {
			@Override
			public ExtensionBooleanResult call(DocumentExtension behavior) {
				return behavior.isPublishActionPossible(
						new DocumentExtension.DocumentExtensionActionPossibleParams(document, user));
			}
		});
	}

	public boolean isPublishActionPossibleOnDocument(final Document document, final User user) {
		return user.hasWriteAccess().on(document) && documentExtensions.getBooleanValue(true, new ExtensionUtils.BooleanCaller<DocumentExtension>() {
			@Override
			public ExtensionBooleanResult call(DocumentExtension behavior) {
				return behavior.isPublishActionPossible(
						new DocumentExtension.DocumentExtensionActionPossibleParams(document, user));
			}
		});
	}

	public boolean isGetPublicLinkActionPossibleOnDocument(final Document document, final User user) {
		return documentExtensions.getBooleanValue(true, new ExtensionUtils.BooleanCaller<DocumentExtension>() {
			@Override
			public ExtensionBooleanResult call(DocumentExtension behavior) {
				return behavior.isGetPublicLinkActionPossible(
						new DocumentExtension.DocumentExtensionActionPossibleParams(document, user));
			}
		});
	}

	public boolean isDeleteActionPossbileOnDocument(final Document document, final User user) {
		return user.hasDeleteAccess().on(document) && documentExtensions.getBooleanValue(true, new ExtensionUtils.BooleanCaller<DocumentExtension>() {
			@Override
			public ExtensionBooleanResult call(DocumentExtension behavior) {
				return behavior.isDeleteActionPossible(
						new DocumentExtension.DocumentExtensionActionPossibleParams(document, user));
			}
		});
	}

	public boolean isShareActionPossibleOnDocument(final Document document, final User user) {
		return user.hasWriteAccess().on(document) && documentExtensions.getBooleanValue(true, new ExtensionUtils.BooleanCaller<DocumentExtension>() {
			@Override
			public ExtensionBooleanResult call(DocumentExtension behavior) {
				return behavior.isShareActionPossible(
						new DocumentExtension.DocumentExtensionActionPossibleParams(document, user));
			}
		});
	}

	public boolean isConsultLinkActionPossibleOnDocument(Document document, User user) {
		return documentExtensions.getBooleanValue(true, (behavior -> behavior.isConsultLinkActionPossible(document, user)));
	}

	public boolean isConsultActionPossibleOnContainerRecord(final ContainerRecord containerRecord, final User user) {
		return containerRecordExtensions.getBooleanValue(true, (behavior ->
				behavior.isConsultActionPossible(new ContainerRecordExtensionActionPossibleParams(containerRecord, user))));
	}

	public boolean isAddToCartActionPossibleOnContainerRecord(final ContainerRecord containerRecord, final User user) {
		return containerRecordExtensions.getBooleanValue(true,
				(behavior) -> behavior.isAddToCartActionPossible(new ContainerRecordExtensionActionPossibleParams(containerRecord, user)));
	}

	public boolean isEditActionPossibleOnContainerRecord(final ContainerRecord containerRecord, final User user) {
		return containerRecordExtensions.getBooleanValue(true,
				(behavior -> behavior.isEditActionPossible(new ContainerRecordExtensionActionPossibleParams(containerRecord, user))));
	}

	public boolean isSlipActionPossibleOnContainerRecord(final ContainerRecord containerRecord, final User user) {
		return containerRecordExtensions.getBooleanValue(true,
				(behavior -> behavior.isSlipActionPossible(new ContainerRecordExtensionActionPossibleParams(containerRecord, user))));
	}

	public boolean isLabelsActionPossibleOnContainerRecord(final ContainerRecord containerRecord, final User user) {
		return containerRecordExtensions.getBooleanValue(true,
				(behavior -> behavior.isLabelActionPossible(new ContainerRecordExtensionActionPossibleParams(containerRecord, user))));
	}

	public boolean isDeleteActionPossibleOnContainerRecord(final ContainerRecord containerRecord, final User user) {
		return containerRecordExtensions.getBooleanValue(true,
				(behavior -> behavior.isDeleteActionPossible(new ContainerRecordExtensionActionPossibleParams(containerRecord, user))));
	}

	public boolean isEmptyTheBoxActionPossibleOnContainerRecord(final ContainerRecord containerRecord,
																final User user) {
		return containerRecordExtensions.getBooleanValue(true,
				(behavior -> behavior.isEmptyTheBoxActionPossible(new ContainerRecordExtensionActionPossibleParams(containerRecord, user))));
	}


	public boolean isConsultLinkActionPossibleOnContainerRecord(ContainerRecord containerRecord, User user) {
		return containerRecordExtensions.getBooleanValue(true,
				(behavior -> behavior.isConsultLinkActionPossible(new ContainerRecordExtensionActionPossibleParams(containerRecord, user))));
	}

	public boolean isAddRemoveToSelectionActionPossibleOnContainerRecord(final ContainerRecord containerRecord,
																		 final User user) {
		return containerRecordExtensions.getBooleanValue(true,
				behavior -> behavior.isAddSelectionActionPossible(new ContainerRecordExtensionActionPossibleParams(containerRecord, user)));
	}

	public boolean isGenerateReportActionPossibleOnContainer(final ContainerRecord containerRecord, final User user) {
		return containerRecordExtensions.getBooleanValue(true,
				(behavior) -> behavior.isGenerateReportActionPossible(new ContainerRecordExtensionActionPossibleParams(containerRecord, user)));
	}

	public boolean isGenerateReportActionPossibleOnStorageSpace(final StorageSpace storageSpace, final User user) {
		return storageSpaceExtensions.getBooleanValue(true,
				(behavior) -> behavior.isGenerateReportActionPossible(new StorageSpaceExtensionActionPossibleParams(storageSpace, user)));
	}

	public boolean isConsultActionPossibleOnStorageSpace(final StorageSpace storageSpace, final User user) {
		return storageSpaceExtensions.getBooleanValue(true, (behavior ->
				behavior.isConsultActionPossible(new StorageSpaceExtensionActionPossibleParams(storageSpace, user))));
	}

	public boolean isEditActionPossibleOnStorageSpace(final StorageSpace storageSpace, final User user) {
		return storageSpaceExtensions.getBooleanValue(true,
				(behavior -> behavior.isEditActionPossible(new StorageSpaceExtensionActionPossibleParams(storageSpace, user))));
	}

	public boolean isDeleteActionPossibleOnStorageSpace(final StorageSpace storageSpace, final User user) {
		return storageSpaceExtensions.getBooleanValue(true,
				(behavior -> behavior.isDeleteActionPossible(new StorageSpaceExtensionActionPossibleParams(storageSpace, user))));
	}

	public boolean isConsultLinkActionPossibleOnStorageSpace(StorageSpace storageSpace, User user) {
		return storageSpaceExtensions.getBooleanValue(true,
				(behavior -> behavior.isConsultLinkActionPossible(new StorageSpaceExtensionActionPossibleParams(storageSpace, user))));
	}

	public BaseBreadcrumbTrail getBreadCrumbtrail(DocumentFolderBreadCrumbParams documentBreadcrumParams) {
		BaseBreadcrumbTrail breadcrumbTrail = null;

		for (DocumentFolderBreadCrumbExtention documentFolderBreadCrumbExtention : documentBreadcrumExtentions) {
			breadcrumbTrail = documentFolderBreadCrumbExtention.getBreadcrumTrail(documentBreadcrumParams);

			if (breadcrumbTrail != null) {
				break;
			}
		}

		return breadcrumbTrail;
	}

	public boolean navigateToDisplayDocumentWhileKeepingTraceOfPreviousView(
			NavigateToFromAPageParams navigateToFromAPageParams) {
		for (NavigateToFromAPageImportExtension navigateToFromAPageImportExtension : getNavigateToFromAPageExtensions()) {
			if (navigateToFromAPageImportExtension.navigateToDisplayDocumentWhileKeepingTraceOfPreviousView(navigateToFromAPageParams)) {
				return true;
			}
		}

		return false;
	}

	public boolean navigateToDisplayFolderWhileKeepingTraceOfPreviousView(
			NavigateToFromAPageParams navigateToFromAPageParams) {
		for (NavigateToFromAPageImportExtension navigateToFromAPageImportExtension : getNavigateToFromAPageExtensions()) {
			if (navigateToFromAPageImportExtension.navigateToDisplayFolderWhileKeepingTraceOfPreviousView(navigateToFromAPageParams)) {
				return true;
			}
		}

		return false;
	}

	public boolean navigateToEditFolderWhileKeepingTraceOfPreviousView(
			NavigateToFromAPageParams navigateToFromAPageParams) {
		for (NavigateToFromAPageImportExtension navigateToFromAPageImportExtension : getNavigateToFromAPageExtensions()) {
			if (navigateToFromAPageImportExtension.navigateToEditFolderWhileKeepingTraceOfPreviousView(navigateToFromAPageParams)) {
				return true;
			}
		}

		return false;
	}

	public boolean navigateToDuplicateFolderWhileKeepingTraceOfPreviousView(
			NavigateToFromAPageParams navigateToFromAPageParams) {
		for (NavigateToFromAPageImportExtension navigateToFromAPageImportExtension : getNavigateToFromAPageExtensions()) {
			if (navigateToFromAPageImportExtension.navigateToDuplicateFolderWhileKeepingTraceOfPreviousView(navigateToFromAPageParams)) {
				return true;
			}
		}

		return false;
	}

	public boolean navigateToEditDocumentWhileKeepingTraceOfPreviousView(
			NavigateToFromAPageParams navigateToFromAPageParams) {
		for (NavigateToFromAPageImportExtension navigateToFromAPageImportExtension : getNavigateToFromAPageExtensions()) {
			if (navigateToFromAPageImportExtension.navigateToEditDocumentWhileKeepingTraceOfPreviousView(navigateToFromAPageParams)) {
				return true;
			}
		}

		return false;
	}

	public boolean navigateToAddDocumentWhileKeepingTraceOfPreviousView(
			NavigateToFromAPageParams navigateToFromAPageParams) {
		for (NavigateToFromAPageImportExtension navigateToFromAPageImportExtension : getNavigateToFromAPageExtensions()) {
			if (navigateToFromAPageImportExtension.navigateToAddDocumentWhileKeepingTraceOfPreviousView(navigateToFromAPageParams)) {
				return true;
			}
		}

		return false;
	}

	public RecordVO buildExternalLinkVO(RMExternalLinkVOExtensionParams params) {
		RecordVO result = null;
		for (RMExternalLinkVOExtension extension : externalLinkVOExtensions) {
			result = extension.buildExternalLinkVO(params);
			if (result != null) {
				break;
			}
		}
		return result;
	}
	
	public void afterSaveDocument(RMAddEditDocumentPageExtension.AfterSaveDocumentParams params) {
		for (RMAddEditDocumentPageExtension extension : addEditDocumentPageExtensions) {
			extension.afterSaveDocument(params);
		}
	}
	
}
