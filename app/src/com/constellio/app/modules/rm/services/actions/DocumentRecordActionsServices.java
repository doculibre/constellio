package com.constellio.app.modules.rm.services.actions;

import com.constellio.app.modules.rm.ConstellioRMModule;
import com.constellio.app.modules.rm.constants.RMPermissionsTo;
import com.constellio.app.modules.rm.extensions.api.RMModuleExtensions;
import com.constellio.app.modules.rm.model.enums.FolderStatus;
import com.constellio.app.modules.rm.services.RMSchemasRecordsServices;
import com.constellio.app.modules.rm.wrappers.Document;
import com.constellio.app.modules.rm.wrappers.Folder;
import com.constellio.app.services.factories.AppLayerFactory;
import com.constellio.app.ui.pages.base.SessionContext;
import com.constellio.model.entities.records.Content;
import com.constellio.model.entities.records.Record;
import com.constellio.model.entities.records.wrappers.User;
import com.constellio.model.extensions.ModelLayerCollectionExtensions;

public class DocumentRecordActionsServices {

	private RMSchemasRecordsServices rm;
	private RMModuleExtensions rmModuleExtensions;
	private transient ModelLayerCollectionExtensions modelLayerCollectionExtensions;

	public DocumentRecordActionsServices(String collection, AppLayerFactory appLayerFactory) {
		rm = new RMSchemasRecordsServices(collection, appLayerFactory);
		modelLayerCollectionExtensions = appLayerFactory.getModelLayerFactory().getExtensions().forCollection(collection);
		rmModuleExtensions = appLayerFactory.getExtensions().forCollection(collection).forModule(ConstellioRMModule.ID);
	}

	public boolean isDisplayActionPossible(Record record, User user) {
		return hasUserReadAccess(record, user) &&
			   rmModuleExtensions.isDisplayActionPossibleOnDocument(rm.wrapDocument(record), user);
	}

	public boolean isOpenActionPossible(Record record, User user) {
		return hasUserReadAccess(record, user) &&
			   rmModuleExtensions.isOpenActionPossibleOnDocument(rm.wrapDocument(record), user);
	}

	public boolean isEditActionPossible(Record record, User user) {
		return hasUserWriteAccess(record, user) &&
			   rmModuleExtensions.isEditActionPossibleOnDocument(rm.wrapDocument(record), user);
	}

	public boolean isDownloadActionPossible(Record record, User user) {
		Document document = rm.wrapDocument(record);
		return hasUserReadAccess(record, user) && document.hasContent() &&
			   rmModuleExtensions.isDownloadActionPossibleOnDocument(rm.wrapDocument(record), user);
	}

	public boolean isRenameActionPossible(Record record, User user) {
		return false;
	}

	public boolean isCopyActionPossible(Record record, User user) {
		// TODO
		if (!user.hasReadAccess().on(record)) {
			return false;
		}

		// TODO rename DocumentExtension to something like DocumentRecordActionsExtension
		return rmModuleExtensions.isCopyActionPossibleOnDocument(rm.wrapDocument(record), user);
	}

	public boolean isUnPublishActionPossible(Record record, User user) {
		Document document = rm.wrapDocument(record);
		return user.has(RMPermissionsTo.PUBLISH_AND_UNPUBLISH_DOCUMENTS)
					   .on(record) && rmModuleExtensions.isUnPublishActionPossibleOnDocument(document, user)
			   && document.isPublished();
	}

	public boolean isPublishActionPossible(Record record, User user) {
		Document document = rm.wrapDocument(record);
		return user.has(RMPermissionsTo.PUBLISH_AND_UNPUBLISH_DOCUMENTS)
				.on(record) && rmModuleExtensions.isPublishActionPossibleOnDocument(document, user)
				&& !document.isPublished();
	}

	public boolean isPrintLabelActionPossible(Record record, User user) {
		Document document = rm.wrapDocument(record);
		return user.hasReadAccess().on(record)
				&& rmModuleExtensions.isPrintLabelActionPossibleOnDocument(document, user);
	}

	public boolean isDeleteActionPossible(Record record, User user) {
		Document document = rm.wrapDocument(record);

		if (user.hasDeleteAccess().on(record) &&
			rmModuleExtensions.isDeleteActionPossbileOnDocument(rm.wrapDocument(record), user)) {
			if (document.isPublished() && !user.has(RMPermissionsTo.DELETE_PUBLISHED_DOCUMENT)
					.on(record)) {
				return false;
			}

			if (getCurrentBorrowerOf(document) != null && !user.has(RMPermissionsTo.DELETE_BORROWED_DOCUMENT)
					.on(record)) {
				return false;
			}
			FolderStatus archivisticStatus = document.getArchivisticStatus();
			if (archivisticStatus != null && archivisticStatus.isInactive()) {
				Folder parentFolder = rm.getFolder(document.getFolder());
				if (parentFolder.getBorrowed() != null && parentFolder.getBorrowed()) {
					return user.has(RMPermissionsTo.MODIFY_INACTIVE_BORROWED_FOLDER).on(parentFolder)
									   && user.has(RMPermissionsTo.DELETE_INACTIVE_DOCUMENT).on(record);
				}
				return user.has(RMPermissionsTo.DELETE_INACTIVE_DOCUMENT).on(record);
			}
			if (archivisticStatus != null && archivisticStatus.isInactive()) {
				Folder parentFolder = rm.getFolder(document.getFolder());
				if (parentFolder.getBorrowed() != null && parentFolder.getBorrowed()) {
					return user.has(RMPermissionsTo.MODIFY_SEMIACTIVE_BORROWED_FOLDER).on(parentFolder)
									   && user.has(RMPermissionsTo.DELETE_SEMIACTIVE_DOCUMENT).on(record);
				}
				return user.has(RMPermissionsTo.DELETE_SEMIACTIVE_DOCUMENT).on(record);
			}
			return true;
		}
		return false;
	}

	public boolean isCreatePdfActionPossible(Record record, User user) {
		Document document = rm.getDocument(record.getId());

		if (!isCheckOutPossible(document) ||
			document.getContent() == null || !isEditActionPossible(record, user)) {
			return false;
		}

		return rmModuleExtensions.isCreatePDFAActionPossibleOnDocument(rm.wrapDocument(record), user);
	}

	public boolean isAddToCartActionPossible(Record record, User user) {
		return user.hasReadAccess().on(record) &&
			   (hasUserPermissionToUseCart(user) || hasUserPermissionToUseMyCart(user)) &&
			   rmModuleExtensions.isAddToCartActionPossibleOnDocument(rm.wrapDocument(record), user);
	}

	private boolean hasUserPermissionToUseCart(User user) {
		return user.has(RMPermissionsTo.USE_GROUP_CART).globally();
	}

	private boolean hasUserPermissionToUseMyCart(User user) {
		return user.has(RMPermissionsTo.USE_MY_CART).globally();
	}

	public boolean isAddToSelectionActionPossible(Record record, User user, SessionContext sessionContext) {
		return  hasUserReadAccess(record, user)
				&& rmModuleExtensions.isAddRemoveToSelectionActionPossibleOnDocument(rm.wrapDocument(record), user);
	}

	public boolean isRemoveToSelectionActionPossible(Record record, User user) {
		return  hasUserReadAccess(record, user)
				&& rmModuleExtensions.isAddRemoveToSelectionActionPossibleOnDocument(rm.wrapDocument(record), user);
	}

	private boolean isUploadPossible(Document document, User user) {
		boolean email = isEmail(document);
		boolean checkedOut = isContentCheckedOut(document);
		boolean borrower = isCurrentUserBorrower(user, document.getContent());
		return !email && (!checkedOut || borrower);
	}

	protected boolean isCurrentUserBorrower(User currentUser, Content content) {
		return content != null && currentUser.getId().equals(content.getCheckoutUserId());
	}

	public boolean isUploadActionPossible(Record record, User user) {
		Document document = rm.wrapDocument(record);

		if(!rmModuleExtensions.isUploadActionPossibleOnDocument(rm.wrapDocument(record), user)
		   || !isEditActionPossible(record, user)) {
			return false;
		}

			FolderStatus archivisticStatus = document.getArchivisticStatus();
			if (archivisticStatus != null && isUploadPossible(document, user) && user.hasWriteAccess().on(record)
				&& modelLayerCollectionExtensions.isRecordModifiableBy(record, user)
				&& !modelLayerCollectionExtensions.isModifyBlocked(record, user)) {
				if (archivisticStatus.isInactive()) {
					Folder parentFolder = rm.getFolder(document.getFolder());
					if (parentFolder.getBorrowed() != null && parentFolder.getBorrowed()) {
						return user.has(RMPermissionsTo.MODIFY_INACTIVE_BORROWED_FOLDER).on(parentFolder)
										   && user.has(RMPermissionsTo.UPLOAD_INACTIVE_DOCUMENT).on(record);
					}
					return (user.has(RMPermissionsTo.UPLOAD_INACTIVE_DOCUMENT).on(record));
				}
				if (archivisticStatus.isSemiActive()) {
					Folder parentFolder = rm.getFolder(document.getFolder());
					if (parentFolder.getBorrowed() != null && parentFolder.getBorrowed()) {
						return user.has(RMPermissionsTo.MODIFY_SEMIACTIVE_BORROWED_FOLDER).on(parentFolder)
										   && user.has(RMPermissionsTo.UPLOAD_SEMIACTIVE_DOCUMENT).on(record);
					}
					return user.has(RMPermissionsTo.UPLOAD_SEMIACTIVE_DOCUMENT).on(record);
				}
				return true;
			}
			return false;
	}

	public boolean isCheckInActionPossible(Record record, User user) {
		if (user.hasWriteAccess().on(record)) {
			boolean permissionToReturnOtherUsersDocuments = user.has(RMPermissionsTo.RETURN_OTHER_USERS_DOCUMENTS)
					.on(record);
			Document document = rm.wrapDocument(record);
			if (isCheckInPossible(user, document) || (permissionToReturnOtherUsersDocuments && isContentCheckedOut(document))) {
				return true;
			}
		}
		return false;
	}

	private boolean isCheckInPossible(User user, Document document) {
		boolean email = isEmail(document);
		return !email && (document.getContent() != null && isCurrentUserBorrower(user, document.getContent()));
	}

	public boolean isCheckOutActionPossible(Record record, User user) {
		Document document = rm.wrapDocument(record);

		if (user.hasWriteAccess().on(record)) {
			if (isCheckOutPossible(document) && modelLayerCollectionExtensions.isRecordModifiableBy(record, user) && !modelLayerCollectionExtensions
					.isModifyBlocked(record, user)) {
				return true;
			}
		}
		return false;
	}

	public boolean isGenerateReportActionPossible(Record record, User user) {
		return user.hasReadAccess().on(record)
			   && rmModuleExtensions.isGenerateReportActionPossibleOnDocument(rm.wrapDocument(record), user);
	}

	public boolean isAddAuthorizationActionPossible(Record record, User user) {
		return user.has(RMPermissionsTo.MANAGE_DOCUMENT_AUTHORIZATIONS).on(record)
			   && rmModuleExtensions.isAddAuthorizationActionPossibleOnDocument(rm.wrapDocument(record), user);
	}

	public boolean isFinalizeActionPossible(Record record, User user) {
		Document document = rm.wrapDocument(record);

		boolean borrowed = isContentCheckedOut(document.getContent());
		boolean minorVersion;
		Content content = document.getContent();
		minorVersion = content != null && content.getCurrentVersion().getMinor() != 0;
		if (borrowed || !minorVersion || !hasUserWriteAccess(record, user)) {
			return false;
		}

		return rmModuleExtensions.isFinalizeActionPossibleOnDocument(document, user) && isEditActionPossible(record, user);
	}

	protected boolean isContentCheckedOut(Content content) {
		return content != null && content.getCheckoutUserId() != null;
	}

	protected boolean isCheckOutPossible(Document document) {
		boolean email = isEmail(document);
		return !email && (document != null && !isContentCheckedOut(document.getContent()));
	}

	protected boolean isContentCheckedOut(Document document) {
		return isContentCheckedOut(document.getContent());
	}

	private String getCurrentBorrowerOf(Document document) {
		return document.getContent() == null ? null : document.getContent().getCheckoutUserId();
	}

	public boolean isCancelCheckOutPossible(Document document) {
		boolean email = isEmail(document);
		return !email && (document.getContent() != null && isContentCheckedOut(document));
	}

	private boolean isEmail(Document document) {
		boolean email;
		if (document.getContent() != null && document.getContent().getCurrentVersion() != null) {
			email = rm.isEmail(document.getContent().getCurrentVersion().getFilename());
		} else {
			email = false;
		}
		return email;
	}

	/*
	DocumentActionsComponent.linkToDocument= Lien vers ce document
	DocumentActionsComponent.modifyDocumentType=Modifier le type de ce document
	*/

	/*

	Old buttons from context menu :
	if (displayDocumentButtonVisible) {
			ContextMenuItem displayDocumentItem = addItem($("DocumentContextMenu.displayDocument"), FontAwesome.FILE_O);
			displayDocumentItem.addItemClickListener(new BaseContextMenuItemClickListener() {
				@Override
				public void contextMenuItemClicked(ContextMenuItemClickEvent event) {
					presenter.displayDocumentButtonClicked();
				}
			});
		}

		if (openDocumentButtonVisible) {
			String fileName = contentVersionVO.getFileName();
			Resource icon = FileIconUtils.getIcon(fileName);
			ContextMenuItem downloadDocumentItem = addItem($("DocumentContextMenu.openDocument"), icon);
			downloadDocumentItem.addItemClickListener(new BaseContextMenuItemClickListener() {
				@Override
				public void contextMenuItemClicked(ContextMenuItemClickEvent event) {
					String agentURL = ConstellioAgentUtils.getAgentURL(recordVO, contentVersionVO);
					openAgentURL(agentURL);
					presenter.logOpenAgentUrl(recordVO);
				}
			});
		}

		if (downloadDocumentButtonVisible) {
			ContextMenuItem downloadDocumentItem = addItem($("DocumentContextMenu.downloadDocument"), FontAwesome.DOWNLOAD);
			downloadDocumentItem.addItemClickListener(new BaseContextMenuItemClickListener() {
				@SuppressWarnings("deprecation")
				@Override
				public void contextMenuItemClicked(ContextMenuItemClickEvent event) {
					ContentVersionVOResource contentVersionResource = new ContentVersionVOResource(contentVersionVO);
					Resource downloadedResource = DownloadLink.wrapForDownload(contentVersionResource);
					Page.getCurrent().open(downloadedResource, null, false);
					presenter.logDownload(recordVO);
				}
			});
		}

		if (editDocumentButtonVisible) {
			ContextMenuItem editDocumentItem = addItem($("DocumentContextMenu.editDocument"), FontAwesome.EDIT);
			editDocumentItem.addItemClickListener(new BaseContextMenuItemClickListener() {
				@Override
				public void contextMenuItemClicked(ContextMenuItemClickEvent event) {
					presenter.editDocumentButtonClicked(ParamUtils.getCurrentParams());
				}
			});
		}

		if (deleteDocumentButtonVisible) {
			ContextMenuItem deleteDocumentItem = addItem($("DocumentContextMenu.deleteDocument"), FontAwesome.TRASH_O);
			deleteDocumentItem.addItemClickListener(new ConfirmDialogContextMenuItemClickListener(DialogMode.INFO) {
				@Override
				protected String getConfirmDialogMessage() {
					return $("ConfirmDialog.confirmDelete");
				}

				@Override
				protected void confirmButtonClick(ConfirmDialog dialog) {
					presenter.deleteDocumentButtonClicked(ParamUtils.getCurrentParams());
				}
			});
		}

		if (addAuthorizationButtonVisible) {
			ContextMenuItem addAuthorizationItem = addItem($("DocumentContextMenu.addAuthorization"), FontAwesome.KEY);
			addAuthorizationItem.addItemClickListener(new BaseContextMenuItemClickListener() {
				@Override
				public void contextMenuItemClicked(ContextMenuItemClickEvent event) {
					presenter.addAuthorizationButtonClicked();
				}
			});
		}

		if (createPDFAButtonVisible) {
			ContextMenuItem createPDFAItem = addItem($("DocumentContextMenu.createPDFA"), FontAwesome.FILE_PDF_O);
			createPDFAItem.addItemClickListener(new ConfirmDialogContextMenuItemClickListener(DialogMode.WARNING) {
				@Override
				protected String getConfirmDialogMessage() {
					return $("ConfirmDialog.confirmCreatePDFA");
				}

				@Override
				protected void confirmButtonClick(ConfirmDialog dialog) {
					presenter.createPDFA(ParamUtils.getCurrentParams());
				}
			});
		}

		if (shareDocumentButtonVisible) {
			ContextMenuItem shareDocumentItem = addItem($("DocumentContextMenu.shareDocument"), FontAwesome.PAPER_PLANE_O);
			shareDocumentItem.addItemClickListener(new BaseContextMenuItemClickListener() {
				@Override
				public void contextMenuItemClicked(ContextMenuItemClickEvent event) {
					presenter.shareDocumentButtonClicked();
				}
			});
		}

		if (uploadButtonVisible) {
			ContextMenuItem uploadItem = addItem($("DocumentContextMenu.upload"), FontAwesome.UPLOAD);
			uploadItem.addItemClickListener(new BaseContextMenuItemClickListener() {
				@Override
				public void contextMenuItemClicked(ContextMenuItemClickEvent event) {
					presenter.uploadButtonClicked();
				}
			});
		}

		if (checkInButtonVisible) {
			ContextMenuItem checkInItem = addItem($("DocumentContextMenu.checkIn"), FontAwesome.UNLOCK);
			checkInItem.addItemClickListener(new BaseContextMenuItemClickListener() {
				@Override
				public void contextMenuItemClicked(ContextMenuItemClickEvent event) {
					presenter.checkInButtonClicked();
				}
			});
		}

		if (alertWhenAvailableButtonVisible) {
			ContextMenuItem alertWhenAvailableItem = addItem($("DocumentContextMenu.alertWhenAvailable"), FontAwesome.BELL_O);
			alertWhenAvailableItem.addItemClickListener(new BaseContextMenuItemClickListener() {
				@Override
				public void contextMenuItemClicked(ContextMenuItemClickEvent event) {
					presenter.alertWhenAvailable();
				}
			});
		}

		if (checkOutButtonVisible) {
			ContextMenuItem checkOutItem = addItem($("DocumentContextMenu.checkOut"), FontAwesome.LOCK);
			checkOutItem.addItemClickListener(new BaseContextMenuItemClickListener() {
				@Override
				public void contextMenuItemClicked(ContextMenuItemClickEvent event) {
					presenter.checkOutButtonClicked(getSessionContext());
					refreshParent();
				}
			});
		}

		if (finalizeButtonVisible) {
			ContextMenuItem finalizeItem = addItem($("DocumentContextMenu.finalize"), FontAwesome.LEVEL_UP);
			finalizeItem.addItemClickListener(new ConfirmDialogContextMenuItemClickListener(DialogMode.INFO) {
				@Override
				protected String getConfirmDialogMessage() {
					return $("DocumentActionsComponent.finalize.confirm");
				}

				@Override
				protected void confirmButtonClick(ConfirmDialog dialog) {
					presenter.finalizeButtonClicked();
				}
			});
		}

		if (presenter.hasMetadataReport()) {
			ContextMenuItem metadataReportGenerator = addItem($("DocumentActionsComponent.printMetadataReportWithoutIcon"),
					FontAwesome.LIST_ALT);
			metadataReportGenerator.addItemClickListener(new BaseContextMenuItemClickListener() {

				@Override
				public void contextMenuItemClicked(ContextMenuItemClickEvent contextMenuItemClickEvent) {
					View parentView = ConstellioUI.getCurrent().getCurrentView();
					ReportTabButton button = new ReportTabButton($("DocumentActionsComponent.printMetadataReport"),
							$("DocumentActionsComponent.printMetadataReport"), (BaseView) parentView, true);
					button.setRecordVoList(presenter.getDocumentVO());
					button.click();
				}
			});
		}

	 */

	private boolean hasUserWriteAccess(Record record, User user) {
		return user.hasWriteAccess().on(record);
	}

	private boolean hasUserReadAccess(Record record, User user) {
		return user.hasReadAccess().on(record);
	}
}
