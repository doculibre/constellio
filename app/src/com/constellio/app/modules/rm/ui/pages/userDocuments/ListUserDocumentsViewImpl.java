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
package com.constellio.app.modules.rm.ui.pages.userDocuments;

import static com.constellio.app.ui.i18n.i18n.$;

import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.vaadin.dialogs.ConfirmDialog;
import org.vaadin.easyuploads.MultiFileUpload;

import com.constellio.app.modules.rm.ui.components.userDocuments.DeclareUserDocumentContainerButton;
import com.constellio.app.modules.rm.wrappers.Folder;
import com.constellio.app.ui.entities.ContentVersionVO;
import com.constellio.app.ui.entities.UserDocumentVO;
import com.constellio.app.ui.framework.buttons.DeleteButton;
import com.constellio.app.ui.framework.components.ContentVersionDisplay;
import com.constellio.app.ui.framework.components.converters.RecordIdToCaptionConverter;
import com.constellio.app.ui.framework.components.fields.lookup.LookupRecordField;
import com.constellio.app.ui.framework.components.fields.upload.BaseMultiFileUpload;
import com.constellio.app.ui.framework.components.table.RecordVOTable;
import com.constellio.app.ui.framework.containers.ButtonsContainer;
import com.constellio.app.ui.framework.containers.ButtonsContainer.ContainerButton;
import com.constellio.app.ui.pages.base.BaseViewImpl;
import com.vaadin.data.Item;
import com.vaadin.data.Property;
import com.vaadin.data.util.AbstractProperty;
import com.vaadin.data.util.IndexedContainer;
import com.vaadin.data.util.ObjectProperty;
import com.vaadin.event.dd.DragAndDropEvent;
import com.vaadin.event.dd.DropHandler;
import com.vaadin.event.dd.acceptcriteria.AcceptCriterion;
import com.vaadin.navigator.ViewChangeListener.ViewChangeEvent;
import com.vaadin.ui.AbstractSelect.ItemCaptionMode;
import com.vaadin.ui.Alignment;
import com.vaadin.ui.Button;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.Button.ClickListener;
import com.vaadin.ui.CheckBox;
import com.vaadin.ui.Component;
import com.vaadin.ui.DragAndDropWrapper;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Label;
import com.vaadin.ui.VerticalLayout;

public class ListUserDocumentsViewImpl extends BaseViewImpl implements ListUserDocumentsView, DropHandler {
	
	public static final String STYLE_NAME = "user-documents";
	
	public static final String STYLE_LAYOUT = STYLE_NAME + "-layout";
	
	public static final String TABLE_STYLE_NAME = STYLE_NAME + "-table";
	
	private static final String SELECT_PROPERTY_ID = "select";
	
	private static final String CAPTION_PROPERTY_ID = "caption";
	
	private static final String FOLDER_PROPERTY_ID = "folder";
	
	private DragAndDropWrapper dragAndDropWrapper;
	
	private VerticalLayout mainLayout;
	
	private MultiFileUpload multiFileUpload;
	
	private HorizontalLayout setFolderLayout;
	
	private LookupRecordField setFolderLookupField;
	
	private Button setFolderButton;
	
	private List<UserDocumentVO> selectedUserDocuments = new ArrayList<>();
	
	private ButtonsContainer<IndexedContainer> userDocumentsContainer;
	
	private RecordVOTable userDocumentsTable;
	
	private ListUserDocumentsPresenter presenter;
	
	private RecordIdToCaptionConverter recordIdToCaptionConverter = new RecordIdToCaptionConverter();
	
	public ListUserDocumentsViewImpl() {
		addStyleName(STYLE_NAME);
		setCaption($("UserDocumentsWindow.title"));
		
		mainLayout = new VerticalLayout();
		mainLayout.addStyleName(STYLE_LAYOUT);
		mainLayout.setSpacing(true);
//		mainLayout.setSizeFull();
		
		multiFileUpload = new BaseMultiFileUpload() {
			@Override
			protected void handleFile(File file, String fileName, String mimeType, long length) {
				presenter.handleFile(file, fileName, mimeType, length);
			}
		};
		multiFileUpload.setWidth("100%");
		
		setFolderLayout = new HorizontalLayout();
		setFolderLayout.setSpacing(true);
		
		setFolderLookupField = new LookupRecordField(Folder.SCHEMA_TYPE);
		
		setFolderButton = new Button($("ListUserDocumentsView.setFolder"));
		setFolderButton.addClickListener(new ClickListener() {
			@Override
			public void buttonClick(ClickEvent event) {
				presenter.setFolderButtonClicked();
			}
		});
		
		userDocumentsContainer = new ButtonsContainer<IndexedContainer>(new IndexedContainer()) {
			@Override
			public Collection<?> getContainerPropertyIds() {
				List<Object> containerPropertyIds = new ArrayList<>();
				Collection<?> parentContainerPropertyIds = super.getContainerPropertyIds();
				containerPropertyIds.add(SELECT_PROPERTY_ID);
				containerPropertyIds.addAll(parentContainerPropertyIds);
				return containerPropertyIds;
			}

			@Override
			protected Property<?> getOwnContainerProperty(final Object itemId, final Object propertyId) {
				Property<?> property;
				if (SELECT_PROPERTY_ID.equals(propertyId)) {
					Property<?> selectProperty = new AbstractProperty<Boolean>() {
						@Override
						public Boolean getValue() {
							UserDocumentVO userDocumentVO = (UserDocumentVO) itemId;
							return selectedUserDocuments.contains(userDocumentVO);
						}

						@Override
						public void setValue(Boolean newValue)
								throws com.vaadin.data.Property.ReadOnlyException {
							UserDocumentVO userDocumentVO = (UserDocumentVO) itemId;
							if (Boolean.TRUE.equals(newValue)) {
								if (!selectedUserDocuments.contains(userDocumentVO)) {
									selectedUserDocuments.add(userDocumentVO);
								}
							} else {
								selectedUserDocuments.remove(userDocumentVO);
							}
						}

						@Override
						public Class<? extends Boolean> getType() {
							return Boolean.class;
						}
					};
					CheckBox checkBox = new CheckBox();
					checkBox.setPropertyDataSource(selectProperty);
					property = new ObjectProperty<CheckBox>(checkBox);
				} else {
					property = super.getOwnContainerProperty(itemId, propertyId);
				}
				return property;
			}

			@Override
			protected Class<?> getOwnType(Object propertyId) {
				Class<?> ownType;
				if (SELECT_PROPERTY_ID.equals(propertyId)) {
					ownType = CheckBox.class;
				} else {
					ownType = super.getOwnType(propertyId);
				}
				return ownType;
			}
		};
		userDocumentsContainer.addContainerProperty(CAPTION_PROPERTY_ID, Component.class, null);
		userDocumentsContainer.addContainerProperty(FOLDER_PROPERTY_ID, Component.class, null);
		
		userDocumentsContainer.addButton(new DeclareUserDocumentContainerButton());
		userDocumentsContainer.addButton(new ContainerButton() {
			@Override
			protected Button newButtonInstance(final Object itemId) {
				return new DeleteButton() {
					@Override
					protected void confirmButtonClick(ConfirmDialog dialog) {
						presenter.deleteButtonClicked((UserDocumentVO) itemId);
					}
				};
			}
		});

		userDocumentsTable = new RecordVOTable();
		userDocumentsTable.setContainerDataSource(userDocumentsContainer);
		userDocumentsTable.setWidth("100%");
		userDocumentsTable.addStyleName(TABLE_STYLE_NAME);
		userDocumentsTable.setItemCaptionMode(ItemCaptionMode.PROPERTY);
		userDocumentsTable.setItemCaptionPropertyId(CAPTION_PROPERTY_ID);
		userDocumentsTable.setColumnHeader(SELECT_PROPERTY_ID, $("ListUserDocumentsView.selectColumnTitle"));
		userDocumentsTable.setColumnHeader(CAPTION_PROPERTY_ID, $("ListUserDocumentsView.captionColumnTitle"));
		userDocumentsTable.setColumnHeader(FOLDER_PROPERTY_ID, $("ListUserDocumentsView.folderColumnTitle"));
		userDocumentsTable.setColumnHeader(ButtonsContainer.DEFAULT_BUTTONS_PROPERTY_ID, "");
		userDocumentsTable.setColumnExpandRatio(CAPTION_PROPERTY_ID, 1);
		
		mainLayout.addComponents(multiFileUpload, setFolderLayout, userDocumentsTable);
		setFolderLayout.addComponents(setFolderLookupField, setFolderButton);
		
		mainLayout.setComponentAlignment(setFolderLayout, Alignment.MIDDLE_CENTER);

		dragAndDropWrapper = new DragAndDropWrapper(mainLayout);
		dragAndDropWrapper.setSizeFull();
		dragAndDropWrapper.setDropHandler(multiFileUpload);
	}
	
	@Override
	public String getFolderId() {
		return setFolderLookupField.getValue();
	}

	@Override
	public void setFolderId(String folderId) {
		setFolderLookupField.setValue(folderId);
	}

	@Override
	public List<UserDocumentVO> getSelectedUserDocuments() {
		return selectedUserDocuments;
	}

	@Override
	public void setSelectedUserDocuments(List<UserDocumentVO> userDocuments) {
		this.selectedUserDocuments = userDocuments;
	}

	@Override
	protected String getTitle() {
		return $("ListUserDocumentsView.viewTitle");
	}

	@Override
	protected Component buildMainComponent(ViewChangeEvent event) {	
		return dragAndDropWrapper;
	}
	
	@Override
	protected void afterViewAssembled(ViewChangeEvent event) {
		presenter = new ListUserDocumentsPresenter(this);
		presenter.viewAssembled();
	}
	
	protected Component newCaptionComponent(UserDocumentVO userDocumentVO) {
		ContentVersionVO contentVersionVO = userDocumentVO.getContent();
		return new ContentVersionDisplay(userDocumentVO, contentVersionVO, contentVersionVO.getFileName());
	}

	@SuppressWarnings("unchecked")
	@Override
	public List<UserDocumentVO> getUserDocuments() {
		return (List<UserDocumentVO>) userDocumentsContainer.getItemIds();
	}

	@Override
	public void setUserDocuments(List<UserDocumentVO> userDocumentVOs) {
		userDocumentsContainer.removeAllItems();
		for (UserDocumentVO userDocumentVO : userDocumentVOs) {
			addUserDocument(userDocumentVO);
		}
	}

	@SuppressWarnings("unchecked")
	@Override
	public void addUserDocument(UserDocumentVO userDocumentVO) {
		if (!userDocumentsContainer.containsId(userDocumentVO)) {
			String folderId = userDocumentVO.getFolder();
			String folderCaption;
			if (folderId != null) {
				folderCaption = recordIdToCaptionConverter.convertToPresentation(folderId, String.class, getLocale());
			} else {
				folderCaption = null;
			}
			userDocumentsContainer.addItem(userDocumentVO);
			Item userDocumentItem = userDocumentsContainer.getItem(userDocumentVO);
			Component captionComponent = newCaptionComponent(userDocumentVO);
			Label folderLabel = new Label(folderCaption);
			userDocumentItem.getItemProperty(CAPTION_PROPERTY_ID).setValue(captionComponent);
			userDocumentItem.getItemProperty(FOLDER_PROPERTY_ID).setValue(folderLabel);
		}
	}

	@Override
	public void removeUserDocument(UserDocumentVO userDocumentVO) {
		userDocumentsContainer.removeItem(userDocumentVO);
	}

	@Override
	public void drop(DragAndDropEvent event) {
		setVisible(true);
		multiFileUpload.drop(event);
	}

	@Override
	public AcceptCriterion getAcceptCriterion() {
		return multiFileUpload.getAcceptCriterion();
	}

}
