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

import java.io.IOException;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;

import com.constellio.app.modules.rm.services.RMSchemasRecordsServices;
import com.constellio.app.modules.rm.ui.builders.DocumentToVOBuilder;
import com.constellio.app.modules.rm.ui.entities.DocumentVO;
import com.constellio.app.modules.rm.wrappers.Document;
import com.constellio.app.services.factories.ConstellioFactories;
import com.constellio.app.ui.entities.ContentVersionVO;
import com.constellio.app.ui.entities.RecordVO.VIEW_MODE;
import com.constellio.app.ui.framework.builders.ContentVersionToVOBuilder;
import com.constellio.app.ui.pages.base.SchemaPresenterUtils;
import com.constellio.app.ui.pages.base.SessionContext;
import com.constellio.app.ui.pages.base.SingleSchemaBasePresenter;
import com.constellio.app.ui.params.ParamUtils;
import com.constellio.model.entities.records.Content;
import com.constellio.model.entities.records.ContentVersion;
import com.constellio.model.entities.records.Record;
import com.constellio.model.entities.records.wrappers.UserDocument;

public class AddEditDocumentPresenter extends SingleSchemaBasePresenter<AddEditDocumentView> {

	private DocumentToVOBuilder voBuilder = new DocumentToVOBuilder();

	private ContentVersionToVOBuilder contentVersionToVOBuilder = new ContentVersionToVOBuilder();

	private boolean addView;

	private DocumentVO documentVO;

	private String userDocumentId;

	private SchemaPresenterUtils userDocumentPresenterUtils;

	private transient RMSchemasRecordsServices rmSchemasRecordsServices;

	public AddEditDocumentPresenter(AddEditDocumentView view) {
		super(view, Document.DEFAULT_SCHEMA);
		initTransientObjects();
	}

	private void readObject(java.io.ObjectInputStream stream)
			throws IOException, ClassNotFoundException {
		stream.defaultReadObject();
		initTransientObjects();
	}

	private void initTransientObjects() {
		ConstellioFactories constellioFactories = view.getConstellioFactories();
		SessionContext sessionContext = view.getSessionContext();
		rmSchemasRecordsServices = new RMSchemasRecordsServices(collection, modelLayerFactory);
		userDocumentPresenterUtils = new SchemaPresenterUtils(UserDocument.DEFAULT_SCHEMA, constellioFactories, sessionContext);
	}

	public void forParams(String params) {
		Map<String, String> paramsMap = ParamUtils.getParamsMap(params);
		String id = paramsMap.get("id");
		String parentId = paramsMap.get("parentId");
		userDocumentId = paramsMap.get("userDocumentId");

		Document document;
		if (StringUtils.isNotBlank(id)) {
			document = rmSchemasRecordsServices.getDocument(id);
			addView = false;
		} else {
			document = rmSchemasRecordsServices.newDocument();
			addView = true;
		}

		documentVO = voBuilder.build(document.getWrappedRecord(), VIEW_MODE.FORM);
		if (parentId != null) {
			documentVO.set(Document.FOLDER, parentId);
		}

		if (addView && userDocumentId != null) {
			populateFromUserDocument(userDocumentId);
		}
		view.setRecord(documentVO);
	}

	protected void populateFromUserDocument(String userDocumentId) {
		Record userDocumentRecord = userDocumentPresenterUtils.getRecord(userDocumentId);
		UserDocument userDocument = new UserDocument(userDocumentRecord, userDocumentPresenterUtils.types());
		Content content = userDocument.getContent();
		ContentVersion contentVersion = content.getCurrentVersion();
		ContentVersionVO contentVersionVO = contentVersionToVOBuilder.build(content, contentVersion);
		// Reset as new content
		contentVersionVO.setHash(null);
		contentVersionVO.setVersion(null);
		documentVO.setContent(contentVersionVO);
		documentVO.setTitle(userDocument.getContent().getCurrentVersion().getFilename());
	}

	public boolean isAddView() {
		return addView;
	}

	public void cancelButtonClicked() {
		if (addView) {
			String parentId = documentVO.getFolder();
			if (parentId != null) {
				view.navigateTo().displayFolder(parentId);
			} else {
				view.navigateTo().recordsManagement();
			}
		} else {
			view.navigateTo().displayDocument(documentVO.getId());
		}
	}

	public void saveButtonClicked() {
		Record record = toRecord(documentVO);
		addOrUpdate(record);
		if (userDocumentId != null) {
			Record userDocumentRecord = userDocumentPresenterUtils.getRecord(userDocumentId);
			userDocumentPresenterUtils.delete(userDocumentRecord, null);
		}
		view.navigateTo().displayDocument(record.getId());
	}

}
