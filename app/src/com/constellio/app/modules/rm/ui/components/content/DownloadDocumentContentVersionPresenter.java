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
package com.constellio.app.modules.rm.ui.components.content;

import static com.constellio.app.ui.i18n.i18n.$;

import java.io.IOException;
import java.io.Serializable;

import com.constellio.app.modules.rm.services.RMSchemasRecordsServices;
import com.constellio.app.modules.rm.ui.entities.DocumentVO;
import com.constellio.app.modules.rm.ui.util.ConstellioAgentUtils;
import com.constellio.app.modules.rm.wrappers.Document;
import com.constellio.app.services.factories.ConstellioFactories;
import com.constellio.app.ui.entities.ContentVersionVO;
import com.constellio.app.ui.entities.RecordVO;
import com.constellio.app.ui.pages.base.SchemaPresenterUtils;
import com.constellio.app.ui.pages.base.SessionContext;
import com.constellio.model.entities.records.Record;
import com.constellio.model.entities.records.wrappers.User;
import com.constellio.model.services.factories.ModelLayerFactory;

public class DownloadDocumentContentVersionPresenter implements Serializable {
	
	private DownloadDocumentContentVersionLink link;
	
	private DocumentVO documentVO;
	
	private ContentVersionVO contentVersionVO;

	private SchemaPresenterUtils presenterUtils;

	private transient ConstellioFactories constellioFactories;
	
	private transient ModelLayerFactory modelLayerFactory;
	
	private transient RMSchemasRecordsServices rmSchemasRecordsServices;

	public DownloadDocumentContentVersionPresenter(DownloadDocumentContentVersionLink link) {
		this.link = link;
		
		RecordVO recordVO = link.getRecordVO();
		if (recordVO instanceof DocumentVO) {
			documentVO = (DocumentVO) recordVO;
		} else {
			documentVO = new DocumentVO(recordVO);
		}
		contentVersionVO = link.getContentVersionVO();

		initTransientObjects();

		SessionContext sessionContext = link.getSessionContext();
		presenterUtils = new SchemaPresenterUtils(Document.DEFAULT_SCHEMA, constellioFactories, sessionContext);
		
		boolean checkOutLinkVisible = isCheckOutLinkVisible();
		link.setCheckOutLinkVisible(checkOutLinkVisible);
		
		String readOnlyMessage;
		if (!hasWritePermission()) {
			readOnlyMessage = $("DownloadDocumentContentVersionLink.noWritePermission");
		} else if (isCheckedOutByOtherUser()) {
			readOnlyMessage = $("DownloadDocumentContentVersionLink.checkedOutByOtherUser");
		} else if (!isCheckedOut()) {
			readOnlyMessage = $("DownloadDocumentContentVersionLink.notCheckedOut");
		} else if (!isLatestVersion()) {
			readOnlyMessage = $("DownloadDocumentContentVersionLink.notLatestVersion");
		} else {
			readOnlyMessage = null;
		}
		link.setReadOnlyMessage(readOnlyMessage);
	}

	private void readObject(java.io.ObjectInputStream stream)
			throws IOException, ClassNotFoundException {
		stream.defaultReadObject();
		initTransientObjects();
	}
	
	private void initTransientObjects() {
		SessionContext sessionContext = link.getSessionContext();
		String collection = sessionContext.getCurrentCollection();
		
		constellioFactories = link.getConstellioFactories();
		modelLayerFactory = constellioFactories.getModelLayerFactory();
		
		rmSchemasRecordsServices = new RMSchemasRecordsServices(collection, modelLayerFactory);
	}
	
	private boolean hasWritePermission() {
		User currentUser = presenterUtils.getCurrentUser();
		Record record = presenterUtils.getRecord(documentVO.getId());
		return currentUser.hasWriteAccess().on(record);
	}

	private boolean isLatestVersion() {
		String latestVersion = documentVO.getContent().getVersion();
		String contentVersionVOVersion = contentVersionVO.getVersion();
		return latestVersion.equals(contentVersionVOVersion);
	}

	private boolean isCheckedOut() {
		return documentVO.getContent().getCheckoutUserId() != null;
	}

	private boolean isCheckedOutByOtherUser() {
		User currentUser = presenterUtils.getCurrentUser();
		String checkOutUserId = documentVO.getContent().getCheckoutUserId();
		return checkOutUserId != null && !checkOutUserId.equals(currentUser.getId());
	}

	private boolean isCheckOutLinkVisible() {
		return hasWritePermission() && !isCheckedOut() && isLatestVersion();
	}
	
	void checkOutLinkClicked() {
		if (!isCheckedOut()) {
			User currentUser = presenterUtils.getCurrentUser();
			Document document = rmSchemasRecordsServices.getDocument(documentVO.getId());
			document.getContent().checkOut(currentUser);
			presenterUtils.addOrUpdate(document.getWrappedRecord());
			
			link.closeWindow();
			link.navigateTo().displayDocument(documentVO.getId());
			String agentURL = ConstellioAgentUtils.getAgentURL(documentVO, contentVersionVO);
			link.open(agentURL);
		}
	}

}
