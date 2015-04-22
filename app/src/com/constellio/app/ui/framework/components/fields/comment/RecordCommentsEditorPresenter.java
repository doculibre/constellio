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
package com.constellio.app.ui.framework.components.fields.comment;

import java.io.Serializable;
import java.util.List;

import com.constellio.app.modules.rm.wrappers.structures.Comment;
import com.constellio.app.services.factories.ConstellioFactories;
import com.constellio.app.ui.entities.RecordVO;
import com.constellio.app.ui.pages.base.SchemaPresenterUtils;
import com.constellio.app.ui.pages.base.SessionContext;
import com.constellio.model.entities.records.Record;
import com.constellio.model.entities.records.wrappers.User;
import com.constellio.model.entities.schemas.Metadata;
import com.constellio.model.services.factories.ModelLayerFactory;
import com.constellio.model.services.records.RecordServices;
import com.constellio.model.services.security.AuthorizationsServices;

public class RecordCommentsEditorPresenter implements Serializable {

	private RecordCommentsEditor editor;

	private String recordId;

	private String metadataCode;

	private SchemaPresenterUtils presenterUtils;

	public RecordCommentsEditorPresenter(RecordCommentsEditor editor) {
		this.editor = editor;
	}

	public void forRecordVO(RecordVO recordVO, String metadataCode) {
		this.recordId = recordVO.getId();
		this.metadataCode = metadataCode;
		init();
	}

	public void forRecordId(String recordId, String metadataCode) {
		this.recordId = recordId;
		this.metadataCode = metadataCode;
		init();
	}

	private void init() {
		SessionContext sessionContext = editor.getSessionContext();
		ConstellioFactories constellioFactories = editor.getConstellioFactories();
		ModelLayerFactory modelLayerFactory = constellioFactories.getModelLayerFactory();
		RecordServices recordServices = modelLayerFactory.newRecordServices();

		AuthorizationsServices authorizationsServices = modelLayerFactory.newAuthorizationsServices();

		Record record = recordServices.getDocumentById(recordId);
		String schemaCode = record.getSchemaCode();

		presenterUtils = new SchemaPresenterUtils(schemaCode, constellioFactories, sessionContext);

		Metadata metadata = presenterUtils.getMetadata(metadataCode);
		String caption = metadata.getLabel();
		List<Comment> comments = record.get(metadata);
		editor.setComments(comments);
		editor.setCaption(caption);

		User currentUser = presenterUtils.getCurrentUser();
		if (!authorizationsServices.canWrite(currentUser, record)) {
			editor.setVisible(false);
		}
	}

	public void commentsChanged(List<Comment> comments) {

		if (comments != null) {

			User user = presenterUtils.getCurrentUser();
			ConstellioFactories constellioFactories = editor.getConstellioFactories();
			ModelLayerFactory modelLayerFactory = constellioFactories.getModelLayerFactory();
			AuthorizationsServices authorizationsServices = modelLayerFactory.newAuthorizationsServices();

			Record record = presenterUtils.getRecord(recordId);
			Metadata metadata = presenterUtils.getMetadata(metadataCode);
			record.set(metadata, comments);
			if (authorizationsServices.canWrite(user, record)) {
				presenterUtils.addOrUpdate(record);
			}
		}
	}

}
