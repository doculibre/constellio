package com.constellio.app.modules.rm.extensions.api;

import com.constellio.app.api.extensions.DocumentFolderBreadCrumbExtention;
import com.constellio.app.api.extensions.NavigateToFromAPageImportExtension;
import com.constellio.app.api.extensions.params.DocumentFolderBreadCrumbParams;
import com.constellio.app.api.extensions.params.NavigateToFromAPageParams;
import com.constellio.app.extensions.ModuleExtensions;
import com.constellio.app.modules.rm.extensions.api.ContainerRecordExtension.ContainerRecordExtensionActionPossibleParams;
import com.constellio.app.modules.rm.extensions.api.DocumentExtension.DocumentExtensionActionPossibleParams;
import com.constellio.app.modules.rm.extensions.api.DocumentExtension.DocumentExtensionAddMenuItemParams;
import com.constellio.app.modules.rm.extensions.api.FolderExtension.FolderExtensionActionPossibleParams;
import com.constellio.app.modules.rm.extensions.api.reports.RMReportBuilderFactories;
import com.constellio.app.modules.rm.wrappers.ContainerRecord;
import com.constellio.app.modules.rm.wrappers.Document;
import com.constellio.app.modules.rm.wrappers.Folder;
import com.constellio.app.modules.tasks.extensions.TaskManagementPresenterExtension;
import com.constellio.app.modules.tasks.extensions.TaskPreCompletionExtention;
import com.constellio.app.modules.tasks.extensions.param.PromptUserParam;
import com.constellio.app.services.factories.AppLayerFactory;
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
	private VaultBehaviorsList<TaskManagementPresenterExtension> taskManagementPresenterExtensions;
	private VaultBehaviorsList<DocumentExtension> documentExtensions;
	private VaultBehaviorsList<FolderExtension> folderExtensions;
	private VaultBehaviorsList<ContainerRecordExtension> containerRecordExtensions;
	private VaultBehaviorsList<AdvancedSearchPresenterExtension> advancedSearchPresenterExtensions;
	private VaultBehaviorsList<DocumentFolderBreadCrumbExtention> documentBreadcrumExtentions;
	private VaultBehaviorsList<NavigateToFromAPageImportExtension> navigateToFromAPageExtensions;
	private VaultBehaviorsList<TaskPreCompletionExtention> taskPreCompletionExetention;
	private VaultBehaviorsList<MenuItemActionExtension> menuItemActionExtensions;

	private ModelLayerExtensions modelLayerExtensions;

	public RMModuleExtensions(AppLayerFactory appLayerFactory) {
		rmReportBuilderFactories = new RMReportBuilderFactories(appLayerFactory);
		decommissioningBuilderPresenterExtensions = new VaultBehaviorsList<>();
		decommissioningListPresenterExtensions = new VaultBehaviorsList<>();
		taskManagementPresenterExtensions = new VaultBehaviorsList<>();
		this.documentExtensions = new VaultBehaviorsList<>();
		this.folderExtensions = new VaultBehaviorsList<>();
		this.containerRecordExtensions = new VaultBehaviorsList<>();
		advancedSearchPresenterExtensions = new VaultBehaviorsList<>();
		menuItemActionExtensions = new VaultBehaviorsList<>();
		this.documentBreadcrumExtentions = new VaultBehaviorsList<>();
		this.navigateToFromAPageExtensions = new VaultBehaviorsList<>();
		this.taskPreCompletionExetention = new VaultBehaviorsList<>();
		this.modelLayerExtensions = appLayerFactory.getModelLayerFactory().getExtensions();
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

	public VaultBehaviorsList<NavigateToFromAPageImportExtension> getNavigateToFromAPageExtensions() {
		return this.navigateToFromAPageExtensions;
	}

	public VaultBehaviorsList<DecommissioningListPresenterExtension> getDecommissioningListPresenterExtensions() {
		return decommissioningListPresenterExtensions;
	}

	public VaultBehaviorsList<TaskManagementPresenterExtension> getTaskManagementPresenterExtensions() {
		return taskManagementPresenterExtensions;
	}

	public VaultBehaviorsList<DocumentExtension> getDocumentExtensions() {
		return documentExtensions;
	}

	public void addMenuBarButtons(DocumentExtensionAddMenuItemParams params) {
		for (DocumentExtension documentExtension : documentExtensions) {
			documentExtension.addMenuItems(params);
		}
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

	public VaultBehaviorsList<MenuItemActionExtension> getMenuItemActionExtensions() {
		return menuItemActionExtensions;
	}

	public boolean isCopyActionPossibleOnFolder(final Folder folder, final User user) {
		return folderExtensions.getBooleanValue(true, new ExtensionUtils.BooleanCaller<FolderExtension>() {
			@Override
			public ExtensionBooleanResult call(FolderExtension behavior) {
				return behavior.isCopyActionPossible(
						new FolderExtensionActionPossibleParams(folder, user));
			}
		});
	}

	public boolean isPromptUser(PromptUserParam taskPreCompletionParam) throws ValidationException {
		for (TaskPreCompletionExtention taskPreCompletion : taskPreCompletionExetention) {
			if (taskPreCompletion.isPromptUser(taskPreCompletionParam)) {
				return true;
			}
		}
		return false;
	}

	public boolean isAddDocumentActionPossibleOnFolder(final Folder folder, final User user) {
		return folderExtensions.getBooleanValue(true,
				(behavior) -> behavior.isAddDocumentActionPossible(new FolderExtensionActionPossibleParams(folder, user)));
	}

	public boolean isMoveActionPossibleOnFolder(final Folder folder, final User user) {
		return folderExtensions.getBooleanValue(true,
				(behavior) -> behavior.isMoveActionPossible(new FolderExtensionActionPossibleParams(folder, user)));
	}

	public boolean isAddSubFolderActionPossibleOnFolder(final Folder folder, final User user) {
		return folderExtensions.getBooleanValue(true,
				(behavior) -> behavior.isAddSubFolderActionPossible(new FolderExtensionActionPossibleParams(folder, user)));
	}

	public boolean isDisplayActionPossibleOnFolder(final Folder folder, final User user) {
		return folderExtensions.getBooleanValue(true,
				(behavior) -> behavior.isDisplayActionPossible(new FolderExtensionActionPossibleParams(folder, user)));
	}

	public boolean isEditActionPossibleOnFolder(final Folder folder, final User user) {
		return folderExtensions.getBooleanValue(true,
				(behavior) -> behavior.isEditActionPossible(new FolderExtensionActionPossibleParams(folder, user)));
	}

	public boolean isDeleteActionPossibleOnFolder(final Folder folder, final User user) {
		return folderExtensions.getBooleanValue(true,
				(behavior) -> behavior.isDeleteActionPossible(new FolderExtensionActionPossibleParams(folder, user)));
	}

	public boolean isAddAuthorizationActionPossibleOnFolder(final Folder folder, final User user) {
		return folderExtensions.getBooleanValue(true,
				(behavior) -> behavior.isAddAuthorizationActionPossible(new FolderExtensionActionPossibleParams(folder, user)));
	}

	// TODO adapt to use lambda

	public boolean isShareActionPossibleOnFolder(final Folder folder, final User user) {
		return folderExtensions.getBooleanValue(true,
				(behavior) -> behavior.isShareActionPossible(new FolderExtensionActionPossibleParams(folder, user)));
	}

	public boolean isAddToCartActionPossibleOnFolder(final Folder folder, final User user) {
		return folderExtensions.getBooleanValue(true,
				(behavior) -> behavior.isAddToCartActionPossible(new FolderExtensionActionPossibleParams(folder, user)));
	}

	public boolean isDecommissioningActionPossibleOnFolder(final Folder folder, final User user) {
		return folderExtensions.getBooleanValue(true, new ExtensionUtils.BooleanCaller<FolderExtension>() {
			@Override
			public ExtensionBooleanResult call(FolderExtension behavior) {
				return behavior.isDecommissioningActionPossible(
						new FolderExtensionActionPossibleParams(folder, user));
			}
		});
	}

	public boolean isBorrowingActionPossibleOnFolder(final Folder folder, final User user) {
		return folderExtensions.getBooleanValue(true,
				(behavior) -> behavior.isBorrowingActionPossible(new FolderExtensionActionPossibleParams(folder, user)));
	}

	public boolean isReturnActionPossibleOnFolder(final Folder folder, final User user) {
		return folderExtensions.getBooleanValue(true,
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

	public boolean isDisplayActionPossibleOnDocument(final Document document, final User user) {
		return documentExtensions.getBooleanValue(true,
				(behavior) -> behavior.isDisplayActionPossible(
						new DocumentExtension.DocumentExtensionActionPossibleParams(document, user)));
	}

	public boolean isAddAuthorizationActionPossibleOnDocument(final Document document, final User user) {
		return documentExtensions.getBooleanValue(true,
				(behavior) -> behavior.isAddAuthorizationActionPossible(new DocumentExtensionActionPossibleParams(document, user)));
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
		return documentExtensions.getBooleanValue(true,
				(behavior) -> behavior.isEditActionPossible(
						new DocumentExtension.DocumentExtensionActionPossibleParams(document, user)));
	}

	public boolean isDownloadActionPossibleOnDocument(final Document document, final User user) {
		return documentExtensions.getBooleanValue(true,
				(behavior) -> behavior.isDownloadActionPossible(
						new DocumentExtension.DocumentExtensionActionPossibleParams(document, user)));
	}

	public boolean isCopyActionPossibleOnDocument(final Document document, final User user) {
		return documentExtensions.getBooleanValue(true,
				(behavior) -> behavior.isCopyActionPossible(
						new DocumentExtension.DocumentExtensionActionPossibleParams(document, user)));
	}

	// TODO adapt methods below to use a lambda

	public boolean isCreatePDFAActionPossibleOnDocument(final Document document, final User user) {
		return documentExtensions.getBooleanValue(true, new ExtensionUtils.BooleanCaller<DocumentExtension>() {
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
		return documentExtensions.getBooleanValue(true, new BooleanCaller<DocumentExtension>() {
			@Override
			public ExtensionBooleanResult call(DocumentExtension behavior) {
				return behavior.isUploadActionPossible(new DocumentExtensionActionPossibleParams(document, user));
			}
		});
	}

	public boolean isFinalizeActionPossibleOnDocument(final Document document, final User user) {
		return documentExtensions.getBooleanValue(true, new ExtensionUtils.BooleanCaller<DocumentExtension>() {
			@Override
			public ExtensionBooleanResult call(DocumentExtension behavior) {
				return behavior.isFinalizeActionPossible(
						new DocumentExtension.DocumentExtensionActionPossibleParams(document, user));
			}
		});
	}

	public boolean isMoveActionPossibleOnDocument(final Document document, final User user) {
		return documentExtensions.getBooleanValue(true, new ExtensionUtils.BooleanCaller<DocumentExtension>() {
			@Override
			public ExtensionBooleanResult call(DocumentExtension behavior) {
				return behavior.isMoveActionPossible(
						new DocumentExtension.DocumentExtensionActionPossibleParams(document, user));
			}
		});
	}

	public boolean isUnPublishActionPossibleOnDocument(final Document document, final User user) {
		return documentExtensions.getBooleanValue(true, new ExtensionUtils.BooleanCaller<DocumentExtension>() {
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
		return documentExtensions.getBooleanValue(true, new ExtensionUtils.BooleanCaller<DocumentExtension>() {
			@Override
			public ExtensionBooleanResult call(DocumentExtension behavior) {
				return behavior.isPublishActionPossible(
						new DocumentExtension.DocumentExtensionActionPossibleParams(document, user));
			}
		});
	}

	public boolean isDeleteActionPossbileOnDocument(final Document document, final User user) {
		return documentExtensions.getBooleanValue(true, new ExtensionUtils.BooleanCaller<DocumentExtension>() {
			@Override
			public ExtensionBooleanResult call(DocumentExtension behavior) {
				return behavior.isDeleteActionPossible(
						new DocumentExtension.DocumentExtensionActionPossibleParams(document, user));
			}
		});
	}

	public boolean isShareActionPossibleOnDocument(final Document document, final User user) {
		return documentExtensions.getBooleanValue(true, new ExtensionUtils.BooleanCaller<DocumentExtension>() {
			@Override
			public ExtensionBooleanResult call(DocumentExtension behavior) {
				return behavior.isShareActionPossible(
						new DocumentExtension.DocumentExtensionActionPossibleParams(document, user));
			}
		});
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

	public boolean isLabelsActionPossible(final ContainerRecord containerRecord, final User user) {
		return containerRecordExtensions.getBooleanValue(true,
				(behavior -> behavior.isLabelActionPossible(new ContainerRecordExtensionActionPossibleParams(containerRecord, user))));
	}

	public boolean isDeleteActionPossible(final ContainerRecord containerRecord, final User user) {
		return containerRecordExtensions.getBooleanValue(true,
				(behavior -> behavior.isDeleteActionPossible(new ContainerRecordExtensionActionPossibleParams(containerRecord, user))));
	}

	public boolean isEmptyTheBoxActionPossible(final ContainerRecord containerRecord, final User user) {
		return containerRecordExtensions.getBooleanValue(true,
				(behavior -> behavior.isEmptyTheBoxActionPossible(new ContainerRecordExtensionActionPossibleParams(containerRecord, user))));
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
}
