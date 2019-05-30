package com.constellio.app.modules.rm.services.actions;

import com.constellio.app.modules.rm.ConstellioRMModule;
import com.constellio.app.modules.rm.extensions.api.RMModuleExtensions;
import com.constellio.app.modules.rm.services.RMSchemasRecordsServices;
import com.constellio.app.modules.rm.wrappers.Document;
import com.constellio.app.services.factories.AppLayerFactory;
import com.constellio.model.entities.records.Record;
import com.constellio.model.entities.records.wrappers.User;

public class DocumentRecordActionsServices {

	private RMSchemasRecordsServices rm;
	private RMModuleExtensions rmModuleExtensions;

	public DocumentRecordActionsServices(String collection, AppLayerFactory appLayerFactory) {
		rm = new RMSchemasRecordsServices(collection, appLayerFactory);
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

	public boolean isPublishActionPossible(Record record, User user) {
		return false;
	}

	public boolean isPrintLabelActionPossible(Record record, User user) {
		return false;
	}

	public boolean isDeleteActionPossible(Record record, User user) {
		return false;
	}

	public boolean isCreatePdfActionPossible(Record record, User user) {
		return false;
	}

	public boolean isAddCartActionPossible(Record record, User user) {
		return false;
	}

	public boolean isAddSelectionActionPossible(Record record, User user) {
		return false;
	}

	public boolean isUploadActionPossible(Record record, User user) {
		return false;
	}

	public boolean isCheckInActionPossible(Record record, User user) {
		return false;
	}

	public boolean isCancelCheckInActionPossible(Record record, User user) {
		return false;
	}

	public boolean isCheckoutActionPossible(Record record, User user) {
		return false;
	}

	public boolean isGenerateReportActionPossible(Record record, User user) {
		return false;
	}

	public boolean isAddAuthorizationActionPossible(Record record, User user) {
		return false;
	}

	public boolean isFinalizeActionPossible(Record record, User user) {
		return false;
	}

	public boolean isStartWorkflowActionPossible(Record record, User user) {
		return false;
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
