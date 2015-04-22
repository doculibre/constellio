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
package com.constellio.app.modules.rm.ui.components.contextmenu;

import com.constellio.app.modules.rm.ui.components.document.DocumentActionsComponentPresenter;
import com.constellio.app.modules.rm.ui.entities.DocumentVO;
import com.constellio.app.modules.rm.wrappers.Document;
import com.constellio.app.ui.entities.ContentVersionVO;
import com.constellio.app.ui.entities.RecordVO;
import com.constellio.app.ui.entities.RecordVO.VIEW_MODE;
import com.constellio.model.entities.records.Content;
import com.constellio.model.entities.records.Record;
import com.constellio.model.services.schemas.SchemaUtils;

public class DocumentContextMenuPresenter extends DocumentActionsComponentPresenter<DocumentContextMenu> {

	private DocumentContextMenu contextMenu;
	
	public DocumentContextMenuPresenter(DocumentContextMenu contextMenu) {
		super(contextMenu);
		this.contextMenu = contextMenu;
	}

	@Override
	public void setRecordVO(RecordVO recordVO) {
		super.setRecordVO(recordVO);
		updateActionsComponent();
	}

	@Override
	protected void updateActionsComponent() {
		super.updateActionsComponent();
		Content content = getContent();
		if (content != null) {
			ContentVersionVO contentVersionVO = contentVersionVOBuilder.build(content);
			contextMenu.setContentVersionVO(contentVersionVO);
			contextMenu.setDownloadDocumentButtonVisible(true);
		} else {
			contextMenu.setDownloadDocumentButtonVisible(false);
		}
		contextMenu.buildMenuItems();
	}

	public void displayDocumentButtonClicked() {
		contextMenu.navigateTo().displayDocument(documentVO.getId());
	}

	public boolean openForRequested(String recordId) {
		boolean showContextMenu;
		Record record = presenterUtils.getRecord(recordId);
		String recordSchemaCode = record.getSchemaCode();
		String recordSchemaTypeCode = new SchemaUtils().getSchemaTypeCode(recordSchemaCode);
		if (Document.SCHEMA_TYPE.equals(recordSchemaTypeCode)) {
			this.documentVO = voBuilder.build(record, VIEW_MODE.DISPLAY);
			contextMenu.setRecordVO(documentVO);
			updateActionsComponent();
			showContextMenu = true;
		} else {
			showContextMenu = false;
		}
		contextMenu.setVisible(showContextMenu);
		return showContextMenu;
	}

	public boolean openForRequested(RecordVO recordVO) {
		boolean showContextMenu;
		String recordSchemaCode = recordVO.getSchema().getCode();
		String recordSchemaTypeCode = new SchemaUtils().getSchemaTypeCode(recordSchemaCode);
		if (Document.SCHEMA_TYPE.equals(recordSchemaTypeCode)) {
			this.documentVO = new DocumentVO(recordVO);
			contextMenu.setRecordVO(documentVO);
			updateActionsComponent();
			showContextMenu = true;
		} else {
			showContextMenu = false;
		}
		contextMenu.setVisible(showContextMenu);
		return showContextMenu;
	}

}
