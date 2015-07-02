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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.apache.commons.lang3.ObjectUtils;

import com.constellio.app.modules.rm.ui.builders.DocumentToVOBuilder;
import com.constellio.app.modules.rm.ui.components.document.DocumentActionsPresenterUtils;
import com.constellio.app.modules.rm.ui.entities.DocumentVO;
import com.constellio.app.modules.rm.wrappers.Document;
import com.constellio.app.ui.entities.ContentVersionVO;
import com.constellio.app.ui.entities.RecordVO.VIEW_MODE;
import com.constellio.app.ui.framework.builders.ContentVersionToVOBuilder;
import com.constellio.app.ui.pages.base.SingleSchemaBasePresenter;
import com.constellio.model.entities.records.Content;
import com.constellio.model.entities.records.ContentVersion;
import com.constellio.model.entities.records.Record;
import com.constellio.model.entities.records.wrappers.User;
import com.constellio.model.services.factories.ModelLayerFactory;
import com.constellio.model.services.records.RecordServicesRuntimeException.NoSuchRecordWithId;

public class DisplayDocumentPresenter extends SingleSchemaBasePresenter<DisplayDocumentView> {
	
	protected DocumentToVOBuilder voBuilder = new DocumentToVOBuilder();
	protected ContentVersionToVOBuilder contentVersionVOBuilder = new ContentVersionToVOBuilder();
	private DocumentActionsPresenterUtils<DisplayDocumentView> presenterUtils;

	public DisplayDocumentPresenter(final DisplayDocumentView view) {
		super(view);
		presenterUtils = new DocumentActionsPresenterUtils<DisplayDocumentView>(view) {
			@Override
			public void updateActionsComponent() {
				super.updateActionsComponent();
				view.refreshMetadataDisplay();
				updateContentVersions();
			}
		};
	}

	@Override
	protected boolean hasPageAccess(String params, User user) {
		return true;
	}

	public void forParams(String params) {
		Record record = getRecord(params);
		DocumentVO documentVO = voBuilder.build(record, VIEW_MODE.DISPLAY);
		view.setRecordVO(documentVO);
		presenterUtils.setRecordVO(documentVO);
		ModelLayerFactory modelLayerFactory = view.getConstellioFactories().getModelLayerFactory();
		User user = getCurrentUser();
		modelLayerFactory.newLoggingServices().logRecordView(record, user);
	}

	public void backgroundViewMonitor() {
		DocumentVO documentVO = presenterUtils.getDocumentVO();
		try {
			ContentVersionVO contentVersionVO = documentVO.getContent();
			String contentVersionNumber = contentVersionVO != null ? contentVersionVO.getVersion() : null;
			String checkoutUserId = contentVersionVO != null ? contentVersionVO.getCheckoutUserId() : null;
			Long length = contentVersionVO != null ? contentVersionVO.getLength() : null;
			Record currentRecord = getRecord(documentVO.getId());
			Document currentDocument = new Document(currentRecord, types());
			Content currentContent = currentDocument.getContent();
			ContentVersion currentContentVersion = currentContent != null ? currentContent.getCurrentVersionSeenBy(getCurrentUser()) : null;
			String currentContentVersionNumber = currentContentVersion != null ? currentContentVersion.getVersion() : null;
			String currentCheckoutUserId = currentContent != null ? currentContent.getCheckoutUserId() : null;
			Long currentLength = currentContentVersion != null ? currentContentVersion.getLength() : null;
			if (ObjectUtils.notEqual(contentVersionNumber, currentContentVersionNumber) 
					|| ObjectUtils.notEqual(checkoutUserId, currentCheckoutUserId) 
					|| ObjectUtils.notEqual(length, currentLength)) {
				documentVO = voBuilder.build(currentRecord, VIEW_MODE.DISPLAY);
				view.setRecordVO(documentVO);
				presenterUtils.setRecordVO(documentVO);
				presenterUtils.updateActionsComponent();
			}
		} catch (NoSuchRecordWithId e) {
			view.navigateTo().home();
		}
	}

	@Override
	protected boolean hasRestrictedRecordAccess(String params, User user, Record restrictedRecord) {
		return user.hasReadAccess().on(restrictedRecord);
	}

	@Override
	protected List<String> getRestrictedRecordIds(String params) {
		DocumentVO documentVO = presenterUtils.getDocumentVO();
		return Arrays.asList(documentVO.getId());
	}

	public void viewAssembled() {
		presenterUtils.updateActionsComponent();
	}

	private void updateContentVersions() {
		List<ContentVersionVO> contentVersionVOs = new ArrayList<ContentVersionVO>();
		DocumentVO documentVO = presenterUtils.getDocumentVO();
		Record record = getRecord(documentVO.getId());
		Document document = new Document(record, types());

		Content content = document.getContent();
		if (content != null) {
			for (ContentVersion contentVersion : content.getHistoryVersions()) {
				ContentVersionVO contentVersionVO = contentVersionVOBuilder.build(content, contentVersion);
				contentVersionVOs.add(contentVersionVO);
			}
			ContentVersion currentVersion = content.getCurrentVersionSeenBy(getCurrentUser());
			ContentVersionVO currentVersionVO = contentVersionVOBuilder.build(content, currentVersion);
			contentVersionVOs.remove(currentVersionVO);
			contentVersionVOs.add(currentVersionVO);
		}
		Collections.reverse(contentVersionVOs);
		view.setContentVersions(contentVersionVOs);
	}

	public void backButtonClicked() {
		DocumentVO documentVO = presenterUtils.getDocumentVO();
		String parentId = documentVO.get(Document.FOLDER);
		if (parentId != null) {
			view.navigateTo().displayFolder(parentId);
		} else {
			view.navigateTo().recordsManagement();
		}
	}

	public boolean isDeleteContentVersionPossible() {
		return presenterUtils.isDeleteContentVersionPossible();
	}

	public boolean isDeleteContentVersionPossible(ContentVersionVO contentVersionVO) {
		return presenterUtils.isDeleteContentVersionPossible(contentVersionVO);
	}

	public void deleteContentVersionButtonClicked(ContentVersionVO contentVersionVO) {
		presenterUtils.deleteContentVersionButtonClicked(contentVersionVO);
	}

	public void editDocumentButtonClicked() {
		presenterUtils.editDocumentButtonClicked();
	}

	public void deleteDocumentButtonClicked() {
		presenterUtils.deleteDocumentButtonClicked();
	}

	public void linkToDocumentButtonClicked() {
		presenterUtils.linkToDocumentButtonClicked();
	}

	public void addAuthorizationButtonClicked() {
		presenterUtils.addAuthorizationButtonClicked();
	}

	public void shareDocumentButtonClicked() {
		presenterUtils.shareDocumentButtonClicked();
	}

	public void uploadButtonClicked() {
		presenterUtils.uploadButtonClicked();
	}

	public void checkInButtonClicked() {
		presenterUtils.checkInButtonClicked();
	}

	public void checkOutButtonClicked() {
		presenterUtils.checkOutButtonClicked();
	}

	public void finalizeButtonClicked() {
		presenterUtils.finalizeButtonClicked();
	}

	public void updateWindowClosed() {
		presenterUtils.updateWindowClosed();
	}

	public String getDocumentTitle() {
		DocumentVO documentVO = presenterUtils.getDocumentVO();
		return documentVO.getTitle();
	}

	public void copyContentButtonClicked() {
		presenterUtils.copyContentButtonClicked();
	}

	public String getContentTitle() {
		return presenterUtils.getContentTitle();
	}

	public void renameContentButtonClicked(String newContentTitle) {
		Document document = presenterUtils.renameContentButtonClicked(newContentTitle);
		if(document != null){
			addOrUpdate(document.getWrappedRecord());
			view.navigateTo().displayDocument(document.getId());
		}
	}

	public boolean hasContent() {
		return presenterUtils.hasContent();
	}
}
