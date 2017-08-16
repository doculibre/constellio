package com.constellio.app.modules.rm.ui.components.contextmenu;

import static com.constellio.app.ui.i18n.i18n.$;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.vaadin.dialogs.ConfirmDialog;

import com.constellio.app.modules.rm.navigation.RMNavigationConfiguration;
import com.constellio.app.modules.rm.ui.entities.DocumentVO;
import com.constellio.app.modules.rm.ui.util.ConstellioAgentUtils;
import com.constellio.app.modules.rm.wrappers.Document;
import com.constellio.app.services.factories.ConstellioFactories;
import com.constellio.app.ui.application.ConstellioUI;
import com.constellio.app.ui.application.CoreViews;
import com.constellio.app.ui.application.Navigation;
import com.constellio.app.ui.entities.ContentVersionVO;
import com.constellio.app.ui.entities.MetadataVO;
import com.constellio.app.ui.entities.RecordVO;
import com.constellio.app.ui.framework.buttons.ConfirmDialogButton.DialogMode;
import com.constellio.app.ui.framework.buttons.DownloadLink;
import com.constellio.app.ui.framework.components.ComponentState;
import com.constellio.app.ui.framework.components.content.ContentVersionVOResource;
import com.constellio.app.ui.framework.components.content.UpdateContentVersionWindowImpl;
import com.constellio.app.ui.framework.components.contextmenu.BaseContextMenuItemClickListener;
import com.constellio.app.ui.framework.components.contextmenu.ConfirmDialogContextMenuItemClickListener;
import com.constellio.app.ui.framework.components.contextmenu.RecordContextMenu;
import com.constellio.app.ui.pages.base.SessionContext;
import com.constellio.app.ui.pages.base.UIContext;
import com.constellio.app.ui.pages.home.HomeViewImpl;
import com.constellio.app.ui.util.FileIconUtils;
import com.vaadin.navigator.View;
import com.vaadin.server.Page;
import com.vaadin.server.Resource;
import com.vaadin.ui.Component;
import com.vaadin.ui.Notification;
import com.vaadin.ui.Notification.Type;

public class DocumentContextMenuImpl extends RecordContextMenu implements DocumentContextMenu {
	
	private boolean visible;
	private RecordVO recordVO;
	private ContentVersionVO contentVersionVO;
	private UpdateContentVersionWindowImpl updateWindow;
	private String borrowedMessage;
	private boolean openDocumentButtonVisible;
	private boolean downloadDocumentButtonVisible;
	private boolean editDocumentButtonVisible;
	private boolean deleteDocumentButtonVisible;
	private boolean addAuthorizationButtonVisible;
	private boolean shareDocumentButtonVisible;
	private boolean createPDFAButtonVisible;
	private boolean uploadButtonVisible;
	private boolean checkInButtonVisible;
	private boolean alertWhenAvailableButtonVisible;
	private boolean checkOutButtonVisible;
	//private boolean cancelCheckOutButtonVisible;
	private boolean finalizeButtonVisible;

	protected DocumentContextMenuPresenter presenter;

	public DocumentContextMenuImpl() {
		this(null);
	}

	public DocumentContextMenuImpl(DocumentVO documentVO) {
		presenter = newPresenter();
		setDocumentVO(documentVO);
		if (documentVO != null) {
			presenter.setRecordVO(documentVO);
		}
	}
	
	protected DocumentContextMenuPresenter newPresenter() {
		return new DocumentContextMenuPresenter(this);
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

		ContextMenuItem displayDocumentItem = addItem($("DocumentContextMenu.displayDocument"));
		displayDocumentItem.addItemClickListener(new BaseContextMenuItemClickListener() {
			@Override
			public void contextMenuItemClicked(ContextMenuItemClickEvent event) {
				presenter.displayDocumentButtonClicked();
			}
		});

		if (openDocumentButtonVisible) {
			String fileName = contentVersionVO.getFileName();
			Resource icon = FileIconUtils.getIcon(fileName);
			ContextMenuItem downloadDocumentItem = addItem($("DocumentContextMenu.openDocument"), icon);
			downloadDocumentItem.addItemClickListener(new BaseContextMenuItemClickListener() {
				@Override
				public void contextMenuItemClicked(ContextMenuItemClickEvent event) {
					String agentURL = ConstellioAgentUtils.getAgentURL(recordVO, contentVersionVO);
					openAgentURL(agentURL);
				}
			});
		}

		if (downloadDocumentButtonVisible) {
			ContextMenuItem downloadDocumentItem = addItem($("DocumentContextMenu.downloadDocument"));
			downloadDocumentItem.addItemClickListener(new BaseContextMenuItemClickListener() {
				@SuppressWarnings("deprecation")
				@Override
				public void contextMenuItemClicked(ContextMenuItemClickEvent event) {
					ContentVersionVOResource contentVersionResource = new ContentVersionVOResource(contentVersionVO);
					Resource downloadedResource = DownloadLink.wrapForDownload(contentVersionResource);
					Page.getCurrent().open(downloadedResource, null, false);
				}
			});
		}

		if (editDocumentButtonVisible) {
			ContextMenuItem editDocumentItem = addItem($("DocumentContextMenu.editDocument"));
			editDocumentItem.addItemClickListener(new BaseContextMenuItemClickListener() {
				@Override
				public void contextMenuItemClicked(ContextMenuItemClickEvent event) {
					presenter.editDocumentButtonClicked();
				}
			});
		}

		if (deleteDocumentButtonVisible) {
			ContextMenuItem deleteDocumentItem = addItem($("DocumentContextMenu.deleteDocument"));
			deleteDocumentItem.addItemClickListener(new ConfirmDialogContextMenuItemClickListener(DialogMode.INFO) {
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
			ContextMenuItem addAuthorizationItem = addItem($("DocumentActionsComponent.addAuthorization"));
			addAuthorizationItem.addItemClickListener(new BaseContextMenuItemClickListener() {
				@Override
				public void contextMenuItemClicked(ContextMenuItemClickEvent event) {
					presenter.addAuthorizationButtonClicked();
				}
			});
		}

		if (createPDFAButtonVisible) {
			ContextMenuItem createPDFAItem = addItem($("DocumentActionsComponent.createPDFA"));
			createPDFAItem.addItemClickListener(new ConfirmDialogContextMenuItemClickListener(DialogMode.WARNING) {
				@Override
				protected String getConfirmDialogMessage() {
					return $("ConfirmDialog.confirmCreatePDFA");
				}

				@Override
				protected void confirmButtonClick(ConfirmDialog dialog) {
					presenter.createPDFA();
				}
			});
		}

		if (shareDocumentButtonVisible) {
			ContextMenuItem shareDocumentItem = addItem($("DocumentActionsComponent.shareDocument"));
			shareDocumentItem.addItemClickListener(new BaseContextMenuItemClickListener() {
				@Override
				public void contextMenuItemClicked(ContextMenuItemClickEvent event) {
					presenter.shareDocumentButtonClicked();
				}
			});
		}

		if (uploadButtonVisible) {
			ContextMenuItem uploadItem = addItem($("DocumentActionsComponent.upload"));
			uploadItem.addItemClickListener(new BaseContextMenuItemClickListener() {
				@Override
				public void contextMenuItemClicked(ContextMenuItemClickEvent event) {
					presenter.uploadButtonClicked();
				}
			});
		}

		if (checkInButtonVisible) {
			ContextMenuItem checkInItem = addItem($("DocumentActionsComponent.checkIn"));
			checkInItem.addItemClickListener(new BaseContextMenuItemClickListener() {
				@Override
				public void contextMenuItemClicked(ContextMenuItemClickEvent event) {
					presenter.checkInButtonClicked();
				}
			});
		}

		if (alertWhenAvailableButtonVisible) {
			ContextMenuItem alertWhenAvailableItem = addItem($("RMObject.alertWhenAvailable"));
			alertWhenAvailableItem.addItemClickListener(new BaseContextMenuItemClickListener() {
				@Override
				public void contextMenuItemClicked(ContextMenuItemClickEvent event) {
					presenter.alertWhenAvailable();
				}
			});
		}

		if (checkOutButtonVisible) {
			ContextMenuItem checkOutItem = addItem($("DocumentActionsComponent.checkOut"));
			checkOutItem.addItemClickListener(new BaseContextMenuItemClickListener() {
				@Override
				public void contextMenuItemClicked(ContextMenuItemClickEvent event) {
					presenter.checkOutButtonClicked(getSessionContext());
					refreshParent();
				}
			});
		}

		if (finalizeButtonVisible) {
			ContextMenuItem finalizeItem = addItem($("DocumentActionsComponent.finalize"));
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
	}

	@Override
	public ConstellioFactories getConstellioFactories() {
		return ConstellioFactories.getInstance();
	}

	@Override
	public CoreViews navigateTo() {
		return ConstellioUI.getCurrent().navigateTo();
	}

	@Override
	public Navigation navigate() {
		return ConstellioUI.getCurrent().navigate();
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
	public UIContext getUIContext() {
		return ConstellioUI.getCurrent();
	}

	@Override
	public void setDocumentVO(DocumentVO documentVO) {
		this.recordVO = documentVO;
	}

	private void initUploadWindow() {
		Map<RecordVO, MetadataVO> record = new HashMap<>();
		record.put(recordVO, recordVO.getMetadata(Document.CONTENT));
		updateWindow = new UpdateContentVersionWindowImpl(record) {
			@Override
			public void close() {
				super.close();
				presenter.updateWindowClosed();
				postClose();
			}
		};
	}

	public void postClose() {
		refreshParent();
	}

	@Override
	public void openUploadWindow(boolean checkingIn) {
		initUploadWindow();
		updateWindow.open(checkingIn);
	}

	@Override
	public void setStartWorkflowButtonState(ComponentState state) {

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
	public void setCreatePDFAButtonState(ComponentState state) {
		createPDFAButtonVisible = state.isEnabled();
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
	public void setAlertWhenAvailableButtonState(ComponentState state) {
		alertWhenAvailableButtonVisible = state.isEnabled();
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
	public void open(Component component) {
		super.open(component);
	}

	@Override
	public void open(int x, int y) {
		if (visible) {
			super.open(x, y);
		}
	}

	@Override
	public void setOpenDocumentButtonVisible(boolean visible) {
		this.openDocumentButtonVisible = visible;
	}

	@Override
	public void setDownloadDocumentButtonVisible(boolean visible) {
		this.downloadDocumentButtonVisible = visible;
	}

	@Override
	public void openAgentURL(String agentURL) {
		Page.getCurrent().open(agentURL, "_top");
	}

	@Override
	public void refreshParent() {
		View parentView = ConstellioUI.getCurrent().getCurrentView();
		if (parentView instanceof HomeViewImpl) {
			HomeViewImpl homeView = (HomeViewImpl) parentView;
			String selectedTabCode = homeView.getSelectedTabCode();
			if (Arrays.asList(
					RMNavigationConfiguration.CHECKED_OUT_DOCUMENTS, 
					RMNavigationConfiguration.LAST_VIEWED_DOCUMENTS).contains(selectedTabCode)) {
				navigateTo().home(selectedTabCode);
			}
		}
	}
	
}
