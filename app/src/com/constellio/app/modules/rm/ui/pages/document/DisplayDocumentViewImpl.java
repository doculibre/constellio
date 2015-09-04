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
package com.constellio.app.modules.rm.ui.pages.document;

import static com.constellio.app.ui.i18n.i18n.$;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.vaadin.dialogs.ConfirmDialog;

import com.constellio.app.modules.rm.ui.components.RMMetadataDisplayFactory;
import com.constellio.app.modules.rm.ui.components.breadcrumb.FolderDocumentBreadcrumbTrail;
import com.constellio.app.modules.rm.wrappers.Document;
import com.constellio.app.ui.entities.ContentVersionVO;
import com.constellio.app.ui.entities.RecordVO;
import com.constellio.app.ui.framework.buttons.ConfirmDialogButton;
import com.constellio.app.ui.framework.buttons.DeleteButton;
import com.constellio.app.ui.framework.buttons.EditButton;
import com.constellio.app.ui.framework.buttons.LinkButton;
import com.constellio.app.ui.framework.buttons.WindowButton;
import com.constellio.app.ui.framework.components.BaseForm;
import com.constellio.app.ui.framework.components.ComponentState;
import com.constellio.app.ui.framework.components.RecordDisplay;
import com.constellio.app.ui.framework.components.breadcrumb.BaseBreadcrumbTrail;
import com.constellio.app.ui.framework.components.content.UpdateContentVersionWindowImpl;
import com.constellio.app.ui.framework.components.table.ContentVersionVOTable;
import com.constellio.app.ui.framework.components.table.RecordVOTable;
import com.constellio.app.ui.framework.data.RecordVODataProvider;
import com.constellio.app.ui.framework.items.RecordVOItem;
import com.constellio.app.ui.pages.base.BaseViewImpl;
import com.vaadin.event.ItemClickEvent;
import com.vaadin.event.ItemClickEvent.ItemClickListener;
import com.vaadin.event.dd.DragAndDropEvent;
import com.vaadin.event.dd.DropHandler;
import com.vaadin.event.dd.acceptcriteria.AcceptAll;
import com.vaadin.event.dd.acceptcriteria.AcceptCriterion;
import com.vaadin.navigator.ViewChangeListener.ViewChangeEvent;
import com.vaadin.ui.Alignment;
import com.vaadin.ui.Button;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.Button.ClickListener;
import com.vaadin.ui.Component;
import com.vaadin.ui.CustomComponent;
import com.vaadin.ui.Label;
import com.vaadin.ui.TabSheet;
import com.vaadin.ui.Table;
import com.vaadin.ui.TextField;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.themes.ValoTheme;

public class DisplayDocumentViewImpl extends BaseViewImpl implements DisplayDocumentView, DropHandler {

	private VerticalLayout mainLayout;
	private Label borrowedLabel;
	private RecordVO recordVO;
	private TabSheet tabSheet;
	private RecordDisplay recordDisplay;
	private ContentVersionVOTable versionTable;
	private Component tasksComponent;
	private UpdateContentVersionWindowImpl uploadWindow;
	private EditButton editDocumentButton;
	private DeleteButton deleteDocumentButton;
	private Button copyContentButton;
	private WindowButton renameContentButton;

	private Button linkToDocumentButton, addAuthorizationButton, uploadButton, checkInButton, checkOutButton, finalizeButton, shareDocumentButton, createPDFAButton, alertWhenAvailableButton;

	private DisplayDocumentPresenter presenter;

	public DisplayDocumentViewImpl() {
		presenter = new DisplayDocumentPresenter(this);
	}

	@Override
	protected void initBeforeCreateComponents(ViewChangeEvent event) {
		presenter.forParams(event.getParameters());
	}

	@Override
	protected void afterViewAssembled(ViewChangeEvent event) {
		presenter.viewAssembled();
	}

	@Override
	public void setRecordVO(RecordVO recordVO) {
		this.recordVO = recordVO;
		if (recordDisplay != null) {
			recordDisplay.setRecordVO(recordVO);
		}
	}

	@Override
	public void setContentVersions(List<ContentVersionVO> contentVersions) {
		versionTable.setContentVersions(contentVersions);
	}

	@Override
	protected String getTitle() {
		//		return $("DisplayDocumentView.viewTitle") + " " + presenter.getDocumentTitle();
		return null;
	}

	@Override
	protected Component buildMainComponent(ViewChangeEvent event) {
		mainLayout = new VerticalLayout();
		mainLayout.setSizeFull();

		borrowedLabel = new Label();
		borrowedLabel.setVisible(false);
		borrowedLabel.addStyleName(ValoTheme.LABEL_COLORED);
		borrowedLabel.addStyleName(ValoTheme.LABEL_BOLD);

		tabSheet = new TabSheet();

		recordDisplay = new RecordDisplay(recordVO, new RMMetadataDisplayFactory());
		versionTable = new ContentVersionVOTable() {
			@Override
			protected boolean isDeleteColumn() {
				return presenter.isDeleteContentVersionPossible();
			}

			@Override
			protected boolean isDeletePossible(ContentVersionVO contentVersionVO) {
				return presenter.isDeleteContentVersionPossible(contentVersionVO);
			}

			@Override
			protected void deleteButtonClick(ContentVersionVO contentVersionVO) {
				presenter.deleteContentVersionButtonClicked(contentVersionVO);
			}
		};
		tasksComponent = new CustomComponent();
		versionTable.setSizeFull();

		tabSheet.addTab(recordDisplay, $("DisplayDocumentView.tabs.metadata"));
		tabSheet.addTab(versionTable, $("DisplayDocumentView.tabs.versions"));
		tabSheet.addTab(tasksComponent, $("DisplayDocumentView.tabs.tasks", presenter.getTaskCount()));

		Component disabled = new CustomComponent();
		tabSheet.addTab(disabled, $("DisplayDocumentView.tabs.logs"));
		tabSheet.getTab(disabled).setEnabled(false);

		mainLayout.addComponents(borrowedLabel, tabSheet);
		return mainLayout;
	}

	@Override
	protected BaseBreadcrumbTrail buildBreadcrumbTrail() {
		return new FolderDocumentBreadcrumbTrail(recordVO.getId());
	}

	@Override
	public void refreshMetadataDisplay() {
		recordDisplay.refresh();
	}

	@Override
	protected boolean isBackgroundViewMonitor() {
		return true;
	}

	@Override
	protected void onBackgroundViewMonitor() {
		presenter.backgroundViewMonitor();
	}

	@Override
	protected ClickListener getBackButtonClickListener() {
		return new ClickListener() {
			@Override
			public void buttonClick(ClickEvent event) {
				presenter.backButtonClicked();
			}
		};
	}

	@Override
	public void setTasks(RecordVODataProvider dataProvider) {
		Table tasksTable = new RecordVOTable(dataProvider);
		tasksTable.setSizeFull();
		tasksTable.addItemClickListener(new ItemClickListener() {
			@Override
			public void itemClick(ItemClickEvent event) {
				RecordVOItem item = (RecordVOItem) event.getItem();
				RecordVO recordVO = item.getRecord();
				presenter.taskClicked(recordVO);
			}
		});
		Component oldTasksComponent = tasksComponent;
		tasksComponent = tasksTable;
		tabSheet.replaceComponent(oldTasksComponent, tasksComponent);
	}

	@Override
	protected List<Button> buildActionMenuButtons(ViewChangeEvent event) {
		List<Button> actionMenuButtons = new ArrayList<Button>();

		editDocumentButton = new EditButton($("DisplayDocumentView.editDocument")) {
			@Override
			protected void buttonClick(ClickEvent event) {
				presenter.editDocumentButtonClicked();
			}
		};

		deleteDocumentButton = new DeleteButton($("DisplayDocumentView.deleteDocument")) {
			@Override
			protected void confirmButtonClick(ConfirmDialog dialog) {
				presenter.deleteDocumentButtonClicked();
			}
		};

		linkToDocumentButton = new LinkButton($("DocumentActionsComponent.linkToDocument")) {
			@Override
			protected void buttonClick(ClickEvent event) {
				presenter.linkToDocumentButtonClicked();
			}
		};
		linkToDocumentButton.setVisible(false);

		addAuthorizationButton = new LinkButton($("DocumentActionsComponent.addAuthorization")) {
			@Override
			protected void buttonClick(ClickEvent event) {
				presenter.addAuthorizationButtonClicked();
			}
		};

		createPDFAButton = new LinkButton($("DocumentActionsComponent.createPDFA")) {
			@Override
			protected void buttonClick(ClickEvent event) {
				presenter.createPDFAButtonClicked();
			}
		};

		shareDocumentButton = new LinkButton($("DocumentActionsComponent.shareDocument")) {
			@Override
			protected void buttonClick(ClickEvent event) {
				presenter.shareDocumentButtonClicked();
			}
		};

		uploadButton = new LinkButton($("DocumentActionsComponent.upload")) {
			@Override
			protected void buttonClick(ClickEvent event) {
				presenter.uploadButtonClicked();
			}
		};

		checkInButton = new LinkButton($("DocumentActionsComponent.checkIn")) {
			@Override
			protected void buttonClick(ClickEvent event) {
				presenter.checkInButtonClicked();
			}
		};

		alertWhenAvailableButton = new LinkButton($("RMObject.alertWhenAvailable")) {
			@Override
			protected void buttonClick(ClickEvent event) {
				presenter.alertWhenAvailableClicked();
			}
		};

		checkOutButton = new LinkButton($("DocumentActionsComponent.checkOut")) {
			@Override
			protected void buttonClick(ClickEvent event) {
				presenter.checkOutButtonClicked();
			}
		};

		finalizeButton = new ConfirmDialogButton(null, $("DocumentActionsComponent.finalize")) {
			@Override
			protected String getConfirmDialogMessage() {
				return $("DocumentActionsComponent.finalize.confirm");
			}

			@Override
			protected void confirmButtonClick(ConfirmDialog dialog) {
				presenter.finalizeButtonClicked();
			}
		};
		finalizeButton.addStyleName(ValoTheme.BUTTON_LINK);

		actionMenuButtons.add(editDocumentButton);
		if (presenter.hasContent()) {
			WindowButton.WindowConfiguration config = new WindowButton.WindowConfiguration(true, false, "40%", "20%");
			renameContentButton = new WindowButton($("DocumentContextMenu.renameContent"), "", config) {
				@Override
				protected Component buildWindowContent() {
					VerticalLayout layout = new VerticalLayout();
					layout.setSizeFull();
					final TextField titleField = new TextField($("DisplayDocumentView.title"));
					String fileTitle = presenter.getContentTitle();
					titleField.setValue(fileTitle);
					layout.addComponent(titleField);
					layout.setComponentAlignment(titleField, Alignment.MIDDLE_CENTER);
					Component confirmButton = new ConfirmDialogButton($("DisplayDocumentView.renameContentConfirm")) {
						@Override
						protected String getConfirmDialogMessage() {
							return $("DisplayDocumentView.renameContentConfirmMessage");
						}

						@Override
						protected void confirmButtonClick(ConfirmDialog dialog) {
							presenter.renameContentButtonClicked(titleField.getValue());
							renameContentButton.getWindow().close();
						}
					};
					confirmButton.addStyleName(ValoTheme.BUTTON_PRIMARY);
					confirmButton.addStyleName(BaseForm.SAVE_BUTTON);
					layout.addComponent(confirmButton);
					layout.setComponentAlignment(confirmButton, Alignment.BOTTOM_RIGHT);
					return layout;
				}

			};

			copyContentButton = new LinkButton($("DocumentContextMenu.copyContent")) {
				@Override
				protected void buttonClick(ClickEvent event) {
					presenter.copyContentButtonClicked();
				}
			};
			actionMenuButtons.add(renameContentButton);
			actionMenuButtons.add(copyContentButton);
		}

		actionMenuButtons.add(deleteDocumentButton);
		actionMenuButtons.add(linkToDocumentButton);
		actionMenuButtons.add(addAuthorizationButton);
		actionMenuButtons.add(createPDFAButton);
		actionMenuButtons.add(shareDocumentButton);
		actionMenuButtons.add(uploadButton);
		actionMenuButtons.add(checkInButton);
		actionMenuButtons.add(alertWhenAvailableButton);
		actionMenuButtons.add(checkOutButton);
		actionMenuButtons.add(finalizeButton);

		return actionMenuButtons;
	}

	private void initUploadWindow() {
		if (uploadWindow == null) {
			if (recordVO != null) {
				uploadWindow = new UpdateContentVersionWindowImpl(recordVO, recordVO.getMetadata(Document.CONTENT)) {
					@Override
					public void close() {
						super.close();
						presenter.updateWindowClosed();
					}
				};
			}
		}
	}

	@Override
	public void drop(DragAndDropEvent event) {
		openUploadWindow(false);
		uploadWindow.drop(event);
	}

	@Override
	public AcceptCriterion getAcceptCriterion() {
		initUploadWindow();
		if (uploadWindow != null) {
			return uploadWindow.getAcceptCriterion();
		} else {
			return AcceptAll.get();
		}

	}

	@Override
	public void openUploadWindow(boolean checkingIn) {
		uploadWindow = null;
		initUploadWindow();
		uploadWindow.open(checkingIn);
	}

	@Override
	public void setUploadButtonState(ComponentState state) {
		uploadButton.setVisible(state.isVisible());
		uploadButton.setEnabled(state.isEnabled());
	}

	@Override
	public void setCheckInButtonState(ComponentState state) {
		checkInButton.setVisible(state.isVisible());
		checkInButton.setEnabled(state.isEnabled());
	}

	@Override
	public void setAlertWhenAvailableButtonState(ComponentState state) {
		alertWhenAvailableButton.setVisible(state.isVisible());
		alertWhenAvailableButton.setEnabled(state.isEnabled());
	}

	@Override
	public void setCheckOutButtonState(ComponentState state) {
		checkOutButton.setVisible(state.isVisible());
		checkOutButton.setEnabled(state.isEnabled());
	}

	@Override
	public void setFinalizeButtonVisible(boolean visible) {
		finalizeButton.setVisible(visible);
	}

	@Override
	public void setEditDocumentButtonState(ComponentState state) {
		editDocumentButton.setVisible(state.isVisible());
		editDocumentButton.setEnabled(state.isEnabled());
		if (renameContentButton != null) {
			renameContentButton.setVisible(state.isVisible());
			renameContentButton.setEnabled(state.isEnabled());
		}
	}

	@Override
	public void setAddDocumentButtonState(ComponentState state) {
		//nothing to set only from context
		if (copyContentButton != null) {
			copyContentButton.setVisible(state.isVisible());
			copyContentButton.setEnabled(state.isEnabled());
		}
	}

	@Override
	public void setDeleteDocumentButtonState(ComponentState state) {
		deleteDocumentButton.setVisible(state.isVisible());
		deleteDocumentButton.setEnabled(state.isEnabled());
	}

	@Override
	public void setAddAuthorizationButtonState(ComponentState state) {
		addAuthorizationButton.setVisible(state.isVisible());
		addAuthorizationButton.setEnabled(state.isEnabled());
	}

	@Override
	public void setShareDocumentButtonState(ComponentState state) {
		shareDocumentButton.setVisible(state.isVisible());
		shareDocumentButton.setEnabled(state.isEnabled());
	}

	@Override
	public void setCreatePDFAButtonState(ComponentState state) {
		createPDFAButton.setVisible(state.isVisible());
		createPDFAButton.setEnabled(state.isEnabled());
	}

	@Override
	public void setBorrowedMessage(String borrowedMessageKey, String... args) {
		if (StringUtils.isNotBlank(borrowedMessageKey)) {
			borrowedLabel.setVisible(true);
			borrowedLabel.setValue($(borrowedMessageKey, (Object[]) args));
		} else {
			borrowedLabel.setVisible(false);
			borrowedLabel.setValue(null);
		}
	}

}
