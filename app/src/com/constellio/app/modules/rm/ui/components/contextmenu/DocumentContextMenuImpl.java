package com.constellio.app.modules.rm.ui.components.contextmenu;

import com.constellio.app.modules.rm.ui.components.document.DocumentActionsPresenterUtils;
import com.constellio.app.modules.rm.ui.entities.DocumentVO;
import com.constellio.app.modules.rm.ui.pages.document.DisplayDocumentView;
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
import com.constellio.app.ui.framework.components.ReportTabButton;
import com.constellio.app.ui.framework.components.content.ContentVersionVOResource;
import com.constellio.app.ui.framework.components.content.UpdateContentVersionWindowImpl;
import com.constellio.app.ui.framework.components.contextmenu.BaseContextMenuItemClickListener;
import com.constellio.app.ui.framework.components.contextmenu.ConfirmDialogContextMenuItemClickListener;
import com.constellio.app.ui.framework.components.contextmenu.RecordContextMenu;
import com.constellio.app.ui.framework.containers.RefreshableContainer;
import com.constellio.app.ui.pages.base.BaseView;
import com.constellio.app.ui.pages.base.BaseViewImpl;
import com.constellio.app.ui.pages.base.SessionContext;
import com.constellio.app.ui.pages.base.UIContext;
import com.constellio.app.ui.params.ParamUtils;
import com.constellio.app.ui.util.FileIconUtils;
import com.vaadin.data.Container;
import com.vaadin.navigator.View;
import com.vaadin.server.ClientConnector;
import com.vaadin.server.FontAwesome;
import com.vaadin.server.Page;
import com.vaadin.server.Resource;
import com.vaadin.ui.Component;
import com.vaadin.ui.Notification;
import com.vaadin.ui.Notification.Type;
import com.vaadin.ui.Table;
import com.vaadin.ui.Window;
import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.Nullable;
import org.vaadin.dialogs.ConfirmDialog;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import static com.constellio.app.ui.i18n.i18n.$;

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
	private boolean metadataReportButtonVisible;
	//private boolean cancelCheckOutButtonVisible;
	private boolean finalizeButtonVisible;
	private boolean addToCartButtonVisible;
	private boolean publishButtonVisible;
	private boolean addToOrRemoveFromSelectionButtonVisible;

	private DocumentActionsPresenterUtils<DocumentContextMenu> presenterUtils;
	protected DocumentContextMenuPresenter presenter;

	public DocumentContextMenuImpl() {
		this(null);
	}

	public DocumentContextMenuImpl(DocumentVO documentVO) {
		presenter = newPresenter();
		setRecordVO(documentVO);
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

		ContextMenuItem displayDocumentItem = addItem($("DocumentContextMenu.displayDocument"), FontAwesome.FILE_O);
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
					button.setRecordVoList(presenter.getRecordVO());
					button.click();
				}
			});
		}

		View parentView = ConstellioUI.getCurrent().getCurrentView();
		presenter.addItemsFromExtensions(this, (BaseViewImpl) parentView);
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
		Navigation navigation = ConstellioUI.getCurrent().navigate();
		for (Window window : new ArrayList<Window>(ConstellioUI.getCurrent().getWindows())) {
			window.close();
		}
		return navigation;
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
	public void setRecordVO(RecordVO documentVO) {
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
	public void setCopyDocumentButtonState(ComponentState state) {
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
	public void setCartButtonState(ComponentState state) {
		addToCartButtonVisible = state.isVisible();
	}

	@Override
	public void setAddToOrRemoveFromSelectionButtonState(ComponentState state) {
		addToOrRemoveFromSelectionButtonVisible = state.isVisible();
	}

	@Override
	public void setGenerateMetadataButtonState(ComponentState state) {
		metadataReportButtonVisible = presenter.hasMetadataReport();
	}

	@Override
	public void setPublishButtonState(ComponentState state) {
		publishButtonVisible = state.isVisible();
	}

	@Override
	public void setFinalizeButtonState(ComponentState state) {
		finalizeButtonVisible = state.isVisible();
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
		ClientConnector parent = getParent();
		if (parent instanceof Table) {
			Container container = ((Table) parent).getContainerDataSource();
			if (container instanceof RefreshableContainer) {
				((RefreshableContainer) container).refresh();
			}
		}
	}

}
