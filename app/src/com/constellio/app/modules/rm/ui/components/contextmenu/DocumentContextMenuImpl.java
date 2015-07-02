/*Constellio Enterprise Information Management

Copyright (c) 2015 "Constellio inc."

This program is free software: you can redistribute it and/or modify
it under the terms of the GNU Affero General Public License as
published by the Free Software Foundation, either version 3 of the
License, or (at your option) any later version.

This program is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
GNU Affero General Public License for more details.

You should have received a copy of the GNU Affero General Public License
along with this program. If not, see <http://www.gnu.org/licenses/>.
*/
package com.constellio.app.modules.rm.ui.components.contextmenu;

import static com.constellio.app.ui.i18n.i18n.$;

import org.apache.commons.lang3.StringUtils;
import org.vaadin.dialogs.ConfirmDialog;

import com.constellio.app.modules.rm.ui.entities.ComponentState;
import com.constellio.app.modules.rm.ui.util.ConstellioAgentUtils;
import com.constellio.app.modules.rm.wrappers.Document;
import com.constellio.app.services.factories.ConstellioFactories;
import com.constellio.app.ui.application.ConstellioNavigator;
import com.constellio.app.ui.application.ConstellioUI;
import com.constellio.app.ui.entities.ContentVersionVO;
import com.constellio.app.ui.entities.RecordVO;
import com.constellio.app.ui.framework.buttons.DownloadLink;
import com.constellio.app.ui.framework.components.content.ContentVersionVOResource;
import com.constellio.app.ui.framework.components.content.UpdateContentVersionWindowImpl;
import com.constellio.app.ui.framework.components.contextmenu.BaseContextMenuItemClickListener;
import com.constellio.app.ui.framework.components.contextmenu.ConfirmDialogContextMenuItemClickListener;
import com.constellio.app.ui.framework.components.contextmenu.RecordContextMenu;
import com.constellio.app.ui.pages.base.SessionContext;
import com.constellio.app.ui.util.FileIconUtils;
import com.vaadin.server.Page;
import com.vaadin.server.Resource;
import com.vaadin.ui.Notification;
import com.vaadin.ui.Notification.Type;

public class DocumentContextMenuImpl extends RecordContextMenu implements DocumentContextMenu {
	private boolean visible;
	private RecordVO recordVO;
	private ContentVersionVO contentVersionVO;
	private UpdateContentVersionWindowImpl updateWindow;
	private String borrowedMessage;
	private boolean downloadDocumentButtonVisible;
	private boolean editDocumentButtonVisible;
	private boolean deleteDocumentButtonVisible;
	private boolean addAuthorizationButtonVisible;
	private boolean shareDocumentButtonVisible;
	private boolean uploadButtonVisible;
	private boolean checkInButtonVisible;
	private boolean checkOutButtonVisible;
	//private boolean cancelCheckOutButtonVisible;
	private boolean finalizeButtonVisible;

	private DocumentContextMenuPresenter presenter;

	public DocumentContextMenuImpl() {
		this(null);
	}

	public DocumentContextMenuImpl(RecordVO recordVO) {
		presenter = new DocumentContextMenuPresenter(this);
		setRecordVO(recordVO);
		if (recordVO != null) {
			presenter.setRecordVO(recordVO);
		}
	}

	public final boolean isVisible() {
		return visible;
	}

	public final void setVisible(boolean visible) {
		if (this.visible && !visible) {
			removeAllItems();
		}
		this.visible = visible;
	}

	@Override
	public boolean openFor(String recordId) {
		return presenter.openForRequested(recordId);
	}

	@Override
	public boolean openFor(RecordVO recordVO) {
		return presenter.openForRequested(recordVO);
	}

	@Override
	public void buildMenuItems() {
		removeAllItems();

		if (StringUtils.isNotBlank(borrowedMessage)) {
			addItem(borrowedMessage);
		}

		ContextMenuItem displayDocumentButton = addItem($("DocumentContextMenu.displayDocument"));
		displayDocumentButton.addItemClickListener(new BaseContextMenuItemClickListener() {
			@Override
			public void contextMenuItemClicked(ContextMenuItemClickEvent event) {
				presenter.displayDocumentButtonClicked();
			}
		});

		if (downloadDocumentButtonVisible) {
			String fileName = contentVersionVO.getFileName();
			Resource icon = FileIconUtils.getIcon(fileName);
			ContextMenuItem downloadDocumentButton = addItem(contentVersionVO.toString(), icon);
			downloadDocumentButton.addItemClickListener(new BaseContextMenuItemClickListener() {
				@SuppressWarnings("deprecation")
				@Override
				public void contextMenuItemClicked(ContextMenuItemClickEvent event) {
					if (contentVersionVO != null) {
						String agentURL = ConstellioAgentUtils.getAgentURL(recordVO, contentVersionVO);
						if (agentURL != null) {
							Page.getCurrent().open(agentURL, null);
						} else {
							ContentVersionVOResource contentVersionResource = new ContentVersionVOResource(contentVersionVO);
							Resource downloadedResource = DownloadLink.wrapForDownload(contentVersionResource);
							Page.getCurrent().open(downloadedResource, null, false);
						}
					}
				}
			});
		}

		if (editDocumentButtonVisible) {
			ContextMenuItem editDocumentButton = addItem($("DocumentContextMenu.editDocument"));
			editDocumentButton.addItemClickListener(new BaseContextMenuItemClickListener() {
				@Override
				public void contextMenuItemClicked(ContextMenuItemClickEvent event) {
					presenter.editDocumentButtonClicked();
				}
			});
		}

		if (deleteDocumentButtonVisible) {
			ContextMenuItem deleteDocumentButton = addItem($("DocumentContextMenu.deleteDocument"));
			deleteDocumentButton.addItemClickListener(new ConfirmDialogContextMenuItemClickListener() {
				@Override
				protected String getConfirmDialogMessage() {
					return $("ConfirmDialog.confirmDelete");
				}

				@Override
				protected void confirmButtonClick(ConfirmDialog dialog) {
					presenter.deleteDocumentButtonClicked();
				}
			});
		}

		if (addAuthorizationButtonVisible) {
			ContextMenuItem addAuthorizationButton = addItem($("DocumentActionsComponent.addAuthorization"));
			addAuthorizationButton.addItemClickListener(new BaseContextMenuItemClickListener() {
				@Override
				public void contextMenuItemClicked(ContextMenuItemClickEvent event) {
					presenter.addAuthorizationButtonClicked();
				}
			});
		}

		if (shareDocumentButtonVisible) {
			ContextMenuItem shareDocument = addItem($("DocumentActionsComponent.shareDocument"));
			shareDocument.addItemClickListener(new BaseContextMenuItemClickListener() {
				@Override
				public void contextMenuItemClicked(ContextMenuItemClickEvent event) {
					presenter.shareDocumentButtonClicked();
				}
			});
		}

		if (uploadButtonVisible) {
			ContextMenuItem uploadButton = addItem($("DocumentActionsComponent.upload"));
			uploadButton.addItemClickListener(new BaseContextMenuItemClickListener() {
				@Override
				public void contextMenuItemClicked(ContextMenuItemClickEvent event) {
					presenter.uploadButtonClicked();
				}
			});
		}

		if (checkInButtonVisible) {
			ContextMenuItem checkInButton = addItem($("DocumentActionsComponent.checkIn"));
			checkInButton.addItemClickListener(new BaseContextMenuItemClickListener() {
				@Override
				public void contextMenuItemClicked(ContextMenuItemClickEvent event) {
					presenter.checkInButtonClicked();
				}
			});
		}

		if (checkOutButtonVisible) {
			ContextMenuItem checkOutButton = addItem($("DocumentActionsComponent.checkOut"));
			checkOutButton.addItemClickListener(new BaseContextMenuItemClickListener() {
				@Override
				public void contextMenuItemClicked(ContextMenuItemClickEvent event) {
					presenter.checkOutButtonClicked();
				}
			});
		}

		if (finalizeButtonVisible) {
			ContextMenuItem finalizeButton = addItem($("DocumentActionsComponent.finalize"));
			finalizeButton.addItemClickListener(new ConfirmDialogContextMenuItemClickListener() {
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
	}

	@Override
	public ConstellioFactories getConstellioFactories() {
		return ConstellioFactories.getInstance();
	}

	@Override
	public ConstellioNavigator navigateTo() {
		return ConstellioUI.getCurrent().navigateTo();
	}

	@Override
	public void showMessage(String message) {
		Notification.show(message, Type.WARNING_MESSAGE);
	}

	@Override
	public void showErrorMessage(String errorMessage) {
		Notification.show(errorMessage, Type.ERROR_MESSAGE);
	}

	@Override
	public SessionContext getSessionContext() {
		return ConstellioUI.getCurrentSessionContext();
	}

	@Override
	public void setRecordVO(RecordVO recordVO) {
		this.recordVO = recordVO;
	}

	private void initUploadWindow() {
		if (updateWindow == null) {
			updateWindow = new UpdateContentVersionWindowImpl(recordVO, recordVO.getMetadata(Document.CONTENT)) {
				@Override
				public void close() {
					super.close();
					presenter.updateWindowClosed();
				}
			};
		}
	}

	@Override
	public void openUploadWindow(boolean checkingIn) {
		initUploadWindow();
		updateWindow.open(checkingIn);
	}

	@Override
	public void setEditDocumentButtonState(ComponentState state) {
		editDocumentButtonVisible = state.isEnabled();
	}

	@Override
	public void setAddDocumentButtonState(ComponentState state) {
		//not supported
	}

	@Override
	public void setDeleteDocumentButtonState(ComponentState state) {
		deleteDocumentButtonVisible = state.isEnabled();
	}

	@Override
	public void setAddAuthorizationButtonState(ComponentState state) {
		addAuthorizationButtonVisible = state.isEnabled();
	}

	@Override
	public void setShareDocumentButtonState(ComponentState state) {
		shareDocumentButtonVisible = state.isEnabled();
	}

	@Override
	public void setUploadButtonState(ComponentState state) {
		uploadButtonVisible = state.isEnabled();
	}

	@Override
	public void setCheckInButtonState(ComponentState state) {
		checkInButtonVisible = state.isEnabled();
	}

	@Override
	public void setCheckOutButtonState(ComponentState state) {
		checkOutButtonVisible = state.isEnabled();
	}

	@Override
	public void setFinalizeButtonVisible(boolean visible) {
		finalizeButtonVisible = visible;
	}

	@Override
	public void setContentVersionVO(ContentVersionVO contentVersionVO) {
		this.contentVersionVO = contentVersionVO;
	}

	@Override
	public void setBorrowedMessage(String borrowedMessageKey, String... args) {
		if (StringUtils.isNotBlank(borrowedMessageKey)) {
			borrowedMessage = $(borrowedMessageKey, (Object[]) args);
		} else {
			borrowedMessage = null;
		}
	}

	@Override
	public void hide() {
		super.hide();
	}

	@Override
	public void open(int x, int y) {
		if (visible) {
			super.open(x, y);
		}
	}

	@Override
	public void setDownloadDocumentButtonVisible(boolean visible) {
		this.downloadDocumentButtonVisible = visible;
	}

}
