package com.constellio.app.modules.rm.extensions.api;

import com.constellio.app.extensions.ModuleExtensions;
import com.constellio.app.modules.rm.extensions.api.DocumentExtension.DocumentExtensionAddMenuItemParams;
import com.constellio.app.modules.rm.extensions.api.reports.RMReportBuilderFactories;
import com.constellio.app.modules.rm.wrappers.Document;
import com.constellio.app.modules.rm.wrappers.Folder;
import com.constellio.app.services.factories.AppLayerFactory;
import com.constellio.data.frameworks.extensions.ExtensionBooleanResult;
import com.constellio.data.frameworks.extensions.ExtensionUtils;
import com.constellio.data.frameworks.extensions.VaultBehaviorsList;
import com.constellio.model.entities.records.wrappers.User;
import com.constellio.model.services.extensions.ModelLayerExtensions;

public class RMModuleExtensions implements ModuleExtensions {

	private RMReportBuilderFactories rmReportBuilderFactories;
	private VaultBehaviorsList<DecommissioningBuilderPresenterExtension> decommissioningBuilderPresenterExtensions;
	private DecommissioningListFolderTableExtension decommissioningListFolderTableExtension;
	private VaultBehaviorsList<DecommissioningListPresenterExtension> decommissioningListPresenterExtensions;
	private VaultBehaviorsList<DocumentExtension> documentExtensions;
	private VaultBehaviorsList<FolderExtension> folderExtensions;
	private VaultBehaviorsList<AdvancedSearchPresenterExtension> advancedSearchPresenterExtensions;

	private ModelLayerExtensions modelLayerExtensions;

	public RMModuleExtensions(AppLayerFactory appLayerFactory) {
		rmReportBuilderFactories = new RMReportBuilderFactories(appLayerFactory);
		decommissioningBuilderPresenterExtensions = new VaultBehaviorsList<>();
		decommissioningListPresenterExtensions = new VaultBehaviorsList<>();
		documentExtensions = new VaultBehaviorsList<>();
		folderExtensions = new VaultBehaviorsList<>();
		advancedSearchPresenterExtensions = new VaultBehaviorsList<>();
		this.modelLayerExtensions = appLayerFactory.getModelLayerFactory().getExtensions();
	}

	public RMReportBuilderFactories getReportBuilderFactories() {
		return rmReportBuilderFactories;
	}

	public VaultBehaviorsList<DecommissioningBuilderPresenterExtension> getDecommissioningBuilderPresenterExtensions() {
		return decommissioningBuilderPresenterExtensions;
	}

	public DecommissioningListFolderTableExtension getDecommissioningListFolderTableExtension() {
		return decommissioningListFolderTableExtension;
	}

	public void setDecommissioningListFolderTableExtension(
			DecommissioningListFolderTableExtension decommissioningListFolderTableExtension) {
		this.decommissioningListFolderTableExtension = decommissioningListFolderTableExtension;
	}

	public VaultBehaviorsList<DecommissioningListPresenterExtension> getDecommissioningListPresenterExtensions() {
		return decommissioningListPresenterExtensions;
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

	public boolean isCopyActionPossibleOnFolder(final Folder folder, final User user) {
		return folderExtensions.getBooleanValue(true, new ExtensionUtils.BooleanCaller<FolderExtension>() {
			@Override
			public ExtensionBooleanResult call(FolderExtension behavior) {
				return behavior.isCopyActionPossible(
						new FolderExtension.FolderExtensionActionPossibleParams(folder, user));
			}
		});
	}

	public boolean isMoveActionPossibleOnFolder(final Folder folder, final User user) {
		return folderExtensions.getBooleanValue(true, new ExtensionUtils.BooleanCaller<FolderExtension>() {
			@Override
			public ExtensionBooleanResult call(FolderExtension behavior) {
				return behavior.isMoveActionPossible(
						new FolderExtension.FolderExtensionActionPossibleParams(folder, user));
			}
		});
	}

	public boolean isShareActionPossibleOnFolder(final Folder folder, final User user) {
		return folderExtensions.getBooleanValue(true, new ExtensionUtils.BooleanCaller<FolderExtension>() {
			@Override
			public ExtensionBooleanResult call(FolderExtension behavior) {
				return behavior.isShareActionPossible(
						new FolderExtension.FolderExtensionActionPossibleParams(folder, user));
			}
		});
	}

	public boolean isDecommissioningActionPossibleOnFolder(final Folder folder, final User user) {
		return folderExtensions.getBooleanValue(true, new ExtensionUtils.BooleanCaller<FolderExtension>() {
			@Override
			public ExtensionBooleanResult call(FolderExtension behavior) {
				return behavior.isDecommissioningActionPossible(
						new FolderExtension.FolderExtensionActionPossibleParams(folder, user));
			}
		});
	}

	public boolean isBorrowingActionPossibleOnFolder(final Folder folder, final User user) {
		boolean defaultValue = !modelLayerExtensions.forCollection(folder.getCollection())
				.isModifyBlocked(folder.getWrappedRecord(), user);
		return folderExtensions.getBooleanValue(defaultValue, new ExtensionUtils.BooleanCaller<FolderExtension>() {
			@Override
			public ExtensionBooleanResult call(FolderExtension behavior) {
				return behavior.isBorrowingActionPossible(
						new FolderExtension.FolderExtensionActionPossibleParams(folder, user));
			}
		});
	}

	public boolean isCopyActionPossibleOnDocument(final Document document, final User user) {
		return documentExtensions.getBooleanValue(true, new ExtensionUtils.BooleanCaller<DocumentExtension>() {
			@Override
			public ExtensionBooleanResult call(DocumentExtension behavior) {
				return behavior.isCopyActionPossible(
						new DocumentExtension.DocumentExtensionActionPossibleParams(document, user));
			}
		});
	}

	public boolean isCreatePDFAActionPossibleOnDocument(final Document document, final User user) {
		return documentExtensions.getBooleanValue(true, new ExtensionUtils.BooleanCaller<DocumentExtension>() {
			@Override
			public ExtensionBooleanResult call(DocumentExtension behavior) {
				return behavior.isCreatePDFAActionPossible(
						new DocumentExtension.DocumentExtensionActionPossibleParams(document, user));
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

	public boolean isPublishActionPossibleOnDocument(final Document document, final User user) {
		return documentExtensions.getBooleanValue(true, new ExtensionUtils.BooleanCaller<DocumentExtension>() {
			@Override
			public ExtensionBooleanResult call(DocumentExtension behavior) {
				return behavior.isPublishActionPossible(
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

}
