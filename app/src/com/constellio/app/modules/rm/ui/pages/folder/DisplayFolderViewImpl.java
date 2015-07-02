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
package com.constellio.app.modules.rm.ui.pages.folder;

import static com.constellio.app.ui.i18n.i18n.$;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import com.constellio.app.modules.rm.model.labelTemplate.LabelTemplate;
import com.constellio.app.modules.rm.ui.components.RMMetadataDisplayFactory;
import com.constellio.app.modules.rm.ui.entities.ComponentState;
import com.constellio.app.ui.entities.ContentVersionVO;
import com.constellio.app.ui.entities.RecordVO;
import com.constellio.app.ui.framework.buttons.AddButton;
import com.constellio.app.ui.framework.buttons.BaseButton;
import com.constellio.app.ui.framework.buttons.DeleteWithJustificationButton;
import com.constellio.app.ui.framework.buttons.EditButton;
import com.constellio.app.ui.framework.buttons.LabelsButton;
import com.constellio.app.ui.framework.buttons.LabelsButton.RecordSelector;
import com.constellio.app.ui.framework.buttons.LinkButton;
import com.constellio.app.ui.framework.buttons.WindowButton;
import com.constellio.app.ui.framework.components.RecordDisplay;
import com.constellio.app.ui.framework.components.fields.date.JodaDateField;
import com.constellio.app.ui.framework.components.fields.lookup.LookupRecordField;
import com.constellio.app.ui.framework.components.fields.upload.ContentVersionUploadField;
import com.constellio.app.ui.framework.components.table.RecordVOTable;
import com.constellio.app.ui.framework.data.RecordVODataProvider;
import com.constellio.app.ui.framework.items.RecordVOItem;
import com.constellio.app.ui.pages.base.BaseViewImpl;
import com.constellio.data.utils.Factory;
import com.constellio.model.entities.records.wrappers.User;
import com.vaadin.data.Property.ValueChangeEvent;
import com.vaadin.data.Property.ValueChangeListener;
import com.vaadin.event.ItemClickEvent;
import com.vaadin.event.ItemClickEvent.ItemClickListener;
import com.vaadin.event.dd.DragAndDropEvent;
import com.vaadin.event.dd.DropHandler;
import com.vaadin.event.dd.acceptcriteria.AcceptAll;
import com.vaadin.event.dd.acceptcriteria.AcceptCriterion;
import com.vaadin.navigator.ViewChangeListener.ViewChangeEvent;
import com.vaadin.shared.MouseEventDetails.MouseButton;
import com.vaadin.ui.Button;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.Button.ClickListener;
import com.vaadin.ui.Component;
import com.vaadin.ui.CustomComponent;
import com.vaadin.ui.Field;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Label;
import com.vaadin.ui.TabSheet;
import com.vaadin.ui.Table;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.themes.ValoTheme;

public class DisplayFolderViewImpl extends BaseViewImpl implements DisplayFolderView, DropHandler {
	public static final String STYLE_NAME = "display-folder";
	public static final String USER_LOOKUP = "user-lookup";
	private RecordVO recordVO;
	private VerticalLayout mainLayout;
	private ContentVersionUploadField uploadField;
	private TabSheet tabSheet;
	private RecordDisplay recordDisplay;
	private Component documentsComponent;
	private Component subFoldersComponent;
	private DisplayFolderPresenter presenter;
	private boolean dragNDropAllowed;
	private Button deleteFolderButton, duplicateFolderButton,
			editFolderButton, addSubFolderButton, addDocumentButton, addAuthorizationButton, shareFolderButton,
			printLabelButton, linkToFolderButton, borrowButton, returnFolderButton;
	private Label borrowedLabel;

	public DisplayFolderViewImpl() {
		presenter = new DisplayFolderPresenter(this);
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
	public void setRecord(RecordVO recordVO) {
		this.recordVO = recordVO;
	}

	@Override
	protected String getTitle() {
		return $("DisplayFolderView.viewTitle") + " " + presenter.getFolderTitle();
	}

	@Override
	protected Component buildMainComponent(ViewChangeEvent event) {
		mainLayout = new VerticalLayout();
		mainLayout.setSizeFull();
		mainLayout.setSpacing(true);

		uploadField = new ContentVersionUploadField();
		uploadField.setVisible(false);
		uploadField.setImmediate(true);
		uploadField.setMultiValue(false);
		uploadField.addValueChangeListener(new ValueChangeListener() {
			@Override
			public void valueChange(ValueChangeEvent event) {
				ContentVersionVO uploadedContentVO = (ContentVersionVO) uploadField.getValue();
				presenter.contentVersionUploaded(uploadedContentVO);
			}
		});

		recordDisplay = new RecordDisplay(recordVO, new RMMetadataDisplayFactory());
		documentsComponent = new CustomComponent();
		subFoldersComponent = new CustomComponent();

		tabSheet = new TabSheet();
		tabSheet.addStyleName(STYLE_NAME);
		tabSheet.addTab(recordDisplay, $("DisplayFolderView.tabs.metadata"));
		tabSheet.addTab(documentsComponent, $("DisplayFolderView.tabs.documents"));
		tabSheet.addTab(subFoldersComponent, $("DisplayFolderView.tabs.subFolders"));

		Component disabled = new CustomComponent();
		tabSheet.addTab(disabled, $("DisplayFolderView.tabs.logs"));
		tabSheet.getTab(disabled).setEnabled(false);

		borrowedLabel = new Label();
		borrowedLabel.setVisible(false);
		borrowedLabel.addStyleName(ValoTheme.LABEL_COLORED);
		borrowedLabel.addStyleName(ValoTheme.LABEL_BOLD);

		mainLayout.addComponents(borrowedLabel, uploadField, tabSheet);
		presenter.selectInitialTabForUser();
		return mainLayout;
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
	protected List<Button> buildActionMenuButtons(ViewChangeEvent event) {
		List<Button> actionMenuButtons = new ArrayList<Button>();

		addDocumentButton = new AddButton($("DisplayFolderView.addDocument")) {
			@Override
			protected void buttonClick(ClickEvent event) {
				presenter.addDocumentButtonClicked();
			}
		};

		addSubFolderButton = new AddButton($("DisplayFolderView.addSubFolder")) {
			@Override
			protected void buttonClick(ClickEvent event) {
				presenter.addSubFolderButtonClicked();
			}
		};

		editFolderButton = new EditButton($("DisplayFolderView.editFolder")) {
			@Override
			protected void buttonClick(ClickEvent event) {
				presenter.editFolderButtonClicked();
			}
		};

		deleteFolderButton = new DeleteWithJustificationButton($("DisplayFolderView.deleteFolder"), false) {
			@Override
			protected void deletionConfirmed(String reason) {
				presenter.deleteFolderButtonClicked(reason);
			}
		};

		duplicateFolderButton = new WindowButton($("DisplayFolderView.duplicateFolder"),
				"Dupliquer seulement le dossier ou son arborescence au complet?") {
			@Override
			protected Component buildWindowContent() {

				HorizontalLayout layout = new HorizontalLayout();

				BaseButton folderButton = new BaseButton("Dossier") {
					@Override
					protected void buttonClick(ClickEvent event) {
						presenter.duplicateFolderButtonClicked();
						getWindow().close();
					}
				};
				folderButton.addStyleName(ValoTheme.BUTTON_PRIMARY);

				BaseButton structureButton = new BaseButton("Arborescence") {
					@Override
					protected void buttonClick(ClickEvent event) {
						presenter.duplicateStructureButtonClicked();
						getWindow().close();
					}
				};
				structureButton.addStyleName(ValoTheme.BUTTON_PRIMARY);

				BaseButton cancelButton = new BaseButton("Annuler") {
					@Override
					protected void buttonClick(ClickEvent event) {
						getWindow().close();
					}
				};
				cancelButton.addStyleName(ValoTheme.BUTTON_PRIMARY);

				layout.addComponents(folderButton, structureButton, cancelButton);
				layout.setSpacing(true);

				return layout;
			}
		};

		linkToFolderButton = new LinkButton($("DisplayFolderView.linkToFolder")) {
			@Override
			protected void buttonClick(ClickEvent event) {
				presenter.linkToFolderButtonClicked();
			}
		};
		linkToFolderButton.setVisible(false);

		addAuthorizationButton = new LinkButton($("DisplayFolderView.addAuthorization")) {
			@Override
			protected void buttonClick(ClickEvent event) {
				presenter.addAuthorizationButtonClicked();
			}
		};

		shareFolderButton = new LinkButton($("DisplayFolderView.shareFolder")) {
			@Override
			protected void buttonClick(ClickEvent event) {
				presenter.shareFolderButtonClicked();
			}
		};

		Factory<List<LabelTemplate>> labelTemplatesFactory = new Factory<List<LabelTemplate>>() {
			@Override
			public List<LabelTemplate> get() {
				return presenter.getTemplates();
			}
		};
		printLabelButton = new LabelsButton(
				$("DisplayFolderView.printLabel"), $("DisplayFolderView.printLabel"),
				new RecordSelector() {
					@Override
					public List<String> getSelectedRecordIds() {
						return Arrays.asList(recordVO.getId());
					}
				}, labelTemplatesFactory);

		borrowButton = buildBorrowButton();

		returnFolderButton = new BaseButton($("DisplayFolderView.returnDossier")) {
			@Override
			protected void buttonClick(ClickEvent event) {
				presenter.returnFolder();
			}
		};

		actionMenuButtons.add(addDocumentButton);
		actionMenuButtons.add(addSubFolderButton);
		actionMenuButtons.add(editFolderButton);
		actionMenuButtons.add(deleteFolderButton);
		actionMenuButtons.add(duplicateFolderButton);
		actionMenuButtons.add(linkToFolderButton);
		actionMenuButtons.add(addAuthorizationButton);
		actionMenuButtons.add(shareFolderButton);
		actionMenuButtons.add(printLabelButton);
		actionMenuButtons.add(borrowButton);
		actionMenuButtons.add(returnFolderButton);

		return actionMenuButtons;
	}

	@Override
	public void setDocuments(RecordVODataProvider dataProvider) {
		Table documentsTable = new RecordVOTable(dataProvider);
		documentsTable.setSizeFull();
		documentsTable.addItemClickListener(new ItemClickListener() {
			@Override
			public void itemClick(ItemClickEvent event) {
				if (event.getButton() == MouseButton.LEFT) {
					RecordVOItem item = (RecordVOItem) event.getItem();
					RecordVO recordVO = item.getRecord();
					presenter.documentClicked(recordVO);
				}
			}
		});
		Component oldDocumentsComponent = documentsComponent;
		documentsComponent = documentsTable;
		tabSheet.replaceComponent(oldDocumentsComponent, documentsComponent);
	}

	@Override
	public void setSubFolders(RecordVODataProvider dataProvider) {
		Table subFoldersTable = new RecordVOTable(dataProvider);
		subFoldersTable.setSizeFull();
		subFoldersTable.addItemClickListener(new ItemClickListener() {
			@Override
			public void itemClick(ItemClickEvent event) {
				RecordVOItem item = (RecordVOItem) event.getItem();
				RecordVO recordVO = item.getRecord();
				presenter.subFolderClicked(recordVO);
			}
		});
		Component oldSubFoldersComponent = subFoldersComponent;
		subFoldersComponent = subFoldersTable;
		tabSheet.replaceComponent(oldSubFoldersComponent, subFoldersComponent);
	}

	@Override
	public void selectMetadataTab() {
		tabSheet.setSelectedTab(recordDisplay);
	}

	@Override
	public void selectDocumentsTab() {
		tabSheet.setSelectedTab(documentsComponent);
	}

	@Override
	public void selectSubFoldersTab() {
		tabSheet.setSelectedTab(subFoldersComponent);
	}

	@Override
	public void setLogicallyDeletable(ComponentState state) {
		deleteFolderButton.setVisible(state.isVisible());
		deleteFolderButton.setEnabled(state.isEnabled());

	}

	@Override
	public void setEditButtonState(ComponentState state) {
		editFolderButton.setVisible(state.isVisible());
		editFolderButton.setEnabled(state.isEnabled());
	}

	@Override
	public void setAddDocumentButtonState(ComponentState state) {
		addDocumentButton.setVisible(state.isVisible());
		addDocumentButton.setEnabled(state.isEnabled());
		dragNDropAllowed = state.isEnabled();
	}

	@Override
	public void setAddSubFolderButtonState(ComponentState state) {
		addSubFolderButton.setVisible(state.isVisible());
		addSubFolderButton.setEnabled(state.isEnabled());
	}

	@Override
	public void setDuplicateFolderButtonState(ComponentState state) {
		duplicateFolderButton.setVisible(state.isVisible());
		duplicateFolderButton.setEnabled(state.isEnabled());
	}

	@Override
	public void setPrintButtonState(ComponentState state) {
		printLabelButton.setVisible(state.isVisible());
		printLabelButton.setEnabled(state.isEnabled());
	}

	@Override
	public void setShareFolderButtonState(ComponentState state) {
		shareFolderButton.setVisible(state.isVisible());
		shareFolderButton.setEnabled(state.isEnabled());
	}

	@Override
	public void setAuthorizationButtonState(ComponentState state) {
		addAuthorizationButton.setVisible(state.isVisible());
		addAuthorizationButton.setEnabled(state.isEnabled());
	}

	@Override
	public void setBorrowButtonState(ComponentState state) {
		borrowButton.setVisible(state.isVisible());
		borrowButton.setEnabled(state.isEnabled());
	}

	@Override
	public void setReturnFolderButtonState(ComponentState state) {
		returnFolderButton.setVisible(state.isVisible());
		returnFolderButton.setEnabled(state.isEnabled());

	}

	@Override
	public void drop(DragAndDropEvent event) {
		if (dragNDropAllowed) {
			uploadField.drop(event);
		}
	}

	@Override
	public AcceptCriterion getAcceptCriterion() {
		return uploadField != null ? uploadField.getAcceptCriterion() : AcceptAll.get();
	}

	private Button buildBorrowButton() {
		return new WindowButton($("DisplayFolderView.borrow"),
				$("DisplayFolderView.borrow")) {
			@Override
			protected Component buildWindowContent() {

				final Field<?> lookupUser = new LookupRecordField(User.SCHEMA_TYPE);
				lookupUser.setCaption($("DisplayFolderView.borrower"));
				lookupUser.setId("borrower");
				lookupUser.addStyleName(USER_LOOKUP);

				final JodaDateField previewReturnDatefield = new JodaDateField();
				previewReturnDatefield.setCaption($("DisplayFolderView.previewReturnDate"));
				previewReturnDatefield.setRequired(true);
				previewReturnDatefield.setId("previewReturnDate");
				previewReturnDatefield.addStyleName("previewReturnDate");

				BaseButton borrowButton = new BaseButton($("DisplayFolderView.borrow")) {
					@Override
					protected void buttonClick(ClickEvent event) {
						String userId = null;
						if (lookupUser.getValue() != null) {
							userId = (String) lookupUser.getValue();
						}
						if (presenter.borrowFolder(previewReturnDatefield.getValue(), userId)) {
							getWindow().close();
						}
					}
				};
				borrowButton.addStyleName(ValoTheme.BUTTON_PRIMARY);

				BaseButton cancelButton = new BaseButton($("cancel")) {
					@Override
					protected void buttonClick(ClickEvent event) {
						getWindow().close();
					}
				};
				cancelButton.addStyleName(ValoTheme.BUTTON_PRIMARY);

				HorizontalLayout horizontalLayout = new HorizontalLayout();
				horizontalLayout.setSpacing(true);
				horizontalLayout.addComponents(borrowButton, cancelButton);

				VerticalLayout verticalLayout = new VerticalLayout();
				verticalLayout.addComponents(lookupUser, previewReturnDatefield, horizontalLayout);
				verticalLayout.setSpacing(true);

				return verticalLayout;
			}
		};
	}

	@Override
	public void setBorrowedMessage(String borrowedMessage) {
		if (borrowedMessage != null) {
			borrowedLabel.setVisible(true);
			borrowedLabel.setValue($(borrowedMessage));
		} else {
			borrowedLabel.setVisible(false);
			borrowedLabel.setValue(null);
		}
	}

}
