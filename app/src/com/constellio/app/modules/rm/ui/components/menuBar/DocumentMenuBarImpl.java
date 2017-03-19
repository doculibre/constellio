package com.constellio.app.modules.rm.ui.components.menuBar;

import static com.constellio.app.ui.i18n.i18n.$;

import java.util.HashMap;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.vaadin.dialogs.ConfirmDialog;

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
import com.constellio.app.ui.framework.components.ComponentState;
import com.constellio.app.ui.framework.components.content.UpdateContentVersionWindowImpl;
import com.constellio.app.ui.framework.components.menuBar.ConfirmDialogMenuBarItemCommand;
import com.constellio.app.ui.pages.base.SessionContext;
import com.constellio.app.ui.pages.home.HomeViewImpl;
import com.constellio.app.ui.util.FileIconUtils;
import com.vaadin.navigator.View;
import com.vaadin.server.FontAwesome;
import com.vaadin.server.Page;
import com.vaadin.server.Resource;
import com.vaadin.ui.MenuBar;
import com.vaadin.ui.Notification;
import com.vaadin.ui.Notification.Type;

public class DocumentMenuBarImpl extends MenuBar implements DocumentMenuBar {
	
	private boolean visible = true;
	private RecordVO recordVO;
	private ContentVersionVO contentVersionVO;
	private UpdateContentVersionWindowImpl updateWindow;
	private String borrowedMessage;
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
	private View parentView;

	protected DocumentMenuBarPresenter presenter;

	public DocumentMenuBarImpl(DocumentVO documentVO) {
		presenter = newPresenter();
		setDocumentVO(documentVO);
		if (documentVO != null) {
			presenter.setRecordVO(documentVO);
		}
	}
	
	protected DocumentMenuBarPresenter newPresenter() {
		return new DocumentMenuBarPresenter(this);
	}

	public final boolean isVisible() {
		return visible;
	}

	public final void setVisible(boolean visible) {
		if (this.visible && !visible) {
			removeItems();
		}
		this.visible = visible;
	}
	
	@Override
	public void buildMenuItems() {
		removeItems();

		MenuItem rootItem = addItem("", FontAwesome.BARS, null);
		rootItem.setIcon(FontAwesome.BARS);
		
		if (StringUtils.isNotBlank(borrowedMessage)) {
			rootItem.addItem(borrowedMessage, null);
		}

		MenuItem displayDocumentItem = rootItem.addItem($("DocumentContextMenu.displayDocument"), null);
		displayDocumentItem.setCommand(new Command() {
			@Override
			public void menuSelected(MenuItem selectedItem) {
				presenter.displayDocumentButtonClicked();
			}
		});

		if (downloadDocumentButtonVisible) {
			String fileName = contentVersionVO.getFileName();
			Resource icon = FileIconUtils.getIcon(fileName);
			MenuItem downloadDocumentItem = rootItem.addItem($("DocumentContextMenu.openDocument"), icon, null);
			downloadDocumentItem.setCommand(new Command() {
				@Override
				public void menuSelected(MenuItem selectedItem) {
					String agentURL = ConstellioAgentUtils.getAgentURL(recordVO, contentVersionVO);
					Page.getCurrent().open(agentURL, "_top");
				}
			});
		}

		if (editDocumentButtonVisible) {
			MenuItem editDocumentItem = rootItem.addItem($("DocumentContextMenu.editDocument"), null);
			editDocumentItem.setCommand(new Command() {
				@Override
				public void menuSelected(MenuItem selectedItem) {
					presenter.editDocumentButtonClicked();
				}
			});
		}

		if (deleteDocumentButtonVisible) {
			MenuItem deleteDocumentItem = rootItem.addItem($("DocumentContextMenu.deleteDocument"), null);
			deleteDocumentItem.setCommand(new ConfirmDialogMenuBarItemCommand() {
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
			MenuItem addAuthorizationItem = rootItem.addItem($("DocumentActionsComponent.addAuthorization"), null);
			addAuthorizationItem.setCommand(new Command() {
				@Override
				public void menuSelected(MenuItem selectedItem) {
					presenter.addAuthorizationButtonClicked();
				}
			});
		}

		if (createPDFAButtonVisible) {
			MenuItem createPDFAItem = rootItem.addItem($("DocumentActionsComponent.createPDFA"), null);
			createPDFAItem.setCommand(new Command() {
				@Override
				public void menuSelected(MenuItem selectedItem) {
					presenter.createPDFA();
				}
			});
		}

		if (shareDocumentButtonVisible) {
			MenuItem shareDocumentItem = rootItem.addItem($("DocumentActionsComponent.shareDocument"), null);
			shareDocumentItem.setCommand(new Command() {
				@Override
				public void menuSelected(MenuItem selectedItem) {
					presenter.shareDocumentButtonClicked();
				}
			});
		}

		if (uploadButtonVisible) {
			MenuItem uploadItem = rootItem.addItem($("DocumentActionsComponent.upload"), null);
			uploadItem.setCommand(new Command() {
				@Override
				public void menuSelected(MenuItem selectedItem) {
					presenter.uploadButtonClicked();
				}
			});
		}

		if (checkInButtonVisible) {
			MenuItem checkInItem = rootItem.addItem($("DocumentActionsComponent.checkIn"), null);
			checkInItem.setCommand(new Command() {
				@Override
				public void menuSelected(MenuItem selectedItem) {
					presenter.checkInButtonClicked();
				}
			});
		}

		if (alertWhenAvailableButtonVisible) {
			MenuItem alertWhenAvailableItem = rootItem.addItem($("RMObject.alertWhenAvailable"), null);
			alertWhenAvailableItem.setCommand(new Command() {
				@Override
				public void menuSelected(MenuItem selectedItem) {
					presenter.alertWhenAvailable();
				}
			});
		}

		if (checkOutButtonVisible) {
			MenuItem checkOutItem = rootItem.addItem($("DocumentActionsComponent.checkOut"), null);
			checkOutItem.setCommand(new Command() {
				@Override
				public void menuSelected(MenuItem selectedItem) {
					presenter.checkOutButtonClicked(getSessionContext());
				}
			});
		}

		if (finalizeButtonVisible) {
			MenuItem finalizeItem = rootItem.addItem($("DocumentActionsComponent.finalize"), null);
			finalizeItem.setCommand(new ConfirmDialogMenuBarItemCommand() {
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
		if(parentView instanceof HomeViewImpl) {
			navigateTo().home("checkedOutDocuments");
		}
	}

	public void setParentView(View view) {
		parentView = view;
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
	public void setDownloadDocumentButtonVisible(boolean visible) {
		this.downloadDocumentButtonVisible = visible;
	}

	@Override
	public void openAgentURL(String agentURL) {
		Page.getCurrent().open(agentURL, null);
	}

}
