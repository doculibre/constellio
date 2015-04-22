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
package com.constellio.app.ui.pages.userDocuments;

import static com.constellio.app.ui.i18n.i18n.$;

import java.io.File;
import java.util.List;

import org.vaadin.dialogs.ConfirmDialog;
import org.vaadin.easyuploads.MultiFileUpload;

import com.constellio.app.ui.application.ConstellioUI;
import com.constellio.app.ui.entities.ContentVersionVO;
import com.constellio.app.ui.entities.UserDocumentVO;
import com.constellio.app.ui.framework.buttons.DeleteButton;
import com.constellio.app.ui.framework.components.content.DownloadContentVersionLink;
import com.constellio.app.ui.framework.components.fields.upload.BaseMultiFileUpload;
import com.constellio.app.ui.framework.components.table.RecordVOTable;
import com.constellio.app.ui.framework.containers.ButtonsContainer;
import com.constellio.app.ui.framework.containers.ButtonsContainer.ContainerButton;
import com.constellio.app.ui.pages.base.BaseViewImpl;
import com.vaadin.data.Item;
import com.vaadin.data.util.IndexedContainer;
import com.vaadin.event.dd.DragAndDropEvent;
import com.vaadin.event.dd.DropHandler;
import com.vaadin.event.dd.acceptcriteria.AcceptCriterion;
import com.vaadin.navigator.ViewChangeListener.ViewChangeEvent;
import com.vaadin.ui.AbstractSelect.ItemCaptionMode;
import com.vaadin.ui.Button;
import com.vaadin.ui.Component;
import com.vaadin.ui.DragAndDropWrapper;
import com.vaadin.ui.VerticalLayout;

public class ListUserDocumentsViewImpl extends BaseViewImpl implements ListUserDocumentsView, DropHandler {
	
	public static final String STYLE_NAME = "user-documents";
	
	public static final String STYLE_LAYOUT = STYLE_NAME + "-layout";
	
	public static final String TABLE_STYLE_NAME = STYLE_NAME + "-table";
	
	private static final String CAPTION_PROPERTY_ID = "caption";
	
	private DragAndDropWrapper dragAndDropWrapper;
	
	private VerticalLayout mainLayout;
	
	private MultiFileUpload multiFileUpload;
	
	private ButtonsContainer<IndexedContainer> userDocumentsContainer;
	
	private RecordVOTable userDocumentsTable;
	
	private ListUserDocumentsPresenter presenter;
	
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
		
		userDocumentsContainer = new ButtonsContainer<IndexedContainer>(new IndexedContainer());
		userDocumentsContainer.addContainerProperty(CAPTION_PROPERTY_ID, Component.class, null);
		
		List<ContainerButton> containerButtons = ConstellioUI.getCurrent().getUserDocumentContainerButtons();
		for (ContainerButton containerButton : containerButtons) {
			userDocumentsContainer.addButton(containerButton);
		}
		
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
		userDocumentsTable.setColumnHeader(CAPTION_PROPERTY_ID, $("ListUserDocumentsView.captionColumnTitle"));
		userDocumentsTable.setColumnHeader(ButtonsContainer.DEFAULT_BUTTONS_PROPERTY_ID, "");
		userDocumentsTable.setColumnExpandRatio(CAPTION_PROPERTY_ID, 1);
		
		mainLayout.addComponents(multiFileUpload, userDocumentsTable);

		dragAndDropWrapper = new DragAndDropWrapper(mainLayout);
		dragAndDropWrapper.setSizeFull();
		dragAndDropWrapper.setDropHandler(multiFileUpload);
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
		return new DownloadContentVersionLink(contentVersionVO);
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
			userDocumentsContainer.addItem(userDocumentVO);
			Item userDocumentItem = userDocumentsContainer.getItem(userDocumentVO);
			Component captionComponent = newCaptionComponent(userDocumentVO);
			userDocumentItem.getItemProperty(CAPTION_PROPERTY_ID).setValue(captionComponent);
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
